package net.decimation.fixes.hosting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Direct joins over IPv6 (docs/player_hosting.md section 15; user, 18 Oktober 2026: the relay's path through Europe was
 * too slow). The host offers its global IPv6 addresses with the port of its {@link Gate}; a joiner who clicks Join first
 * probes them ("DRPROBE room", answered "DROK" only by the gate of that world) and plays straight to the host, else
 * through the relay as before. The gate applies the same login checks as the relay path before the world sees a byte.
 */
public final class Direct
{
    static final String PROBE = "DRPROBE ", OK = "DROK\n";
    private static final int MAX = 4, HELD_MAX = 4096, FIRST_BYTES_MS = 10000;

    private Direct()
    {
    }

    /** Told when a gated connection ends, with the login name it reserved (null for a status ping). */
    public interface Closed
    {
        void closed(String name);
    }

    /** This machine's global IPv6 addresses (2000::/3) as "[address]:port", at most 4. */
    public static List<String> addresses(int port)
    {
        List<InetAddress> all = new ArrayList<InetAddress>();
        try
        {
            for (NetworkInterface n : Collections.list(NetworkInterface.getNetworkInterfaces()))
            {
                if (n.isUp() && !n.isLoopback() && !n.isVirtual())
                {
                    all.addAll(Collections.list(n.getInetAddresses()));
                }
            }
        }
        catch (IOException e)
        {
            return Collections.emptyList();
        }
        return addresses(all, port);
    }

    static List<String> addresses(List<InetAddress> all, int port)
    {
        List<String> out = new ArrayList<String>();
        for (InetAddress a : all)
        {
            if (a instanceof Inet6Address && (a.getAddress()[0] & 0xe0) == 0x20 && out.size() < MAX)
            {
                String s = a.getHostAddress();
                int scope = s.indexOf('%');
                out.add("[" + (scope < 0 ? s : s.substring(0, scope)) + "]:" + port);
            }
        }
        return out;
    }

    /** True when the gate of `room` answers at "[address]:port" within the timeout. */
    public static boolean probe(String address, String room, int timeoutMs)
    {
        Socket s = new Socket();
        try
        {
            s.connect(socketAddress(address), timeoutMs);
            s.setSoTimeout(timeoutMs);
            s.getOutputStream().write((PROBE + room + "\n").getBytes(StandardCharsets.US_ASCII));
            byte[] want = OK.getBytes(StandardCharsets.US_ASCII), got = new byte[want.length];
            InputStream in = s.getInputStream();
            for (int i = 0; i < got.length; i++)
            {
                int b = in.read();
                if (b < 0)
                {
                    return false;
                }
                got[i] = (byte) b;
            }
            return java.util.Arrays.equals(want, got);
        }
        catch (IOException | IllegalArgumentException e)
        {
            return false;
        }
        finally
        {
            close(s);
        }
    }

    /** The first address whose gate answers for `room`, or null (then the relay carries the join). */
    public static String pick(List<String> addresses, String room, int timeoutMs)
    {
        for (String a : addresses)
        {
            if (probe(a, room, timeoutMs))
            {
                return a;
            }
        }
        return null;
    }

    static InetSocketAddress socketAddress(String address)
    {
        int close = address.lastIndexOf(']');
        if (!address.startsWith("[") || close < 0 || address.length() < close + 3 || address.charAt(close + 1) != ':')
        {
            throw new IllegalArgumentException("not [ipv6]:port: " + address);
        }
        return new InetSocketAddress(address.substring(1, close), Integer.parseInt(address.substring(close + 2)));
    }

    /** Copies one way until either side closes, then closes both. */
    static void pump(final InputStream in, final OutputStream out, final Socket a, final Socket b, final Runnable done, String name)
    {
        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                byte[] buf = new byte[8192];
                try
                {
                    for (int n; (n = in.read(buf)) > 0; )
                    {
                        out.write(buf, 0, n);
                        out.flush();
                    }
                }
                catch (IOException e)
                {
                    // one side closed
                }
                close(a);
                close(b);
                if (done != null)
                {
                    done.run();
                }
            }
        }, name);
        t.setDaemon(true);
        t.start();
    }

    static void close(Socket s)
    {
        try
        {
            s.close();
        }
        catch (IOException e)
        {
            // closed
        }
    }

    /**
     * The host's direct port: answers the probe for its room, holds a joiner's first bytes until the gate decides (the
     * relay path's {@link HostService#admit}), then carries the connection to the world's LAN port on 127.0.0.1.
     */
    public static final class Gate implements AutoCloseable
    {
        private final ServerSocket server;
        private final int lanPort;
        private volatile String room;
        private final Pipe.Gate gate;
        private final Closed closed;
        private final Set<Socket> live = Collections.newSetFromMap(new ConcurrentHashMap<Socket, Boolean>());

        private Gate(ServerSocket server, int lanPort, String room, Pipe.Gate gate, Closed closed)
        {
            this.server = server;
            this.lanPort = lanPort;
            this.room = room;
            this.gate = gate;
            this.closed = closed;
        }

        /** Listens on every address (IPv6 and IPv4) at a free port. */
        public static Gate open(int lanPort, String room, Pipe.Gate gate, Closed closed) throws IOException
        {
            final Gate g = new Gate(new ServerSocket(0, 16), lanPort, room, gate, closed);
            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    while (!g.server.isClosed())
                    {
                        try
                        {
                            g.serve(g.server.accept());
                        }
                        catch (IOException e)
                        {
                            return; // closed
                        }
                    }
                }
            }, "DeciRemnant direct gate");
            t.setDaemon(true);
            t.start();
            return g;
        }

        public int port()
        {
            return server.getLocalPort();
        }

        /** The room the relay gave (known only after registering, which needs this port first). */
        public void room(String r)
        {
            room = r;
        }

        /** Joiners connected right now (probes and refused ones are not counted). */
        public int connections()
        {
            return live.size();
        }

        private void serve(final Socket s)
        {
            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    handle(s);
                }
            }, "DeciRemnant direct joiner");
            t.setDaemon(true);
            t.start();
        }

        private void handle(final Socket s)
        {
            String admitted = null;
            try
            {
                s.setSoTimeout(FIRST_BYTES_MS);
                InputStream in = s.getInputStream();
                ByteArrayOutputStream held = new ByteArrayOutputStream();
                String r = room;
                byte[] probe = (PROBE + (r == null ? "-" : r) + "\n").getBytes(StandardCharsets.US_ASCII);
                byte[] buf = new byte[1024];
                int verdict = Pipe.Gate.MORE;
                while (verdict == Pipe.Gate.MORE)
                {
                    int n = in.read(buf);
                    if (n < 0 || held.size() + n > HELD_MAX)
                    {
                        Direct.close(s);
                        return;
                    }
                    held.write(buf, 0, n);
                    byte[] h = held.toByteArray();
                    if (startsWith(h, PROBE.getBytes(StandardCharsets.US_ASCII)))
                    {
                        if (h.length < probe.length && h[h.length - 1] != '\n')
                        {
                            continue; // the rest of the probe line
                        }
                        if (java.util.Arrays.equals(h, probe))
                        {
                            s.getOutputStream().write(OK.getBytes(StandardCharsets.US_ASCII));
                        }
                        Direct.close(s);
                        return;
                    }
                    verdict = gate.decide(h);
                }
                if (verdict != Pipe.Gate.ALLOW)
                {
                    Direct.close(s);
                    return;
                }
                String name = LoginName.of(held.toByteArray());
                admitted = name == null || name.equals(LoginName.STATUS) ? null : name;
                s.setSoTimeout(0);
                final Socket lan = new Socket(InetAddress.getLoopbackAddress(), lanPort);
                lan.getOutputStream().write(held.toByteArray());
                live.add(s);
                final String who = admitted;
                final boolean[] once = {false};
                Runnable done = new Runnable()
                {
                    public void run()
                    {
                        synchronized (once)
                        {
                            if (once[0])
                            {
                                return;
                            }
                            once[0] = true;
                        }
                        live.remove(s);
                        if (closed != null)
                        {
                            closed.closed(who);
                        }
                    }
                };
                pump(in, lan.getOutputStream(), s, lan, done, "DeciRemnant direct up");
                pump(lan.getInputStream(), s.getOutputStream(), s, lan, done, "DeciRemnant direct down");
            }
            catch (IOException e)
            {
                Direct.close(s);
                if (admitted != null && closed != null)
                {
                    closed.closed(admitted);
                }
            }
        }

        private static boolean startsWith(byte[] h, byte[] p)
        {
            int n = Math.min(h.length, p.length);
            for (int i = 0; i < n; i++)
            {
                if (h[i] != p[i])
                {
                    return false;
                }
            }
            return true;
        }

        public void close()
        {
            try
            {
                server.close();
            }
            catch (IOException e)
            {
                // closed
            }
            for (Socket s : live)
            {
                Direct.close(s);
            }
        }
    }
}

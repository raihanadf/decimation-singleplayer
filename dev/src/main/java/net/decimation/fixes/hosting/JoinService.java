package net.decimation.fixes.hosting;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The join side (docs/superpowers/specs/2026-10-18-player-hosting-part1-design.md 4.2): one 127.0.0.1 port per
 * listed world (Decimation's Community rows ping and join it like any server), each connection carried to the world's
 * relay room; a world whose mods this game lacks gets a port that refuses with a toast naming the mod.
 */
public final class JoinService
{
    public static final JoinService INSTANCE = new JoinService();
    /** The text of lang deciworldgen.host.full (plain Java here: tests run without Minecraft's language files). */
    static final String FULL = "The free relay is full until tomorrow 07:00 WIB";

    public interface Toasts
    {
        void show(String line);
    }

    /** Told before a connection is carried (the Community page's one time retry needs the world and the time). */
    public interface JoinHook
    {
        void joining(String name, int port);
    }

    private final List<ServerSocket> listeners = new CopyOnWriteArrayList<ServerSocket>();
    private final Map<String, Integer> ports = new ConcurrentHashMap<String, Integer>();
    private volatile Toasts toasts = new Toasts()
    {
        public void show(String line)
        {
        }
    };
    private volatile JoinHook hook;

    /** A listed world's direct addresses (the relay's /list/direct; tests give their own). */
    public interface DirectLookup
    {
        List<String> addresses(String room) throws IOException;
    }

    private volatile DirectLookup directLookup;

    public void setDirectLookup(DirectLookup l)
    {
        directLookup = l;
    }

    private final Map<String, String> routes = new ConcurrentHashMap<String, String>();
    /** Per room: the host's direct address that answered, or "" when none did (asked once per room). */
    private final Map<String, String> chosen = new ConcurrentHashMap<String, String>();
    static final int PROBE_MS = 1500;

    /** "direct" or "relay": how the last join of this world went; null before one. */
    public String routeOf(String name)
    {
        return routes.get(name);
    }

    private JoinService()
    {
    }

    public void setToasts(Toasts t)
    {
        toasts = t;
    }

    public void setJoinHook(JoinHook h)
    {
        hook = h;
    }

    public int openPorts()
    {
        return listeners.size();
    }

    /** The port opened for the world with this name, -1 when none. */
    public int portOf(String name)
    {
        Integer p = ports.get(name);
        return p == null ? -1 : p;
    }

    public int listen(final ListEntry e, ModSet mine) throws IOException
    {
        final String missing = mine.firstMissing(e.modSet());
        final ServerSocket ss = new ServerSocket(0, 8, InetAddress.getLoopbackAddress());
        listeners.add(ss);
        ports.put(e.name, ss.getLocalPort());
        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                while (!ss.isClosed())
                {
                    final Socket s;
                    try
                    {
                        s = ss.accept();
                    }
                    catch (IOException ex)
                    {
                        return; // closed
                    }
                    if (missing != null)
                    {
                        close(s);
                        toasts.show("You need " + missing + " to join " + e.name);
                        continue;
                    }
                    carry(e, s, ss.getLocalPort());
                }
            }
        }, "DeciRemnant join " + e.room);
        t.setDaemon(true);
        t.start();
        return ss.getLocalPort();
    }

    /**
     * A world that offers a direct join: read Minecraft's first bytes; a login (never a Community row's ping, so opening
     * the page contacts no listed address) asks for the host's addresses and plays straight to the first that answers.
     * Anything else, or no answer, goes through the relay with those bytes first.
     */
    private void route(final ListEntry e, final Socket s, final int port)
    {
        byte[] first;
        try
        {
            s.setSoTimeout(10000);
            byte[] b = new byte[4096];
            int n = s.getInputStream().read(b);
            s.setSoTimeout(0);
            if (n <= 0)
            {
                close(s);
                return;
            }
            first = java.util.Arrays.copyOf(b, n);
        }
        catch (IOException ex)
        {
            close(s);
            return;
        }
        if (LoginName.nextState(first) == 2)
        {
            String at = chosen.get(e.room);
            if (at == null)
            {
                at = "";
                try
                {
                    DirectLookup l = directLookup;
                    List<String> all = l != null ? l.addresses(e.room) : new RelayClient(RelayClient.base()).direct(e.room);
                    String picked = Direct.pick(all, e.room, PROBE_MS);
                    at = picked == null ? "" : picked;
                }
                catch (IOException ex)
                {
                    at = "";
                }
                chosen.put(e.room, at);
            }
            if (!at.isEmpty() && straight(e, s, port, at, first))
            {
                return;
            }
        }
        relay(e, s, port, first);
    }

    /** Plays straight to the host's gate at `at`; false when it cannot be reached now (the relay takes over). */
    private boolean straight(ListEntry e, Socket s, int port, String at, byte[] first)
    {
        Socket d = new Socket();
        try
        {
            d.connect(Direct.target(at), PROBE_MS);
            d.getOutputStream().write(first);
        }
        catch (IOException | IllegalArgumentException ex)
        {
            Direct.close(d);
            chosen.put(e.room, "");
            return false;
        }
        routes.put(e.name, "direct");
        JoinHook h = hook;
        if (h != null)
        {
            h.joining(e.name, port);
        }
        try
        {
            Direct.pump(s.getInputStream(), d.getOutputStream(), s, d, null, "DeciRemnant direct join up");
            Direct.pump(d.getInputStream(), s.getOutputStream(), s, d, null, "DeciRemnant direct join down");
        }
        catch (IOException ex)
        {
            Direct.close(s);
            Direct.close(d);
        }
        return true;
    }

    /** One connection to the room, on its own thread (the relay socket takes up to 10 s to open). */
    private void carry(final ListEntry e, final Socket s, final int port)
    {
        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                if ("direct".equals(e.route))
                {
                    route(e, s, port);
                    return;
                }
                relay(e, s, port, null);
            }
        }, "DeciRemnant join pipe");
        t.setDaemon(true);
        t.start();
    }

    /** Through the relay room, with `first` (bytes already read, or null) sent first; on the calling thread. */
    private void relay(final ListEntry e, final Socket s, final int port, byte[] first)
    {
        Pipe.First tell = new Pipe.First()
        {
            public void bytes(byte[] b)
            {
                JoinHook h = hook;
                if (h != null && LoginName.nextState(b) == 2)
                {
                    h.joining(e.name, port); // a real join, not a Community row's ping
                }
            }
        };
        if (first != null)
        {
            routes.put(e.name, "relay");
            tell.bytes(first);
        }
        try
        {
            Pipe.start(s, new RelayClient(RelayClient.base()).joinSocket(e.room), true, null, null, first != null ? null : tell, first);
        }
        catch (IOException ex)
        {
            close(s);
            // the relay refuses the upgrade: 429 / 1015 / 1027 the free daily limit, 409 full, 404 no host (spec 5)
            String m = String.valueOf(ex.getMessage());
            toasts.show(m.contains("429") || m.contains("1015") || m.contains("1027") ? FULL
                        : m.contains("409") ? "This world is full (" + e.max + "/" + e.max + ")" : "The host stopped hosting");
        }
    }

    private static void close(Socket s)
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

    public void closeAll()
    {
        for (ServerSocket s : listeners)
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
        listeners.clear();
        ports.clear();
        chosen.clear();
    }
}

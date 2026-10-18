package net.decimation.fixes.hosting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * One TCP connection carried over one relay WebSocket, both ways: on the host the LAN world's port, on the joiner
 * Minecraft's own connection. With waitReady (the joiner) nothing is read from the TCP side until the relay says
 * {"ready": true}, i.e. the host's data socket is there (relay/src/room.ts).
 */
public final class Pipe
{
    /** Looks at the first bytes from the relay before any of them reaches the TCP side (the host's login name check). */
    public interface Gate
    {
        int ALLOW = 0, DENY = 1, MORE = 2;

        int decide(byte[] soFar);
    }

    private final Socket tcp;
    private RelaySocket relay;
    private final CountDownLatch ready = new CountDownLatch(1);
    private final Runnable onClose;
    private volatile boolean closed;

    private Pipe(Socket tcp, Runnable onClose)
    {
        this.tcp = tcp;
        this.onClose = onClose;
    }

    public static Pipe start(Socket tcp, URI relayUri, boolean waitReady, Runnable onClose) throws IOException
    {
        return start(tcp, relayUri, waitReady, onClose, null);
    }

    /** Told the first bytes read from the TCP side (the join side tells a login from a Community row's ping). */
    public interface First
    {
        void bytes(byte[] first);
    }

    private First first;
    /** Bytes already read from the TCP side before the pipe started (the join side read them to choose a route). */
    private byte[] initial;


    public static Pipe start(Socket tcp, URI relayUri, boolean waitReady, Runnable onClose, final Gate gate) throws IOException
    {
        return start(tcp, relayUri, waitReady, onClose, gate, null);
    }

    public static Pipe start(Socket tcp, URI relayUri, boolean waitReady, Runnable onClose, final Gate gate, First first)
        throws IOException
    {
        return start(tcp, relayUri, waitReady, onClose, gate, first, null);
    }

    /** As above; `initial` (bytes already read from the TCP side, or null) goes to the relay first. */
    public static Pipe start(Socket tcp, URI relayUri, boolean waitReady, Runnable onClose, final Gate gate, First first,
                             byte[] initial) throws IOException
    {
        final Pipe p = new Pipe(tcp, onClose);
        p.first = first;
        p.initial = initial;
        final OutputStream out = tcp.getOutputStream();
        final java.io.ByteArrayOutputStream held = new java.io.ByteArrayOutputStream();
        final boolean[] open = {gate == null};
        RelaySocket relay = RelaySocket.open(relayUri, new RelaySocket.Listener()
        {
            public void binary(byte[] d)
            {
                try
                {
                    if (!open[0])
                    {
                        held.write(d, 0, d.length);
                        int verdict = held.size() > 4096 ? Gate.DENY : gate.decide(held.toByteArray());
                        if (verdict == Gate.MORE)
                        {
                            return;
                        }
                        if (verdict == Gate.DENY)
                        {
                            p.close();
                            return;
                        }
                        open[0] = true;
                        d = held.toByteArray();
                    }
                    out.write(d);
                    out.flush();
                }
                catch (IOException e)
                {
                    p.close();
                }
            }

            public void text(String s)
            {
                if (s.contains("\"ready\""))
                {
                    p.ready.countDown();
                }
            }

            public void closed(int code, String reason)
            {
                if (!p.closed)
                {
                    HostService.log("relay socket closed: " + code + " " + reason); // why a player left (docs/player_hosting.md 16)
                }
                p.close();
            }
        });
        p.relay = relay;
        if (p.closed && relay.isOpen())
        {
            relay.close("pipe closed"); // refused (gate) before the open returned: never leave it open (review)
            return p;
        }
        if (!waitReady)
        {
            p.ready.countDown();
        }
        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                p.copy();
            }
        }, "DeciRemnant pipe");
        t.setDaemon(true);
        t.start();
        return p;
    }

    private void copy()
    {
        try
        {
            if (!ready.await(15, TimeUnit.SECONDS))
            {
                close();
                return;
            }
            InputStream in = tcp.getInputStream();
            if (initial != null && initial.length > 0)
            {
                relay.send(initial, 0, initial.length);
            }
            byte[] b = new byte[16384];
            for (int n; (n = in.read(b)) > 0; )
            {
                if (first != null)
                {
                    First f = first;
                    first = null;
                    f.bytes(java.util.Arrays.copyOf(b, n));
                }
                relay.send(b, 0, n);
            }
        }
        catch (Exception e)
        {
            // the socket closed
        }
        close();
    }

    public boolean isClosed()
    {
        return closed;
    }

    public void close()
    {
        if (closed)
        {
            return;
        }
        closed = true;
        try
        {
            tcp.close();
        }
        catch (IOException e)
        {
            // already closed
        }
        if (relay != null && relay.isOpen())
        {
            relay.close("pipe closed");
        }
        if (onClose != null)
        {
            onClose.run();
        }
    }
}

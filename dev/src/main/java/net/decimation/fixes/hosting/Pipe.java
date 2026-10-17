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

    public static Pipe start(Socket tcp, URI relayUri, boolean waitReady, Runnable onClose, final Gate gate) throws IOException
    {
        final Pipe p = new Pipe(tcp, onClose);
        final OutputStream out = tcp.getOutputStream();
        final java.io.ByteArrayOutputStream held = new java.io.ByteArrayOutputStream();
        final boolean[] open = {gate == null};
        p.relay = RelaySocket.open(relayUri, new RelaySocket.Listener()
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
                p.close();
            }
        });
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
            byte[] b = new byte[16384];
            for (int n; (n = in.read(b)) > 0; )
            {
                relay.send(b, 0, n);
            }
        }
        catch (Exception e)
        {
            // the socket closed
        }
        close();
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

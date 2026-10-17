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
        final Pipe p = new Pipe(tcp, onClose);
        final OutputStream out = tcp.getOutputStream();
        p.relay = RelaySocket.open(relayUri, new RelaySocket.Listener()
        {
            public void binary(byte[] d)
            {
                try
                {
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

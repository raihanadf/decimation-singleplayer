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

    /** One connection to the room, on its own thread (the relay socket takes up to 10 s to open). */
    private void carry(final ListEntry e, final Socket s, final int port)
    {
        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    Pipe.start(s, new RelayClient(RelayClient.base()).joinSocket(e.room), true, null, null, new Pipe.First()
                    {
                        public void bytes(byte[] first)
                        {
                            JoinHook h = hook;
                            if (h != null && LoginName.nextState(first) == 2)
                            {
                                h.joining(e.name, port); // a real join, not a Community row's ping
                            }
                        }
                    });
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
        }, "DeciRemnant join pipe");
        t.setDaemon(true);
        t.start();
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
    }
}

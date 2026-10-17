package net.decimation.fixes.hosting;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * The host side (docs/superpowers/specs/2026-10-18-player-hosting-part1-design.md 4.1): registers the world on the
 * list, beats every 60 s (a "gone" answer registers again with the same settings), holds the control socket, opens one
 * data socket per joiner piped to the LAN port, stops on request or when the world closes. Network calls run on one
 * background thread in the game (the menu never waits on the relay); tests run them in line.
 */
public final class HostService
{
    public static final HostService INSTANCE = new HostService();
    private static final long BEAT_MS = 60_000;

    public enum State { OFF, STARTING, ONLINE, FULL_TODAY, FAILED }

    public static final class Settings
    {
        public String name, player, version;
        public boolean online;
        public int max = 4;
    }

    public interface Players
    {
        int count();
    }

    private final ExecutorService worker = Executors.newSingleThreadExecutor(new ThreadFactory()
    {
        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(r, "DeciRemnant hosting");
            t.setDaemon(true);
            return t;
        }
    });
    private volatile State state = State.OFF;
    private volatile String error = "";
    private volatile Settings settings;
    private Players players;
    private int lanPort;
    private String room, secret;
    private long lastBeat;
    private RelaySocket control;
    private boolean sockets = true, inLine;
    private final Map<String, Pipe> pipes = new ConcurrentHashMap<String, Pipe>();

    private HostService()
    {
    }

    /** Tests: no control socket (a fake relay has no WebSocket) and network calls in line. */
    void socketsForTests(boolean on)
    {
        sockets = on;
        inLine = !on;
    }

    public State state()
    {
        return state;
    }

    public String error()
    {
        return error;
    }

    public int joiners()
    {
        return pipes.size();
    }

    public Settings settings()
    {
        return settings;
    }

    private void run(Runnable r)
    {
        if (inLine)
        {
            r.run();
        }
        else
        {
            worker.execute(r);
        }
    }

    public void start(Settings s, int lanPort, Players players)
    {
        synchronized (this)
        {
            this.settings = s;
            this.players = players;
            this.lanPort = lanPort;
            state = State.STARTING;
            error = "";
        }
        run(new Runnable()
        {
            public void run()
            {
                register();
            }
        });
    }

    private synchronized void register()
    {
        if (state == State.OFF)
        {
            return; // stopped while waiting
        }
        RelayClient c = new RelayClient(RelayClient.base());
        try
        {
            String[] rs = c.host(settings.name, settings.player, settings.online, settings.max, ModSet.current0(), settings.version);
            room = rs[0];
            secret = rs[1];
            lastBeat = System.currentTimeMillis();
            if (sockets)
            {
                openControl(c);
            }
            state = State.ONLINE;
        }
        catch (RelayClient.RelayFull e)
        {
            state = State.FULL_TODAY;
            error = e.getMessage();
        }
        catch (IOException e)
        {
            state = State.FAILED;
            error = e.getMessage();
        }
    }

    private void openControl(final RelayClient c) throws IOException
    {
        control = RelaySocket.open(c.hostSocket(room, secret), new RelaySocket.Listener()
        {
            public void binary(byte[] d)
            {
            }

            public void text(String s)
            {
                com.google.gson.JsonObject o = new com.google.gson.Gson().fromJson(s, com.google.gson.JsonObject.class);
                if (o.has("open"))
                {
                    final String id = o.get("open").getAsString();
                    run(new Runnable()
                    {
                        public void run()
                        {
                            openData(c, id);
                        }
                    });
                }
                else if (o.has("close"))
                {
                    Pipe p = pipes.remove(o.get("close").getAsString());
                    if (p != null)
                    {
                        p.close();
                    }
                }
            }

            public void closed(int code, String reason)
            {
                if (state == State.ONLINE)
                {
                    state = State.FAILED;
                    error = "relay closed: " + code + " " + reason;
                }
            }
        });
    }

    private void openData(RelayClient c, final String id)
    {
        try
        {
            Socket lan = new Socket(InetAddress.getLoopbackAddress(), lanPort);
            pipes.put(id, Pipe.start(lan, c.dataSocket(room, secret, id), false, new Runnable()
            {
                public void run()
                {
                    pipes.remove(id);
                }
            }));
        }
        catch (IOException e)
        {
            error = "joiner " + id + ": " + e.getMessage();
        }
    }

    /** The heartbeat clock; call it from the client tick. */
    public void tick(long now)
    {
        synchronized (this)
        {
            if (state != State.ONLINE || now - lastBeat < BEAT_MS)
            {
                return;
            }
            lastBeat = now;
        }
        run(new Runnable()
        {
            public void run()
            {
                beat();
            }
        });
    }

    private synchronized void beat()
    {
        if (state != State.ONLINE)
        {
            return;
        }
        try
        {
            if (!new RelayClient(RelayClient.base()).beat(room, secret, players.count()))
            {
                closeSockets();
                register();
            }
        }
        catch (RelayClient.RelayFull e)
        {
            state = State.FULL_TODAY;
            error = e.getMessage();
        }
        catch (IOException e)
        {
            error = e.getMessage(); // a missed beat: the next one tries again; after 3 min the list answers gone
        }
    }

    /** Stops hosting: the list entry goes at once, every socket closes. */
    public void stop()
    {
        final String r, s;
        synchronized (this)
        {
            if (state == State.OFF)
            {
                return;
            }
            r = room;
            s = secret;
            closeSockets();
            room = null;
            secret = null;
            state = State.OFF;
        }
        if (r != null)
        {
            run(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        new RelayClient(RelayClient.base()).stop(r, s);
                    }
                    catch (IOException e)
                    {
                        // the entry goes after 3 min anyway
                    }
                }
            });
        }
    }

    private void closeSockets()
    {
        for (Pipe p : pipes.values())
        {
            p.close();
        }
        pipes.clear();
        if (control != null && control.isOpen())
        {
            control.close("host stopped");
        }
        control = null;
    }

    /** True when `name` is already playing here, in any letter case (an offline world trusts names). */
    static boolean nameTaken(String name, java.util.List<String> playing)
    {
        for (String p : playing)
        {
            if (p.equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * An "Any account" (offline) world takes any name: a stranger could join as the host or as another player. A login
     * under a name already playing is refused (security review, 18 Oktober 2026).
     */
    @SubscribeEvent
    public void onLogin(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent e)
    {
        Settings s = settings;
        if (state == State.OFF || s == null || s.online || !(e.player instanceof net.minecraft.entity.player.EntityPlayerMP))
        {
            return;
        }
        net.minecraft.entity.player.EntityPlayerMP joined = (net.minecraft.entity.player.EntityPlayerMP) e.player;
        java.util.List<String> others = new java.util.ArrayList<String>();
        for (Object o : joined.mcServer.getConfigurationManager().playerEntityList)
        {
            if (o != joined)
            {
                others.add(((net.minecraft.entity.player.EntityPlayer) o).getCommandSenderName());
            }
        }
        if (nameTaken(joined.getCommandSenderName(), others))
        {
            joined.playerNetServerHandler.kickPlayerFromServer("That name is already playing here");
        }
    }

    /** The world closed (leaving it, quitting): stop hosting at once. */
    public void worldClosed()
    {
        stop();
    }

    @SubscribeEvent
    public void onUnload(net.minecraftforge.event.world.WorldEvent.Unload e)
    {
        if (!e.world.isRemote && e.world.provider.dimensionId == 0)
        {
            worldClosed();
        }
    }
}

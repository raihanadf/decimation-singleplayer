package net.decimation.fixes.hosting;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The host side (docs/superpowers/specs/2026-10-18-player-hosting-part1-design.md 4.1): registers the world on the
 * list, beats every 60 s (a "gone" answer or a lost control socket registers it again with the same settings), holds
 * the control socket, opens one data socket per joiner piped to the LAN port, stops on request or when the world closes.
 * No network call ever runs under the lock: list calls run on one background thread, each joiner's data socket on its
 * own, and every start or stop bumps a generation so a late answer never overwrites newer state (final review,
 * 18 Oktober 2026). Tests run the list calls in line.
 */
public final class HostService
{
    public static final HostService INSTANCE = new HostService();
    private static final long BEAT_MS = 60_000, LOGIN_MS = 30_000;

    public enum State { OFF, STARTING, ONLINE, FULL_TODAY, FAILED }

    public static final class Settings
    {
        public String name, player, version;
        public boolean online = true; // "Mojang accounts only" unless the host picks otherwise (user, 18 Oktober 2026)
        public int max = 4;
        public boolean commands = true; // "Commands: only me" (user, 18 Oktober 2026)
    }

    public interface Players
    {
        int count();
    }

    /** Told once per change into FULL_TODAY or FAILED (the game shows a toast, whatever screen is open). */
    public interface StateListener
    {
        void changed(State state, String error);
    }

    /** The names playing in the hosted world (the integrated server's list; tests set their own). */
    interface Playing
    {
        List<String> names();
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
    private volatile Players players;
    private volatile int lanPort;
    private volatile String room, secret;
    private volatile RelaySocket control;
    private volatile int gen;
    private volatile StateListener listener;
    private final AtomicLong lastBeat = new AtomicLong();
    private boolean sockets = true, inLine;
    private final Map<String, Pipe> pipes = new ConcurrentHashMap<String, Pipe>();
    private final Map<String, Long> loggingIn = new HashMap<String, Long>(); // lower case name -> time; its own lock
    private volatile Playing playing = new Playing()
    {
        public List<String> names()
        {
            List<String> out = new ArrayList<String>();
            net.minecraft.server.MinecraftServer srv = net.minecraft.server.MinecraftServer.getServer();
            if (srv != null && srv.getConfigurationManager() != null)
            {
                for (Object o : new ArrayList<Object>(srv.getConfigurationManager().playerEntityList))
                {
                    out.add(((net.minecraft.entity.player.EntityPlayer) o).getCommandSenderName());
                }
            }
            return out;
        }
    };

    private HostService()
    {
    }

    /** Tests: no control socket (a fake relay has no WebSocket) and list calls in line. */
    void socketsForTests(boolean on)
    {
        sockets = on;
        inLine = !on;
    }

    /** Tests: list calls on the background thread as in the game. */
    void asyncForTests()
    {
        inLine = false;
    }

    /** Tests: as before the first start. */
    void clearForTests()
    {
        settings = null;
    }

    void playingForTests(Playing p)
    {
        playing = p;
    }

    public void setStateListener(StateListener l)
    {
        listener = l;
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

    /** Sets the state of generation g (ignored when a newer start or stop came in between). */
    private void set(int g, State s, String err)
    {
        StateListener l;
        synchronized (this)
        {
            if (g != gen)
            {
                return;
            }
            boolean changed = s != state;
            state = s;
            error = err;
            l = changed && (s == State.FULL_TODAY || s == State.FAILED) ? listener : null;
        }
        if (l != null)
        {
            l.changed(s, err);
        }
    }

    public void start(Settings s, int lanPort, Players players)
    {
        final String oldRoom, oldSecret;
        final int g;
        synchronized (this)
        {
            g = ++gen;
            oldRoom = room;
            oldSecret = secret;
            closeSockets();
            room = null;
            secret = null;
            settings = s;
            this.players = players;
            this.lanPort = lanPort;
            state = State.STARTING;
            error = "";
        }
        synchronized (loggingIn)
        {
            loggingIn.clear();
        }
        if (oldRoom != null)
        {
            run(stopCall(oldRoom, oldSecret)); // a second start: the first room goes off the list
        }
        run(new Runnable()
        {
            public void run()
            {
                register(g);
            }
        });
    }

    /** Registers the world (network, never under the lock), then opens the control socket. */
    private void register(int g)
    {
        Settings st = settings;
        RelayClient c = new RelayClient(RelayClient.base());
        String[] rs;
        try
        {
            rs = c.host(st.name, st.player, st.online, st.max, ModSet.current0(), st.version);
        }
        catch (RelayClient.RelayFull e)
        {
            set(g, State.FULL_TODAY, e.getMessage());
            return;
        }
        catch (IOException e)
        {
            set(g, State.FAILED, e.getMessage());
            return;
        }
        synchronized (this)
        {
            if (g != gen)
            {
                run(stopCall(rs[0], rs[1])); // stopped while registering: take the new room off again
                return;
            }
            room = rs[0];
            secret = rs[1];
            lastBeat.set(System.currentTimeMillis());
        }
        if (sockets)
        {
            try
            {
                RelaySocket ctl = openControl(c, rs[0], rs[1], g);
                synchronized (this)
                {
                    if (g != gen)
                    {
                        ctl.close("stopped");
                        return;
                    }
                    control = ctl;
                }
            }
            catch (IOException e)
            {
                set(g, State.FAILED, e.getMessage());
                return;
            }
        }
        set(g, State.ONLINE, "");
    }

    private RelaySocket openControl(final RelayClient c, final String r, final String s, final int g) throws IOException
    {
        return RelaySocket.open(c.hostSocket(r, s), new RelaySocket.Listener()
        {
            public void binary(byte[] d)
            {
            }

            public void text(String text)
            {
                com.google.gson.JsonObject o = new com.google.gson.Gson().fromJson(text, com.google.gson.JsonObject.class);
                if (o.has("open"))
                {
                    openData(c, r, s, o.get("open").getAsString());
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
                if (g == gen && state == State.ONLINE)
                {
                    controlLost(); // the relay dropped us (network, sleep, a relay deploy): list the world again
                }
            }
        });
    }

    /** One joiner: a data socket to the room piped to the LAN port, on its own thread (opening takes up to 10 s). */
    private void openData(final RelayClient c, final String r, final String s, final String id)
    {
        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                final String[] admitted = new String[1];
                try
                {
                    Socket lan = new Socket(InetAddress.getLoopbackAddress(), lanPort);
                    Pipe p = Pipe.start(lan, c.dataSocket(r, s, id), false, new Runnable()
                    {
                        public void run()
                        {
                            pipes.remove(id);
                            release(admitted[0]); // a dropped join may try again at once (review)
                        }
                    }, new Pipe.Gate()
                    {
                        public int decide(byte[] soFar)
                        {
                            int verdict = admit(soFar);
                            if (verdict == ALLOW)
                            {
                                String name = LoginName.of(soFar);
                                admitted[0] = name == null || name.equals(LoginName.STATUS) ? null : name;
                            }
                            return verdict;
                        }
                    });
                    if (!p.isClosed())
                    {
                        pipes.put(id, p);
                    }
                }
                catch (IOException e)
                {
                    error = "joiner " + id + ": " + e.getMessage();
                }
            }
        }, "DeciRemnant host pipe");
        t.setDaemon(true);
        t.start();
    }

    /** The heartbeat clock; called from the client tick: never takes the lock, never waits. */
    public void tick(long now)
    {
        if (state != State.ONLINE)
        {
            return;
        }
        long last = lastBeat.get();
        if (now - last < BEAT_MS || !lastBeat.compareAndSet(last, now))
        {
            return;
        }
        final int g = gen;
        run(new Runnable()
        {
            public void run()
            {
                beat(g);
            }
        });
    }

    private void beat(int g)
    {
        String r, s;
        synchronized (this)
        {
            if (g != gen || state != State.ONLINE)
            {
                return;
            }
            r = room;
            s = secret;
        }
        try
        {
            if (!new RelayClient(RelayClient.base()).beat(r, s, players.count()))
            {
                registerAgain(g);
            }
        }
        catch (RelayClient.RelayFull e)
        {
            set(g, State.FULL_TODAY, e.getMessage());
        }
        catch (IOException e)
        {
            error = e.getMessage(); // a missed beat (also a passing 5xx): the next one tries again
        }
    }

    /** The list forgot the world, or the relay dropped the control socket: a new room with the same settings. */
    private void registerAgain(int g)
    {
        final int ng;
        synchronized (this)
        {
            if (g != gen)
            {
                return;
            }
            ng = ++gen;
            closeSockets();
            room = null;
            secret = null;
            state = State.STARTING;
        }
        register(ng);
    }

    /** The control socket closed while online (also called by tests). */
    void controlLost()
    {
        final int g = gen;
        run(new Runnable()
        {
            public void run()
            {
                registerAgain(g);
            }
        });
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
            gen++;
            r = room;
            s = secret;
            closeSockets();
            room = null;
            secret = null;
            state = State.OFF;
        }
        if (r != null)
        {
            run(stopCall(r, s));
        }
    }

    private Runnable stopCall(final String r, final String s)
    {
        return new Runnable()
        {
            public void run()
            {
                try
                {
                    new RelayClient(RelayClient.base()).stop(r, s);
                }
                catch (IOException e)
                {
                    // the relay drops the room when its control socket closes, and the list after 3 min anyway
                }
            }
        };
    }

    private void closeSockets()
    {
        for (Pipe p : pipes.values())
        {
            p.close();
        }
        pipes.clear();
        RelaySocket c = control;
        control = null;
        if (c != null && c.isOpen())
        {
            c.close("host stopped");
        }
    }

    /** True when `name` is already playing here, in any letter case (an offline world trusts names). */
    static boolean nameTaken(String name, List<String> playing)
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
     * The host's gate on each joiner's first bytes: an "Any account" (offline) world takes any name, so a login under a
     * name already playing or logging in (the host's too, in any letter case) is refused before it reaches the world;
     * Minecraft would otherwise kick the player who has that name. Unreadable bytes are refused (fail closed).
     */
    int admit(byte[] soFar)
    {
        String name = LoginName.of(soFar);
        if (name == null)
        {
            return Pipe.Gate.MORE;
        }
        if (name.equals(LoginName.BAD))
        {
            return Pipe.Gate.DENY;
        }
        Settings s = settings;
        if (s == null || state == State.OFF)
        {
            return Pipe.Gate.DENY; // nothing hosted: fail closed
        }
        if (name.equals(LoginName.STATUS) || s.online)
        {
            return Pipe.Gate.ALLOW; // a status ping, or Mojang checks the account
        }
        List<String> taken = new ArrayList<String>(playing.names());
        synchronized (loggingIn)
        {
            long now = System.currentTimeMillis();
            for (Iterator<Map.Entry<String, Long>> it = loggingIn.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry<String, Long> e = it.next();
                if (now - e.getValue() > LOGIN_MS)
                {
                    it.remove();
                }
                else
                {
                    taken.add(e.getKey());
                }
            }
            if (nameTaken(name, taken))
            {
                return Pipe.Gate.DENY;
            }
            loggingIn.put(name.toLowerCase(java.util.Locale.ROOT), now);
            return Pipe.Gate.ALLOW;
        }
    }

    /** Frees a name reserved by admit (its pipe closed). */
    void release(String name)
    {
        if (name != null)
        {
            synchronized (loggingIn)
            {
                loggingIn.remove(name.toLowerCase(java.util.Locale.ROOT));
            }
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

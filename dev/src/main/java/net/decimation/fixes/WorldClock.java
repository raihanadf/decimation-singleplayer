package net.decimation.fixes;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

/**
 * The world's day and its daily events (docs/systems_plan.md 2.1), for every system that runs on
 * days: zombie growth and unlocks, horde nights, raids, claim decay and revenue, infection stages.
 * Systems subscribe instead of keeping their own timers.
 *
 * Day = overworld time / 24000 + 1 (day 1 starts at world creation). Nothing is stored: the day comes
 * from the world time, which the world saves. Events fire when the time crosses their point: DAY at
 * 0 (the morning a new day number starts), DUSK at 12000 (sunset), NIGHT at 13800 (dark enough for
 * monsters), DAWN at 22200 (sky lightens). A jump forward (sleeping, /time add) fires each point it
 * crosses once; a jump backward (/time set) fires nothing.
 */
public class WorldClock
{
    public enum Event
    {
        DAY(0), DUSK(12000), NIGHT(13800), DAWN(22200);

        public final int time;

        Event(int time)
        {
            this.time = time;
        }
    }

    public interface Listener
    {
        /** On the server thread; `day` is the day the event belongs to. */
        void onClock(World world, Event event, int day);
    }

    private static final List<Listener> listeners = new ArrayList<Listener>();
    private static long last = -1;

    public static void listen(Listener l)
    {
        synchronized (listeners)
        {
            listeners.add(l);
        }
    }

    public static void unlisten(Listener l)
    {
        synchronized (listeners)
        {
            listeners.remove(l);
        }
    }

    /** Day number of a world, 1 on the first day. */
    public static int day(World world)
    {
        return (int) (world.getWorldTime() / 24000L) + 1;
    }

    /** Day number of the overworld, 0 without a server. */
    public static int day()
    {
        MinecraftServer s = MinecraftServer.getServer();
        return s == null || s.worldServers == null || s.worldServers.length == 0 ? 0 : day(s.worldServers[0]);
    }

    /** True between DUSK and DAWN of the overworld time. */
    public static boolean isNight(World world)
    {
        long t = world.getWorldTime() % 24000L;
        return t >= Event.DUSK.time && t < Event.DAWN.time;
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent e)
    {
        if (e.phase != TickEvent.Phase.END)
        {
            return;
        }
        MinecraftServer s = MinecraftServer.getServer();
        if (s == null || s.worldServers == null || s.worldServers.length == 0)
        {
            return;
        }
        World w = s.worldServers[0];
        long now = w.getWorldTime();
        if (last < 0 || now < last)
        {
            last = now; // first tick of a world, or time set back: no events
            return;
        }
        fire(w, last, now);
        last = now;
    }

    /** Fires every event point in (from, to] in time order, each event type at most once. */
    public static void fire(World w, long from, long to)
    {
        List<long[]> due = new ArrayList<long[]>();
        for (Event ev : Event.values())
        {
            // the latest time <= to that is this event's point, if it lies after `from`
            long p = (to - ev.time) / 24000L * 24000L + ev.time;
            if (to >= ev.time && p > from)
            {
                due.add(new long[] {p, ev.ordinal()});
            }
        }
        java.util.Collections.sort(due, new java.util.Comparator<long[]>()
        {
            public int compare(long[] a, long[] b)
            {
                return Long.compare(a[0], b[0]);
            }
        });
        List<Listener> copy;
        synchronized (listeners)
        {
            copy = new ArrayList<Listener>(listeners);
        }
        for (long[] d : due)
        {
            for (Listener l : copy)
            {
                l.onClock(w, Event.values()[(int) d[1]], (int) (d[0] / 24000L) + 1);
            }
        }
    }

    @SubscribeEvent
    public void unload(net.minecraftforge.event.world.WorldEvent.Unload e)
    {
        if (!e.world.isRemote && e.world.provider.dimensionId == 0)
        {
            reset();
        }
    }

    /** Forget the last seen time (world unload; dev tests). */
    public static void reset()
    {
        last = -1;
    }
}

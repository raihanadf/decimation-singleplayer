package net.decimation.worldgen.devtest;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

/**
 * Dev diagnostic for bug.md "Entity is already tracked" (-Ptrackdebug, system property deciworldgen.trackdebug; inert
 * without it). Vanilla's EntityTracker refuses an entity whose id it tracks already and logs only a stack trace, not the
 * entity. This remembers where every entity joined a server world (a stack per entity: only for a test run) and, when an
 * id joins a second time, logs both: class, id, position, chunk, whether it is the same object and whether the world
 * already lists it, with both stacks. Entity ids are never reused in one session, so a second join of an id is the bug.
 */
public class TrackDebug
{
    public static final String PROPERTY = "deciworldgen.trackdebug";

    private static final class Join
    {
        final WeakReference<Entity> entity;
        final String what;
        final Throwable where;

        Join(Entity e, Throwable where)
        {
            this.entity = new WeakReference<Entity>(e);
            this.what = describe(e);
            this.where = where;
        }
    }

    private final Map<Integer, Join> joins = new HashMap<Integer, Join>();
    private int duplicates;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onJoin(EntityJoinWorldEvent e)
    {
        if (e.isCanceled() || !(e.world instanceof WorldServer))
        {
            return;
        }
        Throwable here = new Throwable("joined here");
        Join first = joins.get(e.entity.getEntityId());
        if (first == null)
        {
            joins.put(e.entity.getEntityId(), new Join(e.entity, here));
            return;
        }
        duplicates++;
        Entity before = first.entity.get();
        FMLLog.warning("TRACKDEBUG duplicate join %d: id %d, now %s; first %s; same object %s; already in the world's list %s",
                       duplicates, e.entity.getEntityId(), describe(e.entity), first.what, before == e.entity,
                       e.world.loadedEntityList.contains(e.entity));
        if (duplicates <= 5)
        {
            FMLLog.warning("TRACKDEBUG first join of id %d:%s", e.entity.getEntityId(), stack(first.where));
            FMLLog.warning("TRACKDEBUG second join of id %d:%s", e.entity.getEntityId(), stack(here));
        }
    }

    static String describe(Entity e)
    {
        return String.format("%s at %.1f %.1f %.1f (chunk %d %d, addedToChunk %s, chunk coords %d %d)",
                             e.getClass().getName(), e.posX, e.posY, e.posZ, (int) Math.floor(e.posX / 16.0),
                             (int) Math.floor(e.posZ / 16.0), e.addedToChunk, e.chunkCoordX, e.chunkCoordZ);
    }

    private static String stack(Throwable t)
    {
        StringBuilder s = new StringBuilder();
        StackTraceElement[] lines = t.getStackTrace();
        for (int i = 1; i < Math.min(lines.length, 28); i++)
        {
            s.append("\n    at ").append(lines[i]);
        }
        return s.toString();
    }
}

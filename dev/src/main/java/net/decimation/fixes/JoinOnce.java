package net.decimation.fixes;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

/**
 * An entity joins a server world once (bug.md "Entity is already tracked"). Decimation's join handler (PlayerJoinSync,
 * deci.an.e) swaps 1 in 10 joining infected for a bloater or a hulk, 5 in 100 soldiers for a mech and infected in the
 * rain for a frail by spawning the new mob at once. During a chunk load that happens inside World.addLoadedEntities,
 * which walks the chunk's live entity list: it reaches the new mob there and adds it again. The world then lists it
 * twice and updates it twice a tick (double speed until it dies or its chunk unloads), and vanilla's tracker logs
 * "Entity is already tracked!". This cancels the join of an entity the world already holds (WorldServer's id map has
 * this very object), first of all handlers so Decimation never rolls a swap for it again. One map lookup per join.
 */
public class JoinOnce
{
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onJoin(EntityJoinWorldEvent e)
    {
        if (!e.world.isRemote && e.world.getEntityByID(e.entity.getEntityId()) == e.entity)
        {
            e.setCanceled(true);
        }
    }
}

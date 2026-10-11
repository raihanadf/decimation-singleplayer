package net.decimation.fixes;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.decimation.worldgen.ZoneKind;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

/**
 * Singleplayer fix: zones never change what spawns.
 *
 * Decimation's spawn handler (obfuscated {@code deci.aK.d}) turns a quarter
 * of the infected spawning inside a MILITARY or POLICE zone into the
 * military / police variant, and cancels infected spawns inside a SAFEZONE.
 * Its event method is {@code @SideOnly(Side.SERVER)}, so FML strips it in
 * singleplayer, the same ServerProxy disease as the bottlecap fix. This
 * replicates the infected part 1:1 using the mod's own zone checks
 * ({@code net.decimation.mod.server.zones.b}).
 *
 * Since v0.46.0 also the rest of that handler (landmines, campfires, punji
 * sticks, barbed wire, crossbow bolts, rockets, grenades and mortar rounds
 * within 10 blocks of a safezone are cancelled) and, ours on both sides,
 * no bandit, Soviet or mech spawns inside a safezone (docs/zones.md).
 *
 * On a dedicated server the original handler IS registered, so the copied
 * parts step aside.
 */
public class ZoneSpawnHandler
{
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        if (event.world.isRemote)
        {
            return;
        }
        net.minecraft.entity.Entity e = event.entity;
        // ours, on both sides: no hostile NPC spawns inside a safezone (Decimation only stops infected)
        if ((Deci.npcKind(e) == NpcKind.BANDIT || Deci.npcKind(e) == NpcKind.SOVIET || Deci.isMech(e))
            && Deci.isEntityInZone(e, ZoneKind.SAFEZONE))
        {
            event.setCanceled(true);
            return;
        }
        if (FMLCommonHandler.instance().getSide().isServer())
        {
            return; // dedicated server: original deci.aK.d handles the rest
        }
        // traps and explosives within 10 blocks of a safezone (EntitySpawnZoneHandler)
        if ((Deci.isTrap(e) || Deci.isSafezoneProjectile(e)) && Deci.isEntityNearZone(e, ZoneKind.SAFEZONE))
        {
            event.setCanceled(true);
            return;
        }
        if (!Deci.isInfected(e))
        {
            return;
        }
        net.minecraft.entity.Entity infected = e;
        double roll = Math.random();
        if (Deci.isInfectedInZone(infected, ZoneKind.MILITARY))
        {
            if (roll < 0.25)
            {
                Deci.setInfectedVariant(infected, "MILITARY");
            }
        }
        else if (Deci.isInfectedInZone(infected, ZoneKind.POLICE))
        {
            if (roll < 0.25)
            {
                Deci.setInfectedVariant(infected, "POLICE");
            }
        }
        else if (Deci.isInfectedInZone(infected, ZoneKind.SAFEZONE))
        {
            event.setCanceled(true);
        }
    }
}

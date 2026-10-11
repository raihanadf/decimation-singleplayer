package net.decimation.fixes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.decimation.mod.server.zones.ObjectZone;
import net.decimation.mod.server.zones.ObjectZoneList;
import net.decimation.worldgen.ZoneKind;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

/**
 * Decimation's zones working in singleplayer, and safezones that keep everyone friendly safe
 * (docs/zones.md section 5).
 *
 * Decimation's zone rules live in server proxy handlers (PlayerZoneTickHandler deci.aK.m,
 * SafezoneAttackHandler deci.aK.g), their methods @SideOnly(SERVER): stripped in singleplayer.
 * The copies here step aside on a dedicated server. Ours on top, on both sides:
 * - inside a safezone nobody friendly can be hurt (players, traders and guards, civilians,
 *   friendly soldiers, owned pets); hostiles stay attackable so the guards can defend;
 * - a player who attacks a protected one there is provoked for PROVOKED_TICKS after the last
 *   offence: no damage done, but their combat timer keeps running (Decimation's own rule then
 *   takes their safezone protection away) and the guards and soldiers around take them as target;
 * - conditions (infection, bleeding, broken leg) cannot start inside a safezone, which
 *   Decimation's setters only check on a dedicated server;
 * - every player gets the zones near them as PacketZoneLists (Decimation only ever sent the
 *   zones of its json file): the safezone chat line, its sound and the map markers.
 */
public class ZoneRules
{
    /** Provocation lasts this long after the last offence. */
    static final int PROVOKED_TICKS = 1200;
    /** Guards and soldiers within this many blocks turn on a provoked player. */
    static final double GUARD_RANGE = 64;
    /** Zones sent to a player: boxes within this many blocks (x / z). */
    static final int SYNC_RANGE = 1024;
    static final int SYNC_EVERY = 100;

    /** World time until which a player is provoked. */
    private final Map<UUID, Long> provoked = new HashMap<UUID, Long>();
    /** Conditions of a player at the last tick inside a safezone: infected, bleeding, broken leg. */
    private final Map<UUID, boolean[]> conditions = new HashMap<UUID, boolean[]>();
    /** Zone list text last sent to a player. */
    private final Map<UUID, String> sent = new HashMap<UUID, String>();
    private int tick;

    private static boolean dedicated()
    {
        return FMLCommonHandler.instance().getSide().isServer();
    }

    /** Safe inside a safezone: players (by Decimation's check: not while fighting), friendly NPCs, pets. */
    static boolean protectedHere(Entity e)
    {
        if (e instanceof EntityPlayer)
        {
            return Deci.isPlayerInZone((EntityPlayer) e, ZoneKind.SAFEZONE);
        }
        return friendly(e) && Deci.isEntityInZone(e, ZoneKind.SAFEZONE);
    }

    /** Friendly to a player who behaves: traders and guards, civilians, soldiers, owned pets. */
    static boolean friendly(Entity e)
    {
        NpcKind k = Deci.npcKind(e);
        return Deci.isTrader(e) || Deci.isCivilian(e) || k == NpcKind.SOLDIER || k == NpcKind.HAZMAT
            || e instanceof EntityTameable && ((EntityTameable) e).isTamed();
    }

    public boolean isProvoked(EntityPlayer p)
    {
        Long until = provoked.get(p.getUniqueID());
        return until != null && until >= p.worldObj.getTotalWorldTime();
    }

    /** Ends a provocation now (dev tests). */
    public void forgive(EntityPlayer p)
    {
        provoked.remove(p.getUniqueID());
        Deci.Player d = Deci.player(p);
        if (d != null)
        {
            d.setCombatTimer(0);
        }
    }

    // ------------------------------------------------------------ attacks

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onLivingAttack(LivingAttackEvent event)
    {
        EntityLivingBase target = event.entityLiving;
        if (target.worldObj.isRemote)
        {
            return;
        }
        Entity src = event.source.getEntity();
        if (!dedicated() && target instanceof EntityPlayer && !event.isCanceled())
        {
            // SafezoneAttackHandler: PvP from or into a safezone, the grace timer, infected
            EntityPlayer player = (EntityPlayer) target;
            if (src instanceof EntityPlayer)
            {
                Deci.Player a = Deci.player((EntityPlayer) src);
                if (Deci.safezoneGraceEnabled() && a != null && a.safezoneGrace() > 0)
                {
                    event.setCanceled(true);
                    ((EntityPlayer) src).addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED
                        + "Can't attack player! They've left a safezone too recently!"));
                    return;
                }
            }
            Deci.Player d = Deci.player(player);
            if (Deci.safezoneGraceEnabled() && d != null && d.safezoneGrace() > 0 && !isProvoked(player))
            {
                event.setCanceled(true);
                return;
            }
        }
        if (!(protectedHere(target) || Deci.isTrader(target) && Deci.isEntityNearZone(target, ZoneKind.SAFEZONE)))
        {
            return;
        }
        event.setCanceled(true);
        if (src instanceof EntityPlayer && src != target && !((EntityPlayer) src).capabilities.isCreativeMode)
        {
            provoke((EntityPlayer) src);
        }
    }

    /** A player attacked someone protected in a safezone: the guards turn on them. */
    void provoke(EntityPlayer p)
    {
        if (!isProvoked(p))
        {
            p.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED
                + "You attacked someone in a safezone! The guards open fire."));
            cpw.mods.fml.common.FMLLog.info("[deciworldgen] safezone provoked by %s at %d,%d,%d", p.getCommandSenderName(),
                                            (int) p.posX, (int) p.posY, (int) p.posZ);
        }
        provoked.put(p.getUniqueID(), p.worldObj.getTotalWorldTime() + PROVOKED_TICKS);
        hunt(p);
    }

    /** Keeps the combat timer running and points the guards and soldiers around at the player. */
    private void hunt(EntityPlayer p)
    {
        Deci.Player d = Deci.player(p);
        if (d != null && d.combatTimer() < 40)
        {
            d.setCombatTimer(40);
        }
        List<?> near = p.worldObj.getEntitiesWithinAABBExcludingEntity(p, p.boundingBox.expand(GUARD_RANGE, 24, GUARD_RANGE));
        for (Object o : near)
        {
            Entity e = (Entity) o;
            NpcKind k = Deci.npcKind(e);
            if (!(Deci.isArmedTrader(e) || k == NpcKind.SOLDIER || k == NpcKind.HAZMAT) || e.getDistanceToEntity(p) > GUARD_RANGE)
            {
                continue;
            }
            ((EntityLivingBase) e).setRevengeTarget(p);
            if (e instanceof EntityCreature)
            {
                ((EntityCreature) e).setTarget(p);
            }
        }
    }

    // ------------------------------------------------------------ player tick

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        EntityPlayer p = event.player;
        if (event.phase != TickEvent.Phase.START || p.worldObj.isRemote)
        {
            return;
        }
        Deci.Player d = Deci.player(p);
        if (d == null)
        {
            return;
        }
        boolean safe = Deci.isPlayerInZone(p, ZoneKind.SAFEZONE);
        if (!dedicated())
        {
            // PlayerZoneTickHandler
            if (safe)
            {
                d.setWater(20);
            }
            if (Deci.worldBorderEnabled() && !Deci.isPlayerInZone(p, ZoneKind.BORDER))
            {
                p.attackEntityFrom(DamageSource.generic, 4.0f);
            }
            if (p.capabilities.isCreativeMode)
            {
                if (!d.creativeFlag())
                {
                    d.setCreativeFlag(true);
                    d.healFully();
                }
            }
            else if (d.creativeFlag())
            {
                d.setCreativeFlag(false);
            }
            boolean scary = Deci.isPlayerInZone(p, ZoneKind.SCARY);
            if (scary != d.inScaryZone())
            {
                d.setInScaryZone(scary);
            }
            if (Deci.irradiationEnabled())
            {
                boolean rad = Deci.isPlayerInZone(p, ZoneKind.RADIATION) && !p.capabilities.isCreativeMode;
                if (rad != d.irradiated())
                {
                    d.setIrradiated(rad);
                }
                if (!p.capabilities.isCreativeMode && Deci.isPlayerNearZone(p, ZoneKind.RADIATION) && Math.random() < 0.05)
                {
                    p.worldObj.playSoundAtEntity(p, "deci:misc.effect.irradiated", 1.0f, 1.0f);
                }
            }
            // conditions cannot start inside a safezone (Decimation checks it on a dedicated server only)
            UUID id = p.getUniqueID();
            boolean[] was = conditions.get(id);
            if (safe && was != null)
            {
                if (d.infected() && !was[0])
                {
                    d.setInfected(false);
                }
                if (d.bleeding() && !was[1])
                {
                    d.setBleeding(false);
                }
                if (d.brokenLeg() && !was[2])
                {
                    d.setBrokenLeg(false);
                }
            }
            conditions.put(id, new boolean[] {d.infected(), d.bleeding(), d.brokenLeg()});
        }
    }

    // ------------------------------------------------------------ server tick: provocation, zone lists

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.getConfigurationManager() == null)
        {
            return;
        }
        List<?> players = server.getConfigurationManager().playerEntityList;
        tick++;
        if (tick % 20 == 0 && !provoked.isEmpty())
        {
            for (Iterator<Map.Entry<UUID, Long>> it = provoked.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry<UUID, Long> e = it.next();
                EntityPlayer p = online(players, e.getKey());
                if (p == null || p.isDead)
                {
                    it.remove();
                    continue;
                }
                if (e.getValue() < p.worldObj.getTotalWorldTime())
                {
                    it.remove();
                    p.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GRAY + "The guards lost interest in you."));
                    continue;
                }
                hunt(p);
            }
        }
        if (tick % SYNC_EVERY == 0)
        {
            for (Object o : players)
            {
                sync((EntityPlayerMP) o);
            }
        }
    }

    private static EntityPlayer online(List<?> players, UUID id)
    {
        for (Object o : players)
        {
            if (((EntityPlayer) o).getUniqueID().equals(id))
            {
                return (EntityPlayer) o;
            }
        }
        return null;
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        sent.remove(event.player.getUniqueID());
        if (event.player instanceof EntityPlayerMP)
        {
            sync((EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event)
    {
        UUID id = event.player.getUniqueID();
        sent.remove(id);
        conditions.remove(id);
    }

    /** Sends the zones near the player when they differ from the last ones sent. */
    void sync(EntityPlayerMP p)
    {
        if (p.dimension != 0)
        {
            return;
        }
        ObjectZoneList live = Deci.zoneList();
        ArrayList<String> safe = new ArrayList<String>(), mil = new ArrayList<String>(), rad = new ArrayList<String>(),
            scary = new ArrayList<String>(), police = new ArrayList<String>();
        String border = "";
        if (live != null && live.zoneList != null)
        {
            for (ObjectZone z : new ArrayList<ObjectZone>(live.zoneList))
            {
                String box = z.zoneX1 + "/" + z.zoneY1 + "/" + z.zoneZ1 + "=" + z.zoneX2 + "/" + z.zoneY2 + "/" + z.zoneZ2;
                if (Deci.isZone(z, ZoneKind.BORDER))
                {
                    border = box;
                    continue;
                }
                if (!near(z, p))
                {
                    continue;
                }
                if (Deci.isZone(z, ZoneKind.SAFEZONE))
                {
                    safe.add(box);
                }
                else if (Deci.isZone(z, ZoneKind.MILITARY))
                {
                    mil.add(box);
                }
                else if (Deci.isZone(z, ZoneKind.RADIATION))
                {
                    rad.add(box);
                }
                else if (Deci.isZone(z, ZoneKind.SCARY))
                {
                    scary.add(box);
                }
                else if (Deci.isZone(z, ZoneKind.POLICE))
                {
                    police.add(box);
                }
            }
        }
        String text = safe + "|" + mil + "|" + rad + "|" + scary + "|" + police + "|" + border;
        if (text.equals(sent.get(p.getUniqueID())))
        {
            return;
        }
        sent.put(p.getUniqueID(), text);
        Deci.sendZoneLists(p, safe, mil, rad, scary, police, border);
    }

    private static boolean near(ObjectZone z, EntityPlayer p)
    {
        int x0 = Math.min(z.zoneX1, z.zoneX2), x1 = Math.max(z.zoneX1, z.zoneX2);
        int z0 = Math.min(z.zoneZ1, z.zoneZ2), z1 = Math.max(z.zoneZ1, z.zoneZ2);
        return p.posX >= x0 - SYNC_RANGE && p.posX <= x1 + SYNC_RANGE && p.posZ >= z0 - SYNC_RANGE && p.posZ <= z1 + SYNC_RANGE;
    }
}

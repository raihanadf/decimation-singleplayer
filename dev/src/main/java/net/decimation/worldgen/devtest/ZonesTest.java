package net.decimation.worldgen.devtest;

import java.util.ArrayList;
import java.util.List;

import net.decimation.fixes.Deci;
import net.decimation.fixes.ZoneRules;
import net.decimation.mod.server.zones.ObjectZone;
import net.decimation.worldgen.DecimationWorldGen;
import net.decimation.worldgen.ZoneKind;
import net.decimation.worldgen.ZoneStore;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

/**
 * Zones in singleplayer (fixes/ZoneRules, fixes/ZoneSpawnHandler, docs/zones.md section 5). On the
 * arena: a safezone around (X - 20, Z), 25 x 25, and a radiation zone further west. A guns trader,
 * a guard and a civilian stand in the safezone. The player is in survival. Checks, in order:
 * 1. a bandit and an infected spawning inside are refused;
 * 2. water refilled inside, bleeding cannot start inside;
 * 3. radiation zone: irradiated flag on inside (and damage without a gas mask), off outside;
 * 4. a bandit's hit on the civilian inside does nothing;
 * 5. the player's gunshot on the civilian does nothing but provokes: the guard targets the player,
 *    the player loses safezone protection and is hurt by the guard;
 * 6. a bandit outside the safezone gets shot by the guard; a bandit moved inside can be hurt by
 *    the player without provoking anyone;
 * 7. leaving the safezone starts Decimation's grace timer;
 * 8. the client got the safezone box (PacketZoneLists: chat line, map marker).
 * The zones are removed at the end.
 */
public class ZonesTest extends DevTestMode
{
    public String name() { return "zones"; }

    private static final int X = DevTestArena.X, Y = DevTestArena.Y, Z = DevTestArena.Z;
    /** Safezone centre and half size; radiation zone centre. */
    private static final int SX = X - 20, SR = 12, RX = X - 66;

    private volatile int ticks;
    private volatile boolean done;
    private volatile String box;
    private volatile boolean clientSaw;
    private ObjectZone safe, rad;
    private EntityLiving trader, guard, civilian, bandit, inner;
    private float minHealth = 99, banditStart;
    private final List<Entity> spawned = new ArrayList<Entity>();

    public boolean client(Minecraft mc)
    {
        String b = box;
        if (b != null && !clientSaw && Deci.clientSafezones().contains(b))
        {
            clientSaw = true;
        }
        return !done;
    }

    public void server()
    {
        int t = ++ticks;
        EntityPlayerMP p = DevTestUtil.player();
        if (p == null || done)
        {
            return;
        }
        World w = p.worldObj;
        ZoneRules rules = DecimationWorldGen.zoneRules();
        Deci.Player d = Deci.player(p);
        if (t == 20)
        {
            DevTestArena.build(w, p);
            w.setWorldTime(6000);
            p.setGameType(WorldSettings.GameType.SURVIVAL);
            p.capabilities.disableDamage = false;
            safe = ZoneStore.add(ZoneKind.SAFEZONE, SX - SR, Y - 3, Z - SR, SX + SR, Y + 8, Z + SR);
            rad = ZoneStore.add(ZoneKind.RADIATION, RX - 6, Y - 3, Z - 6, RX + 6, Y + 8, Z + 6);
            box = safe.zoneX1 + "/" + safe.zoneY1 + "/" + safe.zoneZ1 + "=" + safe.zoneX2 + "/" + safe.zoneY2 + "/" + safe.zoneZ2;
            trader = spawn(w, EntityList.createEntityByName("TraderGuns", w), SX, Z + 6);
            guard = spawn(w, EntityList.createEntityByName("TraderGuard", w), SX + 8, Z);
            civilian = spawn(w, EntityList.createEntityByName("Human", w), SX - 4, Z - 4);
            DevTestResults.value(name(), "spawned trader / guard / civilian", (trader != null) + " / " + (guard != null)
                + " / " + (civilian != null));
            p.setPositionAndUpdate(SX + 0.5, Y, Z + 0.5);
        }
        if (t == 22)
        {
            boolean b = place(w, Deci.newBandit(w), SX + 2, Z + 2), i = place(w, Deci.newInfected(w), SX - 2, Z + 2);
            DevTestResults.check(name(), "bandit spawn inside refused", !b, !b, "true");
            DevTestResults.check(name(), "infected spawn inside refused", !i, !i, "true");
        }
        if (t == 25)
        {
            d.setWater(5);
            d.setBleeding(false); // a condition from before stays (the rule stops new ones only)
        }
        if (t == 30)
        {
            DevTestResults.check(name(), "water refilled inside", d.water(), d.water() == 20, "20");
            d.setBleeding(true);
        }
        if (t == 33)
        {
            DevTestResults.check(name(), "bleeding cannot start inside", d.bleeding(), !d.bleeding(), "false");
            p.setPositionAndUpdate(RX + 0.5, Y, Z + 0.5);
            p.setHealth(p.getMaxHealth());
        }
        if (t == 73)
        {
            boolean hurt = p.getHealth() < p.getMaxHealth();
            DevTestResults.check(name(), "irradiated inside the radiation zone", d.irradiated(), d.irradiated(), "true");
            DevTestResults.check(name(), "radiation hurts without a gas mask", p.getHealth(), hurt, "< max");
            p.setPositionAndUpdate(SX + 0.5, Y, Z + 0.5);
            p.setHealth(p.getMaxHealth());
        }
        if (t == 78)
        {
            DevTestResults.check(name(), "irradiated off outside", d.irradiated(), !d.irradiated(), "false");
            if (civilian != null && bandit == null)
            {
                float h = civilian.getHealth();
                EntityLiving attacker = Deci.newBandit(w);
                attacker.setLocationAndAngles(SX, Y, Z, 0, 0);
                civilian.attackEntityFrom(DamageSource.causeMobDamage(attacker), 6);
                DevTestResults.check(name(), "bandit hit on a civilian inside", civilian.getHealth(), civilian.getHealth() == h,
                    "no damage (" + h + ")");
            }
        }
        if (t == 85 && civilian != null)
        {
            float h = civilian.getHealth();
            civilian.attackEntityFrom(Deci.gunDamage(p), 6);
            DevTestResults.check(name(), "player shot on a civilian inside", civilian.getHealth(), civilian.getHealth() == h,
                "no damage (" + h + ")");
            DevTestResults.check(name(), "player provoked", rules.isProvoked(p), rules.isProvoked(p), "true");
        }
        if (t > 85 && t < 220)
        {
            minHealth = Math.min(minHealth, p.getHealth());
            if (p.getHealth() < 8)
            {
                p.setHealth(p.getMaxHealth()); // keep the player alive under fire
            }
        }
        if (t == 110)
        {
            DevTestResults.check(name(), "guard targets the provoked player", guard == null ? "none" : String.valueOf(guard.getAITarget() == p),
                guard != null && guard.getAITarget() == p, "true");
            boolean in = Deci.isPlayerInZone(p, ZoneKind.SAFEZONE);
            DevTestResults.check(name(), "provoked player loses safezone protection", in, !in, "false");
        }
        if (t == 220)
        {
            DevTestResults.check(name(), "guard hurts the provoked player", minHealth, minHealth < p.getMaxHealth(), "< max");
            rules.forgive(p);
            p.setHealth(p.getMaxHealth());
            if (guard != null)
            {
                guard.setRevengeTarget(null);
            }
            bandit = spawn(w, Deci.newBandit(w), X + 8, Z);
            inner = spawn(w, Deci.newBandit(w), X + 8, Z + 6);
            banditStart = bandit == null ? 0 : bandit.getHealth();
        }
        if (t == 224 && inner != null)
        {
            inner.setPositionAndUpdate(SX - 6 + 0.5, Y, Z + 6 + 0.5);
        }
        if (t == 228 && inner != null)
        {
            float h = inner.getHealth();
            inner.attackEntityFrom(Deci.gunDamage(p), 4);
            DevTestResults.check(name(), "bandit inside can be hurt", inner.getHealth(), inner.getHealth() < h, "< " + h);
            DevTestResults.check(name(), "hurting a bandit does not provoke", rules.isProvoked(p), !rules.isProvoked(p), "false");
            inner.setDead();
        }
        if (t == 420)
        {
            float h = bandit == null ? -1 : bandit.isDead ? 0 : bandit.getHealth();
            DevTestResults.check(name(), "guard shoots a bandit outside", h, bandit != null && h < banditStart,
                "< " + banditStart);
            p.setPositionAndUpdate(X - 45 + 0.5, Y, Z + 0.5);
        }
        if (t == 430)
        {
            DevTestResults.check(name(), "grace timer after leaving", d.safezoneGrace(), d.safezoneGrace() > 0, "> 0");
        }
        if (t == 440)
        {
            DevTestResults.check(name(), "client got the safezone box", clientSaw, clientSaw, "true");
            ZoneStore.remove(safe);
            ZoneStore.remove(rad);
            for (Entity e : spawned)
            {
                e.setDead();
            }
            if (bandit != null)
            {
                bandit.setDead();
            }
            p.setGameType(WorldSettings.GameType.CREATIVE);
            done = true;
        }
    }

    private boolean place(World w, EntityLiving e, double x, double z)
    {
        e.setLocationAndAngles(x + 0.5, Y, z + 0.5, 0, 0);
        boolean ok = w.spawnEntityInWorld(e);
        if (ok)
        {
            e.setDead();
        }
        return ok;
    }

    private EntityLiving spawn(World w, Entity e, double x, double z)
    {
        for (int i = 0; i < 20 && e instanceof EntityLiving; i++)
        {
            e.setLocationAndAngles(x + 0.5, Y, z + 0.5, 0, 0);
            if (w.spawnEntityInWorld(e))
            {
                spawned.add(e);
                return (EntityLiving) e;
            }
        }
        return null;
    }
}

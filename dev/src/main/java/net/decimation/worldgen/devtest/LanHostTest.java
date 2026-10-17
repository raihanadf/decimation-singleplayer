package net.decimation.worldgen.devtest;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import net.decimation.fixes.Deci;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

/**
 * Player hosting spike (docs/player_hosting.md section 6): this game opens its world to LAN (online mode from
 * `online=true|false`, default false) and writes the port to devtest/lan_port.txt for the relay's host agent. A
 * second game joins through the relay; then, with the joiner in survival on the arena and the host in creative far
 * off: an infected next to the joiner (does it chase the joiner, does the joiner lose health), a bandit (does it shoot
 * the joiner), the joiner kills an infected (Decimation's humanity for that player), then two bandwidth phases for the
 * relay log: standing still, and travelling 200 blocks every 5 s (fresh chunks). Phase times go to the results so the
 * relay's per second byte log can be cut by them. `wait=TICKS` (default 2400) for the joiner.
 */
public class LanHostTest extends DevTestMode
{
    public String name() { return "lanhost"; }

    private final boolean online = "true".equals(System.getProperty("deciworldgen.autotest.online"));
    private final int wait = Integer.parseInt(System.getProperty("deciworldgen.autotest.wait", "2400"));
    private volatile int ticks, joinedAt = -1;
    private volatile boolean done;
    private EntityPlayerMP joiner;
    private int deaths;
    private EntityLiving mob;
    private float minHealth;
    private int humanityBefore;
    private double travelX;

    public boolean client(Minecraft mc)
    {
        return !done;
    }

    public void server()
    {
        int t = ++ticks;
        EntityPlayerMP host = DevTestUtil.player();
        if (host == null || done)
        {
            return;
        }
        IntegratedServer s = (IntegratedServer) MinecraftServer.getServer();
        List<?> players = s.getConfigurationManager().playerEntityList;
        if (t == 20)
        {
            s.setOnlineMode(online);
            // a second run in the same live game finds the world open already (1.7.10 cannot close it again)
            String port = s.getPublic() ? String.valueOf(lastPort) : s.shareToLAN(WorldSettings.GameType.SURVIVAL, false);
            if (!s.getPublic() || port == null || port.equals("-1"))
            {
                DevTestResults.check(name(), "world open to LAN", port, false, "a port");
                done = true;
                return;
            }
            lastPort = Integer.parseInt(port);
            write(port);
            DevTestResults.value(name(), "LAN port", port);
            DevTestResults.value(name(), "online mode", s.isServerInOnlineMode());
            return;
        }
        if (joinedAt < 0)
        {
            for (Object o : players)
            {
                if (o != host)
                {
                    joiner = (EntityPlayerMP) o;
                    joinedAt = t;
                    DevTestResults.check(name(), "second player joined", joiner.getCommandSenderName() + " after "
                                         + (t - 20) / 20 + " s", true, "within " + wait / 20 + " s");
                    stamp("joined");
                }
            }
            if (joinedAt < 0 && t > 20 + wait)
            {
                DevTestResults.check(name(), "second player joined", "nobody", false, "within " + wait / 20 + " s");
                done = true;
            }
            return;
        }
        EntityPlayerMP now = s.getConfigurationManager().func_152612_a(joiner.getCommandSenderName());
        if (now != null && now != joiner)
        {
            deaths++; // a respawn makes a new player object
            joiner = now;
        }
        if (!players.contains(joiner))
        {
            DevTestResults.check(name(), "joiner stays connected", "left at step " + (t - joinedAt), false, "to the end");
            done = true;
            return;
        }
        int j = t - joinedAt;
        World w = joiner.worldObj;
        if (j == 80) // spawn protection is over after 60 ticks
        {
            Deci.Player d = Deci.player(joiner);
            DevTestResults.value(name(), "joiner Decimation data", "bottlecaps " + d.bottlecaps() + ", humanity "
                                 + d.humanity() + ", water " + d.water());
            w.getWorldInfo().setRaining(false);
            w.getWorldInfo().setThundering(false);
            w.setWorldTime(6000);
            DevTestArena.build(w, host);
            host.setGameType(WorldSettings.GameType.CREATIVE);
            host.setPositionAndUpdate(DevTestArena.X - 40, DevTestArena.Y, DevTestArena.Z);
            joiner.setGameType(WorldSettings.GameType.SURVIVAL);
            joiner.setPositionAndUpdate(DevTestArena.X + 0.5, DevTestArena.Y, DevTestArena.Z + 0.5);
            Deci.player(joiner).healFully();
            joiner.setHealth(joiner.getMaxHealth());
        }
        if (j == 120)
        {
            mob = spawn(Deci.newInfected(w), w, 6);
            minHealth = joiner.getHealth();
            stamp("infected");
        }
        if (j > 120 && j < 420)
        {
            minHealth = Math.min(minHealth, joiner.getHealth());
            joiner.setHealth(Math.max(joiner.getHealth(), 6)); // keep the joiner alive: a respawn ends the chase
        }
        if (j == 420)
        {
            DevTestResults.check(name(), "infected chases the joiner", target(mob), joiner == targetEntity(mob),
                                 "the joiner");
            DevTestResults.check(name(), "infected hurts the joiner", joiner.getMaxHealth() + " -> " + minHealth,
                                 minHealth < joiner.getMaxHealth(), "health drops");
            mob.setDead();
            Deci.player(joiner).healFully();
            joiner.setHealth(joiner.getMaxHealth());
        }
        if (j == 440)
        {
            mob = spawn(Deci.newBandit(w), w, 12);
            minHealth = joiner.getHealth();
            stamp("bandit");
        }
        if (j > 440 && j < 840)
        {
            minHealth = Math.min(minHealth, joiner.getHealth());
            joiner.setHealth(Math.max(joiner.getHealth(), 6)); // keep the joiner alive through the bandit's fire
        }
        if (j == 840)
        {
            DevTestResults.check(name(), "bandit targets the joiner", target(mob), joiner == targetEntity(mob),
                                 "the joiner");
            DevTestResults.check(name(), "bandit hurts the joiner", joiner.getMaxHealth() + " -> " + minHealth,
                                 minHealth < joiner.getMaxHealth(), "health drops");
            mob.setDead();
            Deci.player(joiner).healFully();
            joiner.setHealth(joiner.getMaxHealth());
            humanityBefore = Deci.player(joiner).humanity();
            mob = spawn(Deci.newInfected(w), w, 4);
        }
        if (j == 860)
        {
            mob.attackEntityFrom(DamageSource.causePlayerDamage(joiner), 1000);
        }
        if (j == 900)
        {
            int after = Deci.player(joiner).humanity();
            DevTestResults.check(name(), "joiner's infected kill counts", humanityBefore + " -> " + after,
                                 after != humanityBefore, "humanity changes");
            stamp("idle");
        }
        if (j == 1500)
        {
            stamp("travel");
            travelX = DevTestArena.X;
        }
        if (j > 1500 && j <= 2100 && (j - 1500) % 100 == 0)
        {
            travelX += 200;
            joiner.setGameType(WorldSettings.GameType.CREATIVE);
            joiner.setPositionAndUpdate(travelX, 120, DevTestArena.Z);
        }
        if (j == 2100)
        {
            stamp("end");
            joiner.setPositionAndUpdate(DevTestArena.X + 0.5, DevTestArena.Y, DevTestArena.Z + 0.5);
            DevTestResults.value(name(), "joiner ping (keep alive, ms)", joiner.ping);
            DevTestResults.value(name(), "joiner respawns", deaths);
            done = true;
        }
    }

    private static int lastPort = -1;

    private EntityLiving spawn(EntityLiving e, World w, int dx)
    {
        for (int i = 0; i < 10; i++)
        {
            e.setPosition(joiner.posX + dx, DevTestArena.Y, joiner.posZ);
            if (w.spawnEntityInWorld(e))
            {
                return e;
            }
            e = Deci.isInfected(e) ? Deci.newInfected(w) : Deci.newBandit(w);
        }
        return e;
    }

    private static Entity targetEntity(EntityLiving e)
    {
        if (e.getAttackTarget() != null)
        {
            return e.getAttackTarget();
        }
        return e instanceof EntityCreature ? ((EntityCreature) e).getEntityToAttack() : null;
    }

    private static String target(EntityLiving e)
    {
        Entity t = targetEntity(e);
        return t == null ? "none" : t.getCommandSenderName();
    }

    private void stamp(String phase)
    {
        DevTestResults.value(name(), "phase " + phase + " at", System.currentTimeMillis() / 10 / 100.0);
    }

    private static void write(String port)
    {
        try
        {
            File f = new File(Minecraft.getMinecraft().mcDataDir, "devtest/lan_port.txt");
            f.getParentFile().mkdirs();
            FileWriter out = new FileWriter(f);
            out.write(port);
            out.close();
        }
        catch (Exception e)
        {
            DevTestResults.value("lanhost", "port file", e.toString());
        }
    }
}

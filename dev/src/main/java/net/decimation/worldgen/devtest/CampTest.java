package net.decimation.worldgen.devtest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.decimation.fixes.Deci;
import net.decimation.mod.server.zones.ObjectZone;
import net.decimation.worldgen.SafezoneSites;
import net.decimation.worldgen.Slices;
import net.decimation.worldgen.ZoneKind;
import net.decimation.worldgen.ZoneStore;
import net.decimation.worldgen.military.SurvivorCampPlan;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

/**
 * Survivor camp (docs/safezone.md) built on flat ground in the sky at -20000, 230, -19700, with its
 * SAFEZONE and its residents as the world generator would register them. Checks: no prop clash or
 * refusal in the generator log, every resident spawned by the keeper (by kind), the residents are
 * protected (a player shot at a trader does nothing), the player inside counts as in a safezone.
 * Then one shot per camera point. Writes run/client/devtest/camp.tsv for tools/props/propclash.py
 * and walkcheck.py. -Pturns=0..3, -Pseed=N, -Ppoints=a,b. Zone, site and residents are removed at
 * the end; the blocks stay in the sky.
 */
public class CampTest extends DevTestMode
{
    public String name() { return "camp"; }

    private static final int Y = 230, X0 = -20000, Z0 = -19700;
    private static final int BUILD = 10, FILL = 140, SETTLE = 40, SHOT = 70;

    private final int turns = Integer.parseInt(System.getProperty("deciworldgen.autotest.turns", "0"));
    private final long seed = Long.parseLong(System.getProperty("deciworldgen.autotest.seed", "42"));
    private final String only = System.getProperty("deciworldgen.autotest.points", "");

    private final List<float[]> shots = new ArrayList<float[]>();
    private final List<String> names = new ArrayList<String>();
    private volatile int ticks;
    private volatile boolean built;
    private int taken = -1;
    private SurvivorCampPlan plan;
    private ObjectZone zone;
    private String id;

    public boolean client(Minecraft mc)
    {
        int t = ticks;
        if (mc.thePlayer == null || !built)
        {
            return true;
        }
        int i = (t - FILL - 20) / SHOT, u = (t - FILL - 20) % SHOT;
        if (i >= shots.size())
        {
            mc.gameSettings.hideGUI = false;
            return t < FILL + 20 + shots.size() * SHOT + 40;
        }
        if (i < 0)
        {
            return true;
        }
        float[] s = shots.get(i);
        mc.gameSettings.hideGUI = true;
        mc.gameSettings.fovSetting = 80f;
        mc.gameSettings.clouds = false;
        mc.gameSettings.renderDistanceChunks = 16;
        mc.thePlayer.rotationYaw = mc.thePlayer.prevRotationYaw = s[3];
        mc.thePlayer.rotationPitch = mc.thePlayer.prevRotationPitch = s[4];
        if (u >= SETTLE && taken != i)
        {
            taken = i;
            DevTestUtil.screenshot(mc, name(), "camp_" + names.get(i));
        }
        return true;
    }

    public void server()
    {
        int t = ++ticks;
        EntityPlayerMP p = DevTestUtil.player();
        if (p == null)
        {
            return;
        }
        World w = p.worldObj;
        if (t == 2)
        {
            p.capabilities.isFlying = true;
            p.capabilities.disableDamage = true;
            w.setWorldTime(6000);
            w.difficultySetting = net.minecraft.world.EnumDifficulty.NORMAL;
            w.getWorldInfo().setRaining(false);
            w.getWorldInfo().setThundering(false);
            p.playerNetServerHandler.setPlayerLocation(X0, Y + 40, Z0, 0, 60);
        }
        if (t == BUILD)
        {
            id = "TC_" + seed + "_" + turns;
            SafezoneSites.remove(id);
            plan = new SurvivorCampPlan(id, seed, X0, Z0, turns);
            pregenerate(w, plan);
            long t0 = System.nanoTime();
            Slices.placeAt(w, plan, Y, false);
            DevTestResults.value(name(), "built ms", (System.nanoTime() - t0) / 1000000);
            int problems = 0;
            for (String line : plan.debugLog())
            {
                DevTestResults.value(name(), "log", line);
                problems++;
            }
            DevTestResults.check(name(), "clashes and refusals", problems, problems == 0, "0");
            // as Slices does for a generated plan: the zone box 6 around, the residents
            int x0 = plan.minX() - 6, z0 = plan.minZ() - 6, x1 = plan.maxX() + 6, z1 = plan.maxZ() + 6;
            zone = ZoneStore.add(ZoneKind.SAFEZONE, x0, Y - 4, z0, x1, Y + plan.height() + 12, z1);
            SafezoneSites.add(id, x0, Y - 4, z0, x1, Y + plan.height() + 12, z1, plan.residents(Y));
            dump(w, plan);
            p.playerNetServerHandler.setPlayerLocation((plan.minX() + plan.maxX()) / 2.0, Y + 1, (plan.minZ() + plan.maxZ()) / 2.0, 0, 0);
            for (Map.Entry<String, float[]> e : plan.pointsOfInterest().entrySet())
            {
                if (!only.isEmpty() && !("," + only + ",").contains("," + e.getKey() + ","))
                {
                    continue;
                }
                float[] q = e.getValue();
                shots.add(new float[] {q[0], Y + q[1], q[2], q[3], q[4]});
                names.add(e.getKey());
            }
        }
        if (t == FILL)
        {
            SafezoneSites.Site s = SafezoneSites.find(id);
            Map<String, Integer> want = new HashMap<String, Integer>(), got = new HashMap<String, Integer>();
            for (SafezoneSites.Post q : s.posts)
            {
                String k = q.kind.startsWith("trader:") ? "trader" : q.kind;
                want.put(k, (want.containsKey(k) ? want.get(k) : 0) + 1);
            }
            EntityLiving trader = null;
            for (Object o : w.loadedEntityList)
            {
                Entity e = (Entity) o;
                if (e instanceof EntityLiving && !e.isDead && id.equals(e.getEntityData().getString(SafezoneSites.SITE_TAG)))
                {
                    String k = s.posts.get(e.getEntityData().getInteger(SafezoneSites.POST_TAG)).kind;
                    k = k.startsWith("trader:") ? "trader" : k;
                    got.put(k, (got.containsKey(k) ? got.get(k) : 0) + 1);
                    if (k.equals("trader") && trader == null)
                    {
                        trader = (EntityLiving) e;
                    }
                }
            }
            for (String k : want.keySet())
            {
                int n = got.containsKey(k) ? got.get(k) : 0;
                DevTestResults.check(name(), "residents " + k, n, n == want.get(k), want.get(k) + "");
            }
            DevTestResults.check(name(), "site filled", s.filled, s.filled, "true");
            boolean in = Deci.isPlayerInZone(p, ZoneKind.SAFEZONE);
            DevTestResults.value(name(), "player at", (int) p.posX + "," + (int) p.posY + "," + (int) p.posZ + " combat "
                + Deci.player(p).combatTimer() + " zone " + zone.zoneX1 + "," + zone.zoneY1 + "," + zone.zoneZ1 + ".."
                + zone.zoneX2 + "," + zone.zoneY2 + "," + zone.zoneZ2 + " live " + Deci.zoneList().zoneList.contains(zone));
            DevTestResults.check(name(), "player inside counts as safezone", in, in, "true");
            if (trader != null)
            {
                float h = trader.getHealth();
                trader.attackEntityFrom(Deci.gunDamage(p), 6);
                DevTestResults.check(name(), "trader shot does nothing", trader.getHealth(), trader.getHealth() == h, "" + h);
                net.decimation.worldgen.DecimationWorldGen.zoneRules().forgive(p);
            }
            built = true;
        }
        int end = FILL + 20 + shots.size() * SHOT;
        if (built)
        {
            int i = (t - FILL - 20) / SHOT;
            if (i >= 0 && i < shots.size())
            {
                float[] s = shots.get(i);
                p.playerNetServerHandler.setPlayerLocation(s[0], s[1] - 1.62, s[2], s[3], s[4]);
                p.motionX = p.motionY = p.motionZ = 0;
            }
            if (t == end + 10)
            {
                ZoneStore.remove(zone);
                SafezoneSites.remove(id);
                for (Object o : w.loadedEntityList)
                {
                    Entity e = (Entity) o;
                    if (id.equals(e.getEntityData().getString(SafezoneSites.SITE_TAG)))
                    {
                        e.setDead();
                    }
                }
            }
        }
    }

    private static void pregenerate(World w, SurvivorCampPlan p)
    {
        net.minecraft.world.gen.ChunkProviderServer cps = (net.minecraft.world.gen.ChunkProviderServer) w.getChunkProvider();
        for (int cx = (p.minX() >> 4) - 3; cx <= (p.maxX() >> 4) + 3; cx++)
        {
            for (int cz = (p.minZ() >> 4) - 3; cz <= (p.maxZ() >> 4) + 3; cz++)
            {
                cps.loadChunk(cx, cz);
            }
        }
    }

    /** Every block of the camp to run/client/devtest/camp.tsv ("x y z name meta" from the plan corner and Y). */
    private static void dump(World w, SurvivorCampPlan plan)
    {
        java.io.File f = new java.io.File(Minecraft.getMinecraft().mcDataDir, "devtest/camp.tsv");
        try (java.io.PrintWriter out = new java.io.PrintWriter(f, "UTF-8"))
        {
            out.println("# origin " + plan.minX() + " " + Y + " " + plan.minZ() + " camp");
            for (Map.Entry<String, float[]> e : plan.pointsOfInterest().entrySet())
            {
                float[] q = e.getValue();
                out.println("# poi " + e.getKey() + " " + q[0] + " " + q[1] + " " + q[2]);
            }
            for (int y = Y - 1; y <= Y + plan.height(); y++)
            {
                for (int x = plan.minX(); x <= plan.maxX(); x++)
                {
                    for (int z = plan.minZ(); z <= plan.maxZ(); z++)
                    {
                        net.minecraft.block.Block b = w.getBlock(x, y, z);
                        if (b != net.minecraft.init.Blocks.air)
                        {
                            out.println((x - plan.minX()) + " " + (y - Y) + " " + (z - plan.minZ()) + " "
                                        + net.minecraft.block.Block.blockRegistry.getNameForObject(b) + " "
                                        + w.getBlockMetadata(x, y, z));
                        }
                    }
                }
            }
        }
        catch (java.io.IOException e)
        {
            DevTestResults.value("camp", "dump failed", e.toString());
        }
    }
}

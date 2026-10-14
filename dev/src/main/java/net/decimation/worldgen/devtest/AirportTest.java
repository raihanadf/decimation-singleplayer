package net.decimation.worldgen.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.decimation.worldgen.military.AirportPlan;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/**
 * The military airport (docs/airport.md) built in the sky at -20000, 230, -18300 on a stone layer, written
 * straight from its plan (turns 0; -Pturns=3 for a runway along x), no garrison (nobody shoots the camera).
 * Checks: no prop clash (Canvas.validateProps). Writes run/client/devtest/airport.tsv for tools/props/propclash.py
 * and walkcheck.py; one shot per camera point. -Pseed=N.
 */
public class AirportTest extends DevTestMode
{
    public String name() { return "airport"; }

    private static final int Y = 230, X0 = -20000, Z0 = -18300, BUILD = 10, START = 80, SETTLE = 50, SHOT = 80;

    private final int turns = Integer.parseInt(System.getProperty("deciworldgen.autotest.turns", "0"));
    private final long seed = Long.parseLong(System.getProperty("deciworldgen.autotest.seed", "42"));
    private final List<float[]> shots = new ArrayList<float[]>();
    private final List<String> names = new ArrayList<String>();
    private volatile int ticks;
    private volatile boolean built;
    private int taken = -1;

    public boolean client(Minecraft mc)
    {
        int t = ticks;
        if (mc.thePlayer == null || !built)
        {
            return true;
        }
        int i = (t - START) / SHOT, u = (t - START) % SHOT;
        if (i >= shots.size())
        {
            mc.gameSettings.hideGUI = false;
            return t < START + shots.size() * SHOT + 40;
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
            DevTestUtil.screenshot(mc, name(), name() + "_" + names.get(i));
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
            w.getWorldInfo().setRaining(false);
            p.playerNetServerHandler.setPlayerLocation(X0 + 56, Y + 40, Z0 + 120, 0, 90);
        }
        if (t == BUILD)
        {
            AirportPlan plan = new AirportPlan("TA_" + seed, seed, X0, Z0, turns, Y);
            long t0 = System.nanoTime();
            int[] m = new int[1];
            for (int x = plan.minX(); x <= plan.maxX(); x++)
            {
                for (int z = plan.minZ(); z <= plan.maxZ(); z++)
                {
                    w.getChunkProvider().loadChunk(x >> 4, z >> 4);
                    if (w.getBlock(x, Y - 1, z) != Blocks.stone)
                    {
                        w.setBlock(x, Y - 1, z, Blocks.stone, 0, 2);   // under the gravel ground layer
                    }
                    for (int y = 0; y <= plan.height(); y++)
                    {
                        m[0] = 0;
                        Block b = plan.blockAt(x - plan.minX(), y, z - plan.minZ(), m);
                        if (b != null)
                        {
                            w.setBlock(x, Y + y, z, b, m[0], 2);
                            if (b == Blocks.mob_spawner)
                            {
                                net.decimation.worldgen.Garrisons.setupSpawner(w, x, Y + y, z);
                            }
                        }
                        else if (!w.isAirBlock(x, Y + y, z))
                        {
                            w.setBlock(x, Y + y, z, Blocks.air, 0, 2);
                        }
                    }
                }
            }
            DevTestResults.value(name(), "built ms", (System.nanoTime() - t0) / 1000000);
            List<String> log = plan.debugLog();
            for (String line : log)
            {
                DevTestResults.value(name(), "log", line);
            }
            DevTestResults.check(name(), "clashes", log.size(), log.isEmpty(), "0");
            DevTestResults.value(name(), "garrison posts and spawners", plan.garrison(Y).length / 3);
            dump(w, plan);
            for (Map.Entry<String, float[]> e : plan.pointsOfInterest().entrySet())
            {
                if (e.getKey().equals("main_road"))
                {
                    continue;                                   // the walk test's start, on the ground
                }
                float[] q = e.getValue();
                shots.add(new float[] {q[0], Y + q[1], q[2], q[3], q[4]});
                names.add(e.getKey());
            }
            built = true;
        }
        if (built)
        {
            int i = (t - START) / SHOT;
            if (i >= 0 && i < shots.size())
            {
                float[] s = shots.get(i);
                p.playerNetServerHandler.setPlayerLocation(s[0], s[1] - 1.62, s[2], s[3], s[4]);
                p.motionX = p.motionY = p.motionZ = 0;
            }
        }
    }

    private static void dump(World w, AirportPlan plan)
    {
        java.io.File f = new java.io.File(Minecraft.getMinecraft().mcDataDir, "devtest/airport.tsv");
        try (java.io.PrintWriter out = new java.io.PrintWriter(f, "UTF-8"))
        {
            out.println("# origin " + plan.minX() + " " + Y + " " + plan.minZ() + " airport");
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
                        Block b = w.getBlock(x, y, z);
                        if (b != Blocks.air)
                        {
                            out.println((x - plan.minX()) + " " + (y - Y) + " " + (z - plan.minZ()) + " "
                                        + Block.blockRegistry.getNameForObject(b) + " " + w.getBlockMetadata(x, y, z));
                        }
                    }
                }
            }
        }
        catch (java.io.IOException e)
        {
            DevTestResults.value("airport", "dump failed", e.toString());
        }
    }
}

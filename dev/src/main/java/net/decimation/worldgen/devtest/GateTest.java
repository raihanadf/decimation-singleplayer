package net.decimation.worldgen.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.decimation.worldgen.military.CityGate;
import net.decimation.worldgen.military.CityGatePlan;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/**
 * The wasteland gates (military/CityGate) built in the sky at -20000, 230, -19100 on a stone pad: every look
 * x state (looks along x with the compact toll first, states along z), each written straight from its plan (turns 1 as
 * on a line along z; -Pturns=2 for a line along x). Checks: no prop clash (Canvas.validateProps), landmines
 * spawned by the wire gates. Writes run/client/devtest/gates_<look>_<state>.tsv for tools/props/propclash.py;
 * one front and one top shot each. -Pgates=toll/wire,compact_toll/hesco picks some (default all).
 */
public class GateTest extends DevTestMode
{
    public String name() { return "gates"; }

    private static final int Y = 230, X0 = -20000, Z0 = -19100, STEP = 26, SETTLE = 40, SHOT = 70, BUILD = 10, START = 60;

    private final int turns = Integer.parseInt(System.getProperty("deciworldgen.autotest.turns", "1"));
    private final String pick = System.getProperty("deciworldgen.autotest.gates", "");

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
            p.playerNetServerHandler.setPlayerLocation(X0, Y + 30, Z0, 0, 60);
        }
        if (t == BUILD)
        {
            int mines = 0;
            for (int state = 0; state < CityGate.STATES.length; state++)
            {
                for (int look = -1; look < CityGate.LOOKS.length; look++)
                {
                    String n = CityGate.name(look, state);
                    if (!pick.isEmpty() && !("," + pick + ",").contains("," + n.replace(' ', '_') + ","))   // compact_toll/wire
                    {
                        continue;
                    }
                    int x = X0 + (look + 1) * STEP, z = Z0 + state * STEP;
                    CityGatePlan plan = new CityGatePlan("TG_" + (look + 1) + "_" + state, x, z, turns, look, state);
                    int m = build(w, plan);
                    mines += m;
                    List<String> log = plan.debugLog();
                    for (String line : log)
                    {
                        DevTestResults.value(name(), n + " log", line);
                    }
                    DevTestResults.check(name(), n + " clashes", log.size(), log.isEmpty(), "0");
                    if (look == 1)
                    {
                        // the checkpoint's flag pole (a prop placed outside the canvas is dropped without a word)
                        int flags = count(w, plan, Block.getBlockFromName("deci:BlockFlagPollUAHD"));
                        DevTestResults.check(name(), n + " flag", flags, flags == 1, "1 (BlockFlagPollUAHD)");
                    }
                    if (state == CityGate.WIRE)
                    {
                        DevTestResults.check(name(), n + " landmines", m, m > 0, "> 0");
                    }
                    dump(w, plan, "gates_" + n.replace('/', '_').replace(' ', '_'));
                    for (Map.Entry<String, float[]> e : plan.pointsOfInterest().entrySet())
                    {
                        float[] q = e.getValue();
                        shots.add(new float[] {q[0], Y + q[1], q[2], q[3], q[4]});   // heights above the street surface
                        names.add(n.replace('/', '_').replace(' ', '_') + "_" + e.getKey());
                    }
                }
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

    /**
     * A stone pad 6 around at the street surface (Y), cleared air above, then every cell of the plan written
     * straight (canvas y 0 = Y); landmine markers spawn mines. Returns the mines spawned.
     */
    static int build(World w, CityGatePlan plan)
    {
        int base = Y, mines = 0;
        // one layer of pad at the surface round the plan (thick pads cost seconds of sky light updates each)
        for (int x = plan.minX() - 4; x <= plan.maxX() + 4; x++)
        {
            for (int z = plan.minZ() - 4; z <= plan.maxZ() + 4; z++)
            {
                w.getChunkProvider().loadChunk(x >> 4, z >> 4);
                if (w.getBlock(x, Y, z) != Blocks.stone)
                {
                    w.setBlock(x, Y, z, Blocks.stone, 0, 2);
                }
            }
        }
        // mines of an earlier run here go first
        for (Object o : new ArrayList<Object>(w.loadedEntityList))
        {
            net.minecraft.entity.Entity e = (net.minecraft.entity.Entity) o;
            if (e.posX >= plan.minX() && e.posX <= plan.maxX() + 1 && e.posZ >= plan.minZ() && e.posZ <= plan.maxZ() + 1
                && e.posY > base && e.posY < Y + CityGate.H && !(e instanceof net.minecraft.entity.player.EntityPlayer))
            {
                e.setDead();
            }
        }
        int[] m = new int[1];
        for (int y = 0; y < CityGate.H; y++)
        {
            for (int x = plan.minX(); x <= plan.maxX(); x++)
            {
                for (int z = plan.minZ(); z <= plan.maxZ(); z++)
                {
                    m[0] = 0;
                    Block b = plan.blockAt(x - plan.minX(), y, z - plan.minZ(), m);
                    if (b == CityGate.MINE)
                    {
                        w.setBlock(x, base + y, z, Blocks.air, 0, 2);
                        w.spawnEntityInWorld(net.decimation.fixes.Deci.newLandmine(w, x + 0.5, base + y, z + 0.5));
                        mines++;
                    }
                    else if (b != null)
                    {
                        w.setBlock(x, base + y, z, b, m[0], 2);
                    }
                    else if (plan.blockAt(x - plan.minX(), 0, z - plan.minZ(), m) != null)   // an owned column (ground under it)
                    {
                        w.setBlock(x, base + y, z, Blocks.air, 0, 2);   // owned cells: air where the plan has none
                    }
                }
            }
        }
        return mines;
    }

    /** Every block of the plan to run/client/devtest/<file>.tsv ("x y z name meta" from the plan corner and Y). */
    private static void dump(World w, CityGatePlan plan, String file)
    {
        java.io.File f = new java.io.File(Minecraft.getMinecraft().mcDataDir, "devtest/" + file + ".tsv");
        try (java.io.PrintWriter out = new java.io.PrintWriter(f, "UTF-8"))
        {
            out.println("# origin " + plan.minX() + " " + Y + " " + plan.minZ() + " " + file);
            for (int y = Y - 1; y <= Y + CityGate.H; y++)
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
            DevTestResults.value("gates", "dump failed", e.toString());
        }
    }

    /** How many cells of a built gate hold this block (surface to 12 above). */
    private static int count(World w, CityGatePlan plan, Block b)
    {
        int n = 0;
        for (int x = plan.minX(); x <= plan.maxX(); x++)
        {
            for (int z = plan.minZ(); z <= plan.maxZ(); z++)
            {
                for (int y = Y; y <= Y + 12; y++)
                {
                    n += b != null && w.getBlock(x, y, z) == b ? 1 : 0;
                }
            }
        }
        return n;
    }
}

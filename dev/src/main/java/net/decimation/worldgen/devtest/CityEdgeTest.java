package net.decimation.worldgen.devtest;

import java.util.ArrayList;
import java.util.List;

import net.decimation.worldgen.city.LcCity;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/**
 * The city edge (docs/city_engine.md "Open", EdgePlan.wear; user 2026-10-13: the edge "looks not
 * natural"): generates the north edge of a city cell whose band was never generated (cells searched
 * outward from -Prx, -Prz, default 40 / 6; each run moves on), counts the worn ground in the band (slab,
 * stone, cobblestone, mossy cobblestone tops within 10 of the city) and photographs it: from the street
 * looking out, from the land looking in, along the band, from above (cityedge_<n>.png).
 */
public class CityEdgeTest extends DevTestMode
{
    public String name() { return "cityedge"; }

    private static final int BUILD = 10, SETTLE = 40, SHOT = 60;

    private final int rz = Integer.parseInt(System.getProperty("deciworldgen.autotest.rz", "6"));
    private final String rxProp = System.getProperty("deciworldgen.autotest.rx", "");
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
        int i = (t - BUILD - 20) / SHOT, u = (t - BUILD - 20) % SHOT;
        if (i >= shots.size())
        {
            mc.gameSettings.hideGUI = false;
            return false;
        }
        if (i < 0)
        {
            return true;
        }
        float[] s = shots.get(i);
        mc.gameSettings.hideGUI = true;
        mc.gameSettings.fovSetting = 70f;
        mc.thePlayer.rotationYaw = mc.thePlayer.prevRotationYaw = s[3];
        mc.thePlayer.rotationPitch = mc.thePlayer.prevRotationPitch = s[4];
        if (u >= SETTLE && taken != i)
        {
            taken = i;
            DevTestUtil.screenshot(mc, name(), "cityedge_" + names.get(i));
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
            w.getWorldInfo().setThundering(false);
        }
        if (t == BUILD)
        {
            build(w);
            built = true;
        }
        if (built)
        {
            int i = (t - BUILD - 20) / SHOT;
            if (i >= 0 && i < shots.size())
            {
                float[] s = shots.get(i);
                p.playerNetServerHandler.setPlayerLocation(s[0], s[1] - 1.62, s[2], s[3], s[4]);
                p.motionX = p.motionY = p.motionZ = 0;
            }
        }
    }

    private void build(World w)
    {
        long seed = w.getSeed();
        net.minecraft.world.gen.ChunkProviderServer cps = (net.minecraft.world.gen.ChunkProviderServer) w.getChunkProvider();
        int sx = rxProp.isEmpty() ? 40 : Integer.parseInt(rxProp);
        for (int r = 0; r <= 80; r++)
        {
            for (int i = -r; i <= r; i++)
            {
                for (int j = -r; j <= r; j++)
                {
                    int cx = sx + i, cz = rz + j;
                    if (Math.max(Math.abs(i), Math.abs(j)) != r || !LcCity.isCity(seed, cx, cz) || LcCity.isCity(seed, cx, cz - 1)
                        || LcCity.isCity(seed, cx - 1, cz - 1) || LcCity.isCity(seed, cx + 1, cz - 1))
                    {
                        continue;                         // a city cell with open land north of it
                    }
                    int chx = cx * LcCity.CELL + LcCity.CELL / 2, chz = cz * LcCity.CELL - 1;
                    if (cps.currentChunkLoader instanceof net.minecraft.world.chunk.storage.AnvilChunkLoader
                        && ((net.minecraft.world.chunk.storage.AnvilChunkLoader) cps.currentChunkLoader).chunkExists(w, chx, chz))
                    {
                        continue;
                    }
                    for (int a = cx * LcCity.CELL - 2; a < (cx + 1) * LcCity.CELL + 2; a++)
                    {
                        for (int b = cz * LcCity.CELL - 4; b < cz * LcCity.CELL + 3; b++)
                        {
                            cps.loadChunk(a, b);
                        }
                    }
                    survey(w, cx, cz);
                    ramp(w, cps, cx, cz);
                    return;
                }
            }
        }
        DevTestResults.value(name(), "edge", "none found");
    }

    /**
     * v0.56 asphalt ramp between city levels (StreetPlan.rampAt): the first road slab in the cells south
     * of the edge cell (generated here), photographed from its low side.
     */
    private void ramp(World w, net.minecraft.world.gen.ChunkProviderServer cps, int cx, int cz)
    {
        Block slab = Block.getBlockFromName("deci:BlockRoadSlab");
        for (int dz = 0; dz < 3; dz++)
        {
            for (int dx = -1; dx <= 1; dx++)
            {
                int c0x = (cx + dx) * LcCity.CELL, c0z = (cz + dz) * LcCity.CELL;
                for (int a = c0x - 1; a <= c0x + LcCity.CELL; a++)
                {
                    for (int b = c0z - 1; b <= c0z + LcCity.CELL; b++)
                    {
                        cps.loadChunk(a, b);
                    }
                }
                for (int x = c0x * 16; x < (c0x + LcCity.CELL) * 16; x++)
                {
                    for (int z = c0z * 16; z < (c0z + LcCity.CELL) * 16; z++)
                    {
                        for (int y = 60; y < 110; y++)
                        {
                            if (w.getBlock(x, y, z) == slab)
                            {
                                float[] cam = CityPropsTest.camera(w, x, y, z, 10, -1);
                                if (cam != null)
                                {
                                    shots.add(cam);
                                    names.add("ramp");
                                }
                                DevTestResults.value(name(), "ramp", x + "," + y + "," + z);
                                return;
                            }
                        }
                    }
                }
            }
        }
        DevTestResults.value(name(), "ramp", "none in the 9 cells south of the edge");
    }

    private void survey(World w, int cx, int cz)
    {
        long seed = w.getSeed();
        int x0 = cx * LcCity.CELL * 16, z0 = cz * LcCity.CELL * 16, g = LcCity.ground(seed, cx, cz);
        DevTestResults.value(name(), "edge", "cell " + cx + "," + cz + " (north edge z " + z0 + ", x " + x0 + " to " + (x0 + 63)
            + ", ground " + g + ")");
        int[] rows = new int[10];
        int worn = 0, columns = 0;
        for (int x = x0; x < x0 + 64; x++)
        {
            for (int d = 1; d <= 10; d++)
            {
                int z = z0 - d, y = w.getHeightValue(x, z) - 1;
                Block b = w.getBlock(x, y, z);
                while (y > 1 && (b.getMaterial().isReplaceable() || b == Blocks.leaves || b == Blocks.log))
                {
                    b = w.getBlock(x, --y, z);
                }
                columns++;
                boolean wear = b == Blocks.double_stone_slab || b == Blocks.stone || b == Blocks.cobblestone
                    || b == Blocks.mossy_cobblestone;
                if (wear)
                {
                    worn++;
                    rows[d - 1]++;
                }
            }
        }
        StringBuilder byRow = new StringBuilder();
        for (int d = 0; d < 10; d++)
        {
            byRow.append(d == 0 ? "" : " ").append(rows[d]);
        }
        DevTestResults.value(name(), "worn tops by distance 1..10 (of 64)", byRow.toString());
        DevTestResults.check(name(), "worn ground along the edge", worn + " of " + columns,
                             worn > columns / 10 && rows[0] > rows[7], "a band, densest at the city");
        float mx = x0 + 32.5f;
        shots.add(new float[] {mx, g + 2.62f, z0 + 3.5f, 180, 12});
        names.add("out");
        int outY = w.getHeightValue((int) mx, z0 - 22);
        shots.add(new float[] {mx, Math.max(outY, g) + 3.62f, z0 - 22.5f, 0, 14});
        names.add("in");
        shots.add(new float[] {x0 + 2.5f, g + 6.62f, z0 - 6.5f, -90, 25});
        names.add("along");
        shots.add(new float[] {mx, g + 40, z0 - 4f, 180, 90});
        names.add("aerial");
    }
}

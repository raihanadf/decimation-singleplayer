package net.decimation.worldgen.devtest;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.decimation.worldgen.city.CityRivers;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/**
 * Rivers through cities and their bridges (city/CityRivers, city/RiverPlan, docs/city_engine.md
 * "Rivers and bridges"). Switches city rivers on for this session (the dev world is older than
 * them), finds the first city region from rx 40 (or -Prx) on region row -Prz (default 6) with a
 * bridge whose chunk was never generated, generates around it, then checks: the bridge deck at
 * the street level, water in the canals, nothing built over a canal, a player can walk across
 * from street to street, no water above the canal level. Shots bridge_<view>.png.
 */
public class BridgeTest extends DevTestMode
{
    public String name() { return "bridge"; }

    private static final int BUILD = 10, SETTLE = 40, SHOT = 70, R = 4;

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
        mc.gameSettings.fovSetting = 80f;
        mc.thePlayer.rotationYaw = mc.thePlayer.prevRotationYaw = s[3];
        mc.thePlayer.rotationPitch = mc.thePlayer.prevRotationPitch = s[4];
        if (u >= SETTLE && taken != i)
        {
            taken = i;
            DevTestUtil.screenshot(mc, name(), "bridge_" + names.get(i));
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
            CityRivers.force(w, true);
        }
        if (t == BUILD)
        {
            int[] b = find(w);
            if (b == null)
            {
                DevTestResults.value(name(), "bridge", "none found");
                built = true;
                return;
            }
            check(w, b);
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
            if (i >= shots.size())
            {
                CityRivers.force(w, CityRivers.stored(w));
            }
        }
    }

    private void check(World w, int[] b)
    {
        long seed = w.getSeed();
        int cx = b[0], cz = b[1], g = b[3];
        boolean alongX = b[2] == 0;
        int dx = alongX ? 1 : 0, dz = alongX ? 0 : 1;
        DevTestResults.value(name(), "bridge", (cx << 4) + "," + g + "," + (cz << 4) + (alongX ? " along x" : " along z"));
        // the span: street chunks on the river along the street, land at both ends
        int a = 0, e = 0;
        while (a < 8 && CityRivers.chunk(seed, cx - (a + 1) * dx, cz - (a + 1) * dz)) a++;
        while (e < 8 && CityRivers.chunk(seed, cx + (e + 1) * dx, cz + (e + 1) * dz)) e++;
        int sx = cx - (a + 1) * dx, sz = cz - (a + 1) * dz, ex = cx + (e + 1) * dx, ez = cz + (e + 1) * dz;
        DevTestResults.value(name(), "span chunks", a + e + 1);
        // a deck at the street level over every span chunk: 64+ of its 256 cells (the wasteland
        // district's broken bridge has only its sidewalks)
        int decks = 0;
        for (int k = -a; k <= e; k++)
        {
            int n = 0, x0 = (cx + k * dx) << 4, z0 = (cz + k * dz) << 4;
            for (int x = x0; x < x0 + 16; x++)
            {
                for (int z = z0; z < z0 + 16; z++)
                {
                    n += w.isAirBlock(x, g, z) ? 0 : 1;
                }
            }
            decks += n >= 64 ? 1 : 0;
        }
        DevTestResults.check(name(), "deck at street level", decks, decks == a + e + 1, (a + e + 1) + " chunks");
        // canals around: water, nothing built over them, no water above the canal
        int canals = 0, wet = 0, built = 0, spill = 0;
        for (int i = cx - R; i <= cx + R; i++)
        {
            for (int k = cz - R; k <= cz + R; k++)
            {
                if (!net.decimation.worldgen.city.LcCity.isCity(seed, Math.floorDiv(i, 4), Math.floorDiv(k, 4)))
                {
                    continue; // natural ponds outside may lie higher
                }
                for (int x = i << 4; x < (i << 4) + 16; x++)
                {
                    for (int z = k << 4; z < (k << 4) + 16; z++)
                    {
                        for (int y = 63; y <= g + 3; y++)
                        {
                            spill += w.getBlock(x, y, z).getMaterial().isLiquid() ? 1 : 0;
                        }
                    }
                }
                if (!CityRivers.chunk(seed, i, k))
                {
                    continue;
                }
                canals++;
                wet += w.getBlock((i << 4) + 4, 60, (k << 4) + 4) == Blocks.water ? 1 : 0;
                for (int x = (i << 4) + 1; x < (i << 4) + 15; x++)
                {
                    for (int z = (k << 4) + 1; z < (k << 4) + 15; z++)
                    {
                        for (int y = g + 6; y <= g + 20; y++)
                        {
                            built += w.isAirBlock(x, y, z) ? 0 : 1;
                        }
                    }
                }
            }
        }
        DevTestResults.check(name(), "canal chunks with water", wet, wet == canals, canals + " canal chunks");
        DevTestResults.check(name(), "blocks over the canals (street + 6 .. 20)", built, built == 0, "0");
        DevTestResults.check(name(), "water above the canal level (city chunks)", spill, spill == 0, "0");
        int across = walk(w, sx, sz, ex, ez, g);
        DevTestResults.check(name(), "street to street across on foot", across, across > 0, "far street cells reached > 0");
        // shots: from above, along the deck, from the canal side, under it
        float mx = (cx << 4) + 8, mz = (cz << 4) + 8;
        shot("aerial", mx - 20 * dz, g + 30, mz - 20 * dx, alongX ? 0 : -90, 55);
        shot("deck", (sx << 4) + 8 - 6 * dx, g + 1.62f, (sz << 4) + 8 - 6 * dz, alongX ? -90 : 0, 5);
        shot("side", mx - 14 * dz + 0.5f, g + 3, mz - 14 * dx + 0.5f, alongX ? 0 : -90, 15);
        shot("under", mx - 6 * dz + 0.5f, 63.6f, mz - 6 * dx + 0.5f, alongX ? 0 : -90, 0);
    }

    private void shot(String n, float x, float eyeY, float z, float yaw, float pitch)
    {
        shots.add(new float[] {x, eyeY, z, yaw, pitch});
        names.add(n);
    }

    /** Walk from standing cells of the start street chunk to any in the end one, over the span. */
    private static int walk(World w, int sx, int sz, int ex, int ez, int g)
    {
        int x0 = Math.min(sx, ex) << 4, x1 = (Math.max(sx, ex) << 4) + 15;
        int z0 = Math.min(sz, ez) << 4, z1 = (Math.max(sz, ez) << 4) + 15;
        ArrayDeque<int[]> q = new ArrayDeque<int[]>();
        Set<Long> seen = new HashSet<Long>();
        for (int x = sx << 4; x < (sx << 4) + 16; x++)
        {
            for (int z = sz << 4; z < (sz << 4) + 16; z++)
            {
                if (RailTest.stand(w, x, g + 1, z) && seen.add(RailTest.key(x, g + 1, z)))
                {
                    q.add(new int[] {x, g + 1, z});
                }
            }
        }
        int reached = 0;
        int[][] side = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!q.isEmpty())
        {
            int[] c = q.poll();
            if (c[0] >> 4 == ex && c[2] >> 4 == ez)
            {
                reached++;
            }
            for (int[] d : side)
            {
                int nx = c[0] + d[0], nz = c[2] + d[1];
                if (nx < x0 || nx > x1 || nz < z0 || nz > z1)
                {
                    continue;
                }
                for (int dy = 1; dy >= -2; dy--)
                {
                    int ny = c[1] + dy;
                    if (dy == 1 && !RailTest.open(w, c[0], c[1] + 2, c[2]))
                    {
                        continue;
                    }
                    if (RailTest.stand(w, nx, ny, nz) && (dy >= 0 || RailTest.openColumn(w, nx, ny + 2, c[1] + 1, nz)))
                    {
                        if (seen.add(RailTest.key(nx, ny, nz)))
                        {
                            q.add(new int[] {nx, ny, nz});
                        }
                        break;
                    }
                }
            }
        }
        return reached;
    }

    /**
     * First bridge from rx 40 (or -Prx) on row rz whose chunk was never generated and whose span
     * (street chunks on the river along the street) has city street chunks on land at both ends.
     */
    private int[] find(World w)
    {
        long seed = w.getSeed();
        net.minecraft.world.gen.ChunkProviderServer cps = (net.minecraft.world.gen.ChunkProviderServer) w.getChunkProvider();
        int from = rxProp.isEmpty() ? 40 : Integer.parseInt(rxProp);
        for (int rx = from; rx <= from + 300; rx++)
        {
            for (int[] b : CityRivers.bridges(seed, rx, rz))
            {
                if (cps.currentChunkLoader instanceof net.minecraft.world.chunk.storage.AnvilChunkLoader
                    && ((net.minecraft.world.chunk.storage.AnvilChunkLoader) cps.currentChunkLoader).chunkExists(w, b[0], b[1]))
                {
                    continue;
                }
                int dx = b[2] == 0 ? 1 : 0, dz = 1 - dx, a = 1, e = 1;
                while (a < 9 && CityRivers.chunk(seed, b[0] - a * dx, b[1] - a * dz)) a++;
                while (e < 9 && CityRivers.chunk(seed, b[0] + e * dx, b[1] + e * dz)) e++;
                if (!cityStreet(seed, b[0] - a * dx, b[1] - a * dz) || !cityStreet(seed, b[0] + e * dx, b[1] + e * dz))
                {
                    continue; // a span ending at the city edge: its land end is the countryside
                }
                for (int i = b[0] - R - 1; i <= b[0] + R + 1; i++)
                {
                    for (int k = b[1] - R - 1; k <= b[1] + R + 1; k++)
                    {
                        cps.loadChunk(i, k);
                    }
                }
                return b;
            }
        }
        return null;
    }

    private static boolean cityStreet(long seed, int cx, int cz)
    {
        return net.decimation.worldgen.city.LcCity.isCity(seed, Math.floorDiv(cx, 4), Math.floorDiv(cz, 4))
            && net.decimation.worldgen.city.LcCity.streetChunk(seed, cx, cz);
    }
}

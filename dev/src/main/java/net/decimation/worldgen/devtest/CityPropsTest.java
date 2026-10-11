package net.decimation.worldgen.devtest;

import java.util.ArrayList;
import java.util.List;

import net.decimation.worldgen.city.LcCity;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Props of the converted city fitted to their drawn size (city/PropFit, docs/city_engine.md
 * "Props fitted to their drawn size"): generates a city cell whose chunks were never generated
 * (from cell x -Prx, default 40, on row -Prz, default 6; each run moves on), counts what the fit
 * pass made there (vanilla chests from wood crates, ceiling lights from hanging lamps, plank ledges
 * from wall shelves) and photographs examples of each, plus a chair and a street wreck, from a
 * standing spot nearby: shots cityprops_<kind>_<n>.png. The numbers are checked from a dedicated
 * server world (tools/props/worlddump.py + propclash.py); this mode is for the look.
 */
public class CityPropsTest extends DevTestMode
{
    public String name() { return "cityprops"; }

    private static final int BUILD = 10, SETTLE = 40, SHOT = 60, R = 3;

    private final int rz = Integer.parseInt(System.getProperty("deciworldgen.autotest.rz", "6"));
    private final String rxProp = System.getProperty("deciworldgen.autotest.rx", "");
    private final List<float[]> shots = new ArrayList<float[]>();
    private final List<String> names = new ArrayList<String>();
    private volatile int ticks;
    private volatile boolean built;
    private int taken = -1;
    private float gamma = -1;

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
            if (gamma >= 0)
            {
                mc.gameSettings.gammaSetting = gamma;
            }
            return false;
        }
        if (i < 0)
        {
            return true;
        }
        float[] s = shots.get(i);
        if (gamma < 0)
        {
            gamma = mc.gameSettings.gammaSetting;
            mc.gameSettings.gammaSetting = 8.0F;           // unlit rooms: shots for the props, not the mood
        }
        mc.gameSettings.hideGUI = true;
        mc.gameSettings.fovSetting = 70f;
        mc.thePlayer.rotationYaw = mc.thePlayer.prevRotationYaw = s[3];
        mc.thePlayer.rotationPitch = mc.thePlayer.prevRotationPitch = s[4];
        if (u >= SETTLE && taken != i)
        {
            taken = i;
            DevTestUtil.screenshot(mc, name(), "cityprops_" + names.get(i));
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
            int[] c = find(w);
            if (c == null)
            {
                DevTestResults.value(name(), "city cell", "none found");
            }
            else
            {
                survey(w, c[0], c[1]);
            }
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

    /** The first city cell from (rx, rz) on whose middle chunk was never generated; its chunks generated. */
    private int[] find(World w)
    {
        long seed = w.getSeed();
        net.minecraft.world.gen.ChunkProviderServer cps = (net.minecraft.world.gen.ChunkProviderServer) w.getChunkProvider();
        int from = rxProp.isEmpty() ? 40 : Integer.parseInt(rxProp);
        for (int cx = from; cx <= from + 400; cx++)
        {
            if (!LcCity.isCity(seed, cx, rz))
            {
                continue;
            }
            int mx = cx * LcCity.CELL + 2, mz = rz * LcCity.CELL + 2;
            if (cps.currentChunkLoader instanceof net.minecraft.world.chunk.storage.AnvilChunkLoader
                && ((net.minecraft.world.chunk.storage.AnvilChunkLoader) cps.currentChunkLoader).chunkExists(w, mx, mz))
            {
                continue;
            }
            for (int i = mx - R - 1; i <= mx + R + 1; i++)
            {
                for (int k = mz - R - 1; k <= mz + R + 1; k++)
                {
                    cps.loadChunk(i, k);
                }
            }
            DevTestResults.value(name(), "city cell", cx + "," + rz + " (blocks " + (mx << 4) + "," + (mz << 4) + ")");
            return new int[] {mx, mz};
        }
        return null;
    }

    /** What to photograph: block name prefix, label, how many, camera distance (street props further). */
    private static final Object[][] KINDS = {
        {"minecraft:chest", "chest", 1, 3}, {"deci:BlockTrafficLights", "trafficlight", 2, 6},
        {"deci:BlockRoadsignStop", "stopsign", 2, 4}, {"deci:BlockRoadsign", "speedsign", 1, 4},
        {"deci:BlockMailbox", "mailbox", 2, 3}, {"deci:BlockNewsStand", "newsstand", 2, 3},
        {"deci:BlockStreetBench", "bench", 2, 3}, {"deci:BlockBicycle", "bicycle", 1, 3},
        {"deci:BlockPowerPole", "powerpole", 1, 6}, {"deci:BlockExitLight", "exitlight", 2, 3},
        {"deci:BlockCCTV", "cctv", 1, 3}, {"deci:BlockFlatscreenTV", "tv", 2, 3},
        {"deci:BlockVendingMachine", "vending", 2, 3}, {"deci:BlockSkeletonWall", "skeleton", 2, 3},
        {"deci:BlockElectricBoxBin", "cabinet", 1, 3}, {"deci:BlockPhonebooth", "payphone", 1, 3},
        {"deci:BlockWashingMachine", "washer", 1, 3}, {"deci:BlockShopDisplay_1", "shopdisplay", 1, 3},
        {"deci:BlockGraffiti", "graffiti", 1, 3}, {"deci:decal.", "decal", 1, 3},
        {"deci:BlockBodyBag", "bodybag", 1, 3}, {"deci:BlockWreckagePolice", "policecar", 2, 7},
        {"deci:BlockHazardbarrier", "sawhorse", 1, 5}, {"deci:BlockCone", "cone", 1, 5},
        {"deci:BlockMaintenanceScreen", "roadworks", 1, 6}, {"deci:BlockHedgehog", "checkpoint", 1, 6},
        {"deci:BlockTruckWreckage", "truck", 1, 7}, {"deci:BlockWreckage", "wreck", 2, 7},
        {"deci:BlockSign_", "shopsign", 2, 4}, {"deci:BlockChesstable", "chesstable", 1, 3},
        {"deci:BlockCanFire", "camp", 1, 3}, {"deci:BlockDumpster", "dumpster", 1, 4},
        {"deci:BlockPowerPole", "pole", 1, 6}};

    private void survey(World w, int mx, int mz)
    {
        List<int[]> found = new ArrayList<int[]>();
        int[] got = new int[KINDS.length], total = new int[KINDS.length];
        for (int x = (mx - R) << 4; x < (mx + R + 1) << 4; x++)
        {
            for (int z = (mz - R) << 4; z < (mz + R + 1) << 4; z++)
            {
                for (int y = 40; y < 160; y++)
                {
                    Block b = w.getBlock(x, y, z);
                    if (b == Blocks.air)
                    {
                        continue;
                    }
                    String n = String.valueOf(Block.blockRegistry.getNameForObject(b));
                    int k = -1;
                    for (int i = 0; i < KINDS.length && k < 0; i++)
                    {
                        if (n.startsWith((String) KINDS[i][0]))
                        {
                            k = i;
                        }
                    }
                    if (k < 0)
                    {
                        continue;
                    }
                    total[k]++;
                    if (got[k] >= (Integer) KINDS[k][2] || far(found, x, z))
                    {
                        continue;
                    }
                    int front = net.decimation.worldgen.city.PropFacingAccess.front(b, w.getBlockMetadata(x, y, z));
                    float[] cam = camera(w, x, y, z, (Integer) KINDS[k][3], front);
                    if (cam != null)
                    {
                        found.add(new int[] {x, z});
                        got[k]++;
                        shots.add(cam);
                        names.add(KINDS[k][1] + "_" + got[k]);
                        DevTestResults.value(name(), KINDS[k][1] + " " + got[k], x + "," + y + "," + z + " meta "
                            + w.getBlockMetadata(x, y, z) + " front " + (front < 0 ? "-" : "ESWN".charAt(front)));
                    }
                }
            }
        }
        for (int i = 0; i < KINDS.length; i++)
        {
            DevTestResults.value(name(), "count " + KINDS[i][1], total[i]);
        }
    }

    /** Different examples come from different places (another room or building). */
    private static boolean far(List<int[]> found, int x, int z)
    {
        for (int[] f : found)
        {
            if (Math.abs(f[0] - x) < 12 && Math.abs(f[1] - z) < 12)
            {
                return true;
            }
        }
        return false;
    }

    /** A standing spot r (2 .. r + 2) away that sees the target: {eyeX, eyeY, eyeZ, yaw, pitch}. */
    private static float[] camera(World w, int tx, int ty, int tz, int r, int front)
    {
        int[][] base = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
        // in front of the prop first (its face shows if its facing is right), then the rest
        int[][] dirs = new int[8][];
        int n = 0;
        if (front >= 0)
        {
            dirs[n++] = base[front];
            dirs[n++] = new int[] {base[front][0] + base[(front + 1) & 3][0], base[front][1] + base[(front + 1) & 3][1]};
            dirs[n++] = new int[] {base[front][0] + base[(front + 3) & 3][0], base[front][1] + base[(front + 3) & 3][1]};
        }
        for (int[] d : base)
        {
            boolean dup = false;
            for (int k = 0; k < n; k++)
            {
                dup |= dirs[k][0] == d[0] && dirs[k][1] == d[1];
            }
            if (!dup && n < 8)
            {
                dirs[n++] = d;
            }
        }
        for (int d = r; d <= r + 2; d++)
        {
            for (int[] dir : dirs)
            {
                int sx = tx + dir[0] * d, sz = tz + dir[1] * d;
                for (int sy = ty + 1; sy >= ty - 4; sy--)
                {
                    if (!RailTest.stand(w, sx, sy, sz))
                    {
                        continue;
                    }
                    double ex = sx + 0.5, ey = sy + 1.62, ez = sz + 0.5;
                    double gx = tx + 0.5, gy = ty + 0.5, gz = tz + 0.5;
                    MovingObjectPosition hit = w.rayTraceBlocks(Vec3.createVectorHelper(ex, ey, ez),
                                                                Vec3.createVectorHelper(gx, gy, gz));
                    if (hit != null && (Math.abs(hit.blockX - tx) > 1 || Math.abs(hit.blockY - ty) > 1
                                        || Math.abs(hit.blockZ - tz) > 1
                                        || w.getBlock(hit.blockX, hit.blockY, hit.blockZ) instanceof net.minecraft.block.BlockLeaves))
                    {
                        continue;
                    }
                    float yaw = (float) Math.toDegrees(Math.atan2(-(gx - ex), gz - ez));
                    float pitch = (float) -Math.toDegrees(Math.atan2(gy - ey, Math.hypot(gx - ex, gz - ez)));
                    return new float[] {(float) ex, (float) ey, (float) ez, yaw, pitch};
                }
            }
        }
        return null;
    }
}

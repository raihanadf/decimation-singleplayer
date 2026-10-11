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

    private void survey(World w, int mx, int mz)
    {
        Block light = Block.getBlockFromName("deci:BlockLightOff");
        Block chair = Block.getBlockFromName("deci:BlockChair");
        int chests = 0, lights = 0, ledges = 0;
        List<int[]> found = new ArrayList<int[]>();
        String[] kinds = {"chest", "light", "ledge", "chair", "wreck"};
        int[] want = {3, 2, 2, 2, 1}, got = new int[5];
        for (int x = (mx - R) << 4; x < (mx + R + 1) << 4; x++)
        {
            for (int z = (mz - R) << 4; z < (mz + R + 1) << 4; z++)
            {
                for (int y = 40; y < 160; y++)
                {
                    Block b = w.getBlock(x, y, z);
                    int k = -1;
                    if (b == Blocks.chest)
                    {
                        chests++;
                        k = 0;
                    }
                    else if (b == light)
                    {
                        lights++;
                        k = 1;
                    }
                    else if (b == Blocks.wooden_slab && w.getBlockMetadata(x, y, z) == 8 && w.isAirBlock(x, y - 1, z)
                             && !LcCity.streetChunk(w.getSeed(), x >> 4, z >> 4))
                    {
                        ledges++;
                        k = 2;
                    }
                    else if (b == chair)
                    {
                        k = 3;
                    }
                    else if (String.valueOf(Block.blockRegistry.getNameForObject(b)).startsWith("deci:BlockWreckage")
                             && LcCity.streetChunk(w.getSeed(), x >> 4, z >> 4))
                    {
                        k = 4;
                    }
                    if (k < 0 || got[k] >= want[k] || far(found, x, z))
                    {
                        continue;
                    }
                    float[] cam = camera(w, x, y, z, k == 4 ? 7 : 3);
                    if (cam != null)
                    {
                        found.add(new int[] {x, z});
                        got[k]++;
                        shots.add(cam);
                        names.add(kinds[k] + "_" + got[k]);
                        DevTestResults.value(name(), kinds[k] + " " + got[k], x + "," + y + "," + z);
                    }
                }
            }
        }
        DevTestResults.value(name(), "chests (wood crates that fit nowhere)", chests);
        DevTestResults.value(name(), "ceiling lights", lights);
        DevTestResults.value(name(), "plank ledges (wall shelves)", ledges);
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
    private static float[] camera(World w, int tx, int ty, int tz, int r)
    {
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
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
                                        || Math.abs(hit.blockZ - tz) > 1))
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

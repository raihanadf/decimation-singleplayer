package net.decimation.worldgen.devtest;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.decimation.worldgen.city.Rails;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/**
 * Underground railway (city/Rails.java, docs/city_engine.md "Railway"): finds a city station whose
 * chunk this world has never generated (east of rx 40 on region row -Prz, default 6, or from
 * -Prx=N on; the camera's view distance generates chunks too, so each run moves on), generates and populates the chunks around it, then checks the track through the
 * station and its platforms, whether a player can walk from the street down the stairs to the
 * platform, and shoots the entrance, the platform, the stairs and the tunnel. Shots rail_<view>.png.
 */
public class RailTest extends DevTestMode
{
    public String name() { return "rail"; }

    private static final int BUILD = 10, SETTLE = 40, SHOT = 70;

    private final int rz = Integer.parseInt(System.getProperty("deciworldgen.autotest.rz", "6"));
    private final String rxProp = System.getProperty("deciworldgen.autotest.rx", "");
    /** {x, y, z, yaw, pitch} per shot, and its name. */
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
            if (gamma >= 0)
            {
                mc.gameSettings.gammaSetting = gamma;
            }
            mc.gameSettings.hideGUI = false;
            return false;
        }
        if (i < 0)
        {
            return true;
        }
        if (gamma < 0)
        {
            gamma = mc.gameSettings.gammaSetting;
        }
        float[] s = shots.get(i);
        mc.gameSettings.hideGUI = true;
        mc.gameSettings.fovSetting = 80f;
        mc.gameSettings.gammaSetting = 1f;
        mc.thePlayer.rotationYaw = mc.thePlayer.prevRotationYaw = s[3];
        mc.thePlayer.rotationPitch = mc.thePlayer.prevRotationPitch = s[4];
        if (u >= SETTLE && taken != i)
        {
            taken = i;
            DevTestUtil.screenshot(mc, name(), "rail_" + names.get(i));
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
            int[] st = find(w);
            if (st == null)
            {
                DevTestResults.value(name(), "station", "none found");
                built = true;
                return;
            }
            int x = st[0], y = st[1], z = st[2], g = st[3];
            DevTestResults.value(name(), "station", x + "," + y + "," + z + " street " + g);
            // track through the platform, station and platform chunks (48 blocks along x)
            int rails = 0;
            for (int bx = x - 8 - 16; bx < x - 8 + 32; bx++)
            {
                Block b = w.getBlock(bx, y, z);
                rails += b == Blocks.rail || b == Blocks.golden_rail ? 1 : 0;
            }
            DevTestResults.check(name(), "track through the station", rails, rails == 48, "48 rails");
            // springs / lakes must stay off the tunnels (TerrainEvents): no liquid inside along 11 chunks
            int wet = 0;
            for (int bx = x - 8 - 5 * 16; bx < x - 8 + 6 * 16; bx++)
            {
                for (int bz = z - 8; bz < z + 8; bz++)
                {
                    for (int by = y - 1; by < y + 8; by++)
                    {
                        wet += w.getBlock(bx, by, bz).getMaterial().isLiquid() ? 1 : 0;
                    }
                }
            }
            DevTestResults.check(name(), "liquid in the tunnel", wet, wet == 0, "0");
            int down = walk(w, x - 8, z - 8, g, y);
            DevTestResults.check(name(), "street to platform on foot", down, down > 0, "platform cells reached > 0");
            int bx = x - 8, bz = z - 8;
            shot("entrance", bx - 10, g + 8, bz + 8.5f, -90, 25);
            shot("platform", bx - 14, y + 2.62f, bz + 2.5f, -90, 5);
            shot("stairs", bx + 7, y + 2.62f, bz + 5.5f, 180, -35);
            shot("shaft", bx + 6.5f, g + 2.62f, bz + 2.5f, 0, 60);
            shot("tunnel", bx + 72, y + 1.62f, bz + 8.5f, -90, 5);
            ends(w, x >> 4, z >> 4, y);
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

    /**
     * Line ends and the crossing of the station's region (its row and column, one chunk beyond):
     * generated around them, then one shot from inside each end toward its buffer (end parts are
     * turned per side) and one along the crossing's other line.
     */
    private void ends(World w, int scx, int scz, int y)
    {
        long seed = w.getSeed();
        net.minecraft.world.gen.ChunkProviderServer cps = (net.minecraft.world.gen.ChunkProviderServer) w.getChunkProvider();
        int r0 = Math.floorDiv(scx, 16) * 16;
        List<int[]> found = new ArrayList<int[]>();
        for (int i = -1; i <= 16; i++)
        {
            found.add(new int[] {r0 + i, scz});
        }
        int col = -1;
        for (int cx = r0; cx < r0 + 16; cx++)
        {
            if (Rails.kind(seed, cx, scz) == Rails.CROSS)
            {
                col = cx;
            }
        }
        if (col >= 0)
        {
            int z0 = Math.floorDiv(scz, 16) * 16;
            for (int i = -1; i <= 16; i++)
            {
                found.add(new int[] {col, z0 + i});
            }
        }
        int n = 0;
        for (int[] c : found)
        {
            int k = Rails.kind(seed, c[0], c[1]);
            if (k < Rails.END_W || k > Rails.END_S)
            {
                continue;
            }
            for (int a = c[0] - 1; a <= c[0] + 1; a++)
            {
                for (int b = c[1] - 1; b <= c[1] + 1; b++)
                {
                    cps.loadChunk(a, b);
                }
            }
            float cx = (c[0] << 4) + 8.5f, cz = (c[1] << 4) + 8.5f;
            // stand 10 blocks back from the buffer side, looking at it (yaw 0 south, 90 west)
            float[] at = k == Rails.END_E ? new float[] {cx - 10, cz, -90} : k == Rails.END_W ? new float[] {cx + 10, cz, 90}
                : k == Rails.END_S ? new float[] {cx, cz - 10, 0} : new float[] {cx, cz + 10, 180};
            shot("end_" + (++n), at[0], y + 1.62f, at[1], at[2], 5);
            DevTestResults.value(name(), "end " + n, (c[0] << 4) + "," + (c[1] << 4) + " kind " + k);
        }
        if (col >= 0)
        {
            shot("crossing", (col << 4) + 8.5f, y + 1.62f, (scz << 4) + 2.5f, 0, 5);
        }
    }

    private void shot(String n, float x, float eyeY, float z, float yaw, float pitch)
    {
        shots.add(new float[] {x, eyeY, z, yaw, pitch});
        names.add(n);
    }

    /**
     * The first city station from rx 40 (or -Prx) on region row rz whose chunk was never
     * generated: generates and populates the chunks around it. {x, y, z, street} or null.
     */
    private int[] find(World w)
    {
        long seed = w.getSeed();
        net.minecraft.world.gen.ChunkProviderServer cps = (net.minecraft.world.gen.ChunkProviderServer) w.getChunkProvider();
        int from = rxProp.isEmpty() ? 40 : Integer.parseInt(rxProp), to = from + 200;
        for (int rx = from; rx <= to; rx++)
        {
            int[] st = Rails.station(seed, rx, rz);
            if (st == null)
            {
                continue;
            }
            int cx = st[0] >> 4, cz = st[2] >> 4;
            if (cps.currentChunkLoader instanceof net.minecraft.world.chunk.storage.AnvilChunkLoader
                && ((net.minecraft.world.chunk.storage.AnvilChunkLoader) cps.currentChunkLoader).chunkExists(w, cx, cz))
            {
                DevTestResults.value(name(), "skipped (generated before)", rx + "," + rz);
                continue; // an older run (or its camera's view distance) made it, maybe with older code
            }
            for (int a = cx - 5; a <= cx + 5; a++)
            {
                for (int b = cz - 3; b <= cz + 3; b++)
                {
                    cps.loadChunk(a, b); // a chunk is populated once its neighbours exist
                }
            }
            return st;
        }
        return null;
    }

    /**
     * Player walk inside the station chunk: from every standing cell on the street (y = street + 1)
     * down to standing cells at track level + 1 (the platforms are one block up). A cell is open
     * when its block does not block movement; a player stands where two open cells rest on one
     * that does; a move goes to a side neighbour up to 1 higher (a jump) or up to 3 lower.
     * Returns the number of reached standing cells at platform height, 0 when none.
     */
    private static int walk(World w, int x0, int z0, int street, int track)
    {
        ArrayDeque<int[]> q = new ArrayDeque<int[]>();
        Set<Long> seen = new HashSet<Long>();
        for (int x = x0; x < x0 + 16; x++)
        {
            for (int z = z0; z < z0 + 16; z++)
            {
                if (stand(w, x, street + 1, z))
                {
                    q.add(new int[] {x, street + 1, z});
                    seen.add(key(x, street + 1, z));
                }
            }
        }
        int reached = 0;
        int[][] side = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!q.isEmpty())
        {
            int[] c = q.poll();
            if (c[1] == track + 1 || c[1] == track + 2)
            {
                reached++;
            }
            for (int[] d : side)
            {
                int nx = c[0] + d[0], nz = c[2] + d[1];
                if (nx < x0 || nx >= x0 + 16 || nz < z0 || nz >= z0 + 16)
                {
                    continue;
                }
                for (int dy = 1; dy >= -3; dy--)
                {
                    int ny = c[1] + dy;
                    if (ny < track || ny > street + 2)
                    {
                        continue;
                    }
                    // the way there: head room above the current cell for a jump, open cells down for a drop
                    if (dy == 1 && !open(w, c[0], c[1] + 2, c[2]))
                    {
                        continue;
                    }
                    if (stand(w, nx, ny, nz) && (dy >= 0 || openColumn(w, nx, ny + 2, c[1] + 1, nz)))
                    {
                        if (seen.add(key(nx, ny, nz)))
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

    private static boolean openColumn(World w, int x, int y0, int y1, int z)
    {
        for (int y = y0; y <= y1; y++)
        {
            if (!open(w, x, y, z))
            {
                return false;
            }
        }
        return true;
    }

    private static boolean stand(World w, int x, int y, int z)
    {
        return open(w, x, y, z) && open(w, x, y + 1, z) && !open(w, x, y - 1, z);
    }

    private static boolean open(World w, int x, int y, int z)
    {
        return !w.getBlock(x, y, z).getMaterial().blocksMovement();
    }

    private static long key(int x, int y, int z)
    {
        return ((long) x << 38) ^ ((long) (y & 255) << 30) ^ (z & 0x3FFFFFFFL);
    }
}

package net.decimation.worldgen.devtest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.decimation.fixes.Deci;
import net.decimation.fixes.MilitarySpawner;
import net.decimation.fixes.NpcKind;
import net.decimation.fixes.NpcLoadouts;
import net.decimation.worldgen.ZoneKind;
import net.decimation.worldgen.ZoneStore;
import net.decimation.worldgen.city.LcCity;
import net.decimation.worldgen.city.Wasteland;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * The wasteland district as the city's military area (docs/wasteland.md): switches the military
 * building pick on for the session, generates a wasteland district whose chunks were never generated
 * (district origins searched outward from cell -Prx, -Prz, default 40 / 6; each run moves on), then
 * checks the district's MILITARY zone (a street and a building point), that the NPC rules count it as
 * a military area (and a neighbouring district not), how many containers hold military loot, the zone
 * entry checkpoints, and that an enemy military group spawns on its streets (never on a roof).
 * Photographs examples of each new scene (wasteland_<kind>_<n>.png) and the district from above.
 */
public class WastelandTest extends DevTestMode
{
    public String name() { return "wasteland"; }

    private static final int BUILD = 10, SETTLE = 40, SHOT = 60;

    private final int rz = Integer.parseInt(System.getProperty("deciworldgen.autotest.rz", "6"));
    private final String rxProp = System.getProperty("deciworldgen.autotest.rx", "");
    private final List<float[]> shots = new ArrayList<float[]>();
    private final List<String> names = new ArrayList<String>();
    private volatile int ticks;
    private volatile boolean built;
    private int taken = -1;
    private float gamma = -1;
    private boolean layoutBefore;

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
            Wasteland.setLayout(layoutBefore);
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
            mc.gameSettings.gammaSetting = 8.0F;
        }
        mc.gameSettings.hideGUI = true;
        mc.gameSettings.fovSetting = 70f;
        mc.thePlayer.rotationYaw = mc.thePlayer.prevRotationYaw = s[3];
        mc.thePlayer.rotationPitch = mc.thePlayer.prevRotationPitch = s[4];
        if (u >= SETTLE && taken != i)
        {
            taken = i;
            DevTestUtil.screenshot(mc, name(), "wasteland_" + names.get(i));
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
            layoutBefore = Wasteland.layout();
            Wasteland.setLayout(true);
        }
        if (t == BUILD)
        {
            int[] g = find(w);
            if (g == null)
            {
                DevTestResults.value(name(), "district", "none found");
            }
            else
            {
                checks((WorldServer) w, p, g[0], g[1]);
                survey(w, g[0], g[1]);
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

    /** The nearest wasteland district (origin cell gx, gz) from (rx, rz) whose middle chunk was never generated; its chunks generated. */
    private int[] find(World w)
    {
        long seed = w.getSeed();
        net.minecraft.world.gen.ChunkProviderServer cps = (net.minecraft.world.gen.ChunkProviderServer) w.getChunkProvider();
        int sx = Math.floorDiv(rxProp.isEmpty() ? 40 : Integer.parseInt(rxProp), 2) * 2, sz = Math.floorDiv(rz, 2) * 2;
        for (int r = 0; r <= 60; r++)
        {
            for (int i = -r; i <= r; i++)
            {
                for (int j = -r; j <= r; j++)
                {
                    if (Math.max(Math.abs(i), Math.abs(j)) != r)
                    {
                        continue;                         // the ring at r only
                    }
                    int gx = sx + 2 * i, gz = sz + 2 * j;
                    if (!LcCity.isCity(seed, gx, gz) || !LcCity.isCity(seed, gx + 1, gz) || !LcCity.isCity(seed, gx, gz + 1)
                        || !LcCity.isCity(seed, gx + 1, gz + 1) || !LcCity.style(seed, gx, gz).endsWith(":deadzone"))
                    {
                        continue;
                    }
                    int mx = gx * LcCity.CELL + LcCity.CELL, mz = gz * LcCity.CELL + LcCity.CELL;
                    if (cps.currentChunkLoader instanceof net.minecraft.world.chunk.storage.AnvilChunkLoader
                        && ((net.minecraft.world.chunk.storage.AnvilChunkLoader) cps.currentChunkLoader).chunkExists(w, mx, mz))
                    {
                        continue;
                    }
                    int c0x = gx * LcCity.CELL, c0z = gz * LcCity.CELL, n = 2 * LcCity.CELL;
                    for (int a = c0x - 2; a < c0x + n + 2; a++)
                    {
                        for (int b = c0z - 2; b < c0z + n + 2; b++)
                        {
                            cps.loadChunk(a, b);
                        }
                    }
                    DevTestResults.value(name(), "district", gx + "," + gz + " (blocks " + (c0x << 4) + "," + (c0z << 4)
                        + " to " + ((c0x + n) * 16 - 1) + "," + ((c0z + n) * 16 - 1) + ")");
                    return new int[] {gx, gz};
                }
            }
        }
        return null;
    }

    private void checks(WorldServer w, EntityPlayerMP p, int gx, int gz)
    {
        long seed = w.getSeed();
        int x0 = gx * LcCity.CELL * 16, z0 = gz * LcCity.CELL * 16, g = LcCity.ground(seed, gx, gz);
        // the zone: on the crossing of the origin cell (a street) and in the middle of its first building chunk
        boolean street = ZoneStore.has(ZoneKind.MILITARY, x0 + 8, g + 1, z0 + 8);
        boolean inside = ZoneStore.has(ZoneKind.MILITARY, x0 + 40, g + 3, z0 + 40);
        DevTestResults.check(name(), "military zone over the district", street + " street, " + inside + " building",
                             street && inside, "true, true");
        boolean mil = NpcLoadouts.military(w, x0 + 64, z0 + 64);
        DevTestResults.check(name(), "NPC rules: military area", mil, mil, "true");
        // a neighbouring city district that is not the wasteland: not a military area
        int[][] nb = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}};
        for (int[] o : nb)
        {
            int cx = gx + o[0], cz = gz + o[1];
            if (LcCity.isCity(seed, cx, cz) && !LcCity.style(seed, cx, cz).endsWith(":deadzone"))
            {
                boolean other = NpcLoadouts.military(w, cx * LcCity.CELL * 16 + 64, cz * LcCity.CELL * 16 + 64);
                DevTestResults.check(name(), "NPC rules: next district (" + LcCity.style(seed, cx, cz) + ")", other, !other, "false");
                break;
            }
        }
        // an enemy military group spawned from the district's middle crossing (24 to 48 away: still in the
        // district): every NPC of THAT group on a street (Soviets already about are not counted)
        p.setPositionAndUpdate(x0 + 64 + 8.5, g + 1, z0 + 64 + 8.5);
        AxisAlignedBB around = AxisAlignedBB.getBoundingBox(p.posX - 64, 0, p.posZ - 64, p.posX + 64, 256, p.posZ + 64);
        List<Object> before = new ArrayList<Object>(w.getEntitiesWithinAABB(EntityLiving.class, around));
        MilitarySpawner spawner = new MilitarySpawner(new File(Minecraft.getMinecraft().mcDataDir, "config"));
        boolean placed = spawner.spawnGroup(w, p);
        int onStreet = 0, total = 0;
        for (Object o : w.getEntitiesWithinAABB(EntityLiving.class, around))
        {
            EntityLiving e = (EntityLiving) o;
            if (Deci.npcKind(e) == NpcKind.SOVIET)
            {
                if (!before.contains(e))
                {
                    total++;
                    onStreet += Wasteland.streetSpot(seed, (int) Math.floor(e.posX), (int) Math.floor(e.posY),
                                                     (int) Math.floor(e.posZ)) && Wasteland.at(seed, e.posX, e.posZ) ? 1 : 0;
                }
                e.setDead();                              // the shots follow: no fight with the camera
            }
        }
        DevTestResults.check(name(), "enemy military group on the district's streets", placed + ", " + onStreet + " of " + total,
                             placed && total > 0 && onStreet == total, "true, all on a street of the district");
    }

    /** What to photograph: block name prefix, label, how many, camera distance. */
    private static final Object[][] KINDS = {
        {"deci:BlockHedgehog", "entry", 2, 7}, {"deci:BlockWreckageMilitary1", "jeep", 2, 7},
        {"deci:BlockWreckageMilitary2", "apc", 1, 8}, {"deci:BlockWreckageMilitary3", "helicopter", 1, 10},
        {"deci:BlockMilitaryCrate", "milcrate", 3, 3}, {"deci:BlockAmmoCrate", "ammo", 1, 3},
        {"deci:BlockWeaponCabinet", "cabinet", 1, 3}, {"deci:BlockMedicalCrate", "medcrate", 1, 3},
        {"deci:BlockSandbagStack", "sandbags", 2, 5}, {"deci:BlockConcertinaWire", "wire", 1, 4},
        {"deci:BlockCarePackage", "supplies", 1, 6}, {"deci:BlockBodyBag", "casualties", 1, 5},
        {"deci:BlockSpotlight", "spotlight", 1, 5}, {"deci:decal.warningtrespass", "trespass", 1, 3},
        {"deci:decal.usflag", "flag", 1, 4}};

    private void survey(World w, int gx, int gz)
    {
        long seed = w.getSeed();
        int x0 = gx * LcCity.CELL * 16, z0 = gz * LcCity.CELL * 16, size = 2 * LcCity.CELL * 16;
        int g = LcCity.ground(seed, gx, gz);
        List<int[]> found = new ArrayList<int[]>();
        int[] got = new int[KINDS.length], total = new int[KINDS.length];
        int wood = 0, chests = 0;
        for (int x = x0; x < x0 + size; x++)
        {
            for (int z = z0; z < z0 + size; z++)
            {
                for (int y = Math.max(4, g - 30); y < g + 60; y++)
                {
                    Block b = w.getBlock(x, y, z);
                    if (b == Blocks.air)
                    {
                        continue;
                    }
                    if (b == Blocks.chest)
                    {
                        chests++;
                        continue;
                    }
                    String n = String.valueOf(Block.blockRegistry.getNameForObject(b));
                    if (n.equals("deci:BlockWoodCrate"))
                    {
                        wood++;
                        continue;
                    }
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
                    if (got[k] >= (Integer) KINDS[k][2] || CityPropsTest.far(found, x, z))
                    {
                        continue;
                    }
                    int front = net.decimation.worldgen.city.PropFacingAccess.front(b, w.getBlockMetadata(x, y, z));
                    float[] cam = CityPropsTest.camera(w, x, y, z, (Integer) KINDS[k][3], front);
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
        // the district from above last: by then the client has its chunks (first, it showed sky)
        shots.add(new float[] {x0 + size / 2f, g + 80, z0 + size / 2f, 180, 90});
        names.add("aerial");
        for (int i = 0; i < KINDS.length; i++)
        {
            DevTestResults.value(name(), "count " + KINDS[i][1], total[i]);
        }
        DevTestResults.value(name(), "count wood crates left", wood);
        DevTestResults.value(name(), "count vanilla chests left", chests);
        int loot = total[4] + total[5] + total[6] + total[7];
        DevTestResults.check(name(), "military loot containers", loot, loot >= 10, "10 or more");
        DevTestResults.check(name(), "zone entry checkpoints", total[0], total[0] > 0, "hedgehogs at an entry");
    }
}

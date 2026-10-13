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
    private boolean layoutBefore, apartBefore, sectorsBefore;

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
            Wasteland.setApart(apartBefore);
            Wasteland.setSectors(sectorsBefore);
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
            apartBefore = Wasteland.apart();
            Wasteland.setApart(true);
            sectorsBefore = Wasteland.sectors();
            Wasteland.setSectors(true);   // v0.57.1: whole sector wasteland cities (this world is older)
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
                bunkers(w, g[0], g[1]);
                fence(w, g[0], g[1]);
                lots(w, p);
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
                    boolean st = Wasteland.streetSpot(seed, (int) Math.floor(e.posX), (int) Math.floor(e.posY), (int) Math.floor(e.posZ));
                    boolean in = Wasteland.at(seed, e.posX, e.posZ);
                    onStreet += st && in ? 1 : 0;
                    DevTestResults.value(name(), "group member " + total, String.format("%.1f,%.1f,%.1f street %s district %s",
                                         e.posX, e.posY, e.posZ, st, in));
                }
                e.setDead();                              // the shots follow: no fight with the camera
            }
        }
        DevTestResults.check(name(), "enemy military group on the district's open ground", placed + ", " + onStreet + " of " + total,
                             placed && total > 0 && onStreet == total, "true, all at street level in the district");
    }

    /**
     * The four military lot scenes, forced (Wasteland.previewLot) side by side in the sky far from
     * everything (x, z -21000, y 200; the shared arena had other tests' builds around it): generation
     * may not have produced each one in the district; every scene must place its key prop.
     */
    private void lots(World w, EntityPlayerMP p)
    {
        String[] scene = {"motorpool", "supplies", "helicopter", "casualties"};
        String[][] key = {{"deci:BlockWreckageMilitary1", "deci:BlockWreckageMilitary2"}, {"deci:BlockMilitaryCrate"},
                          {"deci:BlockWreckageMilitary3"}, {"deci:BlockBodyBag"}};
        int y0 = 200, base = -21000;
        net.minecraft.world.gen.ChunkProviderServer cps = (net.minecraft.world.gen.ChunkProviderServer) w.getChunkProvider();
        for (int k = 0; k < 4; k++)
        {
            int x0 = base + k * 24, z0 = base;
            for (int a = (x0 >> 4) - 1; a <= (x0 >> 4) + 1; a++)
            {
                for (int b = (z0 >> 4) - 1; b <= (z0 >> 4) + 2; b++)
                {
                    cps.loadChunk(a, b);
                }
            }
            Wasteland.previewLot(w, x0, y0, z0, k, w.getSeed() + k);
            int props = 0, hits = 0;
            for (int x = x0; x < x0 + 16; x++)
            {
                for (int z = z0; z < z0 + 16; z++)
                {
                    for (int y = y0 + 1; y < y0 + 5; y++)
                    {
                        String n = String.valueOf(Block.blockRegistry.getNameForObject(w.getBlock(x, y, z)));
                        if (n.startsWith("deci:"))
                        {
                            props++;
                            for (String kk : key[k])
                            {
                                hits += n.startsWith(kk) ? 1 : 0;
                            }
                        }
                    }
                }
            }
            DevTestResults.check(name(), "lot " + scene[k], props + " props, " + hits + " key", hits > 0, "its key prop placed");
            // from the south, 11 up, looking down at the lot's middle; the first one twice (the client
            // is still loading the sky chunks right after the teleport)
            float[] cam = {x0 + 8.5f, y0 + 11, z0 + 22.5f, 180, 33};
            if (k == 0)
            {
                shots.add(cam);
                names.add("lot_warmup");
            }
            shots.add(cam);
            names.add("lot_" + scene[k]);
        }
    }

    /**
     * v0.56: the district's bunkers (BuildingDress.bunker, worldgen/Garrisons): each has a garrison
     * registered; the first one is manned now (juggernauts and elite military on its posts), 4 infected
     * spawned beside its spawner cage must wear the military outfit; shots of its blockhouse (keycard
     * screen), its spawner and a garrison post.
     */
    private void bunkers(World w, int gx, int gz)
    {
        int x0 = gx * LcCity.CELL * 16, z0 = gz * LcCity.CELL * 16, size = 2 * LcCity.CELL * 16;
        int found = 0, manned = 0, military = 0, tried = 0;
        for (String id : net.decimation.worldgen.Garrisons.ids(w))
        {
            int[] v = net.decimation.worldgen.StructureData.get(w).list(id);
            int first = 2 + 3 * v[1];
            if (v.length < first + 3 || v[first] < x0 || v[first] >= x0 + size || v[first + 2] < z0 || v[first + 2] >= z0 + size)
            {
                continue;
            }
            if (++found > 1)
            {
                continue;
            }
            int px = v[first], py = v[first + 1], pz = v[first + 2];
            manned = net.decimation.worldgen.Garrisons.manNow(w, id);
            if (v[1] > 0)
            {
                int sx = v[2], sy = v[3], sz = v[4];
                for (int[] o : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}})
                {
                    int x = sx + o[0], z = sz + o[1];
                    if (tried < 4 && w.isAirBlock(x, sy, z) && w.isAirBlock(x, sy + 1, z))
                    {
                        EntityLiving inf = Deci.newInfected(w);
                        inf.setLocationAndAngles(x + 0.5, sy, z + 0.5, 0, 0);
                        if (w.spawnEntityInWorld(inf))
                        {
                            tried++;
                            military += Deci.infectedVariant(inf) == 1 ? 1 : 0;
                        }
                    }
                }
                float[] cam = CityPropsTest.camera(w, sx, sy, sz, 2, -1);
                if (cam != null)
                {
                    shots.add(cam);
                    names.add("bunker_spawner");
                }
            }
            float[] cam = CityPropsTest.camera(w, px, py, pz, 3, -1);
            if (cam != null)
            {
                shots.add(cam);
                names.add("bunker_garrison");
            }
            // the blockhouse: its keycard screen in the bunker's chunk
            Block screen = Block.getBlockFromName("deci:BlockKeycardScreenMilitary");
            search:
            for (int x = (px >> 4) << 4; x < ((px >> 4) << 4) + 16; x++)
            {
                for (int z = (pz >> 4) << 4; z < ((pz >> 4) << 4) + 16; z++)
                {
                    for (int y = py; y < py + 16; y++)
                    {
                        if (w.getBlock(x, y, z) == screen)
                        {
                            int fr = net.decimation.worldgen.city.PropFacingAccess.front(screen, w.getBlockMetadata(x, y, z));
                            float[] c = CityPropsTest.camera(w, x, y, z, 7, fr);
                            if (c != null)
                            {
                                shots.add(c);
                                names.add("bunker_blockhouse");
                            }
                            DevTestResults.value(name(), "bunker blockhouse screen", x + "," + y + "," + z);
                            break search;
                        }
                    }
                }
            }
        }
        if (found == 0)
        {
            DevTestResults.value(name(), "bunkers", "none in this district (a bunker is a random pick)");
            return;
        }
        DevTestResults.check(name(), "bunkers with a garrison, NPCs manned", found + ", " + manned, manned >= 4,
                             "4 or 5 NPCs");
        DevTestResults.check(name(), "spawner infected in the military outfit", military + " of " + tried, tried > 0 && military == tried,
                             "all");
    }

    /**
     * v0.56: the fence round the wasteland city standing apart (city/WastelandFence): chain link and
     * barbed wire, a gate (striped barriers across a street), landmines outside; shots of the fence and a
     * gate. Only sides facing open land: a district whose neighbour districts were generated before
     * (this world is older than the ring) has no fence there.
     */
    private void fence(World w, int gx, int gz)
    {
        int x0 = gx * LcCity.CELL * 16, z0 = gz * LcCity.CELL * 16, size = 2 * LcCity.CELL * 16;
        int g = LcCity.ground(w.getSeed(), gx, gz);
        Block wire = Block.getBlockFromName("deci:BlockConcertinaWire"), bar = Block.getBlockFromName("deci:BlockHazardbarrier");
        int bars = 0, wires = 0, barriers = 0;
        float[] fenceCam = null, gateCam = null;
        for (int x = x0 - 8; x < x0 + size + 8; x++)
        {
            for (int z = z0 - 8; z < z0 + size + 8; z++)
            {
                if (x >= x0 && x < x0 + size && z >= z0 && z < z0 + size)
                {
                    continue;
                }
                for (int y = g - 8; y < g + 12; y++)
                {
                    Block b = w.getBlock(x, y, z);
                    if (b == Blocks.iron_bars)
                    {
                        bars++;
                    }
                    else if (b == wire && w.getBlock(x, y - 1, z) == Blocks.iron_bars)
                    {
                        wires++;
                        if (fenceCam == null && wires > 20)
                        {
                            fenceCam = CityPropsTest.camera(w, x, y - 2, z, 7, -1);
                        }
                    }
                    else if (b == bar)
                    {
                        barriers++;
                        if (gateCam == null)
                        {
                            gateCam = CityPropsTest.camera(w, x, y, z, 9, -1);
                        }
                    }
                }
            }
        }
        if (fenceCam != null)
        {
            shots.add(fenceCam);
            names.add("fence");
        }
        if (gateCam != null)
        {
            shots.add(gateCam);
            names.add("gate");
        }
        int mines = 0;
        for (Object o : w.loadedEntityList)
        {
            net.minecraft.entity.Entity e = (net.minecraft.entity.Entity) o;
            if ("deci.Landmine".equals(net.minecraft.entity.EntityList.getEntityString(e)) && e.posX > x0 - 24
                && e.posX < x0 + size + 24 && e.posZ > z0 - 24 && e.posZ < z0 + size + 24)
            {
                mines++;
            }
        }
        DevTestResults.value(name(), "fence iron bars, wire on top, gate barriers", bars + ", " + wires + ", " + barriers);
        DevTestResults.check(name(), "fence round the district", wires + " wire, " + mines + " mines",
                             wires > 20, "a wired fence (landmines only where the sector meets open land)");
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
        {"deci:decal.usflag", "flag", 1, 4}, {"deci:Door_Emergency_1_Locked", "locked", 2, 4},
        {"deci:BlockKeycardScreenMilitary", "keycard", 1, 3}};

    private void survey(World w, int gx, int gz)
    {
        long seed = w.getSeed();
        int x0 = gx * LcCity.CELL * 16, z0 = gz * LcCity.CELL * 16, size = 2 * LcCity.CELL * 16;
        int g = LcCity.ground(seed, gx, gz);
        List<int[]> found = new ArrayList<int[]>();
        int[] got = new int[KINDS.length], total = new int[KINDS.length];
        int wood = 0, chests = 0, vanillaDoors = 0;
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
                    if (b == Blocks.wooden_door || b == Blocks.iron_door)
                    {
                        vanillaDoors++;
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
        // v0.55: most buildings locked (Door_Emergency_1_Locked, both halves counted), keycard screens beside
        // them, no vanilla door anywhere
        int locked = total[KINDS.length - 2] / 2, screens = total[KINDS.length - 1];
        DevTestResults.check(name(), "locked doors, keycard screens", locked + ", " + screens, locked > 0 && screens > 0,
                             "both present");
        DevTestResults.check(name(), "vanilla doors", vanillaDoors, vanillaDoors == 0, "0 (Decimation doors only)");
    }
}

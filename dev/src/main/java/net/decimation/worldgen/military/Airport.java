package net.decimation.worldgen.military;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/**
 * Military airport in a wasteland city (docs/airport.md; user, 14 Oktober 2026: half a wasteland sector, planes
 * built from blocks, held by the enemy military but half wrecked, bunker level loot in the HQ and armory; second
 * pass after "to basic": the real airfield's details from the references of docs/airport.md section 1).
 * Canvas W x H x L: x across (0 west .. 111), z along the runway (0 north, the entry side .. 239), y 0 = the ground
 * surface. Across: perimeter, runway strip, infield, taxiway, apron, landside road, landside. Every module reserves
 * its rectangle (a clash with another is logged and fails the dev test, as at the FOB); props are checked by
 * Canvas.validateProps.
 */
public final class Airport
{
    public static final int W = 112, L = 240, H = 30;
    static final int E = 2, S = 3, WE = 4, N = 5;
    static final int RWY0 = 8, RWY1 = 21, TWY0 = 34, TWY1 = 41, APRON0 = 42, APRON1 = 71, ROAD0 = 72, ROAD1 = 77, LAND0 = 80;
    static final int[] STUBS = {40, 116, 196};

    final Canvas c = new Canvas(W, H, L);
    final List<String> log = new ArrayList<String>();
    final Map<String, float[]> poi = new LinkedHashMap<String, float[]>();
    /** Garrison posts (x, y, z) and spawner cells, canvas coordinates. */
    final List<int[]> posts = new ArrayList<int[]>(), spawners = new ArrayList<int[]>();
    private final Random r;
    private final byte[] occ = new byte[W * L];
    private final Block metal, metalGreen, concrete, concreteDark, road, line, sand, sandBeige, hesco, wire,
        locked, keycard, door, clay = Blocks.stained_hardened_clay, paint = Blocks.wool;   // aircraft paint: grey clay reads pink-brown

    Airport(long seed)
    {
        r = new Random(seed ^ 0x41495250L);
        metal = d("BlockMetal_2", Blocks.iron_block);
        metalGreen = d("BlockMetalWall", Blocks.iron_block);
        concrete = d("BlockStone_7", Blocks.stone);
        concreteDark = d("BlockStone_6", Blocks.stone);
        road = d("BlockRoad", Blocks.stone);
        line = d("BlockRoad_CenterLine", null);
        sand = d("BlockSandbagStack", Blocks.sandstone);
        sandBeige = d("BlockSandbagStackBeige", Blocks.sandstone);
        hesco = BlockHesco.instance != null ? BlockHesco.instance : Blocks.sandstone;
        wire = d("BlockConcertinaWire", null);
        locked = d("Door_Emergency_1_Locked", Blocks.iron_door);
        keycard = d("BlockKeycardScreenMilitary", null);
        door = d("Door_Metal_3", Blocks.iron_door);
    }

    // ================================================================ helpers

    private static Block d(String name, Block fallback)
    {
        Block b = Block.getBlockFromName("deci:" + name);
        return b == null || b == Blocks.air ? fallback : b;
    }

    private void prop(int x, int y, int z, String name, int facing)
    {
        Block b = d(name, null);
        if (b != null)
        {
            c.prop(x, y, z, b, facing);
        }
    }

    private void plain(int x, int y, int z, Block b)
    {
        c.set(x, y, z, b, 0, Canvas.PLAIN);
    }

    private void colour(int x, int y, int z, Block b, int m)
    {
        c.set(x, y, z, b, m, Canvas.PLAIN);                     // wool / clay / glass colours never turn
    }

    private void door(int x, int y, int z, Block b, int front)
    {
        int m = front == S ? 3 : front == N ? 1 : front == E ? 2 : 0;
        c.set(x, y, z, b, m, Canvas.DOOR);
        c.set(x, y + 1, z, b, 8, Canvas.DOOR);
    }

    /**
     * Reserves a module's rectangle; false (and a "refused" log line naming the first taken cell) when another
     * module holds part of it: overlaps are loud (FOB lesson 13), the dev test fails on any log line.
     */
    private boolean reserve(String name, int x0, int z0, int x1, int z1)
    {
        for (int x = x0; x <= x1; x++)
        {
            for (int z = z0; z <= z1; z++)
            {
                if (x < 0 || z < 0 || x >= W || z >= L || occ[x + z * W] != 0)
                {
                    log.add("refused " + name + " at " + x + "," + z);
                    return false;
                }
            }
        }
        for (int x = x0; x <= x1; x++)
        {
            for (int z = z0; z <= z1; z++)
            {
                occ[x + z * W] = 1;
            }
        }
        return true;
    }

    private double noise(int x, int z)
    {
        long h = (x * 73856093L) ^ (z * 19349663L) ^ 0x5DEECE66DL;
        h ^= h >>> 13;
        h *= 0x2545F4914F6CDD1DL;
        double a = ((h >>> 11) & 0xFFFF) / 65535.0;
        long g = ((x / 3) * 83492791L) ^ ((z / 3) * 297121507L);
        g ^= g >>> 17;
        g *= 0x9E3779B97F4A7C15L;
        return a * 0.35 + ((g >>> 20) & 0xFFFF) / 65535.0 * 0.65;
    }

    private void poi(String name, float x, float y, float z, float yaw, float pitch)
    {
        poi.put(name, new float[] {x, y, z, yaw, pitch});
    }

    /** A walled box x0..x1, z0..z1, walls h - 1 high, flat roof at h, concrete floor. */
    private void box(int x0, int z0, int x1, int z1, int h, Block wall)
    {
        for (int x = x0; x <= x1; x++)
        {
            for (int z = z0; z <= z1; z++)
            {
                plain(x, 0, z, concrete);
                boolean ring = x == x0 || x == x1 || z == z0 || z == z1;
                if (ring)
                {
                    for (int y = 1; y < h; y++)
                    {
                        plain(x, y, z, wall);
                    }
                }
                plain(x, h, z, Blocks.double_stone_slab);
            }
        }
    }

    /** Ceiling lights every 4 in the cells under the roof at y, where nothing stands. */
    private void lights(int x0, int z0, int x1, int z1, int y)
    {
        for (int x = x0 + 2; x <= x1 - 2; x += 4)
        {
            for (int z = z0 + 2; z <= z1 - 2; z += 4)
            {
                if (c.get(x, y, z) == null && c.get(x, y - 1, z) == null)
                {
                    prop(x, y, z, "BlockLight", S);
                }
            }
        }
    }

    /** Barred windows every 3 at height 2 on a wall line (concrete cells only). */
    private void windows(int x0, int z0, int x1, int z1)
    {
        for (int x = x0; x <= x1; x++)
        {
            for (int z = z0; z <= z1; z++)
            {
                if (((x + z) % 3) == 0 && c.get(x, 2, z) == concrete && c.get(x, 1, z) == concrete)
                {
                    plain(x, 2, z, Blocks.iron_bars);
                }
            }
        }
    }

    // ================================================================ build order

    void build()
    {
        ground();
        perimeter();
        entry();
        runway();
        infield();
        taxiway();
        apron();
        landsideRoad();
        fuelFarm();
        shelter(50, 0);
        shelter(78, 1);
        hangar(106);
        fireStation();
        radar();
        tower();
        hq();
        barracks();
        munitions();
        damage();
        c.validateProps(log);
    }

    /** Gravel with rare packed dirt (Decimation's dirt reads orange), grass on the infield and the runway strip. */
    private void ground()
    {
        for (int z = 0; z < L; z++)
        {
            for (int x = 0; x < W; x++)
            {
                double n = noise(x, z);
                boolean grassy = x >= 4 && x <= 33;
                Block g = grassy ? (n > 0.25 ? Blocks.grass : Blocks.gravel) : n > 0.93 ? Blocks.dirt : Blocks.gravel;
                plain(x, 0, z, g);
            }
        }
    }

    // ================================================================ perimeter and entry

    /**
     * Chain link 3 high on the outermost row (posts every 4, wire on top), a broken concertina ring 2 inside on the
     * landside (the airside has the runway), HESCO guard towers at the four corners.
     */
    private void perimeter()
    {
        for (int i = 0; i < W; i++)
        {
            fence(i, 0);
            fence(i, L - 1);
        }
        for (int i = 1; i < L - 1; i++)
        {
            fence(0, i);
            fence(W - 1, i);
        }
        guardTower(1, 1, true, "tower_nw");
        guardTower(W - 5, L - 5, false, "tower_se");
        guardTower(1, L - 5, false, "tower_sw");
        for (int z = 50; z < 200; z++)
        {
            if (wire != null && noise(W, z) > 0.2)
            {
                c.prop(W - 2, 1, z, wire, S);
            }
        }
    }

    private void fence(int x, int z)
    {
        boolean post = ((x + z) & 3) == 0;
        for (int y = 1; y <= 3; y++)
        {
            plain(x, y, z, post ? metal : Blocks.iron_bars);
        }
        if (post)
        {
            plain(x, 4, z, metal);
        }
        else if (wire != null)
        {
            c.prop(x, 4, z, wire, S);
        }
    }

    /**
     * HESCO tower 4 x 4 (as the FOB's): HESCO base 3 high, plank deck at 4 with the ladder hatch, sandbag parapet with
     * firing gaps, corner posts, a slab roof at 8, a radio and an ammo crate on the deck, a guard post. The ladder
     * runs up the inner face (south for the north towers) in a shaft cut into the HESCO, the cell in front free.
     */
    private void guardTower(int x0, int z0, boolean north, String name)
    {
        if (!reserve(name, x0, z0, x0 + 3, z0 + 4))
        {
            return;
        }
        for (int x = x0; x <= x0 + 3; x++)
        {
            for (int z = z0; z <= z0 + 3; z++)
            {
                boolean edge = x == x0 || x == x0 + 3 || z == z0 || z == z0 + 3;
                boolean corner = (x == x0 || x == x0 + 3) && (z == z0 || z == z0 + 3);
                for (int y = 1; y <= 3; y++)
                {
                    plain(x, y, z, hesco);
                }
                c.set(x, 4, z, Blocks.planks, 1);
                if (edge && !corner && ((x + z) & 1) == 0)
                {
                    plain(x, 5, z, sand);
                }
                if (corner)
                {
                    for (int y = 5; y <= 7; y++)
                    {
                        c.set(x, y, z, Blocks.fence, 0);
                    }
                }
                c.set(x, 8, z, Blocks.wooden_slab, 1);
            }
        }
        int lx = x0 + 1, lz = north ? z0 + 3 : z0;
        int out = north ? 1 : -1;
        for (int y = 1; y <= 4; y++)
        {
            c.clear(lx, y, lz);
            c.set(lx, y, lz, Blocks.ladder, north ? 2 : 3);
        }
        c.clear(lx, 5, lz);
        for (int y = 1; y <= 2; y++)
        {
            c.clear(lx, y, lz + out);
        }
        prop(x0 + 2, 5, north ? z0 + 1 : z0 + 2, "BlockMilitaryRadioSmall", north ? S : N);   // toward the hatch
        prop(x0 + 1, 5, north ? z0 + 1 : z0 + 2, "BlockMilitaryCrate", north ? S : N);         // 1 x 1, an ammo case is 1.6 long
        posts.add(new int[] {x0 + 2, 5, north ? z0 + 2 : z0 + 1});
    }

    /**
     * Entry control point on the north side over the landside road (FOB style): the gate in the fence (x 70..79)
     * between tall posts with a drop arm and its red lamp, a serpentine of jersey barriers on the road, sawhorses,
     * the guard booth west of it (sandbags, plank roof, radio, chair), an overwatch HESCO tower east of it with a
     * searchlight, the vehicle search pad (a jeep being searched, a light tower, drums, a cone), a sign, the flag.
     */
    private void entry()
    {
        reserve("ecp road", 70, 0, 79, 18);
        for (int x = 70; x <= 79; x++)
        {
            for (int y = 1; y <= 4; y++)
            {
                c.clear(x, y, 0);
            }
            for (int z = 0; z <= 2; z++)
            {
                plain(x, 0, z, road);
            }
        }
        for (int y = 1; y <= 5; y++)
        {
            plain(69, y, 0, metal);
            plain(80, y, 0, metal);
        }
        Block boom = BlockBoomArm.instance;
        if (boom != null)
        {
            for (int x = 72; x <= 77; x++)
            {
                c.set(x, 1, 3, boom, 0, Canvas.PLAIN);
            }
        }
        plain(71, 1, 3, metal);
        prop(71, 2, 3, "BlockRedlight", N);
        for (int k = 0; k < 4; k++)
        {
            prop((k & 1) == 0 ? 72 : 75, 1, 6 + k * 3, "BlockBarrier", S);
        }
        prop(73, 1, 17, "BlockHazardbarrier", S);
        prop(76, 1, 17, "BlockHazardbarrier", S);
        if (reserve("ecp booth", 63, 3, 68, 8))
        {
            for (int x = 64; x <= 67; x++)
            {
                for (int z = 4; z <= 7; z++)
                {
                    boolean ring = x == 64 || x == 67 || z == 4 || z == 7;
                    boolean doorway = x == 65 && z == 7, window = x == 67 && (z == 5 || z == 6);
                    if (ring && !doorway)
                    {
                        plain(x, 1, z, sandBeige);
                        if (!window)
                        {
                            plain(x, 2, z, sandBeige);
                        }
                        plain(x, 3, z, sandBeige);
                    }
                    c.set(x, 4, z, Blocks.wooden_slab, 1);
                }
            }
            prop(66, 1, 5, "BlockMilitaryRadioSmall", E);
            prop(65, 1, 5, "BlockChair", E);
            posts.add(new int[] {66, 1, 6});
        }
        guardTower(83, 3, true, "ecp overwatch");
        prop(84, 9, 4, "BlockSpotlight", N);
        if (reserve("search pad", 89, 3, 106, 12))
        {
            for (int x = 89; x <= 106; x++)
            {
                for (int z = 3; z <= 10; z++)
                {
                    plain(x, 0, z, concreteDark);
                }
            }
            for (int x = 90; x <= 104; x += 2)
            {
                prop(x, 1, 12, "BlockBarrier", S);
            }
            prop(96, 1, 6, "BlockWreckageMilitary1", E);
            prop(103, 1, 4, "BlockHazardLight", S);
            prop(90, 1, 4, "BlockBarrel", S);
            prop(91, 1, 4, "BlockBarrel", S);
            prop(105, 1, 9, "BlockCone", S);
        }
        prop(68, 1, 12, "BlockRussianRoadSignWarning", S);
        prop(69, 1, 16, "BlockFlagPollUAHD", S);
        poi("ecp", 74.5F, 3, 26, 180, 10);
        poi("main_road", 74.5F, 1, 22, 180, 10);                  // walkcheck starts here
    }

    // ================================================================ airside

    /**
     * Runway x 8..21, z 10..229 with concrete shoulders: edge lines, threshold bars, runway numbers (18 north, 36
     * south, readable from the approach), touchdown zone bars, aiming point blocks, a dashed centreline; lights: white
     * edge lights every 12 on the shoulders, a green threshold bar and a red end bar, approach light bars on posts
     * beyond both thresholds; PAPI boxes beside the aiming points; arresting gear (absorber housings both sides, the
     * pendant cable across) 24 in from each threshold.
     */
    private void runway()
    {
        reserve("runway strip", 4, 6, 25, L - 6);
        for (int z = 10; z <= 229; z++)
        {
            for (int x = RWY0 - 2; x <= RWY1 + 2; x++)
            {
                if (x < RWY0 || x > RWY1)
                {
                    plain(x, 0, z, z % 12 == 0 && (x == RWY0 - 2 || x == RWY1 + 2) ? Blocks.glowstone : concrete);
                    continue;
                }
                boolean edge = x == RWY0 || x == RWY1;
                boolean threshold = (z <= 15 || z >= 224) && x != 14 && x != 15 && (x - RWY0) % 2 == 1;
                boolean centre = (x == 14 || x == 15) && z > 30 && z < 209 && z % 9 < 5;
                boolean touch = (x == 10 || x == 11 || x == 18 || x == 19) && ((z >= 32 && z <= 35) || (z >= 44 && z <= 47)
                    || (z >= 192 && z <= 195) || (z >= 204 && z <= 207));
                boolean aim = (x >= 10 && x <= 12 || x >= 17 && x <= 19) && (z >= 56 && z <= 63 || z >= 176 && z <= 183);
                if (edge || threshold || centre || touch || aim)
                {
                    colour(x, 0, z, paint, 0);
                }
                else
                {
                    plain(x, 0, z, road);
                }
            }
        }
        for (int x = RWY0; x <= RWY1; x++)
        {
            colour(x, 0, 9, Blocks.stained_glass, 5);            // green threshold lights
            colour(x, 0, 230, Blocks.stained_glass, 14);         // red end lights
        }
        digits("18", 18, true);
        digits("36", 221, false);
        for (int[] end : new int[][] {{1, 8}, {231, 238}})
        {
            for (int z = end[0]; z <= end[1]; z += 2)
            {
                approachLight(14, z);
                approachLight(15, z);
            }
            int bar = end[0] + 3;
            for (int x = 10; x <= 19; x++)
            {
                if (x != 14 && x != 15)
                {
                    approachLight(x, bar);
                }
            }
        }
        for (int z : new int[] {60, 179})
        {
            for (int k = 0; k < 4; k++)
            {
                int x = RWY1 + 3 + k;
                c.set(x, 1, z, Blocks.cobblestone_wall, 0);
                colour(x, 2, z, paint, k < 2 ? 0 : 14);         // PAPI: two white, two red
            }
        }
        for (int z : new int[] {34, 205})
        {
            for (int x : new int[] {4, 24})
            {
                for (int dx = 0; dx <= 1; dx++)
                {
                    for (int dz = -1; dz <= 1; dz++)
                    {
                        plain(x + dx, 1, z + dz, metalGreen);
                    }
                    plain(x + dx, 2, z, metalGreen);
                }
            }
            for (int x = RWY0 - 2; x <= RWY1 + 2; x++)
            {
                colour(x, 1, z, Blocks.carpet, 15);              // the pendant cable lying across
            }
        }
        poi("runway_north", 14.5F, 9, 1, 0, 25);
        poi("arresting_gear", 31, 4, 27, 135, 15);
    }

    private void approachLight(int x, int z)
    {
        c.set(x, 1, z, Blocks.cobblestone_wall, 0);
        plain(x, 2, z, Blocks.glowstone);
    }

    /** Runway numbers in white, 3 x 5 pixel digits, readable by a pilot landing over the end near zEdge. */
    private void digits(String s, int zEdge, boolean north)
    {
        String[] font = {"111101101101111", "010110010010111", "111001111100111", "111001111001111", "101101111001001",
            "111100111001111", "111100111101111", "111001001001001", "111101111101111", "111101111001111"};
        for (int i = 0; i < 2; i++)
        {
            String f = font[s.charAt(i) - '0'];
            // a pilot from the north faces +z (left = +x); from the south faces -z (left = -x)
            int x0 = north ? (i == 0 ? 17 : 10) : (i == 0 ? 10 : 17);
            for (int row = 0; row < 5; row++)
            {
                for (int col = 0; col < 3; col++)
                {
                    if (f.charAt(row * 3 + col) == '1')
                    {
                        int x = north ? x0 + 2 - col : x0 + col;
                        int z = north ? zEdge + 4 - row : zEdge + row;
                        colour(x, 0, z, paint, 0);
                    }
                }
            }
        }
    }

    /** Infield grass: a windsock on its mast. */
    private void infield()
    {
        int wx = 29, wz = 124;
        for (int y = 1; y <= 5; y++)
        {
            plain(wx, y, wz, Blocks.iron_bars);
        }
        colour(wx + 1, 5, wz, paint, 1);
        colour(wx + 2, 5, wz, paint, 0);
        colour(wx + 3, 5, wz, paint, 1);
        poi("windsock", wx - 6, 4, wz + 8, 210, 10);
    }

    /** Taxiway x 34..41: yellow centreline, blue edge lights every 10, three stubs with hold short lines and a sign. */
    private void taxiway()
    {
        reserve("taxiway", 26, 12, TWY1, 227);
        for (int z = 14; z <= 225; z++)
        {
            for (int x = TWY0; x <= TWY1; x++)
            {
                if (x == 37)
                {
                    colour(x, 0, z, clay, 4);
                }
                else if ((x == TWY0 || x == TWY1) && z % 10 == 0)
                {
                    colour(x, 0, z, Blocks.stained_glass, 11);
                }
                else
                {
                    plain(x, 0, z, road);
                }
            }
        }
        for (int s : STUBS)
        {
            for (int z = s; z <= s + 7; z++)
            {
                for (int x = RWY1 + 3; x < TWY0; x++)
                {
                    boolean hold = x == 28 || x == 29 || (x == 31 || x == 32) && (z & 1) == 0;
                    if (hold || z == s + 3)
                    {
                        colour(x, 0, z, clay, 4);
                    }
                    else
                    {
                        plain(x, 0, z, road);
                    }
                }
            }
            prop(33, 1, s - 3, "BlockRussianRoadSign", S);
        }
    }

    // ================================================================ apron

    /**
     * Apron x 42..71, z 46..202: concrete with joints; four HESCO revetments with fighters and their ground
     * equipment, the cargo hardstand, the helipad with landing lights, the equipment yard (fuel truck, tow tractor,
     * bomb trolleys, light carts, generators, FOD bins), floodlights along the road side.
     */
    private void apron()
    {
        for (int z = 46; z <= 202; z++)
        {
            for (int x = APRON0; x <= APRON1; x++)
            {
                plain(x, 0, z, ((x % 8) == 0 || (z % 8) == 0) ? concreteDark : concrete);
            }
        }
        int burnt = r.nextInt(4), empty = (burnt + 1 + r.nextInt(3)) % 4;
        if (reserve("revetments", 44, 50, 68, 50 + 4 * 16))
        {
            for (int k = 0; k < 4; k++)
            {
                revetment(50 + k * 16, k == burnt ? 2 : k == empty ? 1 : 0, k);
            }
        }
        cargoStand(116);
        helipad(146);
        equipmentYard(166);
        for (int z = 52; z <= 192; z += 20)
        {
            if (z < 114 || z > 144)
            {
                prop(APRON1, 1, z, "BlockHazardLight", WE);   // not in the cargo stand
            }
        }
    }

    /**
     * HESCO revetment x 46..68, z s..s+16 in a row of four sharing their side walls (Bagram's rows of U bays): 15 wide
     * inside for a fighter of 13 span, open to the taxiway, back wall 2 thick. A fighter nose out with a chock; its
     * ground equipment in the back corners beside the tail (generator, light, bomb trolley, wall box, crate). kind 1:
     * the bay is empty (oil stains where its fighter stood), kind 2: burnt (its fighter wrecked, scorched, a body).
     */
    private void revetment(int s, int kind, int k)
    {
        for (int x = 46; x <= 68; x++)
        {
            for (int y = 1; y <= 3; y++)
            {
                plain(x, y, s, hesco);
                plain(x, y, s + 16, hesco);
            }
        }
        for (int z = s; z <= s + 16; z++)
        {
            for (int y = 1; y <= 3; y++)
            {
                plain(67, y, z, hesco);
                plain(68, y, z, hesco);
            }
        }
        int zc = s + 8;
        if (kind == 0)
        {
            fighter(47, zc, true, false);
            c.set(50, 1, zc, Blocks.wooden_slab, 0);            // a chock in front of the nose gear
            if (!poi.containsKey("fighter"))
            {
                poi("fighter", 42, 9, zc - 10, 306, 25);          // three quarter view from above the wall
            }
        }
        else if (kind == 2)
        {
            fighter(47, zc, true, true);
            for (int i = 0; i < 35; i++)
            {
                int x = 46 + r.nextInt(20), z = s + 1 + r.nextInt(15);
                if (c.get(x, 1, z) == null)
                {
                    colour(x, 0, z, clay, r.nextBoolean() ? 15 : 7);
                }
            }
            prop(56, 1, s + 3, "BlockSkeletonGround", E);
        }
        else
        {
            for (int i = 0; i < 10; i++)
            {
                colour(53 + r.nextInt(8), 0, s + 6 + r.nextInt(5), clay, 15);
            }
        }
        prop(65, 1, s + 2, "BlockPowerGenerator", WE);
        prop(65, 1, s + 14, "BlockHazardLight", WE);
        bombTrolley(62, s + 1);
        prop(66, 1, s + 9, "BlockElectricBox1", WE);              // wall mounted: against the HESCO
        prop(62, 1, s + 14, "BlockMilitaryCrate", E);
        poi("revetment_" + k, 40, 4, zc, 270, 12);
    }

    /** Bomb trolley along z: a slab cart, a coal block bomb in the middle with button fins at its ends. */
    private void bombTrolley(int x, int z)
    {
        for (int dz = 0; dz <= 2; dz++)
        {
            c.set(x, 1, z + dz, Blocks.stone_slab, 0);
        }
        plain(x, 2, z + 1, Blocks.coal_block);
    }

    /** Cargo hardstand: the cargo plane, pallets and a care package by its ramp, crates. */
    private void cargoStand(int s)
    {
        if (!reserve("cargo stand", APRON0 + 1, s, APRON1, s + 26))
        {
            return;
        }
        cargo(APRON0 + 2, s + 13);
        prop(68, 1, s + 9, "BlockWaterPalletTarp", S);
        prop(68, 1, s + 11, "BlockWaterPallet", S);
        prop(69, 1, s + 16, "BlockCarePackage", S);
        prop(68, 1, s + 20, "BlockWoodCrate", S);
        poi("cargo", 73, 5, s + 13, 90, 8);
    }

    /** Helipad: concrete square with a white H, corner landing lights, the helicopter wreck on it. */
    private void helipad(int pz)
    {
        int px = 50;
        if (!reserve("helipad", px, pz, px + 15, pz + 15))
        {
            return;
        }
        for (int x = px; x <= px + 15; x++)
        {
            for (int z = pz; z <= pz + 15; z++)
            {
                boolean h = (x == px + 5 || x == px + 10) && z >= pz + 4 && z <= pz + 11 || z == pz + 7 && x >= px + 5 && x <= px + 10;
                boolean light = (x == px || x == px + 15) && (z == pz || z == pz + 15);
                if (light)
                {
                    plain(x, 0, z, Blocks.glowstone);
                }
                else if (h)
                {
                    colour(x, 0, z, paint, 0);
                }
                else
                {
                    plain(x, 0, z, concreteDark);
                }
            }
        }
        prop(px + 8, 1, pz + 8, "BlockWreckageMilitary3", S);
        poi("helipad", px - 7, 9, pz - 7, 315, 25);
    }

    /** Equipment yard: the fuel truck, the tow tractor, bomb trolley rows, light carts, generators, FOD bins, cases. */
    private void equipmentYard(int s)
    {
        if (!reserve("equipment yard", APRON0, s, APRON1, s + 32))
        {
            return;
        }
        fuelTruck(46, s + 3);
        tractor(58, s + 4);
        for (int k = 0; k < 4; k++)
        {
            bombTrolley(46 + k * 3, s + 16);
        }
        for (int k = 0; k < 3; k++)
        {
            prop(60 + k * 3, 1, s + 16, "BlockPowerGenerator", S);
            prop(60 + k * 3, 1, s + 21, "BlockHazardLight", S);
        }
        prop(46, 1, s + 25, "BlockTrashcan", S);
        prop(49, 1, s + 25, "BlockTrashcan", S);
        prop(55, 1, s + 25, "BlockAmmoCrateLarge", S);
        prop(59, 1, s + 25, "BlockMilitaryCrate", S);
        prop(60, 1, s + 25, "BlockMilitaryCrate", S);
        poi("equipment_yard", 56, 7, s - 5, 0, 25);
    }

    /** Fuel truck along z: olive cab with a windscreen, grey tank, coal wheels. */
    private void fuelTruck(int x, int z)
    {
        for (int dz = 0; dz < 9; dz++)
        {
            for (int dx = 0; dx < 3; dx++)
            {
                boolean wheel = (dx == 0 || dx == 2) && (dz == 1 || dz == 6 || dz == 7);
                if (wheel)
                {
                    plain(x + dx, 1, z + dz, Blocks.coal_block);
                }
                else
                {
                    plain(x + dx, 1, z + dz, metalGreen);
                }
                if (dz < 3)
                {
                    if (dz == 0)
                    {
                        plain(x + dx, 2, z + dz, Blocks.glass_pane);
                    }
                    else
                    {
                        colour(x + dx, 2, z + dz, paint, 13);
                    }
                    colour(x + dx, 3, z + dz, paint, 13);
                }
                else
                {
                    colour(x + dx, 2, z + dz, paint, 8);
                    if (dx == 1)
                    {
                        colour(x + dx, 3, z + dz, paint, 8);
                    }
                }
            }
        }
    }

    /** Tow tractor: a low yellow body with black wheels and a seat. */
    private void tractor(int x, int z)
    {
        for (int dz = 0; dz < 4; dz++)
        {
            for (int dx = 0; dx < 2; dx++)
            {
                boolean wheel = dz == 0 || dz == 3;
                colour(x + dx, 1, z + dz, paint, wheel ? 15 : 4);
            }
        }
        c.set(x, 2, z + 2, Blocks.stone_stairs, 0);
    }

    private void landsideRoad()
    {
        reserve("landside road", ROAD0, 19, ROAD1 + 1, 236);
        for (int z = 1; z <= 236; z++)
        {
            for (int x = ROAD0; x <= ROAD1; x++)
            {
                if (x == 74 && z % 4 < 2 && line != null && z > 18)
                {
                    c.set(x, 0, z, line, 4);
                }
                else
                {
                    plain(x, 0, z, road);
                }
            }
        }
        for (int z = 30; z <= 230; z += 24)
        {
            prop(ROAD1 + 1, 1, z, "BlockStreetLight", WE);
        }
    }

    // ================================================================ landside

    /** Fuel farm (x 80..106, z 14..46): four bladders in a sandbag berm, a pump house, drums, fuel lines, a sign. */
    private void fuelFarm()
    {
        if (!reserve("fuel farm", LAND0, 14, 107, 47))
        {
            return;
        }
        for (int x = LAND0; x <= 106; x++)
        {
            for (int z = 14; z <= 46; z++)
            {
                boolean berm = x == LAND0 || x == 106 || z == 14 || z == 46;
                if (berm && !(x == LAND0 && z >= 34 && z <= 37))
                {
                    plain(x, 1, z, noise(x, z) > 0.4 ? sand : sandBeige);
                    if (noise(x * 3, z) > 0.55)
                    {
                        plain(x, 2, z, sand);
                    }
                }
            }
        }
        for (int k = 0; k < 4; k++)
        {
            int x0 = 86 + (k % 2) * 10, z0 = 17 + (k / 2) * 14;     // a walkway along the pump house door
            for (int x = x0; x <= x0 + 8; x++)
            {
                for (int z = z0; z <= z0 + 9; z++)
                {
                    boolean rim = x == x0 || x == x0 + 8 || z == z0 || z == z0 + 9;
                    colour(x, 1, z, paint, rim ? 7 : 15);
                    if (!rim)
                    {
                        colour(x, 2, z, Blocks.carpet, 15);
                    }
                }
            }
            for (int x = LAND0 + 5; x < x0; x++)
            {
                colour(x, 1, z0 + 5, Blocks.carpet, 15);          // the fuel line to the pump house
            }
        }
        box(LAND0 + 1, 17, LAND0 + 4, 23, 3, metalGreen);
        c.clear(LAND0 + 4, 1, 20);                                // its door opens into the farm
        c.clear(LAND0 + 4, 2, 20);
        prop(LAND0 + 3, 1, 18, "BlockPowerGenerator", N);
        prop(LAND0 + 3, 1, 22, "BlockElectricBoxBin", N);
        for (int x = 86; x <= 102; x += 2)
        {
            prop(x, 1, 45, "BlockBarrel", S);
        }
        prop(105, 1, 16, "BlockRussianRoadSignWarning", S);
        poi("fuel", 70, 8, 30, 270, 25);
    }

    /**
     * Hardened aircraft shelter (x 79..104, z s..s+22): a concrete arch over the bay, the back wall, blast doors in two
     * leaves half open at the front (west, on their track); kind 0: a fighter nose out, crates, a generator; kind 1:
     * maintenance (a fighter with a wing panel off, its engine on a stand, a work stand, shelves, tools, a missile
     * case, a weapon cabinet). A guard post at the door.
     */
    private void shelter(int s, int kind)
    {
        int x0 = LAND0, x1 = LAND0 + 24, z1 = s + 22;
        if (!reserve("shelter " + kind, x0 - 1, s, x1, z1))
        {
            return;
        }
        double mid = (s + z1) / 2.0, rad = (z1 - s) / 2.0 + 0.5;
        for (int x = x0; x <= x1; x++)
        {
            for (int z = s; z <= z1; z++)
            {
                plain(x, 0, z, concrete);
                int h = arch(z, mid, rad, 9);
                int hn = Math.min(arch(z - 1, mid, rad, 9), arch(z + 1, mid, rad, 9));
                for (int y = Math.max(1, Math.min(h, hn)); y <= h; y++)
                {
                    plain(x, y, z, (x + y) % 6 != 0 ? concrete : concreteDark);
                }
                if (x == x1)
                {
                    for (int y = 1; y < h; y++)
                    {
                        plain(x, y, z, concrete);
                    }
                }
            }
        }
        for (int z = s; z <= z1; z++)
        {
            int h = arch(z, mid, rad, 9);
            boolean leaf = z <= s + 6 || z >= z1 - 6;
            for (int y = 1; y < h; y++)
            {
                if (leaf)
                {
                    plain(x0 - 1, y, z, y % 4 == 0 ? metal : metalGreen);   // steel leaves in camouflage green, ribbed
                }
            }
            plain(x0 - 1, 0, z, concreteDark);                     // the door track
        }
        int zc = s + 11;
        fighter(x0 + 3, zc, true, false);
        if (kind == 0)
        {
            prop(x1 - 2, 1, s + 4, "BlockAmmoCrateLarge", WE);
            prop(x1 - 2, 1, z1 - 4, "BlockMilitaryCrate", WE);
            prop(x1 - 2, 1, z1 - 5, "BlockMilitaryCrate", WE);
            prop(x0 + 18, 1, s + 4, "BlockPowerGenerator", N);
        }
        else
        {
            for (int v = 2; v <= 4; v++)
            {
                c.clear(x0 + 14, 2, zc + v);                      // a wing panel off (wing cells u 11, 12)
                c.clear(x0 + 15, 2, zc + v);
            }
            for (int dx = 0; dx < 3; dx++)
            {
                plain(x0 + 12 + dx, 1, zc + 7, Blocks.fence);
                plain(x0 + 12 + dx, 2, zc + 7, Blocks.coal_block);
            }
            for (int y = 1; y <= 3; y++)
            {
                plain(x0 + 6, y, zc - 6, Blocks.fence);
            }
            c.set(x0 + 6, 4, zc - 6, Blocks.wooden_slab, 1);
            prop(x1 - 2, 1, s + 5, "BlockMetalShelf", WE);
            prop(x1 - 1, 1, s + 8, "BlockElectricBox1", WE);
            prop(x1 - 2, 1, z1 - 4, "BlockAmmoCrateLarge", WE);
            prop(x1 - 1, 1, z1 - 7, "BlockWeaponCabinet", WE);        // against the back wall, clear of the stabilator
        }
        posts.add(new int[] {x0 - 2, 1, s + 9});
        poi("shelter_" + kind, x0 - 10, 4, zc, 270, 8);
    }

    /** Height of an arch at z: 1 at the springing, top at its middle, 0 outside. */
    private static int arch(int z, double mid, double rad, int top)
    {
        double t = Math.abs(z - mid) / rad;
        if (t >= 1)
        {
            return 0;
        }
        return Math.max(1, (int) Math.round(top * Math.sqrt(1 - t * t)));
    }

    /**
     * The burnt maintenance hangar (x 80..107, z s..s+25): metal vault open west; scorched panels and floor, roof
     * holes, a wrecked fighter, tyres, drums, a body bag, a burning can, the spawner of military infected.
     */
    private void hangar(int z0)
    {
        int x0 = LAND0, x1 = LAND0 + 27, z1 = z0 + 25;
        if (!reserve("burnt hangar", x0, z0, x1, z1))
        {
            return;
        }
        for (int x = x0; x <= x1; x++)
        {
            for (int z = z0; z <= z1; z++)
            {
                if (noise(x, z) > 0.6)
                {
                    colour(x, 0, z, clay, 15);
                }
                else
                {
                    plain(x, 0, z, concrete);
                }
                boolean side = z == z0 || z == z1, back = x == x1, front = x == x0;
                int roof = 8 + Math.min(4, Math.min(z - z0, z1 - z) / 2);
                if (side || back)
                {
                    for (int y = 1; y < roof; y++)
                    {
                        if (noise(x, z * 7 + y) > 0.68)
                        {
                            colour(x, y, z, clay, 15);
                        }
                        else
                        {
                            plain(x, y, z, metal);
                        }
                    }
                }
                if (front)
                {
                    boolean opening = z >= z0 + 2 && z <= z1 - 2;
                    for (int y = opening ? 8 : 1; y < roof; y++)
                    {
                        plain(x, y, z, metal);
                    }
                }
                if (noise(x * 3, z) <= 0.74)
                {
                    plain(x, roof, z, metalGreen);
                }
            }
        }
        fighter(x0 + 8, z0 + 13, true, true);
        c.set(x1 - 4, 1, z0 + 5, Blocks.mob_spawner, 0, Canvas.PLAIN);
        spawners.add(new int[] {x1 - 4, 1, z0 + 5});
        prop(x0 + 4, 1, z0 + 3, "BlockTireStack", S);
        prop(x0 + 20, 1, z1 - 4, "BlockBarrel", S);
        prop(x0 + 21, 1, z1 - 4, "BlockBarrel", S);
        prop(x0 + 15, 1, z0 + 4, "BlockBodyBagGreen", E);
        prop(x1 - 3, 1, z1 - 3, "BlockCanFire", S);
        posts.add(new int[] {x0 + 2, 1, z0 + 2});
        poi("hangar_burnt", x0 - 7, 4, z0 + 13, 270, 8);
    }

    /**
     * Crash fire rescue station (x 80..96, z 136..154): two bays open west, the red crash truck in one, hose coils in
     * the other; the crew room behind (bunks, lockers, a table, a radio, a medical crate); a siren on the roof.
     */
    private void fireStation()
    {
        int x0 = LAND0, z0 = 136, x1 = 96, z1 = 154;
        if (!reserve("fire station", x0, z0, x1, z1))
        {
            return;
        }
        box(x0, z0, x1, z1, 6, concrete);
        for (int z = z0 + 1; z <= z1 - 1; z++)
        {
            for (int y = 1; y <= 5; y++)
            {
                plain(x0 + 11, y, z, concrete);                   // the crew room's wall
            }
        }
        for (int x = x0 + 1; x < x0 + 11; x++)
        {
            for (int y = 1; y <= 5; y++)
            {
                plain(x, y, z0 + 9, concrete);                    // between the bays
            }
        }
        for (int[] bay : new int[][] {{z0 + 2, z0 + 7}, {z0 + 11, z0 + 16}})
        {
            for (int z = bay[0]; z <= bay[1]; z++)
            {
                for (int y = 1; y <= 4; y++)
                {
                    c.clear(x0, y, z);
                }
            }
        }
        door(x0 + 11, 1, z0 + 13, door, E);
        crashTruck(x0 + 2, z0 + 2);
        for (int z = z0 + 11; z <= z0 + 16; z += 2)
        {
            colour(x0 + 9, 1, z, Blocks.carpet, 14);              // hose coils
        }
        for (int z = z0 + 2; z <= z1 - 3; z += 3)
        {
            bunkAt(x1 - 1, 1, z);
        }
        prop(x0 + 13, 1, z0 + 2, "BlockElectricBoxBin", S);
        prop(x0 + 14, 1, z0 + 2, "BlockElectricBoxBin", S);
        prop(x0 + 13, 1, z0 + 8, "BlockWoodTable", S);
        prop(x0 + 13, 1, z1 - 2, "BlockMilitaryRadioSmall", N);
        prop(x0 + 14, 1, z1 - 2, "BlockMedicalCrate", N);
        lights(x0 + 11, z0, x1, z1, 5);
        prop(x0 + 5, 7, z0 + 9, "BlockRedlight", S);
        poi("fire_station", x0 - 10, 4, z0 + 5, 270, 10);
    }

    /** Crash truck along z: red body, a white front, black wheels, a foam turret and a light bar. */
    private void crashTruck(int x, int z)
    {
        for (int dz = 0; dz < 6; dz++)
        {
            for (int dx = 0; dx < 3; dx++)
            {
                boolean wheel = (dx == 0 || dx == 2) && (dz == 1 || dz == 4);
                colour(x + dx, 1, z + dz, paint, wheel ? 15 : 14);
                colour(x + dx, 2, z + dz, paint, dz == 0 ? 0 : 14);
                if (dz < 4)
                {
                    colour(x + dx, 3, z + dz, paint, 14);
                }
            }
        }
        plain(x + 1, 2, z, Blocks.glass_pane);
        plain(x + 1, 4, z + 2, Blocks.iron_bars);
        prop(x + 1, 4, z + 1, "BlockRedlight", S);
    }

    /**
     * Surveillance radar (x 99..109, z 136..154): a chain link plot with its gate west, a lattice tower 8 high with the
     * antenna (a reflector and its feed arm) on top, an equipment shelter with a monitor and panels, a generator.
     */
    private void radar()
    {
        int x0 = 99, z0 = 136, x1 = 109, z1 = 154;
        if (!reserve("radar", x0, z0, x1, z1))
        {
            return;
        }
        for (int x = x0; x <= x1; x++)
        {
            for (int z = z0; z <= z1; z++)
            {
                plain(x, 0, z, Blocks.gravel);
                boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                if (edge && !(x == x0 && z >= 142 && z <= 143))
                {
                    c.set(x, 1, z, Blocks.iron_bars, 0);                   // chain link, like the city fence
                    c.set(x, 2, z, Blocks.iron_bars, 0);
                }
            }
        }
        int tx = 105, tz = 148;
        for (int y = 1; y <= 8; y++)
        {
            prop(tx, y, tz, "BlockRadioTower", S);
        }
        plain(tx, 9, tz, metal);
        for (int dz = -2; dz <= 2; dz++)
        {
            plain(tx - 1, 10, tz + dz, metal);
            if (Math.abs(dz) < 2)
            {
                plain(tx - 1, 11, tz + dz, metal);
            }
        }
        plain(tx, 10, tz - 2, metal);
        plain(tx, 10, tz + 2, metal);
        plain(tx - 2, 10, tz, Blocks.iron_bars);
        box(x0 + 2, z0 + 2, x0 + 6, z0 + 6, 4, metalGreen);
        c.clear(x0 + 2, 1, z0 + 4);
        c.clear(x0 + 2, 2, z0 + 4);
        prop(x0 + 4, 1, z0 + 3, "BlockMonitor", S);
        prop(x0 + 5, 1, z0 + 5, "BlockElectricBox1", WE);
        prop(x0 + 4, 1, z0 + 10, "BlockPowerGenerator", N);
        poi("radar", x0 + 1, 3.5F, z1 + 2.5F, 200, -8);          // on the street between the plot and the HQ
    }

    /**
     * Control tower 8 x 8 (x 80..87, z 158..165) with a catwalk round the cab: concrete shaft, door west, a ladder up
     * to the cab hatch, the glass cab from y 16 with consoles along the windows (desks, monitors, radios, chairs), a
     * roof with masts, a beacon and a searchlight. Guard posts in the cab.
     */
    private void tower()
    {
        int x0 = 80, z0 = 158, x1 = 87, z1 = 165, cab = 16;
        if (!reserve("tower", x0 - 1, z0 - 1, x1 + 1, z1 + 1))
        {
            return;
        }
        for (int x = x0; x <= x1; x++)
        {
            for (int z = z0; z <= z1; z++)
            {
                boolean ring = x == x0 || x == x1 || z == z0 || z == z1;
                plain(x, 0, z, concrete);
                for (int y = 1; y < cab; y++)
                {
                    if (ring)
                    {
                        plain(x, y, z, y % 5 == 0 ? concreteDark : concrete);
                    }
                }
                plain(x, cab, z, concrete);
                if (ring)
                {
                    plain(x, cab + 1, z, concrete);
                    for (int y = cab + 2; y <= cab + 3; y++)
                    {
                        boolean corner = (x == x0 || x == x1) && (z == z0 || z == z1);
                        plain(x, y, z, corner ? concrete : Blocks.glass_pane);
                    }
                }
                plain(x, cab + 4, z, Blocks.double_stone_slab);
            }
        }
        for (int x = x0 - 1; x <= x1 + 1; x++)
        {
            for (int z = z0 - 1; z <= z1 + 1; z++)
            {
                if (x == x0 - 1 || x == x1 + 1 || z == z0 - 1 || z == z1 + 1)
                {
                    c.set(x, cab, z, Blocks.stone_slab, 8);
                    plain(x, cab + 1, z, Blocks.iron_bars);
                }
            }
        }
        door(x0, 1, z0 + 3, door, WE);
        for (int y = 1; y <= cab; y++)
        {
            c.set(x1 - 1, y, z0 + 5, Blocks.ladder, 4);
        }
        c.clear(x1 - 1, cab, z0 + 5);
        for (int x = x0 + 2; x <= x0 + 4; x += 2)
        {
            prop(x, cab + 1, z0 + 1, "BlockMetalTable", S);       // a table is drawn 2 long: 2 in from the wall
            prop(x, cab + 2, z0 + 1, "BlockMonitor", S);
        }
        prop(x0 + 2, cab + 1, z0 + 3, "BlockOfficeChair", N);
        prop(x0 + 4, cab + 1, z0 + 3, "BlockOfficeChair", N);
        prop(x0 + 1, cab + 1, z1 - 1, "BlockMilitaryRadioSmall", N);
        prop(x0 + 3, cab + 1, z1 - 1, "BlockMilitaryRadioSmall", N);
        for (int y = cab + 5; y <= cab + 9; y++)
        {
            prop(x0 + 1, y, z0 + 1, "BlockRadioTower", S);
        }
        prop(x1 - 1, cab + 5, z0 + 1, "BlockRedlight", S);
        prop(x1 - 1, cab + 5, z1 - 1, "BlockSpotlight", WE);
        posts.add(new int[] {x0 + 3, cab + 1, z0 + 5});
        posts.add(new int[] {x0 + 5, cab + 1, z1 - 2});
        poi("tower", x0 - 22, 10, z0 + 4, 270, 18);
        poi("tower_cab", x0 + 4.5F, cab + 2.6F, z1 - 1.5F, 180, 10);
    }

    /**
     * HQ (x 91..109, z 158..180), concrete, barred windows, a sandbag blast wall along its west front; the locked door
     * west with the keycard screen; inside a corridor and three rooms: operations (map tables, desks with monitors,
     * a radio, a weapon cabinet), the commander's office (desk, cabinet, large case, a care package), the comms room
     * (radios, a panel). Top military loot. Antenna and flag outside.
     */
    private void hq()
    {
        int x0 = 91, z0 = 158, x1 = 109, z1 = 180;
        if (!reserve("hq", x0 - 2, z0, x1, z1))
        {
            return;
        }
        box(x0, z0, x1, z1, 5, concrete);
        windows(x0 + 1, z0, x1 - 1, z0);
        windows(x0 + 1, z1, x1 - 1, z1);
        for (int z = z0 + 1; z < z1; z++)
        {
            for (int y = 1; y <= 4; y++)
            {
                plain(x0 + 4, y, z, concrete);                    // corridor wall
            }
        }
        for (int x = x0 + 5; x < x1; x++)
        {
            for (int y = 1; y <= 4; y++)
            {
                plain(x, y, z0 + 11, concrete);
                plain(x, y, z0 + 17, concrete);
            }
        }
        door(x0 + 4, 1, z0 + 5, door, E);
        door(x0 + 4, 1, z0 + 14, door, E);
        door(x0 + 4, 1, z0 + 19, door, E);
        door(x0, 1, z0 + 9, locked, WE);
        if (keycard != null)
        {
            c.prop(x0 - 1, 2, z0 + 11, keycard, WE);
        }
        prop(x0 + 9, 1, z0 + 6, "BlockChesstable", S);           // map tables
        prop(x0 + 11, 1, z0 + 6, "BlockChesstable", S);
        for (int x = x0 + 7; x <= x0 + 15; x += 2)
        {
            prop(x, 1, z0 + 1, "BlockMetalTable", S);
            prop(x, 2, z0 + 1, "BlockMonitor", S);
            prop(x, 1, z0 + 2, "BlockOfficeChair", N);
        }
        prop(x1 - 1, 1, z0 + 5, "BlockMilitaryRadioSmall", WE);   // the big radio is drawn 1.7 deep
        prop(x1 - 1, 1, z0 + 9, "BlockWeaponCabinet", WE);
        prop(x0 + 10, 1, z0 + 14, "BlockMetalTable", S);
        prop(x0 + 10, 1, z0 + 15, "BlockOfficeChair", N);
        prop(x1 - 1, 1, z0 + 13, "BlockWeaponCabinet", WE);
        prop(x1 - 1, 1, z0 + 15, "BlockAmmoCrateLarge", WE);
        prop(x0 + 6, 1, z0 + 15, "BlockCarePackage", E);
        for (int x = x0 + 6; x <= x1 - 2; x += 3)
        {
            prop(x, 1, z1 - 1, "BlockMilitaryRadioSmall", N);
        }
        prop(x1 - 1, 1, z0 + 19, "BlockElectricBox1", WE);
        prop(x0 + 1, 1, z0 + 2, "BlockMilitaryCrate", E);
        prop(x0 + 1, 1, z0 + 3, "BlockMilitaryCrate", E);
        prop(x0 + 1, 1, z1 - 2, "BlockAmmoCrateLarge", E);
        lights(x0, z0, x1, z1, 4);
        for (int z = z0; z <= z1; z++)
        {
            if (z < z0 + 7 || z > z0 + 12)
            {
                plain(x0 - 2, 1, z, sand);
                plain(x0 - 2, 2, z, noise(x0, z) > 0.3 ? sand : sandBeige);
            }
        }
        for (int y = 6; y <= 9; y++)
        {
            prop(x1 - 2, y, z0 + 2, "BlockRadioTower", S);
        }
        prop(x0 - 2, 1, z1 + 2, "BlockFlagPollUAHD", S);         // clear of the blast wall (the flag flies east)
        posts.add(new int[] {x0 - 1, 1, z0 + 8});
        poi("hq", x0 - 10, 3, z0 + 9.5F, 270, 8);
        poi("hq_ops", x0 + 14.5F, 2.6F, z0 + 9.5F, 135, 20);
        poi("hq_office", x0 + 5.5F, 2.6F, z0 + 12.5F, 290, 15);
    }

    /**
     * Barracks (z 184..200): two B-huts (raised birch floor, gable roof, windows, a step at the door) with bunks,
     * footlockers (half vanilla chests), a weapon cabinet and a crate; a duck and cover bunker between them.
     */
    private void barracks()
    {
        for (int k = 0; k < 2; k++)
        {
            int x0 = LAND0 + k * 17, x1 = x0 + 8, z0 = 184, z1 = 200;
            if (!reserve("barracks " + k, x0, z0 - 1, x1, z1))
            {
                continue;
            }
            int ridge = (x0 + x1) / 2;
            for (int x = x0; x <= x1; x++)
            {
                for (int z = z0; z <= z1; z++)
                {
                    boolean ring = x == x0 || x == x1 || z == z0 || z == z1;
                    c.set(x, 1, z, Blocks.planks, 2);
                    if (ring)
                    {
                        for (int y = 2; y <= 4; y++)
                        {
                            c.set(x, y, z, Blocks.planks, 2);
                        }
                    }
                    int roof = 5 + Math.min(2, Math.min(x - x0, x1 - x) / 2);
                    c.set(x, roof, z, x == ridge ? Blocks.wooden_slab : Blocks.planks, 1);
                    for (int y = 5; y < roof; y++)
                    {
                        if (z == z0 || z == z1)
                        {
                            c.set(x, y, z, Blocks.planks, 1);
                        }
                    }
                }
            }
            for (int z = z0 + 3; z <= z1 - 3; z += 4)
            {
                c.set(x0, 3, z, Blocks.glass_pane, 0);
                c.set(x1, 3, z, Blocks.glass_pane, 0);
            }
            door(ridge, 2, z0, door, N);
            c.set(ridge, 1, z0 - 1, Blocks.oak_stairs, 2);       // the step up to the door (ascending south)
            for (int z = z0 + 2; z <= z1 - 4; z += 3)
            {
                bunkAt(x0 + 1, 2, z);
                bunkAt(x1 - 1, 2, z);
                if ((z & 1) == 0)
                {
                    c.set(x0 + 2, 2, z, Blocks.chest, 5);
                }
                else
                {
                    prop(x0 + 2, 2, z, "BlockStorageCrate", E);
                }
            }
            prop(x1 - 1, 2, z1 - 1, "BlockWeaponCabinet", N);
            prop(x0 + 1, 2, z1 - 1, "BlockMilitaryCrate", N);
        }
        if (reserve("bunker", 91, 186, 95, 198))
        {
            for (int x = 91; x <= 95; x++)
            {
                for (int z = 186; z <= 198; z++)
                {
                    if (x == 91 || x == 95)
                    {
                        plain(x, 1, z, concrete);
                        plain(x, 2, z, concrete);
                    }
                    plain(x, 3, z, concrete);
                    plain(x, 4, z, sand);
                }
            }
            prop(93, 1, 192, "BlockWaterPallet", S);
        }
        poi("barracks", LAND0 + 4.5F, 4, 177, 0, 10);
    }

    private void bunkAt(int x, int y, int z)
    {
        c.set(x, y, z, Blocks.bed, 0);
        c.set(x, y, z + 1, Blocks.bed, 8);
        c.set(x, y + 1, z, Blocks.wooden_slab, 9);
        c.set(x, y + 1, z + 1, Blocks.wooden_slab, 9);
        c.set(x, y + 2, z, Blocks.carpet, 8);
        c.set(x, y + 2, z + 1, Blocks.carpet, 8);
    }

    /**
     * Munitions storage area (x 80..110, z 204..233): HESCO wall 2 high, its gate on the road side with a warning
     * sign and a guard post; three earth covered igloos (concrete headwall, metal door, an arch under a grass mound)
     * holding ammo cases, crates and bombs; bomb trolleys; the armory (locked, keycard: the top loot).
     */
    private void munitions()
    {
        int x0 = LAND0, z0 = 204, x1 = 110, z1 = 233;
        if (!reserve("munitions area", x0, z0, x1, z1))
        {
            return;
        }
        for (int x = x0; x <= x1; x++)
        {
            for (int z = z0; z <= z1; z++)
            {
                boolean wall = x == x0 || x == x1 || z == z0 || z == z1;
                boolean gate = x == x0 && z >= 209 && z <= 212;
                if (wall && !gate)
                {
                    plain(x, 1, z, hesco);
                    plain(x, 2, z, hesco);
                }
            }
        }
        prop(x0 - 1, 1, 207, "BlockRussianRoadSignWarning", WE);
        posts.add(new int[] {x0 + 2, 1, 211});
        for (int k = 0; k < 3; k++)
        {
            igloo(x0 + 6, 206 + k * 10, k);
        }
        bombTrolley(x0 + 3, 216);
        bombTrolley(x0 + 3, 226);
        int ax0 = 100, az0 = 207, ax1 = 108, az1 = 216;
        box(ax0, az0, ax1, az1, 4, concreteDark);
        door(ax0, 1, az0 + 4, locked, WE);
        if (keycard != null)
        {
            c.prop(ax0 - 1, 2, az0 + 6, keycard, WE);
        }
        for (int z = az0 + 2; z <= az1 - 2; z++)
        {
            prop(ax1 - 1, 1, z, z % 2 == 0 ? "BlockWeaponCabinet" : "BlockMilitaryCrate", WE);
        }
        prop(ax0 + 3, 1, az1 - 1, "BlockAmmoCrateLarge", S);
        prop(ax0 + 6, 1, az1 - 1, "BlockAmmoCrate", N);
        lights(ax0, az0, ax1, az1, 3);
        poi("munitions", x0 - 9, 7, 220, 270, 18);
        poi("armory", ax0 + 1.5F, 2.6F, az0 + 1.5F, 315, 15);
    }

    /** Igloo at x0, z0 (door west): concrete headwall and back, a metal door, an arch 7 wide under a grass mound; loot inside. */
    private void igloo(int x0, int z0, int k)
    {
        int x1 = x0 + 10, z1 = z0 + 6;
        double mid = z0 + 3, rad = 4;
        for (int x = x0; x <= x1; x++)
        {
            for (int z = z0; z <= z1; z++)
            {
                plain(x, 0, z, concrete);
                int h = arch(z, mid, rad, 4);
                int hn = Math.min(arch(z - 1, mid, rad, 4), arch(z + 1, mid, rad, 4));
                for (int y = Math.max(1, Math.min(h, hn)); y <= h; y++)
                {
                    plain(x, y, z, concrete);
                }
                if (x == x1 || x == x0)
                {
                    for (int y = 1; y <= h; y++)
                    {
                        plain(x, y, z, concrete);
                    }
                }
                if (x > x0)
                {
                    plain(x, h + 1, z, Blocks.dirt);
                    plain(x, h + 2, z, Blocks.grass);              // the earth cover
                }
            }
        }
        door(x0, 1, z0 + 3, door, WE);
        prop(x1 - 1, 1, z0 + 2, "BlockAmmoCrateLarge", WE);
        prop(x1 - 1, 1, z0 + 4, "BlockMilitaryCrate", WE);
        prop(x0 + 3, 1, z0 + 2, k == 1 ? "BlockAmmoCrate" : "BlockWoodCrate", S);
        prop(x0 + 6, 1, z0 + 4, "BlockMilitaryCrate", N);
        if (k == 0)
        {
            poi("igloo_inside", x0 + 1.5F, 1.6F, z0 + 3.5F, 270, 10);
        }
    }

    // ================================================================ aircraft

    /** Fighter size (F-15 at 1 block = 1 m, docs/airport.md section 4): cells along, half span. */
    static final int FIGHTER_LEN = 19, FIGHTER_HALF = 6;

    /**
     * A fighter built from blocks, F-15 size (19 long, 13 span, 6 high), nose west or east from x0, centred on zc.
     * Grey wool body 3 wide and 2 high from the intakes back, darker radome, glass canopy, box intakes with dark mouths,
     * swept slab wings, stabilators, twin fins, two nozzles, gear legs. Wrecked: the tail section and the right wing
     * gone, scorched panels, the broken tail lying beside it.
     */
    void fighter(int x0, int zc, boolean noseWest, boolean wrecked)
    {
        int last = wrecked ? 14 : FIGHTER_LEN - 1;
        for (int u = 0; u <= last; u++)
        {
            int x = noseWest ? x0 + u : x0 + FIGHTER_LEN - 1 - u;
            int grey = wrecked && r.nextInt(4) == 0 ? 15 : 8;
            if (u <= 2)
            {
                colour(x, 2, zc, paint, 8);                         // radome (grey 7 reads near black)
            }
            else if (u <= 5)
            {
                colour(x, 2, zc, paint, grey);
                plain(x, 3, zc, Blocks.glass);                      // canopy
            }
            else if (u <= 16)
            {
                for (int v = -1; v <= 1; v++)
                {
                    colour(x, 2, zc + v, paint, wrecked && r.nextInt(4) == 0 ? 15 : 8);
                    colour(x, 3, zc + v, paint, wrecked && r.nextInt(5) == 0 ? 15 : 8);
                }
            }
            else
            {
                colour(x, 2, zc, paint, 7);                         // the stinger between the engines
                for (int v = -1; v <= 1; v += 2)
                {
                    if (u == FIGHTER_LEN - 1)
                    {
                        plain(x, 2, zc + v, Blocks.coal_block);     // nozzles
                    }
                    else
                    {
                        colour(x, 2, zc + v, paint, 7);
                    }
                }
            }
            if (u == 5)
            {
                for (int v = -1; v <= 1; v += 2)
                {
                    plain(x, 2, zc + v, Blocks.coal_block);         // intake mouths
                    colour(x, 3, zc + v, paint, 8);
                }
            }
            for (int v = 2; v <= FIGHTER_HALF; v++)
            {
                if (u >= 7 + v && u <= 14)
                {
                    c.set(x, 2, zc - v, Blocks.stone_slab, 8);       // swept wings
                    if (!wrecked)
                    {
                        c.set(x, 2, zc + v, Blocks.stone_slab, 8);
                    }
                }
                if (v <= 4 && u >= 13 + v)
                {
                    c.set(x, 2, zc - v, Blocks.stone_slab, 8);       // stabilators
                    c.set(x, 2, zc + v, Blocks.stone_slab, 8);
                }
            }
            if (u >= 14 && !wrecked)
            {
                for (int v = -1; v <= 1; v += 2)
                {
                    colour(x, 4, zc + v, paint, 8);                 // twin fins
                    if (u >= 15)
                    {
                        colour(x, 5, zc + v, paint, 8);
                    }
                    if (u >= 16 && u <= 17)
                    {
                        colour(x, 6, zc + v, paint, 8);
                    }
                }
            }
            if (u == 4)
            {
                plain(x, 1, zc, Blocks.cobblestone_wall);           // nose gear
            }
            if (u == 12)
            {
                plain(x, 1, zc - 1, Blocks.cobblestone_wall);       // main gear
                plain(x, 1, zc + 1, Blocks.cobblestone_wall);
            }
        }
        if (wrecked)
        {
            for (int k = 0; k < 3; k++)
            {
                int x = noseWest ? x0 + 11 + k : x0 + FIGHTER_LEN - 12 - k;
                colour(x, 1, zc + 4, paint, k == 1 ? 15 : 7);       // the broken tail lies beside the wreck
                colour(x, 1, zc + 5, paint, k == 2 ? 15 : 8);
            }
            int fx = noseWest ? x0 + 12 : x0 + FIGHTER_LEN - 13;
            colour(fx, 2, zc + 4, paint, 7);                        // one fin still standing on it
            colour(fx, 3, zc + 4, paint, 15);
        }
    }

    /**
     * Cargo plane nose west: 22 long, 24 span, fuselage 5 wide and 5 high, hollow (3 x 3 inside), the rear ramp down
     * (walk in), dark windows, a high wing with four engine pods, a fin and tailplane. Loot inside.
     */
    private void cargo(int x0, int zc)
    {
        for (int u = 0; u <= 21; u++)
        {
            int x = x0 + u;
            int half = u <= 1 ? 1 : 2;
            for (int v = -half; v <= half; v++)
            {
                boolean wall = Math.abs(v) == half;
                for (int y = 1; y <= 5; y++)
                {
                    boolean shell = wall || y == 1 || y == 5 || u == 0;
                    boolean ramp = u >= 19 && !wall && y > 1;
                    if (shell && !ramp)
                    {
                        colour(x, y, zc + v, paint, (y == 3 && wall && u % 3 == 1) ? 15 : 8);
                    }
                }
            }
            if (u >= 1 && u <= 2)
            {
                for (int v = -1; v <= 1; v++)
                {
                    plain(x, 4, zc + v, Blocks.glass);
                }
            }
        }
        for (int v = -1; v <= 1; v++)
        {
            c.set(x0 + 21, 1, zc + v, Blocks.stone_stairs, 0);
        }
        for (int u = 7; u <= 10; u++)
        {
            for (int v = -12; v <= 12; v++)
            {
                if (Math.abs(v) > 2)
                {
                    colour(x0 + u, 5, zc + v, paint, 8);
                }
            }
        }
        for (int v : new int[] {-9, -5, 5, 9})
        {
            for (int u = 6; u <= 9; u++)
            {
                plain(x0 + u, 4, zc + v, Blocks.coal_block);
            }
            plain(x0 + 6, 3, zc + v, metal);
        }
        for (int y = 6; y <= 10; y++)
        {
            colour(x0 + 19, y, zc, paint, 8);
            colour(x0 + 20, y, zc, paint, y == 10 ? 7 : 8);
        }
        for (int v = -5; v <= 5; v++)
        {
            if (v != 0)
            {
                c.set(x0 + 20, 7, zc + v, Blocks.stone_slab, 8);
            }
        }
        prop(x0 + 5, 2, zc - 1, "BlockMilitaryCrate", S);
        prop(x0 + 6, 2, zc - 1, "BlockMilitaryCrate", S);
        prop(x0 + 9, 2, zc + 1, "BlockMilitaryCrate", N);
        prop(x0 + 13, 2, zc - 1, "BlockAmmoCrate", S);
    }

    // ================================================================ damage

    /**
     * Half wrecked and still held: a fighter crashed on the runway (in two, scorched, a body beside it), scorch marks
     * on the taxiway, barricades across the taxiway, sandbag positions at the apron's corners with guard posts, trash.
     */
    private void damage()
    {
        int crashZ = 90 + r.nextInt(50);
        fighter(RWY0 + 1, crashZ, r.nextBoolean(), true);
        for (int k = 0; k < 55; k++)
        {
            int x = RWY0 - 1 + r.nextInt(22), z = crashZ - 7 + r.nextInt(15);
            if (c.get(x, 1, z) == null && c.get(x, 2, z) == null)
            {
                colour(x, 0, z, clay, r.nextInt(3) == 0 ? 7 : 15);
            }
        }
        prop(RWY1 + 3, 1, crashZ + 6, "BlockSkeletonGround", S);
        for (int k = 0; k < 3; k++)
        {
            scorch(TWY0 + r.nextInt(3), 60 + r.nextInt(120));
        }
        for (int z : new int[] {100, 154})
        {
            prop(TWY0, 1, z, "BlockBarrier", S);
            prop(TWY0 + 2, 1, z, "BlockBarrier", S);
            prop(TWY0 + 5, 1, z, "BlockHedgehog", S);
        }
        for (int[] p : new int[][] {{APRON0, 46}, {APRON1 - 3, 46}, {APRON0, 199}, {APRON1 - 3, 199}})
        {
            for (int x = p[0]; x <= p[0] + 3; x++)
            {
                for (int z = p[1]; z <= p[1] + 3; z++)
                {
                    boolean ring = x == p[0] || x == p[0] + 3 || z == p[1] || z == p[1] + 3;
                    if (ring && !(x == p[0] + 1 && (z == p[1] || z == p[1] + 3)) && c.get(x, 1, z) == null)
                    {
                        plain(x, 1, z, sand);
                        plain(x, 2, z, noise(x, z) > 0.5 ? sand : sandBeige);
                    }
                }
            }
            posts.add(new int[] {p[0] + 2, 1, p[1] + 2});
        }
        prop(ROAD1 + 2, 1, 104, "BlockTrashBag2", S);
        prop(ROAD1 + 2, 1, 134, "BlockTrashBag1", S);
        poi("runway_crash", RWY1 + 12, 6, crashZ - 14, 30, 15);
        poi("top", W / 2.0F, 70, L / 2.0F, 180, 89);
    }

    private void scorch(int x0, int z0)
    {
        for (int x = x0; x < x0 + 6; x++)
        {
            for (int z = z0; z < z0 + 6; z++)
            {
                double dx = x - x0 - 2.5, dz = z - z0 - 2.5;
                if (dx * dx + dz * dz < 7 + r.nextInt(3) && c.get(x, 1, z) == null)
                {
                    colour(x, 0, z, clay, r.nextInt(4) == 0 ? 12 : 15);
                }
            }
        }
    }
}

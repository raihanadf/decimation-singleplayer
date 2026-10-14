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
 * built from blocks, held by the enemy military but half wrecked, bunker level loot in the HQ and armory).
 * Canvas W x H x L: x across (0 west .. 111), z along the runway (0 north, the entry side .. 239), y 0 = the
 * ground surface. Left to right across: perimeter, runway strip, grass, taxiway, apron, landside road,
 * landside buildings, perimeter. Built once per seed; props checked by Canvas.validateProps.
 */
public final class Airport
{
    public static final int W = 112, L = 240, H = 28;
    static final int E = 2, S = 3, WE = 4, N = 5;
    // columns across (x)
    static final int RWY0 = 8, RWY1 = 21, TWY0 = 34, TWY1 = 41, APRON0 = 42, APRON1 = 71, ROAD0 = 72, ROAD1 = 77,
        LAND0 = 78, LAND1 = 107;

    final Canvas c = new Canvas(W, H, L);
    final List<String> log = new ArrayList<String>();
    final Map<String, float[]> poi = new LinkedHashMap<String, float[]>();
    /** Garrison posts (x, y, z) and spawner cells, canvas coordinates. */
    final List<int[]> posts = new ArrayList<int[]>(), spawners = new ArrayList<int[]>();
    private final Random r;
    private final Block metal, metalRust, metalGreen, concrete, concreteDark, road, line, sand, hesco, wire,
        locked, keycard, door, clay = Blocks.stained_hardened_clay, paint = Blocks.wool;   // aircraft paint: grey clay reads pink-brown

    Airport(long seed)
    {
        r = new Random(seed ^ 0x41495250L);
        metal = d("BlockMetal_2", Blocks.iron_block);
        metalRust = d("BlockMetal_3", Blocks.iron_block);
        metalGreen = d("BlockMetalWall", Blocks.iron_block);
        concrete = d("BlockStone_7", Blocks.stone);
        concreteDark = d("BlockStone_6", Blocks.stone);
        road = d("BlockRoad", Blocks.stone);
        line = d("BlockRoad_CenterLine", null);
        sand = d("BlockSandbagStack", Blocks.sandstone);
        hesco = BlockHesco.instance != null ? BlockHesco.instance : Blocks.sandstone;
        wire = d("BlockConcertinaWire", null);
        locked = d("Door_Emergency_1_Locked", Blocks.iron_door);
        keycard = d("BlockKeycardScreenMilitary", null);
        door = d("Door_Metal_3", Blocks.iron_door);
    }

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
        c.set(x, y, z, b, m, Canvas.PLAIN);                     // wool / clay colours never turn
    }

    private void door(int x, int y, int z, Block b, int front)
    {
        int m = front == S ? 3 : front == N ? 1 : front == E ? 2 : 0;
        c.set(x, y, z, b, m, Canvas.DOOR);
        c.set(x, y + 1, z, b, 8, Canvas.DOOR);
    }

    void build()
    {
        ground();
        runway();
        taxiway();
        apron();
        landsideRoad();
        perimeter();
        entry();
        hangars();
        tower();
        hq();
        armory();
        barracks();
        fuelFarm();
        aircraft();
        damage();
        c.validateProps(log);
    }

    // ---------------------------------------------------------------- ground

    /** Gravel with a few packed dirt patches (Decimation's dirt reads orange: kept rare), grass along the strip. */
    private void ground()
    {
        for (int z = 0; z < L; z++)
        {
            for (int x = 0; x < W; x++)
            {
                double n = noise(x, z);
                Block g = x >= 24 && x <= 33 ? (n > 0.3 ? Blocks.grass : Blocks.gravel) : n > 0.92 ? Blocks.dirt : Blocks.gravel;
                plain(x, 0, z, g);
            }
        }
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

    // ---------------------------------------------------------------- runway, taxiway, apron

    /**
     * Runway x 8..21 (14 wide), z 10..229: asphalt, white edge lines, threshold bars at both ends, dashed
     * centreline, concrete shoulders with flush lights every 12.
     */
    private void runway()
    {
        for (int z = 10; z <= 229; z++)
        {
            for (int x = RWY0 - 2; x <= RWY1 + 2; x++)
            {
                boolean shoulder = x < RWY0 || x > RWY1;
                if (shoulder)
                {
                    plain(x, 0, z, (z % 12 == 0) && (x == RWY0 - 2 || x == RWY1 + 2) ? Blocks.glowstone : concrete);
                    continue;
                }
                boolean edge = x == RWY0 || x == RWY1;
                boolean threshold = (z <= 17 || z >= 222) && (x - RWY0) % 2 == 1 && x != RWY1;
                boolean centre = (x == 14 || x == 15) && z > 22 && z < 217 && z % 9 < 5;
                if (edge || threshold || centre)
                {
                    colour(x, 0, z, Blocks.wool, 0);             // white paint (white clay reads pink in the pack)
                }
                else
                {
                    plain(x, 0, z, road);
                }
            }
        }
    }

    /** Parallel taxiway x 34..41 with a yellow centreline, three stub taxiways across to the runway. */
    private void taxiway()
    {
        for (int z = 14; z <= 225; z++)
        {
            for (int x = TWY0; x <= TWY1; x++)
            {
                if (x == 37 && z % 3 != 0)
                {
                    colour(x, 0, z, clay, 4);                    // yellow centreline
                }
                else
                {
                    plain(x, 0, z, road);
                }
            }
        }
        for (int s : new int[] {40, 116, 196})
        {
            for (int z = s; z <= s + 7; z++)
            {
                for (int x = RWY1 + 3; x < TWY0; x++)
                {
                    if (z == s + 3 && x % 3 != 0)
                    {
                        colour(x, 0, z, clay, 4);
                    }
                    else
                    {
                        plain(x, 0, z, road);
                    }
                }
            }
        }
    }

    /** Apron x 42..71, z 46..194: concrete with yellow parking lead in lines. */
    private void apron()
    {
        for (int z = 46; z <= 194; z++)
        {
            for (int x = APRON0; x <= APRON1; x++)
            {
                boolean lead = (z - 52) % 26 == 0 && x <= 58;
                if (lead)
                {
                    colour(x, 0, z, clay, 4);
                }
                else
                {
                    plain(x, 0, z, ((x + z) & 7) == 0 ? concreteDark : concrete);
                }
            }
        }
    }

    /** Landside road x 72..77 the whole length, from the entry gate south. */
    private void landsideRoad()
    {
        for (int z = 3; z <= 234; z++)
        {
            for (int x = ROAD0; x <= ROAD1; x++)
            {
                if (x == 74 && z % 4 < 2 && line != null)
                {
                    c.set(x, 0, z, line, 4);                    // centre dashes along z (turned with the canvas)
                }
                else
                {
                    plain(x, 0, z, road);
                }
            }
        }
    }

    // ---------------------------------------------------------------- perimeter and entry

    /** Chain link 3 high on the outermost row (posts every 4, wire on top), HESCO guard towers at the corners. */
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
        guardTower(3, 3, true);
        guardTower(W - 6, 3, true);
        guardTower(3, L - 6, false);
        guardTower(W - 6, L - 6, false);
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

    /** HESCO tower 3 x 3, 3 high, a sandbag parapet on top, a ladder on its inner face; a guard post on top. */
    private void guardTower(int x0, int z0, boolean north)
    {
        for (int x = x0; x <= x0 + 2; x++)
        {
            for (int z = z0; z <= z0 + 2; z++)
            {
                for (int y = 1; y <= 3; y++)
                {
                    plain(x, y, z, hesco);
                }
                boolean edge = x == x0 || x == x0 + 2 || z == z0 || z == z0 + 2;
                if (edge)
                {
                    plain(x, 4, z, sand);
                }
            }
        }
        // ladder up the middle of the inner face (south face for the north towers)
        int lz = north ? z0 + 3 : z0 - 1;
        for (int y = 1; y <= 4; y++)
        {
            c.set(x0 + 1, y, lz, Blocks.ladder, north ? 3 : 2);
        }
        c.clear(x0 + 1, 4, north ? z0 + 2 : z0);               // the step off the ladder onto the deck
        posts.add(new int[] {x0 + 1, 4, z0 + 1});
    }

    /**
     * Entry control point on the north side over the landside road: the gate in the fence (x 70..79), a guard
     * booth, a barrier arm down, a jersey chicane inside, sandbags, a sign.
     */
    private void entry()
    {
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
                c.set(x, 1, 4, boom, 0, Canvas.PLAIN);
            }
        }
        plain(71, 1, 4, metal);
        // booth west of the road
        for (int x = 64; x <= 67; x++)
        {
            for (int z = 4; z <= 7; z++)
            {
                boolean ring = x == 64 || x == 67 || z == 4 || z == 7;
                if (ring)
                {
                    plain(x, 1, z, concrete);
                    plain(x, 2, z, (x == 67 && z > 4 && z < 7) || (z == 4 && x > 64 && x < 67) ? Blocks.glass_pane : concrete);
                    plain(x, 3, z, concrete);
                }
                plain(x, 4, z, Blocks.double_stone_slab);
            }
        }
        door(65, 1, 7, door, S);
        posts.add(new int[] {66, 1, 5});
        // chicane: jersey rows across the road, gaps on alternate sides
        prop(72, 1, 10, "BlockBarrier", S);
        prop(74, 1, 10, "BlockBarrier", S);
        prop(76, 1, 14, "BlockBarrier", S);
        prop(74, 1, 14, "BlockBarrier", S);
        for (int z = 9; z <= 15; z++)
        {
            plain(80, 1, z, sand);
            plain(80, 2, z, sand);
        }
        prop(82, 1, 6, "BlockRussianRoadSignWarning", S);
        prop(68, 1, 12, "BlockSpotlight", S);
        prop(66, 1, 18, "BlockFlagPollUAHD", S);
        poi("entry", 74.5F, 3, 26, 180, 10);
        poi("main_road", 74.5F, 1, 20, 180, 10);                  // walkcheck starts here
    }

    // ---------------------------------------------------------------- landside buildings

    /** Three hangars x 78..105, doors west onto the road and the apron; the middle one burnt. */
    private void hangars()
    {
        int[] zs = {56, 90, 124};
        for (int i = 0; i < zs.length; i++)
        {
            hangar(zs[i], i);
        }
    }

    /**
     * Hangar 28 deep, 24 wide: metal walls 8 high, a vaulted roof (stepped, closed between steps), the door
     * opening on the west wall (2 in from each side, 7 high). Inside by kind: 0 a parked fighter, 1 burnt
     * (scorched walls, roof holes, debris, the military infected spawner), 2 a workshop and stores.
     */
    private void hangar(int z0, int kind)
    {
        int x0 = LAND0, x1 = LAND0 + 27, z1 = z0 + 23;
        for (int x = x0; x <= x1; x++)
        {
            for (int z = z0; z <= z1; z++)
            {
                plain(x, 0, z, concrete);
                boolean side = z == z0 || z == z1, back = x == x1, front = x == x0;
                int roof = 8 + Math.min(4, Math.min(z - z0, z1 - z) / 2);   // vault: 8 at the walls, 12 in the middle
                if (side || back)
                {
                    for (int y = 1; y < roof; y++)
                    {
                        if (kind == 1 && noise(x, z * 7 + y) > 0.7)
                        {
                            colour(x, y, z, clay, 15);               // scorched panels
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
                // roof skin (the vault steps by one block between rows: closed without fillers); holes when burnt
                if (!(kind == 1 && noise(x * 3, z) > 0.78))
                {
                    plain(x, roof, z, metalGreen);
                }
            }
        }
        prop(x0 + 2, 1, z0 + 1, "BlockHazardLight", S);
        posts.add(new int[] {x0 + 1, 1, z0 + 1});
        if (kind == 0)
        {
            fighter(x0 + 6, z0 + 12, true, false);                     // nose toward the door (west)
            prop(x1 - 2, 1, z0 + 2, "BlockMetalShelf", WE);
            prop(x1 - 2, 1, z0 + 5, "BlockWeaponCabinet", WE);
            prop(x1 - 2, 1, z1 - 5, "BlockMilitaryCrate", WE);
            prop(x1 - 2, 1, z1 - 2, "BlockAmmoCrateLarge", WE);
            prop(x1 - 4, 1, z1 - 2, "BlockToolbox", N);
        }
        else if (kind == 1)
        {
            for (int k = 0; k < 40; k++)
            {
                int x = x0 + 1 + r.nextInt(x1 - x0 - 1), z = z0 + 1 + r.nextInt(z1 - z0 - 1);
                colour(x, 0, z, clay, r.nextBoolean() ? 15 : 7);   // scorched floor
            }
            c.set(x1 - 4, 1, z0 + 12, Blocks.mob_spawner, 0, Canvas.PLAIN);
            spawners.add(new int[] {x1 - 4, 1, z0 + 12});
            prop(x0 + 8, 1, z0 + 6, "BlockTireStack", S);
            prop(x0 + 12, 1, z1 - 6, "BlockBarrel", S);
            prop(x0 + 15, 1, z0 + 9, "BlockSkeletonGround", E);
            prop(x0 + 10, 1, z0 + 16, "BlockBodyBagGreen", E);
        }
        else
        {
            for (int z = z0 + 3; z <= z1 - 3; z += 3)
            {
                prop(x1 - 2, 1, z, "BlockMetalShelf", WE);
            }
            prop(x0 + 10, 1, z0 + 8, "BlockMetalTable", S);
            prop(x0 + 10, 1, z0 + 14, "BlockMetalTable", S);
            prop(x0 + 14, 1, z0 + 4, "BlockMilitaryCrate", S);
            prop(x0 + 15, 1, z0 + 4, "BlockMilitaryCrate", S);
            prop(x0 + 14, 1, z1 - 4, "BlockCarePackage", S);
            prop(x0 + 6, 1, z1 - 4, "BlockPowerGenerator", S);
            prop(x0 + 6, 1, z0 + 4, "BlockTireStack", S);
        }
        poi("hangar_" + kind, x0 - 6, 4, z0 + 12, 270, 8);
    }

    /**
     * Control tower 8 x 8 at the apron's middle (x 80..87, z 160..167): concrete shaft with a ladder inside, the
     * glass cab from y 16 (radios, monitors, chairs), a roof with masts. Guard posts in the cab.
     */
    private void tower()
    {
        int x0 = 80, z0 = 160, x1 = 87, z1 = 167, cab = 16;
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
                plain(x, cab, z, concrete);                       // cab floor
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
        // door on the road side (west) and a ladder up the inside of the east wall into the cab
        door(x0, 1, z0 + 3, door, WE);
        for (int y = 1; y <= cab; y++)
        {
            c.set(x1 - 1, y, z0 + 4, Blocks.ladder, 4);
        }
        c.clear(x1 - 1, cab, z0 + 4);                              // the hatch
        prop(x0 + 1, cab + 1, z0 + 1, "BlockMilitaryRadioSmall", S);   // the big radio is drawn 1.7 deep
        prop(x0 + 3, cab + 1, z0 + 1, "BlockMonitor", S);
        prop(x0 + 4, cab + 1, z0 + 1, "BlockMonitor", S);
        prop(x0 + 3, cab + 1, z0 + 2, "BlockOfficeChair", N);
        prop(x0 + 1, cab + 1, z1 - 1, "BlockMilitaryRadioSmall", N);
        for (int y = cab + 5; y <= cab + 8; y++)
        {
            prop(x0 + 1, y, z0 + 1, "BlockRadioTower", S);
        }
        prop(x1 - 1, cab + 5, z1 - 1, "BlockSpotlight", WE);
        posts.add(new int[] {x0 + 2, cab + 1, z0 + 4});
        posts.add(new int[] {x0 + 5, cab + 1, z1 - 2});
        poi("tower", x0 - 18, 8, z0 + 4, 270, 15);
        poi("tower_cab", x0 + 4.5F, cab + 2.6F, z1 - 1.5F, 180, 10);
    }

    /**
     * HQ 18 x 16 (x 88..105, z 170..185): concrete, windows barred, the locked door on the road side (west) with
     * the military keycard screen beside it; inside: ops desks, radios, weapon cabinets, large ammo cases,
     * military crates (top military loot), a wall flag.
     */
    private void hq()
    {
        int x0 = 88, z0 = 170, x1 = 105, z1 = 185;
        box(x0, z0, x1, z1, 5, concrete);
        for (int z = z0 + 2; z <= z1 - 2; z += 3)
        {
            plain(x1, 3, z, Blocks.iron_bars);
        }
        door(x0, 1, z0 + 7, locked, WE);
        if (keycard != null)
        {
            c.prop(x0 - 1, 2, z0 + 9, keycard, WE);
        }
        for (int x = x0 + 3; x <= x0 + 9; x += 3)
        {
            prop(x, 1, z0 + 3, "BlockMetalTable", S);
            prop(x, 2, z0 + 3, "BlockMonitor", S);
            prop(x, 1, z0 + 4, "BlockOfficeChair", N);
        }
        prop(x0 + 2, 1, z1 - 2, "BlockMilitaryRadio", N);
        for (int z = z0 + 2; z <= z1 - 3; z += 2)
        {
            prop(x1 - 1, 1, z, z % 4 == 0 ? "BlockWeaponCabinet" : "BlockAmmoCrateLarge", WE);
        }
        prop(x0 + 8, 1, z1 - 2, "BlockMilitaryCrate", N);
        prop(x0 + 9, 1, z1 - 2, "BlockMilitaryCrate", N);
        prop(x0 + 12, 1, z1 - 2, "BlockAmmoCrate", N);
        posts.add(new int[] {x0 - 2, 1, z0 + 5});
        poi("hq", x0 - 8, 3, z0 + 7.5F, 270, 8);
        poi("hq_inside", x0 + 2, 2.6F, z0 + 7.5F, 270, 15);
    }

    /** Armory 10 x 10 (x 96..105, z 190..199) behind a HESCO ring: locked door, keycard, cabinets and cases. */
    private void armory()
    {
        int x0 = 96, z0 = 190, x1 = 105, z1 = 199;
        box(x0, z0, x1, z1, 4, concreteDark);
        door(x0, 1, z0 + 4, locked, WE);
        if (keycard != null)
        {
            c.prop(x0 - 1, 2, z0 + 6, keycard, WE);
        }
        for (int z = z0 + 2; z <= z1 - 2; z++)
        {
            prop(x1 - 1, 1, z, z % 2 == 0 ? "BlockWeaponCabinet" : "BlockMilitaryCrate", WE);
        }
        prop(x0 + 3, 1, z1 - 1, "BlockAmmoCrateLarge", S);
        prop(x0 + 6, 1, z1 - 1, "BlockAmmoCrateLarge", S);
        // the HESCO ring 2 out, open in front of the door
        for (int x = x0 - 2; x <= x1; x++)
        {
            for (int z : new int[] {z0 - 2, z1 + 2})
            {
                plain(x, 1, z, hesco);
                plain(x, 2, z, hesco);
            }
        }
        for (int z = z0 - 2; z <= z1 + 2; z++)
        {
            if (z < z0 + 2 || z > z0 + 6)
            {
                plain(x0 - 2, 1, z, hesco);
                plain(x0 - 2, 2, z, hesco);
            }
        }
        poi("armory", x0 - 8, 3, z0 + 4.5F, 270, 8);
    }

    /** A walled box x0..x1, z0..z1, h high (floor concrete, flat roof). */
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
        for (int x = x0 + 3; x <= x1 - 3; x += 4)
        {
            for (int z = z0 + 3; z <= z1 - 3; z += 4)
            {
                prop(x, h - 1, z, "BlockLight", S);
            }
        }
    }

    /** Three barracks huts (x 80..107, z 206..230): plywood walls, bunks, footlockers. */
    private void barracks()
    {
        for (int k = 0; k < 3; k++)
        {
            int x0 = 80 + k * 9, x1 = x0 + 6, z0 = 206, z1 = 228;
            for (int x = x0; x <= x1; x++)
            {
                for (int z = z0; z <= z1; z++)
                {
                    plain(x, 0, z, Blocks.planks);
                    boolean ring = x == x0 || x == x1 || z == z0 || z == z1;
                    if (ring)
                    {
                        for (int y = 1; y <= 3; y++)
                        {
                            c.set(x, y, z, Blocks.planks, 1);
                        }
                    }
                    c.set(x, 4, z, Blocks.wooden_slab, 1);
                }
            }
            door(x0 + 3, 1, z0, door, N);
            for (int z = z0 + 3; z <= z1 - 3; z += 4)
            {
                bunk(x0 + 1, z);
                bunk(x1 - 1, z);
                prop(x0 + 2, 1, z, "BlockStorageCrate", E);
            }
        }
        poi("barracks", 78, 4, 200, 135, 12);
    }

    private void bunk(int x, int z)
    {
        c.set(x, 1, z, Blocks.bed, 0);
        c.set(x, 1, z + 1, Blocks.bed, 8);
        c.set(x, 2, z, Blocks.wooden_slab, 9);
        c.set(x, 2, z + 1, Blocks.wooden_slab, 9);
        c.set(x, 3, z, Blocks.carpet, 8);
        c.set(x, 3, z + 1, Blocks.carpet, 8);
    }

    /** Fuel farm (x 82..105, z 14..44): four bladders in a sandbag berm, drum rows, a pump, its road loop. */
    private void fuelFarm()
    {
        for (int x = 80; x <= 106; x++)
        {
            for (int z = 12; z <= 46; z++)
            {
                boolean berm = x == 80 || x == 106 || z == 12 || z == 46;
                if (berm && !(z == 12 && x >= 84 && x <= 88))
                {
                    plain(x, 1, z, sand);
                }
            }
        }
        for (int k = 0; k < 4; k++)
        {
            int x0 = 83 + (k % 2) * 12, z0 = 15 + (k / 2) * 15;
            for (int x = x0; x <= x0 + 8; x++)
            {
                for (int z = z0; z <= z0 + 10; z++)
                {
                    boolean rim = x == x0 || x == x0 + 8 || z == z0 || z == z0 + 10;
                    colour(x, 1, z, Blocks.wool, rim ? 7 : 15);   // dark grey rubber bladder
                    if (!rim)
                    {
                        c.set(x, 2, z, Blocks.carpet, 15);
                    }
                }
            }
        }
        for (int x = 84; x <= 102; x += 2)
        {
            prop(x, 1, 44, "BlockBarrel", S);
        }
        prop(96, 1, 13, "BlockPowerGenerator", S);
        prop(100, 1, 13, "BlockHazardLight", S);
        poi("fuel", 70, 6, 30, 270, 20);
    }

    // ---------------------------------------------------------------- aircraft

    /** Parked aircraft on the apron: three fighters in a row, the cargo plane, the helicopter pad. */
    private void aircraft()
    {
        for (int k = 0; k < 3; k++)
        {
            fighter(APRON0 + 4, 54 + k * 13, true, false);
        }
        cargo(APRON0 + 2, 120);
        // helipad: concrete square with a white H, Decimation's helicopter wreck on it
        int px = 50, pz = 160;
        for (int x = px; x <= px + 15; x++)
        {
            for (int z = pz; z <= pz + 15; z++)
            {
                boolean h = (x == px + 5 || x == px + 10) && z >= pz + 4 && z <= pz + 11 || z == pz + 7 && x >= px + 5 && x <= px + 10;
                if (h)
                {
                    colour(x, 0, z, Blocks.wool, 0);
                }
                else
                {
                    plain(x, 0, z, concreteDark);
                }
            }
        }
        prop(px + 8, 1, pz + 8, "BlockWreckageMilitary3", S);
        poi("apron", 56, 8, 104, 0, 20);
    }

    /**
     * A fighter built from blocks, nose west (toward the taxiway) or east: 14 long, 9 wide, 5 high. Grey clay
     * fuselage on gear legs, glass canopy, slab wings and tailplanes, a fin, coal intakes and nozzle.
     * Wrecked: one wing gone, the tail broken off and turned, scorched patches.
     */
    void fighter(int x0, int zc, boolean noseWest, boolean wrecked)
    {
        for (int u = 0; u <= 13; u++)
        {
            int x = noseWest ? x0 + u : x0 + 13 - u;
            boolean tail = u >= 10;
            if (wrecked && tail)
            {
                continue;                                     // drawn apart below
            }
            int body = wrecked && r.nextInt(4) == 0 ? 15 : (u == 0 ? 7 : 8);
            colour(x, 2, zc, paint, body);
            if (u >= 3 && u <= 9)
            {
                colour(x, 2, zc - 1, paint, u == 4 ? 15 : 8);
                colour(x, 2, zc + 1, paint, u == 4 ? 15 : 8);
            }
            if (u >= 2 && u <= 4)
            {
                plain(x, 3, zc, Blocks.glass);
            }
            else if (u >= 5 && u <= 9)
            {
                colour(x, 3, zc, paint, 8);
            }
            if (u == 2 || u == 8)
            {
                for (int v : u == 2 ? new int[] {0} : new int[] {-1, 1})
                {
                    plain(x, 1, zc + v, Blocks.cobblestone_wall);   // gear legs
                }
            }
            if (u >= 5 && u <= 8)
            {
                int span = u == 5 ? 3 : 4;
                for (int v = 2; v <= span; v++)
                {
                    c.set(x, 2, zc - v, Blocks.stone_slab, 8);
                    if (!wrecked)
                    {
                        c.set(x, 2, zc + v, Blocks.stone_slab, 8);
                    }
                }
            }
            if (u >= 11)
            {
                for (int v = 2; v <= 3; v++)
                {
                    c.set(x, 2, zc - v, Blocks.stone_slab, 8);
                    c.set(x, 2, zc + v, Blocks.stone_slab, 8);
                }
                colour(x, 3, zc, paint, 7);
                if (u >= 12)
                {
                    colour(x, 4, zc, paint, 7);
                }
            }
            if (u == 13)
            {
                plain(x, 2, zc, Blocks.coal_block);
            }
        }
        if (wrecked)
        {
            // the tail section lies off to one side, turned, scorched
            int tx = noseWest ? x0 + 12 : x0 + 1;
            for (int k = 0; k < 3; k++)
            {
                colour(tx, 1, zc + 4 + k, paint, k == 1 ? 15 : 7);
            }
            colour(tx, 2, zc + 5, paint, 7);
            colour(tx, 3, zc + 5, paint, 15);
            for (int k = 0; k < 30; k++)
            {
                int x = x0 - 2 + r.nextInt(18), z = zc - 5 + r.nextInt(11);
                if (c.get(x, 1, z) == null)
                {
                    colour(x, 0, z, clay, r.nextInt(3) == 0 ? 7 : 15);
                }
            }
        }
    }

    /**
     * Cargo plane nose west: 22 long, 24 span, fuselage 5 wide and 5 high, hollow (3 x 3 inside), the rear
     * ramp down to the ground (walk in), a high wing with four engine pods, a fin and tailplane. Loot inside.
     */
    private void cargo(int x0, int zc)
    {
        for (int u = 0; u <= 21; u++)
        {
            int x = x0 + u;
            int half = u <= 1 ? 1 : 2;                         // the nose is narrower
            for (int v = -half; v <= half; v++)
            {
                boolean wall = Math.abs(v) == half;
                for (int y = 1; y <= 5; y++)
                {
                    boolean shell = wall || y == 1 || y == 5 || u == 0;
                    boolean ramp = u >= 19 && !wall && y > 1;     // open rear
                    if (shell && !ramp)
                    {
                        colour(x, y, zc + v, paint, (y == 3 && wall && u % 3 == 1) ? 15 : 8);   // dark windows
                    }
                }
            }
            if (u >= 1 && u <= 2)
            {
                for (int v = -1; v <= 1; v++)
                {
                    plain(x, 4, zc + v, Blocks.glass);           // cockpit glass
                }
            }
        }
        // ramp: stairs down from the floor at the open tail
        for (int v = -1; v <= 1; v++)
        {
            c.set(x0 + 21, 1, zc + v, Blocks.stone_stairs, 0);
        }
        // high wing on top, four engines under it
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
        // fin and tailplane
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
        // cargo inside
        prop(x0 + 5, 2, zc - 1, "BlockMilitaryCrate", S);
        prop(x0 + 6, 2, zc - 1, "BlockMilitaryCrate", S);
        prop(x0 + 9, 2, zc + 1, "BlockMilitaryCrate", N);         // a care package is wider than the hold
        prop(x0 + 13, 2, zc - 1, "BlockAmmoCrate", S);
        poi("cargo", x0 + 30, 4, zc, 90, 8);
    }

    // ---------------------------------------------------------------- damage and defence

    /**
     * Half wrecked: a fighter crashed on the runway (in two, scorched), scorch marks on the apron and taxiway,
     * barricades across the taxiway, sandbag positions at the apron's corners with garrison posts.
     */
    private void damage()
    {
        int crashZ = 100 + r.nextInt(40);
        fighter(RWY0 + 1, crashZ, r.nextBoolean(), true);
        prop(RWY1 + 3, 1, crashZ + 6, "BlockSkeletonGround", S);
        for (int k = 0; k < 4; k++)
        {
            int sx = TWY0 + r.nextInt(APRON1 - TWY0 - 6), sz = 60 + r.nextInt(120);
            scorch(sx, sz);
        }
        // barricades across the taxiway with a gap
        for (int z : new int[] {96, 150})
        {
            prop(TWY0, 1, z, "BlockBarrier", S);
            prop(TWY0 + 2, 1, z, "BlockBarrier", S);
            prop(TWY0 + 5, 1, z, "BlockHedgehog", S);
        }
        // sandbag positions at the apron's corners
        for (int[] p : new int[][] {{APRON0, 46}, {APRON1 - 3, 46}, {APRON0, 191}, {APRON1 - 3, 191}})
        {
            for (int x = p[0]; x <= p[0] + 3; x++)
            {
                for (int z = p[1]; z <= p[1] + 3; z++)
                {
                    boolean ring = x == p[0] || x == p[0] + 3 || z == p[1] || z == p[1] + 3;
                    if (ring && !(x == p[0] + 1 && (z == p[1] || z == p[1] + 3)))
                    {
                        plain(x, 1, z, sand);
                        plain(x, 2, z, sand);
                    }
                }
            }
            posts.add(new int[] {p[0] + 2, 1, p[1] + 2});
        }
        poi("runway_crash", RWY1 + 12, 6, crashZ - 14, 30, 15);
        poi("top", W / 2.0F, 60, L / 2.0F, 180, 89);
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

    private void poi(String name, float x, float y, float z, float yaw, float pitch)
    {
        poi.put(name, new float[] {x, y, z, yaw, pitch});
    }
}

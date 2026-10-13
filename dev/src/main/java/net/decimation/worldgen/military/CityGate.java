package net.decimation.worldgen.military;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;

/**
 * The gates in the fence between a wasteland city sector and the city beside it (city/WastelandFence; user,
 * 14 Oktober 2026). Each gate has a LOOK (toll gate, military checkpoint, container wall, quarantine, police
 * roadblock) and a STATE: 8 in 10 are shut so neither players nor mobs get through (barbed wire with
 * landmines, a welded steel wall, a burnt bus plugging it, or a HESCO wall; user, 14 Oktober 2026: the tank
 * ditch "really bad idea ... remove it"), 2 in 10
 * stand open because they broke (the look smashed: arms torn off, burnt road, bodies, loose wire).
 * <p>
 * Final canvas W x H x L: x = across the street (0..15, sidewalks 0..2 and 13..15, road 3..12); z = d + BACK,
 * d the distance from the fence line (0 = the line, -1 / -2 the crossing street's first two road rows, which
 * only the bus and the compact toll use; 1..8 the street chunk in front); y = height above the street
 * surface (0 = the surface). Arriving traffic comes from +z: signs and lights face south. A crossing whose
 * street chunk in front cannot take a gate (a canal) gets the COMPACT toll gate on the line and the
 * crossing's two rows only (user: no plain gate on a connector).
 * Looks are drawn on a small canvas (y 0 = surface, z = d) and copied in; props checked by validateProps.
 */
public final class CityGate
{
    public static final String[] LOOKS = {"toll", "checkpoint", "containers", "quarantine", "police"};
    public static final String[] STATES = {"open", "wire", "steel", "bus", "hesco"};
    public static final int OPEN = 0, WIRE = 1, STEEL = 2, BUS = 3, HESCO = 4;
    public static final int W = 16, BACK = 2, L = 9 + BACK, H = 9;
    static final int E = 2, S = 3, WE = 4, N = 5;

    /** Marker for a landmine: Slices spawns Decimation's landmine entity in its cell instead of a block. */
    public static final Block MINE = new Block(Material.air)
    {
    };

    private static final Map<Integer, Canvas> CANVAS = new java.util.HashMap<Integer, Canvas>();
    private static final Map<Integer, List<String>> LOGS = new java.util.HashMap<Integer, List<String>>();

    private Canvas c = new Canvas(W, 9, 9);
    private final Canvas f = new Canvas(W, H, L);
    private final List<String> log = new ArrayList<String>();
    private final Block metal, road;
    private final int look, state;

    public static String name(int look, int state)
    {
        return (look < 0 ? "compact toll" : LOOKS[look]) + "/" + STATES[state];
    }

    private static int key(int look, int state)
    {
        return (look + 1) * STATES.length + state;
    }

    static synchronized Canvas canvas(int look, int state)
    {
        int k = key(look, state);
        Canvas cv = CANVAS.get(k);
        if (cv == null)
        {
            CityGate g = new CityGate(look, state);
            cv = g.f;
            CANVAS.put(k, cv);
            LOGS.put(k, g.log);
            if (!g.log.isEmpty())
            {
                cpw.mods.fml.common.FMLLog.info("[deciworldgen] gate %s: %s", name(look, state), g.log);
            }
        }
        return cv;
    }

    /** Clashes Canvas.validateProps removed (dev test). */
    public static synchronized List<String> log(int look, int state)
    {
        canvas(look, state);
        return LOGS.get(key(look, state));
    }

    /** Camera points: a view from the street in front and one from above (canvas coordinates). */
    static Map<String, float[]> points()
    {
        Map<String, float[]> p = new LinkedHashMap<String, float[]>();
        p.put("front", new float[] {7.5F, 3.0F, 15.0F + BACK, 180F, 12F});
        p.put("top", new float[] {7.5F, 16.0F, 9.0F + BACK, 180F, 60F});
        return p;
    }

    private CityGate(int look, int state)
    {
        this.look = look;
        this.state = state;
        metal = d("BlockMetal_2", Blocks.iron_block);
        road = d("BlockRoad", Blocks.stone);
        ground();
        if (look >= 0)
        {
            switch (look)
            {
                case 0:  toll(); break;
                case 1:  checkpoint(); break;
                case 2:  containers(); break;
                case 3:  quarantine(); break;
                default: police(); break;
            }
        }
        copy();
        if (look < 0)
        {
            compactToll();
        }
        switch (state)
        {
            case OPEN:  smash(); break;
            case WIRE:  wire(); break;
            case STEEL: steel(); break;
            case BUS:   if (look < 0) steel(); else bus(); break;   // no room for a bus beside the booth
            default:    hesco(); break;
        }
        f.validateProps(log);
    }

    /** The look canvas into the final one; the crossing's two road rows; the compact toll on those and the line. */
    private void copy()
    {
        for (int z = 0; z < L; z++)
        {
            for (int a = 0; a < W; a++)
            {
                int d = z - BACK;
                if (d <= 0 && walk(a))
                {
                    continue;                                   // the line's fence, the crossing's corners
                }
                if (d < 0)
                {
                    f.set(a, 0, z, road, 0, Canvas.PLAIN);
                    continue;
                }
                for (int y = 0; y < c.h; y++)
                {
                    Block b = c.get(a, y, d);
                    if (b != null)
                    {
                        f.set(a, y, z, b, c.meta(a, y, d), c.kind(a, y, d));
                    }
                }
            }
        }
        c = null;
    }

    // final canvas helpers: h = height above the surface (0 = the surface), d = distance from the line
    private Block at(int a, int h, int d)
    {
        return f.get(a, h, d + BACK);
    }

    private void put(int a, int h, int d, Block b, int m)
    {
        f.set(a, h, d + BACK, b, m, Canvas.PLAIN);
    }

    private void putProp(int a, int h, int d, String name, int facing)
    {
        Block b = d(name, null);
        if (b != null)
        {
            f.prop(a, h, d + BACK, b, facing);
        }
    }

    private void clearColumn(int a, int d, int fromH)
    {
        for (int h = fromH; h < H; h++)
        {
            f.clear(a, h, d + BACK);
        }
    }

    /**
     * A surface cell with nothing standing in it: no block at heights 1 and 2, no prop's DRAWN box
     * (PropBoxes) reaching into it more than the 0.08 validateProps forgives (a jersey barrier is drawn 2
     * long, a car 7). Props without a known box count as their own cell.
     */
    private boolean free(int a, int d)
    {
        if (at(a, 0, d) == null || at(a, 1, d) != null || at(a, 2, d) != null)
        {
            return false;
        }
        int cy = 1, cz = d + BACK;
        for (int z = cz - 5; z <= cz + 5; z++)
        {
            for (int y = cy - 1; y <= cy + 1; y++)
            {
                for (int x = a - 5; x <= a + 5; x++)
                {
                    if (f.kind(x, y, z) != Canvas.PROP || f.get(x, y, z) == null)
                    {
                        continue;
                    }
                    float[] b = PropBoxes.box(f.get(x, y, z), f.meta(x, y, z));
                    if (b == null)
                    {
                        b = new float[] {0, 1, 0, 1, 0, 1};
                    }
                    if (over(x + b[0], x + b[1], a, a + 1) && over(y + b[2], y + b[3], cy, cy + 2)
                        && over(z + b[4], z + b[5], cz, cz + 1))
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean over(float a0, float a1, float b0, float b1)
    {
        return Math.min(a1, b1) - Math.max(a0, b0) > 0.08f;
    }

    private static long hash(int a, int b, int salt)
    {
        long h = (a * 73856093L) ^ (b * 19349663L) ^ (salt * 83492791L);
        h ^= h >>> 13;
        h *= 0x2545F4914F6CDD1DL;
        return (h >>> 33) & 0x7FFFFFFFL;
    }

    // ---------------------------------------------------------------- states

    /**
     * Open, because it broke: arms torn off, scorched road in the opening, a body in the road and one on a
     * sidewalk, a knocked barrier, wire left at both ends of the line.
     */
    private void smash()
    {
        for (int z = 0; z < L; z++)
        {
            for (int y = 0; y < H; y++)
            {
                for (int a = 0; a < W; a++)
                {
                    if (f.get(a, y, z) == BlockBoomArm.instance && f.meta(a, y, z) < 2)
                    {
                        f.clear(a, y, z);                       // the lowered arm is gone
                    }
                }
            }
        }
        Block burnt = Blocks.stained_hardened_clay;
        for (int d = -2; d <= 4; d++)
        {
            for (int a = 3; a <= 12; a++)
            {
                if (at(a, 0, d) == road && hash(a, d, 1) % 10 < 4)
                {
                    put(a, 0, d, burnt, 15);                    // black: scorched asphalt
                }
            }
        }
        // bodies on spots checked against each look's drawn props by hand (a skeleton lies 2 long toward +d,
        // a bag 2 long across at E, along d at S); validateProps would drop one that still clashed
        if (look < 0)
        {
            putProp(3, 1, -1, "BlockSkeletonGround", E);       // only the line and the crossing's rows are ours
            return;
        }
        int[][] skeleton = {{4, 6}, {10, 3}, {12, 5}, {3, 7}, {4, 5}};
        int[][] bag = {{1, 3}, {12, 2}, {3, 5}, null, null};
        int[] bagFacing = {E, S, S, S, S};
        int[][] knocked = {null, {9, 1}, {9, 1}, {9, 1}, null};
        putProp(skeleton[look][0], 1, skeleton[look][1], "BlockSkeletonGround", S);
        if (bag[look] != null)
        {
            putProp(bag[look][0], 1, bag[look][1], "BlockBodyBagGreen", bagFacing[look]);
        }
        if (knocked[look] != null && free(knocked[look][0], knocked[look][1]))
        {
            putProp(knocked[look][0], 1, knocked[look][1], "BlockHazardbarrier", E);
        }
        if (free(3, 0))
        {
            putProp(3, 1, 0, "BlockConcertinaWire", S);
        }
        if (free(12, 0))
        {
            putProp(12, 1, 0, "BlockConcertinaWire", S);
        }
    }

    /** The compact toll's booth island on the line (closures leave it standing). */
    private boolean island(int a)
    {
        return look < 0 && a >= 6 && a <= 9;
    }

    /** Clears the line's road cells (the look's barriers there) for a closure. */
    private void clearLine()
    {
        for (int a = 3; a <= 12; a++)
        {
            if (!island(a))
            {
                clearColumn(a, 0, 1);
            }
        }
    }

    /**
     * Barbed wire with landmines (user: "version one using landmine"): the chain link fence carried across
     * the road (posts every 4, iron bars 3 high, wire on top), wire rolls along both sides of it, landmines
     * on the road in front on both sides (about one cell in three; they go off under players).
     */
    private void wire()
    {
        clearLine();
        Block wireB = d("BlockConcertinaWire", null);
        for (int a = 3; a <= 12; a++)
        {
            if (island(a))
            {
                continue;
            }
            boolean post = a % 4 == 1;
            for (int h = 1; h <= 3; h++)
            {
                put(a, h, 0, post ? metal : Blocks.iron_bars, 0);
            }
            if (post)
            {
                put(a, 4, 0, metal, 0);
            }
            else if (wireB != null)
            {
                f.prop(a, 4, BACK, wireB, S);
            }
        }
        for (int a = 3; a <= 12; a++)
        {
            for (int d : look < 0 ? new int[] {} : new int[] {1, -1})   // a compact gate owns no front rows
            {
                if (free(a, d))
                {
                    putProp(a, 1, d, "BlockConcertinaWire", S);
                }
            }
            // the compact gate's one free row (between the line and its arms) is all mines
            for (int d : look < 0 ? new int[] {-1} : new int[] {2, 3, 4, -2})
            {
                if (free(a, d) && (look < 0 ? hash(a, d, 2) % 2 == 0 : hash(a, d, 2) % 3 == 0))
                {
                    put(a, 1, d, MINE, 0);
                }
            }
        }
    }

    /** Welded steel wall: plates 3 high across the opening (a ragged fourth here and there), sandbags at its foot, a warning sign. */
    private void steel()
    {
        clearLine();
        Block[] plates = {metal, d("BlockMetal_3", Blocks.iron_block), d("BlockMetalWall", Blocks.iron_block)};
        for (int a = 3; a <= 12; a++)
        {
            if (island(a))
            {
                continue;
            }
            for (int h = 1; h <= 3; h++)
            {
                put(a, h, 0, plates[(int) (hash(a, h, 3) % 3)], 0);
            }
            if (hash(a, 4, 3) % 2 == 0)
            {
                put(a, 4, 0, plates[1], 0);
            }
        }
        if (look < 0)
        {
            return;                                           // the front rows are not the compact gate's
        }
        Block sand = d("BlockSandbagStack", Blocks.sandstone);
        for (int a = 3; a <= 12; a++)
        {
            if (free(a, 1) && hash(a, 1, 4) % 2 == 0)
            {
                put(a, 1, 1, sand, 0);
            }
        }
        if (free(7, 2) && free(8, 2) && free(6, 2))
        {
            putProp(7, 1, 2, "BlockRussianRoadSignWarning", S);   // drawn about 2 wide: room both sides
        }
    }

    /**
     * A burnt bus plugging the opening: 10 long across the road, 3 deep (the line and the crossing's two road
     * rows), body 3 high of yellow and scorched clay, wheels, windows broken out at the middle height (one
     * block: nobody fits through), wire on its roof.
     */
    private void bus()
    {
        for (int a = 3; a <= 12; a++)
        {
            for (int d = -2; d <= 0; d++)
            {
                clearColumn(a, d, 1);
            }
        }
        Block clay = Blocks.stained_hardened_clay;
        for (int a = 3; a <= 12; a++)
        {
            for (int d = -2; d <= 0; d++)
            {
                boolean side = d != -1, end = a == 3 || a == 12;
                boolean wheel = side && (a == 4 || a == 5 || a == 10 || a == 11);
                int body = hash(a, d, 5) % 10 < 4 ? 15 : 4;     // scorched black or school bus yellow
                put(a, 1, d, wheel ? Blocks.coal_block : clay, wheel ? 0 : body);
                if (side || end)
                {
                    boolean window = !end && hash(a, d, 6) % 3 != 0;
                    put(a, 2, d, window ? Blocks.glass_pane : clay, window ? 0 : body);
                    if (window && hash(a, d, 7) % 2 == 0)
                    {
                        f.clear(a, 2, d + BACK);         // broken out
                    }
                }
                put(a, 3, d, clay, hash(a, d, 8) % 10 < 5 ? 15 : 4);
            }
        }
        Block wireB = d("BlockConcertinaWire", null);
        for (int a = 4; a <= 11; a += 3)
        {
            if (wireB != null)
            {
                f.prop(a, 4, BACK - 1, wireB, S);
            }
        }
    }

    /** HESCO wall: our HESCO block 3 high across the opening, wire on top, a sandbag sentry post and a crate in front. */
    private void hesco()
    {
        clearLine();
        Block h = BlockHesco.instance != null ? BlockHesco.instance : d("BlockMilitaryBarrier", Blocks.sandstone);
        Block wireB = d("BlockConcertinaWire", null);
        for (int a = 3; a <= 12; a++)
        {
            if (island(a))
            {
                continue;                                     // the compact toll's booth island
            }
            for (int y = 1; y <= 3; y++)
            {
                put(a, y, 0, h, 0);
            }
            if (wireB != null)
            {
                f.prop(a, 4, BACK, wireB, S);
            }
        }
        if (look < 0)
        {
            return;
        }
        Block sand = d("BlockSandbagStack", Blocks.sandstone);
        for (int a = 3; a <= 4; a++)
        {
            if (free(a, 1))
            {
                put(a, 1, 1, sand, 0);
            }
        }
        if (free(12, 1))
        {
            putProp(12, 1, 1, "BlockMilitaryCrate", S);
        }
    }

    /**
     * Compact toll gate (a crossing where the street in front cannot take the full one): a glass booth on an
     * island in the middle of the line and the crossing's two rows, a canopy over both lanes resting on the
     * booth and the fence posts, an arm down across each lane on the far side, a red lamp.
     */
    private void compactToll()
    {
        Block door = d("Door_Metal_3", Blocks.iron_door);
        for (int d = -2; d <= 0; d++)
        {
            for (int a = 6; a <= 9; a++)
            {
                boolean side = a == 6 || a == 9, end = d == -2 || d == 0;
                put(a, 0, d, Blocks.double_stone_slab, 0);
                put(a, 1, d, metal, 0);
                for (int y = 2; y <= 3; y++)
                {
                    if (side && end)
                    {
                        put(a, y, d, metal, 0);
                    }
                    else if (side || end)
                    {
                        f.set(a, y, d + BACK, Blocks.glass_pane, 0);
                    }
                }
                put(a, 4, d, Blocks.double_stone_slab, 0);
            }
            for (int a = 7; a <= 8; a++)
            {
                f.clear(a, 1, d + BACK);                       // inside the booth
            }
        }
        f.set(7, 1, -2 + BACK, door, 1, Canvas.DOOR);         // door to the far side
        f.set(7, 2, -2 + BACK, door, 8, Canvas.DOOR);
        for (int a = 3; a <= 12; a++)
        {
            for (int d = -2; d <= 0; d++)
            {
                put(a, 5, d, Blocks.double_stone_slab, 0);       // canopy over both lanes, on the booth
            }
        }
        Block boom = BlockBoomArm.instance;
        if (boom != null && state != OPEN)
        {
            for (int a = 3; a <= 5; a++)
            {
                put(a, 1, -2, boom, 0);
            }
            for (int a = 10; a <= 12; a++)
            {
                put(a, 1, -2, boom, 0);
            }
        }
        putProp(6, 5 + 1, -1, "BlockRedlight", S);
    }

    // ---------------------------------------------------------------- looks (y 0 = surface, z = d)

    private static Block d(String name, Block fallback)
    {
        Block b = Block.getBlockFromName("deci:" + name);
        return b == null || b == Blocks.air ? fallback : b;
    }

    private void prop(int a, int y, int z, String name, int facing)
    {
        Block b = d(name, null);
        if (b != null)
        {
            c.prop(a, y, z, b, facing);
        }
    }

    private void plain(int a, int y, int z, Block b, int m)
    {
        c.set(a, y, z, b, m, Canvas.PLAIN);
    }

    private static boolean walk(int a)
    {
        return a < 3 || a > 12;
    }

    /** The street surface: sidewalks of double stone slab, road blocks; on the line only the road. */
    private void ground()
    {
        for (int z = 0; z < L; z++)
        {
            for (int a = 0; a < W; a++)
            {
                if (z == 0 && walk(a))
                {
                    continue;
                }
                plain(a, 0, z, walk(a) ? Blocks.double_stone_slab : road, 0);
            }
        }
    }

    private void door(int a, int y, int z, Block b, int front)
    {
        int m = front == S ? 3 : front == N ? 1 : front == E ? 2 : 0;
        c.set(a, y, z, b, m, Canvas.DOOR);
        c.set(a, y + 1, z, b, 8, Canvas.DOOR);
    }

    // ---------------------------------------------------------------- 0 toll gate

    /**
     * Toll plaza: an island in the middle (x 6..9) with a glass booth, two lanes (3..5, 10..12) under a
     * canopy on four columns, a barrier arm across each lane at the line end (the west one down, the east
     * one raised), a red lamp on the arm housing, delineator posts at the island's nose, a road sign.
     */
    private void toll()
    {
        Block boom = BlockBoomArm.instance;
        Block door = d("Door_Metal_3", Blocks.iron_door);
        for (int z = 1; z <= 6; z++)
        {
            for (int a = 6; a <= 9; a++)
            {
                plain(a, 0, z, Blocks.double_stone_slab, 0);       // the island
            }
        }
        for (int z = 2; z <= 5; z++)
        {
            for (int a = 6; a <= 9; a++)
            {
                boolean side = a == 6 || a == 9, end = z == 2 || z == 5;
                if (!side && !end)
                {
                    continue;
                }
                boolean corner = side && end;
                plain(a, 1, z, metal, 0);
                for (int y = 2; y <= 3; y++)
                {
                    if (corner)
                    {
                        plain(a, y, z, metal, 0);
                    }
                    else
                    {
                        c.set(a, y, z, Blocks.glass_pane, 0);
                    }
                }
            }
            for (int a = 6; a <= 9; a++)
            {
                plain(a, 4, z, Blocks.double_stone_slab, 0);       // booth roof
            }
        }
        door(7, 1, 5, door, S);
        prop(8, 1, 3, "BlockOfficeChair", WE);
        prop(7, 1, 3, "BlockRadio1", E);
        // canopy on four columns over both lanes
        int[][] cols = {{1, 1}, {1, 6}, {14, 1}, {14, 6}};
        for (int[] p : cols)
        {
            for (int y = 1; y <= 4; y++)
            {
                plain(p[0], y, p[1], metal, 0);
            }
        }
        for (int z = 1; z <= 6; z++)
        {
            for (int a = 1; a <= 14; a++)
            {
                plain(a, 5, z, Blocks.double_stone_slab, 0);         // the canopy roof
            }
        }
        prop(4, 4, 3, "BlockLight", S);
        prop(11, 4, 3, "BlockLight", S);
        // arm housings at the line end of the island, the arms across the lanes
        plain(6, 1, 1, metal, 0);
        plain(9, 1, 1, metal, 0);
        if (boom != null)
        {
            for (int a = 3; a <= 5; a++)
            {
                plain(a, 1, 1, boom, 0);                          // down
            }
            for (int y = 2; y <= 4; y++)
            {
                plain(9, y, 1, boom, 2);                          // raised
            }
        }
        prop(6, 2, 1, "BlockRedlight", S);
        prop(6, 1, 6, "BlockHazardpole", S);
        prop(9, 1, 6, "BlockHazardpole", S);
        prop(1, 1, 8, "BlockRussianRoadSign", S);
        prop(11, 1, 8, "BlockCone", S);
    }

    // ---------------------------------------------------------------- 1 military checkpoint

    /**
     * Entry control point: two jersey barrier rows staggered into a zigzag (d 2 closes the west half, d 5
     * the east half), a sandbag fighting position on the west sidewalk at the line, a guard tower with a
     * ladder and sandbag parapet on the east sidewalk, a searchlight and the flag behind the position,
     * hedgehogs at the approach, a warning sign.
     */
    private void checkpoint()
    {
        Block sand = d("BlockSandbagStack", Blocks.sandstone);
        Block camo = d("BlockMetalWall", Blocks.iron_block);
        for (int a = 4; a <= 6; a += 2)
        {
            prop(a, 1, 2, "BlockBarrier", S);
        }
        for (int a = 9; a <= 11; a += 2)
        {
            prop(a, 1, 5, "BlockBarrier", S);
        }
        // fighting position: front wall along the line, side wall by the road, open behind
        for (int y = 1; y <= 2; y++)
        {
            for (int a = 0; a <= 2; a++)
            {
                plain(a, y, 1, sand, 0);
            }
            plain(2, y, 2, sand, 0);
            plain(2, y, 3, sand, 0);
        }
        prop(0, 1, 3, "BlockMilitaryCrate", E);
        prop(1, 1, 6, "BlockSpotlight", S);
        prop(0, 1, 8, "BlockFlagPollUAHD", S);
        // guard tower x 13..15, z 4..7: legs, deck at 4, parapet at 5, ladder at (14, 7) on a back panel
        for (int y = 1; y <= 4; y++)
        {
            plain(13, y, 4, camo, 0);
            plain(15, y, 4, camo, 0);
            plain(13, y, 7, camo, 0);
            plain(15, y, 7, camo, 0);
            plain(14, y, 8, camo, 0);
            c.set(14, y, 7, Blocks.ladder, 2);
        }
        for (int z = 4; z <= 7; z++)
        {
            for (int a = 13; a <= 15; a++)
            {
                if (a == 14 && z == 7)
                {
                    continue;                                   // the hatch over the ladder
                }
                if (c.get(a, 4, z) == null)
                {
                    c.set(a, 4, z, Blocks.planks, 1);
                }
                boolean edge = a != 14 || z == 4;
                if (edge)
                {
                    plain(a, 5, z, sand, 0);
                }
            }
        }
        prop(14, 1, 1, "BlockRussianRoadSignWarning", S);
        prop(4, 1, 7, "BlockHedgehog", S);
        prop(11, 1, 7, "BlockHedgehog", S);
        prop(10, 1, 8, "BlockConcertinaWire", S);
    }

    // ---------------------------------------------------------------- 2 container wall

    /**
     * Shipping containers stacked two high as the wall (west x 0..6, east x 10..15, d 1..3) with a 3 wide
     * gap, razor wire on top; a third container across the way behind (x 5..11, d 5..7), doors open at its
     * east end with two crates inside, so the way in bends round it. Work lights, tyres, a crate.
     */
    private void containers()
    {
        Block[] skins = {d("BlockMetalWall", Blocks.iron_block), d("BlockMetal_3", Blocks.iron_block), metal};
        box(0, 6, 1, 3, 1, skins[0]);
        box(0, 6, 1, 3, 4, skins[1]);
        box(10, 15, 1, 3, 1, skins[2]);
        box(10, 15, 1, 3, 4, skins[0]);
        Block wire = d("BlockConcertinaWire", null);
        for (int a = 0; a <= 15; a++)
        {
            if (a >= 7 && a <= 9 || wire == null)
            {
                continue;
            }
            c.prop(a, 7, 2, wire, S);
        }
        box(5, 11, 5, 7, 1, skins[1]);
        c.clear(11, 1, 6);                                      // doors open at the east end
        c.clear(11, 2, 6);
        prop(6, 1, 6, "BlockMilitaryCrate", E);
        prop(7, 1, 6, "BlockMilitaryCrate", E);
        prop(1, 1, 6, "BlockHazardLight", S);
        prop(14, 1, 6, "BlockHazardLight", S);
        prop(1, 1, 8, "BlockTireStack", S);
        prop(13, 1, 8, "BlockWoodCrate", S);
    }

    /** A container shell x0..x1, z0..z1, 3 high from y0 (hollow, roof on top). */
    private void box(int x0, int x1, int z0, int z1, int y0, Block skin)
    {
        for (int a = x0; a <= x1; a++)
        {
            for (int z = z0; z <= z1; z++)
            {
                boolean edge = a == x0 || a == x1 || z == z0 || z == z1;
                for (int y = y0; y < y0 + 3; y++)
                {
                    if (edge || y == y0 + 2)
                    {
                        plain(a, y, z, skin, 0);
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------- 4 quarantine

    /**
     * Decontamination lane: a white tent over the road (walls x 4 and 11, d 2..6, roof and ridge) open at
     * both ends with strip curtains on the outer halves, medical crates and a stretcher inside; generator,
     * work light and water on the west sidewalk, body bags on the east one, barriers at the line ends.
     */
    private void quarantine()
    {
        Block wool = Blocks.wool;
        for (int z = 2; z <= 6; z++)
        {
            for (int y = 1; y <= 3; y++)
            {
                plain(4, y, z, wool, 0);
                plain(11, y, z, wool, 0);
            }
            for (int a = 4; a <= 11; a++)
            {
                plain(a, 4, z, wool, 0);
            }
            plain(7, 5, z, wool, 0);
            plain(8, 5, z, wool, 0);
        }
        for (int z = 2; z <= 6; z += 4)
        {
            prop(5, 1, z, "BlockHazardScreen_1", S);
            prop(10, 1, z, "BlockHazardScreen_1", S);
        }
        prop(5, 1, 3, "BlockMedicalCrate", E);
        prop(5, 1, 4, "BlockMedicalCrate", E);
        prop(10, 1, 4, "BlockStretcher", WE);
        prop(9, 1, 5, "BlockHazardLamp", N);
        prop(3, 1, 0, "BlockHazardbarrier", S);
        prop(11, 1, 0, "BlockHazardbarrier", S);
        prop(1, 1, 3, "BlockPowerGenerator", S);
        prop(1, 1, 5, "BlockHazardLight", S);
        prop(0, 1, 7, "BlockWaterPalletTarp", S);
        String[] bags = {"BlockBodyBagBlack", "BlockBodyBagBlue", "BlockBodyBagGreen"};
        for (int i = 0; i < 3; i++)
        {
            prop(13, 1, 3 + 2 * i, bags[i], E);
        }
        prop(14, 1, 1, "BlockRussianRoadSignWarning", S);
    }

    // ---------------------------------------------------------------- 5 police roadblock

    /**
     * Police roadblock: barriers closing the west half of the line, a police car across the east half
     * behind it (the way in bends round its tail), a cone line at the approach, a small police booth with a
     * beacon on the west sidewalk and a police crate beside it, a work lamp and a sign on the east one.
     */
    private void police()
    {
        for (int a = 4; a <= 8; a += 2)
        {
            prop(a, 1, 0, "BlockHazardbarrier", S);
        }
        prop(10, 1, 3, "BlockWreckagePolice3", WE);
        for (int a = 3; a <= 11; a += 2)
        {
            if (a != 7)
            {
                prop(a, 1, 7, "BlockCone", S);
            }
        }
        // booth x 0..2, d 2..4: blue walls, glass band, roof, door to the south
        for (int z = 2; z <= 4; z++)
        {
            for (int a = 0; a <= 2; a++)
            {
                boolean ring = a != 1 || z != 3;
                if (ring)
                {
                    boolean corner = a != 1 && z != 3;
                    plain(a, 1, z, corner ? Blocks.stonebrick : Blocks.stained_hardened_clay, corner ? 0 : 11);
                    if (corner)
                    {
                        plain(a, 2, z, Blocks.stonebrick, 0);
                    }
                    else
                    {
                        c.set(a, 2, z, Blocks.glass_pane, 0);
                    }
                }
                plain(a, 3, z, Blocks.double_stone_slab, 0);
            }
        }
        door(1, 1, 4, d("Door_Metal_3", Blocks.iron_door), S);
        prop(2, 1, 6, "BlockPoliceCrate", E);                  // outside: the booth's one cell is its doorway
        prop(1, 4, 3, "BlockRedlight", S);
        prop(14, 1, 6, "BlockHazardLamp", S);
        prop(14, 1, 1, "BlockRussianRoadSign", S);
    }
}

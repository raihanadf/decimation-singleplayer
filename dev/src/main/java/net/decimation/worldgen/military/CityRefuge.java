package net.decimation.worldgen.military;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/**
 * A fortified city building, the safezone of a city (docs/safezone.md): one city block (the 3 x 3
 * building chunks, 48 x 48) turned into a refuge around a three storey concrete civic building.
 * References (docs/safezone.md section 1): entry control points are a corridor of segments (approach,
 * identity check, a chicane that slows anyone coming in, a guard post); standoff between the street
 * and the building (barriers and wire at the lot edge, a wall, a yard); emergency shelters in public
 * buildings run reception / registration at a controlled entrance, a dormitory, a medical area, a
 * canteen and a distribution point, with generators away from the rooms.
 * Layout (canvas x east, z south, gate south; ground floor of the building at y 0 = the city ground):
 * - apron 0..1: jersey barriers and wire along the street, never before the gate;
 * - wall on 2 / 45: concrete 3 high with posts, wire on top; gate x 21..26 with safezone signs;
 * - entry channel x 19..28, z 38..44 between sandbag walls: two staggered barrier lines (chicane),
 *   a sandbagged guard each side of the gate;
 * - building x 9..38, z 6..29, storeys at 0 / 5 / 10, roof 15: ground floor lobby with a
 *   registration desk and a guard, a market hall of six stalls along a central aisle, a clinic
 *   (west) and a canteen (east); first floor dormitory; second floor command room, armory, guard
 *   bunks and stores; roof with a parapet, sandbag nests and a guard in each corner, a spotlight
 *   and a flag; one switchback stair (north side) from the hall to a roof bulkhead;
 * - yard: forecourt with benches and a fire barrel, tents (south west), aid distribution tables
 *   (south east), an APC and a generator yard (west), water tanks and raised garden beds (east),
 *   a jeep and supply stacks (north).
 * Residents (posts): traders, guards, civilians and armed survivors.
 */
final class CityRefuge
{
    static final int W = 48, L = 48, HEIGHT = 21;
    static final int E = 2, S = 3, WEST = 4, N = 5;
    /** Wall lines, gate opening, building box, storey floors, roof. */
    static final int X0 = 2, X1 = 45, Z0 = 2, Z1 = 45, GX0 = 21, GX1 = 26;
    static final int BX0 = 9, BX1 = 38, BZ0 = 6, BZ1 = 29, F1 = 5, F2 = 10, ROOF = 15;
    /** Ground floor interior walls (clinic west of HW, canteen east of HE), aisle, stair rows. */
    static final int HW = 16, HE = 31, AX0 = 22, AX1 = 25;

    final Canvas c = new Canvas(W, HEIGHT, L);
    final List<SurvivorCamp.Post> posts = new ArrayList<SurvivorCamp.Post>();
    final Map<String, float[]> poi = new LinkedHashMap<String, float[]>();
    final List<String> log = new ArrayList<String>();
    private final Random r;
    private final MilitaryBase.B b = MilitaryBase.B.get();
    private Block concrete, floor;

    CityRefuge(long seed)
    {
        r = new Random(seed);
    }

    private void poi(String name, double x, double y, double z, float yaw, float pitch)
    {
        if (!poi.containsKey(name))
        {
            poi.put(name, new float[] {(float) x, (float) y, (float) z, yaw, pitch});
        }
    }

    private void post(String kind, double x, double y, double z, float yaw)
    {
        posts.add(new SurvivorCamp.Post(kind, x + 0.5, y, z + 0.5, yaw));
    }

    Canvas build()
    {
        concrete = b.concrete;
        floor = b.concreteFloor;
        ground();
        apron();
        wall();
        gate();
        building();
        stairs();
        groundFloor();
        dormitory();
        command();
        roof();
        yard();
        people();
        c.validateProps(log);
        return c;
    }

    // ================================================================ ground, apron, wall, gate

    /**
     * Asphalt yard (the block was a parking lot around a civic building), concrete paving for the
     * apron, the entry channel and the forecourt path, gravel here and there (first shots: concrete
     * yard under a concrete facade read as one grey mass).
     */
    private void ground()
    {
        Block asphalt = d("BlockRoad");
        for (int z = 0; z < L; z++)
        {
            for (int x = 0; x < W; x++)
            {
                boolean apron = x < X0 || z < Z0 || x > X1 || z > Z1;
                boolean paved = x >= 18 && x <= 29 && z >= 30 || x >= 21 && x <= 26 && z > BZ1;
                double n = noise(x, z);
                Block g = apron || paved || asphalt == null ? floor : asphalt;
                c.set(x, 0, z, n > 0.93 ? Blocks.gravel : g);
            }
        }
    }

    private double noise(int x, int z)
    {
        long h = (x * 73856093L) ^ (z * 19349663L) ^ 0xC17EL;
        h ^= h >>> 13;
        h *= 0x9E3779B97F4A7C15L;
        return ((h >>> 11) & 0xFFFFF) / (double) 0xFFFFF;
    }

    /** Standoff along the street: jersey barriers in the outer row (gaps between), wire in the row behind. */
    private void apron()
    {
        for (int k = 3; k <= W - 4; k += 3)
        {
            boolean gateFront = k >= GX0 - 3 && k <= GX1 + 3;
            if (noise(k, 1) > 0.2)
            {
                c.prop(k, 1, 0, b.jersey, N);                 // along x on the north edge
            }
            if (!gateFront && noise(k, 2) > 0.2)
            {
                c.prop(k, 1, L - 1, b.jersey, S);
            }
            if (noise(0, k) > 0.2)
            {
                c.prop(0, 1, k, b.jersey, WEST);              // along z on the west edge
            }
            if (noise(1, k) > 0.2)
            {
                c.prop(W - 1, 1, k, b.jersey, E);
            }
        }
        for (int k = 2; k <= W - 3; k++)
        {
            boolean gateFront = k >= GX0 - 4 && k <= GX1 + 4;
            if (noise(k, 9) > 0.45)
            {
                c.set(k, 1, 1, b.wire);
            }
            if (!gateFront && noise(k, 10) > 0.45)
            {
                c.set(k, 1, L - 2, b.wire);
            }
            if (noise(9, k) > 0.45)
            {
                c.set(1, 1, k, b.wire);
            }
            if (noise(10, k) > 0.45)
            {
                c.set(W - 2, 1, k, b.wire);
            }
        }
    }

    private boolean onWall(int x, int z)
    {
        return (x == X0 || x == X1) && z >= Z0 && z <= Z1 || (z == Z0 || z == Z1) && x >= X0 && x <= X1;
    }

    /** Concrete wall 3 high, a post every 6 cells one higher, wire on the top here and there. */
    private void wall()
    {
        for (int z = Z0; z <= Z1; z++)
        {
            for (int x = X0; x <= X1; x++)
            {
                if (!onWall(x, z) || z == Z1 && x >= GX0 && x <= GX1)
                {
                    continue;
                }
                int along = z == Z0 || z == Z1 ? x : z;
                boolean corner = (x == X0 || x == X1) && (z == Z0 || z == Z1);
                if (corner || along % 6 == 2)
                {
                    c.fill(x, 1, z, x, 4, z, Blocks.stonebrick);
                    c.set(x, 5, z, Blocks.torch, 5);         // lit at night, as the camp's posts
                    continue;
                }
                c.fill(x, 1, z, x, 3, z, concrete);
                if (noise(x + 31, z) > 0.35)
                {
                    c.set(x, 4, z, b.wire);
                }
            }
        }
    }

    private void gate()
    {
        c.fill(GX0 - 1, 1, Z1, GX0 - 1, 5, Z1, Blocks.stonebrick);
        c.fill(GX1 + 1, 1, Z1, GX1 + 1, 5, Z1, Blocks.stonebrick);
        for (int x = GX0; x <= GX1; x++)
        {
            c.set(x, 1, Z1, Blocks.fence_gate, 0);          // closed, spanning along x
            c.set(x, 4, Z1, concrete);                      // lintel
            c.set(x, 0, Z1, Blocks.stonebrick);
        }
        // the entry text one block outside the lintel and one up, the exit text inside (camp rule,
        // user review 11 Oktober 2026: on the lintel the text was hidden)
        c.prop((GX0 + GX1) / 2, 5, Z1 + 1, d("BlockSafezoneSignEntry"), S);
        c.prop((GX0 + GX1) / 2, 5, Z1 - 1, d("BlockSafezoneSignExit"), N);
        // entry channel: sandbag walls 2 high on x 18 and 29, two staggered barrier lines inside
        for (int z = 38; z <= Z1 - 1; z++)
        {
            c.fill(18, 1, z, 18, 2, z, b.sandbag);
            c.fill(29, 1, z, 29, 2, z, b.sandbag);
        }
        for (int x : new int[] {20, 22, 24})
        {
            c.prop(x, 1, 42, b.jersey, S);                  // drawn 2 long along x: 19.5 .. 24.5, open east
        }
        for (int x : new int[] {23, 25, 27})
        {
            c.prop(x, 1, 39, b.jersey, N);                  // 22.5 .. 28.5, open west
        }
        // a sandbagged guard each side inside the gate
        for (int gx : new int[] {19, 28})
        {
            c.set(gx, 1, Z1 - 2, b.sandbag);
            post("guard", gx, 1, Z1 - 1, 0);
        }
        poi("gate_outside", (GX0 + GX1) / 2 + 1, 3.6, L + 7, 180, 8);
        poi("main_road", (GX0 + GX1) / 2 + 0.5, 1.6, Z1 - 1.5, 0, 8);
        poi("chicane", 23.5, 4.6, 36.5, 0, 35);
    }

    // ================================================================ building shell

    private boolean onFace(int x, int z)
    {
        return x == BX0 || x == BX1 || z == BZ0 || z == BZ1;
    }

    /** Shell: concrete walls, slabs at 0 / 5 / 10 / 15, stone brick corners and floor bands, windows. */
    private void building()
    {
        for (int z = BZ0; z <= BZ1; z++)
        {
            for (int x = BX0; x <= BX1; x++)
            {
                for (int y : new int[] {0, F1, F2, ROOF})
                {
                    c.set(x, y, z, floor);
                }
                if (!onFace(x, z))
                {
                    continue;
                }
                boolean corner = (x == BX0 || x == BX1) && (z == BZ0 || z == BZ1);
                for (int y = 1; y < ROOF; y++)
                {
                    c.set(x, y, z, corner || y == F1 || y == F2 ? Blocks.stonebrick : concrete);
                }
                c.set(x, ROOF + 1, z, concrete);           // parapet
                if (corner)
                {
                    continue;
                }
                int along = z == BZ0 || z == BZ1 ? x : z;
                if (along % 3 != 1 || doorway(x, z))
                {
                    continue;
                }
                // windows: ground floor boarded behind bars, upper floors glass, some sandbagged
                c.set(x, 2, z, Blocks.planks, 1);
                c.set(x, 3, z, Blocks.iron_bars);
                for (int base : new int[] {F1, F2})
                {
                    boolean bagged = noise(x + base, z) > (base == F2 ? 0.4 : 0.7);
                    c.set(x, base + 2, z, bagged ? b.sandbag : Blocks.glass_pane);
                    c.set(x, base + 3, z, Blocks.glass_pane);
                }
            }
        }
        // the entrance: a double metal door in the south face, a sandbag vestibule outside, a wall flag
        door(23, 1, BZ1, b.metalDoor, S, false);
        door(24, 1, BZ1, b.metalDoor, S, true);
        for (int x : new int[] {21, 26})
        {
            c.fill(x, 1, BZ1 + 1, x, 2, BZ1 + 2, b.sandbag);
        }
        c.fill(22, 4, BZ1, 25, 4, BZ1, Blocks.stonebrick);
        c.prop(23, 6, BZ1 + 1, b.wallFlag, S);
        poi("front", 23.5, 2.6, 37.5, 180, 8);
        poi("aerial", W / 2 + 0.5, 38, L + 22, 180, 45);
    }

    private boolean doorway(int x, int z)
    {
        return z == BZ1 && x >= 22 && x <= 25;
    }

    /** A door of two halves; front east 2, south 3, west 4, north 5 (vanilla door metadata, turned as one). */
    private void door(int x, int y, int z, Block d, int front, boolean rightHinge)
    {
        int m = front == S ? 3 : front == N ? 1 : front == E ? 2 : 0;
        c.set(x, y, z, d, m, Canvas.DOOR);
        c.set(x, y + 1, z, d, rightHinge ? 9 : 8, Canvas.DOOR);
    }

    /**
     * Switchback stair against the north wall: run A (row 7) east from the hall up to the first
     * floor, run B (row 8) west up to the second, run C (row 7) east up to the roof bulkhead. Each run
     * is 5 steps; the slab above each run is open where a head would hit it.
     */
    private void stairs()
    {
        for (int k = 0; k < 5; k++)
        {
            c.set(25 + k, 1 + k, 7, Blocks.stone_brick_stairs, 0);          // ascending east
            c.set(29 - k, F1 + 1 + k, 8, Blocks.stone_brick_stairs, 1);     // ascending west
            c.set(25 + k, F2 + 1 + k, 7, Blocks.stone_brick_stairs, 0);
        }
        for (int x = 25; x <= 28; x++)
        {
            c.clear(x, F1, 7);
            c.clear(x, ROOF, 7);
        }
        for (int x = 26; x <= 29; x++)
        {
            c.clear(x, F2, 8);
        }
        // roof bulkhead around the exit: walls 24..31 x 6..9 up to 18, a roof at 19, a door south
        for (int x = 24; x <= 31; x++)
        {
            for (int z = BZ0; z <= 9; z++)
            {
                boolean edge = x == 24 || x == 31 || z == BZ0 || z == 9;
                if (edge)
                {
                    c.fill(x, ROOF + 1, z, x, ROOF + 3, z, concrete);
                }
                c.set(x, ROOF + 4, z, floor);
            }
        }
        door(30, ROOF + 1, 9, b.metalDoor, S, false);
        c.prop(27, ROOF + 3, 8, b.light, S);
        poi("stair", 23.5, 2.6, 11.5, 210, 25);           // looking north east up run A
    }

    // ================================================================ ground floor

    private static final String[] FIXED = {"TraderGuns", "TraderAmmunition", "TraderFood", "TraderMedical"};
    private static final String[] EXTRA = {"TraderArmor", "TraderBackpack", "TraderConvenience", "TraderScrap",
        "TraderFortification", "TraderBlackMarket", "TraderHeavyGuns", "TraderGoldenCap"};
    private static final int[] STRIPE = {14, 11, 13, 1, 4, 12};

    private void groundFloor()
    {
        // interior walls: clinic (west) and canteen (east), doors off the lobby
        for (int z = BZ0 + 1; z < BZ1; z++)
        {
            c.fill(HW, 1, z, HW, 4, z, concrete);
            c.fill(HE, 1, z, HE, 4, z, concrete);
        }
        door(HW, 1, 26, Blocks.wooden_door, E, false);
        door(HE, 1, 26, Blocks.wooden_door, WEST, false);
        // aisle from the door to the stair
        for (int z = 7; z < BZ1; z++)
        {
            for (int x = AX0; x <= AX1; x++)
            {
                c.set(x, 0, z, Blocks.stonebrick);
            }
        }
        // market: six stalls along the aisle
        List<String> traders = new ArrayList<String>();
        for (String t : FIXED)
        {
            traders.add(t);
        }
        List<String> extra = new ArrayList<String>();
        for (String t : EXTRA)
        {
            extra.add(t);
        }
        java.util.Collections.shuffle(extra, r);
        traders.add(extra.get(0));
        traders.add(extra.get(1));
        java.util.Collections.shuffle(traders, r);
        int[] zs = {10, 15, 20};
        for (int i = 0; i < 6; i++)
        {
            stall(i < 3, zs[i % 3], traders.get(i), STRIPE[i]);
        }
        for (int z = 10; z < BZ1 - 1; z += 5)
        {
            c.prop(23, 4, z + 2, b.light, S);
        }
        // the gaps between stalls: a barrel at the back (goods are drawn up to 1.9 long into the gap)
        c.prop(17, 1, 14, b.barrel, S);
        c.prop(30, 1, 19, b.barrel, S);
        lobby();
        clinic();
        canteen();
        poi("market", 23.5, 2.6, 26.5, 180, 12);
    }

    /**
     * A stall 4 long along the aisle: plank floor, the counter (two wood tables drawn 2 long) at the
     * aisle edge, the trader behind it, two goods props at the back wall, a striped wool awning
     * under the ceiling.
     */
    private void stall(boolean west, int z0, String trader, int stripe)
    {
        int front = west ? 21 : 26, back = west ? 17 : 30, dir = west ? 1 : -1;
        for (int z = z0; z <= z0 + 3; z++)
        {
            for (int k = 0; k <= 4; k++)
            {
                int x = back + dir * k;
                c.set(x, 0, z, Blocks.planks, 1);
                c.set(x, 4, z, Blocks.wool, (k & 1) == 0 ? stripe : 0);
            }
        }
        int face = west ? E : WEST;
        c.prop(front, 1, z0, b.woodTable, face);
        c.prop(front, 1, z0 + 2, b.woodTable, face);
        post("trader:" + trader, front - dir, 1, z0 + 1, west ? 270 : 90);
        Block[] goods;
        if (trader.contains("Guns") || trader.contains("BlackMarket"))
        {
            goods = new Block[] {b.weaponCabinet, b.ammoCrate};
        }
        else if (trader.contains("Ammunition"))
        {
            goods = new Block[] {b.ammoCrateLarge, b.militaryCrate};
        }
        else if (trader.contains("Food"))
        {
            goods = new Block[] {b.waterPallet, b.cardboard};
        }
        else if (trader.contains("Medical"))
        {
            goods = new Block[] {b.medicalCrate, b.medicalCrate};
        }
        else if (trader.contains("Armor") || trader.contains("Backpack"))
        {
            goods = new Block[] {b.storageCrate, b.militaryCrate};
        }
        else
        {
            goods = new Block[] {b.woodCrate, b.cardboard};
        }
        c.prop(back, 1, z0, goods[0], face);
        c.prop(back, 1, z0 + 2, goods[1], face);
        poi("stall_" + trader.substring(6).toLowerCase(), (AX0 + AX1) / 2.0 + 0.5, 1.6, z0 + 1.5, west ? 90 : 270, 10);
    }

    /** Registration desk west of the door (a guard checks who comes in), waiting benches east. */
    private void lobby()
    {
        c.prop(19, 1, 26, b.metalTable, S);                 // drawn 2 long along x
        c.prop(19, 1, 25, b.officeChair, S);
        c.prop(19, 2, 26, b.monitor, N);                    // on the desk, screen toward the chair
        c.prop(18, 1, 27, b.cardboard, E);
        c.prop(28, 1, 25, b.bench, WEST);
        c.prop(28, 1, 27, b.bench, WEST);
        post("guard", 21, 1, 27, 0);
        poi("lobby", 24.5, 2.6, 23.5, 60, 15);
    }

    /** Clinic: stretchers along the west wall, medical crates, a desk. */
    private void clinic()
    {
        for (int z = 9; z <= 21; z += 3)
        {
            c.prop(BX0 + 2, 1, z, b.stretcher, E);           // drawn 2.1 long along z
        }
        for (int x = 12; x <= 14; x++)
        {
            c.prop(x, 1, 7, b.medicalCrate, S);
        }
        // patient beds head to the partition, a cabinet between pairs
        for (int z = 10; z <= 19; z += 3)
        {
            c.set(15, 1, z, Blocks.bed, 3 | 8);             // head east
            c.set(14, 1, z, Blocks.bed, 3);
        }
        c.prop(15, 1, 21, b.shelf, N);
        c.prop(13, 1, 24, b.metalTable, S);
        c.prop(13, 1, 25, b.chair, N);
        for (int z = 10; z <= 22; z += 6)
        {
            c.prop(13, 4, z, b.light, S);
        }
        poi("clinic", 12.5, 2.6, 22.5, 180, 15);
    }

    /** Canteen: cooking stations on the east wall, two tables with chairs, water. */
    private void canteen()
    {
        for (int z = 8; z <= 12; z += 2)
        {
            c.prop(BX1 - 1, 1, z, b.cooking, WEST);
        }
        c.prop(33, 1, 8, b.waterPallet, E);
        // serving counter across the kitchen end, a gap at the partition side
        for (int x = 34; x <= 37; x += 2)
        {
            c.prop(x, 1, 13, b.metalTable, N);               // drawn 2 long along x
        }
        for (int z : new int[] {16, 19, 22})
        {
            c.prop(34, 1, z, b.woodTable, E);                // drawn 2 long along z
            c.prop(33, 1, z, b.chair, E);
            c.prop(35, 1, z + 1, b.chair, WEST);
        }
        for (int z = 10; z <= 22; z += 6)
        {
            c.prop(34, 4, z, b.light, S);
        }
        poi("canteen", 34.5, 1.6, 27.5, 180, 10);
    }

    // ================================================================ upper floors

    /** First floor: rows of beds (head north) with a footlocker beside each pair, lights. */
    private void dormitory()
    {
        int y = F1 + 1;
        for (int z : new int[] {12, 17, 22, 26})
        {
            for (int x = 11; x <= BX1 - 2; x += 2)
            {
                if (x >= AX0 && x <= AX1)
                {
                    continue;                                // keep the middle aisle free
                }
                c.set(x, y, z, Blocks.bed, 2 | 8);           // head
                c.set(x, y, z + 1, Blocks.bed, 2);           // foot
                if (((x - 11) / 2) % 3 == 1 && z != 26)
                {
                    c.prop(x, y, z + 2, b.storageCrate, S);
                }
            }
        }
        for (int z = 11; z <= 26; z += 5)
        {
            c.prop(17, y + 3, z, b.light, S);
            c.prop(30, y + 3, z, b.light, S);
        }
        poi("dormitory", 23.5, F1 + 1.6, 27.5, 180, 12);
    }

    /** Second floor: command desk with radios, armory along the west wall, guard bunks east, stores north. */
    private void command()
    {
        int y = F2 + 1;
        // rooms as on the ground floor: armory west, guard bunks east (doors off the middle hall)
        for (int z = BZ0 + 1; z < BZ1; z++)
        {
            c.fill(HW, y, z, HW, y + 3, z, concrete);
            c.fill(HE, y, z, HE, y + 3, z, concrete);
        }
        door(HW, y, 24, Blocks.wooden_door, E, false);
        door(HE, y, 24, Blocks.wooden_door, WEST, false);
        // armory shelves on the partition, a locker, a work table
        for (int z = 9; z <= 21; z += 3)
        {
            c.prop(HW - 1, y, z, b.shelf, N);                 // metadata 5: drawn 2 long along z, 2 high
        }
        c.prop(13, y, 26, b.metalTable, S);
        c.prop(13, y + 1, 26, b.ammoCrate, S);
        c.prop(15, y, 27, b.toolLocker, N);
        // command: two metal tables along x, radios on them, chairs
        c.prop(18, y, 24, b.metalTable, S);
        c.prop(21, y, 24, b.metalTable, S);
        c.prop(18, y + 1, 24, b.radioSmall, S);
        c.prop(17, y, 25, b.officeChair, N);
        c.prop(21, y, 25, b.officeChair, N);
        c.prop(27, y, 27, b.radio, WEST);                   // big radio: metadata 4 puts its dials north
        c.prop(19, y, 20, b.metalTable, S);
        c.prop(19, y + 1, 20, b.monitor, S);
        c.prop(19, y, 19, b.officeChair, S);
        c.prop(22, y, 20, b.metalTable, S);
        c.prop(22, y + 1, 20, b.cardboard, S);
        // armory: weapon cabinets and ammunition on the west wall
        for (int z = 12; z <= 20; z += 2)
        {
            c.prop(BX0 + 1, y, z, z % 4 == 0 ? b.weaponCabinet : b.ammoCrateLarge, E);
        }
        c.prop(BX0 + 1, y, 22, b.militaryCrate, E);
        // guard bunks east (beds head east along x)
        for (int z = 12; z <= 22; z += 3)
        {
            c.set(BX1 - 1, y, z, Blocks.bed, 3 | 8);         // head east
            c.set(BX1 - 2, y, z, Blocks.bed, 3);
            c.prop(BX1 - 1, y, z + 1, b.militaryCrate, WEST);
        }
        // stores north: water and food, away from the stair
        for (int x = 18; x <= 22; x += 2)
        {
            // crates are drawn 1.4 across the front: clear of the partition; x 24 free (run B lands there)
            c.prop(x, y, 8, x == 20 ? b.waterPallet : b.woodCrate, S);
        }
        for (int z = 11; z <= 26; z += 5)
        {
            c.prop(17, y + 3, z, b.light, S);
            c.prop(30, y + 3, z, b.light, S);
        }
        poi("command", 23.5, F2 + 1.6, 27.5, 180, 12);
        poi("armory", 12.5, F2 + 2.6, 23.5, 180, 15);
        poi("bunks", 33.5, F2 + 2.6, 24.5, 180, 15);
    }

    /** Roof: sandbag nests on the corners with a guard each, spotlight over the entrance, flag, water tanks. */
    private void roof()
    {
        int y = ROOF + 1;
        int[][] corners = {{BX0 + 2, BZ0 + 2, 1, 1}, {BX1 - 2, BZ0 + 2, -1, 1}, {BX0 + 2, BZ1 - 2, 1, -1},
            {BX1 - 2, BZ1 - 2, -1, -1}};
        for (int[] k : corners)
        {
            int cx = k[0] - k[2] * 1, cz = k[1] - k[3] * 1;   // the corner cell inside the parapet
            for (int d = 0; d <= 3; d++)
            {
                c.fill(cx + k[2] * d, y, cz, cx + k[2] * d, y + 1, cz, b.sandbag);
                c.fill(cx, y, cz + k[3] * d, cx, y + 1, cz + k[3] * d, b.sandbag);
            }
            // looking out over the corner: yaw 0 south, 90 west, 180 north, 270 east
            float yaw = k[3] < 0 ? (k[2] > 0 ? 45 : 315) : (k[2] > 0 ? 135 : 225);
            post("guard", k[0], y, k[1], yaw);
        }
        c.prop(23, y, 26, b.spotlight, S);
        c.prop(14, y, 16, b.flagPole, S);
        c.fill(33, y, 13, 34, y + 1, 14, Blocks.iron_block);
        c.set(33, y, 16, Blocks.cauldron, 3);
        c.prop(18, y, 10, b.radioTower, S);
        poi("roof", 33.5, y + 1.6, 26.5, 135, 18);
    }

    // ================================================================ yard

    private void yard()
    {
        // forecourt: benches and a fire barrel, light towers
        c.prop(15, 1, 33, b.canFire, S);
        c.prop(15, 1, 31, b.bench, S);
        c.prop(15, 1, 35, b.bench, N);
        c.prop(32, 1, 31, b.lightTower, S);
        c.prop(17, 1, 36, b.lightTower, N);
        // tents south west
        int n = 0;
        for (int[] t : new int[][] {{4, 32}, {10, 32}, {4, 39}, {11, 39}})
        {
            tent(t[0], t[1], n++, t[1] > 35);                // the back row opens north, away from the wall
        }
        // aid distribution south east: tables in a row, supplies behind, a queue line
        for (int x : new int[] {33, 36, 39})
        {
            c.prop(x, 1, 34, b.metalTable, S);
        }
        c.prop(33, 1, 32, b.waterPallet, S);
        c.prop(36, 1, 32, b.cardboard, S);
        c.prop(39, 1, 32, b.carePackage, S);
        for (int x : new int[] {32, 34, 36})
        {
            c.prop(x, 1, 37, b.sawhorse, S);
        }
        Block car = d("BlockWreckage1");
        c.prop(42, 1, 39, car != null ? car : b.jeep, S);
        poi("distribution", 36.5, 2.6, 41.5, 180, 12);
        // west strip: an APC along z, the generator yard
        c.prop(5, 1, 12, b.apc, E);                         // drawn along z, 8.4 .. 17.3
        c.prop(5, 1, 21, b.generator, E);
        c.prop(4, 1, 24, b.barrel, S);
        c.prop(5, 1, 24, b.barrel, S);
        c.prop(7, 1, 24, b.tireStack, S);
        c.prop(4, 1, 27, b.lightTower, E);
        c.prop(7, 1, 27, b.toolLocker, WEST);
        poi("west_yard", 6.5, 2.6, 30.5, 180, 12);
        // east strip: water tanks, raised garden beds
        c.fill(40, 1, 4, 41, 2, 5, Blocks.iron_block);
        c.set(42, 1, 4, Blocks.cauldron, 3);
        c.set(42, 1, 5, Blocks.cauldron, 3);
        for (int z = 9; z <= 23; z += 7)
        {
            bed(40, z);
        }
        poi("east_yard", 41.5, 3.6, 29.5, 180, 25);
        // north strip: a jeep along x, supply stacks, bins
        c.prop(13, 1, 4, b.jeep, WEST);                     // military wrecks lie along x at 4 / 5
        c.prop(19, 1, 4, b.woodCrate, E);
        c.prop(20, 1, 4, b.woodCrate, E);
        c.prop(22, 1, 4, b.carePackage, S);
        c.prop(26, 1, 4, b.trashcan, S);
        c.prop(27, 1, 4, b.trashBag, S);
        c.prop(28, 1, 4, b.trashBag2 != null ? b.trashBag2 : b.trashBag, S);
        c.prop(32, 1, 4, b.barrel, S);
        c.prop(33, 1, 4, b.barrel, S);
        poi("north_yard", 23.5, 2.6, 4.5, 270, 10);
    }

    /** A canvas tent 5 x 6 (door south, or north), a bed, a footlocker, a light. */
    private void tent(int x0, int z0, int n, boolean doorNorth)
    {
        int x1 = x0 + 4, z1 = z0 + 5, mx = x0 + 2;
        int kind = (int) (noise(x0, z0) * 4);
        Block skin = kind == 0 ? Blocks.sandstone : Blocks.wool;
        int skinMeta = kind == 0 ? 2 : kind == 1 ? 12 : kind == 2 ? 13 : 7;
        for (int z = z0; z <= z1; z++)
        {
            boolean end = z == z0 || z == z1;
            for (int x = x0; x <= x1; x++)
            {
                int dd = Math.min(x - x0, x1 - x);
                c.set(x, 0, z, Blocks.planks, 2);
                int top = dd == 0 ? 2 : dd == 1 ? 3 : 4;
                for (int y = 1; y <= top; y++)
                {
                    if (y == top || dd == 0 || end)
                    {
                        c.set(x, y, z, skin, skinMeta);
                    }
                }
            }
        }
        int dz = doorNorth ? z0 : z1, back = doorNorth ? z1 - 1 : z0 + 1;
        c.clear(mx, 1, dz);
        c.clear(mx, 2, dz);
        // the bed's head at the back wall
        c.set(x0 + 1, 1, back, Blocks.bed, (doorNorth ? 0 : 2) | 8);
        c.set(x0 + 1, 1, back + (doorNorth ? -1 : 1), Blocks.bed, doorNorth ? 0 : 2);
        c.prop(x1 - 1, 1, back, b.storageCrate, WEST);
        c.prop(mx, 3, z0 + 3, b.light, S);
        if (n == 0)
        {
            poi("tent_inside", mx + 0.5, 1.6, z1 - 0.5, 180, 10);
        }
    }

    /** A raised bed 4 x 5: log frame, farmland with carrots / potatoes / wheat, a water cell. */
    private void bed(int x0, int z0)
    {
        for (int z = z0; z <= z0 + 4; z++)
        {
            for (int x = x0; x <= x0 + 3; x++)
            {
                boolean edge = x == x0 || x == x0 + 3 || z == z0 || z == z0 + 4;
                if (edge)
                {
                    c.set(x, 1, z, Blocks.log, 0);
                    continue;
                }
                c.set(x, 0, z, Blocks.dirt);
                if (x == x0 + 1 && z == z0 + 2)
                {
                    c.set(x, 1, z, Blocks.water);
                    continue;
                }
                c.set(x, 1, z, Blocks.farmland, 7);
                c.set(x, 2, z, z < z0 + 2 ? Blocks.carrots : z < z0 + 3 ? Blocks.potatoes : Blocks.wheat, 7);
            }
        }
    }

    // ================================================================ people

    /** Civilians and armed survivors on free cells of the yard, the dormitory, the canteen and the lobby. */
    private void people()
    {
        // {x, storey y, z}
        int[][] civ = {{20, 1, 34}, {27, 1, 34}, {18, F1 + 1, 15}, {29, F1 + 1, 20}, {34, 1, 20}, {7, 1, 30}};
        int[][] surv = {{14, 1, 37}, {27, 1, 27}, {35, 1, 40}};
        for (int[] p : civ)
        {
            place("civilian", p[0], p[1], p[2]);
        }
        for (int[] p : surv)
        {
            place("survivor", p[0], p[1], p[2]);
        }
    }

    private void place(String kind, int x, int y, int z)
    {
        int[] f = standing(x, y, z);
        if (f != null)
        {
            post(kind, f[0], y, f[1], r.nextInt(360));
        }
    }

    /** The nearest cell to (x, z) on storey y with a solid floor and two free cells above, or null. */
    private int[] standing(int x, int y, int z)
    {
        for (int d = 0; d < 6; d++)
        {
            for (int dx = -d; dx <= d; dx++)
            {
                for (int dz = -d; dz <= d; dz++)
                {
                    int px = x + dx, pz = z + dz;
                    Block g = c.get(px, y - 1, pz);
                    if (g != null && g != Blocks.water && g != Blocks.farmland && c.get(px, y, pz) == null
                        && c.get(px, y + 1, pz) == null && px > X0 + 1 && px < X1 - 1 && pz > Z0 + 1 && pz < Z1 - 1)
                    {
                        return new int[] {px, pz};
                    }
                }
            }
        }
        log.add("no standing cell near " + x + "," + y + "," + z);
        return null;
    }

    private static Block d(String name)
    {
        Block k = Block.getBlockFromName("deci:" + name);
        return k != null && k != Blocks.air ? k : null;
    }
}

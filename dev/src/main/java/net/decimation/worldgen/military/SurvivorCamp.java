package net.decimation.worldgen.military;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/**
 * A walled survivor camp, a safezone outside the cities (docs/safezone.md). Canvas 47 x 47, gate
 * on the south side. Real references (docs/safezone.md section 1): a walled perimeter with
 * watchtowers and a guarded gate, a market street of trader stalls, communal facilities (fire,
 * water point, latrines) in the middle and lit, family tents grouped around them, a garden.
 * Layout (canvas x east, z south):
 * - scrap wall on 2 / 44 (rusty sheets, dark plate, spruce boards between log posts, rough top),
 *   concertina wire outside, four corner watchtowers with a guard each;
 * - gate in the south wall, 5 fence gates between log posts, safezone entry / exit signs over it,
 *   two guards inside beside it;
 * - market street north from the gate (x 21..25), three stalls each side with a trader behind a
 *   counter under a striped awning, goods behind;
 * - plaza (x 15..31, z 15..24): campfire with benches, light towers, water point;
 * - family tents north and on both sides, a medical tent north of the plaza, latrines,
 *   generator yard, a garden.
 * Residents (posts, canvas coordinates): traders, guards, civilians and armed survivors.
 */
final class SurvivorCamp
{
    static final int W = 47, L = 47, HEIGHT = 12;
    static final int E = 2, S = 3, WEST = 4, N = 5;
    /** Wall lines and the gate opening. */
    static final int X0 = 2, X1 = 44, Z0 = 2, Z1 = 44, GX0 = 21, GX1 = 25;

    /** One resident: kind ("guard", "trader:TraderGuns", "civilian", "survivor"), cell, yaw. */
    static final class Post
    {
        final String kind;
        final double x, y, z;
        final float yaw;

        Post(String kind, double x, double y, double z, float yaw)
        {
            this.kind = kind;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
        }
    }

    final Canvas c = new Canvas(W, HEIGHT, L);
    final List<Post> posts = new ArrayList<Post>();
    final Map<String, float[]> poi = new LinkedHashMap<String, float[]>();
    final List<String> log = new ArrayList<String>();
    private final Random r;
    private final MilitaryBase.B b = MilitaryBase.B.get();
    private Block log1, wool;

    SurvivorCamp(long seed)
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
        posts.add(new Post(kind, x + 0.5, y, z + 0.5, yaw));
    }

    Canvas build()
    {
        log1 = Blocks.log;
        wool = Blocks.wool;
        ground();
        wall();
        gate();
        towers();
        street();
        plaza();
        stalls();
        medicalTent(19, 5);
        int[][] tents = {{7, 8}, {13, 8}, {29, 8}, {8, 16}, {8, 24}, {35, 16}, {35, 24}};
        int n = 0;
        for (int[] t : tents)
        {
            familyTent(t[0], t[1], n++);
        }
        latrines(36, 10);
        generatorYard(7, 33);
        garden(33, 33);
        clutter();
        people();
        c.validateProps(log);
        return c;
    }

    // ================================================================ ground, wall, gate

    private void ground()
    {
        for (int z = 0; z < L; z++)
        {
            for (int x = 0; x < W; x++)
            {
                double n = noise(x, z);
                c.set(x, 0, z, n > 0.82 ? Blocks.dirt : Blocks.gravel);
            }
        }
    }

    private double noise(int x, int z)
    {
        long h = (x * 73856093L) ^ (z * 19349663L) ^ 0x5AFEL;
        h ^= h >>> 13;
        h *= 0x9E3779B97F4A7C15L;
        return ((h >>> 11) & 0xFFFFF) / (double) 0xFFFFF;
    }

    private boolean onWall(int x, int z)
    {
        return (x == X0 || x == X1) && z >= Z0 && z <= Z1 || (z == Z0 || z == Z1) && x >= X0 && x <= X1;
    }

    /** Scrap wall: panels of one material between log posts every 4 cells, a rough top. */
    private void wall()
    {
        Block[] sheets = {b.metalRust, b.metalPlate, Blocks.planks};
        for (int z = Z0; z <= Z1; z++)
        {
            for (int x = X0; x <= X1; x++)
            {
                if (!onWall(x, z) || z == Z1 && x >= GX0 - 1 && x <= GX1 + 1)
                {
                    continue;
                }
                int along = z == Z0 || z == Z1 ? x : z;
                if (along % 4 == 2)
                {
                    c.fill(x, 1, z, x, 4, z, log1);
                    c.set(x, 5, z, Blocks.torch, 5);
                    continue;
                }
                int panel = along / 4 + (x == X0 || x == X1 ? 100 : 0) + (z == Z1 ? 200 : 0) + (x == X1 ? 300 : 0);
                Block s = sheets[(int) (noise(panel, 7) * 3) % 3];
                int m = s == Blocks.planks ? 1 : 0;
                c.fill(x, 1, z, x, 3, z, s, m);
                if (noise(x, z + 50) > 0.7)
                {
                    c.set(x, 4, z, s, m);                    // a sheet stacked higher here and there
                }
            }
        }
        // concertina outside, with gaps, never in front of the gate
        for (int z = 0; z < L; z++)
        {
            for (int x = 0; x < W; x++)
            {
                boolean ring = x == 0 || z == 0 || x == W - 1 || z == L - 1;
                if (ring && !(z == L - 1 && x >= GX0 - 3 && x <= GX1 + 3) && noise(x + 9, z) > 0.25)
                {
                    c.set(x, 1, z, b.wire);
                }
            }
        }
    }

    private void gate()
    {
        c.fill(GX0 - 1, 1, Z1, GX0 - 1, 5, Z1, log1);
        c.fill(GX1 + 1, 1, Z1, GX1 + 1, 5, Z1, log1);
        for (int x = GX0; x <= GX1; x++)
        {
            c.set(x, 1, Z1, Blocks.fence_gate, 0);         // closed, spanning along x
            c.set(x, 5, Z1, log1, 4);                      // lintel, logs along x
            c.set(x, 0, Z1, Blocks.cobblestone);
        }
        // the entry text one block outside the lintel (on it, the logs hid it: user review 11 Oktober
        // 2026), the exit text one block inside
        c.prop((GX0 + GX1) / 2, 6, Z1 + 1, d("BlockSafezoneSignEntry"), S);
        c.prop((GX0 + GX1) / 2, 6, Z1 - 1, d("BlockSafezoneSignExit"), N);
        // sandbag guard posts inside beside the gate
        for (int gx : new int[] {GX0 - 1, GX1 + 1})
        {
            c.set(gx, 1, Z1 - 2, b.sandbag);
            post("guard", gx, 1, Z1 - 1, 0);
        }
        poi("gate_outside", (GX0 + GX1) / 2 + 0.5, 1.6, Z1 + 7, 180, 5);
        poi("gate_inside", (GX0 + GX1) / 2 + 0.5, 1.6, Z1 - 4, 0, 5);
    }

    // ================================================================ towers

    private void towers()
    {
        tower(X0 + 1, Z0 + 1, 1, 1);
        tower(X1 - 4, Z0 + 1, -1, 1);
        tower(X0 + 1, Z1 - 4, 1, -1);
        tower(X1 - 4, Z1 - 4, -1, -1);
    }

    /**
     * Wooden watchtower 4 x 4 against the wall corner: a plank filled base up to 3, a ladder in a
     * shaft on one of the faces turned to the camp (the cell in front stays open), a deck at 4 with
     * a fence railing, log corner posts, a slab roof at 8, a guard on the deck.
     */
    private void tower(int x0, int z0, int ix, int iz)
    {
        c.fill(x0, 1, z0, x0 + 3, 3, z0 + 3, Blocks.planks, 1);
        int lx = x0 + (ix > 0 ? 2 : 1), lz = iz > 0 ? z0 + 3 : z0;   // a middle cell of the z face toward the camp
        int fdz = iz;
        c.clearBox(lx, 1, lz, lx, 4, lz);
        for (int y = 1; y <= 4; y++)
        {
            c.set(lx, y, lz, Blocks.ladder, fdz > 0 ? 3 : 2);
        }
        c.clear(lx, 1, lz + fdz);
        c.clear(lx, 2, lz + fdz);
        for (int x = x0; x <= x0 + 3; x++)
        {
            for (int z = z0; z <= z0 + 3; z++)
            {
                if (!(x == lx && z == lz))
                {
                    c.set(x, 4, z, Blocks.planks, 1);
                }
                boolean edge = x == x0 || x == x0 + 3 || z == z0 || z == z0 + 3;
                boolean corner = (x == x0 || x == x0 + 3) && (z == z0 || z == z0 + 3);
                if (corner)
                {
                    c.fill(x, 5, z, x, 7, z, log1);
                }
                else if (edge && !(x == lx && z == lz) && !(Math.abs(x - lx) <= 1 && z == lz))
                {
                    c.set(x, 5, z, Blocks.fence);
                }
            }
        }
        for (int x = x0 - 1; x <= x0 + 4; x++)
        {
            for (int z = z0 - 1; z <= z0 + 4; z++)
            {
                boolean mid = x >= x0 && x <= x0 + 3 && z >= z0 && z <= z0 + 3;
                c.set(x, 8, z, mid ? Blocks.planks : Blocks.wooden_slab, 1);
            }
        }
        // the guard on the deck cell farthest from the hatch, looking out over the corner
        int gx = ix > 0 ? x0 + 1 : x0 + 2, gz = iz > 0 ? z0 + 1 : z0 + 2;
        post("guard", gx, 5, gz, iz > 0 ? (ix > 0 ? 135 : 225) : (ix > 0 ? 45 : 315));
        poi("tower_ladder", lx + 0.5, 1.6, lz + 4 * fdz + 0.5, fdz > 0 ? 180 : 0, 20);
        poi("tower_deck", x0 + 1.5 + (ix > 0 ? 0.5 : 0), 6.6, z0 + 1.5 + (iz > 0 ? 0.5 : 0), ix > 0 ? 315 : 45, 18);
    }

    // ================================================================ street, plaza

    private void street()
    {
        for (int z = 15; z <= Z1 - 1; z++)
        {
            for (int x = GX0; x <= GX1; x++)
            {
                c.set(x, 0, z, Blocks.cobblestone);
            }
        }
        poi("main_road", (GX0 + GX1) / 2 + 0.5, 1.6, Z1 - 3, 180, 8);
    }

    private void plaza()
    {
        for (int z = 15; z <= 24; z++)
        {
            for (int x = 15; x <= 31; x++)
            {
                c.set(x, 0, z, noise(x, z + 99) > 0.85 ? Blocks.gravel : Blocks.cobblestone);
            }
        }
        // campfire: netherrack under a fire, a ring of stone slabs, benches 3 away facing it
        int fx = 23, fz = 19;
        c.set(fx, 0, fz, Blocks.netherrack);
        c.set(fx, 1, fz, Blocks.fire);
        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dz = -1; dz <= 1; dz++)
            {
                if (dx != 0 || dz != 0)
                {
                    c.set(fx + dx, 1, fz + dz, Blocks.stone_slab);
                }
            }
        }
        c.prop(fx, 1, fz - 3, b.bench, S);
        c.prop(fx, 1, fz + 3, b.bench, N);
        c.prop(fx - 3, 1, fz, b.bench, E);
        c.prop(fx + 3, 1, fz, b.bench, WEST);
        // light towers on the plaza corners, a water point on its east side
        c.prop(15, 1, 15, b.lightTower, S);
        c.prop(31, 1, 15, b.lightTower, S);
        c.prop(15, 1, 24, b.lightTower, N);
        c.prop(31, 1, 24, b.lightTower, N);
        c.fill(29, 1, 16, 30, 2, 17, Blocks.iron_block);   // water tank
        c.set(29, 1, 19, Blocks.cauldron, 3);
        c.set(30, 1, 19, Blocks.cauldron, 3);
        c.prop(29, 1, 21, b.waterPallet, WEST);
        c.prop(30, 1, 22, b.waterPalletTarp, WEST);
        poi("plaza", 17.5, 2.6, 23.5, 225, 12);
        poi("campfire", fx + 0.5, 3.6, fz + 6.5, 180, 22);
    }

    // ================================================================ market

    private static final String[] FIXED = {"TraderGuns", "TraderAmmunition", "TraderFood", "TraderMedical"};
    private static final String[] EXTRA = {"TraderArmor", "TraderBackpack", "TraderConvenience", "TraderScrap",
        "TraderFortification", "TraderBlackMarket", "TraderHeavyGuns", "TraderGoldenCap"};
    /** Awning stripes per stall: wool colours (white with red, blue, green, orange, yellow, brown). */
    private static final int[] STRIPE = {14, 11, 13, 1, 4, 12};

    private void stalls()
    {
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
        int[] zs = {27, 32, 37};
        for (int i = 0; i < 6; i++)
        {
            stall(i < 3, zs[i % 3], traders.get(i), STRIPE[i]);
        }
        poi("market", (GX0 + GX1) / 2 + 0.5, 2.6, 41.5, 180, 12);
    }

    /**
     * A stall 5 deep (across) x 4 long (along the street): a plank floor, a board back wall, the
     * counter (two wood tables, drawn 2 long each) at the street edge, the trader behind it, goods
     * at the back, an awning of striped wool on fence posts reaching over the counter.
     */
    private void stall(boolean west, int z0, String trader, int stripe)
    {
        int z1 = z0 + 3;
        int front = west ? 19 : 27, back = west ? 15 : 31, dir = west ? 1 : -1;  // dir: toward the street
        for (int z = z0; z <= z1; z++)
        {
            for (int k = 0; k <= 4; k++)
            {
                c.set(back + dir * k, 0, z, Blocks.planks, 1);
            }
            c.fill(back, 1, z, back, 3, z, Blocks.planks, 1);
            for (int k = 0; k <= 5; k++)
            {
                int x = back + dir * k;
                // colour on the odd columns: the front edge (k 5), the one seen from the street, is coloured
                c.set(x, 4, z, wool, (k & 1) == 1 ? stripe : 0);
            }
        }
        for (int z : new int[] {z0, z1})
        {
            c.fill(front + dir, 1, z, front + dir, 3, z, Blocks.fence);
        }
        int face = west ? E : WEST;
        c.prop(front, 1, z0, b.woodTable, face);
        c.prop(front, 1, z0 + 2, b.woodTable, face);
        int tx = front - dir;
        post("trader:" + trader, tx, 1, z0 + 1, west ? 270 : 90);
        // goods at the back, by trade
        int gx = back + dir;
        Block[] goods;
        if (trader.contains("Guns") || trader.contains("BlackMarket"))
        {
            goods = new Block[] {b.weaponCabinet, b.ammoCrate, b.weaponCabinet};
        }
        else if (trader.contains("Ammunition"))
        {
            goods = new Block[] {b.ammoCrateLarge, b.militaryCrate, b.ammoCrate};
        }
        else if (trader.contains("Food"))
        {
            goods = new Block[] {b.waterPallet, b.cardboard, b.woodCrate};
        }
        else if (trader.contains("Medical"))
        {
            goods = new Block[] {b.medicalCrate, b.medicalCrate, b.medicalCrate};
        }
        else if (trader.contains("Armor") || trader.contains("Backpack"))
        {
            goods = new Block[] {b.storageCrate, b.militaryCrate, b.storageCrate};
        }
        else
        {
            goods = new Block[] {b.woodCrate, b.cardboard, b.barrel};
        }
        // cases and cabinets are drawn up to 1.9 long: two goods 2 cells apart along the back wall
        c.prop(gx, 1, z0, goods[0], face);
        c.prop(gx, 1, z0 + 2, goods[1], face);
        poi("stall_" + trader.substring(6).toLowerCase(), (west ? 23 : 23) + 0.5, 1.6, z0 + 1.5, west ? 90 : 270, 10);
    }

    // ================================================================ tents and services

    /** Wool / canvas colours of family tents: sandstone tan, brown, green, grey wool. */
    private void familyTent(int x0, int z0, int n)
    {
        int w = 5, l = 6, x1 = x0 + w - 1, z1 = z0 + l - 1, mx = x0 + 2;
        if (!c.free(x0 - 1, z0 - 1, x1 + 1, z1 + 1, 4))
        {
            log.add("refused family tent at " + x0 + "," + z0);
            return;
        }
        int kind = (int) (noise(x0, z0) * 4);
        Block skin = kind == 0 ? Blocks.sandstone : wool;
        int skinMeta = kind == 0 ? 2 : kind == 1 ? 12 : kind == 2 ? 13 : 7;
        for (int z = z0; z <= z1; z++)
        {
            boolean end = z == z0 || z == z1;
            for (int x = x0; x <= x1; x++)
            {
                int d = Math.min(x - x0, x1 - x);
                c.set(x, 0, z, Blocks.planks, 2);
                int top = d == 0 ? 2 : d == 1 ? 3 : 4;
                for (int y = 1; y <= top; y++)
                {
                    boolean s = y == top || d == 0 || end;
                    if (s)
                    {
                        c.set(x, y, z, skin, skinMeta);
                    }
                }
            }
        }
        c.clear(mx, 1, z1);
        c.clear(mx, 2, z1);
        // a bed along the west side (foot south, head north), a footlocker and a lamp
        c.set(x0 + 1, 1, z0 + 2, Blocks.bed, 2);
        c.set(x0 + 1, 1, z0 + 1, Blocks.bed, 2 | 8);
        c.prop(x1 - 1, 1, z0 + 1, b.storageCrate, WEST);
        c.prop(mx, 3, z0 + 3, b.light, S);
        if (n == 0)
        {
            poi("tent_inside", mx + 0.5, 1.6, z1 - 0.5, 180, 10);
        }
    }

    private void medicalTent(int x0, int z0)
    {
        int w = 9, l = 8, x1 = x0 + w - 1, z1 = z0 + l - 1, mx = x0 + 4;
        for (int z = z0; z <= z1; z++)
        {
            boolean end = z == z0 || z == z1;
            for (int x = x0; x <= x1; x++)
            {
                int d = Math.min(x - x0, x1 - x);
                c.set(x, 0, z, Blocks.planks, 2);
                int top = d == 0 ? 2 : d == 1 ? 3 : 4;
                for (int y = 1; y <= top; y++)
                {
                    boolean s = y == top || d == 0 || end;
                    if (s)
                    {
                        c.set(x, y, z, wool, 0);              // white canvas
                    }
                }
            }
        }
        c.set(mx, 4, z1, wool, 14);                           // a red mark over the door
        c.clear(mx, 1, z1);
        c.clear(mx, 2, z1);
        c.clear(mx - 1, 1, z1);
        c.clear(mx - 1, 2, z1);
        // stretchers drawn 2.1 long: one cell in from the end walls, 3 apart
        for (int z = z0 + 2; z + 1 <= z1 - 2; z += 3)
        {
            c.prop(x0 + 1, 1, z, b.stretcher, E);
            c.prop(x1 - 1, 1, z, b.stretcher, WEST);
        }
        c.prop(mx, 1, z0 + 1, b.medicalCrate, S);
        c.prop(mx + 1, 1, z0 + 1, b.medicalCrate, S);
        c.prop(mx, 3, z0 + 3, b.light, S);
        c.prop(mx, 3, z0 + 6, b.light, S);
        poi("medical_inside", mx + 0.5, 1.6, z1 - 0.5, 180, 10);
    }

    /** Two plank outhouses with doors facing south (an empty stall: a seat would leave no head room), a wash cauldron between them. */
    private void latrines(int x0, int z0)
    {
        for (int k = 0; k < 2; k++)
        {
            int x = x0 + k * 3;
            c.fill(x - 1, 1, z0 - 1, x + 1, 3, z0 + 1, Blocks.planks, 0);
            c.clearBox(x, 1, z0, x, 2, z0);
            c.set(x, 1, z0 + 1, Blocks.wooden_door, 3);
            c.set(x, 2, z0 + 1, Blocks.wooden_door, 8);
            c.fill(x - 1, 4, z0 - 1, x + 1, 4, z0 + 1, Blocks.wooden_slab, 0);
        }
        c.set(x0 + 1, 1, z0 + 2, Blocks.cauldron, 2);
        poi("latrines", x0 + 1.5, 1.6, z0 + 6, 180, 10);
    }

    private void generatorYard(int x0, int z0)
    {
        c.prop(x0 + 1, 1, z0 + 1, b.generator, S);
        c.prop(x0 + 3, 1, z0 + 1, b.barrel, S);
        c.prop(x0 + 4, 1, z0 + 1, b.barrel, S);
        c.prop(x0 + 3, 1, z0 + 3, b.tireStack, S);
        c.prop(x0 + 1, 1, z0 + 4, b.woodCrate, E);
        c.prop(x0 + 5, 1, z0 + 4, b.lightTower, N);
        poi("generator", x0 + 3.5, 2.6, z0 + 7.5, 180, 15);
    }

    /** Farmland rows with carrots, potatoes and wheat around a water channel, a fence with a gate. */
    private void garden(int x0, int z0)
    {
        int x1 = x0 + 6, z1 = z0 + 6;
        for (int z = z0; z <= z1; z++)
        {
            for (int x = x0; x <= x1; x++)
            {
                boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                if (edge)
                {
                    c.set(x, 1, z, Blocks.fence);
                    continue;
                }
                if (x == x0 + 3)
                {
                    c.set(x, 0, z, Blocks.water);
                    continue;
                }
                c.set(x, 0, z, Blocks.farmland, 7);
                Block crop = z < z0 + 3 ? Blocks.carrots : z < z0 + 5 ? Blocks.potatoes : Blocks.wheat;
                c.set(x, 1, z, crop, 7);
            }
        }
        c.set(x0 + 3, 1, z0, Blocks.fence_gate, 0);
        poi("garden", x0 + 3.5, 3.6, z0 - 3, 0, 30);
    }

    // ================================================================ clutter

    /**
     * Life along the walls and in the corners (user review 11 Oktober 2026: "add more cosmetic
     * props ... jeep cars, package, crate"): parked vehicles in the strips between the wall and the
     * tents, supply stacks, care packages, barrels and tyres, a table with chairs on the plaza, odds
     * and ends in the gaps between the stalls. Every prop from its drawn box (validateProps).
     */
    private void clutter()
    {
        Block car = d("BlockWreckage1");
        // west strip (x 3..6): a military jeep and a car, both long along z
        c.prop(4, 1, 12, b.jeep, S);
        c.prop(5, 1, 30, car != null ? car : b.jeep, S);
        c.prop(4, 1, 22, b.tireStack, S);
        c.prop(4, 1, 24, b.barrel, S);
        c.prop(5, 1, 24, b.barrel, S);
        // east strip (x 40..43): a jeep, a care package, tyres and barrels
        c.prop(42, 1, 20, b.jeep, S);                          // military wrecks lie along z at 2 / 3
        c.prop(42, 1, 28, b.carePackage, WEST);
        c.prop(42, 1, 32, b.tireStack, S);
        c.prop(42, 1, 35, b.barrel, S);
        c.prop(41, 1, 35, b.barrel, S);
        c.prop(42, 1, 37, b.trashBag, S);
        // north strip (z 3..6): supply stacks
        c.prop(9, 1, 4, b.woodCrate, E);                       // drawn 1.4 long across the front: side by side facing E
        c.prop(10, 1, 4, b.woodCrate, E);
        c.prop(12, 1, 4, b.waterPalletTarp, S);
        c.prop(14, 1, 4, b.cardboard, S);
        c.prop(16, 1, 4, b.barrel, S);
        c.prop(30, 1, 4, b.woodCrate, E);
        c.prop(31, 1, 4, b.woodCrateOpen != null ? b.woodCrateOpen : b.woodCrate, E);
        c.prop(35, 1, 5, b.carePackage, S);
        // south strip: a jeep along x west of the gate, tyres, barrels and bags east of it
        c.prop(10, 1, 42, b.jeep, WEST);
        c.prop(33, 1, 42, b.tireStack, S);
        c.prop(35, 1, 42, b.barrel, S);
        c.prop(36, 1, 42, b.barrel, S);
        c.prop(38, 1, 42, b.trashBag, S);
        // plaza: a table with two chairs west of the fire
        c.prop(18, 1, 21, b.woodTable, E);
        c.prop(17, 1, 21, b.chair, E);
        c.prop(19, 1, 22, b.chair, WEST);
        // the gaps between the stalls: a barrel and a bag at the back
        c.prop(16, 1, 31, b.barrel, S);
        c.prop(16, 1, 36, b.trashBag, S);
        c.prop(30, 1, 31, b.barrel, S);
        c.prop(30, 1, 36, b.trashBag2 != null ? b.trashBag2 : b.trashBag, S);
        poi("clutter_west", 5.5, 2.6, 38.5, 180, 15);
        poi("clutter_east", 41.5, 2.6, 40.5, 180, 15);
    }

    // ================================================================ people

    /** Civilians and armed survivors on free cells of the plaza, the street and between the tents. */
    private void people()
    {
        int[][] civ = {{18, 18}, {28, 22}, {20, 23}, {13, 20}, {33, 28}};
        int[][] surv = {{26, 17}, {17, 22}, {23, 30}};
        for (int[] p : civ)
        {
            int[] f = standing(p[0], p[1]);
            if (f != null)
            {
                post("civilian", f[0], 1, f[1], r.nextInt(360));
            }
        }
        for (int[] p : surv)
        {
            int[] f = standing(p[0], p[1]);
            if (f != null)
            {
                post("survivor", f[0], 1, f[1], r.nextInt(360));
            }
        }
        poi("aerial", W / 2 + 0.5, 34, L + 18, 180, 50);
    }

    /** The nearest cell to (x, z) with solid ground and two free cells above, or null. */
    private int[] standing(int x, int z)
    {
        for (int d = 0; d < 6; d++)
        {
            for (int dx = -d; dx <= d; dx++)
            {
                for (int dz = -d; dz <= d; dz++)
                {
                    int px = x + dx, pz = z + dz;
                    Block g = c.get(px, 0, pz);
                    if (g != null && g != Blocks.water && c.get(px, 1, pz) == null && c.get(px, 2, pz) == null
                        && px > X0 + 4 && px < X1 - 4 && pz > Z0 + 4 && pz < Z1 - 4)
                    {
                        return new int[] {px, pz};
                    }
                }
            }
        }
        log.add("no standing cell near " + x + "," + z);
        return null;
    }

    private static Block d(String name)
    {
        Block k = Block.getBlockFromName("deci:" + name);
        return k != null && k != Blocks.air ? k : null;
    }
}

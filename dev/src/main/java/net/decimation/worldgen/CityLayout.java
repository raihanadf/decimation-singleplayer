package net.decimation.worldgen;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cities of many sizes (docs/city_engine.md "City sizes"; user, 13 Oktober 2026: "city have variant size,
 * so its not stuck on 4 district like we have now, we can have 8, 16, 24, 48 or even 64 district inside one
 * city"; choices: each size more likely than first offered, compact blobs, cities and roads less rare,
 * one wasteland sector per 4 city sectors on the side toward the military).
 * <p>
 * The world is cut into cells of CELL x CELL sectors (Sectors.REGION, 256 blocks; a sector holds 4
 * districts). Each cell may hold one city, grown as a compact blob of 1, 2, 4, 6, 12 or 16 sectors (4 to
 * 64 districts) inside the cell's BOX x BOX corner; the last row and column of every cell stay free, so
 * two cities never touch. A city of 2+ sectors gets a military sector beside it, on one side, and its
 * wasteland sectors (1 per 4, at least 1) are the city sectors nearest to it. Every other sector rolls
 * wilderness / civilian / military as before, without city. About 24 % of the land is city (was 15 %,
 * most cities one sector).
 * <p>
 * New worlds only (StructureData KEY, set by CityLayoutEvents): ON by default, because Minecraft picks a new
 * world's spawn (biome search through Sectors) inside the World constructor, before WorldEvent.Load; an
 * older world switches it off when it loads, and unloading the overworld switches it back on.
 * config/deciworldgen_cities.cfg: size weights and the chance a cell has a city (changing them changes
 * where cities are in every world made with this layout).
 */
public final class CityLayout
{
    public static final String KEY = "!citySizes";
    /** Cell size in sectors; cities grow inside the BOX x BOX corner of their cell. */
    public static final int CELL = 5, BOX = 4;
    /** City sizes in sectors (x 4 = districts). */
    public static final int[] SIZES = {1, 2, 4, 6, 12, 16};
    /** Default weights (user: the first offer 15 20 25 20 13 7, each plus 15). */
    public static final int[] DEFAULT_WEIGHTS = {30, 35, 40, 35, 28, 22};

    private static volatile boolean on = true;
    private static volatile int epoch;
    private static volatile int[] weights = DEFAULT_WEIGHTS.clone();
    private static volatile double chance = 1.0;
    private static final Map<Long, Plan> PLANS = new ConcurrentHashMap<Long, Plan>();
    private static volatile long plansSeed;

    private CityLayout()
    {
    }

    /** One cell's city: its sectors, wasteland sectors and the military sector beside it (keys). */
    public static final class Plan
    {
        public final Set<Long> city = new HashSet<Long>(), waste = new HashSet<Long>();
        public long mil = Long.MIN_VALUE;
        public int size;
    }

    /** True when this world uses city sizes (new worlds since v0.60.0). */
    public static boolean on()
    {
        return on;
    }

    /** Changes with every switch: caches of sector answers compare it. */
    public static int epoch()
    {
        return epoch;
    }

    /** Switches the layout (world load / unload, dev tests); every cached plan is dropped. */
    public static synchronized void set(boolean v)
    {
        if (on != v)
        {
            on = v;
            epoch++;
            PLANS.clear();
            net.decimation.worldgen.city.LcCity.layoutChanged();
        }
    }

    /** Size weights (in SIZES order) and the chance a cell holds a city (config). */
    public static synchronized void configure(int[] w, double c)
    {
        if (w != null && w.length == SIZES.length)
        {
            weights = w.clone();
        }
        chance = Math.max(0, Math.min(1, c));
        epoch++;
        PLANS.clear();
    }

    public static long key(int rx, int rz)
    {
        return ((long) rx << 32) ^ (rz & 0xffffffffL);
    }

    /** CITY, MIL for the military sector beside a city, or -1: roll as usual (without city). */
    static int sector(long seed, int rx, int rz)
    {
        int cx = Math.floorDiv(rx, CELL), cz = Math.floorDiv(rz, CELL);
        long k = key(rx, rz);
        Plan p = plan(seed, cx, cz);
        if (p.city.contains(k))
        {
            return StructureGenerator.CITY;
        }
        // a city's military sector is beside it: in its own cell or in the free row / column of the next one
        if (p.mil == k || plan(seed, cx - 1, cz).mil == k || plan(seed, cx + 1, cz).mil == k
            || plan(seed, cx, cz - 1).mil == k || plan(seed, cx, cz + 1).mil == k)
        {
            return StructureGenerator.MIL;
        }
        return -1;
    }

    /** A wasteland city sector of this layout. */
    public static boolean wasteland(long seed, int rx, int rz)
    {
        return plan(seed, Math.floorDiv(rx, CELL), Math.floorDiv(rz, CELL)).waste.contains(key(rx, rz));
    }

    /** The plan of the cell holding sector (rx, rz). */
    public static Plan planAt(long seed, int rx, int rz)
    {
        return plan(seed, Math.floorDiv(rx, CELL), Math.floorDiv(rz, CELL));
    }

    static Plan plan(long seed, int cx, int cz)
    {
        if (seed != plansSeed || PLANS.size() > 16384)
        {
            PLANS.clear();
            plansSeed = seed;
        }
        Long k = key(cx, cz);
        Plan p = PLANS.get(k);
        if (p == null)
        {
            p = build(seed, cx, cz);
            PLANS.put(k, p);
        }
        return p;
    }

    private static Plan build(long seed, int cx, int cz)
    {
        Plan p = new Plan();
        Random r = new Random(seed ^ (cx * 341873128712L + cz * 132897987541L) ^ 0x434954594cL);
        if (r.nextDouble() >= chance)
        {
            return p;
        }
        int[] w = weights;
        int total = 0;
        for (int x : w)
        {
            total += Math.max(0, x);
        }
        if (total <= 0)
        {
            return p;
        }
        int roll = r.nextInt(total), pick = 0;
        while (roll >= Math.max(0, w[pick]))
        {
            roll -= Math.max(0, w[pick]);
            pick++;
        }
        p.size = SIZES[pick];
        int ox = cx * CELL, oz = cz * CELL;
        // grow a compact blob inside the box: each step the free box sector with the most city neighbours,
        // nearest to the blob's middle (a little noise breaks ties)
        int sx = ox + r.nextInt(BOX), sz = oz + r.nextInt(BOX);
        List<int[]> cells = new ArrayList<int[]>();
        cells.add(new int[] {sx, sz});
        p.city.add(key(sx, sz));
        int[][] side = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (cells.size() < p.size)
        {
            double mx = 0, mz = 0;
            for (int[] c : cells)
            {
                mx += c[0];
                mz += c[1];
            }
            mx /= cells.size();
            mz /= cells.size();
            int[] best = null;
            double bestScore = -1e9;
            for (int x = ox; x < ox + BOX; x++)
            {
                for (int z = oz; z < oz + BOX; z++)
                {
                    if (p.city.contains(key(x, z)))
                    {
                        continue;
                    }
                    int n = 0;
                    for (int[] s : side)
                    {
                        n += p.city.contains(key(x + s[0], z + s[1])) ? 1 : 0;
                    }
                    if (n == 0)
                    {
                        continue;
                    }
                    double score = 3 * n - ((x - mx) * (x - mx) + (z - mz) * (z - mz)) + r.nextDouble();
                    if (score > bestScore)
                    {
                        bestScore = score;
                        best = new int[] {x, z};
                    }
                }
            }
            cells.add(best);
            p.city.add(key(best[0], best[1]));
        }
        if (p.size < 2)
        {
            return p;
        }
        // the military sector: beside the city's farthest sector toward one side
        int[] d = side[r.nextInt(4)];
        int[] edge = null;
        int far = Integer.MIN_VALUE;
        for (int[] c : cells)
        {
            int along = c[0] * d[0] + c[1] * d[1];
            if (!p.city.contains(key(c[0] + d[0], c[1] + d[1])) && (along > far || along == far && r.nextBoolean()))
            {
                far = along;
                edge = c;
            }
        }
        p.mil = key(edge[0] + d[0], edge[1] + d[1]);
        // wasteland: 1 per 4 city sectors (at least 1), the city sectors nearest the military one
        int want = Math.max(1, p.size / 4);
        ArrayDeque<int[]> q = new ArrayDeque<int[]>();
        q.add(edge);
        Set<Long> seen = new HashSet<Long>();
        seen.add(key(edge[0], edge[1]));
        while (!q.isEmpty() && p.waste.size() < want)
        {
            int[] c = q.poll();
            p.waste.add(key(c[0], c[1]));
            for (int[] s : side)
            {
                long n = key(c[0] + s[0], c[1] + s[1]);
                if (p.city.contains(n) && seen.add(n))
                {
                    q.add(new int[] {c[0] + s[0], c[1] + s[1]});
                }
            }
        }
        return p;
    }

    /**
     * Map and numbers without the game: java -cp CLASSES net.decimation.worldgen.CityLayout SEED [RADIUS]
     * (RADIUS in cells, default 20). Prints the share of city land, cities per size, wasteland per city, and
     * a map of the middle (# city, W wasteland, M military beside a city, . other) when RADIUS <= 8.
     */
    public static void main(String[] a)
    {
        long seed = Long.parseLong(a[0]);
        int rad = a.length > 1 ? Integer.parseInt(a[1]) : 20;
        int[] count = new int[17], waste = new int[17];
        long city = 0, all = 0, wasteAll = 0;
        for (int cx = -rad; cx < rad; cx++)
        {
            for (int cz = -rad; cz < rad; cz++)
            {
                Plan p = plan(seed, cx, cz);
                count[p.size]++;
                waste[p.size] += p.waste.size();
                city += p.city.size();
                wasteAll += p.waste.size();
                all += CELL * CELL;
            }
        }
        System.out.printf("cells %d, city land %.1f %%, wasteland %.1f %% of city%n", 4 * rad * rad, 100.0 * city / all,
                          100.0 * wasteAll / Math.max(1, city));
        for (int s : SIZES)
        {
            System.out.printf("  %2d districts: %5d cities (%.1f %%), wasteland sectors each %.2f%n", s * 4, count[s],
                              100.0 * count[s] / (4 * rad * rad), count[s] == 0 ? 0 : (double) waste[s] / count[s]);
        }
        if (rad <= 8)
        {
            for (int rz = -rad * CELL; rz < rad * CELL; rz++)
            {
                StringBuilder b = new StringBuilder();
                for (int rx = -rad * CELL; rx < rad * CELL; rx++)
                {
                    int s = sector(seed, rx, rz);
                    b.append(wasteland(seed, rx, rz) ? 'W' : s == StructureGenerator.CITY ? '#' : s == StructureGenerator.MIL ? 'M' : '.');
                }
                System.out.println(b);
            }
        }
    }
}

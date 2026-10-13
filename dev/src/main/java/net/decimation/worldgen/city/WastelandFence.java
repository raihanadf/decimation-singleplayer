package net.decimation.worldgen.city;

import net.decimation.fixes.Deci;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import static net.decimation.worldgen.city.LcCity.*;

/**
 * The fence round a wasteland city standing apart (Wasteland.apart; user, 13 Oktober 2026: "make full
 * covered fence using decimation block? with barbed wire and full of landmine on the edge side of city.
 * this is only applicable on wasteland city", "cars portal in there like toll gate and military post
 * gate"; chosen: chain link and wire). Written column by column by EdgePlan.grade on the land a
 * wasteland cell owns, by the Chebyshev distance d to the wasteland (a square fence):
 * - d = FENCE: chain link (iron bars 3 high) between dark metal posts every 4, barbed wire on top;
 *   d = FENCE + 1: barbed wire at the foot every second cell;
 * - every street running out of the district (a north south street on the north and south sides, an
 *   east west one on the west and east sides) goes on through a gate: the street (sidewalks, road) out to
 *   ROAD_TO, at the fence line a row of striped barriers across the road between taller posts, and a
 *   guard booth inside on one sidewalk (a military post) or on both (a toll gate);
 * - MINES_FROM to MINES_TO outside: Decimation's landmines, about one in MINE_ONE_IN columns, none in
 *   front of a gate (they go off under a player only, without breaking blocks).
 */
final class WastelandFence
{
    static final int FENCE = 4, ROAD_TO = 9, MINES_FROM = 6, MINES_TO = 16, MINE_ONE_IN = 28;

    private WastelandFence()
    {
    }

    private static Block deci(String name)
    {
        Block b = Block.getBlockFromName("deci:" + name);
        return b == null || b == Blocks.air ? null : b;
    }

    /** One column at (x, z) whose top block is at y, owned by the city edge of cell (cx, cz). */
    static void column(World w, long seed, int cx, int cz, int x, int z, int y)
    {
        // the nearest wasteland cell, and how far out of it the column lies along x and z
        int best = Integer.MAX_VALUE, ox = 0, oz = 0, ex = 0, ez = 0, g = 0;
        for (int i = -1; i <= 1; i++)
        {
            for (int j = -1; j <= 1; j++)
            {
                if (!Wasteland.deadCell(seed, cx + i, cz + j))
                {
                    continue;
                }
                int x0 = (cx + i) * CELL * 16, z0 = (cz + j) * CELL * 16, x1 = x0 + CELL * 16 - 1, z1 = z0 + CELL * 16 - 1;
                int dx = Math.max(0, Math.max(x0 - x, x - x1)), dz = Math.max(0, Math.max(z0 - z, z - z1));
                int d = Math.max(dx, dz);
                if (d < best)
                {
                    best = d;
                    ox = dx;
                    oz = dz;
                    ex = Math.max(x0, Math.min(x1, x));       // the column brought onto the wasteland's edge
                    ez = Math.max(z0, Math.min(z1, z));
                    g = ground(seed, cx + i, cz + j);
                }
            }
        }
        int d = best;
        if (d < 1 || d > MINES_TO)
        {
            return;
        }
        // a gate: the edge chunk in front is a street running out (straight sides only, not corners)
        boolean nsSide = oz > 0 && ox == 0, weSide = ox > 0 && oz == 0;
        int bchx = ex >> 4, bchz = ez >> 4;
        boolean gate = false;
        if ((nsSide || weSide) && isStreetChunk(seed, bchx, bchz))
        {
            int[] b = block(seed, Math.floorDiv(bchx, CELL), Math.floorDiv(bchz, CELL));
            gate = nsSide ? bchx - b[0] * CELL == 0 : bchz - b[1] * CELL == 0;
        }
        if (gate)
        {
            gate(w, seed, x, z, d, nsSide, nsSide ? x & 15 : z & 15, g, y, bchx, bchz);
            return;
        }
        boolean alongX = oz >= ox;                         // the fence runs along x on the north and south sides
        if (d == FENCE)
        {
            boolean post = ((x + z) & 3) == 0;
            Block metal = deci("BlockMetal_2");
            for (int k = 1; k <= 3; k++)
            {
                w.setBlock(x, y + k, z, post && metal != null ? metal : Blocks.iron_bars, 0, 2);
            }
            Block wire = deci("BlockConcertinaWire");
            if (post && metal != null)
            {
                w.setBlock(x, y + 4, z, metal, 0, 2);
            }
            else if (wire != null)
            {
                w.setBlock(x, y + 4, z, wire, Math.max(2, PropFacing.along(wire, alongX, 0)), 2);
            }
            clearAbove(w, x, y + 5, z);
        }
        else if (d == FENCE + 1)
        {
            Block wire = deci("BlockConcertinaWire");
            if (wire != null && ((x + z) & 1) == 0 && (w.isAirBlock(x, y + 1, z) || plant(w, x, y + 1, z)))
            {
                w.setBlock(x, y + 1, z, wire, Math.max(2, PropFacing.along(wire, alongX, 0)), 2);
            }
        }
        else if (d >= MINES_FROM && hl(seed ^ 0x4D494E45L, x, z) % MINE_ONE_IN == 0
            && (w.isAirBlock(x, y + 1, z) || plant(w, x, y + 1, z)) && w.getBlock(x, y, z).getMaterial().isSolid())
        {
            w.setBlock(x, y + 1, z, Blocks.air, 0, 2);
            w.spawnEntityInWorld(Deci.newLandmine(w, x + 0.5, y + 1, z + 0.5));
        }
    }

    // ---- inside the city: the edge between a wasteland city sector and a city sector beside it (v0.57.1)

    /**
     * Which edge lines of a wasteland city sector inside the city pass through column (x, z): 1 = a line
     * along z at this x, 2 = a line along x at this z, 3 = both (a corner), 0 = none. A line is the first
     * column of the eastern (or southern) sector of a pair of city sectors where one is a wasteland city
     * and the other is not: the outer sidewalk of the street on the sector edge (cell row and column 0 are
     * always streets), so no building is touched.
     */
    static int line(long seed, int x, int z)
    {
        int r = net.decimation.worldgen.Sectors.REGION_BLOCKS, rx = Math.floorDiv(x, r), rz = Math.floorDiv(z, r);
        int out = 0;
        if (Math.floorMod(x, r) == 0 && edge(seed, rx - 1, rz, rx, rz))
        {
            out |= 1;
        }
        if (Math.floorMod(z, r) == 0 && edge(seed, rx, rz - 1, rx, rz))
        {
            out |= 2;
        }
        return out;
    }

    /**
     * A land column outside the city on the extension of an inside line (v0.57.1): the line goes on up to
     * FENCE blocks past the city edge, where it meets the land fence (column at distance FENCE), so the
     * corner has no gap. Called by EdgePlan for land owned by a city cell that is not wasteland.
     */
    static void lineOnLand(World w, long seed, int x, int z, int y)
    {
        int r = net.decimation.worldgen.Sectors.REGION_BLOCKS;
        boolean alongZ = Math.floorMod(x, r) == 0 && extends_(seed, x, z, true);
        boolean alongX = !alongZ && Math.floorMod(z, r) == 0 && extends_(seed, x, z, false);
        if (!alongZ && !alongX)
        {
            return;
        }
        boolean post = ((x + z) & 3) == 0;
        Block metal = deci("BlockMetal_2"), wire = deci("BlockConcertinaWire");
        for (int k = 1; k <= 3; k++)
        {
            w.setBlock(x, y + k, z, post && metal != null ? metal : Blocks.iron_bars, 0, 2);
        }
        if (post && metal != null)
        {
            w.setBlock(x, y + 4, z, metal, 0, 2);
        }
        else if (wire != null)
        {
            w.setBlock(x, y + 4, z, wire, Math.max(2, PropFacing.along(wire, alongX, 0)), 2);
        }
        clearAbove(w, x, y + 5, z);
    }

    /** The line at x (alongZ) or z continues within FENCE of a city sector pair it separates. */
    private static boolean extends_(long seed, int x, int z, boolean alongZ)
    {
        int r = net.decimation.worldgen.Sectors.REGION_BLOCKS;
        int along = alongZ ? z : x, fixed = Math.floorDiv(alongZ ? x : z, r);
        for (int k = -1; k <= 1; k++)
        {
            int ra = Math.floorDiv(along, r) + k, lo = ra * r, hi = lo + r - 1;
            int dist = Math.max(0, Math.max(lo - along, along - hi));
            if (dist >= 1 && dist <= FENCE
                && (alongZ ? edge(seed, fixed - 1, ra, fixed, ra) : edge(seed, ra, fixed - 1, ra, fixed)))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean edge(long seed, int ax, int az, int bx, int bz)
    {
        int c = net.decimation.worldgen.StructureGenerator.CITY;
        return net.decimation.worldgen.Sectors.regionSector(seed, ax, az) == c && net.decimation.worldgen.Sectors.regionSector(seed, bx, bz) == c
            && Wasteland.sector(seed, ax, az) != Wasteland.sector(seed, bx, bz);
    }

    /**
     * The fence of a street chunk on a wasteland city's edge inside the city (StreetPlan.blockAt): chain
     * link 3 high between posts every 4 with wire on top on the sidewalk, and where a street crosses the
     * line a gate: posts at the road's edges, striped barriers on every second cell across the road, open
     * between them. `surface` is the street's top at the column (plan y). Null when the cell is not fence.
     */
    static Block inCity(long seed, int x, int z, int ly, int surface, int[] meta)
    {
        int l = line(seed, x, z);
        if (l == 0 || ly <= surface || ly > surface + 4)
        {
            return null;
        }
        int k = ly - surface;
        Block metal = deci("BlockMetal_2"), wire = deci("BlockConcertinaWire");
        // a street crossing the line here: along x for a line along z (its across is z & 15), and so on
        boolean alongZ = (l & 1) != 0;
        int across = alongZ ? z & 15 : x & 15;
        int crossChunk = alongZ ? z >> 4 : x >> 4;
        boolean crossing = Math.floorMod(crossChunk, CELL) == 0 && l != 3;
        if (crossing && across >= SIDEWALK - 1 && across <= 16 - SIDEWALK)
        {
            meta[0] = 0;
            if (across == SIDEWALK - 1 || across == 16 - SIDEWALK)
            {
                return metal != null ? metal : Blocks.iron_bars;   // gate posts, 4 high
            }
            Block bar = deci("BlockHazardbarrier");
            if (k == 1 && (across & 1) == 1 && bar != null)
            {
                meta[0] = Math.max(2, PropFacing.along(bar, !alongZ, 0));
                return bar;
            }
            return null;
        }
        boolean post = ((x + z) & 3) == 0;
        meta[0] = 0;
        if (k <= 3)
        {
            return post && metal != null ? metal : Blocks.iron_bars;
        }
        if (post && metal != null)
        {
            return metal;
        }
        if (wire != null)
        {
            meta[0] = Math.max(2, PropFacing.along(wire, !alongZ, 0));
            return wire;
        }
        return null;
    }

    // ---- styled gates on the inside edge (v0.61.0, military/CityGate)

    private static final java.util.Map<Long, Object> GATES = new java.util.concurrent.ConcurrentHashMap<Long, Object>();
    private static final Object NONE = new Object();
    private static volatile long gatesSeed;
    private static volatile int gatesEpoch;

    /**
     * The styled gate owning column (x, z), or null: a street crossing an inside fence line (line()) whose
     * street chunk in front (west of a line along z, north of a line along x) is a plain street at the
     * same level as the crossing (no river, no ramp). It owns that chunk's 8 columns nearest the line, the
     * road cells of the line itself and of the crossing's first two columns (the posts and the fence on the
     * sidewalks stay inCity's). Look and state by the crossing's hash (military/CityGate).
     */
    static net.decimation.worldgen.military.CityGatePlan gate(long seed, int x, int z)
    {
        int r = net.decimation.worldgen.Sectors.REGION_BLOCKS;
        for (int axis = 0; axis < 2; axis++)
        {
            // axis 0: a line along z at x0 (street along x west of it); axis 1: a line along x at z0
            int out = axis == 0 ? x : z, across = axis == 0 ? z : x;
            if (Math.floorMod(across >> 4, CELL) != 0)
            {
                continue;
            }
            int a = across & 15;
            for (int line0 : new int[] {Math.floorDiv(out + 8, r) * r, Math.floorDiv(out, r) * r})
            {
                int d = line0 - out;                          // 1..8 in front, 0 the line, -1 / -2 the crossing's road
                if (d < -2 || d > 8 || d <= 0 && (a < SIDEWALK || a > 15 - SIDEWALK))
                {
                    continue;                                 // the fence on the line's sidewalks, the crossing's corners
                }
                net.decimation.worldgen.military.CityGatePlan g = gateAt(seed, axis, line0, across >> 4);
                if (g != null)
                {
                    return g;
                }
            }
        }
        return null;
    }

    /** True when the street chunk (chx, chz) is the street in front of a styled gate (its dressing skips the checkpoint). */
    static boolean gateApproach(long seed, int chx, int chz)
    {
        return gate(seed, chx * 16 + 15, chz * 16 + 8) != null || gate(seed, chx * 16 + 8, chz * 16 + 15) != null;
    }

    private static net.decimation.worldgen.military.CityGatePlan gateAt(long seed, int axis, int line0, int crossChunk)
    {
        int epoch = net.decimation.worldgen.CityLayout.epoch();
        if (seed != gatesSeed || epoch != gatesEpoch || GATES.size() > 4096)
        {
            GATES.clear();
            gatesSeed = seed;
            gatesEpoch = epoch;
        }
        long key = ((long) line0 << 33) ^ ((long) crossChunk << 1) ^ axis;
        Object o = GATES.get(key);
        if (o == null)
        {
            o = buildGate(seed, axis, line0, crossChunk);
            GATES.put(key, o == null ? NONE : o);
        }
        return o == NONE ? null : (net.decimation.worldgen.military.CityGatePlan) o;
    }

    private static net.decimation.worldgen.military.CityGatePlan buildGate(long seed, int axis, int line0, int crossChunk)
    {
        int mid = crossChunk * 16 + 8;
        int lx = axis == 0 ? line0 : mid, lz = axis == 0 ? mid : line0;
        if ((line(seed, lx, lz) & (axis == 0 ? 1 : 2)) == 0)
        {
            return null;
        }
        // the crossing chunk on the line and the street chunk in front of it
        int cx = axis == 0 ? line0 >> 4 : crossChunk, cz = axis == 0 ? crossChunk : line0 >> 4;
        int fx = axis == 0 ? cx - 1 : cx, fz = axis == 0 ? cz : cz - 1;
        if (!isStreetChunk(seed, fx, fz) || CityRivers.chunk(seed, fx, fz) || CityRivers.chunk(seed, cx, cz)
            || ground(seed, Math.floorDiv(fx, CELL), Math.floorDiv(fz, CELL)) != ground(seed, Math.floorDiv(cx, CELL), Math.floorDiv(cz, CELL)))
        {
            return null;
        }
        // look by the crossing's hash; 2 in 10 open (broken), else one of the four closures (user, 14 Oktober 2026)
        long h = hl(seed ^ 0x47415445L, cx * 2 + axis, cz);
        int look = (int) (h % net.decimation.worldgen.military.CityGate.LOOKS.length);
        int state = (h >>> 20) % 10 < 2 ? net.decimation.worldgen.military.CityGate.OPEN : 1 + (int) ((h >>> 28) % 4);
        String id = "lcg_" + cx + "_" + cz + "_" + axis;
        cpw.mods.fml.common.FMLLog.info("[deciworldgen] lc gate %s at %d,%d,%d (line along %s)", net.decimation.worldgen.military.CityGate.name(look, state),
                                        axis == 0 ? line0 : crossChunk * 16 + 8, ground(seed, Math.floorDiv(cx, CELL), Math.floorDiv(cz, CELL)),
                                        axis == 0 ? crossChunk * 16 + 8 : line0, axis == 0 ? "z" : "x");
        return axis == 0
            ? new net.decimation.worldgen.military.CityGatePlan(id, line0 - 8, crossChunk * 16, 1, look, state)
            : new net.decimation.worldgen.military.CityGatePlan(id, crossChunk * 16, line0 - 8, 2, look, state);
    }

    private static boolean plant(World w, int x, int y, int z)
    {
        return w.getBlock(x, y, z) instanceof net.minecraft.block.BlockBush;
    }

    private static void clearAbove(World w, int x, int y, int z)
    {
        for (int k = 0; k < 4; k++)
        {
            Block b = w.getBlock(x, y + k, z);
            if (b == Blocks.air)
            {
                break;
            }
            if (b instanceof net.minecraft.block.BlockBush || b == Blocks.leaves || b == Blocks.leaves2)
            {
                w.setBlock(x, y + k, z, Blocks.air, 0, 2);
            }
        }
    }

    /**
     * A column of a gate in front of the street chunk (bchx, bchz): a = position across the road (0..15,
     * the street's own across), d out from the wasteland, street level g, column top y.
     */
    private static void gate(World w, long seed, int x, int z, int d, boolean nsSide, int a, int g, int y, int bchx, int bchz)
    {
        if (d > ROAD_TO)
        {
            return;                                     // no mines on the way in
        }
        boolean toll = hl(seed ^ 0x544F4C4CL, bchx, bchz) % 2 == 0;
        // the street goes on: sidewalks and road at street level
        LcCity.reshape(w, x, z, y, g);
        boolean walk = a < SIDEWALK || a > 15 - SIDEWALK;
        Block road = Block.getBlockFromName("deci:BlockRoad");
        Block line = roadLine();
        if (walk)
        {
            w.setBlock(x, g, z, Blocks.double_stone_slab, 0, 2);
        }
        else if (a == 7 && ((nsSide ? z : x) & 3) < 2 && line != Blocks.bedrock)
        {
            w.setBlock(x, g, z, line, nsSide ? 4 : 2, 2);
        }
        else if (road != null && road != Blocks.air)
        {
            w.setBlock(x, g, z, road, 0, 2);
        }
        for (int k = 1; k <= 5; k++)
        {
            w.setBlock(x, g + k, z, Blocks.air, 0, 2);
        }
        Block metal = deci("BlockMetal_2");
        if (d == FENCE)
        {
            if ((a == 0 || a == 15) && metal != null)
            {
                for (int k = 1; k <= 4; k++)
                {
                    w.setBlock(x, g + k, z, metal, 0, 2);   // the gate posts
                }
            }
            else if (a >= SIDEWALK && a <= 15 - SIDEWALK && (a & 1) == 1)
            {
                Block bar = deci("BlockHazardbarrier");     // striped barriers across the road
                if (bar != null)
                {
                    w.setBlock(x, g + 1, z, bar, Math.max(2, PropFacing.along(bar, nsSide, 0)), 2);
                }
            }
        }
        // the guard booth inside the fence on the sidewalk: 3 across, 3 deep (d 1..3), the window to the road
        boolean booth = d >= 1 && d <= 3 && (a <= 2 || toll && a >= 13);
        if (booth)
        {
            int ia = a <= 2 ? a : 15 - a;                 // 0 outer .. 2 road side
            boolean ring = ia == 0 || ia == 2 || d == 1 || d == 3;
            for (int k = 1; k <= 3; k++)
            {
                Block b = null;
                int m = 0;
                if (ring)
                {
                    b = Blocks.stonebrick;
                    if (k == 2 && (ia == 2 || ia == 1))
                    {
                        b = Blocks.glass_pane;              // windows to the road and to both ends
                    }
                    if (ia == 1 && d == 1 && k <= 2)
                    {
                        b = deci("Door_Metal_3");             // the door at the inner end, walked through toward the city
                        m = k == 1 ? (nsSide ? 1 : 0) : 8;
                    }
                }
                if (b != null)
                {
                    w.setBlock(x, g + k, z, b, m, 2);
                }
            }
            w.setBlock(x, g + 4, z, Blocks.double_stone_slab, 0, 2);
        }
    }
}

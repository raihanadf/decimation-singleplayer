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
 * - where a street of the district meets the city edge (it ends there in a T or a dead end, StreetPlan.arms)
 *   the fence holds a closed gate (user, 14 Oktober 2026: "blocker gate ... only 5% chance to be opened and
 *   even its opened its broken style, no road on it"): the same closures as the inside gates (barbed wire
 *   with landmines, welded steel, a burnt bus, HESCO), 1 in 20 a broken gap; no road leads to it;
 * - MINES_FROM to MINES_TO outside: Decimation's landmines, about one in MINE_ONE_IN columns, none in
 *   front of a gate (they go off under a player only, without breaking blocks).
 */
final class WastelandFence
{
    static final int FENCE = 4, MINES_FROM = 6, MINES_TO = 16, MINE_ONE_IN = 28;

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
                // ties go to a cell the column lies straight out from (else a gate's first columns beside the
                // next wasteland cell were taken for a corner and kept the plain fence)
                if (d < best || d == best && (dx == 0 || dz == 0) && ox > 0 && oz > 0)
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
            int a = nsSide ? x & 15 : z & 15;
            if (a >= SIDEWALK - 1 && a <= 16 - SIDEWALK && landGate(w, seed, x, z, d, nsSide, a, y, g, bchx, bchz))
            {
                return;
            }
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
     * same level as the crossing (no river, no ramp): it owns that chunk's 8 columns nearest the line, the
     * road cells of the line itself and of the crossing's first two columns (the posts and the fence on the
     * sidewalks stay inCity's). Where the street in front cannot take it, a compact toll gate owns only the
     * line and the crossing's rows. Look and state by the crossing's hash (military/CityGate).
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
                if (g != null && !(g.compact() && d > 0))         // a compact toll owns no front rows
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
        // the street in front cannot take a full gate (a canal, a level change, no street): the compact toll
        // on the line and the crossing's two rows (user, 14 Oktober 2026: toll gates instead of plain ones); a
        // crossing on a canal carries its gate on the bridge deck (RiverPlan asks the street above the deck)
        boolean compact = !isStreetChunk(seed, fx, fz) || CityRivers.chunk(seed, fx, fz)
            || ground(seed, Math.floorDiv(fx, CELL), Math.floorDiv(fz, CELL)) != ground(seed, Math.floorDiv(cx, CELL), Math.floorDiv(cz, CELL));
        // look by the crossing's hash; 2 in 10 open (broken), else one of the four closures (user, 14 Oktober 2026)
        long h = hl(seed ^ 0x47415445L, cx * 2 + axis, cz);
        int look = compact ? -1 : (int) (h % net.decimation.worldgen.military.CityGate.LOOKS.length);
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
     * A column of an edge gate where the street chunk (bchx, bchz) meets the fence: a = position across the
     * street's width (SIDEWALK - 1 .. 16 - SIDEWALK, posts at the ends), d out from the wasteland, y the
     * column's top, g the street level (a closure's ground is levelled to it: a bus on the slope came out
     * jagged). State by the street's hash: 1 in 20 broken open, else a closure. Returns false when the
     * normal fence column applies (the wire gate's fence line, ground beyond what the gate uses).
     */
    private static boolean landGate(World w, long seed, int x, int z, int d, boolean nsSide, int a, int y, int g, int bchx, int bchz)
    {
        long h = hl(seed ^ 0x45444745L, bchx, bchz);
        int state = h % 20 == 0 ? CityGateState.OPEN : 1 + (int) ((h >>> 20) % 4);
        boolean post = a == SIDEWALK - 1 || a == 16 - SIDEWALK;
        Block metal = deci("BlockMetal_2"), wire = deci("BlockConcertinaWire");
        boolean solid = state == CityGateState.STEEL || state == CityGateState.HESCO || state == CityGateState.BUS;
        if (solid && (d == FENCE || state == CityGateState.BUS && d <= FENCE + 2 && d >= FENCE))
        {
            y = level(w, x, z, y, g);
            if (post && d != FENCE)
            {
                return false;                                 // beside the bus off the line: the fence's own column
            }
            if (post)
            {
                for (int k = 1; k <= 4; k++)
                {
                    w.setBlock(x, y + k, z, metal != null ? metal : Blocks.iron_bars, 0, 2);   // the posts at its ends
                }
                clearAbove(w, x, y + 5, z);
                return true;
            }
        }
        switch (state)
        {
            case CityGateState.OPEN:
                // the gap: bars bent down to stubs beside it, scorched ground, scraps of wire, a body
                if (d == FENCE && !post)
                {
                    clearAbove(w, x, y + 1, z);
                    if (a == SIDEWALK || a == 15 - SIDEWALK)
                    {
                        w.setBlock(x, y + 1, z, Blocks.iron_bars, 0, 2);
                    }
                    burn(w, seed, x, y, z);
                    return true;
                }
                if (d >= FENCE - 2 && d <= FENCE + 2 && !post)
                {
                    burn(w, seed, x, y, z);
                    if (d == FENCE + 1 && a == 6 && wire != null)
                    {
                        w.setBlock(x, y + 1, z, wire, 2, 2);
                    }
                    if (d == FENCE + 2 && a == 9)
                    {
                        Block bones = deci("BlockSkeletonGround");
                        if (bones != null)
                        {
                            w.setBlock(x, y + 1, z, bones, nsSide ? 2 : 3, 2);
                        }
                    }
                    return true;                              // no mines on the way through
                }
                return d >= MINES_FROM && d <= FENCE + 6 && !post;
            case CityGateState.STEEL:
                if (d == FENCE && !post)
                {
                    Block[] plates = {metal, deci("BlockMetal_3"), deci("BlockMetalWall")};
                    for (int k = 1; k <= 4; k++)
                    {
                        Block p = plates[(int) (hl(seed ^ k, x, z) % 3)];
                        if (k < 4 || (hl(seed, x, z) & 1) == 0)
                        {
                            w.setBlock(x, y + k, z, p != null ? p : Blocks.iron_block, 0, 2);
                        }
                    }
                    clearAbove(w, x, y + 5, z);
                    return true;
                }
                return false;
            case CityGateState.HESCO:
                if (d == FENCE && !post)
                {
                    Block hb = net.decimation.worldgen.military.BlockHesco.instance;
                    for (int k = 1; k <= 3; k++)
                    {
                        w.setBlock(x, y + k, z, hb != null ? hb : Blocks.sandstone, 0, 2);
                    }
                    if (wire != null)
                    {
                        w.setBlock(x, y + 4, z, wire, 2, 2);
                    }
                    clearAbove(w, x, y + 5, z);
                    return true;
                }
                return false;
            case CityGateState.BUS:
                if (d >= FENCE && d <= FENCE + 2 && !post)
                {
                    int dd = d - FENCE;
                    boolean side = dd != 1, end = a == SIDEWALK || a == 15 - SIDEWALK;
                    boolean wheel = side && (a == 4 || a == 5 || a == 10 || a == 11);
                    int body = hl(seed ^ 0x425553L, x, z) % 10 < 4 ? 15 : 4;
                    clearAbove(w, x, y + 1, z);
                    w.setBlock(x, y + 1, z, wheel ? Blocks.coal_block : Blocks.stained_hardened_clay, wheel ? 0 : body, 2);
                    if (side || end)
                    {
                        boolean window = !end && hl(seed ^ 0x57494EL, x, z) % 3 != 0;
                        if (!window)
                        {
                            w.setBlock(x, y + 2, z, Blocks.stained_hardened_clay, body, 2);
                        }
                        else if (hl(seed ^ 0x42524BL, x, z) % 2 == 0)
                        {
                            w.setBlock(x, y + 2, z, Blocks.glass_pane, 0, 2);
                        }
                    }
                    w.setBlock(x, y + 3, z, Blocks.stained_hardened_clay, hl(seed ^ 0x524F46L, x, z) % 2 == 0 ? 15 : 4, 2);
                    return true;
                }
                return false;
            default:
                // barbed wire with landmines: the fence runs on as it is, wire along both sides of it, mines
                // thick on the outside (about one cell in three)
                if ((d == FENCE + 1 || d == FENCE - 1) && !post && wire != null && w.isAirBlock(x, y + 1, z))
                {
                    w.setBlock(x, y + 1, z, wire, 2, 2);
                    return true;
                }
                if (d >= FENCE + 2 && d <= FENCE + 5 && !post && hl(seed ^ 0x4D494E45L, x, z) % 3 == 0
                    && w.getBlock(x, y, z).getMaterial().isSolid())
                {
                    if (!w.isAirBlock(x, y + 1, z))
                    {
                        w.setBlock(x, y + 1, z, Blocks.air, 0, 2);
                    }
                    w.spawnEntityInWorld(Deci.newLandmine(w, x + 0.5, y + 1, z + 0.5));
                    return true;
                }
                return false;
        }
    }

    /** The column's ground brought to y = g (filled with its own surface block's under layer, or cut down). */
    private static int level(World w, int x, int z, int y, int g)
    {
        if (y == g || g <= 0)
        {
            return y;
        }
        Block top = w.getBlock(x, y, z);
        int meta = w.getBlockMetadata(x, y, z);
        if (top instanceof net.minecraft.block.BlockFalling)
        {
            top = Blocks.stone;                               // never fill with blocks that fall
            meta = 0;
        }
        if (y < g)
        {
            for (int k = y; k < g; k++)
            {
                w.setBlock(x, k, z, top == Blocks.grass ? Blocks.dirt : top, top == Blocks.grass ? 0 : meta, 2);
            }
        }
        else
        {
            for (int k = y; k > g; k--)
            {
                w.setBlock(x, k, z, Blocks.air, 0, 2);
            }
        }
        w.setBlock(x, g, z, top, meta, 2);
        return g;
    }

    /** Scorched ground: about 4 in 10 columns turn black (stained clay; podzol reads orange in the pack). */
    private static void burn(World w, long seed, int x, int y, int z)
    {
        if (hl(seed ^ 0x4255524EL, x, z) % 10 < 4 && w.getBlock(x, y, z).getMaterial().isSolid())
        {
            w.setBlock(x, y, z, Blocks.stained_hardened_clay, 15, 2);
        }
    }

    /** The edge gates' states, the same as the inside gates' (military/CityGate). */
    private static final class CityGateState
    {
        static final int OPEN = net.decimation.worldgen.military.CityGate.OPEN, WIRE = net.decimation.worldgen.military.CityGate.WIRE,
            STEEL = net.decimation.worldgen.military.CityGate.STEEL, BUS = net.decimation.worldgen.military.CityGate.BUS,
            HESCO = net.decimation.worldgen.military.CityGate.HESCO;
    }
}

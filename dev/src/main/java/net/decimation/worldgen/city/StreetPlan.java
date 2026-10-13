package net.decimation.worldgen.city;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.decimation.worldgen.Plan;
import net.decimation.worldgen.Sectors;
import net.decimation.worldgen.Slices;
import net.decimation.worldgen.StructureGenerator;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import static net.decimation.worldgen.city.LcCity.*;

/** A street chunk: road at the ground, optional stairs part above it. */
final class StreetPlan implements net.decimation.worldgen.FixedBase
{
    private final String id;
    private final int minX, minZ, ground, turns;
    private final Block road;
    private final LcContent.Shape stairs;

    private final int kind;
    private final StreetProps props;
    private final long seed;
    private final LcCity city;
    private final String style;
    /** Street scene in the road (turned along it), or null. */
    private final LcContent.Shape scene;
    /** Front parts per side (west, north, east, south: turns 0..3), resolved on first use. */
    private LcContent.Shape[] fronts;
    /** The district's street part (slice 0 = street surface) and its turns, or null for our own street. */
    private final LcContent.Shape piece;
    private final int pieceTurns;
    /** Carried over a canal (RiverPlan): no fronts of the buildings beside it. */
    private final boolean onWater;

    /**
     * The street climbs one city level toward side turns (0 W, 1 N, 2 E, 3 S) as an asphalt ramp
     * (rampAt) instead of DeceasedCraft's stairs part (user, 13 Oktober 2026: the stairway "doesnt match
     * with asphalt normal road like we have"; chosen: an asphalt ramp).
     */
    private final boolean climb;

    StreetPlan(String id, int minX, int minZ, int ground, Block road, LcContent.Shape stairs, int turns,
               int kind, StreetProps props, long seed, LcCity city, String style, LcContent.Shape scene,
               LcContent.Shape piece, int pieceTurns, boolean onWater, boolean climb)
    {
        this.climb = climb;
        this.onWater = onWater;
        this.piece = piece;
        this.pieceTurns = pieceTurns;
        this.city = city;
        this.style = style;
        this.scene = scene;
        this.kind = kind;
        this.props = props;
        this.seed = seed;
        this.id = id;
        this.minX = minX;
        this.minZ = minZ;
        this.ground = ground;
        this.road = road;
        this.stairs = stairs;
        this.turns = turns;
    }

    /** 0 = north-south street, 1 = east-west, 2 = crossing. */
    int kind() { return kind; }
    public int fixedBaseY() { return ground - sink(); }

    private int sink = -1;

    /**
     * Blocks this chunk's base sinks below the street: CityGate.SINK when it is the street in front of a gate
     * closed by a tank ditch (the ditch is dug below the road), else 0.
     */
    private int sink()
    {
        if (sink < 0)
        {
            int s = 0;
            if (Wasteland.sectors() && !onWater && !climb && stairs == null)
            {
                for (net.decimation.worldgen.military.CityGatePlan g : new net.decimation.worldgen.military.CityGatePlan[] {
                    WastelandFence.gate(seed, minX + 15, minZ + 8), WastelandFence.gate(seed, minX + 8, minZ + 15)})
                {
                    // the gate's line is this chunk's east / south edge (a gate reaches 2 past its line)
                    if (g != null && g.sink() > 0 && (g.maxX() == minX + 16 + 2 || g.maxZ() == minZ + 16 + 2))
                    {
                        s = g.sink();
                    }
                }
            }
            sink = s;
        }
        return sink;
    }

    public String id() { return id; }
    public int minX() { return minX; }
    public int minZ() { return minZ; }
    public int maxX() { return minX + 15; }
    public int maxZ() { return minZ + 15; }
    public int height() { return sink() + (climb ? LcCity.LEVEL + 3 : stairs != null ? stairs.height + 1 : Math.max(13, piece != null ? piece.height : 0)); }
    public int clearAbove() { return 14; }
    public int maxSpread() { return 255; }
    public Block foundation() { return Blocks.stone; }
    public net.decimation.worldgen.ZoneKind zone() { return null; }
    public String describe() { return null; }

    /**
     * Fronts toward the buildings beside a straight street: the
     * neighbour chunk's building (looked up in its block's plans, so
     * the other side of the street works too) at this street's level
     * gets one of its district's fronts half the time. Resolved at
     * write time, when no block plan is being built (no recursion).
     */
    private LcContent.Shape[] fronts()
    {
        if (fronts != null)
        {
            return fronts;
        }
        fronts = new LcContent.Shape[4];
        if (kind == 2 || stairs != null || climb || onWater)
        {
            return fronts;
        }
        int chx = minX >> 4, chz = minZ >> 4;
        int[][] dirs = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}}; // west, north, east, south
        for (int d = 0; d < 4; d++)
        {
            if ((kind == 0) != (dirs[d][0] != 0))
            {
                continue; // only the sides along the street
            }
            int nx = chx + dirs[d][0], nz = chz + dirs[d][1];
            int ncx = Math.floorDiv(nx, CELL), ncz = Math.floorDiv(nz, CELL);
            if (!isCity(seed, ncx, ncz) || hl(seed ^ 0x46524F4EL, nx * 4 + d, nz) % 1000 >= FRONT_CHANCE * 1000)
            {
                continue;
            }
            int[] nb = block(seed, ncx, ncz);
            for (Plan p : city.plan(seed, nb[0], nb[1]))
            {
                if (p instanceof BuildingPlan && p.minX() <= nx * 16 && p.maxX() >= nx * 16 + 15
                    && p.minZ() <= nz * 16 && p.maxZ() >= nz * 16 + 15)
                {
                    BuildingPlan b = (BuildingPlan) p;
                    if (b.ground == ground)
                    {
                        fronts[d] = decor(seed, b.style, "fronts", nx, nz, 0x46524EL + d);
                    }
                    break;
                }
            }
        }
        return fronts;
    }

    /** Props added by the city dressing (StreetDress), built on first use. */
    private Dress dress;

    /**
     * The building beside this street chunk on side d (0 west, 1 north, 2 east, 3 south) at this
     * street's level, or null (the dressing hangs signs and graffiti on its facade).
     */
    BuildingPlan neighbourBuilding(int d)
    {
        int[][] dirs = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};
        int nx = (minX >> 4) + dirs[d][0], nz = (minZ >> 4) + dirs[d][1];
        int ncx = Math.floorDiv(nx, CELL), ncz = Math.floorDiv(nz, CELL);
        if (!isCity(seed, ncx, ncz))
        {
            return null;
        }
        int[] nb = block(seed, ncx, ncz);
        for (Plan q : city.plan(seed, nb[0], nb[1]))
        {
            if (q instanceof BuildingPlan && q.minX() <= nx * 16 && q.maxX() >= nx * 16 + 15
                && q.minZ() <= nz * 16 && q.maxZ() >= nz * 16 + 15)
            {
                BuildingPlan b = (BuildingPlan) q;
                return b.ground == ground ? b : null;
            }
        }
        return null;
    }

    int dressGround() { return ground; }

    public Block blockAt(int lx, int ly, int lz, int[] meta)
    {
        if (Wasteland.sectors() && !onWater && !climb && stairs == null)
        {
            // a styled gate (military/CityGate) owns its cells whole: its own surface, nothing of the street above
            net.decimation.worldgen.military.CityGatePlan gate = WastelandFence.gate(seed, minX + lx, minZ + lz);
            if (gate != null)
            {
                int cy = ly - sink() + net.decimation.worldgen.military.CityGate.SINK;   // canvas y: SINK = the surface
                if (cy >= gate.canvasHeightPublic())
                {
                    return null;
                }
                meta[0] = 0;
                return gate.blockAt(minX + lx - gate.minX(), cy, minZ + lz - gate.minZ(), meta);
            }
        }
        if (sink() > 0)
        {
            if (ly < sink())
            {
                return Blocks.stone;                          // the base sank for a tank ditch: keep the ground
            }
            ly -= sink();
        }
        if (Wasteland.sectors() && (lx == 0 || lz == 0))           // bridges too: no gap over a canal
        {
            // the fence and gates on a wasteland city's edge inside the city (WastelandFence.inCity)
            int x = minX + lx, z = minZ + lz;
            if (WastelandFence.line(seed, x, z) != 0)
            {
                // the fence stands on the last full block and replaces what the part has above it (half
                // slab sidewalks, hedges, railings): counted from their top it floated (user, 14 Oktober 2026)
                int floor = floor(lx, lz);
                Block f = WastelandFence.inCity(seed, x, z, ly, floor, meta);
                if (f != null)
                {
                    return f;
                }
                if (ly > floor && ly <= floor + 4)
                {
                    return null;                          // the gate stays open: no dressing in it
                }
            }
        }
        Dress d = dress();
        if (d != null)
        {
            Block b = d.at(lx, ly, lz, meta);
            if (b != null)
            {
                return b == Blocks.air ? null : b;
            }
        }
        return baseAt(lx, ly, lz, meta);
    }

    /** Highest plan y (0 .. 8) with a full opaque block of this street's own at (lx, lz). */
    private int floor(int lx, int lz)
    {
        int[] m = new int[1];
        for (int y = 8; y > 0; y--)
        {
            Block b = baseAt(lx, y, lz, m);
            if (b != null && b.isOpaqueCube() && b.renderAsNormalBlock()
                && b.getMaterial() != net.minecraft.block.material.Material.leaves)   // hedges: opaque on fast graphics
            {
                return y;
            }
        }
        return 0;
    }

    private synchronized Dress dress()
    {
        if (dress == null && stairs == null && !climb && Dress.ON)
        {
            dress = StreetDress.dress(this);
        }
        return dress;
    }

    // what StreetDress reads: this street's own blocks without the dressing
    int dressKind() { return kind; }
    long dressSeed() { return seed; }
    String dressStyle() { return style; }
    boolean dressOnWater() { return onWater; }
    boolean dressScene() { return scene != null; }
    boolean dressPiece() { return piece != null; }
    Block dressSidewalk() { return piece == null ? props.sidewalk : null; }

    Block baseAt(int lx, int ly, int lz, int[] meta)
    {
        meta[0] = 0;
        if (climb)
        {
            return rampAt(lx, ly, lz, meta);
        }
        // across = position across the street (0..15), along = along it
        int across = kind == 1 ? lz : lx, along = kind == 1 ? lx : lz;
        boolean sidewalk = kind == 2 ? (lx < SIDEWALK || lx > 15 - SIDEWALK) && (lz < SIDEWALK || lz > 15 - SIDEWALK)
            : across < SIDEWALK || across > 15 - SIDEWALK;
        if (piece != null)
        {
            return pieceAt(lx, ly, lz, across, along, meta);
        }
        if (ly == 0)
        {
            if (sidewalk)
            {
                return props.sidewalk;
            }
            // dashed centre line (2 on, 2 off) on straight streets
            if (kind != 2 && props.centreLine != null && across == 7 && (along & 3) < 2)
            {
                meta[0] = kind == 0 ? 4 : 2;
                return props.centreLine;
            }
            return road;
        }
        if (stairs != null)
        {
            return stairsAt(lx, ly, lz, meta);
        }
        if (scene != null)
        {
            Block b = partAt(scene, kind == 1 ? 1 : 0, lx, ly - 1, lz, meta);
            if (b != null)
            {
                return b;
            }
        }
        LcContent.Shape[] f = fronts();
        for (int d = 0; d < 4; d++)
        {
            if (f[d] != null)
            {
                Block b = partAt(f[d], d, lx, ly - 1, lz, meta);
                if (b != null)
                {
                    return b;
                }
            }
        }
        meta[0] = 0;
        if (ly != 1 || kind == 2)
        {
            return null;
        }
        Block b = furniture(across, along, sidewalk, meta);
        return scene != null && b != null && contains(props.cars, b) ? null : b;
    }

    /** Lamps on the road edge of each sidewalk, benches / bins / bags inside, a wreck now and then. */
    private Block furniture(int across, int along, boolean sidewalk, int[] meta)
    {
        int wx = minX + (kind == 1 ? along : across), wz = minZ + (kind == 1 ? across : along);
        long hv = hl(seed ^ 0x5354524545L, wx, wz); // all 64 bits random (low bits drive the chances)
        boolean west = across < SIDEWALK; // west / north sidewalk; the road lies toward +across
        // toward the road: across is x for kind 0 (east 2 / west 4), z for kind 1 (south 3 / north 5)
        int towardRoad = kind == 0 ? (west ? 2 : 4) : (west ? 3 : 5);
        if (sidewalk)
        {
            boolean roadEdge = across == SIDEWALK - 1 || across == 16 - SIDEWALK;
            if (roadEdge && along == (west ? 4 : 12) && props.lamp != null && (hv & 3) != 0)
            {
                meta[0] = towardRoad;
                return props.lamp;
            }
            if (across == (west ? 1 : 14))
            {
                if (along == (west ? 10 : 2) && props.bench != null && (hv & 0xFF) < 110)
                {
                    meta[0] = towardRoad;
                    return props.bench;
                }
                if (along == (west ? 1 : 9) && props.bin != null && (hv & 0xFF) < 102)
                {
                    meta[0] = 2 + (int) ((hv >>> 8) & 3);
                    return props.bin;
                }
            }
            if (props.trashBags.length > 0 && (hv & 0xFF) < 6)
            {
                meta[0] = 2 + (int) ((hv >>> 16) & 3);
                return props.trashBags[(int) ((hv >>> 8) % props.trashBags.length)];
            }
            return null;
        }
        // wrecks: lanes 5 and 10, one slot per chunk, 7% each
        if ((across == 5 || across == 10) && along == 7 && props.cars.length > 0 && (hv & 0xFF) < 18)
        {
            meta[0] = kind == 0 ? (across == 5 ? 5 : 3) : (across == 5 ? 4 : 2);
            return props.cars[(int) ((hv >>> 8) % props.cars.length)];
        }
        return null;
    }

    /**
     * A street of the district's own parts: the part (its lamps, benches,
     * sidewalks, paint) with scenes and fronts on top, and a wreck now
     * and then in its 6 wide road (lanes 6 / 9, 7% each).
     */
    private Block pieceAt(int lx, int ly, int lz, int across, int along, int[] meta)
    {
        if (ly == 0)
        {
            Block b = partAt(piece, pieceTurns, lx, 0, lz, meta);
            if (b == null)
            {
                meta[0] = 0;
                return road;
            }
            return b;
        }
        if (scene != null)
        {
            Block b = partAt(scene, kind == 1 ? 1 : 0, lx, ly - 1, lz, meta);
            if (b != null)
            {
                return b;
            }
        }
        LcContent.Shape[] f = fronts();
        for (int d = 0; d < 4; d++)
        {
            if (f[d] != null)
            {
                Block b = partAt(f[d], d, lx, ly - 1, lz, meta);
                if (b != null)
                {
                    return b;
                }
            }
        }
        Block b = partAt(piece, pieceTurns, lx, ly, lz, meta);
        if (b != null)
        {
            return b;
        }
        meta[0] = 0;
        // one wreck per chunk: a car is drawn 3.85 wide (BlockWreckage1..5, docs/prop_placement.md), the
        // part's road 6 (part across 5..10): only centred, anchored on lane 7 (heading north / west) or
        // lane 8 (south / east), does it stay off the raised sidewalks, hedges and the other lane's car
        if (ly != 1 || kind == 2 || scene != null || props.cars.length == 0 || (across != 7 && across != 8)
            || along != 7)
        {
            return null;
        }
        long hv = hl(seed ^ 0x5354524545L, minX + (kind == 1 ? lx : 0), minZ + (kind == 1 ? 0 : lz));
        if ((hv & 0xFF) >= 34 || (across == 7) != (((hv >>> 16) & 1) == 0))
        {
            return null;
        }
        int m = kind == 0 ? (across == 7 ? 5 : 3) : (across == 7 ? 4 : 2);
        Block car = props.cars[(int) ((hv >>> 8) % props.cars.length)];
        if (!carFits(lx, lz, car, m))
        {
            return null;                                  // a narrower road, a bay, a planter in this part
        }
        meta[0] = m;
        return car;
    }

    /** Is everything the car is drawn over (beyond 0.15 of a cell) empty in the part, the scene and the fronts? */
    private boolean carFits(int lx, int lz, Block car, int m)
    {
        float[] d = net.decimation.worldgen.military.PropBoxes.box(car, m);
        if (d == null)
        {
            return true;
        }
        int[] tmp = new int[1];
        LcContent.Shape[] f = fronts();
        for (int cx = (int) Math.floor(lx + d[0] + 0.15f); cx <= (int) Math.floor(lx + d[1] - 0.15f); cx++)
        {
            for (int cz = (int) Math.floor(lz + d[4] + 0.15f); cz <= (int) Math.floor(lz + d[5] - 0.15f); cz++)
            {
                if (cx < 0 || cz < 0 || cx > 15 || cz > 15)
                {
                    return false;
                }
                for (int ly = 1; ly <= 2; ly++)
                {
                    if ((cx != lx || cz != lz || ly != 1) && partAt(piece, pieceTurns, cx, ly, cz, tmp) != null)
                    {
                        return false;
                    }
                    for (int k = 0; k < 4; k++)
                    {
                        if (f[k] != null && partAt(f[k], k, cx, ly - 1, cz, tmp) != null)
                        {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean contains(Block[] l, Block b)
    {
        for (Block x : l)
        {
            if (x == b)
            {
                return true;
            }
        }
        return false;
    }

    private static Block roadSlab, roadSlabLine;

    /**
     * The ramp: the street rises LEVEL blocks across the chunk in half block steps (2 x LEVEL steps over
     * 16 cells, the first half a block over the lower street, the last flush with the higher one): road
     * blocks and Decimation's road slabs (the dashed centre line on both) between sidewalks of stone slab,
     * stone brick under it (a retaining wall where a lot lies beside it), iron railings on the outer edge
     * once the ramp stands 2 blocks above the ground.
     */
    private Block rampAt(int lx, int ly, int lz, int[] meta)
    {
        if (roadSlab == null)
        {
            roadSlab = Block.getBlockFromName("deci:BlockRoadSlab");
            roadSlabLine = Block.getBlockFromName("deci:BlockRoadSlab_CenterLine");
        }
        boolean alongX = turns == 0 || turns == 2;
        int t = turns == 0 ? 15 - lx : turns == 2 ? lx : turns == 1 ? 15 - lz : lz;   // 0 low side .. 15 high side
        int across = alongX ? lz : lx;
        int steps = ((t + 1) * 2 * LcCity.LEVEL + 15) / 16;                       // half blocks above the street
        int full = steps / 2;                                                     // full blocks above ly 0
        boolean slab = (steps & 1) == 1;
        boolean walk = across < LcCity.SIDEWALK || across > 15 - LcCity.SIDEWALK;
        boolean line = !walk && across == 7 && (t & 3) < 2;
        int lineMeta = alongX ? 2 : 4;
        if (ly < full)
        {
            return Blocks.stonebrick;
        }
        if (ly == full)
        {
            if (walk)
            {
                return Blocks.double_stone_slab;
            }
            if (line && props.centreLine != null)
            {
                meta[0] = lineMeta;
                return props.centreLine;
            }
            return road;
        }
        if (ly == full + 1 && slab)
        {
            if (walk)
            {
                return Blocks.stone_slab;
            }
            Block b = line && roadSlabLine != null && roadSlabLine != Blocks.air ? roadSlabLine : roadSlab;
            if (b != null && b != Blocks.air)
            {
                meta[0] = line ? lineMeta : 0;
                return b;
            }
            return Blocks.stone_slab;
        }
        // railings on the outer edge, on top of the surface, once it stands 2 above the street
        int above = full + (slab ? 2 : 1);
        if (ly == above && (across == 0 || across == 15) && steps >= 4)
        {
            return Blocks.iron_bars;
        }
        return null;
    }

    private Block stairsAt(int lx, int ly, int lz, int[] meta)
    {
        if (ly - 1 >= stairs.height)
        {
            return null;
        }
        // world local -> part position (inverse of a clockwise turn, as SchematicPlan)
        int x, z;
        switch (turns)
        {
            case 1:  x = lz; z = stairs.length - 1 - lx; break;
            case 2:  x = stairs.width - 1 - lx; z = stairs.length - 1 - lz; break;
            case 3:  x = stairs.width - 1 - lz; z = lx; break;
            default: x = lx; z = lz; break;
        }
        if (x < 0 || z < 0 || x >= stairs.width || z >= stairs.length)
        {
            return null;
        }
        int i = stairs.index(x, ly - 1, z);
        Block b = stairs.blocks[i];
        if (b == null)
        {
            return null;
        }
        meta[0] = net.decimation.worldgen.Rotation.rotateMeta(Block.getIdFromBlock(b), stairs.meta[i] & 15, turns);
        return b;
    }
}

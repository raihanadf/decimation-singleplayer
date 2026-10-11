package net.decimation.worldgen.city;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import static net.decimation.worldgen.city.LcCity.*;

/**
 * A city river chunk (CityRivers): a canal with its bed at BED, water up to
 * WATER (sea level, as the natural river outside), stone brick quay walls up
 * to the street level on every side that meets land, iron railings on the
 * walls. A street chunk on the river is the same street as on land (its
 * StreetPlan: the district's street part, paint, sidewalks, lamps) at the
 * street level, on a stone brick deck with piers, iron railings on the edges
 * that face water or a lot. In the wasteland district the middle of the
 * road has fallen into the canal (broken).
 */
final class RiverPlan implements net.decimation.worldgen.FixedBase
{
    /** Canal bed: above the railway tunnels (top at 54). */
    static final int BED = 56;
    /** Top water layer: vanilla's sea level, as the natural river. */
    static final int WATER = 62;

    private final String id;
    private final long seed;
    private final int cx, cz, ground;
    /** The street carried over the canal, or null for a plain canal chunk. */
    private final StreetPlan street;
    private final boolean broken;
    /** Land on the west, north, east, south side (a quay wall there); resolved on first use. */
    private boolean[] land;
    /** A street goes on beyond the west, north, east, south edge (no railing there). */
    private boolean[] open;

    RiverPlan(String id, long seed, int cx, int cz, int ground, StreetPlan street, boolean broken)
    {
        this.id = id;
        this.seed = seed;
        this.cx = cx;
        this.cz = cz;
        this.ground = ground;
        this.street = street;
        this.broken = broken;
    }

    public int fixedBaseY() { return BED; }
    public String id() { return id; }
    public int minX() { return cx << 4; }
    public int minZ() { return cz << 4; }
    public int maxX() { return (cx << 4) + 15; }
    public int maxZ() { return (cz << 4) + 15; }
    public int height() { return ground - BED + (street != null ? street.height() : 2); }
    public int clearAbove() { return 14; }
    public int maxSpread() { return 255; }
    public Block foundation() { return Blocks.stone; }
    public net.decimation.worldgen.ZoneKind zone() { return null; }
    public String describe() { return null; }

    private static final int[][] SIDES = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}}; // west, north, east, south

    private boolean[] land()
    {
        if (land == null)
        {
            boolean[] l = new boolean[4], o = new boolean[4];
            for (int i = 0; i < 4; i++)
            {
                int nx = cx + SIDES[i][0], nz = cz + SIDES[i][1];
                boolean city = isCity(seed, Math.floorDiv(nx, CELL), Math.floorDiv(nz, CELL));
                // a river chunk goes on as water; outside the city the natural river does
                l[i] = city ? !CityRivers.chunk(seed, nx, nz) : !CityRivers.water(seed, nx, nz);
                o[i] = city && isStreetChunk(seed, nx, nz) || Highways.at(seed, nx, nz);
            }
            open = o;
            land = l;
        }
        return land;
    }

    /** Stone brick piers from the bed to the deck: across the middle of a bridge, four for a crossing. */
    private boolean pier(int lx, int lz)
    {
        if (street == null)
        {
            return false;
        }
        if (street.kind() == 2)
        {
            return (lx == 2 || lx == 13) && (lz == 2 || lz == 13);
        }
        int along = street.kind() == 1 ? lx : lz, across = street.kind() == 1 ? lz : lx;
        return (along == 7 || along == 8) && (across == 2 || across == 3 || across == 12 || across == 13);
    }

    /** The fallen middle of a broken bridge's road. */
    private boolean hole(int lx, int lz)
    {
        if (!broken || street == null)
        {
            return false;
        }
        if (street.kind() == 2)
        {
            return lx >= 5 && lx <= 10 && lz >= 5 && lz <= 10;
        }
        int along = street.kind() == 1 ? lx : lz, across = street.kind() == 1 ? lz : lx;
        return across >= 5 && across <= 10 && along >= 4 && along <= 11;
    }

    public Block blockAt(int lx, int ly, int lz, int[] meta)
    {
        meta[0] = 0;
        int y = BED + ly;
        boolean[] l = land();
        boolean wall = lx == 0 && l[0] || lz == 0 && l[1] || lx == 15 && l[2] || lz == 15 && l[3];
        if (y < ground)
        {
            if (wall || pier(lx, lz) && ly > 0)
            {
                return Blocks.stonebrick;
            }
            if (ly == 0)
            {
                return Blocks.clay;
            }
            if (y <= WATER)
            {
                return Blocks.water;
            }
            return street != null && y == ground - 1 && !hole(lx, lz) ? Blocks.stonebrick : null;
        }
        int k = y - ground;
        if (street != null)
        {
            if (hole(lx, lz))
            {
                return null;
            }
            Block b = street.blockAt(lx, k, lz, meta);
            if (b != null)
            {
                return b;
            }
            meta[0] = 0;
            // railings on the edges that face water or a lot, open where a street goes on: on the road
            // (k 1), or on a raised sidewalk (a street part's slab at k 1, the railing at k 2)
            boolean edge = lx == 0 && !open[0] || lz == 0 && !open[1] || lx == 15 && !open[2] || lz == 15 && !open[3];
            boolean below = k == 2 && street.blockAt(lx, 1, lz, new int[1]) != null;
            return edge && (k == 1 || below) ? Blocks.iron_bars : null;
        }
        if (wall && k == 0)
        {
            return Blocks.stonebrick;
        }
        // railings on the quay walls
        return wall && k == 1 ? Blocks.iron_bars : null;
    }
}

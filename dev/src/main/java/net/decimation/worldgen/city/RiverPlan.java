package net.decimation.worldgen.city;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import static net.decimation.worldgen.city.LcCity.*;

/**
 * A city river chunk (CityRivers): a canal with its bed at BED, water up to
 * WATER (sea level, as the natural river outside), stone brick quay walls up
 * to the street level on every side that meets land, iron railings on the
 * walls. A street chunk on the river carries a bridge part of the district
 * (DeceasedCraft's bridge_1, the broken bridge_deadzone_1 in the wasteland
 * district; slice 0 is the road, laid at the street level) on two stone
 * brick piers; a crossing gets a plain road deck on four piers.
 */
final class RiverPlan implements net.decimation.worldgen.FixedBase
{
    /** Canal bed: above the railway tunnels (top at 54). */
    static final int BED = 56;
    /** Top water layer: vanilla's sea level, as the natural river. */
    static final int WATER = 62;

    private final String id;
    private final long seed;
    private final int cx, cz, ground, turns;
    private final LcContent.Shape bridge;
    private final boolean crossing;
    /** Land on the west, north, east, south side (a quay wall there); resolved on first use. */
    private boolean[] land;
    private static Block deck;

    RiverPlan(String id, long seed, int cx, int cz, int ground, LcContent.Shape bridge, int turns, boolean crossing)
    {
        this.id = id;
        this.seed = seed;
        this.cx = cx;
        this.cz = cz;
        this.ground = ground;
        this.bridge = bridge;
        this.turns = turns;
        this.crossing = crossing;
    }

    public int fixedBaseY() { return BED; }
    public String id() { return id; }
    public int minX() { return cx << 4; }
    public int minZ() { return cz << 4; }
    public int maxX() { return (cx << 4) + 15; }
    public int maxZ() { return (cz << 4) + 15; }
    public int height() { return ground - BED + (bridge != null ? bridge.height : 2); }
    public int clearAbove() { return 14; }
    public int maxSpread() { return 255; }
    public Block foundation() { return Blocks.stone; }
    public net.decimation.worldgen.ZoneKind zone() { return null; }
    public String describe() { return null; }

    private boolean[] land()
    {
        if (land == null)
        {
            int[][] d = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};
            boolean[] l = new boolean[4];
            for (int i = 0; i < 4; i++)
            {
                int nx = cx + d[i][0], nz = cz + d[i][1];
                boolean city = isCity(seed, Math.floorDiv(nx, CELL), Math.floorDiv(nz, CELL));
                // a river chunk goes on as water; outside the city the natural river does
                l[i] = city ? !CityRivers.chunk(seed, nx, nz) : !CityRivers.water(seed, nx, nz);
            }
            land = l;
        }
        return land;
    }

    private static Block deck()
    {
        if (deck == null)
        {
            Block b = Block.getBlockFromName("deci:BlockRoad");
            deck = b != null ? b : Blocks.stone;
        }
        return deck;
    }

    /** Stone brick piers from the bed to the deck: across the middle of a bridge, four for a crossing. */
    private boolean pier(int lx, int lz)
    {
        if (crossing)
        {
            return (lx == 2 || lx == 13) && (lz == 2 || lz == 13);
        }
        if (bridge == null)
        {
            return false;
        }
        int along = turns == 0 ? lx : lz, across = turns == 0 ? lz : lx;
        return (along == 7 || along == 8) && (across == 2 || across == 3 || across == 12 || across == 13);
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
            return crossing && y == ground - 1 ? Blocks.stonebrick : null;
        }
        int k = y - ground;
        if (bridge != null)
        {
            Block b = partAt(bridge, turns, lx, k, lz, meta);
            if (b != null)
            {
                return b;
            }
            meta[0] = 0;
        }
        if (crossing)
        {
            return k == 0 ? deck() : null;
        }
        if (wall && k == 0)
        {
            return Blocks.stonebrick;
        }
        // railings on the quay walls, not across a bridge's road ends
        if (wall && k == 1)
        {
            int along = turns == 0 ? lx : lz;
            boolean roadEnd = bridge != null && (along == 0 || along == 15);
            return roadEnd ? null : Blocks.iron_bars;
        }
        return null;
    }
}

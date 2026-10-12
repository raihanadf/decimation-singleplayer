package net.decimation.worldgen.city;

import java.util.List;

import cpw.mods.fml.common.FMLLog;
import net.decimation.worldgen.DecimationWorldGen;
import net.decimation.worldgen.Sectors;
import net.decimation.worldgen.StructureData;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/**
 * Underground railway (Lost Cities railway parts from the content pack,
 * DeceasedCraft's versions; docs/city_engine.md "Railway"). Tracks run
 * RAIL = 3 city levels below the level 0 street (Lost Cities
 * RAILWAY_LEVEL_OFFSET -3) on the highway lines (Highways.line): in a city
 * region along its whole highway row and column (always street chunks, so
 * never under a building), between city regions wherever a highway would
 * span the gap (Highways.spans), crossings where both lines meet, a buffer
 * end where a line leaves a city with nothing beyond. Each city region has
 * one underground station on its row: three chunks of one street segment
 * (the middle one with stairs up to the street, Lost Cities
 * station_underground_stairs + station_staircase per level +
 * station_staircase_surface on the street). Pure function of the seed.
 */
public final class Rails
{
    /** Track part floor (slice 0): 3 levels below the level 0 street. */
    static final int RAIL = LcCity.BASE - 3 * LcCity.LEVEL;
    public static final int NONE = 0, X = 1, Z = 2, CROSS = 3, END_W = 4, END_E = 5, END_N = 6, END_S = 7, STATION = 8,
        PLATFORM = 9;
    static final long SALT_STATION = 0x5354414EL;

    private Rails()
    {
    }

    /** Chunk on an east west line: a city region's highway row, or a row a highway would carry between cities. */
    public static boolean xRail(long seed, int cx, int cz)
    {
        int rz = Math.floorDiv(cz, Sectors.REGION), rx = Math.floorDiv(cx, Sectors.REGION);
        return cz == Highways.line(seed, rz, Highways.SALT_X)
            && (Highways.cityRegion(seed, rx, rz) || Highways.spans(seed, rx, rz, true));
    }

    /** Chunk on a north south line (a region column's highway column). */
    public static boolean zRail(long seed, int cx, int cz)
    {
        int rz = Math.floorDiv(cz, Sectors.REGION), rx = Math.floorDiv(cx, Sectors.REGION);
        return cx == Highways.line(seed, rx, Highways.SALT_Z)
            && (Highways.cityRegion(seed, rx, rz) || Highways.spans(seed, rz, rx, false));
    }

    /**
     * The station's middle chunk x of a city region's row (its street
     * segment 0..2 by hash: chunks 4k+1..4k+3, never a crossing, never a
     * line end, never a stairs chunk between city levels, never on the
     * river); Integer.MIN_VALUE when the region is no city or all three
     * segments touch the river.
     */
    public static int stationX(long seed, int rx, int rz)
    {
        if (!Highways.cityRegion(seed, rx, rz))
        {
            return Integer.MIN_VALUE;
        }
        // the hashed segment, else the next one clear of the river (its street chunks are bridges)
        int cz = Highways.line(seed, rz, Highways.SALT_X), first = (int) (LcCity.hl(seed ^ SALT_STATION, rx, rz) % 3);
        for (int i = 0; i < 3; i++)
        {
            int sx = rx * Sectors.REGION + 4 * ((first + i) % 3) + 2;
            if (!CityRivers.chunk(seed, sx - 1, cz) && !CityRivers.chunk(seed, sx, cz) && !CityRivers.chunk(seed, sx + 1, cz))
            {
                return sx;
            }
        }
        return Integer.MIN_VALUE;
    }

    /** A city region's station: {track x, y, z (middle of the station chunk), street ground}, or null. */
    public static int[] station(long seed, int rx, int rz)
    {
        int sx = stationX(seed, rx, rz);
        if (sx == Integer.MIN_VALUE)
        {
            return null;
        }
        int cz = Highways.line(seed, rz, Highways.SALT_X);
        return new int[] {(sx << 4) + 8, RAIL + 1, (cz << 4) + 8,
            LcCity.ground(seed, Math.floorDiv(sx, LcCity.CELL), Math.floorDiv(cz, LcCity.CELL))};
    }

    /**
     * True when the block box touches a railway chunk (worldgen that must
     * stay off the tunnels: springs and lakes, TerrainEvents). Off without
     * rail content.
     */
    public static boolean near(long seed, int x0, int z0, int x1, int z1)
    {
        if (!LcCity.enabled() || LcContent.rails("rails_horizontal").isEmpty())
        {
            return false;
        }
        for (int cx = x0 >> 4; cx <= x1 >> 4; cx++)
        {
            for (int cz = z0 >> 4; cz <= z1 >> 4; cz++)
            {
                if (kind(seed, cx, cz) != NONE)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /** What the railway puts into a chunk. */
    public static int kind(long seed, int cx, int cz)
    {
        boolean x = xRail(seed, cx, cz), z = zRail(seed, cx, cz);
        if (x && z)
        {
            boolean xArms = xRail(seed, cx - 1, cz) || xRail(seed, cx + 1, cz);
            boolean zArms = zRail(seed, cx, cz - 1) || zRail(seed, cx, cz + 1);
            if (xArms && zArms)
            {
                return CROSS;
            }
            x = xArms;
            z = !xArms;
        }
        if (x)
        {
            int sx = stationX(seed, Math.floorDiv(cx, Sectors.REGION), Math.floorDiv(cz, Sectors.REGION));
            if (cx == sx)
            {
                return STATION;
            }
            if (cx == sx - 1 || cx == sx + 1)
            {
                return PLATFORM;
            }
            return !xRail(seed, cx - 1, cz) ? END_W : !xRail(seed, cx + 1, cz) ? END_E : X;
        }
        if (z)
        {
            return !zRail(seed, cx, cz - 1) ? END_N : !zRail(seed, cx, cz + 1) ? END_S : Z;
        }
        return NONE;
    }

    /** Writes the railway chunks this population window touches (only their part inside it). */
    static void populate(World world, int[] w)
    {
        if (LcContent.rails("rails_horizontal").isEmpty())
        {
            return;
        }
        long seed = world.getSeed();
        for (int cx = w[0] >> 4; cx <= w[2] >> 4; cx++)
        {
            for (int cz = w[1] >> 4; cz <= w[3] >> 4; cz++)
            {
                int k = kind(seed, cx, cz);
                if (k != NONE)
                {
                    place(world, seed, cx, cz, k, w);
                }
            }
        }
    }

    private static void place(World world, long seed, int cx, int cz, int k, int[] w)
    {
        boolean wet = (k == X || k == Z) && wet(world, cx, cz);
        String part;
        int turns = 0;
        switch (k)
        {
            case X: part = wet ? "rails_horizontal_water" : "rails_horizontal"; break;
            case Z: part = wet ? "rails_vertical_water" : "rails_vertical"; break;
            case CROSS: part = "rails_3split"; break;
            case STATION: part = "station_underground_stairs"; break;
            case PLATFORM: part = "station_underground"; break;
            default: // the end part is open to the west unturned (Lost Cities RAILS_END_HERE WEST)
                part = "rails_horizontal_end";
                turns = k == END_W ? 2 : k == END_N ? 3 : k == END_S ? 1 : 0;
                break;
        }
        if (!write(world, part, turns, cx, cz, RAIL, w))
        {
            return;
        }
        if (k == CROSS)
        {
            seal(world, seed, cx, cz, w);
        }
        if (k == STATION)
        {
            if (w[0] <= cx << 4 && cx << 4 <= w[2] && w[1] <= cz << 4 && cz << 4 <= w[3])
            {
                FMLLog.info("[%s] rail station at %d,%d,%d", DecimationWorldGen.MODID, (cx << 4) + 8, RAIL + 1,
                            (cz << 4) + 8);
            }
            int g = LcCity.ground(seed, Math.floorDiv(cx, LcCity.CELL), Math.floorDiv(cz, LcCity.CELL));
            // the staircase up to just under the street (its top layer would replace the street's), no
            // surface plaza: the street stays whole and the stairs come out in its south sidewalk
            for (int y = RAIL + LcCity.LEVEL; y < g; y += LcCity.LEVEL)
            {
                write(world, "station_staircase", 0, cx, cz, y, w, g);
            }
            entrance(world, cx, cz, g, w);
        }
        if (k == X || k == Z)
        {
            boost(world, cx, cz, k == X, w);
        }
        if (k == STATION || k == PLATFORM)
        {
            light(world, cx, cz, w);
        }
    }

    /**
     * Ceiling lights over the platforms and the track of station chunks:
     * the parts' own glowstone sits under slabs, which let no light through
     * in 1.7.10, and the station was black (rail test, 11 Oktober 2026).
     * Glowstone into the first opaque block above, between the beams.
     */
    private static void light(World world, int cx, int cz, int[] w)
    {
        for (int lx = 2; lx < 16; lx += 4)
        {
            for (int lz : new int[] {3, 8, 12})
            {
                int x = (cx << 4) + lx, z = (cz << 4) + lz;
                if (x < w[0] || x > w[2] || z < w[1] || z > w[3])
                {
                    continue;
                }
                for (int y = RAIL + 3; y <= RAIL + 12; y++)
                {
                    Block b = world.getBlock(x, y, z);
                    if (b == Blocks.air)
                    {
                        continue;
                    }
                    if (b.isOpaqueCube())
                    {
                        world.setBlock(x, y, z, Blocks.glowstone, 0, 2);
                    }
                    break;
                }
            }
        }
    }

    /**
     * One part at chunk (cx, cz) with its floor at y0, turned clockwise:
     * kept world where the part says so (bedrock marker), air where it is
     * empty. Rails keep the part's metadata (placing a rail reshapes it
     * toward its neighbours).
     */
    private static boolean write(World world, String kind, int turns, int cx, int cz, int y0, int[] w)
    {
        return write(world, kind, turns, cx, cz, y0, w, Integer.MAX_VALUE);
    }

    /** As above, writing only below maxY. */
    private static boolean write(World world, String kind, int turns, int cx, int cz, int y0, int[] w, int maxY)
    {
        List<String> files = LcContent.rails(kind);
        LcContent.Shape s = files.isEmpty() ? null : LcContent.shape(files.get(0));
        if (s == null)
        {
            return false;
        }
        int x0 = Math.max(w[0], cx << 4), x1 = Math.min(w[2], (cx << 4) + 15);
        int z0 = Math.max(w[1], cz << 4), z1 = Math.min(w[3], (cz << 4) + 15);
        int[] meta = new int[1];
        for (int x = x0; x <= x1; x++)
        {
            for (int z = z0; z <= z1; z++)
            {
                for (int y = 0; y < s.height && y0 + y < maxY; y++)
                {
                    if (keep(s, turns, x - (cx << 4), y, z - (cz << 4)))
                    {
                        continue;
                    }
                    Block b = LcCity.partAt(s, turns, x - (cx << 4), y, z - (cz << 4), meta);
                    world.setBlock(x, y0 + y, z, b != null ? b : Blocks.air, b != null ? meta[0] : 0, 2);
                    if (b instanceof net.minecraft.block.BlockRailBase)
                    {
                        world.setBlockMetadataWithNotify(x, y0 + y, z, meta[0], 2);
                    }
                }
            }
        }
        return true;
    }

    /**
     * The station's way up (user, 13 Oktober 2026: "make sure the placement of the subway station
     * properly on side building instead on road"; DeceasedCraft's surface part paved the whole street
     * chunk into a plaza and cut the road): the staircase's last flight comes up under the south
     * sidewalk (cells x 10..11, z 13..14 of the chunk, the top step at the street level), so the
     * sidewalk opens there as a metro entrance: the top step, a 6 x 2 well over the flight below it, the
     * street's props cleared above, iron railings round the well on the road side, the far end and the
     * building side, open at the top step toward the sidewalk east of it.
     */
    private static void entrance(World world, int cx, int cz, int g, int[] w)
    {
        int bx = cx << 4, bz = cz << 4;
        for (int lx = 3; lx <= 12; lx++)
        {
            for (int lz = 12; lz <= 15; lz++)
            {
                int x = bx + lx, z = bz + lz;
                if (x < w[0] || x > w[2] || z < w[1] || z > w[3])
                {
                    continue;
                }
                boolean well = lz >= 13 && lz <= 14 && lx >= 4 && lx <= 9, step = lz >= 13 && lz <= 14 && lx >= 10 && lx <= 11;
                boolean rail = lx <= 11 && (lz == 12 || lz == 15) && lx >= 3 || lx == 3 && (lz == 13 || lz == 14);
                if (well || step)
                {
                    world.setBlock(x, g, z, well ? Blocks.air : Blocks.stone_slab, 0, 2);
                    for (int y = g + 1; y <= g + 3; y++)
                    {
                        world.setBlock(x, y, z, Blocks.air, 0, 2);
                    }
                }
                else if (rail && lx <= 10)
                {
                    world.setBlock(x, g + 1, z, Blocks.iron_bars, 0, 2);
                }
            }
        }
    }

    /** True where the part keeps the world (outside its bounds too). */
    private static boolean keep(LcContent.Shape s, int turns, int lx, int y, int lz)
    {
        int x, z;
        switch (turns)
        {
            case 1:  x = lz; z = s.length - 1 - lx; break;
            case 2:  x = s.width - 1 - lx; z = s.length - 1 - lz; break;
            case 3:  x = s.width - 1 - lz; z = lx; break;
            default: x = lx; z = lz; break;
        }
        return x < 0 || z < 0 || x >= s.width || z >= s.length || s.skip[s.index(x, y, z)];
    }

    /**
     * A powered rail in the middle of every straight track chunk, on a
     * redstone block, so a minecart keeps its speed from city to city
     * (about one every 34 blocks is enough on the flat).
     */
    private static void boost(World world, int cx, int cz, boolean alongX, int[] w)
    {
        int x = (cx << 4) + 8, z = (cz << 4) + 8;
        for (int i = 0; i < 16; i++)
        {
            int bx = alongX ? x : (cx << 4) + i, bz = alongX ? (cz << 4) + i : z;
            if (bx < w[0] || bx > w[2] || bz < w[1] || bz > w[3] || world.getBlock(bx, RAIL + 1, bz) != Blocks.rail)
            {
                continue;
            }
            int meta = (alongX ? 1 : 0) | 8;
            world.setBlock(bx, RAIL, bz, Blocks.redstone_block, 0, 2);
            world.setBlock(bx, RAIL + 1, bz, Blocks.golden_rail, meta, 2);
            world.setBlockMetadataWithNotify(bx, RAIL + 1, bz, meta, 2);
        }
    }

    /**
     * Track chunk under water (the tunnel's own height, as Lost Cities
     * samples it): the glass tube variant. Decided once per chunk (the first
     * window to reach it stores it as "rw_X_Z"), since the tunnel replaces
     * the water it looked at.
     */
    private static boolean wet(World world, int cx, int cz)
    {
        String id = "rw_" + cx + "_" + cz;
        StructureData data = StructureData.get(world);
        Integer v = data.baseY(id);
        if (v == null)
        {
            int bx = cx << 4, bz = cz << 4;
            int[][] at = {{3, 2, 3}, {12, 2, 3}, {3, 2, 12}, {12, 2, 12}, {3, 4, 7}, {12, 4, 8}};
            boolean all = true;
            for (int[] a : at)
            {
                all &= world.getBlock(bx + a[0], RAIL + a[1], bz + a[2]).getMaterial().isLiquid();
            }
            v = all ? 1 : 0;
            data.setBaseY(id, v);
        }
        return v == 1;
    }

    /**
     * A crossing whose line stops there (a city row at the region's edge
     * with no line beyond): a wall across that arm's opening.
     */
    private static void seal(World world, long seed, int cx, int cz, int[] w)
    {
        boolean[] open = {xRail(seed, cx - 1, cz), xRail(seed, cx + 1, cz), zRail(seed, cx, cz - 1),
            zRail(seed, cx, cz + 1)};
        for (int d = 0; d < 4; d++)
        {
            if (open[d])
            {
                continue;
            }
            for (int i = 0; i < 16; i++)
            {
                int x = d == 0 ? cx << 4 : d == 1 ? (cx << 4) + 15 : (cx << 4) + i;
                int z = d == 2 ? cz << 4 : d == 3 ? (cz << 4) + 15 : (cz << 4) + i;
                if (x < w[0] || x > w[2] || z < w[1] || z > w[3])
                {
                    continue;
                }
                for (int y = RAIL; y < RAIL + 9; y++)
                {
                    if (world.isAirBlock(x, y, z) || world.getBlock(x, y, z) instanceof net.minecraft.block.BlockRailBase)
                    {
                        world.setBlock(x, y, z, Blocks.stained_hardened_clay, 9, 2);
                    }
                }
            }
        }
    }
}

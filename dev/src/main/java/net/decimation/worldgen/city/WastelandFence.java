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

package net.decimation.worldgen.city;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;

/**
 * What a street chunk is made of, read from its own blocks (district street part, our own street,
 * scene, fronts) so the dressing works for every part without knowing it (docs/city_engine.md
 * "City dressing"). Per column: ROAD (stand at 1 on the road surface), WALK (a sidewalk, stand
 * on its surface), BUSY (the part already put something there) or OTHER (planters, stairs,
 * water, a front). A WALK column next to a ROAD column is a curb cell; each WALK column knows the
 * direction of the nearest road (the way a bench or a sign beside the road looks).
 */
final class StreetLayout
{
    static final int ROAD = 1, WALK = 2, BUSY = 3, OTHER = 0;
    /** Directions as prop fronts: 0 E (+x), 1 S (+z), 2 W (-x), 3 N (-z). */
    static final int[][] DIR = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};

    final int[][] type = new int[16][16];
    /** Layer a prop stands at on this column (the cell above the surface). */
    final int[][] stand = new int[16][16];
    /** Direction (0..3) of the nearest road from a walk column, -1 if none within 8. */
    final int[][] toRoad = new int[16][16];
    /** Distance to that road (1 = curb). */
    final int[][] roadDist = new int[16][16];

    StreetLayout(Dress.Base base, Block legacySidewalk)
    {
        for (int x = 0; x < 16; x++)
        {
            for (int z = 0; z < 16; z++)
            {
                classify(base, legacySidewalk, x, z);
            }
        }
        for (int x = 0; x < 16; x++)
        {
            for (int z = 0; z < 16; z++)
            {
                toRoad[x][z] = -1;
                if (type[x][z] != WALK)
                {
                    continue;
                }
                int best = 99;
                for (int d = 0; d < 4; d++)
                {
                    for (int k = 1; k <= 8; k++)
                    {
                        int nx = x + DIR[d][0] * k, nz = z + DIR[d][1] * k;
                        if (nx < 0 || nz < 0 || nx > 15 || nz > 15)
                        {
                            break;
                        }
                        if (type[nx][nz] == ROAD)
                        {
                            if (k < best)
                            {
                                best = k;
                                toRoad[x][z] = d;
                            }
                            break;
                        }
                    }
                }
                roadDist[x][z] = best;
            }
        }
    }

    private void classify(Dress.Base base, Block legacySidewalk, int x, int z)
    {
        Block b0 = base.block(x, 0, z), b1 = base.block(x, 1, z), b2 = base.block(x, 2, z), b3 = base.block(x, 3, z);
        if (b1 != null)
        {
            boolean walkable = b1.isOpaqueCube() || b1 instanceof BlockSlab;
            if (walkable && b1 instanceof BlockSlab && (base.meta(x, 1, z) & 8) != 0)
            {
                walkable = true;                      // an upper slab reads as a full step
            }
            if (!walkable || b2 != null && !(b2 instanceof BlockSlab))
            {
                type[x][z] = b2 == null && !walkable ? OTHER : BUSY;
                return;
            }
            if (b2 != null)
            {
                type[x][z] = OTHER;                   // a step up again: stairs or a planter
                return;
            }
            type[x][z] = b3 == null ? WALK : BUSY;
            stand[x][z] = 2;
            return;
        }
        if (b0 == null)
        {
            type[x][z] = OTHER;                       // a hole (a broken bridge)
            return;
        }
        if (b2 != null)
        {
            type[x][z] = BUSY;
            return;
        }
        stand[x][z] = 1;
        type[x][z] = b0 == legacySidewalk ? WALK : ROAD;
    }

    boolean curb(int x, int z)
    {
        return type[x][z] == WALK && roadDist[x][z] == 1;
    }
}

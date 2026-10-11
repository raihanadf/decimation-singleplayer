package net.decimation.worldgen.city;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockBush;

/**
 * The inside of a converted building, read from its blocks (docs/city_engine.md "City dressing"):
 * cells a player stands in, which of them lie against a wall (and which way), doors, and how
 * high the room is above each cell. Read once per schematic (the dressing then picks per building
 * with its own seed). A floor covering (carpet, a pressure plate, snow, a plant) counts as the
 * stand cell itself: a prop replaces it, so it stands on the floor, not on the carpet in the air.
 */
final class BuildingLayout
{
    static final int[][] DIR = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};   // 0 E, 1 S, 2 W, 3 N

    /** A cell a player can stand in. */
    static final class Spot
    {
        final int x, y, z;
        /** Bits 0..3: a wall (full block at y and y + 1) on side E, S, W, N. */
        final int walls;
        /** Free cells above the floor (2 = a person fits, up to 6). */
        final int room;
        /** In front of / behind a door, or a door's own cell. */
        final boolean doorway;
        /** Open sky above (a roof, a balcony, a yard). */
        final boolean outside;

        Spot(int x, int y, int z, int walls, int room, boolean doorway, boolean outside)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.walls = walls;
            this.room = room;
            this.doorway = doorway;
            this.outside = outside;
        }

        int wallCount()
        {
            return Integer.bitCount(walls);
        }
    }

    final List<Spot> spots = new ArrayList<Spot>();
    /** Door lower halves: {x, y, z, meta}. */
    final List<int[]> doors = new ArrayList<int[]>();
    private final LcContent.Shape s;

    BuildingLayout(LcContent.Shape s)
    {
        this.s = s;
        for (int y = 1; y < s.height - 1; y++)
        {
            for (int z = 0; z < s.length; z++)
            {
                for (int x = 0; x < s.width; x++)
                {
                    Block b = at(x, y, z);
                    if (b != null && PropGeom.isDoor(b) && (meta(x, y, z) & 8) == 0)
                    {
                        doors.add(new int[] {x, y, z, meta(x, y, z)});
                    }
                    if (!open(x, y, z) || !open(x, y + 1, z) || !floor(x, y - 1, z))
                    {
                        continue;
                    }
                    int walls = 0;
                    for (int d = 0; d < 4; d++)
                    {
                        if (wall(x + DIR[d][0], y, z + DIR[d][1]) && wall(x + DIR[d][0], y + 1, z + DIR[d][1]))
                        {
                            walls |= 1 << d;
                        }
                    }
                    int room = 2;
                    while (room < 6 && y + room < s.height && open(x, y + room, z))
                    {
                        room++;
                    }
                    boolean sky = true;
                    for (int k = y + room; k < s.height && sky; k++)
                    {
                        sky = at(x, k, z) == null;
                    }
                    spots.add(new Spot(x, y, z, walls, room, false, sky));
                }
            }
        }
        // doorways: the spots in front of / behind each door
        for (int i = 0; i < spots.size(); i++)
        {
            Spot p = spots.get(i);
            for (int[] d : doors)
            {
                boolean alongX = (d[3] & 1) == 0;   // vanilla door meta: even = walked through along x
                int dx = p.x - d[0], dz = p.z - d[2];
                if (p.y == d[1] && (alongX ? Math.abs(dx) <= 1 && dz == 0 : Math.abs(dz) <= 1 && dx == 0))
                {
                    spots.set(i, new Spot(p.x, p.y, p.z, p.walls, p.room, true, p.outside));
                    break;
                }
            }
        }
    }

    Block at(int x, int y, int z)
    {
        if (x < 0 || y < 0 || z < 0 || x >= s.width || y >= s.height || z >= s.length)
        {
            return null;
        }
        int i = s.index(x, y, z);
        return s.skip[i] ? null : s.blocks[i];
    }

    int meta(int x, int y, int z)
    {
        return s.meta[s.index(x, y, z)] & 15;
    }

    /** Air, or a floor covering a prop may replace. */
    boolean open(int x, int y, int z)
    {
        Block b = at(x, y, z);
        return b == null || cover(b);
    }

    static boolean cover(Block b)
    {
        return b instanceof BlockCarpet || b instanceof BlockPressurePlate || b instanceof BlockSnow
            || b instanceof BlockBush;
    }

    private boolean floor(int x, int y, int z)
    {
        Block b = at(x, y, z);
        return b != null && !cover(b) && net.decimation.worldgen.military.PropBoxes.box(b, meta(x, y, z)) == null
            && (b.isOpaqueCube() || b.getMaterial().isSolid());
    }

    boolean wall(int x, int y, int z)
    {
        Block b = at(x, y, z);
        return b != null && b.isOpaqueCube();
    }
}

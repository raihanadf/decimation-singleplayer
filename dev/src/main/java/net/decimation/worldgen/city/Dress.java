package net.decimation.worldgen.city;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.decimation.worldgen.military.PropBoxes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockWall;
import net.minecraft.block.BlockWeb;
import net.minecraft.init.Blocks;

/**
 * Props added on top of a plan's own blocks (city dressing, docs/city_engine.md "City dressing"):
 * a sparse overlay in the plan's local coordinates. Every prop goes in through {@link #place},
 * which accepts it only when it fits as tools/props/propclash.py would judge it: its cell empty,
 * its drawn box (PropBoxes, at the metadata given) clear of every block and of every prop already
 * there (the plan's and the overlay's), nothing drawn outside the plan (no prop reaches into a
 * neighbour chunk written by another plan), the cells above a tall prop free, standing on
 * something, never in a door's walkway. A prop on a bottom half slab gets the slab under it
 * made whole (a raised sidewalk is half slabs; a prop on one would float half a block).
 */
final class Dress
{
    /** The plan's own blocks in its local coordinates (null = air or outside). */
    interface Base
    {
        Block block(int x, int y, int z);

        int meta(int x, int y, int z);
    }

    /** City dressing switch: off until checked (-Ddeciworldgen.dress=true turns it on). WORK IN PROGRESS. */
    static final boolean ON = Boolean.getBoolean("deciworldgen.dress");

    private static final int[][] SIDES = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};

    final int w, h, l;
    private final Base base;
    private final Map<Integer, Block> blocks = new HashMap<Integer, Block>();
    private final Map<Integer, Integer> metas = new HashMap<Integer, Integer>();
    private final List<float[]> boxes = new ArrayList<float[]>();
    /** Props placed, refused (did not fit) per prop name: the dev test and the log read it. */
    final Map<String, int[]> tally = new HashMap<String, int[]>();

    Dress(Base base, int w, int h, int l)
    {
        this.base = base;
        this.w = w;
        this.h = h;
        this.l = l;
    }

    private int key(int x, int y, int z)
    {
        return (y * l + z) * w + x;
    }

    private boolean inside(int x, int y, int z)
    {
        return x >= 0 && y >= 0 && z >= 0 && x < w && y < h && z < l;
    }

    /** The overlay's block at a cell, or null (then the plan's own block stands). */
    Block at(int x, int y, int z, int[] meta)
    {
        if (blocks.isEmpty() || !inside(x, y, z))
        {
            return null;
        }
        int k = key(x, y, z);
        Block b = blocks.get(k);
        if (b != null)
        {
            meta[0] = metas.get(k);
        }
        return b;
    }

    boolean isEmpty()
    {
        return blocks.isEmpty();
    }

    /** What stands at a cell now: the overlay over the plan. */
    Block block(int x, int y, int z)
    {
        if (!inside(x, y, z))
        {
            return null;
        }
        Block b = blocks.get(key(x, y, z));
        return b != null ? (b == Blocks.air ? null : b) : base.block(x, y, z);
    }

    int meta(int x, int y, int z)
    {
        Integer m = metas.get(key(x, y, z));
        return m != null ? m : base.meta(x, y, z);
    }

    /** Puts a block without checks (support pads, plain blocks the rules chose). */
    void put(Block b, int m, int x, int y, int z)
    {
        if (inside(x, y, z))
        {
            blocks.put(key(x, y, z), b == null ? Blocks.air : b);
            metas.put(key(x, y, z), m);
        }
    }

    /** A full block, as a wall or a floor. */
    boolean solid(int x, int y, int z)
    {
        Block b = block(x, y, z);
        return b != null && (b.isOpaqueCube() || PropGeom.fullGlass(b)
                             || b instanceof BlockSlab && (meta(x, y, z) & 8) != 0);
    }

    /** Something a standing prop rests on (as propclash.py counts it). */
    boolean supports(int x, int y, int z)
    {
        Block b = block(x, y, z);
        if (b == null)
        {
            return false;
        }
        float[] box = PropBoxes.box(b, meta(x, y, z));
        if (box != null)
        {
            return box[3] >= 0.7f;
        }
        return b.getMaterial().isSolid() && !(b instanceof BlockWeb) || b instanceof BlockSlab || b instanceof BlockStairs
            || b instanceof BlockFence || b instanceof BlockWall || b instanceof BlockChest;
    }

    /** Is (x, y, z) in front of or behind a door (its walkway)? */
    boolean inDoorway(int x, int y, int z)
    {
        for (int[] f : SIDES)
        {
            for (int dy = 0; dy >= -1; dy--)
            {
                Block k = block(x + f[0], y + dy, z + f[1]);
                if (k == null || !PropGeom.isDoor(k))
                {
                    continue;
                }
                int m = meta(x + f[0], y + dy, z + f[1]);
                if ((m & 8) != 0)
                {
                    Block low = block(x + f[0], y + dy - 1, z + f[1]);
                    m = low != null && PropGeom.isDoor(low) ? meta(x + f[0], y + dy - 1, z + f[1]) : 0;
                }
                if (((m & 1) == 0) == (f[0] != 0))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Places a prop if it fits (see the class comment). mount: 0 standing (needs support below),
     * 1 hung on a wall or a ceiling (no support needed; the caller picked the wall / ceiling and its
     * metadata), 2 lying in the road (support below, may touch nothing). Returns true when placed.
     */
    boolean place(Block b, int m, int x, int y, int z, int mount)
    {
        String name = String.valueOf(Block.blockRegistry.getNameForObject(b));
        int[] t = tally.get(name);
        if (t == null)
        {
            t = new int[2];
            tally.put(name, t);
        }
        if (b == null || !fits(b, m, x, y, z, mount))
        {
            t[1]++;
            return false;
        }
        if (mount != 1)
        {
            Block below = block(x, y - 1, z);
            if (below instanceof BlockSlab && (meta(x, y - 1, z) & 8) == 0 && !below.isOpaqueCube())
            {
                Block whole = wholeSlab(below);
                if (whole != null)
                {
                    put(whole, meta(x, y - 1, z) & 7, x, y - 1, z);   // a pad under it, flush with the raised walk
                }
            }
        }
        put(b, m, x, y, z);
        float[] d = PropBoxes.box(b, m);
        if (d != null)
        {
            boxes.add(PropGeom.box(b, m, x, y, z));
        }
        t[0]++;
        return true;
    }

    private static Block wholeSlab(Block slab)
    {
        if (slab == Blocks.stone_slab)
        {
            return Blocks.double_stone_slab;
        }
        if (slab == Blocks.wooden_slab)
        {
            return Blocks.double_wooden_slab;
        }
        return null;
    }

    private boolean fits(Block b, int m, int x, int y, int z, int mount)
    {
        Block here = inside(x, y, z) ? block(x, y, z) : null;
        if (!inside(x, y, z) || here != null && !BuildingLayout.cover(here))
        {
            return false;                                  // taken (a carpet, a plate, a plant is replaced)
        }
        float[] d = PropBoxes.box(b, m);
        if (d == null)
        {
            return true;                                   // a plain block: its own cell only
        }
        float[] p = PropGeom.box(b, m, x, y, z);
        if (p[0] < -PropGeom.SMALL || p[4] < -PropGeom.SMALL || p[1] > w + PropGeom.SMALL || p[5] > l + PropGeom.SMALL)
        {
            return false;                                  // drawn into the next chunk's plan
        }
        if (mount != 1)
        {
            Block below = block(x, y - 1, z);
            boolean halfSlab = below instanceof BlockSlab && (meta(x, y - 1, z) & 8) == 0 && wholeSlab(below) != null;
            if (!supports(x, y - 1, z) && !halfSlab)
            {
                return false;
            }
            if (inDoorway(x, y, z))
            {
                return false;
            }
        }
        for (int cy = Math.max(0, (int) Math.floor(p[2])); cy <= Math.min(h - 1, (int) Math.floor(p[3])); cy++)
        {
            for (int cz = Math.max(0, (int) Math.floor(p[4]) - 1); cz <= Math.min(l - 1, (int) Math.floor(p[5]) + 1); cz++)
            {
                for (int cx = Math.max(0, (int) Math.floor(p[0]) - 1); cx <= Math.min(w - 1, (int) Math.floor(p[1]) + 1); cx++)
                {
                    if (cx == x && cy == y && cz == z || cy < y)
                    {
                        continue;                          // its own cell, the ground it stands in
                    }
                    Block k = block(cx, cy, cz);
                    if (k == null)
                    {
                        continue;
                    }
                    if (mount == 1 && cy == y && (PropBoxes.box(k, meta(cx, cy, cz)) == null))
                    {
                        continue;                          // the wall / ceiling it hangs on
                    }
                    float[] q = PropGeom.shape(k, meta(cx, cy, cz));
                    if (q != null && PropGeom.overlap(p, cx + q[0], cx + q[1], cy + q[2], cy + q[3], cz + q[4], cz + q[5],
                                                      PropGeom.isDoor(k) ? 0 : PropGeom.SMALL))
                    {
                        return false;
                    }
                }
            }
        }
        for (float[] q : boxes)
        {
            if (PropGeom.overlap(p, q[0], q[1], q[2], q[3], q[4], q[5], PropGeom.SMALL))
            {
                return false;
            }
        }
        return true;
    }
}

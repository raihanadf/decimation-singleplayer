package net.decimation.worldgen.city;

import net.decimation.worldgen.military.PropBoxes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;

/**
 * Where things stand inside their cell, shared by the prop fitter (PropFit) and the city dressing
 * (Dress): a prop's drawn box (PropBoxes, shrunk by TOL), a block's shape, overlaps with the
 * thresholds of tools/props/propclash.py (boxes shrunk by 0.08, overlaps under 0.15 allowed,
 * a door never touched).
 */
final class PropGeom
{
    static final float TOL = 0.08f;
    static final float SMALL = 0.15f - TOL - 0.005f;   // propclash reports from 0.15: stay just under

    private PropGeom()
    {
    }

    static boolean isDoor(Block k)
    {
        String n = String.valueOf(Block.blockRegistry.getNameForObject(k));
        return (n.contains("door") || n.contains("Door")) && !n.contains("trapdoor");
    }

    /** Glass and stained glass blocks (not panes): full cubes that are not opaque. */
    static boolean fullGlass(Block b)
    {
        return b.getMaterial() == Material.glass && !(b instanceof BlockPane);
    }

    /** A prop's drawn box at (x, y, z), shrunk by TOL: {x0, x1, y0, y1, z0, z1}. */
    static float[] box(Block b, int m, int x, int y, int z)
    {
        return shrink(PropBoxes.box(b, m), x, y, z);
    }

    /**
     * A drawn box moved to (x, y, z) and shrunk by TOL per side, but never thinner than 0.04: a flat TV
     * (0.125 deep) shrunk by 0.08 a side turned inside out and overlapped nothing at all.
     */
    static float[] shrink(float[] d, int x, int y, int z)
    {
        float[] o = {x, y, z};
        float[] r = new float[6];
        for (int a = 0; a < 3; a++)
        {
            float lo = d[2 * a], hi = d[2 * a + 1];
            float t = Math.min(TOL, Math.max(0f, (hi - lo - 0.04f) / 2f));
            r[2 * a] = o[a] + lo + t;
            r[2 * a + 1] = o[a] + hi - t;
        }
        return r;
    }

    /** Where a neighbour stands in its cell: a prop's drawn box (shrunk), a block's shape, null for nothing. */
    static float[] shape(Block k, int m)
    {
        float[] d = PropBoxes.box(k, m);
        if (d != null)
        {
            return shrink(d, 0, 0, 0);
        }
        String nm = String.valueOf(Block.blockRegistry.getNameForObject(k));
        if (nm.startsWith("deci:decal.") || nm.startsWith("deci:BlockGraffiti"))
        {
            return new float[] {0, 1, 0, 1, 0, 1};      // a picture on the wall: nothing else draws in its cell
        }
        if (isDoor(k))
        {
            return new float[] {0, 1, 0, 1, 0, 1};
        }
        String n = String.valueOf(Block.blockRegistry.getNameForObject(k));
        if (n.endsWith("pressure_plate"))
        {
            return new float[] {0, 1, 0, 0.0625f, 0, 1};
        }
        if (n.endsWith("daylight_detector"))
        {
            return new float[] {0, 1, 0, 0.375f, 0, 1};
        }
        if (k instanceof BlockTrapDoor)
        {
            return (m & 4) != 0 ? null : (m & 8) != 0 ? new float[] {0, 1, 0.8125f, 1, 0, 1} : new float[] {0, 1, 0, 0.1875f, 0, 1};
        }
        if (k.getMaterial().isLiquid())
        {
            return new float[] {0, 1, 0, 1, 0, 1};      // never drawn into water or lava
        }
        if (k instanceof net.minecraft.block.BlockBed)
        {
            return new float[] {0, 1, 0, 0.5625f, 0, 1};
        }
        if (k instanceof BlockCauldron)
        {
            return new float[] {0, 1, 0, 0.8f, 0, 1};
        }
        if (k instanceof BlockPane)
        {
            return new float[] {0.4375f, 0.5625f, 0, 1, 0.4375f, 0.5625f};
        }
        if (k instanceof BlockChest)
        {
            return new float[] {0.0625f, 0.9375f, 0, 0.875f, 0.0625f, 0.9375f};
        }
        if (k instanceof BlockFence || k instanceof BlockWall)
        {
            return new float[] {0.375f, 0.625f, 0, 1, 0.375f, 0.625f};
        }
        if (k instanceof BlockSlab && !k.isOpaqueCube())
        {
            return (m & 8) != 0 ? new float[] {0, 1, 0.5f, 1, 0, 1} : new float[] {0, 1, 0, 0.5f, 0, 1};
        }
        if (k.isOpaqueCube() || fullGlass(k) || k instanceof BlockStairs || k instanceof BlockLeaves)
        {
            return new float[] {0, 1, 0, 1, 0, 1};
        }
        return null;
    }

    static boolean overlap(float[] p, float x0, float x1, float y0, float y1, float z0, float z1, float min)
    {
        float ox = Math.min(p[1], x1) - Math.max(p[0], x0);
        float oy = Math.min(p[3], y1) - Math.max(p[2], y0);
        float oz = Math.min(p[5], z1) - Math.max(p[4], z0);
        return ox > 0 && oy > 0 && oz > 0 && Math.min(ox, Math.min(oy, oz)) >= min;   // propclash reports from 0.15
    }
}

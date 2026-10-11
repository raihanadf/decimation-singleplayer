package net.decimation.worldgen.city;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.decimation.worldgen.military.PropBoxes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockWall;
import net.minecraft.block.BlockWeb;
import net.minecraft.init.Blocks;

/**
 * Fits the Decimation props of a converted Lost Cities piece (building, street part, scene, front,
 * park) to their DRAWN size, once when the schematic is loaded (docs/city_engine.md "Props fitted
 * to their drawn size"). tools/lctranslate.py maps furniture of the 1.20 mods cell by cell, but
 * Decimation's models are not cell sized and the 1.20 furniture often hung on walls and ceilings.
 * A seed 1 world had 3609 tools/props/propclash.py findings over 11009 props, 93 % of them inside
 * converted buildings:
 * <ul>
 * <li>a lamp hung under a ceiling became a floor lantern in the air: it becomes a ceiling light
 * (BlockLightOff), a lamp with neither floor nor ceiling (a wall lamp, a chain link) goes;</li>
 * <li>a wall shelf became a cardboard box stack in the air: it becomes a plank ledge (upper oak
 * slab), so whatever stood on the shelf still stands;</li>
 * <li>a seat, screen or machine with its front against a wall and free space behind it is turned
 * around;</li>
 * <li>a prop drawn into a wall, a door or another prop (wood crates are 1.38 long, cardboard boxes
 * reach 0.22 out of their cell) takes the first facing that fits; a wood crate that fits no facing
 * (a corner, a row of crates) becomes a vanilla chest facing the room (also Decimation loot), never
 * a third chest in a row (such a crate keeps its 0.19 overhang: loot first);</li>
 * <li>a prop that floats (its 1.20 stand was not translated) drops up to 3 to the floor below, loose clutter
 * that cannot goes; seats and clutter in a door's walkway go; a machine taller than its room goes.</li>
 * </ul>
 * Same thresholds as tools/props/propclash.py (boxes shrunk by 0.08, overlaps under 0.15 allowed).
 */
final class PropFit
{
    private static final float TOL = PropGeom.TOL;
    private static final float SMALL = PropGeom.SMALL;
    private static final int[][] FRONT = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};   // metadata 2 E, 3 S, 4 W, 5 N

    /** Props whose front matters (a seat, a screen, a door of a cabinet): never turned to fit. */
    private static final Set<String> FRONTED = new HashSet<String>(Arrays.asList(
        "BlockChair", "BlockOfficeChair", "BlockStreetBench", "BlockMonitor", "BlockRadio1", "BlockWeaponCabinet",
        "BlockVendingMachine_1", "BlockVendingMachine_2", "BlockMilitaryRadio", "BlockMilitaryRadioSmall",
        "BlockPowerGenerator", "BlockHazardLight", "BlockSpotlight", "BlockMedicalCrate", "BlockWashingMachine"));
    /** Props hung on the wall behind them or under the ceiling, and multiblocks: left as they are. */
    private static final Set<String> LEAVE = new HashSet<String>(Arrays.asList(
        "BlockKeycardScreenMilitary", "BlockKeycardScreen", "BlockKeycardScreenLaboratory", "BlockWallflag", "BlockCCTV",
        "BlockExitLight", "BlockElectricBox1", "BlockWaterfountain", "BlockHazardScreen_1", "BlockHazardScreen_2",
        "BlockLight", "BlockLightOff", "BlockCeilingVent", "BlockCeilingVentCorner", "BlockBarrierTall",
        "BlockStreetBarrier", "BlockMetalShelf", "BlockMetalShelf_Empty", "BlockWreckageMilitary2", "BlockPhonebox"));
    /** Props hung under a ceiling. */
    private static final Set<String> CEILING = new HashSet<String>(Arrays.asList(
        "BlockLight", "BlockLightOff", "BlockCeilingVent", "BlockCeilingVentCorner"));
    /** Seats: removed from a door's walkway (DeceasedCraft porches put a chair before the door). */
    private static final Set<String> SEATS = new HashSet<String>(Arrays.asList(
        "BlockChair", "BlockOfficeChair", "BlockStreetBench"));
    /** Loose clutter without loot: removed when it blocks a doorway or fits nowhere. */
    private static final Set<String> DECOR = new HashSet<String>(Arrays.asList(
        "BlockCardboardBoxes1", "BlockCardboardBoxes2", "BlockCardboardBoxes3", "BlockBarrel", "BlockTrashBag1",
        "BlockTrashBag2"));

    private final LcContent.Shape s;
    final int[] count = new int[7];   // lights, ledges, turned, chests, faced, dropped, removed

    private PropFit(LcContent.Shape s)
    {
        this.s = s;
    }

    /** Fits the props of a freshly loaded shape in place; returns the counts (see {@link #count}). */
    static int[] fit(LcContent.Shape s)
    {
        PropFit f = new PropFit(s);
        f.run();
        f.run();   // a prop checked before its neighbour changed (crate -> chest) is checked again
        return f.count;
    }

    private void run()
    {
        Block lantern = Block.getBlockFromName("deci:BlockLantern");
        Block boxes = Block.getBlockFromName("deci:BlockCardboardBoxes3");
        Block light = Block.getBlockFromName("deci:BlockLightOff");
        Block crate = Block.getBlockFromName("deci:BlockWoodCrate");
        Block crateOpen = Block.getBlockFromName("deci:BlockWoodCrateOpen");
        for (int y = 0; y < s.height; y++)
        {
            for (int z = 0; z < s.length; z++)
            {
                for (int x = 0; x < s.width; x++)
                {
                    int i = s.index(x, y, z);
                    Block b = s.blocks[i];
                    int m = s.meta[i] & 15;
                    if (b == null || s.skip[i] || PropBoxes.box(b, m) == null)
                    {
                        continue;
                    }
                    String name = Block.blockRegistry.getNameForObject(b).substring(5);
                    if (CEILING.contains(name) && !solid(x, y + 1, z))
                    {
                        // hung from nothing (the 1.20 lamp hung on a chain or a block we do not have): up to
                        // the ceiling if it is within 3, else it goes
                        int u = 1;
                        while (u <= 3 && y + u < s.height && at(x, y + u, z) == null && !solid(x, y + u + 1, z))
                        {
                            u++;
                        }
                        boolean up = u <= 3 && y + u < s.height && at(x, y + u, z) == null && solid(x, y + u + 1, z);
                        if (up)
                        {
                            set(s.index(x, y + u, z), b, m);
                        }
                        set(i, null, 0);
                        count[up ? 5 : 6]++;
                        continue;
                    }
                    if (LEAVE.contains(name))
                    {
                        continue;
                    }
                    if (!supported(x, y - 1, z))
                    {
                        if (b == lantern && light != null)
                        {
                            boolean ceiling = solid(x, y + 1, z);
                            set(i, ceiling ? light : null, ceiling ? 2 : 0);
                            count[0]++;
                            continue;
                        }
                        if (b == boxes)
                        {
                            set(i, Blocks.wooden_slab, 8);
                            count[1]++;
                            continue;
                        }
                        int d = 1;
                        while (d <= 3 && y - d >= 0 && at(x, y - d, z) == null && !supported(x, y - d - 1, z))
                        {
                            d++;
                        }
                        if (d <= 3 && y - d >= 0 && at(x, y - d, z) == null && !s.skip[s.index(x, y - d, z)])
                        {
                            set(s.index(x, y - d, z), b, m);     // fell off whatever it stood on in the 1.20 build
                            set(i, null, 0);
                            count[5]++;
                            continue;
                        }
                        if (DECOR.contains(name))
                        {
                            set(i, null, 0);
                            count[6]++;
                            continue;
                        }
                    }
                    if ((DECOR.contains(name) || SEATS.contains(name)) && inDoorway(x, y, z))
                    {
                        set(i, null, 0);
                        count[6]++;
                        continue;
                    }
                    if (FRONTED.contains(name))
                    {
                        int[] f = m >= 2 && m <= 5 ? FRONT[m - 2] : null;
                        if (f != null && (wall(x + f[0], y, z + f[1])
                                          || name.equals("BlockStreetBench") && railing(x + f[0], y, z + f[1]))
                            && !railing(x - f[0], y, z - f[1])
                            && (!wall(x - f[0], y, z - f[1])                    // open behind it, or a counter /
                                || wall(x + f[0], y + 1, z + f[1]) && at(x - f[0], y + 1, z - f[1]) == null))  // desk
                        {
                            set(i, b, PropBoxes.turn(b, PropBoxes.turn(b, m)));
                            count[4]++;
                        }
                        else if (tooTall(x, y, z, b, m))
                        {
                            set(i, null, 0);                              // a vending machine under a low ceiling
                            count[6]++;
                        }
                        continue;
                    }
                    if (!clashes(x, y, z, b, m))
                    {
                        continue;
                    }
                    int t = m;
                    boolean fits = false;
                    for (int k = 0; k < 3 && !fits; k++)
                    {
                        t = PropBoxes.turn(b, t);
                        fits = !clashes(x, y, z, b, t);
                    }
                    if (fits)
                    {
                        set(i, b, t);
                        count[2]++;
                    }
                    else if ((b == crate || b == crateOpen) && chestAllowed(x, y, z))
                    {
                        set(i, Blocks.chest, chestFacing(x, y, z));
                        count[3]++;
                    }
                    else if (DECOR.contains(name))
                    {
                        set(i, null, 0);
                        count[6]++;
                    }
                }
            }
        }
    }

    private void set(int i, Block b, int m)
    {
        s.blocks[i] = b;
        s.meta[i] = (byte) m;
    }

    private Block at(int x, int y, int z)
    {
        if (x < 0 || y < 0 || z < 0 || x >= s.width || y >= s.height || z >= s.length)
        {
            return null;
        }
        int i = s.index(x, y, z);
        return s.skip[i] ? null : s.blocks[i];
    }

    private int metaAt(int x, int y, int z)
    {
        return s.meta[s.index(x, y, z)] & 15;
    }

    /** Is a cell beside (x, y, z) a door this cell lies in the walkway of (in front of / behind it)? */
    private boolean inDoorway(int x, int y, int z)
    {
        for (int[] f : FRONT)
        {
            for (int dy = 0; dy >= -1; dy--)
            {
                Block k = at(x + f[0], y + dy, z + f[1]);
                if (k == null || !PropGeom.isDoor(k))
                {
                    continue;
                }
                int m = metaAt(x + f[0], y + dy, z + f[1]);
                if ((m & 8) != 0)
                {
                    Block low = at(x + f[0], y + dy - 1, z + f[1]);
                    m = low != null && PropGeom.isDoor(low) ? metaAt(x + f[0], y + dy - 1, z + f[1]) : 0;
                }
                if (((m & 1) == 0) == (f[0] != 0))      // vanilla door meta: even = walked through along x
                {
                    return true;
                }
            }
        }
        return false;
    }

    /** A full block: a wall, a floor, a ceiling. */
    private boolean solid(int x, int y, int z)
    {
        Block b = at(x, y, z);
        return b != null && (b.isOpaqueCube() || b instanceof BlockLeaves || PropGeom.fullGlass(b)
                             || (b instanceof BlockSlab && (metaAt(x, y, z) & 8) != 0));
    }

    /** A wall for a front: a full block, not a window pane or a fence a seat may face. */
    private boolean wall(int x, int y, int z)
    {
        Block b = at(x, y, z);
        return b != null && (b.isOpaqueCube() || b instanceof BlockLeaves);
    }

    /** A railing a bench was set to face (iron bars, fence): benches look the other way, at the street. */
    private boolean railing(int x, int y, int z)
    {
        Block b = at(x, y, z);
        return b instanceof BlockPane && b.getMaterial() != Material.glass || b instanceof BlockFence || b instanceof BlockWall;
    }

    /** Something a standing prop rests on (the cell below it), as propclash.py counts it. */
    private boolean supported(int x, int y, int z)
    {
        if (y < 0)
        {
            return true;                                  // the world below the piece
        }
        Block b = at(x, y, z);
        if (b == null)
        {
            return x < 0 || z < 0 || x >= s.width || z >= s.length;
        }
        float[] box = PropBoxes.box(b, metaAt(x, y, z));
        if (box != null)
        {
            return box[3] >= 0.7f;                        // on a crate, a table, a counter
        }
        return b.getMaterial().isSolid() && !(b instanceof BlockWeb) || b instanceof BlockSlab
            || b instanceof BlockStairs || b instanceof BlockFence || b instanceof BlockWall || b instanceof BlockChest;
    }

    /** Is the prop drawn higher than the room above its own cell? */
    private boolean tooTall(int x, int y, int z, Block b, int m)
    {
        float top = PropBoxes.box(b, m)[3] - TOL;
        for (int k = 1; k < top; k++)
        {
            Block a = at(x, y + k, z);
            float[] q = a == null ? null : PropGeom.shape(a, metaAt(x, y + k, z));
            if (q != null && q[2] < top - k - SMALL)
            {
                return true;
            }
        }
        return false;
    }

    /** Does the prop drawn at (x, y, z) with metadata m reach into a wall, a door or another prop? */
    private boolean clashes(int x, int y, int z, Block b, int m)
    {
        float[] d = PropBoxes.box(b, m);
        float[] p = PropGeom.shrink(d, x, y, z);
        for (int cy = Math.max(y, (int) Math.floor(p[2])); cy <= (int) Math.floor(p[3]); cy++)
        {
            for (int cz = (int) Math.floor(p[4]) - 1; cz <= (int) Math.floor(p[5]) + 1; cz++)
            {
                for (int cx = (int) Math.floor(p[0]) - 1; cx <= (int) Math.floor(p[1]) + 1; cx++)
                {
                    if (cx == x && cy == y && cz == z)
                    {
                        continue;
                    }
                    Block k = at(cx, cy, cz);
                    if (k == null)
                    {
                        continue;
                    }
                    float[] q = PropGeom.shape(k, metaAt(cx, cy, cz));
                    if (q == null)
                    {
                        continue;
                    }
                    if (PropGeom.overlap(p, cx + q[0], cx + q[1], cy + q[2], cy + q[3], cz + q[4], cz + q[5], PropGeom.isDoor(k) ? 0 : SMALL))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** A chest here would not make a run of three (vanilla pairs only two). */
    private boolean chestAllowed(int x, int y, int z)
    {
        int near = 0;
        for (int[] f : FRONT)
        {
            if (at(x + f[0], y, z + f[1]) == Blocks.chest)
            {
                near++;
                for (int[] g : FRONT)
                {
                    if ((g[0] != -f[0] || g[1] != -f[1]) && at(x + f[0] + g[0], y, z + f[1] + g[1]) == Blocks.chest)
                    {
                        return false;
                    }
                }
            }
        }
        return near <= 1;
    }

    /** Vanilla chest metadata (2 N, 3 S, 4 W, 5 E) toward the first open side. */
    private int chestFacing(int x, int y, int z)
    {
        int[] meta = {5, 3, 4, 2};
        for (int k = 0; k < 4; k++)
        {
            if (!solid(x + FRONT[k][0], y, z + FRONT[k][1]))
            {
                return meta[k];
            }
        }
        return 3;
    }
}

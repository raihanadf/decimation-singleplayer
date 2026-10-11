package net.decimation.worldgen.city;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;

/**
 * Which way every Decimation prop looks, per metadata, VERIFIED from the renderers, the models and
 * the gallery photos (docs/references/prop_facing.tsv, copied to assets/deciworldgen/prop_facing.tsv;
 * docs/prop_placement.md section 3). The dressing never computes a prop's metadata itself: it asks
 * {@link #meta(Block, int)} for the metadata whose FRONT (screen, seat, sign face, lens, headlights)
 * points a direction, and {@link #along(Block, boolean)} for long props laid along an axis.
 */
final class PropFacing
{
    /** Directions: 0 E (+x), 1 S (+z), 2 W (-x), 3 N (-z). */
    static final int E = 0, S = 1, W = 2, N = 3;

    private static final class Row
    {
        String mount;
        int[] front = {-1, -1, -1, -1};   // per metadata 2..5: direction 0..3, -1 symmetric / unknown
        char[] axis = {'-', '-', '-', '-'};
    }

    private static Map<String, Row> rows;

    private PropFacing()
    {
    }

    private static Row row(Block b)
    {
        if (rows == null)
        {
            load();
        }
        String n = String.valueOf(Block.blockRegistry.getNameForObject(b));
        return rows.get(n.startsWith("deci:") ? n.substring(5) : n);
    }

    /** Metadata (2..5) whose front points dir; for a symmetric prop any (2); -1 if the table lacks it. */
    static int meta(Block b, int dir)
    {
        Row r = row(b);
        if (r == null)
        {
            return -1;
        }
        boolean any = false;
        for (int m = 0; m < 4; m++)
        {
            if (r.front[m] == dir)
            {
                return m + 2;
            }
            any |= r.front[m] >= 0;
        }
        return any ? -1 : 2;
    }

    /** Metadata (2..5) that lays a long prop along x (or z); turn picks one of the two that do. */
    static int along(Block b, boolean x, int turn)
    {
        Row r = row(b);
        if (r == null)
        {
            return -1;
        }
        int found = -1, n = 0;
        for (int m = 0; m < 4; m++)
        {
            if (r.axis[m] == (x ? 'x' : 'z'))
            {
                if (found < 0 || n == (turn & 1))
                {
                    found = m + 2;
                }
                n++;
            }
        }
        return found;
    }

    /** floor, wall, ceiling, road, floor_multiblock, flat_picture, other; null if not in the table. */
    static String mount(Block b)
    {
        Row r = row(b);
        return r == null ? null : r.mount;
    }

    /** Direction the front points at a metadata, -1 if symmetric / unknown. */
    static int front(Block b, int meta)
    {
        Row r = row(b);
        return r == null || meta < 2 || meta > 5 ? -1 : r.front[meta - 2];
    }

    private static synchronized void load()
    {
        Map<String, Row> m = new HashMap<String, Row>();
        InputStream in = PropFacing.class.getResourceAsStream("/assets/deciworldgen/prop_facing.tsv");
        if (in != null)
        {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8")))
            {
                String line;
                while ((line = r.readLine()) != null)
                {
                    if (line.startsWith("#") || line.startsWith("prop\t") || line.trim().isEmpty())
                    {
                        continue;
                    }
                    // prop  mount  front_m2 front_m3 front_m4 front_m5  axis_m2 axis_m3 axis_m4 axis_m5 ...
                    String[] f = line.split("\t");
                    Row row = new Row();
                    row.mount = f[1];
                    for (int k = 0; k < 4; k++)
                    {
                        row.front[k] = "ESWN".indexOf(f[2 + k].isEmpty() ? '-' : f[2 + k].charAt(0));
                        row.axis[k] = f[6 + k].isEmpty() ? '-' : f[6 + k].charAt(0);
                    }
                    m.put(f[0], row);
                }
            }
            catch (Exception e)
            {
                // an empty table: the dressing places nothing that needs a facing
            }
        }
        rows = m;
    }
}

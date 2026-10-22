package net.decimation.fixes.gunpack;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The attachment table lines gun packages add to the shipped ones (assets/deciworldgen/sight_offsets.txt,
 * grip_offsets.txt, sight_mounts.txt), in the same text format, so SightPlacement, GripPlacement, GripRails and
 * SightMounts read a package's gun like every other gun: each reads its table through {@link #with}. Sight rows come
 * from the package's points like tools/guns/sightfit.py's (dy = railTop + 4.45 down when over 0.06; dx from the top
 * rail), the mount row from points.mount, grip rows from GripFit (GunPacks). Plain Java (unit test GunPackRowsTest).
 */
public final class GunPackRows
{
    public static final String SIGHT_TABLE = "sight_offsets.txt", GRIP_TABLE = "grip_offsets.txt", MOUNT_TABLE = "sight_mounts.txt";
    /** Receiver top Decimation's sights are placed for (sightfit.DESIGN_TOP) and the gap left alone (sightfit.TOL). */
    static final double DESIGN_TOP = -4.45, TOL = 0.06;
    /** Where Decimation draws a long scope along the gun (x 0.8 .. 5.5; the red dot 2.4 .. 2.8: study.py attach). */
    static final double SCOPE_FROM = 0.8, SCOPE_TO = 5.5;
    /** sightfit.SIGHTS, used when the shipped table cannot be read. */
    static final List<String> SIGHTS = Arrays.asList("reddot", "2x", "4x", "8x", "dragunovScope", "eotech558", "ta11acog",
                                                     "specterdr");
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final Map<String, List<String>> LINES = new HashMap<String, List<String>>();

    private GunPackRows()
    {
    }

    public static synchronized void add(String table, String line)
    {
        List<String> l = LINES.get(table);
        if (l == null)
        {
            LINES.put(table, l = new ArrayList<String>());
        }
        l.add(line);
    }

    public static synchronized List<String> lines(String table)
    {
        List<String> l = LINES.get(table);
        return l == null ? new ArrayList<String>() : new ArrayList<String>(l);
    }

    /** Forgets every added line (unit tests). */
    public static synchronized void clear()
    {
        LINES.clear();
    }

    /** The shipped table (may be null) followed by the packages' lines; null when there is neither. */
    public static InputStream with(InputStream shipped, String table)
    {
        List<String> ours = lines(table);
        if (ours.isEmpty())
        {
            return shipped;
        }
        StringBuilder s = new StringBuilder("\n");
        for (String l : ours)
        {
            s.append(l).append('\n');
        }
        InputStream extra = new ByteArrayInputStream(s.toString().getBytes(UTF8));
        return shipped == null ? extra : new SequenceInputStream(shipped, extra);
    }

    /** How far sights go down on this gun (sightfit: railTop against -4.45, only down, left alone within 0.06). */
    public static double sightDy(GunPack p)
    {
        if (p.points.railTop == null)
        {
            return 0;
        }
        double dy = p.points.railTop - DESIGN_TOP;
        return dy > TOL ? round3(dy) : 0;
    }

    /**
     * How far sights go along the gun: points.sightShift when given; else 0 when the top rail holds a long scope where
     * Decimation draws it (x 0.8 .. 5.5) or there is no top rail; else the least shift that puts the scope on the rail
     * (back for a rail ending early, the AS Val case), or the scope's middle on the rail's middle for a rail shorter
     * than a scope.
     */
    public static double sightDx(GunPack p)
    {
        if (p.points.sightShift != null)
        {
            return round3(p.points.sightShift);
        }
        double[] r = p.points.rail;
        if (r == null || r[0] <= SCOPE_FROM && SCOPE_TO <= r[1])
        {
            return 0;
        }
        if (r[1] - r[0] >= SCOPE_TO - SCOPE_FROM)
        {
            return round3(r[1] < SCOPE_TO ? r[1] - SCOPE_TO : r[0] - SCOPE_FROM);
        }
        return round3((r[0] + r[1]) / 2 - (SCOPE_FROM + SCOPE_TO) / 2);
    }

    /** sight_offsets.txt lines for this gun ("gun sight dy [dx]"), none for a gun without a sight mount. */
    public static List<String> sightLines(GunPack p, Collection<String> sights)
    {
        List<String> out = new ArrayList<String>();
        if (!p.takesSights())
        {
            return out;
        }
        double dy = sightDy(p), dx = sightDx(p);
        if (dy <= 0 && dx == 0)
        {
            return out;   // Decimation's own placement fits: no row, as sightfit writes none
        }
        for (String s : sights)
        {
            out.add(p.name + " " + s + " " + plain(dy) + (dx != 0 ? " " + plain(dx) : ""));
        }
        return out;
    }

    /** sight_mounts.txt line ("gun rail|side|slide"), or null for a gun that takes no sight. */
    public static String mountLine(GunPack p)
    {
        return p.takesSights() ? p.name + " " + p.points.mount : null;
    }

    /** grip_offsets.txt line ("gun grip dx dy dz"). */
    public static String gripLine(String gun, String grip, double[] d)
    {
        return String.format(Locale.ROOT, "%s %s %.3f %.3f %.3f", gun, grip, d[0], d[1], d[2]);
    }

    /** The sight names of the shipped sight_offsets.txt (its second column, as sightfit.SIGHTS lists them). */
    public static List<String> sightNames(InputStream shipped)
    {
        Set<String> out = new LinkedHashSet<String>();
        if (shipped != null)
        {
            try
            {
                BufferedReader r = new BufferedReader(new InputStreamReader(shipped, UTF8));
                for (String line; (line = r.readLine()) != null; )
                {
                    String[] f = line.trim().split("\\s+");
                    if ((f.length == 3 || f.length == 4) && !line.trim().startsWith("#"))
                    {
                        out.add(f[1]);
                    }
                }
                r.close();
            }
            catch (Exception e)
            {
                out.clear();
            }
        }
        return out.isEmpty() ? new ArrayList<String>(SIGHTS) : new ArrayList<String>(out);
    }

    static double round3(double v)
    {
        return Math.round(v * 1000.0) / 1000.0 + 0.0;   // + 0.0: no -0.0
    }

    /** A number as sightfit writes it: no trailing zeros ("0.85", "-4.0"). */
    static String plain(double v)
    {
        String s = String.format(Locale.ROOT, "%.3f", v);
        s = s.replaceAll("0+$", "");
        return s.endsWith(".") ? s + "0" : s;
    }
}

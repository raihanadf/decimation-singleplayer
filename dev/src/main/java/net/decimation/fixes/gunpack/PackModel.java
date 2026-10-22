package net.decimation.fixes.gunpack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A .bmodel file read as geometry, the way tools/guns/study.py reads it (docs/gun_model_spec.md section 2): header lines
 * (flamePos, rhPos ...), the texture size, every part with its shape box (offset, 8 corner offsets, size), rotation
 * point, rotation and parent (addChild). Children of a parent the file never declares are left out, as Decimation's
 * BModelLoader never draws them. Plain Java: the gun package loader measures a package's model with it on both sides
 * (the dedicated server has no Decimation model classes) and unit tests run it without Minecraft.
 */
public final class PackModel
{
    private static final String NUM = "-?[\\d.]+(?:[eE]-?\\d+)?F?";
    private static final Pattern HEADER = Pattern.compile("(\\w+):\\s*(" + NUM + ")\\s*,\\s*(" + NUM + ")\\s*,\\s*(" + NUM + ")\\s*;");
    private static final Pattern TEXTURE = Pattern.compile("texture(Width|Height)\\s*=\\s*(\\d+)");
    private static final Pattern DECLARE = Pattern.compile("(\\w+)\\s*=\\s*new\\s+\\w+\\(this,\\s*(\\d+),\\s*(\\d+)\\)");
    private static final Pattern CHILD = Pattern.compile("(\\w+)\\.addChild\\((\\w+)\\);");
    private static final Pattern CALL = Pattern.compile("(\\w+)\\.(addShape|setRotationPoint|setRotation)\\((.*)\\);");
    private static final Pattern NUMBER = Pattern.compile(NUM);

    /** Header lines with three numbers ("flamePos" -> x, y, z). */
    public final Map<String, double[]> header = new LinkedHashMap<String, double[]>();
    public int textureWidth = 64, textureHeight = 32;
    /** The parts drawn (children included, orphans left out), in file order. */
    public final List<Part> parts = new ArrayList<Part>();
    /** Every part the file declares (orphans included): what BModelLoader puts in BModel.parts. */
    public int declared;

    /** One shape box part (study.py Part). Model units, x forward, y down, z sideways. */
    public static final class Part
    {
        public final String name;
        final double[] off = new double[3];
        final double[][] corners = new double[8][3];
        final double[] size = new double[3];
        final double[] pivot = new double[3];
        final double[] rot = new double[3];
        Part parent;
        private double[][] verts;

        Part(String name)
        {
            this.name = name;
        }

        /** A copy (grips are placed by moving a copy of their model). */
        Part copy(String newName)
        {
            Part p = new Part(newName);
            System.arraycopy(off, 0, p.off, 0, 3);
            for (int i = 0; i < 8; i++)
            {
                System.arraycopy(corners[i], 0, p.corners[i], 0, 3);
            }
            System.arraycopy(size, 0, p.size, 0, 3);
            System.arraycopy(pivot, 0, p.pivot, 0, 3);
            System.arraycopy(rot, 0, p.rot, 0, 3);
            return p;
        }

        /** The part's group: its name without the trailing number (gunModel, ammoModel, defaultScopeModel ...). */
        public String group()
        {
            return name.replaceAll("\\d+$", "");
        }

        /**
         * The 8 corners in model space, indexed like vanilla ModelBox: v7 (0,0,0), v0 (w,0,0), v1 (w,h,0), v2 (0,h,0),
         * v3 (0,0,d), v4 (w,0,d), v5 (w,h,d), v6 (0,h,d). Corner offsets in BModelBox order (index 7 = (0,0,0),
         * 6 = (w,0,0), 4 = (w,h,0), 5 = (0,h,0), 3 = (0,0,d), 2 = (w,0,d), 0 = (w,h,d), 1 = (0,h,d)) are added; a
         * box flat in one axis keeps the corner set last, as study.py's dictionary does.
         */
        public double[][] verts()
        {
            if (verts != null)
            {
                return verts;
            }
            double w = size[0], h = size[1], d = size[2];
            int[] order = {7, 6, 4, 5, 3, 2, 0, 1};
            double[][] base = new double[8][];
            base[7] = new double[] {0, 0, 0};
            base[6] = new double[] {w, 0, 0};
            base[4] = new double[] {w, h, 0};
            base[5] = new double[] {0, h, 0};
            base[3] = new double[] {0, 0, d};
            base[2] = new double[] {w, 0, d};
            base[0] = new double[] {w, h, d};
            base[1] = new double[] {0, h, d};
            Map<String, double[]> corner = new HashMap<String, double[]>();
            for (int i : order)
            {
                double[] b = base[i];
                double[] c = corners[i];
                corner.put(key(b), new double[] {off[0] + b[0] + c[0], off[1] + b[1] + c[1], off[2] + b[2] + c[2]});
            }
            double[][] named = {
                corner.get(key(new double[] {w, 0, 0})), corner.get(key(new double[] {w, h, 0})),
                corner.get(key(new double[] {0, h, 0})), corner.get(key(new double[] {0, 0, d})),
                corner.get(key(new double[] {w, 0, d})), corner.get(key(new double[] {w, h, d})),
                corner.get(key(new double[] {0, h, d})), corner.get(key(new double[] {0, 0, 0}))};
            double[][] out = new double[8][];
            for (int i = 0; i < 8; i++)
            {
                out[i] = place(named[i]);
            }
            verts = out;
            return out;
        }

        /** Forgets the cached corners (after a move). */
        void moved()
        {
            verts = null;
        }

        private static String key(double[] p)
        {
            return (p[0] + 0.0) + "," + (p[1] + 0.0) + "," + (p[2] + 0.0);   // + 0.0: -0.0 and 0.0 are one key
        }

        /** A point in this part's space to model space: rotation Z, Y, X about the rotation point, then the parents. */
        double[] place(double[] p)
        {
            double[] q = rotZ(rotY(rotX(p, rot[0]), rot[1]), rot[2]);
            q = new double[] {q[0] + pivot[0], q[1] + pivot[1], q[2] + pivot[2]};
            return parent != null ? parent.place(q) : q;
        }

        /** The 6 faces as vertex index quads of verts(), as study.py's quads(). */
        static final int[][] QUADS = {{4, 0, 1, 5}, {7, 3, 6, 2}, {4, 3, 7, 0}, {1, 2, 6, 5}, {0, 7, 2, 1}, {3, 4, 5, 6}};

        /** Lowest and highest corner per axis: {x0, x1, y0, y1, z0, z1}. */
        public double[] bounds()
        {
            double[] b = {Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE};
            for (double[] v : verts())
            {
                for (int a = 0; a < 3; a++)
                {
                    b[2 * a] = Math.min(b[2 * a], v[a]);
                    b[2 * a + 1] = Math.max(b[2 * a + 1], v[a]);
                }
            }
            return b;
        }
    }

    static double[] rotX(double[] p, double a)
    {
        double c = Math.cos(a), s = Math.sin(a);
        return new double[] {p[0], p[1] * c - p[2] * s, p[1] * s + p[2] * c};
    }

    static double[] rotY(double[] p, double a)
    {
        double c = Math.cos(a), s = Math.sin(a);
        return new double[] {p[0] * c + p[2] * s, p[1], -p[0] * s + p[2] * c};
    }

    static double[] rotZ(double[] p, double a)
    {
        double c = Math.cos(a), s = Math.sin(a);
        return new double[] {p[0] * c - p[1] * s, p[0] * s + p[1] * c, p[2]};
    }

    private static double num(String s)
    {
        return Double.parseDouble(s.endsWith("F") || s.endsWith("f") ? s.substring(0, s.length() - 1) : s);
    }

    private static List<Double> numbers(String s)
    {
        List<Double> out = new ArrayList<Double>();
        Matcher m = NUMBER.matcher(s.replace("new float[][]", ""));
        while (m.find())
        {
            out.add(num(m.group()));
        }
        return out;
    }

    /** Reads a .bmodel text (study.py Gun.parse). */
    public static PackModel parse(String text)
    {
        PackModel model = new PackModel();
        Map<String, Part> by = new LinkedHashMap<String, Part>();
        List<Part> orphans = new ArrayList<Part>();
        for (String line : text.split("\n"))
        {
            String s = line.trim();
            Matcher m = HEADER.matcher(s);
            if (m.lookingAt())
            {
                model.header.put(m.group(1), new double[] {num(m.group(2)), num(m.group(3)), num(m.group(4))});
                continue;
            }
            m = TEXTURE.matcher(s);
            if (m.lookingAt())
            {
                if (m.group(1).equals("Width"))
                {
                    model.textureWidth = Integer.parseInt(m.group(2));
                }
                else
                {
                    model.textureHeight = Integer.parseInt(m.group(2));
                }
                continue;
            }
            m = DECLARE.matcher(s);
            if (m.lookingAt())
            {
                Part p = new Part(m.group(1));
                by.put(p.name, p);
                model.parts.add(p);
                model.declared++;
                continue;
            }
            m = CHILD.matcher(s);
            if (m.lookingAt())
            {
                Map<String, Part> low = new HashMap<String, Part>();   // BModelLoader matches names ignoring case
                for (Map.Entry<String, Part> e : by.entrySet())
                {
                    low.put(e.getKey().toLowerCase(), e.getValue());
                }
                Part parent = low.get(m.group(1).toLowerCase()), child = low.get(m.group(2).toLowerCase());
                if (child != null && parent == null)
                {
                    orphans.add(child);   // marked a child all the same and dropped by the game: never drawn
                }
                else if (child != null)
                {
                    child.parent = parent;
                }
                continue;
            }
            m = CALL.matcher(s);
            if (!m.lookingAt() || !by.containsKey(m.group(1)))
            {
                continue;
            }
            Part p = by.get(m.group(1));
            List<Double> n = numbers(m.group(3));
            if (m.group(2).equals("addShape") && n.size() >= 30)
            {
                for (int i = 0; i < 3; i++)
                {
                    p.off[i] = n.get(i);
                    p.size[i] = n.get(27 + i);
                }
                for (int c = 0; c < 8; c++)
                {
                    for (int i = 0; i < 3; i++)
                    {
                        p.corners[c][i] = n.get(3 + 3 * c + i);
                    }
                }
            }
            else if (m.group(2).equals("setRotationPoint") && n.size() >= 3)
            {
                for (int i = 0; i < 3; i++)
                {
                    p.pivot[i] = n.get(i);
                }
            }
            else if (m.group(2).equals("setRotation") && n.size() >= 3)
            {
                for (int i = 0; i < 3; i++)
                {
                    p.rot[i] = n.get(i);
                }
            }
        }
        for (Part o : orphans)
        {
            for (int i = model.parts.size() - 1; i >= 0; i--)
            {
                if (model.parts.get(i) == o)
                {
                    model.parts.remove(i);
                }
            }
        }
        return model;
    }

    /** Parts drawn at the top level (BModel.parts: children hang from their parent, orphans are dropped). */
    public int topLevel()
    {
        int n = 0;
        for (Part p : parts)
        {
            n += p.parent == null ? 1 : 0;
        }
        return n;
    }

    /** Header value or null ("flamePos" -> {x, y, z}). */
    public double[] header(String key)
    {
        return header.get(key);
    }

    /** {x0, x1, y0, y1, z0, z1} over the parts whose name starts with one of the prefixes (all parts for none). */
    public double[] bounds(String... prefixes)
    {
        double[] b = {Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE};
        for (Part p : parts)
        {
            boolean take = prefixes.length == 0;
            for (String pre : prefixes)
            {
                take |= p.name.startsWith(pre);
            }
            if (!take)
            {
                continue;
            }
            double[] pb = p.bounds();
            for (int a = 0; a < 3; a++)
            {
                b[2 * a] = Math.min(b[2 * a], pb[2 * a]);
                b[2 * a + 1] = Math.max(b[2 * a + 1], pb[2 * a + 1]);
            }
        }
        return b;
    }
}

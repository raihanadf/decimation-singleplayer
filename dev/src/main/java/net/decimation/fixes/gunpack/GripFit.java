package net.decimation.fixes.gunpack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Where a grip slot attachment hangs under a gun: a port of tools/guns/gripfit.py's fit (docs/decimation_code/weapons.md
 * section 8), so a gun package gets the same grip_offsets.txt rows the Python tool writes for every other gun. Decimation
 * draws every grip at one spot (GunItemRenderer's grip translate, (-3.2, 0.8, 0) model units); here a map of the gun's
 * underside (vertical rays through every part's real faces, a column each 0.05), the run ahead of the last thing hanging
 * below it, and in that run the place nearest the target where the underside is flat over the grip's seat. Lights with
 * no such place go on the gun's right side (the P90). The same numbers and the same decisions as the Python tool (unit
 * test GripFitTest reads the shipped table against it). A gun package's bottom rail ("gripRail") limits where a foregrip
 * may sit: the seat must lie on it. Plain Java (both sides, unit tests).
 */
public final class GripFit
{
    static final double ZC = -0.15;
    static final double STEP = 0.05;
    static final double HANG = 2.3;
    static final double NOSE = 0.6;
    static final double OVERLAP = 0.05;
    static final double CONTACT = 0.25;
    static final double REACH = 0.15;
    static final int NX = 7, NZ = 3;
    /** How far into the rail window a seat may hang over its ends. */
    static final double RAIL_SLACK = 0.05;
    /** Grip slot attachments: Decimation's and our RK-1; lights may sit on the gun's side. */
    public static final String[] GRIPS = {"flashlight", "foregrip", "laser", "rk1"};
    static final List<String> LIGHTS = Arrays.asList("laser", "flashlight");
    /** Vertical grips: they need a rail (fixes/GripRails), category rifle as Decimation's Foregrip. */
    public static final List<String> RAIL_GRIPS = Arrays.asList("foregrip", "rk1");
    /** Decimation's foregrip.bmodel: a parent plate 7.5 behind the grip (never drawn by GripPlacement). */
    static final Pattern STRAY = Pattern.compile("grip");
    /** Decimation's Foregrip's two clamp tabs: they go into the rail and do not bear (bug.md "Foregrip floated"). */
    static final Pattern CLAMP = Pattern.compile("grip_[12]");

    private GripFit()
    {
    }

    /** A part's faces as planes (outward normal n, offset d: inside where n . p <= d) and its box. */
    static final class Shape
    {
        final List<double[]> n = new ArrayList<double[]>();
        final List<Double> d = new ArrayList<Double>();
        double x0, x1, z0, z1, y0, y1;
    }

    static Shape planes(PackModel.Part part)
    {
        double[][] v = part.verts();
        double[] c = new double[3];
        for (double[] p : v)
        {
            for (int i = 0; i < 3; i++)
            {
                c[i] += p[i];
            }
        }
        for (int i = 0; i < 3; i++)
        {
            c[i] /= 8;
        }
        Shape s = new Shape();
        for (int[] q : PackModel.Part.QUADS)
        {
            double[] a = v[q[0]], b = v[q[1]], cc = v[q[2]], dd = v[q[3]];
            double[] e1 = {cc[0] - a[0], cc[1] - a[1], cc[2] - a[2]};
            double[] e2 = {dd[0] - b[0], dd[1] - b[1], dd[2] - b[2]};
            double[] n = {e1[1] * e2[2] - e1[2] * e2[1], e1[2] * e2[0] - e1[0] * e2[2], e1[0] * e2[1] - e1[1] * e2[0]};
            double nl = Math.sqrt(n[0] * n[0] + n[1] * n[1] + n[2] * n[2]);
            if (nl < 1e-9)
            {
                continue;   // a collapsed face (wedge edge)
            }
            n = new double[] {n[0] / nl, n[1] / nl, n[2] / nl};
            double[] m = new double[3];
            for (int i = 0; i < 3; i++)
            {
                m[i] = (a[i] + b[i] + cc[i] + dd[i]) / 4;
            }
            if (n[0] * (c[0] - m[0]) + n[1] * (c[1] - m[1]) + n[2] * (c[2] - m[2]) > 0)
            {
                n = new double[] {-n[0], -n[1], -n[2]};
            }
            s.n.add(n);
            s.d.add(n[0] * m[0] + n[1] * m[1] + n[2] * m[2]);
        }
        double[] b = part.bounds();
        s.x0 = b[0];
        s.x1 = b[1];
        s.y0 = b[2];
        s.y1 = b[3];
        s.z0 = b[4];
        s.z1 = b[5];
        return s;
    }

    /** Where the vertical line at x, z runs inside one part: {top, bottom} y (y grows downward), or null. */
    static double[] ray(Shape s, double x, double z)
    {
        if (!(s.x0 <= x && x <= s.x1 && s.z0 <= z && z <= s.z1))
        {
            return null;
        }
        double lo = -1e9, hi = 1e9;
        for (int i = 0; i < s.n.size(); i++)
        {
            double[] n = s.n.get(i);
            double r = s.d.get(i) - n[0] * x - n[2] * z;
            if (Math.abs(n[1]) < 1e-9)
            {
                if (r < -1e-6)
                {
                    return null;
                }
                continue;
            }
            double t = r / n[1];
            if (n[1] > 0)
            {
                hi = Math.min(hi, t);
            }
            else
            {
                lo = Math.max(lo, t);
            }
            if (lo > hi + 1e-6)
            {
                return null;
            }
        }
        return new double[] {lo, hi};
    }

    /** Where the line across the gun (along z) at x, y runs inside one part: {z0, z1}, or null. */
    static double[] rayZ(Shape s, double x, double y)
    {
        if (!(s.x0 <= x && x <= s.x1 && s.y0 <= y && y <= s.y1))
        {
            return null;
        }
        double lo = -1e9, hi = 1e9;
        for (int i = 0; i < s.n.size(); i++)
        {
            double[] n = s.n.get(i);
            double r = s.d.get(i) - n[0] * x - n[1] * y;
            if (Math.abs(n[2]) < 1e-9)
            {
                if (r < -1e-6)
                {
                    return null;
                }
                continue;
            }
            double t = r / n[2];
            if (n[2] > 0)
            {
                hi = Math.min(hi, t);
            }
            else
            {
                lo = Math.max(lo, t);
            }
            if (lo > hi + 1e-6)
            {
                return null;
            }
        }
        return new double[] {lo, hi};
    }

    private static boolean moving(PackModel.Part p)
    {
        return p.name.startsWith("ammoModel") || p.name.startsWith("pumpModel");
    }

    /** The gun's underside map (gripfit.gun_grid). */
    static final class Grid
    {
        double x0;
        double[] xs, zs;
        Double[][] under;
        boolean[][] mag;
        String cat;
        double muzzle, bore;
        List<Shape> body;
    }

    static Grid grid(PackModel gun, String cat)
    {
        List<Shape> body = new ArrayList<Shape>(), mags = new ArrayList<Shape>();
        double x0 = Double.MAX_VALUE, x1 = -Double.MAX_VALUE;
        for (PackModel.Part p : gun.parts)
        {
            (moving(p) ? mags : body).add(planes(p));
            if (!p.name.startsWith("ammoModel"))
            {
                for (double[] q : p.verts())
                {
                    x0 = Math.min(x0, q[0]);
                    x1 = Math.max(x1, q[0]);
                }
            }
        }
        Grid m = new Grid();
        int nx = (int) ((x1 - x0) / STEP) + 1;
        m.x0 = x0;
        m.xs = new double[nx];
        for (int i = 0; i < nx; i++)
        {
            m.xs[i] = x0 + STEP * i;
        }
        m.zs = new double[33];
        for (int k = -16; k <= 16; k++)
        {
            m.zs[k + 16] = ZC + STEP * k;
        }
        m.under = new Double[nx][m.zs.length];
        m.mag = new boolean[nx][m.zs.length];
        for (int i = 0; i < nx; i++)
        {
            double x = m.xs[i];
            List<Shape> bs = new ArrayList<Shape>(), ms = new ArrayList<Shape>();
            for (Shape s : body)
            {
                if (s.x0 <= x && x <= s.x1)
                {
                    bs.add(s);
                }
            }
            for (Shape s : mags)
            {
                if (s.x0 <= x && x <= s.x1)
                {
                    ms.add(s);
                }
            }
            for (int k = 0; k < m.zs.length; k++)
            {
                double z = m.zs[k];
                Double low = null, mlow = null;
                for (Shape s : bs)
                {
                    double[] h = ray(s, x, z);
                    if (h != null && (low == null || h[1] > low))
                    {
                        low = h[1];
                    }
                }
                for (Shape s : ms)
                {
                    double[] h = ray(s, x, z);
                    if (h != null && (mlow == null || h[1] > mlow))
                    {
                        mlow = h[1];
                    }
                }
                m.under[i][k] = low;
                m.mag[i][k] = mlow != null && (low == null || mlow > low - 0.05);
            }
        }
        double[] fp = gun.header("flamePos");
        m.cat = cat;
        m.muzzle = fp != null ? fp[0] : x1;
        m.bore = (fp != null ? fp[1] : 0) + 0.85;
        m.body = body;
        return m;
    }

    /** {start, end} along x of the gun's front part, from ahead of the last thing hanging below it to the muzzle. */
    static double[] frontRun(Grid m)
    {
        List<Integer> mid = new ArrayList<Integer>();
        for (int k = 0; k < m.zs.length; k++)
        {
            if (Math.abs(m.zs[k] - ZC) <= 0.3 + 1e-6)
            {
                mid.add(k);
            }
        }
        double deep = m.bore + HANG;
        if ("pistol".equals(m.cat) || "revolver".equals(m.cat))
        {
            List<Double> nose = new ArrayList<Double>();
            for (int i = 0; i < m.xs.length; i++)
            {
                double x = m.xs[i];
                if (m.muzzle - 0.4 * (m.muzzle - m.xs[0]) <= x && x <= m.muzzle)
                {
                    for (int k : mid)
                    {
                        if (m.under[i][k] != null)
                        {
                            nose.add(m.under[i][k]);
                        }
                    }
                }
            }
            if (!nose.isEmpty())
            {
                java.util.Collections.sort(nose);
                deep = Math.min(deep, nose.get(nose.size() / 2) + NOSE);
            }
        }
        Double last = null;
        for (int i = 0; i < m.xs.length; i++)
        {
            if (!(m.xs[i] <= m.muzzle - 1.0))
            {
                continue;
            }
            boolean hang = false;
            Double lowest = null;
            for (int k : mid)
            {
                hang |= m.mag[i][k];
                if (m.under[i][k] != null && (lowest == null || m.under[i][k] > lowest))
                {
                    lowest = m.under[i][k];
                }
            }
            if (hang || (lowest != null && lowest > deep))
            {
                last = m.xs[i];
            }
        }
        double start = last != null ? last + STEP : m.xs[0] + 0.45 * (m.muzzle - m.xs[0]);
        return new double[] {start, m.muzzle - 0.2};
    }

    private static int idx(Grid m, double x, boolean up)
    {
        return up ? (int) Math.floor((x - m.x0) / STEP + 1e-6) : (int) Math.ceil((x - m.x0) / STEP - 1e-6);
    }

    /** How a seat from sx0 to sx1, half width hz about the gun's line, would sit: {touch, covered, lowest|NaN, blocked 0/1}. */
    static double[] spot(Grid m, double sx0, double sx1, double hz)
    {
        List<Integer> allz = new ArrayList<Integer>(), inz = new ArrayList<Integer>();
        for (int k = 0; k < m.zs.length; k++)
        {
            if (Math.abs(m.zs[k] - ZC) <= hz + 1e-6)
            {
                allz.add(k);
            }
            if (Math.abs(m.zs[k] - ZC) <= 0.8 * hz + 1e-6)
            {
                inz.add(k);
            }
        }
        int a0 = idx(m, sx0, false), a1 = idx(m, sx1, true);
        int n0 = idx(m, sx0 + 0.1 * (sx1 - sx0), false), n1 = idx(m, sx1 - 0.1 * (sx1 - sx0), true);
        if (a0 < 0 || a1 >= m.xs.length || n1 < n0 || inz.isEmpty())
        {
            return new double[] {0, 0, Double.NaN, 0};
        }
        boolean magIn = false;
        for (int i = a0; i <= a1; i++)
        {
            for (int k : allz)
            {
                magIn |= m.mag[i][k];
            }
        }
        Double lowest = null;
        for (int i = n0; i <= n1; i++)
        {
            for (int k : inz)
            {
                if (m.under[i][k] != null && (lowest == null || m.under[i][k] > lowest))
                {
                    lowest = m.under[i][k];
                }
            }
        }
        if (lowest == null)
        {
            return new double[] {0, 0, Double.NaN, magIn ? 1 : 0};
        }
        double edge = lowest;
        for (int i = a0; i <= a1; i++)
        {
            for (int k : allz)
            {
                if (m.under[i][k] != null)
                {
                    edge = Math.max(edge, m.under[i][k]);
                }
            }
        }
        boolean blocked = magIn || edge > lowest + REACH;
        double top = lowest - OVERLAP;
        int touch = 0, covered = 0;
        for (int n = 0; n < NX; n++)
        {
            int i = (int) Math.rint((sx0 + (sx1 - sx0) * (0.1 + 0.8 * n / (NX - 1)) - m.x0) / STEP);
            Double near = null;
            boolean any = false;
            for (int k : inz)
            {
                Double u = m.under[i][k];
                if (u != null)
                {
                    any = true;
                    near = near == null ? top - u : Math.min(near, top - u);
                }
            }
            covered += any ? 1 : 0;
            touch += any && near <= CONTACT ? 1 : 0;
        }
        return new double[] {touch, covered, lowest, blocked ? 1 : 0};
    }

    /** The grip's seat, the face that bears on the gun: {x0, x1, z0, z1, top} (gripfit.seat). */
    static double[] seat(List<PackModel.Part> parts)
    {
        double top = Double.MAX_VALUE;
        for (PackModel.Part p : parts)
        {
            top = Math.min(top, p.bounds()[2]);
        }
        List<PackModel.Part> near = new ArrayList<PackModel.Part>();
        for (PackModel.Part p : parts)
        {
            if (p.bounds()[2] <= top + 0.25)
            {
                near.add(p);
            }
        }
        PackModel.Part widest = null;
        double best = -1;
        for (PackModel.Part p : near)
        {
            double[] b = p.bounds();
            double area = (b[1] - b[0]) * (b[5] - b[4]);
            if (widest == null || area > best)
            {
                widest = p;
                best = area;
            }
        }
        double level = widest.bounds()[2];
        double x0 = Double.MAX_VALUE, x1 = -Double.MAX_VALUE, z0 = Double.MAX_VALUE, z1 = -Double.MAX_VALUE;
        for (PackModel.Part p : near)
        {
            if (Math.abs(p.bounds()[2] - level) <= 0.05)
            {
                for (double[] q : p.verts())
                {
                    x0 = Math.min(x0, q[0]);
                    x1 = Math.max(x1, q[0]);
                    z0 = Math.min(z0, q[2]);
                    z1 = Math.max(z1, q[2]);
                }
            }
        }
        return new double[] {x0, x1, z0, z1, level};
    }

    /** The parts a seat is measured from: the grip's parts less its clamp tabs. */
    static List<PackModel.Part> bearing(String grip, List<PackModel.Part> parts)
    {
        if (!"foregrip".equals(grip))
        {
            return parts;
        }
        List<PackModel.Part> keep = new ArrayList<PackModel.Part>();
        for (PackModel.Part p : parts)
        {
            if (!CLAMP.matcher(p.name).matches())
            {
                keep.add(p);
            }
        }
        return keep.isEmpty() ? parts : keep;
    }

    /**
     * A grip's parts where Decimation alone draws them on any gun (the grip translate (-0.2, 0.05, 0) GL units = (-3.2,
     * 0.8, 0) model units added to the top level parts), the Foregrip's stray plate left out.
     */
    public static List<PackModel.Part> rawGrip(PackModel gripModel, String grip)
    {
        double ox = -0.2 / 0.0625, oy = 0.05 / 0.0625, oz = 0.0 / 0.0625;
        java.util.Map<PackModel.Part, PackModel.Part> copies = new java.util.IdentityHashMap<PackModel.Part, PackModel.Part>();
        for (PackModel.Part p : gripModel.parts)
        {
            copies.put(p, p.copy(p.name));
        }
        List<PackModel.Part> out = new ArrayList<PackModel.Part>();
        for (PackModel.Part p : gripModel.parts)
        {
            PackModel.Part c = copies.get(p);
            c.parent = p.parent == null ? null : copies.get(p.parent);
            if (p.parent != null && c.parent == null)
            {
                c.parent = p.parent;   // a parent outside the drawn list keeps its own place
            }
            if (c.parent == null)
            {
                c.pivot[0] += ox;
                c.pivot[1] += oy;
                c.pivot[2] += oz;
            }
            if (!("foregrip".equals(grip) && STRAY.matcher(p.name).matches()))
            {
                out.add(c);
            }
        }
        return out;
    }

    /** What the fit knows about one grip at Decimation's spot: {gx0, gx1, gy0, gy1, gz0, gz1, sx0, sx1, sz0, sz1, top}. */
    public static double[] gripBox(List<PackModel.Part> parts, String grip)
    {
        double gx0 = Double.MAX_VALUE, gx1 = -Double.MAX_VALUE, gy0 = Double.MAX_VALUE, gy1 = -Double.MAX_VALUE,
            gz0 = Double.MAX_VALUE, gz1 = -Double.MAX_VALUE;
        for (PackModel.Part p : parts)
        {
            for (double[] q : p.verts())
            {
                gx0 = Math.min(gx0, q[0]);
                gx1 = Math.max(gx1, q[0]);
                gy0 = Math.min(gy0, q[1]);
                gy1 = Math.max(gy1, q[1]);
                gz0 = Math.min(gz0, q[2]);
                gz1 = Math.max(gz1, q[2]);
            }
        }
        double[] s = seat(bearing(grip, parts));
        return new double[] {gx0, gx1, gy0, gy1, gz0, gz1, s[0], s[1], s[2], s[3], s[4]};
    }

    /** One gun measured once (its underside map), for the fits of its grips. */
    public static final class Gun
    {
        final Grid grid;

        public Gun(PackModel model, String cat)
        {
            grid = grid(model, cat);
        }

        /** {start, end} of the gun's front run (where grips may hang). */
        public double[] frontRun()
        {
            return GripFit.frontRun(grid);
        }

        public double[] fit(String grip, List<PackModel.Part> parts, double[] window)
        {
            return GripFit.fit(grid, grip, parts, window);
        }
    }

    /**
     * {dx, dy, dz} that hangs the grip under the gun's front part, its top on the gun's lowest point there, or null
     * (gripfit.fit). window: {from, to} along x the grip's seat must stay inside (a package's gripRail), or null.
     */
    public static double[] fit(PackModel gun, String cat, String grip, List<PackModel.Part> parts, double[] window)
    {
        return fit(grid(gun, cat), grip, parts, window);
    }

    static double[] fit(Grid m, String grip, List<PackModel.Part> parts, double[] window)
    {
        if (parts.isEmpty())
        {
            return null;
        }
        String cat = m.cat;
        double[] g = gripBox(parts, grip);
        double gx0 = g[0], gx1 = g[1], sx0 = g[6], sx1 = g[7], sz0 = g[8], sz1 = g[9], top = g[10];
        double gz = (sz0 + sz1) / 2;
        double w = gx1 - gx0, hz = (sz1 - sz0) / 2;
        double mid = (gx0 + gx1) / 2;
        double[] run = frontRun(m);
        double start = run[0], end = run[1];
        double front = "pistol".equals(cat) || "revolver".equals(cat) ? 0.0 : 0.4;
        double target = start + w / 2 + front * Math.max(0.0, end - start - w);
        // best key: good spots by distance to the target; else (touch, covered, distance)
        boolean bestGood = false;
        double bestTouch = 0, bestCovered = 0, bestDist = 0, bestTx = 0, bestLowest = 0;
        boolean found = false;
        double tx = start + w / 2;
        while (tx <= end - w / 2 + 1e-6)
        {
            double a = sx0 - mid + tx, b = sx1 - mid + tx;
            if (window == null || (a >= window[0] - RAIL_SLACK && b <= window[1] + RAIL_SLACK))
            {
                double[] r = spot(m, a, b, hz);
                double touch = r[0], covered = r[1], lowest = r[2];
                boolean blocked = r[3] != 0;
                if (!Double.isNaN(lowest))
                {
                    boolean good = !blocked && covered == NX && touch >= NX - 1 && lowest <= m.bore + HANG;
                    double dist = -Math.abs(tx - target);
                    boolean better;
                    if (!found)
                    {
                        better = true;
                    }
                    else if (good != bestGood)
                    {
                        better = good;
                    }
                    else if (good)
                    {
                        better = dist > bestDist;
                    }
                    else
                    {
                        better = touch > bestTouch || touch == bestTouch && (covered > bestCovered
                            || covered == bestCovered && dist > bestDist);
                    }
                    if (better)
                    {
                        found = true;
                        bestGood = good;
                        bestTouch = touch;
                        bestCovered = covered;
                        bestDist = dist;
                        bestTx = tx;
                        bestLowest = lowest;
                    }
                }
            }
            tx += STEP;
        }
        if ((!found || !bestGood) && LIGHTS.contains(grip))
        {
            double[] side = sideFit(m, parts, start, end, target);
            if (side != null)
            {
                return side;
            }
        }
        if (!found)
        {
            return null;
        }
        return new double[] {bestTx - mid, bestLowest - OVERLAP - top, ZC - gz};
    }

    private static double[][] columns(double x0, double x1, double z0, double z1, int nx, int nz)
    {
        double[][] out = new double[nx * nz][];
        int n = 0;
        for (int i = 0; i < nx; i++)
        {
            for (int j = 0; j < nz; j++)
            {
                out[n++] = new double[] {x0 + (x1 - x0) * (0.1 + 0.8 * i / (nx - 1)), z0 + (z1 - z0) * (0.1 + 0.8 * j / (nz - 1))};
            }
        }
        return out;
    }

    /** Nothing of the gun inside the box (vertical rays on a 7 x 3 grid over it, kept 0.05 in from its faces). */
    static boolean clear(List<Shape> body, double x0, double x1, double y0, double y1, double z0, double z1)
    {
        for (double[] c : columns(x0, x1, z0, z1, NX, NZ))
        {
            for (Shape s : body)
            {
                double[] r = ray(s, c[0], c[1]);
                if (r != null && r[0] < y1 - 0.05 && r[1] > y0 + 0.05)
                {
                    return false;
                }
            }
        }
        return true;
    }

    /** A light beside the gun's front on its right (+z), as near the bore's height as fits (gripfit.side_fit). */
    static double[] sideFit(Grid m, List<PackModel.Part> parts, double start, double end, double target)
    {
        List<Shape> body = m.body;
        double bore = m.bore;
        double gx0 = Double.MAX_VALUE, gx1 = -Double.MAX_VALUE, gy0 = Double.MAX_VALUE, gy1 = -Double.MAX_VALUE,
            gz0 = Double.MAX_VALUE, gz1 = -Double.MAX_VALUE;
        for (PackModel.Part p : parts)
        {
            for (double[] q : p.verts())
            {
                gx0 = Math.min(gx0, q[0]);
                gx1 = Math.max(gx1, q[0]);
                gy0 = Math.min(gy0, q[1]);
                gy1 = Math.max(gy1, q[1]);
                gz0 = Math.min(gz0, q[2]);
                gz1 = Math.max(gz1, q[2]);
            }
        }
        double w = gx1 - gx0, h = gy1 - gy0, d = gz1 - gz0;
        boolean found = false;
        double bestA = 0, bestB = 0, bestTx = 0, bestSide = 0, bestCy = 0;
        for (int k = 0; k < 31; k++)
        {
            double cy = bore + (0.1 * ((k + 1) / 2) * (k % 2 != 0 ? 1 : -1));
            double tx = start + w / 2;
            while (tx <= end - w / 2 + 1e-6)
            {
                List<Double> real = new ArrayList<Double>();
                for (int i = 0; i < NX; i++)
                {
                    double x = tx - w / 2 + w * (0.1 + 0.8 * i / (NX - 1));
                    Double side = null;
                    for (int j = 0; j < NZ; j++)
                    {
                        double y = cy - h / 2 + h * (0.1 + 0.8 * j / (NZ - 1));
                        for (Shape s : body)
                        {
                            double[] r = rayZ(s, x, y);
                            if (r != null && (side == null || r[1] > side))
                            {
                                side = r[1];
                            }
                        }
                    }
                    if (side != null)
                    {
                        real.add(side);
                    }
                }
                if (real.size() >= NX - 1)
                {
                    double side = -Double.MAX_VALUE;
                    for (double v : real)
                    {
                        side = Math.max(side, v);
                    }
                    double ka = -Math.abs(cy - bore), kb = -Math.abs(tx - target);
                    int touching = 0;
                    for (double v : real)
                    {
                        touching += side - v <= CONTACT ? 1 : 0;
                    }
                    boolean better = !found || ka > bestA || ka == bestA && kb > bestB;
                    if (touching >= NX - 2 && better
                        && clear(body, tx - w / 2, tx + w / 2, cy - h / 2, cy + h / 2, side + 0.02, side - OVERLAP + d))
                    {
                        found = true;
                        bestA = ka;
                        bestB = kb;
                        bestTx = tx;
                        bestSide = side;
                        bestCy = cy;
                    }
                }
                tx += STEP;
            }
            if (found)
            {
                break;
            }
        }
        if (!found)
        {
            return null;
        }
        return new double[] {bestTx - (gx0 + gx1) / 2, bestCy - (gy0 + gy1) / 2, bestSide - OVERLAP - gz0};
    }
}

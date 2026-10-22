package net.decimation.fixes.gunpack;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * GripFit is tools/guns/gripfit.py's fit in Java: a gun package gets the grip rows the Python tool writes for every
 * other gun. Checked against the shipped grip_offsets.txt (gripfit's own output) for every gun whose model is on the
 * test class path (Decimation's jar in dev/libs, our MAC-10).
 */
class GripFitTest
{
    private static final String[] CATS = {"rifle", "smg", "mg", "shotgun", "pistol", "revolver", "rocket", "crossbow"};

    static PackModel grip(String name) throws Exception
    {
        String text = PackModelTest.resource("/assets/deci/models/attachments/grip/" + name + ".bmodel");
        return text == null ? null : PackModel.parse(text);
    }

    /** {category, model} of a gun on the class path, or null. */
    static Object[] gun(String name) throws Exception
    {
        for (String c : CATS)
        {
            String text = PackModelTest.resource("/assets/deci/models/guns/" + c + "/" + name + ".bmodel");
            if (text != null)
            {
                return new Object[] {c, PackModel.parse(text)};
            }
        }
        return null;
    }

    @Test
    void ourRk1AtDecimationsSpotSeatsOnItsClamp() throws Exception
    {
        PackModel rk1 = grip("rk1");
        assertNotNull(rk1, "our RK-1 model on the class path");
        double[] b = GripFit.gripBox(GripFit.rawGrip(rk1, "rk1"), "rk1");
        // gripfit.py: gx 10.100 .. 11.300, seat x 10.100 .. 11.300, z -0.620 .. 0.320, top -1.3500
        assertArrayEquals(new double[] {10.1, 11.3}, new double[] {b[0], b[1]}, 1e-6);
        assertArrayEquals(new double[] {10.1, 11.3, -0.62, 0.32, -1.35}, new double[] {b[6], b[7], b[8], b[9], b[10]}, 1e-6);
    }

    @Test
    void decimationsForegripSeatsOnItsBodyNotItsTabs() throws Exception
    {
        PackModel fg = grip("foregrip");
        assumeTrue(fg != null, "Decimation's jar on the test class path (dev/libs)");
        List<PackModel.Part> parts = GripFit.rawGrip(fg, "foregrip");
        assertEquals(8, parts.size());      // its stray parent plate left out, the orphan magazine never drawn
        double[] b = GripFit.gripBox(parts, "foregrip");
        // Decimation's float literals leave noise (6.700004): study.py reads the same and prints it rounded
        assertArrayEquals(new double[] {6.7, 7.7, 6.85, 7.55, -0.35, 0.35, -2.6},
                          new double[] {b[0], b[1], b[6], b[7], b[8], b[9], b[10]}, 1e-4);
        double[] l = GripFit.gripBox(GripFit.rawGrip(grip("laser"), "laser"), "laser");
        assertArrayEquals(new double[] {14.45, 15.45, 14.45, 15.45, 0.0, 0.3, -2.75},
                          new double[] {l[0], l[1], l[6], l[7], l[8], l[9], l[10]}, 1e-4);
    }

    @Test
    void everyShippedRowComesOutTheSame() throws Exception
    {
        BufferedReader r = new BufferedReader(new InputStreamReader(
            GripFitTest.class.getResourceAsStream("/assets/deciworldgen/grip_offsets.txt"), "UTF-8"));
        Map<String, GripFit.Gun> guns = new HashMap<String, GripFit.Gun>();
        Map<String, List<PackModel.Part>> grips = new HashMap<String, List<PackModel.Part>>();
        List<String> wrong = new ArrayList<String>();
        int compared = 0, skipped = 0;
        long started = System.nanoTime();
        for (String line; (line = r.readLine()) != null; )
        {
            String[] f = line.trim().split("\\s+");
            if (f.length != 5 || line.startsWith("#"))
            {
                continue;
            }
            if (!guns.containsKey(f[0]))
            {
                Object[] g = gun(f[0]);
                guns.put(f[0], g == null ? null : new GripFit.Gun((PackModel) g[1], (String) g[0]));
            }
            if (!grips.containsKey(f[1]))
            {
                PackModel gm = grip(f[1]);
                grips.put(f[1], gm == null ? null : GripFit.rawGrip(gm, f[1]));
            }
            GripFit.Gun g = guns.get(f[0]);
            List<PackModel.Part> parts = grips.get(f[1]);
            if (g == null || parts == null)
            {
                skipped++;   // a gun generated from Decimation's art only in some checkouts (UMP9, HK416, Mk18)
                continue;
            }
            double[] d = g.fit(f[1], parts, null);
            compared++;
            double[] want = {Double.parseDouble(f[2]), Double.parseDouble(f[3]), Double.parseDouble(f[4])};
            if (d == null || Math.abs(d[0] - want[0]) > 0.0015 || Math.abs(d[1] - want[1]) > 0.0015
                || Math.abs(d[2] - want[2]) > 0.0015)
            {
                wrong.add(line.trim() + " -> " + (d == null ? "none" : GunPackRows.gripLine(f[0], f[1], d)));
            }
        }
        r.close();
        System.out.printf("GripFitTest: %d rows compared, %d skipped, %.0f ms%n", compared, skipped,
                          (System.nanoTime() - started) / 1e6);
        assertTrue(compared >= 10, "rows compared: " + compared);
        assertEquals(new ArrayList<String>(), wrong);
    }

    @Test
    void aRailWindowKeepsTheSeatOnTheRail() throws Exception
    {
        Object[] m4 = gun("m4a4");
        PackModel fg = grip("foregrip");
        assumeTrue(m4 != null && fg != null, "Decimation's jar on the test class path (dev/libs)");
        GripFit.Gun g = new GripFit.Gun((PackModel) m4[1], (String) m4[0]);
        List<PackModel.Part> parts = GripFit.rawGrip(fg, "foregrip");
        // the M4A4's own spot (x 12.25) lies on its handguard rail (8.5 .. 14.7): the same row as without a window
        assertArrayEquals(new double[] {5.05, 1.25, -0.15}, g.fit("foregrip", parts, new double[] {8.5, 14.7}), 0.0015);
        // a rail ending at 10.0: the seat (0.7 long about its middle) must end there
        double[] d = g.fit("foregrip", parts, new double[] {8.5, 10.0});
        assertNotNull(d);
        double seatEnd = 7.2 + d[0] + 0.35;
        assertTrue(seatEnd <= 10.0 + GripFit.RAIL_SLACK + 1e-9, "seat ends at " + seatEnd);
    }
}

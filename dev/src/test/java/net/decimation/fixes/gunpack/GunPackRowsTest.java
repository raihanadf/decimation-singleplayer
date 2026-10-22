package net.decimation.fixes.gunpack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import net.decimation.fixes.GripRails;
import net.decimation.fixes.SightMounts;

/**
 * A package's attachment rows: sights from points.railTop and points.rail as tools/guns/sightfit.py would write them, the
 * mount row, and the shipped tables read with the packages' lines added (GripRails and SightMounts see a package's gun
 * like any other).
 */
class GunPackRowsTest
{
    static final List<String> SIGHTS = Arrays.asList("reddot", "2x", "4x", "8x", "dragunovScope", "eotech558", "ta11acog",
                                                     "specterdr");

    @AfterEach
    void forget()
    {
        GunPackRows.clear();
        GripRails.load();
        SightMounts.load();
    }

    static GunPack rifle(String from, String to) throws Exception
    {
        return GunPack.parse(from == null ? GunPackTest.RIFLE : GunPackTest.with(GunPackTest.RIFLE, from, to), false);
    }

    static List<String> read(InputStream in) throws Exception
    {
        List<String> out = new ArrayList<String>();
        if (in == null)
        {
            return null;
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        for (String l; (l = r.readLine()) != null; )
        {
            out.add(l);
        }
        return out;
    }

    @Test
    void aFlatTopAtTheM4a4sHeightGetsItsRows() throws Exception
    {
        // the M4A4's rail top is -3.6: sightfit wrote "m4a4 <sight> 0.85" for every sight
        List<String> rows = GunPackRows.sightLines(rifle(null, null), SIGHTS);
        assertEquals(8, rows.size());
        assertEquals("mk47 reddot 0.85", rows.get(0));
        assertEquals("mk47 specterdr 0.85", rows.get(7));
        assertEquals("mk47 rail", GunPackRows.mountLine(rifle(null, null)));
    }

    @Test
    void aReceiverAtDecimationsHeightNeedsNoRow() throws Exception
    {
        assertTrue(GunPackRows.sightLines(rifle("-3.6", "-4.45"), SIGHTS).isEmpty());
        assertTrue(GunPackRows.sightLines(rifle("-3.6", "-4.40"), SIGHTS).isEmpty());      // within sightfit's 0.06
        assertEquals("mk47 4x 0.1", GunPackRows.sightLines(rifle("-3.6", "-4.35"), SIGHTS).get(2));
    }

    @Test
    void sightsFollowTheTopRail() throws Exception
    {
        assertEquals(0.0, GunPackRows.sightDx(rifle(null, null)), 1e-9);                       // -1 .. 14.7 holds 0.8 .. 5.5
        // a receiver rail ending at 2.6 (the AS Val's): the scope comes back until it ends there
        assertEquals(-2.9, GunPackRows.sightDx(rifle("[-1.0, 14.7]", "[-6.7, 2.6]")), 1e-9);
        // a rail starting late: forward
        assertEquals(1.2, GunPackRows.sightDx(rifle("[-1.0, 14.7]", "[2.0, 9.0]")), 1e-9);
        // a short rail: the scope's middle over the rail's middle
        assertEquals(-0.65, GunPackRows.sightDx(rifle("[-1.0, 14.7]", "[1.0, 4.0]")), 1e-9);
        // sightShift wins
        assertEquals(-4.0, GunPackRows.sightDx(rifle("\"mount\": \"rail\"", "\"mount\": \"rail\", \"sightShift\": -4")), 1e-9);
        assertEquals("mk47 ta11acog 0.85 -2.9",
                     GunPackRows.sightLines(rifle("[-1.0, 14.7]", "[-6.7, 2.6]"), SIGHTS).get(6));
    }

    @Test
    void noMountNoSightRows() throws Exception
    {
        GunPack p = rifle("\"mount\": \"rail\"", "\"mount\": null");
        assertTrue(GunPackRows.sightLines(p, SIGHTS).isEmpty());
        assertNull(GunPackRows.mountLine(p));
    }

    @Test
    void tablesReadWithThePackagesLines() throws Exception
    {
        assertNull(GunPackRows.with(null, GunPackRows.GRIP_TABLE));
        GunPackRows.add(GunPackRows.GRIP_TABLE, "mk47 foregrip 5.050 1.250 -0.150");
        assertEquals(Arrays.asList("", "mk47 foregrip 5.050 1.250 -0.150"), read(GunPackRows.with(null, GunPackRows.GRIP_TABLE)));
        List<String> both = read(GunPackRows.with(new ByteArrayInputStream("m4a4 rk1 1.6 0 0".getBytes("UTF-8")),
                                                  GunPackRows.GRIP_TABLE));
        assertEquals(Arrays.asList("m4a4 rk1 1.6 0 0", "mk47 foregrip 5.050 1.250 -0.150"), both);
    }

    @Test
    void gripRailsAndSightMountsSeeAPackagesGun() throws Exception
    {
        GripRails.load();
        SightMounts.load();
        assertFalse(GripRails.fits("mk47", "foregrip"));
        assertFalse(SightMounts.fits("mk47", "eotech558"));
        GunPackRows.add(GunPackRows.GRIP_TABLE, GunPackRows.gripLine("mk47", "foregrip", new double[] {5.05, 1.25, -0.15}));
        GunPackRows.add(GunPackRows.MOUNT_TABLE, GunPackRows.mountLine(rifle(null, null)));
        GripRails.load();
        SightMounts.load();
        assertTrue(GripRails.fits("mk47", "foregrip"));
        assertTrue(GripRails.fits("m4a4", "rk1"));               // the shipped rows are all still there
        assertFalse(GripRails.fits("rpk", "foregrip"));
        assertTrue(SightMounts.fits("mk47", "eotech558"));
        assertFalse(SightMounts.fits("mk47", "reddot"));          // the red dot only on a pistol's slide
        assertTrue(SightMounts.fits("akm", "ta11acog"));
    }

    @Test
    void sightNamesFromTheShippedTable() throws Exception
    {
        InputStream shipped = GunPackRowsTest.class.getResourceAsStream("/assets/deciworldgen/" + GunPackRows.SIGHT_TABLE);
        assertEquals(SIGHTS, GunPackRows.sightNames(shipped));
        assertEquals(SIGHTS, GunPackRows.sightNames(null));
    }
}

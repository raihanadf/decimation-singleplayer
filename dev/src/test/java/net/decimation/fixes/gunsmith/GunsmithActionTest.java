package net.decimation.fixes.gunsmith;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** The Gunsmith screen's six slots and the list clicks it sends as container button ids (spec 2026-10-21 section 2). */
class GunsmithActionTest
{
    @Test
    void slotsMatchDecimation()
    {
        // deciIndex = the slot's index in Decimation's WeaponModifyContainer (FilteredSlot rules key on it)
        assertEquals(2, GunSlot.SIGHT.deciIndex);
        assertEquals(1, GunSlot.BARREL.deciIndex);
        assertEquals(4, GunSlot.GRIP.deciIndex);
        assertEquals(3, GunSlot.STOCK.deciIndex);
        assertEquals(5, GunSlot.PAINT.deciIndex);
        assertEquals(6, GunSlot.TRACER.deciIndex);
        assertEquals("sightAttach", GunSlot.SIGHT.nbtKey);
        assertEquals("skin", GunSlot.PAINT.nbtKey);
        assertEquals("tracerColorID", GunSlot.TRACER.nbtKey);
        assertEquals("Grips you carry", GunSlot.GRIP.listTitle());
        assertTrue(GunSlot.PAINT.usedUp());
        assertTrue(GunSlot.TRACER.usedUp());
        assertFalse(GunSlot.SIGHT.usedUp());
        assertFalse(GunSlot.STOCK.usedUp());
    }

    @Test
    void actionsRoundTrip()
    {
        GunsmithAction a = GunsmithAction.decode(GunsmithAction.fit(GunSlot.GRIP, 35));
        assertNotNull(a);
        assertFalse(a.takeOff);
        assertEquals(GunSlot.GRIP, a.slot);
        assertEquals(35, a.invIndex);
        GunsmithAction b = GunsmithAction.decode(GunsmithAction.fit(GunSlot.SIGHT, 0));
        assertEquals(GunSlot.SIGHT, b.slot);
        assertEquals(0, b.invIndex);
        GunsmithAction c = GunsmithAction.decode(GunsmithAction.takeOff(GunSlot.STOCK));
        assertTrue(c.takeOff);
        assertEquals(GunSlot.STOCK, c.slot);
    }

    @Test
    void badIdsDecodeToNull()
    {
        for (int id : new int[] {-1, 0, 999, 1036, 1600, 1999, 2004, 2005, 2006, 3000})
        {
            assertNull(GunsmithAction.decode(id), "id " + id);
        }
    }

    @Test
    void textsHaveNoDashes()
    {
        for (GunSlot s : GunSlot.values())
        {
            assertFalse(s.listTitle().matches(".*[-\\u2013\\u2014].*"), s.listTitle());
            assertFalse(s.label.matches(".*[-\\u2013\\u2014].*"), s.label);
        }
    }
}

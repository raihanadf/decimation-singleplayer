package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class ListEntryTest
{
    private static String times(char c, int n)
    {
        return new String(new char[n]).replace('\0', c);
    }

    @Test
    void parsesTheListAnswer()
    {
        String json = "[{\"room\":\"abcdefghij012345\",\"name\":\"Ucil world\",\"host\":\"Ucil\",\"online\":false,\"players\":2,"
                    + "\"max\":4,\"mods\":[[\"deci\",\"1.21.10f\"]],\"version\":\"0.64.9\",\"route\":\"relay\",\"since\":5}]";
        List<ListEntry> l = ListEntry.parse(json);
        assertEquals(1, l.size());
        assertEquals("Ucil world", l.get(0).name);
        assertEquals(2, l.get(0).players);
        assertEquals("deci", l.get(0).mods.get(0).get(0));
        assertFalse(l.get(0).online);
        assertNull(l.get(0).modSet().firstMissing(l.get(0).modSet()));
    }

    @Test
    void brokenAnswerIsAnEmptyList()
    {
        assertTrue(ListEntry.parse("<html>").isEmpty());
    }

    @Test
    void nameCleaned()
    {
        assertEquals("Red world", ListEntry.cleanName("  §cRed §lworld  "));
        assertEquals(32, ListEntry.cleanName(times('x', 40)).length());
        assertNull(ListEntry.cleanName("  §r "));
    }
}

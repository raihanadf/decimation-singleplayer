package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CommunityTextTest
{
    private static List<ListEntry> worlds(int... joiners)
    {
        List<ListEntry> l = new ArrayList<ListEntry>();
        for (int j : joiners)
        {
            ListEntry e = new ListEntry();
            e.players = j;
            l.add(e);
        }
        return l;
    }

    @Test
    void headlineCountsHostsAndJoinersWithoutWorlds()
    {
        String h = CommunityText.headline(worlds(1, 0));
        assertTrue(h.endsWith("Players Online: §a3"), h); // two hosts and one joiner
        assertFalse(h.contains("world"), h);
    }

    @Test
    void emptyListShowsAPlaceholder()
    {
        String[] lines = CommunityText.empty(worlds());
        assertTrue(lines.length >= 2);
        assertTrue(lines[0].contains("No worlds"), lines[0]);
        assertTrue(String.join(" ", lines).contains("Host Online"));
    }

    @Test
    void aListedWorldHasNoPlaceholder()
    {
        assertEquals(0, CommunityText.empty(worlds(0)).length);
    }
}

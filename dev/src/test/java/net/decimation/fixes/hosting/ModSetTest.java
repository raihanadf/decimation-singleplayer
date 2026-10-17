package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ModSetTest
{
    private static ModSet set(String... idVersion)
    {
        List<String[]> l = new ArrayList<String[]>();
        for (int i = 0; i < idVersion.length; i += 2)
        {
            l.add(new String[] {idVersion[i], idVersion[i + 1]});
        }
        return ModSet.of(l);
    }

    @Test
    void sameSetMissesNothing()
    {
        assertNull(set("deci", "1.21.10f", "deciworldgen", "0.64.9").firstMissing(set("deciworldgen", "0.64.9", "deci", "1.21.10f")));
    }

    @Test
    void namesTheFirstMissingMod()
    {
        assertEquals("gvc 0.6.1", set("deci", "1.21.10f").firstMissing(set("deci", "1.21.10f", "gvc", "0.6.1")));
    }

    @Test
    void anotherVersionCountsAsMissing()
    {
        assertEquals("deciworldgen 0.64.9", set("deciworldgen", "0.64.7").firstMissing(set("deciworldgen", "0.64.9")));
    }

    @Test
    void pairsKeepOrder()
    {
        assertEquals("gvc", set("deci", "1", "gvc", "2").pairs().get(1)[0]);
    }
}

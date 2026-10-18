package net.decimation.fixes.hosting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Which Cloudflare data centers (IATA codes, the relay's x-colo) count as near a room's region. Only the Asian regions
 * have a list: free Worker traffic from Indonesia was measured landing in Singapore, Frankfurt, London and Paris
 * (docs/player_hosting.md 14), and a socket that lands in Europe adds about 200 ms per trip. Elsewhere nothing retries.
 */
final class Colos
{
    private static final Map<String, Set<String>> NEAR = new HashMap<String, Set<String>>();

    static
    {
        Set<String> se = set("SIN", "CGK", "KUL", "JHB", "BKK", "CNX", "MNL", "CEB", "SGN", "HAN", "DAD", "PNH", "RGN", "HKG", "DPS",
                             "SUB");
        Set<String> ne = set("NRT", "KIX", "FUK", "OKA", "ICN", "TPE", "KHH", "HKG", "MFM", "ULN");
        Set<String> all = new HashSet<String>(se);
        all.addAll(ne);
        all.addAll(set("BOM", "DEL", "MAA", "BLR", "HYD", "CCU", "AMD", "CMB", "DAC", "KTM", "ISB", "KHI", "LHE"));
        NEAR.put("apac-se", se);
        NEAR.put("apac-ne", ne);
        NEAR.put("apac", all);
    }

    private Colos()
    {
    }

    private static Set<String> set(String... s)
    {
        return new HashSet<String>(Arrays.asList(s));
    }

    /** True when a socket in data center `colo` suits a room in `region`; true when either is unknown. */
    static boolean near(String region, String colo)
    {
        Set<String> s = region == null ? null : NEAR.get(region);
        return s == null || colo == null || s.contains(colo);
    }
}

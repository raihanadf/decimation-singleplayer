package net.decimation.worldgen.city;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.decimation.worldgen.Sectors;
import net.decimation.worldgen.StructureGenerator;

/**
 * Names of cities and their districts (docs/zones.md "Place names"; user choice 12 Oktober 2026:
 * "City: District - number", one name per district, shown by Decimation's own turf banner). A city
 * is a group of touching city regions (Sectors, 16 x 16 chunks, 4 neighbours); its name comes from
 * the seed and its first region. A district is the 2 x 2 cell square (8 x 8 chunks, 128 blocks) that
 * shares one style; its label comes from the style, its number is its place in the city (rows north
 * to south, west to east). Everything follows from the seed: no storage.
 */
public final class CityNames
{
    private static final String[] FIRST = {"Ash", "Brook", "Cedar", "Clear", "Elm", "Fair", "Glen", "Granite", "Green",
        "Hollow", "Iron", "Lake", "Maple", "Mill", "North", "Oak", "Pine", "Red", "River", "Rock", "Silver", "Spring",
        "Stone", "West", "Willow", "Wolf", "Black", "Bright", "Cold", "East", "Fox", "Grey", "Harbor", "High", "Long",
        "Salt"};
    private static final String[] LAST = {"field", "ford", "haven", "port", "ridge", "ville", "wood", "burg", "dale",
        "ton", "view", "water", "crest", "gate", "mouth", "brook"};
    private static final int MAX_REGIONS = 64;

    private static final Map<Long, List<long[]>> CITIES = new HashMap<Long, List<long[]>>();
    private static long citiesLayout;

    private CityNames()
    {
    }

    private static boolean cityRegion(long seed, int rx, int rz)
    {
        return Sectors.regionSector(seed, rx, rz) == StructureGenerator.CITY;
    }

    private static long key(int x, int z)
    {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    /** The regions of the city holding region (rx, rz), sorted north to south, west to east; null if not a city. */
    static synchronized List<long[]> city(long seed, int rx, int rz)
    {
        if (!cityRegion(seed, rx, rz))
        {
            return null;
        }
        long layout = seed ^ net.decimation.worldgen.CityLayout.epoch() * 0x9E3779B97F4A7C15L;
        if (layout != citiesLayout)
        {
            CITIES.clear();                                // another world or layout: the groups differ
            citiesLayout = layout;
        }
        List<long[]> c = CITIES.get(key(rx, rz));
        if (c != null)
        {
            return c;
        }
        c = new ArrayList<long[]>();
        ArrayDeque<int[]> q = new ArrayDeque<int[]>();
        java.util.Set<Long> seen = new java.util.HashSet<Long>();
        q.add(new int[] {rx, rz});
        seen.add(key(rx, rz));
        while (!q.isEmpty() && c.size() < MAX_REGIONS)
        {
            int[] r = q.poll();
            c.add(new long[] {r[0], r[1]});
            int[][] side = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] s : side)
            {
                int nx = r[0] + s[0], nz = r[1] + s[1];
                if (cityRegion(seed, nx, nz) && seen.add(key(nx, nz)))
                {
                    q.add(new int[] {nx, nz});
                }
            }
        }
        Collections.sort(c, (a, b) -> a[1] != b[1] ? Long.compare(a[1], b[1]) : Long.compare(a[0], b[0]));
        for (long[] r : c)
        {
            CITIES.put(key((int) r[0], (int) r[1]), c);
        }
        if (CITIES.size() > 4096)
        {
            CITIES.clear();
        }
        return c;
    }

    /** The city's name: from its first region; big cities (4+ regions) are "... City". */
    static String cityName(long seed, List<long[]> city)
    {
        long[] first = city.get(0);
        long h = LcCity.hl(seed ^ 0x4349545949L, (int) first[0], (int) first[1]);
        String n = FIRST[(int) (h % FIRST.length)] + LAST[(int) ((h >>> 20) % LAST.length)];
        return city.size() >= 4 ? n + " City" : n;
    }

    /**
     * A wasteland city standing apart (Wasteland.apart): its wasteland districts joined side by side are
     * one city named "Fort <name>" after its first district, numbered north to south, west to east.
     */
    static synchronized String[] wasteland(long seed, int dx, int dz)
    {
        List<long[]> c = new ArrayList<long[]>();
        ArrayDeque<int[]> q = new ArrayDeque<int[]>();
        java.util.Set<Long> seen = new java.util.HashSet<Long>();
        q.add(new int[] {dx, dz});
        seen.add(key(dx, dz));
        while (!q.isEmpty() && c.size() < MAX_REGIONS)
        {
            int[] r = q.poll();
            c.add(new long[] {r[0], r[1]});
            for (int[] s : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}})
            {
                int nx = r[0] + s[0], nz = r[1] + s[1];
                if (Wasteland.deadCell(seed, nx * 2, nz * 2) && seen.add(key(nx, nz)))
                {
                    q.add(new int[] {nx, nz});
                }
            }
        }
        Collections.sort(c, (a, b) -> a[1] != b[1] ? Long.compare(a[1], b[1]) : Long.compare(a[0], b[0]));
        int number = 1;
        for (long[] r : c)
        {
            if (r[0] == dx && r[1] == dz)
            {
                break;
            }
            number++;
        }
        long h = LcCity.hl(seed ^ 0x464F5254L, (int) c.get(0)[0], (int) c.get(0)[1]);
        String n = "Fort " + FIRST[(int) (h % FIRST.length)] + LAST[(int) ((h >>> 20) % LAST.length)];
        return new String[] {n + ": Wasteland - " + number, "Military"};
    }

    /** What a district of this style is called. */
    static String label(String style)
    {
        if (style == null)
        {
            return "District";
        }
        if (style.endsWith(":deadzone")) return "Wasteland";
        if (style.endsWith(":highrise_office")) return "Downtown";
        if (style.endsWith(":highrise_hotel")) return "Hotel District";
        if (style.endsWith(":highrise_residential")) return "Uptown";
        if (style.endsWith(":retail_district")) return "Market District";
        if (style.endsWith(":suburb_residential")) return "Suburbs";
        if (style.endsWith(":standardcity")) return "Old Town";
        if (style.endsWith(":residential")) return "Residential";
        return "District";
    }

    /**
     * The district at district coordinates (dx, dz) (8 x 8 chunk squares): {name, owner}, or null when
     * it is not a city. Name "Ashford: Downtown - 7"; owner "Military" in the wasteland, else "unknown".
     */
    public static String[] district(long seed, int dx, int dz)
    {
        if (Wasteland.own())
        {
            // the wasteland stands apart (v0.56): the ring around it is open land, the wasteland its own city
            boolean any = false;
            for (int i = 0; i < 2 && !any; i++)
            {
                for (int j = 0; j < 2 && !any; j++)
                {
                    any = LcCity.isCity(seed, dx * 2 + i, dz * 2 + j);
                }
            }
            if (!any)
            {
                return null;
            }
            if (Wasteland.deadCell(seed, dx * 2, dz * 2))
            {
                return wasteland(seed, dx, dz);
            }
        }
        int rx = Math.floorDiv(dx, 2), rz = Math.floorDiv(dz, 2);
        List<long[]> city = city(seed, rx, rz);
        if (city == null)
        {
            return null;
        }
        int index = 0;
        for (long[] r : city)
        {
            if (r[0] == rx && r[1] == rz)
            {
                break;
            }
            index++;
        }
        // 4 districts per region; rows then columns inside the region
        int number = index * 4 + Math.floorMod(dz, 2) * 2 + Math.floorMod(dx, 2) + 1;
        String style = LcCity.enabled() ? LcCity.style(seed, dx * 2, dz * 2) : null;
        String label = label(style);
        return new String[] {cityName(seed, city) + ": " + label + " - " + number,
                             style != null && style.endsWith(":deadzone") ? "Military" : "unknown"};
    }
}

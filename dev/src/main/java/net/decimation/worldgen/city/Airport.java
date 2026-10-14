package net.decimation.worldgen.city;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.decimation.worldgen.CityLayout;
import net.decimation.worldgen.Sectors;
import net.decimation.worldgen.StructureGenerator;

import static net.decimation.worldgen.city.LcCity.*;

/**
 * Where the military airports go (docs/airport.md; user, 14 Oktober 2026: half a wasteland sector, any city with
 * a wasteland can get one). One airport per city at most (CityLayout plan): 1 city in 3, in the city's wasteland
 * sector picked by hash, in the half of that sector that faces away from the rest of the city (its outer side on
 * land or a military sector scores highest). The half is one block of 2 x 4 cells (LcCity.block kind AIRPORT):
 * its first chunk column and row stay streets, the inside is the airport (military/AirportPlan).
 * New worlds only (StructureData KEY, CityLayoutEvents).
 */
public final class Airport
{
    public static final String KEY = "!airports";
    /** LcCity.block kind of an airport block. */
    public static final int KIND = 3;
    /** Cells across (the narrow side) and along (the runway's side). */
    public static final int ACROSS = 2, ALONG = 4;

    private static volatile boolean on;
    private static final Map<Long, int[]> SITES = new ConcurrentHashMap<Long, int[]>();
    private static volatile long sitesStamp;

    private Airport()
    {
    }

    public static boolean on()
    {
        return on;
    }

    /** Switches airports (world load, dev tests); cached city plans are dropped. */
    public static void set(boolean v)
    {
        if (on != v)
        {
            on = v;
            SITES.clear();
            LcCity.layoutChanged();
        }
    }

    /**
     * The airport of the wasteland sector (rx, rz), or null: {origin cell x, origin cell z, cells along x, cells
     * along z} (2 x 4: runway along z; 4 x 2: along x).
     */
    public static int[] site(long seed, int rx, int rz)
    {
        if (!on || !CityLayout.on() || !Wasteland.sectors() || !Wasteland.sector(seed, rx, rz))
        {
            return null;
        }
        long stamp = seed ^ CityLayout.epoch() * 0x9E3779B97F4A7C15L;
        if (stamp != sitesStamp || SITES.size() > 4096)
        {
            SITES.clear();
            sitesStamp = stamp;
        }
        long key = CityLayout.key(rx, rz);
        int[] s = SITES.get(key);
        if (s == null)
        {
            s = choose(seed, rx, rz);
            SITES.put(key, s);
        }
        return s.length == 0 ? null : s;
    }

    private static int[] choose(long seed, int rx, int rz)
    {
        // the city's airport sector: its wasteland sector with the lowest hash, 1 city in 3
        CityLayout.Plan p = CityLayout.planAt(seed, rx, rz);
        long pick = Long.MAX_VALUE, pickKey = 0;
        for (long k : p.waste)
        {
            long h = hl(seed ^ 0x41495250L, (int) (k >> 32), (int) k);
            if (h < pick)
            {
                pick = h;
                pickKey = k;
            }
        }
        if (pick == Long.MAX_VALUE || pickKey != CityLayout.key(rx, rz) || hl(seed ^ 0x41495243L, rx, rz) % 3 != 0)
        {
            return new int[0];
        }
        // the half facing away from the city: west, east, north, south by its outer neighbour sector
        int[][] out = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int per = Sectors.REGION / CELL;
        int best = -1, bestScore = -1;
        for (int h = 0; h < 4; h++)
        {
            int nx = rx + out[h][0], nz = rz + out[h][1];
            int sec = Sectors.regionSector(seed, nx, nz);
            int score = sec != StructureGenerator.CITY ? 3 : Wasteland.sector(seed, nx, nz) ? 2 : 0;
            score = score * 4 + (int) (hl(seed ^ 0x48414C46L ^ h, rx, rz) % 4);
            if (score > bestScore && !rivers(seed, rx * per, rz * per, h))
            {
                best = h;
                bestScore = score;
            }
        }
        if (best < 0)
        {
            return new int[0];
        }
        int ox = rx * per + (best == 1 ? per - ACROSS : 0), oz = rz * per + (best == 3 ? per - ACROSS : 0);
        boolean alongZ = best < 2;
        int[] site = {ox, oz, alongZ ? ACROSS : ALONG, alongZ ? ALONG : ACROSS};
        cpw.mods.fml.common.FMLLog.info("[deciworldgen] lc airport at %d,%d (%d x %d cells, runway along %s)",
                                        ox * CELL * 16, oz * CELL * 16, site[2], site[3], alongZ ? "z" : "x");
        return site;
    }

    /** Any river cell in half h (0 west, 1 east, 2 north, 3 south) of the sector whose first cell is (cx, cz). */
    private static boolean rivers(long seed, int cx, int cz, int h)
    {
        int per = Sectors.REGION / CELL;
        for (int i = 0; i < per; i++)
        {
            for (int j = 0; j < per; j++)
            {
                boolean in = h == 0 ? i < ACROSS : h == 1 ? i >= per - ACROSS : h == 2 ? j < ACROSS : j >= per - ACROSS;
                if (in && CityRivers.cell(seed, cx + i, cz + j))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /** The airport block holding cell (cellX, cellZ): {origin x, origin z, KIND, cells x, cells z}, or null. */
    static int[] block(long seed, int cellX, int cellZ)
    {
        if (!on)
        {
            return null;
        }
        int per = Sectors.REGION / CELL;
        int[] s = site(seed, Math.floorDiv(cellX, per), Math.floorDiv(cellZ, per));
        if (s == null || cellX < s[0] || cellZ < s[1] || cellX >= s[0] + s[2] || cellZ >= s[1] + s[3])
        {
            return null;
        }
        return new int[] {s[0], s[1], KIND, s[2], s[3]};
    }
}

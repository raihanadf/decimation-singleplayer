package net.decimation.worldgen.terrain;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import net.decimation.worldgen.CityLayout;
import net.decimation.worldgen.Sectors;
import net.decimation.worldgen.StructureGenerator;
import net.minecraft.world.gen.NoiseGeneratorSimplex;

/**
 * Biome at any block of a Decimation world, from the seed alone.
 *
 * - City sector: Decimated City (flat), military sector: Irradiated Military
 *   Zone, both exactly on the sector squares the structure generator uses.
 * - Civilian sector: Decimated Suburbs, edge warped by noise (up to 56
 *   blocks) so it does not run along the sector square.
 * - Wilderness: dead (Decimated Plains, Burnt Forest) within about 100
 *   blocks of a city or military sector, overgrown (plains, forest, hills)
 *   further out. The border is warped by noise so it is not a straight line.
 * - Rivers: a winding noise contour, kept 32+ blocks away from city and
 *   military sectors. No oceans. With city rivers on (new worlds since
 *   v0.45.0, city.CityRivers) the contour also runs through city sectors
 *   (canals and bridges there, city.RiverPlan) and keeps its clearance
 *   from military sectors only.
 */
public final class BiomeMap
{
    private static final int RIVER_CLEARANCE = 32;
    private static final double RIVER_WIDTH = 0.022;
    /** Wilderness closer than this (plus noise) to a city / military sector is dead land. */
    private static final int DEAD_RADIUS = 100;
    private static final int SUBURB_WARP = 56;

    private final long seed;
    private final NoiseGeneratorSimplex warpX, warpZ, variety, river;
    private final ConcurrentHashMap<Long, Integer> sectors = new ConcurrentHashMap<Long, Integer>();
    private volatile int sectorsEpoch = CityLayout.epoch();
    /** The map of each seed a Decimation world was created with (city code asks for river chunks). */
    private static final ConcurrentHashMap<Long, BiomeMap> MAPS = new ConcurrentHashMap<Long, BiomeMap>();
    private volatile boolean cityRivers;

    /** The map of a Decimation world's seed, or null (no Decimation world with that seed this session). */
    public static BiomeMap of(long seed)
    {
        return MAPS.get(seed);
    }

    public boolean cityRivers()
    {
        return cityRivers;
    }

    public void setCityRivers(boolean on)
    {
        cityRivers = on;
    }

    /** True on the river contour (whatever the sector). */
    public boolean riverAt(int x, int z)
    {
        return isRiver(x, z);
    }

    public BiomeMap(long seed)
    {
        MAPS.put(seed, this);
        this.seed = seed;
        Random r = new Random(seed ^ 0x7E44A1BL);
        warpX = new NoiseGeneratorSimplex(r);
        warpZ = new NoiseGeneratorSimplex(r);
        variety = new NoiseGeneratorSimplex(r);
        river = new NoiseGeneratorSimplex(r);
    }

    public DeciBiome biomeAt(int x, int z)
    {
        int rx = Math.floorDiv(x, Sectors.REGION_BLOCKS), rz = Math.floorDiv(z, Sectors.REGION_BLOCKS);
        int s = sector(rx, rz);
        if (s == StructureGenerator.CITY)
        {
            return cityRivers && isRiver(x, z) ? DecimationBiomes.river : DecimationBiomes.urban;
        }
        if (s == StructureGenerator.MIL)
        {
            return DecimationBiomes.military;
        }
        double dead = deadDistance(x, z, rx, rz, false);
        if ((cityRivers ? deadDistance(x, z, rx, rz, true) : dead) > RIVER_CLEARANCE && isRiver(x, z))
        {
            return DecimationBiomes.river;
        }
        // suburbs follow the civilian sector through warped coordinates, so
        // their edge wanders instead of running along the sector square
        double wx = warpX.func_151605_a(x / 180.0, z / 180.0);
        double wz = warpZ.func_151605_a(x / 180.0, z / 180.0);
        int sx = x + (int) (SUBURB_WARP * wx), sz = z + (int) (SUBURB_WARP * wz);
        if (sector(Math.floorDiv(sx, Sectors.REGION_BLOCKS), Math.floorDiv(sz, Sectors.REGION_BLOCKS))
            == StructureGenerator.CIV)
        {
            return DecimationBiomes.suburb;
        }
        double v = variety.func_151605_a(x / 360.0, z / 360.0);
        if (dead < DEAD_RADIUS + 48 * wx)
        {
            return v > 0.35 ? DecimationBiomes.burntForest : DecimationBiomes.wasteland;
        }
        if (v > 0.38)
        {
            return DecimationBiomes.hills;
        }
        return v < -0.2 ? DecimationBiomes.forest : DecimationBiomes.plains;
    }

    private boolean isRiver(int x, int z)
    {
        double wx = x + 40 * warpX.func_151605_a(x / 120.0, z / 120.0);
        double wz = z + 40 * warpZ.func_151605_a(x / 120.0, z / 120.0);
        return Math.abs(river.func_151605_a(wx / 520.0, wz / 520.0)) < RIVER_WIDTH;
    }

    /** Distance in blocks to the nearest city or military sector (searched 2 regions out), or military only. */
    private double deadDistance(int x, int z, int rx, int rz, boolean militaryOnly)
    {
        double best = Double.MAX_VALUE;
        for (int i = -2; i <= 2; i++)
        {
            for (int k = -2; k <= 2; k++)
            {
                int s = sector(rx + i, rz + k);
                if (s != StructureGenerator.MIL && (militaryOnly || s != StructureGenerator.CITY))
                {
                    continue;
                }
                int x0 = (rx + i) * Sectors.REGION_BLOCKS, z0 = (rz + k) * Sectors.REGION_BLOCKS;
                int dx = Math.max(0, Math.max(x0 - x, x - (x0 + Sectors.REGION_BLOCKS - 1)));
                int dz = Math.max(0, Math.max(z0 - z, z - (z0 + Sectors.REGION_BLOCKS - 1)));
                best = Math.min(best, Math.sqrt((double) dx * dx + (double) dz * dz));
            }
        }
        return best;
    }

    private int sector(int rx, int rz)
    {
        long key = ((long) rx << 32) ^ (rz & 0xFFFFFFFFL);
        if (sectorsEpoch != CityLayout.epoch())
        {
            sectors.clear();                               // city layout switched (world load)
            sectorsEpoch = CityLayout.epoch();
        }
        Integer s = sectors.get(key);
        if (s == null)
        {
            if (sectors.size() > 4096)
            {
                sectors.clear();
            }
            s = Sectors.regionSector(seed, rx, rz);
            sectors.put(key, s);
        }
        return s;
    }
}

package net.decimation.worldgen;

import java.util.Random;

/**
 * The world's sector map: REGION x REGION chunk squares, each wilderness,
 * civilian, city or military, decided from the world seed alone. Shared by
 * the structure generator and the Decimation world type's biome map (city
 * sectors get the flat urban biome), so both always agree. Worlds since v0.60.0 take their cities
 * from CityLayout (cities of 4 to 64 districts).
 */
public final class Sectors
{
    /** Sector size in chunks (256 blocks). */
    public static final int REGION = 16;
    public static final int REGION_BLOCKS = REGION * 16;

    private Sectors()
    {
    }

    public static int sector(long seed, int chunkX, int chunkZ)
    {
        return regionSector(seed, Math.floorDiv(chunkX, REGION), Math.floorDiv(chunkZ, REGION));
    }

    public static int regionSector(long seed, int regionX, int regionZ)
    {
        Random r = new Random(seed ^ (regionX * 875949887L + regionZ * 656887297L));
        float roll = r.nextFloat();
        if (CityLayout.on())
        {
            // city sizes (worlds since v0.60.0): cities come only from CityLayout; the rest keeps the old
            // shares without the city one (wild 40, civilian 25, military 20 of 85)
            int s = CityLayout.sector(seed, regionX, regionZ);
            if (s >= 0) return s;
            roll *= 0.85f;
            if (roll < 0.40f) return StructureGenerator.WILD;
            if (roll < 0.65f) return StructureGenerator.CIV;
            return StructureGenerator.MIL;
        }
        if (roll < 0.40f) return StructureGenerator.WILD;
        if (roll < 0.65f) return StructureGenerator.CIV;
        if (roll < 0.80f) return StructureGenerator.CITY;
        return StructureGenerator.MIL;
    }
}

package net.decimation.worldgen.city;

import java.util.concurrent.ConcurrentHashMap;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.decimation.worldgen.DecimationWorldGen;
import net.decimation.worldgen.StructureData;
import net.decimation.worldgen.terrain.BiomeMap;
import net.decimation.worldgen.terrain.DecimationWorldType;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

/**
 * Rivers through cities (docs/city_engine.md "Rivers and bridges"): with it
 * on, the Decimation world type's river contour (terrain.BiomeMap) also
 * crosses city sectors. A city chunk the contour touches is a river chunk: a canal (city.RiverPlan), its street chunks bridges, its
 * cells city level 0 (Lost Cities builds bridges at level 0 only).
 *
 * On for worlds created since v0.45.0 only (stored per world in
 * StructureData as "!cityRivers"): an older world would get canals in the
 * chunks it generates from now on, beside city chunks built without them.
 * -Ddeciworldgen.cityrivers=true / false overrides it for a session.
 */
public final class CityRivers
{
    private static final String KEY = "!cityRivers";
    private static final ConcurrentHashMap<Long, Boolean> CHUNKS = new ConcurrentHashMap<Long, Boolean>();
    private static final ConcurrentHashMap<Long, Boolean> CELLS = new ConcurrentHashMap<Long, Boolean>();

    /** City river chunk (a city chunk whose middle is on the river contour), when city rivers are on. */
    public static boolean chunk(long seed, int cx, int cz)
    {
        BiomeMap m = BiomeMap.of(seed);
        if (m == null || !m.cityRivers())
        {
            return false;
        }
        long key = ((long) cx << 32) ^ (cz & 0xFFFFFFFFL) ^ seed * 31;
        Boolean b = CHUNKS.get(key);
        if (b == null)
        {
            b = LcCity.isCity(seed, Math.floorDiv(cx, LcCity.CELL), Math.floorDiv(cz, LcCity.CELL)) && touches(m, cx, cz);
            if (CHUNKS.size() > 65536)
            {
                CHUNKS.clear();
            }
            CHUNKS.put(key, b);
        }
        return b;
    }

    /** The river touches the chunk, in or out of a city (the natural river next to a canal). */
    static boolean water(long seed, int cx, int cz)
    {
        BiomeMap m = BiomeMap.of(seed);
        return m != null && m.cityRivers() && touches(m, cx, cz);
    }

    /**
     * The contour crosses one of 3 x 3 points spread over the chunk. Only the
     * middle point made a diagonal river a chain of chunks touching at their
     * corners: ponds, not a river (seed 1 server test, 11 Oktober 2026).
     */
    private static boolean touches(BiomeMap m, int cx, int cz)
    {
        int[] at = {2, 8, 13};
        for (int a : at)
        {
            for (int b : at)
            {
                if (m.riverAt((cx << 4) + a, (cz << 4) + b))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /** True when any chunk of a city cell is a river chunk. */
    public static boolean cell(long seed, int cellX, int cellZ)
    {
        BiomeMap m = BiomeMap.of(seed);
        if (m == null || !m.cityRivers())
        {
            return false;
        }
        long key = ((long) cellX << 32) ^ (cellZ & 0xFFFFFFFFL) ^ seed * 31;
        Boolean b = CELLS.get(key);
        if (b == null)
        {
            b = false;
            for (int i = 0; i < LcCity.CELL && !b; i++)
            {
                for (int k = 0; k < LcCity.CELL && !b; k++)
                {
                    b = chunk(seed, cellX * LcCity.CELL + i, cellZ * LcCity.CELL + k);
                }
            }
            if (CELLS.size() > 65536)
            {
                CELLS.clear();
            }
            CELLS.put(key, b);
        }
        return b;
    }

    /**
     * Every bridge of a city region (a street chunk on the river, not a
     * crossing), by chunk rows: {chunk x, chunk z, 0 along x / 1 along z,
     * street level}.
     */
    public static java.util.List<int[]> bridges(long seed, int rx, int rz)
    {
        java.util.List<int[]> out = new java.util.ArrayList<int[]>();
        int r = net.decimation.worldgen.Sectors.REGION;
        for (int chz = rz * r; chz < (rz + 1) * r; chz++)
        {
            for (int chx = rx * r; chx < (rx + 1) * r; chx++)
            {
                if (!chunk(seed, chx, chz) || !LcCity.isStreetChunk(seed, chx, chz))
                {
                    continue;
                }
                int cellX = Math.floorDiv(chx, LcCity.CELL), cellZ = Math.floorDiv(chz, LcCity.CELL);
                int[] b = LcCity.block(seed, cellX, cellZ);
                int lx = chx - b[0] * LcCity.CELL, lz = chz - b[1] * LcCity.CELL;
                if (lx == 0 && lz == 0)
                {
                    continue;
                }
                out.add(new int[] {chx, chz, lx == 0 ? 1 : 0, LcCity.ground(seed, cellX, cellZ)});
            }
        }
        return out;
    }

    /** Switches city rivers for a world this session (dev tests); not stored. */
    public static void force(World world, boolean on)
    {
        BiomeMap m = BiomeMap.of(world.getSeed());
        if (m != null && m.cityRivers() != on)
        {
            m.setCityRivers(on);
            CHUNKS.clear();
            CELLS.clear();
            LcCity.clearPlans();
        }
    }

    /** True when the world stored city rivers on. */
    public static boolean stored(World world)
    {
        Integer v = StructureData.get(world).baseY(KEY);
        return v != null && v == 1;
    }

    /**
     * World load (overworld, server, Decimation world type): a world that has
     * never ticked is new and gets city rivers; the choice is stored so it
     * stays the same on every later load.
     */
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        World world = event.world;
        if (world.isRemote || world.provider == null || world.provider.dimensionId != 0
            || world.getWorldInfo().getTerrainType() != DecimationWorldType.INSTANCE)
        {
            return;
        }
        BiomeMap m = BiomeMap.of(world.getSeed());
        if (m == null)
        {
            return;
        }
        StructureData data = StructureData.get(world);
        Integer v = data.baseY(KEY);
        if (v == null)
        {
            v = world.getTotalWorldTime() == 0 ? 1 : 0;
            data.setBaseY(KEY, v);
        }
        String force = System.getProperty("deciworldgen.cityrivers");
        boolean on = force != null ? "true".equals(force) : v == 1;
        m.setCityRivers(on);
        CHUNKS.clear();
        CELLS.clear();
        LcCity.clearPlans();
        FMLLog.info("[%s] rivers through cities %s", DecimationWorldGen.MODID, on ? "on" : "off");
    }
}

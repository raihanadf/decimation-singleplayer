package net.decimation.worldgen.city;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.decimation.worldgen.DecimationWorldGen;
import net.decimation.worldgen.StructureData;
import net.decimation.worldgen.ZoneKind;
import net.decimation.worldgen.ZoneStore;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

import static net.decimation.worldgen.city.LcCity.*;

/**
 * The wasteland district as the city's military area (docs/wasteland.md; user, 12 Oktober 2026:
 * "wasteland district = military area"). The district itself (legacy deadzone style within
 * DEADZONE_RANGE cells of a military sector) comes from LcCity.style; this class holds what makes it
 * military beyond the dressing (StreetDress, LotDress, BuildingDress read StreetDress.WASTELAND):
 * - one MILITARY zone over the whole district (2 x 2 cells, every height), stored once per world;
 * - DeceasedCraft's military buildings (base, camp, bunker) weigh more in the district's building
 *   pick (factor), for worlds created since v0.54 only (a changed pick in an older world
 *   would cut buildings at the edge of its generated chunks, as city rivers and refuges);
 * - the enemy military's spawner and the heavier bandit tiers count the district as a military area
 *   (fixes/NpcLoadouts.military), groups spawning on its streets, never on roofs (streetSpot).
 */
public final class Wasteland
{
    /**
     * DeceasedCraft's military buildings are 11 of 47 weight points in the district. Raised per building:
     * the base (3 x 2 chunks, the most military of them, fits only where the block is still free) x 6,
     * the camp (2 x 1) x 4, the bunker (1 x 1, fits anywhere: x 3 made it a third of the district) x 2.
     */
    static int factor(String name)
    {
        String n = name.toLowerCase();
        return n.contains("militarybase") ? 6 : n.contains("military") ? 4 : n.contains("bunker") ? 2 : 1;
    }
    static final String KEY = "!militaryWasteland";
    private static volatile boolean layout;

    /** True for a chunk of a wasteland district (a city chunk of the deadzone style). */
    public static boolean chunk(long seed, int chx, int chz)
    {
        int cx = Math.floorDiv(chx, CELL), cz = Math.floorDiv(chz, CELL);
        return LcCity.enabled() && isCity(seed, cx, cz) && style(seed, cx, cz).endsWith(":deadzone");
    }

    /** True for a block position inside a wasteland district. */
    public static boolean at(long seed, double x, double z)
    {
        return chunk(seed, (int) Math.floor(x) >> 4, (int) Math.floor(z) >> 4);
    }

    static boolean district(String style)
    {
        return style != null && style.endsWith(":deadzone");
    }

    /** Weight of a building in a district's pick (LcCity.pick). */
    static int weight(LcContent.Building b, String style)
    {
        int w = b.styles.get(style);
        return layout && district(style) ? w * factor(b.name) : w;
    }

    /** Military building pick on (worlds created since v0.54, or forced for a session). */
    public static boolean layout()
    {
        return layout;
    }

    /** Switches the military building pick for this session (dev tests); not stored. Cached plans are dropped. */
    public static void setLayout(boolean on)
    {
        if (layout != on)
        {
            layout = on;
            LcCity.layoutChanged();
        }
    }

    /**
     * One MILITARY zone over the district of this cell: its 2 x 2 cells (128 x 128 blocks), y 0 to
     * 255, added once per world when the first chunk near it is populated (StructureData marker).
     * Decimation then gives 25 % of the infected spawning there the military look and draws the
     * district on the GPS map; the buildings no longer carry boxes of their own.
     */
    static void zone(World world, int cellX, int cellZ)
    {
        long seed = world.getSeed();
        if (!district(style(seed, cellX, cellZ)))
        {
            return;
        }
        int gx = Math.floorDiv(cellX, 2) * 2, gz = Math.floorDiv(cellZ, 2) * 2;
        String id = "!wz_" + gx + "_" + gz;
        StructureData data = StructureData.get(world);
        if (data.baseY(id) != null)
        {
            return;
        }
        int x0 = gx * CELL * 16, z0 = gz * CELL * 16, size = 2 * CELL * 16;
        if (ZoneStore.add(ZoneKind.MILITARY, x0, 0, z0, x0 + size - 1, 255, z0 + size - 1) != null)
        {
            data.setBaseY(id, 1);
            FMLLog.info("[%s] wasteland military zone %d,%d to %d,%d", DecimationWorldGen.MODID, x0, z0,
                        x0 + size - 1, z0 + size - 1);
        }
    }

    /**
     * A spot for a group of NPCs in a city: a street chunk, standing at most 2 above the street (on
     * the road, a sidewalk, a wreck's roof at most), so a group never appears on a roof or inside a
     * building. Outside cities every spot is fine.
     */
    public static boolean streetSpot(long seed, int x, int y, int z)
    {
        int chx = x >> 4, chz = z >> 4, cx = Math.floorDiv(chx, CELL), cz = Math.floorDiv(chz, CELL);
        if (!LcCity.enabled() || !isCity(seed, cx, cz))
        {
            return true;
        }
        int g = ground(seed, cx, cz);
        return isStreetChunk(seed, chx, chz) && y > g && y <= g + 3;
    }

    /**
     * World load (overworld, server): a world that has never ticked is new and gets the military
     * building pick; the choice is stored so it stays the same on every later load.
     * -Ddeciworldgen.milwaste=true / false overrides it for a session.
     */
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        World world = event.world;
        if (world.isRemote || world.provider == null || world.provider.dimensionId != 0)
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
        String force = System.getProperty("deciworldgen.milwaste");
        setLayout(force != null ? "true".equals(force) : v == 1);
        FMLLog.info("[%s] wasteland military building pick %s", DecimationWorldGen.MODID, layout ? "on" : "off");
    }
}

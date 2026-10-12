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
 * - DeceasedCraft's military base and camp weigh more in the district's building pick (factor), and
 *   it has more open lots, most of them bare for the military lot scenes, for worlds created since v0.54 only (a changed pick in an older world
 *   would cut buildings at the edge of its generated chunks, as city rivers and refuges);
 * - the enemy military's spawner and the heavier bandit tiers count the district as a military area
 *   (fixes/NpcLoadouts.military), groups spawning on its streets, never on roofs (streetSpot).
 */
public final class Wasteland
{
    /**
     * DeceasedCraft's military buildings are 11 of 47 weight points in the district. Raised per building:
     * the base (3 x 2 chunks, fits only where the block is still free) and the camp (2 x 1) x 3; the bunker
     * (1 x 1, fits anywhere) keeps its own weight. Tried on seed 1's three districts: all x 3 gave 23
     * bunkers and 1 base, base x 6 gave 6 copies of the same base: both read as one building repeated.
     */
    static int factor(String name)
    {
        String n = name.toLowerCase();
        return n.contains("military") ? 3 : 1;
    }

    /** Open lots of the district (new worlds): more of them, most bare, for the military lot scenes. */
    static final float LOT_CHANCE = 0.25f;
    static final int LOT_BARE = 75;

    /** Chance a building chunk of this district is an open lot (LcCity.LOT_CHANCE elsewhere). */
    static float lotChance(String style)
    {
        return layout && district(style) ? LOT_CHANCE : LcCity.LOT_CHANCE;
    }

    /** Per cent of the open lots left bare (no park part): LotDress puts its scene there. */
    static int bareShare(String style)
    {
        return layout && district(style) ? LOT_BARE : 35;
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
     * A spot for a group of NPCs in a city: the top block (MilitarySpawner asks for the highest one) at
     * street level, at most 2 above it, so out in the open: a street, a yard, a lot, a park. A roof is
     * higher; a ground floor has its roof above it, so it is never the top. Outside cities every spot is
     * fine. (v0.54.0 asked for a street chunk: in a superblock district the streets lie on its edge only,
     * out of the 24 to 48 ring from its middle, and no group came.)
     */
    public static boolean streetSpot(long seed, int x, int y, int z)
    {
        int cx = Math.floorDiv(x >> 4, CELL), cz = Math.floorDiv(z >> 4, CELL);
        if (!LcCity.enabled() || !isCity(seed, cx, cz))
        {
            return true;
        }
        int g = ground(seed, cx, cz);
        return y > g && y <= g + 2;
    }

    /**
     * Dev test: writes a bare wasteland lot (16 x 16, its ground at y0) at (x0, z0) with a forced military
     * scene (0 motor pool, 1 supply dump, 2 helicopter crash, 3 casualties) straight into the world, so
     * every scene can be photographed without waiting for one in generation.
     */
    public static void previewLot(World w, int x0, int y0, int z0, int scene, long seed)
    {
        LotPlan p = new LotPlan("lotpreview_" + scene, x0, z0, y0, null, seed, "legacy:deadzone");
        java.util.List<int[]> props = new java.util.ArrayList<int[]>();
        LotDress.force = scene;
        try
        {
            int[] meta = new int[1];
            for (int ly = 0; ly < 6; ly++)
            {
                for (int lz = 0; lz < 16; lz++)
                {
                    for (int lx = 0; lx < 16; lx++)
                    {
                        net.minecraft.block.Block b = p.blockAt(lx, ly, lz, meta);
                        if (meta[0] == net.decimation.worldgen.Plan.SKIP)
                        {
                            continue;
                        }
                        b = b == null ? net.minecraft.init.Blocks.air : b;
                        w.setBlock(x0 + lx, y0 + ly, z0 + lz, b, Math.max(0, meta[0]), 2);
                        if (b.hasTileEntity(Math.max(0, meta[0])))
                        {
                            props.add(new int[] {x0 + lx, y0 + ly, z0 + lz});
                        }
                    }
                }
            }
        }
        finally
        {
            LotDress.force = -1;
        }
        for (int[] q : props)
        {
            net.decimation.fixes.MultiblockRepairHandler.complete(w, q[0], q[1], q[2]);
        }
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

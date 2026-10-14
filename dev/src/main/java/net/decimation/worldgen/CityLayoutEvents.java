package net.decimation.worldgen;

import java.io.File;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.WorldEvent;

/**
 * Switches CityLayout per world (Forge bus): a world that has never ticked is new and gets city sizes; the
 * choice is stored (StructureData CityLayout.KEY) so it stays the same on every later load. Unloading the
 * overworld switches the layout back on, so the next new world's spawn search (before its load event)
 * already sees it. -Ddeciworldgen.citysizes=true|false overrides for a session (dev tests). Airports (city/Airport)
 * the same way, stored as Airport.KEY, -Ddeciworldgen.airports.
 */
public class CityLayoutEvents
{
    /** config/deciworldgen_cities.cfg: size weights and the chance a cell holds a city. */
    public static void configure(File configDir)
    {
        Configuration cfg = new Configuration(new File(configDir, "deciworldgen_cities.cfg"));
        String[] names = new String[CityLayout.SIZES.length];
        int[] w = new int[CityLayout.SIZES.length];
        for (int i = 0; i < w.length; i++)
        {
            names[i] = "weight" + CityLayout.SIZES[i] * 4 + "Districts";
            w[i] = cfg.getInt(names[i], "cities", CityLayout.DEFAULT_WEIGHTS[i], 0, 1000,
                              "how often a city of " + CityLayout.SIZES[i] * 4 + " districts (" + CityLayout.SIZES[i]
                                  + " sectors) is picked, against the other weights");
        }
        double chance = cfg.getFloat("cityChance", "cities", 1f, 0f, 1f,
                                     "chance a 5 x 5 sector cell holds a city (1 = every cell, about 24 % of the land is city)");
        if (cfg.hasChanged())
        {
            cfg.save();
        }
        CityLayout.configure(w, chance);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldLoad(WorldEvent.Load event)
    {
        World world = event.world;
        if (world.isRemote || world.provider == null || world.provider.dimensionId != 0)
        {
            return;
        }
        StructureData data = StructureData.get(world);
        Integer v = data.baseY(CityLayout.KEY);
        if (v == null)
        {
            v = world.getTotalWorldTime() == 0 ? 1 : 0;
            data.setBaseY(CityLayout.KEY, v);
        }
        String force = System.getProperty("deciworldgen.citysizes");
        CityLayout.set(force != null ? "true".equals(force) : v == 1);
        FMLLog.info("[%s] city sizes %s", DecimationWorldGen.MODID, CityLayout.on() ? "on" : "off (world older than v0.60.0)");
        // military airports in wasteland cities (city/Airport): worlds created since v0.62.0
        Integer a = data.baseY(net.decimation.worldgen.city.Airport.KEY);
        if (a == null)
        {
            a = world.getTotalWorldTime() == 0 ? 1 : 0;
            data.setBaseY(net.decimation.worldgen.city.Airport.KEY, a);
        }
        String forceAir = System.getProperty("deciworldgen.airports");
        net.decimation.worldgen.city.Airport.set(forceAir != null ? "true".equals(forceAir) : a == 1);
        FMLLog.info("[%s] airports %s", DecimationWorldGen.MODID, net.decimation.worldgen.city.Airport.on() ? "on" : "off (world older than v0.62.0)");
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
        World world = event.world;
        if (!world.isRemote && world.provider != null && world.provider.dimensionId == 0)
        {
            CityLayout.set(true);                          // the default for the next new world's spawn search
        }
    }
}

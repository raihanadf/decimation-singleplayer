package net.decimation.worldgen.terrain;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.WorldTypeEvent;

/** TERRAIN_GEN_BUS hooks for the Decimation world type. */
public class TerrainEvents
{
    /** Swap in our biome layers (fired from the WorldChunkManager constructor). */
    @SubscribeEvent
    public void onInitBiomeGens(WorldTypeEvent.InitBiomeGens event)
    {
        if (event.worldType != DecimationWorldType.INSTANCE)
        {
            return;
        }
        BiomeMap map = new BiomeMap(event.seed);
        event.newBiomeGens = new GenLayer[] {new DeciGenLayer(event.seed, map, 4),
                                             new DeciGenLayer(event.seed, map, 1)};
    }

    /**
     * Caves and ravines that stay below y 50 under cities and military zones.
     * Only replaces the exact vanilla generators (another mod's stays).
     */
    @SubscribeEvent
    public void onInitMapGen(InitMapGenEvent event)
    {
        if (event.type == InitMapGenEvent.EventType.CAVE
            && event.originalGen.getClass() == net.minecraft.world.gen.MapGenCaves.class)
        {
            event.newGen = new SealedCaves.Caves();
        }
        else if (event.type == InitMapGenEvent.EventType.RAVINE
                 && event.originalGen.getClass() == net.minecraft.world.gen.MapGenRavine.class)
        {
            event.newGen = new SealedCaves.Ravines();
        }
    }

    /**
     * Lakes: none on or next to a city (city cells, any biome) or in the military zone (buildings cancel on
     * water), one in four of vanilla's in the other dead biomes (dry land),
     * vanilla's rate in the overgrown ones. No surface lava pools anywhere.
     */
    @SubscribeEvent
    public void onPopulate(PopulateChunkEvent.Populate event)
    {
        if (event.type != PopulateChunkEvent.Populate.EventType.LAKE
            && event.type != PopulateChunkEvent.Populate.EventType.LAVA)
        {
            return;
        }
        // a lake spans the 2 x 2 chunks from this one: none over a railway tunnel
        int bx = event.chunkX << 4, bz = event.chunkZ << 4;
        // nor over a city: a lake carved through the airport's ground after it was written (14 Oktober 2026)
        if (net.decimation.worldgen.city.Rails.near(event.world.getSeed(), bx, bz, bx + 31, bz + 31)
            || net.decimation.worldgen.city.LcCity.enabled()
                && net.decimation.worldgen.city.LcCity.nearCity(event.world.getSeed(), bx, bz, bx + 31, bz + 31))
        {
            event.setResult(Event.Result.DENY);
            return;
        }
        BiomeGenBase b = event.world.getBiomeGenForCoords(event.chunkX * 16 + 16, event.chunkZ * 16 + 16);
        if (!(b instanceof DeciBiome))
        {
            return;
        }
        boolean dead = b == DecimationBiomes.urban || b == DecimationBiomes.military
            || b == DecimationBiomes.suburb || b == DecimationBiomes.wasteland || b == DecimationBiomes.burntForest;
        boolean city = b == DecimationBiomes.urban || b == DecimationBiomes.military;
        if (event.type == PopulateChunkEvent.Populate.EventType.LAVA || city
            || dead && ((event.chunkX * 31 + event.chunkZ * 17) & 3) != 0)
        {
            event.setResult(Event.Result.DENY);
        }
    }

    /**
     * Springs (vanilla's 50 water and 20 lava sources per chunk, set where
     * stone has one open side) never next to a railway tunnel: a tunnel
     * chunk is written in two population windows, a neighbour's springs
     * found the open face of the first half and flooded the tunnel, and the
     * second half then removed the source and left the flow standing (rail
     * test, 11 Oktober 2026). Springs land at x + 8 .. x + 23 (block coords).
     */
    @SubscribeEvent
    public void onDecorate(DecorateBiomeEvent.Decorate event)
    {
        long seed = event.world.getSeed();
        int x0 = event.chunkX + 7, z0 = event.chunkZ + 7, x1 = event.chunkX + 24, z1 = event.chunkZ + 24;
        // nor in a city: on the open ground of an airport a spring ran over the road (airport test, 14 Oktober
        // 2026); a city has no use for them
        if (event.type == DecorateBiomeEvent.Decorate.EventType.LAKE
            && (net.decimation.worldgen.city.Rails.near(seed, x0, z0, x1, z1)
                || net.decimation.worldgen.city.LcCity.enabled() && net.decimation.worldgen.city.LcCity.nearCity(seed, x0, z0, x1, z1)))
        {
            event.setResult(Event.Result.DENY);
        }
    }
}

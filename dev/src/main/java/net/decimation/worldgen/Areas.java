package net.decimation.worldgen;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

/**
 * Areas we own, with a kind, an owner and a state (docs/systems_plan.md 2.5), per world in
 * <world>/deciworldgen_areas.json. Skeleton for the claims, raids, radiation and police safezones of
 * later steps: an area of a kind Decimation knows (SAFEZONE, MILITARY, RADIATION) also puts a zone of
 * that type into Decimation's zone list through ZoneStore, so its HUD and zone rules see it; claims
 * (BASE_CLAIM, CITY_CLAIM) are ours only. The state is free text per kind ("wild", "claimed",
 * "decaying" ...) with the day it was set (WorldClock). Generated sites still write ZoneStore directly;
 * they move here when their step needs owners or states.
 */
public class Areas
{
    public enum Kind
    {
        SAFEZONE(ZoneKind.SAFEZONE), MILITARY(ZoneKind.MILITARY), RADIATION(ZoneKind.RADIATION), BASE_CLAIM(null), CITY_CLAIM(null);

        /** Decimation zone type it maps to, or null (ours only). */
        public final ZoneKind zone;

        Kind(ZoneKind zone)
        {
            this.zone = zone;
        }
    }

    public static class Area
    {
        public String id, name, owner, state;
        public Kind kind;
        public int x1, y1, z1, x2, y2, z2;
        /** WorldClock day the state was set. */
        public int since;

        public boolean contains(int x, int y, int z)
        {
            return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
        }
    }

    private static final String FILE = "deciworldgen_areas.json";
    private static final Map<String, Area> areas = new LinkedHashMap<String, Area>();
    private static File file;
    private static boolean dirty;

    /**
     * Adds an area (replacing one with the same id) in state "new"; the box is normalised. Null when no
     * world is loaded.
     */
    public static synchronized Area add(String id, Kind kind, String name, int x1, int y1, int z1, int x2, int y2, int z2)
    {
        if (file == null)
        {
            return null;
        }
        remove(id);
        Area a = new Area();
        a.id = id;
        a.kind = kind;
        a.name = name;
        a.state = "new";
        a.since = net.decimation.fixes.WorldClock.day();
        a.x1 = Math.min(x1, x2);
        a.y1 = Math.min(y1, y2);
        a.z1 = Math.min(z1, z2);
        a.x2 = Math.max(x1, x2);
        a.y2 = Math.max(y1, y2);
        a.z2 = Math.max(z1, z2);
        if (kind.zone != null)
        {
            ZoneStore.add(kind.zone, a.x1, a.y1, a.z1, a.x2, a.y2, a.z2);
        }
        areas.put(id, a);
        dirty = true;
        return a;
    }

    /** Removes an area and its Decimation zone; true when it existed. */
    public static synchronized boolean remove(String id)
    {
        Area a = areas.remove(id);
        if (a == null)
        {
            return false;
        }
        if (a.kind.zone != null)
        {
            ZoneStore.removeBox(a.kind.zone, a.x1, a.y1, a.z1, a.x2, a.y2, a.z2);
        }
        dirty = true;
        return true;
    }

    public static synchronized Area get(String id)
    {
        return areas.get(id);
    }

    /** Sets state and owner (null keeps the owner) and stamps the day. */
    public static synchronized void setState(String id, String state, String owner)
    {
        Area a = areas.get(id);
        if (a != null)
        {
            a.state = state;
            if (owner != null)
            {
                a.owner = owner;
            }
            a.since = net.decimation.fixes.WorldClock.day();
            dirty = true;
        }
    }

    /** Areas containing the point, of any kind when `kind` is null. */
    public static synchronized List<Area> at(Kind kind, int x, int y, int z)
    {
        List<Area> out = new ArrayList<Area>();
        for (Area a : areas.values())
        {
            if ((kind == null || a.kind == kind) && a.contains(x, y, z))
            {
                out.add(a);
            }
        }
        return out;
    }

    public static synchronized List<Area> of(Kind kind)
    {
        List<Area> out = new ArrayList<Area>();
        for (Area a : areas.values())
        {
            if (kind == null || a.kind == kind)
            {
                out.add(a);
            }
        }
        return out;
    }

    /** Writes now (dev tests; otherwise on world save). */
    public static synchronized void save()
    {
        if (!dirty || file == null)
        {
            return;
        }
        try
        {
            Writer w = new FileWriter(file);
            w.write(new GsonBuilder().setPrettyPrinting().create().toJson(new ArrayList<Area>(areas.values())));
            w.close();
            dirty = false;
        }
        catch (Exception e)
        {
            FMLLog.info("[%s] could not write %s: %s", DecimationWorldGen.MODID, file, e);
        }
    }

    /** Reads the file again, dropping what is in memory (dev tests: the round trip). */
    public static synchronized int reload()
    {
        areas.clear();
        if (file != null && file.isFile())
        {
            try
            {
                Reader in = new FileReader(file);
                List<Area> list = new Gson().fromJson(in, new TypeToken<List<Area>>() {}.getType());
                in.close();
                if (list != null)
                {
                    for (Area a : list)
                    {
                        if (a != null && a.id != null && a.kind != null)
                        {
                            areas.put(a.id, a);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                FMLLog.info("[%s] could not read %s: %s", DecimationWorldGen.MODID, file, e);
            }
        }
        dirty = false;
        return areas.size();
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        World world = event.world;
        if (world.isRemote || world.provider == null || world.provider.dimensionId != 0)
        {
            return;
        }
        synchronized (Areas.class)
        {
            file = new File(world.getSaveHandler().getWorldDirectory(), FILE);
            FMLLog.info("[%s] %d area(s) loaded for this world", DecimationWorldGen.MODID, reload());
        }
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event)
    {
        if (!event.world.isRemote && event.world.provider != null && event.world.provider.dimensionId == 0)
        {
            save();
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
        if (!event.world.isRemote && event.world.provider != null && event.world.provider.dimensionId == 0)
        {
            synchronized (Areas.class)
            {
                save();
                areas.clear();
                file = null;
            }
        }
    }
}

package net.decimation.fixes.hosting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The mods a world needs, as [id, version] pairs (docs/superpowers/specs/2026-10-18-player-hosting-part1-design.md 3.3). */
public final class ModSet
{
    private static final Set<String> BUILT_IN = new HashSet<String>(Arrays.asList("mcp", "FML", "Forge", "MinecraftForge"));
    private final Map<String, String> mods;

    private ModSet(Map<String, String> mods)
    {
        this.mods = mods;
    }

    public static ModSet of(List<String[]> pairs)
    {
        Map<String, String> m = new LinkedHashMap<String, String>();
        for (String[] p : pairs)
        {
            m.put(p[0], p[1]);
        }
        return new ModSet(m);
    }

    /** This game's active mods without Minecraft's and Forge's own. */
    public static ModSet current()
    {
        List<String[]> l = new ArrayList<String[]>();
        for (cpw.mods.fml.common.ModContainer c : cpw.mods.fml.common.Loader.instance().getActiveModList())
        {
            if (!BUILT_IN.contains(c.getModId()))
            {
                l.add(new String[] {c.getModId(), c.getVersion()});
            }
        }
        return of(l);
    }

    /** ModSet.current() in a game; an empty set outside one (unit tests). */
    static ModSet current0()
    {
        try
        {
            return current();
        }
        catch (Throwable t)
        {
            return of(new ArrayList<String[]>());
        }
    }

    /** This game's DeciRemnant version (deciworldgen's version from mcmod.info, filled from the git tag). */
    public static String ourVersion()
    {
        try
        {
            return cpw.mods.fml.common.Loader.instance().getIndexedModList().get("deciworldgen").getVersion();
        }
        catch (Throwable t)
        {
            return "dev";
        }
    }

    /** "id version" of the first mod of `need` this set lacks or has in another version; null when none. */
    public String firstMissing(ModSet need)
    {
        for (Map.Entry<String, String> e : need.mods.entrySet())
        {
            if (!e.getValue().equals(mods.get(e.getKey())))
            {
                return e.getKey() + " " + e.getValue();
            }
        }
        return null;
    }

    public List<String[]> pairs()
    {
        List<String[]> l = new ArrayList<String[]>();
        for (Map.Entry<String, String> e : mods.entrySet())
        {
            l.add(new String[] {e.getKey(), e.getValue()});
        }
        return Collections.unmodifiableList(l);
    }
}

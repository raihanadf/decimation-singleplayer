package net.decimation.fixes.hosting;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.NetworkModHolder;
import cpw.mods.fml.relauncher.Side;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Mods a joiner may lack (ModSet.OPTIONAL: Gliby's Voice Chat). Forge refuses a client that lacks any mod the server has
 * (NetworkModHolder's default checker), so a host with the voice chat jar turned away every player of our release.
 * Each optional mod's checker is wrapped: a remote without the mod passes, a remote with it still needs Forge's answer.
 */
public final class OptionalMods
{
    private OptionalMods()
    {
    }

    /** Wraps the checker of every optional mod this game has; called once in postInit. */
    public static void install()
    {
        for (Map.Entry<ModContainer, NetworkModHolder> e : NetworkRegistry.INSTANCE.registry().entrySet())
        {
            String id = e.getKey().getModId();
            if (ModSet.OPTIONAL.contains(id))
            {
                try
                {
                    relax(e.getValue(), id);
                    FMLLog.info("[deciworldgen] %s is optional for players who join", id);
                }
                catch (Exception ex)
                {
                    FMLLog.warning("[deciworldgen] could not make %s optional: %s", id, ex);
                }
            }
        }
    }

    static void relax(final NetworkModHolder h, final String id) throws Exception
    {
        Field f = NetworkModHolder.class.getDeclaredField("checker");
        f.setAccessible(true);
        final NetworkModHolder.NetworkChecker forge = (NetworkModHolder.NetworkChecker) f.get(h);
        if (forge instanceof Relaxed)
        {
            return;
        }
        f.set(h, new Relaxed(h, forge, id));
    }

    /** Lets a remote without the mod through; asks Forge's own checker otherwise. */
    private static final class Relaxed extends NetworkModHolder.NetworkChecker
    {
        private final NetworkModHolder.NetworkChecker forge;
        private final String id;

        Relaxed(NetworkModHolder h, NetworkModHolder.NetworkChecker forge, String id)
        {
            h.super();
            this.forge = forge;
            this.id = id;
        }

        @Override
        public boolean check(Map<String, String> remoteVersions, Side side)
        {
            return !remoteVersions.containsKey(id) || forge.check(remoteVersions, side);
        }

        @Override
        public String toString()
        {
            return "optional, else " + forge;
        }
    }
}

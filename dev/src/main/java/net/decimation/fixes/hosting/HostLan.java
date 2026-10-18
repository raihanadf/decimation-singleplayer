package net.decimation.fixes.hosting;

/** The port shareToLAN returned; the world stays open to LAN until it closes (1.7.10 cannot close it again). */
public final class HostLan
{
    private static volatile String port = "-1";

    private HostLan()
    {
    }

    public static void remember(String p)
    {
        if (p != null)
        {
            port = p;
        }
    }

    public static String port()
    {
        return port;
    }

    /**
     * The hosted world's player limit follows the host's choice (Minecraft's LAN world keeps 8: Community rows showed
     * "x/8" while the relay let in only the host's max). ServerConfigurationManager.maxPlayers is protected.
     */
    public static void limit(net.minecraft.server.integrated.IntegratedServer s, int max)
    {
        if (s != null && s.getConfigurationManager() != null)
        {
            cpw.mods.fml.relauncher.ReflectionHelper.setPrivateValue(net.minecraft.server.management.ServerConfigurationManager.class,
                s.getConfigurationManager(), max, "maxPlayers", "field_72405_c");
        }
    }
}

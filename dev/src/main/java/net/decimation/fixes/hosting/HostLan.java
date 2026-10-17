package net.decimation.fixes.hosting;

/** The port shareToLAN returned; the world stays open to LAN until it closes (1.7.10 cannot close it again). */
final class HostLan
{
    private static volatile String port = "-1";

    private HostLan()
    {
    }

    static void remember(String p)
    {
        if (p != null)
        {
            port = p;
        }
    }

    static String port()
    {
        return port;
    }
}

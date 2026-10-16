package net.decimation.fixes;

/**
 * Our name and version where Decimation shows its own (user, 17 Oktober 2026: "for version replace version code with
 * our version code name, and for decimation replace with deciremnant"). PatchMenuTexts sends Decimation's version
 * constant "1.21.10f" in the main menu (under the logo, in the welcome box) here, and the window title; the title
 * line becomes "DeciRemnant: " and the welcome text names DeciRemnant (plain text swaps).
 */
public final class MenuTexts
{
    private MenuTexts()
    {
    }

    /** Our version, without a dev build's suffix (0.64.0-main.3+abc1234 becomes 0.64.0); Decimation's when unknown. */
    public static String version()
    {
        try
        {
            String v = Updater.current();
            return v == null || v.isEmpty() || "0".equals(v) ? "1.21.10f" : v.split("[-+]")[0];
        }
        catch (Throwable early)
        {
            return "1.21.10f"; // Forge's mod list not ready yet
        }
    }

    /** The game window's title (Decimation's was "Minecraft - Decimation v1.21.10f"; no dash in ours: user rule). */
    public static String windowTitle()
    {
        return "DeciRemnant v" + version() + " (Minecraft 1.7.10)";
    }
}

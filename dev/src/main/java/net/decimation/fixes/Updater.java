package net.decimation.fixes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import net.minecraftforge.common.config.Configuration;

/**
 * Auto update from our GitHub releases (docs/ideas.md item 20; user, 13 Oktober 2026: launch, check
 * GitHub, the menu button says "Update" when there is one, it installs with a progress screen, else
 * "Play"). Client only:
 * - check(): on a background thread, GitHub's release list (pre-releases too: ours are), the newest
 *   release with a "Decimation.jar" asset whose version is above ours (mcmod.info);
 * - required(): what Decimation's UpdateChecker.isUpdateRequired answers (tools/patches/PatchNoUpdate,
 *   v2): true once a newer release is known, so its menu shows "Update"; its update page is replaced by
 *   UpdateScreen;
 * - download(): the asset into "<jar>.download" beside the running jar with progress, checked against
 *   GitHub's sha256 (or the size), then staged; the swap (UpdateSwap, its own process) runs when the
 *   game closes ("Quit game" or later), because a loaded jar cannot be replaced on Windows.
 * config/deciworldgen_update.cfg: enabled, api. Dev tests: -Ddeciworldgen.update.api / .target.
 */
public final class Updater
{
    public enum State { IDLE, CHECKING, NONE, AVAILABLE, DOWNLOADING, READY, FAILED }

    public static final String ASSET = "Decimation.jar";
    private static final String DEFAULT_API = "https://api.github.com/repos/ucilmenangis/decimation-singleplayer/releases?per_page=10";

    private static volatile State state = State.IDLE;
    private static volatile String latest, url, digest, message = "";
    private static volatile long size, done;
    private static volatile boolean enabled = true, hooked;
    private static String api = DEFAULT_API;
    private static File staged, target;

    private Updater()
    {
    }

    public static void configure(File configDir)
    {
        Configuration cfg = new Configuration(new File(configDir, "deciworldgen_update.cfg"));
        enabled = cfg.getBoolean("enabled", "update", true, "check GitHub for a newer release at start and offer it in the menu");
        api = cfg.getString("api", "update", DEFAULT_API, "release list (GitHub API)");
        cfg.save();
        api = System.getProperty("deciworldgen.update.api", api);
    }

    /** True when a newer release is known (or downloaded and waiting for a restart). */
    public static boolean required()
    {
        State s = state;
        return s == State.AVAILABLE || s == State.DOWNLOADING || s == State.READY || s == State.FAILED && latest != null;
    }

    public static State state() { return state; }
    public static String latest() { return latest; }
    public static String message() { return message; }
    public static long size() { return size; }
    public static long downloaded() { return done; }

    /** Our own version from mcmod.info (FML metadata). */
    public static String current()
    {
        ModContainer c = Loader.instance().getIndexedModList().get("deciworldgen");
        return c == null || c.getMetadata() == null ? "0" : c.getMetadata().version;
    }

    /** The jar this game runs from (the one to replace), or the dev test's stand in; null in a dev folder. */
    public static File target()
    {
        String t = System.getProperty("deciworldgen.update.target");
        if (t != null)
        {
            return new File(t);
        }
        try
        {
            File f = new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return f.isFile() ? f : null;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /** Starts the release check on a background thread (once at a time). */
    public static synchronized void check()
    {
        if (!enabled || state == State.CHECKING || state == State.DOWNLOADING || state == State.READY)
        {
            return;
        }
        state = State.CHECKING;
        Thread t = new Thread("deciworldgen update check")
        {
            public void run()
            {
                try
                {
                    find();
                }
                catch (Throwable e)
                {
                    message = "update check failed: " + e;
                    state = Updater.State.NONE;
                    FMLLog.info("[deciworldgen] %s", message);
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    @SuppressWarnings("unchecked")
    private static void find() throws Exception
    {
        HttpURLConnection c = open(api);
        InputStream in = c.getInputStream();
        List<Map<String, Object>> releases = new Gson().fromJson(new InputStreamReader(in, "UTF-8"), List.class);
        in.close();
        String mine = current();
        for (Map<String, Object> r : releases)
        {
            if (Boolean.TRUE.equals(r.get("draft")))
            {
                continue;
            }
            String tag = String.valueOf(r.get("tag_name"));
            List<Map<String, Object>> assets = (List<Map<String, Object>>) r.get("assets");
            for (Map<String, Object> a : assets == null ? java.util.Collections.<Map<String, Object>>emptyList() : assets)
            {
                if (ASSET.equals(a.get("name")) && newer(tag, mine))
                {
                    latest = tag;
                    url = String.valueOf(a.get("browser_download_url"));
                    digest = a.get("digest") == null ? null : String.valueOf(a.get("digest"));
                    size = ((Number) a.get("size")).longValue();
                    message = "update " + tag + " available (" + (size >> 20) + " MB)";
                    state = State.AVAILABLE;
                    FMLLog.info("[deciworldgen] %s, running %s", message, mine);
                    return;
                }
            }
            if (assets != null && !assets.isEmpty())
            {
                break;                                     // the newest release with files is not newer: none
            }
        }
        message = "up to date (" + mine + ")";
        state = State.NONE;
        FMLLog.info("[deciworldgen] update check: %s", message);
    }

    /** a > b by their leading dotted numbers ("0.58.0", "0.58.0-main+abc-dirty"). */
    static boolean newer(String a, String b)
    {
        int[] x = numbers(a), y = numbers(b);
        for (int i = 0; i < Math.max(x.length, y.length); i++)
        {
            int p = i < x.length ? x[i] : 0, q = i < y.length ? y[i] : 0;
            if (p != q)
            {
                return p > q;
            }
        }
        return false;
    }

    private static int[] numbers(String v)
    {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^v?(\\d+(?:\\.\\d+)*)").matcher(v == null ? "" : v.trim());
        if (!m.find())
        {
            return new int[0];
        }
        String[] parts = m.group(1).split("\\.");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++)
        {
            out[i] = Integer.parseInt(parts[i]);
        }
        return out;
    }

    /** Downloads the known update on a background thread; READY when checked, FAILED with a message. */
    public static synchronized void download()
    {
        if (state != State.AVAILABLE && state != State.FAILED || url == null)
        {
            return;
        }
        target = target();
        if (target == null)
        {
            message = "this game does not run from a jar (dev folder): nothing to replace";
            state = State.FAILED;
            return;
        }
        staged = new File(target.getPath() + ".download");
        state = State.DOWNLOADING;
        done = 0;
        Thread t = new Thread("deciworldgen update download")
        {
            public void run()
            {
                try
                {
                    fetch();
                }
                catch (Throwable e)
                {
                    message = "download failed: " + e;
                    state = Updater.State.FAILED;
                    staged.delete();
                    FMLLog.info("[deciworldgen] %s", message);
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    private static void fetch() throws Exception
    {
        HttpURLConnection c = open(url);
        long total = c.getContentLengthLong();
        if (total > 0)
        {
            size = total;
        }
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        InputStream in = c.getInputStream();
        OutputStream out = new FileOutputStream(staged);
        byte[] buf = new byte[1 << 16];
        for (int r; (r = in.read(buf)) > 0;)
        {
            out.write(buf, 0, r);
            sha.update(buf, 0, r);
            done += r;
        }
        out.close();
        in.close();
        StringBuilder hex = new StringBuilder();
        for (byte b : sha.digest())
        {
            hex.append(String.format("%02x", b));
        }
        boolean ok = digest != null && digest.startsWith("sha256:") ? digest.substring(7).equalsIgnoreCase(hex.toString())
            : size <= 0 || done == size;
        if (!ok)
        {
            staged.delete();
            message = "the download did not match the release (" + (digest != null ? "sha256" : "size") + "): try again";
            state = State.FAILED;
            FMLLog.info("[deciworldgen] update %s: %s", latest, message);
            return;
        }
        message = "update " + latest + " ready: restart the game to play it";
        state = State.READY;
        FMLLog.info("[deciworldgen] update %s downloaded and checked (sha256 %s)", latest, hex);
        hookSwap();
    }

    /** When the game closes, the staged jar replaces the running one (UpdateSwap in its own process). */
    private static synchronized void hookSwap()
    {
        if (hooked)
        {
            return;
        }
        hooked = true;
        Runtime.getRuntime().addShutdownHook(new Thread("deciworldgen update swap")
        {
            public void run()
            {
                swap(false);
            }
        });
    }

    /**
     * Starts UpdateSwap from a copy of its class in a temporary folder (it must not run from the jar it
     * replaces); waits for it when `wait` (dev tests). Returns the helper's exit code when waited, else 0.
     */
    public static int swap(boolean wait)
    {
        if (staged == null || !staged.isFile() || target == null)
        {
            return -1;
        }
        try
        {
            // a fresh private folder each time (owner only on POSIX): a fixed name in the shared temp folder
            // could be planted by another local user and its class run as the player (security review)
            File dir = java.nio.file.Files.createTempDirectory("deciworldgen-update").toFile();
            File cls = new File(dir, "net/decimation/fixes/UpdateSwap.class");
            cls.getParentFile().mkdirs();
            InputStream in = Updater.class.getResourceAsStream("/net/decimation/fixes/UpdateSwap.class");
            OutputStream out = new FileOutputStream(cls);
            byte[] buf = new byte[8192];
            for (int r; (r = in.read(buf)) > 0;)
            {
                out.write(buf, 0, r);
            }
            out.close();
            in.close();
            String java = new File(new File(System.getProperty("java.home"), "bin"),
                                   System.getProperty("os.name").toLowerCase().contains("win") ? "javaw.exe" : "java").getPath();
            Process p = new ProcessBuilder(java, "-cp", dir.getPath(), "net.decimation.fixes.UpdateSwap",
                                           target.getAbsolutePath(), staged.getAbsolutePath(), dir.getAbsolutePath()).start();
            return wait ? p.waitFor() : 0;
        }
        catch (Exception e)
        {
            System.err.println("[deciworldgen] update swap could not start: " + e);
            return -2;
        }
    }

    private static HttpURLConnection open(String u) throws Exception
    {
        HttpURLConnection c = (HttpURLConnection) new URL(u).openConnection();
        c.setRequestProperty("User-Agent", "deciworldgen-updater");
        c.setRequestProperty("Accept", "application/vnd.github+json");
        c.setConnectTimeout(10000);
        c.setReadTimeout(30000);
        c.setInstanceFollowRedirects(true);
        return c;
    }

    /** Release list to ask (dev tests: the fake release server). */
    public static void setApi(String u)
    {
        api = u;
        enabled = true;
    }

    /** Forget everything (dev tests). */
    public static synchronized void reset()
    {
        state = State.IDLE;
        latest = url = digest = null;
        size = done = 0;
        message = "";
    }
}

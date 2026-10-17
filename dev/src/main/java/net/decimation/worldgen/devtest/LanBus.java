package net.decimation.worldgen.devtest;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Two games on one Mac talk through two files in a shared folder (`-Ddeciworldgen.autotest.bus=DIR`, docs/
 * player_hosting.md section 9): the joiner writes `req` ("N verb"), the host answers in `res` ("N verb words...").
 * Test plumbing only: each side's own checks still go through the real game connection.
 */
final class LanBus
{
    private static final File DIR = new File(System.getProperty("deciworldgen.autotest.bus", "lanbus"));

    private LanBus()
    {
    }

    static void write(String name, String text)
    {
        try
        {
            DIR.mkdirs();
            File tmp = new File(DIR, name + ".tmp");
            Files.write(tmp.toPath(), text.getBytes(StandardCharsets.UTF_8));
            if (!tmp.renameTo(new File(DIR, name)))
            {
                Files.write(new File(DIR, name).toPath(), text.getBytes(StandardCharsets.UTF_8));
            }
        }
        catch (Exception e)
        {
            DevTestResults.value("lanbus", "write " + name, e.toString());
        }
    }

    /** The file's words, or null when it is missing. */
    static String[] read(String name)
    {
        try
        {
            File f = new File(DIR, name);
            return f.isFile() ? new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8).trim().split("\\s+") : null;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    static void clear()
    {
        new File(DIR, "req").delete();
        new File(DIR, "res").delete();
    }
}

package net.decimation.worldgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.FMLLog;

/**
 * The world generation content inside the release jar (user, 13 Oktober 2026: one jar with
 * everything; the city never appeared on a friend's install because the city pack lived only in our
 * own config folders). tools/onejar.py puts config/decimation_worldgen (the converted city pack lc/,
 * large schematics, small schematics) into the jar under assets/deciworldgen/content/ with a file list
 * (manifest.txt) and a version (version.txt). On start, before anything reads the folder, the files go
 * to config/decimation_worldgen/ when that folder's stamp (.content_version) is not this version; the
 * loaders stay as they are. A jar without the content (dev builds) changes nothing.
 */
public final class BundledContent
{
    private static final String ROOT = "/assets/deciworldgen/content/";
    private static final String STAMP = ".content_version";

    private BundledContent()
    {
    }

    /** Unpacks the bundled content into `dir` if needed; returns the number of files written. */
    public static int unpack(File dir)
    {
        String version = text(ROOT + "version.txt");
        String manifest = text(ROOT + "manifest.txt");
        if (version == null || manifest == null)
        {
            return 0;                                     // dev build: content is in the run folder
        }
        version = version.trim();
        File stamp = new File(dir, STAMP);
        String have = stamp.isFile() ? readFile(stamp) : null;
        if (version.equals(have == null ? null : have.trim()))
        {
            return 0;
        }
        int n = 0;
        List<String> failed = new ArrayList<String>();
        for (String line : manifest.split("\n"))
        {
            String path = line.trim();
            if (path.isEmpty() || path.contains(".."))
            {
                continue;
            }
            File out = new File(dir, path);
            if (copy(ROOT + path, out))
            {
                n++;
            }
            else
            {
                failed.add(path);
            }
        }
        if (failed.isEmpty())
        {
            writeFile(stamp, version);
        }
        FMLLog.info("[%s] bundled content %s unpacked: %d files, %d failed %s", DecimationWorldGen.MODID, version, n,
                    failed.size(), failed.isEmpty() ? "" : failed.toString());
        return n;
    }

    private static boolean copy(String resource, File out)
    {
        InputStream in = BundledContent.class.getResourceAsStream(resource);
        if (in == null)
        {
            return false;
        }
        try
        {
            File parent = out.getParentFile();
            if (parent != null && !parent.isDirectory() && !parent.mkdirs())
            {
                return false;
            }
            File tmp = new File(out.getPath() + ".part");
            OutputStream o = new FileOutputStream(tmp);
            byte[] buf = new byte[65536];
            for (int r; (r = in.read(buf)) > 0;)
            {
                o.write(buf, 0, r);
            }
            o.close();
            if (out.exists() && !out.delete())
            {
                return false;
            }
            return tmp.renameTo(out);
        }
        catch (Exception e)
        {
            FMLLog.info("[%s] could not unpack %s: %s", DecimationWorldGen.MODID, resource, e);
            return false;
        }
        finally
        {
            try
            {
                in.close();
            }
            catch (Exception ignored)
            {
            }
        }
    }

    private static String text(String resource)
    {
        InputStream in = BundledContent.class.getResourceAsStream(resource);
        if (in == null)
        {
            return null;
        }
        try
        {
            BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder b = new StringBuilder();
            for (String l; (l = r.readLine()) != null;)
            {
                b.append(l).append('\n');
            }
            r.close();
            return b.toString();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static String readFile(File f)
    {
        try
        {
            return new String(java.nio.file.Files.readAllBytes(f.toPath()), "UTF-8");
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static void writeFile(File f, String s)
    {
        try
        {
            java.nio.file.Files.write(f.toPath(), s.getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            FMLLog.info("[%s] could not write %s: %s", DecimationWorldGen.MODID, f, e);
        }
    }
}

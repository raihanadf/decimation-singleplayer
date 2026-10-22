package net.decimation.fixes.gunpack;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Where one gun package's files are: a folder (a dev's packages, the converter's output) or our jar
 * (assets/deciworldgen/gunpacks/NAME/, the shipped ones). Paths are relative to the package root: "gun.json",
 * "assets/deci/models/guns/rifle/NAME.bmodel" ... Plain Java.
 */
public abstract class GunPackSource
{
    /** Shipped packages inside our jar: one folder each under this, listed in index.txt. */
    public static final String JAR_ROOT = "/assets/deciworldgen/gunpacks/";
    /** The package's folder name (the gun's name). */
    public final String name;

    GunPackSource(String name)
    {
        this.name = name;
    }

    public abstract InputStream open(String path) throws IOException;

    public abstract boolean has(String path);

    /** Where it is, for logs and the dev test. */
    public abstract String where();

    /** Files of a folder of the package (folder sources only; empty for the jar, which cannot be listed). */
    public List<String> list(String folder)
    {
        return new ArrayList<String>();
    }

    public String text(String path) throws IOException
    {
        InputStream in = open(path);
        try
        {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            for (int n; (n = in.read(buf)) > 0; )
            {
                b.write(buf, 0, n);
            }
            return new String(b.toByteArray(), "UTF-8");
        }
        finally
        {
            in.close();
        }
    }

    /** A package in a folder of the file system. */
    public static final class Folder extends GunPackSource
    {
        final File dir;

        public Folder(File dir)
        {
            super(dir.getName());
            this.dir = dir;
        }

        private File file(String path)
        {
            return new File(dir, path.replace('/', File.separatorChar));
        }

        public InputStream open(String path) throws IOException
        {
            File f = file(path);
            if (!f.isFile())
            {
                throw new FileNotFoundException(f.getPath());
            }
            return new FileInputStream(f);
        }

        public boolean has(String path)
        {
            return file(path).isFile();
        }

        public String where()
        {
            return dir.getPath();
        }

        @Override
        public List<String> list(String folder)
        {
            List<String> out = new ArrayList<String>();
            File[] files = file(folder).listFiles();
            if (files != null)
            {
                for (File f : files)
                {
                    if (f.isFile())
                    {
                        out.add(folder + f.getName());
                    }
                }
            }
            java.util.Collections.sort(out);
            return out;
        }
    }

    /** A package shipped in our jar (or the dev build's resources folder), under a class path root (JAR_ROOT). */
    public static final class ClassPath extends GunPackSource
    {
        private final String root;

        public ClassPath(String root, String name)
        {
            super(name);
            this.root = root;
        }

        private String resource(String path)
        {
            return root + name + "/" + path;
        }

        public InputStream open(String path) throws IOException
        {
            InputStream in = GunPackSource.class.getResourceAsStream(resource(path));
            if (in == null)
            {
                throw new FileNotFoundException(resource(path));
            }
            return in;
        }

        public boolean has(String path)
        {
            return GunPackSource.class.getResource(resource(path)) != null;
        }

        public String where()
        {
            return "jar " + root.substring(1) + name;
        }
    }

    /**
     * Package folders under a directory: the directory itself when it holds a gun.json, else each sub folder that
     * does, sorted by name.
     */
    public static List<GunPackSource> scan(File dir)
    {
        List<GunPackSource> out = new ArrayList<GunPackSource>();
        if (new File(dir, "gun.json").isFile())
        {
            out.add(new Folder(dir));
            return out;
        }
        File[] kids = dir.listFiles();
        if (kids == null)
        {
            return out;
        }
        java.util.Arrays.sort(kids);
        for (File k : kids)
        {
            if (k.isDirectory() && new File(k, "gun.json").isFile())
            {
                out.add(new Folder(k));
            }
        }
        return out;
    }

    /** The shipped packages named in our jar's gunpacks/index.txt (one name a line, # comments). */
    public static List<GunPackSource> shipped()
    {
        return shipped(JAR_ROOT);
    }

    /** The packages named in ROOT/index.txt on the class path, each in ROOT/NAME/ (unit tests use a root of their own). */
    public static List<GunPackSource> shipped(String root)
    {
        List<GunPackSource> out = new ArrayList<GunPackSource>();
        InputStream in = GunPackSource.class.getResourceAsStream(root + "index.txt");
        if (in == null)
        {
            return out;
        }
        try
        {
            java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(in, "UTF-8"));
            for (String line; (line = r.readLine()) != null; )
            {
                String n = line.trim();
                if (!n.isEmpty() && !n.startsWith("#"))
                {
                    out.add(new ClassPath(root, n));
                }
            }
            r.close();
        }
        catch (IOException e)
        {
            // a broken index: no shipped packages
        }
        return out;
    }
}

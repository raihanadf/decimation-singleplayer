package net.decimation.fixes.gunpack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import net.decimation.fixes.Deci;
import net.decimation.fixes.LootPools;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Guns from gun packages, data driven (docs/superpowers/specs/2026-10-22-gun-converter-design.md, docs/gun_model_spec.md
 * section 7): no Java line per gun. A package is a folder with gun.json and the gun's model, texture, icon and
 * animations under assets/deci/ (the converter, tools/gunconv, writes them). Found at start, in this order (the first
 * valid package of a name wins, a later one is noted as shadowed):
 * 1. the folders in -Ddeciworldgen.gunpacks (a package folder, or a folder of package folders; several separated by
 *    the path separator or commas; `-Pgunpacks=DIR` for runClient / runServer, `devtest.py --live --gunpacks=DIR`);
 * 2. in the dev workspace only: build/gunpacks/ of the checkout (tools/guns/gunpack_standin.py writes there);
 * 3. shipped: our jar's assets/deciworldgen/gunpacks/NAME/, the names listed in gunpacks/index.txt.
 * Each gun is registered as deciworldgen:NAME with the like gun's stats unless overridden, its magazines (or one of its
 * own like a Decimation magazine), loot where the like gun is (Decimation's crates and our lists), the like gun's
 * sounds and, when its model has none, its hand poses; its attachment rows go to GunPackRows (sights from
 * points.railTop / rail, the mount, grips by GripFit, foregrips only on its gripRail). On the client the package's
 * files are served to Decimation through GunPackResources, installed before the first gun loads its model. Both sides
 * (a dedicated server registers the same items and rows); no work after startup.
 */
public final class GunPacks
{
    public static final String PROPERTY = "deciworldgen.gunpacks";
    /** The rows' name for barrel attachment shifts (client only, applied directly: no shipped table has them). */
    public static final String BARREL = "barrel";
    private static final List<Loaded> LOADED = new ArrayList<Loaded>();
    private static final List<String> PROBLEMS = new ArrayList<String>();
    private static final List<String> FOUND = new ArrayList<String>();
    private static final List<String> SHADOWED = new ArrayList<String>();
    private static final Map<String, PackModel> GRIP_MODELS = new HashMap<String, PackModel>();
    private static long nanos;

    private GunPacks()
    {
    }

    /** One package that registered, with what it was given (the dev test reads it). */
    public static final class Loaded
    {
        public final GunPack pack;
        public final GunPackSource source;
        public Item gun, magazine, like, likeMagazine;
        public PackModel model;
        /** Table lines added ("table: line"). */
        public final List<String> rows = new ArrayList<String>();
        /** Grip rows: grip -> {dx, dy, dz}. */
        public final Map<String, double[]> grips = new LinkedHashMap<String, double[]>();
        public final List<String> warnings = new ArrayList<String>();
        public int lootPools, magazinePools, ourLists, ourMagazineLists, sounds;
        /** Hand poses taken from the like gun (its model has no rhPos / lhPos). */
        public boolean hands;
        public long nanos;

        Loaded(GunPack pack, GunPackSource source)
        {
            this.pack = pack;
            this.source = source;
            warnings.addAll(pack.warnings);
        }
    }

    public static synchronized List<Loaded> loaded()
    {
        return new ArrayList<Loaded>(LOADED);
    }

    /** Packages refused, each "where: why". */
    public static synchronized List<String> problems()
    {
        return new ArrayList<String>(PROBLEMS);
    }

    /** Every package folder looked at ("name at where"). */
    public static synchronized List<String> found()
    {
        return new ArrayList<String>(FOUND);
    }

    /** Packages of a name found again later in the order (a dev folder's over the jar's), each "where: why". */
    public static synchronized List<String> shadowed()
    {
        return new ArrayList<String>(SHADOWED);
    }

    public static long nanos()
    {
        return nanos;
    }

    /** The dev workspace (deobfuscated run), where build/gunpacks and license "test" count. */
    static boolean devWorkspace()
    {
        try
        {
            return Boolean.TRUE.equals(net.minecraft.launchwrapper.Launch.blackboard.get("fml.deobfuscatedEnvironment"));
        }
        catch (Throwable t)
        {
            return false;
        }
    }

    /** The checkout's root (the folder with tools/devtest.py above the run folder), or null. */
    static File repoRoot()
    {
        File d = new File(".").getAbsoluteFile();
        for (int i = 0; i < 8 && d != null; i++, d = d.getParentFile())
        {
            if (new File(d, "tools/devtest.py").isFile())
            {
                return d;
            }
        }
        return null;
    }

    /** The package folders, in the order they count (see the class comment). */
    static List<GunPackSource> sources(boolean dev)
    {
        List<GunPackSource> out = new ArrayList<GunPackSource>();
        String prop = System.getProperty(PROPERTY, "");
        for (String part : prop.split("[," + java.util.regex.Pattern.quote(File.pathSeparator) + "]"))
        {
            if (!part.trim().isEmpty())
            {
                out.addAll(GunPackSource.scan(new File(part.trim())));
            }
        }
        File root = dev ? repoRoot() : null;
        if (root != null)
        {
            out.addAll(GunPackSource.scan(new File(root, "build/gunpacks")));
        }
        out.addAll(GunPackSource.shipped());
        return out;
    }

    /** preInit, both sides: after Decimation's guns (required-after:deci), before the attachment tables are read. */
    public static synchronized void register()
    {
        long t0 = System.nanoTime();
        boolean dev = devWorkspace();
        boolean client = FMLCommonHandler.instance().getSide().isClient();
        List<Loaded> parsed = new ArrayList<Loaded>();
        Set<String> names = new LinkedHashSet<String>();
        for (GunPackSource s : sources(dev))
        {
            FOUND.add(s.name + " at " + s.where());
            if (names.contains(s.name))
            {
                SHADOWED.add(s.where() + ": shadowed by the " + s.name + " found first (a dev folder before the jar)");
                continue;
            }
            try
            {
                GunPack p = GunPack.parse(s.text("gun.json"), dev);
                List<String> bad = new ArrayList<String>();
                if (!p.name.equals(s.name))
                {
                    bad.add("the folder is " + s.name + " but gun.json names " + p.name);
                }
                Loaded l = new Loaded(p, s);
                files(l, bad);
                if (!bad.isEmpty())
                {
                    throw new IllegalArgumentException(join(bad));
                }
                names.add(s.name);
                parsed.add(l);
            }
            catch (GunPack.Invalid e)
            {
                PROBLEMS.add(s.where() + ": " + e.getMessage());
            }
            catch (IOException e)
            {
                PROBLEMS.add(s.where() + ": " + e);
            }
            catch (RuntimeException e)
            {
                PROBLEMS.add(s.where() + ": " + e.getMessage());
            }
        }
        if (client && !parsed.isEmpty())
        {
            GunPacksClient.install(parsed);   // before any of these guns loads its model through the resource manager
        }
        List<String> sights = GunPackRows.sightNames(GunPacks.class.getResourceAsStream("/assets/deciworldgen/"
                                                                                        + GunPackRows.SIGHT_TABLE));
        for (Loaded l : parsed)
        {
            long t = System.nanoTime();
            try
            {
                registerOne(l, sights, client);
                LOADED.add(l);
            }
            catch (Exception e)
            {
                PROBLEMS.add(l.source.where() + ": " + (e.getMessage() != null ? e.getMessage() : e.toString()));
            }
            l.nanos = System.nanoTime() - t;
        }
        nanos = System.nanoTime() - t0;
        for (String p : PROBLEMS)
        {
            FMLLog.warning("[deciworldgen] gun package refused: %s", p);
        }
        for (String p : SHADOWED)
        {
            FMLLog.info("[deciworldgen] gun package %s", p);
        }
        for (Loaded l : LOADED)
        {
            FMLLog.info("[deciworldgen] gun package %s (%s, like %s) from %s: loot %d pools, %d sounds, %d grip rows, %d table lines,"
                        + " %.1f ms", l.pack.name, l.pack.display, l.pack.like, l.source.where(), l.lootPools, l.sounds,
                        l.grips.size(), l.rows.size(), l.nanos / 1e6);
            for (String w : l.warnings)
            {
                FMLLog.info("[deciworldgen] gun package %s: %s", l.pack.name, w);
            }
        }
        FMLLog.info("[deciworldgen] gun packages: %d registered, %d refused, %.0f ms", LOADED.size(), PROBLEMS.size(), nanos / 1e6);
    }

    /** The files a package must have (model, texture, icon, Fire and Reload1); Rack and SlideBack are optional. */
    private static void files(Loaded l, List<String> bad)
    {
        GunPack p = l.pack;
        for (String f : new String[] {p.modelPath(), p.texturePath(), p.iconPath(), p.animationPath("Fire"), p.animationPath("Reload1")})
        {
            if (!l.source.has("assets/deci/" + f))
            {
                bad.add("missing assets/deci/" + f);
            }
        }
        for (String kind : new String[] {"Rack", "SlideBack"})
        {
            if (!l.source.has("assets/deci/" + p.animationPath(kind)))
            {
                l.warnings.add("no " + kind + " animation (" + ("Rack".equals(kind) ? "racking after an empty reload plays nothing"
                    : "the slide does not stay back when empty") + ")");
            }
        }
    }

    private static boolean taken(String name)
    {
        return GameRegistry.findItem("deci", name) != null || GameRegistry.findItem("deciworldgen", name) != null;
    }

    private static void registerOne(Loaded l, List<String> sights, boolean client) throws IOException
    {
        GunPack p = l.pack;
        // everything is checked before the first item exists: a refused package leaves nothing behind
        Item like = GameRegistry.findItem("deci", p.like);
        if (!Deci.isGun(like))
        {
            throw new IllegalArgumentException("like \"" + p.like + "\" is not a Decimation gun");
        }
        if (taken(p.name))
        {
            throw new IllegalArgumentException("the name " + p.name + " is taken by an item already");
        }
        String likeCat = Deci.gunCategory(like);
        if (!p.category.equals(likeCat))
        {
            l.warnings.add("a " + p.category + " like the " + likeCat + " " + p.like + ": attachments follow " + p.category);
        }
        Item likeMag = null;
        if (p.magazine != null)
        {
            likeMag = GameRegistry.findItem("deci", p.magazine.like);
            if (!Deci.isMagazine(likeMag))
            {
                throw new IllegalArgumentException("magazine.like \"" + p.magazine.like + "\" is not a Decimation magazine");
            }
            if (p.magazine.icon != null && !Deci.isMagazine(GameRegistry.findItem("deci", p.magazine.icon)))
            {
                throw new IllegalArgumentException("magazine.icon \"" + p.magazine.icon + "\" is not a Decimation magazine");
            }
            if (taken(p.magazine.name))
            {
                throw new IllegalArgumentException("the magazine name " + p.magazine.name + " is taken by an item already");
            }
        }
        l.model = PackModel.parse(l.source.text("assets/deci/" + p.modelPath()));
        double[] fp = l.model.header("flamePos");
        if (fp == null)
        {
            l.warnings.add("the model has no flamePos: Decimation draws the flash and barrel attachments at (20, -4, 0)");
        }
        else if (Math.abs(fp[0] - p.points.flame[0]) > 0.05 || Math.abs(fp[1] - p.points.flame[1]) > 0.05
                 || Math.abs(fp[2] - p.points.flame[2]) > 0.05)
        {
            l.warnings.add(String.format(java.util.Locale.ROOT, "points.flame (%.2f, %.2f, %.2f) is not the model's flamePos"
                + " (%.2f, %.2f, %.2f): Decimation uses the model's", p.points.flame[0], p.points.flame[1], p.points.flame[2],
                fp[0], fp[1], fp[2]));
        }
        Item[] mags;
        if (p.magazine == null)
        {
            mags = Deci.gunMagazines(like).toArray(new Item[0]);
        }
        else
        {
            l.likeMagazine = likeMag;
            l.magazine = Deci.newMagazine(p.magazine.name, p.magazine.capacity, p.magazine.like,
                                          p.magazine.icon != null ? p.magazine.icon : p.magazine.like);
            mags = new Item[] {l.magazine};
        }
        l.like = like;
        l.gun = Deci.newGunLike(p.name, p.category, like, p.stats, mags);
        l.lootPools = Deci.addLootLike(like, new ItemStack(l.gun, 1));
        if (l.magazine != null)
        {
            l.magazinePools = Deci.addLootLike(likeMag, new ItemStack(l.magazine, 2));
        }
        if (client)
        {
            l.sounds = GunPacksClient.sounds(p, like);
            l.hands = GunPacksClient.hands(l.gun, like, l.model);
            for (Map.Entry<String, double[]> e : p.points.barrelShift.entrySet())
            {
                double[] d = e.getValue();
                if (GunPacksClient.barrel(l.gun, e.getKey(), d))
                {
                    l.rows.add(BARREL + ": " + GunPackRows.gripLine(p.name, e.getKey(), d));
                }
                else
                {
                    l.warnings.add("barrelShift " + e.getKey() + ": not a Decimation attachment, left out");
                }
            }
        }
        for (String line : GunPackRows.sightLines(p, sights))
        {
            add(l, GunPackRows.SIGHT_TABLE, line);
        }
        String mount = GunPackRows.mountLine(p);
        if (mount != null)
        {
            add(l, GunPackRows.MOUNT_TABLE, mount);
        }
        grips(l);
    }

    private static void add(Loaded l, String table, String line)
    {
        GunPackRows.add(table, line);
        l.rows.add(table + ": " + line);
    }

    /** Grip rows as tools/guns/gripfit.py would write them; foregrips only on a rifle with a gripRail, their seat on it. */
    private static void grips(Loaded l)
    {
        GunPack p = l.pack;
        GripFit.Gun gun = new GripFit.Gun(l.model, p.category);
        for (String grip : GripFit.GRIPS)
        {
            boolean rail = GripFit.RAIL_GRIPS.contains(grip);
            if (rail && !p.railed())
            {
                continue;
            }
            PackModel gm = gripModel(grip);
            double[] d = gm == null ? null : gun.fit(grip, GripFit.rawGrip(gm, grip), rail ? p.points.gripRail : null);
            if (d == null)
            {
                if (!rail)
                {
                    l.warnings.add("no place under or beside the gun for the " + grip + ": it stays at Decimation's spot");
                    continue;
                }
                l.warnings.add("no flat seat for the " + grip + " on the gripRail: drawn at Decimation's spot, still allowed");
                d = new double[] {0, 0, 0};
            }
            l.grips.put(grip, d);
            add(l, GunPackRows.GRIP_TABLE, GunPackRows.gripLine(p.name, grip, d));
        }
    }

    /** A grip slot attachment's model from the class path (Decimation's jar, our RK-1), cached; null when missing. */
    static synchronized PackModel gripModel(String grip)
    {
        if (!GRIP_MODELS.containsKey(grip))
        {
            PackModel m = null;
            try
            {
                java.io.InputStream in = GunPacks.class.getResourceAsStream("/assets/deci/models/attachments/grip/" + grip + ".bmodel");
                if (in != null)
                {
                    java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    for (int n; (n = in.read(buf)) > 0; )
                    {
                        b.write(buf, 0, n);
                    }
                    in.close();
                    m = PackModel.parse(new String(b.toByteArray(), "ISO-8859-1"));
                }
            }
            catch (IOException e)
            {
                m = null;
            }
            GRIP_MODELS.put(grip, m);
        }
        return GRIP_MODELS.get(grip);
    }

    /** postInit (our loot lists are loaded then): each gun and its own magazine join the lists that name the like ones. */
    public static synchronized void postInit()
    {
        for (Loaded l : LOADED)
        {
            l.ourLists = LootPools.addLike(Item.itemRegistry.getNameForObject(l.like), Item.itemRegistry.getNameForObject(l.gun));
            if (l.magazine != null)
            {
                l.ourMagazineLists = LootPools.addLike(Item.itemRegistry.getNameForObject(l.likeMagazine),
                                                       Item.itemRegistry.getNameForObject(l.magazine));
            }
        }
        if (!LOADED.isEmpty())
        {
            StringBuilder s = new StringBuilder();
            for (Loaded l : LOADED)
            {
                s.append(s.length() > 0 ? ", " : "").append(l.pack.name).append(' ').append(l.ourLists);
            }
            FMLLog.info("[deciworldgen] gun packages in our loot lists: %s", s);
        }
    }

    private static String join(List<String> p)
    {
        StringBuilder s = new StringBuilder();
        for (String x : p)
        {
            s.append(s.length() > 0 ? "; " : "").append(x);
        }
        return s.toString();
    }
}

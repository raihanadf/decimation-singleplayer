package net.decimation.fixes.gunpack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * One gun package's gun.json (docs/superpowers/specs/2026-10-22-gun-converter-design.md "The gun package"): the
 * converter (tools/gunconv) writes it next to the gun's model, texture, icon and animations; GunPacks registers the gun
 * from it. Parsed and checked here, every problem listed at once ({@link Invalid}); fields that are not known are
 * warnings, so either side of the contract may add optional fields. Plain Java (unit test GunPackTest).
 *
 * Contract fields: name, display, category, like, magazine ("like" or {name, capacity, like}), stats (optional
 * overrides of the like gun's: damage, rpm, pitch, yaw, recovery, slowdown, auto), points {flame, railTop, rail,
 * gripRail, mount}, source {file, author, url, license}. Optional fields of the game side: magazine.icon,
 * magazine.display, points.sightShift.
 */
public final class GunPack
{
    /** Decimation's WeaponCategory names a gun can have (flamethrower is unused, all is no gun's). */
    public static final List<String> CATEGORIES = Collections.unmodifiableList(
        Arrays.asList("rifle", "smg", "mg", "shotgun", "pistol", "revolver", "rocket", "crossbow"));
    public static final List<String> MOUNTS = Collections.unmodifiableList(Arrays.asList("rail", "side", "slide"));
    private static final List<String> TOP = Arrays.asList("name", "display", "category", "like", "magazine", "stats", "points",
                                                          "source");
    private static final List<String> STATS = Arrays.asList("damage", "rpm", "pitch", "yaw", "recovery", "slowdown", "auto");
    private static final List<String> POINTS = Arrays.asList("flame", "railTop", "rail", "gripRail", "mount", "sightShift",
                                                             "barrelShift");
    private static final List<String> MAGAZINE = Arrays.asList("name", "capacity", "like", "icon", "display");
    private static final List<String> SOURCE = Arrays.asList("file", "author", "url", "license");

    public String name, display, category, like;
    /** The gun's own magazine, or null: it loads the like gun's magazines. */
    public Magazine magazine;
    public final Stats stats = new Stats();
    public final Points points = new Points();
    public final Source source = new Source();
    /** Fields not known to this loader and similar remarks: logged, never fatal. */
    public final List<String> warnings = new ArrayList<String>();

    public static final class Magazine
    {
        public String name, like, icon, display;
        public int capacity;
    }

    /** Overrides of the like gun's stats; null = the like gun's. damage as Decimation registers it (halved in game). */
    public static final class Stats
    {
        public Integer damage;
        public Float rpm, pitch, yaw, recovery;
        public Double slowdown;
        public Boolean auto;

        public boolean any()
        {
            return damage != null || rpm != null || pitch != null || yaw != null || recovery != null || slowdown != null
                || auto != null;
        }
    }

    public static final class Points
    {
        public double[] flame;
        public Double railTop;
        /** Top rail {from, to} along x, or null. */
        public double[] rail;
        /** Bottom rail under the handguard {from, to} along x, or null (no foregrip). */
        public double[] gripRail;
        public String mount;
        /** Optional: sights moved forward (+) or back (-) by this, instead of the shift found from "rail". */
        public Double sightShift;
        /**
         * Optional: barrel attachments moved on this gun, name -> {dx, dy, dz} model units (Decimation places them by a
         * formula of flamePos that misses short muzzles: the MAC-10's smgSuppressor -2.37, -0.16, 0; skill decimation-gun
         * lesson 6). Empty: Decimation's place.
         */
        public final Map<String, double[]> barrelShift = new java.util.LinkedHashMap<String, double[]>();
    }

    public static final class Source
    {
        public String file, author, url, license;
    }

    /** gun.json could not be used: every problem found. */
    public static final class Invalid extends Exception
    {
        public final List<String> problems;

        Invalid(List<String> problems)
        {
            super(join(problems));
            this.problems = problems;
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

    /** Model, texture, icon and animation paths inside the package (and in Decimation's deci domain without "assets/deci/"). */
    public String modelPath()
    {
        return "models/guns/" + category + "/" + name + ".bmodel";
    }

    public String textureFolder()
    {
        return "textures/model/guns/" + category + "/" + name + "/";
    }

    public String texturePath()
    {
        return textureFolder() + name + ".png";
    }

    public String iconPath()
    {
        return "textures/items/gun/" + category + "/" + name + ".png";
    }

    public String animationFolder()
    {
        return "animations/" + name + "/";
    }

    /** kind: Fire, Rack, Reload1, SlideBack. */
    public String animationPath(String kind)
    {
        return animationFolder() + name + kind + ".anib";
    }

    /** True when the sight rows come out of railTop: a gun with a sight mount. */
    public boolean takesSights()
    {
        return points.mount != null && points.railTop != null;
    }

    /** True when a foregrip or the RK-1 may go on this gun: a bottom rail, and a rifle (Decimation's Foregrip rule). */
    public boolean railed()
    {
        return points.gripRail != null && "rifle".equals(category);
    }

    /**
     * Reads and checks gun.json. dev: the dev workspace, where license "test" is accepted (local test packages made from
     * Decimation's own art, never shipped).
     */
    public static GunPack parse(String json, boolean dev) throws Invalid
    {
        List<String> bad = new ArrayList<String>();
        GunPack p = new GunPack();
        JsonObject o;
        try
        {
            JsonElement e = new JsonParser().parse(json);
            if (!e.isJsonObject())
            {
                throw new Invalid(Collections.singletonList("gun.json is not a JSON object"));
            }
            o = e.getAsJsonObject();
        }
        catch (Invalid e)
        {
            throw e;
        }
        catch (RuntimeException e)
        {
            throw new Invalid(Collections.singletonList("gun.json is not valid JSON: " + e.getMessage()));
        }
        unknown(o, TOP, "", p.warnings);
        p.name = text(o, "name", bad, true);
        if (p.name != null && !p.name.matches("[a-z][a-z0-9_]{1,31}"))
        {
            bad.add("name \"" + p.name + "\": lower case letters, digits and _, 2 to 32, starting with a letter");
        }
        p.display = text(o, "display", bad, true);
        if (p.display != null && (p.display.trim().isEmpty() || p.display.length() > 64))
        {
            bad.add("display: 1 to 64 characters");
        }
        p.category = text(o, "category", bad, true);
        if (p.category != null && !CATEGORIES.contains(p.category))
        {
            bad.add("category \"" + p.category + "\": one of " + CATEGORIES);
        }
        p.like = text(o, "like", bad, true);
        if (p.like != null && !p.like.matches("[A-Za-z0-9_]+"))
        {
            bad.add("like \"" + p.like + "\": a Decimation gun name");
        }
        magazine(o, p, bad);
        stats(o, p, bad);
        points(o, p, bad);
        source(o, p, bad, dev);
        if (!bad.isEmpty())
        {
            throw new Invalid(bad);
        }
        return p;
    }

    private static void magazine(JsonObject o, GunPack p, List<String> bad)
    {
        JsonElement m = o.get("magazine");
        if (m == null || m.isJsonNull())
        {
            bad.add("magazine: missing (\"like\" or {name, capacity, like})");
            return;
        }
        if (m.isJsonPrimitive() && m.getAsJsonPrimitive().isString())
        {
            if (!"like".equals(m.getAsString()))
            {
                bad.add("magazine \"" + m.getAsString() + "\": \"like\" or {name, capacity, like}");
            }
            return;
        }
        if (!m.isJsonObject())
        {
            bad.add("magazine: \"like\" or {name, capacity, like}");
            return;
        }
        JsonObject mo = m.getAsJsonObject();
        unknown(mo, MAGAZINE, "magazine.", p.warnings);
        Magazine g = new Magazine();
        g.name = text(mo, "name", bad, true);
        if (g.name != null && !g.name.matches("[A-Za-z][A-Za-z0-9_]{1,40}"))
        {
            bad.add("magazine.name \"" + g.name + "\": letters, digits and _, 2 to 41");
        }
        Double cap = number(mo, "capacity", bad, true);
        if (cap != null && (cap != Math.rint(cap) || cap < 1 || cap > 500))
        {
            bad.add("magazine.capacity: a whole number 1 to 500");
        }
        g.capacity = cap == null ? 0 : cap.intValue();
        g.like = text(mo, "like", bad, true);
        g.icon = text(mo, "icon", bad, false);
        g.display = text(mo, "display", bad, false);
        for (String k : new String[] {g.like, g.icon})
        {
            if (k != null && !k.matches("[A-Za-z0-9_]+"))
            {
                bad.add("magazine: \"" + k + "\" is not a Decimation magazine name");
            }
        }
        p.magazine = g;
    }

    private static void stats(JsonObject o, GunPack p, List<String> bad)
    {
        JsonElement s = o.get("stats");
        if (s == null || s.isJsonNull())
        {
            return;
        }
        if (!s.isJsonObject())
        {
            bad.add("stats: an object of overrides");
            return;
        }
        JsonObject so = s.getAsJsonObject();
        for (Map.Entry<String, JsonElement> e : so.entrySet())
        {
            if (!STATS.contains(e.getKey()))
            {
                p.warnings.add("stats." + e.getKey() + " is not a stat this loader overrides (" + STATS + "): ignored");
            }
        }
        Double d = range(so, "damage", 1, 1000, bad);
        if (d != null && d != Math.rint(d))
        {
            bad.add("stats.damage: a whole number");
        }
        p.stats.damage = d == null ? null : d.intValue();
        p.stats.rpm = toFloat(range(so, "rpm", 1, 3000, bad));
        p.stats.pitch = toFloat(range(so, "pitch", 0, 100, bad));
        p.stats.yaw = toFloat(range(so, "yaw", 0, 100, bad));
        p.stats.recovery = toFloat(range(so, "recovery", 0, 100, bad));
        p.stats.slowdown = range(so, "slowdown", 0, 1, bad);
        JsonElement auto = so.get("auto");
        if (auto != null && !auto.isJsonNull())
        {
            if (auto.isJsonPrimitive() && auto.getAsJsonPrimitive().isBoolean())
            {
                p.stats.auto = auto.getAsBoolean();
            }
            else
            {
                bad.add("stats.auto: true or false");
            }
        }
    }

    private static void points(JsonObject o, GunPack p, List<String> bad)
    {
        JsonElement e = o.get("points");
        if (e == null || !e.isJsonObject())
        {
            bad.add("points: missing ({flame, railTop, rail, gripRail, mount})");
            return;
        }
        JsonObject po = e.getAsJsonObject();
        unknown(po, POINTS, "points.", p.warnings);
        p.points.flame = numbers(po, "flame", 3, bad, true);
        p.points.railTop = number(po, "railTop", bad, false);
        p.points.rail = span(po, "rail", bad);
        p.points.gripRail = span(po, "gripRail", bad);
        p.points.mount = text(po, "mount", bad, false);
        if (p.points.mount != null && !MOUNTS.contains(p.points.mount))
        {
            bad.add("points.mount \"" + p.points.mount + "\": one of " + MOUNTS + " or null");
        }
        if (p.points.mount != null && p.points.railTop == null)
        {
            bad.add("points.railTop: needed with a mount (the height the sights sit on)");
        }
        p.points.sightShift = number(po, "sightShift", bad, false);
        JsonElement barrel = po.get("barrelShift");
        if (barrel != null && !barrel.isJsonNull())
        {
            if (!barrel.isJsonObject())
            {
                bad.add("points.barrelShift: {attachment: [dx, dy, dz]}");
            }
            else
            {
                for (Map.Entry<String, JsonElement> entry : barrel.getAsJsonObject().entrySet())
                {
                    double[] d = numbers(barrel.getAsJsonObject(), entry.getKey(), 3, bad, true);
                    if (!entry.getKey().matches("[A-Za-z0-9_]+"))
                    {
                        bad.add("points.barrelShift: \"" + entry.getKey() + "\" is not an attachment name");
                    }
                    else if (d != null)
                    {
                        p.points.barrelShift.put(entry.getKey(), d);
                    }
                }
            }
        }
        if (p.points.gripRail != null && !"rifle".equals(p.category))
        {
            p.warnings.add("points.gripRail on a " + p.category + ": Decimation's Foregrip and the RK-1 fit rifles only");
        }
    }

    private static void source(JsonObject o, GunPack p, List<String> bad, boolean dev)
    {
        JsonElement e = o.get("source");
        if (e == null || !e.isJsonObject())
        {
            bad.add("source: missing ({file, author, url, license}: CC0, CC BY or own work only)");
            return;
        }
        JsonObject so = e.getAsJsonObject();
        unknown(so, SOURCE, "source.", p.warnings);
        p.source.file = text(so, "file", bad, true);
        p.source.author = text(so, "author", bad, true);
        if (p.source.author != null && p.source.author.trim().isEmpty())
        {
            bad.add("source.author: empty (CC BY needs the credit)");
        }
        p.source.url = text(so, "url", bad, false);
        p.source.license = text(so, "license", bad, true);
        String l = p.source.license == null ? "" : p.source.license.trim();
        String u = l.toUpperCase(java.util.Locale.ROOT);
        boolean cc = u.startsWith("CC0") || u.startsWith("CC BY") || u.startsWith("CC-BY");
        boolean own = "own".equals(l);
        boolean test = "test".equals(l);
        if (p.source.license != null && !cc && !own && !test)
        {
            bad.add("source.license \"" + l + "\": CC0, CC BY (credit kept) or own work only (spec \"Source models\")");
        }
        if (test && !dev)
        {
            bad.add("source.license \"test\": local dev packages only, never in a shipped game");
        }
    }

    private static void unknown(JsonObject o, List<String> known, String prefix, List<String> warnings)
    {
        for (Map.Entry<String, JsonElement> e : o.entrySet())
        {
            if (!known.contains(e.getKey()))
            {
                warnings.add(prefix + e.getKey() + " is not known to this loader: ignored");
            }
        }
    }

    private static String text(JsonObject o, String key, List<String> bad, boolean required)
    {
        JsonElement e = o.get(key);
        if (e == null || e.isJsonNull())
        {
            if (required)
            {
                bad.add(key + ": missing");
            }
            return null;
        }
        if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString())
        {
            bad.add(key + ": a string");
            return null;
        }
        return e.getAsString();
    }

    private static Double number(JsonObject o, String key, List<String> bad, boolean required)
    {
        JsonElement e = o.get(key);
        if (e == null || e.isJsonNull())
        {
            if (required)
            {
                bad.add(key + ": missing");
            }
            return null;
        }
        if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber())
        {
            bad.add(key + ": a number");
            return null;
        }
        double d = e.getAsDouble();
        if (Double.isNaN(d) || Double.isInfinite(d))
        {
            bad.add(key + ": a finite number");
            return null;
        }
        return d;
    }

    private static Double range(JsonObject o, String key, double lo, double hi, List<String> bad)
    {
        Double d = number(o, key, bad, false);
        if (d != null && (d < lo || d > hi))
        {
            bad.add("stats." + key + ": " + lo + " to " + hi);
            return null;
        }
        return d;
    }

    private static Float toFloat(Double d)
    {
        return d == null ? null : d.floatValue();
    }

    private static double[] numbers(JsonObject o, String key, int n, List<String> bad, boolean required)
    {
        JsonElement e = o.get(key);
        if (e == null || e.isJsonNull())
        {
            if (required)
            {
                bad.add("points." + key + ": missing ([" + n + " numbers])");
            }
            return null;
        }
        if (!e.isJsonArray() || e.getAsJsonArray().size() != n)
        {
            bad.add("points." + key + ": [" + n + " numbers]");
            return null;
        }
        JsonArray a = e.getAsJsonArray();
        double[] out = new double[n];
        for (int i = 0; i < n; i++)
        {
            JsonElement x = a.get(i);
            if (!x.isJsonPrimitive() || !((JsonPrimitive) x).isNumber())
            {
                bad.add("points." + key + ": [" + n + " numbers]");
                return null;
            }
            out[i] = x.getAsDouble();
        }
        return out;
    }

    private static double[] span(JsonObject o, String key, List<String> bad)
    {
        double[] s = numbers(o, key, 2, bad, false);
        if (s != null && !(s[0] < s[1]))
        {
            bad.add("points." + key + ": [from, to] with from < to");
            return null;
        }
        return s;
    }
}

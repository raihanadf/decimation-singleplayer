package net.decimation.fixes.gunpack;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * gun.json, the contract between the gun converter and the game (docs/superpowers/specs/2026-10-22-gun-converter-design.md
 * "The gun package"): what the loader reads, and every way a package is refused, all problems at once.
 */
class GunPackTest
{
    /** A rifle package as the converter writes it (the contract's fields only). */
    static final String RIFLE = "{\"name\": \"mk47\", \"display\": \"Mk47 Mutant\", \"category\": \"rifle\", \"like\": \"m4a4\","
        + " \"magazine\": \"like\","
        + " \"points\": {\"flame\": [19.15, -3.4, -0.15], \"railTop\": -3.6, \"rail\": [-1.0, 14.7], \"gripRail\": [8.5, 14.7],"
        + " \"mount\": \"rail\"},"
        + " \"source\": {\"file\": \"mk47.glb\", \"author\": \"Some Author\", \"url\": \"https://example.org/mk47\","
        + " \"license\": \"CC BY 4.0\"}}";

    static String with(String json, String from, String to)
    {
        if (!json.contains(from))
        {
            throw new IllegalArgumentException("no " + from);
        }
        return json.replace(from, to);
    }

    static List<String> problems(String json, boolean dev)
    {
        GunPack.Invalid e = assertThrows(GunPack.Invalid.class, () -> GunPack.parse(json, dev));
        return e.problems;
    }

    static boolean mentions(List<String> problems, String text)
    {
        for (String p : problems)
        {
            if (p.contains(text))
            {
                return true;
            }
        }
        return false;
    }

    @Test
    void theContractsRifleReadsWithoutWarnings() throws Exception
    {
        GunPack p = GunPack.parse(RIFLE, false);
        assertEquals("mk47", p.name);
        assertEquals("Mk47 Mutant", p.display);
        assertEquals("rifle", p.category);
        assertEquals("m4a4", p.like);
        assertNull(p.magazine);                       // the like gun's magazines
        assertFalse(p.stats.any());
        assertArrayEquals(new double[] {19.15, -3.4, -0.15}, p.points.flame, 1e-9);
        assertEquals(-3.6, p.points.railTop, 1e-9);
        assertArrayEquals(new double[] {-1.0, 14.7}, p.points.rail, 1e-9);
        assertArrayEquals(new double[] {8.5, 14.7}, p.points.gripRail, 1e-9);
        assertEquals("rail", p.points.mount);
        assertEquals("CC BY 4.0", p.source.license);
        assertTrue(p.warnings.isEmpty(), p.warnings.toString());
        assertTrue(p.railed());
        assertTrue(p.takesSights());
        assertEquals("models/guns/rifle/mk47.bmodel", p.modelPath());
        assertEquals("textures/model/guns/rifle/mk47/mk47.png", p.texturePath());
        assertEquals("textures/items/gun/rifle/mk47.png", p.iconPath());
        assertEquals("animations/mk47/mk47Reload1.anib", p.animationPath("Reload1"));
    }

    @Test
    void anOwnMagazineAndStatOverrides() throws Exception
    {
        String json = with(RIFLE, "\"magazine\": \"like\"", "\"magazine\": {\"name\": \"mk47Mag\", \"capacity\": 30,"
            + " \"like\": \"ak74Mag\", \"icon\": \"akmMag\"}, \"stats\": {\"damage\": 15, \"rpm\": 700, \"pitch\": 6.5,"
            + " \"yaw\": 0.3, \"recovery\": 3, \"slowdown\": 0.12, \"auto\": true}");
        GunPack p = GunPack.parse(json, false);
        assertEquals("mk47Mag", p.magazine.name);
        assertEquals(30, p.magazine.capacity);
        assertEquals("ak74Mag", p.magazine.like);
        assertEquals("akmMag", p.magazine.icon);
        assertEquals(Integer.valueOf(15), p.stats.damage);
        assertEquals(700f, p.stats.rpm, 1e-6);
        assertEquals(6.5f, p.stats.pitch, 1e-6);
        assertEquals(0.3f, p.stats.yaw, 1e-6);
        assertEquals(3f, p.stats.recovery, 1e-6);
        assertEquals(0.12, p.stats.slowdown, 1e-9);
        assertEquals(Boolean.TRUE, p.stats.auto);
    }

    /** Converter milestone 4: a mesh gun names its mesh.dmesh beside gun.json; no "mesh" is a box gun. */
    @Test
    void aMeshGunNamesItsMeshFile() throws Exception
    {
        GunPack p = GunPack.parse(with(RIFLE, "\"magazine\": \"like\",", "\"magazine\": \"like\", \"mesh\": \"mesh.dmesh\","), false);
        assertEquals("mesh.dmesh", p.mesh);
        assertTrue(p.warnings.isEmpty(), p.warnings.toString());
        assertNull(GunPack.parse(RIFLE, false).mesh, "a box gun has none");
        for (String bad : new String[] {"../mesh.dmesh", "assets/mesh.dmesh", "mesh.obj", "Mesh.dmesh"})
        {
            assertTrue(mentions(problems(with(RIFLE, "\"magazine\": \"like\",", "\"magazine\": \"like\", \"mesh\": \"" + bad + "\","),
                                         false), "mesh"), bad);
        }
    }

    @Test
    void fieldsNotKnownAreWarningsNeverErrors() throws Exception
    {
        String json = with(RIFLE, "\"like\": \"m4a4\",", "\"like\": \"m4a4\", \"parts\": 180, \"stats\": {\"spread\": 1.2},");
        json = with(json, "\"mount\": \"rail\"", "\"mount\": \"rail\", \"magWell\": [7.0, 9.0]");
        GunPack p = GunPack.parse(json, false);
        assertEquals(3, p.warnings.size(), p.warnings.toString());
        assertTrue(mentions(p.warnings, "parts"));
        assertTrue(mentions(p.warnings, "stats.spread"));
        assertTrue(mentions(p.warnings, "points.magWell"));
    }

    @Test
    void everyMissingFieldAtOnce()
    {
        List<String> p = problems("{}", false);
        for (String key : new String[] {"name", "display", "category", "like", "magazine", "points", "source"})
        {
            assertTrue(mentions(p, key), key + " in " + p);
        }
    }

    @Test
    void badValuesAreRefused()
    {
        assertTrue(mentions(problems(with(RIFLE, "\"mk47\"", "\"Mk 47\""), false), "name"));
        assertTrue(mentions(problems(with(RIFLE, "\"rifle\"", "\"flamethrower\""), false), "category"));
        assertTrue(mentions(problems(with(RIFLE, "\"magazine\": \"like\"", "\"magazine\": \"stanag\""), false), "magazine"));
        assertTrue(mentions(problems(with(RIFLE, "\"magazine\": \"like\"",
                                          "\"magazine\": {\"name\": \"mk47Mag\", \"capacity\": 0, \"like\": \"m4a4Mag\"}"), false),
                            "capacity"));
        assertTrue(mentions(problems(with(RIFLE, "[19.15, -3.4, -0.15]", "[19.15, -3.4]"), false), "flame"));
        assertTrue(mentions(problems(with(RIFLE, "[8.5, 14.7]", "[14.7, 8.5]"), false), "gripRail"));
        assertTrue(mentions(problems(with(RIFLE, "\"mount\": \"rail\"", "\"mount\": \"top\""), false), "mount"));
        assertTrue(mentions(problems(with(RIFLE, "\"railTop\": -3.6,", ""), false), "railTop"));
        List<String> stats = problems(with(RIFLE, "\"magazine\": \"like\"",
                                           "\"magazine\": \"like\", \"stats\": {\"slowdown\": 2, \"auto\": \"yes\", \"damage\": 7.5}"),
                                      false);
        assertTrue(mentions(stats, "slowdown"), stats.toString());
        assertTrue(mentions(stats, "auto"), stats.toString());
        assertTrue(mentions(stats, "damage"), stats.toString());
    }

    @Test
    void onlyModelsWeMayShip()
    {
        assertTrue(mentions(problems(with(RIFLE, "\"CC BY 4.0\"", "\"GPL-3.0\""), false), "license"));
        assertTrue(mentions(problems(with(RIFLE, "\"CC BY 4.0\"", "\"Battlestate Games\""), false), "license"));
        assertTrue(mentions(problems(with(RIFLE, "\"Some Author\"", "\"  \""), false), "author"));
        // "test": a local package made from Decimation's own art, only in the dev workspace
        assertTrue(mentions(problems(with(RIFLE, "\"CC BY 4.0\"", "\"test\""), false), "dev"));
    }

    @Test
    void licencesThatPass() throws Exception
    {
        for (String l : new String[] {"CC0", "CC0 1.0", "CC BY 4.0", "CC-BY-3.0", "own"})
        {
            assertEquals(l, GunPack.parse(with(RIFLE, "\"CC BY 4.0\"", "\"" + l + "\""), false).source.license);
        }
        assertEquals("test", GunPack.parse(with(RIFLE, "\"CC BY 4.0\"", "\"test\""), true).source.license);
    }

    @Test
    void barrelShiftsOfTheGameSide() throws Exception
    {
        GunPack p = GunPack.parse(with(RIFLE, "\"mount\": \"rail\"",
                                       "\"mount\": \"rail\", \"barrelShift\": {\"arSuppressor\": [-0.73, 0, 0]}"), false);
        assertArrayEquals(new double[] {-0.73, 0, 0}, p.points.barrelShift.get("arSuppressor"), 1e-9);
        assertTrue(p.warnings.isEmpty(), p.warnings.toString());
        assertTrue(mentions(problems(with(RIFLE, "\"mount\": \"rail\"",
                                          "\"mount\": \"rail\", \"barrelShift\": {\"arSuppressor\": [1]}"), false), "arSuppressor"));
    }

    @Test
    void notJsonAtAll()
    {
        assertTrue(mentions(problems("[1, 2]", false), "object"));
        assertTrue(mentions(problems("{\"name\": ", false), "JSON"));
    }

    @Test
    void anSmgTakesNoForegripAndNoMountMeansNoSights() throws Exception
    {
        String json = with(RIFLE, "\"rifle\"", "\"smg\"");
        json = with(json, "\"mount\": \"rail\"", "\"mount\": null");
        GunPack p = GunPack.parse(json, false);
        assertFalse(p.railed());
        assertFalse(p.takesSights());
        assertTrue(mentions(p.warnings, "gripRail"));
    }
}

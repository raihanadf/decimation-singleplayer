package net.decimation.fixes.gunpack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Where packages are found: the shipped ones by name from an index on the class path (test resources hold one under a
 * root of their own, assets/deciworldgen/gunpacks_test, never shipped), folders of a dev's packages.
 */
class GunPackSourceTest
{
    static final String TEST_ROOT = "/assets/deciworldgen/gunpacks_test/";

    @Test
    void theShippedIndexListsNoneYet()
    {
        assertTrue(GunPackSource.shipped().isEmpty(), "our jar ships no package until a release adds one");
    }

    @Test
    void aPackageOnTheClassPath() throws Exception
    {
        List<GunPackSource> s = GunPackSource.shipped(TEST_ROOT);
        assertEquals(1, s.size());
        GunPackSource tiny = s.get(0);
        assertEquals("tiny", tiny.name);
        assertEquals("jar assets/deciworldgen/gunpacks_test/tiny", tiny.where());
        GunPack p = GunPack.parse(tiny.text("gun.json"), false);
        assertEquals("tiny", p.name);
        assertTrue(tiny.has("assets/deci/" + p.modelPath()));
        assertTrue(tiny.has("assets/deci/" + p.iconPath()));
        assertTrue(tiny.has("assets/deci/" + p.animationPath("Reload1")));
        assertFalse(tiny.has("assets/deci/" + p.animationPath("Rack")));
        assertThrows(FileNotFoundException.class, () -> tiny.open("assets/deci/" + p.animationPath("SlideBack")));
        assertEquals(1, PackModel.parse(tiny.text("assets/deci/" + p.modelPath())).parts.size());
        assertTrue(tiny.list("assets/deci/" + p.animationFolder()).isEmpty(), "a jar cannot be listed");
    }

    @Test
    void foldersOfPackages() throws Exception
    {
        File dir = Files.createTempDirectory("gunpacks").toFile();
        for (String n : new String[] {"zeta", "alpha"})
        {
            File k = new File(dir, n);
            assertTrue(new File(k, "assets/deci/animations/" + n).mkdirs());
            FileWriter w = new FileWriter(new File(k, "gun.json"));
            w.write("{}");
            w.close();
            new FileWriter(new File(k, "assets/deci/animations/" + n + "/" + n + "Fire.anib")).close();
        }
        assertTrue(new File(dir, "notapackage").mkdirs());
        List<GunPackSource> s = GunPackSource.scan(dir);
        assertEquals(2, s.size());
        assertEquals("alpha", s.get(0).name);
        assertEquals("zeta", s.get(1).name);
        assertEquals(1, s.get(0).list("assets/deci/animations/alpha/").size());
        assertEquals(1, GunPackSource.scan(new File(dir, "alpha")).size());   // a package folder itself
        assertTrue(GunPackSource.scan(new File(dir, "missing")).isEmpty());
    }
}

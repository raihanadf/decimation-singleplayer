package net.decimation.fixes.gunpack;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

/**
 * The .bmodel reader of gun packages measures a model as tools/guns/study.py does (the numbers below are study.py's for
 * the same files, 22 Oktober 2026).
 */
class PackModelTest
{
    static String resource(String path) throws Exception
    {
        InputStream in = PackModelTest.class.getResourceAsStream(path);
        if (in == null)
        {
            return null;
        }
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        for (int n; (n = in.read(buf)) > 0; )
        {
            b.write(buf, 0, n);
        }
        in.close();
        return new String(b.toByteArray(), "ISO-8859-1");
    }

    private static final String ZERO = "{0F,0F,0F},{0F,0F,0F},{0F,0F,0F},{0F,0F,0F},{0F,0F,0F},{0F,0F,0F},{0F,0F,0F},{0F,0F,0F}";

    @Test
    void aPlainBoxSitsAtItsRotationPointPlusOffset()
    {
        PackModel m = PackModel.parse("  flamePos: 10.4, -4.75, -0.15;\n  textureWidth = 512;\n  textureHeight = 64;\n"
            + "  part0 = new ModelRenderer(this, 1, 1);\n"
            + "  part0.addShape(1F,2F,3F, new float[][]{" + ZERO + "}, 2, 3, 4);\n"
            + "  part0.setRotationPoint(10F, 0F, 0F);\n  part0.setRotation(0F, 0F, 0F);\n");
        assertEquals(1, m.parts.size());
        assertArrayEquals(new double[] {10.4, -4.75, -0.15}, m.header("flamePos"), 1e-9);
        assertEquals(512, m.textureWidth);
        assertEquals(64, m.textureHeight);
        assertArrayEquals(new double[] {11, 13, 2, 5, 3, 7}, m.parts.get(0).bounds(), 1e-9);
    }

    @Test
    void cornerOffsetsFollowBModelBoxOrder()
    {
        // index 7 is the (0,0,0) corner: moved 0.5 back along x; index 0 is (w,h,d): moved 0.25 down
        PackModel m = PackModel.parse("p = new BeardieModelRenderer(this, 0, 0);\n"
            + "p.addShape(0F,0F,0F, new float[][]{{0F,0.25F,0F},{0F,0F,0F},{0F,0F,0F},{0F,0F,0F},{0F,0F,0F},{0F,0F,0F},"
            + "{0F,0F,0F},{-0.5F,0F,0F}}, 1, 1, 1);\n");
        double[][] v = m.parts.get(0).verts();
        assertArrayEquals(new double[] {-0.5, 0, 0}, v[7], 1e-9);   // ModelBox v7 = (0,0,0)
        assertArrayEquals(new double[] {1, 1.25, 1}, v[5], 1e-9);   // ModelBox v5 = (w,h,d)
    }

    @Test
    void rotationAndChildrenAsModelRenderer()
    {
        PackModel m = PackModel.parse("a = new ModelRenderer(this, 0, 0);\n"
            + "a.addShape(0F,0F,0F, new float[][]{" + ZERO + "}, 2, 1, 1);\n"
            + "a.setRotationPoint(5F, 0F, 0F);\n"
            + "a.setRotation(0F, 0F, 1.5707964F);\n"
            + "b = new ModelRenderer(this, 0, 8);\n"
            + "b.addShape(0F,0F,0F, new float[][]{" + ZERO + "}, 1, 1, 1);\n"
            + "b.setRotationPoint(1F, 0F, 0F);\n"
            + "a.addChild(b);\n");
        // a quarter turn about z: the box's x run (2 long) points along +y from the rotation point
        assertArrayEquals(new double[] {4, 5, 0, 2, 0, 1}, m.parts.get(0).bounds(), 1e-6);
        // b sits 1 along a's x, which the turn maps to +y
        assertArrayEquals(new double[] {4, 5, 1, 2, 0, 1}, m.parts.get(1).bounds(), 1e-6);
    }

    @Test
    void childrenOfAnUndeclaredParentAreNeverDrawn()
    {
        PackModel m = PackModel.parse("ammoModel1 = new ModelRenderer(this, 0, 0);\n"
            + "ammoModel1.addShape(0F,0F,0F, new float[][]{" + ZERO + "}, 1, 1, 1);\n"
            + "ammoModel0.addChild(ammoModel1);\n");
        assertEquals(0, m.parts.size());
        assertEquals(1, m.declared);
    }

    @Test
    void ourMac10AsStudyPyReadsIt() throws Exception
    {
        String text = resource("/assets/deci/models/guns/smg/mac10.bmodel");
        assertNotNull(text, "our MAC-10 model on the class path");
        PackModel m = PackModel.parse(text);
        assertEquals(110, m.parts.size());
        assertEquals(110, m.declared);
        assertArrayEquals(new double[] {10.4, -4.75, -0.15}, m.header("flamePos"), 1e-9);
        assertArrayEquals(new double[] {1.14, 10.5, -4.97, 3.05, -0.9, 0.6}, m.bounds(), 1e-4);
    }

    @Test
    void decimationsM4a4WithItsRotatedPartsAndChild() throws Exception
    {
        String text = resource("/assets/deci/models/guns/rifle/m4a4.bmodel");
        assumeTrue(text != null, "Decimation's jar on the test class path (dev/libs)");
        PackModel m = PackModel.parse(text);
        assertEquals(229, m.parts.size());
        assertEquals(228, m.topLevel());      // BModel.parts in the game: its one addChild child hangs from the parent
        assertArrayEquals(new double[] {-5.15, 19.15, -4.95, 2.45, -1.35, 0.9}, m.bounds(), 1e-4);
        PackModel.Part rotated = null, child = null;
        for (PackModel.Part p : m.parts)
        {
            rotated = p.name.equals("gunModel78") ? p : rotated;
            child = p.name.equals("ammoModel1") ? p : child;
        }
        assertArrayEquals(new double[] {-2.84897, -1.25349, -0.55}, rotated.verts()[0], 1e-4);
        assertArrayEquals(new double[] {7.41336, 0.59678, -0.58}, child.verts()[0], 1e-4);
    }
}

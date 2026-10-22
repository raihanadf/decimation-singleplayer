package net.decimation.fixes.gunpack;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Converted guns as real meshes (converter milestone 4, docs/superpowers/specs/2026-10-23-gun-mesh-design.md; user,
 * 23 Oktober 2026: "use convert 1:1 instead generating decimation style guns", texture "pixelated like decimation", a cap
 * of 30000 triangles).
 */
class MeshGunTest
{
    /** A .dmesh of one group "ammoModel" with one triangle, as tools/gunconv writes it (big endian). */
    static byte[] oneTriangle() throws IOException
    {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(b);
        o.writeBytes("DMESH1");
        o.writeInt(1);
        byte[] name = "ammoModel".getBytes("UTF-8");
        o.writeShort(name.length);
        o.write(name);
        o.writeFloat(1f);
        o.writeFloat(2f);
        o.writeFloat(3f);                                    // pivot
        o.writeInt(1);
        float[][] v = {{0, 0, 0, 0, 0, 1, 0, 0}, {1, 0, 0, 0, 0, 1, 1, 0}, {0, 1, 0, 0, 0, 1, 0, 1}};
        for (float[] x : v)
        {
            for (float f : x)
            {
                o.writeFloat(f);
            }
        }
        return b.toByteArray();
    }

    @Test
    void readsGroupsPivotsAndVertices() throws IOException
    {
        MeshGun m = MeshGun.read(oneTriangle());
        assertEquals(1, m.groups.size());
        MeshGun.Group g = m.groups.get(0);
        assertEquals("ammoModel", g.name);
        assertArrayEquals(new float[] {1f, 2f, 3f}, g.pivot);
        assertEquals(1, g.triangles());
        assertEquals(3 * 8, g.vertices.length);
        assertEquals(1f, g.vertices[8], "the second vertex's x");
        assertEquals(1f, g.vertices[23], "the third vertex's v");
    }

    @Test
    void refusesAnythingElse()
    {
        try
        {
            MeshGun.read("NOTAMESH".getBytes("UTF-8"));
            throw new AssertionError("read a file that is no mesh");
        }
        catch (IOException expected)
        {
            // the package is refused with this reason
        }
    }

    /** The six faces of a box from (x, y, z) to (x + s, y + s, z + s), as ModelBatch captures them: 6 x 4 x (x, y, z). */
    static float[] box(float x, float y, float z, float s)
    {
        float[][] c = new float[8][];
        for (int i = 0; i < 8; i++)
        {
            c[i] = new float[] {x + ((i & 1) != 0 ? s : 0), y + ((i & 2) != 0 ? s : 0), z + ((i & 4) != 0 ? s : 0)};
        }
        int[][] faces = {{0, 1, 3, 2}, {4, 5, 7, 6}, {0, 1, 5, 4}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5}};
        float[] out = new float[6 * 12];
        int k = 0;
        for (int[] f : faces)
        {
            for (int i : f)
            {
                out[k++] = c[i][0];
                out[k++] = c[i][1];
                out[k++] = c[i][2];
            }
        }
        return out;
    }

    @Test
    void findsTheMarkerAtAnyScale()
    {
        for (float s : new float[] {0.0625f, 1f, 0.03125f})
        {
            int[] m = MeshGun.marker(box(4242 * s, 2 * s, 7777 * s, s), 6);
            assertArrayEquals(new int[] {4242, 2}, m, "scale " + s);
            assertEquals(s, MeshGun.markerScale(box(4242 * s, 2 * s, 7777 * s, s), 6), s * 1e-4, "the size gives the scale");
        }
        assertArrayEquals(new int[] {65535, 0}, MeshGun.marker(box(65535 * 0.0625f, 0, 7777 * 0.0625f, 0.0625f), 6), "the largest id");
    }

    /** A scale that is no power of two rounds every corner (float): the size alone is too coarse for a large id. */
    @Test
    void findsTheMarkerAtScalesFloatsCannotHoldExactly()
    {
        for (float s : new float[] {0.05f, 0.0427f, 0.1f})
        {
            for (int id : new int[] {65535, 33367, 1})
            {
                assertArrayEquals(new int[] {id, 3}, MeshGun.marker(box(id * s, 3 * s, 7777 * s, s), 6), "id " + id + " at scale " + s);
            }
        }
    }

    @Test
    void anyOtherPartIsNoMarker()
    {
        assertNull(MeshGun.marker(box(1, 2, 3, 0.0625f), 6), "a plain box of a gun");
        assertNull(MeshGun.marker(box(4242, 2, 7777, 1), 5), "not six faces");
        float[] stretched = box(4242 * 0.0625f, 2 * 0.0625f, 7777 * 0.0625f, 0.0625f);
        stretched[3] += 0.5f;                                // one corner moved: a shape box, no marker
        assertNull(MeshGun.marker(stretched, 6));
    }

    @Test
    void theNameGivesTheIdTheConverterUses()
    {
        assertEquals(MeshGun.id("ak47"), MeshGun.id("ak47"));
        assertEquals(true, MeshGun.id("ak47") >= 0 && MeshGun.id("ak47") <= 65535);
        assertEquals(true, MeshGun.id("ak47") != MeshGun.id("akmc"));
        assertEquals(33367, MeshGun.id("m4a1"), "FNV-1a 32 folded to 16 bits, as tools/gunconv/meshpack.py computes it");
    }
}

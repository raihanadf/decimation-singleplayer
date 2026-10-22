package net.decimation.fixes.gunpack;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A converted gun drawn as its real mesh (converter milestone 4, docs/superpowers/specs/2026-10-23-gun-mesh-design.md;
 * user, 23 Oktober 2026: "use convert 1:1 instead generating decimation style guns").
 *
 * Decimation's .bmodel holds only shape boxes, each face drawn flat; a source mesh as boxes looked like dice and cost
 * several akms. A mesh package ships mesh.dmesh beside its gun.json and a skeleton .bmodel: per group (gunModel,
 * ammoModel, slideModel) one placeholder part, a 1 x 1 x 1 box at (id, group, 7777) times its own size, its rotation point
 * the group's pivot. When Decimation compiles that part, ModelBatch captures its six faces; {@link #marker} recognises
 * them and ModelBatch compiles the mesh group into the part's list instead, scaled by the marker's size. Decimation's
 * animations, hands, aim and attachment points then work on the part as for any gun.
 *
 * File (big endian): "DMESH1", groups; per group a name (short length, UTF-8), its pivot (3 floats, model units), its
 * triangle count, then per vertex x, y, z (model units, relative to the pivot), nx, ny, nz, u, v (0..1 of the gun texture).
 */
public final class MeshGun
{
    public static final String MAGIC = "DMESH1";
    /** The marker box's z in units of its own size. */
    static final int MARK_Z = 7777;

    public static final class Group
    {
        public final String name;
        public final float[] pivot;
        /** 8 floats a vertex, 3 vertices a triangle. */
        public final float[] vertices;

        Group(String name, float[] pivot, float[] vertices)
        {
            this.name = name;
            this.pivot = pivot;
            this.vertices = vertices;
        }

        public int triangles()
        {
            return vertices.length / 24;
        }
    }

    public final List<Group> groups;

    private MeshGun(List<Group> groups)
    {
        this.groups = Collections.unmodifiableList(groups);
    }

    public static MeshGun read(byte[] bytes) throws IOException
    {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
        byte[] magic = new byte[MAGIC.length()];
        if (bytes.length < magic.length + 4)
        {
            throw new IOException("not a mesh file (too short)");
        }
        in.readFully(magic);
        if (!MAGIC.equals(new String(magic, "US-ASCII")))
        {
            throw new IOException("not a mesh file (no " + MAGIC + ")");
        }
        int n = in.readInt();
        if (n < 1 || n > 16)
        {
            throw new IOException("mesh groups " + n + " (1 to 16)");
        }
        List<Group> groups = new ArrayList<Group>();
        for (int g = 0; g < n; g++)
        {
            byte[] name = new byte[in.readUnsignedShort()];
            in.readFully(name);
            float[] pivot = {in.readFloat(), in.readFloat(), in.readFloat()};
            int t = in.readInt();
            if (t < 0 || t > 30000)
            {
                throw new IOException("mesh group of " + t + " triangles (the cap is 30000)");
            }
            float[] v = new float[t * 24];
            for (int i = 0; i < v.length; i++)
            {
                v[i] = in.readFloat();
            }
            groups.add(new Group(new String(name, "UTF-8"), pivot, v));
        }
        return new MeshGun(groups);
    }

    /** The marker id of a gun's name: FNV-1a 32 of its UTF-8 bytes, the halves xored to 16 bits (tools/gunconv the same). */
    public static int id(String name)
    {
        int h = 0x811c9dc5;
        byte[] b;
        try
        {
            b = name.getBytes("UTF-8");
        }
        catch (java.io.UnsupportedEncodingException e)
        {
            throw new IllegalStateException(e);
        }
        for (byte x : b)
        {
            h ^= x & 0xff;
            h *= 0x01000193;
        }
        return ((h >>> 16) ^ (h & 0xffff)) & 0xffff;
    }

    /**
     * {id, group} when the captured quads (n quads of 4 corners, x, y, z each) are exactly a marker box: six faces of a
     * cube of size s whose lowest corner sits at (id, group, 7777) times s; else null.
     */
    public static int[] marker(float[] xyz, int n)
    {
        float s = markerScale(xyz, n);
        if (s <= 0)
        {
            return null;
        }
        float[] lo = bounds(xyz, n, false);
        double id = lo[0] / (double) s, group = lo[1] / (double) s;
        long i = Math.round(id), g = Math.round(group);
        if (Math.abs(id - i) > 0.05 || Math.abs(group - g) > 0.05 || i < 0 || i > 65535 || g < 0 || g > 15)
        {
            return null;
        }
        return new int[] {(int) i, (int) g};
    }

    /**
     * The marker's size (its scale) when the quads are six faces of a cube whose corners all sit on its corners and whose
     * lowest z is 7777 times its size, else 0. The size comes from that z: at a scale floats cannot hold exactly (0.05)
     * the cube's own size is rounded by about 0.5 %, too coarse for an id up to 65535.
     */
    public static float markerScale(float[] xyz, int n)
    {
        if (n != 6 || xyz.length < 72)
        {
            return 0;
        }
        float[] lo = bounds(xyz, n, false), hi = bounds(xyz, n, true);
        float s = lo[2] / MARK_Z;
        if (!(s > 0))
        {
            return 0;
        }
        for (int a = 0; a < 3; a++)                           // a cube of that size (to 1 %: its sides are rounded)
        {
            if (Math.abs(hi[a] - lo[a] - s) > s * 0.01)
            {
                return 0;
            }
        }
        for (int i = 0; i < 72; i++)                          // every corner on a corner of the cube
        {
            float d = Math.min(Math.abs(xyz[i] - lo[i % 3]), Math.abs(xyz[i] - hi[i % 3]));
            if (d > s * 0.01)
            {
                return 0;
            }
        }
        return s;
    }

    private static float[] bounds(float[] xyz, int n, boolean max)
    {
        float[] b = {xyz[0], xyz[1], xyz[2]};
        for (int i = 0; i < n * 12; i++)
        {
            b[i % 3] = max ? Math.max(b[i % 3], xyz[i]) : Math.min(b[i % 3], xyz[i]);
        }
        return b;
    }

    // ---- the meshes of the packages loaded, by marker id

    private static final Map<Integer, MeshGun> BY_ID = new HashMap<Integer, MeshGun>();

    /** Registers a package's mesh under its name's id; false when another gun's name has the same id (refused). */
    public static synchronized boolean register(String name, MeshGun mesh)
    {
        int id = id(name);
        if (BY_ID.containsKey(id))
        {
            return false;
        }
        BY_ID.put(id, mesh);
        return true;
    }

    public static synchronized MeshGun byId(int id)
    {
        return BY_ID.get(id);
    }
}

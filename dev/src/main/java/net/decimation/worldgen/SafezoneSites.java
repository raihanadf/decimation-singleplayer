package net.decimation.worldgen;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;

/**
 * The people of the safezone sites (docs/safezone.md): traders, guards, civilians and armed
 * survivors. A site's plan hands over its posts when its first slice is written (Slices, through
 * {@link Residents}); they are kept per world in {@code <world>/deciworldgen_safezones.json}.
 *
 * NPCs are not spawned during generation. Every 2 s, for each site with a player within 112 blocks
 * and all its chunks loaded, the keeper counts the living NPCs tagged with the site and post
 * (entity data deciwgSite / deciwgPost) and spawns the missing ones: all at the first visit, later
 * a dead one again after 5 minutes, only while no player is within 40 blocks of its post.
 */
public class SafezoneSites
{
    private static final String FILE = "deciworldgen_safezones.json";
    public static final String SITE_TAG = "deciwgSite", POST_TAG = "deciwgPost";
    static final int EVERY = 40, VISIT = 112, UNSEEN = 40, RESPAWN = 6000;

    /** One resident post: kind ("guard", "trader:TraderGuns", "civilian", "survivor"), position, yaw. */
    public static final class Post
    {
        public String kind;
        public double x, y, z;
        public float yaw;

        public Post(String kind, double x, double y, double z, float yaw)
        {
            this.kind = kind;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
        }
    }

    public static final class Site
    {
        public String id;
        public int x0, y0, z0, x1, y1, z1;
        public boolean filled;
        public List<Post> posts = new ArrayList<Post>();
    }

    private static final class SiteList
    {
        List<Site> sites = new ArrayList<Site>();
    }

    /** Survivor camps in this world: worlds created since v0.47 (StructureData CAMPS_KEY). */
    private static volatile boolean camps;
    static final String CAMPS_KEY = "!safezoneCamps";
    private static File file;
    private static final List<Site> sites = new ArrayList<Site>();
    private static boolean dirty;
    private final Map<String, Long> missingSince = new HashMap<String, Long>();
    private int tick;

    /** Called by Slices when a resident plan's first slice is written. */
    public static synchronized void add(String id, int x0, int y0, int z0, int x1, int y1, int z1, List<Post> posts)
    {
        if (file == null || find(id) != null)
        {
            return;
        }
        Site s = new Site();
        s.id = id;
        s.x0 = Math.min(x0, x1);
        s.y0 = Math.min(y0, y1);
        s.z0 = Math.min(z0, z1);
        s.x1 = Math.max(x0, x1);
        s.y1 = Math.max(y0, y1);
        s.z1 = Math.max(z0, z1);
        s.posts.addAll(posts);
        sites.add(s);
        dirty = true;
        FMLLog.info("[%s] safezone site %s at %d,%d,%d: %d residents", DecimationWorldGen.MODID, id, s.x0, s.y0, s.z0,
                    posts.size());
    }

    public static synchronized Site find(String id)
    {
        for (Site s : sites)
        {
            if (s.id.equals(id))
            {
                return s;
            }
        }
        return null;
    }

    /** All sites of this world (dev tests). */
    public static synchronized List<Site> all()
    {
        return new ArrayList<Site>(sites);
    }

    /** Forgets a site (dev tests; later a fallen safezone). */
    public static synchronized boolean remove(String id)
    {
        Site s = find(id);
        if (s != null)
        {
            sites.remove(s);
            dirty = true;
        }
        return s != null;
    }

    /** True when the loaded world gets survivor camps (LargeSites). */
    public static boolean camps()
    {
        return camps;
    }

    /** Switches survivor camps for this session (dev tests); not stored. */
    public static void forceCamps(boolean on)
    {
        camps = on;
    }

    /** True when the world stored survivor camps on. */
    public static boolean stored(World world)
    {
        Integer v = StructureData.get(world).baseY(CAMPS_KEY);
        return v != null && v == 1;
    }

    // ------------------------------------------------------------ keeper

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || ++tick % EVERY != 0)
        {
            return;
        }
        MinecraftServer server = MinecraftServer.getServer();
        WorldServer w = server == null ? null : server.worldServerForDimension(0);
        if (w == null || w.playerEntities.isEmpty())
        {
            return;
        }
        for (Site s : all())
        {
            if (nearest(w, (s.x0 + s.x1) / 2.0, (s.z0 + s.z1) / 2.0) > VISIT || !loaded(w, s))
            {
                continue;
            }
            keep(w, s);
        }
    }

    /** Spawns the missing residents of a site that may be filled now. */
    void keep(World w, Site s)
    {
        Set<Integer> present = new HashSet<Integer>();
        for (Object o : w.loadedEntityList)
        {
            Entity e = (Entity) o;
            if (e instanceof EntityLiving && !e.isDead && s.id.equals(e.getEntityData().getString(SITE_TAG)))
            {
                present.add(e.getEntityData().getInteger(POST_TAG));
            }
        }
        long now = w.getTotalWorldTime();
        int spawned = 0;
        for (int i = 0; i < s.posts.size(); i++)
        {
            String key = s.id + "#" + i;
            if (present.contains(i))
            {
                missingSince.remove(key);
                continue;
            }
            Post p = s.posts.get(i);
            Long since = missingSince.get(key);
            if (since == null)
            {
                missingSince.put(key, since = now);
            }
            boolean due = !s.filled || now - since >= RESPAWN && nearest(w, p.x, p.z) > UNSEEN;
            if (due && spawn(w, s, i) != null)
            {
                missingSince.remove(key);
                spawned++;
            }
        }
        if (!s.filled)
        {
            synchronized (SafezoneSites.class)
            {
                s.filled = true;
                dirty = true;
            }
            FMLLog.info("[%s] safezone site %s filled: %d residents spawned", DecimationWorldGen.MODID, s.id, spawned);
        }
    }

    /** Spawns the resident of one post, tagged with its site and post; null when refused. */
    public static EntityLiving spawn(World w, Site s, int i)
    {
        Post p = s.posts.get(i);
        for (int attempt = 0; attempt < 10; attempt++)
        {
            EntityLiving e = create(w, p.kind);
            if (e == null)
            {
                return null;
            }
            e.setLocationAndAngles(p.x, p.y, p.z, p.yaw, 0);
            e.rotationYawHead = e.renderYawOffset = p.yaw;
            e.getEntityData().setString(SITE_TAG, s.id);
            e.getEntityData().setInteger(POST_TAG, i);
            e.func_110163_bv(); // never despawns
            if (w.spawnEntityInWorld(e))
            {
                return e;
            }
        }
        return null;
    }

    private static EntityLiving create(World w, String kind)
    {
        Entity e;
        if (kind.startsWith("trader:"))
        {
            e = EntityList.createEntityByName(kind.substring(7), w);
        }
        else if (kind.equals("guard"))
        {
            e = EntityList.createEntityByName("TraderGuard", w);
        }
        else if (kind.equals("civilian"))
        {
            e = EntityList.createEntityByName("Human", w);
        }
        else if (kind.equals("survivor"))
        {
            EntityLiving s = net.decimation.fixes.Deci.newSoldier(w);
            net.decimation.fixes.NpcLoadouts.instance().equipTier(s, "survivor");
            e = s;
        }
        else
        {
            e = null;
        }
        return e instanceof EntityLiving ? (EntityLiving) e : null;
    }

    private static double nearest(World w, double x, double z)
    {
        double best = Double.MAX_VALUE;
        for (Object o : w.playerEntities)
        {
            EntityPlayer p = (EntityPlayer) o;
            best = Math.min(best, Math.hypot(p.posX - x, p.posZ - z));
        }
        return best;
    }

    private static boolean loaded(World w, Site s)
    {
        for (int cx = s.x0 >> 4; cx <= s.x1 >> 4; cx++)
        {
            for (int cz = s.z0 >> 4; cz <= s.z1 >> 4; cz++)
            {
                if (!w.getChunkProvider().chunkExists(cx, cz))
                {
                    return false;
                }
            }
        }
        return true;
    }

    // ------------------------------------------------------------ storage

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        World world = event.world;
        if (world.isRemote || world.provider == null || world.provider.dimensionId != 0)
        {
            return;
        }
        // a world that never ticked is new and gets survivor camps; older worlds would get camps
        // cut off by chunks generated before (as city rivers); -Ddeciworldgen.camps overrides
        StructureData data = StructureData.get(world);
        Integer v = data.baseY(CAMPS_KEY);
        if (v == null)
        {
            v = world.getTotalWorldTime() == 0 ? 1 : 0;
            data.setBaseY(CAMPS_KEY, v);
        }
        String force = System.getProperty("deciworldgen.camps");
        camps = force != null ? "true".equals(force) : v == 1;
        FMLLog.info("[%s] survivor camps %s", DecimationWorldGen.MODID, camps ? "on" : "off");
        synchronized (SafezoneSites.class)
        {
            sites.clear();
            file = new File(world.getSaveHandler().getWorldDirectory(), FILE);
            if (file.isFile())
            {
                try
                {
                    Reader in = new FileReader(file);
                    SiteList saved = new Gson().fromJson(in, SiteList.class);
                    in.close();
                    if (saved != null && saved.sites != null)
                    {
                        sites.addAll(saved.sites);
                    }
                }
                catch (Exception e)
                {
                    FMLLog.info("[%s] could not read %s: %s", DecimationWorldGen.MODID, file, e);
                }
            }
            dirty = false;
        }
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event)
    {
        if (!event.world.isRemote && event.world.provider != null && event.world.provider.dimensionId == 0)
        {
            save();
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
        if (!event.world.isRemote && event.world.provider != null && event.world.provider.dimensionId == 0)
        {
            synchronized (SafezoneSites.class)
            {
                save();
                sites.clear();
                file = null;
            }
        }
    }

    private static synchronized void save()
    {
        if (!dirty || file == null)
        {
            return;
        }
        try
        {
            SiteList out = new SiteList();
            out.sites = new ArrayList<Site>(sites);
            Writer wr = new FileWriter(file);
            wr.write(new GsonBuilder().setPrettyPrinting().create().toJson(out));
            wr.close();
            dirty = false;
        }
        catch (Exception e)
        {
            FMLLog.info("[%s] could not write %s: %s", DecimationWorldGen.MODID, file, e);
        }
    }
}

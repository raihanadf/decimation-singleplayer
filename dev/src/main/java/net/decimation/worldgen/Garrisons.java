package net.decimation.worldgen;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.decimation.fixes.Deci;
import net.decimation.fixes.NpcLoadouts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

/**
 * Garrisons of the hardest places (user, 13 Oktober 2026: the wasteland bunker "most harder place
 * building with full of high tier enemy npc in there. elite npc + juggernaut and full military outfit
 * zombie spawner"; docs/wasteland.md). A plan with a garrison hands Slices its posts and spawner cells
 * when its first slice is written; they are kept per world in StructureData lists "!gar_<id>":
 * {state, spawners, sx, sy, sz, ..., px, py, pz, ...}, state 0 waiting, 1 manned. The first time a
 * player comes within VISIT of a garrison whose chunks are loaded, its posts are manned: juggernauts
 * and elite military in turn, never despawning (spawned then and not at generation: Decimation's NPCs
 * would despawn before anyone came). Its spawners (vanilla mob spawner cages) breed Decimation's
 * infected; every infected that appears within SPAWNER_REACH of one gets the military outfit.
 */
public class Garrisons
{
    /** A plan with a garrison: posts and spawners in world coordinates, the floor at baseY. */
    public interface Garrisoned extends Plan
    {
        /** {spawners, sx, sy, sz, ..., px, py, pz, ...}; null or empty for none. */
        int[] garrison(int baseY);
    }

    static final String PREFIX = "!gar_";
    private static final int EVERY = 40, VISIT = 40, SPAWNER_REACH = 6;
    /** Spawner cells of this world, x / y / z packed (filled at load and on register). */
    private static final Set<Long> spawners = new HashSet<Long>();
    private static volatile boolean loaded;
    private int tick;

    /** Slices: the plan's garrison, stored once. */
    public static void register(World world, Plan p, int baseY)
    {
        if (!(p instanceof Garrisoned))
        {
            return;
        }
        StructureData data = StructureData.get(world);
        if (data.list(PREFIX + p.id()) != null)
        {
            return;
        }
        int[] g = ((Garrisoned) p).garrison(baseY);
        if (g == null || g.length == 0)
        {
            return;
        }
        int[] v = new int[g.length + 1];
        System.arraycopy(g, 0, v, 1, g.length);
        data.setList(PREFIX + p.id(), v);
        addSpawners(v);
        FMLLog.info("[%s] garrison %s: %d posts, %d spawners", DecimationWorldGen.MODID, p.id(),
                    (g.length - 1 - 3 * g[0]) / 3, g[0]);
    }

    private static synchronized void addSpawners(int[] v)
    {
        for (int i = 0; i < v[1]; i++)
        {
            spawners.add(key(v[2 + 3 * i], v[3 + 3 * i], v[4 + 3 * i]));
        }
    }

    private static long key(int x, int y, int z)
    {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }

    /** Slices: a mob spawner written by a plan breeds Decimation's infected (vanilla's default is a pig). */
    public static void setupSpawner(World world, int x, int y, int z)
    {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityMobSpawner)
        {
            String name = EntityList.getEntityString(Deci.newInfected(world));
            if (name != null)
            {
                ((TileEntityMobSpawner) te).func_145881_a().setEntityName(name);
            }
        }
    }

    private static synchronized void load(World w)
    {
        if (loaded)
        {
            return;
        }
        loaded = true;
        spawners.clear();
        StructureData data = StructureData.get(w);
        for (String id : data.lists(PREFIX))
        {
            addSpawners(data.list(id));
        }
    }

    /** A world loading (dev test worlds, a new world in one session) reads its own garrisons. */
    public static synchronized void reset()
    {
        loaded = false;
        spawners.clear();
    }

    @SubscribeEvent
    public void onWorldLoad(net.minecraftforge.event.world.WorldEvent.Load event)
    {
        if (!event.world.isRemote && event.world.provider != null && event.world.provider.dimensionId == 0)
        {
            reset();
        }
    }

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
        load(w);
        StructureData data = StructureData.get(w);
        for (String id : data.lists(PREFIX))
        {
            int[] v = data.list(id);
            int posts = v.length - 2 - 3 * v[1];
            if (v[0] != 0 || posts < 3)
            {
                continue;
            }
            int first = 2 + 3 * v[1];
            int px = v[first], py = v[first + 1], pz = v[first + 2];
            EntityPlayer near = w.getClosestPlayer(px + 0.5, py, pz + 0.5, VISIT);
            if (near == null || !w.checkChunksExist(px - 16, py, pz - 16, px + 16, py, pz + 16))
            {
                continue;
            }
            man(w, id, v, first);
        }
    }

    /** Spawns the garrison's NPCs on their posts: juggernaut, elite, elite, juggernaut, ... */
    static int man(World w, String id, int[] v, int first)
    {
        int n = 0;
        for (int i = first, k = 0; i + 2 < v.length; i += 3, k++)
        {
            EntityLiving npc = Deci.newSoviet(w);
            if (NpcLoadouts.instance() != null)
            {
                NpcLoadouts.instance().equipTier(npc, k % 3 == 0 ? "juggernaut" : "elite_military");
            }
            npc.setLocationAndAngles(v[i] + 0.5, v[i + 1], v[i + 2] + 0.5, w.rand.nextFloat() * 360, 0);
            npc.func_110163_bv();                       // never despawns
            n += w.spawnEntityInWorld(npc) ? 1 : 0;
        }
        v[0] = 1;
        StructureData.get(w).setList(id, v);
        FMLLog.info("[%s] garrison %s manned: %d NPCs", DecimationWorldGen.MODID, id.substring(PREFIX.length()), n);
        return n;
    }

    /** Every infected that appears beside a garrison spawner wears the military outfit. */
    @SubscribeEvent
    public void onJoin(EntityJoinWorldEvent event)
    {
        Entity e = event.entity;
        if (e.worldObj.isRemote || !Deci.isPlainInfected(e) || spawners.isEmpty())
        {
            return;
        }
        int x = (int) Math.floor(e.posX), y = (int) Math.floor(e.posY), z = (int) Math.floor(e.posZ);
        synchronized (Garrisons.class)
        {
            for (int dx = -SPAWNER_REACH; dx <= SPAWNER_REACH; dx++)
            {
                for (int dz = -SPAWNER_REACH; dz <= SPAWNER_REACH; dz++)
                {
                    for (int dy = -3; dy <= 3; dy++)
                    {
                        if (spawners.contains(key(x + dx, y + dy, z + dz)))
                        {
                            Deci.setInfectedVariant(e, "MILITARY");
                            return;
                        }
                    }
                }
            }
        }
    }

    /** The garrison ids of this world (dev tests). */
    public static List<String> ids(World w)
    {
        return StructureData.get(w).lists(PREFIX);
    }

    /** Mans a garrison now (dev tests); the number of NPCs spawned. */
    public static int manNow(World w, String id)
    {
        int[] v = StructureData.get(w).list(id);
        return v == null || v[0] != 0 ? 0 : man(w, id, v, 2 + 3 * v[1]);
    }
}

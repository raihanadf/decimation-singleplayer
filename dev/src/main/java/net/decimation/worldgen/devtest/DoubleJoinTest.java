package net.decimation.worldgen.devtest;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.decimation.fixes.Deci;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * Entities loaded with a chunk join the world once (bug.md "Entity is already tracked"). Decimation's join handler
 * (PlayerJoinSync, deci.an.e) replaces 1 in 10 joining infected with a bloater or a hulk (5 in 100 soldiers with a mech,
 * infected in the rain with a frail) by spawning the new mob at once. During a chunk load that happens inside
 * World.addLoadedEntities, which walks the chunk's live entity list: the new mob is appended to that list, the walk
 * reaches it and adds it a second time (the tracker refuses it with "Entity is already tracked!", the world lists it
 * twice and updates it twice a tick). The test puts 80 plain infected into an arena chunk's list the way a chunk load
 * from disk does (Chunk.addEntity), runs addLoadedEntities on that list as Chunk.onChunkLoad does, then counts the
 * replacements, the entities the world lists twice, and the updates the replacements get in 20 ticks.
 */
public class DoubleJoinTest extends DevTestMode
{
    public String name() { return "doublejoin"; }

    private static final int INFECTED = 80, WAIT = 20;
    private volatile int ticks;
    private volatile boolean done;
    private final List<Entity> made = new ArrayList<Entity>();
    private final Map<Entity, Integer> replacements = new IdentityHashMap<Entity, Integer>();

    public boolean client(Minecraft mc)
    {
        return !done;
    }

    public void server()
    {
        int t = ++ticks;
        EntityPlayerMP p = DevTestUtil.player();
        if (p == null || done)
        {
            return;
        }
        World w = p.worldObj;
        if (t == 20)
        {
            w.getWorldInfo().setRaining(false); // the rain swaps every infected for a frail: keep it to Decimation's 1 in 10
            w.getWorldInfo().setThundering(false);
            DevTestArena.build(w, p);
            load(w);
        }
        if (t == 20 + WAIT)
        {
            int[] updates = new int[replacements.size()];
            int i = 0, min = Integer.MAX_VALUE, max = 0;
            for (Map.Entry<Entity, Integer> r : replacements.entrySet())
            {
                updates[i] = r.getKey().ticksExisted - r.getValue();
                min = Math.min(min, updates[i]);
                max = Math.max(max, updates[i]);
                i++;
            }
            boolean once = !replacements.isEmpty() && max <= WAIT + 1;
            DevTestResults.check(name(), "replacement updates in " + WAIT + " ticks",
                                 replacements.isEmpty() ? "no replacement" : min + " .. " + max, once,
                                 "at most " + (WAIT + 1) + " (one update a tick)");
            for (Entity e : made)
            {
                e.setDead();
            }
            for (Entity e : replacements.keySet())
            {
                e.setDead();
            }
            done = true;
        }
    }

    private void load(World w)
    {
        int x0 = (DevTestArena.X >> 4 << 4) + 32; // a whole chunk inside the arena floor, two chunks east of its middle
        int z0 = DevTestArena.Z >> 4 << 4;
        int y = DevTestArena.Y;
        Chunk chunk = w.getChunkFromBlockCoords(x0, z0);
        for (int i = 0; i < INFECTED; i++)
        {
            EntityLiving e = Deci.newInfected(w);
            e.setPosition(x0 + 0.5 + i % 16, y, z0 + 0.5 + (i / 16) * 3);
            chunk.addEntity(e); // what AnvilChunkLoader.loadEntities does: in the chunk's list, not in the world yet
            made.add(e);
        }
        @SuppressWarnings("unchecked")
        List<Entity> list = (List<Entity>) chunk.entityLists[Math.max(0, Math.min(chunk.entityLists.length - 1, y >> 4))];
        w.addLoadedEntities(list); // what Chunk.onChunkLoad does with each of its lists
        Map<Entity, Integer> listed = new IdentityHashMap<Entity, Integer>();
        for (Object o : w.loadedEntityList)
        {
            Entity e = (Entity) o;
            if (e.chunkCoordX == chunk.xPosition && e.chunkCoordZ == chunk.zPosition)
            {
                listed.put(e, listed.containsKey(e) ? listed.get(e) + 1 : 1);
            }
        }
        int twice = 0;
        for (Map.Entry<Entity, Integer> l : listed.entrySet())
        {
            twice += l.getValue() > 1 ? 1 : 0;
            if (!Deci.isPlainInfected(l.getKey()) && !made.contains(l.getKey()))
            {
                replacements.put(l.getKey(), l.getKey().ticksExisted);
            }
        }
        DevTestResults.value(name(), "infected loaded with the chunk", INFECTED);
        DevTestResults.check(name(), "replaced by Decimation", replacements.size(), !replacements.isEmpty(),
                             "some (1 in 10 on average): else the test saw nothing, run it again");
        DevTestResults.check(name(), "entities the world lists twice", twice, twice == 0, "0");
    }
}

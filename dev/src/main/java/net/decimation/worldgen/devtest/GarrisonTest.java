package net.decimation.worldgen.devtest;

import java.util.ArrayList;
import java.util.List;

import net.decimation.fixes.Deci;
import net.decimation.fixes.NpcKind;
import net.decimation.fixes.NpcLoadouts;
import net.decimation.worldgen.Garrisons;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

/**
 * An airport's garrison manned in a real generated world (docs/airport.md: "Not checked: the garrison manned in game").
 * Fresh seed 1 world (airports only in worlds made since v0.62); the player, in creative (Decimation's NPCs ignore
 * creative players), hovers over seed 1's airport at 272, -2032 until its garrison is registered (worldgen/Garrisons:
 * posts stored when the airport's slices are written), comes within 40 blocks of the first post, then flies over every
 * post in turn (posts far from the first wait for their chunks). Checks: the first visit mans posts, every post gets
 * tried, every post holds a juggernaut or elite Soviet, nobody stands inside a block; saves garrison_post (the first
 * post from 9 blocks away).
 */
public class GarrisonTest extends DevTestMode
{
    public String name() { return "garrison"; }

    /** Airports exist only in worlds created since v0.62: the fresh seed 1 world. */
    public boolean freshWorld() { return true; }

    /** Seed 1's airport with its runway along x (docs/airport.md section 8). */
    private static final int AX = 272, AZ = -2032, NEAR = 300;
    private static final int REGISTER_WAIT = 20 * 180, MAN_WAIT = 20 * 30, SETTLE = 60;

    private volatile int ticks, step;
    private volatile boolean done, shoot, shot;
    private volatile double camX, camY, camZ;
    private volatile float camYaw, camPitch;
    private String id;
    private int[][] posts;
    private int stepTicks, tour;

    public boolean client(Minecraft mc)
    {
        if (shoot && !shot && mc.thePlayer != null)
        {
            mc.gameSettings.hideGUI = true;
            if (++stepTicks > 80)
            {
                DevTestUtil.screenshot(mc, name(), "garrison_post");
                mc.gameSettings.hideGUI = false;
                shot = true;
            }
        }
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
        switch (step)
        {
            case 0:
                if (t < 20)
                {
                    return;
                }
                p.setGameType(WorldSettings.GameType.CREATIVE);
                p.capabilities.isFlying = true;
                p.sendPlayerAbilities();
                w.getWorldInfo().setRaining(false);
                w.getWorldInfo().setThundering(false);
                w.setWorldTime(6000);
                next();
                return;
            case 1:
                hold(p, AX + 0.5, 150, AZ + 0.5, 0, 90);
                id = nearest(w);
                if (id == null && ticks < REGISTER_WAIT)
                {
                    return;                         // the airport is written chunk by chunk around the player
                }
                posts = id == null ? new int[0][] : Garrisons.posts(w, id);
                DevTestResults.check(name(), "airport garrison registered", id == null ? "none within " + NEAR + " blocks" : id + ", "
                                     + posts.length + " posts", id != null && posts.length >= 3, "the airport's garrison with its posts");
                if (id == null)
                {
                    done = true;
                    return;
                }
                next();
                return;
            case 2:
                int[] first = posts[0];
                hold(p, first[0] + 20.5, first[1] + 8, first[2] + 0.5, 90, 20); // 20 blocks east of the first post
                if (Garrisons.postsDone(w, id) == 0 && ticks < MAN_WAIT)
                {
                    return;
                }
                DevTestResults.check(name(), "manned when a player came near", Garrisons.postsDone(w, id) + " of " + posts.length
                                     + " posts after " + ticks + " ticks", Garrisons.postsDone(w, id) > 0,
                                     "posts manned (within 40 blocks of the first post)");
                tour = 0;
                step = 5;
                ticks = 0;
                return;
            case 5:
                // a player walking the airport: every post gets its NPC once its chunk is loaded
                int[] at = posts[Math.min(tour, posts.length - 1)];
                hold(p, at[0] + 0.5, at[1] + 10, at[2] + 0.5, 0, 90);
                if (ticks % 60 == 0)
                {
                    tour++;
                }
                if (!Garrisons.manned(w, id) && tour < posts.length)
                {
                    return;
                }
                DevTestResults.check(name(), "every post tried", Garrisons.postsDone(w, id) + " of " + posts.length + " after "
                                     + tour + " posts visited", Garrisons.manned(w, id), "all posts (each once its chunk is loaded)");
                step = 3;
                ticks = 0;
                return;
            case 3:
                // over the airport's middle, every post's chunk loaded (NPCs out there were saved with their chunks)
                double mx = 0, mz = 0;
                int top = 0;
                boolean loaded = true;
                for (int[] q : posts)
                {
                    mx += q[0];
                    mz += q[2];
                    top = Math.max(top, q[1]);
                    loaded &= w.blockExists(q[0], q[1], q[2]);
                }
                hold(p, mx / posts.length + 0.5, top + 30, mz / posts.length + 0.5, 0, 90);
                if ((!loaded || ticks < SETTLE) && ticks < SETTLE + 20 * 60)
                {
                    return;
                }
                DevTestResults.check(name(), "every post's chunk loaded", loaded, loaded, "true (seen from the airport's middle)");
                check(w);
                shoot = true;
                next();
                return;
            case 4:
                int[] g = posts[0];
                double hx = g[0] + 6.5, hy = g[1] + 4.5, hz = g[2] + 6.5;
                double ex = g[0] + 0.5 - hx, ey = g[1] + 1.0 - (hy + 1.62), ez = g[2] + 0.5 - hz;
                hold(p, hx, hy, hz, (float) Math.toDegrees(-Math.atan2(ex, ez)),
                     (float) Math.toDegrees(-Math.atan2(ey, Math.sqrt(ex * ex + ez * ez))));
                if (shot || ticks > 400)
                {
                    done = true;
                }
                return;
            default:
                done = true;
        }
    }

    private void check(World w)
    {
        List<Entity> garrison = new ArrayList<Entity>();
        for (Object o : w.loadedEntityList)
        {
            Entity e = (Entity) o;
            String tier = e.getEntityData().getString(NpcLoadouts.TAG);
            if (Deci.npcKind(e) == NpcKind.SOVIET && e.isEntityAlive() && ("juggernaut".equals(tier) || "elite_military".equals(tier)))
            {
                garrison.add(e);
            }
        }
        int held = 0, inBlock = 0;
        StringBuilder missing = new StringBuilder();
        for (int[] post : posts)
        {
            boolean here = false;
            for (Entity e : garrison)
            {
                if (Math.abs(e.posX - post[0] - 0.5) <= 6 && Math.abs(e.posZ - post[2] - 0.5) <= 6 && Math.abs(e.posY - post[1]) <= 4)
                {
                    here = true;
                    break;
                }
            }
            held += here ? 1 : 0;
            if (!here)
            {
                missing.append(missing.length() == 0 ? "" : " ").append(post[0]).append(',').append(post[1]).append(',').append(post[2]);
            }
        }
        for (Entity e : garrison)
        {
            int x = (int) Math.floor(e.posX), y = (int) Math.floor(e.posY), z = (int) Math.floor(e.posZ);
            if (w.getBlock(x, y, z).isNormalCube() || w.getBlock(x, y + 1, z).isNormalCube())
            {
                inBlock++;
            }
        }
        int homed = 0;
        for (Entity e : garrison)
        {
            net.minecraft.entity.EntityCreature c = (net.minecraft.entity.EntityCreature) e;
            homed += c.hasHome() && c.func_110174_bM() <= 6 && nearPost(c.getHomePosition().posX, c.getHomePosition().posY,
                                                                       c.getHomePosition().posZ) ? 1 : 0;
        }
        DevTestResults.check(name(), "guards keep to their posts", homed + " of " + garrison.size() + " with their home at a post",
                             homed == garrison.size() && homed > 0, "all (home area at the post, a few blocks)");
        DevTestResults.value(name(), "garrison NPCs alive", garrison.size());
        DevTestResults.check(name(), "posts held", held + " of " + posts.length + (missing.length() == 0 ? "" : ", empty: " + missing),
                             held == posts.length, "every post (a juggernaut or elite Soviet within 6 blocks: they walk a little)");
        DevTestResults.check(name(), "garrison NPCs inside a block", inBlock, inBlock == 0, "0");
    }

    private boolean nearPost(int x, int y, int z)
    {
        for (int[] post : posts)
        {
            if (post[0] == x && post[1] == y && post[2] == z)
            {
                return true;
            }
        }
        return false;
    }

    private String nearest(World w)
    {
        String best = null;
        double bestD = NEAR * NEAR;
        for (String g : Garrisons.ids(w))
        {
            int[][] ps = Garrisons.posts(w, g);
            if (ps.length == 0)
            {
                continue;
            }
            double d = (ps[0][0] - AX) * (double) (ps[0][0] - AX) + (ps[0][2] - AZ) * (double) (ps[0][2] - AZ);
            if (d < bestD)
            {
                best = g;
                bestD = d;
            }
        }
        return best;
    }

    private static void hold(EntityPlayerMP p, double x, double y, double z, float yaw, float pitch)
    {
        p.motionX = p.motionY = p.motionZ = 0;
        p.fallDistance = 0;
        p.playerNetServerHandler.setPlayerLocation(x, y, z, yaw, pitch);
    }

    private void next()
    {
        step++;
        ticks = 0;
    }
}

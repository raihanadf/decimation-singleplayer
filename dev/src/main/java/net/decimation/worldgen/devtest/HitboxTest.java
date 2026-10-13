package net.decimation.worldgen.devtest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

/**
 * Big infected hitboxes (bug report 13 Oktober 2026: shots at a big zombie's head do not hit): spawns
 * Decimation's Hulk and Bloater held in place in the arena and photographs them side on with the
 * bounding boxes drawn (hitbox_side), so the drawn model can be compared with the box shots are tested
 * against. Records each one's box size.
 */
public class HitboxTest extends DevTestMode
{
    public String name() { return "hitbox"; }

    private static final String[] KINDS = {"Hulk", "Bloater"};
    private volatile int ticks;
    private final EntityLiving[] held = new EntityLiving[KINDS.length];
    private boolean shot, done;

    public boolean client(Minecraft mc)
    {
        int t = ticks;
        if (mc.thePlayer == null || t < 30)
        {
            return true;
        }
        RenderManager.debugBoundingBox = true;
        mc.gameSettings.hideGUI = true;
        mc.gameSettings.fovSetting = 70f;
        mc.thePlayer.rotationYaw = mc.thePlayer.prevRotationYaw = 90;
        mc.thePlayer.rotationPitch = mc.thePlayer.prevRotationPitch = 0;
        if (t >= 70 && !shot)
        {
            shot = true;
            DevTestUtil.screenshot(mc, name(), "hitbox_side");
        }
        if (t >= 80)
        {
            RenderManager.debugBoundingBox = false;
            mc.gameSettings.hideGUI = false;
            done = true;
        }
        return !done;
    }

    public void server()
    {
        int t = ++ticks;
        EntityPlayerMP p = DevTestUtil.player();
        if (p == null)
        {
            return;
        }
        World w = p.worldObj;
        int x = DevTestArena.X, y = DevTestArena.Y, z = DevTestArena.Z;
        if (t == 10)
        {
            w.difficultySetting = EnumDifficulty.NORMAL;
            w.setWorldTime(6000);
            DevTestArena.build(w, p);
            p.capabilities.disableDamage = true;
            for (int i = 0; i < KINDS.length; i++)
            {
                for (int tries = 0; tries < 20 && held[i] == null; tries++)
                {
                    Entity e = EntityList.createEntityByName(KINDS[i], w);
                    if (e instanceof EntityLiving)
                    {
                        e.setPosition(x - 8 + 0.5, y, z - 2 + i * 4 + 0.5);
                        if (w.spawnEntityInWorld(e))
                        {
                            held[i] = (EntityLiving) e;
                            DevTestResults.value(name(), KINDS[i] + " box", e.width + " wide, " + e.height + " high, eyes "
                                + e.getEyeHeight());
                        }
                    }
                }
                if (held[i] == null)
                {
                    DevTestResults.value(name(), KINDS[i], "could not spawn");
                }
            }
        }
        if (t > 10)
        {
            for (int i = 0; i < KINDS.length; i++)
            {
                if (held[i] != null)
                {
                    held[i].setPosition(x - 8 + 0.5, y, z - 2 + i * 4 + 0.5);
                    held[i].motionX = held[i].motionZ = 0;
                    held[i].rotationYaw = held[i].renderYawOffset = held[i].rotationYawHead = 90;
                    held[i].setAttackTarget(null);
                }
            }
            p.playerNetServerHandler.setPlayerLocation(x + 0.5, y + 0.5, z + 0.5, 90, 0);
        }
        if (t == 85)
        {
            for (EntityLiving e : held)
            {
                if (e != null)
                {
                    e.setDead();
                }
            }
        }
    }
}

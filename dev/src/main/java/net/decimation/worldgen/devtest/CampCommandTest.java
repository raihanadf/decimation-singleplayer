package net.decimation.worldgen.devtest;

import net.decimation.worldgen.SafezoneSites;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

/**
 * The /deciworldgen camp command (docs/safezone.md) on real ground in the test world, at
 * -21000, -19700 (away from the sky test sites): runs the command as the player, waits for the
 * keeper, counts the camp's residents (20 expected) and takes an aerial shot. A rerun replaces the
 * camp at the same spot (same id).
 */
public class CampCommandTest extends DevTestMode
{
    public String name() { return "campcmd"; }

    private static final int X = -21000, Z = -19700;
    private volatile int ticks;
    private volatile boolean shoot, done;
    private volatile float[] view;

    public boolean client(Minecraft mc)
    {
        float[] v = view;
        if (mc.thePlayer != null && v != null && shoot)
        {
            mc.gameSettings.hideGUI = true;
            mc.gameSettings.clouds = false;
            mc.thePlayer.rotationYaw = mc.thePlayer.prevRotationYaw = v[0];
            mc.thePlayer.rotationPitch = mc.thePlayer.prevRotationPitch = v[1];
            if (ticks > 260 && ticks < 263)
            {
                DevTestUtil.screenshot(mc, name(), "campcmd_aerial");
            }
        }
        if (done)
        {
            mc.gameSettings.hideGUI = false;
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
        if (t == 2)
        {
            p.capabilities.isFlying = true;
            p.capabilities.disableDamage = true;
            w.setWorldTime(6000);
            w.difficultySetting = net.minecraft.world.EnumDifficulty.NORMAL;
            p.setPositionAndUpdate(X + 0.5, 120, Z + 0.5);
        }
        if (t == 60)
        {
            int y = w.getTopSolidOrLiquidBlock(X, Z);
            p.capabilities.isFlying = false;
            p.setPositionAndUpdate(X + 0.5, y, Z + 0.5);
        }
        if (t == 80)
        {
            p.onGround = true;
            MinecraftServer.getServer().getCommandManager().executeCommand(p, "deciworldgen camp 1");
            DevTestResults.value(name(), "command at", (int) p.posX + "," + (int) p.posY + "," + (int) p.posZ);
        }
        if (t == 220)
        {
            String id = null;
            for (SafezoneSites.Site s : SafezoneSites.all())
            {
                if (s.id.startsWith("C") && Math.abs(s.x0 + 29 - X) < 40 && Math.abs(s.z0 + 29 - Z) < 40)
                {
                    id = s.id;
                }
            }
            int n = 0;
            for (Object o : w.loadedEntityList)
            {
                Entity e = (Entity) o;
                if (e instanceof EntityLiving && !e.isDead && id != null && id.equals(e.getEntityData().getString(SafezoneSites.SITE_TAG)))
                {
                    n++;
                }
            }
            DevTestResults.check(name(), "camp site from the command", id, id != null, "a C.. site");
            DevTestResults.check(name(), "residents of the command camp", n, n == 20, "20");
            p.capabilities.isFlying = true;
            p.playerNetServerHandler.setPlayerLocation(X + 0.5, p.posY + 30, Z + 45, 180, 45);
            view = new float[] {180, 45};
            shoot = true;
        }
        if (t == 280)
        {
            done = true;
        }
    }
}

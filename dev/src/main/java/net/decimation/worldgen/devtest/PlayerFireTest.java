package net.decimation.worldgen.devtest;

import cpw.mods.fml.common.registry.GameRegistry;
import net.decimation.fixes.Deci;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * The player's own gunshot, end to end (docs/decimation_code/weapons.md): the client fires the held AKM the way the
 * mouse path does (Deci.fireHeldGun: GunItem.fire, fireRay, PacketGunHit / PacketGunFire), the server's handlers take
 * a round and hurt the target. Shot 1 aims at an infected 5 blocks away, shot 2 into the sky. Night, so the infected
 * does not burn. Checks: the hit lands, one round per shot on the server and in the client's copy, a miss hurts
 * nothing. Records the health lost next to GunItem.damage.
 */
public class PlayerFireTest extends DevTestMode
{
    public String name() { return "playerfire"; }

    private static final String GUN = "akm";
    private static final int ROUNDS = 30;
    private volatile int ticks, targetId = -1;
    private volatile boolean ready, shot1, check1Done, shot2, check2Done;
    private volatile int clientAmmo1 = -2, clientAmmo2 = -2;
    private int step, wait;
    private EntityLiving target;
    private float baseline;
    private int shotAt;

    public boolean client(Minecraft mc)
    {
        if (mc.thePlayer == null || !ready)
        {
            return true;
        }
        wait++;
        Entity e = mc.theWorld.getEntityByID(targetId);
        switch (step)
        {
            case 0: // shot 1: at the infected
                if (wait == 20 && e != null)
                {
                    aim(mc, e.posX, e.posY + e.height * 0.6, e.posZ);
                    DevTestResults.value(name(), "client ammo before", String.valueOf(Deci.gunAmmo(mc.thePlayer.getHeldItem())));
                    DevTestResults.check(name(), "client fires the held gun", "", Deci.fireHeldGun(mc.thePlayer), "a GunItem held");
                    shot1 = true;
                    next(1);
                }
                if (wait > 200)
                {
                    DevTestResults.check(name(), "target seen by the client", "no", false, "the infected on the client");
                    return false;
                }
                break;
            case 1: // the server checked the hit; read the client's copy of the gun after the sync
                if (check1Done && wait > 15)
                {
                    clientAmmo1 = Deci.gunAmmo(mc.thePlayer.getHeldItem());
                    DevTestResults.check(name(), "client copy after one shot", String.valueOf(clientAmmo1), clientAmmo1 == ROUNDS - 1,
                                         String.valueOf(ROUNDS - 1));
                    aim(mc, mc.thePlayer.posX, mc.thePlayer.posY + 200, mc.thePlayer.posZ - 1);
                    Deci.fireHeldGun(mc.thePlayer);
                    shot2 = true;
                    next(2);
                }
                break;
            case 2:
                if (check2Done && wait > 15)
                {
                    clientAmmo2 = Deci.gunAmmo(mc.thePlayer.getHeldItem());
                    DevTestResults.check(name(), "client copy after two shots", String.valueOf(clientAmmo2), clientAmmo2 == ROUNDS - 2,
                                         String.valueOf(ROUNDS - 2));
                    return false;
                }
                break;
        }
        return wait < 400;
    }

    private void next(int s)
    {
        step = s;
        wait = 0;
    }

    /** Turns the client player to look at a point (yaw 0 = south, as Minecraft counts it). */
    private static void aim(Minecraft mc, double x, double y, double z)
    {
        double dx = x - mc.thePlayer.posX, dz = z - mc.thePlayer.posZ;
        double dy = y - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        mc.thePlayer.rotationYaw = mc.thePlayer.prevRotationYaw = yaw;
        mc.thePlayer.rotationPitch = mc.thePlayer.prevRotationPitch = pitch;
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
            DevTestArena.build(w, p);
            w.setWorldTime(18000);
            p.capabilities.disableDamage = true;
            ItemStack g = new ItemStack(GameRegistry.findItem("deci", GUN));
            g.stackTagCompound = new NBTTagCompound();
            g.stackTagCompound.setInteger("ammo", ROUNDS);
            p.inventory.setInventorySlotContents(p.inventory.currentItem, g);
            p.playerNetServerHandler.setPlayerLocation(DevTestArena.X + 0.5, DevTestArena.Y, DevTestArena.Z + 0.5, 180, 0);
        }
        if (t >= 10 && target == null && t < 200)
        {
            EntityLiving e = Deci.newInfected(w); // Decimation refuses some spawns (hulks, bloaters): retry
            e.setLocationAndAngles(DevTestArena.X + 0.5, DevTestArena.Y, DevTestArena.Z - 4.5, 0, 0);
            if (w.spawnEntityInWorld(e))
            {
                target = e;
                targetId = e.getEntityId();
                ready = true;
            }
        }
        if (target == null)
        {
            return;
        }
        if (!shot1)
        {
            baseline = target.getHealth();
        }
        else if (shotAt == 0)
        {
            shotAt = t;
        }
        if (shot1 && !check1Done && t - shotAt >= 10)
        {
            float lost = baseline - (target.isDead ? 0 : target.getHealth());
            ItemStack g = p.getHeldItem();
            DevTestResults.check(name(), "the shot hits the infected", String.format("%.2f of %.2f lost", lost, baseline), lost > 0,
                                 "health lost");
            DevTestResults.value(name(), "health lost vs GunItem.damage", String.format("%.2f vs %s", lost, String.valueOf(Deci.gunDamageOf(g))));
            DevTestResults.check(name(), "server takes one round", String.valueOf(Deci.gunAmmo(g)), Deci.gunAmmo(g) == ROUNDS - 1,
                                 String.valueOf(ROUNDS - 1));
            baseline = target.isDead ? 0 : target.getHealth();
            check1Done = true;
            shotAt = -1;
        }
        if (shot2 && shotAt == -1)
        {
            shotAt = t;
        }
        if (shot2 && !check2Done && shotAt > 0 && t - shotAt >= 10)
        {
            ItemStack g = p.getHeldItem();
            float now = target.isDead ? 0 : target.getHealth();
            DevTestResults.check(name(), "a miss uses a round", String.valueOf(Deci.gunAmmo(g)), Deci.gunAmmo(g) == ROUNDS - 2,
                                 String.valueOf(ROUNDS - 2));
            DevTestResults.check(name(), "a miss hurts nothing", String.format("%.2f -> %.2f", baseline, now),
                                 target.isDead || Math.abs(now - baseline) < 0.01f || now > baseline, "same health");
            target.setDead();
            check2Done = true;
        }
    }
}

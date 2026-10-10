package net.decimation.worldgen.devtest;

import cpw.mods.fml.common.registry.GameRegistry;
import net.decimation.fixes.Deci;
import net.decimation.fixes.GunNoise;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Gunshot noise (fixes/GunNoise, dev test noise). Three phases on the arena, 10 s each:
 * 1. a shot at the arena centre: an infected 50 blocks west walks at least 15 blocks toward it,
 *    one 76 blocks west (out of the 64 block range) stays;
 * 2. a suppressed shot: an infected 30 blocks away stays (12 block range);
 * 3. a bandit fires and leaves: a Soviet 43 blocks away (an enemy) walks toward the spot;
 * 4. the same with a bandit 43 blocks away (its own side): it does not;
 * 5. the player's held gun goes from 30 to 29 rounds (how the game records a shot): an
 *    infected 45 blocks away comes (the detection path of real player shots);
 * 6. the same shot by a SURVIVAL player (damage off): a Soviet 40 blocks away comes, and by a
 *    creative player it does not (Decimation's NPCs are never hostile to creative players, user
 *    report 11 Oktober 2026 "enemy npc do nothing when me shooting near them", in creative).
 * The player is in creative, so the infected do not chase the player instead.
 */
public class NoiseTest extends DevTestMode
{
    public String name() { return "noise"; }

    private static final int PHASE = 260;
    private volatile int ticks;
    private volatile boolean done;
    private EntityLiving a, b, c, source;
    private double ax, bx, cx, sx, sz;

    public boolean client(Minecraft mc)
    {
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
        int X = DevTestArena.X, Y = DevTestArena.Y, Z = DevTestArena.Z;
        ItemStack ak = new ItemStack(GameRegistry.findItem("deci", "ak74"));
        sweep(w);
        if (t == 20)
        {
            DevTestArena.build(w, p);
            a = spawn(w, Deci.newInfected(w), X - 50, Z);
            b = spawn(w, Deci.newInfected(w), X - 76, Z);
            ax = dist(a, X, Z);
            bx = dist(b, X, Z);
            if (a == null || b == null)
            {
                DevTestResults.value(name(), "phase 1 spawn refused", (a == null ? "a " : "") + (b == null ? "b" : ""));
            }
        }
        if (t == 30)
        {
            GunNoise.shot(p, ak);                     // the player at the centre fires
        }
        if (t == 20 + PHASE - 10)
        {
            check("unsuppressed: infected 50 away came closer by", ax - dist(a, X, Z), ax - dist(a, X, Z) >= 15, ">= 15");
            check("unsuppressed: infected 76 away (out of range) moved by", bx - dist(b, X, Z),
                  Math.abs(bx - dist(b, X, Z)) < 4, "< 4");
        }
        if (t == 20 + PHASE)
        {
            DevTestArena.build(w, p);
            c = spawn(w, Deci.newInfected(w), X - 30, Z);
            cx = dist(c, X, Z);
        }
        if (t == 30 + PHASE)
        {
            ItemStack quiet = ak.copy();
            quiet.stackTagCompound = new NBTTagCompound();
            quiet.stackTagCompound.setString("barrelAttach", "arSuppressor");
            GunNoise.shot(p, quiet);
        }
        if (t == 20 + 2 * PHASE - 10)
        {
            check("suppressed: infected 30 away came closer by", cx - dist(c, X, Z), cx - dist(c, X, Z) < 4, "< 4");
        }
        playerShotPhase(t, p, w, ak);
        // phases 3 and 4: a bandit fires and leaves; first a Soviet (an enemy) alone, then a bandit
        // (its own side) alone (together they would see and fight each other: correct, but no test)
        for (int k = 0; k < 2; k++)
        {
            int base = 20 + (2 + k) * PHASE;
            if (t == base)
            {
                DevTestArena.build(w, p);
                source = spawn(w, Deci.newBandit(w), X, Z);
                a = spawn(w, k == 0 ? Deci.newSoviet(w) : Deci.newBandit(w), X - 43, Z);
                heardIt = false;
                sx = source == null ? X : source.posX;
                sz = source == null ? Z : source.posZ;
                ax = dist(a, sx, sz);
            }
            if (t > base + 10)
            {
                watch(a, p);
            }
            if (t == base + 10 && source != null)
            {
                GunNoise.shot(source, ak);
                source.setDead();                     // fires and leaves: no fight at the spot
            }
            if (t == base + PHASE - 10)
            {
                state(k == 0 ? "Soviet state" : "bandit state", a);
                if (k == 0)
                {
                    check("NPC shot: Soviet (enemy) came closer by", ax - dist(a, sx, sz), ax - dist(a, sx, sz) >= 10, ">= 10");
                }
                else
                {
                    DevTestResults.check(name(), "NPC shot: bandit (same side) took the noise", heardIt, !heardIt,
                                         "false (a friend's shot; it may still chase a stray infected)");
                }
            }
        }
    }

    /** Phase 5: a real player shot as the game makes it: the held gun's ammo drops by one. */
    private void playerShotPhase(int t, EntityPlayerMP p, World w, ItemStack ak)
    {
        int base = 20 + 4 * PHASE, X = DevTestArena.X, Z = DevTestArena.Z;
        if (t == base)
        {
            DevTestArena.build(w, p);
            c = spawn(w, Deci.newInfected(w), X - 45, Z);
            cx = dist(c, X, Z);
            ItemStack gun = ak.copy();
            gun.stackTagCompound = new NBTTagCompound();
            gun.stackTagCompound.setInteger("ammo", 30);
            p.inventory.setInventorySlotContents(p.inventory.currentItem, gun);
        }
        if (t == base + 10 && p.getHeldItem() != null && p.getHeldItem().stackTagCompound != null)
        {
            p.getHeldItem().stackTagCompound.setInteger("ammo", 29);   // what the server does per shot
        }
        if (t == base + PHASE - 10)
        {
            check("player shot (ammo 30 -> 29): infected 45 away came closer by", cx - dist(c, X, Z),
                  cx - dist(c, X, Z) >= 15, ">= 15");
            p.inventory.setInventorySlotContents(p.inventory.currentItem, null);
        }
        for (int k = 0; k < 2; k++)
        {
            int b = base + (1 + k) * PHASE;
            boolean survival = k == 0;
            if (t == b)
            {
                DevTestArena.build(w, p);
                creative = p.capabilities.isCreativeMode;
                p.capabilities.isCreativeMode = !survival;
                p.capabilities.disableDamage = true;
                p.sendPlayerAbilities();
                p.setPositionAndUpdate(X + 0.5, DevTestArena.Y, Z + 0.5);
                a = spawn(w, Deci.newSoviet(w), X - 40, Z);
                ax = dist(a, X, Z);
                heardIt = engaged = false;
                ItemStack gun = ak.copy();
                gun.stackTagCompound = new NBTTagCompound();
                gun.stackTagCompound.setInteger("ammo", 30);
                p.inventory.setInventorySlotContents(p.inventory.currentItem, gun);
            }
            if (t == b + 10 && p.getHeldItem() != null && p.getHeldItem().stackTagCompound != null)
            {
                p.getHeldItem().stackTagCompound.setInteger("ammo", 29);
            }
            if (t > b + 10)
            {
                watch(a, p);
            }
            if (t == b + PHASE - 10)
            {
                double came = ax - dist(a, X, Z);
                state(survival ? "survival Soviet state" : "creative Soviet state", a);
                if (survival)
                {
                    // it walks toward the shot until it sees the player within 32 (Decimation's
                    // findTarget), then fights from there: 8 blocks closer and engaged is a pass
                    DevTestResults.check(name(), "survival player shot: Soviet 40 away heard / came / engaged",
                        heardIt + " / " + String.format("%.1f", came) + " / " + engaged,
                        heardIt && (came >= 10 || engaged && came >= 5), "heard, and came >= 10 or came >= 5 and engaged");
                }
                else
                {
                    DevTestResults.check(name(), "creative player shot: Soviet 40 away heard / came",
                        heardIt + " / " + String.format("%.1f", came), !heardIt && came < 5,
                        "false / < 5 (Decimation's NPCs ignore creative players)");
                }
                p.inventory.setInventorySlotContents(p.inventory.currentItem, null);
                p.capabilities.isCreativeMode = creative;
                p.capabilities.disableDamage = creative;
                p.sendPlayerAbilities();
                if (k == 1)
                {
                    DevTestArena.build(w, p);
                    done = true;
                }
            }
        }
    }

    private boolean creative;
    /** Since the last phase start: did the NPC under test take the noise, did it lock onto the player. */
    private boolean heardIt, engaged;

    /**
     * Infected keep spawning on the arena at night and every NPC turns to fight them (seen: a
     * bandit and both Soviets "attacking" a stray infected instead of anything under test):
     * remove every mob the test did not spawn itself (other kinds came too and killed a Soviet).
     */
    private void sweep(World w)
    {
        for (Object o : w.loadedEntityList)
        {
            Entity e = (Entity) o;
            if (e != a && e != b && e != c && e != source && !e.isDead && e instanceof EntityLiving
                && Math.abs(e.posX - DevTestArena.X) < 120 && Math.abs(e.posZ - DevTestArena.Z) < 120)
            {
                e.setDead();
            }
        }
    }

    private void watch(EntityLiving e, EntityPlayerMP p)
    {
        if (e != null)
        {
            heardIt |= e.getEntityData().hasKey("deciwgNoise");
            engaged |= ((net.minecraft.entity.EntityCreature) e).getEntityToAttack() == p;
        }
    }

    /** What an NPC was doing at the end of a phase: tier, gun, whether it still follows a noise, its target. */
    private void state(String what, EntityLiving e)
    {
        if (e == null)
        {
            DevTestResults.value(name(), what, "not spawned");
            return;
        }
        net.minecraft.nbt.NBTTagCompound d = e.getEntityData();
        DevTestResults.value(name(), what, d.getString(net.decimation.fixes.NpcLoadouts.TAG) + " / "
            + d.getString(net.decimation.fixes.NpcLoadouts.GUN_TAG) + " / noise " + d.hasKey("deciwgNoise")
            + " / target " + (e.getAttackTarget() == null ? "none" : e.getAttackTarget().getClass().getSimpleName())
            + " / attacking " + (((net.minecraft.entity.EntityCreature) e).getEntityToAttack() == null ? "none"
                : ((net.minecraft.entity.EntityCreature) e).getEntityToAttack().getClass().getSimpleName()
                  + (((net.minecraft.entity.EntityCreature) e).getEntityToAttack().isEntityAlive() ? "" : " (dead)"))
            + " / dead " + e.isDead + " / speed " + String.format("%.3f", e.getEntityAttribute(
                net.minecraft.entity.SharedMonsterAttributes.movementSpeed).getAttributeValue()));
    }

    private void check(String what, double v, boolean ok, String expected)
    {
        DevTestResults.check(name(), what, String.format("%.1f", v), ok, expected);
    }

    private static double dist(Entity e, double x, double z)
    {
        return e == null ? 0 : Math.sqrt((e.posX - x) * (e.posX - x) + (e.posZ - z) * (e.posZ - z));
    }

    /** Spawned on the arena floor (Decimation refuses some spawns: retried). */
    private static EntityLiving spawn(World w, EntityLiving e, double x, double z)
    {
        for (int i = 0; i < 20; i++)
        {
            e.setLocationAndAngles(x + 0.5, DevTestArena.Y, z + 0.5, 0, 0);
            if (w.spawnEntityInWorld(e))
            {
                return e;
            }
        }
        return null;
    }
}

package net.decimation.worldgen.devtest;

import net.decimation.fixes.VanillaMobs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

/**
 * Left click in survival (bug report 13 Oktober 2026, a friend's laptop: "left mouse click is not
 * usable, except gun it can shot, but cant click left mouse button with melee and hit block too").
 * Presses the physical left mouse button the way Minecraft does (key code -100: setKeyBindState and
 * onTick, as Minecraft.runTick does for a mouse event) and checks: a bare hand breaks dirt; after
 * holding a gun (Decimation unbinds the attack key while a gun is held) and switching back, it still
 * does; a fire axe hits a pig. Records the attack key code at each step.
 */
public class ClickTest extends DevTestMode
{
    public String name() { return "click"; }

    private static final int LEFT = -100;
    private volatile int ticks, phase;          // server phase: 1 dirt placed, 2 dirt again, 3 pig
    private volatile boolean checkDirt, checkPig, done;
    private int step, wait;
    private EntityPig pig;
    private float pigHealth;

    public boolean client(Minecraft mc)
    {
        if (mc.thePlayer == null || phase == 0)
        {
            return true;
        }
        wait++;
        mc.inGameHasFocus = true;          // Minecraft digs only while the window has focus (a test window has not)
        if (step == 0 && wait == 1)
        {
            mc.thePlayer.inventory.currentItem = 0;   // the client owns the selected slot
        }
        switch (step)
        {
            case 0:
                if (wait == 20)
                {
                    DevTestResults.value(name(), "attack key at start", mc.gameSettings.keyBindAttack.getKeyCode());
                    press(true);
                }
                if (wait == 21 || wait == 40)
                {
                    net.minecraft.util.MovingObjectPosition m = mc.objectMouseOver;
                    DevTestResults.value(name(), "target at dig tick " + wait, m == null ? "none" : m.typeOfHit + " " + m.blockX + ","
                        + m.blockY + "," + m.blockZ + " pressed "
                        + mc.gameSettings.keyBindAttack.getIsKeyPressed() + " screen " + mc.currentScreen + " creative "
                        + mc.playerController.isInCreativeMode() + " hand " + mc.thePlayer.getHeldItem() + " on ground "
                        + mc.thePlayer.onGround + " flying " + mc.thePlayer.capabilities.isFlying);
                }
                if (wait == 60)
                {
                    press(false);
                    checkDirt = true;
                    next(1);
                }
                break;
            case 1:
                if (wait == 10)
                {
                    mc.thePlayer.inventory.currentItem = 1;         // the gun
                }
                if (wait == 40)
                {
                    DevTestResults.value(name(), "attack key holding a gun", mc.gameSettings.keyBindAttack.getKeyCode());
                    mc.thePlayer.inventory.currentItem = 0;
                }
                if (wait == 60 && phase >= 2)
                {
                    DevTestResults.value(name(), "attack key after the gun", mc.gameSettings.keyBindAttack.getKeyCode());
                    press(true);
                }
                if (wait == 100)
                {
                    press(false);
                    checkDirt = true;
                    next(2);
                }
                break;
            case 2:
                if (wait == 10)
                {
                    mc.thePlayer.inventory.currentItem = 2;         // the fire axe
                }
                if (wait == 30 && phase >= 3)
                {
                    press(true);
                }
                if (wait == 32)
                {
                    press(false);
                }
                if (wait == 50)
                {
                    checkPig = true;
                    next(3);
                }
                break;
            default:
                if (wait > 20)
                {
                    done = true;
                }
        }
        return !done;
    }

    private static void press(boolean down)
    {
        KeyBinding.setKeyBindState(LEFT, down);
        if (down)
        {
            KeyBinding.onTick(LEFT);
        }
    }

    private void next(int s)
    {
        step = s;
        wait = 0;
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
        int x = DevTestArena.X, y = DevTestArena.Y, z = DevTestArena.Z - 2;
        if (t == 20)
        {
            DevTestArena.build(w, p);
            p.setGameType(WorldSettings.GameType.SURVIVAL);
            p.capabilities.disableDamage = true;
            p.capabilities.isFlying = false;
            p.inventory.setInventorySlotContents(0, null);
            p.inventory.setInventorySlotContents(1, stack("deci:ak74"));
            p.inventory.setInventorySlotContents(2, stack("deci:ItemFireaxe"));
            p.inventory.currentItem = 0;
            look(p);
            w.setBlock(x, y, z, Blocks.dirt);
            phase = 1;
        }
        if (phase > 0 && phase < 3 && t % 40 == 0)
        {
            look(p);                                      // not every tick: a teleport each tick keeps the player off the ground (digging x 1/5)
        }
        if (checkDirt)
        {
            checkDirt = false;
            boolean broken = w.getBlock(x, y, z) != Blocks.dirt;
            DevTestResults.check(name(), phase == 1 ? "bare hand breaks dirt" : "bare hand after a gun breaks dirt",
                                 broken ? "broken" : "still there", broken, "broken");
            if (phase == 1)
            {
                w.setBlock(x, y, z, Blocks.dirt);
                phase = 2;
            }
            else
            {
                w.setBlockToAir(x, y, z);
                pig = new EntityPig(w);
                pig.getEntityData().setBoolean(VanillaMobs.KEEP, true);
                pig.setPosition(x + 0.5, y, z + 0.5);
                w.spawnEntityInWorld(pig);
                pigHealth = pig.getHealth();
                phase = 3;
            }
        }
        if (phase == 3 && pig != null && !checkPig)
        {
            pig.setPosition(x + 0.5, y, z + 0.5);
            pig.motionX = pig.motionZ = 0;
        }
        if (checkPig)
        {
            checkPig = false;
            boolean hit = pig.isDead || pig.getHealth() < pigHealth;
            DevTestResults.check(name(), "fire axe hits a pig", pig.getHealth() + " of " + pigHealth, hit, "less health");
            pig.setDead();
            p.setGameType(WorldSettings.GameType.CREATIVE);
        }
    }

    private static void look(EntityPlayerMP p)
    {
        p.playerNetServerHandler.setPlayerLocation(DevTestArena.X + 0.5, DevTestArena.Y, DevTestArena.Z + 0.5, 180, 30);
    }

    private static ItemStack stack(String name)
    {
        Item i = (Item) Item.itemRegistry.getObject(name);
        return i == null ? null : new ItemStack(i);
    }
}

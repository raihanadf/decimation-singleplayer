package net.decimation.worldgen.devtest;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

/**
 * Locked doors and keycard screens (docs/decimation_code/blocks.md), the pair our bases, airports and wasteland
 * buildings use: Door_Emergency_1_Locked with a military keycard screen beside it, 1 up. The client right clicks the
 * screen as the player does. Checks: a security key (wrong level) does nothing, the military key opens the door, the
 * same key again shuts it (the screen toggles), and a door 3 blocks east of its screen never opens (the reader looks
 * at offsets -3 .. +2 only).
 */
public class KeycardTest extends DevTestMode
{
    public String name() { return "keycard"; }

    private volatile int ticks, step;
    private volatile boolean ready, ask;
    private volatile int sx, sy, sz, fx, fy, fz;
    private int wait;
    private int doorX, doorY, doorZ, farX;
    private WorldSettings.GameType modeBefore;

    public boolean client(Minecraft mc)
    {
        if (mc.thePlayer == null || !ready || ask)
        {
            return step < 5;
        }
        if (++wait < 15)
        {
            return true;
        }
        wait = 0;
        boolean far = step == 3;
        int x = far ? fx : sx, y = far ? fy : sy, z = far ? fz : sz;
        mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), x, y, z, 2,
                                               Vec3.createVectorHelper(x + 0.5, y + 0.5, z));
        ask = true;
        return step < 5;
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
            modeBefore = p.theItemInWorldManager.getGameType();
            p.setGameType(WorldSettings.GameType.SURVIVAL);
            p.capabilities.disableDamage = true;
            Block door = Block.getBlockFromName("deci:Door_Emergency_1_Locked");
            Block screen = Block.getBlockFromName("deci:BlockKeycardScreenMilitary");
            if (door == null || screen == null)
            {
                DevTestResults.check(name(), "door and screen blocks", "missing", false, "deci:Door_Emergency_1_Locked, BlockKeycardScreenMilitary");
                step = 5;
                return;
            }
            // pair 1: the door in a wall line at z - 3, the screen on the wall cell beside it, 1 up (as our generators place it)
            doorX = DevTestArena.X;
            doorY = DevTestArena.Y;
            doorZ = DevTestArena.Z - 3;
            placeDoor(w, door, doorX, doorY, doorZ);
            sx = doorX - 1;
            sy = doorY + 1;
            sz = doorZ;
            w.setBlock(sx, sy - 1, sz, net.minecraft.init.Blocks.stonebrick, 0, 3);
            w.setBlock(sx, sy, sz, screen, 3, 3);
            // pair 2: the door 3 east of its screen
            farX = DevTestArena.X + 6;
            placeDoor(w, door, farX + 3, doorY, doorZ);
            fx = farX;
            fy = doorY + 1;
            fz = doorZ;
            w.setBlock(fx, fy - 1, fz, net.minecraft.init.Blocks.stonebrick, 0, 3);
            w.setBlock(fx, fy, fz, screen, 3, 3);
            p.playerNetServerHandler.setPlayerLocation(DevTestArena.X + 2.5, DevTestArena.Y, DevTestArena.Z - 0.5, 180, 0);
            hold(p, "ItemSecurityKeycard");
            DevTestResults.value(name(), "door metadata at the start", String.valueOf(w.getBlockMetadata(doorX, doorY, doorZ)));
            ready = true;
            return;
        }
        if (!ask || ++wait < 8)
        {
            return;
        }
        wait = 0;
        boolean open = open(w, doorX, doorY, doorZ);
        switch (step)
        {
            case 0:
                DevTestResults.check(name(), "wrong level key does nothing", "open=" + open, !open, "shut");
                hold(p, "ItemMilitaryKeycard");
                break;
            case 1:
                DevTestResults.check(name(), "military key opens the door", "open=" + open + " meta " + w.getBlockMetadata(doorX, doorY, doorZ),
                                     open, "open");
                break;
            case 2:
                DevTestResults.check(name(), "the key again shuts it (toggle)", "open=" + open, !open, "shut");
                break;
            case 3:
                boolean farOpen = open(w, farX + 3, doorY, doorZ);
                DevTestResults.check(name(), "a door 3 east of the screen stays shut", "open=" + farOpen, !farOpen,
                                     "shut (reader reach -3 .. +2)");
                if (modeBefore != null)
                {
                    p.setGameType(modeBefore);
                }
                step = 5;
                return;
        }
        step++;
        ask = false;
    }

    /** A two block door like a placed one: bottom metadata 1 (facing), top metadata 8. */
    private static void placeDoor(World w, Block door, int x, int y, int z)
    {
        w.setBlock(x, y, z, door, 1, 2);
        w.setBlock(x, y + 1, z, door, 8, 2);
    }

    /** Vanilla door rule: the bottom half's metadata bit 4 is "open". */
    private static boolean open(World w, int x, int y, int z)
    {
        return (w.getBlockMetadata(x, y, z) & 4) != 0;
    }

    private static void hold(EntityPlayerMP p, String key)
    {
        p.inventory.setInventorySlotContents(p.inventory.currentItem, new ItemStack(GameRegistry.findItem("deci", key)));
    }
}

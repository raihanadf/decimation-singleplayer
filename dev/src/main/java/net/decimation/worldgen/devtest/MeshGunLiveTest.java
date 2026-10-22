package net.decimation.worldgen.devtest;

import java.util.ArrayList;
import java.util.List;

import net.decimation.fixes.Deci;
import net.decimation.fixes.gunpack.GunPacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Mesh guns (converter milestone 4, docs/superpowers/specs/2026-10-23-gun-mesh-design.md): a mesh group moves with its
 * placeholder part. Decimation's GunAnimation poses a gun by moving parts named in the .anib (matched by boxName) on their
 * rotation points; this test moves slideModel0 back 2 and ammoModel0 down 3 the same way (Deci.nudgeGunPart) while the gun
 * is drawn at a fixed spot ahead by its own renderer (EQUIPPED, as gunpack perf draws it), and compares each picture with
 * the gun at rest. The like gun (Decimation's, box parts) goes through the same as the control. Decimation's own SlideBack
 * cannot be used: GunAnimation.get resets the animation's active and static flags on every held tick, so an empty gun's
 * slide shows back for a tick at most, on its guns too. Then a reload of the first mesh gun in the hand, for the eye
 * (meshgun_<gun>_reload_<tick>). gun=NAME picks one package gun, else every loaded mesh gun; control=false leaves the
 * like guns out; vmmap=true reads the process and graphics memory (vmmap -summary) around the first gun's first draw.
 */
public class MeshGunLiveTest extends DevTestMode
{
    public String name() { return "meshgun"; }

    private static final int STEP = 60, REST = 20, SLID = 30, DROPPED = 40;
    private static final boolean VMMAP = "true".equals(System.getProperty("deciworldgen.autotest.vmmap"));
    private static final String[][] POSES = {{"slideModel0", "-2", "0"}, {"ammoModel0", "0", "3"}};
    private static final int[] RELOAD_SHOTS = {6, 14, 22, 30, 38, 46};
    private final List<Item> guns = new ArrayList<Item>();
    private final List<String> names = new ArrayList<String>();
    private final List<Boolean> controls = new ArrayList<Boolean>();
    private Item firstMag;
    private volatile int ticks;
    private int clientTicks;
    private volatile boolean handOver;
    private boolean listed;
    private java.awt.image.BufferedImage rest;
    private Ahead ahead;
    private final boolean[] meshHas = new boolean[POSES.length];

    private synchronized void list()
    {
        if (listed)
        {
            return;
        }
        listed = true;
        String only = System.getProperty("deciworldgen.autotest.gun", "");
        for (GunPacks.Loaded l : GunPacks.loaded())
        {
            if (l.pack.mesh == null || !only.isEmpty() && !only.equals(l.pack.name))
            {
                continue;
            }
            if (firstMag == null && !Deci.gunMagazines(l.gun).isEmpty())
            {
                firstMag = Deci.gunMagazines(l.gun).get(0);    // its own or the like gun's ("magazine": "like")
            }
            guns.add(l.gun);
            names.add(l.pack.name);
            controls.add(false);
            if (!"false".equals(System.getProperty("deciworldgen.autotest.control")))
            {
                guns.add(l.like);                                // the control: the like gun's own box parts
                names.add(Item.itemRegistry.getNameForObject(l.like).replace("deci:", "") + "_for_" + l.pack.name);
                controls.add(true);
            }
        }
    }

    private int reloadAt()
    {
        return 20 + guns.size() * STEP + 40;
    }

    public boolean client(Minecraft mc)
    {
        if (mc.thePlayer == null)
        {
            return true;
        }
        list();
        if (guns.isEmpty())
        {
            DevTestResults.check(name(), "mesh guns loaded", "none", false, "a mesh package (devtest.py --gunpacks=DIR)");
            return false;
        }
        int t = ++clientTicks;                                  // its own count: a blocking vmmap skips server ticks
        if (ticks < 6)
        {
            clientTicks = 0;                                     // the server builds the arena first
            return true;
        }
        mc.gameSettings.hideGUI = true;
        mc.gameSettings.thirdPersonView = 0;
        mc.thePlayer.rotationYaw = 180;
        mc.thePlayer.rotationPitch = 0;
        int i = (t - 20) / STEP, k = (t - 20) % STEP;
        if (VMMAP && (t == 16 || t == 20 + REST + 2))           // around the first gun's first draw (its lists compile)
        {
            DevTestResults.value(name(), "memory " + (t == 16 ? "before " : "after ") + names.get(0) + "'s first draw",
                                 RefugeVisitTest.rss() + " (MB resident | vmmap)");
        }
        if (t >= 20 && i < guns.size())
        {
            mc.thePlayer.inventory.currentItem = 8;              // an empty hand: only the gun ahead is drawn
            if (ahead == null)
            {
                ahead = new Ahead();
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(ahead);
            }
            ahead.stack = stack(guns.get(i), 5);
            if (k < REST)
            {
                Deci.stopGunAnimation();                         // a reload left running would undo the poses below
            }
            if (k == REST)
            {
                rest = shot(mc, "meshgun_" + names.get(i) + "_rest");
            }
            for (int q = 0; q < POSES.length; q++)
            {
                int at = q == 0 ? SLID : DROPPED;
                float dx = Float.parseFloat(POSES[q][1]), dy = Float.parseFloat(POSES[q][2]);
                if (k == at - 6)
                {
                    boolean control = controls.get(i), skip = control && !meshHas[q];   // a control only for groups the mesh gun has
                    boolean moved = !skip && Deci.nudgeGunPart(guns.get(i), POSES[q][0], dx, dy, 0);
                    if (!control)
                    {
                        meshHas[q] = moved;
                    }
                    if (!moved)
                    {
                        DevTestResults.value(name(), names.get(i) + " " + POSES[q][0], skip ? "no control (the mesh gun has no such group)"
                                             : "no such part (nothing in that group)");
                    }
                }
                if (k == at)
                {
                    java.awt.image.BufferedImage posed = shot(mc, "meshgun_" + names.get(i) + "_" + POSES[q][0]);
                    if ((!controls.get(i) || meshHas[q]) && Deci.nudgeGunPart(guns.get(i), POSES[q][0], -dx, -dy, 0))
                    {
                        int[] d = changed(rest, posed);
                        double share = d[0] / (double) Math.max(1, d[1]);
                        DevTestResults.check(name(), names.get(i) + " " + POSES[q][0] + " moved",
                                             String.format("%d px changed (%.2f %%), box %d,%d to %d,%d", d[0], 100 * share, d[2],
                                                           d[3], d[4], d[5]), d[0] >= 40 && share < 0.1,
                                             (!controls.get(i) ? "its mesh group follows the part" : "control, Decimation's own parts")
                                             + ": 40 px or more, under 10 % of the frame");
                    }
                }
            }
        }
        if (i == guns.size() && ahead != null)
        {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(ahead);
            ahead = null;
            mc.thePlayer.inventory.currentItem = 0;
        }
        int r = t - reloadAt();
        if (r == -30)
        {
            handOver = true;                                     // the server puts the gun and a magazine in the hotbar
        }
        if (r >= -10)
        {
            mc.thePlayer.inventory.currentItem = 0;
        }
        for (int s : RELOAD_SHOTS)
        {
            if (r == s)
            {
                DevTestUtil.screenshot(mc, name(), "meshgun_" + names.get(0) + "_reload_" + s);
            }
        }
        if (r == 0)
        {
            KeyBinding.setKeyBindState(Deci.reloadKey().getKeyCode(), true);
            KeyBinding.onTick(Deci.reloadKey().getKeyCode());   // one press, as the keyboard makes it
        }
        if (r == 3)
        {
            KeyBinding.setKeyBindState(Deci.reloadKey().getKeyCode(), false);
        }
        if (r > RELOAD_SHOTS[RELOAD_SHOTS.length - 1] + 2)
        {
            mc.gameSettings.hideGUI = false;
            return false;
        }
        return true;
    }

    public void server()
    {
        int t = ++ticks;
        EntityPlayerMP p = DevTestUtil.player();
        list();
        if (p == null || guns.isEmpty())
        {
            return;
        }
        if (t == 5)
        {
            DevTestArena.build(p.worldObj, p);
            p.worldObj.setWorldTime(6000);
            p.capabilities.isFlying = true;
            for (int s = 0; s < 9; s++)
            {
                p.inventory.mainInventory[s] = null;
            }
            p.inventoryContainer.detectAndSendChanges();
        }
        if (handOver)
        {
            handOver = false;
            p.inventory.currentItem = 0;
            p.inventory.mainInventory[0] = stack(guns.get(0), 0);
            if (firstMag != null)                                // never a stack of no item: the client ticks it (NPE)
            {
                ItemStack m = new ItemStack(firstMag);
                m.stackTagCompound = new NBTTagCompound();
                m.stackTagCompound.setInteger("ammo", 30);
                p.inventory.mainInventory[1] = m;
            }
            p.inventoryContainer.detectAndSendChanges();
        }
    }

    private static ItemStack stack(Item gun, int rounds)
    {
        ItemStack g = new ItemStack(gun);
        g.stackTagCompound = new NBTTagCompound();
        g.stackTagCompound.setInteger("ammo", rounds);
        return g;
    }

    /** The gun drawn by its own renderer (EQUIPPED) three blocks ahead, laid flat side on and larger, each frame. */
    public static final class Ahead
    {
        static final float ROLL = Float.parseFloat(System.getProperty("deciworldgen.autotest.roll", "90"));
        volatile ItemStack stack;
        private net.minecraft.client.renderer.RenderBlocks blocks;

        @cpw.mods.fml.common.eventhandler.SubscribeEvent
        public void onWorldLast(net.minecraftforge.client.event.RenderWorldLastEvent e)
        {
            ItemStack s = stack;
            Minecraft mc = Minecraft.getMinecraft();
            if (s == null || mc.thePlayer == null)
            {
                return;
            }
            net.minecraftforge.client.IItemRenderer r = net.minecraftforge.client.MinecraftForgeClient.getItemRenderer(s,
                net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED);
            if (r == null)
            {
                return;
            }
            if (blocks == null)
            {
                blocks = new net.minecraft.client.renderer.RenderBlocks();
            }
            org.lwjgl.opengl.GL11.glPushMatrix();
            org.lwjgl.opengl.GL11.glTranslated(0, -0.3, -3);                       // the camera looks north (yaw 180)
            org.lwjgl.opengl.GL11.glRotatef(ROLL, 0, 0, 1);                        // EQUIPPED stands it up: lay it flat
            org.lwjgl.opengl.GL11.glScalef(2.6f, 2.6f, 2.6f);
            r.renderItem(net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED, s, blocks, mc.thePlayer);
            org.lwjgl.opengl.GL11.glPopMatrix();
        }
    }

    private java.awt.image.BufferedImage shot(Minecraft mc, String file)
    {
        DevTestUtil.screenshot(mc, name(), file);
        try
        {
            return javax.imageio.ImageIO.read(new java.io.File(mc.mcDataDir, "screenshots/" + file)); // saved without an extension
        }
        catch (java.io.IOException e)
        {
            return null;
        }
    }

    /** {pixels differing by more than 24 in a channel, pixels compared, the box of the changes x0, y0, x1, y1}. */
    private static int[] changed(java.awt.image.BufferedImage a, java.awt.image.BufferedImage b)
    {
        if (a == null || b == null || a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight())
        {
            return new int[] {-1, 1, 0, 0, 0, 0};
        }
        int n = 0, x0 = Integer.MAX_VALUE, y0 = Integer.MAX_VALUE, x1 = -1, y1 = -1;
        for (int y = 0; y < a.getHeight(); y++)
        {
            for (int x = 0; x < a.getWidth(); x++)
            {
                int p = a.getRGB(x, y), q = b.getRGB(x, y);
                if (Math.abs((p >> 16 & 255) - (q >> 16 & 255)) > 24 || Math.abs((p >> 8 & 255) - (q >> 8 & 255)) > 24
                    || Math.abs((p & 255) - (q & 255)) > 24)
                {
                    n++;
                    x0 = Math.min(x0, x);
                    y0 = Math.min(y0, y);
                    x1 = Math.max(x1, x);
                    y1 = Math.max(y1, y);
                }
            }
        }
        return new int[] {n, a.getWidth() * a.getHeight(), x0, y0, x1, y1};
    }
}

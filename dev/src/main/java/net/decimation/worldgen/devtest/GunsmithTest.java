package net.decimation.worldgen.devtest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.decimation.fixes.gunsmith.GunSlot;
import net.decimation.fixes.gunsmith.GunsmithAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;

/**
 * The Gunsmith screen (docs/superpowers/plans/2026-10-21-gunsmith-screen.md, Task 4: server side). Decimation's attach
 * key (the way its client tick reads it) opens our container for the held gun; list actions (container button ids) fit,
 * swap and take off parts by Decimation's own rules; a full inventory refuses; the gun never leaves its hotbar slot and
 * closing drops nothing; a replaced held stack closes the screen and keeps the parts; death drops exactly what was held.
 * Survival on the test arena, mob spawning off. Inventory: 0 M4A4, 1 free, 2 two 4x, 3 reddot, 4 AR suppressor,
 * 5 foregrip, 6 M4A4 paint, 7 AK74 paint, 8 tracer powder, 9 AS Val (built in suppressor), 10 AUG (built in scope).
 */
public class GunsmithTest extends DevTestMode
{
    public String name() { return "gunsmith"; }

    private static final String SCREEN = "GunsmithScreen";
    private int step, ticks;
    private volatile Runnable onServer;
    private volatile boolean serverDone = true;
    private final List<String> chat = Collections.synchronizedList(new ArrayList<String>());
    private static Chat listener;
    /** Server readings, written on the server thread by read(). */
    private volatile String held = "", sight = "", barrel = "", skin = "", wantSkin = "";
    private volatile boolean tracerSet;
    private volatile int count4x, countReddot, countM4Paint, countAkPaint, countTracer, heldId, heldIndex, loose, dropped,
                         carried, found = -1;
    private int openHeldId, windowId;

    /** Catches the corpse a death makes (any inventory entity but a minecart joining on the server). */
    public static final class Joins
    {
        volatile GunsmithTest test;

        @SubscribeEvent(priority = cpw.mods.fml.common.eventhandler.EventPriority.LOWEST, receiveCanceled = true)
        public void join(net.minecraftforge.event.entity.EntityJoinWorldEvent e)
        {
            GunsmithTest t = test;
            if (t != null && !e.world.isRemote && e.entity instanceof IInventory && !(e.entity instanceof net.minecraft.entity.item.EntityMinecart))
            {
                t.watched = e.entity;
            }
        }
    }

    private static Joins joins;
    /** The corpse that joined last: Decimation moves a dead player's items into one (DeathHandler), and its chunk may
     *  unload (and save it) within a few ticks of the death, so it is counted the moment it joins. */
    volatile Entity watched;


    public static final class Chat
    {
        volatile GunsmithTest test;

        @SubscribeEvent
        public void chat(ClientChatReceivedEvent e)
        {
            GunsmithTest t = test;
            if (t != null && e.message != null)
            {
                t.chat.add(e.message.getUnformattedText());
            }
        }
    }

    public boolean client(Minecraft mc)
    {
        if (listener == null)
        {
            listener = new Chat();
            MinecraftForge.EVENT_BUS.register(listener);
        }
        listener.test = this;
        if (joins == null)
        {
            joins = new Joins();
            MinecraftForge.EVENT_BUS.register(joins);
        }
        joins.test = this;
        net.decimation.worldgen.DevAutoTest.keepScreen = true;
        if (mc.thePlayer == null || !serverDone)
        {
            return true;
        }
        ticks++;
        switch (step)
        {
            case 0: // the arena and the inventory
                server(this::setup);
                next();
                break;
            case 1: // no gun in hand: nothing opens, a short message
                if (ticks == 1) mc.thePlayer.inventory.currentItem = 1;
                if (ticks == 6) pressAttach(mc);
                if (ticks == 40)
                {
                    check("no gun: nothing opens", screenName(mc), mc.currentScreen == null, "no screen");
                    check("no gun: message", String.join(" | ", chat), said("Hold a gun to modify it") > 0, "Hold a gun to modify it");
                    mc.thePlayer.closeScreen();
                    next();
                }
                break;
            case 2: // gun in hand: the Gunsmith screen for it
                if (ticks == 1) mc.thePlayer.inventory.currentItem = 0;
                if (ticks == 6) pressAttach(mc);
                if (ticks == 50)
                {
                    check("gun in hand opens the Gunsmith screen", screenName(mc), SCREEN.equals(screenName(mc)), SCREEN);
                    Object slot = field(mc.thePlayer.openContainer, "heldSlot");
                    check("the container holds the selected slot", slot, Integer.valueOf(0).equals(slot), "0");
                    windowId = mc.thePlayer.openContainer.windowId;
                    server(this::read);
                    next();
                }
                break;
            case 3: // fit a 4x from a stack of two
                if (ticks == 1) openHeldId = heldId;
                if (ticks == 2) act(mc, GunsmithAction.fit(GunSlot.SIGHT, 2));
                if (ticks == 14) server(this::read);
                if (ticks == 16)
                {
                    check("fit: the gun has the 4x", sight, "4x".equals(sight), "sightAttach 4x");
                    check("fit: one 4x taken", count4x, count4x == 1, "1 left");
                    next();
                }
                break;
            case 4: // swap to the reddot: the 4x comes back
                if (ticks == 1) server(() -> found = find(Item.itemRegistry.getObject("deci:reddot")));
                if (ticks == 3) act(mc, GunsmithAction.fit(GunSlot.SIGHT, found));
                if (ticks == 15) server(this::read);
                if (ticks == 17)
                {
                    check("swap: the gun has the reddot", sight, "reddot".equals(sight), "sightAttach reddot");
                    check("swap: the 4x came back", count4x, count4x == 2, "2");
                    check("swap: the reddot left the inventory", countReddot, countReddot == 0, "0");
                    next();
                }
                break;
            case 5: // take off
                if (ticks == 1) act(mc, GunsmithAction.takeOff(GunSlot.SIGHT));
                if (ticks == 13) server(this::read);
                if (ticks == 15)
                {
                    check("take off: no sight", sight, "".equals(sight), "sightAttach empty");
                    check("take off: the reddot is back", countReddot, countReddot == 1, "1");
                    next();
                }
                break;
            case 6: // a full inventory refuses what could not come back
                if (ticks == 1) server(() -> found = find(Item.itemRegistry.getObject("deci:reddot")));
                if (ticks == 3) act(mc, GunsmithAction.fit(GunSlot.SIGHT, found));
                if (ticks == 15) server(() -> { twoFourX(); fillWithDirt(); });
                if (ticks == 17) act(mc, GunsmithAction.fit(GunSlot.SIGHT, 2)); // the stack of two 4x: the reddot could not come back
                if (ticks == 29) act(mc, GunsmithAction.takeOff(GunSlot.SIGHT));
                if (ticks == 41) server(this::read);
                if (ticks == 43)
                {
                    check("full: nothing changed", sight, "reddot".equals(sight), "sightAttach still reddot");
                    check("full: told twice", said("Inventory full"), said("Inventory full") >= 2, "Inventory full x2");
                    check("full: the 4x stack kept", count4x, count4x == 2, "2");
                    server(this::clearDirt);
                    next();
                }
                break;
            case 7: // rules: built in suppressor and scope, paint of another gun
                if (ticks == 1) mc.thePlayer.closeScreen();
                if (ticks == 2) server(() -> put(1, "deci:asval"));
                if (ticks == 4) mc.thePlayer.inventory.currentItem = 1;
                if (ticks == 8) pressAttach(mc);
                if (ticks == 40) act(mc, GunsmithAction.fit(GunSlot.BARREL, 4));
                if (ticks == 52) server(this::read);
                if (ticks == 54)
                {
                    check("rules: no suppressor on the AS Val", held + " barrel '" + barrel + "'", "".equals(barrel), "refused");
                    mc.thePlayer.closeScreen();
                    server(() -> put(1, "deci:aug1"));
                }
                if (ticks == 60) pressAttach(mc);
                if (ticks == 92) act(mc, GunsmithAction.fit(GunSlot.SIGHT, 2));
                if (ticks == 104) server(this::read);
                if (ticks == 106)
                {
                    check("rules: no scope on the AUG", held + " sight '" + sight + "'", "".equals(sight), "refused");
                    mc.thePlayer.closeScreen();
                    mc.thePlayer.inventory.currentItem = 0;
                }
                if (ticks == 112) pressAttach(mc);
                if (ticks == 144) act(mc, GunsmithAction.fit(GunSlot.PAINT, 7));
                if (ticks == 156) server(this::read);
                if (ticks == 158)
                {
                    check("rules: no AK74 paint on the M4A4", "skin '" + skin + "', AK74 paint " + countAkPaint,
                          !skin.equals(wantSkin) && countAkPaint == 1, "refused, kept");
                    next();
                }
                break;
            case 8: // paint and tracer are used up
                if (ticks == 1) act(mc, GunsmithAction.fit(GunSlot.PAINT, 6));
                if (ticks == 2) act(mc, GunsmithAction.fit(GunSlot.TRACER, 8));
                if (ticks == 14) server(this::read);
                if (ticks == 16)
                {
                    check("paint: applied", skin + " (want " + wantSkin + ")", !wantSkin.isEmpty() && wantSkin.equals(skin), "the M4A4 paint's skin");
                    check("paint: used up", countM4Paint, countM4Paint == 0, "0");
                    check("tracer: applied", tracerSet, tracerSet, "tracerColorID set");
                    check("tracer: used up", countTracer, countTracer == 0, "0");
                    check("paint cannot be taken off", GunsmithAction.decode(GunsmithAction.takeOff(GunSlot.PAINT)),
                          GunsmithAction.decode(GunsmithAction.takeOff(GunSlot.PAINT)) == null, "no such action");
                    next();
                }
                break;
            case 9: // drag the suppressor from the hotbar onto the barrel slot
                if (ticks == 1) click(mc, 6 + 4, 0, 0);
                if (ticks == 3) click(mc, GunSlot.BARREL.ordinal(), 0, 0);
                if (ticks == 15) server(this::read);
                if (ticks == 17)
                {
                    check("drag: the gun has the suppressor", barrel, "arSuppressor".equals(barrel), "barrelAttach arSuppressor");
                    next();
                }
                break;
            case 10: // the held gun cannot be picked up or swapped away
                if (ticks == 1) click(mc, 6, 0, 0);
                if (ticks == 3) click(mc, GunSlot.SIGHT.ordinal(), 0, 2);
                if (ticks == 15) server(this::read);
                if (ticks == 17)
                {
                    check("locked: the same gun in hand", held + " id " + heldId, heldId == openHeldId && held.endsWith("m4a4"), "unchanged");
                    check("locked: nothing on the cursor", mc.thePlayer.inventory.getItemStack(), mc.thePlayer.inventory.getItemStack() == null, "empty");
                    next();
                }
                break;
            case 11: // I closes it, nothing dropped, the parts stay
                if (ticks == 1) typeAttach(mc);
                if (ticks == 20) server(this::read);
                if (ticks == 22)
                {
                    check("I closes the screen", screenName(mc), mc.currentScreen == null, "closed");
                    check("closing drops nothing", loose, loose == 0, "0 items on the ground");
                    check("the gun keeps its parts", barrel, "arSuppressor".equals(barrel) && heldIndex == 0, "barrel on, slot 0");
                    next();
                }
                break;
            case 12: // the held stack replaced while open: the screen closes, the copy keeps the parts
                if (ticks == 1) pressAttach(mc);
                if (ticks == 35) server(this::replaceHeld);
                if (ticks == 45) server(this::read);
                if (ticks == 47)
                {
                    check("replaced: the screen closed", screenName(mc), mc.currentScreen == null, "no screen");
                    check("replaced: the copy keeps its parts", barrel, "arSuppressor".equals(barrel), "barrelAttach arSuppressor");
                    mc.thePlayer.closeScreen();
                    next();
                }
                break;
            case 13: // death while open: the drops are what was held
                if (ticks == 1) pressAttach(mc);
                if (ticks == 35) server(this::die);
                if (ticks == 45)
                {
                    check("death: what was held, no more", dropped + " of " + carried, carried > 0 && dropped == carried, carried + " (dropped or kept)");
                    mc.thePlayer.respawnPlayer();
                    mc.displayGuiScreen(null);
                }
                if (ticks == 120)
                {
                    net.decimation.worldgen.DevAutoTest.keepScreen = false;
                    listener.test = null;
                    joins.test = null;
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    public void server()
    {
        Runnable r = onServer;
        if (r != null)
        {
            onServer = null;
            try
            {
                r.run();
            }
            catch (RuntimeException e)
            {
                DevTestResults.value(name(), "server error", e.toString());
            }
            serverDone = true;
        }
    }

    // ---- client side

    private void next()
    {
        step++;
        ticks = 0;
    }

    private void server(Runnable r)
    {
        serverDone = false;
        onServer = r;
    }

    private void check(String what, Object value, boolean pass, String expected)
    {
        DevTestResults.check(name(), what, value, pass, expected);
    }

    private int said(String line)
    {
        int n = 0;
        synchronized (chat)
        {
            for (String s : chat)
            {
                n += s.contains(line) ? 1 : 0;
            }
        }
        return n;
    }

    private static String screenName(Minecraft mc)
    {
        return mc.currentScreen == null ? "none" : mc.currentScreen.getClass().getSimpleName();
    }

    private static KeyBinding attachKey(Minecraft mc)
    {
        for (KeyBinding k : mc.gameSettings.keyBindings)
        {
            if ("key.decimation.gui.attach".equals(k.getKeyDescription()))
            {
                return k;
            }
        }
        throw new IllegalStateException("no key.decimation.gui.attach");
    }

    /** A press of Decimation's attach key the way its client tick reads it (KeyBinding.isPressed). */
    private static void pressAttach(Minecraft mc)
    {
        KeyBinding.onTick(attachKey(mc).getKeyCode());
    }

    /** The attach key typed into the open screen, the way a key reaches a GUI (GuiScreen.keyTyped). */
    private static void typeAttach(Minecraft mc)
    {
        GuiScreen s = mc.currentScreen;
        int code = attachKey(mc).getKeyCode();
        for (Class<?> c = s == null ? null : s.getClass(); c != null; c = c.getSuperclass())
        {
            for (String n : new String[] {"keyTyped", "func_73869_a"})
            {
                try
                {
                    java.lang.reflect.Method m = c.getDeclaredMethod(n, char.class, int.class);
                    m.setAccessible(true);
                    m.invoke(s, 'i', code);
                    return;
                }
                catch (NoSuchMethodException e)
                {
                    // the other name, then the parent class
                }
                catch (ReflectiveOperationException e)
                {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    /** A list click as the screen sends it: the container's button id. */
    private void act(Minecraft mc, int id)
    {
        mc.playerController.sendEnchantPacket(mc.thePlayer.openContainer.windowId, id);
    }

    /** A slot click as the screen sends it (mode 0 pick up / put, 2 hotbar swap with button as the hotbar index). */
    private void click(Minecraft mc, int slot, int button, int mode)
    {
        mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot, button, mode, mc.thePlayer);
    }

    private static Object field(Object o, String name)
    {
        for (Class<?> c = o.getClass(); c != null; c = c.getSuperclass())
        {
            try
            {
                java.lang.reflect.Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(o);
            }
            catch (NoSuchFieldException e)
            {
                // the parent class
            }
            catch (IllegalAccessException e)
            {
                return null;
            }
        }
        return null;
    }

    // ---- server side (run on the server thread through server(Runnable))

    private static EntityPlayerMP player()
    {
        WorldServer w = MinecraftServer.getServer().worldServerForDimension(0);
        return w.playerEntities.isEmpty() ? null : (EntityPlayerMP) w.playerEntities.get(0);
    }

    private static Item byName(String prefix)
    {
        Item best = null;
        for (Object o : Item.itemRegistry)
        {
            Item i = (Item) o;
            String n = i.getUnlocalizedName();
            if (n != null && n.startsWith(prefix) && (best == null || n.contains("Orange")))
            {
                best = i;
            }
        }
        return best;
    }

    private static ItemStack stack(String name, int n)
    {
        Object i = Item.itemRegistry.getObject(name);
        return i == null ? null : new ItemStack((Item) i, n);
    }

    private void setup()
    {
        EntityPlayerMP p = player();
        WorldServer w = (WorldServer) p.worldObj;
        w.getGameRules().setOrCreateGameRule("doMobSpawning", "false");
        DevTestArena.build(w, p);
        p.setGameType(WorldSettings.GameType.SURVIVAL);
        p.setPositionAndUpdate(DevTestArena.X + 0.5, DevTestArena.Y, DevTestArena.Z + 0.5);
        for (int i = 0; i < p.inventory.mainInventory.length; i++)
        {
            p.inventory.mainInventory[i] = null;
        }
        Item m4Paint = byName("item.spraycan.m4a4."), akPaint = byName("item.spraycan.ak74."), tracer = byName("item.ItemPyroPowder");
        wantSkin = m4Paint == null ? "" : m4Paint.getUnlocalizedName().substring("item.spraycan.m4a4.".length());
        p.inventory.mainInventory[0] = stack("deci:m4a4", 1);
        p.inventory.mainInventory[2] = stack("deci:4x", 2);
        p.inventory.mainInventory[3] = stack("deci:reddot", 1);
        p.inventory.mainInventory[4] = stack("deci:arSuppressor", 1);
        p.inventory.mainInventory[5] = stack("deci:foregrip", 1);
        p.inventory.mainInventory[6] = m4Paint == null ? null : new ItemStack(m4Paint);
        p.inventory.mainInventory[7] = akPaint == null ? null : new ItemStack(akPaint);
        p.inventory.mainInventory[8] = tracer == null ? null : new ItemStack(tracer);
        p.inventory.mainInventory[9] = stack("deci:asval", 1);
        p.inventory.mainInventory[10] = stack("deci:aug1", 1);
        p.inventory.currentItem = 1;
        p.inventoryContainer.detectAndSendChanges();
        DevTestResults.value(name(), "paints and tracer", m4Paint + ", " + akPaint + ", " + tracer + ", skin " + wantSkin);
    }

    private static String nbt(ItemStack s, String key)
    {
        return s != null && s.hasTagCompound() && s.getTagCompound().hasKey(key) ? s.getTagCompound().getString(key) : "";
    }

    private int countOf(EntityPlayerMP p, Item item)
    {
        int n = 0;
        for (ItemStack s : p.inventory.mainInventory)
        {
            n += s != null && item != null && s.getItem() == item ? s.stackSize : 0;
        }
        return n;
    }

    private int find(Object item)
    {
        EntityPlayerMP p = player();
        for (int i = 0; i < p.inventory.mainInventory.length; i++)
        {
            ItemStack s = p.inventory.mainInventory[i];
            if (s != null && s.getItem() == item)
            {
                return i;
            }
        }
        return -1;
    }

    private void read()
    {
        EntityPlayerMP p = player();
        ItemStack h = p.inventory.getCurrentItem();
        held = h == null ? "nothing" : String.valueOf(Item.itemRegistry.getNameForObject(h.getItem()));
        heldId = System.identityHashCode(h);
        heldIndex = p.inventory.currentItem;
        sight = nbt(h, GunSlot.SIGHT.nbtKey);
        barrel = nbt(h, GunSlot.BARREL.nbtKey);
        skin = nbt(h, GunSlot.PAINT.nbtKey);
        tracerSet = h != null && h.hasTagCompound() && h.getTagCompound().hasKey(GunSlot.TRACER.nbtKey);
        count4x = countOf(p, (Item) Item.itemRegistry.getObject("deci:4x"));
        countReddot = countOf(p, (Item) Item.itemRegistry.getObject("deci:reddot"));
        countM4Paint = countOf(p, byName("item.spraycan.m4a4."));
        countAkPaint = countOf(p, byName("item.spraycan.ak74."));
        countTracer = countOf(p, byName("item.ItemPyroPowder"));
        loose = p.worldObj.getEntitiesWithinAABB(EntityItem.class, p.boundingBox.expand(4, 4, 4)).size();
    }

    /** Exactly one stack of two 4x, at index 2 (attachments do not stack on their own). */
    private void twoFourX()
    {
        EntityPlayerMP p = player();
        Object fourX = Item.itemRegistry.getObject("deci:4x");
        for (int i = 0; i < p.inventory.mainInventory.length; i++)
        {
            ItemStack s = p.inventory.mainInventory[i];
            if (s != null && s.getItem() == fourX)
            {
                p.inventory.mainInventory[i] = null;
            }
        }
        p.inventory.mainInventory[2] = stack("deci:4x", 2);
    }

    private void fillWithDirt()
    {
        EntityPlayerMP p = player();
        for (int i = 0; i < p.inventory.mainInventory.length; i++)
        {
            if (p.inventory.mainInventory[i] == null)
            {
                p.inventory.mainInventory[i] = new ItemStack(Blocks.dirt, 64);
            }
        }
        p.openContainer.detectAndSendChanges();
    }

    private void clearDirt()
    {
        EntityPlayerMP p = player();
        Item dirt = Item.getItemFromBlock(Blocks.dirt);
        for (int i = 0; i < p.inventory.mainInventory.length; i++)
        {
            ItemStack s = p.inventory.mainInventory[i];
            if (s != null && s.getItem() == dirt)
            {
                p.inventory.mainInventory[i] = null;
            }
        }
        p.openContainer.detectAndSendChanges();
    }

    private void put(int index, String name)
    {
        EntityPlayerMP p = player();
        p.inventory.mainInventory[index] = stack(name, 1);
        p.openContainer.detectAndSendChanges();
    }

    private void replaceHeld()
    {
        EntityPlayerMP p = player();
        ItemStack h = p.inventory.getCurrentItem();
        p.inventory.setInventorySlotContents(p.inventory.currentItem, h == null ? null : h.copy());
    }

    private void die()
    {
        EntityPlayerMP p = player();
        int n = 0;
        for (ItemStack s : p.inventory.mainInventory)
        {
            n += s == null ? 0 : 1;
        }
        for (ItemStack s : p.inventory.armorInventory)
        {
            n += s == null ? 0 : 1;
        }
        carried = n;
        watched = null;
        p.attackEntityFrom(DamageSource.outOfWorld, 1000);
        countDrops(); // at once: the corpse's chunk can unload (and save it) a few ticks later
    }


    /** Stacks lying near the body: loose items and whatever a corpse holds. */
    private void countDrops()
    {
        EntityPlayerMP p = player();
        int n = p.worldObj.getEntitiesWithinAABB(EntityItem.class, p.boundingBox.expand(16, 16, 16)).size();
        Entity corpse = watched;
        if (corpse instanceof IInventory)
        {
            IInventory inv = (IInventory) corpse;
            for (int i = 0; i < inv.getSizeInventory(); i++)
            {
                n += inv.getStackInSlot(i) == null ? 0 : 1;
            }
        }
        int kept = 0;
        for (ItemStack s : p.inventory.mainInventory)
        {
            kept += s == null ? 0 : 1;
        }
        for (ItemStack s : p.inventory.armorInventory)
        {
            kept += s == null ? 0 : 1;
        }
        dropped = n + kept;
        DevTestResults.value(name(), "death", (corpse == null ? "no corpse" : "a corpse") + ", kept " + kept);
    }
}

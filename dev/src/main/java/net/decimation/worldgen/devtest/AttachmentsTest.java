package net.decimation.worldgen.devtest;

import java.util.List;
import java.util.Map;

import net.decimation.fixes.gunsmith.GunParts;
import net.decimation.fixes.gunsmith.GunSlot;
import net.decimation.fixes.Deci;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

/**
 * The new attachments (docs/superpowers/specs/2026-10-21-new-attachments-design.md): the RK-1 Foregrip and the ELCAN
 * SpecterDR 1x/4x are registered with their names, fit what the spec says (the Gunsmith's own rule, GunParts.fits) and
 * lie in Decimation's loot wherever the Foregrip / the 4x lie; recoil per grip; grips placed per gun; the SpecterDR's
 * 1x / 4x switch.
 */
public class AttachmentsTest extends DevTestMode
{
    public String name() { return "attachments"; }

    private int ticks, step, grip;
    private volatile Runnable onServer;
    /** Recoil per grip (none, Foregrip, RK-1): the view's turn up and sideways in one recoil tick. */
    private static final String[] GRIPS = {"", "foregrip", "rk1"};
    private final double[][] sums = new double[GRIPS.length][4];

    public boolean client(Minecraft mc)
    {
        ticks++;
        switch (step)
        {
            case 0:
                if (ticks == 5)
                {
                    items();
                    step++;
                    ticks = 0;
                }
                return true;
            case 1:
                if (!recoil(mc))
                {
                    step++;
                    ticks = 0;
                }
                return true;
            case 2:
                if (!placement(mc))
                {
                    step++;
                    ticks = 0;
                }
                return true;
            case 3:
                return specter(mc);
            default:
                return false;
        }
    }

    public void server()
    {
        Runnable r = onServer;
        if (r != null)
        {
            onServer = null;
            r.run();
        }
    }

    /**
     * Each grip in turn on an M4A4 in the hand: a known recoil (1000 up, 1000 sideways) set in PlayerData, then one run
     * of Decimation's own GunItem.clientHeldTick, the code that turns the view (Deci.runClientHeldTick: no sway or
     * server sync in between). It keeps 4 % of the recoil and turns the view by it, divided by the grip's factor: 40 x
     * the factor up and sideways. The ratios against no grip are the factors (spec section 2). A real shot cannot be
     * measured: its turn is that small 4 % and Decimation's view sway moves the view more per tick (seen).
     */
    private boolean recoil(Minecraft mc)
    {
        if (ticks == 1)
        {
            onServer = () -> {
                EntityPlayerMP p = player();
                ItemStack gun = new ItemStack((Item) Item.itemRegistry.getObject("deci:m4a4"));
                gun.stackTagCompound = new NBTTagCompound();
                gun.stackTagCompound.setInteger("ammo", 30);
                p.inventory.setInventorySlotContents(0, gun);
                p.inventory.currentItem = 0;
            };
            mc.thePlayer.inventory.currentItem = 0;
        }
        if (ticks == 20)
        {
            ItemStack held = mc.thePlayer.getHeldItem();
            Deci.Player d = Deci.player(mc.thePlayer);
            for (int g = 0; g < GRIPS.length; g++)
            {
                if (held == null || held.stackTagCompound == null || d == null)
                {
                    break;
                }
                held.stackTagCompound.setString("gripAttach", GRIPS[g]);
                mc.thePlayer.rotationPitch = 0;
                d.setRecoil(1000, 1000);
                float p0 = mc.thePlayer.rotationPitch, y0 = mc.thePlayer.rotationYawHead;
                try
                {
                    Deci.runClientHeldTick(held, mc.thePlayer);
                }
                catch (Exception e)
                {
                    DevTestResults.value(name(), "recoil tick failed", String.valueOf(e));
                }
                sums[g][0] = p0 - mc.thePlayer.rotationPitch;
                sums[g][2] = y0 - mc.thePlayer.rotationYawHead;
                sums[g][1] = sums[g][3] = 1;
                d.setRecoil(0, 0);
                mc.thePlayer.rotationPitch = 0;
            }
            if (held != null && held.stackTagCompound != null)
            {
                held.stackTagCompound.setString("gripAttach", "");
            }
            double up0 = sums[0][0], side0 = sums[0][2];
            DevTestResults.value(name(), "recoil: no grip", String.format("up %.3f, sideways %.3f degrees", up0, side0));
            ratio("recoil: Foregrip", sums[1], up0, side0, 1 / 3.0, 1 / 3.0);
            ratio("recoil: RK-1", sums[2], up0, side0, 0.5, 0.25);
            return false;
        }
        return true;
    }

    /**
     * Grips placed per gun (GripPlacement at startup): on the M4A4 Decimation's Foregrip, the Laser and our RK-1 have
     * their own moved copy (not the one spot for all guns), the Foregrip's parent part without its stray plate; shots of
     * four grips on their guns in the Gunsmith screen (Decimation's renderer draws them as in the world).
     */
    private boolean placement(Minecraft mc)
    {
        if (ticks == 1)
        {
            Item m4 = (Item) Item.itemRegistry.getObject("deci:m4a4");
            for (String g : new String[] {"foregrip", "rk1", "laser"})
            {
                float[] o = Deci.attachmentOffset(m4, g);
                DevTestResults.check(name(), "placement: " + g + " moved on the m4a4", o == null ? "the one spot" : String.format("%.2f %.2f %.2f",
                    o[0], o[1], o[2]), o != null && (o[0] != 0 || o[1] != 0 || o[2] != 0), "its own offset from grip_offsets.txt");
            }
            Map<String, Integer> parts = Deci.attachmentPartBoxes(m4, "foregrip");
            boolean plain = parts != null && Integer.valueOf(0).equals(parts.get("grip"))
                            && parts.keySet().stream().noneMatch(n -> n.startsWith("ammoModel"));
            DevTestResults.check(name(), "placement: Foregrip without its stray plate", parts == null ? "no copy" : "own boxes " + parts,
                                 plain, "{grip=0}: the parent of its 8 parts drawn without its plate, no magazine");
        }
        // a grip on two rifles, an SMG and a pistol (the RK-1 and the Foregrip fit rifles only, Decimation's rule)
        String[][] shots = {{"deci:m4a4", "rk1"}, {"deci:ak74", "foregrip"}, {"deci:ump45", "flashlight"}, {"deci:glock17", "laser"}};
        int k = (ticks - 5) / 40, t = (ticks - 5) % 40;
        if (ticks >= 5 && k < shots.length)
        {
            final String[] sh = shots[k];
            if (t == 0)
            {
                onServer = () -> {
                    EntityPlayerMP p = player();
                    ItemStack gun = new ItemStack((Item) Item.itemRegistry.getObject(sh[0]));
                    gun.stackTagCompound = new NBTTagCompound();
                    gun.stackTagCompound.setInteger("ammo", 10);
                    gun.stackTagCompound.setString("gripAttach", sh[1]);
                    p.inventory.setInventorySlotContents(0, gun);
                    p.inventory.currentItem = 0;
                };
                mc.thePlayer.inventory.currentItem = 0;
                net.decimation.worldgen.DevAutoTest.keepScreen = true;
            }
            if (t == 8) GunsmithTest.pressAttach(mc);
            if (t == 34) DevTestUtil.screenshot(mc, name(), "grip_" + sh[1] + "_" + sh[0].substring(5) + ".png");
            if (t == 36) mc.thePlayer.closeScreen();
            return true;
        }
        if (ticks >= 5)
        {
            net.decimation.worldgen.DevAutoTest.keepScreen = false;
            return false;
        }
        return true;
    }

    /** The three guns of the SpecterDR step (slots 0 .. 2): two with the SpecterDR, one with the ACOG. */
    private static final String[] SPECTER_SIGHTS = {"specterdr", "specterdr", "ta11acog"};
    private String runId;
    private int clicks0;

    /**
     * The SpecterDR's switch (spec section 2): 4x by default (magnification 4, the gun and its glass kept, no black
     * overlay); the key (SpecterKey, default G) flips the held gun's SpecterDR to 1x (1.25) and back; another gun keeps
     * its own mode; with the ACOG the key changes nothing and plays no click; aimed shots at 1x and 4x. Each run gives
     * its guns new ids: the modes live for the session (a live game keeps them between runs).
     */
    private boolean specter(Minecraft mc)
    {
        ItemStack held = mc.thePlayer.getHeldItem();
        if (ticks == 1)
        {
            runId = Long.toString(System.nanoTime(), 36);
            onServer = () -> {
                EntityPlayerMP p = player();
                for (int i = 0; i < SPECTER_SIGHTS.length; i++)
                {
                    ItemStack gun = new ItemStack((Item) Item.itemRegistry.getObject("deci:m4a4"));
                    gun.stackTagCompound = new NBTTagCompound();
                    gun.stackTagCompound.setInteger("ammo", 30);
                    gun.stackTagCompound.setString("sightAttach", SPECTER_SIGHTS[i]);
                    gun.stackTagCompound.setString("uniqueID", "specter" + i + "_" + runId);
                    p.inventory.setInventorySlotContents(i, gun);
                }
                p.inventory.currentItem = 0;
            };
            mc.thePlayer.inventory.currentItem = 0;
        }
        if (ticks == 20)
        {
            check("specter: 4x by default", held, 4.0f, false);
            clicks0 = net.decimation.fixes.SpecterKey.clicks();
            press();
        }
        if (ticks == 24)
        {
            check("specter: the key switches to 1x", held, 1.25f, net.decimation.fixes.SpecterKey.clicks() != clicks0 + 1);
            press();
        }
        if (ticks == 28)
        {
            check("specter: the key again: 4x", held, 4.0f, net.decimation.fixes.SpecterKey.clicks() != clicks0 + 2);
            press();                                   // the first gun to 1x, then the second gun in hand
        }
        if (ticks == 32)
        {
            mc.thePlayer.inventory.currentItem = 1;
        }
        if (ticks == 36)
        {
            ItemStack first = mc.thePlayer.inventory.getStackInSlot(0);
            float other = net.decimation.fixes.ScopeZoom.magnificationFor(held);
            boolean firstFour = net.decimation.fixes.SpecterMode.isFour(first, 0);
            DevTestResults.check(name(), "specter: another gun keeps its own mode", String.format("second %.2f, first %s", other,
                                 firstFour ? "4x" : "1x"), Math.abs(other - 4f) < 0.01f && !firstFour, "second 4.00, first 1x");
            mc.thePlayer.inventory.currentItem = 2;
        }
        if (ticks == 41)
        {
            clicks0 = net.decimation.fixes.SpecterKey.clicks();
            press();
        }
        if (ticks == 45)
        {
            float acog = net.decimation.fixes.ScopeZoom.magnificationFor(held);
            int clicks = net.decimation.fixes.SpecterKey.clicks() - clicks0;
            DevTestResults.check(name(), "specter: the key with the ACOG", String.format("%.2f, %d clicks", acog, clicks),
                                 Math.abs(acog - 3.5f) < 0.01f && clicks == 0, "3.50, 0 clicks (nothing changes)");
            mc.thePlayer.inventory.currentItem = 0;    // the first gun, at 1x: aim through it
            aim(mc, 1);
        }
        if (ticks == 46)
        {
            onServer = () -> {                          // shots at noon, clear, full health (no damage tint)
                EntityPlayerMP p = player();
                if (p != null)
                {
                    p.worldObj.setWorldTime(6000);
                    p.worldObj.getWorldInfo().setRaining(false);
                    p.worldObj.getWorldInfo().setThundering(false);
                    p.setHealth(p.getMaxHealth());
                }
            };
        }
        if (ticks == 85)
        {
            double z = net.decimation.fixes.ScopeZoom.currentZoom();
            DevTestResults.check(name(), "specter: aimed at 1x", String.format("zoom %.2f", z), Math.abs(z - 1.25) < 0.05, "zoom 1.25");
            DevTestUtil.screenshot(mc, name(), "specter_1x.png");
            press();
        }
        if (ticks == 115)
        {
            double z = net.decimation.fixes.ScopeZoom.currentZoom();
            boolean overlay = net.decimation.fixes.ScopeZoom.overlayShown();
            DevTestResults.check(name(), "specter: aimed at 4x, gun and glass kept", String.format("zoom %.2f, overlay %s", z, overlay),
                                 Math.abs(z - 4.0) < 0.1 && !overlay, "zoom 4.00, overlay false");
            DevTestUtil.screenshot(mc, name(), "specter_4x.png");
        }
        if (ticks == 116)
        {
            aim(mc, 0);
        }
        return ticks < 120;                            // the server takes the aim off before the mode ends
    }

    /** The held gun's magnification and overlay; `clickWrong` adds a failure when the click count is not as expected. */
    private void check(String key, ItemStack held, float mag, boolean clickWrong)
    {
        float m = net.decimation.fixes.ScopeZoom.magnificationFor(held);
        boolean overlay = net.decimation.fixes.ScopeZoom.overlays(held);
        DevTestResults.check(name(), key, String.format("%.2f, overlay %s%s", m, overlay, clickWrong ? ", click count wrong" : ""),
                             Math.abs(m - mag) < 0.01f && !overlay && !clickWrong, String.format("%.2f, overlay false, one click a press", mag));
    }

    /** A press of the SpecterDR key as the keyboard makes it (KeyBinding state down, a tick, up). */
    private static void press()
    {
        net.minecraft.client.settings.KeyBinding k = net.decimation.fixes.SpecterKey.binding();
        if (k != null)
        {
            net.minecraft.client.settings.KeyBinding.setKeyBindState(k.getKeyCode(), true);
            net.minecraft.client.settings.KeyBinding.onTick(k.getKeyCode());
            net.minecraft.client.settings.KeyBinding.setKeyBindState(k.getKeyCode(), false);
        }
    }

    /** Aim mode on both sides (Decimation's player data). */
    private void aim(Minecraft mc, int mode)
    {
        Deci.setAimMode(mc.thePlayer, mode);
        onServer = () -> {
            EntityPlayerMP p = player();
            if (p != null)
            {
                Deci.setAimMode(p, mode);
            }
        };
    }

    private void ratio(String key, double[] s, double up0, double side0, double up, double side)
    {
        double u = s[0] / Math.max(1e-9, s[1]) / up0, w = s[2] / Math.max(1e-9, s[3]) / side0;
        DevTestResults.check(name(), key, String.format("up %.3f, sideways %.3f of no grip (no grip: %.3f, %.3f)", u, w, up0, side0),
                             Math.abs(u - up) <= 0.02 * Math.max(1, up / 0.5) && Math.abs(w - side) <= 0.02 * Math.max(1, side / 0.5),
                             String.format("up %.3f, sideways %.3f (within 2 %%)", up, side));
    }

    private static EntityPlayerMP player()
    {
        MinecraftServer s = MinecraftServer.getServer();
        return s == null || s.getConfigurationManager().playerEntityList.isEmpty() ? null
            : (EntityPlayerMP) s.getConfigurationManager().playerEntityList.get(0);
    }

    private void items()
    {
        name("RK-1 registered", "deci:rk1", "RK-1 Foregrip");
        name("SpecterDR registered", "deci:specterdr", "ELCAN SpecterDR 1x/4x");
        name("EOTech named", "deci:eotech558", "EOTech 558");
        name("ACOG named", "deci:ta11acog", "ACOG TA11 3.5x");
        ItemStack m4 = stack("deci:m4a4"), glock = stack("deci:glock17"), rk1 = stack("deci:rk1"), specter = stack("deci:specterdr");
        boolean rifle = rk1 != null && GunParts.fits(GunSlot.GRIP, m4, rk1), pistol = rk1 != null && GunParts.fits(GunSlot.GRIP, glock, rk1);
        DevTestResults.check(name(), "items: RK-1 fits a rifle, not a pistol", "m4a4 " + rifle + ", glock17 " + pistol, rifle && !pistol,
                             "m4a4 true, glock17 false (category rifle, as the Foregrip)");
        boolean sm4 = specter != null && GunParts.fits(GunSlot.SIGHT, m4, specter);
        boolean sglock = specter != null && GunParts.fits(GunSlot.SIGHT, glock, specter);
        DevTestResults.check(name(), "items: SpecterDR fits a rifle and a pistol", "m4a4 " + sm4 + ", glock17 " + sglock, sm4 && sglock,
                             "both true (category all, as the ACOG)");
        loot("loot: the SpecterDR wherever the 4x is", "deci:4x", "deci:specterdr");
        loot("loot: the RK-1 wherever the Foregrip is", "deci:foregrip", "deci:rk1");
    }

    private void name(String key, String item, String want)
    {
        ItemStack s = stack(item);
        String got = s == null ? "not registered" : s.getDisplayName();
        DevTestResults.check(name(), "items: " + key, got, want.equals(got), want);
    }

    /** Decimation's crate lists holding `like` against those also holding `added` (Deci.lootTable). */
    private void loot(String key, String like, String added)
    {
        Item a = (Item) Item.itemRegistry.getObject(like), b = (Item) Item.itemRegistry.getObject(added);
        int withA = 0, withBoth = 0;
        for (Map.Entry<Integer, List<ItemStack>> e : net.decimation.fixes.Deci.lootTable().values())
        {
            boolean hasA = false, hasB = false;
            for (ItemStack s : e.getValue())
            {
                hasA |= s != null && s.getItem() == a;
                hasB |= s != null && b != null && s.getItem() == b;
            }
            withA += hasA ? 1 : 0;
            withBoth += hasA && hasB ? 1 : 0;
        }
        DevTestResults.check(name(), key, withBoth + " of " + withA + " lists", withA > 0 && withBoth == withA, "every one of them");
    }

    private static ItemStack stack(String name)
    {
        Object i = Item.itemRegistry.getObject(name);
        return i == null ? null : new ItemStack((Item) i);
    }
}

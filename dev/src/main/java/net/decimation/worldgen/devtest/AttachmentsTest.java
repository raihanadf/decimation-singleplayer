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
 * lie in Decimation's loot wherever the Foregrip / the 4x lie. Later steps of the plan add recoil, placement and the
 * SpecterDR's switch to this mode.
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
                return recoil(mc);
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

package net.decimation.worldgen.devtest;

import cpw.mods.fml.common.registry.GameRegistry;
import net.decimation.fixes.Deci;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldSettings;

/**
 * Decimation's medical items and drinks used the vanilla way (docs/decimation_code/items.md): the item held, item use
 * started (as a right click does), the use time run out, so EntityPlayer finishes it and calls the item's onEaten on
 * the server. Bandage twice on bleeding, splint on a broken leg, bottled water on thirst. Records what each bandage
 * hands back (BandageItem keeps that stack in a static field shared by every bandage).
 */
public class UseItemsTest extends DevTestMode
{
    public String name() { return "useitems"; }

    private volatile int ticks, phase;
    private int startAt, before;
    private WorldSettings.GameType modeBefore;
    private String invBefore;

    public boolean client(Minecraft mc)
    {
        return phase < 9;
    }

    public void server()
    {
        int t = ++ticks;
        EntityPlayerMP p = DevTestUtil.player();
        if (p == null)
        {
            return;
        }
        Deci.Player d = Deci.player(p);
        if (t == 2)
        {
            DevTestArena.build(p.worldObj, p);
            modeBefore = p.theItemInWorldManager.getGameType();
            p.setGameType(WorldSettings.GameType.SURVIVAL);
            p.capabilities.disableDamage = true;
            p.inventory.currentItem = 0;
            for (int i = 0; i < 36; i++)
            {
                p.inventory.setInventorySlotContents(i, null);
            }
            phase = 1;
            return;
        }
        switch (phase)
        {
            case 1: // bandage, first use
            case 3: // bandage, second use
                if (startAt == 0)
                {
                    d.setBleeding(true);
                    if (phase == 1)
                    {
                        p.inventory.setInventorySlotContents(0, new ItemStack(item("ItemBandage"), 2));
                    }
                    before = count(p, item("ItemBandage"));
                    invBefore = others(p, item("ItemBandage"));
                    use(p, t);
                }
                else if (t - startAt > 45)
                {
                    String n = phase == 1 ? "first" : "second";
                    DevTestResults.check(name(), "bandage (" + n + ") stops bleeding", "bleeding=" + d.bleeding(), !d.bleeding(), "false");
                    DevTestResults.check(name(), "bandage (" + n + ") used up", before + " -> " + count(p, item("ItemBandage")),
                                         count(p, item("ItemBandage")) == before - 1, "-1");
                    DevTestResults.value(name(), "bandage (" + n + ") hands back", invBefore + " -> " + others(p, item("ItemBandage")));
                    next();
                }
                break;
            case 5: // splint on a broken leg
                if (startAt == 0)
                {
                    d.setBrokenLeg(true);
                    p.inventory.setInventorySlotContents(0, new ItemStack(item("ItemSplint"), 1));
                    use(p, t);
                }
                else if (t - startAt > 45)
                {
                    DevTestResults.check(name(), "splint mends a broken leg", "brokenLeg=" + d.brokenLeg(), !d.brokenLeg(), "false");
                    next();
                }
                break;
            case 7: // bottled water when thirsty
                if (startAt == 0)
                {
                    d.setWater(5);
                    p.inventory.setInventorySlotContents(0, new ItemStack(item("ItemBottledWater"), 1));
                    before = count(p, Items.glass_bottle);
                    use(p, t);
                }
                else if (t - startAt > 45)
                {
                    DevTestResults.check(name(), "bottled water: water +6", "5 -> " + d.water(), d.water() == 11, "11");
                    DevTestResults.check(name(), "bottled water: empty bottle back", before + " -> " + count(p, Items.glass_bottle),
                                         count(p, Items.glass_bottle) == before + 1, "+1");
                    if (modeBefore != null)
                    {
                        p.setGameType(modeBefore);
                    }
                    phase = 9;
                }
                break;
            default:
                if (phase % 2 == 0 && t - startAt > 50) // a pause between items
                {
                    phase++;
                    startAt = 0;
                }
                break;
        }
    }

    private void next()
    {
        phase++;
        startAt = ticks;
    }

    /** What a right click starts: item use of the held stack for its full duration (finished by EntityPlayer.onUpdate). */
    private void use(EntityPlayerMP p, int t)
    {
        ItemStack held = p.inventory.getCurrentItem();
        p.setItemInUse(held, held.getMaxItemUseDuration());
        startAt = t;
    }

    private static Item item(String name)
    {
        return GameRegistry.findItem("deci", name);
    }

    private static int count(EntityPlayerMP p, Item item)
    {
        int n = 0;
        for (ItemStack s : p.inventory.mainInventory)
        {
            n += s != null && s.getItem() == item ? s.stackSize : 0;
        }
        return n;
    }

    /** Every stack but `skip` in the inventory, "name xN, ..." ("nothing" when empty). */
    private static String others(EntityPlayerMP p, Item skip)
    {
        StringBuilder b = new StringBuilder();
        for (ItemStack s : p.inventory.mainInventory)
        {
            if (s != null && s.getItem() != skip)
            {
                b.append(b.length() == 0 ? "" : ", ").append(GameRegistry.findUniqueIdentifierFor(s.getItem())).append(" x").append(s.stackSize);
            }
        }
        return b.length() == 0 ? "nothing" : b.toString();
    }
}

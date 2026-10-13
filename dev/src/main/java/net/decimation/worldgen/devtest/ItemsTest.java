package net.decimation.worldgen.devtest;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.decimation.fixes.Deci;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/**
 * Dumps every item of Decimation and ours to run/client/devtest/items.tsv (docs/references/
 * decimation_items.tsv is made from it): registry name, display name, runtime class, max stack, armor
 * slot, Decimation's damage multiplier and loot chance, and the crate loot lists holding the item
 * (block names). The source for our loot lists (docs/loot_levels.md) and the protection table.
 */
public class ItemsTest extends DevTestMode
{
    public String name() { return "items"; }

    private volatile boolean done;

    public boolean client(Minecraft mc)
    {
        return !done;
    }

    public void server()
    {
        if (done)
        {
            return;
        }
        Map<Item, List<String>> lists = new LinkedHashMap<Item, List<String>>();
        for (Map.Entry<String, Map.Entry<Integer, List<ItemStack>>> e : Deci.lootTable().entrySet())
        {
            for (ItemStack s : e.getValue().getValue())
            {
                if (s == null)
                {
                    continue;
                }
                List<String> l = lists.get(s.getItem());
                if (l == null)
                {
                    lists.put(s.getItem(), l = new ArrayList<String>());
                }
                String b = e.getKey().replace("deci:", "") + "x" + s.stackSize;
                if (!l.contains(b))
                {
                    l.add(b);
                }
            }
        }
        Map<String, String> rows = new TreeMap<String, String>();
        for (Object o : Item.itemRegistry.getKeys())
        {
            String key = (String) o;
            if (!key.startsWith("deci:") && !key.startsWith("deciworldgen:"))
            {
                continue;
            }
            Item item = (Item) Item.itemRegistry.getObject(key);
            String display;
            try
            {
                display = new ItemStack(item).getDisplayName();
            }
            catch (Exception e)
            {
                display = "?";
            }
            String armor = item instanceof ItemArmor ? String.valueOf(((ItemArmor) item).armorType) : "";
            String mult = "", chance = "";
            if (item instanceof net.decimation.mod.common.item.armor.ItemArmorDeci)
            {
                net.decimation.mod.common.item.armor.ItemArmorDeci a = (net.decimation.mod.common.item.armor.ItemArmorDeci) item;
                mult = String.valueOf(a.getDamageMultiplier());
                chance = String.valueOf(a.getLootChance());
            }
            else
            {
                try
                {
                    chance = String.valueOf(item.getClass().getMethod("getLootChance").invoke(item));
                }
                catch (Exception e)
                {
                    chance = "";
                }
            }
            List<String> l = lists.get(item);
            rows.put(key, key + "\t" + display + "\t" + item.getClass().getName() + "\t" + item.getItemStackLimit() + "\t" + armor
                + "\t" + mult + "\t" + chance + "\t" + (l == null ? "" : String.join(",", l)));
        }
        File out = new File(Minecraft.getMinecraft().mcDataDir, "devtest/items.tsv");
        try
        {
            PrintWriter w = new PrintWriter(out, "UTF-8");
            w.println("name\tdisplay\tclass\tmax_stack\tarmor_slot\tdamage_multiplier\tloot_chance\tloot_lists");
            for (String r : rows.values())
            {
                w.println(r);
            }
            w.close();
        }
        catch (Exception e)
        {
            DevTestResults.value(name(), "write", e.toString());
        }
        DevTestResults.value(name(), "items", rows.size() + " in " + out);
        DevTestResults.value(name(), "loot lists", Deci.lootTable().size() + " blocks");
        done = true;
    }
}

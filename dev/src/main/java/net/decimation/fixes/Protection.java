package net.decimation.fixes;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * What each piece of armor protects against (docs/systems_plan.md 2.6, 5.1), in
 * config/deciworldgen_protection.json: per item "zombie" (share of an infected hit blocked), "bite"
 * (share taken off the bite infection chance), "radiation" (share of radiation blocked; a gas mask
 * alone is 1, as in Decimation), and "gunfire" (damage multiplier against NPC shots; absent = the
 * item's own Decimation value, ArmorGunfireHandler). Worn pieces add up, capped at ZOMBIE_CAP,
 * BITE_CAP and 1.
 *
 * Built-in values by armor family `[draft numbers, user review]`: clothes 0, light (bandit, militia,
 * police clothes) low, military sets and vests medium, plate carriers and juggernaut high, hazmat
 * radiation. Not used by any damage code yet: the zombie step (systems_plan 5.1, 5.2) reads it.
 * The file is written when missing or older than VERSION, then it is the user's.
 */
public class Protection
{
    public static final int VERSION = 1;
    public static final double ZOMBIE_CAP = 0.8, BITE_CAP = 0.75;
    private static final String FILE = "deciworldgen_protection.json";

    public static class Values
    {
        public double zombie, bite, radiation;
        public Float gunfire;

        Values(double zombie, double bite, double radiation)
        {
            this.zombie = zombie;
            this.bite = bite;
            this.radiation = radiation;
        }
    }

    static class FileData
    {
        int version;
        Map<String, Values> items = new TreeMap<String, Values>();
    }

    private static final Map<Item, Values> table = new java.util.HashMap<Item, Values>();

    public static synchronized void load(File configDir)
    {
        File f = new File(configDir, FILE);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileData data = null;
        if (f.isFile())
        {
            try
            {
                Reader in = new FileReader(f);
                data = gson.fromJson(in, FileData.class);
                in.close();
            }
            catch (Exception e)
            {
                FMLLog.info("[deciworldgen] %s unreadable, built-in protection used: %s", f, e);
            }
        }
        if (data == null || data.version < VERSION || data.items == null)
        {
            if (f.isFile())
            {
                File old = new File(configDir, FILE + ".old");
                old.delete();
                f.renameTo(old);
            }
            data = defaults();
            try
            {
                Writer out = new FileWriter(f);
                out.write(gson.toJson(data));
                out.close();
            }
            catch (Exception e)
            {
                FMLLog.info("[deciworldgen] could not write %s: %s", f, e);
            }
        }
        table.clear();
        int unknown = 0;
        for (Map.Entry<String, Values> e : data.items.entrySet())
        {
            Item item = (Item) Item.itemRegistry.getObject(e.getKey());
            if (item == null)
            {
                unknown++;
                FMLLog.info("[deciworldgen] protection entry skipped (unknown item): %s", e.getKey());
                continue;
            }
            table.put(item, e.getValue());
        }
        FMLLog.info("[deciworldgen] protection table loaded: %d items, %d skipped", table.size(), unknown);
    }

    /** Values of one item; null for anything not in the table (no protection). */
    public static synchronized Values of(Item item)
    {
        return table.get(item);
    }

    public static synchronized int size()
    {
        return table.size();
    }

    /** Everything an entity wears added up and capped: armor slots, and a player's vest, mask, backpack. */
    public static Values worn(EntityLivingBase e)
    {
        Values sum = new Values(0, 0, 0);
        for (int i = 1; i <= 4; i++)
        {
            addTo(sum, e.getEquipmentInSlot(i));
        }
        if (e instanceof EntityPlayer)
        {
            for (ItemStack s : Deci.playerGear((EntityPlayer) e))
            {
                addTo(sum, s);
            }
        }
        sum.zombie = Math.min(ZOMBIE_CAP, sum.zombie);
        sum.bite = Math.min(BITE_CAP, sum.bite);
        sum.radiation = Math.min(1, sum.radiation);
        return sum;
    }

    /** The same sum for a set of stacks (dev tests, NPC loadouts). */
    public static Values worn(ItemStack... stacks)
    {
        Values sum = new Values(0, 0, 0);
        for (ItemStack s : stacks)
        {
            addTo(sum, s);
        }
        sum.zombie = Math.min(ZOMBIE_CAP, sum.zombie);
        sum.bite = Math.min(BITE_CAP, sum.bite);
        sum.radiation = Math.min(1, sum.radiation);
        return sum;
    }

    private static void addTo(Values sum, ItemStack s)
    {
        Values v = s == null ? null : of(s.getItem());
        if (v != null)
        {
            sum.zombie += v.zombie;
            sum.bite += v.bite;
            sum.radiation += v.radiation;
        }
    }

    /** Built-in values: every wearable item of Decimation and ours, by family (name). */
    static FileData defaults()
    {
        FileData d = new FileData();
        d.version = VERSION;
        for (Object o : Item.itemRegistry.getKeys())
        {
            String key = (String) o;
            if (!key.startsWith("deci:") && !key.startsWith("deciworldgen:"))
            {
                continue;
            }
            Values v = family(key.substring(key.indexOf(':') + 1));
            if (v != null)
            {
                d.items.put(key, v);
            }
        }
        return d;
    }

    /** Values per piece by name; null for items that are not worn. */
    static Values family(String n)
    {
        if (n.startsWith("juggernaut"))
        {
            return new Values(0.20, 0.1875, 0);
        }
        if (n.startsWith("ItemPlateCarrier_"))
        {
            return new Values(0.15, 0.15, 0);
        }
        if (n.startsWith("ItemChestRig_"))
        {
            return new Values(0.04, 0.04, 0);
        }
        if (n.startsWith("hazmat"))
        {
            return new Values(0.05, 0.10, 0.25);
        }
        if (n.equals("gasmask") || n.equals("gasmaskmilitary"))
        {
            return new Values(0, 0, 1);
        }
        if (n.equals("militaryvest") || n.startsWith("militaryvest") || n.equals("bulletproofvest") || n.equals("policevest")
            || n.equals("pressvest"))
        {
            return new Values(0.10, 0.10, 0);
        }
        if (n.equals("uahdHelmet"))
        {
            return new Values(0.08, 0.10, 0);
        }
        if (n.startsWith("marine") || n.startsWith("spetsnaz") || n.startsWith("sas"))
        {
            return n.endsWith("Hat") ? new Values(0.03, 0.03, 0) : new Values(0.08, 0.10, 0);
        }
        if (n.equals("nypdHelm"))
        {
            return new Values(0.06, 0.08, 0);
        }
        if (n.equals("nypdCoat"))
        {
            return new Values(0.04, 0.06, 0);
        }
        if (n.startsWith("nypd"))
        {
            return new Values(0.02, 0.03, 0);
        }
        if (n.startsWith("bandit") || n.startsWith("militia"))
        {
            return new Values(0.05, 0.06, 0);
        }
        if (n.startsWith("ghillie"))
        {
            return new Values(0.02, 0.02, 0);
        }
        if (n.startsWith("casual") || n.startsWith("hoodie") || n.startsWith("cap") || n.equals("santahat") || n.equals("balaclava")
            || n.startsWith("nvgoggles"))
        {
            return new Values(0, 0, 0);
        }
        return null;
    }
}

package net.decimation.fixes;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;

/**
 * The ELCAN SpecterDR's two magnifications (docs/superpowers/specs/2026-10-21-new-attachments-design.md; user: "1x/4x
 * switch"): 4x until the player flips it (SpecterKey, default G), then 1x, per gun, for the session. A gun is known by
 * the "uniqueID" Decimation's OwnedItem tags it with in a player's inventory, else by its inventory slot. Plain Java;
 * ScopeZoom asks it on the client.
 */
public final class SpecterMode
{
    /** Guns switched to 1x (keys from key()); every other gun's SpecterDR is at 4x. */
    private static final Set<String> ONE = new HashSet<String>();

    private SpecterMode()
    {
    }

    /** True while this gun's SpecterDR is at 4x (the default). */
    public static boolean isFour(ItemStack gun, int slot)
    {
        return isFourById(id(gun), slot);
    }

    /** Flips this gun's SpecterDR between 4x and 1x; the new state (true = 4x). */
    public static boolean toggle(ItemStack gun, int slot)
    {
        return toggleById(id(gun), slot);
    }

    static boolean isFourById(String id, int slot)
    {
        return !ONE.contains(key(id, slot));
    }

    static boolean toggleById(String id, int slot)
    {
        String k = key(id, slot);
        if (ONE.remove(k))
        {
            return true;
        }
        ONE.add(k);
        return false;
    }

    static void reset()
    {
        ONE.clear();
    }

    private static String key(String id, int slot)
    {
        return id != null && !id.isEmpty() ? "id " + id : "slot " + slot;
    }

    private static String id(ItemStack gun)
    {
        return gun != null && gun.stackTagCompound != null ? gun.stackTagCompound.getString("uniqueID") : null;
    }
}

package net.decimation.fixes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryBasic;

/**
 * Opens Decimation's loot screen on an inventory of ours (docs/loot_levels.md, docs/systems_plan.md
 * 2.2): the same steps as Decimation's crate handler (LootInteractHandler): the inventory goes into
 * the player's data and to the client first, the screen opens 2 ticks later so the packet is there
 * before it. Server side only; register on the FML bus.
 */
public class Loot
{
    private static final int DELAY = 2;
    private static final List<Object[]> pending = new ArrayList<Object[]>();

    /** Shows `inv` in the loot area; stacks taken there leave `inv` itself. */
    public static void open(EntityPlayerMP player, InventoryBasic inv)
    {
        Deci.setLootInventory(player, inv);
        synchronized (pending)
        {
            pending.add(new Object[] {player, inv, DELAY});
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent e)
    {
        if (e.phase != TickEvent.Phase.END)
        {
            return;
        }
        synchronized (pending)
        {
            for (Iterator<Object[]> it = pending.iterator(); it.hasNext();)
            {
                Object[] p = it.next();
                int left = (Integer) p[2] - 1;
                p[2] = left;
                if (left > 0)
                {
                    continue;
                }
                it.remove();
                EntityPlayerMP player = (EntityPlayerMP) p[0];
                // another loot opened meanwhile: that one wins
                if (!player.isDead && Deci.lootInventory(player) == p[1])
                {
                    Deci.openLootGui(player);
                }
            }
        }
    }
}

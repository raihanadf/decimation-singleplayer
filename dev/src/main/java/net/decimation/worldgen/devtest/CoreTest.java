package net.decimation.worldgen.devtest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import net.decimation.fixes.Deci;
import net.decimation.fixes.LootPools;
import net.decimation.fixes.Protection;
import net.decimation.fixes.WorldClock;
import net.decimation.worldgen.Areas;
import net.decimation.worldgen.ZoneKind;
import net.decimation.worldgen.ZoneStore;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Step 1 of docs/systems_plan.md section 7, the shared core, and the loot off by one fix
 * (tools/patches/PatchLootRolls.java):
 * - Decimation's own crates: items per open reach n (military crate 1 and 2, chest up to 3, supply drop
 *   up to 8), listed stacks reach their size and never pass it;
 * - our loot lists (LootPools): no unknown names, 16 lists, every roll inside its range, level 4 holds
 *   an item of every type;
 * - WorldClock: events in time order for jumps, and the live tick handler at dusk;
 * - Protection: values per family and their caps;
 * - Areas: a radiation area puts a Decimation zone in place, survives a save and reload, goes again.
 */
public class CoreTest extends DevTestMode
{
    public String name() { return "core"; }

    private static final int ROLLS = 2000;
    private volatile boolean done;
    private int ticks, liveAt = -1;
    private final List<String> seen = new ArrayList<String>();
    private final WorldClock.Listener listener = new WorldClock.Listener()
    {
        public void onClock(World world, WorldClock.Event event, int day)
        {
            synchronized (seen)
            {
                seen.add(event + "@" + day);
            }
        }
    };

    public boolean client(Minecraft mc)
    {
        return !done;
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
        if (t == 20)
        {
            decimationCrates();
            pools();
            protection();
            areas(w, p);
            clockJumps(w);
            // live: from just before dusk, the tick handler must fire DUSK once
            WorldClock.listen(listener);
            synchronized (seen)
            {
                seen.clear();
            }
            w.setWorldTime(w.getWorldTime() / 24000L * 24000L + 11990);
            liveAt = t;
        }
        if (liveAt > 0 && t == liveAt + 40)
        {
            List<String> got;
            synchronized (seen)
            {
                got = new ArrayList<String>(seen);
            }
            WorldClock.unlisten(listener);
            int day = WorldClock.day(w);
            DevTestResults.check(name(), "clock: live dusk", got, got.equals(java.util.Arrays.asList("DUSK@" + day)),
                                 "[DUSK@" + day + "]");
            w.setWorldTime(w.getWorldTime() / 24000L * 24000L + 6000);
            done = true;
        }
    }

    // ---- Decimation's crates after the patch

    private void decimationCrates()
    {
        Map<String, Map.Entry<Integer, List<ItemStack>>> table = Deci.lootTable();
        String[][] cases = {{"deci:BlockMilitaryCrate", "1", "2"}, {"minecraft:chest", "1", "3"}, {"deci:BlockSupplyDrop", "1", "8"}};
        Random r = new Random(1);
        for (String[] c : cases)
        {
            Block b = Block.getBlockFromName(c[0]);
            Map.Entry<Integer, List<ItemStack>> listed = table.get(c[0]);
            if (b == null || listed == null)
            {
                DevTestResults.check(name(), "decimation " + c[0], "no pool", false, "a loot pool");
                continue;
            }
            Map<Item, Integer> size = new HashMap<Item, Integer>();
            for (ItemStack s : listed.getValue())
            {
                if (s != null)
                {
                    size.put(s.getItem(), Math.max(s.stackSize, size.containsKey(s.getItem()) ? size.get(s.getItem()) : 0));
                }
            }
            TreeMap<Integer, Integer> perOpen = new TreeMap<Integer, Integer>();
            int over = 0, reachedFull = 0, multi = 0;
            Set<Item> full = new HashSet<Item>(), multiSeen = new HashSet<Item>();
            for (int i = 0; i < ROLLS; i++)
            {
                InventoryBasic inv = new InventoryBasic("t", true, 18);
                Deci.rollDecimationLoot(b, inv);
                int n = 0;
                for (int k = 0; k < 18; k++)
                {
                    ItemStack s = inv.getStackInSlot(k);
                    if (s == null)
                    {
                        continue;
                    }
                    n++;
                    Integer max = size.get(s.getItem());
                    if (max != null && s.stackSize > max)
                    {
                        over++;
                    }
                    if (max != null && max >= 2)
                    {
                        multiSeen.add(s.getItem());
                        if (s.stackSize == max)
                        {
                            full.add(s.getItem());
                        }
                    }
                }
                perOpen.put(n, perOpen.containsKey(n) ? perOpen.get(n) + 1 : 1);
            }
            int lo = Integer.parseInt(c[1]), hi = Integer.parseInt(c[2]);
            boolean range = perOpen.firstKey() >= lo && perOpen.lastKey() <= hi && perOpen.containsKey(hi);
            DevTestResults.check(name(), "decimation " + c[0] + " items per open", perOpen, range,
                                 lo + " .. " + hi + ", " + hi + " seen (n = " + listed.getKey() + ")");
            DevTestResults.check(name(), "decimation " + c[0] + " stacks", "over " + over + ", full size reached by " + full.size()
                + " of " + multiSeen.size() + " multi stack items seen", over == 0 && (multiSeen.isEmpty() || !full.isEmpty()),
                                 "never above the listed size, the listed size reached");
        }
    }

    // ---- our lists

    private void pools()
    {
        DevTestResults.check(name(), "pools: unknown names", LootPools.problems, LootPools.problems.isEmpty(), "none");
        DevTestResults.check(name(), "pools: lists", LootPools.names().size(), LootPools.names().size() == 16,
                             "16 (4 kinds x 4 levels)");
        Random r = new Random(2);
        for (String name : LootPools.names())
        {
            LootPools.Pool pool = LootPools.pool(name);
            TreeMap<Integer, Integer> perOpen = new TreeMap<Integer, Integer>();
            int bad = 0, typed = 0;
            for (int i = 0; i < 500; i++)
            {
                List<ItemStack> roll = LootPools.roll(name, r);
                perOpen.put(roll.size(), perOpen.containsKey(roll.size()) ? perOpen.get(roll.size()) + 1 : 1);
                for (ItemStack s : roll)
                {
                    bad += s.stackSize < 1 || s.stackSize > s.getMaxStackSize() ? 1 : 0;
                }
                if (pool.first != null && hasEveryType(pool, roll))
                {
                    typed++;
                }
            }
            boolean ok = perOpen.firstKey() >= pool.rolls[0] && perOpen.lastKey() <= pool.rolls[1] && bad == 0
                && (pool.first == null || typed == 500);
            DevTestResults.check(name(), "pools: " + name, perOpen + (pool.first == null ? "" : " every type " + typed + "/500")
                + " e.g. " + names(LootPools.roll(name, r)), ok, pool.rolls[0] + " .. " + pool.rolls[1] + " items, stacks valid"
                + (pool.first == null ? "" : ", one of each type"));
        }
    }

    /** Every "first" list has an item of its own in the roll. */
    private static boolean hasEveryType(LootPools.Pool pool, List<ItemStack> roll)
    {
        for (String f : pool.first)
        {
            Set<Item> own = new HashSet<Item>();
            for (LootPools.Entry e : LootPools.pool(f).items)
            {
                if (!e.item.startsWith("@"))
                {
                    own.add((Item) Item.itemRegistry.getObject(e.item));
                }
            }
            boolean any = false;
            for (ItemStack s : roll)
            {
                any |= own.contains(s.getItem());
            }
            if (!any)
            {
                return false;
            }
        }
        return true;
    }

    private static String names(List<ItemStack> roll)
    {
        StringBuilder b = new StringBuilder();
        for (ItemStack s : roll)
        {
            String n = Item.itemRegistry.getNameForObject(s.getItem());
            b.append(b.length() == 0 ? "" : ", ").append(n.substring(n.indexOf(':') + 1)).append(" x").append(s.stackSize);
        }
        return b.toString();
    }

    // ---- protection

    private void protection()
    {
        DevTestResults.check(name(), "protection: items", Protection.size(), Protection.size() > 100, "over 100 wearables");
        Protection.Values jug = Protection.worn(stacks("deci:juggernautHelm", "deci:juggernautVest", "deci:juggernautPants",
                                                       "deci:juggernautBoots"));
        DevTestResults.check(name(), "protection: full juggernaut", jug.zombie + " / " + jug.bite, jug.zombie == Protection.ZOMBIE_CAP
            && Math.abs(jug.bite - 0.75) < 1e-9, "zombie 0.8 (cap), bite 0.75 (8 % to 2 %)");
        Protection.Values haz = Protection.worn(stacks("deci:hazmatHelm", "deci:hazmatVest", "deci:hazmatPants", "deci:hazmatBoots"));
        Protection.Values mask = Protection.worn(stacks("deci:gasmask"));
        DevTestResults.check(name(), "protection: radiation", "hazmat " + haz.radiation + ", gas mask " + mask.radiation,
                             Math.abs(haz.radiation - 1) < 1e-9 && mask.radiation == 1, "1 and 1");
        Protection.Values clothes = Protection.worn(stacks("deci:hoodie3", "deci:casual1Pants", "deci:casual1Boots"));
        Protection.Values mil = Protection.worn(stacks("deci:marineHelm", "deci:marineVest", "deci:marinePants", "deci:marineBoots",
                                                       "deci:militaryvest"));
        DevTestResults.check(name(), "protection: clothes < military", clothes.zombie + " < " + mil.zombie,
                             clothes.zombie == 0 && mil.zombie > 0.3 && mil.zombie < jug.zombie, "0 < 0.42 < 0.8");
    }

    private static ItemStack[] stacks(String... names)
    {
        List<ItemStack> out = new ArrayList<ItemStack>();
        for (String n : names)
        {
            Item i = (Item) Item.itemRegistry.getObject(n);
            if (i != null)
            {
                out.add(new ItemStack(i));
            }
        }
        return out.toArray(new ItemStack[0]);
    }

    // ---- areas

    private void areas(World w, EntityPlayerMP p)
    {
        int x = (int) p.posX + 1000, y = 70, z = (int) p.posZ + 1000;
        Areas.Area a = Areas.add("coretest_rad", Areas.Kind.RADIATION, "Test leak", x - 5, 0, z - 5, x + 5, 255, z + 5);
        boolean zone = ZoneStore.has(ZoneKind.RADIATION, x, y, z);
        DevTestResults.check(name(), "areas: radiation area adds a Decimation zone", a != null && zone, a != null && zone,
                             "area and RADIATION zone at its middle");
        Areas.setState("coretest_rad", "claimed", "tester");
        Areas.save();
        int n = Areas.reload();
        Areas.Area back = Areas.get("coretest_rad");
        boolean kept = back != null && "claimed".equals(back.state) && "tester".equals(back.owner) && back.contains(x, y, z)
            && Areas.at(Areas.Kind.RADIATION, x, y, z).size() == 1;
        DevTestResults.check(name(), "areas: saved and loaded", n + " area(s), state " + (back == null ? "-" : back.state), kept,
                             "state claimed, owner tester, same box");
        boolean removed = Areas.remove("coretest_rad") && !ZoneStore.has(ZoneKind.RADIATION, x, y, z);
        Areas.save();
        DevTestResults.check(name(), "areas: removed with its zone", removed, removed, "no area, no zone");
    }

    // ---- clock

    private void clockJumps(World w)
    {
        WorldClock.listen(listener);
        long[][] cases = {{11000, 14000}, {23000, 25000}, {20000, 48500}, {30000, 29000}};
        String[] want = {"[DUSK@1, NIGHT@1]", "[DAY@2]", "[DUSK@2, NIGHT@2, DAWN@2, DAY@3]", "[]"};
        for (int i = 0; i < cases.length; i++)
        {
            synchronized (seen)
            {
                seen.clear();
            }
            WorldClock.fire(w, cases[i][0], cases[i][1]);
            String got;
            synchronized (seen)
            {
                got = seen.toString();
            }
            DevTestResults.check(name(), "clock: " + cases[i][0] + " -> " + cases[i][1], got, got.equals(want[i]), want[i]);
        }
        WorldClock.unlisten(listener);
    }
}

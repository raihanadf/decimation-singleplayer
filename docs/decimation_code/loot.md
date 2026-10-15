# Decimation code: loot (crates, cooldowns, supply drops)

Part of the feature study of Decimation's own code (16 Oktober 2026, index: docs/decimation_code/README.md). Names
are the readable ones of `deobf/src` (obfuscated name in brackets); our code reaches every one of them through
`net.decimation.fixes.Deci`. Design of our crate levels and bodies: docs/loot_levels.md.

## 1. What it is for

Decimation has no chests to fill: almost every container and car wreck in the world is a loot block. A right click
rolls a fresh handful of items from that block's list into Decimation's inventory screen, then the spot is empty for
15 minutes. Supply drops are timed crates that fall from the sky with more rolls and vanish once looted.

## 2. Code map

| Class (obf) | Job |
|---|---|
| LootInteractHandler (deci/aK/k) | `onPlayerInteract`: the whole right click flow (section 3) |
| LootTableRegistry (deci/aD/l) | hardcoded table: `buildLootTables` (gh) makes 12 item lists and binds 33 blocks to them with a roll count and a sound; `getPool` (c), `hasPool` (d); vanilla chests use list 1 with 3 rolls, the supply drop 8 rolls of list 11 |
| LootPool (deci/aB/c) | `fillInventory` (a): rolls entries by weight into random free slots of an 18 slot inventory; `getSound` (ga) |
| LootEntry (deci/aB/d) | one ItemStack plus a stack size range; `roll` (c) |
| IntRange (deci/aB/a) | roll counts and stack sizes; `pick` (b). Was [1, n): PatchLootRolls makes it 1 .. n |
| WeightedRandom (deci/aB/f) | weight = the item's getLootChance (OwnedItem, LootChanceItem, ItemArmorDeci), else 0.9 |
| LootCooldownRegistry (deci/aB/e) | per dimension map packed position -> last open in ms; `forWorld` (d), `isReady` (k), `markLooted` (l), `clear`. In memory only: a restart makes every crate full again |
| LootCloseCallback (deci/aB/b) | blocks told when the player closes the loot screen (SupplyDropBlock removes itself) |
| PacketLootInventory (deci/aE/a$R) | server to client: the rolled inventory, stored in the client's PlayerData |
| PlayerData (deci/Q/b) | `setLootInventory` (a), `getLootInventory` (cN): what the loot area of the screen shows |
| PlayerInventoryScreen (deci/g/g), PlayerGearContainer (deci/f/d) | the screen: GUI id DeciConstants.GUI_LOOT, y = 2 adds the loot slots |
| TickScheduler (net/decimation/mod/common/utils/b) | delayed tasks; `server()` (gD) and `client()` (gE) queues |
| BlockTipLayer (render) | the "loot" hint over a lootable block the player looks at |
| SupplyDropSpawner (deci/aM/c), EntityFallingSupplyDrop (deci/ac/a), SupplyDropBlock (deci/V/a) | supply drops: `spawnRandomSupplyDrop` (ha) from the admin's decimation_supplydrop.properties, falling crate entity, the crate block |
| ServerTickHandler (deci/aK/q) | counts ServerConfig.supplyDropCountdown down (server only) |

## 3. The right click, as it runs (call logger trace, 16 Oktober 2026)

Dev test `loot` step 3 right clicks a placed `deci:BlockMilitaryCrate` through the client's own click
(`python3 tools/devtest.py --live loot '-Ptrace=deci.aK.k,deci.aB,deci.aD.l,deci.aE.a$R,net.decimation.mod.common.utils.b'`).

1. Server thread: `LootInteractHandler.onPlayerInteract` (right click on a block, not creative; on a dedicated
   server also `disableLootSystem` off). Vanilla workstations and vanilla chests are cancelled unless the config
   allows them; a StorageCrate in another clan's turf is refused; a multiblock part resolves to its origin.
2. `LootCooldownRegistry.forWorld` -> `isReady`. Not ready: chat "This <block> is empty..." and nothing else.
3. Ready and not in or near a safezone (a SupplyDropBlock inside one, or a BlockProp marked safezone lootable, is
   allowed): `getPool`, `markLooted`, cancel the event.
4. `LootPool.fillInventory` into a new InventoryBasic("Loot", 18): `IntRange.pick` for the roll count, a
   WeightedRandom over all entries of the list (282 adds for the military crate), per roll `LootEntry.roll` with its
   own `IntRange.pick` for the stack size.
5. The pool's sound plays (`deci:loot.metal` / `wood` / `garbage`), `PlayerData.setLootInventory`,
   `PacketLootInventory` to the player (the client stores it in its own PlayerData).
6. `TickScheduler.server().schedule(openGui(GUI_LOOT, -1, 2, 0), 2 ticks)`; a LootCloseCallback block also gets a
   task that polls until the player's container is closed.
7. Two ticks later the queue runs the task and the screen opens (checked: screen `deci.g.g`, rolled stacks shown,
   the spot not ready, a second click opens nothing).

## 4. Singleplayer state

- The handler is registered on both sides (an early one off patch of ours, no saved source; architecture.md
  deci/aK). It works in singleplayer: verified by the loot test above.
- The server task queue is drained by `ClientProxy.deciDrainLootTaskQueue` on the CLIENT tick (also that early
  patch; ServerTickHandler, its real home, is dedicated server only). Trace: TickScheduler.server().tick runs on the
  "Client thread". So the delayed openGui for the server player and SupplyDropBlock's self removal run on the
  client thread, touching the server world from another thread. It works in every test so far; a rare race is
  possible [inferred]. A clean fix would tick the server queue from a ServerTickEvent of ours instead.
- The loot hint (BlockTipLayer) asks the CLIENT world's cooldown registry, which nothing marks, so the hint stays
  on a crate that is on cooldown [inferred from the code; not looked at in game].
- Cooldowns are not saved: leaving and reloading a world refills every crate.
- Supply drop timer: ServerTickHandler is dedicated server only; our fixes/SupplyDropScheduler runs the same
  countdown and chat warnings in singleplayer and drops Decimation's falling crate 64 to 144 blocks from a player
  (dev test `checks`: "supply drop landed").

## 5. How our mod uses it

| Need | Use |
|---|---|
| roll a Decimation list into an inventory | `Deci.rollDecimationLoot(block, inv)` |
| read or extend the table | `Deci.lootTable()`, `Deci.addLootLike(item, stack)` (our guns join every list holding a similar item) |
| show our own inventory on the loot screen | `fixes/Loot.open` (Deci.setLootInventory + Deci.openLootGui), LootBody entity |
| cooldown | `Deci.lootReady(world, x, y, z)`, `Deci.clearLootCooldowns()` |
| supply drops | `Deci.supplyDropsEnabled / supplyDropCountdown / setSupplyDropCountdown / supplyDropInterval / supplyDropBlock / isFallingSupplyDrop`, fixes/SupplyDropScheduler |
| our own item lists | fixes/LootPools (config/deciworldgen_loot.json) |
| roll counts | tools/patches/PatchLootRolls.java (three jars) |

Loot inside our structures needs no code: place a block that has a pool and Decimation's handler does the rest.

## 6. Checks

- `python3 tools/devtest.py --live loot`: 16 checks (our inventory on the screen, the body, Decimation's crate by a
  real click with cooldown).
- `python3 tools/devtest.py --live checks`: supply drop lands.
- Dev test `items` dumps every item with its loot chance (docs/references/decimation_items.tsv).

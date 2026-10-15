---
name: decimation-code-loot
description: Use when changing, debugging or extending loot in the Decimation mod - crates or car wrecks that open empty or not at all, loot amounts, item lists, the loot screen, crate cooldowns ("This ... is empty"), supply drops, or putting lootable containers into generated structures or entities.
---

# Decimation loot code

Facts and the verified call order live in docs/decimation_code/loot.md; crate levels and bodies (our design) in
docs/loot_levels.md. Read the doc section you need, not Decimation's source.

## Quick reference

| Task | Where |
|---|---|
| right click flow | `LootInteractHandler` (deci/aK/k), doc section 3 |
| which block gets which list | `LootTableRegistry.buildLootTables` (deci/aD/l), `Deci.lootTable()` |
| roll a list into an inventory | `Deci.rollDecimationLoot(block, inv)` |
| our items in Decimation's lists | `Deci.addLootLike(item, stack)` |
| show any inventory on the loot screen | `fixes/Loot.open`, entity `fixes/LootBody` |
| our own lists | `fixes/LootPools`, config/deciworldgen_loot.json |
| cooldown | `Deci.lootReady`, `Deci.clearLootCooldowns` |
| supply drops in singleplayer | `fixes/SupplyDropScheduler`, `Deci.supplyDrop*` accessors |
| roll count off by one | `tools/patches/PatchLootRolls.java` (three jars) |

## Rules

1. Loot in a structure = place a block that has a pool. No new code; the handler finds any block of the table.
2. Decimation names only through `Deci` (new accessor first, javadoc with the readable and obfuscated name).
3. Test loot changes with the live dev test, never by reasoning alone:
   `python3 tools/devtest.py --live loot` (16 checks, a real click on a real crate). Add
   `'-Ptrace=deci.aK.k,deci.aB,deci.aD.l'` to see the calls.
4. Cooldowns are in memory per world object. Tests must call `Deci.clearLootCooldowns()` first or a rerun within
   15 minutes finds the crate empty.
5. The handler skips creative players: put the test player in survival for the click, restore after.

## Common mistakes

- Expecting the loot screen to open on the click tick: it opens two ticks later through the server task queue,
  which our singleplayer patch drains from the CLIENT tick (doc section 4).
- Reading the client's cooldown registry: only the server's is marked.
- Loot near a safezone: the handler refuses it (except supply drops and props marked safezone lootable).
- Changing a list in LootTableRegistry without calling its `init()` again: pools are built from the entries once.

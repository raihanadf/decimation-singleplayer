# Decimation code, feature by feature

How Decimation's own code works, one doc per feature, written from the fully named reference tree (`deobf/src`,
every method named since 16 Oktober 2026) and checked at run time with the call logger (docs/call_trace.md). Each
feature has a project skill (`.claude/skills/decimation-code-*`, named after the feature) that says how to work
on it. Package by package notes: deobf/notes/architecture.md.

Every doc and skill here must pass `python3 tools/skillcheck.py` (it looks up every class, method, accessor, path and
dev test the files name).

| Feature | Doc | Skill | Run time check |
|---|---|---|---|
| loot: crates, cooldowns, supply drops | loot.md | decimation-code-loot | dev test `loot` (real click on a Decimation crate, trace 16 Oktober 2026) |
| weapons: guns, shots, ammo, attachments | weapons.md | decimation-code-weapons | dev test `playerfire` (the player's shot end to end, trace 16 Oktober 2026) |
| NPCs and infected: humans, factions, zombies, spawning | npcs.md | decimation-code-npcs | dev test `shots` (armed NPC turn, trace 16 Oktober 2026) |
| sides, proxies, network, singleplayer gaps | singleplayer.md | decimation-code-singleplayer | dev tests `zombies` + `checks` with the handler packages traced (16 Oktober 2026) |
| vehicles: driving, fuel, seats, damage, despawn | vehicles.md | decimation-code-vehicles | dev test `drive` (a real drive with fuel, trace 16 Oktober 2026) |
| zones: safezone, military, police, radiation, scary, border | docs/zones.md (run time added) | decimation-code-zones | dev test `zones` traced (16 Oktober 2026); turfs go with clans |
| player data: conditions, humanity, bottlecaps, stats, sync | player.md | decimation-code-player | dev tests `zombies` + `checks` traced (16 Oktober 2026) |
| clans and turfs | clans.md | decimation-code-clans | dev test `places` traced: server clan / turf code 0 calls (16 Oktober 2026) |
| traders: offers, trades, banker, guards | traders.md | decimation-code-traders | dev test `trade` (buy, wrong price, sale, trace 16 Oktober 2026) |
| items and gear: armor, slots, food, drink, medical, throwables, traps | items.md | decimation-code-items | dev test `useitems` (bandage, splint, drink, 16 Oktober 2026) |
| blocks and props: locked doors, keycards, elevators, multiblocks | blocks.md (drawing: docs/prop_placement.md) | decimation-code-blocks | dev test `keycard` traced, tools/props/keycardcheck.py on a fresh seed 1 world (16 Oktober 2026) |
| menus, screens and HUD | menus.md | decimation-code-menus | dev tests `menu` + `stats` traced (16 Oktober 2026) |
| rendering and models | rendering.md (formats: docs/gun_model_spec.md) | decimation-code-rendering | dev test `gunview` traced (16 Oktober 2026) |
| backend, update checker, outside connections | backend.md | decimation-code-backend | live game log and thread dumps (16 Oktober 2026) |
| weather, day length, world rules | world.md | decimation-code-world | trace of weather and day length (16 Oktober 2026) |

## Findings to follow up

- Loot: the server task queue is drained from the client thread in singleplayer (loot.md section 4) [inferred risk].
- Loot: the loot hint reads the client's cooldown registry, never marked [inferred].
- Weapons: the hit handler's infected branch never runs (weapons.md section 4); the server trusts the client's
  damage value.
- NPCs: findTarget rays every living entity before asking isHostileTo, FactionHumanEntity walks every loaded entity
  each tick (npcs.md section 4) [inferred cost, not profiled]; InfectedEntity's client only canEntityBeSeen.
- Sides: PlayerLoginHandler is registered twice, so it would run twice per login on a dedicated server [inferred];
  a destroyed vehicle drops its items in singleplayer (NoDropsHandler is server only) [inferred].
- Vehicles: the HUD's despawn countdown says 900 s, the vehicle goes at 600 s, and the clock is not saved.
- Zones: every zone question walks all zones of the world (9.9 million isBetween calls in 24 s with 4435 zones);
  grows with the world [measured calls, time not profiled].
- Traders: a buy hands over `new ItemStack(item)` (no metadata, count or NBT); offer lists are replaced per trader
  object, a rare race in singleplayer [inferred].
- Items: bandages, splints and the morphine syringe hand nothing back (static shared "hand back" stack; run time
  checked, cause inferred).
- Rendering: all 13 world layers run per loaded entity per frame, empty and debug ones included [cost not profiled].
- Backend: one leaked kryonet thread every 15 s (measured: 27 -> 28 threads): FIXED v0.62.8, PatchBackend v2 cuts
  the BoehMod connection (bug.md).
- World: Decimation's storm weather can never show (both weathers id 0); its daily world time reset broke OUR
  WorldClock day count: FIXED v0.62.9, a saved counter (bug.md "WorldClock day never advances").
- Overrides of Forge added methods renamed by Decimation's obfuscator never run (`...Lost` methods,
  architecture.md "Method naming pass"); other classes may have the same break [not verified].

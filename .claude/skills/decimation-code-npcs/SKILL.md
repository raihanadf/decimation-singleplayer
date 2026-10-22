---
name: decimation-code-npcs
description: Use when changing or debugging Decimation's NPCs and infected: bandits, soldiers, Soviets, civilians, medics, zombies and their kinds (crawler, dog, frail, gawker, hulk, bloater), who attacks whom, NPC guns and shooting, NPC spawning, hordes, infection bites, NPC performance, or when code must spawn, identify or steer them.
---

# Decimation NPC and infected code

Facts and the verified turn order: docs/decimation_code/npcs.md. Our tiers: new_feature.md "Step 1 design: NPC tiers".

## Quick reference

| Task | Where |
|---|---|
| armed human logic | `BanditEntity` (deci/ag/a): `findTarget`, `isHostileTo`, `shootAt`; Soldier / Soviet override `isHostileTo` |
| faction of a human | `FactionHumanEntity.getFaction` (deci/ah/d), `Deci.npcHostileTo` |
| zombie logic | `InfectedEntity` (deci/ag/d): melee AI, variant (data watcher 21), horde speed, bites |
| spawn one | `Deci.newBandit` / `newSoldier` / `newSoviet` / `newInfected`, retry until `spawnEntityInWorld` is true |
| NPC shots | our `NpcShots` through `Deci.setNpcShotHook` (PatchTracer v2), not Decimation's `shootAt` body |
| tiers and gear | `fixes/NpcLoadouts` (gun synced through data watcher slot 26) |
| natural spawns | `MobSpawnRegistry` (deci/aD/j), zones: fixes/ZoneSpawnHandler, places: fixes/PlaceSpawns (config deciworldgen_places.json) |

## Rules

1. `deobf/src` predates our patches: read tools/patches/PatchTracer.java, PatchFactions.java and PatchInfectedAI.java
   before trusting the decompiled `shootAt`, `isHostileTo` of Soviets or the infected AI.
2. Infected walk speed BASE is reset every tick by the horde rule: change speed with attribute modifiers
   (operation 2), never `setBaseValue`.
3. NPCs ignore creative players: test reactions in survival.
4. Test live: `python3 tools/devtest.py --live shots` / `npc` / `zombies` / `noise`; trace with
   `'-Ptrace=deci.ag.a.eA,deci.ag.a.c,deci.ag.a.e,net.decimation.fixes.NpcShots'`.
5. Anything run per NPC per tick must stay cheap: check distance and faction before any ray (`canEntityBeSeen`).

## Common mistakes

- Reading `entityToAttack` as "is fighting": it is never cleared; use `getAITarget`.
- Expecting the client to show the server's gun: Decimation rolls guns per side; use NpcLoadouts' synced gun.
- Spawning in the air for a test: the client copy falls; stand NPCs on the DevTestArena floor.
- Testing a spawn check (Forge's CheckSpawn, `ForgeEventFactory.canEntitySpawn`) on the player's own spot: the player
  collides, so getCanSpawnHere fails; in peaceful it fails for every EntityMob. Use a spot away from the player.
- Forgetting traders are FactionHumanEntity too: a "every human" rule hits traders unless they are excluded.
- Spawning a mob from an EntityJoinWorldEvent handler while a chunk loads: World.addLoadedEntities walks the chunk's
  live list and adds the new mob again (Decimation's PlayerJoinSync swaps did: double speed mobs; fixes/JoinOnce now
  cancels such second joins). Check with `python3 tools/devtest.py doublejoin`; -Ptrackdebug logs any entity that
  joins twice with both stacks.
- Walking `world.loadedEntityList` while calling anything that can load chunks (a path search: tryMoveToXYZ builds a
  ChunkCache about 72 blocks round the mob; getChunkFromChunkCoords): a loaded chunk adds its entities to that list
  and the loop dies with a ConcurrentModificationException. Walk a copy (Garrisons.backToPosts since v0.64.6).

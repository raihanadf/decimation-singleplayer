# Decimation code: player data (conditions, humanity, bottlecaps, stats, sync)

Part of the feature study of Decimation's own code (16 Oktober 2026, index docs/decimation_code/README.md). Package
notes: deobf/notes/architecture.md "deci/Q".

## 1. What it is for

Every survival rule of Decimation that is about the player lives in one per player object: thirst (water), blood,
the conditions (infected, bleeding, broken leg, irradiated, scary), humanity (hero or bandit), money (bottlecaps and
gold caps), bounty, stance, recoil, the extra gear slots (backpack, vest, mask), the banker's items, the loot screen's
inventory and the kill counters.

## 2. Code map

| Class (obf) | Job |
|---|---|
| PlayerData (deci/Q/b) | IExtendedEntityProperties "decimation.player_data"; `get` (e) per player; every setter calls `markDirty` (df); conditions `setInfected` (f), `setBleeding` (g), `setBrokenLeg` (e), `setIrradiated` (i); `setWater` (v) 0 to 20, `setBlood` (w) 0 to 20; humanity `setHumanity` (K), `addHumanity` (I), `getHumanity` (cd) 0 to 100; money `getBottlecaps` (cb), `setBottlecaps` (g), `addBottlecaps` (k), cap 1 500 000; `tickMovementAndRecoil` (bx) every tick per player (stance speed modifier, recoil onto the view) |
| PlayerDataAttacher (common handler) | attaches PlayerData to every player (EntityConstructing) |
| PlayerTickHandler (common) | each player tick: START calls `PlayerData.tick` (thirst every 1600 ticks with damage at water 0, infection damage, bleeding drains blood every 1200 ticks then damage, radiation damage without a gas mask) with the naturalRegeneration game rule saved before and put back at END, so PlayerData can switch regeneration per player; END calls `tickMovementAndRecoil`; riders cannot strafe; prone players cannot sneak. Runs in singleplayer |
| PacketSyncPlayerData (deci/aE/a$Y), PacketSyncPlayerDataPublic (deci/aE/a$Z) | owner sync and tracker sync, sent after `markDirty` |
| PlayerCloneHandler, RespawnPointHandler, DeathHandler (common) | data across death and respawn, keep inventory, NPC humanity on kill |
| HumanityRank | the hero I to V / bandit I to V tiers shown on the HUD |
| ItemPickupHandler, DeathStatsHandler (server proxy) | bottlecap items into money, kills and deaths to the dead backend: never in singleplayer |

## 3. As it runs (call logger, dev tests `zombies` + `checks`, 16 Oktober 2026)

Traced `deci.Q.b.df,deci.Q.b.I,deci.Q.b.K,deci.Q.b.k,deci.Q.b.bx,deci.aE.a$Y,deci.aE.a$Z` plus our two handlers:

- `markDirty`: 1526 on the client thread, 214 on the server thread; the server sent PacketSyncPlayerData 211 times
  (once per dirty server tick), the client applied each one (`setHumanity` 211 times on the client thread: the sync
  handler writes every field through the setters).
- `tickMovementAndRecoil` 2556 (every tick, both sides).
- Kills: HumanityKillHandler.onDeath 5, `addHumanity` 2 (the checks killed infected: 100 -> 102); bottlecaps:
  BottlecapHandler.onPickup 1, `addBottlecaps` 1 (31 -> 36 caps); Decimation's ItemPickupHandler 0.

## 4. Singleplayer state and known faults

- The shared tick (thirst, blood, conditions) runs in singleplayer.
- Gone in singleplayer and replaced by us: bottlecap pickup (fixes/BottlecapHandler), humanity for ordinary kills
  (fixes/HumanityKillHandler), conditions refused inside a safezone (setters check that only on a dedicated server:
  fixes/ZoneRules), kill and death counters (the backend's profile: fixes/LocalStats and PatchMenuStats).
- `tickMovementAndRecoil` syncs clan humanity only on a dedicated server (clans do not exist in singleplayer).
- The client writes PlayerData too (markDirty 1526 on the client): client side setters do not reach the server;
  only the server's values are synced. Change player data on the server.

## 5. How our mod uses it

| Need | Use |
|---|---|
| read or change a player's data | `Deci.player(p)`: `water`, `setWater`, `bottlecaps`, `addBottlecaps`, `goldBottlecaps`, `maxBottlecaps`, `humanity`, `addHumanity`, `removeHumanity`, `bounty`, `infected`, `bleeding`, `brokenLeg`, `irradiated` and their setters, `safezoneGrace`, `combatTimer`, `healFully` |
| armor against NPC fire | fixes/ArmorGunfireHandler; protection table fixes/Protection (config/deciworldgen_protection.json) |
| local stats on the menu and HUD | fixes/LocalStats, tools/patches/PatchMenuStats.java |

## 6. Checks

- `python3 tools/devtest.py --live checks`: humanity kill, bottlecaps picked up, armor and helmet against NPC fire.
- `stats` (local counters on the menu and HUD), `zones` (conditions in safezones, radiation).

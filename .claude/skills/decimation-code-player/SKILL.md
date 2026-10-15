---
name: decimation-code-player
description: Use when changing or debugging a Decimation player's survival data - thirst and water, blood, infection, bleeding, broken legs, radiation, humanity and hero / bandit rank, bottlecaps and gold caps, bounty, kill and death stats, data lost or not syncing between client and server - or when code must read or set any of it.
---

# Decimation player data code

Facts and the measured sync: docs/decimation_code/player.md.

## Quick reference

| Task | Where |
|---|---|
| everything per player | `PlayerData` (deci/Q/b), from our code only through `Deci.player(p)` |
| thirst, blood, condition damage over time | PlayerTickHandler (common, runs in singleplayer) |
| money from bottlecap items | fixes/BottlecapHandler (Decimation's handler is server proxy only) |
| humanity from kills | fixes/HumanityKillHandler on top of Decimation's DeathHandler |
| stats on menu and HUD | fixes/LocalStats |

## Rules

1. Write player data on the SERVER; the setter marks it dirty and the next server tick syncs it to the client. A
   client side write is overwritten by the next sync.
2. A Decimation setter that refuses something "in a safezone" only does so on a dedicated server; our ZoneRules
   does it in singleplayer.
3. Test live: `python3 tools/devtest.py --live checks` (humanity, bottlecaps, armor) and `stats`; trace with
   `'-Ptrace=deci.Q.b.df,deci.Q.b.K,deci.Q.b.k,deci.aE.a$Y'` (obfuscated names: deobf/decimation.srg).
4. Conditions a player already has stay; a test of "cannot start" must clear them first.

## Common mistakes

- Counting `setHumanity` calls as humanity changes: the client's sync handler calls it on every sync.
- Reading `getBottlecaps` on the client right after a server change: wait a tick for the sync.
- Tracing `deci.Q.b.e`: that prefix hits `get`, called everywhere; trace the exact setter.

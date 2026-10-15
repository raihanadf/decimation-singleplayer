---
name: decimation-code
description: Use first when a task touches how Decimation's own (closed, obfuscated) code behaves and it is not obvious which feature it belongs to, or when looking for how a Decimation class or method works, where something happens at run time, or why something does not run in singleplayer.
---

# Decimation code skills index

Every feature of Decimation's code has a doc in docs/decimation_code/ (index and open findings:
docs/decimation_code/README.md) and a skill:

| Feature | Skill |
|---|---|
| crates, loot screen, cooldowns, supply drops | decimation-code-loot |
| guns in play: shots, damage, ammo, attachments | decimation-code-weapons |
| NPCs, infected, factions, spawning | decimation-code-npcs |
| sides, proxies, packets, singleplayer gaps | decimation-code-singleplayer |
| drivable vehicles | decimation-code-vehicles |
| zones | decimation-code-zones |
| player data: conditions, humanity, money, stats | decimation-code-player |
| clans and turfs | decimation-code-clans |
| traders | decimation-code-traders |
| non gun items and gear | decimation-code-items |
| interactive blocks: locked doors, keycards, elevators | decimation-code-blocks |
| menus, screens, HUD | decimation-code-menus |
| drawing and frame rate | decimation-code-rendering |
| backend and outside connections | decimation-code-backend |
| weather and world time | decimation-code-world |

## How the code is read and checked

1. Names: deobf/src is fully named (every method but compiler made lambdas); names.tsv maps obfuscated to readable;
   deobf/decimation.srg has every member. Our code reaches Decimation only through `Deci`.
2. Run time: the call logger (docs/call_trace.md) shows which methods run, in order, per thread; Decimation
   methods in a trace prefix go by their obfuscated names.
3. Every doc and skill of this family passes `python3 tools/skillcheck.py` (it looks up each class, method, accessor,
   path and dev test they name): run it after any edit.

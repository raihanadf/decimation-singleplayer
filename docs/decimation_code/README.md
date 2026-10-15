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
| NPCs and infected | not yet | | |
| network, proxies, singleplayer gaps | not yet | | |
| vehicles | not yet | | |
| zones and turf | not yet (docs/zones.md covers the zone rules) | | |
| player data: stats, humanity, bottlecaps | not yet | | |
| clans | not yet | | |
| traders and quests | not yet | | |
| items and gear | not yet | | |
| blocks and props | not yet (docs/prop_placement.md covers drawing) | | |
| menus and HUD | not yet | | |
| rendering and models | not yet (docs/gun_model_spec.md covers .bmodel) | | |
| backend and update checker | not yet | | |
| weather and world features | not yet | | |

## Findings to follow up

- Loot: the server task queue is drained from the client thread in singleplayer (loot.md section 4) [inferred risk].
- Loot: the loot hint reads the client's cooldown registry, never marked [inferred].
- Weapons: the hit handler's infected branch never runs (weapons.md section 4); the server trusts the client's
  damage value.
- Overrides of Forge added methods renamed by Decimation's obfuscator never run (`...Lost` methods,
  architecture.md "Method naming pass"); other classes may have the same break [not verified].

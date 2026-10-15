---
name: decimation-code-zones
description: Use when changing or debugging Decimation's zones - safezones (nobody hurt, no hostile spawns, water refill, grace timer), military and police zones (infected variants), radiation, scary and border zones, zone lists on the client, place name banners - or when worldgen creates zones or code asks whether something is inside one.
---

# Decimation zone code

Facts: docs/zones.md (data, checks, every zone type, client side, what fixes/ZoneRules makes work in singleplayer,
run time counts). Safezone sites: docs/safezone.md. Which zone handlers run where:
docs/decimation_code/singleplayer.md.

## Quick reference

| Task | Where |
|---|---|
| is X inside / near a zone | `ZoneManager` (net/decimation/mod/server/zones/b): `isPlayerInZone`, `isPlayerNearZone`, `isEntityInZone`, `isInfectedInZone` |
| zone types | `EnumZoneType` (net/decimation/mod/server/zones/a); ours: `ZoneKind`, converted by `Deci.zoneType` |
| zones our worldgen makes | worldgen/ZoneStore (per world deciworldgen_zones.json) |
| singleplayer rules | fixes/ZoneRules (copies of the server proxy handlers, plus our safezone protection) |
| natural spawn rules in zones | fixes/ZoneSpawnHandler |

## Rules

1. Decimation's own zone handlers never run in singleplayer; ZoneRules does their work and steps aside on a
   dedicated server. Change zone behaviour there, not in Decimation's classes.
2. Every zone question walks every zone of the world: never ask per entity per tick without a cheap distance or
   cached answer first; thousands of zones exist in a generated world.
3. Test live: `python3 tools/devtest.py --live zones` (16 checks). Run an arena mode before `checks` after `camp` or
   `refuge`: a leftover safezone blocks damage and spawns.
4. A check that fails only while a hot call trace runs is the trace's cost (docs/call_trace.md): rerun without.

## Common mistakes

- Using `EnumZoneType` directly in our code: obfuscated names go through `Deci`.
- Forgetting the safezone grace timer: it blocks every attack on the player for a while after leaving, radiation too.
- Expecting zone boxes on the client without a sync: the client only knows what PacketZoneLists sent (ZoneRules.sync).

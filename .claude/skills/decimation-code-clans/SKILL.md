---
name: decimation-code-clans
description: Use when touching Decimation's clans or turfs - clan files, turf capture and income, turf building rules, "Can't open that in another clans turf", clan chat, the floating turf names and the entering-turf banner - or when our place names (districts, bases) misbehave, since they ride on Decimation's turf list.
---

# Decimation clans and turfs code

Facts: docs/decimation_code/clans.md. Place names: docs/zones.md "Place names".

## Quick reference

| Task | Where |
|---|---|
| clan data and income | `ClanManagerV1` (net/decimation/mod/server/clans/a) |
| turf data and rules | `TurfManager` (net/decimation/mod/server/turf/a) |
| floating turf names | `ClanTurfLayer` (deci/M/l), client |
| our names on that path | worldgen/PlaceNames, tools/patches/PatchTurfNames.java |

## Rules

1. Clans and turfs are dedicated server only; in singleplayer nothing creates or captures them. Do not build a
   singleplayer feature on them; build on the client turf list (as PlaceNames does) or on our own data.
2. TurfManager reads its JSON from disk on every call: never call it per tick or per block.
3. Test the client side live: `python3 tools/devtest.py --live places`; the server side only on a dedicated server.

## Common mistakes

- Trusting `getTurfById` or `isMemberOnline`: both compare UUIDs with != and never match.
- Expecting `canPlayerUseInTurf` to look at the block: it uses the player's position.
- Forgetting the floating layer runs per loaded entity per frame: keep anything added there cheap.

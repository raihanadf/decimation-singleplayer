---
name: decimation-worldgen
description: Index of the worldgen skills for our Decimation mod - which skill to use for which job (city layout, streets, vehicles, interiors, decay, military bases, props, safezones, guns) and how worldgen work is tested. Use first when a worldgen request does not obviously belong to one skill.
---

# Worldgen skills index

| job | skill |
|---|---|
| city layout: districts, blocks, streets, levels, highways, rail, rivers, bridges, parks | decimation-city |
| sidewalks and crossings: traffic lights, signs, mailboxes, benches, poles | decimation-city-street |
| wrecks and road scenes: cars, roadblocks, checkpoints, crashes, road works | decimation-city-vehicles |
| inside city buildings by kind: EXIT, CCTV, TVs, cabinets, kind props | decimation-city-interior |
| bodies, blood, graffiti, trash, looted rooms | decimation-city-decay |
| military bases (FOB layout, modules) and compounds like them (camps, refuges) | decimation-military-base |
| placing ANY Decimation prop: drawn sizes, verified facing, fit, checks | decimation-props |
| guns | decimation-gun |

Planned (user, 11 Oktober 2026): decimation-military-interior (split from decimation-military-base).

## Rules for everything we generate
- No vanilla doors (user 13 Oktober 2026): worldgen/Doors swaps them in Slices and the small placer;
  a new writer that bypasses both must call Doors.swap too.

## How worldgen is tested
1. Server, no window: `python3 tools/servertest.py 1 type=decimation pregen=128,128,9 [dress]`.
2. Blocks from the region files: tools/worldcheck.py, tools/props/worlddump.py + propclash.py,
   walkcheck.py for compounds; diff against a run without the change.
3. Client, only after checking no game of the user runs (`pgrep -fl "zulu-8.jdk.*GradleStart"`, own
   step): `python3 tools/devtest.py --live [--dress] MODE`, world deciworldgen_devtest only. Read the
   sheet; shots to docs/shots/<topic>_v<version>/ + docs/shots_index.md.

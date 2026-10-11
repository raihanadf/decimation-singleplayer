---
name: decimation-city
description: Build, extend or review the Lost Cities style city of our worldgen for Decimation (converted DeceasedCraft buildings, streets, levels, highways, railway, rivers and bridges, parks, street scenes). Use when the user asks for anything placed in or between cities.
---

# Decimation city, end to end

The facts live in docs/city_engine.md (one section per feature: what it does, code map, how it
was checked); this skill is the order of work, the checks and the lessons from the user's
reviews.

Sub-skills: decimation-city-street (sidewalks, crossings), decimation-city-vehicles (wrecks, road
scenes), decimation-city-interior (inside buildings), decimation-city-decay (story layer); props in
general: decimation-props; all of them: decimation-worldgen.

## 0. Read first
1. docs/city_engine.md (the section of the feature, plus "Open"), docs/roadmap.md "Worldgen and
   cities", docs/shots_index.md for what earlier shots show (do not re-shoot what is described).
2. Code: worldgen/city/ (LcCity: cells, levels, plan(), street(); StreetPlan, BuildingPlan,
   LotPlan, EdgePlan, RiverPlan; LcContent: the converted pack; Highways, Rails, CityRivers),
   worldgen/Slices (window, written per population window), FixedBase.
3. Content comes from DeceasedCraft's Lost Cities data through tools/lcpack.py into
   config/decimation_worldgen/lc (local only: never commit it).
4. The lessons and casebook below.

## 1. Build
- A city chunk is one Plan (FixedBase): blockAt(lx, ly, lz) from its base Y. Keep the plan
  deterministic from the seed (hl / h hashes), never from what other chunks hold.
- Anything a street runs over or into (a bridge, a deck, a ramp, a station entrance) reuses the
  street's own plan (StreetPlan) and adds only what the place needs (deck, piers, railings).
- New flags for an existing plan go in its constructor (StreetPlan onWater), not in a copy.
- Log one line per placed feature with its coordinates ("lc bridge ... at x,y,z"): servertest and
  the dev tests read them.
- Changing layout rules at run time: LcCity.clearPlans().

## 2. Test
- Server first (no game window): `python3 tools/servertest.py 1 type=decimation pregen=0,0,20`,
  read the log lines, then the blocks: tools/worldcheck.py, or a small script with
  worldcheck.World printing a top layer / cross section (letters per block name). Compare the new
  thing with the land beside it row by row.
- Client: check for the user's game as its OWN step (`pgrep -f "zulu-8.jdk.*GradleStart"`; no
  `-Ddeciworldgen.autotest` = the user's game: stop and ask). Then the dev test mode of the
  feature (`python3 tools/devtest.py rail` / `bridge`), in deciworldgen_devtest only; the modes
  pick never generated chunks, each run moves on. Read the sheet.
- Checks must look only where the feature writes: DeceasedCraft buildings carry their own water
  (the sunken club: 194 blocks) and fountains.
- Shots to docs/shots/<topic>_v<version>/, described in docs/shots_index.md.

## Lessons (read before starting)
1. A bridge (or anything a street runs over) continues the street's own look: same part, same
   blocks, same height. DeceasedCraft's bridge part (quartz slab deck half a block below the
   street, white railings) read as a different road. Railings go on edges that face water or a
   lot, on top of a raised sidewalk when the street part has one (DC sidewalks are slabs at
   street + 1).
2. A structure written across two population windows can be flooded by a spring decorated in
   between: deny springs / lakes near anything open underground (TerrainEvents, Rails.near).
3. Sample the river at 3 x 3 points per chunk, not the middle: a diagonal course otherwise
   becomes chunks touching at their corners (ponds).

## Casebook (never delete a case)

### Case 1: bridges did not match the roads (v0.45.0 -> v0.45.1, 11 Oktober 2026)
- User (looked in game): "the bridge show up just need change the design so the bridge match
  with other road". Shots of 0.45.0: docs/shots/lc_bridge_v0.45.0/ (bridge_1 quartz deck).
- Cause: river street chunks used DeceasedCraft's bridge_1 / bridge_deadzone_1 parts; crossings
  a plain deci:BlockRoad deck.
- User choices (plan mode): "street over water" (the same street part, same height, deck and
  piers below, railings), wasteland bridges with a collapsed hole in the road.
- Fix: LcCity.street builds the land StreetPlan for river chunks too (no stairs, scene, fronts)
  and hands it to RiverPlan, which adds the stone brick deck at street - 1, piers, iron railings
  on non street edges (above a raised sidewalk) and the hole (across 5..10, along 4..11) for
  ":deadzone" styles. DC bridge parts dropped from LcContent and lcpack.
- Check: seed 1 server layers (bridge rows identical to street rows, deck at 63, water below,
  hole open), dev test bridge all PASS with the new "road block as the street" check.
- Evidence: docs/shots/lc_bridge_v0.45.1/ (shots_index.md).

---
name: decimation-city-street
description: Dress the city streets of our Decimation worldgen with Decimation props the way a real (abandoned) US street has them - traffic lights and stop / speed signs at crossings, the sidewalk furniture zone (mailboxes, newspaper boxes, benches, bins, bicycles, power poles, signal cabinets), all facing the right way. Use when adding, changing or reviewing anything on city sidewalks or at crossings.
---

# City streets (sidewalks and crossings)

Facts: docs/city_dressing.md section 2 (rules), docs/references/city_dressing_research.md (174
real world facts with sources: MUTCD, NACTO, city manuals), docs/references/prop_facing.tsv (where
every prop's front points per metadata, verified), docs/prop_placement.md (drawn sizes).
Code: worldgen/city/StreetDress (rules), StreetLayout (what a street chunk is made of), Dress
(fit), PropFacing (facing). Umbrella skill: decimation-props.

## 0. Read first
1. docs/city_dressing.md (principles, section 2, status / handoff at the end).
2. The research doc's topics "Traffic signals", "Regulatory road signs", "Sidewalk street
   furniture zone", "Street lights" (the "blocks" line of each fact is the Minecraft rule).
3. Lessons and casebook below.

## 1. Rules
- DIRECTION FIRST: a rule says where the front looks ("toward the buildings", "toward the
  approaching traffic") and calls PropFacing.meta(block, dir) / along(block, axis). Never write a
  metadata number. A prop missing from the table, or "low" confidence, is not placed.
- Right hand traffic. Street along z: west half southbound, east half northbound. Street along x:
  north half westbound, south half eastbound. A sign faces the drivers it serves (opposite their
  travel direction) on their RIGHT curb.
- Traffic light: one pole per corner on the FAR RIGHT corner of the approach it serves, lenses
  toward that traffic (lenses are 90 degrees LEFT of the arm: NE pole lenses S, SE W, SW N, NW E);
  the arm then lies over that approach. Second sidewalk cell from both curbs.
- Sidewalk bands: FRONTAGE (against the building), CLEAR path (never furnished), FURNITURE (the curb
  row). Mailboxes, newspaper boxes, benches at the curb face THE BUILDINGS (back to the road); a
  bus stop bench faces the road.
- The street's own part may already hold lamps, benches, planters: every prop slides up to 3 along
  the curb until it fits (StreetDress.place); nothing is forced.
- Power poles: one side of a whole street line, every second chunk, crossarm across the street.
- Not on the curb: the payphone cabinet (wall prop: lobbies), the small junction box pole (wall
  prop), the tall phone box (no verified front).

- Facades: shop signs and graffiti hang on the building's wall; done by BuildingDress.exterior on
  the building's outdoor spots (most buildings stand back from the chunk edge), by
  StreetDress.facades only where the wall stands at the edge (StreetPlan.neighbourBuilding).
- A DC gutter (flush cell between road and raised sidewalk) is road, never a curb spot.

## 2. Check (all before handing over)
- `python3 tools/servertest.py 1 type=decimation pregen=128,128,9 dress`, then
  `python3 tools/props/worlddump.py dev/run/server/world OUT.tsv` and `tools/props/propclash.py`;
  diff the findings against an undressed run (same command without `dress`): the dressing must add
  none. The log line "dress totals after N plans" gives placed / refused per prop and why.
- `python3 tools/devtest.py --live --dress cityprops` (never generated cell each run): every prop is
  shot from where its front points; a wrong facing shows the back. Read the sheet; save shots to
  docs/shots/city_dressing_v<version>/ and describe them in docs/shots_index.md.
- A doubtful facing: `python3 tools/devtest.py --live facing props=A,B` (each prop at meta 2..5 on
  coloured pads, shots from south and north), then a row in docs/references/
  prop_facing_overrides.tsv and `python3 tools/props/facing_table.py docs/references/prop_facing_audit.json`.

## Lessons
1. Verify facing per prop, never assume the placer rule: traffic lights look N E S W at 2..5, road
   signs, cars, bicycles their own orders (v0.50.0 audit, two readers + judge + pictures).
2. A judge can be wrong too: road signs 60 / one way were judged opposite to their family; the
   pictures settled it. Same model = same table.
3. Read the street from its blocks, not from assumptions: DC suburb parts have flush sidewalks
   (paving and grass on a dirt road); the road is the commonest flush block in the middle.

## Casebook (never delete a case)
### Case 1: first street dressing (v0.50.0, 12 Oktober 2026)
- Request: "all city ... full of props ... dont forget the direction".
- Found on the way: a third of the street sides had no sidewalk found (flush suburb parts); fixed
  rules at one along position collided with the part's own lamps / planters (power poles never
  tried); fixed by reading the road block and sliding props along the curb.
- Result: seed 1 area 35 traffic lights, 23 stop signs, 14 speed signs, 46 mailboxes, 87 newspaper
  boxes, 51 benches, 113 bins, 28 bicycles, 13 power poles; 0 propclash findings from the dressing.
- Evidence: docs/shots/city_dressing_v0.50/, docs/shots/facing_v0.50/.

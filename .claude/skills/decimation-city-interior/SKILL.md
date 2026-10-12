---
name: decimation-city-interior
description: Dress the insides of the converted city buildings of our Decimation worldgen by building kind (homes, offices, shops, food, medical, police, industry, military, lab, civic) - EXIT lights over exits, CCTV at entrances, cabinets, TVs, vending machines, kind props against walls, looted story - facing into the room, never in a doorway. Use when adding or reviewing props inside city buildings.
---

# City building interiors

Facts: docs/city_dressing.md section 3, research topic "Street facing ground floors" (EXIT, CCTV,
cabinets, looted rooms), docs/references/prop_facing.tsv (mount wall / ceiling / floor and front).
Code: worldgen/city/BuildingDress (rules), BuildingLayout (stand spots, walls per side, room height,
doors, sky; cached per schematic), Dress, PropFacing, PropFit (fixes the converted furniture first).

## Rules
- Building kind from its name (BuildingDress.kind). Props of the kind against walls, front into the
  room; wall props hang in the air cell in front of the wall with the front away from it; ceiling
  props in the cell under the ceiling.
- EXIT light over each door on the inside (2 up, facing into the room); CCTV one cell beside an
  entrance under the ceiling (it only looks N or S: east / west doors get none).
- Shop display: its FRONT is the closed panel: front toward the wall so the shelves face the room.
- Decals: registry deci:decal.<name>. Blood splats draw 2 x 2, the US flag 3 x 3 centred on their
  cell: the whole wall behind that square must be solid. Never decal.notegeneric (floating
  "information" icon) or decal.imagegeneric (downloads a web picture).
- Floors are real tops only (not open trapdoors, wire); skip ("keep the world") cells are solid.
- A front never looks into anything over 0.6 high with something above it (propclash FACE).

## Check
servertest dress + worlddump + propclash diff (0 from the dressing); cityprops shots (exitlight,
cctv, tv, vending, skeleton, cabinet, payphone, washer, shopdisplay, graffiti, decal, bodybag).

## Lessons
1. A prop drawn thinner than twice the test margin (a flat TV is 0.125 deep) turned inside out when
   shrunk and passed every check (PropGeom.shrink keeps 0.04).
2. Props stood on open trapdoors (upright) and wire: floor = a real top.
3. The checker and the placer must judge alike (thresholds, front rule, solid shapes): every
   difference showed up as findings the placer thought fine.

4. Variety and balance (user review of v0.51.2): one short prop list per building kind made
   buildings of a kind look alike and some props too common. Give each building its own pick of
   the kind's palette (seeded subset, a theme), and cap each prop per building / room size.
   Done in v0.53.0 (BuildingDress.PALETTE / theme / story). Measure before and after:
   `servertest.py 1 type=decimation dress` then `tools/props/dressbalance.py` (mean similarity of
   two buildings of a kind under about 0.45, no prop over about 10 % of all).
5. Never place a BlockChest subclass (vanilla chest, BlockStorageCrate footlocker) where its facing
   matters: it turns itself from its neighbours when placed and ignores the metadata.
6. Furniture reads real in groups, not single props: a desk is table + monitor + chair looking at
   it, a waiting area is a row of chairs, a shop is runs of shelves.
7. Loot is per block in Decimation: to change what a building gives, swap its containers in place
   (Dress.put air, then Dress.place the new one with its front toward the open side; put the old one
   back when it does not fit). A military crate is drawn 1.15 high: never stack two, and it fails
   where a wood crate stood under a shelf (the base kept 24 of 87).
8. Per building variety needs per building choices in EVERY layer, loot too: one swap mix for all
   military buildings made their crates a third of their props (similarity 0.79); a store type per
   building (armory, ammunition, aid, supply) breaks it.
9. A rule written for "the entrance" must test that it is one: barricade() piled every door, inner
   ones too (sandbags a third of a bunker's props). Entrance = one side of the door outdoors.

## Casebook (never delete a case)
### Case 1: first interior dressing (v0.50.0, 12 Oktober 2026)
- 574 EXIT lights, 375 cabinets, TVs, vending machines, washers, police / medical / military props,
  skeletons with blood, body bags, trash, graffiti and warning decals on seed 1; the dressing adds
  0 propclash findings (1 left: water flowed in from the world after generation).
- Evidence: docs/shots/city_dressing_v0.50/ (exitlight, tv, vending, payphone, skeleton, bodybag).

### Case 2: user review of v0.51.2 (12 Oktober 2026)
- User walked a new world: refuge accepted ("really nice and fit with other structure"); props
  "nice improvement but still need learning more ... not balanced and mostly similar between other
  building". Open: per building palettes and caps (lesson 4).

### Case 3: variety and balance (v0.53.0, 12 Oktober 2026)
- Fix for case 2: palettes per kind with weights, caps and placement kinds (ROW, DESK), a theme per
  building (kept entries, weights, variant, density) and a story per building (calm, looted,
  squatters, massacre, quarantine, barricade).
- Numbers (seed 1, tools/props/dressbalance.py): similarity 0.61 -> 0.39, distinct props 51 -> 73,
  top prop 11 % -> 8 %; propclash 25 findings, none from the new dressing after two fixes found on
  the way: the footlocker's chest re-aiming (lesson 5) and a table into a hopper (PropGeom.shape
  now counts hoppers whole, as propclash does).
- Evidence: docs/shots/city_dressing_v0.53/ (desk, dining table). Waiting for the user's look.

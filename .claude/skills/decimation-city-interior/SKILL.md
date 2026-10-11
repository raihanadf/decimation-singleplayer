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

# Military airport in a wasteland city (design, 14 Oktober 2026)

User idea (docs/ideas.md item 18, 13 Oktober 2026): "airport military base with barracks, HQ, tower, aircraft
park and hangar on wasteland". Choices taken (asked with options, 14 Oktober 2026):
- size: HALF a wasteland sector (2 districts side by side, 2 x 4 cells), any city with a wasteland sector can
  get one (about 1 in 3);
- aircraft: planes BUILT FROM BLOCKS (fighters, a cargo plane) plus Decimation's helicopter wreck on a pad;
- state: HELD AND HALF WRECKED: the enemy military still holds it (garrison posts like the bunker, elite and
  juggernaut, military infected), but a plane crashed on the runway, a hangar burnt, barricades;
- loot: BUNKER LEVEL IN PLACES: HQ and armory locked (keycard), top military loot inside; hangars and barracks
  normal military loot.

## 1. Reference (what a military airfield has, in the order a visitor meets it)
- Runway with shoulders, edge lights, threshold and centreline markings; a parallel taxiway joined to it by
  stub taxiways; aprons (aircraft parking) beside the taxiway; hangars grouped next to the aprons (NCAP,
  "Anatomy of airfields": https://www.ncap.org/news/2026/anatomy-of-airfields); a control tower with a view
  of the whole runway; fuel stores with their own road loop (same source), expeditionary fuel in collapsible
  bladders (SLDinfo, FARP at WTI 2-26: https://sldinfo.com/2026/06/fueling-the-forward-edge-the-farp-practical-application-at-wti-2-26/);
  HQ, barracks, armory on the landside; perimeter fence, entry control point.
- Main runways are about 45 m wide (UK ACP 31 reference: https://scottbouch.com/rtfm/uk/sequential/acp/31/b1/al6/acp31-b1-s3-c1.pdf);
  layout rules for military airfields are in UFC 3-260-01 (https://www.wbdg.org/node/23093). Scaled to the
  game: a runway 14 wide and about 220 long reads as a runway beside 3.85 wide cars [inferred: scale].

## 2. Placement (city/Airport, LcCity.block)
- One airport per city at most: the city's first wasteland sector by hash, 1 in 3 cities; the half of that
  sector (west / east: runway along z; north / south: along x) facing away from the rest of the city where
  possible. No airport when a river cell lies in it.
- The half sector is one BLOCK of 2 x 4 cells (LcCity.block kind 3, like the 2 x 2 superblock): its first
  chunk column and row stay streets (so it joins the wasteland's streets and the fence lines on sector edges
  keep working), the rest (7 x 15 chunks = 112 x 240 blocks) is the airport canvas. One ground level for the
  whole block (the lowest member level), edge plans of the member cells kept (land fence, ramps).
- New worlds only (StructureData flag), like every other city layout change.

## 3. Layout (canvas 112 x 240, runway along z, the entry gate on the north side toward the block's street)
| x | what |
|---|---|
| 0..2 | perimeter: chain link, posts, wire; HESCO guard towers at the corners |
| 4..25 | runway strip: runway x 8..21 (14 wide), shoulders, edge lights, threshold bars, centreline dashes |
| 26..33 | grass and gravel, stub taxiways across every ~60 |
| 34..41 | parallel taxiway (8 wide, yellow centreline) |
| 42..71 | apron (concrete) z 50..190: fighters in a row, the cargo plane, the helipad with the helicopter wreck |
| 72..108 | landside: 3 hangars facing the apron, control tower at the apron's middle, HQ (locked), armory (locked), barracks rows, fuel farm (bladders, drums, a pump), motor pool |
| 109..111 | perimeter |

Along z: north perimeter with the entry control point (chicane, booth, barrier arm) on the street side; the
runway z 10..229.

## 4. Aircraft from blocks
- Fighter (about 13 long, 9 wide, 4 high): grey clay fuselage, glass canopy, slab wings and tailplanes, a fin,
  coal block intake and nozzle, gear legs of fences.
- Cargo plane (about 20 long, 22 span, 6 high): hollow fuselage 3 x 3 with the rear ramp down (walk in), high
  wing, four engine pods, crates and a care package inside (loot).
- Wrecked fighter on the runway: broken in two, a wing off, black and grey scorch, debris, burnt ground.

## 5. Held and half wrecked
- Garrison posts (worldgen/Garrisons as at the bunker): the tower top, the ECP, hangar doors, the HQ; elite and
  juggernaut tiers; a spawner of military infected in the burnt hangar.
- Damage: the crashed fighter on the runway, the burnt hangar (black clay patches, roof holes), sandbagged
  positions and barricades across the taxiway, a few scorch marks (no holes: the user rejected the ditch).

## 6. Loot
- HQ and armory: Door_Emergency_1_Locked + military keycard screen (as the TOC and wasteland buildings), weapon
  cabinets, large ammo cases, military crates (Decimation's loot lists by block).
- Hangars, barracks, cargo plane: military crates, ammo cases, care packages, footlockers.

## 7. Checks (before showing the user)
- Canvas.validateProps 0, tools/props/propclash.py 0, tools/props/walkcheck.py (every door, ladder, loot
  reachable from the gate), dev test `airport` shots (sky), servertest with `lc airport at` log and a top
  down block picture (tools/topdown_blocks.py).

## 8. Built (v0.62.0, 14 Oktober 2026)
- Code: city/Airport (sites: one per city at most, 1 city in 3, the city's wasteland sector with the lowest hash,
  the half facing away from the city; no river cell; block kind 3 of 2 x 4 cells, LcCity.block / ground /
  planAirport), military/Airport (the canvas builder), military/AirportPlan (FixedBase, Garrisoned: 15 posts,
  1 spawner; the boom arm turned with the plan), CityLayoutEvents (StructureData `!airports`, new worlds,
  -Ddeciworldgen.airports). WastelandFence: no gate where the street beyond a fence line is an airport (its own
  fence closes it). Rails: no station segment in an airport block (the stairs came up through a hangar).
  TerrainEvents: no lakes and no springs over or next to a city (a lake carved through the airport ground, a
  spring ran over its road).
- Layout as section 3, with the perimeter fence on the outermost row; white paint and the planes in wool (grey
  and white stained clay read pink-brown in Decimation's pack), yellow lines in clay (reads yellow).
- Checked: dev test `airport` (sky at -20000, 230, -18300, no garrison): clashes 0, propclash 0 on 118 props,
  walkcheck: every ladder, door and loot reachable, the HQ and armory doors locked (keycard); shots in
  docs/shots/airport_v0.62/. Seed 1: `servertest.py 1 type=decimation gatescan=0,0,3000` lists 4 airports
  within 3000 blocks (2 along z, 2 along x); the ones at -2544,64,1296 (runway along z) and 272,64,-2032
  (along x) generated and read back: no water, no holes in the ground, garrison registered (15 posts, 1
  spawner), top down pictures topdown_alongz.png / topdown_alongx.png.
- Not checked: the garrison manned in game (posts spawn on a player's first visit), how it feels to walk in.


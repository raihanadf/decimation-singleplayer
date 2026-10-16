# City engine: Lost Cities style cities from converted content (v0.24, 8 Oktober 2026)

User direction (8 Oktober 2026): use DeceasedCraft's buildings as the
city content instead of procedural buildings, with the Lost Cities layout:
road, sidewalk and building doors at the same height, the city divided by
chunk and height level, a structure (stairs / bridge) between street
levels. Content study: docs/references/deceasedcraft_buildings.md.

## Content (offline, local only)

`python3 tools/lcpack.py OUT_DIR dc=<data>:deceasedcraft legacy=<data>:c70cities`
(venv python) converts every building / multi building the packs' city
styles list (rotation variants are separate entries there), plus each
style's stairs parts, into `OUT_DIR/<pack>/*.schematic` and
`OUT_DIR/index.json` (per building: file, pack, styles with weights,
size in chunks, groundY = layer of the ground floor, height; stairs per
style; "names" = id -> block name of the registry world). OUT_DIR is
`<instance>/config/decimation_worldgen/lc/` (dev: run/client and
run/server; Prism: the instance's config). DeceasedCraft content: never
committed (dev/run is git ignored). 2026-10-08: 290 buildings (170
current beta, 120 legacy), 8 stairs styles, 1.7 MB.
Cellar padding under multi building chunks with fewer cellars is bedrock
= "keep the world".

## Engine (`dev/src/main/java/net/decimation/worldgen/city/`)

- `LcContent`: loads index.json at init, remaps schematic ids to this
  world's blocks by name, caches schematics (`shape`).
- Files (split 2026-10-09): `LcCity` (layout: cells, levels, blocks,
  districts, plan(), shared helpers hl / partAt / decor / reshape),
  `StreetPlan` (street chunks: own dressing or district street parts,
  scenes, fronts, stairs), `BuildingPlan`, `LotPlan` (parks), `EdgePlan`
  (city edge ramp), `StreetProps`, `Highways`, `LcContent`. The plan
  classes reach LcCity's helpers by `import static ...LcCity.*`.
- `LcCity`: replaces CityDistrict + paintStreets in CITY sectors when
  content is installed (`LcCity.enabled()`; `-Ddeciworldgen.lccity=false`
  switches back to the procedural city).
  - Cell = 4 x 4 chunks (the existing city cell). Chunk column 0 and row
    0 of each cell are STREET chunks, the 3 x 3 rest holds buildings.
  - Level per cell 0..2: smooth value noise on a 3 cell lattice; at most
    the distance in cells to a non city cell, so the city meets the land
    at level 0. Ground G = 64 + 6 * level (Lost Cities FLOORHEIGHT).
  - District style per 2 x 2 cells ("dc:suburb_residential",
    "legacy:deadzone"...): since v0.24.3 the wasteland district (legacy
    deadzone) within 2 cells of a military sector, elsewhere a weighted
    pick (current beta districts weight 2, legacy town styles 1). Seed 1
    spawn area: 98 current, 35 deadzone buildings.
  - Buildings: weighted pick (style factors) of buildings that fit the
    free part of the 3 x 3 block (multi buildings up to 3 x 3 chunks),
    cellars must stay above y 4 and the top below 250 (laboratory with 90
    deep cellars, casino 276 high are skipped). 6% of chunks stay empty
    lots (grass, dead bushes). Placed with their ground floor layer at G
    (`FixedBase` plans: Slices skips terrain sampling).
  - Streets: road surface at G; a street chunk whose street continues
    into a cell one level higher gets a stairs part of the district,
    turned so its high side (west in the data) faces that cell.
  - Street dressing (v0.24.1): 3 wide sidewalks (double stone slab) flush
    with the road on both sides of straight street chunks, corner squares
    at crossings, dashed centre line (Road_CenterLine, meta 4 north-south /
    2 east-west), street lights on the sidewalk's road edge (one per side
    per chunk, sides staggered, 25% missing), benches and bins on the inner
    column facing the road, trash bags (6 / 256 of sidewalk cells), wrecks
    in lanes 5 / 10 (7% per lane per chunk). Facing rules are the old
    street's (prop front east 2, south 3, west 4, north 5). Stairs chunks
    get no furniture or cars. Chances use a full 64 bit integer hash (a
    double scaled to a long had zero low bits: every chance passed).
  - Zones: POLICE per building; the deadzone (wasteland) district is one MILITARY zone as a whole
    since v0.54.0, the city's military area (docs/wasteland.md: zone, military building pick in new
    worlds, loot, dressing, enemy military).
- Rules taken from Lost Cities' source: street surface at G, ground floor
  floor layer at G, cellars below, stairs at G + 1 toward the higher
  neighbour (LostCityTerrainFeature.generateStreet / generateBuilding /
  generateStreetDecorations); floors -cellars..F with the top part at
  floor >= F (BuildingInfo).

## District street parts (v0.27.0)

User choice 8 Oktober 2026 ("really big gain own street pieces"). Each DC
city style lists street parts (citystyle "streetblocks" / "parts": all,
t, straight, bend, end, none, full; 61 parts, e.g. bus stop, newspaper
stand, adboard, police roadblock variants). Legacy styles have none: the
deadzone and the legacy towns keep our own dressed streets (v0.24.1).
- Placement as Lost Cities' generateNormalStreetSection: count the
  connections (neighbour street chunk at the same street level, or a
  highway chunk at the deck level); 0 none, 1 end, 2 straight or bend, 3
  t, 4 all. Unturned: straight runs along x, end opens west, bend west +
  north, t all but south. Its Transform ROTATE_90 maps part (x, z) to
  (15 - z, x), which is exactly one of our clockwise turns, so LC
  rotations map 1:1 to turns. Slice 0 at the street surface G. Stairs
  chunks keep our road + stairs part.
- Parts: 16 x 16, 5 wide sidewalks raised by half slabs, a 6 wide road
  (part z 5..10), lamps and benches of their own. Our furniture is not
  added; scenes and fronts still are. Wrecks: since v0.49.0 one car per
  chunk (13%), centred on lane 7 or 8 (a wreck is drawn 3.85 wide, the
  road 6), only where the part, its scene and fronts leave its drawn box
  empty (`StreetPlan.carFits`); before, lanes 6 / 9 (7% each) put cars into
  the raised sidewalks, hedges and each other.
- Road paint: the refueled mod's decals (side, corner, side_corner, zebra,
  about 1800 blocks over the 61 parts) are converted by lcpack.py
  `paint()` into the layer below: lines -> deci:BlockRoad_CenterLine
  (meta 2 along x for paint facing east / west, 4 along z), zebra ->
  white quartz. Turning a part by an odd number of turns flips the line
  meta (XOR 2) in `LcCity.partAt` (Rotation leaves Decimation blocks
  unchanged; since v0.49.0 partAt turns every Decimation prop with
  `PropBoxes.turn`, so benches and lamps of turned parts, scenes, fronts
  and parks face as the part does). Translator additions: oxeye daisy, lily pads, ochrum,
  construction barricade (hazard barrier), command block / observer skip.
- Shots lc_streets_v0.27.0 (shots_index); autotest all pass, 0 generator
  errors.

## Parks, street scenes, fronts (v0.26.0)

User said "try next" on 8 Oktober 2026 after testing 0.25.0; this is the
next listed option. Rules read from Lost Cities (BuildingInfo,
LostCityTerrainFeature.generateStreet / generateFrontPart).
- Pack: `tools/lcpack.py` exports each city style's selectors "parks",
  "fountains", "fronts" into index.json "decor" (repeats = weight; road
  override as highways). 8 styles: dc 11 parks each, 1..2 fountains, 1..5
  fronts; legacy 12..14 parks, 9..10 fountains, 4 fronts.
- Parks: open lots now LOT_CHANCE = 10% of building chunks (was 6%) and
  carry a park part of the district (fallback legacy:standardcity, never
  for the deadzone) on their grass, layer 1 up (as Lost Cities: park part
  at street level + 1). Logged as "lc park". Seed 1 spawn: 16 parks,
  plazas with benches and a fountain pool, deadzone plazas with planters.
- Street scenes: DeceasedCraft's "fountains" are scenes in the road (bus,
  ambulance, roadblock, trash; legacy has real fountains too). 6% of
  straight street chunks without stairs get one, turned along the road;
  no extra wreck in that chunk. Logged as "lc street scene".
- Fronts: a straight street chunk looks up the chunk beside it on each
  side along the street, in that chunk's block plans (resolved lazily at
  write time, so block plans never recurse; the far side of the street
  belongs to another block and works too). A building chunk at the same
  street level gets one of its district's fronts with FRONT_CHANCE = 0.5,
  turned so the part's x 0 side faces the building (west 0, north 1, east
  2, south 3). Street chunks are now 13 high (fronts up to 12). Fronts are
  0..7 deep, so they cover the sidewalk and part of the outer lane; two
  hotel fronts facing each other read as a glass walkway over the road.
- Shots lc_decor_v0.26.0 (shots_index). Autotest all pass, 0 generator
  errors.

## Highways (v0.25.0, `city/Highways.java`)

User choice 8 Oktober 2026 (over fronts / parks, giant buildings,
interiors). Lost Cities' own bridges are road decks across non city
chunks between level 0 city streets (BuildingInfo.calculateXBridge); our
cities fill whole 256 block sectors, so those gaps barely exist. Highways
between cities do the job instead.
- Network (pure function of the seed, `tools/hwmap.py` is the Python
  copy, old layout only: worlds since v0.60.0 use CityLayout and a gap of 5, see "City sizes"): each region row has one highway chunk row (region start + 0 or 8
  chunks, by hash). That is always a city street row, superblocks
  included (they sit on even cells). Where the row leaves a city region and
  the next city region lies at most MAX_GAP = 3 non city regions away,
  every chunk between is an east west highway. Region columns the same
  way (north south). Both on one chunk: a crossing part. Seed 1: x row
  chunk 0 from x 256 to 511, z column chunk 0 from z -512 to -1.
- Deck at DECK = 64 (city level 0 street surface): parts are placed with
  slice 0 (deck underside) at 63, so the road (slice 1) is flush with the
  city street it continues.
- Kind per chunk, decided by the first slice from the terrain it can see
  (5 x 5 soil samples) and stored in StructureData as "hw_X_Z": TUNNEL when
  the median is 6+ above the deck, BRIDGE over water or ground more than 2
  below, else OPEN. Parts from the pack's world style "highways" selector
  (dc modern: 13 open, 13 bridge with adboard / sideblock / crash
  variants, tunnel, the 3 _bi crossings). Open and bridge chunks clear 20
  blocks above the deck; open chunks fill dirt down to the ground; bridges
  get 2 x 2 stone brick pillars at both rails of every chunk down to solid
  ground (through water). Wrecks in lanes 4 / 11, 8% each per chunk.
- Side ramps: land beside OPEN chunks ramps from the deck to the natural
  height over 3..8 blocks (2 per block of difference, smoothstep, shared
  `LcCity.reshape`); columns within the city edge band are the city's.
  The city edge ramp skips highway chunks.
- Small and large sites within 4 blocks of a highway chunk are dropped.
- Pack: `tools/lcpack.py` exports the highway parts into index.json
  "highways"; the deck asphalt (Biomes O' Plenty black sandstone) becomes
  deci:BlockRoad there (plain translation gave beige sandstone).
- Checked (seed 1): 13 highway chunks in the server spawn area (11 open,
  2 bridges over a ravine), more in the client run (7 bridges, 5
  tunnels); side profile at z -128 climbs 4,5,5,6,6,7,7 above 60 from the
  road; edge band still 0.3% steps over 1 block; autotest all pass. Shots
  lc_highway_v0.25.0(b), lc_tunnel_v0.25.0.
- v0.29.0 polish (user choice 9 Oktober 2026):
  - L links: a city region with no city within MAX_GAP + 1 regions
    straight east, west, north or south (so no street and no straight
    highway reaches it) gets one L highway to its nearest city at most 2
    regions away on both axes (`Highways.link`, cached per region): along
    its region row's x line to the target's z line, or the column's z line
    to the target's x line, whichever passes only non city regions (x
    first tried first). The corner chunk is a crossing part. Seed 1, 25 x
    25 regions: 10 links, groups of connected cities 29 -> 19;
    `tools/hwmap.py` prints them as ('L', A, B, ...).
  - Crossing parts have no rails: sides facing open land (no highway, no
    city) get a leaf hedge on the deck (index 1 / 14, like the hedge rows
    of the straight parts).
  - No tunnel within a city's edge band (LcCity.nearCity): the band graded
    the hill away and left a short free standing underpass; such chunks
    are cut open instead.
  - Bridge chunks get side ramps too, but only land above the deck is cut
    (the gap under the bridge stays). Seed 1 spawn area vs 0.28.10: 467
    blocks in 3 chunks, all beside the ravine bridge at z -112..-97.
  - Checked: L link (3,5) -> (4,6) generated with pregen at 1100,1460
    (8 open x chunks, crossing at 1152,64,1408, 7 z chunks with one
    bridge); shots lc_highway_v0.29.0. edgescan of the spawn area: 0.4%
    of band pairs step over 1 block, same before and after this change
    (it was 0.3% at v0.24.4; the extra 6 block steps at z -1, x -169..
    -233, 64 vs 70, come from an earlier version `[not investigated]`).
  - Still open: lamp heads now translate to redstone lamps (translator
    fix of 9 Oktober); a corner hedge is thin from above `[not verified]`
    in game by the user.

## Railway (v0.44.0, `city/Rails.java`)

User choice 11 Oktober 2026 (roadmap worldgen item 2: rail first, then
bridges over rivers through cities): an underground subway like
DeceasedCraft's Lost Cities profile (railways and underground stations on,
surface stations off), tunnels linking nearby cities.
- Lost Cities' design (read from its source, Railway.java / Railways.java):
  tracks at RAILWAY_LEVEL_OFFSET -3 city levels on a station grid,
  station_underground_stairs with station_staircase parts per level up to
  station_staircase_surface on the street, station extensions beside it,
  buffer ends, ramps for surface stations, rail dungeons. Parts are run
  with HardAirSetting.AIR: " " keeps the world, structure void is air.
- Ours: track floor RAIL = 46 (rail at 47), 3 levels below the level 0
  street. Lines are the highway lines (Highways.line): a city region's
  highway row and column carry track over the whole region (always street
  chunks: never under a building, never in a cellar); between city regions
  wherever a highway would span the gap (Highways.spans, up to 3 regions,
  no L links). Both lines on one chunk: crossing (rails_3split, a 4 way in
  DeceasedCraft's version; an arm without a line beyond is walled off). A
  line with nothing beyond the city: buffer end (rails_horizontal_end, open
  west unturned, turned per side). One station per city region on its row:
  street segment 0..2 by hash (chunks 4k+1 .. 4k+3 of the region: never a
  crossing, a line end or a stairs chunk), the middle chunk with the
  stairs and the street entrance (replaces that street chunk with the
  plaza), the two beside it plain platforms (station_underground).
- Content: `tools/lcpack.py OUT_DIR --rails dc=<data>:deceasedcraft` adds
  index.json "rails" (RAIL_KINDS) to an existing pack with the pack's own
  id table. Translation (rail_override): Create tracks and vanilla rails to
  1.7.10 rails (meta 1 along x, 0 along z, crossings along x), structure
  void to air, " " to the keep marker; the staircase's water wall to light
  blue stained glass (its frame does not hold water here, it flooded the
  station); the end part's interior (rows 4..11, layers 1..4) carved to air
  (" " there kept the rock and buried the track).
- Placement: per population window like the highways (only the chunk's
  part inside the window). Rails keep the part's metadata (placing a rail
  reshapes it toward its neighbours). A powered rail on a redstone block in
  the middle of every straight chunk (minecarts keep speed between
  cities). Station and platform chunks get glowstone in the ceiling
  between the beams (the parts' own glowstone sits under slabs, which block
  light in 1.7.10: the station was black). Water variants
  (rails_*_water) when the six Lost Cities sample points at track height
  are liquid, decided once per chunk (StructureData "rw_X_Z") `[not seen
  yet]`.
- Springs and lakes stay off the tunnels (TerrainEvents): a tunnel chunk
  is written by two population windows; between them a neighbour's
  decoration found the open stone face of the first half, put a spring
  there (WorldGenLiquids: stone with one open side) and flooded the tunnel
  at once; the second half then replaced the source and left diamonds of
  standing water and lava. Now Decorate LAKE (springs) and Populate LAKE /
  LAVA are denied where their area touches a rail chunk (Rails.near).
  City cellars could meet the same springs `[not checked]`.
- Not used: surface stations and ramps (rails_down1/2, station_open),
  rail dungeons, rails_bend (our lines only cross). The tunnel's single
  track does not meet the end part's two tracks (DeceasedCraft's parts as
  they are).
- Tools: `tools/hwmap.py SEED R --rails` prints every station and the
  chunk kinds (Python copy of Rails.kind). Dev test `rail`: the first
  station east of region 40 on region row 6 (or `-Prx=N`, `-Prz=N`) whose
  chunk was never generated; generates around it; checks the 48 rails
  through the station, no liquid in 11 tunnel chunks, a player can walk
  from the street down to the platform (flood fill in the station chunk);
  shots entrance, platform, stairs, shaft, tunnel, every line end of the
  region, crossing (docs/shots_index.md "lc_rail_v0.44.0"). Each run
  moves on to the next fresh region (the camera's view distance
  generates chunks too).
- Checked: seed 1 server world, stations at -152,47,8 and 40,47,8 as
  hwmap predicted, station section (track 47, platforms, stairwells 52..63,
  entrance at 64), the gap line x 256..511 complete (256 rails, no liquid,
  highway deck above); dev test all PASS at region 69,6 (street level 70,
  three staircase parts).

## Props fitted to their drawn size (v0.49.0, `city/PropFit.java`)

User choice 11 Oktober 2026 ("City prop overlap", roadmap worldgen item 4).
`tools/lctranslate.py` maps 1.20 furniture to Decimation props cell by
cell, but the models are not cell sized (docs/prop_placement.md) and much
1.20 furniture hung on walls and ceilings.
- Measured on seed 1 (fresh `servertest.py 1 type=decimation
  pregen=128,128,9`, then `tools/props/worlddump.py` + `propclash.py`):
  11009 props, 3609 findings (2901 with the corrected checker below), 93 %
  inside converted buildings: wood crates (drawn 1.38 long) into walls and
  each other 1065 + 94, hanging lamps turned floor lanterns in the air 389,
  wall shelves turned cardboard box stacks in the air 507, seats and screens
  facing walls, our own wrecks into sidewalks and hedges.
- `PropFit.fit(shape)` runs once per schematic in `LcContent.shape` (all
  DC pieces: buildings, street parts, scenes, fronts, parks), twice over
  so a prop sees its neighbour's change; log line "lc props fitted FILE:
  lights, ledges, turned, chests, faced, dropped, removed":
  - lantern with nothing under it: ceiling light (BlockLightOff) if a
    ceiling is above, else removed (wall lamp, chain link);
  - cardboard boxes with nothing under them: upper oak slab ledge (what
    stood on the shelf still stands);
  - other floating props drop up to 3 to a floor; clutter that cannot is
    removed;
  - seats / screens / machines (FRONTED) with the front against a wall and
    open space or a desk / counter behind: turned around; a bench facing a
    railing too; a machine taller than its room removed;
  - a prop drawn into a wall, a door (any touch), a slab, glass, a sink or
    another prop by more than 0.15: first facing that fits; a wood crate
    that fits none becomes a vanilla chest facing the room (Decimation loot
    too), never a third chest in a row; clutter (boxes, barrels, trash
    bags) that fits none is removed;
  - seats and clutter in a door's walkway are removed.
- Result, same seed and area: 30 findings (from 2901): about 14 wood
  crates in crate runs keep a 0.19 overhang (a chest there would make a
  run of three), 7 street lamp arms over a car or railing (the drawn box
  is the whole lamp, the arm hangs above; false positives), 5 chairs facing
  stairs / a bank counter fence, a few 0.2 to 0.3 overlaps with trapdoors.
  Per run of the area: 226 ceiling lights, 310 ledges, 168 turned, 462
  chests, 234 faced, 69 dropped, 58 removed.
- Dev test `cityprops` (devtest/CityPropsTest): generates a never
  generated city cell (from -Prx / -Prz, each run moves on), counts
  chests / ceiling lights / ledges and photographs examples (chest, light,
  ledge, chair, wreck); shots docs/shots/city_props_v0.49.0 (shots_index).
  Applies to chunks generated from now on; already generated chunks keep
  their props.

## Rivers and bridges (v0.45.0, v0.45.1, `city/CityRivers.java`, `city/RiverPlan.java`)

User choice 11 Oktober 2026 (after the railway): rivers allowed through
cities, streets crossing them become bridges. Since v0.45.1 (user review
of 0.45.0: "change the design so the bridge match with other road") a
bridge is the street itself carried over the water, no DeceasedCraft
bridge part. Lost
Cities builds a bridge where a level 0 street has to cross non city chunks
(water) to reach another street (BuildingInfo.calculateXBridge, bridges at
level 0 only); our cities had no such gaps (rivers kept 32 blocks off).
- Terrain (terrain/BiomeMap): with city rivers on, the river contour also
  runs through city sectors (river biome there) and keeps its 32 block
  clearance from military sectors only, so the natural river outside flows
  on into the city.
- On for worlds created since v0.45.0 only (CityRivers.onWorldLoad: a world
  that never ticked; stored in StructureData "!cityRivers"). An older world
  would get canals only in the chunks it generates from now on, beside city
  chunks built without them. `-Ddeciworldgen.cityrivers=true|false`
  overrides it for a session; dev tests use CityRivers.force.
- River chunk (CityRivers.chunk): a city chunk the contour touches at one of
  3 x 3 points (2, 8, 13). The middle point alone turned a diagonal river
  into chunks touching at their corners: ponds.
- City levels: a cell with a river chunk is level 0, and counts like a non
  city cell for its neighbours' levels (they ramp up from it).
- RiverPlan (FixedBase at BED = 56, above the railway tunnels): clay bed,
  water up to 62 (sea level, as the river outside), stone brick quay walls
  up to the street level on every side that meets land (a city chunk not on
  the river, or outside the city a chunk the natural river does not touch),
  iron railings on them. Building chunks on the river: canal, no building
  (multi buildings fit around them). Street chunks on the river (bridges
  and crossings): LcCity.street builds the same StreetPlan as on land
  (the district's street part by connections, paint, sidewalks, lamps,
  benches, wrecks; no stairs part, no street scene, no building fronts:
  StreetPlan onWater) and RiverPlan writes it at the street level over a
  stone brick deck at street - 1, two stone brick piers across the middle
  of a bridge (along 7 / 8, across 2, 3, 12, 13), four on a crossing.
  Iron bar railings on every chunk edge whose neighbour is not a street
  (water or a lot): at street + 1, or on top of a raised sidewalk (the DC
  street parts have slab sidewalks at street + 1, railing at street + 2).
  Wasteland district (style ":deadzone"): broken, the road's middle
  (across 5..10, along 4..11; a crossing 5..10 both ways) and the deck
  under it are open down to the water, sidewalks kept. Log line
  "lc bridge [crossing] [broken] at x,y,z". v0.45.0 used DeceasedCraft's
  bridge_1 / bridge_deadzone_1 parts (quartz deck of bottom slabs half a
  block below the street, white railings); the user wanted the road look.
- Railway: a station never on a segment touching the river (the next of the
  three segments, else none); tunnels pass under the canals (bed 56, tunnel
  top 54). tools/hwmap.py --rails ignores rivers and airports (a station moved off either is not modelled).
- Content: none since v0.45.1 (`tools/lcpack.py --rails` no longer exports
  bridge parts; an old "bridges" entry in a pack's index.json is ignored).
- Dev test `bridge`: switches city rivers on for the session, takes the first
  bridge (dev world from region 40 on row 6, `-Prx`, `-Prz`) whose chunk was
  never generated and whose span has city streets at both ends; checks the
  deck at the street level over every span chunk, water in every canal chunk
  around, nothing built over the canals, no water above 62 in street and
  canal chunks (buildings have their own pools: the sunken club holds 194
  water blocks), the road block (across 4, along 8) of every span chunk is
  the land street's, a player walks across from street to street; shots
  aerial, deck, side, under.
- Checked: seed 1 server world (pregen 20 chunks): 23 bridges logged, every
  generated one has its deck and water under it (the 3 without a road
  middle are the wasteland district's broken bridges); top down renders
  (docs/shots_index.md "lc_bridge_v0.45.0"). Dev test run 1 all PASS
  (picked a span at the city edge; its deck shot was inside the hill).
  Water at 64 / 65 in the spawn city: small fountain clusters in parks
  `[inferred]`, not canal water.
- Checked v0.45.1: seed 1 server world (pregen 20): 29 bridges logged, 3
  broken; layers read with worldcheck: bridge rows identical to the land
  street rows (asphalt, centre line, sidewalks), stone brick deck at 63,
  water 57..62, railings on the edges, the broken bridge at 120,64,136 open
  over across 5..10 / along 4..11. Dev test all PASS at 30144,64,1696
  (docs/shots_index.md "lc_bridge_v0.45.1").

## City edges end in T junctions (v0.61.1)
User, 14 Oktober 2026: no road running out of a city into empty land. StreetPlan.arms: a street chunk of our
own (no Lost Cities street part) closes each side whose neighbour chunk is neither a city street nor a highway
with sidewalk (a crossing becomes a T or an L, a street an end). District street parts already pick their
"t" / "end" parts from the same connections (LcCity.street). Highways keep their arm.

## City sizes (v0.60.0, `worldgen/CityLayout.java`, new worlds only)

User idea 13 Oktober 2026 (docs/ideas.md item 19): "city have variant size ... 8, 16, 24, 48 or even 64
district inside one city". Choices: sizes a little more even than first offered (each weight plus 15),
compact blobs, cities and roads less rare, 1 wasteland sector per 4 city sectors on the military side.
- Before: every 256 block sector rolled city alone (15 %), so a city was whatever city sectors touched:
  seed 1 had 1096 of 1509 cities of one sector (4 districts), the biggest 8 sectors.
- Now: the world is cut into cells of 5 x 5 sectors. Each cell holds one city (config `cityChance`, 1)
  grown as a compact blob inside its 4 x 4 corner: 1, 2, 4, 6, 12 or 16 sectors (4 to 64 districts),
  weights 30 35 40 35 28 22. The cell's last row and column stay free, so two cities never touch (not
  even at a corner). A city of 2+ sectors gets a military sector beside its farthest sector toward one
  side; its wasteland sectors (sector count / 4, at least 1) are the city sectors nearest that one (a
  breadth first walk from it). Other sectors roll wilderness / civilian / military in the old shares
  without the city one (40 / 25 / 20 of 85).
- Sectors.regionSector asks CityLayout first; Wasteland.sector takes the layout's wasteland sectors;
  Highways join cities up to 5 empty sectors apart here (maxGap(), 3 in older worlds); every cache of
  sector answers (Wasteland, Highways, CityNames, CityRivers, BiomeMap) carries CityLayout.epoch().
- New worlds only: StructureData `!citySizes` (CityLayoutEvents). The layout is ON by default because a
  new world's spawn search runs inside the World constructor, before WorldEvent.Load
  (MinecraftServer.loadAllWorlds posts it after; read in the Forge sources); an older world switches it
  off on load, unloading the overworld switches it back on. -Ddeciworldgen.citysizes=true|false.
- config/deciworldgen_cities.cfg: weight4Districts .. weight64Districts, cityChance. Changing them moves
  the cities of every world made with this layout (chunks already generated stay).
- Numbers without the game: `java -cp dev/build/classes/java/main net.decimation.worldgen.CityLayout SEED
  [RADIUS]`. Seed 1, 6400 cells: city land 24.3 % (was 15 %), sizes 4 / 8 / 16 / 24 / 48 / 64 districts
  15.8 / 18.8 / 21.0 / 18.0 / 15.0 / 11.4 %, wasteland 24.4 % of city land (1, 1, 1, 3, 4 sectors per
  city of 8 / 16 / 24 / 48 / 64; none in a 4 district town); RADIUS <= 8 prints a map.
- Checked: seed 1 server world (servertest pregen=1536,512,22): the 48 district city at x 1280 .. 2303,
  z 256 .. 1023 with its 3 wasteland sectors as an L on the military side (wasteland zones at 1280,256
  ...), fence and gates on their edge, streets joined, highways in from the west and north
  (docs/shots/city_sizes_v0.60/topdown_seed1_48districts.png). Live dev world (older): "city sizes off",
  dev test places PASS.
- Refuges (v0.60.2, user 13 Oktober 2026: "1 per 4 sectors"): round(sectors / 4), at least 1, so 1 / 1 / 1 /
  2 / 3 / 4 for cities of 4 / 8 / 16 / 24 / 48 / 64 districts, never in a wasteland sector, spread apart
  (LcCity.refugeSectors: the first nearest the city's middle, each next the sector farthest from those taken;
  a sector with no block that fits is skipped for the next). Older worlds keep one per city sector. Checked 17
  Oktober 2026 (gatescan on a fresh seed 1 server world): the 64 district city at x 1280 .. 2303, z -2560 .. -1537
  has 4, none in its 4 wasteland sectors (docs/safezone.md section 7). `gatescan` lists refuges since then.
- Not changed: rails per highway line. `tools/hwmap.py` and `tools/edgescan.py` follow the sized layout since 17
  Oktober 2026: `tools/citylayout.py` ports CityLayout and Sectors.regionSector bit for bit (java.util.Random, float
  rolls such as 0.65f, Java's %.1f rounding); checked against the Java classes: the CityLayout map and statistics of
  seeds 1, 2, 12345, -7 and 424242 identical, 171000 sectors and 6480 plans in far areas identical, hwmap's highway and
  rail kind of 5.2 million chunks identical (10 seeds, both layouts). `--old-layout` gives worlds made before v0.60;
  edgescan warns when the world's `!citySizes` flag says otherwise. With the sized layout L links nearly vanish (the
  isolation test reaches maxGap() + 1 = 6 sectors, more than a 5 sector cell).

## Verified (seed 1, Decimation world type, fresh server world)

119 converted buildings placed in the spawn area (102 current, 17
legacy), levels 64 and 70, no generator errors. Shots
`docs/shots/lc_city_v0.24/` (docs/shots_index.md): streets level with
building fronts, several stairs designs joining levels.

## User review

- 8 Oktober 2026, 0.24.4 tested in Prism: "i love it for oneshot
  progress". Next chosen: highways between cities (over the bridge,
  fronts / parks, giant buildings and interior options).

## Open

- City edge (v0.24.4, replaces the v0.24.1 band of 10): every city cell
  with open land among its 8 neighbours has an `EdgePlan` that ramps the
  land up to EDGE = 24 blocks out (smoothstep, Euclidean distance so outer
  corners are rounded, width 2 blocks per block of height difference,
  6..24, contours wobbled +-4 blocks by value noise so the 1 block steps
  do not run parallel to the street). A column belongs to the nearest city
  cell only (ties to the lower cell): no column is graded twice.
  `populate` now looks at cells within EDGE of the window (before, the
  outermost 2 band columns were never written). Small sites and large
  sites within EDGE of a city are dropped (the ramp would cut under them).
  `tools/edgescan.py WORLD [R]` measures it (sector map recomputed in
  Python): seed 1, 9024 band columns, neighbour steps over 1 block 1.2%
  (v0.24.3) -> 0.3% (v0.24.4); the 3 block steps left are natural hills
  and tree tops. It still reads as 1 block grass terraces from the street
  (shots lc_edge_v0.24.4b), which is plain Minecraft terrain.
  v0.55.0 (user: the edge "looks not natural when see from my view"): EdgePlan.wear turns the grass
  tops within WEAR = 10 of the city into broken pavement in patches, denser at the city (sidewalk slab
  and stone, then cobblestone, then mossy cobblestone). Coarse dirt was tried first: orange from
  above. Dev test `cityedge` (cell 16,-16): worn tops by distance 1..10 of 64: 62 53 41 26 14 7 1 0 0 0;
  shots docs/shots/v0.55_cityedge/. The terraces' grass sides stay.
- v0.56.0 (user 13 Oktober 2026, shots docs/shots/v0.56_city/): (1) the street stairs between city
  levels are an asphalt ramp (StreetPlan.rampAt: road blocks and Decimation's road slabs with the
  dashed line, 12 half block steps over the chunk, stone slab sidewalks, stone brick under it, iron
  railings once it stands 2 above the street; the dev test `cityedge` photographs one: ramp.png);
  DeceasedCraft's stairs parts are no longer drawn. (2) The wasteland's broken bridges are damaged
  naturally (RiverPlan.damage: per chunk a centre and size, a ragged hole to the water, road sunk to the
  deck round it, cracked cobblestone and stone brick fading out, 3 in 10 chunks cracks only), not a
  rectangle hole in every chunk. (3) Stations keep the street whole: the staircase stops under the
  street, no surface plaza, the stairs come out in the south sidewalk (Rails.entrance: the top step, a
  6 x 2 well, railings); the rail walk test reaches the platform from the street.
- Doors (v0.55.0, worldgen/Doors): the pack's vanilla doors (tools/lctranslate.py writes wooden / iron
  doors) become Decimation doors when Slices writes them, styled by building kind.
- Fronts, parks and street scenes since v0.26.0, district street parts
  since v0.27.0, highways since v0.25.0, the railway since v0.44.0, rivers
  through cities with bridges since v0.45.0 (new worlds).
- Superblocks (v0.24.2): 30% of aligned 2 x 2 city cell groups (all 4
  city) become one block: streets only on its first chunk row / column,
  7 x 7 building chunks, one level (the lowest of the 4), a landmark
  (multi building over 3 x 3 chunks) placed first at its corner. Seed 1:
  the courtyard office tower (4 x 4, 192 high). Still too big / tall:
  casino (276 high), oasis condo (top above 250 with its cellars),
  laboratory (90 deep cellars), terrace plaza 5 x 7 fits.
- Building rotation: only the rotation variants the data lists.
- Loot: chests became Decimation wood crates (loot works by block).

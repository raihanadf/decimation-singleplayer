# The wasteland district: the city's military area (v0.54.0 to v0.56.0)

User request (12 Oktober 2026, docs/ideas.md item 14, picked from the "what's next" list): "go
with 1, wasteland military area. do full system now". The user's claim from the idea list:
DeceasedCraft's wasteland area with its wasteland buildings is really a military zone, to be stocked
with military loot ("if you dont trust me you can check it up on deceased craft directly").

## 1. DeceasedCraft, checked (legacy 5.5.5 instance, read only)

Source: Prism instance `legacy`, `minecraft/kubejs/data/c70cities/lostcities/` (city data) and
`kubejs/data/lostcities/lostcities/conditions/chestloot.json` (339 rules: which loot table a chest
gets per building / part), loot tables in `kubejs/data/deceasedcraft/loot_tables/chests/`.

- The wasteland district is the legacy city style `deadzone` (docs/references/
  deceasedcraft_buildings.md "Legacy DeceasedCraft 5.5.5"). Its building weights in our pack (style
  factor x 10, at least 1, all rotations): military base 4, military camp 3, bunker 4 = 11 of 47
  (23 %); laboratory complex 11 (never placed here: 90 deep cellars), small lab 4; industry 28
  (warehouses, factories, car factory, oil derrick, pumpjack).
- Loot per building (chestloot.json): the military base parts have military tables
  (`military/armory_weapons` 93 items, 77 % guns and attachments; `armory_ammunitions`, `cabinet`
  (armour, medkits, MREs, night vision), `ground`, `medical`, `office`, `kitchen_*`, `power_room`);
  the military camp `military/ground` (helmets, vests, 5.56 / 7.62 ammo, HK416, M1014, AA-12); the
  bunker and the small lab tables with a few pistols, ammo and armour (2 to 6 % each); warehouses,
  factories, car factory, oil: industrial materials only (steel, plates, components, no weapons).
  One street scene (`fountain_deadzone_9`) holds `military/ground`.
- Verdict: partly right. The district holds DeceasedCraft's military base, military camp and
  bunker with military loot, but most of it is industry with industrial loot. Making the WHOLE
  district a military area (below) is our design, built on that.

## 2. What the district is now

The district itself is unchanged: the legacy deadzone style on the 2 x 2 cell squares within
DEADZONE_RANGE (2) cells of a military sector (LcCity.style). On top:

| Part | What | Where |
|---|---|---|
| Zone | One MILITARY zone over the whole district (128 x 128, y 0 to 255), added once per world when a chunk near it is populated (StructureData marker `!wz_<gx>_<gz>`); its buildings no longer carry their own boxes. Decimation then gives 25 % of the infected spawning there its military look and draws the zone on the GPS map (docs/zones.md section 3) | city/Wasteland.zone, LcCity.populate |
| Buildings | DeceasedCraft's military base and camp weigh x 3 in the district's pick, the bunker keeps its own weight (v0.54.1; v0.54.0 had base x 6, camp x 4, bunker x 2: 11 alike bunkers, then 6 alike bases). More open lots (25 % of the picks instead of 10 %, 75 % of them bare for the military scenes) | Wasteland.factor / weight / lotChance / bareShare in LcCity.plan / pick; NEW WORLDS ONLY (below) |
| Loot | The buildings' own containers (converted chests = Decimation wood crates, vanilla chests where a crate did not fit) become military loot by the building's own mix (v0.54.1): a lead item by kind (military: weapon cabinet, ammo case, medical or military crate; labs and clinics medical crates), random weights per building for that, military crates, medical crates and keeping the old crate (military buildings keep 15 to 55 in 100 parts, factories most). Front toward the room, only where it fits as the old one did; a tall cabinet or long case that does not fit becomes a 1 x 1 crate by the same weights. A looted building has lost a quarter of its containers, an overrun one some (never one with a crate on top) | BuildingDress.supplies |
| Supplies | Every non military building keeps part of the army's supplies (SUPPLY palette: military crate always, ammo cases, weapon cabinet, medical crate, radio desk, water pallets); military buildings 30 % denser and at least 4 pieces per storey (bunkers have small storeys), plus a US flag, a medical crate, the big radio, a stretcher; their palette's crate entry is no longer core (it added three more olive crates to every one) | BuildingDress.theme, storey, PALETTE[MILITARY] |
| Stories | Held and overrun: calm 10 %, looted 15 %, squatters 5 %, massacre 22 % (1 or 2 bodies), quarantine 20 %, barricade 28 % (sandbag walls two high beside the entrances, inside); body bags anywhere; "no trespassing" signs (outer walls 3 in 10 of the tags), Soviet symbols, biohazard / nuclear signs in labs | BuildingDress.theme, story, barricade, sandbags |
| Zone entries | Every street entering the district from another district (and 6 in 10 from outside the city): from the boundary in, a stop sign facing arriving drivers, hedgehogs, a concertina line, a jersey barrier line with a 2 wide gap in the inbound lane, sandbag walls two high on both sidewalks (frontage row open), a spotlight (or a flag pole), a jeep in the outbound lane nose out. Log "dress zone entry at x,z facing D" | StreetDress.entry |
| Streets | Road scenes: inner checkpoint 14 %, crash 16 % (a military jeep 4 in 10), a parked jeep or APC 15 %, a convoy of two jeeps 10 %. Sidewalks: barrels, skeletons, sandbags left on the curb, wire coils, a military crate (loot). Crossings: crash 25 %, a sandbag fighting position on one corner 35 % (military crate and ammo case inside, the walk round the corner open) | StreetDress.roadScene, militaryVehicles, straight, fightingPosition |
| Lots | Bare lots: motor pool and supply dump (rows of military crates, ammo cases, care packages, water pallets, spotlight) on a concrete pad (double stone slab, grass border), crash site, casualty collection point (body bags in rows, stretchers, medical crates, hazard light), or nothing; bushes cleared under every scene. Motor pool (v0.54.2): three bays along the street side (LotDress.streetSide), noses to the street, an APC in the middle bay half the time, a second line of jeeps behind in 6 of 10, a workshop row on the back edge (parts shelves, workbench, tyres, generator), a fuel point of drums in a back corner, a work light, a towed missile launcher where no second line stands. Crash site (v0.54.2): the helicopter (drawn 9.4 x 14, lopsided) tries every heading with a shifted anchor, burnt ground along its hull (stained clay black, dark brown, grey), its dead, a body bag, a military crate, an ammo case, a wheel, a cordon of sawhorses and cones (street side, else the hull's long sides) and a work light. Log "dress lot military SCENE at x,z" | LotDress.militaryLot, groundWork, motorPool, helicopter |
| NPCs | The district counts as a military area: bandits roll the heavier military weights, the enemy military spawner (Soviet groups of 2 to 3) runs for a player in it; in a city a group spawns only on the top block at street level (a street, yard, lot or park; never a roof or inside), each member on a free good cell of its own or on the group's spot (v0.54.1: v0.54.0 spread members one block round the spot, into a building's wall at a street chunk's edge, and asked for a street chunk, which a superblock district has only on its edge) | fixes/NpcLoadouts.military, MilitarySpawner, Wasteland.streetSpot |
| Locked doors (v0.55.0) | Most buildings locked, the richer the likelier: chance 700 + 30 per loot container per mille (military buildings and labs + 250, clinics and police + 120, at most 970), always from 20 loot containers. Every entrance (flood fill from the plot's edge at the door's storey reaches one side and not the other) gets Door_Emergency_1_Locked and a BlockKeycardScreenMilitary on the outer wall 1 or 2 beside it, 1 up, where the plot has an outdoor cell there. Opens with a military keycard on the screen (doors at offsets -3 .. +2 (KeycardReaderBlock; tools/props/keycardcheck.py)) or a lockpick. Log "wasteland doors NAME at x,z: locked n doors, k keycard screens, loot L" / "open" / "no entrance door" | BuildingDress.lockDoors, BuildingLayout.outdoors |
| Whole sector (v0.57.1) | New worlds only (StructureData `!wastelandSector`, `-Ddeciworldgen.wastesector`; dev tests: Wasteland.setSectors): a wasteland city is a whole city sector (256 x 256, its 4 districts all wasteland), picked for a city sector with a military sector on one of its 4 sides, half of them (Wasteland.sector: about 1 city sector in 3 on seed 1). No ring: it stands in its city, its streets joined to the neighbours'. Fence inside the city on the sector edge (WastelandFence.line / inCity, drawn by the edge street chunks: the outermost sidewalk cell of the edge street, chain link and posts, wire on top) with a gate on every street crossing it (posts at the road's edges, striped barriers on every second cell); on land the v0.56 fence and mines, the inside line carried out to meet it (lineOnLand). No city refuge in a wasteland sector | Wasteland.sector / deadCell, LcCity.style, WastelandFence, StreetPlan.blockAt, LcCity.refuge |
| City apart (v0.56.0, worlds of 0.56.0 .. 0.57.0 only) | New worlds only (StructureData `!wastelandCity`, `-Ddeciworldgen.wasteapart`; dev tests: Wasteland.setApart): the other districts' cells touching a wasteland district (8 neighbours) are open land (LcCity.isCity false, Wasteland.ring), so the wasteland stands alone, its districts side by side one city "Fort <name>: Wasteland - n" (CityNames.wasteland); ring districts get no name | Wasteland.ring / deadCell, LcCity.isCity, CityNames |
| Fence (v0.56.0) | On the land round the wasteland city (EdgePlan.grade, WastelandFence), by Chebyshev distance d to it: d 4 chain link (iron bars 3 high) between dark metal posts every 4 with barbed wire on top, d 5 barbed wire at the foot every second cell, d 6 to 16 Decimation's landmines (about 1 in 28 columns: entities "deci.Landmine", go off under a player, power 4, no block damage); every street running out of the district goes on through a gate (the street to d 9, striped barriers across the road at the fence between taller posts, a stone guard booth with windows and a metal door on the sidewalk inside: one = military post, both sides = toll gate) | WastelandFence, Deci.newLandmine |
| Bunker (v0.56.0) | DeceasedCraft's bunker (underground, a trapdoor hatch): a 5 x 5 stone brick blockhouse over the hatch with the locked door and a keycard screen; every container military, up to 8 more (weapon cabinets, ammo cases, military crates); a mob spawner cage breeding infected in the military outfit (Garrisons.onJoin within 6 blocks); 5 posts manned by juggernauts and elite military on the first visit (within 40 blocks), each post once its chunk is loaded, never despawning, each guard kept near its post by a home area (since v0.64.3, docs/airport.md section 12) | BuildingDress.bunker / blockhouse, worldgen/Garrisons, StructureData lists "!gar_<id>" |
| Name | Unchanged since v0.52.0: "City: Wasteland - n", controlled by Military | city/CityNames |

Crates are never stacked: a military crate is drawn 1.15 high, one on another overlaps it by 0.15
(propclash reports from 0.15).

### Old and new worlds
- The military building pick changes which building stands on a chunk, so it is on only for worlds
  created since v0.54 (StructureData `!militaryWasteland`, set when a world that never ticked loads;
  `-Ddeciworldgen.milwaste=true|false` overrides it for a session; dev tests switch it with
  `Wasteland.setLayout`). An older world would get buildings cut at the edge of its generated chunks.
- Everything else (zone, dressing, loot, NPC rules) applies to chunks generated from now on in any
  world; the NPC rules to the whole district at once (they follow from the seed).

## 3. Code map
- `city/Wasteland`: district test (`chunk`, `at`), the world flag, `weight`, `zone`, `streetSpot`.
- `city/LcCity`: `populate` calls Wasteland.zone per city cell; `pick` uses Wasteland.weight; district
  buildings get no zone of their own; `style` reads a cached style list (the NPC rules ask often).
- `city/BuildingDress`: `waste` flag (from BuildingPlan.style), `supplies`, SUPPLY, story weights,
  sandbag barricades, signs. `kind`: building_lab* is LAB (was read as an office before v0.54).
- `city/StreetDress`: `entry`, `militaryVehicles`, `fightingPosition`, `plain`, sidewalk clutter.
- `city/LotDress`: `militaryLot`, `groundWork`, `motorPool`, `supplyDump`, `helicopter`, `casualties`;
  `streetSide` (the lot side a city street runs along) and `ux` / `uz` / `faceUv` / `alongUv` (cells
  counted from that side: u along the street edge, v in from it);
  `force` (dev test only) with `Wasteland.previewLot` writes a lot with a chosen scene into the world.
- `fixes/NpcLoadouts.military` (public), `fixes/MilitarySpawner` (street spots, 16 tries).
- `ZoneStore.has` (dev tests), devtest `WastelandTest` (mode `wasteland`).

## 4. How to test
- Server (no game window): `python3 tools/servertest.py 1 type=decimation dress pregen=-64,192,12`
  (seed 1's wasteland districts lie around x -256..127, z 128..255). Log lines: "wasteland military
  zone", "dress zone entry", "dress military vehicle / convoy", "dress fighting position", "dress lot
  military", "wasteland loot NAME: n containers military, m kept", "dressed building ... wasteland".
- `python3 tools/props/dressbalance.py build/server.log`: wasteland buildings are their own rows (w).
- Blocks: `tools/props/worlddump.py` + `propclash.py` on the district, diff against v0.53;
  `tools/props/topdown.py` for the facing of the entry jeep, convoys, motor pools.
- Client (only with no game of the user running): `python3 tools/devtest.py --live wasteland`: picks
  a wasteland district never generated in deciworldgen_devtest (from cell -Prx / -Prz), checks the
  zone, the NPC rules, an enemy group spawning on the streets, the loot count, the entries; shots
  wasteland_<kind>_<n>.png and the aerial view; then the four military lot scenes forced side by side
  in the sky at x, z -21000, y 200 (Wasteland.previewLot; the shared arena had other tests' builds
  around it), each must place its key prop, shots wasteland_lot_<scene>.png.

## 5. Checked (12 Oktober 2026)
- Seed 1 server world (`servertest.py 1 type=decimation dress pregen=-64,192,12`, a new world: the
  military pick on): 3 district zones logged (-256..-129, -128..-1, 0..127 by z 128..255); 5 zone
  entries, 7 parked vehicles, 7 convoys, 2 fighting positions, 2 military lots; 208 containers
  swapped to military loot, 109 kept (where the new one did not fit, or the factories' half).
  Buildings of the districts: bunker 11, military camp 9, military base 3 (DeceasedCraft's own
  share would have been about 1 in 4 military; the first run with x 3 for all gave 23 bunkers and
  1 base, see lesson 6 of the decimation-city skill), the rest factories, warehouses, labs, oil.
- tools/props/dressbalance.py (rows (w) = wasteland): industry (w) 29 buildings, similarity 0.31,
  military crate 15 %; lab (w) 6, 0.53; military (w) 23, 0.65, military crates 37 % of their props.
  The military rows stay alike: 11 of them are the same DeceasedCraft bunker, whose six containers
  stand side by side against walls where only 1 x 1 props fit (the ammo case was refused 67 times
  for drawing into a wall), so the store's military crate wins. Whole run: 82 distinct props, mean
  similarity 0.42 (v0.53: 0.39 without the wasteland rows), top prop the military crate 8 %.
- tools/props/propclash.py on the dump (x -256..149, z 0..255): 40 findings, none from a prop the
  wasteland rules placed. Same box as the v0.53 check (x -242..149, z 0..178): 24 -> 20. Left:
  converted buildings' own props (stacked wood crates 0.19 to 0.30 into walls or each other, a
  lantern on a stair before a door), water in cellars, a skeleton whose sand floor fell (sand under
  a spot of the converted building), ceiling lights at the dump edge. Found and fixed on the way:
  sandbags of a fighting position in front of a traffic light's lenses (Dress now refuses a solid
  block in front of a prop's face; fighting positions take a corner whose square holds no prop),
  a barrier gap 4 wide (barriers now laid outward from the gap: 2 wide).
- Top down maps (tools/props/topdown.py): docs/shots/wasteland_v0.54/topdown_entry_0_240.png (entry
  facing south: jeep in the outbound lane nose south, stop sign facing the arriving traffic, spotlight
  looking out, barrier line at z 250), topdown_convoy.png (two jeeps nose to tail in the lane,
  facing its way), topdown_motorpool.png (one jeep fitted on that lot: the lot's uneven ground refuses
  the other bays), topdown_fighting_0_192.png (before the corner fix: the position took the traffic
  light's corner).
- Dev test `wasteland` (live, deciworldgen_devtest), run 2 all PASS: district 30,-4 (blocks
  1920,-256): zone on a street and in a building, NPC rules military there and not in the next
  district (dc:highrise_office), an enemy group of 2 spawned on the district's streets, 269
  military loot containers, 11 entry hedgehogs. Run 1 (district 36,12) failed only the spawn check:
  the test stood on the district's west edge (the group came up outside it) and counted Soviets
  already there; the test now stands at the middle crossing and counts only the new group. Shots:
  docs/shots/wasteland_v0.54/ (shots_index.md).
- Not verified in game by the user yet; not checked: a military building pick in a world created
  before v0.54 (stays off by the flag), the helicopter crash and casualty lots in game (none in the
  shot districts; the server run logged 2 motor pools).

### v0.61.1 user review of the gates (14 Oktober 2026)
User shots docs/shots/wasteland_gate_v0.61_review/: the land edge street ran out through a gate to nothing
("better use T intersection rather use 4 intersection since there is no road"; edge gates "blocker gate
... only 5% chance to be opened ... no road on it"), a connector left as the plain gate ("boring and no
taste"), the tank ditch ("really bad idea ... remove it"). Choices taken (asked): toll replaces only the
plain connectors (the 5 looks stay), a HESCO wall instead of the ditch, T junctions at EVERY city edge, the
edge gates shut with the same closures.
- Closures now: wire + landmines, welded steel, burnt bus, HESCO wall (our block 3 high, wire on top,
  sandbags and a crate in front). The ditch and the street base sinking are gone.
- Connectors that cannot take a full gate (the street before the line is a canal, a level change, or no
  street) get the COMPACT toll gate (look -1 in CityGate): a booth island on the line and the crossing's two
  rows, a canopy over both lanes, arms down on the far side, the closure in the line's lanes beside the
  booth (a bus becomes steel there). Gates are allowed on bridge decks (RiverPlan asks the street above
  the deck), so a crossing on a canal is no longer plain. Seed 1: 15 connectors, none plain.
- T junctions (StreetPlan.arms): our own street chunks (districts without Lost Cities street parts, the
  wasteland's among them) close every side with no city street or highway beyond it with sidewalk, so a
  crossing at a city edge becomes a T and a street running out ends; the district street parts already
  chose "t" / "end" parts by their connections.
- Edge gates (WastelandFence.landGate): no road; where a district street meets the land fence the fence
  holds a closure (1 in 20 a broken gap: bars bent to stubs, scorched ground, wire scraps, a skeleton, no
  mines on the way through); the stretch's ground is levelled to the street level first (a bus on the
  slope came out jagged); the wire version adds wire both sides and mines one cell in three outside.
  Wasteland cell ties go to the side the column lies straight out from (a gate's first columns took the
  next cell's corner and kept the plain fence).
- Checked after the user's push (14 Oktober 2026): `servertest.py 1 type=decimation gatescan=0,0,3000` (the gate
  decisions without generating, city/WastelandFence.scan): 188 connector gates, 21 % open (aim 20 %), 11
  compact tolls, no plain; 195 edge gates, 4 % open (aim 5 %). Generated and read back: the compact toll
  (open) at 136,70,-1024 (booth x 134..137 on the line and the crossing's rows, canopy, lamp, door on the far
  side, lanes clear, scorch, skeleton); the broken edge gate at -896,64,508 (stubs at x -893 / -884,
  scorched ground both sides, wire scrap, skeleton, the street ending in sidewalk at z 512); the wire gates
  at 1544,64,512 and 1536,64,520 saved 25 Decimation landmines in their chunks, all in the rows the canvas
  puts them. Dev test `gatewalk` (infected pushed down every lane with a 1.1 step, at night, line fence beside
  the gate): every shut toll and compact toll stopped 10 of 10 lanes (two runs); both open ones let 6 of 10
  through (the two traffic lanes; the booth island blocks the middle). Test traps found on the way: infected
  burn in daylight in the sky; a sky gate without the line's fence beside it lets bodies walk round its end.
  Since 17 Oktober 2026 (v0.64.4) `gatewalk` builds every look (compact toll, toll, checkpoint, containers,
  quarantine, police) in every state, the line's sidewalk fence as the real one (bars 3 high, wire or a post on
  top), bodies that never despawn (the corner gates are about 128 blocks from the player), and asks vanilla's path
  finder for a way from the approach to 4 past the line within 18 blocks: open gates need one (the containers' way
  bends round a container, so its pushed lanes are only noted), shut gates none and 0 of 10 pushed through. Found:
  the checkpoint with the HESCO closure had a stair over the line (the closure's 1 high sandbag beside the
  checkpoint's 2 high sandbag position, then the 3 high fence, whose wire on top does not stop walking). Fixed by
  moving the position one row off the line (spotlight and flag back too, 0 prop clashes); 55 of 55 checks PASS.
- Checked: seed 1 server (pregen 1472,448,18): 15 connector gates, no plain; west land edge: the crossing's
  arm toward the land is sidewalk (T); the edge gates at z 320 and 448 are buses level at y 65..67 across
  z 323..332 with posts at both ends on the fence line.

### v0.61.0 gates between a wasteland city and its city (14 Oktober 2026)
(History: v0.61.1 above removed the tank ditch and the base sinking, and changed the canvas to H 9 without the
SINK rows; the lines below describe v0.61.0 as built then.)
User on 0.60.2: the fence "actually very good", but the gates should be a toll gate and different
styles, "around 6 gate style"; then "80% is blocked so player or mobs cannot pass it ... 20% where the gate is
opened even its half opened cause its has breaked before"; the closures: "version one using landmine, other
version can use traps". Choices taken (asked with options): 8 in 10 shut, 2 in 10 broken open; looks toll,
checkpoint, containers, quarantine, police; closures barbed wire + landmines, welded steel wall, vehicle plug,
tank ditch; an open gate keeps its look, smashed; up to 8 blocks each side.
- Code: military/CityGate (canvas: looks drawn y 0 = street surface, copied into the final canvas W 16 x H 13
  x L 11 with SINK 4 rows of ground below and the crossing's two road rows d -1 / -2), CityGatePlan (the
  canvas turned onto the line: turns 1 for a line along z, 2 along x), city/WastelandFence.gate (which
  columns a gate owns: the street chunk's 8 columns before the line, the line's road cells, the crossing's
  first 2 road columns; look and state by the crossing's hash), StreetPlan (asks the gate first; sinks the
  street chunk's base 4 for a ditch), StreetDress (no checkpoint / road scene in a gate's street chunk),
  Slices (CityGate.MINE marker cells spawn Decimation's landmine), BlockBoomArm (deciworldgen:boom_barrier,
  our thin striped arm; textures by tools/props/boom_textures.py).
- Looks: toll (island booth, canopy on 4 columns over 2 lanes, one arm down, one raised, red lamp, posts,
  sign), checkpoint (staggered jersey rows, sandbag position, camo guard tower with ladder, searchlight,
  flag, hedgehogs), containers (two stacks 2 high with a 3 wide gap, wire on top, a third container behind,
  work lights), quarantine (white tent over the road with strip curtains, medical crates, stretcher, body
  bags, generator), police (barriers, cruiser across, cones, small blue booth with a beacon).
- Closures in v0.61.0 (all at least 3 high or uncrossable: Decimation's mines only go off under players, props like
  wire do not stop mobs): wire (the chain link carried across the road, wire rolls both sides, about one
  mine per 3 road cells before it, 12 per gate in the test), steel (plates 3 high, ragged top, sandbags,
  warning sign), bus (a burnt bus of clay blocks 10 x 3 x 3 across the line and the crossing's 2 rows,
  windows one block high: nobody fits through), ditch (whole street 4 deep and 4 wide right before the line,
  hedgehogs and wire at the bottom and on both lips; replaces the look). Open: the look with the lowered
  arm gone, scorched road, a skeleton, a body bag, a knocked barrier, wire at the line ends.
- Not every crossing gets one: a river chunk, a level change or a non street chunk before the line keeps the
  plain gate (posts and striped barriers, open).
- Fence fix in the same version (user shot docs/shots/wasteland_gate_v0.60.2_review/user_fence_float.png:
  posts over a half slab): the inside fence stands on the last full block and replaces slabs, hedges and
  railings above it (StreetPlan.floor); it now also runs over bridges (no gap over a canal).
- Test: dev test `gates` (all 21 looks x states in the sky at -20000, 230, -19100, clash checks, mines
  spawned, shots); `servertest.py 1 type=decimation pregen=1664,512,14` logs "lc gate LOOK/STATE at x,y,z".
- Checked (14 Oktober 2026): dev test gates all 21 PASS (0 clashes, 10 to 16 mines per wire gate); seed 1
  server, the 48 district city's wasteland edges: 11 gates (3 ditch, 2 bus, 3 steel, 3 wire, none open in
  this sample [inferred: chance, 2 in 10 expected]), no errors. Region files: the ditch at 1736,64,512 is dug
  z 508..511 down to y 61 with wire at the bottom and on both lips; the bus at 1792,64,456 spans x 1792..1794,
  z 451..460, y 65..67 (the crossing's lanes from x 1795 stay free). Mines in a generated world, an open gate
  in a generated world and walking into a closure in game: [not verified] (sky test only).

### v0.54.1 (12 Oktober 2026, the weak spots of v0.54.0, user: "do weak spot task now")
- Seed 1 server (same area): military buildings 23 -> 10 (bases 3, camps 4, bunkers 3; was 11 alike
  bunkers); military lots 2 -> 8 (4 motor pools, 2 supply dumps, 1 helicopter crash, 1 empty).
- dressbalance military (w): military crates 37 -> 27 % of their props, medical crates 21 %; the
  similarity of their prop counts stays 0.75 because the loot crates are their top items in every one
  (a military area keeps military loot); without the loot containers (and EXIT / CCTV) the military
  buildings are 0.17 alike, labs 0.30, factories 0.19. Mean of all kinds 0.42 -> 0.38.
- propclash, same box: 40 -> 31 findings; one with a wasteland prop: a large ammo case in a base cellar
  at -78,41,216 beside lava that came in from a cave after generation [inferred].
- Dev test `wasteland` (live): district 30,6 counted 0 of 3 group members on the streets (members
  spread off the street chunk; the spot check is per member now), 40,-10 (a superblock) found no spot
  at all (fixed: any open spot at street level); after both fixes all PASS on districts 32,24, 44,24
  and 58,18, an enemy group of 2 or 3 at street level in the district each time; the four forced lot
  scenes place their key props (motor pool 2 jeeps, supply dump 12 to 16 military crates, the
  helicopter, 10 to 12 body bags). Shots docs/shots/wasteland_v0.54.1/ (shots_index.md).
- Found on the way: gravel ground (first try for the lots) fell in the sky preview as soon as block
  updates reached it: lots now get a concrete pad (falling blocks over a cellar or cave would open
  holes in a real city too). A magenta line shows along the west edge of the floating preview lot
  (a side texture of its grass border seen from the air) [not verified]; real lots have no exposed
  side.
- Not seen in game yet: casualty lots from generation (only the forced preview); the user's look.

### v0.54.2 (13 Oktober 2026, user: "lets continue fixing military")
- Weak spots from my own v0.54.1 shots (docs/shots/wasteland_v0.54.1/lots_sheet.png): the motor pool
  was two jeeps in the middle of an empty pad; the crashed helicopter stood on clean grass and read as
  parked. Both reworked (table above). A first try with podzol for the torn ground read orange from
  above (the colour the user rejected for base ground): stained clay instead. A second try put
  cobblestone slab lumps beside the hull: propclash counted them inside the helicopter, whose drawn box
  is the whole rotor disc, so no lumps; for the same reason the cordon falls back to the hull's long
  sides, where the disc leaves 3 cells.
- Seed 1 server (`servertest.py 1 type=decimation dress pregen=-64,192,12`), same 8 military lots: 4
  motor pools (3 with a second line, 6 jeeps each; 1 with the missile launcher and a 3 jeep line),
  noses toward the lot's street side in all of them (tools/props/topdown.py, docs/shots/wasteland_v0.54.2/
  topdown_lots.png); the crash site with its burnt strip and the cordon on its two long sides.
  propclash on x -256..149, z 0..255: 31 findings, as v0.54.1, 0 inside a military lot.
- Dev test `wasteland` (live): all PASS on districts 18,24, 48,-16 and 18,30; the forced lot shots
  show the new scenes (wasteland_lot_motorpool / _helicopter). A generated crash site in district
  18,24 (lot 1232,1616) checked from the region file: burnt cells along the hull, cordon on its west
  side.
- The magenta line along the floating preview lot (v0.54.1) is magenta wool of another test's floor at
  y 199 under the lot's west edge (region file), not a missing texture.
- Not seen yet: a casualty lot from generation (still none in seed 1's 8 military lots, 17 % each
  [inferred: chance]); the user's look.

### v0.55.0 (13 Oktober 2026, user: "for wasteland, 80% most building its locked door, so if the building has more loots + high value in there, you put locked door decimation in there")
- Seed 1 server, same area: of the 52 wasteland buildings, 35 have an entrance door: 27 locked (77 %),
  8 open; 17 have no entrance door (open doorways, or doors only inside) and cannot be locked.
  49 locked doors, 48 keycard screens; 0 vanilla doors (all doors Decimation's since v0.55.0,
  worldgen/Doors). propclash 29 findings, none from a door or a screen.
- First entrance test (one side of the door under open sky, as barricade() asks) found entrances in
  only 18 buildings: doors under canopies, earth cover or on the plot's edge were missed. The flood fill
  from the plot's edge (BuildingLayout.outdoors) found 35.
- Dev test `wasteland` (live, district 20,-20): 23 locked doors, 22 screens, 0 vanilla doors, all PASS.
  Shots docs/shots/v0.55_locked/: the locked door says "This door needs a lockpick", the screen
  "Right click to use a key card". Opening with a keycard in game: not tried [not verified].
- Ties to the crate levels idea (docs/ideas.md item 17): higher level crates behind the locked doors.



### v0.57.1 (13 Oktober 2026, user review of v0.56: "why the wasteland sector is not connected with other city road? ... now i wanted you to make 1 district full of wasteland sector on 4 big chunk inside it, not splited like in that photo!! thas absurd"; then "wastelanda is spilited, so its his own city so later it will like city full of wasteland, not 1 city have 1 sector of wasteland and otjer sector of resident or high building")
- What went wrong in v0.56: "split out the wasteland city with other city" was read as "cut it out
  of its city": the 8 cells round each wasteland district became grass, so the city around it had holes
  and its roads stopped at the grass (user shots docs/shots/v0.56_user_review/user_ring_1.png, _2).
  Meant: the wasteland is a city of its own made of wasteland only, not one wasteland district among
  residential and high rise ones.
- Now (new worlds): whole wasteland city sectors, joined to the city beside them through gates (table
  above). User choices: fence on the district edge with gates (asked); the count: a whole sector.
- Seed 1 server (fresh, pregen round -128,128): the sector x -256 .. -1, z 0 .. 255 is a wasteland city
  (4 military zones), flush against the spawn city at x 0; the fence line along x 0 on the spawn city
  street's west sidewalk, a gate on each of its 4 crossing streets, the wasteland streets running into
  the city's; the land fence on the other three sides with its gates; corners closed (the inside line
  carried 4 out to the land fence). The refuge first landed in the wasteland (-112,144): refuges now
  skip wasteland sectors (one left, 80,144 in the spawn city). 8 bunkers in the 4 districts.
  Shots: docs/shots/v0.57.1_wasteland_sector/ (tools/topdown_blocks.py).
- Dev test `wasteland` (live, sector mode forced in the older dev world): all PASS; the fence check no
  longer needs mines (a sector inside a city has none).
- Not checked: a wasteland sector with city on several sides (seed 1's has one), its look in game by
  the user; the user's own world (created in 0.56) keeps the ring; a new world shows the new layout.

### v0.56.0 (13 Oktober 2026, user: bunker "highest loot ... most harder place ... elite npc + juggernaut and full military outfit zombie spawner"; "split out the wasteland city with other city"; "full covered fence ... barbed wire and full of landmine"; "toll gate and military post gate")
- Seed 1 server (fresh): the three wasteland districts stand alone in open land (docs/shots/
  v0.56_wasteland_city/topdown_seed1.png), "wasteland city apart on"; the fence round them (iron bars,
  wire on top: about 2 000 wire props, 1 100 iron bars, 1 300 post blocks), gates at the streets running
  out (topdown_gate.png), 380 landmines saved in the chunks; 3 bunkers each with "blockhouse locked,
  keycard screen yes, armory 5 to 8, spawners 1, posts 5" and a garrison registered.
- Dev test `wasteland` (live; the dev world is older, so the session turns the ring on): all PASS on
  district 28,34: bunker manned with 5 NPCs, the infected beside its spawner in the military outfit
  (1 tried: the other cells were taken), fence 242 wire on bars and 164 mines round the district.
  District 12,22 had no bunker (a random pick: the check now accepts none). Shots: blockhouse (concrete
  hut, red brown locked door, green keycard screen), garrison (elites with night vision in a bunker
  room; flames are particles: the save holds no fire block), fence, gate.
- The first bunker check found "no entrance door" for all 3 bunkers: the bunker is entered by a hatch
  and a ladder (layers read from the world), hence the blockhouse.
- Not measured: the fps cost of the barbed wire props (about 2 000 per wasteland city; small props draw
  only within 24 blocks since v0.38.2) [not verified]; opening the locked door with a keycard in game.

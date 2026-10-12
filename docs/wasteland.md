# The wasteland district: the city's military area (v0.54.0)

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
| Buildings | DeceasedCraft's military buildings weigh more in the district's pick: the base x 6 (3 x 2 chunks, fits only an empty block corner), the camp x 4, the bunker x 2 (1 x 1, fits anywhere): about 40 % of its buildings | Wasteland.factor / weight in LcCity.pick; NEW WORLDS ONLY (below) |
| Loot | The buildings' own containers (converted chests = Decimation wood crates, vanilla chests where a crate did not fit) become military loot, from a store type per building (military buildings: armory, ammunition store, aid station or supply room; labs and clinics medical crates; others military crates with ammo cases or medical crates). Military buildings all, factories and warehouses half, the rest 7 in 10; front toward the room, only where it fits as the old one did (the store's other items, then a military crate) | BuildingDress.supplies |
| Supplies | Every non military building keeps part of the army's supplies (SUPPLY palette: military crate always, ammo cases, weapon cabinet, medical crate, radio desk, water pallets); military buildings 30 % denser, plus a US flag, a medical crate, the big radio, a stretcher | BuildingDress.theme, PALETTE[MILITARY] |
| Stories | Held and overrun: calm 10 %, looted 15 %, squatters 5 %, massacre 22 % (1 or 2 bodies), quarantine 20 %, barricade 28 % (sandbag walls two high beside the entrances, inside); body bags anywhere; "no trespassing" signs (outer walls 3 in 10 of the tags), Soviet symbols, biohazard / nuclear signs in labs | BuildingDress.theme, story, barricade, sandbags |
| Zone entries | Every street entering the district from another district (and 6 in 10 from outside the city): from the boundary in, a stop sign facing arriving drivers, hedgehogs, a concertina line, a jersey barrier line with a 2 wide gap in the inbound lane, sandbag walls two high on both sidewalks (frontage row open), a spotlight (or a flag pole), a jeep in the outbound lane nose out. Log "dress zone entry at x,z facing D" | StreetDress.entry |
| Streets | Road scenes: inner checkpoint 14 %, crash 16 % (a military jeep 4 in 10), a parked jeep or APC 15 %, a convoy of two jeeps 10 %. Sidewalks: barrels, skeletons, sandbags left on the curb, wire coils, a military crate (loot). Crossings: crash 25 %, a sandbag fighting position on one corner 35 % (military crate and ammo case inside, the walk round the corner open) | StreetDress.roadScene, militaryVehicles, straight, fightingPosition |
| Lots | Bare lots (35 % of open lots): motor pool (jeeps and an APC side by side, fuel and spares behind), supply dump (rows of military crates, ammo cases, care packages, water pallets, spotlight), crashed helicopter with its dead, casualty collection point (body bags in rows, stretchers, medical crates, hazard light), or nothing. Log "dress lot military SCENE at x,z" | LotDress.militaryLot |
| NPCs | The district counts as a military area: bandits roll the heavier military weights, the enemy military spawner (Soviet groups of 2 to 3) runs for a player in it; in a city a group spawns only on a street chunk at street level (never on a roof or inside) | fixes/NpcLoadouts.military, MilitarySpawner, Wasteland.streetSpot |
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
- `city/LotDress`: `militaryLot`, `motorPool`, `supplyDump`, `helicopter`, `casualties`.
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
  wasteland_<kind>_<n>.png and the aerial view.

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

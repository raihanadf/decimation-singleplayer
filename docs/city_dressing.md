# City dressing: Decimation props through the whole city (v0.50.0)

User request 11 Oktober 2026: "do that for all city until its fully really like real world full of
props, check small detail ... make sure all props and all decoration decimation all placed on city
and put properly ... dont forget the direction ... like ready to be open to public".

References: docs/references/city_dressing_research.md (174 real world placement facts with sources,
each fact checked by a second agent; 34 corrected), docs/references/prop_facing.tsv (front, mount,
long axis of every prop per metadata, two independent readers + judge), docs/prop_placement.md (drawn
sizes, rules), docs/city_engine.md (the city the dressing sits on).

## 1. Principles

1. DIRECTION FIRST. A rule never writes a metadata number. It says where the front looks (toward
   the road, toward the buildings, toward the approaching traffic, into the room, along the lane) and
   `PropFacing.meta(block, dir)` / `PropFacing.along(block, axis)` turns that into metadata from the
   verified table. A prop missing from the table, or with an unresolved facing, is not placed.
2. FIT FIRST. Every prop goes in through `Dress.place`: its drawn box clear of blocks and props, inside
   its own plan (no box into the next chunk), standing on something (a half slab under it is made whole),
   room above tall props, never in a door's walkway. A refused prop is counted, never forced.
3. Real layout, then decay. The fixed furniture stands where a real street has it, in its real facing
   (research "Abandoned layer: the fixed street furniture stays IN PLACE"); the story (wrecks, bags,
   bodies, blood) is added on top in scenes that explain it, never as random noise.
4. Leave a way through: a sidewalk keeps at least 2 clear blocks along its length (1 on 3 wide ones),
   a room keeps its walkways, every door its walkway (walkcheck).
5. Deterministic per place (seed + position): the same world seed gives the same city.

## 2. Streets (StreetDress, skill decimation-city-street)

Sidewalk bands across the width (research "Street facing ground floors"): FRONTAGE (the row against
the building), CLEAR path (never furnished), FURNITURE (the curb row). StreetLayout reads them from
the part's own blocks (curb = walk cell next to road).

| what | where | facing |
|---|---|---|
| traffic lights (BlockTrafficLights) | crossings of two city streets: one pole per corner on the far right corner of each approach, 2nd sidewalk cell from the curb, past the crosswalk | arm over the approach, lenses toward the approaching traffic |
| stop sign (BlockRoadsignStop) | crossings without lights (residential, legacy towns): right curb of each approach, 1 row behind the crosswalk | face the approaching traffic |
| speed limit (BlockRoadsign20..60) | first straight chunk after a crossing, right curb | face the traffic it serves |
| one way / parking signs | one way streets / every 2..5 chunks of curb | face traffic / along the curb |
| street light (BlockStreetLight) | one side of a street, about every 64 blocks, slid to a party wall line, never within 15 of a traffic light pole; DC parts already have their own lamps: none added where the part has one | arm toward the road |
| mailbox | 1 per crossing (one corner, curb row past the corner band), shops, police | door toward the buildings |
| newspaper boxes (NewsStand1 / 2) | groups of 2..4 in the curb row, downtown, near crossings | window toward the buildings |
| phone (Phonebooth / Phonebox) | 5 wide sidewalks, transit groups, curb row | toward the buildings |
| bench (StreetBench) | curb row facing the buildings, or frontage facing the road; bus stops face the road | as stated |
| bins (StreetBin, Trashcan) | one per corner cluster, by benches and bus stops | symmetric |
| bicycles | parallel to the curb on 3 wide walks, 2 apart; clusters at schools / stations | long axis along the curb |
| power poles + electric boxes | residential districts, one side, every 2 chunks | crossarm across the street; boxes on the road side |
| electric cabinet (ElectricBoxBin) | one per signalised crossing, curb row of the minor street | door toward the buildings |
| dumpster | backs and sides of shops / restaurants, 2 cells off the wall | lid edge away from the wall |

Road: parked wrecks in the parking lane (aligned with the curb), jams toward the city exit on
arteries, crash pairs in some crossings with a debris cone, police / military checkpoints at a few
city entries (barrier funnel, chicane, cones, police cars across the lane), cone tapers before
blocked spots. Density per chunk: at most 1 hero scene, 2..3 small clusters, 1 chunk in 3 quiet.

## 3. Buildings (BuildingDress, skill decimation-city-interior)

Per building kind (from its name: residential, office, shop kinds, food, medical, police, industry,
military, lab, civic) per storey, on BuildingLayout spots:
- wall props: EXIT light over each exit door inside, CCTV above shop / office / police entrances,
  electrical cabinet per storey on a corridor wall with its front cell free, fire alarm bells in
  corridors, keycard screens beside lab / police / military doors that are locked;
- room props by kind: flatscreen TVs in living rooms and lobbies (screen into the room), vending
  machines in lobbies / break rooms (back to the wall), washing machines in flats, shop displays in
  shops, metal shelves in storage / hardware, medical crates and stretchers in clinics, police crates
  in police buildings, weapon cabinets in gun stores / police / military;
- story layer: looted look (boxes by doors, open crates with spill), a few bodies (skeletons, body
  bags in medical / police / military), blood decals near them, graffiti and notes on walls, trash bags.

## 4. Decay and decals (skill decimation-city-decay)

Graffiti on facades (street level, 1..3 per street run, never over windows or doors), blood decals
only where a scene explains them, warning decals by their hazard (biohazard at clinics, chemical
storage in industry, CCTV warning by cameras, watch your step at stairs).

## 5. Checks (all must pass before a release)

1. propclash on a seed 1 world dump: no new findings from dressed props (tools/props/worlddump.py).
2. Facing check (tools/props/propclash.py FACE + the semantic checks: signs face traffic, street
   furniture faces its side, wall props have their wall behind).
3. walkcheck on dressed streets and buildings: every door, room and loot container reachable.
4. Dev test `cityprops` / a city tour: pictures of every context, reviewed.
5. Performance: tile entities per chunk (props are models) within the budget measured on the
   current city (docs/performance.md), fps on a dense street.

## 6. Status and handoff (11 Oktober 2026, session paused at the user's usage limit)

WORK IN PROGRESS, behind a switch: `Dress.ON` (`-Ddeciworldgen.dress=true`); off by default, so
generation is unchanged. Nothing of it has run in a world yet.

Done (compiles):
- city/PropGeom (shared drawn box / shape / overlap, PropFit uses it), city/Dress (overlay +
  place() with every fit rule; props replace carpet / plates / plants; half slab under a prop made
  whole), city/StreetLayout (road / walk / busy per column, curb, direction to road),
  city/BuildingLayout (stand spots, walls per side, room height, doors, doorways, sky; cached per
  file in LcContent.layout), city/PropFacing (reads assets/deciworldgen/prop_facing.tsv; meta for a
  front direction, meta for a long axis), StreetPlan / BuildingPlan read the overlay first.
- city/StreetDress rules: crossings (traffic light per corner, lenses to the approach served:
  NE->S, SE->W, SW->N, NW->E; signal cabinet; else stop signs on the near right corner facing the
  approach), straight chunks (speed sign, corner cluster mailbox / newspaper boxes / bin, phone,
  bench + bin, bicycle along the curb, power poles + boxes one side in residential, trash bag
  clusters, wasteland barrels / skeletons). District groups by style: downtown (office, hotel,
  retail, legacy standardcity), residential, wasteland (deadzone).
- city/BuildingDress rules: building kind by name; EXIT light over doors inside, CCTV at
  entrances; per storey a budget of kind props against walls; story (skeleton + blood, body bags,
  trash, graffiti / warning decals). Decals: registry names deci:decal.<name>; blood splats draw
  2 x 2, the US flag 3 x 3 centred on their cell (hang() checks the wall behind the whole square);
  decal.notegeneric (floating "information" icon) and decal.imagegeneric (downloads a web image)
  are never used.
- docs/references/city_dressing_research.md (174 facts, sources, checks).
- tools/props/facing_table.py (workflow result -> docs/references/prop_facing.tsv + game
  resource), propclash.py reads that table for FACE.

Pending, in order:
1. Facing audit result: workflow run wf_8c4d2f56-6d8 (decimation-prop-facing-audit). Its journal:
   ~/.claude/projects/-Users-ucilmenangis-Projects-Decimation/<session>/subagents/workflows/
   wf_8c4d2f56-6d8/journal.jsonl (one result per reader). If the run finished, its output is the
   {merged, judged} object: `python3 tools/props/facing_table.py OUTPUT.json`. If not, resume it
   with Workflow({scriptPath, resumeFromRunId: "wf_8c4d2f56-6d8"}) (finished readers replay from
   cache). Early reader answers (not final): traffic lights front N E S W at meta 2..5 (NOT the
   placer rule), road signs W N E S, cars / trucks / police cars headlights W N E S, bicycle W N E S,
   mailbox / newspaper box / bench / street light / dumpster / cabinet E S W N, CCTV is a ceiling
   prop, flat TV and phone booth hang on walls, decals and graffiti E S W N (meta 3 on the north wall).
   DONE 11 Oktober 2026: the audit finished (41 agents, 192 props: 165 both readers agreed, 27 judged,
   0 unresolved); raw result docs/references/prop_facing_audit.json, table written by
   tools/props/facing_table.py to docs/references/prop_facing.tsv and the game resource (190 rows,
   low confidence left out). PICTURE CHECK before using: the judge gave road signs 20 / 30 / 40 / 50 /
   parking / stop front E S W N but 60 / one way W N E S (same family, opposite: suspicious); body
   bags black / orange E S W N, blue / green W N E S; work light tower S S N N; generator S W N E.
   Place one of each in the gallery and photograph it before the dressing uses them.
   Traffic lights (both readers agree): lenses N E S W at meta 2..5, arm E S W N (lenses 90 degrees
   left of the arm); StreetDress's corner table matches (NE pole lenses S = meta 4, arm W).
2. Check StreetDress / BuildingDress against the final table (front vs arm of the traffic light,
   power pole crossarm axis, CCTV facing table looked odd: N S S N).
3. servertest seed 1 with -Ddeciworldgen.dress=true, worlddump + propclash (no new findings),
   walkcheck, counts per prop; dev test cityprops extended to street / interior shots; read them.
4. Performance: tile entities per chunk before / after (docs/performance.md).
5. Skills: decimation-city-street, -vehicles, -interior, -decay, decimation-military-interior,
   decimation-worldgen index (user agreed the tree 11 Oktober 2026); roadway scenes (jams, crashes,
   checkpoints) still to write.

### 12 Oktober 2026: first working version (still behind the switch)

- Facing picture check (dev test `facing`, shots docs/shots/facing_v0.50): road signs all E S W N,
  body bags all E S W N; corrections in docs/references/prop_facing_overrides.tsv (applied by
  tools/props/facing_table.py, rows marked "in game").
- Rules fixed against the table: shop display front is its closed panel (shelves face the room:
  front toward the wall); the payphone cabinet hangs on a wall (lobbies only, not the curb); the
  small junction box pole is a wall prop (not on the curb); the tall phone box has no verified
  front (not used); CCTV only looks north or south (east / west doors get none).
- Fit fixes found by checking a dressed seed 1 world (servertest dress, worlddump, propclash,
  diff against the undressed world): thin props (a flat TV is 0.125 deep) turned inside out when
  shrunk by 0.08 a side and passed every check (PropGeom.shrink keeps 0.04); decals and graffiti
  are solid to other props; liquids, beds solid; skip ("keep the world") cells solid; floors are
  real tops only (open trapdoors, wire are not); a front never looks into anything over 0.6 high
  with something above it (as propclash FACE); long base props (cars) are checked 5 around; the
  threshold sits just under propclash's 0.15. Converted ceiling lights hung from nothing move up
  to the ceiling or go (PropFit). Street layout: flush sidewalks (suburb parts: paving and grass on
  a dirt road) found by the chunk's own road block; props slide up to 3 along the curb to fit.
- Result on seed 1 (pregen 128,128,9): undressed 8406 props / 39 findings, dressed 13216 props /
  50 findings of which ONE is caused by the dressing (a cabinet into water, fixed since: liquids).
  Totals per prop and refusal reasons: log line "dress totals after N plans" (Dress.logTotals).
- Still to do: road scenes (jams toward exits, crashes, checkpoints, cone tapers), facade graffiti
  and shop signs (the wall is in the neighbour plan), parks / lots, power poles are rare (13),
  performance check (props per chunk about +60 %), then the switch on by default, skills.

### 12 Oktober 2026: ON by default (v0.50.0)

- `Dress.ON` defaults to on; `-Ddeciworldgen.dress=false` switches it off (servertest `dress`,
  devtest `--dress` are no longer needed but harmless). New chunks only: places already generated
  keep what they have; a building split between old and new chunks is dressed in its new part only.
- Road scenes added (StreetDress.roadScene): police roadblock (car across the road, sawhorses,
  cones), wasteland checkpoint (jersey barriers with a gap, hedgehogs, sandbags), crash (car in its
  lane headlights the lane's way, a second car across it, debris downstream, a skeleton in the
  wasteland), road works (sign trailer facing the traffic, cone taper, sawhorse). Seed 1: 0
  findings from them. Shots docs/shots/city_dressing_v0.50/cell132_*.
- Performance: dev test cityview on a dressed street vs an undressed one: fps varies more between
  repeats (47..89) than between props drawn / not drawn; props within render distance about the
  same (3155..3603) because the dressed cell is a small part of the render area. A fully dressed
  area has about 57 % more props per chunk [not verified in a whole dressed city].
- Skills: decimation-city-street, -vehicles, -interior, -decay, decimation-worldgen (index);
  decimation-props and decimation-city updated. decimation-military-interior still to split.
- Still open: facade graffiti and shop signs (the wall is in the neighbour plan), parks and lots,
  traffic jams toward city exits, crossing crashes, power poles are few.

### 12 Oktober 2026: the open items (v0.51.0)

- Facades: BuildingDress.exterior puts a shop's sign (Decimation's logos: Minedonalds / Mineway for
  food, Gunsrus / Decimunition for gun shops, Minebay for other shops) over its door or window, and
  graffiti low on blank outer walls, on the building's outdoor ground storey spots. StreetDress.facades
  does the same from the street side for walls right at the chunk edge (read through
  StreetPlan.neighbourBuilding / BuildingPlan.baseBlockAt). Most converted buildings stand back from
  their chunk edge, so the building side does nearly all of it (seed 1: 214 outer wall graffiti, 1
  facade graffiti from the street; 1 sign: only 2 shops in that area).
- Parks and lots (LotDress, LotPlan now knows seed and district): parks get a bin beside each bench,
  a chess table, a street light, a bicycle, trash, now and then a body; bare lots one scene or none
  (rows of parked cars all one way, a survivor's spot round a fire can, a dumping ground with a
  dumpster). Seed 1's area has parks only (the wasteland district has no park parts and no lots
  there), so the bare lot scenes were not seen yet [not verified in game].
- Traffic jams (StreetDress.jam): within 3 chunks of where a street leaves the city, the outbound
  lane holds cars headlights toward the exit, 2 per chunk (anchors 3 and 11), 80 % of such chunks
  (half that in the wasteland); log "dress jam at x,z heading D". Seed 1: 6 jams.
- Crossing crash (StreetDress.crossingCrash): 11 % of crossings (25 % wasteland): a car through the
  middle, a second one from the side with its nose toward it (gap 4..8), debris; log "dress
  crossing crash at x,z".
- Power poles: every residential street chunk on the line's side (was every second): 31 placed.
- DC gutters (the flush sandstone cell between the road and a raised sidewalk) count as road now:
  curb props stood in them before.
- New tool tools/props/topdown.py DUMP X0 Z0 X1 Z1 OUT.png: top down map of a dump with every
  prop's drawn box and its front as an arrow (from the facing table): checks the direction of cars in
  jams and crashes without the game. docs/shots/city_dressing_v0.51/topdown_jam_crash.png: jam
  cars all pointing south / east toward the exits, the crash car through the crossing, traffic light
  arms over the roads.
- Check: seed 1 dressed vs undressed: the dressing adds 1 finding, a weapon cabinet in the air at
  -19 41 220 under a bunker: the building's floor there is gone in the world (carved by the railway
  tunnel or a cave after the building was written [inferred]).
- Still open: bare lot scenes seen in game; a whole dressed city walked by the user.

### 12 Oktober 2026: v0.51.1

- Arm props (traffic lights, street lights) count as their pole only against a prop whose top stays
  under the arm (4 above their base): PropGeom.propsOverlap in the dressing, low_box in
  tools/props/propclash.py. Their drawn box spans the whole arm from the ground up, so a car under a
  traffic light arm was refused (the crossing crash never got its second car) and street lamps over
  parked cars were reported (the "lamp arm" false positives of v0.49.0).
- Crossing crash: the first car sits 2 off the middle away from the side the second comes from; the
  second (6.9 long) then fits inside the crossing chunk with its nose 0.7 from the first one's side.
  docs/shots/city_dressing_v0.51/topdown_crossing_crash.png: car through the crossing pointing east,
  the second from the north pointing south at its side, under a traffic light arm.
- Found with the verified table (not the dressing): the military base test dump milbase_1 now shows
  8 FACE findings that the placer rule hid: military crates in a conex with their front (label) to
  the conex wall, an ammo case, the flag pole's cloth and the work light into HESCO. Military base
  follow up (docs/military_base.md), not changed here.

### 12 Oktober 2026: v0.51.2

- Bare lots: every district has its own park parts (the wasteland too), so no lot was ever bare
  and LotDress.bare never ran. Now 35 % of open lots stay bare (LcCity.plan, only with the dressing
  on): parked cars, a survivor's spot, a dumping ground or nothing. Seed 1: lots "cars" at -176,144,
  "dump" at 48,96, "empty" at 16,16; the dressing adds no finding.
  docs/shots/city_dressing_v0.51/topdown_lots.png: two cars in their rows, noses toward the middle
  line, on a grass lot; the dump's junk along one edge.
- Military base facing (docs/military_base.md): fixed in MilitaryBase (conex crates face the open
  end, the guard booth crate the door, the TOC ammo case the room); overrides: flag poles have no
  front (cloth reads both sides), the generator's front is unknown (pictures unreadable). All three
  sizes 0 findings, walk test only the known locked TOC note, camp and refuge 0.

## User review (12 Oktober 2026, new world on v0.51.2)

- City refuge: "i was though you place it on wilderness but i was wrong. its really nice and fit
  with other structure. great job." Accepted.
- Props: "nice improvement but still need learning more and the props is not balanced and mostly
  similar between other building." To do: variety between buildings of one kind (today every
  building of a kind draws from the same short list with the same weights) and balance (how many
  of each prop per building / room size). Done in v0.53.0 (section below), waiting for the user's look.

### 12 Oktober 2026: variety and balance (v0.53.0)

Answer to the review above (BuildingDress palettes, themes, stories):
- Each kind has a palette (BuildingDress.PALETTE): 7 to 13 entries, each with a weight, a cap per
  storey (a building may use 3 x that) and how it goes in: STAND, HANG (1 up), LOW (wall at floor
  level), DISPLAY (shop display, closed panel to the wall), ROW (2 or 3 along the wall: shelf runs,
  waiting chairs), DESK (a table along the wall, maybe a monitor or radio on its top, a chair in
  front looking at it). New props: metal / wood tables, chess tables, chairs, office chairs, CRT
  monitors, trash cans, wood crates, open crates, generators, junction boxes, targets, presents,
  water pallets with tarp, large ammo crates, the studio camera, lanterns.
- Each building keeps its own part of the palette (theme(): core entries always, the rest 55 %, at
  least 4), its own weights (x 0.5 .. 1.5), mostly one variant per entry (one TV model, one crate
  kind; 30 % mix them) and its own density (50 .. 140 % of the usual furniture).
- One story per building: calm, looted (open crates, boxes, more trash and graffiti), squatters (a
  camp on one storey: lantern, radio or stereo, boxes, a crate), massacre (1 or 2 bodies with blood
  per storey), quarantine (medical / police / military / lab: body bags, stretchers, biohazard
  signs), barricade (crates, barrels, boxes beside the ground floor doors, off the walkway).
- Measured with tools/props/dressbalance.py on seed 1 (servertest dress, -Pdress logs one line per
  building): mean similarity of two buildings of a kind 0.61 -> 0.39 (police 0.82 -> 0.50, office
  0.71 -> 0.44), distinct props 51 -> 73, the most used prop 11 % (electrical cabinet) -> 8 % (trash
  bags). propclash on the dump: 25 findings (dressing before: about 30); the ones left are converted
  building props (the same cell every storey, crates in walls, water flowed in later).
- Trap: BlockStorageCrate (the footlocker) extends BlockChest, which turns itself from its
  neighbours when placed and ignores the metadata we give it (4 fronts into walls on seed 1). Not
  used by the dressing; vanilla chests have the same habit.
- Pictures: docs/shots/city_dressing_v0.53/ (desk, dining table, squatters' lantern, present).

### 12 Oktober 2026: the wasteland as the military area (v0.54.0)

Full rules and checks in docs/wasteland.md. What changed in the dressing classes:
- BuildingDress: a `waste` flag (the building's district style). Containers of the converted building
  swapped to military loot (supplies(): per building store type), the SUPPLY palette in every non
  military building, denser military buildings, war stories (massacre 22 %, quarantine 20 %,
  barricade 28 %), sandbag barricades, "no trespassing" / Soviet / hazard signs.
- BuildingDress for every district: barricades only at entrances (they piled every inner door);
  building_lab* is LAB (was read as an office).
- StreetDress: zone entry checkpoints (entry), parked military vehicles and convoys
  (militaryVehicles), sandbag fighting positions at crossings, sandbags / wire / military crates on
  the sidewalks, military jeeps in crashes.
- LotDress: military scenes on bare lots (motor pool, supply dump, helicopter crash, casualties).
- tools/props/dressbalance.py reports wasteland buildings as their own rows (kind (w)).

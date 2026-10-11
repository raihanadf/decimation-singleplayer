# City dressing: Decimation props through the whole city (v0.50.0, design)

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

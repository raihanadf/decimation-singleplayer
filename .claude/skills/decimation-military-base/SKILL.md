---
name: decimation-military-base
description: Build, extend or review the procedural military bases (US FOB style, three sizes) of our worldgen for Decimation, or any similar procedural compound (police station, refugee camp, Soviet base). Use when the user asks for military places, bases, outposts, checkpoints or compound like structures with props and loot.
---

# Decimation military base (and compounds like it), end to end

Learned building the US FOB generator (10 Oktober 2026, v0.42.0). The facts live in
docs/military_base.md (reference, layout, code map, status, how to test); this skill is the order
of work, the checks and the lessons. User standard for this work: "really really good output ...
check small detail ... be detailed person and architecture person ... work on loots especially
props placement".

## 0. Read first
1. docs/military_base.md (all of it), docs/prop_placement.md and the skill decimation-props
   (how props are drawn, facing, drawn sizes: a jersey barrier / table 2 long, large ammo case
   1.9, care package 2.25 x 1.9, spotlight 2.5 x 3.2, APC 4.5 x 8.9), docs/prop_catalogue.md
   (what every Decimation block looks like).
2. Code: worldgen/military/ (Canvas, MilitaryBase, MilitaryBasePlan), LargeSites.militaryBase,
   Slices.placeAt, devtest/MilBaseTest.
3. The lessons and casebook at the end of this file.

## 1. Research before building (the user is away; do it yourself)
- Web search real references (doctrine pages, army.mil / DVIDS articles, product sheets) for
  the layout, the sizes and the small things that make the place real. Write the facts with
  their sources into the design doc section 1 BEFORE coding.
- Map every real element to a Decimation block (table in section 2 of the doc); the catalogue
  has HESCO (our block deciworldgen:hesco; Decimation's BlockMilitaryBarrier is a costly prop),
  sandbags, concertina, T-walls (BlockBarrierTall), jersey
  barriers, hedgehogs, crates, cabinets, radios, generators, light towers, flags, wrecks.

## 2. Build
- Canvas in plan coordinates (gate south), Decimation props by facing (front east 2, south 3,
  west 4, north 5), doors through Canvas.DOOR (vanilla door metadata), vanilla blocks through
  Canvas.VANILLA (Rotation turns them). MilitaryBasePlan turns the whole base.
- Fixed first (wall, gate, towers, roads, TOC, the living area rows), then the greedy placer
  `fit(name, zone, scan direction, module)` for the rest; every module calls `reserve()` for its
  rectangle (a 1 block margin may touch roads, never another module) and returns false when it
  does not fit.
- Record camera points (`poi`) inside every module worth a look; the dev test photographs them.
- Loot = Decimation's own crate blocks placed with intent (arms room, ASP, towers, fighting
  positions, conexes, aid station, footlockers); its loot registry fills them.
- Every prop from its DRAWN box (PropBoxes, docs/prop_placement.md): rows step by drawn length,
  long props along walls, aisles and door fronts free, tall props with free cells above.
  `c.validateProps(log)` at the end of build() removes and logs clashes; keep it at 0.
- Walls placed by the hundreds are plain blocks (deciworldgen:hesco), never props.

## 3. Test (live, minutes per round)
- `python3 tools/devtest.py --live milbase sizes=0,1,2 points=none`: builds and lists what did not
  fit ("refused"). Fix the layout until nothing important is refused.
- The same run writes run/client/devtest/milbase_<size>.tsv; `python3 tools/props/propclash.py
  dev/run/client/devtest/milbase_1.tsv` must report findings 0 (walls, props, doors, facing,
  wall mounts, floating).
- `python3 tools/devtest.py --live milbase sizes=...`: all shots; read the sheet and the single
  shots (aerials, top, gate, every inside). New classes / methods need `--stop` first.
- Real worldgen: `python3 tools/servertest.py 1 type=decimation pregen=-64,448,9` and a top down
  block map from the region files (tools/worldcheck.World) to see it on real terrain.

## 4. Review checklist (small details; the user looks at these)
- Nothing overlaps (refused list), nothing floats, no prop inside a wall, every door has a step
  and opens onto free ground, every ladder has a hatch, bunk beds have their upper bunk.
- The walk test is 0 (tools/props/walkcheck.py on every size and turns 0 / 1 / 3): every ladder,
  door, room and loot container can be walked to from the gate, no traps. Checking a thing's own
  cells is NOT enough: check the player's way to it.
- Every essential module was built: "has its TOC" PASS; read the refused list for anything else
  that matters (motor pool, LSA, ASP).
- Every area tells what it is from 10 blocks away (TOC antenna + flag, motor pool vehicles +
  net, ASP berm + crates, aid tent stretchers, helipad H + lights).
- Density: no big empty dirt patches; fill with conex yards, hangars, bunkers, generator pads.
- Colours in Decimation's pack: dirt and coarse dirt are orange: ground mostly gravel.
- Decay of an overrun base is light (a breach, slumped cells, trash), never on the roads.
- Shots saved to docs/shots/<topic>_v<version>/ and described in docs/shots_index.md.

## Lessons (read before starting)
0. A test site must stay out of the user's play area: the sky test now builds at x -20000,
   z -20000, y 230; the earlier sites (3000 / 110 and -4000 / 200) cut into cities and were
   cleared from the dev world with `tools/perfcheck.py clearblocks` (game closed, region backup).
1. Research real references first and write them down with sources; the layout follows real
   doctrine (perimeter rings, ECP, TOC in the interior, living area, logistics, services).
2. Modules overlap unless something stops them: the occupancy grid with a road class and a module
   class, plus a greedy placer that logs what did not fit. Fixed coordinates for three sizes
   failed (first layout: the TOC on the main road, tents in the living area, ASP on the fuel
   point).
3. Company streets: rows of B-huts face each other across a 9 wide street, bunkers between the
   door columns (in front of a door they block it), blast walls between huts.
4. Test in the sky with no ground ramp (grading from y 200 to the real ground grew 100 block
   pillars), clouds off, render distance 16, a long capture window (slow frames skip short ones).
5. Decimation's texture pack: dirt and coarse dirt are orange; exposed dirt grows grass (a berm
   top turns green); hardened clay reads orange brown: tents use smooth sandstone.
6. Regular patterns look artificial: a sandbag course on every third block read as castle
   battlements; use noise for piles and rough edges.
7. Name camera points in the generator itself (poi), so every module can be photographed after
   every change without hand placed cameras.
8. Props are models, not blocks (user review of v0.42.0, 28 shots): compute the drawn box of
   every prop before placing it and validate the whole base (skill decimation-props). The first
   bases had about 40 clashes per FOB that pictures from a distance did not show.
9. Count tile entities: a prop used as a building material (BlockMilitaryBarrier as HESCO) makes
   thousands of model renders. Use a plain block of our own.
10. Locked buildings: the TOC uses Door_Emergency_1_Locked + a military keycard screen; the key
    drops from military crates, wrecks and care packages, so the room stays reachable.
11. Ground in Decimation's pack: gravel with about 10 % dirt patches; coarse dirt never.
12. Reachability, not just placement (user review v0.42.3: "check the corner dude"): a ladder in
    the middle of a corner tower's inner face stood on the wall line. Place access points (ladders,
    doors) by testing the cell in front, and run the walk test before handing over.
13. A refused module must be loud: the outpost lost its TOC for four versions because a refused
    reserve was silent. Every essential module logs its refusal with the blocking cell
    (blockedAt) and the dev test fails on it.

14. People belong to the site, not to generation: a compound with residents (safezone camp)
    hands its posts to worldgen/SafezoneSites with its zone; a keeper spawns them when a player
    comes near (entities spawned during population get lost or doubled). Check every post kind
    in the dev test.
16. A test site the user can visit must look like the real thing when the test ends: never
    remove its people or zone afterwards (remove an earlier run's at the start instead). And
    check Decimation's server side resets of NPCs (traders wiped every 3000 ticks on a
    dedicated server) when residents depend on Decimation classes.
17. Residents are placed by a keeper, never by the generator, and only once the site's chunks are
    populated; static NPCs (Decimation traders and guards) hang wherever they spawned, so spawning
    them before their floor exists leaves them in the air. Watch the residents for 30 s in the test
    (positions, put back count) and keep friendly fire harmless in safezones (no retaliation).
15. A 1 x 1 stall (outhouse, booth) needs two free cells above its floor: a seat block on the
    floor left no head room (walkcheck DOOR finding).
18. A building with storeys is only done when walkcheck reaches every floor and the roof (it climbs
    stairs since v0.48.0; it used to stop at y 4): place stair runs with the slab above opened where
    a head passes, and give every floor its loot / beds a reachable cell.
19. Test every rotation through the LIVE game (`--live MODE turns=N`): a one launch run ignores
    key=value options and silently tests turns 0. Read the turned dump for stairs, gates, doors,
    beds and wrecks: Canvas.set(x, y, z, block, 0) used to skip rotation for metadata 0 (stairs
    ascending east, fence gates spanning x).
20. Put camera points where a person would stand to look at the room, not in the furniture: read
    every shot, move the point when a chair or table fills the frame.
21. A rebuild over an earlier build at another rotation breaks multiblock props of the old one
    (APC, shelves) and their parts take new cells with them: clear the box before rebuilding in
    tests and commands.

14. Facing comes from the verified table (docs/references/prop_facing.tsv), not the placer rule
    alone: a crate's front is its latch face, so a crate in a 1 wide hold faces the open end, a crate
    in a booth faces the door. propclash.py checks FACE with the table; keep it 0 for every size.

14. In Decimation's pack grey and white stained clay read pink-brown: paint aircraft and white markings in
    wool (light grey 8, grey 7, white 0); yellow and black clay read right.
15. A big flat open compound in a city meets the world's late decoration: lakes and springs (deny them over
    cities, TerrainEvents) and other city features laid by rows (a metro station's stairs): check the
    generated world for water and holes, not only the sky test.

16. Start every compound by invoking this skill and doing its research step for real (a dozen sources, the small
    things written into the doc): the airport built from memory with two searches read as "to basic".
17. When the user is playing, verify through the dedicated server (servertest, region file dumps for propclash
    and walkcheck) instead of the client; never chain the pgrep check with a live test.
18. Read every sky test shot, not only the sheet: a frame showing one wall, a ceiling or darkness is a camera point
    inside or facing a wall (the airport's HQ rooms, armory and radar shots showed nothing for a whole version).
    Put room cameras in a corner looking across the room at its furniture.
19. Faction props follow the base: US style compounds fly BlockFlagPollUAHD (blue), never BlockFlagPollUSSR.
    Colours of big surfaces too: rusty BlockMetal_3 reads brown from 10 blocks (blast doors are camouflage green).
20. Decimation blocks with an axis in their metadata are not turned by Rotation: BlockRoad_CenterLine (4 along z,
    2 along x) needs a swap in the plan's blockAt on odd turns, like the boom arm. Look at turns 1 / 3 shots for
    dashes showing as ticks across a road.

## Casebook (never delete a case)

### Case 1: first FOB generator (v0.42.0, 10 Oktober 2026)
- Request: "military zone with buildings ... our own generator, all three sizes, US FOB style ...
  really really good output", user away for an hour.
- Round 1 (sky test inside a city): base seen, many details right (TOC inside, towers, gate
  lane); problems: orange ground, ASP berm like a planter, TOC roof crenellated, shelter roof an
  orange table, city towers in the aerials.
- Round 2: grading pillars in the sky test, fog, skipped shots: test fixed (no ramp, y 200,
  clouds off, render distance, 30 tick window).
- Round 3: modules overlapping, bunkers blocking doors: occupancy grid + greedy placer +
  refused log; the COP got its own layout; sizes COP 50 x 58.
- Round 4: large FOB too empty in the south-east: hangar and conex yards. Real worldgen on seed
  1: two COPs placed, no errors.
- Evidence: docs/shots/milbase_v0.42/ (shots_index.md).

### Case 2: user review of v0.42.0 (11 Oktober 2026) -> v0.42.1
- User: "fix 1 and 2 now, replace military barrier ... its really hurt fps ... door type is using
  non locked type ... dataset issue about props position ... read fully codebase about props
  placement direction". 28 shots: docs/shots/milbase_v0.42_review/ (shots_index.md).
- Found: HESCO prop per cell (FPS), unlocked TOC door, keycard on the outside wall flag, raw dirt
  ASP berm and orange ground patches, about 40 prop clashes per FOB (arms room, TOC desks,
  DFAC, kitchen, tower spotlight, fighting positions, conexes, vehicles sideways), stair roofs
  reading as a saw tooth, closed trapdoor shutters as shelves, latrine without a step, guard
  booth built into the gate tower.
- Fix: deciworldgen:hesco, Door_Emergency_1_Locked, gravel ground, sandbag ASP, the prop
  placement study (docs/prop_placement.md, tools/props, skill decimation-props), validateProps,
  every module re-laid; 0 clashes on all sizes. Shots: docs/shots/milbase_v0.42.1/.

### Case 3: second review (v0.42.1 -> v0.42.2, 11 Oktober 2026)
- User shots docs/shots/milbase_v0.42.1_review/user_100..106: TOC door side held only an ammo
  case and a cabinet turned toward the door; barracks footlockers should partly be real chests
  (loot); the DFAC counter stoves should be the 1 block radio; the motor pool "boring" (the user
  placed shelves to show what they want); our test structures cut into a city in their world.
- Fix: door wall loot (cabinets facing the room, police / military crates, the large case with a
  small one on top), footlocker() half vanilla chests, radioSmall on the counter, parts yard +
  workshop in the motor pool (shelves drawn 2 wide step 3), test site moved and old sites cleared.
- Test traps found: rebuilding a base in place drops its multiblocks every second run; the first
  build into brand new sky chunks can leave a tower knocked down (rebuild was clean) [inferred:
  generation of the chunks being written].

### Case 4: third review (v0.42.2 -> v0.42.3, 11 Oktober 2026)
- User shots docs/shots/milbase_v0.42.2_review/user_109..114.
- Tower: the ladder in the inner corner sat under the corner post: the hatch was blocked. Now the
  ladder runs up the middle of the inner side (shaft cut into the HESCO, ladder on its back face),
  crate and radio on the deck row away from the hatch, the cell in front of the hatch free.
- Hangar: each vault column only had its top block, and the 3 -> 5 step left a gap along both
  sides: the roof floated. Now each column's skin runs from the outer column's top to its own.
  Lesson: a stepped vault or roof must be closed between steps (check from the inside).
- Motor pool shelter: full spruce roof instead of the checkerboard. Hangar: crates, cases,
  police / medical crates, a cabinet along both walls.
- Sky test: real worldgen in chunks generated after the test base read the base top as the
  ground (a city edge ramp cut a band out of it). MilBaseTest now generates and populates every
  chunk under and 3 around a base before writing it, and puts each size at its own spot
  (X0 + size * STEP; a sizes=2 run used to land on the outpost's spot).

### Case 5: walled in ladders, a missing TOC (v0.42.3 -> v0.42.4, 11 Oktober 2026)
- User shot docs/shots/milbase_v0.42.3_review/user_115.png ("check the corner dude"): the corner
  tower's ladder runs up above the side wall. The dump showed 5 of 6 ladders with HESCO or the
  booth's sandbags in front; my v0.42.3 check only looked at the air above the hatch.
- User choices (plan mode): ladder inside the tower in an open face cell; an automatic walk test
  (no access to their computer) covering ladders and decks, doors and rooms, loot, traps, roofs.
- Fix: MilitaryBase.ladderCell (first non corner cell of a base facing face with open ground in
  front), deck crate / radio on the cells farthest from the step-in cell, radio facing it;
  tools/props/walkcheck.py; the outpost TOC reserve fixed (overlapped the main road; missing
  since v0.42.0, found only because the new ladder rule took the large FOB's TOC and its refusal
  was logged); small TOC layout; "has its TOC" check. All walk tests 0, propclash 0.

### Case 6: verdict, accepted (v0.42.4, 11 Oktober 2026)
- User: "i like the result. so work is done". Accepted after four review rounds: the layout,
  props from their drawn boxes, loot placement, reachable towers, the walk test. Path that
  worked: research -> build -> sky test pictures -> user review -> fix the mechanism (prop
  geometry, walk test, refusal logs), not the single symptom.

### Case 7: survivor camp, first build (v0.47.0, 11 Oktober 2026)
- Request: safezone with traders and guards (user choices: camps outside cities, walled camp of
  our own generator, guards friendly unless provoked, civilians and armed survivors).
- Built on the same pipeline: CanvasPlan now shared (MilitaryBasePlan moved onto it, military
  test all PASS after), military/SurvivorCamp, residents via SafezoneSites.
- First run: 6 clashes (stretchers into the tent end wall; ammo cases / cabinets 1 cell apart in
  the stalls) and an outhouse seat with no head room; walkcheck counted closed fence gates as
  walls (now opened like doors). After the fixes: clashes 0, propclash 0, walkcheck 0, turns 0
  and 1. Evidence docs/shots/safezone_camp_v0.47.0/.

### Case 8: "the npc is missing" (v0.47.0 -> v0.47.1, 11 Oktober 2026)
- User shot docs/shots/safezone_camp_v0.47.0_review/user_119.png: the sky test camp's market
  empty; the user suspected Decimation's trader reset countdown (from setting up its server).
- Cause: the camp test removed the residents and site at its end; the user visited the test
  world afterwards. The reset exists only on a dedicated server (ServerTickHandler, stripped in
  singleplayer); handled anyway (keeper respawns traders / guards at once there).
- Also seen in the shot: awnings white from the street (the white stripe was the front column).
- Fix: test camp stays populated, keeper dedicated server respawn, front edge coloured,
  `/deciworldgen camp`.

### Case 9: flying guard, retaliation, hidden entry sign, boring corners (v0.47.1 -> v0.47.2, 11 Oktober 2026)
- User: "npc shot me when they got hit by me better remove it, its easily accidently hit friendly
  npc"; shots user_120 / user_121 (a guard standing in the air over the wall) and user_122 (the
  "Entering Safezone!" text cut by the lintel logs); "add more cosmetic props ... jeep cars,
  package, crate".
- Fix: provocation removed (ZoneRules, zones test reworked), keeper waits for populated chunks
  and puts residents back, entry sign one block outside, clutter() (jeeps, a car wreck, care
  packages, crates, barrels, tyres, bags, a plaza table). First clutter run: 3 clashes (a jeep
  turned along x into the wall: military wrecks lie along z at 2 / 3 and along x at 4 / 5; crates
  drawn 1.4 across their front side by side), fixed. Camp test with a 30 s watch: 0 off post,
  0 put back, survivors in survivor gear; zones test all PASS; propclash 0 (91 props), walkcheck 0.

### Case 10: city refuge, first build (v0.48.0, 11 Oktober 2026)
- Request: "start the fortified city building now"; choices: our own building on a whole city
  block (Recommended over fortifying a DeceasedCraft building: their interiors differ per building
  and rotation), on the block nearest each city region's middle. User away: "check check all
  details correctly okay? from layout, props and anything on it".
- Built: worldgen/military/CityRefuge (48 x 48, three storeys + roof, walled yard, chicane gate,
  22 residents) and CityRefugePlan (FixedBase, no grading, zone box 2 around), LcCity.refuge.
- Found and fixed in the rounds: 3 clashes (stall goods drawn into the gaps), walkcheck never
  climbed above y 4 (stairs added to the tool), back row tents opening into the wall, a light
  tower facing a tent, cameras inside furniture, a crate drawn into a partition, the stair camera
  facing away, Canvas metadata 0 not turned (gates and stairs in rotated compounds; camp and bases
  rechecked turned: propclash and walkcheck 0), propclash counting a shelf's upper part as a
  floating prop when it crossed a 4 block bucket, one launch runs ignoring turns=N, a turned
  rebuild losing a tent to the old APC's multiblock break.
- Check: turns 0..3 all PASS, propclash 0 (222 props), walkcheck 0; seed 1 server: refuge at
  80,64,144 complete and level with the streets. Evidence: docs/shots/city_refuge_v0.48.0/.

### Case 7: facing against the verified table (v0.51.2, 12 Oktober 2026)
- The verified facing table (city dressing work) made propclash FACE stricter: 2 / 7 / 30 findings
  on the outpost / FOB / large FOB (turns 0): conex crates with their latches to the conex wall, the
  guard booth crate to its bags, the TOC ammo case to the wall, the flag pole and a generator.
- Fix: crates face the open end / the door / the room; flag poles no front, generator front unknown
  (overrides). All sizes 0, walk test unchanged. Trap: hotswap does not rebuild the cached base
  layout; stop and start the live game after changing MilitaryBase.

### Case 7: military airport (v0.62.0, 14 Oktober 2026)
- User idea 18; choices: half a wasteland sector, any wasteland city, planes from blocks, held but half
  wrecked, bunker level loot in the HQ and armory. Design: docs/airport.md (references, layout).
- Built on the Canvas pipeline (military/Airport, AirportPlan) as a city block of 2 x 4 cells (city/Airport).
- Found on the way: clay colours (lesson 14), 4 prop clashes (cargo hold too narrow for a care package, the big
  radio against the cab wall, crates 1 apart), a lake and a spring in the generated airport, a metro station's
  stairs through a hangar floor (lesson 15).
- Check: sky test clashes 0, propclash 0, walkcheck all reachable; seed 1 server, two airports generated and
  read back clean. Shots docs/shots/airport_v0.62/.

### Case 8: "to basic" (airport v0.62.0 -> v0.62.1, 14 Oktober 2026)
- User: "hmm, to basic. are you using the skills before? and you still remember how to make structure correctly and
  really detailed?" The first airport was built without invoking this skill: thin research, fixed boxes, empty
  concrete, bare buildings.
- Redone through the skill: 13 sources into docs/airport.md, the real details mapped to blocks, module reserves,
  lighting and markings, revetments, shelters, fire station, radar, igloos, a FOB style entry (lesson 16).
- Test trap: the live sky test ran beside the user's open game (pgrep chained, third time); stopped, verified on the
  server instead (lesson 17): no clash, propclash 0 on 248 props, walkcheck 0.

### Case 9: airport sky shots (v0.62.1 -> v0.62.2, 14 Oktober 2026)
- User closed their game and asked for the pictures. Sky test turns 0 and 3, 28 shots each.
- Found by reading each shot: red Soviet flags at the entry and HQ, brown stepped blast door leaves, an oak farm
  fence round the radar, four camera points showing a wall or darkness (hq_ops, hq_office, armory, radar), and in
  turns 3 the landside road dashes lying across the road (lessons 18 to 20).
- Fix: UAHD flags, green ribbed leaves, iron bar fence, AirportPlan.blockAt turns the centre line, cameras moved.
- Check: clashes 0, propclash 0 on 247 props and walkcheck 0 (2 keycard notes) in turns 0 and 3; turns 3 dump has
  all 109 centre line cells at metadata 2. Evidence docs/shots/airport_v0.62.1/.

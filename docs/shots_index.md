# Screenshot index (what every saved picture shows)

The pictures live in `docs/shots/` (git ignored: they show Decimation's
models and textures, the public repo holds only our own work). They stay
on this machine. This file is the committed memory of what they show, so a
later session reads THIS instead of opening or re-taking images. Open an
image only to check a detail not written here; re-shoot only after the
code that produced it changed.

How shots are made: `./gradlew runClient -Pautotest` (street views),
`-Paudit` (building audit), `-Pgallery` (prop gallery); output in
`dev/run/client/screenshots/`, then copied here into a versioned folder.

## wasteland_gate_v0.60.2_review/ (user shots in their dev world, 14 Oktober 2026)
- user_gate_1.png: a city to wasteland gate of v0.60.2 seen from the city street: posts at the road edges,
  striped sawhorses across, wire, a traffic light; the wasteland's own checkpoint (jersey line, sandbags,
  jeep) behind; banner "Greyford: Downtown - 6". The user wanted a toll gate and 6 styles.
- user_fence_float.png: the inside fence beside a sidewalk: metal posts and iron bars standing on top of the
  sidewalk's half slabs, hedges and wooden fences, so they look like they float (fixed v0.61.0).

## wasteland_gate_v0.61/ (dev test gates, sky at -20000, 230, -19100, 14 Oktober 2026)
- sheet_gates.png: every look x state (front from the street in front, top from above). Looks: toll
  (canopy on 4 columns, island glass booth, arm down / raised), checkpoint (staggered jerseys, sandbag
  position left, camo tower right, searchlight, flag), containers (two stacks 2 high with a gap, wire on
  top, container behind), quarantine (white peaked tent, strip curtains, body bags right, generator left),
  police (blue booth with beacon, cruiser across, cones). States: wire (iron bar fence across the road at
  the line, wire rolls, mines not visible: entities), steel (patched metal wall at the line, sandbags),
  bus (yellow and black clay bus filling the gap at the line), ditch (no look: trench across the street,
  hedgehogs and wire inside, wire on both lips, sign), open (lowered arm gone, black scorched road,
  skeleton, knocked sawhorse). Sky test artefact: light blue at the ditch's sides (the pad is one layer).
  Single shots gates_<look>_<state>_front / _top.png beside it. Sky artefacts: the quarantine/open and
  police/open fronts show leftovers of the first run's six style layout built on the same sky spots (a
  blue sedan, a white tent), not the gates.

## city_sizes_v0.60/ (cities of 4 to 64 districts, seed 1 server world, 13 Oktober 2026)
- topdown_seed1_48districts.png: tools/topdown_blocks.py, x 1150 .. 1900, z 170 .. 870, 1 px per
  block (north up). The 48 district city (CityLayout: 12 sectors at x 1280 .. 2303, z 256 .. 1023, only
  its north west part generated). Its 3 wasteland sectors form an L in the top left (x 1280 .. 1791 on
  z 256 .. 511, x 1280 .. 1535 on z 512 .. 767): grey military lots ringed red (iron bar fence), dark
  road grid; the normal city (green park edges, coloured roofs, blue pools) to the right and below,
  its streets running straight on through the fence line. Grass to the west (the military sector
  beside it lies further west, not in the picture), a highway comes in from the west at z about 520
  and one from the north at x about 1545; small lakes north of the city.

## update_v0.59/ (auto update, dev test `update` against the fake release server, 13 Oktober 2026)
- sheet_update_real.png: the same three screens against the real GitHub release 0.59.0 (229 MB).
- sheet_update.png: update_menu: Decimation's home menu with a red "Update" button where Resume / Play
  was; update_progress: our screen "Decimation update 9.9.9", "Downloading 13 of 40 MB", green bar
  33 %; update_ready: "Ready. Restart the game to play 9.9.9.", full bar, the note on Quit game / Later,
  buttons Quit game and Later.

## bugs_v0.58/ (friend's bug reports, 13 Oktober 2026)
- hitbox_side.png: arena, a Hulk (left) and a Bloater (right) seen from behind with their bounding boxes
  drawn (white): both models stand inside a 1.2 x 2.85 box, the box top level with the head.
- sheet_scope.png: dev test `scope` after the copy change: 4x / 8x / integrated sniper overlays, red dot
  and 2x sights with the world visible through the glass, two window sizes.

## menu_v0.57.2/ (update check patched out, 13 Oktober 2026)
- menu_home_no_voicechat.png: Decimation's home menu opened in a world with mods/DecimationVoiceChat.jar
  moved aside: buttons Resume Game (orange), My Community, Server Status, Settings, Disconnect; no
  Update button, profile and stats on the left.

## v0.57.1_wasteland_sector/ (whole sector wasteland city, seed 1 server; 13 Oktober 2026)
- topdown_seed1.png (tools/topdown_blocks.py, 2 px per block, x -300 .. 100, z -40 .. 300): the
  wasteland city sector (x -256 .. -1) with its dark broken streets, red outlined military lots and
  bunkers, flush against the spawn city at x 0 (lighter DeceasedCraft streets, parks, pools); no grass
  between them; the land fence (red dots) on its north, west and south sides.
- topdown_edge_gate.png (8 px per block, x -30 .. 30, z 50 .. 140): the fence along x 0 (red wire, black
  posts every 4) on the spawn city street's west sidewalk; where the wasteland street z 64 .. 79 meets
  it a gate: orange barriers on every second cell across the road, posts at the road edges, the street
  going on into the city's crossing.
- topdown_corner.png: the south end: the inside line at x 0 carried out to the land fence row at
  z 259, closed corner.
- sheet_wasteland.png: dev test `wasteland` in sector mode: fence shot (chain link, posts, wire on a
  sidewalk), gate (barriers across a road, guard booth), bunker, checkpoints, lots, as in v0.56.

## v0.56_user_review/ (user screenshots of v0.56, 13 Oktober 2026)
- user_ring_1.png, user_ring_2.png: from the air: a fenced wasteland piece beside normal city blocks
  with a large square of bare grass between them where city blocks should be; the city's streets end
  at the grass. The ring the user called absurd.

## loot_spike_v0.56/ (dev test loot, the two loot spikes, 13 Oktober 2026)
- sheet_loot.png, three shots. loot_crate: Decimation's inventory screen (clothing slots, player
  model, "Loot" area on the right) showing OUR inventory: apple x3 in slot 0, iron ingot x2 in
  slot 5, exactly as filled by Loot.open. loot_body_world: the LootBody entity on the arena floor,
  drawn as a plain white box (no model of its own yet). loot_body: the same screen opened by a
  right click on the body: gold ingot x4, bread x2; the apple taken from the crate earlier sits in
  the main inventory.

## place_names_v0.52.1/ (dev test places, 12 Oktober 2026)
- places_3_floating_name.png: flying at y 128 in the Suburbs district looking south at its middle:
  ClanTurfLayer's floating name "Hollowhaven: Suburbs - 6" over the district with spaces (was "_",
  user report); chat "Now entering Hollowhaven: Suburbs - 6". A first try in the Downtown district
  stood inside a tower and the wall hid the name.

## place_names_v0.52/ (dev test places, 12 Oktober 2026)
- sheet_places.png: places_0 Decimation's banner "HOLLOWHAVEN: DOWNTOWN - 5" / "ENTERING LOCATION"
  over the city and the chat line "Now entering Hollowhaven: Downtown - 5" (spaces shown);
  places_1 walking into the next district: "HOLLOWHAVEN: SUBURBS - 6" and a second chat line;
  places_2 outside the city: "Now entering the Wilderness". Night rain in the dev world.

## v0.56_wasteland_city/ (wasteland city apart, fence, bunker; 13 Oktober 2026)
- topdown_seed1.png: seed 1 server world from above (2 px per block): the three wasteland districts in a
  row, open grass all round (the other districts gone from the ring), the fence line as red dots (wire)
  and black posts, street stubs out through gates, a river east. topdown_gate.png: one corner enlarged:
  the street going north through the fence with orange barriers across it, a west gate the same.
- wasteland_fence.png: from inside: dark metal posts, chain link of iron bars, coils of barbed wire on
  top, grass beyond, a landmine disc on the ground.
- wasteland_gate.png: a gate: a stone brick guard booth with glass windows and a metal door at the
  roadside, striped sawhorse barriers across the road at the fence.
- wasteland_bunker_blockhouse.png: the bunker's blockhouse: a stone brick hut on the bunker's concrete
  roof with the red brown locked door and a green keycard screen beside it. wasteland_bunker_garrison.png:
  inside, elite military with night vision and machine guns between bookshelves and crates; flames are
  particles (spawner cage and gunfire), no fire in the save. sheet_wasteland.png: the run's sheet.

## v0.56_city/ (13 Oktober 2026)
- ramp.png (dev test `cityedge`): the asphalt ramp: road slab steps climbing to the higher street,
  iron railings on the high side, the dashed line on the road.
- sheet_rail.png: rail test: rail_entrance the street through the station chunk intact, the railed stair
  well in the right sidewalk; platform, stairs, shaft, tunnel, line end, crossing as before.
- sheet_bridge.png: bridge test (a non wasteland bridge): aerial, deck, side, under.

## v0.55_cityedge/ (city edge worn ground, 13 Oktober 2026; dev test `cityedge`, cell 16,-16)
- cityedge_out.png: from the north sidewalk looking out: the sidewalk's slab tiles fray into stone and
  cobblestone cells, then dead grass, a 1 block grass terrace beyond, dead trees.
- cityedge_in.png: from 22 blocks out looking at the city: grass terraces in front, a big building
  behind the street; the pavement band is hidden behind the terrace.
- cityedge_along.png: along the band: grey broken pavement beside the sidewalk, mossy cobblestone
  cells thinning into the grass. cityedge_aerial.png: from above, the frayed grey edge along the
  street and grass beyond. sheet.png: the four. (An earlier try with coarse dirt read orange: not kept.)

## v0.55_locked/ (wasteland locked doors, 13 Oktober 2026; dev test `wasteland`, district 20,-20)
- wasteland_locked_1.png: a red brown Decimation locked door seen from inside a building (crates and
  military cases before it), hover text "This door needs a lockpick".
- wasteland_locked_2.png: a locked door in a dark brick wall with the green military keycard screen
  beside it, 1 up. wasteland_keycard_1.png: the screen on an outer wall beside a locked door, hover
  text "Right click to use a key card". sheet_wasteland.png: the run's contact sheet.

## v0.55_npc_rifles/ (elite and juggernaut rifles, 13 Oktober 2026)
- sheet_npc.png (dev test `npc`): lineups; npc_3.png juggernauts with rifles (one tan stock),
  npc_4.png elites (night vision) with rifles.
- sheet_gunview.png (`gunview guns=m110,ak12 attach=4x,arSuppressor,foregrip`): hip / aim / NPC side
  view; the NPC's AK-12 shows the suppressor and the scope.

## wasteland_v0.54.2/ (motor pool and crash site reworked, 13 Oktober 2026; docs/wasteland.md "v0.54.2")
- wasteland_lot_motorpool.png: forced preview in the sky (dev test `wasteland`): two jeeps in the bay
  line at the near edge, noses toward the camera (the preview's random street side), middle bay out;
  on the far edge the workshop row (metal shelves, workbench, tyre stack, generator), fuel drums and a
  cone in the far left corner, a work light. The preview's seed rolls no second line. Magenta at the
  left edge: another test's wool floor under the lot.
- wasteland_lot_helicopter.png: the crash site preview: helicopter on burnt ground (black, dark brown,
  grey clay cells along the hull), a line of sawhorse barriers and cones across the near edge with a
  military crate in it. wasteland_lot_supplies / _casualties.png: unchanged scenes.
- sheet.png: contact sheet of the last run (district 18,30).
- topdown_lots.png (seed 1 server, tools/props/topdown.py; red = vehicles with the front as a yellow
  line from the block centre, blue = other props, dark brown = burnt clay): motor pools at -240,176,
  -48,144 and -32,240 with two lines of three jeeps, noses toward the west street, the workshop on the
  east edge, drums in a corner; -144,192 a line of three jeeps nose east, the missile launcher behind
  them, the workshop on the west edge; the crash site at -224,160 (burnt strip along the hull, cordon
  on the north and south edges). topdown_<scene>_<x>_<z>.png are the single lots.

## wasteland_v0.54.1/ (wasteland weak spots, 12 Oktober 2026; docs/wasteland.md "v0.54.1")
Dev test `wasteland` (live, deciworldgen_devtest), district 58,18 (blocks 3712,1152), all PASS.
- lots_sheet.png: the four forced lot scenes (Wasteland.previewLot, sky at -21000, 200, -21000),
  one per quarter: motor pool (two jeeps, noses the same way, on a concrete pad with a grass border,
  barrels and a generator at the edge), supply dump (rows of military crates, care packages, a water
  pallet, a spotlight, an ammo case, on the pad), helicopter crash (camo helicopter with rotor on
  grass, debris), casualty point (body bags in rows in four colours, medical crates, a stretcher, a
  hazard light). A magenta line along the motor pool lot's west edge: the floating lot's side
  [not verified]. wasteland_lot_<scene>.png are the single shots.
- sheet.png: the run's contact sheet. From generation in the district: wasteland_supplies_1 and
  wasteland_spotlight_1 (a supply dump lot on its concrete pad among the buildings, crate rows, a
  care package, a spotlight), wasteland_helicopter_1 (a helicopter crash lot on grass beside a
  building), wasteland_jeep_1/2 (jeeps in their lanes). Weak shots: milcrate_3 (camera in a corner),
  entry_1 (a grass mound in front).
- wasteland_aerial.png: the district from 80 above (streets, rooftops, water to the east).
- wasteland_milcrate_1.png: a military crate on the sidewalk against a planter, front to the road;
  wasteland_medcrate_1.png: a medical crate at the edge of a room with boxes and a military radio;
  wasteland_cabinet_1.png: a weapon cabinet in a camp tent, front out; wasteland_entry_1.png: weak
  (a grass mound hides the entry).
- topdown_helicopter.png (seed 1 server, lot -224,160: the helicopter 14 long along x inside the lot,
  nose east, ammo case and dead around) and topdown_motorpool.png (lot -240,176: a jeep and an APC
  side by side, both noses west, spares behind their tails on the east edge). Both from the gravel
  try before the concrete pads.

## wasteland_v0.54/ (the wasteland as the city's military area, 12 Oktober 2026; docs/wasteland.md)
Dev test `wasteland` (live, deciworldgen_devtest). Run 2, district 30,-4 (blocks 1920,-256 to
2047,-129), all checks PASS; flag_1 is from run 1 (district 36,12, blocks 2304,768).
- sheet.png: run 2 contact sheet (19 shots).
- wasteland_entry_1.png, wasteland_entry_2.png: zone entries seen from inside: jersey barrier line
  across the road, hedgehogs beyond it, sandbag wall two high on the sidewalk (entry_2), wire.
- wasteland_spotlight_1.png: an entry from the side: barrier, concertina coil on the road, sandbag
  stack on the sidewalk, the spotlight behind, a jeep in the outbound lane.
- wasteland_jeep_1.png, wasteland_jeep_2.png: military jeeps parked in their lanes, headlights the
  lane's way; wasteland_apc_1.png: an APC in a lane, facing the camera (its lane's way).
- wasteland_supplies_1.png: supply dump lot: a row of military crates, a care package behind.
- wasteland_milcrate_1/2/3.png: military crates swapped in for the converted chests, against walls,
  front into the room (2: a row of three with boxes; 3: beside a table in a camp tent).
- wasteland_medcrate_1.png: a medical crate (aid station store) between wood crates in a corner.
- wasteland_cabinet_1.png: a weapon cabinet in a corridor, front out; wasteland_ammo_1.png: an ammo
  case on a step against a wall.
- wasteland_casualties_1.png: a camp tent with a table, chair and radio; the target body bag
  (orange, on the floor left of the chair) is half hidden by the table (weak shot).
- wasteland_trespass_1.png: two "WARNING no trespassing" signs on a plank wall outside.
- wasteland_wire_1.png: concertina coils and a trash bag against a wall on the sidewalk.
- wasteland_sandbags_2.png: sandbags left on a curb, wrecks and a jeep beyond.
- wasteland_flag_1.png: the US flag decal on a military building's wall over a counter (run 1).
- wasteland_aerial.png: the district from 80 above: streets, rooftops, water to the east.
- topdown_entry_0_240.png, topdown_convoy.png, topdown_motorpool.png, topdown_fighting_0_192.png:
  seed 1 server world, prop boxes with fronts as arrows (described in docs/wasteland.md section 5).

## city_dressing_v0.53/ (dev test cityprops, city cell 368,6, 12 Oktober 2026)
- sheet_cityprops.png: 27 shots of the fresh cell (street props as before plus the new interior kinds).
- cityprops_desk_1.png: office desk: grey metal table along the wall, beige CRT monitor on it facing
  the room, black office chair in front looking at it. Right.
- cityprops_desk_2.png: a monitor on a white stone counter in a kitchen like room, a wood table and
  chair to the right (likely converted furniture with our monitor counted) [inferred].
- cityprops_table_1.png: wooden trestle table with a dining chair facing it in a grey room. Right.
- cityprops_squat_1.png: squatters' lantern on the floor at the end of a narrow aisle between white
  fixtures (a washroom); the rest of the camp kit did not fit there. Odd spot, harmless.
- cityprops_present_1.png: a red present on the floor and a lantern on a wooden counter in a flat.
- cityprops_officechair_1.png: an office chair beside a grey counter by a window.

## city_dressing_v0.51/ (12 Oktober 2026)
- topdown_jam_crash.png (tools/props/topdown.py on the dressed seed 1 server world; road dark, walk
  light grey, grass green, buildings grey; red boxes cars, orange signs / traffic lights, blue other
  props; yellow line = front): left a jam at x 0 z 200..247, three cars in a column all pointing south
  toward the city exit; middle the crossing at 0,128: a car through the middle pointing east, traffic
  light arms (orange) from the corners over the roads; right a jam car at 240,128 pointing east.
- topdown_lots.png (v0.51.2): left the bare lot at -176,144: two cars in their rows, noses toward
  the middle line, on grass; right the dump at 48,96: junk along one edge.
- topdown_crossing_crash.png (v0.51.1): the crossing at 0,128: first car through the middle pointing
  east, the second from the north pointing south with its nose at the first one's side, under a
  traffic light arm (arm props count as their pole under the arm).
- sheet_cell180.png: cityprops cell 180,6: a power pole and a bicycle at the curb, graffiti on an
  outer wall at the street, a skeleton against a garage wall, TVs, washers, EXIT lights, stop and
  speed signs; three shots still behind hedges (camera fixed after: leaves block the view).

## facing_v0.50/ (dev test facing, 12 Oktober 2026)
- facing_<prop>_s.png / _n.png: each prop at metadata 2, 3, 4, 5 west to east on wool pads magenta,
  light blue, yellow, lime; camera south (fronts pointing S show their face) / north. Road signs 20,
  60, one way, stop: meta 3 shows the face from the south, meta 5 the back from the south: all road
  signs E S W N (the judge's W N E S for 60 / one way was wrong). Body bags black and blue: same model,
  identical at every meta. Work light tower and generator: not readable from these two sides.
  Rain and wandering NPCs in some shots (no weather / mob control in that test yet).

## city_dressing_v0.50/ (city dressing on, dev test cityprops, cell 92,6, 12 Oktober 2026)
- sheet_cityprops.png and cityprops_<kind>_<n>.png, each taken from where the prop's verified front
  points (a wrong facing would show its back). newsstand_1: four newspaper boxes in the curb row,
  windows toward the buildings, road behind. stopsign_1 / _2: STOP on the right curb before the
  crossing, face toward the approaching lane. speedsign_1: 30 on the right curb facing traffic.
  trafficlight_1: corner pole, lenses toward the camera; mailbox_1: blue mailbox beside newspaper
  boxes, front to the buildings. exitlight_1: EXIT over a door, facing into the room. tv_1 / tv_2,
  vending_1 / _2, payphone_1: on / against walls facing the room. skeleton_2: a skeleton against a
  wall beside a chest and trash bags. graffiti_1: a tag on an inner wall. Camera misses (not the
  props): newsstand_2, bench_2, mailbox_2 behind a hedge; washer_1 / cctv_1 too small to judge.

## city_props_v0.49.0/ (converted city props fitted to their drawn size, dev test cityprops, 11 Oktober 2026)
- sheet_cityprops.png and cityprops_<kind>_<n>.png, city cell 76,6 (blocks 4896,416) of the dev
  world, gamma raised (unlit rooms). chest_1: a vanilla dungeon chest under the city (not ours).
  chest_2: a vanilla chest (a wood crate that fit nowhere) in a corner with a wood crate standing on
  it. chest_3 and ledge_2: the camera ended outside a wall (test camera, nothing to judge).
  light_1 / light_2: a DeceasedCraft street lamp's hanging lantern now a ceiling light under the
  lamp head (was a floor lantern in the air). chair_1: two chairs facing their desks; chair_2: a
  chair facing a counter. ledge_1: a pergola of upper slabs and fences (DC's own, not a ledge we
  made). wreck_1: one car centred in a DC street part's road, clear of the raised sidewalks.
- An earlier run (cell 48,6, dark, not kept) showed a plank ledge on a brick wall with a lantern
  standing on it (a DC wall shelf with a lamp), a vanilla chest in a stone corner, a ceiling light
  in a wooden room and a centred car.

## safezone_camp_v0.47.1_review/ (user review, 11 Oktober 2026)
- user_120.png (night, the sky test camp): a guard standing in the air over the scrap wall left
  of a watchtower (another guard correctly on the tower deck), civilians and survivors crowding at
  the tower ladder; chat "Safezone protection ends in 16 seconds!" / "Now entering a Safezone
  controlled by Nobody" (the client zone list works).
- user_121.png (day, the camp from /deciworldgen camp near a city): a guard (boonie hat, woodland
  camo, M16) standing on top of the wall by a torch post.
- user_122.png: the gate from outside: the "Entering Safezone!" text partly hidden by the lintel
  logs it stood on; inside, the market with traders, guards behind sandbags, a civilian lying on
  the street.

## city_refuge_v0.48.0/ (city refuge, dev test refuge, turns 0, 11 Oktober 2026)
- sheet_turns0.png: all 26 camera points. refuge_gate_outside.png: the wall with jersey barriers
  and wire along the apron, the gate with the green "Entering Safezone!" sign, the three storey
  concrete building behind. refuge_main_road.png: the closed fence gates from inside.
  refuge_chicane.png: the entry channel from above, sandbag walls, two staggered barrier lines,
  a guard each side, the red exit sign over the gate.
- refuge_front.png: the entrance, double metal door, sandbag vestibule, wall flag, boarded
  ground floor windows. refuge_aerial.png: the whole block in the sky test, roof with bulkhead,
  flag and water tank, tents (sand / brown / green / grey) south west, asphalt yard.
- refuge_market.png and refuge_stall_*.png: the hall along the stone brick aisle, striped
  awnings, a trader behind each two table counter, goods at the back. refuge_lobby.png: the
  registration desk with monitor and chair, the desk guard, the door. refuge_clinic.png:
  stretchers, patient beds, a shelf. refuge_canteen.png: tables with chairs, kitchen behind.
- refuge_dormitory.png: rows of beds, civilians. refuge_command.png: radio desks in the second
  floor hall. refuge_armory.png: shelves, weapon cabinets, ammunition. refuge_bunks.png: guard
  beds and crates. refuge_roof.png: spotlight, flag, bulkhead door, sandbag nests with guards.
  refuge_stair.png (reshot after moving the camera): run A of the stair climbing along the north
  wall through the opened slab, the hall's first stall on the right.
- refuge_tent_inside.png, refuge_distribution.png (tables, supplies, sawhorses, the car),
  refuge_west_yard.png (generator, barrels, locker), refuge_east_yard.png (raised beds),
  refuge_north_yard.png (the narrow strip behind the building, bins).

## safezone_camp_v0.47.2/ (after the review)
- gate_outside.png: the entry sign now one block outside the lintel (its stripes over the gate).
- clutter_west.png: a black civilian car wreck parked along the west wall, the generator yard and
  crates, a watchtower behind. clutter_east.png: barrels, a tyre stack and a jeep in the east
  strip by the garden. aerial.png: the camp with the new vehicles and stacks in its corners.

## safezone_camp_v0.47.1/ (camp people kept, awning fronts, camp command, 11 Oktober 2026)
- camp_market.png (dev test camp, turns 0): the market street, awning front edges coloured
  (green, blue, red left; orange, yellow, brown right), a trader behind every counter, guards
  and civilians further up the street.
- campcmd_aerial.png (dev test campcmd, /deciworldgen camp 1 on real ground at -21000, 67,
  -19700): the camp turned 90 on grass land, graded, people on the plaza and street, a highway
  passing north of it (the command does not avoid highways); the lower half of the frame is the
  camera inside the hillside south of it.

## safezone_camp_v0.47.0_review/ (user review of the survivor camp, 11 Oktober 2026)
- user_119.png (user's dev client, creative, the dev test sky camp in deciworldgen_devtest):
  the market street from the gate end, both rows of stalls with counters, goods (medical cases
  left, water pallet right), the medical tent at the end, NO people (the camp test had removed
  them at its end); the awnings white from this side (the front column was a white stripe).

## safezone_camp_v0.47.0/ (survivor camp, a safezone outside the cities, 11 Oktober 2026)
Dev test `camp`, sky build at -20000, 230, -19700, turned 90 degrees (turns=1), seed 42, all
PASS. Files camp_<point>_turn1.png and the sheet.
- gate_outside: the scrap wall (rusty and dark metal panels, spruce boards, log posts with
  torches), concertina rings with a gap before the gate, the log gate frame with 5 closed fence
  gates, "Entering Safezone!" floating over the lintel; inside, the market awnings.
- gate_inside: two guards (marine forest camo) behind sandbag stacks beside the gate.
- tower_ladder / tower_deck: the ladder up the plank tower on the camp side; the deck railing,
  log corner posts, the next tower across the camp, tents below.
- main_road / market: the street of 6 stalls with striped awnings (red, blue, green, orange,
  yellow, brown with white), a trader behind each counter, trader icons over their heads.
- stall_<trade>: one stall each (ammunition, medical, heavy guns, guns, convenience, food):
  the counter of two wood trestle tables, the trader behind it, goods at the back.
- plaza / campfire: the fire in its slab ring with benches, civilians and an armed survivor
  around, the medical tent and a watchtower behind (the campfire camera was 2 blocks behind a
  bench: its back filled the frame; moved back and up after this run).
- medical_inside: white tent, stretchers both sides, medical crates at the far end.
- tent_inside: a family tent: bed, footlocker. latrines: two outhouses and a wash cauldron.
- generator: generator, barrels, tyres, crate, a light tower by the wall.
- garden: fenced plot of carrots / potatoes / wheat around a water channel.
- aerial: the whole camp from the south: wall ring, towers, market street, plaza, tents.
- seed1_S6_3_top.png (tools: worldcheck top block per column, 4 px per block, real worldgen,
  seed 1 server world at 790..890, 375..475): the camp turned 90 (gate west), red wall ring
  (Decimation metal panels), brown towers in the corners, grey market street from the west, white
  tents and medical tent, the garden with its water line bottom left; grass and trees around,
  untouched outside the graded ring.

## lc_bridge_v0.45.1/ (bridges as streets over water, 11 Oktober 2026)
User review of 0.45.0: "the bridge show up, just need change the design so the bridge match with other
road". Dev test `bridge` at 30144,64,1696 (along z, dev world), all PASS.
- bridge_deck.png: standing on the land street looking onto the bridge: the same DeceasedCraft
  asphalt, dashed centre line and raised sidewalks run on across the canal without a step; iron bar
  railings on the sidewalk edges; a parked wreck on the street.
- bridge_aerial.png: from above, the street crossing the canal at street height, the canal running
  on beside a hotel between quay walls.
- bridge_side.png: from the canal side: stone brick quay wall with railings, the bridge's railing
  and lamps on the street level behind.
- bridge_under.png: at water level under the deck: stone brick deck, a pier, canal water.
- sheet_bridge.png: the 4 shots as one sheet.
Server check (seed 1 fresh, not shot, read with worldcheck): straight and crossing bridges carry the
land street's blocks row by row; broken wasteland bridges (120,64,136) have an open hole across
5..10 / along 4..11 down to the water, sidewalks kept.

## lc_bridge_v0.45.0/ (rivers through cities and bridges, 11 Oktober 2026)
- seed1_worldmap.png (tools/worldmap.py, fresh seed 1 server world, city rivers on): the natural
  winding rivers outside, one enters the spawn city at the top middle and leaves at the bottom:
  inside the city its course is canal chunks (blue squares) under the buildings' grid.
- seed1_top_96_96.png / seed1_top_-16_-48.png (top block per column, 3 px per block; blue water,
  red iron bars, dark road, light grey stone brick, grid line every chunk): canal chunks with
  railings on their land sides, bridge decks on the street chunks across them, a black road deck
  on the crossing at 200,200. First version (middle point only) gave a diagonal chain of chunks
  touching at corners: ponds; the 3 x 3 sample version is continuous.
- run1_top_24256_1488.png: the first dev test bridge (24336,64,1536, dev world region 95,6): city
  corner, a highway arriving at a crossing deck, canals, bridges on both streets.
- run1_bridge_aerial / side / under.png: bridge_1 from above (quartz deck, lamps, canal beside);
  from the street side (iron bar railing with hedge, lamp posts); from under the deck at water
  level (canal, stone brick quay walls, deck above). run1_bridge_deck.png is wrong: that span
  started at the city edge, the camera stood inside the hill outside (the test now picks spans
  with city streets at both ends). run1_sheet_bridge.png: the four.

## lc_rail_v0.44.0/ (underground railway, dev test `rail`, 11 Oktober 2026)
Station 17768,47,1672 (region 69,6 of the dev world, seed 1, street level 70), fresh chunks.
- rail_entrance.png: the street from 10 blocks west at street + 8: the Lost Cities station
  entrance plaza across the street (two white kiosks with iron bar fronts, stairs down on both
  sidewalk sides, grass strip and planters in the middle); the road ends at it.
- rail_platform.png: from the west platform chunk looking east along the north platform: quartz
  platform, track trench with the rail, cobwebs, hedges, the station hall lit by our ceiling
  glowstone (before the lights it was black: the parts' glowstone sits under slabs).
- rail_stairs.png: from the track looking north and up: quartz stair flight under the glowstone.
- rail_shaft.png: from the street looking down into a stairwell: iron bar railings, quartz
  steps, the light blue glass water wall (was water, flooded the station).
- rail_tunnel.png: a straight tunnel chunk east of the station, looking east: one track on
  road blocks, slab walkways both sides, terracotta walls, glowstone every 4 blocks; the bright
  far end is client render lag (the world there is rock).
- rail_end_1..4.png: the four line ends (west, east, north, south) from 10 blocks inside: two
  tracks, benches, crate, cobwebs and a rubble pile with the buffer at the closed side, so every
  turn is right; the single tunnel track does not meet the end part's two tracks (DeceasedCraft's
  parts). Sky seen through the rubble in end_1 / end_3 is again render lag (rock behind, checked
  with section.py).
- rail_crossing.png: the crossing looking south along the north south line: x rail across.
- sheet_rail.png: all of the above. Earlier runs (not kept): flooded tunnels with stranded water
  and lava diamonds from springs (fixed by denying springs and lakes near rails).

## aimdrift_v0.43.1/ (gun creeps off screen while aiming, 11 Oktober 2026)
- user_116.png: the user's dev client, AK-74 + ACOG aimed: the scope sits down right of the
  screen centre, a sniper NPC in view at the centre. user_117.png: two minutes later the gun is
  nearly gone (only the top of the scope in the bottom right), four NPCs in view.
- sheet_fixed_npc_in_view.png: dev test aimdrift after the fix, 1160 x 650 window, elite sniper
  NPC in front: at 3 s and 15 s the ACOG centred, reticle on the crosshair, identical pictures.

## aimdrift_v0.43.2/ (aim test with gun and sight changes, 11 Oktober 2026)
- sheet_changes.png: dev test aimdrift, 1160 x 650, elite sniper NPC in view: AK-74 + ACOG at 3
  and 15 s centred; then changed while aiming: AK-74 + red dot (no zoom, Decimation's own aim
  pose, sniper NPC at the right), M4A4 + 2x centred, AK-74 + ACOG centred again.

_v0.14.png
Top down map (`tools/worldmap.py`), seed 1, Decimation world type, 800x800
around spawn. Grey squares = Decimated City sectors full of building
footprints; brown = military; yellow tan = dead plains / suburbs; green =
overgrown forest and plains (north west, east); blue winding rivers that
stop at city / military edges; small blue lakes in dry areas.

## street_v0.15/ (autotest street views, 0.15 / 0.16)
- autotest_0: from the east sidewalk of a north-south street looking north:
  flat dark asphalt, dashed white centre line running ALONG the street,
  light grey sidewalks, street lights on both sides with arms over the
  road, tall ruined apartment blocks (pink / brick / white palettes) with
  vines, a grass lot with a dead tree on the right.
- autotest_1: side-on view of a street light (meta 2): the arm bends to
  the RIGHT = east = over the road. Confirms the facing table in
  docs/building_design.md "Street life". A street bin on the inner
  sidewalk, a row of parked wrecks in the lane.
- autotest_2: from the west sidewalk looking east across the street:
  cross-section flat after levelling; the lot behind ramps up in steps of
  grass terraces to a white office tower.

## audit_v0.16/ (building audit, round 2 step 4a, see docs/building_audit.md)
- audit_apartment.png (b0_1_0, 3 floors, 17x14): facade = small grey stone
  brick box, gravel path, vines, no balconies or canopy. Ground storey: a
  1 wide corridor of birch plank walls, oak plank floor and ceiling, a full
  dark stone block in the way; claustrophobic, empty. Storey 1: same birch
  box, a full mossy cobblestone block in the doorway. Roof view: flat roof
  with scattered blocks.
- audit_office.png (b0_1_1, 11 floors, 24x26): facade = tall pink concrete
  grid, window rhythm good, vines. Ground AND storey 1 = identical open
  plan: rows of grey metal desks with black office chairs, a few wooden
  X-leg tables mixed in, grey stone floor, dark grey ceiling grid, full
  mossy cobble blocks standing between desks. Roof: parapet, snow / leaf
  dots, nothing else.
- audit_shop.png (b0_1_2, 2 floors, 12x15): facade = brick front with a
  small sign-like panel, car park with 3 wrecks in front, slab walkway.
  Ground storey: grey metal shelving with wooden goods on it, light grey
  floor, dark plank ceiling (good, reads as a store). Storey 1: an empty
  oak room with orange clay / mossy walls and a full mossy cobble block in
  the middle. Roof: sandstone top with a few blocks.

## plans_v0.16/ (tools/floorplan.py, seed 1)
Per storey plans, 14 px per block: dark grey = wall, light blue = glass,
white = air, letters = props (T table, c chair, S shelf, B bed, k cooking,
X crate, x boxes, V vending, t trashcan, m mailbox, r rubble, / stairs,
L ladder). Key observations are in docs/building_audit.md (empty ground
storey units in apartments, identical office storeys, b-4_1_0 shop upper
storey unreachable = the ladder bug fixed in 0.16.x).

## gallery_v0.16/ (prop gallery, round 2 step 4b)
83 views, 3 blocks each, `views.txt` maps view number -> block names
(left to right). Every block is described in docs/prop_catalogue.md (look,
size, facing at meta 3); read that, not the images. All views checked:
the first attempt (5 per view, far camera) was deleted; views 0, 23 and
70..76 were re-shot (platform not rendered yet, camera turned by a touched
mouse, doors placed without their upper half). Camera is now locked per
tick and `-Ponly=` re-shoots single views.

## audit_v0.17/ (after step 4d.1: doors + low debris)
Same 3 buildings and camera spots as audit_v0.16.
- apartment: dark wood office doors with a small window stand in the unit
  doorways along the corridor (both halves, flush in the wall); corridor
  debris is now a brick / stone slab instead of a mossy cube. Walls,
  floors and ceilings are still all birch / oak planks (step 4d.2).
- office: desks unchanged; floor debris now brick and stone slabs;
  cobwebs were standing between desks (fixed right after: cobwebs only next
  to walls). Ceiling still dark grey, no lights (step 4d.2).
- shop: as v0.16 (shelves with goods), stockroom metal door.
- audit_*_in.png (after step 4d.2 surfaces, ground storey + storey 1):
  apartment corridor = white / pink plaster wall panels with a skirting
  course, black tile floor (looked like a dark tunnel because the floor
  above shows as ceiling -> corridor floors switched to light stone),
  office doors. Office = grey carpet floor, the carpet of the storey above
  as a grey ceiling, light panels / vents hanging as dark fixtures in a
  grid, pink plaster walls. Shop = shelving aisles under hanging lights,
  stone stockroom floor, white plaster walls upstairs.

## audit_v0.18/ and plans_v0.18/ (after step 4d.3 apartment rooms)
- audit_rooms.png: audit_1/2 = inside an apartment living room: kitchen
  run along the side wall (tall grey fridge, furnace oven, washing
  machine, cauldron sink) on black / white checker tiles beside an oak
  plank living floor, plaster walls, office door; the flat above showed
  its checker kitchen floor as a checker CEILING (fixed: upper kitchens use
  light stone). audit_5/6 = office open plan, chairs at desks, carpet,
  hanging fixtures. audit_9 = shop aisles.
- audit_apartment.png: facade unchanged; corridor with light stone floor
  and plaster walls.
- plans_v0.18 b-4_0_1 (24x25): every flat has F fridge, u sink, M washer,
  T + c dining, V TV + h sofa + rug, B bed + C chest, o toilet + u sink in
  bathrooms, D doors. b-4_0_2 (18x16): small flats (bed, fridge, sofa),
  ground storey = lobby (plants p, bicycles y) + a furnished flat.

## audit_v0.19/ (storey height 5, own ceilings)
- audit_s5.png: first try, ceilings of BlockCeiling_3/4 tiles: rendered as
  a dark grey lid (undersides are shaded); facades 25% taller, office
  tower window rhythm now 2 solid rows between window bands.
- audit_s5b.png: final: white plaster ceilings (shaded light grey) over
  the kitchen and living room, plaster lintels above doors, offices with
  plain ceilings and hanging fixtures. Remaining: outer walls still show
  stone brick facade on the inside (step 1b lining).

## sets_v0.20/ (furniture set preview, -Psets)
Each set alone in a plaster bay on a spruce floor, seen front-on;
set_<n>.png, sets_sheet_*.png (8 per sheet, names on top).
- dresser: double chest with a plant pot. dining_table: X leg wooden table,
  a chair each side facing it. bed_along_wall: chest + red bed lying along
  the wall, pillow at the chest. lobby_bin_plant: bin + leafy plant.
  lobby_bikes: 2 bicycles + plants (first version overlapped; now a gap).
  kitchen_run_4: tall grey fridge, cauldron sink, quartz counter, furnace,
  oak wall cabinets above. bath_tub: 2 cauldrons. wardrobe: first 3 high
  spruce block (read as a wall chunk) -> now 2 high dark oak cabinet.
  kitchen_run_6: fridge, counter, sink, counter, oven, washing machine,
  dark oak cabinets above. desk_chair: chair facing an oak block desk with
  a pot. closet_boxes: empty metal shelving + cardboard boxes.
  bed_nightstands: bed head to the wall between 2 chests, pot and radio on
  top. living_couch_wall: spruce stair couch facing the room, light grey
  rug. bath_toilet_sink: white quartz stair toilet + cauldron basin.
  living_tv_small: TV on a spruce cabinet, oak stair sofa facing it.
  living_tv_sofa: TV + stereo + pot on a dark oak cabinet, red rug (brown
  was invisible on the floor), dark oak slab coffee table, spruce sofa.
  dining_small: table + chair. kitchenette_3: fridge, sink, oven, cabinets.
  bed_single: bed + chest nightstand. living_tv_armchair: TV on cabinet,
  oak stair armchair. kitchen_run_5: fridge, counter, sink, counter, oven.
- sets_fix.png: the 4 sets after the fixes above.
- audit_v0.19/audit_sets_v0.20.png: in buildings: a kitchen run with wall
  cabinets and a dining table in a flat; offices unchanged.

## flats_v0.20/ and flats_v0.21/ (-Pflats: 3 apartment blocks, living / bedroom / ground)
- v0.20: b0_1_0 (17x14): long oak floored hall (leftover space next to
  the stairs, labelled living by the fallback), door lintels over empty
  doorways, a debris slab mid floor; bedroom: bed along the window, chest,
  grey carpet floor; ground: stone lobby with bikes. b0_1_3 (12x22, 8
  storeys): every storey an empty hall with windows: NO flats (the stair
  core took the width). b0_3_1: living with a colour-bar TV close to the
  camera, bedroom with dark wardrobe, chest, bed.
- v0.21: same spots before the narrow-block fix; b0_1_3 still empty halls
  (fixed after these shots: ladder for blocks under 16 wide).
## sets_v0.21/
- sets_kitchen.png: kitchen runs with a 2 high silver iron fridge, one
  counter material, dark cauldron sink with a tripwire hook tap above,
  furnace oven, wall cabinets with trapdoor door fronts; kitchen_run_6
  ends with the washing machine. lobby_bench: 2 street benches + plant.


## study_usa_coast/ (-Pstudy on the USA coast map, 8 Okt, roof filter + night vision)
- Rerun after the camera fix; the first run had an Options menu in view
  1, black rooms and roof spots. views.txt has every spot. Night vision
  swirls (blue) sit in some frames; replaced by gamma 8 full bright
  afterwards (tested clean on 4 views).
- 4 (b4 s0): waiting room, rows of wooden chairs facing a long white
  counter with a purple stripe, ceiling light panel grid, white tiles.
- 5 (b4 s1): corridor, white office panels with a dark dado stripe,
  doors. 6 (b4 s2): empty hall, plank wall decor, blood decal.
- 7, 8 (b5): military hall, dark walls, oak fence queue rows, ladder,
  blue grey carpet, stone brick room boxes.
- 9 (b6 s0): office cubicles of 2 high cracked stone partitions, desk +
  computer, ceiling light strip + vent.
- 10 (b8): weapon room with metal shelves, a glass case, posters, a
  dead body prop. 12 (b9): brick corridor, red carpet, cracked glass.
- 14, 15, 16 (b12): store / office with metal shelves, office chairs,
  white walls, stone brick pillars, light panels, dirt and leaf patches
  on the floor (decay), desk + computer, camo vehicle and crates.
- 17 (b13): long hall, tables in a row, rose planters along the wall.
  18: bookshelves, red couch, cracked window. 21 (b14): water channel.
- 26 (b29): office with white chairs, flooded floor, exit sign, office
  chairs. 28: helicopter on the roof.

## study_decicraft/ (-Pstudy on Decicraft spawn town, 57 views)
- Mostly a converted vanilla city: 0..1, 7..10 street views from under
  overhangs (lane lines, sidewalks, hedges, towers, red cranes).
- 2..5 (b3): mall like hall, white pillars, wooden floor, cots (camp).
- 12..16 (b24): market street with awnings and cobwebs; supermarket with
  double metal shelf aisles, planks floor.
- 17..25 (b25): dark tower corridors, plank floor, nothing inside.
- 26..30 (b26): office floors with desk islands of computers, white
  floors, glass atrium with balconies.
- 34..47 (b30, b34): hotel / cell corridors, rows of security doors,
  redstone lamps in the ceiling, empty.
- 54..56 (b62, b63): empty office hall, stone corridor with lamps.

## audit_v0.23/ (-Paudit, 0.23.0: offices and shops 6 high)
- 0..4 apartment b0_1_0 (17x14, storey 5): unchanged look: stone brick
  facade with vines, corridor with bike and benches, plaster walls,
  kitchen with tiles, roof with leaves.
- 5 office b0_1_1 (24x26, 11 floors, storey 6) facade: camera ends up
  close above the window heads, mostly window recesses and vines.
- 6, 7 office storey 1 and 2: open plan desk rows with office chairs,
  3 high window bands, ceiling vents and light panels under the ceiling,
  stone brick stair core; reads roomier than the 5 high offices.
- 8 office ground: desk rows, stair core, broken glass bits in view.
- 9 office roof: stone with leaves, parapet.
- 10 shop b0_1_2 (12x15, 2 floors, storey 6) facade: orange clay, tall
  front, cars parked in front. 11: stockroom with shelves and boxes.
- 12, 13 shop storey 1: camera faces a plaster wall with a door (stock
  partition) from up close: poor camera spot, not a building defect.
- 14 shop roof with leaves.

## dc_proof_v0.23/ (DeceasedCraft buildings converted to 1.7.10, pasted at y 150)
- tools/lc2schem.py + lctranslate.py, `/deciworldgen paste`; buildings
  0 apartmentsmalla, 1 residentiala, 2 policeoffice1, 3 fastfood,
  4 office1a, pasted at x -140, -114, -88, -62, -36 (z 20). Cameras were
  naive (fixed spot per storey): many face a wall or the sky.
- 0 apartment ground: white walls, light tile floor, doorways, glass
  doors, wooden furniture: clean, detailed. 1..3: closet corner + door.
  4: facade, white quartz bands, dark stone, window strips.
- 5 house: brick, dark oak, chairs + table, birch floor.
- 10 police ground: glass front, stone counters, metal doors, tiles.
- 15 fast food: tables with chairs, glass + brick front, counter, door.
- 20, 21 office: stone counters, desk with monitor and chair, glass walls.
- 24 office tower exterior: glass curtain wall, stone core, floor bands.
- 11, 13, 22: the camera ended on the ground street (teleport issue),
  not the pasted buildings; 12, 23: inside a wall.

## wasteland_legacy_v0.23/ (all 14 legacy deadzone buildings converted, pasted at y 150, x -420..84, z 40)
- Order (building index): 0 warehouse1, 1 warehouse2, 2 factory1,
  3 factory2, 4 carfactory1, 5 lab1, 6 bunker1, 7 multi_warehouse,
  8 multi_factory, 9 multi_pumpjack, 10 multi_derrick, 11
  multi_militarybase, 12 multi_militarycamp1, 13 multi_laboratory1.
  "storey 99" = aerial view (pitch 40).
- Every building stands on its own pale ground plate (dried salt ->
  sandstone: reads too yellow, a grey / white stone would match salt
  flats better).
- 0, 1 warehouses: pitched grey roofs, plank walls, sandbags around.
  2, 3 factories: dark metal roofs with vents, machinery, chimneys, red
  stripe walls. 4 car factory: grey block with red band, wrecks outside.
  5, 6 lab and bunker: small open compounds with crates, vehicles, sandbags.
- 7, 8 multi warehouse / factory: long sheds with skylights, chimney and
  crane tower on the factory.
- 9, 10 pumpjack / derrick: oil rigs with derrick towers, tanks, pipes.
- 11 military base: walled compound, barbed wire fences, sandbag walls,
  watchtower, military truck in the court (19), parapets (18).
- 12 military camp: open camp with tents / crates on a plate.
- 13 laboratory complex: big walled block with the glass dome ring,
  corridors (22), a room with rows of chairs (23).
- Interior cameras (11) from tools/mapbuildings.py: some face walls.

## wasteland_cellars_v0.23/ (cellars of the legacy wasteland buildings, ground floors at y 200)
- Converter now generates floors like Lost Cities (cellars included);
  camera in creative (no suffocation inside blocks).
- 0 lab1 cellar: sandstone lined room, computer desks, bookshelf, crates.
  1 bunker1 cellar: brick room, computers, gun rack, desks, chairs.
- 2..5 military base, 4 levels (y 176 / 182 / 188 / 194): brick and
  stone brick corridors, rows of seats, cell bars, ladders between
  levels, crates, desks with chairs.
- 6..16 laboratory complex (15 cellars, y 110..200): bottom (-15) a big
  hall with sandstone blocks and machinery, glass partitions; -12, -9,
  -6, -3 show the same office / lab layout (one part used for a floor
  range in the data): rows of chairs and desks, glass walls. Several
  manual spots (x 28, z 60) face a wall.

## lc_alignment_v0.23/ (alignment test scene, tools/lcscene.py, pasted at -120 70 30)
- Low street (level A) road surface y 70, high street (level B) y 76,
  buildings' ground floors at y 70, military base cellars below.
- 0 along street A: road level with the building fronts on the left
  (sheds with dead bushes on their salt plates), our generated city on
  the right (the scene was pasted inside it).
- 1 lab / factory fronts: plates and doorways flush with the road.
- 2 military base: outer wall with razor wire right at the street edge.
- 3 the deadzone stairs part from street A: a wide stone staircase with
  brick sides and railings climbing to street B: lines up.
- 4 from street B looking back east; 5 overview from above (mostly our
  city's tower in view, poor spot).
- 6 base courtyard passage at street level. 7, 8 camera inside cellar
  walls (see-through), bunker levels with cell bars visible.

## lc_city_v0.24/ (first Lost Cities style city, seed 1, 20 views, all valid)
- 0..11 street views (pairs: along a north-south street, then facing the
  block fronts): asphalt chunk wide streets flush with the buildings;
  legacy deadzone sheds / factories / military base wall with razor wire
  (0..3), white modern blocks (4, 5), timber framed / glass fronted
  current beta buildings (6, 7), dark offices (8, 9), brick and white
  facades (10, 11). City edge: the land beyond rises as a grass topped
  dirt cliff next to the street (0, 2, 10).
- 12..19 stairs between street levels (64 -> 70): wide stone stairs with
  brick sides and railings (12, 13), sandstone terraced steps with stone
  side blocks (14, 16, 19), a brick terrace with stone steps and a blue
  door wall (15, 17), an underpass like part (18).

## lc_city_v0.24.1/ (street dressing + city edge ramp, seed 1, 19 views, all valid)
- 0..11 streets: grey stone sidewalks 3 wide on both sides flush with the
  asphalt, dashed white centre line, street lights on the road edge with
  arms over the road (staggered sides), benches facing the road, bins,
  scattered trash bags; a wreck now and then (an earlier run of this
  version had a hash bug: wrecks and bags everywhere, no lamps; fixed).
- 12..15 stairs between levels as before.
- 16..18 city edge (north side, z 6): the land outside is ramped down to
  the street over 10 blocks; it reads as grass terraces 1 block high up
  to natural ground about 12 higher. Works, could be smoother / wider.

## lc_interiors_v0.24.1/ and lc_interiors_v0.24.2/ (interiors of converted city buildings, 24 views each, same spots)
- v0.24.1: offices with desk rows, monitors and chairs, restaurant tables,
  kitchens; BUT rows of sandstone cubes on floors (7, 13, 15: basalt
  translated as salt flat) and many open window holes (glass panes were
  skipped: the frying pan rule matched "pane").
- v0.24.2 (translator fixed): glass walls and windows back (4, 6, 8, 14,
  23), stone floors instead of sandstone cubes, stone bricks grey (were red
  brick). Several camera spots face a wall or stand in a corridor (3, 5,
  10, 16, 17).

## lc_superblock_v0.24.3/ (superblock with the courtyard office tower, 0.24.2 code)
- 0 from the street next to a legacy military base wall: the 192 high
  courtyard office tower (4 x 4 chunks), lamps, other towers behind.
- 1 from above (y 140): a real skyline: white and glass towers, the
  chunk street grid with sidewalks and stairs, lots; reads like the
  DeceasedCraft screenshots the user sent.
- 2 street level between the tower's columns and a pink glass fronted
  block; 3 a rooftop terrace of a neighbouring tower.

## lc_edge_v0.24.4/ and lc_edge_v0.24.4b/ (city edge ramp, seed 1, 7 views each, all valid)
Same spots both: 0..2 from the north sidewalk (z 6) looking north at
x -150, -60, 20; 3 aerial over the city's north west corner; 4 and 5 from
the ramp looking south at the city; 6 aerial over the north band.
- v0.24.4: 0, 1 grass terraces 1 block high in long straight rows parallel
  to the street, up to the natural ground; 2 a natural hill with trees east;
  3 the corner ramp rounded, no cliff, dirt shows on cut faces; 4, 5 the
  city seen from the ramp, land meets the sidewalk; 6 smooth contours.
- v0.24.4b (contour wobble): 0 the first terrace now steps in and out;
  rows wander instead of running straight; rest as v0.24.4.

## lc_highway_v0.25.0/ and lc_highway_v0.25.0b/ (highways, seed 1, 8 views, all valid; b = with side ramps, same spots)
- 0 from the city street (8,67,24) north: the street runs on straight into
  the highway, hedge median in the distance.
- 1, 2 on the deck at z -150 north / south: dark asphalt (BlockRoad), leaf
  hedge median and side hedges, stone wall lamp posts with coal block heads
  and slab arms; 2 an adboard tower with ladder in the median, low grass
  slopes beside the road.
- 3 aerial side view of the 2 bridge chunks (z -112..-97) over a ravine:
  rails, a crash variant with white blocks.
- 4 under the bridge looking north west: stone brick pillar, natural stone
  slope (bridge chunks get no side ramp).
- 5 deck at z -300 north: bridge over a lake with iron bar rails, a wreck
  in the lane.
- 6 the east west highway east of the city (250,68,8) looking east.
- 7 v0.25.0: aerial, straight highway across the land; v0.25.0b: aerial
  from the east (36,80,-170): road with lamp arms, graded grass beside it.

## lc_tunnel_v0.25.0/ (highway tunnels, seed 1, 4 views, all valid)
- 0 x highway at 400,67,8 looking east: road in a cut between graded grass
  slopes, a lit tunnel portal ahead.
- 1 aerial over that tunnel: the road disappears under the hill.
- 2 z highway at z -440 looking north: adboard tower, a short tunnel just
  before the northern city, towers behind.
- 3 aerial near the northern city: the highway reaching the city edge.

## lc_decor_v0.26.0/ (parks, street scenes, fronts; seed 1, 12 views, 10 valid)
- 0 (-200,66,8) west: a street scene with a blue tarp shape on the road
  edge, bench, terraces of the city edge on the right.
- 1 (170,66,8) west: concrete roadblock across the road ahead; leaf hedge
  front on the left sidewalk.
- 2 (-136,66,136) west, deadzone: debris scene on the road, wide stone
  sidewalk in front.
- 3 (-248,66,172) north: legacy roadblock (stone blocks) in the road,
  hedges, the deadzone wall with razor wire on the right.
- 4 INVALID for parks: looks down between towers (camera placement).
- 5 aerial over the parks at 16..47,16: a plaza with seating groups and a
  white fountain pool with water.
- 6 aerial over the deadzone park at -176,144: stone plaza with leaf
  planters, benches, scattered dead bushes.
- 7 (-248,67,60) north: a cream awning with pillars on the right building,
  roadblock ahead.
- 8 (8,67,60) north: two hotel fronts facing each other form a glass
  walkway over the road on stone pillars; leaf hedges cover both
  sidewalks.
- 9 INVALID: black, camera inside a building (z 64 is no street there:
  superblock).
- 10 (-60,67,8) west: plain street, a wreck in the lane.
- 11 (-248,67,100) south: plain street with lamps, bin, bench.

## lc_streets_v0.27.0/ (district street parts, seed 1, 10 views, 9 valid)
- 0 (-200,67,8) west, office district: asphalt with white edge lines and a
  double dashed centre line, raised sidewalks with curbs and a paving strip,
  lamp posts; the blue tarp scene ahead.
- 1 (170,67,8) west, suburb: hedge rows along the sidewalks, a roadblock
  scene ahead.
- 2 (-248,67,60) north: north south street (turned part): centre dashes run
  along the street (line meta flip works), wooden benches, a white zebra
  patch in the foreground.
- 3 (8,67,60) north: hotel fronts' glass walkway over the part street.
- 4 (-60,67,8) west: residential hedges, plain road.
- 5 (-248,67,100) south: like 2.
- 6 aerial over a crossing near (-30,-20): white crossing marks (X pattern
  of quartz), wrecks, houses with brown roofs.
- 7 aerial at (8,90,240) north: a wide stairs part between levels, the
  deadzone district (sand bags, crates) on the left.
- 8 INVALID / unclear: (-136,67,136) west shows a railed platform and a
  blue water like area with mirrored buildings.
- 9 (72,67,200) north: legacy street (our own lamps, centre line), stairs
  railing in the foreground.

## scope_v0.28.0/ (scope test, ak74 with 4x, aiming, dev client)
- scope_pip.png: original picture in picture scope: normal FOV, the glass
  shows the second camera view.
- scope_zoom.png: view zoom (FOV x 0.284 = 4x): world magnified, gun and
  scope the same size as in scope_pip, glass shows the centre of the
  zoomed frame with the reticle. An F3 debug overlay is open in this one
  (key pressed during the run, not the test).
- scope_mag1.png: control, zoom off (4x set to 1.0): gun the same size
  again, so the zoom does not scale the gun.
- sheet.png: the three side by side. (An earlier zoom shot with the glass
  not drawn showed a solid black eyepiece: replaced.)
- scope_normal.png (v0.28.1, camera zoom 4.00): world and gun zoom
  together, the scope ring fills the middle of the screen, the glass shows
  the zoomed road with the reticle (see-through look); eyepiece slightly
  above the screen centre.

## scope_v0.28.2/ (every sight, two windows, v0.28.2)
- scope_<854x480|900x895>_<reddot|2x|4x|8x|integrated>.png, sheet.png
  (top row 854x480, bottom 900x895, red cross = screen centre): the
  reticle sits on the screen centre in all 10; the glass is see-through
  (road and trees continue across its edge); 8x and the integrated aug
  scope fill the screen with the glass, ring edges at the sides.

## scope_v0.28.3/ (every sight, two windows, region copy, v0.28.3)
- same layout as scope_v0.28.2 (sheet.png, red cross = screen centre):
  reticle on the centre in all 10, glass see-through, the gun's front sight
  no longer visible inside 2x / 4x / 8x glasses. (A depth only glass try
  showed the front sight post inside the glass and a grey integrated scope:
  not kept.)

## scope_v0.28.4/ (sniper overlay from 4x, v0.28.4)
- sheet.png (top 854x480, bottom 900x895, red cross = screen centre):
  reddot and 2x as in v0.28.3 (gun, see-through glass); 4x, 8x and the
  integrated aug scope: black screen with a round view, a soft dark edge,
  the scope's reticle (4x / 8x posts and bars, aug circle) on the centre,
  no gun, HUD still on top. One earlier run's integrated 900x895 shot
  showed the player off position (test glitch, rerun fine).

## props_fps/ (prop fps test, 9 Oktober 2026)
- props_0..3.png, sheet.png: empty stone platform; the 15 x 15 grid in
  view (car wrecks and street lights dominate the skyline); the grid boxed
  in stone; the box alone. sheet_cached.png: empty / in view after the line
  of sight cache: props and a few NPCs still render. cityfps.png: the
  -Pcityfps view (city street looking north from z 120).

## lc_quality_v0.28.10 (converted building quality pass, 9 Oktober 2026)

Seed 1, Decimation world type, pack rebuilt with the fixed translator;
camera spots from tools/mapbuildings.py (2 per building, mid storeys).
- sheet_interiors_before_stairs.png: 30 views. Many land inside walls or
  look out of a facade (mapbuildings spots fit hand-built maps better
  than Lost Cities floors); the rest show offices (desks, computers,
  chairs, bookshelves), lobbies with light stone brick floors. No lamp
  blocks on floors. Ceilings have recessed redstone lamps (dark brown
  with orange) in a grid. Views 4 / 11 / 15: stairs of beige sandstone
  next to black asphalt blocks (black sandstone stairs still beige).
- sheet_stairs_after.png: the same stair spots after the stairs / slab
  fix: cobblestone stairs and slabs, no beige left. Doorway header of
  oak planks over red brick comes from the source building.

## lc_highway_v0.29.0/ (L link, seed 1, pregen 1100,1460)
- sheet_l_link.png: 0 aerial from the north west: the highway runs east
  then turns south at a corner, graded land and a pond beside it. 1 on
  the x run looking east and 2 on the z run looking north: hedge median,
  adboard tower ahead, lamp arms with redstone lamp heads. 3 side view of
  the z run's bridge chunk: iron bar rails over a small gully. 4 the z run
  entering city B: road continues into the city street, buildings ahead.
  5 blocked (camera against a median tower).
- sheet_l_link_b.png: 0 the x run entering city A (1066,67,1411 west):
  road narrows into the city street between graded grass terraces. 1 top
  down on the corner (1160,100,1416, top of image = south): the two runs
  meet in the crossing square, outer sides face open land, side cuts
  graded. 2 the bridge chunk from the east: rails, graded slopes.

## npc_tiers_v0.30.0/ (dev test mode npc, reused autotest world, lineups facing the camera)
- sheet_npc.png: 0 six bandits (light, light, medium, medium, heavy,
  heavy): red cap, hoodies, militia coats and helmets, rifles and an MG in
  hand, all different looks. 1 four soldiers: marine, forest, urban and
  black camo, helmets or hat, rifles. 2 the Soviet enemy military in
  spetsnaz gear with AK family rifles, one prone and firing (muzzle
  flash). An earlier run (not kept) was blocked by Decimation mechs that
  had gathered in the reused world; the test now clears other mobs.
- sheet_npc_eggs_v0.30.1.png: the 3 lineups again plus npc_eggs: the 8
  tier eggs in the hotbar: 3 tan bandit eggs (brown, green, black spots),
  4 blue soldier eggs (tan, dark green, grey, black spots), 1 yellow
  military egg with red spots.

## npc_shots_v0.30.3/ (dev test mode shots)
- sheet_shots.png: shots_wall, a stone wall 140 high in the air seen from
  the side, white "explode" puffs and dark stone crack bits where the
  bandit's shots stop on it; the pig behind it in the distance (camera
  angle off, but the impacts show).
- sheet_shots_rocket_far_v0.30.4.png: rocket phase from 14 blocks: an
  RPG-7 bandit and the pig on stone blocks in the air over the city,
  rockets only small dark specks between them.
- sheet_shots_rocket_v0.30.4.png: the same from 5 blocks, 3 ticks after
  each launch: the bandit holds the RPG-7 (clear in rocket_3), the rocket
  is a small dark body flying west with white smoke puffs behind it.

## npc_juggernaut_v0.31.0/ (dev test mode npc, fresh world)
- sheet_npc.png: npc_0 to npc_2 the bandit, soldier and Soviet lineups as
  before; npc_3 three juggernauts: bulky dark juggernaut armor with olive
  accents, machine guns in hand; npc_eggs the first 9 eggs in the hotbar,
  held egg named "Spawn Soldier (marineforest)".

## npc_elite_v0.32.0/ (dev test mode npc, fresh world)
- npc_elite.png: three elite military in the all black marine set with
  glowing green night vision goggles; a bipod machine gun and a long
  sniper rifle in hand; tall grass cleared around the lineup.
- sheet_npc.png: all lineups (bandits, soldiers, Soviets, juggernauts,
  elites) and the egg hotbar.
- npc_snipers_v0.32.1.png: a juggernaut (dark set) with a Barrett and three
  elite snipers (black set, green night vision goggles, long scoped rifles).

## zombies_v0.34.0/ (dev test mode zombies)
- sheet_zombies.png: lineup of four infected on a grass terrace, left to
  right: common (red top, jeans), runner (dark hoodie), riot (combat helmet,
  dark police clothes), screamer (white hazmat suit). Two earlier tries
  (not kept) had the lineup below the camera (ground taken 18 blocks away)
  and natural spawns walking in front.

## arena_v0.34.0/ (all NPC test modes on the test arena, DevTestArena)
- sheet_npc.png: the six lineups (bandits, soldiers, Soviets, juggernauts,
  elites, snipers) on the flat grey stone floor against the sky, nothing
  in front; the egg hotbar.
- sheet_zombies.png: common, runner, riot, screamer on the arena floor.
- sheet_shots.png: impacts on the test wall; the RPG bandit (one prone,
  Decimation's random stance) firing, rockets with smoke trails in flight.
  A first arena run (not kept) still had blood marks, dropped items and
  NPCs of earlier modes on the floor: build() now cleans both.

## mags_v0.35.0/
- npc_mags.png: hotbar with the 30 round STANAG, 60rnd NATO STANAG
  Magazine (selected, name shown), 30 round AK, 60rnd AK Magazine (RPK
  style long icon), 40 round RPK mag.

## mac10_v0.36.0/ (the MAC-10 pilot)
- render_bbmodel_contact_sheet_1..4.png: Blockbench MCP previews (dark
  flat lighting, first texture): left side, right side, three-quarter,
  top: boxy receiver with a dark ejection port window, threaded barrel,
  sights, cocking knob, checkered grip, long magazine, trigger guard,
  stock rods and butt plate. The texture was brightened after this.
- gun_fp.png: first person on the test arena, the MAC-10 seen from behind
  and above (rear sight, top, stock plate), grey metal; name "MAC-10";
  hotbar with its icon and two magazines. gun_fp_uzi.png: Decimation's Uzi
  in the same view for comparison (the Uzi shows more of its side).
- gun_reload_8.png: start of the reload (gun tilted).
- gun_npc_front.png: a bandit holding the MAC-10 at the chest.
- gun_npc_fire_110 / 120.png: behind the bandit, firing at a Soviet,
  tracers both ways.

## guns_study_v0.36/ (tools/guns/study.py, Decimation's guns drawn from the jar, 10 Oktober 2026)
- sheet_smg / sheet_pistol / sheet_mg / sheet_shotgun_rocket / sheet_rifle1..3.png:
  side views of all 98 Decimation guns with their textures, label = parts,
  shaped %, length x height. Near black metal, dark brown wood, tan or
  green only on guns of that colour; slim silhouettes; magazines and
  grips skewed, not boxy.
- uzi / mp5a3 / glock17 / ak74 / ours_mac10 _side / _other / _top / _three.png:
  close renders at 40 px a unit. Uzi: chamfered sight ears, layered
  receiver, thin bent trigger guard, near black. MP5: rounded receiver
  rear, curved mag lighter grey, raised top rail. AK74: octagon barrel and
  gas tube, brown polymer curved mag, dark red brown wood.
- *_split.png (uzi, mp5a3, ak74, glock17): every part a flat colour with
  outlines. Uzi side: receiver = long panels stacked (top, upper, lower)
  with thin raised strips 0.1 thick, front taper trapezoids, stock of
  skewed bars, grip of stacked skewed segments. AK74 three quarter:
  barrel / gas tube / muzzle as 3 part octagon slices, magazine of
  segments, stock of big tapered panels with an inset panel.
- cmp_uzi_mac10.png, cmp_three.png: our MAC-10 v0.36 next to the Uzi:
  ours is light grey, chunky, 23 plain boxes, grip and mag as thick as the
  receiver; the Uzi is near black, slim, 105 shaped parts.
- tex_crops.png: Uzi, AK74, MP5 texture strips at 5x: one flat tone per
  part island with faint noise, no painted detail.
- icons_all.png: all 98 Decimation gun icons at 3x plus our MAC-10: side
  silhouettes, muzzle right, dark tones, 1 px black outline, slim; ours a
  light grey block filling the frame.
- fp/gunview_<gun>.png (dev test mode gunview, 854 x 480, GUI on): first
  person hip view of uzi, mp5a3, ump45, vector, mp7, glock17, deagle,
  ak74, m4a4, r870 and our mac10 on the test arena. Guns come from the
  lower right to the centre, seen from the rear, top and right side;
  long guns show their right side as a diagonal, short ones mostly their
  rear. Uzi rear: slim, dark, stock bars and hinge. MAC-10: a big light
  grey square (the stock plate) fills the view.
- fp_uzi_mac10_mp5.png: contact sheet of those three first person shots.
- attach/attach_<gun>_<attachments>_side / _three.png, attach_sides.png
  (study.py attach): M4A4 with reddot, arSuppressor, foregrip; Uzi with
  4x, smgSuppressor, laser; our MAC-10 with reddot, smgSuppressor,
  flashlight. Suppressors sit on the muzzle; the foregrip lands behind
  and below the M4A4 (under the stock), the red dot small on the rear top.
- fp/gunview_<gun>_att(_aim / _npc).png, fp_attach_sheet.png: M4A4, Uzi,
  MAC-10 with reddot + smgSuppressor + foregrip in game: hip, aim (red
  dot ring at the screen centre on M4A4 and Uzi), a bandit holding it
  seen from its right side (suppressor at the muzzle on all three, no
  foregrip visible).
- fp/gunview_<gun>_aim.png (uzi, mp5a3, ak74, glock17, mac10, no
  attachments) and fp_aim_iron.png (crops with red lines at the screen
  centre): the centre is at the top of the iron sights for Decimation's
  guns; the MAC-10 sight ears stick out about 40 px above it.

## mac10_v0.37/ (MAC-10 v2, gunmodel v2)
- cmp_side.png: Uzi next to the MAC-10 v2 at the same scale: both near
  black, similar density; MAC-10 boxy receiver with sights, threads,
  raked grip, long magazine, wire loop on the rear top. side.png: the
  final side view (one piece raked grip).
- ours_mac10_side / other / top / three(_split).png: renders and part
  split of v2.
- gunview_uzi / mac10 (_aim / _npc).png: first person, aim, bandit side
  view; aim_centre.png: crops with the screen centre lines, the MAC-10
  sight tops on the line like the Uzi's.
- icon_vs_uzi.png: Decimation's Uzi icon next to ours at 4x: both dark
  silhouettes with a dark outline (after the halo fix).
- sheet_gun.png: dev test gun on v2: first person, reload frames (tilt,
  magazine down and back), fire frames, Uzi control, bandit side / front /
  firing. sheet_gunview_attach.png: MAC-10 with reddot, smgSuppressor,
  flashlight: hip, aim (red dot ring at the centre), bandit side view.
- attach_ours_mac10_smgSuppressor_side / three.png (study.py with
  ATTACH_FIX): suppressor 0.1 from the muzzle (drawn low: study.py y
  offset for barrel attachments is off for every gun).
- suppressor_fix.png (lower half): v0.37.1 bandit holding the MAC-10, the
  suppressor flush on the muzzle and in line; gunview_mac10_supp.png:
  first person hip with the suppressor attached at the front;
  gunview_<gun>_supp_npc.png: the full NPC shots.
- v0372_aim_cmp.png / v0372_aim_iron_cmp.png: v0.37.2 aim crops (centre
  lines), Uzi left, MAC-10 right, with the suppressor / iron only: both
  rear aperture holes on the centre line; the MAC-10 stock loop bar now
  below the hole. v0372_<gun>_aim(_iron).png, v0372_<gun>_hip.png full
  shots; v0372_hip_crop.png: first person, the suppressor over the
  threads at the receiver front.
- user_v0372_final.png: the user's own screenshot of v0.37.2 (dev client,
  hip view looking down): MAC-10 with suppressor and red dot, suppressor
  on the bore over the threads; their verdict: aim, firing and icon good.
- v0373_cmp_side.png: Uzi next to MAC-10 v0.37.3 (110 parts): mottled
  gradation along long panels, rivet, housing plate, SAFE / FIRE lever
  visible. v0373/ours_mac10_*.png renders; v0373_aim_cmp.png: aim crops
  with the Uzi, both holes on the centre line; v0373_hip_crop.png,
  v0373_hip_supp_crop.png: first person, side details now 0.08 proud,
  suppressor over the threads; v0373_mac10_att(_npc).png full shots.

## ump9_v0.38/ (UMP9, second gun of our own; references)
- ref_side.png: Decimation's UMP45, MP5A3, AK74 side views (curved mag
  recipes: MP5 4 skewed segments, AK74 11 parts). ump45_side / other /
  top / three(_split).png: the UMP45 renders and part split (receiver
  panels, top rail as small teeth parts, railed handguard with vents as
  rows of small blocks, skeleton stock as a frame of long bars, straight
  mag of 2 parts in front of the guard, raked grip).
- cmp_side.png: Decimation's UMP45 next to our UMP9 (their body, our
  curved magazine with window strips, 155 parts). ours_ump9_side / other /
  top / three.png: UMP9 renders (three quarter: the curve and windows
  read clearly). gun_fp / gun_reload_14 / 22 / 36.png and reload_sheet.png:
  dev test gun with -Pgun=ump9 (first person, reload tilt). In game hip
  and aim identical to the UMP45 (gunview guns=ump45,ump9).
- gunperf_<gun>_dropped / _held (dev/run/client/screenshots, not copied):
  gun dropped 2.5 blocks in front of the camera on the arena and held;
  fps numbers in docs/performance.md section 2.

## milbase_v0.42.1_review/ (the user's second review, 11 Oktober 2026)
- user_100: AS Val with an ACOG at the hip: the sight sits forward on the handguard, not over the
  receiver (fixed: sights 4 back, docs/gun_style_guide.md section 14).
- user_101: TOC inside toward the locked door: only a large ammo case and a weapon cabinet whose
  doors face the door.
- user_102: barracks: footlockers (storage crates) at the bed ends; asked for real chests (loot).
- user_103: DFAC serving table with two pink camping stoves on it (asked: the 1 block radio).
- user_104, user_105: motor pool at night: the user placed metal shelves with boxes and a tyre
  stack by a jeep and an APC to show "more props"; T-walls, conex, camo net post.
- user_106: Decimation main menu in the dev client: "Warning! Your session is invalid!" banner,
  grey "Play Offline", profile box Player Kills / Deaths / Zombie Kills 0, Total Player / Infected
  Casualties 0.

## milbase_v0.42.3_review/ (the user's fourth review, 11 Oktober 2026)
- user_115: a corner guard tower seen from the HESCO wall top: the ladder runs up the tower face
  above the side wall, so from the ground it is walled in (5 of 6 ladders were, by the dump).

## milbase_v0.42.4/ (after the fourth review, 11 Oktober 2026)
- milbase_<size>_tower_ladder: a tower's ladder seen from the base: a 1 wide shaft in the HESCO
  with the ladder at its back, open gravel in front.
- milbase_0_toc_inside / toc_door: the outpost's TOC, back after four versions without it:
  desks with radios and monitors both sides, flag, the locked door ("This door needs a lockpick")
  with the military keycard screen beside it between the sandbag ring ends.
- milbase_<size>_toc_inside (FOB / large): map table with chairs, desk rows, flag.
- milbase_<size>_hut_back: B-hut back end, window, power panel.

## milbase_v0.42.3/ (after the third review, 11 Oktober 2026)
- milbase_<size>_tower_deck / tower_roof: tower decks and the searchlight on the plank roof; the
  ladder shafts (middle of the inner side, air above the hatch) were checked in the block dump.
- milbase_2_hangar: the closed sandstone vault seen from the open end, APC inside, crates along
  the walls.
- milbase_<size>_motor_pool: the workshop shelter with a full spruce roof, the parts yard.
- milbase_<size>_aerial_sw / top: the rebuilt sky test site, bases whole (no band cut out).

## milbase_v0.42.2_review/ (the user's third review, 11 Oktober 2026, their own runClient)
- user_109, user_110: guard tower at night: the ladder runs up the inner corner and the corner
  post (log) stands right over the hatch, so the deck cannot be reached (fixed: ladder in the
  middle of the inner side). Parapet sandbags, a military crate on the deck, plank roof on posts.
- user_111: hangar from the open end: the side walls are 3 high and the stepped roof starts at 5,
  so a gap runs along both sides and the roof looks like it floats; APC wreck inside, a
  footlocker, a table, drums, a generator (fixed: closed skin; more crates).
- user_112: motor pool workshop shelter: roof a checkerboard of camo metal and slabs; under it
  pallets, a tool locker, a table; camo net and jeep beside it (fixed: full spruce roof).
- user_113: the underside of the sky test large base: rows of stone / grass / dirt / wire blocks
  hanging below it in a band. Read from the world files: a city edge ramp generated in the
  chunks below took the sky base top as the ground height, cut a 24 row band (z -19968 ..
  -19945) out of the large base at -19660 and left its ramp blocks floating (not a render bug).
- user_114: the sky test seen from above: two big pits through the large base (the cut band)
  and the large base rebuilt over the outpost's spot (a test bug: sizes=2 alone went to the
  first slot).

## milbase_v0.42.2/ (after the second review, 11 Oktober 2026)
- stats_hud: the in game HUD list "Human Kills / Zombie Kills / Deaths / Days Survived /
  Humanity / Bottlecaps" with local numbers (dev test stats).
- stats_menu: Decimation's home menu opened in the world: no session banner, profile box "Human
  Kills: 4 (K/D: 4), Deaths: 0, Zombie Kills: 4", bottom "Total Human Kills: 4 / Total Infected
  Kills: 4" (test kills, put back after the test); the button reads Resume Game in a world.
- gunview_asval_att*, gunview_ak74_att*: AS Val with the ACOG on the receiver at the hip, aim
  picture the same as the AK-74's (reticle centred); with the red dot too.
- milbase_<size>_toc_door_wall: the door wall seen from the map table: crates, cabinets, the
  locked door. milbase_<size>_hut_barracks_inside: a vanilla chest among the footlockers.
  milbase_<size>_dfac_inside: the small radio on the counter. milbase_<size>_motor_pool: the parts
  yard (shelves, tyre stacks, drums, covered pallet, open crate) along the south edge.
- milbase_<size>_aerial_sw: the new test site at -20000, y 230: no dirt pillars, nothing around.

## milbase_v0.42.1/ (military bases after the user's review, 11 Oktober 2026)
milbase_<size>_<point>.png, sizes 0 COP, 1 FOB, 2 large FOB, turns 0 (sky test at y 200):
- toc_inside: desk runs on both walls (radios lying along the desks, monitors), map table with
  chairs at its sides, flag behind the commander's desk; nothing in a wall.
- toc_door: the red brown LOCKED door (Door_Emergency_1_Locked) with the green military keycard
  screen beside it on the wall, no flag there.
- hut_arms_inside: open weapon cabinets along the left wall, crates and a large case on the
  right, the aisle free, a cleaning bench at the far end.
- dfac_inside (from the south end): serving table with camping stoves on top, water pallet, the
  long middle table with chairs on both sides beyond.
- aid_inside: stretchers along both sides, medical crates at the far end, aisle free.
- tower_roof: the searchlight standing on the tower's plank roof.
- asp: sandbag berm 2 thick (no dirt), crate rows with small cases on the large ones.
- hut_back: B-hut back end with a window and the junction box power panel; clean low roofs.
- lsa_street: hut door with its step and light, gravel street with few dirt patches.
- hut_barracks_inside: bunks with upper slab bunks, footlockers.
- fighting_position (from above): sandbag U against the HESCO wall, a crate inside.
- guard_booth: the booth north of the gate tower, window toward the lane.
- motor_pool: camo net on posts over a jeep and an APC lying north south, conex row, T-wall
  screen, work light, tyre stack.
- 2_helipad: crashed helicopter on the 13 x 13 pad, landing lights; 2_hangar: APC inside.
- aerial_sw / top: the sky test bases (dirt pillars below are left over from an earlier graded
  run), clean B-hut roofs.

## milbase_v0.42_review/ (the user's review of v0.42.0 in the dev client, 10 Oktober 2026)
User screenshots, creative mode, the milbase sky test bases; each finding is fixed in v0.42.1
(docs/prop_placement.md, docs/military_base.md):
- user_71: aerial of a FOB: HESCO walls, wire rings, helipad, TOC with antenna, sandstone tents,
  B-huts; many orange dirt patches on the gravel.
- user_72: TOC inside: wall flag on the north wall behind the commander's desk (monitor, office
  chairs), radio desk on the right.
- user_73: TOC inside toward the door: footlocker style crate, open weapon cabinet, the metal
  door standing open (not a locked door), an NPC.
- user_74: TOC front outside: the keycard screen drawn ON the outside wall flag next to the metal
  door (both on one wall spot).
- user_75: COP TOC side: work light tower, two generators against the sandbag ring, barrels,
  flag + keycard on the wall, a trash bag; the radio tower drawn black.
- user_76: a work light tower with a log pole (camo net post) standing through it, by a tent.
- user_77: ASP: raw dirt berm with grass growing; fuel bladder (black wool) in a sandbag berm,
  drums, a generator.
- user_78: inside the ASP: gravel pad, one large crate on a pallet, an ammo case, a red light,
  dirt walls with grass (sparse).
- user_79: a sandbag fighting position / pit with crates clipping into the sandbags, dirt patch.
- user_80, user_82: DFAC: wood tables across the tent with chairs at the table ENDS, partly inside
  the tables; a barrel under a table.
- user_81: DFAC serving end: pink camping stoves under the grey metal tables, water pallet.
- user_83: looking into a conex: crates in a 1 wide hold, a wood crate clipping the walls.
- user_84 to user_86: guard tower: the 2.5 x 3.2 searchlight crammed on the 2 x 2 deck into the
  sandbag parapet, an ammo crate overlapping it, ladder, plank deck, roof on posts.
- user_87: latrine front: door one block up with no step, dirt in front.
- user_88: B-hut back end: blank wall, sandbag blast wall pillar beside it.
- user_89: fighting position: ammo case wider than the 1 wide U, drawn into the bags.
- user_90, user_91: B-hut sides and roof: closed trapdoor "shutters" lying flat like shelves
  under the windows; the stair roof reads as a saw tooth from above.
- user_92, user_93, user_96, user_97: barracks: beds with spruce top slab upper bunks on the
  walls, footlockers at the bed ends.
- user_94, user_95: arms room: weapon cabinets on one wall, crates on the other, a metal table in
  the aisle, cases and crates drawn into each other.
- user_98, user_99: sunset aerials: TOC roof sandbag piles read as a maze / battlements, B-hut
  saw tooth roofs, flag pole, radio tower.

## milbase_v0.42/ (military bases, 10 Oktober 2026)
- milbase_<size>_<point>.png: dev test shots, size 0 COP, 1 FOB, 2 large FOB; points aerial_sw /
  aerial_ne / top (sky test: the bases float at y 200, the dirt pillars under them are left
  from an earlier graded run), gate_approach (wire rows, hedgehogs, T-wall lane, sawhorses,
  flanking towers), ecp_inside_lane (serpentine jersey barriers between T-walls), main_road,
  tower_deck (sandbag parapet, spotlight), toc_outside (concrete, HESCO ring, flag, keycard),
  toc_inside (map table, desks with monitors and radios, chairs, flag, lights),
  hut_barracks_inside (bunks with upper bunk, footlockers), hut_arms_inside (cabinets, crates),
  hut_office_inside, lsa_street, motor_pool (net shelter, vehicles, oil stains), asp (berm,
  crates), aid_inside (stretchers, medical crates), dfac_inside (table rows, serving line),
  gym_inside, helipad (H, lights, windsock; the large base's crashed helicopter), hangar
  (clamshell vault with an APC).
- round1_details.png: first round problems (orange ground, ASP planter, TOC battlements);
  round3_overview.png / round3_details.png: after the greedy placer; round4_sheet.png: hangar and
  conex yards. real_world_cop_topdown.png: top down block map of the seed 1 COP at -90,66,404
  (rot 180, gate north) on real grassland.

## sights_v0.41.1/ (reticles in the glass, 10 Oktober 2026)
- user_acog_barrett_fixed_reticle.png: the user's Barrett + ACOG screenshot: BDC reticle in the
  screen centre while the scope swayed right of it (the v0.41.0 HUD reticle).
- in_glass_first.png: top EOTech, bottom ACOG, aim on Mk18 and HK416 (reticles in the glass,
  centred, ACOG numbers right; EOTech ring at a quarter size nearly invisible) and HK416 hip: no
  front sight under the sight.
- eotech_512px_dashes.png: EOTech at a third with the 512 px texture: the ring broken into dashes.
- eotech_32px_final.png: Mk18 and MP5 aim with the 32 px reticle: clean red ring, ticks, dot.
- eotech_reticle_texture.png: the 32 px texture enlarged.

## sights_v0.41/ (EOTech 558, ACOG TA11, Mk18 TS stock, 10 Oktober 2026)
- photos_eotech.png / photos_acog.png: the user's photos 60 to 67 and 52 to 59 as contact sheets.
- ts_stock_v1_vs_photo.png: first TS stock render next to the user's photo (the M4A4's grey buffer
  tube still across it); ts_stock_final.png: side and three quarter, cuts dark, lever across the
  window.
- eotech_v1_long.png: first EOTech, too long, small window; eotech_final_views.png: three, left,
  right, rear: compact hood, window filling the rear, label / NV / buttons, dials, battery cap.
- acog_views.png: ACOG three, left, right, rear (eyepiece, turrets, fibre, bell, TA51 mount);
  acog_reticle.png: the BDC reticle texture on a light background.
- icon_eotech558.png / icon_ta11acog.png: the inventory icons enlarged.
- ingame_first_pass.png: hip / aim / NPC for EOTech and ACOG on Mk18 and Uzi (first pass: Mk18
  EOTech window above the centre on the first aim, ACOG lines too thin, square glass).
- aim_eotech_runs_acog.png: Mk18 EOTech aim, run 1 and 2 (ring centred), ACOG with the round
  eyepiece and thick reticle; eotech_final_aim_hip.png: EOTech aim (red ring with ticks) and hip
  on the Mk18 and the MP5.
- final_eotech_three_guns.png: Mk18, M4A4, HK416 with the EOTech, hip and NPC: on the rail, no
  colourful rail teeth.

## sights_v0.40.1/ (sights onto the rail, 10 Oktober 2026)
- user_mk18_reddot_floating.png: the user's Mk18 hip screenshot enlarged, red dot frame in the air.
- m4a4_v036_vs_v040_reddot_hip.png: M4A4 red dot hip, v0.36 (frame resting on the rear sight) and
  v0.40.0 (rear sight folded: frame at the same spot, floating).
- before_reddot_2x_4x_hip.png: M4A4 hip with red dot, 2x, 4x before the fix: all float.
- study_8guns_lowered.png: study.py side renders with the table applied (M60, AA-12, M4A4, Honey
  Badger, ACR, SCAR, JNG90, MP7; red dot left, 4x right): scopes on the rails.
- lowered_no_aimshift.png: lowered without raising the gun in aim: dot below the centre.
- after_reddot_hip_aim.png: Mk18, M4A4, ACR, Uzi: hip on the rail, aim on the centre lines.
- after_2x_hip_aim.png: Mk18 and ACR with 2x: hip on the rail, aim centred.

## mk18_v0.40/ (Mk18 Mod 1, iron sights hidden under a sight, 10 Oktober 2026)
- photo42_rail.png: the user's photo 2 rail enlarged: tall side rail with cross teeth and 3 screws,
  two staggered rows of round holes above it, one row below, round bolt at the front end.
- cmp_side_m4a4_hk416_mk18_v1.png: first pass (rail too light, holes tiny); cmp_side.png: M4A4 and
  Mk18 now; rail_v2_side.png: the reworked rail side; ours_mk18_<view>(_split).png renders.
- attach_ours_mk18_reddot_arSuppressor_three.png: suppressor from the rail front over the flash
  hider (study.py, which draws the irons even with a sight).
- hip_plain_vs_reddot_shots.png: in game hip, M4A4 / Mk18, no attachment (both rows).
- irons_hidden_reddot.png: top: hip with red dot, M4A4 (A-frame front sight still up) and Mk18
  (no iron sights); bottom: aim crops M4A4 / Mk18 without attachment (same picture, post on the
  line), then with red dot (M4A4 front post inside the dot, Mk18 clean).
- attach_five_guns.png: M4A4, Mk18, ACR, G36C, AR15 with red dot and suppressor, hip and NPC side.

## mac10_v0.39.3/ (MAC-10 parts seated, 10 Oktober 2026)
- cmp_three_before_after.png / cmp_low_before_after.png: v0.39.2 left, v0.39.3 right: no visible
  change at this size (the fixes are 0.04 to 0.14), hinge now around the rod end, trigger tip
  continues the trigger.
- aim_cmp_uzi.png: Uzi / MAC-10 aim crops with centre lines: both aperture holes on the line.

## hk416_v0.39.1/ (HK416 review fixes, 10 Oktober 2026)
- user_stock_v0390.png / user_sight_v0390.png: the user's v0.39.0 screenshots enlarged: diagonal
  fins hanging below the stock's angled panel; in aim a wide block left, a thin tall pillar right
  and the aperture ring between (the rear sight cut by the texture bug, not the front sight).
- photo36_sight_stock.png: the user's photo 36 enlarged: HK front sight a U (thick ears flared at
  the base, short post between), slim line stock with a ribbed diagonal band and square butt end.
- debug_colours_aim.png / debug_colours_hip.png: tan gun with a debug texture (our islands red,
  the M4A4's green / blue): the rear sight (ring and both ears) came out red, so it read our
  texture rows.
- aim_cmp_before_texfix.png: M4A4 / HK416 / HK416 Tan aim crops with the new U sight but the old
  texture: ours still asymmetric. aim_cmp.png: after the texture fix: all three the same
  symmetric rear sight, post on the centre line. hip.png: HK416 and Tan hip, rear sight whole.
- ours_hk416_<view>(_split).png: renders incl. the new rear (U sight, post centred) and low
  views (ribs inside the angled panel, nothing hanging).

## hk416_v0.39/ (HK416 black and tan, variant of the M4A4)
- m4a4_side_split.png, m4a4_*.png: the M4A4's part split (handguard 112 parts x 7.5 .. 14.7, A-frame
  and gas block 14.7 .. 16.6, barrel 12.15 .. 18.15, stock below x 0, A2 grip).
- cmp_side.png: M4A4, HK416, HK416 Tan side by side: HK rail flush with the receiver rail, tall
  folding front sight, thin barrel with HK flash hider, slim line stock with ribbed butt and fins,
  raked grip; the tan one tan except sights, barrel, muzzle, trigger.
- ours_hk416(_tan)_side / other / top / three.png renders; attach_ours_hk416tan_arSuppressor_reddot_*.png.
- gunview_<gun>(_aim / _npc).png and aim_cmp.png: aim crops with centre lines, M4A4 / HK416 /
  HK416 Tan: rear aperture and post tip on the same line. gunview_hk416tan_att(_npc).png: first
  person with red dot and suppressor (tan furniture, black sight and suppressor).

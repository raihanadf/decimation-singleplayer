# Feature Tracker - Decimation Mod

Status values: `Done` / `Decided` (direction agreed, not built yet) /
`Documented` (how-to written, not executed) / `Not started`.

---

## Done

### Wasteland gates: 5 looks, 8 in 10 shut (14 Oktober 2026, v0.61.0)
- User on 0.60.2: gates between a wasteland city and its city in about 6 styles, the first a toll gate;
  then 80 % shut so players and mobs cannot pass, 20 % broken open; closures barbed wire with landmines
  plus traps. Chosen (asked): looks toll, checkpoint, containers, quarantine, police; closures wire +
  mines, welded steel, burnt bus, tank ditch; open gates smashed.
- military/CityGate, CityGatePlan, city/WastelandFence.gate, our block deciworldgen:boom_barrier. Details,
  checks and what is not verified: docs/wasteland.md "v0.61.0". New worlds' chunks only (wasteland sector
  worlds since v0.57.1).
- Refuges in big cities 1 per 4 city sectors (v0.60.2, docs/city_engine.md "City sizes").

### Cities of 4 to 64 districts (13 Oktober 2026, v0.60.0)
- User idea (docs/ideas.md item 19) and choices: sizes 4 / 8 / 16 / 24 / 48 / 64 districts with
  weights 30 35 40 35 28 22, compact blobs, more city land (cities and roads felt too rare), one
  wasteland sector per 4 city sectors on the side toward the military.
- worldgen/CityLayout (+ CityLayoutEvents, config/deciworldgen_cities.cfg); new worlds only. Design,
  numbers and checks: docs/city_engine.md "City sizes". Seed 1: city land 24.3 % (was 15 %).

### Auto update from our GitHub releases (13 Oktober 2026, v0.59.0)
- User: "user launch decimation then it trigger check updates on github via api, if there is update,
  the button on hud show "update" and it will begin install on minecraft with install progress
  screen. if not it show "play" button"; finish chosen: "Quit game" / "Later".
- fixes/Updater: at start (client) a background check of the release list
  (api.github.com/repos/ucilmenangis/decimation-singleplayer/releases, pre-releases included), the
  newest release with a "Decimation.jar" asset above our mcmod.info version. Decimation's own
  UpdateChecker.isUpdateRequired (tools/patches/PatchNoUpdate.java v2) answers Updater.required(), so its
  menu button says "Update" only then, "Play" otherwise. The button's page (GuiMenuUpdates) is swapped
  for fixes/UpdateScreen (GuiOpenEvent; since v0.64.7 Decimation's own progress screen GuiUpdateProgress through
  fixes/UpdateProgress, UpdateScreen only for a failure: docs/main_menu.md 2d): download at once into "<jar>.download" beside the running jar
  with a progress bar, checked against GitHub's sha256 digest, then "Quit game" / "Later"; the swap runs
  when the game closes (fixes/UpdateSwap as its own Java process from a temp copy: Windows locks a
  loaded jar). config/deciworldgen_update.cfg: enabled, api.
- Checked: dev test `update` against tools/fake_release_server.py: release found, Decimation's check
  true, menu Update button, our screen, progress (33 %), ready, the helper replaced a stand in jar
  (all PASS); a wrong digest: refused, download deleted, old jar kept. The real GitHub check from the dev
  game: "up to date (0.58.0...)". Shots docs/shots/update_v0.59/sheet_update.png.
- Real GitHub (after 0.59.0 was published, dev test `update real=true`: the dev game pretends to run
  0.58.0): found 0.59.0, Decimation's check true, Update button, our screen, the real 229 MB jar
  downloaded in 177 s and matched GitHub's sha256 (9e20395d...), the helper swapped it into the stand in
  (starts PK, 240980301 bytes): all PASS. Shots docs/shots/update_v0.59/sheet_update_real.png.
- Not verified: old Java 8 builds bundled by launchers (HTTPS to GitHub), a launcher that kills the game's
  child processes on exit, the update running inside a real installed game (the dev game runs from a
  folder, so a stand in jar was replaced; the first real one is 0.59.0 -> the next release).
- Security (commit review, same day): the swap helper ran from a fixed folder in the shared temp
  directory, where another local user could plant a class; now a fresh folder from
  Files.createTempDirectory (owner only: rwx------ checked on Java 8), deleted by the helper when done.
- Release rules for it: the asset must be named exactly Decimation.jar, the tag a dotted version above
  the last one (0.59.0, 0.60.0 ...). Since v0.59.1 also upload Decimation.jar.sha256 (tools/onejar.py
  writes it beside the jar): the fallback below checks the download against it.
- v0.59.1, rate limit fallback (13 Oktober 2026): GitHub allows 60 API calls an hour per address without
  a login; our own production client got 403 after an afternoon of checks, and players behind one shared
  address (mobile networks, CGNAT) would too: the menu then showed Play and never offered the update.
  Now (fixes/Updater): (1) the API is asked with the ETag of its last answer
  (config/deciworldgen_update_cache.json); GitHub does not count a 304; (2) when the API fails anyway
  (403 / 429, timeout, bad answer) the check reads the release feed <web>.atom (not the API, pre-releases
  included, newest first), takes the first newer tag whose <web>/download/<tag>/Decimation.jar answers a
  HEAD, and its sha256 from <web>/download/<tag>/Decimation.jar.sha256 (older releases have none: size
  check only). Config key `web`. Updater.source() says where the answer came from.
- Checked: dev test `update` with tools/fake_release_server.py: API answer, then a second check "api (not
  modified)" (304); with LIMITED (API 403) the answer comes from the feed and the jar downloads, is checked
  against the .sha256 and swapped; LIMITED BADDIGEST: refused, old jar kept. Real GitHub with the API
  pointed at a dead address (`real=true feedonly=true`): 0.59.0 found from github.com's feed, 229 MB in
  13 s, size matched (0.59.0 has no .sha256), swapped: all PASS.

### Shared core, step 1 of the master plan (13 Oktober 2026, v0.57.0)
- docs/systems_plan.md section 2: `fixes/WorldClock` (day number, DAY / DUSK / NIGHT / DAWN events
  for every later system), `fixes/LootPools` (16 loot lists, 4 kinds x 4 levels, in
  config/deciworldgen_loot.json; magazines taken from each gun's own list; roller with "first"
  lists so level 4 holds one item of every type), `fixes/Protection` (armor table in
  config/deciworldgen_protection.json, 124 wearables by family; not used by damage yet),
  `worldgen/Areas` (our areas with kind, owner, state, per world; radiation / safezone / military
  areas put a Decimation zone in place). Loot spikes before it (v0.57.0 too): fixes/Loot, LootBody.
- Decimation's crate off by one fixed (bug.md).
- Checked: dev test `core` 36 checks PASS; dedicated server boots with it; dev test `items` dumps
  every item (docs/references/decimation_items.tsv).

### Zones on generated structures (auto zone tagging)
- **Requested**: 27 Juli 2026
- Zones (safezone/military/police/radiation/scary/border,
  `net.decimation.mod.server.zones`) normally need manual wand placement.
- **Root cause found on the way**: zones never worked in singleplayer at all.
  The zone list (`deci.aJ.b.aAc`) is only loaded by ServerProxy, and every
  zone effect handler (`deci.aK.d/g/i/m`) is `@SideOnly(Side.SERVER)`.
- **Implementation (v0.7.0)**: `ZoneStore` keeps zones per world in
  `<world>/deciworldgen_zones.json` (Decimation's own JSON format) and merges
  them into Decimation's live list, re-adding them if Decimation reloads its
  global file. The generator tags `mil_*` structures MILITARY and `city_*`
  POLICE, footprint plus 8 blocks. `ZoneSpawnHandler` replicates
  `deci.aK.d` in singleplayer: 25% of infected spawning in a MILITARY/POLICE
  zone become that uniformed variant, infected spawns in a SAFEZONE are
  cancelled.
- **Verified 7 Oktober 2026** without the user: dedicated server run (zones
  created, saved, reloaded after restart without duplicates) and an
  unattended singleplayer run (`gradlew runClient -Pautotest`): inside a
  POLICE zone 10 of 33 infected became police, outside 0 of 34.
- Not covered: radiation/scary/safezone player effects (`deci.aK.m`, `g`,
  `i`) in singleplayer; our generator does not create those zone types.

### Skip intro screens (jumpscare + BoehMod studio logo)
- **Requested**: 25 Juli 2026
- Every launch replaced the vanilla main menu with a chained animated intro
  (390-frame "jumpscare" clip, then BoehMod logo zoom/mortar-explosion
  sequence) before reaching the mod's own main menu.
- **Implementation**: flipped the default value of two static gate flags
  (`deci.i.d.iH`, `deci.i.c.io`) to `true` in their class initializers - both
  screens' own logic already treats "flag true" as "already played." Assets
  and classes untouched, just never instantiated.
- **Done**: 25 Juli 2026, 14.30. User-confirmed working 14.41.

### Nerf ranged weapon damage (50%, all weapons including crossbow)
- **Requested**: 27 Juli 2026
- **Implementation**: single chokepoint patch on `deci.ay.i.am(int)` - the
  damage-per-hit setter every one of the ~90 registered guns/rockets funnels
  through via its `.am(N)` builder call. Now stores `Math.round(n * 0.5f)`
  instead of `n` directly. Applies uniformly, no per-weapon exceptions.
- Side effect (welcome one): NPC bandit/soldier gunfire uses the same `.aew`
  field for their own damage, so this also halved NPC-inflicted bullet damage.
- **Done**: 27 Juli 2026. User-confirmed via mob testing (2->3 bullets to kill
  unarmored zombie/skeleton, consistent with the cut).

### Buff armor damage reduction (35%)
- **Requested**: 27 Juli 2026
- **Implementation**: scaled `ItemArmorDeci.damageMultiplier` by `x0.65` at
  construction (both constructors), proportional to each armor piece's
  original value.
- **Known gap**: only affects damage from a *player's own gun* - does nothing
  against NPC (bandit/soldier) gunfire. See `bug.md` -> "Armor damage-reduction
  buff doesn't affect NPC gunfire" for the root cause and proposed fix
  (pending user go-ahead, not yet implemented).
- **Done (partially)**: 27 Juli 2026, 00.08.

---

### Cheap scope (view zoom instead of picture in picture), DONE v0.28.0..0.28.4
- **Requested**: 8 Oktober 2026 (scope cost about 150 -> 110 fps); user
  approved the result 9 Oktober 2026 ("the scope do really well").
- reddot / 2x: world and gun zoom together (EntityRenderer.cameraZoom),
  see-through glass (frame under the glass copied before the hand, mapped
  by screen position), sight centred on the aim point.
- 4x and up (4x, dragunov, 8x, integrated aug): black sniper overlay with
  the scope's reticle, no gun (config `overlayFrom`).
- Mouse slows to 1 / zoom while zoomed; old picture in picture scope kept
  behind `pictureInPicture=true` (config/deciworldgen_scope.cfg).
- Code: `net.decimation.fixes.ScopeZoom`, `tools/patches/PatchScope.java`;
  history and measurements in bug.md "FPS drop while aiming through
  scopes"; test `./gradlew runClient -Pautotest -Pscopeonly`.

## In progress

### Decimation world generation (own code inside the jar)
- **Requested**: 26 Juli 2026 (original "Decimation world type" idea);
  direction changed 13-15 Agustus 2026.
- **History of direction**: first decision was "don't build it, use companion
  mods" - tried Ruins, then ezWastelands + GeneratorMods (CARuins/GreatWall/
  WalledCity). All dropped: CARuins generated wrong-aesthetic rubble blobs,
  GreatWall/WalledCity ship zero usable templates + Windows-backslash path bug,
  ezWastelands terrain made stepped-pyramid artifacts. New decision: build
  world-gen ourselves as ordinary Java compiled into `Decimation.jar`.
- **Architecture** (settled 15 Agustus 2026):
  - **Terrain is not our job**: user wants realistic real-life-looking
    terrain, handled by companion mod **RTG (Realistic Terrain Generation)
    1.7.10-1.1.1.7** (CurseForge, confirmed exists; not yet installed).
  - **Structures are our job**: an FML `IWorldGenerator` (runs per chunk on
    top of ANY terrain generator, no custom WorldType needed) placing
    buildings/cities read from standard **`.schematic` files** (MCEdit/
    WorldEdit format - user picked this over a custom save command).
  - **Second `@Mod` in the same jar**: new code lives under
    `net.decimation.worldgen` as mod id `deciworldgen`
    (`required-after:deci`). Forge scans all classes in a jar for `@Mod`, so
    this loads alongside the original mod with zero edits to obfuscated
    bytecode - fully removable by deleting its entries from the jar.
- **Milestone 2 DONE, user-confirmed in-game 15 Agustus 2026, 11.52**:
  proof-of-life generator (`MarkerGenerator`) places a 4-tall glowstone
  pillar at the centre of every overworld chunk. Screenshot confirmed pillars
  + FML log confirmed full mod lifecycle. Crash-hardened: catch-all around
  generation (logs + self-disables after 3 errors, never crashes the game),
  null-guards on block lookup and world provider, +8 chunk offset so no
  cascading chunk generation. (Marker later removed again - replaced by the
  milestone-3 structure generator.)
- **Milestone 3 DONE, user-confirmed in-game 15 Agustus 2026, 12.08**:
  real structure placement. `Schematic.java` loads standard MCEdit/WorldEdit
  `.schematic` files (gzip NBT, AddBlocks nibbles for mod-block ids > 255,
  TileEntities ignored - Decimation loot is position-based) from
  `config/decimation_worldgen/`. `StructureGenerator.java` places them:
  deterministic 4x4-chunk cells (world seed + cell coords -> same layout
  every time, no overlap possible), 50% chance per cell, site rejected if
  ground-height spread > 6 or on water, dirt foundation auto-filled up to 8
  deep, footprint capped 24x24 to stay inside the safe population window
  (zero cascading chunk generation). Placement coords logged. Same
  crash-hardening pattern as milestone 2. Two generated test schematics
  (`tools/make_test_schematics.py` -> `structures/`): `ruined_house` 11x6x9,
  `watchtower` 7x9x7, vanilla blocks + vanilla chest only (chest is in
  Decimation's loot table -> loot GUI works, and vanilla ids are immune to
  FML's per-save mod-id assignment). Screenshots confirmed: placement +
  foundation + ruin decay + Decimation loot GUI on the chest all working.
- **Milestone 4 DONE, user-confirmed in-game 15 Agustus 2026, 12.27**:
  rotation. `Rotation.java` = clockwise metadata rotation table per 1.7.10
  block family (6-direction facing, stairs, log axes, torch/button/lever,
  doors incl. untouched hinge half, trapdoors, beds, fence gates, pumpkins,
  repeaters/comparators, 16-step signs, rails incl. slopes/curves, vines,
  anvils; unknown ids - including Decimation's own blocks - pass through
  unchanged). Structure direction (0/90/180/270) picked deterministically
  per cell from the world seed; coordinates transformed, footprint extents
  swapped on 90/270. Unit-tested before deploy: 4x90 = identity for all
  256 ids x 16 metas + 15 spot checks. Old chunks keep their baked north-
  facing builds; new chunks rotate.
- ezWastelands + GeneratorMods renamed to `.jar.disabled` in the instance's
  `mods/` folder (15 Agustus 2026) - restore by renaming back if ever needed.
- **RTG installed 15 Agustus 2026** (`RTG-1.7.10-1.1.1.7.jar` in mods/,
  archive copy in `WorldGenerator/`), user-confirmed loading; world type
  "Realistic" at world creation.
- **Prop substitution DONE, crates user-confirmed in-game 15 Agustus 2026,
  12.46**: schematics use vanilla placeholder blocks, swapped for real
  Decimation props at placement, resolved BY REGISTRY NAME (immune to FML
  per-save numeric ids). Registry names are the CamelCase class names
  (`deci:BlockWoodCrate`) - found via FML registry snapshot in a world's
  `level.dat`; the lowercase `prop_*` strings in the jar are texture paths
  (first attempt used those, failed cleanly thanks to the null-guard).
  Mapping: sponge->WoodCrate, gold->MilitaryCrate, lapis->AmmoCrate,
  diamond->MedicalCrate, emerald->PoliceCrate, iron->Wreckage1-5 (random per
  position), coal block->BlockRoad, wool colour meta = 16 street-prop slots
  (sandbag/cone/vending/streetlight/barrier/bin/bench/dumpster/tire/barrel/
  cardboard/trashbag/electricbox/shelf/canfire/roadsign).
- **Full prop catalog discovered** in level.dat: Decimation ships road blocks
  with lane markings (`BlockRoad_*`, `BlockRoadSlab_*`), street lights,
  traffic lights, road signs, sandbags, barriers, dumpsters, vending
  machines, office wall sets, graffiti, metro props - everything needed for
  proper city generation later.
- **8 schematics** now (`tools/make_test_schematics.py`): ruined_house,
  watchtower, gas_station, house_2story, military_outpost,
  police_checkpoint, street_scene, survivor_camp - the last six use real
  Decimation props (asphalt roads, sandbag barricades, street lights, fire
  barrels, wrecked cars).
- **Underwater placement fixed, 7 Oktober 2026 (v0.7.0)**: the water check
  never worked. `getTopSolidOrLiquidBlock` skips water in 1.7.10, so the check
  looked at the lake/sea bed and structures were built on the ocean floor
  (found in a dedicated-server test: a shed at y=31 under 27 blocks of
  water, read straight from the region file with `tools/worldcheck.py`).
  Now the block above every sampled ground point is checked; the same seed
  rejects all 4 underwater sites. Terrain grading had the same blind spot,
  also fixed. Worlds generated before 0.7.0 keep their underwater ruins.
- **Ships as its own jar since 7 Oktober 2026**: `dist/deciworldgen-0.7.0.jar`
  next to `dist/Decimation.jar` (= patched Decimation without our classes).
  Built in the `dev/` workspace (`./gradlew build`). Prism confirmed loading
  it (7 mods, all our handlers registered).
- **City street grid, 7 Oktober 2026 (v0.8.0, in `dist/`, NOT yet in
  Prism; Prism runs 0.7.0)**: CITY sectors get a 5 block wide street
  (`deci:BlockRoad`) along every cell's west and north edge, i.e. a grid
  with 64 block spacing. Each chunk paints only its own columns (no
  cascading generation), the surface follows the terrain, skips water (streets
  stop at lakes, no bridges yet) and clears plants/tree parts above.
  Structure anchors are now limited to the first 3 chunks of a cell so a
  footprint never reaches the next cell's street. Side effect: in worlds
  started before 0.8.0, newly generated chunks use the new anchor rule, so a
  structure can rarely be duplicated or missing right at the old/new border.
  Verified on seed 1 from the region files: continuous streets at x=128 and
  z=192 (y 62 to 72), gap of at least 3 blocks to every structure; the
  singleplayer autotest still passes. Needs the user's eyes on how it looks.
- **Procedural city blocks, 7 Oktober 2026 (v0.10.0)**: user found the
  structures too basic and chose procedural buildings plus community
  schematics. City sector cells are now filled by `CityDistrict`: 4 lots per
  street block with ruined apartments, offices and shops (1 to 6 floors,
  rooms, ladder shafts, broken windows, decay, collapsed corners, Decimation
  crates and furniture), POLICE zone per building. Verified from the world
  files on seed 1 (34 buildings near spawn; a 6 floor office checked column
  by column) and in the singleplayer autotest (40 buildings).
- **Large schematics, 7 Oktober 2026 (v0.11.0)**: any size up to 120x120 from
  `config/decimation_worldgen/large/`, one per 128 block site, written slice
  by slice like the city buildings. Test content: `mil_compound` (48x48
  walled base, 4 towers, 2 barracks, helipad, gate). Verified whole with the
  new pregen tool: all 16 chunks, towers, roads, 6 military / 2 ammo / 2
  medical crates, cabinets, wreckage. See `docs/worldgen.md`.
- **City v2, 7 Oktober 2026 (v0.12.0)**: user review of 0.11.0 ("good for a
  oneshot, needs polish"): more size variety (1 to 20 floors), less brick,
  heavier decay matched to the biome, realistic interiors (asked for
  research), sidewalks, parked cars facing along the road (an old session
  had all cars facing north). Done: researched floor plans
  (`docs/building_design.md`): apartments with a double-loaded corridor and
  units (kitchen, dining, bedroom or studio bed), offices with reception,
  meeting room, storage and desk rows, shops with checkout front left,
  aisles and a stockroom; switchback stair cores to a roof hatch; 11
  palettes; collapse cones over 1 to 3 storeys; biome overgrowth (vines,
  leaves, snow, sand, dead bushes). Verified on seed 1: 33 buildings, 0
  missing walls, 8 stairs per storey in every cored building, beds in all 14
  apartments, 72/72 cars aligned with their street, 3000 sidewalk columns.
  User checked in game: cars sat across the road (model long axis is x, not
  z), swapped in v0.12.2 (north-south 5/3, east-west 4/2), fewer cars.
- **Decimation world type (user chose A over re-skinning vanilla / RTG,
  7 Oktober 2026)**: flat rolling land, rivers and lakes no ocean, dead near
  cities and overgrown far away, one temperate climate. DONE in v0.14.0
  (docs/terrain.md); seen in game by the user (screenshot). Inserted before
  round 2 step 2. Open: fog colour, more dead decoration (rubble, ash).
- **Worldgen round 2 (user chose option A, 7 Oktober 2026), plan in order**:
  1. terrain blending: Building plan covers its whole 26x26 lot; yard
     columns graded from the building floor (at the walls) to the natural
     height (at the sidewalk), so no plinth cliffs; front yard as gravel /
     asphalt parking with a few parked wrecks;
  2. street life: street lamps on sidewalks facing the road, benches,
     bins, dashed centre lines (road marking orientation must be checked
     in game like the cars were);
  3. full multiblocks: place the second block of 2 block props (shelves);
  4. new procedural building types: police station, hospital, gas station,
     warehouse, with themed loot;
  5. loot balance per building.
  Status: step 1 DONE in v0.13.0 (headless verified, in game look not yet
  seen by the user). Implementation differs from the first sketch: the
  Building plan keeps its own bounds; it implements `Graded` (lot bounds +
  `grade(world, x, z, baseY)`) and `Slices` grades every lot column of a
  window before writing the plan there. Buildings now keep a 2 block side
  yard and a 3 block back yard where the lot allows, and a setback of 6..9
  (75% of lots with room) for a front yard.
  Step 2 (street life) DONE in v0.15.0: levelled streets, centre lines,
  lamps, benches, bins, trash bags, checked in autotest screenshots.
  Step 3 (full multiblocks) DONE in v0.16.0: generated shelves get their
  upper part (they are 2 tall, not 2 wide); vending machines turned out to
  be single block props.
  NEW step 4 (user, 7 Oktober 2026): building quality pass BEFORE new
  types. User: buildings still need improvement and polish in every
  aspect, interior and exterior; people love small details; design the
  interiors in many deliberate steps so future buildings are production
  quality. Sub-steps: a. audit (floor plan renders per storey + in game
  screenshots per kind, findings with evidence); b. prop catalogue
  (screenshot gallery of every Decimation prop); c. design spec per room
  type and for exteriors in docs/building_design.md; d. interiors room by
  room, each verified; e. exterior polish; f. user review in game.
  Then step 5: police station / hospital / gas station / warehouse, and
  step 6: loot balance per building.
  User decisions on the spec (7 Oktober 2026): survivor camps yes, shop
  signs from Decimation's assets, start with the audited apartment /
  office / shop; later many more types in categories (civilian: houses,
  garage, food store, vehicle store...; police; military base; more
  categories to come). Build the polish as reusable parts (see
  docs/interior_spec.md section 9) so new types are cheap.
  4d.1 DONE v0.17.0: doors + low debris. 4d.2 DONE v0.17.0: surfaces
  (room grid, floors, wall panels, ceiling lights / vents). 4d.3 DONE
  v0.18.0: apartment rooms. Next 4d.4: office storey programs + lobby.
  REVISED after the user's in-game review of 0.18 (7 Oktober 2026, "not
  ready to be called interior"): items stood on the floor along the walls
  (TV and radio on the floor, sofa of bare stairs, kitchen = a row of loose
  blocks, lime rug, empty room middle), ceilings were the floor of the
  storey above, outer walls showed the facade inside. New order, approved:
  1. structure: storeys 5 high (floor, 3 air, own ceiling layer), plaster
     lining on the inner face of outer walls;
  2. furniture SETS designed as groups (living, kitchen, bedroom,
     bathroom first), DATA DRIVEN and user editable like Lost Cities parts
     (JSON grid + palette in config/decimation_worldgen/sets/, in-game
     reload command), each checked with close-up screenshots;
  3. reference library per building category (real plans + good Minecraft
     interiors) saved once in docs/, a checklist per category, and ONE
     critic agent pass per milestone on the audit screenshots;
  4. then offices / shops with sets + critic.
  Lost Cities (McJty, MIT, data-driven JSON parts / palettes, 1.10.2+):
  do not port the mod (replaces terrain, fights our world type, its
  interiors are simple too); take the data-driven idea, maybe import its
  parts later through a block translation tool with a license notice.
  Steps 1a (v0.19.0) and 1b + 2 for apartments (v0.20.0) DONE; live loop
  added on user request (no restart per change): /deciworldgen reload /
  rebuild, -Photswap + tools/hotswap.py. Step 3 first pass DONE v0.21.0:
  reference + checklist, critic pass 1 (overall 3/10 on v0.20), fixes for
  the verified findings. Open items listed in
  docs/references/critic_apartment_v0.20.md; then offices / shops on sets.
  DIRECTION CHANGE (user, 7 Oktober 2026): no fixed score target, steady
  improvement per version; first restructure the worldgen code so new
  building types are easy and interiors manageable. Study of Lost Cities
  + Recurrent Complex done (docs/references/worldgen_study.md); user
  decisions: hybrid content (authored parts + procedural), in-game
  capture tool early, footprint size classes. Architecture draft:
  docs/worldgen_architecture.md, migration in 6 steps. Map studies (8
  Oktober): hand-built Decimation maps + DeceasedCraft's 79 building
  types (docs/references/decimation_maps.md, deceasedcraft_buildings.md).
  Architecture APPROVED 8 Oktober 2026. Step 1 (asset core + capture) DONE
  v0.22.0. Next: step 2, extract Shell / StoreyPlan / Furnisher / layers
  out of Building without behaviour change. User decision 8 Oktober:
  public buildings 6 high storeys, homes / apartments 5 (after step 2).
  DIRECTION CHANGE (user, 8 Oktober 2026): DeceasedCraft's buildings as
  the city content (current beta + legacy 5.5.5 with the wasteland
  district), Lost Cities layout (chunk streets, levels 6 apart, stairs).
  v0.24.0..0.24.3 (docs/city_engine.md): content pack tool, city engine,
  street dressing, city edge ramp, superblocks for towers, wasteland
  district next to military sectors. Procedural buildings stay as the
  fallback when no pack is installed.
  Step 2 DONE v0.22.1 (Building split into worldgen/building/, identical
  output). Offices and shops 6 high DONE v0.23.0. Next: step 3 (types
  as JSON, rotated stair core).
  Lost Cities leftovers (user, 11 Oktober 2026: "start 6, except giant
  building"; choices: rail first, underground subway with tunnels between
  cities, then bridges over rivers through cities): railway DONE v0.44.0,
  waiting for the user's look in game (docs/city_engine.md "Railway",
  shots lc_rail_v0.44.0). Bridges DONE v0.45.0 (rivers through cities in
  new worlds, canals, DeceasedCraft's bridges; docs/city_engine.md "Rivers
  and bridges"). User look at 0.45.0: "the bridge show up just need change
  the design so the bridge match with other road"; v0.45.1: the bridge is
  the street part itself over the water (same blocks, same height, railings,
  broken hole in the wasteland), waiting for the user's look.

---

## Not started

### 3 new mobs
- **Requested**: 27 Juli 2026
- **Civilian**: new peaceful/passive NPC mob. Scope (combat behavior, spawn
  conditions, etc.) not yet defined - flesh out when picked up.
- **Trader fix**: trader NPCs already exist in the codebase. Since the
  7 Oktober 2026 naming pass the family is known: `deci.ai.a` TraderEntity
  (base), `deci.ai.v` ArmedTraderEntity, and about 20 types in `deci.ai.*`
  (ammo, armor, guns, food, medical, black market, car dealer, banker =
  `deci.ai.e`, ...); see `deobf/names.tsv`. Which one(s) the user means still
  needs confirming. They currently can
  only be spawned via a server command and is static - can't move. Goal: make
  it spawn normally (not command-only) and able to walk around like other
  mobs.
- **Spec-ops military**: new elite mob variant. Full Juggernaut armor
  (`ItemArmorJuggernaut` items already exist -
  `juggernautHelm/Vest/Pants/Boots`, see `deci.aD.k` around the `.setLootChance`
  calls) *or* a full black armor set + night vision goggles - two possible
  looks, decide which (or both as variants) when implementing. Heavy loadout,
  all weapons already exist in `deci.aD.k`, no new guns needed:
  - Rifles: SCAR-H (`fnscarhamr`/`arI` or `fnscar`/`arH` - confirm exact
    variant), SCAR SSR (`fnscarssr`/`arL`), M110 (`m110`/`arf`), FAL (`fal`/
    `ark`).
  - Snipers: SV98 (`sv98`/`asl`), JNG-90 (`jng90`/`asm`), Barrett (`barrett`/
    `asn` - user said "M82," confirm this existing entry is that model), AWM
    (not seen in the registered weapon list yet - confirm it exists or needs
    adding), SVD (`svd`/`asj`).

### New hostile NPCs and stronger bandits (plan, 9 Oktober 2026, "later")
- **Enemy military**: medium to high armor, rifles or heavy weapons.
- **Enemy juggernaut**: full juggernaut armor (`juggernautHelm/Vest/Pants/
  Boots` exist), heavy machine guns (PKM, M240, any machine gun in the
  registry) or a Barrett sniper with ridiculous damage (.50 BMG).
- Both spawn in military buildings / military areas (our MIL sectors,
  military zones, the wasteland district next to them).
- **Bandits**: heavier arsenal (PKM, SV98, militia guns of the medium to
  heavy class) and medium to heavy armor.
- Relates to "3 new mobs" (spec-ops military, weapon list there) and
  "More clothing variety" below; NPC shots now draw correct tracers
  (bug.md, fixed 9 Oktober 2026), and NPC gunfire respects armor (v0.9.0).
- Registry names checked 9 Oktober 2026 (level.dat ItemData): every gun
  and armor piece registers under its plain name (`deci:pkm`, `deci:sv98`,
  `deci:m240`, `deci:rpk74`, `deci:akm`, `deci:marineHelm`,
  `deci:spetsnazVest`, `deci:juggernautVest`...), so our code finds them
  with `GameRegistry.findItem("deci", name)`, no obfuscated names.

#### Step 1 design: NPC tiers (started 9 Oktober 2026, user: "Bandits +
military first, hard but fair")
How Decimation's armed NPCs work today (read from deobf source):
- BanditEntity (deci.ag.a, base of Soldier deci.ag.l, Hazmat deci.ag.c,
  Soviet deci.ag.m) rolls its gun and armor in the CONSTRUCTOR, on server
  and client separately. The gun is a plain field, put in the held slot
  every tick on both sides and never saved: the client shows its own
  random gun, and a reloaded NPC rolls a new one. Armor goes through
  vanilla equipment (saved, sent to the client on tracking start).
  Backpack / mask / vest are plain unsynced fields (client random).
- Health 20 for all of them. Shots: 75% hit, gun damage / 8 against
  non infected, delay 5..20 ticks (soldier 2..5), "human" source without
  the shooter.
- Player shots hit NPCs for the full gun damage: Decimation applies armor
  multipliers only when the victim is a player, so NPC armor is
  cosmetic. Runtime multipliers are the originals x 0.65 (our armor buff),
  a full bandit set would be x 0.17: far too strong to reuse for NPCs.
- Soldiers attack players with humanity under 50 only. Soviets (spetsnaz
  set, AK74 / AKS74U / AK12) are hostile to every player and every non
  Soviet human: Decimation's own enemy military. Bandit, Soldier, Soviet,
  Hazmat each spawn naturally at weight 1 in every biome (MobSpawnRegistry).

Design (our code, `net.decimation.fixes.NpcLoadouts` + `MilitarySpawner`):
- Every armed NPC gets a tier when it first joins a world (server), stored
  in its entity data (ForgeData "deciworldgen_tier", saved with it): gear
  (all 4 armor slots, so the client's own roll never shows), gun, max
  health, shot delay, and the share of a player's gun damage it takes
  (LivingHurtEvent, source "gunDeci"). Reloaded NPCs get gun and delay
  back from the stored data.
- The chosen gun goes to the client through data watcher slot 26 (gun
  registry name, added in EntityConstructing), applied on the client each
  tick, so the gun seen, its flash and sound match what shoots.
- Tiers (defaults, config `deciworldgen_npc.cfg`):
  bandit light (more clothing mixes, pistols / SMGs / old rifles), bandit
  medium (militia set, AKM / SKS / RPK / FAL / G3 class), bandit heavy
  (militia + steel helmet, PKM / RPK74 / RPD / SV98 / SVD); soldiers keep
  their side but get camo sets and NATO rifles; Soviets are the enemy
  military tier (spetsnaz set, AK74 family, PKP / RPK74 / SVD, more
  health, takes less damage). Machine guns fire faster, sniper rifles
  slower. Military areas (our MIL sectors and MILITARY zones) shift
  bandits to the heavier tiers.
- MilitarySpawner: near a player standing in a military area, groups of
  2 to 3 enemy military spawn 24 to 48 blocks away now and then, up to a
  cap nearby (not in peaceful).
- Juggernaut: step 2, same tier system (juggernaut set, PKM / M240 /
  Barrett, much more health).

Step 1 DONE in v0.30.0 (9 Oktober 2026), `fixes/NpcLoadouts`,
`fixes/MilitarySpawner`, `fixes/NpcKind`, Deci npcKind / npcGun /
setNpcGun / setNpcShotDelay / newSoviet; dev test mode `npc`
(`python3 tools/devtest.py npc`, about 70 s), all PASS:
- 60 bandits outside / inside a military sector: every one tiered with 4
  armor pieces, the tier's gun and health; medium + heavy 22..29 of 60
  outside, 38..47 inside. 12 soldiers, 12 Soviets: same checks.
- A player's 10 damage gun hit takes 10.0 (bandit light), 7.0 (bandit
  heavy), 6.0 (military).
- Client shows the server's gun for all 14 lineup NPCs (before: each side
  rolled its own). Military spawner places 2 to 3 tier military NPCs.
- checks + tracer modes still PASS (tracer 3.6 deg mean).
- Untiered soldiers in some runs: explained 9 Oktober 2026, Decimation's
  PlayerJoinSync cancels 5% of soldier spawns and spawns a Mech instead
  (also why mechs gather in the reused test world); the test now skips
  refused spawns.
Defaults (config/deciworldgen_npc.cfg): bandit light 20 hp, takes 100%,
weight 55 (military 25), Decimation's 5..20 tick delay; bandit medium 26
hp, 80%, weight 35 (45); bandit heavy 32 hp, 70%, weight 10 (30);
soldiers 24 hp, 80%, delay 2..6, camo marine / forest / urban / black;
military (Soviets) 40 hp, 60%, delay 3..9. Machine guns 3..8 ticks,
sniper rifles 25..45. Spawner: every 400 ticks per player in a military
sector, chance 0.5, group 2..3 at 24..48 blocks, cap 4 within 64.
Not seen by the user in game yet `[not verified]`; balance numbers are
first guesses for "hard but fair".
v0.33.0 (user: "remove vanilla mobs since this is a zombie mod";
`fixes/VanillaMobs`, config deciworldgen_mobs.cfg): vanilla zombies,
skeletons, creepers, spiders, cave spiders, endermen, slimes, witches,
silverfish, zombie pigmen, pigs, cows, sheep, chickens, horses, wolves,
ocelots, mooshrooms, squid, bats, villagers, iron golems and snow golems
leave every biome's spawn lists but Hell and Sky (postInit, after
DecimationBiomes.copySpawns) and are refused (and marked dead, so old
chunks drop them) when they join the overworld (spawners, villages, eggs,
breeding, old saves). Exact class match: Decimation's mobs that extend
vanilla classes stay. Config: removeVanillaMobs, keep (entity names).
Dev tests tag mobs they need with VanillaMobs.KEEP (the shots test pig).
Checked: checks mode 5 / 5 vanilla mobs refused, infected still spawn, 0
vanilla spawn entries; fresh seed 1 world: saved entities only Decimation's
(Bandit 43, Human 17, Boar 17, Hazmat 9, Buck 6, Doggo 2, Soviet, Mech).
Also: DecimationBiomes.copySpawns told vanilla animals by package name,
which only works in dev (the shipped game is obfuscated); it now also
checks the class. Juggernaut armor (user question): ArmorGunfireHandler
covers every ItemArmorDeci, juggernaut pieces included (body x 0.022 after
the 35% buff: an NPC rifle hit about 0.22 hp, a Barrett about 4.4)
`[not verified]` in game, computed from the formula the marine numbers
matched.

v0.32.2 (user: snipers should spot players further than infantry):
Decimation's findTarget looks 32 blocks around (15 up / down) for every
armed NPC. NpcLoadouts.sniperSearch: once a second, an NPC holding a sniper
rifle without a target looks sniperRange (96, config npc_fire) around (40
up / down) for the nearest enemy it can see (isHostileTo) and takes it as
its target (setRevengeTarget; vanilla drops it after 100 ticks, the search
finds it again while in sight). Sniper shots fly 160 blocks (others 96).
Checked (shots mode, range phase): enemy bandit 70 blocks away, elite
sniper spots it, elite machine gunner does not.

v0.32.1 (user: Barrett for the elite, split sniper eggs, "don't make it
rare"): juggernaut and elite_military are machine gun only now; new tiers
juggernaut_sniper (juggernaut set, Barrett with 8x, always) and
elite_sniper (elite gear, Barrett 4 in 8 / L115A3 / JNG90 / SV98 / M110,
8x, suppressor, laser, x2 damage). MilitarySpawner: sniperShare 0.5 of its
juggernauts / elites are the sniper version: a Barrett in about 12% of
groups (was 3%). Elite Barrett hit: 6 x 5 x 2 = 60 hp bare, about 23
through a marine body set (armor piercing). 14 eggs. Checked (npc mode):
40 / 40 juggernaut_sniper Barrett, 40 / 40 elite_sniper sniper rifles, 0
snipers in the base tiers, 14 / 14 eggs, client guns 24 / 24, goggles and
attachments 6 / 6; photo npc_snipers_v0.32.1.

v0.32.0 (user requests 9 Oktober 2026):
- ELITE MILITARY, tier "elite_military" (Soviet side): marine black set,
  night vision goggles (mask "nvgoggles"), heavy machine guns (PKP, M240,
  MK48, MG3, PKM) with 4x sight, MG suppressor and laser, or sniper rifles
  (L115A3, JNG90, SV98, M110) with 8x sight, rifle suppressor and laser;
  150 hp, takes 16% (about 59 rifle hits, 2 magazines), its hits x2 on a
  player (tier damageDealt, on top of x5), speed 0.27, knockback
  resistance 0.5. Only MilitarySpawner (eliteChance 0.2 per group) and its
  egg. The gun spec "name;sight=4x;barrel=..;grip=..;mask=.." is stored in
  ForgeData and synced through watcher slot 26, so the client shows the
  attachments (Decimation's NBT keys sightAttach / barrelAttach / ...) and
  the goggles (HumanEntity.setMask).
- AUTO FIRE: guns with an AUTO or BURST fire mode fire bursts (3..6 rounds,
  machine guns 6..12) at the gun's own rate (GunStats.secondsPerShot), each
  shot's spread x (1 + 0.35 x its place in the burst), then a pause (3 x
  the tier's delay). Sniper rifles stay single shot.
- MAGAZINES (user: "M16 30 rounds, then reload around 4 seconds"): an NPC
  fires its gun's magazine (Deci.gunMagazine: the first ammo item's
  capacity, M16 / AK 30, PKM belt 250; loose rounds or clips: the gun's
  capacity), then reloads reloadTicks (80, +-10) with the MagOut sound.
- Config category npc_fire: autoFire, recoilSpread, reloadTicks.
- Checked (dev test modes, fresh world, 39 PASS): elite 1.6 of a 10 hit,
  elite hit = 2 x normal, goggles and attachments on all 3 client lineup
  elites, 12 eggs; shots: AKM 12 bursts / 53 shots, first shots hit 7/12
  at 20 blocks vs 3/18 from the 4th on, first magazine 30 rounds then 82
  ticks; tracers 0.2 deg from the shot line. Photo npc_elite.png.
  Test trap: calling shootAt from a test while the AI also calls it
  halves every cooldown (reloads looked 2 s); the burst phase lets the AI
  shoot alone.

v0.31.0 (user choice, step 2 of the NPC plan): the JUGGERNAUT, tier
"juggernaut" (Soviet side, so hostile to every player): one matching
juggernaut set (normal or gray), PKM / PKP / M240 / MK48 or a Barrett,
200 hp, takes 25% of a player's gun damage (a 16 damage rifle hit does 4:
about 50 hits, near two magazines), walk speed 0.18 (others 0.25),
knockback resistance 1. Never rolled at random (weights 0): only
MilitarySpawner brings one (juggernautChance 0.15 per group, so only in
military sectors) and its egg. Barrett hits are armor piercing
(NpcShots.armorPiercing): ArmorGunfireHandler applies the square root of
the armor multiplier. Barrett hit (50 / 8 = 6, x5): 30 hp bare (one shot
kills), 11.6 through a full marine body set (normal 6 hit: 4.5). Checked
(npc mode, all PASS): 2.5 of a 10 hit, set / speed / knockback, piercing,
11 eggs, spawner group; lineup photo npc_3.

v0.30.4 (user request): rocket launchers. Tiers bandit_rpg (RPG-7,
weight 3, 8 in military sectors, 30 hp, takes 75%) and military_rpg
(RPG-18 standard rockets, about 1 in 10 Soviets: military weight 9 vs 1,
40 hp, takes 60%); a config written before (version != "2") gets the new
weights once. NpcShots fires a real RocketEntity (the player's rocket:
speed 1.5, gravity 0.002 per tick, smoke trail, explosion 6 without block
damage), aimed at the chest plus the gravity drop over the flight, spread
1.5 x the tier's; 60..100 ticks between rockets; no rocket when the target
is closer than 8 blocks or an ally stands within 1.5 of the line or 5 of
the target (a rocket explodes on anything). Fire sound deci:<gun>Fire.
Checked (shots mode): 7 to 8 rockets in 15 s at 24 blocks, 3 to 5 hurt the
pig; close shots show the launcher in hand and the rocket with its smoke.
New tiers sit at the end of the list (egg metadata = index), 10 eggs now.

v0.30.1 (user request): one spawn egg per tier, `deciworldgen:npc_egg`
(`fixes/NpcEgg`), metadata = index in NpcLoadouts' tier list, so new
tiers (juggernaut) get an egg on their own. Creative tab Misc, names
"Spawn Bandit (heavy)", "Spawn Soldier (marineforest)", "Spawn Military";
vanilla egg look, base colour per faction (Decimation's own egg colours),
spots per tier. Test mode npc: all 8 eggs spawn their tier.

### Military bases, US FOB style (v0.42.0, 10 Oktober 2026, waiting for the user's review)
User: "military zone with buildings ... our own generator, all three sizes, US FOB style ... really
really good output ... work on loots especially props placement". worldgen/military: combat
outpost (50 x 58), FOB (78 x 84), large FOB (112 x 118) in military sector sites (45 / 35 / 20 %),
turned 0..270, ground ramped around them, MILITARY zone. Perimeter (wire, HESCO, towers, ECP with
serpentine), TOC, B-hut living area with bunkers, logistics (ASP, fuel, motor pool, conexes,
hangar), services (DFAC, aid station, gym, helipads), loot in Decimation's crates. Details:
docs/military_base.md; skill decimation-military-base.

### Requests for later (10 and 11 Oktober 2026, the user: "save this feat/fix later")
Not started unless marked DONE; each one also has a line in docs/roadmap.md.
- **Gunshot noise** (DONE v0.43.0, section "Gunshot noise"): a shot from a gun without a suppressor draws attention; zombies walk to
  where the shot came from, NPCs react too; suppressed shots stay quiet.
- **Sniper NPC nerf** (DONE v0.42.5): sniper damage about 20 % lower, sniper range 70 blocks (config
  `sniperRange`, 96 now; the user said 90); Barrett NPCs (juggernaut_sniper, elite_sniper)
  about 40 % lower and health cut so one 5.56 magazine kills them.
- **NPC loot system**: what NPCs drop on death, per tier, how it works, rewards; design first
  with the user.
- **Smart NPCs**: open, ideas to be collected with the user.
- **Human Kills** and the **main menu Play button**: DONE in v0.42.2 (bug.md, Fixed).
- Military jeep / tank wrecks breaking with one punch: DONE v0.42.5 (bug.md).

### Gunshot noise (v0.43.0, 11 Oktober 2026)
User request (10 Oktober: "if player shot with non suppressor gun, it will draw attention to player
and zombie will be go to direction where the shot from. this applied to npc too"); choices
(11 Oktober): one range for every gun; zombies go to the spot and search; hostile NPCs
investigate; NPC shots draw zombies too; "if the shot from their team or group, they dont need
to go to direction ... except if its from the enemy or rival".
- fixes/GunNoise: a shot is heard 64 blocks away, 12 with a suppressor (barrelAttach on the
  gun, NPC guns too). Player shots: the held gun's ammo NBT drops (no packet hook); NPC shots:
  NpcShots. A burst is one noise (10 ticks, 8 blocks).
- Who reacts: every infected (InfectedEntity and the infected dog) in range; armed NPCs in range
  only when Decimation's isHostileTo says the shooter is their enemy (so their own side, and a
  player they do not fight, e.g. a bandit vs a player under humanity 50, are ignored).
- How: an AI task (Investigate; infected priority 3, NPCs 2, before their wander) walks to the
  spot in 12 block legs, then to random points within 6 blocks of it; search time 30 s
  (infected) / 40 s (NPCs). A mob with a target fights it instead.
- Config: config/deciworldgen_noise.cfg (enabled, range, suppressedRange, zombieSearchSeconds,
  npcSearchSeconds).
- Checked (dev test noise, 6 checks PASS): an infected 50 away comes 32 closer, one 76 away does
  not; a suppressed shot does not move one 30 away; a Soviet 43 away walks 32 toward a bandit's
  shot, a bandit does not; a real player shot (ammo 30 -> 29) brings an infected from 45 away.
  Not measured: frame time with many infected walking to a firefight [not verified].
- User test of 0.43.0 (11 Oktober): "enemy npc do nothing when me shooting near them", zombies
  react. Two causes (v0.43.2):
  - The user was in Creative. Decimation's isHostileTo is false for every creative player
    (bandit, soldier, Soviet, mech), so no NPC fights or investigates a creative player's shots;
    kept as Decimation's rule. In survival: Soviets react to everyone, bandits to humanity 50
    or more, soldiers and mechs to humanity under 50.
  - Bug: an NPC that once killed something kept the corpse as its old style target
    (entityToAttack is never cleared under the new AI) and never investigated again; Investigate
    now clears dead targets (bug.md "NPCs that once killed something ignored gunshots").
  - v0.43.3 (user: the juggernaut still did not react in survival): entityToAttack keeps the
    last enemy seen even while it lives on far away; busy now reads only the revenge target and
    the new AI target (bug.md "Enemy NPCs ignored gunshots, juggernaut too").
  - Accepted by the user on 0.43.3 (11 Oktober 2026): "nice it work now".
  - The dev test noise (v0.43.2) had 8 checks: a survival player's shot brings a Soviet from 40 away
    until it sees the player within 32 and fights (heard, came 12, engaged); a creative player's
    shot does not; the friendly bandit check reads whether it took the noise. The test removes
    mobs it did not spawn (stray infected on the arena made every NPC fight them). All PASS.

### v0.42.4: fourth review (11 Oktober 2026)
Ladders walkable from the base on every tower (5 of 6 were walled in), the outpost's TOC back,
a walk test for every base (ladders, doors, rooms, loot, traps; tools/props/walkcheck.py).

### v0.42.3: third review (11 Oktober 2026)
Tower ladders reachable (middle of the inner side), hangar roof closed with loot inside, motor
pool shelter with a full wooden roof; the "render issue" was worldgen below the sky test cutting
it (fixed in the test, docs/military_base.md section 6).

### v0.42.2: second review (11 Oktober 2026)
User list: AS Val sight position, TOC door side loot, barracks chests, DFAC radio, a busier
garage, the main menu (no session warning, green Play, local stats). Done: sights shifted onto the
AS Val's receiver with the aim compensated; Human Kills / Zombie Kills / Deaths kept locally
(fixes/LocalStats) on the HUD and menu, labels renamed (tools/patches/PatchMenuStats.java);
the base items (docs/military_base.md section 5). Checked: dev tests stats, gunview, milbase
(0 clashes).

### Military bases v0.42.1: user review fixes (11 Oktober 2026)
User review of v0.42.0 (28 shots, docs/shots/milbase_v0.42_review/): fix the orange dirt, the
raw dirt ASP berm, replace BlockMilitaryBarrier (FPS), make the TOC door a locked keycard door,
and every prop position. Done: deciworldgen:hesco (own texture), Door_Emergency_1_Locked + the
military keycard screen, gravel ground, sandbag ASP, the prop placement study
(docs/prop_placement.md, tools/props/propgeom.py + propclash.py, skill decimation-props),
Canvas.validateProps in the generator, every module re-laid (0 clashes). Tile entity props per
base: 1020 / 2007 / 2902 -> 129 / 381 / 571.

### EOTech 558, ACOG TA11 3.5x, Mk18 TS stock (DONE v0.41.0, 10 Oktober 2026, waiting for the user's verdict)
User: holographic 558 and ACOG 3.5x "so detailed ... same like in real life", the Mk18 with an IMI
Defense TS stock. tools/guns/sights.py (our art, committed): EOTech 558 (92 parts) and ACOG TA11
(117 parts) as deci:eotech558 / deci:ta11acog (NewSights, loot like the red dot / 4x), reticles
drawn in the glass while aiming (ScopeZoom: 1.25x red ring and dot, 3.5x BDC with green centre),
on every gun's rail (sightfit). Mk18: TS stock from the user's photo. Details: skill case 18.
v0.41.1 (user review): reticles drawn in the glass and swaying with the gun (SightReticle), EOTech
ring smaller (32 px pixel art), HK416 front sight folds away under a sight (skill case 19).
Verdict (user, 10 Oktober 2026): "its really good result"; front sights fold away only with a
sight attached (kept without one).

### Mk18 Mod 1 and iron sights that fold away (DONE v0.40.0, 10 Oktober 2026, waiting for the user's verdict)
User: "mk18 mod 1, black only", 9 photos; irons ready for attachments and hidden when one is
fitted. Mk18 from Decimation's M4A4 (tools/guns/mk18.py, generated locally, git ignored): DD RIS
II rail, flip up front sight, short barrel and flash hider (muzzle 16.1 vs 19.15), flamePos
moved, suppressor offset; registered as deciworldgen:mk18 (STANAG, damage 15, 800 rpm, recoil 7.5
/ 0.3, slowdown 0.12, M4A4 sounds and loot). Mechanic: every `defaultScopeModel*` part of a gun is
hidden while a sight is attached (fixes/IronSights, tools/patches/PatchIronSights.java): our Mk18
and 19 Decimation guns. Details: skill casebook case 16.

### HK416 and HK416 Tan (DONE v0.39.0, 10 Oktober 2026, waiting for the user's verdict)
User: "try hk416 ... make 2 version, black and tan", 5 photos; "two separate guns" (asked:
skin spray can vs two guns vs both). Variant route like the UMP9: tools/guns/hk416.py builds
both from Decimation's M4A4 (generated locally, git ignored). Kept: receiver, trigger group,
magazine, folding rear sight (aim and hands stay the M4A4's). Dropped by position (155 parts):
quad rail, gas block and A-frame, barrel and birdcage, collapsible stock, A2 grip. Ours (121
parts): HK rail handguard flush with the receiver rail (teeth y -3.6, 0.4 pitch; side and
bottom rails, slots), tall HK folding front sight (post tip -4.7 like the M4A4's), thin barrel
on the M4A4 bore and HK flash hider to the same muzzle x 19.15, HK slim line stock (ribbed
butt, angled panel with fins, buffer tube), raked HK grip with finger bumps and panels.
Lengths from the user's side photo (rear sight and muzzle as anchors: the butt then lands
within 0.03 of the M4A4's). Tan: the same model, the texture's islands recoloured flat dark
earth (each island brought to one tone, its gradation kept) except sights, barrel, muzzle,
trigger, bolt. Registered as deciworldgen:hk416 / hk416tan (STANAG: m4a4Mag and our 60 round),
damage 16, 800 rpm, recoil 6.5 / 0.2, the M4A4's sounds, loot where the M4A4 is.
Checked: renders next to the M4A4 (docs/shots/hk416_v0.39), aim crops with the M4A4 (rear
aperture and post tip on the same line), arSuppressor at the M4A4's place, red dot, reload 30,
gunperf within noise of the M4A4.
v0.39.1 (user review: "flying" pieces under the stock, front sight "like 2 pillar"): stock
lower panel rebuilt (square rear part plus angled wedge, ribbed band placed from the edge's
formula), every detail seated (new `study.py contact`: 46 parts not seated before, 1 after,
the flash hider collar); the "pillars" were the M4A4's rear sight reading texture rows past
its 32 high texture, which our taller texture filled with our islands (fixed: wrapped rows
copied, ours start below); front sight now a U of thick flared ears on a bridge, tops at
-4.74. Aim crops match the M4A4 again (docs/shots/hk416_v0.39.1). Skill case 14, lessons 15
and 16.

### UMP9 (DONE v0.38.0, 10 Oktober 2026; user: "the ump9 work really well and no problem")
Built as the user suggested: Decimation's UMP45 generated locally from
their Decimation.jar (tools/guns/ump9.py, outputs git ignored) with our
own curved 9 mm magazine (15 parts); registered only when the generated
model exists; ump9Mag 30 rounds (MP5A3 bullet and icon); damage 11, 650
rpm. Checked: hip / aim identical to the UMP45, reload 30. Details:
skill casebook case 10. The notes below were written before the
shortcut.
User request: the 9 mm UMP, Decimation has only the UMP45 (straight .45
magazine); the UMP9's magazine is curved like the RPK / AKM ones. Built
with the decimation-gun skill (second gun).
Reference measurements (Decimation's ump45, numbers only, nothing copied):
19.7 long, 9.9 high, 2.0 wide, 142 parts; x -5.35 to 14.3 (skeleton stock
extended, receiver about x 2 to 12.5, top rail, front sight hood at the
front, rear sight at the rear top, railed handguard with vents, short
barrel with a muzzle cap); magazine in front of the trigger guard at x
7.4 to 9.9, straight, leaning forward, 2 parts; flamePos (13.9, -3.75,
-0.15), rhPos (-5.6, 2.62, -2.0), lhPos (6.5, 8.12, 4.62); stats damage
13, 600 rpm, recoil 6 / 0.4, recovery 5, slowdown 0.14, AUTO / SINGLE.
Curved magazine recipes: AK74 11 parts, MP5A3 4 segments shifted forward
each (children of ammoModel0). Renders docs/shots/ump9_v0.38/
(ref_side.png, ump45_*.png). Waiting for the user's photos (lesson 1).

### MAC-10 gaps closed (v0.37.3, 10 Oktober 2026)
User: "perfect is perfect, we close the gaps until the mac10 looks really
like decimation guns". New `study.py gaps` (ours vs the 13 Decimation
SMGs). Shape kinds 60 / 31 / 5 / 5 % (Decimation 60 / 31 / 5 / 4), 110
parts, declared sizes like Decimation's (gunmodel.size / layout: long
parts 1x1xN with gradation along them), tone 47, middle details from the
photos (rivets, housing plates, SAFE / FIRE lever, deflector, slot
notch), raised details 0.08 proud. Only the short real length and what
follows from it stay outside. Checked: geometry unchanged by the size
change, aim holes on the line with the Uzi, suppressor unchanged, reload
30. Details: skill casebook case 9.

### MAC-10 accepted (v0.37.2, 10 Oktober 2026)
User verdict in game: aim fixed, "i like the style of the gun firing",
icon "good art". Screenshot docs/shots/mac10_v0.37/user_v0372_final.png.
Player firing, fire / rack animations in first person and the icon are
now verified by the user.

### MAC-10 v2 revision 2 (DONE v0.37.2, 10 Oktober 2026)
User review of 0.37.1 with screenshots (MAC-10 and Uzi aiming, MAC-10
hip view with the suppressor): suppressor not matching the muzzle and
still low; aim compared with the Uzi; asked that every revision updates
the skill.
- Suppressor: flamePos y must sit 0.85 above the bore (Decimation's
  convention, Uzi / UMP45); now -4.75 over the bore at -3.9. Offset
  (-2.37, -0.16, 0): it screws over the threads up to the receiver front
  like the real one (user photo 2), centred on the bore.
- Aim: the user's Uzi shot shows the centre in the Uzi's rear aperture
  hole; ours sat 0.25 higher. Rear aperture hole now -4.8 to -4.55, front
  post tip -4.67, stock loop top lowered to -4.57. Checked in the same
  gunview run as the Uzi: both holes on the centre line.
- Skill .claude/skills/decimation-gun: photos first, corrected anchors,
  side by side aim check, attachment check, revision log; style guide
  sections 9, 13, 14 corrected.

### MAC-10 v2 revision (DONE v0.37.1, 10 Oktober 2026)
User review of 0.37.0 ("huge upgrade"): 1. aim not centred on the
crosshair, 2. suppressor flying, 3. no gradation like Decimation's guns.
- 2: the barrel attachment formula meets the muzzle only on guns with
  flamePos x 12 to 15; the MAC-10 gap was 1.12 units (also in the user's
  shot). New `Deci.offsetAttachment` (our copy of the attachment model in
  AttachmentItem.ST with an offset), NewGuns moves smgSuppressor by
  (-1.02, +0.14, 0). Checked in game: flush on the muzzle (bandit side
  view, first person).
- 3: gunmodel.paint gradation from measured Decimation textures (style
  guide section 4): 37 part tones, face spread 5.2, noise 2.1.
- 1: steady aim (gunview, camera held) puts the sight tops on the screen
  centre exactly like the Uzi, and the rear sight matches the Uzi's
  build; the user's shot shows them 0.2 units high, which matches
  Decimation's aim sway after a mouse move `[inferred]`. Not changed;
  asked the user to compare with the Uzi in the same moment.

### MAC-10 v2 in Decimation's style (DONE v0.37.0, 10 Oktober 2026)
After the user's "not good, needs polish" and the gun study
(docs/gun_style_guide.md, skill .claude/skills/decimation-gun).
- tools/guns/gunmodel.py v2: every part a hexahedron (box bent with inset
  / shift / mirror / octagon), written as Decimation does (1x1x1 declared,
  shape in the corner offsets), one flat tone per part (textureWidth 512,
  UV step 8, 2 px a unit), icon from the model's side render, animations
  with Decimation's timings (Fire 2 frames slide only, Reload1 57,
  Rack 19 Hand 1, new SlideBack static), our own keyframe values.
- tools/guns/mac10.py v2: 102 parts (93 body, 4 slide: knob halves with
  the sight notch, stem, bolt in the ejection port; 5 magazine), 9.35 x
  8.1 x 1.5 units (v1: 23 plain boxes, 12 x 10.2 x 2.2). Proportions
  measured on the user's side photo (receiver = 7.0 units): raked grip
  behind the mag housing, threads right at the receiver, thin barrel,
  knob forward, front sight, strap loop, butt pad, the folded wire loop
  over the rear top, left side stamping and pins, ejection port with the
  bolt on the right (-z). Sight tops y -4.95 to -5.0, receiver top -4.45.
- Checked: study.py renders next to the Uzi; in game (gunview, gun):
  dark slim gun where the Uzi sits, the rear seen inside the wire loop
  frame (no grey block), aim centre exactly at the sight tops (v1: 0.75
  above), red dot on the receiver and its ring at the aim centre,
  suppressor on the muzzle, flashlight; reload loads 30 (animation:
  tilt, magazine out and in), a bandit holds and fires it. Not checked:
  the player firing it (mouse) and the feel `[not verified]`; icon only
  seen as a file.
- Shape kinds 52 % taper / 44 % cuboid / 2 / 2 (Decimation 60 / 31 / 5 /
  4): more tapered chamfers would move it closer `[idea]`.

### MAC-10, the first gun of our own (DONE v0.36.0, 9 Oktober 2026)
User: a gun made by Claude with tools (docs/roadmap.md pilot); user picked
the MAC-10. Pipeline in docs/gun_model_spec.md section 6. Model: 23 boxes
of our own (boxy receiver with ejection port, threaded barrel, sights,
checkered grip with the magazine inside, trigger guard, strap lug,
retracted wire stock, cocking knob as slideModel0), texture and icon
painted by script, animations Fire / Reload1 / Rack written by us. Gun:
smg, .45 ACP (the UMP45 / Uzi rounds), 30 round mac10Mag (Uzi mag icon),
1100 rpm, damage 12, slowdown 0.08, the Uzi's sounds; in the loot pools
of the Uzi (8) and its magazine (8), and in the light bandits' guns.
Checked (dev test mode gun): registered, takes its magazine, the reload
key loads 30 rounds (control: the Uzi loads its 32 the same way), a
bandit holds it (third person) and fires it (tracers); first person it
renders where the Uzi does, lighter texture after a first run that was
almost black. Not testable here: the player firing it (mouse read
directly) `[not verified]`; how it looks while aiming and the fire / rack
animations in first person `[not verified]`. Tools: headless Blockbench
MCP (registered for later sessions, `claude mcp add blockbench`), our
client tools/bbmcp.py for this session.

### 60 round magazines (DONE v0.35.0, 9 Oktober 2026)
`fixes/Magazines`, Deci newMagazine / gunTakes / addGunMagazine /
addLootLike. Read from deobf: a magazine is an AmmoItem (deci.ay.f: name,
feed type mag, capacity, stack 3, bullet item adI) registered by its own
constructor; a gun keeps the mags it loads in ammoTypes (deci.ay.i.aep,
copied at construction: the AK family shares ak74 / aks74u / rpk74 / ak12
mags, the M16 family the STANAG list); its max ammo is the loaded mag's
capacity. Loot: LootTableRegistry (DecimationMod.getLootTable) builds item
lists per container block in Decimation's preInit (entries awA, package
private class, lists awC), then pools (init).
- 60rnd NATO STANAG Magazine (deciworldgen:stanag60Mag, 5.56): M16A1 /
  A2, M231, AR-15, XM177, Honey Badger (Decimation's STANAG list) plus M4A4,
  ACR, L85A1, SCAR-L (own mag in Decimation, STANAG in reality). Not FAMAS,
  G36C, AUG, Galil, INSAS. Icon of m16a2Mag.
- 60rnd AK Magazine (deciworldgen:ak60Mag, 5.45): every gun taking ak74Mag
  (AK-74, AKS-74, AKS-74U, AK-12, RPK-74, and AKM / AKMS as Decimation
  shares that list). Icon of the longer rpk74Mag.
- Names in assets/deciworldgen/lang/en_US.lang (also names the egg items).
- Loot: one in every pool already holding the 30 round mag (8 STANAG pools,
  7 AK pools). Black market trader offers not added `[not decided]`.
- Checked (checks mode): right guns take them, wrong ones not, capacity
  60; hotbar photo npc_mags (names, icons). Reloading a gun with one in
  game `[not verified]`.

### Wasteland district = military area (DONE v0.54.0, 12 Oktober 2026)
User: "go with 1, wasteland military area. do full system now" (docs/ideas.md item 14). Full
design, checks and code map: docs/wasteland.md. DeceasedCraft checked first (legacy 5.5.5 data):
its wasteland holds the military base, camp and bunker with military loot, the rest industry with
industrial loot. Ours: the whole district one MILITARY zone, military buildings more often (new
worlds), military loot in its containers, army supplies, wartime stories, zone entry checkpoints,
military vehicles, fighting positions, military lot scenes, enemy military groups on its streets.
Dev test `wasteland`. v0.54.1: the weak spots (alike military buildings, few lots, unseen lot
scenes, patrol spots) fixed, docs/wasteland.md "v0.54.1". v0.54.2: motor pool and crash site
scenes reworked (docs/wasteland.md "v0.54.2"). Waiting for the user's look.

### Rifles for the elite and the juggernaut, Decimation doors, city edge, locked wasteland (DONE v0.55.0, 13 Oktober 2026)
User: "elite npc and juggernaut have rifle now, from marksman and normal automatic rifle, combined with
attachement (50% change) full or half attachment. and add gradient texture block on border of
wildneres cause its looks not natural when see from my view. door never use vanilla ones, use
decimation door instead on all world in decimation. for wasteland, 80% most building its locked door,
so if the building has more loots + high value in there, you put locked door decimation in there".
Choices asked: the border is the city edge (not dead land vs overgrown), locked doors open with a
keycard screen or a lockpick, all four in one release.
- Rifles (fixes/NpcLoadouts): juggernaut and elite_military carry a rifle 6 in 10 (config
  `rifleShare` per tier): automatic (ak74, ak12, akm, m4a4, hk416, mk18, fnscarl, g36c, acr, aug3) 6 in
  10, marksman (m110, svd, m14, galilsar, fnscarssr) the rest; their machine guns otherwise. A rifle's
  attachments (rifleAttachments): none 1 in 2, else a full set (sight, arSuppressor, grip) or half of
  one (1 or 2 slots); marksman sights 4x / 8x / TA11 ACOG (the SVD its Dragunov scope), automatic red
  dot / 2x / EOTech / ACOG / 4x; grips foregrip / laser / flashlight; no sight on a gun with a built in
  scope. Dev test `npc`: 121 of 200 with a rifle (none 62, half 35, full 24), the client gets the
  server's attachments; `gunview guns=m110,ak12 attach=4x,arSuppressor,foregrip`: attachments drawn on
  the NPC's gun. Snipers tiers unchanged (the Barrett, elite_sniper).
- Decimation doors everywhere we generate (worldgen/Doors): every vanilla wooden / iron door written
  by Slices or the small schematic placer becomes a Decimation door with the same metadata, styled
  per structure (city buildings by kind: homes dark wood / blue / orange / green, offices dark wood /
  blue, shops coloured with windows, clinics green / emergency, police blue / security, industry and
  military metal / security / emergency, labs security; bases metal, camps dark wood, the refuge
  emergency / metal; iron doors metal). Seed 1 area: 0 vanilla doors. Chunks generated before keep
  theirs.
- City edge (city/EdgePlan.wear): the ramp's grass within 10 of the city turns into broken pavement
  in patches, denser at the city: the sidewalk's slab and stone next to it, then cobblestone, then
  mossy cobblestone. Dev test `cityedge`: worn tops by distance 62 53 41 26 14 7 1 0 of 64.
- Locked wasteland (BuildingDress.lockDoors, docs/wasteland.md "v0.55.0").

### The wasteland city apart, its fence, the bunker; ramps, bridges, stations (DONE v0.56.0, 13 Oktober 2026)
User: bunker "photo the bunker and make it lockable ... highest loot ... most harder place building with
full of high tier enemy npc in there. elite npc + juggernaut and full military outfit zombie spawner";
"make full covered fence using decimation block? with barbed wire and full of landmine on the edge side
of city. this is only applicable on wasteland city"; "cars portal in there like toll gate and military
post gate"; answers: the fence on the wasteland district square, "split out the wasteland city with other
city", chain link + wire, asphalt ramp for the street stairs, bridge damage "gradient, dont put retacle
hole"; and the subway station "on side building instead on road". Details: docs/wasteland.md "v0.56.0",
docs/city_engine.md "Open" (v0.56.0). The city edge gradient of v0.55.0 accepted ("i like it").

### Main menu: server pages coming soon, Early access, Chernarus (DONE v0.64.1, 17 Oktober 2026)
User: "for server status, changed it like this: Global Server, Community Server, Download, with that name 3 of them
for the offline"; "for my supporter, change the name to Early access"; "for server that is down, replace it with
Bukovka and Chernarus only and its say coming soon, same with player online its say coming soon instead of unknown,
with same community to when notification appear, its say community server coming soon instead of failed to load";
five Chernarus pictures. Official list rows from fixes/MenuServers (PatchOfficialServers), texts by PatchMenuTexts (47
in 12 classes), Chernarus pictures and the player count line by tools/menu_assets.py. Dev test menupages RED (every
check) then PASS 9 of 9; menu, stats, mainmenu PASS. Later: a forum on our website (roadmap UI 3), real servers (UI 4).
Details: docs/main_menu.md section 2c.

### Main menu: links, our name and version, Continue, our corner (DONE v0.64.0, 17 Oktober 2026)
User picked from suggestions: "Links to our site", "Replace FURY advert", "Our version shown" ("for version replace
version code with our version code name, and for decimation replace with deciremnant"), "Continue last world"; and
"replace the picture of news panel to real topic image". Website / GitHub links, DeciRemnant and our version in the
title line, under the logo, the welcome box and the window title, Continue under Play, our corner art, topic pictures.
Dev test mainmenu RED then PASS; menu, stats PASS. Details: docs/main_menu.md section 2b.

### Main menu: our news in Decimation's news panels, real scene slideshow (DONE v0.63.0, 17 Oktober 2026)
User: "lets make the news panel now. 1. github repo, 2. update release, 3. our website release, 4. Coming soon
feature"; "replace with real scene with your suggestion animation"; "just use component from decimation gui menu its
self, no need to add something new on it"; "make the background image, changed every 7 second". Built with Decimation's
own parts: four of its news panels (ArticlePanel) in its own news grid, its own background drawing with six real scenes
of our world, 7 s each with a fade, the black layer and lava cube planet removed (PatchMenuBackground, three jars).
Checked in the dev client (dev test mainmenu, RED then PASS twice; menu and stats still PASS). Waiting for the user's
look in game (needs a release: their Prism has the old jars). Details: docs/main_menu.md.

### Zones, factions and world (user list 9 Oktober 2026, "later")
Only the list and starting points; scope of each is `[not decided]`, ask
the user before building.
1. Advanced safezone. Exists: Decimation zone type SAFEZONE (ZoneManager,
   infected spawns cancelled inside; explosions skip it, RocketEntity
   checks it); our ZoneStore tags generated structures, no safezones yet.
   User choices (11 Oktober 2026, after the bridges): "create safezone maybe
   that have trader + guard inside"; where: BOTH one per city and own sites
   outside cities (roadside / countryside camps); look: BOTH a walled
   survivor camp built by our own generator (FOB style: scrap and sandbag
   walls, guarded gate, trader stalls, tents, campfire, medical tent) and a
   fortified city building (barricaded, traders on the ground floor);
   guards friendly unless provoked (they shoot infected, bandits and
   Soviets near the zone, turn on a player who shoots inside or hits
   someone); people inside: traders, guards, unarmed civilians and armed
   survivors (item 5 joins this). Next: research the existing pieces
   (Decimation trader entity, civilian FactionHumanEntity, faction rules, zone
   registration) and a plan for the user.
   What Decimation already has (deobf/names.tsv, 11 Oktober 2026, read from
   the names table only; behaviour `[not verified]` until the classes are
   read):
   - Traders: TraderEntity deci.ai.a (FactionHumanEntity, static offer list
     per class, opens TraderScreen; PacketTraderTransaction buys / sells for
     caps or gold caps) and 19 subclasses: ammunition, armor, backpack,
     banker, black market, bounty, car dealer, convenience, drug dealer,
     food, fortification, gold cap, GuardTrader deci.ai.n ("armed guard NPC
     protecting a safezone"), guitar player, guns, heavy guns, medical,
     scrap, skins (+ Santa / witch). ArmedTraderEntity deci.ai.v shoots
     infected and bandits within 32 blocks with its held gun.
   - Placement on the official server: TraderSpawnManager
     (net.decimation.mod.server.traders, decimation_traders.json, removes
     all trader entities and respawns them), server proxy only: in
     singleplayer we place and keep them ourselves.
   - Safezone rules: SafezoneAttackHandler deci.aK.g (cancels attacks in
     safezones and in the leave grace timer), FriendlyFireHandler deci.aK.i,
     EntitySpawnZoneHandler deci.aK.d (blocks hostiles in safezones),
     PlayerZoneTickHandler deci.aK.m (grace timer): all ServerProxy, so dead
     in singleplayer like the others `[inferred]` (our ZoneSpawnHandler
     covers the spawn side since v0.7.0). TraderAndSafezoneAttackGuard
     deci.an.a (both sides): traders cannot be attacked.
   - Props: BlockSafezoneSignEntry / Exit (signs at a safezone gate).
   - Civilians: FactionHumanEntity deci.ah.d (faction CIVILIAN, unarmed);
     NpcFaction deci.ae.b: Bandit, Military, Civilian, Soviet.
   Open design point for the plan: Decimation's safezone cancels every
   attack inside, while the user wants guards to turn on a player who
   shoots or hits someone inside; the plan must say what "provoked" means
   (a shot fired inside, an attack attempt on a trader / civilian).
   User answer (11 Oktober 2026): "since there is safezone zones in decimation, you can use that so
   if player shot, the trader or any npc or any player can be safe. learn how zone working ...
   end by end"; destroying a safezone later, plan in docs (docs/zones.md section 8).
   Step 1 DONE v0.46.0 (fixes/ZoneRules, docs/zones.md): Decimation's zone rules working in
   singleplayer plus protection of every friendly entity in a safezone; provoked = a player
   attacks someone protected inside: no damage, guards and soldiers within 64 blocks fire on them
   for 60 s after the last offence, their combat timer runs (no safezone protection). Dev test
   `zones` 19 PASS. Provocation REMOVED in v0.47.2 (user: "its easily accidently hit friendly
   npc so i guess in safezone dont need that feature"): a hit on a protected one just does
   nothing.
   Step 2 DONE v0.47.0 (docs/safezone.md): survivor camps in wild / civilian sectors (5 % of
   sites, new worlds only): scrap wall, 4 watchtowers, guarded gate with safezone signs, market
   street of 6 traders, plaza with campfire and water, medical tent, family tents, latrines,
   generator, garden; residents 6 guards, 6 traders, 5 civilians, 3 armed survivors (tier
   "survivor"), kept by SafezoneSites. Dev test `camp` all PASS, propclash and walkcheck 0.
   Next: fortified city building.
2. Player claimable zone areas. Exists: Decimation's TURF system
   (net.decimation.mod.server.turf: TurfManager add / remove / owner /
   isCapturable, turfs json, TurfCommands), server proxy only, so likely
   dead in singleplayer like the other ServerProxy handlers
   `[not verified]`; check before writing a new one.
3. Advanced claims: a claimed zone can be taken back now and then by NPCs
   (raids by bandits / enemy military). Builds on 2 and the NPC tiers.
4. Police NPC. Exists: police infected look (NYPD set), NYPD armor items;
   no living police NPC. Would be a new tier on an existing human class
   (as the elites) or a faction `[not decided]`.
5. Survivor civilians with a minimal weapon. Exists: civilian humans
   (FactionHumanEntity, faction CIVILIAN, entity "Human", unarmed); bandits hunt
   them, Soviets too since the faction fix.
6. Radiated areas. Exists: our "Irradiated Military Zone" biome (no
   effect yet), hazmat suits and gas masks as items.
7. Advanced military base. Exists: military sectors with mil_ schematics,
   MILITARY zones, MilitarySpawner groups (juggernaut / elite / snipers).
   Update: since v0.42.0 / v0.42.1 our own US FOB style bases replace the
   mil_ schematics (docs/military_base.md).
8. Advanced bandits. Exists: bandit tiers (light / medium / heavy / rpg).
   Step 3 DONE v0.48.0 (docs/safezone.md section 7): city refuges. User choice (11 Oktober
   2026): our own building on a whole city block (not a fortified DeceasedCraft building), on
   the block nearest each city region's middle. 48 x 48: barriers and wire at the street, a
   concrete wall, gate with a sandbag chicane and two guards, a three storey concrete building
   (lobby with registration desk, market hall of 6 stalls, clinic, canteen; dormitory; armory,
   guard bunks, command; roof with 4 guard nests, spotlight, flag), yard with tents, aid
   distribution, APC, generator, water, garden; 22 residents. New worlds only ("!cityRefuges").

### More zombie variants, 60 round magazines, NPC bullet impacts (9 Oktober 2026, "later")
#### Zombie variants design (started 9 Oktober 2026, user choice)
Read from deobf source: InfectedEntity (deci.ag.d) is a HumanEntity with
skins from the "infected" folder, 20 hp, attack 3, speed set EVERY TICK in
onLivingUpdate (0.25, 0.3 in a horde of 10+), 8% bite infection. Variant
(watcher 21): COMMON, MILITARY (marine set), POLICE (NYPD set), the armor
re-applied every tick while it holds nothing; zones pick military / police
for 25%. Specials exist as own classes: crawler, dog, frail (screams),
gawker, hulk, bloater (10% of infected spawns become hulk / bloater).
Plan (`fixes/InfectedVariants`, tag "deciworldgen_zvariant", eggs):
- runner (12%): civilian clothes (hoodie, casual), 14 hp, speed x1.55;
- riot (8%): combat helmet + police clothes, 30 hp, takes 50% of player
  gun damage, speed x0.85;
- screamer (5%): hazmat suit, 16 hp; when it gets a target it screams
  (deci:mob.frail.scream) and every infected within 32 blocks takes the
  same target (once per 10 s);
- night frenzy: at night every infected walks x1.2 and hits +2.
Speed through attribute MODIFIERS (the base value is reset every tick).
Only plain infected with the common variant get one (zone looks stay).
DONE v0.34.0 (`fixes/InfectedVariants`, `fixes/ZombieEgg`
deciworldgen:zombie_egg, config deciworldgen_zombies.cfg). Night frenzy
speed uses modifier operation 2 (multiplies on top of a variant's speed;
operation 1 amounts add up: riot 0.85 + night 0.2 gave 1.05). The join
handler runs at LOWEST priority, after the zone looks. Checked (dev test
mode zombies, about 30 s, all PASS): 265 infected (the rest became hulks /
bloaters): runner 31, riot 21, screamer 14; runner speed x1.55, health per
variant; riot takes 5.0 of a 10 gun hit; a scream reached all 6 infected
25 blocks away (49 in range); night x1.20 speed and +2 attack, off by day;
lineup photo zombies_v0.34.0. Not seen by the user in game yet
`[not verified]`.

- More infected / zombie variants.
- New magazines: 60 round STANAG and 60 round 5.45 AK.
- Bullet impact particles on blocks hit by NPC shots (today only player
  shots make them; NPC shots damage the target directly, see bug.md
  "NPC tracers", the tracer patch now knows the target).

### More clothing variety on military/bandit NPCs
- **Requested**: 27 Juli 2026
- Bandits already have *some* randomization - `deci.ag.a` picks from small
  item pools (`aam`/`aan`/`aao`/`aap`, 2 options each) per armor slot on
  spawn. User wants more variety (bigger pools, more distinct looks) so NPCs
  don't all read as "one type" - and the same treatment extended to
  soldier-type mobs, which currently use fixed (non-randomized) gear.

---

## Documented, not started

### Custom weapon creation
- **Requested**: 27 Juli 2026
- Full how-to written: `create_weapons.md` - covers the `.bmodel` model format
  (turns out to be plain text Techne-style code, not a proprietary binary
  format), the `deci.aD.k` item-registration pattern, and two recipes: reskin
  an existing weapon (fast, no new 3D model needed) vs. a genuinely new model
  (needs Techne - Claude can't usefully author the 3D shape itself, no visual
  feedback loop and texture painting is out of scope).
- **Next step** (not started, user said "later if I have long time"): pick a
  base weapon to clone for a first reskin test, or start modeling in Techne
  for a new shape.

# Bug Tracker - Decimation Mod

Status values: `Open` (found, not fixed) / `Fixed` / `Pending decision` (fix
known, waiting on user go-ahead).

For "known non-bugs" (behavior that looks broken but is original mod design,
not something we introduced or need to fix) see `documentation.md`.

---

## Open


### Converted city props drawn into walls, floating, facing walls; cars in the sidewalks (found 11 Oktober 2026, FIXED v0.49.0)
- Found measuring a seed 1 world (tools/props/worlddump.py + propclash.py): 3609 findings over
  11009 props, 93 % inside converted DeceasedCraft buildings.
- Causes: tools/lctranslate.py maps 1.20 furniture to a Decimation prop per cell (wood crates
  are drawn 1.38 long; hung lamps and wall shelves became floor props in the air; facing kept
  from the 1.20 state); props of turned street parts / scenes / fronts / parks were not turned
  (Rotation leaves Decimation blocks); our wrecks in DC street parts sat on lanes 6 and 9 of a
  6 wide road although a car is drawn 3.85 wide.
- Fix: city/PropFit at load (docs/city_engine.md "Props fitted to their drawn size"), props
  turned in LcCity.partAt, one centred and fit-tested wreck per chunk (StreetPlan.carFits).
  Same area after: 30 findings. Checker fixes on the way (trapdoors counted as doors, doors
  flagged for props beside them along the wall, pressure plates as full blocks).
- New chunks only. Left: about 14 crates in crate runs with a 0.19 overhang (loot kept rather
  than a third chest in a row), lamp arm false positives.

### Railway tunnels flooded with standing water and lava (11 Oktober 2026, found in testing, FIXED v0.44.0)
- Dev test `rail` (first runs): diamonds of standing water and lava on the track, no source
  block anywhere, rails washed out (43 of 48 through a station).
- Cause: a tunnel chunk is written by two population windows (Slices.window, offset 8). Between
  them a neighbour chunk's decoration found the open stone face of the first half: vanilla
  springs (BiomeDecorator, 50 water and 20 lava per chunk, WorldGenLiquids) go into stone with
  exactly one open side, and they flow at once (immediate updates during worldgen). The second
  half then wrote its tunnel over the spring block and left the flow standing.
- Fix (terrain/TerrainEvents): Decorate LAKE (the springs) and Populate LAKE / LAVA denied where
  their area touches a rail chunk (city/Rails.near). Checked: liquid in 11 tunnel chunks 0 (fresh
  region), seed 1 gap line 0. A second, unrelated flood: the station staircase's water wall
  (DeceasedCraft part) does not hold its water in 1.7.10; it became light blue glass.
- City cellars could meet the same springs `[not checked]`.

### Dedicated server crashed at start since v0.42.2 (found 11 Oktober 2026, FIXED v0.44.0)
- tools/servertest.py: "NoClassDefFoundError: net/minecraft/client/gui/GuiScreen" from
  Magazines.register (the first call into fixes/Deci).
- Cause: Deci.homeMenu() (v0.42.2, for the stats test picture) returns a GuiScreen; verifying Deci
  on a server loads that client class. Singleplayer was never affected.
- Fix: @SideOnly(CLIENT) on homeMenu (stripped on the server). servertest runs again.

### Enemy NPCs ignored gunshots, juggernaut too (11 Oktober 2026, FIXED v0.43.3)
- User on 0.43.2: "even on survival mode the juggernaut dont react when i shooting when there is
  no zombies". v0.43.2 (below) fixed only dead targets.
- Cause: Decimation's NPCs fight their revenge target (getAITarget): findTarget sets it together
  with the old style entityToAttack, vanilla drops the revenge target about 5 s later or when it
  dies, then findTarget looks again. entityToAttack is only ever overwritten by the next enemy
  found, never cleared, so it keeps the last enemy seen even while that one lives far away or out
  of sight. GunNoise's busy check read entityToAttack: an NPC that had ever seen any enemy counted
  as fighting forever, and with no zombies near nothing replaced it.
- Reproduced (dev test noise -Ptier=juggernaut -Pfar=45, stale living target held, a pig behind
  it): old code heard the shot and moved -0.1 blocks; fixed: came 13 to 15 and engaged.
- Fix (fixes/GunNoise Investigate.busy): busy only while the revenge target or the new AI target
  is alive; entityToAttack is ignored.
- Seen on the way (dev test, juggernaut 20 blocks away in sight, survival): Decimation's own
  targeting works, it engaged and hit the player on 8 ticks; in creative 0.
- Test hardening: noise test removes stray mobs, respawns infected Decimation swapped for a
  hulk / bloater, checks "took the noise" where wandering made distances flaky, counts hits on
  a survival player (damage on, health 100000); a sniper rifle Soviet engages from up to 70
  (sniperSearch) without walking, which counts. 4 runs in a row all PASS (10 checks).
- Confirmed by the user in game on 0.43.3: "nice it work now".

### NPCs that once killed something ignored gunshots (11 Oktober 2026, FIXED v0.43.2)
- User (0.43.0): "enemy npc do nothing when me shooting near them"; zombies did react. They were
  in Creative, where Decimation's NPCs never react to a player at all (isHostileTo is false for
  creative players in every NPC class: kept, new_feature.md "Gunshot noise").
- Found while testing the survival case: every NPC on the test arena showed a DEAD infected as
  its old style target (EntityCreature.entityToAttack). Decimation's NPCs run the new AI, so
  vanilla's updateEntityActionState, which clears a dead entityToAttack, never runs; Decimation's
  findTarget sets it and nothing clears it. GunNoise's Investigate counted that as "busy
  fighting", so an NPC that had killed anything never investigated a shot again.
- Fix (fixes/GunNoise Investigate.busy): a dead entityToAttack or attack target is cleared.
  Not enough: see the v0.43.3 entry above (a living stale entityToAttack blocked too).
- Check: dev test noise, 8 checks PASS: Soviet walks 19.8 toward a bandit's shot; survival player
  shot: Soviet heard, came 12, engaged the player; creative player shot: not heard. Not yet tried
  by the user in survival [not verified].

### Gun creeps down while aiming through a scope until it leaves the screen (11 Oktober 2026, FIXED v0.43.1)
- User (AK-74 with the ACOG, screenshots docs/shots/aimdrift_v0.43.1/user_116.png, user_117.png):
  "the guns go to below ... second by second until the gun dont show up"; they suspected the
  window size. Both shots have sniper NPCs in view; the scope sits down right of the centre, then
  the gun is nearly gone.
- Cause: ScopeZoom centres the sight by measuring where the scope glass is drawn (PatchScope adds
  a GL feedback measurement to Decimation's renderScopeGlass, every 8th draw). That method draws
  EVERY scope glass, also the scoped guns NPCs hold in the world. A measurement of an NPC's glass
  was taken for the player's sight: ScopeZoom learned a wrong position and pushed the gun away
  (reproduced: elite sniper NPC in view, gun shift x -3.89, gun off screen; without the NPC 0.005
  / -0.22). The window size was not the cause (1160 x 650 and 854 x 480 gave the same shift).
- Fix (fixes/ScopeZoom): measuring is off (mask -1) except during the player's own hand draw:
  onRenderTick switches it off at the start of every frame, onRenderHand switches it on just
  before the hand (ours while zoomed, vanilla's at hip) and off right after; a sample is learned
  only when it was taken inside that draw.
- Check: dev test `aimdrift` (AimDriftTest: AK-74 + ACOG aimed 15 s with an elite sniper NPC in
  view, -Psize / -Pturn / -Pnpc): old code shift -3.8931, 0.1726 (gun gone); fixed 0.0049,
  -0.2189 at 3, 8 and 15 s, same as with no NPC; sheet docs/shots/aimdrift_v0.43.1/
  sheet_fixed_npc_in_view.png. Not yet confirmed by the user in their own game [not verified].

### The user's dev world would not load: broken level.dat id table (11 Oktober 2026, FIXED v0.42.5)
- User: "world got corrupted or idk". The live test game (npckill) ran in deciworldgen_autotest,
  the world the user plays in. A cancelled command still sent "quit": DevAutoTest called
  mc.shutdown() inside the world, and the server's save and the client shutdown hook's save both
  wrote level.dat at 03:59:29; the file kept had 603 item ids missing (592 deci, 11 ours) and 15
  at -1. The next start failed: "Can't map item deci:ItemSelectionTool to id -1 (seen at: 4096)".
- Fixed: level.dat restored from level.dat_old (1656 entries, none at -1; the broken file kept
  in dev/run/client/world_backups/broken_leveldat_20261011_0359/). DevAutoTest.quitSafely leaves
  the world, waits until the integrated server stopped, then exits (checked: "leaving the world",
  "Stopping server", "world saved and closed", id table complete). Tests now use their own world
  deciworldgen_devtest; a fresh world test deleted the test world's folder, which was the user's
  world. Checked: the user's level.dat untouched by the next test run.

### Military jeep and tank wrecks break with one punch (reported 10 Oktober 2026, FIXED v0.42.5)
- User: "tank, jeep military can be destroyed with one click hand".
- Cause (read in deobf/src, net/decimation/mod/common/block/props/
  BlockWreckageMilitary1 and 2): the constructors call only
  `super(Material.iron)`, never `setHardness`, so hardness stays 0 and the
  block breaks instantly by hand, also in survival. The car wrecks
  (BlockProp) get hardness 10 from BlockProp. BlockWreckageMilitary3 not
  checked yet [not verified].
- Not the same as the vehicle entity bug fixed in v0.8.1 ("Military
  jeep/tank/helicopter destroyed in one hit" below): these are the WRECK
  props our military bases place. The user's shots were taken in creative,
  where any block breaks with one click; with hardness 0 survival breaks
  them in one click too [inferred from code, not tested in survival].
- Fix idea: set hardness / resistance on those blocks from our side at
  init (Deci accessor, `Block.setHardness`), like a BlockProp (10).
- FIXED v0.42.5: fixes/WreckHardness sets hardness 10 on the three (like a BlockProp) at init;
  dev test npckill: 50 s to break by hand in survival (log "wreck hardness 10: ...").


### Kill and death counters read the dead backend, "Player Kills" should be "Human Kills" (reported 11 Oktober 2026, FIXED v0.42.2)
- User: "players kill change to humans kill instead and the total can be view ingame or in main
  menu at bottom". Their HUD shows Player Kills 0, Zombie Kills 0, Deaths 0 (shots
  docs/shots/milbase_v0.42_review/).
- Code: the in game HUD (deobf/src/decimation/hud/IngameHud.java line 128) and the main menu
  list (GuiMenuBase line 180, GuiMenuHome line 111) read `ObjectPlayerProfile` from
  `BackendProfileCache` (the dead backend), not the local PlayerData. PlayerData does count:
  `DeathHandler` line 57 adds a player kill only when the dead entity is a PLAYER killed by a
  player; zombie kills are counted there too [line not read yet].
- Wanted: "Human Kills" = every human killed (bandits, soldiers, Soviets, juggernauts, elites,
  survivors and players), shown in game and at the bottom of the main menu, from local data.
- Fix idea: count human NPC kills in our own handler (like HumanityKillHandler), store in the
  player's data, rename the label, feed the HUD / menu from local data (Javassist on the label
  and the profile source, or draw our own line).

### Main menu shows "Play offline" and an offline banner for a real account (reported 11 Oktober 2026, FIXED v0.42.2)
- User: "main menu have issue with non crack user where main menu show up offline banner and
  play button say play offline instead play only text with green color background of the
  button". Wanted: the button says "Play" on Decimation's green background, no offline banner.
- Code (deobf/src/decimation/gui/GuiMenuBase.java around line 371): the green "Play" button
  (colour -13395661, tooltip "Play now!") only when `ClientState.bv` is true, otherwise the grey
  "Play offline" (-8355712); the same choice in GuiMenuProfile line 63 and
  GuiMenuSupporterLounge line 78. The banner: GuiMenuHome around lines 66 to 76 (checks the
  backend client's connection). `bv` is most likely set by a successful login to the dead
  Decimation backend, so it never turns on now [inferred, not traced yet].
- Fix idea: a Javassist patch (or Deci accessor at start) that treats a valid Minecraft session
  as "online" for the menu, without touching the dead backend calls (PatchBackend already
  removed the 5 s wait).
- FIXED v0.42.2 (both): fixes/LocalStats counts every kill and death in singleplayer per player
  UUID across worlds (config/deciworldgen_stats.cfg): human kills (players and every human NPC;
  Deci.isHuman, which excludes the infected: InfectedEntity extends Decimation's human entity
  class), infected kills, deaths; the client puts them into BackendProfileCache.profile and the
  totals line (DeciConstants) once a second. Deci.setSessionValid(true) at startup (green "Play",
  no banner). tools/patches/PatchMenuStats.java (ASM, three jars) renames the labels: Human Kills,
  Deaths, Total Human Kills, Total Infected Kills. Checked with dev test `stats` (kills +1 each,
  menu profile = local counts, totals, session flag; HUD and home menu pictures
  docs/shots/milbase_v0.42.2/stats_*.png); the test puts the counters back afterwards.
  Not changed: "Not Supporter :(" in the profile box (backend supporter flag).


### Colourful rail teeth on the M4A4 under a sight (v0.41.0, 10 Oktober 2026)
- Seen in our own shots: a rainbow strip on the receiver top of the M4A4 (and the HK416 / Mk18
  built from it) once a sight is fitted. The M4A4's rail teeth gunModel205..208 (x 2.1 .. 3.5)
  use a placeholder texture island with saturated colours; its tall rear sight always covered
  them, and since v0.40.0 that sight folds away under a sight (IronSights).
- Fix: our variants repaint the four islands with the clean tooth gunModel204's (tools/guns/mk18.py,
  hk416.py); on Decimation's own M4A4 (its art, unchanged) IronSights hides those four teeth while a
  sight is attached (the sight covers that spot). Check: gunview Mk18 / M4A4 / HK416 with the
  EOTech (docs/shots/sights_v0.41/final_eotech_three_guns.png).

### Sights float above flat top rifles (v0.40.1, 10 Oktober 2026)
- User (Honey Badger and Mk18 screenshots, red dot): the sight hangs in the air above the gun.
- Cause: GunItemRenderer.renderAttachments draws every sight at one spot, made for receivers
  whose top in the sight zone is about y -4.45 (Uzi, MP5). Flat top rifles sit lower (M4A4 rail
  teeth -3.6, Honey Badger -3.85, ACR -3.87): 0.6 to 1.6 of air. Their tall folding rear sight
  used to fill it (the v0.36 M4A4 shot has the red dot frame at the same screen spot, resting on
  the rear sight); since v0.40.0 that sight folds away under a sight (IronSights) and the gap
  shows. Not a render bug: the NPC side view matched study.py.
- Fix: tools/guns/sightfit.py measures each gun's rail top in the sight zone (x 2 to 3.5, long
  parts plus rail teeth, iron sights excluded) and writes
  assets/deciworldgen/sight_offsets.txt (dy = rail top + 4.45, only downwards, 20 guns x 5
  sights); fixes/SightPlacement moves those sights down per gun at startup
  (Deci.offsetAttachment) and, while aiming in first person, raises the whole gun by the same
  dy (from IronSights.begin inside the gun draw method), so the sight picture stays where it was.
- Check: gunview reddot on Mk18 / M4A4 / ACR / Uzi: hip on the rail, aim dot on the centre lines,
  Uzi unchanged; 2x on Mk18 / ACR the same. docs/shots/sights_v0.40.1/.
- Open: the muzzle flash is drawn outside the gun draw method, so while aiming with a lowered
  sight it shows dy below the muzzle `[inferred, not checked]`.

### Some NPC machine guns fire at double rate (v0.39.2, 10 Oktober 2026)
- Verdict (user, in game, 10 Oktober 2026): "feels right now, wew. better than before".
- User: "juggernaut who using pkm, the firerate is ridiculous, like using mg guns ... the elite
  mobs who using pkp is most balance ... this happen not in juggernaut but on other npc too".
- Measured (new dev test `firerate`: one NPC per tier and gun on the arena, its own AI only,
  every shot timed): inside a burst the PKM, M240, MG3 and M4A4 fired every tick (1200 rpm),
  the PKP, AK74, AKM every 2nd tick (600). Juggernaut PKM: 1 tick gaps, 46 shots in 15 s.
- Cause: Decimation runs a gun at 1.3 x its listed rpm (GunStats: seconds per shot =
  1 / (rpm x 1.3 / 60)); our burst code (NpcShots) rounded that to whole ticks: PKP 600 ->
  780 -> 1.54 ticks -> 2; PKM 650 -> 845 -> 1.42 ticks -> 1. A 50 rpm gap on paper became
  double the rate.
- Fix: NPC bursts never faster than `maxBurstRpm` (config deciworldgen_npc.cfg, npc_fire,
  default 600 = the elite PKP), slower guns at their own rate with the tick fraction carried
  from round to round instead of rounded away (NpcLoadouts.burstTicksPerShot, NpcShots).
- Check: firerate after the fix: every case 2 tick gaps; juggernaut PKM 232 rpm overall,
  elite PKP 340, elite PKM 268, elite MG3 324, soldier M240 304, bandit heavy PKM 244, AKM
  124, Uzi 84 (15 s each, bursts plus pauses); `shots` all PASS. Players keep Decimation's
  real rates (a player's gun times shots in milliseconds, not ticks).

### 1 fps in the dev world: entity explosion through a corrupt chunk (v0.38.1, 10 Oktober 2026)
User: "really big performance issue now ... 1 fps after i drop it to the ground". Not the gun
(gunperf: our guns cost what Decimation's cost). The dev world's region slot (-1,-6) held chunk
(-4,-1)'s data, so entities were saved twice and doubled on every load (9 800 -> 40 730 in one
session, 31 010 Hulks), and Forge printed a stack trace for each every tick. Fixed in the save
(tools/perfcheck.py fixchunk, census kill=); origin of the broken slot unknown `[not verified]`
(it already crashed the server on 10-09 04:12). docs/performance.md section 2.

### Infected path search every tick (Decimation, v0.38.1, 10 Oktober 2026)
InfectedEntity.onLivingUpdate (deci.ag.d) ran updateWanderPath every tick on server and client
plus a 40 x 40 horde scan every tick: about 60 % of both threads in pathfinding. Patch
tools/patches/PatchInfectedAI.java (three jars): wander server side once a second, horde scan
every 10 ticks. Same 53 infected: fps 21 -> 35, client pathfinding 60 % -> 0 %. zombies test
passes. docs/performance.md section 2.

### Upper storeys unreachable (ladder popped off, stair core collapsed)
- **Found**: 7 Oktober 2026 by the building audit (`tools/floorplan.py`
  reachability): shop b-4_1_0 storey 1 0% reachable, office b-3_2_2 top
  storey 0% reachable (seed 1, Decimation world type).
- **Root causes (ours)**: (1) buildings without a stair core use a ladder
  at (W-2, L-2) hanging on the back wall. The wall cell could be a window
  or a decay hole, and the wall column often belongs to the NEXT population
  window, so it does not exist yet when the ladder is written; any block
  update meanwhile (a metal shelf next to the ladder being completed with
  flag 3 since 0.16.0) makes the unsupported ladder pop off. (2) The
  collapsed corner cone also removed ladder and stair core cells on the
  top storeys.
- **Fix (v0.16.1)**: the wall cell behind the ladder is always solid wall;
  no furniture on the 4 cells around the ladder; ladder shaft and stair
  core (plus a 1 block ring) are exempt from the collapse. Rescan of 105
  buildings: no storey 0% reachable. Remaining low reachability comes from
  full rubble cubes in doorways (step 4d rubble redesign; jumpable in game).

### Supply drop crate vanished on a street prop
- **Found**: 7 Oktober 2026 by the dev autotest after street furniture
  (0.15.0): drop column 14/62 was a sidewalk with a trash bag on it.
- **Root cause**: same mechanism as the flower case below. Decimation props
  have collision boxes, the falling crate stops on one inside the prop's
  cell, cannot become a block there and dies.
- **Fix (v0.16.0)**: `SupplyDropScheduler.drop` skips a candidate column
  whose top block has a tile entity (any prop, chest, car) and tries the
  next of its 12 random positions. Autotest passes again.

### Supply drop crate vanished when it landed in a flower
- **Found**: 7 Oktober 2026 by the dev autotest (drop column 70/287: crate
  entity fell, no crate block anywhere in the column).
- **Root cause (vanilla behaviour, our column choice)**: the crate is an
  `EntityFallingBlock` (`EntityFallingSupplyDrop`); on landing it turns into
  a block only if the cell is replaceable. Flowers, saplings and the tall
  flowers are not (`BlockDoublePlant.isReplaceable` is true only for tall
  grass and fern), so the entity died and the drop was lost. The column had
  a 2 block tall flower.
- **Fix (v0.13.0)**: `SupplyDropScheduler.clearLanding` removes
  non-replaceable plants in the drop column down to the first solid block or
  liquid before deploying. Autotest passes again (crate block at y=71). The
  flower case itself was not reproduced (drop columns are random).

### Graded yard sand fell into caves (hole next to a building)
- **Found**: 7 Oktober 2026 by `tools/gradescan.py` on seed 7 while building
  terrain blending (a ring column 49 blocks below the floor).
- **Root cause (ours)**: the grader filled raised yard columns with the
  column's own surface block. Sand and gravel are `BlockFalling`;
  `setBlock` calls `onBlockAdded`, which schedules the fall even during
  generation, so a sand fill over a cave dropped into it.
- **Fix (v0.13.0)**: falling blocks are never used as fill (sand fills with
  sandstone, gravel with stone), and a sand or gravel top goes only on a
  solid block. Rescan of 3 seeds: ground at the walls within 1 block of the
  floor everywhere.

### Generated metal shelves invisible (multiblock master never set)
- **Reported**: 7 Oktober 2026 by the user; the culling patch below did not
  fix it (still invisible right next to the prop).
- **Root cause (ours)**: multiblock props (tile entities extending
  `deci.W.a` MultiblockPart, e.g. `deci:BlockMetalShelf`, MetalShelf_Empty)
  are drawn only by their master part: `MetalShelfRenderer` returns unless
  `isMaster()` (stored master position == own position). The master is set
  by the block's placement code; world generation places the block directly,
  so the master stayed 0,0,0 and the shelf never rendered. Found by reading
  the tile entity NBT (`multibl.mx/my/mz` = 0) in a generated world.
- **Fix (v0.12.3)**: `Slices` and the small-schematic placer call
  `setSelfMaster()` on every multiblock part they place;
  `MultiblockRepairHandler` makes orphaned parts (master never set) their own
  master on chunk load, which also repairs worlds generated before 0.12.3
  and any legacy Decimation prop broken the same way. Verified: new world
  407/407 shelves are masters; a copy of the user's pre-fix world: every
  shelf in the loaded area repaired, the rest repair when visited.

### Props randomly not rendered (legacy Decimation bug)
- **Reported**: 7 Oktober 2026 by the user (screenshot: selection box of a
  placed prop, no model). Happens with player-placed props too, mostly
  props whose model is bigger than one block (tables etc).
- **Root cause**: `PropRenderer` (`deci.I.l`) first calls
  `LineOfSight.canSeeTileEntity` (`deci.a.c$a.a(EntityPlayer,TileEntity)`):
  8 rays from the eye to the 8 corners of `TileEntityProp.getRenderBoundingBox()`,
  drawn only if one ray is clear; Forge's frustum culling uses the same box.
  That box is the bare 1x1x1 cell for 36 of 72 props (no render size
  declared) although their models are drawn up to ~1.5 blocks wide, and it
  never rotates with the prop (the model turns by metadata % 4 * 90). So the
  corners land off screen or behind floors / walls while the model is in
  view, and the prop is skipped.
- **Fix (Javassist, `tools/patches/PatchPropCulling.java`)**:
  `getRenderBoundingBox` returns a box centred on the block, rotation proof,
  at least 2x2, covering the declared render size, half a block taller;
  `canSeeTileEntity` returns visible within 4 blocks and tries a ray to the
  box centre first. Applied to `Decimation.jar.patched`,
  `dist/Decimation.jar`, `dev/libs/Decimation-base.jar` (Prism needs the new
  `dist/Decimation.jar`). Verified: classes load, no render errors in the
  autotest, a table's box is now 99.5..101.5 instead of 100..101. Visual
  confirmation pending. NOTE: the user's invisible shelf was actually the
  multiblock bug above; this culling fix still stands for normal props.

### City buildings missing a whole wall at sector borders
- **Reported**: 7 Oktober 2026 by the user (0.11.0 in game: "one side has no
  wall, looks cut off like old village bugs").
- **Reproduced**: `tools/wallscan.py` on seed 1 + pregen: 2 of 33 buildings
  had a side 97% air. One was only the edge of the generated area (owner
  chunk never populated). The other (`b4_4_2`, west wall x=263) was real.
- **Root cause**: a chunk's population window is 16x16 at +8/+8, so it
  reaches 8 blocks into the neighbouring chunks and can cross a SECTOR
  border. `StructureGenerator.generateImpl` only ran the city writer when the
  populating chunk itself was in a CITY sector; a building in a city sector
  whose column fell in a window owned by a non-city chunk never got written.
  The large-schematic writer had the mirror bug (not run for city chunks).
- **Fix (v0.11.1)**: both slice writers run for every chunk (each checks the
  sector of the structure, not of the populating chunk); only small ruins
  and streets use the populating chunk's sector. Rescan: 37 buildings, 0 real
  missing walls, 4 gaps all at unpopulated edge chunks.
- **Note for players**: buildings straddling chunks generated by an older
  version (or never populated yet) still look cut off until those chunks
  populate; use a new world for a clean test.

### No supply drops in singleplayer
- **Found**: 7 Oktober 2026 (naming pass).
- **Root cause**: the 15 minute countdown lives in `ServerTickHandler`
  (`deci.aK.q`, ServerProxy only) and every `SupplyDropSpawner` (`deci.aM.c`)
  method checks `isServer()`. Drop points came from an admin file
  `decimation_supplydrop.properties`, which singleplayer never has.
- **Fix (v0.9.0, dist only)**: `SupplyDropScheduler` runs the same countdown
  (`ServerConfig.supplyDropCountdown/Interval`, 18000 ticks), the same 10 and
  5 minute chat warnings, then places the mod's own falling supply drop block
  at a random LOADED column 64 to 144 blocks from a random player and
  announces the coordinates. Honours `enableSupplyDrops`. The crate drifts
  down for roughly 45 s. Verified by the autotest (dropped, landed at y=69).

### Humanity never changed from ordinary kills in singleplayer
- **Found**: 7 Oktober 2026 (naming pass).
- **Root cause**: `DeathHandler` (`deci.an.f`) runs in singleplayer, but its
  "+1 per non player kill / -10 for killing an innocent player / +1 for any
  other player" block is behind `isServer()`. Only the victim's own
  `getHumanityOnKill()` value applied.
- **Fix (v0.9.0, dist only)**: `HumanityKillHandler` replicates that block
  without the clan branch (clans are dedicated only). Killing one infected now
  gives +2 (its own +1 plus the rule's +1), the same as on the official
  server. Verified by the autotest (50 -> 52).

### Armor damage-reduction buff doesn't affect NPC gunfire
- **Found**: 27 Juli 2026, while trying to explain how to test the armor buff
- **Symptom**: `ItemArmorDeci.damageMultiplier` (recently buffed 35%, see
  `new_feature.md`) has zero effect when bandits/soldiers shoot the player.
- **Root cause**: the multiplier is only ever consumed in one place in the
  whole mod - `deci.aE.a$z$a`, the network handler for a **player's own**
  gunshot hit report (client tells server "I hit X for Y damage", server
  applies the multiplier there). NPC ranged attacks
  (`deci.ag.a.e(EntityLivingBase)`) call vanilla `attackEntityFrom` directly,
  server-side, with a different `DamageSource` type (`"human"` vs the
  player-shot `"gunDeci"`) - completely bypassing the multiplier.
- **Proposed fix**: add a `LivingHurtEvent` listener (fires for all incoming
  damage regardless of source), filtered to the known gunshot-type
  `DamageSource`s (both `"gunDeci"` and `"human"`, possibly more NPC variants
  to confirm), apply the multiplier there instead - and remove the
  now-redundant multiplication inside `deci.aE.a$z$a` so player-vs-mob shots
  don't get it applied twice.
- **Status 7 Oktober 2026: FIXED** (user said go). Implemented slightly
  differently from the proposal: the player hit handler `deci.aE.a$z$a` is
  left untouched (it only applies the multiplier when the target is a player,
  i.e. PvP), and `ArmorGunfireHandler` (LivingHurtEvent) applies the same
  rule to the NPC sources `"human"` (bandits, armed traders) and `"turret"`.
  Same pieces, same helmet exclusion, no double application, no bytecode
  patch. Every Decimation damage source bypasses vanilla armor, so before
  this armor did literally nothing against NPC bullets. Verified by the
  singleplayer autotest: 10 human damage, bare 10.00 taken, with a
  spetsnazVest (x0.553) 5.52 taken.
- **Helmets (v0.9.1, user decision: headshots only)**: helmets carry a much
  stronger multiplier (0.3 raw, 0.195 after the 35% buff) than other pieces
  (0.8 to 0.85), which is probably why the original skipped them; on every
  hit a full set would block about 97%. Now: player guns (`gunDeci`) add the
  helmet only when the shooter's aim line crosses the victim's head band (top
  25% of the body); NPC/turret hits use it on a random 20% (no hit position,
  and NPCs always aim at eye level). Verified: 81/400 NPC hits used it; a
  player gun aimed at the head took 4.88 (helmet x0.487), at the chest 10.00.

### Background thread pinned one CPU core all session (SmoothSwingThread)
- **Found**: 7 Oktober 2026 by the naming pass, verified in code.
- **Root cause**: `deci.b.h.run()` (thread "SmoothSwingThread", started by
  ClientProxy) busy-waits with no sleep to advance the weapon sway clock
  `deci.b.i.bz` at 60 Hz.
- **Fix**: Javassist patch (`tools/patches/PatchSwing.java`), same 60 Hz
  accounting plus a 4 ms sleep, applied to `Decimation.jar.patched`,
  `dist/Decimation.jar`, `dev/libs/Decimation-base.jar` and Prism. Measured
  on Prism, idle main menu: 200% CPU -> 105%. Thread dump shows it sleeping.
  Weapon sway not yet looked at in game. May help the FPS reports below
  (less heat / throttling), not a proven fix for them.

### Prism instance crashed on every launch (CustomSkinLoader)
- **Found**: 7 Oktober 2026, while testing the separate deciworldgen jar.
- **Symptom**: crash during startup, before any mod loads, with
  `NoClassDefFoundError: net/minecraftforge/fml/relauncher/IFMLLoadingPlugin`.
- **Root cause**: `CustomSkinLoader_Universal-15.0.1.jar` (added to `mods/` on
  24 Agustus) ships a coremod built for Forge 1.8+ package names. Not related
  to our changes; the last successful launch log before it was 15 Agustus.
- **Fix**: renamed to `CustomSkinLoader_Universal-15.0.1.jar.disabled`
  (rename back to restore). A 1.7.10 build of CustomSkinLoader would be
  needed to keep custom skins.

### Generated structures built on the ocean floor
- **Found**: 7 Oktober 2026 (dedicated-server test, region file inspection).
- **Root cause**: `World.getTopSolidOrLiquidBlock` skips water in 1.7.10, so
  our "is this site water" check inspected the seabed instead of the water.
- **Fix**: v0.7.0 checks the block above each sampled ground point. Verified:
  same seed, all 4 underwater sites now rejected. Older worlds keep theirs.

### Zones never active in singleplayer
- **Found**: 7 Oktober 2026.
- **Root cause**: zone list loaded only by ServerProxy, zone handlers
  `deci.aK.d/g/i/m` are `@SideOnly(Side.SERVER)`. Same pattern as the loot bugs.
- **Fix (partial)**: v0.7.0 loads per-world generated zones and replicates the
  zone spawn handler (`deci.aK.d`) in singleplayer, verified by the
  unattended singleplayer test. Player effects of safezone/radiation/scary
  zones (`deci.aK.g/i/m`) are still not replicated.

### Loot never worked in singleplayer (crash on right-click)
- **Found**: 25 Juli 2026
- **Symptom**: crate/car/ammobox/supply drop right-click did nothing (or later,
  crashed) in singleplayer only. Worked fine in old multiplayer.
- **Root cause**: `ClientProxy` (`deci.a.c`) never registered the loot
  interact handler (`deci.aK.k`) - only `ServerProxy` (`deci.a.e`, dedicated
  server only) did. Singleplayer's integrated server reports physical side
  `CLIENT`, so ClientProxy is what loads even though it's hosting the "server."
  Handler also carried `@SideOnly(Side.SERVER)`, which gets ASM-stripped when
  loaded under `CLIENT` regardless.
- **Fix**: added `deci.aK.k` registration into `deci.a.c.preInit()`, stripped
  the `@SideOnly` annotation from the handler method.
- **Fixed**: 25 Juli 2026, 13.12

### `ClassCastException: deci.a.c cannot be cast to deci.a.e`
- **Found**: 25 Juli 2026, 13.09 (crash report), right after the above fix
- **Symptom**: crash on interacting with any lootable block, immediately after
  the previous fix went in.
- **Root cause**: `deci.aK.k`'s handler also calls `deci.a.b.d()` (which
  hard-casts the proxy to ServerProxy) to reach the clan-turf manager for an
  unrelated permission check. Fine on dedicated server, crashes on
  ClientProxy.
- **Fix**: `deci.a.b.d()` made null-safe (`instanceof` check, returns `null`
  instead of throwing). `deci.aK.k`'s two call sites that use the result made
  null-safe too (turf check treated as "no restriction" when null - correct,
  since clans/turf don't exist in singleplayer anyway).
- **Fixed**: 25 Juli 2026, ~13.15

### Loot GUI never opens (sound plays, no window)
- **Found**: 25 Juli 2026, 13.20
- **Symptom**: right-click plays the loot sound and marks the position looted,
  but no GUI window appears. Second click reports "this is empty."
- **Root cause**: the GUI-open call is scheduled on a delayed-task queue
  (`net.decimation.mod.common.utils.b`), drained only by
  `deci.aK.q` (`TickEvent.ServerTickEvent`) - which is *also*
  ServerProxy-only, so the queue is never processed in singleplayer.
- **Fix**: added a new `TickEvent.ClientTickEvent` listener directly on
  `deci.a.c` (already registered to the event bus) that drains the same
  static queue. Deliberately did *not* register the full `deci.aK.q` class -
  it pulls in unrelated dedicated-server-only logic (network reconnect,
  anti-dupe scan, clan supply-crate announcements) not wanted in singleplayer.
- **Fixed**: 25 Juli 2026, 13.20

### Crash opening ammo crate (large)
- **Found**: 25 Juli 2026, 13.40 (crash report)
- **Symptom**: `NullPointerException` in `RenderItem` while rendering the loot
  GUI, specifically for the large ammo crate.
- **Root cause**: `deci.aD.k.avk` (an item field) is declared but never
  assigned anywhere in the mod - dead/leftover reference, always `null`. It's
  the first entry in the large ammo crate's loot pool
  (`deci.aD.l$4`, 25-entry list). Pre-existing bug in the original abandoned
  mod, unrelated to any of our patches - only surfaced once loot started
  working at all.
- **Fix**: substituted the dead field read for its valid neighbor
  (`deci.aD.k.avl`) in `deci.aD.l$4`'s constructor.
- **Fixed**: 25 Juli 2026, ~13.55

---

## Pending decision

(none)

---

## Not investigated yet
(Some entries below were fixed later and say so in their heading; the open
ones on 9 Oktober 2026: base height order, prop dense FPS, LED lamp floor (fixed)
at the end of this file.)

### Building base height depends on chunk generation order (ours)
- **Found**: 8 Oktober 2026 (fresh seed 1 worlds, `tools/worlddiff.py`):
  apartments b-1_3_0 and b0_3_0 sat at y 64 in one run and y 63 in
  another, with the same seed.
- **Cause**: `Slices.decideBase` samples the footprint where
  `world.blockExists` is true when the building's FIRST slice is written,
  so the result depends on which neighbour chunks happen to exist then.
  The chosen height is stored (StructureData), so a building is always
  whole; only the height can vary between two new worlds of one seed,
  near the edge of what was generated. Spawn area: identical.
- **Impact**: low (no broken buildings); matters for "same seed, same
  city" expectations and for refactor checks (use `--within`). A fix
  would sample terrain height from the chunk generator (noise) instead of
  the world `[not verified]` how costly that is.

### FPS drop while aiming through scopes (red dot/2x/4x/6x) (FIXED v0.28.0..0.28.4)
- **Reported**: 26 Juli 2026; user 8 Oktober 2026: about 150 -> 110 fps
  with a scope, asked for the old cheap method (view zoom) instead of the
  two camera method, keeping the old code switchable.
- **Root cause (code, deobf ClientRenderHandler.renderScopeView)**: the
  world is rendered a second time (narrow FOV, up to 1024 x 1024) into a
  texture every frame a scoped gun is HELD, aiming or not; BModel
  renderScopeGlass draws it on the glass.
- **FIXED v0.28.0**: `tools/patches/PatchScope.java` gates renderScopeView
  behind system property `decimation.scope.pip` (method otherwise
  untouched: the old scope returns with `pictureInPicture=true` in
  `config/deciworldgen_scope.cfg`). `net.decimation.fixes.ScopeZoom`
  zooms the whole picture while aiming (reddot 1.25, 2x, 4x, 8x, dragunov
  4x, configurable) and copies the centre of each finished frame into the
  scope texture (RenderWorldLastEvent, before the hand) so the glass looks
  see-through.
  - v0.28.0 zoomed only the world (FOV; the hand renders at a fixed FOV
    70, so the gun stayed 1x). User then chose the "NORMAL" look of a
    NORMAL / PIP comparison picture: world AND gun zoom together.
  - v0.28.1: EntityRenderer.cameraZoom = magnification (eased, log
    space). Vanilla renderWorld skips renderHand while cameraZoom != 1,
    so ScopeZoom calls renderHand itself on RenderHandEvent (fired right
    before that check); renderHand applies the same cameraZoom scale, so
    the gun zooms with the world. Glass copy sized glassView x zoom. The
    eyepiece sits a little above the screen centre at 4x, but the glass
    shows the screen centre (the aim point), so the reticle aims true.
  - v0.28.2 (user: glass misaligned fullscreen / windowed; gun swinging
    wildly above 1x in a first centring try): the WHOLE world frame is
    copied and the patched glass maps it by screen position (projective
    texturing), so it is see-through at any window size and zoom. The
    glass reports its screen box (GL feedback, every 4th frame); the gun
    is moved (cameraYaw / cameraPitch, hand only) so the sight sits on the
    screen centre where shots go. The sight position is learned per gun +
    scope + window aspect, only from unclipped samples and after 3 samples
    in a row agree (the first try corrected every frame from old samples
    and chased the weapon sway; a sample taken while the gun swung in from
    the hip pushed the glass off screen for good); the first aim with a
    new combination holds 1.5x up to 0.8 s while it learns. Mouse turning
    slows to 1 / zoom while zoomed (config "sensitivity"). Checked: reddot,
    2x, 4x, 8x, integrated (aug1) in 854x480 and 900x895, reticle on the
    centre in all 10 (docs/shots/scope_v0.28.2); a +-4 degree sweep moves
    the sight 6..25 px (normal weapon lag). Java 8 trap: Javassist on a
    newer JDK compiled FloatBuffer.flip() with the Java 9 return type
    (NoSuchMethodError in game): call it through java.nio.Buffer.
  - v0.28.3 (user asked for a faster, more efficient way): the copy now
    covers only the glass's last measured box plus a margin (whole frame
    while the zoom changes), into a texture allocated once per window size
    (glCopyTexSubImage2D); GL buffers allocated once; the glass is measured
    every 8th frame (every 2nd while a new sight is learned: 10 settled
    samples averaged, zoom held at 1.5x up to 1.5 s, which also fixed the
    integrated aug scope learning a still moving pose). Tried and dropped:
    a depth only glass drawn before the body (no copy at all): attachment
    glasses are drawn AFTER the gun body, so its front sight showed in the
    glass, and the integrated scope stayed grey. Checked all 10 (5 sights x
    2 windows) centred and see-through, docs/shots/scope_v0.28.3.
  - v0.43.1: measured only during the player's own hand draw (NPC guns' scope glass was taken
    for the player's: "Gun creeps down while aiming" above).
  - v0.28.4 (user: "for 8x better use the fake it method with a black
    layout", then "include 4x"): scopes from `overlayFrom` (config,
    default 4) hide the gun once the zoom is 70% in and draw a classic
    sniper overlay on the HUD (RenderGameOverlayEvent HELMET): black
    outside a round view (radius 0.47 x the smaller screen side), a
    darkening edge of 12 thin rings, the scope's own reticle texture
    (textures/model/guns/scopes/<name>.png, black lines on transparent)
    full size, vanilla crosshair hidden. No gun, no glass copy, no sight
    learning for those scopes. GL traps met: the circle strips wind
    backwards in GUI coordinates (cull face off), the HUD shades flat
    (per vertex colour fades became spikes: solid rings instead). A
    "white haze" at the edge was the sky fading to black, checked by
    pixel values (190,213,250 -> 149 -> 97 -> 56 -> 0). Checked 10 shots
    (docs/shots/scope_v0.28.4): reddot / 2x gun with see-through glass,
    4x / 8x / integrated overlay, reticle on the centre.
- **Measured** (`./gradlew runClient -Pautotest -Pscope`, dev client,
  unlimited fps, 4x on an ak74): picture in picture empty hand 85..103,
  held 45..51, aiming 37..44; view zoom empty hand 70..92, held 64..76
  (the gun model only), aiming 105..132. Shots docs/shots/scope_v0.28.0.
  v0.28.1 on a busy machine: picture in picture empty 26, held 10,
  aiming 10; camera zoom empty 28, held 32, aiming 42.
- First try also gated renderScopeGlass: the glass vanished and the scope
  showed its solid black body (looked like a bigger gun); reverted.

### FPS drop in prop-dense areas (cars/crates/shelves etc.)
- **Reported**: 26 Juli 2026
- E.g. ~100fps baseline down to 20-30fps around 15+ props, worse with 50+.
- **Measured 9 Oktober 2026** (`./gradlew runClient -Pautotest -Pprops`,
  last autotest world: stone platform high up, 15 x 15 mixed props;
  `-Pjfr` adds a Java Flight Recorder CPU profile, run/client/profile.jfr,
  read with JDK 25 `jfr print --json`; the client thread is `main` in dev).
  Before: empty 28..29 fps, 225 props in view 12..14, hidden in a stone box
  23 (box alone 24..27). Profile: 76% of PropRenderer (`deci.I.l`) time in
  ClientProxy.LineOfSight.canSeeTileEntity (`deci.a.c$a.a`): up to 9 ray
  casts per prop per frame, a chunk lookup (LongHashMap) per block per ray;
  hidden props pay all 9 and are not drawn anyway. canSeeEntity does the
  same for every living entity and vehicle.
- **IMPROVED (v0.28.6, PatchPropCulling step 3)**: both answers cached until
  the player (or the entity) moves 0.3 blocks, at most 1.0..1.3 s for props
  and 0.15..0.18 s for entities, staggered per object. (A first try with a
  plain 80..111 ms lifetime rechecked nearly every frame at low fps; a null
  player on the main menu preview crashed the cache until guarded.) After:
  line of sight 25 of 251 prop renderer samples (was 804 of 1053); prop
  renderer share of the client thread 21% -> 9%. All autotest checks pass;
  props and NPCs still render (docs/shots/props_fps).
- **Left**: what remains is drawing the models (100% in glCallList, driver
  / GPU work): 225 visible props still about halve the fps on the test
  platform.
- **City street, after the cache** (`-Pcityfps`: x 8.5, y 68, z 120.5
  looking north, seed 1 autotest world, + `-Pjfr`): props 5.5% of the
  client thread, chunk drawing 15%, chunk rebuild 12.7%, entities and tile
  entities 11.8%. Props are no longer the bottleneck there.
- **Render distance by size** (PatchPropCulling step 4, config
  `deciworldgen_props.cfg`: small < 0.8 block, medium < 1.6, large): 32 / 48
  / 64 measured 35..40 fps vs 41 at 64 / 64 / 64 in that street (noise, no
  gain) and can pop in, so the default is 64 for all (vanilla); lower it
  for open prop heavy places.
- **Baking static props into chunk meshes** (the full fix for open views
  with hundreds of visible props): not started; worth it only if the user
  finds places that still drop (atlas for every prop texture, both model
  formats to quads).
- Side finding: Decimation's postInit blocked about 5 s at launch connecting
  to its dead server (kryonet `deci.aP.a.e`), on the main thread; startup
  only, not in game. FIXED 9 Oktober 2026 (`tools/patches/PatchBackend.java`):
  connect timeout 300 ms, and kryonet's own Client.connect (bundled in
  Decimation.jar) now passes it to the TCP connect, which it hardcoded to
  5000 ms (shortening only the first timeout still waited 5 s: measured).
  Launch: "Registering new client network" -> "Unable to connect" went from
  5 s to under 1 s; later reconnect attempts still fail quietly in their
  own threads as before.

### Military jeep/tank/helicopter destroyed in one hit (survival mode) (FIXED v0.8.1)
- **Reported**: 26 Juli 2026, clarified 27 Juli 2026
- **Symptom** (clarified): a single click/hit destroys the vehicle outright,
  like breaking a normal block - it should instead be tough/unbreakable this
  way, same as other static props (e.g. crate/prop blocks return hardness `-1`
  or similar unbreakable-by-hand values). Not about health-bar/HP tuning -
  about whatever block/entity property currently lets one click destroy it at
  all.
- **Root cause (7 Oktober 2026)**: vehicle body `deci.ad.e.attackEntityFrom`
  (subclasses `deci.ad.f/g/h/i/j`, `i` = hummer) treats any player hit on an
  EMPTY vehicle as "pick up": `setDead()` + drop its item, survival included.
  Only gun damage (`gundeci`) takes the health path. Seat/hitbox parts
  `deci.ad.b` / `deci.ad.a` forward every hit to the body. It is the
  original design (minecart style pickup), not a singleplayer bug.
- **FIXED in v0.8.1 (dist only, Prism still 0.7.0)**: `VehicleHitHandler`
  cancels Forge's `AttackEntityEvent` for survival players on vehicles and
  their parts unless sneaking. Sneak + punch still recovers an empty vehicle
  as an item (compromise so survival can still pick vehicles up; one line to
  remove). Creative unchanged; gunfire/explosions still use vehicle health.
  Not covered: projectiles (arrows) fired by a player still trigger pickup.
  Verified by the unattended singleplayer test: survival punch leaves a
  hummer alive, sneak punch picks it up.
- **Arrows FIXED in v0.48.1 (11 Oktober 2026)**: the body's pickup test is
  `getEntity() instanceof EntityPlayer`, and an arrow's source entity is the
  shooter (indirect source), so arrows, thrown items and the player's own TNT
  picked an empty vehicle up (TNT `[inferred]` from vanilla Explosion: its source is
  indirect with the placer as the entity; not tested). No Forge event covers damage to a non living
  entity, so `tools/patches/PatchVehicleArrows.java` (Javassist, three jars,
  only vanilla names) swaps an indirect player source for
  `DamageSource.generic` at the start of `deci.ad.e.func_70097_a`: those hits
  now take the health path. Melee, sneak pickup, creative punch and gun
  bullets unchanged. Dev test `checks` "vehicle arrow" (arrow and snowball
  damage with the player as shooter): FAIL before the patch (picked up), PASS
  after. A real bow shot in game not tried `[not verified]` (the test calls
  attackEntityFrom with the same source EntityArrow builds).

### Bottlecaps only work as a plain item in singleplayer, not as currency
- **Reported**: 27 Juli 2026
- **Status 7 Oktober 2026: FIXED.** `BottlecapHandler` in deciworldgen
  replicates `deci.aK.e`. Verified by the unattended singleplayer test: 5
  bottlecaps dropped on the player, balance 0 -> 5, no caps left as items.
  The HUD display of the balance has not been looked at by the user yet.
- **Symptom**: picking up bottlecaps keeps them as a literal `ItemStack` in
  the inventory, instead of auto-converting into the currency
  counter/balance the way it did on the old official multiplayer server.
- **Likely root cause** (not yet confirmed, pattern strongly suggests it):
  earlier investigation of `deci.aL.a`/`deci.aK.*` found an
  `EntityItemPickupEvent` handler that detects bottlecap items specifically
  (`k.alK`/`k.alL`), deducts the picked-up stack, and credits a player-cap
  currency field instead (shows a "+N bottlecaps" message). That handler is
  very likely one of the same ServerProxy-only `deci.aK.*` classes as every
  other singleplayer bug found this session - never registered on
  `ClientProxy`, so it just never fires in singleplayer, leaving caps as
  plain items. Needs confirming the exact class and applying the same
  registration fix already used for the loot handler (`deci.aK.k`) and the
  tick-queue drain.

## City edge ramp missed its outer columns (fixed v0.24.4, 8 Oktober 2026)
`LcCity.populate` only planned the cells under the population window, so a
window lying wholly outside the city never wrote the outer 1..2 columns of
the 10 wide edge band (a small step at the band's end). Also small and
large sites next to a city could sit inside the band and get cut under.
Fix: populate scans cells within EDGE of the window; sites within EDGE of a
city are dropped. Checked with tools/edgescan.py (seed 1).

## Converted buildings: LED lamp blocks as floor (reported 8 Oktober 2026, FIXED 9 Oktober 2026)
User saw some converted (DeceasedCraft) buildings with LED lamp blocks
used as floor.
- **Found with** `tools/lcaudit.py` (counts every source block of every
  converted building by placement: floor / ceiling / wall / free).
- **Cause**: not the simplylight lamps. tools/lctranslate.py's lamp rule
  matched the word "light" inside COLOUR names:
  `embellishcraft:light_gray_corrugated_metal_plate` (5429 uses, 1350 of
  them as floor), light_gray / light_blue sheet metal, tiles, seats and
  cushions all became deci:BlockLightOff. `minecraft:daylight_detector`
  (935, mostly floor trim) and `lightning_rod` matched too. Full
  simplylight illuminant blocks (about 5000) also became the small lamp
  prop, sitting in walls and ceilings as a floating fixture.
- **Fix** (lctranslate.py, pack rebuilt): `unlit()` removes light_gray /
  light_grey / light_blue / lightning / daylight before the lamp rules;
  full illuminant blocks -> minecraft:redstone_lamp (a solid lamp block);
  daylight_detector, redstone_lamp kept as themselves; rods -> iron_bars.
- **Verified**: worlddiff of seed 1 old pack vs new pack (`--top`):
  2115 BlockLightOff -> BlockMetal_2, 542 -> redstone_lamp, 111 ->
  daylight_detector. 30 interior photos (shots_index "lc_quality") show
  no lamp floors; ceilings show redstone lamps where the source had them.
  Not yet seen by the user in game `[not verified]`.

## NPC tracers fly sideways or backwards (old bug, also in the original; FIXED 9 Oktober 2026)
- **Reported**: 9 Oktober 2026: an NPC shooting forward draws its tracer to
  the left, right or behind.
- **Cause (code)**: BanditEntity.shootAt (`deci.ag.a.e`, also soldiers and
  hazmat soldiers) and ArmedTraderEntity.shootAt (`deci.ai.v.e`) damage the
  target directly on the server and send PacketGunFireEffects
  (`deci.aE.a$B`) with only the shooter's id; the client handler
  (`deci.aE.a$B$a`) draws the tracer along the shooter's getLook(), which
  for a mob is its BODY facing, not its aim.
- **Fix** (`tools/patches/PatchTracer.java`, in Decimation.jar.patched,
  dist/Decimation.jar, dev/libs/Decimation-base.jar): the packet also carries
  the target's id (read only when present; -1 = none), shootAt records its
  target, and the handler aims the tracer from the shooter's eyes at the
  target's chest (bounding box bottom + 0.6 x height). Players unchanged.
- **Measured** (`./gradlew runClient -Pautotest -Ptracer`: a bandit 8
  blocks from the player, held facing away, shoots the player): original
  classes 52 tracers, mean 133 degrees off the line to the target, max 179;
  patched 42 tracers, mean 1.0, max 2.0. (The test also showed the original
  shootAt crashes on a miss when the bandit has no AI target: it plays the
  miss sound at getAITarget(); never happens in play, the AI sets it.)

## Full military armor makes NPC gunfire almost harmless (reported 9 Oktober 2026, FIXED v0.30.2)
User: a full set of normal military armor takes about 1 hp per NPC shot,
even with a group of NPCs shooting at once.
- **NPC shots are not bullets** (BanditEntity.shootAt, read in deobf
  source): when the NPC can see its target, 75% of shots call
  `attackEntityFrom("human", gun damage / 8)` on the target directly, 25%
  only play a sound. The tracer (PacketGunFireEffects) is a visual only.
  Player shots are client ray traces sent as PacketGunHit; the server
  applies body armor and hits with "gunDeci".
- **Cause 1, armor multiplies piece by piece**: ArmorGunfireHandler
  (v0.9.0) uses Decimation's PvP formula, damage x every worn body piece's
  multiplier, and the multipliers are the originals x 0.65 (our armor
  buff). Marine set: vest 0.85, pants 0.8, boots 0.8 -> 0.5525 x 0.52 x
  0.52 = x 0.149 (checks mode: chest alone 5.52 of 10). An M4A4 NPC hit
  (16 / 8 = 2) does 0.30 hp; the helmet (0.195) on the 20% headshots
  makes that 0.06.
- **Cause 2, vanilla hit cooldown**: EntityLivingBase.attackEntityFrom
  (lines 854..870 of the MCP source) ignores a hit while hurtResistantTime
  is above half of maxHurtResistantTime (20 ticks for players) unless it
  is bigger than the last one. NPC shots never reset it (Decimation's
  player gun handler sets it to half after every hit, so player shots all
  count). So for 0.5 s after a hit every other NPC hit of the same size is
  dropped: ten NPCs do about as much as one, at most ~2 hits per second.
- **Fix (v0.30.2, user decision)**: player shots unchanged; NPC gun hits
  ("human") on a player x `npcDamageToPlayer` (default 5, user: "hardcore
  singleplayer"), after armor; and NPC hits clear the victim's hit
  cooldown first (LivingAttackEvent), so every hit of a group lands
  (`everyNpcHitCounts`). Both in config/deciworldgen_npc.cfg, category
  "player"; code NpcLoadouts.onAttack / onHurt.
- **Numbers (checks mode)**: an NPC M4A4 hit (2) on a bare player 10.00 hp
  (half the bar), full marine body set 1.49 (was 0.30), 5 hits in one
  tick 7.47 (all land; before only the first). Helmet headshots (20%)
  cut a hit further. Armed traders also use "human" at full gun damage
  but only ever target infected and bandits, never players.
- Not seen in game by the user yet `[not verified]`.
- **v0.30.3 (user: "too hard")**: the hit cooldown is back (config
  `npcHitsSkipCooldown`, default false; the v0.30.2 key
  `everyNpcHitCounts` is removed from the file on load); x5 stays. Full
  marine body set: 5 hits in one tick now 1.49 (the first lands).

## Soviets kill each other (reported 9 Oktober 2026, FIXED v0.30.3)
- **Cause** (bytecode of deci.ag.m.c, original Decimation):
  SovietEntity.isHostileTo returns, for another armed human, faction ==
  SOVIET: Soviets were hostile ONLY to Soviets (and ignored bandits and
  soldiers). Shown once MilitarySpawner put them in groups. (The deobf
  naming note "hostile to everything except Soviets" described the
  intent, not the code.)
- **Fix** `tools/patches/PatchFactions.java` (built on PatchTracer's
  deci/ag/a): Soviets hostile to every other armed human except Soviets
  and traders; bandits and soldiers (and hazmat soldiers) hostile to
  Soviets. Our traced NPC shots (NpcShots) pass through allies of the
  shooter's side, so no friendly fire. Checked: npc mode "factions"
  [soviet>soviet false, soviet>bandit / soldier, bandit / soldier>soviet
  true].

## NPC shots were not bullets (user request 9 Oktober 2026, DONE v0.30.3)
User: a player should only take damage when the tracer hits; plus NPC
bullet impacts on blocks (until now only player shots made them).
- **Before**: shootAt hit its target on 75% of shots through anything once
  it could see it; the tracer was a picture only, and Decimation's client
  makes 60% of tracers invisible (INVIS colour) unless the gun has a
  tracer attachment.
- **Now** (`tools/patches/PatchTracer.java` v2 + `fixes/NpcShots`):
  BanditEntity.shootAt asks our hook (`shotHook`), which traces a shot
  from the shooter's eyes toward the target's chest with a gaussian
  spread per tier (degrees: bandit light 1.6, medium 1.3, heavy 1.1,
  soldier 1.0, military 0.9; machine guns x1.4, sniper rifles 0.25;
  config `spread`), stops at the first solid block (glass, plants, vines,
  iron bars, ladders pass, as for player guns) or the first living thing
  that is not an ally, and only that takes Decimation's damage (gun / 8,
  full on infected). A block hit sends Decimation's own impact particles
  ("explode" + "blockcrack", as the player block hit handler does). The
  packet carries a point far along the shot line, the client draws the
  tracer along it and always visible.
- **Checked** (dev test modes, fresh world, all PASS): shots: AKM medium
  bandit at a pig, 97% hits at 10 blocks, 57..66% at 20, 0 / 143 through
  a stone wall, impact particles on the wall (shots_wall); tracer: 163
  shots, 163 visible tracers, 0.2 deg mean from the server's shot line,
  2.1 deg mean from the target (the spread). Not seen in game by the user
  yet `[not verified]`.
- Test traps found: an NPC held in mid air falls on the client (tracers
  then start lower): test NPCs stand on a block now; the tracer test only
  counts its own bandit's shots (nearby NPCs fight each other since the
  faction fix).
- **v0.30.4 (user: shorter, 0.25 s)**: `npcHitCooldownTicks` (default 5;
  10 = vanilla, 0 = every hit lands) replaces `npcHitsSkipCooldown`. An
  NPC hit 4 ticks after the last is dropped, 5 ticks after lands
  (checks mode); a group lands at most about 4 hits a second.

## Zone rules dead in singleplayer: safezone PvP / water / traps, radiation, scary, client zone lists (found 11 Oktober 2026, FIXED v0.46.0)

Found while studying the zone system for the safezone feature (docs/zones.md). Decimation's zone
handlers that are not about plain damage live in the server proxy with @SideOnly(SERVER) methods
(PlayerZoneTickHandler deci.aK.m, SafezoneAttackHandler deci.aK.g, FriendlyFireHandler
deci.aK.i, most of EntitySpawnZoneHandler deci.aK.d), so in singleplayer: radiation zones never
irradiated anyone, safezones never refilled water, traps / rockets / grenades were not kept away,
the grace timer protected nobody, conditions could start inside a safezone (PlayerData setters
check the zone only on a dedicated server), and the client never got any zone list (only
ZoneManager.refreshZones sends one, with the json file zones only), so no "Now entering a
Safezone" line and no map markers, on a server too for our generated zones.
Fix: fixes/ZoneRules (copies of the server handlers, stepping aside on a dedicated server, plus
the client zone list sync on both) and ZoneSpawnHandler (traps, projectiles; ours: no bandits,
Soviets, mechs spawning inside a safezone). Checked: dev test `zones`, 19 checks PASS.

## Survivor camp NPCs missing (reported 11 Oktober 2026 on 0.47.0, FIXED v0.47.1)

User shot docs/shots/safezone_camp_v0.47.0_review/user_119.png: the market of the camp with no
trader, guard or civilian. The user suspected Decimation's trader "reset" countdown. Read from
the code: that reset is real but server only (ServerTickHandler traderRestockCountdown 3000
ticks -> TraderSpawnManager.respawnTraders removes every TraderEntity and spawns those of
decimation_traders.json), stripped in singleplayer. The camp shown was the dev test's sky camp
in deciworldgen_devtest (the user's game had that world open, log "survivor camps off"), whose
residents and site the camp test removed at its end. Fixes: the test keeps its camp populated;
on a dedicated server the keeper respawns traders and guards at once after a reset; awning
front edges coloured; `/deciworldgen camp` to place a populated camp anywhere. Checked: camp
test all PASS, all 20 residents saved in the world after the test, campcmd 20 residents. Peaceful
difficulty removes Decimation humans (EntityMob) `[inferred]`, documented in docs/safezone.md.

## Camp guard "flying" over the wall (reported 11 Oktober 2026 on 0.47.1, FIXED v0.47.2)

User shots docs/shots/safezone_camp_v0.47.1_review/user_120.png and user_121.png: a guard
standing in the air over the scrap wall next to a tower. Guards (Decimation's GuardTrader) are
static: TraderEntity resets its position every tick, so one spawned before the blocks under it
exist hangs in the air. The keeper only checked that the chunks existed, while the camp is
written during chunk population (and a camp built over an older one keeps the older residents
in place) `[inferred: not reproduced; the 30 s test after the fix had nobody off post]`. Fix:
the keeper waits until every chunk under the site and one around is populated, and puts any
trader / guard off its post, or any civilian / survivor on a wall, roof or outside the walls,
back to its post every 2 s (logged). Same review: provocation removed, entry sign one block out,
more cosmetic props.

## Canvas metadata 0 never turned with a rotated compound (found 11 Oktober 2026, FIXED v0.48.0)

Canvas.set(x, y, z, block, meta) marked a cell VANILLA (turned by Rotation) only when meta was not
0, so blocks whose metadata 0 has a direction stayed unturned in a plan rotated by 90 / 270:
stairs ascending east, fence gates spanning x (the survivor camp gate and garden gate in camps at
turns 1 / 3 spanned across their wall line) `[inferred from the code; not looked at in game]`.
Found building the city refuge (its stair runs ascend east). Fix: every set with metadata is
VANILLA (Rotation leaves blocks without a table unchanged). Checked: refuge turns 1 dump (stairs
meta 2, gate meta 1), camp turns 1 and bases turns 1 propclash / walkcheck 0.

## Dev tools: walkcheck stopped at y 4, propclash counted shelf tops as floating (found 11 Oktober 2026, FIXED v0.48.0)

tools/props/walkcheck.py allowed a climb of one only below y 4 (towers and decks by ladder), so
the refuge's upper floors read as unreachable; stairs now climb at any height. propclash.py took
the first multiblock part per 4 block bucket as the master, so a shelf standing at y 11 had its
top part (y 12, next bucket) reported "nothing under it"; an upper part (same block below) is now
skipped. Also: `devtest.py MODE key=value` in a one launch run does not pass the option (only
-P flags the Gradle script knows); use `--live MODE key=value`.

# Safezone sites: survivor camps and city refuges

User request 11 Oktober 2026: "create safezone maybe that have trader + guard inside?"; choices
(new_feature.md "Zones, factions and world" item 1): one per city AND camps outside, a walled
camp of our own generator AND a fortified city building, guards friendly unless provoked
(provocation removed later, v0.47.2: friendlies get hit by accident),
traders, guards, unarmed civilians and armed survivors. The zone rules are docs/zones.md
(v0.46.0). This doc: the sites. v0.47.0 builds the survivor camps outside cities; v0.48.0 the
fortified city building ("city refuge", section 7), one per city region.

## 1. References (research 11 Oktober 2026)

- Survivor trader hubs in survival games and mods: a walled perimeter with watchtowers, guarded
  gates, market stalls and a trader area ("North East Traders" map for FiveM:
  https://forum.cfx.re/t/ymap-north-east-traders-apocalypse/5356629); DayZ trader mods put
  merchant NPCs inside a safezone with invincibility inside the zone, players "camping" just
  outside the boundary (https://help.freakhosting.com/games/dayz/installing-and-configuring-trader-mod.md).
  7 Days to Die traders: zombies gather outside the trader walls
  (https://help.freakhosting.com/games/7-days-to-die/trader-protection-and-opening-hours.md).
- UNHCR site planning (https://emergency.unhcr.org/sites/default/files/Settlement%20Folio.pdf,
  https://emergency.unhcr.org/print/pdf/node/531): plan from the family unit up (family tents
  grouped into communities); communal facilities (water points, latrines, food distribution)
  central and near the shelters; water points and latrines lit; layouts that prevent fires
  (firebreaks) and disease. No numbers were found in these pages `[not verified]`.

## 2. The camp (worldgen/military/SurvivorCamp, SurvivorCampPlan)

Canvas 47 x 47, height 12, gate south (built on the military base Canvas pipeline: drawn prop
boxes, validateProps, CanvasPlan turns 0..270 and grades the ground around over 10 blocks).
| Part | Where (canvas) | Blocks |
| Scrap wall | lines 2 and 44 | panels of BlockMetal_3 (rusty), BlockMetal_2 (dark plate) or spruce boards between oak log posts every 4, a sheet stacked higher here and there, torches on the posts |
| Concertina | outer ring 0 / 46, gaps, none before the gate | BlockConcertinaWire |
| Gate | south, x 21..25 | 5 closed fence gates between log posts, log lintel, BlockSafezoneSignEntry one block outside the lintel (on it the logs hid the text, v0.47.2) / Exit one block inside, two guards behind sandbags inside |
| Watchtowers | 4 corners, 4 x 4 | spruce filled base, ladder in a shaft on the camp side (front cell free), deck at 4, fence railing, log corner posts, slab roof at 8, a guard on the deck |
| Market street | x 21..25, z 15..43 | cobblestone; 3 stalls each side (z 27 / 32 / 37) |
| Stall | 5 deep x 4 long | plank floor, board back wall, counter of two BlockWoodTable (2 apart, drawn 2 long), trader behind it, two goods props at the back by trade (cabinets / cases / water / crates), striped wool awning on fence posts (coloured on the odd columns, so the front edge seen from the street is coloured; v0.47.0 had it white) |
| Plaza | x 15..31, z 15..24 | cobblestone with gravel; campfire (fire on netherrack, stone slab ring, benches 3 away facing it), 4 BlockHazardLight light towers, water tank (iron) with 2 cauldrons, water pallets |
| Medical tent | x 19..27, z 5..12 | white wool vault, red mark over the door, stretchers both sides, medical crates, lights |
| Family tents | 7 tents 5 x 6 | vault of sandstone / brown / green / grey wool, a bed, a footlocker, a light |
| Latrines | x 35..40, z 9..12 | two plank outhouses with doors, a wash cauldron |
| Generator yard | x 7..12, z 33..38 | generator, barrels, tyres, crate, light tower |
| Garden | x 33..39, z 33..39 | fenced farmland, carrots / potatoes / wheat, a water channel, a fence gate |
| Clutter (v0.47.2, user: "add more cosmetic props") | strips between wall and tents, plaza, stall gaps | military jeeps (along z at metadata 2 / 3, along x at 4 / 5), a civilian car wreck, two care packages, crates (side by side facing E: drawn 1.4 across the front), water pallet, cardboard, tyre stacks, barrels, trash bags, a plaza table with two chairs |
Traders: guns, ammunition, food, medical always, plus two of armor, backpack, convenience,
scrap, fortification, black market, heavy guns, gold cap (shuffled per camp).
No loot: Decimation gives no loot from containers inside or within 10 blocks of a safezone.

## 3. Residents (worldgen/SafezoneSites)

- Posts per camp: 6 guards (GuardTrader "TraderGuard": static, M16 / M4, shoots infected and
  bandits within 32), 6 traders, 5 civilians ("Human", HumanEntity2), 3 armed survivors
  (SoldierEntity with our tier "survivor": casual clothes, pistol / shotgun / Mosin / SKS,
  NpcLoadouts, weights 0).
- Registered when the camp's first slice is written (Slices, interface Residents) with the
  SAFEZONE box (plan +6), stored in `<world>/deciworldgen_safezones.json`.
- Not spawned during generation. The keeper (server tick every 2 s): a site with a player within
  112 blocks and all its chunks loaded gets its missing residents, all at the first visit
  (`filled`), later a dead or missing one again after 5 minutes while no player is within 40
  blocks of its post. Residents carry entity data deciwgSite / deciwgPost, never despawn.
- Dedicated server: Decimation's ServerTickHandler (server only) removes EVERY TraderEntity each
  3000 ticks (2.5 min, traderRestockCountdown) and respawns only those of decimation_traders.json
  (TraderSpawnManager.respawnTraders): the user remembered this "reset" from setting up a
  Decimation server. There the keeper respawns trader and guard posts at once (no 5 minute wait).
  In singleplayer that handler is stripped, the reset never runs (read from the code
  11 Oktober 2026).
- Peaceful difficulty: every Decimation human (traders and guards too) is an EntityMob, and
  vanilla removes mobs on peaceful each tick: no camp residents on peaceful `[inferred from
  EntityMob.onUpdate; not tested]`.
- `/deciworldgen camp [turns]`: a camp centred on the player (graded), its zone and people
  registered as for a generated one (an earlier camp of the same id is replaced).
- Protection: docs/zones.md section 5 (every friendly resident is protected inside the safezone;
  a player's hit on one does nothing, no provocation since v0.47.2).
- Spawn timing and places (v0.47.2, user review: a guard "flying" over the wall): the keeper
  fills a site only when every chunk under it and one around is populated (the structure is
  written during population; a static guard spawned before its tower would hang in the air);
  every 2 s it puts back a trader / guard found off its post and a civilian / survivor found
  more than a step above its post (wall, roof, tower) or outside the inner area (log line
  "safezone resident ... put back from x,y,z"). In the 30 s camp test nobody had to be put back
  and nobody left their post.

## 4. Placement (LargeSites)

- Wild and civilian sectors, own roll per 128 block site: 5 % (CAMP_CHANCE), before the large
  schematic roll (other sites unchanged), placed 12 blocks inside the site, never near a city or
  a highway (10 blocks): up to 6 spots per site are tried (one fixed spot skipped the first
  site found on seed 1); id "S<siteX>_<siteZ>". Log lines "camp site S.. planned at x,z
  (spot n)" / "skipped (city or highway near every spot)" once per site. Seed 1 simulation
  (jshell, the same rolls): 217 camp sites of 4244 wild / civilian sites within 40 sites of
  spawn (5.1 %); the nearest is S6_3.
- Only in worlds created since v0.47 (StructureData "!safezoneCamps", set at the first load of a
  world that never ticked; older worlds would get camps cut off by chunks generated before);
  `-Ddeciworldgen.camps=true|false` overrides; dev tests use SafezoneSites.forceCamps.

## 5. How to test

- Dev test `camp` (devtest/CampTest): the camp in the sky at -20000, 230, -19700 with its zone
  and residents registered as the generator does; checks: no clash / refusal in the generator
  log, every resident spawned by kind, the player inside counts as in a safezone, a gunshot on a
  trader does nothing; one shot per camera point; writes run/client/devtest/camp.tsv.
  `-Pturns=0..3`, `-Pseed=N`. Since v0.47.1 the camp keeps its zone and people after the test
  (an earlier run's are removed first): the user visited the sky camp and found it empty
  (11 Oktober 2026, docs/shots/safezone_camp_v0.47.0_review/user_119.png).
- Dev test `campcmd` (devtest/CampCommandTest): runs `/deciworldgen camp 1` as the player on
  real ground at -21000, -19700 in the test world, checks the site and its 20 residents, one
  aerial shot.
- `python3 tools/props/propclash.py dev/run/client/devtest/camp.tsv` and
  `python3 tools/props/walkcheck.py dev/run/client/devtest/camp.tsv`: both 0 (walkcheck opens
  fence gates since v0.47.0).
- Real worldgen: `python3 tools/servertest.py 1 type=decimation pregen=0,0,36`, log lines
  "survivor camp (safezone)" and "safezone site ... residents".

Checked v0.47.0 (11 Oktober 2026): turns 0 and 1 all PASS (6 guards, 6 traders, 5 civilians,
3 survivors), propclash 0 (60 props), walkcheck 0; first build had 6 clashes (stretchers into
the tent end, long cases 1 cell apart: now 2) and an outhouse seat leaving no head room.
Checked v0.47.1: camp test turns 0 all PASS (residents 6 / 6 / 5 / 3), the site and all 20
residents saved in the test world after the test (deciworldgen_safezones.json, entity data in
the region files), awning fronts coloured; campcmd PASS (20 residents). Not tested: the dedicated
server respawn after Decimation's trader reset `[not verified]`.
Real worldgen: seed 1 fresh server world, `pregen=832,448,9`: camp S6_3 at 816,70,399, turned
90, 20 residents registered; top down render seed1_S6_3_top.png: the camp on grass, ground
graded around it, trees cleared. Residents spawn only with a player near (not in servertest).
Shots: docs/shots/safezone_camp_v0.47.0/ (shots_index.md).

## 6. Next

- Destroyed by the player: plan in docs/zones.md section 8.

## 7. City refuge: the fortified city building (v0.48.0, 11 Oktober 2026)

User choices (11 Oktober 2026, "start the fortified city building now"): OUR OWN building on one
whole city block (not a fortified DeceasedCraft building: their interiors differ per building
and rotation, the community centre is a campus with a pool and garden, so trader posts would be
hand picked per building), on the block nearest the middle of each city region.

References (research 11 Oktober 2026): entry control points are built as segments (approach,
identity check, inspection, a way to turn back) with barriers that slow the way in and a guard
post; standoff from the street by barriers before the wall
(https://breakingac.com/news/2026/jan/05/designing-safer-access-control-points-for-military-and-government-sites/,
UFC 4-022-01 cited there; the numbers are FOUO `[not verified]`). Shelters in public buildings:
reception / registration at a controlled entrance, dormitory, medical area, cafeteria, command,
distribution point, generators away from the rooms (NJ shelter field guide
https://nj.gov/humanservices/oem/documents/Shelter_Field_Guide_508_f3.pdf, Virginia MRC shelter
training https://www.vdh.virginia.gov/content/uploads/sites/17/2022/02/MRC-Shelter-Training-Slides.pdf).

Code: worldgen/military/CityRefuge (canvas 48 x 48 x 21, gate south) and CityRefugePlan
(CanvasPlan + FixedBase + Residents: ground floor at the city ground, no grading, Slices adds the
SAFEZONE box 2 around and the residents with wall inset 4). Placement: LcCity.refuge(seed, rx,
rz) picks, per city region (4 x 4 cells), the plain block (no superblock, no river chunk)
nearest the region's middle (ties by hash), turned so the gate opens on a street at the block's
own ground (west / north are its own streets, east / south only when the next cell is at the
same level), toward the region's middle when possible. LcCity.plan() then puts the refuge plan
on the 3 x 3 building chunks instead of buildings (id lcz_<cellX>_<cellZ>, log "city refuge
(safezone) rot N lcz_.. at x,y,z").
| Part | Where (canvas) | Blocks |
| Apron | rows 0..1 along the street | jersey barriers (BlockBarrier, drawn 2 long) every 3 with gaps, concertina wire behind, none before the gate |
| Wall | lines 2 / 45 | concrete (BlockStone_7) 3 high, stone brick posts every 6 one higher with a torch, wire on top here and there |
| Gate | south, x 21..26 | 6 closed fence gates, stone brick pillars, concrete lintel at 4, entry sign one out and one up, exit sign inside |
| Entry channel | x 19..28, z 38..44 | sandbag walls 2 high on x 18 / 29, jersey lines at z 42 (open east) and z 39 (open west): a chicane, a sandbagged guard each side of the gate |
| Building | x 9..38, z 6..29, floors 0 / 5 / 10, roof 15 | concrete walls, stone brick corners and floor bands, parapet; windows every 3rd column: ground floor boards + iron bars, upper floors glass, some sandbagged (more on the top floor) |
| Entrance | south face x 23..24 | double metal door (Door_Metal_3), sandbag vestibule, stone brick lintel, wall flag above |
| Ground floor | lobby z 24..28; market hall x 17..30 | registration desk (metal table, office chair, monitor, guard), waiting benches; six stalls along a stone brick aisle (x 22..25): plank floor, two wood table counter, trader, goods by trade, striped wool awning under the ceiling |
| Clinic / canteen | x 10..15 / 32..37, doors at z 26 | stretchers, patient beds, medical crates, a shelf, desk / cooking stations, serving counter, three tables with chairs, water |
| Stair | north wall, x 25..29, rows 7 / 8 | switchback of 5 step runs (stone brick stairs) hall -> first floor -> second floor -> roof bulkhead (x 24..31, z 6..9, door south) |
| First floor | whole floor | dormitory: 4 rows of beds (head north), footlockers, middle aisle free, lights |
| Second floor | rooms like the ground floor | armory west (shelves, weapon cabinets, ammunition, work table, tool locker), guard bunks east (beds, crates), command in the hall (radio desks, big radio, monitor), stores north |
| Roof | parapet at 16 | four sandbag nests (2 high L) with a guard each, spotlight over the entrance, flag pole, water tank, radio tower |
| Yard (asphalt) | around the building | forecourt (fire barrel, benches, light towers), 4 tents south west (back row opens north), aid distribution (3 tables, supplies, queue sawhorses, a car) south east, APC + generator yard west, water tanks + 3 raised garden beds east, jeep, crates, bins north |
Residents: 7 guards (2 at the gate, 1 at the registration desk, 4 on the roof), 6 traders
(guns, ammunition, food, medical + 2 of the extras, as the camp), 6 civilians (forecourt,
dormitory, canteen, tents), 3 armed survivors. Kept by SafezoneSites as the camp's (section 3);
civilians and survivors are put back when found more than a step above their post (a
dormitory civilian may walk down, a yard one may not climb) or on / past the wall (Site.inset 4:
site box margin 2 + wall line 2).
On: worlds created since v0.48 (StructureData "!cityRefuges"; -Ddeciworldgen.refuges=true|false
overrides; SafezoneSites.setRefuges for dev tests); older worlds would get a refuge cut into a
block whose other chunks already hold buildings.

How to test:
- Dev test `refuge` (devtest/RefugeTest, the camp test on CityRefugePlan): sky build at
  -20000, 230, -19400, same checks as the camp, writes run/client/devtest/refuge.tsv. Rotations:
  `python3 tools/devtest.py --live refuge turns=N points=none` (one launch runs ignore key=value
  options: only the live game forwards them). The test clears the box first (see below).
- `python3 tools/props/propclash.py dev/run/client/devtest/refuge.tsv` and walkcheck.py: 0 / 0
  (walkcheck climbs stairs storey by storey since v0.48.0).
- Real worldgen: `python3 tools/servertest.py 1 type=decimation pregen=128,128,9`: log "city
  refuges on", "city refuge (safezone) rot 270 lcz_1_2 at 80,64,144" and its safezone site with
  22 residents; ground at 64 like the streets on both sides (cross section from the region file).

Checked v0.48.0 (11 Oktober 2026): turns 0 / 1 / 2 / 3 all PASS (residents 7 / 6 / 6 / 3, filled,
inside counts as safezone, trader shot unhurt, 0 off post after 30 s, survivors in survivor
gear), propclash 0 (222 props), walkcheck 0 (3445 reachable cells: every floor, the roof and its
nests, every door and loot container). Rotations checked in the dump: stairs, gate, doors, beds,
wrecks turn with the plan. Seed 1 server: two refuges planned (regions 0,0 and -1,0); the one
inside the pregenerated area complete (signs, 15 stairs, 6 metal door halves, shelves, beds,
vehicles), its ground level with the streets; the other half written (its chunks were outside
the pregenerated area, residents spawn only once all its chunks are populated).
Found on the way: the first build had 3 prop clashes (goods drawn into the gaps between stalls),
upper floors unreachable in walkcheck (it never climbed above y 4: stairs added), tents of the
back row opening into the wall, a light tower facing a tent, cameras inside furniture, a crate
drawn into the second floor partition. A rebuild over an earlier test build at another rotation
lost a tent: writing over a multiblock prop (the APC) breaks its parts, which took freshly
written cells with it; the test now clears the box first. The same holds for /deciworldgen camp
over an earlier camp `[inferred]`.
Shots: docs/shots/city_refuge_v0.48.0/ (shots_index.md).

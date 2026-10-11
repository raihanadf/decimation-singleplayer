# Safezone sites: survivor camps (and fortified city buildings, next)

User request 11 Oktober 2026: "create safezone maybe that have trader + guard inside?"; choices
(new_feature.md "Zones, factions and world" item 1): one per city AND camps outside, a walled
camp of our own generator AND a fortified city building, guards friendly unless provoked,
traders, guards, unarmed civilians and armed survivors. The zone rules are docs/zones.md
(v0.46.0). This doc: the sites. v0.47.0 builds the survivor camps outside cities; the fortified
city building is the next step `[not built]`.

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
| Gate | south, x 21..25 | 5 closed fence gates between log posts, log lintel, BlockSafezoneSignEntry (outside) / Exit (inside) over it, two guards behind sandbags inside |
| Watchtowers | 4 corners, 4 x 4 | spruce filled base, ladder in a shaft on the camp side (front cell free), deck at 4, fence railing, log corner posts, slab roof at 8, a guard on the deck |
| Market street | x 21..25, z 15..43 | cobblestone; 3 stalls each side (z 27 / 32 / 37) |
| Stall | 5 deep x 4 long | plank floor, board back wall, counter of two BlockWoodTable (2 apart, drawn 2 long), trader behind it, two goods props at the back by trade (cabinets / cases / water / crates), striped wool awning on fence posts (coloured on the odd columns, so the front edge seen from the street is coloured; v0.47.0 had it white) |
| Plaza | x 15..31, z 15..24 | cobblestone with gravel; campfire (fire on netherrack, stone slab ring, benches 3 away facing it), 4 BlockHazardLight light towers, water tank (iron) with 2 cauldrons, water pallets |
| Medical tent | x 19..27, z 5..12 | white wool vault, red mark over the door, stretchers both sides, medical crates, lights |
| Family tents | 7 tents 5 x 6 | vault of sandstone / brown / green / grey wool, a bed, a footlocker, a light |
| Latrines | x 35..40, z 9..12 | two plank outhouses with doors, a wash cauldron |
| Generator yard | x 7..12, z 33..38 | generator, barrels, tyres, crate, light tower |
| Garden | x 33..39, z 33..39 | fenced farmland, carrots / potatoes / wheat, a water channel, a fence gate |
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
- Protection, provocation: docs/zones.md section 5 (every friendly resident is protected
  inside the safezone; a player who attacks one is fired on by the guards and soldiers around).

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
- `python3 tools/props/propclash.py dev/run/client/devtest/camp.tsv` and
  `python3 tools/props/walkcheck.py dev/run/client/devtest/camp.tsv`: both 0 (walkcheck opens
  fence gates since v0.47.0).
- Real worldgen: `python3 tools/servertest.py 1 type=decimation pregen=0,0,36`, log lines
  "survivor camp (safezone)" and "safezone site ... residents".

Checked v0.47.0 (11 Oktober 2026): turns 0 and 1 all PASS (6 guards, 6 traders, 5 civilians,
3 survivors), propclash 0 (60 props), walkcheck 0; first build had 6 clashes (stretchers into
the tent end, long cases 1 cell apart: now 2) and an outhouse seat leaving no head room.
Real worldgen: seed 1 fresh server world, `pregen=832,448,9`: camp S6_3 at 816,70,399, turned
90, 20 residents registered; top down render seed1_S6_3_top.png: the camp on grass, ground
graded around it, trees cleared. Residents spawn only with a player near (not in servertest).
Shots: docs/shots/safezone_camp_v0.47.0/ (shots_index.md).

## 6. Next

- Fortified city building, one per city (user choice): a converted building chosen near the
  city centre, barricaded (sandbags, boarded windows, wire), traders on the ground floor, guards
  at the doors, the SAFEZONE box tight around its lot `[not built]`.
- Destroyed by the player: plan in docs/zones.md section 8.

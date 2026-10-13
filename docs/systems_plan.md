# Systems plan: loot, human NPCs, zombies, world (13 Oktober 2026)

PLAN, no code yet. The user asked to plan every open feature together, "so we can syncronize with
other feat docs and make proper code later since its share same method or class later". This doc
is the master: the shared core every feature uses (section 2), then each system with the user's
choices and a draft design (sections 3 to 6), the build order (section 7) and what is still open
(section 8). Loot details live in docs/loot_levels.md. Sources of the features: docs/ideas.md,
docs/roadmap.md, new_feature.md "Zones, factions and world".

Markers: facts read in code this session carry no marker; `[inferred]` = reasoned from code not
run; `[not verified]` = not checked; `[draft]` = a number or rule of mine for the user to change.

## 1. What exists today (read in code, 13 Oktober 2026)

| thing | where | today |
|---|---|---|
| crate loot | LootTableRegistry deci.aD.l, LootPool deci.aB.c | 33 blocks, 12 hardcoded lists, 1 .. n-1 items per open (fixed to 1 .. n in v0.57.0), rarity per item (docs/loot_levels.md section 2) |
| infection | PlayerData (decimation.player_data) | one boolean; 8 % chance per infected bite; while infected 1 damage at intervals (DamageSources.infected) until a cure syringe; refused inside a safezone on a dedicated server |
| armor | ItemArmorDeci | every Decimation piece is built with ArmorMaterial.CHAIN, so a hoodie and juggernaut plate block mob hits alike `[inferred: vanilla reduction from the material]` |
| demons | DemonEntity deci.ag.b | EntityMob, 100 hp, spawns capped by ServerConfig maxDemonCount |
| radiation | zones EnumZoneType.RADIATION, PlayerData irradiated | damage unless wearing a gas mask; works in singleplayer since v0.46.0 (ZoneRules); we generate no radiation zones; our "Irradiated Military Zone" biome has no effect |
| turfs | TurfManager, ClanManagerV1 | clan turfs from decimation_turfs.json; income 20 caps per owned turf every 18000 ticks (15 min), dedicated server only `[not verified in singleplayer]` |
| place names | worldgen/PlaceNames, city/CityNames | districts and named bases sent as the turf list (v0.52.0) |
| NPC tiers | fixes/NpcLoadouts | bandit light / medium / heavy / rpg, soldier_*, military, military_rpg, juggernaut, elite_military, juggernaut_sniper, elite_sniper, survivor; eggs (fixes/NpcEgg) |
| zombie variants | fixes/InfectedVariants | runner, riot, screamer, night frenzy; Decimation's own crawler, dog, frail, gawker, hulk, bloater |
| gunshot noise | fixes/GunNoise | infected and enemy NPCs walk to a shot heard (64 blocks, 12 suppressed) |
| safezones | worldgen/SafezoneSites, military/SurvivorCamp, CityRefuge | camps outside cities, one refuge per city region |

## 2. Shared core (build once, used by everything)

Each piece below is used by two or more systems; building it first avoids writing it twice.

### 2.1 WorldClock (BUILT v0.57.0: fixes/WorldClock)
- The world day (world time / 24000 + 1), plus a schedule of events by day. Nothing is stored: the
  day comes from the world time, which the world saves. Events DAY (time 0), DUSK (12000), NIGHT
  (13800), DAWN (22200), fired in time order when the time crosses them (sleep and /time add
  included, each type once per jump; /time set back fires nothing); `WorldClock.listen`.
- Used by: zombie growth and unlocks (5.4), horde nights (5.5), NPC raids (6.3), claim decay and
  revenue (6.2), infection stage timers (5.2).
- One server tick handler fires day start, dusk and dawn events; every system subscribes instead
  of keeping its own timer.

### 2.2 Pools (loot lists, roller, loot screen bridge) (BUILT v0.57.0: fixes/LootPools)
- One registry of named item lists from a config file of ours (`deciworldgen_loot.json`): items
  with weight and count, items per open, refill minutes. Our own roller (rarity per list, not per
  item).
- `fixes/Loot.open(player, inventory)`: shows a rolled inventory on Decimation's loot screen (the
  packet and GUI_LOOT LootInteractHandler uses). VERIFIED by the spike (docs/loot_levels.md
  section 7).
- Used by: crates of every kind and level (3.1), NPC bodies (3.2, 4.4), supply drops, claim
  stashes (6.2), radiation site loot (6.4), raid rewards.

### 2.3 Body entity
- One entity of ours holding an inventory; right click opens it through 2.2; gone when empty or
  after a timer. Spike version exists: `fixes/LootBody` (VERIFIED, docs/loot_levels.md section 7;
  no model yet).
- Used by: NPC bodies (3.2), the player's gear after turning (5.2) `[draft: or Decimation's own
  EntityCorpse there]`, raid groups' bodies.

### 2.4 Tier table (NpcLoadouts grown)
- Every human NPC tier in one table: faction, outfit sets, guns, drop table (4.4), smart behaviour
  flags (4.3), egg. New tiers go LAST (egg metadata = index).
- Used by: police (4.1), outfits (4.2), sniper bandit (4.2), bodies (3.2), raids (6.3), garrisons.

### 2.5 Areas registry (zones we own) (SKELETON v0.57.0: worldgen/Areas)
- Our areas with a kind and a state, mapped onto Decimation's zone list (Deci.zoneType) and the
  turf list (PlaceNames) so its banner, HUD and zone rules work.
- Kinds: SAFEZONE (camps, refuges, police stations), MILITARY, RADIATION (6.4), BASE_CLAIM and
  CITY_CLAIM (6.2), each with an owner and a state machine.
- Used by: claims, raids, radiation, police safezones, horde nights (safezones are not safe on a
  horde night? 8, open), zombie spawning rules.

### 2.6 Protection table (armor values) (BUILT v0.57.0: fixes/Protection, not used by damage yet)
- Per armor item: protection against infected hits, against NPC gunfire (today
  ArmorGunfireHandler), bite infection chance, radiation (gas mask, hazmat).
- Used by: armor against zombies (5.1), infection (5.2), radiation (6.4), NPC shots.

### 2.7 Group spawner
- MilitarySpawner's group logic as a shared service: spawn a group of a tier mix at a spot, on
  good cells, with a goal (patrol, attack an area, guard).
- Used by: enemy patrols today, raids (6.3), reinforcements, horde waves (5.5), police patrols.

## 3. Loot (docs/loot_levels.md has the lists)

### 3.1 Crates (user, 13 Oktober 2026)
- Every kind splits by CONTENT TYPE in 4 levels (user: "military crate divided by 3 level just by
  type. level 1 is magazine, level 2 is attachment, level 3 is guns it self and last one is level 4
  that include all of them in one crate"; other kinds the same way, chosen):

| kind | L1 | L2 | L3 | L4 |
|---|---|---|---|---|
| military | magazines and ammo boxes | attachments (sights incl. EOTech / ACOG, suppressors, grips, lasers) | guns | all of them |
| police | ammo and magazines | gear (vests, batons, helmets, grenades, keycards) | guns | all of them |
| medical | bandages and basics | drugs (morphine, painkillers, adrenaline) | surgery, blood, cure | all of them |
| civilian | food and drink | tools and scrap | packs, clothes, melee | all of them |

- Look: one crate shape per kind, the band colour tells the level (user choice "colour band"):
  L1 grey, L2 green, L3 red, L4 gold `[draft colours]`.
- Placement by building value; L4 only in the rarest places: bunker armory, supply drops, the
  best locked wasteland building, the base command post (TOC).
- Blocks: four of ours (one per kind), metadata = level x 4 + facing (16 values, fits); baked
  block render, no tile entity.

### 3.2 NPC bodies
- A human NPC killed by a player leaves a body (2.3); contents per tier (4.4). Killed only by
  zombies or other NPCs: nothing. Gone after 5 minutes or when empty.
- Smart NPCs may loot bodies too (4.3), so a body can be emptied before the player gets there.

### 3.3 Supply drops
- The military L4 list with 4 to 6 items `[draft]` instead of Decimation's list 11 (open, 8).

## 4. Human NPCs

### 4.1 Police (user: neutral survivors)
- New faction POLICE: fights infected and bandits, neutral to the player until shot at (then
  hostile to that player for a while `[draft: 5 minutes]`), friendly to survivors and traders.
- Tiers: police_patrol (pistol / shotgun), police_swat (MP5 / UMP9, helmet, vest), police_sniper
  (rare) `[draft]`; NYPD outfits (shirt, pants, coat, helmet, boots, police vest).
- Where: the barricaded police station safezone (6.5), patrols of 2 to 4 on streets near police
  buildings `[draft]`.
- Built on an existing human class (Soldier or Human) with our faction rule (the Soviets fix in
  tools/patches/PatchFactions.java is the model) `[inferred]`.

### 4.2 Outfits and the sniper bandit (ideas 3 and 4)
- More outfits per tier from Decimation's armor sets: marine colours (forest, urban, black, snow,
  blue), spetsnaz, SAS, ghillie for snipers, hazmat for lab guards; bandits in casual sets with
  balaclavas.
- bandit_sniper tier (hunting rifles: mosin, kar98k, sks, m14) and its egg, last in the table.

### 4.3 Smart NPCs (user chose: cover and flank, retreat and heal, loot and pick up)
- Cover: when shot at, move to the nearest cell with a solid block between it and the shooter
  (walls, cars, sandbags) within 8 blocks `[draft]`; shoot from there.
- Flank: in a group of 3 or more, one or two hold while the others walk round to a cell with a
  clear line from the side (90 degrees off the line to the target) `[draft]`.
- Retreat and heal: below 35 % health `[draft]` fall back out of sight, heal over 10 s (bandage
  animation if there is one `[not verified]`), come back. Juggernauts never retreat.
- Loot and pick up: NPCs walk to a body or dropped gun within 12 blocks when no enemy is in sight
  and take guns, magazines and armor better than theirs.
- Flags per tier (2.4): bandits all three, military all, juggernauts no retreat, police cover and
  retreat, survivors cover only `[draft]`.
- AI tasks added to Decimation's NPCs at join (like GunNoise does today).

### 4.4 Drop tables (bodies)
- Per tier: gun chance, magazines, each worn armor piece chance, extras from a crate list, caps.
  Draft table in docs/loot_levels.md section 4.2; police tiers get police L1 / L2 extras.

## 5. Zombies

### 5.1 Armor against zombies (user: per armor type)
- Protection table (2.6): clothes about 0, police / military sets medium, plate carriers and
  juggernaut high `[draft numbers]`; a helmet counts against bites to the head; armor lowers the
  bite infection chance (8 % bare, down to 2 % in full heavy armor `[draft]`).

### 5.2 Infection in 5 stages (user: die and turn)
- A bite that infects starts stage 1; each stage lasts about one game day `[draft]`:
  1. fever: no effect but a message and a cough sound;
  2. slower healing (no natural regeneration);
  3. weak and hungry: thirst and hunger faster, slower sprint;
  4. damage over time (Decimation's infection damage) and blurred view at times;
  5. death; the player rises as an infected wearing their armor look, their gear in a body (2.3)
     or Decimation's corpse at the spot; killing the turned one is how you get it back.
- Cures: antibiotics stop stages 1 and 2 (item: reuse Decimation's vitamin capsules or a new item
  of ours, open), the cure syringe works up to stage 4. Stage kept in our player NBT next to
  Decimation's infected flag.

### 5.3 Smarter zombies and melee zombies
- Remember the shooter (user choice): a zombie that hears a shot (GunNoise) stores WHO fired and
  hunts that player for 60 s `[draft]`, even out of sight, walking to the last seen spot.
- Melee zombies: a variant in InfectedVariants holding a melee item (crowbar, pipe, machete, bat:
  Decimation items) that hits harder (+3 `[draft]`), unlocked by day (5.4).

### 5.4 Growth from day 1 to day 100 (user: more of them, more specials, unlocks by day)
- Count: spawn count and horde size x1 at day 1 to x2.5 at day 100 `[draft curve]`.
- Specials: runner / riot / screamer / hulk / bloater shares rise over the days.
- Unlocks `[draft days]`: melee zombies day 10, demons day 30 (5.6), screamer hordes day 40,
  military outfit hordes day 60.
- Not chosen: stronger zombies (health, damage) stay as they are.

### 5.5 Horde nights (user, 13 Oktober 2026)
- User: "each 5 or 10 day will be horde night, so player cant sleep even its night time due to
  horde mode and all zombie from all distance know players place even its not horde zombie".
  Chosen: every 10th night until day 50, every 5th after.
- Warning in chat the evening before and at dusk (siren or radio sound); ends at sunrise.
- On a horde night: sleeping refused; EVERY infected loaded, any distance, gets the player as its
  target (5.3 lock without a shot); waves of extra infected spawn around the player out of sight
  (group spawner 2.7), size by day (5.4); demons join (5.6).

### 5.6 Demons (user: wasteland and horde nights)
- From the unlock day: a few at night in the wasteland and military zones, some in every horde
  night. Decimation's own demon mob and its cap.

## 6. World and zones

### 6.1 Personal base claim (user: "1 for personal life so its like base for player")
- Clear and plant a flag: pick a place (a building, a camp, a lot), kill every hostile in its
  area, place a claim flag (item: crafted or bought from a trader, open). It becomes the player's
  base: no infected spawn inside, a name on entry, stash crates keep their items (no loot
  refill), Decimation's protection rules of a turf (building allowed inside).

### 6.2 City place claims (user: "1 + 3 combined")
- User: "for capturing other city place ... need to kill zombies and humans inside that city and
  after its finishied the task, player automaticly claimed the place and player will got caps
  revenue in there like turn system legacy ... if player get out from the zone of it, its has
  scheldule time if player dont go back, the city place or zone will be claimed by nature again
  ... zone city not all of them is claimable, only 10% change its can be claimed".
- Which: 10 % of city districts are claimable (by seed, so a world keeps the same ones); the
  banner says so on entry `[draft]`.
- Task: kill the infected and hostile humans inside the district; a counter in the HUD or chat
  ("hostiles left: N") `[draft]`; spawning inside pauses while the player clears it `[draft]`.
- Claimed: automatically when the count is 0; the district name shows "controlled by <player>"
  (turf list), and pays caps like Decimation's turfs (20 caps every 15 min, its legacy rate)
  `[draft]`.
- Decay: when the player has not been inside for a set time (3 game days `[draft]`) the district
  goes back to nature: hostiles spawn again and it must be cleared again. A warning a day before.
- Raids (6.3) attack claimed districts and base claims.

### 6.3 NPC raids (user: warned raids)
- Every few days `[draft: every 4 to 7 days, from day 15]` a bandit or enemy military group
  attacks one claim; a chat / radio warning a minute before.
- Kill them all: the claim stays (and their bodies hold loot). If they reach the claim's flag (or
  the district's middle) and hold it for 60 s: the claim is lost and becomes a hostile area to
  retake.

### 6.4 Radiation (user chose all, plus the wasteland city)
- RADIATION zones (Decimation's rules: damage without a gas mask; hazmat for the strongest):
  - our Irradiated Military Zone biome;
  - crash and blast sites: missile crater, crashed military truck with barrels, reactor wreck; best
    loot in the middle (an L3 / L4 crate);
  - some wasteland labs and bunkers leak: radiation inside, hazmat needed for the L4 crate;
  - the wasteland city (user: "since its one of high tier loot city"): whole district or hot
    spots inside it (open, 8).

### 6.5 Police buildings in 3 kinds and the police safezone (user: police safezone + notes)
- Normal (police loot, police infected), abandoned (looted, decayed, bodies), barricaded safezone
  with neutral police (4.1), traders and survivors: one per city. Our refuge stays outside cities
  `[draft: or both, open]`.

### 6.6 Story (user: notes)
- Notes, radio logs and posters found in buildings telling what happened, by place (police
  station: last orders; hospital: patient notes; base: evacuation orders). Later: trader quests.
- Needs a readable item or block: check Decimation for a note / book item first `[not verified]`.

## 7. Build order (each step a release with its tests)

0. Spikes: the loot screen from our code (2.2), a body entity opening it (2.3). DONE 13 Oktober
   2026, both work (dev test `loot`, docs/loot_levels.md section 7).
1. Core: WorldClock, Pools + config, protection table, areas registry skeleton. DONE v0.57.0 (dev
   test `core`), with Decimation's off by one fixed.
2. Loot: crate blocks and look, lists, placement, supply drops; NPC bodies with drop tables.
3. Human NPCs: tier table grown, outfits, sniper bandit, police faction; smart behaviours.
4. Zombies: armor per type, infection stages, shooter memory, melee variant, growth, horde nights,
   demons.
5. World: radiation zones and sites, police buildings and safezone, base claim, city claims with
   revenue and decay, raids, story notes.
Tests per step as usual: dev test modes (arena for NPCs and zombies, servertest census for
placement), shots to docs/shots, docs and skills updated.

## 8. Still open (ask before building that step)
1. Loot lists per kind and level (docs/loot_levels.md); supply drop list (ours or Decimation's);
   fix Decimation's 1 .. n-1 for its own crates? (ANSWERED: fixed, v0.57.0)
2. Friendly NPCs leaving bodies or not.
3. Antibiotics: an item of ours or Decimation's vitamin capsules; stage length.
4. Claim flag item: crafted or bought; base claim size.
5. City claims: decay time, revenue, does clearing pause spawns.
6. Wasteland radiation: whole district or hot spots.
7. Horde night inside safezones and claims: safe or not.
8. Day numbers for unlocks and the growth curve.
9. Police safezone: replaces the city refuge or both.

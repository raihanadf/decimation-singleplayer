# Decimation code: NPCs and infected (humans, factions, zombies, spawning)

Part of the feature study of Decimation's own code (16 Oktober 2026, index docs/decimation_code/README.md). Our
tiers, shots and variants: new_feature.md "Step 1 design: NPC tiers", fixes/NpcLoadouts, fixes/NpcShots,
fixes/InfectedVariants. Traders are their own feature. Note: `deobf/src` was decompiled from the jar as it was on
7 Oktober 2026; later patches (PatchTracer v2 `shotHook`, PatchFactions, PatchInfectedAI) are not in it.

## 1. What it is for

The world is shared by infected (zombies in several kinds) and armed humans in factions: bandits, soldiers, Soviets,
hazmat soldiers, plus unarmed civilians and traders. Humans fight whoever their faction rules call hostile, players
included depending on the player's humanity.

## 2. Code map

| Class (obf) | Job |
|---|---|
| HumanEntity (deci/af/d) | base of every human AND of InfectedEntity: random name and looks (`randomizeIdentity`), stance and gesture with their animations, backpack / vest / mask slots, `getHumanityOnKill` |
| FactionHumanEntity (deci/ah/d) | adds `faction` (NpcFaction deci/ae/b: BANDIT, MILITARY, CIVILIAN, SOVIET; CIVILIAN default) and `hasNoFear`; is the civilian "Human" and the base of BanditEntity and every TraderEntity. Its `onUpdate` walks the WHOLE loaded entity list every tick to look scared near a gun |
| BanditEntity (deci/ag/a) | every armed human. Constructor rolls a gun (INSAS, AK-12, MP5, M14, Colt, Makarov) and gear: on each side separately, never synced. `onUpdate`: no AI target -> `findTarget` (eA, 32 blocks, 15 up and down); a target it can see -> 1 % `throwMolotovAt` (d), else `shootAt` (e). `isHostileTo` (c): military and civilian humans (never traders), infected, players at humanity 50 or more. Drops: 1 % suicide vest or detonator, 10 % its gun, 10 % ammo, else a bottlecap |
| SoldierEntity (deci/ag/l) | MILITARY, M4A4, shots every 2 to 5 ticks; 10 % medics (`healPlayer`: heal 1, mend a broken leg, 25 % cure infection); hostile to bandits, infected, players under humanity 50; killing one costs 2 humanity |
| SovietEntity (deci/ag/m) | SOVIET; Decimation's `isHostileTo` targets SOVIET humans (Soviets killed each other) and every player: PatchFactions |
| HazmatSoldierEntity (deci/ag/c) | soldier in a hazmat suit, green gun skin |
| InfectedEntity (deci/ag/d) | zombie: melee AI on players, animals, FactionHumanEntity and mechs; variant (data watcher 21: 0 common, 1 military in marine gear, 2 police in NYPD gear) set by the zone spawn handler; horde: 10 or more infected within 20 blocks makes the base speed 0.3 (else 0.25), reset EVERY tick; 8 % of hits infect a player (ServerConfig.enableInfection); drops flesh 70 %, else a cap, 1 in 10000 a gold cap |
| InfectedCrawlerEntity, InfectedDogEntity, FrailEntity, GawkerEntity, HulkEntity, BloaterEntity (deci/ag/f, g, h, i, j, e) | infected kinds (crawler, dog, crying frail, staring gawker, knock up hulk, exploding bloater) |
| DemonEntity (deci/ag/b), MechEntity (deci/ag/k) | flying carrier demon (capped by maxDemonCount); walking mech with machine gun and mortar |
| MobSpawnRegistry (deci/aD/j) | biome spawn lists for every biome: humans as `creature` (Human weight 1 x1-4, Bandit / Soviet / Soldier / Hazmat 1 x1-2), infected as `monster` (Infected 10 x1-4, Crawler, Hulk, Frail 1, InfectedDog 1); CommonProxy removes vanilla monsters |
| EntitySpawnZoneHandler (event) | zone variants (25 % in MILITARY / POLICE), no hostiles in safezones, demon cap: dedicated server only (our fixes/ZoneSpawnHandler) |
| DamageSources.human | NPC gunfire source "human" (not GunDamageSource) |

## 3. An armed NPC's turn, as it runs (call logger trace, 16 Oktober 2026)

Dev test `shots` (153 s, bandits, soldiers and Soviets in the arena), trace prefixes
`deci.ag.a.e,deci.ag.a.eA,deci.ag.a.c,deci.ag.a.d,deci.ag.l.c,deci.ag.m.c,deci.ag.l.k,net.decimation.fixes.NpcShots`.

1. Server thread, every tick per NPC without a target: `findTarget` -> for each living entity in the box: distance,
   `canEntityBeSeen` (a ray), `isHostileTo` -> the first match becomes revenge target and entityToAttack.
2. With a target in sight: `shootAt`. Decimation's own body (75 % direct damage gun.damage / 8, / 1 on infected;
   25 % only a sound) is replaced by our `shotHook` (PatchTracer v2) -> `NpcShots.apply`: spread by tier, a traced
   shot that walls stop (`traceBlocks`, `passes`), no hits on its own side (`sameSide`, `allyInWay`), misses that fly
   on (`missEnd`), rockets for RPG tiers.
3. Counts over the run: SoldierEntity.isHostileTo 330075, BanditEntity.findTarget 27459, BanditEntity.getGun 19350,
   BanditEntity.isHostileTo 13026, BanditEntity.shootAt 10399 (cooldown ticks included), SovietEntity.isHostileTo
   3297, NpcShots.apply 601 real shots, BanditEntity.throwMolotovAt 82. Most isHostileTo calls come from our
   `NpcLoadouts.sniperSearch` (idle snipers scan sniperRange every tick; cheap checks first, the ray last).

## 4. Singleplayer state and known faults

- NPCs spawn and fight in singleplayer; zone variants and safezone spawn blocking needed fixes/ZoneSpawnHandler and
  fixes/ZoneRules (the handlers are ServerProxy only).
- The gun is rolled per side and never synced: the client may draw a different gun (fixed by NpcLoadouts: data
  watcher slot 26).
- `entityToAttack` is never cleared (the last enemy seen stays); only `getAITarget` (revenge, expires) means
  fighting. GunNoise reads that (CLAUDE.md v0.43.3).
- Decimation never reacts to a creative player (every isHostileTo excludes creative).
- PlayerJoinSync (deci.an.e, EntityJoinWorldEvent, both sides) swaps joining mobs by spawning the new one at once:
  1 in 10 infected (not bloaters) become a bloater (3 in 10) or a hulk, 5 in 100 soldiers a mech, an infected in the
  rain a frail (both rolls can fire for one infected); the old one is cancelled and set dead. During a chunk load
  (World.addLoadedEntities walks the chunk's live list) the new mob was added twice: two updates a tick (40 in 20
  ticks, measured) and "Entity is already tracked!". Fixed by fixes/JoinOnce in v0.64.2 (bug.md; dev test
  `doublejoin`). The swaps also roll again for every infected saved in a chunk, at each load.
- InfectedEntity overrides `canEntityBeSeen` but marks it client only (@SideOnly(CLIENT)): a dedicated server uses
  vanilla's check, singleplayer Decimation's (ClientProxy.LineOfSight) even on the server thread [inferred].
- Cost [inferred, not profiled]: findTarget rays every living entity in range before asking isHostileTo, every tick
  per idle armed NPC; FactionHumanEntity walks the whole loaded entity list every tick per human; the infected path
  search and horde scan every tick were cut by PatchInfectedAI (docs/performance.md).

## 5. How our mod uses it

| Need | Use |
|---|---|
| spawn | `Deci.newBandit`, `Deci.newSoldier`, `Deci.newSoviet`, `Deci.newInfected`; retry: Decimation turns some spawns into hulks, bloaters or mechs |
| what is it | `Deci.npcKind`, `Deci.isHuman`, `Deci.isCivilian`, `Deci.isTrader`, `Deci.isArmedTrader`, `Deci.isInfected`, `Deci.isInfectedKind`, `Deci.isPlainInfected`, `Deci.isMech` |
| guns and shots | `Deci.npcGun`, `Deci.setNpcGun`, `Deci.setNpcShotDelay`, `Deci.setNpcShotHook`, `Deci.banditShootAt`, `Deci.sendNpcShot`, fixes/NpcShots |
| who fights whom | `Deci.npcHostileTo`, tools/patches/PatchFactions.java |
| tiers, groups | fixes/NpcLoadouts (config deciworldgen_npc.cfg), fixes/MilitarySpawner, NPC eggs fixes/NpcEgg |
| infected kinds | `Deci.setInfectedVariant`, `Deci.infectedVariant`, fixes/InfectedVariants, fixes/ZombieEgg |
| hearing gunshots | fixes/GunNoise |
| vanilla mobs out | fixes/VanillaMobs (a test needing one sets the KEEP flag) |
| no spawns on their own (tests) | `/gamerule doMobSpawning false`: vanilla stops the biome lists (all of Decimation's own NPC and infected spawns); fixes/NaturalSpawns makes MilitarySpawner, garrison posts and garrison spawner cages follow it; code, eggs and /summon still spawn |

## 6. Checks

- `nospawn`: natural spawns off by the game rule (military spawner, spawner cage, code spawn; 4 checks).
- `python3 tools/devtest.py --live shots` (hits by range, walls, bursts, reloads, rockets), `npc` (tiers, factions),
  `zombies` (variants), `noise`, `npckill`, `tracer`, `firerate`.
- Test NPCs stand on the arena floor (DevTestArena); a held mob's client copy falls.

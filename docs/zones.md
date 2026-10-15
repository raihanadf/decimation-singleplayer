# Zones: how Decimation's zone system works, and what we make work in singleplayer

Read from the decompiled source (deobf/src, readable names; obfuscated names in brackets) on
11 Oktober 2026. Statements about behaviour come from the code; anything only inferred is
marked `[inferred]`. The code that makes zones work in singleplayer is `fixes/ZoneRules.java`
(v0.46.0); safezone sites themselves: section 7.

## 1. Data: a zone is a box with a type

- `ObjectZone` (net.decimation.mod.server.zones): `zoneType` + two corners `zoneX1..zoneZ2`
  (ints, inclusive, any order). `ObjectZoneList.zoneList` holds them.
- `EnumZoneType` (zones.a): SAFEZONE, RADIATION, MILITARY, POLICE, SCARY, BORDER.
- The live list is ONE static field: `ServerConfig.zoneList` (deci.aJ.b.aAc). Every zone check
  in the mod reads it. On a dedicated server, ServerProxy.init loads it from
  `decimation_zones.json` in the game folder (ZoneManager.reloadZones, hk); in singleplayer
  nothing loads it, so it stays null and every check answers "no".
- Our generator keeps its zones per world in `<world>/deciworldgen_zones.json` (ZoneStore,
  v0.7.0) and merges them into that live list, re-adding them when Decimation reloads its file.
  So since v0.7.0 every zone CHECK sees our zones in singleplayer too.
- Admin tools (dedicated server only): `/zone` commands (ZoneCommands: add with the selection
  wand corners, remove at the player, refresh), the selection wand (SelectionToolItem), and in
  creative with the wand the ZoneDebugLayer draws every zone box (safezone cyan, scary red,
  military magenta, police blue, radiation green).

## 2. The checks (ZoneManager, zones.b)

- `isPlayerInZone(player, type)` (a(EntityPlayer, a)): inside the box. For SAFEZONE it answers
  false while the player's combat timer runs (PlayerData.combatTimer > 0): a player who is
  fighting has no safezone protection.
- `isEntityInZone(entity, type)` (a(Entity, a)), `isInfectedInZone` (a(deci.ag.d, a)).
- `isEntityNearZone` / `isPlayerNearZone` / `isInfectedNearZone` (b(...)): the box grown by 10
  blocks on every side (`isPlayerNearZone(p, type, n)` takes the margin, but its body uses 10).
- Coordinates are cast to int (posX truncated toward zero: off by one west / north of 0).

## 3. What each zone type does

### SAFEZONE
Rules in the code, and whether they run in singleplayer (SP) before v0.46.0:
| Rule | Where | SP |
| Infected spawns inside cancelled | EntitySpawnZoneHandler (deci.aK.d, @SideOnly SERVER) | ours since v0.7.0 (ZoneSpawnHandler) |
| Landmines, campfires, punji sticks, barbed wire placed within 10 blocks cancelled | same | no |
| Crossbow bolts, rockets, grenades, mortar rounds spawning within 10 blocks cancelled | same | no |
| Rockets, heat seeking rockets, nuclear rockets, mortar rounds do not explode near a safezone | the entities themselves (isEntityNearZone) | yes |
| A player inside cannot be attacked (any attacker) | TraderAndSafezoneAttackGuard (deci.an.a, both sides) | yes |
| A player inside takes no damage (LivingHurt cancelled) | DamageModifierHandler (both sides) | yes |
| Traders can never be attacked, anywhere | TraderAndSafezoneAttackGuard | yes |
| PvP: a player inside, or attacking from inside, cannot hurt a player; the grace timer | SafezoneAttackHandler (deci.aK.g, SERVER) | no |
| Infected cannot attack a player inside | SafezoneAttackHandler | covered by the guard above |
| A player inside takes no hurt; owned dogs inside take no hurt; clan friendly fire | FriendlyFireHandler (deci.aK.i, SERVER) | player part covered; dogs no |
| Water refilled to 20 every tick inside | PlayerZoneTickHandler (deci.aK.m, SERVER) | no |
| Grace timer: leaving a safezone keeps protection 400 ticks (20 s), counted down with chat warnings, cancelled by moving 30+ blocks in one tick (teleport) | PlayerData.tick / tickMovementAndRecoil (server side code, runs in SP) | the timer runs; only SafezoneAttackHandler reads it |
| Cannot get infected, bleed or break a leg inside | PlayerData.setInfected / setBleeding / setBrokenLeg, only `if side isServer` | no: in SP those setters skip the safezone check |
| No loot from containers inside or within 10 blocks (supply drops inside, and props flagged safezoneLootable, excepted) | LootInteractHandler (registered by ClientProxy) | yes |
| Syringes cannot draw blood inside | EmptySyringeItem | yes |
| Juggernaut armour footsteps silent inside | PlayerData | yes |
| `/rspawn` only inside | Command_RSpawn (server) | no (server command) |
| HUD: "Now entering a Safezone controlled by Nobody" in chat + sound; map markers | IngameHud / MapRenderHelper from the client lists (section 4) | no: the client lists are never sent |

### MILITARY and POLICE
- Infected spawning inside: 25 % get the military (marine set) or police (NYPD) variant
  (EntitySpawnZoneHandler; ours since v0.7.0).
- Map: military zones drawn on the GPS map (client list).
- Our generator tags military sites and bases MILITARY, city streets / police places POLICE, and
  since v0.54.0 each city's wasteland district as one MILITARY box (128 x 128, y 0 to 255; city/
  Wasteland.zone, docs/wasteland.md).

### RADIATION
- PlayerZoneTickHandler (SERVER): inside, not creative, `enableIrradiation` (default true):
  PlayerData.irradiated = true, else false. Within 10 blocks: 5 % per tick the
  `deci:misc.effect.irradiated` sound.
- PlayerData.tick (runs in SP): irradiated and no gas mask: 10 % per tick 1 damage
  (DamageSources.irradiated), smoke particles; with a gas mask a breathing sound. HUD line
  "Irradiated" / "Protected"; client effects in ClientEventHandler.
- In SP nothing sets the flag before v0.46.0, so radiation zones did nothing. We generate no
  RADIATION zones yet (the "Irradiated Military Zone" biome has no effect, `[not decided]`).

### SCARY
- Inside: PlayerData.inScaryZone = true (synced to the client). No reader of that flag was found
  besides the sync `[inferred: unused]`; the debug layer draws scary zones red.

### BORDER
- With `enableWorldBorder` (default false): a player outside every BORDER box takes 4 damage
  per tick. Its box goes to the client as the `text` of the zone list packet (IngameHud shows a
  border warning with ClientStats fields `[inferred]`).

### Location names (turfs, not zones)
- "Now entering X controlled by Y" for named places comes from clan TURFS (TurfManager,
  `decimation_turfs.json`, client list ClientState.bY "x/y/z=x/y/z=name=owner"), dedicated
  server only. Leaving every turf and safezone prints "Now entering the Wilderness". A later
  step can send our own named places (city names, safezone names) this way `[not decided]`.

### Place names (ours, v0.52.0; user choices 12 Oktober 2026)
- What: every city district (the 2 x 2 cell square, 128 x 128 blocks, that shares one style) is
  named "City: District - number", e.g. "Hollowhaven: Downtown - 5"; military bases "FOB Viper" /
  "COP Thunder" / "Camp Sentinel" (controlled by Military). Out of every place: Decimation's own
  "Now entering the Wilderness". Camps and refuges keep Decimation's safezone line.
- How (worldgen/PlaceNames, city/CityNames, NamedPlace): every 40 server ticks per player, the
  districts within 3 of the player's (from the seed: a city is the 4-neighbour group of city regions,
  named from its first region, "... City" from 4 regions; district label from the style: Downtown,
  Hotel District, Uptown, Market District, Suburbs, Old Town, Residential, Wasteland (owner
  Military); number = region index in the city * 4 + quarter), named places within 600 stored in
  StructureData (bases, stored by Slices on their first slice), and Decimation's own clan turfs
  (ServerConfig.turfList) go to the player as the turf list (PacketSetClientStringList,
  Deci.sendTurfList) when it changed. IngameHud then shows its "ENTERING LOCATION" banner, the chat
  line and the newturf sound. Works in singleplayer (integrated server) and on servers, in old worlds
  too (districts follow from the seed; bases only once generated with v0.52).
- Traps: the client strips every space from turf entries (names travel with "_"; the banner turns
  "_" into spaces, PatchTurfNames makes the chat line do the same, three jars; since v0.52.1 also
  the floating name ClanTurfLayer (deci.M.l) draws over the middle of the turf the player stands in,
  at y 130); a turf box inside a
  safezone box makes IngameHud switch between the two messages every frame, so safezone boxes are
  cut out of district boxes (PlaceNames.cut); turf boxes span y 0..255.
- Check: dev test `places` (two neighbouring districts and the wilderness: client list has the
  names, shots docs/shots/place_names_v0.52/; v0.52.1 adds a shot of the floating name over the
  second district's middle); servertest log "place NAME (OWNER) at x,z".

## 4. The client side

- `PacketZoneLists` (deci.aE.a$ax): five string lists (safezone, military, radiation, scary,
  police; each entry "x1/y1/z1=x2/y2/z2") and the border text. Its handler replaces the client
  lists ClientState.bS / bT / bU / bV / bW (deci.b.i) and ca.
- Sent only by ZoneManager.refreshZones (player login and zone commands on a dedicated server),
  and only with the zones of `decimation_zones.json`: our generated zones were never sent, in
  SP or on a server. Cleared on disconnect.
- Readers: IngameHud (safezone enter / leave chat line and sound
  `deci:gui.notification.safezone`), MapRenderHelper (GPS map: radiation, safezone, military
  markers; minimap: safezones), ZoneDebugLayer.

## 5. What v0.46.0 makes work in singleplayer (fixes/ZoneRules.java)

All handlers step aside on a dedicated server where Decimation's own run, except the client
list sync, which runs on both (it sends a superset: the live list holds the file zones too).
1. Player zone tick, a copy of PlayerZoneTickHandler: safezone water, radiation flag and sound,
   scary flag, world border, creative flag.
2. Safezone attacks, a copy of SafezoneAttackHandler (PvP, grace timer, infected) plus OUR rule:
   inside a safezone NOBODY friendly can be hurt: players, traders and guards, civilians
   (FactionHumanEntity that are not bandits), friendly soldiers / survivors, owned pets. Hostiles
   (infected, bandits, Soviets, mechs, vanilla monsters) stay attackable so guards can defend.
3. No provocation (since v0.47.2): a player's hit on a protected one inside a safezone simply does
   nothing. v0.46.0 to v0.47.1 had the guards and soldiers within 64 blocks fire on such a player
   for 60 s; the user removed it (11 Oktober 2026: "its easily accidently hit friendly npc so i
   guess in safezone dont need that feature").
4. Spawns near safezones, the rest of EntitySpawnZoneHandler: traps and projectiles within 10
   blocks cancelled; plus ours: bandits, Soviets and mechs never spawn inside.
5. Safezone status effects the SP setters skip: infection, bleeding and broken legs cleared
   inside (once a second).
6. Client zone lists: every 5 s each player gets the zones within 1024 blocks (only when the set
   changed), so the safezone chat line, sound and map markers work.

## 6. How to test

Dev test mode `zones` (devtest/ZonesTest): builds a safezone and a radiation zone on the test
arena, spawns a trader, a guard, a civilian and a bandit, and checks every rule of section 5
(see its javadoc for the list). Run it as a live or one launch test in deciworldgen_devtest.
Result 11 Oktober 2026 (v0.46.0): 19 checks PASS (spawns refused inside, water, bleeding,
radiation flag and damage, hits on a civilian by a bandit and by the player do nothing, the
player provoked, the guard targets and hurts them, a bandit inside can be hurt without
provoking, the guard shoots a bandit outside, grace timer, the client got the safezone box).
Since v0.47.2 (no provocation): the player's shot does nothing and nothing follows (the guard
ignores the player, who keeps the protection and stays unhurt); 16 checks PASS. The radiation
check zeroes the safezone grace first (it blocks every attack on the player, radiation too).
Not covered by the test: traps and projectiles near a safezone `[not verified]`, the chat line
and map markers drawn from the client list `[inferred from IngameHud / MapRenderHelper]`.
Test trap: a condition the player already has stays (the rule stops new ones only); the dev
player was bleeding from earlier runs.

Run time (call logger, 16 Oktober 2026, docs/decimation_code/README.md): the `zones` test traced
`net.decimation.mod.server.zones,net.decimation.fixes.ZoneRules` recorded 11.7 million calls in 24 s:
ZoneManager.isBetween 9914390, isBetweenWithMargin 1693156, EnumZoneType.valueOf 86742, against only 1638
isPlayerInZone and 571 isInfectedInZone questions. Every zone question walks ALL zones of the world (4435 in the dev
world, more as the world is explored: our worldgen adds one per site and district) and turns each zone's type string
into the enum again. Per call cheap; the cost grows with the zone count [measured calls, time not profiled]. A
spatial index (zones by region) would make it constant. ZoneRules itself: onPlayerTick 1598, onServerTick 898,
onLivingAttack 357, near 22183. Decimation's PlayerZoneTickHandler and SafezoneAttackHandler: 0 calls (server proxy
only, docs/decimation_code/singleplayer.md). Two timing checks (water refill, bleeding) FAILED under that trace and
PASS without it: the recording overhead, not the code.

## 7. Safezone sites (docs/safezone.md)

Survivor camps outside the cities since v0.47.0 (walled, market of traders, guards, civilians,
armed survivors; residents kept by worldgen/SafezoneSites); city refuges since v0.48.0 (our
fortified building on one block per city region). The SAFEZONE box is the camp +6 blocks, the
refuge's block +2 (loot containers within 10 blocks of a safezone give nothing).

## 8. Safezones destroyed by the player (plan only, user 11 Oktober 2026: "later i figure it out")

Not built. Ideas to decide with the user:
- What counts as destroyed: every guard dead, or a "control point" (the camp's radio / flag
  pole) held for N seconds, or the trader leader killed.
- Guards must be killable for this: a safezone under attack drops its protection (a
  "breached" state: the SAFEZONE box is removed from the live list while the fight lasts, so
  Decimation's checks and ours stop protecting), triggered by a deliberate act (today shots at
  protected NPCs do nothing, and since v0.47.2 nothing follows them: the user removed the
  provocation because friendlies get hit by accident; an attack must be clearly intended, for
  example an explosive at the gate or a held key).
- After the fall: the box removed from deciworldgen_zones.json for good (ZoneStore.remove),
  traders gone or hostile, loot unlocked (the 10 block no loot margin goes with the zone),
  humanity loss like killing civilians, bandits may move in (zone becomes a bandit camp,
  roadmap "Advanced claims").
- Retake: the player could later claim it (roadmap "Zone areas claimable by the player").
- Persistence: a per world record of fallen safezones in StructureData so regeneration never
  brings one back.

# Decimation code: sides, proxies, network and the singleplayer gaps

Part of the feature study of Decimation's own code (16 Oktober 2026, index docs/decimation_code/README.md). The root
cause pattern in CLAUDE.md ("this mod was built assuming not a dedicated server means just a client") is made
concrete here: every handler, where it is registered, and what replaces it in singleplayer.

## 1. Two rules that decide what runs

1. A handler registered only in ServerProxy (deci/a/e) never exists in singleplayer: the integrated server runs in the
   CLIENT JVM, where FML loads ClientProxy (deci/a/c), not ServerProxy.
2. A method marked `@SideOnly(Side.SERVER)` is stripped from the class in the client JVM, so even a handler
   registered everywhere does nothing in singleplayer if its event method carries it (PlayerLoginHandler is
   registered by the main class AND ServerProxy, and still never runs in singleplayer). `isServer()` checks inside a
   method (DamageModifierHandler, RespawnPointHandler, LootInteractHandler, ItemPickupHandler) switch single
   branches off the same way.

## 2. Lifecycle (DecimationMod, deci/a/b)

- `preInit`: registries in order: blocks, items, armor materials, recipes (RecipeRegistry), tile entities, skins,
  network messages (NetworkMessageRegistry), entities, biome spawns (MobSpawnRegistry), sounds, loot table; then the
  15 common handlers (section 3).
- `init` / `postInit`: the proxy's init; postInit connects to BoehMod's dead backend (kryonet, 5 s timeout: our
  PatchBackend stops launch waiting for it).
- `serverStarted`: registers `/deci` (Handler_Commands) on every side. `serverStarting`: the nine admin command sets
  (Command_Server, ScreenshotCommand, TraderCommands, ZoneCommands, TurfCommands, Command_RSpawn, Command_BottleCaps,
  Command_Supporter, ClanCommands) only when `isServer()`: none of them exist in singleplayer.
- `serverStopping`: saves clans only on a dedicated server.
- `getServerProxy` (`deci.a.b.d()`) returns the proxy as ServerProxy, or null on the client side (the original
  hard cast threw there; an early one off patch of ours, "proxy cast", made it return null). Every caller must cope
  with null; LootInteractHandler does (no turf rules then).

## 3. Every event handler

Registered in: main class = DecimationMod (both sides); ClientProxy; ServerProxy. Side = the @SideOnly of the event
methods. Run time column from the call logger: dev tests `zombies` + `checks` (55 s, kills, bottlecap pickup, supply
drop, damage, zones) traced `deci.aK,deci.an,deci.aL` (16 Oktober 2026).

| Handler | Registered in | Event | Side | Singleplayer | Ours in its place |
|---|---|---|---|---|---|
| TraderAndSafezoneAttackGuard | main class | LivingAttackEvent | both | runs (1239 calls) | |
| PlayerDataAttacher | main class | EntityConstructing | both | runs | |
| VehicleSeatDamageGuard | main class | LivingHurtEvent | both | runs | |
| ItemEntityClickPickup | main class | EntityInteractEvent | both | runs [not seen in this trace] | |
| PlayerJoinSync | main class | EntityJoinWorldEvent | both | runs | |
| DeathHandler | main class | LivingDeathEvent | both | runs (NPC humanity on kill) | fixes/HumanityKillHandler adds the isServer gated rules |
| BottlecapTossGuard | main class | ItemTossEvent | both | runs [not seen] | |
| DamageModifierHandler | main class | LivingHurtEvent | both, isServer() inside | runs, server branch off | |
| ClearCommandHandler | main class | CommandEvent | both | runs [not seen] | |
| PlayerCloneHandler | main class | PlayerEvent.Clone | both | runs [not seen] | |
| RespawnPointHandler | main class | PlayerRespawnEvent | both, isServer() inside | runs, server branch off | |
| PlayerTickHandler | main class | PlayerTickEvent | both | runs | |
| PlayerTrackingSync | main class | StartTracking | both | runs | |
| DayLengthHandler | main class | WorldTickEvent | both | runs | |
| PlayerLoginHandler | main class + ServerProxy | PlayerLoggedInEvent | SERVER | never (stripped); twice per login on a dedicated server [inferred] | none (supporter check, clan / zone / turf reload, combat log kill) |
| LootInteractHandler | ClientProxy + ServerProxy | PlayerInteractEvent | both, isServer() inside | runs (our early patch put it in ClientProxy) | |
| ClientProxy.deciDrainLootTaskQueue | ClientProxy | ClientTickEvent | both | runs: drains the SERVER task queue on the client thread (loot.md section 4) | |
| BlockBreakProtectionHandler, BlockPlaceProtectionHandler | ServerProxy | BreakEvent, PlaceEvent | SERVER | never | none (block break / place config, turf rules) |
| HorseInteractBlocker | ServerProxy | EntityInteractEvent | SERVER | never | none (fixes/VanillaMobs keeps horses out anyway) |
| EntitySpawnZoneHandler | ServerProxy | EntityJoinWorldEvent | SERVER | never (0 calls) | fixes/ZoneSpawnHandler |
| ItemPickupHandler | ServerProxy | EntityItemPickupEvent | SERVER | never (0 calls while caps were picked up) | fixes/BottlecapHandler |
| SpectatorItemTossHandler | ServerProxy | ItemTossEvent | SERVER | never | none needed |
| SafezoneAttackHandler | ServerProxy | LivingAttackEvent | SERVER | never | fixes/ZoneRules |
| DeathStatsHandler | ServerProxy | LivingDeathEvent | SERVER | never | fixes/LocalStats (counters), bounties none |
| FriendlyFireHandler | ServerProxy | LivingHurtEvent | SERVER | never | none (clans only on servers) |
| NoDropsHandler | ServerProxy | LivingDropsEvent | SERVER | never: a destroyed vehicle drops its items in singleplayer [inferred] | none |
| PlayerZoneTickHandler | ServerProxy | PlayerTickEvent | SERVER | never (0 calls during the zone checks) | fixes/ZoneRules (zone flags, radiation, border) |
| ChatHandler | ServerProxy | ServerChatEvent | SERVER | never | none (radio and clan chat) |
| ServerCommandRegistrar | ServerProxy | server start | | never | none |
| ServerTickHandler | ServerProxy | ServerTickEvent | SERVER | never | task queue: ClientProxy.deciDrainLootTaskQueue; supply drops: fixes/SupplyDropScheduler; dynamic weather, anti dupe sweep, trader restock, backend reconnect: none |
| ServerStopHandler | ServerProxy | server stop | | never | none |

LegacyHandlerUnused (deci/aL/a) is registered nowhere (0 calls); ClientEventHandler, ClientRenderHandler,
WorldRenderLayerDispatcher and PlayerModelSwapper are client drawing and input (ClientProxy).

## 4. Network

One SimpleNetworkWrapper, channel "ctx" (NetworkChannel deci/aF/a$a$a, `gB` getChannel); NetworkMessageRegistry
(deci/aD/n) registers 75 messages on both sides, the Side argument being the side that RECEIVES:

- to the server (28): PlayerAction, LockpickUnlock, LockpickAttempt, TraderTransaction, PlaceBounty, PickupItem,
  ReopenLootGui, SetStance, SetTyping, ClimbState, SetAimMode, ThrowItem, ApplyPoison, FlashbangDetonate,
  SetCosmetics, RenameBackpack, GunFire, Reload, GunHit, FireProjectileWeapon, BlockHitParticles,
  ScreenshotChunkToServer, SetPropRotationServer, SetTileTextServer, SetSignTextServer, SetDecalServer, ModHashes,
  VehicleInput;
- to the client (47): player data syncs (SyncPlayerData, SyncPlayerDataPublic, SyncExtraArmor, SyncPlayerSlot,
  ClearPlayerSlot, BackpackContents), sounds and effects (PlaySound, DeathSound, HurtSound, ExplosionSound,
  ExplosionShake, Flashbang, NukeFlash, JetParticle, SpawnVanillaParticle, SpawnDeciParticle, BulletTracer,
  GunFireEffects, HitMarker, BulletRicochet), state (LootInventory, ZoneLists, SetWeather, ServerTickTime,
  ClientStats, ClanInfo, ClearClanInfo, SetClientStringList, SetClientBoundsA / B), messages (ShowNotification,
  AddClientMessage, ClearClientMessages, CopyToClipboard), props and tiles (SetPropRotationClient,
  SetTileTextClient, SetSignTextClient, SetTileRotationClient, SetDecalClient), admin (RequestScreenshot,
  ScreenshotChunkToClient, RequestModHashes, RequestCosmetics), music (ToggleStereo, StopStereo), LockpickResult,
  VehicleYawClient.

Messages are classes `X` inside NetworkMessages (deci/aE/a) with handler `X$a`; per message details: the feature
docs (loot, weapons). The packets work in singleplayer: client and integrated server talk through the same channel.

## 5. How to add or fix something for singleplayer

- A rule Decimation only runs on a dedicated server: copy its logic into a handler of ours registered on both sides,
  which steps aside when `FMLCommonHandler.instance().getSide().isServer()` (the original still runs there). Pattern:
  fixes/BottlecapHandler, fixes/ZoneRules, fixes/HumanityKillHandler, fixes/SupplyDropScheduler.
- Never register a handler whose event method is `@SideOnly(SERVER)` and expect it to run in singleplayer.
- Reach Decimation's classes through `Deci`; send its packets with `Deci.sendNpcShot`, `Deci.setLootInventory` and
  the like rather than building them in our code.

## 6. Checks

- `python3 tools/devtest.py --live zombies checks '-Ptrace=deci.aK,deci.an,deci.aL'`: which handlers fire (the table's
  run time column); the checks themselves test our replacements (bottlecaps, supply drop, zones, humanity).
- `python3 tools/servertest.py 1`: the dedicated server path, where ServerProxy's handlers do run.

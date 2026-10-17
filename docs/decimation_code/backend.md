# Decimation code: BoehMod backend, update checker and outside connections

Part of the feature study of Decimation's own code (16 Oktober 2026, index docs/decimation_code/README.md). Our own
update system: new_feature.md "auto update", fixes/Updater.

## 1. What it is for

Decimation was an online mod: a BoehMod network server (kryonet) kept profiles, global stats, news, the server list,
supporter status and capes; the game checked GitHub for updates and BoehMod's site for a resource pack. All of those
servers are dead. Decimation still tries them.

## 2. Code map

| Class (obf) | Job |
|---|---|
| BackendClient (deci/aP/a) | kryonet Client `aBX`; `connect` (e) registers the message classes, starts `new Thread(aBX)` and connects to network.mcdecimation.net TCP 54557 / UDP 54779 |
| BackendClientListener / BackendServerListener | answers from the backend (profiles, articles, server list, player counts) |
| ClientEventHandler reconnect (client tick, every 300 ticks = 15 s) | `new Thread(aBX).start(); aBX.reconnect()`: a NEW kryonet thread on every try |
| ServerTickHandler reconnect (dedicated server), BackendServerList (deci/aO/a), OfficialServerListRegistrar (deci/aM/a) | the same on servers; player count polls; adding the server to the official list |
| DeathStatsHandler, PlayerLoginHandler, Command_Supporter | kills, deaths, supporter checks sent to the backend (dedicated server only) |
| UpdateChecker (net/decimation/mod/common/utils/h) | latestversion.txt from raw.githubusercontent.com (404 now), resource pack download from GitHub; `isUpdateRequired` (our PatchNoUpdate answers it) |
| cape groups (net/decimation/mod/client/cape/a), RenderUtil.loadSkin, ImageElement, TileEntityDecalImage | capes JSON, player skins from crafatar.com, menu images and picture decals from URLs |
| UuidFetcher (deci/aN/c) | player names to UUIDs through api.mojang.com / mcapi.ca |
| DiscordPresence (deci/D/a) | Discord rich presence (off on macOS and by config) |
| anticheat (PacketRequestModHashes / ModHashes) | mod file hashes on login (dedicated server only) |

Hosts named in the code: network.mcdecimation.net, eu. / us.mcdecimation.net, www.mcdecimation.net, www.boehmod.com,
www.boehmod.net, www.boehmodhosting.com, boehmod.buycraft.net, github.com, raw.githubusercontent.com, crafatar.com,
api.mojang.com, mcapi.ca, discord, twitter.

## 3. As it runs (the live test game's own log and thread dump, 16 Oktober 2026)

- Launch: "Registering new client network connection", connect to network.mcdecimation.net:54557 fails in about
  1 s (PatchBackend: 300 ms timeout instead of 5 s); "BoehMod - Decimation Initialized and Loaded in: 6 Second(s)".
- From then on every 15 s: "Attempting reconnection to Network...", "Failed to Connect to Network!", each from a new
  Thread-N (Thread-15, 19, 23, ... 92 within 6 minutes).
- Thread dump of the running game: 27 threads blocked forever in kryonet `Client.run` -> `Client.update` (waiting on
  the one selector) at 03:56:48, 28 at 03:57:09 after 7 min 10 s uptime: ONE LEAKED THREAD EVERY 15 SECONDS, about
  240 an hour, for the whole session (bug.md "Backend reconnect leaks a thread every 15 seconds").
- The host network.mcdecimation.net resolved to 37.48.77.81 in an earlier run and to 212.92.104.119 in this one:
  somebody answers DNS for it. If a server there accepted the connection, Decimation would send it its requests and
  deserialize whatever it sends back (kryonet with Decimation's registered classes) [risk inferred, never seen].
- The update check logs FileNotFoundException for latestversion.txt (repository gone); harmless.

## 3b. The anticheat on a dedicated server (seen 17 Oktober 2026, release 0.59.0 on Forge 1.7.10-10.13.4.1614)

- `AntiCheatScanner.collectModHashes` hashes (SHA-256) every file in `mods/1.7.10/` when that folder exists, else every
  file in `mods/`; a server skips names containing "dynmap". The server logged "[BoehMod-Anticheat] Detected hash
  '9e20395d...' (Decimation.jar)": the same sha256 as the GitHub release asset.
- On login `PlayerLoginHandler` sends RequestModHashes (to every player, when `allowAdditionalMods` is false: the
  default that `decimation_server.properties` gets on the first start); the client answers ModHashes with its own list.
- `PacketModHashesHandler.handle` kicks when a received hash is neither the server's nor one of 11 whitelisted hashes,
  or when the client sent none while the server has some; players in the ops list are never kicked. Message: "BoehMod
  Anticheat / You've been kicked for having modified files! / New update Out? / You can force-update from the options
  menu".
- Seen: the dev client (an EMPTY `mods/1.7.10`, so no hashes) was kicked a second after logging in ("Did not receive any
  hashes from player Developer!"); with one identical small file in the server's `mods` and the client's `mods/1.7.10` it
  stayed in the server's world (dev test `guideshots`). A player with exactly the server's Decimation.jar passes the same
  way; another version or any extra mod is an unknown hash [the hash path read in code; a second real client not tried].
- The same file sets Decimation's other server rules: `enableBlockBreaking` / `enableBlockPlacing` false
  (BlockBreakProtectionHandler, BlockPlaceProtectionHandler: "You cannot break blocks on this server!", fortification
  blocks and creative players excepted), `enableItemPickup` false (ItemPickupHandler cancels pickups, bottlecaps still
  count) [code read, not tried in game]. The site's server guide (/docs/server) lists them under Problems.

## 4. Singleplayer state and known faults

- Thread leak above: FIXED v0.62.8 by PatchBackend v2 (no BoehMod connection at all: 0 kryonet threads after 2 minutes,
  bug.md).
- Profile, stats, news, server lists, supporter capes: empty; our LocalStats fills stats.
- Every outside call fails or returns nothing; none blocks the game since PatchBackend (launch) and our own updater.

## 5. How our mod uses it

| Need | Use |
|---|---|
| no BoehMod connection, no reconnect threads | tools/patches/PatchBackend.java v2 (v1: 300 ms timeout) |
| "Play" without the voice chat jar, our releases | tools/patches/PatchNoUpdate.java, fixes/Updater, fixes/UpdateProgress, fixes/UpdateScreen |
| stats without the backend | fixes/LocalStats, PatchMenuStats |

## 6. Checks

- Thread leak: `jcmd <pid> Thread.print` on a running game, count threads in `kryonet.Client.run`: 0 since v0.62.8
  (was one more every 15 s); the log says "BoehMod network is off" once.
- `python3 tools/devtest.py --live menu` (no forced update), `update` (our updater against
  tools/fake_release_server.py).
- Anticheat and joining a server: a Forge 1.7.10 server in the scratchpad with the release Decimation.jar, then
  `python3 tools/devtest.py --live guideshots -Pserver=localhost` (join result in the results; docs/website.md).

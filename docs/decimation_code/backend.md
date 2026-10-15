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

## 4. Singleplayer state and known faults

- Thread leak above (client side; the dedicated server's reconnect has the same pattern).
- Profile, stats, news, server lists, supporter capes: empty; our LocalStats fills stats.
- Every outside call fails or returns nothing; none blocks the game since PatchBackend (launch) and our own updater.

## 5. How our mod uses it

| Need | Use |
|---|---|
| no 5 s launch wait | tools/patches/PatchBackend.java (timeout 300 ms; the reconnect loop is untouched) |
| "Play" without the voice chat jar, our releases | tools/patches/PatchNoUpdate.java, fixes/Updater, fixes/UpdateScreen |
| stats without the backend | fixes/LocalStats, PatchMenuStats |

## 6. Checks

- Thread leak: `jcmd <pid> Thread.print` on a running game, count threads in `kryonet.Client.run`; grows by one every
  15 s today.
- `python3 tools/devtest.py --live menu` (no forced update), `update` (our updater against
  tools/fake_release_server.py).

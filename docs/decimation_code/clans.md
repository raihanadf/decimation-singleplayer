# Decimation code: clans and turfs

Part of the feature study of Decimation's own code (16 Oktober 2026, index docs/decimation_code/README.md). Package
notes: deobf/notes/architecture.md "net/decimation/mod/server/zones and turf" and "... clans".

## 1. What it is for

On Decimation's servers players formed clans, captured named turfs (areas) and earned money from them; turf rules
limited building and opening crates in another clan's turf. All of it is dedicated server code. In singleplayer only
the client side turf list survives, and our place names ride on it (docs/zones.md "Place names").

## 2. Code map

| Class (obf) | Job |
|---|---|
| ClanManagerV1 (net/decimation/mod/server/clans/a) | the live clan system: clans/NAME.json files plus userData.json; `tick` pays 20 caps per owned turf every 18000 ticks; lookups by PlayerData's clan name. `isMemberOnline` compares UUIDs with != (always false). The v2 ClanManager / Clan classes are a rewrite that is not wired in |
| ObjectClan, ObjectMember, ObjectUserData, EnumClanRank | clan file data |
| ClanCommands | /clan: dedicated server only (DecimationMod.serverStarting) |
| TurfManager (net/decimation/mod/server/turf/a) | decimation_turfs.json: capture, ownership, `canPlayerUseInTurf`; re-reads the JSON from disk on every call (block break / place, crate opens on a server); `getTurfById` compares UUIDs with != (never matches); `canPlayerUseInTurf` checks the player's position, not the block, and refuses clanless players |
| ObjectTurf, ObjectTurfList, TurfCommands | turf data, /turf (dedicated server only) |
| ClanTurfLayer (deci/M/l) | client: the floating turf name over each turf, turf boxes for the selection tool in creative |
| IngameHud turf banner | client: "entering" banner, chat line and sound when the player crosses into a turf of the client's list |
| FriendlyFireHandler, BlockBreak / BlockPlaceProtectionHandler, ChatHandler "#" clan chat | server proxy only |

## 3. As it runs in singleplayer (call logger, dev test `places`, 16 Oktober 2026)

Traced `net.decimation.mod.server.clans,net.decimation.mod.server.turf,deci.M.l,net.decimation.worldgen.PlaceNames`:
clan and turf server classes 0 calls; our PlaceNames.onServerTick 844 (sends the districts and bases near the
player as Decimation's turf list); ClanTurfLayer.render 518 (once a frame) and its renderEntity 19713 (once per loaded
entity per frame); the client's turf list held the district names (checks PASS).

## 4. Singleplayer state

- No clans, no turf capture, no turf income, no clan chat, no /clan or /turf in singleplayer.
- Decimation's turf client parts work when something fills the client's turf list: worldgen/PlaceNames does, with
  our districts and named bases; PatchTurfNames shows "_" as spaces in chat and over the turf.
- Anything that asks "is the player in a clan" gets no clan; Decimation's humanity on player kills goes to the
  clan only on a server (fixes/HumanityKillHandler replicates the no clan branch).

## 5. How our mod uses it

| Need | Use |
|---|---|
| place name banners | worldgen/PlaceNames (Decimation's turf list as the carrier), docs/zones.md "Place names" |
| spaces in turf names | tools/patches/PatchTurfNames.java (chat and the floating layer) |

## 6. Checks

- `python3 tools/devtest.py --live places`: the client's turf list gets the district names.
- Clans and turfs themselves: only on a dedicated server (`python3 tools/servertest.py`); no test drives them.

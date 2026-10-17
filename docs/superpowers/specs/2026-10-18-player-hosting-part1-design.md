# Player hosting, part 1: host online through our relay, join from the Community page

Date: 18 Oktober 2026. Status: design approved in the browser (user: "screens good, write the spec"); this spec waits
for the user's review. Background, measurements and every earlier decision: docs/player_hosting.md sections 1 to 9.
Mockups: .superpowers/brainstorm/99330-1792267404/content/part1-design.html (local only).

## 1. Goal

A player without a server hosts the world they are playing from their own game. It appears on Decimation's Community
page for everyone, with its mods. Anyone with the same mods joins it with one click. Nothing costs money.

Success looks like: a player on another computer, on another network, finds the world on the Community page, joins,
and plays (loot, vehicles, traders, guns, NPCs) with the host; the host never touched their router.

## 2. Decisions this spec builds on (user)

| Decision | When |
|---|---|
| The host picks who can join: any account (offline mode) or Mojang accounts only (online mode) | 18 Oktober 01:09 |
| No money: free services only; a donation may later pay for a server, never expected | 18 Oktober 01:09 |
| A public list for everyone; no passwords | 18 Oktober 01:09, 03:02 |
| The list shows each world's mods | 18 Oktober 02:14 |
| No card, so Oracle is out: Cloudflare (relay and list) now, playit.gg as the second option | 18 Oktober 02:20 |
| Four parts; this is part 1 (core, end to end) | 18 Oktober 03:02 |
| Approach A: our own Cloudflare relay and list; B (playit addresses) stays the fallback if A is down | 18 Oktober 03:14 |
| The screens of the mockup | 18 Oktober 03:14 |

Out of part 1: direct connections and playit addresses (part 2); the list on the website, kick, ban, report (part 3);
hole punching (part 4); passwords; a picture per world.

## 3. Architecture

Three pieces, joined by secure WebSockets (WSS):
- the HOST's game (our mod): the LAN world, HostService (heartbeat, control socket, one data socket per joiner);
- Cloudflare (free plan): the Worker "deciremnant-relay" with the Durable Objects Lobby (the list, one) and Room (one
  per hosted world);
- the JOINER's game (our mod): JoinService (a local port 127.0.0.1:N that Minecraft connects to), the list fetch.

### 3.1 Units and their jobs

| Unit | Where | Job | Talks to |
|---|---|---|---|
| Relay Worker | new folder `relay/` in the mod repo (TypeScript, wrangler; no Decimation content, public is fine) | routes `/list`, `/host/...`, `/join/...` to the Durable Objects | Lobby, Room |
| Lobby (Durable Object, one) | relay | the public list: keeps entries in memory and in its storage, drops an entry 3 min after its last heartbeat, answers the list as JSON | Worker |
| Room (Durable Object, one per hosted world) | relay | pairs each joiner's socket with a data socket from the host, forwards binary messages both ways, counts players | Worker |
| `hosting/HostService` | our mod (`net.decimation.fixes.hosting`) | opens the world to LAN with the chosen online mode, registers with the list, heartbeats, holds the control socket, opens one data socket per joiner and pipes it to the LAN port | Lobby, Room, IntegratedServer |
| `hosting/JoinService` | our mod | checks mods and version against a list entry, opens a local port, pipes each Minecraft connection to the Room, starts Minecraft's connect screen on that port, one retry on Forge's handshake timeout | Room |
| `hosting/RelaySocket` | our mod | a WebSocket client (TLS) on the netty 4.0.10 that Minecraft 1.7.10 ships; writes coalesced per 50 ms (one game tick) to keep the message count down | Worker |
| `hosting/HostScreen` | our mod | the Host Online page (mockup section 3): name, who can join, max players, mods (read only), connection line, Start / Stop, players with ping | HostService |
| `hosting/HostMenu` | our mod | pause menu: Host Online >> in Server Status' place (green while hosting); the globe (LAN) icon opens the same page | Forge GuiScreenEvent, Deci |
| `hosting/CommunityList` | our mod | fetches the list, fills Decimation's Community page (GuiMenuCommunityServers) with rows, shows a selected row's mods (missing ones red), greys other versions, hands Join to JoinService; the tab tooltip and the "Players Online" line count real worlds | Lobby, Deci |
| `hosting/ModSet` | our mod | the networked mods of this game (FML's mod list without mcp, FML, Forge) as `[id, version]`; compares two sets | FML Loader |

Every obfuscated Decimation name goes through `fixes/Deci` (rule in CLAUDE.md).

### 3.2 Relay interface (HTTP and WebSocket, JSON in text frames, game bytes in binary frames)

| Call | Who | Body / frames | Answer |
|---|---|---|---|
| `POST /list/host` | host | `{name, host, online, max, mods, version}` | `{room, secret}` (new room) |
| `POST /list/beat` | host, every 60 s | `{room, secret, players}` | `{ok}` or `{gone}` (re-register) |
| `POST /list/stop` | host | `{room, secret}` | `{ok}` |
| `GET /list` | anyone | | `[{room, name, host, online, players, max, mods, version, route:"relay", since}]` |
| `WSS /host/{room}?secret=` | host, control | relay sends `{"open": id}` per joiner, `{"close": id}` | |
| `WSS /host/{room}/data/{id}?secret=` | host, one per joiner | binary game bytes | |
| `WSS /join/{room}` | joiner | binary game bytes | closed with a reason text when the room is gone or full |

`room` is random (not guessable), `secret` is only known to the host. A room refuses joins beyond `max`. A joiner
socket with no matching host data socket within 10 s is closed ("host did not answer").

### 3.3 Data in the list entry

`name` (the host's text, 32 characters at most, no formatting codes), `host` (player name), `online` (true = Mojang
accounts only), `players` / `max` (2 to 8), `mods` (`[[id, version], ...]`), `version` (DeciRemnant version), `route`
("relay" in part 1), `since` (start time). Nothing else; no addresses.

## 4. Flows

### 4.1 Host
1. Pause menu, Host Online >> (or the globe icon): HostScreen.
2. Start hosting: `IntegratedServer.setOnlineMode(choice)`, `shareToLAN` (game mode as the world, cheats off), then
   `POST /list/host`, the control socket, heartbeats every 60 s.
3. Per `{"open": id}`: a data socket to the Room and a TCP socket to the LAN port, piped both ways.
4. Stop hosting, leaving the world or quitting: `POST /list/stop`, sockets closed. The LAN port stays open until the
   world closes (1.7.10 cannot close it again); with the list entry gone nobody can reach it through the relay.

### 4.2 Join
1. Community tab: `GET /list` (also on Refresh List), rows filled.
2. Join: ModSet compares; a missing or different mod, or another DeciRemnant version, shows a toast naming it, no
   connection.
3. JoinService opens 127.0.0.1 on a free port, starts Minecraft's connect screen to it; each accepted connection gets
   a `WSS /join/{room}` and is piped.
4. Forge's handshake times out ("Timeout waiting for client thread to catch up!", seen in the spike): one retry.

## 5. Errors and limits (what the player sees)

| Case | Seen as |
|---|---|
| host gone (room closed, entry dropped) | toast "The host stopped hosting"; the row goes on the next refresh |
| room full | toast "This world is full (4/4)" |
| Cloudflare's free daily limit reached (any 4xx / quota error from the Worker) | host: "The free relay is full until tomorrow 07:00 WIB" (00:00 UTC); joiner: the same; option B text: "Ask the host for a playit address" (part 2) |
| list unreachable | Community page: "Community list not reachable, try Refresh" (instead of rows) |
| Mojang accounts only, joiner offline | Minecraft's own refusal message (no change) |
| mods differ | toast naming the first missing mod and version |

## 6. Privacy and abuse

- Players never see the host's address; the relay sees it, keeps no logs of game bytes.
- The list shows only what the host typed and the host's player name.
- Only a registered room (heartbeat younger than 3 min) accepts sockets; one host registration per address per minute;
  names longer than 32 characters or with formatting codes are refused.

## 7. Free plan budget (to check in the plan's first tasks)

Cloudflare Durable Objects free plan: 100,000 requests a day, incoming WebSocket messages counted 20 to 1 (official
pricing page, 18 Oktober 2026). With writes coalesced per 50 ms, one player is at most 20 incoming messages a second per
direction [inferred], so roughly 14 to 28 play hours a day for everyone together. The plan measures the real message
rate in the local test first and writes the number into docs/player_hosting.md.

## 8. Testing

1. Relay: unit tests with wrangler's local runtime (vitest): register, beat, drop after 3 min, list JSON, room pairing,
   full room, wrong secret, closed host. Each test fails before its code exists.
2. Mod, local (this Mac): the existing dev tests `lanhost scene=true` and `lanjoin` run against `wrangler dev` (a local
   relay) instead of tools/hostspike, then against the deployed relay. New checks: the row appears with its mods, a
   missing mod is refused with its name, Stop hosting removes the row, a full room refuses.
3. Real case (user, 18 Oktober 2026: the brother's Linux laptop on Tailscale): the joiner runs on that laptop on another
   network and joins through the DEPLOYED relay, so the bytes cross the internet. What it needs, asked at test time: the
   brother's agreement, Java 8 and the release jar on the laptop, and how it is driven (the brother clicks, or a shell
   over Tailscale if the user allows it). Measured: ping, join time, bytes and messages per minute.
4. The full live suite still passes (zombies, checks, places, zones, loot, drive, trade), mainmenu and menupages last.

## 9. Risks to settle first (plan task 1, a small spike each)

1. netty 4.0.10's WebSocket client with TLS to Cloudflare on Java 8 (also Mojang's bundled 8u51, older root
   certificates) [not verified]. If it fails: a small shaded WebSocket library [to pick in the plan].
2. Building our pages on Decimation's own GuiMenuBase (deci) so the left column and card match, or drawing our own
   screen in its style [to decide in the plan by trying].
3. Decimation's Community rows: how a click and a double click join (GuiMenuCommunityServers, ListElement) so Join
   reaches JoinService [read in code first].
4. The user creates the Cloudflare account and runs `npx wrangler login` themselves; deploy keys never pass through
   the chat or a file in a repo.

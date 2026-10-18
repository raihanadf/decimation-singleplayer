# Player hosted worlds, listed as community servers (analysis, 18 Oktober 2026)

User idea (docs/ideas.md item 22): a player without a server hosts from their own computer; our server only bridges
and lists it on Decimation's Community page. User: "this is great potential to make the mods go back alive again ...
try analyze more deeply". NOT DESIGNED, nothing built. This file is the analysis to decide from.

## 1. Facts checked in code (Minecraft 1.7.10 sources, dev/build/rfg/minecraft-src)

- A singleplayer world is a real server inside the game (IntegratedServer). "Open to LAN" (`shareToLAN`) binds it on
  all interfaces at a free port (HttpUtil, fallback 25564) and announces it on the LAN (ThreadLanServerPing).
- The integrated server ALWAYS runs in online mode (`startServer`: `setOnlineMode(true)`): every joining player is
  checked with Mojang's session server. Players on unofficial (cracked) launchers cannot join a LAN world unless our
  mod turns online mode off. This decides who can play (section 5, question 1).
- The client's first packet (C00Handshake, GuiConnecting) carries the address the player typed, up to 255 characters.
  A relay can read it and route many worlds over ONE port (as Velocity / BungeeCord virtual hosts do), for example a
  join code inside the name. Since every player has our mod, the Community page can also put the code there itself.
- After the handshake, login is encrypted between the player and the host (Mojang's key exchange): a relay passing
  bytes through cannot read or change the game.
- 1.7.10 parses IPv6 addresses in brackets (ServerAddress `[addr]:port`), so a direct IPv6 join is possible
  [not tried; whether the launcher forces IPv4 with java.net.preferIPv4Stack is not checked].
- Decimation's anticheat (mod hashes, docs/decimation_code/backend.md 3b) and its other server rules live in the
  ServerProxy handlers, which a LAN world does not load [inferred from the proxy split]; FML still compares mod lists.
- Our own code has no single player assumption on the server side (only ScopeZoom, SightPlacement, SightReticle read
  `thePlayer`, all client drawing). Whether DECIMATION's systems behave with two or more players on an integrated
  server was never tested: the biggest unknown (section 4, step 1).

## 2. Outside facts (web, 18 Oktober 2026; secondary sources, check before relying)

- e4mc: host only mod, LAN world gets a public domain through a relay (outbound, no port forwarding); modern versions.
  e4mc Retro backports it; its CurseForge page shows 1.12.2 as the oldest listed (1.7.10 not seen).
- World Host mod: a coordinating server; for a friend it first tries UPnP (a temporary port forward on the host's
  router, then a direct join), else a proxy relay; a friends list; warns the proxy raises ping. Open source on GitHub.
- Essential mod: hosts the world from the player's game, invite only, about 8 players, everyone needs the same mods.
  How it connects is not public in what was found.
- playit.gg: a separate agent program makes an outbound tunnel; free tier with a few tunnels (blog, date unknown).
- NAT hole punching (direct connection between two homes): 82 % of NATs for UDP and 64 % for TCP in 2005 (Ford et al.),
  about 70 % for both TCP and QUIC in a 2025 to 2026 large measurement (libp2p DCUtR). So roughly 3 in 10 pairs still
  need a relay.
- Carrier grade NAT (CGNAT, many homes and every mobile hotspot share one public address) blocks UPnP and port
  forwarding; whether IndiHome / Biznet use it was not found.
- Oracle Cloud Always Free: Arm A1 4 cores, 24 GB, 10 TB outbound a month; reported "out of capacity" in busy regions and
  idle instances reclaimed (third party, 2026). Enough for a relay and probably one 1.7.10 server [not verified].

## 3. Every way to do it (not three: ten, in four layers)

### Layer A: how the bytes travel (pick one or chain them)
1. **Our relay (reverse tunnel)**: the host's game connects OUT to our relay; players connect to the relay; it pipes
   bytes. Works behind any router and CGNAT. Ping = player to relay + relay to host. We pay every byte.
2. **UPnP first, relay fallback** (World Host): the host's router opens a port by itself; players then join directly
   (ping host only, no relay bytes). Fails under CGNAT, with UPnP off, on mobile hotspots.
3. **Hole punching, relay fallback** (ICE: STUN + TURN, like WebRTC and Steam's relay): since BOTH sides have our mod,
   the player's mod opens a local port and tunnels to the host over UDP (QUIC or KCP inside). About 7 in 10 pairs go
   direct. Hardest to build; Java 8 library (ice4j) support not checked.
4. **IPv6 direct**: when both have IPv6, a direct join with no relay (1.7.10 parses it). Cheap extra path, often blocked
   by home router firewalls.
5. **Someone else's tunnel**: playit.gg (separate program per host), World Host's or e4mc's servers (not built for
   1.7.10; using their servers needs their consent). No servers for us, no control over list, limits or uptime.
6. **Virtual LAN** (Radmin VPN, ZeroTier, Tailscale, Hamachi): everyone installs a VPN and joins as LAN. Zero code, a
   website guide only; high friction, no listing, friends only.

### Layer B: where the world runs
7. **In the host's game** (Open to LAN): no install, the world stops when the host quits.
8. **A local dedicated server started by our mod** on the host's computer (the release jar already runs as a server):
   keeps running after the host leaves the game, Decimation's real server rules apply; more RAM on the host.
9. **Our own always on servers** (the menu already has Global Server, Bukovka, Chernarus "coming soon"): one community
   home that never goes offline; ping depends on our server's place; hosting cost and moderation are ours.
10. **Worlds on demand in the cloud** (Realms style): a world uploaded once, a server starts when someone joins and
    sleeps when empty. Most work and running cost; the only way a player world stays online without its owner.

### Layer C: how people find a world (works with any of the above)
- The Community page in Decimation's menu (GuiMenuCommunityServers, today "coming soon") filled from our list over
  HTTPS (roadmap UI 2: servers.json idea); the same list on the website; join codes for friends only worlds; a
  Discord link per world.

### Layer D: trust
- Who may join: online accounts only, or also cracked (section 1); public, code only, or friends; the host's kick /
  ban / whitelist; a report button on the list; limits per host on the relay (no open proxy).
- Host's privacy: through a relay players never see the host's home address (a plus over direct paths).

## 4. Recommendation (phases)

1. **Spike, now, free, thrown away after**: two dev clients, one opens to LAN, the other joins through a tiny relay
   on this Mac. Answers: does Decimation work with 2+ players on an integrated server (zones, loot, NPCs, vehicles,
   stats, our fixes); bytes per player per minute while walking, driving, in a city (sets the relay's cost); does the
   FML handshake pass through a dumb pipe. Nothing else is worth building if this fails.
2. **Relay plus list (Layer A1 + B7 + C)**: "Host online" in the pause menu; relay on one small server in Singapore;
   list over HTTPS on the Community page and the website; join codes; online accounts only to start.
3. **Cheaper and faster paths in front of the relay**: UPnP (A2) and IPv6 (A4), later hole punching (A3); the relay
   stays the fallback. Each direct join saves relay bytes and ping.
4. **Always on worlds**: a local dedicated server option (B8) and, when there are players, one official server (B9).
- Honest note: the tech removes a barrier; players still come from being found (website, videos, Discord). Both needed.

## 5. Decisions (user, 18 Oktober 2026, 01:09)
1. Accounts: BOTH. The host picks online or offline mode for their world, as a Decimation server's own setup does.
2. Money: none. Only free hosting for anything of ours (a donation for a server may come later, never expected).
3. A public list for everyone.
4. Spike now ("quick test now for find a truth code"): DONE, section 6.

## 6. Spike results (18 Oktober 2026, 01:10 to 02:15; tools/hostspike, dev test `lanhost`)

Setup on this Mac: HOST = the dev client (live game, world deciworldgen_devtest) opened to LAN by the dev test `lanhost`
(online mode off); a relay (tools/hostspike/relay.py) and a host agent that connects OUT to it (agent.py, as our mod
would); JOINER = the production client with the release jar 0.64.8 (docs/production_test.md), started with `--server
spike-world.relay.localhost --port 25600`, so every byte went through the relay. 10 runs.

What is now TRUE (seen):
1. **A reverse tunnel carries the whole game unchanged**: server list ping (46 bytes up, 481 down), login, Forge's
   handshake and play, 0 to 2 ms keep alive locally. Nothing in Minecraft, Forge or Decimation needed changing.
2. **The relay reads the address the player typed** (`"addr": "spike-world.relay.localhost"` in every handshake):
   one relay port can serve many worlds by name.
3. **Offline mode lets an unofficial account in**: the joiner had a legacy token (`--accessToken 0`) and joined a world
   with online mode off. Online mode with a real Mojang account was NOT tested (no premium account here; never handle
   one) [not verified]: a legacy account should be refused there [inferred from IntegratedServer / Mojang session check].
4. **Host and joiner need the same mods**: run 1 was refused by Forge, "Mod rejections [FMLMod:gvc{0.6.1}]" (the host
   had Gliby's Voice Chat, the joiner only the release jar). Technic players have the voice chat jar, release players do
   not: the list must show a world's mods, or the hosting must leave the voice chat out of the check (design question).
5. **Decimation works for the remote player** (host side checks): the joiner's Decimation data exists (bottlecaps 0,
   humanity 50, water 20); infected went for the joiner and killed it (20 -> 0 health, a Hulk); the joiner respawned
   (the server makes a new player object: the test looks the joiner up by name); the joiner's infected kill changed ITS
   humanity (50 -> 52). NOT checked yet: bandit gunfire at the joiner (the bandit picked a Hulk), zone banners, loot
   screens, vehicles, trading and voice chat on the joiner's side (needs the joiner driven by a test, a second live port).
6. **Bandwidth through the relay** (host view distance as set; joiner 4 chunks): join burst about 1.1 MB in the first
   seconds; standing or fighting about 8 kbit/s down; walking or teleporting into new chunks 0.6 to 0.9 Mbit/s down;
   the joiner sends 2 to 5 kbit/s. So the relay's cost is chunk loading: a player who moves all the time is about
   0.1 GB a quarter hour at worst; Oracle's 10 TB a month is far more than an early community uses [inferred].
   The host's upload carries the same bytes per player (8 moving players about 7 Mbit/s up) [inferred].

Found on the way (traps and a Decimation bug):
- **Decimation HUD crash when a world is joined without its menu** (bug.md): IngameHud.render (deci/g/j.b) reads
  DecimationMod.fakePlayerProfile (field `B`) when fancy graphics is on; only Decimation's menu (GuiMenuBase) creates it.
  A game that connects at start (`--server`) crashed 4 to 5 s after joining, 3 times of 3 with fancy graphics, 0 of 2
  without. Proven with a line tagged copy of the class (tools/hostspike/LineTag.java: crash at instruction 1064, the
  INVOKEVIRTUAL on the null field after the fancy graphics test). Players who go through Decimation's menu (the
  Community page) do not hit it [inferred]; a cheap guard is worth it anyway.
- **Forge's handshake times out on a slow joiner**: "Timeout waiting for client thread to catch up!" (FML
  waitForPlayClient) when the joiner's main thread was busy right after connecting (this Mac was 14.6 of 15.3 GB into
  swap with two games). Decimation hashes the mod files on the client at login ("[BoehMod-Anticheat] Detected hash"
  lines just before the timeout) [inferred as a cause]. Weak laptops may see this; a retry joins.
- A debugger on the joiner: the dev client already listens on 5005 (hotswap); use 5006. jdb's `catch` on
  NullPointerException stops on hundreds of harmless class loading NPEs: attach only after the join.
- One logged "Couldn't render entity" (NPE drawing the host player once at join, skin not loaded yet), harmless.

Verdict (18 Oktober 2026): the idea works with today's game; the relay is plain byte piping. Next design questions: the mod set check
(item 4), the menu guard (bug.md), online mode switch per host (decision 1), where the free relay runs (decision 2).

## 7. Decisions after the spike (user, 18 Oktober 2026, 02:14 and 02:20)
- Mods: the public list SHOWS each world's mods (a joiner sees what to install); no change to Forge's check.
- Relay: check the other free options before Oracle (section 8). The user freed RAM on this Mac (two games fit better).
- No card (user): Oracle is out; the free path is playit.gg and Cloudflare, with the free direct paths in front.

## 8. Free relay options (web, 18 Oktober 2026; secondary sources unless said, verify before relying)

What the relay must do: hold long connections both ways and carry about 8 kbit/s idle and up to 0.9 Mbit/s while a
player walks into new chunks (section 6). Since EVERY player has our mod, the transport between the mods can be raw TCP
or a WebSocket (Minecraft 1.7.10 ships netty 4.0.10 with an HTTP / WebSocket client and TLS [inferred: not tried]); the
joiner's mod then offers Minecraft a local port.

| Option | Raw TCP | Free how long | Limits that matter | Verdict |
|---|---|---|---|---|
| Oracle Cloud Always Free (Arm VM) | yes | always | 10 TB out a month; card needed (credit, or debit that works like credit: no PIN, virtual or prepaid cards, Oracle FAQ); "out of capacity" in busy regions | best capacity; blocked if no card |
| Cloudflare Workers + Durable Objects (free plan) | no, WebSocket | always | 100,000 requests a day, incoming WebSocket messages count 20 to 1 (official pricing page); duration allowance quoted as 13,000 or 313,000 GB-s a day (two copies of the page disagree); no egress fee | real, small: about 14 to 28 player hours a day [inferred: 20 to 40 messages a second per player]; no card needed [not verified] |
| playit.gg free | yes | always | host runs playit's own agent program; tunnel count and bandwidth not stated on the pricing page; Premium 3 USD a month | zero cost to us today: the list just stores the host's playit address; extra install for hosts |
| Google Cloud e2-micro | yes | always | 1 GB out a month (North America), US regions only | useless for a relay (a few player hours a month) |
| AWS | yes | 6 months (credits 100 to 200 USD since July 2025, official) | account closes after the free plan | not lasting |
| Fly.io | yes | no free tier for new accounts since October 2024 (third party) | card | no |
| Koyeb free instance | TCP proxy only on paid plans (launch post); Frankfurt / Washington; sleeps after 1 h | | no |
| Render free web service | no, WebSocket | 750 h a month | sleeps after 15 min without traffic (WebSocket messages keep it awake since Feb 2026, official); included bandwidth reportedly cut to 5 GB (third party, July 2026) | too little bandwidth |
| Deno Deploy free | no, WebSocket | always | egress quoted as 100 GB or 20 GiB, CPU 10 to 15 h a month (sources disagree); Classic retired July 2026 | unclear, test before relying |

Ways to need LESS relay (all free): a host with a public address or a port forward is listed directly; UPnP opens the
port on the host's router (fails under carrier grade NAT); IPv6 direct; UDP hole punching with free public STUN servers
plus a reliable UDP layer (java-Kcp, Apache 2.0, Netty based: its Netty version against 1.7.10's netty 4.0.10 not
checked) and signalling through our site: about 7 in 10 pairs direct (section 2), only the rest relayed.

The list itself (who hosts, address, mods, players): free on the same Cloudflare account (Workers KV / D1 / Durable
Object) or Upstash Redis through Vercel (500,000 commands a month since March 2025, third party): a heartbeat a minute
per host is 43,200 a month each, so Cloudflare fits a public list better [inferred].

Proposal (for the user): direct first (public address, UPnP, IPv6), Cloudflare Durable Objects as the free fallback
relay to start, playit.gg addresses accepted on the list as a host's own option; Oracle when a card is available or a
donation pays a small server; hole punching later to cut relay use.

## 9. Joiner side checks (18 Oktober 2026, 02:20 to 02:55; dev tests `lanhost scene=true` + `lanjoin`)

Both games ran tests: the HOST (dev client) `lanhost scene=true` sets each scene up on the server; the JOINER (production
client with a one jar of the current code, own live port 25598, `devtest.py --live --port=25598 --results=FILE lanjoin`)
leaves its own world for Decimation's menu, turns fancy graphics on, joins through the relay and asks for one scene
after another through two files (devtest/LanBus, `bus=DIR` on both), checking each on its own screen. Last two runs
(a first join, then the rejoin) all PASS (11 and 12 checks):
- joined through the relay; HUD with fancy graphics did NOT crash after going through the menu (bug.md crash needs the
  join without the menu);
- the place names list equals what the server sent this player (5), the safezone list arrived (2);
- Decimation's own crate (deci:BlockMilitaryCrate) opens its loot screen (deci.g.g) for the remote player;
- a right click on a hummer SEAT (VehicleSeatEntity deci.ad.c, not the body) seats the joiner; the host sees it riding;
- the trader screen opens (deci.g.q, 29 offers on the joiner's side) and a buy costs bottlecaps on the host (exact);
- the joiner's AKM (Deci.fireHeldGun) kills an infected on the host;
- a bandit's fire hurts the joiner (20 -> 0 or 1) and its tracers are drawn on the joiner (2 to 9).
Found on the way: **our place names list never came back after a rejoin** (bug.md, FIXED in code): PlaceNames skipped a
list equal to the last one sent to that player, but the client's list is empty after reconnecting (seen: 0 of 5); now
forgotten on logout (PlayerLoggedOutEvent); RED (0 vs 5) then GREEN (5 vs 5) in the rejoin run; singleplayer `places` and
`zones` still all PASS. Flaky, not bugs: a bandit at 14 blocks sometimes missed every shot in 20 s (now 8 blocks, 30 s);
one join dropped right after login with no reason logged (the retry joined).
Not covered yet: voice chat, zone banners on walking into a zone, online mode with real accounts.

## 10. Part 1 design (18 Oktober 2026)
Approach A chosen (our Cloudflare relay and list; playit.gg addresses as the fallback if it is down), screens approved
in the browser mockup. Spec: docs/superpowers/specs/2026-10-18-player-hosting-part1-design.md (waits for the user's
review, then the implementation plan). Real case test planned with the user's brother's Linux laptop over Tailscale.

## 11. Build notes (part 1, from 18 Oktober 2026)
- Relay tests use @cloudflare/vitest-plugin (the renamed vitest-pool-workers) with vitest 4; `cd relay && npx vitest run`.
- Background security reviews of the relay commits led to: at most 200 worlds and 2 per address, a request body read
  at most 8 KB while streaming, join deadlines never postponed by later joins, one data socket per joiner, a host's
  control socket reconnect keeps its players (relay suite 20 tests).
- TLS check (plan Task 4): the mod's WebSocket client on Minecraft's netty 4.0.10 reached wss://ws.postman-echo.com/raw
  with Java 1.8.0_504 (zulu 8 on this Mac): "TLS OK". Mojang's bundled Java 8u51 (older root certificates) is not on
  this Mac [not verified].
- Task 10 (18 Oktober 2026, 04:20 to 05:05): the two games through a LOCAL relay (`cd relay && npx wrangler dev`) with
  the real code path (host: HostService from `lanhost real=true`; joiner: Decimation's Community page filled by
  CommunityList, joined the way a row's button does): every check of section 9 PASS again (crate, lists, hummer, trader
  buy, AKM, bandit fire and tracers). Found on the way: (1) a row's Join crashed the game when Minecraft's own
  Multiplayer screen had never been opened (FMLClientHandler.connectToServer reads FML's server list cache, created only
  by setupServerList): CommunityList calls setupServerList (RED: the crash, GREEN: the join); (2) ModSet read the
  @Mod version ("0.43.0" in the dev client) instead of mcmod.info's (the git tag): both games now compare the same
  version text and a different one is refused, as designed; (3) a crashed host's room stays listed until its 3 minutes
  run out (the row appears twice for a while). Traps: never run a Gradle build while the live dev game runs (it
  recompiles the classes the game loads: NoClassDefFoundError HostService$7, the host crashed); a commit by another
  session changes the build version (git describe) and makes host and joiner differ.
- Relay message counts per minute (spec 7) were not measurable with `wrangler dev` [not verified]: read them in the
  Cloudflare dashboard after the deploy (Task 11).
- Task 11 (18 Oktober 2026, 05:15 to 05:55): the relay is DEPLOYED on the user's free Cloudflare account,
  https://deciremnant-relay.deciremnant-relay.workers.dev (RelayClient.DEPLOYED; wrangler deploy from relay/, the user
  logged in with `npx wrangler login`). Found at once: Minecraft's netty 4.0.10 never finished a TLS 1.3 handshake with
  Cloudflare ("SSLException: handshake timed out"; the echo server earlier spoke TLS 1.2): RelaySocket now offers TLS 1.2
  only, proven RED then GREEN with a probe (control socket "open true"). Then the two games through the deployed relay:
  host ONLINE, the Community page row "Spike world / host Developer · via relay - 297ms" (524 ms in another run), Join,
  1/8; the join took 1 to 2 s; every check of section 9 PASS (05:43 and 05:52). The same Mac talks to Cloudflare's edge
  and back, so these pings are the relay's own cost; a real joiner adds the way to the host [inferred].
- "Mojang accounts only" (the default since the user's choice of 18 Oktober 05:18) refused the joiner's offline account:
  the host logged the profile without id and "lost connection" before the world, the joiner got Minecraft's disconnect
  screen (05:53). The exact text of that screen was not captured [not verified].
- Final review of the branch (one fresh reviewer): 1 Critical (the host's lock held across network calls froze the
  client tick), 10 Important, 14 Minor; all Critical and Important fixed in one pass with tests (ledger
  .superpowers/sdd/2026-10-18-player-hosting-part1/progress.md), the relay redeployed with them. Deferred minors there.
- Fixed after (18 Oktober 2026 06:15): the hosted world's player limit follows the host's choice (HostLan.limit; the
  rows said "x/8"; lanhost check "server max players" RED 8 then GREEN 4); Decimation's HUD crash without its menu
  (fixes/HudGuard, bug.md).
- Still open: the relay's real message count per minute (the Cloudflare dashboard); the website list, kick and ban
  (part 3); hosting goes into the next batched release (user: after the brother test, done in section 12).

## 12. Real test with the user's brother, commands for the host (18 Oktober 2026)

- Setup: the test jar (built at 53be3fb) served over the home Wi-Fi with python http.server; the brother's Linux laptop
  (offline account "kucing", no Tailscale needed) downloaded it. First the harness host (production client, dev test
  lanhost online=false scene=true real=true): kucing joined at 07:02 through the deployed relay, the list showed 1
  player of 4. Then the user hosted by hand from a normal game (Host Online page, Any account): kucing joined again.
- Result (user): ping 150 to 270 ms on the brother's side, "works smooth". The user found him in the world (07:12 shot:
  kucing in front of the host, creative, Decimation's HUD).
- Found: the host could not use any command ("You do not have permission to use this command"): a hosted world is
  shared with cheats off (shareToLAN(mode, false)), so nobody has commands, the host included.
- Fix (user: "add commands only me before release"): the Host Online page option "Commands: only me" (default on,
  "Commands: off" by a click). fixes/hosting/HostOps makes the host an operator of the world (Minecraft's ops list,
  ops.json in the game folder) when hosting starts, and takes it away on Stop hosting or when the world unloads; the
  entry it added is written to deciworldgen_hostop.txt so a game that crashed while hosting removes it at the next
  start or stop; a host who was an operator already stays one. Joiners never get commands (commandsAllowedForAll
  stays false). Dev test `hostops` (world cheats and the dev client's commands for everyone turned off for the test):
  RED 2 FAIL (host while hosting, default), GREEN 7 PASS; hostmenu checks the default. Release jar (2d954c1) in the production client: hostops 7 PASS,
  hostmenu PASS (07:31; the first run showed the test's reflection needs SRG field names too). Real game 07:38: the user
  hosted with the new jar, kucing joined (07:37:12), "/tp kucing" worked (log: "Teleported DeciHost to kucing"). The
  brother's own /tp attempt was not tried [not verified]; the joiner side is covered by the hostops check.

- Second session (18 Oktober 2026 afternoon): jar built from fa0c60b (sha256 4e759116...), served the same way. The user
  hosted from the dev client, which always has Gliby's Voice Chat (gvc 0.6.1, installVoiceChat); the brother had only
  Decimation.jar and got our "need gvc 0.6.1" refusal. With the original voice chat jar served beside it he joined;
  user: "cloudflare multiplayer is really do well now". Fixed the same day: gvc is optional for joiners
  (fixes/hosting/OptionalMods wraps Forge's checker, ModSet.OPTIONAL; bug.md), two game test PASS (dev host with gvc,
  production joiner without, deployed relay, 4 s join).
- Corpse scenes (18 Oktober 2026): lanhost / lanjoin `corpse` (a corpse 2 blocks in front of the joiner), `corpsedeath`
  (the joiner dies holding an AKM), `corpsefind` (back at its own corpse with an AKM), `corpseopen` / `corpsestate` (the
  host's side). They found the gun bug (bug.md "A joined player could not open a corpse"). The scenes run with natural
  spawns off (doMobSpawning false from "arena" until "end"). tools: a two game run script in the scratchpad
  (onejar of the dev build into the production client, both live games, lanhost then lanjoin) [scratchpad only].

## 13. Community page texts (18 Oktober 2026)

- User: the page says only how many players are online, not "in N worlds"; an empty list shows a placeholder.
- fixes/hosting/CommunityText (plain Java, JUnit CommunityTextTest RED 2 FAIL then GREEN): headline "Players Online: N"
  (hosts plus joiners), three placeholder lines centred in Decimation's panel when no world is listed. The tab tooltip
  still says "N worlds". Dev test menupages shoots the opened page (menupages_communitylist; docs/shots_index.md
  community_empty.png).

## 14. Relay lag from Indonesia, playit.gg measured (18 Oktober 2026)

User (after a test with a friend): "for the cloudflare i think its to laggy ... its better to more advance the multiplayer
use playit gg?". Measured from the user's home connection (Indonesia), 21:20:
- Our relay (deciremnant-relay ... workers.dev, free plan): /cdn-cgi/trace says colo=CDG (Paris), loc=ID. TCP connect
  33 to 47 ms (a near edge), TLS 0.44 to 0.56 s, a whole request 0.69 to 0.82 s (5 tries). cloudflare.com from the same
  connection: colo=CGK (Jakarta). So the free Worker is served from Paris for this ISP, and every packet of a relayed
  game goes Indonesia, Paris, Indonesia twice (joiner leg and host leg): the 297 / 524 ms rows and the brother's 150
  to 270 ms fit that. Cloudflare's own Indonesia post names ISPs that refuse to peer locally (traffic pushed abroad);
  why free Workers land in Paris exactly is [not verified]. A Durable Object location hint would not help: the Worker
  entry is already Paris.
- playit.gg free (global anycast): ping.gl.ply.gg answers "tunnel_name: Singapore_1"; ICMP ping 48 to 68 ms (avg 57);
  traceroute ends in Equinix Singapore. Its free tunnels cannot pick a region (Premium can, 3 USD a month); playit says
  anycast may route some players far away (their docs) [per player, not measured for the friend].
- Reference: 1.1.1.1 ping 42 ms, 8.8.8.8 58 ms.
Conclusion: for players in Indonesia a playit.gg tunnel (Singapore) should cut the relay's round trip several times
[inferred from the pings, not yet measured in a game]. Options for the user: section 8 option B (the host's own
playit.gg address on the Community list, joiners connect straight to it, our relay as the fallback), a direct
connection (IPv6 or port forward; fails behind CGNAT), or our relay elsewhere (a server in Singapore costs money).
Sources: playit.gg/support/playit-premium, discuss.playit.gg (regional tunnels), blog.cloudflare.com/indonesia,
developers.cloudflare.com geographic traffic routing.
- Second look (21:35): the free Worker's data center changes per new connection. Six tries (IPv4 and IPv6, 3 each):
  FRA 0.72 s, FRA 0.70 s, SIN 0.25 s, SIN 0.24 s, LHR 0.80 s, FRA 0.83 s (whole HTTPS request). cloudflare.com and
  workers.cloudflare.com from the same connection: CGK (Jakarta) every time. So free traffic from this ISP is spread over
  Asia and Europe; Paris in the first look was one of them. A Durable Object (each hosted world's Room) is placed near
  its first request and stays there [Cloudflare docs; where our Rooms sit is not measured], so a room first touched
  through Europe keeps the whole game in Europe.
  Idea (user wants no extra program for hosts): keep Cloudflare, but (1) create each Room with locationHint "apac"
  (Durable Objects location hints) when the host is in Asia, and (2) let the mod reconnect a data socket that landed far
  away (the Worker reports request.cf.colo) a few times until it lands in Asia. [Not built, not measured.]

## 15. Direct joins over IPv6, relay as the fallback (18 Oktober 2026)

User: "build 1 first, if its good we can pending the playit gg later as optional", and on the host's address: "Host
chooses, direct on" (the address is never on the public list; the relay hands it only to a game that joins).
- Host (fixes/hosting/Direct.Gate, HostService.register): with "Direct + relay" (the Host Online page's Connection switch,
  default) a gate listens on a free port of every address; the host's global IPv6 addresses (2000::/3, at most 4) go
  with the registration as "[address]:port". The gate answers "DRPROBE <room>" with "DROK" for its own room only,
  holds a joiner's first bytes until HostService.admit decides (the relay path's login checks: names in use, offline
  worlds), then carries the connection to the LAN port on 127.0.0.1. No login before the relay gave the room; a total
  10 s deadline and at most 16 joiners in that phase; closed with the control socket. No global IPv6: relay only
  ("Relay (no IPv6 address here)" while hosting).
- Relay (relay/src/lobby.ts): `direct` on /list/host, only numeric global IPv6 with a port 1024 .. 65535, never listed
  (route "direct" on the list instead); POST /list/direct {room} answers them, at most 10 asks a minute per caller.
  Local tests only: `wrangler dev --var DIRECT_LOOPBACK:true` takes [::1]. Deployed 18 Oktober 2026 22:3x (version
  29fbb993); checked live: list, unknown room 404, [::1] refused.
- Joiner (JoinService.route): Minecraft still connects to the local 127.0.0.1 port; for a route "direct" world the first
  bytes are read; a LOGIN (never a Community row's ping, so opening the page contacts no listed address) asks
  /list/direct, probes each address (1.5 s), and pipes straight to the first that answers; otherwise the relay with those
  bytes first. Only numeric global IPv6 targets are ever connected (Direct.target).
- Checks: unit tests 73 (DirectTest 10, JoinDirectTest 4, HostServiceTest 3 new; RED first), relay vitest 25 (5 new).
  hostmenu: Connection switch default "Direct + relay", flips to "Relay only". Two games through a LOCAL relay with
  `directloopback=true` (both on this Mac, [::1] standing in): "join route direct", "direct gate open true", every
  scene PASS on both sides (crate, lists, hummer, trader, AKM, bandit fire, corpses). Relay only (`direct=false`): joined
  through the relay, gate closed; bandit fire FAILed once (20 -> 20; flaky, also seen before, bug.md).
- NOT verified: a real direct join over the internet. This Mac had a global IPv6 address at 21:20 (playit's page and
  curl -6 worked) and none at 22:10 (only fd7a / fe80, curl -6 fails): the home connection's IPv6 comes and goes
  [cause not checked]. A real test needs global IPv6 on host AND joiner at that moment, and routers that let the
  connection in (many block incoming IPv6 by default) [not verified].
- Security reviews on the way (fixed): numeric global IPv6 only, deadline and cap, gate closed on a failed control
  socket, ask limit. Left as the user's choice: anyone who clicks Join (or asks /list/direct for a listed room) can learn
  a direct host's address; the tooltip says so; "Relay only" hides it.

## 16. Stability and ping (18 to 19 Oktober 2026)

User (after the friend test, direct on): "reduce really well from 1000ms to 400ms ... but the problem is the server really
often counter auto left from my friend, he often left even the ping stable", then "make it stable as you can ... lower the
ping more and more".
- Cause found: the host's control WebSocket to the relay carried nothing while nobody joined (no keep-alive). Measured on
  the deployed relay: an idle control socket was cut after 391 s with code 1006. A lost control socket made the relay close
  every joiner at once and the host register a new room (closing every relay pipe and the direct gate): everyone kicked
  while the ping looked fine. Cloudflare's docs: idle WebSockets are closed after a while (time not documented), and ping
  frames are answered by the runtime without waking the Durable Object.
- Fixes (relay version 94b7cb49, game from 5ed53b2):
  1. RelaySocket sends a WebSocket ping every 25 s (free, never a message).
  2. Relay room: a dropped control socket gets GRACE_MS = 20 s; joiners stay, the list keeps the world; a host back in
     time keeps everyone (room.ts lostAt + alarm).
  3. Host: a lost control socket reconnects to the SAME room (5 tries in about 15 s) before registering a new one; the direct
     gate and its players survive a re-registration; new logins only while ONLINE.
  4. Rooms placed near the host (location hint by the host's country: apac-se for Indonesia); each world carries its region.
  5. Relay sockets report their data center (x-colo); a socket that landed far from an Asian room is opened again (host 3,
     joiner 2 tries; Hong Kong counts as far for apac-se: measured 150 ms against 80 ms). Host control and data sockets
     overlap (the next opens before the far one closes, the relay replaces the older data socket without dropping the
     joiner, and what the far one heard is carried over: without that the joiner's login was lost; found with a local relay
     that fakes far landings, `--var FAKE_COLOS:FRA,SIN --var FAKE_REGION:apac-se`).
  6. A write after a quiet tick goes at once (no 50 ms wait for a lone click); busy streams still one frame per tick.
  7. Log lines `[deciworldgen hosting]`: where each socket landed, control lost / back, joiner knocks / not answered, relay
     closes with code and reason, joined directly / through the relay.
- Measured on the deployed relay from this Mac (script scratchpad rtt_probe.mjs: host and joiner, timestamps through a
  room): before about 300 to 500 ms (Community rows 297 / 524 ms); after, all sockets in Singapore, median 77 to 86 ms
  (best 74, worst about 200) in 3 of 4 runs; 150 ms when a socket landed in Hong Kong (now retried).
- Checks: unit 82, relay 29 (RED first each). Two games with a forced control drop (dev test scene `dropcontrol`, an
  abrupt cut): local relay, relay and direct paths; local relay with every other socket landing far; and the DEPLOYED
  relay (relay only): joined, still in the world 30 s after the drop, host ONLINE on the same room, every scene PASS
  (bandit fire flaky once). Test traps: a dev host game rebuilds itself on a commit, so the joiner jar must come from the
  same commit (the row says "needs DeciRemnant ..."); `pgrep -f zulu-8.jdk` also matches Gradle test workers.
- NOT verified: a long session with the friend over the internet with this build [user test]; the direct path from the
  friend's network (his toast says which way).


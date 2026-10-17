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

## 5. Open decisions (the user's)
1. Cracked launchers: online accounts only (safe, smaller audience) or allow offline accounts (bigger audience; names
   can be faked, ops and bans by name break)?
2. Money: a small rented server for the relay (a few dollars a month), Oracle's free tier (capacity risk), or none
   (playit.gg / virtual LAN guide only)?
3. Public list for everyone, or join codes first?
4. Spike now?

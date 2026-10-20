# Testing the shipped game, not the dev client (13 Oktober 2026)

The dev client runs Minecraft with readable names, our classes from Gradle and Decimation from
dev/libs. Bugs that only players see (user: "maybe there is issue with local and production") need the
RELEASE jar in a plain Forge game. Never Prism (off limits): we build our own production client and
server in the scratchpad. Nothing here is committed except this doc, tools/prodclient/classpath.py and
tools/prodsrv_run.py (drives a server folder: fresh world, DevPregen flags, stop, our lines and errors).

## Production server (dedicated)
1. Forge installer 1.7.10-10.13.4.1614 from maven.minecraftforge.net, `java -jar installer.jar
   --installServer` in an empty folder (Java 8: ~/.jdks/zulu-8.jdk).
2. mods/ = only the release Decimation.jar (dist/onejar), eula.txt, server.properties (level-type
   decimation, seed, port 25577, online-mode false).
3. Start `java -jar forge-...-universal.jar nogui`, wait for "Done (", send `stop`.
Checked with it: bundled content unpacked, cities generated, no errors (bug.md v0.58.0).

## Production client
- Classpath (tools/prodclient/classpath.py DIR): Forge universal FIRST, Forge's libraries (from a
  server install's libraries/ folder), the 1.7.10 client libraries from the Gradle cache (the same
  versions the dev client uses, LWJGL 2.9.4 nightly with osx-arm64 natives extracted into DIR/natives),
  the vanilla client jar LAST (Mojang's version json; sha1 e80d9b3b...).
- Start: `java -Djava.library.path=DIR/natives -Dfml.ignoreInvalidMinecraftCertificates=true
  [-Ddeciworldgen.autotest=true -Ddeciworldgen.autotest.live=true] -cp CP net.minecraft.launchwrapper.Launch
  --username X --version 1.7.10 --gameDir DIR/game --assetsDir ~/.gradle/caches/retro_futura_gradle/assets
  --assetIndex 1.7.10 --uuid 0...1 --accessToken 0 --userProperties {} --userType legacy --tweakClass
  cpw.mods.fml.common.launcher.FMLTweaker`; mods/ = only the release jar.
- With the autotest flags it opens the live test port: `python3 tools/devtest.py --live MODE` drives it
  like the dev client (it connects to whatever listens on 25599). Our dev tests are in the release jar
  (inert without the flag).
- Traps found setting it up:
  1. Do NOT use the Forge universal jar from a server install folder: its manifest Class-Path names
     minecraft_server.1.7.10.jar, which then sits before the client on the classpath; every Forge
     patch reported "binary discrepency" (server classes). Use the universal jar downloaded on its own.
  2. No -XstartOnFirstThread on macOS with LWJGL 2: Decimation's preInit hung in Display.setTitle.
  3. The Gradle cache holds a -javadoc jar next to LWJGL: pick the plain one.
  4. The first test in a new world runs while the player still falls: run it twice.
- With OptiFine (the brother's laptop plays with OptiFine HD U E7): put OptiFine_1.7.10_HD_U_E7.jar next to the release
  jar in mods/ (the copy in the scratchpad came from wyvern's mods folder); Forge loads its tweaker. OptiFine moves
  RenderHandEvent into renderHand: test anything that touches the hand or the scope there too (bug.md "Aiming with
  OptiFine"). Run `devtest.py --live --port=25598 --results=DIR/game/devtest/results.txt scopeperf sight=eotech558`.
- GitHub's release API answered 403 (unauthenticated rate limit, 60 requests an hour per address)
  after many checks in one afternoon [inferred]; the updater then showed Play (no update). Since v0.59.1
  it falls back to the release feed and plain download links (new_feature.md "rate limit fallback").

## Results (13 Oktober 2026)
- Left click (bug.md, friend's report), dev test `click` in the production client, simulated presses
  (KeyBinding with key code -100 as Minecraft.runTick does): bare hand breaks dirt, also after holding a
  gun (attack key 0 while the gun is held, -100 after), a fire axe hits a pig: all PASS, same as the dev
  client. Not tested: a real physical mouse click (desktop control needs macOS Accessibility / Screen
  Recording permission for the Claude app; `click manual=true` waits for a person's click).

## Results (17 Oktober 2026, release 0.64.1)
- Server: the one jar (sha256 f6ae0b91...) on a fresh seed 1 world: bundled content 0.64.1 unpacked (486 files, 0
  failed), city sizes / airports / refuges on, structures placed, "Done (4.332s)", clean stop. Error lines only Forge's
  version check, FML's signature notice and Decimation's dead BoehMod hosts (www.boehmod.net, its update file).
- Client (prodclient, the same jar): `devtest.py --live menu stats mainmenu menupages` 22 PASS, 0 FAIL (results in
  prodclient/game/devtest/results.txt: devtest.py reads only the dev run folder and printed "NO RESULTS"); window title
  "DeciRemnant v0.64.1 (Minecraft 1.7.10)", Updates "Up to date: 0.64.1", the server pages coming soon, Early access.
- Trap found: dev test helpers that reflect on Minecraft members by MCP name ("buttonList", "actionPerformed") crash
  the shipped game (SRG names there: field_146292_n, func_146284_a). DevTestUtil.buttons / press try both since
  17 Oktober 2026; GuideShotsTest still has its own MCP only copies (dev client only).

## Results (17 Oktober 2026, release 0.64.5)
- The one jar (sha256 945e7cd4...): server on a fresh seed 1 world: content 0.64.5 unpacked (486 files, 0 failed),
  "Done (3.990s)", clean stop. Client: live `doublejoin menu stats mainmenu menupages` 25 PASS, 0 FAIL (title
  "DeciRemnant v0.64.5"); one launch `garrison` (a fresh seed 1 world: start the client with
  -Ddeciworldgen.autotest.test=garrison instead of .live, scratchpad prodclient/run_garrison.sh) all PASS: 16 of 16
  posts held, 16 homes, a guard moved 12 off back after 175 ticks.
- Trap: a live run keeps the open world, so tests that need a fresh world (garrison, siteshots, checks) find nothing
  there ("none within 300 blocks"); run those one launch.

## Results (17 Oktober 2026, release 0.64.6, after the code review fixes)
- The one jar (sha256 d6b8dfe2...): server fresh seed 1 world: content 0.64.6 unpacked (486 files, 0 failed), "Done
  (3.695s)", clean stop. Client one launch `garrison`: all PASS (13 posts at the first visit, the rest as their chunks
  filled in, 16 homes, a guard back after 174 ticks). Client live `doublejoin gates menu stats mainmenu menupages`
  (gates=checkpoint/hesco): 27 PASS, 0 FAIL; title "DeciRemnant v0.64.6", the checkpoint's flag counted once.
- After publishing 0.64.6 (17 Oktober 2026): an older release jar (deciworldgen 0.60.1) in the production client logged
  "update 0.64.6 available (238 MB) from the api, running 0.60.1, sha256 known": players on older versions get the
  Update button. Trap: an older jar cannot open a test world made by a newer one (Forge's missing blocks prompt,
  "Unidentified block: deciworldgen:boom_barrier"), so a live run on it hangs; read the startup log instead.


## Second player through a relay (18 Oktober 2026, player hosting spike)
- The production client as a joiner: tools/hostspike/run_join.sh.example (`--server NAME --port 25600`, -Xmx2G, no live
  test port: the dev client holds 25599). Its mods must match the host's: the dev client also loads Gliby's Voice Chat,
  so copy dev/libs/DecimationVoiceChat.jar into its mods for the run and remove it after (Forge refused it otherwise).
- Traps: fancy graphics + joining at start crashes Decimation's HUD (bug.md); with two games this Mac swaps and Forge's
  handshake can time out ("Timeout waiting for client thread to catch up!"): render distance 4, fancy off, retry.
- Joiner checks (section 9 of docs/player_hosting.md): the production client gets a one jar of the CURRENT code
  (`tools/onejar.py dev/build/libs/deciworldgen-<ver>.jar SCRATCH/Decimation.jar`, never dist/onejar) and its own live
  port (tools/hostspike/run_join_live.sh.example); put the release jar back after.

## Results (20 Oktober 2026, city stall fixes, test jar d6aea397 from main 461ed9d)
- Client (prodclient, one launch `-Ddeciworldgen.autotest.test=genwalk`, script run_genwalk.sh in the scratchpad: a copy of
  run_garrison.sh with the mode changed): the 30 chunk flight over new city land in a fresh seed 1 world (row -523, view
  12), structure writes lit once: server ticks median 6.3 ms, slowest 1 % 149 ms, worst 289 ms, 17 over 100 ms, 3 over
  250 ms; client frames median 19.5 ms, slowest 1 % 35 ms; flight check PASS. Midnight shot on the roof: dark moonlit
  roof, a lamp lit window (block light works in the release jar), no dark or bright patches. Log errors: only Forge's
  version check. The jar is also on the Desktop (DeciRemnant-multiplayer, feeb69f0 kept beside it); the Mac Prism and
  wyvern still hold feeb69f0.

## Results (20 Oktober 2026, release candidate 0.65.0 from the prerelease branch, sha256 1c604032)
- First release built from the prerelease branch (CLAUDE.md "RELEASING FROM IT"): base jar = main's minus the Multiplayer
  news picture; the 26 calls of patched Decimation classes into our code all exist in the prerelease build; the 28
  generated gun files copied in (the worktree lacked them: without them UMP9, HK416 and Mk18 would have shipped without
  models); content equal to 0.64.8's. Against 0.64.8: nothing removed, 54 new entries (our classes, dev tests), 10
  patched Decimation classes, 3 news pictures changed.
- Server (prodsrv, fresh seed 1 world, `python3 tools/prodsrv_run.py DIR JAR NAME -Ddeciworldgen.pregen=512,-1024,6`): pregen light check sky 2,197, block 8,620, the same as the dev build to the
  cell; 225 chunks in 1.5 s; walk 40 steps median 101 ms, worst 275 ms. Log: only Forge's version check, FML's
  signature notice, BoehMod's dead host and the vanilla whitelist file. (The folder had lost its eula.txt: restored with
  eula=true as in the earlier release checks.)
- Client (prodclient): genwalk same path worst tick 301 ms, slowest 1 % 119 ms; glstack, throwables, armskin,
  corpsegun, nospawn, npclook, gunbatch, threads, radios all PASS; lightleak PASS on its second run (the first came
  right after a 24,000 block jump: the arena's chunks had not reached the client yet); menu, stats, mainmenu,
  menupages all PASS (GitHub news panel, not Multiplayer; window title v0.65.0; "Last played: ..."; Community "coming
  soon"); with OptiFine scopeperf all PASS, 0 GL errors. Decimation's riot shield (ShieldItem) throws a caught
  NullPointerException when glstack holds it in third person (test only; Decimation's own model).
- Real update: the 0.64.8 release jar in mods/, the fake release server serving the candidate (FILE=), `update jar=true
  expect=0.65.0`: found 0.65.0 through the API, Update button, Decimation's progress screen, 239.6 MB in 8 s, checked,
  Quit game / Later, jar swapped (sha256 of mods/Decimation.jar = 1c604032); restarted as 0.65.0 (content unpacked 486
  files, "up to date (0.65.0)").
- Published 20 Oktober 2026 10:55 (draft first, CLAUDE.md "RELEASE 0.65.0"): GitHub's digest = local sha256, public
  download 302 then 200 with 251,272,285 bytes, the published .sha256 reads 1c604032. Site: Download 0.65.0 at 11:11
  (the first request was STALE, the next one HIT with 0.65.0).
- Real update from GitHub: the 0.64.8 release jar in mods/ (its live port is 25599: the port setting came after
  0.64.8), the game's own start check "update 0.65.0 available (239 MB) from the api, running 0.64.8, sha256 known";
  `devtest.py --live update real=true jar=true from=0.64.8 expect=0.65.0`: Decimation's progress screen, "Downloading:
  33%", 239 MB in 28 s, checked, Quit game / Later, swap exit 0 (mods/Decimation.jar sha256 1c604032); restarted:
  content 0.65.0-3d2721f3ce41 unpacked (486 files, 0 failed), "up to date (0.65.0)"; `menu mainmenu` all PASS (window
  title DeciRemnant v0.65.0, Updates "Up to date: 0.65.0", "Last played: deciworldgen_devtest").
- Trap: the first run FAILed "Update opens Decimation's progress screen" (still deci.i.e) and then waited 20 minutes:
  the game had gone straight into the world, and Decimation's home menu ignores clicks during its welcome fade
  (GuiMenuHome.actionPerformed passes a click only when ClientState.cg <= 0.1; cg starts at 1.5 and drops 0.01 a menu
  frame: about 140 frames once per game session). The rerun in the same game passed. Run `menu` before `update` in a
  fresh game.


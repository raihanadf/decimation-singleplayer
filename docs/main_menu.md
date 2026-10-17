# Our main menu: Decimation's own menu with our news and real scenes (17 Oktober 2026)

How Decimation's menu is built, element by element: docs/decimation_code/menus.md section 7. This doc is what we
changed in it.

## 1. What the user asked

- "lets make the news panel now. 1. github repo, 2. update release, 3. our website release, 4. Coming soon feature.
  that for new panel/first main menu page"
- "for the new look, i agree with you to replace with real scene with your suggestion animation. do that"
- "just use component from decimation gui menu its self, no need to add something new on it"
- "if you can make the background image, changed every 7 second"
- Later (not done yet): "put real decimation logo from main menu to add it on website logo too" (docs/tasks.md).

## 2. What a player sees (shots: docs/shots/main_menu_v0.62.9/, shots_index.md)

- **News panels**: Decimation's own news panel (ArticlePanel, 120 x 70: title, a small line, a picture, red on hover
  with its hover sound and link mark, a click opens the page) four times, in the 2 x 2 grid Decimation keeps for news
  right of the buttons (GuiMenuHome would fill it only from BoehMod's dead backend):

  | Panel | Line | Opens | Picture (a real picture of the topic) |
  |---|---|---|---|
  | GitHub | Source code, issues, all releases | the public repo | our repo page on GitHub (dark) |
  | Updates | follows fixes/Updater: "Checking GitHub releases", "Up to date: 0.62.9" (dev suffix cut), "New version X is out", "Release check failed, see GitHub" | the newest release's page, else the release list | the release list on GitHub with 0.59.0 |
  | Website | Download, install and server guides | deciremnant.vercel.app | the site's top with the Decimation logo |
  | Coming soon | What we are building next | the site's #coming | the site's coming soon cards |

  The first pictures were scenes of our world (MAC-10, airport, camp, base at sunset); the user: "replace the picture of
  news panel to real topic image rather using placeholder like that". The topic pictures are agent-browser screenshots
  at 2.32 : 1 in docs/shots/main_menu_v0.62.9/news_src/ (GitHub pages in dark mode at 928 x 400, the site's top at
  1160 x 500, the coming soon cards at 928 x 400), made 464 x 200 by tools/menu_assets.py. Retake them when they go
  stale: the Updates picture shows the newest release (0.59.0), the website pictures the site as it is.

  Main menu only: in a world the same screen is the pause menu, no panels there.
- **Background**: Decimation's own background drawing (one picture with its slow sway, slow zoom in and out and the
  mouse parallax, its drifting fog, side shadow and dark top and bottom), now showing six real scenes of our world one
  after another, 7 s each, the next fading in over 1.2 s, starting with the one Decimation picked for the launch. The
  opaque black layer that hid Decimation's own pictures, the rotating lava "earth" cube, the white "sun" cube and its
  six glow sprites are gone. Scenes (from the website's shots, dimmed to 62 % and 80 % colour): a city from above, a
  city refuge among towers, a wasteland gate street, a bridge over a canal, a base at sunset, the airport.

## 2b. v0.64.0 (17 Oktober 2026): links, our name and version, Continue, our corner

The user picked from suggestions: "Links to our site", "Replace FURY advert", "Our version shown" ("for version replace
version code with our version code name, and for decimation replace with deciremnant"), "Continue last world". Not
picked: Play straight to the worlds, hiding the dead pages.
- The red links top right say **Website** and **GitHub** on every menu page and open our site and our repo
  (PatchMenuTexts: labels in GuiMenuBase and the other pages, addresses in ClientResources).
- Our name and version where Decimation showed its own: the title line **DeciRemnant: Zombie Apocalypse**, **v0.63.0**
  under the logo (our version, a dev build's suffix cut), the welcome box **Welcome to DeciRemnant v0.63.0! Have fun!**
  (Decimation's lang file plus the version call), the window title **DeciRemnant v0.63.0 (Minecraft 1.7.10)**. Our
  texts carry no dash (user rule; Decimation's "Decimation - " and "Minecraft - Decimation v1.21.10f" had one).
  Decimation's own mod version stays 1.21.10f (servers and the handshake use it).
- **Continue** under Play (Decimation's own black menu button, tooltip "Open <world>"): opens the world played last; the
  buttons under it move down one row; main menu only, only when a world exists (fixes/MenuContinue). Dev tests never
  click it (it would open the newest world of the dev run folder, maybe the user's).
- The FURY store advert's corner shows ours: Decimation's torn black panel with **DeciRemnant**, "Download and guides"
  and deciremnant.vercel.app in the Minecraft font the website uses; the title pulses like FURY's did.

## 2c. v0.64.1 (17 Oktober 2026): the server pages say coming soon, Early access, Chernarus

The user's words: "for server status, changed it like this: Global Server, Community Server, Download, with that name 3
of them for the offline"; "for my supporter, change the name to Early access"; "for server that is down, replace it with
Bukovka and Chernarus only and its say coming soon, same with player online its say coming soon instead of unknown, with
same community to when notification appear, its say community server coming soon instead of failed to load"; five
Chernarus pictures ("here the chernarus location"). Shots: docs/shots/main_menu_v0.64.0/menupages_*.png (shots_index.md).
- **Official server list** (Play): two rows, **Bukovka** and **Chernarus**, line "Official DeciRemnant Server (Official)",
  state **Coming soon** in gold. Decimation drew "?/?" over the state text of every row without a player count (its
  "Not Reachable" read "Not Reach?/?le") and " - 0ms" for a ping that never happened: both gone. Above the list
  **Players Online: Coming soon** (was Unknown).
- **Map pictures**: hovering a row fades one of its map's eight pictures in behind the page and inside the row (Decimation's
  own, picked at random per hover: textures/gui/menu/serverbackgrounds/<map>0..7.png). Bukovka keeps Decimation's eight;
  Chernarus shows the user's five pictures (coastal town, harbour with cranes, castle ruin, village under power lines,
  village with a pond; 720 x 405, sources in docs/shots/main_menu_v0.64.0/chernarus_src), the first three twice.
- **Community tab** on that page: tooltip "Coming soon" (was "Offline"); a click shows the toast **Community server coming
  soon** (was "Failed to load Community list!").
- **Server Status page**: Global Server (Official DeciRemnant server), Community Server (Servers run by players), Download
  (DeciRemnant download server), each **Offline**; the fourth row, Website (BoehMod's site), unchanged and Online.
- **My Community popup** (the button under Continue): **★ Early access** (line "New features before everyone else!"; a
  click: toast "★ Early access is coming soon!"), Website ("Download, install and server guides"), Forums (unchanged:
  BoehMod's forums; the user: "later we make forum on our website on it"), GitHub ("Source code, issues, all releases").
- The profile cards say **No early access** (was "Not Supporter :(" and "Is not a Supporter :("), the lounge title
  "★ Early access lounge" (nobody reaches it: no player is a supporter without BoehMod's backend).
- Since v0.64.7 (user): toasts carry the title DeciRemnant (ToastNotification deci/b/b), and the status page's fourth
  row is our Website (Official DeciRemnant Website, deciremnant.vercel.app port 80, Online when the site answers).
- No network: every row's host ends in .invalid (RFC 6761: never resolves), so the pings fail at once and nobody's
  old domain gets our players' connections. The status page's pinger has no timeout: on Decimation's dead hosts it sat
  at "Pinging..." for over 20 s (a resolving host that never answers holds the socket about 75 s).

## 2d. v0.64.7 (17 Oktober 2026): Decimation's own update screen, our logo

- **Update** (user: "why not using from decimation instead? as i know decimation has it owns update loading screen"):
  the menu's Update button opens Decimation's own progress screen GuiUpdateProgress (deci.i.m: black page, the banner
  with the soldier and the infected, "DO NOT CLOSE YOUR GAME!" in red while it downloads, "SAFE TO CLOSE GAME!" in green
  after, "Downloading: Decimation.jar (12.8mb)", two lines, "Downloading: DeciRemnant 0.64.6 from GitHub", the green
  bar with its flare, "40.0mb/12.8mb" (Decimation prints total first)). fixes/UpdateProgress fills the fields it draws
  (UpdateChecker azL .. azP and fileName, Deci.setUpdateProgress) from our download each client tick. When the jar is
  checked: **Quit game** and **Later** under the bar (that screen draws its black layer over its own buttons, so ours
  are drawn again on DrawScreenEvent.Post). A failed download or a check that found nothing yet shows our old
  UpdateScreen (the reason, Try again / Back). The two lines come from Decimation's lang file, rewritten by
  tools/menu_assets.py (no "your game may freeze", no "your game will Shut down": ours does neither).
- **Logo** (user: "replace old logo on decimation with our new logo"): banner.png (top left of every menu page; in game
  too) and banner_intro.png (home page fade in, update screen) carry our official DeciRemnant logo (the website's,
  docs/shots/main_menu_v0.64.0/logo_src, white paper cleared by tools/site/export_assets.py clear_paper), at twice
  Decimation's size (drawn by UV 0..1: sharper, same place). banner_intro keeps Decimation's soldier and infected; only
  its lettering and barbed wire (row 127 down) are replaced. The window icon (a character portrait, no name) and the
  BoehMod logo (credit) stay. tools/menu_assets.py logos(), three jars.
- Checked (RED first, then PASS): `update` (fake release: Decimation's screen opens, it shows 33 % / downloading /
  13.5 of 40.0 MB, ready: Quit game and Later, safe to close), `menu` ("menu logo is ours": 922 x 202 and 1024 x 480
  loaded, was 461 x 101 and 512 x 240); in the production client with the one jar: `menu update mainmenu menupages
  real=true jar=true from=0.59.0 expect=0.64.6` all PASS, the real 238 MB release downloaded in 19 s on Decimation's
  screen and swapped into mods/Decimation.jar. Shots docs/shots/update_v0.64.7 (shots_index.md).

- v0.64.8 (user: update screen polish, the release for watching a real auto update in Prism): the lines as Decimation's
  own updater filled them: "Downloading: DeciRemnant 0.64.8 (239.5mb)" (version and file size), "Downloading: 33%" then
  "Downloading: Complete!" (its lang key menu.updating.complete), the size line done before total ("80.0mb/239.5mb";
  the screen prints totalBytes first, so UpdateProgress puts done in totalBytes and the size in downloadedBytes).
  Dev test `update` checks "progress lines" and "ready lines" (Deci.updateLines), RED first. Shots
  docs/shots/update_v0.64.8.

## 3. Code map

| Piece | Job |
|---|---|
| fixes/MenuNews | Forge GuiScreenEvent.InitGuiEvent.Post on the home menu (Deci.isHomeMenu, no world): four panels through Deci.addNewsPanel, pictures first into ArticlePanel's picture map (Deci.newsPicture: a key missing there is downloaded as a URL every frame); client tick keeps the Updates line current (Deci.newsLine). Registered in DecimationWorldGen (client, both buses). |
| fixes/MenuBackground | the slideshow: `draw(x, y, picked, w, h, zoom)` replaces the menu's one picture draw (Deci.drawCentered, and the alpha one for the fade); `shown()` for tests. Finds background0.png, background1.png ... in Decimation's menu textures. |
| Deci | isHomeMenu, addNewsPanel (ArticlePanel deci.d.c, DeciScreen.addPanel deci.d.i.a), newsLine, newsPicture (deci.d.c.et), newsPanels (tests), menuShown (ClientState.bO and isMenuFadedOut), menuBackground (ClientState.bC), drawCentered (RenderUtil deci.j.a.a, two forms) |
| tools/patches/PatchMenuBackground.java | Javassist on RenderUtil.drawMenuBackground (deci.j.a.b(GuiScreen)): removes the black layer, the two cube draws and the six glow sprites, sends the picture draw to MenuBackground.draw. Three jars. |
| tools/menu_assets.py | writes background0..5.png (from docs/shots/site_v0.62.9), news_github / update / website / coming.png (from news_src), the corner donor_rank_0 / 1.png (Decimation's buttonbg plus tools/menu/MinecraftStandard.otf, public domain) and the welcome line of Decimation's lang file into the three jars; Decimation art, never committed |
| fixes/MenuTexts | our version (Updater.current without a dev suffix) and the window title, called from Decimation's code by PatchMenuTexts |
| fixes/MenuContinue | the Continue button: Forge InitGuiEvent.Post (Deci.menuButton, buttons 27, 11, 5, 0 moved down 21) and ActionPerformedEvent.Pre (launches the newest save) |
| tools/patches/PatchMenuTexts.java | ASM on the ORIGINAL classes plus PatchMenuStats (base.jar, section 4b): 47 texts in 12 classes since v0.64.1 (labels, title, version calls, link addresses, window title; status rows and hosts in deci/i/h; row state, "?/?" and " - 0ms" in ListElement deci/d/p; official line and Community tooltip in deci/i/i; popup, toast and profile texts in deci/d/k, i/a, i/e, i/k). Three jars. |
| fixes/MenuServers | the official list's rows (Bukovka, Chernarus on *.deciremnant.invalid, pictures by map key) through Deci.officialServer (ServerListEntry deci.k.a, setBackground y) |
| tools/patches/PatchOfficialServers.java | Javassist: ServerListPinger.officialServers (deci.k.b.bg) returns MenuServers.official(). Three jars. |
| fixes/UpdateProgress | the update on Decimation's progress screen: GuiOpenEvent swaps GuiMenuUpdates (deci.i.l) for it and starts Updater.download; client tick mirrors the download (Deci.setUpdateProgress) and falls back to UpdateScreen on a failure; InitGuiEvent.Post adds Quit game / Later when ready, DrawScreenEvent.Post draws them over the black layer, ActionPerformedEvent.Pre handles them. Registered on both buses. |
| tools/menu_assets.py logos() | banner.png 922 x 202 and banner_intro.png 1024 x 480 with our logo (source docs/shots/main_menu_v0.64.0/logo_src), the update screen's two lang lines |
| Deci (tests) | officialRows, showRowPicture (ListElement fH / fK), statusRows (deci.i.h iS..iV, ServerPinger deci.l.a), toasts (ToastNotification deci.b.b S, R, W), popupLines / clickPopupLine (DeciSubScreen deci.d.j fa, fb, g), menuTooltip (DeciMenuButton eN) |

## 4. Rebuild (after new site shots, before a release)

1. Patch from the ORIGINAL class (Decimation.jar.original.bak copied to a name ending in .jar), our classes compiled:
   ```bash
   cd dev && ./gradlew compileJava && cd ..
   mkdir -p build/patch_menubg/out && cp Decimation.jar.original.bak build/patch_menubg/original.jar
   javac -cp tools/lib/javassist.jar -source 8 -target 8 -d build/patch_menubg tools/patches/PatchMenuBackground.java
   java -cp tools/lib/javassist.jar:build/patch_menubg PatchMenuBackground build/patch_menubg/original.jar build/patch_menubg/out dev/build/classes/java/main
   (cd build/patch_menubg/out && zip <jar> deci/j/a.class)   # Decimation.jar.patched, dist/Decimation.jar, dev/libs/Decimation-base.jar
   ```
2. `python3 tools/menu_assets.py` (all three jars; `--only-dir DIR` to look at the pictures first).
3. Check: entry counts (the first run adds 7 entries: background3..5 and four news pictures), `unzip -tq`, the backup's
   hash unchanged. Checked 17 Oktober 2026: 21597 / 21591 / 21591 before the slideshow, +3 after; backup 57946011...

## 4b. Rebuild PatchMenuTexts (after PatchMenuStats, before menu_assets)

```bash
ASM=$(ls ~/.gradle/caches/modules-2/files-2.1/org.ow2.asm/asm-debug-all/5.0.3/*/asm-debug-all-5.0.3.jar | head -1)
javac -cp $ASM -source 8 -target 8 -d build/menutexts_rebuild/cls tools/patches/PatchMenuTexts.java
java -cp $ASM:build/menutexts_rebuild/cls PatchMenuTexts build/menutexts_rebuild/base.jar build/menutexts_rebuild/texts_out   # 47 texts in 12 classes
(cd build/menutexts_rebuild/texts_out && zip <jar> deci/i/a.class deci/i/e.class deci/i/g.class deci/i/i.class deci/i/j.class \
     deci/i/k.class deci/i/l.class deci/d/k.class deci/b/c.class deci/m/b.class deci/i/h.class deci/d/p.class)   # the three jars
# the official list's rows (v0.64.1): deci/k/b is the original class in every jar; Javassist reads only names ending in .jar
javac -cp tools/lib/javassist.jar --release 8 -d build/patch_servers tools/patches/PatchOfficialServers.java
java -cp tools/lib/javassist.jar:build/patch_servers PatchOfficialServers dist/Decimation.jar build/patch_servers/out dev/build/classes/java/main
(cd build/patch_servers/out && zip <jar> deci/k/b.class)   # the three jars
```
The input must be the ORIGINAL classes with only PatchMenuStats on them (this patch removes the texts it looks for):
copy Decimation.jar.original.bak to base.jar, run PatchMenuStats on it and zip its three classes back in, then run
PatchMenuTexts on base.jar (done 17 Oktober 2026 in build/menutexts_rebuild; base.jar's ten classes matched the jars;
deci/i/h and deci/d/p, added in v0.64.1, were the original classes in all three jars). Every text of the plan must
be changed exactly once in its class, or the patch stops and names it (per text since 17 Oktober 2026, after a code
review: a total per class could hide a missing text behind a doubled one; the stricter run gave the same 12 classes).

## 5. Tests

- `python3 tools/devtest.py --live mainmenu`: leaves the world, opens the home menu, waits for the welcome box and the
  fade, checks the four panels (titles in order, a line, an https address), saves mainmenu_home, waits for the next
  background picture (check: changes within 7 s, at least 2 pictures), saves mainmenu_next. RED first both times (no
  panels with the hook unregistered; "picture -1 -> -1 of 0" before the patch), then PASS (6 pictures, 0 -> 1 after
  122 ticks).
- v0.64.0 checks in `mainmenu` (RED first: "Discord ... Twitter", "Minecraft - Decimation v1.21.10f", no Continue; then
  PASS): the links say Website and GitHub and their addresses are ours, the window title is "DeciRemnant v0.63.0
  (Minecraft 1.7.10)", a Continue button is there.
- With it, `menu` (no forced update) and `stats` (menu counters) still PASS.
- `menupages` (v0.64.1, run it last like mainmenu: it leaves the world): My Community popup lines, the Early access
  click's toast, the official rows (Bukovka and Chernarus, both not reachable), "Coming soon" as the player count, all
  16 map pictures load as textures (not the missing texture), the Community tab's tooltip and toast, the status rows
  (first three Offline), and our texts in the loaded classes (profile card, lounge: nothing to click). Shots
  menupages_community, menupages_official (Chernarus' first picture shown as a hover shows it), menupages_status.
  RED first (every check FAIL on the old jars), then PASS: 9 of 9 with menu, stats and mainmenu (17 Oktober 2026).
  Trap found: a class check for "Supporter" also matched the field name isSupporter; check exact old texts.
- TRAP: `mainmenu` ends at the title menu with no world; tests that wait for a player (`menu`, `stats`, most others)
  then wait forever and `--live --stop` times out (the quit waits for the run). Run them before `mainmenu`, or restart
  the live game.

## 6. Open

- The user's look in their own game: released in 0.64.6, the update screen and logo in 0.64.7 (17 Oktober 2026,
  GitHub pre-releases). Players on 0.59.0 .. 0.64.6 install 0.64.7 by hand once (bug.md); the dev client shows it too.
- Still dead and kept (not picked): My Community, Server Status, "Not Supporter :(", and Play still opens the dead
  official server list first (menus.md section 7). Since v0.64.1 those pages say coming soon (section 2c).
- Later (user): a forum on our website for the My Community popup's Forums line (roadmap UI 3); Global Server,
  Community Server, Download and the Bukovka / Chernarus rows wait for real servers (MenuServers holds the rows).
- Answered by the user (17 Oktober 2026): toasts say DeciRemnant and the Website row pings our site (both v0.64.7);
  the red X icon of an unreachable row stays ("no problem"); the Chernarus pictures are not the user's, their creator
  allowed their use; credited in the 0.64.7 release notes (creator's name not known here).
- The Decimation logo (banner.png) on the website: built 17 Oktober 2026 (site fc0d159), live after the user's push.

## 7. References looked at first (skill decimation-website lesson 17)

- Minecraft's title screen: six square panorama faces (front, right, back, left, up, down), the camera turns inside
  them; vanilla 1.7.10 renders them at 256 x 256 with 64 jittered passes and 7 blur passes, pitch sway sin(t / 400)
  * 25 + 20, yaw 0.1 per tick (dev/build/rfg GuiMainMenu.drawPanorama). Panorama mods take the faces at 90 FOV.
- Menu advice found: subtle motion keeps a background alive; darken behind the buttons so they read.
- Launcher news: a picture plus a small info box per item; players found oversized news areas cluttered.
- Taken: real scenes, slow motion, dark behind the buttons, small picture news panels. The panorama was not built:
  the user asked to use Decimation's own menu parts; a camera turning over a city stays an option.

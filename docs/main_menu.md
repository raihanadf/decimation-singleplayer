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
| tools/patches/PatchMenuTexts.java | ASM on the jar's CURRENT classes (deci/i/a and i/e carry PatchMenuStats): 20 texts in 10 classes (labels, title, version calls, link addresses, window title). Three jars. |

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
javac -cp $ASM -source 8 -target 8 -d build/patch_menutexts tools/patches/PatchMenuTexts.java
java -cp $ASM:build/patch_menutexts PatchMenuTexts dist/Decimation.jar build/patch_menutexts/out   # 20 texts in 10 classes
(cd build/patch_menutexts/out && zip <jar> deci/i/a.class deci/i/e.class deci/i/g.class deci/i/i.class deci/i/j.class \
     deci/i/k.class deci/i/l.class deci/d/k.class deci/b/c.class deci/m/b.class)   # the three jars
```
The input must be the ORIGINAL classes with only PatchMenuStats on them (this patch removes the texts it looks for):
copy Decimation.jar.original.bak to base.jar, run PatchMenuStats on it and zip its three classes back in, then run
PatchMenuTexts on base.jar (done 17 Oktober 2026 in build/menutexts_rebuild; base.jar's ten classes matched the jars).

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
- TRAP: `mainmenu` ends at the title menu with no world; tests that wait for a player (`menu`, `stats`, most others)
  then wait forever and `--live --stop` times out (the quit waits for the run). Run them before `mainmenu`, or restart
  the live game.

## 6. Open

- The user's look in their own game: their Prism instance has the old jars (Prism is off limits for us); a release
  (tools/onejar.py from dist/Decimation.jar) carries this. The dev client shows it now.
- Still dead and kept (not picked): My Community, Server Status, "Not Supporter :(", and Play still opens the dead
  official server list first (menus.md section 7).
- The Decimation logo (banner.png) on the website: built 17 Oktober 2026 (site fc0d159), live after the user's push.

## 7. References looked at first (skill decimation-website lesson 17)

- Minecraft's title screen: six square panorama faces (front, right, back, left, up, down), the camera turns inside
  them; vanilla 1.7.10 renders them at 256 x 256 with 64 jittered passes and 7 blur passes, pitch sway sin(t / 400)
  * 25 + 20, yaw 0.1 per tick (dev/build/rfg GuiMainMenu.drawPanorama). Panorama mods take the faces at 90 FOV.
- Menu advice found: subtle motion keeps a background alive; darken behind the buttons so they read.
- Launcher news: a picture plus a small info box per item; players found oversized news areas cluttered.
- Taken: real scenes, slow motion, dark behind the buttons, small picture news panels. The panorama was not built:
  the user asked to use Decimation's own menu parts; a camera turning over a city stays an option.

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

  | Panel | Line | Opens | Picture |
  |---|---|---|---|
  | GitHub | Source code, issues, all releases | the public repo | our MAC-10 in hand |
  | Updates | follows fixes/Updater: "Checking GitHub releases", "Up to date: 0.62.9" (dev suffix cut), "New version X is out", "Release check failed, see GitHub" | the newest release's page, else the release list | the airport |
  | Website | Download, install and server guides | deciremnant.vercel.app | a survivor camp |
  | Coming soon | What we are building next | the site's #coming | a base at sunset |

  Main menu only: in a world the same screen is the pause menu, no panels there.
- **Background**: Decimation's own background drawing (one picture with its slow sway, slow zoom in and out and the
  mouse parallax, its drifting fog, side shadow and dark top and bottom), now showing six real scenes of our world one
  after another, 7 s each, the next fading in over 1.2 s, starting with the one Decimation picked for the launch. The
  opaque black layer that hid Decimation's own pictures, the rotating lava "earth" cube, the white "sun" cube and its
  six glow sprites are gone. Scenes (from the website's shots, dimmed to 62 % and 80 % colour): a city from above, a
  city refuge among towers, a wasteland gate street, a bridge over a canal, a base at sunset, the airport.

## 3. Code map

| Piece | Job |
|---|---|
| fixes/MenuNews | Forge GuiScreenEvent.InitGuiEvent.Post on the home menu (Deci.isHomeMenu, no world): four panels through Deci.addNewsPanel, pictures first into ArticlePanel's picture map (Deci.newsPicture: a key missing there is downloaded as a URL every frame); client tick keeps the Updates line current (Deci.newsLine). Registered in DecimationWorldGen (client, both buses). |
| fixes/MenuBackground | the slideshow: `draw(x, y, picked, w, h, zoom)` replaces the menu's one picture draw (Deci.drawCentered, and the alpha one for the fade); `shown()` for tests. Finds background0.png, background1.png ... in Decimation's menu textures. |
| Deci | isHomeMenu, addNewsPanel (ArticlePanel deci.d.c, DeciScreen.addPanel deci.d.i.a), newsLine, newsPicture (deci.d.c.et), newsPanels (tests), menuShown (ClientState.bO and isMenuFadedOut), menuBackground (ClientState.bC), drawCentered (RenderUtil deci.j.a.a, two forms) |
| tools/patches/PatchMenuBackground.java | Javassist on RenderUtil.drawMenuBackground (deci.j.a.b(GuiScreen)): removes the black layer, the two cube draws and the six glow sprites, sends the picture draw to MenuBackground.draw. Three jars. |
| tools/menu_assets.py | writes background0..5.png and news_github / update / website / coming.png into Decimation's menu textures in the three jars (from docs/shots/site_v0.62.9); Decimation art, never committed |

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

## 5. Tests

- `python3 tools/devtest.py --live mainmenu`: leaves the world, opens the home menu, waits for the welcome box and the
  fade, checks the four panels (titles in order, a line, an https address), saves mainmenu_home, waits for the next
  background picture (check: changes within 7 s, at least 2 pictures), saves mainmenu_next. RED first both times (no
  panels with the hook unregistered; "picture -1 -> -1 of 0" before the patch), then PASS (6 pictures, 0 -> 1 after
  122 ticks).
- With it, `menu` (no forced update) and `stats` (menu counters) still PASS.
- TRAP: `mainmenu` ends at the title menu with no world; tests that wait for a player (`menu`, `stats`, most others)
  then wait forever and `--live --stop` times out (the quit waits for the run). Run them before `mainmenu`, or restart
  the live game.

## 6. Open

- The user's look in their own game: their Prism instance has the old jars (Prism is off limits for us); a release
  (tools/onejar.py from dist/Decimation.jar) carries this. The dev client shows it now.
- Untouched (user: nothing new): My Community, Server Status, the FURY store advert, Discord / Twitter, "Not
  Supporter :(" (all dead, menus.md section 7).
- The Decimation logo (banner.png) on the website, later (user).

## 7. References looked at first (skill decimation-website lesson 17)

- Minecraft's title screen: six square panorama faces (front, right, back, left, up, down), the camera turns inside
  them; vanilla 1.7.10 renders them at 256 x 256 with 64 jittered passes and 7 blur passes, pitch sway sin(t / 400)
  * 25 + 20, yaw 0.1 per tick (dev/build/rfg GuiMainMenu.drawPanorama). Panorama mods take the faces at 90 FOV.
- Menu advice found: subtle motion keeps a background alive; darken behind the buttons so they read.
- Launcher news: a picture plus a small info box per item; players found oversized news areas cluttered.
- Taken: real scenes, slow motion, dark behind the buttons, small picture news panels. The panorama was not built:
  the user asked to use Decimation's own menu parts; a camera turning over a city stays an option.

---
name: decimation-main-menu
description: Use when adding, fixing, removing or updating anything in Decimation's main menu as our mod shows it, on the title screen or in game (the Esc pause menu): buttons, texts, version, links, news panels, background, profile card, supporter line, My Community popup, Server Status rows, official and Community server lists, the update check, prompt and progress screens, toasts and dialogs.
---

# The main menu (home and in game)

Every piece, its Decimation code and ours: docs/main_menu.md section 8 (map), 9 (how to change it), 10 (two lines).
Decimation's own internals: docs/decimation_code/menus.md sections 7 and 7b (skill decimation-code-menus). The
history of what the user asked: docs/main_menu.md sections 1 to 2f.

## Workflow
1. Read docs/main_menu.md section 8 for the piece; find its row (Decimation class, our class or patch, test).
2. New screen, page or look: study 3 to 5 famous examples first and say which patterns you take (user rule); show the
   user before building anything they will look at.
3. Write the dev test check first (menu, mainmenu, menupages, update, stats; `python3 tools/devtest.py --live MODE`),
   watch it FAIL, build, watch it PASS. Unit tests for anything that is plain Java (dev/src/test).
4. Build with Decimation's own parts (Deci.menuButton, ConfirmScreen deci/d/s, Deci.toast, ArticlePanel news panels,
   its progress screen) through Forge events; a jar patch only when a drawn element or a constant must change.
5. Jar patch: PatchMenuTexts (texts), PatchMenuBackground (drawing), PatchOfficialServers, PatchNoUpdate; rebuild from
   the right base (docs/main_menu.md 4b) into the THREE jars, then `python3 tools/linecheck.py` (two lines).
6. Docs: docs/main_menu.md (a dated section with the user's words, the map row), bug.md for a bug, docs/tasks.md; shot
   into a docs/shots folder named main_menu_v plus the version, described in docs/shots_index.md; graph; this skill's lessons and casebook.
7. Main only (multiplayer) or plain? Plain fixes are carried to prerelease ("from main abc1234"); main only pieces
   (Community page, Host Online, the Multiplayer news panel, the relay) never go there.

## Quick reference

| Task | Where |
|---|---|
| Play / Update / Resume Game button, update notice | GuiMenuBase.addMenuButtons, UpdateChecker.isUpdateRequired (PatchNoUpdate: fixes/Updater.required) |
| new button on the home menu | fixes/MenuContinue as the pattern: InitGuiEvent.Post + Deci.menuButton, ActionPerformedEvent.Pre |
| a Decimation text, link, version, toast title | tools/patches/PatchMenuTexts.java, fixes/MenuTexts |
| news panels | fixes/MenuNews, Deci.addNewsPanel, Deci.newsLine; pictures tools/menu_assets.py |
| a news panel's popup (carousel, release list) | fixes/MenuDetail, fixes/MenuDetails, MenuNews.open; slide pictures menu_assets.py DETAILS |
| corner art bottom right, its click | menu_assets.py corner() from `python3 tools/devtest.py --live --window=1440x810 cornerart`; fixes/MenuCorner |
| background slideshow | fixes/MenuBackground, tools/patches/PatchMenuBackground.java |
| profile numbers, totals line | fixes/LocalStats, tools/patches/PatchMenuStats.java |
| official server list rows | fixes/MenuServers, tools/patches/PatchOfficialServers.java, Deci.officialServer |
| Server Status rows | GuiMenuServerStatus (deci/i/h) rows ServerPinger (deci/l/a); fixes/MenuStatus, Deci.setStatus, Deci.statusRows |
| updates: check, question, progress, failure, quitting | fixes/Updater, fixes/UpdatePrompt, fixes/UpdateProgress, fixes/UpdateScreen, fixes/UpdateBar, fixes/UpdateSwap, fixes/SafeQuit |
| a question to the player | ConfirmScreen (deci/d/s) via Deci.confirm: lines, left (red, id 10) and right (id 11) labels; clicks in ActionPerformedEvent.Pre |
| in game pause menu, hosting (main only) | GuiMenuHome with a world; fixes/hosting/HostMenu, fixes/hosting/CommunityList |
| tests | `python3 tools/devtest.py --live menu update mainmenu`, `menupages`, `stats`, `hostmenu` |

## Lessons (from the user's revisions)
1. Use Decimation's own components, never a look of our own beside them (17 Oktober 2026: "just use component from
   decimation gui menu its self").
2. Our name is DeciRemnant, our version and our links replace Decimation's; Decimation's own mod version stays.
3. What does not exist yet says "Coming soon"; status rows of things not running say Offline (v0.64.1).
4. The update runs on Decimation's own progress screen (v0.64.7). Updates are the player's choice, never forced, and a
   client the updater cannot replace gets a link to download the new jar (20 Oktober 2026). Dev and test builds never
   offer an update (20 Oktober 2026).
5. "Supporter" stays Decimation's word: it is for people who donate to the user (20 Oktober 2026: "rewind it to
   supporter"); Early access is gone.
6. Server Status rows come from real sources: Download from the GitHub release link, Community Server from the
   Cloudflare relay, Global Server from the official server list (community run dedicated servers, not the user's own);
   later an upvote rating ranks popular servers on top (20 Oktober 2026).
7. No dash characters in any text the player reads (user rule); check every new string.
8. The update question comes on the title menu, or on the Esc menu when the player is in a world (user, 21 Oktober
   2026: "yes"), once a launch. Anything that closes the game from a world goes through fixes/SafeQuit (leave, save,
   then close): mc.shutdown inside a world once broke a level.dat.
9. Supporter tier: for the user's donors, through the website (login, donation; docs/ideas.md 23), not designed yet.
10. Promise little: Coming soon shows only what is being built (two items, user 21 Oktober 2026), on the menu and the
    website alike.
11. Our name and links everywhere in the menu; BoehMod is credited on the website only (user, 21 Oktober 2026).
12. Art of our own comes from the game itself: the corner's soldier is a real NPC drawn by dev test cornerart, the
    layout learned from Decimation's FURY art; mock up on the user's own screenshot first.

## Common mistakes
- Testing after `mainmenu` or `menupages` in the same live game: they leave the world, the next mode waits for a
  player forever and `--stop` times out. Run them last.
- Clicking the home menu in its first 140 frames of a launch: GuiMenuHome ignores clicks until ClientState.cg <= 0.1.
- Forgetting that the title menu and the pause menu are ONE screen (GuiMenuHome): a change shows in both unless it
  checks `mc.theWorld`.
- Reading deobf/src for current texts: it is Decimation's original code, our patches are not in it.
- Patching PatchMenuTexts from a jar it already ran on (its texts are gone there), or matching a text that is also a
  field name ("Supporter" matched isSupporter): check exact old strings.
- A Server Status row on a host that resolves but never answers: ServerPinger has no timeout (75 s of "Pinging...").
- A news picture key missing from the picture map: Decimation downloads the key as a URL every frame.
- Work that runs every frame doing more than reading state: menu and HUD code runs each frame.
- Pressing a button with DevTestUtil.press: it calls actionPerformed directly and skips Forge's ActionPerformedEvent,
  so our handlers never see it; DevTestUtil.click clicks like a player.
- Testing a news panel (or any of Decimation's panels) through a shortcut: a real click arrives inside
  DeciScreen.mouseClicked's loop over the panels, and only on a panel hovered in the last frame. Draw the screen with
  the mouse over it, then DevTestUtil.clickAt. Never change screens or re-init a DeciScreen inside that loop: switch on
  the next tick (Minecraft.func_152344_a). bug.md "Clicking a news popup crashed the game" (the user found it).
- Calling a static method of a GuiScreen subclass from a unit test: GuiScreen's static init needs LWJGL (not on the
  test classpath); keep testable logic in a plain class (UpdatePrompt.screenLabels for UpdateScreen).

## Casebook (never delete a case)

### Case 1: our main menu, v0.63.0 to v0.64.8 (17 Oktober 2026)
- News panels from Decimation's ArticlePanel, the scene slideshow, links, name and version, Continue, our corner,
  server pages "Coming soon", Early access, toasts titled DeciRemnant, the update on Decimation's progress screen, our
  logo. docs/main_menu.md sections 2 to 2d; dev tests mainmenu and menupages.

### Case 2: two lines, update offers, this skill (20 Oktober 2026)
- User: "why the it show update?" in the dev client: main builds (version 0.64.8-main.N) took the 0.65.0 pre-release
  for an update. Only release builds look now ("Updates off: dev build (main)", version "0.64.8 dev"); bug.md.
- User asked for this skill ("just like other skills we have") and, next: updates the player can decline with a
  download link fallback, Supporter back instead of Early access, Server Status rows from real sources.

### Case 3: the update question, Supporter, real status rows (21 Oktober 2026)
- Built with Decimation's own parts: ConfirmScreen for the question, its Settings / View Updates button for later, its
  ServerPinger states for the rows (Unknown drawn "Coming soon"); one PatchMenuTexts rebuild per change, compared class
  by class with the last output (only the meant classes changed), then tools/linecheck.py (26 hooks, PASS).
- The user's own dev client was running in main: tests ran in a temporary third checkout and in the prerelease
  worktree (own build folders and ports); never Gradle in the checkout whose game is open.
- Main and prerelease differ only where multiplayer is: Community Server is the relay on main, Coming soon on
  prerelease (MenuStatus.community is set by main's hosting code). docs/main_menu.md 2g; shots
  docs/shots/main_menu_v0.65.0/.
- User checked it in game ("nice") and pushed; then "yes" to the question on the Esc menu too: built with SafeQuit
  for Quit game from a world (update inworld=true RED then PASS on both lines).

### Case 4: popups, Discord, our footer, the corner (21 Oktober 2026)
- User's screenshots of the menu: popups with a carousel for the news panels, Discord instead of GitHub and Forums,
  our footer, "learn FURY and make our version". Mockups on the user's own screenshot first (PIL, the game's font and
  Decimation's torn button textures tinted as its code tints them), then built: docs/main_menu.md 2h.
- Found on the way: a resource lookup opens a stream (cache it, never per frame); Decimation tints buttonbg_red in
  code (the file is white); a 2.32 : 1 picture loses its edges when cut to 16:9 (letterbox it); Decimation draws the
  corner 150 x 150 GUI units, which reaches into the news panels at 854 x 480.

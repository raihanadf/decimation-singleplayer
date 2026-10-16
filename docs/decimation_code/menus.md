# Decimation code: menus, screens and HUD

Part of the feature study of Decimation's own code (16 Oktober 2026, index docs/decimation_code/README.md).

## 1. What it is for

Decimation replaces Minecraft's main menu with its own (news, profile, server lists, updates, supporter lounge),
has its own inventory and loot screen, trader, banker, attachment, crafting and lockpicking screens, and draws its
own HUD (conditions, zone and turf banners, ammo, compass, gas mask view) with toasts and one time tips.

## 2. Code map

| Class (obf) | Job |
|---|---|
| GuiMenuBase (deci/i/a) | base of every custom menu page: background, banner, profile card, the casualty totals line; `addMenuButtons` |
| GuiMenuHome (deci/i/e) | home page: news from the backend (dead), welcome notification, combat timer, loading fade, the player preview (deci/e MenuFakeWorld), "Play" or "Update" |
| GuiMenuOptions, GuiMenuProfile, GuiMenuServerStatus, GuiMenuOfficialServers, GuiMenuCommunityServers, GuiMenuSupporterLounge, GuiMenuUpdates, GuiUpdateProgress (deci/i/f to m) | the other pages; server pages ping BoehMod's dead servers; updates read a GitHub updates.json |
| GuiEulaAgreement, GuiIntroBoehModLogo, GuiIntroJumpscare (deci/i/b, c, d) | first start EULA, boot intros |
| UpdateChecker (net/decimation/mod/common/utils/h) | `isUpdateRequired` (gI): "Play" or "Update"; `tickVersionCheck`; our PatchNoUpdate answers through fixes/Updater |
| DeciScreen (deci/d/i), MenuPanel, ScrollPanel (deci/d/l), PanelElement, DeciButton | the menu toolkit (all methods named in m_methods_rest.tsv) |
| PlayerInventoryScreen (deci/g/g) | the inventory with gear slots and the loot area (GUI_LOOT) |
| TraderScreen (deci/g/q), BankerScreen (deci/g/c), AttachmentScreen (deci/g/a), LockpickingScreen (deci/g/k) | trader, banker, gun attachments (writes the attachment NBT), lockpicking minigame |
| IngameHud (deci/g/j) | singleton HUD: conditions, stats, compass and zone info, turf banner and chat line, ammo; `render` once a frame |
| ToastNotification (deci/b/b), TipPopup (deci/b/d), BlockTipLayer | sliding notices, one time tips (kept in a local file), floating hints over blocks |
| GUI handler (net/decimation/mod/common/handlers/a) | GUI ids (DeciConstants) to containers and screens |

## 3. As it runs (dev tests `menu` + `stats`, call logger, 16 Oktober 2026)

Traced `deci.i,net.decimation.mod.common.utils.h,deci.g.j,net.decimation.fixes.LocalStats,net.decimation.fixes.Updater`:
- home menu: GuiMenuHome.drawScreen 161 frames (drawMenuBars, drawWelcomeNotification, drawLoadingFade each frame),
  UpdateChecker.isUpdateRequired 168 times -> fixes/Updater.required 168 (PatchNoUpdate v2): "Play" shown (checked);
- in game: IngameHud.render 361 frames, isBetween 7220 (about 20 zone box tests a frame against the client's zone
  list); LocalStats.onClientTick 552 puts our counters into the menu profile and totals line (checked: 48 human, 419
  infected kills shown).

## 4. Singleplayer state and known faults

- The menus work offline; everything they fetch from BoehMod (news, profile, server lists, supporter state) is gone:
  our LocalStats fills the profile numbers, PatchMenuStats renames the labels, the session flag is set valid
  (green Play).
- Without mods/DecimationVoiceChat.jar Decimation's menu demanded an update: PatchNoUpdate; our own update check
  (fixes/Updater, UpdateScreen) replaces it.
- The boot jumpscare intro played its frames from a busy wait thread (the one off intro skip patch).
- MenuFakeWorld's overrides of Forge world methods were renamed by the obfuscator and never run (the `...Lost`
  methods, architecture.md "Method naming pass").

## 5. How our mod uses it

| Need | Use |
|---|---|
| "Play" without the voice chat jar, our updates | tools/patches/PatchNoUpdate.java, fixes/Updater, fixes/UpdateScreen, fixes/UpdateSwap |
| stats on menu and HUD | fixes/LocalStats, tools/patches/PatchMenuStats.java |
| our loot inventory on Decimation's loot screen | fixes/Loot (loot.md) |
| turf banners for our places | worldgen/PlaceNames, PatchTurfNames (clans.md) |
| our news in the main menu's news panels (GitHub, Updates, Website, Coming soon) | fixes/MenuNews, `Deci.addNewsPanel` (docs/main_menu.md) |
| real scenes as the menu background, a new one every 7 s | fixes/MenuBackground, tools/patches/PatchMenuBackground.java, tools/menu_assets.py |
| our links, name and version in the menu, the window title | tools/patches/PatchMenuTexts.java, fixes/MenuTexts |
| Continue (the last world) under Play | fixes/MenuContinue, `Deci.menuButton` |

## 6. Checks

- `python3 tools/devtest.py --live menu` ("Play" not "Update"), `stats` (local counters on menu and HUD), `update`
  (needs tools/fake_release_server.py), `loot` (the loot screen).
- `python3 tools/devtest.py --live mainmenu` (our four news panels, the background changes within 7 s); run it after
  the tests that need a player: it leaves the world.

## 7. The main menu, element by element (17 Oktober 2026; the user's Prism shot docs/shots/main_menu_v0.62.9/)

How it opens: `ClientEventHandler.onGuiOpen` swaps Minecraft's title screen for GuiIntroJumpscare (once), then
GuiIntroBoehModLogo (once), GuiEulaAgreement (until accepted), GuiUpdateProgress (while Decimation's own updater
downloads), else GuiMenuHome. Esc in a world (GuiIngameMenu) also becomes GuiMenuHome: the same screen is the pause
menu, with Resume Game, Disconnect and LAN / achievement icons instead of Play and Exit Game.

Layout (GUI units, x0 = width / 2 - 173), all drawn by `GuiMenuBase.drawScreen` unless named:
| Element | Code | Works in singleplayer |
|---|---|---|
| background scene | `RenderUtil.drawMenuBackground` (below) | yes |
| DECIMATION banner 160 x 35 at (5, 3), swaying; "v1.21.10f" | textures/gui/menu/banner.png | yes (Decimation's logo) |
| "Decimation - Zombie Apocalypse" / page title at x 170 | `GuiMenuBase.setTitle` ("Main Menu") | yes |
| Discord / Twitter text links top right | button ids 12, 15: BoehMod's URLs | outside links, BoehMod's [not checked if alive] |
| profile card at (x0 + 1, 47), 100 x 38: name, Human Kills (K/D), Deaths, Zombie Kills, "Not Supporter :(", the player model | `RenderUtil.drawMenuPlayer`, MenuPreviewWorld | yes (LocalStats numbers); supporter line dead |
| home icon + Play (green) at (x0, 86) | `GuiMenuBase.addMenuButtons`: Play / Update / Resume Game / Play Offline by state | yes: Play opens the official server list |
| My Community >> | id 27: SupporterOnlyScreen (supporters only) | dead |
| Server Status >> | id 11: GuiMenuServerStatus (BoehMod servers and websites) | dead |
| Settings >> | id 5: GuiMenuOptions | yes |
| Exit Game (Disconnect in a world) | id 0: confirm, quit (id 24 in a world) | yes |
| BoehMod logo bottom left at 15 %, copyright line | ClientResources, `GuiMenuBase.drawScreen` | yes |
| "Total Human Kills / Total Infected Kills" bottom centre | PatchMenuStats labels, LocalStats numbers | yes |
| FURY "Join the fury" rank advert bottom right, 150 x 150, pulsing | textures/gui/menu/donor_rank_0.png, donor_rank_1.png | dead (BoehMod's store) |
| news article panels | `GuiMenuHome.initGui`, only when the backend is connected | never shown |

Background, back to front (`RenderUtil.drawMenuBackground`): one of textures/gui/menu/background0..2.png (red tinted
Decimation scenes, 960 x 540, picked at random each launch: `ClientState.bC`) with sway and mouse parallax; fog
(`RenderUtil.drawMenuFog`, background_fog.png); a black layer that hides the scene in the user's shot [inferred from
the shot]; two rotating CUBES (CubeModel): the "earth" (textures/model/gui/earth.png, 4096 x 2048 lava rock, size
130 x GUI scale) at 1/5 of the width and the "sun" (plain white, 15 x GUI scale) at 4/5 with glow sprites; then
sideshadow.png and dark gradients top and bottom. The big lava rock on the left of the shot is that earth cube, the
white diamond on the right the sun cube.

First open in a launch: a welcome box ("Ohey! You there!", "Welcome to Decimation v1.21.10f! Have fun!", Esc or 10 s)
over a loading fade with banner_intro.png ("A BoehMod Original") (`GuiMenuHome.drawWelcomeNotification`,
`GuiMenuHome.drawLoadingFade`).

Other art in the jar for menus: serverbackgrounds/newyork0..7 and bukovka0..7 (1920 x 1017 shots of the official maps,
shown behind hovered servers), banner_battlegrounds, background_fade, buttonbg / buttonbg_red (the torn buttons the
website uses). Contact sheet: docs/shots/main_menu_v0.62.9/menu_assets_sheet.png.

Changing it: done in v0.63.0 with Decimation's own parts (docs/main_menu.md): its news panels added from a Forge
InitGuiEvent (no jar patch), its background drawing patched (`RenderUtil.drawMenuBackground`, three jars).

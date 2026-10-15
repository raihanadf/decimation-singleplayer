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

## 6. Checks

- `python3 tools/devtest.py --live menu` ("Play" not "Update"), `stats` (local counters on menu and HUD), `update`
  (needs tools/fake_release_server.py), `loot` (the loot screen).

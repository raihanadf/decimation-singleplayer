---
name: decimation-code-menus
description: Use when changing or debugging Decimation's main menu (Play or Update button, news, profile, server lists, update pages), its in game screens (inventory and loot, trader, banker, attachments, lockpicking) or its HUD, toasts and tips, or when our code must show something in them (stats, turf banners, our inventory on the loot screen).
---

# Decimation menus, screens and HUD code

Facts and the traced menu and HUD: docs/decimation_code/menus.md.

## Quick reference

| Task | Where |
|---|---|
| main menu pages | GuiMenuBase / GuiMenuHome (deci/i/a, deci/i/e) and deci/i/f..m |
| Play or Update | UpdateChecker.isUpdateRequired (net/decimation/mod/common/utils/h) -> our fixes/Updater (PatchNoUpdate) |
| HUD | IngameHud (deci/g/j), once a frame |
| loot and gear screen | PlayerInventoryScreen (deci/g/g), GUI id GUI_LOOT |
| stats shown | fixes/LocalStats, PatchMenuStats |
| our main menu news and background slideshow | fixes/MenuNews, fixes/MenuBackground, PatchMenuBackground, tools/menu_assets.py (docs/main_menu.md) |
| our links, name, version, Continue | PatchMenuTexts, fixes/MenuTexts, fixes/MenuContinue (docs/main_menu.md 2b) |
| official server list rows, map pictures | ServerListPinger.officialServers (deci/k/b.bg) -> fixes/MenuServers (PatchOfficialServers); rows ListElement (deci/d/p); pictures serverbackgrounds/<map>0..7 (menus.md 7b) |
| status page, My Community popup, toasts | GuiMenuServerStatus (deci/i/h), SupporterOnlyScreen (deci/d/k), ToastNotification (deci/b/b); texts by PatchMenuTexts (docs/main_menu.md 2c) |

## Rules

1. Menu and HUD code runs every frame: keep anything we add there cheap; read state, never search the world.
2. Decimation's menus expect BoehMod's backend: whatever a page shows from it must come from our own data
   (LocalStats does that for stats).
3. Test live: `python3 tools/devtest.py --live menu` and `stats`; screenshots on the sheet show the result.
4. Screens closed by the test harness: set `DevAutoTest.keepScreen` while a test needs a screen open.
5. Change Decimation's menu with its OWN parts (user, 17 Oktober 2026: "just use component from decimation gui menu its
   self"): its news panels (ArticlePanel through `Deci.addNewsPanel`), its background drawing, its textures; no new
   widgets.
6. Texts we put into the game carry no dash either (the user's rule for everything we write): "DeciRemnant: Zombie
   Apocalypse" and "DeciRemnant v0.64.0 (Minecraft 1.7.10)", not Decimation's " - " style. Scan patched strings too.
7. A page with no service behind it says so plainly (user, 17 Oktober 2026): "Coming soon" on the official rows, the
   player count and the Community tab, Offline on the status rows, "Early access is coming soon!". Rows and pingers
   point at hosts ending in .invalid (RFC 6761: never resolves), so nothing waits on the network and no stranger's
   domain gets our players' connections.

## Common mistakes

- Forgetting the client turf and zone lists feed the HUD banner: no list sent, no banner.
- Changing labels by editing Decimation's strings in code: they are compiled constants; PatchMenuStats rewrites
  them in the jar (three jars).
- Checking a patched class for an old word ("Supporter") with a byte search: field names match too (isSupporter).
  Search for the exact old text ("Not Supporter :(").
- Trusting Decimation's own row drawing: ListElement drew "?/?" over every unreachable row's state, and the status
  page's pinger has no timeout (a dead host that resolves sits at "Pinging..." for over a minute).

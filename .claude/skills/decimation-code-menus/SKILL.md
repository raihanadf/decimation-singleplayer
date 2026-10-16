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

## Rules

1. Menu and HUD code runs every frame: keep anything we add there cheap; read state, never search the world.
2. Decimation's menus expect BoehMod's backend: whatever a page shows from it must come from our own data
   (LocalStats does that for stats).
3. Test live: `python3 tools/devtest.py --live menu` and `stats`; screenshots on the sheet show the result.
4. Screens closed by the test harness: set `DevAutoTest.keepScreen` while a test needs a screen open.
5. Change Decimation's menu with its OWN parts (user, 17 Oktober 2026: "just use component from decimation gui menu its
   self"): its news panels (ArticlePanel through `Deci.addNewsPanel`), its background drawing, its textures; no new
   widgets.

## Common mistakes

- Forgetting the client turf and zone lists feed the HUD banner: no list sent, no banner.
- Changing labels by editing Decimation's strings in code: they are compiled constants; PatchMenuStats rewrites
  them in the jar (three jars).

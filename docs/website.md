# DeciRemnant: the project website (spec)

Spec written 16 Oktober 2026 from the design talk with the user (brainstorming, mockups in the browser
companion). Status: SPEC, waiting for the user's review; nothing built yet. Roadmap: docs/roadmap.md UI 2.
Skill: .claude/skills/decimation-website (lessons and casebook from the design talk).

## 1. What it is

A one page website for this project: what it adds to Decimation, a download button for the newest release,
a full install guide, screenshots of what we built, what is coming, the changelog and help.

- Name: **DeciRemnant** (user choice). Address `deciremnant.vercel.app` [free or taken: not verified until the
  Vercel project is made]. Private GitHub repo `deciremnant`.
- Audience: Minecraft 1.7.10 players who want Decimation in singleplayer; old Decimation players.
- v1 success: a player finds the page, downloads the right jar and gets into a Decimation world without asking.
- Later (not v1): the community server list on the site and in Decimation's server menu, maybe live server
  status (section 11).

## 2. Decisions (user answers, 16 Oktober 2026)

| Topic | Decision |
|---|---|
| v1 scope | download + guide; the server list comes later |
| Hosting | Vercel |
| Build | Next.js with a server function (GitHub release data fetched on the server, cached) |
| Code and screenshots | a separate PRIVATE repo; this public repo keeps its rule: no Decimation jars, code or art |
| Language | English |
| Pictures | a few in game screenshots of our work (6 to 10 plus the hero), credited to Decimation by BoehMod |
| Sections | what it adds, screenshots, coming soon, install guide, changelog, help |
| Look | warm night (dark brown, clay), the Minecraft GUI font at the game's sizes, real item icons, the yellow splash text, Decimation's torn orange and black buttons; no grass strip |
| Sharing | releases are public (CLAUDE.md and documentation.md updated: no longer "not for redistribution") |
| Name | DeciRemnant |

## 3. The page, top to bottom

The approved mockup is `page-full-v4.html` in `.superpowers/brainstorm/60609-1792139172/content/` (local, git
ignored: it holds Decimation art). Pictures of the mockups: docs/shots/website_design_v0.62/ (shots_index.md).

1. **Hero**: full width screenshot (a generated city from above), darkened toward the bottom.
   - Top bar: brand "DeciRemnant" left; links Features, Screenshots, Install, Changelog, GitHub.
   - Label "Minecraft 1.7.10 · unofficial", title "Decimation, alive again in singleplayer".
   - Splash text in yellow, turned 20 degrees, pulsing like the game's title screen, one picked at random per
     visit (click: next one): "Now with airports!", "Up to 64 districts!", "No dead servers!", "Mind the
     landmines!", "Traders are open!".
2. **Download**: lead text "Fixes that make BoehMod's zombie mod work without its dead servers, plus a whole
   generated world: cities, military bases, airports and safe zones." Torn orange button "Download <version> ·
   <size>" with the supply drop radio icon; torn black button "Install guide >>"; a line "Pre-release ·
   <date> · Minecraft 1.7.10 with Forge · All versions on GitHub".
3. **What it adds** ("A world worth surviving in"), six cards, icon in an inventory slot:

   | Icon (Decimation item) | Title | Text |
   |---|---|---|
   | bottlecap | Singleplayer that works | Loot, zones, supply drops, armour and humanity, all without Decimation's dead servers. |
   | map | Generated cities | 4 to 64 districts each, joined by highways, an underground railway and rivers. |
   | firstaidkit | Safe zones | Survivor camps and city refuges with traders, guards and a clinic. |
   | militarykeycard | Bases and airports | Military bases in three sizes and airfields in wasteland cities, locked rooms and loot inside. |
   | our MAC-10 icon | Soldiers, bandits, new guns | NPC tiers with real loadouts, plus the MAC-10, UMP9, HK416 and Mk18. |
   | radio | Updates in the game | When a new version is out, the menu button says Update. |

4. **Screenshots** ("In the game"): ten shots in a grid of four columns, two of them double size; caption over
   each in the game font; a click opens the shot large (lightbox). Subjects in section 6.
5. **Coming soon** ("What we are building next"), six cards, tag "Planned" (or "Idea"), note "Plans, not
   promises: no dates." Only items from docs/roadmap.md and docs/systems_plan.md:

   | Icon | Title | Text | Tag | Source |
   |---|---|---|---|---|
   | bottlecapgold | Crate levels | Loot in four levels by place and danger, and bodies you can search. | Planned | systems_plan 3.1, 3.2 |
   | achivement_trader_bandit | Smarter people | Police survivors, and NPCs that take cover, flank and pull back to heal. | Planned | 4.1, 4.3 |
   | syringe_infection | Infection in stages | Five stages from the bite to the end, and the dead get back up. | Planned | 5.2 |
   | infectedflesh | Zombies that grow | More and stronger infected from day 1 to day 100, and horde nights. | Planned | 5.4, 5.5 |
   | hugemilitarypack_green | A survivor partner | A friend who follows you, wears the gear you give and carries a backpack. | Idea | ideas.md 21 |
   | graphics_card | Community servers | Servers listed on this site and in Decimation's own server menu. | Planned | roadmap UI 2 |

   When an item ships it moves to "What it adds"; when a plan is dropped it leaves the list.
6. **Install** ("Installation guide"): a requirements box, then four tabs.
   - Requirements: Account "Minecraft Java Edition"; Java "Java 8 (Prism Launcher can download it)"; Memory
     "4 GB for the game or more" [inferred: common advice, not measured]; Download "One file, Decimation.jar,
     <size>".
   - Tab **New player** (Prism Launcher):
     1. Install Prism Launcher: get it from prismlauncher.org (Windows, macOS, Linux) and add your Microsoft account.
     2. Make a Forge 1.7.10 instance: Add Instance, pick version 1.7.10, then Mod loader: Forge, version
        10.13.4.1614. Name it Decimation.
     3. Java and memory: edit the instance, Settings: choose Java 8 and set the maximum memory to 4096 MB or more.
     4. Add the mod: download Decimation.jar above. Edit the instance, Mods, Add file, and pick it.
     5. First start: launch. The first start takes a little longer. Decimation's own menu opens: click Play.
     6. Create the world: Singleplayer, Create New World, More World Options, then click World Type until it
        says Decimation. Create.
   - Tab **Already have Decimation**: back up the saves folder; replace mods/Decimation.jar (keep the name);
     delete any deciworldgen jar (it is inside Decimation.jar now), the voice chat jar is optional; old worlds
     keep working, cities, bases and airports appear in new worlds with the world type Decimation.
   - Tab **Updating**: Decimation's Play button says Update when a newer version is out; click it, the jar
     downloads with a progress bar and is checked against its checksum; Quit game and start again, or Later
     (the new version goes in when the game closes). Or swap the jar by hand.
   - Tab **Server**: in an empty folder with Java 8 `java -jar forge-1.7.10-10.13.4.1614-1.7.10-installer.jar
     --installServer`; Decimation.jar into mods; `level-type=decimation` in server.properties before the first
     start; `java -Xmx4G -jar forge-1.7.10-10.13.4.1614-1.7.10-universal.jar nogui`, accept the EULA in
     eula.txt, start again; players use the same Decimation.jar.
   - To check before launch (section 9): the Prism wording against its current version, the screen after
     Decimation's Play, the Forge file names.
7. **Changelog** ("What changed"): the five newest releases, newest open, older ones fold out (version, date,
   pre-release tag, GitHub's rendered release notes); link "Every release on GitHub".
8. **Help** ("Questions"), fold out:
   - The menu button says Update: a newer version is out. Click it, wait for the download, then start the
     game again.
   - My old world has no cities: cities, bases and airports appear in new worlds made with the world type
     Decimation.
   - The game lags in cities: lower the prop render distance in `config/deciworldgen_props.cfg`.
   - Found a bug?: open an issue on GitHub and attach `logs/fml-client-latest.log`. Link: Report a bug (the
     public repo's issues).
9. **Footer**: a dirt strip (our own generated texture), then "Unofficial fan project. Decimation belongs to
   BoehMod. Not an official Minecraft product; not approved by or associated with Mojang or Microsoft." and a
   GitHub link.

On phones: one column everywhere, the top bar folds into a menu button, the hero is shorter, the gallery becomes
two columns of equal shots.

## 4. Look

### Colours (warm night)

| Token | Value | Use |
|---|---|---|
| bg | #1C1815 | page |
| card | #26211D | cards, tabs, fold outs |
| text | #EDE6DA | text, titles |
| muted | #B9AE9E | body text |
| dim | #9A8F80 | small print, dates |
| label | #E08A5A | section labels, step numbers (game shadow #382216) |
| link | #E59468 | links |
| download | #F89838 | torn orange button, white text, shadow #3E260E, edges #A85E18 / #FFB868 |
| splash | #FFFF55 | splash text and the "Planned" tag (shadow #3F3F15) |
| idea | #55FFFF | the "Idea" tag (shadow #153F3F) |
| slot | #15110F | icon slots, inner edges #0B0908 / #3B332C |

Tried and rejected: Decimation's own menu palette (red tinted hero, torn black panels everywhere, logo coral)
"really worse"; ivory field report and the game menu echo (first round).

### Type: the Minecraft GUI font at the game's sizes

- Font: **Minecraft Standard** by Marek Polaček (dafont.com/minecraft-standard.font), a faithful recreation of
  the Minecraft GUI font; its readme says "License: CC PD" and that it can be used "as a font on websites".
  Files MinecraftStandard.otf (and Bold, Oblique). Self hosted (next/font/local).
- Pixel grid measured: one font pixel = font size / 6 (a capital H is 7 font pixels high, as in the game). So
  12 px = GUI scale 2 (capitals 14 px, the game's usual size), 18 px = scale 3, 24 px = scale 4, 36 px = scale 6.
  Only these sizes. Text shadow one font pixel right and down (2, 3, 4, 6 px), colour = 25 % of the text colour
  (#3F3F3F for white), like the game. Font smoothing off (`-webkit-font-smoothing: none`).
- Where: brand and buttons 18 px, nav, labels, card titles, tabs, captions, step numbers 12 px, section titles
  24 px, hero title 36 px. Long text (lead, card text, steps, help) stays in the system sans (15 to 18 px).

### Icons and textures

| Asset | Source | Note |
|---|---|---|
| item icons (16 or 32 px) | Decimation.jar `assets/deci/textures/items/<name>.png` (graphics_card in `scraps/monitor/`) | shown at 32 px in a slot, pixelated |
| MAC-10 icon | ours: dev/src/main/resources/assets/deci/textures/items/gun/smg/mac10.png | |
| torn orange button | Decimation.jar `assets/deci/textures/gui/menu/buttonbg_red.png` used as a CSS mask over #F89838 | |
| torn black button | Decimation.jar `assets/deci/textures/gui/menu/buttonbg.png` stretched | |
| dirt strip | ours, generated (seeded noise in browns) | |
| font | Minecraft Standard (CC PD) | |

Decimation's files go into the PRIVATE site repo only. Never the Decimation logo image (would read as an official
BoehMod page).

## 5. How it works

### Repo and hosting

- Private repo `deciremnant` (local copy beside this one: ~/Projects/deciremnant), branch main, same git rules as
  here (commits by the agent, pushes by the user, no attribution).
- Vercel project `deciremnant` imported from that repo; production deploys when the user pushes main. Hobby
  plan for a non commercial project [inferred: check Vercel's terms at setup].

### Code shape (Next.js App Router, TypeScript)

- `app/page.tsx` puts the sections together; one component per section (Hero, Download, Features, Gallery with
  Lightbox, ComingSoon, Install with tabs, Changelog, Help, Footer); `app/layout.tsx` loads the font and the
  metadata (title "DeciRemnant: Decimation, alive again in singleplayer", description, Open Graph picture from the
  hero).
- Texts in `content/` (features.ts, coming.ts, install.ts, help.ts, splashes.ts): changing a sentence never
  touches a component.
- `public/` holds icons, textures, the font and the screenshots. Pixel art (icons, textures) is never resized by
  the image optimiser (unoptimized, `image-rendering: pixelated`); screenshots go through next/image (WebP / AVIF,
  lazy except the hero).

### Release data (the server function)

- `lib/releases.ts`: `getReleases()` fetches
  `https://api.github.com/repos/ucilmenangis/decimation-singleplayer/releases?per_page=10` on the server with
  `Accept: application/vnd.github.html+json` (release notes come as rendered HTML, `body_html`),
  `X-GitHub-Api-Version: 2022-11-28`, a User-Agent "deciremnant", optional `GITHUB_TOKEN`; cached and
  revalidated every 600 s (about 6 GitHub requests an hour, whatever the visitors do).
- `pickRelease(list)`: the newest release with an asset named exactly `Decimation.jar`, pre-releases included
  (the same rule as fixes/Updater); gives tag, published date, size, pre-release flag, the asset's
  browser_download_url, and the `Decimation.jar.sha256` asset when there is one (uploaded since 0.59.1).
- The download button links straight to the GitHub asset: Vercel never serves the jar.
- Changelog: `body_html` of the five newest releases (GitHub sanitises it; our own releases only).
- Failure: a failed revalidation keeps serving the last good page; with no data at all (first build while GitHub
  is down) the download button links to the releases page and the changelog shows only that link. The build never
  fails because of GitHub.

## 6. Screenshots

- Eleven shots at 1920 x 1080 from REAL generated worlds (seed 1, world type Decimation, current version): no sky
  test platforms, no window bar, HUD hidden (F1), clouds as the player sees them, noon light except the base.

  | Shot | Subject |
  |---|---|
  | hero | a generated city from above (towers, streets) |
  | city | a tower street at street level |
  | highway | a highway between cities |
  | bridge | a river through a city, the street crossing it |
  | rail | an underground railway platform |
  | gate | a wasteland gate (fence, wire, checkpoint) |
  | camp | a survivor camp's market street with traders |
  | refuge | the market hall of a city refuge |
  | base | a forward operating base at sunset, from above |
  | airport | a military airport: tower, helipad, aircraft |
  | gun | the MAC-10 in hand, first person |

- Made by a new dev test mode `siteshots` in this repo (dev/src/main/java/net/decimation/worldgen/devtest):
  finds each subject in the seed 1 world from our generation data (city plans, StructureData places, airport and
  gate scans), generates the chunks, places the camera, waits for the chunks to draw, shoots; window 1920 x 1080.
  Re-run for new versions. The user may replace any shot with their own.
- Output stays local (dev/run/client/devtest/siteshots/, kept in docs/shots/site_v<version>/, git ignored) and is
  described in docs/shots_index.md; copied into the site repo's public/shots by a small script.

## 7. Checks before calling it done

- `next build` and the type check pass; lint clean.
- Unit tests (vitest) for `pickRelease`: newest wins, pre-releases count, a release without Decimation.jar is
  skipped, the sha256 asset is found, an empty list and a GitHub error give the fallback.
- The page rendered with agent-browser at 1300 x 900 (desktop) and 390 x 844 (phone), full page shots read:
  every section, the splash, the lightbox, the install tabs, the fold outs; no dash characters in the page text;
  every link answers (the download link redirects to GitHub's file).
- Fonts at their sizes only (12 / 18 / 24 / 36 px), icons crisp.
- After the user's push: the live deciremnant.vercel.app checked the same way. "Done" only after that.

## 8. Updating the site later

- New release: nothing to do (the server picks it up within 10 minutes).
- A feature ships or a plan changes: edit `content/` (features, coming soon) and push.
- New version with big visual changes: re-run `siteshots`, copy, push.
- Install steps change (Forge, launcher): edit `content/install.ts`.

## 9. Not verified yet

- `deciremnant` free on Vercel; the Hobby plan fits.
- Prism Launcher wording (Add Instance, Mod loader, Edit instance, Settings, Mods, Add file) against its current
  version; Prism offering Java 8 itself (seen once: bug.md, the brother's laptop got Java from Prism).
- The screen after Decimation's Play on a fresh install (Singleplayer next) [inferred].
- Forge installer and universal jar file names for 10.13.4.1614 [inferred from Forge's naming].
- 4 GB of memory as advice [inferred, not measured].

## 10. Design talk record (user revisions)

1. First look, three directions (ivory field report, warm night, game menu echo): "B looks best, but can be more
   minecraft style".
2. Three Minecraft styles on B (title screen, inventory and tooltips, night with pixel touches): "3 with the
   splash text from 1".
3. Whole page in that style: "remove grass on hero section, use real minecraft font not that pixel font with same
   size like real minecraft font, use real pixel icons instead of generated, show more screenshot like 6 or 10 ...,
   add 1 section coming soon feature in future, make proper installation step".
4. Revised page: "really nice"; then "use decimation colors instead?": the Decimation palette page was "really
   worse"; kept from it only the torn orange download and torn black "Install guide >>" buttons.
5. Name: "any suggestion name instead of decimation? with same feel and word deci": DeciRemnant.

## 11. Later (not v1)

- Community server list: a `servers.json` in the site repo, shown on the site and read by our mod over HTTPS to
  fill Decimation's community server page (GuiMenuCommunityServers) [inferred: not tried]; maybe live status
  (a Vercel function pinging each server). Decimation's own BoehMod connection (kryonet) cannot carry it.
- A custom domain if wanted.

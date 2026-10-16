# DeciRemnant: the project website (spec)

Spec written 16 Oktober 2026 from the design talk with the user (brainstorming, mockups in the browser
companion). Status: LIVE since 16 Oktober 2026 at https://deciremnant.vercel.app/ (private repo
github.com/ucilmenangis/deciremnant, deployed by the user on Vercel); built by docs/website_plan.md (tasks 1..14,
rulings at its end). Checked live: desktop and phone pictures read, phone width 390 without sideways scroll, no
console errors, fonts only 12 / 18 / 24 / 36 px, every link (the jar link redirects to GitHub), Open Graph picture.
Roadmap: docs/roadmap.md UI 2.
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
| Install and server (17 Oktober 2026) | install for new players only with a lined requirements table and real game screens; the server guide on its own docs page /docs/server with a side menu |

## 3. The page, top to bottom

The approved mockup is `page-full-v4.html` in `.superpowers/brainstorm/60609-1792139172/content/` (local, git
ignored: it holds Decimation art). Pictures of the mockups: docs/shots/website_design_v0.62/ (shots_index.md).

1. **Hero**: full width screenshot (a generated city from above), darkened toward the bottom.
   - Top bar: brand "DeciRemnant" left; links Features, Screenshots, Install, Server, Changelog, GitHub (since 17
     Oktober 2026 absolute: /#features ... and /docs/server, so the same bar works on every page).
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
6. **Install** ("Installation guide"), for NEW PLAYERS ONLY (user, 17 Oktober 2026: "just for new player, no need
   already have decimation and server"): the requirements as a lined table (a line between rows, no box), the six
   steps divided by lines (no tabs, no panel box), a real game screen beside a step where one could be taken, then
   the torn black link "Running a server? Server guide >>" to /docs/server. Updating stays in Questions ("The menu
   button says Update"); "Already have Decimation" was dropped; the server steps moved to their own page (3b).
   - Requirements: Account "Minecraft Java Edition"; Java "Java 8 (Prism Launcher can download it)"; Memory
     "4 GB for the game or more" [inferred: common advice, not measured]; Download "One file, Decimation.jar,
     <size>".
   - Pictures (user: "game screens only"): step 6 shows Create New World with World Type: Decimation. Prism shots
     are impossible here (Prism is off limits, no screen capture); the Decimation menu was left out (section 6).
   - The steps (Prism Launcher):
     1. Install Prism Launcher: get it from prismlauncher.org (Windows, macOS, Linux) and add your Microsoft account.
     2. Make a Forge 1.7.10 instance: Add Instance, pick version 1.7.10, then Mod loader: Forge, version
        10.13.4.1614. Name it Decimation.
     3. Java and memory: edit the instance, Settings, Java: Java 8 (Prism 9 or newer downloads it by itself), and
        set Maximum Memory Usage to 4096 MB or more.
     4. Add the mod: download Decimation.jar above. Edit the instance, Mods, Add File, and pick it.
     5. First start: launch. The first start takes a little longer. Decimation's own menu opens: click Play, then
        Singleplayer on the server page.
     6. Create the world: Create New World, More World Options, then click World Type until it says Decimation.
        Create New World. (Picture: worldtype.)
   - Checked in the game (dev test `guideshots`, 17 Oktober 2026): Play opens the server page with Singleplayer and
     Other servers, Singleplayer opens Minecraft's world list, Create New World, More World Options, World Type
     reaches "World Type: Decimation" in 4 clicks. The Prism wording was checked against Prism's wiki and source
     (section 9), not clicked [not verified in Prism].
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

## 3b. The server guide page (/docs/server, 17 Oktober 2026)

User: "for the server put on new page docs (we create new page now, suggest your design)"; picked "Docs with side
menu" from two ASCII previews (the other: an article with section chips).

- Layout: a solid top bar with the same links (`header.docbar`), then a grid: the side menu "On this page"
  (Requirements, Set up, Join, Update, Problems; sticky while scrolling) and the article: label "Docs · Server", h1
  "Run a Decimation server" (36 px, 24 on phones), lead, the five sections. Phones: one column, the menu becomes a
  wrap of chips above the title. Texts in the site repo's `content/server.ts`, metadata in `lib/seo.ts`
  (serverMetadata: "Decimation Minecraft server guide | DeciRemnant" (47), a 148 character description, canonical
  /docs/server), sitemap.xml lists it.
- Requirements (lined table): Java 8; 4 GB for the server, set in the start command; Forge 1.7.10 10.13.4.1614;
  Decimation.jar, the same file the players use; port 25565 (TCP).
- Set up: 1 Forge installer (direct maven link) `--installServer`, real last line; 2 a `mods` folder with only
  Decimation.jar; 3 first start writes eula.txt, server.properties, decimation_server.properties and stops at the
  EULA (real line); 4 `eula=true` and the line `level-type=decimation` (the world is made on the next start); 5 start
  again, real "Done (8.421s)!" line, `stop` to save and close.
- Join: install by the guide (/#install); Decimation's menu Play, Other servers (Minecraft's Multiplayer screen); Add
  server, name and address, Done (picture addserver); pick it, Join Server (picture multiplayer).
- Update: stop, replace mods/Decimation.jar, start; players get Update in their menu and need the server's version.
- Problems (fold out): the anticheat kick and its fixes (same jar, no other mods, an empty mods/1.7.10 folder, or
  `allowAdditionalMods=true`; ops never kicked), "You cannot break blocks on this server!" (enableBlockBreaking /
  enableBlockPlacing), items not picked up (enableItemPickup), no cities (world made before level-type).

### Server guide facts (a real run, 17 Oktober 2026)

Scratchpad folder, Java 8 (Zulu 1.8.0_504), the Forge installer from maven (sha1 fccafccf... matches its .sha1), the
release Decimation.jar from GitHub (240980301 bytes, sha256 9e20395d... = GitHub's digest). Never Prism.
- Installer: "The server installed successfully, you should now be able to run the file
  forge-1.7.10-10.13.4.1614-1.7.10-universal.jar" (no mods folder made).
- First start: "server.properties does not exist", "Generating new properties file" (it holds only two comment lines),
  "You need to agree to the EULA in order to run the server. Go to eula.txt for more info."; also wrote config/ (our
  configs and the unpacked content) and decimation_server.properties (Decimation's server rules: enableBlockBreaking
  false, enableBlockPlacing false, enableItemPickup false, allowAdditionalMods false, lootRespawnTime 900, ...).
- Second start with eula=true, level-type=decimation (and online-mode=false ONLY for the dev client test): "Decimation
  world type: 9 biomes", camps and other sites placed around spawn, "Starting Minecraft server on *:25565", "Done
  (8.421s)!"; `stop` saved and closed it.
- Dev test `guideshots` against it: ping 1 to 2 ms, "A Minecraft Server", 0/20. First join: kicked by Decimation's
  anticheat a second after login (the dev client's mods/1.7.10 is empty, so it sent no hashes). With one identical small
  file in the server's mods and the client's mods/1.7.10: in the server's world for 10 s, no kick. How the check works:
  docs/decimation_code/backend.md section 3b.
- LIVE 17 Oktober 2026 (site commit 9f2bfcd pushed by the user): / and /docs/server 200, sitemap.xml lists both, the
  home page has no tabs, the lined table and the Server guide link; all links on both pages; at 1300 and 390 no
  sideways scroll, fonts 12 / 18 / 24 / 36, 0 dashes, no broken pictures. Lighthouse 12: home mobile 98 / 100 / 100 /
  100 (LCP 2.1 s; earlier runs 99 and 100, run to run noise [inferred]), desktop 100 x 4; server guide mobile 98 / 100 /
  100 / 100 (LCP 2.0 s), desktop 100 x 4.

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

- Eleven shots at 1440 x 810 (planned 1920 x 1080; macOS caps the dev client window at this screen, 1470 x 895, so
  the largest 16:9 size that fits is used, measured 16 Oktober 2026) from REAL generated worlds (seed 1, world type Decimation, current version): no sky
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
  gate scans), generates the chunks, places the camera, waits for the chunks to draw, shoots; window set with
  `-Pwindow=1440x810` (one launch) or `devtest.py --live --window=1440x810`.
  Re-run for new versions. The user may replace any shot with their own.
- SHOT 16 Oktober 2026: all eleven in docs/shots/site_v0.62.9/ (shots_index.md); camera rows in tools/site/siteshots.tsv.
- Output stays local (dev/run/client/screenshots/site_<name>.png, kept in docs/shots/site_v<version>/, git ignored) and is
  described in docs/shots_index.md; copied into the site repo's public/shots by a small script.
- Guide pictures (17 Oktober 2026): Minecraft's own screens from the dev test `guideshots`
  (devtest/GuideShotsTest: leaves the world, walks Play, Singleplayer, Create New World, More World Options, World Type,
  then Other servers, Multiplayer, Add Server and a join; window 1440 x 810, GUI scale auto = 3; -Pserver=host for a
  running server). `python3 tools/site/export_assets.py ../deciremnant --guide docs/shots/website_guide_v0.62.9` exports
  ONLY them: cropped to (225, 20, 1215, 800), PNG (the pixel font stays sharp, 23 to 33 KB), content hash names in
  public/guide with manifest.json; the site serves them unoptimised and zooms them pixelated. Used: worldtype (install
  step 6), addserver and multiplayer (server Join).
- Decimation's own menu was shot too (guide_menu_home, guide_menu_servers): no "session invalid" banner any more (the
  menu says Play), but it shows the Decimation logo (never on the site, lesson 8) and the dev player's profile and kills,
  so it is not used; a crop of the buttons alone would be possible if the user wants it.

## 7. Checks before calling it done

- `next build` and the type check pass; lint clean.
- Unit tests (vitest) for `pickRelease`: newest wins, pre-releases count, a release without Decimation.jar is
  skipped, the sha256 asset is found, an empty list and a GitHub error give the fallback.
- Both pages (/ and /docs/server) rendered with agent-browser at 1300 x 900 (desktop) and 390 x 844 (phone), full
  page shots read: every section, the splash, the lightbox, the install steps and their pictures, the server guide's
  side menu (sticky: a full page shot draws it where the capture scrolled; check it with viewport shots), the fold
  outs; no dash characters in the page text; every link answers on both pages (the download link redirects to
  GitHub's file; before the push the server page's canonical is a 404 on the live site, expected).
- Fonts at their sizes only (12 / 18 / 24 / 36 px), icons crisp.
- After the user's push: the live deciremnant.vercel.app checked the same way. "Done" only after that.

## 8. Updating the site later

- New release: nothing to do (the server picks it up within 10 minutes).
- A feature ships or a plan changes: edit `content/` (features, coming soon) and push.
- New version with big visual changes: re-run `siteshots`, then `python3 tools/site/export_assets.py ../deciremnant --shots dev/run/client/screenshots`, push. Pictures are named by content hash (public/shots/manifest.json), so the new ones get new addresses; the image optimiser caches a picture 4 hours (max-age 14400), which would otherwise show old pictures to returning visitors.
- Install steps change (Forge, launcher): edit `content/install.ts`.
- Server guide: `content/server.ts`; change a command or console line only after running it on a test server (3b);
  new pictures through `guideshots` and `export_assets.py --guide`.

## 9. Not verified yet (and what was checked, 16 Oktober 2026)

- CHECKED: `deciremnant` was free on Vercel (live at deciremnant.vercel.app). Whether a GITHUB_TOKEN is set there is
  not visible from outside [not verified]; without it GitHub may refuse the release list from Vercel's shared IPs.
- CHECKED Prism Launcher wording: its wiki ("Add Instance", pick a version and modloader, right click the instance
  and "Edit", the "Mods" tab, Java downloaded automatically in Prism 9.0+: "Java 8 for pre-1.17") and its source
  (Mods page action "&Add File", Java settings "Maximum Memory Usage"). The guide uses those words.
- CHECKED in Decimation's code (deobf GuiMenuBase case 9, GuiMenuOfficialServers): "Play" opens the official
  servers page (after fetching that list), which has a small "Singleplayer" button opening Minecraft's world list.
  CHECKED IN THE GAME 17 Oktober 2026 (dev test `guideshots`): Play opens the server page (deci.i.i) with Singleplayer
  and Other servers; Singleplayer opens GuiSelectWorld; Other servers opens Minecraft's GuiMultiplayer.
- CHECKED Forge files: forge-1.7.10-10.13.4.1614-1.7.10-installer.jar and -universal.jar answer 200 on
  maven.minecraftforge.net.
- 4 GB of memory as advice [inferred, not measured].

## 9b. Search (SEO audit 16 Oktober 2026, searchfit-seo plugin + Lighthouse 12)

- Target keywords (user): "decimation" and "minecraft". Title "Decimation Minecraft mod in singleplayer | DeciRemnant"
  (54), description of 151 characters with both, the same in Open Graph and Twitter, both in the first paragraph
  (LEAD) and in every screenshot and hero alt text ("Decimation Minecraft mod screenshot: ..."). The visible h1 stays as
  approved. Guarded by tests/seo.test.ts in the site repo (lengths, keywords, canonical, Open Graph, crawl files,
  JSON-LD, WebP, icons).
- Crawl: app/robots.ts (allow all, sitemap link), app/sitemap.ts (the page), canonical "/" with metadataBase.
- Structured data: JSON-LD WebSite + SoftwareApplication (GameApplication, Minecraft mod, version and download link
  from the live release, free offer, isBasedOn Decimation by BoehMod).
- Page outline: release notes' h1 / h2 become h3 under the "What changed" h2.
- Icons: our MAC-10 on the download orange (app/icon.png 32, app/apple-icon.png 180, app/favicon.ico 16 / 32 / 64),
  written by tools/site/export_assets.py.
- Lighthouse 12 on the live site (performance, accessibility, best practices, SEO): before mobile 96 / 96 / 100 / 100,
  desktop 100 / 96 / 100 / 100; after the push of 546f637 mobile 100 / 100 / 100 / 100 (LCP 1.8 s, CLS 0), desktop
  100 / 100 / 100 / 100 (LCP 0.5 s). Left, no score effect: unused framework JavaScript 55 KiB, legacy JavaScript
  14 KiB (Next.js chunks), panel / mask textures 17 KiB, hero 29 KiB larger than shown on desktop.
- Live checked 16 Oktober 2026: title 54, description 151, Open Graph and Twitter complete, canonical, robots.txt,
  sitemap.xml, JSON-LD (WebSite + SoftwareApplication with the release's version and jar link), three icons 200, one
  h1, no skipped heading level, every picture with alt (11 screenshots carry both keywords), no dash characters, all
  links. The canonical prints the root without its trailing slash: the same address as the sitemap's (an empty path
  is "/").
- Google Search Console (user's account): HTML file method, `public/googlec28e26622fed860e.html` in the site repo
  (site commit 294adc4, served 200 by a local production build). It must stay: Google checks it again later;
  tests/seo.test.ts fails without it. Live since the user's push (checked 16 Oktober 2026: 200, exact content).
  Verified by the user, and https://deciremnant.vercel.app/sitemap.xml submitted (user, 17 Oktober 2026). Rechecked
  live the same day: the file, sitemap.xml (the one page) and robots.txt all 200. Google's sitemap status, crawling
  and ranking are seen only in the user's Search Console [not verified]. Indexing of the home page requested by the
  user (URL Inspection, 17 Oktober 2026 01:30 WIB): "URL is not on Google", last crawl N/A, added to Google's
  priority crawl queue (shot docs/shots/website_live_v0.62.9/search_console_indexing_requested.png).
- INDEXING CHECK ON 20 Oktober 2026 (the user asked to be told that day): the user opens Search Console, URL
  Inspection of https://deciremnant.vercel.app/ (wanted: "URL is on Google"), Pages (1 indexed), Sitemaps (status
  Success, 1 discovered page), Performance (first impressions for decimation / minecraft searches). From here: a
  Google search for site:deciremnant.vercel.app in agent-browser (stop at any CAPTCHA). Still not indexed then is
  normal for a new site: look again a week later [inferred].

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
6. Install section (17 Oktober 2026, the user's shots of the live tabs and requirements box): "for the installation,
   just for new player, no need already have decimation and server. for the server put on new page docs (we create new
   page now, suggest your design). and the installation, better to put real image instead of step list only. for
   requierement, make list table lined style instead using boxed styled". Choices: pictures "game screens only" (no
   Prism, no Decimation menu), each picture beside its step's text, the server page as docs with a side menu (3b).
7. Verdict on 6 (17 Oktober 2026): "not bad but i think you need to learn famous web before making things like this,
   noted that. i take this, we can figure it out later": kept as built; study famous sites of the kind before the next
   design (skill lesson 17).

## 11. Later (not v1)

- Community server list: a `servers.json` in the site repo, shown on the site and read by our mod over HTTPS to
  fill Decimation's community server page (GuiMenuCommunityServers) [inferred: not tried]; maybe live status
  (a Vercel function pinging each server). Decimation's own BoehMod connection (kryonet) cannot carry it.
- A custom domain if wanted.

---
name: decimation-website
description: Use when building, changing or reviewing DeciRemnant, the project website (download page, install guide, screenshots, coming soon, changelog, help), or when a release, a shipped feature or a roadmap change needs the site updated.
---

# DeciRemnant, the project website

Facts, decisions and the page text: docs/website.md (the spec). Site code: the PRIVATE repo `deciremnant`
(local ~/Projects/deciremnant), Next.js on Vercel. This public repo keeps only docs, tools and the screenshot test.

## Workflow
1. Read docs/website.md and the lessons below; docs/shots_index.md "website_design_v0.62" for what the mockups
   looked like.
2. A visual change: show it first (mockup with real texts, real icons, real screenshots), side by side with the
   current look or behind a switch, before building it.
3. Build in the site repo; texts only in `content/`.
4. Check: next build, vitest (release picker), agent-browser full page at 1300 x 900 and 390 x 844 (read the
   shots), link check, no dash characters in page text, font only at 12 / 18 / 24 / 36 px.
5. After the user's push: check deciremnant.vercel.app live the same way. Only then "done".

## Lessons (from the user's revisions)
1. Minecraft style means the game's own GUI font at the game's own pixel sizes: Minecraft Standard (CC PD),
   font size = 6 x GUI scale (12, 18, 24, 36 px), shadow one font pixel at 25 % of the text colour. A generic
   pixel font (Pixelify Sans) was rejected.
2. Icons are the game's real item icons (Decimation's, ours for our guns) in inventory slots, never hand drawn
   ones.
3. This site's palette is warm night (dark brown, clay). Decimation's red and black menu palette was "really
   worse" as a whole page; its torn orange and torn black buttons were liked. Try a new direction one element at
   a time and keep the way back.
4. No decoration that reads as a game block strip across the hero (the grass strip was removed).
5. The install guide is the NEW PLAYER path only, complete for a first time player: requirements, launcher, Forge
   version, Java, memory, world type; never a three line summary. Updating lives in Questions; the server has its own
   docs page (/docs/server); no tabs for other readers (user, 17 Oktober 2026).
6. Show what was built: 6 to 10 screenshots of our features in real worlds; never sky test platforms, window
   bars or HUDs.
7. Coming soon lists only roadmap items, tagged Planned or Idea, no dates.
8. Fan site: say unofficial, credit BoehMod, carry Mojang's "not an official Minecraft product" line. The site logo is
   OUR official DeciRemnant logo (user, 17 Oktober 2026: "we have our official logo now"; it carries the name, no text
   beside it), and BoehMod's own logo stands in the footer as the credit ("add boehmod logo on bottom website for
   credit"). Before it the logo was the game's DECIMATION banner (case 7).
9. Check the page like a visitor and measure it: full page at 1300 and 390 wide after scrolling once (lazy images), the
   top bar at tablet widths 768 to 1024 (links running off the edge, case 7),
   `scrollWidth` at 390, overlap of the splash with the hero text by bounding boxes, font sizes in use, console
   errors, every link (scripts/check-links.mjs). The splash covered the title on phones; only the measurement said so.
10. Every picture the site serves is named by its content hash (tools/site/export_assets.py writes
   public/shots/manifest.json): the image optimiser answers with max-age 14400, so a new picture under an old name
   stays hidden behind the old one for hours.
11. Screenshots come from the real world by the siteshots dev test: find subjects in the server log
   (tools/site/subjects.py), aim with top down block maps (tools/topdown_blocks.py), shoot at the largest 16:9 the
   screen allows (1440 x 810 here), read every shot, and reshoot a first visit (fresh chunks draw for minutes).
12. Keyboard: an overlay (the screenshot lightbox, the phone menu) takes focus when it opens, closes on Escape and
   gives focus back to the control that opened it; test it in Vitest and once in a real browser.
13. Search: the site is found by "decimation" and "minecraft" (user). Both go in the title (at most 60 characters),
   the description (120 to 160), Open Graph and Twitter, the first paragraph, every picture's alt and the JSON-LD;
   robots.txt, sitemap.xml, the canonical and the Search Console file (public/google*.html) stay; tests/seo.test.ts guards all of it. Audit with Lighthouse locally
   (Google's free PageSpeed API runs out of quota) and read every flagged audit, not only the scores.
14. Lists of facts (requirements) are lined tables: a line between rows, no boxed panel (user). Steps are divided by
   the same lines.
15. Steps show REAL pictures, and only where a real one can be taken here: Minecraft's own screens from the dev
   test `guideshots` (no Prism: off limits, no screen capture; not Decimation's menu: its logo and the dev profile).
   Ask before promising a picture; say plainly which steps stay text.
16. A docs page (/docs/<topic>) states only what was run: every command and console line from a real test server
   (scratchpad, release jar), every rule from code plus what the run showed. A first run teaches what the guide
   misses (the anticheat kick was found that way).
17. Study famous sites of the same kind BEFORE designing (user: "you need to learn famous web before making things like
   this"): for a docs page well known docs (Next.js, Stripe, Tailwind, Fabric / NeoForge, the Minecraft Wiki), for a
   download page Modrinth / CurseForge. Look at 3 to 5, write down the patterns taken (navigation, step layout, code
   blocks, callouts), then mock up. The server guide was built from previews only: "not bad", kept for now.

## Casebook (never delete a case)

### Case 1: the design talk (16 Oktober 2026)
- Rounds: three looks, B chosen ("more minecraft style"); three Minecraft styles, "3 with the splash text from 1";
  whole page revised (no grass, real font at real size, real icons, 6 to 10 shots, coming soon, proper install);
  Decimation colours tried and rejected, torn buttons kept; name DeciRemnant.
- Evidence: docs/shots/website_design_v0.62/ (shots_index.md); mockups in .superpowers/brainstorm/ (local).
- Outcome: docs/website.md, approved v4 mockup.

### Case 2: the build (16 Oktober 2026)
- Site repo ~/Projects/deciremnant (private, local; 33 tests), plan docs/website_plan.md executed task by task, the
  rulings at the end of that plan.
- Found on the way, each fixed and checked: @types/node 20 from the template blocked Vitest 5 (24 now); Testing
  Library cleanup needs a setup file without Vitest globals; the phone splash covered the hero label (measured
  overlap, moved under the nav); full page shots showed empty icon slots (lazy images: scroll first); the real
  screenshots did not appear (optimiser cache 4 hours: content hash names); a failed ISR refresh keeps the last page
  (checked: STALE then HIT after 610 s with a dead API).
- Screenshots: -Pwindow=1920x1080 reaches the game but macOS caps the window at the screen (1470 x 895): 1440 x 810;
  the Mac slept for 75 minutes during a run (server "Running 4500894ms behind"); siteshots tuned (clouds off above
  y 120, wait for the chunks at the camera and 48 ahead, counters reset per row: a row was shot before the camera
  moved). Four rounds; evidence docs/shots/site_v0.62.9/ (shots_index.md).

### Case 3: live (16 Oktober 2026)
- The user created nothing by hand but the push and the Vercel import; `gh repo create ... --private --source . --remote
  origin` (no push) made the repo. Live at https://deciremnant.vercel.app/: served from Vercel's cache (HIT,
  prerendered), real release data (Download 0.59.0, 241 MB), desktop and phone pictures read, width 390, no console
  errors, fonts 12 / 18 / 24 / 36 px, all links (jar 302), Open Graph picture 200.
- Open: the user's verdict on the live site; a GITHUB_TOKEN in Vercel (cannot be seen from outside).

### Case 4: deferred minors (16 Oktober 2026)
- User: "fix the deferred minors". Fixed: base placement line logged once (3 lines for 3 bases, was up to 122),
  lightbox focus in and back, Escape closes the phone menu (two tests RED then GREEN, real browser checked), Vite's own
  tsconfig paths, the site README. Site commit 680fb97. Live checked after the user's push: focus into the zoomed
  shot and back, Escape on the phone menu, width 390, no console errors, all links.

### Case 5: SEO audit (16 Oktober 2026)
- User: "audit the seo using seo plugin", then all findings fixed: "i just want to search keyword for this website is
  decimation and minecraft, so it can easily find by people".
- Found: no robots.txt / sitemap.xml / canonical, description 181 characters, Open Graph without url / type /
  site_name / locale, no structured data, 8 h2 from the release notes in the changelog, template favicon, gallery
  alts repeating their captions, a link told apart by colour only, hero without fetchpriority (Next 16 deprecated
  priority), PNG textures. Lighthouse SEO said 100 before: it checks only the basics.
- Fixed in site commit 546f637; local Lighthouse accessibility 100, SEO 100; heading order needed h3, not h4.
- Live after the user's push: Lighthouse 100 in all four categories on mobile and desktop (was 96 / 96 / 100 / 100 on
  mobile), every head tag, crawl file, icon and link checked; desktop and phone pages read (docs/shots/
  website_live_v0.62.9/).
- Search Console: the user picked the HTML file method and downloaded the file; it went into public/ test first (RED,
  then GREEN), served 200 by a local production build; site commit 294adc4, live after the push (200, exact content). GITHUB_TOKEN: the user creates and pastes
  it; never through the chat.
- 17 Oktober 2026: the user verified the property, submitted sitemap.xml and requested indexing of the home page
  ("URL is not on Google", priority crawl queue). Whether Google crawls and ranks the page shows only in the user's
  Search Console: ask, never claim it. Indexing check on 20 Oktober 2026 (the user asked to be told then).

### Case 6: install for new players, server guide page (17 Oktober 2026)
- User (shots of the live tabs and requirements box): "for the installation, just for new player, no need already have
  decimation and server. for the server put on new page docs (we create new page now, suggest your design). and the
  installation, better to put real image instead of step list only. for requierement, make list table lined style
  instead using boxed styled".
- Plan mode, AskUserQuestion with ASCII previews: pictures "game screens only", picture beside the text, server page
  "docs with side menu". My premise for "no menu pictures" (a session warning banner) was out of date: the menu now
  says Play without a banner; it stays out for the logo and the dev profile (docs/website.md section 6).
- Real server run first (scratchpad Forge server, release jar from GitHub, hashes checked): installer, EULA stop, empty
  server.properties, "Done" line copied word for word; dev test `guideshots` checked Play, Singleplayer, World Type
  (4 clicks), Other servers, Multiplayer, Add Server and a join. The first join was KICKED by Decimation's anticheat
  (dev client sent no mod hashes): a whole Problems entry came from that (docs/decimation_code/backend.md 3b).
- Site commit 9f2bfcd (local): FactTable, Steps, Zoom and Lightbox (shared with the gallery), DocMenu, Nav links
  absolute, /docs/server, sitemap 2 URLs, 60 tests (RED first for install, nav, inline links, seo). Local checks: both
  pages at 1300 and 390 read, no sideways scroll, fonts 12 / 18 / 24 / 36, 0 dashes, links ok, keyboard zoom in a real
  browser, Lighthouse server page 98 / 100 / 100 / 100 mobile and 100 desktop (local home 95 is HTTP/1.1 local
  serving: same script bytes as live, which scores 99). Evidence docs/shots/website_guide_v0.62.9/.
- User verdict: "not bad but i think you need to learn famous web before making things like this, noted that. i take
  this, we can figure it out later" (lesson 17). Pushed (9f2bfcd) and checked live 17 Oktober 2026: both pages 200,
  sitemap.xml lists both, no tabs on the home page, all links, no sideways scroll at 390, fonts 12 / 18 / 24 / 36, 0
  dashes, no broken pictures; Lighthouse home 98 / 100 / 100 / 100 mobile and 100 desktop, server guide 98 / 100 / 100
  / 100 mobile and 100 desktop.

### Case 7: the Decimation logo, tablets (17 Oktober 2026)
- User: "now put the decimation logo on website" (asked earlier as "later"). The menu banner (461 x 101) as WebP in the
  top bar, 40 px high (28 on phones), "DeciRemnant" at 12 px beside it, like the famous pattern of logo plus site name
  (wikis, Modrinth project pages). Test first (RED: no logo image in the brand link), 62 tests.
- Measuring every width found what the shots at 1300 and 390 hid: logo plus six links need about 990 px; from 760 to
  986 px the links ran off the right edge (it began with the Server link). Fix: the links fold into the Menu button
  below 1024 px; gap between logo and links checked at 1025, 1100, 1300 (46 px and more).
- Lesson: measure the top bar at tablet widths too (768 to 1024), not only desktop and phone.

### Case 8: our official logo, BoehMod in the footer (17 Oktober 2026)
- User: "we have our official logo now @Code_Generated_Image.png, but i think its still use white background" and "add
  boehmod logo on bottom website for credit logo". The user's file stays untracked in both repos (.git/info/exclude).
- Clearing the paper: colour to alpha faded the red letters and the grey wire; a mask kept them: near white with every
  channel at 232 or more becomes clear, a 2 px band of low saturation grey around the drawing gets partial alpha, all
  else stays opaque (tools/site/export_assets.py --logo, pure PIL). 539 x 120 WebP, 40 px high in the bar (28 on
  phones); BoehMod's boehmod_big.png from the jar, 64 px high, 26 px in the footer.
- Tests first: the brand link holds our logo with alt DeciRemnant and no text; the footer holds BoehMod's logo; the
  narrower logo moved the fold of the links to 900 px (measured 1300 gap 456 px, 1000 gap 156, 901 gap 57). Site 72cc3c7.
- Live after the user's push (17 Oktober 2026): logo 179 x 40 and credit 171 x 26 load at 1300, 125 x 28 and 171 x 26
  at 390, no sideways scroll, fonts 12 / 18 / 24 / 36, 0 console errors, all links on both pages; shots
  docs/shots/website_live_v0.64.6/.

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
5. An install guide is complete for a first time player: requirements, launcher, Forge version, Java, memory,
   world type, updating, server; never a three line summary.
6. Show what was built: 6 to 10 screenshots of our features in real worlds; never sky test platforms, window
   bars or HUDs.
7. Coming soon lists only roadmap items, tagged Planned or Idea, no dates.
8. Fan site: say unofficial, credit BoehMod, carry Mojang's "not an official Minecraft product" line, never the
   Decimation logo image.
9. Check the page like a visitor and measure it: full page at 1300 and 390 wide after scrolling once (lazy images),
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
- 17 Oktober 2026: the user verified the property and submitted sitemap.xml. Whether Google crawls and ranks the page
  shows only in the user's Search Console: ask, never claim it.


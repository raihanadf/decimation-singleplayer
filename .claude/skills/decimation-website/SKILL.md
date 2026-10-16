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

## Casebook (never delete a case)

### Case 1: the design talk (16 Oktober 2026)
- Rounds: three looks, B chosen ("more minecraft style"); three Minecraft styles, "3 with the splash text from 1";
  whole page revised (no grass, real font at real size, real icons, 6 to 10 shots, coming soon, proper install);
  Decimation colours tried and rejected, torn buttons kept; name DeciRemnant.
- Evidence: docs/shots/website_design_v0.62/ (shots_index.md); mockups in .superpowers/brainstorm/ (local).
- Outcome: docs/website.md, approved v4 mockup.

### Case 2: the build (16 Oktober 2026)
- Site repo ~/Projects/deciremnant (private, local; 33 tests), plan docs/website_plan.md executed task by task, ledgers
  in both repos' .superpowers/sdd/website_plan/progress.md.
- Found on the way, each fixed and checked: @types/node 20 from the template blocked Vitest 5 (24 now); Testing
  Library cleanup needs a setup file without Vitest globals; the phone splash covered the hero label (measured
  overlap, moved under the nav); full page shots showed empty icon slots (lazy images: scroll first); the real
  screenshots did not appear (optimiser cache 4 hours: content hash names); a failed ISR refresh keeps the last page
  (checked: STALE then HIT after 610 s with a dead API).
- Screenshots: -Pwindow=1920x1080 reaches the game but macOS caps the window at the screen (1470 x 895): 1440 x 810;
  the Mac slept for 75 minutes during a run (server "Running 4500894ms behind"); siteshots tuned (clouds off above
  y 120, wait for the chunks at the camera and 48 ahead, counters reset per row: a row was shot before the camera
  moved). Four rounds; evidence docs/shots/site_v0.62.9/ (shots_index.md).


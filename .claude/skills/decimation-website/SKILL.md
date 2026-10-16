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

## Casebook (never delete a case)

### Case 1: the design talk (16 Oktober 2026)
- Rounds: three looks, B chosen ("more minecraft style"); three Minecraft styles, "3 with the splash text from 1";
  whole page revised (no grass, real font at real size, real icons, 6 to 10 shots, coming soon, proper install);
  Decimation colours tried and rejected, torn buttons kept; name DeciRemnant.
- Evidence: docs/shots/website_design_v0.62/ (shots_index.md); mockups in .superpowers/brainstorm/ (local).
- Outcome: docs/website.md, approved v4 mockup.

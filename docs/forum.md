# DeciRemnant forum (design in progress)

Roadmap UI 3. User, 18 Oktober 2026: "talk about the game, share builds, report bugs. just as normal specific game
forum". The My Community popup's Forums line in the game will open it.

## References studied (18 Oktober 2026)
Shots and notes: docs/shots/forum_design_v0.64.8/ (shots_index.md). Patterns taken:
- Factorio (phpBB): boards in groups with topics / posts / last post columns; bugs sorted by state.
- Project Zomboid (Invision): New Topics sidebar; every bug report carries version, mode, mods, save, steps.
- Vintage Story (Invision): bot spam forced them to limit posting to game owners; plan spam control from day one.
- Godot (Discourse): one list with status tags instead of many boards; similar topics shown while typing a title;
  an answer can be marked as the solution.

## Mockup v1 (shown 18 Oktober 2026, waiting for the verdict)
Five screens in docs/shots/forum_design_v0.64.8/mockup_v1.html, built with the site's own CSS, font and item icons:
1. Forum home: groups DeciRemnant (Announcements, General discussion, Builds and screenshots), Support (Bug reports,
   Help and install, Suggestions), Community (Servers and hosted worlds, Off topic). Right column: current release,
   new topics, numbers, rules.
2. Bug reports: status tags Open, Need info, Confirmed, Fixed in X, Not a bug, Duplicate (team sets them), version on
   every row, +1 count, filter chips.
3. A topic: the report form's answers as a lined table, log block, screenshot, author column (Team badge, posts,
   version played), status change events, marked answer, reply box.
4. Report form: version (from the release list), where (singleplayer, LAN or hosted world, server), how often, world
   type, other mods, what happened, steps, expected, log and pictures; similar topics warning.
5. Builds gallery: picture cards with tags (Base, Refuge, City, Found in the world), Latest / Top this month / All time.

## Open (not decided)
- Sign in method, where the data lives (the site is Next.js on Vercel), moderation and spam control, whether the
  hosting relay's Community list shares accounts with the forum (player hosting, docs/player_hosting.md).

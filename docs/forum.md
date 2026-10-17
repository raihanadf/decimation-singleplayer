# DeciRemnant forum and Discord

Roadmap UI 3. User, 18 Oktober 2026: "talk about the game, share builds, report bugs. just as normal specific game
forum". The My Community popup's Forums line in the game will open it.

## Where it stands (18 Oktober 2026, 05:23)
- **Discord first** (user: "wait, what if the foums use discord community? since we dont need to figure it out payment
  at all?" then "ok discord first, make the channel plan"). Plan: site repo `docs/discord/channel-plan.md` (site
  86d00ac): roles, categories, forum channels with tags and post guidelines, AutoMod, onboarding, texts to paste, the
  GitHub release webhook. The user creates the server and sends a never expiring invite; then the site gets a Discord
  link and the "A forum" coming soon card becomes "Join our Discord".
- **Website forum design on the shelf**: site repo `docs/superpowers/specs/2026-10-18-forum-design.md` (site d18c840),
  mockups beside it in `forum-mockups/`. Built later only if Discord stops being enough (repeated questions that need
  Google, too noisy, players in countries that block Discord).
- **Game link later**, once the game is stable (user, 05:10). The Forums line opens the Discord invite; later the
  Early access role and the account tie to the player (premium: Mojang session check; cracked: an install key the mod
  keeps; never by name).

## Decisions from the website forum brainstorm (kept for when it comes back)
| When | Decision | User's words |
| --- | --- | --- |
| 04:36 | The five v1 screens are good | "all of them good more than i expected" |
| 04:59 | Phone: B, feed first, bottom tab bar | "good, take B for phone" |
| 05:03 | Desktop: B with A's title banner | "take B option to but combined with above title screen" |
| 05:07 | Google sign in first, Discord later, game link later | "take 1, discord or google plus game link" |
| 05:10 | Sign in for the forum only for now | "we better to waiting the game to be stable" |
| 05:17 | Database Turso | "ok take turso, write the design" |

## Mockup v1 (approved as "more than i expected")
Five screens in docs/shots/forum_design_v0.64.8/mockup_v1.html, built with the site's own CSS, font and item icons:
1. Forum home: groups DeciRemnant (Announcements, General discussion, Builds and screenshots), Support (Bug reports,
   Help and install, Suggestions), Community (Servers and hosted worlds, Off topic). Right column: current release,
   new topics, numbers, rules.
2. Bug reports: status tags Open, Need info, Confirmed, Fixed in X, Not a bug, Duplicate (team sets them), version on
   every row, +1 count, filter chips.
3. A topic: the report form's answers as a lined table, log block, screenshot, author column, status change events,
   marked answer, reply box.
4. Report form: version (from the release list), where, how often, world type, other mods, what happened, steps,
   expected, log and pictures; similar topics warning.
5. Builds gallery: picture cards with tags (Base, Refuge, City, Found in the world).
Then five home variants (home5.html) and the phone layout (phone.html); see shots_index.md.

## References studied
Shots in docs/shots/forum_design_v0.64.8/ (shots_index.md).
- Round 1: Factorio (phpBB: grouped boards, bugs split by state), Project Zomboid (Invision: New Topics sidebar; bug
  template Branch / Version / Mode / Mods / Save / Steps), Vintage Story (posting limited to game owners after bot
  spam), Godot (Discourse: one list with status tags, similar topics while typing, marked solution).
- Round 2 (home pages): Hypixel and Wynncraft (art banner over the board index, item icons, members online), Flarum
  ("Need help?" helper, help others, top contributors), Unity Discussions (big search, boards as cards), Terraria
  (news carousel), NodeBB (newest topics strip), Satisfactory Q&A (filters by version and status, Latest fixed).
- Blocked by Cloudflare: Klei, Paradox, Minecraft Forum, Bohemia, Warframe.

## Data behind the choices
- Phones carry 50 to 66 % of web traffic (StatCounter 59.6 % Sep 2026, Similarweb 66.6 % Aug 2026); no forum only
  figure. The site has no analytics: our own share is unknown.
- NN/g: navigation used 89 % when visible against 44 % and 56 % behind a hidden menu on phones.
- Replies bring newcomers back: The Guardian's threaded comments made new users more likely to comment again, mostly
  because more comments got a reply (Budak et al. 2017, doi 10.1145/3134662).
- Discourse advises the Latest list as home for small and medium forums; near empty boards look dead.
- Discord: the most common home for PC gaming communities (Hive Index, 37 %, small sample); not indexable by Google;
  blocked in Russia and Turkey since October 2024.
- Free services checked for the website forum: Neon Free (no card; 100 CU hours, about 400 awake hours a month, then
  suspended to the next month), Turso Free (no card; 5 GB, 500 M rows read, 10 M written a month; over any one the
  databases are BLOCKED), Resend (100 mails a day), Vercel Hobby (non commercial; Blob 1 GB per third party guides).
- Minecraft sign in through Microsoft needs the restricted XboxLive.signin scope (Xbox developer programme): not for
  us. The game link would use the mod's own session check instead [inferred, needs a spike].

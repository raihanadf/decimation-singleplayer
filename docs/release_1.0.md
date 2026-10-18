# Release 1.0.0 beta: plan (18 Oktober 2026)

User (18 Oktober 2026): "began to planning whats release 1.0.0 beta version will be add (multiplayer, new generated
building, big improvment perfomance, many bug fix and many more). we can still develop previous 1.0.0 version cause
that version is "final product" and begin transform from pre-release to release. so before 1.0.0 version, we try to
do so many things like bug fix, perfomance boost and many thing task we still left behind".

## 1. What 1.0.0 means

- 1.0.0 beta is the first GitHub release that is not marked pre-release: the "final product" baseline players can
  trust. Everything before it stays pre-release (user rule: pre-release until city generation is done; the new city
  content is part of 1.0.0, so the rules agree).
- Until then: batched pre-releases 0.65, 0.66 ... (user rule: no upload per small change), each one closing a block of
  the list below. After 1.0.0: 1.0.x for fixes, 1.1 and up for features.

## 2. Scope (must have for 1.0.0)

| Area | What | Where it stands |
| --- | --- | --- |
| Multiplayer | player hosting part 1 (Host Online, Community list, relay, Commands: only me) | built, real two laptop test smooth; not released |
| Multiplayer | joining with a missing mod sits on Connecting for 2 minutes | bug.md, open (voice chat now optional: fixed 179e211) |
| Multiplayer | website guide page /docs/multiplayer; site teaser now | teaser local (site 5ca55b6) |
| Cities | new city content, legal to ship (docs/city_replacement.md: McJty Lost Cities, MIT, plus our layers) | planned, user decision |
| Performance | a measured pass: fps in a city view, prop draw cost, chunk generation time, server tick time with 2 to 4 players, memory (docs/performance.md methods) | partly done before (props, infected AI, scope) |
| Bugs | every open bug in bug.md and roadmap "Open bugs" fixed or consciously parked with a reason | see section 3 |
| Updates | in-game update from any 0.6x to 1.0.0 proven in the production client | proven 0.64.7 to 0.64.8 |
| Release | production server + client checks (docs/production_test.md), release notes, site download, Discord | per release |

## 3. Known open bugs today (18 Oktober 2026)

- Joining a world with a mod you do not have: Connecting for 2 minutes (bug.md, new today).
- Drawing cost of props in prop heavy views: 225 props still about halve the fps (roadmap Open bugs 1).
- Building base height depends on chunk generation order (low impact; roadmap Open bugs 2).
- Every Decimation mob solid black on a fresh Windows install (parked, waiting for that laptop's logs).
- Continue tooltip "Open world" reads like an instruction: "Last played: <name>" (tasks.md).
- Two stale / flaky shots test checks (roadmap Code and tools 2, pending).
- Giant buildings skipped (casino, oasis condo, laboratory): goes away with the new city content [inferred].

## 4. Candidates ("many more"; the user picks)

From docs/roadmap.md and docs/systems_plan.md: crate levels and NPC loot bodies (systems plan step 2), smarter NPCs,
infection stages, zombies that grow over days, survivor partner (idea 21), police NPCs, radiated areas, claimable zones,
more guns and scopes, the website world list with kick and ban (hosting part 3), official servers, donations through
Early access, a forum (Discord first).

## 5. Gates before tagging 1.0.0

1. Full dev test suite PASS in one live session (camp and refuge in their own launch; mainmenu and menupages last).
2. Production server and client checks PASS with the release jar (docs/production_test.md).
3. A real two player session through the relay with the release jar (the brother test again).
4. A two hour play session in a fresh world without a crash; fps and tick numbers written down.
5. No DeceasedCraft or other unlicensed content in the jar (content list checked).
6. Release notes, site download and changelog, Discord post; GitHub release NOT marked pre-release.

## 6. Order (user decision, 18 Oktober 2026)

User: "as long we dont have pre-release that include multiplayer, its fine. pre-release only do low-medium feat/fix in
there. for multiplayer + new city gen release, it will be released at 1.0.0 version as release version".

1. Pre-releases 0.65 and up: low to medium features and fixes only (update bar, HudGuard, place names rejoin, corpses
   with a gun, performance, bug fixes). NO multiplayer in them (Host Online, the Community list, the Multiplayer news
   panel stay out or say coming in 1.0.0).
2. 1.0.0, the first full release (not pre-release): multiplayer (hosting part 1, voice chat optional, Commands: only
   me) together with the new city content (docs/city_replacement.md).
3. The teaser posts (docs/promotion.md) promote 1.0.0; the follow up post goes out on the 1.0.0 day.
4. How (user choice, 18 Oktober 2026: "Release branch", then "split 2 branch, so player cant reverse engineer it ... make
   sure the commit upstream still appear"): branch `prerelease` from tag 0.64.8 with the plain fixes carried from main
   (CLAUDE.md "Two branches"); main keeps every commit. At 1.0.0 the release is built from main.
(My earlier suggestion was 0.65 with multiplayer first; replaced by the user's order.)

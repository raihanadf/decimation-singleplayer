# Achievements and story mode UI (feature 14; docs/ideas.md 31, 38) DRAFT, open questions for the user

User, 22 Oktober 2026: "Full story mode with an achievement styled UI"; "Achievements from easy to extreme, and secret
ones". Not decided yet: the questions at the end.

## Proposal
- Achievements (fixes/Achievements): a list in a JSON of ours (name, description, tier easy / medium / hard / extreme,
  secret flag, condition), per player progress saved with the player (IExtendedEntityProperties, like RadiationDose),
  checked on events we already have (kills, loot opens, days survived from WorldClock, zones entered, items crafted or
  fitted, radiation, durability repairs, distances); a toast in Decimation's style when one is earned
  (Deci.toast); secret ones show as "???" until earned.
- An achievements page in Decimation's menu style (its panels and fonts), reachable from the in game menu and the main
  menu profile card: tiers as tabs, progress bars, earned dates.
- Story mode: chapters of goals that lead through our world (first night, find a refuge, reach the military base, the
  airport, the wasteland city's radiation core), each chapter a set of achievements plus a short text; the page shows
  the current chapter and its next goal; a marker on the HUD compass or as a chat hint.
- First list size: about 40 achievements (10 a tier), 5 secret, a 5 chapter story.

## Performance
Event counters only; the page drawn only while open.

## Questions for the user
1. Story: a guided chapter line through the world (as above), or only achievements with a story flavour?
2. Rewards: none (pride), bottlecaps, or items and skins?
3. Multiplayer: per player only, or also server wide firsts announced in chat?
4. Where the page lives: the in game Esc menu, the main menu profile card, a key, or all of these?

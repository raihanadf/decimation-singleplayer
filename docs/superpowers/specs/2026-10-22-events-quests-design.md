# Survivor events, friendly NPCs and trader quests (feature 15; docs/ideas.md 28, 32, 33) DRAFT, open questions

User, 22 Oktober 2026: "random survivor that provide random quest that need to be solved with limited time before they
dissapear"; friendly NPCs that give quests or gifts, never despawn and glow; "Trader quests from easy to extreme".

## Facts
Decimation has an unused Quest class (deci/ai/z, tasks and a reward, no caller); our survivor tier and camps
(docs/safezone.md), traders in camps and refuges, WorldClock for days, LootBody for rewards, our channel for screens.

## Proposal
- fixes/Quests: quest kinds as data (JSON): fetch (bring N of an item), clear (kill N infected near a place), find (reach
  a marked place), escort (walk a survivor to a refuge), deliver (carry a package between traders); each with a tier
  (easy to extreme), a time limit in world minutes and a reward (caps, a loot body of a tier's items).
- Random survivor events: every 1 to 2 world days near a player outside towns a survivor appears (our survivor tier,
  friendly) with one timed quest; fails and leaves when the time runs out.
- Friendly NPCs with quests or gifts: never despawn while their quest lasts; a glow of ours (an outline drawn after the
  entity, or particles) since 1.7.10 has no glow effect.
- Trader quests: each trader kind offers 3 quests at a time from its kind's list (a medic: medical items; a gun trader:
  parts), refreshed each day; tiers unlock by quests done with that trader.
- A quest screen in Decimation's style (its trader screen as the base), a HUD line with the active quest and its time.

## Questions for the user
1. Quest kinds to start with (fetch, clear, find, escort, deliver): all, or which first?
2. Rewards: caps only, items, or reputation that unlocks better trader offers?
3. Time limits in world minutes (a world day is 20): easy 30, extreme 10, or other?
4. Multiplayer: personal quests only, or shared quests a group can do?

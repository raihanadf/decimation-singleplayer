# City content replacement: plan (18 Oktober 2026)

User (18 Oktober 2026): "for the worldgen cities we gonna have full replacement due to copyright issue if we use our
structure now. so we gonna move on to new opensource structure (only for city worldgen, other structure on wilderness
is okey). here the replacement ChaoszPack Lost Cities", then "start planning the city building with chaoszpack
structure". Roadmap Worldgen item 5. This is the plan; nothing is built yet.

## 1. Why it matters now

The city buildings are DeceasedCraft's Lost Cities parts (current and legacy packs) converted by tools/lcpack.py
(docs/city_engine.md). The repo never holds them, but the RELEASE jar does: tools/onejar.py packs
dist/config/decimation_worldgen (the lc pack included) as assets/deciworldgen/content since v0.58.0. Every public
release since then ships DeceasedCraft's buildings: the 0.64.8 release jar holds 474 files under
assets/deciworldgen/content/lc/ (folders dc and legacy), checked 18 Oktober 2026. The `large` schematics beside them
(docs/worldgen.md) need the same source check [not verified].

## 2. ChaosZPack checked (18 Oktober 2026, its CurseForge page and license page)

- What it is: "ChaosZPack [Lost Cities] Expansion" by ChaosCraft_HD, maintained by Admany, published by BlackRift
  Studios; post apocalyptic Lost Cities buildings for the ChaosZProject modpack. Minecraft 1.20.1, Forge only.
- Built with modded blocks: needs Create, Create Deco, MrCrayfish's Furniture, TaCZ guns, Immersive Weathering,
  Doomsday Decoration, MineTraps, Horror Elements and more (about 15 mods), plus a custom Lost Cities profile from
  their Discord. None of those blocks exist in 1.7.10: a conversion would drop most of what makes the buildings look
  like theirs [inferred from the dependency list; the structure files were not opened].
- License: "Custom License". Sharing the UNMODIFIED official version is allowed; bundling only through Modrinth or
  CurseForge, anything else needs written permission; no re-uploading to other platforms; no presenting modified
  versions as original; no derivative works that replicate its protected systems (not defined). It does not grant a
  right to modify or port. Our use (convert the structures, put them in our jar on GitHub releases) is a modified
  re-upload on a platform it does not allow: NOT usable without BlackRift Studios' written permission.
- Some structures reuse layouts of the OWZA Dev Team (their page says so): a second owner to clear.

## 3. Options

| | Source | License | Fits 1.7.10 | Work | Look |
| --- | --- | --- | --- | --- | --- |
| A | McJty's The Lost Cities (github.com/McJtyMods/LostCities) | MIT (repo license, checked) | vanilla blocks; same data format lcpack.py reads (1.20 branch) or 1.12 branch | small: a new pack name in lcpack | plain city, our decay and dressing make it ruined |
| B | ChaosZPack | custom, needs written permission | modded 1.20 blocks, most lost | large | theirs, if allowed |
| C | our own buildings (worldgen v3 building engine, capture tool) | ours | yes | large, ongoing | ours |
| D | A now, C over time for landmark buildings | MIT + ours | yes | small now | grows |

## 4. Recommendation: D

1. Convert McJty's own Lost Cities city data (MIT; credit "The Lost Cities by McJty, MIT" in the release notes, the
   site's credits and a LICENSE notice in the content folder) with tools/lcpack.py as a new pack, namespace
   `lostcities`. Our layers stay: BuildingDress (props, stories), Ruins and decay, wasteland military area, refuges,
   gates, airports, rivers, rails.
2. Drop the DeceasedCraft packs (dc, legacy) from dist/config and from the release.
3. Over time: our own landmark buildings (hospital, police station, mall, apartment towers) with the building engine
   and the capture tool, so the cities become ours.
4. Optional, if the user wants ChaosZPack's look: ask BlackRift Studios for written permission first (the user
   writes; the license points to their official channels).

## 5. Steps for A (when the user says go)

1. Fetch McJty's LostCities source (git clone, MIT) into the scratchpad; find its city data (buildings, parts,
   palettes, styles, multibuildings, street and highway parts).
2. `python3 tools/lcpack.py OUT lc=<data>:lostcities`; fix lcpack where McJty's format differs from DeceasedCraft's
   (palette entries, conditions, variants) [not checked].
3. Server checks on seed 1 (servertest type=decimation): building count, walls (wallscan), props (worlddump +
   propclash), grading, streets, wasteland, refuges, gates, airports still placed.
4. Shots of the new cities (dev test cityprops / views, the site's siteshots rows) and the user's look in game.
5. Remove dc / legacy from dist/config; rebuild the one jar; check its content list has no DeceasedCraft file.
6. Docs: city_engine.md, deceasedcraft_buildings.md marked history, website credits.

## 6. Open (the user decides)

- A (McJty, MIT) instead of ChaosZPack, or ask BlackRift Studios first?
- Until the new pack ships: keep releasing with DeceasedCraft's buildings, or pause public releases?

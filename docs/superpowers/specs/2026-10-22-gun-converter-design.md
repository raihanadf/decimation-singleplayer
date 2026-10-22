# 3D gun converter (docs/ideas.md 27; roadmap "Tools")

User, 21 Oktober 2026: "later, we can make some private web html that convert 3D guns object ... when it click 'convert'
there is convert tools that generate it to bmodel and minecraft decimation styled". 22 Oktober 2026: "setup 2 agent for
big feature: convert 3D gun"; chosen: a model file in, a game gun out (idea 27), two agents (the converter, the game
side), two new worktrees, first milestone one rifle end to end.

## Source models (rule)
Only models we may use and ship: CC0, or CC BY with the credit kept, or the user's own. Each file's author, link and
license go into docs/references/gunconv_sources.md before it is used; the files themselves stay git ignored. Escape from
Tarkov's models are Battlestate Games' work: never taken from the weapon builder or any API (the user asked, 22 Oktober
2026; declined by this session: the repository and releases are public, and a converted copy is still their mesh).
Tarkov stays a picture reference (tools/guns/refcapture.py).

## Parts
1. Converter (agent 1, ../Decimation-gunconv-tool, branch gunconv-tool): tools/gunconv/. A private local page (three.js,
   served by a small local Python server, never published) shows the source model and the converted one side by side;
   Convert runs the converter; an edit step adjusts parts before export. The converter: read GLB / glTF, OBJ (and
   Blockbench .bbmodel if cheap); scale to Decimation's units (31 mm a unit; docs/gun_style_guide.md) and its axes
   (muzzle +x, up -y as the .bmodel has it); cut the mesh into parts (node names where present, else geometry); fit
   each part with Decimation's shape kinds (tapers, shrunk boxes, octagon barrels, skewed grips and magazines); flat
   dark tones; find the attachment points (rail top, muzzle, grip area, magazine well); write a gun spec that
   tools/guns/gunmodel.py builds (.bmodel, texture, icon from the render, .anib from Decimation's timings), then the
   gun package below. Quality as the decimation-gun skill measures it: tools/guns/study.py sheet / gaps / contact /
   vocab against Decimation guns of the same kind. The hard part is the style, not the format: the converter proposes,
   the edit step and the skill's rules refine.
2. Game side (agent 2, ../Decimation-gunconv-game, branch gunconv-game): load gun packages into the game data driven
   (no Java line per gun): register the gun, its magazine, sounds and hands from the "like" gun, loot like it, the
   rows for sights, grips and mounts (sight_offsets.txt, grip_offsets.txt, sight_mounts.txt, the grip rails), aim
   (ScopeZoom learns it), attachments; dev tests gunview, gun, playerfire, attachments with the converted gun. Until
   agent 1's rifle exists, a stand in package made from one of our own guns (the MAC-10's files and its NewGuns line).

## The gun package (the contract)
A folder per gun, written by the converter to ../Decimation-gunconv-tool/build/gunconv/out/<name>/ (git ignored):
- assets/deci/models/guns/<category>/<name>.bmodel
- assets/deci/textures/model/guns/<category>/<name>/<name>.png (model texture)
- assets/deci/textures/items/gun/<category>/<name>.png (icon)
- assets/deci/animations/<name>/<name>Fire.anib, <name>Rack.anib, <name>Reload1.anib, <name>SlideBack.anib
- gun.json: {"name", "display", "category" (Decimation's WeaponCategory name), "like" (the Decimation gun whose sounds,
  hands, stats and loot it takes), "magazine" ("like" for the like gun's magazine, or {"name", "capacity", "like"}),
  "stats" (optional overrides of the like gun's: damage, rpm, ...), "points": {"flame": [x, y, z], "railTop": y,
  "rail": [xFrom, xTo] or null, "gripRail": [xFrom, xTo] or null, "mount": "rail" | "side" | "slide" | null},
  "source": {"file", "author", "url", "license"}}
Either side may add optional fields; neither renames or drops one without the other (the main session merges both).

## Milestone 1
One CC0 / CC BY rifle: converted on the page, its package loaded in the dev client, held, aimed through its iron sights
and a sight, fired, reloaded, a grip fitted, an NPC carrying it; screenshots next to a Decimation rifle; the costs as
any gun (gunperf). Then more guns and kinds.

## Performance
The converter runs offline (seconds a gun is fine). In game a converted gun must cost what one of ours costs: parts
count inside Decimation's range for its kind (study.py gaps), gunperf within a few fps of the like gun.

## Rules for both agents
The project's CLAUDE.md (read it), the decimation-gun skill, test first, commits on their own branch (one plain line,
no attribution, never pushed), no Decimation art or decompiled code committed, no graph edits (the main session updates
the graph after merging), no dash characters in docs, no production runs (the production ports belong to the main
session), never Gradle or a game in another checkout.

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

## Milestone 2: the dataset and exemplar areas (user, 22 Oktober 2026)
User, after previewing the page ("nice btw"): "make dataset full of decimation guns and used as data so the converter can
be good enough"; asked how far, they chose exemplar areas (not statistics only, not a scoring loop). Converter side only
(agent 1, branch gunconv-tool); the package contract does not change.

### What is stored
tools/gunconv/dataset.py reads all 98 Decimation guns from the user's jar with study.py's parser, labels every part with
an anatomy area and stores, per gun and area, an exemplar: the area's box, its run axis, its profile (the section's
extents at 12 stations along that axis), and every part: its 8 corners, shape kind, group, tone (the mean colour of its
texture island), whether it is a detail; plus counts, detail density and the mean tone. LEGAL: this is Decimation's
model data in another form. It lives in build/gunconv/dataset/ (git ignored), is regenerated from the jar by the tool,
and is never committed. The repository gets only the code, these rules and aggregate numbers
(docs/references/decimation_gun_areas.tsv: parts, details and size per area and category, like decimation_guns.tsv).
A gun converted with exemplars is a derivative of Decimation's models, like the UMP9 and HK416 variants: generated
locally, git ignored, it may ship in the release jar as those do.

### Anatomy rules (dataset.label)
Areas: receiver, handguard, barrel, muzzle (device), front_sight, rear_sight, stock, buttpad, grip (pistol grip),
trigger_guard, trigger, magazine, magwell, charging_handle, bolt (bolt or slide), rail, pump, cylinder. A part whose
longest side is under 0.3 is also a detail (rivet, screw, vent, groove) of its area.
1. Anchors per gun: the bore (flamePos y plus 0.85), the muzzle (the front end), the magazine (ammoModel parts), the
   grip (the deepest narrow region hanging under the bore, at most 4.5 long, not at the rear end except on pistols and
   revolvers), the stock end (the grip's rear; without a grip a third of the length), the receiver front (the
   magazine's front plus 12 % of the way to the muzzle, 0.6 to 2.5; without a magazine in front of the grip, 35 % of
   the way from the grip).
2. Names first: ammoModel magazine, slideModel bolt, defaultScopeModel front or rear sight by position,
   defaultStockModel stock, defaultGripModel grip, defaultBarrelModel barrel or muzzle, pumpModel pump,
   revolverBarrelModel cylinder, breakActionModel and minigunBarrelModel barrel. A source file's names count the same
   way (mag, bolt, sight, stock, grip, barrel, muzzle, handguard, trigger, rail).
3. Otherwise by position and size: behind the stock end stock (its rear end buttpad), in the grip region grip, in front
   of the receiver front slender parts on the bore barrel (muzzle within 1.6 of the front), tall parts in the front
   third front_sight, the rest handguard; in the receiver zone magwell over the magazine, thin parts under it between
   grip and magazine trigger_guard, small ones there trigger, small parts out on the right side charging_handle, small
   parts above the receiver's top line rear_sight, a row of 5 or more equal small parts on top rail, the rest receiver.
4. Checks: the rules with names switched off agree with Decimation's own defaultStock, defaultGrip and defaultBarrel
   names (target 80 % of those parts), and renders of two guns per category with one colour per area, read by eye.

### Choosing and refitting an exemplar (exemplar.py)
For each area of the converted gun (labelled by the same rules): candidates are the Decimation guns of the category with
that area; the score is the overlap of the two side outlines, each scaled to its own box, plus the agreement of the box
proportions. The like gun's exemplar is taken unless another scores more than 0.1 better. Refit: every corner of every
exemplar part is carried from the exemplar's box to ours along the run axis, and across it relative to the profile at
that station, so the silhouette and proportions come from the source and the construction (panels, rivets, skewed grip
segments, magazine slices, grooves, bevels) from Decimation. Tones: the area's tone from our palette, each part shaded as
in the exemplar (its tone over the exemplar area's mean). A refit is kept only when its side outline overlaps the
source area's by at least 0.7; otherwise the area keeps the milestone 1 fit. Panels and top plates apply only to areas
that kept the fit.

### Report
gun.json "converter" gets "exemplars" (per area: the gun, the part count and the score, or "fit") and "byGun" (parts
per Decimation gun), shown on the page, so an output is never quietly a clone of one gun.

### Also in milestone 2 (findings of milestone 1)
The icon takes the like gun's icon proportions (Decimation draws rifle icons about twice as tall as true: the AKM's is 32
by 21, akmc's was 31 by 9); its muzzle stays to the right (undecided by the user). akmc's rear sight notch (agent 2: a
single post where the AKM shows notch ears) comes from the rear sight exemplar.

### Quality gates
ak47 and akmc converted again: study.py gaps, vocab and contact against the category, before and after, in a table;
side by side renders with Decimation's akm and one non AK rifle; a page smoke test with agent-browser; conversion time
per gun.

### Performance
The dataset is built once from the jar (seconds, cached until the jar changes); a conversion loads it once per process
and refits a few hundred parts (well under a second more). In game an exemplar gun costs what its parts cost: part
counts stay inside the category's range (study.py gaps).

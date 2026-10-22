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

## Milestone 3: the details (user, 22 Oktober 2026)
User, after the milestone 2 sheet, rear view, icons and in game shots: "still need big improvement yet. so lets do max
effort to this convert feature. still so many missing detail in there". Converter side only (agent 1, branch
gunconv-tool); the package contract does not change.

What milestone 2 left (measured 22 Oktober 2026, docs/gunconv.md section 11): barrel, handguard, magwell, trigger and
trigger guard never passed the overlap gate on either AK; the ak47's milled receiver failed (0.605) because the labeller
cut the source's receiver and magwell at another line than the akm's (the source's lower receiver landed in "magwell");
the akmc's handguard failed (0.489) because its upper and lower handguards differ in length from the akm's; details
(rivets, pins, ribs, levers, strips) exist only where a whole area came from an exemplar.

### 1. Details in the dataset
A detail is a part with its smallest side at most 0.3 that sits on a host: another part at least 3 times its volume that
it touches, with its footprint across one axis inside the host's face and at most 35 % of it, sticking out of the host
by at most 0.45 along that axis (rivets, pins through the receiver, ribs, strips, levers, sight ears, screws, serrations);
a part whose longest side is under 0.35 touching a bigger part is one too. The rule lives in study.py (detail_parts), so
study.py measures any gun the same way. The dataset (version 2) stores per area its details: the face they stand on
(axis and side, or a pin through the area), the place on it (u along the area's run between its ends, v across between
the area's outline edges at that u), the corners relative to that point on the host's surface (size, kind and how proud
it stands stay as Decimation made them), group, tone, the host's tone. Aggregates per category and area: details and
detail density (details a unit of the area's length).

### 2. The detail layer (details.py)
Every area of the converted gun that did not take an exemplar's parts gets the details of its closest exemplar (the one
the area was scored against, the like gun first): placed at the same u and v on our area, the point's height found by
probing our parts along the face's normal (the outermost face there), each corner on our surface at its own place (a
leaning face included, skill lesson 15), the inner face 0.06 inside, the outer face as proud as in the exemplar; a pin
through spans our width plus its overhang. Kept only when it is seated (its inner corners inside our parts), not hidden
inside another part, not inside a part of another area and not within 0.25 of a detail the source already has; long
details shrink with our area when it is shorter than the exemplar's. Report: gun.json "converter" "details" per area
{gun, placed, dropped by reason} and "detailsByGun".

### 3. Construction for every area (exemplar.py)
- Robust boxes: an exemplar is mapped onto our area's box without small outliers (parts under 5 % of the area's volume
  that widen the box by more than 15 %).
- Warp along the run: after the box to box map, the exemplar's side outline columns are matched to ours by dynamic time
  warping (steps that stretch or shrink by at most 2), so a gas block, a sight base or a magwell lands where ours is.
- Assemblies first: action (receiver, magwell, trigger, trigger guard, rear sight, charging handle, bolt, rail), front
  (handguard, barrel, front sight, muzzle, pump) and butt (stock, buttpad) are scored and gated as one outline, so a
  labelling difference at an inner border no longer fails a match. Passed: every area of the assembly takes that gun's
  parts. Measured on the prototype: ak47 action 0.833 and front 0.776 box to box, 0.867 and 0.855 warped (gate 0.7).
- Then single areas (warped), then sections: an area that still fails is judged in bins of 0.5 along its run; the best
  exemplar takes the bins where its outline overlaps ours by 0.7, a second exemplar the bins left, our fit the rest
  (parts cut at the bin edges). The report lists each exemplar with its bins.
- Exemplar parts keep their own group (a charging handle knob on Decimation's bolt carrier stays slideModel; iron sight
  parts are defaultScopeModel only when the like gun folds its own).
- Smoothing: stock and grip follow a smoothed profile (a straight line per edge where it strays under 0.15), so the
  exemplar's slices stay boxes and tapers instead of wedges.
- Proportions: a front or rear sight that keeps the fit and is taller or longer than the category's q90 is scaled about
  its base to the category's median.

### 4. Metrics
study.metrics gets "details" and "detail%" (gaps compares them with the category); `dataset.py density PACKAGE` prints
per area the details and detail density against the category's medians. Results before and after for ak47, akmc and
the new guns in docs/gunconv.md.

### 5. Generality
Four guns without a Decimation twin, of other kinds, recorded in docs/references/gunconv_sources.md before use: the
Mossberg 590A1 (pump shotgun, J-Toastie, CC BY 3.0), the SIG P228 (pistol, PuKkBuMXDD, CC BY 3.0), a vz. 61 layout
submachine gun (Kutejnikov, CC BY 4.0) and a Colt Lightning style pump action rifle (LonesomeDucky, CC0). Each compared
with two Decimation guns of its category on sheets, split, rear and low views.

### 6. Checks
Test first (tools/gunconv/tests). Every change looked at next to two Decimation guns of the same kind (study.py sheet,
render --split, rear and low); what was seen goes into docs/gunconv.md and docs/shots_index.md. study.py contact must not
list a transferred detail. In game at the end in this worktree's dev client (live ports 25595 and 5009): gunview hip,
aim and NPC for each converted gun next to its like gun, and the gunpack test. A final sheet with each gun beside its
closest Decimation guns and an honest verdict per gun.

### 7. Performance
The dataset build adds the detail rule (one pass over each gun's part pairs, boxes first). A conversion adds the warp
(a few hundred columns squared at most) and the detail placement (a few hundred probes): seconds. In game a converted
gun costs what its parts cost; the part count stays inside the category's q10..q90 (study.py gaps).

### Status (22 Oktober 2026)
Converter side done: docs/gunconv.md section 11 (what was built, results before and after, the in game checks, the
verdict per gun, what is still weak). Beyond this section on the way: per part source colours, greys mapped by area,
Decimation's pistol scale, donors from the closest guns, wood against steel agreement, holes filled from the fit, details
thinned on squeezed exemplars.


## Exact mode (user, 22 Oktober 2026)
User: "add 1 method converter that only convert 3D gun to bmodel with same 1:1 output, so its basicly just changing the
format"; then "yes build exact mode. and for the texture still using decimation style". A second way to convert beside
the Decimation style one; the package contract does not change.

### Geometry (exact.py)
The source mesh as it is, placed like the Decimation style conversion (axes, like gun, scale, alignment, z centre): no
exemplars, no shape kinds. Before the boxes the mesh is prepared per piece (one material and skin joint each, as now):
vertices welded, simplified to a face budget (default 800, 300 to 2000) by quadric edge collapse that keeps border and
sharp edges (dihedral over 40 degrees weighted) and refuses collapses that flip a face, then coplanar neighbouring
triangles merged into quads (normals within 3 degrees, the quad convex), then faces too small to see dropped (longest
side under 0.06 units). The budget is shared by the pieces by surface area.

Every face becomes one part: a thin plate, the face itself on both sides, 0.02 thick along its normal (a quad exactly, a
triangle as a quad with two corners on one point), its corners in the box's right handed order so each side's normal
points out of it. Two faces 0.02 apart cannot fight for a pixel and each side is lit from its own normal, with or
without back face culling in Decimation's renderer.

### Groups, tones, points
Groups as now: magazine and bolt from names, else the like gun's anatomy boxes (reload and fire animate). Tones Decimation
style: the face's material or source colour through the like gun's palette (greys by area), one colour files from the
like gun's parts by position; gunmodel.paint gives every part its shade, gradation and noise. Hands, sounds, aim line and
attachment points from the like gun as now. gun.json "converter": {"mode": "exact", "faces", "budget"}.

### Cost gate
In game, measured: each gun's draw time (gunpack perf=true: median, glFinish around, GPU included) and its display lists'
graphics memory (vmmap -summary, IOAccelerator, before and after its first draw) against Decimation's akm, with ModelBatch
on in the dev client. Budget: draw time at most 1.5 x the akm's (+0.05 ms). The page shows the part count and an estimated
cost from a fit of those measurements before export, warns above the budget and refuses an export above twice it.

### Page
A second button "Exact" beside Convert, the face budget slider, the part count and the estimated cost.

### Checks
Test first. The six guns (ak47, akmc, m590, p228, csmg, lightning) converted exact: in game hip, aim and NPC, gunpack
perf=true against their like guns, memory against the akm; a sheet of each gun exact beside its Decimation style version
and its closest Decimation gun.

### Exact mode status (23 Oktober 2026)
Built and measured: docs/gunconv.md section 12. Changed on the way: one quadric queue over all pieces instead of a budget
shared by area (thin bars broke apart); the default budget is 350, measured within 1.5 x the akm (800 drew about 2.2 x).


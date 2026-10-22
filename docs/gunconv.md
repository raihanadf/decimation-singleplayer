# 3D gun converter (tools/gunconv)

A 3D gun model in, a Decimation styled gun package out (docs/ideas.md 27; spec
docs/superpowers/specs/2026-10-22-gun-converter-design.md). Agent 1 builds the converter (this doc, branch gunconv-tool),
agent 2 the game side that loads the packages (branch gunconv-game). Milestone 1, converter side: done 22 Oktober 2026.
The converted rifles have not been in the game yet: that is agent 2's part of milestone 1.

## 1. Run it

| What | How |
|---|---|
| The page (private, local) | `python3 tools/gunconv/server.py [--port 8790]`, then open http://127.0.0.1:8790/ . Listens on 127.0.0.1 only, never published. three.js 0.160.0 comes from cdn.jsdelivr.net, so the page needs the network. |
| A conversion without the page | `python3 tools/gunconv/convert.py MODEL --name NAME --display TEXT [--like GUN] [--category CAT] [--length UNITS or --length-mm MM] [--muzzle +x] [--up +y] [--mirror] [--author A --url U --license L]` writes build/gunconv/out/NAME/ |
| Compare with Decimation's guns | `STUDY_OURS=$PWD/build/gunconv/out/NAME/assets/deci python3 tools/guns/study.py gaps ours:NAME` (also sheet, render, vocab, contact, attach) |
| Tests | `python3 -m unittest discover -s tools/gunconv/tests -t tools/gunconv/tests` (73 tests, about 12 s; tests with a like gun need Decimation.jar, two need the git ignored samples) |

Folders (all git ignored under build/gunconv): inputs/<folder>/ holds a sample model and its source.json
({"author", "url", "license"}); work/uploads/ the files opened on the page; out/<name>/ the packages.

The page: Model (a sample or a file of yours, with its author, link and license), Conversion (name, display name,
category, like gun, the real length in mm, muzzle and up directions when the guess is wrong, mirror), Convert. The source
model shows as loaded (three.js GLTFLoader, OBJLoader with its MTL, the server's triangles for .bbmodel), turned into
the converted gun's pose once converted, both views share the camera (Side, Top, Three quarter, Rear; Part colours
shows the cut, Like gun outline overlays Decimation's like gun). Edit: click a part in the view or in the list, then
its group, its kind (cuboid, taper, wedge, skew, octagon, split: tools/gunconv/edit.py), move it by the step (Back,
Forward, Up, Down, Left, Right in the gun's own directions), Drop part, Undo. Attachment points are editable. Export
writes the package and shows study.py's gaps table against the category.

## 2. Source models

Only CC0, CC BY with the credit kept, public domain or our own (spec, "Source models"). Each file gets its row in
docs/references/gunconv_sources.md before it is used; the files stay under build/. Export refuses any other license
(package.license_ok: CC0, CC BY of any version without NC, ND or SA, public domain, own) and a source without an
author; gun.json keeps the credit. Never models of other games (Escape from Tarkov and the like), from a site or an API.

## 3. How a conversion works (code map)

| Step | Where | What |
|---|---|---|
| Read | meshio.py | GLB and glTF (buffers in the GLB, data URIs, files beside the model only), node transforms, skinned meshes in their joints' pose (the mesh node's own transform ignored, as the glTF spec says), each vertex's main joint kept as its label; base colour times the texture sampled at each vertex, vertex colours. OBJ with its MTL (Kd, map_Kd), objects, groups, materials. Blockbench .bbmodel cubes (with rotation and inflate) and mesh elements, group paths as names. Draco and meshopt files are refused with a message. Pure Python. |
| Axes | fit.orient, fit.rotation | Length = the longest extent, height = the next. The muzzle is the end whose last eighth is lower (a barrel, not a butt); down is the side that sticks out more beyond its usual line (magazine, grip, guard, stock toe) than the other (sights). Decimation's model space: x to the muzzle, y down, z = minus (forward x up), so the right side (ejection port, AK charging handle) lands at -z as on Decimation's guns. A rotation, never a mirror (--mirror on request only). |
| Like gun | match.candidates | Side silhouettes normalised to one length, overlap (IoU, small shifts tried) with every Decimation gun, of the category when given. The best is "like": its hands, sounds, stats, loot and magazine are the game side's. |
| Scale | convert.convert | The real length (31 mm a unit) when given, else the like gun's length, else the category's median. Slimmed in z to 1.15 times the like gun's width when wider (Decimation draws guns slimmer than real). |
| Place | match.align, match.front_tip, convert.body_z | From the like gun's bounding box centre, the best overlap of the side silhouettes within 4 units along and 2.5 up or down (0.125 steps); then the front sight tip (the highest point of the front 22 %) onto the like gun's, its sight line, because Decimation draws every gun in one aim pose (moved only when within 1.5); z centred on -0.15 by the median of the top view's column middles at 64 pixels a unit (PIL fills rows floor(a) to floor(b): 8 pixels a unit was 0.06 off). |
| Pieces | convert.pieces_of | Connected parts of each mesh (one material each; vertices welded by position), split by skin joint. Label: the joint, else the node or object name, else the material. |
| Groups | convert.groups_of | Names first: mag, clip, ammo, cartridge, bullet, shell = ammoModel; bolt, slide, carrier, charg, cocking = slideModel; sight, iron, notch, aperture = defaultScopeModel when the like gun folds its own iron sights, else gunModel. A file that names none of a kind uses the like gun's anatomy: pieces centred inside its magazine, bolt or folding sight box. |
| Tones | convert.palette_tones, match.palette, match.template_tone | The like gun's palette measured on its texture: our greys keep their order and take its metal greys (5th to 95th percentile), coloured materials its body tone or, on the magazine, its magazine tone (Decimation's akm: metal 10 to 46, wood about 38,24,21, bakelite magazine about 88,57,47). A file with one colour takes the tone of the like gun's part it overlaps most. Without Decimation.jar: convert.tone (greys into 24 to 64). |
| Fit | fit.fit_piece | A loft along the piece's longest axis: two axis aligned end rectangles joined by straight edges, which is a Decimation shape box and gives its kinds (taper, cuboid, skew, wedge). Cut where a section jumps (fit.step_cuts, scanned every 0.03: a thin post or a sight block becomes its own part) and where a straight loft strays more than 0.08 (fit.segments; a curved magazine becomes slices). Chamfered tips at a piece's ends are left out of the fit (they pulled the AKM's grip and magazine to slivers). Round bars along x become Decimation's octagon (3 parts a segment). End faces within 0.05 of each other become a plain box. |
| Clean up | convert.convert | Parts under 0.08 dropped; parts inside another dropped (cartridges in a magazine); body parts longer than 1.5 cut into panels (fit.panels); panels whose ends are offset by under 0.08 levelled (fit.snap_part); bulky body parts open on top get a top plate 0.2 thick with bevelled long edges (fit.layer_top, Decimation's layered receivers). |
| Points | convert.points_of | flame: muzzle tip minus 0.1, 0.85 above the bore (the median centre of the body parts reaching within 0.4 of the muzzle), z -0.15. railTop: sightfit.rail_top's rule (match.rail_top). rail: parts named rail or picatinny above the bore, else along the rail top when the like gun mounts sights on a rail. gripRail: rail parts below the bore. mount: the like gun's row in sight_mounts.txt. |
| Package | package.export | tools/guns/gunmodel.build writes the .bmodel, texture (tones with Decimation's gradation), icon (the model's render) and the four animations (gunmodel.smg_animations on Decimation's timings; the bolt kicks 2.4 on rifles, 1.6 on SMGs); the header's hands and ejection come from the like gun; gun.json. |

## 4. The package

The contract is the spec's ("The gun package"): build/gunconv/out/<name>/ with assets/deci/models/guns/<category>/<name>
.bmodel, the model texture, the icon, the four .anib files and gun.json. An export replaces the folder whole, and
nothing else is written into it. Optional field of ours in gun.json: "converter": {"version", "parts", "likeCandidates"
(the five closest Decimation guns with their silhouette overlap), "warnings"}. A name must be 2 to 32 lowercase letters
and digits, not one of Decimation's guns (its assets share the deci folders) and not one of ours.

## 5. Milestone 1 results (22 Oktober 2026)

Two packages, both like akm (Decimation's AKM), compared with study.py against Decimation's 53 rifles:

| | ak47 (the milestone rifle) | akmc |
|---|---|---|
| Source | "High poly AK-47" by Lamoot, CC0 1.0, OBJ, 30,730 triangles, one object, one material | "AKM" by J-Toastie, CC BY 3.0, GLB, 5,247 triangles, 9 materials, skinned (Root, Magazine, Trigger, Bolt) |
| Convert time | 2.9 s (the first conversion of a process also reads Decimation's 99 guns, about 1 s) | 1.6 s |
| Parts | 205 (gunModel 178, ammoModel 21, slideModel 6); akm 194, rifles median 174, q10 .. q90 102 .. 300 | 153 (gunModel 137, ammoModel 12, slideModel 4) |
| Shape kinds (study.py vocab) | taper 66, cuboid 27, skew 7 % (akm 68, 30, 2; rifles median 60, 31, 3) | taper 75, cuboid 24, skew 1 % |
| Tone, tone spread | 31.7, 12.5 (akm 29.3, 15.5) | 31.8, 12.3 |
| study.py gaps, outside q10 .. q90 | 2 of 19: corner offset 0.43 (q90 0.40), parts in the top two fifths 89 % (q90 87) | 2 of 19: middle part side 0.70 (q90 0.60) and the same relative to the length |
| study.py contact (parts not seated) | 2 (Decimation's akm 5, ak74 5) | 7: 3 pins that pass through the receiver (no corner inside a part, like collars), 2 rear sight wings, the guard bar |
| Sight alignment | the front sight tip step moved the gun 0.06: body and sights both match the akm | moved 0.42: its front sight is taller than the akm's, so its body sits 0.42 lower than the akm's |

Verdict: both read as AKs in Decimation's style at first glance: the akm's colour families, its blocky panelled build,
curved magazines in tapered slices, octagon barrels and gas tubes, the top profile within about 0.1 of the akm along the
receiver, the iron sights on the akm's sight line. They are plainer than Decimation's hand made akm: no rivets, selector
or port details on the receiver side (the sources do not have them), the pistol grips are straight blocks, the AKM's
trigger guard is thin. An automatic fit gets the anatomy and the proportions; the detail of the skill's lesson 12 still
needs the edit step and a modeller's pass (decimation-gun skill, steps 3 and 4). Pictures: docs/shots_index.md,
gunconv_v0.66.1.

What changed the look most on the way (each from a measured fault, tests in tools/gunconv/tests): leaving chamfered
tips out of the fit (grip and magazine were slivers), step cuts (the guard's thin post had become a wedge), the like
gun's palette (one narrow grey band before: tone spread 4.9, now 12.3 and 12.5), panels and bevelled top plates (84
big parts before), levelled gentle slopes (AK-47 skews 18 % to 7 %, AKM 11 % to 1 %).

## 6. Decisions made in milestone 1, with the reason

- The milestone rifle is the CC0 AK-47, not the AKM: no attribution duty, a gun Decimation does not have (name ak47 is
  free; akm is Decimation's), and its sights and body both sit where the akm's do (0.06 against 0.42). The AKM stays as
  the second package: it exercises names, materials and skin joints.
- The like gun is the template for everything the file does not say: hands, aim line, anatomy groups, tones.
- Iron sights fold (defaultScopeModel) only when the like gun folds its own: Decimation's akm keeps its AK sights up.
- Constant width magazine slices (the style guide's MP5 recipe) were dropped: Decimation's akm uses tapered slices (10
  of 13).
- Levelled panels: Decimation's akm has a level receiver top; a slope under 0.08 a panel is below a texel.
- Upper and lower core panels were tried and reverted: they made parts wider, not thinner (middle side 0.70 to 0.77).
- Animations: gunmodel.smg_animations (our MAC-10 values on Decimation's timings, accepted by the user) with a 2.4 bolt
  kick on rifles, as Decimation's rifles; the magazine path is the MAC-10's [not seen on a rifle in game].
- study.py reads a package through STUDY_OURS (one line), and its render scale bug was fixed (bug.md): every render had
  been drawn at the part count in pixels a unit, so sheets of guns with different part counts were not at one scale.

## 7. Not verified, known limits

- Nothing of the converted guns has run in the game yet (agent 2): hands, aim picture, attachments, reload, NPC use,
  gunperf. The aim line is matched by numbers only [not verified in game].
- The akmc body sits 0.42 lower than the akm's: the like gun's right hand may hover over its grip [not verified].
- A Blockbench .bbmodel goes through the reader's unit tests only, not through the page in a browser [not verified].
- Large files: everything is pure Python; the 30,730 triangle AK-47 takes 2.9 s, a 100,000 triangle file [not
  measured] may take a minute.
- Labels of a one material file are its material name ("Preview_Material" for every AK-47 piece): the list does not
  name the anatomy.
- A loop or an L shaped piece lofted along a diagonal is still approximated by boxes; steep diagonal bars come out as
  steps.
- Decimation's akm inventory icon faces left; gunmodel's icons face right (as the accepted MAC-10's) [not analysed].
- Draco and meshopt compressed GLB files are refused: export them again without compression.

## 8. For the game side (agent 2)

- Packages: build/gunconv/out/ak47 (the milestone rifle) and build/gunconv/out/akmc in the converter's worktree
  (/Users/ucilmenangis/Projects/Decimation-gunconv-tool). Both like akm, category rifle, magazine "like", stats {},
  mount "side", rail and gripRail null.
- The .bmodel header already holds the akm's rhPos, rhRot, lhPos, lhRot and ejectPos, and flamePos = points.flame.
- Groups: gunModel, ammoModel (the whole magazine follows the Reload1 ammoModel0 pose) and slideModel (the bolt and
  charging handle, kicked by Fire and held by SlideBack). No defaultScopeModel parts (the akm keeps its sights up).
- railTop follows sightfit's rule. `python3 tools/guns/sightfit.py` (with STUDY_OURS set to the package): akm rail top
  -4.72, ours:ak47 -4.71, ours:akmc -4.41 (+0.04, under sightfit's 0.06 tolerance): neither needs a sight_offsets.txt
  row; sights sit on them as on the akm.
- gun.json may hold the optional "converter" field; ignore it.

## 9. Performance

Offline: 1.5 to 6 s a gun. In game a converted gun must cost what one of ours costs: its part count (ak47 205, akmc
153) sits inside Decimation's rifle range (akm 194); gunperf against the akm is agent 2's [not run].

## 10. Milestone 2: the dataset and exemplar areas (22 Oktober 2026, done on the converter side)

Spec: docs/superpowers/specs/2026-10-22-gun-converter-design.md, "Milestone 2".

| Piece | What |
|---|---|
| tools/gunconv/dataset.py | The anatomy labeller and the dataset. Anchors: bore, magazine (its main cluster; one lying on top is ignored), grip (looked for beside the magazine first, a run beside skipped magazine columns rejected, a too long run cut to its deepest part, in front of the magazine on bullpups: magazine in the rear 40 % with a full depth grip ahead), stock (below the receiver behind the grip; at receiver height only behind the receiver core), receiver front. Barrels are found by section (short slices on the bore line count). `python3 tools/gunconv/dataset.py build` writes build/gunconv/dataset/decimation_areas.json (98 guns, 16,757 parts, 5.2 MB, 13 s; git ignored, made again when the jar changes) and docs/references/decimation_gun_areas.tsv (medians per category and area, numbers only); `check`, `render GUN`. |
| tools/gunconv/exemplar.py | Per area of the converted gun: candidates of the category, score = normalised side outline overlap (0.7) and box proportions (0.3), the like gun's unless another scores 0.1 better; refit along the source's profile for magazine, grip, stock and buttpad, box to box elsewhere (a profile map stretched the AKM receiver's sight details into strands); kept when the side outline overlaps the source area's by 0.7; tones: our area's tone shaded like the exemplar. |
| convert.py, package.py | exemplars=True by default; panels and top plates only where the fit stays; gun.json "converter" version 2 with "exemplars" (per area: gun, parts, score, overlap, or "fit"), "byGun", "fromExemplars", "fromFit"; the page lists them. The icon takes the like gun's icon proportions (package.icon_like). |

Labeller check: with names switched off it agrees with Decimation's own stock, grip and barrel names on 448 of 539
parts (83.1 %; target 80; 69.6 % at first). Read by eye on area colour renders of the AKM, M4A4, AUG, MP5A3, Uzi, Glock
17, R870 and PKM (docs/shots_index.md, gunconv_v0.66.1). Still labelled differently from Decimation's names: the
Thompson's "barrel" group (it covers receiver plates), bipods named barrel on the FAMAS-I.

Results (study.py against Decimation's 53 rifles; before = milestone 1):

| | ak47 before | ak47 after | akmc before | akmc after |
|---|---|---|---|---|
| parts | 205 | 204 (74 from exemplars: akm 64, ak74 7, insas 3) | 153 | 162 (120 from exemplars: akm 111, vz58 6, mpt76 3) |
| gaps outside q10 .. q90 | 2: offset 0.43, top2fifths 89 % | 3: middle side 0.63 and relative, tone spread 9.3 (q10 9.4) | 2: middle side 0.70 and relative | 2: offset 0.43, taper 77.2 % (q90 76.5) |
| vocab taper / cuboid / skew | 66 / 27 / 7 % | 70 / 23 / 8 % | 75 / 24 / 1 % | 77 / 19 / 4 % |
| contact, parts not seated | 2 | 3 | 7 | 7 |
| tone spread (akm 15.5) | 12.5 | 9.3 | 12.3 | 15.1 |
| icon box (akm 32 x 21) | 30 x 9 | 32 x 23 | 31 x 9 | 32 x 22 |
| convert time | 2.9 s | 9.8 s | 1.6 s | 3.6 s (13.5 s on the page, first conversion of the server) |

Verdict: akmc gains the most: its receiver, stock, grip and magazine are Decimation's AKM construction refitted to the
source's outline (receiver overlap 0.89), its middle part size and tone spread now sit where Decimation's do, and its
rear view shows the AKM's receiver top with the rear sight block and a front post with ears (agent 2's "single post"
finding; [not verified in game]). ak47 takes stock, grip, magazine, bolt, front sight and muzzle from exemplars; its
milled receiver does not pass the gate (its outline differs from every rifle receiver) and stays fitted. Barrel,
handguard, magwell, trigger and guard did not pass the gate on either gun. Neither gun is a clone: the report names
every source gun per area, akm gives 31 % (ak47) and 69 % (akmc) of the parts.

Not verified: the converted guns in game after milestone 2 (agent 2's loader reads the same package format); the aim
picture with the new rear sight; icons in the inventory.

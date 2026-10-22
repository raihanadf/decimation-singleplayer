# Gun model and animation spec (Decimation 1.21.10f)

How a gun's 3D model, textures, animations and sounds are found, parsed and
drawn. Written 2026-10-07 from the decompiled code (`deobf/src`) and the 98
shipped guns. Use it to build a new gun that works on the first try.
Statements marked `[inferred]` were read from code but not seen in game.

Code map (readable names, obfuscated name in brackets):

| Class | Role |
|---|---|
| `GunItem` (`deci.ay.i`) | the gun item; builds every path below from `gunName` + `category` |
| `BModelLoader` (`deci.n.g`) | parses `.bmodel` text files |
| `BModel` (`deci.n.f`) | loaded model: parts, anchor points, scope glass, casings |
| `BModelPart` (`deci.n.b`) | one named part (a ModelRenderer) |
| `GunAnimation` (`deci.F.a`) | parses and plays `.anib` files |
| `GunItemRenderer` (`deci.K.b`) | first/third person drawing, sway, recoil, attachments |
| `AttachmentItem` | attachment items; their models are separate `.bmodel`s |

## 1. Files one gun needs

For a gun registered as `new GunItem("ak74", stats, WeaponCategory.rifle, ammo)`
(category folder = the enum name: `rifle`, `smg`, `mg`, `shotgun`, `pistol`,
`revolver`, `rocket`, `crossbow`):

| File (inside the jar, `assets/deci/...`) | Required |
|---|---|
| `models/guns/rifle/ak74.bmodel` | yes, loaded once at item construction (client) |
| `textures/model/guns/rifle/ak74/ak74.png` | yes, the model texture |
| `textures/model/guns/rifle/ak74/ak74_<skin>.png` | per weapon skin (spray can) |
| `textures/items/gun/rifle/ak74.png` | inventory icon (inventory does NOT draw the 3D model) |
| `animations/ak74/ak74Fire.anib` | yes, played on every shot |
| `animations/ak74/ak74Reload1.anib` | yes, played on reload key, drives the actual reload |
| `animations/ak74/ak74Rack.anib` | yes for bolt/charge guns (played when racking) |
| `animations/ak74/ak74SlideBack.anib` | optional (6 of 98 guns lack it; its absence is silently ignored) |
| `sounds/guns/rifle/ak74/fire.ogg`, `fire_distant.ogg`, `fire_suppressed.ogg` | always registered |
| `.../magIn.ogg`, `magOut.ogg`, `rack.ogg` | magazine guns and revolvers (`rocket`: magIn/magOut only) |
| `.../insert_shell.ogg` | guns with shell-by-shell ammo |
| `.../pump.ogg` | guns with the PUMP fire mode |

Note the animation folder has no category level: `animations/<gun>/`.

## 2. The `.bmodel` format

Plain text, read line by line by naive `contains`/`substring` matching (no real
parser, so keep to the exact shapes below; one statement per line).

Header lines (all optional, values are model units, 1 unit = 1/16 block):

| Line | Meaning | Default |
|---|---|---|
| `mOff: x, y, z;` | whole model offset | 0,0,0 |
| `sPos: x, y, z;` | sight position (used to centre the gun on screen when aiming) | 0 |
| `flamePos: x, y, z;` | muzzle: flash, smoke, barrel attachments are placed here | 20,-4,0 |
| `ejectPos: x, y, z;` | where spent casings spawn | 0 |
| `rhPos: x, y, z;` / `rhRot: x, y, z;` | right hand (trigger hand) pose, rot in degrees | -9,0.72,-2 / 0 |
| `lhPos: x, y, z;` / `lhRot: x, y, z;` | left hand (support hand) pose | 6.5,7.2,3.5 / 0 |
| `Scale: s;` | model scale | 1 |
| `textureWidth = 512;` / `textureHeight = 32;` | texture size for UV mapping | |

Parts (Techne / Toolbox style export):

```
gunModel0 = new ModelRenderer(this, 1, 1);       // also WW2ModelRenderer / BeardieModelRenderer
gunModel0.addShape(x, y, z, {{..},{..},{..},{..},{..},{..},{..},{..}}, w, h, d);  // 8 corner offsets
gunModel0.setRotationPoint(x, y, z);
gunModel0.setRotation(rx, ry, rz);               // radians; " / rotFix" suffix allowed
parent.addChild(child);                          // child is removed from the top level list
```

- The variable name becomes the part's name. It is what animations target.
- `new ...ModelRenderer(this, u, v)` gives the texture offset of the part.
- Numbers may carry an `F` suffix (`1.5F`); it is stripped.
- **Use `addShape`, never `addBox`.** `BModelLoader` passes the Y offset as
  the Z offset for `addBox` (bytecode confirmed: same local pushed twice), so
  any `addBox` part lands in the wrong place. All 98 shipped guns use
  `addShape` only (16757 lines, 0 `addBox`). A plain box is an `addShape`
  with all corner offsets 0.
- `addBox`/`addShape` lines containing a `"` are ignored.

### Part names that the code treats specially

| Name contains | Effect |
|---|---|
| `ammoModel` | every part whose name contains `ammoModel` follows any animation pose whose name contains `ammoModel`, so one pose moves the whole magazine |
| `scopeGlass` | rendered with the scope texture: originally a second world render (render-to-texture zoom); since v0.28.0 the centre of the zoomed frame (fixes/ScopeZoom), old way behind `pictureInPicture` |
| `scopeOverlay` | drawn semi transparent over the scope, texture `textures/model/guns/scopes/<scope>.png` |
| `leftArm` / `rightArm` | used in arm models, not gun models |

Every other name is free. The shipped guns use these conventions (only
meaningful because their `.anib` files target them by name):
`gunModel*` body, `ammoModel*` magazine, `slideModel*` slide/bolt carrier,
`pumpModel*`, `breakActionModel*`, `revolverBarrelModel*`,
`minigunBarrelModel*`, `bolt*`, `defaultBarrelModel*`, `defaultScopeModel*`,
`defaultGripModel*`, `defaultStockModel*`.

Important: **Decimation's own code hides no `default*Model` part when an
attachment is fitted** (no reference to those names anywhere). Since
v0.40.0 our patch does it for sights: `defaultScopeModel*` parts of the gun
being drawn are skipped while its NBT `sightAttach` is set
(fixes/IronSights + tools/patches/PatchIronSights.java: begin / end around
GunItemRenderer deci.K.b.a(ItemStack, Object[]), skip in BModel.renderParts
deci.n.f.bm). Only the gun's own model: the Dragunov scope attachment has
defaultScope parts of its own. 19 guns have defaultScope parts (crotchbow,
barettam9, acr, ar15, ar15beowulf, asval, aug1, aug3, aug3para, famasi,
g36c, honeybadger, jng90, m110, m4a4 (its folding rear sight, not its
A-frame front sight), mpt76, aa12, mpi40, vector). `defaultBarrel`,
`defaultGrip`, `defaultStock` parts still always render. Name the iron
sights of a new gun `defaultScopeModel*` (the build scripts use the part's
group as the name prefix).

## 3. The `.anib` animation format

```
Length: 57          // number of frames (frame slots are created up front)
Hand: 0             // 0 or 1, see below
STATIC              // optional: never advances (SlideBack uses this to hold a pose)
---------------------------
START
Frame {             // a keyframe
 OffHand { Pos: x, y, z; Rot: x, y, z; }    // support hand
 Model   { Pos: ...; Rot: ...; }            // whole gun
 slideModel0 { Pos: -2.4, 0, 0; Rot: 0, 0, 0; }   // any part, by exact name (case insensitive)
}
Frame SKIP          // an in-between frame: interpolated from the keyframes around it
RAND Frame { ... }  // every value scaled by a random 0..1 (fire jitter)
SWITCH Frame { ... }    // on this frame: unload (sends PacketReload())
LOAD Frame { ... }      // on this frame: load (sends PacketReload(1))
TRYBOLT Frame { ... }   // on this frame: if the gun was empty, chain into the Rack animation
12 REPEAT Frame { ... } // jump back to frame 12 while ammo is missing (shell by shell reload)
PlaySound: MagOut;      // plays deci:<gunName>MagOut on the current frame
Shake: 1.2;             // camera shake on the current frame
END
```

- Poses are offsets added to the part's rest pose: `Pos` in model units,
  `Rot` in degrees. Parts not named in a frame inherit the pose they have in
  other frames (the loader fills every frame with every named part).
- Playback: one frame per client tick (20 per second), advanced in
  `PlayerData.clientTick`. A 57 frame reload lasts about 2.9 s.
- When the animation ends every animated part snaps back to its rest pose.
- `PlaySound` names are appended to the gun name, so they must match the
  registered sounds: `Fire`, `MagIn`, `MagOut`, `Rack`, `Pump`, `LoadShell`.

What the shipped guns do (useful as templates):

| Animation | Frames (min/avg/max) | Typical content |
|---|---|---|
| Fire | 1 / 3 / 19 | `RAND Frame` kicking `slideModel*` back; Hand 0 (90 guns) |
| Rack | 1 / 16 / 19 | slide/bolt travel, `PlaySound: Rack;`, `Shake`; Hand 1 for 70 guns |
| Reload1 | 46 / 56 / 57 | magazine out (`SWITCH`), `PlaySound: MagOut/MagIn`, magazine in (`LOAD`), `TRYBOLT`; 2 guns use `REPEAT` |
| SlideBack | 1 | `STATIC`, holds the slide back when the gun is emptied |

`Hand` [inferred]: the muzzle flash/smoke offset follows the animation's
`Model` rotation only when `Hand` is 0; it also selects which arm follows the
`OffHand` pose.

When each plays: Fire on every shot (`GunItem`), Reload1 on the reload key
(`KeyBindingHandler`), Rack when a `TRYBOLT` frame finds the gun empty
(`PlayerData`) or when racking, SlideBack after the last round if the gun was
bolted.

## 4. Drawing (GunItemRenderer)

- Inventory: not handled by the 3D renderer; the flat icon is used.
- First person: fixed base transform, then aim mode offset (`sPos` vs `rhPos`
  centre the sights), idle sway from `ClientState.swayClock` (sin waves; the
  clock is the SmoothSwingThread, see bug.md), recoil from
  `GunItem.cameraKick` (halved by a foregrip), head yaw sway, then the current
  animation's `Model` rotation, then the gun at 2x scale.
- Overlays (fancy graphics only): the model is drawn again with
  `textures/particle/mud/mudN.png` and `textures/particle/blood/bloodN.png`
  when the gun is dirty, so the model's UVs are reused for dirt.
- Attachments: barrel attachment at `flamePos / 21` plus fixed offsets, grip
  and sight at fixed offsets (with special cases for mp7, bayonet, dragunov
  scope, red dot). Attachment models: `models/attachments/<slot>/<name>.bmodel`,
  textures `textures/model/attachments/<slot>/<name>.png`. Fixed offsets mean a
  new gun's rails must sit where the existing guns' rails sit, or attachments
  will float [inferred].
- Muzzle flash and smoke puffs at `flamePos`; casings spawn at `ejectPos`
  while the client cooldown runs.

## 5. Checklist for a new gun

1. Model (our way: tools/guns/gunmodel.py, section 6 and the skill
   decimation-gun; or by hand in Techne/Toolbox), every box as a shape box (`addShape`). Name the
   body `gunModel*`, the magazine `ammoModel*`, the moving slide/bolt
   `slideModel*`. Export, add the header lines (`flamePos`, `ejectPos`,
   `sPos`, `rhPos`, `lhPos`, texture size).
2. Texture at the model's UV layout, plus a 16x16 style inventory icon.
3. Copy the four `.anib` files of a similar gun, rename them, and keep the
   part names they target identical to your model's names.
4. Add the sounds (or copy a similar gun's).
5. Register: clone an existing `new GunItem(...)` line in `ItemRegistry`
   (`deci.aD.k`) with the new name and stats (`create_weapons.md`), as a
   Javassist patch or from our own mod.
6. Test in the dev workspace (`dev/`, `runClient`).

Fastest path: start from an existing gun of the same action type (copy its
`.bmodel` and `.anib` files) and change the shape, so the animation part
names already match.

## 5b. Our own sight attachments (v0.41.0)

tools/guns/sights.py writes, per sight: models/attachments/sight/<name>.bmodel (the red dot's
header lines, parts in file space = gun space minus (0.8, 1.12, -0.128)),
textures/model/attachments/sight/<name>.png, textures/items/attach/<name>.png (icon, the item's
registerIcons uses deci:attach/<name>), textures/model/guns/scopes/<name>.png (reticle). All under
assets/deci in our jar (our own art, committed). fixes/NewSights registers them through
Deci.newSightAttachment, which switches FML's active mod container to Decimation's for the
AttachmentItem constructor (GunItem.getSightAttachment looks up GameRegistry.findItem("deci", ..)).
Reticle: fixes/SightReticle draws textures/model/guns/scopes/<name>.png as a square on the
glass's eye side (assets/deciworldgen/sight_reticles.txt from sights.py) in the gun's matrix,
from IronSights.end, so it sways and kicks with the gun (v0.41.1).
BModel.renderParts skips parts named scopeGlass / scopeOverlay; for scope attachments
renderGun(-26.42) draws only scopeGlass parts (through PatchScope's projective glass).

## 6. Our own guns: the tools/guns pipeline (v0.36.0, MAC-10 pilot)

Look and construction rules: docs/gun_style_guide.md (study of all 98
Decimation guns, 10 Oktober 2026); read it first.

Every asset is our own work (public repo: never copy Decimation art).
1. Spec (gunmodel v2, v0.37.0): `tools/guns/<gun>.py` makes parts in model
   units (x forward, y down, z sideways, centre z -0.15) with
   `part(name, group, colour, a, b)` and bends them: `.inset(axis, side,
   other=(lo, hi))` (taper / wedge; negative values grow a corner),
   `.shift(axis, side, other=d)` (skew), `.mirror()` (pair about z
   -0.15), `octagon(...)` (3 part round bar); groups gunModel /
   ammoModel / slideModel; header (flamePos, ejectPos, rhPos / lhPos);
   animations with `gunmodel.anib` and `slide_names(parts)` (every
   slideModel part). Example: tools/guns/mac10.py.
2. Build: `python3 tools/guns/<gun>.py` writes into
   dev/src/main/resources/assets/deci/: `models/guns/<cat>/<gun>.bmodel`
   (each part declared at its rounded size, at least 1, with the rest of
   its shape in the 8 corner offsets, pivot at its lowest corner, a
   comment line with the readable name, textureWidth 512, box UV islands
   shelf packed in steps of 8), the texture (tone per part with
   gradation: shade, height gradient, face shifts, drift along long
   faces, texel noise; 2 px a unit), the 32x32 icon (the model's
   side render from study.py on a transparent background, dark outline),
   `animations/<gun>/*.anib`.
2b. Variant of a Decimation gun (UMP9): tools/guns/ump9.py builds it
   from Decimation.jar locally (their parts minus the replaced ones plus
   ours via gunmodel.part_block, texture extended below, animations
   copied); the outputs are git ignored and NewGuns registers the gun
   only when its model exists.
3. Preview without the game: tools/guns/study.py (`sheet REF ours:<gun>`,
   `render ours:<gun> [--split]`, `attach ours:<gun> reddot ...`, `gaps
   ours:<gun>`, `vocab`, `stats`); docs/gun_style_guide.md section 6. The headless Blockbench
   MCP (tools/bbmcp.py) still works but is no longer part of the loop.
4. Register in Java (fixes/NewGuns): `Deci.newMagazine` (bullet of an
   existing mag, an existing mag's icon), `Deci.newGun` (GunStats, category,
   magazines, slowdown, damage; registers as deciworldgen:<gun>),
   `Deci.addLootLike` (loot where a similar gun is), and on the client
   `Deci.useGunSounds` (points the gun's SoundEntry paths at an existing
   gun's files and rewrites Decimation's sounds.json; no copied audio).
   Names in assets/deciworldgen/lang/en_US.lang (item.<gun>.name).
5. Test: `python3 tools/devtest.py gun` (devtest/GunTest, test arena):
   first person still, reload key pressed (Decimation's key 19, isPressed
   on its client tick; items must be given on the SERVER, mags need NBT
   "ammo"), the gun's rounds after the reload, a bandit holding and firing
   it. Player firing cannot be automated: GunItem reads the fire button
   from the mouse itself (Mouse.isButtonDown(0)).
Facts learned: BModelBox corner array order (index 7 = (0,0,0) corner, 6
= (w,0,0), 4 = (w,h,0), 5 = (0,h,0), 3 = (0,0,d), 2 = (w,0,d), 0 =
(w,h,d), 1 = (0,h,d)), offsets are added; face UVs: top (y = 0) at
(u+d, v), bottom at (u+d+w, v), then x = 0, z = 0, x = w, z = d sides on
the row v+d. Decimation's guns use 2 px per unit textures (Uzi 1024x32
for a 512x16 layout), 32x32 icons.

Outside 3D generators (Claude Design "3D object", 2026-10-10, judged from
a screenshot only, no file tried): they export OBJ meshes (triangles, own
UV atlas). Decimation draws only `addShape` parts (8 corners each), so an
OBJ cannot be loaded as is. Usable path [not verified]: ask for boxes only
(every part one cuboid or 8 vertex block, no cylinders), one OBJ object per
part, named by group (gunModel / ammoModel / slideModel), colours per
material (MTL), no texture; a converter (to write: obj to the tools/guns
spec) turns each 8 vertex object into a box with corner offsets, and
gunmodel.py paints the box UV texture as usual. The generator's own atlas
and UVs are useless here (box UV is fixed by BModelBox).
Decision (user, 2026-10-10): keep the tools/guns pipeline (spec boxes +
Blockbench MCP previews); no OBJ converter, no outside generator.

## 7. Gun packages: guns from the converter, data driven (game side, milestone 1, 22 Oktober 2026)

Contract and plan: docs/superpowers/specs/2026-10-22-gun-converter-design.md ("The gun package"). The converter
(tools/gunconv, agent 1) writes one folder per gun; the game (fixes/gunpack, agent 2) registers it at start with no Java
line per gun. Status, checks and numbers: new_feature.md "3D gun converter, game side, milestone 1".

### Where packages live (found in this order; the first VALID package of a name wins, a later one is "shadowed")
1. The folders named in the system property `deciworldgen.gunpacks` (a package folder, or a folder of package folders;
   several separated by the path separator or commas): `./gradlew runClient -Pgunpacks=DIR` (runServer too),
   `python3 tools/devtest.py --live --gunpacks=DIR MODE` when it starts the live game. Agent 1's output:
   `--gunpacks=../Decimation-gunconv-tool/build/gunconv/out`.
2. Dev workspace only (deobfuscated run): the checkout's `build/gunpacks/` (git ignored), no flag needed.
   `python3 tools/guns/gunpack_standin.py` writes stand ins there: gcmac10 (our MAC-10 as a package), gcm4a4
   (Decimation's M4A4, local only, license "test") and gcbroken (must be refused).
3. Shipped: our jar's `assets/deciworldgen/gunpacks/NAME/` (dev/src/main/resources), the names listed one a line in
   `assets/deciworldgen/gunpacks/index.txt` (comments with #). None shipped yet.

A RELEASE ADDS ONE by copying the converter's folder to dev/src/main/resources/assets/deciworldgen/gunpacks/NAME/ and
adding NAME to index.txt (only CC0, CC BY with the credit kept, or our own model: its row in
docs/references/gunconv_sources.md, its credit in the release notes); tools/onejar.py carries it into the one jar like
any of our resources. Removing a shipped gun later leaves worlds holding it on Forge's missing items screen, like any
mod item.

### What the loader does (fixes/gunpack/GunPacks.register, preInit right after NewGuns, both sides)
- gun.json read and checked (GunPack.parse): every problem listed at once; an unknown field is a warning, never an
  error (either side may add optional fields). Refused: a bad name ([a-z][a-z0-9_]{1,31}, the folder's name too), a
  category outside rifle smg mg shotgun pistol revolver rocket crossbow, a magazine other than "like" or {name,
  capacity 1 .. 500, like}, stats out of range, flame not 3 numbers, a rail span from >= to, a mount outside rail side
  slide (or one without railTop), a license other than CC0, CC BY or "own" ("test" only in the dev workspace).
- Files checked: model, texture, icon, Fire and Reload1 must exist; Rack and SlideBack are optional (a warning).
- Checked before any item exists (so a refused package leaves nothing): like is a Decimation gun (deci:NAME: its sound
  files are read from guns/CAT/LIKE/), the names are free in deci and deciworldgen, magazine.like and magazine.icon are
  Decimation magazines.
- Client: one resource pack (GunPackResources, domain deci) serves only each package's own paths (models/guns/CAT/
  NAME.bmodel, textures/model/guns/CAT/NAME/*, textures/items/gun/CAT/NAME.png, animations/NAME/*) plus a generated
  deci:lang/en_US.lang with the names. It goes into Minecraft's default packs (survives every reload) and straight into
  the resource manager BEFORE the first gun is made: GunItem's constructor loads its .bmodel through the resource
  manager on the spot.
- The gun: Deci.newGunLike, a copy of the like gun's GunStats (recoil, fire modes and params, aim table, rate of fire,
  manufacturer, particles, forced tracer), damage, slowdown, capacity, crate weight (lootChance), integral suppressor,
  no fire sounds, reduced bass, launcher ammo, with the overrides of "stats": damage (as Decimation registers it, halved
  in game like every gun), rpm, pitch, yaw, recovery, slowdown, auto (AUTO + SINGLE or SINGLE). Not copied: an
  integrated scope (it is the like gun's model). Registers as deciworldgen:NAME.
- Magazine: "like" loads the like gun's magazines (60 round ones included); an own magazine is Deci.newMagazine with
  magazine.like's round and loot, magazine.icon's icon (default like), display "<capacity>rnd <display> Magazine" ("30rnd MAC-10 (gun package) Magazine") unless given.
- Loot: Decimation's crates and car wrecks wherever the like gun (and like magazine, 2 a stack) is (Deci.addLootLike);
  our lists in postInit, in memory, wherever the like is named (LootPools.addLike; the user's json is not rewritten).
- Client: the like gun's sounds (Deci.useGunSounds); its hand poses only when the model has no rhPos and no lhPos line;
  points.barrelShift (optional, game side) moves barrel attachments per gun as NewGuns does for the MAC-10.
- Attachment rows (GunPackRows, the same text as the shipped tables; GripRails, SightMounts, SightPlacement and
  GripPlacement read their table through GunPackRows.with): sight rows as tools/guns/sightfit.py writes them (dy =
  railTop + 4.45 down when over 0.06; dx 0 when the top rail holds x 0.8 .. 5.5, else the least shift that puts a long
  scope on the rail, or the scope's middle on a short rail's middle; points.sightShift wins), only with a mount; the
  mount row; grip rows by GripFit, tools/guns/gripfit.py in Java (230 of the shipped rows reproduced within 0.0015, unit
  GripFitTest): lights on every gun, a Foregrip and the RK-1 only on a rifle with points.gripRail, their seat on it.
- Nothing runs after startup; ScopeZoom learns a package gun's sight glass on its first aim like any gun's
  (SightMemory keeps it); IronSights folds defaultScopeModel* parts under a sight like any gun's.

### Field meanings the game side relies on (points)
flame: the muzzle as the model's flamePos (Decimation uses the model's; a mismatch over 0.05 is a warning). railTop: the
top of what a sight sits on (rail teeth, a side mount's base); Decimation places sights for -4.45. rail: the top rail's
x span. gripRail: the bottom rail's x span under the handguard (null: no foregrip). mount: rail, side (AK / SVD style),
slide (pistol optic cut: the red dot only) or null (no sight). Optional of the game side: sightShift (number),
barrelShift ({attachment: [dx, dy, dz]}), magazine.icon, magazine.display.

### Tests
Unit (fixes/gunpack): GunPackTest (contract), GunPackRowsTest (rows, tables with package lines), GripFitTest (the
shipped grip table reproduced), PackModelTest (.bmodel geometry as study.py), GunPackSourceTest (jar and folder
sources; test resources assets/deciworldgen/gunpacks_test, never shipped). Live: `devtest.py --live gunpack` (the
loader, every package against its like gun, rows as placed, a stand in gc<gun> against <gun>, first person shots),
`gunpack perf=true` (draw time against the like gun or the copied gun, glFinish around); with the gun's name: `gun
gun=NAME [mag=deci:LIKEMAG]`, `playerfire gun=NAME`, `gunview guns=LIKE,NAME [attach=...]`, `gunperf guns=LIKE,NAME`,
`gripsweep pairs=NAME:foregrip,...`. Traps: the live key=value settings stay set (clear gun= and perf=); the packages
are read at start, so a changed package needs the live game restarted.

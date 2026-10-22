# Converted guns as real meshes, 1:1 (converter milestone 4)

User, 23 Oktober 2026: the Decimation style converter "never hit same output" in three tests; "decimation style" now
means the texture only, not the model; "use convert 1:1 instead generating decimation style guns". Chosen: texture
"pixelated like decimation", a cap of 30000 triangles, built in this session (no agent).

## Why boxes cannot do it (checked)
- Decimation's .bmodel holds only shape boxes (addShape, 8 corner offsets, Minecraft's box texture layout).
- Decimation draws every face flat: BModelQuad.draw computes one normal from the face's corners (deobf BModelQuad).
- One part per box: a 1:1 mesh of 30000 triangles as boxes would cost about 0.34 ms + 0.0009 ms a part, about 27 ms a
  gun a frame; Exact mode at 1550 faces already drew 3.78 x the akm (the page refused it) and looked like dice.

## Design
1. Package: the converter writes `mesh.dmesh` beside gun.json (gun.json "mesh": "mesh.dmesh"):
   magic "DMESH1", groups (name gunModel / ammoModel / slideModel, pivot), per triangle three vertices of position
   (model units, relative to the group's pivot), smooth normal and texture coordinate (0..1 of the gun's texture).
   Over 30000 triangles the mesh is simplified to 30000 (the existing quadric queue); normals are the source's or,
   when missing, averaged across faces within 40 degrees (sharp edges stay sharp).
2. Skeleton .bmodel: the header (sPos, flamePos, ejectPos, hands, Scale, texture size) as today, and per group one
   placeholder part: a 1 x 1 x 1 shape box at (id, group, 7777) times its own size, id = a 16 bit hash of the gun's name.
   Its rotation point is the group's pivot, its name the group's (gunModel0, ammoModel0, slideModel0), so Decimation's
   animations, hands, aim, muzzle and attachment points work as for any gun.
3. Game (fixes/gunpack + fixes/ModelBatch, no jar patch): GunPacks reads each package's mesh. When Decimation compiles a
   part, ModelBatch captures its quads (PatchModelBatch); a capture that is exactly one marker box (its six faces, a cube
   whose corner sits at id, group, 7777 times its size) compiles that mesh group into the part's list instead
   (triangles, per vertex normals, scaled by the marker's size, so any scale works). The part then draws one list a
   frame with its own animated transform. Nothing per frame but that list.
4. Texture, pixelated like Decimation's guns: a source texture is scaled down so one texel covers about a quarter of a
   model unit, its light and dark mapped into Decimation's tones (steel greys 24 to 64, wood 41,25,22, polymer, magazine
   brown), low saturation, 15 to 31 shades, faint noise; no source texture: each part's material or area tone with
   Decimation's gradation (lighter on top) as cells of a small texture. Drawn with nearest filtering (Minecraft's default).
5. Icon: a 32 x 32 render of the mesh (side view, tones, 1 px dark outline), as Decimation's icons.
6. The page and the CLI: one Convert (1:1); the Decimation style generator and the box Exact mode leave the page (their
   code stays in git history); the page shows triangles, parts and the measured cost line.

## Performance
Budget: draw time of a converted gun within 1.5 x the akm's (0.46 ms median) in first person and as an NPC's gun;
graphics memory per gun under 10 MB (vmmap, after the driver's first step); nothing allocated per frame. Measured with
gunpack perf=true, npcdraw, glstack, and the production client with OptiFine.

## Tests
Python: the dmesh round trip, the cap, normals by angle, the skeleton's markers, the texture's texel size and palette.
Java: the dmesh reader, marker detection from captured quads (pure). Dev test `meshgun`: a converted gun's parts drawn
from mesh lists (triangle count), the magazine group moving in the reload pose, the bolt in the fire pose, first person
shot beside the page's render, costs; gunpack, gunview, glstack; production with OptiFine.

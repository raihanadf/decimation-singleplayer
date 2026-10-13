---
name: decimation-block
description: Make a block of our own (deciworldgen:<name>) for Decimation worldgen when no Decimation prop fits or a prop is too costly: plain full blocks (HESCO) and thin shaped blocks with bounds per metadata (the toll barrier arm). Covers when to make one, the class, bounds and collision, scripted textures, registration, lang, turning on a canvas, and the checks. Use when the user asks for a new block, when a structure needs something Decimation does not have, or when a prop placed by the hundreds costs frame rate.
---

# Our own blocks (deciworldgen:<name>)

Two made so far: `deciworldgen:hesco` (military/BlockHesco, v0.42.1: a plain full block replacing
Decimation's BlockMilitaryBarrier, a tile entity per cell that cost frame rate in bases) and
`deciworldgen:boom_barrier` (military/BlockBoomArm, v0.61.0: the toll gate arm, a thin striped bar;
Decimation has no barrier arm). Facts on props: docs/prop_placement.md, docs/prop_catalogue.md.

## 0. Make one, or use what exists?
1. Look for a Decimation prop first (docs/prop_catalogue.md, docs/references/prop_facing.tsv,
   `python3 tools/props/propgeom.py show BlockX`). Vanilla blocks next (slabs, panes, bars, wool, stained
   clay; dirt / coarse dirt read orange in Decimation's pack, never use them for ground).
2. Make our own when: nothing looks right (no barrier arm), or the thing is placed by the hundreds and the
   prop is a tile entity (HESCO: every Decimation BlockProp is a tile entity with its own model render).
3. Prefer a plain block (standard renderer, no tile entity). A shape that needs a model, animation or
   text is a bigger job: ask the user first.
4. Never copy Decimation's textures or models (public repo): draw our own by script.

## 1. The class (dev/src/main/java/net/decimation/worldgen/military/, next to its users)
- `public final class BlockX extends Block`, `public static BlockX instance;`, constructor with material,
  hardness / resistance, step sound, `setBlockName("deciworldgen.x")`,
  `setBlockTextureName("deciworldgen:x")`, a creative tab.
- Icons: `registerBlockIcons` / `getIcon(side, meta)` with @SideOnly(Side.CLIENT); a separate icon per
  face group (HESCO top vs sides) or per metadata (arm lying vs raised).
- Shaped block (not a full cube): metadata picks the shape (arm: 0 along x, 1 along z, 2 upright).
  `setBlockBoundsBasedOnState` sets bounds from metadata (the standard renderer draws those bounds and
  cuts the texture to them), `getCollisionBoundingBoxFromPool` sets them before super (so the collision
  matches: a waist high arm at 0.62..0.82 stops walking), `setBlockBoundsForItemRender`,
  `isOpaqueCube` and `renderAsNormalBlock` false, `setLightOpacity(0)`.
- No tile entity unless it must store data.

## 2. Texture (tools/props/<name>_textures.py, run from the project root)
- PIL, 16 x 16, our own colours with faint noise (+/- 7), written to
  dev/src/main/resources/assets/deciworldgen/textures/blocks/. Commit the script and the PNGs.
- Think about which part of the texture each face shows: the standard renderer cuts the texture by the
  bounds (a bar 0.2 thick shows a 3 px strip). Stripes across u for faces along the bar, a second texture
  with stripes across v for the upright version (boom_textures.py).

## 3. Register and name
- DecimationWorldGen.preInit next to the others:
  `BlockX.instance = new BlockX(); GameRegistry.registerBlock(BlockX.instance, "x");`
- Lang line in dev/src/main/resources/assets/deciworldgen/lang/en_US.lang:
  `tile.deciworldgen.x.name=...`.
- Code that places it uses the instance (with a fallback when null) or
  `Block.getBlockFromName("deciworldgen:x")`. Lookups by name of Decimation things go through Deci.

## 4. Placing it from generators
- On a Canvas (military bases, camps, refuges, city gates) set it as Canvas.PLAIN: Rotation does not know
  our blocks. If its metadata is directional, turn it in the plan's blockAt after the canvas turn
  (CityGatePlan: an arm lying along x lies along z when turns is odd: meta ^= 1).
- Canvas.validateProps checks props against it like any full block: a thin block can make props near it
  look clashing; keep props a cell away or accept a logged reason.

## 5. Check
- `./gradlew compileJava`, then a dev test that builds it in the sky and photographs it (the gates test
  for the arm: `python3 tools/devtest.py --live gates`), read the sheet: texture the right way round,
  bounds and shading right, upright version right.
- Walk into it in a test or by reasoning from the collision box: does it stop the player / mobs where
  it should (an arm closes its lane)?
- Server run (`python3 tools/servertest.py 1 type=decimation ...`): it registers on a dedicated server
  (no client class touched outside @SideOnly methods).
- Docs: the feature's doc names the block and its texture script; CLAUDE.md version line; shots index.

## Lessons
1. HESCO (v0.42.1): a prop used as building material cost frame rate (a tile entity and a model per
   cell, thousands per base): replaced by a plain block; tile entities per base 2007 to 381.
2. Barrier arm (v0.61.0): Decimation had no such prop; a thin block with bounds per metadata and two
   striped textures did it without any model; Rotation does not turn our metadata, the plan does.

## Casebook (never delete a case)
### Case 1: HESCO as a plain block (v0.42.1, 11 Oktober 2026)
- User: the military barrier "really hurt fps". Fix: BlockHesco, tools/props/hesco_textures.py.
### Case 2: toll gate arm (v0.61.0, 14 Oktober 2026)
- User: the wasteland gates "use toll gate". No Decimation arm prop: BlockBoomArm, boom_textures.py,
  checked in the gates dev test shots (docs/shots/wasteland_gate_v0.61/). User: "make it as specialist
  skills" (this skill).

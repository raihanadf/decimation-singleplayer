---
name: decimation-code-blocks
description: Use when changing or debugging how Decimation's blocks behave (not how props look or where they go): locked doors, keycard screens and keycards, lockpicking, elevators, multiblock props breaking or not rendering, supply crates, fortifications, block registration, or when generated structures need working locked rooms or elevators.
---

# Decimation blocks and interactive props code

Facts and the checked keycard: docs/decimation_code/blocks.md. Drawing and placing props: skill decimation-props.

## Quick reference

| Task | Where |
|---|---|
| locked door logic | `LockedDoorBlock` (deci/U/g), `TileEntityLockedDoor` (deci/ac/f) |
| keycard screen | `KeycardReaderBlock` (deci/U/d): reach -3 .. +2, level must match |
| multiblocks | `MultiblockHelper` (deci/W/e), master part renders |
| check keycards in a world | `python3 tools/props/keycardcheck.py DUMP.tsv` (dump: tools/props/worlddump.py) |
| test in game | `python3 tools/devtest.py --live keycard` |

## Rules

1. Every keycard screen needs a locked door at offsets -3 .. +2 from it; run keycardcheck on the dumps of any
   generator that places them (bases, airport, wasteland buildings).
2. A keycard toggles: a second use shuts the door again.
3. Place doors as both halves (bottom with its facing, top metadata 8); the tile entity comes with the block.
4. Check generators against a FRESH world: a world generated across versions keeps old layouts' leftovers.

## Common mistakes

- Putting the screen west of the door with the door 3 blocks east of it: out of reach.
- Expecting block protection in singleplayer: it is server proxy only.
- Placing a multiblock prop part by part without setSelfMaster: it does not render.

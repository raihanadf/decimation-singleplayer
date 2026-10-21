# Decimation code: blocks and props (registration, prop blocks, multiblocks, locked doors, keycards, elevators)

Part of the feature study of Decimation's own code (16 Oktober 2026, index docs/decimation_code/README.md). How props
are DRAWN and where to place them: docs/prop_placement.md, skill decimation-props; what each looks like:
docs/prop_catalogue.md.

## 1. What it is for

Decimation's world furniture: hundreds of props (crates, tables, cabinets, wrecks, barriers, lights, signs), building
blocks, decals, fortifications, and the interactive ones: locked doors with keycard screens and lockpicks, elevators,
alarm bells, generators, water fountains.

## 2. Code map

| Class (obf) | Job |
|---|---|
| BlockRegistry (deci/aD/c) | block registration: calls PropBlockRegistry (deci/aD/g, hundreds of props), OfficeBlockRegistry (deci/aD/d), SoundBlockRegistry (deci/aD/f), DecalBlockRegistry (deci/aD/e); `afA` the supply drop crate |
| BlockProp, TileEntityProp, the TESRs in deci/I | a prop is a tile entity drawn by its own renderer and model; metadata 2 to 5 is its facing (docs/prop_placement.md) |
| MultiblockHelper (deci/W/e) | multiblock props: `isSpaceFree`, `placeWhole` (every part gets the origin as master), `breakWhole`; only the master renders (our code calls setSelfMaster, CLAUDE.md v0.12.3) |
| LockedDoorBlock (deci/U/g) | two block door (vanilla door metadata: bottom bit 4 = open, top = 8); `isUnlocked` asks its tile entity; once unlocked a hand click toggles it like a normal door; `toggleDoor` flips bit 4 |
| TileEntityLockedDoor (deci/ac/f), TileEntityLockable (deci/ac/d) | lock state; `unlock` (f) -> `onUnlocked` -> `toggleDoor`; lockpicking pins (`getPin`, `shufflePins`, PacketLockpick...) |
| KeycardReaderBlock (deci/U/d) | keycard screen (BlockKeycardScreen, ...Military, ...Laboratory): with a keycard of ITS level (ItemKeycard deci/av/k: 0 security, 1 military, 2 laboratory) it unlocks the FIRST TileEntityLockedDoor at offsets -3 .. +2 on each axis (loop `-3 <= i < 3`), so a door 3 blocks to the east, up or south is never reached; each success toggles the door and wears the keycard by 1 |
| ElevatorBlock (deci/U/b), ElevatorButtonBlock (deci/U/c), TileEntityElevator (deci/ac/c) | elevators: after a countdown teleport living entities in a 3 x 3 area to the next open elevator in the same column |
| fortifications (deci/T): concertina wire, metal wall, sandbags, StorageCrateBlock | the blocks allowed in clan turfs |
| SupplyDropBlock (deci/V/a) | the supply crate (loot.md) |
| DecalRenderer (deci/H/a), decals (deci/S) | wall decals; some download pictures from the web (never use decal.imagegeneric) |

## 3. A keycard, as it runs (dev test `keycard`, call logger, 16 Oktober 2026)

Door_Emergency_1_Locked with a BlockKeycardScreenMilitary beside it, 1 up (as our generators place them); the
client right clicks the screen: server `KeycardReaderBlock.onBlockActivated` -> `TileEntityLockable.unlock` ->
`TileEntityLockedDoor.onUnlocked` -> `LockedDoorBlock.toggleDoor` (metadata 1 -> 5). Checked: a security key does
nothing, the military key opens, the same key again shuts the door, a door 3 east of its screen stays shut.

World check (tools/props/keycardcheck.py on a dump): fresh seed 1 around the airport (1022 chunks): 32 keycard
screens, every one with a locked door in reach; the military base dumps: 1 of 1 each. An older world generated
across several versions had 3 screens with no door at all (left from earlier layouts), not from the current code.

## 4. Singleplayer state and known faults

- Keycards, locked doors and elevators work in singleplayer (keycard checked live).
- The keycard reach is lopsided (-3 .. +2): place the screen so the door is west, below or north of it, or within 2
  blocks east, up or south.
- Block break and place protection (config, turfs) are server proxy only: in singleplayer every block can be broken
  and placed (our fixes/WreckHardness makes wreck props hard to break, fixes/PropHardness every other block of hardness 0:
  street barriers, tall barriers, metal shelves, elevator and its button; ropes still break with one click; dev test
  `hardness` dumps every block's one click share, bug.md).

## 5. How our mod uses it

| Need | Use |
|---|---|
| place props right | docs/prop_placement.md, `Canvas.validateProps`, tools/props/propgeom.py, propclash.py |
| locked buildings | city/BuildingDress.lockDoors, military/MilitaryBase and military/Airport: Door_Emergency_1_Locked plus BlockKeycardScreenMilitary |
| multiblocks | worldgen/Slices and the small placer call setSelfMaster; fixes/MultiblockRepairHandler on chunk load |
| check a world | tools/props/worlddump.py, then propclash.py, walkcheck.py, keycardcheck.py |
| our own blocks | deciworldgen:hesco, deciworldgen:boom_barrier (skill decimation-block) |

## 6. Checks

- `python3 tools/devtest.py --live keycard` (4 checks), `props` (prop fps), `facing` (prop facing table).
- `python3 tools/props/keycardcheck.py DUMP.tsv` on a world or base dump: every screen reaches a locked door.

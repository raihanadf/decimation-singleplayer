# Loot rules, lootable props, loot kinds, Tarkov search (feature 12; docs/ideas.md 36, 49, 50, 29)

User's words and choices, 22 Oktober 2026 (docs/ideas.md has them in full):
- 36: "loot by default is empty forever after player loot it, except multiplayer they can be disable or enable for minutes
  refill loots (by default is enable for server)"; then "15 minute use old decimation setting, for singleplayer it has
  option on cfg file world too so they can use refill method or not".
- 49: "props later can bee looted like truck, wooden crate, watter pallet, military radio and small military radio (they
  have 2 version non loot and loot)"; then "lootable is just copy of block the props i say then make lootable version ...
  so later when we make world city props, we can placed 2 version props non loot and lootable"; the loot hint shows.
- 50: "shelf can be civilian loot or military loot or police loot, weapon cabinet now have 2 version its civilian loot
  with name cabinet (without weapon word) and police loot and military loot)"; the building decides the kind.
- 29: "loot system where it need to be analyzed one by one before taking it. so the item will show loading icon
  animation then if it done it can be take it"; by value, 1 to 3 s an item.

## Facts (docs/decimation_code/loot.md)
Decimation's LootInteractHandler (deci/aK/k) rolls a block's list into a fresh 18 slot inventory on a right click and
marks the spot in LootCooldownRegistry (deci/aB/e): 15 minutes, in memory only (a reload refills everything). The table
(LootTableRegistry deci/aD/l) binds 33 blocks to 12 lists. The loot screen's slots are ours since the shift click fix
(Deci.GearContainer, fixes/InventoryShift). Props: BlockTruckWreckage1..6, BlockWaterPallet (+Tarp), BlockMilitaryRadio,
BlockMilitaryRadioSmall (and their Off versions) are not lootable; BlockWoodCrate is; BlockMetalShelf (a multiblock) uses
the general list, BlockWeaponCabinet the guns and magazines list.

## Design
A. Refill (fixes/LootRefill; BUILT 22 Oktober 2026, new_feature.md "Loot rules A"): a saved record per world (WorldSavedData, position -> world tick of the last loot), read
   before Decimation's handler (PlayerInteractEvent HIGHEST): a looted spot answers Decimation's own "This <block> is
   empty..." and nothing opens, unless refill is on and its minutes have passed. Written after Decimation looted it
   (LOWEST, also when cancelled, as LootMagazines does). The switch: <world>/deciworldgen_loot.cfg, written on first
   load: refill false in singleplayer, true with 15 minutes on a dedicated server; either can be changed per world.
   Supply drops (they vanish) and our loot bodies keep their own rules. The loot hint of a looted spot: [not in scope:
   Decimation asks the client's never marked registry; noted].
B. Lootable copies and loot kinds (fixes/LootBlocks): blocks of ours made from Decimation's own prop classes (same
   look, same multiblock behaviour), each bound to a Decimation list in its table (a new table entry), the loot hint
   then shows by itself:
   - lootable copies: the six pickup trucks (car wreck list), the water pallets (the food and drink list), the two
     military radios (military list); a plain copy of the wooden crate (no list);
   - shelves: the original stays civilian (general list); a police copy (police list), a military copy (military list);
   - the weapon cabinet: the original keeps its guns and magazines list as the military one (existing worlds keep their
     gun loot; my call); copies: "Cabinet" with the civilian list and a police one with the police list.
   Our world generation picks the version by building: police buildings police, military buildings and bases military,
   the rest civilian (BuildingDress, the military base and wasteland code). Existing worlds keep what they have.
C. Tarkov search (fixes/LootSearch): a new loot inventory starts unsearched; the server reveals its filled slots in
   slot order, each after its time (guns, keycards, gold bottlecaps 3 s; attachments, armor, vests, backpacks, masks,
   repair parts and filters 2 s; the rest 1 s), refuses taking an unsearched slot (our GearContainer), and sends the
   progress when it changes (our channel); the screen draws a dark "?" over unsearched slots and a spinner on the one
   being searched (GuiScreenEvent.DrawScreenEvent.Post). Closing the screen keeps the progress of that inventory.
   Crates, cars, our loot bodies alike.

## Performance
A: one map lookup a loot click. B: blocks only. C: once a tick per player with an open search (a time compare), a
packet per item revealed, the overlay per frame only on that screen (rectangles, no allocation).

## Tests
Unit: the refill rule (looted, minutes, switch), the search times and order, the config defaults. Dev tests: `lootrules`
(a crate looted stays empty after a save and reload in singleplayer; refill on a 1 minute setting), `lootblocks` (each
copy places, renders like its original (shots), opens its list or none, the hint), `lootsearch` (items unknown at first,
revealed in order and time, an unsearched slot cannot be taken, a body too); the production client with OptiFine.

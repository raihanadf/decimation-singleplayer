# Gunsmith Screen Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Pressing I with a gun in hand opens a Tarkov style Gunsmith screen: the held gun's real 3D model, six slots
joined by lines to their parts, parts fitted with a click from the whole inventory, measured fast before it is done.

**Architecture:** Decimation's own I key and ATTACH action stay; our handler wraps Decimation's IGuiHandler (FML
NetworkRegistry) so its attachments GUI id opens our container and screen instead of AttachmentScreen. The container
(server authoritative) keeps the gun in its hotbar slot, edits the gun's NBT the way Decimation's AttachmentInventory
does, and takes list clicks as Container.enchantItem ids (no new network channel). The screen draws the gun with
Decimation's GunItemRenderer (ENTITY type), measures its silhouette from the depth buffer once per change, and places
the boxes, lines and list from plain, unit tested layout code.

**Tech Stack:** Java 8, Forge 1.7.10 (MCP names in dev), JUnit 5 for plain classes, dev test modes (tools/devtest.py
--live), Decimation through fixes/Deci only.

**Spec:** docs/superpowers/specs/2026-10-21-gunsmith-screen-design.md

## Global Constraints

- Opening: Decimation's key "key.decimation.gui.attach" (default I) and its ATTACH action. No gun in hand: nothing
  opens, chat line "Hold a gun to modify it".
- Slots and order: Sight, Barrel, Grip, Stock, Paint, Tracer. Texts exactly: list title "<plural> you carry" (Sights,
  Barrels, Grips, Stocks, Paints, Tracers), "None in your inventory", "Take off", "Inventory full", "Built in",
  "Esc  Close". No dash characters in any player text.
- Lists come from the WHOLE inventory (hotbar and the 27 main slots); dragging from the hotbar row onto a slot works.
- What fits is decided by Decimation's FilteredSlot.isItemValid (deci/f/k), never by our own copy of the rules.
- The gun never leaves its hotbar slot; closing drops nothing; Esc, E or I closes the screen.
- Paint and tracer are used up when applied and cannot be taken off (spec 2.7 "as today"); Sight, Barrel, Grip and
  Stock can be taken off.
- A slot the gun cannot use is dimmed and says "Built in": Sight on a gun with an integrated scope, Barrel on an
  integrally suppressed gun, Tracer on a gun with forced tracers.
- Look: boxes black translucent with a grey border, fitted border lighter grey, the open slot and the list in red
  #E24848, red dot at each line end, white lines at about half opacity, Minecraft's font, the world dimmed behind.
- Every GUI size from 854 x 480 (427 x 240 units) to 1920 x 1080: nothing leaves the screen or covers another box.
- Performance (spec 5): frame time open at most 1 ms above closed (median of 200 frames), no texture load and no
  display list compile after the first frame open, nothing measured or listed per frame (only on open or change), GL
  stacks and errors clean, no slot updates while idle.
- Obfuscated names only in fixes/Deci; no jar patch; no client type in the signature of any class the dedicated server
  loads (CLAUDE.md: Deci.homeMenu trap).
- Before ANY Gradle run in main: `pgrep -fl "zulu-8.jdk.*GradleStart"` plus `lsof -a -p PID -d cwd`; never Gradle in a
  checkout whose live game runs (CLAUDE.md "TWO LINES SIDE BY SIDE").

## Review Focus

1. A gun no attachment can fit (RPG-7, crossbow): the screen opens, every list says "None in your inventory", no crash.
   Test: Task 5 opens it with rpg7 in hand.
2. Very long and very short guns (minigun, deagle) at 854 x 480: the gun and all boxes fit on screen. Test: Task 5 shots
   and bounds checks.
3. The held stack replaced while the screen is open (a reload or another mod swapping the object): the server closes
   the screen, the gun keeps its parts, nothing duplicates. Test: Task 4 replaces the held stack server side.
4. The player dies with the screen open: the death drops are exactly the stacks held before, no extra part. Test: Task 4.
5. Off the integrated server (production server with the production client): open, fit and take off behave the same.
   Test: Task 6.

---

### Task 1: Slots and actions (plain)

**Files:**
- Create: `dev/src/main/java/net/decimation/fixes/gunsmith/GunSlot.java`
- Create: `dev/src/main/java/net/decimation/fixes/gunsmith/GunsmithAction.java`
- Test: `dev/src/test/java/net/decimation/fixes/gunsmith/GunsmithActionTest.java`

**Interfaces:**
- Produces: `enum GunSlot { SIGHT, BARREL, GRIP, STOCK, PAINT, TRACER }` with `final String label, plural, nbtKey;
  final int deciIndex; boolean usedUp(); String listTitle()`. Values: label "Sight" plural "Sights" deciIndex 2 nbtKey
  "sightAttach"; "Barrel"/"Barrels"/1/"barrelAttach"; "Grip"/"Grips"/4/"gripAttach"; "Stock"/"Stocks"/3/"stockAttach";
  "Paint"/"Paints"/5/"skin"; "Tracer"/"Tracers"/6/"tracerColorID" (deciIndex = WeaponModifyContainer's slot index).
  usedUp() true for PAINT and TRACER. listTitle() = plural + " you carry".
- Produces: `final class GunsmithAction { static int fit(GunSlot s, int invIndex); static int takeOff(GunSlot s);
  static GunsmithAction decode(int id); final boolean takeOff; final GunSlot slot; final int invIndex; }`.
  fit = 1000 + s.ordinal() * 100 + invIndex (invIndex 0..35); takeOff = 2000 + s.ordinal(); decode returns null for
  anything else, for invIndex over 35, and for takeOff of a usedUp slot.

- [ ] **Step 1: Write the failing tests**

```java
@Test void slotsMatchDecimation() {
    assertEquals(2, GunSlot.SIGHT.deciIndex); assertEquals(1, GunSlot.BARREL.deciIndex);
    assertEquals(4, GunSlot.GRIP.deciIndex); assertEquals(3, GunSlot.STOCK.deciIndex);
    assertEquals(5, GunSlot.PAINT.deciIndex); assertEquals(6, GunSlot.TRACER.deciIndex);
    assertEquals("Grips you carry", GunSlot.GRIP.listTitle());
    assertTrue(GunSlot.PAINT.usedUp()); assertTrue(GunSlot.TRACER.usedUp()); assertFalse(GunSlot.SIGHT.usedUp());
}
@Test void actionsRoundTrip() {
    GunsmithAction a = GunsmithAction.decode(GunsmithAction.fit(GunSlot.GRIP, 35));
    assertFalse(a.takeOff); assertEquals(GunSlot.GRIP, a.slot); assertEquals(35, a.invIndex);
    assertTrue(GunsmithAction.decode(GunsmithAction.takeOff(GunSlot.STOCK)).takeOff);
}
@Test void badIdsDecodeToNull() {
    for (int id : new int[] {-1, 0, 999, 1036, 1600, 1999, 2004, 2005, 2006, 3000})
        assertNull(GunsmithAction.decode(id), "id " + id);
}
@Test void textsHaveNoDashes() {
    for (GunSlot s : GunSlot.values()) assertFalse(s.listTitle().matches(".*[-\\u2013\\u2014].*"));
}
```

- [ ] **Step 2: Run them, see them fail**

Run (after the pgrep check): `cd dev && ./gradlew test --tests 'net.decimation.fixes.gunsmith.*'`
Expected: compilation FAILS (GunSlot, GunsmithAction missing).

- [ ] **Step 3: Write GunSlot and GunsmithAction** as in Interfaces; plain Java, no Minecraft imports.

- [ ] **Step 4: Run them, see them pass**

Run: `cd dev && ./gradlew test --tests 'net.decimation.fixes.gunsmith.*'`
Expected: BUILD SUCCESSFUL, 4 tests pass.

- [ ] **Step 5: Commit** `git add` the three files; message: `Gunsmith slots and list actions (plain, unit tested)`

### Task 2: Silhouette (plain)

**Files:**
- Create: `dev/src/main/java/net/decimation/fixes/gunsmith/Silhouette.java`
- Test: `dev/src/test/java/net/decimation/fixes/gunsmith/SilhouetteTest.java`

**Interfaces:**
- Produces: `static Silhouette measure(boolean[] mask, int w, int h)` (row major, y down, the gun drawn muzzle right);
  null when the mask is empty. Fields `final int minX, minY, maxX, maxY; final int[][] points` (index GunSlot.ordinal(),
  each {x, y}).

Points (W = maxX - minX + 1; every point is finally snapped to the nearest set pixel, searching outward up to W/4):
- BARREL: x = maxX, y = mean row of set pixels in columns [maxX - max(1, W/50), maxX].
- STOCK: x = minX + W/20, y = mean row of set pixels in columns [minX, minX + W/10].
- SIGHT: the topmost set pixel in columns [minX + 30% W, minX + 60% W].
- GRIP: x = minX + 70% W, y = the lowest set pixel in columns [minX + 60% W, minX + 80% W].
- TRACER: the lowest set pixel in columns [minX + 35% W, minX + 60% W], moved 2 px up.
- PAINT: the centroid of all set pixels.

- [ ] **Step 1: Write the failing tests** on a synthetic 200 x 80 rifle mask built in the test: stock rect x 0..39
  y 30..49; body x 40..119 y 25..44; barrel x 120..199 y 32..35; magazine x 80..95 y 45..69; scope x 60..99 y 12..24;
  fore end x 120..159 y 30..39.

```java
@Test void emptyMaskIsNull() { assertNull(Silhouette.measure(new boolean[100], 10, 10)); }
@Test void boundsAndMuzzle() {
    Silhouette s = Silhouette.measure(RIFLE, 200, 80);
    assertEquals(0, s.minX); assertEquals(199, s.maxX); assertEquals(12, s.minY); assertEquals(69, s.maxY);
    assertEquals(199, s.points[GunSlot.BARREL.ordinal()][0]);
    assertTrue(between(s.points[GunSlot.BARREL.ordinal()][1], 32, 35));
}
@Test void sightOnTheScopeMagazineOnTheMag() {
    Silhouette s = Silhouette.measure(RIFLE, 200, 80);
    assertEquals(12, s.points[GunSlot.SIGHT.ordinal()][1]);
    int[] t = s.points[GunSlot.TRACER.ordinal()];
    assertTrue(between(t[0], 80, 95) && between(t[1], 60, 69));
}
@Test void everyPointOnTheGun() {
    Silhouette s = Silhouette.measure(RIFLE, 200, 80);
    for (int[] p : s.points) assertTrue(RIFLE[p[1] * 200 + p[0]], Arrays.toString(p));
}
@Test void pistolShapeStillOnTheGun() {   // slide x 0..99 y 10..29, grip x 10..29 y 30..69
    Silhouette s = Silhouette.measure(PISTOL, 100, 70);
    for (int[] p : s.points) assertTrue(PISTOL[p[1] * 100 + p[0]], Arrays.toString(p));
}
```

- [ ] **Step 2: Run, see them fail** (`./gradlew test --tests 'net.decimation.fixes.gunsmith.*'`; Silhouette missing).
- [ ] **Step 3: Write Silhouette** with the rules above; one pass over the mask for bounds, column bands for the rest.
- [ ] **Step 4: Run, see all pass.**
- [ ] **Step 5: Commit** `Gunsmith silhouette: line ends from the drawn gun (plain, unit tested)`

### Task 3: Layout (plain)

**Files:**
- Create: `dev/src/main/java/net/decimation/fixes/gunsmith/GunsmithLayout.java`
- Test: `dev/src/test/java/net/decimation/fixes/gunsmith/GunsmithLayoutTest.java`

**Interfaces:**
- Produces: `static final int BOX = 24, TOP = 30, BOTTOM = 52, LIST_W = 120, ROW = 18, MAX_ROWS = 6;`
  `static int[] gunArea(int w, int h)` = {x, y, aw, ah}: x = max(40, w / 8), y = TOP + 12, aw = w - 2x,
  ah = h - y - BOTTOM - 20.
  `static int[][] boxes(int w, int h, int[][] points)` = top left {x, y} per GunSlot.ordinal(); preferred offsets from
  the point: SIGHT (-12, -48), BARREL (+18, -36), GRIP (+10, +22), STOCK (-30, -44), PAINT (-40, +26), TRACER
  (-12, +34); clamp into [4, w - 4 - BOX] x [TOP, h - BOTTOM - BOX - 10]; then up to 10 passes pushing a box that
  overlaps an earlier one (box plus a 10 unit label band below, 4 unit gap) along x away from it, clamping again.
  `static int[] list(int w, int h, int[] box, int rows)` = {x, y, pw, ph}: pw = LIST_W, ph = 14 + ROW * min(rows,
  MAX_ROWS); right of the box when it fits, else left; top aligned with the box; clamped inside the screen.

- [ ] **Step 1: Write the failing tests**

```java
static final int[][] SIZES = {{427, 240}, {480, 270}, {640, 360}, {960, 540}, {320, 240}};
@Test void boxesStayOnScreenAndApart() {
    for (int[] s : SIZES) {
        int[][] b = GunsmithLayout.boxes(s[0], s[1], rifleAt(GunsmithLayout.gunArea(s[0], s[1])));
        for (int[] x : b) assertTrue(x[0] >= 4 && x[0] + GunsmithLayout.BOX <= s[0] - 4 && x[1] >= GunsmithLayout.TOP
                                     && x[1] + GunsmithLayout.BOX + 10 <= s[1] - GunsmithLayout.BOTTOM, Arrays.toString(s));
        for (int i = 0; i < 6; i++) for (int j = i + 1; j < 6; j++) assertFalse(overlap(b[i], b[j]), s[0] + " " + i + "/" + j);
    }
}
@Test void boxesStayNearTheirParts() {
    for (int[] s : SIZES) {
        int[][] pts = rifleAt(GunsmithLayout.gunArea(s[0], s[1]));
        int[][] b = GunsmithLayout.boxes(s[0], s[1], pts);
        for (int i = 0; i < 6; i++)
            assertTrue(Math.hypot(b[i][0] + 12 - pts[i][0], b[i][1] + 12 - pts[i][1]) <= 120, s[0] + " slot " + i);
    }
}
@Test void listOnScreenAndBesideItsBox() {
    for (int[] s : SIZES) for (int[] box : corners(s)) {
        int[] l = GunsmithLayout.list(s[0], s[1], box, 7);
        assertTrue(l[0] >= 0 && l[1] >= 0 && l[0] + l[2] <= s[0] && l[1] + l[3] <= s[1]);
        assertFalse(overlap(l, new int[] {box[0], box[1], GunsmithLayout.BOX, GunsmithLayout.BOX}));
    }
}
```
`rifleAt(area)` maps the Task 2 rifle points (scaled to 90 % of the area width) into the area.

- [ ] **Step 2: Run, see them fail.**
- [ ] **Step 3: Write GunsmithLayout** with the values above.
- [ ] **Step 4: Run, see all pass** (tune only the preferred offsets if a size fails, never the assertions).
- [ ] **Step 5: Commit** `Gunsmith layout: gun area, boxes near their parts, the list panel (plain, unit tested)`

### Task 4: Server side: Deci accessors, gun parts, container, handler (dev test first)

**Files:**
- Modify: `dev/src/main/java/net/decimation/fixes/Deci.java` (new accessors, section "weapons")
- Create: `dev/src/main/java/net/decimation/fixes/gunsmith/GunParts.java`
- Create: `dev/src/main/java/net/decimation/fixes/gunsmith/GunsmithContainer.java`
- Create: `dev/src/main/java/net/decimation/fixes/gunsmith/GunsmithGuiHandler.java`
- Create: `dev/src/main/java/net/decimation/fixes/gunsmith/GunsmithClient.java` (client only entry, returns Object)
- Create: `dev/src/main/java/net/decimation/fixes/gunsmith/GunsmithScreen.java` (stub in this task: a GuiContainer
  over the container that draws only the default background and the slots)
- Modify: `dev/src/main/java/net/decimation/worldgen/DecimationWorldGen.java` (postInit: `GunsmithGuiHandler.install()`)
- Create: `dev/src/main/java/net/decimation/worldgen/devtest/GunsmithTest.java`; Modify: `DevAutoTest.mode()` ("gunsmith")

**Interfaces:**
- Consumes: GunSlot, GunsmithAction (Task 1).
- Produces (Deci, obfuscated names from deobf/decimation.srg): `int attachmentsGuiId()` (DeciConstants.gUI_ATTACHMENTS
  deci/Q/c.Xc); `Object decimationMod()` (DecimationMod.instance deci/a/b.f); `IGuiHandler decimationGuiHandler()`
  (NetworkRegistry's private serverGuiHandlers map entry for Decimation's ModContainer, ReflectionHelper);
  `int attachKeyCode()` (KeyBindingHandler.keyAttach deci/c/a.dt, getKeyCode; client only, int return);
  `boolean fitsSlot(ItemStack gun, int deciIndex, ItemStack part)` (a FilteredSlot deci/f/k parent at index 0 over a
  temporary InventoryBasic holding gun.copy(), a child FilteredSlot at deciIndex, child.isItemValid(part));
  `boolean isIntegrallySuppressed(Item gun)` (fL), `boolean hasForcedTracer(Item gun)` (stats aeq, forcedTracer adA);
  `boolean isSkin(Item)` (deci/ay/j), `String skinId(Item)` (fS), `String skinName(Item)` (fT), `String skinGun(Item)`
  (fR); `boolean isTracer(Item)` (deci/ay/k), `int tracerColorId(Item)` (fW); `Item skinItem(ItemStack gun)` (the
  SkinRegistry.allSkins deci/aD/q.awF entry with skinGun == the gun's gunName aer and skinId == GunItem.getSkin(stack)
  A, or null); `Item tracerItem(int colorId)` (scan Item.itemRegistry once, cache the map).
- Produces: `GunParts.read(ItemStack gun) -> ItemStack[6]` (Sight, Barrel, Grip, Stock from
  GameRegistry.findItem("deci", nbt key), Paint from Deci.skinItem, Tracer from Deci.tracerItem when the key exists);
  `GunParts.write(ItemStack gun, GunSlot slot, ItemStack part)` (Sight..Stock: the part's registry name or ""; Paint:
  "skin" = skinId and "skinEng" = skinName; Tracer: "tracerColorID" = colorId; creates the tag when missing).
- Produces: `GunsmithContainer(EntityPlayer player)`: `final int heldSlot`; slots 0..5 the parts (index =
  GunSlot.ordinal()), slots 6..14 the hotbar (inventory 0..8), slots 15..41 the main inventory (9..35, placed off
  screen by the screen); `ItemStack gun()`; `int slotUpdates` (counted in detectAndSendChanges: slots that differ from
  inventoryItemStacks before super runs). Server rules: canInteractWith is false once
  `player.inventory.currentItem != heldSlot` or the held stack is not the same object or not a gun; the held slot and
  hotbar swaps onto it are refused in slotClick (mode 2 with button == heldSlot, or slotId == 6 + heldSlot);
  transferStackInSlot returns null; a part slot accepts a drag only when Deci.fitsSlot says so, Paint and Tracer
  cannot be taken; any change of the four removable part slots writes the gun NBT; enchantItem(player, id) decodes
  GunsmithAction: fit takes ONE item from inventory index invIndex (refused with "Inventory full" when the old part
  cannot go back: the source stack has more than one item and there is no empty inventory slot), puts it in the part
  slot, writes NBT, returns the old removable part to the inventory; takeOff refuses with "Inventory full" when
  `getFirstEmptyStack() < 0`. onContainerClosed: vanilla only (the cursor item), parts are never dropped.
- Produces: `GunsmithGuiHandler implements IGuiHandler` wrapping Decimation's handler: for attachmentsGuiId() the
  server returns a GunsmithContainer, or null plus a gray chat line "Hold a gun to modify it" when the held item is not
  a gun; the client returns `GunsmithClient.screen(player)` (Object); every other id goes to Decimation's handler.
  `static void install()` registers it for decimationMod() through NetworkRegistry.INSTANCE.registerGuiHandler.

- [ ] **Step 1: Write the dev test first** (GunsmithTest, live mode `gunsmith`, records PASS / FAIL with
  DevTestResults.check; survival player on DevTestArena, doMobSpawning false, inventory cleared, then given: m4a4,
  two 4x, reddot, arSuppressor, foregrip, the m4a4 paint from SkinRegistry, an ak74 paint, an orange tracer powder):
  1. empty hand, `KeyBinding.onTick(Deci.attachKeyCode())`: after 20 ticks no GunsmithScreen, and the chat lines the
     test records (its own ClientChatReceivedEvent listener) hold "Hold a gun to modify it".
  2. gun in hand, key: within 40 ticks `mc.currentScreen instanceof GunsmithScreen` and its container's heldSlot is
     the selected hotbar index.
  3. fit 4x through `mc.playerController.sendEnchantPacket(windowId, GunsmithAction.fit(SIGHT, idx))`: the SERVER
     player's held gun NBT sightAttach == "4x" and one 4x remains (stack of two became one).
  4. swap to reddot: sightAttach "reddot", the 4x count back to two.
  5. take off: sightAttach "", reddot back in the inventory.
  6. full inventory: with reddot fitted and the two 4x in one stack, fill every empty slot with dirt; fitting a 4x is
     refused (the reddot could not come back): NBT still "reddot", chat "Inventory full"; take off refused the same
     way; dirt removed.
  7. rules: Deci.fitsSlot false for arSuppressor on the first registered gun with isIntegrallySuppressed, for any
     scope on the first gun with hasIntegratedScope, for the ak74 paint on the m4a4; true for the m4a4 paint.
  8. paint and tracer: fit both by action: "skin" == the paint's skinId, "tracerColorID" == the powder's id, both
     items gone, `GunsmithAction.decode(takeOff(PAINT))` is null.
  9. drag: windowClick pick up arSuppressor from its hotbar slot, place it on part slot BARREL: barrelAttach
     "arSuppressor".
  10. held slot locked: windowClick mode 2 (swap) with button == heldSlot, and a pickup on slot 6 + heldSlot: the
      server's held stack is still the same gun object.
  11. close with I: screen gone, no EntityItem within 4 blocks of the player, held index unchanged, NBT kept.
  12. stack replaced while open (Review Focus 3): set the server held slot to `gun.copy()`: within 2 ticks the client
      screen is closed and the copy keeps every part.
  13. death while open (Review Focus 4): count non empty inventory stacks, kill the server player
      (DamageSource.outOfWorld, 1000), count EntityItems near the body == that count, then respawn.
- [ ] **Step 2: Run it, see it fail**

Run: `python3 tools/devtest.py --live gunsmith` (main, port 25599; start the live game after the pgrep check)
Expected: FAIL from check 2 on (Decimation's AttachmentScreen opens, NBT unchanged).

- [ ] **Step 3: Write the Deci accessors, GunParts, GunsmithContainer, GunsmithGuiHandler, GunsmithClient and the
  stub GunsmithScreen; call GunsmithGuiHandler.install() in DecimationWorldGen.postInit.**
- [ ] **Step 4: Run it, see it pass**

Run: `python3 tools/devtest.py --live --stop` then `python3 tools/devtest.py --live gunsmith`
Expected: every gunsmith check PASS.

- [ ] **Step 5: Prove the dedicated server still boots**

Run: `python3 tools/servertest.py 1`
Expected: the server reaches "Done", our placement lines print, no NoClassDefFoundError.

- [ ] **Step 6: Commit** `Gunsmith server side: Decimation's attachments GUI opens our container (the gun stays in hand, list actions, its own rules), dev test gunsmith`

### Task 5: The screen (dev test first)

**Files:**
- Modify: `dev/src/main/java/net/decimation/fixes/gunsmith/GunsmithScreen.java` (full screen)
- Modify: `dev/src/main/java/net/decimation/worldgen/devtest/GunsmithTest.java` (screen checks and shots)

**Interfaces:**
- Consumes: Silhouette (Task 2), GunsmithLayout (Task 3), GunsmithContainer and GunSlot (Tasks 1, 4).
- Produces: `GunsmithScreen extends GuiContainer` with test hooks `int[] box(GunSlot s)` (top left), `int[] point(GunSlot
  s)`, `int[] gunBounds()` ({x, y, w, h} in GUI units), `GunSlot openList()` (null when closed), `int[] listRow(int i)`
  (top left of row i), `int measures` (how often the silhouette was measured).

Screen behaviour (spec 2 and 3, mockup A docs/shots/gunsmith_v0.65.0/):
- initGui: guiLeft = guiTop = 0, xSize = width, ySize = height; part slot positions = box + 4; hotbar slots in one row
  at the bottom centre (y = height - 24); main inventory slots at x = -2000; mark for measuring.
- Measuring (on open, on resize, when the held gun's six part values change; checked in updateScreen, never per frame):
  colour writes off, depth cleared, the gun drawn at a probe scale through
  `MinecraftForgeClient.getItemRenderer(gun, ENTITY).renderItem(ENTITY, gun, RenderBlocks.getInstance(), item)` (one
  cached EntityItem), the gun area's depth read once into a reused FloatBuffer (real pixels = GUI units times the
  scale factor), `Silhouette.measure` on depth < 1, the final scale fills 90 % of the area width or 80 % of its height,
  points scaled about the draw origin (the GUI projection is orthographic), boxes from GunsmithLayout.boxes; colour
  writes on, depth cleared.
- Rotation: side view with the muzzle to the right; set by the shot check in Step 1 item 2 (a suppressor fitted must
  move the right edge of the bounds, not the left).
- Drawing per frame: drawDefaultBackground, the gun with standard item lighting at the measured scale, lines from each
  box edge to its point plus a 3 x 3 red dot, the boxes, labels at half size under each box (the part's display name
  or the slot label; "Built in" dimmed for the three cases in Global Constraints), the gun's display name at the top,
  "Esc  Close" top right, the slots through GuiContainer with the held hotbar slot outlined red, the list panel last.
- Input: a click on a part box with an empty cursor toggles its list (a "Built in" box does nothing); a list row sends
  `sendEnchantPacket(windowId, GunsmithAction.fit(slot, invIndex))` or `takeOff(slot)`; a click outside a list closes
  it; any other click goes to GuiContainer (dragging from the hotbar); the attach key, E and Esc close the screen;
  number keys aimed at the held slot are ignored.
- The list (rebuilt only when its slot opens or the inventory signature changes in updateScreen): every inventory index
  0..35 whose stack Deci.fitsSlot accepts, icon and display name per row, "Take off" first when the slot holds a
  removable part, "None in your inventory" when empty, at most MAX_ROWS rows with the mouse wheel scrolling.

- [ ] **Step 1: Add the screen checks to GunsmithTest first**
  1. clicks like a mouse (DevTestUtil.clickAt after drawScreen with the mouse over): the Sight box opens a list titled
     "Sights you carry" whose rows name 4x and reddot; its 4x row fits the 4x (server NBT); the list closes.
  2. orientation: bounds measured bare, then with arSuppressor fitted: the right edge moved out, the left edge did not.
  3. Take off row, "None in your inventory" (open Grip with no grip carried), "Built in" (Sight box text on the
     integrated scope gun of Task 4 item 7).
  4. drag from the hotbar row onto the Barrel box by two clickAt calls: barrelAttach set.
  5. for m4a4, glock17, svd, r870, uzi, minigun, deagle and rpg7 at 854 x 480 and 1440 x 810: every point inside
     gunBounds, every box inside the screen and apart from the others (same rules as Task 3), shot
     run/client/devtest/gunsmith_<gun>_<w>.png; rpg7's lists all say "None in your inventory".
- [ ] **Step 2: Run, see the new checks fail** (`python3 tools/devtest.py --live --stop`, then `--live gunsmith`; the
  stub screen has no boxes).
- [ ] **Step 3: Write the full GunsmithScreen.**
- [ ] **Step 4: Run, see every gunsmith check pass; read every shot** (lines end on the right parts, labels readable,
  nothing covered). Copy the keepers to docs/shots/gunsmith_v0.65.0/ and describe them in docs/shots_index.md.
- [ ] **Step 5: Commit** `Gunsmith screen: the held gun in 3D, six slots with lines to their parts, click to fit`

### Task 6: Optimize and prove it (CLAUDE.md "Every feature: optimized before done")

**Files:**
- Modify: `dev/src/main/java/net/decimation/worldgen/devtest/GunsmithTest.java` (`perf=true` phase)
- Modify: whatever the numbers point at (GunsmithScreen, GunsmithContainer)

- [ ] **Step 1: Add the perf checks first** (run with `perf=true`, mob spawning off, the same view each time):
  1. frame time: median of 200 frames with the screen closed, then 200 open (skip the first 20): open minus closed at
     most 1.0 ms.
  2. textures: TextureManager's loaded texture count (as RefugeVisitTest.Probe counts it) unchanged from frame 2 open
     to frame 200.
  3. GL: errors 0 and modelview / projection / attrib stack depths equal before and after the screen's drawScreen
     (as ScopePerfTest.Probe checks them).
  4. measuring: `measures` is 1 after opening and rises by exactly 1 per fitted part; the list builds once per open.
  5. server: `slotUpdates` rises by 0 over 100 idle ticks with the screen open.
- [ ] **Step 2: Run** `python3 tools/devtest.py --live gunsmith perf=true` and fix what fails, profiling first
  (docs/performance.md; JFR on the live game if the frame budget fails), re-running until all pass.
- [ ] **Step 3: Production client with OptiFine and a production server** (docs/production_test.md): build the jar
  (`cd dev && ./gradlew build`, then `python3 tools/onejar.py dev/build/libs/<exact jar> OUT`), run the production
  server with it (`python3 tools/prodsrv_run.py SERVER_DIR JAR NAME`), join with the production client (OptiFine in
  mods), run `python3 tools/devtest.py --live --port=25598 gunsmith perf=true`. Expected: every check PASS there, which
  is also Review Focus 5 (off the integrated server).
- [ ] **Step 4: Write the numbers** (dev and production: frame cost open vs closed, texture count, GL, slot updates)
  into docs/decimation_code/weapons.md under a new "Gunsmith screen" section.
- [ ] **Step 5: Commit** `Gunsmith measured: frame cost, textures, GL, server updates (dev and production with OptiFine)`

### Task 7: Docs, graph, prerelease

**Files:**
- Modify: docs/decimation_code/weapons.md, docs/decimation_code/menus.md (code map rows), docs/ideas.md (25 built),
  docs/roadmap.md, docs/tasks.md, docs/shots_index.md, CLAUDE.md (one dated line under "Current state"), bug.md for any
  bug met on the way, the decimation-code skill that covers weapons (its quick reference row).

- [ ] **Step 1: Write the docs** above (what was built, where, how to test, the numbers, what is open: idea 26).
- [ ] **Step 2: Check them**: `python3 tools/skillcheck.py` (every docs/decimation_code file and decimation-code skill)
  Expected: all "ok".
- [ ] **Step 3: Graph**: `python3 tools/graph_update.py prepare`, carry the changed docs with a small EXTRA.json
  (`tools/graph_carry.py`), `python3 tools/graph_update.py finish`; confirm GunsmithScreen, GunsmithContainer,
  Silhouette and GunsmithLayout appear in graphify-out/graph.json.
- [ ] **Step 4: Commit** `Gunsmith screen written down; graph updated`, then check no jar and nothing over 5 MB is
  tracked (`git ls-files | grep -E '\.jar$'`).
- [ ] **Step 5: Carry to prerelease** (plain feature, spec 4): in /Users/ucilmenangis/Projects/Decimation-prerelease
  `git apply --3way` the code commits of Tasks 1 to 6 (fixes/gunsmith, the Deci accessors, the postInit line, the dev
  test and its registration), run `./gradlew test` there and `python3 tools/devtest.py --live gunsmith` (port 25597),
  commit `Gunsmith screen (from main <sha>)`, check `git show --stat` holds only those files.

# New Attachments (RK-1 Foregrip, SpecterDR 1x/4x, grips placed per gun) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Two new attachments of our own in Decimation's style (RK-1 Foregrip, ELCAN SpecterDR 1x/4x) and every grip
slot attachment drawn under each gun's own handguard.

**Architecture:** Items are Decimation AttachmentItems registered as `deci:<name>` (as NewSights does). Placement uses
the per gun model cache (`Deci.offsetAttachment`, as SightPlacement) from a fit tool's table. Recoil goes through one
Javassist patch that asks our hook `fixes/GripRecoil`. The SpecterDR switch is a client key plus a per gun mode read
by ScopeZoom.

**Tech Stack:** Java 8 (Forge 1.7.10, MCP names in dev/), Javassist patches (tools/patches), Python 3 + PIL
(tools/guns, tools/guns/refcapture.py), JUnit 5 (dev/src/test), dev tests (tools/devtest.py --live).

**Spec:** docs/superpowers/specs/2026-10-21-new-attachments-design.md

## Global Constraints

- RK-1 Foregrip: category rifle (as Decimation's Foregrip); recoil UP x 1/2, SIDEWAYS x 1/4; the view's kick calmed
  as the Foregrip's. Decimation's Foregrip stays x 1/3 both (unchanged); every other grip slot item x 1.
- SpecterDR: category all (as the ACOG); 1x = ScopeZoom magnification 1.25, 4x = 4.0; key G by default, rebindable;
  the gun and the see-through glass kept at 4x (no black overlay); one reticle in both modes, in the glass; mode per
  gun for the session; a click on switching.
- Placement for deci:rk1, deci:foregrip, deci:laser, deci:flashlight on every gun they fit.
- Loot: the SpecterDR joins every list holding Decimation's 4x, the RK-1 every list holding the Foregrip
  (Deci.addLootLike), plus fixes/LootPools beside them.
- Registry names deci:rk1 and deci:specterdr; display names "RK-1 Foregrip" and "ELCAN SpecterDR 1x/4x".
- Models built in code in Decimation's style (skill decimation-gun, docs/gun_style_guide.md); Tarkov pictures are
  references only: never copied into textures, never committed, never shipped.
- The jar patch goes into the three jars (Decimation.jar.patched, dist/Decimation.jar, dev/libs/Decimation-base.jar);
  its hook class exists on main and prerelease.
- No dash characters in any text the player reads. Commits one plain line, no attribution.
- Gradle never runs in a checkout whose live game is open (the user's game may be open in main: use
  ../Decimation-scratch then).

## Review Focus

1. Guns without a handguard (pistols take the Laser and Flashlight; SMGs; bullpups like the AUG; pump shotguns): the
   grip must sit under the barrel or pump, touching, never inside the magazine or trigger guard. Pinned in Task 5:
   `gripfit.py --check` over every gun and grip pair.
2. G pressed while aiming, or with another sight fitted: the zoom eases to the new magnification without a jump; with
   the ACOG or no sight nothing changes and no click plays. Pinned in Task 6 checks.
3. A dedicated server: recoil and drawing are client side; the RK-1's numbers must hold for a joined player. Pinned in
   Task 7 (production server recoil check).
4. Old guns with Decimation's Foregrip: its recoil stays exactly 1/3 (regression). Pinned in Task 4 checks.
5. Two guns with a SpecterDR each: their modes stay apart; a gun without an id falls back to its hotbar slot. Pinned
   in Task 6 unit test SpecterModeTest.

---

### Task 1: Close up references (refcapture --focus)

**Files:**
- Modify: `tools/guns/refcapture.py`

**Interfaces:**
- Produces: `refcapture.py PRESET.json OUTDIR --focus=X,Y --closer=N`: before the views, the view is panned so the
  side view pixel (X, Y) of the zoomed out side view comes to the frame's middle (right drag, OrbitControls pan), then
  zoomed in N wheel steps; views then turn about that point.

- [ ] **Step 1: Selftest first.** Add to `selftest()`: `pan_for((900, 500), (W, H)) == (W // 2 - 900, H // 2 - 500)`.
- [ ] **Step 2: Run** `python3 tools/guns/refcapture.py --selftest`. Expected: AssertionError / NameError (no pan_for).
- [ ] **Step 3: Implement** `pan_for(point, frame) -> (dx, dy)` and the `--focus` / `--closer` options (right button
  drag of (dx, dy) after `reset(steps)`, then `--closer` wheel steps of -100).
- [ ] **Step 4: Run** the selftest (Expected: `selftest ok`), then capture both parts from the user's preset:
  `python3 tools/guns/refcapture.py ~/Downloads/my_preset.json docs/shots/refcapture_v0.65.0/rk1 --focus=<grip x,y in
  my_preset/side.png's frame> --closer=12` and the same for the scope (`.../specterdr`). Expected: in each `side.png`
  the part fills at least 40 % of the cut out's width (read the sheets).
- [ ] **Step 5: Commit** `tools/guns/refcapture.py` ("refcapture: close ups of one part").

### Task 2: The two models (RK-1, SpecterDR) in Decimation's style

**Files:**
- Create: `tools/guns/grips.py` (RK-1, written like sights.py: parts in GUN space, shifted into the file at the end)
- Modify: `tools/guns/sights.py` (add the SpecterDR)
- Outputs (committed, our art): `dev/src/main/resources/assets/deci/models/attachments/grip/rk1.bmodel`,
  `textures/model/attachments/grip/rk1.png`, `textures/items/attach/rk1.png`, and the same for `sight/specterdr`,
  plus the reticle `textures/model/guns/scopes/specterdr.png`

**Interfaces:**
- Produces: model files at the paths above (Decimation's grip file space: drawn at file coordinates plus GL (-0.2,
  0.05, 0); sight file space: plus (0.8, 1.12, -0.128) model units as sights.py states); glass parts of the SpecterDR
  named `scopeGlass*`.

- [ ] **Step 1: Checks first.** `python3 tools/guns/study.py contact` style check for the two new models (no floating
  part): add `grips.py --check` and `sights.py --check specterdr` that exit 1 on a floating part or on part counts
  outside docs/gun_style_guide.md's attachment range.
- [ ] **Step 2: Run** both checks. Expected: exit 1 (no models yet).
- [ ] **Step 3: Build** both models from the close ups of Task 1 (skill decimation-gun steps 1 to 4: proportions from
  `side` and `top`, Decimation's shape kinds and flat dark tones), icons from the render.
- [ ] **Step 4: Run** the checks (Expected: exit 0) and a side by side sheet: ours rendered next to each reference
  (`study.py attach m4a4 rk1`, `attach m4a4 specterdr`). Read it; fix until the shapes and proportions match.
- [ ] **Step 5: Commit** the tools and the outputs ("RK-1 Foregrip and SpecterDR models").

### Task 3: Items, loot, Gunsmith

**Files:**
- Modify: `dev/src/main/java/net/decimation/fixes/Deci.java` (add `newGripAttachment`)
- Modify: `dev/src/main/java/net/decimation/fixes/NewSights.java` (register the SpecterDR)
- Create: `dev/src/main/java/net/decimation/fixes/NewGrips.java`
- Modify: `dev/src/main/java/net/decimation/fixes/LootPools.java`, the lang file
  `dev/src/main/resources/assets/deciworldgen/lang/en_US.lang`, `worldgen/DecimationWorldGen.java` (NewGrips.register
  beside NewSights.register)
- Create: `dev/src/main/java/net/decimation/worldgen/devtest/AttachmentsTest.java`; register `attachments` in
  `DevAutoTest.mode()`

**Interfaces:**
- Produces: `public static Item Deci.newGripAttachment(String name)` (AttachmentItem, slot grip, category rifle,
  registered in Decimation's namespace like newSightAttachment); `NewGrips.rk1`, `NewSights.specterdr` (static Item).

- [ ] **Step 1: Dev test checks first** (AttachmentsTest, step "items"): `deci:rk1` and `deci:specterdr` exist with
  display names "RK-1 Foregrip" and "ELCAN SpecterDR 1x/4x"; `Deci.fitsSlot` says RK-1 fits m4a4 and not glock17,
  SpecterDR fits m4a4 and glock17; the loot lists holding deci:4x also hold deci:specterdr and those holding
  deci:foregrip hold deci:rk1 (counts equal).
- [ ] **Step 2: Run** `python3 tools/devtest.py --live attachments`. Expected: those checks FAIL.
- [ ] **Step 3: Implement** the accessor, registration, loot (addLootLike, LootPools lines beside deci:ta11acog and
  deci:foregrip), names.
- [ ] **Step 4: Run** the dev test (Expected: PASS) and `gunsmith` (Expected: still all PASS).
- [ ] **Step 5: Commit** ("RK-1 Foregrip and SpecterDR registered, in loot").

### Task 4: Grip recoil

**Files:**
- Create: `dev/src/main/java/net/decimation/fixes/GripRecoil.java`, `dev/src/test/java/net/decimation/fixes/GripRecoilTest.java`
- Create: `tools/patches/PatchGripRecoil.java` (Javassist; applied to the three jars)
- Modify: AttachmentsTest (step "recoil")

**Interfaces:**
- Produces: `GripRecoil.pitch(ItemStack gun, float recoil) -> float`, `GripRecoil.yaw(ItemStack gun, float recoil)
  -> float`, `GripRecoil.calmsView(Object grip) -> boolean`, `GripRecoil.factors(String gripName) -> float[] {up,
  side}`.
- The patch, decided: in GunItem.clientHeldTick (`deci/ay/i.d(ItemStack, EntityPlayer)`) the two reads of
  ItemRegistry.foregrip become null (the Foregrip branch never runs) and every call of PlayerData.getRecoilPitch
  (`deci/Q/b.cs()F`) / getRecoilYaw (`ct()F`) becomes `GripRecoil.pitch($1, $proceed())` / `yaw(...)`; in
  GunItemRenderer (`deci/K/b`, on top of PatchScope and PatchIronSights) the call `grip.equals(ItemRegistry.foregrip)`
  becomes `GripRecoil.calmsView($0) || $proceed($$)`.

- [ ] **Step 1: Tests first.** `GripRecoilTest.factorsMatchTheSpec`: factors("foregrip") = {1/3, 1/3}, ("rk1") =
  {0.5, 0.25}, ("laser") = {1, 1}, (null) = {1, 1}; `calmsViewForBothGrips`. Dev test step "recoil": the M4A4 fired
  once (Deci.fireHeldGun) at pitch 0 with no grip, the Foregrip, the RK-1; for the 30 ticks after the shot, the summed
  pitch change over the summed PlayerData recoil pitch of the same ticks (a Deci accessor; the same ticks, so the
  random sideways kick cancels out), and the same for yaw head: no grip 1 and 1, Foregrip 1/3 and 1/3, RK-1 1/2 and
  1/4 (within 2 %).
- [ ] **Step 2: Run** `./gradlew test --tests '*GripRecoilTest'` (Expected: compile FAIL) and the dev test (Expected:
  RK-1 ratios 1 and 1, FAIL).
- [ ] **Step 3: Implement** GripRecoil, the patch, apply it to the three jars (check the backup hash, entry counts,
  `unzip -tq`), restart the live game.
- [ ] **Step 4: Run** the unit test and the dev test. Expected: PASS, Foregrip still 1/3.
- [ ] **Step 5: Commit** GripRecoil, its test, the patch source ("Grip recoil through GripRecoil: RK-1 1/2 up, 1/4
  sideways").

### Task 5: Grips placed per gun

**Files:**
- Create: `tools/guns/gripfit.py` (writes `dev/src/main/resources/assets/deciworldgen/grip_offsets.txt`, lines
  `gun grip dx dy dz`)
- Create: `dev/src/main/java/net/decimation/fixes/GripPlacement.java`, `dev/src/test/java/net/decimation/fixes/GripPlacementTest.java`
- Modify: `worldgen/DecimationWorldGen.java` (GripPlacement.apply beside SightPlacement.apply), AttachmentsTest (step
  "placement")

**Interfaces:**
- Produces: `GripPlacement.parse(List<String> lines) -> Map<String, float[]>` (key "gun grip"), `GripPlacement.apply()`
  (client: Deci.offsetAttachment per line).

- [ ] **Step 1: Checks first.** `gripfit.py --check` (exit 1 listing each gun and grip pair whose placed grip does not
  touch the gun's underside in the front half, or overlaps the magazine or trigger guard; pistols take the Laser and
  Flashlight under the barrel); `GripPlacementTest.parsesLines`; dev test step "placement": after startup the
  Foregrip's model for m4a4 in its per gun cache is offset (not the shared model) and shots of m4a4 + rk1, ump45 +
  foregrip, glock17 + laser.
- [ ] **Step 2: Run** them. Expected: `--check` lists pairs (today's fixed spot), the unit test fails to compile, the
  dev test FAILs.
- [ ] **Step 3: Implement** gripfit (measure each gun's underside in its front half from the model, like sightfit),
  write the table, GripPlacement.
- [ ] **Step 4: Run** `gripfit.py --check` (Expected: exit 0), the unit and dev tests (PASS), read the shots.
- [ ] **Step 5: Commit** ("Grips placed per gun: RK-1, Foregrip, Laser, Flashlight").

### Task 6: SpecterDR 1x/4x

**Files:**
- Create: `dev/src/main/java/net/decimation/fixes/SpecterMode.java`, `dev/src/test/java/net/decimation/fixes/SpecterModeTest.java`
- Modify: `fixes/ScopeZoom.java` (magnification and overlay for specterdr), `fixes/SightReticle` table, run
  `tools/guns/sightfit.py` (the SpecterDR's rows), `worldgen/DecimationWorldGen.java` (key registration, client),
  AttachmentsTest (step "specter")

**Interfaces:**
- Produces: `SpecterMode.isFour(ItemStack gun, int slot) -> boolean` (default true: 4x), `SpecterMode.toggle(ItemStack
  gun, int slot) -> boolean` (the new state), key "DeciRemnant: SpecterDR 1x/4x" default G (Keyboard.KEY_G, checked
  free against Decimation's and vanilla's bindings); `public static float ScopeZoom.magnificationFor(ItemStack held)`,
  `public static boolean ScopeZoom.overlays(ItemStack held)`.

- [ ] **Step 1: Tests first.** `SpecterModeTest`: a new gun is 4x; toggle flips; two guns with different ids keep
  their own modes; a gun without an id falls back to its slot. Dev test step "specter": the M4A4 with the SpecterDR:
  magnificationFor 4.0, overlays false; the key press (KeyBinding state set as a press) makes it 1.25, again 4.0; a
  second gun keeps its mode; with the ACOG fitted G changes nothing and plays no click; aimed shots at 1x and 4x show
  the gun and the reticle.
- [ ] **Step 2: Run** them. Expected: compile FAIL / dev checks FAIL.
- [ ] **Step 3: Implement** SpecterMode (session map by the gun's id tag where Decimation sets one, else by slot), the
  key (client tick: isPressed, the held gun's sight is specterdr), ScopeZoom's two statics and the specterdr case,
  the reticle row, sightfit rows.
- [ ] **Step 4: Run** the tests. Expected: PASS; read the shots.
- [ ] **Step 5: Commit** ("SpecterDR 1x/4x on G").

### Task 7: Optimize and production

**Files:**
- Modify: AttachmentsTest (perf step), `docs/decimation_code/weapons.md` (numbers)

- [ ] **Step 1: Perf checks first** (AttachmentsTest `perf=true`): frame time aimed through the SpecterDR at 1x and
  4x against the ACOG (at most 0.5 ms more); a gun with the RK-1 against none (at most 0.1 ms); GL clean (glstack
  counters) with every new part drawn; no new allocation per frame from GripPlacement or SpecterMode.
- [ ] **Step 2: Run** in the dev client; then the production client with OptiFine (one jar of main's tree,
  scratchpad prodclient): `attachments` and `attachments perf=true`; then the dedicated production server: a joined
  client fires with the RK-1 (recoil ratios as Task 4).
- [ ] **Step 3: Fix** whatever exceeds a budget (profile first: docs/performance.md), rerun.
- [ ] **Step 4: Write** the measured numbers into docs/decimation_code/weapons.md.
- [ ] **Step 5: Commit** ("Attachments measured: dev, production with OptiFine, dedicated server").

### Task 8: Docs, graph, prerelease

**Files:**
- Modify: docs/decimation_code/weapons.md (attachments section), docs/ideas.md 26, CLAUDE.md (patch list and
  traps), `.claude/skills/decimation-gun/SKILL.md` (grip lessons, case), bug.md ("Grips drawn at one spot for every
  gun"), docs/tasks.md, docs/shots_index.md (attachments shots), the graph

- [ ] **Step 1:** Write the docs; `python3 tools/skillcheck.py docs/decimation_code/weapons.md
  .claude/skills/decimation-code-weapons/SKILL.md` (Expected: ok).
- [ ] **Step 2:** Graph: `python3 tools/graph_update.py prepare`, `tools/graph_carry.py`, `finish`; the new classes
  appear in graph.json.
- [ ] **Step 3:** Carry the code to prerelease (one commit "from main <sha>"), run there: unit tests and
  `python3 tools/devtest.py --live attachments gunsmith` (port 25597). Expected: all PASS.
- [ ] **Step 4:** Full suites on main: unit tests, `gunsmith`, `attachments`, `playerfire`, `scopeperf`.
- [ ] **Step 5: Commit** the docs and the graph ("New attachments written down").

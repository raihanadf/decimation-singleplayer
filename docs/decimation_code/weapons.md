# Decimation code: weapons (guns, shots, ammo, attachments)

Part of the feature study of Decimation's own code (16 Oktober 2026, index docs/decimation_code/README.md). How to
MAKE a gun of our own: skill decimation-gun, docs/gun_model_spec.md, docs/gun_style_guide.md. NPC gunfire: the NPC
feature (BanditEntity.shootAt) and our fixes/NpcShots. Names are the readable ones (obfuscated in brackets); our code
reaches them through `net.decimation.fixes.Deci`.

## 1. What it is for

Every firearm in Decimation is one GunItem with a stat block, a .bmodel model, .anib animations and per gun sounds.
Shots are instant rays, not bullet entities. The CLIENT decides what a shot hits and how much damage it does; the
server takes the round, hurts the target and tells everyone to play the shot.

## 2. Code map

| Class (obf) | Job |
|---|---|
| GunItem (deci/ay/i) | one per gun. `clientHeldTick` (d) polls the mouse each tick (LWJGL `Mouse.isButtonDown`, right button toggles aim), checks fire mode and `stats.secondsPerShot`, calls `fire` (a). `fireRay` (a) per pellet: `getShotOrigin` (r, eye height by stance), `getShotDirection` (a, spread), a ray of 200 blocks against every loaded entity's box and `rayTraceBlocks` (glass, vines, flowers let it through; `handleBlockHit`). `consumeAmmoForShot` (a), `getAmmo` (t) / `setAmmo` (e) on NBT "ammo", `onServerFire` (b) -> `alertNearbyInfected` (64 blocks, 10 suppressed), `applyRecoil` (c), `playFireEffects`. `damage` (aew) set through `setDamage` (am), the one chokepoint (stores round(n * 0.5): the old balance patch) |
| GunStats (deci/ay/e), GunStats$FireMode (deci/ay/e$a) | recoil pitch / yaw, fire modes (SINGLE, AUTO, BURST, PUMP), aim table, recoil recovery, rate of fire: secondsPerShot = 1 / (rpm * 1.3 / 60) |
| WeaponCategory (deci/ay/c) | rifle, smg, mg, shotgun, pistol, revolver, rocket, crossbow, flamethrower (unused), all: also the model and texture folder |
| AmmoItem (deci/ay/f) | loose rounds and magazines (a magazine keeps its rounds in NBT "ammo"); AmmoBoxItem (deci/ay/g) hands out rounds |
| AttachmentItem (deci/ay/h) | sights (zoom fov, sway), suppressors, flashlight; per gun model cache `getModelFor` (b). Gun NBT keys sightAttach, barrelAttach, gripAttach, stockAttach, triggerAttach |
| WeaponSkinItem (deci/ay/j), TracerItem (deci/ay/k) | cosmetic skins and tracer colours |
| GunDamageSource (deci/ab/a) | `forShooter` (c): source "gunDeci", bypasses vanilla armor |
| PacketGunHit (deci/aE/a$z) | client to server, one per pellet: target id, damage, hit height, didHit, shot index, time |
| PacketGunFire (deci/aE/a$A) | client to server, once per shot: `onServerFire`, then PacketGunFireEffects (deci/aE/a$B) to all (sound, flash, tracer) |
| PacketBlockHitParticles (deci/aE/a$C), PacketBulletRicochet (deci/aE/a$D), PacketReload (deci/aE/a$E), PacketFireProjectileWeapon (deci/aE/a$Q) | block hits, ricochet sound, reload steps from the animation, rockets and crossbows |
| GunItemRenderer (deci/K/b) | first and third person drawing, attachments (`renderAttachments`), muzzle flash, smoke puffs; holds the playing GunAnimation |
| GunAnimation (deci/F/a) | .anib files; frames with triggerUnload / triggerLoad send PacketReload |
| ISlowsHolder (deci/ao/a) | guns slow the player who holds them (ClientEventHandler) |

## 3. One player shot, as it runs (call logger trace, 16 Oktober 2026)

Dev test `playerfire` (AKM, an infected 5 blocks ahead, then a shot into the sky); trace prefixes
`deci.ay.i.a,deci.ay.i.b,deci.ay.i.t,deci.ay.i.e,deci.aE.a$z,deci.aE.a$A,deci.aE.a$B,deci.ab.a`.

1. Client thread: `GunItem.fire` -> `fireRay` -> `getShotDirection` -> `rayTraceBlocks` (`handleBlockHit` x17 along
   the ray) -> `consumeAmmoForShot` (the client's copy of the gun: 30 -> 29) -> PacketGunHit with the gun's
   `damage` -> `playFireEffects` (`isUnderRoof` picks the echo) -> PacketGunFire. The hit packet goes BEFORE the fire
   packet.
2. Server thread, PacketGunHit handler: no rounds left = ignored; `consumeAmmoForShot` again on the SERVER's copy
   (30 -> 29); then, if didHit and the shot is under 400 ms old: a player target gets the leg break roll and
   ItemArmorDeci.damageMultiplier of armor slots 0 to 2 (helmet skipped), any living target
   `attackEntityFrom(GunDamageSource.forShooter, damage)` with half the hurt cooldown and its motion zeroed;
   a landmine detonates; a vehicle takes 0.4 from its own riders only. `detectAndSendChanges` syncs the gun.
3. Server thread, PacketGunFire handler: `onServerFire` -> `alertNearbyInfected`, PacketGunFireEffects to all.
4. Client thread: PacketGunFireEffects handler (sound, flash, tracer).

Measured: the infected lost the gun's 8 damage (20 health infected), or 4 with our riot variant (fixes/InfectedVariants
takes 0.5); a miss used a round and hurt nothing; client and server copies agreed (29, then 28).

## 4. Singleplayer state and known faults

- Firing works in singleplayer (the packets are registered on both sides): verified by `playerfire`.
- The server trusts the client's damage value and target; fine for singleplayer, a cheat hole on a server.
- The PacketGunHit handler has an infected branch (helmeted infected take less) after the living entity branch that
  already catches every infected: it can never run (and its roll `nextFloat() > 1.0` could never be true either).
- `setTracerColor` writes "tracerColor" but `getTracerColor` reads "tracerColorID" (the attachment screen writes the
  right key, only that helper is broken).
- Every shot walks the whole loaded entity list per pellet on the client [inferred cost, not measured].
- The picture in picture scope rendered the world twice: replaced by our ScopeZoom (v0.28, PatchScope); the sway
  thread SmoothSwingThread spun a core at 100 %: PatchSwing.
- With a gun in hand ClientEventHandler.onClientTick (START) unbinds the use key every tick (keyBindUseItem key code 0:
  right click only aims) and binds it again (-99) only while the crosshair is on an item frame. So a right click with a
  gun opens no entity: no corpse, no loot body (checked 18 Oktober 2026, dev test lanjoin; bug.md). Our fixes/GunUseKey
  binds it again for bodies.

## 5. How our mod uses it

| Need | Use |
|---|---|
| a new gun, magazine, sight | `Deci.newGun`, `Deci.newMagazine`, `Deci.newSightAttachment`, fixes/NewGuns, fixes/NewSights (skill decimation-gun) |
| read a gun stack | `Deci.gunAmmo`, `Deci.gunDamageOf`, `Deci.gunMagazine`, `Deci.gunIsAutomatic`, `Deci.gunSecondsPerShot`, `Deci.isScopedGun`, `Deci.hasSuppressor` |
| magazines | `Deci.gunTakes`, `Deci.addGunMagazine`, `Deci.gunMagazines`, fixes/Magazines (60 round mags) |
| fire the held gun in a test | `Deci.fireHeldGun(player)` (client thread) |
| attachments drawn right | `Deci.offsetAttachment`, fixes/SightPlacement, fixes/SightReticle, fixes/IronSights, PatchIronSights |
| scopes | fixes/ScopeZoom, tools/patches/PatchScope.java |
| armor against NPC shots | fixes/ArmorGunfireHandler (NPC shots use the "human" source, not PacketGunHit) |
| gunshot noise | fixes/GunNoise |
| open a body with a gun in hand | fixes/GunUseKey (after Decimation's tick: use key bound while the crosshair is on a corpse or loot body) |
| grips placed per gun | tools/guns/gripfit.py (grip_offsets.txt; `--check`), fixes/GripPlacement (`Deci.offsetAttachment`, `Deci.clearAttachmentPartBoxes`, `Deci.batchAttachmentParts`: one display list a copy, Deci.BatchedPart); section 8 |
| recoil per grip | fixes/GripRecoil (Foregrip 1/3, RK-1 1/2 up and 1/4 sideways), tools/patches/PatchGripRecoil.java (GunItem.clientHeldTick, GunItemRenderer.renderItem; three jars) |
| new attachments | `Deci.newGripAttachment` (fixes/NewGrips: RK-1), `Deci.newSightAttachment` (fixes/NewSights: EOTech 558, ACOG TA11, SpecterDR); models tools/guns/grips.py, tools/guns/sights.py |
| the SpecterDR's 1x / 4x | fixes/SpecterMode (per gun for the session), fixes/SpecterKey (G), `ScopeZoom.magnificationFor`, `ScopeZoom.overlays` |
| fit attachments (the I key) | the Gunsmith screen: fixes/gunsmith/GunsmithGuiHandler.java (Decimation's attachments GUI id, `Deci.attachmentsGuiId`, opens our container and screen), GunsmithContainer, GunsmithScreen, GunParts (`Deci.fitsSlot`: Decimation's own FilteredSlot rules); section 7 |

## 6. Checks

- `python3 tools/devtest.py --live playerfire`: the player's shot end to end (9 checks).
- `gun` (reload loads a magazine, NPC fires), `gunview` (hip, aim, NPC view per gun), `gunperf`, `firerate`,
  `checks` (armor against NPC fire, 60 round mags).
- `python3 tools/devtest.py --live gunsmith` (110 checks: the screen, fits, rules, no loss, the cursor on close and on
  logout, 8 guns at two sizes, every registered gun's layout, turning and zoom),
  `gunsmith perf=true` (10 checks, section 7), `gunsmithmp server=HOST:PORT` (a joined client on a dedicated server; run
  it last, it leaves for the menu).
- `attachments` (22 checks: the RK-1 and SpecterDR items, loot, recoil per grip, grips placed per gun, the SpecterDR's
  switch), `attachments perf=true` (7 checks: draw cost per attachment, aimed frames, GL, allocation; section 8),
  `attachmentsmp server=HOST:PORT` (12 checks on a dedicated server; run it last), `python3 tools/guns/gripfit.py
  --check` (every gun and grip pair placed right), `glstack` (every sight and grip on two guns).

## 7. The Gunsmith screen (21 Oktober 2026, plan docs/superpowers/plans/2026-10-21-gunsmith-screen.md)

- The I key stays Decimation's (KeyBindingHandler, the ATTACH action); fixes/gunsmith/GunsmithGuiHandler.java wraps
  Decimation's IGuiHandler (FML keeps one per mod) so its attachments id opens GunsmithContainer and GunsmithScreen.
  No gun in hand: nothing opens, a gray chat line "Hold a gun to modify it".
- The gun stays in its hotbar slot. Part slots are a view of the gun's NBT (the keys Decimation's AttachmentInventory
  uses): every change is written at once, so closing, death or a disconnect lose nothing. Paint and tracer are used up.
  List clicks are container button ids (Container.enchantItem): ONE signed byte on the wire (C11PacketEnchantItem), so
  GunsmithAction packs slot and inventory index into 0 .. 221 (bug.md "Gunsmith list clicks lost on a server").
- The screen draws the held gun with Decimation's renderer (its dropped item draw, stood up side on), measures its outline
  from the depth buffer once per open and per change (Silhouette), places boxes and lines (GunsmithLayout), hides the HUD
  and the hand while open (GunsmithClient). A model drawn into the GUI's view needs one mirror (the view flips y), else
  it draws inside out (bug.md "Gunsmith screen drew the gun inside out").
- Measured (production client with OptiFine HD U E7, one jar of main, singleplayer; dev client numbers in brackets, the
  user's own game ran beside it): frame median closed 4.75 ms, open 4.26 ms (13.57, 12.10): the hidden HUD and hand cost
  more than the screen; the screen's own draw 0.97 ms with a full bag, 0.99 ms empty, GPU included (1.29, 1.56); 0
  textures loaded while open; 0 GL errors and no stack change in 100 draws; 0 slot updates in 100 idle ticks; the outline
  measured once per open plus once per fitted part, a list built once per open. Before the fix of the hidden bag slots
  (GunsmithContainer.BagSlot: GuiContainer drew the 27 main inventory items each frame, off screen) a full bag added 0.39
  ms (1.52 against 1.13). Four earlier runs gave open minus closed +1.35 (the first run after the functional test), +0.13,
  -2.72, -0.58 ms.
- On a dedicated production server (gunsmithmp): joined, the key opens it, the 4x fitted and taken off as the server sent
  them back, closing drops nothing; frame 4.46 ms closed, 4.21 ms open.
- Final review fixes (21 Oktober 2026, bug.md "a part copied by a swap", "a part on the cursor dropped"):
  GunsmithContainer.slotClick refuses unless the hand still holds the very stack the screen opened on (a container's
  canInteractWith runs once a tick, after that tick's packets); a part on the cursor goes back to the bag on closing and
  on PlayerLoggedOutEvent (GunsmithContainer.Logout; dropped beside the player only when the bag is full); part slots and
  the gun's slot never join a drag (canDragIntoSlot); "Inventory full" is said in the screen for 3 s
  (GunsmithClient.chat, GunsmithScreen.notice) and replaces Take off in a list when the bag is full.
- Optimized in the same pass: the depth is read from the gun area only (GunsmithScreen.probe), into buffers kept
  between openings, one probe plus one finer probe at the drawn size (halved while the area's edge cuts the gun): 3.08 ms
  and 323,568 depth pixels at 854 x 480 (twice the 161,784 pixel area), before one probe of the whole window read 409,920
  pixels and a small gun up to five; list rows, their widths, the panel and the box labels are built on a change only (0
  text built per frame with a list open, was 727 then 2701 over 56 ticks); ScopeZoom does no glass work while the screen
  is open over an aimed gun (GunsmithClient.isOpen). The list is as wide as its longest row (LIST_W 120 up to 200,
  GunsmithLayout.list with a width): "None in your inventory" was cut to "None in your inve.." at 120.
- Measured after the fixes (dev client in the scratch checkout, nothing else running): frame median closed 12.17 ms, open
  9.34 ms; the screen's own draw 1.02 ms with a full bag, 0.94 ms empty; every registered gun (103) lays out at 854 x 480.
  Production client with OptiFine HD U E7 (one jar of main's tree, sha256 76b71b05): 92 checks and perf 8 PASS, frame
  median closed 4.39 ms, open 4.08 ms, own draw 0.90 ms full bag, 0.88 ms empty; joined to the dedicated production
  server (gunsmithmp, 8 PASS): frame 5.54 ms closed, 5.68 ms open.
- Turning (21 Oktober 2026, spec section 9, user: "its possible to make it more like tarkov where it can be viewed 360
  by mouse?"): a left press on empty space and a move of 2 GUI pixels turns the gun (GunsmithScreen.mouseClickMove,
  1 degree a GUI pixel, the tilt stops at 85), it stays turned, a double click on empty space resets, the wheel zooms 1
  to 2.5 (GunsmithScreen.wheel; a list open scrolls instead). The gun turns about its 3D middle: x and y from the side
  view's outline, depth from one more probe from above (half scale, half the area read). Each line end is its part's
  side view point on the middle plane, turned and projected in Java on a view change only (updateEnds); the boxes stay.
  Checked against real renders of four views (6 of 6 line ends on the gun, the middle 0.25 GUI pixels off at 90 degrees);
  measuring now reads 2.5 times the gun area, 5.26 ms once per open or change; turning every frame (dev client): 10.25
  ms against 12.58 closed, GL clean while turned. Production client with OptiFine (one jar sha256 ca773d60): 110 checks
  and perf 10 PASS, measuring 4.42 ms, turning every frame 6.75 ms against 9.06 closed. At large turns the lines cross
  the gun (the boxes stay, as chosen). Shots docs/shots/gunsmith_v0.65.0/turning/.
  The user tried it in runClient the same day: "nice, work really well".
- The user saw it in runClient (21 Oktober 2026): "i like it how the HUD looks" (the dimming, the light band and the
  hidden HUD stay), then asked for Tarkov style turning: "its possible to make it more like tarkov where it can be
  viewed 360 by mouse?" ("cursor i mean").
- Open, the final review's minor findings (deferred, 21 Oktober 2026): M1 the Barrel box says "Built in" on integrally
  suppressed guns, which also blocks Decimation's bayonet there and hides one already fitted; M2 putting on the same
  paint or tracer again uses an item for no change; M3 the open list's frame is drawn with item lighting still on; M4
  smoke puffs of a shot fired just before opening are drawn and measured with the gun; M5 a dragged part lands only on
  the inner 16 x 16 of a 24 x 24 box; M6 clicking the open box opens its list again instead of closing it; M7
  GunsmithGuiHandler.install fails silently when Decimation's handler is missing; M8 box labels are not checked for
  overlap or the screen edge; M9 keys act on a slot hidden under the open list; M10 boxes can sit on short guns (the
  Glock's grip, the Uzi's tracer dot); M11 updateScreen builds two strings every tick; M12 test gaps (gun bounds on
  screen, the crossbow, display list compiles not counted); M13 Deci.tracerItem's lazy cache is unsynchronized
  (harmless).


## 8. New attachments: RK-1 Foregrip and ELCAN SpecterDR 1x/4x (21 Oktober 2026, plan docs/superpowers/plans/2026-10-21-new-attachments.md)

- User (21 Oktober 2026): "we have 2 new attachment: new grip, and 4x scope", chosen: the SpecterDR with a 1x/4x switch
  on a key (default G), the grip with its own numbers (recoil up to 1/2, sideways to 1/4), every grip placed per gun,
  loot like Decimation's 4x and Foregrip. Spec docs/superpowers/specs/2026-10-21-new-attachments-design.md. Models from
  the user's tarkovbot build (tools/guns/refcapture.py views, reference only), built in Decimation's style:
  tools/guns/grips.py (rk1, 56 parts), tools/guns/sights.py (specterdr, 95 parts, reticle drawn in the glass).
- Items: AttachmentItems of ours registered as deci:rk1 (Deci.newGripAttachment, category rifle like the Foregrip:
  fixes/NewGrips) and deci:specterdr (Deci.newSightAttachment, category all: fixes/NewSights), named in our lang file,
  in every Decimation crate list holding the Foregrip / the 4x (Deci.addLootLike) and in LootPools (VERSION 2).
- Recoil per grip: GunItem.clientHeldTick (deci/ay/i.d) turned the view by the player's recoil, divided by 3 only for
  Decimation's Foregrip; tools/patches/PatchGripRecoil.java (three jars, both lines) asks fixes/GripRecoil instead:
  Foregrip 1/3 and 1/3 (unchanged), RK-1 1/2 up and 1/4 sideways, 1 otherwise; GunItemRenderer's view kick calms for
  both (GripRecoil.calmsView). Measured with a known recoil through Decimation's own tick (dev test attachments):
  no grip 40 degrees up and sideways, Foregrip 0.333 / 0.333, RK-1 0.500 / 0.250 of that.
- Grips placed per gun: Decimation drew every grip at one spot for all guns (renderAttachments' grip translate): the
  Foregrip sat on the M4A4's magazine, the Laser and Flashlight inside its handguard. tools/guns/gripfit.py writes
  dev/src/main/resources/assets/deciworldgen/grip_offsets.txt (318 rows: every gun of the jar and our five, each grip it
  takes) from a map of each gun's underside (vertical rays through every part's real faces, a column each 0.05): the
  grip goes ahead of the last thing hanging below the gun (a magazine or pump part below the body, anything more than
  2.3 below the bore, which is flamePos y + 0.85; on pistols also the trigger guard, 0.6 deeper than the frame's nose),
  40 % into that free run (pistols right ahead of the trigger guard), where the gun's underside is flat over the grip's
  whole seat (6 of 7 places along it touching, nothing at its edges reaching into it). The seat is the face that bears:
  the widest part at the grip's top and everything level with it (the RK-1's clamp, not its rail jaws). A light finding
  no underside (the P90's front is its hand loop) sits against the gun's right side near the bore's height, as P90
  lights sit on its side rails. `gripfit.py --check` judges every placed pair on its own (rays through the placed parts,
  not the fit's map): 4 of 7 places along the seat touching (gap at most 0.25), nothing of the gun more than 0.15 into
  the grip, no magazine or pump part in it, or for a light the same against the gun's side; 0 pairs not right.
  fixes/GripPlacement gives each gun its moved copy at startup (Deci.offsetAttachment; 318 copies, 0 not found; once
  at startup: 76 to 212 ms in the dev clients' logs, 96 to 333 ms in the production client's). The Foregrip hangs its 8 parts from a parent "grip" whose own plate sits 7.5 behind them:
  the copies draw that parent without its boxes (Deci.clearAttachmentPartBoxes). The magazine in foregrip.bmodel
  (ammoModel1..4) is never drawn: children of an ammoModel0 the file never declares, which Decimation's BModelLoader
  drops (study.py now does the same).
- Final review (21 Oktober 2026, one reviewer agent): the first gripfit placed by the lowest point of any part touching
  the grip's footprint, a sliver included, and read a slanted part's lowest corner past its end, and its check reused
  the same measure, so it passed by construction ("0 pairs not right") while 37 grips floated (AUG A1 and A3 1.2 to 1.6,
  Model 642, our MAC-10, RPK, FAMAS-I, L85A1, M1 Garand, MGL, G36C). The rewritten check read 42 pairs not right on that
  table (RED), 0 on the new one (GREEN); the reviewer's own march check (scratchpad review/float.py) leaves 4: the P90's
  Laser and Flashlight (on its side by design) and the RPK and RPK-74 Foregrip (resting at its edges on the folded
  bipod legs, 0.8 under the gas tube in the middle). Renders of one gun per class:
  docs/shots/attachments_v0.65.0/gripfit2/sheet_side.png. The RK-1 had hung 0.11 low on every gun (measured from its
  rail jaws, which rise 0.16 above the clamp): now flush (M4A4 dy 0.16 to 0.00; `grips.py --check` 0 unseated, was 1).
- The SpecterDR's switch: fixes/SpecterKey ("DeciRemnant: SpecterDR 1x/4x" among Decimation's keys, default G, free in
  vanilla, Decimation and the voice chat) flips the held gun's mode with a click (fixes/SpecterMode: per gun by
  Decimation's "uniqueID", else by slot, for the session; 4x by default). ScopeZoom: 4x = magnification 4.0, 1x = 1.25
  (magnification names specterdr and specterdr1x: read from deciworldgen_scope.cfg when an entry is there, not written
  to it by default, like eotech558 and ta11acog); our sights never get the black sniper overlay, so the SpecterDR keeps the
  gun and its see through glass at 4x, its reticle drawn in the glass (SightReticle) in both modes.
- Checks: `python3 tools/devtest.py --live attachments` (21 checks: items, loot, recoil, placement, the switch; shots of
  four grips in the Gunsmith screen and the SpecterDR aimed at 1x and 4x), `attachments perf=true` (budgets, below),
  `glstack` (the SpecterDR and the four grips on two guns, hip and aimed, first and third person), `attachmentsmp
  server=HOST:PORT` (a joined client on a dedicated server; run it last, it leaves for the menu),
  `python3 tools/guns/gripfit.py --check`, unit tests GripRecoilTest, GripPlacementTest, SpecterModeTest.
- Grip copies draw from one display list (Deci.BatchedPart, made by GripPlacement for all 318 copies): BModelPart draws
  each part with about seven GL calls (push, move, scale, its lists, pop); the first draw lets each part compile its own
  lists (a list never compiles inside another), the second compiles one list calling them all. Dev test checks: the
  RK-1's copy holds its 56 parts in one list; drawn both ways into a cleared middle of the screen, 0 of 4,637 gun pixels
  differ; GL clean.
- Measured, dev client (scratch checkout; other sessions kept the Mac's load at 5 to 6), `attachments perf=true`. The gun
  drawn once more each frame as Decimation draws it, timed with glFinish around (median of 200), and the part handed to
  the driver (CPU): none 0.723 ms (CPU 0.306), Decimation's Foregrip 0.741 (0.319), RK-1 from one list 0.773 (0.346) =
  +0.050 (budget 0.1; one by one +0.079), Decimation's 4x 0.803, ACOG 0.898, SpecterDR 0.863 (-0.035 against the ACOG,
  +0.003 against the EOTech 0.860). What the RK-1 still costs is mostly its texture switch (the Foregrip pays +0.018 for
  its own). 0 GL errors and no stack change in 1,618 timed draws. Aimed frames, six rounds of 100 frames each in turn,
  looking at the sky (no chunks in view): ACOG 3.35 ms, SpecterDR 4x 3.57, EOTech 3.33, SpecterDR 1x 3.00; the median of
  each round's difference of neighbours: SpecterDR 4x against the ACOG +0.29 ms, 1x against the EOTech +0.11 (budget
  0.5; single rounds swing by up to 2 ms on this loaded Mac). Glass box copied each aimed frame (854 x 480): ACOG 133 x
  133, SpecterDR 4x 225 x 225, EOTech 62 x 48, SpecterDR 1x 70 x 70. SpecterMode.isFour: 0 to 3,224 bytes in 100,000
  calls (a one time cost; it built a string key, 80 bytes a call, before). glstack: 88 gun cases (the SpecterDR and four
  grips on the AKM and M4A4, hip and aimed, first and third person), 0 leaking.
- scopeperf sight=specterdr (dev, the AKM, 4x): aiming median 8.3 ms, slowest 1 % 17.0 ms, against 8.2 / 16.3 over iron
  sights; 59 glass measurements while aiming, 0 at the hip; no GL error at five points of every frame; the glass a plain
  lens again after aiming (100 %). Main's suites after the carry: unit 139, attachments 22, gunsmith 110 (run after
  attachments in one game), playerfire, scopeperf all PASS; prerelease 8a976c8: unit 60, attachments 22, gunsmith 110
  (one earlier run there read "measuring: under a frame" 26.7 ms against 8 right after attachments in the same game,
  M4A4 + 4x, no grip; alone 3.97 ms) [the stall not explained: GC suspected, not verified].
- Production client with OptiFine HD U E7 (one jar of main's tree, sha256 f0afd86c), same Mac: attachments 22 PASS, perf
  7 PASS: gun draw none 0.540 ms (CPU 0.146), Foregrip 0.550, RK-1 from one list 0.622 = +0.083 (one by one +0.104; the
  first production run, before the one list, read +0.127 and failed the budget), 4x 0.573, ACOG 0.658, SpecterDR 0.617
  (-0.041 against the ACOG, -0.001 against the EOTech 0.618); 0 of 1,450 pixels differ between the two ways; 0 GL errors
  in 1,617 draws; aimed at the sky: ACOG 3.76 ms, SpecterDR 4x 3.61, EOTech 3.66, SpecterDR 1x 4.14, paired medians
  -0.07 and +0.21 ms; glstack 88 gun cases, 0 leaking. Dedicated production server, a joined client (attachmentsmp, 12
  PASS): the RK-1 and the SpecterDR fitted by clicks as the server sent them back, recoil 0.333 / 0.333 and 0.500 / 0.250,
  G switches 4x to 1.25 with one click, one real shot 30 to 29 rounds with both fitted.
- Found in the production client (not caused by these parts): the glass box reads 0 x 0 for every see-through sight
  there (the ACOG and the EOTech too), so ScopeZoom never copies the world under the glass: in the SpecterDR's 4x shot
  the glass shows sky and a horizon while the zoomed view around it is stone floor, a picture not taken in that frame
  [inferred: the full frame copied once when the scope texture was made]. Dev reads real boxes on the same Mac. bug.md
  "See-through glass not updated with OptiFine" [cause not found yet].
- Open (21 Oktober 2026): the user has not seen the RK-1 or the SpecterDR in game yet; the RK-1 hangs 40 % of the way
  from the magazine to the muzzle (M4A4 x 12.2, 60 % along the handguard), where the reference build had it at 36 %
  (x 10.7): one number in gripfit.py (FRONT) if the user wants it further back. The Gunsmith's grip line ends on the
  lowest point 60 to 80 % along the gun's outline (Silhouette), so on the UMP45 it ends on the magazine, not on a fitted
  Flashlight. Decimation's Laser and Flashlight are small flat grey boxes that barely show on the Gunsmith's dark
  backdrop. The OptiFine glass finding above (bug.md).
- How the budgets were measured (lessons): at the horizon a 1.25x view draws about 4.5 ms more world than a 3.5x one
  (the SpecterDR at 1x read +3.19 ms against the ACOG: the EOTech at 1.25x costs the same), so each mode is compared
  with our sight of its own zoom; looking down from the arena (150 high) a 1.25x view took in the land below (24 ms
  frames); at the sky the world is out of the way. One reading per configuration was noise (the same path read -0.61
  ms in production): rounds in turn and the median of their paired differences.

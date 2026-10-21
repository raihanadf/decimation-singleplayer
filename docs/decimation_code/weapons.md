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
| fit attachments (the I key) | the Gunsmith screen: fixes/gunsmith/GunsmithGuiHandler.java (Decimation's attachments GUI id, `Deci.attachmentsGuiId`, opens our container and screen), GunsmithContainer, GunsmithScreen, GunParts (`Deci.fitsSlot`: Decimation's own FilteredSlot rules); section 7 |

## 6. Checks

- `python3 tools/devtest.py --live playerfire`: the player's shot end to end (9 checks).
- `gun` (reload loads a magazine, NPC fires), `gunview` (hip, aim, NPC view per gun), `gunperf`, `firerate`,
  `checks` (armor against NPC fire, 60 round mags).
- `python3 tools/devtest.py --live gunsmith` (92 checks: the screen, fits, rules, no loss, the cursor on close and on
  logout, 8 guns at two sizes, every registered gun's layout),
  `gunsmith perf=true` (8 checks, section 7), `gunsmithmp server=HOST:PORT` (a joined client on a dedicated server; run
  it last, it leaves for the menu).

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


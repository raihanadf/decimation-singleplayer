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

## 6. Checks

- `python3 tools/devtest.py --live playerfire`: the player's shot end to end (9 checks).
- `gun` (reload loads a magazine, NPC fires), `gunview` (hip, aim, NPC view per gun), `gunperf`, `firerate`,
  `checks` (armor against NPC fire, 60 round mags).

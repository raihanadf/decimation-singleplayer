---
name: decimation-code-weapons
description: Use when changing or debugging how Decimation guns behave in play: player shots not hitting or doing wrong damage, ammo counts going wrong, reloads, fire modes, recoil, attachments, tracers, gun sounds, armor against gunshots, or when code must read or fire a gun. For modelling a new gun use decimation-gun instead.
---

# Decimation weapons code

Facts and the verified shot order: docs/decimation_code/weapons.md. Making a gun: skill decimation-gun.

## Quick reference

| Task | Where |
|---|---|
| what a shot does, client then server | doc section 3 |
| gun stats, damage chokepoint | `GunItem.setDamage` (deci/ay/i, `am`), `GunStats` (deci/ay/e) |
| read a gun stack | `Deci.gunAmmo`, `Deci.gunDamageOf`, `Deci.gunMagazine`, `Deci.gunIsAutomatic` |
| fire in a test | `Deci.fireHeldGun(player)` on the client thread (the mouse is read from LWJGL: a test cannot press it) |
| hit handling on the server | PacketGunHit handler (deci/aE/a$z) |
| NPC gunfire (not PacketGunHit) | fixes/NpcShots, fixes/ArmorGunfireHandler, the NPC feature |
| fit attachments (the I key): the Gunsmith screen | fixes/gunsmith/GunsmithGuiHandler.java, `Deci.fitsSlot` (Decimation's rules), doc section 7; tests `gunsmith`, `gunsmith perf=true`, `gunsmithmp` |
| grips placed per gun, recoil per grip, the SpecterDR's 1x / 4x | doc section 8; fixes/GripPlacement, fixes/GripRecoil, fixes/SpecterMode, fixes/SpecterKey; tests `attachments`, `attachments perf=true`, `attachmentsmp` |

## Rules

1. The client picks the target and the damage; the server only takes the round and applies it. A damage change
   belongs in `GunItem.setDamage` / our LivingHurtEvent handlers, not in the hit packet.
2. Ammo lives in NBT "ammo" on BOTH copies of the gun: the client takes a round in `fireRay`, the server takes one
   in the hit handler and syncs. Read the server copy for truth.
3. Player gunshots use GunDamageSource "gunDeci" (vanilla armor bypassed, Decimation armor multiplier for slots 0 to
   2); NPC shots use the "human" source. Anything meant for both must handle both sources.
4. Test gun behaviour live: `python3 tools/devtest.py --live playerfire` (player shot), `gun` (reload, NPC fire),
   `firerate`, `gunview`. Trace with `'-Ptrace=deci.ay.i.a,deci.aE.a$z,deci.aE.a$A'`.
5. Shoot infected at night in tests: by day they burn and their health drops by itself.
6. Anything a screen sends through a vanilla packet is checked off the integrated server once: a container button id
   (Container.enchantItem) travels as ONE signed byte (C11PacketEnchantItem), singleplayer passes the int whole (bug.md
   "Gunsmith list clicks lost on a server"; dev test `gunsmithmp` on the production server).

## Common mistakes

- Expecting damage equal to `GunItem.damage` on infected: our infected variants scale it (riot takes 0.5).
- Formatting `Deci.gunDamageOf` with %f: it returns an int (a format error in a server tick crashes the test game).
- Trusting the infected helmet branch of the hit handler: it never runs.
- Changing a gun's sounds or model paths in code that also runs on the server: sounds and renderers are client only.

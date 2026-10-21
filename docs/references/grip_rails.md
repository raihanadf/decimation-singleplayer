# Grips, rails and the RK-1: what the real guns allow (21 Oktober 2026)

User review of the new attachments (21 Oktober 2026): "for RPK and some guns that doesnt have picatiny rail, they dont
support any grip on it", the RK-1 "is not vertical grip, it bit sway to left ... the grip use align 45%" and "its to
big for decimation guns. check it out by yourself". This file holds what was checked. Renders: docs/shots/
grip_rails_v0.65.0 (docs/shots_index.md).

## 1. How a foregrip mounts on a real gun

- A foregrip needs an interface under the handguard: a Picatinny rail (MIL-STD-1913, cross slots), M-LOK or KeyMod
  slots (a short rail section or the grip's own M-LOK base bolts into them), or a gun specific hard point (the G36's
  handguard takes rail sections at 3, 6 and 9 o'clock: HK, hk-usa.com G36C page).
- Wooden or plain polymer handguards (AK-74, AKM, RPK, FAL, G3, M16A1/A2, M14, SKS, the bolt rifles) have none. A
  grip goes on them only after the handguard is swapped for a railed one (aftermarket AK rails exist, e.g. drop in
  Picatinny for AKM receivers): that is a different gun configuration, not the model Decimation draws.
- Rifles with their own folding foregrip (AUG A1) already have a grip; the AUG A3's grip can be swapped for an
  under barrel rail (Wikipedia, Steyr AUG).
- Bipod rails under bolt action and anti materiel rifles (L115A3, SV-98, JNG-90, Barrett) are Picatinny too, but
  they carry a bipod; a fighting foregrip there is unusual [inferred].
- Grip kinds: vertical (straight down, most leverage on recoil), angled (AFG, along the handguard, wrist comfort, less
  recoil control), and grips on angled mounts (a short 45 degree rail or an offset clamp that rolls the grip toward the
  support hand side). Sources: recoilweb.com best AR-15 foregrips; patents US9891020 / US10480896 (ergonomic foregrip);
  BCM Gunfighter Mod 3 mounts forward or back.

## 2. The RK-1 of the user's reference build, measured (docs/shots/refcapture_v0.65.0/my_preset_close)

- The Zenit RK-1 on its B-25U mount is ROLLED sideways about 40 to 45 degrees about the bore, toward the gun's left
  (end_a.png from the muzzle: the clamp sits on the bottom rail, the body runs down and out to one side; bottom.png:
  the grip reaches 231 px sideways from the handguard centre, the handguard is 97 px wide; side.png: it drops 232 px
  below the rail; atan 231/232 = 45 degrees). Seen from the side it looks vertical (its rake along the gun is about 3
  degrees forward), which is why our v0.65 model was built straight down. The side view also shows its end cap as an
  ellipse: the grip leans toward the camera.
- B-25U and RK-1 real sizes: not found on the web (retailers give no numbers; the Tarkov wiki weight is a game value)
  [not verified].

## 3. Our RK-1 against Decimation's own Foregrip (study.py, boxes of the placed parts, model units)

| grip | long | tall | thick | on the M4A4 |
|---|---|---|---|---|
| Decimation Foregrip | 1.0 | 2.7 | 0.8 | y -1.35 .. 1.35 |
| our RK-1 (v0.65) | 1.9 | 3.96 | 1.58 | y -1.51 .. 2.45 = down to the magazine's bottom (2.45) |

The user is right: ours is 1.5 times as tall and twice as thick as Decimation's grip. Decimation draws grips slimmer
than real (its Foregrip body is 0.36 of the M4A4 handguard's height, a real one about 0.7). The RK-1 must be sized to
Decimation's Foregrip, not to the reference's proportions.

## 4. Which guns take a grip today, and which have a rail (Decimation's models, renders rifles_a..d.png)

Decimation itself fits the Foregrip on every rifle (category rule) and lights on every gun; grip_offsets.txt has 103
guns (56 rifles take Foregrip and RK-1). Per rifle, from the side and underside renders and the real gun:

- Rail or slots under the handguard: acr, ak12 [rail under not seen in the renders, real AK-12 not verified], ar15,
  ar15beowulf (M-LOK), aug3, aug3para (rail under the front), fnscar, fnscarhamr, fnscarl, fnscarsc, fnscarssr, g36c
  (side rail drawn; bottom from HK's hard points [not verified on the model]), honeybadger (M-LOK), m110, m4a4, mpt76;
  ours hk416, hk416tan, mk18.
- Bipod rail only (bolt / anti materiel): barrett, jng90, l115a3, sv98.
- No rail: ak74, akm, akms, aks74, aks74u, asval, aug1 (own folding grip), fal, famas, famasi [FAMAS FELIN has side
  rails; the model shows side mounts only], g3a3, g3a4, galil, galilsar, insas, kar98k, kms72, l85a1 (A1, no RIS),
  m14, m16a1, m16a2, m1garand, m231, mosinnagant, parafal, rpk, rpk74, sks, svd, svt40, vz52, vz58, xm177.
- Lights (flashlight, laser) on pistols, SMGs, shotguns, MGs: open question (real lights need a rail too: a Glock 17
  has one, a Makarov or a 1911 does not).

## 5. Open (user decisions, 21 Oktober 2026)

Rule and enforcement place, lights, snipers, guns already holding a grip in saved worlds: asked in chat.

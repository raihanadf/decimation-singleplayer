# Source models of the 3D gun converter

Rule (docs/superpowers/specs/2026-10-22-gun-converter-design.md, "Source models"): the converter only takes models we may
use and ship: CC0, CC BY with the credit kept, or the user's own. Every file gets its line here BEFORE it is used. The
files themselves stay git ignored (build/gunconv/inputs/<name>/, never committed). Models of other games (Escape from
Tarkov and the like) are never taken, from a site or through an API.

A converted gun keeps its source's credit in its gun.json ("source": file, author, url, license). A CC BY source must
also be credited wherever the gun ships (release notes, website credits).

| Name here | Title on the site | Author | Link | License | File used | Downloaded |
|---|---|---|---|---|---|---|
| lamoot_ak47 | High poly AK-47 | Lamoot | https://opengameart.org/content/high-poly-ak-47 | CC0 1.0 (https://creativecommons.org/publicdomain/zero/1.0/) | highpoly_ak47.obj (https://opengameart.org/sites/default/files/highpoly_ak47.obj) | 22 Oktober 2026 |
| toastie_akm | AKM | J-Toastie | https://poly.pizza/m/ZBBoDqAn0A | CC BY 3.0 (https://creativecommons.org/licenses/by/3.0/) | the site's GLB (https://static.poly.pizza/3c910107-0f25-4409-87e2-bbbe27bce413.glb) | 22 Oktober 2026 |
| jtoastie_m590 | Mossberg 590A1 | J-Toastie | https://poly.pizza/m/eAh1oHY32T | CC BY 3.0 (https://creativecommons.org/licenses/by/3.0/) | the site's GLB (https://static.poly.pizza/f6a3852b-57e3-4245-a064-4d7b1e67f87c.glb) | 22 Oktober 2026 |
| pukk_p228 | Rigged Sig p228 | PuKkBuMXDD | https://poly.pizza/m/xce5OmcO43 | CC BY 3.0 (https://creativecommons.org/licenses/by/3.0/) | the site's GLB (https://static.poly.pizza/d6bbd6e3-ab0e-483a-a12a-60ac6c9b207a.glb) | 22 Oktober 2026 |
| kutejnikov_smg | Submachine Gun (Scorpion-inspired) | Kutejnikov | https://opengameart.org/content/skorpion-vz-61 | CC BY 4.0 (https://creativecommons.org/licenses/by/4.0/) | smg_tris.obj from czechoslovak_smg.7z (https://opengameart.org/sites/default/files/czechoslovak_smg.7z) | 22 Oktober 2026 |
| ducky_lightning | Lightning Pump Action Rifle | LonesomeDucky | https://opengameart.org/content/lightning-pump-action-rifle | CC0 1.0 (http://creativecommons.org/publicdomain/zero/1.0/) | lightning.glb from lightning_pump_action_0.zip (https://opengameart.org/sites/default/files/lightning_pump_action_0.zip) | 22 Oktober 2026 |

Used in (22 Oktober 2026, docs/gunconv.md section 5): lamoot_ak47 became package ak47 (the milestone rifle, CC0);
toastie_akm became package akmc (CC BY 3.0: its credit is in its gun.json and must go wherever it ships).

Notes:
- lamoot_ak47: the page lists CC0 and asks no attribution; we credit Lamoot all the same. A comment there calls the
  .blend about 150k polygons; the OBJ (842.6 KB) holds 30,730 triangles (16,113 vertices, one object "Circle.011",
  one material), so it is a lighter mesh than that.
- toastie_akm: the page says "Creative Commons Attribution" with the CC BY 3.0 link, "FBX/GLTF format", rigged. Credit
  line to keep: "AKM by J-Toastie (https://poly.pizza/m/ZBBoDqAn0A), CC BY 3.0".

Milestone 3 (22 Oktober 2026): guns WITHOUT a Decimation twin, of other kinds, for the converter's generality test.
- jtoastie_m590: pump shotgun (Decimation has the Remington 870, not the Mossberg). The page's data says "CC-BY 3.0" and
  "This shotgun comes rigged and ready to be animated!"; 2,138 triangles, 2 materials, joints Root, Trigger and FR.
  Credit line: "Mossberg 590A1 by J-Toastie (https://poly.pizza/m/eAh1oHY32T), CC BY 3.0".
- pukk_p228: pistol. The page's data says "CC-BY 3.0"; the uploader writes "I really love to model pistols xd" (own work
  by their statement [not verified further]); 9,799 triangles, meshes base_gun, slide_gun, barrel, trigger, back_trigger,
  magazine. Credit line: "Rigged Sig p228 by PuKkBuMXDD (https://poly.pizza/m/xce5OmcO43), CC BY 3.0".
- kutejnikov_smg: submachine gun in the vz. 61 Skorpion layout (Decimation has no Skorpion). License line on the page:
  "CC-BY 4.0"; the author says it was on TurboSquid for six years and carries no brand names; the site asked for the
  trademarked name to be dropped from the title, so our package and its display name are generic. 5,758 triangles,
  groups gun, Stock, magazine, bolt, switch, trigger, 3 bullets; no MTL (one colour). Credit line: "Submachine Gun
  (Scorpion-inspired) by Kutejnikov (https://opengameart.org/content/skorpion-vz-61), CC BY 4.0".
- ducky_lightning: pump action rifle with a tube magazine (Colt Lightning style; Decimation has none). CC0; "A low-poly
  model ... the high-poly model I used to bake the textures from" (own work); lightning.glb 3,046 triangles, one mesh
  with joints Root, Pump, Hammer, Trigger, Pin, Lifter. We credit LonesomeDucky all the same.
- Used in (22 Oktober 2026, docs/gunconv.md section 11): jtoastie_m590 became package m590, pukk_p228 package p228,
  kutejnikov_smg package csmg (display "SMG vz61 style"), ducky_lightning package lightning. Their CC BY credits are in
  their gun.json files and must go wherever they ship.
- Looked at and NOT used: austincford's guns on poly.pizza (MPX, MDR, MK14, SR1MP, P226 and more, "CC-BY 3.0" but no
  word on where they come from, and the set matches a game's weapon list closely: the rule says never models of other
  games, so they stay out).

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

Used in (22 Oktober 2026, docs/gunconv.md section 5): lamoot_ak47 became package ak47 (the milestone rifle, CC0);
toastie_akm became package akmc (CC BY 3.0: its credit is in its gun.json and must go wherever it ships).

Notes:
- lamoot_ak47: the page lists CC0 and asks no attribution; we credit Lamoot all the same. A comment there calls the
  .blend about 150k polygons; the OBJ (842.6 KB) holds 30,730 triangles (16,113 vertices, one object "Circle.011",
  one material), so it is a lighter mesh than that.
- toastie_akm: the page says "Creative Commons Attribution" with the CC BY 3.0 link, "FBX/GLTF format", rigged. Credit
  line to keep: "AKM by J-Toastie (https://poly.pizza/m/ZBBoDqAn0A), CC BY 3.0".

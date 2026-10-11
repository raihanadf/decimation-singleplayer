---
name: decimation-city-decay
description: The story layer of the overrun city in our Decimation worldgen - bodies, blood, graffiti, warning decals, trash and looted rooms - placed only where a scene explains them. Use when adding or reviewing bodies, decals, graffiti, trash or debris anywhere in a city.
---

# City decay and story layer

Facts: docs/city_dressing.md section 4, research topic "Abandoned / evacuated city" (debris is caused,
never random; at most one hero scene per chunk; bodies only in a scene that explains them).
Code: BuildingDress.story, StreetDress (trash clusters, wasteland skeletons, crash debris).

## Rules
- Trash bags never alone: 2..5 against a wall, a bin or a lamp foot.
- A skeleton against a wall gets blood on the wall above it (whole 2 x 2 splat on solid wall).
- Body bags in medical, police and military buildings (rows, same orientation), or by a crash.
- Graffiti on inner walls of homes, industry and civic buildings; warning decals by their hazard
  (biohazard in medical / lab, chemical storage in industry, watch your step / slippery floor).
- Never decal.notegeneric or decal.imagegeneric (see decimation-city-interior).

- Outer walls: graffiti low on blank walls of the ground storey (BuildingDress.exterior), shop signs
  over doors / windows of shops and food places.
- Lots: a survivor's spot round a fire can, a dumping ground; parks: a body now and then.

## Open
Looted spills around open containers, overgrowth from tree pits: not done yet.

## Casebook (never delete a case)
### Case 1: first story layer (v0.50.0, 12 Oktober 2026)
- Skeletons with blood, body bags, trash clusters, graffiti and warning decals; pictures in
  docs/shots/city_dressing_v0.50/.

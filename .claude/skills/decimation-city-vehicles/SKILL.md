---
name: decimation-city-vehicles
description: Place wrecked vehicles and road scenes in the city of our Decimation worldgen - parked and crashed cars and trucks, police roadblocks, military checkpoints, road works with cone tapers - with headlights the right way and every drawn box clear. Use when adding or reviewing anything standing in a city road.
---

# Vehicles and road scenes

Facts: docs/city_dressing.md section 2 (road), research topic "Abandoned / evacuated city",
docs/references/prop_facing.tsv (cars, trucks, police cars: headlights W N E S at meta 2..5; long
axis x at 2 / 4), docs/prop_placement.md (a car is drawn 3.85 x 6.9). Code: StreetDress
roadScene / roadblock / checkpoint / crash / roadWorks, StreetPlan.carFits (the part's own wreck).

## Rules
- A car in its lane points its headlights the way the lane runs (right hand traffic):
  PropFacing.meta(car, laneDirection). A car across the road: PropFacing.along(car, acrossAxis).
- One scene at most per chunk, none where the district part already has a scene; most chunks quiet.
- A DC street part's road is 6 wide: one car only, centred (lane 7 / 8); test the part's blocks under
  the whole drawn box (carFits). Long props are checked 5 cells around (Dress).
- Scenes explain themselves: debris lies downstream of the crash in the travel direction; bodies only
  in the wasteland or with a crash; a roadblock closes the road except one lane.
- Wasteland checkpoint: jersey barriers (step 2 along their long axis) with a 2 wide gap,
  hedgehogs before it, sandbags at the ends.

## Check
Same as decimation-city-street (servertest dress + worlddump + propclash diff; cityprops shots with
kinds policecar, sawhorse, cone, roadworks, checkpoint, truck, wreck).

## Lessons
1. Two cars in lanes 6 and 9 of a 6 wide road overlap each other and the raised sidewalks: one car,
   centred, fit-tested (v0.49.0).
2. A base prop far from the new one can still overlap it (a car reaches 3.4 from its block): check
   the plan's props 5 around, not only the neighbouring cells (v0.50.0).

## Casebook (never delete a case)
### Case 1: road scenes (v0.50.0, 12 Oktober 2026)
- Roadblocks (police car across, sawhorses, cones), wasteland checkpoints, crashes with T-bone and
  debris, road works with the sign trailer; seed 1: 0 propclash findings from them.
- Evidence: docs/shots/city_dressing_v0.50/cell132_*.png.

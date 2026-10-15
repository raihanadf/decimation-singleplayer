# Decimation code: vehicles (Hummer, ATV, Buggy, M35, BTR-70)

Part of the feature study of Decimation's own code (16 Oktober 2026, index docs/decimation_code/README.md). Car wrecks
in cities are props (blocks), not vehicles: docs/prop_placement.md, skill decimation-city-vehicles.

## 1. What it is for

Drivable vehicles come only from spawner items (vehicleSpawnerHummer, ...ATV, ...Buggy, ...M35, ...BTR70: loot and
traders), placed by the player. A vehicle has seats the player rides, runs on fuel from a jerry can in the hotbar,
takes damage and explodes, and despawns when nobody has driven it for a while.

## 2. Code map

| Class (obf) | Job |
|---|---|
| VehicleEntity (deci/ad/e) | the body. `onUpdate` drives ON THE SERVER (the client only smooths toward the server's position): driver seat's yaw sets the heading, the rider's `moveForward` (from the client's input packet) adds or takes 0.02 speed a tick up to max speed (reverse to a third), `playerHasFuel` (a jerry can with fuel in hotbar slots 0 to 8, creative always), `consumeFuel` (h: 10 % of ticks while moving), `setEngineOn`; out of fuel: speed drains 0.05 a tick while speed / 10 is ADDED to the motion, then it stops dead. `attackEntityFrom`: a rider cannot hurt it; any player hit on an EMPTY vehicle (or a creative player's) picks it up (dies, drops its spawner item), gunshots ("gunDeci") excluded; otherwise damage += amount / 10, smoke over 10, fire over 40, explodes over max health. Despawn: dies 600 s after its last driver (`dateCreated`, wall clock, refreshed while a player drives; NOT saved, so a world load starts it over), while `getDespawnSecondsLeft` (dU) counts from 900 for the HUD |
| VehiclePartEntity (deci/ad/b) | child entity at an offset, follows the body |
| VehicleSeatEntity (deci/ad/c) | rideable seat: `isDriver` (dG), protect, hide player, overlay, forced perspective |
| TurretSeatEntity (deci/ad/d) | BTR-70 gunner seat, fires TankShellEntity |
| VehicleEmitterEntity (deci/ad/a) | exhaust particles while driven |
| HummerEntity, ATVEntity, BuggyEntity, M35Entity, BTR70Entity (deci/ad/i, f, h, j, g) | kinds: seats, sizes, speeds, drop item; M35 marks itself placed by player (only plays the reverse beep) |
| HummerItem and the other spawner items | place a vehicle |
| VehicleIdleSound, VehicleThrottleSound (deci/ad/l, m) | engine sound loops (client) |
| PacketVehicleInput (deci/aE/a$r) / PacketVehicleYawClient (deci/aE/a$s) | driver yaw to the server, then to everyone |
| VehicleSeatDamageGuard (common handler) | protected seats take no damage |

## 3. One drive, as it runs (dev test `drive`, call logger, 16 Oktober 2026)

A Hummer in the arena, the player in its driver seat (survival, a jerry can in slot 1), the client holding forward
through a MovementInput that always pushes (so its input packet carries it):

- server: `VehicleEntity.onUpdate` 108 times and `playerHasFuel` 90 times (twice a tick: engine flag and the drive
  branch) over the run; seats tick on both sides (`VehicleSeatEntity.onUpdate` 199 server, 192 client); the client
  asks the seat for `isDriver`, overlay, hidden player and forced perspective every frame;
- measured: 12.9 to 14.8 blocks in 40 ticks, top speed 0.68 to 0.76; fuel 3 to 4 units in 40 ticks (10 % of ticks);
  with the can gone: engine off, coasts 12.8 blocks, then speed 0 and standing; the HUD countdown starts at 900 s.

## 4. Singleplayer state and known faults

- Driving, fuel and riding work in singleplayer (checked by `drive`).
- A survival punch on an empty vehicle used to pick it up at once: fixes/VehicleHitHandler (only a sneaking punch
  picks it up) and tools/patches/PatchVehicleArrows.java (arrows, thrown items, player TNT take the health path).
- The HUD says 15 minutes, the vehicle goes at 10; the timer restarts on every world load [from the code].
- NoDropsHandler (no item drops from a destroyed vehicle) is server proxy only [inferred: drops in singleplayer].
- A player's gunshot damages a vehicle only when the shooter rides it (PacketGunHit handler, weapons.md).

## 5. How our mod uses it

| Need | Use |
|---|---|
| spawn | `Deci.newHummer(world, x, y, z)` |
| is it a vehicle (body or part) | `Deci.isVehicle` |
| drive state | `Deci.vehicleDriverSeat`, `Deci.vehicleSpeed`, `Deci.vehicleEngineOn`, `Deci.vehicleDespawnSecondsLeft` |
| fuel | `Deci.jerryCan()`, `Deci.itemAmount(stack)` |
| hits | fixes/VehicleHitHandler, PatchVehicleArrows |

## 6. Checks

- `python3 tools/devtest.py --live drive`: seat, engine, driving, fuel use, engine off, coast and stop (9 checks).
- `python3 tools/devtest.py --live checks`: "vehicle punch", "vehicle arrow".

---
name: decimation-code-vehicles
description: Use when changing or debugging Decimation's drivable vehicles (Hummer, ATV, Buggy, M35, BTR-70): driving, fuel and jerry cans, seats and riders, vehicle damage, explosions, being picked up by a punch, despawning, or when code must spawn, ride or read a vehicle. City car wrecks are props: use decimation-city-vehicles for those.
---

# Decimation vehicle code

Facts and the measured drive: docs/decimation_code/vehicles.md.

## Quick reference

| Task | Where |
|---|---|
| driving, fuel, damage, despawn | `VehicleEntity` (deci/ad/e) `onUpdate` and `attackEntityFrom` (doc section 2) |
| seats | `VehicleSeatEntity` (deci/ad/c), `Deci.vehicleDriverSeat` |
| spawn and read | `Deci.newHummer`, `Deci.isVehicle`, `Deci.vehicleSpeed`, `Deci.vehicleEngineOn` |
| fuel | `Deci.jerryCan()`, `Deci.itemAmount` |
| punch / arrow pickup | fixes/VehicleHitHandler, tools/patches/PatchVehicleArrows.java |

## Rules

1. Driving is server side; the client only smooths. Read speed and position on the server.
2. The driver's forward comes from the CLIENT's input packet: a test drives by replacing the client player's
   MovementInput (see DriveTest), not by setting `moveForward` on the server (the next packet overwrites it).
3. Seats exist only after the vehicle's first server tick: wait for `Deci.vehicleDriverSeat` before mounting.
4. Test live: `python3 tools/devtest.py --live drive` and `checks`. Trace prefixes use OBFUSCATED method names
   (`deci.ad.e.h` = consumeFuel; deobf/decimation.srg), else they match nothing.
5. A vehicle test in survival needs fuel in the hotbar; creative drives without using any.

## Common mistakes

- Expecting a vehicle to stop when the fuel runs out: it coasts more than 10 blocks first.
- Trusting the HUD countdown: the vehicle goes at 600 s idle, not 900, and a world load resets the clock.
- Testing a hit with a gun: gunshots never pick up or damage an empty vehicle; fists and arrows do.

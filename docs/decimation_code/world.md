# Decimation code: weather, day length and other world features

Part of the feature study of Decimation's own code (16 Oktober 2026, index docs/decimation_code/README.md).
Decimation never generates terrain or structures: all world generation is ours (docs/worldgen.md, docs/terrain.md).
Supply drops: loot.md. Zones: docs/zones.md.

## 1. What it is for

Decimation's own world rules are few: a foggy "default" weather and a darker "storm" weather with their own fog and
ambient sounds, chosen by the server and sent to every client; a day that always restarts at morning time 0; the
supply drop timer.

## 2. Code map

| Class (obf) | Job |
|---|---|
| WeatherType (deci/aI/a) | the weather list: `byId` (av), `nextWeather` (gS), per weather `getName`, `fogType` ("misty"), `fogDensity`, `ambientSound`, `ambientSoundIndoors`; empty `onStart` / `onTick` / `onEnd` hooks |
| WeatherDefault (deci/aI/b), WeatherStorm (deci/aI/c) | fog 0.008 and "weather.default.ambient"; fog 0.045 and "weather.storm.ambient". BOTH are built with id 0 |
| WeatherController (world) | dedicated server only (ServerTickHandler, ServerConfig.enableDynamicWeather): every 18000 ticks the next weather, sent as PacketSetWeather |
| ClientState.currentFogDensity | the fog the client draws: the current weather's `fogDensity` |
| DayLengthHandler (deci/an/p) | common handler: at the start of every world tick, world time above 24000 goes back to 0 |
| SupplyDropSpawner (deci/aM/c) | supply drops from the admin's list (dedicated server; ours in singleplayer: loot.md) |

## 3. As it runs (call logger, dev test `trade` as a carrier, 16 Oktober 2026)

Traced `deci.aI,deci.an.p,deci.b.i.a`: client thread WeatherType.byId 112 (once a client tick), ClientState
.currentFogDensity 168 and WeatherDefault.fogDensity 168 (once a frame; WeatherStorm never); DayLengthHandler
.onWorldTick 671 (every world tick, every dimension).

## 4. Known faults

- The storm can never show [from the code, matching the trace]: both weathers have id 0, so `byId` finds the
  default first for any id it is given (an unknown id falls back to the first entry too), and the client's own tick
  sets `byId(1)` every tick anyway, overwriting what PacketSetWeather set.
- No dynamic weather in singleplayer at all (WeatherController is server proxy only); nothing lost given the above.
- The world's day count never grows: world time goes back to 0 after 24000, so the moon phase (computed from the day)
  stays the same night after night and `/time set` past 24000 is undone [inferred].
- OUR fixes/WorldClock counts days as world time / 24000 + 1, so with this reset its day stays 1, and at each
  midnight it sees the time go back to 0 and treats it as "time set back": the DAY event never fires after the
  first day (DUSK, NIGHT, DAWN still do) [inferred from both codes, bug.md "WorldClock day never advances"].

## 5. How our mod uses it

| Need | Use |
|---|---|
| days and time of day for our systems | fixes/WorldClock (broken by the reset: bug.md "WorldClock day never advances") |
| infected stronger at night | fixes/InfectedVariants (night frenzy) |
| world type, terrain, structures | our worldgen (docs/terrain.md, docs/worldgen.md) |

## 6. Checks

- No dev test drives the weather (there is nothing to drive in singleplayer); the trace above is the run time
  evidence. Night frenzy: `zombies`.

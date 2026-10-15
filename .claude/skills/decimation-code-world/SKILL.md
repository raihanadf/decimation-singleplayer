---
name: decimation-code-world
description: Use when touching Decimation's own world rules: weather and fog (default, storm), ambient weather sounds, day length and world time, moon phase, day counting, or when our features need time of day or days survived.
---

# Decimation weather and world rules code

Facts and the trace: docs/decimation_code/world.md. Decimation generates no terrain; worldgen is ours.

## Quick reference

| Task | Where |
|---|---|
| weather list, fog | WeatherType (deci/aI/a), WeatherDefault / WeatherStorm (deci/aI/b, c) |
| fog drawn | ClientState.currentFogDensity |
| world time reset | DayLengthHandler (deci/an/p) |
| days for our systems | fixes/WorldClock |

## Rules

1. Do not build on Decimation's weather: the storm never shows (both weathers have id 0, the client resets to
   `byId(1)` each tick). A weather feature of ours needs its own state and its own fog hook.
2. World time never passes 24000: never count days with `getWorldTime() / 24000` (fixes/WorldClock still does:
   bug.md "WorldClock day never advances"); keep an own counter that advances when the time wraps to 0.
3. Test anything time based at a set time (`setWorldTime`) inside the test; the reset happens every world tick.

## Common mistakes

- Reading `getTotalWorldTime` and `getWorldTime` as the same: only world time is reset; total world time grows.
- Expecting the moon phase to change: it is computed from world time, which never passes one day.

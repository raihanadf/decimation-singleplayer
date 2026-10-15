# Call logger (dev client only)

Built 15 Oktober 2026 (v0.62.6) after the user asked for it ("yes build the call logger"). It records which
methods of Decimation and of our mod run, in which order and on which thread, while an action happens in the
game. Use it to find the code behind a behaviour (the chain behind NPC tracers took a decompile read; the logger
shows it in one run), to name unnamed methods by when they run, and to see which handlers never fire in
singleplayer. Never in the release: the agent is a JVM flag of the dev client, the shipped jar has only the inert
command and bridge.

## How it works

| Part | Where | Job |
|---|---|---|
| agent | tools/trace/src/decitrace/agent/TraceAgent.java -> build/trace/decitrace-agent.jar | Java agent (`-javaagent`): every class of the marked packages gets `Trace.enter(id)` at the start of each method (ASM 5, on the final bytes after Forge's transformers; no frames recomputed). Default packages `deci/`, `net/decimation/`, `com/boehmod/` |
| recorder | tools/trace/src/decitrace/Trace.java -> build/trace/decitrace-boot.jar | on the boot class path (agent manifest `Boot-Class-Path`), so every class loader sees one copy; one volatile read per call while off; while on, records time, thread and method for methods under the chosen prefixes; back to back repeats on one thread are counted, not listed; 200 000 events max, then calls are only counted |
| bridge | dev/src/main/java/net/decimation/worldgen/devtest/CallTrace.java | our mod's side (reflection: works only when the agent is loaded) |
| namer | tools/trace/tracelog.py | adds readable names from deobf/decimation.srg, writes `<trace>.named.txt`, prints most called and first events |

Names in a trace are dev names: Decimation's obfuscated classes and methods (`deci/aE/a$B$a.a(...)`), MCP names
for Minecraft. The namer turns them into `NetworkMessages$PacketGunFireEffects$PacketGunFireEffectsHandler.handle`
where deobf/decimation.srg has a name (all classes, every method except compiler made lambdas and accessors since 16 Oktober 2026).

## Use

| What | Command |
|---|---|
| build the agent (once, and after editing tools/trace/src) | `python3 tools/trace/build.py` |
| live test run, recorded | `python3 tools/devtest.py --live MODE '-Ptrace=deci.aE.a$B,deci.n.d$a,net.decimation.fixes.NpcShots.apply'` (starts the game with the agent if none runs; quote `$`) |
| start the live game marking only some packages (cheaper) | `python3 tools/devtest.py --live --trace=deci/aE,deci/n MODE ...` |
| by hand in the dev client | `./gradlew runClient -Ptrace` (or `-Ptrace=deci/aE`), then in game `/deciworldgen trace start deci.aE,deci.n 10` (stops by itself after 10 s) or `... start PREFIXES` and `/deciworldgen trace stop` |
| name a trace | `python3 tools/trace/tracelog.py dev/run/client/trace/trace_<time>.txt` (devtest.py does it by itself) |

Prefixes take dots or slashes and may go down to a method (`net.decimation.fixes.NpcShots.apply`). A live run's
`trace=` lasts for that run only (not a lasting key). Traces land in dev/run/client/trace/ (git ignored).

## Traps

- A live game that is already running keeps how it was started: a game without the agent answers "no call
  logger"; a game started with `--trace=PACKAGES` marks only those (a later `-Ptrace=` for other packages finds
  nothing). `--stop` and start again.
- Decimation methods in a prefix go by their OBFUSCATED name (`deci.ad.e.h` for VehicleEntity.consumeFuel): look it up
  in deobf/decimation.srg (`MD: deci/ad/e/h (...) decimation/vehicle/VehicleEntity/consumeFuel`). A readable name
  only matches where the jar itself is readable (overrides like `onUpdate`, or methods such as `playerHasFuel`); a
  prefix that matches nothing traces nothing and says nothing (16 Oktober 2026, the drive test).
- Do not start a second game by hand beside devtest.py (both take the debug port 5005 and the live port; the
  second one dies with "bind failed").
- Broad prefixes fill the event list fast: `deci.n,com.boehmod,deci.aE` over a 20 s tracer run made 2.75 million
  calls, the list was full after 200 000 events (render methods). Trace narrow: a package, a class, a method.
- A hot prefix slows the game enough to break timing checks: the `zones` test traced over the zone package recorded
  11.7 million calls in 24 s and failed water refill and bleeding, which pass without the trace. A check that fails
  only under the trace is the trace's cost: rerun it without, and trace narrower (a class, not its per zone loop).
- Frame rate: do not run fps tests (cityfps, gunperf, scope, props) in a game started with the agent. Measured
  15 Oktober 2026, cityfps in the live game, sequences of runs: without the agent 40, 30, 31, 28 and 19, 28, 29;
  with all packages marked 28, 26 and 30, 28, 20, 19; with deci/aE, deci/n marked 20, 18, 18. The numbers drift
  run to run by 10 fps either way, so the cost is not measured `[not verified]`; a cost is likely
  `[inferred]` (a call added to every method also makes small methods too big to inline).

## Checked

- Offline (scratchpad, `-Xverify:all`): constructor, static method, lambda, second thread; repeats counted.
- Live tracer run (15 Oktober 2026), prefixes `deci.aE.a$B, deci.n.d$a, NpcShots.apply, Deci.sendNpcShot`: 7045
  methods marked, 18 traced, 1484 calls; the trace shows the whole chain: server thread NpcShots.apply,
  Deci.sendNpcShot, PacketGunFireEffects.<init> / toBytes; client thread fromBytes, the handler's onMessage and
  handle, BulletTracer.<init>, then BulletTracer.tick and render each frame. The tracer test passed beside it.
- `--trace=deci/aE,deci/n`: only those packages marked (861 methods), the trace held only deci/aE and deci/n.

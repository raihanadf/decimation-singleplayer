# Performance: how to check it, what we found (10 Oktober 2026)

User report: "really big performance issue now ... 1 fps after i drop it [a gun] to the
ground ... check other variables that maybe can make the performance degrade". Measured, not
guessed; every number below comes from the tools in section 1.

## 1. Tools

| Tool | What it tells |
|---|---|
| `python3 tools/devtest.py --live gunperf [guns=uzi,ump45,mac10,ump9] [attach=...]` | fps per gun: nothing in view (baseline), the gun dropped 2.5 blocks in front of the camera (EntityItem, IItemRenderer ENTITY), held; Decimation guns are the control; uncapped frame rate |
| `python3 tools/devtest.py --live census [span=60] [kill=Infected,Hulk,...]` | loaded entities by registry name, tile entities, loaded chunks, fps every 10 s, start vs end (growth check: PASS under 100 in the span); `kill=` removes those entities first (cleaning a test world) |
| `python3 tools/perfcheck.py world WORLD` | offline scan of the save: entities by id, most crowded chunks, chunks in the wrong region slot, entities stored outside their chunk, tile entities by id |
| `python3 tools/perfcheck.py log LOG` | most repeated log lines (a stack trace printed every tick is a frame killer) |
| `python3 tools/perfcheck.py fixchunk WORLD CX CZ` | clears one chunk's region slot (regenerated from the seed); copy the world first |
| JFR on the live game | `~/.jdks/zulu-8.jdk/Contents/Home/bin/jcmd <pid> JFR.start name=x settings=profile duration=30s filename=F.jfr`, then `~/.jdks/zulu-25.jdk/Contents/Home/bin/jfr print --events jdk.ExecutionSample --stack-depth 200 --json F.jfr` and count top frames / callers per thread ("Client thread", "Server thread"). JFR records 64 frames: deep recursion (pathfinding) cuts the bottom of the stacks. `jfr print` itself keeps only 5 frames unless given --stack-depth (a 20 Oktober analysis missed its callers that way) |
| `python3 tools/servertest.py 1 type=decimation pregen=x,z,r [jfr] [bulklight=false]` | world generation on the dev server, fresh world: PREGEN lines with our population and structure write time (wall and CPU), saving time, queued block ticks by block, chunk median / slowest 1 % / ten slowest with their sector, and the light check (cells breaking vanilla's light rule); `jfr` records dev/run/server/profile.jfr |
| `python3 tools/servertest.py 1 type=decimation walk=x,z,steps,view [bulklight=false]` | chunks loaded as a player walking east loads them (a new row of 2 view + 1 chunks a step): the stall per chunk crossed (WALK line: median, 90 %, worst, steps over 250 ms, each step with C for a city row) |
| `python3 tools/genprof.py profile.jfr [FROM TO]` | the server thread's generation time by part (light, saving, vanilla terrain, decoration, our classes) and light by who set the block |
| `python3 tools/devtest.py genwalk [-Pbulklight=false]` | the player flies 30 chunks east over never generated city land in a fresh seed 1 world (two runs fly the same path): integrated server tick times (median, slowest 1 %, worst, over 100 / 250 ms, the slow ticks with their place), client frame times, noon and midnight shots of the new land. Live: `--live genwalk light=old|new` (new land each flight, so flights do not compare) |
| `python3 tools/devtest.py refugevisit [-Pwarmup=false]` | first sight of a city refuge's 22 residents in a fresh seed 1 world, two refuges a game (section 10): the keeper's fill per resident kind, server tick to tick times, client frames, per frame what the first frames after the arrival loaded (textures, looks, model parts, layer files), process and graphics memory; check no Decimation texture loaded at arrival |

## 2. What it was (dev world, deciworldgen_autotest)

1. **Entity explosion through a corrupt chunk** (the 1 fps). Region slot (-1,-6) held the data
   of chunk (-4,-1) (xPos / zPos of the other chunk). The game then had two chunk objects
   claiming (-4,-1); entities of both were saved into (-4,-1) and came back as new entities on
   every load, while Forge printed "Wrong location!" plus a full stack trace for each of them
   every tick (43 000 traces in one session). The save grew from about 9 800 entities to
   40 730 (31 010 Hulks, 8 263 Infected, 1 252 Bloaters) in one 15 minute live session; 32 965
   of them stored in (-4,-1) with positions outside it. Five server crashes on 10-09 04:12
   (AnvilChunkLoader.loadEntities -> addTileEntity, index -20: a tile entity outside its chunk)
   show the slot was already broken then; what broke it is not known `[not verified]` (a kill
   during a save is the usual cause).
   Fix: `perfcheck fixchunk` on (-1,-6) and (-4,-1) (backup first), `census kill=...` removed
   the remaining 8 121 piled up infected; afterwards about 200 entities, no growth in 60 s, no
   "Wrong location" lines.
2. **The ground under the test arena is a mob farm**: the 97 x 49 stone floor at y 150 keeps the
   ground below permanently dark, next to spawn (always loaded); infected spawn there and walk
   around under the player (about 50 infected and 15 infected dogs come back within a minute).
   Harmless now that they are cheap (item 3), but they are why a dev world always has infected
   near the player.
3. **Decimation's infected path search every tick** (real game too): InfectedEntity
   .onLivingUpdate (deci.ag.d) called updateWanderPath (a full path search) every tick for every
   infected on the server AND on the client copies, and scanned a 40 x 40 box for other infected
   every tick (n x n). JFR: about 60 % of the client thread and of the server thread in
   pathfinding; the client even searched the server's chunks. Removing the ~90 infected near the
   arena took the fps from 21 to 51. Fixed by tools/patches/PatchInfectedAI.java (v0.38.1):
   wander search server side once a second per infected (staggered by id), horde scan every 10
   ticks with the result kept. With the same 53 infected alive: baseline fps 21 -> 35, client
   pathfinding 60 % -> 0 %, server 60 % -> 20 %; the zombies dev test still passes (variants,
   runner speed, riot armour, screamer, night frenzy).
4. **Guns are not the cause**: gunperf in the clean world, fps dropped / held vs baseline: Uzi
   22 / 19, UMP45 23 / 18, MAC-10 22 / 20, UMP9 22 / 20 (baseline 21; after the patch baseline
   35, Uzi 34 / 34, MAC-10 35 / 35). Our guns cost what Decimation's cost.

## 3. Other things that cost frame time (checked, not changed)

- 10 694 PropTile tile entities in the dev world: they do NOT tick (TileEntityProp extends
  StaticTileEntity deci.Z.a, canUpdate() false; an earlier note here guessed otherwise, checked
  10 Oktober 2026). Their cost is drawing only (section 5).
- 784 loaded chunks in the dev world (spawn chunks + view distance): vanilla random block ticks
  (WorldServer.func_147456_g) are now the top server item.
- Prop drawing in prop heavy views: bug.md "FPS drop in prop-dense areas" (open, item 1 of the
  roadmap's open bugs).

## 4. Checklist when the user reports lag

1. `perfcheck log` on the session log: anything printed thousands of times?
2. `perfcheck world` on the save: entity totals, a chunk with hundreds of entities, chunks in
   the wrong slot, entities outside their chunk.
3. `census` in the live game: growth over 60 s.
4. JFR 30 s on the live game: top frames and callers per thread.
5. `gunperf` (or a mode for the suspected item) against Decimation's own as the control.
6. Fix, then measure the same way again; write the numbers here.

## 5. Props (user, 10 Oktober 2026: "the performance really fuck up when props so many")

Measured with dev test props (225 props packed in front of the camera; now with clear weather
every run: rain particles cost frame time and made runs differ) and JFR:
- Java side is small: PropRenderer (deci.I.l) 1.4 % of client samples. The client thread
  waits in the driver: native samples 70 % in glCallLists (terrain lists, where the driver
  stalls on a busy GPU) and 12 % in the props' own part display lists (BModelPart deci.n.b).
- Isolation in one session (fps empty platform -> 225 props in view): Decimation's renderer
  48 -> 29 and 31 -> 24; transforms and texture bind without the model 33 -> 33. The cost is
  drawing the models (geometry through the props' display lists), not the per prop setup,
  not the line of sight checks, not ticking.
- Tried and dropped: one flat display list per prop type (BModel parts recorded in immediate
  mode): slower, 26 / 23 -> 16 / 17 fps in A/B runs (two each). Per draw call overhead is not
  the bottleneck on this Mac; removed again (not shipped).
- Earlier (10-09): prop render distance 32 / 48 in a seed 1 city street gave no gain, props were
  5.5 % of that frame. So props hurt in dense spots (packed props, tower chunks with 500+),
  not in an ordinary street.
- The fps of the empty platform falls from run to run in one live session (48 -> about 33)
  with steady entity counts: probably the machine heating up after GPU heavy runs
  `[inferred]`; compare only within one run.
The user's lag spot (screenshots 10 Oktober 2026, F3): 70 to 90 fps away from the city, 40 to
55 looking over the seed 1 city (render distance 12; C 823 vs 1 023 chunk sections drawn).
Dev test `cityview` (props drawn vs their renderer swapped for one that draws nothing,
alternating three times so the drift cancels; -Pview=x,y,z,yaw,pitch).
- First views were WRONG: from the arena (y 151) the floor or the cloud layer (y 128) filled
  the picture, no city in it (props on = off there). Always look at the cityview screenshot.
- Valid view x 3, y 110, z 40, south, 30 down (under the clouds, past the arena's south edge,
  the city fills the picture, 4 651 props in the render chunks): props on / off 38 / 50,
  31 / 38, 32 / 35: props cost about 18 % (34 vs 41 fps). The rest of the drop from the 82 fps
  outside the city is the city's blocks (more chunk sections) and the Mac heating up.
- Prop render distance by size (PatchPropCulling step 4, deciworldgen_props.cfg) large 48 /
  medium 32 / small 24: props on / off 45 / 42, 39 / 37, 39 / 39: the props' cost gone, no
  visible difference in the screenshot (far props are a few pixels). New defaults (v0.38.2);
  64 = vanilla. An older note said 32 / 48 gave nothing in a street: that view had few props.
- Angelica (GTNH rendering overhaul, dev only: `tools/devtest.py --live --angelica`,
  `gradlew runClient -Pangelica -PforceEnableMixins=true`): needs a newer fastutil than the
  copy Decimation bundles, so the dev run uses libs/Decimation-base-nofastutil.jar
  (`zip -d` of it/unimi/dsi/*; no class in the jar references fastutil). It starts (20 mods),
  but on this Mac (OpenGL 2.1 context) it does NOT draw the far terrain: the city view is
  empty sky at 700 fps, so its numbers mean nothing; our MAC-10 also drew slowly under it
  (held 60 fps vs the Uzi's 703). Not usable here `[inferred: the GL 2.1 context]`; Prism keeps
  OptiFine. "Entity is already tracked" server errors appeared in that session only (later in nearly every dev log; cause found 17 Oktober 2026: Decimation's mob swaps during chunk loads added the new mob twice, fixed v0.64.2, bug.md).
- Java 25 + lwjgl3ify (`tools/devtest.py --live --java25`, task runClient25; every runClient*
  task now gets the dev hooks): the log still says "OpenGL: Apple M4 GL version 2.1 Metal"
  with LWJGL 3.4.2. macOS gives programs that use the old fixed function GL (all of 1.7.10)
  only a 2.1 context, whatever the LWJGL; a modern core context drops what Minecraft and
  Decimation draw with. The run also crashed on the same fastutil clash (GTNHLib comes with
  lwjgl3ify): fixable, but pointless while the context stays 2.1. Prism runs the same Java 8 /
  LWJGL 2 on the same Mac, so Angelica there is expected to show the same empty terrain
  `[inferred]` (and it would replace OptiFine, which works).
- Other options left: render distance 8 to 10 in cities, OptiFine in the dev client, fewer
  hidden faces in our buildings.

## 6. Props used as building material (11 Oktober 2026, v0.42.1)

User on the v0.42.0 military bases: BlockMilitaryBarrier "really hurt fps lots cause its prop".
Every BlockProp is a tile entity whose model the TileEntitySpecialRenderer draws each frame; the
bases built their HESCO walls from it (about 900 / 1600 / 2300 cells per outpost / FOB / large
FOB). Replaced by deciworldgen:hesco, a plain full block in the chunk mesh: tile entity props per
base 1020 / 2007 / 2902 -> 129 / 381 / 571 (counted by tools/props/propclash.py on the dumps).
The frame rate itself was not measured [not verified]. Rule: anything placed by the hundreds is
a plain block, never a prop (docs/prop_placement.md).

## 7. FPS falling over a session, a frozen Mac (19 Oktober 2026)

User after a 20 minute two player session (brother's laptop hosting, the Mac joining): "decimation really often dropping
fps from example 120 fps on fresh spawn and minute by minute its drop hard to 60 or even worse", the Mac froze. Full
story with numbers: bug.md "FPS falls minute by minute" and "OpenGL Stack overflow".

New tools:

| Tool | What it tells |
|---|---|
| `python3 tools/devtest.py --live threads` | CPU of every thread over 5 s (ThreadMXBean); FAIL when a helper thread (not the client or server thread) uses half a core or more. A thread that never sleeps heats a laptop, which then slows down after minutes |
| `python3 tools/devtest.py --live soak [minutes=10] [speed=6] [still=true]` | every 10 s: fps, heap after the last collection, threads, client / server entities, tile entities, particles, loaded chunks, while the player flies on into new land (or stays); take `jcmd <pid> GC.class_histogram` at the start and the end and diff them for a leak |
| `python3 tools/devtest.py --live radios` | 30 radio props: playing prop loops near, 80 blocks away, back again; fps each time |
| `python3 tools/devtest.py --live glstack [glonly=a,b] [glall=true] [glprobe=true]` | GL stack depths after each frame while every item with its own renderer is held (first / third person), guns with each sight hip / aimed, another player with fancy nameplates off, a frame that leaks on purpose; glprobe wraps every item renderer and names calls that change the modelview depth |
| `python3 tools/glstackcheck.py JAR:PREFIX ...` | from the bytecode, every method whose return paths leave a GL matrix / attribute push open (branches, switches, exceptions; helpers like pushScissor followed); path blind, confirm in the game |

Found and fixed (all in the dev workspace, three jars for the patches):
1. Decimation's boot intro thread (GuiIntroJumpscare, deci.i.d.aS) busy waited at 99% of a core for the whole session
   (`threads`). tools/patches/PatchIntroThread.java. PatchSwing's own note ("idle main menu 200% CPU -> 105%") had the
   100% of this thread left in it.
2. Prop sound loops (radios streamed) ran until their chunk unloaded: 26 of 30 radios still playing 80 blocks away,
   fps 74 -> 57 with them (`radios`). fixes/PropSounds: out of hearing (20 blocks) stopped, 6 at most.
3. Decimation's melee / throwable item renderers leaked a matrix level per first person call (after every switch away
   from one) and its name tag handler one per other player a frame with fancy nameplates off; the 32 level stack filled,
   then every push failed. fixes/GlStackGuard (wrappers, a pop after the name tag, and a per frame guard).
4. A dead BoehMod version fetch every 60 s (thread, TLS, stack trace). tools/patches/PatchNoVersionFetch.java.
5. Not a leak: 10 minutes of `soak` kept the heap after collection at 230 -> 256 MB and threads at 27 to 29; the class
   histogram grew 16 MB (vanilla structure data). JFR while flying: client thread mostly vanilla chunk meshing (about 70%),
   server thread world generation; the frame rate falls by place there, not by time.

Traps: a server player's PlayerTickEvent runs only when that client's movement packet is processed (do per player
server work on the server tick); the dev world's frame rate swings 58 to 74 for the same empty scene (mob farm under the
arena, section 2 item 2): compare counts (loops, threads, stack depth), not single fps readings.

## 8. NPCs and zombies under load: the bench (19 Oktober 2026)

User: "do benchmark ... when they standby, shooting, wars and test out on zombie too. make it really really stable even
there so many entities". Dev test `bench` (scenarios on the arena at night, each run twice with mob spawning off and
everything else cleared within 96 blocks, 3 s to settle, 8 s measured): standby (40 idle armed NPCs), shooting (20
soldiers, infected keep coming), war (20 soldiers against 20 bandits), zombies (80 infected around a survival player who
takes no damage), crowd (all of it, about 290 entities on the server). Per scenario: server mean and worst ms a tick,
client median and slowest 1 % frame, entity counts, wall clock window (`-Ponly=war,zombies`, `-Prepeat`, `-Pmeasure`).
Cut a JFR recording of the run into scenarios with the windows (scratchpad scripts of this session: benchjfr.py,
benchcall.py: top methods, and per sample the mod frame that called the costly vanilla method).

Found and fixed (bug.md "Safezones lag", "Infected searched a wander path no one walked", "Blood splats on the ground
moved for 40 seconds", "GunNoise ..."): target searches every tick with ray traces before the hostility check (traders,
guards, bandits, soldiers, mech), vanilla target tasks at targetChance 0, the unused infected wander path search, blood
splats moving while lying on the ground, GunNoise writing to every mob per burst. Related dev tests: traderperf
(`npc=TraderGuard|Bandit`: cost of 20 near, and they still shoot an infected), targetpace, zoneperf (inside a test
safezone vs away; run refuge or camp first).

Results on the M4 (controlled bench, two runs each; server mean ms a tick, worst ms, client slowest 1 % ms):

| scenario | before (patches of this day up to the trader and bandit fixes) | after (all) |
|---|---|---|
| standby | 1.9 / 2.5, worst 14 / 9, slowest frames 81 / 52 | 2.8 / 2.1, worst 37 / 27, 71 / 108 |
| shooting | 3.3 / 3.9, worst 10 / 95, 42 / 34 | 4.2 / 2.7, worst 35 / 19, 67 / 36 |
| war | 4.1 / 3.0, worst 68 / 92, 36 / 32 | 3.8 / 2.4, worst 25 / 11, 103 / 35 |
| zombies | 2.4 / 2.6, worst 10 / 10, 35 / 43 | 3.3 / 2.1, worst 34 / 6, 53 / 32 |
| crowd | 8.9 / 19.4, worst 118 / 259, 408 / 678 | 7.4 / 6.8, worst 85 / 186, 101 / 98 |

Before the trader and bandit fixes (first, uncontrolled bench): standby client median 45 ms, shooting server 11.6 ms.
The server keeps 20 ticks a second in every scenario. A later run while the Mac was busy (load 7.8: IntelliJ, browser)
gave client medians 22 to 68 ms with the same code: compare client numbers only on a quiet machine.

Scenario `loaded` (30 civilians, about 1250 entities, items 40 to 90 blocks away): every human NPC walked the whole loaded
entity list each tick (PatchHumanScare): server 11.1 / 8.5 ms a tick with the original class, 7.4 / 8.0 patched; the
rest there is vanilla item and chunk work.

Still costly (bug.md "Drawing many human NPCs is heavy on the client", open): each human NPC drawn in seven texture
layers plus armour, vest, mask, backpack and the gun part by part; terrain display lists dominate the client's native
time on the sky arena. Vanilla random block ticks and entity collisions in a dense crowd are vanilla's.

NPC drawing measured on its own (dev test `npcdraw`, 19 Oktober 2026): the bench's client frame times cannot show a
change in how NPCs are drawn (Mac load 4 to 14 during the runs, frames capped near 60 fps in some scenes). npcdraw turns
the frame cap and vsync off and sums, per frame, the time from RenderLivingEvent.Pre to Post of every human NPC, drawn
the new way and the old way in turns of 30 frames (HumanLayers.enabled off and sightChecks on is Decimation's drawing
exactly), so load changes hit both alike. Looks in one picture (fixes/HumanLayers):

| scene, NPCs drawn a frame | old way | new way |
|---|---|---|
| horde, 80 infected | 6.55 ms (82 us each) | 4.72 ms (59 us each) |
| camp, 70 civilians and guards with guns and gear | 31.2 ms (446 us each) | 29.6 ms (423 us each) |

An armed NPC spent about 360 us in its gun and gear (BModelPart display lists part by part, every quad its own draw).
Model parts in one draw each (fixes/ModelBatch, PatchModelBatch), old way now Decimation's whole drawing (layers off,
sight checks on, ModelBatch off), same alternation, Mac load about 5:

| scene, NPCs drawn a frame | old way | new way | frame median old / new |
|---|---|---|---|
| camp, 46 armed civilians and guards | 12.3 ms (274 us each) | 6.0 ms (130 us each) | 28.3 / 16.9 ms |
| horde, 82 infected | 8.4 ms (102 us each) | 4.7 ms (56 us each) | 16.5 / 12.9 ms |
| fight, 40 soldiers and bandits shooting (healed each tick) | 35.9 ms (897 us each) | 19.3 ms (483 us each) | 50.0 / 32.4 ms |

`-Pscenes=fight` also splits draws by the gun's muzzle flash: 735 us flashing vs 686 us not (new way), on 8 % of the
draws: the flash is about 1 % of NPC drawing. The bench's `layers=off` switches all three back for whole scene runs.

## 9. World generation stalls (20 Oktober 2026)

User: "when new generated building or chunk its bit laggy, idk its from the city it self or from safezone (i encounter 1
safezone city) or from world generated it self"; the host of the three player session logged "Can't keep up" 2.5 to
2.8 s behind while new city areas generated (bug.md "Server stalls while new city areas generate").

Where the time went (pregen of 225 chunks of a seed 1 city, JFR, tools/genprof.py): light 39 % (82 % of it from our
structure writes), saving 24 %, vanilla terrain 22 %, Highways' link cache 5 to 8 %, vanilla decoration 4 %.
- Structure writes: World.setBlock lights every block (a light check at the block, and a relight of the column and its
  neighbours when the column's height changes). Slices writes column by column and bottom up, so each new column moved
  the open side of the half built rooms one column on: their light was taken away and put back with every column (93 %
  of a structure write was light). worldgen/BlockBatch writes a window without per block light and lights it once,
  on the final blocks, with vanilla's own World.updateLightByType (ring first, so outside cells lit through what became
  a wall go dark before the rooms take light from them). Next to chunks that do not exist yet vanilla's update does
  nothing, as it did block by block.
- Saving: vanilla decoration queued a block tick for every gravel and sand block it placed (gravel veins, discs at
  water): 188,721 after 225 chunks, and WorldServer.getPendingBlockUpdates walks the whole queue for every saved chunk.
  DeciBiome.decorate runs the updates at once (scheduledUpdatesAreImmediate, as vanilla places springs).
- Highways: the link cache's Long keys hashed to rx ^ rz (bins turned into trees). worldgen/Keys.pair spreads them.
  Other caches with that key shape (CityLayout, LcCity cells, CityRivers, Wasteland, CityNames, BiomeMap) did not show
  in the profile; switch them to Keys.pair when one does. With the keys fixed the cache still cost 5 to 8 % in the live
  flights: Highways.sides asked xHighway / zHighway (25 cached links each) for every block column of the window and its
  four neighbours, though the answer depends on the chunk only; it now asks once a chunk (same answers, 0 blocks
  differ).
- BlockBatch itself in the live flights: the ring took the whole window's height range for every ring column, and every
  written cell whose opacity changed got a vanilla block light check though almost none has block light near it. Now
  a ring column takes the heights of the written columns beside it, and a cell is checked only if it or a neighbour
  holds block light from before the batch (cells whose own light changed, lamps, are always checked).

Light check (DevPregen.lightCheck, the gate for BlockBatch): every cell from y 30 to 160 of the asked chunks against
vanilla's own rule (World.computeLightValue: sky 15 at or above the height map, an opaque block 0, else the brightest
neighbour minus the cell's opacity, at least 1; block light the same from the block's own light). Vanilla block by block
already leaves some cells wrong; the batch must never leave more.

| measure (back to back runs, Mac load about 3.4) | original | after |
|---|---|---|
| pregen 225 chunks: loading (server thread CPU) | 3.9 s (3.7 s) | 2.0 s (1.9 s) |
| structure writes, CPU | 2.47 s | 0.83 s |
| saving | 1.64 s (188,721 queued ticks) | 0.11 s (3,556) |
| chunk median / slowest 1 % / slowest | 6.7 / 114 / 300 ms | 5.5 / 40 / 107 ms |
| light wrong, sky / block (5.67 M cells) | 6,714 / 15,423 | 2,197 / 8,620 |
| walk 40 steps, view 10, per step median / 90 % / worst | 222 / 435 / 466 ms | 99 to 107 / 160 to 168 / 209 ms |
| walk steps over 250 ms | 15 | 0 |
| refuge area 656,-880,5: structure writes CPU (batch off / on) | 0.46 s | 0.27 s |
| dev client, genwalk same path (fresh seed 1 world, view 12; batch off / on): slowest 1 % / worst tick | 304 / 690 ms | 188 / 403 ms |
| dev client, genwalk: ticks over 250 ms / over 100 ms | 14 / 26 | 4 / 22 |
| production client, release jar d6aea397, same path (batch on only): slowest 1 % / worst / over 250 ms | | 149 / 289 ms / 3 |

Light of the same spot both ways (genwalk shots at noon and midnight, docs/shots/worldgen_stalls_v0.64.8): no visible
difference. The client's frames did not change (median 18.9 / 18.5 ms, slowest 1 % 34 / 35 ms).

The walk's "original" is the code before these fixes (fix 1 and the Highways changes stashed, batch off); with only
the batch off (fix 1 and Highways in) the same walk gave 184 to 188 / 404 to 407 / 438 to 541 ms and 11 to 12 steps over
250 ms (two back to back pairs, Mac load about 3.5): the batch alone halves the stall per city row.
Blocks are the same (worlddiff over the area: 0 differences, except 4 daylight sensors whose power is read from light).

In the dev client (genwalk, view 12, 25 chunks a row): a slow server tick each time the player crosses into a new chunk
(x steps of 16), when vanilla's PlayerManager loads the whole new row in that one tick. JFR of two live flights: chunk
loading 66 % of the server thread, vanilla's chunk ticks 25 %: 80 % of those are Chunk.func_150809_p retried EVERY tick
for chunks at the edge of the loaded square (a new chunk's light is filled once every chunk within 17 blocks exists;
until then each tick tries again, column by column), about 20 % of the server's time while flying. That retry and the
whole row at once are vanilla's; changing either needs a patch of Minecraft's own classes (no coremod here).
The refuge area is no slower to generate than other city land; its 22 residents are runtime work (their AI was
measured and cut on 19 Oktober, section 8), not generation. Left per sample after the fixes: vanilla terrain 36 %, light 29 % (our batch relight about 13 %, vanilla's
own light of each new chunk about 10 %), vanilla decoration 14 %.


## 10. First sight of NPCs: a city refuge's residents (20 Oktober 2026)

The refuge's first visit (bug.md "First sight of armed NPCs stalls a frame"). Dev test `refugevisit` (one launch, fresh
seed 1 world, two refuges in one game): finds a refuge the generator plans on land never made (LcCity.refuge, east from
x 4096), holds the keeper (SafezoneSites.hold) while the land around a player 40 blocks south of its middle (y 100) is
made and drawn, 5 s of baseline, then lets the keeper fill it. Reports: the fill per resident kind (SafezoneSites
lastFill*, also in the log line "filled: N residents spawned in X ms"), server tick to tick times, client frames, the
first 12 frames after the arrival with what each loaded for the first time (TextureManager textures, HumanLayers looks,
old way draws and layer files, ModelBatch parts and capture time) and the textures' names, who compiled parts
(ModelBatch.callers), the warm up's state, and the process and graphics memory (`vmmap -summary`: Physical footprint,
IOAccelerator). Check "no Decimation texture loaded at arrival". Switches for comparisons (dev client): `-Pwarmup=false`,
`-Pmodelbatch=false`, `-Phumanlayers=false`.

| first frame with the 22 residents | dev client | production client (one jar) |
|---|---|---|
| no warm up, first refuge of the game | 150 to 630 ms (11 runs) | 941 ms |
| no warm up, second refuge | 120 to 635 ms | 122 ms |
| fixes/ModelWarmup, first refuge | 77 to 114 ms | 80 ms |
| fixes/ModelWarmup, second refuge | 36 to 96 ms | 48 ms |

- Server: the fill is 3 to 10 ms. The 150 ms server ticks near it are vanilla's chunk light population, about 80 % of the
  server thread's samples the whole time while standing at the edge of new land (section 9).
- What the first frame did without the warm up: 38 to 49 textures loaded (armour skins, masks, backpacks, guns, bullets,
  human layer files for NPCs drawn the old way), 750 to 1200 model parts compiled, gear models loaded by ModelHuman, 4
  HumanLayers pictures (5 to 20 ms with their 15 to 20 layer files). JFR is no help inside such a frame: the client
  thread is mostly in GL native code and got 6 samples in 200 ms; per frame counters found it.
- Not a cause: ModelBatch. Its capture of 900 parts took 2 to 3 ms, and the arrival with ModelBatch off was no better
  (279 ms).
- Compiling every model ahead (tried, dropped): 140 gear models and 25,628 parts in 1.5 to 2.9 s brought the first frame
  to 38 to 46 ms, but the graphics memory went from 1.2 to 2.6 GB and the footprint from 4.0 to 5.8 GB: Decimation's
  display lists (one draw per quad) cost about 9 KB of driver memory a quad on the M4 [inferred: 1.4 GB / 160k quads],
  and ModelBatch keeps them beside its own list. Every model a session draws costs that, warm up or not; freeing
  Decimation's lists after ModelBatch's capture would save memory, but BModelPart.renderWithRotation still calls them
  [idea, not built].
- Left: about 1,100 part compiles in the first frame when a whole refuge appears at once (the test's worst case); in play
  residents come within the client's 80 block entity range one by one [inferred].

## 11. Scope aim delay (21 Oktober 2026)

- Report (user): "the scope aiming still need more optimizing cause sometime it has delay on it. due to loading the
  scope render". Suspects before measuring: the glass texture allocated on the first aim (glCopyTexImage2D), the
  reticle texture's first bind, the sight model's display list compiled on its first draw, ScopeZoom's learning hold.
- Probe: dev test `aimdelay` (devtest/AimDelayTest; `pairs=gun:sight,...`): per pair a first aim (ScopeZoom's learned
  sights forgotten), a second aim, and an aim after a relaunch (kept sights read back); per aim the time until the zoom
  is 95 % in, the worst frame in the first second and the frames over 50 ms.
- Found: no stall (dev worst 27 ms, production with OptiFine 37 ms, none over 50 ms): nothing loads late. The delay is
  the learning hold: magnified sights took about twice as long on the first aim of every launch (dev ACOG 431 / 187 ms,
  SpecterDR 382 / 200, 2x 339 / 183; production 375 / 193, 392 / 193, 351 / 178; 1.25x sights 166 both, under the hold).
  At lower frame rates it is longer (10 samples every 2nd frame, at most 1.5 s).
- Fix: fixes/SightMemory (bug.md "Scope aim sometimes slow to zoom in"): after a relaunch every pair zooms like a learned
  sight (dev 166 to 193 ms).

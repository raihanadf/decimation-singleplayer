# Decimation code: rendering and models

Part of the feature study of Decimation's own code (16 Oktober 2026, index docs/decimation_code/README.md). The gun
model and animation formats and the gun renderer in detail: docs/gun_model_spec.md; prop drawing: docs/prop_placement.md;
measured costs and fixes: docs/performance.md.

## 1. What it is for

Everything Decimation draws beyond vanilla: guns in first and third person with animations and attachments, the
players' stances, gear and gestures, 96 prop renderers, mob and vehicle renderers, particles, and per frame world
layers (tracers, laser dots, loot hints, turf names, icons, debug boxes).

## 2. Code map

| Class (obf) | Job |
|---|---|
| BModelLoader (deci/n/g), BModel (deci/n/f), BModelPart (deci/n/b), BModelQuad (deci/n/c) | the plain text .bmodel format (docs/gun_model_spec.md) |
| GunItemRenderer (deci/K/b) | IItemRenderer of every gun: first person, third person (`renderEquippedThirdPerson`), attachments (`renderAttachments`), muzzle flash, smoke puffs; holds the playing GunAnimation |
| GunAnimation (deci/F/a) | .anib animations; frames can send reload steps |
| PlayerModelSwapper (deci/J/e) | swaps the player model each RenderPlayerEvent.Pre (`applyStancePose`): stances, gestures, extra armor |
| StanceEntityRenderer (deci/E/a) | the camera height while crouched or prone (swapped in by ClientRenderHandler) |
| ClientRenderHandler (deci/c/b) | render tick work (`onRenderTickStart`, `onRenderTick`), name tags; the old picture in picture scope (replaced by our ScopeZoom) |
| WorldRenderLayerDispatcher (deci/c/d) | runs the 13 world layers (deci/M): each layer's `render` once a frame and its `renderEntity` once per loaded entity per frame |
| layers (deci/M/c to o) | GrenadeTimerLayer, LaserDotLayer, BlockTipLayer, BulletTracerLayer, NpcIconLayer, ClanTurfLayer, WorldIconLayer, VehicleSpawnLayer, and the debug or empty ones (VehiclePartDebugLayer, VehicleDebugLayer, ZoneDebugLayer, EmptyRenderLayer, UnusedModelLayer) |
| prop renderers (deci/I), models (deci/p, q, r, s, t, u, w) | one TESR per prop tile entity with a Techne model; mobs, vehicles, items |
| DeciParticleSpawner (deci/B/d) | custom particles by name (blood, smoke, embers, flashes), also sent from the server |

## 3. As it runs (dev test `gunview` with an AKM, call logger, 16 Oktober 2026)

Traced `deci.M,deci.c.d,deci.K.b,deci.F.a,deci.J.e,deci.c.b` over 88 rendered frames: every one of the 13 layers'
`renderEntity` 4447 times (88 frames x about 50 entities) and `render` 88 times; ClientRenderHandler.onRenderTickStart
264; PlayerModelSwapper.applyStancePose 132; GunItemRenderer.handleRenderType 128 and tickSmokePuffs 99;
GunAnimation.get 99 (a lookup each frame while the gun is held).

## 4. Known costs and faults

- The layers run per loaded entity per frame, the empty and debug ones too: in a city with hundreds of entities that
  is hundreds of thousands of mostly empty calls a second [cost not profiled]; skipping the empty layers would be a
  cheap patch.
- Fixed by us before: the picture in picture scope rendered the world twice (ScopeZoom, PatchScope), the sway
  thread spun a core (PatchSwing), props drew at any distance and without line of sight caching (PatchPropCulling),
  the scope glass measured NPC guns (ScopeZoom, v0.43.1). Details: docs/performance.md.
- Never run fps tests in a game started with the call logger (docs/call_trace.md).

## 5. How our mod uses it

| Need | Use |
|---|---|
| our guns and sights drawn like Decimation's | tools/guns (gunmodel.py), fixes/NewGuns, fixes/NewSights, fixes/SightPlacement, fixes/SightReticle, `Deci.offsetAttachment` (skill decimation-gun) |
| scopes | fixes/ScopeZoom |
| prop render distance and culling | tools/patches/PatchPropCulling.java, config deciworldgen_props.cfg |
| NPC tracers | fixes/NpcShots, PatchTracer |

## 6. Checks

- `gunview` (guns in hand, aimed, on an NPC), `scope`, `props` (prop fps), `cityfps` / `cityview` (fps: never with the
  call logger), `views` (street shots).

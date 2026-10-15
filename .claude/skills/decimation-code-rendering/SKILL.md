---
name: decimation-code-rendering
description: Use when changing or debugging how Decimation draws things - guns in hand and their animations and attachments, player stances and gear, prop and mob renderers, particles, the per frame world layers (tracers, laser dots, loot hints, turf names, icons) - or when chasing low frame rates. For making a gun model use decimation-gun; for placing props use decimation-props.
---

# Decimation rendering code

Facts and the traced frame: docs/decimation_code/rendering.md. Formats: docs/gun_model_spec.md. Costs:
docs/performance.md.

## Quick reference

| Task | Where |
|---|---|
| gun drawing | GunItemRenderer (deci/K/b), GunAnimation (deci/F/a), BModelLoader (deci/n/g) |
| player model | PlayerModelSwapper (deci/J/e), StanceEntityRenderer (deci/E/a) |
| per frame world layers | WorldRenderLayerDispatcher (deci/c/d), layers in deci/M |
| props | the TESRs in deci/I, PatchPropCulling |

## Rules

1. Profile before optimising (docs/performance.md: JFR on the live game); guesses were wrong twice.
2. Never judge fps in a game started with the call logger; trace render code only for call counts and order.
3. Anything added to a layer's `renderEntity` runs per entity per frame: return at once when there is nothing to do.
4. Test visually with the dev tests that take screenshots (`gunview`, `views`, `scope`) and read the sheet.

## Common mistakes

- Reading the decompiled scope code as current: our ScopeZoom and PatchScope replaced its picture in picture view.
- Trusting the notes' class names over names.tsv: some were renamed since (tools/skillcheck.py checks the docs).

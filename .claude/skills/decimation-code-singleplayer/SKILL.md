---
name: decimation-code-singleplayer
description: Use when a Decimation feature works on a dedicated server but not in singleplayer (or the reverse), when adding or changing an event handler, packet or command that must run in singleplayer, when something "never fires", or when deciding on which side and thread code runs (ClientProxy, ServerProxy, @SideOnly, isServer).
---

# Decimation sides, proxies and singleplayer gaps

Facts: docs/decimation_code/singleplayer.md (every handler, where registered, whether it runs in singleplayer, ours
in its place; the 75 network messages by receiving side).

## Quick reference

| Question | Answer |
|---|---|
| does this Decimation handler run in singleplayer? | the table in the doc, section 3 |
| why not? | registered only in ServerProxy (deci/a/e), or its event method is `@SideOnly(SERVER)` (stripped in the client JVM), or an `isServer()` branch |
| the fix pattern | a handler of ours on both sides that steps aside when `isServer()` is true: fixes/BottlecapHandler, fixes/ZoneRules, fixes/SupplyDropScheduler |
| which side receives a packet | its `Side` in NetworkMessageRegistry (deci/aD/n) |
| server proxy in our code | `getServerProxy` returns null in singleplayer |

## Rules

1. Look first, then fix: check the doc's table, then prove it at run time with the call logger:
   `python3 tools/devtest.py --live MODE '-Ptrace=deci.aK,deci.an'` lists which handlers fired.
2. A copy of a server only rule must step aside on a dedicated server, so the rule never runs twice there.
3. In singleplayer the integrated server and the client share one JVM but not one thread: code reached from a client
   tick runs on the client thread (Decimation's server task queue does, loot.md section 4). Touch the server world
   from the server thread.
4. Check both paths after a change: the live dev client (singleplayer) and `python3 tools/servertest.py 1`
   (dedicated server, where ServerProxy handlers do run). A client only type in `Deci` broke the dedicated server
   once (CLAUDE.md v0.44.0).

## Common mistakes

- Registering a Decimation handler in our mod and expecting it to run: its `@SideOnly(SERVER)` method is gone in the
  client JVM.
- Testing only in singleplayer: a fix that does not step aside doubles the rule on a server.
- Assuming /zone, /turf, /trader, /clan commands exist in singleplayer: only `/deci` is registered there.

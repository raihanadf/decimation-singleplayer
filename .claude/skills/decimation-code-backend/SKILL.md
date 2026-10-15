---
name: decimation-code-backend
description: Use when touching anything Decimation does over the network outside the game - the dead BoehMod backend (kryonet, network.mcdecimation.net), reconnect attempts, threads piling up, launch delays, update checks, server lists, news, capes, skins or images fetched from the web, Discord presence - or when deciding what the mod may contact.
---

# Decimation backend and outside connections code

Facts, the hosts, and the measured thread leak: docs/decimation_code/backend.md.

## Quick reference

| Task | Where |
|---|---|
| backend connect | BackendClient (deci/aP/a) `connect`, called from DecimationMod.postInit |
| reconnect every 15 s | ClientEventHandler (client), ServerTickHandler (dedicated server) |
| launch wait fix | tools/patches/PatchBackend.java |
| update check | UpdateChecker (net/decimation/mod/common/utils/h), our fixes/Updater |

## Rules

1. Treat every BoehMod host as untrusted: the domains are dead and may be re-registered by anyone. Never add code
   that talks to them; prefer cutting existing calls.
2. Measure threads, not guesses: `jcmd <pid> Thread.print` and count `kryonet.Client.run`.
3. Any change to Decimation's classes here is a jar patch into three jars (CLAUDE.md), from the original classes.

## Common mistakes

- Thinking PatchBackend stopped the backend: it only shortened the connect timeout; reconnects still run every 15 s.
- Reading "Failed to Connect to Network!" as harmless: each try leaks a thread.

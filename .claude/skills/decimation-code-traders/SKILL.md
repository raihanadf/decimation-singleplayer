---
name: decimation-code-traders
description: Use when changing or debugging Decimation's traders: buying and selling for bottlecaps or gold caps, prices and offers, the banker, bounty board, guards and armed traders, traders missing or not respawning, or when code must spawn a trader, read its offers or trade.
---

# Decimation trader code

Facts and the checked trade: docs/decimation_code/traders.md. Traders in our camps and refuges: docs/safezone.md.

## Quick reference

| Task | Where |
|---|---|
| offers of a trader kind | its class constructor (deci/ai/b..x) via `TraderEntity.addOffer`; read with `Deci.traderOffers` |
| what a trade checks | PacketTraderTransaction handler (deci/aE/a$M), doc section 2 |
| trade from a test | `Deci.sendTrade` on the client |
| spawn one | `EntityList.createEntityByName("TraderFood", world)` (registry names Trader...) |
| keep them in place | worldgen/SafezoneSites keeper |

## Rules

1. Prices are checked against the trader CLASS's offer list: changing a price means changing the offers, not the
   packet.
2. A bought item arrives as `new ItemStack(item)`: anything needing metadata, a count or NBT must be handled after
   the trade (or sold differently).
3. Singleplayer has no trader respawn: whatever places traders must also keep them (our keeper does).
4. Test live: `python3 tools/devtest.py --live trade`; trace with `'-Ptrace=deci.aE.a$M,deci.ai.a'`.

## Common mistakes

- Reading `oFFERS_BY_CLASS` before any trader of that kind was constructed: the list exists only after one was.
- Counting offers right after a client copy spawned: each construction replaces the list, then refills it.
- Hurting a trader in a test: TraderAndSafezoneAttackGuard cancels every attack on traders.

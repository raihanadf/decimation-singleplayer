# Decimation code: traders (offers, trades, banker, guards)

Part of the feature study of Decimation's own code (16 Oktober 2026, index docs/decimation_code/README.md). Our camps
and refuges with traders and guards: docs/safezone.md.

## 1. What it is for

Traders are standing NPCs that buy and sell for bottlecaps (or gold caps): food, guns, ammunition, armor,
medical, backpacks, fortifications, drugs, black market, scrap, skins, car dealer, plus the banker (storage), the
bounty board, guards and seasonal ones (Santa, witch, guitar player).

## 2. Code map

| Class (obf) | Job |
|---|---|
| TraderEntity (deci/ai/a) | base: a FactionHumanEntity that never moves (speed 0, follow range 0), health 100, name tag always shown, never attackable (TraderAndSafezoneAttackGuard). Its constructor puts a NEW offer list for its class into the static `oFFERS_BY_CLASS` (abd), then the subclass adds its offers: `addOffer(item, price)` = buy price, sell price price / 4; `addOffer(stack, buy, sell)`; `addOfferWithCurrency(item, buy, sell, 1)` for gold caps |
| TraderOffer (deci/ai/y) | itemStack, buyPrice (abi), sellPrice (abj), currencyType (hW: 0 caps, 1 gold caps) |
| FoodTrader, GunsTrader, AmmunitionTrader, ArmorTrader, MedicalTrader, ... (deci/ai/b to x) | one class per trader kind, registry names TraderFood, TraderGuns, ... |
| ArmedTraderEntity (deci/ai/v) | traders that shoot infected and bandits within 32 blocks (guns, ammunition, armor, medical, drugs, black market, heavy guns, guard) |
| GuardTrader (deci/ai/n) | armed guard NPC, registry name TraderGuard |
| BankerTrader (deci/ai/e) | opens the banker storage (GUI_BANKER; 24 stacks kept in PlayerData) |
| BountyTrader (deci/ai/g) | place bounties on players (PacketPlaceBounty) |
| Quest (deci/ai/z) | quest with tasks and reward: no caller found, unused |
| PacketTraderTransaction (deci/aE/a$M) | client to server: action (0 buy, 1 sell, 2 ...), item, price, trader id, currency. Handler: the trader must exist; buy: enough caps and `isValidOffer` (item, price and currency match an offer of that trader's class) -> `addItemStackToInventory(new ItemStack(item))` and caps taken; sell: an undamaged item in the inventory and a valid sell offer -> one item removed, caps paid |
| TraderSpawnManager (net/decimation/mod/server/traders/a) | dedicated server only: removes every trader each 3000 ticks and respawns them from decimation_traders.json |

## 3. A trade, as it runs (dev test `trade`, call logger, 16 Oktober 2026)

A FoodTrader in the arena (29 offers), the player with 1046 caps; picked Canned Green Apples (buy 10, sell 2):
buy 1046 -> 1036 caps and +1 item; a buy at price 9 refused (nothing changes); sale 1036 -> 1038 and -1 item.
Trace `deci.aE.a$M,deci.ai.a,deci.ai.k`: TraderEntity.addOffer 58 = 29 offers x 2 constructions (the server's trader
and the client's copy share the static map in one JVM), onLivingUpdate 854, the transaction handler reading the
packet's fields on the server.

## 4. Singleplayer state and known faults

- Trading works in singleplayer (checked by `trade`).
- No trader respawns in singleplayer (TraderSpawnManager is server only): traders exist only where something spawns
  them; our worldgen/SafezoneSites keeper fills camps and refuges and puts traders and guards back.
- A buy gives `new ItemStack(item)`: count 1, metadata 0, no NBT, whatever the offer's stack holds [from the code].
- Each new trader object (client copies included) replaces its class's offer list, then refills it. In
  singleplayer the client builds its copy on the client thread while the server may be checking a trade: a valid
  trade could be refused in that moment [inferred, rare race, never seen].
- Quests are not used.

## 5. How our mod uses it

| Need | Use |
|---|---|
| is it a trader / armed trader | `Deci.isTrader`, `Deci.isArmedTrader` |
| a trader's offers | `Deci.traderOffers(trader)` |
| trade like the trader screen | `Deci.sendTrade(action, item, price, traderId, currency)` (client) |
| prices by day (feature 11) | fixes/TraderMarket rewrites the class offer lists (`Deci.traderOfferLists`, `Deci.newTraderOffer`), sees trades in Decimation's channel (`Deci.decimationChannel`, `Deci.tradeMessage`); test `market` |
| traders in our camps and refuges | worldgen/SafezoneSites (EntityList names "TraderGuns", "TraderGuard", ...), docs/safezone.md |

## 6. Checks

- `python3 tools/devtest.py --live trade`: buy, wrong price refused, sale (6 checks).
- `camp`, `refuge`: traders and guards placed and kept at their posts.

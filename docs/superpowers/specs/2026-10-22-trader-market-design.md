# Trader market by day (feature 11; docs/ideas.md 39)

User, 22 Oktober 2026: "trader now have dynamic market supply and demand, its randomize by day in minecraft"; chosen:
daily random drift plus player trades (selling a lot lowers a price, buying a lot raises it, both ease back over days),
each trader a few items on sale and a few in demand per day.

## Facts (docs/decimation_code/traders.md)
Each trader class holds one static offer list (TraderOffer deci/ai/y: item, buyPrice abi, sellPrice abj, currency); a new
trader object refills it; the server's transaction handler (PacketTraderTransaction deci/aE/a$M) accepts a trade only
when item, price and currency match an offer (isValidOffer). On a dedicated server the client builds its own offers: a
price we change on the server must reach the client or the client's screen shows old prices and its trades are refused.

## Design
- fixes/TraderMarket, a WorldSavedData of the world: per item a pressure (+ bought, - sold, in units of one trade),
  easing 30 % toward 0 each day; a daily seed from WorldClock.day() and the world seed.
- The day's price of an offer = Decimation's price x drift (random per item and day, 0.85 .. 1.15) x pressure factor
  (1 + 0.03 a unit, clamped 0.6 .. 1.6), rounded to a whole cap, never below 1; sell price never above the buy price.
- Per trader kind and day: 3 items "on sale" (buy price x 0.7) and 3 "in demand" (sell price x 1.5), drawn from its own
  offers; shown in the trader screen with a small tag.
- Applied by rewriting the offer list's prices at dawn and after each trade (server), and sent to clients on our channel
  (a price table per trader kind, only on change and when a trader screen opens). Decimation's isValidOffer then checks
  the new prices on both sides.
- Bottlecaps in and out stay Decimation's own transaction; only prices move.

## Performance
Work only at dawn (one pass over offer lists, about 500 offers) and per trade; a price table packet per screen open or
change (under 2 KB); nothing per tick.

## Tests
Unit: drift range and determinism by day, pressure and easing, clamps, sale and demand picks. Dev test `market`: day 1
prices, buy 10 of one item and see its price rise and a sale lower another, next day's ease back, the on sale and in
demand tags, a trade at the new price accepted and one at the old refused; channel check on the dedicated server
(production): the joined client sees the server's prices and trades work.

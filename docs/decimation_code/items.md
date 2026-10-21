# Decimation code: items and gear (armor, extra slots, food, drink, medical, throwables, traps)

Part of the feature study of Decimation's own code (16 Oktober 2026, index docs/decimation_code/README.md). Guns:
weapons.md. Item list with classes, loot chances and lists: docs/references/decimation_items.tsv (dev test `items`).

## 1. What it is for

Around 585 items registered in one place (ItemRegistry deci/aD/k): ammunition, guns, armor sets, backpacks, vests and
masks, food and drinks, medical items, grenades and rockets, traps and fortification kits, tools and junk for
crafting and trading.

## 2. Code map

| Class (obf) | Job |
|---|---|
| ItemRegistry (deci/aD/k) | registers every item in preInit (`init`); field names follow the unlocalized names |
| ItemArmorDeci (net/decimation/mod/common/item/armor) | armor with `damageMultiplier` against player gunshots (PacketGunHit handler, slots 0 to 2; the helmet is skipped there) and a full body .bmodel |
| PlayerData extra slots | backpack, vest, mask (PacketSyncExtraArmor); PlayerGearContainer (deci/f/d), BackpackContainer (deci/f/b) |
| FoodItem (deci/ar/a), CannedFoodItem (d), PacketFoodItem (c), RawMeatFoodItem (b), InfectedFleshItem (e) | food with hydration / nutrition ratings; raw meat may infect, infected flesh does |
| BottledDrinkItem (deci/aq/a), CannedDrinkItem (deci/aq/b) | drinks: only usable when thirsty (`onItemRightClick` checks), +6 water, a glass bottle back |
| BandageItem (deci/au/a), SplintItem (deci/au/c), BloodBagItem (b), syringes (d to h) | medical: used like food (`setItemInUse`, 32 ticks), the effect in `onEaten` on the server: bandage stops bleeding, splint mends a broken leg, blood bag restores blood, antibiotic may cure infection, healing syringe heals, empty syringe draws blood |
| DescribedItem (deci/ao/b) | "amount" items (jerry can): `getAmount` (p), `refill` (o), `consume` (a) |
| ISlowsHolder (deci/ao/a) | items that slow their holder (guns) |
| GrenadeEntity (deci/ak/c) and kinds, RocketEntity (deci/ak/f), MortarRoundEntity, TankShellEntity, CrossbowBoltEntity | thrown and fired projectiles, spawned server side from PacketThrowItem / PacketFireProjectileWeapon; DeciExplosion (deci/ak/a) lets explosions pass plants and ladders |
| LandmineEntity (deci/aj/c), BarbedWireEntity, PunjiSticksEntity, CampFireEntity, MortarGunEntity: package deci/aj | placed traps and gear; a gunshot detonates a landmine |
| melee (deci/az): ChainsawItem (fuel from a jerry can), melee weapons, shield (halves gunshot damage) | |

## 3. Item use, as it runs (dev test `useitems`, 16 Oktober 2026)

Each item held and used the vanilla way (item use started, its 32 ticks run out, EntityPlayer finishes it):
- Bandage on bleeding: bleeding stops, one bandage used, twice in a row: PASS. It hands back NOTHING both times.
- Splint on a broken leg: mended: PASS.
- Bottled water at water 5: water 11 and an empty glass bottle back: PASS.

Why the bandage hands back nothing [inferred from the code]: BandageItem and SplintItem keep the "hand back" stack in
a STATIC field, so the last item of each kind built in ItemRegistry sets it for all of them: every bandage uses the
rag's dirty rag stack; every splint (and the morphine syringe, which is a SplintItem) uses the splint's own stack,
built while `itemSplint` was still null. SplintItem's use time is static too (the last one built: 32).

## 4. Singleplayer state and known faults

- Using items works in singleplayer (checked above).
- Bandages never give a dirty bandage back, splints and the morphine syringe never give anything back [run time: none
  handed back; cause inferred].
- PacketThrowItem only blocks throwing near safezones on a dedicated server (architecture.md deci/aE).
- Against NPC fire ("human", "turret") each piece protects 20 % less than its multiplier says (user, 22 Oktober 2026:
  "nerf all armor buff by -20% that affected to player when they shooted by npc"): ArmorGunfireHandler.againstNpc =
  1 - 0.8 x (1 - multiplier), helmets too; player shots keep the full multiplier. Dev test checks "armor vs NPC
  gunfire": a chest of x0.552 gives x0.642.
- Helmets: ItemArmorDeci multipliers skip the helmet on player gunshots; our fixes/ArmorGunfireHandler counts helmets
  on headshots for NPC fire (CLAUDE.md v0.9.1).

## 5. How our mod uses it

| Need | Use |
|---|---|
| a player's conditions and water | `Deci.player(p)` (player.md) |
| jerry can fuel | `Deci.jerryCan()`, `Deci.itemAmount` |
| armor against NPC fire | fixes/ArmorGunfireHandler, fixes/Protection (draft table) |
| item dump with loot chances | dev test `items` -> docs/references/decimation_items.tsv |
| our own items | fixes/NewGuns, fixes/Magazines, fixes/NewSights, NPC and zombie eggs; lang in assets/deciworldgen/lang |

## 6. Checks

- `python3 tools/devtest.py --live useitems`: bandage twice, splint, bottled water (7 checks).
- `checks`: armor and helmets against NPC fire; `items`: the item dump.

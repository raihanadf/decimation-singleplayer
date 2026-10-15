---
name: decimation-code-items
description: Use when changing or debugging Decimation's non gun items - armor and its damage multipliers, backpacks, vests and masks, food and drinks, bandages, splints, blood bags and syringes, grenades, rockets, landmines and other traps, jerry cans, chainsaw and melee - or when code must give, use or read such items.
---

# Decimation items and gear code

Facts and the checked item use: docs/decimation_code/items.md. Item names, classes, loot chances:
docs/references/decimation_items.tsv. Guns: decimation-code-weapons.

## Quick reference

| Task | Where |
|---|---|
| where an item is registered | `ItemRegistry` (deci/aD/k) |
| what using it does | its class's `onEaten` (food, drinks, medical) or `onItemRightClick` |
| armor numbers | ItemArmorDeci `damageMultiplier`, fixes/ArmorGunfireHandler, fixes/Protection |
| find an item from code | `GameRegistry.findItem("deci", "ItemBandage")` (names as in the tsv) |

## Rules

1. Medical items and drinks act in `onEaten` on the server after their use time: a test must start item use on the
   held stack and let the time run out, not call the effect directly (UseItemsTest).
2. Drinks only start when the player is thirsty; tests bypass that by starting item use themselves.
3. Do not trust what BandageItem / SplintItem hand back: their static "hand back" stack is shared and broken.
4. Test live: `python3 tools/devtest.py --live useitems` and `checks`.

## Common mistakes

- Reading the item class from the tsv and assuming one class is one kind: the morphine syringe is a SplintItem, the
  rag is a BandageItem.
- Giving armor and expecting a multiplier on NPC shots: Decimation applies it only to player gunshots; NPC fire
  goes through our ArmorGunfireHandler.

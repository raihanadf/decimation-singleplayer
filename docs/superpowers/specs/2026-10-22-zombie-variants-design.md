# Zombie variants, mutants and infected animals (feature 13; docs/ideas.md 37)

User, 22 Oktober 2026: "More zombie variants, mutants too". Chosen the same evening: all three groups (special infected,
rare big mutants, infected animals); looks from Decimation's own models with new skins of ours; spawning through the
place table of feature 8.

## What exists (checked in code)
- fixes/InfectedVariants: looks and stats on Decimation's infected (deci/ag/d) by attribute modifiers and armor: runner,
  riot, screamer (its scream calls nearby infected), irradiated, medic; Decimation's own kinds: crawler, dog, frail,
  gawker, hulk, bloater, demon.
- fixes/PlaceSpawns (feature 8): config/deciworldgen_places.json turns natural infected spawns into a place's mobs
  (airport, base, police station, hospital, highway, wasteland, forest), with chances, groups, caps, day or night.

## Design
A. Special infected (InfectedVariants, more entries; behaviour in a small AI task or a server tick hook per kind):
   - spitter: keeps 6 to 12 blocks away and throws acid (an arcing projectile of ours drawn as a green particle trail;
     a hit: 3 damage and poison 3 s), every 3 s; weak in melee.
   - stalker: in light 7 or less it is nearly invisible (Decimation's model drawn at 15 % alpha) and quiet; within 6
     blocks it leaps at the player (one jump, 4 s cooldown); in light it walks as a plain infected.
   - armored: riot gear plus a plate: body shots take a quarter, headshots full (the shot's height against the eyes, as
     ArmorGunfireHandler does).
   - screamer stays as it is (its call already exists), spawns more often at night.
B. Mutants (rare, one at a time near a player, caps in the place table):
   - radiation mutant: Decimation's hulk model, 1.4 times, green skin of ours with a glow layer; 200 health; a 6 block
     radiation aura (our dose rises as in a zone); only in the wasteland city; drops a loot body with military items.
   - brute: hulk model, scarred skin; breaks Decimation's doors and our wood in its way; 150 health; city streets at
     night.
   - night hunter: infected model, long thin skin, 1.2 times; fast, leaps from 10 blocks, flees light; only at night,
     forests and highways.
   Each mutant: a boss bar of Minecraft's (name over it), a loud cue when it notices a player.
C. Infected animals:
   - infected wolf: Decimation's dog model, grey torn skin; packs of 3 to 5 in forests (the dog entry today stays).
   - crow: a small flying mob (vanilla bat model with a black skin of ours; Decimation has no bird) that perches on
     roofs and, when it sees a player within 16 blocks, caws and draws infected within 32 blocks to that spot
     (the GunNoise path).
   - rat: a tiny fast mob (vanilla silverfish model, brown skin of ours) in rail stations, sewers and basements, bites
     for 1 with a small infection chance (Decimation's infection).
   My call: the two models Decimation lacks (a bird, a rat) come from vanilla with our skins.
D. Spawning: new entries in the place table's defaults (a table already written by a player keeps its own entries; ours
   are added once with a version bump), chances per place and night; the place spawn hook does the rest.

## Performance
Per mob per tick only what vanilla AI does; behaviour checks every 10 ticks (light, distance); the stalker's alpha is a
render state, no extra pass; mutant auras check players every 20 ticks; budgets: server under 50 us a tick for 20 of the
new mobs, client draw within 10 % of a plain infected per mob.

## Tests
Unit: stats and rules per kind (aura dose, armor factor by hit height, leap cooldown, alpha by light). Dev test `zvariants`:
each kind spawned in the arena, its behaviour seen (spit hits, stalker alpha in dark and light, armored body vs head,
mutant aura and boss bar, brute breaks a door, crow call draws infected, rat bite), shots of each next to a plain one,
the costs; the production client with OptiFine for the drawing.

## My calls (for review)
Behaviours and numbers above, vanilla bat and silverfish models for crow and rat, mutants one at a time per player.

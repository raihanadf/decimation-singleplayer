# Ideas for later (user, 12 Oktober 2026)

The user's list of future features ("just idea"), written down as given, with notes on what we
already know. Nothing here is planned or designed yet: each needs a design talk with the user
first (questions with options) before any code. Priority now: finish the current work first
(user: "we figure it out previous feature now, prioritize that!"). Listed in docs/roadmap.md
"Ideas (later)".
PLANNED TOGETHER 13 Oktober 2026 (user choices taken, no code yet): docs/systems_plan.md covers
every item below that is not DONE.

## Loot and items
1. **More crate types**: industrial, civilian and more, each with its own loot.
   Note: Decimation's loot pools are hardcoded per block (deci.aD.l, documentation.md "Loot pool
   definition"); a new crate type means a new block of ours plus a pool registered through Deci, or
   reusing an existing pool [not checked how].
2. **More loot type blocks** (same direction as 1: more containers with their own loot).
17. **Crate levels** (user, 13 Oktober 2026: "new custom crate we make later, maybe the crate system
    will be based on level, example crate level 1,2,3 like that with different loot reward"): our own
    crates in levels 1, 2, 3, each level a richer loot pool. Ties to the wasteland locked doors
    (v0.55.0): the buildings behind a locked door are the ones with the most and best loot, so the
    higher level crates would go there first. Not designed yet (how many levels, which pools, how the
    level shows on the crate, where each level spawns); same notes as item 1 on Decimation's
    hardcoded pools.
    DESIGN DRAFT 13 Oktober 2026 (user choices taken): docs/loot_levels.md.

## NPCs
3. **More military NPC outfits** (skins / armour looks per tier).
4. **Sniper bandit spawn egg** (we have tier eggs: fixes/NpcEgg, NpcLoadouts; a bandit sniper tier
   may need a new tier, new tiers go LAST in NpcLoadouts).
21. **Survivor partner** (user, 16 Oktober 2026: "survivor can follow players like bro anywhere like dog
    partnership, they can be customize loaded by player like what type armor he use, guns, weapon, and
    player can put backpacks on him so he gonna be like chest moving"). A friendly survivor NPC that
    follows its player everywhere (like a tamed wolf), fights beside them, and is equipped by the player:
    armour, gun and melee weapon chosen from the player's own items, and a backpack that makes the
    partner a walking chest. Not designed yet; first look (16 Oktober 2026) in the chat: possible, pieces
    exist (NpcLoadouts gun sync, Decimation armour on NPCs, our loot screen inventory from LootBody,
    vanilla follow owner AI) [inferred, nothing built or tested]. Open: how a partner is found or
    recruited, ammo use from the backpack, death (loot body with the gear?), teleport when left far
    behind, one or several, orders (stay, follow, guard).

## Zombies
5. **Harder zombies**, **more zombie variants** (fixes/InfectedVariants already has variants).
6. **Zombies with melee weapons** as a variant.
7. **Smarter zombies**: today GunNoise makes infected walk to where a shot was heard; idea: they know
   WHO shot and follow / lock onto that player.
8. **Armour counts against zombies too** (today ArmorGunfireHandler covers NPC gunfire; zombie hits
   [not checked]).
9. **Five levels of infection** until the player dies (an infection that worsens in stages).
10. **Demons** (Decimation has a demon mob: deobf render/DemonRenderer) spawning in the wasteland or
    military zones.
11. **Zombies grow over time**: from day 1 to day 100 the zombies get more / stronger ("idk which
    yet": count, strength, variants all open).

## World
12. **City zone names**: DONE v0.52.0 (docs/zones.md "Place names"). Was: entering a city shows its name on screen, generated as "[district] -
    [number]", the number taken from the chunk / cell location (zones: docs/zones.md; client zone
    lists exist since v0.46.0).
13. **City safezone as a police building**: our city refuge (v0.48.0, docs/safezone.md section 7) is
    a 48 x 48 compound that does not fit the chunk city like DeceasedCraft's generator does. Idea:
    keep the refuge feature, but in cities use a structure we already have, a police building,
    barricaded, as the safezone; so police buildings come in 3 kinds: normal, abandoned, and a
    barricaded safezone with people inside.
14. **Wasteland district = military area**: DONE v0.54.0 (docs/wasteland.md; DeceasedCraft checked:
    partly right, its wasteland holds the military base, camp and bunker with military loot but is
    mostly industry; the whole district is military in ours). Was: the user says DeceasedCraft's wasteland area with its
    wasteland buildings is really a military zone / sector, to be stocked with military loot. User:
    "if you dont trust me you can check it up on deceased craft directly" [not verified yet: check
    DeceasedCraft's city style data (legacy deadzone) and its loot before acting]. Today our
    wasteland district stands next to military sectors (v0.24.3) and is zoned MILITARY.
15. **Story** ("story??": open, nothing decided).
18. **Military airport in a wasteland city** (DONE v0.62.0, docs/airport.md) (user, 13 Oktober 2026: "airport military base with
    barracks, HQ, tower, aircraft park and hangar on wasteland later? and how gonna big it is?").
    First size estimate [draft]: it takes half a wasteland city sector (2 districts side by side,
    128 x 256 blocks) instead of city blocks: runway about 220 x 15 along the long side with
    markings, a parallel taxiway 8 wide, an apron about 64 x 48 as the aircraft park, 2 or 3 hangars
    about 32 x 24 (our FOB hangar vault, bigger), a control tower about 8 x 8 and 25 high, HQ and
    barracks rows, a fuel farm; the sector fence already closes it. Aircraft: Decimation has only the
    crashed helicopter wreck prop (and its flyable helicopter vehicle [not verified]), so parked planes
    need models of our own (multiblock props or entities) [not verified: effort]. Built on the
    military base Canvas (docs/military_base.md). A full sector (256 x 256) if a longer runway is
    wanted.
20. **Auto update**: DONE v0.59.0 (fixes/Updater, UpdateScreen, UpdateSwap; PatchNoUpdate v2; dev test
    `update` with tools/fake_release_server.py). Was (user, 13 Oktober 2026: "user launch decimation then it trigger check updates on
    github via api, if there is update, the button on hud show "update" and it will begin install on
    minecraft with install progress screen. if not it show "play" button"; earlier: a Vercel website
    showing the project). Draft: our mod asks GitHub's latest release API at the menu; newer = the
    menu's Play button reads Update (Decimation's own menu button and progress screen style), the new
    Decimation.jar downloads with a progress bar into mods/ under a temporary name, and the swap
    happens after the game closes (a small helper process: Windows locks a loaded jar [inferred]);
    then "restart to play". The site: a project page linking the GitHub releases (Vercel serves no
    jars). Every update is about 240 MB while the release is one jar.
19. **City sizes** (user, 13 Oktober 2026: "city have variant size, so its not stuck on 4 district
    like we have now, we can have 8, 16, 24, 48 or even 64 disctrit inside one city"). Today a city
    is the CITY sectors that touch (Sectors: 15 % of 256 block sectors, one at random each), so most
    cities are one sector = 4 districts (seed 1: 1096 of 1509 cities one sector, the biggest 8
    sectors). Idea: a city layout of its own that grows a city of 2 to 16 sectors (8 to 64
    districts) from a seed, the wasteland city sectors on its side toward the military.
    DONE v0.60.0 (new worlds): worldgen/CityLayout, docs/city_engine.md "City sizes".

## Menu
16. **GitHub repo link on the home screen**: on the menu shown before the player clicks Play (the
    "previous hud"), not on Decimation's server screens. The main menu GUI is otherwise "not our
    priority" (roadmap UI 1).
22. **Host from your own computer, listed as a community server** (user, 18 Oktober 2026: "user that doesnt have
    server and cant make it, they can make it by it self from their computer as local server, then its bridged to our
    server so its can be listed on community server. so our server just act as bridge"). The world runs on the
    player's computer (Minecraft's integrated server, Open to LAN); the mod opens an OUTBOUND connection to a relay of
    ours (works behind home routers, no port forwarding); the relay gives the world a public address and lists it on
    Decimation's Community page (roadmap UI 4, the servers.json plan of UI 2). Known as listen server plus relay /
    tunnel: Minecraft mods e4mc and Essential, playit.gg, Steam Datagram Relay, Unity Relay [not verified: from memory,
    not looked up]. Notes: through a relay a player's ping is player to relay plus relay to host (only a direct hole
    punched connection is host only); the relay needs an always on machine with a public address (Vercel cannot hold
    game connections); the relay carries every byte (bandwidth cost); the world is offline when the host quits.
    Not designed yet. Deeper analysis: docs/player_hosting.md.

23. **Supporter tier for donors, through our website** (user, 21 Oktober 2026: "website maybe, so user login with username
    or something and they can donater their money so they get supporter tier"; the day before: Supporter "will use for
    people who donate to me so they got supporter benefit"). The menu says Supporter again (docs/main_menu.md 2g); nobody
    is one yet: Decimation took isSupporter from BoehMod's backend. Questions for the design talk [not designed]: how a
    player proves a Minecraft name is theirs (offline mode names can be typed by anyone: a Mojang or Microsoft sign in,
    or a code shown in game), which donation service takes the money (never card data on our own site), where the
    supporter list lives and how the game reads it (like the relay's list), what a supporter gets (Decimation's lounge:
    capes), and money rules (refunds, taxes).
24. **Official server list of community run dedicated servers, ranked by upvotes** (user, 20 Oktober 2026; 21 Oktober:
    "yeah maybe"): Global Server on the status page follows that list (fixes/MenuStatus already does once real rows
    exist). Not designed yet (roadmap UI 4, C).
25. **Gun customization screen, Tarkov style** (user, 21 Oktober 2026: "later i want to add new feat again for gun
    arsenal, we have gun customization UI instead using 'craft' UI like we have now when click I button, so its look like
    tarkov style customization UI"; reference picture in the chat, a modern shooter's gunsmith screen): the gun large in
    the middle on a dark background, a label at each slot pointing at its place on the gun (Optics, Stocks, Foregrips,
    Magazines, Rear Grips, Forend Device), tabs Attachments and Appearance top left, Quit top right, Clear Attachments and
    Randomize Attachments bottom right. It would replace Decimation's crafting style attachment screen (AttachmentScreen
    deci/g/a, docs/decimation_code/weapons.md). DESIGNED 21 Oktober 2026 (user: "its actually still using six slot but
    showing real 3d of the gun, just that simple ... just click I and it show the guns model on it"; Tarkov weapon
    modding as the reference; layout A "Tarkov lines" picked from three mockups): docs/superpowers/specs/
    2026-10-21-gunsmith-screen-design.md. The I key is Decimation's key.decimation.gui.attach (ATTACH action). BUILT
    21 Oktober 2026 (plan docs/superpowers/plans/2026-10-21-gunsmith-screen.md; docs/decimation_code/weapons.md section 7).
    Turning the gun with the mouse added the same day (spec section 9).
26. **Full attachment update** (user, 21 Oktober 2026: "in future we may fix grip and laser model since they dont have
    model on it, and at the same time we build full new attachment update later"): grips, lasers and flashlights
    showing on every gun (GunItemRenderer deci/K/b draws a grip part at one fixed offset for all guns), and new
    attachments. After the Gunsmith screen (item 25). Not designed.
    Reference source the user uses (21 Oktober 2026): tarkovbot.eu/weapon-builder, whose "download" gives a preset JSON
    (checked on the user's my_preset.json, 384 KB): presetName, selectedWeaponId (a Tarkov item id, 24 hex), attachments
    (a tree of {slotId, itemId, attachments}, 13 parts in 4 levels there), stats (Ergonomics, Accuracy, SightingRange,
    VerticalRecoil, HorizontalRecoil, MuzzleVelocity, FireRate, Weight) and icon (a data URL PNG, 1600 x 900, the build
    side on, muzzle to the LEFT). No 3D model, no geometry, no part positions. Item names come from tarkov.dev's public
    GraphQL API by id (it answered "GraphQL server unavailable" that day) [not verified]. The picture and Tarkov's
    models are Battlestate Games' art: reference only, never in the repo, a jar or the website.
    Its 3D view (checked with agent-browser, 21 Oktober 2026, the default Makarov): a left drag on the view turns the
    gun, the slot boxes stay and their lines follow the parts (the same as the Gunsmith turning the user chose); buttons
    for fit, a picture (a download: not used), reset view, "Dark BG"; "Load Downloaded" imports a preset JSON. So views
    from every side (both sides, top, bottom, front, back, three quarter) can be captured by script with agent-browser
    screenshots for modelling references (user: "you use that as reference as you know the model 3D so you make new one
    with decimation styled"; single pictures left parts at wrong positions). Checked further the same day with the user's
    preset (loaded through #loadPreset, the file input "Load Downloaded" uses): the CMMG Mk47 Mutant build appears with
    every part's slot box (item short names such as 409mm, Mk47 Resolute, ATP, MOE FS, B-25U RK-1); buttons by id
    togglePoints (the slot points and lines, drawn in the canvas), screenshot, resetView; "Dark BG" off swaps the dark
    backdrop for a bright game scene (both busy: a picture of the gun alone needs a cut out, for example two backdrops
    differenced); an "Export GLB" button exports the build as a 3D model file. A GLB would give exact geometry (and maybe
    part names) for exact views and sizes; I may not download files from that site myself, so the user would export it
    and hand over the file, kept outside the repo. Resolved the same day: that button is hidden by the site
    (display none, not offered to users), so it is not used. BUILT 21 Oktober 2026: tools/guns/refcapture.py (skill
    decimation-gun step 1): the preset loaded (upload retried until the gun or its parts change), points hidden, 8 views
    each shot on the dark and the light backdrop and cut out by equal pixels (exact: no backdrop pixel is equal in both),
    zoomed out 21 wheel steps so the side view fills a third of a 2560 x 1600 frame (perspective eased); the user's Mk47
    preset gave side 846 x 258, top 907 x 107, the same sizes on a second run, and its 13 parts by name.
    PART BUILT 21 Oktober 2026 (plan docs/superpowers/plans/2026-10-21-new-attachments.md, weapons.md section 8): every
    grip (Decimation's Foregrip, Laser, Flashlight and our RK-1) placed under each gun's own handguard (gripfit.py,
    GripPlacement), the RK-1 Foregrip (recoil 1/2 up, 1/4 sideways: GripRecoil, PatchGripRecoil) and the ELCAN SpecterDR
    1x/4x (G switches it: SpecterMode, SpecterKey), modelled from the user's build's views. Still open from this idea: a
    laser beam and a flashlight that lights, new laser and flashlight models, more attachments.
27. **3D gun converter: a model in, a Decimation styled .bmodel out** (user, 21 Oktober 2026, "for long development":
    "later, we can make some private web html that convert 3D guns object as example we use tarkov 3D first and when it
    click 'convert' there is convert tools that generate it to bmodel and minecraft decimation styled"). A private local
    page (three.js) shows the source model and the converted one side by side; Convert runs our tools/guns pipeline:
    scale to Decimation's units, cut the mesh into parts (by its node names where it has them), fit each part with
    Decimation's shape kinds (boxes, tapers, octagon barrels; docs/gun_style_guide.md), flat dark tone textures, the
    attachment points (rail, muzzle, grip) for sightfit and the grip fit, then .bmodel, icon and .anib from Decimation's
    templates; an edit step before export. The hard part is the style, not the format: an automatic fit looks like a
    voxel blob without gun anatomy, so the converter proposes and the decimation-gun skill refines. Inputs must be
    models we may use (CC0 / CC BY, our own); Tarkov's models stay picture references only: their files are not offered
    (the builder hides its GLB export) and a converted copy would be a copy of Battlestate Games' work in a public
    release. Suggested start: after the RK-1 and SpecterDR, whose fit tools and style rules it reuses. Designed and
    started 22 Oktober 2026 (spec docs/superpowers/specs/2026-10-22-gun-converter-design.md); milestone 1 converter
    side built: docs/gunconv.md.

## The user's list of 22 Oktober 2026 (not designed; each needs a design talk first)
User: "for the feat i have more". Notes on what we already have are mine [not analysed in code unless said].
28. **Random events: a survivor with a timed quest** ("random survivor that provide random quest that need to be solved
    with limited time before they dissapear"). Related: the survivor tier and camps (docs/safezone.md), WorldClock.
29. **Tarkov style loot search** ("loot system where it need to be analyzed one by one before taking it. so the item will
    show loading icon animation then if it done it can be take it"): items in a crate or body unknown until searched,
    one at a time with a spinner. Related: our loot screen (fixes/Loot, Deci.setLootInventory), docs/loot_levels.md.
    Decided 22 Oktober 2026 (user): by value, 1 to 3 s an item (ammo and scrap 1 s, attachments and gear 2 s, guns and
    rare items 3 s), one by one with a spinner, crates and bodies alike. Feature 12, this session.
30. **Radiation status in the HUD** ("radiation status below player status"). Decimation already tracks radiation per
    player (docs/decimation_code/player.md) and draws its HUD in IngameHud (deci/g/j). Built 22 Oktober 2026 as a radiation dose in
    percent (new_feature.md "Radiation dose on the HUD").
31. **Full story mode with an achievement styled UI**. Related: the story item in "Ideas (later)" (docs/roadmap.md).
32. **Friendly NPCs that give quests or gifts**: random friendly NPCs offer a quest or a gift; such an NPC never
    despawns and glows (vanilla's glow effect does not exist in 1.7.10: an outline or particles of our own [inferred]).
33. **Trader quests from easy to extreme**. Related: Decimation's traders (docs/decimation_code/traders.md).
34. **NPC tracers in random colours, every bullet a tracer**: NPC guns use other tracer colours than white, and every
    shot draws a tracer instead of one in 3 to 6. Related: PatchTracer, fixes/NpcShots, Decimation's tracer items
    (TracerItem deci/ay/k, the Gunsmith's tracer slot).
35. **Specific mobs at specific places** ("specific location can be spawned by specfic mob"). Related: zone infected
    variants (fixes/InfectedVariants, docs/zones.md), MilitarySpawner, Garrisons.
    Decided 22 Oktober 2026 (user): a table per place kind, editable in a config file: the wasteland irradiated and
    military infected, demons at night; bases and airports soldiers; hospitals medic infected; police stations riot
    infected; forests infected dogs; highways bandit groups. Agent, feature 8.
    Built 22 Oktober 2026 (new_feature.md "Specific mobs at specific places"): config/deciworldgen_places.json, the
    place kinds known from the seed and our named places (worldgen/Places), natural infected spawns turned into the
    place's mobs or looks (fixes/PlaceSpawns), new looks irradiated and medic; dev test placespawns.
36. **Loot does not refill in singleplayer, a refill timer on servers**: "since city can be generated, loot by default is
    empty forever after player loot it, except multiplayer they can be disable or enable for minutes refill loots (by
    default is enable for server)". Decimation's crate cooldown is per position (deci/aB/e; docs/decimation_code/
    loot.md): singleplayer would never reset it, servers a config (on by default, minutes).
    Decided 22 Oktober 2026 (user): "15 minute use old decimation setting, for singleplayer it has option on cfg file world
    too so they can use refill method or not": servers refill after 15 minutes (on by default), singleplayer never by
    default with a per world config switch; cooldowns saved with the world. Feature 12, this session. BUILT 22 Oktober
    2026 on main (fixes/LootRefill, <world>/deciworldgen_loot.cfg; new_feature.md "Loot rules A").
37. **More zombie variants, mutants too**. Related: fixes/InfectedVariants (v0.34.0), ideas 5 to 10 above.
    Decided 22 Oktober 2026 (user): special infected, rare big mutants and infected animals; Decimation models with new
    skins of ours; spec docs/superpowers/specs/2026-10-22-zombie-variants-design.md (feature 13).
38. **Achievements from easy to extreme, and secret ones**. Related: item 31 (its UI), LocalStats.
39. **Trader market: supply and demand, changed each Minecraft day** ("trader now have dynamic market supply and demand,
    its randomize by day in minecraft"). Related: Decimation's trader offers (docs/decimation_code/traders.md),
    WorldClock.day() for the day (never getWorldTime() / 24000).
    Decided 22 Oktober 2026 (user): daily random drift plus player trades (selling a lot lowers a price, buying raises it,
    both ease back over days), each trader a few items on sale and in demand per day (feature 11, agent).
40. **Attachments with benefits on guns** ("attachemnt can have benefit on guns"): each attachment changes stats
    (recoil, spread, aim speed, noise...). Today: grips change recoil (fixes/GripRecoil), suppressors noise
    (fixes/GunNoise), sights zoom; Decimation's own rules otherwise (docs/decimation_code/weapons.md).
    Decided 22 Oktober 2026 (user): Tarkov style stats, a small plus and minus each: sights aim faster or slower by size,
    suppressors quieter and no flash but a little less damage, stocks less recoil, lasers tighter hip fire, grips as
    now; shown in the Gunsmith screen. Agent, feature 9.
41. **Radiation pills** for radiation sickness. Decimation has no such item [checked in decimation_items.tsv: no rad
    or iodine item]; its radiation today is only "irradiated" while inside a RADIATION zone without a gas mask, 10 % a
    tick 1 damage (docs/zones.md RADIATION): a sickness that lasts after leaving would be new.
    Decided 22 Oktober 2026 (user): a pill takes 40 percentage points off the radiation dose at once (fixes/RadiationDose,
    feature 5); found in medical loot. Built by the agent on branch bugfix (feature 7).
    Built 22 Oktober 2026 (new_feature.md "Radiation set"): deciworldgen:radiation_pills (fixes/RadiationPills), used
    like Decimation's Pain Killers (4 a stack, 32 ticks of use, its syringe sounds), in medical crates like Vitamin
    Capsules; dev test radset.
42. **Gas mask durability, refilled with a gas mask filter**. Decimation already has the items: deci:gasmask,
    deci:gasmaskmilitary (ItemGasMaskDeci) and deci:ItemScrapMilitaryGasMaskFilter ("Gas Mask Filter", today scrap, in
    supply drops only). The mask protects without wear today.
    Decided 22 Oktober 2026 (user): a filter wears only while its wearer is in radiation, about 10 minutes a filter; an
    empty filter protects no more until a new Gas Mask Filter is fitted (right click it). Agent, feature 7.
    Built 22 Oktober 2026 (fixes/GasMaskFilter): 12000 ticks a filter in the worn mask's tag, "Filter: N%" in the
    tooltip, an empty filter lets our dose rise and gets Decimation's zone damage, Decimation's HUD then says
    "Irradiated"; fitting through our channel (message 32), one filter used; filters in military and police loot (one
    entry each, as rare as a gas mask).
    Follow up decided 22 Oktober 2026 (user): Gas Mask Filters about 3 % of military crate opens (police 7 % as built); an
    empty filter protects against nothing, Decimation's tear gas included. Agent, feature 7 follow up.
    Follow up built 22 Oktober 2026 (new_feature.md "Radiation set", follow up): 9 filter entries in the military pools,
    3.2 % of military crate opens; police 1 entry, 5.6 % now (feature 10's repair parts grew the pool, 2 entries would
    be 10.6 %); an empty filter lets Decimation's tear gas poison through (fixes/GasMaskFilter, server side).
43. **The wasteland city one radiation zone everywhere** ("wasteland city become full radiation zone on all place"). Each
    wasteland district is a MILITARY zone today (docs/wasteland.md, city/Wasteland); Decimation's RADIATION zone works in
    singleplayer since v0.46.0 (fixes/ZoneRules). A place can be in two zone kinds only if the zone lists allow it
    [not analysed]. Goes with 41 and 42.
    Agent, feature 7 (user wish as written: the whole wasteland city one radiation zone).
    Built 22 Oktober 2026 (city/WastelandRadiation): two kinds on one place work (each zone check asks one kind,
    docs/zones.md section 2); one RADIATION box over each wasteland sector (older worlds: each district), older worlds
    get theirs at load.
44. **Unload a magazine with shift + right click** (its rounds back as ammo). Related: fixes/Magazines (v0.35.0),
    Decimation's magazine items keep rounds in NBT (docs/decimation_code/weapons.md).
45. **Magazines from loot hold a random number of rounds** ("all mag loot have variaty capacity ammo inside it").
    Related: fixes/Loot, LootPools, Decimation's loot lists (docs/loot_levels.md section 2).
46. **Armor nerf against NPC fire, 20 % less protection** ("nerf all armor buff by -20% that affected to player when
    they shooted by npc. now the damage dealt more"). Today: fixes/ArmorGunfireHandler applies Decimation's
    damageMultiplier per piece to the "human" and "turret" sources, helmets on headshots, after the old 35 % armor
    buff (a one-off jar patch), then NPC hits x5 (v0.30.2). Exact meaning to settle in the design talk: each piece's
    protection (1 - multiplier) times 0.8 [my reading].
47. **Armor durability, repaired with parts**: armor 256, vests 512; 1 shot = 1 durability; armor repaired with an armor
    part, vests with an armor plate (a new item found in military and police crates). Decimation's armor has no wear
    against gunshots today [not analysed].
    Decided 22 Oktober 2026 (user): at 0 an armor piece or vest stays worn but protects nothing until repaired; one
    armor part or armor plate repairs a quarter (four for full); parts and plates in military and police crates only.
    Feature 10, this session.
    Vests (ItemVestDeci: Military, Police, Bullet Proof...) protected nothing until now: their multiplier was never
    applied, by Decimation (its hit handler multiplies armor slots 0 to 2 only) or by us. Decided: against body shots
    the stronger of the vest and the chest armor piece counts (no stacking); worn 1 a hit, repaired with armor plates.
    BUILT the same day (new_feature.md "Durability of armor, vests and guns").
48. **Gun durability, repaired with gun parts**. Decimation's guns have no durability today [not analysed].
    Decided 22 Oktober 2026 (user): 1 durability a shot, about 1500 rounds for a rifle (more for pistols, less for machine
    guns); below 25 % the odd jam, cleared by racking; at 0 it will not fire until repaired; one gun parts item repairs a
    quarter; found in military and police crates. Feature 10, this session. BUILT the same day (new_feature.md).
49. **More lootable props, each in two versions (plain and lootable)**: truck, wooden crate, water pallet, military
    radio, small military radio. Decimation's loot registry is per block (33 blocks, docs/loot_levels.md section 2):
    a lootable version is a block of ours or a metadata rule [not designed].
    Decided 22 Oktober 2026 (user): "lootable is just copy of block the props i say then make lootable version ... so later
    when we make world city props, we can placed 2 version props non loot and lootable": a lootable copy of each of these
    props (same look), marked by Decimation's loot hint; worldgen places either. Feature 12, this session.
    BUILT 22 Oktober 2026 on main (fixes/LootBlocks; new_feature.md "Loot rules B").
50. **Props with a loot kind**: a shelf can hold civilian, military or police loot; the weapon cabinet in three versions:
    civilian ("Cabinet", no "weapon" in the name), police and military. Today the metal shelf uses list 1 (general) and
    the weapon cabinet list 10 (guns and magazines). Goes with crate levels (item 17, docs/loot_levels.md).
    Decided 22 Oktober 2026 (user): the building a prop stands in decides its kind (police stations police, military
    buildings and bases military, else civilian); the civilian weapon cabinet is called "Cabinet". Feature 12.
    BUILT 22 Oktober 2026 on main (fixes/LootBlocks; new_feature.md "Loot rules B").
51. **More new blocks** later (user, no list yet).
52. **DayZ dark nights** ("the night become so dark so its like dayz they almost black 95% in night time"): at night the
    world goes almost black, about 95 %, so light matters (the Flashlight attachment, night vision goggles, torches,
    street lights of our cities). Vanilla never goes that dark (a minimum light level plus the brightness / gamma
    setting); it needs our own light map at night, and a fix so the player's brightness setting cannot undo it
    [not analysed; Decimation's weather and fog: docs/decimation_code/world.md]. My reading: a feature, not a report
    that nights are too dark now.
    Decided 22 Oktober 2026 (user): DayZ style, near black (about 95 %) on a new moon, a full moon lets you make out
    shapes; light sources work as now; nights ignore the brightness slider (daytime keeps it). Feature 6, this session.
    BUILT the same day (new_feature.md "DayZ dark nights"; with real moon phases: fixes/MoonClock).
53. **Some looted guns hold rounds** (user, 22 Oktober 2026: "some guns loot have ammo inside it, so not all of them is
    empty"). DONE the same day (new_feature.md "Looted guns hold rounds").
54. **No Steve: every player a random Decimation skin** (user, 22 Oktober 2026: "later we dont use steve default skin,
    instead using decimation skin with randomize so each player has unique skin they use, maybe like random ID we got
    from 1-10 skin as example"). Decided the same day (user answers): the skin comes from the player's id (the same
    skin every time, every client works it out alone, no packets: "as long not distrub other feature especially
    multiplyaer"); only players showing the default skin get one ("player that have skins before can use their own
    skin"; Minecraft 1.7.10 has only Steve, Alex came in 1.8); all of Decimation's 21 human skins (human0 to human20,
    textures/model/entities/mob/human, the ones its NPCs wear); built later, as 5b of docs/roadmap.md "Feature order".
    Open for the build [not analysed]: how Decimation draws players (its own player renderer and gear layers, or
    vanilla RenderPlayer), and whether its human skins fit the player model's texture layout (64 x 32). BUILT the same
    day (new_feature.md "Random Decimation skins instead of Steve"): vanilla RenderPlayer, skins 64 x 32, picked by name.
55. **Gun converter, next steps after the mesh guns** (user, 23 Oktober 2026, after milestone 4 worked: "im happy this
    feature is real, im desprate before cause its look shit"; later, not designed yet, in this order as the user said
    it):
    1. **Attachments through the converter**: an attachment model (sight, grip, suppressor, ...) converted like a gun,
       as a mesh in Decimation's tones, into its own attachment package.
    2. **Attachment preview on the converted gun**: the page shows every attachment the gun takes and lets the user
       fit one in the preview, to test how it sits before export.
    3. **Better part identification**: the roles (body, magazine, bolt or slide, iron sights) found more reliably
       (today: names first, then materials, then the like gun's anatomy; docs/gunconv.md section 13).
    4. **A stock attachment slot** (AR-15 platform): stocks become an attachment kind of their own.
    5. **Aim preview**: the page shows the gun as it looks while aiming, so the right crosshair spot (the sight line)
       is known exactly before export.
    6. **Attachment aim preview**: for a converted sight, how it looks while aiming through it, with its own reticle /
       crosshair.
56. **Gunsmith: no slots a gun cannot use, and stats** (user, 23 Oktober 2026: "some guns that doesnt support any
    attachment no longer need to show attachment slot in there, so user dont think that gun have attachment. and gun
    custom UI have stats later"). A fix for later: the Gunsmith screen (fixes/gunsmith) hides every slot the held gun
    cannot take (sight mount, grip rail, barrel), so a gun without any shows none; and the stats panel (feature 9,
    docs/superpowers/specs/2026-10-22-attachment-benefits-design.md) joins it.
57. **More blocks and colour variants of props** (user, 23 Oktober 2026: "more blocks maybe related with decimation
    ... more color type crate, example military crate has 4 color, green, tan desert, grey, black. this feat not on
    crate too, its on some other props too like package. so this feat just retexture task"): new blocks in Decimation's
    world, and the same prop in several colours (a military crate in green, desert tan, grey and black; packages and
    other props too): retextured copies of Decimation's props (as fixes/LootBlocks copies them), loot kept per kind.


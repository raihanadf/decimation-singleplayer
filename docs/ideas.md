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
    release. Suggested start: after the RK-1 and SpecterDR, whose fit tools and style rules it reuses. Not designed.

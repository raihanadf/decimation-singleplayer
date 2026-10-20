# Gunsmith screen (Tarkov style attachment screen): design

Date: 21 Oktober 2026. Status: written for the user's approval. Talk: this session (layout picked in the browser
mockups, behaviour played back and confirmed: "nothing i guess"). Idea: docs/ideas.md item 25.

## 1. Intent

The user, 21 Oktober 2026:
- "later i want to add new feat again for gun arsenal, we have gun customization UI instead using 'craft' UI like we
  have now when click I button, so its look like tarkov style customization UI"
- "its actually still using six slot but showing real 3d of the gun, just that simple. but the different it doesnt need
  to pull gun item to gun slot, so barely just click I and it show the guns model on it"
- Reference: Escape from Tarkov's weapon modding (a tarkovbot.eu weapon builder shot): the gun large in the middle, a
  box per slot around it, each joined to its mount point on the gun by a thin line.
- Picked layout A, "Tarkov lines", from three mockups (A Tarkov lines, B lines from two columns, C simple row).

Why: today the I key opens Decimation's small crafting style box (AttachmentScreen deci/g/a over
WeaponModifyContainer deci/f/a). You drag the gun into a middle slot and parts into six slots around it, you never see the gun,
and closing the box with the gun still in it drops the gun as an item (WeaponModifyContainer.onContainerClosed).

Success: press I with a gun in hand and you see that gun, large and real, with its parts on it. Fitting a part is a click.

## 2. What the player gets

1. Press I (Decimation's own key, "key.decimation.gui.attach", default I) with a gun in the hand: the Gunsmith screen
   opens at once. No gun in the hand: nothing opens and a short message says "Hold a gun to modify it".
2. On screen: the held gun's 3D model, large, in the middle, over the dimmed world; its name at the top; "Esc Close" top
   right; the hotbar row at the bottom with the gun's slot marked.
3. The same six slots as today, each near its part with a thin line ending in a dot on that part:
   Sight (the sight mount), Barrel (the muzzle), Grip (under the fore end), Stock (the stock), Paint (the body),
   Tracer (the magazine). An empty slot shows its name; a fitted slot shows the part's icon and name.
4. Click a slot: a small list, "<Slot>s you carry", shows the matching parts in the WHOLE inventory (hotbar and the 27
   main slots), icon and name each. Click one to fit it; a part already fitted there goes back to the inventory. A
   fitted slot's list also offers "Take off". Nothing matching: "None in your inventory". Dragging a part from the
   hotbar row onto a slot works too.
5. The 3D gun changes at once: sight, barrel and grip parts as Decimation's own renderer draws them, paint as the
   gun's skin.
6. Only parts that fit are listed, by Decimation's own rules (FilteredSlot.isItemValid, FilteredSlot deci/f/k, read in
   deobf/src/decimation/gui/FilteredSlot.java): same weapon category as the gun (or "all"), the right slot, no
   suppressor on an integrally suppressed gun, no scope on a gun with a scope built in, paint made for that gun, no
   tracer on a gun with forced tracers.
7. Paint and tracer work as today. Tracer powder is used up when applied [inferred: AttachmentInventory (deci/g/s) restores barrel,
   sight, stock, grip and skin from the gun but not the tracer; check before building].
8. The gun never leaves the player's hand as far as the player can tell, and closing the screen never drops anything.
   Esc, E or I closes it. [my call: I as a toggle]
9. Singleplayer, a dedicated server and a hosted world behave the same; the server decides every fit.

Not in this version: a stats panel, presets, randomize, turning the gun with the mouse, new attachments. The user,
21 Oktober 2026: "in future we may fix grip and laser model since they dont have model on it, and at the same time we
build full new attachment update later" (docs/ideas.md item 26). Today GunItemRenderer draws a grip part at one fixed
offset for every gun (GunItemRenderer deci/K/b, renderAttachments), so grips, lasers and
flashlights may not show on the 3D gun; this screen shows whatever the renderer draws.

## 3. Look

- Mockup A: docs/shots/gunsmith_v0.65.0/ (shots_index.md "gunsmith_v0.65.0"), page
  .superpowers/brainstorm/*/content/layout.html (local).
- Decimation's in game style: black translucent slot boxes with a grey border, red (#E24848) border on the open slot and
  around the list, a red dot at each line end, white lines at about half opacity, Minecraft's font, the world dimmed
  behind.
- Lines end on the right part for EVERY gun (Decimation's 98 and ours), from model data (the muzzle point, the sight
  point, the grip offset the renderer uses, the model's bounds), never placed by hand per gun.
- Fits every GUI size from 854 x 480 (427 x 240 GUI units) to 1920 x 1080: the gun scales to the free space; slots,
  labels and the list never leave the screen or cover each other.

## 4. Constraints

- Decimation's own parts: its GunItemRenderer draws the gun (attachments and skins exactly as in game), its rules decide
  what fits, its key and its ATTACH action open the screen. Replace only what the player sees (AttachmentScreen) and how
  the container holds the gun.
- Project rules: obfuscated names only inside fixes/Deci; Forge events before jar patches; a jar patch, if unavoidable,
  goes into the three jars and passes tools/linecheck.py (both lines share one base jar).
- Cheap each frame: menu and HUD rule, no resource lookups or searches per frame.
- No lost or doubled items: the server checks every fit and take off; closing mid action, a disconnect while open,
  death, a dropped gun, a reload or shot still running when it opens, the scroll wheel or number keys changing the
  held slot while the screen is open must never copy or lose the gun or a part.
- Taking off a part with a full inventory: refused with "Inventory full" (the part stays on the gun). [my call]
- A slot the gun cannot use (built in scope or suppressor, forced tracer): shown dimmed, label "Built in". [my call]
- It is a plain feature with no multiplayer code, so it is carried to the prerelease line like other plain fixes
  (pre-releases take low to medium features, user 18 Oktober 2026). [my call]

## 5. Tests and checks

- New dev test `gunsmith` (live), written first and seen to FAIL: hold an M4A4 and send ATTACH as the key does; the
  screen opens with the held gun; click a slot the way a mouse does (DevTestUtil.clickAt), pick a part from the list;
  the gun's NBT has it (sightAttach), the part left the inventory; take it off, it is back; close: nothing dropped, the
  gun still in the same hotbar slot; no gun in hand: no screen, the message; rules: a suppressor refused on an
  integrally suppressed gun, a scope on a gun with one built in, paint for another gun not listed; shots at 854 x 480
  and 1440 x 810 for a rifle, a pistol, a sniper rifle, a shotgun and an SMG (lines end on their parts).
- The server side proven once off the integrated server (the dev dedicated server or a hosted world with a joiner).
- Unit tests for the plain layout math (slots and list inside the screen at every GUI size).

## 6. Left to the builder

How the container binds the held gun, the GL set up for the large model, how line ends come from each model, the list
panel's details, sounds (Decimation's own UI sounds), names of new classes.

## 7. When built

docs/decimation_code/weapons.md and menus.md rows, docs/ideas.md 25 marked built, docs/roadmap.md, docs/tasks.md,
shots in docs/shots/gunsmith_v<version>/ with shots_index.md, bug.md for any bug found, the graph, the prerelease carry.

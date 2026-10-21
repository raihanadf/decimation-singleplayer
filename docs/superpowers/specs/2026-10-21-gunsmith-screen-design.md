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

## 5. Performance (user, 21 Oktober 2026: "dont forget to optimize yet. you always forget this")

Optimizing is part of this feature, measured before it is called done (CLAUDE.md "Every feature: optimized before done").

- Per frame while open, only drawing: one gun through Decimation's renderer (fixes/ModelBatch keeps each part in one
  display list), the slot boxes, lines, labels and the hotbar row.
- Computed once on open and again only when the inventory or the gun changes, never per frame: the line ends (attach
  points projected to the screen), the lists of matching parts, labels, the gun's name.
- Budget: the frame time with the screen open at most 1 ms above the same view with it closed (median over 200 frames),
  in the dev client and in the production client with OptiFine. [my call: 1 ms]
- No texture load and no display list compile after the first frame open; no per frame allocation of lists or strings.
- The scope glass work (fixes/ScopeZoom: frame copy, glass measuring) must not run for the screen's gun [inferred risk:
  renderAttachments calls the scope model's renderGun].
- GL left clean: matrix stacks as deep after the screen as before, no GL errors, lighting state restored
  (fixes/GlStackGuard, fixes/EntityLighting lessons).
- Server: no per tick work for an open screen beyond the container sync; packets only when a slot changes (counted).
- Measured by the `gunsmith` dev test: frame time probe open vs closed, GL error count, texture loads per frame after
  open (0), packets per fit; numbers written into docs/decimation_code/weapons.md when built.

## 6. Tests and checks

- New dev test `gunsmith` (live), written first and seen to FAIL: hold an M4A4 and send ATTACH as the key does; the
  screen opens with the held gun; click a slot the way a mouse does (DevTestUtil.clickAt), pick a part from the list;
  the gun's NBT has it (sightAttach), the part left the inventory; take it off, it is back; close: nothing dropped, the
  gun still in the same hotbar slot; no gun in hand: no screen, the message; rules: a suppressor refused on an
  integrally suppressed gun, a scope on a gun with one built in, paint for another gun not listed; shots at 854 x 480
  and 1440 x 810 for a rifle, a pistol, a sniper rifle, a shotgun and an SMG (lines end on their parts).
- The server side proven once off the integrated server (the dev dedicated server or a hosted world with a joiner).
- Unit tests for the plain layout math (slots and list inside the screen at every GUI size).

## 7. Left to the builder

How the container binds the held gun, the GL set up for the large model, how line ends come from each model, the list
panel's details, sounds (Decimation's own UI sounds), names of new classes.

## 8. When built

docs/decimation_code/weapons.md and menus.md rows, docs/ideas.md 25 marked built, docs/roadmap.md, docs/tasks.md,
shots in docs/shots/gunsmith_v<version>/ with shots_index.md, bug.md for any bug found, the graph, the prerelease carry.

## 9. Turning the gun (user, 21 Oktober 2026)

User, after seeing the screen in runClient: "i like it how the HUD looks", then "its possible to make it more like tarkov
where it can be viewed 360 by mouse?" ("cursor i mean"). Choices (all four recommended options, AskUserQuestion): a left
drag on empty space turns the gun, it stays turned when let go, the line ends follow the parts, the wheel zooms.
tarkovbot.eu's weapon builder (checked with agent-browser) behaves the same: its slot boxes stay, its lines follow.

- Controls: a left press on empty space (no box, no list, no visible slot) followed by a move of 2 GUI pixels or more
  turns the gun, 1 degree per GUI pixel: left and right without limit (360), up and down a tilt stopping at 85 degrees
  either way. Dragging right brings the near side to the right (grab and spin). A press and release without moving
  stays a plain click; two such clicks within 300 ms reset the view (side on, zoom 1). The wheel zooms 1x to 2.5x (10 %
  a notch) when no list is open; in an open list it scrolls the list. Opening the screen again starts side on.
- Lines: the boxes stay where the side view put them. Each line's end is its part's point from the side view outline,
  set on the gun's middle plane in depth, turned with the gun and projected each frame: it stays on its part. A part
  turned to the far side keeps its line.
- The middle: the outline measuring adds one view from above (half scale, a quarter of the area read) for the gun's
  thickness, so the gun turns about its true middle. Measuring stays side on whatever the player's view; a fit keeps
  the view.
- Performance: while turning a frame is the same one gun draw plus six points turned in Java; no depth read, no text,
  no packet. Budget: frame time while turning at most 1 ms over the closed screen; the measuring on open grows by the
  top view (depth pixels at most 2.5 times the gun area).
- Checks (dev test gunsmith, written first, seen to FAIL): a drag turns the gun and it stays turned; at several angles
  and zooms every line ends on the gun and the gun turns about its middle, both checked against a real render of that
  view; a click on a box still opens its list and does not turn; a plain click on empty space does not turn; a double
  click resets; the tilt stops at 85; the wheel zooms within 1 and 2.5 and leaves the zoom alone with a list open;
  turning reads no depth and builds no text; a fit keeps the view; perf: frame time while turning each frame, GL clean
  while turned. Dev client, then the production client with OptiFine; carried to prerelease (a plain feature).

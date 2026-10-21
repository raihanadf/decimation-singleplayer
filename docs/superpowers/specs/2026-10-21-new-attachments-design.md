# New attachments: RK-1 foregrip and SpecterDR 1x/4x (design, 21 Oktober 2026)

Status: draft for the user's approval. Part of docs/ideas.md item 26 (full attachment update).

## 1. Intent

User, after the reference capture tool ran on their Tarkov build: "i guess we have 2 new attachment: new grip, and 4x
scope". The two parts of that build (docs/shots/refcapture_v0.65.0/my_preset, parts.txt): the B-25U RK-1 (a Zenit
RK-1 vertical foregrip on a B-25U mount) and the SpecterDR (an ELCAN SpecterDR 1x/4x combat scope) [names inferred
from the builder's part names and the pictures]. Both become attachments of our own, modelled in Decimation's style
from those references (never copied: the pictures are Battlestate Games' models).

The user had also asked earlier for grips and lasers to show properly ("in future we may fix grip and laser model since
they dont have model on it"). Checked 21 Oktober 2026: Decimation draws every grip slot attachment (Foregrip, Laser
Marker, Flashlight) at ONE fixed offset for all guns (GunItemRenderer.renderAttachments, translate -0.2, 0.05, 0); on
the M4A4 the Foregrip lands behind and below the stock (study.py attach m4a4 foregrip). This design fixes that too.

## 2. What the player gets (user choices, AskUserQuestion 21 Oktober 2026)

- **RK-1 Foregrip** (new item): fits the guns Decimation's Foregrip fits (category rifle) [my call: same rule]. Its own
  numbers (user): recoil UP cut to 1/2, SIDEWAYS cut to 1/4 (Decimation's Foregrip cuts both to 1/3). The view's kick
  is calmed like the Foregrip's (GunItemRenderer's foregrip branch) [my call].
- **ELCAN SpecterDR 1x/4x** (new item): a sight that switches between 1x and 4x (user: "1x/4x switch"), key G by
  default (user), rebindable in Controls. 1x: as close as the red dots (ScopeZoom 1.25); 4x: 4.0, with the gun and the
  see-through glass kept (no black sniper overlay, as our ACOG) [my call: a combat optic, not a sniper scope]. The same
  reticle in both, drawn in the glass (SightReticle). The mode is per gun, remembered for the session [my call]; a
  switch plays a click [my call]. Fits every gun, as the ACOG (Decimation's category "all", Deci.newSightAttachment).
- **Grips placed per gun, every gun** (user): the RK-1, Decimation's Foregrip, Laser Marker and Flashlight sit under
  each gun's own handguard / rail, not at one spot.
- **Loot** (user): the same lists as Decimation's 4x (SpecterDR) and Foregrip (RK-1), the way the ACOG joined the
  4x's lists (Deci.addLootLike), and our LootPools beside them.
- Both work in the Gunsmith screen like any part (Decimation's FilteredSlot rules through Deci.fitsSlot).

## 3. How (decided)

- Items: AttachmentItem instances of ours registered as deci:<name> like NewSights (Deci.newSightAttachment for the
  sight; a grip variant for the RK-1); names in our lang file; icons from the model render (tools/guns pipeline).
- Models: built in code in Decimation's style (skill decimation-gun, docs/gun_style_guide.md), proportions from the
  refcapture views; close ups of the two parts taken by refcapture (a focus option: the view panned and zoomed onto
  one part) [builder]. Compared side by side with the references before showing the user.
- Placement: no jar patch. Each attachment's model cache per gun (AttachmentItem.getModelFor) gets a shifted copy for
  each gun (Deci.offsetAttachment, as SightPlacement does for sights); a fit tool like sightfit.py measures each gun's
  handguard bottom in its front half and writes the grip offsets file. The SpecterDR goes through sightfit like every
  sight.
- Recoil: a jar patch (Javassist, the shared base jar, three jars, both lines) on GunItem's two recoil lines and
  GunItemRenderer's kick branch: they ask a hook of ours (fixes/GripRecoil) for the factors instead of comparing with
  Decimation's Foregrip; the hook answers 1/3 for the Foregrip (unchanged), 1/2 up and 1/4 sideways for the RK-1, 1
  otherwise. The hook class exists on main and prerelease (CLAUDE.md: both lines share one base jar). GunItemRenderer
  (deci/K/b) already carries PatchScope and PatchIronSights: the new patch goes on top of them, in the three jars.
- The switch: a KeyBinding of ours; ScopeZoom asks the SpecterDR's mode for its magnification; client only (the zoom
  is a view effect) [inferred: Decimation's server does not read sight zoom; checked at build]. The mode is kept per
  gun by the gun's own id in its tag where Decimation sets one, else per hotbar slot [builder].

## 4. Non goals

Lasers that draw a beam, flashlights that light, new laser or flashlight models, other new attachments (each later,
item 26); the switch for other scopes; changing Decimation's Foregrip numbers.

## 5. Performance (CLAUDE.md "Every feature: optimized before done")

- Per frame: the grip and the sight are drawn as Decimation draws attachments (one cached model each, the per gun copy
  made once); no new work per frame; the key handler reads one key state a tick.
- Models within Decimation's part counts for attachments (docs/gun_style_guide.md); scopeperf with the SpecterDR at 1x
  and 4x: frame time no worse than the ACOG's; GL clean (glstack); the production client with OptiFine (scope).
- Server: nothing per tick; recoil stays client side as Decimation's.

## 6. Checks (written first, seen to FAIL)

- Dev tests: both items registered and in the loot lists of the 4x / Foregrip; the Gunsmith lists the RK-1 for a
  rifle and not for a pistol, the SpecterDR where the ACOG fits; recoil per shot with no grip, the Foregrip and the
  RK-1 (pitch and yaw ratios 1, 1/3 and 1/3, 1/2 and 1/4); every gun's grip offset puts the grip under its handguard
  (the drawn grip touches the gun and hangs below it, study.py contact for every gun and grip); the G key switches the
  SpecterDR's magnification 1.25 and 4, per gun, and 4x keeps the gun drawn (no overlay); the reticle in the glass;
  scopeperf and gunperf with both; shots of each grip on a rifle, an SMG, a pistol (Laser) and the SpecterDR aimed at
  both modes, compared with the references.
- Production client with OptiFine; a dedicated server run (recoil and drawing do not depend on singleplayer).

## 7. Left to the builder

Item registry names and display names, the exact model detail and textures (Decimation's flat dark tones), the
reticle's look, the grip fit tool's measuring rule, the focus option of refcapture, the click sound.

## 8. When built

docs/decimation_code/weapons.md (attachments section), docs/ideas.md 26, CLAUDE.md, the decimation-gun skill (grip
lessons), docs/tasks.md, shots in docs/shots/attachments_v<version>/ with shots_index.md, bug.md for the grip placement
fault, the graph, the prerelease carry (a plain feature).

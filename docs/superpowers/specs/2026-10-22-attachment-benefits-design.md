# Attachments with benefits (feature 9; docs/ideas.md 40)

User, 22 Oktober 2026: Tarkov style stats, a small plus and minus each: sights aim faster or slower by size, suppressors
quieter and no flash but a little less damage, stocks less recoil, lasers tighter hip fire, grips as now; shown in the
Gunsmith screen.

## Facts
Grips already change recoil (fixes/GripRecoil, PatchGripRecoil), suppressors noise (fixes/GunNoise); sights zoom
(ScopeZoom); the Gunsmith screen (fixes/gunsmith) shows the held gun with its six slots; damage per hit is
GunItem.setDamage's (deci/ay/i), the player's shot damage reaches us in LivingHurtEvent with the "gunDeci" source.

## Design
- fixes/AttachmentStats: one table (attachment name -> stat changes in percent), editable as
  config/deciworldgen_attachments.json, defaults:
  red dot aim speed +10 %; 2x 0; EOTech -5 %; ACOG / SpecterDR 4x -15 %, 4x and up -25 %; suppressors noise range to the
  existing suppressed value, no muzzle flash, damage -5 %; stocks recoil -10 % (where a gun has a stock slot); lasers hip
  spread -25 %; flashlight none; grips as GripRecoil.
- Aim speed: ScopeZoom's zoom ramp and the aim movement scaled; damage: our LivingHurtEvent on "gunDeci" by the
  shooter's held gun's attachments (server side, both lines); hip spread: Decimation's spread value for the held gun
  while not aiming (a hook like PatchGripRecoil's, only if no event reaches it [to check in code]).
- Gunsmith screen: a stats panel under the slots (aim speed, recoil, hip spread, noise, damage) with the change each
  attachment makes, green plus and red minus, in Decimation's font.

## Performance
Stats of the held gun computed when its attachments change (cached by the stack's attachment tag), read per shot and
per frame as numbers; nothing allocated per frame.

## Tests
Unit: the table and the sums per gun. Dev tests: aim time to 95 % zoom per sight (aimdelay), damage per shot with and
without a suppressor (playerfire), hip spread spread of 50 shots with and without a laser, recoil with a stock; the
Gunsmith panel shot; attachmentsmp on the dedicated server.

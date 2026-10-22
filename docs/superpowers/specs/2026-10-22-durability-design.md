# Durability of armor, vests and guns, with repair parts (feature 10)

BUILT 22 Oktober 2026 (new_feature.md "Durability of armor, vests and guns"); differences from this spec are noted there.

User, 22 Oktober 2026: "armor durability highly nerf to 256 and they will be reduce by hit or shot, 1 shot = 1
durability. armor now can be repaired with armor part and for vest durability is 512 and need armor plate item that can
be find it on military or police crate. guns now has durabitlity and they need to be repaired by guns part."
Choices the same day (docs/ideas.md 47, 48): at 0 an item is kept but stops working until repaired; one part repairs a
quarter; guns wear 1 a shot, jam now and then below 25 %, will not fire at 0; parts in military and police crates only;
vests (which protected nothing until now) protect body shots, the stronger of the vest and the chest piece counting.

## Facts it rests on (read 22 Oktober 2026)
- Armor: ItemArmorDeci extends vanilla ItemArmor (CHAIN material, own max damage); gunshot protection is its
  damageMultiplier: Decimation's player hit handler (PacketGunHit, deci/aE/a$z) multiplies armor slots 0 to 2 (boots,
  legs, chest) for every hit, fixes/ArmorGunfireHandler does NPC shots and adds the helmet on headshots. Gun damage
  sources bypass vanilla armor, so gunshots never wore armor; melee hits wear it the vanilla way (damage / 4, at least
  1, through InventoryPlayer.damageArmor), and vanilla destroys a piece whose damage passes its maximum.
- Vests: ItemVestDeci (an ItemArmorDeci) in PlayerData's vest slot; multipliers 0.455 (military) to 0.6175 (press);
  read by nobody. Masks (ItemMaskDeci, gas masks, goggles, balaclava) are left out of this feature.
- Guns: a shot is GunItem.fire on the client (fireRay, PacketGunHit, PacketGunFire), then on the server the hit handler
  and PacketGunFire, which calls GunItem.onServerFire once a shot. Firing is decided in clientHeldTick (ammo, rack
  animation, fire mode): no hook of Forge's in between. R is Decimation's keyReload, read with isPressed() in its client
  tick. Racking is Decimation's Rack animation (GunItemRenderer.currentAnimation, "<gun>Rack.anib").

## Design
1. Numbers (fixes/Durability, pure, unit tested): armor pieces 256 (juggernaut pieces too), vests 512, guns by category:
   rifle 1500, SMG 1800, pistol and revolver 2500, shotgun 1200, machine gun 1000; launchers and crossbows do not wear.
   A part repairs a quarter of the maximum. Jams: below 25 % left, 2 % a shot. Broken = used up (damage at maximum).
2. Armor and vests keep vanilla's item damage (maximum set to 256 / 512 at postInit for every ItemArmorDeci but masks),
   so the vanilla durability bar shows. fixes/ArmorWear (server, LivingHurtEvent at LOWEST, after our gunfire handler):
   - a gunshot ("gunDeci", "human", "turret") wears 1 on each piece that counted: body pieces on a body shot, the
     helmet on a headshot, the vest instead of the chest piece when the vest counted;
   - vanilla's own melee wear stays, clamped so that it stops at the maximum (the piece is never destroyed);
   - a broken piece protects nothing: its gunshot factor is taken out again (Decimation's handler applied it); against
     blockable damage Forge's ArmorProperties.ApplyArmor already caps a piece's absorption at its durability left + 1,
     so a used up piece absorbs next to nothing (the planned armor point compensation is not needed);
   - the vest: against a body shot the stronger of vest and chest counts (Decimation's handler applied the chest:
     replaced by the vest's factor when the vest is stronger); a broken vest counts for nothing.
3. Guns: NBT deciworldgen_wear (rounds fired) and deciworldgen_jammed. A jar patch (tools/patches/PatchGunWear.java,
   three jars, on top of PatchGripRecoil's GunItem): GunItem.fire starts with `if (GunWear.blocked(stack, player))
   return;` (client: a jammed or broken gun clicks, a line tells why), GunItem.onServerFire starts with
   `GunWear.onShot(stack, player);` (server: +1 wear, the jam roll); both calls guarded by a catch of
   NoClassDefFoundError so a checkout without GunWear keeps working (the base jar is shared by every checkout).
   Racking: with a jammed gun in hand R plays Decimation's Rack animation and sends our message 3; the server clears
   the jam (a press of R is taken by our client tick before Decimation's, only while jammed).
4. Items (ours, pixel icons in Decimation's style): deciworldgen:armor_part "Armor Repair Kit", deciworldgen:armor_plate
   "Armor Plate", deciworldgen:gun_parts "Gun Parts". A right click on the server repairs a quarter: the armor part the
   most worn piece worn, the plate the vest, gun parts the most worn gun in the hotbar; one used, a sound, a line;
   nothing to repair uses nothing. Loot: military and police crates (Deci.addLootLike after military / police items),
   about as common as a magazine; never civilian.
5. Tooltips (client): "Durability N / M" on armor, vests and guns, "Broken: repair with ..." in red, "Jammed: press R".

## Performance (budgets)
- Server: work only on a hurt event of a player (a few slot reads) and once a shot (NBT); nothing a tick.
- Client: the fire hook once a shot (two NBT reads); the R check once a tick only with a gun in hand (one NBT read);
  tooltips only when shown. Measured with the dev test (handler times) and written into new_feature.md.

## Tests
- Unit: Durability numbers and math (maxima, repair steps clamped, jam chance only below 25 %, broken at the maximum,
  the vest and chest choice).
- Dev test `durability` (arena, mob spawning off): NPC shot and player shot wear each counted piece by 1; a broken
  piece protects nothing; vest choice; melee wear stops at the maximum (never destroyed); a gun wears 1 a shot (real
  shots, Deci.fireHeldGun), jams below 25 % (forced roll), a jammed gun does not fire and R clears it; a broken gun does
  not fire; each part repairs a quarter and is used up, nothing to repair uses nothing; loot rolls give parts in
  military crates and none in civilian ones. Then the production client with OptiFine; a dedicated server run of the
  rack message (channelmp style) [planned].

# Military airport in a wasteland city (design, 14 Oktober 2026)

User idea (docs/ideas.md item 18, 13 Oktober 2026): "airport military base with barracks, HQ, tower, aircraft
park and hangar on wasteland". Choices taken (asked with options, 14 Oktober 2026):
- size: HALF a wasteland sector (2 districts side by side, 2 x 4 cells), any city with a wasteland sector can
  get one (about 1 in 3);
- aircraft: planes BUILT FROM BLOCKS (fighters, a cargo plane) plus Decimation's helicopter wreck on a pad;
- state: HELD AND HALF WRECKED: the enemy military still holds it (garrison posts like the bunker, elite and
  juggernaut, military infected), but a plane crashed on the runway, a hangar burnt, barricades;
- loot: BUNKER LEVEL IN PLACES: HQ and armory locked (keycard), top military loot inside; hangars and barracks
  normal military loot.

## 1. Reference (researched 14 Oktober 2026, second pass after the user's review "to basic")
- Runway, taxiway, aprons, hangars beside the aprons, tower, fuel stores with a road loop, fuel bladders (NCAP
  "Anatomy of airfields" https://www.ncap.org/news/2026/anatomy-of-airfields; SLDinfo FARP
  https://sldinfo.com/2026/06/fueling-the-forward-edge-the-farp-practical-application-at-wti-2-26/). Main runways
  about 45 m wide (ACP 31 https://scottbouch.com/rtfm/uk/sequential/acp/31/b1/al6/acp31-b1-s3-c1.pdf); layout
  rules UFC 3-260-01 (https://www.wbdg.org/node/23093).
- Lighting (aeroclass https://www.aeroclass.org/runway-lights/, IVAO https://wiki.ivao.aero/en/home/training/documentation/Aerodrome_Lighting_System,
  OCEM https://ocem.com/en/all-about-airfield-ground-lighting/what-lights-are-part-of-the-airfield-ground-lighting-set-up-at-an-airport/):
  white approach light bars from the threshold outward (centreline + crossbars), green threshold lights, white
  edge lights about 60 m apart, red end lights, PAPI: four boxes beside the touchdown zone (two red two white =
  on the glide path), blue taxiway edge lights.
- Arresting gear BAK-12 (af.mil https://www.creech.af.mil/News/Article-Display/Article/1017474/nttrrss-certifies-arresting-system/,
  https://www.ramstein.af.mil/News/Features/Display/Article/727167/786th-ces-barrier-mx-team-leash-aircraft):
  an energy absorber housing on each side of the runway, a pendant cable across it on supports, fired from the
  tower.
- Hardened aircraft shelters (AFMAN 141182 https://stg.wbdg.org/FFC/AF/AFMAN/141182_Hardened_Aircraft_Shelters.pdf;
  Airfield Research Group https://www.airfieldresearchgroup.org.uk/forum/airfield-discussion/261-uk-cold-war-airfield-structures):
  steel lined concrete about 0.46 m thick, an arched shell, one aircraft each (first and second generation),
  heavy sliding blast doors in two leaves on rails at one end.
- Revetments (Wikipedia https://en.wikipedia.org/wiki/Revetment_(aircraft); Bagram
  https://en.wikipedia.org/wiki/Bagram_Airfield, https://www.globalsecurity.org/military/world/afghanistan/bagram.htm):
  a parking pad with blast walls on three sides; Bagram had over 110 revetments, five dispersal areas, three
  large hangars. Soviet model bases (Air Power Australia https://www.ausairpower.net/APA-2008-02.html; CIA Tutow
  https://www.cia.gov/readingroom/node/1997290): earth revetted hardstands, pads for up to three fighters,
  dispersal taxiways.
- Aerospace ground equipment (af.mil https://www.shaw.af.mil/News/Article-Display/Article/1734808/age-supports-mission-from-ground):
  diesel and turbine generators, hydraulic test stands, nitrogen carts, air conditioners, heaters, jacks,
  maintenance stands, light carts, bomb lifts; hundreds of pieces per base.
- Munitions storage (Coffman https://www.coffman.com/portfolio_items/p-3105-munitions-storage-igloos/; Ellsworth
  https://wearethemighty.com/mighty-history/a-rare-look-beyond-the-gates-at-ellsworth-afb): earth covered
  magazines (igloos): an arch under at least 0.6 m of soil, a concrete headwall with blast doors, built side by
  side but apart.
- Radar (FAA ASR-11 https://www.faa.gov/air_traffic/technology/asr-11/): the rotating antenna on its own tower
  (8 to 23 m) in a fenced plot with an equipment shelter at its foot, apart from the control tower.

## 2. In Minecraft (Decimation props, docs/prop_catalogue.md)
| Real thing | Built from |
|---|---|
| runway / taxiway / apron | deci:BlockRoad asphalt; concrete BlockStone_7 aprons with darker joints; white paint wool 0 (white clay reads pink); yellow clay lines |
| lights | glowstone flush under stained glass: white edge, lime threshold, red ends, blue taxiway edges; approach bars of white glass lights on posts beyond the thresholds |
| PAPI | four small boxes on legs beside the touchdown zone, red / white wool faces |
| windsock | iron bar mast, an orange and white wool sock |
| arresting gear | BlockMetalWall housings 2 x 3 each side, black carpet cable across the runway |
| runway numbers, touchdown marks | white wool pixels (3 x 5 digits) |
| hardened shelter | concrete arch (BlockStone_7), blast doors of BlockMetal_3 half open on rails (rail block) |
| revetment | deciworldgen:hesco walls 3 high, U open to the taxiway |
| aircraft | wool fighters and cargo plane (section 4) |
| ground equipment | generators, light carts (BlockHazardLight), bomb trolleys (slab carts with coal block bombs), a fuel truck and a tow tractor from blocks, wheel chocks (slabs), FOD bins (BlockTrashcan), toolboxes (BlockElectricBox1) |
| fire station | concrete, two bays, a red wool crash truck, hose rack, lockers, beds |
| radar | BlockRadioTower lattice 8 high, a block dish on top, equipment shelter, fence |
| igloos | stone arch covered by grass on dirt, concrete headwall, metal blast doors, ammo cases and crates inside |
| loot | as the FOB: weapon cabinets, large ammo cases, military crates, care packages, footlockers |

## 3. Layout (canvas 112 x 240, runway along z, the gate north)
| x / z | what |
|---|---|
| x 0 | perimeter fence, concertina ring inside; HESCO towers at the corners and over the ECP |
| x 4..25 | runway strip: shoulders, runway x 8..21, approach bars z 1..9 and 230..238, arresting gear z 34 / 205, PAPI beside z 48 / 191, numbers and touchdown marks |
| x 26..33 | infield grass: windsock, hold short lines on the stubs, taxiway signs |
| x 34..41 | taxiway, blue edge lights, three stubs |
| x 42..71 | apron: four HESCO revetments with fighters and their ground equipment (z 50..110), the cargo plane hardstand (z 116..142), helipad (z 148..164), fuel truck, tow tractor, bomb trolleys, light carts (z 168..196) |
| x 72..77 | landside road |
| x 78..110 | ECP side and fuel farm (z 12..44), two hardened shelters (z 50..72, 78..100), the burnt maintenance hangar (z 106..132), fire station and radar (z 136..154), tower and HQ (z 158..178), barracks with bunkers (z 182..200), the munitions area with igloos and the armory (z 204..236) |

## 4. Aircraft from blocks
- Fighter (about 13 long, 9 wide, 4 high): grey clay fuselage, glass canopy, slab wings and tailplanes, a fin,
  coal block intake and nozzle, gear legs of fences.
- Cargo plane (about 20 long, 22 span, 6 high): hollow fuselage 3 x 3 with the rear ramp down (walk in), high
  wing, four engine pods, crates and a care package inside (loot).
- Wrecked fighter on the runway: broken in two, a wing off, black and grey scorch, debris, burnt ground.

## 5. Held and half wrecked
- Garrison posts (worldgen/Garrisons as at the bunker): the tower top, the ECP, hangar doors, the HQ; elite and
  juggernaut tiers; a spawner of military infected in the burnt hangar.
- Damage: the crashed fighter on the runway, the burnt hangar (black clay patches, roof holes), sandbagged
  positions and barricades across the taxiway, a few scorch marks (no holes: the user rejected the ditch).

## 6. Loot
- HQ and armory: Door_Emergency_1_Locked + military keycard screen (as the TOC and wasteland buildings), weapon
  cabinets, large ammo cases, military crates (Decimation's loot lists by block).
- Hangars, barracks, cargo plane: military crates, ammo cases, care packages, footlockers.

## 7. Checks (before showing the user)
- Canvas.validateProps 0, tools/props/propclash.py 0, tools/props/walkcheck.py (every door, ladder, loot
  reachable from the gate), dev test `airport` shots (sky), servertest with `lc airport at` log and a top
  down block picture (tools/topdown_blocks.py).

## 8. Built (v0.62.0, 14 Oktober 2026)
- Code: city/Airport (sites: one per city at most, 1 city in 3, the city's wasteland sector with the lowest hash,
  the half facing away from the city; no river cell; block kind 3 of 2 x 4 cells, LcCity.block / ground /
  planAirport), military/Airport (the canvas builder), military/AirportPlan (FixedBase, Garrisoned: 15 posts,
  1 spawner; the boom arm turned with the plan), CityLayoutEvents (StructureData `!airports`, new worlds,
  -Ddeciworldgen.airports). WastelandFence: no gate where the street beyond a fence line is an airport (its own
  fence closes it). Rails: no station segment in an airport block (the stairs came up through a hangar).
  TerrainEvents: no lakes and no springs over or next to a city (a lake carved through the airport ground, a
  spring ran over its road).
- Layout as section 3, with the perimeter fence on the outermost row; white paint and the planes in wool (grey
  and white stained clay read pink-brown in Decimation's pack), yellow lines in clay (reads yellow).
- Checked: dev test `airport` (sky at -20000, 230, -18300, no garrison): clashes 0, propclash 0 on 118 props,
  walkcheck: every ladder, door and loot reachable, the HQ and armory doors locked (keycard); shots in
  docs/shots/airport_v0.62/. Seed 1: `servertest.py 1 type=decimation gatescan=0,0,3000` lists 4 airports
  within 3000 blocks (2 along z, 2 along x); the ones at -2544,64,1296 (runway along z) and 272,64,-2032
  (along x) generated and read back: no water, no holes in the ground, garrison registered (15 posts, 1
  spawner), top down pictures topdown_alongz.png / topdown_alongx.png.
- Not checked: the garrison manned in game (posts spawn on a player's first visit), how it feels to walk in.

## 9. Rework after the user's review (v0.62.1, 14 Oktober 2026)
User on v0.62.0: "hmm, to basic. are you using the skills before? and you still remember how to make structure
correctly and really detailed?" Cause: built from memory without the decimation-military-base skill (2 searches,
fixed boxes, big empty concrete). Redone through the skill: sections 1 to 3 rewritten from 13 sources, then:
- every module reserves its rectangle (Airport.reserve: an overlap logs "refused ..." and fails the test);
- runway: numbers 18 / 36 (3 x 5 pixels, readable from each approach), touchdown bars, aiming blocks, edge and
  threshold bars, dashed centreline; lights: edge every 12, green threshold glass, red end glass, approach light bars
  on posts beyond both ends; PAPI (two white, two red); arresting gear housings both sides with the cable across;
- infield windsock; taxiway blue edge lights, hold short lines and signs on the stubs;
- apron: four HESCO revetments (fighter nose out with a chock, generator, light cart, bomb trolley, toolbox, crate;
  one bay empty with oil stains, one burnt with a wrecked fighter and a body), the cargo stand with pallets and a
  care package, the helipad with corner lights, the equipment yard (block built fuel truck and tow tractor, bomb
  trolley rows, generators, light carts, FOD bins), floodlights;
- landside: FOB style entry point (drop arm with its lamp, serpentine, sandbag booth, overwatch tower with a
  searchlight, vehicle search pad with a jeep), fuel farm (bladders in a berm, pump house, fuel lines, drums), two
  hardened aircraft shelters (concrete arch, blast door leaves half open on their track; one a maintenance bay with
  a wing panel off, the engine on a stand, a work stand), the burnt maintenance hangar (spawner), a crash fire
  rescue station (two bays, red crash truck, hose coils, crew room), a surveillance radar (fenced plot, lattice mast
  with the antenna, equipment shelter), the tower with a catwalk and consoles, the HQ in three rooms (operations
  with map tables, the commander's office, comms) behind a blast wall, two B-huts and a duck and cover bunker, the
  munitions area (HESCO wall, three earth covered igloos with loot, bomb trolleys, the locked armory); guard
  towers like the FOB's at three corners and over the entry.
- Checked on the dedicated server (the user's game was open, so no client shots): seed 1 airport at -2544,1296
  generated, no clash or refusal logged, propclash 0 findings on 248 props, walkcheck reaches every cell except the
  two keycard doors (HQ, armory), garrison registered. Top down: shots airport_v0.62/topdown_v2_alongz.png.
- Not checked: in game shots of the rework (client test not run while the user plays), turns 3 (runway along x) in
  the walk test.

## 10. Sky shots of the rework and fixes (v0.62.2, 14 Oktober 2026)
The user closed their game; dev test `airport` ran live in deciworldgen_devtest, turns 0 and 3. Shots in
docs/shots/airport_v0.62.1/ (shots_index.md). Read every shot; found and fixed:
- the entry point and HQ flags were BlockFlagPollUSSR (red Soviet flag) on a US style base: now BlockFlagPollUAHD,
  the flag of the FOBs and gates;
- the hardened shelter blast door leaves were BlockMetal_3 (rusty brown: stepped brown stacks, not doors): now
  camouflage green BlockMetalWall with a BlockMetal_2 rib every 4 high;
- the radar plot fence was oak fence (reads as a farm): now iron bars (chain link, like the city fence);
- the landside road's dashed centre line (BlockRoad_CenterLine: metadata 4 runs along z, 2 along x) was not turned
  with the plan: with the runway along x (turns 3) the dashes showed as ticks across the road. AirportPlan.blockAt
  swaps 4 and 2 on odd turns, like the boom arm;
- camera points that showed nothing: hq_ops faced the partition wall, hq_office the corridor wall, armory stood
  inside an igloo mound, radar on the HQ roof looking at concrete. Now: ops from its south east corner toward the
  desks, office toward the desk and cabinets, armory inside from its north west corner, radar from the street
  between the plot and the HQ.
- Checked after the fixes: turns 0 and 3: clashes 0, propclash 0 on 247 props, walkcheck reaches every cell but the
  two keycard doors (HQ 91,1,167 and armory 100,1,211 in turns 0); turns 3 dump: 109 centre line cells all metadata 2.
- Seen and left as is: the revetment fighters are small seen nose on from the bay mouth; the helipad camera has the
  cargo plane's wing tip in its foreground; the radar shot has the fire station's side wall on its left.
- Not checked: the garrison manned in game (posts spawn on a player's first visit; the sky test has no garrison
  keeper), a real worldgen airport in the client.

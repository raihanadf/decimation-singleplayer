# Graph Report - .  (2026-10-14)

## Corpus Check
- 12 files · ~528,002 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 4146 nodes · 9142 edges · 307 communities (167 shown, 140 thin omitted)
- Extraction: 92% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 682 edges (avg confidence: 0.83)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Deci: Deci
- StreetDress: StreetPlan
- CityGate: CityGate
- ?: net.minecraft.world.World
- LotDress: LotDress
- SurvivorCamp: SurvivorCamp
- ?: cpw.mods.fml.common.eventhandler.SubscribeEvent
- NpcLoadouts: NpcLoadouts
- LcContent: LcContent
- ServerChecks: ServerChecks
- ScopeZoom: ScopeZoom
- architecture: Bug tracker (bug.md)
- Building: Building
- hwmap: hwmap.py
- StructureGenerator: StructureGenerator
- new_feature: NPC tiers design and result (v0.30.0): tier per armed NPC on first join (gear, gun, health, fire rate, share of player gun damage), stored in entity data, gun synced via data watcher slot 26; bandit light / medium / heavy, soldier camo sets, Soviets as enemy military
- SchematicPlan: SchematicPlan
- SafezoneSites: SafezoneSites
- Deci: net.minecraft.entity.player.EntityPlayer
- LootTest: LootBody
- interior_spec: Building interior and exterior spec (round 2 step 4c, approved 7 Oktober 2026)
- performance: Performance doc: tools (gunperf, census, perfcheck, JFR via jcmd), findings, checklist
- DeciBiome: DeciBiome
- PropFacing: net.minecraft.block.Block
- city_engine: City engine: Lost Cities style cities from converted DeceasedCraft content
- Wasteland: Wasteland
- CanvasPlan: CanvasPlan
- SurvivorCamp: SurvivorCamp
- StatsTest: .client()
- make_test_schematics: make_test_schematics.py
- worldgen: Multiblock props (0.16.0): generation completes Decimation multiblocks (tile entity extends deci.W.a MultiblockPart) whole
- Shell: .unit()
- BuildingPlan: BuildingPlan
- architecture: Bug tracker (bug.md)
- PropFit: PropFit
- GunTest: DevTestMode
- CLAUDE: decimation-singleplayer README (public repo overview)
- prop_catalogue: Decimation prop catalogue (look, size and facing of every deci: block, from the prop gallery)
- BuildingDress: BuildingDress
- LcCity: LcCity
- CityRefugePlan: CityRefugePlan
- lcstudy: dcinventory.py
- bug: Bug tracker (bug.md)
- CameraViews: CameraViews
- new_feature: Current state and pending decisions
- worldcheck: worldcheck.py
- architecture: ServerProxy (deci.a.e, dedicated only)
- ?: DecimationWorldGen
- EdgePlan: EdgePlan
- new_feature: Current state and pending decisions
- WorldClock: CoreTest
- worldgen: World generation doc (deciworldgen)
- ZombieEgg: Override
- Updater: Updater
- Areas: Areas
- worldgen: Lot grading (city yards, 0.13.0: Graded plans own the 26x26 lot, Slices grades every lot column before writing)
- NpcTest: .batches()
- CLAUDE: Testing without the user (devtest.py first choice, servertest, worldcheck, worlddiff, autotest flags)
- Slices: Slices
- ZoneStore: ZoneStore
- bug: Bug tracker (bug.md)
- NoiseTest: net.minecraft.entity.EntityLiving
- building_design: v0.10.0 to v0.12.3 city blocks, large schematics, city v2 and fixes status
- StoreyPlan: StoreyPlan
- Highways: Highways
- worldgen: Lot grading (city yards, 0.13.0: Graded plans own the 26x26 lot, Slices grades every lot column before writing)
- ZoneRules: ZoneRules
- CameraViews: CameraViews
- gun_style_guide: Decimation gun style guide: study of all 98 shipped guns (look and construction rules for our own guns)
- Canvas: Canvas
- NoiseTest: NoiseTest
- FurnitureSets: FurnitureSet
- LotPlan: LotPlan
- DeciBiome: DeciBiome
- ?: cpw.mods.fml.relauncher.SideOnly
- ?: DecimationWorldGen.java
- wasteland: docs/wasteland.md: the wasteland district as the city's military area (v0.54.0 to v0.56.0)
- Capture: Capture
- RiverPlan: RiverPlan
- safezone: Safezone sites doc: survivor camps (references, layout, residents, placement, test), fortified city building next
- floorplan: Building quality audit (round 2 step 4a, baseline of v0.16.0 city buildings)
- loot_levels: Crate levels and NPC loot: design draft (13 Oktober 2026)
- SKILL: decimation-gun skill: 10 lessons for every gun from user reviews (ask for real photos, measure, Decimation look, accessories like the real gun, flamePos above bore, short gun attachments, aim side by side, clear line of sight, user screenshots win, show renders first)
- ClickTest: FireRateTest
- military_base: Military bases design doc: US FOB style bases of our own in three sizes (COP 50x58, FOB 78x84, large FOB 112x118)
- LootPools: LootPools
- UpdateScreen: UpdateScreen
- Furnisher: Furnisher
- BuildingLayout: BuildingLayout
- LargeSites: LargeSites
- hk416: hk416.py
- LcContent: LcContent
- InfectedVariants: InfectedVariants
- Dress: Dress
- hwmap: hwmap.py
- systems_plan: Systems plan: loot, human NPCs, zombies, world planned together (13 Oktober 2026)
- BlockBoomArm: BlockBoomArm
- furniture_sets: Furniture sets doc: data driven JSON furniture groups, user editable
- UpdateTest: .client()
- BiomeMap: BiomeMap
- worldgen_architecture: Worldgen architecture v3 draft (layers, assets, size classes, capture tool)
- city_engine: City engine: Lost Cities style cities from converted DeceasedCraft content
- ideas: Ideas for later (user, 12 Oktober 2026)
- SKILL: City rivers and bridges v0.45.0 / v0.45.1: river contour through cities as canal chunks, street chunks carried over on decks, river cells level 0, new worlds only
- SightPlacement: .draw()
- city_engine: City highways: seed based network between city regions, open / bridge / tunnel chunks, deck at 64, side ramps
- lctranslate: lctranslate.py
- TerrainEvents: TerrainEvents
- Deci: .onServerTick()
- zones: Zones doc: Decimation's zone system end to end and what ZoneRules makes work in singleplayer
- CLAUDE: tools/build.py real javac pipeline
- DevTestLive: DevTestLive
- gun_model_spec: Our own guns pipeline (gun_model_spec section 6): tools/guns spec to .bmodel / texture / icon / .anib, bbmcp previews, NewGuns registration (newGun, newMagazine, addLootLike, useGunSounds), dev test mode gun
- Shell: Shell
- DevAutoTest: DevAutoTest
- PlaceNames: PlaceNames
- CityRivers: CityRivers
- StructureData: StructureData
- CityLayout: CityLayout
- Garrisons: Garrisons
- lc2schem: lc2schem.py
- lcpack: lcpack.py
- bug: Bug: armor buff ignores NPC gunfire (fixed)
- bug: Bug: armor buff ignores NPC gunfire (fixed)
- PatchTurfNames: PatchTurfNames
- DecimationBiomes: DecimationBiomes
- AssetDir: AssetDir
- CityLayoutEvents: CityLayoutEvents
- city_engine: City engine: Lost Cities style cities from converted DeceasedCraft content
- UpdateSwap: Auto update from our GitHub releases (v0.59.0): Updater check, Decimation's Update button via PatchNoUpdate v2, UpdateScreen progress, sha256 check, UpdateSwap after the game closes
- bug: Done v0.30.3: NPC shots traced with spread per tier, stopped by walls, impact particles, tracer always visible along the real line (PatchTracer v2 shotHook + NpcShots)
- SightPlacement: .draw()
- gun_style_guide: Gun aiming: aim mode draws every gun at one fixed place (x 0.5, translate -1 -0.35 0.923), sPos unused in first person; screen centre at the top of the iron sights, y -4.85 to -5.0, z -0.15
- Doors: Doors
- GunNoise: Override
- Protection: Protection
- Palettes: Palettes
- StreetLayout: Base
- ZoneStore: ZoneStore
- AssetDir: AssetDir
- SealedCaves: SealedCaves
- gunmodel: Part
- mapsurvey: mapsurvey.py
- lcaudit: lcaudit.py
- gun_model_spec: Fix: SmoothSwingThread busy wait (PatchSwing 4 ms sleep)
- DECIMATION_MOD_TASK: Goal: loot crates and cars in singleplayer
- city_engine: LcCity
- CityNames: CityNames
- CityEdgeTest: CityEdgeTest
- SKILL: City sizes v0.60.0: 5x5 sector cells, one compact city of 4 to 64 districts each, wasteland 1 per 4 sectors beside a military sector, new worlds only (!citySizes)
- Heuristic Auto Namer
- Condition: com.google.gson.JsonObject
- DecimationBiomes: DecimationBiomes
- StreetProps: Slices.java
- edgescan: gradescan.py
- SKILL: Project skill decimation-gun: end to end gun workflow (read docs, references, part plan, anchors, build, compare with study.py, register, gunview test, finish)
- SKILL: Project skill decimation-military-base: research, build (Canvas, occupancy grid, greedy fit, poi), test, small detail review checklist
- Updater: State
- BundledContent: BundledContent
- CityPropsTest: CityPropsTest
- ShotTest: ShotTest
- TracerTest: TracerTest
- MilBaseTest: MilBaseTest
- study: .verts()
- GunViewTest: GunViewTest
- fake_release_server: H
- graph_update: graph_update.py
- sightfit: sightfit.py
- SKILL: Props fitted to their drawn size (v0.49.0, city/PropFit)
- GlInfo: Bug: Decimation mobs black, props black / flickering on an Optimus laptop (RTX 2060 + UHD 630); GPU choice suspected
- graph_update: graph_update.py
- NewGuns: NewGuns
- perfcheck: perfcheck.py
- WorldClock: Event
- PropsTest: PropsTest
- RefugeTest: RefugeTest
- DecimationWorldType: DecimationWorldType
- production_test: Testing the shipped game: production Forge server and client in the scratchpad, driven by devtest.py --live
- CLAUDE: Military base v0.42.3: tower ladder in the middle of the inner side, hangar vault closed with loot, shelter full roof, test chunks pregenerated
- PatchIronSights: Case 16: Mk18 Mod 1 and iron sights hidden under a sight (v0.40.0)
- safezone: City refuge: fortified building on the block nearest each city region's middle (v0.48.0)
- new_feature: Zombie variants v0.34.0: runner, riot, screamer (scream alerts infected within 32), night frenzy; InfectedVariants, ZombieEgg, config deciworldgen_zombies.cfg; dev test mode zombies
- CampCommandTest: CampCommandTest
- NpcKind: NpcKind
- RefugeTest: RefugeTest
- shots_index: Bug: survivor camp NPCs missing (test camp emptied by the test; Decimation's trader reset is dedicated server only), fixed v0.47.1
- CLAUDE: New players forced to update (Update instead of Play, empty server list): UpdateChecker.isUpdateRequired true without DecimationVoiceChat.jar; FIXED v0.57.2 by PatchNoUpdate
- DecimationWorldType: DecimationWorldType
- new_feature: Worldgen round 2 plan (option A: terrain blending, street life, full multiblocks, new building types, loot balance)
- dcinventory: dcinventory.py
- BuildingDress: Item
- ideas: Wasteland locked doors (v0.55.0): most buildings locked by loot, Door_Emergency_1_Locked + military keycard screen, flood fill entrances
- mac10: mac10.py
- Sectors: Sectors
- dressbalance: dressbalance.py
- onejar: onejar.py
- military_base: Military base status v0.42.0: three sizes built and photographed, two seed 1 COPs placed in real worldgen, mil_ test boxes retired, waiting for user review
- worlddump: worlddump.py
- Schematic: Schematic
- facing_table: facing_table.py
- hesco_textures: hesco_textures.py
- boom_textures: boom_textures.py
- PatchFactions: PatchFactions.java
- bug: Dev test game crashed once in vanilla grass ticking (open, not reproduced)
- bug: Water spread over a wasteland crossing once in the bridge test (open)
- ?: BuildingLayout
- PatchMenuStats: PatchMenuStats
- topdown: topdown.py
- worlddump: worlddump.py
- ?: b
- ?: Block
- ?: Entity
- ?: EntityLiving
- ?: EntityLivingBase
- ?: EntityPlayerMP
- ?: IInventory
- ?: InventoryBasic
- skeleton: skeleton.py
- ?: ObjectZone
- ?: ObjectZoneList
- ?: Override
- CLAUDE: Prism instance updated to 0.38.1 (2026-10-10) with the dev worldgen config; it has OptiFine HD U E7 and RTG, the dev client has neither
- SKILL: Military base case 6: verdict, accepted
- ?: World
- ?: ZoneKind
- Weather Type Id Bug
- ?: ServerTickEvent
- ?: ServerTickEvent
- Gradle Dependencies
- Gradle Repositories
- ?: EntityLivingBase
- ?: NpcKind
- ?: ObjectZoneList
- ?: Override
- ?: SuppressWarnings
- ?: BuildingPlan
- ?: Dress
- ?: Dress
- ?: SuppressWarnings
- ?: Building
- ?: Shape
- ?: StreetProps
- ?: Dress
- ?: BuildingPlan
- ?: Dress
- ?: Shape
- ?: StreetProps
- ?: Building
- ?: NpcLoadouts
- ?: ClientTickEvent
- ?: Schematic
- ?: Sub
- ?: EntityLiving
- ?: EntityPlayer
- ?: Entry
- ?: ItemStack
- ?: ServerTickEvent
- ?: ServerTickEvent
- ?: ItemStack
- ?: EntityPlayer
- ?: Item
- ?: EntityPlayerMP
- ?: Entity
- ?: EntityLivingBase
- ?: Canvas
- ?: Canvas
- ?: Override
- ?: Canvas
- ?: Canvas
- ?: Post
- ?: a
- ?: Load
- ?: Save
- ?: Unload
- ?: a
- ?: FurnitureSet
- ?: a
- ?: Plan
- ?: Override
- ?: CityDistrict
- ?: NoiseGeneratorSimplex
- ?: Schematic
- ?: Override
- city_dressing_research: Real world street layout facts (174, sourced, fact checked)
- ?: a
- city_dressing_research: Real world street layout facts (174, sourced, fact checked)
- interior_spec: propFacing was inverted; BlockProp front points 2 E, 3 S, 4 W, 5 N
- CLAUDE: Live dev test trap: key=value persists across live runs, clear with key=; wait after --stop
- roadmap: Roadmap UI: main menu GUI fix later (user), details not decided
- shots_index: flats_v0.20 / v0.21 interiors and sets_v0.21 kitchens
- ?: net.decimation.worldgen.city.LcCity
- ?: net.decimation.fixes.MilitarySpawner
- ?: net.decimation.fixes.VanillaMobs
- ?: net.decimation.worldgen.city.LcCity
- ?: Item
- ?: LargeSites
- ?: StreetProps
- ?: Spot
- ?: StreetPlan
- CLAUDE: Test trap: live checks after camp/refuge run inside a safezone

## God Nodes (most connected - your core abstractions)
1. `Deci` - 129 edges
2. `MilitaryBase` - 53 edges
3. `StreetPlan` - 51 edges
4. `Building` - 50 edges
5. `LcCity` - 50 edges
6. `StreetDress` - 48 edges
7. `NpcLoadouts` - 44 edges
8. `StructureGenerator` - 42 edges
9. `BuildingDress` - 40 edges
10. `CityGate` - 39 edges

## Surprising Connections (you probably didn't know these)
- `Survivor camp (about 1 in 12 buildings, one room: lantern, CanFire, bedroll, crates, radio, WaterPallet, barricaded door, note decal, graffiti outside)` --semantically_similar_to--> `survivor_camp()`  [INFERRED] [semantically similar]
  docs/interior_spec.md → tools/make_test_schematics.py
- `Spawn trap: Decimation adds its spawns to biomes existing in its preInit and strips vanilla monsters in its init; our biomes are made in our preInit and copySpawns copies plains lists in postInit` --semantically_similar_to--> `Recurring root cause: integrated server reports side CLIENT`  [AMBIGUOUS] [semantically similar]
  docs/terrain.md → CLAUDE.md
- `run/client/devtest/results.txt: one line per value, PASS / FAIL with the expectation, exit code 1 on a FAIL` --references--> `DevTestResults`  [INFERRED]
  CLAUDE.md → dev/src/main/java/net/decimation/worldgen/devtest/DevTestResults.java
- `Scope fps measured (4x on ak74): picture in picture aiming 37..44 vs view zoom aiming 105..132` --references--> `ScopeTest`  [INFERRED]
  bug.md → dev/src/main/java/net/decimation/worldgen/devtest/ScopeTest.java
- `Tracer test (-Ptracer): original 52 tracers mean 133 degrees off the target, patched 42 tracers mean 1.0, max 2.0` --references--> `TracerTest`  [INFERRED]
  bug.md → dev/src/main/java/net/decimation/worldgen/devtest/TracerTest.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **v0.12.1 hillside foundation fix (whole footprint sampling, median floor, dirt foundation under schematics)** — claude_v0_12_1_footprint_floor_sampling, docs_worldgen_floor_height_sampling, docs_worldgen_stone_brick_foundation, dev_src_main_java_net_decimation_worldgen_slices_slices, dev_src_main_java_net_decimation_worldgen_plan_plan_foundation [INFERRED 0.85]
- **Saved Javassist patch sources and the rules for applying them** — claude_three_jar_patch_rule, claude_patch_from_original_classes_rule, claude_javassist_patcher, tools_patches_patchswing, tools_patches_patchpropculling, tools_patches_patchscope, tools_patches_patchtracer, tools_patches_patchbackend [INFERRED 0.85]
- **Visual evidence memory (autotest screenshot modes, docs/shots, written shots index, prop catalogue)** — claude_visual_evidence_rule, claude_autotest_screenshot_modes, docs_shots_index_shots_index, docs_prop_catalogue_prop_catalogue_doc [INFERRED 0.85]
- **Dev test modes run in one launch by tools/devtest.py** — claude_devtest_workflow, tools_devtest, dev_src_main_java_net_decimation_worldgen_devautotest_devautotest, dev_src_main_java_net_decimation_worldgen_devtest_devtestmode_devtestmode, dev_src_main_java_net_decimation_worldgen_devtest_devtestresults_devtestresults, dev_src_main_java_net_decimation_worldgen_devtest_serverchecks_serverchecks, dev_src_main_java_net_decimation_worldgen_devtest_cameraviews_cameraviews, dev_src_main_java_net_decimation_worldgen_devtest_scopetest_scopetest, dev_src_main_java_net_decimation_worldgen_devtest_tracertest_tracertest, dev_src_main_java_net_decimation_worldgen_devtest_propstest_propstest, dev_src_main_java_net_decimation_worldgen_devtest_cityfpstest_cityfpstest [EXTRACTED 1.00]
- **Singleplayer bugs from ServerProxy only registration or isServer gates** — claude_singleplayer_side_root_cause_pattern, bug_loot_singleplayer_crash, bug_loot_gui_never_opens, bug_no_supply_drops_singleplayer, bug_humanity_kill_singleplayer, bug_zones_inactive_singleplayer, bug_bottlecap_currency, deobf_notes_architecture_serverproxy [INFERRED 0.95]
- **Prop culling failure and fix (PropRenderer, LineOfSight corner rays, 1x1 unrotated render box, PatchPropCulling)** — bug_props_not_rendered, deobf_notes_architecture_prop_tesr_renderers, bug_lineofsight_corner_rays, bug_prop_render_bounding_box_1x1, tools_patches_patchpropculling_patchpropculling [INFERRED 0.95]
- **Multiblock master fix (v0.12.3): render gate, setSelfMaster in placers, repair on chunk load** — bug_generated_metal_shelves_invisible, bug_multiblock_master_render_gate, deobf_notes_architecture_multiblock_props, dev_src_main_java_net_decimation_worldgen_slices_slices, dev_src_main_java_net_decimation_fixes_multiblockrepairhandler_multiblockrepairhandler, claude_v0_12_3_multiblock_master_rule [EXTRACTED 1.00]
- **Supply drop landing safety: falling crate dies on non replaceable cells (flowers, props), fixed by clearLanding and skipping prop topped columns** — bug_supply_drop_flower_vanish, bug_supply_drop_street_prop_vanish, bug_falling_block_replaceable_landing, dev_src_main_java_net_decimation_fixes_supplydropscheduler_supplydropscheduler_clearlanding, dev_src_main_java_net_decimation_fixes_supplydropscheduler_supplydropscheduler_drop [EXTRACTED 1.00]
- **Upper storey reachability fix v0.16.1 (floorplan scan, unsupported ladder, collapse exemption, Building.ladder / collapsed)** — bug_upper_storeys_unreachable, bug_ladder_unsupported_wall_cell, bug_ladder_core_collapse_exemption, tools_floorplan [EXTRACTED 1.00]
- **Prop render performance: measure, cache line of sight, render distance, bake (open)** — bug_prop_fps_drop, bug_prop_fps_measurement, bug_los_cache, bug_prop_render_distance_by_size, bug_cityfps_measurement, bug_bake_props_chunk_mesh, tools_patches_patchpropculling_patchpropculling, claude_jfr_profiling, docs_roadmap_prop_drawing_cost [EXTRACTED 1.00]
- **Cheap scope v0.28.0..0.28.4 (gate, view zoom, projective glass, sniper overlay)** — feature_cheap_scope, bug_scope_pip_gate, bug_scope_camera_zoom, bug_scope_projective_glass, bug_scope_sniper_overlay, dev_src_main_java_net_decimation_fixes_scopezoom_scopezoom, tools_patches_patchscope_patchscope, dev_src_main_java_net_decimation_worldgen_devtest_scopetest_scopetest [EXTRACTED 1.00]
- **Decimation world type biome pipeline (TerrainEvents swaps GenLayers, DeciGenLayer reads BiomeMap, BiomeMap uses shared Sectors, DecimationBiomes defines the biomes)** — dev_src_main_java_net_decimation_worldgen_terrain_terrainevents_terrainevents, dev_src_main_java_net_decimation_worldgen_terrain_decigenlayer_decigenlayer, dev_src_main_java_net_decimation_worldgen_terrain_biomemap_biomemap, dev_src_main_java_net_decimation_worldgen_sectors_sectors, dev_src_main_java_net_decimation_worldgen_terrain_decimationbiomes_decimationbiomes, dev_src_main_java_net_decimation_worldgen_terrain_decimationworldtype_decimationworldtype [EXTRACTED 1.00]
- **Gun model, animation and registration pipeline** — deobf_notes_architecture_gunitem, deobf_notes_architecture_itemregistry, docs_gun_model_spec_bmodelloader, docs_gun_model_spec_bmodel_format, docs_gun_model_spec_gunanimation, docs_gun_model_spec_anib_format, docs_gun_model_spec_gunitemrenderer, create_weapons_gun_registration_pattern [EXTRACTED 1.00]
- **Gun registration builder chain** — create_weapons_gun_registration_pattern, deobf_notes_architecture_gunitem, deobf_notes_architecture_gunstats, create_weapons_weapon_category_enum, create_weapons_fire_mode_enum, deobf_notes_architecture_gunitem_setdamage [EXTRACTED 1.00]
- **Slice placement flow for structures larger than the population window** — docs_worldgen_population_window, docs_worldgen_slice_placement, docs_worldgen_floor_height_sampling, dev_src_main_java_net_decimation_worldgen_slices_slices, dev_src_main_java_net_decimation_worldgen_plan_plan, dev_src_main_java_net_decimation_worldgen_structuredata_structuredata, dev_src_main_java_net_decimation_worldgen_schematicplan_schematicplan [EXTRACTED 1.00]
- **Worldgen test tooling without a player** — tools_servertest, tools_wallscan, tools_worldcheck, dev_src_main_java_net_decimation_worldgen_devpregen_devpregen, tools_make_test_schematics, docs_worldgen_seed1_test_world [EXTRACTED 1.00]
- **Terrain blending v0.13.0 (lot grading, Graded plans, Building.grade, CityDistrict lot placement, gradescan check)** — docs_worldgen_lot_grading, dev_src_main_java_net_decimation_worldgen_graded_graded, dev_src_main_java_net_decimation_worldgen_slices_slices, dev_src_main_java_net_decimation_worldgen_citydistrict_citydistrict_plan, tools_gradescan, bug_graded_sand_cave_fall [EXTRACTED 1.00]
- **Whole multiblock generation (v0.16.0): slice writer and small placer call complete after writing, Decimation helper forms the box, multiscan checks** — dev_src_main_java_net_decimation_worldgen_slices_slices_write, dev_src_main_java_net_decimation_worldgen_structuregenerator_structuregenerator_place, dev_src_main_java_net_decimation_fixes_multiblockrepairhandler_multiblockrepairhandler_complete, docs_worldgen_form_multiblock_helper, docs_worldgen_complete_after_slice_rule, tools_multiscan [EXTRACTED 1.00]
- **Street aligned car wreck facing (placement rule, metadata facing, renderer, axis assumption)** — docs_building_design_street_car_placement, docs_building_design_prop_facing_metadata, docs_building_design_car_model_axis_assumption, docs_worldgen_street_car_wrecks, deobf_notes_architecture_prop_tesr_renderers [INFERRED 0.85]
- **Interior spec layered build (structure, surfaces, room programs, story / decay, exterior, in work order)** — docs_interior_spec_principle_layer_order, docs_interior_spec_doors, docs_interior_spec_surfaces, docs_interior_spec_apartment_unit_program, docs_interior_spec_office_storey_programs, docs_interior_spec_shop_program, docs_interior_spec_story_decay_layer, docs_interior_spec_exterior, docs_interior_spec_order_of_work [EXTRACTED 1.00]
- **Step 4d.1 doors and low debris (spec, code, tracker, release note, audit shots)** — docs_interior_spec_step1_doors_low_debris, claude_v0_17_0_doors_low_debris, new_feature_step_4d1_doors_debris, docs_shots_index_audit_v0_17 [INFERRED 0.85]
- **Verification loop for every building change (floor plans, audit shots, scans, autotest, written findings)** — docs_interior_spec_verification, tools_floorplan, tools_wallscan, tools_gradescan, tools_multiscan, docs_building_audit_building_audit_doc, docs_shots_index_shots_index [EXTRACTED 1.00]
- **Building quality audit toolchain (floor plans with reachability, audit screenshots, view cells, indexed results)** — docs_building_audit_audit_method, tools_floorplan, docs_shots_index_audit_v0_16, docs_shots_index_plans_v0_16 [INFERRED 0.85]
- **ServerProxy-only logic absent in singleplayer** — deobf_notes_architecture_serverproxy, deobf_notes_architecture_servertickhandler, deobf_notes_architecture_itempickuphandler, deobf_notes_architecture_entityspawnzonehandler, deobf_notes_architecture_playerzonetickhandler, deobf_notes_architecture_safezoneattackhandler, deobf_notes_architecture_servercommandregistrar, deobf_notes_architecture_zonemanager, deobf_notes_architecture_supplydropspawner, claude_singleplayer_side_root_cause_pattern [EXTRACTED 1.00]
- **Right click loot flow (interact, cooldown, pool, packet, delayed GUI)** — deobf_notes_architecture_lootinteracthandler, deobf_notes_architecture_lootcooldownregistry, deobf_notes_architecture_loottable, deobf_notes_architecture_lootpool, deobf_notes_architecture_packetlootinventory, deobf_notes_architecture_tickscheduler, deobf_notes_architecture_deciconstants [EXTRACTED 1.00]

## Communities (307 total, 140 thin omitted)

### Community 0 - "Deci: Deci"
Cohesion: 0.03
Nodes (34): Bug: colourful M4A4 rail teeth under a sight (v0.41.0), Bug: NPC machine guns fire at double rate (v0.39.2), CLAUDE.md v0.32.0 note and the shootAt test trap, Decimation runs guns at 1.3x listed rpm (GunStats), maxBurstRpm 600 (deciworldgen_npc.cfg npc_fire), DamageSource, Deci, Magazine (+26 more)

### Community 1 - "StreetDress: StreetPlan"
Cohesion: 0.05
Nodes (14): Bug: wasteland fence floating over half slabs (FIXED v0.61.0), BuildingPlan, Lesson 23: stand on the last full block, replace slabs above, BuildingPlan, Dress, StreetDress, ZoneKind, StreetPlan (+6 more)

### Community 2 - "CityGate: CityGate"
Cohesion: 0.05
Nodes (18): Case 10: wasteland gates and floating fence (v0.61.0), Case 11: gate review (v0.61.0 to v0.61.1), Lesson 22: barriers need real collision or an uncrossable gap, Lesson 24: no road out of a city into nothing; compact form instead of a plain fallback, Lesson 25: a trench reads as a hole, barriers stand on the road, CityGateState, WastelandFence, GateTest (+10 more)

### Community 3 - "?: net.minecraft.world.World"
Cohesion: 0.08
Nodes (21): Block, DevTestUtil, ZombieTest, ZonesTest, EntityPlayerMP, Item, ItemStack, MilitarySpawner (+13 more)

### Community 4 - "LotDress: LotDress"
Cohesion: 0.06
Nodes (40): Bug: small lab read as an office; barricades at every inner door (FIXED v0.54.0), Trap: BlockChest subclasses (footlocker) re-aim themselves on placement, Skill decimation-city-decay, Skill decimation-city-interior, decimation-city case 3: wasteland weak spots (v0.54.1), decimation-city case 4: lot scenes that did not tell their story (v0.54.2), decimation-city case 5: edge, doors, locked wasteland (v0.55.0), decimation-city case 6: wasteland city apart, bunker, ramps, bridges, stations (v0.56.0) (+32 more)

### Community 5 - "SurvivorCamp: SurvivorCamp"
Cohesion: 0.06
Nodes (11): CanvasPlan, MilitaryBasePlan, B, Post, SurvivorCamp, Canvas, SurvivorCampPlan, MilitaryBase (+3 more)

### Community 6 - "?: cpw.mods.fml.common.eventhandler.SubscribeEvent"
Cohesion: 0.05
Nodes (21): Bug: enemy group members spawned into walls; none in superblock districts (FIXED v0.54.1), Trap: vanilla class names are obfuscated in the shipped game, never test by package name (copySpawns fixed), Decimation NPCs are never hostile to creative players (isHostileTo), Configuration, cpw.mods.fml.common.eventhandler.SubscribeEvent, GunNoise, MilitarySpawner, VanillaMobs (+13 more)

### Community 7 - "NpcLoadouts: NpcLoadouts"
Cohesion: 0.07
Nodes (9): ItemStack, NpcKind, World, NpcLoadouts, Tier, Minecraft, NpcKillTest, NpcLoadoutsAccess (+1 more)

### Community 8 - "LcContent: LcContent"
Cohesion: 0.09
Nodes (5): Canvas, MilBaseTest, MilitaryBase, MilitaryBasePlan, net.decimation.worldgen.military.MilitaryBasePlan

### Community 9 - "ServerChecks: ServerChecks"
Cohesion: 0.06
Nodes (34): Bug: building base height depends on chunk generation order (Slices.decideBase), Bug: vehicles destroyed in one hit (fixed v0.8.1), VehicleHitHandler (v0.8.1), Obfuscated Decimation names in our code go through fixes/Deci, run/client/devtest/results.txt: one line per value, PASS / FAIL with the expectation, exit code 1 on a FAIL, tools/devtest.py: dev test modes in ONE game launch, results file, contact sheet per mode (first choice since 2026-10-09), Live loop: hotswap code, reload sets, rebuild in place, no restart per change, Rule: new code adds an accessor to Deci, never calls deci.* directly (all ~60 obfuscated uses migrated 2026-10-09, seed 1 world 0 blocks differ) (+26 more)

### Community 10 - "ScopeZoom: ScopeZoom"
Cohesion: 0.06
Nodes (34): Bug: gun creeps off screen while aiming (NPC scope glass measured, fixed v0.43.1), Scope zoom lag on a weak laptop: full screen glass copy every frame while zooming or holding; v0.58.0 copies only the glass box while aiming, v0.28.1: EntityRenderer.cameraZoom zooms world and gun together; ScopeZoom draws the hand itself on RenderHandEvent (vanilla skips it while zoomed), Scope FPS drop: picture in picture second world render, fixed v0.28.0 with view zoom, Report: FPS drop while aiming scopes (fixed v0.28.0..0.28.4), Scope fps measured (4x on ak74): picture in picture aiming 37..44 vs view zoom aiming 105..132, PatchScope: renderScopeView gated behind system property decimation.scope.pip (old scope via pictureInPicture=true in config/deciworldgen_scope.cfg), v0.28.2 / 0.28.3: projective see-through glass (frame copied, mapped by screen position), sight learned per gun + scope + window aspect, copy limited to the glass box (+26 more)

### Community 11 - "architecture: Bug tracker (bug.md)"
Cohesion: 0.07
Nodes (6): B, BottlecapHandler (deciworldgen), BottlecapHandler, Player, net.minecraftforge.event.entity.player.EntityItemPickupEvent, PlayerTickEvent

### Community 12 - "Building: Building"
Cohesion: 0.06
Nodes (12): Building, Props, Interior, ShopPlanner, Furnisher, Interior, net.decimation.worldgen.Graded, Ruins (+4 more)

### Community 13 - "hwmap: hwmap.py"
Cohesion: 0.10
Nodes (27): Bug: city edge ramp missed its outer columns (fixed v0.24.4, populate scans cells within EDGE), RailTest, City edge ramp: 24 wide, rounded corners, nearest cell owns a column, wobbled contours, Dev test rail: fresh station, 48 rails, no liquid, street to platform walk, shots of every end, lc_rail_v0.44.0 shots: entrance plaza, lit platform, stairs, shaft, tunnel, four line ends, crossing, is_city(), jrandom_float(), region_sector() (+19 more)

### Community 14 - "StructureGenerator: StructureGenerator"
Cohesion: 0.10
Nodes (10): Refactor check on both city paths: worlddiff 0, legacy path by moving the lc pack aside, git stash -u, cpw.mods.fml.common.IWorldGenerator, Yard, LegacyStreets, ZoneKind, StructureGenerator, LargeSites, LegacyStreets (+2 more)

### Community 15 - "new_feature: NPC tiers design and result (v0.30.0): tier per armed NPC on first join (gear, gun, health, fire rate, share of player gun damage), stored in entity data, gun synced via data watcher slot 26; bandit light / medium / heavy, soldier camo sets, Soviets as enemy military"
Cohesion: 0.06
Nodes (39): CLAUDE.md v0.30.1 note: spawn egg per NPC tier, CLAUDE.md v0.30.4: cooldown ticks, rocket tiers, config version 2, new tiers last, CLAUDE.md v0.30.0 note: NPC tiers, gun sync via data watcher slot 26, test mode npc, CLAUDE.md v0.31.0: juggernaut tier, Barrett armor piercing, SAFEZONE, Balance v0.31.0: juggernaut, Balance v0.30.4: hit cooldown 0.25 s, RPG NPCs, Balance: NPC tiers (v0.30.0), config deciworldgen_npc.cfg (+31 more)

### Community 16 - "SchematicPlan: SchematicPlan"
Cohesion: 0.06
Nodes (11): Plan, Schematic, SchematicPlan, Sub, ZoneKind, BORDER, MILITARY, POLICE (+3 more)

### Community 17 - "SafezoneSites: SafezoneSites"
Cohesion: 0.09
Nodes (30): all_names(), attach_offset(), Gun, label(), load(), main(), metrics(), num() (+22 more)

### Community 18 - "Deci: net.minecraft.entity.player.EntityPlayer"
Cohesion: 0.07
Nodes (10): a, Test arena (DevTestArena): flat stone floor at y 150 around (8, 8), every NPC test mode builds and cleans it first; retry Decimation-refused spawns, ItemStack, NpcEgg, DevTestArena, arena_v0.34.0: all NPC test lineups and shots on the test arena, EntityLiving, InventoryBasic (+2 more)

### Community 19 - "LootTest: LootBody"
Cohesion: 0.11
Nodes (10): After 0.56.0 (unreleased): fixes/Loot.open, fixes/LootBody (deciworldgen:loot_body), dev test loot, keepScreen trap, World, Loot, LootBody, InventoryBasic, LootTest, Loot spikes DONE 13 Oktober 2026 (dev test loot, 12 PASS): our inventory on Decimation's loot screen, taken stacks leave the same InventoryBasic, LootBody opens it on right click, goes when emptied, NBT save, Master plan step 0 DONE: both loot spikes work; next step 1 core (+2 more)

### Community 20 - "interior_spec: Building interior and exterior spec (round 2 step 4c, approved 7 Oktober 2026)"
Cohesion: 0.10
Nodes (35): v0.17.0: step 4d.1, doors in every DOOR cell and low debris (docs/interior_spec.md section 8), Finding: rooms have no function (sparse apartment units, empty ground storey units, identical office desk grid on every storey incl. ground, repeated plans on tall buildings, undefined upper shop storey), Apartment slab layout (double loaded corridor, stair core, living part and bedroom per unit), Procedural building design doc (city blocks, Building v2), Minecraft scale (storey 4 blocks, corridor 2 wide, doors 1x2, units 5 to 8 deep), Office floor layout (core, open plan desk rows at windows, meeting rooms, reception, storage, break room), Layout research sources (Auckland Design Manual, archgyan, Pult, Shopify, Small Business Trends), Shop grid layout (parallel aisles, decompression zone, checkout front left, stockroom behind) (+27 more)

### Community 21 - "performance: Performance doc: tools (gunperf, census, perfcheck, JFR via jcmd), findings, checklist"
Cohesion: 0.09
Nodes (19): CensusTest, CityViewTest, GunPerfTest, Angelica in the dev client (-Pangelica, Decimation jar without its unused fastutil): starts but draws no far terrain on this Mac (GL 2.1), not usable, Lag checklist: perfcheck log, perfcheck world, census growth, JFR, gunperf vs control, fix and measure again, City view lag: first views were wrong (arena floor / clouds); valid view: props cost ~18% (34 vs 41 fps), the rest is city blocks and Mac heat, Dev world entity explosion: region slot (-1,-6) held chunk (-4,-1), entities doubled each save / load to 40 730 with a Forge stack trace per tick; fixed with fixchunk + census kill, Guns are not the cause: dropped / held fps of Uzi, UMP45, MAC-10, UMP9 equal to baseline (+11 more)

### Community 22 - "DeciBiome: DeciBiome"
Cohesion: 0.10
Nodes (33): NPC tracers flew along the body facing; fixed by sending the target id (PatchTracer), Cause: PacketGunFireEffects carried only the shooter id; the client drew the tracer along getLook() (a mob's body facing), Tracer test (-Ptracer): original 52 tracers mean 133 degrees off the target, patched 42 tracers mean 1.0, max 2.0, NPC ranged attacks call attackEntityFrom directly server side, BankerTrader (deci.ai.e), FactionHumanEntity (deci.ah.d), Hostile mob entities (deci.ag.*: Bandit, Soldier, Infected), ItemRegistry (deci.aD.k) (+25 more)

### Community 23 - "PropFacing: net.minecraft.block.Block"
Cohesion: 0.11
Nodes (7): Surfaces, PropFacing, Row, PropFacingAccess, B, PropBoxes, net.minecraft.block.Block

### Community 24 - "city_engine: City engine: Lost Cities style cities from converted DeceasedCraft content"
Cohesion: 0.07
Nodes (31): FixedBase, City engine: Lost Cities style cities from converted DeceasedCraft content, City districts: wasteland next to military sectors, current beta weighted, Skipped giant buildings: casino 276 high, oasis condo top above 250, laboratory 90 deep cellars, Rules taken from Lost Cities source: street surface at G, ground floor at G, cellars below, stairs at G + 1 toward the higher neighbour, City levels per cell (6 blocks apart), streets at G, stairs parts between levels, City engine open items: railway (v0.44.0) and rivers with bridges (v0.45.0) done; giant buildings still skipped, City superblocks: 2x2 cells, 7x7 building chunks, landmark towers (+23 more)

### Community 25 - "Wasteland: Wasteland"
Cohesion: 0.13
Nodes (3): Load, Wasteland, WastelandTest

### Community 28 - "StatsTest: .client()"
Cohesion: 0.09
Nodes (9): Dedicated server crashed at start since v0.42.2: Deci.homeMenu loaded GuiScreen (fixed v0.44.0), HumanityKillHandler, ClientTickEvent, LocalStats, MenuTest, StatsTest, GuiScreen, net.minecraftforge.common.config.Configuration (+1 more)

### Community 29 - "make_test_schematics: make_test_schematics.py"
Cohesion: 0.16
Nodes (26): mil_compound large test schematic (48x14x48), city_office(), city_shop(), city_street(), civ_gas_station(), civ_house_ruin(), civ_shed(), decay() (+18 more)

### Community 30 - "worldgen: Multiblock props (0.16.0): generation completes Decimation multiblocks (tile entity extends deci.W.a MultiblockPart) whole"
Cohesion: 0.11
Nodes (23): Bug: generated metal shelves invisible (multiblock master never set, fixed v0.12.3), MetalShelfRenderer draws only the master part (returns unless isMaster: stored master position equals own position), setSelfMaster() on every placed multiblock part plus repair on chunk load, v0.12.3 rule: multiblock props (deci.W.a, metal shelves) render only from a master part; anything placing them outside player placement must call setSelfMaster(), v0.16.0: multiblock props generated whole (shelves are 1x1x2 TALL), tools/multiscan.py checks them, supply drops skip columns topped by a prop, Multiblock props (deci.W.*), Load, World (+15 more)

### Community 32 - "BuildingPlan: BuildingPlan"
Cohesion: 0.08
Nodes (8): Building, Shape, BuildingPlan, Building, Shape, ZoneKind, Garrisoned, Styled

### Community 33 - "architecture: Bug tracker (bug.md)"
Cohesion: 0.11
Nodes (31): Bug: large ammo crate NPE (dead field avk), Bug: ClassCastException deci.a.c to deci.a.e, Bug: loot never worked in singleplayer, Obfuscation map (package to meaning), 8 agent deobfuscation naming pass, Subsystem taxonomy (core, proxy, network, loot, zone, ...), Decimation architecture notes, ClientProxy (deci.a.c) (+23 more)

### Community 34 - "PropFit: PropFit"
Cohesion: 0.16
Nodes (3): Shape, PropFit, PropGeom

### Community 35 - "GunTest: DevTestMode"
Cohesion: 0.11
Nodes (8): Shots at a big zombie's head miss; not reproduced: Hulk / Bloater models inside their 1.2 x 2.85 boxes, FacingTest, GunTest, HitboxTest, ItemsTest, PlacesTest, DevTestMode, bugs_v0.58 shots: hitbox side view, scope sheet

### Community 36 - "CLAUDE: decimation-singleplayer README (public repo overview)"
Cohesion: 0.09
Nodes (29): Case sensitive volume extraction, CFR --caseinsensitivefs true silently drops colliding classes, CLAUDE.md project guide, dev/libs/Decimation-base.jar (patched jar minus our classes), Decimation.jar (obfuscated Forge 1.7.10 mod jar), Decimation.jar.original.bak (hash checked backup), Decimation.jar.patched (deliverable), deobf/ readable reference tree (decompiled with readable names, read only) (+21 more)

### Community 37 - "prop_catalogue: Decimation prop catalogue (look, size and facing of every deci: block, from the prop gallery)"
Cohesion: 0.10
Nodes (29): A floor block is also the ceiling below: keep floors light, BlockRegistry (deci.aD.c / g), Exterior findings: flat facades (no balconies, canopy, shopfront glazing, shop signs) and bare roofs (no vents, water tank, antenna, stair hut, AC units), Finding: no doors anywhere, only gaps (14 Decimation door blocks unused), Finding: no story details (barricades, skeletons, body bags, blood decals, notes, graffiti, survivor camps, looted crates all exist and are unused), Finding: one interior material everywhere (birch plank walls, oak plank floors and ceilings), no ceilings, lighting, carpets or tiles, Wall / trim / accent palettes from vanilla 1.7.10 blocks (brick, clays, sandstone, quartz, stone brick), Door decay: 25 to 50% missing, a few left open (meta bit 4), one barricaded in the most decayed buildings (+21 more)

### Community 40 - "CityRefugePlan: CityRefugePlan"
Cohesion: 0.09
Nodes (3): CityRefugePlan, Residents, net.decimation.worldgen.FixedBase

### Community 41 - "lcstudy: dcinventory.py"
Cohesion: 0.12
Nodes (21): chunk_biomes(), chunk_blocks(), chunks(), main(), Palette, {section Y: biomes[4,4,4] (y, z, x) global ids}., Entries packed without spanning longs (1.16+)., Global name -> id for a whole survey. (+13 more)

### Community 42 - "bug: Bug tracker (bug.md)"
Cohesion: 0.10
Nodes (22): Baking static props into chunk meshes (prop textures into the block atlas, both model formats to quads): full fix for open views, not started, Launch waited 5 s for the dead Decimation backend (kryonet hardcoded 5000 ms); PatchBackend, LineOfSight.canSeeTileEntity (deci.a.c$a.a): 8 rays from the eye to the render box corners, PatchPropCulling step 3 (v0.28.6): line of sight answers cached until the player or entity moves 0.3 blocks (props 1.0..1.3 s, entities 0.15..0.18 s); prop renderer share 21% -> 9%, Prop dense FPS drop: line of sight ray casts 76% of prop rendering, cached in v0.28.6; model drawing remains, Report: FPS drop in prop dense areas (line of sight cached v0.28.6, model drawing remains), Prop fps measured (-Pprops): 225 props in view 12..14 fps vs empty 28..29; 76% of PropRenderer time in canSeeTileEntity ray casts, TileEntityProp.getRenderBoundingBox: bare 1x1x1 cell for 36 of 72 props, never rotated (+14 more)

### Community 43 - "CameraViews: CameraViews"
Cohesion: 0.12
Nodes (8): City street after the cache (-Pcityfps): props 5.5% of the client thread, chunk drawing 15%, chunk rebuild 12.7%; props no longer the bottleneck, Dev test modes: checks (fresh seed 1 world: zones, vehicle, humanity, prop box, bottlecaps, armor, helmet, supply drop), views (-Paudit / -Pgallery / -Pfootprint / -Pstudy / -Pflats / -Psets), scope, tracer, props, cityfps, New dev test: DevTestMode subclass, register its name in DevAutoTest.mode(), record with DevTestResults.value / check, screenshots with DevTestUtil.screenshot, CityFpsTest, DevTestMode, DevTestResults, EntityPlayerMP, ScopeTest

### Community 44 - "new_feature: Current state and pending decisions"
Cohesion: 0.11
Nodes (24): Current state and pending decisions, v0.14.0: Decimation world type (own biome map on vanilla terrain generator, flat cities on the exact city sectors, Sectors now shared, autotest runs on this type), Rotation, worldmap_seed1_v0.14.png: top down map of seed 1, Decimation world type (city sectors, military, suburbs, overgrown forest, rivers stopping at city edges), BiomeMap.biomeAt rules (seed only: city and military biomes exactly on sector squares, suburbs warped up to 56, dead wilderness within about 100 blocks, overgrown further out, no villages), Decimation world type (level-type=decimation: flat rolling land, rivers and lakes, no ocean, one temperate climate), Lake rules (none in city / military, 1 in 4 in other dead biomes, vanilla rate in overgrown, no surface lava pools), Terrain not done yet: fog colour comes from the world provider [not verified whether needed], no snow by design (temperature 0.7), fixed ids may clash (+16 more)

### Community 45 - "worldcheck: worldcheck.py"
Cohesion: 0.11
Nodes (19): Bug: structures built on ocean floor, Underwater placement fix (v0.7.0), tools/worldcheck.py region file inspection, chunks(), fix_chunk(), log_report(), main(), world_report() (+11 more)

### Community 46 - "architecture: ServerProxy (deci.a.e, dedicated only)"
Cohesion: 0.11
Nodes (27): Bug: zones never active in singleplayer (partial fix), AntiCheatScanner (deci.aN.a), BackendConnection (deci.aP.a, kryonet), Block break/place protection handlers (deci.aK.a, b), ChatHandler (deci.aK.n, radio chat), ClanManagerV1 (server.clans.a), DeathStatsHandler (deci.aK.h), DecimationMod (deci.a.b, @Mod entry) (+19 more)

### Community 47 - "?: DecimationWorldGen"
Cohesion: 0.13
Nodes (16): CityDistrict, cpw.mods.fml.common.event.FMLInitializationEvent, cpw.mods.fml.common.event.FMLPreInitializationEvent, cpw.mods.fml.common.Mod, DecimationWorldGen, Placeholder blocks to Decimation props (sponge, gold, lapis, diamond, emerald, iron, coal, wool and stained clay colours), FMLPostInitializationEvent, FMLServerStartingEvent (+8 more)

### Community 48 - "EdgePlan: EdgePlan"
Cohesion: 0.11
Nodes (3): EdgePlan, ZoneKind, Graded

### Community 49 - "new_feature: Current state and pending decisions"
Cohesion: 0.14
Nodes (7): ServerTickEvent, Post, SafezoneSites, Site, SiteList, Load, Save

### Community 50 - "WorldClock: CoreTest"
Cohesion: 0.14
Nodes (9): v0.57.0: loot spikes, shared core (WorldClock, LootPools, Protection, Areas), dev tests core / items, PatchLootRolls, ServerTickEvent, Unload, Listener, WorldClock, CoreTest, ItemStack, Shared core: WorldClock (world day, dusk / dawn events) for growth, horde nights, raids, claim decay, infection stages (+1 more)

### Community 51 - "worldgen: World generation doc (deciworldgen)"
Cohesion: 0.13
Nodes (20): Ladder at (W-2, L-2) hangs on a back wall cell that can be a window, a decay hole or not yet written (next population window); a block update pops it off, Bug: city buildings missing a whole wall at sector borders (fixed v0.11.1), DevPregen, ServerTickEvent, Schematic, Adding community schematics (prefix, folder, full restart, new chunks only), Cell grid (4x4 chunks, one small schematic or one city block), Large schematics (up to 120x120, per site chance, placed inside the site) (+12 more)

### Community 52 - "ZombieEgg: Override"
Cohesion: 0.17
Nodes (7): cpw.mods.fml.relauncher.SideOnly, SuppressWarnings, ZombieEgg, BlockHesco, net.minecraft.client.renderer.texture.IIconRegister, net.minecraft.util.IIcon, Override

### Community 53 - "Updater: Updater"
Cohesion: 0.13
Nodes (3): SuppressWarnings, Updater, java.net.HttpURLConnection

### Community 54 - "Areas: Areas"
Cohesion: 0.14
Nodes (12): Area, Areas, Load, Save, Unload, ZoneKind, Kind, BASE_CLAIM (+4 more)

### Community 55 - "worldgen: Lot grading (city yards, 0.13.0: Graded plans own the 26x26 lot, Slices grades every lot column before writing)"
Cohesion: 0.13
Nodes (21): CityDistrict, Biome overgrowth (temperate vines and moss, jungle heavy vines, snowy snow layers, dry sand drifts and dead bushes), Decay model (level 0.15 to 0.55, wall holes, cracked and mossy blocks, broken windows, rubble, corner collapse over 1 to 3 storeys), Floor counts (shop 1 to 2, apartment 2 to 9, office 3 to 20, about 15% towers), Lots and yards (2 free on the sides, 3 behind, 6 to 9 front yard; offices and shops car park with nose-in wrecks, apartments gravel path and lawn), 1 block margin ring outside walls for exterior vines (SKIP elsewhere), Parked and wrecked cars on road lanes (spacing at least 9, about 25% of slots filled, never in intersections, rotated along the street), Ravines cut 40 block trenches through flat cities; SealedCaves (InitMapGenEvent) digs nothing above y 50 under city and military biomes (+13 more)

### Community 56 - "NpcTest: .batches()"
Cohesion: 0.16
Nodes (22): anib(), bmodel(), build(), fnum(), icon(), layout(), numbered(), paint() (+14 more)

### Community 57 - "CLAUDE: Testing without the user (devtest.py first choice, servertest, worldcheck, worlddiff, autotest flags)"
Cohesion: 0.14
Nodes (20): Wall scan reproduction on seed 1 (2 of 33 buildings, one real: b4_4_2), Autotest screenshot modes (default 3 street views, -Paudit building audit, -Pgallery every Decimation block 3 per shot, -Ponly= re-shoots single views; peaceful, mobs removed, camera locked per tick, fov / gamma restored), Autotest ends with 3 city street screenshots (dev/run/client/screenshots/autotest_<n>.png: along the street, street light side on, across); read them to check visuals instead of asking the user; -Ddeciworldgen.autotest.views=false skips them, Autotest forces pauseOnLostFocus false, Damage checks must run after 60 server ticks (spawn invulnerability), Testing without the user (devtest.py first choice, servertest, worldcheck, worlddiff, autotest flags), worldcheck World.registry() maps block names to ids from level.dat (BlockWreckage1..5 = 176..180, id >= 256 is not a mod block test), Interior prop inventory (275 deci: blocks from World.registry(); no toilet, sink, bath, sofa, bed or fridge props) (+12 more)

### Community 58 - "Slices: Slices"
Cohesion: 0.14
Nodes (7): v0.12.1: whole footprint floor height sampling and dirt fill under schematics, Graded, Slices, Floor height sampling (5x5 grid over the whole footprint where chunks exist plus 9 soilTop points in the window, median, maxSpread buildings 12 / schematics 7, stored in StructureData), Foundation down to the ground (max 12, Plan.foundation): stone brick plinth for buildings, dirt for schematics, Terrain blending (round 2 step 1, done v0.13.0, headless verified, not yet seen in game), Plan

### Community 59 - "ZoneStore: ZoneStore"
Cohesion: 0.14
Nodes (11): Load, ObjectZone, ObjectZoneList, Save, ServerTickEvent, Unload, ZoneKind, ZoneStore (+3 more)

### Community 60 - "bug: Bug tracker (bug.md)"
Cohesion: 0.14
Nodes (23): Bug: bottlecaps not converted to currency (fixed), Bug tracker (bug.md), Bug: CustomSkinLoader coremod crash, EntityFallingSupplyDrop turns into a block only on a replaceable cell (flowers, saplings, tall flowers are not), Bug: graded yard sand fell into caves, hole next to a building (fixed v0.13.0), Bug: humanity never changed from ordinary kills in singleplayer (fixed v0.9.0), Bug: loot GUI never opens, Bug: no supply drops in singleplayer (fixed v0.9.0) (+15 more)

### Community 61 - "NoiseTest: net.minecraft.entity.EntityLiving"
Cohesion: 0.16
Nodes (11): Bug: NPCs kept a dead entityToAttack and ignored gunshots (fixed v0.43.2), Bug: enemy NPCs (juggernaut) ignored gunshots, living stale entityToAttack (fixed v0.43.3), v0.43.2 stale target fix, aimdrift changes, test sweep, v0.43.3 GunNoise reads revenge target, noise test tier / far / hits, Decimation NPCs fight getAITarget (revenge, expires); entityToAttack never cleared, NoiseTest, v0.43.2 NPCs react to gunshots again, v0.43.3 enemy NPCs react to gunshots with an old target remembered (+3 more)

### Community 62 - "building_design: v0.10.0 to v0.12.3 city blocks, large schematics, city v2 and fixes status"
Cohesion: 0.15
Nodes (22): v0.10.0 to v0.12.3 city blocks, large schematics, city v2 and fixes status, v0.12.2: car wreck long axis is x at 0 degrees (north south 5/3, east west 4/2), v0.15.0: street life (levelled street cross sections, dashed centre lines, street lights, benches, bins, trash bags, facing derived from PropRenderer transform), Prop TileEntitySpecialRenderers (deci.I.*), BlockProp facing transform from deobf PropRenderer (rotate 180 about x, metadata % 4 * 90 about y, then extra rotation; toward the road: road west 4, east 2, north 5, south 3), Car wreck model axis: long axis along x at 0 degrees (confirmed in game 2026-10-07), Dashed centre line (3 on 3 off, deci:BlockRoad_CenterLine, DeciTexturedBlock top texture by metadata % 4; meta 4 north south, 2 east west; never in or next to intersections), Prop facing rule (PropRenderer rotation = metadata % 4 * 90; north south street 5/3, east west 4/2) (+14 more)

### Community 63 - "StoreyPlan: StoreyPlan"
Cohesion: 0.19
Nodes (3): ApartmentPlanner, OfficePlanner, StoreyPlan

### Community 65 - "worldgen: Lot grading (city yards, 0.13.0: Graded plans own the 26x26 lot, Slices grades every lot column before writing)"
Cohesion: 0.19
Nodes (22): .bmodel is plain text Techne style code (earlier binary note was wrong), addChild is vanilla direction: parent.addChild(child) (old note said reversed; corrected 2026-10-10), Gun specific .bmodel header fields (mOff, sPos, flamePos, lhPos, rhPos, ejectPos, Scale), Unmapped .f(n) builder call (likely spread or sway), Fire mode enum deci.ay.e.a (SINGLE, AUTO, BURST, PUMP, BOLT), Gun registration call new i(...).f().am(), Genuinely new model recipe (needs Techne), Reskin an existing weapon recipe (fast path) (+14 more)

### Community 66 - "ZoneRules: ZoneRules"
Cohesion: 0.13
Nodes (6): ServerTickEvent, ZoneRules, net.minecraftforge.event.entity.living.LivingAttackEvent, ObjectZoneList, PlayerLoggedInEvent, PlayerLoggedOutEvent

### Community 68 - "gun_style_guide: Decimation gun style guide: study of all 98 shipped guns (look and construction rules for our own guns)"
Cohesion: 0.10
Nodes (22): study.py gaps: ours vs Decimation guns of the category, metric by metric (median, q10..q90, outside marks), incl. part sizes relative to length, Gun animation templates: Fire 2 frames slide only, Reload1 57 frames keyframes every 5 (SWITCH 20, LOAD 40, TRYBOLT 50), Rack 19 frames Hand 1, SlideBack static in 92 of 98, Gun attachments: NBT sightAttach / barrelAttach / gripAttach, category match or all, fixed offsets in model units, barrel follows flamePos, foregrip misplaced even on M4A4 [inferred], Construction rules: small shape boxes, layered receiver panels, grooves as thin raised plates, octagons from 3 parts per slice, skewed segments for grips and curved mags (addChild), chamfered edges, thin features thin, docs/references/decimation_guns.tsv: per gun parts, shaped %, rotated %, length / height / width, texture size, Where gun detail goes: receiver 30 to 70% of length holds half the parts, 3 of 4 parts in the top two fifths; first person shows top, right side, rear, First person gun look (dev test gunview): seen from rear, top, right; short guns are their rear section; MAC-10 stock plate fills the view, Gun icon style: 32x32 side silhouette, muzzle right, dark tones, 1 px black outline; make from the model side render (+14 more)

### Community 70 - "NoiseTest: NoiseTest"
Cohesion: 0.10
Nodes (21): Open bug: HUD and menu kill / death counters read the dead backend profile cache; Player Kills should become Human Kills (NPC humans and players) from local data, Open bug: with a real account the main menu shows an offline banner and grey Play offline (ClientState.bv off, dead backend) instead of Play on green, Fixed v0.42.2: local Human Kills / Infected Kills / Deaths (LocalStats) on HUD and menu, labels by PatchMenuStats, session flag valid (green Play), Open bug: military jeep / tank wreck blocks have hardness 0 and break with one punch (reported 10 Oktober 2026), Fixed v0.42.5: military wrecks hardness 10 (WreckHardness), 50 s by hand, CLAUDE.md v0.42.2 note and PatchMenuStats in the patch list, CLAUDE.md v0.43.0 note: gunshot noise, Roadmap: gunshot noise draws zombies and NPCs (unsuppressed shots), later (+13 more)

### Community 71 - "FurnitureSets: FurnitureSet"
Cohesion: 0.15
Nodes (5): com.google.gson.JsonArray, Condition, FurnitureSet, FurnitureSets, Set conditions: when kinds / storey / floors

### Community 72 - "LotPlan: LotPlan"
Cohesion: 0.11
Nodes (4): Shape, ZoneKind, LotPlan, FixedBase

### Community 73 - "DeciBiome: DeciBiome"
Cohesion: 0.13
Nodes (6): DeadTree, Override, DeciBiome, Override, net.minecraft.world.gen.feature.WorldGenAbstractTree, net.minecraft.world.gen.NoiseGeneratorSimplex

### Community 74 - "?: cpw.mods.fml.relauncher.SideOnly"
Cohesion: 0.14
Nodes (20): add(), acog(), eotech(), icon(), oct_y(), oct_z(), Gun space -> attachment file space (minus renderAttachments' translate)., 68 MOA ring, centre dot, four ticks across the ring (photo 62), red. Drawn… (+12 more)

### Community 75 - "?: DecimationWorldGen.java"
Cohesion: 0.33
Nodes (3): Building, net.decimation.worldgen.building.Building, ObjectZone

### Community 76 - "wasteland: docs/wasteland.md: the wasteland district as the city's military area (v0.54.0 to v0.56.0)"
Cohesion: 0.11
Nodes (20): v0.57.1 line: whole sector wasteland city, tools/topdown_blocks.py, Case 7: the wasteland ring was absurd (v0.56.0 -> v0.57.1), Lesson 17: split out / own city means made of its own kind, not cut away by empty land; streets stay joined, Lesson 18: user's sector = our district, user's district / city = our city sector, big chunk = cell, Dev test mode wasteland (WastelandTest), City dressing: the wasteland as the military area (v0.54.0), City v0.56.0: asphalt ramp for street stairs, natural bridge damage, metro entrance in the sidewalk, Idea 18: military airport in a wasteland city, half a sector 128 x 256, runway about 220 x 15, apron, hangars, tower, HQ, barracks; planes need models of our own (+12 more)

### Community 77 - "Capture: Capture"
Cohesion: 0.21
Nodes (3): Capture, JsonArray, JsonObject

### Community 79 - "safezone: Safezone sites doc: survivor camps (references, layout, residents, placement, test), fortified city building next"
Cohesion: 0.11
Nodes (19): Bug: camp guard flying over the wall (static guard spawned before its floor); keeper waits for populated chunks and puts residents back, fixed v0.47.2, Military base skill case 7: survivor camp first build (clashes, outhouse head room, fence gates in walkcheck), Military base skill case 9: flying guard, retaliation removed, entry sign, cosmetic props, CLAUDE.md v0.47.0: survivor camps, CanvasPlan shared, SafezoneSites, dev test camp, CLAUDE.md v0.47.1: camp people kept, dedicated trader reset respawn, camp command, CLAUDE.md v0.47.2: no provocation, keeper populated check and put back, sign, props, Safezone sites doc: survivor camps (references, layout, residents, placement, test), fortified city building next, Survivor camp layout: scrap wall, concertina, gate with safezone signs, 4 watchtowers, market of 6 stalls, plaza with campfire and water, medical tent, family tents, latrines, generator, garden (+11 more)

### Community 80 - "floorplan: Building quality audit (round 2 step 4a, baseline of v0.16.0 city buildings)"
Cohesion: 0.18
Nodes (18): Fix v0.16.1: solid wall cell behind the ladder, no furniture on the 4 cells around it, ladder shaft and stair core (plus 1 block ring) exempt from the collapse, Bug: upper storeys unreachable (ladder popped off, stair core collapsed; fixed v0.16.1), Knowledge index (docs/, interior spec, deobf notes, names.tsv, trackers), Knowledge index entry: docs/roadmap.md holds EVERYTHING planned in one list, v0.16.1: every storey reachable (ladder support, collapse spares the stairs); building audit, prop gallery, catalogue and spec (round 2 step 4a..c), Audit method: tools/floorplan.py per storey plans with reachability flood fill, plus runClient -Pautotest -Paudit (facade, ground, storey 1, roof of a sample apartment, office, shop); seed 1, Decimation world type, Building quality audit (round 2 step 4a, baseline of v0.16.0 city buildings), Finding: rubble is full mossy cobblestone cubes in corridors, rooms and doorways (reads as noise, cuts reachability) (+10 more)

### Community 81 - "loot_levels: Crate levels and NPC loot: design draft (13 Oktober 2026)"
Cohesion: 0.11
Nodes (18): Decimation's crates gave one item fewer than listed, stacks one short (IntRange half open); FIXED v0.57.0 by PatchLootRolls, PatchLootRolls.java: IntRange.pick (deci/aB/a) inclusive, three jars, User choices: every kind split by content type in 4 levels (military L1 magazines, L2 attachments, L3 guns, L4 all), colour band per level, L4 only in the rarest places, lootable NPC body, Four crate blocks (military, police, medical, civilian), metadata level (0..3) x 4 + facing: 16 crates, baked block render, no tile entity, Decimation loot today: 33 blocks, 12 hardcoded lists, 1 .. n-1 items per open (n 2 gives 1), rarity per item lootChance, 900 s refill, Crate levels and NPC loot: design draft (13 Oktober 2026), Duplication guard: Loot.closeFor closes loot screens on a body's inventory when it dies or the player is over 8 blocks away (Decimation's container never checks its source), Section 3.2 lists BUILT as LootPools built-in lists in config/deciworldgen_loot.json; magazines read from each gun; names in docs/references/decimation_items.tsv (+10 more)

### Community 82 - "SKILL: decimation-gun skill: 10 lessons for every gun from user reviews (ask for real photos, measure, Decimation look, accessories like the real gun, flamePos above bore, short gun attachments, aim side by side, clear line of sight, user screenshots win, show renders first)"
Cohesion: 0.13
Nodes (19): Skill casebook case 10: UMP9 as a variant of the UMP45 (how, magazine anchor, registration, checks), Skill case 12: UMP9 accepted without revision (variant route), Skill case 13: variants with many replaced parts (drop by position, fit to the kept receiver, colour twins by recolouring), Case 14: HK416 floating stock pieces and 2 pillar sight (v0.39.1), Case 15: MAC-10 parts not seated, contact check cleanup (v0.39.3), Case 20: verdict, sights accepted (v0.41.1), Lesson 15: every part sits on its host, sights like the real gun from behind, Lesson 17: iron sights in the defaultScopeModel group (+11 more)

### Community 83 - "ClickTest: FireRateTest"
Cohesion: 0.17
Nodes (5): ClickTest, FireRateTest, Entity, EntityLivingBase, EntityPig

### Community 84 - "military_base: Military bases design doc: US FOB style bases of our own in three sizes (COP 50x58, FOB 78x84, large FOB 112x118)"
Cohesion: 0.11
Nodes (18): CLAUDE.md v0.42.0 note: military bases, worldgen/military, milbase dev test, mil_ schematics no longer placed, Military bases accepted by the user on v0.42.4 (11 Oktober 2026), Military bases design doc: US FOB style bases of our own in three sizes (COP 50x58, FOB 78x84, large FOB 112x118), Base layout: perimeter rings, ECP south, HESCO wall with towers, ring and main road, TOC north, LSA west, logistics east, greedy placer for the rest, light decay, Real element to Decimation block mapping (HESCO = BlockMilitaryBarrier, T-wall = BlockBarrierTall, jersey = BlockBarrier, sandbags, wire, crates, radios, wrecks; tents smooth sandstone), Real FOB reference (sources): concertina, HESCO MIL7 walls, T-walls, guard towers, ECP with serpentine and search area, TOC, B-hut LSA, DFAC, aid station, ASP, fuel point, motor pool, helipad, burn pit, duck and cover bunkers, Military base test: devtest --live milbase (sizes, turns, seed, points; points=none lists refused modules), servertest seed 1 pregen=-64,448,9, Test trap: chunks generated after a sky base read its top as the ground; MilBaseTest pregenerates footprint + 3 chunks, own spot per size (+10 more)

### Community 85 - "LootPools: LootPools"
Cohesion: 0.23
Nodes (6): Entry, FileData, ItemStack, LootPools, Pool, net.minecraft.inventory.IInventory

### Community 86 - "UpdateScreen: UpdateScreen"
Cohesion: 0.20
Nodes (6): Hook, GuiButton, UpdateScreen, net.minecraft.client.gui.GuiButton, net.minecraft.client.gui.GuiScreen, net.minecraftforge.client.event.GuiOpenEvent

### Community 87 - "Furnisher: Furnisher"
Cohesion: 0.22
Nodes (4): Facing, Furnisher, Entry, net.decimation.worldgen.sets.FurnitureSet

### Community 89 - "LargeSites: LargeSites"
Cohesion: 0.20
Nodes (6): Plan, Schematic, Sub, LargeSites, Military sector sites always build our US FOB style bases (LargeSites.militaryBase); mil_ schematics no longer placed since v0.42.0, StructureGenerator

### Community 90 - "hk416: hk416.py"
Cohesion: 0.12
Nodes (17): CLAUDE.md v0.42.1 note and prop_placement.md index entry, Military base v0.42.1: deciworldgen:hesco, locked TOC door with keycard, gravel ground, sandbag ASP, props from drawn boxes, 0 clashes; tile entity props 129/381/571, Performance 6: props used as building material (BlockMilitaryBarrier HESCO, a tile entity per cell) replaced by deciworldgen:hesco; tile entity props per base 1020/2007/2902 -> 129/381/571, BlockMilitaryBarrier is a prop (tile entity per cell), not a plain block: never as bulk building material, Prop checks: Canvas.validateProps removes and logs clashes; tools/props/propclash.py on block dumps (WALL PROP DOOR FACE MOUNT FLOAT), Prop placement doc: how Decimation props are drawn (PropRenderer and ~75 own renderers, per renderer metadata tables, model offsets), facing, drawn sizes, rules, checks, Prop facing: placer rule front 2 E 3 S 4 W 5 N (checked props); exceptions big military radio (90 clockwise), military wrecks (long along z at 2/3), wall mounted keycard / wall flag / junction box, Prop render chain: BlockProp -> TileEntityProp -> PropRenderer (translate, flip about x, meta%4*90, extra rotation); setPropSize is only collision, setPropRenderSize only culling (+9 more)

### Community 91 - "LcContent: LcContent"
Cohesion: 0.16
Nodes (3): com.google.gson.JsonObject, Building, LcContent

### Community 92 - "InfectedVariants: InfectedVariants"
Cohesion: 0.18
Nodes (6): InfectedVariants, Variant, ItemStack, Zombie growth day 1 to 100: more of them, more specials, unlocks by day (melee 10, demons 30), LivingUpdateEvent, net.minecraft.entity.ai.attributes.IAttributeInstance

### Community 95 - "systems_plan: Systems plan: loot, human NPCs, zombies, world planned together (13 Oktober 2026)"
Cohesion: 0.12
Nodes (17): Roadmap: master plan pointer to docs/systems_plan.md, Master plan step 1 DONE v0.57.0; next step 2 loot, Claims: personal base (clear, plant flag) and city place claims (10 % of districts, clear all hostiles, caps revenue, decay back to nature when away), Systems plan: loot, human NPCs, zombies, world planned together (13 Oktober 2026), What exists today: infection one boolean (8 % bite), all Decimation armor CHAIN, demons 100 hp capped, radiation zones unused, turf income 20 caps per 15 min, Shared core: group spawner (MilitarySpawner logic for patrols, raids, reinforcements, horde waves), Horde nights every 10th night to day 50 then every 5th: no sleep, every infected targets the player, waves, demons, Infection in 5 stages ending in death and rising as an infected in your armor look; antibiotics stages 1 to 2, cure up to 4 (+9 more)

### Community 96 - "BlockBoomArm: BlockBoomArm"
Cohesion: 0.21
Nodes (7): Skill decimation-block: our own blocks (deciworldgen:<name>), decimation-props case 1: military base props review v0.42.0 -> v0.42.1 (about 40 clashes per FOB fixed, 0 left), Project skill decimation-props: placing Decimation props right in any structure (read, look up, write, check), lessons, casebook, BlockBoomArm, Override, net.minecraft.util.AxisAlignedBB, net.minecraft.world.IBlockAccess

### Community 97 - "furniture_sets: Furniture sets doc: data driven JSON furniture groups, user editable"
Cohesion: 0.12
Nodes (16): v0.20.0 furniture sets, wall lining, corner doors, one sided corridors, live loop, DeceasedCraft interiors fully authored per storey, no procedural rooms, Furniture sets doc: data driven JSON furniture groups, user editable, In-game capture: pos1/pos2, capture set/part, Set format: layers (floor, +1, under ceiling), row 0 against the wall, palette with face/type, rooms slot, weight, known.txt: unedited old built-in copies are updated (tools/asset_hashes.py), Named palettes and weighted styles for sets (base / style keys), Placement: seeded weighted order, every wall and offset, free cells off walkway, back against wall, no full height piece over a window (+8 more)

### Community 99 - "BiomeMap: BiomeMap"
Cohesion: 0.19
Nodes (6): BiomeMap, DeciGenLayer, Override, GenLayer swap on WorldTypeEvent.InitBiomeGens (TERRAIN_GEN_BUS): two DeciGenLayers (1:4 and 1:1) reading one BiomeMap, net.minecraft.world.gen.layer.GenLayer, NoiseGeneratorSimplex

### Community 100 - "worldgen_architecture: Worldgen architecture v3 draft (layers, assets, size classes, capture tool)"
Cohesion: 0.13
Nodes (16): Builders: separate ceiling tiles with light panel grid and vents, Study: hand-built Decimation maps (USA coast, Decicraft, Cloverfield, world-e161), Builders: decay as dirt/leaves/water/cracked glass on intact shells, Builders: furniture in rows and islands (waiting rows, cubicles, shelf aisles), Autotest -Pstudy camera mode for reference maps, Builders: two tone WallOffice walls (dado bottom + top), Audit shots v0.23: 6 high offices and shops, Worldgen code map: building package parts (Shell, StoreyPlan, planners, Furnisher, Surfaces, Interior, Ruins, Yard) (+8 more)

### Community 101 - "city_engine: City engine: Lost Cities style cities from converted DeceasedCraft content"
Cohesion: 0.24
Nodes (13): bmodel(), evaluate(), gl_calls(), gl_ops(), java_model(), model_points(), num(), Prop (+5 more)

### Community 102 - "ideas: Ideas for later (user, 12 Oktober 2026)"
Cohesion: 0.15
Nodes (15): Bug: floating district name showed _ (FIXED v0.52.1), ClanTurfLayer (deci.M.l): floating turf name over the turf's middle, Dev test places, Ideas for later (user, 12 Oktober 2026), Ideas planned together 13 Oktober 2026 in docs/systems_plan.md, Place names: districts and bases through Decimation's turf banner (v0.52.0), City names on entering: [district] - [number], More crate / loot block types (industrial, civilian) (+7 more)

### Community 103 - "SKILL: City rivers and bridges v0.45.0 / v0.45.1: river contour through cities as canal chunks, street chunks carried over on decks, river cells level 0, new worlds only"
Cohesion: 0.14
Nodes (15): decimation-city case 2: the wasteland as the city's military area (v0.54.0), Skill decimation-city: city work order, server and dev test checks, lessons (bridges continue the street), casebook case 1, decimation-city case 1: bridges did not match the roads (v0.45.0 -> v0.45.1), CLAUDE.md v0.45.0 note: rivers through cities only for worlds created since 0.45, dev test bridge, game check as own step, CLAUDE.md v0.45.1 note: bridges are the street over the canal, DC bridge parts dropped, skill decimation-city, Bridges v0.45.1: the street (StreetPlan onWater) carried over the canal by RiverPlan, deck and piers below, railings on non street edges, wasteland bridges broken with a hole, City rivers and bridges v0.45.0 / v0.45.1: river contour through cities as canal chunks, street chunks carried over on decks, river cells level 0, new worlds only, Roadmap: bridges DONE v0.45.0, revised v0.45.1 (street over water), waiting for the user's look (+7 more)

### Community 104 - "SightPlacement: .draw()"
Cohesion: 0.22
Nodes (14): desc_params(), ident(), is_obf_member(), load_classes(), main(), norm_desc_type(), norm_src_type(), params_match() (+6 more)

### Community 105 - "city_engine: City highways: seed based network between city regions, open / bridge / tunnel chunks, deck at 64, side ramps"
Cohesion: 0.16
Nodes (13): CLAUDE.md v0.29.0 note: highway polish, pregen to check far away L links, CLAUDE.md v0.44.0 note: underground railway, lcpack --rails, springs denied near rails, rail dev test, server fix, Highway chunk kinds: TUNNEL (median 6+ above deck), BRIDGE (water or ground 2+ below, stone brick pillars), OPEN; stored as hw_X_Z, Highway L links (v0.29.0): isolated city region joined to its nearest diagonal city (at most 2 regions) by an L of the region lines; seed 1: 10 links, city groups 29 -> 19, Highway polish v0.29.0: hedges on crossing parts facing open land, no tunnels in city edge bands, side ramps beside bridges cut land above the deck only, Highway side ramps beside OPEN chunks (deck to natural height over 3..8 blocks, shared LcCity.reshape), City highways: seed based network between city regions, open / bridge / tunnel chunks, deck at 64, side ramps, City railway v0.44.0: underground subway on the highway lines at y 46, a station per city, tunnels between cities, buffer ends, crossings (+5 more)

### Community 106 - "lctranslate: lctranslate.py"
Cohesion: 0.29
Nodes (13): colour_of(), furniture(), material(), parse(), Rule based translation of 1.20 block states (vanilla + furniture mods) to…, Furniture of any mod -> one cell prop or vanilla stand-in; None if not…, The name without words that only look like "light": light_gray / light_blue…, 1.7.10 stone_slab / wooden_slab for a material. (+5 more)

### Community 107 - "TerrainEvents: TerrainEvents"
Cohesion: 0.17
Nodes (8): Railway tunnels flooded with standing water and lava (fixed v0.44.0), Decorate, TerrainEvents, Springs and lakes denied near rail chunks: a tunnel written by two windows was flooded by a neighbour's spring, Terrain: no lakes or springs where their area touches a railway chunk (v0.44.0), InitBiomeGens, net.minecraftforge.event.terraingen.InitMapGenEvent, Populate

### Community 108 - "Deci: .onServerTick()"
Cohesion: 0.22
Nodes (4): SupplyDropScheduler.drop skips a candidate column whose top block has a tile entity (prop, chest, car) and tries the next of its 12 random positions, ServerTickEvent, SupplyDropScheduler, net.minecraft.server.MinecraftServer

### Community 109 - "zones: Zones doc: Decimation's zone system end to end and what ZoneRules makes work in singleplayer"
Cohesion: 0.15
Nodes (13): Bug: zone rules dead in singleplayer (server proxy handlers), fixed v0.46.0, CLAUDE.md v0.46.0: zones work in singleplayer, docs/zones.md, Radiation zones: irradiated biome, crash and blast sites, leaking labs and bunkers, the wasteland city, Zones doc: Decimation's zone system end to end and what ZoneRules makes work in singleplayer, ZoneManager checks: isPlayerInZone (safezone false while the combat timer runs), isEntityInZone, near = box plus 10 blocks, Client zone lists: PacketZoneLists replaces ClientState bS..bW; HUD safezone chat line, GPS map markers; never sent for generated zones before v0.46.0, Zone data: ObjectZone boxes, EnumZoneType (safezone, radiation, military, police, scary, border), one live list ServerConfig.zoneList, ZoneStore merges ours, No provocation since v0.47.2: a hit on a protected NPC in a safezone does nothing (user: friendlies hit by accident) (+5 more)

### Community 110 - "CLAUDE: tools/build.py real javac pipeline"
Cohesion: 0.19
Nodes (11): deobfuscation_data-1.7.10.lzma notch to SRG mapping, Compile only shim for Forge binpatch members (tools/shim_src), tools/build.py real javac pipeline, SpecialSource notch to SRG remapping, SRG member names (no reobfuscation step), classpath(), compile_sources(), inject() (+3 more)

### Community 111 - "DevTestLive: DevTestLive"
Cohesion: 0.19
Nodes (5): Live dev test mode: tools/devtest.py --live [--swap] [--stop]; game stays open (-Plive, DevTestLive on 127.0.0.1:25599), ready in about 26 s, reruns only cost their own time; hotswap for method bodies, Command, DevTestLive, Recently done: live dev test mode, java.net.Socket

### Community 112 - "gun_model_spec: Our own guns pipeline (gun_model_spec section 6): tools/guns spec to .bmodel / texture / icon / .anib, bbmcp previews, NewGuns registration (newGun, newMagazine, addLootLike, useGunSounds), dev test mode gun"
Cohesion: 0.21
Nodes (13): Skill casebook case 9: closing the gaps after acceptance (method, numbers, checks), CLAUDE.md v0.36.0: MAC-10 pipeline, our items register as deciworldgen:<name>, BModelBox facts: corner array order and added offsets, box UV face layout, Decimation textures 2 px per unit, 32x32 icons, Our own guns pipeline (gun_model_spec section 6): tools/guns spec to .bmodel / texture / icon / .anib, bbmcp previews, NewGuns registration (newGun, newMagazine, addLootLike, useGunSounds), dev test mode gun, gunmodel.py v2: hexahedron parts (part, inset, shift, mirror, octagon), 1x1x1 declared with corner offsets, flat tones, textureWidth 512 UV step 8, icon from the study.py render, anib with slide_names, MAC-10 accepted by the user (v0.37.2): aim fixed, likes the firing style, icon good art, MAC-10 v0.37.3 gaps closed: shape kinds 60/31/5/5, 110 parts, declared sizes 1x1xN, tone 47, middle details, raised details 0.08, MAC-10 v2 (v0.37.0): 102 parts, 9.35 x 8.1 x 1.5, proportions from the user's side photo, sights at the aim centre, checked in game (gunview, gun) (+5 more)

### Community 113 - "Shell: Shell"
Cohesion: 0.19
Nodes (11): Lesson 16: base parts reading past textureHeight wrap (M4A4 rear sight), Variant texture trap: M4A4 rear sight reads rows past textureHeight 32, black_part(), build(), dropped(), lower_bottom(), y of the lower panel's bottom edge at x (the wedge's front face is collapsed to…, The M4A4 parts our HK416 replaces (by position, measured on the M4A4:… (+3 more)

### Community 114 - "DevAutoTest: DevAutoTest"
Cohesion: 0.28
Nodes (5): ClientTickEvent, Command, DevAutoTest, IntegratedServer, net.decimation.worldgen.devtest.DevTestMode

### Community 115 - "PlaceNames: PlaceNames"
Cohesion: 0.23
Nodes (4): NamedPlace, World, PlaceNames, Shared core: areas registry (SAFEZONE, MILITARY, RADIATION, BASE_CLAIM, CITY_CLAIM with states) on Decimation's zone and turf lists

### Community 117 - "StructureData: StructureData"
Cohesion: 0.18
Nodes (3): Override, StructureData, net.minecraft.world.WorldSavedData

### Community 120 - "lc2schem: lc2schem.py"
Cohesion: 0.31
Nodes (9): building_columns(), main(), matches(), Pack, part_slices(), (layers bottom up, ground layer index): every floor picked like Lost Cities…, Lost Cities part conditions for one floor (cellars are negative)., tag() (+1 more)

### Community 121 - "lcpack: lcpack.py"
Cohesion: 0.29
Nodes (12): convert(), export_rails(), main(), paint(), rail_override(), rasterise(), Railway parts: Create tracks and vanilla rails become 1.7.10 rails (meta 1…, Lost Cities railway parts (RailwayParts.DEFAULT names, the pack's own versions)… (+4 more)

### Community 122 - "bug: Bug: armor buff ignores NPC gunfire (fixed)"
Cohesion: 0.24
Nodes (9): Bug: armor buff ignores NPC gunfire (fixed), DamageSource split: gunDeci (player) vs human/turret (NPC), Helmets give no gun protection (slot 3 excluded), Helmet counts on headshots only (v0.9.1: aim line for player guns, 20% random for NPC), Proposed LivingHurtEvent gunshot damage unification, Two gunshot DamageSource identities (gunDeci player, human NPC), DeciDamageSources (deci.aD.h), PacketGunHit handler (deci.aE.a$z$a) (+1 more)

### Community 123 - "bug: Bug: armor buff ignores NPC gunfire (fixed)"
Cohesion: 0.24
Nodes (11): Thin props passed every overlap check (FIXED v0.50.0), propclash fixes: trapdoor not a door, walkway only, plates flat, glass view, load_facing(), load_geometry(), main(), overlap(), passage(), True when a door at this cell is walked through along x, False along z, None:… (+3 more)

### Community 124 - "PatchTurfNames: PatchTurfNames"
Cohesion: 0.20
Nodes (5): ClassPool, javassist.ClassPool, PatchLootRolls, PatchNoUpdate, PatchTurfNames

### Community 125 - "DecimationBiomes: DecimationBiomes"
Cohesion: 0.24
Nodes (6): DeciBiome, DecimationBiomes, Biome names carry AmbientMusicPlayer keywords (forest, river, plains, hills, decimated, irrated), Fixed biome ids 110..118 (Decimated City, Suburbs, Irradiated Military Zone, Decimated Plains, Burnt Forest, Overgrown Plains / Forest / Hills, Murky River), Spawn trap: Decimation adds its spawns to biomes existing in its preInit and strips vanilla monsters in its init; our biomes are made in our preInit and copySpawns copies plains lists in postInit, net.minecraft.world.biome.BiomeGenBase

### Community 127 - "CityLayoutEvents: CityLayoutEvents"
Cohesion: 0.18
Nodes (4): CityLayoutEvents, Load, Sectors, Unload

### Community 128 - "city_engine: City engine: Lost Cities style cities from converted DeceasedCraft content"
Cohesion: 0.24
Nodes (12): apply(), cells(), main(), mat_id(), mat_mul(), The metadata drawing this prop turned a quarter clockwise (seen from above:…, Cells the drawn box covers past its own (overhang under 0.3 ignored)., rotate() (+4 more)

### Community 129 - "UpdateSwap: Auto update from our GitHub releases (v0.59.0): Updater check, Decimation's Update button via PatchNoUpdate v2, UpdateScreen progress, sha256 check, UpdateSwap after the game closes"
Cohesion: 0.20
Nodes (7): No city on a friend's install: city pack never in a release; FIXED v0.58.0 by one jar (tools/onejar.py, BundledContent), checked on a real Forge server, v0.58.0: release is one Decimation.jar from tools/onejar.py; test on a real Forge server; ScopeZoom copy change; dev tests click, hitbox, v0.59.0 line: auto update, release rules (asset Decimation.jar, dotted tag), dev test update with the fake release server, UpdateSwap, Idea 20: auto update from GitHub releases with an Update button and progress screen, swap after the game closes; project page on Vercel, update_v0.59 shots: Update button, progress 33 %, ready screen, Auto update from our GitHub releases (v0.59.0): Updater check, Decimation's Update button via PatchNoUpdate v2, UpdateScreen progress, sha256 check, UpdateSwap after the game closes

### Community 130 - "bug: Done v0.30.3: NPC shots traced with spread per tier, stopped by walls, impact particles, tracer always visible along the real line (PatchTracer v2 shotHook + NpcShots)"
Cohesion: 0.18
Nodes (11): Bug (fixed v0.30.2): full military armor made NPC gunfire almost harmless: armor multiplies per piece (x0.149 marine set) and vanilla hit cooldown dropped group hits; NPC shots are direct damage, not bullets, v0.30.4: NPC hit cooldown 0.25 s (npcHitCooldownTicks 5), v0.30.3: vanilla hit cooldown kept for NPC hits (npcHitsSkipCooldown false), x5 stays, Fix v0.30.2: NPC gun hits on players x npcDamageToPlayer (5) after armor, every NPC hit lands (LivingAttackEvent clears hurtResistantTime); bare 10 hp, marine set 1.49, 5 hits 7.47, Done v0.30.3: NPC shots traced with spread per tier, stopped by walls, impact particles, tracer always visible along the real line (PatchTracer v2 shotHook + NpcShots), CLAUDE.md v0.30.2 note: NPC hits x5, no hit cooldown, mech swap, CLAUDE.md v0.30.3: traced NPC shots, PatchTracer v2 then PatchFactions, test NPCs on a block, Balance: NPC gunfire on the player x5 after armor, every hit lands (v0.30.2) (+3 more)

### Community 131 - "SightPlacement: .draw()"
Cohesion: 0.22
Nodes (5): Bug: sights float above flat top rifles (v0.40.1), Case 17: sights floating above flat top rifles, Case 19: reticles fixed on screen, EOTech too big, HK416 front sight (v0.41.1), SightPlacement, SightReticle

### Community 132 - "gun_style_guide: Gun aiming: aim mode draws every gun at one fixed place (x 0.5, translate -1 -0.35 0.923), sPos unused in first person; screen centre at the top of the iron sights, y -4.85 to -5.0, z -0.15"
Cohesion: 0.20
Nodes (11): decimation-gun skill revision log: lessons from every user review (v1, v2, v0.37.0, v0.37.1), Aim rule corrected v0.37.2: centre passes the Uzi's rear aperture hole (about -4.65); compare with a Decimation gun in the same shot, Aim sway: headYawSway / dP tilt the gun after mouse moves, so judge aim only steady, Barrel attachment formula meets the muzzle only for flamePos x 12 to 15; Deci.offsetAttachment puts an offset model copy in AttachmentItem.ST per gun (MAC-10 -1.02, +0.14, 0), flamePos y sits about 0.85 above the bore (Uzi -4.5 over barrel -3.5); barrel attachments hang from it, Gun aiming: aim mode draws every gun at one fixed place (x 0.5, translate -1 -0.35 0.923), sPos unused in first person; screen centre at the top of the iron sights, y -4.85 to -5.0, z -0.15, Gun texture gradation: Decimation 15 to 31 part tones, faces 4.7 to 5.7 apart, texel noise 2 to 2.7, top lighter; gunmodel.paint reproduces it, Gun placement conventions: top at y -5, z centre -0.15, flamePos at muzzle minus 0.1, rhPos / lhPos medians per category, no sPos / mOff / Scale (+3 more)

### Community 133 - "Doors: Doors"
Cohesion: 0.22
Nodes (5): Worldgen rule: no vanilla doors in anything we generate (Doors.swap), Doors, Plan, Styled, Vanilla doors become Decimation doors at write time (worldgen/Doors)

### Community 134 - "GunNoise: Override"
Cohesion: 0.27
Nodes (3): Investigate, net.minecraft.entity.ai.EntityAIBase, net.minecraft.entity.EntityCreature

### Community 135 - "Protection: Protection"
Cohesion: 0.40
Nodes (3): FileData, Protection, Values

### Community 136 - "Palettes: Palettes"
Cohesion: 0.29
Nodes (3): Palettes, Style, Entry

### Community 138 - "ZoneStore: ZoneStore"
Cohesion: 0.33
Nodes (3): BridgeTest, Dev test bridge: forces city rivers on, fresh span with city streets at both ends, deck, water, no building over canals, walk across, lc_bridge_v0.45.0 shots: seed 1 world map and top down renders of canals and bridges, first dev test bridge views

### Community 139 - "AssetDir: AssetDir"
Cohesion: 0.38
Nodes (4): World, WorldGenCommand, net.minecraft.command.CommandBase, net.minecraft.command.ICommandSender

### Community 140 - "SealedCaves: SealedCaves"
Cohesion: 0.27
Nodes (6): Caves, Override, Ravines, SealedCaves, net.minecraft.world.gen.MapGenCaves, net.minecraft.world.gen.MapGenRavine

### Community 141 - "gunmodel: Part"
Cohesion: 0.18
Nodes (7): octagon(), Part, A hexahedron: corner (ix, iy, iz) in {0, 1}^3 -> absolute model point., Shrink the face at `side` (0 = low end, 1 = high end) of `axis`: amounts per…, Move the face at `side` of `axis` (a skew / shear)., The same part mirrored about z = ZC (left / right pairs)., A round bar along x as Decimation builds it: a middle slab plus a trapezoid…

### Community 142 - "mapsurvey: mapsurvey.py"
Cohesion: 0.25
Nodes (6): chunk_ids(), chunks(), colour(), main(), ids[y, z, x] for the chunk (256 high)., registry()

### Community 143 - "lcaudit: lcaudit.py"
Cohesion: 0.27
Nodes (9): Converted buildings used LED lamp blocks as floor (FIXED 9 Oktober 2026: 'light' in colour names like light_gray matched the lamp rule), Knowledge index: translation audit tools/lcaudit.py, lcpack content: 290 converted buildings, stairs per district style (local only), Translation audit 9 Oktober 2026: dropped blocks and props by placement over 290 converted buildings; black sandstone is asphalt, laboratory panels, wallpaper, corundum, posts, shelves, seats mapped, Recently done: converted building quality pass (pack rebuilt, LED floor fixed, about 30000 dropped blocks mapped), lc_quality_v0.28.10 shots: 30 interior views after the translator fix, no lamp floors, ceiling redstone lamps; black sandstone stairs now cobblestone, buildings(), main() (+1 more)

### Community 144 - "gun_model_spec: Fix: SmoothSwingThread busy wait (PatchSwing 4 ms sleep)"
Cohesion: 0.24
Nodes (8): tools/patches/PatchSwing.java, Fix: SmoothSwingThread busy wait (PatchSwing 4 ms sleep), ClientState (deci.b.i), SmoothSwingThread (deci.b.h), Attachment fixed offsets on rails, BModel / BModelPart (deci.n.f, deci.n.b), GunItemRenderer (deci.K.b), PatchSwing

### Community 145 - "DECIMATION_MOD_TASK: Goal: loot crates and cars in singleplayer"
Cohesion: 0.22
Nodes (10): CFR decompiler, rtk hook drops grep/find flags (use Python os.walk), Toolchain set up each session (nothing preinstalled), Hypotheses: dedicated gate, YAML config path, dedicated lifecycle event, Personal use only, no redistribution, backup first, Prism Launcher instance mods folder, Goal: loot crates and cars in singleplayer, Singleplayer loot fix task brief (+2 more)

### Community 146 - "city_engine: LcCity"
Cohesion: 0.20
Nodes (8): v0.25.0 highways, v0.26.0 parks / street scenes / fronts, v0.27.0 district Lost Cities street parts, City decor: parks on open lots, street scenes (pack fountains), building fronts on the street side, Fronts: building chunk beside a straight street gets a district front part (FRONT_CHANCE 0.5), resolved lazily at write time, Parks: open lots LOT_CHANCE 10% of building chunks carry a district park part one layer up, Props of turned street parts turned in partAt (PropBoxes.turn), Road paint: refueled mod decals converted by lcpack paint() into Road_CenterLine and quartz zebra; odd turns flip line meta, District street parts: LC street parts by connection count, road paint to painted road blocks, Street scenes: DeceasedCraft 'fountains' (bus, ambulance, roadblock, trash) in 6% of straight street chunks

### Community 148 - "CityEdgeTest: CityEdgeTest"
Cohesion: 0.42
Nodes (3): ChunkProviderServer, CityEdgeTest, City edge worn ground (v0.55.0): EdgePlan.wear broken pavement band, coarse dirt rejected (orange)

### Community 149 - "SKILL: City sizes v0.60.0: 5x5 sector cells, one compact city of 4 to 64 districts each, wasteland 1 per 4 sectors beside a military sector, new worlds only (!citySizes)"
Cohesion: 0.28
Nodes (9): Refuges 1 per 4 city sectors (v0.60.2), Case 8: cities of many sizes (v0.60.0), Case 9: refuges in big cities (v0.60.2), Lesson 19: sector layout changes need cache epochs and default ON (spawn search before WorldEvent.Load), Lesson 21: recount one-per-part placements when a feature scales, City sizes v0.60.0: 5x5 sector cells, one compact city of 4 to 64 districts each, wasteland 1 per 4 sectors beside a military sector, new worlds only (!citySizes), Idea 19: city sizes of 8 to 64 districts instead of mostly one sector (4 districts), Feature: cities of 4 to 64 districts (v0.60.0) (+1 more)

### Community 150 - "Heuristic Auto Namer"
Cohesion: 0.28
Nodes (7): Case 18: Mk18 TS stock, EOTech 558, ACOG TA11 (v0.41.0), build(), dropped(), lower_edge(), px(), y of the stock's bottom edge (the lower strut) at model x: photo (480, 1470) to…, The M4A4 parts our Mk18 replaces (by position, measured on the M4A4:…

### Community 151 - "Condition: com.google.gson.JsonObject"
Cohesion: 0.22
Nodes (9): v0.21.0 critic pass 1, kitchen rework, wall breaches by column, narrow blocks ladder, -Pflats audit, Apartment references: real-world clearances, 1.7.10 furniture techniques, review checklist, Apartment review checklist: walkway, function readable, 40-60% free, palette, plausible decay, per room rules, Clearances: 1 block walkway, sofa-table 0-1 block, one free bed side, kitchen work triangle in a 4-6 block run, 1.7.10 techniques: stairs sofas with trapdoor arms, slab coffee tables, cauldron sink + tripwire tap, quartz stair toilet, paintings, wool curtains, Critic pass 1 on v0.20 flats: overall 3/10, verified findings and fixes, critic wrong on chair facing and lintels, Root cause found: narrow apartment blocks had no flats (core took the width); ladder under 16 wide, Open after pass 1: empty living fallback, wall detail layer, sofa arms, deeper living sets, bath sets, bedroom min width, camera (+1 more)

### Community 152 - "DecimationBiomes: DecimationBiomes"
Cohesion: 0.33
Nodes (8): camel(), classes(), known_fields(), main(), (binary name, source text) for every top-level file., Field names already chosen by the AI tables: (owner, obf) -> name., Field names declared directly in the outer class (indent 4)., top_level_fields()

### Community 153 - "StreetProps: Slices.java"
Cohesion: 0.44
Nodes (3): StreetProps, net.decimation.worldgen.Plan, net.decimation.worldgen.StructureGenerator

### Community 154 - "edgescan: gradescan.py"
Cohesion: 0.29
Nodes (7): walkcheck stopped at y 4, propclash counted shelf tops as floating (FIXED v0.48.0), CLAUDE.md v0.42.4 note, How to test: walkcheck.py rules and the must-be-0 findings, Prop rule 12: every loot container reachable (walk test LOOT), collision_table(), main(), Prop block -> (collision height, walk through) from Decimation's registry code.

### Community 155 - "SKILL: Project skill decimation-gun: end to end gun workflow (read docs, references, part plan, anchors, build, compare with study.py, register, gunview test, finish)"
Cohesion: 0.25
Nodes (8): Skill casebook case 11 and lesson 14: verify performance for every gun; the 1 fps report was a corrupt chunk and infected pathing, Case 21: AS Val sights on the handguard, moved 4 back over the receiver (v0.42.2), Project skill decimation-gun: end to end gun workflow (read docs, references, part plan, anchors, build, compare with study.py, register, gunview test, finish), Lesson 23: sights must sit over the receiver (fixed sight zone x 0.8..5.5; AS Val receiver ends at 2.6), sightfit SHIFT dx, aim and reticle compensated, create_weapons.md: new models are built in code by tools/guns and the skill decimation-gun since v0.36.0; Techne is the older manual route, Style guide 14: sight position along the gun, SHIFT for guns whose receiver ends before the sight zone, Roadmap items and weapons (11 Oktober 2026): waiting looks (MAC-10 seated, HK416 fixes), more scopes, more guns, sniper nerf pointer, documentation.md: guns of our own (MAC-10, UMP9, HK416 / Tan, Mk18; rpm, damage halved by the global patch, loot with their model gun) and sights EOTech 558 / ACOG TA11

### Community 156 - "SKILL: Project skill decimation-military-base: research, build (Canvas, occupancy grid, greedy fit, poi), test, small detail review checklist"
Cohesion: 0.25
Nodes (8): Military base case 1: first FOB generator, four rounds (colours, test setup, overlaps, density), Military base case 2: user review of v0.42.0 (HESCO prop FPS, unlocked TOC door, orange dirt, prop clashes) -> v0.42.1, Military base case 3: second review, test site moved to -20000 y 230, old sites cleared, multiblock rebuild trap, Military base case 4: third review (blocked hatch, floating hangar roof, shelter roof, worldgen cutting the sky test), Military base case 5: walled in ladders and a missing TOC (v0.42.4), Lessons 12 and 13: check the player's way to a thing; refused modules must be loud, Military base lessons: research first, occupancy grid against overlaps, company streets, sky test without grading, orange dirt in Decimation's pack, avoid regular patterns, camera points in the generator, Project skill decimation-military-base: research, build (Canvas, occupancy grid, greedy fit, poi), test, small detail review checklist

### Community 157 - "Updater: State"
Cohesion: 0.25
Nodes (8): State, AVAILABLE, CHECKING, DOWNLOADING, FAILED, IDLE, NONE, READY

### Community 163 - "study: .verts()"
Cohesion: 0.36
Nodes (6): contact(), inside(), True when point q lies in the part's (convex) shape, grown by eps., Parts that do not sit on anything. A thin detail (0.15 or less in some axis):…, The 8 corners in model space, indexed like vanilla ModelBox: v7 (0,0,0), v0…, (4 vertices, (u1, v1, u2, v2)) per face, like vanilla ModelBox.

### Community 166 - "graph_update: graph_update.py"
Cohesion: 0.52
Nodes (6): finish(), prepare(), Keep an old community name when its members mostly carried over., relabel(), rj(), wj()

### Community 167 - "sightfit: sightfit.py"
Cohesion: 0.43
Nodes (6): bbox(), gap(), main(), rail_top(), Top of the receiver / rail in the sight zone: the highest long part there, iron…, dy > 0: the sights float that much above this gun's rail (they are placed for…

### Community 168 - "SKILL: Props fitted to their drawn size (v0.49.0, city/PropFit)"
Cohesion: 0.33
Nodes (6): Converted city props into walls / floating / facing walls (FIXED v0.49.0), Case 2: converted city props (v0.49.0), Lesson: converted content needs the drawn size check, fix by rule at load, Dev test cityprops (CityPropsTest): fresh city cell, shots of fitted props, Props fitted to their drawn size (v0.49.0, city/PropFit), worlddump.py: props of a generated world for propclash

### Community 170 - "graph_update: graph_update.py"
Cohesion: 0.33
Nodes (6): v0.19.0 storey height 5 with own ceilings, Revised order after user 0.18 review: structure first, then furniture sets, then reference library + critic, Step 1a done v0.19.0: storeys 5 high, own white plaster ceiling layer, 5 step stair runs, Step 1b next: plaster lining inside outer walls, with furniture sets, audit_v0.19: dark tile ceilings first, then white plaster ceilings, Revised interior plan after user review: structure, data driven furniture sets, references + critic; Lost Cities idea not port

### Community 172 - "perfcheck: perfcheck.py"
Cohesion: 0.33
Nodes (5): NpcKind, BANDIT, HAZMAT, SOLDIER, SOVIET

### Community 173 - "WorldClock: Event"
Cohesion: 0.33
Nodes (5): Event, DAWN, DAY, DUSK, NIGHT

### Community 176 - "DecimationWorldType: DecimationWorldType"
Cohesion: 0.40
Nodes (3): DecimationWorldType, Override, net.minecraft.world.WorldType

### Community 177 - "production_test: Testing the shipped game: production Forge server and client in the scratchpad, driven by devtest.py --live"
Cohesion: 0.40
Nodes (5): Bug: left click does nothing with fists or melee (closed: friend's own key binding), Knowledge index entry: docs/production_test.md, Testing the shipped game: production Forge server and client in the scratchpad, driven by devtest.py --live, Left click in the production client: all PASS with simulated presses; real mouse click untested, Traps: Forge universal from a server folder pulls minecraft_server via its manifest; -XstartOnFirstThread hangs LWJGL 2; javadoc jar; first test in a new world while falling; GitHub API 403 rate limit

### Community 178 - "CLAUDE: Military base v0.42.3: tower ladder in the middle of the inner side, hangar vault closed with loot, shelter full roof, test chunks pregenerated"
Cohesion: 0.40
Nodes (5): CLAUDE.md v0.42.3 note, Military base v0.42.3: tower ladder in the middle of the inner side, hangar vault closed with loot, shelter full roof, test chunks pregenerated, Roadmap recently done v0.42.3, milbase_v0.42.2_review (user 109..114) and milbase_v0.42.3 shots, new_feature v0.42.3 third review

### Community 179 - "PatchIronSights: Case 16: Mk18 Mod 1 and iron sights hidden under a sight (v0.40.0)"
Cohesion: 0.40
Nodes (3): Case 16: Mk18 Mod 1 and iron sights hidden under a sight (v0.40.0), defaultScopeModel parts hidden while a sight is attached (19 guns), PatchIronSights

### Community 180 - "safezone: City refuge: fortified building on the block nearest each city region's middle (v0.48.0)"
Cohesion: 0.40
Nodes (5): Case 10: city refuge, first build (v0.48.0), Lessons 18-21: stairs in walkcheck, rotations via live, cameras, multiblock rebuild, City refuge: fortified building on the block nearest each city region's middle (v0.48.0), !cityRefuges world flag: refuges only in worlds created since v0.48, Refuge layout: apron barriers, concrete wall, chicane gate, 3 storey building (market hall, clinic, canteen, dormitory, armory, roof nests), yard

### Community 181 - "new_feature: Zombie variants v0.34.0: runner, riot, screamer (scream alerts infected within 32), night frenzy; InfectedVariants, ZombieEgg, config deciworldgen_zombies.cfg; dev test mode zombies"
Cohesion: 0.40
Nodes (5): CLAUDE.md v0.34.0: zombie variants, attribute modifiers trap, Zombie variants (v0.34.0) in documentation, zombies_v0.34.0: lineup common, runner, riot, screamer, InfectedEntity facts: 20 hp, attack 3, speed base reset every tick (0.25, horde 0.3), zone looks military / police, 8% bite, Zombie variants v0.34.0: runner, riot, screamer (scream alerts infected within 32), night frenzy; InfectedVariants, ZombieEgg, config deciworldgen_zombies.cfg; dev test mode zombies

### Community 183 - "NpcKind: NpcKind"
Cohesion: 0.70
Nodes (4): axis(), clean(), letter(), main()

### Community 184 - "RefugeTest: RefugeTest"
Cohesion: 0.80
Nodes (4): clamp(), main(), side(), top()

### Community 185 - "shots_index: Bug: survivor camp NPCs missing (test camp emptied by the test; Decimation's trader reset is dedicated server only), fixed v0.47.1"
Cohesion: 0.50
Nodes (4): Bug: survivor camp NPCs missing (test camp emptied by the test; Decimation's trader reset is dedicated server only), fixed v0.47.1, Military base skill case 8: the npc is missing (test cleanup, trader reset, awning front colour), safezone_camp_v0.47.1 shots: coloured awning fronts with traders, camp command aerial on real ground, safezone_camp_v0.47.0_review: user_119 empty market of the test camp, white awnings from the street

### Community 186 - "CLAUDE: New players forced to update (Update instead of Play, empty server list): UpdateChecker.isUpdateRequired true without DecimationVoiceChat.jar; FIXED v0.57.2 by PatchNoUpdate"
Cohesion: 0.50
Nodes (4): New players forced to update (Update instead of Play, empty server list): UpdateChecker.isUpdateRequired true without DecimationVoiceChat.jar; FIXED v0.57.2 by PatchNoUpdate, PatchNoUpdate.java: net/decimation/mod/common/utils/h.gI always false, three jars; dev test menu, v0.57.2: no forced Update for players without the voice chat jar, menu_v0.57.2: home menu without the voice chat jar, Resume Game, no Update button

### Community 188 - "new_feature: Worldgen round 2 plan (option A: terrain blending, street life, full multiblocks, new building types, loot balance)"
Cohesion: 0.50
Nodes (4): v0.18.0 apartment rooms and propFacing fix, Step 3 apartment rooms done v0.18.0: kitchen run, dining, TV + sofa, bedroom, bathroom, lobby on one side, audit_v0.18 / plans_v0.18: kitchen run, checker ceiling issue, furnished flats, 4d.3 apartment rooms done v0.18.0, next 4d.4 office programs

### Community 191 - "ideas: Wasteland locked doors (v0.55.0): most buildings locked by loot, Door_Emergency_1_Locked + military keycard screen, flood fill entrances"
Cohesion: 0.50
Nodes (4): Idea 17: crate levels 1 / 2 / 3 with richer loot, behind locked doors first, Wasteland locked doors (v0.55.0): most buildings locked by loot, Door_Emergency_1_Locked + military keycard screen, flood fill entrances, v0.55.0: elite / juggernaut rifles with attachments, Decimation doors everywhere, city edge worn ground, locked wasteland, Shots v0.55: city edge, locked doors, NPC rifles

### Community 194 - "dressbalance: dressbalance.py"
Cohesion: 0.83
Nodes (3): cosine(), load(), main()

### Community 196 - "military_base: Military base status v0.42.0: three sizes built and photographed, two seed 1 COPs placed in real worldgen, mil_ test boxes retired, waiting for user review"
Cohesion: 0.67
Nodes (3): Military base status v0.42.0: three sizes built and photographed, two seed 1 COPs placed in real worldgen, mil_ test boxes retired, waiting for user review, milbase_v0.42 shots: per size aerials, top, gate, ECP lane, tower deck, TOC in and out, huts, motor pool, ASP, aid, DFAC, gym, helipad, hangar; round sheets; real world COP top down, Large walled military base, 48x48: LARGE structure (structures/large/).

## Ambiguous Edges - Review These
- `worldcheck.py` → `Bug: generated metal shelves invisible (multiblock master never set, fixed v0.12.3)`  [AMBIGUOUS]
  bug.md · relation: conceptually_related_to
- `Genuinely new model recipe (needs Techne)` → `addBox passes Y as Z origin (use addShape)`  [AMBIGUOUS]
  docs/gun_model_spec.md · relation: conceptually_related_to
- `Spawn trap: Decimation adds its spawns to biomes existing in its preInit and strips vanilla monsters in its init; our biomes are made in our preInit and copySpawns copies plains lists in postInit` → `Recurring root cause: integrated server reports side CLIENT`  [AMBIGUOUS]
  docs/terrain.md · relation: semantically_similar_to
- `FML IWorldGenerator per chunk hook` → `v0.14.0: Decimation world type (own biome map on vanilla terrain generator, flat cities on the exact city sectors, Sectors now shared, autotest runs on this type)`  [AMBIGUOUS]
  CLAUDE.md · relation: conceptually_related_to
- `Fix: SmoothSwingThread busy wait (PatchSwing 4 ms sleep)` → `Report: FPS drop while aiming scopes (fixed v0.28.0..0.28.4)`  [AMBIGUOUS]
  bug.md · relation: conceptually_related_to

## Knowledge Gaps
- **372 isolated node(s):** `Technic modpack Decimation 1.7.10 (linusrhone)`, `Prism Launcher instance mods folder`, `Subsystem taxonomy (core, proxy, network, loot, zone, ...)`, `ServerCommandRegistrar (deci.aK.o)`, `ChatHandler (deci.aK.n, radio chat)` (+367 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **140 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `worldcheck.py` and `Bug: generated metal shelves invisible (multiblock master never set, fixed v0.12.3)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Genuinely new model recipe (needs Techne)` and `addBox passes Y as Z origin (use addShape)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Spawn trap: Decimation adds its spawns to biomes existing in its preInit and strips vanilla monsters in its init; our biomes are made in our preInit and copySpawns copies plains lists in postInit` and `Recurring root cause: integrated server reports side CLIENT`?**
  _Edge tagged AMBIGUOUS (relation: semantically_similar_to) - confidence is low._
- **What is the exact relationship between `FML IWorldGenerator per chunk hook` and `v0.14.0: Decimation world type (own biome map on vanilla terrain generator, flat cities on the exact city sectors, Sectors now shared, autotest runs on this type)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Fix: SmoothSwingThread busy wait (PatchSwing 4 ms sleep)` and `Report: FPS drop while aiming scopes (fixed v0.28.0..0.28.4)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **Why does `Deci` connect `Deci: Deci` to `ZoneRules: ZoneRules`, `?: net.minecraft.world.World`, `?: cpw.mods.fml.common.eventhandler.SubscribeEvent`, `ServerChecks: ServerChecks`, `architecture: Bug tracker (bug.md)`, `Deci: .onServerTick()`, `InfectedVariants: InfectedVariants`, `loot_levels: Crate levels and NPC loot: design draft (13 Oktober 2026)`, `Deci: net.minecraft.entity.player.EntityPlayer`, `PlaceNames: PlaceNames`, `ZombieEgg: Override`, `LootPools: LootPools`, `DeciBiome: DeciBiome`, `city_engine: City engine: Lost Cities style cities from converted DeceasedCraft content`, `StatsTest: .client()`, `worldgen: Multiblock props (0.16.0): generation completes Decimation multiblocks (tile entity extends deci.W.a MultiblockPart) whole`?**
  _High betweenness centrality (0.078) - this node is a cross-community bridge._
- **Why does `StructureGenerator` connect `StructureGenerator: StructureGenerator` to `StreetDress: StreetPlan`, `?: net.minecraft.world.World`, `?: cpw.mods.fml.common.eventhandler.SubscribeEvent`, `ServerChecks: ServerChecks`, `?: DecimationWorldGen.java`, `new_feature: Current state and pending decisions`, `?: DecimationWorldGen`, `SchematicPlan: SchematicPlan`, `worldgen: World generation doc (deciworldgen)`, `PropFacing: net.minecraft.block.Block`, `worldgen: Lot grading (city yards, 0.13.0: Graded plans own the 26x26 lot, Slices grades every lot column before writing)`, `worldgen: Multiblock props (0.16.0): generation completes Decimation multiblocks (tile entity extends deci.W.a MultiblockPart) whole`, `StreetProps: Slices.java`, `building_design: v0.10.0 to v0.12.3 city blocks, large schematics, city v2 and fixes status`, `CityLayoutEvents: CityLayoutEvents`?**
  _High betweenness centrality (0.056) - this node is a cross-community bridge._
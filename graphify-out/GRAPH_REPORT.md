# Graph Report - .  (2026-10-13)

## Corpus Check
- 1 files · ~510,281 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3990 nodes · 8844 edges · 309 communities (174 shown, 135 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 708 edges (avg confidence: 0.83)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- ?: net.minecraft.item.ItemStack
- Highways: Highways
- ?: net.minecraft.world.World
- LotDress: LotDress
- Deci: Deci
- LcContent: LcContent
- ScopeZoom: ScopeZoom
- Deci: net.minecraft.item.Item
- prop_catalogue: Decimation prop catalogue (look, size and facing of every deci: block, from the prop gallery)
- StructureGenerator: StructureGenerator
- architecture: Bug tracker (bug.md)
- CLAUDE: decimation-singleplayer README (public repo overview)
- ?: DecimationWorldGen
- building_design: v0.10.0 to v0.12.3 city blocks, large schematics, city v2 and fixes status
- StreetDress: StreetDress
- CanvasPlan: CanvasPlan
- LcCity: LcCity
- Garrisons: StructureData
- ?: cpw.mods.fml.common.eventhandler.SubscribeEvent
- Surfaces: net.minecraft.block.Block
- StreetPlan: net.minecraft.block.Block
- SafezoneSites: SafezoneSites
- Deci: net.minecraft.entity.player.EntityPlayer
- architecture: Obfuscation map (package to meaning)
- CanvasPlan: CanvasPlan
- SurvivorCamp: SurvivorCamp
- BuildingDress: BuildingDress
- hwmap: hwmap.py
- make_test_schematics: make_test_schematics.py
- new_feature: Current state and pending decisions
- StreetPlan: StreetPlan
- LootBody: LootBody
- Building: Building
- BuildingPlan: BuildingPlan
- DeciBiome: DeciBiome
- StoreyPlan: StoreyPlan
- CityRefugePlan: CityRefugePlan
- EdgePlan: EdgePlan
- new_feature: Current state and pending decisions
- city_engine: City engine: Lost Cities style cities from converted DeceasedCraft content
- worldgen: World generation doc (deciworldgen)
- Wasteland: Wasteland
- SurvivorCamp: SurvivorCamp
- bug: Bug tracker (bug.md)
- military_base: Military bases design doc: US FOB style bases of our own in three sizes (COP 50x58, FOB 78x84, large FOB 112x118)
- interior_spec: Building interior and exterior spec (round 2 step 4c, approved 7 Oktober 2026)
- gunmodel: gunmodel.py
- ZombieEgg: cpw.mods.fml.relauncher.SideOnly
- Areas: Areas
- Shell: .unit()
- RiverPlan: RiverPlan
- NpcTest: .batches()
- architecture: ServerProxy (deci.a.e, dedicated only)
- FurnitureSets: FurnitureSet
- safezone: Safezone sites doc: survivor camps (references, layout, residents, placement, test), fortified city building next
- CLAUDE: Testing without the user (devtest.py first choice, servertest, worldcheck, worlddiff, autotest flags)
- MilitaryBasePlan: MilitaryBasePlan
- worldgen: Lot grading (city yards, 0.13.0: Graded plans own the 26x26 lot, Slices grades every lot column before writing)
- CameraViews: CameraViews
- Canvas: Canvas
- NoiseTest: NoiseTest
- devtest: Recently done: v0.28.0..0.28.4 cheap scope, v0.28.5 NPC tracers, v0.28.6 / 0.28.7 line of sight cache and prop render distance, v0.28.8 Deci + ZoneKind + launch wait removed, v0.28.9 dev test modes
- LotPlan: LotPlan
- ServerChecks: ServerChecks
- ZoneStore: ZoneStore
- ?: cpw.mods.fml.relauncher.SideOnly
- NoiseTest: NoiseTest
- Capture: Capture
- SurvivorCampPlan: SurvivorCampPlan
- Rails: net.minecraft.world.World
- DeciBiome: DeciBiome
- porting: Study: DeceasedCraft city buildings (DCTweaks jar Lost Cities data)
- bug: Bug: armor buff ignores NPC gunfire (fixed)
- bug: Report: FPS drop in prop dense areas (line of sight cached v0.28.6, model drawing remains)
- hk416: hk416.py
- WorldClock: WorldClock
- SKILL: decimation-gun skill revision casebook: 8 cases (boxy look, aim centre, suppressor floating, suppressor low / threads, gradation, stock in sight picture, icon halo, test traps) with symptom, cause, code location, fix, check, evidence
- LootPools: LootPools
- Deci: LocalStats
- NpcTest: net.minecraft.entity.EntityLiving
- Furnisher: Furnisher
- BuildingLayout: BuildingLayout
- Dress: Dress
- CLAUDE: Testing without the user (devtest.py first choice, servertest, worldcheck, worlddiff, autotest flags)
- worldcheck: worldcheck.py
- DevTestResults: tools/devtest.py: dev test modes in ONE game launch, results file, contact sheet per mode (first choice since 2026-10-09)
- wasteland: docs/wasteland.md: the wasteland district as the city's military area (v0.54.0 to v0.56.0)
- new_feature: NPC tiers design and result (v0.30.0): tier per armed NPC on first join (gear, gun, health, fire rate, share of player gun damage), stored in entity data, gun synced via data watcher slot 26; bandit light / medium / heavy, soldier camo sets, Soviets as enemy military
- systems_plan: Systems plan: loot, human NPCs, zombies, world planned together (13 Oktober 2026)
- Updater: Updater
- gun_style_guide: Decimation gun style guide: study of all 98 shipped guns (look and construction rules for our own guns)
- loot_levels: Crate levels and NPC loot: design draft (13 Oktober 2026)
- furniture_sets: Furniture sets doc: data driven JSON furniture groups, user editable
- city_engine: City engine: Lost Cities style cities from converted DeceasedCraft content
- lcstudy: lcstudy.py
- CLAUDE: Javassist bytecode patcher
- SKILL: City rivers and bridges v0.45.0 / v0.45.1: river contour through cities as canal chunks, street chunks carried over on decks, river cells level 0, new worlds only
- gun_style_guide: Gun aiming: aim mode draws every gun at one fixed place (x 0.5, translate -1 -0.35 0.923), sPos unused in first person; screen centre at the top of the iron sights, y -4.85 to -5.0, z -0.15
- SightPlacement: .draw()
- UpdateTest: .client()
- CityNames: CityNames
- Shell: Shell
- gun_model_spec: Our own guns pipeline (gun_model_spec section 6): tools/guns spec to .bmodel / texture / icon / .anib, bbmcp previews, NewGuns registration (newGun, newMagazine, addLootLike, useGunSounds), dev test mode gun
- CoreTest: CoreTest
- worldgen_architecture: Worldgen architecture v3 draft (layers, assets, size classes, capture tool)
- lctranslate: lctranslate.py
- ClickTest: ClickTest
- TerrainEvents: TerrainEvents
- Deci: .onServerTick()
- zones: Zones doc: Decimation's zone system end to end and what ZoneRules makes work in singleplayer
- CLAUDE: tools/build.py real javac pipeline
- DevAutoTest: DevAutoTest
- DevAutoTest: DevAutoTest
- InfectedVariants: InfectedVariants
- NpcLoadouts: Tier
- AssetDir: AssetDir
- new_feature: Round 2 step 4: building quality pass before new types (a audit, b prop catalogue, c design spec, d interiors room by room, e exterior polish, f user review in game; 4d.1 doors + low debris DONE v0.17.0, next 4d.2 surfaces)
- performance: Performance doc: tools (gunperf, census, perfcheck, JFR via jcmd), findings, checklist
- anvil118: anvil118.py
- lc2schem: lc2schem.py
- lcpack: lcpack.py
- bug: Bug: armor buff ignores NPC gunfire (fixed)
- PatchTurfNames: PatchTurfNames
- hotswap: Live editing: /deciworldgen reload + rebuild, -Photswap + tools/hotswap.py (method bodies only)
- PlaceNames: PlaceNames
- GunNoise: Override
- Protection: Protection
- UpdateScreen: UpdateScreen
- AssetDir: AssetDir
- CityRivers: CityRivers
- WastelandFence: WastelandFence
- DevTestMode: DevTestMode
- UpdateSwap: Auto update from our GitHub releases (v0.59.0): Updater check, Decimation's Update button via PatchNoUpdate v2, UpdateScreen progress, sha256 check, UpdateSwap after the game closes
- bug: Bug: armor buff ignores NPC gunfire (fixed)
- Doors: Doors
- Palettes: Palettes
- Graded: Graded
- StreetLayout: Base
- ZoneStore: ZoneStore
- FireRateTest: FireRateTest
- SealedCaves: SealedCaves
- new_feature: Zones, factions and world list with starting points: SAFEZONE zones, Decimation turf system (TurfManager, server only, likely dead in singleplayer), NYPD looks, civilian HumanEntity2, irradiated biome, military sectors, bandit tiers
- gunmodel: Part
- mapsurvey: mapsurvey.py
- lcaudit: lcaudit.py
- SKILL: Project skill decimation-military-base: research, build (Canvas, occupancy grid, greedy fit, poi), test, small detail review checklist
- NpcEgg: NpcEgg
- ?: Sectors.java
- BiomeMap: BiomeMap
- ideas: Ideas for later (user, 12 Oktober 2026)
- MenuTest: DevTestMode
- sightfit: sightfit.py
- bug: Bug: vehicles destroyed in one hit (fixed v0.8.1)
- CityEdgeTest: CityEdgeTest
- Heuristic Auto Namer
- Condition: com.google.gson.JsonObject
- DecimationBiomes: DecimationBiomes
- DecimationBiomes: DecimationBiomes
- GunPerfTest: GunPerfTest
- edgescan: edgescan.py
- edgescan: gradescan.py
- edgescan: gradescan.py
- SKILL: Project skill decimation-gun: end to end gun workflow (read docs, references, part plan, anchors, build, compare with study.py, register, gunview test, finish)
- Updater: State
- BundledContent: BundledContent
- CityPropsTest: CityPropsTest
- ShotTest: ShotTest
- TracerTest: TracerTest
- MilBaseTest: MilBaseTest
- study: .verts()
- PropsTest: PropsTest
- Interior: .cell()
- CityViewTest: CityViewTest
- GunViewTest: GunViewTest
- DeciGenLayer: DeciGenLayer
- graph_update: graph_update.py
- SKILL: Props fitted to their drawn size (v0.49.0, city/PropFit)
- zones: Place names: districts and bases through Decimation's turf banner (v0.52.0)
- HitboxTest: HitboxTest
- graph_update: graph_update.py
- ItemsTest: .lootTable()
- NewGuns: NewGuns
- perfcheck: perfcheck.py
- NpcKillTest: .server()
- CensusTest: CensusTest
- DevTestUtil: DevTestUtil
- FacingTest: FacingTest
- RefugeTest: RefugeTest
- DecimationWorldType: DecimationWorldType
- new_feature: Juggernaut v0.31.0: Soviet side, matching juggernaut set, MGs or armor piercing Barrett, 200 hp, takes 25%, speed 0.18, no knockback; only with military groups (juggernautChance 0.15) and its egg
- perfcheck: perfcheck.py
- PatchIronSights: Case 16: Mk18 Mod 1 and iron sights hidden under a sight (v0.40.0)
- safezone: City refuge: fortified building on the block nearest each city region's middle (v0.48.0)
- new_feature: Zombie variants v0.34.0: runner, riot, screamer (scream alerts infected within 32), night frenzy; InfectedVariants, ZombieEgg, config deciworldgen_zombies.cfg; dev test mode zombies
- CampCommandTest: CampCommandTest
- PlacesTest: PlacesTest
- NpcKind: NpcKind
- RefugeTest: RefugeTest
- DecimationWorldType: DecimationWorldType
- new_feature: Worldgen round 2 plan (option A: terrain blending, street life, full multiblocks, new building types, loot balance)
- dcinventory: dcinventory.py
- ideas: Wasteland locked doors (v0.55.0): most buildings locked by loot, Door_Emergency_1_Locked + military keycard screen, flood fill entrances
- fake_release_server: H
- mac10: mac10.py
- Sectors: Sectors
- dressbalance: dressbalance.py
- worlddump: worlddump.py
- worlddump: worlddump.py
- Schematic: Schematic
- facing_table: facing_table.py
- hesco_textures: hesco_textures.py
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
- ?: Dress
- ?: Load
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
- ?: Shape
- ?: Spot
- ?: StreetProps
- CLAUDE: Test trap: live checks after camp/refuge run inside a safezone

## God Nodes (most connected - your core abstractions)
1. `Deci` - 129 edges
2. `LcCity` - 54 edges
3. `MilitaryBase` - 53 edges
4. `Building` - 50 edges
5. `StreetDress` - 48 edges
6. `StreetPlan` - 47 edges
7. `NpcLoadouts` - 44 edges
8. `StructureGenerator` - 42 edges
9. `BuildingDress` - 40 edges
10. `CityRefuge` - 38 edges

## Surprising Connections (you probably didn't know these)
- `Survivor camp (about 1 in 12 buildings, one room: lantern, CanFire, bedroll, crates, radio, WaterPallet, barricaded door, note decal, graffiti outside)` --semantically_similar_to--> `survivor_camp()`  [INFERRED] [semantically similar]
  docs/interior_spec.md → tools/make_test_schematics.py
- `Spawn trap: Decimation adds its spawns to biomes existing in its preInit and strips vanilla monsters in its init; our biomes are made in our preInit and copySpawns copies plains lists in postInit` --semantically_similar_to--> `Recurring root cause: integrated server reports side CLIENT`  [AMBIGUOUS] [semantically similar]
  docs/terrain.md → CLAUDE.md
- `run/client/devtest/results.txt: one line per value, PASS / FAIL with the expectation, exit code 1 on a FAIL` --references--> `DevTestResults`  [INFERRED]
  CLAUDE.md → dev/src/main/java/net/decimation/worldgen/devtest/DevTestResults.java
- `Tracer test (-Ptracer): original 52 tracers mean 133 degrees off the target, patched 42 tracers mean 1.0, max 2.0` --references--> `TracerTest`  [INFERRED]
  bug.md → dev/src/main/java/net/decimation/worldgen/devtest/TracerTest.java
- `Dashed centre line (3 on 3 off, deci:BlockRoad_CenterLine, DeciTexturedBlock top texture by metadata % 4; meta 4 north south, 2 east west; never in or next to intersections)` --references--> `DevAutoTest`  [INFERRED]
  docs/building_design.md → dev/src/main/java/net/decimation/worldgen/DevAutoTest.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Singleplayer bugs from ServerProxy only registration or isServer gates** — claude_singleplayer_side_root_cause_pattern, bug_loot_singleplayer_crash, bug_loot_gui_never_opens, bug_no_supply_drops_singleplayer, bug_humanity_kill_singleplayer, bug_zones_inactive_singleplayer, bug_bottlecap_currency, deobf_notes_architecture_serverproxy [INFERRED 0.95]
- **Prop culling failure and fix (PropRenderer, LineOfSight corner rays, 1x1 unrotated render box, PatchPropCulling)** — bug_props_not_rendered, deobf_notes_architecture_prop_tesr_renderers, bug_lineofsight_corner_rays, bug_prop_render_bounding_box_1x1, tools_patches_patchpropculling_patchpropculling [INFERRED 0.95]
- **Multiblock master fix (v0.12.3): render gate, setSelfMaster in placers, repair on chunk load** — bug_generated_metal_shelves_invisible, bug_multiblock_master_render_gate, deobf_notes_architecture_multiblock_props, dev_src_main_java_net_decimation_worldgen_slices_slices, dev_src_main_java_net_decimation_fixes_multiblockrepairhandler_multiblockrepairhandler, claude_v0_12_3_multiblock_master_rule [EXTRACTED 1.00]
- **Supply drop landing safety: falling crate dies on non replaceable cells (flowers, props), fixed by clearLanding and skipping prop topped columns** — bug_supply_drop_flower_vanish, bug_supply_drop_street_prop_vanish, bug_falling_block_replaceable_landing, dev_src_main_java_net_decimation_fixes_supplydropscheduler_supplydropscheduler_clearlanding, dev_src_main_java_net_decimation_fixes_supplydropscheduler_supplydropscheduler_drop [EXTRACTED 1.00]
- **Upper storey reachability fix v0.16.1 (floorplan scan, unsupported ladder, collapse exemption, Building.ladder / collapsed)** — bug_upper_storeys_unreachable, bug_ladder_unsupported_wall_cell, bug_ladder_core_collapse_exemption, tools_floorplan [EXTRACTED 1.00]
- **Prop render performance: measure, cache line of sight, render distance, bake (open)** — bug_prop_fps_drop, bug_prop_fps_measurement, bug_los_cache, bug_prop_render_distance_by_size, bug_cityfps_measurement, bug_bake_props_chunk_mesh, tools_patches_patchpropculling_patchpropculling, claude_jfr_profiling, docs_roadmap_prop_drawing_cost [EXTRACTED 1.00]
- **Cheap scope v0.28.0..0.28.4 (gate, view zoom, projective glass, sniper overlay)** — feature_cheap_scope, bug_scope_pip_gate, bug_scope_camera_zoom, bug_scope_projective_glass, bug_scope_sniper_overlay, dev_src_main_java_net_decimation_fixes_scopezoom_scopezoom, tools_patches_patchscope_patchscope, dev_src_main_java_net_decimation_worldgen_devtest_scopetest_scopetest [EXTRACTED 1.00]
- **v0.12.1 hillside foundation fix (whole footprint sampling, median floor, dirt foundation under schematics)** — claude_v0_12_1_footprint_floor_sampling, docs_worldgen_floor_height_sampling, docs_worldgen_stone_brick_foundation, dev_src_main_java_net_decimation_worldgen_slices_slices, dev_src_main_java_net_decimation_worldgen_plan_plan_foundation [INFERRED 0.85]
- **Saved Javassist patch sources and the rules for applying them** — claude_three_jar_patch_rule, claude_patch_from_original_classes_rule, claude_javassist_patcher, tools_patches_patchswing, tools_patches_patchpropculling, tools_patches_patchscope, tools_patches_patchtracer, tools_patches_patchbackend [INFERRED 0.85]
- **Visual evidence memory (autotest screenshot modes, docs/shots, written shots index, prop catalogue)** — claude_visual_evidence_rule, claude_autotest_screenshot_modes, docs_shots_index_shots_index, docs_prop_catalogue_prop_catalogue_doc [INFERRED 0.85]
- **Dev test modes run in one launch by tools/devtest.py** — claude_devtest_workflow, tools_devtest, dev_src_main_java_net_decimation_worldgen_devautotest_devautotest, dev_src_main_java_net_decimation_worldgen_devtest_devtestmode_devtestmode, dev_src_main_java_net_decimation_worldgen_devtest_devtestresults_devtestresults, dev_src_main_java_net_decimation_worldgen_devtest_serverchecks_serverchecks, dev_src_main_java_net_decimation_worldgen_devtest_cameraviews_cameraviews, dev_src_main_java_net_decimation_worldgen_devtest_scopetest_scopetest, dev_src_main_java_net_decimation_worldgen_devtest_tracertest_tracertest, dev_src_main_java_net_decimation_worldgen_devtest_propstest_propstest, dev_src_main_java_net_decimation_worldgen_devtest_cityfpstest_cityfpstest [EXTRACTED 1.00]
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

## Communities (309 total, 135 thin omitted)

### Community 0 - "?: net.minecraft.item.ItemStack"
Cohesion: 0.04
Nodes (36): Bug: colourful M4A4 rail teeth under a sight (v0.41.0), Bug: NPC machine guns fire at double rate (v0.39.2), Trap: vanilla class names are obfuscated in the shipped game, never test by package name (copySpawns fixed), CLAUDE.md v0.32.0 note and the shootAt test trap, Decimation runs guns at 1.3x listed rpm (GunStats), Decimation NPCs are never hostile to creative players (isHostileTo), maxBurstRpm 600 (deciworldgen_npc.cfg npc_fire), Configuration (+28 more)

### Community 1 - "Highways: Highways"
Cohesion: 0.05
Nodes (19): CLAUDE.md v0.29.0 note: highway polish, pregen to check far away L links, CLAUDE.md v0.44.0 note: underground railway, lcpack --rails, springs denied near rails, rail dev test, server fix, Highways, Building, LcContent, Shape, PropFit, PropGeom (+11 more)

### Community 2 - "?: net.minecraft.world.World"
Cohesion: 0.07
Nodes (23): Block, Loot, ZombieTest, ZonesTest, EntityPlayerMP, Item, ItemStack, MilitarySpawner (+15 more)

### Community 3 - "LotDress: LotDress"
Cohesion: 0.06
Nodes (40): Bug: small lab read as an office; barricades at every inner door (FIXED v0.54.0), Trap: BlockChest subclasses (footlocker) re-aim themselves on placement, Skill decimation-city-decay, Skill decimation-city-interior, decimation-city case 3: wasteland weak spots (v0.54.1), decimation-city case 4: lot scenes that did not tell their story (v0.54.2), decimation-city case 5: edge, doors, locked wasteland (v0.55.0), decimation-city case 6: wasteland city apart, bunker, ramps, bridges, stations (v0.56.0) (+32 more)

### Community 4 - "Deci: Deci"
Cohesion: 0.06
Nodes (12): a, DamageSource, Deci, ServerTickEvent, Entity, EntityLivingBase, f, IInventory (+4 more)

### Community 5 - "LcContent: LcContent"
Cohesion: 0.09
Nodes (4): MilBaseTest, MilitaryBase, MilitaryBasePlan, net.decimation.worldgen.military.MilitaryBasePlan

### Community 6 - "ScopeZoom: ScopeZoom"
Cohesion: 0.06
Nodes (32): Bug: gun creeps off screen while aiming (NPC scope glass measured, fixed v0.43.1), Scope zoom lag on a weak laptop: full screen glass copy every frame while zooming or holding; v0.58.0 copies only the glass box while aiming, v0.28.1: EntityRenderer.cameraZoom zooms world and gun together; ScopeZoom draws the hand itself on RenderHandEvent (vanilla skips it while zoomed), Scope FPS drop: picture in picture second world render, fixed v0.28.0 with view zoom, Report: FPS drop while aiming scopes (fixed v0.28.0..0.28.4), PatchScope: renderScopeView gated behind system property decimation.scope.pip (old scope via pictureInPicture=true in config/deciworldgen_scope.cfg), v0.28.2 / 0.28.3: projective see-through glass (frame copied, mapped by screen position), sight learned per gun + scope + window aspect, copy limited to the glass box, v0.28.4: scopes from overlayFrom (default 4x) hide the gun and draw a black sniper overlay with the scope's reticle texture on the HUD (+24 more)

### Community 7 - "Deci: net.minecraft.item.Item"
Cohesion: 0.06
Nodes (10): Bug: every Decimation mob drawn solid black on a fresh Windows install (entity lighting, driver suspected), Case 19: reticles fixed on screen, EOTech too big, HK416 front sight (v0.41.1), Magazines, NewSights, SightPlacement, SightReticle, GunTest, net.minecraft.item.Item (+2 more)

### Community 8 - "prop_catalogue: Decimation prop catalogue (look, size and facing of every deci: block, from the prop gallery)"
Cohesion: 0.07
Nodes (45): Fix v0.16.1: solid wall cell behind the ladder, no furniture on the 4 cells around it, ladder shaft and stair core (plus 1 block ring) exempt from the collapse, Bug: upper storeys unreachable (ladder popped off, stair core collapsed; fixed v0.16.1), A floor block is also the ceiling below: keep floors light, v0.16.1: every storey reachable (ladder support, collapse spares the stairs); building audit, prop gallery, catalogue and spec (round 2 step 4a..c), Audit method: tools/floorplan.py per storey plans with reachability flood fill, plus runClient -Pautotest -Paudit (facade, ground, storey 1, roof of a sample apartment, office, shop); seed 1, Decimation world type, Building quality audit (round 2 step 4a, baseline of v0.16.0 city buildings), Exterior findings: flat facades (no balconies, canopy, shopfront glazing, shop signs) and bare roofs (no vents, water tank, antenna, stair hut, AC units), Finding: rubble is full mossy cobblestone cubes in corridors, rooms and doorways (reads as noise, cuts reachability) (+37 more)

### Community 9 - "StructureGenerator: StructureGenerator"
Cohesion: 0.08
Nodes (13): CityDistrict, Refactor check on both city paths: worlddiff 0, legacy path by moving the lc pack aside, git stash -u, cpw.mods.fml.common.IWorldGenerator, Yard, LegacyStreets, ZoneKind, StructureGenerator, Sub (+5 more)

### Community 10 - "architecture: Bug tracker (bug.md)"
Cohesion: 0.08
Nodes (5): B, BottlecapHandler (deciworldgen), BottlecapHandler, Player, net.minecraftforge.event.entity.player.EntityItemPickupEvent

### Community 11 - "CLAUDE: decimation-singleplayer README (public repo overview)"
Cohesion: 0.06
Nodes (40): tools/patches/PatchSwing.java, Fix: SmoothSwingThread busy wait (PatchSwing 4 ms sleep), Case sensitive volume extraction, CFR --caseinsensitivefs true silently drops colliding classes, CLAUDE.md project guide, dev/libs/Decimation-base.jar (patched jar minus our classes), Decimation.jar (obfuscated Forge 1.7.10 mod jar), Decimation.jar.original.bak (hash checked backup) (+32 more)

### Community 12 - "?: DecimationWorldGen"
Cohesion: 0.12
Nodes (15): Building, cpw.mods.fml.common.event.FMLInitializationEvent, cpw.mods.fml.common.event.FMLPreInitializationEvent, cpw.mods.fml.common.Mod, DecimationWorldGen, FMLPostInitializationEvent, FMLServerStartingEvent, InfectedVariants (+7 more)

### Community 13 - "building_design: v0.10.0 to v0.12.3 city blocks, large schematics, city v2 and fixes status"
Cohesion: 0.09
Nodes (38): v0.10.0 to v0.12.3 city blocks, large schematics, city v2 and fixes status, v0.12.2: car wreck long axis is x at 0 degrees (north south 5/3, east west 4/2), v0.15.0: street life (levelled street cross sections, dashed centre lines, street lights, benches, bins, trash bags, facing derived from PropRenderer transform), Prop TileEntitySpecialRenderers (deci.I.*), Props, CityDistrict, Biome overgrowth (temperate vines and moss, jungle heavy vines, snowy snow layers, dry sand drifts and dead bushes), BlockProp facing transform from deobf PropRenderer (rotate 180 about x, metadata % 4 * 90 about y, then extra rotation; toward the road: road west 4, east 2, north 5, south 3) (+30 more)

### Community 14 - "StreetDress: StreetDress"
Cohesion: 0.15
Nodes (4): BuildingPlan, StreetDress, StreetLayout, StreetPlan

### Community 15 - "CanvasPlan: CanvasPlan"
Cohesion: 0.06
Nodes (13): Plan, Schematic, SchematicPlan, ZoneKind, BORDER, MILITARY, POLICE, RADIATION (+5 more)

### Community 16 - "LcCity: LcCity"
Cohesion: 0.12
Nodes (12): BuildingPlan, v0.25.0 highways, v0.26.0 parks / street scenes / fronts, v0.27.0 district Lost Cities street parts, Building, StreetProps, LcCity, City decor: parks on open lots, street scenes (pack fountains), building fronts on the street side, Fronts: building chunk beside a straight street gets a district front part (FRONT_CHANCE 0.5), resolved lazily at write time, Parks: open lots LOT_CHANCE 10% of building chunks carry a district park part one layer up (+4 more)

### Community 17 - "Garrisons: StructureData"
Cohesion: 0.11
Nodes (8): v0.12.1: whole footprint floor height sampling and dirt fill under schematics, Garrisons, Load, Plan, Slices, StructureData, Floor height sampling (5x5 grid over the whole footprint where chunks exist plus 9 soilTop points in the window, median, maxSpread buildings 12 / schematics 7, stored in StructureData), net.minecraft.world.WorldSavedData

### Community 18 - "?: cpw.mods.fml.common.eventhandler.SubscribeEvent"
Cohesion: 0.08
Nodes (14): cpw.mods.fml.common.eventhandler.SubscribeEvent, ServerTickEvent, ZoneRules, EntityPlayer, net.decimation.mod.server.zones.ObjectZoneList, net.minecraft.client.gui.GuiButton, net.minecraft.client.gui.GuiScreen, net.minecraftforge.common.config.Configuration (+6 more)

### Community 19 - "Surfaces: net.minecraft.block.Block"
Cohesion: 0.10
Nodes (7): Surfaces, PropFacing, Row, PropFacingAccess, B, PropBoxes, net.minecraft.block.Block

### Community 20 - "StreetPlan: net.minecraft.block.Block"
Cohesion: 0.07
Nodes (7): Shape, Shape, StreetProps, ZoneKind, StreetPlan, City engine files after the 2026-10-09 split: LcCity layout + StreetPlan, BuildingPlan, LotPlan, EdgePlan, StreetProps, Props of turned street parts turned in partAt (PropBoxes.turn)

### Community 21 - "SafezoneSites: SafezoneSites"
Cohesion: 0.09
Nodes (30): all_names(), attach_offset(), Gun, label(), load(), main(), metrics(), num() (+22 more)

### Community 22 - "Deci: net.minecraft.entity.player.EntityPlayer"
Cohesion: 0.08
Nodes (12): Bug: enemy group members spawned into walls; none in superblock districts (FIXED v0.54.1), Test arena (DevTestArena): flat stone floor at y 150 around (8, 8), every NPC test mode builds and cleans it first; retry Decimation-refused spawns, MilitarySpawner, DevTestArena, arena_v0.34.0: all NPC test lineups and shots on the test arena, Shared core: group spawner (MilitarySpawner logic for patrols, raids, reinforcements, horde waves), Wasteland system: district zone, military building pick, loot swap, supplies, stories, entries, vehicles, fighting positions, lots, enemy military, EntityLiving (+4 more)

### Community 23 - "architecture: Obfuscation map (package to meaning)"
Cohesion: 0.10
Nodes (34): Bug: ClassCastException deci.a.c to deci.a.e, Bug: loot never worked in singleplayer, Obfuscation map (package to meaning), 8 agent deobfuscation naming pass, Subsystem taxonomy (core, proxy, network, loot, zone, ...), Decimation architecture notes, ClientProxy (deci.a.c), DeciConstants (deci.Q.c, GUI ids) (+26 more)

### Community 26 - "BuildingDress: BuildingDress"
Cohesion: 0.22
Nodes (3): BuildingDress, Item, Spot

### Community 27 - "hwmap: hwmap.py"
Cohesion: 0.14
Nodes (21): RailTest, Dev test rail: fresh station, 48 rails, no liquid, street to platform walk, shots of every end, lc_rail_v0.44.0 shots: entrance plaza, lit platform, stairs, shaft, tunnel, four line ends, crossing, at(), city(), clear(), hl(), line() (+13 more)

### Community 28 - "make_test_schematics: make_test_schematics.py"
Cohesion: 0.16
Nodes (26): mil_compound large test schematic (48x14x48), city_office(), city_shop(), city_street(), civ_gas_station(), civ_house_ruin(), civ_shed(), decay() (+18 more)

### Community 29 - "new_feature: Current state and pending decisions"
Cohesion: 0.09
Nodes (24): Knowledge index (docs/, interior spec, deobf notes, names.tsv, trackers), Current state and pending decisions, v0.14.0: Decimation world type (own biome map on vanilla terrain generator, flat cities on the exact city sectors, Sectors now shared, autotest runs on this type), Rotation, Sectors, Decimation world type (level-type=decimation: flat rolling land, rivers and lakes, no ocean, one temperate climate), Lake rules (none in city / military, 1 in 4 in other dead biomes, vanilla rate in overgrown, no surface lava pools), Terrain not done yet: fog colour comes from the world provider [not verified whether needed], no snow by design (temperature 0.7), fixed ids may clash (+16 more)

### Community 30 - "StreetPlan: StreetPlan"
Cohesion: 0.12
Nodes (22): Bug: generated metal shelves invisible (multiblock master never set, fixed v0.12.3), MetalShelfRenderer draws only the master part (returns unless isMaster: stored master position equals own position), setSelfMaster() on every placed multiblock part plus repair on chunk load, v0.12.3 rule: multiblock props (deci.W.a, metal shelves) render only from a master part; anything placing them outside player placement must call setSelfMaster(), v0.16.0: multiblock props generated whole (shelves are 1x1x2 TALL), tools/multiscan.py checks them, supply drops skip columns topped by a prop, Multiblock props (deci.W.*), Load, World (+14 more)

### Community 31 - "LootBody: LootBody"
Cohesion: 0.12
Nodes (8): After 0.56.0 (unreleased): fixes/Loot.open, fixes/LootBody (deciworldgen:loot_body), dev test loot, keepScreen trap, World, LootBody, InventoryBasic, LootTest, Loot spikes DONE 13 Oktober 2026 (dev test loot, 12 PASS): our inventory on Decimation's loot screen, taken stacks leave the same InventoryBasic, LootBody opens it on right click, goes when emptied, NBT save, Master plan step 0 DONE: both loot spikes work; next step 1 core, loot_spike_v0.56 shots: loot screen with our inventory, LootBody white box, body loot screen

### Community 32 - "Building: Building"
Cohesion: 0.09
Nodes (9): Building, Furnisher, Interior, net.decimation.worldgen.Graded, Ruins, Shell, StoreyPlan, Surfaces (+1 more)

### Community 33 - "BuildingPlan: BuildingPlan"
Cohesion: 0.09
Nodes (7): Building, Shape, BuildingPlan, Building, Shape, ZoneKind, Styled

### Community 34 - "DeciBiome: DeciBiome"
Cohesion: 0.11
Nodes (28): DamageSource split: gunDeci (player) vs human/turret (NPC), NPC tracers flew along the body facing; fixed by sending the target id (PatchTracer), Cause: PacketGunFireEffects carried only the shooter id; the client drew the tracer along getLook() (a mob's body facing), Tracer test (-Ptracer): original 52 tracers mean 133 degrees off the target, patched 42 tracers mean 1.0, max 2.0, Two gunshot DamageSource identities (gunDeci player, human NPC), NPC ranged attacks call attackEntityFrom directly server side, Knowledge index entry: docs/roadmap.md holds EVERYTHING planned in one list, BankerTrader (deci.ai.e) (+20 more)

### Community 35 - "StoreyPlan: StoreyPlan"
Cohesion: 0.14
Nodes (4): ApartmentPlanner, OfficePlanner, ShopPlanner, StoreyPlan

### Community 36 - "CityRefugePlan: CityRefugePlan"
Cohesion: 0.09
Nodes (3): CityRefugePlan, Residents, net.decimation.worldgen.FixedBase

### Community 37 - "EdgePlan: EdgePlan"
Cohesion: 0.10
Nodes (3): EdgePlan, ZoneKind, Graded

### Community 38 - "new_feature: Current state and pending decisions"
Cohesion: 0.13
Nodes (8): ServerTickEvent, Post, SafezoneSites, Site, SiteList, Load, Save, Unload

### Community 39 - "city_engine: City engine: Lost Cities style cities from converted DeceasedCraft content"
Cohesion: 0.15
Nodes (25): apply(), bmodel(), cells(), evaluate(), gl_calls(), gl_ops(), java_model(), main() (+17 more)

### Community 40 - "worldgen: World generation doc (deciworldgen)"
Cohesion: 0.12
Nodes (22): Ladder at (W-2, L-2) hangs on a back wall cell that can be a window, a decay hole or not yet written (next population window); a block update pops it off, Bug: city buildings missing a whole wall at sector borders (fixed v0.11.1), DevPregen, ServerTickEvent, Schematic, 1 block margin ring outside walls for exterior vines (SKIP elsewhere), Adding community schematics (prefix, folder, full restart, new chunks only), Cell grid (4x4 chunks, one small schematic or one city block) (+14 more)

### Community 41 - "Wasteland: Wasteland"
Cohesion: 0.17
Nodes (3): Building, Wasteland, WastelandTest

### Community 43 - "bug: Bug tracker (bug.md)"
Cohesion: 0.12
Nodes (26): Bug: large ammo crate NPE (dead field avk), Bug: bottlecaps not converted to currency (fixed), Bug tracker (bug.md), Bug: CustomSkinLoader coremod crash, EntityFallingSupplyDrop turns into a block only on a replaceable cell (flowers, saplings, tall flowers are not), Bug: graded yard sand fell into caves, hole next to a building (fixed v0.13.0), Bug: humanity never changed from ordinary kills in singleplayer (fixed v0.9.0), Bug: loot GUI never opens (+18 more)

### Community 44 - "military_base: Military bases design doc: US FOB style bases of our own in three sizes (COP 50x58, FOB 78x84, large FOB 112x118)"
Cohesion: 0.08
Nodes (26): CLAUDE.md v0.42.0 note: military bases, worldgen/military, milbase dev test, mil_ schematics no longer placed, CLAUDE.md v0.42.3 note, Military bases accepted by the user on v0.42.4 (11 Oktober 2026), Military bases design doc: US FOB style bases of our own in three sizes (COP 50x58, FOB 78x84, large FOB 112x118), Real element to Decimation block mapping (HESCO = BlockMilitaryBarrier, T-wall = BlockBarrierTall, jersey = BlockBarrier, sandbags, wire, crates, radios, wrecks; tents smooth sandstone), Real FOB reference (sources): concertina, HESCO MIL7 walls, T-walls, guard towers, ECP with serpentine and search area, TOC, B-hut LSA, DFAC, aid station, ASP, fuel point, motor pool, helipad, burn pit, duck and cover bunkers, Military base status v0.42.0: three sizes built and photographed, two seed 1 COPs placed in real worldgen, mil_ test boxes retired, waiting for user review, Military base test: devtest --live milbase (sizes, turns, seed, points; points=none lists refused modules), servertest seed 1 pregen=-64,448,9 (+18 more)

### Community 45 - "interior_spec: Building interior and exterior spec (round 2 step 4c, approved 7 Oktober 2026)"
Cohesion: 0.14
Nodes (26): v0.17.0: step 4d.1, doors in every DOOR cell and low debris (docs/interior_spec.md section 8), Finding: rooms have no function (sparse apartment units, empty ground storey units, identical office desk grid on every storey incl. ground, repeated plans on tall buildings, undefined upper shop storey), Apartment slab layout (double loaded corridor, stair core, living part and bedroom per unit), Procedural building design doc (city blocks, Building v2), Minecraft scale (storey 4 blocks, corridor 2 wide, doors 1x2, units 5 to 8 deep), Office floor layout (core, open plan desk rows at windows, meeting rooms, reception, storage, break room), Layout research sources (Auckland Design Manual, archgyan, Pult, Shopify, Small Business Trends), Shop grid layout (parallel aisles, decompression zone, checkout front left, stockroom behind) (+18 more)

### Community 46 - "gunmodel: gunmodel.py"
Cohesion: 0.13
Nodes (5): World, NpcLoadouts, EntityConstructing, LivingAttackEvent, LivingUpdateEvent

### Community 47 - "ZombieEgg: cpw.mods.fml.relauncher.SideOnly"
Cohesion: 0.13
Nodes (9): cpw.mods.fml.relauncher.SideOnly, Magazine, ItemStack, SuppressWarnings, ZombieEgg, BlockHesco, IIconRegister, net.minecraft.client.renderer.texture.IIconRegister (+1 more)

### Community 48 - "Areas: Areas"
Cohesion: 0.14
Nodes (12): Area, Areas, Load, Save, Unload, ZoneKind, Kind, BASE_CLAIM (+4 more)

### Community 50 - "RiverPlan: RiverPlan"
Cohesion: 0.10
Nodes (5): ZoneKind, RiverPlan, City v0.56.0: asphalt ramp for street stairs, natural bridge damage, metro entrance in the sidewalk, v0.56.0: wasteland city apart, fence, bunker garrison; ramps, bridges, stations, Shots v0.56: wasteland city topdown, fence, gate, bunker; ramp, rail, bridge sheets

### Community 51 - "NpcTest: .batches()"
Cohesion: 0.16
Nodes (22): anib(), bmodel(), build(), fnum(), icon(), layout(), numbered(), paint() (+14 more)

### Community 52 - "architecture: ServerProxy (deci.a.e, dedicated only)"
Cohesion: 0.12
Nodes (24): Bug: zones never active in singleplayer (partial fix), Block break/place protection handlers (deci.aK.a, b), BlockRegistry (deci.aD.c / g), ChatHandler (deci.aK.n, radio chat), ClanManagerV1 (server.clans.a), EntitySpawnZoneHandler (deci.aK.d), LootCooldownRegistry (deci.aB.e), PlayerZoneTickHandler (deci.aK.m) (+16 more)

### Community 53 - "FurnitureSets: FurnitureSet"
Cohesion: 0.14
Nodes (6): com.google.gson.JsonArray, com.google.gson.JsonObject, Condition, FurnitureSet, FurnitureSets, Set conditions: when kinds / storey / floors

### Community 54 - "safezone: Safezone sites doc: survivor camps (references, layout, residents, placement, test), fortified city building next"
Cohesion: 0.09
Nodes (23): Bug: camp guard flying over the wall (static guard spawned before its floor); keeper waits for populated chunks and puts residents back, fixed v0.47.2, Bug: survivor camp NPCs missing (test camp emptied by the test; Decimation's trader reset is dedicated server only), fixed v0.47.1, Military base skill case 7: survivor camp first build (clashes, outhouse head room, fence gates in walkcheck), Military base skill case 8: the npc is missing (test cleanup, trader reset, awning front colour), Military base skill case 9: flying guard, retaliation removed, entry sign, cosmetic props, CLAUDE.md v0.47.0: survivor camps, CanvasPlan shared, SafezoneSites, dev test camp, CLAUDE.md v0.47.1: camp people kept, dedicated trader reset respawn, camp command, CLAUDE.md v0.47.2: no provocation, keeper populated check and put back, sign, props (+15 more)

### Community 55 - "CLAUDE: Testing without the user (devtest.py first choice, servertest, worldcheck, worlddiff, autotest flags)"
Cohesion: 0.12
Nodes (18): Dedicated server crashed at start since v0.42.2: Deci.homeMenu loaded GuiScreen (fixed v0.44.0), Autotest screenshot modes (default 3 street views, -Paudit building audit, -Pgallery every Decimation block 3 per shot, -Ponly= re-shoots single views; peaceful, mobs removed, camera locked per tick, fov / gamma restored), Autotest ends with 3 city street screenshots (dev/run/client/screenshots/autotest_<n>.png: along the street, street light side on, across); read them to check visuals instead of asking the user; -Ddeciworldgen.autotest.views=false skips them, run/client/devtest/results.txt: one line per value, PASS / FAIL with the expectation, exit code 1 on a FAIL, tools/devtest.py: dev test modes in ONE game launch, results file, contact sheet per mode (first choice since 2026-10-09), Autotest forces pauseOnLostFocus false, Damage checks must run after 60 server ticks (spawn invulnerability), Testing without the user (devtest.py first choice, servertest, worldcheck, worlddiff, autotest flags) (+10 more)

### Community 56 - "MilitaryBasePlan: MilitaryBasePlan"
Cohesion: 0.11
Nodes (5): Canvas, CanvasPlan, MilitaryBasePlan, MilitaryBase, net.decimation.worldgen.ZoneKind

### Community 57 - "worldgen: Lot grading (city yards, 0.13.0: Graded plans own the 26x26 lot, Slices grades every lot column before writing)"
Cohesion: 0.19
Nodes (22): .bmodel is plain text Techne style code (earlier binary note was wrong), addChild is vanilla direction: parent.addChild(child) (old note said reversed; corrected 2026-10-10), Gun specific .bmodel header fields (mOff, sPos, flamePos, lhPos, rhPos, ejectPos, Scale), Unmapped .f(n) builder call (likely spread or sway), Fire mode enum deci.ay.e.a (SINGLE, AUTO, BURST, PUMP, BOLT), Gun registration call new i(...).f().am(), Genuinely new model recipe (needs Techne), Reskin an existing weapon recipe (fast path) (+14 more)

### Community 60 - "NoiseTest: NoiseTest"
Cohesion: 0.10
Nodes (21): Open bug: HUD and menu kill / death counters read the dead backend profile cache; Player Kills should become Human Kills (NPC humans and players) from local data, Open bug: with a real account the main menu shows an offline banner and grey Play offline (ClientState.bv off, dead backend) instead of Play on green, Fixed v0.42.2: local Human Kills / Infected Kills / Deaths (LocalStats) on HUD and menu, labels by PatchMenuStats, session flag valid (green Play), Open bug: military jeep / tank wreck blocks have hardness 0 and break with one punch (reported 10 Oktober 2026), Fixed v0.42.5: military wrecks hardness 10 (WreckHardness), 50 s by hand, CLAUDE.md v0.42.2 note and PatchMenuStats in the patch list, CLAUDE.md v0.43.0 note: gunshot noise, Roadmap: gunshot noise draws zombies and NPCs (unsuppressed shots), later (+13 more)

### Community 61 - "devtest: Recently done: v0.28.0..0.28.4 cheap scope, v0.28.5 NPC tracers, v0.28.6 / 0.28.7 line of sight cache and prop render distance, v0.28.8 Deci + ZoneKind + launch wait removed, v0.28.9 dev test modes"
Cohesion: 0.13
Nodes (18): Launch waited 5 s for the dead Decimation backend (kryonet hardcoded 5000 ms); PatchBackend, Obfuscated Decimation names in our code go through fixes/Deci, Rule: new code adds an accessor to Deci, never calls deci.* directly (all ~60 obfuscated uses migrated 2026-10-09, seed 1 world 0 blocks differ), Worldgen uses ZoneKind instead of Decimation's obfuscated zone enum; Deci.zoneType converts, AntiCheatScanner (deci.aN.a), BackendConnection (deci.aP.a, kryonet), DeathStatsHandler (deci.aK.h), PlayerLoginHandler (deci.aK.l) (+10 more)

### Community 62 - "LotPlan: LotPlan"
Cohesion: 0.11
Nodes (4): Shape, ZoneKind, LotPlan, FixedBase

### Community 64 - "ZoneStore: ZoneStore"
Cohesion: 0.16
Nodes (8): Load, ObjectZone, ObjectZoneList, Save, ServerTickEvent, Unload, ZoneKind, ZoneStore

### Community 65 - "?: cpw.mods.fml.relauncher.SideOnly"
Cohesion: 0.14
Nodes (20): add(), acog(), eotech(), icon(), oct_y(), oct_z(), Gun space -> attachment file space (minus renderAttachments' translate)., 68 MOA ring, centre dot, four ticks across the ring (photo 62), red. Drawn… (+12 more)

### Community 66 - "NoiseTest: NoiseTest"
Cohesion: 0.19
Nodes (10): Bug: NPCs kept a dead entityToAttack and ignored gunshots (fixed v0.43.2), Bug: enemy NPCs (juggernaut) ignored gunshots, living stale entityToAttack (fixed v0.43.3), v0.43.2 stale target fix, aimdrift changes, test sweep, v0.43.3 GunNoise reads revenge target, noise test tier / far / hits, Decimation NPCs fight getAITarget (revenge, expires); entityToAttack never cleared, NoiseTest, v0.43.2 NPCs react to gunshots again, v0.43.3 enemy NPCs react to gunshots with an old target remembered (+2 more)

### Community 67 - "Capture: Capture"
Cohesion: 0.21
Nodes (3): Capture, JsonArray, JsonObject

### Community 68 - "SurvivorCampPlan: SurvivorCampPlan"
Cohesion: 0.13
Nodes (5): Post, Canvas, SurvivorCampPlan, net.decimation.worldgen.Residents, Post

### Community 70 - "DeciBiome: DeciBiome"
Cohesion: 0.14
Nodes (5): DeadTree, Override, DeciBiome, Override, net.minecraft.world.gen.feature.WorldGenAbstractTree

### Community 71 - "porting: Study: DeceasedCraft city buildings (DCTweaks jar Lost Cities data)"
Cohesion: 0.11
Nodes (19): DeceasedCraft content catalogue: 79 Lost Cities buildings, city parts, apocalypsenow structures, disabled vanilla structures, Lost Cities to 1.7.10 conversion (lc2schem, lctranslate, paste command), DeceasedCraft 79 building types and 5 district city styles, Study: DeceasedCraft city buildings (DCTweaks jar Lost Cities data), DeceasedCraft storey is 6 high (4 air) vs our 5, Porting Decimation to modern Minecraft: three paths (parked plan), First spike: convert the MAC-10 to the chosen format and compare with our render; recommendation path B first, Model conversion table: .bmodel to OBJ / Blockbench, .anib keyframes, props to JSON block models, facing to block states (+11 more)

### Community 72 - "bug: Bug: armor buff ignores NPC gunfire (fixed)"
Cohesion: 0.14
Nodes (15): Bug: armor buff ignores NPC gunfire (fixed), Helmets give no gun protection (slot 3 excluded), Helmet counts on headshots only (v0.9.1: aim line for player guns, 20% random for NPC), Proposed LivingHurtEvent gunshot damage unification, CLAUDE.md v0.31.0: juggernaut tier, Barrett armor piercing, DeciDamageSources (deci.aD.h), PacketGunHit handler (deci.aE.a$z$a), ArmorGunfireHandler (+7 more)

### Community 73 - "bug: Report: FPS drop in prop dense areas (line of sight cached v0.28.6, model drawing remains)"
Cohesion: 0.17
Nodes (16): Baking static props into chunk meshes (prop textures into the block atlas, both model formats to quads): full fix for open views, not started, Bug: building base height depends on chunk generation order (Slices.decideBase), LineOfSight.canSeeTileEntity (deci.a.c$a.a): 8 rays from the eye to the render box corners, PatchPropCulling step 3 (v0.28.6): line of sight answers cached until the player or entity moves 0.3 blocks (props 1.0..1.3 s, entities 0.15..0.18 s); prop renderer share 21% -> 9%, Prop dense FPS drop: line of sight ray casts 76% of prop rendering, cached in v0.28.6; model drawing remains, Report: FPS drop in prop dense areas (line of sight cached v0.28.6, model drawing remains), Prop fps measured (-Pprops): 225 props in view 12..14 fps vs empty 28..29; 76% of PropRenderer time in canSeeTileEntity ray casts, TileEntityProp.getRenderBoundingBox: bare 1x1x1 cell for 36 of 72 props, never rotated (+8 more)

### Community 74 - "hk416: hk416.py"
Cohesion: 0.12
Nodes (18): CLAUDE.md v0.42.1 note and prop_placement.md index entry, Military base v0.42.1: deciworldgen:hesco, locked TOC door with keycard, gravel ground, sandbag ASP, props from drawn boxes, 0 clashes; tile entity props 129/381/571, Performance 6: props used as building material (BlockMilitaryBarrier HESCO, a tile entity per cell) replaced by deciworldgen:hesco; tile entity props per base 1020/2007/2902 -> 129/381/571, BlockMilitaryBarrier is a prop (tile entity per cell), not a plain block: never as bulk building material, Vehicle wreck props (BlockWreckage1..5 cars, BlockTruckWreckage1..6, police cars, military jeep / APC / helicopter, mech; cars long along z at meta 3), Prop checks: Canvas.validateProps removes and logs clashes; tools/props/propclash.py on block dumps (WALL PROP DOOR FACE MOUNT FLOAT), Prop placement doc: how Decimation props are drawn (PropRenderer and ~75 own renderers, per renderer metadata tables, model offsets), facing, drawn sizes, rules, checks, Prop facing: placer rule front 2 E 3 S 4 W 5 N (checked props); exceptions big military radio (90 clockwise), military wrecks (long along z at 2/3), wall mounted keycard / wall flag / junction box (+10 more)

### Community 75 - "WorldClock: WorldClock"
Cohesion: 0.12
Nodes (11): v0.57.0: loot spikes, shared core (WorldClock, LootPools, Protection, Areas), dev tests core / items, PatchLootRolls, Event, DAWN, DAY, DUSK, NIGHT, ServerTickEvent, Unload (+3 more)

### Community 76 - "SKILL: decimation-gun skill revision casebook: 8 cases (boxy look, aim centre, suppressor floating, suppressor low / threads, gradation, stock in sight picture, icon halo, test traps) with symptom, cause, code location, fix, check, evidence"
Cohesion: 0.14
Nodes (18): Skill casebook case 10: UMP9 as a variant of the UMP45 (how, magazine anchor, registration, checks), Skill case 12: UMP9 accepted without revision (variant route), Skill case 13: variants with many replaced parts (drop by position, fit to the kept receiver, colour twins by recolouring), Case 15: MAC-10 parts not seated, contact check cleanup (v0.39.3), Case 20: verdict, sights accepted (v0.41.1), Lesson 15: every part sits on its host, sights like the real gun from behind, Lesson 17: iron sights in the defaultScopeModel group, Lesson 18: shorter variant scaled from the base gun (+10 more)

### Community 77 - "LootPools: LootPools"
Cohesion: 0.23
Nodes (6): Entry, FileData, ItemStack, LootPools, Pool, net.minecraft.inventory.IInventory

### Community 78 - "Deci: LocalStats"
Cohesion: 0.18
Nodes (3): ClientTickEvent, LocalStats, StatsTest

### Community 80 - "Furnisher: Furnisher"
Cohesion: 0.22
Nodes (4): Facing, Furnisher, Entry, net.decimation.worldgen.sets.FurnitureSet

### Community 83 - "CLAUDE: Testing without the user (devtest.py first choice, servertest, worldcheck, worlddiff, autotest flags)"
Cohesion: 0.20
Nodes (6): Plan, Schematic, Sub, LargeSites, Base layout: perimeter rings, ECP south, HESCO wall with towers, ring and main road, TOC north, LSA west, logistics east, greedy placer for the rest, light decay, StructureGenerator

### Community 84 - "worldcheck: worldcheck.py"
Cohesion: 0.16
Nodes (11): Bug: structures built on ocean floor, Underwater placement fix (v0.7.0), tools/worldcheck.py region file inspection, Top down picture of a generated world from its region files (no game): the top…, main(), _meta(), Block metadata (0..15) at a position, None if the chunk is missing., Block name -> numeric id, from the FML id map in level.dat. (+3 more)

### Community 85 - "DevTestResults: tools/devtest.py: dev test modes in ONE game launch, results file, contact sheet per mode (first choice since 2026-10-09)"
Cohesion: 0.21
Nodes (5): Scope fps measured (4x on ak74): picture in picture aiming 37..44 vs view zoom aiming 105..132, New dev test: DevTestMode subclass, register its name in DevAutoTest.mode(), record with DevTestResults.value / check, screenshots with DevTestUtil.screenshot, DevTestResults, EntityPlayerMP, ScopeTest

### Community 86 - "wasteland: docs/wasteland.md: the wasteland district as the city's military area (v0.54.0 to v0.56.0)"
Cohesion: 0.13
Nodes (17): v0.57.1 line: whole sector wasteland city, tools/topdown_blocks.py, Case 7: the wasteland ring was absurd (v0.56.0 -> v0.57.1), Lesson 17: split out / own city means made of its own kind, not cut away by empty land; streets stay joined, Lesson 18: user's sector = our district, user's district / city = our city sector, big chunk = cell, Dev test mode wasteland (WastelandTest), City dressing: the wasteland as the military area (v0.54.0), Idea 18: military airport in a wasteland city, half a sector 128 x 256, runway about 220 x 15, apron, hangars, tower, HQ, barracks; planes need models of our own, v0.57.1 done item and next ideas airport, city sizes (+9 more)

### Community 87 - "new_feature: NPC tiers design and result (v0.30.0): tier per armed NPC on first join (gear, gun, health, fire rate, share of player gun damage), stored in entity data, gun synced via data watcher slot 26; bandit light / medium / heavy, soldier camo sets, Soviets as enemy military"
Cohesion: 0.12
Nodes (17): CLAUDE.md v0.30.1 note: spawn egg per NPC tier, CLAUDE.md v0.30.4: cooldown ticks, rocket tiers, config version 2, new tiers last, CLAUDE.md v0.30.0 note: NPC tiers, gun sync via data watcher slot 26, test mode npc, Balance v0.30.4: hit cooldown 0.25 s, RPG NPCs, Balance: NPC tiers (v0.30.0), config deciworldgen_npc.cfg, Recently done v0.30.4: hit cooldown 0.25 s, RPG NPCs, Recently done v0.30.0: NPC tiers, stronger bandits, enemy military spawner, sheet_npc_eggs_v0.30.1: lineups plus the 8 tier eggs in the hotbar (+9 more)

### Community 88 - "systems_plan: Systems plan: loot, human NPCs, zombies, world planned together (13 Oktober 2026)"
Cohesion: 0.12
Nodes (16): NPC lootable body: player kills only, gun / mags / worn armor / extras / caps per tier, gone after 5 min or emptied, Master plan step 1 DONE v0.57.0; next step 2 loot, Shared core: body entity with an inventory (NPC bodies, turned player gear), Claims: personal base (clear, plant flag) and city place claims (10 % of districts, clear all hostiles, caps revenue, decay back to nature when away), Systems plan: loot, human NPCs, zombies, world planned together (13 Oktober 2026), What exists today: infection one boolean (8 % bite), all Decimation armor CHAIN, demons 100 hp capped, radiation zones unused, turf income 20 caps per 15 min, Horde nights every 10th night to day 50 then every 5th: no sleep, every infected targets the player, waves, demons, Infection in 5 stages ending in death and rising as an infected in your armor look; antibiotics stages 1 to 2, cure up to 4 (+8 more)

### Community 89 - "Updater: Updater"
Cohesion: 0.15
Nodes (3): SuppressWarnings, Updater, java.net.HttpURLConnection

### Community 90 - "gun_style_guide: Decimation gun style guide: study of all 98 shipped guns (look and construction rules for our own guns)"
Cohesion: 0.13
Nodes (17): study.py gaps: ours vs Decimation guns of the category, metric by metric (median, q10..q90, outside marks), incl. part sizes relative to length, Gun animation templates: Fire 2 frames slide only, Reload1 57 frames keyframes every 5 (SWITCH 20, LOAD 40, TRYBOLT 50), Rack 19 frames Hand 1, SlideBack static in 92 of 98, Construction rules: small shape boxes, layered receiver panels, grooves as thin raised plates, octagons from 3 parts per slice, skewed segments for grips and curved mags (addChild), chamfered edges, thin features thin, docs/references/decimation_guns.tsv: per gun parts, shaped %, rotated %, length / height / width, texture size, Where gun detail goes: receiver 30 to 70% of length holds half the parts, 3 of 4 parts in the top two fifths; first person shows top, right side, rear, First person gun look (dev test gunview): seen from rear, top, right; short guns are their rear section; MAC-10 stock plate fills the view, Gun icon style: 32x32 side silhouette, muzzle right, dark tones, 1 px black outline; make from the model side render, Gun numbers: 99% shape boxes, 72% declared 1x1x1, real part median 0.2 x 0.4 x 0.9, offsets median 0.35, UV step 8, texture brightness median 49; smg ~105 parts, rifle ~174, pistol ~83; about 31 mm per unit [inferred] (+9 more)

### Community 91 - "loot_levels: Crate levels and NPC loot: design draft (13 Oktober 2026)"
Cohesion: 0.13
Nodes (16): Decimation's crates gave one item fewer than listed, stacks one short (IntRange half open); FIXED v0.57.0 by PatchLootRolls, PatchLootRolls.java: IntRange.pick (deci/aB/a) inclusive, three jars, User choices: every kind split by content type in 4 levels (military L1 magazines, L2 attachments, L3 guns, L4 all), colour band per level, L4 only in the rarest places, lootable NPC body, Four crate blocks (military, police, medical, civilian), metadata level (0..3) x 4 + facing: 16 crates, baked block render, no tile entity, Decimation loot today: 33 blocks, 12 hardcoded lists, 1 .. n-1 items per open (n 2 gives 1), rarity per item lootChance, 900 s refill, Crate levels and NPC loot: design draft (13 Oktober 2026), Duplication guard: Loot.closeFor closes loot screens on a body's inventory when it dies or the player is over 8 blocks away (Decimation's container never checks its source), Section 3.2 lists BUILT as LootPools built-in lists in config/deciworldgen_loot.json; magazines read from each gun; names in docs/references/decimation_items.tsv (+8 more)

### Community 92 - "furniture_sets: Furniture sets doc: data driven JSON furniture groups, user editable"
Cohesion: 0.12
Nodes (16): v0.20.0 furniture sets, wall lining, corner doors, one sided corridors, live loop, DeceasedCraft interiors fully authored per storey, no procedural rooms, Furniture sets doc: data driven JSON furniture groups, user editable, In-game capture: pos1/pos2, capture set/part, Set format: layers (floor, +1, under ceiling), row 0 against the wall, palette with face/type, rooms slot, weight, known.txt: unedited old built-in copies are updated (tools/asset_hashes.py), Named palettes and weighted styles for sets (base / style keys), Placement: seeded weighted order, every wall and offset, free cells off walkway, back against wall, no full height piece over a window (+8 more)

### Community 93 - "city_engine: City engine: Lost Cities style cities from converted DeceasedCraft content"
Cohesion: 0.17
Nodes (14): FixedBase, City engine: Lost Cities style cities from converted DeceasedCraft content, Skipped giant buildings: casino 276 high, oasis condo top above 250, laboratory 90 deep cellars, Rules taken from Lost Cities source: street surface at G, ground floor at G, cellars below, stairs at G + 1 toward the higher neighbour, City levels per cell (6 blocks apart), streets at G, stairs parts between levels, City engine open items: railway (v0.44.0) and rivers with bridges (v0.45.0) done; giant buildings still skipped, City street dressing: sidewalks, centre lines, lamps, benches, wrecks, City superblocks: 2x2 cells, 7x7 building chunks, landmark towers (+6 more)

### Community 94 - "lcstudy: lcstudy.py"
Cohesion: 0.23
Nodes (11): category(), districts(), main(), building name -> {city style: weight share}, structure_summary(), category(), main(), Pack (+3 more)

### Community 95 - "CLAUDE: Javassist bytecode patcher"
Cohesion: 0.14
Nodes (15): CFR decompiler, Hand written Forge/Minecraft stub classes, Javassist bytecode patcher, Javassist cannot compile Java 8 lambdas, rtk hook drops grep/find flags (use Python os.walk), Toolchain set up each session (nothing preinstalled), Hypotheses: dedicated gate, YAML config path, dedicated lifecycle event, Personal use only, no redistribution, backup first (+7 more)

### Community 96 - "SKILL: City rivers and bridges v0.45.0 / v0.45.1: river contour through cities as canal chunks, street chunks carried over on decks, river cells level 0, new worlds only"
Cohesion: 0.14
Nodes (15): decimation-city case 2: the wasteland as the city's military area (v0.54.0), Skill decimation-city: city work order, server and dev test checks, lessons (bridges continue the street), casebook case 1, decimation-city case 1: bridges did not match the roads (v0.45.0 -> v0.45.1), CLAUDE.md v0.45.0 note: rivers through cities only for worlds created since 0.45, dev test bridge, game check as own step, CLAUDE.md v0.45.1 note: bridges are the street over the canal, DC bridge parts dropped, skill decimation-city, Bridges v0.45.1: the street (StreetPlan onWater) carried over the canal by RiverPlan, deck and piers below, railings on non street edges, wasteland bridges broken with a hole, City rivers and bridges v0.45.0 / v0.45.1: river contour through cities as canal chunks, street chunks carried over on decks, river cells level 0, new worlds only, Roadmap: bridges DONE v0.45.0, revised v0.45.1 (street over water), waiting for the user's look (+7 more)

### Community 97 - "gun_style_guide: Gun aiming: aim mode draws every gun at one fixed place (x 0.5, translate -1 -0.35 0.923), sPos unused in first person; screen centre at the top of the iron sights, y -4.85 to -5.0, z -0.15"
Cohesion: 0.15
Nodes (15): decimation-gun skill revision log: lessons from every user review (v1, v2, v0.37.0, v0.37.1), Aim rule corrected v0.37.2: centre passes the Uzi's rear aperture hole (about -4.65); compare with a Decimation gun in the same shot, Aim sway: headYawSway / dP tilt the gun after mouse moves, so judge aim only steady, Barrel attachment formula meets the muzzle only for flamePos x 12 to 15; Deci.offsetAttachment puts an offset model copy in AttachmentItem.ST per gun (MAC-10 -1.02, +0.14, 0), flamePos y sits about 0.85 above the bore (Uzi -4.5 over barrel -3.5); barrel attachments hang from it, Gun aiming: aim mode draws every gun at one fixed place (x 0.5, translate -1 -0.35 0.923), sPos unused in first person; screen centre at the top of the iron sights, y -4.85 to -5.0, z -0.15, Gun attachments: NBT sightAttach / barrelAttach / gripAttach, category match or all, fixed offsets in model units, barrel follows flamePos, foregrip misplaced even on M4A4 [inferred], Gun texture gradation: Decimation 15 to 31 part tones, faces 4.7 to 5.7 apart, texel noise 2 to 2.7, top lighter; gunmodel.paint reproduces it (+7 more)

### Community 98 - "SightPlacement: .draw()"
Cohesion: 0.22
Nodes (14): desc_params(), ident(), is_obf_member(), load_classes(), main(), norm_desc_type(), norm_src_type(), params_match() (+6 more)

### Community 101 - "Shell: Shell"
Cohesion: 0.18
Nodes (12): Case 14: HK416 floating stock pieces and 2 pillar sight (v0.39.1), Lesson 16: base parts reading past textureHeight wrap (M4A4 rear sight), Variant texture trap: M4A4 rear sight reads rows past textureHeight 32, black_part(), build(), dropped(), lower_bottom(), y of the lower panel's bottom edge at x (the wedge's front face is collapsed to… (+4 more)

### Community 102 - "gun_model_spec: Our own guns pipeline (gun_model_spec section 6): tools/guns spec to .bmodel / texture / icon / .anib, bbmcp previews, NewGuns registration (newGun, newMagazine, addLootLike, useGunSounds), dev test mode gun"
Cohesion: 0.18
Nodes (14): Skill casebook case 9: closing the gaps after acceptance (method, numbers, checks), CLAUDE.md v0.36.0: MAC-10 pipeline, our items register as deciworldgen:<name>, BModelBox facts: corner array order and added offsets, box UV face layout, Decimation textures 2 px per unit, 32x32 icons, Our own guns pipeline (gun_model_spec section 6): tools/guns spec to .bmodel / texture / icon / .anib, bbmcp previews, NewGuns registration (newGun, newMagazine, addLootLike, useGunSounds), dev test mode gun, gunmodel.py v2: hexahedron parts (part, inset, shift, mirror, octagon), 1x1x1 declared with corner offsets, flat tones, textureWidth 512 UV step 8, icon from the study.py render, anib with slide_names, MAC-10 v0.37.3 gaps closed: shape kinds 60/31/5/5, 110 parts, declared sizes 1x1xN, tone 47, middle details, raised details 0.08, MAC-10 v2 (v0.37.0): 102 parts, 9.35 x 8.1 x 1.5, proportions from the user's side photo, sights at the aim centre, checked in game (gunview, gun), Outside 3D generators (Claude Design 3D object): OBJ meshes not loadable, Decimation draws only addShape parts; user decision 2026-10-10: keep the tools/guns pipeline, no OBJ converter (+6 more)

### Community 103 - "CoreTest: CoreTest"
Cohesion: 0.31
Nodes (3): Listener, CoreTest, ItemStack

### Community 104 - "worldgen_architecture: Worldgen architecture v3 draft (layers, assets, size classes, capture tool)"
Cohesion: 0.15
Nodes (14): Builders: separate ceiling tiles with light panel grid and vents, Study: hand-built Decimation maps (USA coast, Decicraft, Cloverfield, world-e161), Builders: decay as dirt/leaves/water/cracked glass on intact shells, Builders: furniture in rows and islands (waiting rows, cubicles, shelf aisles), Autotest -Pstudy camera mode for reference maps, Builders: two tone WallOffice walls (dado bottom + top), Audit shots v0.23: 6 high offices and shops, Worldgen code map: building package parts (Shell, StoreyPlan, planners, Furnisher, Surfaces, Interior, Ruins, Yard) (+6 more)

### Community 105 - "lctranslate: lctranslate.py"
Cohesion: 0.29
Nodes (13): colour_of(), furniture(), material(), parse(), Rule based translation of 1.20 block states (vanilla + furniture mods) to…, Furniture of any mod -> one cell prop or vanilla stand-in; None if not…, The name without words that only look like "light": light_gray / light_blue…, 1.7.10 stone_slab / wooden_slab for a material. (+5 more)

### Community 106 - "ClickTest: ClickTest"
Cohesion: 0.22
Nodes (6): Bug: left click does nothing with fists or melee (closed: friend's own key binding), Knowledge index entry: docs/production_test.md, ClickTest, Testing the shipped game: production Forge server and client in the scratchpad, driven by devtest.py --live, Left click in the production client: all PASS with simulated presses; real mouse click untested, Traps: Forge universal from a server folder pulls minecraft_server via its manifest; -XstartOnFirstThread hangs LWJGL 2; javadoc jar; first test in a new world while falling; GitHub API 403 rate limit

### Community 107 - "TerrainEvents: TerrainEvents"
Cohesion: 0.17
Nodes (8): Railway tunnels flooded with standing water and lava (fixed v0.44.0), Decorate, TerrainEvents, Springs and lakes denied near rail chunks: a tunnel written by two windows was flooded by a neighbour's spring, Terrain: no lakes or springs where their area touches a railway chunk (v0.44.0), InitBiomeGens, net.minecraftforge.event.terraingen.InitMapGenEvent, Populate

### Community 108 - "Deci: .onServerTick()"
Cohesion: 0.22
Nodes (4): SupplyDropScheduler.drop skips a candidate column whose top block has a tile entity (prop, chest, car) and tries the next of its 12 random positions, ServerTickEvent, SupplyDropScheduler, net.minecraft.server.MinecraftServer

### Community 109 - "zones: Zones doc: Decimation's zone system end to end and what ZoneRules makes work in singleplayer"
Cohesion: 0.15
Nodes (13): Bug: zone rules dead in singleplayer (server proxy handlers), fixed v0.46.0, CLAUDE.md v0.46.0: zones work in singleplayer, docs/zones.md, Shared core: areas registry (SAFEZONE, MILITARY, RADIATION, BASE_CLAIM, CITY_CLAIM with states) on Decimation's zone and turf lists, Radiation zones: irradiated biome, crash and blast sites, leaking labs and bunkers, the wasteland city, Zones doc: Decimation's zone system end to end and what ZoneRules makes work in singleplayer, ZoneManager checks: isPlayerInZone (safezone false while the combat timer runs), isEntityInZone, near = box plus 10 blocks, Client zone lists: PacketZoneLists replaces ClientState bS..bW; HUD safezone chat line, GPS map markers; never sent for generated zones before v0.46.0, Zone data: ObjectZone boxes, EnumZoneType (safezone, radiation, military, police, scary, border), one live list ServerConfig.zoneList, ZoneStore merges ours (+5 more)

### Community 110 - "CLAUDE: tools/build.py real javac pipeline"
Cohesion: 0.19
Nodes (11): deobfuscation_data-1.7.10.lzma notch to SRG mapping, Compile only shim for Forge binpatch members (tools/shim_src), tools/build.py real javac pipeline, SpecialSource notch to SRG remapping, SRG member names (no reobfuscation step), classpath(), compile_sources(), inject() (+3 more)

### Community 111 - "DevAutoTest: DevAutoTest"
Cohesion: 0.19
Nodes (5): Live dev test mode: tools/devtest.py --live [--swap] [--stop]; game stays open (-Plive, DevTestLive on 127.0.0.1:25599), ready in about 26 s, reruns only cost their own time; hotswap for method bodies, Command, DevTestLive, Recently done: live dev test mode, java.net.Socket

### Community 112 - "DevAutoTest: DevAutoTest"
Cohesion: 0.28
Nodes (5): ClientTickEvent, Command, DevAutoTest, IntegratedServer, net.decimation.worldgen.devtest.DevTestMode

### Community 113 - "InfectedVariants: InfectedVariants"
Cohesion: 0.26
Nodes (3): InfectedVariants, Variant, net.minecraft.entity.ai.attributes.IAttributeInstance

### Community 115 - "AssetDir: AssetDir"
Cohesion: 0.29
Nodes (4): World, WorldGenCommand, net.minecraft.command.CommandBase, net.minecraft.command.ICommandSender

### Community 116 - "new_feature: Round 2 step 4: building quality pass before new types (a audit, b prop catalogue, c design spec, d interiors room by room, e exterior polish, f user review in game; 4d.1 doors + low debris DONE v0.17.0, next 4d.2 surfaces)"
Cohesion: 0.19
Nodes (13): Building categories: civilian (apartment, office, shops, houses, garage), police (police station), military (base, checkpoint), later medical / industrial; category decides sector and Decimation zone, Extensibility: polish built as reusable parts (shell, room programs, surface sets, door rules, story / decay layer, exterior add-ons); a new type = footprint rule + room programs + facade + loot profile, Survivor camp (about 1 in 12 buildings, one room: lantern, CanFire, bedroll, crates, radio, WaterPallet, barricaded door, note decal, graffiti outside), Spec user decisions: survivor camps (about 1 in 12 buildings), shop signs use BlockSign_* assets, reusable parts for later categories, start by fixing the audited apartment / office / shop, BiomeMap.biomeAt rules (seed only: city and military biomes exactly on sector squares, suburbs warped up to 56, dead wilderness within about 100 blocks, overgrown further out, no villages), Rivers as a noise contour (|simplex| < 0.022 at scale 520, domain warped, 32+ blocks from city and military sectors), Sector grid (16x16 chunks: WILD 40%, CIV 25%, CITY 15%, MIL 20%), Zone tagging (mil_ MILITARY, city_ and buildings POLICE, deciworldgen_zones.json) (+5 more)

### Community 117 - "performance: Performance doc: tools (gunperf, census, perfcheck, JFR via jcmd), findings, checklist"
Cohesion: 0.19
Nodes (13): Angelica in the dev client (-Pangelica, Decimation jar without its unused fastutil): starts but draws no far terrain on this Mac (GL 2.1), not usable, Lag checklist: perfcheck log, perfcheck world, census growth, JFR, gunperf vs control, fix and measure again, City view lag: first views were wrong (arena floor / clouds); valid view: props cost ~18% (34 vs 41 fps), the rest is city blocks and Mac heat, Dev world entity explosion: region slot (-1,-6) held chunk (-4,-1), entities doubled each save / load to 40 730 with a Forge stack trace per tick; fixed with fixchunk + census kill, Guns are not the cause: dropped / held fps of Uzi, UMP45, MAC-10, UMP9 equal to baseline, Decimation infected ran a full path search every tick on server and client plus a 40x40 horde scan: ~60% of both threads; PatchInfectedAI: fps 21 -> 35, Java 25 + lwjgl3ify still gets an OpenGL 2.1 context on this Mac: Angelica cannot help on macOS; Prism expected the same, Prop render distance by size 48/32/24 removes the props' ~18% cost in a city view with no visible loss; new defaults v0.38.2 (+5 more)

### Community 118 - "anvil118: anvil118.py"
Cohesion: 0.24
Nodes (10): chunk_biomes(), chunk_blocks(), chunks(), main(), Palette, {section Y: biomes[4,4,4] (y, z, x) global ids}., Entries packed without spanning longs (1.16+)., Global name -> id for a whole survey. (+2 more)

### Community 119 - "lc2schem: lc2schem.py"
Cohesion: 0.31
Nodes (9): building_columns(), main(), matches(), Pack, part_slices(), (layers bottom up, ground layer index): every floor picked like Lost Cities…, Lost Cities part conditions for one floor (cellars are negative)., tag() (+1 more)

### Community 120 - "lcpack: lcpack.py"
Cohesion: 0.29
Nodes (12): convert(), export_rails(), main(), paint(), rail_override(), rasterise(), Railway parts: Create tracks and vanilla rails become 1.7.10 rails (meta 1…, Lost Cities railway parts (RailwayParts.DEFAULT names, the pack's own versions)… (+4 more)

### Community 121 - "bug: Bug: armor buff ignores NPC gunfire (fixed)"
Cohesion: 0.24
Nodes (11): Thin props passed every overlap check (FIXED v0.50.0), propclash fixes: trapdoor not a door, walkway only, plates flat, glass view, load_facing(), load_geometry(), main(), overlap(), passage(), True when a door at this cell is walked through along x, False along z, None:… (+3 more)

### Community 122 - "PatchTurfNames: PatchTurfNames"
Cohesion: 0.20
Nodes (5): ClassPool, javassist.ClassPool, PatchLootRolls, PatchNoUpdate, PatchTurfNames

### Community 123 - "hotswap: Live editing: /deciworldgen reload + rebuild, -Photswap + tools/hotswap.py (method bodies only)"
Cohesion: 0.20
Nodes (10): Live loop: hotswap code, reload sets, rebuild in place, no restart per change, Live editing: /deciworldgen reload + rebuild, -Photswap + tools/hotswap.py (method bodies only), Roadmap: code and tools, Live dev test mode [idea]: keep the dev game open and start test modes over a localhost port, Roadmap code and tools: port to modern Minecraft PARKED (user 13 Oktober 2026), Split LcCity (1225 lines) and StructureGenerator (861 lines) into street, building, lot and edge plan files; old procedural city apart, jdb(), main() (+2 more)

### Community 124 - "PlaceNames: PlaceNames"
Cohesion: 0.26
Nodes (3): NamedPlace, World, PlaceNames

### Community 125 - "GunNoise: Override"
Cohesion: 0.30
Nodes (4): Investigate, net.minecraft.entity.ai.EntityAIBase, net.minecraft.entity.EntityCreature, Override

### Community 126 - "Protection: Protection"
Cohesion: 0.35
Nodes (4): FileData, Protection, Values, Shared core: protection table per armor item (infected hits, gunfire, bite chance, radiation)

### Community 127 - "UpdateScreen: UpdateScreen"
Cohesion: 0.27
Nodes (4): Hook, GuiButton, UpdateScreen, net.minecraftforge.client.event.GuiOpenEvent

### Community 131 - "DevTestMode: DevTestMode"
Cohesion: 0.20
Nodes (3): City street after the cache (-Pcityfps): props 5.5% of the client thread, chunk drawing 15%, chunk rebuild 12.7%; props no longer the bottleneck, CityFpsTest, DevTestMode

### Community 132 - "UpdateSwap: Auto update from our GitHub releases (v0.59.0): Updater check, Decimation's Update button via PatchNoUpdate v2, UpdateScreen progress, sha256 check, UpdateSwap after the game closes"
Cohesion: 0.20
Nodes (7): No city on a friend's install: city pack never in a release; FIXED v0.58.0 by one jar (tools/onejar.py, BundledContent), checked on a real Forge server, v0.58.0: release is one Decimation.jar from tools/onejar.py; test on a real Forge server; ScopeZoom copy change; dev tests click, hitbox, v0.59.0 line: auto update, release rules (asset Decimation.jar, dotted tag), dev test update with the fake release server, UpdateSwap, Idea 20: auto update from GitHub releases with an Update button and progress screen, swap after the game closes; project page on Vercel, update_v0.59 shots: Update button, progress 33 %, ready screen, Auto update from our GitHub releases (v0.59.0): Updater check, Decimation's Update button via PatchNoUpdate v2, UpdateScreen progress, sha256 check, UpdateSwap after the game closes

### Community 133 - "bug: Bug: armor buff ignores NPC gunfire (fixed)"
Cohesion: 0.18
Nodes (11): Bug (fixed v0.30.2): full military armor made NPC gunfire almost harmless: armor multiplies per piece (x0.149 marine set) and vanilla hit cooldown dropped group hits; NPC shots are direct damage, not bullets, v0.30.4: NPC hit cooldown 0.25 s (npcHitCooldownTicks 5), v0.30.3: vanilla hit cooldown kept for NPC hits (npcHitsSkipCooldown false), x5 stays, Fix v0.30.2: NPC gun hits on players x npcDamageToPlayer (5) after armor, every NPC hit lands (LivingAttackEvent clears hurtResistantTime); bare 10 hp, marine set 1.49, 5 hits 7.47, Done v0.30.3: NPC shots traced with spread per tier, stopped by walls, impact particles, tracer always visible along the real line (PatchTracer v2 shotHook + NpcShots), CLAUDE.md v0.30.2 note: NPC hits x5, no hit cooldown, mech swap, CLAUDE.md v0.30.3: traced NPC shots, PatchTracer v2 then PatchFactions, test NPCs on a block, Balance: NPC gunfire on the player x5 after armor, every hit lands (v0.30.2) (+3 more)

### Community 134 - "Doors: Doors"
Cohesion: 0.22
Nodes (5): Worldgen rule: no vanilla doors in anything we generate (Doors.swap), Doors, Plan, Styled, Vanilla doors become Decimation doors at write time (worldgen/Doors)

### Community 135 - "Palettes: Palettes"
Cohesion: 0.29
Nodes (3): Palettes, Style, Entry

### Community 136 - "Graded: Graded"
Cohesion: 0.18
Nodes (3): Garrisoned, Graded, Plan

### Community 138 - "ZoneStore: ZoneStore"
Cohesion: 0.33
Nodes (3): BridgeTest, Dev test bridge: forces city rivers on, fresh span with city streets at both ends, deck, water, no building over canals, walk across, lc_bridge_v0.45.0 shots: seed 1 world map and top down renders of canals and bridges, first dev test bridge views

### Community 139 - "FireRateTest: FireRateTest"
Cohesion: 0.29
Nodes (4): FireRateTest, Entity, EntityLivingBase, EntityPig

### Community 140 - "SealedCaves: SealedCaves"
Cohesion: 0.27
Nodes (6): Caves, Override, Ravines, SealedCaves, net.minecraft.world.gen.MapGenCaves, net.minecraft.world.gen.MapGenRavine

### Community 141 - "new_feature: Zones, factions and world list with starting points: SAFEZONE zones, Decimation turf system (TurfManager, server only, likely dead in singleplayer), NYPD looks, civilian HumanEntity2, irradiated biome, military sectors, bandit tiers"
Cohesion: 0.20
Nodes (11): SAFEZONE, Roadmap: advanced military base in progress (v0.42.0, waiting for review), Roadmap: advanced safezone, zones v0.46.0 and survivor camps v0.47.0 done, next the fortified city building, Roadmap: zones, factions and world (user list 9 Oktober 2026): advanced safezone, player claimable zones, claims lost to NPCs, police NPC, armed survivor civilians, radiated areas, advanced military base, advanced bandits, Plan: safezones destroyed by the player (breach state, fall, loot unlocked, humanity, retake), not built, Decimation turf system: net.decimation.mod.server.turf TurfManager (add / remove / owner / isCapturable, turfs json, TurfCommands), server proxy only, Zones, factions and world list with starting points: SAFEZONE zones, Decimation turf system (TurfManager, server only, likely dead in singleplayer), NYPD looks, civilian HumanEntity2, irradiated biome, military sectors, bandit tiers, Safezone user choices: one per city and camps outside, walled camp and fortified building, guards friendly unless provoked, traders, guards, civilians, armed survivors (+3 more)

### Community 142 - "gunmodel: Part"
Cohesion: 0.18
Nodes (7): octagon(), Part, A hexahedron: corner (ix, iy, iz) in {0, 1}^3 -> absolute model point., Shrink the face at `side` (0 = low end, 1 = high end) of `axis`: amounts per…, Move the face at `side` of `axis` (a skew / shear)., The same part mirrored about z = ZC (left / right pairs)., A round bar along x as Decimation builds it: a middle slab plus a trapezoid…

### Community 143 - "mapsurvey: mapsurvey.py"
Cohesion: 0.25
Nodes (6): chunk_ids(), chunks(), colour(), main(), ids[y, z, x] for the chunk (256 high)., registry()

### Community 144 - "lcaudit: lcaudit.py"
Cohesion: 0.27
Nodes (9): Converted buildings used LED lamp blocks as floor (FIXED 9 Oktober 2026: 'light' in colour names like light_gray matched the lamp rule), Knowledge index: translation audit tools/lcaudit.py, lcpack content: 290 converted buildings, stairs per district style (local only), Translation audit 9 Oktober 2026: dropped blocks and props by placement over 290 converted buildings; black sandstone is asphalt, laboratory panels, wallpaper, corundum, posts, shelves, seats mapped, Recently done: converted building quality pass (pack rebuilt, LED floor fixed, about 30000 dropped blocks mapped), lc_quality_v0.28.10 shots: 30 interior views after the translator fix, no lamp floors, ceiling redstone lamps; black sandstone stairs now cobblestone, buildings(), main() (+1 more)

### Community 145 - "SKILL: Project skill decimation-military-base: research, build (Canvas, occupancy grid, greedy fit, poi), test, small detail review checklist"
Cohesion: 0.20
Nodes (10): Military base case 1: first FOB generator, four rounds (colours, test setup, overlaps, density), Military base case 2: user review of v0.42.0 (HESCO prop FPS, unlocked TOC door, orange dirt, prop clashes) -> v0.42.1, Military base case 3: second review, test site moved to -20000 y 230, old sites cleared, multiblock rebuild trap, Military base case 4: third review (blocked hatch, floating hangar roof, shelter roof, worldgen cutting the sky test), Military base case 5: walled in ladders and a missing TOC (v0.42.4), Lessons 12 and 13: check the player's way to a thing; refused modules must be loud, Military base lessons: research first, occupancy grid against overlaps, company streets, sky test without grading, orange dirt in Decimation's pack, avoid regular patterns, camera points in the generator, Project skill decimation-military-base: research, build (Canvas, occupancy grid, greedy fit, poi), test, small detail review checklist (+2 more)

### Community 147 - "?: Sectors.java"
Cohesion: 0.42
Nodes (3): StreetProps, net.decimation.worldgen.Plan, net.decimation.worldgen.StructureGenerator

### Community 148 - "BiomeMap: BiomeMap"
Cohesion: 0.33
Nodes (3): BiomeMap, net.minecraft.world.gen.NoiseGeneratorSimplex, NoiseGeneratorSimplex

### Community 149 - "ideas: Ideas for later (user, 12 Oktober 2026)"
Cohesion: 0.20
Nodes (10): Ideas for later (user, 12 Oktober 2026), Idea 19: city sizes of 8 to 64 districts instead of mostly one sector (4 districts), Ideas planned together 13 Oktober 2026 in docs/systems_plan.md, More crate / loot block types (industrial, civilian), Demons in wasteland / military zones, GitHub link on the home menu, Five stages of infection until death, More military NPC outfits, sniper bandit egg (+2 more)

### Community 150 - "MenuTest: DevTestMode"
Cohesion: 0.25
Nodes (6): New players forced to update (Update instead of Play, empty server list): UpdateChecker.isUpdateRequired true without DecimationVoiceChat.jar; FIXED v0.57.2 by PatchNoUpdate, PatchNoUpdate.java: net/decimation/mod/common/utils/h.gI always false, three jars; dev test menu, v0.57.2: no forced Update for players without the voice chat jar, MenuTest, DevTestMode, menu_v0.57.2: home menu without the voice chat jar, Resume Game, no Update button

### Community 151 - "sightfit: sightfit.py"
Cohesion: 0.31
Nodes (8): Bug: sights float above flat top rifles (v0.40.1), Case 17: sights floating above flat top rifles, bbox(), gap(), main(), rail_top(), Top of the receiver / rail in the sight zone: the highest long part there, iron…, dy > 0: the sights float that much above this gun's rail (they are placed for…

### Community 152 - "bug: Bug: vehicles destroyed in one hit (fixed v0.8.1)"
Cohesion: 0.28
Nodes (9): Bug: vehicles destroyed in one hit (fixed v0.8.1), VehicleHitHandler (v0.8.1), VehicleEntity (deci.ad.e) and parts, VehicleHitHandler, checks: vehicle arrow (arrow and snowball do not pick up), Arrows picked up empty vehicles (FIXED v0.48.1), Known non-bugs (trapped chest, truck wreckage, no trunk), Vanilla chest GUI suppressed for loot GUI (+1 more)

### Community 153 - "CityEdgeTest: CityEdgeTest"
Cohesion: 0.42
Nodes (3): ChunkProviderServer, CityEdgeTest, City edge worn ground (v0.55.0): EdgePlan.wear broken pavement band, coarse dirt rejected (orange)

### Community 154 - "Heuristic Auto Namer"
Cohesion: 0.28
Nodes (7): Case 18: Mk18 TS stock, EOTech 558, ACOG TA11 (v0.41.0), build(), dropped(), lower_edge(), px(), y of the stock's bottom edge (the lower strut) at model x: photo (480, 1470) to…, The M4A4 parts our Mk18 replaces (by position, measured on the M4A4:…

### Community 155 - "Condition: com.google.gson.JsonObject"
Cohesion: 0.22
Nodes (9): v0.21.0 critic pass 1, kitchen rework, wall breaches by column, narrow blocks ladder, -Pflats audit, Apartment references: real-world clearances, 1.7.10 furniture techniques, review checklist, Apartment review checklist: walkway, function readable, 40-60% free, palette, plausible decay, per room rules, Clearances: 1 block walkway, sofa-table 0-1 block, one free bed side, kitchen work triangle in a 4-6 block run, 1.7.10 techniques: stairs sofas with trapdoor arms, slab coffee tables, cauldron sink + tripwire tap, quartz stair toilet, paintings, wool curtains, Critic pass 1 on v0.20 flats: overall 3/10, verified findings and fixes, critic wrong on chair facing and lintels, Root cause found: narrow apartment blocks had no flats (core took the width); ladder under 16 wide, Open after pass 1: empty living fallback, wall detail layer, sofa arms, deeper living sets, bath sets, bedroom min width, camera (+1 more)

### Community 156 - "DecimationBiomes: DecimationBiomes"
Cohesion: 0.33
Nodes (4): DeciBiome, DecimationBiomes, Biome names carry AmbientMusicPlayer keywords (forest, river, plains, hills, decimated, irrated), Fixed biome ids 110..118 (Decimated City, Suburbs, Irradiated Military Zone, Decimated Plains, Burnt Forest, Overgrown Plains / Forest / Hills, Murky River)

### Community 157 - "DecimationBiomes: DecimationBiomes"
Cohesion: 0.33
Nodes (8): camel(), classes(), known_fields(), main(), (binary name, source text) for every top-level file., Field names already chosen by the AI tables: (owner, obf) -> name., Field names declared directly in the outer class (indent 4)., top_level_fields()

### Community 158 - "GunPerfTest: GunPerfTest"
Cohesion: 0.39
Nodes (3): GunPerfTest, EntityItem, net.minecraft.entity.item.EntityItem

### Community 159 - "edgescan: edgescan.py"
Cohesion: 0.39
Nodes (6): Bug: city edge ramp missed its outer columns (fixed v0.24.4, populate scans cells within EDGE), City edge ramp: 24 wide, rounded corners, nearest cell owns a column, wobbled contours, is_city(), jrandom_float(), region_sector(), s64()

### Community 160 - "edgescan: gradescan.py"
Cohesion: 0.29
Nodes (7): walkcheck stopped at y 4, propclash counted shelf tops as floating (FIXED v0.48.0), CLAUDE.md v0.42.4 note, How to test: walkcheck.py rules and the must-be-0 findings, Prop rule 12: every loot container reachable (walk test LOOT), collision_table(), main(), Prop block -> (collision height, walk through) from Decimation's registry code.

### Community 161 - "edgescan: gradescan.py"
Cohesion: 0.43
Nodes (6): Wall scan reproduction on seed 1 (2 of 33 buildings, one real: b4_4_2), main(), props(), surface(), main(), populated()

### Community 162 - "SKILL: Project skill decimation-gun: end to end gun workflow (read docs, references, part plan, anchors, build, compare with study.py, register, gunview test, finish)"
Cohesion: 0.25
Nodes (8): Skill casebook case 11 and lesson 14: verify performance for every gun; the 1 fps report was a corrupt chunk and infected pathing, Case 21: AS Val sights on the handguard, moved 4 back over the receiver (v0.42.2), Project skill decimation-gun: end to end gun workflow (read docs, references, part plan, anchors, build, compare with study.py, register, gunview test, finish), Lesson 23: sights must sit over the receiver (fixed sight zone x 0.8..5.5; AS Val receiver ends at 2.6), sightfit SHIFT dx, aim and reticle compensated, create_weapons.md: new models are built in code by tools/guns and the skill decimation-gun since v0.36.0; Techne is the older manual route, Style guide 14: sight position along the gun, SHIFT for guns whose receiver ends before the sight zone, Roadmap items and weapons (11 Oktober 2026): waiting looks (MAC-10 seated, HK416 fixes), more scopes, more guns, sniper nerf pointer, documentation.md: guns of our own (MAC-10, UMP9, HK416 / Tan, Mk18; rpm, damage halved by the global patch, loot with their model gun) and sights EOTech 558 / ACOG TA11

### Community 163 - "Updater: State"
Cohesion: 0.25
Nodes (8): State, AVAILABLE, CHECKING, DOWNLOADING, FAILED, IDLE, NONE, READY

### Community 169 - "study: .verts()"
Cohesion: 0.36
Nodes (6): contact(), inside(), True when point q lies in the part's (convex) shape, grown by eps., Parts that do not sit on anything. A thin detail (0.15 or less in some axis):…, The 8 corners in model space, indexed like vanilla ModelBox: v7 (0,0,0), v0…, (4 vertices, (u1, v1, u2, v2)) per face, like vanilla ModelBox.

### Community 174 - "DeciGenLayer: DeciGenLayer"
Cohesion: 0.33
Nodes (4): DeciGenLayer, Override, GenLayer swap on WorldTypeEvent.InitBiomeGens (TERRAIN_GEN_BUS): two DeciGenLayers (1:4 and 1:1) reading one BiomeMap, net.minecraft.world.gen.layer.GenLayer

### Community 175 - "graph_update: graph_update.py"
Cohesion: 0.52
Nodes (6): finish(), prepare(), Keep an old community name when its members mostly carried over., relabel(), rj(), wj()

### Community 176 - "SKILL: Props fitted to their drawn size (v0.49.0, city/PropFit)"
Cohesion: 0.33
Nodes (6): Converted city props into walls / floating / facing walls (FIXED v0.49.0), Case 2: converted city props (v0.49.0), Lesson: converted content needs the drawn size check, fix by rule at load, Dev test cityprops (CityPropsTest): fresh city cell, shots of fitted props, Props fitted to their drawn size (v0.49.0, city/PropFit), worlddump.py: props of a generated world for propclash

### Community 177 - "zones: Place names: districts and bases through Decimation's turf banner (v0.52.0)"
Cohesion: 0.47
Nodes (6): Bug: floating district name showed _ (FIXED v0.52.1), ClanTurfLayer (deci.M.l): floating turf name over the turf's middle, Dev test places, Place names: districts and bases through Decimation's turf banner (v0.52.0), City names on entering: [district] - [number], PatchTurfNames: chat and floating turf name show _ as spaces

### Community 178 - "HitboxTest: HitboxTest"
Cohesion: 0.47
Nodes (3): Shots at a big zombie's head miss; not reproduced: Hulk / Bloater models inside their 1.2 x 2.85 boxes, HitboxTest, bugs_v0.58 shots: hitbox side view, scope sheet

### Community 179 - "graph_update: graph_update.py"
Cohesion: 0.33
Nodes (6): v0.19.0 storey height 5 with own ceilings, Revised order after user 0.18 review: structure first, then furniture sets, then reference library + critic, Step 1a done v0.19.0: storeys 5 high, own white plaster ceiling layer, 5 step stair runs, Step 1b next: plaster lining inside outer walls, with furniture sets, audit_v0.19: dark tile ceilings first, then white plaster ceilings, Revised interior plan after user review: structure, data driven furniture sets, references + critic; Lost Cities idea not port

### Community 182 - "perfcheck: perfcheck.py"
Cohesion: 0.33
Nodes (5): NpcKind, BANDIT, HAZMAT, SOLDIER, SOVIET

### Community 183 - "NpcKillTest: .server()"
Cohesion: 0.40
Nodes (3): ItemStack, Minecraft, NpcKillTest

### Community 188 - "DecimationWorldType: DecimationWorldType"
Cohesion: 0.40
Nodes (3): DecimationWorldType, Override, net.minecraft.world.WorldType

### Community 189 - "new_feature: Juggernaut v0.31.0: Soviet side, matching juggernaut set, MGs or armor piercing Barrett, 200 hp, takes 25%, speed 0.18, no knockback; only with military groups (juggernautChance 0.15) and its egg"
Cohesion: 0.33
Nodes (6): Recently done v0.32.0: elite military, bursts, magazines, npc_elite_v0.32.0: elites with night vision goggles, npc_snipers_v0.32.1: juggernaut with Barrett and elite snipers, Elite military v0.32.0: marine black, night vision goggles, MGs or snipers with all attachments, 150 hp, takes 16%, x2 damage; only with military groups (eliteChance 0.2) and its egg, Sniper range v0.32.2: NPCs with sniper rifles look 96 blocks for enemies (Decimation findTarget 32), shots fly 160, Sniper versions v0.32.1: juggernaut_sniper (Barrett) and elite_sniper (Barrett or sniper rifles), sniperShare 0.5, base juggernaut / elite MG only

### Community 190 - "perfcheck: perfcheck.py"
Cohesion: 0.60
Nodes (5): chunks(), fix_chunk(), log_report(), main(), world_report()

### Community 191 - "PatchIronSights: Case 16: Mk18 Mod 1 and iron sights hidden under a sight (v0.40.0)"
Cohesion: 0.40
Nodes (3): Case 16: Mk18 Mod 1 and iron sights hidden under a sight (v0.40.0), defaultScopeModel parts hidden while a sight is attached (19 guns), PatchIronSights

### Community 192 - "safezone: City refuge: fortified building on the block nearest each city region's middle (v0.48.0)"
Cohesion: 0.40
Nodes (5): Case 10: city refuge, first build (v0.48.0), Lessons 18-21: stairs in walkcheck, rotations via live, cameras, multiblock rebuild, City refuge: fortified building on the block nearest each city region's middle (v0.48.0), !cityRefuges world flag: refuges only in worlds created since v0.48, Refuge layout: apron barriers, concrete wall, chicane gate, 3 storey building (market hall, clinic, canteen, dormitory, armory, roof nests), yard

### Community 193 - "new_feature: Zombie variants v0.34.0: runner, riot, screamer (scream alerts infected within 32), night frenzy; InfectedVariants, ZombieEgg, config deciworldgen_zombies.cfg; dev test mode zombies"
Cohesion: 0.40
Nodes (5): CLAUDE.md v0.34.0: zombie variants, attribute modifiers trap, Zombie variants (v0.34.0) in documentation, zombies_v0.34.0: lineup common, runner, riot, screamer, InfectedEntity facts: 20 hp, attack 3, speed base reset every tick (0.25, horde 0.3), zone looks military / police, 8% bite, Zombie variants v0.34.0: runner, riot, screamer (scream alerts infected within 32), night frenzy; InfectedVariants, ZombieEgg, config deciworldgen_zombies.cfg; dev test mode zombies

### Community 196 - "NpcKind: NpcKind"
Cohesion: 0.70
Nodes (4): axis(), clean(), letter(), main()

### Community 197 - "RefugeTest: RefugeTest"
Cohesion: 0.80
Nodes (4): clamp(), main(), side(), top()

### Community 199 - "new_feature: Worldgen round 2 plan (option A: terrain blending, street life, full multiblocks, new building types, loot balance)"
Cohesion: 0.50
Nodes (4): v0.18.0 apartment rooms and propFacing fix, Step 3 apartment rooms done v0.18.0: kitchen run, dining, TV + sofa, bedroom, bathroom, lobby on one side, audit_v0.18 / plans_v0.18: kitchen run, checker ceiling issue, furnished flats, 4d.3 apartment rooms done v0.18.0, next 4d.4 office programs

### Community 201 - "ideas: Wasteland locked doors (v0.55.0): most buildings locked by loot, Door_Emergency_1_Locked + military keycard screen, flood fill entrances"
Cohesion: 0.50
Nodes (4): Idea 17: crate levels 1 / 2 / 3 with richer loot, behind locked doors first, Wasteland locked doors (v0.55.0): most buildings locked by loot, Door_Emergency_1_Locked + military keycard screen, flood fill entrances, v0.55.0: elite / juggernaut rifles with attachments, Decimation doors everywhere, city edge worn ground, locked wasteland, Shots v0.55: city edge, locked doors, NPC rifles

### Community 205 - "dressbalance: dressbalance.py"
Cohesion: 0.83
Nodes (3): cosine(), load(), main()

### Community 206 - "worlddump: worlddump.py"
Cohesion: 0.83
Nodes (3): chunks(), main(), prop_names()

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
- **359 isolated node(s):** `Technic modpack Decimation 1.7.10 (linusrhone)`, `Prism Launcher instance mods folder`, `Subsystem taxonomy (core, proxy, network, loot, zone, ...)`, `ServerCommandRegistrar (deci.aK.o)`, `ChatHandler (deci.aK.n, radio chat)` (+354 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **135 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

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
- **Why does `Deci` connect `Deci: Deci` to `?: net.minecraft.item.ItemStack`, `?: net.minecraft.world.World`, `Deci: net.minecraft.item.Item`, `architecture: Bug tracker (bug.md)`, `Garrisons: StructureData`, `?: cpw.mods.fml.common.eventhandler.SubscribeEvent`, `Deci: net.minecraft.entity.player.EntityPlayer`, `MenuTest: DevTestMode`, `StreetPlan: StreetPlan`, `DeciBiome: DeciBiome`, `ZombieEgg: cpw.mods.fml.relauncher.SideOnly`, `ItemsTest: .lootTable()`, `CLAUDE: Testing without the user (devtest.py first choice, servertest, worldcheck, worlddiff, autotest flags)`, `devtest: Recently done: v0.28.0..0.28.4 cheap scope, v0.28.5 NPC tracers, v0.28.6 / 0.28.7 line of sight cache and prop render distance, v0.28.8 Deci + ZoneKind + launch wait removed, v0.28.9 dev test modes`, `porting: Study: DeceasedCraft city buildings (DCTweaks jar Lost Cities data)`, `LootPools: LootPools`, `Deci: LocalStats`, `systems_plan: Systems plan: loot, human NPCs, zombies, world planned together (13 Oktober 2026)`, `Deci: .onServerTick()`, `InfectedVariants: InfectedVariants`, `PlaceNames: PlaceNames`?**
  _High betweenness centrality (0.097) - this node is a cross-community bridge._
- **Why does `Porting Decimation to modern Minecraft: three paths (parked plan)` connect `porting: Study: DeceasedCraft city buildings (DCTweaks jar Lost Cities data)` to `hotswap: Live editing: /deciworldgen reload + rebuild, -Photswap + tools/hotswap.py (method bodies only)`, `lctranslate: lctranslate.py`, `CLAUDE: decimation-singleplayer README (public repo overview)`, `Deci: Deci`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
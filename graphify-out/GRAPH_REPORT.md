# Graph Report - .  (2026-10-13)

## Corpus Check
- 18 files · ~497,931 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3846 nodes · 8630 edges · 265 communities (153 shown, 112 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 713 edges (avg confidence: 0.83)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Deci: Deci
- SurvivorCamp: SurvivorCamp
- lctranslate: lctranslate.py
- SurvivorCamp: SurvivorCamp
- LcContent: LcContent
- ?: DecimationWorldGen
- new_feature: NPC tiers design and result (v0.30.0): tier per armed NPC on first join (gear, gun, health, fire rate, share of player gun damage), stored in entity data, gun synced via data watcher slot 26; bandit light / medium / heavy, soldier camo sets, Soviets as enemy military
- building_design: Procedural building design doc (city blocks, Building v2)
- BiomeMap: BiomeMap
- gunmodel: gunmodel.py
- StreetDress: StreetDress
- ScopeZoom: ScopeZoom
- CanvasPlan: CanvasPlan
- InfectedVariants: net.minecraft.entity.Entity
- Surfaces: net.minecraft.block.Block
- architecture: Bug tracker (bug.md)
- Building: Building
- BuildingDress: BuildingDress
- StreetPlan: StreetPlan
- SafezoneSites: SafezoneSites
- hwmap: hwmap.py
- interior_spec: Building interior and exterior spec (round 2 step 4c, approved 7 Oktober 2026)
- StructureGenerator: StructureGenerator
- ?: net.minecraft.entity.EntityLiving
- NpcEgg: Override
- Deci: net.minecraft.item.Item
- CanvasPlan: CanvasPlan
- systems_plan: Systems plan: loot, human NPCs, zombies, world planned together (13 Oktober 2026)
- LootTest: net.minecraft.client.Minecraft
- NoiseTest: NoiseTest
- StreetPlan: StreetPlan
- ?: cpw.mods.fml.common.eventhandler.SubscribeEvent
- BuildingPlan: BuildingPlan
- PropFit: PropFit
- architecture: Bug tracker (bug.md)
- DeciBiome: DeciBiome
- Deci: net.minecraft.item.ItemStack
- LcCity: LcCity
- CameraViews: CameraViews
- CLAUDE: Knowledge index (docs/, interior spec, deobf notes, names.tsv, trackers)
- Shell: net.minecraft.block.Block
- lcstudy: dcinventory.py
- ?: cpw.mods.fml.common.eventhandler.SubscribeEvent
- terrain: Decimation world type doc (terrain, 0.14.0)
- StoreyPlan: StoreyPlan
- EdgePlan: EdgePlan
- new_feature: Current state and pending decisions
- city_engine: City engine: Lost Cities style cities from converted DeceasedCraft content
- DevTestResults: ScopeTest
- Ruins: .unit()
- Wasteland: Wasteland
- CityRefugePlan: CityRefugePlan
- architecture: Obfuscation map (package to meaning)
- NpcShots: NpcShots
- worldgen: Lot grading (city yards, 0.13.0: Graded plans own the 26x26 lot, Slices grades every lot column before writing)
- Graded: net.minecraft.world.World
- Areas: Areas
- CLAUDE: Testing without the user (devtest.py first choice, servertest, worldcheck, worlddiff, autotest flags)
- city_dressing: City dressing: Decimation props through the whole city (v0.50.0)
- CLAUDE: Javassist bytecode patcher
- LcContent: LcContent
- ZoneStore: ZoneStore
- Capture: Capture
- NpcTest: .batches()
- bug: Report: FPS drop in prop dense areas (line of sight cached v0.28.6, model drawing remains)
- safezone: Safezone sites doc: survivor camps (references, layout, residents, placement, test), fortified city building next
- prop_catalogue: Decimation prop catalogue (look, size and facing of every deci: block, from the prop gallery)
- FacingTest: DevTestMode
- Deci: net.minecraft.entity.player.EntityPlayer
- FurnitureSets: FurnitureSet
- RiverPlan: RiverPlan
- furniture_sets: Furniture sets doc: data driven JSON furniture groups, user editable
- Garrisons: StructureData
- porting: Study: DeceasedCraft city buildings (DCTweaks jar Lost Cities data)
- gun_style_guide: Decimation gun style guide: study of all 98 shipped guns (look and construction rules for our own guns)
- bug: Bug tracker (bug.md)
- NoiseTest: NoiseTest
- CLAUDE: Javassist patches go into three jars (patched, dist, dev base) plus Prism; sources in tools/patches (PatchSwing, PatchPropCulling, PatchScope, PatchTracer, PatchBackend)
- devtest: tools/devtest.py: dev test modes in ONE game launch, results file, contact sheet per mode (first choice since 2026-10-09)
- new_feature: Current state and pending decisions
- LotPlan: LotPlan
- ServerChecks: ServerChecks
- StreetProps: Slices.java
- ?: cpw.mods.fml.relauncher.SideOnly
- worldcheck: worldcheck.py
- bug: Feature tracker (new_feature.md)
- zones: Zones doc: Decimation's zone system end to end and what ZoneRules makes work in singleplayer
- city_engine: City engine: Lost Cities style cities from converted DeceasedCraft content
- Capture: Capture
- LotDress: LotDress
- WorldClock: WorldClock
- CLAUDE: Testing without the user (devtest.py first choice, servertest, worldcheck, worlddiff, autotest flags)
- bug: Bug: armor buff ignores NPC gunfire (fixed)
- hk416: hk416.py
- SKILL: decimation-gun skill revision casebook: 8 cases (boxy look, aim centre, suppressor floating, suppressor low / threads, gradation, stock in sight picture, icon halo, test traps) with symptom, cause, code location, fix, check, evidence
- DevAutoTest: DevAutoTest
- LootPools: LootPools
- ZoneRules: ZoneRules
- Furnisher: Furnisher
- SKILL: BuildingDress.java
- worldgen: World generation doc (deciworldgen)
- ?: Sectors.java
- Dress: Dress
- Rails: Rails
- CLAUDE: tools/build.py real javac pipeline
- ideas: Ideas for later (user, 12 Oktober 2026)
- SightPlacement: .draw()
- CoreTest: CoreTest
- performance: Performance doc: tools (gunperf, census, perfcheck, JFR via jcmd), findings, checklist
- Shell: Shell
- city_engine: City highways: seed based network between city regions, open / bridge / tunnel chunks, deck at 64, side ramps
- CityNames: CityNames
- RailTest: RailTest
- bug: worlddiff.py
- SupplyDropScheduler: .onServerTick()
- PropsTest: TracerTest
- DevAutoTest: DevAutoTest
- gun_model_spec: Our own guns pipeline (gun_model_spec section 6): tools/guns spec to .bmodel / texture / icon / .anib, bbmcp previews, NewGuns registration (newGun, newMagazine, addLootLike, useGunSounds), dev test mode gun
- PlaceNames: PlaceNames
- BuildingLayout: BuildingLayout
- Slices: Slices
- AssetDir: AssetDir
- DECIMATION_MOD_TASK: Goal: loot crates and cars in singleplayer
- Protection: Protection
- AssetDir: AssetDir
- WastelandFence: .column()
- Ruins: .unit()
- gun_style_guide: Gun aiming: aim mode draws every gun at one fixed place (x 0.5, translate -1 -0.35 0.923), sPos unused in first person; screen centre at the top of the iron sights, y -4.85 to -5.0, z -0.15
- Doors: Doors
- StreetLayout: Base
- ZoneStore: ZoneStore
- SealedCaves: SealedCaves
- Graded: Graded
- gunmodel: Part
- interior_spec: Surfaces table per space (planks + FloorCarpet rugs, FloorTiles in kitchens / baths / corridors, WallOffice colour sets, Ceiling_1..4, lights, CeilingVent; interiors no longer copy the facade)
- NewGuns: NewGuns
- FireRateTest: FireRateTest
- GunPerfTest: GunPerfTest
- MilBaseTest: MilBaseTest.java
- Deci: net.minecraft.entity.player.EntityPlayer
- sightfit: sightfit.py
- CityEdgeTest: CityEdgeTest
- PatchTurfNames: PatchTurfNames
- VanillaMobs: VanillaMobs
- Heuristic Auto Namer
- Condition: com.google.gson.JsonObject
- DecimationBiomes: DecimationBiomes
- GunNoise: Investigate
- CensusTest: CensusTest
- ShotTest: ShotTest
- edgescan: gradescan.py
- ?: net.minecraft.nbt.NBTTagCompound
- new_feature: Feature tracker (new_feature.md)
- GunTest: GunTest
- ZombieTest: ZombieTest
- MilBaseTest: MilBaseTest
- study: .verts()
- SKILL: Project skill decimation-gun: end to end gun workflow (read docs, references, part plan, anchors, build, compare with study.py, register, gunview test, finish)
- NewGuns: NewGuns
- CityViewTest: DevTestMode
- GunViewTest: GunViewTest
- graph_update: graph_update.py
- perfcheck: perfcheck.py
- NpcKillTest: .server()
- RefugeTest: RefugeTest
- PatchIronSights: Case 16: Mk18 Mod 1 and iron sights hidden under a sight (v0.40.0)
- safezone: City refuge: fortified building on the block nearest each city region's middle (v0.48.0)
- LotDress: .dress()
- NpcKind: NpcKind
- RefugeTest: RefugeTest
- DecimationWorldType: DecimationWorldType
- new_feature: Worldgen round 2 plan (option A: terrain blending, street life, full multiblocks, new building types, loot balance)
- dcinventory: dcinventory.py
- mac10: mac10.py
- Sectors: Sectors
- dressbalance: dressbalance.py
- worlddump: worlddump.py
- Schematic: Schematic
- facing_table: facing_table.py
- hesco_textures: hesco_textures.py
- PatchFactions: PatchFactions.java
- bug: Dev test game crashed once in vanilla grass ticking (open, not reproduced)
- bug: Water spread over a wasteland crossing once in the bridge test (open)
- ?: BuildingLayout
- ?: BuildingPlan
- PatchMenuStats: PatchMenuStats
- topdown: topdown.py
- worlddump: worlddump.py
- ?: b
- ?: Entity
- ?: EntityLiving
- ?: InventoryBasic
- skeleton: skeleton.py
- ?: Override
- CLAUDE: Prism instance updated to 0.38.1 (2026-10-10) with the dev worldgen config; it has OptiFine HD U E7 and RTG, the dev client has neither
- SKILL: Military base case 6: verdict, accepted
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
- ?: NpcLoadouts
- ?: ClientTickEvent
- ?: Schematic
- ?: Sub
- ?: EntityLiving
- ?: EntityPlayer
- ?: Entry
- ?: ItemStack
- ?: ServerTickEvent
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
- ?: net.decimation.worldgen.city.LcCity
- ?: Item
- ?: LargeSites
- ?: StreetProps
- ?: ObjectZoneList
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
6. `StreetPlan` - 46 edges
7. `NpcLoadouts` - 44 edges
8. `StructureGenerator` - 42 edges
9. `BuildingDress` - 40 edges
10. `CityRefuge` - 38 edges

## Surprising Connections (you probably didn't know these)
- `Survivor camp (about 1 in 12 buildings, one room: lantern, CanFire, bedroll, crates, radio, WaterPallet, barricaded door, note decal, graffiti outside)` --semantically_similar_to--> `survivor_camp()`  [INFERRED] [semantically similar]
  docs/interior_spec.md → tools/make_test_schematics.py
- `Spawn trap: Decimation adds its spawns to biomes existing in its preInit and strips vanilla monsters in its init; our biomes are made in our preInit and copySpawns copies plains lists in postInit` --semantically_similar_to--> `Recurring root cause: integrated server reports side CLIENT`  [AMBIGUOUS] [semantically similar]
  docs/terrain.md → CLAUDE.md
- `VehicleHitHandler (v0.8.1)` --references--> `VehicleHitHandler`  [INFERRED]
  bug.md → dev/src/main/java/net/decimation/fixes/VehicleHitHandler.java
- `run/client/devtest/results.txt: one line per value, PASS / FAIL with the expectation, exit code 1 on a FAIL` --references--> `DevTestResults`  [INFERRED]
  CLAUDE.md → dev/src/main/java/net/decimation/worldgen/devtest/DevTestResults.java
- `Scope fps measured (4x on ak74): picture in picture aiming 37..44 vs view zoom aiming 105..132` --references--> `ScopeTest`  [INFERRED]
  bug.md → dev/src/main/java/net/decimation/worldgen/devtest/ScopeTest.java

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

## Communities (265 total, 112 thin omitted)

### Community 0 - "Deci: Deci"
Cohesion: 0.06
Nodes (15): a, DamageSource, Deci, Block, EntityLivingBase, EntityPlayerMP, IInventory, ObjectZone (+7 more)

### Community 1 - "SurvivorCamp: SurvivorCamp"
Cohesion: 0.06
Nodes (11): CanvasPlan, MilitaryBasePlan, B, Post, SurvivorCamp, Canvas, SurvivorCampPlan, MilitaryBase (+3 more)

### Community 2 - "lctranslate: lctranslate.py"
Cohesion: 0.06
Nodes (49): Converted buildings used LED lamp blocks as floor (FIXED 9 Oktober 2026: 'light' in colour names like light_gray matched the lamp rule), Knowledge index: translation audit tools/lcaudit.py, lcpack content: 290 converted buildings, stairs per district style (local only), Translation audit 9 Oktober 2026: dropped blocks and props by placement over 290 converted buildings; black sandstone is asphalt, laboratory panels, wallpaper, corundum, posts, shelves, seats mapped, Recently done: converted building quality pass (pack rebuilt, LED floor fixed, about 30000 dropped blocks mapped), lc_quality_v0.28.10 shots: 30 interior views after the translator fix, no lamp floors, ceiling redstone lamps; black sandstone stairs now cobblestone, building_columns(), main() (+41 more)

### Community 3 - "SurvivorCamp: SurvivorCamp"
Cohesion: 0.11
Nodes (3): Canvas metadata 0 never turned with a rotated compound (FIXED v0.48.0), Canvas, CityRefuge

### Community 5 - "?: DecimationWorldGen"
Cohesion: 0.10
Nodes (20): Building, cpw.mods.fml.common.event.FMLInitializationEvent, cpw.mods.fml.common.event.FMLPreInitializationEvent, cpw.mods.fml.common.Mod, DecimationWorldGen, Placeholder blocks to Decimation props (sponge, gold, lapis, diamond, emerald, iron, coal, wool and stained clay colours), FMLPostInitializationEvent, FMLServerStartingEvent (+12 more)

### Community 6 - "new_feature: NPC tiers design and result (v0.30.0): tier per armed NPC on first join (gear, gun, health, fire rate, share of player gun damage), stored in entity data, gun synced via data watcher slot 26; bandit light / medium / heavy, soldier camo sets, Soviets as enemy military"
Cohesion: 0.05
Nodes (42): CLAUDE.md v0.30.1 note: spawn egg per NPC tier, CLAUDE.md v0.30.4: cooldown ticks, rocket tiers, config version 2, new tiers last, CLAUDE.md v0.30.0 note: NPC tiers, gun sync via data watcher slot 26, test mode npc, CLAUDE.md v0.31.0: juggernaut tier, Barrett armor piercing, MilitarySpawner, SAFEZONE, Balance v0.31.0: juggernaut, Balance v0.30.4: hit cooldown 0.25 s, RPG NPCs (+34 more)

### Community 7 - "building_design: Procedural building design doc (city blocks, Building v2)"
Cohesion: 0.09
Nodes (46): v0.10.0 to v0.12.3 city blocks, large schematics, city v2 and fixes status, v0.12.2: car wreck long axis is x at 0 degrees (north south 5/3, east west 4/2), v0.15.0: street life (levelled street cross sections, dashed centre lines, street lights, benches, bins, trash bags, facing derived from PropRenderer transform), Finding: rooms have no function (sparse apartment units, empty ground storey units, identical office desk grid on every storey incl. ground, repeated plans on tall buildings, undefined upper shop storey), Apartment slab layout (double loaded corridor, stair core, living part and bedroom per unit), Biome overgrowth (temperate vines and moss, jungle heavy vines, snowy snow layers, dry sand drifts and dead bushes), BlockProp facing transform from deobf PropRenderer (rotate 180 about x, metadata % 4 * 90 about y, then extra rotation; toward the road: road west 4, east 2, north 5, south 3), Procedural building design doc (city blocks, Building v2) (+38 more)

### Community 8 - "BiomeMap: BiomeMap"
Cohesion: 0.07
Nodes (23): decimation-city case 2: the wasteland as the city's military area (v0.54.0), Skill decimation-city: city work order, server and dev test checks, lessons (bridges continue the street), casebook case 1, decimation-city case 1: bridges did not match the roads (v0.45.0 -> v0.45.1), CLAUDE.md v0.45.0 note: rivers through cities only for worlds created since 0.45, dev test bridge, game check as own step, CLAUDE.md v0.45.1 note: bridges are the street over the canal, DC bridge parts dropped, skill decimation-city, CityRivers, Sectors, BiomeMap (+15 more)

### Community 9 - "gunmodel: gunmodel.py"
Cohesion: 0.08
Nodes (8): NpcKind, World, NpcLoadouts, Tier, EntityConstructing, ItemStack, LivingAttackEvent, LivingUpdateEvent

### Community 10 - "StreetDress: StreetDress"
Cohesion: 0.15
Nodes (4): BuildingPlan, StreetDress, StreetLayout, StreetPlan

### Community 11 - "ScopeZoom: ScopeZoom"
Cohesion: 0.08
Nodes (21): v0.28.1: EntityRenderer.cameraZoom zooms world and gun together; ScopeZoom draws the hand itself on RenderHandEvent (vanilla skips it while zoomed), Scope FPS drop: picture in picture second world render, fixed v0.28.0 with view zoom, Report: FPS drop while aiming scopes (fixed v0.28.0..0.28.4), Scope fps measured (4x on ak74): picture in picture aiming 37..44 vs view zoom aiming 105..132, PatchScope: renderScopeView gated behind system property decimation.scope.pip (old scope via pictureInPicture=true in config/deciworldgen_scope.cfg), v0.28.2 / 0.28.3: projective see-through glass (frame copied, mapped by screen position), sight learned per gun + scope + window aspect, copy limited to the glass box, v0.28.4: scopes from overlayFrom (default 4x) hide the gun and draw a black sniper overlay with the scope's reticle texture on the HUD, Javassist snippets must compile against Java 8 signatures (FloatBuffer.flip() through java.nio.Buffer, else NoSuchMethodError in game) (+13 more)

### Community 12 - "CanvasPlan: CanvasPlan"
Cohesion: 0.06
Nodes (12): v0.12.1: whole footprint floor height sampling and dirt fill under schematics, Plan, Schematic, SchematicPlan, Sub, ZoneKind, BORDER, MILITARY (+4 more)

### Community 13 - "InfectedVariants: net.minecraft.entity.Entity"
Cohesion: 0.08
Nodes (16): Decimation NPCs are never hostile to creative players (isHostileTo), Configuration, GunNoise, InfectedVariants, Variant, ItemStack, Smarter zombies remember the shooter 60 s; melee zombie variant; armor per type against zombies, Noise config note: NPCs ignore creative players (+8 more)

### Community 14 - "Surfaces: net.minecraft.block.Block"
Cohesion: 0.10
Nodes (7): Surfaces, PropFacing, Row, PropFacingAccess, B, PropBoxes, net.minecraft.block.Block

### Community 16 - "Building: Building"
Cohesion: 0.07
Nodes (10): Building, Interior, Furnisher, Interior, net.decimation.worldgen.Graded, Ruins, Shell, StoreyPlan (+2 more)

### Community 17 - "BuildingDress: BuildingDress"
Cohesion: 0.19
Nodes (3): BuildingDress, Item, Spot

### Community 18 - "StreetPlan: StreetPlan"
Cohesion: 0.08
Nodes (6): Shape, StreetProps, ZoneKind, StreetPlan, City engine files after the 2026-10-09 split: LcCity layout + StreetPlan, BuildingPlan, LotPlan, EdgePlan, StreetProps, Props of turned street parts turned in partAt (PropBoxes.turn)

### Community 19 - "SafezoneSites: SafezoneSites"
Cohesion: 0.09
Nodes (30): all_names(), attach_offset(), Gun, label(), load(), main(), metrics(), num() (+22 more)

### Community 20 - "hwmap: hwmap.py"
Cohesion: 0.11
Nodes (34): Bug: city edge ramp missed its outer columns (fixed v0.24.4, populate scans cells within EDGE), City edge ramp: 24 wide, rounded corners, nearest cell owns a column, wobbled contours, Skipped giant buildings: casino 276 high, oasis condo top above 250, laboratory 90 deep cellars, City engine open items: railway (v0.44.0) and rivers with bridges (v0.45.0) done; giant buildings still skipped, City superblocks: 2x2 cells, 7x7 building chunks, landmark towers, Lost Cities to 1.7.10 conversion (lc2schem, lctranslate, paste command), Measured prop footprints (footprint mode, tools/footprint.py), Giant buildings that are skipped (casino 276 high, oasis condo above 250, laboratory 90 deep cellars): trim cellars or cap height (+26 more)

### Community 21 - "interior_spec: Building interior and exterior spec (round 2 step 4c, approved 7 Oktober 2026)"
Cohesion: 0.09
Nodes (36): Fix v0.16.1: solid wall cell behind the ladder, no furniture on the 4 cells around it, ladder shaft and stair core (plus 1 block ring) exempt from the collapse, Bug: upper storeys unreachable (ladder popped off, stair core collapsed; fixed v0.16.1), Knowledge index (docs/, interior spec, deobf notes, names.tsv, trackers), Knowledge index entry: docs/roadmap.md holds EVERYTHING planned in one list, v0.16.1: every storey reachable (ladder support, collapse spares the stairs); building audit, prop gallery, catalogue and spec (round 2 step 4a..c), v0.17.0: step 4d.1, doors in every DOOR cell and low debris (docs/interior_spec.md section 8), Audit method: tools/floorplan.py per storey plans with reachability flood fill, plus runClient -Pautotest -Paudit (facade, ground, storey 1, roof of a sample apartment, office, shop); seed 1, Decimation world type, Building quality audit (round 2 step 4a, baseline of v0.16.0 city buildings) (+28 more)

### Community 22 - "StructureGenerator: StructureGenerator"
Cohesion: 0.11
Nodes (12): CityDistrict, Refactor check on both city paths: worlddiff 0, legacy path by moving the lc pack aside, git stash -u, cpw.mods.fml.common.IWorldGenerator, Yard, LegacyStreets, ZoneKind, StructureGenerator, LargeSites (+4 more)

### Community 23 - "?: net.minecraft.entity.EntityLiving"
Cohesion: 0.14
Nodes (13): Block, EntityPlayerMP, Item, net.decimation.fixes.MilitarySpawner, net.decimation.fixes.NpcKind, net.decimation.fixes.NpcLoadouts, net.minecraft.client.Minecraft, net.minecraft.entity.EntityLiving (+5 more)

### Community 24 - "NpcEgg: Override"
Cohesion: 0.14
Nodes (11): cpw.mods.fml.relauncher.SideOnly, ItemStack, NpcEgg, SuppressWarnings, ZombieEgg, BlockHesco, IIconRegister, net.minecraft.client.renderer.texture.IIconRegister (+3 more)

### Community 25 - "Deci: net.minecraft.item.Item"
Cohesion: 0.09
Nodes (9): Magazine, Magazines, NewSights, f, net.minecraft.item.Item, net.minecraft.util.ResourceLocation, ResourceLocation, Our own sight attachments (deci:<name>, sights.py) (+1 more)

### Community 27 - "systems_plan: Systems plan: loot, human NPCs, zombies, world planned together (13 Oktober 2026)"
Cohesion: 0.06
Nodes (35): Decimation's crates gave one item fewer than listed, stacks one short (IntRange half open); FIXED v0.57.0 by PatchLootRolls, PatchLootRolls.java: IntRange.pick (deci/aB/a) inclusive, three jars, Idea 17: crate levels 1 / 2 / 3 with richer loot, behind locked doors first, User choices: every kind split by content type in 4 levels (military L1 magazines, L2 attachments, L3 guns, L4 all), colour band per level, L4 only in the rarest places, lootable NPC body, Four crate blocks (military, police, medical, civilian), metadata level (0..3) x 4 + facing: 16 crates, baked block render, no tile entity, Decimation loot today: 33 blocks, 12 hardcoded lists, 1 .. n-1 items per open (n 2 gives 1), rarity per item lootChance, 900 s refill, Crate levels and NPC loot: design draft (13 Oktober 2026), Section 3.2 lists BUILT as LootPools built-in lists in config/deciworldgen_loot.json; magazines read from each gun; names in docs/references/decimation_items.tsv (+27 more)

### Community 28 - "LootTest: net.minecraft.client.Minecraft"
Cohesion: 0.12
Nodes (11): After 0.56.0 (unreleased): fixes/Loot.open, fixes/LootBody (deciworldgen:loot_body), dev test loot, keepScreen trap, World, Loot, LootBody, InventoryBasic, LootTest, Duplication guard: Loot.closeFor closes loot screens on a body's inventory when it dies or the player is over 8 blocks away (Decimation's container never checks its source), Loot spikes DONE 13 Oktober 2026 (dev test loot, 12 PASS): our inventory on Decimation's loot screen, taken stacks leave the same InventoryBasic, LootBody opens it on right click, goes when emptied, NBT save (+3 more)

### Community 29 - "NoiseTest: NoiseTest"
Cohesion: 0.10
Nodes (20): Bug: gun creeps off screen while aiming (NPC scope glass measured, fixed v0.43.1), Bug: NPCs kept a dead entityToAttack and ignored gunshots (fixed v0.43.2), Bug: enemy NPCs (juggernaut) ignored gunshots, living stale entityToAttack (fixed v0.43.3), v0.43.1 aim drift fix, dev test aimdrift, v0.43.2 stale target fix, aimdrift changes, test sweep, v0.43.3 GunNoise reads revenge target, noise test tier / far / hits, Case 22: gun creeps off screen while aiming a scope (v0.43.1), Case 22 follow up: aim test with gun / sight changes (+12 more)

### Community 30 - "StreetPlan: StreetPlan"
Cohesion: 0.11
Nodes (23): Bug: generated metal shelves invisible (multiblock master never set, fixed v0.12.3), MetalShelfRenderer draws only the master part (returns unless isMaster: stored master position equals own position), setSelfMaster() on every placed multiblock part plus repair on chunk load, v0.12.3 rule: multiblock props (deci.W.a, metal shelves) render only from a master part; anything placing them outside player placement must call setSelfMaster(), v0.16.0: multiblock props generated whole (shelves are 1x1x2 TALL), tools/multiscan.py checks them, supply drops skip columns topped by a prop, Multiblock props (deci.W.*), Load, World (+15 more)

### Community 31 - "?: cpw.mods.fml.common.eventhandler.SubscribeEvent"
Cohesion: 0.16
Nodes (26): mil_compound large test schematic (48x14x48), city_office(), city_shop(), city_street(), civ_gas_station(), civ_house_ruin(), civ_shed(), decay() (+18 more)

### Community 32 - "BuildingPlan: BuildingPlan"
Cohesion: 0.08
Nodes (8): Building, Shape, BuildingPlan, Building, Shape, ZoneKind, Garrisoned, Styled

### Community 33 - "PropFit: PropFit"
Cohesion: 0.16
Nodes (3): Shape, PropFit, PropGeom

### Community 34 - "architecture: Bug tracker (bug.md)"
Cohesion: 0.10
Nodes (31): Bug: bottlecaps not converted to currency (fixed), Bug: humanity never changed from ordinary kills in singleplayer (fixed v0.9.0), Bug: zones never active in singleplayer (partial fix), Recurring root cause: integrated server reports side CLIENT, AntiCheatScanner (deci.aN.a), BackendConnection (deci.aP.a, kryonet), Block break/place protection handlers (deci.aK.a, b), ChatHandler (deci.aK.n, radio chat) (+23 more)

### Community 35 - "DeciBiome: DeciBiome"
Cohesion: 0.09
Nodes (30): NPC tracers flew along the body facing; fixed by sending the target id (PatchTracer), Cause: PacketGunFireEffects carried only the shooter id; the client drew the tracer along getLook() (a mob's body facing), Tracer test (-Ptracer): original 52 tracers mean 133 degrees off the target, patched 42 tracers mean 1.0, max 2.0, NPC ranged attacks call attackEntityFrom directly server side, CLAUDE.md v0.34.0: zombie variants, attribute modifiers trap, BankerTrader (deci.ai.e), FactionHumanEntity (deci.ah.d), Hostile mob entities (deci.ag.*: Bandit, Soldier, Infected) (+22 more)

### Community 36 - "Deci: net.minecraft.item.ItemStack"
Cohesion: 0.09
Nodes (6): Bug: colourful M4A4 rail teeth under a sight (v0.41.0), Case 19: reticles fixed on screen, EOTech too big, HK416 front sight (v0.41.1), IronSights, SightPlacement, SightReticle, net.minecraft.item.ItemStack

### Community 37 - "LcCity: LcCity"
Cohesion: 0.17
Nodes (4): Building, StreetProps, LcCity, Highways

### Community 38 - "CameraViews: CameraViews"
Cohesion: 0.12
Nodes (4): CameraViews, EntityPlayerMP, DevTestUtil, Entry

### Community 39 - "CLAUDE: Knowledge index (docs/, interior spec, deobf notes, names.tsv, trackers)"
Cohesion: 0.09
Nodes (28): Case sensitive volume extraction, CFR --caseinsensitivefs true silently drops colliding classes, CLAUDE.md project guide, Decimation.jar (obfuscated Forge 1.7.10 mod jar), deobf/ readable reference tree (decompiled with readable names, read only), Graph update workflow (graph_update.py prepare/finish, one agent per chunk), Knowledge maintenance rule (write findings to docs, bug.md, new_feature.md, maps, graph), Map file case trap (p_deci_aK.tsv equals p_deci_ak.tsv on APFS) (+20 more)

### Community 40 - "Shell: net.minecraft.block.Block"
Cohesion: 0.12
Nodes (4): Ruins, Shell, Worldgen code map: building package parts (Shell, StoreyPlan, planners, Furnisher, Surfaces, Interior, Ruins, Yard), Plan

### Community 41 - "lcstudy: dcinventory.py"
Cohesion: 0.12
Nodes (21): chunk_biomes(), chunk_blocks(), chunks(), main(), Palette, {section Y: biomes[4,4,4] (y, z, x) global ids}., Entries packed without spanning longs (1.16+)., Global name -> id for a whole survey. (+13 more)

### Community 42 - "?: cpw.mods.fml.common.eventhandler.SubscribeEvent"
Cohesion: 0.09
Nodes (17): BottlecapHandler (deciworldgen), Railway tunnels flooded with standing water and lava (fixed v0.44.0), cpw.mods.fml.common.eventhandler.SubscribeEvent, Decorate, BottlecapHandler, VehicleHitHandler, ServerTickEvent, TerrainEvents (+9 more)

### Community 43 - "terrain: Decimation world type doc (terrain, 0.14.0)"
Cohesion: 0.09
Nodes (20): DeciBiome, DecimationBiomes, DecimationWorldType, Override, worldmap_seed1_v0.14.png: top down map of seed 1, Decimation world type (city sectors, military, suburbs, overgrown forest, rivers stopping at city edges), Biome names carry AmbientMusicPlayer keywords (forest, river, plains, hills, decimated, irrated), BiomeMap.biomeAt rules (seed only: city and military biomes exactly on sector squares, suburbs warped up to 56, dead wilderness within about 100 blocks, overgrown further out, no villages), Fixed biome ids 110..118 (Decimated City, Suburbs, Irradiated Military Zone, Decimated Plains, Burnt Forest, Overgrown Plains / Forest / Hills, Murky River) (+12 more)

### Community 44 - "StoreyPlan: StoreyPlan"
Cohesion: 0.14
Nodes (4): ApartmentPlanner, OfficePlanner, ShopPlanner, StoreyPlan

### Community 45 - "EdgePlan: EdgePlan"
Cohesion: 0.10
Nodes (3): EdgePlan, ZoneKind, Graded

### Community 46 - "new_feature: Current state and pending decisions"
Cohesion: 0.13
Nodes (8): ServerTickEvent, Post, SafezoneSites, Site, SiteList, Load, Save, Unload

### Community 47 - "city_engine: City engine: Lost Cities style cities from converted DeceasedCraft content"
Cohesion: 0.15
Nodes (25): apply(), bmodel(), cells(), evaluate(), gl_calls(), gl_ops(), java_model(), main() (+17 more)

### Community 48 - "DevTestResults: ScopeTest"
Cohesion: 0.12
Nodes (7): City street after the cache (-Pcityfps): props 5.5% of the client thread, chunk drawing 15%, chunk rebuild 12.7%; props no longer the bottleneck, New dev test: DevTestMode subclass, register its name in DevAutoTest.mode(), record with DevTestResults.value / check, screenshots with DevTestUtil.screenshot, CityFpsTest, DevTestMode, DevTestResults, EntityPlayerMP, ScopeTest

### Community 49 - "Ruins: .unit()"
Cohesion: 0.11
Nodes (6): ClientTickEvent, LocalStats, StatsTest, GuiScreen, net.minecraftforge.common.config.Configuration, net.minecraftforge.event.entity.living.LivingDeathEvent

### Community 50 - "Wasteland: Wasteland"
Cohesion: 0.15
Nodes (4): Building, Load, Wasteland, WastelandTest

### Community 51 - "CityRefugePlan: CityRefugePlan"
Cohesion: 0.09
Nodes (3): CityRefugePlan, Residents, net.decimation.worldgen.FixedBase

### Community 52 - "architecture: Obfuscation map (package to meaning)"
Cohesion: 0.12
Nodes (26): Bug: large ammo crate NPE (dead field avk), Obfuscation map (package to meaning), 8 agent deobfuscation naming pass, Subsystem taxonomy (core, proxy, network, loot, zone, ...), Decimation architecture notes, DeciConstants (deci.Q.c, GUI ids), DecimationMod (deci.a.b, @Mod entry), IntRange (deci.aB.a, off by one rolls) (+18 more)

### Community 53 - "NpcShots: NpcShots"
Cohesion: 0.11
Nodes (14): Bug: NPC machine guns fire at double rate (v0.39.2), CLAUDE.md v0.32.0 note and the shootAt test trap, Decimation runs guns at 1.3x listed rpm (GunStats), maxBurstRpm 600 (deciworldgen_npc.cfg npc_fire), ArmorGunfireHandler, NpcKind, NpcLoadouts, NpcShots (+6 more)

### Community 54 - "worldgen: Lot grading (city yards, 0.13.0: Graded plans own the 26x26 lot, Slices grades every lot column before writing)"
Cohesion: 0.16
Nodes (26): .bmodel is plain text Techne style code (earlier binary note was wrong), addChild is vanilla direction: parent.addChild(child) (old note said reversed; corrected 2026-10-10), Gun specific .bmodel header fields (mOff, sPos, flamePos, lhPos, rhPos, ejectPos, Scale), Unmapped .f(n) builder call (likely spread or sway), Fire mode enum deci.ay.e.a (SINGLE, AUTO, BURST, PUMP, BOLT), Gun registration call new i(...).f().am(), Genuinely new model recipe (needs Techne), Reskin an existing weapon recipe (fast path) (+18 more)

### Community 55 - "Graded: net.minecraft.world.World"
Cohesion: 0.11
Nodes (7): Test arena (DevTestArena): flat stone floor at y 150 around (8, 8), every NPC test mode builds and cleans it first; retry Decimation-refused spawns, Props, CityDistrict, DevTestArena, Graded, arena_v0.34.0: all NPC test lineups and shots on the test arena, net.minecraft.world.World

### Community 56 - "Areas: Areas"
Cohesion: 0.14
Nodes (12): Area, Areas, Load, Save, Unload, ZoneKind, Kind, BASE_CLAIM (+4 more)

### Community 57 - "CLAUDE: Testing without the user (devtest.py first choice, servertest, worldcheck, worlddiff, autotest flags)"
Cohesion: 0.13
Nodes (19): Dedicated server crashed at start since v0.42.2: Deci.homeMenu loaded GuiScreen (fixed v0.44.0), Wall scan reproduction on seed 1 (2 of 33 buildings, one real: b4_4_2), Autotest screenshot modes (default 3 street views, -Paudit building audit, -Pgallery every Decimation block 3 per shot, -Ponly= re-shoots single views; peaceful, mobs removed, camera locked per tick, fov / gamma restored), Autotest ends with 3 city street screenshots (dev/run/client/screenshots/autotest_<n>.png: along the street, street light side on, across); read them to check visuals instead of asking the user; -Ddeciworldgen.autotest.views=false skips them, Autotest forces pauseOnLostFocus false, Damage checks must run after 60 server ticks (spawn invulnerability), Testing without the user (devtest.py first choice, servertest, worldcheck, worlddiff, autotest flags), DevPregen (+11 more)

### Community 58 - "city_dressing: City dressing: Decimation props through the whole city (v0.50.0)"
Cohesion: 0.10
Nodes (25): Trap: BlockChest subclasses (footlocker) re-aim themselves on placement, Skill decimation-city-decay, Skill decimation-city-interior, Skill decimation-city-street, Skill decimation-city-vehicles, Skill decimation-worldgen (index of worldgen skills), Dev test facing (prop at meta 2..5, shots south / north), City dressing: Decimation props through the whole city (v0.50.0) (+17 more)

### Community 59 - "CLAUDE: Javassist bytecode patcher"
Cohesion: 0.08
Nodes (25): CLAUDE.md v0.42.0 note: military bases, worldgen/military, milbase dev test, mil_ schematics no longer placed, CLAUDE.md v0.42.3 note, Military bases accepted by the user on v0.42.4 (11 Oktober 2026), Military bases design doc: US FOB style bases of our own in three sizes (COP 50x58, FOB 78x84, large FOB 112x118), Real element to Decimation block mapping (HESCO = BlockMilitaryBarrier, T-wall = BlockBarrierTall, jersey = BlockBarrier, sandbags, wire, crates, radios, wrecks; tents smooth sandstone), Real FOB reference (sources): concertina, HESCO MIL7 walls, T-walls, guard towers, ECP with serpentine and search area, TOC, B-hut LSA, DFAC, aid station, ASP, fuel point, motor pool, helipad, burn pit, duck and cover bunkers, Military base status v0.42.0: three sizes built and photographed, two seed 1 COPs placed in real worldgen, mil_ test boxes retired, waiting for user review, Military base test: devtest --live milbase (sizes, turns, seed, points; points=none lists refused modules), servertest seed 1 pregen=-64,448,9 (+17 more)

### Community 60 - "LcContent: LcContent"
Cohesion: 0.11
Nodes (6): com.google.gson.JsonArray, com.google.gson.JsonObject, Condition, Building, LcContent, Set conditions: when kinds / storey / floors

### Community 61 - "ZoneStore: ZoneStore"
Cohesion: 0.13
Nodes (11): Load, ObjectZone, ObjectZoneList, Save, ServerTickEvent, Unload, ZoneKind, ZoneStore (+3 more)

### Community 63 - "NpcTest: .batches()"
Cohesion: 0.16
Nodes (22): anib(), bmodel(), build(), fnum(), icon(), layout(), numbered(), paint() (+14 more)

### Community 64 - "bug: Report: FPS drop in prop dense areas (line of sight cached v0.28.6, model drawing remains)"
Cohesion: 0.14
Nodes (19): Baking static props into chunk meshes (prop textures into the block atlas, both model formats to quads): full fix for open views, not started, Launch waited 5 s for the dead Decimation backend (kryonet hardcoded 5000 ms); PatchBackend, LineOfSight.canSeeTileEntity (deci.a.c$a.a): 8 rays from the eye to the render box corners, PatchPropCulling step 3 (v0.28.6): line of sight answers cached until the player or entity moves 0.3 blocks (props 1.0..1.3 s, entities 0.15..0.18 s); prop renderer share 21% -> 9%, Prop dense FPS drop: line of sight ray casts 76% of prop rendering, cached in v0.28.6; model drawing remains, Report: FPS drop in prop dense areas (line of sight cached v0.28.6, model drawing remains), Prop fps measured (-Pprops): 225 props in view 12..14 fps vs empty 28..29; 76% of PropRenderer time in canSeeTileEntity ray casts, TileEntityProp.getRenderBoundingBox: bare 1x1x1 cell for 36 of 72 props, never rotated (+11 more)

### Community 65 - "safezone: Safezone sites doc: survivor camps (references, layout, residents, placement, test), fortified city building next"
Cohesion: 0.09
Nodes (23): Bug: camp guard flying over the wall (static guard spawned before its floor); keeper waits for populated chunks and puts residents back, fixed v0.47.2, Bug: survivor camp NPCs missing (test camp emptied by the test; Decimation's trader reset is dedicated server only), fixed v0.47.1, Military base skill case 7: survivor camp first build (clashes, outhouse head room, fence gates in walkcheck), Military base skill case 8: the npc is missing (test cleanup, trader reset, awning front colour), Military base skill case 9: flying guard, retaliation removed, entry sign, cosmetic props, CLAUDE.md v0.47.0: survivor camps, CanvasPlan shared, SafezoneSites, dev test camp, CLAUDE.md v0.47.1: camp people kept, dedicated trader reset respawn, camp command, CLAUDE.md v0.47.2: no provocation, keeper populated check and put back, sign, props (+15 more)

### Community 66 - "prop_catalogue: Decimation prop catalogue (look, size and facing of every deci: block, from the prop gallery)"
Cohesion: 0.12
Nodes (23): worldcheck World.registry() maps block names to ids from level.dat (BlockWreckage1..5 = 176..180, id >= 256 is not a mod block test), BlockRegistry (deci.aD.c / g), Finding: no doors anywhere, only gaps (14 Decimation door blocks unused), Interior prop inventory (275 deci: blocks from World.registry(); no toilet, sink, bath, sofa, bed or fridge props), Finding: no story details (barricades, skeletons, body bags, blood decals, notes, graffiti, survivor camps, looted crates all exist and are unused), DeceasedCraft flats: 6 to 8 small rooms (3x4..5x5), density 0.35, Apartment unit rooms: living room (sofa of stairs facing FlatscreenTV), kitchen counter run (slabs, cauldron sink, ElectricBoxBin fridge, furnace oven, WashingMachine), bedroom (vanilla bed head to wall), tiled bathroom, studio flat, Door decay: 25 to 50% missing, a few left open (meta bit 4), one barricaded in the most decayed buildings (+15 more)

### Community 67 - "FacingTest: DevTestMode"
Cohesion: 0.12
Nodes (6): CampCommandTest, FacingTest, PlacesTest, ZonesTest, DevTestMode, Dev test campcmd: /deciworldgen camp on real ground, 20 residents

### Community 68 - "Deci: net.minecraft.entity.player.EntityPlayer"
Cohesion: 0.22
Nodes (3): NpcLoadoutsAccess, NpcTest, net.minecraft.entity.player.EntityPlayerMP

### Community 69 - "FurnitureSets: FurnitureSet"
Cohesion: 0.15
Nodes (5): Palettes, Style, Entry, FurnitureSet, FurnitureSets

### Community 71 - "furniture_sets: Furniture sets doc: data driven JSON furniture groups, user editable"
Cohesion: 0.10
Nodes (22): v0.20.0 furniture sets, wall lining, corner doors, one sided corridors, live loop, DeceasedCraft interiors fully authored per storey, no procedural rooms, Furniture sets doc: data driven JSON furniture groups, user editable, In-game capture: pos1/pos2, capture set/part, Set format: layers (floor, +1, under ceiling), row 0 against the wall, palette with face/type, rooms slot, weight, known.txt: unedited old built-in copies are updated (tools/asset_hashes.py), Named palettes and weighted styles for sets (base / style keys), Placement: seeded weighted order, every wall and offset, free cells off walkway, back against wall, no full height piece over a window (+14 more)

### Community 72 - "Garrisons: StructureData"
Cohesion: 0.16
Nodes (4): Garrisons, Load, StructureData, net.minecraft.world.WorldSavedData

### Community 73 - "porting: Study: DeceasedCraft city buildings (DCTweaks jar Lost Cities data)"
Cohesion: 0.10
Nodes (22): DeceasedCraft content catalogue: 79 Lost Cities buildings, city parts, apocalypsenow structures, disabled vanilla structures, DeceasedCraft 79 building types and 5 district city styles, Study: DeceasedCraft city buildings (DCTweaks jar Lost Cities data), DeceasedCraft storey is 6 high (4 air) vs our 5, Builders: separate ceiling tiles with light panel grid and vents, Study: hand-built Decimation maps (USA coast, Decicraft, Cloverfield, world-e161), Builders: decay as dirt/leaves/water/cracked glass on intact shells, Builders: furniture in rows and islands (waiting rows, cubicles, shelf aisles) (+14 more)

### Community 74 - "gun_style_guide: Decimation gun style guide: study of all 98 shipped guns (look and construction rules for our own guns)"
Cohesion: 0.10
Nodes (22): study.py gaps: ours vs Decimation guns of the category, metric by metric (median, q10..q90, outside marks), incl. part sizes relative to length, Gun animation templates: Fire 2 frames slide only, Reload1 57 frames keyframes every 5 (SWITCH 20, LOAD 40, TRYBOLT 50), Rack 19 frames Hand 1, SlideBack static in 92 of 98, Gun attachments: NBT sightAttach / barrelAttach / gripAttach, category match or all, fixed offsets in model units, barrel follows flamePos, foregrip misplaced even on M4A4 [inferred], Construction rules: small shape boxes, layered receiver panels, grooves as thin raised plates, octagons from 3 parts per slice, skewed segments for grips and curved mags (addChild), chamfered edges, thin features thin, docs/references/decimation_guns.tsv: per gun parts, shaped %, rotated %, length / height / width, texture size, Where gun detail goes: receiver 30 to 70% of length holds half the parts, 3 of 4 parts in the top two fifths; first person shows top, right side, rear, First person gun look (dev test gunview): seen from rear, top, right; short guns are their rear section; MAC-10 stock plate fills the view, Gun icon style: 32x32 side silhouette, muzzle right, dark tones, 1 px black outline; make from the model side render (+14 more)

### Community 75 - "bug: Bug tracker (bug.md)"
Cohesion: 0.14
Nodes (21): Bug tracker (bug.md), Bug: ClassCastException deci.a.c to deci.a.e, Bug: CustomSkinLoader coremod crash, EntityFallingSupplyDrop turns into a block only on a replaceable cell (flowers, saplings, tall flowers are not), Bug: graded yard sand fell into caves, hole next to a building (fixed v0.13.0), Bug: loot GUI never opens, Bug: loot never worked in singleplayer, Bug: no supply drops in singleplayer (fixed v0.9.0) (+13 more)

### Community 76 - "NoiseTest: NoiseTest"
Cohesion: 0.10
Nodes (21): Open bug: HUD and menu kill / death counters read the dead backend profile cache; Player Kills should become Human Kills (NPC humans and players) from local data, Open bug: with a real account the main menu shows an offline banner and grey Play offline (ClientState.bv off, dead backend) instead of Play on green, Fixed v0.42.2: local Human Kills / Infected Kills / Deaths (LocalStats) on HUD and menu, labels by PatchMenuStats, session flag valid (green Play), Open bug: military jeep / tank wreck blocks have hardness 0 and break with one punch (reported 10 Oktober 2026), Fixed v0.42.5: military wrecks hardness 10 (WreckHardness), 50 s by hand, CLAUDE.md v0.42.2 note and PatchMenuStats in the patch list, CLAUDE.md v0.43.0 note: gunshot noise, Roadmap: gunshot noise draws zombies and NPCs (unsuppressed shots), later (+13 more)

### Community 77 - "CLAUDE: Javassist patches go into three jars (patched, dist, dev base) plus Prism; sources in tools/patches (PatchSwing, PatchPropCulling, PatchScope, PatchTracer, PatchBackend)"
Cohesion: 0.12
Nodes (17): tools/patches/PatchSwing.java, Fix: SmoothSwingThread busy wait (PatchSwing 4 ms sleep), dev/libs/Decimation-base.jar (patched jar minus our classes), Decimation.jar.original.bak (hash checked backup), Decimation.jar.patched (deliverable), In place jar entry update (zip -u, never rebuild), Patch from ORIGINAL classes only after checking the target class is identical in the patched jar, Javassist patches go into three jars (patched, dist, dev base) plus Prism; sources in tools/patches (PatchSwing, PatchPropCulling, PatchScope, PatchTracer, PatchBackend) (+9 more)

### Community 78 - "devtest: tools/devtest.py: dev test modes in ONE game launch, results file, contact sheet per mode (first choice since 2026-10-09)"
Cohesion: 0.13
Nodes (17): run/client/devtest/results.txt: one line per value, PASS / FAIL with the expectation, exit code 1 on a FAIL, tools/devtest.py: dev test modes in ONE game launch, results file, contact sheet per mode (first choice since 2026-10-09), Live loop: hotswap code, reload sets, rebuild in place, no restart per change, Live editing: /deciworldgen reload + rebuild, -Photswap + tools/hotswap.py (method bodies only), Roadmap: code and tools, Live dev test mode [idea]: keep the dev game open and start test modes over a localhost port, Roadmap code and tools: port to modern Minecraft PARKED (user 13 Oktober 2026), Split LcCity (1225 lines) and StructureGenerator (861 lines) into street, building, lot and edge plan files; old procedural city apart (+9 more)

### Community 79 - "new_feature: Current state and pending decisions"
Cohesion: 0.14
Nodes (13): Current state and pending decisions, v0.14.0: Decimation world type (own biome map on vanilla terrain generator, flat cities on the exact city sectors, Sectors now shared, autotest runs on this type), Rotation, Schematic, deciworldgen second @Mod (net.decimation.worldgen), Dropped companion mods (Ruins, ezWastelands, GeneratorMods), Generator crash hardening (catch all, self disable after 3 errors), FML IWorldGenerator per chunk hook (+5 more)

### Community 80 - "LotPlan: LotPlan"
Cohesion: 0.11
Nodes (4): Shape, ZoneKind, LotPlan, FixedBase

### Community 82 - "StreetProps: Slices.java"
Cohesion: 0.13
Nodes (6): DeadTree, Override, DeciBiome, Override, net.minecraft.world.gen.feature.WorldGenAbstractTree, net.minecraft.world.gen.NoiseGeneratorSimplex

### Community 83 - "?: cpw.mods.fml.relauncher.SideOnly"
Cohesion: 0.14
Nodes (20): add(), acog(), eotech(), icon(), oct_y(), oct_z(), Gun space -> attachment file space (minus renderAttachments' translate)., 68 MOA ring, centre dot, four ticks across the ring (photo 62), red. Drawn… (+12 more)

### Community 84 - "worldcheck: worldcheck.py"
Cohesion: 0.15
Nodes (15): chunks(), fix_chunk(), log_report(), main(), world_report(), chunks(), main(), prop_names() (+7 more)

### Community 85 - "bug: Feature tracker (new_feature.md)"
Cohesion: 0.13
Nodes (20): Bug: armor buff ignores NPC gunfire (fixed), DamageSource split: gunDeci (player) vs human/turret (NPC), Helmets give no gun protection (slot 3 excluded), Helmet counts on headshots only (v0.9.1: aim line for player guns, 20% random for NPC), Proposed LivingHurtEvent gunshot damage unification, Bug: structures built on ocean floor, Two gunshot DamageSource identities (gunDeci player, human NPC), DeciDamageSources (deci.aD.h) (+12 more)

### Community 86 - "zones: Zones doc: Decimation's zone system end to end and what ZoneRules makes work in singleplayer"
Cohesion: 0.11
Nodes (20): Bug: zone rules dead in singleplayer (server proxy handlers), fixed v0.46.0, CLAUDE.md v0.46.0: zones work in singleplayer, docs/zones.md, Dev test mode wasteland (WastelandTest), City dressing: the wasteland as the military area (v0.54.0), Radiation zones: irradiated biome, crash and blast sites, leaking labs and bunkers, the wasteland city, docs/wasteland.md: the wasteland district as the city's military area (v0.54.0 to v0.56.0), Wasteland checks: seed 1 server, dressbalance, propclash 0 from the rules, dev test wasteland all PASS, DeceasedCraft legacy deadzone checked: 23 % military buildings with military loot, rest industry (+12 more)

### Community 87 - "city_engine: City engine: Lost Cities style cities from converted DeceasedCraft content"
Cohesion: 0.12
Nodes (17): v0.25.0 highways, v0.26.0 parks / street scenes / fronts, v0.27.0 district Lost Cities street parts, Shape, FixedBase, City engine: Lost Cities style cities from converted DeceasedCraft content, City decor: parks on open lots, street scenes (pack fountains), building fronts on the street side, City districts: wasteland next to military sectors, current beta weighted, Fronts: building chunk beside a straight street gets a district front part (FRONT_CHANCE 0.5), resolved lazily at write time, Rules taken from Lost Cities source: street surface at G, ground floor at G, cellars below, stairs at G + 1 toward the higher neighbour (+9 more)

### Community 88 - "Capture: Capture"
Cohesion: 0.21
Nodes (3): Capture, JsonArray, JsonObject

### Community 90 - "WorldClock: WorldClock"
Cohesion: 0.12
Nodes (11): v0.57.0: loot spikes, shared core (WorldClock, LootPools, Protection, Areas), dev tests core / items, PatchLootRolls, Event, DAWN, DAY, DUSK, NIGHT, ServerTickEvent, Unload (+3 more)

### Community 91 - "CLAUDE: Testing without the user (devtest.py first choice, servertest, worldcheck, worlddiff, autotest flags)"
Cohesion: 0.19
Nodes (7): Plan, Schematic, Sub, LargeSites, Base layout: perimeter rings, ECP south, HESCO wall with towers, ring and main road, TOC north, LSA west, logistics east, greedy placer for the rest, light decay, Military sector sites always build our US FOB style bases (LargeSites.militaryBase); mil_ schematics no longer placed since v0.42.0, StructureGenerator

### Community 92 - "bug: Bug: armor buff ignores NPC gunfire (fixed)"
Cohesion: 0.14
Nodes (17): Converted city props into walls / floating / facing walls (FIXED v0.49.0), Thin props passed every overlap check (FIXED v0.50.0), Case 2: converted city props (v0.49.0), Lesson: converted content needs the drawn size check, fix by rule at load, Dev test cityprops (CityPropsTest): fresh city cell, shots of fitted props, Props fitted to their drawn size (v0.49.0, city/PropFit), propclash fixes: trapdoor not a door, walkway only, plates flat, glass view, worlddump.py: props of a generated world for propclash (+9 more)

### Community 93 - "hk416: hk416.py"
Cohesion: 0.12
Nodes (18): CLAUDE.md v0.42.1 note and prop_placement.md index entry, Military base v0.42.1: deciworldgen:hesco, locked TOC door with keycard, gravel ground, sandbag ASP, props from drawn boxes, 0 clashes; tile entity props 129/381/571, Performance 6: props used as building material (BlockMilitaryBarrier HESCO, a tile entity per cell) replaced by deciworldgen:hesco; tile entity props per base 1020/2007/2902 -> 129/381/571, BlockMilitaryBarrier is a prop (tile entity per cell), not a plain block: never as bulk building material, Vehicle wreck props (BlockWreckage1..5 cars, BlockTruckWreckage1..6, police cars, military jeep / APC / helicopter, mech; cars long along z at meta 3), Prop checks: Canvas.validateProps removes and logs clashes; tools/props/propclash.py on block dumps (WALL PROP DOOR FACE MOUNT FLOAT), Prop placement doc: how Decimation props are drawn (PropRenderer and ~75 own renderers, per renderer metadata tables, model offsets), facing, drawn sizes, rules, checks, Prop facing: placer rule front 2 E 3 S 4 W 5 N (checked props); exceptions big military radio (90 clockwise), military wrecks (long along z at 2/3), wall mounted keycard / wall flag / junction box (+10 more)

### Community 94 - "SKILL: decimation-gun skill revision casebook: 8 cases (boxy look, aim centre, suppressor floating, suppressor low / threads, gradation, stock in sight picture, icon halo, test traps) with symptom, cause, code location, fix, check, evidence"
Cohesion: 0.14
Nodes (18): Skill casebook case 10: UMP9 as a variant of the UMP45 (how, magazine anchor, registration, checks), Skill case 12: UMP9 accepted without revision (variant route), Skill case 13: variants with many replaced parts (drop by position, fit to the kept receiver, colour twins by recolouring), Case 15: MAC-10 parts not seated, contact check cleanup (v0.39.3), Case 20: verdict, sights accepted (v0.41.1), Lesson 15: every part sits on its host, sights like the real gun from behind, Lesson 17: iron sights in the defaultScopeModel group, Lesson 18: shorter variant scaled from the base gun (+10 more)

### Community 95 - "DevAutoTest: DevAutoTest"
Cohesion: 0.18
Nodes (6): ClientTickEvent, Command, DevAutoTest, ItemsTest, IntegratedServer, net.decimation.worldgen.devtest.DevTestMode

### Community 96 - "LootPools: LootPools"
Cohesion: 0.23
Nodes (6): Entry, FileData, ItemStack, LootPools, Pool, net.minecraft.inventory.IInventory

### Community 97 - "ZoneRules: ZoneRules"
Cohesion: 0.16
Nodes (5): ObjectZoneList, ServerTickEvent, ZoneRules, net.minecraftforge.event.entity.living.LivingAttackEvent, PlayerLoggedInEvent

### Community 98 - "Furnisher: Furnisher"
Cohesion: 0.22
Nodes (4): Facing, Furnisher, Entry, net.decimation.worldgen.sets.FurnitureSet

### Community 99 - "SKILL: BuildingDress.java"
Cohesion: 0.13
Nodes (13): Bug: small lab read as an office; barricades at every inner door (FIXED v0.54.0), decimation-city case 3: wasteland weak spots (v0.54.1), decimation-city case 4: lot scenes that did not tell their story (v0.54.2), decimation-city case 5: edge, doors, locked wasteland (v0.55.0), decimation-city case 6: wasteland city apart, bunker, ramps, bridges, stations (v0.56.0), decimation-city lesson 9: a scene is the key prop plus its story (ground, debris, cordon, layout from the street side); huge drawn boxes take the cells under them, decimation-city lessons 10 to 12: fade city material into land, flood fill entrances, never vanilla doors, decimation-city lessons 13 to 16: read the real shape, ring needs new world flag and cache, keep the street whole, random checks accept absence (+5 more)

### Community 100 - "worldgen: World generation doc (deciworldgen)"
Cohesion: 0.22
Nodes (17): Ladder at (W-2, L-2) hangs on a back wall cell that can be a window, a decay hole or not yet written (next population window); a block update pops it off, Bug: city buildings missing a whole wall at sector borders (fixed v0.11.1), Adding community schematics (prefix, folder, full restart, new chunks only), Cell grid (4x4 chunks, one small schematic or one city block), Large schematics (up to 120x120, per site chance, placed inside the site), Safe population window (chunk cx,cz writes only [cx*16+8, cx*16+23]), Filename prefix pools (civ_, city_, mil_, untagged = any sector), MCEdit/WorldEdit .schematic format (no Sponge .schem or .litematic, TileEntities ignored) (+9 more)

### Community 101 - "?: Sectors.java"
Cohesion: 0.28
Nodes (4): StreetProps, net.decimation.worldgen.Plan, net.decimation.worldgen.StructureGenerator, World

### Community 104 - "CLAUDE: tools/build.py real javac pipeline"
Cohesion: 0.15
Nodes (14): deobfuscation_data-1.7.10.lzma notch to SRG mapping, dev/ RetroFuturaGradle workspace (GTNH ExampleMod1.7.10 template), Compile only shim for Forge binpatch members (tools/shim_src), tools/build.py real javac pipeline, MCP readable member names in dev workspace, SpecialSource notch to SRG remapping, SRG member names (no reobfuscation step), DecimationVoiceChat.jar required or menu shows Update instead of Play (+6 more)

### Community 105 - "ideas: Ideas for later (user, 12 Oktober 2026)"
Cohesion: 0.15
Nodes (15): Bug: floating district name showed _ (FIXED v0.52.1), ClanTurfLayer (deci.M.l): floating turf name over the turf's middle, Dev test places, Ideas for later (user, 12 Oktober 2026), Ideas planned together 13 Oktober 2026 in docs/systems_plan.md, Place names: districts and bases through Decimation's turf banner (v0.52.0), City names on entering: [district] - [number], More crate / loot block types (industrial, civilian) (+7 more)

### Community 106 - "SightPlacement: .draw()"
Cohesion: 0.22
Nodes (14): desc_params(), ident(), is_obf_member(), load_classes(), main(), norm_desc_type(), norm_src_type(), params_match() (+6 more)

### Community 107 - "CoreTest: CoreTest"
Cohesion: 0.29
Nodes (3): Listener, CoreTest, ItemStack

### Community 108 - "performance: Performance doc: tools (gunperf, census, perfcheck, JFR via jcmd), findings, checklist"
Cohesion: 0.18
Nodes (14): Skill casebook case 11 and lesson 14: verify performance for every gun; the 1 fps report was a corrupt chunk and infected pathing, Angelica in the dev client (-Pangelica, Decimation jar without its unused fastutil): starts but draws no far terrain on this Mac (GL 2.1), not usable, Lag checklist: perfcheck log, perfcheck world, census growth, JFR, gunperf vs control, fix and measure again, City view lag: first views were wrong (arena floor / clouds); valid view: props cost ~18% (34 vs 41 fps), the rest is city blocks and Mac heat, Dev world entity explosion: region slot (-1,-6) held chunk (-4,-1), entities doubled each save / load to 40 730 with a Forge stack trace per tick; fixed with fixchunk + census kill, Guns are not the cause: dropped / held fps of Uzi, UMP45, MAC-10, UMP9 equal to baseline, Decimation infected ran a full path search every tick on server and client plus a 40x40 horde scan: ~60% of both threads; PatchInfectedAI: fps 21 -> 35, Java 25 + lwjgl3ify still gets an OpenGL 2.1 context on this Mac: Angelica cannot help on macOS; Prism expected the same (+6 more)

### Community 109 - "Shell: Shell"
Cohesion: 0.18
Nodes (12): Case 14: HK416 floating stock pieces and 2 pillar sight (v0.39.1), Lesson 16: base parts reading past textureHeight wrap (M4A4 rear sight), Variant texture trap: M4A4 rear sight reads rows past textureHeight 32, black_part(), build(), dropped(), lower_bottom(), y of the lower panel's bottom edge at x (the wedge's front face is collapsed to… (+4 more)

### Community 110 - "city_engine: City highways: seed based network between city regions, open / bridge / tunnel chunks, deck at 64, side ramps"
Cohesion: 0.16
Nodes (13): CLAUDE.md v0.29.0 note: highway polish, pregen to check far away L links, CLAUDE.md v0.44.0 note: underground railway, lcpack --rails, springs denied near rails, rail dev test, server fix, Highway chunk kinds: TUNNEL (median 6+ above deck), BRIDGE (water or ground 2+ below, stone brick pillars), OPEN; stored as hw_X_Z, Highway L links (v0.29.0): isolated city region joined to its nearest diagonal city (at most 2 regions) by an L of the region lines; seed 1: 10 links, city groups 29 -> 19, Highway polish v0.29.0: hedges on crossing parts facing open land, no tunnels in city edge bands, side ramps beside bridges cut land above the deck only, Highway side ramps beside OPEN chunks (deck to natural height over 3..8 blocks, shared LcCity.reshape), City highways: seed based network between city regions, open / bridge / tunnel chunks, deck at 64, side ramps, City railway v0.44.0: underground subway on the highway lines at y 46, a station per city, tunnels between cities, buffer ends, crossings (+5 more)

### Community 112 - "RailTest: RailTest"
Cohesion: 0.27
Nodes (3): RailTest, Dev test rail: fresh station, 48 rails, no liquid, street to platform walk, shots of every end, lc_rail_v0.44.0 shots: entrance plaza, lit platform, stairs, shaft, tunnel, four line ends, crossing

### Community 113 - "bug: worlddiff.py"
Cohesion: 0.18
Nodes (12): Bug: building base height depends on chunk generation order (Slices.decideBase), Bug: vehicles destroyed in one hit (fixed v0.8.1), VehicleHitHandler (v0.8.1), Rule: new code adds an accessor to Deci, never calls deci.* directly (all ~60 obfuscated uses migrated 2026-10-09, seed 1 world 0 blocks differ), VehicleEntity (deci.ad.e) and parts, checks: vehicle arrow (arrow and snowball do not pick up), Arrows picked up empty vehicles (FIXED v0.48.1), Open bug 3: building base height depends on chunk generation order (low impact) (+4 more)

### Community 114 - "SupplyDropScheduler: .onServerTick()"
Cohesion: 0.23
Nodes (4): SupplyDropScheduler.drop skips a candidate column whose top block has a tile entity (prop, chest, car) and tries the next of its 12 random positions, ServerTickEvent, SupplyDropScheduler, net.minecraft.server.MinecraftServer

### Community 115 - "PropsTest: TracerTest"
Cohesion: 0.21
Nodes (4): Dev test modes: checks (fresh seed 1 world: zones, vehicle, humanity, prop box, bottlecaps, armor, helmet, supply drop), views (-Paudit / -Pgallery / -Pfootprint / -Pstudy / -Pflats / -Psets), scope, tracer, props, cityfps, PropsTest, EntityLiving, TracerTest

### Community 116 - "DevAutoTest: DevAutoTest"
Cohesion: 0.19
Nodes (5): Live dev test mode: tools/devtest.py --live [--swap] [--stop]; game stays open (-Plive, DevTestLive on 127.0.0.1:25599), ready in about 26 s, reruns only cost their own time; hotswap for method bodies, Command, DevTestLive, Recently done: live dev test mode, java.net.Socket

### Community 117 - "gun_model_spec: Our own guns pipeline (gun_model_spec section 6): tools/guns spec to .bmodel / texture / icon / .anib, bbmcp previews, NewGuns registration (newGun, newMagazine, addLootLike, useGunSounds), dev test mode gun"
Cohesion: 0.21
Nodes (13): Skill casebook case 9: closing the gaps after acceptance (method, numbers, checks), CLAUDE.md v0.36.0: MAC-10 pipeline, our items register as deciworldgen:<name>, BModelBox facts: corner array order and added offsets, box UV face layout, Decimation textures 2 px per unit, 32x32 icons, Our own guns pipeline (gun_model_spec section 6): tools/guns spec to .bmodel / texture / icon / .anib, bbmcp previews, NewGuns registration (newGun, newMagazine, addLootLike, useGunSounds), dev test mode gun, gunmodel.py v2: hexahedron parts (part, inset, shift, mirror, octagon), 1x1x1 declared with corner offsets, flat tones, textureWidth 512 UV step 8, icon from the study.py render, anib with slide_names, MAC-10 accepted by the user (v0.37.2): aim fixed, likes the firing style, icon good art, MAC-10 v0.37.3 gaps closed: shape kinds 60/31/5/5, 110 parts, declared sizes 1x1xN, tone 47, middle details, raised details 0.08, MAC-10 v2 (v0.37.0): 102 parts, 9.35 x 8.1 x 1.5, proportions from the user's side photo, sights at the aim centre, checked in game (gunview, gun) (+5 more)

### Community 118 - "PlaceNames: PlaceNames"
Cohesion: 0.23
Nodes (4): NamedPlace, World, PlaceNames, Shared core: areas registry (SAFEZONE, MILITARY, RADIATION, BASE_CLAIM, CITY_CLAIM with states) on Decimation's zone and turf lists

### Community 120 - "Slices: Slices"
Cohesion: 0.33
Nodes (4): Plan, Slices, Lake rules (none in city / military, 1 in 4 in other dead biomes, vanilla rate in overgrown, no surface lava pools), Floor height sampling (5x5 grid over the whole footprint where chunks exist plus 9 soilTop points in the window, median, maxSpread buildings 12 / schematics 7, stored in StructureData)

### Community 121 - "AssetDir: AssetDir"
Cohesion: 0.29
Nodes (4): World, WorldGenCommand, net.minecraft.command.CommandBase, net.minecraft.command.ICommandSender

### Community 122 - "DECIMATION_MOD_TASK: Goal: loot crates and cars in singleplayer"
Cohesion: 0.18
Nodes (12): CFR decompiler, Hand written Forge/Minecraft stub classes, Javassist bytecode patcher, Javassist cannot compile Java 8 lambdas, rtk hook drops grep/find flags (use Python os.walk), Toolchain set up each session (nothing preinstalled), Hypotheses: dedicated gate, YAML config path, dedicated lifecycle event, Prism Launcher instance mods folder (+4 more)

### Community 123 - "Protection: Protection"
Cohesion: 0.35
Nodes (4): FileData, Protection, Values, Shared core: protection table per armor item (infected hits, gunfire, bite chance, radiation)

### Community 125 - "WastelandFence: .column()"
Cohesion: 0.24
Nodes (5): WastelandFence, City v0.56.0: asphalt ramp for street stairs, natural bridge damage, metro entrance in the sidewalk, Wasteland v0.56.0: city apart (ring), fence with wire, landmines, gates, bunker blockhouse and garrison, v0.56.0: wasteland city apart, fence, bunker garrison; ramps, bridges, stations, Shots v0.56: wasteland city topdown, fence, gate, bunker; ramp, rail, bridge sheets

### Community 126 - "Ruins: .unit()"
Cohesion: 0.18
Nodes (11): Bug (fixed v0.30.2): full military armor made NPC gunfire almost harmless: armor multiplies per piece (x0.149 marine set) and vanilla hit cooldown dropped group hits; NPC shots are direct damage, not bullets, v0.30.4: NPC hit cooldown 0.25 s (npcHitCooldownTicks 5), v0.30.3: vanilla hit cooldown kept for NPC hits (npcHitsSkipCooldown false), x5 stays, Fix v0.30.2: NPC gun hits on players x npcDamageToPlayer (5) after armor, every NPC hit lands (LivingAttackEvent clears hurtResistantTime); bare 10 hp, marine set 1.49, 5 hits 7.47, Done v0.30.3: NPC shots traced with spread per tier, stopped by walls, impact particles, tracer always visible along the real line (PatchTracer v2 shotHook + NpcShots), CLAUDE.md v0.30.2 note: NPC hits x5, no hit cooldown, mech swap, CLAUDE.md v0.30.3: traced NPC shots, PatchTracer v2 then PatchFactions, test NPCs on a block, Balance: NPC gunfire on the player x5 after armor, every hit lands (v0.30.2) (+3 more)

### Community 127 - "gun_style_guide: Gun aiming: aim mode draws every gun at one fixed place (x 0.5, translate -1 -0.35 0.923), sPos unused in first person; screen centre at the top of the iron sights, y -4.85 to -5.0, z -0.15"
Cohesion: 0.20
Nodes (11): decimation-gun skill revision log: lessons from every user review (v1, v2, v0.37.0, v0.37.1), Aim rule corrected v0.37.2: centre passes the Uzi's rear aperture hole (about -4.65); compare with a Decimation gun in the same shot, Aim sway: headYawSway / dP tilt the gun after mouse moves, so judge aim only steady, Barrel attachment formula meets the muzzle only for flamePos x 12 to 15; Deci.offsetAttachment puts an offset model copy in AttachmentItem.ST per gun (MAC-10 -1.02, +0.14, 0), flamePos y sits about 0.85 above the bore (Uzi -4.5 over barrel -3.5); barrel attachments hang from it, Gun aiming: aim mode draws every gun at one fixed place (x 0.5, translate -1 -0.35 0.923), sPos unused in first person; screen centre at the top of the iron sights, y -4.85 to -5.0, z -0.15, Gun texture gradation: Decimation 15 to 31 part tones, faces 4.7 to 5.7 apart, texel noise 2 to 2.7, top lighter; gunmodel.paint reproduces it, Gun placement conventions: top at y -5, z centre -0.15, flamePos at muzzle minus 0.1, rhPos / lhPos medians per category, no sPos / mOff / Scale (+3 more)

### Community 128 - "Doors: Doors"
Cohesion: 0.22
Nodes (5): Worldgen rule: no vanilla doors in anything we generate (Doors.swap), Doors, Plan, Styled, Vanilla doors become Decimation doors at write time (worldgen/Doors)

### Community 130 - "ZoneStore: ZoneStore"
Cohesion: 0.33
Nodes (3): BridgeTest, Dev test bridge: forces city rivers on, fresh span with city streets at both ends, deck, water, no building over canals, walk across, lc_bridge_v0.45.0 shots: seed 1 world map and top down renders of canals and bridges, first dev test bridge views

### Community 131 - "SealedCaves: SealedCaves"
Cohesion: 0.27
Nodes (6): Caves, Override, Ravines, SealedCaves, net.minecraft.world.gen.MapGenCaves, net.minecraft.world.gen.MapGenRavine

### Community 132 - "Graded: Graded"
Cohesion: 0.24
Nodes (11): Building categories: civilian (apartment, office, shops, houses, garage), police (police station), military (base, checkpoint), later medical / industrial; category decides sector and Decimation zone, Extensibility: polish built as reusable parts (shell, room programs, surface sets, door rules, story / decay layer, exterior add-ons); a new type = footprint rule + room programs + facade + loot profile, Survivor camp (about 1 in 12 buildings, one room: lantern, CanFire, bedroll, crates, radio, WaterPallet, barricaded door, note decal, graffiti outside), Spec user decisions: survivor camps (about 1 in 12 buildings), shop signs use BlockSign_* assets, reusable parts for later categories, start by fixing the audited apartment / office / shop, Zone tagging (mil_ MILITARY, city_ and buildings POLICE, deciworldgen_zones.json), Direction change (8 Oktober 2026): DeceasedCraft buildings as city content with Lost Cities layout; procedural buildings stay as fallback, Round 2 step 4: building quality pass before new types (a audit, b prop catalogue, c design spec, d interiors room by room, e exterior polish, f user review in game; 4d.1 doors + low debris DONE v0.17.0, next 4d.2 surfaces), Round 2 step 5 (was step 4): new procedural building types (police station, hospital, gas station, warehouse), then step 6 loot balance per building (+3 more)

### Community 133 - "gunmodel: Part"
Cohesion: 0.18
Nodes (7): octagon(), Part, A hexahedron: corner (ix, iy, iz) in {0, 1}^3 -> absolute model point., Shrink the face at `side` (0 = low end, 1 = high end) of `axis`: amounts per…, Move the face at `side` of `axis` (a skew / shear)., The same part mirrored about z = ZC (left / right pairs)., A round bar along x as Decimation builds it: a middle slab plus a trapezoid…

### Community 134 - "interior_spec: Surfaces table per space (planks + FloorCarpet rugs, FloorTiles in kitchens / baths / corridors, WallOffice colour sets, Ceiling_1..4, lights, CeilingVent; interiors no longer copy the facade)"
Cohesion: 0.24
Nodes (10): A floor block is also the ceiling below: keep floors light, Finding: one interior material everywhere (birch plank walls, oak plank floors and ceilings), no ceilings, lighting, carpets or tiles, Wall / trim / accent palettes from vanilla 1.7.10 blocks (brick, clays, sandstone, quartz, stone brick), Room grid per storey plan (R_CORRIDOR..R_STOCK) decides floors and lights, Step 2 surfaces done v0.17.0: floors per room, wall panel set per building, ceiling light panels and vents, Surfaces table per space (planks + FloorCarpet rugs, FloorTiles in kitchens / baths / corridors, WallOffice colour sets, Ceiling_1..4, lights, CeilingVent; interiors no longer copy the facade), Floor and ceiling blocks and fixtures (BlockCeiling_1..4, BlockFloorCarpet_1..6, BlockFloorTiles_1..3, ceiling vents, BlockLight / LightOff, BlockExitLight), Interior wall panel blocks (BlockWallOffice_* colour sets: _Bottom_N skirting course, _Top above) (+2 more)

### Community 135 - "NewGuns: NewGuns"
Cohesion: 0.20
Nodes (10): Military base case 1: first FOB generator, four rounds (colours, test setup, overlaps, density), Military base case 2: user review of v0.42.0 (HESCO prop FPS, unlocked TOC door, orange dirt, prop clashes) -> v0.42.1, Military base case 3: second review, test site moved to -20000 y 230, old sites cleared, multiblock rebuild trap, Military base case 4: third review (blocked hatch, floating hangar roof, shelter roof, worldgen cutting the sky test), Military base case 5: walled in ladders and a missing TOC (v0.42.4), Lessons 12 and 13: check the player's way to a thing; refused modules must be loud, Military base lessons: research first, occupancy grid against overlaps, company streets, sky test without grading, orange dirt in Decimation's pack, avoid regular patterns, camera points in the generator, Project skill decimation-military-base: research, build (Canvas, occupancy grid, greedy fit, poi), test, small detail review checklist (+2 more)

### Community 136 - "FireRateTest: FireRateTest"
Cohesion: 0.33
Nodes (3): FireRateTest, Entity, EntityLivingBase

### Community 137 - "GunPerfTest: GunPerfTest"
Cohesion: 0.36
Nodes (3): GunPerfTest, EntityItem, net.minecraft.entity.item.EntityItem

### Community 138 - "MilBaseTest: MilBaseTest.java"
Cohesion: 0.33
Nodes (3): MilBaseTest, MilitaryBasePlan, net.decimation.worldgen.military.MilitaryBasePlan

### Community 139 - "Deci: net.minecraft.entity.player.EntityPlayer"
Cohesion: 0.25
Nodes (3): Bug: enemy group members spawned into walls; none in superblock districts (FIXED v0.54.1), InventoryBasic, net.minecraft.entity.player.EntityPlayer

### Community 140 - "sightfit: sightfit.py"
Cohesion: 0.31
Nodes (8): Bug: sights float above flat top rifles (v0.40.1), Case 17: sights floating above flat top rifles, bbox(), gap(), main(), rail_top(), Top of the receiver / rail in the sight zone: the highest long part there, iron…, dy > 0: the sights float that much above this gun's rail (they are placed for…

### Community 141 - "CityEdgeTest: CityEdgeTest"
Cohesion: 0.42
Nodes (3): ChunkProviderServer, CityEdgeTest, City edge worn ground (v0.55.0): EdgePlan.wear broken pavement band, coarse dirt rejected (orange)

### Community 142 - "PatchTurfNames: PatchTurfNames"
Cohesion: 0.28
Nodes (4): ClassPool, javassist.ClassPool, PatchLootRolls, PatchTurfNames

### Community 143 - "VanillaMobs: VanillaMobs"
Cohesion: 0.25
Nodes (4): Trap: vanilla class names are obfuscated in the shipped game, never test by package name (copySpawns fixed), VanillaMobs, No vanilla mobs (v0.33.0) in documentation, No vanilla mobs v0.33.0: vanilla monsters, animals, squid, bats, villagers, golems out of overworld spawn lists and refused on join (old chunks too); config deciworldgen_mobs.cfg; tests tag needed mobs with VanillaMobs.KEEP

### Community 144 - "Heuristic Auto Namer"
Cohesion: 0.28
Nodes (7): Case 18: Mk18 TS stock, EOTech 558, ACOG TA11 (v0.41.0), build(), dropped(), lower_edge(), px(), y of the stock's bottom edge (the lower strut) at model x: photo (480, 1470) to…, The M4A4 parts our Mk18 replaces (by position, measured on the M4A4:…

### Community 145 - "Condition: com.google.gson.JsonObject"
Cohesion: 0.22
Nodes (9): v0.21.0 critic pass 1, kitchen rework, wall breaches by column, narrow blocks ladder, -Pflats audit, Apartment references: real-world clearances, 1.7.10 furniture techniques, review checklist, Apartment review checklist: walkway, function readable, 40-60% free, palette, plausible decay, per room rules, Clearances: 1 block walkway, sofa-table 0-1 block, one free bed side, kitchen work triangle in a 4-6 block run, 1.7.10 techniques: stairs sofas with trapdoor arms, slab coffee tables, cauldron sink + tripwire tap, quartz stair toilet, paintings, wool curtains, Critic pass 1 on v0.20 flats: overall 3/10, verified findings and fixes, critic wrong on chair facing and lintels, Root cause found: narrow apartment blocks had no flats (core took the width); ladder under 16 wide, Open after pass 1: empty living fallback, wall detail layer, sofa arms, deeper living sets, bath sets, bedroom min width, camera (+1 more)

### Community 146 - "DecimationBiomes: DecimationBiomes"
Cohesion: 0.33
Nodes (8): camel(), classes(), known_fields(), main(), (binary name, source text) for every top-level file., Field names already chosen by the AI tables: (owner, obf) -> name., Field names declared directly in the outer class (indent 4)., top_level_fields()

### Community 150 - "edgescan: gradescan.py"
Cohesion: 0.29
Nodes (7): walkcheck stopped at y 4, propclash counted shelf tops as floating (FIXED v0.48.0), CLAUDE.md v0.42.4 note, How to test: walkcheck.py rules and the must-be-0 findings, Prop rule 12: every loot container reachable (walk test LOOT), collision_table(), main(), Prop block -> (collision height, walk through) from Decimation's registry code.

### Community 151 - "?: net.minecraft.nbt.NBTTagCompound"
Cohesion: 0.32
Nodes (3): Override, NBTTagCompound, net.minecraft.nbt.NBTTagCompound

### Community 156 - "study: .verts()"
Cohesion: 0.36
Nodes (6): contact(), inside(), True when point q lies in the part's (convex) shape, grown by eps., Parts that do not sit on anything. A thin detail (0.15 or less in some axis):…, The 8 corners in model space, indexed like vanilla ModelBox: v7 (0,0,0), v0…, (4 vertices, (u1, v1, u2, v2)) per face, like vanilla ModelBox.

### Community 157 - "SKILL: Project skill decimation-gun: end to end gun workflow (read docs, references, part plan, anchors, build, compare with study.py, register, gunview test, finish)"
Cohesion: 0.29
Nodes (7): Case 21: AS Val sights on the handguard, moved 4 back over the receiver (v0.42.2), Project skill decimation-gun: end to end gun workflow (read docs, references, part plan, anchors, build, compare with study.py, register, gunview test, finish), Lesson 23: sights must sit over the receiver (fixed sight zone x 0.8..5.5; AS Val receiver ends at 2.6), sightfit SHIFT dx, aim and reticle compensated, create_weapons.md: new models are built in code by tools/guns and the skill decimation-gun since v0.36.0; Techne is the older manual route, Style guide 14: sight position along the gun, SHIFT for guns whose receiver ends before the sight zone, Roadmap items and weapons (11 Oktober 2026): waiting looks (MAC-10 seated, HK416 fixes), more scopes, more guns, sniper nerf pointer, documentation.md: guns of our own (MAC-10, UMP9, HK416 / Tan, Mk18; rpm, damage halved by the global patch, loot with their model gun) and sights EOTech 558 / ACOG TA11

### Community 161 - "graph_update: graph_update.py"
Cohesion: 0.33
Nodes (6): v0.19.0 storey height 5 with own ceilings, Revised order after user 0.18 review: structure first, then furniture sets, then reference library + critic, Step 1a done v0.19.0: storeys 5 high, own white plaster ceiling layer, 5 step stair runs, Step 1b next: plaster lining inside outer walls, with furniture sets, audit_v0.19: dark tile ceilings first, then white plaster ceilings, Revised interior plan after user review: structure, data driven furniture sets, references + critic; Lost Cities idea not port

### Community 162 - "perfcheck: perfcheck.py"
Cohesion: 0.33
Nodes (5): NpcKind, BANDIT, HAZMAT, SOLDIER, SOVIET

### Community 163 - "NpcKillTest: .server()"
Cohesion: 0.40
Nodes (3): ItemStack, Minecraft, NpcKillTest

### Community 165 - "PatchIronSights: Case 16: Mk18 Mod 1 and iron sights hidden under a sight (v0.40.0)"
Cohesion: 0.40
Nodes (3): Case 16: Mk18 Mod 1 and iron sights hidden under a sight (v0.40.0), defaultScopeModel parts hidden while a sight is attached (19 guns), PatchIronSights

### Community 166 - "safezone: City refuge: fortified building on the block nearest each city region's middle (v0.48.0)"
Cohesion: 0.40
Nodes (5): Case 10: city refuge, first build (v0.48.0), Lessons 18-21: stairs in walkcheck, rotations via live, cameras, multiblock rebuild, City refuge: fortified building on the block nearest each city region's middle (v0.48.0), !cityRefuges world flag: refuges only in worlds created since v0.48, Refuge layout: apron barriers, concrete wall, chicane gate, 3 storey building (market hall, clinic, canteen, dormitory, armory, roof nests), yard

### Community 168 - "NpcKind: NpcKind"
Cohesion: 0.70
Nodes (4): axis(), clean(), letter(), main()

### Community 169 - "RefugeTest: RefugeTest"
Cohesion: 0.80
Nodes (4): clamp(), main(), side(), top()

### Community 171 - "new_feature: Worldgen round 2 plan (option A: terrain blending, street life, full multiblocks, new building types, loot balance)"
Cohesion: 0.50
Nodes (4): v0.18.0 apartment rooms and propFacing fix, Step 3 apartment rooms done v0.18.0: kitchen run, dining, TV + sofa, bedroom, bathroom, lobby on one side, audit_v0.18 / plans_v0.18: kitchen run, checker ceiling issue, furnished flats, 4d.3 apartment rooms done v0.18.0, next 4d.4 office programs

### Community 175 - "dressbalance: dressbalance.py"
Cohesion: 0.83
Nodes (3): cosine(), load(), main()

## Ambiguous Edges - Review These
- `worldcheck.py` → `Bug: generated metal shelves invisible (multiblock master never set, fixed v0.12.3)`  [AMBIGUOUS]
  bug.md · relation: conceptually_related_to
- `Genuinely new model recipe (needs Techne)` → `addBox passes Y as Z origin (use addShape)`  [AMBIGUOUS]
  docs/gun_model_spec.md · relation: conceptually_related_to
- `Spawn trap: Decimation adds its spawns to biomes existing in its preInit and strips vanilla monsters in its init; our biomes are made in our preInit and copySpawns copies plains lists in postInit` → `Recurring root cause: integrated server reports side CLIENT`  [AMBIGUOUS]
  docs/terrain.md · relation: semantically_similar_to
- `Fix: SmoothSwingThread busy wait (PatchSwing 4 ms sleep)` → `Report: FPS drop while aiming scopes (fixed v0.28.0..0.28.4)`  [AMBIGUOUS]
  bug.md · relation: conceptually_related_to
- `FML IWorldGenerator per chunk hook` → `v0.14.0: Decimation world type (own biome map on vanilla terrain generator, flat cities on the exact city sectors, Sectors now shared, autotest runs on this type)`  [AMBIGUOUS]
  CLAUDE.md · relation: conceptually_related_to

## Knowledge Gaps
- **338 isolated node(s):** `Technic modpack Decimation 1.7.10 (linusrhone)`, `Prism Launcher instance mods folder`, `Subsystem taxonomy (core, proxy, network, loot, zone, ...)`, `ServerCommandRegistrar (deci.aK.o)`, `ChatHandler (deci.aK.n, radio chat)` (+333 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **112 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `worldcheck.py` and `Bug: generated metal shelves invisible (multiblock master never set, fixed v0.12.3)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Genuinely new model recipe (needs Techne)` and `addBox passes Y as Z origin (use addShape)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Spawn trap: Decimation adds its spawns to biomes existing in its preInit and strips vanilla monsters in its init; our biomes are made in our preInit and copySpawns copies plains lists in postInit` and `Recurring root cause: integrated server reports side CLIENT`?**
  _Edge tagged AMBIGUOUS (relation: semantically_similar_to) - confidence is low._
- **What is the exact relationship between `Fix: SmoothSwingThread busy wait (PatchSwing 4 ms sleep)` and `Report: FPS drop while aiming scopes (fixed v0.28.0..0.28.4)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `FML IWorldGenerator per chunk hook` and `v0.14.0: Decimation world type (own biome map on vanilla terrain generator, flat cities on the exact city sectors, Sectors now shared, autotest runs on this type)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **Why does `Deci` connect `Deci: Deci` to `Deci: net.minecraft.entity.player.EntityPlayer`, `ScopeZoom: ScopeZoom`, `InfectedVariants: net.minecraft.entity.Entity`, `architecture: Bug tracker (bug.md)`, `CensusTest: CensusTest`, `?: net.minecraft.entity.EntityLiving`, `NpcEgg: Override`, `Deci: net.minecraft.item.Item`, `StreetPlan: StreetPlan`, `DeciBiome: DeciBiome`, `Deci: net.minecraft.item.ItemStack`, `?: cpw.mods.fml.common.eventhandler.SubscribeEvent`, `Ruins: .unit()`, `NpcShots: NpcShots`, `bug: Report: FPS drop in prop dense areas (line of sight cached v0.28.6, model drawing remains)`, `FacingTest: DevTestMode`, `porting: Study: DeceasedCraft city buildings (DCTweaks jar Lost Cities data)`, `DevAutoTest: DevAutoTest`, `LootPools: LootPools`, `ZoneRules: ZoneRules`, `SupplyDropScheduler: .onServerTick()`, `PlaceNames: PlaceNames`, `WastelandFence: .column()`?**
  _High betweenness centrality (0.109) - this node is a cross-community bridge._
- **Why does `Porting Decimation to modern Minecraft: three paths (parked plan)` connect `porting: Study: DeceasedCraft city buildings (DCTweaks jar Lost Cities data)` to `Deci: Deci`, `lctranslate: lctranslate.py`, `devtest: tools/devtest.py: dev test modes in ONE game launch, results file, contact sheet per mode (first choice since 2026-10-09)`, `CLAUDE: Knowledge index (docs/, interior spec, deobf notes, names.tsv, trackers)`?**
  _High betweenness centrality (0.086) - this node is a cross-community bridge._
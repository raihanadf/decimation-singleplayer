package net.decimation.worldgen.city;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import static net.decimation.worldgen.city.LcCity.hl;

/**
 * The city dressing inside a converted building (docs/city_dressing.md section 3, research
 * "Street facing ground floors" and the looted interior facts, skill decimation-city-interior):
 * what a real building of its kind has that the conversion lacks, and the story of an overrun city.
 * Picked per building with its own seed on the spots BuildingLayout read; every prop through
 * {@link Dress#place} with a metadata from {@link PropFacing}: rules speak in directions only
 * ("screen into the room", "front away from the wall").
 *
 * Wall props (EXIT light, flat pictures, TV, junction box) hang on the wall BEHIND their front:
 * the prop goes in the air cell in front of the wall with its front looking away from the wall.
 */
final class BuildingDress
{
    static final int HOME = 0, OFFICE = 1, SHOP = 2, FOOD = 3, MEDICAL = 4, POLICE = 5, INDUSTRY = 6, MILITARY = 7,
        LAB = 8, CIVIC = 9;

    private final Dress d;
    private final BuildingLayout lay;
    private final long seed;
    private final int kind;
    private final String name;
    private final Map<Long, BuildingLayout.Spot> at = new HashMap<Long, BuildingLayout.Spot>();
    private int salt;
    /** One log line per dressed building (prop counts): tools/props/dressbalance.py reads them. */
    static final boolean LOG = Boolean.getBoolean("deciworldgen.dresslog");

    private BuildingDress(Dress d, BuildingLayout lay, long seed, int kind, String name)
    {
        this.name = name.toLowerCase();
        this.d = d;
        this.lay = lay;
        this.seed = seed;
        this.kind = kind;
        for (BuildingLayout.Spot s : lay.spots)
        {
            at.put(key(s.x, s.y, s.z), s);
        }
    }

    private static long key(int x, int y, int z)
    {
        return ((long) x << 40) ^ ((long) y << 20) ^ z;
    }

    static int kind(String name)
    {
        String n = name.toLowerCase();
        if (n.contains("police"))
        {
            return POLICE;
        }
        if (n.contains("hospital") || n.contains("polyclini"))
        {
            return MEDICAL;
        }
        if (n.contains("military") || n.contains("bunker"))
        {
            return MILITARY;
        }
        if (n.contains("laborator"))
        {
            return LAB;
        }
        if (n.contains("factory") || n.contains("warehous") || n.contains("carservic") || n.contains("workshop")
            || n.contains("derrick") || n.contains("pumpjack") || n.contains("gasstation"))
        {
            return INDUSTRY;
        }
        if (n.contains("caf") || n.contains("fastfoo") || n.contains("restaurant") || n.contains("sushi"))
        {
            return FOOD;
        }
        if (n.contains("stor") || n.contains("shop") || n.contains("gallery") || n.contains("plaz") || n.contains("casino"))
        {
            return SHOP;
        }
        if (n.contains("offic") || n.contains("bank") || n.contains("tower") || n.contains("filmworks") || n.contains("thering")
            || n.contains("multi_la") || n.contains("building_la"))
        {
            return OFFICE;
        }
        if (n.contains("communitycenter") || n.contains("school") || n.contains("club"))
        {
            return CIVIC;
        }
        return HOME;
    }

    static Dress dress(BuildingPlan p, final LcContent.Shape s, LcContent.Building b)
    {
        Dress.Base base = new Dress.Base()
        {
            public Block block(int x, int y, int z)
            {
                if (x < 0 || y < 0 || z < 0 || x >= s.width || y >= s.height || z >= s.length)
                {
                    return null;
                }
                int i = s.index(x, y, z);
                return s.skip[i] ? Blocks.bedrock : s.blocks[i];   // "keep the world": terrain we cannot see
            }

            public int meta(int x, int y, int z)
            {
                return s.meta[s.index(x, y, z)] & 15;
            }
        };
        Dress d = new Dress(base, s.width, s.height, s.length);
        BuildingLayout lay = LcContent.layout(b.file, s);
        if (lay == null)
        {
            return d;
        }
        long seed = hl(0x4255494CL ^ p.minX() * 31L, p.minX(), p.minZ());
        new BuildingDress(d, lay, seed, kind(b.name), b.name).run();
        if (LOG)
        {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, int[]> e : new java.util.TreeMap<String, int[]>(d.tally).entrySet())
            {
                if (e.getValue()[0] > 0)
                {
                    sb.append(' ').append(e.getKey().replace("deci:", "")).append('=').append(e.getValue()[0]);
                }
            }
            cpw.mods.fml.common.FMLLog.info("[deciworldgen] dressed building %s kind %d at %d,%d spots %d:%s", b.name,
                                            kind(b.name), p.minX(), p.minZ(), lay.spots.size(), sb);
        }
        Dress.logTotals();
        return d;
    }

    // ------------------------------------------------------------------ helpers

    private int roll()
    {
        return (int) (hl(seed ^ (++salt) * 0x9E3779B97F4A7C15L, salt, salt * 7) % 1000);
    }

    private static Block deci(String name)
    {
        Block b = Block.getBlockFromName(name.contains(":") ? name : "deci:" + name);   // decals: deci:decal.<name>
        return b == Blocks.air ? null : b;
    }

    /** A standing prop on a spot, front toward dir. */
    private boolean stand(String name, BuildingLayout.Spot s, int dir)
    {
        Block b = deci(name);
        if (b == null)
        {
            return false;
        }
        int m = PropFacing.meta(b, dir);
        return m >= 0 && d.place(b, m, s.x, s.y, s.z, 0);
    }

    /**
     * A wall prop hung on the wall on side w of the cell (x, y, z): front away from that wall. A decal
     * draws a square of its size centred on its cell (DecalRenderer: blood splats 2, the US flag 3):
     * the whole wall behind that square must be solid and the cells in front of it free, or the
     * picture hangs over a window, a door or a corner.
     */
    private boolean hang(String name, int x, int y, int z, int w)
    {
        Block b = deci(name);
        if (b == null)
        {
            return false;
        }
        int size = name.startsWith("decal.bloodsplat") ? 2 : name.equals("decal.usflag") ? 3 : 1;
        int reach = size / 2;                            // cells each way beyond its own
        int[] f = BuildingLayout.DIR[w];
        int ax = f[1] != 0 ? 1 : 0, az = f[0] != 0 ? 1 : 0;   // along the wall
        for (int k = -reach; k <= reach; k++)
        {
            for (int dy = -reach; dy <= reach; dy++)
            {
                int cx = x + ax * k, cz = z + az * k;
                if (!lay.wall(cx + f[0], y + dy, cz + f[1]) || (k != 0 || dy != 0) && d.block(cx, y + dy, cz) != null)
                {
                    return false;
                }
            }
        }
        int m = PropFacing.meta(b, (w + 2) & 3);
        return m >= 0 && d.place(b, m, x, y, z, 1);
    }

    /** A ceiling prop in the cell under the ceiling, front toward dir. */
    private boolean ceiling(String name, int x, int y, int z, int dir)
    {
        Block b = deci(name);
        if (b == null || !lay.wall(x, y + 1, z))
        {
            return false;
        }
        int m = PropFacing.meta(b, dir);
        return m >= 0 && d.place(b, m, x, y, z, 1);
    }

    private static int firstWall(BuildingLayout.Spot s, int start)
    {
        for (int k = 0; k < 4; k++)
        {
            int w = (start + k) & 3;
            if ((s.walls & 1 << w) != 0)
            {
                return w;
            }
        }
        return -1;
    }

    private String pick(String... names)
    {
        return names[roll() % names.length];
    }

    // ------------------------------------------------------------------ rules

    private void run()
    {
        doorProps();
        exterior();
        Map<Integer, List<BuildingLayout.Spot>> storeys = new java.util.TreeMap<Integer, List<BuildingLayout.Spot>>();
        for (BuildingLayout.Spot s : lay.spots)
        {
            if (s.outside || s.doorway)
            {
                continue;
            }
            List<BuildingLayout.Spot> l = storeys.get(s.y);
            if (l == null)
            {
                l = new ArrayList<BuildingLayout.Spot>();
                storeys.put(s.y, l);
            }
            l.add(s);
        }
        theme();
        int index = 0;
        for (List<BuildingLayout.Spot> floor : storeys.values())
        {
            if (floor.size() >= 12)
            {
                storey(floor, index++);
            }
        }
    }

    /**
     * Doors: an EXIT light over the door on the inside (research: on the wall directly above an
     * exit door, facing into the room people escape from), a CCTV camera under the ceiling one cell
     * beside an entrance of a shop, office, police station, bank or lab, looking at the door.
     */
    private void doorProps()
    {
        boolean exits = kind != HOME;
        boolean cameras = kind == SHOP || kind == OFFICE || kind == POLICE || kind == LAB || kind == MEDICAL || kind == FOOD;
        for (int[] door : lay.doors)
        {
            boolean alongX = (door[3] & 1) == 0;            // walked through along x
            for (int side = -1; side <= 1; side += 2)
            {
                int ix = door[0] + (alongX ? side : 0), iz = door[2] + (alongX ? 0 : side);
                BuildingLayout.Spot in = at.get(key(ix, door[1], iz));
                if (in == null || in.outside)
                {
                    continue;
                }
                // the wall over the door is behind the cell in front of it, 2 up
                int w = alongX ? (side > 0 ? 2 : 0) : (side > 0 ? 3 : 1);
                if (exits && in.room >= 3 && roll() < 700)
                {
                    hang("BlockExitLight", ix, door[1] + 2, iz, w);
                }
                if (cameras && roll() < 450)
                {
                    int top = door[1] + in.room - 1;
                    int bx = ix + (alongX ? 0 : 1), bz = iz + (alongX ? 1 : 0);
                    ceiling("BlockCCTV", bx, top, bz, w);
                }
            }
        }
    }

    /**
     * The outside of the ground storey: a shop's sign over its door or window (research: the sign
     * band over the shop door at y + 3, flush on the facade, facing the street) and graffiti low on
     * blank outer walls; both on outdoor spots (open sky above) against an outer wall.
     */
    private void exterior()
    {
        int minY = Integer.MAX_VALUE;
        for (BuildingLayout.Spot s : lay.spots)
        {
            if (s.outside && s.wallCount() > 0)
            {
                minY = Math.min(minY, s.y);
            }
        }
        if (minY == Integer.MAX_VALUE)
        {
            return;
        }
        boolean shop = kind == SHOP || kind == FOOD;
        int signs = 0, tags = 0, maxTags = kind == HOME || kind == INDUSTRY || kind == CIVIC ? 3 : 1;
        List<BuildingLayout.Spot> outer = new ArrayList<BuildingLayout.Spot>();
        for (BuildingLayout.Spot s : lay.spots)
        {
            if (s.outside && s.wallCount() > 0 && s.y <= minY + 1 && !s.doorway)
            {
                outer.add(s);
            }
        }
        for (int t = 0; t < outer.size() * 2 && (signs == 0 && shop || tags < maxTags); t++)
        {
            BuildingLayout.Spot s = outer.get(roll() % outer.size());
            int w = firstWall(s, roll() & 3);
            if (w < 0)
            {
                continue;
            }
            int wx = s.x + BuildingLayout.DIR[w][0], wz = s.z + BuildingLayout.DIR[w][1];
            if (shop && signs == 0)
            {
                // over an opening: a door or glass in this wall within 1 to the side, at the walk level
                boolean opening = false;
                for (int o = -1; o <= 1 && !opening; o++)
                {
                    int ox = wx + (BuildingLayout.DIR[w][1] != 0 ? o : 0), oz = wz + (BuildingLayout.DIR[w][0] != 0 ? o : 0);
                    Block b0 = lay.at(ox, s.y, oz), b1 = lay.at(ox, s.y + 1, oz);
                    opening = b0 != null && (PropGeom.isDoor(b0) || b0.getMaterial() == net.minecraft.block.material.Material.glass)
                        || b1 != null && b1.getMaterial() == net.minecraft.block.material.Material.glass;
                }
                if ((opening || t > outer.size()) && lay.wall(wx, s.y + 2, wz) && d.block(s.x, s.y + 2, s.z) == null
                    && hang(signName(), s.x, s.y + 2, s.z, w))
                {
                    signs++;
                    Dress.note("~exterior shop sign");
                    continue;
                }
            }
            if (tags < maxTags && roll() < 600)
            {
                String[] art = {"BlockGraffiti_1", "BlockGraffiti_2", "BlockGraffiti_3", "BlockGraffiti_4", "BlockGraffiti_5",
                                "BlockGraffiti_6", "BlockGraffiti_7", "BlockGraffiti_8", "BlockGraffiti_9", "BlockGraffiti_10",
                                "BlockGraffiti_11", "BlockGraffiti_12"};
                if (lay.wall(wx, s.y, wz) && hang(art[roll() % art.length], s.x, s.y + 1, s.z, w))
                {
                    tags++;
                    Dress.note("~exterior graffiti");
                }
            }
        }
    }

    private String signName()
    {
        if (name.contains("gun"))
        {
            return roll() < 500 ? "BlockSign_Gunsrus" : "BlockSign_Decimunition";
        }
        if (kind == FOOD)
        {
            return roll() < 500 ? "BlockSign_Minedonalds" : "BlockSign_Mineway";
        }
        return "BlockSign_Minebay";
    }

    // ------------------------------------------------------------------ palettes

    /**
     * How a palette entry goes in: STAND on the floor against the wall, front into the room; HANG on
     * the wall 1 up (screens, pictures); LOW on the wall at floor level (fountain, payphone, junction
     * boxes); DISPLAY a shop display (its front is the closed panel: toward the wall); ROW 2 or 3 of
     * them side by side along the wall (shelf runs, waiting chairs); DESK a table along the wall,
     * maybe a thing on its top (monitor, radio) and a chair in front of it looking at it.
     */
    private static final int STAND = 0, HANG = 1, LOW = 2, DISPLAY = 3, ROW = 4, DESK = 5;

    private static final class Item
    {
        final int how, weight, cap;
        final boolean core;
        final String[] names;
        final String top, chair;

        Item(int how, int weight, int cap, boolean core, String top, String chair, String... names)
        {
            this.how = how;
            this.weight = weight;
            this.cap = cap;
            this.core = core;
            this.top = top;
            this.chair = chair;
            this.names = names;
        }
    }

    /** weight, cap per storey (a building may use 3 x that), names (variants). */
    private static Item it(int how, int weight, int cap, String... names)
    {
        return new Item(how, weight, cap, false, null, null, names);
    }

    /** Always in this kind's buildings (what makes the kind: shop displays, stretchers). */
    private static Item core(int how, int weight, int cap, String... names)
    {
        return new Item(how, weight, cap, true, null, null, names);
    }

    private static Item desk(int weight, int cap, boolean core, String table, String top, String chair)
    {
        return new Item(DESK, weight, cap, core, top, chair, table);
    }

    private static final String[] BOXES = {"BlockCardboardBoxes1", "BlockCardboardBoxes2", "BlockCardboardBoxes3"};
    private static final String[] TVS = {"BlockFlatscreenTV_News", "BlockFlatscreenTV_Emergency", "BlockFlatscreenTV",
                                         "BlockFlatscreenTV_Youtube"};
    private static final String[] VENDING = {"BlockVendingMachine_1", "BlockVendingMachine_2"};
    private static final String PC = "BlockMonitor", OCHAIR = "BlockOfficeChair", CHAIR = "BlockChair";

    /**
     * What each kind of building can have (user review of v0.51.2: "not balanced and mostly similar
     * between other building"): a palette per kind, of which each building keeps its own part
     * (theme()), so two offices differ while both read as offices.
     */
    private static final Item[][] PALETTE = new Item[10][];

    static
    {
        PALETTE[HOME] = new Item[] {
            it(HANG, 30, 1, TVS), it(STAND, 15, 1, "BlockWashingMachine"), it(STAND, 20, 2, BOXES),
            it(STAND, 10, 1, "BlockStereo"), desk(15, 1, false, "BlockWoodTable2", "BlockRadio1", CHAIR),
            desk(15, 1, false, "BlockWoodTable", null, CHAIR), desk(8, 1, false, "BlockMetalTable", PC, OCHAIR),
            desk(8, 1, false, "BlockChesstable", null, CHAIR), it(STAND, 10, 1, "BlockTrashcan"),
            it(STAND, 8, 1, "BlockWoodCrate"), it(STAND, 3, 2, "BlockPresent", "BlockPresent2", "BlockPresent3"),
            it(STAND, 6, 1, "BlockRadio1")};
        PALETTE[OFFICE] = new Item[] {
            desk(22, 2, false, "BlockMetalTable", PC, OCHAIR), it(ROW, 8, 1, OCHAIR), it(STAND, 12, 1, VENDING),
            it(STAND, 5, 1, "BlockElectricBoxBin"), it(HANG, 10, 1, "BlockFlatscreenTV_News", "BlockFlatscreenTV_Emergency"),
            it(LOW, 8, 1, "BlockWaterfountain"), it(LOW, 5, 1, "BlockPhonebooth"), it(STAND, 8, 1, "BlockMetalShelf_Empty"),
            it(STAND, 10, 2, "BlockCardboardBoxes1", "BlockCardboardBoxes3"), it(STAND, 10, 2, "BlockTrashcan"),
            it(LOW, 4, 1, "BlockElectricBox1", "BlockElectricBox2"), it(STAND, 2, 1, "BlockStudioCamera"),
            desk(8, 1, false, "BlockWoodTable", null, CHAIR)};
        PALETTE[SHOP] = new Item[] {
            core(DISPLAY, 30, 3, "BlockShopDisplay_1"), it(ROW, 20, 2, "BlockMetalShelf", "BlockMetalShelf_Empty"),
            it(STAND, 10, 1, VENDING), it(STAND, 12, 2, BOXES), desk(10, 1, false, "BlockMetalTable", PC, OCHAIR),
            it(STAND, 6, 1, "BlockTrashcan"), it(STAND, 4, 2, "BlockPresent", "BlockPresent2", "BlockPresent3"),
            it(STAND, 6, 1, "BlockWoodCrateOpen")};
        PALETTE[FOOD] = new Item[] {
            it(DISPLAY, 15, 2, "BlockShopDisplay_1"), desk(20, 3, true, "BlockWoodTable2", null, CHAIR),
            desk(15, 2, false, "BlockChesstable", null, CHAIR), it(STAND, 10, 1, VENDING),
            it(STAND, 8, 2, "BlockCardboardBoxes3"), it(STAND, 10, 2, "BlockTrashcan"), it(LOW, 4, 1, "BlockWaterfountain")};
        PALETTE[MEDICAL] = new Item[] {
            it(STAND, 20, 2, "BlockMedicalCrate"), core(STAND, 20, 2, "BlockStretcher"), it(HANG, 8, 1, "decal.hazardbio"),
            it(STAND, 10, 1, "BlockMetalShelf_Empty"), it(HANG, 8, 1, "BlockFlatscreenTV_Emergency"), it(ROW, 10, 1, CHAIR),
            desk(10, 1, false, "BlockMetalTable", PC, OCHAIR), it(LOW, 6, 1, "BlockWaterfountain"),
            it(STAND, 6, 1, "BlockTrashcan")};
        PALETTE[POLICE] = new Item[] {
            core(STAND, 15, 2, "BlockPoliceCrate"), it(STAND, 12, 1, "BlockWeaponCabinet"),
            desk(20, 2, false, "BlockMetalTable", PC, OCHAIR), it(ROW, 8, 1, CHAIR), it(HANG, 6, 1, "BlockFlatscreenTV_News"),
            it(STAND, 4, 1, "BlockElectricBoxBin"), it(STAND, 8, 1, "BlockAmmoCrate"), it(STAND, 4, 2, "BlockTarget"),
            it(STAND, 8, 1, "BlockMetalShelf"), it(HANG, 4, 1, "decal.warningcctv")};
        PALETTE[INDUSTRY] = new Item[] {
            it(STAND, 18, 3, "BlockWoodCrate", "BlockBarrel"), it(ROW, 12, 1, "BlockMetalShelf"),
            it(STAND, 10, 2, "BlockTire", "BlockTireStack"), it(HANG, 6, 1, "decal.warningchemicalstorage"),
            it(STAND, 5, 1, "BlockElectricBoxBin"), it(LOW, 6, 1, "BlockElectricBox1", "BlockElectricBox2"),
            it(STAND, 8, 1, "BlockPowerGenerator"), it(STAND, 8, 1, "BlockWoodCrateOpen"),
            it(STAND, 6, 1, "BlockWaterPallet", "BlockWaterPalletTarp"), desk(8, 1, false, "BlockMetalTable", "BlockRadio1", CHAIR),
            it(HANG, 4, 1, "decal.warninglights", "decal.warningwatchstep")};
        PALETTE[MILITARY] = new Item[] {
            core(STAND, 20, 3, "BlockMilitaryCrate", "BlockAmmoCrate"), it(STAND, 10, 1, "BlockWeaponCabinet"),
            desk(15, 1, false, "BlockMetalTable", "BlockMilitaryRadioSmall", CHAIR),
            it(STAND, 10, 1, "BlockWaterPallet", "BlockWaterPalletTarp"), it(STAND, 10, 2, "BlockWoodCrate"),
            it(STAND, 6, 1, "BlockAmmoCrateLarge"), it(STAND, 6, 1, "BlockPowerGenerator")};
        PALETTE[LAB] = new Item[] {
            it(STAND, 12, 1, "BlockMedicalCrate"), desk(15, 2, true, "BlockMetalTable", PC, OCHAIR),
            it(STAND, 6, 1, "BlockCookingStation"), it(HANG, 10, 1, "decal.hazardbio", "decal.warningxray", "decal.hazardnuclear"),
            it(STAND, 6, 1, "BlockAcidPit"), it(STAND, 6, 1, "BlockElectricBoxBin"), it(STAND, 8, 1, "BlockMetalShelf_Empty"),
            it(STAND, 5, 1, "BlockStretcher")};
        PALETTE[CIVIC] = new Item[] {
            it(STAND, 10, 1, VENDING), it(LOW, 8, 1, "BlockWaterfountain", "BlockPhonebooth"), it(STAND, 10, 2, BOXES),
            it(ROW, 12, 2, CHAIR), desk(12, 2, false, "BlockWoodTable", null, CHAIR),
            desk(10, 2, false, "BlockChesstable", null, CHAIR), it(STAND, 8, 1, "BlockTrashcan"), it(HANG, 6, 1, TVS),
            it(HANG, 4, 1, "decal.usflag")};
    }

    /** What happened in this building (one per building): the story layer follows it. */
    private static final int CALM = 0, LOOTED = 1, SQUAT = 2, MASSACRE = 3, QUARANTINE = 4, BARRICADE = 5;
    private static final String[] STORIES = {"calm", "looted", "squat", "massacre", "quarantine", "barricade"};

    /** This building's own part of the palette: entries kept, their weights, variants, density, story. */
    private List<Item> theme;
    private int[] themeWeight;
    private String[] variant;
    private int density, story, squatStorey;
    private final Map<Item, Integer> usedInBuilding = new HashMap<Item, Integer>();

    private void theme()
    {
        Item[] all = PALETTE[kind];
        theme = new ArrayList<Item>();
        List<Item> rest = new ArrayList<Item>();
        for (Item i : all)
        {
            if (i.core || roll() < 550)
            {
                theme.add(i);
            }
            else
            {
                rest.add(i);
            }
        }
        while (theme.size() < Math.min(4, all.length) && !rest.isEmpty())
        {
            theme.add(rest.remove(roll() % rest.size()));
        }
        themeWeight = new int[theme.size()];
        variant = new String[theme.size()];
        for (int k = 0; k < theme.size(); k++)
        {
            Item i = theme.get(k);
            themeWeight[k] = Math.max(1, i.weight * (50 + roll() % 101) / 100);
            // most buildings stick to one variant (one TV model, one crate kind), some mix them
            variant[k] = roll() < 700 ? i.names[roll() % i.names.length] : null;
        }
        density = 50 + roll() % 91;                          // 50 .. 140 % of the usual furniture
        int r = roll();
        boolean grim = kind == MEDICAL || kind == POLICE || kind == MILITARY || kind == LAB;
        if (grim)
        {
            story = r < 200 ? CALM : r < 380 ? LOOTED : r < 480 ? SQUAT : r < 640 ? MASSACRE : r < 840 ? QUARANTINE : BARRICADE;
        }
        else
        {
            story = r < 320 ? CALM : r < 600 ? LOOTED : r < 760 ? SQUAT : r < 840 ? MASSACRE : BARRICADE;
        }
        squatStorey = -1;
        Dress.note("~story " + STORIES[story]);
    }

    private String name(int k)
    {
        Item i = theme.get(k);
        return variant[k] != null ? variant[k] : i.names[roll() % i.names.length];
    }

    // ------------------------------------------------------------------ storeys

    /** One storey of a building: furniture from the building's theme, then the story. */
    private void storey(List<BuildingLayout.Spot> floor, int index)
    {
        List<BuildingLayout.Spot> wallSpots = new ArrayList<BuildingLayout.Spot>();
        for (BuildingLayout.Spot s : floor)
        {
            if (s.wallCount() >= 1 && s.room >= 2)
            {
                wallSpots.add(s);
            }
        }
        if (wallSpots.isEmpty())
        {
            return;
        }
        int budget = Math.max(2, Math.min(14, floor.size() / 30)) * density / 100;
        int[] usedHere = new int[theme.size()];
        int placed = 0;
        for (int tries = 0; tries < budget * 4 && placed < budget; tries++)
        {
            int k = choose(usedHere);
            if (k < 0)
            {
                break;
            }
            BuildingLayout.Spot s = wallSpots.get(roll() % wallSpots.size());
            int w = firstWall(s, roll() & 3);
            if (w < 0)
            {
                continue;
            }
            if (furnish(k, s, w))
            {
                usedHere[k]++;
                Item i = theme.get(k);
                Integer u = usedInBuilding.get(i);
                usedInBuilding.put(i, u == null ? 1 : u + 1);
                placed++;
            }
        }
        story(floor, wallSpots, index);
    }

    /** A theme entry by weight among those under their caps (this storey, the whole building), -1 if none. */
    private int choose(int[] usedHere)
    {
        int total = 0;
        int[] w = new int[theme.size()];
        for (int k = 0; k < theme.size(); k++)
        {
            Item i = theme.get(k);
            Integer u = usedInBuilding.get(i);
            if (usedHere[k] < i.cap && (u == null || u < i.cap * 3))
            {
                w[k] = themeWeight[k];
                total += w[k];
            }
        }
        if (total == 0)
        {
            return -1;
        }
        int r = roll() % total;
        for (int k = 0; k < w.length; k++)
        {
            r -= w[k];
            if (r < 0)
            {
                return k;
            }
        }
        return -1;
    }

    /** Theme entry k against the wall w of spot s. */
    private boolean furnish(int k, BuildingLayout.Spot s, int w)
    {
        Item i = theme.get(k);
        int room = (w + 2) & 3;                              // front into the room, back to the wall
        switch (i.how)
        {
            case HANG:
                return hang(name(k), s.x, s.y + 1, s.z, w);
            case LOW:
                return hang(name(k), s.x, s.y, s.z, w);
            case DISPLAY:
                return stand(name(k), s, w);                // its front is the closed panel: shelves to the room
            case ROW:
                return row(name(k), s, w, room);
            case DESK:
                return desk(i, s, w, room);
            default:
                return stand(name(k), s, room);
        }
    }

    /** 2 or 3 of one prop side by side along the wall w from s (shelf runs, waiting chairs). */
    private boolean row(String name, BuildingLayout.Spot s, int w, int room)
    {
        int[] a = BuildingLayout.DIR[(w + 1) & 3];
        int n = 2 + roll() % 2, put = 0;
        for (int k = 0; k < n; k++)
        {
            BuildingLayout.Spot t = at.get(key(s.x + a[0] * k, s.y, s.z + a[1] * k));
            if (t == null || t.doorway || (t.walls & 1 << w) == 0 || !stand(name, t, room))
            {
                break;
            }
            put++;
        }
        return put > 0;
    }

    /**
     * A table along the wall w at s (its long side along the wall), maybe a thing on its top facing
     * the room (a monitor, a radio) and a chair in the cell in front looking at the table.
     */
    private boolean desk(Item i, BuildingLayout.Spot s, int w, int room)
    {
        Block t = deci(i.names[0]);
        if (t == null)
        {
            return false;
        }
        boolean alongX = (w & 1) == 1;                       // a wall to the south / north runs along x
        int m = PropFacing.along(t, alongX, roll());
        if (m < 0)
        {
            m = PropFacing.meta(t, room);
        }
        if (m < 0 || !d.place(t, m, s.x, s.y, s.z, 0))
        {
            return false;
        }
        if (i.top != null)
        {
            Block b = deci(i.top);
            int tm = b == null ? -1 : PropFacing.meta(b, room);
            if (tm >= 0)
            {
                d.place(b, tm, s.x, s.y + 1, s.z, 0);
            }
        }
        if (i.chair != null)
        {
            int[] f = BuildingLayout.DIR[room];
            BuildingLayout.Spot c = at.get(key(s.x + f[0], s.y, s.z + f[1]));
            if (c != null && !c.doorway)
            {
                stand(i.chair, c, w);                        // the seat looks at the table
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ story

    /**
     * The story of one storey (research "abandoned city": debris is caused, bodies only in a scene
     * that explains them), following what happened in this building: calm (a little trash), looted
     * (opened crates, boxes, trash, graffiti), squatters (a camp on one storey: lantern, radio,
     * bedding boxes, a crate, trash), massacre (bodies and blood), quarantine (body bags,
     * stretchers, biohazard signs), barricade (crates and barrels piled at the ground floor doors).
     */
    private void story(List<BuildingLayout.Spot> floor, List<BuildingLayout.Spot> wallSpots, int index)
    {
        int bodies = story == MASSACRE ? 1 + roll() % 2 : story == CALM ? 0 : (roll() < 150 ? 1 : 0);
        for (int b = 0; b < bodies; b++)
        {
            BuildingLayout.Spot s = wallSpots.get(roll() % wallSpots.size());
            int w = firstWall(s, roll() & 3);
            if (w >= 0 && stand("BlockSkeletonWall", s, (w + 2) & 3))
            {
                hang(pick("decal.bloodsplat1", "decal.bloodsplat2", "decal.bloodsplat3", "decal.bloodsplat4",
                          "decal.bloodsplat5"), s.x, s.y + 1, s.z, w);
            }
        }
        if (story == MASSACRE ? roll() < 500 : roll() < 50)
        {
            BuildingLayout.Spot s = floor.get(roll() % floor.size());
            stand("BlockSkeletonGround", s, roll() & 3);
        }
        int bags = story == QUARANTINE ? 1 + roll() % 3
            : (kind == MEDICAL || kind == POLICE || kind == MILITARY) && roll() < 250 ? 1 : 0;
        for (int b = 0; b < bags; b++)
        {
            BuildingLayout.Spot s = floor.get(roll() % floor.size());
            stand(pick("BlockBodyBagBlack", "BlockBodyBagBlue", "BlockBodyBagGreen", "BlockBodyBagOrange"), s, roll() & 3);
        }
        if (story == QUARANTINE && roll() < 600)
        {
            BuildingLayout.Spot s = wallSpots.get(roll() % wallSpots.size());
            int w = firstWall(s, roll() & 3);
            if (w >= 0)
            {
                if (roll() < 500)
                {
                    stand("BlockStretcher", s, (w + 2) & 3);
                }
                else
                {
                    hang("decal.hazardbio", s.x, s.y + 1, s.z, w);
                }
            }
        }
        if (story == LOOTED)
        {
            int n = 1 + roll() % 3;
            for (int k = 0; k < n; k++)
            {
                BuildingLayout.Spot s = wallSpots.get(roll() % wallSpots.size());
                int w = firstWall(s, roll() & 3);
                if (w >= 0)
                {
                    stand(roll() < 500 ? "BlockWoodCrateOpen" : BOXES[roll() % 3], s, (w + 2) & 3);
                }
            }
        }
        if (story == SQUAT && (squatStorey < 0 && (index > 0 || roll() < 400)))
        {
            squatStorey = index;
            camp(wallSpots);
        }
        if (story == BARRICADE && index == 0)
        {
            barricade();
        }
        int trash = story == LOOTED || story == SQUAT ? 600 : story == CALM ? 200 : 350;
        if (roll() < trash)
        {
            BuildingLayout.Spot s = wallSpots.get(roll() % wallSpots.size());
            int n = 1 + roll() % 3;
            for (int k = 0; k < n; k++)
            {
                BuildingLayout.Spot t = at.get(key(s.x + (k % 2), s.y, s.z + k / 2));
                if (t != null && !t.doorway)
                {
                    stand(pick("BlockTrashBag1", "BlockTrashBag2"), t, roll() & 3);
                }
            }
        }
        int tags = story == LOOTED || story == SQUAT ? 500 : story == CALM ? 120 : 300;
        if (roll() < tags)
        {
            BuildingLayout.Spot s = wallSpots.get(roll() % wallSpots.size());
            int w = firstWall(s, roll() & 3);
            if (w >= 0)
            {
                String pic = kind == HOME || kind == INDUSTRY || kind == CIVIC || story == SQUAT || story == LOOTED
                    ? pick("BlockGraffiti_1", "BlockGraffiti_2", "BlockGraffiti_3", "BlockGraffiti_4", "BlockGraffiti_5",
                           "BlockGraffiti_6", "BlockGraffiti_7", "BlockGraffiti_8", "BlockGraffiti_9", "BlockGraffiti_10",
                           "BlockGraffiti_12")
                    : pick("decal.warningwatchstep", "decal.warningslipperyfloor", "BlockGraffiti_11", "decal.warningtrespass");
                hang(pic, s.x, s.y + 1, s.z, w);
            }
        }
    }

    /**
     * Squatters' corner: a lantern, a radio, boxes for bedding, a crate, around one wall spot. Never
     * BlockStorageCrate (a footlocker) or a chest here: they extend BlockChest, which turns itself
     * from its neighbours when placed and ignores the metadata (fronts ended up facing walls).
     */
    private void camp(List<BuildingLayout.Spot> wallSpots)
    {
        BuildingLayout.Spot s = wallSpots.get(roll() % wallSpots.size());
        int w = firstWall(s, roll() & 3);
        if (w < 0)
        {
            return;
        }
        int room = (w + 2) & 3;
        int[] a = BuildingLayout.DIR[(w + 1) & 3];
        String[] kit = {"BlockLantern", roll() < 500 ? "BlockRadio1" : "BlockStereo", BOXES[roll() % 3], "BlockWoodCrate",
                        BOXES[roll() % 3]};
        int put = 0;
        for (int k = 0; k < kit.length; k++)
        {
            int o = (k + 1) / 2 * (k % 2 == 0 ? 1 : -1);  // 0, -1, 1, -2, 2 along the wall
            BuildingLayout.Spot t = at.get(key(s.x + a[0] * o, s.y, s.z + a[1] * o));
            if (t != null && !t.doorway && stand(kit[k], t, room))
            {
                put++;
            }
        }
        if (put > 0)
        {
            Dress.note("~story squat camp");
        }
    }

    /** Barricaded entrances: crates, barrels and boxes in the cells beside each ground door (not in its walkway). */
    private void barricade()
    {
        String[] pile = {"BlockWoodCrate", "BlockBarrel", "BlockCardboardBoxes2", "BlockWoodCrate"};
        int done = 0;
        for (int[] door : lay.doors)
        {
            boolean alongX = (door[3] & 1) == 0;
            for (int side = -1; side <= 1; side += 2)
            {
                int ix = door[0] + (alongX ? side : 0), iz = door[2] + (alongX ? 0 : side);
                BuildingLayout.Spot in = at.get(key(ix, door[1], iz));
                if (in == null || in.outside)
                {
                    continue;
                }
                for (int o = -1; o <= 1; o += 2)          // beside the walkway, inside
                {
                    int bx = ix + (alongX ? 0 : o), bz = iz + (alongX ? o : 0);
                    BuildingLayout.Spot t = at.get(key(bx, door[1], bz));
                    if (t != null && !t.doorway && !t.outside && stand(pile[roll() % pile.length], t, roll() & 3))
                    {
                        done++;
                    }
                }
            }
        }
        if (done > 0)
        {
            Dress.note("~story barricade");
        }
    }
}

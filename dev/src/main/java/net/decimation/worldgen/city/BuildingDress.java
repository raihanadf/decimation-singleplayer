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
        Map<Integer, List<BuildingLayout.Spot>> storeys = new HashMap<Integer, List<BuildingLayout.Spot>>();
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
        for (List<BuildingLayout.Spot> floor : storeys.values())
        {
            if (floor.size() >= 12)
            {
                storey(floor);
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

    /** One storey of a building: furniture its kind lacks, then the story. */
    private void storey(List<BuildingLayout.Spot> floor)
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
        int budget = Math.max(2, Math.min(14, floor.size() / 30));
        int placed = 0;
        for (int tries = 0; tries < budget * 4 && placed < budget; tries++)
        {
            BuildingLayout.Spot s = wallSpots.get(roll() % wallSpots.size());
            int w = firstWall(s, roll() & 3);
            if (w < 0)
            {
                continue;
            }
            int room = (w + 2) & 3;                          // front into the room, back to the wall
            if (prop(s, w, room))
            {
                placed++;
            }
        }
        // the story: bodies and their blood, trash, notes
        story(floor, wallSpots);
    }

    /** One furniture piece of this building's kind against the wall w of spot s. */
    private boolean prop(BuildingLayout.Spot s, int w, int room)
    {
        int r = roll();
        switch (kind)
        {
            case HOME:
                if (r < 300) return hang(pick("BlockFlatscreenTV_News", "BlockFlatscreenTV_Emergency", "BlockFlatscreenTV"), s.x, s.y + 1, s.z, w);
                if (r < 450) return stand("BlockWashingMachine", s, room);
                if (r < 650) return stand(pick("BlockCardboardBoxes1", "BlockCardboardBoxes2", "BlockCardboardBoxes3"), s, room);
                if (r < 800) return stand("BlockStereo", s, room);
                return stand("BlockRadio1", s, room);
            case OFFICE:
                if (r < 200) return stand(pick("BlockVendingMachine_1", "BlockVendingMachine_2"), s, room);
                if (r < 350) return stand("BlockElectricBoxBin", s, room);
                if (r < 500) return hang(pick("BlockFlatscreenTV_News", "BlockFlatscreenTV_Emergency"), s.x, s.y + 1, s.z, w);
                if (r < 650) return hang(pick("BlockWaterfountain", "BlockPhonebooth"), s.x, s.y, s.z, w);
                if (r < 800) return stand("BlockMetalShelf_Empty", s, room);
                return stand(pick("BlockCardboardBoxes1", "BlockCardboardBoxes3"), s, room);
            case SHOP:
                if (r < 350) return stand("BlockShopDisplay_1", s, w);   // its front is the closed panel: shelves to the room
                if (r < 600) return stand(pick("BlockMetalShelf", "BlockMetalShelf_Empty"), s, room);
                if (r < 750) return stand(pick("BlockVendingMachine_1", "BlockVendingMachine_2"), s, room);
                return stand(pick("BlockCardboardBoxes1", "BlockCardboardBoxes2"), s, room);
            case FOOD:
                if (r < 400) return stand("BlockShopDisplay_1", s, w);
                if (r < 650) return stand(pick("BlockVendingMachine_1", "BlockVendingMachine_2"), s, room);
                return stand("BlockCardboardBoxes3", s, room);
            case MEDICAL:
                if (r < 300) return stand("BlockMedicalCrate", s, room);
                if (r < 500) return stand("BlockStretcher", s, room);
                if (r < 650) return hang("decal.hazardbio", s.x, s.y + 1, s.z, w);
                if (r < 800) return stand("BlockMetalShelf_Empty", s, room);
                return hang("BlockFlatscreenTV_Emergency", s.x, s.y + 1, s.z, w);
            case POLICE:
                if (r < 350) return stand("BlockPoliceCrate", s, room);
                if (r < 550) return stand("BlockWeaponCabinet", s, room);
                if (r < 700) return stand("BlockElectricBoxBin", s, room);
                return hang("BlockFlatscreenTV_News", s.x, s.y + 1, s.z, w);
            case INDUSTRY:
                if (r < 300) return stand(pick("BlockWoodCrate", "BlockBarrel"), s, room);
                if (r < 500) return stand("BlockMetalShelf", s, room);
                if (r < 650) return stand(pick("BlockTireStack", "BlockTire"), s, room);
                if (r < 800) return hang("decal.warningchemicalstorage", s.x, s.y + 1, s.z, w);
                return stand("BlockElectricBoxBin", s, room);
            case MILITARY:
                if (r < 350) return stand(pick("BlockMilitaryCrate", "BlockAmmoCrate"), s, room);
                if (r < 550) return stand("BlockWeaponCabinet", s, room);
                if (r < 700) return stand("BlockMilitaryRadioSmall", s, room);
                return stand("BlockWaterPallet", s, room);
            case LAB:
                if (r < 300) return stand("BlockMedicalCrate", s, room);
                if (r < 500) return stand("BlockElectricBoxBin", s, room);
                if (r < 700) return hang(pick("decal.hazardbio", "decal.warningxray", "decal.hazardnuclear"), s.x, s.y + 1, s.z, w);
                return stand("BlockAcidPit", s, room);
            default:
                if (r < 300) return stand(pick("BlockVendingMachine_1", "BlockVendingMachine_2"), s, room);
                if (r < 500) return hang(pick("BlockWaterfountain", "BlockPhonebooth"), s.x, s.y, s.z, w);
                return stand(pick("BlockCardboardBoxes1", "BlockCardboardBoxes3"), s, room);
        }
    }

    /**
     * The story of one storey (research "abandoned city": debris is caused, bodies only in a scene
     * that explains them): now and then a body against a wall with blood on the wall above it, a
     * body bag in a medical / police / military building, trash bags, a note or graffiti.
     */
    private void story(List<BuildingLayout.Spot> floor, List<BuildingLayout.Spot> wallSpots)
    {
        int r = roll();
        if (r < 220)
        {
            BuildingLayout.Spot s = wallSpots.get(roll() % wallSpots.size());
            int w = firstWall(s, roll() & 3);
            if (w >= 0 && stand("BlockSkeletonWall", s, (w + 2) & 3))
            {
                hang(pick("decal.bloodsplat1", "decal.bloodsplat2", "decal.bloodsplat3", "decal.bloodsplat4",
                          "decal.bloodsplat5"), s.x, s.y + 1, s.z, w);
            }
        }
        else if (r < 300)
        {
            BuildingLayout.Spot s = floor.get(roll() % floor.size());
            stand("BlockSkeletonGround", s, roll() & 3);
        }
        if ((kind == MEDICAL || kind == POLICE || kind == MILITARY) && roll() < 450)
        {
            BuildingLayout.Spot s = floor.get(roll() % floor.size());
            stand(pick("BlockBodyBagBlack", "BlockBodyBagBlue", "BlockBodyBagGreen", "BlockBodyBagOrange"), s, roll() & 3);
        }
        if (roll() < 350)
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
        if (roll() < 300)
        {
            BuildingLayout.Spot s = wallSpots.get(roll() % wallSpots.size());
            int w = firstWall(s, roll() & 3);
            if (w >= 0)
            {
                String pic = kind == HOME || kind == INDUSTRY || kind == CIVIC
                    ? pick("BlockGraffiti_1", "BlockGraffiti_9", "BlockGraffiti_10", "BlockGraffiti_12", "BlockGraffiti_2")
                    : pick("decal.warningwatchstep", "decal.warningslipperyfloor", "BlockGraffiti_11");
                hang(pic, s.x, s.y + 1, s.z, w);
            }
        }
    }
}

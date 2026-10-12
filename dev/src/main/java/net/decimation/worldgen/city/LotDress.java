package net.decimation.worldgen.city;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import static net.decimation.worldgen.city.LcCity.hl;

/**
 * The city dressing of an open lot (docs/city_dressing.md "Parks and lots"): a park part gets what
 * a real park has beside its benches (bins, a chess table, a lamp, a bicycle) and, now and then, a
 * body; a bare lot gets one scene or none: cars left in rows (all the same way: research "lot
 * wrecks stay aligned in their stalls"), a survivor's spot (boxes, a fire can, trash), a dumping
 * ground (tyres, barrels, crates, a dumpster against the edge). Every prop through Dress.place with
 * a metadata from PropFacing.
 */
final class LotDress
{
    private final LotPlan p;
    private final Dress d;
    private final int[][] stand = new int[16][16];
    private final int district;
    private int salt;

    private LotDress(LotPlan p, Dress d)
    {
        this.p = p;
        this.d = d;
        this.district = StreetDress.district(p.style);
    }

    static Dress dress(final LotPlan p)
    {
        final int[] tmp = new int[1];
        Dress.Base base = new Dress.Base()
        {
            public Block block(int x, int y, int z)
            {
                return x < 0 || z < 0 || x > 15 || z > 15 ? null : p.baseAt(x, y, z, tmp);
            }

            public int meta(int x, int y, int z)
            {
                p.baseAt(x, y, z, tmp);
                return tmp[0];
            }
        };
        Dress d = new Dress(base, 16, Math.max(4, p.height() + 2), 16);
        LotDress l = new LotDress(p, d);
        l.spots();
        if (p.hasPark())
        {
            l.park();
        }
        else
        {
            l.bare();
        }
        Dress.logTotals();
        return d;
    }

    private int roll()
    {
        return (int) (hl(p.seed ^ 0x4C4F5444L ^ (++salt) * 0x9E3779B97F4A7C15L, p.minX(), p.minZ()) % 1000);
    }

    private static Block deci(String name)
    {
        Block b = Block.getBlockFromName("deci:" + name);
        return b == Blocks.air ? null : b;
    }

    /** The layer a prop stands at on each column: the first open cell over a real top, -1 none. */
    private void spots()
    {
        for (int x = 0; x < 16; x++)
        {
            for (int z = 0; z < 16; z++)
            {
                stand[x][z] = -1;
                for (int y = 1; y < d.h - 1; y++)
                {
                    Block here = d.block(x, y, z), below = d.block(x, y - 1, z);
                    if ((here == null || BuildingLayout.cover(here)) && d.block(x, y + 1, z) == null && below != null
                        && Dress.floorBlock(below, d.meta(x, y - 1, z)))
                    {
                        stand[x][z] = y;
                        break;
                    }
                }
            }
        }
    }

    private boolean put(String name, int x, int z, int meta)
    {
        Block b = deci(name);
        return b != null && meta >= 0 && x >= 0 && z >= 0 && x < 16 && z < 16 && stand[x][z] > 0
            && d.place(b, meta, x, stand[x][z], z, 0);
    }

    private boolean face(String name, int x, int z, int dir)
    {
        Block b = deci(name);
        return b != null && put(name, x, z, PropFacing.meta(b, dir));
    }

    /** Tries a prop on a few random cells until it fits. */
    private boolean somewhere(String name, int dir, int tries)
    {
        for (int k = 0; k < tries; k++)
        {
            int x = 1 + roll() % 14, z = 1 + roll() % 14;
            if (dir < 0 ? put(name, x, z, 2 + (roll() & 3)) : face(name, x, z, dir))
            {
                return true;
            }
        }
        return false;
    }

    /** Park: a bin beside each bench (2 along), one chess table, a lamp, a bicycle, maybe a body. */
    private void park()
    {
        Block bench = deci("BlockStreetBench");
        int bins = 0;
        for (int x = 0; x < 16 && bins < 3; x++)
        {
            for (int z = 0; z < 16 && bins < 3; z++)
            {
                for (int y = 1; y < d.h - 1; y++)
                {
                    if (d.block(x, y, z) == bench && bench != null)
                    {
                        for (int[] o : new int[][] {{2, 0}, {-2, 0}, {0, 2}, {0, -2}})
                        {
                            if (put("BlockStreetBin", x + o[0], z + o[1], 2))
                            {
                                bins++;
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (roll() < 350)
        {
            somewhere("BlockChesstable", -1, 12);
        }
        if (roll() < 450)
        {
            somewhere("BlockStreetLight", roll() & 3, 12);
        }
        if (district != StreetDress.WASTELAND && roll() < 250)
        {
            somewhere("BlockBicycle", -1, 10);
        }
        if (roll() < (district == StreetDress.WASTELAND ? 350 : 100))
        {
            somewhere("BlockSkeletonGround", roll() & 3, 10);
        }
        if (roll() < 300)
        {
            for (int k = 0; k < 2 + roll() % 3; k++)
            {
                somewhere(roll() < 500 ? "BlockTrashBag1" : "BlockTrashBag2", -1, 6);
            }
        }
    }

    /** Bare lot: one scene or nothing. */
    private void bare()
    {
        if (district == StreetDress.WASTELAND)
        {
            militaryLot();
            return;
        }
        int r = roll();
        cpw.mods.fml.common.FMLLog.info("[deciworldgen] dress lot %s at %d,%d", r < 280 ? "cars" : r < 450 ? "camp"
            : r < 620 ? "dump" : "empty", p.minX(), p.minZ());
        if (r < 280)
        {
            parkedCars();
        }
        else if (r < 450)
        {
            // a survivor's spot: boxes against each other, a fire can, bags, in the wasteland a body
            int x = 4 + roll() % 8, z = 4 + roll() % 8;
            put("BlockCanFire", x, z, 2);
            String[] around = {"BlockCardboardBoxes1", "BlockCardboardBoxes3", "BlockTrashBag1", "BlockTrashBag2",
                               "BlockWoodCrateOpen", "BlockLantern"};
            int[][] o = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}, {2, 2}, {-2, -2}};
            for (int k = 0; k < o.length; k++)
            {
                put(around[(k + roll()) % around.length], x + o[k][0], z + o[k][1], 2 + (roll() & 3));
            }
            if (district == StreetDress.WASTELAND && roll() < 500)
            {
                face("BlockSkeletonGround", x + 3, z, roll() & 3);
            }
        }
        else if (r < 620)
        {
            // a dumping ground against one edge: tyres, barrels, crates, a dumpster with its lid edge out
            String[] junk = {"BlockTireStack", "BlockTire", "BlockBarrel", "BlockBarrel", "BlockWoodCrate",
                             "BlockCardboardBoxes2", "BlockTrashBag1", "BlockTrashBag2"};
            int edge = roll() & 3;
            for (int k = 0; k < 8; k++)
            {
                int a = 2 + roll() % 12, b = 1 + roll() % 3;
                int x = edge == 0 ? 15 - b : edge == 2 ? b : a, z = edge == 1 ? 15 - b : edge == 3 ? b : a;
                put(junk[roll() % junk.length], x, z, 2 + (roll() & 3));
            }
            int a = 4 + roll() % 8;
            int x = edge == 0 ? 12 : edge == 2 ? 3 : a, z = edge == 1 ? 12 : edge == 3 ? 3 : a;
            face("BlockDumpster", x, z, (edge + 2) & 3);
        }
    }

    /** Two rows of cars left in a lot, every car the same way (noses toward the middle line). */
    private void parkedCars()
    {
        String[] cars = {"BlockWreckage1", "BlockWreckage2", "BlockWreckage3", "BlockWreckage4", "BlockWreckage5",
                         "BlockTruckWreckage1", "BlockTruckWreckage3", "BlockTruckWreckage6"};
        boolean rowsAlongX = roll() < 500;               // cars stand side by side along x, long along z
        for (int row = 0; row < 2; row++)
        {
            int across = row == 0 ? 4 : 11;               // rows' anchors across, noses toward the middle
            int dir = rowsAlongX ? (row == 0 ? 1 : 3) : (row == 0 ? 0 : 2);
            for (int k = 0; k < 4; k++)
            {
                if (roll() < 350)
                {
                    continue;                             // an empty stall
                }
                int along = 2 + k * 4;
                int x = rowsAlongX ? along : across, z = rowsAlongX ? across : along;
                String name = cars[roll() % cars.length];
                face(name, x, z, dir);
            }
        }
    }

    // ------------------------------------------------------------------ the military area

    /**
     * A bare lot of the wasteland district, the city's military area (docs/wasteland.md): a motor
     * pool (vehicles side by side, all one way, fuel and spares at the edge), a supply dump (rows of
     * military crates, ammo cases, care packages, water pallets, a spotlight), a crashed
     * helicopter with its dead, a casualty collection point (body bags in rows, stretchers, medical
     * crates, a hazard light), or nothing. Crates and cases are Decimation loot.
     */
    private void militaryLot()
    {
        int r = roll();
        String scene = r < 250 ? "motorpool" : r < 520 ? "supplies" : r < 680 ? "helicopter" : r < 850 ? "casualties" : "empty";
        cpw.mods.fml.common.FMLLog.info("[deciworldgen] dress lot military %s at %d,%d", scene, p.minX(), p.minZ());
        if (r < 250)
        {
            motorPool();
        }
        else if (r < 520)
        {
            supplyDump();
        }
        else if (r < 680)
        {
            helicopter();
        }
        else if (r < 850)
        {
            casualties();
        }
    }

    /** Jeeps and an APC side by side, long axis one way, noses the same way; fuel drums and tyres at one edge. */
    private void motorPool()
    {
        boolean rowAlongX = roll() < 500;                // vehicles stand side by side along x, long along z
        int dir = rowAlongX ? (roll() < 500 ? 1 : 3) : (roll() < 500 ? 0 : 2);
        int[] anchors = {3, 8, 13};
        for (int k = 0; k < anchors.length; k++)
        {
            if (roll() < 200)
            {
                continue;                                 // an empty bay
            }
            String name = k == 1 && roll() < 500 ? "BlockWreckageMilitary2" : "BlockWreckageMilitary1";
            int x = rowAlongX ? anchors[k] : 8, z = rowAlongX ? 8 : anchors[k];
            face(name, x, z, dir);
        }
        String[] spares = {"BlockBarrel", "BlockBarrel", "BlockTireStack", "BlockTire", "BlockPowerGenerator"};
        int edge = rowAlongX ? (dir == 1 ? 3 : 1) : (dir == 0 ? 2 : 0);   // behind the vehicles' tails
        for (int k = 0; k < 5; k++)
        {
            int a = 2 + roll() % 12;
            int x = edge == 0 ? 14 : edge == 2 ? 1 : a, z = edge == 1 ? 14 : edge == 3 ? 1 : a;
            put(spares[k], x, z, 2 + (roll() & 3));
        }
    }

    /** Two rows of military crates, ammo cases, care packages, water pallets, a spotlight. */
    private void supplyDump()
    {
        boolean alongX = roll() < 500;
        int placed = 0;
        for (int row = 0; row < 2; row++)
        {
            int across = row == 0 ? 5 : 10;
            int front = alongX ? (row == 0 ? 3 : 1) : (row == 0 ? 2 : 0);   // labels toward the aisle between the rows
            for (int a = 3; a <= 12; a++)
            {
                if (roll() < 250)
                {
                    continue;
                }
                int x = alongX ? a : across, z = alongX ? across : a;
                // never stacked: a crate is drawn 1.15 high, one on another would overlap it by 0.15
                placed += face("BlockMilitaryCrate", x, z, front) ? 1 : 0;
            }
        }
        somewhere("BlockAmmoCrateLarge", roll() & 3, 10);
        somewhere("BlockAmmoCrate", roll() & 3, 10);
        for (int k = 0; k < 2; k++)
        {
            somewhere("BlockCarePackage", -1, 10);
        }
        somewhere("BlockWaterPalletTarp", -1, 10);
        somewhere("BlockSpotlight", roll() & 3, 10);
        if (placed == 0)
        {
            somewhere("BlockMilitaryCrate", roll() & 3, 10);
        }
    }

    /** A crashed helicopter in the middle, two dead beside it, an ammo case and debris thrown ahead. */
    private void helicopter()
    {
        Block heli = deci("BlockWreckageMilitary3");
        if (heli == null || !face("BlockWreckageMilitary3", 8, 8, roll() & 3))
        {
            Dress.note("~wasteland helicopter did not fit");
            return;
        }
        for (int k = 0; k < 2; k++)
        {
            somewhere("BlockSkeletonGround", roll() & 3, 12);
        }
        somewhere("BlockAmmoCrate", roll() & 3, 10);
        String[] junk = {"BlockTire", "BlockCardboardBoxes3", "BlockBarrel"};
        for (int k = 0; k < 3; k++)
        {
            somewhere(junk[k], -1, 8);
        }
    }

    /** Body bags in rows, stretchers, medical crates and a hazard light by the edge. */
    private void casualties()
    {
        String[] bags = {"BlockBodyBagBlack", "BlockBodyBagGreen", "BlockBodyBagBlue", "BlockBodyBagOrange"};
        boolean alongX = roll() < 500;
        for (int row = 0; row < 3; row++)
        {
            int across = 4 + row * 3;
            for (int a = 3; a <= 12; a += 2)
            {
                if (roll() < 300)
                {
                    continue;
                }
                int x = alongX ? a : across, z = alongX ? across : a;
                face(bags[roll() % bags.length], x, z, alongX ? 1 : 0);
            }
        }
        for (int k = 0; k < 2; k++)
        {
            somewhere("BlockStretcher", roll() & 3, 10);
        }
        somewhere("BlockMedicalCrate", roll() & 3, 10);
        somewhere("BlockMedicalCrate", roll() & 3, 10);
        somewhere("BlockHazardLight", roll() & 3, 10);
        if (roll() < 500)
        {
            somewhere("BlockSkeletonGround", roll() & 3, 10);
        }
    }
}

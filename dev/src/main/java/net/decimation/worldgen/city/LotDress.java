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
}

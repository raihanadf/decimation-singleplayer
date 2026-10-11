package net.decimation.worldgen.city;

import net.minecraft.block.Block;

import static net.decimation.worldgen.city.LcCity.*;

/**
 * The city dressing of a street chunk (docs/city_dressing.md section 2, research
 * docs/references/city_dressing_research.md, skill decimation-city-street): traffic lights or stop
 * signs at crossings, the sidewalk furniture zone (mailboxes, newspaper boxes, phones, benches,
 * bins, bicycles, power poles, signal cabinets, speed signs), trash and debris. Every prop goes
 * through {@link Dress#place} (fits its drawn size) with a metadata from {@link PropFacing} (where
 * its front looks); rules speak in directions only.
 *
 * Traffic drives on the right. Directions 0 E, 1 S, 2 W, 3 N (StreetLayout.DIR).
 */
final class StreetDress
{
    static final int DOWNTOWN = 0, RESIDENTIAL = 1, WASTELAND = 2;
    private static int debugLeft = 8;

    private final StreetPlan p;
    private final Dress d;
    private final StreetLayout lay;
    private final long seed;
    private final int chx, chz, district, kind;

    private StreetDress(StreetPlan p, Dress d, StreetLayout lay)
    {
        this.p = p;
        this.d = d;
        this.lay = lay;
        this.seed = p.dressSeed();
        this.chx = p.minX() >> 4;
        this.chz = p.minZ() >> 4;
        this.kind = p.dressKind();
        this.district = district(p.dressStyle());
    }

    static int district(String style)
    {
        if (style == null)
        {
            return RESIDENTIAL;
        }
        if (style.endsWith(":deadzone"))
        {
            return WASTELAND;
        }
        if (style.contains("office") || style.contains("hotel") || style.contains("retail") || style.endsWith(":standardcity"))
        {
            return DOWNTOWN;
        }
        return RESIDENTIAL;
    }

    static Dress dress(final StreetPlan p)
    {
        final int[] tmp = new int[1];
        Dress.Base base = new Dress.Base()
        {
            public Block block(int x, int y, int z)
            {
                return p.baseAt(x, y, z, tmp);
            }

            public int meta(int x, int y, int z)
            {
                p.baseAt(x, y, z, tmp);
                return tmp[0];
            }
        };
        Dress d = new Dress(base, 16, Math.max(12, p.height()), 16);
        if (p.dressOnWater())
        {
            return d;                                      // a bridge: its railings, nothing on the deck
        }
        StreetLayout lay = new StreetLayout(base, p.dressSidewalk());
        StreetDress s = new StreetDress(p, d, lay);
        Dress.note("~street kind" + s.kind + (p.dressPiece() ? " part" : " ours") + " district" + s.district);
        if (s.kind == 2)
        {
            s.crossing();
        }
        else
        {
            s.straight();
        }
        Dress.logTotals();
        return d;
    }

    // ------------------------------------------------------------------ helpers

    /** A random number 0..999 for this chunk and a rule (salt). */
    private int roll(long salt)
    {
        return (int) (hl(seed ^ salt, chx, chz) % 1000);
    }

    private int roll(long salt, int k)
    {
        return (int) (hl(seed ^ salt, chx * 31 + k, chz * 17 - k) % 1000);
    }

    private static Block deci(String name)
    {
        Block b = Block.getBlockFromName("deci:" + name);
        return b == net.minecraft.init.Blocks.air ? null : b;
    }

    /** Places a prop whose front looks dir on a walk / road column (its stand layer). */
    private boolean put(String name, int x, int z, int dir)
    {
        Block b = deci(name);
        if (b == null || x < 0 || z < 0 || x > 15 || z > 15)
        {
            return false;
        }
        int m = PropFacing.meta(b, dir);
        return m >= 0 && d.place(b, m, x, lay.stand[x][z], z, 0);
    }

    /** Places a long prop with its long axis along x (or z). */
    private boolean putAlong(String name, int x, int z, boolean alongX, int turn)
    {
        Block b = deci(name);
        if (b == null)
        {
            return false;
        }
        int m = PropFacing.along(b, alongX, turn);
        return m >= 0 && d.place(b, m, x, lay.stand[x][z], z, 0);
    }

    private boolean walk(int x, int z)
    {
        return x >= 0 && z >= 0 && x <= 15 && z <= 15 && lay.type[x][z] == StreetLayout.WALK;
    }

    // ------------------------------------------------------------------ straight streets

    /**
     * A straight street chunk: per side, the curb (furniture) row and the frontage row. The clear
     * path between them stays empty (research: a 5 wide walk keeps 2..3 clear blocks, a 3 wide
     * one 1). The first chunk past a crossing carries the corner cluster (mailbox, bin, speed sign).
     */
    private void straight()
    {
        boolean alongZ = kind == 0;
        for (int side = 0; side < 2; side++)
        {
            int[] rows = rows(side, alongZ);           // {curb across, frontage across}, -1 when missing
            if (rows == null)
            {
                Dress.note("~street side without walk");
                if (debugLeft-- > 0)
                {
                    StringBuilder sb = new StringBuilder();
                    for (int k = 0; k < 6; k++)
                    {
                        int across = side == 0 ? k : 15 - k;
                        int x = alongZ ? across : 8, z = alongZ ? 8 : across;
                        sb.append(" [").append(lay.type[x][z]);
                        for (int y = 0; y < 3; y++)
                        {
                            Block b = d.block(x, y, z);
                            sb.append(' ').append(b == null ? "." : String.valueOf(Block.blockRegistry.getNameForObject(b)).replace("minecraft:", ""));
                        }
                        sb.append(']');
                    }
                    cpw.mods.fml.common.FMLLog.info("[deciworldgen] dress no walk at %d,%d kind %d %s:%s", chx << 4, chz << 4,
                                                    kind, p.dressStyle(), sb);
                }
                continue;
            }
            int curb = rows[0], front = rows[1];
            int width = Math.abs(front - curb) + 1;
            int awayRoad = side == 0 ? (alongZ ? 2 : 3) : (alongZ ? 0 : 1);   // toward the buildings
            int toRoad = (awayRoad + 2) & 3;
            int towardNear = nearCrossing(alongZ);           // -1 none, else the along index of the crossing end
            Dress.note(towardNear >= 0 ? "~street side next to a crossing" : "~street side mid block");
            long sideSalt = side * 0x9E3779B97F4A7C15L;
            // speed limit sign: right curb of the traffic leaving the crossing, facing that traffic
            if (towardNear >= 0 && district != WASTELAND && roll(0x5350454544L ^ sideSalt) < 300)
            {
                int dirTraffic = trafficOnSide(side, alongZ);       // direction cars on this side travel
                boolean leaving = towardNear == 0 ? isPositive(dirTraffic) : !isPositive(dirTraffic);
                if (leaving)
                {
                    int a = towardNear == 0 ? 5 : 10;
                    String sign = district == DOWNTOWN ? pick(new String[] {"BlockRoadsign30", "BlockRoadsign40"}, 0x53)
                        : pick(new String[] {"BlockRoadsign20", "BlockRoadsign30"}, 0x53);
                    place(sign, curb, a, alongZ, (dirTraffic + 2) & 3);
                }
            }
            // corner cluster in the first chunk past a crossing: mailbox + bin, newspaper boxes downtown
            if (towardNear >= 0 && district != WASTELAND)
            {
                int a0 = towardNear == 0 ? 3 : 12, step = towardNear == 0 ? 1 : -1;
                int a = a0;
                if (roll(0x4D41494CL ^ sideSalt) < (district == DOWNTOWN ? 450 : 200))
                {
                    if (place("BlockMailbox", curb, a, alongZ, awayRoad))
                    {
                        a += 2 * step;
                    }
                }
                if (district == DOWNTOWN && roll(0x4E455753L ^ sideSalt) < 350)
                {
                    int n = 2 + roll(0x4E4557L ^ sideSalt) % 3;
                    for (int k = 0; k < n; k++)
                    {
                        place(k % 2 == 0 ? "BlockNewsStand1" : "BlockNewsStand2", curb, a + k * step, alongZ, awayRoad);
                    }
                    a += (n + 1) * step;
                }
                if (roll(0x42494EL ^ sideSalt) < 500)
                {
                    place(district == DOWNTOWN ? "BlockStreetBin" : "BlockTrashcan", curb, a, alongZ, awayRoad);
                }
            }
            // mid block: bench + bin, bicycle, power poles residential. No phones here: the payphone cabinet
            // hangs on a wall (prop_facing.tsv mount "wall") and the tall phone box has no verified front.
            if (width >= 4 && district != WASTELAND && roll(0x42454E43L ^ sideSalt) < 220)
            {
                if (place("BlockStreetBench", curb, 6, alongZ, awayRoad))
                {
                    place("BlockStreetBin", curb, 8, alongZ, awayRoad);
                }
            }
            if (district != WASTELAND && roll(0x4249434BL ^ sideSalt) < 120)
            {
                int a = 11 + roll(0x424943L ^ sideSalt) % 3;
                placeAlong("BlockBicycle", curb, a, alongZ, alongZ ? false : true, roll(0x4249L ^ sideSalt));
            }
            if (district == RESIDENTIAL && poleSide(alongZ) == side)
            {
                placeAlong("BlockPowerPole", curb, 8, alongZ, alongZ, 0);   // crossarm across the street
            }
            // trash: a cluster of bags against the frontage or a bin
            if (roll(0x42414753L ^ sideSalt) < (district == WASTELAND ? 600 : district == DOWNTOWN ? 350 : 200))
            {
                int a = 2 + roll(0x424147L ^ sideSalt) % 12;
                int n = 2 + roll(0x4241L ^ sideSalt) % 4;
                for (int k = 0; k < n; k++)
                {
                    place(k % 2 == 0 ? "BlockTrashBag1" : "BlockTrashBag2", front, a + k, alongZ, (k + roll(0x42L ^ sideSalt)) & 3);
                }
            }
            // wasteland: barrels, burnt clutter, a skeleton now and then
            if (district == WASTELAND)
            {
                if (roll(0x4241524CL ^ sideSalt) < 400)
                {
                    place("BlockBarrel", front, 4 + roll(0x424152L ^ sideSalt) % 8, alongZ, toRoad);
                }
                if (roll(0x534B454CL ^ sideSalt) < 120)
                {
                    place("BlockSkeletonGround", front, 3 + roll(0x534B45L ^ sideSalt) % 10, alongZ, toRoad);
                }
            }
        }
        facades(alongZ);
        if (!jam(alongZ))
        {
            roadScene(alongZ);
        }
    }

    // ------------------------------------------------------------------ facades

    /**
     * Shop signs and graffiti on the buildings beside the street (research "Street facing ground
     * floors": the sign over the shop door at y + 3 facing the road; graffiti low on blank walls).
     * The wall belongs to the building's chunk, the air in front of it to this street: the picture
     * hangs in this street's frontage cell, its back on the building's wall (read through
     * StreetPlan.neighbourBuilding), its front toward the road.
     */
    private void facades(boolean alongZ)
    {
        int ground = p.dressGround();
        for (int side = 0; side < 2; side++)
        {
            // toward the buildings: street along z, side 0 is the west side
            int away = side == 0 ? (alongZ ? 2 : 3) : (alongZ ? 0 : 1);
            int[] toFronts = {2, 3, 0, 1};               // DIR (E S W N) -> neighbourBuilding side (W N E S order)
            BuildingPlan b = p.neighbourBuilding(toFronts[away]);
            if (b == null)
            {
                continue;
            }
            int edge = side == 0 ? 0 : 15;
            int wx = StreetLayout.DIR[away][0], wz = StreetLayout.DIR[away][1];
            int kindB = b.dressKind();
            boolean shop = kindB == BuildingDress.SHOP || kindB == BuildingDress.FOOD;
            Dress.note(shop ? "~facade shop" : "~facade other");
            int signs = 0, tags = 0;
            int maxTags = district == WASTELAND ? 3 : district == DOWNTOWN ? 1 : 2;
            for (int k = 0; k < 16; k++)
            {
                int along = (k * 7 + roll(0x46414341L ^ side)) & 15;   // a shuffled order along the facade
                int x = alongZ ? edge : along, z = alongZ ? along : edge;
                if (!walk(x, z))
                {
                    continue;
                }
                int s = lay.stand[x][z];
                int worldX = p.minX() + x + wx, worldZ = p.minZ() + z + wz, y0 = ground + s;
                Block w0 = b.baseBlockAt(worldX, y0, worldZ), w1 = b.baseBlockAt(worldX, y0 + 1, worldZ);
                Block w2 = b.baseBlockAt(worldX, y0 + 2, worldZ), w3 = b.baseBlockAt(worldX, y0 + 3, worldZ);
                boolean door = w0 != null && PropGeom.isDoor(w0);
                boolean opening = door || w0 != null && w0.getMaterial() == net.minecraft.block.material.Material.glass
                    || w1 != null && w1.getMaterial() == net.minecraft.block.material.Material.glass;
                // the shop sign: over the door or the shop window if the facade has one here, else
                // (second pass) on any solid stretch at sign height
                if (shop && signs == 0 && opaque(w2) && opaque(w3) && (opening || k >= 12))
                {
                    if (wallPicture(shopSign(b), x, s + 2, z, away))
                    {
                        signs++;
                        Dress.note("~facade shop sign");
                    }
                }
                else if (tags < maxTags && opaque(w0) && opaque(w1) && roll(0x54414753L, k + side * 16) < 220)
                {
                    String[] art = {"BlockGraffiti_1", "BlockGraffiti_2", "BlockGraffiti_3", "BlockGraffiti_4",
                                    "BlockGraffiti_5", "BlockGraffiti_6", "BlockGraffiti_7", "BlockGraffiti_8",
                                    "BlockGraffiti_9", "BlockGraffiti_10", "BlockGraffiti_11", "BlockGraffiti_12"};
                    if (wallPicture(art[roll(0x415254L, k) % art.length], x, s + 1, z, away))
                    {
                        tags++;
                        Dress.note("~facade graffiti");
                    }
                }
            }
        }
    }

    private static boolean opaque(Block b)
    {
        return b != null && b.isOpaqueCube();
    }

    /** A flat picture in (x, y, z) of this chunk, its back on the wall toward wallDir. */
    private boolean wallPicture(String name, int x, int y, int z, int wallDir)
    {
        Block b = deci(name);
        if (b == null || d.block(x, y, z) != null)
        {
            return false;
        }
        int m = PropFacing.meta(b, (wallDir + 2) & 3);
        return m >= 0 && d.place(b, m, x, y, z, 1);
    }

    /** The sign of the shop kind (Decimation's logos): food chains for food, the gun shops for guns. */
    private String shopSign(BuildingPlan b)
    {
        String n = b.describe().toLowerCase();
        if (n.contains("gun"))
        {
            return roll(0x47554EL) < 500 ? "BlockSign_Gunsrus" : "BlockSign_Decimunition";
        }
        if (b.dressKind() == BuildingDress.FOOD)
        {
            return roll(0x464F4F44L) < 500 ? "BlockSign_Minedonalds" : "BlockSign_Mineway";
        }
        return "BlockSign_Minebay";
    }

    // ------------------------------------------------------------------ traffic jams

    /**
     * Near a city exit (the street runs out of the city within 3 chunks) the outbound lane is a
     * queue of abandoned cars (research: outbound lanes 70..90 % full toward the exit, a car per 7..8
     * blocks), headlights toward the exit. Returns true when this chunk is part of a jam.
     */
    private boolean jam(boolean alongZ)
    {
        int dx = alongZ ? 0 : 1, dz = alongZ ? 1 : 0;
        int exit = 0;                                     // +1: the exit lies toward +along, -1 toward -along
        for (int k = 1; k <= 3 && exit == 0; k++)
        {
            if (!isCity(seed, Math.floorDiv(chx + dx * k, CELL), Math.floorDiv(chz + dz * k, CELL)))
            {
                exit = 1;
            }
            else if (!isCity(seed, Math.floorDiv(chx - dx * k, CELL), Math.floorDiv(chz - dz * k, CELL)))
            {
                exit = -1;
            }
        }
        if (exit == 0 || district == WASTELAND && roll(0x4A414DL) < 500 || roll(0x4A414D32L) >= 800)
        {
            return false;
        }
        int lo = -1, hi = -1;
        for (int k = 0; k < 16; k++)
        {
            int x = alongZ ? k : 8, z = alongZ ? 8 : k;
            if (lay.type[x][z] == StreetLayout.ROAD)
            {
                if (lo < 0)
                {
                    lo = k;
                }
                hi = k;
            }
        }
        if (lo < 0 || hi - lo < 5)
        {
            return false;
        }
        // outbound direction and its lane (right hand traffic); a 6 wide road holds one car centred
        int dir = alongZ ? (exit > 0 ? 1 : 3) : (exit > 0 ? 0 : 2);
        int mid = (lo + hi) / 2;
        int lane = hi - lo >= 8 ? (laneDir(alongZ, lo + 1, mid) == dir ? mid - 2 : mid + 3) : (hi - lo >= 6 ? mid : mid + 1);
        String[] cars = {"BlockWreckage1", "BlockWreckage2", "BlockWreckage3", "BlockWreckage4", "BlockWreckage5",
                         "BlockTruckWreckage2", "BlockTruckWreckage4", "BlockTruckWreckage6"};
        int placed = 0;
        for (int along : new int[] {3, 11})
        {
            String name = cars[roll(0x4A43L, along) % cars.length];
            Block c = deci(name);
            if (c != null && road(name, lane, along, alongZ, PropFacing.meta(c, dir)))
            {
                placed++;
            }
        }
        // a stalled driver's things beside a car now and then
        if (placed > 0 && roll(0x5354414CL) < 250)
        {
            road(roll(0x53544CL) < 500 ? "BlockCardboardBoxes1" : "BlockTrashBag2", lane + (lane <= mid ? -2 : 2), 7, alongZ, 2);
        }
        Dress.note("~jam chunk");
        if (placed > 0)
        {
            cpw.mods.fml.common.FMLLog.info("[deciworldgen] dress jam at %d,%d heading %s", chx << 4, chz << 4, "ESWN".charAt(dir));
        }
        return placed > 0;
    }

    // ------------------------------------------------------------------ road scenes

    /**
     * At most one scene in the road of a straight chunk (research "abandoned city": scenes that
     * explain themselves, at most one hero scene per chunk, 1 chunk in 3 quiet): a police roadblock,
     * a military checkpoint in the wasteland, a crash with its debris, road works. Cars and trucks
     * point their headlights the way their lane runs (right hand traffic); a car across the road
     * lies with its long axis across it.
     */
    private void roadScene(boolean alongZ)
    {
        if (p.dressScene())
        {
            return;                                      // a district scene already stands in this road
        }
        int lo = -1, hi = -1;
        for (int k = 0; k < 16; k++)
        {
            int x = alongZ ? k : 8, z = alongZ ? 8 : k;
            if (lay.type[x][z] == StreetLayout.ROAD)
            {
                if (lo < 0)
                {
                    lo = k;
                }
                hi = k;
            }
        }
        if (lo < 0 || hi - lo < 5)
        {
            return;
        }
        int mid = (lo + hi) / 2;
        int r = roll(0x524F4144L);
        if (district == WASTELAND)
        {
            if (r < 140)
            {
                checkpoint(alongZ, lo, hi);
            }
            else if (r < 300)
            {
                crash(alongZ, lo, hi, true);
            }
            return;
        }
        if (r < 45)
        {
            roadblock(alongZ, lo, hi);
        }
        else if (r < 130)
        {
            crash(alongZ, lo, hi, false);
        }
        else if (r < 175)
        {
            roadWorks(alongZ, lo, hi);
        }
    }

    /** A road prop at (across, along) of this straight chunk; m = metadata (from PropFacing). */
    private boolean road(String name, int across, int along, boolean alongZ, int m)
    {
        Block b = deci(name);
        int x = alongZ ? across : along, z = alongZ ? along : across;
        if (b == null || m < 0 || x < 0 || z < 0 || x > 15 || z > 15 || lay.type[x][z] != StreetLayout.ROAD)
        {
            return false;
        }
        return d.place(b, m, x, lay.stand[x][z], z, 2);
    }

    /** Direction the lane at across runs (right hand traffic): low across half one way, high the other. */
    private int laneDir(boolean alongZ, int across, int mid)
    {
        boolean low = across <= mid;
        return alongZ ? (low ? 1 : 3) : (low ? 2 : 0);
    }

    /**
     * Police roadblock: a police car across the road, a sawhorse line and cones closing the rest, one
     * lane left open on the right of the approaching traffic (research: block the lane plus one).
     */
    private void roadblock(boolean alongZ, int lo, int hi)
    {
        String car = roll(0x504F4CL) < 500 ? "BlockWreckagePolice1" : "BlockWreckagePolice3";
        Block c = deci(car);
        int across = (lo + hi) / 2;
        // long axis across the road
        if (c == null || !road(car, across, 7, alongZ, PropFacing.along(c, alongZ, roll(0x504F4C49L))))
        {
            return;
        }
        Block hb = deci("BlockHazardbarrier");
        for (int k = lo; k <= hi; k++)
        {
            road("BlockHazardbarrier", k, 3, alongZ, hb == null ? -1 : PropFacing.along(hb, alongZ, 0));
            if (k % 2 == 0)
            {
                road("BlockCone", k, 11, alongZ, 2);
            }
        }
    }

    /**
     * Wasteland checkpoint: a jersey barrier line across the road with a gap a car's width, hedgehogs
     * before it, sandbag stacks at the ends on the sidewalk.
     */
    private void checkpoint(boolean alongZ, int lo, int hi)
    {
        Block bar = deci("BlockBarrier");
        if (bar == null)
        {
            return;
        }
        int gap = roll(0x474150L) < 500 ? lo + 1 : hi - 2;   // 2 wide gap on one side
        for (int k = lo; k <= hi; k += 2)
        {
            if (k == gap || k + 1 == gap)
            {
                continue;
            }
            road("BlockBarrier", k, 8, alongZ, PropFacing.along(bar, alongZ, 0));
        }
        for (int k = lo + 1; k <= hi; k += 3)
        {
            road("BlockHedgehog", k, 3, alongZ, 2);
        }
        Block sand = deci("BlockSandbagStack");
        if (sand != null)
        {
            for (int s = 0; s < 2; s++)
            {
                int across = s == 0 ? lo - 1 : hi + 1;
                int x = alongZ ? across : 8, z = alongZ ? 8 : across;
                if (walk(x, z))
                {
                    d.place(sand, 0, x, lay.stand[x][z], z, 0);
                }
            }
        }
    }

    /**
     * A crash: one car stopped in its lane at an angle of 90 degrees to another (T bone), or a car
     * against the curb, with debris spilled ahead of it (tyres, boxes, a bag; research "debris cone
     * downstream"); in the wasteland a body nearby.
     */
    private void crash(boolean alongZ, int lo, int hi, boolean wasteland)
    {
        int mid = (lo + hi) / 2;
        String[] cars = {"BlockWreckage1", "BlockWreckage2", "BlockWreckage3", "BlockWreckage4", "BlockWreckage5",
                         "BlockTruckWreckage1", "BlockTruckWreckage3", "BlockTruckWreckage5"};
        String name = cars[roll(0x43415221L) % cars.length];
        Block c = deci(name);
        if (c == null)
        {
            return;
        }
        int lane = roll(0x4C414E45L) < 500 ? mid - 1 : mid + 2;
        int dir = laneDir(alongZ, lane, mid);
        if (!road(name, lane, 7, alongZ, PropFacing.meta(c, dir)))
        {
            return;
        }
        // the second car across the road, nose into the first one's side
        String other = cars[roll(0x43415222L) % 5];
        Block o = deci(other);
        if (o != null && roll(0x54424F4EL) < 600)
        {
            int across = lane < mid ? hi - 1 : lo + 1;
            road(other, across, 7, alongZ, PropFacing.along(o, alongZ, roll(0x54424FL)));
        }
        // debris downstream of the travel direction
        int ahead = dir == 1 || dir == 0 ? 1 : -1;
        String[] junk = {"BlockTire", "BlockCardboardBoxes1", "BlockTrashBag1", "BlockTire", "BlockCardboardBoxes3"};
        int n = 2 + roll(0x4A554E4BL) % 3;
        for (int k = 0; k < n; k++)
        {
            int along = 7 + ahead * (4 + k);
            int across = lane + (k % 3) - 1;
            road(junk[(k + roll(0x4A55L)) % junk.length], across, along, alongZ, 2 + (roll(0x4AL + k) & 3));
        }
        if (wasteland && roll(0x424F4459L) < 400)
        {
            Block bag = deci("BlockSkeletonGround");
            if (bag != null)
            {
                road("BlockSkeletonGround", lane + (lane < mid ? -1 : 1), 7 - ahead * 4, alongZ, PropFacing.meta(bag, (dir + 2) & 3));
            }
        }
    }

    /** Road works: the sign trailer, a cone taper closing one lane, a sawhorse at its end. */
    private void roadWorks(boolean alongZ, int lo, int hi)
    {
        int mid = (lo + hi) / 2;
        boolean lowLane = roll(0x574F524BL) < 500;
        int lane = lowLane ? lo + 1 : hi - 1;
        int dir = laneDir(alongZ, lane, mid);
        Block screen = deci("BlockMaintenanceScreen");
        // the sign trailer faces the traffic coming at it
        if (screen != null)
        {
            int along = dir == 1 || dir == 0 ? 3 : 12;
            road("BlockMaintenanceScreen", lane, along, alongZ, PropFacing.meta(screen, (dir + 2) & 3));
        }
        for (int k = 0; k < 5; k++)
        {
            int along = dir == 1 || dir == 0 ? 6 + k * 2 : 9 - k * 2;
            int across = lowLane ? lo + Math.min(k, 2) : hi - Math.min(k, 2);
            road("BlockCone", across, along, alongZ, 2);
        }
        Block hb = deci("BlockHazardbarrier");
        if (hb != null)
        {
            road("BlockHazardbarrier", lane, dir == 1 || dir == 0 ? 14 : 1, alongZ, PropFacing.along(hb, alongZ, 0));
        }
    }

    /** Along offsets tried in order: the part may already have a lamp, a bench or a planter on the first. */
    private static final int[] NEAR = {0, 1, -1, 2, -2, 3, -3};

    /** A prop on (across, along) of a straight chunk, front toward dir; slides up to 3 along to fit. */
    private boolean place(String name, int across, int along, boolean alongZ, int dir)
    {
        for (int o : NEAR)
        {
            int a = along + o, x = alongZ ? across : a, z = alongZ ? a : across;
            if (walk(x, z) && put(name, x, z, dir))
            {
                return true;
            }
        }
        return false;
    }

    private boolean placeAlong(String name, int across, int along, boolean alongZ, boolean longAlongX, int turn)
    {
        for (int o : NEAR)
        {
            int a = along + o, x = alongZ ? across : a, z = alongZ ? a : across;
            if (walk(x, z) && putAlong(name, x, z, longAlongX, turn))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * The curb row and the frontage row of one side (side 0: the low across side), read from the
     * layout at the middle of the chunk; null when that side has no sidewalk.
     */
    private int[] rows(int side, boolean alongZ)
    {
        int curb = -1, front = -1;
        for (int k = 0; k < 16; k++)
        {
            int across = side == 0 ? k : 15 - k;
            int x = alongZ ? across : 8, z = alongZ ? 8 : across;
            int t = lay.type[x][z];
            if (t == StreetLayout.ROAD)
            {
                break;
            }
            if (t == StreetLayout.WALK || t == StreetLayout.BUSY)
            {
                if (front < 0)
                {
                    front = across;
                }
                curb = across;
            }
        }
        return curb < 0 ? null : new int[] {curb, front};
    }

    /** 0 if the chunk at the low along end is a crossing, 15 at the high end, -1 neither. */
    private int nearCrossing(boolean alongZ)
    {
        int dx = alongZ ? 0 : 1, dz = alongZ ? 1 : 0;
        if (isCrossing(chx - dx, chz - dz))
        {
            return 0;
        }
        if (isCrossing(chx + dx, chz + dz))
        {
            return 15;
        }
        return -1;
    }

    private boolean isCrossing(int x, int z)
    {
        int cx = Math.floorDiv(x, CELL), cz = Math.floorDiv(z, CELL);
        return isCity(seed, cx, cz) && isStreetChunk(seed, x, z) && Math.floorMod(x, CELL) == 0
            && Math.floorMod(z, CELL) == 0;
    }

    /** Direction cars travel on a side (right hand traffic): street along z, west side (0) goes south. */
    private static int trafficOnSide(int side, boolean alongZ)
    {
        if (alongZ)
        {
            return side == 0 ? 1 : 3;                    // west half southbound, east half northbound
        }
        return side == 0 ? 2 : 0;                        // north half westbound, south half eastbound
    }

    private static boolean isPositive(int dir)
    {
        return dir == 0 || dir == 1;
    }

    /** Power lines run on one side of a whole street line. */
    private int poleSide(boolean alongZ)
    {
        return (int) (hl(seed ^ 0x504F4C45L, alongZ ? chx : 0, alongZ ? 0 : chz) & 1);
    }

    private String pick(String[] names, long salt)
    {
        return names[roll(salt) % names.length];
    }

    // ------------------------------------------------------------------ crossings

    /**
     * A crossing chunk. Downtown: a traffic light pole on every corner that has a sidewalk, each
     * on the FAR RIGHT corner of the traffic it serves, lenses toward that traffic, its arm then
     * over that approach (research "Traffic signals"); a signal cabinet on one corner. Elsewhere a
     * stop sign on the right curb of each approach, facing the approaching drivers. The wasteland
     * keeps its dead lights only at a few crossings.
     */
    private void crossing()
    {
        // corners: 0 NE (serves northbound, lenses S), 1 SE (eastbound, lenses W), 2 SW (southbound, lenses N),
        // 3 NW (westbound, lenses E)
        int[][] corner = {{1, -1}, {1, 1}, {-1, 1}, {-1, -1}};
        int[] lens = {1, 2, 3, 0};
        boolean lights = district == DOWNTOWN || district == RESIDENTIAL && roll(0x4C494748L) < 250
            || district == WASTELAND && roll(0x4C494748L) < 300;
        boolean cabinet = false;
        for (int c = 0; c < 4; c++)
        {
            int sx = corner[c][0], sz = corner[c][1];
            if (lights)
            {
                int[] cell = cornerCell(sx, sz, 2);
                if (cell != null && put("BlockTrafficLights", cell[0], cell[1], lens[c]))
                {
                    if (!cabinet && roll(0x4341424EL, c) < 600)
                    {
                        int[] cab = cornerCell(sx, sz, 3);
                        if (cab != null)
                        {
                            cabinet = put("BlockElectricBoxBin", cab[0], cab[1], cornerAway(sx, sz, cab));
                        }
                    }
                    continue;
                }
            }
            if (district != WASTELAND)
            {
                stopSign(c);
            }
        }
        crossingCrash();
        // a mailbox on one corner downtown
        if (district == DOWNTOWN && roll(0x4D41494CL) < 400)
        {
            int c = roll(0x4D4149L) % 4;
            int[] cell = cornerCell(corner[c][0], corner[c][1], 1);
            if (cell != null)
            {
                put("BlockMailbox", cell[0], cell[1], cornerAway(corner[c][0], corner[c][1], cell));
            }
        }
    }

    /**
     * A T-bone crash in the middle of the crossing (research: 4 in 10 crashes at intersections):
     * one car along one street, a second along the other with its nose at the first one's side.
     */
    private void crossingCrash()
    {
        if (roll(0x58435241L) >= (district == WASTELAND ? 250 : 110))
        {
            return;
        }
        String[] cars = {"BlockWreckage1", "BlockWreckage2", "BlockWreckage3", "BlockWreckage4", "BlockWreckage5",
                         "BlockWreckagePolice2", "BlockTruckWreckage1"};
        String a = cars[roll(0x58434131L) % cars.length], b = cars[roll(0x58434132L) % 5];
        Block ca = deci(a), cb = deci(b);
        if (ca == null || cb == null)
        {
            return;
        }
        boolean aAlongX = roll(0x584158L) < 500;
        int dirA = aAlongX ? (roll(0x5844L) < 500 ? 0 : 2) : (roll(0x5844L) < 500 ? 1 : 3);
        // a car is 6.9 long: the first one sits 2 off the middle, away from the side the second comes
        // from, so the second fits inside this chunk with its nose 0.7 from the first one's side
        int side = roll(0x58534944L) < 500 ? 1 : -1;
        int ax = aAlongX ? 8 : 8 - 2 * side, az = aAlongX ? 8 - 2 * side : 8;
        if (!crossRoad(ca, PropFacing.meta(ca, dirA), ax, az))
        {
            return;
        }
        cpw.mods.fml.common.FMLLog.info("[deciworldgen] dress crossing crash at %d,%d", chx << 4, chz << 4);
        // the other car comes from that side, nose toward the first: try a few spots
        int dirB = aAlongX ? (side > 0 ? 3 : 1) : (side > 0 ? 2 : 0);
        for (int gap = 4; gap <= 6; gap++)
        {
            int bx = aAlongX ? 8 : 8 - 2 * side + side * (gap + 2), bz = aAlongX ? 8 - 2 * side + side * (gap + 2) : 8;
            if (crossRoad(cb, PropFacing.meta(cb, dirB), bx, bz))
            {
                break;
            }
        }
        for (int k = 0; k < 3; k++)
        {
            int x = 8 + (roll(0x44454231L, k) % 7) - 3, z = 8 + (roll(0x44454232L, k) % 7) - 3;
            crossRoad(deci(k == 0 ? "BlockTire" : k == 1 ? "BlockCardboardBoxes3" : "BlockTrashBag1"), 2 + k, x, z);
        }
    }

    private boolean crossRoad(Block b, int m, int x, int z)
    {
        return b != null && m >= 0 && x >= 0 && z >= 0 && x <= 15 && z <= 15 && lay.type[x][z] == StreetLayout.ROAD
            && d.place(b, m, x, lay.stand[x][z], z, 2);
    }

    /**
     * A walk cell in a corner quarter (sx, sz: +1 east / south half, -1 west / north half) whose
     * distance to the road in both directions is closest to want (1 = right at the curb).
     */
    private int[] cornerCell(int sx, int sz, int want)
    {
        int[] best = null;
        int score = 99;
        for (int x = 0; x < 16; x++)
        {
            for (int z = 0; z < 16; z++)
            {
                if ((x >= 8) != (sx > 0) || (z >= 8) != (sz > 0) || !walk(x, z))
                {
                    continue;
                }
                int dX = roadSteps(x, z, -sx, 0), dZ = roadSteps(x, z, 0, -sz);
                if (dX > 6 || dZ > 6)
                {
                    continue;
                }
                int sc = Math.abs(dX - want) + Math.abs(dZ - want);
                if (sc < score)
                {
                    score = sc;
                    best = new int[] {x, z};
                }
            }
        }
        return best;
    }

    /** Steps from (x, z) toward (dx, dz) to the first road column, 99 if none. */
    private int roadSteps(int x, int z, int dx, int dz)
    {
        for (int k = 1; k <= 8; k++)
        {
            int nx = x + dx * k, nz = z + dz * k;
            if (nx < 0 || nz < 0 || nx > 15 || nz > 15)
            {
                return 99;
            }
            if (lay.type[nx][nz] == StreetLayout.ROAD)
            {
                return k;
            }
        }
        return 99;
    }

    /** From a corner cell, the direction away from the crossing's middle (toward the buildings). */
    private int cornerAway(int sx, int sz, int[] cell)
    {
        // the nearer road decides which street the cell belongs to; face away from that road
        int dX = roadSteps(cell[0], cell[1], -sx, 0), dZ = roadSteps(cell[0], cell[1], 0, -sz);
        return dX <= dZ ? (sx > 0 ? 0 : 2) : (sz > 0 ? 1 : 3);
    }

    /**
     * Stop sign for the approach whose far right corner is c: it stands on the NEAR right corner of
     * that traffic, on the curb of the leg it arrives on, facing the drivers (research "Regulatory
     * road signs": northbound from the south: south leg, east curb, faces south).
     */
    private void stopSign(int c)
    {
        // approach: 0 northbound (south leg, east curb, faces S), 1 eastbound (west leg, south curb, faces W),
        // 2 southbound (north leg, west curb, faces N), 3 westbound (east leg, north curb, faces E)
        int[][] spot = {{1, 1}, {-1, 1}, {-1, -1}, {1, -1}};   // corner quarter of the near right corner
        int[] face = {1, 2, 3, 0};
        int sx = spot[c][0], sz = spot[c][1];
        int[] cell = null;
        int score = 99;
        for (int x = 0; x < 16; x++)
        {
            for (int z = 0; z < 16; z++)
            {
                if ((x >= 8) != (sx > 0) || (z >= 8) != (sz > 0) || !walk(x, z))
                {
                    continue;
                }
                // on the curb of the leg the traffic arrives on, as far out along it as possible
                boolean legAlongZ = c == 0 || c == 2;
                int toLeg = legAlongZ ? roadSteps(x, z, -sx, 0) : roadSteps(x, z, 0, -sz);
                int out = legAlongZ ? (sz > 0 ? z : 15 - z) : (sx > 0 ? x : 15 - x);
                if (toLeg != 1)
                {
                    continue;
                }
                int sc = 15 - out;
                if (sc < score)
                {
                    score = sc;
                    cell = new int[] {x, z};
                }
            }
        }
        if (cell != null && roll(0x53544F50L, c) < 850)
        {
            put("BlockRoadsignStop", cell[0], cell[1], face[c]);
        }
    }
}

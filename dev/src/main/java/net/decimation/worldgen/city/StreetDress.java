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
        if (s.kind == 2)
        {
            s.crossing();
        }
        else
        {
            s.straight();
        }
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
                continue;
            }
            int curb = rows[0], front = rows[1];
            int width = Math.abs(front - curb) + 1;
            int awayRoad = side == 0 ? (alongZ ? 2 : 3) : (alongZ ? 0 : 1);   // toward the buildings
            int toRoad = (awayRoad + 2) & 3;
            int towardNear = nearCrossing(alongZ);           // -1 none, else the along index of the crossing end
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
            // mid block: phone downtown on wide walks, bench + bin, bicycle, power poles residential
            if (district == DOWNTOWN && width >= 4 && roll(0x50484F4EL ^ sideSalt) < 150)
            {
                place(roll(0x50484FL ^ sideSalt) < 500 ? "BlockPhonebox" : "BlockPhonebooth", curb, 8, alongZ, awayRoad);
            }
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
            if (district == RESIDENTIAL && poleSide(alongZ) == side && ((alongZ ? chz : chx) & 1) == 0)
            {
                placeAlong("BlockPowerPole", curb, 8, alongZ, alongZ, 0);
                if (roll(0x45424F58L ^ sideSalt) < 400)
                {
                    place("BlockElectricBox2", curb, 10, alongZ, awayRoad);
                }
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
    }

    /** A prop on (across, along) of a straight chunk, front toward dir. */
    private boolean place(String name, int across, int along, boolean alongZ, int dir)
    {
        int x = alongZ ? across : along, z = alongZ ? along : across;
        return walk(x, z) && put(name, x, z, dir);
    }

    private boolean placeAlong(String name, int across, int along, boolean alongZ, boolean longAlongX, int turn)
    {
        int x = alongZ ? across : along, z = alongZ ? along : across;
        return walk(x, z) && putAlong(name, x, z, longAlongX, turn);
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

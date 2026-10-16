#!/usr/bin/env python3
"""City sizes (worldgen/CityLayout.java) and the sector map (Sectors.java) recomputed from the seed.

    python3 tools/citylayout.py SEED [RADIUS_CELLS]

Prints what `java -cp dev/build/classes/java/main net.decimation.worldgen.CityLayout SEED [RADIUS]`
prints: the share of city land, cities per size, wasteland per city and, when RADIUS <= 8, the map of
the middle (# city, W wasteland, M military beside a city, . other). Worlds since v0.60.0 cut the land
into cells of CELL x CELL sectors, each holding one compact city of 1 to 16 sectors (plan()).
java.util.Random, Java's 64 bit arithmetic, the float rolls and Java's %.1f rounding are reimplemented
bit for bit. Sector coordinates are assumed in the reachable range (|rx| < 2^27): Java's int geometry
only wraps near 2^31.

region_sector() is Sectors.regionSector: the sized layout while ON (CityLayout.on(), worlds since
v0.60.0), the old one after set_on(False) (older worlds: every sector rolls city on its own).
configure() is CityLayout.configure (config/deciworldgen_cities.cfg; its cityChance is a float, pass
f32(value)). edgescan.py and hwmap.py use this module.
"""
import struct
import sys
from collections import deque
from decimal import Decimal, ROUND_HALF_UP

WILD, CIV, CITY, MIL = 0, 1, 2, 3
CELL, BOX = 5, 4
SIZES = (1, 2, 4, 6, 12, 16)
DEFAULT_WEIGHTS = (30, 35, 40, 35, 28, 22)
SIDE = ((1, 0), (-1, 0), (0, 1), (0, -1))
MASK48 = (1 << 48) - 1
ON, EPOCH = True, 0  # EPOCH changes with every switch, as CityLayout.epoch(): caches of sector answers key on it
_weights, _chance, _plans = list(DEFAULT_WEIGHTS), 1.0, {}


def s64(v):
    v &= (1 << 64) - 1
    return v - (1 << 64) if v >> 63 else v


def f32(v):
    """The float (32 bit) nearest v, as Java rounds float arithmetic."""
    return struct.unpack("f", struct.pack("f", v))[0]


F40, F65, F80, F85 = f32(0.40), f32(0.65), f32(0.80), f32(0.85)


class JRandom:
    """java.util.Random, bit for bit."""

    def __init__(self, seed):
        self.s = (seed ^ 0x5DEECE66D) & MASK48

    def next(self, bits):
        self.s = (self.s * 0x5DEECE66D + 0xB) & MASK48
        v = (self.s >> (48 - bits)) & 0xFFFFFFFF
        return v - (1 << 32) if v >> 31 else v

    def next_int(self, bound):
        r, m = self.next(31), bound - 1
        if (bound & m) == 0:
            return (bound * r) >> 31
        u = r
        while True:
            r = u % bound
            if (u - r + m) < (1 << 31):  # Java rejects when the int sum overflows
                return r
            u = self.next(31)

    def next_double(self):
        return ((self.next(26) << 27) + self.next(27)) * (1.0 / (1 << 53))

    def next_float(self):
        return self.next(24) / float(1 << 24)

    def next_boolean(self):
        return self.next(1) != 0


class Plan:
    """One cell's city: its sectors in growth order (cells, city), wasteland sectors, the military one beside it."""
    __slots__ = ("cells", "city", "waste", "mil", "size")

    def __init__(self):
        self.cells, self.city, self.waste, self.mil, self.size = [], set(), set(), None, 0


def set_on(v):
    """CityLayout.set: True for the sized layout (worlds since v0.60.0), False for older worlds."""
    global ON, EPOCH
    if ON != bool(v):
        ON, EPOCH = bool(v), EPOCH + 1


def configure(weights=DEFAULT_WEIGHTS, chance=1.0):
    """CityLayout.configure: size weights (SIZES order) and the chance a cell holds a city."""
    global _weights, _chance, EPOCH
    if weights is not None and len(weights) == len(SIZES):
        _weights = list(weights)
    _chance = float(max(0.0, min(1.0, chance)))
    EPOCH += 1
    _plans.clear()


def plan(seed, cx, cz):
    k = (seed, cx, cz)
    p = _plans.get(k)
    if p is None:
        if len(_plans) > 65536:
            _plans.clear()
        p = _plans[k] = _build(seed, cx, cz)
    return p


def plan_at(seed, rx, rz):
    return plan(seed, rx // CELL, rz // CELL)


def _build(seed, cx, cz):
    p = Plan()
    r = JRandom(seed ^ s64(cx * 341873128712 + cz * 132897987541) ^ 0x434954594C)
    if r.next_double() >= _chance:
        return p
    w = _weights
    total = sum(max(0, x) for x in w)
    if total <= 0:
        return p
    roll, pick = r.next_int(total), 0
    while roll >= max(0, w[pick]):
        roll -= max(0, w[pick])
        pick += 1
    p.size = SIZES[pick]
    ox, oz = cx * CELL, cz * CELL
    # a compact blob inside the box: each step the free box sector with the most city neighbours, nearest
    # to the blob's middle (noise breaks ties); next_double only for candidates, x outer, z inner, as Java
    sx = ox + r.next_int(BOX)
    sz = oz + r.next_int(BOX)
    cells, city = p.cells, p.city
    cells.append((sx, sz))
    city.add((sx, sz))
    while len(cells) < p.size:
        mx = mz = 0.0
        for c in cells:
            mx += c[0]
            mz += c[1]
        mx /= len(cells)
        mz /= len(cells)
        best, best_score = None, -1e9
        for x in range(ox, ox + BOX):
            for z in range(oz, oz + BOX):
                if (x, z) in city:
                    continue
                n = sum((x + s[0], z + s[1]) in city for s in SIDE)
                if n == 0:
                    continue
                score = 3 * n - ((x - mx) * (x - mx) + (z - mz) * (z - mz)) + r.next_double()
                if score > best_score:
                    best_score, best = score, (x, z)
        cells.append(best)
        city.add(best)
    if p.size < 2:
        return p
    # the military sector: beside the city's farthest sector toward one side (next_boolean only on ties)
    d = SIDE[r.next_int(4)]
    edge, far = None, -(1 << 31)
    for c in cells:
        along = c[0] * d[0] + c[1] * d[1]
        if (c[0] + d[0], c[1] + d[1]) not in city and (along > far or along == far and r.next_boolean()):
            far, edge = along, c
    p.mil = (edge[0] + d[0], edge[1] + d[1])
    # wasteland: 1 per 4 city sectors (at least 1), the city sectors nearest the military one
    want = max(1, p.size // 4)
    q, seen = deque([edge]), {edge}
    while q and len(p.waste) < want:
        c = q.popleft()
        p.waste.add(c)
        for s in SIDE:
            n = (c[0] + s[0], c[1] + s[1])
            if n in city and n not in seen:
                seen.add(n)
                q.append(n)
    return p


def sector(seed, rx, rz):
    """CityLayout.sector: CITY, MIL for the military sector beside a city, or -1 (roll as usual)."""
    cx, cz, k = rx // CELL, rz // CELL, (rx, rz)
    if k in plan(seed, cx, cz).city:
        return CITY
    for x, z in ((cx, cz), (cx - 1, cz), (cx + 1, cz), (cx, cz - 1), (cx, cz + 1)):
        if plan(seed, x, z).mil == k:
            return MIL
    return -1


def wasteland(seed, rx, rz):
    """CityLayout.wasteland: a wasteland city sector of the sized layout."""
    return (rx, rz) in plan_at(seed, rx, rz).waste


def region_sector(seed, rx, rz, on=None):
    """Sectors.regionSector: WILD, CIV, CITY or MIL; on=None follows ON."""
    roll = JRandom(seed ^ s64(rx * 875949887 + rz * 656887297)).next_float()
    if ON if on is None else on:
        # cities only from the layout; the rest keeps the old shares without the city one
        s = sector(seed, rx, rz)
        if s >= 0:
            return s
        roll = f32(roll * F85)
        return WILD if roll < F40 else CIV if roll < F65 else MIL
    return WILD if roll < F40 else CIV if roll < F65 else CITY if roll < F80 else MIL


def jf(v, n):
    """Java's %.nf: half up on the shortest decimal form of v (Formatter), not Python's half even."""
    return str(Decimal(repr(v)).quantize(Decimal(1).scaleb(-n), rounding=ROUND_HALF_UP))


def main():
    seed = int(sys.argv[1])
    rad = int(sys.argv[2]) if len(sys.argv) > 2 else 20
    count, waste = [0] * 17, [0] * 17
    city = waste_all = 0
    for cx in range(-rad, rad):
        for cz in range(-rad, rad):
            p = plan(seed, cx, cz)
            count[p.size] += 1
            waste[p.size] += len(p.waste)
            city += len(p.city)
            waste_all += len(p.waste)
    cells = 4 * rad * rad
    print("cells %d, city land %s %%, wasteland %s %% of city" % (
        cells, jf(100.0 * city / (cells * CELL * CELL), 1), jf(100.0 * waste_all / max(1, city), 1)))
    for s in SIZES:
        print("  %2d districts: %5d cities (%s %%), wasteland sectors each %s" % (
            s * 4, count[s], jf(100.0 * count[s] / cells, 1), jf(waste[s] / count[s] if count[s] else 0.0, 2)))
    if rad <= 8:
        span = range(-rad * CELL, rad * CELL)
        for rz in span:
            row = []
            for rx in span:
                s = sector(seed, rx, rz)
                row.append("W" if wasteland(seed, rx, rz) else "#" if s == CITY else "M" if s == MIL else ".")
            print("".join(row))


if __name__ == "__main__":
    main()

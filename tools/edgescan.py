#!/usr/bin/env python3
"""Check the ramps between Lost Cities style cities (LcCity) and the land.

    python3 tools/edgescan.py WORLD_DIR [RADIUS_CHUNKS] [--old-layout]

City cells are recomputed from the seed (same sector map as Sectors.java,
tools/citylayout.py): the sized city layout of worlds made since v0.60.0,
or with --old-layout the one of older worlds, where every sector rolls city
on its own (the v0.56 wasteland ring of worlds made in v0.56 to v0.57.0 is
not modelled). A warning says when the world's stored layout (StructureData
!citySizes) disagrees. Every column outside the city within EDGE (24)
blocks of a city cell whose grading window is populated is read; for each
neighbouring pair (both in the band, or one on the city's border column)
the surface step is counted. Reports the step histogram, the share of steps
over 1 block, and the worst spots (cliffs). The street level is 64.
Highway chunks (tools/hwmap.py) are left out.
"""
import gzip
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import citylayout as cl  # noqa: E402
import worldcheck as wc  # noqa: E402
from gradescan import surface  # noqa: E402
from wallscan import populated  # noqa: E402

CELL, EDGE, REGION = 64, 24, 16


def region_sector(seed, rx, rz):
    """Sectors.regionSector (0 wild, 1 civilian, 2 city, 3 military); old layout after cl.set_on(False)."""
    return cl.region_sector(seed, rx, rz)


def is_city(seed, cx, cz):
    return region_sector(seed, (cx * 4) // REGION, (cz * 4) // REGION) == 2


def stored_layout(world_dir):
    """The world's StructureData !citySizes: 1 sized, 0 old, None when never opened since v0.60.0."""
    try:
        data = wc.read_nbt(gzip.open(os.path.join(world_dir, "data", "deciworldgen_structures.dat")).read())
        return data["data"]["baseY"].get("!citySizes")
    except Exception:  # missing, half written or foreign file: the warning is only advice
        return None


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    old = "--old-layout" in sys.argv
    cl.set_on(not old)
    world_dir = args[0]
    radius = int(args[1]) if len(args) > 1 else 12
    stored = stored_layout(world_dir)
    if (stored == 1) == old:
        print("warning: the world stores %s, %s" % (
            "city sizes on" if stored == 1 else "city sizes off" if stored == 0 else "no city sizes flag (older than v0.60.0)",
            "drop --old-layout" if old else "rerun with --old-layout"))
    w = wc.World(world_dir)
    seed = wc.read_nbt(__import__("gzip").open(os.path.join(world_dir, "level.dat")).read())["Data"]["RandomSeed"]
    spawn = wc.read_nbt(__import__("gzip").open(os.path.join(world_dir, "level.dat")).read())["Data"]
    sx, sz = spawn["SpawnX"] >> 4, spawn["SpawnZ"] >> 4
    city = {}

    def cityat(x, z):
        k = (x // CELL, z // CELL)
        if k not in city:
            city[k] = is_city(seed, *k)
        return city[k]

    def band(x, z):
        """Distance class: 0 city, 1 band (within EDGE of a city cell), 2 beyond."""
        if cityat(x, z):
            return 0
        for dx in (-EDGE, 0, EDGE):
            for dz in (-EDGE, 0, EDGE):
                if cityat(x + dx, z + dz):
                    return 1
        return 2

    tops, final = {}, {}

    def top(x, z):
        if (x, z) not in tops:
            tops[(x, z)] = surface(w, x, z, 200)
        return tops[(x, z)]

    def done(x, z):
        k = ((x - 8) >> 4, (z - 8) >> 4)
        if k not in final:
            final[k] = populated(world_dir, *k)
        return final[k]

    roads = {}

    def road(x, z):
        """Highway chunks level themselves (lamp posts would read as steps)."""
        import hwmap
        k = (x >> 4, z >> 4)
        if k not in roads:
            roads[k] = hwmap.at(seed, *k)
        return roads[k]

    hist, worst, cols = {}, [], 0
    for x in range((sx - radius) * 16, (sx + radius) * 16):
        for z in range((sz - radius) * 16, (sz + radius) * 16):
            if band(x, z) != 1 or not done(x, z) or road(x, z):
                continue
            cols += 1
            a = top(x, z)
            for nx, nz in ((x + 1, z), (x, z + 1), (x - 1, z), (x, z - 1)):
                kind = band(nx, nz)
                if kind == 2 or not done(nx, nz) or road(nx, nz) or (kind == 1 and (nx, nz) < (x, z)):
                    continue
                b = top(nx, nz)
                if a is None or b is None:
                    continue
                step = abs(a - b)
                hist[step] = hist.get(step, 0) + 1
                if step > 2:
                    worst.append((step, x, z, a, b))
    pairs = sum(hist.values())
    print("seed %d, %s layout, %d band columns, %d pairs" % (seed, "sized" if cl.ON else "old", cols, pairs))
    print("steps:", ", ".join("%d: %d" % (k, hist[k]) for k in sorted(hist)))
    if pairs:
        print("over 1 block: %.1f%%, over 2: %.2f%%" % (
            100.0 * sum(v for k, v in hist.items() if k > 1) / pairs,
            100.0 * sum(v for k, v in hist.items() if k > 2) / pairs))
    for s in sorted(worst, reverse=True)[:8]:
        print("  step %d at %d,%d (%d vs %d)" % s)


if __name__ == "__main__":
    main()

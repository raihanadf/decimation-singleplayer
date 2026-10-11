#!/usr/bin/env python3
"""Dump the Decimation props of a generated world (and the blocks around them) for propclash.py.

    python3 tools/props/worlddump.py WORLD_DIR OUT.tsv [X0 Z0 X1 Z1] [--ring N]

Reads the Anvil region files (tools/worldcheck.World, no game). Every deci: block that is a prop
(listed in docs/references/prop_geometry.tsv) is written with every block within --ring (default
3) of it, in the "x y z name meta" format of the dev test dumps, so tools/props/propclash.py can
check a real world: city buildings, street parts, scenes, our own dressing. Without a box the
whole world is read. Chunks are skipped unless they and their 8 neighbours exist (a prop at the
edge of the generated area would report walls or floors that are not written yet).
"""
import os
import sys

ROOT = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
sys.path.insert(0, os.path.join(ROOT, "tools"))
from worldcheck import World  # noqa: E402

GEOM = os.path.join(ROOT, "docs", "references", "prop_geometry.tsv")


def prop_names():
    out = set()
    with open(GEOM) as f:
        next(f)
        for line in f:
            out.add(line.split("\t")[0])
    return out


def chunks(world):
    out = []
    for f in os.listdir(os.path.join(world.path, "region")):
        p = f.split(".")
        if len(p) != 4 or p[3] != "mca":
            continue
        rx, rz = int(p[1]), int(p[2])
        with open(os.path.join(world.path, "region", f), "rb") as fh:
            head = fh.read(4096)
        for i in range(1024):
            if head[4 * i:4 * i + 4] != b"\0\0\0\0":
                out.append((rx * 32 + (i & 31), rz * 32 + (i >> 5)))
    return set(out)


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    ring = 3
    if "--ring" in sys.argv:
        ring = int(sys.argv[sys.argv.index("--ring") + 1])
        args.remove(str(ring))
    world = World(args[0])
    out = args[1]
    box = [int(v) for v in args[2:6]] if len(args) >= 6 else None
    reg = world.registry()
    names = {v: k for k, v in reg.items()}
    props = prop_names()
    prop_ids = {i for i, n in names.items() if n.startswith("deci:") and n[5:] in props}

    have = chunks(world)
    full = {c for c in have if all((c[0] + dx, c[1] + dz) in have for dx in (-1, 0, 1) for dz in (-1, 0, 1))}
    if box:
        x0, z0, x1, z1 = box
        full = {c for c in full if x0 >> 4 <= c[0] <= x1 >> 4 and z0 >> 4 <= c[1] <= z1 >> 4}

    found = []
    for cx, cz in sorted(full):
        secs = world.chunk(cx, cz)
        if not secs:
            continue
        for sy, s in secs.items():
            blocks = s["Blocks"]
            add = s.get("Add")
            for i in range(4096):
                b = blocks[i]
                if add is not None:
                    a = add[i >> 1]
                    b |= ((a >> 4) if i & 1 else (a & 15)) << 8
                if b in prop_ids:
                    x = cx * 16 + (i & 15)
                    z = cz * 16 + ((i >> 4) & 15)
                    y = sy * 16 + (i >> 8)
                    if box and not (x0 <= x <= x1 and z0 <= z <= z1):
                        continue
                    found.append((x, y, z))

    cells = set()
    for x, y, z in found:
        for dx in range(-ring, ring + 1):
            for dy in range(-ring, ring + 1):
                for dz in range(-ring, ring + 1):
                    if 0 <= y + dy < 256:
                        cells.add((x + dx, y + dy, z + dz))
    with open(out, "w") as f:
        f.write("# origin 0 0 0 world %s\n" % os.path.basename(os.path.normpath(world.path)))
        for x, y, z in sorted(cells):
            b = world.block(x, y, z)
            if not b:
                continue
            f.write("%d %d %d %s %d\n" % (x, y, z, names.get(b, "id%d" % b), world.meta(x, y, z)))
    print("chunks %d (with all neighbours), props %d, cells written to %s" % (len(full), len(found), out))


if __name__ == "__main__":
    main()

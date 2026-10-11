#!/usr/bin/env python3
"""Top down picture of the props in a block dump, with each prop's FRONT as an arrow.

    python3 tools/props/topdown.py DUMP.tsv X0 Z0 X1 Z1 OUT.png [--scale 12]

DUMP: tools/props/worlddump.py output. Every column is coloured by its top block (road dark, walk
light, grass green, building grey); every Decimation prop is drawn as its drawn box
(docs/references/prop_geometry.tsv) and, if its front matters, an arrow from its centre toward
its front (docs/references/prop_facing.tsv). Used to check facing of cars in jams, crashes and
lots without the game (docs/city_dressing.md).
"""
import os
import sys

from PIL import Image, ImageDraw

ROOT = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))


def main():
    a = [x for x in sys.argv[1:] if not x.startswith("--")]
    scale = int(sys.argv[sys.argv.index("--scale") + 1]) if "--scale" in sys.argv else 12
    if "--scale" in sys.argv:
        a.remove(str(scale))
    dump, x0, z0, x1, z1, out = a[0], int(a[1]), int(a[2]), int(a[3]), int(a[4]), a[5]
    geom, front = {}, {}
    for l in open(os.path.join(ROOT, "docs/references/prop_geometry.tsv")):
        c = l.rstrip("\n").split("\t")
        if c[0] != "prop":
            geom[(c[0], int(c[3]))] = [float(v) for v in c[6:12]]
    for l in open(os.path.join(ROOT, "docs/references/prop_facing.tsv")):
        c = l.rstrip("\n").split("\t")
        if c[0] != "prop":
            for k in range(4):
                if c[2 + k] in "ESWN" and c[2 + k]:
                    front[(c[0], k + 2)] = {"E": (1, 0), "S": (0, 1), "W": (-1, 0), "N": (0, -1)}[c[2 + k]]
    top, props = {}, []
    for l in open(dump):
        if l[0] == "#":
            continue
        x, y, z, n, m = l.split()
        x, y, z, m = int(x), int(y), int(z), int(m)
        if not (x0 <= x <= x1 and z0 <= z <= z1):
            continue
        short = n.split(":")[-1]
        if n.startswith("deci:") and (short, m if m in (2, 3, 4, 5) else 3) in geom:
            props.append((short, m, x, y, z))
            continue
        if (x, z) not in top or y > top[(x, z)][0]:
            top[(x, z)] = (y, short)
    W, H = (x1 - x0 + 1) * scale, (z1 - z0 + 1) * scale
    img = Image.new("RGB", (W, H), (255, 255, 255))
    dr = ImageDraw.Draw(img)
    for (x, z), (y, n) in top.items():
        col = (60, 60, 64) if "Road" in n else (90, 140, 70) if n in ("grass", "leaves", "dirt") else \
            (205, 205, 200) if "slab" in n or "Stone_6" in n or "Stone_7" in n else (150, 150, 150)
        px, pz = (x - x0) * scale, (z - z0) * scale
        dr.rectangle([px, pz, px + scale - 1, pz + scale - 1], fill=col)
    for short, m, x, y, z in props:
        b = geom[(short, m if m in (2, 3, 4, 5) else 3)]
        bx0, bx1, bz0, bz1 = (x + b[0] - x0) * scale, (x + b[1] - x0) * scale, (z + b[4] - z0) * scale, (z + b[5] - z0) * scale
        col = (200, 40, 40) if "Wreck" in short else (230, 160, 30) if "Sign" in short or "Traffic" in short else (40, 90, 200)
        dr.rectangle([bx0, bz0, bx1, bz1], outline=col, width=2)
        f = front.get((short, m))
        if f:
            cx, cz = (x + 0.5 - x0) * scale, (z + 0.5 - z0) * scale
            dr.line([cx, cz, cx + f[0] * scale * 1.6, cz + f[1] * scale * 1.6], fill=(255, 255, 0), width=3)
            dr.ellipse([cx - 3, cz - 3, cx + 3, cz + 3], fill=(0, 0, 0))
    img.save(out)
    print("props %d, columns %d -> %s" % (len(props), len(top), out))


if __name__ == "__main__":
    main()

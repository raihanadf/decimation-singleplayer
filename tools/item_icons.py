#!/usr/bin/env python3
"""Item icons of our own in the pixel style of Decimation's item icons (its medical items: painkillers, vitamincapsules).

    python3 tools/item_icons.py            # writes the icons into dev/src/main/resources/assets/deciworldgen/textures/items
    python3 tools/item_icons.py --sheet F  # also an 8x sheet of them for a look

Decimation's medical icons (measured 22 Oktober 2026 from its jar, never copied): 32 x 32, every pixel fully opaque or
empty, the shape's own border pixels in one dark slate (64, 66, 72), fills shaded like a lit cylinder (a light stripe a
little left of the middle, darker toward the right edge), the Pain Killers bottle 14 wide and 21 high with a dark cap and
a label round its middle. Our art only: every shape is drawn here from geometry.

radiation_pills.png: an amber pill bottle with a white child safe cap and a yellow label carrying a black radiation sign.
"""
import math
import os
import sys

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "dev/src/main/resources/assets/deciworldgen/textures/items")
SIZE = 32
OUTLINE = (64, 66, 72)


def shade(colour, x, x0, x1):
    """A lit cylinder across x0 .. x1: brightest a third in, darkest at the right edge."""
    t = (x + 0.5 - x0) / (x1 + 1 - x0)
    k = 1.25 - 0.9 * abs(t - 0.32) if t < 0.32 else 1.25 - 0.75 * (t - 0.32) / 0.68 - 0.1
    k = max(0.55, min(1.3, k))
    return tuple(max(0, min(255, int(round(c * k)))) for c in colour)


def trefoil(x, y, cx, cy, r):
    """The radiation sign around (cx, cy), radius r: a centre dot and three blades (top left, top right, bottom)."""
    dx, dy = x + 0.5 - cx, cy - (y + 0.5)
    d = math.hypot(dx, dy)
    if d <= r * 0.16:
        return True
    if d < r * 0.36 or d > r:
        return False
    a = math.degrees(math.atan2(dy, dx)) % 360
    return any(abs((a - c + 180) % 360 - 180) <= 30 for c in (30, 150, 270))


def radiation_pills():
    im = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    px = im.load()
    x0, x1 = 9, 23                                               # 15 wide: the sign sits on a pixel column
    amber, cap, label = (176, 98, 30), (226, 226, 222), (232, 196, 48)
    cx, cy = (x0 + x1 + 1) / 2, 18.5
    for y in range(5, 27):
        for x in range(x0, x1 + 1):
            if y <= 9:                                           # cap with grip ridges
                if y == 5 and x in (x0, x1):
                    continue
                c = shade(cap, x, x0, x1)
                if y >= 6 and (x - x0) % 2 == 1:
                    c = tuple(int(v * 0.86) for v in c)
            elif y == 10:                                        # neck under the cap
                if x in (x0, x1):
                    continue
                c = shade((120, 64, 22), x, x0, x1)
            elif 12 <= y <= 24:                                  # label round the bottle
                c = shade(label, x, x0, x1)
                if y in (12, 24):
                    c = tuple(int(v * 0.72) for v in c)
                elif trefoil(x, y, cx, cy, 6.0):
                    c = (24, 22, 20)
            else:                                                # amber glass
                if y == 26 and x in (x0, x1):
                    continue
                c = shade(amber, x, x0, x1)
                if x == x0 + 3:
                    c = tuple(min(255, int(v * 1.35)) for v in c)   # the glass shine
            px[x, y] = c + (255,)
    edge = [(x, y) for y in range(SIZE) for x in range(SIZE) if px[x, y][3]
            and any(not (0 <= x + a < SIZE and 0 <= y + b < SIZE) or px[x + a, y + b][3] == 0
                    for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1)))]
    for p in edge:
        px[p] = OUTLINE + (255,)
    for x in range(x0 + 1, x1):                                  # the cap's rim
        px[x, 9] = OUTLINE + (255,)
    return im


ICONS = {"radiation_pills.png": radiation_pills}


def main():
    os.makedirs(OUT, exist_ok=True)
    made = []
    for name, draw in ICONS.items():
        im = draw()
        im.save(os.path.join(OUT, name))
        made.append(im)
        print("wrote", os.path.relpath(os.path.join(OUT, name), ROOT))
    if "--sheet" in sys.argv:
        path = sys.argv[sys.argv.index("--sheet") + 1]
        sheet = Image.new("RGBA", (266 * len(made), 266), (110, 110, 110, 255))
        for i, im in enumerate(made):
            big = im.resize((256, 256), Image.NEAREST)
            sheet.paste(big, (266 * i + 5, 5), big)
        sheet.save(path)
        print("sheet", path)


if __name__ == "__main__":
    main()

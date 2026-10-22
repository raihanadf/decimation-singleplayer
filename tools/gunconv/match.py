#!/usr/bin/env python3
"""Decimation's own guns as the converter's references (docs/gunconv.md). Every gun model is read from the user's
Decimation.jar once per process (numbers only: nothing of Decimation is copied or written). From them:
- the closest gun to a converted one ("like": its hands, sounds, stats and loot are taken by the game side), found by
  side silhouettes normalised to the same length (intersection over union, small shifts tried);
- the alignment of a converted gun onto its like gun in model units (so the like gun's hands, aim pose and attachment
  spots fit it);
- the like gun's anatomy: where its magazine (ammoModel), bolt (slideModel) and folding iron sights (defaultScopeModel)
  are, its header (hands, ejection), its sight mount row (sight_mounts.txt).
"""
import os
import re
import sys
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(os.path.dirname(HERE), "guns"))
import fit  # noqa: E402
import study  # noqa: E402

MOUNTS = os.path.join(study.ROOT, "dev", "src", "main", "resources", "assets", "deciworldgen", "sight_mounts.txt")
NORM_W = 160          # pixels along a gun's length in the normalised silhouettes
_GUNS = None
_NORM = {}


def jar_ok():
    return os.path.exists(study.JAR)


def guns():
    """{name: study.Gun} of every Decimation gun (without textures), read from the jar once."""
    global _GUNS
    if _GUNS is None:
        _GUNS = {}
        if jar_ok():
            with zipfile.ZipFile(study.JAR) as z:
                for i in z.namelist():
                    m = re.match(r"assets/deci/models/guns/(\w+)/(\w+)\.bmodel$", i)
                    if m:
                        _GUNS[m.group(2)] = study.Gun(m.group(2), m.group(1), z.read(i).decode("latin1"), None, False)
    return _GUNS


hull = fit.hull


def side_polys_of_gun(gun):
    """Side view (x, y) outlines of a study.Gun's parts."""
    return [hull([(q[0], q[1]) for q in p.verts()]) for p in gun.parts]


def side_polys_of_parts(parts):
    return [hull([(q[0], q[1]) for q in p.points()]) for p in parts]


def side_polys_of_tris(tris):
    return [[(q[0], q[1]) for q in t] for t in tris]


def poly_bounds(polys):
    xs = [p[0] for poly in polys for p in poly]
    ys = [p[1] for poly in polys for p in poly]
    return min(xs), min(ys), max(xs), max(ys)


def normalised(polys):
    x0, y0, x1, y1 = poly_bounds(polys)
    k = NORM_W / max(x1 - x0, 1e-9)
    return fit.silhouette(polys, x0, y0, k, NORM_W + 2, int((y1 - y0) * k) + 2)


def count(img):
    return img.histogram()[-1]


def iou_shifted(a, b, dx, dy):
    """IoU of image a moved by (dx, dy) pixels against image b."""
    from PIL import Image, ImageChops
    w, h = max(a.width, b.width) + 2 * abs(dx) + 2, max(a.height, b.height) + 2 * abs(dy) + 2
    ca, cb = Image.new("1", (w, h), 0), Image.new("1", (w, h), 0)
    ox, oy = max(0, -dx), max(0, -dy)
    ca.paste(a, (ox + dx, oy + dy))
    cb.paste(b, (ox, oy))
    inter = count(ImageChops.logical_and(ca, cb))
    union = count(ImageChops.logical_or(ca, cb))
    return inter / union if union else 0.0


def best_iou(a, b, reach, step):
    best = (-1.0, 0, 0)
    for dx in range(-reach, reach + 1, step):
        for dy in range(-reach, reach + 1, step):
            s = iou_shifted(a, b, dx, dy)
            if s > best[0]:
                best = (s, dx, dy)
    return best


def candidates(polys, category=None, top=8):
    """[(gun, score)] of Decimation's guns (of `category` when given) closest to these side outlines, best first."""
    mine = normalised(polys)
    out = []
    for name, g in sorted(guns().items()):
        if category and g.cat != category:
            continue
        if name not in _NORM:
            _NORM[name] = normalised(side_polys_of_gun(g))
        s, _, _ = best_iou(mine, _NORM[name], 6, 2)
        out.append((name, round(s, 4)))
    out.sort(key=lambda r: -r[1])
    return out[:top]


def align(polys, like, k=8.0, reach_x=4.0, reach_y=2.5):
    """(dx, dy) model units that put these side outlines onto the like gun's (best IoU at k px a unit, coarse 0.5
    then fine 0.125)."""
    g = guns()[like]
    theirs = side_polys_of_gun(g)
    ax0, ay0, _, _ = poly_bounds(polys)
    bx0, by0, bx1, by1 = poly_bounds(theirs)
    ox, oy = min(ax0, bx0) - reach_x - 1, min(ay0, by0) - reach_y - 1
    w = int((max(poly_bounds(polys)[2], bx1) - ox + reach_x + 1) * k) + 2
    h = int((max(poly_bounds(polys)[3], by1) - oy + reach_y + 1) * k) + 2
    a = fit.silhouette(polys, ox, oy, k, w, h)
    b = fit.silhouette(theirs, ox, oy, k, w, h)
    rx, ry = int(reach_x * k), int(reach_y * k)
    best = (-1.0, 0, 0)
    for dx in range(-rx, rx + 1, 4):
        for dy in range(-ry, ry + 1, 4):
            s = iou_shifted(a, b, dx, dy)
            if s > best[0]:
                best = (s, dx, dy)
    _, cx, cy = best
    for dx in range(cx - 4, cx + 5):
        for dy in range(cy - 4, cy + 5):
            s = iou_shifted(a, b, dx, dy)
            if s > best[0]:
                best = (s, dx, dy)
    return best[1] / k, best[2] / k, best[0]


def mount_of(name):
    """The gun's row in sight_mounts.txt: 'rail', 'side', 'slide' or None."""
    if not os.path.exists(MOUNTS):
        return None
    with open(MOUNTS) as fh:
        for line in fh:
            f = line.split()
            if len(f) == 2 and not line.startswith("#") and f[0] == name:
                return f[1]
    return None


def group_box(gun, group):
    pts = [q for p in gun.parts if p.group == group for q in p.verts()]
    if not pts:
        return None
    return [(min(q[i] for q in pts), max(q[i] for q in pts)) for i in range(3)]


def info(name):
    """What the converter takes from the like gun."""
    g = guns()[name]
    b = g.bounds()
    return {
        "name": name, "category": g.cat, "header": dict(g.header), "bounds": b,
        "length": b[0][1] - b[0][0], "height": b[1][1] - b[1][0], "width": b[2][1] - b[2][0],
        "ammo": group_box(g, "ammoModel"), "slide": group_box(g, "slideModel"),
        "sights": group_box(g, "defaultScopeModel"), "foldingSights": group_box(g, "defaultScopeModel") is not None,
        "mount": mount_of(name), "parts": len(g.parts),
    }


# ---------------------------------------------------------------- the like gun's sight line and palette

FRONT = 0.22          # the front sight is the highest point in the front 22 % of the gun's length


def front_tip(vertex_lists, share=FRONT):
    """The highest point (smallest y: y grows downward) in the front share of the gun: the front sight's tip on iron
    sighted guns. Decimation draws every gun at one aim pose, so this height is the sight line."""
    pts = [q for vs in vertex_lists for q in vs]
    x1 = max(q[0] for q in pts)
    x0 = min(q[0] for q in pts)
    return min(q[1] for q in pts if q[0] >= x1 - share * (x1 - x0))


_PALETTE = {}


def lum(c):
    return 0.3 * c[0] + 0.59 * c[1] + 0.11 * c[2]


def grey(c):
    mx, mn = max(c[:3]), min(c[:3])
    return mx - mn < 12 or (mx > 0 and (mx - mn) / mx < 0.15)


def island_tone(gun, p):
    """Mean colour of a part's box UV island in its gun's texture (study.Part u, v, size), or None."""
    tex = gun.texture
    if tex is None:
        return None
    fx, fy = tex.width / gun.tw, tex.height / gun.th
    w, h, d = [int(round(s)) for s in p.size]
    acc, n = [0, 0, 0], 0
    px = tex.load()
    for y in range(int(p.v * fy), int((p.v + d + h) * fy)):
        for x in range(int(p.u * fx), int((p.u + 2 * (d + w)) * fx)):
            if 0 <= x < tex.width and 0 <= y < tex.height:
                r, g, b, a = px[x, y]
                if a > 0:
                    acc[0] += r
                    acc[1] += g
                    acc[2] += b
                    n += 1
    return tuple(round(c / n) for c in acc) if n else None


def palette(name):
    """The like gun's tone families, measured on its texture: grey luminances (metal, sorted; greyW with each part's
    surface area), the median coloured tone of its body (wood, polymer) and of its magazine (Decimation's akm: metal
    10 .. 46, wood about 38,24,21, a bakelite magazine about 88,57,47)."""
    if name not in _PALETTE:
        g = study.load(name)
        greys, body, ammo, ammo_grey, grey_w = [], [], [], [], []
        for p in g.parts:
            c = island_tone(g, p)
            if c is None:
                continue
            if grey(c):
                (ammo_grey if p.group == "ammoModel" else greys).append(lum(c))
                if p.group != "ammoModel":
                    v = p.verts()
                    d = [max(q[i] for q in v) - min(q[i] for q in v) for i in range(3)]
                    grey_w.append((lum(c), 2 * (d[0] * d[1] + d[1] * d[2] + d[0] * d[2])))
            else:
                (ammo if p.group == "ammoModel" else body).append(c)

        def med(cs):
            if not cs:
                return None
            return sorted(cs, key=lum)[len(cs) // 2]
        _PALETTE[name] = {"grey": sorted(greys) or [24.0, 64.0], "greyW": sorted(grey_w), "body": med(body),
                          "ammo": med(ammo), "ammoGrey": sorted(ammo_grey)}
    return _PALETTE[name]


_PART_TONES = {}


def part_tones(name):
    """[(bounding box, tone)] of the like gun's parts (with its texture): a colouring template for files that carry
    no colour of their own."""
    if name not in _PART_TONES:
        g = study.load(name)
        out = []
        for p in g.parts:
            c = island_tone(g, p)
            if c is not None:
                v = p.verts()
                out.append(([(min(q[i] for q in v), max(q[i] for q in v)) for i in range(3)], c))
        _PART_TONES[name] = out
    return _PART_TONES[name]


def template_tone(box, tones, grow=0.05):
    """The tone of the template part this box overlaps most (boxes grown a little), else of the nearest one."""
    best, best_v = None, 0.0
    for b, c in tones:
        v = 1.0
        for i in range(3):
            v *= max(0.0, min(box[i][1], b[i][1] + grow) - max(box[i][0], b[i][0] - grow))
        if v > best_v:
            best, best_v = c, v
    if best is None:
        mid = [(box[i][0] + box[i][1]) / 2 for i in range(3)]
        best = min(tones, key=lambda bc: sum(((bc[0][i][0] + bc[0][i][1]) / 2 - mid[i]) ** 2 for i in range(3)))[1]
    return best


# ---------------------------------------------------------------- the sight rail's top (tools/guns/sightfit.py rule)

ZONE = (2.0, 3.5)
LONG = 1.0


def rail_top(named_points, dx=0.0):
    """Top of the receiver or rail in the sight zone, by sightfit.rail_top's rule, over [(name, points)]: the highest
    long part there (iron sights and the magazine excluded), then rail teeth just above it."""
    zone = (ZONE[0] + dx, ZONE[1] + dx)

    def box(pts):
        return [(min(q[i] for q in pts), max(q[i] for q in pts)) for i in range(3)]
    top = None
    for name, pts in named_points:
        if name.startswith(("defaultScope", "ammo")):
            continue
        (a0, a1), (b0, b1), (c0, c1) = box(pts)
        if a1 - a0 < LONG or a1 < zone[0] or a0 > zone[1] or c1 < -0.45 or c0 > 0.15:
            continue
        top = b0 if top is None else min(top, b0)
    if top is None:
        return None
    teeth = top
    for name, pts in named_points:
        if name.startswith(("defaultScope", "ammo")):
            continue
        (a0, a1), (b0, b1), (c0, c1) = box(pts)
        if a1 < zone[0] or a0 > zone[1] or c1 < -0.45 or c0 > 0.15:
            continue
        if top - 0.15 <= b0 < top and b1 >= top - 0.02:
            teeth = min(teeth, b0)
    return teeth

#!/usr/bin/env python3
"""Geometry of the 3D gun converter (docs/gunconv.md): the gun's axes, mesh pieces, cross sections, and the fit of a
piece with Decimation's shape kinds (docs/gun_style_guide.md sections 3 and 7).

Decimation's model space (docs/gun_model_spec.md): x forward to the muzzle, y DOWN, z sideways with the gun's right
side at -z (ejection port, AK charging handle) and its left at +z, centre line z -0.15. That frame is right handed
(forward x up = right), so a right handed source file maps onto it by a rotation: nothing is mirrored and the right
side stays the right side.

A piece is fitted as a loft along its longest axis: two axis aligned end rectangles joined by straight edges, which is
exactly a Decimation shape box (8 corners) and covers its kinds: cuboid, taper (one end smaller), skew (an end moved),
wedge (an end collapsed). Where the piece bends or steps more than the tolerance it is cut along that axis and each
segment fitted again (a curved magazine becomes skewed segments, as Decimation builds them). Round bars along x
become Decimation's octagon: a middle slab with a trapezoid above and one below (3 parts a segment).
"""
import math
import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "guns"))
import gunmodel  # noqa: E402

MIN_THICK = 0.06     # thinnest side a part keeps (Decimation's thin features: pins and plates 0.06 to 0.1)
TOL = 0.08           # how far a fitted edge may stray from the source (model units, 1 = 31 mm)
STATIONS = 9         # cross sections per segment
MAX_DEPTH = 4        # a piece is cut at most 4 times deep (16 segments)
MIN_SEG = 0.3        # shortest segment a cut may leave
FLAT = 0.42          # octagon middle slab half height / radius (gunmodel.octagon)
SNAP = 0.05          # end faces closer than this in width are made equal (a clean box, not a 0.03 taper)
LEVEL = 0.08         # a panel whose ends are offset by less than this is made level (Decimation's level receivers)
AXES = "xyz"
CYCLIC = {0: (1, 2), 1: (2, 0), 2: (0, 1)}   # the two other axes, in an order that keeps the plane right handed
CORNERS = [(0, 0, 0), (0, 0, 1), (0, 1, 0), (0, 1, 1), (1, 0, 0), (1, 0, 1), (1, 1, 0), (1, 1, 1)]


# ---------------------------------------------------------------- vectors

def sub(a, b):
    return (a[0] - b[0], a[1] - b[1], a[2] - b[2])


def dot(a, b):
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def unit_axis(axis, sign):
    v = [0, 0, 0]
    v[axis] = 1 if sign > 0 else -1
    return tuple(v)


def parse_axis(s):
    """'+x', '-y', 'z' as (axis index, sign)."""
    s = s.strip().lower()
    sign = -1 if s.startswith("-") else 1
    s = s.lstrip("+-")
    if s not in AXES:
        raise ValueError("axis must be one of +x -x +y -y +z -z, got %r" % s)
    return AXES.index(s), sign


# ---------------------------------------------------------------- silhouettes

def silhouette(polys, x0, y0, k, w, h):
    """A 1 bit image of 2D polygons [(x, y), ...] drawn at k pixels a unit from (x0, y0) (image row = (y - y0) k)."""
    from PIL import Image, ImageDraw
    img = Image.new("1", (w, h), 0)
    d = ImageDraw.Draw(img)
    for poly in polys:
        pts = [((x - x0) * k, (y - y0) * k) for x, y in poly]
        if len(pts) >= 3:
            d.polygon(pts, fill=1, outline=1)
        elif pts:
            d.line(pts + pts[:1], fill=1)
    return img


def hull(points):
    """2D convex hull (Andrew's monotone chain), counter clockwise."""
    pts = sorted(set((round(p[0], 6), round(p[1], 6)) for p in points))
    if len(pts) <= 2:
        return pts

    def turn(o, a, b):
        return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0])
    lower, upper = [], []
    for p in pts:
        while len(lower) >= 2 and turn(lower[-2], lower[-1], p) <= 0:
            lower.pop()
        lower.append(p)
    for p in reversed(pts):
        while len(upper) >= 2 and turn(upper[-2], upper[-1], p) <= 0:
            upper.pop()
        upper.append(p)
    return lower[:-1] + upper[:-1]


def columns(img):
    """Per image column: (first, last) occupied row, or None."""
    w, h = img.size
    px = img.load()
    out = []
    for x in range(w):
        first = last = None
        for y in range(h):
            if px[x, y]:
                if first is None:
                    first = y
                last = y
        out.append(None if first is None else (first, last))
    return out


def median(vals):
    s = sorted(vals)
    n = len(s)
    return 0.0 if not n else (s[n // 2] if n % 2 else 0.5 * (s[n // 2 - 1] + s[n // 2]))


# ---------------------------------------------------------------- orientation

def orient(tris, up=None, muzzle=None):
    """The gun's muzzle direction f and up direction u (unit axis vectors in the file's space).
    Length = the longest extent, height = the next; the muzzle is the end whose last eighth is lower (a barrel, not a
    butt); down is the side that sticks out more beyond its usual line (magazine, grip, trigger guard, stock toe) than
    the other (sights). muzzle / up ('+x', '-z', ...) override the guesses."""
    pts = [p for t in tris for p in t]
    lo = [min(p[i] for p in pts) for i in range(3)]
    hi = [max(p[i] for p in pts) for i in range(3)]
    ext = [hi[i] - lo[i] for i in range(3)]
    la, ls = parse_axis(muzzle) if muzzle else (max(range(3), key=lambda i: ext[i]), None)
    if up:
        ua, us = parse_axis(up)
    else:
        ua, us = max((i for i in range(3) if i != la), key=lambda i: ext[i]), None
    if la == ua:
        raise ValueError("muzzle and up must be different axes")
    if ls is None or us is None:
        W = 400
        k = W / max(ext[la], 1e-9)
        H = max(40, min(800, int(ext[ua] * k) + 2))
        k2 = (H - 2) / max(ext[ua], 1e-9)
        kk = min(k, k2)
        # image x = along the length axis from its low end, image row = down from the HIGH end of the up axis
        polys = [[((p[la] - lo[la]), (hi[ua] - p[ua])) for p in t] for t in tris]
        img = silhouette(polys, 0, 0, kk, int(ext[la] * kk) + 2, int(ext[ua] * kk) + 2)
        cols = [c for c in columns(img)]
        used = [c for c in cols if c]
        if ls is None:
            n = max(1, len(cols) // 8)
            def height(cs):
                hs = [c[1] - c[0] for c in cs if c]
                return sum(hs) / len(hs) if hs else 0.0
            ls = 1 if height(cols[-n:]) < height(cols[:n]) else -1
        if us is None:
            top = median([c[0] for c in used])
            bot = median([c[1] for c in used])
            above = sum(max(0.0, top - c[0]) for c in used)       # rows over the usual top (toward +ua)
            below = sum(max(0.0, c[1] - bot) for c in used)       # rows under the usual bottom (toward -ua)
            us = 1 if below >= above else -1
    return unit_axis(la, ls), unit_axis(ua, us)


def rotation(f, u):
    """Rows of the rotation from the file's space to Decimation's model space: x = forward, y = down, z = -right
    (right = forward x up in a right handed file), so the right side lands at -z. A proper rotation (det +1)."""
    r = cross(f, u)
    return [tuple(float(c) for c in f), tuple(-float(c) for c in u), tuple(-float(c) for c in r)]


# ---------------------------------------------------------------- pieces

def components(verts, faces, eps=None):
    """Faces grouped into connected pieces; vertices at the same position (within eps) join their faces."""
    if eps is None:
        lo = [min(v[i] for v in verts) for i in range(3)]
        hi = [max(v[i] for v in verts) for i in range(3)]
        eps = max(1e-9, 1e-6 * math.sqrt(sum((hi[i] - lo[i]) ** 2 for i in range(3))))
    key = [(round(v[0] / eps), round(v[1] / eps), round(v[2] / eps)) for v in verts]
    parent = list(range(len(faces)))

    def find(a):
        while parent[a] != a:
            parent[a] = parent[parent[a]]
            a = parent[a]
        return a
    first = {}
    for fi, f in enumerate(faces):
        for vi in f:
            k = key[vi]
            if k in first:
                ra, rb = find(fi), find(first[k])
                if ra != rb:
                    parent[ra] = rb
            else:
                first[k] = fi
    groups = {}
    for fi in range(len(faces)):
        groups.setdefault(find(fi), []).append(fi)
    return list(groups.values())


# ---------------------------------------------------------------- cross sections

def cross_section(tris, axis, t):
    """The section of the triangles by the plane axis = t: (b_lo, b_hi, c_lo, c_hi, area) over the two other axes in
    right handed order (CYCLIC), or None. The area follows each triangle's winding (outward normals: positive)."""
    b, c = CYCLIC[axis]
    pts, area = [], 0.0
    for tri in tris:
        d = [p[axis] - t for p in tri]
        if (d[0] > 0 and d[1] > 0 and d[2] > 0) or (d[0] < 0 and d[1] < 0 and d[2] < 0):
            continue
        if d[0] == 0 and d[1] == 0 and d[2] == 0:
            continue                                     # lying in the plane: adds no outline
        seg = []
        for i in range(3):
            j = (i + 1) % 3
            if d[i] == 0:
                seg.append((tri[i][b], tri[i][c]))
            if (d[i] < 0 < d[j]) or (d[j] < 0 < d[i]):
                s = d[i] / (d[i] - d[j])
                seg.append((tri[i][b] + s * (tri[j][b] - tri[i][b]), tri[i][c] + s * (tri[j][c] - tri[i][c])))
        if len(seg) < 2:
            continue
        P, Q = seg[0], seg[1]
        if len(seg) > 2:                                 # a vertex on the plane plus a crossing: keep the far pair
            P, Q = max(((p, q) for p in seg for q in seg), key=lambda pq: (pq[0][0] - pq[1][0]) ** 2 + (pq[0][1] - pq[1][1]) ** 2)
        pts += [P, Q]
        n = cross(sub(tri[1], tri[0]), sub(tri[2], tri[0]))
        nb, nc = n[b], n[c]
        if (Q[0] - P[0]) * (-nc) + (Q[1] - P[1]) * nb < 0:
            P, Q = Q, P
        area += 0.5 * (P[0] * Q[1] - Q[0] * P[1])
    if not pts:
        return None
    return (min(p[0] for p in pts), max(p[0] for p in pts), min(p[1] for p in pts), max(p[1] for p in pts), area)


def extent(tris, axis):
    vals = [p[axis] for t in tris for p in t]
    return min(vals), max(vals)


def is_round(tris, axis):
    """A bar whose sections are round: the section fills 0.70 .. 0.87 of its bounding rectangle (a 16 sided polygon
    0.77, a flat sided octagon 0.83, a square 1.0) and that rectangle is about square, at most of 3 stations."""
    a0, a1 = extent(tris, axis)
    if a1 - a0 <= 0:
        return False
    hits = 0
    for f in (0.3, 0.5, 0.7):
        cs = cross_section(tris, axis, a0 + f * (a1 - a0))
        if not cs:
            continue
        wb, wc = cs[1] - cs[0], cs[3] - cs[2]
        if wb <= 0 or wc <= 0:
            continue
        ratio = abs(cs[4]) / (wb * wc)
        if 0.70 <= ratio <= 0.87 and 0.75 <= wb / wc <= 1.33:
            hits += 1
    return hits >= 2


# ---------------------------------------------------------------- fitting

def stations(a0, a1, n=STATIONS):
    eps = 1e-4 * (a1 - a0)
    return [a0 + eps + (a1 - a0 - 2 * eps) * i / (n - 1) for i in range(n)]


def line_fit(ts, vs):
    """Least squares v = p + q t."""
    n = len(ts)
    mt, mv = sum(ts) / n, sum(vs) / n
    var = sum((t - mt) ** 2 for t in ts)
    q = sum((t - mt) * (v - mv) for t, v in zip(ts, vs)) / var if var > 1e-12 else 0.0
    return mv - q * mt, q


def fit_rows(ts, rows, use):
    lines = [line_fit([ts[i] for i in use], [rows[i][e] for i in use]) for e in range(4)]
    err, at = 0.0, ts[use[len(use) // 2]]
    for i in use:
        for e in range(4):
            dv = abs(rows[i][e] - (lines[e][0] + lines[e][1] * ts[i]))
            if dv > err:
                err, at = dv, ts[i]
    return lines, err, at


def segment_fit(tris, axis, a0, a1, tips=(False, False)):
    """Lines for the 4 edges of the loft between a0 and a1 and the largest deviation, or None (no section).
    tips: whether a0 / a1 are the piece's own ends. There a station that collapses to under half its neighbour's
    width and lies off the line through the others is a chamfer or a rounded cap (the AKM sample's grip and
    magazine end in thin ridges): it is left out, and the loft runs on to the end at full size. A true wedge tapers
    onto the line and keeps its point."""
    ts, rows = [], []
    for t in stations(a0, a1):
        cs = cross_section(tris, axis, t)
        if cs:
            ts.append(t)
            rows.append(cs[:4])
    if len(ts) < 2:
        return None
    use = list(range(len(ts)))
    for end, nb, flag in ((0, 1, tips[0]), (len(ts) - 1, len(ts) - 2, tips[1])):
        if not flag or len(use) <= 3 or end not in use:
            continue
        w_end = (rows[end][1] - rows[end][0], rows[end][3] - rows[end][2])
        w_nb = (rows[nb][1] - rows[nb][0], rows[nb][3] - rows[nb][2])
        if not (w_end[0] < 0.5 * w_nb[0] or w_end[1] < 0.5 * w_nb[1]):
            continue
        others = [i for i in use if i != end]
        lines, _, _ = fit_rows(ts, rows, others)
        if max(abs(rows[end][e] - (lines[e][0] + lines[e][1] * ts[end])) for e in range(4)) > TOL:
            use = others
    return fit_rows(ts, rows, use)


PLATE = 0.2          # top plate of a layered part (Decimation's receivers: core plus panels)
BEVEL = 0.15         # the top plate's long top edges bevelled by this much a side (at most a quarter of its width)
LAYER_H, LAYER_W = 0.6, 0.4   # only parts at least this tall and wide get a top plate
JUMP = 0.25          # an extent changing this much between two fine stations is a step (a post, a block, a flange)
MAX_STEPS = 12       # at most this many step cuts a piece (the largest jumps)


def width2(cs):
    return min(cs[1] - cs[0], cs[3] - cs[2])


def step_cuts(tris, axis, a0, a1):
    """Where the piece's section jumps along the axis (scanned every 0.03, 33 to 200 stations): cuts between the two
    stations, so a thin post or a sight block becomes its own part. A jump onto a smaller end station is a chamfered
    tip (segment_fit leaves it out), not a step."""
    n = int(min(200, max(33, (a1 - a0) / 0.03)))
    ts = [a0 + (a1 - a0) * (i + 0.5) / n for i in range(n)]
    secs = [cross_section(tris, axis, t) for t in ts]
    jumps = []
    for i in range(n - 1):
        s, u = secs[i], secs[i + 1]
        if not s or not u:
            continue
        j = max(abs(s[e] - u[e]) for e in range(4))
        if j <= JUMP:
            continue
        if (i == 0 and width2(s) < width2(u)) or (i == n - 2 and width2(u) < width2(s)):
            continue
        jumps.append((j, 0.5 * (ts[i] + ts[i + 1])))
    return sorted(t for _, t in sorted(jumps, reverse=True)[:MAX_STEPS])


def stepped_segments(tris, axis, a0, a1, tol):
    """segments() between the step cuts."""
    cuts = [a0] + step_cuts(tris, axis, a0, a1) + [a1]
    out = []
    for s0, s1 in zip(cuts, cuts[1:]):
        out += segments(tris, axis, s0, s1, tol, ends=(a0, a1))
    return out


def side_iou(parts, tris, k=40.0):
    """How well the parts cover the triangles' side outline (x, y): intersection over union of the two silhouettes."""
    from PIL import ImageChops
    pts = [q for t in tris for q in t] + [q for p in parts for q in p.c.values()]
    x0, y0 = min(q[0] for q in pts), min(q[1] for q in pts)
    w = int((max(q[0] for q in pts) - x0) * k) + 2
    h = int((max(q[1] for q in pts) - y0) * k) + 2
    a = silhouette([[(q[0], q[1]) for q in t] for t in tris], x0, y0, k, w, h)
    b = silhouette([hull([(q[0], q[1]) for q in p.c.values()]) for p in parts], x0, y0, k, w, h)
    inter = ImageChops.logical_and(a, b).histogram()[-1]
    union = ImageChops.logical_or(a, b).histogram()[-1]
    return inter / union if union else 0.0


def segments(tris, axis, a0, a1, tol, depth=0, ends=None):
    """[(a0, a1, lines)] covering a0 .. a1, cut where a straight loft strays more than tol. ends: the piece's own
    ends along the axis (chamfered tips there do not count, segment_fit)."""
    ends = ends or (a0, a1)

    def fitted(s0, s1):
        return segment_fit(tris, axis, s0, s1, (s0 == ends[0], s1 == ends[1]))
    res = fitted(a0, a1)
    if res is None:
        return []
    lines, err, at = res
    if err <= tol or depth >= MAX_DEPTH or a1 - a0 < 2 * MIN_SEG:
        return [(a0, a1, lines)]
    cut = min(max(at, a0 + 0.25 * (a1 - a0)), a1 - 0.25 * (a1 - a0))
    cut = min(max(cut, a0 + MIN_SEG), a1 - MIN_SEG)
    def better(left, right):
        """A cut helps when both halves fit better, or one of them much better (a tilted end cap keeps its error in
        its own half); a profile that slicing cannot fix (an L section along the run) stays one part."""
        if left is None or right is None:
            return False
        return max(left[1], right[1]) < 0.9 * err or min(left[1], right[1]) < 0.6 * err
    left, right = fitted(a0, cut), fitted(cut, a1)
    if not better(left, right):
        mid = 0.5 * (a0 + a1)                            # the worst station did not help: try halves
        left, right = fitted(a0, mid), fitted(mid, a1)
        if not better(left, right):
            return [(a0, a1, lines)]
        cut = mid
    return (segments(tris, axis, a0, cut, tol, depth + 1, ends) +
            segments(tris, axis, cut, a1, tol, depth + 1, ends))


def snap(ends):
    """Equal widths at both ends where widths and centres differ by less than SNAP: a clean box. Anything else stays
    as fitted: Decimation's own akm builds its curved magazine from tapered slices (10 of 13) and has 2 % skews."""
    (b0, b1, c0, c1), (d0, d1, e0, e1) = ends

    def same(w0, w1, m0, m1):
        return abs(w0 - w1) < SNAP and abs(m0 - m1) < SNAP     # only a box: a shifted end stays as fitted
    if same(b1 - b0, d1 - d0, (b0 + b1) / 2, (d0 + d1) / 2):
        w, m0, m1 = ((b1 - b0) + (d1 - d0)) / 2, (b0 + b1) / 2, (d0 + d1) / 2
        b0, b1, d0, d1 = m0 - w / 2, m0 + w / 2, m1 - w / 2, m1 + w / 2
    if same(c1 - c0, e1 - e0, (c0 + c1) / 2, (e0 + e1) / 2):
        w, m0, m1 = ((c1 - c0) + (e1 - e0)) / 2, (c0 + c1) / 2, (e0 + e1) / 2
        c0, c1, e0, e1 = m0 - w / 2, m0 + w / 2, m1 - w / 2, m1 + w / 2
    return [(b0, b1, c0, c1), (d0, d1, e0, e1)]


def edge(lines, e, t):
    return lines[e][0] + lines[e][1] * t


def thick(lo, hi):
    if hi - lo >= MIN_THICK:
        return lo, hi
    m = 0.5 * (lo + hi)
    return m - MIN_THICK / 2, m + MIN_THICK / 2


def new_part(name, group, colour, corners, label=None):
    p = gunmodel.part(name, group, tuple(int(c) for c in colour), (0, 0, 0), (1, 1, 1))
    p.c = {k: list(v) for k, v in corners.items()}
    p.label = label if label is not None else name
    return p


def loft_corners(axis, t0, t1, end0, end1):
    """Corners keyed (ix, iy, iz) of a loft along `axis`: end rectangles (b_lo, b_hi, c_lo, c_hi) at t0 and t1."""
    b, c = CYCLIC[axis]
    out = {}
    for k in CORNERS:
        t, (blo, bhi, clo, chi) = (t1, end1) if k[axis] else (t0, end0)
        p = [0.0, 0.0, 0.0]
        p[axis] = t
        p[b] = bhi if k[b] else blo
        p[c] = chi if k[c] else clo
        out[k] = p
    return out


def octagon_parts(name, group, colour, label, t0, t1, end0, end1):
    """Decimation's round bar along x (gunmodel.octagon) between two end sections (y_lo, y_hi, z_lo, z_hi): a middle
    slab, a trapezoid above (its top face narrower) and one below."""
    def ring(e):
        ylo, yhi, zlo, zhi = e
        cy, cz, ry, rz = (ylo + yhi) / 2, (zlo + zhi) / 2, max(MIN_THICK, yhi - ylo) / 2, max(MIN_THICK, zhi - zlo) / 2
        return cy, cz, ry, rz
    r0, r1 = ring(end0), ring(end1)
    out = []
    for suffix, rows in (("Mid", lambda cy, ry: (cy - FLAT * ry, cy + FLAT * ry)),
                         ("Top", lambda cy, ry: (cy - ry, cy - FLAT * ry)),
                         ("Bot", lambda cy, ry: (cy + FLAT * ry, cy + ry))):
        corners = {}
        for k in CORNERS:
            x = t1 if k[0] else t0
            cy, cz, ry, rz = r1 if k[0] else r0
            ya, yb = rows(cy, ry)
            y = yb if k[1] else ya
            half = rz
            if (suffix == "Top" and not k[1]) or (suffix == "Bot" and k[1]):
                half = FLAT * rz                         # the outer face of a trapezoid is narrower
            corners[k] = [x, y, cz + half if k[2] else cz - half]
        q = new_part(name + suffix, group, colour, corners, label)
        q.round = True
        out.append(q)
    return out


def layer_top(p):
    """Decimation's layered receivers (style guide section 3 rules 3 and 7): a bulky part becomes a core and a top
    plate PLATE thick whose top face is narrower by BEVEL a side (a taper: bevelled long top edges). The outline is
    kept. Parts thinner than LAYER_H tall or LAYER_W wide stay whole. k[1] = 0 is always a part's top face (y up is
    smaller y), whatever axis it was lofted along."""
    (x0, x1), (y0, y1), (z0, z1) = bounds([p])
    if y1 - y0 < LAYER_H or z1 - z0 < LAYER_W:
        return [p]
    plate, core = {}, {}
    for ix in (0, 1):
        for iz in (0, 1):
            top, bot = p.c[(ix, 0, iz)], p.c[(ix, 1, iz)]
            f = PLATE / max(1e-9, bot[1] - top[1])
            cut = [top[i] + (bot[i] - top[i]) * f for i in range(3)]
            plate[(ix, 0, iz)], plate[(ix, 1, iz)] = list(top), list(cut)
            core[(ix, 0, iz)], core[(ix, 1, iz)] = list(cut), list(bot)
    for ix in (0, 1):
        a, b = plate[(ix, 0, 0)], plate[(ix, 0, 1)]
        inset = min(BEVEL, 0.25 * abs(b[2] - a[2]))
        a[2] += inset if a[2] <= b[2] else -inset
        b[2] -= inset if a[2] <= b[2] else -inset
    label = getattr(p, "label", p.name)
    return [new_part(p.name + "Plate", p.group, p.colour, plate, label),
            new_part(p.name + "Core", p.group, p.colour, core, label)]


def snap_part(p):
    """A copy whose two x end faces get equal sizes and one centre where their sizes differ by less than SNAP and
    their centres by less than LEVEL: a level box. Decimation's modellers level a receiver's gentle slopes (the akm's
    top is level); neighbouring panels then step by under LEVEL / 2, below a texel (2 pixels a unit)."""
    c = {k: list(v) for k, v in p.c.items()}
    for a in (1, 2):
        e = []
        for ix in (0, 1):
            vals = [c[k][a] for k in CORNERS if k[0] == ix]
            e.append((min(vals), max(vals)))
        w0, w1 = e[0][1] - e[0][0], e[1][1] - e[1][0]
        m0, m1 = (e[0][0] + e[0][1]) / 2, (e[1][0] + e[1][1]) / 2
        if abs(w0 - w1) >= SNAP or abs(m0 - m1) >= LEVEL:
            continue                                         # only a box: sloped or shifted panels stay as fitted
        w, mm = (w0 + w1) / 2, (m0 + m1) / 2
        for ix in (0, 1):
            m = mm
            lo, hi = e[ix]
            for k in CORNERS:
                if k[0] != ix:
                    continue
                v = c[k][a]
                f = (v - lo) / (hi - lo) if hi > lo else (0.0 if k[a] == 0 else 1.0)
                c[k][a] = m - w / 2 + f * w
    return new_part(p.name, p.group, p.colour, c, getattr(p, "label", p.name))


def panels(p, max_len):
    """A part longer than max_len along x cut into equal panels (each a slice of the same shape, faces meeting),
    as Decimation builds long receivers and handguards from panels: same outline, more parts, a tone each."""
    xs = [q[0] for q in p.c.values()]
    n = int(math.ceil((max(xs) - min(xs)) / max_len - 1e-9))
    if n <= 1:
        return [p]
    out = []
    for j in range(n):
        corners = {}
        for k in CORNERS:
            a, b = p.c[(0, k[1], k[2])], p.c[(1, k[1], k[2])]
            f = (j + k[0]) / n
            corners[k] = [a[i] + (b[i] - a[i]) * f for i in range(3)]
        out.append(new_part("%s_%d" % (p.name, j), p.group, p.colour, corners, getattr(p, "label", p.name)))
    return out


def fit_piece(tris, name, group, colour, tol=TOL, label=None, round_ok=True):
    """Decimation shape boxes for one piece of the mesh (triangles in model units): lofts along the piece's longest
    axis, cut where it bends, octagons for round bars along x."""
    lo = [min(p[i] for t in tris for p in t) for i in range(3)]
    hi = [max(p[i] for t in tris for p in t) for i in range(3)]
    ext = [hi[i] - lo[i] for i in range(3)]
    axis = max(range(3), key=lambda i: (ext[i], -i))
    label = label if label is not None else name
    if ext[axis] < 1e-9:
        return []
    segs = stepped_segments(tris, axis, lo[axis], hi[axis], tol)
    if not segs:                                         # no usable section: the bounding box
        ends = [(thick(lo[CYCLIC[axis][0]], hi[CYCLIC[axis][0]])), (thick(lo[CYCLIC[axis][1]], hi[CYCLIC[axis][1]]))]
        e = (ends[0][0], ends[0][1], ends[1][0], ends[1][1])
        return [new_part(name, group, colour, loft_corners(axis, lo[axis], hi[axis], e, e), label)]
    round_bar = round_ok and axis == 0 and is_round(tris, 0)
    parts = []
    for i, (t0, t1, lines) in enumerate(segs):
        ends = []
        for t in (t0, t1):
            blo, bhi = thick(edge(lines, 0, t), edge(lines, 1, t))
            clo, chi = thick(edge(lines, 2, t), edge(lines, 3, t))
            ends.append((blo, bhi, clo, chi))
        ends = snap(ends)
        pname = name if len(segs) == 1 else "%s%d" % (name, i)
        if round_bar:
            # CYCLIC[0] is (y, z): the ends are already (y_lo, y_hi, z_lo, z_hi)
            parts += octagon_parts(pname, group, colour, label, t0, t1, ends[0], ends[1])
        else:
            parts.append(new_part(pname, group, colour, loft_corners(axis, t0, t1, ends[0], ends[1]), label))
    return parts


# ---------------------------------------------------------------- part shape helpers

def kind(p):
    """Decimation's shape kinds on a part's absolute corners (the same rules as study.shape_kind): wedge (two corners
    merged), cuboid (a plain box), taper (one end face smaller than the other), skew (an end face moved)."""
    pl = [tuple(p.c[k]) for k in CORNERS]
    for i in range(8):
        for j in range(i + 1, 8):
            if sum(abs(pl[i][a] - pl[j][a]) for a in range(3)) < 0.02:
                return "wedge"
    lo = [min(q[a] for q in pl) for a in range(3)]
    hi = [max(q[a] for q in pl) for a in range(3)]
    if all(abs(q[a] - lo[a]) < 1e-3 or abs(q[a] - hi[a]) < 1e-3 for q in pl for a in range(3)):
        return "cuboid"
    for ax in range(3):
        f0 = [p.c[k] for k in CORNERS if k[ax] == 0]
        f1 = [p.c[k] for k in CORNERS if k[ax] == 1]
        for o in range(3):
            if o != ax:
                e0 = max(q[o] for q in f0) - min(q[o] for q in f0)
                e1 = max(q[o] for q in f1) - min(q[o] for q in f1)
                if abs(e0 - e1) > 0.02:
                    return "taper"
    return "skew"


def faces(p):
    """The 6 faces of a part as 4 corner lists in loop order."""
    out = []
    for a in range(3):
        b, c = CYCLIC[a]
        for s in (0, 1):
            quad = []
            for kb, kc in ((0, 0), (1, 0), (1, 1), (0, 1)):
                k = [0, 0, 0]
                k[a], k[b], k[c] = s, kb, kc
                quad.append(tuple(p.c[tuple(k)]))
            out.append(quad)
    return out


def planes(p):
    """Outward (normal, point) planes of a part (a convex hexahedron); collapsed faces are skipped."""
    pts = [p.c[k] for k in CORNERS]
    cen = [sum(q[i] for q in pts) / 8 for i in range(3)]
    out = []
    for quad in faces(p):
        n = cross(sub(quad[2], quad[0]), sub(quad[3], quad[1]))
        nl = math.sqrt(dot(n, n))
        if nl < 1e-12:
            continue
        n = tuple(x / nl for x in n)
        m = tuple(sum(q[i] for q in quad) / 4 for i in range(3))
        if dot(n, sub(cen, m)) > 0:
            n = tuple(-x for x in n)
        out.append((n, m))
    return out


def inside(shape, q, eps=0.0):
    """True when point q is inside a part's planes, grown by eps."""
    return all(dot(n, sub(q, m)) <= eps for n, m in shape)


# box faces of a hexahedron, wound outward, over the corners in HEXA order (tests/synth.py builds boxes the same way)
HEXA = [(0, 0, 0), (1, 0, 0), (1, 1, 0), (0, 1, 0), (0, 0, 1), (1, 0, 1), (1, 1, 1), (0, 1, 1)]
HEXA_FACES = [(0, 2, 1), (0, 3, 2), (4, 5, 6), (4, 6, 7), (0, 1, 5), (0, 5, 4),
              (3, 7, 6), (3, 6, 2), (0, 4, 7), (0, 7, 3), (1, 2, 6), (1, 6, 5)]


def hexa_tris(c):
    """The 12 triangles of a part's corners c[(ix, iy, iz)]."""
    v = [tuple(c[k]) for k in HEXA]
    return [(v[a], v[b], v[d]) for a, b, d in HEXA_FACES]


def profile(tris, axis, box, n):
    """[(t, b_lo, b_hi, c_lo, c_hi)] at n stations t = 0 .. 1 along `axis` of `box` (CYCLIC order for b, c): the
    section's extents there; stations without a section take their neighbours' values."""
    a0, a1 = box[axis]
    rows = []
    for i in range(n):
        t = (i + 0.5) / n
        cs = cross_section(tris, axis, a0 + (a1 - a0) * t) if a1 > a0 else None
        rows.append([t] + (list(cs[:4]) if cs else [None] * 4))
    known = [r for r in rows if r[1] is not None]
    if not known:
        b, c = CYCLIC[axis]
        return [[r[0], box[b][0], box[b][1], box[c][0], box[c][1]] for r in rows]
    for r in rows:
        if r[1] is None:
            near = min(known, key=lambda k: abs(k[0] - r[0]))
            r[1:] = near[1:]
    return rows


def bounds(parts):
    pts = [q for p in parts for q in p.c.values()]
    return [(min(q[i] for q in pts), max(q[i] for q in pts)) for i in range(3)]

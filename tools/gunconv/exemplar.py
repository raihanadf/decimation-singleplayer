#!/usr/bin/env python3
"""Exemplar areas (docs/superpowers/specs/2026-10-22-gun-converter-design.md, milestones 2 and 3): the anatomy areas of
a converted gun take the construction of the closest Decimation exemplars (the like gun first).

A refit carries every corner of an exemplar's parts from the exemplar's box to ours along the area's run axis, and across
it either box to box or relative to the section's extents at that station (curved and raked areas: magazine, grip,
stock, buttpad). The silhouette comes from the source, the construction (panels, rivets, grip segments, magazine slices,
grooves, bevels) from Decimation. Milestone 3:
- boxes without small outliers (fit.robust_box), so a stray part no longer stretches the map;
- after a box to box map, a warp along x (dynamic time warping of the side outline columns) moves the exemplar's features
  (a gas block, a sight base, a magwell) onto ours;
- assemblies first (action, front, butt): judged as one outline, so a labelling difference at an inner border (the
  receiver and magwell of a milled AK) no longer fails a match;
- then single areas, then sections: bins of BIN along x where the best exemplar's outline overlaps ours by GATE take its
  parts, a second exemplar the bins left, our fit the rest;
- exemplar parts keep their own group (moving parts stay slideModel; iron sights fold only with a folding like gun);
- stock, grip and buttpad follow a smoothed profile.
A refit is kept only when its side outline overlaps ours by GATE; the report names the gun of every part.
"""
import bisect
import math
import os
import sys
from collections import Counter, defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import dataset  # noqa: E402
import fit  # noqa: E402

GATE = 0.7            # side outline overlap a refit (or a section's bin) must reach
LIKE_MARGIN = 0.1     # another gun's exemplar wins only when it scores this much better than the like gun's
STATIONS = 12
MIN_SPAN = 0.15       # a section narrower than this is too thin to scale by: the box is used there
# areas that curve or rake follow the source's profile station by station; boxy areas (receiver, handguard, sights)
# map box to box: a section that jumps (a sight block) otherwise stretched thin exemplar parts into strands
PROFILE_AREAS = ("magazine", "grip", "stock", "buttpad")
SMOOTH_AREAS = ("grip", "stock", "buttpad")     # their target profile is smoothed (no wedges from the source's steps)
SMOOTH_TOL = 0.15     # an edge within this of a straight line becomes that line
ACTION = ("receiver", "magwell", "trigger", "trigger_guard", "rear_sight", "charging_handle", "bolt", "rail")
FRONT = ("handguard", "barrel", "front_sight", "muzzle", "pump")
# the body first (action and front warped as one keep their joint: two warps opened a gap behind the handguard), then
# each half, then the butt
ASSEMBLIES = (("body", ACTION + FRONT), ("action", ACTION), ("front", FRONT), ("butt", ("stock", "buttpad")))
BIN = 0.5             # section bins along x
MIN_RUN = 2           # an exemplar takes runs of at least this many bins
SECTION_CANDS = 2     # exemplars tried on the sections of an area (spec: the best, then a second on the bins left)
SECTION_SHARE = 0.4   # sections only when one exemplar takes at least this share of the bins (else a patchwork)
WARP_K = 10.0         # columns a unit for the warp along x
WARP_BAND = 0.3       # the warp moves a column at most this share of the length
WHOLE = 0.5           # parts at most this long along x are never cut at a section edge: they go by their centre
SLIVER = 0.05         # a part cut shorter than this along x is dropped


def lum(c):
    return 0.3 * c[0] + 0.59 * c[1] + 0.11 * c[2]


def new_group(group, folding):
    """The group an exemplar part keeps on our gun: the magazine and the bolt or slide move (Reload1, Fire), iron
    sights fold only when the like gun folds its own; Decimation's other names (defaultStockModel ...) are body."""
    if group in ("ammoModel", "slideModel"):
        return group
    if group == "defaultScopeModel" and folding:
        return group
    return "gunModel"


def make_area(corner_list, tones, groups):
    """An exemplar from corner dicts (for tests and for areas built in code)."""
    pts = [q for c in corner_list for q in c.values()]
    box = [[min(q[i] for q in pts), max(q[i] for q in pts)] for i in range(3)]
    parts = [{"c": [list(c[k]) for k in fit.CORNERS], "tone": list(t) if t else None, "group": g, "kind": "",
              "detail": False} for c, t, g in zip(corner_list, tones, groups)]
    known = [t for t in tones if t]
    tone = [sum(t[i] for t in known) / len(known) for i in range(3)] if known else None
    return {"box": box, "parts": parts, "tone": tone, "count": len(parts)}


def corners_of(ex):
    return [{k: list(p["c"][i]) for i, k in enumerate(fit.CORNERS)} for p in ex["parts"]]


def box_of(corner_list):
    pts = [q for c in corner_list for q in c.values()]
    return [[min(q[i] for q in pts), max(q[i] for q in pts)] for i in range(3)]


def target_area(corner_list):
    plain = box_of(corner_list)
    axis = max(range(3), key=lambda i: (plain[i][1] - plain[i][0], -i))
    box = [list(ab) for ab in fit.robust_box(corner_list, keep_axis=axis)]
    return {"box": box, "axis": axis, "corners": corner_list}


def profile_of(corner_list, axis, box):
    return fit.profile([t for c in corner_list for t in fit.hexa_tris(c)], axis, box, STATIONS)


END_SHRINK = 0.7      # an end station narrower than this share of its neighbour is a chamfer or a slanted cap


def smooth_profile(prof, tol=SMOOTH_TOL):
    """End stations that collapse (a slanted butt cap, a chamfered grip bottom: under END_SHRINK of the neighbour's
    span on either axis) take the neighbour's section, so an exemplar's end is not squeezed into them; then each of the
    4 edges: the least squares line when every station lies within tol of it, else a 3 station moving average (the
    ends kept)."""
    prof = [list(r) for r in prof]
    if len(prof) >= 3:
        for end, nb in ((0, 1), (len(prof) - 1, len(prof) - 2)):
            e, n = prof[end], prof[nb]
            if (e[2] - e[1] < END_SHRINK * (n[2] - n[1])) or (e[4] - e[3] < END_SHRINK * (n[4] - n[3])):
                prof[end] = [e[0]] + n[1:]
    out = [list(r) for r in prof]
    ts = [r[0] for r in prof]
    for e in range(1, 5):
        vs = [r[e] for r in prof]
        p, q = fit.line_fit(ts, vs)
        if max(abs(v - (p + q * t)) for t, v in zip(ts, vs)) <= tol:
            for r, t in zip(out, ts):
                r[e] = p + q * t
        else:
            for i in range(1, len(vs) - 1):
                out[i][e] = (vs[i - 1] + vs[i] + vs[i + 1]) / 3.0
    return out


WIDE = 1.6            # a station spanning more than this times the median span is an outlier (robust_profile)


def robust_profile(prof):
    """Stations whose span along either cross axis is more than WIDE times the median span (a piece that is not part
    of the area widened them: a latch beside a magazine's top) take the nearest station that is not one."""
    spans = [[r[2] - r[1], r[4] - r[3]] for r in prof]
    med = [sorted(s[i] for s in spans)[len(spans) // 2] for i in range(2)]
    bad = [any(s[i] > WIDE * med[i] for i in range(2) if med[i] > 1e-9) for s in spans]
    good = [i for i, b in enumerate(bad) if not b]
    out = [list(r) for r in prof]
    if good and any(bad):
        mid = (len(prof) - 1) / 2.0
        for i, r in enumerate(prof):
            if bad[i]:
                j = min(good, key=lambda g: (abs(g - i), abs(g - mid)))
                out[i] = [r[0]] + list(prof[j][1:])
    # an end station narrower than its neighbour and off its line (its middle more than half the neighbour's span
    # away) is a piece beside the area, not the area's end
    if len(out) >= 3:
        for end, nb in ((0, 1), (len(out) - 1, len(out) - 2)):
            e, n = out[end], out[nb]
            for lo, hi in ((1, 2), (3, 4)):
                if e[hi] - e[lo] < n[hi] - n[lo] and abs((e[lo] + e[hi]) - (n[lo] + n[hi])) / 2 > 0.5 * (n[hi] - n[lo]):
                    out[end] = [e[0]] + list(n[1:])
                    break
    return out


GAP = 0.05            # sections of two parts closer than this along an axis count as one stretch


def stretches(intervals):
    """Overlapping (or within GAP) intervals merged; the longest first."""
    out = []
    for lo, hi in sorted(intervals):
        if out and lo <= out[-1][1] + GAP:
            out[-1][1] = max(out[-1][1], hi)
        else:
            out.append([lo, hi])
    return sorted(out, key=lambda r: -(r[1] - r[0]))


def target_profile(area, smooth=False):
    """Our area's profile for an exemplar to follow: at each station the longest connected stretch of the parts'
    sections along each cross axis (a piece apart from the area, a latch beside a magazine's top, does not widen it),
    outlier stations replaced (robust_profile), smoothed for stock, grip and buttpad (smooth_profile)."""
    import details
    a, box = area["axis"], area["box"]
    b, c = fit.CYCLIC[a]
    prof = []
    for i in range(STATIONS):
        t = (i + 0.5) / STATIONS
        at = box[a][0] + (box[a][1] - box[a][0]) * t
        row = [t]
        for other in (b, c):
            ivs = [e for e in (details.section_extent(cc, a, at, other) for cc in area["corners"]
                               if min(q[a] for q in cc.values()) <= at <= max(q[a] for q in cc.values())) if e]
            st = stretches(ivs)
            row += list(st[0]) if st else [None, None]
        prof.append(row)
    known = [r for r in prof if r[1] is not None and r[3] is not None]
    if not known:
        return [[r[0], box[b][0], box[b][1], box[c][0], box[c][1]] for r in prof]
    for r in prof:
        if r[1] is None or r[3] is None:
            r[1:] = min(known, key=lambda k: abs(k[0] - r[0]))[1:]
    prof = robust_profile(prof)
    return smooth_profile(prof) if smooth else prof


def interp(prof, t):
    ts = [r[0] for r in prof]
    if t <= ts[0]:
        return prof[0][1:]
    if t >= ts[-1]:
        return prof[-1][1:]
    for i in range(len(ts) - 1):
        if ts[i] <= t <= ts[i + 1]:
            f = (t - ts[i]) / (ts[i + 1] - ts[i])
            return [prof[i][j] + f * (prof[i + 1][j] - prof[i][j]) for j in range(1, 5)]
    return prof[-1][1:]


def refit(ex, area, follow=True, smooth=False):
    """The exemplar's parts carried onto the target area: corner dicts. follow: across the run axis relative to each
    station's section (curved and raked areas; smooth: the target's profile smoothed first), else box to box. Both
    boxes are robust (fit.robust_box)."""
    a = area["axis"]
    b, c = fit.CYCLIC[a]
    src = corners_of(ex)
    ebox, tbox = [list(ab) for ab in fit.robust_box(src, keep_axis=a)], area["box"]
    if follow:
        eprof = profile_of(src, a, ebox)
        tprof = target_profile(area, smooth)
    else:
        eprof = [[0.5, ebox[b][0], ebox[b][1], ebox[c][0], ebox[c][1]]]
        tprof = [[0.5, tbox[b][0], tbox[b][1], tbox[c][0], tbox[c][1]]]
    e0, e1 = ebox[a]
    t0, t1 = tbox[a]

    def across(v, lo, hi, blo, bhi):
        if hi - lo < MIN_SPAN:
            lo, hi = blo, bhi
        return (v - lo) / (hi - lo) if hi > lo else 0.5

    out = []
    for part in src:
        nc = {}
        for k, p in part.items():
            t = (p[a] - e0) / (e1 - e0) if e1 > e0 else 0.5
            ee, gg = interp(eprof, t), interp(tprof, t)
            q = [0.0, 0.0, 0.0]
            q[a] = t0 + t * (t1 - t0)
            for idx, j in ((b, 0), (c, 2)):
                u = across(p[idx], ee[j], ee[j + 1], ebox[idx][0], ebox[idx][1])
                glo, ghi = gg[j], gg[j + 1]
                if ghi - glo < MIN_SPAN:
                    glo, ghi = tbox[idx]
                q[idx] = glo + u * (ghi - glo)
            nc[k] = q
        out.append(nc)
    return out


def raster(corner_list, box, k=None, size=(48, 48)):
    """Side outline (x, y) of parts; with k None it is scaled into `box` (normalised), else k pixels a unit."""
    hulls = [fit.hull([(q[0], q[1]) for q in c.values()]) for c in corner_list]
    if k is None:
        sx = (size[0] - 2) / max(1e-6, box[0][1] - box[0][0])
        sy = (size[1] - 2) / max(1e-6, box[1][1] - box[1][0])
        hulls = [[((x - box[0][0]) * sx, (y - box[1][0]) * sy) for x, y in h] for h in hulls]
        return fit.silhouette(hulls, 0, 0, 1.0, size[0], size[1])
    w = int((box[0][1] - box[0][0]) * k) + 3
    h = int((box[1][1] - box[1][0]) * k) + 3
    return fit.silhouette(hulls, box[0][0], box[1][0], k, w, h)


def iou(a, b):
    from PIL import ImageChops
    inter = ImageChops.logical_and(a, b).histogram()[-1]
    union = ImageChops.logical_or(a, b).histogram()[-1]
    return inter / union if union else 0.0


def overlap(corners_a, corners_b, k=16.0):
    """Side outline overlap of two part sets in the same space."""
    box = box_of(list(corners_a) + list(corners_b))
    return iou(raster(corners_a, box, k), raster(corners_b, box, k))


def score(ex, area):
    """Normalised side outline overlap (0.7) and agreement of the box proportions (0.3)."""
    src = corners_of(ex)
    shape = iou(raster(src, box_of(src)), raster(area["corners"], area["box"]))
    eb, tb = box_of(src), area["box"]
    d = [max(1e-3, eb[i][1] - eb[i][0]) for i in range(3)]
    e = [max(1e-3, tb[i][1] - tb[i][0]) for i in range(3)]
    ratios = [abs(math.log((d[0] / d[1]) / (e[0] / e[1]))), abs(math.log((d[2] / d[1]) / (e[2] / e[1])))]
    return 0.7 * shape + 0.3 * math.exp(-sum(ratios) / 2)


def pick(scores, like):
    """(gun, score): the like gun's unless another scores more than LIKE_MARGIN better."""
    best = max(scores, key=lambda s: s[1])
    for name, s in scores:
        if name == like and s >= best[1] - LIKE_MARGIN:
            return name, s
    return best


def ranked(scores, like):
    """The pick first, then the others best first."""
    first = pick(scores, like)
    return [first] + sorted((s for s in scores if s[0] != first[0]), key=lambda s: -s[1])


# ---------------------------------------------------------------- the warp along x

def columns_of(corner_list, box, k):
    img = raster(corner_list, box, k)
    w, h = img.size
    px = img.load()
    return [frozenset(y for y in range(h) if px[x, y]) for x in range(w)]


def coldist(a, b):
    if not a and not b:
        return 0.0
    if not a or not b:
        return 1.0
    return 1.0 - len(a & b) / len(a | b)


def warp_x(warped, target, k=WARP_K, band=WARP_BAND):
    """The exemplar's parts (already mapped box to box) moved along x so its side outline columns line up with ours:
    dynamic time warping of the columns (steps that stretch or shrink by at most 2, within `band` of the length), the
    path turned into a monotone map of x. Kept only when it raises the overlap."""
    box = box_of(list(warped) + list(target))
    T, E = columns_of(target, box, k), columns_of(warped, box, k)
    n, m = len(T), len(E)
    if n < 4 or m < 4:
        return warped
    inf = float("inf")
    D = [[inf] * (m + 1) for _ in range(n + 1)]
    P = [[None] * (m + 1) for _ in range(n + 1)]
    D[0][0] = 0.0
    bw = max(3, int(band * max(n, m)))
    for i in range(1, n + 1):
        for j in range(max(1, i - bw), min(m, i + bw) + 1):
            cost = coldist(T[i - 1], E[j - 1])
            best, arg = inf, None
            for di, dj in ((1, 1), (1, 2), (2, 1)):
                pi, pj = i - di, j - dj
                if pi < 0 or pj < 0 or D[pi][pj] == inf:
                    continue
                v = D[pi][pj] + cost
                if di == 2:
                    v += coldist(T[i - 2], E[j - 1])
                if dj == 2:
                    v += coldist(T[i - 1], E[j - 2])
                if v < best:
                    best, arg = v, (pi, pj)
            D[i][j], P[i][j] = best, arg
    if D[n][m] == inf:
        return warped
    i, j, pairs = n, m, []
    while i > 0 and j > 0 and P[i][j] is not None:
        pairs.append((i - 1, j - 1))
        i, j = P[i][j]
    acc = defaultdict(list)
    for ti, ej in pairs:
        acc[ej].append(ti)
    cols = sorted(acc)
    xe = [box[0][0] + (ej + 0.5) / k for ej in cols]
    xt = [box[0][0] + (sum(acc[ej]) / len(acc[ej]) + 0.5) / k for ej in cols]
    for q in range(1, len(xt)):                           # monotone
        xt[q] = max(xt[q], xt[q - 1])
    sm = list(xt)                                         # a light smoothing of the steps
    for q in range(1, len(xt) - 1):
        sm[q] = (xt[q - 1] + xt[q] + xt[q + 1]) / 3.0

    def f(x):
        if x <= xe[0]:
            return sm[0] + (x - xe[0])
        if x >= xe[-1]:
            return sm[-1] + (x - xe[-1])
        q = bisect.bisect_right(xe, x) - 1
        t = (x - xe[q]) / (xe[q + 1] - xe[q]) if xe[q + 1] > xe[q] else 0.0
        return sm[q] + t * (sm[q + 1] - sm[q])
    out = [{kk: [f(v[0]), v[1], v[2]] for kk, v in c.items()} for c in warped]
    return out if overlap(out, target) > overlap(warped, target) else warped


def fitted(ex, tarea, follow, smooth=False):
    """refit, then the warp along x for box mapped areas that run along x."""
    w = refit(ex, tarea, follow, smooth)
    if not follow and tarea["axis"] == 0:
        w = warp_x(w, tarea["corners"])
    return w


# ---------------------------------------------------------------- sections

def clip_x(c, x0, x1):
    """A part cut to x0 .. x1 (corner dicts), or None when nothing of it lies there. Parts whose box edges do not run
    along x, and short ones (WHOLE), go whole by their centre."""
    xs = [q[0] for q in c.values()]
    lo, hi = min(xs), max(xs)
    if hi <= x0 or lo >= x1:
        return None
    if lo >= x0 and hi <= x1:
        return {k: list(v) for k, v in c.items()}
    along = all(abs(c[(1, iy, iz)][0] - c[(0, iy, iz)][0]) > 0.8 * math.dist(c[(1, iy, iz)], c[(0, iy, iz)])
                for iy in (0, 1) for iz in (0, 1))
    if hi - lo <= WHOLE or not along:
        mid = (lo + hi) / 2
        return {k: list(v) for k, v in c.items()} if x0 <= mid < x1 else None
    out = {}
    for iy in (0, 1):
        for iz in (0, 1):
            p, q = c[(0, iy, iz)], c[(1, iy, iz)]
            a, b = (p, q) if p[0] <= q[0] else (q, p)
            ka, kb = ((0, iy, iz), (1, iy, iz)) if p[0] <= q[0] else ((1, iy, iz), (0, iy, iz))
            for key, x in ((ka, max(x0, a[0])), (kb, min(x1, b[0]))):
                t = (x - a[0]) / (b[0] - a[0]) if b[0] > a[0] else 0.0
                out[key] = [a[i] + t * (b[i] - a[i]) for i in range(3)]
    if max(q[0] for q in out.values()) - min(q[0] for q in out.values()) < SLIVER:
        return None
    return out


def bin_overlaps(warped, target, edges, k=16.0):
    """Side outline overlap of two part sets inside each x bin (edges[i] .. edges[i + 1])."""
    box = box_of(list(warped) + list(target))
    a, b = raster(warped, box, k), raster(target, box, k)
    pa, pb = a.load(), b.load()
    out = []
    for x0, x1 in zip(edges, edges[1:]):
        c0 = max(0, int(round((x0 - box[0][0]) * k)))
        c1 = min(a.width, int(round((x1 - box[0][0]) * k)))
        inter = union = 0
        for x in range(c0, c1):
            for y in range(a.height):
                u, v = pa[x, y], pb[x, y]
                inter += 1 if (u and v) else 0
                union += 1 if (u or v) else 0
        out.append(inter / union if union else 0.0)
    return out


def runs(flags, value=True):
    out, start = [], None
    for i, f in enumerate(list(flags) + [not value]):
        if f == value:
            start = i if start is None else start
        elif start is not None:
            out.append((start, i - 1))
            start = None
    return out


def sections(tarea, cands, follow=False, ours_cols=None):
    """Bins of BIN along x: each of the candidates [(name, score, exemplar)] in turn takes the free bins where its
    outline overlaps ours by GATE (runs of MIN_RUN bins at least); a later one only outside the first one's span (no
    patchwork of two guns). (owners per bin, bin edges, {name: warped})."""
    x0, x1 = tarea["box"][0]
    nb = max(1, int(math.ceil((x1 - x0) / BIN - 1e-9)))
    edges = [x0 + (x1 - x0) * i / nb for i in range(nb + 1)]
    owner = [None] * nb
    warped = {}
    span = None                                # bins from the first exemplar's first to its last: no one else inside
    for name, _, ex in cands[:SECTION_CANDS]:
        w = fitted(ex, tarea, follow)
        loc = bin_overlaps(w, tarea["corners"], edges)
        agree = [None] * nb
        if ours_cols is not None:
            agree, _ = material_agreement(tarea["corners"], ours_cols, w, [p.get("tone") for p in ex["parts"]], edges)
        free = [owner[i] is None and loc[i] >= GATE and agree[i] is not False and
                (span is None or not span[0] <= i <= span[1]) for i in range(nb)]
        took = False
        for a, b in runs(free):
            if b - a + 1 >= MIN_RUN:
                for i in range(a, b + 1):
                    owner[i] = name
                took = True
        if took:
            warped[name] = w
            if span is None:
                mine = [i for i in range(nb) if owner[i] == name]
                span = (mine[0], mine[-1])
    return owner, edges, warped


# ---------------------------------------------------------------- materials along the run

MATERIAL_TOL = 0.25   # an exemplar fails when wood and steel disagree with ours in more than this share of the bins
MATERIAL_DIFF = 0.45  # a bin disagrees when the coloured (wood, polymer) shares differ by more than this


def coloured(c):
    """True for wood, polymer or tan (saturation over 0.3), False for grey metal (under 0.12), None between: a dark
    brownish grey (Decimation's AKM grip, 46,37,38) votes for neither."""
    if c is None:
        return None
    mx, mn = max(c[:3]), min(c[:3])
    sat = (mx - mn) / mx if mx > 0 else 0.0
    if sat < 0.12:
        return False
    if sat > 0.3:
        return True
    return None


def material_bins(corner_list, colours, edges):
    """Per x bin the coloured share of the parts' side area there (None where no part with a known tone is)."""
    out = []
    boxes = [[(min(q[i] for q in c.values()), max(q[i] for q in c.values())) for i in range(3)] for c in corner_list]
    for x0, x1 in zip(edges, edges[1:]):
        tot = col = 0.0
        for b, c in zip(boxes, colours):
            k = coloured(c)
            if k is None:
                continue
            w = max(0.0, min(x1, b[0][1]) - max(x0, b[0][0])) * (b[1][1] - b[1][0])
            tot += w
            col += w if k else 0.0
        out.append(col / tot if tot > 1e-9 else None)
    return out


def material_agreement(ours_c, ours_cols, ex_c, ex_cols, edges):
    """Per bin True (agree), False (wood against steel) or None (unknown), and the share of disagreeing bins."""
    a, b = material_bins(ours_c, ours_cols, edges), material_bins(ex_c, ex_cols, edges)
    per = [None if p is None or q is None else abs(p - q) <= MATERIAL_DIFF for p, q in zip(a, b)]
    known = [v for v in per if v is not None]
    return per, (sum(1 for v in known if not v) / len(known) if known else 0.0)


def bins_of(box, step=BIN):
    x0, x1 = box[0]
    nb = max(1, int(math.ceil((x1 - x0) / step - 1e-9)))
    return [x0 + (x1 - x0) * i / nb for i in range(nb + 1)]


# ---------------------------------------------------------------- holes left between areas

HOLE_LONG = 0.25      # a hole in the side outline at least this long ...
HOLE_TALL = 0.3       # ... and this tall, where our fit had parts and the exemplars left none, is filled from the fit


def fill_holes(original, final, k=16.0):
    """Original parts (the fit, before the exemplars) cut to x ranges where the final side outline lost a stretch of
    at least HOLE_TALL in a run of columns at least HOLE_LONG long (a barrel starting later than ours left a gap
    behind it). Returns [(part corners, original index)]."""
    if not original or not final:
        return []
    box = box_of([p.c for p in original] + [p.c for p in final])
    a = raster([p.c for p in original], box, k)
    b = raster([p.c for p in final], box, k)
    pa, pb = a.load(), b.load()
    need = int(math.ceil(HOLE_TALL * k))
    cols = []
    for x in range(a.width):
        run = best = 0
        for y in range(a.height):
            if pa[x, y] and not pb[x, y]:
                run += 1
                best = max(best, run)
            else:
                run = 0
        cols.append(best >= need)
    out = []
    for c0, c1 in runs(cols):
        if (c1 - c0 + 1) / k < HOLE_LONG:
            continue
        x0, x1 = box[0][0] + c0 / k - 0.05, box[0][0] + (c1 + 1) / k + 0.05
        for i, p in enumerate(original):
            cc = clip_x(p.c, x0, x1)
            if cc and not any(_covered(cc, q) for q in final):
                out.append((cc, i))
    return out


def _covered(c, q):
    """A clipped piece c lies inside final part q's box (nothing to fill there)."""
    qb = [(min(v[i] for v in q.c.values()), max(v[i] for v in q.c.values())) for i in range(3)]
    return all(qb[i][0] - 0.02 <= min(v[i] for v in c.values()) and max(v[i] for v in c.values()) <= qb[i][1] + 0.02
               for i in range(3))


# ---------------------------------------------------------------- the areas of a converted gun

TONE_STEP = 18.0      # an exemplar part or a detail differs from its base by at most this in luminance


def shade(base, delta):
    """base (RGB) lighter or darker by delta in luminance (clamped to TONE_STEP), its hue kept."""
    d = max(-TONE_STEP, min(TONE_STEP, delta))
    lb = max(1.0, lum(base))
    f = max(0.0, lb + d) / lb
    return tuple(int(round(max(0, min(255, v * f)))) for v in base)


def _tone(mean, p, exl):
    """Our area's tone shaded as the exemplar part is against its area's mean (additive in luminance: Decimation's
    dark islands made a multiplier blow up on lighter guns)."""
    if exl and p.get("tone"):
        return shade(mean, lum(p["tone"]) - exl)
    return tuple(int(round(v)) for v in mean)


def _mean(colours):
    return [sum(c[j] for c in colours) / len(colours) for j in range(3)] if colours else None


def apply(parts, cat, like, header, data=None, labels=None, folding=False, exemplar_tones=False, family=None):
    """Exemplar areas on our fitted parts: (parts, report, parts per Decimation gun, plan for the detail layer). Every
    part gets .area; replaced parts carry .exemplar = the gun. plan: {area: {"gun", "ranges"}} for the areas (or x
    ranges of them) that kept our fit: their details come from that gun (details.apply). exemplar_tones: the source
    has no colours of its own (its tones were borrowed from the like gun by position), so exemplar parts keep the
    exemplar's own tones (a wooden grip stays wood where the borrowed tone was the receiver's black). family: the
    Decimation guns that may give parts (the closest to ours overall, the like gun among them); all of the category
    when None."""
    data = data or dataset.load()
    if labels is None:
        labels = [a for a, _ in dataset.label_shapes(dataset.shapes_of_parts(parts), header, cat)]
    else:
        labels = [lab[0] if isinstance(lab, (tuple, list)) else lab for lab in labels]
    for p, a in zip(parts, labels):
        p.area = a
    by = defaultdict(list)
    for i, a in enumerate(labels):
        by[a].append(i)
    guns = {n: g for n, g in data["guns"].items() if g["cat"] == cat and (family is None or n in family or n == like)}
    ours_tone = {a: _mean([parts[i].colour for i in idx]) for a, idx in by.items()}
    keep = set(range(len(parts)))
    added, report, by_gun, plan, done = [], {}, Counter(), {}, set()

    def new_parts(warped, items, gname, fallback, ex_mean):
        """items [(exemplar part, its area, the exemplar area's mean tone)]."""
        out = []
        for c, (p, a, exl) in zip(warped, items):
            if exemplar_tones and p.get("tone"):
                colour = tuple(int(round(v)) for v in p["tone"])
            elif ours_tone.get(a):
                colour = _tone(ours_tone[a], p, exl)
            elif p.get("tone") and ex_mean and fallback:
                f = min(1.6, max(0.6, lum(fallback) / max(1.0, lum(ex_mean))))
                colour = tuple(int(round(min(255, v * f))) for v in p["tone"])
            else:
                colour = tuple(int(round(v)) for v in (fallback or (40, 40, 40)))
            q = fit.new_part("%s_%s" % (a, gname), new_group(p.get("group", "gunModel"), folding), colour, c, a)
            q.exemplar, q.area = gname, a
            out.append(q)
        return out

    # 1. assemblies: one outline over several areas
    for name, areas in ASSEMBLIES:
        mine = [a for a in areas if a in by and a not in done]
        if not mine or (len(mine) == 1 and name != "butt") or any(a in done for a in areas if a in by):
            continue                                       # a single area is judged as one below
        if name == "body" and not (any(a in ACTION for a in mine) and any(a in FRONT for a in mine)):
            continue                                       # one half only: judged as that half
        idx = [i for a in mine for i in by[a]]
        target = [parts[i].c for i in idx]
        tarea = target_area(target)
        follow = name == "butt"
        cands = {}
        for gname, g in guns.items():
            items = [(pp, a, lum(g["areas"][a]["tone"]) if g["areas"][a].get("tone") else None)
                     for a in areas if a in g["areas"] for pp in g["areas"][a]["parts"]]
            if items and any(a in g["areas"] for a in mine):
                cands[gname] = ({"parts": [it[0] for it in items]}, items)
        if not cands:
            continue
        gname, s = pick([(n, score(ex, tarea)) for n, (ex, _) in cands.items()], like)
        ex, items = cands[gname]
        warped = fitted(ex, tarea, follow, smooth=follow)
        ov = overlap(warped, target)
        if ov < GATE:
            continue
        if not exemplar_tones:                         # a one colour source's tones are borrowed: no material to compare
            _, bad = material_agreement(target, [parts[i].colour for i in idx], warped,
                                        [it[0].get("tone") for it in items], bins_of(tarea["box"]))
            if bad > MATERIAL_TOL:
                continue
        fallback = _mean([parts[i].colour for i in idx])
        known = [it[0]["tone"] for it in items if it[0].get("tone")]
        made = new_parts(warped, items, gname, fallback, _mean(known))
        theirs = set(a for _, a, _ in items)
        taken = [a for a in mine if a in theirs]           # our areas the exemplar lacks (a moving bolt) stay ours
        added += made
        keep -= set(i for a in taken for i in by[a])
        done |= set(taken)
        by_gun[gname] += len(made)
        counts = Counter(q.area for q in made)
        for a in sorted(set(taken) | set(counts)):
            report[a] = {"gun": gname, "parts": counts.get(a, 0), "score": round(s, 3), "overlap": round(ov, 3),
                         "via": name}

    # 2. single areas, then 3. sections of them
    for area, idx in sorted(by.items()):
        if area in done:
            continue
        target = [parts[i].c for i in idx]
        tarea = target_area(target)
        cands = {n: g["areas"][area] for n, g in guns.items() if area in g["areas"]}
        if not cands:
            report[area] = {"fit": len(idx)}
            continue
        order = ranked([(n, score(ex, tarea)) for n, ex in cands.items()], like)
        gname, s = order[0]
        follow = area in PROFILE_AREAS
        exl = lambda n: lum(cands[n]["tone"]) if cands[n].get("tone") else None  # noqa: E731
        warped = fitted(cands[gname], tarea, follow, smooth=area in SMOOTH_AREAS)
        ov = overlap(warped, target)
        ours_cols = None if exemplar_tones else [parts[i].colour for i in idx]
        bad = 0.0
        if ours_cols is not None:
            _, bad = material_agreement(target, ours_cols, warped, [p.get("tone") for p in cands[gname]["parts"]],
                                        bins_of(tarea["box"]))
        if ov >= GATE and bad <= MATERIAL_TOL:
            made = new_parts(warped, [(p, area, exl(gname)) for p in cands[gname]["parts"]], gname, None, None)
            added += made
            keep -= set(idx)
            by_gun[gname] += len(made)
            report[area] = {"gun": gname, "parts": len(made), "score": round(s, 3), "overlap": round(ov, 3),
                            "via": "area"}
            continue
        owner = None
        if not follow and tarea["axis"] == 0:
            owner, edges, warped_of = sections(tarea, [(n, sc, cands[n]) for n, sc in order], ours_cols=ours_cols)
        if not owner or max(Counter(o for o in owner if o).values(), default=0) < SECTION_SHARE * len(owner):
            report[area] = {"fit": len(idx), "best": gname, "overlap": round(ov, 3)}
            plan[area] = {"gun": gname}
            continue
        secs, fit_ranges, kept = [], [], []
        i = 0
        while i < len(owner):
            j = i
            while j + 1 < len(owner) and owner[j + 1] == owner[i]:
                j += 1
            x0, x1 = edges[i], edges[j + 1]
            if owner[i] is None:
                fit_ranges.append((round(x0, 5), round(x1, 5)))
                for t in idx:
                    c = clip_x(parts[t].c, x0, x1)
                    if c:
                        q = fit.new_part(parts[t].name, parts[t].group, parts[t].colour, c,
                                         getattr(parts[t], "label", parts[t].name))
                        q.area = area
                        if getattr(parts[t], "round", False):
                            q.round = True
                        kept.append(q)
            else:
                n = owner[i]
                w = warped_of[n]
                pieces = []
                for c, p in zip(w, cands[n]["parts"]):
                    cc = clip_x(c, x0, x1)
                    if cc:
                        pieces.append((cc, p))
                made = new_parts([cc for cc, _ in pieces], [(p, area, exl(n)) for _, p in pieces], n, None, None)
                added += made
                by_gun[n] += len(made)
                secs.append({"gun": n, "from": round(x0, 3), "to": round(x1, 3), "parts": len(made)})
            i = j + 1
        keep -= set(idx)
        added += kept
        report[area] = {"sections": secs, "fit": len(kept), "score": round(s, 3), "overlap": round(ov, 3)}
        if fit_ranges:
            plan[area] = {"gun": gname, "ranges": fit_ranges}
    out = [p for i, p in enumerate(parts) if i in keep] + added
    gone = [p for i, p in enumerate(parts) if i not in keep]
    for cc, i in fill_holes(gone, out):
        src = gone[i]
        q = fit.new_part(src.name, src.group, src.colour, cc, getattr(src, "label", src.name))
        q.area = src.area
        q.filled = True
        out.append(q)
        r = report.setdefault(src.area, {})
        r["filled"] = r.get("filled", 0) + 1
    return out, report, dict(by_gun), plan

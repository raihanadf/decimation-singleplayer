#!/usr/bin/env python3
"""Exemplar areas (docs/superpowers/specs/2026-10-22-gun-converter-design.md, milestone 2): for each anatomy area of a
converted gun, the closest exemplar among Decimation's guns of the category (the like gun first) is refitted onto the
source's area: every corner is carried along the area's run axis from the exemplar's box to ours, and across it
relative to the section's extents at that station. The silhouette comes from the source, the construction (panels,
rivets, grip segments, magazine slices, grooves, bevels) from Decimation. A refit is kept only when its side outline
overlaps the source area's by GATE; the report says which gun gave how many parts.
"""
import math
import os
import sys
from collections import Counter

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import dataset  # noqa: E402
import fit  # noqa: E402

GATE = 0.7            # side outline overlap a refit must reach
LIKE_MARGIN = 0.1     # another gun's exemplar wins only when it scores this much better than the like gun's
STATIONS = 12
MIN_SPAN = 0.15       # a section narrower than this is too thin to scale by: the box is used there


def lum(c):
    return 0.3 * c[0] + 0.59 * c[1] + 0.11 * c[2]


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
    box = box_of(corner_list)
    axis = max(range(3), key=lambda i: (box[i][1] - box[i][0], -i))
    return {"box": box, "axis": axis, "corners": corner_list}


def profile_of(corner_list, axis, box):
    return fit.profile([t for c in corner_list for t in fit.hexa_tris(c)], axis, box, STATIONS)


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


def refit(ex, area):
    """The exemplar's parts carried onto the target area: corner dicts."""
    a = area["axis"]
    b, c = fit.CYCLIC[a]
    src = corners_of(ex)
    ebox, tbox = box_of(src), area["box"]
    eprof = profile_of(src, a, ebox)
    tprof = profile_of(area["corners"], a, tbox)
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


def apply(parts, cat, like, header, data=None):
    """Exemplar areas on our fitted parts: (parts, report, byGun). Replaced parts carry .exemplar = the gun."""
    data = data or dataset.load()
    labels = dataset.label_shapes(dataset.shapes_of_parts(parts), header, cat)
    by = {}
    for i, (area, _) in enumerate(labels):
        by.setdefault(area, []).append(i)
    keep = set(range(len(parts)))
    added, report, by_gun = [], {}, Counter()
    for area, idx in sorted(by.items()):
        target = [parts[i].c for i in idx]
        tarea = target_area(target)
        cands = {n: g["areas"][area] for n, g in data["guns"].items() if g["cat"] == cat and area in g["areas"]}
        if not cands:
            report[area] = {"fit": len(idx)}
            continue
        name, s = pick([(n, score(ex, tarea)) for n, ex in cands.items()], like)
        ex = cands[name]
        warped = refit(ex, tarea)
        ov = overlap(warped, target)
        if ov < GATE:
            report[area] = {"fit": len(idx), "best": name, "overlap": round(ov, 3)}
            continue
        groups = Counter(parts[i].group for i in idx)
        group = groups.most_common(1)[0][0]
        ours = [parts[i].colour for i in idx]
        mean = [sum(c[j] for c in ours) / len(ours) for j in range(3)]
        exl = lum(ex["tone"]) if ex.get("tone") else None
        for c, p in zip(warped, ex["parts"]):
            f = 1.0
            if exl and p.get("tone"):
                f = min(1.6, max(0.6, lum(p["tone"]) / max(1.0, exl)))
            q = fit.new_part("%s_%s" % (area, name), group, tuple(int(round(min(255, v * f))) for v in mean), c, area)
            q.exemplar = name
            added.append(q)
        keep -= set(idx)
        by_gun[name] += len(warped)
        report[area] = {"gun": name, "parts": len(warped), "score": round(s, 3), "overlap": round(ov, 3)}
    out = [p for i, p in enumerate(parts) if i in keep] + added
    return out, report, dict(by_gun)

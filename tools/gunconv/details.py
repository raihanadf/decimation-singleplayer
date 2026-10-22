#!/usr/bin/env python3
"""The detail layer (docs/superpowers/specs/2026-10-22-gun-converter-design.md, milestone 3, sections 1 and 2).

Decimation's guns carry many small parts on their bodies: rivets, pins through the receiver, ribs, strips, levers, sight
ears, screws (study.detail_parts finds them: small or thin parts sitting on a bigger host). The dataset stores, per area
of every Decimation gun, a record of each detail: the face it stands on (an axis and a side, or a pin through), its place
on that face (u along the area between its ends, v across between the area's outline edges at that u), and its corners
relative to that point on the host's surface. This module makes those records (records) and places them on a converted
gun's areas (place, apply): at the same u and v on our area, each corner on our surface (found by probing our parts
along the face's normal), the inner face SEAT inside, the outer face as proud as Decimation made it. A detail is kept
only when it is seated, not hidden, not inside a part of another area and not next to a detail that is already there.
"""
import os
import sys
from collections import Counter

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(os.path.dirname(HERE), "guns"))
import fit  # noqa: E402
import study  # noqa: E402

PLANE = {0: (1, 2), 1: (0, 2), 2: (0, 1)}   # face axis -> (u axis along the area, v axis across it)
SEAT = 0.06          # a placed detail's inner face sits this far inside our surface (skill lesson 15: 0.05 .. 0.15)
LONG = 0.4           # a detail longer than this along u shrinks with an area shorter than the exemplar's ...
SHRINK_MIN = 0.6     # ... to this share at most
DUP = 0.25           # no detail is placed within this of a detail already there
EDGES = [(k, tuple(1 if j == i else k[j] for j in range(3))) for k in fit.CORNERS for i in range(3) if k[i] == 0]


def lum(c):
    return 0.3 * c[0] + 0.59 * c[1] + 0.11 * c[2]


def shade(base, delta, step=18.0):
    """base lighter or darker by delta in luminance (at most step), its hue kept (exemplar.shade)."""
    d = max(-step, min(step, delta))
    lb = max(1.0, lum(base))
    return tuple(int(round(max(0, min(255, v * max(0.0, lb + d) / lb)))) for v in base)


class Shape:
    """A convex part (corners keyed (ix, iy, iz)) with its box and planes, and the part it stands for."""

    def __init__(self, c, part=None):
        self.c, self.part = c, part
        pts = list(c.values())
        self.box = [(min(q[i] for q in pts), max(q[i] for q in pts)) for i in range(3)]
        self._planes = None

    @property
    def planes(self):
        if self._planes is None:
            self._planes = study.planes_of(study.verts_of_corners(self.c))
        return self._planes


def shapes_of(parts):
    return [Shape(p.c, p) for p in parts]


def probe(shape, axis, pt):
    """(lo, hi) along `axis` where the line through pt (its other two coordinates) crosses the shape, or None."""
    for i in range(3):
        if i != axis and not (shape.box[i][0] - 1e-6 <= pt[i] <= shape.box[i][1] + 1e-6):
            return None
    t0, t1 = -1e9, 1e9
    for n, m in shape.planes:
        c = sum(n[i] * ((pt[i] if i != axis else 0.0) - m[i]) for i in range(3))
        na = n[axis]
        if abs(na) < 1e-12:
            if c > 1e-7:
                return None
            continue
        t = -c / na
        if na > 0:
            t1 = min(t1, t)
        else:
            t0 = max(t0, t)
    if t0 > t1 + 1e-9:
        return None
    return t0, t1


def surface(shapes, axis, side, pt):
    """The outermost face along axis toward `side` (+1 the largest coordinate, -1 the smallest) over the line through
    pt: (coordinate, shape), or None when the line misses every shape."""
    best = None
    for s in shapes:
        hit = probe(s, axis, pt)
        if hit is None:
            continue
        v = hit[1] if side > 0 else hit[0]
        if best is None or (v > best[0] if side > 0 else v < best[0]):
            best = (v, s)
    return best


def section_extent(c, axis, at, other):
    """(lo, hi) along `other` of the section of a part by the plane axis = at, or None."""
    vals = []
    for k0, k1 in EDGES:
        p, q = c[k0], c[k1]
        a, b = p[axis], q[axis]
        if (a - at) * (b - at) > 0:
            continue
        if abs(b - a) < 1e-12:
            if abs(a - at) < 1e-9:
                vals += [p[other], q[other]]
            continue
        t = (at - a) / (b - a)
        vals.append(p[other] + t * (q[other] - p[other]))
    return (min(vals), max(vals)) if vals else None


def column(shapes, axis, at, other):
    """The extent along `other` of the shapes' sections by the plane axis = at (the area's outline across), or None."""
    lo = hi = None
    for s in shapes:
        if not (s.box[axis][0] - 1e-9 <= at <= s.box[axis][1] + 1e-9):
            continue
        e = section_extent(s.c, axis, at, other)
        if e:
            lo = e[0] if lo is None else min(lo, e[0])
            hi = e[1] if hi is None else max(hi, e[1])
    return None if lo is None else (lo, hi)


def centre(c):
    pts = list(c.values())
    return [sum(q[i] for q in pts) / len(pts) for i in range(3)]


# ---------------------------------------------------------------- records (the dataset)

def records(cl, rule, idx, tones=None, groups=None, kinds=None):
    """The detail records of one area: cl the corner dicts of every part of the gun, rule study.detail_parts of them,
    idx the area's part indices. The area's frame is its parts that are not details; areas without one give none."""
    frame = [i for i in idx if not rule[i]]
    if not frame:
        return []
    fshapes = [Shape(cl[i]) for i in frame]
    box = fit.robust_box([cl[i] for i in frame])
    out = []
    for i in idx:
        r = rule[i]
        if not r:
            continue
        a, side, j = r["axis"], r["side"], r["host"]
        ub, vb = PLANE[a]
        cen = centre(cl[i])
        length = box[ub][1] - box[ub][0]
        u = (cen[ub] - box[ub][0]) / length if length > 1e-9 else 0.5
        col = column(fshapes, ub, cen[ub], vb)
        if col is None:
            continue
        v = (cen[vb] - col[0]) / (col[1] - col[0]) if col[1] - col[0] > 1e-9 else 0.5
        host = Shape(cl[j])
        hit = probe(host, a, cen) or host.box[a]
        lo, hi = hit
        off = []
        for k in fit.CORNERS:
            q = cl[i][k]
            w = side
            if side == 0:
                w = 1 if q[a] >= (lo + hi) / 2 else -1
            dn = q[a] - hi if w > 0 else lo - q[a]
            off.append([round(q[ub] - cen[ub], 5), round(q[vb] - cen[vb], 5), round(dn, 5), w])
        out.append({"axis": a, "side": side, "u": round(u, 5), "v": round(v, 5), "len": round(length, 5),
                    "off": off, "group": groups[i] if groups else "gunModel", "kind": kinds[i] if kinds else "",
                    "tone": list(tones[i]) if tones and tones[i] else None,
                    "hostTone": list(tones[j]) if tones and tones[j] else None})
    return out


# ---------------------------------------------------------------- placing a record on our area

class Placed:
    def __init__(self, corners=None, reason=None, host=None):
        self.corners, self.reason, self.host = corners, reason, host


def seated(c, others):
    """study.py contact's rule: a thin part (0.15 or less in some axis) needs one face with its 4 corners inside or on
    other parts, a thicker one has to overlap one."""
    pts = list(c.values())
    dims = [max(q[i] for q in pts) - min(q[i] for q in pts) for i in range(3)]
    if min(dims) > 0.15:
        mine = Shape(c)
        va = study.verts_of_corners(c)
        return any(study.touching(va, mine.planes, study.verts_of_corners(o.c), o.planes) for o in others
                   if all(mine.box[i][0] - 0.03 <= o.box[i][1] and o.box[i][0] - 0.03 <= mine.box[i][1]
                          for i in range(3)))
    for quad in fit.faces(fit.new_part("d", "gunModel", (0, 0, 0), c)):
        if all(any(study.inside_planes(o.planes, q, 0.03) for o in others) for q in quad):
            return True
    return False


def place(rec, area_parts, all_parts, existing=None):
    """One detail record on our area (its parts that are not details): a Placed with the corners, or the reason it
    was dropped (float: nothing of ours under it; hidden: covered by another part; clash: inside a part of another
    area; dup: a detail is already there)."""
    frame = shapes_of(area_parts)
    if not frame:
        return Placed(reason="float")
    area = getattr(area_parts[0], "area", None)
    a, side = rec["axis"], rec["side"]
    ub, vb = PLANE[a]
    box = fit.robust_box([p.c for p in area_parts])
    length = box[ub][1] - box[ub][0]
    cu = box[ub][0] + rec["u"] * length
    col = column(frame, ub, cu, vb)
    if col is None:
        return Placed(reason="float")
    cv = col[0] + rec["v"] * (col[1] - col[0])
    pt = [0.0, 0.0, 0.0]
    pt[ub], pt[vb] = cu, cv
    sides = (1, -1) if side == 0 else (side,)
    at_centre = {w: surface(frame, a, w, pt) for w in sides}
    if any(v is None for v in at_centre.values()):
        return Placed(reason="float")
    ext = max(o[0] for o in rec["off"]) - min(o[0] for o in rec["off"])
    r = 1.0
    if ext > LONG and rec.get("len"):
        r = max(SHRINK_MIN, min(1.0, length / rec["len"]))
    dns = sorted(o[2] for o in rec["off"])
    inner_max = dns[3] if side != 0 else None
    corners = {}
    for k, (db, dc, dn, w) in zip(fit.CORNERS, rec["off"]):
        q = [0.0, 0.0, 0.0]
        q[ub], q[vb] = cu + db * r, cv + dc
        hit = surface(frame, a, w, q)
        s = hit[0] if hit else at_centre[w][0]
        if side != 0 and dn <= inner_max:
            dn = min(dn, -SEAT)
        q[a] = s + w * dn
        corners[k] = q
    cen = centre(corners)
    for e in existing or []:
        ec = centre(e.c)
        if sum((ec[i] - cen[i]) ** 2 for i in range(3)) < DUP * DUP:
            return Placed(reason="dup")
    others = [s for s in shapes_of(all_parts)]
    if side != 0:
        outer = [corners[k] for k, o in zip(fit.CORNERS, rec["off"]) if o[2] > inner_max]
    else:
        outer = [corners[k] for k, o in zip(fit.CORNERS, rec["off"]) if o[3] > 0]
    oc = [sum(q[i] for q in outer) / len(outer) for i in range(3)] if outer else cen
    if any(study.inside_planes(o.planes, oc, -0.005) for o in others):
        return Placed(reason="hidden")
    foreign = [o for o in others if getattr(o.part, "area", None) not in (None, area)]
    if any(study.inside_planes(o.planes, q, -0.02) for o in foreign for q in corners.values()):
        return Placed(reason="clash")
    if not seated(corners, others):
        return Placed(reason="float")
    return Placed(corners=corners, host=at_centre[sides[0]][1])


# ---------------------------------------------------------------- the layer on a whole gun

def source_details(parts):
    """Our parts that are details already (the source's own rivets and small parts), by study.detail_parts."""
    rule = study.detail_parts([study.verts_of_corners(p.c) for p in parts])
    return [p for p, r in zip(parts, rule) if r], rule


def apply(parts, plan, data):
    """The detail layer: plan {area: {"gun", "ranges" (x ranges our fit kept, or absent for the whole area)}} from
    exemplar.apply. Returns (parts plus the placed details, report per area, details per Decimation gun)."""
    existing, rule = source_details(parts)
    frames = {}
    for p, r in zip(parts, rule):
        if not r and getattr(p, "area", None):
            frames.setdefault(p.area, []).append(p)
    out = list(parts)
    report, by_gun = {}, Counter()
    for area in sorted(plan):
        gun = plan[area]["gun"]
        ranges = plan[area].get("ranges")
        ex = data["guns"].get(gun, {}).get("areas", {}).get(area, {})
        recs = ex.get("detailRecs", [])
        frame = frames.get(area, [])
        dropped, placed = Counter(), 0
        for i, rec in enumerate(recs):
            if not frame:
                dropped["float"] += 1
                continue
            d = place(rec, frame, out, existing)
            if d.corners is None:
                dropped[d.reason] += 1
                continue
            cx = centre(d.corners)[0]
            if ranges and not any(x0 - 1e-9 <= cx <= x1 + 1e-9 for x0, x1 in ranges):
                continue
            host = d.host.part
            colour = tuple(host.colour)
            if rec.get("tone") and rec.get("hostTone"):
                colour = shade(host.colour, lum(rec["tone"]) - lum(rec["hostTone"]))
            q = fit.new_part("%sD%d" % (area, i), host.group, colour, d.corners, area)
            q.area = area
            q.detail_from = gun
            out.append(q)
            existing.append(q)
            placed += 1
        by_gun[gun] += placed
        report[area] = {"gun": gun, "placed": placed, "dropped": dict(dropped)}
    return out, report, dict((g, n) for g, n in by_gun.items() if n)


def unseated(parts):
    """Names of placed details (detail_from set) that do not sit on another part (study.py contact's rule)."""
    bad = []
    shapes = shapes_of(parts)
    for i, p in enumerate(parts):
        if getattr(p, "detail_from", None) and not seated(p.c, shapes[:i] + shapes[i + 1:]):
            bad.append(p.name)
    return bad

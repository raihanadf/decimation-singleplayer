#!/usr/bin/env python3
"""The edit step's reshapes (docs/gunconv.md): give a part another of Decimation's shape kinds, split it, or make it a
round bar. Every reshape starts from the part's bounding box, keeps its name, group, label and tone, and returns the
new part list (an octagon or a split gives several).

Kinds, as Decimation uses them (docs/gun_style_guide.md section 7):
- cuboid: the plain box;
- taper: the top face narrower (bevelled long top edges, 25 % of the width each side), Decimation's most common part;
- wedge: the top face collapsed onto its rear edge (a ramp, a stock toe, a sight blade);
- skew: the bottom face moved back by 30 % of the height (a raked grip, a magazine segment);
- octagon: Decimation's round bar along x (middle slab, a trapezoid above and one below);
- split: two halves along the longest side.
"""
import fit

KINDS = ("cuboid", "taper", "wedge", "skew", "octagon", "split")


def box_of(p):
    (x0, x1), (y0, y1), (z0, z1) = fit.bounds([p])
    return (x0, y0, z0), (x1, y1, z1)


def boxed(p, a, b, suffix=""):
    return fit.new_part(p.name + suffix, p.group, p.colour, {k: [b[i] if k[i] else a[i] for i in range(3)]
                                                             for k in fit.CORNERS}, getattr(p, "label", p.name))


def reshape(p, kind):
    a, b = box_of(p)
    if kind == "cuboid":
        return [boxed(p, a, b)]
    if kind == "taper":
        q = boxed(p, a, b)
        d = 0.25 * (b[2] - a[2])
        for k in fit.CORNERS:
            if k[1] == 0:
                q.c[k][2] += d if k[2] == 0 else -d
        return [q]
    if kind == "wedge":
        q = boxed(p, a, b)
        for k in fit.CORNERS:
            if k[1] == 0 and k[0] == 1:
                q.c[k][0] = a[0]
        return [q]
    if kind == "skew":
        q = boxed(p, a, b)
        d = 0.3 * (b[1] - a[1])
        for k in fit.CORNERS:
            if k[1] == 1:
                q.c[k][0] -= d
        return [q]
    if kind == "octagon":
        end = (a[1], b[1], a[2], b[2])
        return fit.octagon_parts(p.name, p.group, p.colour, getattr(p, "label", p.name), a[0], b[0], end, end)
    if kind == "split":
        ax = max(range(3), key=lambda i: b[i] - a[i])
        out = []
        for half, suffix in ((0, "A"), (1, "B")):
            q = fit.new_part(p.name + suffix, p.group, p.colour, {k: list(v) for k, v in p.c.items()},
                             getattr(p, "label", p.name))
            for k in fit.CORNERS:
                k0 = list(k)
                k0[ax] = 0
                k1 = list(k)
                k1[ax] = 1
                lo, hi = p.c[tuple(k0)], p.c[tuple(k1)]
                mid = [(lo[i] + hi[i]) / 2 for i in range(3)]
                if half == 0:
                    q.c[k] = list(mid) if k[ax] == 1 else list(lo)
                else:
                    q.c[k] = list(mid) if k[ax] == 0 else list(hi)
            out.append(q)
        return out
    raise ValueError("unknown kind %r: one of %s" % (kind, ", ".join(KINDS)))

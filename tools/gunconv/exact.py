#!/usr/bin/env python3
"""Exact mode of the gun converter (docs/superpowers/specs/2026-10-22-gun-converter-design.md, "Exact mode"): the
source mesh 1:1 in the .bmodel, only the format changed, the texture in Decimation's style (gunmodel.paint).

The mesh is prepared per piece: vertices welded, simplified to a face budget by quadric edge collapse (Garland and
Heckbert: each vertex keeps the sum of its faces' plane quadrics, the cheapest edge collapses first; border edges carry
extra planes so open edges stay, a collapse that flips a face is refused), coplanar neighbouring triangles merged into
quads, faces too small to see dropped. Every face then becomes one part: a plate THICK thick, the face on both sides,
its corners in the box's right handed order so each side's normal points away from it (plate).
"""
import heapq
import math

THICK = 0.02          # a face's plate thickness (model units): two sides that never fight for a pixel
MIN_SIDE = 0.06       # faces whose longest side is shorter are not seen in game (a rifle is about 600 px for 28 units)
COPLANAR = 3.0        # two triangles within this many degrees make one quad
BORDER_W = 100.0      # weight of the planes that hold a border edge in place
TRI_SLACK = 1.5       # simplify to this many triangles a face of budget first (merging makes quads of pairs)
DEFAULT_BUDGET = 350   # within the cost budget (384 parts at 1.5 x the akm's draw time)
BUDGET_RANGE = (300, 2000)

# ---------------------------------------------------------------- the cost gate (spec, "Cost gate")
# Fitted on the in game measurements (dev test gunpack perf=true, two launches on 23 Oktober 2026, docs/gunconv.md
# section 12): the draw time of a held gun, median of 200, glFinish around (GPU included), ModelBatch on, dev client on
# the M4: fourteen exact guns of 354 to 1,940 parts lie on 0.344 + 0.000903 ms a part (within 0.061 ms); Decimation's
# akm (194 parts) drew in 0.438 to 0.530 ms, median 0.4605, in the same runs.
AKM_PARTS = 194
AKM_MS = 0.4605
COST_A = 0.344        # ms a gun whatever its parts
COST_B = 0.000903     # ms a part
BUDGET = 1.5          # at most this times the akm's draw time (384 parts); above: warn, above twice it: refuse (1148)


def estimate(parts):
    """The cost of a gun of `parts` parts: draw time (ms) and its ratio to the akm's, and its quads (6 a part: the
    display lists Decimation compiles grow with them; graphics memory per gun could not be isolated with vmmap)."""
    ms = COST_A + COST_B * parts
    return {"drawMs": round(ms, 3), "ratio": round(ms / AKM_MS, 3), "quads": 6 * parts,
            "model": {"a": COST_A, "b": COST_B, "akm": AKM_MS, "budget": BUDGET}}


def verdict(ratio):
    if ratio <= BUDGET:
        return "ok"
    return "warn" if ratio <= 2 * BUDGET else "refuse"


# ---------------------------------------------------------------- vectors

def sub(a, b):
    return (a[0] - b[0], a[1] - b[1], a[2] - b[2])


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def dot(a, b):
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]


def norm(a):
    ln = math.sqrt(dot(a, a))
    return (a[0] / ln, a[1] / ln, a[2] / ln) if ln > 1e-15 else (0.0, 0.0, 0.0)


def newell(pts):
    """The normal of a polygon (Newell's method: right for a quad that is a little bent)."""
    n = [0.0, 0.0, 0.0]
    for i, p in enumerate(pts):
        q = pts[(i + 1) % len(pts)]
        n[0] += (p[1] - q[1]) * (p[2] + q[2])
        n[1] += (p[2] - q[2]) * (p[0] + q[0])
        n[2] += (p[0] - q[0]) * (p[1] + q[1])
    return norm(n)


# ---------------------------------------------------------------- weld

def weld(tris):
    """(verts, faces) of a triangle soup, vertices at one position (within a millionth of the diagonal) joined, faces
    that collapse dropped."""
    pts = [q for t in tris for q in t]
    if not pts:
        return [], []
    lo = [min(q[i] for q in pts) for i in range(3)]
    hi = [max(q[i] for q in pts) for i in range(3)]
    eps = max(1e-9, 1e-6 * math.sqrt(sum((hi[i] - lo[i]) ** 2 for i in range(3))))
    index, verts, faces = {}, [], []
    for t in tris:
        f = []
        for q in t:
            k = (round(q[0] / eps), round(q[1] / eps), round(q[2] / eps))
            if k not in index:
                index[k] = len(verts)
                verts.append([float(q[0]), float(q[1]), float(q[2])])
            f.append(index[k])
        if len(set(f)) == 3:
            faces.append(f)
    return verts, faces


# ---------------------------------------------------------------- quadric edge collapse

def plane_q(n, p, w):
    a, b, c = n
    d = -dot(n, p)
    return [w * v for v in (a * a, a * b, a * c, a * d, b * b, b * c, b * d, c * c, c * d, d * d)]


def q_add(a, b):
    return [x + y for x, y in zip(a, b)]


def q_err(q, v):
    x, y, z = v
    return (q[0] * x * x + 2 * q[1] * x * y + 2 * q[2] * x * z + 2 * q[3] * x + q[4] * y * y + 2 * q[5] * y * z
            + 2 * q[6] * y + q[7] * z * z + 2 * q[8] * z + q[9])


def q_best(q, a, b):
    """The point minimising the quadric (a 3 x 3 solve), else the best of a, b and their middle."""
    m = [[q[0], q[1], q[2]], [q[1], q[4], q[5]], [q[2], q[5], q[7]]]
    r = [-q[3], -q[6], -q[8]]
    det = (m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1]) - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0])
           + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]))
    cands = [tuple(a), tuple(b), tuple((a[i] + b[i]) / 2 for i in range(3))]
    scale = max(abs(v) for row in m for v in row) or 1.0
    if abs(det) > 1e-9 * scale ** 3:
        def col(i):
            mm = [row[:] for row in m]
            for k in range(3):
                mm[k][i] = r[k]
            return (mm[0][0] * (mm[1][1] * mm[2][2] - mm[1][2] * mm[2][1]) - mm[0][1] * (mm[1][0] * mm[2][2] - mm[1][2]
                    * mm[2][0]) + mm[0][2] * (mm[1][0] * mm[2][1] - mm[1][1] * mm[2][0])) / det
        p = (col(0), col(1), col(2))
        # stay near the edge: a far optimum of a nearly flat quadric is no better than its ends
        span = math.sqrt(sum((a[i] - b[i]) ** 2 for i in range(3))) + 1e-9
        mid = cands[2]
        if math.sqrt(sum((p[i] - mid[i]) ** 2 for i in range(3))) <= 2 * span:
            cands.append(p)
    best = min(cands, key=lambda v: q_err(q, v))
    return best, max(0.0, q_err(q, best))


def simplify(verts, faces, target, keep_index=False):
    """Collapse edges, cheapest first, until at most `target` faces are left (or no collapse is allowed). Returns
    (verts, faces) compacted (and, keep_index, the old index of each vertex kept)."""
    if len(faces) <= target:
        return (verts, faces, list(range(len(verts)))) if keep_index else (verts, faces)
    pos = [list(v) for v in verts]
    fs = [list(f) for f in faces]
    alive = [True] * len(fs)
    vf = [set() for _ in pos]
    for i, f in enumerate(fs):
        for v in f:
            vf[v].add(i)
    Q = [[0.0] * 10 for _ in pos]
    edges = {}
    for i, f in enumerate(fs):
        a, b, c = (pos[v] for v in f)
        n = cross(sub(b, a), sub(c, a))
        area = 0.5 * math.sqrt(dot(n, n))
        if area <= 0:
            continue
        n = norm(n)
        q = plane_q(n, a, area)
        for v in f:
            Q[v] = q_add(Q[v], q)
        for k in range(3):
            e = (min(f[k], f[(k + 1) % 3]), max(f[k], f[(k + 1) % 3]))
            edges.setdefault(e, []).append(i)
    for (u, v), fl in edges.items():                     # border edges: a plane through them, square to their face
        if len(fl) == 1:
            f = fs[fl[0]]
            a, b, c = (pos[x] for x in f)
            fn = norm(cross(sub(b, a), sub(c, a)))
            ev = sub(pos[v], pos[u])
            bn = norm(cross(ev, fn))
            if dot(bn, bn) > 0:
                q = plane_q(bn, pos[u], BORDER_W * dot(ev, ev))
                Q[u] = q_add(Q[u], q)
                Q[v] = q_add(Q[v], q)
    stamp = [0] * len(pos)
    heap = []

    def push(u, v):
        if u == v:
            return
        u, v = min(u, v), max(u, v)
        p, err = q_best(q_add(Q[u], Q[v]), pos[u], pos[v])
        heapq.heappush(heap, (err, u, v, stamp[u], stamp[v], p))
    for (u, v) in edges:
        push(u, v)
    live = sum(alive)

    def nbrs(u):
        return set(x for f in vf[u] for x in fs[f]) - {u}

    while live > target and heap:
        err, u, v, su, sv, p = heapq.heappop(heap)
        if stamp[u] != su or stamp[v] != sv or not vf[u] or not vf[v]:
            continue
        shared = vf[u] & vf[v]
        if not shared:
            continue
        common = nbrs(u) & nbrs(v)
        if len(common) > len(shared):                    # the link condition: no fins, no pinches
            continue
        ok = True
        for f in (vf[u] | vf[v]) - shared:               # no face may flip or fold
            tri = fs[f]
            old = [pos[x] for x in tri]
            new = [p if x in (u, v) else pos[x] for x in tri]
            n0 = cross(sub(old[1], old[0]), sub(old[2], old[0]))
            n1 = cross(sub(new[1], new[0]), sub(new[2], new[0]))
            if dot(n1, n1) <= 1e-18 or dot(n0, n1) <= 0.2 * math.sqrt(dot(n0, n0) * dot(n1, n1)):
                ok = False
                break
        if not ok:
            continue
        pos[u] = list(p)
        Q[u] = q_add(Q[u], Q[v])
        for f in list(vf[v]):
            if f in shared:
                alive[f] = False
                live -= 1
                for x in fs[f]:
                    vf[x].discard(f)
            else:
                fs[f] = [u if x == v else x for x in fs[f]]
                vf[u].add(f)
        vf[v] = set()
        stamp[u] += 1
        stamp[v] += 1
        for w in nbrs(u):
            stamp[w] += 0                                # stamps of the neighbours stay: only u's moved
            push(u, w)
    keep = [f for i, f in enumerate(fs) if alive[i]]
    used = sorted(set(x for f in keep for x in f))
    remap = {o: n for n, o in enumerate(used)}
    out = [pos[o] for o in used], [[remap[x] for x in f] for f in keep]
    return out + (used,) if keep_index else out


# ---------------------------------------------------------------- quads

def merge_quads(verts, faces, max_deg=COPLANAR):
    """Polygons (vertex index lists of 3 or 4): neighbouring triangles wound the same way, within max_deg of one plane
    and making a convex quad, joined; the flattest pairs first."""
    normals = []
    for f in faces:
        a, b, c = (verts[x] for x in f)
        normals.append(norm(cross(sub(b, a), sub(c, a))))
    by_edge = {}
    for i, f in enumerate(faces):
        for k in range(3):
            by_edge.setdefault((f[k], f[(k + 1) % 3]), []).append(i)
    cos_max = math.cos(math.radians(max_deg))
    pairs = []
    for (a, b), fl in by_edge.items():
        for i in fl:
            for j in by_edge.get((b, a), []):            # the same edge the other way round: wound alike
                if i < j:
                    c = dot(normals[i], normals[j])
                    if c >= cos_max:
                        pairs.append((-c, i, j, a, b))
    pairs.sort()
    used = [False] * len(faces)
    out = []
    for _, i, j, a, b in pairs:
        if used[i] or used[j]:
            continue
        ci = [x for x in faces[i] if x not in (a, b)][0]
        cj = [x for x in faces[j] if x not in (a, b)][0]
        quad = [a, cj, b, ci]
        n = normals[i]
        pts = [verts[x] for x in quad]
        convex = all(dot(cross(sub(pts[(k + 1) % 4], pts[k]), sub(pts[(k + 2) % 4], pts[(k + 1) % 4])), n) > 1e-12
                     for k in range(4))
        if not convex:
            continue
        used[i] = used[j] = True
        out.append(quad)
    out += [list(f) for i, f in enumerate(faces) if not used[i]]
    return out


# ---------------------------------------------------------------- preparation

def area_of(tris):
    return sum(0.5 * math.sqrt(dot(n, n)) for n in (cross(sub(t[1], t[0]), sub(t[2], t[0])) for t in tris))


def prepare(tris, budget=DEFAULT_BUDGET, min_side=MIN_SIDE, mesh=None):
    """One piece's triangles as polygons (lists of 3 or 4 points) within `budget` faces: welded, simplified, merged into
    quads, faces too small to see dropped. mesh: a (verts, faces) this piece was already simplified to (a smaller budget
    goes on from it instead of from the start)."""
    return prepare_mesh(tris, budget, min_side, mesh)[0]


def prepare_mesh(tris, budget=DEFAULT_BUDGET, min_side=MIN_SIDE, mesh=None):
    """prepare() and the simplified (verts, faces) it ended with."""
    v2, f2 = mesh if mesh is not None else weld(tris)
    if not f2:
        return [], (v2, f2)
    polys = None
    target = max(2, int(budget * TRI_SLACK))
    for _ in range(6):
        v2, f2 = simplify(v2, f2, target)                 # each pass goes on from the last one
        polys = merge_quads(v2, f2)
        if len(polys) <= budget or target <= 2:
            break
        target = max(2, int(target * budget / float(len(polys))) - 1)
    out = []
    for p in polys:
        pts = [tuple(v2[x]) for x in p]
        longest = max(math.dist(pts[k], pts[(k + 1) % len(pts)]) for k in range(len(pts)))
        n = cross(sub(pts[1], pts[0]), sub(pts[2], pts[0]))
        if longest < min_side or dot(n, n) <= 1e-18:
            continue
        out.append(pts)
    return out, (v2, f2)


def prepare_all(pieces, budget=DEFAULT_BUDGET, min_side=MIN_SIDE):
    """Every piece's triangles (a list of triangle lists) as polygons, [polys per piece], within `budget` faces in all:
    one quadric queue over all the pieces (never joined: a collapse stays inside its piece), so the big flat surfaces
    lose their triangles first and a thin bar keeps its shape; merged into quads, faces too small to see dropped."""
    verts, faces, tag = [], [], []
    for i, tris in enumerate(pieces):
        v, f = weld(tris)
        o = len(verts)
        verts += v
        faces += [[a + o, b + o, c + o] for a, b, c in f]
        tag += [i] * len(v)
    out = [[] for _ in pieces]
    if not faces:
        return out
    target = max(2, int(budget * TRI_SLACK))
    polys = None
    for _ in range(6):
        verts, faces, used = simplify(verts, faces, target, keep_index=True)
        tag = [tag[u] for u in used]
        polys = merge_quads(verts, faces)
        if len(polys) <= budget or target <= 2:
            break
        target = max(2, int(target * budget / float(len(polys))) - 1)
    for p in polys:
        pts = [tuple(verts[x]) for x in p]
        longest = max(math.dist(pts[k], pts[(k + 1) % len(pts)]) for k in range(len(pts)))
        n = cross(sub(pts[1], pts[0]), sub(pts[2], pts[0]))
        if longest < min_side or dot(n, n) <= 1e-18:
            continue
        out[tag[p[0]]].append(pts)
    return out


def share(areas, budget, least=2):
    """A face budget split over pieces by surface area (at least `least` a piece)."""
    total = sum(areas) or 1.0
    return [max(least, int(round(budget * a / total))) for a in areas]


def plate(points, thick=THICK):
    """A face as a part's corners: (ix, iy) around the face in its order, iz = 1 on the side its normal points to, iz = 0
    thick behind; the box is right handed, so either side's normal points away from it. A triangle repeats its last
    corner."""
    pts = [tuple(p) for p in points]
    if len(pts) == 3:
        pts.append(pts[2])
    n = newell(pts)
    h = thick / 2.0
    out = {}
    for (ix, iy), p in zip(((0, 0), (1, 0), (1, 1), (0, 1)), pts):
        out[(ix, iy, 1)] = [p[i] + n[i] * h for i in range(3)]
        out[(ix, iy, 0)] = [p[i] - n[i] * h for i in range(3)]
    return out

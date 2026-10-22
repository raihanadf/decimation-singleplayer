#!/usr/bin/env python3
"""Converted guns as real meshes, 1:1 (converter milestone 4, docs/superpowers/specs/2026-10-23-gun-mesh-design.md;
user, 23 Oktober 2026: "use convert 1:1 instead generating decimation style guns", texture "pixelated like decimation",
a cap of 30000 triangles).

The game draws each mesh group as one display list in place of a placeholder part (fixes/gunpack/MeshGun,
fixes/ModelBatch). This module writes what it needs:
- mesh.dmesh (write_dmesh): big endian "DMESH1", the group count; per group its name (short length, UTF-8), pivot,
  triangle count and per vertex x, y, z (model units, relative to the pivot), normal, u, v (0..1 of the gun texture);
- the skeleton .bmodel (skeleton): the header and one marker part per group, a 1 x 1 x 1 cube at (id, group, 7777),
  its rotation point the group's pivot;
- the texture (bake): charts of our own, one texel a quarter of a model unit on every face (a face is drawn from the
  axis it faces most), each texel coloured from the source surface under it and toned like Decimation's guns (dark
  flat tones, a shade per part, lighter on top, faint texel noise; gunmodel.paint's recipe); the icon (icon).
Normals are smoothed across faces within 40 degrees (sharp edges stay sharp); over the cap the mesh is simplified by
exact.simplify (the quadric queue) and each texel takes the colour of the nearest source face.
"""
import math
import random
import struct

MAGIC = b"DMESH1"
CAP = 30000            # triangles a gun (user, 23 Oktober 2026); MeshGun.read refuses more in a group
CREASE = 40.0          # degrees: faces further apart than this keep their own normals
MARK_Z = 7777          # the marker cube's z in units of its own size (MeshGun.MARK_Z)
TEXEL = 0.25           # model units a texel (spec: "about a quarter of a model unit")
DEPTH = 0.5            # model units: faces of one direction further apart than this along it get texels of their own
GROUPS = ("gunModel", "ammoModel", "slideModel", "defaultScopeModel")
MAX_SIDE = 2048
PLANE = ((2, 1), (0, 2), (0, 1))      # the two coordinates a face looking along x, y or z is drawn with


def mesh_id(name):
    """fixes/gunpack/MeshGun.id: FNV-1a 32 of the name's UTF-8 bytes, the halves xored to 16 bits."""
    h = 0x811c9dc5
    for b in name.encode("utf-8"):
        h = ((h ^ b) * 0x01000193) & 0xffffffff
    return ((h >> 16) ^ (h & 0xffff)) & 0xffff


# ---------------------------------------------------------------- cost

# Draw time against Decimation's akm in the same runs (dev test gunpack perf=true, 23 Oktober 2026, dev client: akmm
# 5,240 triangles 0.411 ms against 0.475, ak47m 30,000 triangles 0.915 ms against 0.611; the production client with
# OptiFine 0.77 x and 1.33 x): a line through the two, in akm draws.
COST_BASE, COST_PER_K, BUDGET = 0.7365, 0.025543, 1.5


def estimate(triangles):
    """{"ratio": the draw time in akm draws, "budget", "verdict": ok | heavy}."""
    ratio = round(COST_BASE + COST_PER_K * triangles / 1000.0, 3)
    return {"ratio": ratio, "budget": BUDGET, "verdict": "ok" if ratio <= BUDGET + 0.01 else "heavy"}


# ---------------------------------------------------------------- the .dmesh file

def write_dmesh(groups):
    """groups: [{"name", "pivot": (x, y, z), "verts": 24 floats a triangle}] -> bytes."""
    if not 1 <= len(groups) <= 16:
        raise ValueError("mesh groups %d (1 to 16)" % len(groups))
    total = 0
    out = bytearray(MAGIC) + struct.pack(">i", len(groups))
    for g in groups:
        v = g["verts"]
        if len(v) % 24:
            raise ValueError("a group's vertices come as triangles of 3 x 8 floats")
        total += len(v) // 24
        name = g["name"].encode("utf-8")
        out += struct.pack(">H", len(name)) + name + struct.pack(">3f", *g["pivot"]) + struct.pack(">i", len(v) // 24)
        out += struct.pack(">%df" % len(v), *v)
    if total > CAP:
        raise ValueError("%d triangles: the cap is %d" % (total, CAP))
    return bytes(out)


def read_dmesh(data):
    if data[:len(MAGIC)] != MAGIC:
        raise ValueError("not a mesh file (no %s)" % MAGIC.decode())
    o = len(MAGIC)
    (n,) = struct.unpack_from(">i", data, o)
    o += 4
    groups = []
    for _ in range(n):
        (ln,) = struct.unpack_from(">H", data, o)
        name = data[o + 2:o + 2 + ln].decode("utf-8")
        o += 2 + ln
        pivot = struct.unpack_from(">3f", data, o)
        (t,) = struct.unpack_from(">i", data, o + 12)
        o += 16
        verts = struct.unpack_from(">%df" % (t * 24), data, o)
        o += t * 96
        groups.append({"name": name, "pivot": pivot, "verts": list(verts)})
    return groups


# ---------------------------------------------------------------- geometry

def sub(a, b):
    return (a[0] - b[0], a[1] - b[1], a[2] - b[2])


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def dot(a, b):
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]


def norm(a):
    d = math.sqrt(dot(a, a))
    return (a[0] / d, a[1] / d, a[2] / d) if d > 1e-12 else (0.0, 0.0, 0.0)


def face_normal(t):
    return norm(cross(sub(t[1], t[0]), sub(t[2], t[0])))


def corner_angle(t, k):
    a, b = norm(sub(t[(k + 1) % 3], t[k])), norm(sub(t[(k + 2) % 3], t[k]))
    return math.acos(max(-1.0, min(1.0, dot(a, b))))


def smooth_normals(tris, crease=CREASE):
    """Per corner normals: the face normals around a position (weighted by their corner angles) within `crease`
    degrees of the corner's own face, so round parts shade round and edges sharper than the crease stay sharp."""
    cos_c = math.cos(math.radians(crease)) - 1e-9
    fn = [face_normal(t) for t in tris]
    around = {}
    for i, t in enumerate(tris):
        for k in range(3):
            q = t[k]
            around.setdefault((round(q[0], 5), round(q[1], 5), round(q[2], 5)), []).append((i, corner_angle(t, k)))
    out = []
    for i, t in enumerate(tris):
        ns = []
        for q in t:
            s = (0.0, 0.0, 0.0)
            for j, a in around[(round(q[0], 5), round(q[1], 5), round(q[2], 5))]:
                if dot(fn[j], fn[i]) >= cos_c:
                    s = (s[0] + a * fn[j][0], s[1] + a * fn[j][1], s[2] + a * fn[j][2])
            n = norm(s)
            ns.append(n if n != (0.0, 0.0, 0.0) else fn[i])
        out.append(ns)
    return out


def signed_volume(tris):
    """Six times the volume a piece's faces enclose about its centre: negative when they are wound inward."""
    pts = [q for t in tris for q in t]
    c = [sum(q[i] for q in pts) / len(pts) for i in range(3)]
    return sum(dot(sub(t[0], c), cross(sub(t[1], c), sub(t[2], c))) for t in tris)


def orient(tris):
    """(tris, flipped): a piece wound inward (a mirrored source, a negative node scale) turned outward, (0, 2, 1)."""
    if tris and signed_volume(tris) < 0:
        return [(t[0], t[2], t[1]) for t in tris], True
    return tris, False


def cap_pieces(pieces, limit=CAP):
    """(pieces, exact): under the cap as they are; else simplified together (exact.simplify, one quadric queue) to at
    most `limit` triangles, each piece kept apart."""
    if sum(len(p) for p in pieces) <= limit:
        return pieces, True
    import exact
    verts, faces, owner = [], [], []
    for pi, tris in enumerate(pieces):
        v, f = exact.weld(tris)
        base = len(verts)
        verts += v
        faces += [[x + base for x in tri] for tri in f]
        owner += [pi] * len(v)
    v2, f2, old = exact.simplify(verts, faces, limit, keep_index=True)
    if len(f2) > limit:
        raise ValueError("simplified to %d triangles, still over the cap of %d" % (len(f2), limit))
    out = [[] for _ in pieces]
    for f in f2:
        out[owner[old[f[0]]]].append(tuple(tuple(float(c) for c in v2[x]) for x in f))
    return out, False


# ---------------------------------------------------------------- the texture

class Chart:
    def __init__(self, piece, axis, sign, tris):
        self.piece, self.axis, self.sign, self.tris = piece, axis, sign, tris
        self.x0 = self.y0 = self.w = self.h = self.ax = self.ay = 0


def projected(t, axis, texel):
    pa, pb = PLANE[axis]
    return [(q[pa] / texel, q[pb] / texel) for q in t]


def centres(p2):
    """[(i, j, (w0, w1, w2))]: the texel centres (i + 0.5, j + 0.5) inside a projected triangle, with their weights."""
    (x0, y0), (x1, y1), (x2, y2) = p2
    den = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2)
    if abs(den) < 1e-12:
        return []
    out = []
    for j in range(int(math.floor(min(y0, y1, y2))), int(math.floor(max(y0, y1, y2))) + 1):
        cy = j + 0.5
        for i in range(int(math.floor(min(x0, x1, x2))), int(math.floor(max(x0, x1, x2))) + 1):
            cx = i + 0.5
            w0 = ((y1 - y2) * (cx - x2) + (x2 - x1) * (cy - y2)) / den
            w1 = ((y2 - y0) * (cx - x2) + (x0 - x2) * (cy - y2)) / den
            w2 = 1.0 - w0 - w1
            if w0 >= -1e-9 and w1 >= -1e-9 and w2 >= -1e-9:
                out.append((i, j, (w0, w1, w2)))
    return out


def make_charts(pieces, texel):
    """Charts per piece and facing axis; faces of one direction that lie over each other further apart than DEPTH
    (the inside and outside of a channel) go into charts of their own, outermost first."""
    by = {}
    for pi, tris in enumerate(pieces):
        for ti, t in enumerate(tris):
            n = face_normal(t)
            a = max(range(3), key=lambda k: abs(n[k]))
            by.setdefault((pi, a, 1 if n[a] >= 0 else -1), []).append(ti)
    charts = []
    for (pi, a, sign), ids in sorted(by.items()):
        tris = pieces[pi]
        ids.sort(key=lambda ti: -sign * sum(q[a] for q in tris[ti]))
        layers = []
        for ti in ids:
            t = tris[ti]
            p2 = projected(t, a, texel)
            cells = {(i, j): sum(w[k] * t[k][a] for k in range(3)) for i, j, w in centres(p2)}
            if not cells:                                # smaller than a texel: the cell under its middle
                cx, cy = sum(p[0] for p in p2) / 3, sum(p[1] for p in p2) / 3
                cells = {(int(math.floor(cx)), int(math.floor(cy))): sum(q[a] for q in t) / 3}
            for cl, members in layers:
                if all(abs(cl.get(c, d) - d) <= DEPTH for c, d in cells.items()):
                    cl.update(cells)
                    members.append(ti)
                    break
            else:
                layers.append((dict(cells), [ti]))
        for _, members in layers:
            c = Chart(pi, a, sign, members)
            pts = [p for ti in members for p in projected(tris[ti], a, texel)]
            c.x0 = int(math.floor(min(p[0] for p in pts))) - 1
            c.y0 = int(math.floor(min(p[1] for p in pts))) - 1
            c.w = int(math.floor(max(p[0] for p in pts))) + 2 - c.x0
            c.h = int(math.floor(max(p[1] for p in pts))) + 2 - c.y0
            charts.append(c)
    return charts


def pack(charts):
    """Shelf packing, tallest first, into the smallest power of two square or 2:1 sheet; returns (W, H)."""
    area = sum(c.w * c.h for c in charts)
    W = 16
    while W * W < area or W < max(c.w for c in charts):
        W *= 2
    while True:
        x = y = row = 0
        for c in sorted(charts, key=lambda c: (-c.h, -c.w)):
            if x + c.w > W:
                x, y, row = 0, y + row, 0
            c.ax, c.ay = x, y
            x += c.w
            row = max(row, c.h)
        H = 16
        while H < y + row:
            H *= 2
        if H <= W or W >= MAX_SIDE:
            break
        W *= 2
    if max(W, H) > MAX_SIDE:
        raise ValueError("the texture would be %d x %d (over %d): the mesh is too large" % (W, H, MAX_SIDE))
    return W, H


class Built:
    """pieces: [{"tris", "uvs": per triangle 3 x (u, v)}] in the order given; image: the RGBA sheet."""

    def __init__(self, pieces, image, charts, texel):
        self.pieces, self.image, self.charts, self.texel = pieces, image, charts, texel


def clamp(v):
    return max(0, min(255, int(round(v))))


def bake(pieces, colour_fns, tone=None, seed=0, shade=True, texel=TEXEL):
    """The pixel texture of pieces (triangle lists in model units): colour_fns[piece](triangle, weights, point) gives
    the source colour under each texel, tone (convert.tone) turns it into Decimation's tones; shade adds
    gunmodel.paint's recipe (a shade per part +-6 %, +5 at the top of the gun to -5 at the bottom, faces looking up +2
    and down -2, sides +-1.5 a chart, texel noise -4 .. 4)."""
    from PIL import Image
    rnd = random.Random(seed)
    charts = make_charts(pieces, texel)
    W, H = pack(charts)
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    px = img.load()
    ys = [q[1] for tris in pieces for t in tris for q in t]
    top, bottom = min(ys), max(ys)
    factor = [1 + rnd.uniform(-0.06, 0.06) for _ in pieces]
    uvs = [[None] * len(tris) for tris in pieces]
    for c in charts:
        tris = pieces[c.piece]
        if c.axis == 1:
            shift = -2.0 if c.sign > 0 else 2.0          # y runs down in Decimation's model space: -y looks up
        else:
            shift = rnd.uniform(-1.5, 1.5)
        filled = []
        for ti in c.tris:
            t = tris[ti]
            p2 = projected(t, c.axis, texel)
            uvs[c.piece][ti] = [((c.ax + p[0] - c.x0) / W, (c.ay + p[1] - c.y0) / H) for p in p2]
            cells = centres(p2)
            if not cells:                                # thinner than a texel: the texel under its middle
                cx, cy = sum(p[0] for p in p2) / 3, sum(p[1] for p in p2) / 3
                if px[c.ax + int(math.floor(cx)) - c.x0, c.ay + int(math.floor(cy)) - c.y0][3]:
                    continue
                cells = [(int(math.floor(cx)), int(math.floor(cy)), (1 / 3, 1 / 3, 1 / 3))]
            for i, j, w in cells:
                p3 = tuple(sum(w[k] * t[k][a] for k in range(3)) for a in range(3))
                col = colour_fns[c.piece](ti, w, p3)
                if tone is not None:
                    col = tone(col)
                if shade:
                    g = 5 - 10 * (p3[1] - top) / max(1e-6, bottom - top)
                    n = rnd.randint(-4, 4)
                    col = tuple(clamp(v * factor[c.piece] + g + shift + n) for v in col[:3])
                else:
                    col = tuple(clamp(v) for v in col[:3])
                x, y = c.ax + i - c.x0, c.ay + j - c.y0
                px[x, y] = col + (255,)
                filled.append(col)
        dilate(px, c, filled)
    return Built([{"tris": tris, "uvs": uvs[pi]} for pi, tris in enumerate(pieces)], img, charts, texel)


def dilate(px, c, filled):
    """Texels of a chart that no face centre covers (its border, slivers) take a covered neighbour's colour, so
    nearest filtering never shows a hole; any left take the chart's mean."""
    for _ in range(3):
        todo = []
        for y in range(c.ay, c.ay + c.h):
            for x in range(c.ax, c.ax + c.w):
                if px[x, y][3] == 0:
                    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                        xx, yy = x + dx, y + dy
                        if c.ax <= xx < c.ax + c.w and c.ay <= yy < c.ay + c.h and px[xx, yy][3]:
                            todo.append((x, y, px[xx, yy]))
                            break
        if not todo:
            break
        for x, y, col in todo:
            px[x, y] = col
    mean = tuple(clamp(sum(f[i] for f in filled) / len(filled)) for i in range(3)) + (255,) if filled else (40, 40, 40, 255)
    for y in range(c.ay, c.ay + c.h):
        for x in range(c.ax, c.ax + c.w):
            if px[x, y][3] == 0:
                px[x, y] = mean


# ---------------------------------------------------------------- the skeleton .bmodel and the icon

def fnum(v):
    v = round(v, 4)
    return ("%g" % (0 if v == 0 else v)) + "F"


def skeleton(name, header, groups, size):
    """The .bmodel of a mesh gun: Decimation's header lines, the texture size, and per group (in mesh.dmesh order) a
    placeholder part <group>0, a plain 1 x 1 x 1 cube at (id, group, 7777) turned about the group's pivot."""
    lines = []
    for k in ("flamePos", "ejectPos", "rhPos", "rhRot", "lhPos", "lhRot"):
        if header.get(k) is not None:
            lines.append("  %s: %s;" % (k, ", ".join("%g" % c for c in header[k])))
    lines += ["  textureWidth = %d;" % size[0], "  textureHeight = %d;" % size[1]]
    mid = mesh_id(name)
    zero = ", ".join(["{0F, 0F, 0F}"] * 8)
    for k, g in enumerate(groups):
        n, piv = g["name"] + "0", g["pivot"]
        lines += ["",
                  "  // %s: mesh.dmesh group %d, drawn by fixes/gunpack/MeshGun" % (g["name"], k),
                  "  %s = new BeardieModelRenderer(this, 0, 0);" % n,
                  "  %s.addShape(%dF, %dF, %dF, new float[][]{%s}, 1, 1, 1);" % (n, mid, k, MARK_Z, zero),
                  "  %s.setRotationPoint(%s, %s, %s);" % (n, fnum(piv[0]), fnum(piv[1]), fnum(piv[2])),
                  "  %s.setRotation(0F, 0F, 0F);" % n]
    return "\n".join(lines) + "\n"


LIGHT = norm((0.25, -0.6, -0.75))     # from the front, above, the side the icon shows (-z)


def icon(pieces, built, size=32, width=30, ss=4):
    """Decimation's icon look (gunmodel.icon): the -z side, muzzle right, `width` px wide, the texture's tones lit a
    little, hard alpha, a 1 px dark outline."""
    from PIL import Image
    tex = built.image
    TW, TH = tex.size
    tp = tex.load()
    pts = [q for tris in pieces for t in tris for q in t]
    x0, x1 = min(q[0] for q in pts), max(q[0] for q in pts)
    y0, y1 = min(q[1] for q in pts), max(q[1] for q in pts)
    k = width * ss / max(1e-6, x1 - x0)
    w, h = width * ss, max(1, int(math.ceil((y1 - y0) * k)))
    zbuf = [[1e30] * w for _ in range(h)]
    col = [[None] * w for _ in range(h)]
    for pi, tris in enumerate(pieces):
        for ti, t in enumerate(tris):
            n = face_normal(t)
            uv = built.pieces[pi]["uvs"][ti]
            u, v = sum(q[0] for q in uv) / 3, sum(q[1] for q in uv) / 3
            c = tp[min(TW - 1, int(u * TW)), min(TH - 1, int(v * TH))]
            lit = 0.8 + 0.5 * abs(dot(n, LIGHT))
            c = (c[0] * lit, c[1] * lit, c[2] * lit)
            p2 = [((q[0] - x0) * k - 0.5, (q[1] - y0) * k - 0.5) for q in t]
            for i, j, wt in centres(p2):
                if 0 <= i < w and 0 <= j < h:
                    z = sum(wt[m] * t[m][2] for m in range(3))
                    if z < zbuf[j][i]:
                        zbuf[j][i], col[j][i] = z, c
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    sw, sh = width, max(1, int(math.ceil(h / ss)))
    ox, oy = (size - sw) // 2, (size - sh) // 2
    for yy in range(sh):
        for xx in range(sw):
            cs = [col[j][i] for j in range(yy * ss, min(h, yy * ss + ss)) for i in range(xx * ss, xx * ss + ss)
                  if col[j][i] is not None]
            if len(cs) * 255 >= 140 * ss * ss and 0 <= ox + xx < size and 0 <= oy + yy < size:
                out.putpixel((ox + xx, oy + yy), tuple(clamp(1.3 * sum(c[m] for c in cs) / len(cs)) for m in range(3)) + (255,))
    edge = out.copy()
    for y in range(size):
        for x in range(size):
            if out.getpixel((x, y))[3] == 0 and any(0 <= x + dx < size and 0 <= y + dy < size
                                                     and out.getpixel((x + dx, y + dy))[3]
                                                     for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                edge.putpixel((x, y), (10, 10, 12, 255))
    return edge


# ---------------------------------------------------------------- one gun

def nearest_colours(pieces, colour_fns, simplified):
    """For a simplified mesh: per piece a colour function of its new triangles by the nearest source face (its middle,
    facing within 90 degrees of the new face), the source faces sampled once at their middles in a grid of TEXEL
    cells."""
    out = []
    for tris, fn, new in zip(pieces, colour_fns, simplified):
        grid = {}
        for ti, t in enumerate(tris):
            m = tuple(sum(q[a] for q in t) / 3 for a in range(3))
            grid.setdefault(tuple(int(math.floor(m[a] / TEXEL)) for a in range(3)), []).append(
                (m, face_normal(t), fn(ti, (1 / 3, 1 / 3, 1 / 3), m)))
        out.append(nearest_in(grid, new))
    return out


def nearest_in(grid, new):
    def look(t, w, p):
        n = face_normal(new[t])
        cell = tuple(int(math.floor(p[a] / TEXEL)) for a in range(3))
        for r in range(1, 9):
            best, bd = None, 1e30
            for dx in range(-r, r + 1):
                for dy in range(-r, r + 1):
                    for dz in range(-r, r + 1):
                        for m, fm, c in grid.get((cell[0] + dx, cell[1] + dy, cell[2] + dz), ()):
                            d = sum((m[a] - p[a]) ** 2 for a in range(3))
                            if d < bd and dot(fm, n) > 0:
                                best, bd = c, d
            if best is not None:
                return best
        return (40, 40, 40)
    return look


class MeshResult:
    """A mesh gun before it is written: pieces [{"tris", "normals", "uvs"}] (capped, wound outward, in the order of
    the source pieces), the texture, stats. Groups are given when written, so the page can move pieces between them
    without baking again."""

    def __init__(self, pieces, image, stats):
        self.pieces, self.image, self.stats = pieces, image, stats

    def dmesh_groups(self, groups):
        """mesh.dmesh groups for a group per piece, in GROUPS order: the pivot is the group's lowest corner (as
        gunmodel.part_block turns a part about its own), positions relative to it."""
        out = []
        for gname in GROUPS:
            idx = [pi for pi, g in enumerate(groups) if g == gname and self.pieces[pi]["tris"]]
            if not idx:
                continue
            pts = [q for pi in idx for t in self.pieces[pi]["tris"] for q in t]
            piv = tuple(round(min(q[a] for q in pts), 4) for a in range(3))
            verts = []
            for pi in idx:
                p = self.pieces[pi]
                for t, ns, uv in zip(p["tris"], p["normals"], p["uvs"]):
                    for q, n, (u, v) in zip(t, ns, uv):
                        verts += [q[0] - piv[0], q[1] - piv[1], q[2] - piv[2], n[0], n[1], n[2], u, v]
            out.append({"name": gname, "pivot": piv, "verts": verts, "pieces": idx})
        return out

    def preview(self):
        """For the page: positions, normals and texture coordinates as base64 float32 (little endian, three.js reads
        them as they are), the piece of each triangle as base64 uint16, the texture as a PNG data URL."""
        import base64
        import io
        pos, nor, uvs, owner = [], [], [], []
        for pi, p in enumerate(self.pieces):
            for t, ns, uv in zip(p["tris"], p["normals"], p["uvs"]):
                for q, n, (u, v) in zip(t, ns, uv):
                    pos += q
                    nor += n
                    uvs += (u, v)
                owner.append(pi)
        b = io.BytesIO()
        self.image.save(b, "PNG")
        enc = lambda fmt, vals: base64.b64encode(struct.pack("<%d%s" % (len(vals), fmt), *vals)).decode()
        return {"positions": enc("f", pos), "normals": enc("f", nor), "uvs": enc("f", uvs), "piece": enc("H", owner),
                "texture": "data:image/png;base64," + base64.b64encode(b.getvalue()).decode(),
                "size": list(self.image.size), "triangles": len(owner)}


def build(pieces, colour_fns, tone=None, seed=0, cap=CAP):
    """One mesh gun from its pieces (triangle lists in Decimation's model units) and their colour functions
    (colour_fns[piece](source triangle, weights, point) -> sRGB): wound outward, capped, baked, normals smoothed."""
    oriented, flips = [], []
    for tris in pieces:
        t2, flipped = orient(tris)
        oriented.append(t2)
        flips.append(flipped)
    fns = [(lambda fn: (lambda t, w, p: fn(t, (w[0], w[2], w[1]), p)))(fn) if fl else fn
           for fn, fl in zip(colour_fns, flips)]
    capped, exact = cap_pieces(oriented, cap)
    if not exact:
        fns = nearest_colours(oriented, fns, capped)
    built = bake(capped, fns, tone=tone, seed=seed)
    out = [{"tris": tris, "normals": smooth_normals(tris), "uvs": built.pieces[pi]["uvs"]}
           for pi, tris in enumerate(capped)]
    stats = {"triangles": sum(len(t) for t in capped), "source": sum(len(t) for t in pieces), "exact": exact,
             "texture": list(built.image.size), "charts": len(built.charts), "flipped": sum(flips)}
    return MeshResult(out, built.image, stats)

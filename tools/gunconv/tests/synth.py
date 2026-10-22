"""Synthetic test models for the gun converter tests: our own geometry, built here, nothing downloaded.

Meshes are (verts, faces): verts a list of (x, y, z), faces a list of (i, j, k) with outward counter clockwise winding.
Writers make OBJ text, GLB bytes and glTF JSON (data URI buffer) from named meshes.
"""
import base64
import json
import math
import struct

# corner order of hexa(): the vertex index of each (ix, iy, iz); box faces wound outward
HEXA = [(0, 0, 0), (1, 0, 0), (1, 1, 0), (0, 1, 0), (0, 0, 1), (1, 0, 1), (1, 1, 1), (0, 1, 1)]
HEXA_FACES = [(0, 2, 1), (0, 3, 2), (4, 5, 6), (4, 6, 7), (0, 1, 5), (0, 5, 4),
              (3, 7, 6), (3, 6, 2), (0, 4, 7), (0, 7, 3), (1, 2, 6), (1, 6, 5)]


def hexa(c):
    """A closed hexahedron from corners c[(ix, iy, iz)] = (x, y, z)."""
    return [tuple(c[k]) for k in HEXA], list(HEXA_FACES)


def box(x0, y0, z0, x1, y1, z1):
    a, b = (x0, y0, z0), (x1, y1, z1)
    return hexa({k: tuple(b[i] if k[i] else a[i] for i in range(3)) for k in HEXA})


def frustum_x(x0, x1, end0, end1):
    """A body along x whose end faces are the rectangles end0 = (y0, y1, z0, z1) at x0 and end1 at x1."""
    c = {}
    for k in HEXA:
        y0, y1, z0, z1 = end1 if k[0] else end0
        c[k] = (x1 if k[0] else x0, y1 if k[1] else y0, z1 if k[2] else z0)
    return hexa(c)


def cylinder_x(x0, x1, cy, cz, r, n=16):
    """A closed round bar along x (n sides, a vertex at angle 0), end caps fanned from their centre."""
    verts, faces = [], []
    for x in (x0, x1):
        for k in range(n):
            a = 2 * math.pi * k / n
            verts.append((x, cy + r * math.sin(a), cz + r * math.cos(a)))
    c0, c1 = len(verts), len(verts) + 1
    verts += [(x0, cy, cz), (x1, cy, cz)]
    for k in range(n):
        a, b = k, (k + 1) % n
        # side quad: ring 0 (x0) and ring 1 (x1); outward normal points away from the axis
        faces += [(a, b, n + b), (a, n + b, n + a)]
        faces += [(c0, b, a), (c1, n + a, n + b)]
    return verts, fix_winding(verts, faces)


def arc_tube(xc, yc, radius, sweep_deg, half_depth, half_width, z=0.0, steps=12):
    """A curved magazine: a rectangular tube whose centre line starts at (xc, yc), runs DOWN (-y) and curves forward
    (+x) along a circle of `radius` through `sweep_deg` degrees; closed with end caps."""
    verts = []
    for s in range(steps + 1):
        phi = math.radians(sweep_deg) * s / steps
        cx, cy = xc + radius - radius * math.cos(phi), yc - radius * math.sin(phi)
        nx, ny = math.cos(phi), math.sin(phi)            # in plane normal (toward the front / up)
        for dn, dz in ((-1, -1), (1, -1), (1, 1), (-1, 1)):
            verts.append((cx + dn * half_depth * nx, cy + dn * half_depth * ny, z + dz * half_width))
    faces = []
    for s in range(steps):
        a, b = 4 * s, 4 * (s + 1)
        for k in range(4):
            k2 = (k + 1) % 4
            faces += [(a + k, a + k2, b + k2), (a + k, b + k2, b + k)]
    last = 4 * steps
    faces += [(0, 1, 2), (0, 2, 3), (last, last + 1, last + 2), (last, last + 2, last + 3)]
    return verts, fix_winding(verts, faces)


def chamfered_raked(height, depth, width, rake, bevel):
    """A grip like prism standing from y 0 down to y = height (y grows downward here, as in Decimation), front to back
    `depth` along x, `width` in z, its bottom `rake` further back (-x); top and bottom bevelled to a thin ridge over
    `bevel` (a closed mesh: rings at y 0, bevel, height - bevel, height)."""
    def ring(y, inset):
        dx = -rake * y / height
        return [(dx + inset, y, -width / 2 + inset), (dx + depth - inset, y, -width / 2 + inset),
                (dx + depth - inset, y, width / 2 - inset), (dx + inset, y, width / 2 - inset)]
    ridge = min(depth, width) / 2 - 0.01
    rings = [ring(0.0, ridge), ring(bevel, 0.0), ring(height - bevel, 0.0), ring(height, ridge)]
    verts = [v for r in rings for v in r]
    faces = []
    for s in range(3):
        a, b = 4 * s, 4 * (s + 1)
        for k in range(4):
            k2 = (k + 1) % 4
            faces += [(a + k, a + k2, b + k2), (a + k, b + k2, b + k)]
    faces += [(0, 1, 2), (0, 2, 3), (12, 13, 14), (12, 14, 15)]
    return verts, fix_winding(verts, faces)


def fix_winding(verts, faces):
    """Orient every face away from the mesh centre (enough for the convex or tube shapes made here)."""
    cx = [sum(v[i] for v in verts) / len(verts) for i in range(3)]
    out = []
    for f in faces:
        a, b, c = (verts[i] for i in f)
        n = cross(sub(b, a), sub(c, a))
        m = [(a[i] + b[i] + c[i]) / 3 for i in range(3)]
        out.append(f if dot(n, sub(m, cx)) >= 0 else (f[0], f[2], f[1]))
    return out


def fix_winding_local(verts, faces, centre_of):
    """Orient each face away from centre_of(face) (a tube: the centre line point nearest the face)."""
    out = []
    for f in faces:
        a, b, c = (verts[i] for i in f)
        n = cross(sub(b, a), sub(c, a))
        m = [(a[i] + b[i] + c[i]) / 3 for i in range(3)]
        out.append(f if dot(n, sub(m, centre_of(m))) >= 0 else (f[0], f[2], f[1]))
    return out


def sub(a, b):
    return (a[0] - b[0], a[1] - b[1], a[2] - b[2])


def dot(a, b):
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def merge(*meshes):
    verts, faces = [], []
    for v, f in meshes:
        o = len(verts)
        verts += list(v)
        faces += [(a + o, b + o, c + o) for a, b, c in f]
    return verts, faces


def transform(mesh, m):
    """Apply a 3x4 matrix (rows) to a mesh's vertices."""
    verts, faces = mesh
    return [tuple(m[r][0] * v[0] + m[r][1] * v[1] + m[r][2] * v[2] + m[r][3] for r in range(3)) for v in verts], faces


# ---------------------------------------------------------------- a synthetic rifle

def synth_gun():
    """A rifle like shape in a right handed, y up frame with the muzzle toward +x (so its right side is +z: forward x
    up). Named pieces: receiver, barrel (round), frontSight, stock (tall at the butt), grip, Magazine (curved, hanging
    down and forward), Bolt (a charging handle nub on the RIGHT side). Units: about 1 = 31 mm."""
    return [
        ("receiver", box(0, 0, -0.5, 10, 1.6, 0.5), "Steel", (60, 60, 62)),
        ("barrel", cylinder_x(10, 20, 1.0, 0.0, 0.25), "Steel", (60, 60, 62)),
        ("frontSight", box(18, 1.25, -0.1, 18.4, 2.2, 0.1), "Steel", (60, 60, 62)),
        ("stock", frustum_x(-8, 0, (-1.5, 1.6, -0.4, 0.4), (0.2, 1.4, -0.35, 0.35)), "Wood", (142, 92, 82)),
        ("grip", frustum_x(2, 3.2, (-3, 0, -0.4, 0.4), (-3, 0, -0.4, 0.4)), "Wood", (142, 92, 82)),
        ("Magazine", arc_tube(6.6, 0.0, 9.0, 32, 0.7, 0.35), "Mag", (150, 70, 40)),
        ("Bolt", box(7, 1.0, 0.5, 7.6, 1.3, 1.0), "Steel", (60, 60, 62)),
    ]


ROTATIONS = {
    # source frames to test: rows of a 3x3 rotation (proper: det +1) applied to the y up, muzzle +x gun
    "identity": ((1, 0, 0), (0, 1, 0), (0, 0, 1)),
    "muzzle -x": ((-1, 0, 0), (0, 1, 0), (0, 0, -1)),          # turned 180 degrees about y
    "muzzle -z, up +y": ((0, 0, 1), (0, 1, 0), (-1, 0, 0)),     # turned 90 degrees about y
    "muzzle +y, up -z": ((0, -1, 0), (0, 0, 1), (-1, 0, 0)),    # a z up file standing on its butt
}


def rotated(pieces, rot, scale=1.0, offset=(0, 0, 0)):
    m = [[rot[r][0] * scale, rot[r][1] * scale, rot[r][2] * scale, offset[r]] for r in range(3)]
    return [(n, transform(mesh, m), mat, col) for n, mesh, mat, col in pieces]


# ---------------------------------------------------------------- writers

def obj_text(pieces):
    """OBJ text (o / usemtl / v / f) and its MTL text for pieces [(name, (verts, faces), material, colour)]."""
    lines, mtl, mats, base = ["mtllib test.mtl"], [], set(), 1
    for name, (verts, faces), mat, col in pieces:
        lines.append("o %s" % name)
        lines += ["v %r %r %r" % v for v in verts]
        lines.append("usemtl %s" % mat)
        lines += ["f %d %d %d" % (a + base, b + base, c + base) for a, b, c in faces]
        base += len(verts)
        if mat not in mats:
            mats.add(mat)
            mtl += ["newmtl %s" % mat, "Kd %r %r %r" % tuple(c / 255.0 for c in col)]
    return "\n".join(lines) + "\n", "\n".join(mtl) + "\n"


def srgb_to_linear(c):
    c = c / 255.0
    return c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4


class Glb:
    """A minimal glTF 2.0 writer for tests."""

    def __init__(self):
        self.bin = bytearray()
        self.j = {"asset": {"version": "2.0", "generator": "gunconv tests"}, "buffers": [], "bufferViews": [],
                  "accessors": [], "meshes": [], "nodes": [], "materials": [], "scenes": [{"nodes": []}], "scene": 0}

    def _view(self, data):
        while len(self.bin) % 4:
            self.bin.append(0)
        self.j["bufferViews"].append({"buffer": 0, "byteOffset": len(self.bin), "byteLength": len(data)})
        self.bin += data
        return len(self.j["bufferViews"]) - 1

    def accessor(self, values, fmt, ctype, type_, n, normalized=False, minmax=False):
        flat = [c for v in values for c in (v if isinstance(v, (tuple, list)) else (v,))]
        view = self._view(struct.pack("<%d%s" % (len(flat), fmt), *flat))
        a = {"bufferView": view, "componentType": ctype, "count": len(values), "type": type_}
        if normalized:
            a["normalized"] = True
        if minmax:
            a["min"] = [min(v[i] for v in values) for i in range(n)]
            a["max"] = [max(v[i] for v in values) for i in range(n)]
        self.j["accessors"].append(a)
        return len(self.j["accessors"]) - 1

    def material(self, name, rgb255):
        self.j["materials"].append({"name": name, "pbrMetallicRoughness": {
            "baseColorFactor": [srgb_to_linear(c) for c in rgb255] + [1.0]}})
        return len(self.j["materials"]) - 1

    def mesh(self, name, mesh, material=None, joints=None, weights=None):
        verts, faces = mesh
        attrs = {"POSITION": self.accessor(verts, "f", 5126, "VEC3", 3, minmax=True)}
        if joints is not None:
            attrs["JOINTS_0"] = self.accessor(joints, "B", 5121, "VEC4", 4)
            attrs["WEIGHTS_0"] = self.accessor(weights, "f", 5126, "VEC4", 4)
        prim = {"attributes": attrs, "indices": self.accessor([i for f in faces for i in f], "I", 5125, "SCALAR", 1),
                "mode": 4}
        if material is not None:
            prim["material"] = material
        self.j["meshes"].append({"name": name, "primitives": [prim]})
        return len(self.j["meshes"]) - 1

    def node(self, name, mesh=None, t=None, r=None, s=None, children=None, skin=None, root=True):
        n = {"name": name}
        for k, v in (("mesh", mesh), ("translation", t), ("rotation", r), ("scale", s), ("children", children),
                     ("skin", skin)):
            if v is not None:
                n[k] = v
        self.j["nodes"].append(n)
        i = len(self.j["nodes"]) - 1
        if root:
            self.j["scenes"][0]["nodes"].append(i)
        return i

    def skin(self, joints, ibms):
        cols = [tuple(m[r][c] for c in range(4) for r in range(4)) for m in ibms]   # column major
        self.j.setdefault("skins", []).append({"joints": joints, "inverseBindMatrices": self.accessor(
            cols, "f", 5126, "MAT4", 16)})
        return len(self.j["skins"]) - 1

    def bytes(self):
        while len(self.bin) % 4:
            self.bin.append(0)
        self.j["buffers"] = [{"byteLength": len(self.bin)}]
        js = json.dumps(self.j).encode()
        js += b" " * ((4 - len(js) % 4) % 4)
        body = struct.pack("<I4s", len(js), b"JSON") + js + struct.pack("<I4s", len(self.bin), b"BIN\x00") + bytes(self.bin)
        return struct.pack("<4sII", b"glTF", 2, 12 + len(body)) + body

    def gltf_text(self):
        while len(self.bin) % 4:
            self.bin.append(0)
        j = json.loads(json.dumps(self.j))
        j["buffers"] = [{"byteLength": len(self.bin),
                         "uri": "data:application/octet-stream;base64," + base64.b64encode(bytes(self.bin)).decode()}]
        return json.dumps(j)


def glb_of(pieces):
    """A GLB with one node and mesh per piece, materials from the pieces' colours."""
    g = Glb()
    mats = {}
    for name, mesh, mat, col in pieces:
        if mat not in mats:
            mats[mat] = g.material(mat, col)
        g.node(name, mesh=g.mesh(name, mesh, mats[mat]))
    return g

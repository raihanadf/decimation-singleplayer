#!/usr/bin/env python3
"""Read 3D gun models for the converter (docs/gunconv.md): GLB and glTF 2.0, OBJ with its MTL, Blockbench .bbmodel.
Pure Python (struct, json); Pillow only to sample texture colours.

A Model is a list of Mesh objects: name (node / object / group / element name), material name, colour (sRGB 0..255,
the material's colour and the texture's average where there is one), verts [(x, y, z)] in the file's own space with
every node transform applied, faces [(i, j, k)] (triangles), and for skinned glTF meshes vlabels: the name of the joint
each vertex follows most (the bone a magazine or a bolt hangs on: the converter reads part groups from them).

Skinned glTF meshes are placed in the pose of their joints (the bind pose unless the file stores another), and the
mesh node's own transform is ignored as the glTF spec says. Draco and meshopt compressed files are refused with a
message: export them again without compression.
"""
import base64
import json
import math
import os
import struct
from io import BytesIO


class MeshError(Exception):
    pass


class Mesh:
    def __init__(self, name, material, colour, verts, faces, vlabels=None, vcolours=None):
        self.name, self.material, self.colour = name, material, colour
        self.verts, self.faces = verts, faces
        self.vlabels, self.vcolours = vlabels, vcolours
        # the texture behind the colours (colour_at): per vertex (u, v) with v down as images run, the image, the
        # material's multiplier (glTF: linear, OBJ: as written) and per vertex multipliers (or None)
        self.uvs, self.image, self.tint, self.linear, self.vcol = None, None, (1.0, 1.0, 1.0), False, None

    def colour_at(self, fi, w):
        """sRGB 0..255 inside face fi at barycentric weights w: the texture at the interpolated UV when the mesh has
        one (a low poly gun's detail lives in its texture), else its vertex colours interpolated, else its colour."""
        f = self.faces[fi]
        if self.image is not None and self.uvs is not None and all(self.uvs[i] is not None for i in f):
            u = sum(w[k] * self.uvs[f[k]][0] for k in range(3))
            v = sum(w[k] * self.uvs[f[k]][1] for k in range(3))
            t = sample(self.image, u, v)
            m = [sum(w[k] * self.vcol[f[k]][i] for k in range(3)) for i in range(3)] if self.vcol else (1.0, 1.0, 1.0)
            if self.linear:
                return to255([linear_to_srgb(self.tint[i] * srgb_to_linear(t[i]) * m[i]) for i in range(3)])
            return to255([self.tint[i] * t[i] * m[i] for i in range(3)])
        if self.vcolours:
            return tuple(int(round(sum(w[k] * self.vcolours[f[k]][i] for k in range(3)))) for i in range(3))
        return tuple(int(c) for c in self.colour[:3])


class Model:
    def __init__(self, meshes, fmt, name=""):
        self.meshes, self.fmt, self.name = meshes, fmt, name

    def triangle_count(self):
        return sum(len(m.faces) for m in self.meshes)

    def points(self):
        return [v for m in self.meshes for v in m.verts]


def load(path):
    with open(path, "rb") as f:
        data = f.read()
    return read_bytes(data, os.path.basename(path), os.path.dirname(os.path.abspath(path)))


def read_bytes(data, filename, base_dir=None):
    ext = os.path.splitext(filename)[1].lower()
    stem = os.path.splitext(os.path.basename(filename))[0]
    if data[:4] == b"glTF":
        j, binchunk = parse_glb(data)
        return read_gltf(j, binchunk, base_dir, "glb", stem)
    if ext == ".gltf":
        return read_gltf(json.loads(data.decode("utf-8")), None, base_dir, "gltf", stem)
    if ext == ".obj":
        return read_obj(data.decode("utf-8", "replace"), base_dir, stem)
    if ext == ".bbmodel":
        return read_bbmodel(json.loads(data.decode("utf-8")), stem)
    raise MeshError("unknown model format %r: use GLB, glTF, OBJ or a Blockbench .bbmodel" % (ext or filename))


# ---------------------------------------------------------------- colour helpers

def linear_to_srgb(c):
    c = max(0.0, min(1.0, c))
    return 12.92 * c if c <= 0.0031308 else 1.055 * c ** (1 / 2.4) - 0.055


def srgb_to_linear(c):
    return c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4


def to255(rgb01):
    return tuple(int(round(max(0.0, min(1.0, c)) * 255)) for c in rgb01)


def open_image(data):
    from PIL import Image
    return Image.open(BytesIO(data)).convert("RGBA")


def sample(img, u, v):
    """The texel at (u, v) (wrapped, v down as glTF and the OBJ convention flipped by the caller), sRGB 0..1."""
    w, h = img.size
    x = int(math.floor((u % 1.0) * w)) % w
    y = int(math.floor((v % 1.0) * h)) % h
    r, g, b, _ = img.getpixel((x, y))
    return r / 255.0, g / 255.0, b / 255.0


# ---------------------------------------------------------------- matrices (4x4, rows)

IDENT = [[1.0 if r == c else 0.0 for c in range(4)] for r in range(4)]


def mat_mul(a, b):
    return [[sum(a[i][k] * b[k][j] for k in range(4)) for j in range(4)] for i in range(4)]


def mat_apply(m, p):
    return (m[0][0] * p[0] + m[0][1] * p[1] + m[0][2] * p[2] + m[0][3],
            m[1][0] * p[0] + m[1][1] * p[1] + m[1][2] * p[2] + m[1][3],
            m[2][0] * p[0] + m[2][1] * p[1] + m[2][2] * p[2] + m[2][3])


def quat_matrix(q):
    x, y, z, w = q
    return [[1 - 2 * (y * y + z * z), 2 * (x * y - z * w), 2 * (x * z + y * w), 0.0],
            [2 * (x * y + z * w), 1 - 2 * (x * x + z * z), 2 * (y * z - x * w), 0.0],
            [2 * (x * z - y * w), 2 * (y * z + x * w), 1 - 2 * (x * x + y * y), 0.0],
            [0.0, 0.0, 0.0, 1.0]]


def node_matrix(n):
    if "matrix" in n:
        m = n["matrix"]                                  # column major
        return [[float(m[c * 4 + r]) for c in range(4)] for r in range(4)]
    t = n.get("translation", [0, 0, 0])
    s = n.get("scale", [1, 1, 1])
    r = quat_matrix(n.get("rotation", [0, 0, 0, 1]))
    return [[r[i][j] * s[j] for j in range(3)] + [float(t[i])] for i in range(3)] + [[0.0, 0.0, 0.0, 1.0]]


def euler_matrix(rx, ry, rz, origin):
    """Blockbench rotation (degrees, X applied first, then Y, then Z) about `origin`, as a 4x4."""
    def rot(axis, deg):
        a = math.radians(deg)
        c, s = math.cos(a), math.sin(a)
        if axis == 0:
            return [[1, 0, 0, 0], [0, c, -s, 0], [0, s, c, 0], [0, 0, 0, 1]]
        if axis == 1:
            return [[c, 0, s, 0], [0, 1, 0, 0], [-s, 0, c, 0], [0, 0, 0, 1]]
        return [[c, -s, 0, 0], [s, c, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]]
    m = mat_mul(rot(2, rz), mat_mul(rot(1, ry), rot(0, rx)))
    ox, oy, oz = origin
    to = [[1, 0, 0, ox], [0, 1, 0, oy], [0, 0, 1, oz], [0, 0, 0, 1]]
    back = [[1, 0, 0, -ox], [0, 1, 0, -oy], [0, 0, 1, -oz], [0, 0, 0, 1]]
    return mat_mul(to, mat_mul(m, back))


# ---------------------------------------------------------------- glTF

FMT = {5120: "b", 5121: "B", 5122: "h", 5123: "H", 5125: "I", 5126: "f"}
NORM = {5120: 127.0, 5121: 255.0, 5122: 32767.0, 5123: 65535.0, 5125: 4294967295.0}
NCOMP = {"SCALAR": 1, "VEC2": 2, "VEC3": 3, "VEC4": 4, "MAT2": 4, "MAT3": 9, "MAT4": 16}
UNSUPPORTED = {"KHR_draco_mesh_compression": "Draco", "EXT_meshopt_compression": "meshopt",
               "KHR_meshopt_compression": "meshopt"}


def parse_glb(data):
    if len(data) < 20:
        raise MeshError("GLB file too short")
    magic, version, length = struct.unpack_from("<4sII", data, 0)
    if version != 2:
        raise MeshError("GLB version %d: only glTF 2.0 is read" % version)
    off, j, binchunk = 12, None, None
    while off + 8 <= min(length, len(data)):
        clen, ctype = struct.unpack_from("<I4s", data, off)
        chunk = data[off + 8:off + 8 + clen]
        if ctype == b"JSON":
            j = json.loads(chunk.decode("utf-8"))
        elif ctype == b"BIN\x00" and binchunk is None:
            binchunk = chunk
        off += 8 + clen
    if j is None:
        raise MeshError("GLB without a JSON chunk")
    return j, binchunk


def safe_join(base_dir, rel):
    if base_dir is None:
        raise MeshError("the file names an outside resource %r: load it from disk, not from memory" % rel)
    from urllib.parse import unquote
    path = os.path.normpath(os.path.join(base_dir, unquote(rel)))
    if not path.startswith(os.path.normpath(base_dir) + os.sep):
        raise MeshError("resource outside the model's folder refused: %r" % rel)
    return path


def uri_bytes(uri, base_dir):
    if uri.startswith("data:"):
        head, _, payload = uri.partition(",")
        return base64.b64decode(payload) if head.endswith(";base64") else payload.encode()
    with open(safe_join(base_dir, uri), "rb") as f:
        return f.read()


class Gltf:
    def __init__(self, j, binchunk, base_dir):
        self.j, self.base_dir = j, base_dir
        used = set(j.get("extensionsRequired", [])) | set(j.get("extensionsUsed", []))
        for ext, what in UNSUPPORTED.items():
            if ext in used:
                raise MeshError("this file is compressed with %s (%s): export it again without compression" % (what, ext))
        self.buffers = []
        for i, b in enumerate(j.get("buffers", [])):
            if "uri" in b:
                self.buffers.append(uri_bytes(b["uri"], base_dir))
            elif i == 0 and binchunk is not None:
                self.buffers.append(binchunk)
            else:
                raise MeshError("buffer %d has no data" % i)
        self.images = {}

    def view_bytes(self, vi):
        bv = self.j["bufferViews"][vi]
        buf = self.buffers[bv["buffer"]]
        off = bv.get("byteOffset", 0)
        return buf[off:off + bv["byteLength"]], bv.get("byteStride")

    def accessor(self, ai):
        a = self.j["accessors"][ai]
        fmt, n, count = FMT[a["componentType"]], NCOMP[a["type"]], a["count"]
        size = struct.calcsize("<" + fmt)
        if "bufferView" in a:
            data, stride = self.view_bytes(a["bufferView"])
            stride = stride or size * n
            base = a.get("byteOffset", 0)
            out = [list(struct.unpack_from("<%d%s" % (n, fmt), data, base + k * stride)) for k in range(count)]
        else:
            out = [[0] * n for _ in range(count)]
        sp = a.get("sparse")
        if sp:
            idata, _ = self.view_bytes(sp["indices"]["bufferView"])
            ifmt = FMT[sp["indices"]["componentType"]]
            ioff = sp["indices"].get("byteOffset", 0)
            vdata, _ = self.view_bytes(sp["values"]["bufferView"])
            voff = sp["values"].get("byteOffset", 0)
            for k in range(sp["count"]):
                idx = struct.unpack_from("<" + ifmt, idata, ioff + k * struct.calcsize("<" + ifmt))[0]
                out[idx] = list(struct.unpack_from("<%d%s" % (n, fmt), vdata, voff + k * size * n))
        if a.get("normalized") and fmt != "f":
            d = NORM[a["componentType"]]
            out = [[max(-1.0, c / d) for c in v] for v in out]
        return out

    def image(self, ii):
        if ii not in self.images:
            img = self.j["images"][ii]
            try:
                if "bufferView" in img:
                    data, _ = self.view_bytes(img["bufferView"])
                    self.images[ii] = open_image(bytes(data))
                else:
                    self.images[ii] = open_image(uri_bytes(img["uri"], self.base_dir))
            except Exception:                            # a texture we cannot read: material colour only
                self.images[ii] = None
        return self.images[ii]


def read_gltf(j, binchunk, base_dir, fmt, stem=""):
    g = Gltf(j, binchunk, base_dir)
    nodes = j.get("nodes", [])
    world, parent = {}, {}
    for i, n in enumerate(nodes):
        for c in n.get("children", []):
            parent[c] = i
    scenes = j.get("scenes")
    roots = scenes[j.get("scene", 0)]["nodes"] if scenes else [i for i in range(len(nodes)) if i not in parent]

    def walk(i, m):
        world[i] = mat_mul(m, node_matrix(nodes[i]))
        for c in nodes[i].get("children", []):
            walk(c, world[i])
    for r in roots:
        walk(r, IDENT)
    for i in range(len(nodes)):                          # joints outside the scene still have a pose
        if i not in world:
            chain, k = [], i
            while k is not None:
                chain.append(k)
                k = parent.get(k)
            m = IDENT
            for k in reversed(chain):
                m = mat_mul(m, node_matrix(nodes[k]))
            world[i] = m
    meshes = []
    for ni in sorted(world):
        n = nodes[ni]
        if "mesh" not in n:
            continue
        gm = j["meshes"][n["mesh"]]
        name = n.get("name") or gm.get("name") or "%s_%d" % (stem, ni)
        for p in gm.get("primitives", []):
            mesh = read_primitive(g, p, n, world[ni], world, name)
            if mesh is not None and mesh.faces:
                meshes.append(mesh)
    if not meshes:
        raise MeshError("no triangles in this file")
    return Model(meshes, fmt, stem)


def read_primitive(g, p, node, m, world, name):
    j = g.j
    mode = p.get("mode", 4)
    if mode not in (4, 5, 6) or "POSITION" not in p.get("attributes", {}):
        return None
    attrs = p["attributes"]
    pos = [tuple(v) for v in g.accessor(attrs["POSITION"])]
    idx = [v[0] for v in g.accessor(p["indices"])] if "indices" in p else list(range(len(pos)))
    if mode == 4:
        faces = [(idx[k], idx[k + 1], idx[k + 2]) for k in range(0, len(idx) - 2, 3)]
    elif mode == 5:
        faces = [(idx[k], idx[k + 1], idx[k + 2]) if k % 2 == 0 else (idx[k + 1], idx[k], idx[k + 2])
                 for k in range(len(idx) - 2)]
    else:
        faces = [(idx[0], idx[k], idx[k + 1]) for k in range(1, len(idx) - 1)]
    faces = [f for f in faces if len(set(f)) == 3]
    labels = None
    if "skin" in node and "JOINTS_0" in attrs and "WEIGHTS_0" in attrs:
        skin = j["skins"][node["skin"]]
        joints = skin["joints"]
        ibm = [IDENT] * len(joints)
        if "inverseBindMatrices" in skin:
            ibm = [[[float(c[col * 4 + row]) for col in range(4)] for row in range(4)]
                   for c in g.accessor(skin["inverseBindMatrices"])]
        jm = [mat_mul(world[jt], ibm[k]) for k, jt in enumerate(joints)]
        names = [j["nodes"][jt].get("name", "joint%d" % k) for k, jt in enumerate(joints)]
        J, W = g.accessor(attrs["JOINTS_0"]), g.accessor(attrs["WEIGHTS_0"])
        verts, labels = [], []
        for v, jj, ww in zip(pos, J, W):
            tot = sum(ww)
            if tot <= 0:
                verts.append(mat_apply(m, v))
                labels.append(None)
                continue
            s = [0.0, 0.0, 0.0]
            for a, b in zip(jj, ww):
                if b > 0:
                    q = mat_apply(jm[int(a)], v)
                    s = [s[t] + b / tot * q[t] for t in range(3)]
            verts.append(tuple(s))
            labels.append(names[int(jj[max(range(len(ww)), key=lambda k: ww[k])])])
    else:
        verts = [mat_apply(m, v) for v in pos]
    # colour: base colour factor (linear) times the texture (sRGB) at each vertex's UV, times vertex colours
    mat_name, factor, tex = "default", [1.0, 1.0, 1.0], None
    if "material" in p:
        mat = j["materials"][p["material"]]
        mat_name = mat.get("name", "material%d" % p["material"])
        pbr = mat.get("pbrMetallicRoughness", {})
        factor = list(pbr.get("baseColorFactor", [1, 1, 1, 1]))[:3]
        bt = pbr.get("baseColorTexture")
        if bt is not None and "textures" in j:
            t = j["textures"][bt["index"]]
            if "source" in t:
                img = g.image(t["source"])
                uvkey = "TEXCOORD_%d" % bt.get("texCoord", 0)
                if img is not None and uvkey in attrs:
                    tex = (img, g.accessor(attrs[uvkey]))
    vcol = g.accessor(attrs["COLOR_0"]) if "COLOR_0" in attrs else None
    vcolours = None
    if tex is not None or vcol is not None:
        vcolours = []
        for k in range(len(verts)):
            lin = list(factor)
            if tex is not None:
                img, uv = tex
                texel = sample(img, uv[k][0], uv[k][1])
                lin = [lin[i] * srgb_to_linear(texel[i]) for i in range(3)]
            if vcol is not None:
                lin = [lin[i] * vcol[k][i] for i in range(3)]
            vcolours.append(to255([linear_to_srgb(c) for c in lin]))
        colour = tuple(int(round(sum(c[i] for c in vcolours) / len(vcolours))) for i in range(3))
    else:
        colour = to255([linear_to_srgb(c) for c in factor])
    mesh = Mesh(name, mat_name, colour, verts, faces, labels, vcolours)
    mesh.tint, mesh.linear = tuple(factor), True
    if vcol is not None:
        mesh.vcol = [tuple(c[:3]) for c in vcol]
    if tex is not None:
        mesh.image, mesh.uvs = tex[0], [tuple(uv[:2]) for uv in tex[1]]
    return mesh


# ---------------------------------------------------------------- OBJ

def read_mtl(path):
    out, cur = {}, None
    try:
        with open(path, encoding="utf-8", errors="replace") as f:
            lines = f.read().splitlines()
    except OSError:
        return out
    for line in lines:
        s = line.split()
        if not s:
            continue
        if s[0] == "newmtl":
            cur = " ".join(s[1:])
            out[cur] = {"Kd": (0.8, 0.8, 0.8)}
        elif cur and s[0] == "Kd" and len(s) >= 4:
            out[cur]["Kd"] = tuple(float(x) for x in s[1:4])
        elif cur and s[0] == "map_Kd" and len(s) >= 2:
            out[cur]["map_Kd"] = s[-1]
    return out


def read_obj(text, base_dir, stem=""):
    V, VT, VC = [], [], []
    mtls = {}
    runs, cur = [], None
    obj_name, grp_name, material = stem, None, "default"

    def run():
        nonlocal cur
        name = grp_name or obj_name or stem
        if cur is None or cur["name"] != name or cur["material"] != material:
            cur = {"name": name, "material": material, "faces": []}
            runs.append(cur)
        return cur
    for line in text.splitlines():
        s = line.split()
        if not s or s[0].startswith("#"):
            continue
        k = s[0]
        if k == "v":
            V.append((float(s[1]), float(s[2]), float(s[3])))
            VC.append(tuple(float(c) for c in s[4:7]) if len(s) >= 7 else None)
        elif k == "vt":
            VT.append((float(s[1]), float(s[2]) if len(s) > 2 else 0.0))
        elif k == "o":
            obj_name, grp_name = " ".join(s[1:]) or stem, None
        elif k == "g":
            grp_name = " ".join(s[1:]) or None
        elif k == "usemtl":
            material = " ".join(s[1:]) or "default"
        elif k == "mtllib" and base_dir:
            for f in s[1:]:
                try:
                    mtls.update(read_mtl(safe_join(base_dir, f)))
                except MeshError:
                    pass
        elif k == "f":
            ids = []
            for t in s[1:]:
                parts = t.split("/")
                vi = int(parts[0])
                vi = vi - 1 if vi > 0 else len(V) + vi
                ti = None
                if len(parts) > 1 and parts[1]:
                    ti = int(parts[1])
                    ti = ti - 1 if ti > 0 else len(VT) + ti
                ids.append((vi, ti))
            r = run()
            for q in range(1, len(ids) - 1):
                r["faces"].append((ids[0], ids[q], ids[q + 1]))
    textures = {}
    meshes = []
    for r in runs:
        if not r["faces"]:
            continue
        remap, verts, uvs, faces, vcols = {}, [], [], [], []
        for f in r["faces"]:
            tri = []
            for vi, ti in f:
                key = (vi, ti)
                if key not in remap:
                    remap[key] = len(verts)
                    verts.append(V[vi])
                    uvs.append(VT[ti] if ti is not None and ti < len(VT) else None)
                    vcols.append(VC[vi])
                tri.append(remap[key])
            if len(set(tri)) == 3:
                faces.append(tuple(tri))
        m = mtls.get(r["material"], {})
        kd = m.get("Kd", (0.8, 0.8, 0.8))
        vcolours = None
        img = None
        if m.get("map_Kd") and base_dir:
            if m["map_Kd"] not in textures:
                try:
                    with open(safe_join(base_dir, m["map_Kd"]), "rb") as f:
                        textures[m["map_Kd"]] = open_image(f.read())
                except Exception:
                    textures[m["map_Kd"]] = None
            img = textures[m["map_Kd"]]
        if img is not None or any(c is not None for c in vcols):
            vcolours = []
            for uv, vc in zip(uvs, vcols):
                c = list(kd)
                if img is not None and uv is not None:
                    t = sample(img, uv[0], 1.0 - uv[1])          # OBJ v runs up
                    c = [c[i] * t[i] for i in range(3)]
                if vc is not None:
                    c = [c[i] * vc[i] for i in range(3)]
                vcolours.append(to255(c))
            colour = tuple(int(round(sum(c[i] for c in vcolours) / len(vcolours))) for i in range(3))
        else:
            colour = to255(kd)
        mesh = Mesh(r["name"], r["material"], colour, verts, faces, None, vcolours)
        mesh.tint = tuple(kd)
        if any(c is not None for c in vcols):
            mesh.vcol = [c if c is not None else (1.0, 1.0, 1.0) for c in vcols]
        if img is not None:
            mesh.image, mesh.uvs = img, [(uv[0], 1.0 - uv[1]) if uv is not None else None for uv in uvs]  # OBJ v up
        meshes.append(mesh)
    if not meshes:
        raise MeshError("no faces in this OBJ file")
    return Model(meshes, "obj", stem)


# ---------------------------------------------------------------- Blockbench .bbmodel

BOX_CORNERS = [(0, 0, 0), (1, 0, 0), (1, 1, 0), (0, 1, 0), (0, 0, 1), (1, 0, 1), (1, 1, 1), (0, 1, 1)]
BOX_FACES = [(0, 2, 1), (0, 3, 2), (4, 5, 6), (4, 6, 7), (0, 1, 5), (0, 5, 4),
             (3, 7, 6), (3, 6, 2), (0, 4, 7), (0, 7, 3), (1, 2, 6), (1, 6, 5)]


def outliner_paths(items, prefix, out):
    for it in items:
        if isinstance(it, str):
            out[it] = prefix
        elif isinstance(it, dict):
            name = it.get("name", "group")
            outliner_paths(it.get("children", []), (prefix + "/" if prefix else "") + name, out)
    return out


def bb_textures(doc):
    out = []
    for t in doc.get("textures", []):
        img = None
        src = t.get("source", "")
        if src.startswith("data:"):
            try:
                img = open_image(base64.b64decode(src.partition(",")[2]))
            except Exception:
                img = None
        out.append((img, t.get("uv_width") or doc.get("resolution", {}).get("width", 16),
                    t.get("uv_height") or doc.get("resolution", {}).get("height", 16)))
    return out


def bb_colour(el, textures):
    """The mean texel colour over an element's face UV rectangles, or a neutral grey."""
    acc, n = [0.0, 0.0, 0.0], 0
    for face in (el.get("faces") or {}).values():
        if not isinstance(face, dict) or face.get("texture") is None or "uv" not in face:
            continue
        ti = face["texture"]
        if not isinstance(ti, int) or ti >= len(textures) or textures[ti][0] is None:
            continue
        img, uw, vh = textures[ti]
        u0, v0, u1, v1 = face["uv"][:4]
        for k in range(9):
            u = (u0 + (u1 - u0) * (0.17 + 0.33 * (k % 3))) / uw
            v = (v0 + (v1 - v0) * (0.17 + 0.33 * (k // 3))) / vh
            c = sample(img, u, v)
            acc = [acc[i] + c[i] for i in range(3)]
            n += 1
    return to255([c / n for c in acc]) if n else (128, 128, 128)


def read_bbmodel(doc, stem=""):
    paths = outliner_paths(doc.get("outliner", []), "", {})
    textures = bb_textures(doc)
    meshes = []
    for el in doc.get("elements", []):
        kind = el.get("type", "cube")
        origin = el.get("origin", [0, 0, 0])
        rot = el.get("rotation", [0, 0, 0])
        if isinstance(rot, dict):                        # older files: one axis, one angle
            r = [0.0, 0.0, 0.0]
            r["xyz".index(rot.get("axis", "y"))] = float(rot.get("angle", 0))
            origin = rot.get("origin", origin)
            rot = r
        m = euler_matrix(rot[0], rot[1], rot[2], origin)
        if kind == "cube":
            inf = float(el.get("inflate", 0))
            a = [float(c) - inf for c in el["from"]]
            b = [float(c) + inf for c in el["to"]]
            verts = [mat_apply(m, tuple(b[i] if k[i] else a[i] for i in range(3))) for k in BOX_CORNERS]
            faces = list(BOX_FACES)
        elif kind == "mesh":
            keys = list(el.get("vertices", {}))
            at = {k: i for i, k in enumerate(keys)}
            verts = [mat_apply(m, tuple(float(c) + float(origin[i]) for i, c in enumerate(el["vertices"][k])))
                     for k in keys]
            faces = []
            for f in el.get("faces", {}).values():
                ids = [at[k] for k in f.get("vertices", []) if k in at]
                faces += [(ids[0], ids[q], ids[q + 1]) for q in range(1, len(ids) - 1)]
        else:
            continue
        group = paths.get(el.get("uuid"), "")
        name = (group + "/" if group else "") + el.get("name", kind)
        meshes.append(Mesh(name, "bbmodel", bb_colour(el, textures), verts, faces))
    if not meshes:
        raise MeshError("no cubes or meshes in this .bbmodel")
    return Model(meshes, "bbmodel", stem)

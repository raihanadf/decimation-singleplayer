"""Converted guns as real meshes, 1:1 (converter milestone 4, docs/superpowers/specs/2026-10-23-gun-mesh-design.md;
user, 23 Oktober 2026: "use convert 1:1 instead generating decimation style guns", texture "pixelated like decimation",
a cap of 30000 triangles)."""
import math
import os
import re
import struct
import sys
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
TOOLS = os.path.dirname(os.path.dirname(HERE))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.dirname(HERE))
sys.path.insert(0, os.path.join(TOOLS, "guns"))
import convert  # noqa: E402
import meshio  # noqa: E402
import meshpack  # noqa: E402
import synth  # noqa: E402

ROOT = os.path.dirname(TOOLS)
JAR = os.path.exists(os.path.join(ROOT, "Decimation.jar"))


def tris_of(mesh):
    verts, faces = mesh
    return [tuple(tuple(float(c) for c in verts[i]) for i in f) for f in faces]


def area(t):
    a = [t[1][i] - t[0][i] for i in range(3)]
    b = [t[2][i] - t[0][i] for i in range(3)]
    c = (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])
    return 0.5 * math.sqrt(sum(v * v for v in c))


def uv_area(uvs, w, h):
    (a, b), (c, d), (e, f) = [(u * w, v * h) for u, v in uvs]
    return 0.5 * abs((c - a) * (f - b) - (e - a) * (d - b))


def texel_at(built, piece, t, w):
    """The atlas pixel under barycentric point w of triangle t of a piece."""
    uv = built.pieces[piece]["uvs"][t]
    u = sum(w[k] * uv[k][0] for k in range(3))
    v = sum(w[k] * uv[k][1] for k in range(3))
    W, H = built.image.size
    return built.image.getpixel((min(W - 1, int(u * W)), min(H - 1, int(v * H))))


def flat(colour):
    return lambda t, w, p: colour


class IdAndFileTest(unittest.TestCase):
    def test_the_id_is_the_games(self):
        """fixes/gunpack/MeshGun.id: FNV-1a 32 of the name, halves xored to 16 bits (MeshGunTest asserts m4a1 too)."""
        self.assertEqual(meshpack.mesh_id("m4a1"), 33367)
        self.assertEqual(meshpack.mesh_id("ak47"), 48799)
        self.assertEqual(meshpack.mesh_id("akmc"), 41071)

    def test_dmesh_round_trip_in_the_games_layout(self):
        one = [0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0,
               0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0]
        groups = [{"name": "gunModel", "pivot": (1.0, 2.0, 3.0), "verts": one},
                  {"name": "ammoModel", "pivot": (0.5, 0.25, -0.125), "verts": one + one}]
        data = meshpack.write_dmesh(groups)
        self.assertEqual(data[:6], b"DMESH1")
        self.assertEqual(struct.unpack(">i", data[6:10])[0], 2, "big endian group count, as Java's DataInputStream")
        self.assertEqual(struct.unpack(">H", data[10:12])[0], len("gunModel"))
        back = meshpack.read_dmesh(data)
        self.assertEqual([g["name"] for g in back], ["gunModel", "ammoModel"])
        self.assertEqual(tuple(back[1]["pivot"]), (0.5, 0.25, -0.125))
        self.assertEqual(list(back[1]["verts"]), one + one)

    def test_over_the_cap_is_refused(self):
        tri = [0.0] * 24
        with self.assertRaises(ValueError):
            meshpack.write_dmesh([{"name": "gunModel", "pivot": (0, 0, 0), "verts": tri * (meshpack.CAP + 1)}])


class NormalsTest(unittest.TestCase):
    def test_a_box_stays_sharp(self):
        tris = tris_of(synth.box(0, 0, 0, 2, 1, 1))
        for t, ns in zip(tris, meshpack.smooth_normals(tris)):
            f = meshpack.face_normal(t)
            for n in ns:
                self.assertAlmostEqual(sum(a * b for a, b in zip(n, f)), 1.0, places=6)

    def test_a_round_bar_is_smooth_and_its_ends_stay_flat(self):
        tris = tris_of(synth.cylinder_x(0, 4, 0, 0, 1, n=16))       # sides 22.5 degrees apart: under the crease
        for t, ns in zip(tris, meshpack.smooth_normals(tris)):
            for p, n in zip(t, ns):
                if abs(meshpack.face_normal(t)[0]) > 0.9:            # an end cap: still along x
                    self.assertAlmostEqual(abs(n[0]), 1.0, places=6)
                elif abs(p[1]) + abs(p[2]) > 0.5:                     # a side corner: radial
                    r = math.hypot(p[1], p[2])
                    self.assertAlmostEqual(n[0], 0.0, places=6)
                    self.assertAlmostEqual(n[1], p[1] / r, places=6)
                    self.assertAlmostEqual(n[2], p[2] / r, places=6)


class CapTest(unittest.TestCase):
    def test_simplified_to_the_cap_keeping_the_shape(self):
        pieces = [tris_of(synth.cylinder_x(0, 10, 0, 0, 1, n=96)), tris_of(synth.box(2, 1, -0.5, 4, 3, 0.5))]
        total = sum(len(p) for p in pieces)
        out, exact = meshpack.cap_pieces(pieces, 200)
        self.assertFalse(exact)
        self.assertLessEqual(sum(len(p) for p in out), 200)
        self.assertGreaterEqual(sum(len(p) for p in out), 150)
        self.assertEqual(len(out), 2, "pieces stay apart")
        xs = [q[0] for t in out[0] for q in t]
        self.assertAlmostEqual(min(xs), 0.0, places=3)
        self.assertAlmostEqual(max(xs), 10.0, places=3)
        same, exact = meshpack.cap_pieces(pieces, total)
        self.assertTrue(exact, "under the cap nothing changes")
        self.assertEqual(same, pieces)


class TextureTest(unittest.TestCase):
    def test_one_texel_a_quarter_unit_on_every_face(self):
        tris = tris_of(synth.box(0, 0, 0, 4, 2, 1))
        built = meshpack.bake([tris], [flat((128, 128, 128))], tone=None, seed=1)
        W, H = built.image.size
        self.assertEqual((W & (W - 1), H & (H - 1)), (0, 0), "power of two sides")
        for t, uv in zip(tris, built.pieces[0]["uvs"]):
            self.assertAlmostEqual(uv_area(uv, W, H), area(t) / meshpack.TEXEL ** 2, places=3)

    def test_colours_come_from_the_surface_under_each_texel(self):
        tris = tris_of(synth.box(0, 0, 0, 4, 1, 1))
        left_right = lambda t, w, p: (200, 0, 0) if p[0] < 2 else (0, 0, 200)
        built = meshpack.bake([tris], [left_right], tone=None, seed=1, shade=False)
        for t, tri in enumerate(tris):
            if abs(meshpack.face_normal(tri)[1]) > 0.9:               # top and bottom faces span x 0 .. 4
                w = [0.0, 0.0, 0.0]
                k = min(range(3), key=lambda i: tri[i][0])            # the corner nearest x 0
                w[k] = 0.9
                for j in range(3):
                    if j != k:
                        w[j] = 0.05
                c = texel_at(built, 0, t, w)
                self.assertGreater(c[0], c[2], "near x 0: the left colour")

    def test_toned_like_decimation_dark_with_faint_noise(self):
        tris = tris_of(synth.box(0, 0, 0, 8, 2, 1))
        built = meshpack.bake([tris], [flat((235, 235, 235))], tone=convert.tone, seed=3)
        px = [built.image.getpixel((x, y)) for y in range(built.image.size[1]) for x in range(built.image.size[0])]
        px = [p for p in px if p[3] == 255]
        lum = [0.3 * p[0] + 0.59 * p[1] + 0.11 * p[2] for p in px]
        self.assertLess(max(lum), 80, "a white source turns into Decimation's metal greys")
        self.assertGreater(min(lum), 10)
        mean = sum(lum) / len(lum)
        sd = math.sqrt(sum((v - mean) ** 2 for v in lum) / len(lum))
        self.assertGreater(sd, 1.0, "texel noise")
        self.assertLess(sd, 6.0, "faint")

    def test_faces_of_one_direction_at_other_depths_get_their_own_texels(self):
        lo = synth.box(0, 0, 0, 4, 0.1, 4)            # two plates one above the other in one piece
        hi = synth.box(0, 2, 0, 4, 2.1, 4)
        verts = lo[0] + hi[0]
        faces = lo[1] + [tuple(i + len(lo[0]) for i in f) for f in hi[1]]
        tris = tris_of((verts, faces))
        by_height = lambda t, w, p: (200, 0, 0) if p[1] < 1 else (0, 0, 200)
        built = meshpack.bake([tris], [by_height], tone=None, seed=1, shade=False)
        for t, tri in enumerate(tris):
            c = texel_at(built, 0, t, (1 / 3, 1 / 3, 1 / 3))
            if tri[0][1] < 1:
                self.assertGreater(c[0], c[2], "the lower plate keeps its colour")
            else:
                self.assertGreater(c[2], c[0], "the upper plate keeps its colour")


class SkeletonTest(unittest.TestCase):
    def test_one_marker_part_per_group(self):
        header = {"flamePos": (19.8, -4.5, -0.15), "ejectPos": (5.0, -3.0, -0.6), "rhPos": (-6.0, 1.0, -2.0),
                  "rhRot": (0, 0, 0), "lhPos": (-1.5, 9.0, 5.3), "lhRot": (0, 0, 0)}
        groups = [{"name": "gunModel", "pivot": (-2.0, -5.0, -1.0)}, {"name": "ammoModel", "pivot": (4.0, -1.0, -0.6)}]
        text = meshpack.skeleton("ak47", header, groups, (128, 64))
        self.assertIn("flamePos: 19.8, -4.5, -0.15;", text)
        self.assertIn("textureWidth = 128;", text)
        self.assertIn("textureHeight = 64;", text)
        shapes = re.findall(r"(\w+)\.addShape\(([-\d.]+)F, ([-\d.]+)F, ([-\d.]+)F, new float\[\]\[\]\{(.*)\}, 1, 1, 1\);",
                            text)
        self.assertEqual([s[0] for s in shapes], ["gunModel0", "ammoModel0"])
        for k, s in enumerate(shapes):
            self.assertEqual((int(s[1]), int(s[2]), int(s[3])), (48799, k, 7777))
            self.assertEqual(s[4].count("{0F, 0F, 0F}"), 8, "no corner offsets: a plain cube")
        self.assertIn("ammoModel0.setRotationPoint(4F, -1F, -0.6F);", text)


class IconTest(unittest.TestCase):
    def test_a_32_px_side_view_muzzle_right_with_a_dark_outline(self):
        tris = tris_of(synth.box(0, 0, -0.5, 20, 3, 0.5)) + tris_of(synth.box(14, 3, -0.4, 16, 8, 0.4))   # a mag down
        built = meshpack.bake([tris], [flat((60, 60, 60))], tone=None, seed=1)
        img = meshpack.icon([tris], built)
        self.assertEqual(img.size, (32, 32))
        solid = [(x, y) for y in range(32) for x in range(32) if img.getpixel((x, y))[3] == 255]
        xs = [x for x, _ in solid]
        self.assertEqual(max(xs) - min(xs) + 1, 32, "30 px of gun and its outline")
        self.assertEqual(img.getpixel((0, 0))[3], 0, "transparent around it")
        mag = [x for x, y in solid if y > 20]
        self.assertGreater(sum(mag) / len(mag), 16, "the magazine (x 14 .. 16 of 20) right of the middle: muzzle right")
        outline = [img.getpixel(p) for p in solid if max(img.getpixel(p)[:3]) <= 20]
        self.assertGreater(len(outline), 20, "a dark outline")


class OverTheCapTest(unittest.TestCase):
    def test_simplified_texels_take_the_nearest_source_colour(self):
        bar = tris_of(synth.cylinder_x(0, 10, 0, 0, 1, n=96))
        by_x = lambda t, w, p: (200, 0, 0) if sum(q[0] for q in bar[t]) / 3 < 5 else (0, 0, 200)
        built = meshpack.build([bar], [by_x], seed=1, cap=200)
        self.assertFalse(built.stats["exact"])
        self.assertLessEqual(built.stats["triangles"], 200)
        tris = built.pieces[0]["tris"]
        for t, tri in enumerate(tris):
            mx = sum(q[0] for q in tri) / 3
            if abs(meshpack.face_normal(tri)[0]) < 0.5 and (mx < 3 or mx > 7):
                c = texel_at(built, 0, t, (1 / 3, 1 / 3, 1 / 3))
                self.assertGreater(c[0] if mx < 3 else c[2], c[2] if mx < 3 else c[0], "x %.1f" % mx)


def synth_model(mirror_z=False):
    pieces = synth.rotated(synth.synth_gun(), synth.ROTATIONS["identity"], scale=1.0)
    if mirror_z:
        pieces = [(n, synth.transform(mesh, [[1, 0, 0, 0], [0, 1, 0, 0], [0, 0, -1, 0]]), mat, col)
                  for n, mesh, mat, col in pieces]
    return meshio.read_bytes(synth.glb_of(pieces).bytes(), "synthgun.glb")


@unittest.skipUnless(JAR, "needs Decimation.jar for the like gun")
class MeshConversionTest(unittest.TestCase):
    def test_every_source_triangle_kept_placed_and_grouped(self):
        model = synth_model()
        conv = convert.convert_mesh(model, name="synthmesh", like="akm", length=28.0)
        st = conv.stats
        self.assertEqual(st["mode"], "mesh")
        self.assertEqual(st["triangles"], model.triangle_count())
        self.assertTrue({"gunModel", "ammoModel", "slideModel"} <= set(st["groups"]), st["groups"])
        xs = [q[0] for p in conv.mesh.pieces for t in p["tris"] for q in t]
        self.assertAlmostEqual(max(xs) - min(xs), 28.0, delta=0.1)
        self.assertEqual(set(conv.points), {"flame", "railTop", "rail", "gripRail", "mount"})
        self.assertEqual(conv.parts, [], "no boxes")
        j = conv.to_json()
        self.assertEqual(len(j["pieces"]), len(conv.mesh.pieces))
        self.assertTrue(j["mesh"]["texture"].startswith("data:image/png;base64,"))

    def test_the_package_holds_the_mesh_its_skeleton_and_texture(self):
        import base64
        import io
        import json
        import tempfile
        import package
        from PIL import Image
        conv = convert.convert_mesh(synth_model(), name="synthmesh", like="akm", length=28.0)
        conv.source = {"file": "synthgun.glb", "author": "gunconv tests", "license": "own"}
        res = package.export(conv, tempfile.mkdtemp())
        d, cat = res["dir"], conv.category
        self.assertIn("mesh.dmesh", res["files"])
        for f in ("assets/deci/models/guns/%s/synthmesh.bmodel" % cat,
                  "assets/deci/textures/model/guns/%s/synthmesh/synthmesh.png" % cat,
                  "assets/deci/textures/items/gun/%s/synthmesh.png" % cat,
                  "assets/deci/animations/synthmesh/synthmeshFire.anib",
                  "assets/deci/animations/synthmesh/synthmeshReload1.anib",
                  "assets/deci/animations/synthmesh/synthmeshRack.anib",
                  "assets/deci/animations/synthmesh/synthmeshSlideBack.anib", "gun.json"):
            self.assertIn(f, res["files"])
        with open(os.path.join(d, "gun.json")) as f:
            gj = json.load(f)
        self.assertEqual(gj["mesh"], "mesh.dmesh")
        self.assertEqual(gj["converter"]["mode"], "mesh")
        with open(os.path.join(d, "mesh.dmesh"), "rb") as f:
            groups = meshpack.read_dmesh(f.read())
        self.assertEqual(sum(len(g["verts"]) // 24 for g in groups), conv.stats["triangles"])
        uv = [v for g in groups for i, v in enumerate(g["verts"]) if i % 8 >= 6]
        self.assertTrue(0.0 <= min(uv) and max(uv) <= 1.0)
        with open(os.path.join(d, "assets/deci/models/guns/%s/synthmesh.bmodel" % cat)) as f:
            text = f.read()
        shapes = re.findall(r"(\w+)0\.addShape\((\d+)F, (\d+)F, 7777F", text)
        self.assertEqual([(n, int(i), int(k)) for n, i, k in shapes],
                         [(g["name"], meshpack.mesh_id("synthmesh"), k) for k, g in enumerate(groups)])
        with Image.open(os.path.join(d, "assets/deci/textures/model/guns/%s/synthmesh/synthmesh.png" % cat)) as tex:
            self.assertIn("textureWidth = %d;" % tex.size[0], text)
            self.assertIn("textureHeight = %d;" % tex.size[1], text)
        with Image.open(os.path.join(d, "assets/deci/textures/items/gun/%s/synthmesh.png" % cat)) as ic:
            self.assertEqual(ic.size, (32, 32))
        with open(os.path.join(d, "assets/deci/animations/synthmesh/synthmeshFire.anib")) as f:
            self.assertIn("slideModel0", f.read(), "the bolt kicks in Fire")

    def test_grey_pieces_take_the_like_guns_metal(self):
        """Decimation's akm is near black metal (10 .. 46): a grey piece covering most of the source reads as its common
        metal, as Exact mode's palette_tones gives it (match.palette), not tone()'s generic band (24 .. 64)."""
        import match
        conv = convert.convert_mesh(synth_model(), name="synthmesh", like="akm", length=28.0)
        greys = match.palette("akm")["greyW"]
        total, acc, common = sum(w for _, w in greys), 0.0, None
        for v, w in greys:
            acc += w
            if common is None and acc >= total / 2:
                common = v
        k = [p["label"] for p in conv.pieces].index("receiver")
        built = conv.mesh
        W, H = built.image.size
        lums = []
        for uv in built.pieces[k]["uvs"]:
            u, v = sum(q[0] for q in uv) / 3, sum(q[1] for q in uv) / 3
            c = built.image.getpixel((min(W - 1, int(u * W)), min(H - 1, int(v * H))))
            lums.append(match.lum(c))
        mean = sum(lums) / len(lums)
        self.assertAlmostEqual(mean, common, delta=10, msg="receiver %.1f, the akm's common metal %.1f" % (mean, common))

    def test_a_mirrored_source_still_faces_out(self):
        conv = convert.convert_mesh(synth_model(mirror_z=True), name="synthmesh", like="akm", length=28.0)
        for p in conv.mesh.pieces:
            if p["tris"]:
                self.assertGreater(meshpack.signed_volume(p["tris"]), 0.0)


if __name__ == "__main__":
    unittest.main()

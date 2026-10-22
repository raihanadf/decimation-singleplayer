"""Exact mode (spec: "Exact mode"): the source mesh 1:1 as thin plates, prepared by welding, simplifying to a face
budget, merging coplanar triangles into quads and dropping faces too small to see."""
import math
import os
import sys
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
TOOLS = os.path.dirname(os.path.dirname(HERE))
ROOT = os.path.dirname(TOOLS)
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.dirname(HERE))
sys.path.insert(0, os.path.join(TOOLS, "guns"))
import exact  # noqa: E402
import fit  # noqa: E402
import study  # noqa: E402
import synth  # noqa: E402

JAR = os.path.exists(os.path.join(ROOT, "Decimation.jar"))


def tris_of(mesh):
    verts, faces = mesh
    return [tuple(verts[i] for i in f) for f in faces]


def grid_box(x0, y0, z0, x1, y1, z1, n):
    """A box whose every side is an n x n grid of quads (2 n^2 triangles a side), wound outward."""
    tris = []
    c = ((x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2)
    lo, hi = (x0, y0, z0), (x1, y1, z1)
    for axis in range(3):
        b, d = [i for i in range(3) if i != axis]
        for side in (lo[axis], hi[axis]):
            for i in range(n):
                for j in range(n):
                    def pt(ii, jj):
                        p = [0.0, 0.0, 0.0]
                        p[axis] = side
                        p[b] = lo[b] + (hi[b] - lo[b]) * ii / n
                        p[d] = lo[d] + (hi[d] - lo[d]) * jj / n
                        return tuple(p)
                    for t in ((pt(i, j), pt(i + 1, j), pt(i + 1, j + 1)), (pt(i, j), pt(i + 1, j + 1), pt(i, j + 1))):
                        n_ = fit.cross(fit.sub(t[1], t[0]), fit.sub(t[2], t[0]))
                        m = [sum(q[k] for q in t) / 3 for k in range(3)]
                        tris.append(t if fit.dot(n_, fit.sub(m, c)) > 0 else (t[0], t[2], t[1]))
    return tris


def normal(poly):
    n = fit.cross(fit.sub(poly[1], poly[0]), fit.sub(poly[2], poly[0]))
    ln = math.sqrt(fit.dot(n, n)) or 1.0
    return tuple(v / ln for v in n)


class SimplifyTest(unittest.TestCase):
    def test_a_dense_box_keeps_its_corners(self):
        tris = grid_box(0, 0, 0, 4, 2, 1, 10)                       # 1,200 triangles
        polys = exact.prepare(tris, budget=24)
        self.assertLessEqual(len(polys), 24)
        pts = [q for p in polys for q in p]
        for i, (lo, hi) in enumerate(((0, 4), (0, 2), (0, 1))):
            self.assertAlmostEqual(min(q[i] for q in pts), lo, delta=1e-6)
            self.assertAlmostEqual(max(q[i] for q in pts), hi, delta=1e-6)
        for q in pts:                                              # still on the box's surface
            self.assertTrue(any(abs(q[i] - v) < 1e-6 for i, v in enumerate((0, 0, 0))) or
                            any(abs(q[i] - v) < 1e-6 for i, v in enumerate((4, 2, 1))), q)

    def test_no_face_flips(self):
        tris = grid_box(0, 0, 0, 4, 2, 1, 8)
        c = (2, 1, 0.5)
        for p in exact.prepare(tris, budget=40):
            m = [sum(q[k] for q in p) / len(p) for k in range(3)]
            self.assertGreater(fit.dot(normal(p), fit.sub(m, c)), 0)

    def test_a_round_bar_keeps_its_silhouette(self):
        verts, faces = synth.cylinder_x(0, 10, 0, 0, 0.5, n=48)
        polys = exact.prepare(tris_of((verts, faces)), budget=60)
        self.assertLessEqual(len(polys), 60)
        pts = [q for p in polys for q in p]
        self.assertAlmostEqual(max(q[0] for q in pts), 10, delta=1e-3)          # a thousandth of a unit: 0.03 mm
        self.assertAlmostEqual(min(q[0] for q in pts), 0, delta=1e-3)
        self.assertGreater(max(q[1] for q in pts), 0.45)            # still round, not collapsed to a line

    def test_under_budget_stays_exact(self):
        tris = grid_box(0, 0, 0, 4, 2, 1, 1)                        # 12 triangles
        polys = exact.prepare(tris, budget=800)
        self.assertEqual(len(polys), 6)                             # merged into the box's 6 quads, nothing lost


class QuadTest(unittest.TestCase):
    def test_box_triangles_become_six_quads(self):
        polys = exact.prepare(grid_box(0, 0, 0, 2, 1, 1, 1), budget=100)
        self.assertEqual(sorted(len(p) for p in polys), [4] * 6)

    def test_bent_pairs_stay_triangles(self):
        a, b, c, d = (0, 0, 0), (1, 0, 0), (1, 1, 0), (0, 1, 0.5)   # d lifted: the two triangles are not coplanar
        polys = exact.prepare([(a, b, c), (a, c, d)], budget=100, min_side=0.0)
        self.assertEqual(sorted(len(p) for p in polys), [3, 3])

    def test_tiny_faces_dropped(self):
        big = [((0, 0, 0), (1, 0, 0), (1, 1, 0))]
        tiny = [((5, 5, 5), (5.02, 5, 5), (5, 5.02, 5))]
        polys = exact.prepare(big + tiny, budget=100)
        self.assertEqual(len(polys), 1)


class PlateTest(unittest.TestCase):
    def test_a_quad_on_both_sides(self):
        quad = [(0, 0, 0), (2, 0, 0), (2, 1, 0), (0, 1, 0)]          # counter clockwise around +z
        c = exact.plate(quad)
        for k, (ix, iy) in zip(((0, 0), (1, 0), (1, 1), (0, 1)), ((0, 0), (1, 0), (1, 1), (0, 1))):
            self.assertAlmostEqual(c[(ix, iy, 0)][2], -exact.THICK / 2, delta=1e-9)
            self.assertAlmostEqual(c[(ix, iy, 1)][2], exact.THICK / 2, delta=1e-9)
        for i, q in enumerate(quad):
            k = ((0, 0), (1, 0), (1, 1), (0, 1))[i]
            self.assertAlmostEqual(c[k + (1,)][0], q[0], delta=1e-9)
            self.assertAlmostEqual(c[k + (1,)][1], q[1], delta=1e-9)

    def test_right_handed_so_each_side_faces_out(self):
        quad = [(0, 0, 0), (0, 1, 0), (2, 1, 0), (2, 0, 0)]          # clockwise around +z: the face looks down -z
        c = exact.plate(quad)
        ex, ey, ez = (fit.sub(c[(1, 0, 0)], c[(0, 0, 0)]), fit.sub(c[(0, 1, 0)], c[(0, 0, 0)]),
                      fit.sub(c[(0, 0, 1)], c[(0, 0, 0)]))
        self.assertGreater(fit.dot(ex, fit.cross(ey, ez)), 0)       # a proper box, not inside out
        self.assertLess(c[(0, 0, 1)][2], c[(0, 0, 0)][2])           # its iz = 1 side is the face's own side (-z)
        planes = study.planes_of(study.verts_of_corners(c))
        self.assertEqual(len(planes), 6)

    def test_a_triangle_is_a_quad_with_two_corners_on_one_point(self):
        c = exact.plate([(0, 0, 0), (1, 0, 0), (0, 1, 0)])
        self.assertEqual(c[(1, 1, 1)], c[(0, 1, 1)])
        self.assertEqual(c[(1, 1, 0)], c[(0, 1, 0)])


class RenderTest(unittest.TestCase):
    def test_a_triangle_plate_renders_its_texture(self):
        """study.py draws a triangle plate (a quad with two corners on one point) with its island's texels, not the
        background (the first exact sheets showed such faces white)."""
        import gunmodel
        from PIL import Image
        q = fit.new_part("t", "gunModel", (120, 40, 40), exact.plate([(0, 0, 0), (4, 0, 0), (0, 3, 0)]))
        text = gunmodel.bmodel({}, [q])
        rows = gunmodel.paint([q])
        img = Image.new("RGBA", (len(rows[0]), len(rows)))
        img.putdata([px for row in rows for px in row])
        g = study.Gun("tri", "rifle", text, img, True)
        im = study.render(g, "side", 20, pad=2, bg=(0, 0, 255, 255))
        inside = im.getpixel((im.width // 4, im.height // 3))               # well inside the triangle
        self.assertGreater(inside[0], 60, inside)                            # the part's red, not the blue background


class EstimateTest(unittest.TestCase):
    def test_cost_grows_with_parts(self):
        a, b = exact.estimate(200), exact.estimate(800)
        self.assertGreater(b["drawMs"], a["drawMs"])
        self.assertGreater(b["ratio"], a["ratio"])
        self.assertIn(exact.verdict(b["ratio"]), ("ok", "warn", "refuse"))
        self.assertEqual(exact.verdict(1.0), "ok")
        self.assertEqual(exact.verdict(exact.BUDGET * 1.2), "warn")
        self.assertEqual(exact.verdict(exact.BUDGET * 2.1), "refuse")


@unittest.skipUnless(JAR, "needs Decimation.jar for the like gun")
class ConvertTest(unittest.TestCase):
    def test_exact_conversion_of_a_synthetic_rifle(self):
        import convert
        import meshio
        g = synth.glb_of(synth.rotated(synth.synth_gun(), synth.ROTATIONS["identity"], scale=1.0))
        conv = convert.convert_exact(meshio.read_bytes(g.bytes(), "synthgun.glb"), name="synthexact", like="akm",
                                     length=28.0, budget=300)
        st = conv.stats
        self.assertEqual(st["mode"], "exact")
        self.assertLessEqual(st["faces"], 300)
        self.assertEqual(st["faces"], len(conv.parts))
        for p in conv.parts:                                        # every part a thin plate
            pts = list(p.c.values())
            ext = sorted(max(q[i] for q in pts) - min(q[i] for q in pts) for i in range(3))
            self.assertLessEqual(min(math.dist(p.c[k[:2] + (0,)], p.c[k[:2] + (1,)]) for k in fit.CORNERS
                                     if k[2] == 0), exact.THICK + 1e-6)
            self.assertGreater(ext[2], 0.0)
        groups = {p.group for p in conv.parts}
        self.assertTrue({"ammoModel", "slideModel"} <= groups, groups)
        (x0, x1), _, _ = fit.bounds(conv.parts)
        self.assertAlmostEqual(x1 - x0, 28.0, delta=0.1)
        self.assertEqual(set(conv.points), {"flame", "railTop", "rail", "gripRail", "mount"})
        self.assertIn(st["cost"]["verdict"], ("ok", "warn", "refuse"))

    def test_the_lamoot_sample_stays_within_a_small_budget(self):
        sample = os.path.join(ROOT, "build", "gunconv", "inputs", "lamoot_ak47", "highpoly_ak47.obj")
        if not os.path.exists(sample):
            self.skipTest("needs the git ignored sample")
        import convert
        conv = convert.convert_exact(sample, name="lamootexact", like="akm", budget=300)
        self.assertLessEqual(conv.stats["faces"], 300)                    # 354 before the shares were redone

    def test_many_pieces_stay_within_the_budget(self):
        """Every piece gets at least a couple of faces: with many small pieces (a one object file cut into its
        connected parts) the total must still stay within the budget (ak47 exact had 870 for 800)."""
        import convert
        import meshio
        pieces = [("p%d" % i, synth.box(i * 0.5, 0, 0, i * 0.5 + 0.4, 1 + (i % 3) * 0.2, 0.4), "Steel", (60, 60, 62))
                  for i in range(150)]
        g = synth.glb_of(pieces)
        conv = convert.convert_exact(meshio.read_bytes(g.bytes(), "many.glb"), name="manyexact", like="akm",
                                     length=28.0, budget=300)
        self.assertLessEqual(conv.stats["faces"], 300)

    def test_export_refuses_a_gun_far_over_the_cost_budget(self):
        import convert
        import meshio
        import package
        import tempfile
        g = synth.glb_of(synth.rotated(synth.synth_gun(), synth.ROTATIONS["identity"], scale=1.0))
        conv = convert.convert_exact(meshio.read_bytes(g.bytes(), "synthgun.glb"), name="synthexact", like="akm",
                                     length=28.0, budget=300)
        conv.parts = conv.parts * (1200 // len(conv.parts) + 1)   # over 1,140 parts: twice the cost budget
        conv.source = {"file": "synthgun.glb", "author": "test", "license": "CC0"}
        out = tempfile.mkdtemp()
        with self.assertRaises(ValueError):
            package.export(conv, out)
        res = package.export(conv, out, force=True)
        self.assertTrue(os.path.isdir(res["dir"]))


if __name__ == "__main__":
    unittest.main()

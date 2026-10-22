"""Geometry: axes and orientation, mesh pieces, cross sections, fitting pieces with Decimation's shape kinds."""
import os
import sys
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.dirname(HERE))
sys.path.insert(0, os.path.join(os.path.dirname(os.path.dirname(HERE)), "guns"))
import fit  # noqa: E402
import gunmodel  # noqa: E402
import study  # noqa: E402
import synth  # noqa: E402


def tris_of(mesh):
    v, f = mesh
    return [(v[a], v[b], v[c]) for a, b, c in f]


def apply(rot, p):
    return tuple(sum(rot[r][i] * p[i] for i in range(3)) for r in range(3))


def part_bounds(parts):
    pts = [q for p in parts for q in p.points()]
    return [(min(q[i] for q in pts), max(q[i] for q in pts)) for i in range(3)]


class OrientTest(unittest.TestCase):
    def check(self, rot_name):
        pieces = synth.rotated(synth.synth_gun(), synth.ROTATIONS[rot_name], scale=0.037, offset=(3, -2, 7))
        tris = [t for _, mesh, _, _ in pieces for t in tris_of(mesh)]
        f, u = fit.orient(tris)
        r = fit.rotation(f, u)
        by = {n: [apply(r, v) for v in mesh[0]] for n, mesh, _, _ in pieces}
        cx = lambda name, i: sum(v[i] for v in by[name]) / len(by[name])  # noqa: E731
        # muzzle (barrel) toward +x, stock toward -x
        self.assertGreater(cx("barrel", 0), cx("stock", 0), rot_name)
        # magazine and grip hang DOWN: bmodel y grows downward
        self.assertGreater(cx("Magazine", 1), cx("receiver", 1), rot_name)
        self.assertGreater(cx("grip", 1), cx("receiver", 1), rot_name)
        # the charging handle stays on the gun's right side: -z in Decimation's model space (no mirror)
        self.assertLess(cx("Bolt", 2), cx("receiver", 2), rot_name)

    def test_identity(self):
        self.check("identity")

    def test_muzzle_minus_x(self):
        self.check("muzzle -x")

    def test_muzzle_minus_z(self):
        self.check("muzzle -z, up +y")

    def test_standing_on_its_butt(self):
        self.check("muzzle +y, up -z")

    def test_overrides(self):
        tris = [t for _, mesh, _, _ in synth.synth_gun() for t in tris_of(mesh)]
        f, u = fit.orient(tris, muzzle="-x", up="-y")
        self.assertEqual((tuple(f), tuple(u)), ((-1, 0, 0), (0, -1, 0)))

    def test_rotation_is_proper(self):
        for f, u in (((1, 0, 0), (0, 1, 0)), ((0, 0, -1), (1, 0, 0)), ((0, 1, 0), (0, 0, -1))):
            r = fit.rotation(f, u)
            det = (r[0][0] * (r[1][1] * r[2][2] - r[1][2] * r[2][1]) - r[0][1] * (r[1][0] * r[2][2] - r[1][2] * r[2][0])
                   + r[0][2] * (r[1][0] * r[2][1] - r[1][1] * r[2][0]))
            self.assertAlmostEqual(det, 1.0)


class ComponentsTest(unittest.TestCase):
    def test_split_and_weld(self):
        a = synth.box(0, 0, 0, 1, 1, 1)
        # two halves of one box with their own (duplicated) vertices: welded by position into one piece
        half1 = synth.box(5, 0, 0, 6, 1, 1)
        half2 = synth.box(6, 0, 0, 7, 1, 1)
        verts, faces = synth.merge(a, half1, half2)
        comps = fit.components(verts, faces)
        self.assertEqual(sorted(len(c) for c in comps), [12, 24])


class CrossSectionTest(unittest.TestCase):
    def test_box_and_cylinder(self):
        cs = fit.cross_section(tris_of(synth.box(0, 0, 0, 4, 2, 1)), 0, 2.0)
        self.assertEqual([round(c, 6) for c in cs[:4]], [0, 2, 0, 1])
        self.assertAlmostEqual(cs[4], 2.0, places=5)
        cyl = tris_of(synth.cylinder_x(0, 6, 0, 0, 0.5))
        cs = fit.cross_section(cyl, 0, 3.0)
        self.assertAlmostEqual(cs[1] - cs[0], 1.0, places=5)
        self.assertAlmostEqual(cs[4], 8 * 0.25 * 0.38268343, places=4)   # a 16 sided polygon of radius 0.5
        self.assertTrue(fit.is_round(cyl, 0))
        self.assertFalse(fit.is_round(tris_of(synth.box(0, 0, 0, 6, 1, 1)), 0))


class FitTest(unittest.TestCase):
    def fit1(self, mesh, **kw):
        return fit.fit_piece(tris_of(mesh), "p", "gunModel", (40, 40, 40), **kw)

    def test_box_is_one_cuboid(self):
        parts = self.fit1(synth.box(0, 0, 0, 4, 1, 0.5))
        self.assertEqual(len(parts), 1)
        self.assertEqual(fit.kind(parts[0]), "cuboid")
        b = part_bounds(parts)
        self.assertEqual([tuple(round(c, 5) for c in ab) for ab in b], [(0, 4), (0, 1), (0, 0.5)])

    def test_frustum_is_a_taper(self):
        parts = self.fit1(synth.frustum_x(0, 4, (0, 2, 0, 1), (0.5, 1.5, 0.25, 0.75)))
        self.assertEqual(len(parts), 1)
        self.assertEqual(fit.kind(parts[0]), "taper")
        c = parts[0].c
        self.assertAlmostEqual(c[(1, 1, 1)][1], 1.5, places=2)
        self.assertAlmostEqual(c[(1, 0, 0)][2], 0.25, places=2)

    def test_sheared_prism_is_a_skew(self):
        parts = self.fit1(synth.frustum_x(0, 4, (0, 1, 0, 1), (1, 2, 0, 1)))
        self.assertEqual(len(parts), 1)
        self.assertEqual(fit.kind(parts[0]), "skew")

    def test_curved_magazine_becomes_segments(self):
        mag = synth.arc_tube(0, 0, 6.0, 50, 0.6, 0.3)
        parts = self.fit1(mag, tol=0.08)
        self.assertGreaterEqual(len(parts), 3)
        self.assertLessEqual(len(parts), 12)
        kinds = {fit.kind(p) for p in parts}
        self.assertTrue(kinds <= {"skew", "taper", "cuboid"}, kinds)   # Decimation's akm: tapered slices
        # Decimation's curved magazine: each lower segment further forward (the tube runs down -y, curving to +x)
        cen = sorted((sum(q[1] for q in p.points()) / 8, sum(q[0] for q in p.points()) / 8) for p in parts)
        self.assertEqual([c[1] for c in cen], sorted([c[1] for c in cen], reverse=True))
        # every source vertex is covered by some part grown by the fit tolerance (0.08) plus the residual of the
        # shortest segments (fit.MIN_SEG stops the cuts at the tilted bottom's corner)
        shapes = [fit.planes(p) for p in parts]
        for v in mag[0]:
            self.assertTrue(any(fit.inside(s, v, 0.15) for s in shapes), v)

    def test_chamfered_tips_do_not_spoil_the_fit(self):
        """A raked grip whose top and bottom are bevelled to thin ridges (the AKM sample's grip and magazine): the
        tips must not pull the fit; the rake (bottom 0.8 further back over 3 units) must show."""
        grip = synth.chamfered_raked(height=3.0, depth=1.1, width=0.8, rake=0.8, bevel=0.25)
        parts = self.fit1(grip)
        corners = [q for p in parts for q in p.points()]
        x_top = min(q[0] for q in corners if q[1] < 0.2)                 # the front of the grip at its top face
        x_bot = min(q[0] for q in corners if q[1] > 2.8)                 # and at its bottom face
        self.assertAlmostEqual(x_bot - x_top, -0.8, delta=0.2)          # raked back toward the bottom
        widths = [max(q[0] for q in p.points()) - min(q[0] for q in p.points()) for p in parts]
        self.assertGreater(min(widths), 0.8)                             # no sliver parts from the tips

    def test_panels_keep_the_shape(self):
        """A long taper sliced into panels: same outline, every panel a slice of the original loft."""
        long = self.fit1(synth.frustum_x(0, 6, (0, 2, 0, 1), (0.5, 1.5, 0.25, 0.75)))[0]
        panels = fit.panels(long, 1.6)
        self.assertEqual(len(panels), 4)
        self.assertEqual([tuple(round(v, 6) for v in ab) for ab in fit.bounds(panels)],
                         [tuple(round(v, 6) for v in ab) for ab in fit.bounds([long])])
        # the panel ends meet: each panel's front face is the next one's rear face
        for a, b in zip(panels, panels[1:]):
            for iy in (0, 1):
                for iz in (0, 1):
                    self.assertEqual([round(v, 6) for v in a.c[(1, iy, iz)]], [round(v, 6) for v in b.c[(0, iy, iz)]])
        self.assertEqual(fit.panels(self.fit1(synth.box(0, 0, 0, 1.5, 1, 1))[0], 1.6)[0].name, "p")

    def test_flat_loop_becomes_bars(self):
        """A trigger guard like U (one welded, flat piece): bars along its outline, the hole left open."""
        u = synth.merge(synth.box(0, 0, 0, 3, 0.2, 0.15), synth.box(0, 0.2, 0, 0.2, 1.2, 0.15),
                        synth.box(2.8, 0.2, 0, 3.0, 1.2, 0.15))
        parts = self.fit1(u)
        self.assertLessEqual(len(parts), 6)
        shapes = [fit.planes(p) for p in parts]
        self.assertFalse(any(fit.inside(s, (1.5, 0.8, 0.07), 0.0) for s in shapes))      # the loop's opening
        for q in ((1.5, 0.1, 0.07), (0.1, 0.9, 0.07), (2.9, 0.9, 0.07)):                  # each bar is there
            self.assertTrue(any(fit.inside(s, q, 0.01) for s in shapes), q)
        self.assertGreaterEqual(fit.side_iou(parts, [(u[0][a], u[0][b], u[0][c]) for a, b, c in u[1]]), 0.85)

    def test_thin_post_at_the_end_keeps_its_shape(self):
        """The AKM sample's trigger guard: a 0.08 thin rear post, a bottom bar, a solid front block, one welded flat
        piece. The post must stay a post (it became a wedge spanning post and bar while cuts kept 0.3 from the ends)."""
        g = synth.merge(synth.box(0, 0, 0, 0.08, 1.0, 0.15), synth.box(0.08, 0.85, 0, 2.2, 1.0, 0.15),
                        synth.box(2.2, 0.0, 0, 2.8, 1.0, 0.15))
        parts = self.fit1(g)
        shapes = [fit.planes(p) for p in parts]
        for q in ((0.04, 0.1, 0.07), (0.04, 0.5, 0.07), (1.1, 0.92, 0.07), (2.5, 0.2, 0.07)):   # post, bar, block
            self.assertTrue(any(fit.inside(s, q, 0.01) for s in shapes), q)
        for q in ((0.3, 0.3, 0.07), (1.1, 0.5, 0.07), (2.0, 0.3, 0.07)):                          # the opening
            self.assertFalse(any(fit.inside(s, q, 0.0) for s in shapes), q)

    def test_top_plate_layer(self):
        """Decimation's layered receivers: a bulky part becomes a core and a top plate with bevelled long edges."""
        core = self.fit1(synth.box(0, -4.5, -0.65, 1.5, -3.5, 0.35))[0]     # 1.0 tall: plate and one core
        parts = fit.layer_top(core)
        self.assertEqual(len(parts), 2)
        plate, rest = sorted(parts, key=lambda p: min(q[1] for q in p.points()))
        self.assertEqual([tuple(round(v, 6) for v in ab) for ab in fit.bounds(parts)],
                         [tuple(round(v, 6) for v in ab) for ab in fit.bounds([core])])
        self.assertAlmostEqual(fit.bounds([plate])[1][1] - fit.bounds([plate])[1][0], fit.PLATE, places=6)
        self.assertEqual(fit.kind(plate), "taper")                       # its top face narrower: the bevel
        top = [plate.c[k] for k in fit.CORNERS if k[1] == 0]
        self.assertAlmostEqual(max(q[2] for q in top) - min(q[2] for q in top), 1.0 - 2 * fit.BEVEL, places=6)
        self.assertEqual(fit.kind(rest), "cuboid")
        self.assertEqual(fit.layer_top(self.fit1(synth.box(0, 0, 0, 1.5, 0.3, 1))[0])[0].name, "p")   # too thin

    def test_snap_part_turns_tiny_tapers_into_boxes(self):
        p = self.fit1(synth.frustum_x(0, 1.4, (0, 1.0, 0, 1.0), (0.01, 0.99, 0.0, 1.0)))[0]
        self.assertEqual(fit.kind(fit.snap_part(p)), "cuboid")
        q = self.fit1(synth.frustum_x(0, 1.4, (0, 1.0, 0, 1.0), (0.3, 0.7, 0.0, 1.0)))[0]
        self.assertEqual(fit.kind(fit.snap_part(q)), "taper")

    def test_round_bar_becomes_an_octagon(self):
        parts = self.fit1(synth.cylinder_x(0, 6, 1.0, -0.15, 0.5))
        self.assertEqual(len(parts), 3)
        self.assertEqual(sorted(p.name[-3:] for p in parts), ["Bot", "Mid", "Top"])
        b = part_bounds(parts)
        self.assertAlmostEqual(b[1][1] - b[1][0], 1.0, delta=0.05)
        self.assertAlmostEqual(b[2][1] - b[2][0], 1.0, delta=0.05)
        mid = [p for p in parts if p.name.endswith("Mid")][0]
        mb = part_bounds([mid])
        self.assertAlmostEqual(mb[1][1] - mb[1][0], 2 * 0.42 * 0.5, delta=0.02)

    def test_kind_matches_study(self):
        """fit.kind on absolute corners agrees with study.shape_kind after a .bmodel round trip."""
        parts = []
        parts += self.fit1(synth.box(0, 0, 0, 4, 1, 0.5))
        parts += self.fit1(synth.frustum_x(0, 4, (0, 2, 0, 1), (0.5, 1.5, 0.25, 0.75)))
        parts += self.fit1(synth.frustum_x(0, 4, (0, 1, 0, 1), (1, 2, 0, 1)))
        w = gunmodel.part("w", "gunModel", (40, 40, 40), (0, 0, 0), (2, 1, 1)).inset("y", 0, x=(1, 1))
        parts.append(w)
        text = gunmodel.bmodel({}, parts)
        g = study.Gun("t", "rifle", text, None, True)
        self.assertEqual([study.shape_kind(p) for p in g.parts], [fit.kind(p) for p in parts])
        self.assertEqual(fit.kind(w), "wedge")

    def test_tiny_piece_is_kept_thin(self):
        parts = self.fit1(synth.box(0, 0, 0, 0.5, 0.02, 0.3))
        self.assertEqual(len(parts), 1)
        b = part_bounds(parts)
        self.assertGreaterEqual(b[1][1] - b[1][0], fit.MIN_THICK - 1e-9)


if __name__ == "__main__":
    unittest.main()

"""Exemplar areas: choosing an exemplar, refitting it along the source's profile, the converter's report."""
import os
import sys
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
TOOLS = os.path.dirname(os.path.dirname(HERE))
ROOT = os.path.dirname(TOOLS)
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.dirname(HERE))
sys.path.insert(0, os.path.join(TOOLS, "guns"))
import exemplar  # noqa: E402
import fit  # noqa: E402

JAR = os.path.exists(os.path.join(ROOT, "Decimation.jar"))


def box_corners(a, b):
    return {k: [b[i] if k[i] else a[i] for i in range(3)] for k in fit.CORNERS}


def straight_mag():
    """An exemplar magazine: three slices stacked along y, straight."""
    parts = [box_corners((0, y, -0.3), (1, y + 1, 0.3)) for y in (0, 1, 2)]
    return exemplar.make_area(parts, [(60, 40, 30)] * 3, ["ammoModel"] * 3)


class RefitTest(unittest.TestCase):
    def test_slices_follow_a_curved_target(self):
        ex = straight_mag()
        # the target: a magazine curving forward, 1 deep at every height, its front edge x = 0.6 * y
        # (a staircase of 0.25 tall boxes standing for the curve)
        target = [box_corners((0.6 * y, y, -0.3), (0.6 * y + 1, y + 0.25, 0.3)) for y in [0.25 * i for i in range(12)]]
        area = exemplar.target_area(target)
        out = exemplar.refit(ex, area)
        self.assertEqual(len(out), 3)
        for c in out:
            ys = [q[1] for q in c.values()]
            xs = [q[0] for q in c.values()]
            ym, xm = sum(ys) / 8, sum(xs) / 8
            self.assertAlmostEqual(xm, 0.6 * ym + 0.5, delta=0.2)    # the slice sits on the curve at its height
        self.assertGreaterEqual(exemplar.overlap(out, target), 0.8)

    def test_profile_keeps_a_box(self):
        ex = straight_mag()
        area = exemplar.target_area([box_corners((5, 0, 0), (7, 6, 1))])
        out = exemplar.refit(ex, area)
        b = fit.bounds([fit.new_part("p", "gunModel", (0, 0, 0), c) for c in out])
        self.assertEqual([tuple(round(v, 6) for v in ab) for ab in b], [(5, 7), (0, 6), (0, 1)])


class SelectTest(unittest.TestCase):
    def test_like_gun_first_unless_clearly_worse(self):
        scores = [("other", 0.80), ("akm", 0.75), ("third", 0.5)]
        self.assertEqual(exemplar.pick(scores, "akm")[0], "akm")
        scores = [("other", 0.90), ("akm", 0.75)]
        self.assertEqual(exemplar.pick(scores, "akm")[0], "other")
        self.assertEqual(exemplar.pick([("x", 0.6)], None)[0], "x")


@unittest.skipUnless(JAR, "needs Decimation.jar")
class ConvertTest(unittest.TestCase):
    def test_report_says_where_parts_came_from(self):
        import convert
        import meshio
        import synth
        g = synth.glb_of(synth.rotated(synth.synth_gun(), synth.ROTATIONS["identity"], scale=1.0))
        conv = convert.convert(meshio.read_bytes(g.bytes(), "synthgun.glb"), name="synthgun", like="akm", length=28.0)
        rep = conv.stats["exemplars"]
        self.assertTrue(rep)
        from_ex = [p for p in conv.parts if getattr(p, "exemplar", None)]
        self.assertGreater(len(from_ex), 0)
        self.assertEqual(sum(conv.stats["byGun"].values()), len(from_ex))
        for area, r in rep.items():
            self.assertTrue("gun" in r or "fit" in r, (area, r))
        self.assertEqual(conv.stats["fromExemplars"] + conv.stats["fromFit"], len(conv.parts))
        groups = {p.group for p in conv.parts}
        self.assertTrue({"ammoModel", "slideModel"} <= groups, groups)        # magazine and bolt keep their groups


if __name__ == "__main__":
    unittest.main()

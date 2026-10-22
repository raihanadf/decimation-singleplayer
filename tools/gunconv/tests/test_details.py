"""Details (spec, milestone 3): the rule that finds Decimation's small parts on a host, the dataset's records of them,
and the detail layer that places them on a converted gun's areas."""
import os
import sys
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
TOOLS = os.path.dirname(os.path.dirname(HERE))
ROOT = os.path.dirname(TOOLS)
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.dirname(HERE))
sys.path.insert(0, os.path.join(TOOLS, "guns"))
import details  # noqa: E402
import fit  # noqa: E402
import study  # noqa: E402

JAR = os.path.exists(os.path.join(ROOT, "Decimation.jar"))


def corners(a, b):
    return {k: [b[i] if k[i] else a[i] for i in range(3)] for k in fit.CORNERS}


def verts(c):
    return study.verts_of_corners(c)


def part(name, a, b, group="gunModel", colour=(40, 40, 40), area=None):
    p = fit.new_part(name, group, colour, corners(a, b), area or name)
    p.area = area or name
    return p


RECEIVER = ((-5.0, -4.6, -0.6), (5.0, -2.6, 0.4))          # 10 long, 2 high, 1 wide; its +z side at 0.4


class RuleTest(unittest.TestCase):
    def rule(self, boxes):
        return study.detail_parts([verts(corners(a, b)) for a, b in boxes])

    def test_rivet_on_a_side(self):
        out = self.rule([RECEIVER, ((1.0, -3.7, 0.36), (1.2, -3.5, 0.48))])
        self.assertIsNone(out[0])
        self.assertEqual((out[1]["host"], out[1]["axis"], out[1]["side"]), (0, 2, 1))

    def test_rivet_on_the_right_side(self):
        out = self.rule([RECEIVER, ((1.0, -3.7, -0.68), (1.2, -3.5, -0.56))])
        self.assertEqual((out[1]["axis"], out[1]["side"]), (2, -1))

    def test_pin_through_the_receiver(self):
        out = self.rule([RECEIVER, ((2.0, -3.3, -0.68), (2.2, -3.1, 0.48))])
        self.assertEqual((out[1]["axis"], out[1]["side"]), (2, 0))

    def test_rib_on_top(self):
        out = self.rule([RECEIVER, ((0.0, -4.8, -0.4), (0.2, -4.55, 0.2))])
        self.assertEqual((out[1]["axis"], out[1]["side"]), (1, -1))           # y grows downward: the top is -y

    def test_strip_along_a_side(self):
        """A thin plate 2.5 long proud of the side (Decimation's handguard strips): a detail."""
        out = self.rule([RECEIVER, ((-2.0, -3.4, 0.35), (0.5, -2.8, 0.5))])
        self.assertEqual((out[1]["axis"], out[1]["side"]), (2, 1))

    def test_barrel_out_of_the_receiver_is_not_one(self):
        out = self.rule([RECEIVER, ((5.0, -3.8, -0.2), (12.0, -3.5, 0.1))])
        self.assertIsNone(out[1])

    def test_layers_of_a_bar_are_not_details(self):
        """An octagon's slices have the same footprint: none hosts another."""
        out = self.rule([((0, -0.2, -0.25), (6, 0.2, 0.25)), ((0, -0.35, -0.15), (6, -0.2, 0.15)),
                         ((0, 0.2, -0.15), (6, 0.35, 0.15))])
        self.assertEqual(out, [None, None, None])

    def test_a_loose_part_is_not_one(self):
        out = self.rule([RECEIVER, ((1.0, -3.7, 0.6), (1.2, -3.5, 0.7))])     # 0.2 off the side
        self.assertIsNone(out[1])


class RecordTest(unittest.TestCase):
    def test_place_and_offsets_on_the_host(self):
        rec_parts = [RECEIVER, ((-2.5, -3.7, 0.32), (-2.3, -3.5, 0.48))]      # u 0.25 along, v 0.5 across, 0.08 proud
        cl = [corners(a, b) for a, b in rec_parts]
        rule = study.detail_parts([verts(c) for c in cl])
        recs = details.records(cl, rule, [0, 1], tones=[(40, 40, 40), (60, 60, 60)], groups=["gunModel"] * 2)
        self.assertEqual(len(recs), 1)
        r = recs[0]
        self.assertEqual((r["axis"], r["side"]), (2, 1))
        self.assertAlmostEqual(r["u"], 0.26, delta=0.001)
        self.assertAlmostEqual(r["v"], 0.5, delta=0.001)
        dn = sorted(o[2] for o in r["off"])
        self.assertAlmostEqual(dn[0], -0.08, delta=1e-6)
        self.assertAlmostEqual(dn[-1], 0.08, delta=1e-6)
        self.assertEqual(r["tone"], [60, 60, 60])
        self.assertEqual(r["hostTone"], [40, 40, 40])


def record(u, v, size=(0.2, 0.2), proud=0.08, inset=-0.04, axis=2, side=1, length=10.0):
    """A detail record by hand: its footprint `size` (along u, across v), inner face `inset`, outer face `proud`."""
    off = []
    for k in fit.CORNERS:
        b, c = details.PLANE[axis]
        db = (size[0] / 2) * (1 if k[b] else -1)
        dc = (size[1] / 2) * (1 if k[c] else -1)
        outer = k[axis] == (1 if side >= 0 else 0)
        if side == 0:
            off.append([db, dc, proud, 1 if k[axis] else -1])
        else:
            off.append([db, dc, proud if outer else inset, side])
    return {"axis": axis, "side": side, "u": u, "v": v, "len": length, "off": off, "group": "gunModel",
            "kind": "cuboid", "tone": [60, 60, 60], "hostTone": [40, 40, 40]}


class PlaceTest(unittest.TestCase):
    def test_on_a_flat_side(self):
        ours = [part("receiver", (0.0, -4.0, -0.5), (20.0, -2.0, 0.5))]
        d = details.place(record(0.25, 0.5), ours, ours)
        self.assertIsNotNone(d.corners, d.reason)
        c = d.corners
        xs = [q[0] for q in c.values()]
        ys = [q[1] for q in c.values()]
        zs = [q[2] for q in c.values()]
        self.assertAlmostEqual(sum(xs) / 8, 5.0, delta=1e-6)                    # u 0.25 of a 20 long area
        self.assertAlmostEqual(sum(ys) / 8, -3.0, delta=1e-6)                   # v 0.5 of its column
        self.assertAlmostEqual(max(zs), 0.58, delta=1e-6)                       # as proud as in the exemplar
        self.assertAlmostEqual(min(zs), 0.5 - details.SEAT, delta=1e-6)        # pushed in to be seated
        self.assertEqual(round(max(xs) - min(xs), 6), 0.2)                     # its own size, not scaled up

    def test_on_a_leaning_side(self):
        """The +z side leans in toward the top (a taper): every inner corner still inside the part."""
        c = corners((0.0, -4.0, -0.5), (20.0, -2.0, 0.5))
        for k in fit.CORNERS:
            if k[1] == 0 and k[2] == 1:
                c[k][2] = 0.2                                                   # the top edge 0.3 further in
        host = fit.new_part("receiver", "gunModel", (40, 40, 40), c, "receiver")
        host.area = "receiver"
        d = details.place(record(0.5, 0.2, size=(0.2, 0.6)), [host], [host])
        self.assertIsNotNone(d.corners, d.reason)
        shape = fit.planes(host)
        inner = [q for k, q in d.corners.items() if k[2] == 0]
        self.assertTrue(all(fit.inside(shape, q, 1e-6) for q in inner))
        outer = [q for k, q in d.corners.items() if k[2] == 1]
        self.assertFalse(any(fit.inside(shape, q, 0.0) for q in outer))       # the outer face shows

    def test_nothing_under_it(self):
        """The column at u is empty (a gap in the area): the detail would float, so it is dropped."""
        ours = [part("receiver", (0.0, -4.0, -0.5), (8.0, -2.0, 0.5)),
                part("receiver", (12.0, -4.0, -0.5), (20.0, -2.0, 0.5))]
        d = details.place(record(0.5, 0.5), ours, ours)
        self.assertIsNone(d.corners)
        self.assertEqual(d.reason, "float")

    def test_hidden_under_another_part(self):
        ours = [part("receiver", (0.0, -4.0, -0.5), (20.0, -2.0, 0.5))]
        cover = part("magazine", (4.0, -3.5, -0.6), (6.0, 2.0, 0.7), group="ammoModel")
        d = details.place(record(0.25, 0.5), ours, ours + [cover])
        self.assertIsNone(d.corners)
        self.assertIn(d.reason, ("hidden", "clash"))

    def test_near_a_detail_the_source_has(self):
        ours = [part("receiver", (0.0, -4.0, -0.5), (20.0, -2.0, 0.5))]
        rivet = part("receiver", (4.95, -3.05, 0.45), (5.15, -2.85, 0.57))
        d = details.place(record(0.25, 0.5), ours, ours + [rivet], existing=[rivet])
        self.assertIsNone(d.corners)
        self.assertEqual(d.reason, "dup")

    def test_pin_through_spans_our_width(self):
        ours = [part("receiver", (0.0, -4.0, -0.7), (20.0, -2.0, 0.4))]
        d = details.place(record(0.5, 0.5, side=0, proud=0.08), ours, ours)
        zs = [q[2] for q in d.corners.values()]
        self.assertAlmostEqual(min(zs), -0.78, delta=1e-6)
        self.assertAlmostEqual(max(zs), 0.48, delta=1e-6)

    def test_long_strip_shrinks_with_a_shorter_area(self):
        ours = [part("handguard", (0.0, -4.0, -0.5), (5.0, -2.0, 0.5))]
        d = details.place(record(0.5, 0.5, size=(2.0, 0.3), length=10.0), ours, ours)
        xs = [q[0] for q in d.corners.values()]
        self.assertLessEqual(max(xs) - min(xs), 2.0 * details.SHRINK_MIN + 1e-6)
        self.assertGreaterEqual(min(xs), 0.0)
        self.assertLessEqual(max(xs), 5.0)


class ApplyTest(unittest.TestCase):
    def data(self):
        recs = [record(0.2, 0.5), record(0.5, 0.5), record(0.8, 0.5)]
        area = {"box": [[0, 10], [-4, -2], [-0.5, 0.5]], "parts": [], "detailRecs": recs, "count": 4, "details": 3}
        return {"guns": {"g1": {"cat": "rifle", "areas": {"receiver": area}}}}

    def test_report_and_counts(self):
        ours = [part("receiver", (0.0, -4.0, -0.5), (20.0, -2.0, 0.5))]
        out, rep, by = details.apply(ours, {"receiver": {"gun": "g1"}}, self.data())
        added = [p for p in out if getattr(p, "detail_from", None)]
        self.assertEqual(len(added), 3)
        self.assertEqual(by, {"g1": 3})
        self.assertEqual(rep["receiver"]["gun"], "g1")
        self.assertEqual(rep["receiver"]["placed"], 3)
        for p in added:
            self.assertEqual(p.area, "receiver")
            self.assertEqual(p.group, "gunModel")
            self.assertGreater(p.colour[0], 40)            # lighter than the host, as the exemplar's tone over its host's

    def test_only_inside_the_fit_ranges(self):
        ours = [part("receiver", (0.0, -4.0, -0.5), (20.0, -2.0, 0.5))]
        out, rep, by = details.apply(ours, {"receiver": {"gun": "g1", "ranges": [(0.0, 7.0)]}}, self.data())
        self.assertEqual(rep["receiver"]["placed"], 1)                   # only u 0.2 (x 4) lies in 0 .. 7

    def test_placed_details_are_seated(self):
        """study.py contact on the result: no transferred detail floats."""
        ours = [part("receiver", (0.0, -4.0, -0.5), (20.0, -2.0, 0.5))]
        out, _, _ = details.apply(ours, {"receiver": {"gun": "g1"}}, self.data())
        rows = details.unseated(out)
        self.assertEqual(rows, [])


@unittest.skipUnless(JAR, "needs Decimation.jar")
class JarTest(unittest.TestCase):
    def test_akm_details(self):
        """Decimation's AKM: rivets, pins, ribs, strips and levers count as details (about a third of its parts), its
        magazine slices and stock panels do not."""
        g = study.load("akm")
        rule = study.detail_parts([p.verts() for p in g.parts])
        n = sum(1 for r in rule if r)
        self.assertGreaterEqual(n, 50)
        self.assertLessEqual(n, 110)
        for p, r in zip(g.parts, rule):
            if p.group == "ammoModel":
                self.assertIsNone(r, p.name)
        m = study.metrics(g)
        self.assertEqual(m["details"], n)
        self.assertAlmostEqual(m["detail%"], 100.0 * n / len(g.parts), places=6)


if __name__ == "__main__":
    unittest.main()

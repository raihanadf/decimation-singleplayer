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


def ours(name, a, b, group="gunModel", colour=(40, 40, 40)):
    p = fit.new_part(name, group, colour, box_corners(a, b), name)
    return p


def gun(**areas):
    """A synthetic dataset gun: area name -> [(a, b, group)]."""
    out = {}
    for area, boxes in areas.items():
        out[area] = exemplar.make_area([box_corners(a, b) for a, b, _ in boxes], [(50, 50, 50)] * len(boxes),
                                       [g for _, _, g in boxes])
    return {"cat": "rifle", "areas": out}


def xs_of(c):
    return [q[0] for q in c.values()]


class RobustBoxTest(unittest.TestCase):
    def test_a_small_outlier_does_not_widen_the_box(self):
        body = box_corners((0, 0, 0), (10, 2, 1))
        pin = box_corners((4.0, 2.0, 0.4), (4.2, 3.5, 0.6))         # a thin stray part hanging 1.5 below
        b = fit.robust_box([body, pin])
        self.assertEqual([tuple(round(v, 6) for v in ab) for ab in b], [(0, 10), (0, 2), (0, 1)])

    def test_big_parts_count(self):
        body = box_corners((0, 0, 0), (10, 2, 1))
        mag = box_corners((4, 2, 0), (6, 6, 1))                       # 8 of 28: not an outlier
        self.assertEqual(round(fit.robust_box([body, mag])[1][1], 6), 6.0)


class ProfileTest(unittest.TestCase):
    def test_a_piece_apart_does_not_shape_the_profile(self):
        """A piece beside a magazine's top and apart from it (a latch) is left out of the profile the exemplar
        follows: each station takes its largest connected stretch."""
        slices = [box_corners((0.3 * i, i, -0.4), (0.3 * i + 2.0, i + 1, 0.4)) for i in range(6)]
        latch = box_corners((3.6, 0.0, -0.6), (3.9, 1.2, 0.6))
        prof = exemplar.target_profile(exemplar.target_area(slices + [latch]))
        self.assertLess(prof[0][4], 2.5)                    # the top station's x extent ends at the slice, not the latch
        self.assertGreater(prof[0][3], -0.1)

    def test_touching_pieces_count_together(self):
        """A skewed bottom segment beside a slice (the AKM sample's magazine) widens the station: both count."""
        a = box_corners((1.1, 3.0, -0.5), (3.9, 3.4, 0.2))
        b = box_corners((2.7, 3.0, -0.5), (4.2, 5.0, 0.2))
        top = box_corners((0.0, 0.0, -0.5), (3.0, 3.0, 0.2))
        area = exemplar.target_area([top, a, b])
        self.assertEqual(area["axis"], 1)
        prof = exemplar.target_profile(area)                    # rows: t, z lo, z hi, x lo, x hi (CYCLIC[1])
        mid = [r for r in prof if 3.05 <= 5.0 * r[0] <= 3.35]
        self.assertTrue(mid)
        self.assertAlmostEqual(min(r[3] for r in mid), 1.1, delta=0.01)
        self.assertAlmostEqual(max(r[4] for r in mid), 4.2, delta=0.01)


class WarpTest(unittest.TestCase):
    def test_a_block_lands_where_ours_is(self):
        """Box to box puts the exemplar's sight block at its own place along the run; the warp moves it onto ours."""
        ex = [box_corners((0, 0, 0), (10, 1, 1)), box_corners((3, -1, 0), (4, 0, 1))]
        tg = [box_corners((0, 0, 0), (10, 1, 1)), box_corners((5, -1, 0), (6, 0, 1))]
        w = exemplar.warp_x(ex, tg)
        xs = xs_of(w[1])
        self.assertAlmostEqual(sum(xs) / 8, 5.5, delta=0.3)
        self.assertGreater(exemplar.overlap(w, tg), exemplar.overlap(ex, tg) + 0.1)
        bar = xs_of(w[0])
        self.assertAlmostEqual(min(bar), 0.0, delta=0.15)              # the ends stay where they were
        self.assertAlmostEqual(max(bar), 10.0, delta=0.15)


class ClipTest(unittest.TestCase):
    def test_cut_a_taper_along_x(self):
        c = box_corners((0, 0, 0), (10, 2, 1))
        for k in fit.CORNERS:
            if k[0] == 1:
                c[k][1] = 1.0 if k[1] else 0.5                      # the front end narrower: y 0.5 .. 1.0
        cut = exemplar.clip_x(c, 2.0, 6.0)
        self.assertEqual(sorted(set(round(v, 6) for v in xs_of(cut))), [2.0, 6.0])
        front = [q for k, q in cut.items() if k[0] == 1]
        self.assertAlmostEqual(min(q[1] for q in front), 0.3, delta=1e-6)   # 0 + (0.5 - 0) * 0.6

    def test_a_part_across_the_run_goes_by_its_centre(self):
        c = box_corners((0, 0, 0), (0.4, 3, 1))
        self.assertIsNotNone(exemplar.clip_x(c, 0.0, 1.0))
        self.assertIsNone(exemplar.clip_x(c, 1.0, 2.0))


class AssemblyTest(unittest.TestCase):
    """Our receiver and magwell are cut at another line than the exemplar's: area by area the outlines differ, together
    they are the same, so the action assembly takes the exemplar's parts."""

    def setUp(self):
        self.data = {"guns": {"ex1": gun(receiver=[((0, -2, -0.5), (10, -1, 0.5), "gunModel")],
                                         magwell=[((3, -1, -0.4), (6, 0, 0.4), "gunModel")])}}
        self.parts = [ours("rec", (0, -2, -0.5), (10, -1.5, 0.5)), ours("low", (0, -1.5, -0.5), (10, -1, 0.5)),
                      ours("well", (3, -1, -0.4), (6, 0, 0.4))]
        self.labels = ["receiver", "magwell", "magwell"]

    def test_areas_alone_fail(self):
        tg = [self.parts[i].c for i in (1, 2)]
        ex = self.data["guns"]["ex1"]["areas"]["magwell"]
        w = exemplar.refit(ex, exemplar.target_area(tg), False)
        self.assertLess(exemplar.overlap(w, tg), exemplar.GATE)

    def test_assembly_passes(self):
        out, rep, by, plan = exemplar.apply(self.parts, "rifle", "ex1", {}, self.data, labels=self.labels)
        self.assertEqual(rep["receiver"]["gun"], "ex1")
        self.assertEqual(rep["receiver"]["via"], "action")
        self.assertEqual(rep["magwell"]["via"], "action")
        self.assertEqual(len(out), 2)
        self.assertTrue(all(getattr(p, "exemplar", None) == "ex1" for p in out))
        self.assertEqual(sorted(p.area for p in out), ["magwell", "receiver"])
        self.assertEqual(by, {"ex1": 2})
        self.assertNotIn("receiver", plan)                            # its details came with the exemplar


class AssemblyKeepsTest(unittest.TestCase):
    def test_areas_the_exemplar_lacks_stay_ours(self):
        """Our bolt (slideModel, it moves when firing) stays when the assembly's exemplar has no bolt of its own."""
        data = {"guns": {"ex1": gun(receiver=[((0, -2, -0.5), (10, -1, 0.5), "gunModel")],
                                    magwell=[((3, -1, -0.4), (6, 0, 0.4), "gunModel")])}}
        parts = [ours("rec", (0, -2, -0.5), (10, -1.5, 0.5)), ours("low", (0, -1.5, -0.5), (10, -1, 0.5)),
                 ours("well", (3, -1, -0.4), (6, 0, 0.4)), ours("bolt", (2, -1.9, -0.7), (4, -1.6, -0.5), "slideModel")]
        out, rep, by, plan = exemplar.apply(parts, "rifle", "ex1", {}, data,
                                            labels=["receiver", "magwell", "magwell", "bolt"])
        self.assertEqual(rep["receiver"]["via"], "action")
        bolts = [p for p in out if p.group == "slideModel"]
        self.assertEqual(len(bolts), 1)
        self.assertIsNone(getattr(bolts[0], "exemplar", None))
        self.assertEqual(rep["bolt"], {"fit": 1})            # judged as an area of its own below: no exemplar has one


class SectionsTest(unittest.TestCase):
    """Ours: a bar along 0 .. 10 with a block on top of its front half. ex_front matches only the front half (its
    block hangs below the bar instead), ex_rear only the rear half."""

    def setUp(self):
        self.parts = [ours("bar", (0, -2, -0.5), (10, -1, 0.5)), ours("top", (5, -3, -0.5), (10, -2, 0.5))]
        self.labels = ["receiver", "receiver"]
        self.front = gun(receiver=[((0, -2, -0.5), (10, -1, 0.5), "gunModel"), ((5, -1, -0.5), (10, 0, 0.5), "gunModel")])
        self.rear = gun(receiver=[((0, -2, -0.5), (5, -1, 0.5), "gunModel"), ((5, -3, -0.5), (10, -2.5, 0.5), "gunModel")])

    def test_whole_area_fails(self):
        tg = [p.c for p in self.parts]
        w = exemplar.refit(self.front["areas"]["receiver"], exemplar.target_area(tg), False)
        self.assertLess(exemplar.overlap(w, tg), exemplar.GATE)

    def test_front_from_the_exemplar_rear_kept(self):
        data = {"guns": {"ex_front": self.front}}
        out, rep, by, plan = exemplar.apply(self.parts, "rifle", "ex_front", {}, data, labels=self.labels)
        r = rep["receiver"]
        self.assertEqual([s["gun"] for s in r["sections"]], ["ex_front"])
        self.assertAlmostEqual(r["sections"][0]["from"], 5.0, delta=0.51)
        self.assertAlmostEqual(r["sections"][0]["to"], 10.0, delta=0.01)
        ex = [p for p in out if getattr(p, "exemplar", None)]
        kept = [p for p in out if not getattr(p, "exemplar", None)]
        self.assertTrue(ex and kept)
        self.assertGreaterEqual(min(min(xs_of(p.c)) for p in ex), 4.49)
        self.assertLessEqual(max(max(xs_of(p.c)) for p in kept), r["sections"][0]["from"] + 1e-6)
        self.assertEqual(plan["receiver"]["gun"], "ex_front")
        self.assertEqual(plan["receiver"]["ranges"], [(0.0, r["sections"][0]["from"])])

    def test_two_exemplars_combined(self):
        data = {"guns": {"ex_front": self.front, "ex_rear": self.rear}}
        out, rep, by, plan = exemplar.apply(self.parts, "rifle", "ex_front", {}, data, labels=self.labels)
        guns = sorted(s["gun"] for s in rep["receiver"]["sections"])
        self.assertEqual(guns, ["ex_front", "ex_rear"])
        self.assertEqual(set(by), {"ex_front", "ex_rear"})
        self.assertNotIn("receiver", plan)                             # no bin left to the fit


class SmoothTest(unittest.TestCase):
    def test_a_collapsed_end_takes_its_neighbour(self):
        """A slanted butt cap makes the last station narrow: the exemplar's end must not be squeezed into it."""
        prof = [[(i + 0.5) / 12, -3.0, 0.5, -0.5, 0.4] for i in range(12)]
        prof[0] = [prof[0][0], -2.7, -1.6, -0.5, 0.4]
        out = exemplar.smooth_profile(prof)
        self.assertAlmostEqual(out[0][2], 0.5, delta=1e-6)
        self.assertAlmostEqual(out[0][1], -3.0, delta=1e-6)

    def test_wide_stations_are_outliers(self):
        """A piece that is not magazine (a latch) widens the magazine's top stations: they take the nearest normal one,
        so the exemplar's top slice is not stretched into a wedge."""
        prof = [[(i + 0.5) / 12, 0.2 * i, 0.2 * i + 2.2, -0.5, 0.3] for i in range(12)]
        prof[1] = [prof[1][0], -0.6, 3.9, -0.8, 0.5]
        prof[2] = [prof[2][0], -0.4, 3.9, -0.7, 0.4]
        out = exemplar.robust_profile(prof)
        self.assertEqual(out[1][1:], prof[0][1:])
        self.assertEqual(out[2][1:], prof[3][1:])
        self.assertEqual(out[5], prof[5])

    def test_an_end_station_off_the_line_is_an_outlier(self):
        """A narrow end station far from its neighbour (a latch at the top of a magazine) takes the neighbour's; a
        narrowing end on the line (a magazine's rounded base) stays."""
        prof = [[(i + 0.5) / 12, 0.2 * i, 0.2 * i + 2.2, -0.5, 0.3] for i in range(12)]
        prof[0] = [prof[0][0], 3.6, 3.9, -0.5, 0.3]
        prof[11] = [prof[11][0], 2.4, 3.0, -0.5, 0.3]
        out = exemplar.robust_profile(prof)
        self.assertEqual(out[0][1:], prof[1][1:])
        self.assertEqual(out[11], prof[11])

    def test_a_straight_edge_stays_a_line(self):
        prof = [[(i + 0.5) / 12, -3.0 + 0.05 * (i % 2), 0.5, -0.5, 0.4] for i in range(12)]
        out = exemplar.smooth_profile(prof)
        top = [r[1] for r in out]
        steps = [b - a for a, b in zip(top, top[1:])]
        self.assertLess(max(steps) - min(steps), 1e-9)                 # one straight line


class ToneTest(unittest.TestCase):
    def test_exemplar_tones_for_a_one_colour_source(self):
        data = {"guns": {"ex1": {"cat": "rifle", "areas": {"grip": exemplar.make_area(
            [box_corners((0, 0, 0), (1, 3, 0.8))], [(41, 25, 22)], ["gunModel"])}}}}
        parts = [ours("grip", (0, 0, 0), (1.1, 3.2, 0.8), colour=(20, 20, 20))]
        out, rep, by, plan = exemplar.apply(parts, "rifle", "ex1", {}, data, labels=["grip"], exemplar_tones=True)
        self.assertEqual(out[0].colour, (41, 25, 22))
        out, rep, by, plan = exemplar.apply(parts, "rifle", "ex1", {}, data, labels=["grip"])
        self.assertEqual(out[0].colour, (20, 20, 20))


class SectionPolicyTest(unittest.TestCase):
    def test_a_small_match_is_no_section(self):
        """An exemplar that matches only a sliver of the area (one metre of a long receiver) gives no section: a
        patchwork of exemplar pieces looked worse than the fit with its details (the m590's first run)."""
        parts = [ours("bar", (0, -2, -0.5), (10, -1, 0.5)), ours("top", (0, -3, -0.5), (1, -2, 0.5))]
        ex = gun(receiver=[((0, -2, -0.5), (10, -1, 0.5), "gunModel"), ((0, -3, -0.5), (10, -2, 0.5), "gunModel")])
        out, rep, by, plan = exemplar.apply(parts, "rifle", "ex", {}, {"guns": {"ex": ex}}, labels=["receiver"] * 2)
        self.assertIn("fit", rep["receiver"])
        self.assertNotIn("sections", rep["receiver"])
        self.assertEqual(plan["receiver"], {"gun": "ex"})


class FamilyTest(unittest.TestCase):
    def test_only_guns_close_overall_give_parts(self):
        """Donors come from the family (the guns closest to ours overall, the like gun first): an area of a far gun
        that happens to fit (an AK receiver on a lever rifle) is not taken."""
        body = ((0, -2, -0.5), (10, -1, 0.5))
        data = {"guns": {"near": gun(receiver=[((0, -2.2, -0.5), (10, -0.6, 0.5), "gunModel"),
                                               ((2, -0.6, -0.5), (4, 0.0, 0.5), "gunModel")]),
                         "far": gun(receiver=[(body[0], body[1], "gunModel")])}}
        parts = [ours("rec", *body)]
        out, rep, by, plan = exemplar.apply(parts, "rifle", "near", {}, data, labels=["receiver"], family=["near"])
        self.assertNotIn("far", by)
        out, rep, by, plan = exemplar.apply(parts, "rifle", "near", {}, data, labels=["receiver"])
        self.assertIn("far", by)


class NoInterleaveTest(unittest.TestCase):
    def test_a_second_exemplar_stays_outside_the_first(self):
        """The best exemplar matches the rear and the front of a receiver, a second one its middle: the middle keeps
        our fit instead of a patchwork of three pieces from two guns (the m590's receiver)."""
        bar = ((0, -1.5, -0.5), (12, -1, 0.5), "gunModel")
        a = gun(receiver=[bar, ((0, -3, -0.5), (3.5, -1.5, 0.5), "gunModel"),
                          ((9.5, -3, -0.5), (12, -1.5, 0.5), "gunModel")])
        b = gun(receiver=[bar, ((3.5, -3, -0.5), (9.5, -1.5, 0.5), "gunModel"), ((0, -3, -0.5), (0.4, -2.9, 0.5), "gunModel"),
                          ((11.6, -3, -0.5), (12, -2.9, 0.5), "gunModel")])
        parts = [ours("bar", (0, -1.5, -0.5), (12, -1, 0.5)), ours("top", (0, -3, -0.5), (12, -1.5, 0.5))]
        out, rep, by, plan = exemplar.apply(parts, "rifle", "a", {}, {"guns": {"a": a, "b": b}},
                                            labels=["receiver"] * 2)
        secs = rep["receiver"]["sections"]
        self.assertEqual({s["gun"] for s in secs}, {"a"})
        self.assertEqual(len(plan["receiver"]["ranges"]), 1)
        x0, x1 = plan["receiver"]["ranges"][0]
        self.assertGreaterEqual(x0, 2.9)
        self.assertLessEqual(x1, 10.1)


def wood_metal(area, boxes):
    """A dataset gun whose parts carry tones: boxes [(a, b, tone)]."""
    ex = exemplar.make_area([box_corners(a, b) for a, b, _ in boxes], [c for _, _, c in boxes],
                            ["gunModel"] * len(boxes))
    return {"cat": "rifle", "areas": {area: ex}}


class MaterialTest(unittest.TestCase):
    WOOD, METAL = (60, 35, 25), (40, 40, 40)

    def test_wood_where_ours_is_metal_fails(self):
        """The same outline in wood where ours is steel (a full length rifle stock's handguard over a lever rifle's
        tube magazine) is no match: the material along the run must agree too."""
        parts = [ours("bar", (0, -2, -0.5), (10, -1, 0.5), colour=self.METAL),
                 ours("pump", (2, -1, -0.5), (4, 0, 0.5), colour=self.WOOD)]
        data = {"guns": {"ex": wood_metal("handguard", [((0, -2, -0.5), (10, -1, 0.5), self.WOOD),
                                                         ((2, -1, -0.5), (4, 0, 0.5), self.WOOD)])}}
        out, rep, by, plan = exemplar.apply(parts, "rifle", "ex", {}, data, labels=["handguard"] * 2)
        self.assertIn("fit", rep["handguard"])
        self.assertEqual(by, {})

    def test_same_materials_pass(self):
        parts = [ours("bar", (0, -2, -0.5), (10, -1, 0.5), colour=self.METAL),
                 ours("pump", (2, -1, -0.5), (4, 0, 0.5), colour=self.WOOD)]
        data = {"guns": {"ex": wood_metal("handguard", [((0, -2, -0.5), (10, -1, 0.5), self.METAL),
                                                         ((2, -1, -0.5), (4, 0, 0.5), self.WOOD)])}}
        out, rep, by, plan = exemplar.apply(parts, "rifle", "ex", {}, data, labels=["handguard"] * 2)
        self.assertEqual(rep["handguard"]["gun"], "ex")


class ColouredTest(unittest.TestCase):
    def test_classes(self):
        self.assertTrue(exemplar.coloured((38, 24, 21)))        # the akm's wood
        self.assertFalse(exemplar.coloured((20, 20, 20)))       # its steel
        self.assertIsNone(exemplar.coloured((46, 37, 38)))      # its grip: a dark brownish grey votes for neither
        self.assertIsNone(exemplar.coloured(None))


class HoleTest(unittest.TestCase):
    def test_a_gap_left_between_areas_is_filled_from_the_fit(self):
        """An exemplar barrel that starts later than ours (its box began with a low rod) left a hole between the
        receiver and the barrel (the m590's first runs): our fit fills it."""
        parts = [ours("rec", (0, -2.5, -0.5), (5, -0.5, 0.5)), ours("bar", (5, -1.8, -0.3), (12, -1.2, 0.3))]
        data = {"guns": {"ex": gun(barrel=[((6, -1.8, -0.3), (12, -1.2, 0.3), "gunModel"),
                                           ((5, -1.2, -0.1), (12, -1.1, 0.1), "gunModel")])}}
        out, rep, by, plan = exemplar.apply(parts, "rifle", "ex", {}, data, labels=["receiver", "barrel"])
        self.assertEqual(rep["barrel"]["gun"], "ex")
        filled = [p for p in out if not getattr(p, "exemplar", None) and p.area == "barrel"]
        self.assertTrue(filled)
        xs = [q[0] for p in filled for q in p.c.values()]
        self.assertAlmostEqual(min(xs), 5.0, delta=0.15)
        self.assertLessEqual(max(xs), 6.3)
        self.assertEqual(rep["barrel"]["filled"], len(filled))


class GroupTest(unittest.TestCase):
    def test_moving_parts_keep_their_group(self):
        self.assertEqual(exemplar.new_group("slideModel", False), "slideModel")
        self.assertEqual(exemplar.new_group("ammoModel", False), "ammoModel")
        self.assertEqual(exemplar.new_group("defaultStockModel", False), "gunModel")
        self.assertEqual(exemplar.new_group("defaultScopeModel", False), "gunModel")
        self.assertEqual(exemplar.new_group("defaultScopeModel", True), "defaultScopeModel")


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
        st = conv.stats
        self.assertEqual(st["fromExemplars"] + st["fromDetails"] + st["fromFit"], len(conv.parts))
        groups = {p.group for p in conv.parts}
        self.assertTrue({"ammoModel", "slideModel"} <= groups, groups)        # magazine and bolt keep their groups
        self.assertTrue(all(getattr(p, "area", None) for p in conv.parts))  # every part knows its area
        import details
        placed = [p for p in conv.parts if getattr(p, "detail_from", None)]
        self.assertEqual(sum(st["detailsByGun"].values()), len(placed))
        self.assertEqual(st["fromDetails"], len(placed))
        for area, r in st["details"].items():
            self.assertIn("gun", r)
            self.assertIn("placed", r)
        self.assertEqual(details.unseated(conv.parts), [])                   # no transferred detail floats


if __name__ == "__main__":
    unittest.main()

"""The conversion: units and axes, anchoring to the like gun, groups, tones, attachment points, names, like."""
import os
import sys
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
TOOLS = os.path.dirname(os.path.dirname(HERE))
ROOT = os.path.dirname(TOOLS)
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.dirname(HERE))
sys.path.insert(0, os.path.join(TOOLS, "guns"))
import convert  # noqa: E402
import meshio  # noqa: E402
import synth  # noqa: E402

JAR = os.path.exists(os.path.join(ROOT, "Decimation.jar"))
SAMPLE = os.path.join(ROOT, "build", "gunconv", "inputs", "toastie_akm", "akm.glb")


def synth_model(rot="muzzle -z, up +y", scale=0.05):
    g = synth.glb_of(synth.rotated(synth.synth_gun(), synth.ROTATIONS[rot], scale=scale, offset=(1, 2, 3)))
    return meshio.read_bytes(g.bytes(), "synthgun.glb")


def bounds(parts, groups=None, names=None):
    pts = [q for p in parts if (groups is None or p.group in groups) and (names is None or p.label in names)
           for q in p.points()]
    return [(min(q[i] for q in pts), max(q[i] for q in pts)) for i in range(3)]


def centre(parts, label):
    pts = [q for p in parts if p.label == label for q in p.points()]
    return [sum(q[i] for q in pts) / len(pts) for i in range(3)]


@unittest.skipUnless(JAR, "needs Decimation.jar for the like gun")
class LayoutTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        # the milestone 1 fit path (exemplar areas: tests/test_exemplar.py)
        cls.conv = convert.convert(synth_model(), name="synthgun", like="akm", length=28.0, exemplars=False)

    def test_length_and_units(self):
        (x0, x1), _, _ = bounds(self.conv.parts)
        self.assertAlmostEqual(x1 - x0, 28.0, delta=0.05)

    def test_axes_muzzle_up_right(self):
        parts = self.conv.parts
        self.assertGreater(centre(parts, "barrel")[0], centre(parts, "stock")[0])
        self.assertGreater(centre(parts, "Magazine")[1], centre(parts, "receiver")[1])   # y grows downward
        self.assertLess(centre(parts, "Bolt")[2], centre(parts, "receiver")[2])          # right side is -z

    def test_centred_on_decimations_line(self):
        _, _, (z0, z1) = bounds(self.conv.parts, names={"receiver"})
        self.assertAlmostEqual((z0 + z1) / 2, -0.15, delta=0.05)

    def test_groups_from_names(self):
        groups = {p.label: p.group for p in self.conv.parts}
        self.assertEqual(groups["Magazine"], "ammoModel")
        self.assertEqual(groups["Bolt"], "slideModel")
        self.assertEqual(groups["frontSight"], "gunModel")   # the AKM keeps its iron sights (no defaultScope parts)

    def test_header_from_the_like_gun(self):
        import study
        akm = study.load("akm")
        for k in ("rhPos", "lhPos", "rhRot", "lhRot"):
            self.assertEqual(tuple(self.conv.header[k]), tuple(akm.header[k]))

    def test_flame_point_on_decimations_convention(self):
        (x0, x1), _, _ = bounds(self.conv.parts)
        flame = self.conv.points["flame"]
        self.assertAlmostEqual(flame[0], x1 - 0.1, delta=0.01)
        _, (by0, by1), _ = bounds(self.conv.parts, names={"barrel"})
        self.assertAlmostEqual(flame[1], (by0 + by1) / 2 - 0.85, delta=0.06)
        self.assertAlmostEqual(flame[2], -0.15, delta=1e-6)
        self.assertEqual(tuple(self.conv.header["flamePos"]), tuple(flame))

    def test_points_shape(self):
        p = self.conv.points
        self.assertEqual(set(p), {"flame", "railTop", "rail", "gripRail", "mount"})
        self.assertIsInstance(p["railTop"], float)
        self.assertEqual(p["mount"], "side")        # the akm's row in sight_mounts.txt
        self.assertIsNone(p["rail"])
        self.assertIsNone(p["gripRail"])

    def test_dark_tones(self):
        for p in self.conv.parts:
            lum = 0.3 * p.colour[0] + 0.59 * p.colour[1] + 0.11 * p.colour[2]
            self.assertLessEqual(lum, 80, p.name)

    def test_front_sight_on_the_like_guns_sight_line(self):
        """The aim pose is the same for every gun: our front sight tip must stand where the like gun's does."""
        import match
        like = match.front_tip([p.verts() for p in match.guns()["akm"].parts])
        ours = match.front_tip([p.points() for p in self.conv.parts])
        self.assertAlmostEqual(ours, like, delta=0.03)

    def test_tones_follow_the_like_guns_palette(self):
        """Decimation's akm: near black metal, dark brown wood, a lighter orange brown bakelite magazine."""
        lum = lambda c: 0.3 * c[0] + 0.59 * c[1] + 0.11 * c[2]  # noqa: E731
        steel = [p.colour for p in self.conv.parts if p.label == "receiver"]
        wood = [p.colour for p in self.conv.parts if p.label == "stock"]
        mag = [p.colour for p in self.conv.parts if p.label == "Magazine"]
        self.assertTrue(all(c[0] == c[1] == c[2] and lum(c) <= 46 for c in steel), steel)
        self.assertTrue(all(c[0] > c[1] > c[2] and 18 <= lum(c) <= 40 for c in wood), wood)
        self.assertTrue(all(c[0] > c[1] > c[2] and 45 <= lum(c) <= 80 for c in mag), mag)
        self.assertGreater(min(lum(c) for c in mag), max(lum(c) for c in wood))

    def test_receiver_panels_have_top_plates(self):
        """Every receiver panel (exposed top, bulky) carries its bevelled top plate."""
        rec = [p for p in self.conv.parts if p.label == "receiver"]
        plates = [p for p in rec if max(q[1] for q in p.points()) - min(q[1] for q in p.points()) <= 0.3]
        self.assertGreater(len(plates), 0)
        self.assertEqual(len(plates) * 2, len(rec))             # a plate and a core each

    def test_long_flat_parts_are_panels(self):
        """Decimation's parts are short (rifle median longest side 1.0, q90 1.4): long plates come in panels."""
        # the body's plates (gunModel); magazine slices are already cut along y and round bars stay long
        flat = [p for p in self.conv.parts if p.group == "gunModel" and not p.name.endswith(("Mid", "Top", "Bot"))]
        longest = max(max(q[0] for q in p.points()) - min(q[0] for q in p.points()) for p in flat)
        self.assertLessEqual(longest, convert.PANEL + 1e-6)

    def test_part_count_in_a_sane_range(self):
        self.assertGreaterEqual(len(self.conv.parts), 10)
        self.assertLessEqual(len(self.conv.parts), 300)


@unittest.skipUnless(JAR, "needs Decimation.jar")
class LikeRulesTest(unittest.TestCase):
    def test_sights_fold_when_the_like_gun_folds_its_own(self):
        conv = convert.convert(synth_model(rot="identity", scale=1.0), name="synthgun", like="m4a4", length=26.0,
                               exemplars=False)
        groups = {p.label: p.group for p in conv.parts}
        self.assertEqual(groups["frontSight"], "defaultScopeModel")

    def test_like_is_chosen_from_decimations_guns(self):
        conv = convert.convert(synth_model(rot="identity", scale=1.0), name="synthgun", category="rifle")
        import study
        self.assertIn(conv.like, study.all_names())
        self.assertEqual(study.load(conv.like).cat, "rifle")
        self.assertGreaterEqual(len(conv.candidates), 3)
        self.assertEqual(conv.candidates[0][0], conv.like)


@unittest.skipUnless(JAR, "needs Decimation.jar")
class TemplateColourTest(unittest.TestCase):
    def test_one_colour_models_take_the_like_guns_colours(self):
        """A file with one material (the Lamoot AK-47 sample) carries no colour: each piece takes the tone of the like
        gun's part it overlaps most (Decimation's akm: wood stock, dark metal receiver)."""
        grey = [(n, mesh, "Default", (128, 128, 128)) for n, mesh, _, _ in synth.synth_gun()]
        g = synth.glb_of(synth.rotated(grey, synth.ROTATIONS["identity"], scale=1.0))
        conv = convert.convert(meshio.read_bytes(g.bytes(), "onecolour.glb"), name="onecolour", like="akm",
                               length=28.0)
        stock = [p.colour for p in conv.parts if p.label == "stock"]
        receiver = [p.colour for p in conv.parts if p.label == "receiver"]
        self.assertTrue(stock and all(c[0] > c[2] + 8 for c in stock), stock)          # brown wood
        self.assertTrue(receiver and all(abs(c[0] - c[2]) <= 8 for c in receiver), receiver)   # grey metal


class NameTest(unittest.TestCase):
    def test_valid(self):
        for n in ("ak47", "akmx", "m4c"):
            convert.validate_name(n)

    def test_invalid(self):
        for n in ("", "AK47", "ak-47", "ak 47", "a" * 33):
            with self.assertRaises(ValueError):
                convert.validate_name(n)

    def test_ours_taken(self):
        for n in ("mac10", "ump9", "hk416", "mk18"):
            with self.assertRaises(ValueError):
                convert.validate_name(n)

    @unittest.skipUnless(JAR, "needs Decimation.jar")
    def test_decimations_taken(self):
        for n in ("akm", "m4a4", "uzi"):
            with self.assertRaises(ValueError):
                convert.validate_name(n)


class ToneTest(unittest.TestCase):
    def test_grey_metal_into_decimations_band(self):
        for c in ((200, 200, 200), (100, 100, 100), (0, 0, 0), (255, 255, 255)):
            t = convert.tone(c)
            self.assertTrue(18 <= t[0] <= 66 and t[0] == t[1] == t[2], (c, t))
        self.assertLess(convert.tone((60, 60, 60))[0], convert.tone((180, 180, 180))[0])

    def test_wood_stays_brown_and_dark(self):
        r, g, b = convert.tone((142, 92, 82))
        self.assertTrue(r > g > b, (r, g, b))
        self.assertLessEqual(0.3 * r + 0.59 * g + 0.11 * b, 60)


@unittest.skipUnless(JAR and os.path.exists(SAMPLE), "needs Decimation.jar and the git ignored sample model")
class SampleTest(unittest.TestCase):
    def test_akm_sample_is_like_an_ak(self):
        conv = convert.convert(SAMPLE, name="akmx", category="rifle")
        self.assertIn(conv.like, {"akm", "akms", "ak74", "aks74", "ak12", "rpk", "rpk74", "kms72", "vz58", "galil",
                                  "galilsar"})
        groups = {}
        for p in conv.parts:
            groups[p.group] = groups.get(p.group, 0) + 1
        self.assertGreater(groups.get("ammoModel", 0), 0)
        self.assertGreater(groups.get("slideModel", 0), 0)


if __name__ == "__main__":
    unittest.main()

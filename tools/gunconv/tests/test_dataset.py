"""The dataset of Decimation's guns: anatomy labels, exemplars per area, the build (spec, milestone 2)."""
import json
import os
import shutil
import sys
import tempfile
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
TOOLS = os.path.dirname(os.path.dirname(HERE))
ROOT = os.path.dirname(TOOLS)
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.dirname(HERE))
sys.path.insert(0, os.path.join(TOOLS, "guns"))
import dataset  # noqa: E402
import fit  # noqa: E402
import gunmodel  # noqa: E402
import study  # noqa: E402

JAR = os.path.exists(os.path.join(ROOT, "Decimation.jar"))

# a rifle laid out like Decimation's AKM (bore y -3.35, muzzle x 16): name, group, box
SYNTH = [
    ("buttpad", "gunModel", (-12.4, -4.0, -0.6), (-12.0, -0.5, 0.3)),
    ("stock", "gunModel", (-12.0, -4.0, -0.5), (-5.2, -1.0, 0.2)),
    ("receiverTop", "gunModel", (-5.0, -4.6, -0.6), (5.0, -3.6, 0.3)),
    ("receiverLow", "gunModel", (-5.0, -3.6, -0.6), (5.0, -2.6, 0.3)),
    ("rivet", "gunModel", (1.0, -3.2, 0.3), (1.2, -3.0, 0.38)),
    ("grip", "gunModel", (-5.0, -2.6, -0.45), (-3.6, 1.0, 0.15)),
    ("guardBottom", "gunModel", (-3.4, -1.2, -0.3), (-0.4, -1.05, 0.0)),
    ("guardFront", "gunModel", (-0.55, -2.6, -0.3), (-0.4, -1.05, 0.0)),
    ("trigger", "gunModel", (-2.2, -2.5, -0.25), (-2.0, -1.9, -0.05)),
    ("magwell", "gunModel", (-0.2, -2.6, -0.55), (3.2, -2.1, 0.25)),
    ("mag", "ammoModel", (0.0, -2.1, -0.45), (3.0, 3.0, 0.15)),
    ("bolt", "slideModel", (-1.0, -4.2, -1.0), (2.0, -3.6, -0.6)),
    ("rearSight", "gunModel", (4.0, -4.9, -0.3), (4.6, -4.6, 0.0)),
    ("handguard", "gunModel", (5.6, -4.3, -0.6), (11.0, -2.4, 0.3)),
    ("barrel", "gunModel", (5.0, -3.6, -0.4), (15.0, -3.1, 0.1)),
    ("muzzle", "gunModel", (15.0, -3.75, -0.5), (16.0, -2.95, 0.2)),
    ("frontSight", "gunModel", (14.0, -4.9, -0.3), (14.4, -3.6, 0.0)),
] + [("tooth%d" % i, "gunModel", (6.0 + 0.5 * i, -4.45, -0.3), (6.2 + 0.5 * i, -4.3, 0.0)) for i in range(6)]
EXPECT = {"buttpad": "buttpad", "stock": "stock", "receiverTop": "receiver", "receiverLow": "receiver",
          "rivet": "receiver", "grip": "grip", "guardBottom": "trigger_guard", "guardFront": "trigger_guard",
          "trigger": "trigger", "magwell": "magwell", "mag": "magazine", "bolt": "bolt", "rearSight": "rear_sight",
          "handguard": "handguard", "barrel": "barrel", "muzzle": "muzzle", "frontSight": "front_sight"}


def synth_gun():
    parts = [gunmodel.part(n, g, (40, 40, 40), a, b) for n, g, a, b in SYNTH]
    text = gunmodel.bmodel({"flamePos": (15.9, -4.2, -0.15)}, parts)
    gun = study.Gun("synthrifle", "rifle", text, None, True)
    names = {"%s%d" % (g, i): n for g in ("gunModel", "ammoModel", "slideModel") for i, n in
             enumerate(n for n, gg, _, _ in SYNTH if gg == g)}
    return gun, names


class LabelTest(unittest.TestCase):
    def test_every_area_of_a_rifle(self):
        gun, names = synth_gun()
        labels = dataset.label_gun(gun)
        got = {names[p.name]: lab for p, lab in zip(gun.parts, labels)}
        for name, area in EXPECT.items():
            self.assertEqual(got[name][0], area, name)
        for i in range(6):
            self.assertEqual(got["tooth%d" % i][0], "rail", i)
        self.assertTrue(got["rivet"][1])                       # a detail of the receiver
        self.assertFalse(got["receiverTop"][1])

    def test_names_win_over_position(self):
        gun, names = synth_gun()
        for p in gun.parts:
            if names[p.name] == "handguard":
                p.name = "defaultStockModel0"                  # Decimation's own name says stock
        labels = dict(zip([p.name for p in gun.parts], dataset.label_gun(gun)))
        self.assertEqual(labels["defaultStockModel0"][0], "stock")
        labels = dict(zip([p.name for p in gun.parts], dataset.label_gun(gun, names=False)))
        self.assertEqual(labels["defaultStockModel0"][0], "handguard")

    def test_anchors(self):
        gun, _ = synth_gun()
        a = dataset.anchors(dataset.shapes_of_gun(gun), gun.header, gun.cat)
        self.assertAlmostEqual(a["bore"], -3.35, places=6)
        self.assertAlmostEqual(a["grip"][0], -5.0, delta=0.15)
        self.assertAlmostEqual(a["grip"][1], -3.6, delta=0.15)
        self.assertEqual([round(v, 2) for v in a["mag"][0]], [0.0, 3.0])


class DensityTest(unittest.TestCase):
    def test_rows_per_area(self):
        """Details (study.detail_parts) per area and per unit of the area's length, next to the category's medians."""
        gun, names = synth_gun()
        medians = {("rifle", "receiver"): {"details": 19.0, "density": 1.83}}
        rows = {r["area"]: r for r in dataset.density(gun, medians)}
        rec = rows["receiver"]
        self.assertEqual(rec["details"], 1)                         # the rivet
        self.assertAlmostEqual(rec["density"], 1 / 10.0, delta=0.01)  # 10 long
        self.assertEqual(rec["medianDetails"], 19.0)
        self.assertEqual(rec["medianDensity"], 1.83)
        self.assertIsNone(rows["stock"]["medianDensity"])


class StraightStockTest(unittest.TestCase):
    def test_a_butt_is_not_a_grip(self):
        """A rifle without a pistol grip (a lever or pump action, the Colt Lightning sample): its straight stock's butt
        hangs deepest, but it is the stock, not a grip."""
        boxes = [("stock", (-11.0, -4.6, -0.5), (-1.5, -1.0, 0.3)), ("butt", (-10.6, -1.0, -0.5), (-8.0, 1.5, 0.3)),
                 ("receiver", (-1.5, -4.8, -0.4), (3.5, -3.2, 0.2)), ("barrel", (3.5, -4.4, -0.3), (23.0, -3.9, 0.1)),
                 ("tube", (3.5, -3.9, -0.3), (20.0, -3.4, 0.1)), ("guard", (0.0, -3.2, -0.2), (1.6, -2.5, 0.0))]
        shapes = [dataset.Shape(n, "gunModel", {k: [b[i] if k[i] else a[i] for i in range(3)] for k in fit.CORNERS})
                  for n, a, b in boxes]
        a = dataset.anchors(shapes, {"flamePos": (22.9, -5.0, -0.15)}, "rifle")
        self.assertTrue(a["grip"] is None or a["grip"][0] > -4.0, a["grip"])
        labels = dict(zip([n for n, _, _ in boxes], dataset.label_shapes(shapes, {"flamePos": (22.9, -5.0, -0.15)},
                                                                        "rifle")))
        self.assertIn(labels["butt"][0], ("stock", "buttpad"))
        self.assertEqual(labels["stock"][0], "stock")


class ExemplarTest(unittest.TestCase):
    def test_exemplar_of_an_area(self):
        gun, names = synth_gun()
        ex = dataset.exemplars(gun, dataset.label_gun(gun))
        mag = ex["magazine"]
        self.assertEqual([[round(v, 3) for v in ab] for ab in mag["box"]], [[0.0, 3.0], [-2.1, 3.0], [-0.45, 0.15]])
        self.assertEqual(mag["axis"], 1)                        # taller than long: along y
        self.assertEqual(len(mag["profile"]), dataset.STATIONS)
        self.assertEqual(mag["count"], 1)
        self.assertEqual(len(mag["parts"][0]["c"]), 8)
        self.assertEqual(mag["parts"][0]["group"], "ammoModel")
        self.assertEqual(ex["receiver"]["details"], 1)          # the rivet
        self.assertEqual(ex["rail"]["count"], 6)


@unittest.skipUnless(JAR, "needs Decimation.jar")
class JarTest(unittest.TestCase):
    def test_akm_areas(self):
        import match
        g = study.load("akm")
        labels = dataset.label_gun(g)
        areas = {lab[0] for lab in labels}
        for a in ("receiver", "handguard", "barrel", "front_sight", "stock", "grip", "magazine", "bolt"):
            self.assertIn(a, areas)
        for p, lab in zip(g.parts, labels):
            if p.group == "ammoModel":
                self.assertEqual(lab[0], "magazine")
        self.assertIsNotNone(match)

    def test_rules_agree_with_decimations_own_names(self):
        """Labelled by position and size only, the parts Decimation itself named stock, grip and barrel. Target 80 %
        (spec, milestone 2); 69.6 % at first, 82.9 % (447 of 539) with the grip beside the magazine, bullpups, the
        magazine on top and barrel slices (docs/gunconv.md section 10)."""
        agree, total = dataset.name_agreement()
        self.assertGreater(total, 300)
        self.assertGreaterEqual(agree / total, 0.8, (agree, total))

    def test_build_writes_the_dataset_outside_the_repository(self):
        out = tempfile.mkdtemp()
        try:
            info = dataset.build(out, names=["akm", "m4a4", "uzi"])
            path = os.path.join(out, "decimation_areas.json")
            with open(path) as f:
                data = json.load(f)
            self.assertEqual(sorted(data["guns"]), ["akm", "m4a4", "uzi"])
            self.assertIn("magazine", data["guns"]["akm"]["areas"])
            part = data["guns"]["akm"]["areas"]["magazine"]["parts"][0]
            self.assertEqual(len(part["tone"]), 3)              # tones read from the texture
            self.assertEqual(info["guns"], 3)
            rows = dataset.aggregate(data)
            self.assertTrue(any(r["category"] == "rifle" and r["area"] == "magazine" for r in rows))
            # aggregate numbers only: no corners, no per part lists
            self.assertTrue(all(isinstance(v, (int, float, str)) for r in rows for v in r.values()))
        finally:
            shutil.rmtree(out, ignore_errors=True)


if __name__ == "__main__":
    unittest.main()

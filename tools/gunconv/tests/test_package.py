"""The gun package (docs/superpowers/specs/2026-10-22-gun-converter-design.md, "The gun package (the contract)")."""
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
import convert  # noqa: E402
import meshio  # noqa: E402
import package  # noqa: E402
import synth  # noqa: E402

JAR = os.path.exists(os.path.join(ROOT, "Decimation.jar"))
SOURCE = {"file": "synthgun.glb", "author": "gunconv tests", "url": "tools/gunconv/tests/synth.py", "license": "own"}


def contract_files(name, cat):
    return sorted([
        "assets/deci/models/guns/%s/%s.bmodel" % (cat, name),
        "assets/deci/textures/model/guns/%s/%s/%s.png" % (cat, name, name),
        "assets/deci/textures/items/gun/%s/%s.png" % (cat, name),
        "assets/deci/animations/%s/%sFire.anib" % (name, name),
        "assets/deci/animations/%s/%sRack.anib" % (name, name),
        "assets/deci/animations/%s/%sReload1.anib" % (name, name),
        "assets/deci/animations/%s/%sSlideBack.anib" % (name, name),
        "gun.json",
    ])


def listing(d):
    out = []
    for base, _, files in os.walk(d):
        for f in files:
            out.append(os.path.relpath(os.path.join(base, f), d).replace(os.sep, "/"))
    return sorted(out)


@unittest.skipUnless(JAR, "needs Decimation.jar for the like gun")
class PackageTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        g = synth.glb_of(synth.rotated(synth.synth_gun(), synth.ROTATIONS["identity"], scale=0.05))
        cls.conv = convert.convert(meshio.read_bytes(g.bytes(), "synthgun.glb"), name="synthgun", like="akm",
                                   length=28.0)
        cls.out = tempfile.mkdtemp()
        cls.res = package.export(cls.conv, cls.out, display="Synth Gun", source=SOURCE)
        cls.dir = os.path.join(cls.out, "synthgun")

    @classmethod
    def tearDownClass(cls):
        shutil.rmtree(cls.out, ignore_errors=True)

    def test_exact_files(self):
        self.assertEqual(listing(self.dir), contract_files("synthgun", "rifle"))
        self.assertEqual(self.res["dir"], self.dir)
        self.assertEqual(self.res["files"], contract_files("synthgun", "rifle"))

    def test_gun_json(self):
        with open(os.path.join(self.dir, "gun.json")) as f:
            j = json.load(f)
        self.assertEqual(j["name"], "synthgun")
        self.assertEqual(j["display"], "Synth Gun")
        self.assertEqual(j["category"], "rifle")
        self.assertEqual(j["like"], "akm")
        self.assertEqual(j["magazine"], "like")
        self.assertIsInstance(j["stats"], dict)
        pts = j["points"]
        self.assertEqual(len(pts["flame"]), 3)
        self.assertTrue(all(isinstance(c, (int, float)) for c in pts["flame"]))
        self.assertIsInstance(pts["railTop"], (int, float))
        for k in ("rail", "gripRail"):
            self.assertTrue(pts[k] is None or (len(pts[k]) == 2 and pts[k][0] < pts[k][1]), k)
        self.assertIn(pts["mount"], ("rail", "side", "slide", None))
        self.assertEqual(j["source"], SOURCE)

    def test_model_parses_like_the_game_reads_it(self):
        import study
        with open(os.path.join(self.dir, "assets/deci/models/guns/rifle/synthgun.bmodel")) as f:
            text = f.read()
        g = study.Gun("synthgun", "rifle", text, None, True)
        self.assertEqual(len(g.parts), len(self.conv.parts))
        self.assertNotIn("addBox", text)
        akm = study.load("akm")
        self.assertEqual([round(c, 4) for c in g.header["rhPos"]], [round(c, 4) for c in akm.header["rhPos"]])
        with open(os.path.join(self.dir, "gun.json")) as f:
            j = json.load(f)
        self.assertEqual([round(c, 4) for c in g.header["flamePos"]], [round(c, 4) for c in j["points"]["flame"]])
        groups = {p.group for p in g.parts}
        self.assertTrue({"gunModel", "ammoModel", "slideModel"} <= groups, groups)

    def test_animations_on_decimations_timings(self):
        lengths = {}
        for a in ("Fire", "Rack", "Reload1", "SlideBack"):
            with open(os.path.join(self.dir, "assets/deci/animations/synthgun/synthgun%s.anib" % a)) as f:
                text = f.read()
            lengths[a] = int(text.split("\n")[0].split(":")[1])
            if a == "SlideBack":
                self.assertIn("STATIC", text)
        self.assertEqual(lengths, {"Fire": 2, "Rack": 19, "Reload1": 57, "SlideBack": 1})

    def test_icon_and_texture(self):
        from PIL import Image
        icon = Image.open(os.path.join(self.dir, "assets/deci/textures/items/gun/rifle/synthgun.png"))
        self.assertEqual(icon.size, (32, 32))
        self.assertEqual(icon.mode, "RGBA")
        tex = Image.open(os.path.join(self.dir, "assets/deci/textures/model/guns/rifle/synthgun/synthgun.png"))
        self.assertEqual(tex.size[0], 1024)

    def test_reexport_replaces_old_files(self):
        stray = os.path.join(self.dir, "assets", "deci", "stray.txt")
        with open(stray, "w") as f:
            f.write("old")
        package.export(self.conv, self.out, display="Synth Gun", source=SOURCE)
        self.assertFalse(os.path.exists(stray))
        self.assertEqual(listing(self.dir), contract_files("synthgun", "rifle"))

    def test_license_is_required(self):
        bad = dict(SOURCE, license="All rights reserved")
        with self.assertRaises(ValueError):
            package.export(self.conv, tempfile.mkdtemp(), display="X", source=bad)
        with self.assertRaises(ValueError):
            package.export(self.conv, tempfile.mkdtemp(), display="X", source=None)
        for ok in ("CC0 1.0", "CC BY 3.0", "CC-BY-4.0", "own"):
            self.assertTrue(package.license_ok(ok), ok)

    def test_parts_json_round_trip(self):
        data = convert.parts_to_json(self.conv.parts)
        back = convert.parts_from_json(json.loads(json.dumps(data)))
        self.assertEqual(len(back), len(self.conv.parts))
        for a, b in zip(self.conv.parts, back):
            self.assertEqual((a.name, a.group, tuple(a.colour), a.label), (b.name, b.group, tuple(b.colour), b.label))
            for k in a.c:
                for i in range(3):
                    self.assertAlmostEqual(a.c[k][i], b.c[k][i], places=4)


if __name__ == "__main__":
    unittest.main()

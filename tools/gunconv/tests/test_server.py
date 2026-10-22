"""The private local page's server: static files, samples, convert, reshape, upload, export (127.0.0.1 only)."""
import json
import os
import shutil
import sys
import tempfile
import threading
import unittest
import urllib.error
import urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
TOOLS = os.path.dirname(os.path.dirname(HERE))
ROOT = os.path.dirname(TOOLS)
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.dirname(HERE))
sys.path.insert(0, os.path.join(TOOLS, "guns"))
import server  # noqa: E402
import synth  # noqa: E402

JAR = os.path.exists(os.path.join(ROOT, "Decimation.jar"))
SOURCE = {"file": "synthgun.glb", "author": "gunconv tests", "url": "tools/gunconv/tests/synth.py", "license": "own"}


class ServerTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.tmp = tempfile.mkdtemp()
        inputs = os.path.join(cls.tmp, "inputs", "synth")
        os.makedirs(inputs)
        g = synth.glb_of(synth.rotated(synth.synth_gun(), synth.ROTATIONS["muzzle -x"], scale=0.05))
        with open(os.path.join(inputs, "synthgun.glb"), "wb") as f:
            f.write(g.bytes())
        with open(os.path.join(inputs, "source.json"), "w") as f:
            json.dump(SOURCE, f)
        cls.srv = server.make_server(0, inputs=os.path.join(cls.tmp, "inputs"), out=os.path.join(cls.tmp, "out"),
                                     work=os.path.join(cls.tmp, "work"))
        cls.base = "http://127.0.0.1:%d" % cls.srv.server_port
        cls.thread = threading.Thread(target=cls.srv.serve_forever, daemon=True)
        cls.thread.start()

    @classmethod
    def tearDownClass(cls):
        cls.srv.shutdown()
        cls.srv.server_close()
        shutil.rmtree(cls.tmp, ignore_errors=True)

    def get(self, path):
        with urllib.request.urlopen(self.base + path, timeout=60) as r:
            return r.status, r.headers.get("Content-Type", ""), r.read()

    def post(self, path, obj=None, raw=None):
        data = raw if raw is not None else json.dumps(obj).encode()
        req = urllib.request.Request(self.base + path, data=data, method="POST",
                                     headers={"Content-Type": "application/octet-stream" if raw else "application/json"})
        with urllib.request.urlopen(req, timeout=120) as r:
            return json.loads(r.read())

    def test_binds_to_localhost_only(self):
        self.assertEqual(self.srv.server_address[0], "127.0.0.1")

    def test_page_and_static_files(self):
        status, ctype, body = self.get("/")
        self.assertEqual(status, 200)
        self.assertIn("text/html", ctype)
        for word in (b"Convert", b"Export", b"importmap", b"three@"):
            self.assertIn(word, body)
        for f in ("/app.js", "/style.css"):
            self.assertEqual(self.get(f)[0], 200)

    def test_samples_and_files(self):
        _, _, body = self.get("/api/samples")
        samples = json.loads(body)["samples"]
        self.assertEqual([s["path"] for s in samples], ["synth/synthgun.glb"])
        self.assertEqual(samples[0]["source"]["license"], "own")
        status, _, data = self.get("/api/file?path=synth/synthgun.glb")
        self.assertEqual(data[:4], b"glTF")
        with self.assertRaises(urllib.error.HTTPError) as e:
            self.get("/api/file?path=../../etc/passwd")
        e.exception.close()

    def test_upload_stays_in_its_folder(self):
        r = self.post("/api/upload?filename=" + "..%2F..%2Fevil.obj", raw=b"o A\nv 0 0 0\nv 1 0 0\nv 0 1 0\nf 1 2 3\n")
        self.assertTrue(r["path"].startswith("uploads/"), r)
        self.assertNotIn("..", r["path"])

    @unittest.skipUnless(JAR, "needs Decimation.jar for the like gun")
    def test_convert_reshape_export(self):
        conv = self.post("/api/convert", {"sample": "synth/synthgun.glb", "name": "synthgun", "like": "akm",
                                          "length": 28})
        self.assertEqual(conv["like"], "akm")
        self.assertGreater(len(conv["parts"]), 10)
        part = conv["parts"][0]
        self.assertEqual(set(part) >= {"name", "group", "colour", "corners", "kind", "label"}, True)
        self.assertEqual(len(part["corners"]), 8)
        self.assertEqual(len(conv["matrix"]), 4)
        r = self.post("/api/reshape", {"part": part, "kind": "cuboid"})
        self.assertEqual(r["parts"][0]["kind"], "cuboid")
        r = self.post("/api/reshape", {"part": part, "kind": "octagon"})
        self.assertEqual(len(r["parts"]), 3)
        conv["display"] = "Synth Gun"
        conv["parts"] = conv["parts"][1:]                    # an edit: one part dropped
        res = self.post("/api/export", conv)
        self.assertTrue(os.path.isfile(os.path.join(res["dir"], "gun.json")))
        with open(os.path.join(res["dir"], "gun.json")) as f:
            j = json.load(f)
        self.assertEqual(j["source"]["license"], "own")
        self.assertEqual(res["parts"], len(conv["parts"]))
        self.assertIn("metrics", res)

    @unittest.skipUnless(JAR, "needs Decimation.jar for the like gun")
    def test_exact_mode(self):
        """The page's Exact button: the faces 1:1 as plates within the budget, the estimated cost with its verdict; an
        export over twice the cost budget (the parts of an edit counted again) is refused."""
        conv = self.post("/api/convert", {"sample": "synth/synthgun.glb", "name": "synthexact", "like": "akm",
                                          "length": 28, "mode": "exact", "budget": 300})
        st = conv["stats"]
        self.assertEqual(st["mode"], "exact")
        self.assertLessEqual(st["faces"], 300)
        self.assertEqual(len(conv["parts"]), st["faces"])
        self.assertIn(st["cost"]["verdict"], ("ok", "warn", "refuse"))
        self.assertIn("drawMs", st["cost"])
        res = self.post("/api/export", conv)
        self.assertTrue(os.path.isfile(os.path.join(res["dir"], "gun.json")))
        heavy = dict(conv, parts=conv["parts"] * (1200 // len(conv["parts"]) + 1))   # over 1,140 parts: refused
        with self.assertRaises(Exception):
            self.post("/api/export", heavy)


if __name__ == "__main__":
    unittest.main()

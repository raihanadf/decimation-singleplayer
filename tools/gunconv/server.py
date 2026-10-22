#!/usr/bin/env python3
"""The converter's private local page (docs/gunconv.md). Serves tools/gunconv/web on 127.0.0.1 only; never published.

    python3 tools/gunconv/server.py [--port 8790]        then open http://127.0.0.1:8790/

API (JSON):
    GET  /api/samples                 models under build/gunconv/inputs/<folder>/ with their source.json credit
    GET  /api/file?path=REL           a sample file (inputs) or an upload (uploads/<file>), for the source view
    GET  /api/mesh?path=REL           a model's triangles as the converter reads them (for .bbmodel and as a fallback)
    GET  /api/gun?name=GUN            a Decimation gun's parts (corners and groups, no texture): the like gun overlay
    POST /api/upload?filename=F       body = the file; stored under build/gunconv/work/uploads/
    POST /api/convert                 {sample | upload, name, display, like, category, length, lengthMm, muzzle, up,
                                       mirror, source, mode}: mode "mesh" (the default, the page's one Convert:
                                       the source's triangles 1:1 with a pixel texture; pieces, a preview of the
                                       mesh and a token: the mesh stays here for the export), or for the command
                                       line's older ways "style" (Decimation style boxes) and "exact" (+ budget)
    POST /api/reshape                 {part, kind}: the part as another kind (edit.py)
    POST /api/export                  the (edited) conversion: writes build/gunconv/out/<name>/, returns files and
                                       (box modes) the study.py gaps rows against Decimation's guns of the category;
                                       a mesh conversion by its token, with the page's groups, name and points
"""
import argparse
import collections
import json
import os
import re
import sys
import threading
import time
import traceback
import urllib.parse
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(os.path.dirname(HERE), "guns"))
import convert  # noqa: E402
import edit  # noqa: E402
import match  # noqa: E402
import meshio  # noqa: E402
import package  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(HERE))
WEB = os.path.join(HERE, "web")
BUILD = os.path.join(ROOT, "build", "gunconv")
MODEL_EXT = (".glb", ".gltf", ".obj", ".bbmodel")
STATIC = {"/": ("index.html", "text/html; charset=utf-8"), "/index.html": ("index.html", "text/html; charset=utf-8"),
          "/app.js": ("app.js", "text/javascript; charset=utf-8"), "/style.css": ("style.css", "text/css; charset=utf-8"),
          "/fonts/MinecraftStandard.otf": ("fonts/MinecraftStandard.otf", "font/otf")}   # public domain (its readme)
# The DeciRemnant site's art (user, 23 Oktober 2026: the page in the site's look): its camo and torn buttons are Decimation's
# textures and stay in the site's private repo; served from there when it is on this Mac (DECIREMNANT_SITE overrides).
SITE_UI = os.path.join(os.environ.get("DECIREMNANT_SITE", os.path.expanduser("~/Projects/deciremnant")), "public", "ui")
UI_NAME = re.compile(r"^/ui/([a-z0-9-]{1,64}\.(?:webp|png))$")
UI_TYPES = {"webp": "image/webp", "png": "image/png"}
MAX_UPLOAD = 64 * 1024 * 1024


class BadRequest(Exception):
    pass


def under(base, rel):
    """base/rel when it stays inside base, else BadRequest."""
    path = os.path.normpath(os.path.join(base, urllib.parse.unquote(rel)))
    if not path.startswith(os.path.normpath(base) + os.sep):
        raise BadRequest("path outside its folder: %r" % rel)
    return path


MESHES = collections.OrderedDict()     # token -> a mesh mode Conversion (its mesh is too large for the page to send back)
MESHES_LOCK = threading.Lock()
KEEP = 4


class Handler(BaseHTTPRequestHandler):
    site_ui = SITE_UI
    inputs = os.path.join(BUILD, "inputs")
    out = os.path.join(BUILD, "out")
    work = os.path.join(BUILD, "work")

    def log_message(self, fmt, *args):
        sys.stderr.write("gunconv %s\n" % (fmt % args))

    # ------------------------------------------------------------ plumbing

    def send(self, status, body, ctype):
        self.send_response(status)
        self.send_header("Content-Type", ctype)
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        self.wfile.write(body)

    def json(self, obj, status=200):
        self.send(status, json.dumps(obj).encode(), "application/json")

    def body(self):
        n = int(self.headers.get("Content-Length") or 0)
        if n > MAX_UPLOAD:
            raise BadRequest("file too large (over 64 MB)")
        return self.rfile.read(n) if n else b""

    def resolve(self, rel):
        """A model path the page names: uploads/<file> in the work folder, else a sample under inputs."""
        if rel.startswith("uploads/"):
            return under(os.path.join(self.work, "uploads"), rel[len("uploads/"):])
        return under(self.inputs, rel)

    def run(self, fn):
        try:
            fn()
        except (BadRequest, ValueError, meshio.MeshError, KeyError) as e:
            self.json({"error": str(e)}, 400)
        except FileNotFoundError as e:
            self.json({"error": "not found: %s" % e.filename}, 404)
        except Exception as e:                           # a converter bug: the page shows it, the console has the trace
            traceback.print_exc()
            self.json({"error": "%s: %s" % (type(e).__name__, e)}, 500)

    # ------------------------------------------------------------ GET

    def do_GET(self):
        url = urllib.parse.urlparse(self.path)
        q = urllib.parse.parse_qs(url.query)
        if url.path in STATIC:
            name, ctype = STATIC[url.path]
            with open(os.path.join(WEB, name), "rb") as f:
                return self.send(200, f.read(), ctype)
        m = UI_NAME.match(url.path)
        if m:
            path = os.path.join(self.site_ui, m.group(1))
            if os.path.isfile(path):
                with open(path, "rb") as f:
                    return self.send(200, f.read(), UI_TYPES[m.group(1).rsplit(".", 1)[1]])
            return self.json({"error": "no such picture"}, 404)
        routes = {"/api/samples": self.samples, "/api/file": lambda: self.file(q), "/api/mesh": lambda: self.mesh(q),
                  "/api/gun": lambda: self.gun(q)}
        if url.path in routes:
            return self.run(routes[url.path])
        self.json({"error": "no such page"}, 404)

    def samples(self):
        out = []
        if os.path.isdir(self.inputs):
            for d in sorted(os.listdir(self.inputs)):
                folder = os.path.join(self.inputs, d)
                if not os.path.isdir(folder):
                    continue
                for f in sorted(os.listdir(folder)):
                    if f.lower().endswith(MODEL_EXT):
                        out.append({"path": "%s/%s" % (d, f), "name": d, "file": f,
                                    "source": package.source_for(os.path.join(folder, f))})
        self.json({"samples": out, "jar": match.jar_ok(), "categories": list(convert.CATEGORIES)})

    def file(self, q):
        path = self.resolve(q.get("path", [""])[0])
        with open(path, "rb") as f:
            self.send(200, f.read(), "application/octet-stream")

    def mesh(self, q):
        m = meshio.load(self.resolve(q.get("path", [""])[0]))
        self.json({"meshes": [{"name": x.name, "colour": list(x.colour),
                               "verts": [round(c, 5) for v in x.verts for c in v],
                               "faces": [i for f in x.faces for i in f]} for x in m.meshes]})

    def gun(self, q):
        name = q.get("name", [""])[0]
        g = match.guns().get(name)
        if g is None:
            raise BadRequest("no Decimation gun %r" % name)
        self.json({"name": name, "category": g.cat,
                   "parts": [{"group": p.group, "verts": [[round(c, 4) for c in v] for v in p.verts()]} for p in g.parts]})

    # ------------------------------------------------------------ POST

    def do_POST(self):
        url = urllib.parse.urlparse(self.path)
        q = urllib.parse.parse_qs(url.query)
        routes = {"/api/upload": lambda: self.upload(q), "/api/convert": self.convert, "/api/reshape": self.reshape,
                  "/api/export": self.export}
        if url.path in routes:
            return self.run(routes[url.path])
        self.json({"error": "no such action"}, 404)

    def upload(self, q):
        name = os.path.basename(urllib.parse.unquote(q.get("filename", ["model"])[0]).replace("\\", "/"))
        name = re.sub(r"[^A-Za-z0-9._]", "_", name).lstrip(".") or "model"
        if not name.lower().endswith(MODEL_EXT):
            raise BadRequest("only %s files" % ", ".join(MODEL_EXT))
        folder = os.path.join(self.work, "uploads")
        os.makedirs(folder, exist_ok=True)
        with open(os.path.join(folder, name), "wb") as f:
            f.write(self.body())
        self.json({"path": "uploads/" + name})

    def convert(self):
        req = json.loads(self.body() or b"{}")
        rel = req.get("sample") or req.get("upload")
        if not rel:
            raise BadRequest("name a sample or an upload")
        path = self.resolve(rel)
        src = package.source_for(path)
        src.update({k: v for k, v in (req.get("source") or {}).items() if k in ("author", "url", "license") and v})
        t = time.time()
        kw = dict(name=req.get("name") or None, display=req.get("display") or None,
                  category=req.get("category") or None, like=req.get("like") or None,
                  length=req.get("length") or None, length_mm=req.get("lengthMm") or None,
                  muzzle=req.get("muzzle") or None, up=req.get("up") or None, mirror=bool(req.get("mirror")),
                  source=src)
        mode = req.get("mode") or "mesh"
        if mode == "exact":                              # box plates of the source's faces (exact.py), command line
            conv = convert.convert_exact(path, budget=req.get("budget") or None, **kw)
        elif mode == "style":                            # Decimation style boxes (milestones 1 to 3), command line
            conv = convert.convert(path, **kw)
        elif mode == "mesh":
            conv = convert.convert_mesh(path, **kw)
        else:
            raise BadRequest("mode %r: mesh, style or exact" % mode)
        out = conv.to_json()
        if mode == "mesh":
            token = os.urandom(8).hex()
            with MESHES_LOCK:
                MESHES[token] = conv
                while len(MESHES) > KEEP:
                    MESHES.popitem(last=False)
            out["token"] = token
        out["path"] = rel
        out["seconds"] = round(time.time() - t, 2)
        self.json(out)

    def reshape(self):
        req = json.loads(self.body() or b"{}")
        part = convert.parts_from_json([req["part"]])[0]
        self.json({"parts": convert.parts_to_json(edit.reshape(part, req.get("kind", "")))})

    def export(self):
        req = json.loads(self.body() or b"{}")
        if (req.get("stats") or {}).get("mode") == "mesh":
            with MESHES_LOCK:
                kept = MESHES.get(req.get("token"))
            if kept is None:
                raise BadRequest("this conversion is no longer kept by the server (restarted, or 4 newer ones): convert again")
            conv = kept
            conv.name = req.get("name") or conv.name
            conv.display = req.get("display") or conv.display
            conv.points = req.get("points") or conv.points
            groups = {p["id"]: p["group"] for p in req.get("pieces", []) if "id" in p and "group" in p}
            conv.pieces = [dict(p, group=groups.get(p["id"], p["group"])) for p in conv.pieces]
            self.json(dict(package.export(conv, self.out, display=req.get("display"), source=req.get("source")),
                           metrics=[]))
            return
        conv = convert.Conversion.from_json(req)
        res = package.export(conv, self.out, display=req.get("display"), source=req.get("source"))
        rows = []
        try:
            rows = [{"metric": r[0], "ours": r[1], "median": r[2], "q10": r[3], "q90": r[4], "inside": r[5]}
                    for r in package.gaps(package.built_gun(res["dir"]))]
        except Exception:                                # no jar: the package is still written
            traceback.print_exc()
        res["metrics"] = rows
        self.json(res)


def make_server(port=8790, inputs=None, out=None, work=None, host="127.0.0.1", site_ui=None):
    attrs = {}
    for k, v in (("inputs", inputs), ("out", out), ("work", work), ("site_ui", site_ui)):
        if v:
            attrs[k] = os.path.abspath(v)
    handler = type("GunconvHandler", (Handler,), attrs)
    return ThreadingHTTPServer((host, port), handler)


def main():
    ap = argparse.ArgumentParser(description="The gun converter's private local page (127.0.0.1 only).")
    ap.add_argument("--port", type=int, default=8790)
    a = ap.parse_args()
    srv = make_server(a.port)
    print("gun converter on http://127.0.0.1:%d/ (inputs %s, packages %s)" % (srv.server_port, Handler.inputs,
                                                                             Handler.out))
    try:
        srv.serve_forever()
    except KeyboardInterrupt:
        pass


if __name__ == "__main__":
    main()

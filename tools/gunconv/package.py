#!/usr/bin/env python3
"""The gun package (docs/superpowers/specs/2026-10-22-gun-converter-design.md, "The gun package (the contract)"):
one folder per gun, written to build/gunconv/out/<name>/ (git ignored), read by the game side (agent 2):

    assets/deci/models/guns/<category>/<name>.bmodel
    assets/deci/textures/model/guns/<category>/<name>/<name>.png      model texture
    assets/deci/textures/items/gun/<category>/<name>.png              icon
    assets/deci/animations/<name>/<name>Fire.anib, Rack, Reload1, SlideBack
    gun.json  {name, display, category, like, magazine, stats, points {flame, railTop, rail, gripRail, mount},
               source {file, author, url, license}}  (+ optional "converter": what the converter saw)

The model, texture, icon and animations come from tools/guns/gunmodel.py (our pipeline, Decimation's timings), so a
converted gun is built exactly like our hand made guns. Only sources we may ship are exported: CC0, CC BY (credit
kept in gun.json) or our own.
"""
import json
import os
import re
import shutil
import statistics
import sys
import threading
import zipfile
from io import BytesIO

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(os.path.dirname(HERE), "guns"))
import convert  # noqa: E402
import gunmodel  # noqa: E402
import match  # noqa: E402
import study  # noqa: E402

LOCK = threading.Lock()       # gunmodel.icon reads study.OURS: one build at a time
ANIMS = ("Fire", "Rack", "Reload1", "SlideBack")


def license_ok(text):
    """CC0 (any version), CC BY (any version, no NC / ND / SA), public domain, or our own work."""
    if not text:
        return False
    t = re.sub(r"\s+", " ", re.sub(r"[-_]+", " ", str(text).strip().lower()))
    if t.startswith(("cc0", "cc zero", "public domain")):
        return True
    return bool(re.match(r"^(own|cc by( \d(\.\d)?)?( international)?)$", t))


def source_for(model_path):
    """The credit of a model file: source.json beside it ({"author", "url", "license"}), the file's own name."""
    src = {"file": os.path.basename(model_path), "author": None, "url": None, "license": None}
    p = os.path.join(os.path.dirname(os.path.abspath(model_path)), "source.json")
    if os.path.exists(p):
        try:
            with open(p) as f:
                data = json.load(f)
            src.update({k: data[k] for k in ("author", "url", "license") if k in data})
        except (OSError, ValueError):
            pass
    return src


def animations(category, parts):
    """Fire / Rack / Reload1 / SlideBack on Decimation's timings with our values (gunmodel.smg_animations, accepted
    by the user on the MAC-10); the bolt kicks back about 2.4 on rifles, as Decimation's (style guide section 10)."""
    return gunmodel.smg_animations(parts, slide=convert.SLIDE_KICK.get(category, 2.4))


def listing(d):
    out = []
    for base, _, files in os.walk(d):
        for f in files:
            out.append(os.path.relpath(os.path.join(base, f), d).replace(os.sep, "/"))
    return sorted(out)


def gun_json(conv, display, source):
    return {
        "name": conv.name, "display": display, "category": conv.category, "like": conv.like, "magazine": "like",
        "stats": {}, "points": conv.points, "source": source,
        "converter": {"version": 2, "parts": len(conv.parts), "likeCandidates": conv.candidates[:5],
                      "warnings": conv.warnings, "exemplars": conv.stats.get("exemplars", {}),
                      "byGun": conv.stats.get("byGun", {}), "fromExemplars": conv.stats.get("fromExemplars", 0),
                      "fromFit": conv.stats.get("fromFit", len(conv.parts))},
    }


def export(conv, out_root, display=None, source=None):
    """Write the package of a Conversion to out_root/<name>/ (an old folder of that name is replaced whole)."""
    name = convert.validate_name(conv.name)
    if conv.category not in convert.CATEGORIES:
        raise ValueError("unknown category %r" % conv.category)
    source = source if source is not None else conv.source
    if not source or not license_ok(source.get("license")):
        raise ValueError("the source model needs a license we may ship (CC0, CC BY or own): %r"
                         % ((source or {}).get("license"),))
    if not source.get("author") or not source.get("file"):
        raise ValueError("the source model needs its author and file name (credit)")
    if not conv.parts:
        raise ValueError("no parts to export")
    display = display or conv.display or name
    out_dir = os.path.join(os.path.abspath(out_root), name)
    with LOCK:
        if os.path.isdir(out_dir):
            shutil.rmtree(out_dir)
        assets = os.path.join(out_dir, "assets", "deci")
        os.makedirs(assets)
        spec = {"name": name, "category": conv.category, "parts": conv.parts,
                "animations": animations(conv.category, conv.parts)}
        for k in ("flamePos", "ejectPos", "rhPos", "rhRot", "lhPos", "lhRot"):
            if conv.header.get(k) is not None:
                spec[k] = tuple(conv.header[k])
        spec["flamePos"] = tuple(conv.points["flame"])
        old = study.OURS
        study.OURS = assets
        try:
            gunmodel.build(spec, assets)
            if conv.like and match.jar_ok():
                icon_like(name, conv.like, os.path.join(assets, "textures", "items", "gun", conv.category, name + ".png"))
        finally:
            study.OURS = old
        with open(os.path.join(out_dir, "gun.json"), "w") as f:
            json.dump(gun_json(conv, display, source), f, indent=2)
            f.write("\n")
    return {"dir": out_dir, "files": listing(out_dir), "parts": len(conv.parts)}


def like_icon_box(like):
    """The opaque box (x0, y0, x1, y1) of the like gun's 32 x 32 icon in the jar, or None."""
    from PIL import Image
    g = match.guns()[like]
    path = "assets/deci/textures/items/gun/%s/%s.png" % (g.cat, like)
    with zipfile.ZipFile(study.JAR) as z:
        if path not in z.namelist():
            return None
        img = Image.open(BytesIO(z.read(path))).convert("RGBA")
    return img.getchannel("A").getbbox()


def icon_like(name, like, path, size=32):
    """The icon in the like gun's proportions: Decimation draws rifle icons about twice as tall as true (the AKM's 32 x
    21 for a gun 3.5 times as long as high). Our side render (muzzle right, as the accepted MAC-10's) is scaled to the
    like gun's icon width and to its height times the ratio of the two guns' proportions, then outlined."""
    from PIL import Image
    box = like_icon_box(like)
    if not box:
        return
    lw, lh = box[2] - box[0], box[3] - box[1]
    info = match.info(like)
    ours = study.load("ours:" + name)
    (ox0, ox1), (oy0, oy1), _ = ours.bounds()
    like_lh, ours_lh = info["length"] / info["height"], (ox1 - ox0) / (oy1 - oy0)
    w = lw
    h = max(4, min(size - 2, int(round(lh * like_lh / ours_lh))))
    img = study.render(ours, "other", scale=24, pad=0, bg=(0, 0, 0, 0)).transpose(Image.FLIP_LEFT_RIGHT)
    small = img.resize((w, h), Image.BOX)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    ox = max(1, min(size - w - 1, (box[0] + box[2] - w) // 2)) if w < size else 0
    oy = max(1, min(size - h - 1, (box[1] + box[3] - h) // 2))
    for y in range(h):
        for x in range(w):
            r, g, b, a = small.getpixel((x, y))
            if a >= 140:
                f = 255.0 / a * 1.3
                out.putpixel((ox + x, oy + y), (min(255, int(r * f)), min(255, int(g * f)), min(255, int(b * f)), 255))
    edge = out.copy()
    for y in range(size):
        for x in range(size):
            if out.getpixel((x, y))[3] == 0 and any(0 <= x + dx < size and 0 <= y + dy < size and
                                                    out.getpixel((x + dx, y + dy))[3]
                                                    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                edge.putpixel((x, y), (10, 10, 12, 255))
    edge.save(path)


# ---------------------------------------------------------------- comparison with Decimation's guns (study.py gaps)

_REF = {}


def reference_metrics(category):
    """study.metrics of every Decimation gun of a category, textures included (one pass over the jar, cached)."""
    if category not in _REF:
        rows = []
        if match.jar_ok():
            from PIL import Image
            with zipfile.ZipFile(study.JAR) as z:
                names = set(z.namelist())
                for name, g in sorted(match.guns().items()):
                    if g.cat != category:
                        continue
                    tex = "assets/deci/textures/model/guns/%s/%s/%s.png" % (g.cat, name, name)
                    gun = study.Gun(name, g.cat, z.read("assets/deci/models/guns/%s/%s.bmodel" % (g.cat, name))
                                    .decode("latin1"), Image.open(BytesIO(z.read(tex))).convert("RGBA")
                                    if tex in names else None, False)
                    rows.append(study.metrics(gun))
        _REF[category] = rows
    return _REF[category]


def built_gun(pkg_dir):
    """The exported gun as study.py sees it (study.Gun with its texture)."""
    from PIL import Image
    with open(os.path.join(pkg_dir, "gun.json")) as f:
        j = json.load(f)
    name, cat = j["name"], j["category"]
    model = os.path.join(pkg_dir, "assets", "deci", "models", "guns", cat, name + ".bmodel")
    tex = os.path.join(pkg_dir, "assets", "deci", "textures", "model", "guns", cat, name, name + ".png")
    with open(model) as f:
        text = f.read()
    with Image.open(tex) as img:
        texture = img.convert("RGBA")
    return study.Gun(name, cat, text, texture, True)


def gaps(gun):
    """study.py gaps as rows: (metric, ours, median, q10, q90, inside) against Decimation's guns of the category."""
    refs = reference_metrics(gun.cat)
    mine = study.metrics(gun)
    rows = []
    for k, v in mine.items():
        vals = sorted(r[k] for r in refs if k in r)
        if not vals:
            continue
        lo, hi = vals[len(vals) // 10], vals[min(len(vals) - 1, len(vals) * 9 // 10)]
        rows.append((k, round(v, 3), round(statistics.median(vals), 3), round(lo, 3), round(hi, 3), lo <= v <= hi))
    return rows

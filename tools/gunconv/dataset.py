#!/usr/bin/env python3
"""The dataset of Decimation's guns (docs/superpowers/specs/2026-10-22-gun-converter-design.md, milestone 2): every
part of all 98 guns labelled with its anatomy area, and per gun and area an exemplar (box, run axis, profile, parts with
their corners, kind, group, tone, detail flag) that the converter refits onto a source gun (exemplar.py).

    python3 tools/gunconv/dataset.py build            # build/gunconv/dataset/decimation_areas.json (git ignored)
                                                      # + docs/references/decimation_gun_areas.tsv (aggregates only)
    python3 tools/gunconv/dataset.py check            # the rules against Decimation's own stock / grip / barrel names
    python3 tools/gunconv/dataset.py render GUN ...   # one colour per area, for the eye (docs/shots, git ignored)

LEGAL: the dataset is Decimation's model data in another form. It stays under build/, is made again from the jar by
this tool and is never committed; only this code, the rules and the aggregate numbers go into the repository.
"""
import json
import os
import re
import statistics
import sys
import zipfile
from io import BytesIO

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(os.path.dirname(HERE), "guns"))
import fit  # noqa: E402
import match  # noqa: E402
import study  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(HERE))
DATASET = os.path.join(ROOT, "build", "gunconv", "dataset")
AGGREGATE = os.path.join(ROOT, "docs", "references", "decimation_gun_areas.tsv")
AREAS = ("receiver", "handguard", "barrel", "muzzle", "front_sight", "rear_sight", "stock", "buttpad", "grip",
         "trigger_guard", "trigger", "magazine", "magwell", "charging_handle", "bolt", "rail", "pump", "cylinder")
DETAIL = 0.3          # a part whose longest side is under this is a detail (rivet, screw, vent, groove) of its area
STATIONS = 12         # profile stations along an area's run axis
GRIP_MAX = 4.5        # a pistol grip is at most this long
NO_STOCK = ("pistol", "revolver")
# study.Part.verts() order as (ix, iy, iz) corners of the box
STUDY_ORDER = [(1, 0, 0), (1, 1, 0), (0, 1, 0), (0, 0, 1), (1, 0, 1), (1, 1, 1), (0, 1, 1), (0, 0, 0)]
# Decimation's own part names and a source file's names, first match wins
GROUP_AREAS = {"ammoModel": "magazine", "slideModel": "bolt", "bolt": "bolt", "pumpModel": "pump",
               "revolverBarrelModel": "cylinder", "breakActionModel": "barrel", "minigunBarrelModel": "barrel"}
NAMED_AREAS = {"defaultStockModel": "stock", "defaultGripModel": "grip", "defaultBarrelModel": "barrel",
               "defaultScopeModel": "sight"}
KEYWORDS = [("magazine", r"mag|clip|cartridge|bullet"), ("charging_handle", r"charg|cocking"),
            ("bolt", r"bolt|slide|carrier"), ("sight", r"sight|iron|notch|aperture"), ("buttpad", r"buttpad|butt_?plate"),
            ("stock", r"stock|butt"), ("trigger_guard", r"guard"), ("trigger", r"trigger"), ("grip", r"grip"),
            ("muzzle", r"muzzle|brake|flash|compensat"), ("barrel", r"barrel"), ("handguard", r"handguard|forend|fore_?end"),
            ("rail", r"rail|picatinny")]


class Shape:
    """A part as the labeller sees it: its name (Decimation's part name or the source's label), group prefix, corners."""

    def __init__(self, name, group, corners):
        self.name, self.group, self.c = name, group, {k: list(v) for k, v in corners.items()}
        pts = list(self.c.values())
        self.box = [(min(q[i] for q in pts), max(q[i] for q in pts)) for i in range(3)]
        self.centre = [(a + b) / 2 for a, b in self.box]
        self.dims = [b - a for a, b in self.box]


def prefix(name):
    m = re.match(r"[A-Za-z]+", name or "")
    return m.group(0) if m else (name or "")


def shapes_of_gun(gun):
    """study.Gun parts as shapes (their verts in model space, rotations and addChild parents applied)."""
    return [Shape(p.name, prefix(p.name), {STUDY_ORDER[i]: v for i, v in enumerate(p.verts())}) for p in gun.parts]


def shapes_of_parts(parts):
    """Our gunmodel parts as shapes: the name is the source's label, the group the converter's."""
    return [Shape(getattr(p, "label", p.name), p.group, p.c) for p in parts]


# ---------------------------------------------------------------- anchors

def anchors(shapes, header, cat):
    """Where the bore, magazine, grip, stock end and receiver front are (spec, anatomy rule 1)."""
    body = [s for s in shapes if s.group != "ammoModel"] or shapes
    x0, x1 = min(s.box[0][0] for s in body), max(s.box[0][1] for s in body)
    fp = header.get("flamePos")
    bore = fp[1] + 0.85 if fp else statistics.median(s.centre[1] for s in body)
    ammo = [s for s in shapes if s.group == "ammoModel"]
    mag = [(min(s.box[i][0] for s in ammo), max(s.box[i][1] for s in ammo)) for i in range(3)] if ammo else None
    step, xs = 0.1, []
    x = x0
    while x <= x1 + 1e-9:
        xs.append(x)
        x += step
    depth = []
    for x in xs:
        bot = [s.box[1][1] for s in body if s.box[0][0] - 1e-9 <= x <= s.box[0][1] + 1e-9]
        depth.append(max(bot) - bore if bot else None)
    # the grip is looked for beside the magazine first (a magwell hanging around it is often deeper than the grip:
    # the SCARs, G36C), then with the magazine's columns too (pistols and the Uzi hold the magazine in the grip)
    grip = None
    for skip_mag in ((True, False) if mag else (False,)):
        cols = [None if (skip_mag and mag[0][0] - 0.2 <= x <= mag[0][1] + 0.2) else d for x, d in zip(xs, depth)]
        grip = find_grip(xs, cols, x0, cat)
        if grip:
            break
    if cat in NO_STOCK:
        stock_end = x0 - 1.0
    else:
        stock_end = grip[0] - 0.1 if grip else x0 + (x1 - x0) / 3
    if mag and (grip is None or mag[0][0] >= grip[1] - 0.5):
        front = mag[0][1] + min(2.5, max(0.6, 0.12 * (x1 - mag[0][1])))
    elif grip:
        front = grip[1] + 0.35 * (x1 - grip[1])
    else:
        front = x0 + 0.5 * (x1 - x0)
    zone = [s for s in body if s.dims[0] >= 1.0 and s.box[0][1] >= stock_end and s.box[0][0] <= front]
    top = statistics.median(s.box[1][0] for s in zone) if zone else bore - 1.0
    zlo = statistics.median(s.box[2][0] for s in zone) if zone else -0.75
    return {"bore": bore, "x0": x0, "x1": x1, "mag": mag, "grip": grip, "stock_end": stock_end,
            "receiver_front": front, "top": top, "zlo": zlo}


def find_grip(xs, depth, x0, cat):
    """The deepest run of columns hanging at least 55 % as deep as the deepest, at most GRIP_MAX long, not at the rear
    end (except on guns without a stock), or None."""
    deepest = max((d for d in depth if d is not None), default=0.0)
    if deepest <= 1.0:
        return None
    need = 0.55 * deepest
    runs, start = [], None
    for i, d in enumerate(depth + [None]):
        if d is not None and d >= need:
            start = i if start is None else start
        elif start is not None:
            runs.append((xs[start], xs[i - 1], max(depth[start:i])))
            start = None
    for a, b, d in sorted(runs, key=lambda r: -r[2]):
        if b - a <= GRIP_MAX and (a > x0 + 0.3 or cat in NO_STOCK):
            return (a, b)
    return None


# ---------------------------------------------------------------- labels

def rail_rows(shapes, a):
    """Indices of rail teeth: 5 or more equal small parts in a row on top of the gun, evenly spaced."""
    groups = {}
    for i, s in enumerate(shapes):
        if s.group != "gunModel" or max(s.dims) > 0.6 or s.centre[1] >= a["bore"] - 0.6:
            continue
        key = (round(s.dims[0] / 0.02), round(s.dims[1] / 0.02), round(s.dims[2] / 0.02), round(s.box[1][0] / 0.03))
        groups.setdefault(key, []).append(i)
    out = set()
    for ids in groups.values():
        if len(ids) < 5:
            continue
        xs = sorted(shapes[i].centre[0] for i in ids)
        gaps = [b - a for a, b in zip(xs, xs[1:])]
        mean = sum(gaps) / len(gaps)
        if mean > 0 and statistics.pstdev(gaps) <= 0.3 * mean:
            out.update(ids)
    return out


def keyword_area(name):
    low = (name or "").lower()
    for area, rx in KEYWORDS:
        if re.search(rx, low):
            return area
    return None


def name_area(s, a, names):
    """The area Decimation's part name, the group or a source label gives, or None."""
    area = GROUP_AREAS.get(s.group)
    if area is None and names:
        area = NAMED_AREAS.get(prefix(s.name)) or (keyword_area(s.name) if s.group == "gunModel" else None)
    if area == "sight":
        area = "front_sight" if s.centre[0] > a["receiver_front"] else "rear_sight"
    if area == "barrel" and s.centre[0] >= a["x1"] - 1.6:
        area = "muzzle"
    if area == "bolt" and s.box[2][0] < a["zlo"] - 0.3 and max(s.dims) <= 1.2:
        area = "charging_handle"
    return area


def position_area(s, a, rail):
    """The area by position and size (spec, anatomy rule 3)."""
    (bx0, bx1), (by0, by1), (bz0, bz1) = s.box
    cx, cy, _ = s.centre
    bore, grip, mag = a["bore"], a["grip"], a["mag"]
    if rail:
        return "rail"
    if grip and grip[0] - 0.1 <= cx <= grip[1] + 0.1 and cy > bore + 1.0:
        return "grip"
    if cx < a["stock_end"]:
        return "buttpad" if bx1 <= a["x0"] + 0.6 and by1 - by0 >= 0.6 else "stock"
    if cx > a["receiver_front"]:
        slender = by1 - by0 <= 1.0 and bz1 - bz0 <= 1.0
        if slender and abs(cy - bore) <= 0.7 and cx >= a["x1"] - 1.6:
            return "muzzle"
        if slender and abs(cy - bore) <= 1.3 and bx1 - bx0 >= 1.0:
            return "barrel"                              # the barrel and the tubes along it (gas, magazine tube)
        if by0 < bore - 0.75 and cx >= a["x1"] - 0.35 * (a["x1"] - a["receiver_front"]):
            return "front_sight"
        return "handguard"
    if mag and mag[0][0] - 0.3 <= cx <= mag[0][1] + 0.3 and cy > bore + 0.5:
        return "magwell"
    rear = grip[1] if grip else a["stock_end"]
    ahead = mag[0][0] if mag and mag[0][0] > rear else a["receiver_front"]
    if cy > bore + 1.0 and rear - 0.2 <= cx <= ahead + 0.2:
        return "trigger_guard" if max(s.dims) >= 0.8 else "trigger"
    if max(s.dims) <= 1.0 and bz0 < a["zlo"] - 0.15:
        return "charging_handle"
    if max(s.dims) <= 1.2 and by0 < a["top"] - 0.2:
        return "rear_sight"
    return "receiver"


def label_shapes(shapes, header, cat, names=True):
    """[(area, detail)] per shape."""
    a = anchors(shapes, header, cat)
    rails = rail_rows(shapes, a)
    out = []
    for i, s in enumerate(shapes):
        area = name_area(s, a, names) or position_area(s, a, i in rails)
        out.append((area, max(s.dims) <= DETAIL))
    return out


def label_gun(gun, names=True):
    return label_shapes(shapes_of_gun(gun), gun.header, gun.cat, names)


# ---------------------------------------------------------------- exemplars

def area_entry(items):
    """An exemplar from [(shape, detail, group, kind, tone)]."""
    pts = [q for it in items for q in it[0].c.values()]
    box = [[min(q[i] for q in pts), max(q[i] for q in pts)] for i in range(3)]
    axis = max(range(3), key=lambda i: (box[i][1] - box[i][0], -i))
    tris = [t for it in items for t in fit.hexa_tris(it[0].c)]
    prof = fit.profile(tris, axis, box, STATIONS)
    parts = [{"c": [[round(v, 5) for v in it[0].c[k]] for k in fit.CORNERS], "group": it[2], "kind": it[3],
              "tone": list(it[4]) if it[4] else None, "detail": it[1]} for it in items]
    known = [it[4] for it in items if it[4]]
    tone = [round(sum(t[i] for t in known) / len(known), 1) for i in range(3)] if known else None
    return {"box": box, "axis": axis, "profile": [[round(v, 5) for v in r] for r in prof], "parts": parts,
            "count": len(items), "details": sum(1 for it in items if it[1]), "tone": tone}


def exemplars(gun, labels):
    """{area: exemplar} of one study.Gun (tones from its texture when it has one)."""
    shapes = shapes_of_gun(gun)
    by = {}
    for p, s, (area, detail) in zip(gun.parts, shapes, labels):
        tone = match.island_tone(gun, p) if gun.texture is not None else None
        by.setdefault(area, []).append((s, detail, prefix(p.name), study.shape_kind(p), tone))
    return {area: area_entry(items) for area, items in by.items()}


# ---------------------------------------------------------------- the dataset

def jar_stamp():
    st = os.stat(study.JAR)
    return {"size": st.st_size, "mtime": int(st.st_mtime)}


def build(out=DATASET, names=None):
    """Read every Decimation gun from the jar (textures too), label it and store its exemplars as
    out/decimation_areas.json. Returns a summary."""
    from PIL import Image
    data = {"version": 1, "jar": jar_stamp(), "areas": list(AREAS), "guns": {}}
    total = 0
    with zipfile.ZipFile(study.JAR) as z:
        listing = set(z.namelist())
        for name, g in sorted(match.guns().items()):
            if names and name not in names:
                continue
            tex = "assets/deci/textures/model/guns/%s/%s/%s.png" % (g.cat, name, name)
            img = Image.open(BytesIO(z.read(tex))).convert("RGBA") if tex in listing else None
            gun = study.Gun(name, g.cat, z.read("assets/deci/models/guns/%s/%s.bmodel" % (g.cat, name)).decode("latin1"),
                            img, False)
            labels = label_gun(gun)
            shapes = shapes_of_gun(gun)
            a = anchors(shapes, gun.header, gun.cat)
            data["guns"][name] = {"cat": gun.cat, "parts": len(gun.parts), "header": {k: list(v) for k, v in
                                                                                     gun.header.items()},
                                  "anchors": {k: v for k, v in a.items()}, "areas": exemplars(gun, labels)}
            total += len(gun.parts)
    os.makedirs(out, exist_ok=True)
    path = os.path.join(out, "decimation_areas.json")
    with open(path, "w") as f:
        json.dump(data, f)
    return {"guns": len(data["guns"]), "parts": total, "path": path}


_LOADED = {}


def load(out=DATASET):
    """The dataset, built first when missing or made from another jar."""
    path = os.path.join(out, "decimation_areas.json")
    if path in _LOADED:
        return _LOADED[path]
    data = None
    if os.path.exists(path):
        with open(path) as f:
            data = json.load(f)
        if data.get("jar") != jar_stamp() or data.get("version") != 1:
            data = None
    if data is None:
        build(out)
        with open(path) as f:
            data = json.load(f)
    _LOADED[path] = data
    return data


def aggregate(data):
    """Rows of aggregate numbers per category and area (what may be committed: no geometry)."""
    cells = {}
    for g in data["guns"].values():
        for area, ex in g["areas"].items():
            b = ex["box"]
            cells.setdefault((g["cat"], area), []).append((ex["count"], ex["details"], b[0][1] - b[0][0],
                                                          b[1][1] - b[1][0], b[2][1] - b[2][0]))
    rows = []
    for (cat, area), vals in sorted(cells.items()):
        med = [statistics.median(v[i] for v in vals) for i in range(5)]
        rows.append({"category": cat, "area": area, "guns": len(vals), "parts": med[0], "details": med[1],
                     "length": round(med[2], 2), "height": round(med[3], 2), "width": round(med[4], 2)})
    return rows


def write_aggregate(rows, path=AGGREGATE):
    keys = ["category", "area", "guns", "parts", "details", "length", "height", "width"]
    with open(path, "w") as f:
        f.write("# Decimation's guns by anatomy area: medians per category (tools/gunconv/dataset.py; numbers only)\n")
        f.write("\t".join(keys) + "\n")
        for r in rows:
            f.write("\t".join(str(r[k]) for k in keys) + "\n")


def name_agreement():
    """(agreeing, total) over the parts Decimation itself named stock, grip and barrel, labelled with names off."""
    ok = {"defaultStockModel": ("stock", "buttpad"), "defaultGripModel": ("grip",),
          "defaultBarrelModel": ("barrel", "muzzle")}
    agree = total = 0
    for name, g in sorted(match.guns().items()):
        labels = label_gun(g, names=False)
        for p, (area, _) in zip(g.parts, labels):
            want = ok.get(prefix(p.name))
            if want:
                total += 1
                agree += area in want
    return agree, total


# ---------------------------------------------------------------- the eye check

COLOURS = {"receiver": (70, 90, 160), "handguard": (150, 110, 60), "barrel": (90, 90, 90), "muzzle": (20, 20, 20),
           "front_sight": (230, 60, 60), "rear_sight": (240, 140, 40), "stock": (120, 70, 40), "buttpad": (60, 30, 20),
           "grip": (60, 150, 70), "trigger_guard": (200, 200, 60), "trigger": (250, 250, 120), "magazine": (170, 60, 170),
           "magwell": (110, 40, 110), "charging_handle": (40, 200, 200), "bolt": (60, 120, 220), "rail": (250, 250, 250),
           "pump": (200, 120, 160), "cylinder": (140, 200, 120)}


def render_labels(name, out):
    """Side, three quarter and low views with each part in its area's colour (its UV island painted)."""
    from PIL import Image, ImageDraw
    g = study.load(name)
    labels = label_gun(g)
    k = 4
    img = Image.new("RGBA", (g.tw * k, g.th * k), (255, 0, 255, 255))
    d = ImageDraw.Draw(img)
    for p, (area, detail) in zip(g.parts, labels):
        w, h, dd = [int(round(s)) for s in p.size]
        col = COLOURS.get(area, (255, 0, 255))
        if detail:
            col = tuple(min(255, c + 60) for c in col)
        d.rectangle([p.u * k, p.v * k, (p.u + 2 * (dd + w)) * k - 1, (p.v + dd + h) * k - 1], fill=col + (255,))
    g.texture = img
    os.makedirs(out, exist_ok=True)
    paths = []
    for view in ("side", "three", "low"):
        path = os.path.join(out, "areas_%s_%s.png" % (name, view))
        study.render(g, view, 20).save(path)
        paths.append(path)
    return paths


def main(argv):
    if not argv or argv[0] == "build":
        info = build()
        with open(info["path"]) as f:
            rows = aggregate(json.load(f))
        write_aggregate(rows)
        print("%d guns, %d parts -> %s; aggregates -> %s" % (info["guns"], info["parts"], info["path"], AGGREGATE))
    elif argv[0] == "check":
        agree, total = name_agreement()
        print("rules agree with Decimation's own names on %d of %d parts (%.1f %%)" % (agree, total, 100.0 * agree / total))
    elif argv[0] == "render":
        out = os.path.join(ROOT, "docs", "shots", "gunconv_v0.66.1", "areas")
        for n in argv[1:]:
            for p in render_labels(n, out):
                print(p)


if __name__ == "__main__":
    main(sys.argv[1:])

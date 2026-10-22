#!/usr/bin/env python3
"""The dataset of Decimation's guns (docs/superpowers/specs/2026-10-22-gun-converter-design.md, milestone 2): every
part of all 98 guns labelled with its anatomy area, and per gun and area an exemplar (box, run axis, profile, parts with
their corners, kind, group, tone, detail flag) that the converter refits onto a source gun (exemplar.py).

    python3 tools/gunconv/dataset.py build            # build/gunconv/dataset/decimation_areas.json (git ignored)
                                                      # + docs/references/decimation_gun_areas.tsv (aggregates only)
    python3 tools/gunconv/dataset.py check            # the rules against Decimation's own stock / grip / barrel names
    python3 tools/gunconv/dataset.py render GUN ...   # one colour per area, for the eye (docs/shots, git ignored)
    python3 tools/gunconv/dataset.py density PKG ...  # details per area of converted packages vs the medians

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
VERSION = 2           # 2: details by study.detail_parts and their records (milestone 3)
GRIP_MAX = 4.5        # a pistol grip is at most this long
GRIP_REAR = 0.2       # without a magazine below, the grip starts ahead of the rear fifth (a straight stock's butt is not one)
GRIP_TO_MAG = 4.0     # with one, the grip's front is at most this far behind the magazine (the Skorpion's grip is at the rear)
NO_STOCK = ("pistol", "revolver")
ZC = -0.15           # Decimation's centre line in z
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
    if ammo:                           # the magazine's main cluster (a stray round elsewhere does not stretch it)
        mid = statistics.median(s.centre[0] for s in ammo)
        ammo = [s for s in ammo if abs(s.centre[0] - mid) <= 2.5] or ammo
    mag = [(min(s.box[i][0] for s in ammo), max(s.box[i][1] for s in ammo)) for i in range(3)] if ammo else None
    if mag and (mag[1][0] + mag[1][1]) / 2 < bore:
        mag = None                     # a magazine lying on top (the P90's) says nothing about the grip or the receiver
    bullpup = False
    step, xs = 0.1, []
    x = x0
    while x <= x1 + 1e-9:
        xs.append(x)
        x += step
    depth = []
    for x in xs:
        bot = [s.box[1][1] for s in body if s.box[0][0] - 1e-9 <= x <= s.box[0][1] + 1e-9]
        depth.append(max(bot) - bore if bot else None)
    # a bullpup: the magazine in the rear 40 % of the gun and a grip as deep as the deepest clearly in front of it (AUG,
    # FAMAS, L85); the Uzi's magazine sits that far back too, but inside its grip
    if mag and (mag[0][0] + mag[0][1]) / 2 < x0 + 0.4 * (x1 - x0):
        deepest = max((d for d in depth if d is not None), default=0.0)
        ahead = max((d for x, d in zip(xs, depth) if d is not None and x >= mag[0][1] + 0.3), default=0.0)
        bullpup = ahead >= 0.8 * deepest
    # the grip is looked for beside the magazine first (a magwell hanging around it is often deeper than the grip:
    # the SCARs, G36C), then with the magazine's columns too (pistols and the Uzi hold the magazine in the grip)
    grip = None
    for skip_mag in ((True, False) if mag else (False,)):
        cols = [None if (skip_mag and mag[0][0] - 0.2 <= x <= mag[0][1] + 0.2) or (bullpup and x < mag[0][1] - 0.5)
                else d for x, d in zip(xs, depth)]
        # beside skipped magazine columns a run is a piece of a grip around the magazine (the Uzi's, a pistol's)
        near = (mag[0][0] - 0.55, mag[0][1] + 0.55) if skip_mag else None
        grip = find_grip(xs, cols, x0, cat, near, mag[0] if mag else None)
        if grip:
            break
    core_bottom = None
    if cat in NO_STOCK:
        stock_end = stock_low = x0 - 1.0
    elif grip:
        # behind the grip everything below the receiver is stock; at the receiver's height the stock starts only behind
        # the receiver core, the long parts near the bore passing over the grip and on forward (the Uzi's receiver runs
        # back past its grip, the AKM's ends at the grip's rear)
        stock_low = grip[0] - 0.1
        core = [s for s in body if s.dims[0] >= 2.0 and abs(s.centre[1] - bore) <= 1.0
                and s.box[0][0] <= grip[0] and s.box[0][1] >= grip[1] + 1.0]
        stock_end = min([stock_low] + [s.box[0][0] - 0.05 for s in core])
        core_bottom = max(s.box[1][1] for s in core) if core else None
    else:
        stock_end = stock_low = x0 + (x1 - x0) / 3
    if mag and (grip is None or mag[0][0] >= grip[1] - 0.5):
        front = mag[0][1] + min(2.5, max(0.6, 0.12 * (x1 - mag[0][1])))
    elif grip:
        front = grip[1] + 0.35 * (x1 - grip[1])
    else:
        front = x0 + 0.5 * (x1 - x0)
    zone = [s for s in body if s.dims[0] >= 1.0 and s.box[0][1] >= stock_end and s.box[0][0] <= front]
    top = min(s.box[1][0] for s in zone) if zone else bore - 1.0       # the highest long part: the receiver's top
    zlo = statistics.median(s.box[2][0] for s in zone) if zone else -0.75
    return {"bore": bore, "x0": x0, "x1": x1, "mag": mag, "grip": grip, "stock_end": stock_end, "stock_low": stock_low,
            "core_bottom": core_bottom, "receiver_front": front, "top": top, "zlo": zlo, "bullpup": bullpup}


def runs_of(xs, depth, need):
    out, start = [], None
    for i, d in enumerate(depth + [None]):
        if d is not None and d >= need:
            start = i if start is None else start
        elif start is not None:
            out.append((start, i - 1))
            start = None
    return out


def find_grip(xs, depth, x0, cat, near=None, mag_x=None):
    """The deepest run of columns hanging at least 55 % as deep as the deepest, at most GRIP_MAX long, near the
    magazine (its front at most GRIP_TO_MAG behind it) or, without a magazine, not in the rear GRIP_REAR of the gun
    (except on guns without a stock): the butt of a straight stock hangs deepest but is no grip. Or None. A run too long (grip, guard and lower receiver in one, the M4A4)
    gives its deepest part: the columns at 80 % of its depth around its deepest one. `near`: an x range a run must
    not touch (magazine columns left out)."""
    deepest = max((d for d in depth if d is not None), default=0.0)
    if deepest <= 1.0:
        return None
    runs = [(i, j, max(depth[i:j + 1])) for i, j in runs_of(xs, depth, 0.55 * deepest)]
    for i, j, d in sorted(runs, key=lambda r: -r[2]):
        a, b = xs[i], xs[j]
        if b - a > GRIP_MAX:
            k = max(range(i, j + 1), key=lambda q: depth[q])
            lo = hi = k
            while lo > i and depth[lo - 1] >= 0.8 * d:
                lo -= 1
            while hi < j and depth[hi + 1] >= 0.8 * d:
                hi += 1
            a, b = xs[lo], xs[hi]
        if near and (near[0] <= a <= near[1] or near[0] <= b <= near[1]):
            continue
        if mag_x is not None:
            placed = b >= mag_x[0] - GRIP_TO_MAG          # a pistol grip stands near the magazine (a butt does not)
        else:
            placed = cat in NO_STOCK or a > x0 + GRIP_REAR * (xs[-1] - x0)
        if b - a <= GRIP_MAX and placed and (a > x0 + 0.3 or cat in NO_STOCK or mag_x is not None):
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
    below = a["core_bottom"] is None or cy > a["core_bottom"]
    if cx < a["stock_end"] or (cx < a["stock_low"] and below):
        return "buttpad" if bx1 <= a["x0"] + 0.6 and by1 - by0 >= 0.6 else "stock"
    if cx > a["receiver_front"]:
        # Decimation builds barrels from short slices (octagon thirds, rings, a magazine tube under it): anything whose
        # whole section stays near the bore line is barrel, at the front muzzle, whatever its length
        on_line = (by0 >= bore - 1.2 and by1 <= bore + 1.2 and max(by1 - by0, bz1 - bz0) <= 0.9
                   and abs((bz0 + bz1) / 2 - ZC) <= 0.6)
        if on_line and abs(cy - bore) <= 0.7 and cx >= a["x1"] - 1.6:
            return "muzzle"
        if on_line:
            return "barrel"
        if by0 < bore - 0.75 and cx >= a["x1"] - 0.35 * (a["x1"] - a["receiver_front"]):
            return "front_sight"
        return "handguard"
    if mag and mag[0][0] - 0.3 <= cx <= mag[0][1] + 0.3 and cy > bore + 0.5:
        return "magwell"
    rear = grip[1] if grip else a["stock_end"]
    ahead = mag[0][0] if mag and mag[0][0] > rear else a["receiver_front"]
    if cy > bore + 1.0 and rear - 0.2 <= cx <= ahead + 0.2 and min(s.dims) <= 0.25:
        return "trigger_guard" if max(s.dims) >= 0.8 else "trigger"     # bulky ones are the lower receiver (the M4's)
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
    """{area: exemplar} of one study.Gun (tones from its texture when it has one). Each part's detail flag and each
    area's detail records follow study.detail_parts (milestone 3: small or thin parts on a host)."""
    import details
    shapes = shapes_of_gun(gun)
    rule = study.detail_parts([p.verts() for p in gun.parts])
    tones = [match.island_tone(gun, p) if gun.texture is not None else None for p in gun.parts]
    groups = [prefix(p.name) for p in gun.parts]
    kinds = [study.shape_kind(p) for p in gun.parts]
    by = {}
    for i, (area, _) in enumerate(labels):
        by.setdefault(area, []).append(i)
    out = {}
    for area, idx in by.items():
        e = area_entry([(shapes[i], bool(rule[i]), groups[i], kinds[i], tones[i]) for i in idx])
        e["detailRecs"] = details.records([s.c for s in shapes], rule, idx, tones, groups, kinds)
        out[area] = e
    return out


# ---------------------------------------------------------------- the dataset

def jar_stamp():
    st = os.stat(study.JAR)
    return {"size": st.st_size, "mtime": int(st.st_mtime)}


def build(out=DATASET, names=None):
    """Read every Decimation gun from the jar (textures too), label it and store its exemplars as
    out/decimation_areas.json. Returns a summary."""
    from PIL import Image
    data = {"version": VERSION, "jar": jar_stamp(), "areas": list(AREAS), "guns": {}}
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
        if data.get("jar") != jar_stamp() or data.get("version") != VERSION:
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
            run = max(1e-6, b[ex["axis"]][1] - b[ex["axis"]][0])
            cells.setdefault((g["cat"], area), []).append((ex["count"], ex["details"], b[0][1] - b[0][0],
                                                          b[1][1] - b[1][0], b[2][1] - b[2][0], ex["details"] / run))
    rows = []
    for (cat, area), vals in sorted(cells.items()):
        med = [statistics.median(v[i] for v in vals) for i in range(6)]
        rows.append({"category": cat, "area": area, "guns": len(vals), "parts": med[0], "details": med[1],
                     "length": round(med[2], 2), "height": round(med[3], 2), "width": round(med[4], 2),
                     "density": round(med[5], 2)})
    return rows


def medians(data):
    """{(category, area): {"details", "density"}} medians over Decimation's guns (aggregate)."""
    return {(r["category"], r["area"]): {"details": r["details"], "density": r["density"]} for r in aggregate(data)}


def density(gun, med=None):
    """Per area of a study.Gun: parts, details (study.detail_parts), the area's length along its run, details a unit
    of it, and the category's medians (milestone 3 metric; med from medians(), else loaded)."""
    if med is None:
        med = medians(load())
    rule = study.detail_parts([p.verts() for p in gun.parts])
    shapes = shapes_of_gun(gun)
    by = {}
    for i, (area, _) in enumerate(label_gun(gun)):
        by.setdefault(area, []).append(i)
    rows = []
    for area, idx in sorted(by.items()):
        pts = [q for i in idx for q in shapes[i].c.values()]
        box = [(min(q[a] for q in pts), max(q[a] for q in pts)) for a in range(3)]
        run = max(b - a for a, b in box)
        n = sum(1 for i in idx if rule[i])
        m = med.get((gun.cat, area), {})
        rows.append({"area": area, "parts": len(idx), "details": n, "length": round(run, 3),
                     "density": round(n / run, 3) if run > 0 else 0.0, "medianDetails": m.get("details"),
                     "medianDensity": m.get("density")})
    return rows


def write_aggregate(rows, path=AGGREGATE):
    keys = ["category", "area", "guns", "parts", "details", "length", "height", "width", "density"]
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
        x1, y1 = (p.u + 2 * (dd + w)) * k - 1, (p.v + dd + h) * k - 1
        if x1 >= p.u * k and y1 >= p.v * k:                  # a part declared 0 wide has no island
            d.rectangle([p.u * k, p.v * k, x1, y1], fill=col + (255,))
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
    elif argv[0] == "density":
        # dataset.py density PACKAGE_DIR ...: details per area of converted guns against the category's medians
        import package
        med = medians(load())
        for d in argv[1:]:
            g = package.built_gun(d)
            print("%s (%s), %d parts" % (g.name, g.cat, len(g.parts)))
            print("  %-16s %5s %7s %7s %8s %8s %8s" % ("area", "parts", "details", "length", "per unit", "median",
                                                       "med/unit"))
            for r in density(g, med):
                print("  %-16s %5d %7d %7.2f %8.2f %8s %8s" % (r["area"], r["parts"], r["details"], r["length"],
                                                              r["density"], r["medianDetails"], r["medianDensity"]))
    elif argv[0] == "render":
        out = os.path.join(ROOT, "docs", "shots", "gunconv_v0.66.1", "areas")
        for n in argv[1:]:
            for p in render_labels(n, out):
                print(p)


if __name__ == "__main__":
    main(sys.argv[1:])

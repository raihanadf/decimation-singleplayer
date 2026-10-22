#!/usr/bin/env python3
"""The 3D gun converter: a model file in, a Decimation styled gun out (docs/gunconv.md; spec
docs/superpowers/specs/2026-10-22-gun-converter-design.md; style docs/gun_style_guide.md, skill decimation-gun).

    python3 tools/gunconv/convert.py MODEL --name NAME [--display TEXT] [--like GUN] [--category CAT]
                                     [--length UNITS | --length-mm MM] [--muzzle +x] [--up +y] [--mirror]
                                     [--author A --url U --license L] [--out build/gunconv/out]

Steps: read the file (meshio), find the gun's axes (muzzle +x, up -y, right side -z) and turn it into Decimation's
model space, pick the closest Decimation gun ("like") by side silhouette, scale (the real length, else the like
gun's), align onto the like gun and centre on z -0.15, cut into pieces (connected parts of the mesh, one material
and one skin joint each), give each piece its group (magazine ammoModel, bolt slideModel, iron sights
defaultScopeModel where the like gun folds its own, by names or else by the like gun's anatomy), fit each piece with
Decimation's shape kinds (fit.py), dark flat tones, drop parts too small to see or hidden inside others, and find the
attachment points. package.py then writes the gun package.
"""
import argparse
import json
import math
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(os.path.dirname(HERE), "guns"))
import fit  # noqa: E402
import match  # noqa: E402
import meshio  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(HERE))
ZC = -0.15
OURS = ("mac10", "ump9", "hk416", "hk416tan", "mk18")      # tools/guns/sightfit.py OURS
NAME_RE = re.compile(r"^[a-z][a-z0-9]{1,31}$")
CATEGORIES = ("pistol", "revolver", "smg", "rifle", "shotgun", "rocket", "mg", "crossbow")
CATEGORY_LENGTH = {"pistol": 8.3, "revolver": 8.9, "smg": 19.9, "rifle": 28.0, "shotgun": 29.9, "rocket": 28.6,
                   "mg": 33.3, "crossbow": 22.8}            # docs/gun_style_guide.md section 2 medians
HANDS = {"pistol": ((-6.35, 1.07, -2.0), (-1.55, 9.12, 5.32)), "smg": ((-6.55, 1.92, -2.0), (4.2, 8.02, 4.52)),
         "rifle": ((-7.2, 2.07, -2.0), (3.7, 8.22, 4.52))}  # section 9 medians, only without a like gun
MIN_SIZE = 0.08       # parts whose longest side is shorter are not seen in game: dropped
PANEL = 1.5           # flat parts longer than this along x become panels (rifle parts: longest side median 1.0, q90 1.4)
SIGHT_REACH = 1.5     # the front sight is moved onto the like gun's sight line only when it is this close already
SLIDE_KICK = {"pistol": 1.2, "revolver": 0.0, "smg": 1.6}  # Fire: slideModel back (style guide section 10: about 2.4)
KEYWORDS = [("ammoModel", re.compile(r"mag|clip|ammo|cartridge|bullet|shell", re.I)),
            ("slideModel", re.compile(r"bolt|slide|carrier|charg|cocking", re.I)),
            ("sight", re.compile(r"sight|iron|notch|aperture", re.I))]
ROOTISH = re.compile(r"^(root|armature|skeleton|rig|joint\d*|bone\d*|)$", re.I)
RAIL = re.compile(r"rail|picatinny", re.I)


class Conversion:
    """The converter's proposal: parts in Decimation model units plus what the package needs."""

    def __init__(self, **kw):
        self.name = kw.get("name")
        self.display = kw.get("display")
        self.category = kw.get("category")
        self.like = kw.get("like")
        self.candidates = kw.get("candidates", [])
        self.parts = kw.get("parts", [])
        self.points = kw.get("points", {})
        self.header = kw.get("header", {})
        self.warnings = kw.get("warnings", [])
        self.matrix = kw.get("matrix")
        self.source = kw.get("source")
        self.stats = kw.get("stats", {})

    def to_json(self):
        return {"name": self.name, "display": self.display, "category": self.category, "like": self.like,
                "candidates": self.candidates, "parts": parts_to_json(self.parts), "points": self.points,
                "header": {k: list(v) for k, v in self.header.items()}, "warnings": self.warnings,
                "matrix": self.matrix, "source": self.source, "stats": self.stats}

    @classmethod
    def from_json(cls, d):
        return cls(name=d.get("name"), display=d.get("display"), category=d.get("category"), like=d.get("like"),
                   candidates=d.get("candidates", []), parts=parts_from_json(d.get("parts", [])),
                   points=d.get("points", {}), header={k: tuple(v) for k, v in d.get("header", {}).items()},
                   warnings=d.get("warnings", []), matrix=d.get("matrix"), source=d.get("source"),
                   stats=d.get("stats", {}))


# ---------------------------------------------------------------- parts as JSON (the page edits these)

def parts_to_json(parts):
    return [{"id": i, "name": p.name, "group": p.group, "label": getattr(p, "label", p.name),
             "colour": [int(c) for c in p.colour], "corners": [[round(v, 5) for v in p.c[k]] for k in fit.CORNERS],
             "kind": fit.kind(p)} for i, p in enumerate(parts)]


def parts_from_json(data):
    out = []
    for d in data:
        p = fit.new_part(d.get("name", "part"), d.get("group", "gunModel"), tuple(d.get("colour", (40, 40, 40))),
                         {k: [float(v) for v in d["corners"][i]] for i, k in enumerate(fit.CORNERS)},
                         d.get("label", d.get("name", "part")))
        out.append(p)
    return out


# ---------------------------------------------------------------- names and tones

def validate_name(name):
    """A gun name the game can use: 2 to 32 lowercase letters and digits starting with a letter, not one of ours and
    not one of Decimation's (its assets share the deci folders)."""
    if not isinstance(name, str) or not NAME_RE.match(name):
        raise ValueError("gun name must be 2 to 32 lowercase letters or digits, starting with a letter: %r" % (name,))
    if name in OURS:
        raise ValueError("%r is one of our own guns already" % name)
    if name in match.guns():
        raise ValueError("%r is one of Decimation's guns: pick another name (its files would clash)" % name)
    return name


def default_name(stem):
    base = re.sub(r"[^a-z0-9]", "", stem.lower()) or "gun"
    if not base[0].isalpha():
        base = "g" + base
    base = base[:30]
    for cand in [base] + [base + suffix for suffix in ("c", "c2", "c3", "c4")]:
        try:
            return validate_name(cand)
        except ValueError:
            continue
    return validate_name(base[:28] + "conv")


def tone(c):
    """A source colour (sRGB 0..255) as a Decimation tone (style guide section 4): greys into the dark metal band
    24 .. 64 keeping their order, coloured materials (wood, polymer, tan) keep their hue at a dark luminance."""
    r, g, b = (float(v) for v in c[:3])
    lum = 0.3 * r + 0.59 * g + 0.11 * b
    mx, mn = max(r, g, b), min(r, g, b)
    sat = (mx - mn) / mx if mx > 0 else 0.0
    if mx - mn < 20 or sat < 0.2:
        t = int(round(24 + 40 * (lum / 255.0) ** 0.9))
        return (t, t, t)
    target = 22 + 34 * (lum / 255.0) ** 0.9
    f = target / max(lum, 1.0)
    return tuple(max(0, min(255, int(round(v * f)))) for v in (r, g, b))


def palette_tones(pieces, groups, pal):
    """One tone per piece. With the like gun's palette (match.palette): grey materials keep their order but take the
    like gun's metal greys (our darkest grey its 5th percentile, our lightest its 95th), coloured ones take its body
    tone (wood, polymer) or, on the magazine, its magazine tone, a little lighter or darker as the source is. Without
    one: tone() (style guide section 4)."""
    if not pal:
        return [tone(p.colour) for p in pieces]
    greys = sorted(set(round(match.lum(p.colour), 1) for p in pieces if match.grey(p.colour)))

    def quantile(vals, f):
        return vals[min(len(vals) - 1, max(0, int(round(f * (len(vals) - 1)))))]
    coloured = {}
    for p, g in zip(pieces, groups):
        if not match.grey(p.colour):
            coloured.setdefault(g == "ammoModel", []).append(match.lum(p.colour))
    out = []
    for p, g in zip(pieces, groups):
        if match.grey(p.colour):
            r = greys.index(round(match.lum(p.colour), 1)) / (len(greys) - 1) if len(greys) > 1 else 0.5
            src = pal["ammoGrey"] if g == "ammoModel" and pal["ammoGrey"] else pal["grey"]
            v = int(round(quantile(src, 0.05 + 0.9 * r)))
            out.append((v, v, v))
            continue
        base = pal["ammo"] if g == "ammoModel" and pal["ammo"] else pal["body"]
        if base is None:
            out.append(tone(p.colour))
            continue
        fam = coloured[g == "ammoModel"]
        mid = sorted(fam)[len(fam) // 2]
        f = min(1.25, max(0.8, match.lum(p.colour) / mid if mid > 0 else 1.0))
        out.append(tuple(max(0, min(255, int(round(c * f)))) for c in base[:3]))
    return out


# ---------------------------------------------------------------- transforms

def mat_apply(m, p):
    return (m[0][0] * p[0] + m[0][1] * p[1] + m[0][2] * p[2] + m[0][3],
            m[1][0] * p[0] + m[1][1] * p[1] + m[1][2] * p[2] + m[1][3],
            m[2][0] * p[0] + m[2][1] * p[1] + m[2][2] * p[2] + m[2][3])


def compose(rot, scale, zscale, t):
    """4x4 rows: model = T + diag(s, s, s * zscale) R p."""
    s = (scale, scale, scale * zscale)
    return [[rot[r][0] * s[r], rot[r][1] * s[r], rot[r][2] * s[r], t[r]] for r in range(3)] + [[0.0, 0.0, 0.0, 1.0]]


def model_tris(model, m):
    return [tuple(mat_apply(m, mesh.verts[i]) for i in f) for mesh in model.meshes for f in mesh.faces]


def body_z(tris, k=64.0):
    """The gun's centre line in z: the median over x columns of the top view's z middle (the charging handle and
    other side parts only move a few columns). PIL fills rows floor(a) .. floor(b) of a span a .. b, so a column's
    middle is (first + last + 1) / 2 on average; at 64 pixels a unit it is within 0.01 of the true one."""
    x0 = min(q[0] for t in tris for q in t)
    z0 = min(q[2] for t in tris for q in t)
    x1 = max(q[0] for t in tris for q in t)
    z1 = max(q[2] for t in tris for q in t)
    img = fit.silhouette([[(q[0], q[2]) for q in t] for t in tris], x0, z0, k, int((x1 - x0) * k) + 2,
                         int((z1 - z0) * k) + 2)
    mids = [z0 + (c[0] + c[1] + 1) / 2.0 / k for c in fit.columns(img) if c]
    return fit.median(mids)


# ---------------------------------------------------------------- pieces

class Piece:
    def __init__(self, tris, label, material, mesh_name, colour):
        self.tris, self.label, self.material, self.mesh_name, self.colour = tris, label, material, mesh_name, colour

    def centre(self):
        pts = [q for t in self.tris for q in t]
        return [sum(q[i] for q in pts) / len(pts) for i in range(3)]

    def box(self):
        pts = [q for t in self.tris for q in t]
        return [(min(q[i] for q in pts), max(q[i] for q in pts)) for i in range(3)]


def pieces_of(model, m):
    """Connected parts of each mesh (one material each), split again by the skin joint their faces follow."""
    names = {mesh.name for mesh in model.meshes}
    out = []
    for mesh in model.meshes:
        verts = [mat_apply(m, v) for v in mesh.verts]
        for comp in fit.components(verts, mesh.faces):
            by = {}
            for fi in comp:
                lab = None
                if mesh.vlabels:
                    labs = [mesh.vlabels[i] for i in mesh.faces[fi]]
                    lab = max(set(labs), key=labs.count)
                by.setdefault(lab, []).append(fi)
            for lab, fis in by.items():
                tris = [tuple(verts[i] for i in mesh.faces[fi]) for fi in fis]
                if mesh.vcolours:
                    cs = [mesh.vcolours[i] for fi in fis for i in mesh.faces[fi]]
                    colour = tuple(int(round(sum(c[k] for c in cs) / len(cs))) for k in range(3))
                else:
                    colour = mesh.colour
                if lab and not ROOTISH.match(lab):
                    label = lab
                elif len(names) > 1:
                    label = mesh.name
                else:
                    label = mesh.material or mesh.name
                out.append(Piece(tris, label, mesh.material or "", mesh.name, colour))
    return out


def keyword_group(piece):
    for text in (piece.label, piece.material, piece.mesh_name):
        for group, rx in KEYWORDS:
            if text and rx.search(text):
                return group
    return None


def grow(box, d):
    return [(a - d, b + d) for a, b in box]


def inside_box(p, box):
    return all(box[i][0] <= p[i] <= box[i][1] for i in range(3))


def groups_of(pieces, like):
    """Group per piece: names first (magazine, bolt, sights); where the file names none of a kind, the like gun's
    anatomy (pieces centred in its magazine, bolt or folding sights, after the alignment)."""
    named = [keyword_group(p) for p in pieces]
    have = set(n for n in named if n)
    folding = bool(like and like.get("foldingSights"))
    out = []
    for p, n in zip(pieces, named):
        g = n
        if g is None and like:
            c = p.centre()
            size = max(b - a for a, b in p.box())
            if "ammoModel" not in have and like.get("ammo") and inside_box(c, grow(like["ammo"], 0.3)):
                g = "ammoModel"
            elif "slideModel" not in have and like.get("slide") and size < 6 and inside_box(c, grow(like["slide"], 0.15)):
                g = "slideModel"
            elif "sight" not in have and folding and like.get("sights") and inside_box(c, grow(like["sights"], 0.15)):
                g = "sight"
        if g == "sight":
            g = "defaultScopeModel" if folding else "gunModel"
        out.append(g or "gunModel")
    return out


# ---------------------------------------------------------------- clean up

def size_of(p):
    pts = p.points()
    return max(max(q[i] for q in pts) - min(q[i] for q in pts) for i in range(3))


def drop_hidden(parts):
    """Parts entirely inside another part are never seen (cartridges in a magazine body): dropped."""
    boxes = [fit.bounds([p]) for p in parts]
    shapes = [None] * len(parts)
    keep = []
    for i, p in enumerate(parts):
        hidden = False
        for j, q in enumerate(parts):
            if i == j or not all(boxes[j][a][0] - 0.01 <= boxes[i][a][0] and boxes[i][a][1] <= boxes[j][a][1] + 0.01
                                 for a in range(3)):
                continue
            if shapes[j] is None:
                shapes[j] = fit.planes(q)
            if all(fit.inside(shapes[j], c, 0.005) for c in p.points()):
                hidden = True
                break
        if not hidden:
            keep.append(p)
    return keep


def exposed_top(p, boxes, i):
    """Nothing lies on this part's top face: other parts just above it cover under half of it (bounding boxes)."""
    (x0, x1), (y0, y1), (z0, z1) = boxes[i]
    area = max(1e-9, (x1 - x0) * (z1 - z0))
    cover = 0.0
    for j, ((a0, a1), (b0, b1), (c0, c1)) in enumerate(boxes):
        if j == i or b0 >= y0 or b1 < y0 - 0.3 or b1 > y0 + 0.05:
            continue
        cover += max(0.0, min(x1, a1) - max(x0, a0)) * max(0.0, min(z1, c1) - max(z0, c0))
    return cover < 0.5 * area


def layered(parts):
    """Body parts that are bulky and open on top get Decimation's bevelled top plate (fit.layer_top)."""
    boxes = [fit.bounds([p]) for p in parts]
    out = []
    for i, p in enumerate(parts):
        if p.group == "gunModel" and not getattr(p, "round", False) and exposed_top(p, boxes, i):
            out += fit.layer_top(p)
        else:
            out.append(p)
    return out


# ---------------------------------------------------------------- attachment points

def centre_y(p):
    pts = p.points()
    return (min(q[1] for q in pts) + max(q[1] for q in pts)) / 2


def points_of(parts, like, mount=None):
    """gun.json "points": flame (muzzle tip minus 0.1, 0.85 above the bore, z -0.15: Decimation's convention), the
    rail top in the sight zone (sightfit's rule), a top rail and a rail under the handguard where the file names one
    (or, top only, along the rail top when the like gun mounts sights on a rail), the sight mount."""
    xs = [q[0] for p in parts for q in p.points()]
    muzzle = max(xs)
    front = [p for p in parts if p.group == "gunModel" and max(q[0] for q in p.points()) >= muzzle - 0.4]
    bore = fit.median([centre_y(p) for p in front]) if front else fit.median([centre_y(p) for p in parts])
    flame = [round(muzzle - 0.1, 4), round(bore - 0.85, 4), ZC]
    top = match.rail_top([(p.group, p.points()) for p in parts])
    if top is None:
        zone = [p for p in parts if p.group not in ("ammoModel", "defaultScopeModel")
                and min(q[0] for q in p.points()) <= match.ZONE[1] and max(q[0] for q in p.points()) >= match.ZONE[0]]
        top = min(q[1] for p in zone for q in p.points()) if zone else bore - 1.0
    mount = mount if mount is not None else (like or {}).get("mount")
    rail = grip_rail = None
    named = [p for p in parts if RAIL.search(getattr(p, "label", "") or "")]
    above = [p for p in named if centre_y(p) < bore]
    below = [p for p in named if centre_y(p) > bore]
    if above:
        rail = [round(min(q[0] for p in above for q in p.points()), 3), round(max(q[0] for p in above for q in p.points()), 3)]
    elif mount == "rail":
        flat = [p for p in parts if p.group == "gunModel" and abs(min(q[1] for q in p.points()) - top) <= 0.1
                and max(q[0] for q in p.points()) - min(q[0] for q in p.points()) >= 1.0]
        if flat:
            rail = [round(min(q[0] for p in flat for q in p.points()), 3), round(max(q[0] for p in flat for q in p.points()), 3)]
    if below:
        grip_rail = [round(min(q[0] for p in below for q in p.points()), 3), round(max(q[0] for p in below for q in p.points()), 3)]
    return {"flame": flame, "railTop": round(float(top), 4), "rail": rail, "gripRail": grip_rail, "mount": mount}


# ---------------------------------------------------------------- the conversion

def convert(model, name=None, display=None, category=None, like=None, length=None, length_mm=None, up=None,
            muzzle=None, mirror=False, tol=fit.TOL, source=None, mount=None):
    warnings = []
    if name:
        validate_name(name)                              # before the work: a taken name fails at once
    if isinstance(model, str):
        model = meshio.load(model)
    if category is not None and category not in CATEGORIES:
        raise ValueError("category must be one of %s" % ", ".join(CATEGORIES))
    all_tris = [tuple(mesh.verts[i] for i in f) for mesh in model.meshes for f in mesh.faces]
    f, u = fit.orient(all_tris, up=up, muzzle=muzzle)
    rot = fit.rotation(f, u)
    if mirror:
        rot = [rot[0], rot[1], tuple(-c for c in rot[2])]
        warnings.append("mirrored on request: the gun's right and left sides swapped")
    r_tris = model_tris(model, compose(rot, 1.0, 1.0, (0, 0, 0)))
    L0 = max(q[0] for t in r_tris for q in t) - min(q[0] for t in r_tris for q in t)
    # the closest Decimation gun, on silhouettes normalised to one length
    cands = []
    if match.jar_ok():
        cands = match.candidates(match.side_polys_of_tris(r_tris), category)
    elif like:
        raise ValueError("like %r needs Decimation.jar (not found)" % like)
    if like is None and cands:
        like = cands[0][0]
    info = None
    if like:
        if like not in match.guns():
            raise ValueError("%r is not one of Decimation's guns" % like)
        info = match.info(like)
        if category and category != info["category"]:
            warnings.append("category %s differs from the like gun's (%s %s): hands, attachments and loot follow the "
                            "like gun" % (category, like, info["category"]))
        category = category or info["category"]
    elif category is None:
        category = "rifle"
        warnings.append("no Decimation.jar: category rifle assumed, hands from the style guide medians")
    # scale: the real length, else the like gun's, else the category's median
    if length:
        target = float(length)
    elif length_mm:
        target = float(length_mm) / 31.0
    elif info:
        target = info["length"]
    else:
        target = CATEGORY_LENGTH[category]
    s = target / L0
    width = (max(q[2] for t in r_tris for q in t) - min(q[2] for t in r_tris for q in t)) * s
    zscale = 1.0
    if info and width > info["width"] * 1.15:
        zscale = info["width"] * 1.15 / width
        warnings.append("slimmed: %.2f wide against the like gun's %.2f (Decimation draws guns slimmer than real)"
                        % (width, info["width"]))
    s_tris = model_tris(model, compose(rot, s, zscale, (0, 0, 0)))
    tz = ZC - body_z(s_tris)
    tx = ty = 0.0
    if info:
        # start from the like gun's bounding box centre, then the best overlap of the side silhouettes nearby
        (bx0, bx1), (by0, by1), _ = info["bounds"]
        xs = [q[0] for t in s_tris for q in t]
        ys = [q[1] for t in s_tris for q in t]
        tx0 = (bx0 + bx1) / 2 - (min(xs) + max(xs)) / 2
        ty0 = (by0 + by1) / 2 - (min(ys) + max(ys)) / 2
        moved = [[(q[0] + tx0, q[1] + ty0) for q in t] for t in s_tris]
        dx, dy, _ = match.align(moved, like)
        tx, ty = tx0 + dx, ty0 + dy
        # one aim pose for every gun: our front sight tip goes where the like gun's is (its sight line)
        ours = match.front_tip(s_tris) + ty
        theirs = match.front_tip([p.verts() for p in match.guns()[like].parts])
        if abs(theirs - ours) <= SIGHT_REACH:
            ty += theirs - ours
        else:
            warnings.append("front sight %.2f off the like gun's sight line: not moved; check the aim" % (theirs - ours))
    else:
        x0 = min(q[0] for t in s_tris for q in t)
        y0 = min(q[1] for t in s_tris for q in t)
        tx, ty = -7.5 - x0, -4.9 - y0                      # rifle median x and top (style guide section 9)
    M = compose(rot, s, zscale, (tx, ty, tz))
    pieces = pieces_of(model, M)
    groups = groups_of(pieces, info)
    if like and len(set(tuple(p.colour) for p in pieces)) <= 1:
        # one colour in the whole file: the like gun's own part tones by position (a colouring template)
        lt = match.part_tones(like)
        tones = [match.template_tone(p.box(), lt) for p in pieces]
        warnings.append("the file has one colour: tones taken from %s's parts by position" % like)
    else:
        tones = palette_tones(pieces, groups, match.palette(like) if like else None)
    parts, labels_seen = [], {}
    for piece, group, colour in zip(pieces, groups, tones):
        base = re.sub(r"[^A-Za-z0-9]", "", piece.label) or "part"
        k = labels_seen.get(base, 0)
        labels_seen[base] = k + 1
        parts += fit.fit_piece(piece.tris, "%s%d" % (base, k), group, colour, tol=tol, label=piece.label)
    n_fit = len(parts)
    parts = [p for p in parts if size_of(p) >= MIN_SIZE]
    small = n_fit - len(parts)
    before = len(parts)
    parts = drop_hidden(parts)
    hidden = before - len(parts)
    if small or hidden:
        warnings.append("dropped %d parts too small to see and %d hidden inside others" % (small, hidden))
    parts = [fit.snap_part(q) for p in parts for q in (fit.panels(p, PANEL) if p.group == "gunModel"
                                                       and not getattr(p, "round", False) else [p])]
    parts = layered(parts)
    if not any(p.group == "ammoModel" for p in parts):
        warnings.append("no magazine found (no ammoModel part): the reload moves nothing; set a part's group")
    if not any(p.group == "slideModel" for p in parts):
        warnings.append("no bolt or slide found (no slideModel part): firing moves nothing; set a part's group")
    points = points_of(parts, info, mount)
    if info:
        hd = info["header"]
        header = {"flamePos": tuple(points["flame"]), "ejectPos": hd.get("ejectPos", (0.0, 0.0, 0.0)),
                  "rhPos": hd.get("rhPos"), "rhRot": hd.get("rhRot", (0.0, 0.0, 0.0)),
                  "lhPos": hd.get("lhPos"), "lhRot": hd.get("lhRot", (0.0, 0.0, 0.0))}
    else:
        rh, lh = HANDS.get(category, HANDS["rifle"])
        header = {"flamePos": tuple(points["flame"]), "ejectPos": (0.0, 0.0, 0.0), "rhPos": rh, "rhRot": (0.0, 0.0, 0.0),
                  "lhPos": lh, "lhRot": (0.0, 0.0, 0.0)}
    name = name or default_name(model.name or "gun")
    counts = {}
    for p in parts:
        counts[p.group] = counts.get(p.group, 0) + 1
    stats = {"triangles": model.triangle_count(), "pieces": len(pieces), "parts": len(parts), "groups": counts,
             "scale": s, "length": target, "zscale": zscale}
    return Conversion(name=name, display=display or default_display(model.name or name), category=category,
                      like=like, candidates=cands, parts=parts, points=points, header=header, warnings=warnings,
                      matrix=[list(r) for r in M], source=source, stats=stats)


def default_display(stem):
    s = re.sub(r"[_\-]+", " ", stem).strip()
    return s.upper() if len(s) <= 5 else s.title()


# ---------------------------------------------------------------- command line

def main(argv=None):
    import package
    ap = argparse.ArgumentParser(description="Convert a 3D gun model into a Decimation gun package.")
    ap.add_argument("model")
    ap.add_argument("--name")
    ap.add_argument("--display")
    ap.add_argument("--like")
    ap.add_argument("--category", choices=CATEGORIES)
    ap.add_argument("--length", type=float, help="length in model units (31 mm a unit)")
    ap.add_argument("--length-mm", type=float, help="the real gun's length in mm")
    ap.add_argument("--muzzle", help="the file's muzzle direction, e.g. -x")
    ap.add_argument("--up", help="the file's up direction, e.g. +y")
    ap.add_argument("--mirror", action="store_true")
    ap.add_argument("--author")
    ap.add_argument("--url")
    ap.add_argument("--license")
    ap.add_argument("--out", default=os.path.join(ROOT, "build", "gunconv", "out"))
    a = ap.parse_args(argv)
    src = package.source_for(a.model)
    for k in ("author", "url", "license"):
        if getattr(a, k):
            src[k] = getattr(a, k)
    conv = convert(a.model, name=a.name, display=a.display, category=a.category, like=a.like, length=a.length,
                   length_mm=a.length_mm, up=a.up, muzzle=a.muzzle, mirror=a.mirror, source=src)
    res = package.export(conv, a.out)
    print("%s (%s), like %s; %d parts %s" % (conv.name, conv.category, conv.like, len(conv.parts),
                                             json.dumps(conv.stats["groups"])))
    print("candidates: " + ", ".join("%s %.3f" % c for c in conv.candidates[:5]))
    for w in conv.warnings:
        print("warning: " + w)
    print("points: " + json.dumps(conv.points))
    print("package: " + res["dir"])
    for f in res["files"]:
        print("  " + f)


if __name__ == "__main__":
    main()

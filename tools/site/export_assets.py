"""Copies the art of DeciRemnant, the project website, into its private repo (docs/website.md section 4).

    python3 tools/site/export_assets.py SITE_DIR                 # stand in screenshots from docs/shots
    python3 tools/site/export_assets.py SITE_DIR --shots DIR     # siteshots output: DIR/site_<name>.png
    python3 tools/site/export_assets.py SITE_DIR --guide DIR     # ONLY the guide pictures: DIR/guide_<name>.png
    python3 tools/site/export_assets.py SITE_DIR --logo FILE     # ONLY the logos: ours from FILE, BoehMod's from the jar

Item icons and the two torn menu textures come from Decimation.jar.original.bak, the MAC-10 icon is ours, the
dirt strip is generated (seeded noise). Screenshots become JPEG (at most 1920 wide) plus og.jpg (1200 x 630 from
the hero). Every picture is named by its content hash (city-1a2b3c4d.jpg; public/shots/manifest.json maps the subject
to the file), so a new picture gets a new address and no browser or image cache shows an old one (the optimiser
answers with max-age 14400). Everything lands in SITE_DIR/public; nothing here is committed to this public repo.

--guide exports nothing else: Minecraft screens from the guideshots dev test (1440 x 810, GUI scale 3) cropped to
their buttons, kept as PNG (the pixel font stays sharp), named by content hash in public/guide with
public/guide/manifest.json (install step and server page pictures, docs/website.md).
"""
import argparse
import glob
import hashlib
import io
import json
import os
import random
import sys
import zipfile

from PIL import Image, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
JAR = os.path.join(ROOT, "Decimation.jar.original.bak")
ITEMS = "assets/deci/textures/items/"
ICONS = {
    "bottlecap": ITEMS + "bottlecap.png",
    "map": ITEMS + "map.png",
    "firstaidkit": ITEMS + "firstaidkit.png",
    "militarykeycard": ITEMS + "militarykeycard.png",
    "radio": ITEMS + "radio.png",
    "supplydropradio": ITEMS + "supplydropradio.png",
    "bottlecapgold": ITEMS + "bottlecapgold.png",
    "achivement_trader_bandit": ITEMS + "achivement_trader_bandit.png",
    "syringe_infection": ITEMS + "syringe_infection.png",
    "infectedflesh": ITEMS + "infectedflesh.png",
    "hugemilitarypack_green": ITEMS + "hugemilitarypack_green.png",
    "graphics_card": ITEMS + "scraps/monitor/graphics_card.png",
}
UI = {"panel": "assets/deci/textures/gui/menu/buttonbg.png", "mask": "assets/deci/textures/gui/menu/buttonbg_red.png"}
MAC10 = os.path.join(ROOT, "dev/src/main/resources/assets/deci/textures/items/gun/smg/mac10.png")
SUBJECTS = ["hero", "city", "highway", "bridge", "rail", "gate", "camp", "refuge", "base", "airport", "gun"]
USER_CROP = (60, 150, 1700, 1073)  # the user's window shots: no title bar, no HUD
STAND_INS = {
    "hero": ("lc_superblock_v0.24.3/study_1.png", None),
    "city": ("lc_superblock_v0.24.3/study_2.png", None),
    "highway": ("lc_highway_v0.25.0b/study_3.png", None),
    "bridge": ("lc_bridge_v0.45.1/bridge_aerial.png", None),
    "rail": ("lc_rail_v0.44.0/rail_platform.png", None),
    "gate": ("wasteland_gate_v0.60.2_review/user_gate_1.png", USER_CROP),
    "camp": ("safezone_camp_v0.47.0_review/user_119.png", USER_CROP),
    "refuge": ("city_refuge_v0.48.0/refuge_market.png", None),
    "base": ("milbase_v0.42_review/user_99.png", USER_CROP),
    "airport": ("airport_v0.62.3/airport_helipad.png", None),
    "gun": ("mac10_v0.37/gunview_mac10.png", None),
}
BOEHMOD_LOGO = "assets/deci/textures/gui/menu/boehmod_big.png"  # BoehMod's logo of the main menu: the footer credit
GUI_BOX = (225, 20, 1215, 800)  # the buttons and fields of a GUI scale 3 screen in a 1440 x 810 window
GUIDE = {"worldtype": GUI_BOX, "multiplayer": GUI_BOX, "addserver": GUI_BOX}


def dirt(path):
    """Our own dirt strip: seeded noise in browns (not Mojang's texture)."""
    pal = [(115, 82, 56), (128, 92, 63), (101, 72, 49), (140, 101, 70), (88, 63, 42), (155, 118, 84)]
    weights = [30, 25, 20, 12, 8, 5]
    r = random.Random(7)
    im = Image.new("RGB", (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), r.choices(pal, weights)[0])
    im.save(path)


def hashed(im, folder, name):
    """Saves im as JPEG named name-<first 8 of its sha1>.jpg in folder; returns the file name."""
    buf = io.BytesIO()
    im.save(buf, "JPEG", quality=85)
    data = buf.getvalue()
    fname = "%s-%s.jpg" % (name, hashlib.sha1(data).hexdigest()[:8])
    with open(os.path.join(folder, fname), "wb") as f:
        f.write(data)
    return fname


def guide(site, src_dir):
    """The guide pictures alone: crop, PNG named name-<first 8 of its sha1>.png, manifest; old ones removed."""
    folder = os.path.join(site, "public", "guide")
    os.makedirs(folder, exist_ok=True)
    for old in glob.glob(os.path.join(folder, "*.png")):
        os.remove(old)
    manifest = {}
    for name, box in GUIDE.items():
        src = os.path.join(src_dir, "guide_%s.png" % name)
        im = Image.open(src).convert("RGB").crop(box)
        buf = io.BytesIO()
        im.save(buf, "PNG", optimize=True)
        data = buf.getvalue()
        manifest[name] = "%s-%s.png" % (name, hashlib.sha1(data).hexdigest()[:8])
        with open(os.path.join(folder, manifest[name]), "wb") as f:
            f.write(data)
        print("guide %-11s %4d x %-4d %6d bytes -> %s" % (name, im.width, im.height, len(data), manifest[name]))
    with open(os.path.join(folder, "manifest.json"), "w") as f:
        json.dump(manifest, f, indent=2, sort_keys=True)
        f.write("\n")


def clear_paper(im):
    """Our logo came on white paper (user, 17 Oktober 2026: "idk you can put invicible background or not"): near white
    pixels (all channels 232 and up) become clear, and only grey edge pixels within 2 px of them get a partial alpha
    (white taken out of their colour), so the coral letters and the barbed wire keep their full colour."""
    im = im.convert("RGB")
    w, h = im.size
    px = list(im.getdata())
    paper = Image.new("L", (w, h))
    paper.putdata([255 if min(p) >= 232 else 0 for p in px])
    band = list(paper.filter(ImageFilter.MaxFilter(5)).getdata())
    flat = list(paper.getdata())
    out = []
    for i, (r, g, b) in enumerate(px):
        if flat[i]:
            out.append((0, 0, 0, 0))
        elif band[i] and max(r, g, b) - min(r, g, b) < 45:
            a = max(0.05, min(1.0, max(255 - r, 255 - g, 255 - b) / 255.0 / 0.85))
            out.append(tuple(int(max(0, min(255, round((c - 255 * (1 - a)) / a)))) for c in (r, g, b)) + (int(round(a * 255)),))
        else:
            out.append((r, g, b, 255))
    clear = Image.new("RGBA", (w, h))
    clear.putdata(out)
    return clear.crop(clear.getbbox())


def logo(site, ours):
    """The site's logos: OUR official logo from `ours` (paper cleared, 120 px high: sharp at 40 px on 3x screens) as
    public/ui/deciremnant-logo.webp (lossy WebP 90, alpha exact), and BoehMod's logo from the jar for the footer credit as
    public/ui/boehmod-logo.webp (lossless WebP)."""
    ui = os.path.join(site, "public", "ui")
    mark = clear_paper(Image.open(ours))
    mark = mark.resize((round(mark.width * 120 / mark.height), 120), Image.LANCZOS)
    mark.save(os.path.join(ui, "deciremnant-logo.webp"), "WEBP", quality=90, alpha_quality=100, method=6)  # a drawing: lossy is fine
    with zipfile.ZipFile(JAR) as z:
        boeh = Image.open(io.BytesIO(z.read(BOEHMOD_LOGO))).convert("RGBA")
    boeh = boeh.resize((round(boeh.width * 64 / boeh.height), 64), Image.LANCZOS)
    boeh.save(os.path.join(ui, "boehmod-logo.webp"), "WEBP", lossless=True)
    for name, im in (("deciremnant-logo.webp", mark), ("boehmod-logo.webp", boeh)):
        print("logo %-22s %4d x %-3d %6d bytes" % (name, im.width, im.height, os.path.getsize(os.path.join(ui, name))))


def site_icons(site):
    """Favicon and touch icon of the site: our MAC-10 (our own art) on the download button's orange, readable on light
    and dark browser tabs. Writes app/icon.png (32), app/apple-icon.png (180) and app/favicon.ico (16, 32, 64)."""
    gun = Image.open(MAC10).convert("RGBA")
    orange = (248, 152, 56, 255)
    icon = Image.new("RGBA", (32, 32), orange)
    icon.alpha_composite(gun.resize((28, 28), Image.NEAREST), (2, 2))
    icon.save(os.path.join(site, "app", "icon.png"))
    apple = Image.new("RGBA", (180, 180), orange)
    apple.alpha_composite(gun.resize((160, 160), Image.NEAREST), (10, 10))
    apple.convert("RGB").save(os.path.join(site, "app", "apple-icon.png"))
    icon.save(os.path.join(site, "app", "favicon.ico"), sizes=[(16, 16), (32, 32), (64, 64)])


def shot(src, crop):
    im = Image.open(src).convert("RGB")
    if crop:
        im = im.crop(crop)
    if im.width > 1920:
        im = im.resize((1920, round(im.height * 1920 / im.width)), Image.LANCZOS)
    return im


def og(hero):
    w, h = hero.size
    target = 1200 / 630
    if w / h > target:
        cw = round(h * target)
        box = ((w - cw) // 2, 0, (w - cw) // 2 + cw, h)
    else:
        ch = round(w / target)
        box = (0, (h - ch) // 2, w, (h - ch) // 2 + ch)
    return hero.crop(box).resize((1200, 630), Image.LANCZOS)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("site")
    ap.add_argument("--shots", help="folder with site_<name>.png from the siteshots dev test")
    ap.add_argument("--guide", help="folder with guide_<name>.png from the guideshots dev test (exports only these)")
    ap.add_argument("--logo", metavar="FILE", help="export only the logos: our official logo from FILE, BoehMod's from the jar")
    a = ap.parse_args()
    if a.guide:
        guide(os.path.abspath(a.site), a.guide)
        return
    if a.logo:
        logo(os.path.abspath(a.site), a.logo)
        return
    pub = os.path.join(os.path.abspath(a.site), "public")
    for d in ("icons", "ui", "shots"):
        os.makedirs(os.path.join(pub, d), exist_ok=True)
    z = zipfile.ZipFile(JAR)
    for name, entry in ICONS.items():
        Image.open(io.BytesIO(z.read(entry))).save(os.path.join(pub, "icons", name + ".png"))
    for name, entry in UI.items():
        # WebP (lossless, alpha kept) is about half the PNG; the torn edges are used as CSS mask and background
        Image.open(io.BytesIO(z.read(entry))).convert("RGBA").save(os.path.join(pub, "ui", name + ".webp"), "WEBP", lossless=True)
        old = os.path.join(pub, "ui", name + ".png")
        if os.path.isfile(old):
            os.remove(old)
    Image.open(MAC10).save(os.path.join(pub, "icons", "mac10.png"))
    site_icons(os.path.abspath(a.site))
    dirt(os.path.join(pub, "ui", "dirt.png"))
    shots_dir = os.path.join(pub, "shots")
    for old in glob.glob(os.path.join(shots_dir, "*.jpg")) + [os.path.join(pub, "og.jpg")]:
        if os.path.isfile(old):
            os.remove(old)  # older names (hashes) would pile up
    manifest = {}
    missing = []
    hero = None
    for s in SUBJECTS:
        if a.shots:
            src, crop = os.path.join(a.shots, "site_%s.png" % s), None
        else:
            rel, crop = STAND_INS[s]
            src = os.path.join(ROOT, "docs", "shots", rel)
        if not os.path.isfile(src):
            missing.append(src)
            continue
        im = shot(src, crop)
        manifest[s] = hashed(im, shots_dir, s)
        print("shot %-8s %4d x %-4d from %s -> %s" % (s, im.width, im.height, os.path.relpath(src, ROOT), manifest[s]))
        if s == "hero":
            hero = im
    if hero is not None:
        manifest["og"] = hashed(og(hero), shots_dir, "og")
    with open(os.path.join(shots_dir, "manifest.json"), "w") as f:
        json.dump(manifest, f, indent=2, sort_keys=True)
        f.write("\n")
    print("icons %d, ui 3 (panel and mask as WebP), site icons 3, shots %d" % (len(ICONS) + 1, len(SUBJECTS) - len(missing)))
    if missing:
        print("MISSING:\n  " + "\n  ".join(missing))
        sys.exit(1)


if __name__ == "__main__":
    main()

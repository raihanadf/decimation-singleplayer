"""Copies the art of DeciRemnant, the project website, into its private repo (docs/website.md section 4).

    python3 tools/site/export_assets.py SITE_DIR                 # stand in screenshots from docs/shots
    python3 tools/site/export_assets.py SITE_DIR --shots DIR     # siteshots output: DIR/site_<name>.png

Item icons and the two torn menu textures come from Decimation.jar.original.bak, the MAC-10 icon is ours, the
dirt strip is generated (seeded noise). Screenshots become JPEG (at most 1920 wide) plus og.jpg (1200 x 630 from
the hero). Everything lands in SITE_DIR/public; nothing here is committed to this public repo.
"""
import argparse
import io
import os
import random
import sys
import zipfile

from PIL import Image

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


def shot(src, crop, dst):
    im = Image.open(src).convert("RGB")
    if crop:
        im = im.crop(crop)
    if im.width > 1920:
        im = im.resize((1920, round(im.height * 1920 / im.width)), Image.LANCZOS)
    im.save(dst, quality=85)
    return im


def og(hero, dst):
    w, h = hero.size
    target = 1200 / 630
    if w / h > target:
        cw = round(h * target)
        box = ((w - cw) // 2, 0, (w - cw) // 2 + cw, h)
    else:
        ch = round(w / target)
        box = (0, (h - ch) // 2, w, (h - ch) // 2 + ch)
    hero.crop(box).resize((1200, 630), Image.LANCZOS).save(dst, quality=85)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("site")
    ap.add_argument("--shots", help="folder with site_<name>.png from the siteshots dev test")
    a = ap.parse_args()
    pub = os.path.join(os.path.abspath(a.site), "public")
    for d in ("icons", "ui", "shots"):
        os.makedirs(os.path.join(pub, d), exist_ok=True)
    z = zipfile.ZipFile(JAR)
    for name, entry in ICONS.items():
        Image.open(io.BytesIO(z.read(entry))).save(os.path.join(pub, "icons", name + ".png"))
    for name, entry in UI.items():
        Image.open(io.BytesIO(z.read(entry))).save(os.path.join(pub, "ui", name + ".png"))
    Image.open(MAC10).save(os.path.join(pub, "icons", "mac10.png"))
    dirt(os.path.join(pub, "ui", "dirt.png"))
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
        im = shot(src, crop, os.path.join(pub, "shots", s + ".jpg"))
        print("shot %-8s %4d x %-4d from %s" % (s, im.width, im.height, os.path.relpath(src, ROOT)))
        if s == "hero":
            hero = im
    if hero is not None:
        og(hero, os.path.join(pub, "og.jpg"))
    print("icons %d, ui 3, shots %d" % (len(ICONS) + 1, len(SUBJECTS) - len(missing)))
    if missing:
        print("MISSING:\n  " + "\n  ".join(missing))
        sys.exit(1)


if __name__ == "__main__":
    main()

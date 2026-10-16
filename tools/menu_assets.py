#!/usr/bin/env python3
"""Real scenes of our world in Decimation's own main menu (docs/main_menu.md; user, 17 Oktober 2026: "replace with real
scene", "just use component from decimation gui menu its self").

- Decimation's menu backgrounds (textures/gui/menu/background0..2.png, one picked at random each launch, drawn by
  RenderUtil.drawMenuBackground with its slow sway, mouse parallax and zoom; PatchMenuBackground removes the black
  layer and the cube planet over them) become six real scenes of our world (background0..5), dimmed for the buttons
  in front; fixes/MenuBackground shows them one after another, 7 seconds each.
- The pictures of our four news panels (textures/gui/menu/news_<id>.png, fixes/MenuNews), cropped to the panel's
  picture box (116 x 50, so 2.32 : 1).

Source: the website's real screenshots (docs/shots/site_v0.62.9, dev test siteshots). Written with `zip` into the
three jars that carry Decimation (Decimation.jar.patched, dist/Decimation.jar, dev/libs/Decimation-base.jar); nothing
here is committed (Decimation art). Run it again after new site shots and before a release (tools/onejar.py takes
dist/Decimation.jar).

    python3 tools/menu_assets.py [--shots DIR] [--only-dir OUT]   # --only-dir: write the PNGs there, touch no jar
"""
import argparse
import os
import shutil
import subprocess
import sys

from PIL import Image, ImageEnhance

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JARS = ["Decimation.jar.patched", "dist/Decimation.jar", "dev/libs/Decimation-base.jar"]
MENU = "assets/deci/textures/gui/menu/"
# background slot -> site shot: cities first (user: "one of our generated cities"), then a base at sunset and the airport
BACKGROUNDS = {"background0": "site_hero", "background1": "site_refuge", "background2": "site_gate",
               "background3": "site_bridge", "background4": "site_base", "background5": "site_airport"}
DIM, COLOUR = 0.62, 0.8  # brightness and saturation of the backgrounds: the menu's text stays readable on them
# news panel -> site shot and the crop's centre (fraction of width, height)
NEWS = {
    "news_github": ("site_gun", (0.42, 0.62)),      # our own MAC-10: the code on GitHub
    "news_update": ("site_airport", (0.5, 0.55)),   # the newest feature of the latest work
    "news_website": ("site_camp", (0.5, 0.5)),
    "news_coming": ("site_base", (0.5, 0.45)),      # sunset over a base: what comes next
}
NEWS_SIZE = (464, 200)


def crop_ratio(im, centre, ratio):
    """The largest crop of `ratio` (w / h) around `centre` that fits the picture."""
    w, h = im.size
    cw, ch = (w, round(w / ratio)) if w / h < ratio else (round(h * ratio), h)
    cx = min(max(round(centre[0] * w - cw / 2), 0), w - cw)
    cy = min(max(round(centre[1] * h - ch / 2), 0), h - ch)
    return im.crop((cx, cy, cx + cw, cy + ch))


def build(shots, out):
    os.makedirs(out, exist_ok=True)
    made = []
    for slot, shot in BACKGROUNDS.items():
        im = Image.open(os.path.join(shots, shot + ".png")).convert("RGB")
        im = ImageEnhance.Color(im).enhance(COLOUR)
        im = ImageEnhance.Brightness(im).enhance(DIM)
        im.save(os.path.join(out, slot + ".png"), optimize=True)
        made.append((slot, shot, im.size))
    for name, (shot, centre) in NEWS.items():
        im = Image.open(os.path.join(shots, shot + ".png")).convert("RGB")
        im = crop_ratio(im, centre, NEWS_SIZE[0] / NEWS_SIZE[1]).resize(NEWS_SIZE, Image.LANCZOS)
        im.save(os.path.join(out, name + ".png"), optimize=True)
        made.append((name, shot, im.size))
    for name, shot, size in made:
        print("%-12s from %-13s %4d x %d" % (name, shot, size[0], size[1]))
    return [n for n, _, _ in made]


def entries(jar):
    out = subprocess.run(["unzip", "-l", jar], capture_output=True, text=True, check=True).stdout.strip().splitlines()
    return int(out[-1].split()[1])


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--shots", default=os.path.join(ROOT, "docs", "shots", "site_v0.62.9"))
    ap.add_argument("--only-dir", help="write the pictures here and touch no jar")
    a = ap.parse_args()
    if a.only_dir:
        build(a.shots, a.only_dir)
        return
    stage = os.path.join(ROOT, "build", "menu_assets")
    shutil.rmtree(stage, ignore_errors=True)
    names = build(a.shots, os.path.join(stage, MENU))
    files = [MENU + n + ".png" for n in names]
    for jar in JARS:
        path = os.path.join(ROOT, jar)
        before = entries(path)
        subprocess.run(["zip", "-q", path] + files, cwd=stage, check=True)
        test = subprocess.run(["unzip", "-tq", path], capture_output=True, text=True).stdout.strip()
        print("%-30s entries %d -> %d | %s" % (jar, before, entries(path), test))
        if not test.startswith("No errors"):
            sys.exit(1)


if __name__ == "__main__":
    main()

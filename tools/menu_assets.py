#!/usr/bin/env python3
"""Real scenes of our world in Decimation's own main menu (docs/main_menu.md; user, 17 Oktober 2026: "replace with real
scene", "just use component from decimation gui menu its self").

- Decimation's menu backgrounds (textures/gui/menu/background0..2.png, one picked at random each launch, drawn by
  RenderUtil.drawMenuBackground with its slow sway, mouse parallax and zoom; PatchMenuBackground removes the black
  layer and the cube planet over them) become six real scenes of our world (background0..5), dimmed for the buttons
  in front; fixes/MenuBackground shows them one after another, 7 seconds each.
- The pictures of our four news panels (textures/gui/menu/news_<id>.png, fixes/MenuNews), each a real picture of its
  topic (user: "replace the picture of news panel to real topic image"): our GitHub repository page, its newest
  release, the website's top and the website's coming soon cards, taken with agent-browser at 2.32 : 1 (the panel's
  picture box is 116 x 50) into docs/shots/main_menu_v0.62.9/news_src/ (docs/main_menu.md).

- The FURY advert in the bottom right corner (textures/gui/menu/donor_rank_0.png, and donor_rank_1.png that pulses over
  it; BoehMod's closed store) becomes ours (user: "Replace FURY advert"): Decimation's own torn black panel (buttonbg)
  with "DeciRemnant", "Download and guides" and the site's address in the Minecraft font the website uses
  (tools/menu/MinecraftStandard.otf, public domain); the title pulses as FURY's did.
- The welcome box line in Decimation's lang file: "Welcome to Decimation v%s!" becomes "Welcome to DeciRemnant v%s!"
  (the version is ours: PatchMenuTexts).

Sources: the website's real screenshots (docs/shots/site_v0.62.9, dev test siteshots) and news_src. Written with `zip` into the
three jars that carry Decimation (Decimation.jar.patched, dist/Decimation.jar, dev/libs/Decimation-base.jar); nothing
here is committed (Decimation art). Run it again after new site shots and before a release (tools/onejar.py takes
dist/Decimation.jar).

    python3 tools/menu_assets.py [--shots DIR] [--only-dir OUT]   # --only-dir: write the PNGs there, touch no jar
"""
import argparse
import io
import os
import shutil
import subprocess
import sys
import zipfile

from PIL import Image, ImageDraw, ImageEnhance, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JARS = ["Decimation.jar.patched", "dist/Decimation.jar", "dev/libs/Decimation-base.jar"]
MENU = "assets/deci/textures/gui/menu/"
# background slot -> site shot: cities first (user: "one of our generated cities"), then a base at sunset and the airport
BACKGROUNDS = {"background0": "site_hero", "background1": "site_refuge", "background2": "site_gate",
               "background3": "site_bridge", "background4": "site_base", "background5": "site_airport"}
DIM, COLOUR = 0.62, 0.8  # brightness and saturation of the backgrounds: the menu's text stays readable on them
# news panel -> its topic picture in news_src (taken at 2.32 : 1; cropped around the centre if not)
NEWS = {"news_github": "github", "news_update": "update", "news_website": "website", "news_coming": "coming"}
NEWS_SRC = os.path.join(ROOT, "docs", "shots", "main_menu_v0.62.9", "news_src")
NEWS_SIZE = (464, 200)


ORIGINAL = os.path.join(ROOT, "Decimation.jar.original.bak")
FONT = os.path.join(ROOT, "tools", "menu", "MinecraftStandard.otf")
LANG = "assets/deci/lang/en_US.lang"


def corner():
    """Our picture for the FURY corner (drawn 150 x 150 at the bottom right): donor_rank_0 the panel and the words,
    donor_rank_1 a lighter title the menu pulses over it."""
    with zipfile.ZipFile(ORIGINAL) as z:
        panel = Image.open(io.BytesIO(z.read(MENU + "buttonbg.png"))).convert("RGBA")
    base = Image.new("RGBA", (800, 800), (0, 0, 0, 0))
    glow = Image.new("RGBA", (800, 800), (0, 0, 0, 0))
    panel = panel.resize((620, 270), Image.LANCZOS)
    base.alpha_composite(panel, (180, 520))
    lines = [("DeciRemnant", 66, (255, 85, 85), 575), ("Download and guides", 30, (240, 236, 228), 668),
             ("deciremnant.vercel.app", 26, (229, 148, 104), 718)]
    for text, size, colour, y in lines:
        font = ImageFont.truetype(FONT, size)
        while font.getbbox(text)[2] > 540:  # inside the panel with a margin
            size -= 2
            font = ImageFont.truetype(FONT, size)
        w = font.getbbox(text)[2]
        x = 180 + (620 - w) // 2
        d = ImageDraw.Draw(base)
        d.text((x + size // 10, y + size // 10), text, font=font, fill=(25, 8, 8, 230))  # the game's text shadow
        d.text((x, y), text, font=font, fill=colour + (255,))
        if text == "DeciRemnant":
            ImageDraw.Draw(glow).text((x, y), text, font=font, fill=(255, 210, 200, 255))
    return {"donor_rank_0": base, "donor_rank_1": glow}


def lang(out):
    """Decimation's lang file with our name in the welcome box (the version there is ours through PatchMenuTexts)."""
    with zipfile.ZipFile(ORIGINAL) as z:
        text = z.read(LANG).decode("utf-8")
    old = "menu.notification.welcome2=Welcome to Decimation v%s! Have fun!"
    if old not in text:
        sys.exit("the welcome line changed in Decimation's lang file")
    path = os.path.join(out, LANG)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="") as f:
        f.write(text.replace(old, "menu.notification.welcome2=Welcome to DeciRemnant v%s! Have fun!"))
    return LANG


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
    for name, im in corner().items():
        im.save(os.path.join(out, name + ".png"), optimize=True)
        made.append((name, "buttonbg + text", im.size))
    for name, topic in NEWS.items():
        im = Image.open(os.path.join(NEWS_SRC, topic + ".png")).convert("RGB")
        im = crop_ratio(im, (0.5, 0.5), NEWS_SIZE[0] / NEWS_SIZE[1]).resize(NEWS_SIZE, Image.LANCZOS)
        im.save(os.path.join(out, name + ".png"), optimize=True)
        made.append((name, "news_src/" + topic, im.size))
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
    files = [MENU + n + ".png" for n in names] + [lang(stage)]
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

#!/usr/bin/env python3
"""Reference views of a gun build from tarkovbot.eu's weapon builder (docs/ideas.md item 26). REFERENCE ONLY.

The user builds a gun in the builder and downloads its preset (JSON). This loads that preset into the builder's 3D view
with agent-browser, hides the slot points, turns the view to fixed angles by mouse drags and takes each view twice, on
the dark and on the light backdrop: the pixels equal in both are the gun (no backdrop pixel matched in the runs
checked), so each view is cut out cleanly. Written to OUTDIR: one cut out PNG per view, views.json (each silhouette's box in pixels; all
views share one camera distance, so their sizes compare), parts.txt (the part names on the builder's slot boxes) and
sheet.png. Nothing is downloaded from the site. The pictures show Battlestate Games' models: they stay local
(docs/shots, git ignored) and never go into the repo, a jar or the website.

    python3 tools/guns/refcapture.py PRESET.json OUTDIR [--views side,end_a,...] [--keep-raw]
    python3 tools/guns/refcapture.py PRESET.json OUTDIR --fill=0.92 [--size=2560x1600]   # close ups: the gun large
    python3 tools/guns/refcapture.py --selftest

Turning: the builder's view behaves like three.js OrbitControls (a drag across the canvas height turns 360 degrees,
checked with the end on views being narrow); a drag down far enough looks from straight above. Its reset view is close
(strong perspective, ends cut by the frame): every view is zoomed out first by the same number of wheel steps, worked
out once so the side view fills about a third of the frame's width (less perspective). The view has no pan (right
drag, Shift drag and arrow keys do not move it; the arrows even drop the gun) and its wheel zooms to the middle, not
to the cursor (checked 21 Oktober 2026), so close ups of a part are the whole gun filling the frame: --fill=0.92 zooms
until the side view takes 92 % of the width (more perspective, every part in view at about 2.6 times the pixels),
--size a bigger frame for more pixels still.
"""

import json
import math
import os
import subprocess
import sys
import time

from PIL import Image, ImageChops, ImageDraw

URL = "https://tarkovbot.eu/weapon-builder"
W, H = 2560, 1600
# name: (horizontal drag, vertical drag) in quarter turns from the reset view (side on, muzzle left)
VIEWS = {
    "side": (0, 0),
    "side_far": (2, 0),
    "end_a": (1, 0),
    "end_b": (-1, 0),
    "top": (0, 1.2),        # past the pole: the view stops at straight above
    "bottom": (0, -1.2),    # and straight below
    "three_quarter_a": (0.5, 0.33),
    "three_quarter_b": (1.5, 0.33),
}


def ab(*args, check=True):
    """One agent-browser command; its output."""
    r = subprocess.run(["agent-browser"] + [str(a) for a in args], capture_output=True, text=True)
    if check and r.returncode != 0:
        raise SystemExit("agent-browser %s failed: %s" % (" ".join(map(str, args)), (r.stderr or r.stdout).strip()[:300]))
    return r.stdout.strip()


def js(code):
    """A script's result: agent-browser prints it as JSON (a string result comes back quoted)."""
    out = ab("eval", code)
    try:
        return json.loads(out)
    except ValueError:
        return out


def cut_out(dark, light):
    """The gun alone: pixels equal on both backdrops kept, the rest clear. Returns (RGBA picture, mask)."""
    a, b = dark.convert("RGB"), light.convert("RGB")
    r, g, bl = ImageChops.difference(a, b).split()
    most = ImageChops.lighter(ImageChops.lighter(r, g), bl)
    mask = most.point(lambda v: 255 if v == 0 else 0)
    cut = Image.new("RGBA", a.size, (0, 0, 0, 0))
    cut.paste(a, mask=mask)
    return cut, mask


def shot(path):
    ab("screenshot", path)
    return Image.open(path)


def settle(tmp, tries=15):
    """Waits until two screenshots a second apart are equal (textures loaded, the view still)."""
    last = None
    for i in range(tries):
        img = shot(os.path.join(tmp, "settle.png")).convert("RGB")
        if last is not None and ImageChops.difference(img, last).getbbox() is None:
            return
        last = img
        time.sleep(1)
    print("view never settled; going on")


def drag(dx, dy, button="left", start=None):
    """A mouse drag across the canvas (from its middle unless `start`), in steps; the left button turns, the right
    pans."""
    x0, y0 = start or (W // 2, H // 2)
    ab("mouse", "move", x0, y0)
    ab("mouse", "down", button)
    steps = max(1, int(max(abs(dx), abs(dy)) / 20))
    for i in range(1, steps + 1):
        ab("mouse", "move", int(x0 + dx * i / steps), int(y0 + dy * i / steps))
    ab("mouse", "up", button)


def steps_for(w0, w5, target):
    """Wheel steps from the reset view to a side view `target` pixels wide, from its width at the reset (w0) and after
    5 steps out (w5): the apparent size goes with the inverse of the camera's distance, which every step multiplies
    by the same factor."""
    if w0 <= target or w5 >= w0:
        return 0
    step = (w0 / float(w5)) ** (1 / 5.0)
    return max(0, min(60, int(math.ceil(math.log(w0 / float(target)) / math.log(step)))))


def dark_bg(on):
    js("(()=>{const c=document.querySelector('input[type=checkbox]'); if(c.checked!==%s) c.click(); return c.checked})()"
       % ("true" if on else "false"))


TITLE = ("[...document.querySelectorAll('div')].filter(d=>d.children.length==0&&/rifle|carbine|pistol|shotgun|machine|SMG|"
         "launcher|revolver|grenade/i.test(d.textContent)).map(d=>d.textContent.trim())[0]||''")
# each slot box (.attachment-square) names its fitted part in .slot-type; an empty slot shows only the slot's name
PARTS = "JSON.stringify([...document.querySelectorAll('.attachment-square .slot-type')].map(e=>e.textContent.trim()))"


def state():
    parts = js(PARTS)
    return js(TITLE), json.loads(parts) if isinstance(parts, str) and parts.startswith("[") else []


def wait_for(test, seconds):
    end = time.time() + seconds
    while time.time() < end:
        v = test()
        if v:
            return v
        time.sleep(1)
    return None


def setup(preset, tmp):
    """The builder with the preset loaded (the upload is retried until the gun or its parts change), the slot points
    hidden, every page element but the 3D view hidden, the dark backdrop on."""
    ab("open", URL)
    ab("set", "viewport", W, H)
    before = wait_for(lambda: (lambda st: st if st[0] else None)(state()), 60)  # a cold load took over 30 s once
    if before is None:
        raise SystemExit("the builder did not come up")
    loaded = None
    for attempt in range(3):
        ab("upload", "#loadPreset", os.path.abspath(preset))
        loaded = wait_for(lambda: (lambda s: s if s != before and s[0] else None)(state()), 20)
        if loaded:
            break
        print("preset not loaded yet, upload again")
    if loaded is None:
        print("warning: the page looks the same after loading the preset (the builder's default gun?)")
        loaded = state()
    time.sleep(3)  # the parts' models and textures
    title, parts = state()
    js("document.getElementById('togglePoints').click()")
    js("(()=>{const keep=document.getElementById('canvas'); for(const el of document.body.querySelectorAll('*')){"
       "if(el===keep||el.contains(keep)||keep.contains(el)) continue; el.style.visibility='hidden';} return 1})()")
    dark_bg(True)
    return title, parts


def reset(zoom_steps=0):
    js("document.getElementById('resetView').click()")
    time.sleep(1)
    if zoom_steps:
        ab("mouse", "move", W // 2, H // 2)
        for _ in range(zoom_steps):
            ab("mouse", "wheel", 100)
        time.sleep(0.5)


def pair(tmp, name):
    """The current view on both backdrops, cut out: (RGBA picture, mask, dark shot)."""
    settle(tmp)
    dark = shot(os.path.join(tmp, name + "_dark.png"))
    dark_bg(False)
    time.sleep(1)
    settle(tmp)
    light = shot(os.path.join(tmp, name + "_light.png"))
    dark_bg(True)
    cut, mask = cut_out(dark, light)
    return cut, mask


def zoom_steps(tmp, target=0.35):
    """Wheel steps that bring the side view to `target` of the frame's width (two measurements, steps_for)."""
    reset()
    box = pair(tmp, "zoom0")[1].getbbox()
    w0 = box[2] - box[0] if box else W
    reset(5)
    box = pair(tmp, "zoom5")[1].getbbox()
    w5 = box[2] - box[0] if box else w0
    n = steps_for(w0, w5, target * W)
    print("zoom: side %d px at the reset, %d after 5 steps: %d steps for %.0f%%" % (w0, w5, n, target * 100))
    return n


def capture(preset, out, names, fill=0.35):
    tmp = os.path.join(out, "raw")
    os.makedirs(tmp, exist_ok=True)
    title, parts = setup(preset, tmp)
    quarter = H / 4  # OrbitControls: a drag across the canvas height turns 360 degrees
    steps = zoom_steps(tmp, fill)
    views = {}
    for name in names:
        turns, tilt = VIEWS[name]
        reset(steps)
        drag(turns * quarter, tilt * quarter)
        cut, mask = pair(tmp, name)
        box = mask.getbbox()
        if box is None:
            print(name, ": nothing cut out")
            continue
        cut.crop(box).save(os.path.join(out, name + ".png"))
        views[name] = {"box": list(box), "width": box[2] - box[0], "height": box[3] - box[1],
                       "pixels": mask.histogram()[255], "touches_edge": box[0] == 0 or box[1] == 0 or box[2] == W
                       or box[3] == H}
        print("%-16s %4d x %-4d %s" % (name, views[name]["width"], views[name]["height"],
                                       "CUT BY THE EDGE" if views[name]["touches_edge"] else ""))
    side = views.get("side")
    for e in ("end_a", "end_b"):
        if side and e in views and views[e]["width"] > 0.3 * side["width"]:
            print("warning: %s is %d wide against the side's %d: the turn is not a quarter (calibration?)"
                  % (e, views[e]["width"], side["width"]))
    with open(os.path.join(out, "views.json"), "w") as f:
        json.dump({"preset": os.path.basename(preset), "title": title, "frame": [W, H], "zoom_steps": steps,
                   "fill": fill, "views": views}, f, indent=1)
    with open(os.path.join(out, "parts.txt"), "w") as f:
        f.write(title + "\n" + "\n".join(parts) + "\n")
    sheet(out, [n for n in names if n in views])


def sheet(out, names):
    cells = [Image.open(os.path.join(out, n + ".png")) for n in names]
    cw = 480
    rows = (len(cells) + 1) // 2
    page = Image.new("RGB", (cw * 2, rows * 300), (40, 40, 40))
    d = ImageDraw.Draw(page)
    for i, (n, c) in enumerate(zip(names, cells)):
        c.thumbnail((cw - 20, 260))
        x, y = (i % 2) * cw, (i // 2) * 300
        page.paste(c, (x + 10, y + 30), c)
        d.text((x + 10, y + 8), n, fill=(230, 230, 230))
    page.save(os.path.join(out, "sheet.png"))


def selftest():
    """The cut out on a made up pair: the same object on two backdrops comes back exactly, the backdrop clear."""
    dark, light = Image.new("RGB", (40, 30), (10, 20, 30)), Image.new("RGB", (40, 30), (200, 210, 220))
    for img in (dark, light):
        ImageDraw.Draw(img).rectangle((5, 6, 24, 15), fill=(60, 60, 60))
    ImageDraw.Draw(light).point((30, 25), fill=(10, 210, 220))  # a backdrop pixel equal in one channel only: still backdrop
    cut, mask = cut_out(dark, light)
    assert mask.getbbox() == (5, 6, 25, 16), mask.getbbox()
    assert mask.histogram()[255] == 20 * 10, mask.histogram()[255]
    assert cut.getpixel((10, 10)) == (60, 60, 60, 255) and cut.getpixel((0, 0))[3] == 0
    # 2440 px at the reset, 1900 after 5 steps: 21 steps to a third of 2560 (as measured), 1 step to 0.92
    assert steps_for(2440, 1900, 0.35 * 2560) == 21, steps_for(2440, 1900, 0.35 * 2560)
    assert steps_for(2440, 1900, 0.92 * 2560) == 1, steps_for(2440, 1900, 0.92 * 2560)
    assert steps_for(2000, 1900, 0.95 * 2560) == 0  # already inside the target
    print("selftest ok")


def main(argv):
    if argv[:1] == ["--selftest"]:
        selftest()
        return
    if len(argv) < 2:
        raise SystemExit(__doc__)
    global W, H
    names, fill = list(VIEWS), 0.35
    for a in argv[2:]:
        if a.startswith("--views="):
            names = a[len("--views="):].split(",")
        elif a.startswith("--fill="):
            fill = float(a[len("--fill="):])
        elif a.startswith("--size="):
            W, H = (int(v) for v in a[len("--size="):].split("x"))
    os.makedirs(argv[1], exist_ok=True)
    capture(argv[0], argv[1], names, fill)
    ab("close", check=False)
    if "--keep-raw" not in argv:
        import shutil
        shutil.rmtree(os.path.join(argv[1], "raw"), ignore_errors=True)  # the full frames: tens of MB


if __name__ == "__main__":
    main(sys.argv[1:])

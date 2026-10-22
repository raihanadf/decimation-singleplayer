#!/usr/bin/env python3
"""Stand in gun packages for the game side of the 3D gun converter (docs/superpowers/specs/2026-10-22-gun-converter-
design.md; docs/gun_model_spec.md section 7): packages in the exact contract format, so the loader (fixes/gunpack) is
built and tested before the converter's first rifle exists. Written to build/gunpacks/ (git ignored), which the dev
client reads without flags.

    python3 tools/guns/gunpack_standin.py            # gcmac10, gcm4a4 (needs Decimation.jar), gcbroken
    python3 tools/guns/gunpack_standin.py --out DIR  # somewhere else

- gcmac10: our own MAC-10 (dev/src/main/resources/assets/deci, committed, our work) under another name, gun.json with
  the numbers fixes/NewGuns gives the MAC-10 as overrides of the Uzi (its like gun), its own magazine like the UMP45's
  (.45 ACP) with the Uzi magazine's icon, the suppressor shift NewGuns applies. License "own".
- gcm4a4: Decimation's M4A4 read from the user's Decimation.jar (Decimation's art: local only, license "test", which the
  loader accepts only in the dev workspace), like m4a4, its magazines, the points measured as sightfit / gripfit see the
  M4A4 (rail top -3.6, bottom rail under the handguard 8.5 .. 14.7). A rifle with a rail: grips, sights, mounts.
- gcbroken: a package the loader must refuse with its reason (a valid gun.json, its icon missing: the file check; the
  gun.json checks are unit tested, GunPackTest).
The dev test `gunpack` checks that gc<gun> gets the same attachment rows as <gun>.
"""
import json
import os
import shutil
import sys
import zipfile

ROOT = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
OURS = os.path.join(ROOT, "dev", "src", "main", "resources", "assets", "deci")
JAR = os.path.join(ROOT, "Decimation.jar")
KINDS = ["Fire", "Rack", "Reload1", "SlideBack"]


def paths(name, cat):
    """Contract paths inside a package (under assets/deci/)."""
    out = {"model": "models/guns/%s/%s.bmodel" % (cat, name),
           "texture": "textures/model/guns/%s/%s/%s.png" % (cat, name, name),
           "icon": "textures/items/gun/%s/%s.png" % (cat, name)}
    for k in KINDS:
        out[k] = "animations/%s/%s%s.anib" % (name, name, k)
    return out


def write(pkg, rel, data):
    f = os.path.join(pkg, "assets", "deci", rel)
    os.makedirs(os.path.dirname(f), exist_ok=True)
    with open(f, "wb") as o:
        o.write(data)


def package(out, name, cat, read, gun):
    """A package folder from a reader of the source gun's files (key -> bytes or None)."""
    pkg = os.path.join(out, name)
    if os.path.isdir(pkg):
        shutil.rmtree(pkg)
    os.makedirs(pkg)
    for key, rel in paths(name, cat).items():
        data = read(key)
        if data is not None:
            write(pkg, rel, data)
    with open(os.path.join(pkg, "gun.json"), "w") as o:
        json.dump(gun, o, indent=2)
        o.write("\n")
    return pkg


def mac10(out):
    src = paths("mac10", "smg")

    def read(key):
        f = os.path.join(OURS, src[key])
        return open(f, "rb").read() if os.path.isfile(f) else None
    gun = {"name": "gcmac10", "display": "MAC-10 (gun package)", "category": "smg", "like": "uzi",
           "magazine": {"name": "gcmac10Mag", "capacity": 30, "like": "ump45Mag", "icon": "uziMag"},
           "stats": {"damage": 12, "rpm": 1100, "pitch": 5.5, "yaw": 0.45, "recovery": 7.0, "slowdown": 0.08, "auto": True},
           "points": {"flame": [10.4, -4.75, -0.15], "railTop": None, "rail": None, "gripRail": None, "mount": None,
                      "barrelShift": {"smgSuppressor": [-2.37, -0.16, 0.0]}},
           "source": {"file": "dev/src/main/resources/assets/deci/models/guns/smg/mac10.bmodel",
                      "author": "DeciRemnant (tools/guns/mac10.py)", "url": "", "license": "own"}}
    return package(out, "gcmac10", "smg", read, gun)


def m4a4(out):
    if not os.path.isfile(JAR):
        print("gcm4a4 skipped: no Decimation.jar")
        return None
    z = zipfile.ZipFile(JAR)
    src = paths("m4a4", "rifle")

    def read(key):
        n = "assets/deci/" + src[key]
        return z.read(n) if n in z.namelist() else None
    gun = {"name": "gcm4a4", "display": "M4A4 (gun package test)", "category": "rifle", "like": "m4a4", "magazine": "like",
           "points": {"flame": [19.14997, -3.400001, -0.15], "railTop": -3.6, "rail": [0.0, 14.7], "gripRail": [8.5, 14.7],
                      "mount": "rail"},
           "source": {"file": "Decimation.jar assets/deci/models/guns/rifle/m4a4.bmodel", "author": "BoehMod (Decimation)",
                      "url": "", "license": "test"}}
    return package(out, "gcm4a4", "rifle", read, gun)


def broken(out):
    src = paths("mac10", "smg")

    def read(key):
        if key == "icon":
            return None                     # missing on purpose
        f = os.path.join(OURS, src[key])
        return open(f, "rb").read() if os.path.isfile(f) else None
    gun = {"name": "gcbroken", "display": "Broken (must be refused)", "category": "smg", "like": "uzi", "magazine": "like",
           "points": {"flame": [10.4, -4.75, -0.15], "railTop": None, "rail": None, "gripRail": None, "mount": None},
           "source": {"file": "none", "author": "DeciRemnant", "url": "", "license": "own"}}
    return package(out, "gcbroken", "smg", read, gun)


def main():
    out = os.path.join(ROOT, "build", "gunpacks")
    if "--out" in sys.argv:
        out = os.path.abspath(sys.argv[sys.argv.index("--out") + 1])
    os.makedirs(out, exist_ok=True)
    for make in (mac10, m4a4, broken):
        pkg = make(out)
        if pkg:
            files = sum(len(fs) for _, _, fs in os.walk(pkg))
            print("%-9s %d files -> %s" % (os.path.basename(pkg), files, pkg))


if __name__ == "__main__":
    main()

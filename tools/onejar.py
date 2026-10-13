#!/usr/bin/env python3
"""One release jar with everything (user, 13 Oktober 2026: "why you using splited mod? why not combine
it with one"): Decimation with our bytecode fixes (dist/Decimation.jar), our mod (the reobfuscated
deciworldgen jar from dev/build/libs) and the world generation content (dist/config/decimation_worldgen:
the converted city pack lc/, large schematics, small schematics) in ONE Decimation.jar.

- Every entry of the Decimation jar is copied as it is, then every entry of ours except its manifest;
  mcmod.info becomes one list with both mods (deci and deciworldgen).
- The content goes under assets/deciworldgen/content/ with manifest.txt (the files) and version.txt
  (a hash of them); worldgen/BundledContent unpacks it into config/decimation_worldgen/ on start.
- Read straight from zip to zip: never extracted onto the case insensitive disk (CLAUDE.md trap).

usage: python3 tools/onejar.py VERSION|OUR_JAR [OUT]   (OUT default dist/onejar/Decimation.jar)
Never commit the output: it is Decimation's jar."""
import hashlib
import json
import os
import sys
import zipfile

PROJ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BASE = os.path.join(PROJ, "dist", "Decimation.jar")
CONTENT = os.path.join(PROJ, "dist", "config", "decimation_worldgen")
SKIP_DIRS = {"paste", "palettes", "sets", "styles"}   # dev scratch; built-in assets come from the jar itself


def main():
    version = sys.argv[1]
    if version.endswith(".jar"):                       # a test build: its own path, version from the name
        ours = version
        version = os.path.basename(version)[len("deciworldgen-"):-len(".jar")]
    else:
        ours = os.path.join(PROJ, "dev", "build", "libs", "deciworldgen-%s.jar" % version)
    out = sys.argv[2] if len(sys.argv) > 2 else os.path.join(PROJ, "dist", "onejar", "Decimation.jar")
    os.makedirs(os.path.dirname(out), exist_ok=True)
    tmp = out + ".part"

    files = []
    for root, dirs, names in os.walk(CONTENT):
        dirs[:] = sorted(d for d in dirs if d not in SKIP_DIRS)
        for n in sorted(names):
            if n.startswith("."):
                continue
            p = os.path.join(root, n)
            files.append(os.path.relpath(p, CONTENT).replace(os.sep, "/"))
    h = hashlib.sha1()
    for f in files:
        h.update(f.encode())
        with open(os.path.join(CONTENT, f), "rb") as fh:
            h.update(fh.read())
    content_version = "%s-%s" % (version, h.hexdigest()[:12])

    base = zipfile.ZipFile(BASE)
    mine = zipfile.ZipFile(ours)
    names = set()
    with zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as z:
        for info in base.infolist():
            if info.filename == "mcmod.info":
                continue
            z.writestr(info, base.read(info.filename), compress_type=info.compress_type)
            names.add(info.filename)
        for info in mine.infolist():
            n = info.filename
            if n in ("mcmod.info", "META-INF/MANIFEST.MF") or n.endswith("/"):
                continue
            if n in names:
                raise SystemExit("clash: %s is in both jars" % n)
            z.writestr(info, mine.read(n), compress_type=zipfile.ZIP_DEFLATED)
            names.add(n)
        deci = json.loads(base.read("mcmod.info").decode("utf-8"))
        wg = json.loads(mine.read("mcmod.info").decode("utf-8"))
        mods = (deci if isinstance(deci, list) else deci.get("modList", [])) + \
               (wg if isinstance(wg, list) else wg.get("modList", []))
        z.writestr("mcmod.info", json.dumps({"modListVersion": 2, "modList": mods}, indent=2))
        root = "assets/deciworldgen/content/"
        for f in files:
            z.write(os.path.join(CONTENT, f), root + f)
        z.writestr(root + "manifest.txt", "\n".join(files) + "\n")
        z.writestr(root + "version.txt", content_version + "\n")
    os.replace(tmp, out)
    total = len(zipfile.ZipFile(out).namelist())
    print("wrote %s: %d entries (Decimation %d, ours %d, content %d files, content version %s)"
          % (out, total, len(base.namelist()), len(mine.namelist()), len(files), content_version))


if __name__ == "__main__":
    main()

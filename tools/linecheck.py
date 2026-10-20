#!/usr/bin/env python3
"""Can a pre-release ship main's Decimation jar? (CLAUDE.md "RELEASING FROM IT" step 1)

    python3 tools/linecheck.py BASE BUILD [--last LAST]

BASE: main's dist/Decimation.jar (Decimation with our jar patches, one jar shared by both lines). BUILD: the
prerelease build of our mod (dev/build/libs/deciworldgen-VERSION.jar of the worktree). LAST: the last release's one jar
(dist/previous/onejar-VERSION/Decimation.jar).

1. Every call or field use from Decimation's patched classes into our code (net/decimation/fixes, worldgen) must exist
   in BUILD with the same descriptor, else the pre-release crashes (NoSuchMethodError) where the patch runs. Main only
   behaviour belongs in our classes, never in a jar patch: a patch both lines share calls a hook both lines have.
2. Entries only main may ship (STRIP) are named: the pre-release's base jar is BASE without them (zip -d).
3. With LAST: Decimation's entries that changed since the last release, to read one by one (each must be a plain fix).
Exit code 1 when a reference is missing.
"""
import struct
import sys
import zipfile

OURS = ("net/decimation/fixes/", "net/decimation/worldgen/")
STRIP = ["assets/deci/textures/gui/menu/news_multiplayer.png"]  # the Multiplayer news panel (main only)


def pool(data):
    """The constant pool of a class file: index -> tuple, and the offset after it."""
    count = struct.unpack(">H", data[8:10])[0]
    cp = [None] * count
    i, off = 1, 10
    while i < count:
        tag = data[off]
        if tag == 1:
            n = struct.unpack(">H", data[off + 1:off + 3])[0]
            cp[i] = ("utf8", data[off + 3:off + 3 + n].decode("utf-8", "replace"))
            off += 3 + n
        elif tag in (3, 4):
            off += 5
        elif tag in (5, 6):
            off += 9
            i += 1
        elif tag == 7:
            cp[i] = ("class", struct.unpack(">H", data[off + 1:off + 3])[0])
            off += 3
        elif tag in (8, 16):
            off += 3
        elif tag in (9, 10, 11):
            cp[i] = ("ref", tag) + struct.unpack(">HH", data[off + 1:off + 5])
            off += 5
        elif tag == 12:
            cp[i] = ("nat",) + struct.unpack(">HH", data[off + 1:off + 5])
            off += 5
        elif tag == 15:
            off += 4
        elif tag == 18:
            off += 5
        else:
            raise ValueError("constant pool tag %d" % tag)
        i += 1
    return cp, off


def utf(cp, i):
    return cp[i][1]


def refs(data):
    """(kind, owner, name, descriptor) of every field or method reference into our code."""
    cp, _ = pool(data)
    out = set()
    for e in cp:
        if e and e[0] == "ref":
            owner = utf(cp, cp[e[2]][1])
            if owner.startswith(OURS):
                nat = cp[e[3]]
                out.add(("field" if e[1] == 9 else "method", owner, utf(cp, nat[1]), utf(cp, nat[2])))
    return out


def members(data):
    """This class, its super class and its fields and methods as (kind, name, descriptor)."""
    cp, off = pool(data)
    this, sup = struct.unpack(">HH", data[off + 2:off + 6])
    n = struct.unpack(">H", data[off + 6:off + 8])[0]
    off += 8 + 2 * n
    found = set()
    for kind in ("field", "method"):
        count = struct.unpack(">H", data[off:off + 2])[0]
        off += 2
        for _ in range(count):
            name, desc, attrs = struct.unpack(">HHH", data[off + 2:off + 8])
            found.add((kind, utf(cp, name), utf(cp, desc)))
            off += 8
            for _ in range(attrs):
                off += 6 + struct.unpack(">I", data[off + 2:off + 6])[0]
    return utf(cp, cp[this][1]), utf(cp, cp[sup][1]) if sup else None, found


def classes(jar, keep):
    with zipfile.ZipFile(jar) as z:
        for e in z.infolist():
            if e.filename.endswith(".class") and keep(e.filename):
                yield e.filename, z.read(e)


def check(base, build, last=None):
    used = {}
    for name, data in classes(base, lambda n: not n.startswith(OURS)):
        for r in refs(data):
            used.setdefault(r, set()).add(name[:-6])
    have = {}
    for _, data in classes(build, lambda n: n.startswith(OURS)):
        this, sup, found = members(data)
        have[this] = (sup, found)
    missing = []
    for r in sorted(used):
        kind, owner, name, desc = r
        c = owner
        while c in have and (kind, name, desc) not in have[c][1]:
            c = have[c][0]                               # inherited from a class of ours above it
        if c not in have:
            missing.append(r)
    users = set().union(*used.values()) if used else set()
    print("references into our code: %d, from %d Decimation classes" % (len(used), len(users)))
    for kind, owner, name, desc in missing:
        print("MISSING in %s: %s %s.%s %s (used by %s)" % (build, kind, owner, name, desc,
                                                          ", ".join(sorted(used[(kind, owner, name, desc)]))))
    with zipfile.ZipFile(base) as z:
        names = set(z.namelist())
        crc = {e.filename: e.CRC for e in z.infolist()}
    for s in STRIP:
        print("strip for the pre-release: %s %s" % (s, "(in BASE)" if s in names else "(not in BASE)"))
    if last:
        with zipfile.ZipFile(build) as z:
            ours = set(z.namelist())                     # the one jar also holds our mod's files (our guns' art too)
        with zipfile.ZipFile(last) as z:
            old = {e.filename: e.CRC for e in z.infolist()
                   if e.filename not in ours and not e.filename.startswith(OURS + ("assets/deciworldgen/",))}
        mine = {n: c for n, c in crc.items() if n not in STRIP and n not in ours}
        changed = sorted(n for n in mine if n in old and old[n] != mine[n])
        added = sorted(n for n in mine if n not in old)
        gone = sorted(n for n in old if n not in mine)
        print("since %s: %d changed, %d added, %d removed Decimation entries" % (last, len(changed), len(added), len(gone)))
        for label, lst in (("changed", changed), ("added", added), ("removed", gone)):
            for n in lst:
                print("  %s %s" % (label, n))
    print("PASS" if not missing else "FAIL: %d missing" % len(missing))
    return not missing


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    last = next((sys.argv[i + 1] for i, a in enumerate(sys.argv) if a == "--last" and i + 1 < len(sys.argv)), None)
    args = [a for a in args if a != last]
    if len(args) != 2:
        print(__doc__)
        return 2
    return 0 if check(args[0], args[1], last) else 1


if __name__ == "__main__":
    sys.exit(main())

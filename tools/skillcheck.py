#!/usr/bin/env python3
"""Checks that the Decimation code feature skills and docs only name things that exist.

    python3 tools/skillcheck.py [FILE ...]

Default files: .claude/skills/decimation-code-*/SKILL.md and docs/decimation_code/*.md. Each reference is looked up:
- repo paths (docs/..., tools/..., fixes/X, worldgen/..., config files are skipped): the file exists;
- obfuscated classes (deci/aK/k, net/decimation/mod/.../b, with $Inner): a class of deobf/decimation-mcp.jar;
- `Deci.name`: a method of dev/src/main/java/net/decimation/fixes/Deci.java;
- `Class.method` where Class is a readable Decimation class: a method of that class in deobf/decimation-named.jar;
- a readable class name followed by (deci/..): names.tsv maps that obfuscated class to that name;
- `devtest.py ... MODE`: a mode of DevAutoTest.mode().
Prints every reference that did not resolve; exit code 1 when any did not. Run it after writing or editing a skill
(it is the test of a reference skill: names that drift from the code are the failure).
"""
import glob
import os
import re
import sys
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, os.path.join(ROOT, "deobf"))
import apply_mappings as am  # noqa: E402  (class file reader)

JAVA = os.path.join(ROOT, "dev", "src", "main", "java", "net", "decimation")


def load():
    obf = set()
    with zipfile.ZipFile(os.path.join(ROOT, "deobf", "decimation-mcp.jar")) as z:
        obf = {n[:-6] for n in z.namelist() if n.endswith(".class")}
    methods = {}  # readable simple class name (with $Inner tail) -> member (method and field) names
    with zipfile.ZipFile(os.path.join(ROOT, "deobf", "decimation-named.jar")) as z:
        for n in z.namelist():
            if n.endswith(".class"):
                c = am.parse_class(z.read(n))
                for key in {c["name"].rsplit("/", 1)[-1], c["name"].rsplit("/", 1)[-1].split("$")[-1]}:
                    methods.setdefault(key, set()).update(m[0] for m in c["methods"] + c["fields"])
    names = {}
    for line in open(os.path.join(ROOT, "deobf", "names.tsv"), encoding="utf-8"):
        p = line.rstrip("\n").split("\t")
        if len(p) > 1:
            names[p[0]] = p[1].rsplit("/", 1)[-1]
    deci = set(re.findall(r"static [\w<>\[\], .?]+ (\w+)\(", open(os.path.join(JAVA, "fixes", "Deci.java")).read()))
    deci |= set(re.findall(r"public static final \w+ (\w+)", open(os.path.join(JAVA, "fixes", "Deci.java")).read()))
    t = open(os.path.join(JAVA, "worldgen", "DevAutoTest.java")).read()
    modes = set(re.findall(r'name\.equals\("(\w+)"\)', t))
    return obf, methods, names, deci, modes


def path_exists(p):
    p = p.rstrip(".,;:)")
    cands = [os.path.join(ROOT, p), os.path.join(JAVA, p), os.path.join(JAVA, p + ".java"), os.path.join(ROOT, p + ".java")]
    if any(os.path.exists(c) for c in cands) or glob.glob(os.path.join(ROOT, p)):
        return True
    # fixes/Loot.open: a Java file plus one of its methods
    if "." in p:
        base, member = p.rsplit(".", 1)
        for c in (os.path.join(JAVA, base + ".java"), os.path.join(ROOT, base + ".java")):
            if os.path.isfile(c) and re.search(r"\b" + re.escape(member) + r"\s*\(", open(c, errors="ignore").read()):
                return True
    return False


def check(path, obf, methods, names, deci, modes):
    text = open(path, encoding="utf-8").read()
    bad = []
    # obfuscated classes
    for m in re.finditer(r"\b((?:deci|net/decimation/mod)/[\w/]+(?:\$\w+)*)", text):
        ref = m.group(1).rstrip("/")
        if ref not in obf and not any(o.startswith(ref + "/") for o in obf):
            bad.append("class " + ref)
    # readable name + (obf): names.tsv agrees
    for m in re.finditer(r"\b([A-Z]\w+(?:\$\w+)*) \((deci/[\w/$]+)\)", text):
        want, ref = m.group(1).split("$")[-1], m.group(2)
        got = names.get(ref, "")
        if got.split("$")[-1] != want:
            bad.append("name %s (%s) is %r in names.tsv" % (want, ref, got))
    # Deci accessors (a trailing * means a family: Deci.supplyDrop*)
    for m in re.finditer(r"\bDeci\.(\w+)(\*)?", text):
        name = m.group(1)
        if m.group(2):
            if not any(d.startswith(name) for d in deci):
                bad.append("Deci." + name + "*")
        elif name not in deci and name not in ("java",):
            bad.append("Deci." + name)
    # Class.member on readable Decimation classes
    for m in re.finditer(r"\b([A-Z][A-Za-z0-9]+)\.([a-z]\w+)\b", text):
        cls, meth = m.group(1), m.group(2)
        if cls in methods and cls != "Deci" and meth not in methods[cls] and not meth.isupper():
            bad.append("method %s.%s" % (cls, meth))
    # repo paths
    for m in re.finditer(r"(?<![\w/])`?((?:docs|tools|deobf|dev|fixes|worldgen|\.claude)/[\w./$*-]+)", text):
        p = m.group(1)
        if p.endswith("/") or "*" in p or p.startswith("dev/run"):
            continue
        if not path_exists(p):
            bad.append("path " + p)
    # dev test modes
    for m in re.finditer(r"devtest\.py(?: --\w+(?:=\S+)?)* (\w+)", text):
        if m.group(1) not in modes and not m.group(1).isupper():  # MODE is a placeholder
            bad.append("dev test mode " + m.group(1))
    return sorted(set(bad))


def main():
    files = sys.argv[1:] or sorted(glob.glob(os.path.join(ROOT, ".claude", "skills", "decimation-code-*", "SKILL.md"))
                                   + glob.glob(os.path.join(ROOT, "docs", "decimation_code", "*.md")))
    obf, methods, names, deci, modes = load()
    total = 0
    for f in files:
        bad = check(f, obf, methods, names, deci, modes)
        total += len(bad)
        print("%s: %s" % (os.path.relpath(f, ROOT), "ok" if not bad else "%d unresolved" % len(bad)))
        for b in bad:
            print("    " + b)
    return 1 if total else 0


if __name__ == "__main__":
    sys.exit(main())

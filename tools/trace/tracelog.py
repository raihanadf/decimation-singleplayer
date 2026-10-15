#!/usr/bin/env python3
"""Names a call logger trace (docs/call_trace.md): every Decimation method gets its readable name from
deobf/decimation.srg (class names for all classes, method names where a naming table has one).

    python3 tools/trace/tracelog.py dev/run/client/trace/trace_<time>.txt [--top N] [--events N]

Writes trace_<time>.named.txt beside it ("deci/aE/a$B$a.a(...) = NetworkMessages$PacketGunFireEffects$...handle")
and prints the most called methods and the first events.
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SRG = os.path.join(ROOT, "deobf", "decimation.srg")


def load():
    classes, methods = {}, {}
    if not os.path.isfile(SRG):
        return classes, methods
    for line in open(SRG, encoding="utf-8"):
        p = line.split()
        if p[0] == "CL:":
            classes[p[1]] = p[2]
        elif p[0] == "MD:":
            owner, name = p[1].rsplit("/", 1)
            methods[(owner, name, p[2])] = p[3].rsplit("/", 1)[1]
    return classes, methods


def readable(cls, meth, desc, classes, methods):
    rc = classes.get(cls)
    rm = methods.get((cls, meth, desc))
    if rc is None and rm is None:
        return None
    short = (rc or cls).rsplit("/", 1)[-1]
    return "%s.%s" % (short, rm or meth)


def main():
    args = sys.argv[1:]
    if not args:
        print(__doc__)
        return 2
    path = args[0]
    top = int(args[args.index("--top") + 1]) if "--top" in args else 15
    first = int(args[args.index("--events") + 1]) if "--events" in args else 25
    classes, methods = load()
    out_lines, events, counts, section = [], [], [], None
    for raw in open(path, encoding="utf-8"):
        line = raw.rstrip("\n")
        if line.startswith("## events"):
            section = "events"
        elif line.startswith("## calls"):
            section = "calls"
        m = None if line.startswith("#") else re.search(r"(\S+?)\.([^.(\s/]+)(\([^\s]*)", line)
        if m:
            cls, meth, desc = m.group(1), m.group(2), m.group(3)
            name = readable(cls, meth, desc, classes, methods)
            if name:
                line = line + "   = " + name
        out_lines.append(line)
        if m and section == "events":
            events.append(line)
        elif m and section == "calls":
            counts.append(line)
    named = re.sub(r"\.txt$", "", path) + ".named.txt"
    with open(named, "w", encoding="utf-8") as f:
        f.write("\n".join(out_lines) + "\n")
    print("named trace: %s (%d events, %d methods called)" % (os.path.relpath(named, ROOT), len(events), len(counts)))
    print("most called:")
    for l in counts[:top]:
        print("  " + l.strip())
    print("first events:")
    for l in events[:first]:
        print("  " + l.strip())
    return 0


if __name__ == "__main__":
    sys.exit(main())

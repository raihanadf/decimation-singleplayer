"""Lists where the website's subjects are in a generated world, nearest to spawn first (docs/website.md section 6).

    python3 tools/site/subjects.py dev/run/server/logs/latest.log [N]

Reads our placement log lines (rail stations, bridges, wasteland gates, airports, highways, camps, city refuges,
military bases) and prints the N nearest of each kind (default 3) with their distance from 0,0. A rail station
stands in every city: the nearest one is where the city and hero shots go.
"""
import math
import re
import sys

PATTERNS = {
    "rail": re.compile(r"rail station at (-?\d+),(-?\d+),(-?\d+)"),
    "bridge": re.compile(r"lc bridge.* at (-?\d+),(-?\d+),(-?\d+)"),
    "gate": re.compile(r"lc (?:edge )?gate .* at (-?\d+),(-?\d+),(-?\d+)"),
    "airport": re.compile(r"lc airport at (-?\d+),(-?\d+)"),
    "highway": re.compile(r"highway .* at (-?\d+),(-?\d+),(-?\d+)"),
    "camp": re.compile(r"camp site \S+ planned at (-?\d+),(-?\d+)"),
    "base": re.compile(r"military base \S+ size \d planned at (-?\d+),(-?\d+)"),
    "refuge": re.compile(r"city refuge \S+ planned at (-?\d+),(-?\d+)"),
}


def main():
    log, n = sys.argv[1], int(sys.argv[2]) if len(sys.argv) > 2 else 3
    found = {k: [] for k in PATTERNS}
    for line in open(log, errors="replace"):
        for kind, pat in PATTERNS.items():
            m = pat.search(line)
            if not m:
                continue
            g = [int(v) for v in m.groups()]
            found[kind].append((g[0], None, g[1]) if len(g) == 2 else (g[0], g[1], g[2]))
    for kind, spots in found.items():
        spots = sorted(set(spots), key=lambda s: math.hypot(s[0], s[2]))[:n]
        print("%-8s %s" % (kind, "  ".join("%d,%s,%d (%d)" % (x, "?" if y is None else y, z, math.hypot(x, z)) for x, y, z in spots) or "none in this log"))


if __name__ == "__main__":
    main()

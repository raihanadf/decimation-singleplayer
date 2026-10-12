#!/usr/bin/env python3
"""Variety and balance of the building dressing (city/BuildingDress), from a server log.

    python3 tools/servertest.py 1 type=decimation dress     # -Pdress logs one line per building
    python3 tools/props/dressbalance.py [build/server.log]

Reads the "dressed building NAME kind K at X,Z spots N: Prop=count ..." lines and prints, per
building kind:
  - buildings, props per 100 floor spots (density) and its spread between buildings;
  - distinct props used by the kind and the top props with their share of all its props
    (a prop over 25 % reads as "everywhere");
  - similarity: mean cosine of the prop count vectors of two buildings of the same kind
    (1.0 = every building has the same mix; the user's "mostly similar" complaint);
  - "same set": share of building pairs whose sets of furniture props are identical.
Door props (EXIT light, CCTV) are left out of shares and similarity: every building has them by
rule. Buildings of the wasteland district (the city's military area, docs/wasteland.md) are their own
rows, marked (w). Whole run summary at the end (the numbers to compare before / after a change).
"""
import itertools
import math
import re
import sys
from collections import Counter, defaultdict

KINDS = ["home", "office", "shop", "food", "medical", "police", "industry", "military", "lab", "civic"]
DOOR = {"BlockExitLight", "BlockCCTV"}
LINE = re.compile(r"dressed building (\S+) kind (\d+) at (-?\d+),(-?\d+) spots (\d+)( wasteland)?:(.*)")


def load(path):
    out, seen = [], set()
    for line in open(path, errors="ignore"):
        m = LINE.search(line)
        if not m:
            continue
        key = (m.group(3), m.group(4), m.group(1))
        if key in seen:
            continue
        seen.add(key)
        props = Counter()
        for part in m.group(7).split():
            k, v = part.rsplit("=", 1)
            if k not in DOOR:
                props[k] += int(v)
        out.append({"name": m.group(1), "kind": int(m.group(2)), "spots": int(m.group(5)), "props": props,
                    "waste": bool(m.group(6))})
    return out


def cosine(a, b):
    keys = set(a) | set(b)
    dot = sum(a[k] * b[k] for k in keys)
    na = math.sqrt(sum(v * v for v in a.values()))
    nb = math.sqrt(sum(v * v for v in b.values()))
    return dot / (na * nb) if na and nb else 0.0


def main():
    path = sys.argv[1] if len(sys.argv) > 1 else "build/server.log"
    rows = load(path)
    if not rows:
        print("no 'dressed building' lines in", path, "(run servertest with dress)")
        return
    by = defaultdict(list)
    for r in rows:
        by[(r["kind"], r["waste"])].append(r)
    all_sim, all_same, all_pairs, all_props = [], 0, 0, Counter()
    print("kind         bldg  per100  spread  distinct  sim   same  top props (share)")
    for k in sorted(by):
        rs = by[k]
        tot = Counter()
        dens = []
        for r in rs:
            tot.update(r["props"])
            dens.append(100.0 * sum(r["props"].values()) / max(1, r["spots"]))
        all_props.update(tot)
        n = sum(tot.values()) or 1
        top = ", ".join("%s %d%%" % (p.replace("Block", ""), round(100.0 * c / n)) for p, c in tot.most_common(4))
        sims, same, pairs = [], 0, 0
        for a, b in itertools.combinations(rs, 2):
            sims.append(cosine(a["props"], b["props"]))
            pairs += 1
            same += set(a["props"]) == set(b["props"])
        all_sim += sims
        all_same += same
        all_pairs += pairs
        mean = sum(dens) / len(dens)
        sd = math.sqrt(sum((d - mean) ** 2 for d in dens) / len(dens))
        label = (KINDS[k[0]] if k[0] < len(KINDS) else str(k[0])) + (" (w)" if k[1] else "")
        print("%-12s %4d  %6.1f  %6.1f  %8d  %4s  %4s  %s" % (
            label, len(rs), mean, sd, len(tot),
            "%.2f" % (sum(sims) / len(sims)) if sims else "-",
            "%d%%" % round(100.0 * same / pairs) if pairs else "-", top))
    n = sum(all_props.values()) or 1
    print()
    print("buildings %d, props %d, distinct %d, mean similarity %.2f, identical sets %d%%" % (
        len(rows), n, len(all_props), sum(all_sim) / max(1, len(all_sim)),
        round(100.0 * all_same / max(1, all_pairs))))
    print("most used:", ", ".join("%s %d%%" % (p.replace("Block", ""), round(100.0 * c / n))
                                  for p, c in all_props.most_common(8)))


if __name__ == "__main__":
    main()

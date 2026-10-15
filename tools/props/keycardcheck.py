#!/usr/bin/env python3
"""Keycard screens that cannot open anything (docs/decimation_code/blocks.md).

    python3 tools/props/keycardcheck.py DUMP.tsv

DUMP.tsv: a block dump in the "x y z name meta" format (tools/props/worlddump.py WORLD OUT.tsv, or a dev test dump such
as run/client/devtest/milbase_<size>.tsv). Decimation's KeycardReaderBlock (deci/U/d) unlocks the first locked door
tile it finds at offsets -3 .. +2 on each axis (its loop runs -3 <= i < 3), and only with a keycard of its own level.
Every screen is reported with the locked doors near it; a screen with no locked door inside that reach is a finding.
Exit code 1 when there is any.
"""
import sys

READER_MIN, READER_MAX = -3, 2     # KeycardReaderBlock.onBlockActivated: for (i = -3; i < 3; i++)


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        return 2
    cells = {}
    for line in open(sys.argv[1]):
        p = line.split()
        if len(p) < 4 or not p[0].lstrip("-").isdigit():
            continue
        cells[(int(p[0]), int(p[1]), int(p[2]))] = p[3]
    screens = sorted(k for k, v in cells.items() if "KeycardScreen" in v)
    doors = [k for k, v in cells.items() if v.endswith("_Locked") and "Door" in v]
    bad = 0
    for s in screens:
        reach = [d for d in doors if all(READER_MIN <= d[i] - s[i] <= READER_MAX for i in range(3))]
        near = [d for d in doors if all(abs(d[i] - s[i]) <= 4 for i in range(3))]
        if reach:
            continue
        bad += 1
        print("NO DOOR IN REACH %s at %d %d %d; locked doors within 4: %s" % (
            cells[s].split(":")[-1], s[0], s[1], s[2],
            ", ".join("%+d %+d %+d" % (d[0] - s[0], d[1] - s[1], d[2] - s[2]) for d in near) or "none"))
    print("keycard screens %d, locked door cells %d, screens without a door in reach %d" % (len(screens), len(doors), bad))
    return 1 if bad else 0


if __name__ == "__main__":
    sys.exit(main())

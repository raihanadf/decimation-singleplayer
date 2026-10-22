"""The edit step's reshapes: change a part's shape kind, split it, turn it into an octagon."""
import os
import sys
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.dirname(HERE))
import edit  # noqa: E402
import fit  # noqa: E402


def box_part():
    a, b = (0, -4, -0.5), (4, -3, 0.5)
    return fit.new_part("p", "ammoModel", (40, 41, 42), {k: [b[i] if k[i] else a[i] for i in range(3)]
                                                         for k in fit.CORNERS}, "Magazine")


def rounded(parts):
    return [tuple(round(v, 6) for v in ab) for ab in fit.bounds(parts)]


class EditTest(unittest.TestCase):
    def test_taper_bevels_the_top_and_keeps_the_box(self):
        p = edit.reshape(box_part(), "taper")[0]
        self.assertEqual(fit.kind(p), "taper")
        self.assertEqual(rounded([p]), rounded([box_part()]))
        top = [p.c[k] for k in fit.CORNERS if k[1] == 0]
        self.assertLess(max(q[2] for q in top) - min(q[2] for q in top), 1.0)
        self.assertEqual((p.group, p.label, tuple(p.colour)), ("ammoModel", "Magazine", (40, 41, 42)))

    def test_cuboid_restores_a_plain_box(self):
        p = edit.reshape(edit.reshape(box_part(), "taper")[0], "cuboid")[0]
        self.assertEqual(fit.kind(p), "cuboid")
        self.assertEqual(rounded([p]), rounded([box_part()]))

    def test_wedge(self):
        self.assertEqual(fit.kind(edit.reshape(box_part(), "wedge")[0]), "wedge")

    def test_skew_leans_the_bottom_back(self):
        p = edit.reshape(box_part(), "skew")[0]
        self.assertEqual(fit.kind(p), "skew")
        bottom = [p.c[k] for k in fit.CORNERS if k[1] == 1]
        self.assertLess(min(q[0] for q in bottom), 0.0)

    def test_octagon(self):
        parts = edit.reshape(box_part(), "octagon")
        self.assertEqual(len(parts), 3)
        self.assertEqual(rounded(parts), rounded([box_part()]))

    def test_split_in_two_along_the_long_side(self):
        parts = edit.reshape(box_part(), "split")
        self.assertEqual(len(parts), 2)
        self.assertEqual(rounded(parts), rounded([box_part()]))
        self.assertEqual(sorted(round(fit.bounds([p])[0][1] - fit.bounds([p])[0][0], 6) for p in parts), [2.0, 2.0])

    def test_unknown_kind(self):
        with self.assertRaises(ValueError):
            edit.reshape(box_part(), "sphere")


if __name__ == "__main__":
    unittest.main()

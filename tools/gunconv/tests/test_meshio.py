"""Readers: OBJ (+ MTL), GLB, glTF with a data URI buffer, skinning in the bind pose, Blockbench .bbmodel."""
import json
import math
import os
import sys
import tempfile
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.dirname(HERE))
import meshio  # noqa: E402
import synth  # noqa: E402


def bounds(mesh):
    return [(min(v[i] for v in mesh.verts), max(v[i] for v in mesh.verts)) for i in range(3)]


class ObjTest(unittest.TestCase):
    def test_objects_materials_quads_and_negative_indices(self):
        d = tempfile.mkdtemp()
        with open(os.path.join(d, "t.mtl"), "w") as f:
            f.write("newmtl Steel\nKd 0.5 0.5 0.5\nnewmtl Wood\nKd 0.4 0.2 0.1\n")
        with open(os.path.join(d, "t.obj"), "w") as f:
            f.write("mtllib t.mtl\no Body\nv 0 0 0\nv 1 0 0\nv 1 1 0\nv 0 1 0\nusemtl Steel\nf 1/1/1 2/2/1 3/3/1 4/4/1\n"
                    "o Mag\nv 0 0 1\nv 1 0 1\nv 0 1 1\nusemtl Wood\nf -3 -2 -1\n")
        m = meshio.load(os.path.join(d, "t.obj"))
        by = {x.name: x for x in m.meshes}
        self.assertEqual(sorted(by), ["Body", "Mag"])
        self.assertEqual(len(by["Body"].faces), 2)          # the quad as two triangles
        self.assertEqual(len(by["Mag"].faces), 1)
        self.assertEqual(by["Body"].material, "Steel")
        self.assertEqual(by["Body"].colour, (128, 128, 128))
        self.assertEqual(by["Mag"].colour, (102, 51, 26))
        self.assertEqual(m.triangle_count(), 3)

    def test_synth_gun_round_trip(self):
        text, mtl = synth.obj_text(synth.synth_gun())
        d = tempfile.mkdtemp()
        with open(os.path.join(d, "test.mtl"), "w") as f:
            f.write(mtl)
        with open(os.path.join(d, "gun.obj"), "w") as f:
            f.write(text)
        m = meshio.load(os.path.join(d, "gun.obj"))
        self.assertEqual([x.name for x in m.meshes], [p[0] for p in synth.synth_gun()])
        self.assertEqual(m.triangle_count(), sum(len(p[1][1]) for p in synth.synth_gun()))


class GltfTest(unittest.TestCase):
    def unit_box_glb(self):
        g = synth.Glb()
        mat = g.material("Grey", (188, 188, 188))
        parent = g.node("Parent", t=[0, 5, 0], children=[1])
        g.node("Box", mesh=g.mesh("Box", synth.box(0, 0, 0, 1, 1, 1), mat), t=[10, 0, 0], s=[2, 2, 2], root=False)
        return g, parent

    def test_glb_node_transforms_and_colour(self):
        g, _ = self.unit_box_glb()
        m = meshio.read_bytes(g.bytes(), "box.glb")
        self.assertEqual(len(m.meshes), 1)
        b = bounds(m.meshes[0])
        for (lo, hi), (elo, ehi) in zip(b, [(10, 12), (5, 7), (0, 2)]):
            self.assertAlmostEqual(lo, elo, places=5)
            self.assertAlmostEqual(hi, ehi, places=5)
        self.assertEqual(m.meshes[0].name, "Box")
        self.assertEqual(m.meshes[0].colour, (188, 188, 188))      # linear factor back to sRGB
        self.assertEqual(m.fmt, "glb")

    def test_gltf_with_data_uri(self):
        g, _ = self.unit_box_glb()
        m = meshio.read_bytes(g.gltf_text().encode(), "box.gltf")
        self.assertEqual(m.triangle_count(), 12)
        self.assertAlmostEqual(bounds(m.meshes[0])[0][1], 12, places=5)

    def test_rotation_quaternion(self):
        g = synth.Glb()
        s = math.sqrt(0.5)
        g.node("R", mesh=g.mesh("R", synth.box(0, 0, 0, 4, 1, 1)), r=[0, s, 0, s])   # 90 degrees about +y
        b = bounds(meshio.read_bytes(g.bytes(), "r.glb").meshes[0])
        self.assertAlmostEqual(b[2][1] - b[2][0], 4, places=5)                        # the long side now along z
        self.assertAlmostEqual(b[0][1] - b[0][0], 1, places=5)

    def test_skin_bind_pose_and_joint_labels(self):
        g = synth.Glb()
        verts, faces = synth.merge(synth.box(0, 0, 0, 1, 1, 1), synth.box(3, 0, 0, 4, 1, 1))
        joints = [(0, 0, 0, 0)] * 8 + [(1, 0, 0, 0)] * 8
        weights = [(1.0, 0, 0, 0)] * 8 + [(1.0, 0, 0, 0)] * 8
        mesh = g.mesh("Body", (verts, faces), joints=joints, weights=weights)
        root = g.node("Root", t=[0, 0, 0], root=False)
        mag = g.node("Magazine", t=[0, 3, 0], root=False)
        ident = [[1 if r == c else 0 for c in range(4)] for r in range(4)]
        skin = g.skin([root, mag], [ident, ident])           # Magazine is 3 above its bind place
        arm = g.node("Armature", children=[root, mag])
        g.node("Body", mesh=mesh, skin=skin, s=[100, 100, 100])   # a skinned mesh node's own transform is ignored
        m = meshio.read_bytes(g.bytes(), "skin.glb")
        mesh = m.meshes[0]
        ys = sorted(set(round(v[1], 5) for v in mesh.verts))
        self.assertEqual(ys, [0.0, 1.0, 3.0, 4.0])
        self.assertLess(max(v[0] for v in mesh.verts), 4.0001)   # the node scale 100 was not applied
        labels = {mesh.vlabels[i] for i, v in enumerate(mesh.verts) if v[0] > 2}
        self.assertEqual(labels, {"Magazine"})
        self.assertIsNotNone(arm)

    def test_draco_is_refused_with_a_clear_message(self):
        g = synth.Glb()
        g.node("B", mesh=g.mesh("B", synth.box(0, 0, 0, 1, 1, 1)))
        g.j["extensionsUsed"] = ["KHR_draco_mesh_compression"]
        g.j["extensionsRequired"] = ["KHR_draco_mesh_compression"]
        with self.assertRaises(meshio.MeshError) as e:
            meshio.read_bytes(g.bytes(), "d.glb")
        self.assertIn("Draco", str(e.exception))

    def test_unknown_format(self):
        with self.assertRaises(meshio.MeshError):
            meshio.read_bytes(b"hello", "x.fbx")


class BbmodelTest(unittest.TestCase):
    def test_cubes_rotation_and_group_names(self):
        doc = {"meta": {"format_version": "4.5"}, "resolution": {"width": 16, "height": 16},
               "elements": [
                   {"name": "body", "type": "cube", "uuid": "a", "from": [0, 0, 0], "to": [16, 4, 2], "origin": [0, 0, 0]},
                   {"name": "pin", "type": "cube", "uuid": "b", "from": [0, 0, 0], "to": [2, 2, 8], "origin": [0, 0, 0],
                    "rotation": [0, 90, 0]}],
               "outliner": [{"name": "receiver", "uuid": "g", "children": ["a"]}, "b"]}
        m = meshio.read_bytes(json.dumps(doc).encode(), "t.bbmodel")
        by = {x.name: x for x in m.meshes}
        self.assertEqual(sorted(by), ["pin", "receiver/body"])
        self.assertEqual(m.triangle_count(), 24)
        b = bounds(by["pin"])
        self.assertAlmostEqual(b[0][1] - b[0][0], 8, places=4)     # turned 90 degrees: 8 long in x now
        self.assertAlmostEqual(b[2][1] - b[2][0], 2, places=4)


if __name__ == "__main__":
    unittest.main()

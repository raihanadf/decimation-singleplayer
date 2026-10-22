// The gun converter's private page (docs/gunconv.md). Talks to tools/gunconv/server.py on 127.0.0.1.
import * as THREE from "three";
import { OrbitControls } from "three/addons/controls/OrbitControls.js";
import { GLTFLoader } from "three/addons/loaders/GLTFLoader.js";
import { OBJLoader } from "three/addons/loaders/OBJLoader.js";
import { MTLLoader } from "three/addons/loaders/MTLLoader.js";

const $ = (id) => document.getElementById(id);

// Part corners come in fit.CORNERS order: index = ix * 4 + iy * 2 + iz. A box's faces, wound outward, over the corner
// order of tests/synth.py HEXA.
const C = (ix, iy, iz) => ix * 4 + iy * 2 + iz;
const HEXA = [C(0, 0, 0), C(1, 0, 0), C(1, 1, 0), C(0, 1, 0), C(0, 0, 1), C(1, 0, 1), C(1, 1, 1), C(0, 1, 1)];
const HEXA_FACES = [[0, 2, 1], [0, 3, 2], [4, 5, 6], [4, 6, 7], [0, 1, 5], [0, 5, 4], [3, 7, 6], [3, 6, 2],
  [0, 4, 7], [0, 7, 3], [1, 2, 6], [1, 6, 5]];
const EDGES = [];
for (let a = 0; a < 8; a++) for (let b = a + 1; b < 8; b++) {
  const d = a ^ b;
  if (d === 1 || d === 2 || d === 4) EDGES.push([a, b]);
}
// Decimation's model space is x forward, y down, z left; three.js here is x forward, y up, z right (a half turn about
// x, so nothing is mirrored).
const toThree = (p) => [p[0], -p[1], -p[2]];
const DECI_TO_THREE = new THREE.Matrix4().makeScale(1, -1, -1);
// the (ix, iy, iz) index of each corner in study.Part.verts() order (the like gun outline)
const STUDY_ORDER = [C(1, 0, 0), C(1, 1, 0), C(0, 1, 0), C(0, 0, 1), C(1, 0, 1), C(1, 1, 1), C(0, 1, 1), C(0, 0, 0)];

const state = { samples: [], path: null, conv: null, parts: [], pieces: [], sel: -1, meshes: [], guns: {},
  sourceFile: null, texture: null };
window.gunconv = { state };

// ------------------------------------------------------------------ views

class View {
  constructor(el) {
    this.el = el;
    this.renderer = new THREE.WebGLRenderer({ antialias: true, preserveDrawingBuffer: true });
    this.renderer.setPixelRatio(Math.min(2, window.devicePixelRatio || 1));
    this.renderer.outputColorSpace = THREE.SRGBColorSpace;
    el.appendChild(this.renderer.domElement);
    this.scene = new THREE.Scene();
    this.camera = new THREE.PerspectiveCamera(20, 1, 0.01, 10000);
    this.controls = new OrbitControls(this.camera, this.renderer.domElement);
    this.root = new THREE.Group();
    this.scene.add(this.root);
    this.scene.add(new THREE.HemisphereLight(0xffffff, 0x9a8f80, 2.4));
    const sun = new THREE.DirectionalLight(0xffffff, 2.2);
    sun.position.set(0.5, 1, 0.8);
    this.scene.add(sun);
    const back = new THREE.DirectionalLight(0xffffff, 0.8);
    back.position.set(-0.6, -0.3, -1);
    this.scene.add(back);
    this.controls.addEventListener("change", () => {
      this.render();
      if (this.onchange) this.onchange(this);
    });
    new ResizeObserver(() => this.resize()).observe(el);
    this.resize();
  }
  resize() {
    const w = Math.max(10, this.el.clientWidth), h = Math.max(10, this.el.clientHeight);
    this.renderer.setSize(w, h);
    this.camera.aspect = w / h;
    this.camera.updateProjectionMatrix();
    this.render();
  }
  render() {
    this.renderer.setClearColor(new THREE.Color(getComputedStyle(document.documentElement).getPropertyValue("--canvas").trim() || "#E7E3D8"));
    this.renderer.render(this.scene, this.camera);
  }
  clear() {
    for (const c of [...this.root.children]) {
      this.root.remove(c);
      c.traverse((o) => { if (o.geometry) o.geometry.dispose(); });
    }
    this.root.matrix.identity();
    this.root.matrixAutoUpdate = true;
    this.root.position.set(0, 0, 0);
    this.root.rotation.set(0, 0, 0);
    this.root.scale.set(1, 1, 1);
  }
  box() {
    this.root.updateMatrixWorld(true);
    return new THREE.Box3().setFromObject(this.root);
  }
  frame(kind, box) {
    box = box || this.box();
    if (box.isEmpty()) return;
    const c = box.getCenter(new THREE.Vector3());
    const dirs = { side: [0, 0, 1], top: [0, 1, 0.0001], three: [1, 0.75, 1.3], rear: [-1, 0.18, 0.05] };
    const v = new THREE.Vector3(...(dirs[kind] || dirs.three)).normalize();
    const up = kind === "top" ? new THREE.Vector3(0, 0, -1) : new THREE.Vector3(0, 1, 0);
    // fit the box's corners as the camera will see them: extents across its right and up directions
    const right = new THREE.Vector3().crossVectors(up, v).normalize();
    const upv = new THREE.Vector3().crossVectors(v, right).normalize();
    let w = 0, h = 0, depth = 0;
    for (const x of [box.min.x, box.max.x]) for (const y of [box.min.y, box.max.y]) for (const z of [box.min.z, box.max.z]) {
      const q = new THREE.Vector3(x, y, z).sub(c);
      w = Math.max(w, Math.abs(q.dot(right)));
      h = Math.max(h, Math.abs(q.dot(upv)));
      depth = Math.max(depth, Math.abs(q.dot(v)));
    }
    const t = Math.tan(THREE.MathUtils.degToRad(this.camera.fov / 2));
    const d = Math.max(w / this.camera.aspect, h) / t * 1.12 + depth;
    this.camera.up.copy(up);
    this.camera.position.copy(c).addScaledVector(v, d);
    this.camera.near = Math.max(0.001, d / 200);
    this.camera.far = d * 20;
    this.camera.updateProjectionMatrix();
    this.controls.target.copy(c);
    this.camera.lookAt(c);
    this.controls.update();
    this.render();
  }
}

const source = new View($("view-source"));
const conv = new View($("view-conv"));
let syncing = false;
function sync(from, to) {
  if (syncing || !state.conv) return;
  syncing = true;
  to.camera.position.copy(from.camera.position);
  to.camera.up.copy(from.camera.up);
  to.camera.near = from.camera.near;
  to.camera.far = from.camera.far;
  to.camera.updateProjectionMatrix();
  to.controls.target.copy(from.controls.target);
  to.camera.lookAt(to.controls.target);
  to.controls.update();
  to.render();
  syncing = false;
}
source.onchange = (v) => sync(v, conv);
conv.onchange = (v) => sync(v, source);

function frameBoth(kind) {
  if (state.conv) {
    const box = conv.box();
    conv.frame(kind, box);
    source.frame(kind, box);
  } else {
    source.frame(kind);
  }
}

// ------------------------------------------------------------------ server

async function api(path, body, raw) {
  const opt = body === undefined ? {} : {
    method: "POST",
    headers: { "Content-Type": raw ? "application/octet-stream" : "application/json" },
    body: raw ? body : JSON.stringify(body),
  };
  const r = await fetch(path, opt);
  const j = await r.json().catch(() => ({ error: "unreadable answer from the server (" + r.status + ")" }));
  if (!r.ok || j.error) throw new Error(j.error || "HTTP " + r.status);
  return j;
}

function status(text, error) {
  const el = $("status");
  el.textContent = text;
  el.classList.toggle("error", !!error);
}

function busy(on) {
  $("load-sample").disabled = on;
  $("convert").disabled = on || !state.path;          // a model first
  $("export").disabled = on || !state.conv;           // a conversion first
}

// ------------------------------------------------------------------ the source model

const fileUrl = (path) => "/api/file?path=" + encodeURIComponent(path);

async function meshFromServer(path) {
  const m = await api("/api/mesh?path=" + encodeURIComponent(path));
  const group = new THREE.Group();
  for (const x of m.meshes) {
    const g = new THREE.BufferGeometry();
    g.setAttribute("position", new THREE.Float32BufferAttribute(x.verts, 3));
    g.setIndex(x.faces);
    g.computeVertexNormals();
    const col = new THREE.Color().setRGB(x.colour[0] / 255, x.colour[1] / 255, x.colour[2] / 255, THREE.SRGBColorSpace);
    group.add(new THREE.Mesh(g, new THREE.MeshStandardMaterial({ color: col, roughness: 0.8, side: THREE.DoubleSide })));
  }
  return group;
}

async function loadObj(path) {
  const text = await (await fetch(fileUrl(path))).text();
  const loader = new OBJLoader();
  const lib = text.match(/^mtllib\s+(.+)$/m);
  if (lib && !path.startsWith("uploads/")) {
    const dir = path.split("/").slice(0, -1).join("/");
    const r = await fetch(fileUrl(dir + "/" + lib[1].trim()));
    if (r.ok) {
      const mtl = new MTLLoader().parse(await r.text(), "/api/file?path=" + encodeURIComponent(dir + "/"));
      mtl.preload();
      loader.setMaterials(mtl);
    }
  }
  return loader.parse(text);
}

async function loadSource(path) {
  source.clear();
  const ext = path.toLowerCase().split(".").pop();
  let obj = null;
  try {
    if (ext === "glb" || ext === "gltf") obj = (await new GLTFLoader().loadAsync(fileUrl(path))).scene;
    else if (ext === "obj") obj = await loadObj(path);
  } catch (e) {
    obj = null;
  }
  if (!obj) obj = await meshFromServer(path);
  source.root.add(obj);
  source.frame("three");
  let meshes = 0, tris = 0;
  obj.traverse((o) => {
    if (!o.isMesh || !o.geometry) return;
    meshes++;
    const g = o.geometry;
    tris += (g.index ? g.index.count : g.attributes.position.count) / 3;
  });
  $("src-stats").textContent = Math.round(tris).toLocaleString() + " triangles, " + meshes + " mesh" + (meshes === 1 ? "" : "es");
  $("pane-source").classList.add("has");
}

function applySourceMatrix() {
  if (!state.conv || !state.conv.matrix) return;
  const m = state.conv.matrix;
  const M = new THREE.Matrix4().set(...m[0], ...m[1], ...m[2], ...m[3]);
  source.root.matrixAutoUpdate = false;
  source.root.matrix.copy(DECI_TO_THREE).multiply(M);
  source.root.matrixWorldNeedsUpdate = true;
  source.render();
}

// ------------------------------------------------------------------ the converted parts

// What each part does, in plain words (the internal group names are Decimation's: gunModel, ammoModel, ...), and its
// colour when "Colour by what each part does" is on.
const ROLES = { gunModel: "Body", ammoModel: "Magazine", slideModel: "Bolt or slide", defaultScopeModel: "Iron sights" };
const GROUP_COLOURS = { gunModel: 0x77726a, ammoModel: 0xc0602f, slideModel: 0x3f72c0, defaultScopeModel: 0x4f9a55 };
const TYPES = { rifle: "Rifle", smg: "SMG", mg: "Machine gun", shotgun: "Shotgun", pistol: "Pistol", revolver: "Revolver",
  rocket: "Rocket launcher", crossbow: "Crossbow" };

function bytes(b64) {
  const bin = atob(b64);
  const out = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i);
  return out.buffer;
}

function texture(src) {
  if (state.texture && state.texture.userData.src === src) return state.texture;
  const t = new THREE.TextureLoader().load(src, () => conv.render());
  t.userData.src = src;
  t.magFilter = THREE.NearestFilter;      // Minecraft's: the texels stay square
  t.minFilter = THREE.NearestFilter;
  t.generateMipmaps = false;
  t.colorSpace = THREE.SRGBColorSpace;
  t.flipY = false;                        // v runs down the image, as Minecraft binds it
  state.texture = t;
  return t;
}

// The converted mesh (the server's preview: positions, normals, texture coordinates and each triangle's piece), one
// three.js mesh a piece so a click picks it; Decimation's y down and z left turned into three.js's.
function drawParts() {
  conv.clear();
  state.meshes = [];
  const m = state.conv && state.conv.mesh;
  if (m) {
    const pos = new Float32Array(bytes(m.positions)), nor = new Float32Array(bytes(m.normals));
    const uv = new Float32Array(bytes(m.uvs)), owner = new Uint16Array(bytes(m.piece));
    const by = new Map();
    for (let t = 0; t < owner.length; t++) {
      if (!by.has(owner[t])) by.set(owner[t], []);
      by.get(owner[t]).push(t);
    }
    const map = texture(m.texture);
    for (const [pi, tris] of by) {
      const P = new Float32Array(tris.length * 9), N = new Float32Array(tris.length * 9), U = new Float32Array(tris.length * 6);
      tris.forEach((t, j) => {
        for (let k = 0; k < 3; k++) {
          const v = t * 3 + k, o = j * 3 + k;
          P[o * 3] = pos[v * 3]; P[o * 3 + 1] = -pos[v * 3 + 1]; P[o * 3 + 2] = -pos[v * 3 + 2];
          N[o * 3] = nor[v * 3]; N[o * 3 + 1] = -nor[v * 3 + 1]; N[o * 3 + 2] = -nor[v * 3 + 2];
          U[o * 2] = uv[v * 2]; U[o * 2 + 1] = uv[v * 2 + 1];
        }
      });
      const g = new THREE.BufferGeometry();
      g.setAttribute("position", new THREE.BufferAttribute(P, 3));
      g.setAttribute("normal", new THREE.BufferAttribute(N, 3));
      g.setAttribute("uv", new THREE.BufferAttribute(U, 2));
      const piece = state.pieces[pi] || { group: "gunModel" };
      const mat = $("split").checked
        ? new THREE.MeshStandardMaterial({ color: GROUP_COLOURS[piece.group] || 0x888888, roughness: 0.85, metalness: 0,
          side: THREE.DoubleSide })
        : new THREE.MeshStandardMaterial({ map, roughness: 0.85, metalness: 0, side: THREE.DoubleSide });
      const mesh = new THREE.Mesh(g, mat);
      mesh.userData.index = pi;
      conv.root.add(mesh);
      state.meshes.push(mesh);
    }
  }
  drawOutline();
  drawGhost();
  conv.render();
  renderTable();
  const st = (state.conv || {}).stats || {};
  $("conv-count").textContent = st.triangles ? st.triangles.toLocaleString() + " triangles, " + st.texture.join(" x ") +
    " texture" : "";
  $("pane-conv").classList.toggle("has", !!m);
  $("parts-count").textContent = state.pieces.length ? state.pieces.length + " parts" : "";
  $("cost").textContent = costLine();
}

function edgeLines(corners, colour, opacity) {
  const pos = [];
  for (const [a, b] of EDGES) pos.push(...toThree(corners[a]), ...toThree(corners[b]));
  const g = new THREE.BufferGeometry();
  g.setAttribute("position", new THREE.Float32BufferAttribute(pos, 3));
  return new THREE.LineSegments(g, new THREE.LineBasicMaterial({ color: colour, transparent: opacity < 1,
    opacity, depthTest: opacity < 1 }));
}

function drawOutline() {
  const old = conv.root.getObjectByName("outline");
  if (old) conv.root.remove(old);
  const mesh = state.meshes.find((m) => m.userData.index === state.sel);
  if (!mesh) return;
  const accent = getComputedStyle(document.documentElement).getPropertyValue("--select").trim() || "#B4552F";
  const box = new THREE.BoxHelper(mesh, accent);
  box.material.depthTest = false;
  box.renderOrder = 10;
  box.name = "outline";
  conv.root.add(box);
}

async function drawGhost() {
  const old = conv.root.getObjectByName("ghost");
  if (old) conv.root.remove(old);
  const like = state.conv && state.conv.like;
  if (!$("ghost").checked || !like) return;
  if (!state.guns[like]) state.guns[like] = await api("/api/gun?name=" + encodeURIComponent(like));
  const group = new THREE.Group();
  group.name = "ghost";
  for (const p of state.guns[like].parts) {
    const corners = new Array(8);
    p.verts.forEach((v, i) => { corners[STUDY_ORDER[i]] = v; });
    group.add(edgeLines(corners, 0x8a8174, 0.55));
  }
  conv.root.add(group);
  conv.render();
}

function renderTable() {
  const body = $("parts").querySelector("tbody");
  body.innerHTML = "";
  state.pieces.forEach((p, i) => {
    const tr = document.createElement("tr");
    tr.dataset.index = i;
    [i, ROLES[p.group] || p.group, p.label, p.triangles.toLocaleString()].forEach((t, k) => {
      const td = document.createElement("td");
      td.textContent = t;
      if (k === 3) td.className = "num";
      tr.appendChild(td);
    });
    if (i === state.sel) tr.classList.add("on");
    tr.addEventListener("click", () => select(i));
    body.appendChild(tr);
  });
}

function select(i) {
  state.sel = i;
  const p = state.pieces[i];
  $("group").disabled = !p;
  if (p) {
    $("sel-info").textContent = "Part " + i + ", " + p.label + ": " + (ROLES[p.group] || p.group) + ", " +
      p.triangles.toLocaleString() + " triangles.";
    $("group").value = p.group;
  } else {
    $("sel-info").textContent = state.pieces.length ? "Click a part in the game view or in the list." :
      "Convert, then click a part in the game view or in the list.";
    $("group").value = "";
  }
  drawOutline();
  conv.render();
  for (const tr of $("parts").querySelectorAll("tbody tr")) tr.classList.toggle("on", +tr.dataset.index === i);
  const row = $("parts").querySelector('tbody tr[data-index="' + i + '"]');
  if (row) row.scrollIntoView({ block: "nearest" });
}

// click to pick a part (a click, not a drag of the view)
let down = null;
conv.renderer.domElement.addEventListener("pointerdown", (e) => { down = [e.clientX, e.clientY]; });
conv.renderer.domElement.addEventListener("pointerup", (e) => {
  if (!down || Math.hypot(e.clientX - down[0], e.clientY - down[1]) > 4) return;
  const r = conv.renderer.domElement.getBoundingClientRect();
  const ray = new THREE.Raycaster();
  ray.setFromCamera(new THREE.Vector2(((e.clientX - r.left) / r.width) * 2 - 1, -((e.clientY - r.top) / r.height) * 2 + 1),
    conv.camera);
  const hit = ray.intersectObjects(state.meshes, false)[0];
  select(hit ? hit.object.userData.index : -1);
});

// ------------------------------------------------------------------ edits

// A piece's group: what Decimation's animations move (the export puts each group's triangles under its placeholder)
$("group").addEventListener("change", () => {
  const p = state.pieces[state.sel];
  if (!p) return;
  p.group = $("group").value;
  drawParts();
  select(state.sel);
});

for (const id of ["split", "ghost"]) $(id).addEventListener("change", () => { drawParts(); select(state.sel); });
for (const b of document.querySelectorAll("[data-view]")) b.addEventListener("click", () => frameBoth(b.dataset.view));

// ------------------------------------------------------------------ points

function num(id) {
  const v = $(id).value.trim();
  return v === "" ? null : parseFloat(v);
}

function showPoints(p) {
  $("flame-x").value = p.flame[0];
  $("flame-y").value = p.flame[1];
  $("flame-z").value = p.flame[2];
  $("rail-top").value = p.railTop;
  $("mount").value = p.mount || "";
  $("rail-from").value = p.rail ? p.rail[0] : "";
  $("rail-to").value = p.rail ? p.rail[1] : "";
  $("grip-from").value = p.gripRail ? p.gripRail[0] : "";
  $("grip-to").value = p.gripRail ? p.gripRail[1] : "";
}

function readPoints() {
  const pair = (a, b) => (num(a) === null || num(b) === null ? null : [num(a), num(b)]);
  return {
    flame: [num("flame-x"), num("flame-y"), num("flame-z")],
    railTop: num("rail-top"),
    rail: pair("rail-from", "rail-to"),
    gripRail: pair("grip-from", "grip-to"),
    mount: $("mount").value || null,
  };
}

// ------------------------------------------------------------------ actions

function fillSelect(sel, values, keep) {
  const cur = keep ? sel.value : "";
  sel.innerHTML = "";
  for (const [value, text] of values) {
    const o = document.createElement("option");
    o.value = value;
    o.textContent = text;
    sel.appendChild(o);
  }
  if (keep) sel.value = cur;
}

function setCredit(src) {
  $("author").value = (src && src.author) || "";
  $("url").value = (src && src.url) || "";
  $("license").value = (src && src.license) || "";
}

async function init() {
  const axes = [["", "Automatic"]].concat(["+x", "-x", "+y", "-y", "+z", "-z"].map((a) => [a, "The file's " + a]));
  const art = new Image();                    // the site's art (camo, torn buttons) when its repo is on this computer
  art.onload = () => document.body.classList.add("site-art");
  art.src = "/ui/mask.webp";
  for (const s of document.querySelectorAll("select.axis")) fillSelect(s, axes);
  try {
    const r = await api("/api/samples");
    state.samples = r.samples;
    fillSelect($("category"), [["", "Automatic"]].concat(r.categories.map((c) => [c, TYPES[c] || c])));
    fillSelect($("sample"), r.samples.map((s) => [s.path, s.name + ": " + s.file]));
    if (!r.samples.length) status("No samples here yet: open your own file in step 1.");
    if (!r.jar) status("Decimation.jar was not found, so no Decimation gun can be matched: hands and stats come from the" +
      " defaults.", true);
  } catch (e) {
    status(e.message, true);
  }
}

$("load-sample").addEventListener("click", async () => {
  const path = $("sample").value;
  if (!path) return;
  const s = state.samples.find((x) => x.path === path);
  state.path = path;
  state.conv = null;
  state.parts = [];
  state.pieces = [];
  state.sourceFile = s ? s.file : path.split("/").pop();
  setCredit(s && s.source);
  conv.clear();
  drawParts();
  busy(true);
  status("Loading the sample ...");
  try {
    await loadSource(path);
    status("Loaded. Check the Model panel's credit, then press Convert.");
  } catch (e) {
    status(e.message, true);
  } finally {
    busy(false);
  }
});

$("file").addEventListener("change", () => openFile($("file").files[0]));

// a model file dropped on either view opens like "Open a file" (gltf.report, model-viewer's editor)
const views = $("views");
views.addEventListener("dragover", (e) => { e.preventDefault(); views.classList.add("drag"); });
views.addEventListener("dragleave", (e) => { if (e.target === views || !views.contains(e.relatedTarget)) views.classList.remove("drag"); });
views.addEventListener("drop", (e) => {
  e.preventDefault();
  views.classList.remove("drag");
  const f = e.dataTransfer.files[0];
  if (f && /\.(glb|gltf|obj|bbmodel)$/i.test(f.name)) openFile(f);
  else status("Drop a .glb, .gltf, .obj or .bbmodel file.", true);
});

async function openFile(f) {
  if (!f) return;
  status("Opening " + f.name + " ...");
  busy(true);
  try {
    const r = await api("/api/upload?filename=" + encodeURIComponent(f.name), await f.arrayBuffer(), true);
    state.path = r.path;
    state.conv = null;
    state.parts = [];
    state.pieces = [];
    state.sourceFile = f.name;
    setCredit(null);
    conv.clear();
    drawParts();
    await loadSource(r.path);
    status("Loaded " + f.name + ". Fill in who made it and its license in the Model panel, then press Convert.");
  } catch (e) {
    status(e.message, true);
  } finally {
    busy(false);
  }
}

// The cost line: triangles, the texture, the draw time estimated from the game's measurements (meshpack.estimate).
function costLine() {
  const st = (state.conv || {}).stats || {};
  if (!st.cost) return "";
  return (st.exact ? "" : "Cut from " + st.source.toLocaleString() + " to 30,000 triangles. ") + "Draws in about " +
    st.cost.ratio.toFixed(2) + " times the AKM's time" + (st.cost.verdict === "ok" ? " (fine up to " + st.cost.budget + ")"
    : ", over the " + st.cost.budget + " we aim for");
}

$("convert").addEventListener("click", convertNow);

async function convertNow() {
  if (!state.path) return status("Pick a model in step 1 first.", true);
  busy(true);
  status("Converting, this takes a few seconds ...");
  const req = {
    [state.path.startsWith("uploads/") ? "upload" : "sample"]: state.path,
    name: $("name").value.trim(), display: $("display").value.trim(), category: $("category").value,
    like: $("like").value, lengthMm: num("length-mm"), muzzle: $("muzzle").value, up: $("up").value,
    mirror: $("mirror").checked,
    source: { author: $("author").value.trim(), url: $("url").value.trim(), license: $("license").value.trim() },
  };
  try {
    const r = await api("/api/convert", req);
    state.conv = r;
    state.pieces = r.pieces || [];
    state.sel = -1;
    $("name").value = r.name;
    $("display").value = r.display;
    $("category").value = r.category;
    fillSelect($("like"), [["", "Automatic"]].concat(r.candidates.map((c) => [c[0], c[0] + " (" + Math.round(c[1] * 100) +
      " % alike)"])));
    if (r.like && ![...$("like").options].some((o) => o.value === r.like)) {
      fillSelect($("like"), [...[...$("like").options].map((o) => [o.value, o.textContent]), [r.like, r.like]]);
    }
    $("like").value = r.like || "";
    showPoints(r.points);
    const ul = $("warnings");
    ul.innerHTML = "";
    $("notes-count").textContent = r.warnings.length ? r.warnings.length + "" : "";
    for (const w of r.warnings.length ? r.warnings : ["Nothing to watch out for."]) {
      const li = document.createElement("li");
      li.textContent = w;
      li.className = "note";
      ul.appendChild(li);
    }
    applySourceMatrix();
    drawParts();
    select(-1);
    frameBoth("side");
    const g = r.stats.groups;
    status("Converted in " + r.seconds + " s: " + (TYPES[r.category] || r.category).toLowerCase() + ", handles like the " +
      r.like + ". " + Object.entries(g).map(([k, v]) => (ROLES[k] || k) + " " + v.toLocaleString()).join(", ") +
      " triangles. Check the parts, then Export.");
  } catch (e) {
    status(e.message, true);
  } finally {
    busy(false);
  }
}

$("export").addEventListener("click", async () => {
  if (!state.conv) return status("Convert the model in step 3 first.", true);
  busy(true);
  status("Writing the package ...");
  const { mesh, ...kept } = state.conv;     // the mesh stays on the server (the token names it)
  const req = Object.assign({}, kept, {
    pieces: state.pieces, name: $("name").value.trim() || state.conv.name, display: $("display").value.trim(),
    points: readPoints(),
    source: { file: state.sourceFile || (state.conv.source && state.conv.source.file), author: $("author").value.trim(),
      url: $("url").value.trim(), license: $("license").value.trim() },
  });
  try {
    const r = await api("/api/export", req);
    const el = (tag, text, cls) => {
      const e = document.createElement(tag);
      if (text !== undefined) e.textContent = text;
      if (cls) e.className = cls;
      return e;
    };
    const report = $("report");
    report.replaceChildren();
    const p = el("p", "Written to ");
    const code = el("code", r.dir);
    code.id = "package-dir";
    p.append(code, " (" + r.files.length + " files" + (r.triangles !== undefined ? ", " + r.triangles.toLocaleString() +
      " triangles" : "") + ").");
    const ul = el("ul");
    ul.id = "package-files";
    for (const f of r.files) ul.appendChild(el("li", f));
    const next = el("p", "To try it, start the dev test game with this folder's parent:");
    const cmd = el("pre", "python3 tools/devtest.py --live --gunpacks=" + r.dir.replace(/\/[^/]+$/, "") + " gunpack", "cmd");
    report.append(p, next, cmd, ul);
    $("export-panel").open = true;
    $("export-panel").scrollIntoView({ block: "nearest", behavior: "smooth" });
    status("Exported to " + r.dir + ".");
  } catch (e) {
    status(e.message, true);
  } finally {
    busy(false);
  }
});

init();

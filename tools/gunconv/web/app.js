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

const state = { samples: [], path: null, conv: null, parts: [], history: [], sel: -1, meshes: [], guns: {},
  sourceFile: null };
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
  for (const id of ["convert", "export", "load-sample"]) $(id).disabled = on;
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

function partColour(p, i) {
  if ($("split").checked) return new THREE.Color().setHSL(((i * 0.618034) % 1), 0.55, 0.55);
  return new THREE.Color().setRGB(p.colour[0] / 255, p.colour[1] / 255, p.colour[2] / 255, THREE.SRGBColorSpace);
}

function drawParts() {
  conv.clear();
  state.meshes = [];
  state.parts.forEach((p, i) => {
    const pos = [];
    for (const f of HEXA_FACES) for (const k of f) pos.push(...toThree(p.corners[HEXA[k]]));
    const g = new THREE.BufferGeometry();
    g.setAttribute("position", new THREE.Float32BufferAttribute(pos, 3));
    g.computeVertexNormals();
    const mesh = new THREE.Mesh(g, new THREE.MeshStandardMaterial({ color: partColour(p, i), roughness: 0.9,
      metalness: 0, flatShading: true, side: THREE.DoubleSide }));
    mesh.userData.index = i;
    conv.root.add(mesh);
    state.meshes.push(mesh);
  });
  drawOutline();
  drawGhost();
  conv.render();
  renderTable();
  $("conv-count").textContent = state.parts.length ? "(" + state.parts.length + " parts)" : "";
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
  if (state.sel < 0 || !state.parts[state.sel]) return;
  const accent = getComputedStyle(document.documentElement).getPropertyValue("--select").trim() || "#B4552F";
  const lines = edgeLines(state.parts[state.sel].corners, accent, 1);
  lines.material.depthTest = false;
  lines.renderOrder = 10;
  lines.name = "outline";
  conv.root.add(lines);
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
  state.parts.forEach((p, i) => {
    const tr = document.createElement("tr");
    tr.dataset.index = i;
    for (const t of [i, p.group, p.label, p.kind]) {
      const td = document.createElement("td");
      td.textContent = t;
      tr.appendChild(td);
    }
    if (i === state.sel) tr.classList.add("on");
    tr.addEventListener("click", () => select(i));
    body.appendChild(tr);
  });
}

function size(p) {
  const s = [0, 1, 2].map((a) => Math.max(...p.corners.map((c) => c[a])) - Math.min(...p.corners.map((c) => c[a])));
  return s.map((v) => v.toFixed(2)).join(" x ");
}

function select(i) {
  state.sel = i;
  const p = state.parts[i];
  if (p) {
    $("sel-info").textContent = "#" + i + "  " + p.label + "  (" + p.name + "), " + p.group + ", " + p.kind +
      ", " + size(p) + " units";
    $("group").value = p.group;
    $("kind").value = p.kind;
  } else {
    $("sel-info").textContent = "No part selected. Click one in the converted view or in the list.";
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

function remember() {
  state.history.push(JSON.stringify(state.parts));
  if (state.history.length > 100) state.history.shift();
}

function kindOf(p) {
  // the same rules as fit.kind (absolute corners)
  const c = p.corners;
  for (let i = 0; i < 8; i++) for (let j = i + 1; j < 8; j++) {
    if (Math.abs(c[i][0] - c[j][0]) + Math.abs(c[i][1] - c[j][1]) + Math.abs(c[i][2] - c[j][2]) < 0.02) return "wedge";
  }
  const lo = [0, 1, 2].map((a) => Math.min(...c.map((q) => q[a])));
  const hi = [0, 1, 2].map((a) => Math.max(...c.map((q) => q[a])));
  if (c.every((q) => [0, 1, 2].every((a) => Math.abs(q[a] - lo[a]) < 1e-3 || Math.abs(q[a] - hi[a]) < 1e-3))) return "cuboid";
  const bit = [4, 2, 1];
  for (let ax = 0; ax < 3; ax++) {
    const f0 = c.filter((_, k) => !(k & bit[ax])), f1 = c.filter((_, k) => k & bit[ax]);
    for (let o = 0; o < 3; o++) {
      if (o === ax) continue;
      const e0 = Math.max(...f0.map((q) => q[o])) - Math.min(...f0.map((q) => q[o]));
      const e1 = Math.max(...f1.map((q) => q[o])) - Math.min(...f1.map((q) => q[o]));
      if (Math.abs(e0 - e1) > 0.02) return "taper";
    }
  }
  return "skew";
}

function move(d) {
  const p = state.parts[state.sel];
  if (!p) return status("Select a part first.", true);
  const step = parseFloat($("step").value) || 0.05;
  remember();
  p.corners = p.corners.map((q) => [q[0] + d[0] * step, q[1] + d[1] * step, q[2] + d[2] * step].map((v) => +v.toFixed(5)));
  drawParts();
  select(state.sel);
}

for (const b of document.querySelectorAll("[data-move]")) {
  b.addEventListener("click", () => move(b.dataset.move.split(",").map(Number)));
}

$("group").addEventListener("change", () => {
  const p = state.parts[state.sel];
  if (!p) return;
  remember();
  p.group = $("group").value;
  drawParts();
  select(state.sel);
});

$("apply-kind").addEventListener("click", async () => {
  const p = state.parts[state.sel];
  if (!p) return status("Select a part first.", true);
  try {
    const r = await api("/api/reshape", { part: p, kind: $("kind").value });
    remember();
    state.parts.splice(state.sel, 1, ...r.parts);
    drawParts();
    select(state.sel);
    status("Part " + state.sel + " is now " + $("kind").value + " (" + r.parts.length + " part" + (r.parts.length > 1 ? "s" : "") + ").");
  } catch (e) {
    status(e.message, true);
  }
});

$("drop").addEventListener("click", () => {
  if (!state.parts[state.sel]) return status("Select a part first.", true);
  remember();
  state.parts.splice(state.sel, 1);
  const next = Math.min(state.sel, state.parts.length - 1);
  drawParts();
  select(next);
});

$("undo").addEventListener("click", () => {
  if (!state.history.length) return status("Nothing to undo.");
  state.parts = JSON.parse(state.history.pop());
  for (const p of state.parts) p.kind = kindOf(p);
  drawParts();
  select(Math.min(state.sel, state.parts.length - 1));
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
  const axes = [["", "auto"]].concat(["+x", "-x", "+y", "-y", "+z", "-z"].map((a) => [a, a]));
  for (const s of document.querySelectorAll("select.axis")) fillSelect(s, axes);
  try {
    const r = await api("/api/samples");
    state.samples = r.samples;
    fillSelect($("category"), [["", "auto"]].concat(r.categories.map((c) => [c, c])));
    fillSelect($("sample"), r.samples.map((s) => [s.path, s.name + ": " + s.file]));
    if (!r.samples.length) status("No samples in build/gunconv/inputs: open a file.");
    if (!r.jar) status("Decimation.jar not found: no like gun, the hands come from the style guide.", true);
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
  state.sourceFile = s ? s.file : path.split("/").pop();
  setCredit(s && s.source);
  conv.clear();
  drawParts();
  status("Loading " + path + " ...");
  try {
    await loadSource(path);
    status("Loaded " + path + ". Convert when ready.");
  } catch (e) {
    status(e.message, true);
  }
});

$("file").addEventListener("change", async () => {
  const f = $("file").files[0];
  if (!f) return;
  status("Uploading " + f.name + " ...");
  try {
    const r = await api("/api/upload?filename=" + encodeURIComponent(f.name), await f.arrayBuffer(), true);
    state.path = r.path;
    state.conv = null;
    state.parts = [];
    state.sourceFile = f.name;
    setCredit(null);
    conv.clear();
    drawParts();
    await loadSource(r.path);
    status("Loaded " + f.name + ". Fill in its author and license, then convert.");
  } catch (e) {
    status(e.message, true);
  }
});

$("convert").addEventListener("click", async () => {
  if (!state.path) return status("Load a model first.", true);
  busy(true);
  status("Converting ...");
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
    state.parts = r.parts;
    state.history = [];
    state.sel = -1;
    $("name").value = r.name;
    $("display").value = r.display;
    $("category").value = r.category;
    fillSelect($("like"), [["", "auto"]].concat(r.candidates.map((c) => [c[0], c[0] + "  " + c[1].toFixed(2)])));
    if (r.like && ![...$("like").options].some((o) => o.value === r.like)) {
      fillSelect($("like"), [...[...$("like").options].map((o) => [o.value, o.textContent]), [r.like, r.like]]);
    }
    $("like").value = r.like || "";
    showPoints(r.points);
    const ul = $("warnings");
    ul.innerHTML = "";
    const notes = r.warnings.slice();
    const st = r.stats || {};
    if (st.byGun) {
      const guns = Object.entries(st.byGun).sort((a, b) => b[1] - a[1]).map(([g, n]) => g + " " + n).join(", ");
      notes.unshift("From Decimation exemplars: " + (st.fromExemplars || 0) + " parts (" + (guns || "none") +
        "); from the fit: " + (st.fromFit || 0) + " parts.");
      const kept = Object.entries(st.exemplars || {}).filter(([, v]) => v.fit !== undefined).map(([a]) => a);
      if (kept.length) notes.push("Areas kept as fitted (no exemplar passed the overlap gate): " + kept.join(", ") + ".");
    }
    for (const w of notes.length ? notes : ["none"]) {
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
    status("Converted in " + r.seconds + " s: " + r.parts.length + " parts (" + Object.entries(g).map(([k, v]) => k + " " + v)
      .join(", ") + "), like " + r.like + ", " + r.category + ".");
  } catch (e) {
    status(e.message, true);
  } finally {
    busy(false);
  }
});

$("export").addEventListener("click", async () => {
  if (!state.conv) return status("Convert first.", true);
  busy(true);
  status("Exporting ...");
  const req = Object.assign({}, state.conv, {
    parts: state.parts, name: $("name").value.trim() || state.conv.name, display: $("display").value.trim(),
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
    p.append(code, " (" + r.parts + " parts).");
    const ul = el("ul");
    ul.id = "package-files";
    for (const f of r.files) ul.appendChild(el("li", f));
    report.append(p, ul);
    if (r.metrics.length) {
      const table = el("table");
      const head = el("tr");
      for (const h of ["Metric", "Ours", "Median", "q10 .. q90"]) head.appendChild(el("th", h));
      table.appendChild(el("thead")).appendChild(head);
      const body = table.appendChild(el("tbody"));
      for (const m of r.metrics) {
        const tr = el("tr");
        tr.append(el("td", m.metric), el("td", String(m.ours), m.inside ? "" : "out"), el("td", String(m.median)),
          el("td", m.q10 + " .. " + m.q90));
        body.appendChild(tr);
      }
      report.appendChild(table);
    }
    const outside = r.metrics.filter((m) => !m.inside).map((m) => m.metric);
    status("Exported " + r.files.length + " files." + (outside.length ? " Outside Decimation's range: " + outside.join(", ") + "." : ""));
  } catch (e) {
    status(e.message, true);
  } finally {
    busy(false);
  }
});

init();

#!/usr/bin/env python3
"""Production server run (docs/production_test.md): a plain Forge 1.7.10 server folder (made with the Forge installer,
eula.txt, server.properties with level-type decimation) with only JAR in mods/, a fresh world (the old one moved aside
as world_<NAME>_old), the given -D flags (DevPregen: -Ddeciworldgen.pregen=x,z,r, -Ddeciworldgen.pregenwalk=x,z,steps,
view); waits for "PREGEN done" / "WALK done" (whichever the flags ask for) or "Done (", sends stop, prints our PREGEN /
WALK lines and every error line.

    python3 tools/prodsrv_run.py SERVER_DIR JAR NAME [-Dkey=value ...]
"""
import os, re, shutil, signal, subprocess, sys, time

if len(sys.argv) < 4:
    sys.exit(__doc__)
SRV, jar, name, flags = sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4:]
for f in os.listdir(os.path.join(SRV, "mods")):
    if f.endswith(".jar"):
        os.remove(os.path.join(SRV, "mods", f))
shutil.copy2(jar, os.path.join(SRV, "mods", "Decimation.jar"))
if os.path.isdir(os.path.join(SRV, "world")):
    old = os.path.join(SRV, "world_%s_old" % name)
    shutil.rmtree(old, ignore_errors=True)
    os.rename(os.path.join(SRV, "world"), old)
log = open(os.path.join(SRV, "run_%s.log" % name), "w")
java = os.path.expanduser("~/.jdks/zulu-8.jdk/Contents/Home/bin/java")
p = subprocess.Popen([java, "-Xmx3G"] + flags + ["-jar", "forge-1.7.10-10.13.4.1614-1.7.10-universal.jar", "nogui"],
                     cwd=SRV, stdout=log, stderr=subprocess.STDOUT, stdin=subprocess.PIPE, start_new_session=True)
want = []
if any(f.startswith("-Ddeciworldgen.pregen=") for f in flags):
    want.append("PREGEN done")
if any(f.startswith("-Ddeciworldgen.pregenwalk=") for f in flags):
    want.append("WALK done")
want = want or ["Done ("]
deadline, ok = time.time() + 1500, False
while time.time() < deadline:
    time.sleep(2)
    s = open(log.name, errors="ignore").read()
    if "Done (" in s and all(w in s for w in want):
        ok = True
        break
    if p.poll() is not None:
        break
time.sleep(2)
try:
    p.stdin.write(b"stop\n")
    p.stdin.flush()
    p.wait(120)
except Exception:
    os.killpg(p.pid, signal.SIGTERM)
s = open(log.name, errors="ignore").read()
print("finished" if ok else "DID NOT FINISH", "| exit", p.returncode)
for l in s.splitlines():
    if re.search(r"PREGEN (parts|timing|light)|WALK |Done \(|bundled content|Exception in server tick|Encountered an unexpected", l):
        print(l[l.find("]: ") + 3 if "]: " in l else 0:][:600])
errs = [l for l in s.splitlines() if re.search(r"ERROR|Exception|Caused by", l)]
print("error lines:", len(errs))
for l in errs[:12]:
    print("  ", l[:200])

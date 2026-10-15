#!/usr/bin/env python3
"""Builds the dev call logger (docs/call_trace.md) into build/trace/:

    decitrace-agent.jar   the Java agent (marks methods as classes load; ASM from the Forge classpath)
    decitrace-boot.jar    the recorder decitrace.Trace, put on the boot class path by the agent's manifest

    python3 tools/trace/build.py

Then `./gradlew runClient -Ptrace` (dev/) or `python3 tools/devtest.py --live --trace ...` starts the game with it.
Compiled by the Java 8 JDK in ~/.jdks against Forge's asm-debug-all 5.0.3 from the Gradle cache.
"""
import glob
import os
import shutil
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SRC = os.path.join(ROOT, "tools", "trace", "src")
OUT = os.path.join(ROOT, "build", "trace")
JDK = os.path.expanduser("~/.jdks/zulu-8.jdk/Contents/Home/bin")


def asm_jar():
    hits = glob.glob(os.path.expanduser(
        "~/.gradle/caches/modules-2/files-2.1/org.ow2.asm/asm-debug-all/5.0.3/*/asm-debug-all-5.0.3.jar"))
    if not hits:
        sys.exit("asm-debug-all-5.0.3.jar not in the Gradle cache (run the dev client once)")
    return hits[0]


def run(cmd):
    r = subprocess.run(cmd, capture_output=True, text=True)
    if r.returncode != 0:
        sys.exit("failed: %s\n%s%s" % (" ".join(cmd), r.stdout, r.stderr))


def main():
    asm = asm_jar()
    classes = os.path.join(OUT, "classes")
    shutil.rmtree(OUT, ignore_errors=True)
    os.makedirs(classes)
    sources = glob.glob(os.path.join(SRC, "decitrace", "*.java")) + glob.glob(os.path.join(SRC, "decitrace", "agent", "*.java"))
    run([os.path.join(JDK, "javac"), "-source", "8", "-target", "8", "-nowarn", "-cp", asm, "-d", classes] + sources)
    boot = os.path.join(OUT, "decitrace-boot.jar")
    agent = os.path.join(OUT, "decitrace-agent.jar")
    manifest = os.path.join(OUT, "MANIFEST.MF")
    with open(manifest, "w") as f:
        f.write("Manifest-Version: 1.0\n")
        f.write("Premain-Class: decitrace.agent.TraceAgent\n")
        f.write("Boot-Class-Path: decitrace-boot.jar\n")
        # Forge's runtime class path has ASM already; this is the fallback
        f.write("Class-Path: file://%s\n" % asm.replace(" ", "%20"))
    entries = ["decitrace/Trace.class"] + [os.path.relpath(p, classes)
                                           for p in glob.glob(os.path.join(classes, "decitrace", "Trace$*.class"))]
    cmd = [os.path.join(JDK, "jar"), "cf", boot]
    for e in entries:
        cmd += ["-C", classes, e]
    run(cmd)
    run([os.path.join(JDK, "jar"), "cfm", agent, manifest, "-C", classes, "decitrace/agent"])
    print("built", os.path.relpath(agent, ROOT), "and", os.path.relpath(boot, ROOT))


if __name__ == "__main__":
    main()

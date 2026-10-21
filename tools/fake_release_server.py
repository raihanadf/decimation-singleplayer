#!/usr/bin/env python3
"""A stand in for GitHub's releases for the dev test `update` (fixes/Updater): serves
/api/releases (one release TAG with a Decimation.jar asset, its sha256 digest and size; ETag, 304 on
If-None-Match), /dl/Decimation.jar (MB megabytes starting with b"NEWJAR", sent at RATE MB/s so the
progress bar moves), and the non API pages the fallback uses: /web/releases.atom (TAG and an older
0.0.1), /web/releases/download/TAG/Decimation.jar (GET, HEAD) and its .sha256.
LIMITED: the API answers 403 like GitHub's rate limit. BADDIGEST: wrong sha256 everywhere.
FILE=path: serve that file as the release jar instead of the made up bytes (a real update before a release: a game
running the last release updates itself to the candidate jar; docs/production_test.md).
REMOTE=url: the release list points its download at that real URL (with FILE= the same file, for size and digest):
the game's own updater downloads from GitHub itself (the 406 check of 22 Oktober 2026, bug.md).
Prints one line per request.
usage: python3 tools/fake_release_server.py [PORT=8765] [TAG=9.9.9] [MB=40] [RATE=8] [BADDIGEST] [LIMITED] [FILE=path] [REMOTE=url]"""
import hashlib, http.server, json, os, sys, time

PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 8765
TAG = sys.argv[2] if len(sys.argv) > 2 else "9.9.9"
MB = int(sys.argv[3]) if len(sys.argv) > 3 else 40
RATE = float(sys.argv[4]) if len(sys.argv) > 4 else 8
BAD = "BADDIGEST" in sys.argv
LIMITED = "LIMITED" in sys.argv
ETAG = 'W/"fake-%s"' % TAG
FILE = next((a.split("=", 1)[1] for a in sys.argv[1:] if a.startswith("FILE=")), None)
REMOTE = next((a.split("=", 1)[1] for a in sys.argv[1:] if a.startswith("REMOTE=")), None)
PAYLOAD = open(FILE, "rb").read() if FILE else b"NEWJAR" + os.urandom(MB * 1024 * 1024 - 6)
HEX = "0" * 64 if BAD else hashlib.sha256(PAYLOAD).hexdigest()
DIGEST = "sha256:" + HEX
JAR = "/web/releases/download/%s/Decimation.jar" % TAG
ATOM = ("""<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
  <id>tag:github.com,2008:https://github.com/fake/fake/releases</id>
  <entry><id>tag:github.com,2008:Repository/1/%s</id><title>%s</title></entry>
  <entry><id>tag:github.com,2008:Repository/1/0.0.1</id><title>0.0.1</title></entry>
</feed>
""" % (TAG, TAG)).encode()


class H(http.server.BaseHTTPRequestHandler):
    def log_message(self, *a):
        pass

    def send(self, code, body=b"", ctype="text/plain", head=False, extra=()):
        self.send_response(code)
        self.send_header("Content-Type", ctype)
        self.send_header("Content-Length", str(len(body)))
        for k, v in extra:
            self.send_header(k, v)
        self.end_headers()
        if not head:
            self.wfile.write(body)

    def do_HEAD(self):
        print("HEAD", self.path, flush=True)
        if self.path == JAR:
            self.send_response(200)
            self.send_header("Content-Length", str(len(PAYLOAD)))
            self.end_headers()
        else:
            self.send(404, head=True)

    def do_GET(self):
        print("GET", self.path, self.headers.get("If-None-Match") or "", flush=True)
        if self.path.startswith("/api/releases") and LIMITED:
            self.send(403, b'{"message": "API rate limit exceeded"}', "application/json",
                      extra=(("X-RateLimit-Remaining", "0"),))
        elif self.path.startswith("/api/releases") and self.headers.get("If-None-Match") == ETAG:
            self.send(304, extra=(("ETag", ETAG),))
        elif self.path == "/web/releases.atom":
            self.send(200, ATOM, "application/atom+xml")
        elif self.path == JAR + ".sha256":
            self.send(200, ("%s  Decimation.jar\n" % HEX).encode())
        elif self.path.startswith("/api/releases"):
            body = json.dumps([{"tag_name": TAG, "draft": False, "prerelease": True, "assets": [
                {"name": "Decimation.jar", "size": len(PAYLOAD), "digest": DIGEST,
                 "browser_download_url": REMOTE or "http://127.0.0.1:%d/dl/Decimation.jar" % PORT}]}]).encode()
            self.send_response(200)
            self.send(200, body, "application/json", extra=(("ETag", ETAG),))
        elif self.path.startswith("/dl/") or self.path == JAR:
            self.send_response(200)
            self.send_header("Content-Length", str(len(PAYLOAD)))
            self.end_headers()
            step = 256 * 1024
            for i in range(0, len(PAYLOAD), step):
                self.wfile.write(PAYLOAD[i:i + step])
                time.sleep(step / (RATE * 1024 * 1024))
        else:
            self.send_response(404)
            self.end_headers()


print("fake release server on %d: tag %s, %d MB, digest %s%s" % (PORT, TAG, MB, DIGEST[:20], ", API LIMITED" if LIMITED else ""),
      flush=True)
http.server.ThreadingHTTPServer(("127.0.0.1", PORT), H).serve_forever()

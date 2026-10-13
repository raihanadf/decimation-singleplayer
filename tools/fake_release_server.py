#!/usr/bin/env python3
"""A stand in for GitHub's release API for the dev test `update` (fixes/Updater): serves
/api/releases (one release TAG with a Decimation.jar asset, its sha256 digest and size) and
/dl/Decimation.jar (MB megabytes starting with b"NEWJAR", sent at RATE MB/s so the progress bar moves).
usage: python3 tools/fake_release_server.py [PORT=8765] [TAG=9.9.9] [MB=40] [RATE=8] [BADDIGEST]"""
import hashlib, http.server, json, os, sys, time

PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 8765
TAG = sys.argv[2] if len(sys.argv) > 2 else "9.9.9"
MB = int(sys.argv[3]) if len(sys.argv) > 3 else 40
RATE = float(sys.argv[4]) if len(sys.argv) > 4 else 8
BAD = "BADDIGEST" in sys.argv
PAYLOAD = b"NEWJAR" + os.urandom(MB * 1024 * 1024 - 6)
DIGEST = "sha256:" + ("0" * 64 if BAD else hashlib.sha256(PAYLOAD).hexdigest())


class H(http.server.BaseHTTPRequestHandler):
    def log_message(self, *a):
        pass

    def do_GET(self):
        if self.path.startswith("/api/releases"):
            body = json.dumps([{"tag_name": TAG, "draft": False, "prerelease": True, "assets": [
                {"name": "Decimation.jar", "size": len(PAYLOAD), "digest": DIGEST,
                 "browser_download_url": "http://127.0.0.1:%d/dl/Decimation.jar" % PORT}]}]).encode()
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)
        elif self.path.startswith("/dl/"):
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


print("fake release server on %d: tag %s, %d MB, digest %s" % (PORT, TAG, MB, DIGEST[:20]), flush=True)
http.server.ThreadingHTTPServer(("127.0.0.1", PORT), H).serve_forever()

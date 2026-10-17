# Player hosting spike tools (18 Oktober 2026, docs/player_hosting.md section 6)

Throwaway quality, kept so the measurement can be repeated.

- `relay.py PLAYER_PORT CONTROL_PORT LOG [LOGIN_DELAY_S]`: reverse tunnel relay. Logs the Minecraft handshake (the
  address the player typed), opens and closes, and bytes per connection per second (JSON lines).
- `agent.py RELAY_HOST CONTROL_PORT PORT_FILE`: the host side. Connects OUT to the relay; for each player connection it
  connects back and pipes to the LAN world port read from PORT_FILE (dev test `lanhost` writes
  dev/run/client/devtest/lan_port.txt).
- `run_join.sh.example`: the production client (docs/production_test.md) as a second player that joins through the
  relay at start (`--server NAME --port 25600`). Replace PRODCLIENT with its folder.
- `LineTag.java` / `ListInsn.java` (ASM 5, Java 8): give every instruction of a class its own line number so a crash
  report names the exact instruction in an obfuscated class, then list the instructions around that number.

Run: relay, `python3 tools/devtest.py --live lanhost online=false wait=9000`, agent once the port file exists, then
the joiner. One Mac with 16 GB runs both games only just (swap); see the traps in docs/player_hosting.md section 6.
- `run_join_live.sh.example`: the joiner with its own live test port (25598) and the shared LanBus folder (BUSDIR) for
  `lanjoin`; it joins from Decimation's menu by itself (no --server).

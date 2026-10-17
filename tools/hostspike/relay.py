"""Spike relay (throwaway): reverse tunnel like a relay server of ours would be.

relay.py PLAYER_PORT CONTROL_PORT LOG
  The host's agent connects to CONTROL_PORT and says HELLO. A player connects to PLAYER_PORT; the relay reads the
  Minecraft handshake (logs the address the player typed), tells the agent "OPEN id", the agent connects back to
  CONTROL_PORT with "DATA id", then the relay pipes both ways. Bytes per connection and second go to LOG.
agent.py RELAY_HOST CONTROL_PORT PORT_FILE   (host side: pipes each DATA connection to the LAN world port)
"""
import asyncio, json, sys, time

PLAYER, CONTROL, LOG = int(sys.argv[1]), int(sys.argv[2]), sys.argv[3]
DELAY = float(sys.argv[4]) if len(sys.argv) > 4 else 0
agent = None
pending = {}
seq = 0
stats = {}


def log(**k):
    k['t'] = round(time.time(), 2)
    with open(LOG, 'a') as f:
        f.write(json.dumps(k) + '\n')


def varint(b, i):
    n = s = 0
    while True:
        c = b[i]; i += 1
        n |= (c & 0x7f) << s; s += 7
        if not c & 0x80:
            return n, i


async def pipe(r, w, cid, way):
    try:
        while True:
            d = await r.read(65536)
            if not d:
                break
            stats[cid][way] += len(d)
            w.write(d)
            await w.drain()
    except Exception as e:
        log(ev='pipe_error', id=cid, way=way, err=str(e))
    finally:
        try: w.close()
        except Exception: pass


async def player(r, w):
    global seq
    seq += 1
    cid = seq
    first = await r.read(4096)
    nxt = 0
    try:
        ln, i = varint(first, 0); pid, i = varint(first, i); proto, i = varint(first, i); sl, i = varint(first, i)
        addr = first[i:i + sl].decode('utf8', 'replace'); i += sl
        port = int.from_bytes(first[i:i + 2], 'big'); nxt, _ = varint(first, i + 2)
        log(ev='handshake', id=cid, packet=pid, proto=proto, addr=addr, port=port, next=nxt)
    except Exception as e:
        log(ev='handshake_unreadable', id=cid, err=str(e), head=first[:40].hex())
    if nxt == 2 and DELAY:
        log(ev='login_delay', id=cid, s=DELAY); await asyncio.sleep(DELAY)
    if agent is None:
        log(ev='no_host', id=cid); w.close(); return
    fut = asyncio.get_event_loop().create_future()
    pending[cid] = fut
    agent.write(f'OPEN {cid}\n'.encode()); await agent.drain()
    hr, hw = await asyncio.wait_for(fut, 10)
    stats[cid] = {'up': 0, 'down': 0}
    hw.write(first); stats[cid]['up'] += len(first); await hw.drain()
    log(ev='open', id=cid)
    await asyncio.gather(pipe(r, hw, cid, 'up'), pipe(hr, w, cid, 'down'))
    log(ev='close', id=cid, **stats.pop(cid))


async def control(r, w):
    global agent
    line = (await r.readline()).decode().split()
    if line[0] == 'HELLO':
        agent = w
        log(ev='host_online')
        await r.read()  # held until the agent leaves
        agent = None
        log(ev='host_offline')
    elif line[0] == 'DATA':
        pending.pop(int(line[1])).set_result((r, w))


async def ticker():
    last = {}
    while True:
        await asyncio.sleep(1)
        for cid, s in list(stats.items()):
            p = last.get(cid, {'up': 0, 'down': 0})
            log(ev='rate', id=cid, up=s['up'] - p['up'], down=s['down'] - p['down'])
            last[cid] = dict(s)


async def main():
    await asyncio.start_server(player, '127.0.0.1', PLAYER)
    await asyncio.start_server(control, '127.0.0.1', CONTROL)
    log(ev='relay_up', player=PLAYER, control=CONTROL)
    await ticker()

asyncio.run(main())

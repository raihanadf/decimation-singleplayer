"""Host side of the spike relay (throwaway): agent.py RELAY_HOST CONTROL_PORT PORT_FILE.
Connects OUT to the relay (as the mod would), and for each OPEN id connects back with DATA id and to the LAN world."""
import asyncio, sys

HOST, CONTROL, PORT_FILE = sys.argv[1], int(sys.argv[2]), sys.argv[3]


async def pipe(r, w):
    try:
        while True:
            d = await r.read(65536)
            if not d:
                break
            w.write(d)
            await w.drain()
    except Exception:
        pass
    finally:
        try: w.close()
        except Exception: pass


async def open_one(cid):
    lan = int(open(PORT_FILE).read().strip())
    lr, lw = await asyncio.open_connection('127.0.0.1', lan)
    dr, dw = await asyncio.open_connection(HOST, CONTROL)
    dw.write(f'DATA {cid}\n'.encode()); await dw.drain()
    await asyncio.gather(pipe(dr, lw), pipe(lr, dw))


async def main():
    r, w = await asyncio.open_connection(HOST, CONTROL)
    w.write(b'HELLO\n'); await w.drain()
    print('agent online', flush=True)
    while True:
        line = await r.readline()
        if not line:
            break
        cmd, cid = line.decode().split()
        if cmd == 'OPEN':
            asyncio.ensure_future(open_one(int(cid)))

asyncio.run(main())

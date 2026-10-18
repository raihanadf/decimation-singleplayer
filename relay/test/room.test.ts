import { SELF, env, runInDurableObject, runDurableObjectAlarm } from 'cloudflare:test';
import { describe, it, expect } from 'vitest';

const run = (runInDurableObject as unknown) as (stub: unknown, f: (o: any, s: any) => any) => Promise<void>;

async function register(ip: string, max = 4) {
  const r = await SELF.fetch('https://relay/list/host', {
    method: 'POST', headers: { 'CF-Connecting-IP': ip },
    body: JSON.stringify({ name: 'Room test', host: 'Ucil', online: false, max, mods: [], version: '0.64.9' }),
  });
  return (await r.json()) as { room: string; secret: string };
}

async function ws(path: string) {
  const r = await SELF.fetch('https://relay' + path, { headers: { Upgrade: 'websocket' } });
  const s = r.webSocket;
  const inbox: any[] = [];
  if (!s) return { status: r.status, socket: null as any, inbox };
  s.accept();
  // the test side's WebSocket hands binary messages over as Blobs (browser default)
  s.addEventListener('message', async (e) => inbox.push(e.data instanceof Blob ? await e.data.arrayBuffer() : e.data));
  s.addEventListener('close', (e) => inbox.push({ closed: e.code, reason: e.reason }));
  return { status: r.status, socket: s, inbox };
}

const until = async (f: () => any, ms = 2000) => {
  const t = Date.now();
  while (!f()) {
    if (Date.now() - t > ms) throw new Error('timeout');
    await new Promise((r) => setTimeout(r, 10));
  }
};

describe('room', () => {
  it('pairs a joiner with a host data socket and forwards bytes both ways', async () => {
    const { room, secret } = await register('10.1.0.1');
    const control = await ws(`/host/${room}?secret=${secret}`);
    const joiner = await ws(`/join/${room}`);
    await until(() => control.inbox.length > 0);
    const id = JSON.parse(control.inbox[0]).open;
    const data = await ws(`/host/${room}/data/${id}?secret=${secret}`);
    await until(() => joiner.inbox.some((m) => typeof m === 'string' && m.includes('ready')));
    joiner.socket.send(new Uint8Array([1, 2, 3]));
    await until(() => data.inbox.length > 0);
    expect(new Uint8Array(data.inbox[0] as ArrayBuffer)).toEqual(new Uint8Array([1, 2, 3]));
    data.socket.send(new Uint8Array([9]));
    await until(() => joiner.inbox.some((m) => m instanceof ArrayBuffer));
  });

  it('refuses a joiner when the host has no control socket', async () => {
    const { room } = await register('10.1.0.2');
    const j = await ws(`/join/${room}`);
    expect(j.status).toBe(404);
  });

  it('refuses joiners beyond max (the host counts as one player)', async () => {
    const { room, secret } = await register('10.1.0.3', 2);
    await ws(`/host/${room}?secret=${secret}`);
    expect((await ws(`/join/${room}`)).status).toBe(101);
    expect((await ws(`/join/${room}`)).status).toBe(409);
  });

  it('closes the joiners with 4410 once a closed control socket stays gone past the grace', async () => {
    const { room, secret } = await register('10.1.0.4');
    const control = await ws(`/host/${room}?secret=${secret}`);
    const joiner = await ws(`/join/${room}`);
    control.socket.close(1000, 'bye');
    await new Promise((r) => setTimeout(r, 100));
    expect(joiner.inbox.some((m) => m && m.closed === 4410)).toBe(false); // not at once
    const stub = env.ROOM.get(env.ROOM.idFromName(room));
    await run(stub, async (_o: any, state: any) => state.storage.put('lostAt', Date.now() - 60_000));
    await runDurableObjectAlarm(stub);
    await until(() => joiner.inbox.some((m) => m && m.closed === 4410));
  });

  it('a host socket with a wrong secret never reaches the room', async () => {
    const { room } = await register('10.1.0.5');
    const r = await SELF.fetch(`https://relay/host/${room}?secret=wrong`, { headers: { Upgrade: 'websocket' } });
    expect(r.status).toBe(404);
  });

  it('a second data socket for the same joiner replaces the first, and the joiner stays (a host retrying a far socket)', async () => {
    const { room, secret } = await register('10.1.0.6');
    const control = await ws(`/host/${room}?secret=${secret}`);
    const joiner = await ws(`/join/${room}`);
    await until(() => control.inbox.length > 0);
    const id = JSON.parse(control.inbox[0]).open;
    const first = await ws(`/host/${room}/data/${id}?secret=${secret}`);
    expect(first.status).toBe(101);
    const second = await ws(`/host/${room}/data/${id}?secret=${secret}`);
    expect(second.status).toBe(101);
    await until(() => first.inbox.some((m) => m && m.closed)); // the old one is closed
    await new Promise((r) => setTimeout(r, 100));
    expect(joiner.inbox.some((m) => m && m.closed)).toBe(false); // the joiner is not
    joiner.socket.send(new Uint8Array([7, 8]).buffer); // and its bytes reach the new data socket
    await until(() => second.inbox.some((m) => m instanceof ArrayBuffer && new Uint8Array(m)[0] === 7));
  });

  it('a later join does not push back the first joiner\'s 10 s deadline', async () => {
    const { room, secret } = await register('10.1.0.7', 8);
    await ws(`/host/${room}?secret=${secret}`);
    const before = Date.now();
    await ws(`/join/${room}`);
    await new Promise((r) => setTimeout(r, 50));
    await ws(`/join/${room}`);
    const stub = env.ROOM.get(env.ROOM.idFromName(room));
    let alarm: number | null = null;
    await (runInDurableObject as unknown as (s: unknown, f: (o: any, st: any) => Promise<void>) => Promise<void>)(stub,
      async (_o, st) => { alarm = await st.storage.getAlarm(); });
    expect(alarm).not.toBeNull();
    expect(alarm! - before).toBeLessThanOrEqual(10_000 + 40);
  });

  it('a host reconnecting its control socket keeps the joiners', async () => {
    const { room, secret } = await register('10.1.0.8');
    await ws(`/host/${room}?secret=${secret}`);
    const joiner = await ws(`/join/${room}`);
    await ws(`/host/${room}?secret=${secret}`);
    await new Promise((r) => setTimeout(r, 100));
    expect(joiner.inbox.some((m) => m && m.closed === 4410)).toBe(false);
  });

  it('a control socket blip keeps the joiners and the listing (grace for the host to come back)', async () => {
    const { room, secret } = await register('10.1.0.9');
    const control = await ws(`/host/${room}?secret=${secret}`);
    const joiner = await ws(`/join/${room}`);
    control.socket.close(1000, 'network blip');
    await new Promise((r) => setTimeout(r, 300));
    expect(joiner.inbox.some((m) => m && m.closed)).toBe(false);
    const listed = ((await (await SELF.fetch('https://relay/list')).json()) as any[]).some((e) => e.room === room);
    expect(listed).toBe(true);
    await ws(`/host/${room}?secret=${secret}`); // the host is back in time
    const stub = env.ROOM.get(env.ROOM.idFromName(room));
    await run(stub, async (_o: any, state: any) => expect(await state.storage.get('lostAt')).toBeUndefined());
  });

  it('a host gone longer than the grace: joiners closed (4410) and off the list', async () => {
    const { room, secret } = await register('10.1.0.10');
    const control = await ws(`/host/${room}?secret=${secret}`);
    const joiner = await ws(`/join/${room}`);
    control.socket.close(1000, 'quit');
    const stub = env.ROOM.get(env.ROOM.idFromName(room));
    await until(() => true);
    await new Promise((r) => setTimeout(r, 100));
    await run(stub, async (_o: any, state: any) => state.storage.put('lostAt', Date.now() - 60_000)); // the grace ran out
    await runDurableObjectAlarm(stub);
    await until(() => joiner.inbox.some((m) => m && m.closed === 4410));
    const listed = ((await (await SELF.fetch('https://relay/list')).json()) as any[]).some((e) => e.room === room);
    expect(listed).toBe(false);
  });
});


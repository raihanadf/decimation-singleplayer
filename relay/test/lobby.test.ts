import { SELF, env, runInDurableObject } from 'cloudflare:test';
import { describe, it, expect } from 'vitest';

const host = (name = 'Ucil world', extra: object = {}, ip = '10.0.0.1') =>
  SELF.fetch('https://relay/list/host', {
    method: 'POST',
    headers: { 'CF-Connecting-IP': ip },
    body: JSON.stringify({ name, host: 'Ucil', online: false, max: 4, mods: [['deciworldgen', '0.64.9']], version: '0.64.9', ...extra }),
  });
const list = async () => (await (await SELF.fetch('https://relay/list')).json()) as any[];
const lobby = () => env.LOBBY.get(env.LOBBY.idFromName('lobby'));
const run = (runInDurableObject as unknown) as (stub: unknown, f: (o: any) => void) => Promise<void>; // the typed form is too deep for tsc

describe('lobby', () => {
  it('registers a world and lists it without its secret', async () => {
    const r = await host('First world', {}, '10.0.0.11');
    expect(r.status).toBe(200);
    const { room, secret } = (await r.json()) as any;
    expect(room).toMatch(/^[a-z0-9]{16}$/);
    expect(secret).toMatch(/^[a-z0-9]{32}$/);
    const e = (await list()).find((x) => x.room === room);
    expect(e).toMatchObject({ name: 'First world', host: 'Ucil', online: false, players: 0, max: 4, route: 'relay' });
    expect(e.secret).toBeUndefined();
  });

  it('name rules: empty, over 32 characters, formatting codes are refused', async () => {
    expect((await host('', {}, '10.0.0.12')).status).toBe(400);
    expect((await host('x'.repeat(33), {}, '10.0.0.13')).status).toBe(400);
    expect((await host('§cRed world', {}, '10.0.0.14')).status).toBe(400);
    expect((await host('ok', { max: 9 }, '10.0.0.15')).status).toBe(400);
  });

  it('one registration per address per minute', async () => {
    expect((await host('a', {}, '10.0.0.16')).status).toBe(200);
    expect((await host('b', {}, '10.0.0.16')).status).toBe(429);
  });

  it('beat updates players; a wrong secret is refused', async () => {
    const { room, secret } = (await (await host('Beat world', {}, '10.0.0.17')).json()) as any;
    const ok = await SELF.fetch('https://relay/list/beat', { method: 'POST', body: JSON.stringify({ room, secret, players: 2 }) });
    expect(await ok.json()).toEqual({ ok: true });
    expect((await list()).find((x) => x.room === room).players).toBe(2);
    const bad = await SELF.fetch('https://relay/list/beat', { method: 'POST', body: JSON.stringify({ room, secret: 'no', players: 1 }) });
    expect(bad.status).toBe(403);
  });

  it('drops an entry 180 s after its last heartbeat and answers gone', async () => {
    const { room, secret } = (await (await host('Stale world', {}, '10.0.0.18')).json()) as any;
    await run(lobby(), (o: any) => { o.offset = 181_000; });
    expect((await list()).find((x) => x.room === room)).toBeUndefined();
    const r = await SELF.fetch('https://relay/list/beat', { method: 'POST', body: JSON.stringify({ room, secret, players: 0 }) });
    expect(await r.json()).toEqual({ gone: true });
    await run(lobby(), (o: any) => { o.offset = 0; });
  });

  it('stop removes the entry at once', async () => {
    const { room, secret } = (await (await host('Stop world', {}, '10.0.0.19')).json()) as any;
    await SELF.fetch('https://relay/list/stop', { method: 'POST', body: JSON.stringify({ room, secret }) });
    expect((await list()).find((x) => x.room === room)).toBeUndefined();
  });

  it('check: host needs the secret, a joiner needs a listed room', async () => {
    const { room, secret } = (await (await host('Check world', {}, '10.0.0.20')).json()) as any;
    expect(await lobby().check(room, secret)).toEqual({ ok: true, max: 4 });
    expect(await lobby().check(room, 'wrong')).toEqual({ ok: false, max: 0 });
    expect(await lobby().check(room, null)).toEqual({ ok: true, max: 4 });
    expect(await lobby().check('nosuchroom000000', null)).toEqual({ ok: false, max: 0 });
  });

  it('refuses a body over 8 KB without reading it into the list', async () => {
    const big = JSON.stringify({ name: 'Big', host: 'Ucil', online: false, max: 4, mods: [['x'.repeat(60), 'y'.repeat(30)]], version: '1', pad: 'z'.repeat(9000) });
    const r = await SELF.fetch('https://relay/list/host', { method: 'POST', headers: { 'CF-Connecting-IP': '10.0.0.30' }, body: big });
    expect(r.status).toBe(413);
  });

  it('holds at most 200 worlds: the next registration is refused', async () => {
    await run(lobby(), (o: any) => {
      for (let i = 0; i < 200; i++) o.entries.set('fill' + i, { room: 'fill' + i, secret: 's', name: 'f', host: 'h', online: false, players: 0, max: 2, mods: [], version: '1', since: o.now(), seen: o.now() });
    });
    expect((await host('One too many', {}, '10.0.0.31')).status).toBe(503);
    await run(lobby(), (o: any) => { for (let i = 0; i < 200; i++) o.entries.delete('fill' + i); });
  });

  it('forgets addresses older than a minute', async () => {
    await host('Forget world', {}, '10.0.0.32');
    await run(lobby(), (o: any) => { o.offset = 61_000; });
    await host('Another', {}, '10.0.0.33');
    let kept = -1;
    await run(lobby(), (o: any) => { kept = o.registered.has('10.0.0.32') ? 1 : 0; o.offset = 0; });
    expect(kept).toBe(0);
  });

  it('an address holds at most 2 worlds, even a minute apart', async () => {
    const ip = '10.0.0.40';
    expect((await host('One', {}, ip)).status).toBe(200);
    await run(lobby(), (o: any) => { o.offset = 61_000; });
    expect((await host('Two', {}, ip)).status).toBe(200);
    await run(lobby(), (o: any) => { o.offset = 122_000; });
    for (const e of (await list())) await run(lobby(), (o: any) => { const x = o.entries.get(e.room); if (x) x.seen = o.now(); });
    expect((await host('Three', {}, ip)).status).toBe(429);
    await run(lobby(), (o: any) => { o.offset = 0; for (const [k, e] of o.entries) if (e.ip === ip) o.entries.delete(k); });
  });

  it('a chunked body over 8 KB (no length header) is refused', async () => {
    const chunk = new TextEncoder().encode('{"name":"' + 'z'.repeat(5000));
    const body = new ReadableStream({ start(c) { c.enqueue(chunk); c.enqueue(chunk); c.close(); } });
    const r = await SELF.fetch('https://relay/list/host', { method: 'POST', headers: { 'CF-Connecting-IP': '10.0.0.41' }, body, duplex: 'half' } as any);
    expect(r.status).toBe(413);
  });

  it('direct addresses: never on the list, handed out only for a listed world, route says direct', async () => {
    const r = await host('Direct world', { direct: ['[2001:db8::5]:51234'] }, '10.0.0.31');
    expect(r.status).toBe(200);
    const { room } = (await r.json()) as any;
    const e = (await list()).find((x) => x.room === room);
    expect(e.route).toBe('direct');
    expect(e.direct).toBeUndefined();
    expect(JSON.stringify(await list())).not.toContain('2001:db8');
    const d = await SELF.fetch('https://relay/list/direct', { method: 'POST', body: JSON.stringify({ room }) });
    expect(d.status).toBe(200);
    expect(((await d.json()) as any).direct).toEqual(['[2001:db8::5]:51234']);
    const gone = await SELF.fetch('https://relay/list/direct', { method: 'POST', body: JSON.stringify({ room: 'nosuchroom000000' }) });
    expect(gone.status).toBe(404);
  });

  it('direct addresses: only global IPv6 with a port, at most 4; else refused', async () => {
    const bad = [['[fe80::1]:51234'], ['[fd00::1]:51234'], ['[::1]:51234'], ['[::ffff:1.2.3.4]:51234'], ['1.2.3.4:51234'],
      ['[2001:db8::5]'], ['[2001:db8::5]:80'], ['[2001:db8::5]:70000'], ['example.com:25565'], 'x',
      ['[2001:db8::1]:51234', '[2001:db8::2]:51234', '[2001:db8::3]:51234', '[2001:db8::4]:51234', '[2001:db8::5]:51234']];
    let n = 40;
    for (const direct of bad) expect((await host('Bad direct', { direct }, `10.0.0.${n++}`)).status).toBe(400);
  });

  it('a world without direct addresses stays route relay and hands out none', async () => {
    const { room } = (await (await host('Relay world', {}, '10.0.0.60')).json()) as any;
    expect((await list()).find((x) => x.room === room).route).toBe('relay');
    const d = await SELF.fetch('https://relay/list/direct', { method: 'POST', body: JSON.stringify({ room }) });
    expect(((await d.json()) as any).direct).toEqual([]);
  });

  it('asking for direct addresses: at most 10 a minute per address (no harvesting the whole list)', async () => {
    const { room } = (await (await host('Asked world', { direct: ['[2001:db8::9]:51234'] }, '10.0.0.70')).json()) as any;
    const ask = (ip: string) => SELF.fetch('https://relay/list/direct', { method: 'POST', headers: { 'CF-Connecting-IP': ip }, body: JSON.stringify({ room }) });
    for (let i = 0; i < 10; i++) expect((await ask('10.0.0.71')).status).toBe(200);
    expect((await ask('10.0.0.71')).status).toBe(429);
    expect((await ask('10.0.0.72')).status).toBe(200); // another address is not held back
  });
});


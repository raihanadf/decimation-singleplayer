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
});

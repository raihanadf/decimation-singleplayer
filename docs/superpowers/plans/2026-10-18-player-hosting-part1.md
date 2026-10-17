# Player Hosting Part 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A player hosts the world they are in from the pause menu; it appears on Decimation's Community page with its mods; anyone with the same mods joins it with one click through our free Cloudflare relay.

**Architecture:** A Cloudflare Worker (`relay/`, TypeScript) with two Durable Objects: Lobby (the public list, one instance) and Room (one per hosted world, pairs joiner and host WebSockets and forwards bytes). Our mod (`net.decimation.fixes.hosting`) holds the host side (LAN world, heartbeat, control and data sockets piped to the LAN port) and the join side (one local port per listed world, piped to the Room); Decimation's own pause menu and Community page show it, through `fixes/Deci` accessors and Forge GUI events.

**Tech Stack:** Cloudflare Workers + Durable Objects (free plan, SQLite backed), wrangler 4, vitest with @cloudflare/vitest-pool-workers; Java 8, Forge 1.7.10 (MCP names in dev), netty 4.0.10 and Gson 2.2.4 as Minecraft ships them, JUnit 5 for plain Java units; the existing dev test harness (tools/devtest.py, dev tests `lanhost`, `lanjoin`).

**Spec:** docs/superpowers/specs/2026-10-18-player-hosting-part1-design.md (approved 18 Oktober 2026). Background and measurements: docs/player_hosting.md sections 1 to 10.

## Global Constraints

- Free services only: Cloudflare free plan; nothing that needs a card (spec section 2).
- The user creates the Cloudflare account and runs `npx wrangler login` themselves; no token or key in the chat or in any file of a repo (spec 9.4). Deploying (`wrangler deploy`) is outward facing: ask the user before the first deploy.
- List entry fields exactly: `room, name, host, online, players, max, mods, version, route, since`; `name` 1 to 32 characters, no `§`; `max` 2 to 8, default 4; `route` is `"relay"` in part 1 (spec 3.3).
- Heartbeat every 60 s; an entry is dropped 3 min (180 s) after its last heartbeat (spec 3.1, 6).
- A joiner socket with no host data socket within 10 s is closed with "host did not answer" (spec 3.2).
- Client writes to the relay are coalesced per 50 ms (one game tick) (spec 3.1).
- The daily free limit resets at 00:00 UTC = 07:00 WIB; the player text says 07:00 WIB (spec 5).
- Our code never names an obfuscated Decimation class outside `fixes/Deci` (CLAUDE.md); new obfuscated names come from `deobf/decimation.srg`.
- No dash punctuation in any text a player or reader sees (toasts, labels, docs, commits).
- Java sources write formatting codes and symbols as unicode escapes (backslash u00a7 for the section sign, backslash u25cf for the dot), as the existing code does; the code blocks below show them as characters.
- Dev tests only in the world `deciworldgen_devtest`; before a live run `pgrep -fl "zulu-8.jdk.*GradleStart"`; `mainmenu` and `menupages` run last.
- Commits: one plain line, no attribution; never `git push` (the user pushes). Never commit jars or Decimation content.
- After each task: docs touched by it updated in the same commit (docs/player_hosting.md, CLAUDE.md where architecture changes, docs/tasks.md).

## Review Focus

1. The host leaves the world or quits while players are connected: every socket closes, the list entry is removed at once (not after 3 min), the joiners see "The host stopped hosting". Test: Task 7 step "world unload stops hosting".
2. Cloudflare's daily quota runs out mid day: the Worker answers an error status; the mod shows the 07:00 WIB toast once and does not retry in a loop. Test: Task 5 "quota maps to RelayFull".
3. A host name that is empty, longer than 32 characters or has `§` codes: refused by the Lobby with 400; HostScreen cleans it (ListEntry.cleanName) before sending. Tests: Task 1 "name rules", Task 3 "nameCleaned".
4. Local ports leak: every listener JoinService opened for the Community page closes when the page closes, and the join's listener closes when the game leaves the world. Test: Task 8 "close frees the ports".
5. A missed heartbeat (laptop asleep, network hiccup): the Lobby answers `{gone:true}`; the host registers again by itself with the same settings and keeps hosting. Test: Task 7 "gone re-registers".

---

## File Structure

| File | Job |
|---|---|
| `relay/package.json`, `relay/wrangler.jsonc`, `relay/tsconfig.json`, `relay/vitest.config.ts` | Worker project (new, public in the mod repo: no Decimation content) |
| `relay/src/index.ts` | routes `/list...` to Lobby, `/host/...` and `/join/...` to Room after Lobby's check |
| `relay/src/lobby.ts` | Lobby Durable Object: register, beat, stop, list, check |
| `relay/src/room.ts` | Room Durable Object: control socket, joiner sockets, data sockets, forwarding, 10 s alarm |
| `relay/test/lobby.test.ts`, `relay/test/room.test.ts` | vitest |
| `dev/src/main/java/net/decimation/fixes/hosting/ModSet.java` | networked mod list of this game, compare two sets |
| `.../hosting/ListEntry.java` | one list entry (Gson fields), validation helpers |
| `.../hosting/RelayClient.java` | HTTP calls: host, beat, stop, list; error mapping (`RelayFull`, `RelayGone`) |
| `.../hosting/RelaySocket.java` | netty 4.0.10 WebSocket client, TLS with SNI, 50 ms coalesced binary writes, text messages |
| `.../hosting/Pipe.java` | one TCP socket piped to one RelaySocket, both ways |
| `.../hosting/HostService.java` | host state machine: open LAN, register, heartbeat, control socket, data pipes, stop |
| `.../hosting/JoinService.java` | one local listener per listed room, mod gate, pipes, close |
| `.../hosting/HostMenu.java` | pause menu button, LAN icon, Community tab interception (Forge events) |
| `.../hosting/HostScreen.java` | the Host Online page |
| `.../hosting/CommunityList.java` | fetch the list, open Decimation's Community page with our rows, hover mods tooltip |
| `dev/src/main/java/net/decimation/fixes/Deci.java` | new accessors: `toast`, `communityScreen`, `communityRow`, `isCommunityScreen`, `communityRows` |
| `dev/src/main/java/net/decimation/worldgen/DecimationWorldGen.java` | register HostMenu, HostService and CommunityList handlers |
| `dev/src/test/java/net/decimation/fixes/hosting/*Test.java` | JUnit 5 tests of the plain Java units |
| `dev/src/main/java/net/decimation/worldgen/devtest/LanHostTest.java`, `LanJoinTest.java` | the real flow through HostService and the Community page |
| `dev/src/main/resources/assets/deciworldgen/lang/en_US.lang` | player texts |
| `docs/player_hosting.md`, `CLAUDE.md`, `docs/tasks.md`, `docs/production_test.md` | docs |

---

### Task 1: Relay project and the Lobby (the public list)

**Files:**
- Create: `relay/package.json`, `relay/wrangler.jsonc`, `relay/tsconfig.json`, `relay/vitest.config.ts`, `relay/src/index.ts`, `relay/src/lobby.ts`, `relay/src/room.ts` (stub only), `relay/test/lobby.test.ts`, `relay/.gitignore`
- Modify: `.gitignore` (root: `relay/node_modules/`, `relay/.wrangler/`)

**Interfaces:**
- Produces: HTTP `POST /list/host` body `{name, host, online, max, mods, version}` answers `{room, secret}`; `POST /list/beat` `{room, secret, players}` answers `{ok:true}` or `{gone:true}`; `POST /list/stop` `{room, secret}` answers `{ok:true}`; `GET /list` answers an array of `{room, name, host, online, players, max, mods, version, route, since}`; RPC `Lobby.check(room: string, secret: string | null): Promise<{ok: boolean, max: number}>` (secret null = a joiner).

- [ ] **Step 1: Scaffold**

```bash
mkdir -p relay/src relay/test && cd relay
npm init -y
npm i -D wrangler@4 typescript @cloudflare/workers-types @cloudflare/vitest-pool-workers vitest
printf 'node_modules/\n.wrangler/\n' > .gitignore
```

`relay/wrangler.jsonc`:

```jsonc
{
  "name": "deciremnant-relay",
  "main": "src/index.ts",
  "compatibility_date": "2026-10-01",
  "durable_objects": {
    "bindings": [
      { "name": "LOBBY", "class_name": "Lobby" },
      { "name": "ROOM", "class_name": "Room" }
    ]
  },
  "migrations": [{ "tag": "v1", "new_sqlite_classes": ["Lobby", "Room"] }]
}
```

`relay/tsconfig.json`:

```json
{
  "compilerOptions": {
    "target": "es2022", "module": "es2022", "moduleResolution": "bundler", "strict": true,
    "types": ["@cloudflare/workers-types", "@cloudflare/vitest-pool-workers"], "noEmit": true
  },
  "include": ["src", "test"]
}
```

`relay/vitest.config.ts` (WebSockets in Durable Objects need shared storage between tests; every test uses its own room):

```ts
import { defineWorkersConfig } from '@cloudflare/vitest-pool-workers/config';
export default defineWorkersConfig({
  test: { poolOptions: { workers: { isolatedStorage: false, singleWorker: true, wrangler: { configPath: './wrangler.jsonc' } } } },
});
```

`relay/package.json` scripts: `"test": "vitest run"`, `"dev": "wrangler dev --port 8787"`, `"deploy": "wrangler deploy"`, `"typecheck": "tsc"`.

- [ ] **Step 2: Write the failing Lobby tests**

`relay/test/lobby.test.ts`:

```ts
import { SELF, env, runInDurableObject } from 'cloudflare:test';
import { describe, it, expect } from 'vitest';

const host = (name = 'Ucil world', extra: object = {}, ip = '10.0.0.1') =>
  SELF.fetch('https://relay/list/host', {
    method: 'POST', headers: { 'CF-Connecting-IP': ip },
    body: JSON.stringify({ name, host: 'Ucil', online: false, max: 4, mods: [['deciworldgen', '0.64.9']], version: '0.64.9', ...extra }),
  });
const list = async () => (await (await SELF.fetch('https://relay/list')).json()) as any[];
const lobby = () => env.LOBBY.get(env.LOBBY.idFromName('lobby'));

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
    await runInDurableObject(lobby(), (o: any) => { o.offset = 181_000; });
    expect((await list()).find((x) => x.room === room)).toBeUndefined();
    const r = await SELF.fetch('https://relay/list/beat', { method: 'POST', body: JSON.stringify({ room, secret, players: 0 }) });
    expect(await r.json()).toEqual({ gone: true });
    await runInDurableObject(lobby(), (o: any) => { o.offset = 0; });
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
```

- [ ] **Step 3: Run it to see it fail**

Run: `cd relay && npx vitest run test/lobby.test.ts`
Expected: FAIL (cannot import `Lobby`, or 404 answers).

- [ ] **Step 4: Implement**

`relay/src/lobby.ts`:

```ts
import { DurableObject } from 'cloudflare:workers';

export interface Entry {
  room: string; secret: string; name: string; host: string; online: boolean; players: number; max: number;
  mods: [string, string][]; version: string; since: number; seen: number;
}
const STALE_MS = 180_000;
const NAME = /^[^§]{1,32}$/;
const PLAYER = /^[A-Za-z0-9_]{1,16}$/;

function token(n: number): string {
  const a = new Uint8Array(n);
  crypto.getRandomValues(a);
  return Array.from(a, (b) => 'abcdefghijklmnopqrstuvwxyz0123456789'[b % 36]).join('');
}
const json = (o: unknown, status = 200) => new Response(JSON.stringify(o), { status, headers: { 'content-type': 'application/json' } });

export class Lobby extends DurableObject {
  entries = new Map<string, Entry>();
  registered = new Map<string, number>();
  offset = 0; // tests move the clock
  loaded = false;

  now() { return Date.now() + this.offset; }

  async load() {
    if (this.loaded) return;
    for (const e of (await this.ctx.storage.get<Entry[]>('entries')) ?? []) this.entries.set(e.room, e);
    this.loaded = true;
  }
  async save() { await this.ctx.storage.put('entries', [...this.entries.values()]); }
  prune() { const t = this.now(); for (const [k, e] of this.entries) if (t - e.seen > STALE_MS) this.entries.delete(k); }

  async check(room: string, secret: string | null): Promise<{ ok: boolean; max: number }> {
    await this.load(); this.prune();
    const e = this.entries.get(room);
    if (!e || (secret !== null && secret !== e.secret)) return { ok: false, max: 0 };
    return { ok: true, max: e.max };
  }

  async fetch(req: Request): Promise<Response> {
    await this.load(); this.prune();
    const path = new URL(req.url).pathname;
    if (req.method === 'GET' && path === '/list') {
      return json([...this.entries.values()].map(({ secret, seen, ...pub }) => ({ ...pub, route: 'relay' })));
    }
    if (req.method !== 'POST') return json({ error: 'method' }, 405);
    const body = (await req.json().catch(() => null)) as any;
    if (!body) return json({ error: 'json' }, 400);
    if (path === '/list/host') {
      const ip = req.headers.get('CF-Connecting-IP') ?? 'unknown';
      const last = this.registered.get(ip) ?? -Infinity;
      if (this.now() - last < 60_000) return json({ error: 'too often' }, 429);
      const ok = typeof body.name === 'string' && NAME.test(body.name.trim()) && body.name.trim().length > 0
        && typeof body.host === 'string' && PLAYER.test(body.host)
        && typeof body.online === 'boolean' && Number.isInteger(body.max) && body.max >= 2 && body.max <= 8
        && Array.isArray(body.mods) && body.mods.length <= 64
        && body.mods.every((m: any) => Array.isArray(m) && m.length === 2 && `${m[0]}`.length <= 64 && `${m[1]}`.length <= 32)
        && typeof body.version === 'string' && body.version.length <= 32;
      if (!ok) return json({ error: 'invalid' }, 400);
      this.registered.set(ip, this.now());
      const e: Entry = { room: token(16), secret: token(32), name: body.name.trim(), host: body.host, online: body.online, players: 0,
        max: body.max, mods: body.mods.map((m: any) => [`${m[0]}`, `${m[1]}`]), version: body.version, since: this.now(), seen: this.now() };
      this.entries.set(e.room, e);
      await this.save();
      return json({ room: e.room, secret: e.secret });
    }
    const e = this.entries.get(body.room);
    if (path === '/list/beat') {
      if (!e) return json({ gone: true });
      if (e.secret !== body.secret) return json({ error: 'secret' }, 403);
      e.players = Math.max(0, Math.min(e.max, Number(body.players) | 0));
      e.seen = this.now();
      await this.save();
      return json({ ok: true });
    }
    if (path === '/list/stop') {
      if (e && e.secret === body.secret) { this.entries.delete(e.room); await this.save(); }
      return json({ ok: true });
    }
    return json({ error: 'path' }, 404);
  }
}
```

`relay/src/room.ts` (stub until Task 2):

```ts
import { DurableObject } from 'cloudflare:workers';
export class Room extends DurableObject {
  async fetch(): Promise<Response> { return new Response('not yet', { status: 501 }); }
}
```

`relay/src/index.ts`:

```ts
import { Lobby } from './lobby';
import { Room } from './room';
export { Lobby, Room };

interface Env { LOBBY: DurableObjectNamespace<Lobby>; ROOM: DurableObjectNamespace<Room>; }

export default {
  async fetch(req: Request, env: Env): Promise<Response> {
    const url = new URL(req.url);
    const parts = url.pathname.split('/').filter(Boolean);
    const lobby = env.LOBBY.get(env.LOBBY.idFromName('lobby'));
    if (parts[0] === 'list') return lobby.fetch(req);
    if ((parts[0] === 'host' || parts[0] === 'join') && parts[1]) {
      const secret = parts[0] === 'host' ? url.searchParams.get('secret') ?? '' : null;
      const c = await lobby.check(parts[1], secret);
      if (!c.ok) return new Response('room not listed', { status: 404 });
      const fwd = new Request(req, { headers: new Headers(req.headers) });
      fwd.headers.set('x-max', String(c.max));
      return env.ROOM.get(env.ROOM.idFromName(parts[1])).fetch(fwd);
    }
    return new Response('DeciRemnant relay', { status: 200 });
  },
};
```

- [ ] **Step 5: Run the tests**

Run: `cd relay && npx vitest run test/lobby.test.ts && npx tsc`
Expected: 7 passed, tsc no output.

- [ ] **Step 6: Commit**

```bash
git add relay .gitignore
git commit -m "Relay: Lobby keeps the public hosting list (register, beat, stop, list, check)"
```

---

### Task 2: Room (pairing and forwarding)

**Files:**
- Modify: `relay/src/room.ts`
- Create: `relay/test/room.test.ts`

**Interfaces:**
- Consumes: Worker routing and `Lobby.check` from Task 1 (header `x-max`).
- Produces: `WSS /host/{room}?secret=` control socket receives text `{"open":"<id>"}` per joiner and `{"close":"<id>"}`; `WSS /host/{room}/data/{id}?secret=` carries one joiner's bytes; `WSS /join/{room}` joiner socket receives text `{"ready":true}` once its host data socket is attached, then binary bytes; closes: 4404 "host offline", 4409 "full", 4408 "host did not answer", 4410 "host stopped".

- [ ] **Step 1: Write the failing tests**

`relay/test/room.test.ts`:

```ts
import { SELF } from 'cloudflare:test';
import { describe, it, expect } from 'vitest';

async function register(ip: string, max = 4) {
  const r = await SELF.fetch('https://relay/list/host', { method: 'POST', headers: { 'CF-Connecting-IP': ip },
    body: JSON.stringify({ name: 'Room test', host: 'Ucil', online: false, max, mods: [], version: '0.64.9' }) });
  return (await r.json()) as { room: string; secret: string };
}
async function ws(path: string) {
  const r = await SELF.fetch('https://relay' + path, { headers: { Upgrade: 'websocket' } });
  const s = r.webSocket;
  if (!s) return { status: r.status, socket: null as any, inbox: [] as any[] };
  s.accept();
  const inbox: any[] = [];
  s.addEventListener('message', (e) => inbox.push(e.data));
  s.addEventListener('close', (e) => inbox.push({ closed: e.code, reason: e.reason }));
  return { status: r.status, socket: s, inbox };
}
const until = async (f: () => any, ms = 2000) => { const t = Date.now(); while (!f()) { if (Date.now() - t > ms) throw new Error('timeout'); await new Promise((r) => setTimeout(r, 10)); } };

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

  it('closes the joiners with 4410 when the host control socket closes', async () => {
    const { room, secret } = await register('10.1.0.4');
    const control = await ws(`/host/${room}?secret=${secret}`);
    const joiner = await ws(`/join/${room}`);
    control.socket.close(1000, 'stop');
    await until(() => joiner.inbox.some((m) => m.closed === 4410));
  });

  it('a host socket with a wrong secret never reaches the room', async () => {
    const { room } = await register('10.1.0.5');
    const r = await SELF.fetch(`https://relay/host/${room}?secret=wrong`, { headers: { Upgrade: 'websocket' } });
    expect(r.status).toBe(404);
  });
});
```

- [ ] **Step 2: Run to see it fail**

Run: `cd relay && npx vitest run test/room.test.ts`
Expected: FAIL (501 "not yet").

- [ ] **Step 3: Implement**

`relay/src/room.ts`:

```ts
import { DurableObject } from 'cloudflare:workers';

const ANSWER_MS = 10_000;

export class Room extends DurableObject {
  async fetch(req: Request): Promise<Response> {
    if (req.headers.get('Upgrade') !== 'websocket') return new Response('websocket only', { status: 426 });
    const parts = new URL(req.url).pathname.split('/').filter(Boolean);
    const max = Number(req.headers.get('x-max') ?? '8');
    const [client, server] = Object.values(new WebSocketPair());
    if (parts[0] === 'host' && parts.length === 2) {
      for (const old of this.ctx.getWebSockets('control')) old.close(1000, 'replaced');
      this.ctx.acceptWebSocket(server, ['control']);
    } else if (parts[0] === 'host' && parts[2] === 'data' && parts[3]) {
      const id = parts[3];
      const joiner = this.ctx.getWebSockets('join:' + id)[0];
      if (!joiner) return new Response('no such joiner', { status: 404 });
      this.ctx.acceptWebSocket(server, ['data', 'data:' + id]);
      joiner.send(JSON.stringify({ ready: true }));
    } else if (parts[0] === 'join') {
      const control = this.ctx.getWebSockets('control')[0];
      if (!control) return new Response('host offline', { status: 404 });
      if (this.ctx.getWebSockets('join').length >= max - 1) return new Response('full', { status: 409 }); // the host is a player too
      const id = crypto.randomUUID().replace(/-/g, '');
      this.ctx.acceptWebSocket(server, ['join', 'join:' + id]);
      server.serializeAttachment({ id, at: Date.now() });
      control.send(JSON.stringify({ open: id }));
      await this.ctx.storage.setAlarm(Date.now() + ANSWER_MS);
    } else {
      return new Response('path', { status: 404 });
    }
    return new Response(null, { status: 101, webSocket: client });
  }

  private partner(ws: WebSocket): WebSocket | undefined {
    for (const t of this.ctx.getTags(ws)) {
      if (t.startsWith('join:')) return this.ctx.getWebSockets('data:' + t.slice(5))[0];
      if (t.startsWith('data:')) return this.ctx.getWebSockets('join:' + t.slice(5))[0];
    }
    return undefined;
  }

  async webSocketMessage(ws: WebSocket, msg: string | ArrayBuffer) {
    if (typeof msg === 'string') return; // only the relay talks text
    this.partner(ws)?.send(msg);
  }

  async webSocketClose(ws: WebSocket, code: number, reason: string) {
    const tags = this.ctx.getTags(ws);
    if (tags.includes('control')) {
      for (const s of this.ctx.getWebSockets('join')) s.close(4410, 'host stopped');
      for (const s of this.ctx.getWebSockets('data')) s.close(1000, 'host stopped');
      return;
    }
    const other = this.partner(ws);
    if (other) other.close(1000, reason || 'closed');
    const join = tags.find((t) => t.startsWith('join:'));
    if (join) this.ctx.getWebSockets('control')[0]?.send(JSON.stringify({ close: join.slice(5) }));
  }

  async alarm() {
    const now = Date.now();
    for (const s of this.ctx.getWebSockets('join')) {
      const a = s.deserializeAttachment() as { id: string; at: number } | null;
      if (a && !this.ctx.getWebSockets('data:' + a.id)[0] && now - a.at >= ANSWER_MS) s.close(4408, 'host did not answer');
    }
  }
}
```

- [ ] **Step 4: Run all relay tests**

Run: `cd relay && npx vitest run && npx tsc`
Expected: 12 passed (7 lobby, 5 room).

- [ ] **Step 5: Commit**

```bash
git add relay
git commit -m "Relay: Room pairs joiner and host sockets and forwards bytes"
```

---

### Task 3: Java test setup, ModSet and ListEntry

**Files:**
- Modify: `dev/dependencies.gradle` (test dependencies), `dev/build.gradle.kts` only if the test task does not already use JUnit 5
- Create: `dev/src/main/java/net/decimation/fixes/hosting/ModSet.java`, `.../hosting/ListEntry.java`
- Test: `dev/src/test/java/net/decimation/fixes/hosting/ModSetTest.java`, `ListEntryTest.java`

**Interfaces:**
- Produces: `ModSet.of(List<String[]> pairs)`; `ModSet.current()` (this game: FML's active mods minus `mcp`, `FML`, `Forge`, `MinecraftForge`); `String ModSet.firstMissing(ModSet need)` (null when every `[id, version]` of `need` is here with the same version; else `"<id> <version>"`); `List<String[]> pairs()`. `ListEntry` public fields `room, name, host, online, players, max, mods, version, route, since`; `static List<ListEntry> parse(String json)`; `static String cleanName(String raw)` (strips `§x` codes, trims, cuts to 32, empty becomes null).

- [ ] **Step 1: Test setup check**

Add to the `dependencies {}` block of `dev/dependencies.gradle`:

```groovy
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
```

Run: `cd dev && ./gradlew test -q`
Expected: BUILD SUCCESSFUL with no tests. If Gradle reports JUnit 4 style, add to `build.gradle.kts`: `tasks.test { useJUnitPlatform() }`.

- [ ] **Step 2: Write the failing tests**

`ModSetTest.java`:

```java
package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ModSetTest
{
    private static ModSet set(String... idVersion)
    {
        java.util.List<String[]> l = new java.util.ArrayList<String[]>();
        for (int i = 0; i < idVersion.length; i += 2) l.add(new String[] {idVersion[i], idVersion[i + 1]});
        return ModSet.of(l);
    }

    @Test void sameSetMissesNothing()
    {
        assertNull(set("deci", "1.21.10f", "deciworldgen", "0.64.9").firstMissing(set("deciworldgen", "0.64.9", "deci", "1.21.10f")));
    }

    @Test void namesTheFirstMissingMod()
    {
        assertEquals("gvc 0.6.1", set("deci", "1.21.10f").firstMissing(set("deci", "1.21.10f", "gvc", "0.6.1")));
    }

    @Test void anotherVersionCountsAsMissing()
    {
        assertEquals("deciworldgen 0.64.9", set("deciworldgen", "0.64.7").firstMissing(set("deciworldgen", "0.64.9")));
    }

    @Test void pairsKeepOrder()
    {
        assertEquals("gvc", set("deci", "1", "gvc", "2").pairs().get(1)[0]);
    }
}
```

`ListEntryTest.java`:

```java
package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListEntryTest
{
    @Test void parsesTheListAnswer()
    {
        String json = "[{\"room\":\"abcdefghij012345\",\"name\":\"Ucil world\",\"host\":\"Ucil\",\"online\":false,\"players\":2,"
                    + "\"max\":4,\"mods\":[[\"deci\",\"1.21.10f\"]],\"version\":\"0.64.9\",\"route\":\"relay\",\"since\":5}]";
        List<ListEntry> l = ListEntry.parse(json);
        assertEquals(1, l.size());
        assertEquals("Ucil world", l.get(0).name);
        assertEquals(2, l.get(0).players);
        assertEquals("deci", l.get(0).mods.get(0).get(0));
        assertFalse(l.get(0).online);
    }

    @Test void brokenAnswerIsAnEmptyList()
    {
        assertTrue(ListEntry.parse("<html>").isEmpty());
    }

    @Test void nameCleaned()
    {
        assertEquals("Red world", ListEntry.cleanName("  §cRed §lworld  "));
        assertEquals(32, ListEntry.cleanName("x".repeat(40)).length());
        assertNull(ListEntry.cleanName("  §r "));
    }
}
```

(`String.repeat` is Java 11: the tests run on the Gradle JDK; if the test JVM is 8, write `new String(new char[40]).replace('\0', 'x')`.)

- [ ] **Step 3: Run to see them fail**

Run: `cd dev && ./gradlew test -q --tests 'net.decimation.fixes.hosting.*'`
Expected: compile errors (classes missing).

- [ ] **Step 4: Implement**

`ModSet.java`:

```java
package net.decimation.fixes.hosting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The mods a world needs, as [id, version] pairs (docs/superpowers/specs/2026-10-18-player-hosting-part1-design.md 3.3). */
public final class ModSet
{
    private static final java.util.Set<String> BUILT_IN = new java.util.HashSet<String>(
        java.util.Arrays.asList("mcp", "FML", "Forge", "MinecraftForge"));
    private final Map<String, String> mods;

    private ModSet(Map<String, String> mods)
    {
        this.mods = mods;
    }

    public static ModSet of(List<String[]> pairs)
    {
        Map<String, String> m = new LinkedHashMap<String, String>();
        for (String[] p : pairs)
        {
            m.put(p[0], p[1]);
        }
        return new ModSet(m);
    }

    /** This game's active mods without Minecraft's and Forge's own. */
    public static ModSet current()
    {
        List<String[]> l = new ArrayList<String[]>();
        for (cpw.mods.fml.common.ModContainer c : cpw.mods.fml.common.Loader.instance().getActiveModList())
        {
            if (!BUILT_IN.contains(c.getModId()))
            {
                l.add(new String[] {c.getModId(), c.getVersion()});
            }
        }
        return of(l);
    }

    /** "id version" of the first mod of `need` this set lacks or has in another version; null when none. */
    public String firstMissing(ModSet need)
    {
        for (Map.Entry<String, String> e : need.mods.entrySet())
        {
            if (!e.getValue().equals(mods.get(e.getKey())))
            {
                return e.getKey() + " " + e.getValue();
            }
        }
        return null;
    }

    /** This game's DeciRemnant version (deciworldgen's version from mcmod.info, filled from the git tag). */
    public static String ourVersion()
    {
        try
        {
            return cpw.mods.fml.common.Loader.instance().getIndexedModList().get("deciworldgen").getVersion();
        }
        catch (Throwable t)
        {
            return "dev";
        }
    }

    public List<String[]> pairs()
    {
        List<String[]> l = new ArrayList<String[]>();
        for (Map.Entry<String, String> e : mods.entrySet())
        {
            l.add(new String[] {e.getKey(), e.getValue()});
        }
        return Collections.unmodifiableList(l);
    }
}
```

`ListEntry.java`:

```java
package net.decimation.fixes.hosting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One world on the public list, as the relay's GET /list answers it (spec 3.3). */
public final class ListEntry
{
    public String room, name, host, version, route;
    public boolean online;
    public int players, max;
    public long since;
    public List<List<String>> mods = new ArrayList<List<String>>();

    public static List<ListEntry> parse(String json)
    {
        try
        {
            ListEntry[] a = new com.google.gson.Gson().fromJson(json, ListEntry[].class);
            List<ListEntry> l = new ArrayList<ListEntry>();
            for (ListEntry e : a == null ? new ListEntry[0] : a)
            {
                if (e != null && e.room != null && e.name != null)
                {
                    l.add(e);
                }
            }
            return l;
        }
        catch (RuntimeException e)
        {
            return Collections.emptyList();
        }
    }

    /** The mods as a ModSet. */
    public ModSet modSet()
    {
        List<String[]> l = new ArrayList<String[]>();
        for (List<String> m : mods)
        {
            if (m != null && m.size() == 2)
            {
                l.add(new String[] {m.get(0), m.get(1)});
            }
        }
        return ModSet.of(l);
    }

    /** A host's text for the list: formatting codes removed, trimmed, at most 32 characters; null when nothing is left. */
    public static String cleanName(String raw)
    {
        if (raw == null)
        {
            return null;
        }
        String s = raw.replaceAll("§.", "").trim();
        if (s.length() > 32)
        {
            s = s.substring(0, 32).trim();
        }
        return s.isEmpty() ? null : s;
    }
}
```

- [ ] **Step 5: Run**

Run: `cd dev && ./gradlew test -q --tests 'net.decimation.fixes.hosting.*'`
Expected: 7 passed.

- [ ] **Step 6: Commit**

```bash
git add dev/dependencies.gradle dev/build.gradle.kts dev/src/main/java/net/decimation/fixes/hosting dev/src/test
git commit -m "Hosting: mod set and list entry (JUnit tests in dev)"
```

---

### Task 4: RelaySocket (WebSocket client on Minecraft's netty) and the TLS check

**Files:**
- Create: `dev/src/main/java/net/decimation/fixes/hosting/RelaySocket.java`
- Test: `dev/src/test/java/net/decimation/fixes/hosting/RelaySocketTest.java`

**Interfaces:**
- Produces: `RelaySocket.open(URI uri, Listener listener)` returns a connected `RelaySocket` or throws `IOException` (timeout 10 s); `void send(byte[] b, int off, int len)` (coalesced, flushed every 50 ms or at 64 KB); `void sendText(String s)`; `void close(String reason)`; `boolean isOpen()`. `interface Listener { void binary(byte[] data); void text(String s); void closed(int code, String reason); }`. One shared `NioEventLoopGroup` of 2 daemon threads (`RelaySocket.GROUP`).

- [ ] **Step 1: Write the failing test** (an in-process netty WebSocket echo server, no network)

```java
package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import java.net.URI;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;

class RelaySocketTest
{
    static NioEventLoopGroup boss = new NioEventLoopGroup(1);
    static Channel server;
    static final java.util.List<Integer> frameSizes = new CopyOnWriteArrayList<Integer>();

    @BeforeAll static void start() throws Exception
    {
        server = new ServerBootstrap().group(boss).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>()
        {
            protected void initChannel(SocketChannel ch)
            {
                ch.pipeline().addLast(new HttpServerCodec(), new HttpObjectAggregator(65536),
                    new WebSocketServerProtocolHandler("/echo"), new SimpleChannelInboundHandler<WebSocketFrame>()
                    {
                        protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame f)
                        {
                            if (f instanceof BinaryWebSocketFrame)
                            {
                                frameSizes.add(f.content().readableBytes());
                                ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.copiedBuffer(f.content())));
                            }
                            else if (f instanceof TextWebSocketFrame)
                            {
                                ctx.writeAndFlush(new TextWebSocketFrame("echo " + ((TextWebSocketFrame) f).text()));
                            }
                        }
                    });
            }
        }).bind("127.0.0.1", 0).sync().channel();
    }

    @AfterAll static void stop()
    {
        server.close();
        boss.shutdownGracefully();
    }

    private static int port() { return ((java.net.InetSocketAddress) server.localAddress()).getPort(); }

    @Test void textAndBinaryRoundTrip() throws Exception
    {
        BlockingQueue<Object> in = new LinkedBlockingQueue<Object>();
        RelaySocket s = RelaySocket.open(new URI("ws://127.0.0.1:" + port() + "/echo"), new RelaySocket.Listener()
        {
            public void binary(byte[] d) { in.add(d); }
            public void text(String t) { in.add(t); }
            public void closed(int c, String r) { in.add("closed " + c); }
        });
        s.sendText("hi");
        assertEquals("echo hi", in.poll(3, TimeUnit.SECONDS));
        s.send(new byte[] {1, 2, 3}, 0, 3);
        assertArrayEquals(new byte[] {1, 2, 3}, (byte[]) in.poll(3, TimeUnit.SECONDS));
        s.close("done");
    }

    @Test void smallWritesWithinOneTickBecomeOneFrame() throws Exception
    {
        frameSizes.clear();
        RelaySocket s = RelaySocket.open(new URI("ws://127.0.0.1:" + port() + "/echo"), new RelaySocket.Listener()
        {
            public void binary(byte[] d) { }
            public void text(String t) { }
            public void closed(int c, String r) { }
        });
        for (int i = 0; i < 20; i++) s.send(new byte[] {(byte) i}, 0, 1);
        Thread.sleep(200);
        assertEquals(java.util.Collections.singletonList(20), frameSizes);
        s.close("done");
    }

    @Test void refusedConnectionThrows()
    {
        assertThrows(java.io.IOException.class, () -> RelaySocket.open(new URI("ws://127.0.0.1:1/echo"), null));
    }
}
```

- [ ] **Step 2: Run to see it fail**

Run: `cd dev && ./gradlew test -q --tests 'net.decimation.fixes.hosting.RelaySocketTest'`
Expected: compile error (RelaySocket missing). If netty is not on the test classpath, add `testImplementation("io.netty:netty-all:4.0.10.Final")` to dependencies.gradle (the same version Minecraft ships) and rerun: the failure must then be the missing class.

- [ ] **Step 3: Implement**

```java
package net.decimation.fixes.hosting;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslHandler;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.*;

/**
 * A WebSocket to the relay on the netty 4.0.10 Minecraft 1.7.10 ships (spec 3.1): TLS with SNI for wss, binary writes
 * coalesced per 50 ms (one game tick) or 64 KB so Cloudflare counts fewer messages (spec 7).
 */
public final class RelaySocket
{
    public interface Listener
    {
        void binary(byte[] data);
        void text(String s);
        void closed(int code, String reason);
    }

    static final NioEventLoopGroup GROUP = new NioEventLoopGroup(2, new java.util.concurrent.ThreadFactory()
    {
        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(r, "DeciRemnant relay");
            t.setDaemon(true);
            return t;
        }
    });
    private static final int FLUSH_MS = 50, MAX_FRAME = 65536;

    private final Channel channel;
    private final ByteBuf pending = Unpooled.buffer();
    private volatile boolean open = true;

    private RelaySocket(Channel channel)
    {
        this.channel = channel;
        channel.eventLoop().scheduleAtFixedRate(new Runnable()
        {
            public void run()
            {
                flush();
            }
        }, FLUSH_MS, FLUSH_MS, TimeUnit.MILLISECONDS);
    }

    public static RelaySocket open(final URI uri, final Listener listener) throws IOException
    {
        final boolean tls = "wss".equals(uri.getScheme());
        final String host = uri.getHost();
        final int port = uri.getPort() > 0 ? uri.getPort() : tls ? 443 : 80;
        final WebSocketClientHandshaker hs = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false,
                                                                                              new DefaultHttpHeaders(), 1 << 20);
        final ChannelPromise[] done = new ChannelPromise[1];
        Bootstrap b = new Bootstrap().group(GROUP).channel(NioSocketChannel.class)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000).option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>()
            {
                protected void initChannel(SocketChannel ch) throws Exception
                {
                    if (tls)
                    {
                        SSLEngine e = SSLContext.getDefault().createSSLEngine(host, port);
                        e.setUseClientMode(true);
                        SSLParameters p = e.getSSLParameters();
                        p.setServerNames(Collections.<SNIServerName>singletonList(new SNIHostName(host)));
                        e.setSSLParameters(p);
                        ch.pipeline().addLast(new SslHandler(e));
                    }
                    ch.pipeline().addLast(new HttpClientCodec(), new HttpObjectAggregator(8192), new SimpleChannelInboundHandler<Object>()
                    {
                        public void channelActive(ChannelHandlerContext ctx)
                        {
                            done[0] = ctx.newPromise();
                            hs.handshake(ctx.channel());
                        }

                        protected void channelRead0(ChannelHandlerContext ctx, Object msg)
                        {
                            if (!hs.isHandshakeComplete())
                            {
                                hs.finishHandshake(ctx.channel(), (FullHttpResponse) msg);
                                done[0].setSuccess();
                                return;
                            }
                            if (msg instanceof BinaryWebSocketFrame && listener != null)
                            {
                                ByteBuf c = ((BinaryWebSocketFrame) msg).content();
                                byte[] d = new byte[c.readableBytes()];
                                c.readBytes(d);
                                listener.binary(d);
                            }
                            else if (msg instanceof TextWebSocketFrame && listener != null)
                            {
                                listener.text(((TextWebSocketFrame) msg).text());
                            }
                            else if (msg instanceof CloseWebSocketFrame)
                            {
                                CloseWebSocketFrame c = (CloseWebSocketFrame) msg;
                                if (listener != null)
                                {
                                    listener.closed(c.statusCode(), c.reasonText());
                                }
                                ctx.close();
                            }
                        }

                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable t)
                        {
                            if (done[0] != null && !done[0].isDone())
                            {
                                done[0].setFailure(t);
                            }
                            ctx.close();
                        }
                    });
                }
            });
        ChannelFuture connect = b.connect(host, port);
        if (!connect.awaitUninterruptibly(10, TimeUnit.SECONDS) || !connect.isSuccess())
        {
            throw new IOException("relay not reachable: " + connect.cause());
        }
        long end = System.currentTimeMillis() + 10000;
        while (done[0] == null && System.currentTimeMillis() < end)
        {
            Thread.yield();
        }
        if (done[0] == null || !done[0].awaitUninterruptibly(Math.max(1, end - System.currentTimeMillis())) || !done[0].isSuccess())
        {
            connect.channel().close();
            throw new IOException("relay handshake failed" + (done[0] == null ? "" : ": " + done[0].cause()));
        }
        final RelaySocket s = new RelaySocket(connect.channel());
        connect.channel().closeFuture().addListener(new ChannelFutureListener()
        {
            public void operationComplete(ChannelFuture f)
            {
                if (s.open && listener != null)
                {
                    s.open = false;
                    listener.closed(1006, "connection lost");
                }
                s.open = false;
            }
        });
        return s;
    }

    public void send(byte[] b, int off, int len)
    {
        synchronized (pending)
        {
            pending.writeBytes(b, off, len);
            if (pending.readableBytes() < MAX_FRAME)
            {
                return;
            }
        }
        flush();
    }

    private void flush()
    {
        ByteBuf out;
        synchronized (pending)
        {
            if (pending.readableBytes() == 0)
            {
                return;
            }
            out = Unpooled.copiedBuffer(pending);
            pending.clear();
        }
        channel.writeAndFlush(new BinaryWebSocketFrame(out));
    }

    public void sendText(String s)
    {
        flush();
        channel.writeAndFlush(new TextWebSocketFrame(s));
    }

    public boolean isOpen()
    {
        return open && channel.isActive();
    }

    public void close(String reason)
    {
        flush();
        open = false;
        channel.writeAndFlush(new CloseWebSocketFrame(1000, reason)).addListener(ChannelFutureListener.CLOSE);
    }
}
```

- [ ] **Step 4: Run**

Run: `cd dev && ./gradlew test -q --tests 'net.decimation.fixes.hosting.RelaySocketTest'`
Expected: 3 passed. If `WebSocketServerProtocolHandler` or a factory overload is missing in netty 4.0.10, the compiler says so: use the overload it lists and record the ruling in the plan ledger.

- [ ] **Step 5: TLS check on Java 8 (spec risk 1)**

Add a `main` to the test sources, `dev/src/test/java/net/decimation/fixes/hosting/TlsCheck.java`:

```java
package net.decimation.fixes.hosting;

public final class TlsCheck
{
    public static void main(String[] a) throws Exception
    {
        final java.util.concurrent.CountDownLatch got = new java.util.concurrent.CountDownLatch(1);
        RelaySocket s = RelaySocket.open(new java.net.URI(a[0]), new RelaySocket.Listener()
        {
            public void binary(byte[] d) { System.out.println("binary " + d.length); got.countDown(); }
            public void text(String t) { System.out.println("text " + t); got.countDown(); }
            public void closed(int c, String r) { System.out.println("closed " + c + " " + r); }
        });
        s.sendText("ping from Java " + System.getProperty("java.version"));
        System.out.println(got.await(10, java.util.concurrent.TimeUnit.SECONDS) ? "TLS OK" : "NO ANSWER");
        s.close("done");
    }
}
```

Run it with the game's Java (classpath from Gradle's test runtime):

```bash
cd dev && ./gradlew -q testClasses && CP=$(./gradlew -q printTestClasspath 2>/dev/null)
```

If no such task exists, add to `build.gradle.kts`: `tasks.register("printTestClasspath") { doLast { println(sourceSets["test"].runtimeClasspath.asPath) } }`. Then:

```bash
~/.jdks/zulu-8.jdk/Contents/Home/bin/java -cp "$CP" net.decimation.fixes.hosting.TlsCheck wss://ws.postman-echo.com/raw
```

Expected: `text ping from Java 1.8...` then `TLS OK`. Write the result (Java version, OK or the exception) into docs/player_hosting.md section 11 ("Build notes"). Mojang's bundled Java 8u51 is not on this Mac: record it as [not verified] there.

- [ ] **Step 6: Commit**

```bash
git add dev/src/main/java/net/decimation/fixes/hosting/RelaySocket.java dev/src/test docs/player_hosting.md dev/build.gradle.kts dev/dependencies.gradle
git commit -m "Hosting: relay WebSocket on Minecraft's netty with TLS and tick coalesced writes"
```

---

### Task 5: RelayClient (the list calls)

**Files:**
- Create: `dev/src/main/java/net/decimation/fixes/hosting/RelayClient.java`
- Test: `dev/src/test/java/net/decimation/fixes/hosting/RelayClientTest.java`

**Interfaces:**
- Consumes: `ListEntry.parse`, `ModSet.pairs()`.
- Produces: `new RelayClient(String base)` (base like `https://deciremnant-relay.<account>.workers.dev` or `http://127.0.0.1:8787`); `String[] host(String name, String player, boolean online, int max, ModSet mods, String version)` returns `{room, secret}`; `boolean beat(String room, String secret, int players)` (false = gone); `void stop(String room, String secret)`; `List<ListEntry> list()`; `URI hostSocket(room, secret)`, `URI dataSocket(room, secret, id)`, `URI joinSocket(room)`; exceptions `RelayClient.RelayFull` (status 429 from a quota, or Cloudflare errors 1015 / 1027 / any 5xx) and `IOException` otherwise. `static String base()`: system property `deciworldgen.relay`, else the deployed address written in Task 11.

- [ ] **Step 1: Write the failing tests** (a JDK HttpServer as the relay)

```java
package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.*;

class RelayClientTest
{
    HttpServer http;
    volatile int status = 200;
    volatile String answer = "{}";
    volatile String lastBody = "";

    @BeforeEach void start() throws Exception
    {
        http = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        http.createContext("/", ex -> {
            lastBody = new String(readAll(ex.getRequestBody()), StandardCharsets.UTF_8);
            byte[] b = answer.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(status, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
        http.start();
    }

    static byte[] readAll(java.io.InputStream in) throws java.io.IOException
    {
        java.io.ByteArrayOutputStream o = new java.io.ByteArrayOutputStream();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) > 0; ) o.write(b, 0, n);
        return o.toByteArray();
    }

    @AfterEach void stop() { http.stop(0); }

    RelayClient client() { return new RelayClient("http://127.0.0.1:" + http.getAddress().getPort()); }

    @Test void hostSendsTheEntryAndReturnsRoomAndSecret() throws Exception
    {
        answer = "{\"room\":\"r0\",\"secret\":\"s0\"}";
        String[] rs = client().host("Ucil world", "Ucil", false, 4,
            ModSet.of(Collections.singletonList(new String[] {"deci", "1.21.10f"})), "0.64.9");
        assertArrayEquals(new String[] {"r0", "s0"}, rs);
        assertTrue(lastBody.contains("\"name\":\"Ucil world\"") && lastBody.contains("[\"deci\",\"1.21.10f\"]"));
    }

    @Test void goneBeatIsFalse() throws Exception
    {
        answer = "{\"gone\":true}";
        assertFalse(client().beat("r0", "s0", 1));
        answer = "{\"ok\":true}";
        assertTrue(client().beat("r0", "s0", 1));
    }

    @Test void quotaMapsToRelayFull()
    {
        status = 429;
        answer = "error code: 1027";
        assertThrows(RelayClient.RelayFull.class, () -> client().list());
        status = 503;
        assertThrows(RelayClient.RelayFull.class, () -> client().list());
    }

    @Test void socketAddressesUseWsSchemes()
    {
        assertEquals("ws://127.0.0.1:" + http.getAddress().getPort() + "/join/r0", client().joinSocket("r0").toString());
        assertTrue(new RelayClient("https://x.workers.dev").hostSocket("r0", "s0").toString().startsWith("wss://x.workers.dev/host/r0?secret="));
    }
}
```

- [ ] **Step 2: Run to see it fail**

Run: `cd dev && ./gradlew test -q --tests 'net.decimation.fixes.hosting.RelayClientTest'`
Expected: compile error (RelayClient missing).

- [ ] **Step 3: Implement**

```java
package net.decimation.fixes.hosting;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** The list calls of the relay (spec 3.2) over HTTPS; RelayFull when Cloudflare's free daily limit is reached (spec 5). */
public final class RelayClient
{
    /** The deployed relay (Task 11 writes the real address here). */
    public static final String DEPLOYED = "https://deciremnant-relay.invalid";

    public static final class RelayFull extends IOException
    {
        RelayFull(String m) { super(m); }
    }

    private final String base;

    public RelayClient(String base)
    {
        this.base = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    public static String base()
    {
        return System.getProperty("deciworldgen.relay", DEPLOYED);
    }

    public String[] host(String name, String player, boolean online, int max, ModSet mods, String version) throws IOException
    {
        Map<String, Object> m = new java.util.LinkedHashMap<String, Object>();
        m.put("name", name);
        m.put("host", player);
        m.put("online", online);
        m.put("max", max);
        m.put("mods", mods.pairs());
        m.put("version", version);
        JsonObject o = new Gson().fromJson(call("POST", "/list/host", new Gson().toJson(m)), JsonObject.class);
        return new String[] {o.get("room").getAsString(), o.get("secret").getAsString()};
    }

    public boolean beat(String room, String secret, int players) throws IOException
    {
        String a = call("POST", "/list/beat", "{\"room\":\"" + room + "\",\"secret\":\"" + secret + "\",\"players\":" + players + "}");
        return !a.contains("\"gone\"");
    }

    public void stop(String room, String secret) throws IOException
    {
        call("POST", "/list/stop", "{\"room\":\"" + room + "\",\"secret\":\"" + secret + "\"}");
    }

    public List<ListEntry> list() throws IOException
    {
        return ListEntry.parse(call("GET", "/list", null));
    }

    private String ws()
    {
        return base.startsWith("https") ? "wss" + base.substring(5) : "ws" + base.substring(4);
    }

    public URI hostSocket(String room, String secret) { return URI.create(ws() + "/host/" + room + "?secret=" + secret); }
    public URI dataSocket(String room, String secret, String id) { return URI.create(ws() + "/host/" + room + "/data/" + id + "?secret=" + secret); }
    public URI joinSocket(String room) { return URI.create(ws() + "/join/" + room); }

    private String call(String method, String path, String body) throws IOException
    {
        HttpURLConnection c = (HttpURLConnection) new URL(base + path).openConnection();
        c.setConnectTimeout(10000);
        c.setReadTimeout(10000);
        c.setRequestMethod(method);
        c.setRequestProperty("User-Agent", "DeciRemnant");
        if (body != null)
        {
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/json");
            OutputStream o = c.getOutputStream();
            o.write(body.getBytes(StandardCharsets.UTF_8));
            o.close();
        }
        int status = c.getResponseCode();
        InputStream in = status < 400 ? c.getInputStream() : c.getErrorStream();
        String text = in == null ? "" : read(in);
        if (status == 429 && !text.contains("too often") || status >= 500 || text.contains("error code: 1027") || text.contains("error code: 1015"))
        {
            throw new RelayFull(status + " " + text);
        }
        if (status >= 400)
        {
            throw new IOException(status + " " + text);
        }
        return text;
    }

    private static String read(InputStream in) throws IOException
    {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) > 0; )
        {
            o.write(b, 0, n);
        }
        in.close();
        return new String(o.toByteArray(), StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 4: Run**

Run: `cd dev && ./gradlew test -q --tests 'net.decimation.fixes.hosting.*'`
Expected: all hosting tests pass (7 + 3 + 4).

- [ ] **Step 5: Commit**

```bash
git add dev/src/main/java/net/decimation/fixes/hosting/RelayClient.java dev/src/test
git commit -m "Hosting: relay list client with the free limit mapped to RelayFull"
```

---

### Task 6: Pipe (one TCP socket to one relay socket)

**Files:**
- Create: `dev/src/main/java/net/decimation/fixes/hosting/Pipe.java`
- Test: `dev/src/test/java/net/decimation/fixes/hosting/PipeTest.java`

**Interfaces:**
- Consumes: `RelaySocket` (Task 4).
- Produces: `Pipe.start(Socket tcp, URI relay, boolean waitReady, Runnable onClose)` returns a `Pipe` (throws `IOException` when the relay socket cannot open); `void close()`. With `waitReady` the TCP side is read only after the relay sent text `{"ready":true}` (the joiner side, spec Room protocol); the host side starts at once.

- [ ] **Step 1: Write the failing test** (reuse the echo server of RelaySocketTest by moving it to a test helper `EchoServer` with `static int start()` and `static void stop()`; the relay echoes, so bytes written to the TCP socket come back)

```java
package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.net.*;
import org.junit.jupiter.api.*;

class PipeTest
{
    static int port;

    @BeforeAll static void up() throws Exception { port = EchoServer.start(); }
    @AfterAll static void down() { EchoServer.stop(); }

    @Test void bytesGoThroughTheRelayAndBack() throws Exception
    {
        ServerSocket local = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        Socket client = new Socket(InetAddress.getLoopbackAddress(), local.getLocalPort());
        Socket accepted = local.accept();
        final boolean[] closed = {false};
        Pipe p = Pipe.start(accepted, URI.create("ws://127.0.0.1:" + port + "/echo"), false, () -> closed[0] = true);
        client.getOutputStream().write(new byte[] {7, 8, 9});
        client.setSoTimeout(3000);
        byte[] back = new byte[3];
        new DataInputStream(client.getInputStream()).readFully(back);
        assertArrayEquals(new byte[] {7, 8, 9}, back);
        client.close();
        Thread.sleep(300);
        assertTrue(closed[0]);
        local.close();
    }
}
```

- [ ] **Step 2: Run to see it fail**

Run: `cd dev && ./gradlew test -q --tests 'net.decimation.fixes.hosting.PipeTest'`
Expected: compile error (Pipe, EchoServer missing).

- [ ] **Step 3: Implement**

`EchoServer.java` (test sources): move the server code of `RelaySocketTest.start/stop` into `static int start()` (returns the port) and `static void stop()`, and make RelaySocketTest call them.

`Pipe.java`:

```java
package net.decimation.fixes.hosting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

/** One TCP connection carried over one relay WebSocket, both ways (host side: LAN port; join side: Minecraft's connection). */
public final class Pipe
{
    private final Socket tcp;
    private RelaySocket relay;
    private final CountDownLatch ready = new CountDownLatch(1);
    private final Runnable onClose;
    private volatile boolean closed;

    private Pipe(Socket tcp, Runnable onClose)
    {
        this.tcp = tcp;
        this.onClose = onClose;
    }

    public static Pipe start(Socket tcp, URI relayUri, boolean waitReady, Runnable onClose) throws IOException
    {
        final Pipe p = new Pipe(tcp, onClose);
        final OutputStream out = tcp.getOutputStream();
        p.relay = RelaySocket.open(relayUri, new RelaySocket.Listener()
        {
            public void binary(byte[] d)
            {
                try
                {
                    out.write(d);
                    out.flush();
                }
                catch (IOException e)
                {
                    p.close();
                }
            }

            public void text(String s)
            {
                if (s.contains("\"ready\""))
                {
                    p.ready.countDown();
                }
            }

            public void closed(int code, String reason)
            {
                p.close();
            }
        });
        if (!waitReady)
        {
            p.ready.countDown();
        }
        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                p.copy();
            }
        }, "DeciRemnant pipe");
        t.setDaemon(true);
        t.start();
        return p;
    }

    private void copy()
    {
        try
        {
            if (!ready.await(15, java.util.concurrent.TimeUnit.SECONDS))
            {
                close();
                return;
            }
            InputStream in = tcp.getInputStream();
            byte[] b = new byte[16384];
            for (int n; (n = in.read(b)) > 0; )
            {
                relay.send(b, 0, n);
            }
        }
        catch (Exception e)
        {
            // socket closed
        }
        close();
    }

    public void close()
    {
        if (closed)
        {
            return;
        }
        closed = true;
        try
        {
            tcp.close();
        }
        catch (IOException e)
        {
            // already closed
        }
        if (relay != null && relay.isOpen())
        {
            relay.close("pipe closed");
        }
        if (onClose != null)
        {
            onClose.run();
        }
    }
}
```

- [ ] **Step 4: Run**

Run: `cd dev && ./gradlew test -q --tests 'net.decimation.fixes.hosting.*'`
Expected: all pass.

- [ ] **Step 5: Commit**

```bash
git add dev/src/main/java/net/decimation/fixes/hosting/Pipe.java dev/src/test
git commit -m "Hosting: pipe one TCP connection over one relay socket"
```

---

### Task 7: HostService

**Files:**
- Create: `dev/src/main/java/net/decimation/fixes/hosting/HostService.java`
- Test: `dev/src/test/java/net/decimation/fixes/hosting/HostServiceTest.java` (the relay side faked with RelayClientTest's HttpServer style and EchoServer; the LAN side is a plain ServerSocket)

**Interfaces:**
- Consumes: `RelayClient`, `RelaySocket`, `Pipe`, `ModSet`, `ListEntry.cleanName`.
- Produces: `HostService.INSTANCE`; `enum State { OFF, STARTING, ONLINE, FULL_TODAY, FAILED }`; `void start(Settings s, int lanPort, Players players)` (call from the client thread after the world is open to LAN); `void stop()`; `State state()`; `String error()`; `int joiners()`; `Settings` (public fields `String name; boolean online; int max; String player; String version`); `interface Players { int count(); }` (the server's player count minus the host); `void tick(long nowMillis)` heartbeat clock (called from the client tick; beats every 60 s; `gone` re-registers with the same settings); `@SubscribeEvent onUnload(WorldEvent.Unload)` stops hosting.

- [ ] **Step 1: Write the failing tests**

```java
package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;

class HostServiceTest
{
    HttpServer http;
    final AtomicInteger hosts = new AtomicInteger(), beats = new AtomicInteger(), stops = new AtomicInteger();
    volatile boolean gone;

    @BeforeEach void up() throws Exception
    {
        http = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        http.createContext("/", ex -> {
            String p = ex.getRequestURI().getPath();
            String a = "{}";
            if (p.equals("/list/host")) { hosts.incrementAndGet(); a = "{\"room\":\"r" + hosts.get() + "\",\"secret\":\"s\"}"; }
            if (p.equals("/list/beat")) { beats.incrementAndGet(); a = gone ? "{\"gone\":true}" : "{\"ok\":true}"; }
            if (p.equals("/list/stop")) { stops.incrementAndGet(); a = "{\"ok\":true}"; }
            byte[] b = a.getBytes();
            ex.sendResponseHeaders(200, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
        http.start();
        System.setProperty("deciworldgen.relay", "http://127.0.0.1:" + http.getAddress().getPort());
        HostService.INSTANCE.socketsForTests(false); // no control socket: the fake relay has no WebSocket
    }

    @AfterEach void down() { HostService.INSTANCE.stop(); http.stop(0); }

    HostService.Settings settings()
    {
        HostService.Settings s = new HostService.Settings();
        s.name = "Ucil world"; s.online = false; s.max = 4; s.player = "Ucil"; s.version = "0.64.9";
        return s;
    }

    @Test void startRegistersAndBeatsEveryMinute()
    {
        HostService.INSTANCE.start(settings(), 25565, () -> 1);
        assertEquals(HostService.State.ONLINE, HostService.INSTANCE.state());
        assertEquals(1, hosts.get());
        HostService.INSTANCE.tick(System.currentTimeMillis() + 61_000);
        assertEquals(1, beats.get());
        HostService.INSTANCE.tick(System.currentTimeMillis() + 62_000);
        assertEquals(1, beats.get(), "only one beat a minute");
    }

    @Test void goneReRegisters()
    {
        HostService.INSTANCE.start(settings(), 25565, () -> 0);
        gone = true;
        HostService.INSTANCE.tick(System.currentTimeMillis() + 61_000);
        assertEquals(2, hosts.get());
        assertEquals(HostService.State.ONLINE, HostService.INSTANCE.state());
    }

    @Test void stopRemovesTheEntryAtOnce()
    {
        HostService.INSTANCE.start(settings(), 25565, () -> 0);
        HostService.INSTANCE.stop();
        assertEquals(1, stops.get());
        assertEquals(HostService.State.OFF, HostService.INSTANCE.state());
    }

    @Test void worldUnloadStopsHosting()
    {
        HostService.INSTANCE.start(settings(), 25565, () -> 0);
        HostService.INSTANCE.worldClosed();
        assertEquals(1, stops.get());
        assertEquals(HostService.State.OFF, HostService.INSTANCE.state());
    }
}
```

- [ ] **Step 2: Run to see it fail**

Run: `cd dev && ./gradlew test -q --tests 'net.decimation.fixes.hosting.HostServiceTest'`
Expected: compile error (HostService missing).

- [ ] **Step 3: Implement**

```java
package net.decimation.fixes.hosting;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The host side (spec 4.1): registers the world on the list, beats every 60 s (a "gone" answer registers again with
 * the same settings), holds the control socket, opens one data socket per joiner piped to the LAN port, stops on
 * request or when the world closes.
 */
public final class HostService
{
    public static final HostService INSTANCE = new HostService();
    private static final long BEAT_MS = 60_000;

    public enum State { OFF, STARTING, ONLINE, FULL_TODAY, FAILED }

    public static final class Settings
    {
        public String name, player, version;
        public boolean online;
        public int max = 4;
    }

    public interface Players
    {
        int count();
    }

    private volatile State state = State.OFF;
    private volatile String error = "";
    private Settings settings;
    private Players players;
    private int lanPort;
    private String room, secret;
    private long lastBeat;
    private RelaySocket control;
    private boolean sockets = true;
    private final Map<String, Pipe> pipes = new ConcurrentHashMap<String, Pipe>();

    private HostService()
    {
    }

    void socketsForTests(boolean on)
    {
        sockets = on;
    }

    public State state() { return state; }
    public String error() { return error; }
    public int joiners() { return pipes.size(); }
    public Settings settings() { return settings; }

    public synchronized void start(Settings s, int lanPort, Players players)
    {
        this.settings = s;
        this.players = players;
        this.lanPort = lanPort;
        state = State.STARTING;
        register();
    }

    private void register()
    {
        RelayClient c = new RelayClient(RelayClient.base());
        try
        {
            String[] rs = c.host(settings.name, settings.player, settings.online, settings.max, ModSet.current0(), settings.version);
            room = rs[0];
            secret = rs[1];
            lastBeat = System.currentTimeMillis();
            if (sockets)
            {
                openControl(c);
            }
            state = State.ONLINE;
        }
        catch (RelayClient.RelayFull e)
        {
            state = State.FULL_TODAY;
            error = e.getMessage();
        }
        catch (IOException e)
        {
            state = State.FAILED;
            error = e.getMessage();
        }
    }

    private void openControl(final RelayClient c) throws IOException
    {
        control = RelaySocket.open(c.hostSocket(room, secret), new RelaySocket.Listener()
        {
            public void binary(byte[] d)
            {
            }

            public void text(String s)
            {
                com.google.gson.JsonObject o = new com.google.gson.Gson().fromJson(s, com.google.gson.JsonObject.class);
                if (o.has("open"))
                {
                    openData(c, o.get("open").getAsString());
                }
                else if (o.has("close"))
                {
                    Pipe p = pipes.remove(o.get("close").getAsString());
                    if (p != null)
                    {
                        p.close();
                    }
                }
            }

            public void closed(int code, String reason)
            {
                if (state == State.ONLINE)
                {
                    state = State.FAILED;
                    error = "relay closed: " + code + " " + reason;
                }
            }
        });
    }

    private void openData(RelayClient c, final String id)
    {
        try
        {
            Socket lan = new Socket(InetAddress.getLoopbackAddress(), lanPort);
            pipes.put(id, Pipe.start(lan, c.dataSocket(room, secret, id), false, new Runnable()
            {
                public void run()
                {
                    pipes.remove(id);
                }
            }));
        }
        catch (IOException e)
        {
            error = "joiner " + id + ": " + e.getMessage();
        }
    }

    /** The heartbeat clock; call it from the client tick. */
    public synchronized void tick(long now)
    {
        if (state != State.ONLINE || now - lastBeat < BEAT_MS)
        {
            return;
        }
        lastBeat = now;
        try
        {
            if (!new RelayClient(RelayClient.base()).beat(room, secret, players.count()))
            {
                closeSockets();
                register();
            }
        }
        catch (RelayClient.RelayFull e)
        {
            state = State.FULL_TODAY;
            error = e.getMessage();
        }
        catch (IOException e)
        {
            error = e.getMessage(); // a missed beat: the next one tries again; after 3 min the list answers gone
        }
    }

    public synchronized void stop()
    {
        if (state == State.OFF)
        {
            return;
        }
        if (room != null)
        {
            try
            {
                new RelayClient(RelayClient.base()).stop(room, secret);
            }
            catch (IOException e)
            {
                // the entry goes after 3 min anyway
            }
        }
        closeSockets();
        room = null;
        secret = null;
        state = State.OFF;
    }

    private void closeSockets()
    {
        for (Pipe p : pipes.values())
        {
            p.close();
        }
        pipes.clear();
        if (control != null && control.isOpen())
        {
            control.close("host stopped");
        }
        control = null;
    }

    /** The world closed (leaving it, quitting): stop hosting at once. */
    public void worldClosed()
    {
        stop();
    }

    @SubscribeEvent
    public void onUnload(net.minecraftforge.event.world.WorldEvent.Unload e)
    {
        if (!e.world.isRemote && e.world.provider.dimensionId == 0)
        {
            worldClosed();
        }
    }
}
```

In `ModSet.java` add (so tests run without FML's Loader):

```java
    /** ModSet.current() in a game; an empty set outside one (unit tests). */
    static ModSet current0()
    {
        try
        {
            return current();
        }
        catch (Throwable t)
        {
            return of(new java.util.ArrayList<String[]>());
        }
    }
```

- [ ] **Step 4: Run**

Run: `cd dev && ./gradlew test -q --tests 'net.decimation.fixes.hosting.*'`
Expected: all pass.

- [ ] **Step 5: Commit**

```bash
git add dev/src/main/java/net/decimation/fixes/hosting dev/src/test
git commit -m "Hosting: host service (register, heartbeat, re-register when gone, stop on world close)"
```

---

### Task 8: JoinService

**Files:**
- Create: `dev/src/main/java/net/decimation/fixes/hosting/JoinService.java`
- Test: `dev/src/test/java/net/decimation/fixes/hosting/JoinServiceTest.java`

**Interfaces:**
- Consumes: `RelayClient.joinSocket`, `Pipe` (waitReady true), `ModSet`, `ListEntry`.
- Produces: `JoinService.INSTANCE`; `int listen(ListEntry e, ModSet mine)` returns a 127.0.0.1 port whose connections go to the world's room (or, when `mine.firstMissing(e.modSet())` is not null or the version differs, a port that closes each connection and reports the refusal); `void closeAll()`; `int openPorts()`; `String lastRefusal()` (the toast text, then cleared); `interface Toasts { void show(String line); }` set by `setToasts` (the game shows Decimation toasts; tests collect lines).

- [ ] **Step 1: Write the failing tests**

```java
package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;
import java.net.*;
import java.util.*;
import org.junit.jupiter.api.*;

class JoinServiceTest
{
    static int echo;

    @BeforeAll static void up() throws Exception { echo = EchoServer.start(); System.setProperty("deciworldgen.relay", "http://127.0.0.1:" + echo); }
    @AfterAll static void down() { JoinService.INSTANCE.closeAll(); EchoServer.stop(); }

    static ListEntry entry(String... mods)
    {
        ListEntry e = new ListEntry();
        e.room = "echo";
        e.name = "Ucil world";
        e.version = "0.64.9";
        for (int i = 0; i < mods.length; i += 2) e.mods.add(Arrays.asList(mods[i], mods[i + 1]));
        return e;
    }

    @Test void missingModIsRefusedWithItsName() throws Exception
    {
        List<String> toasts = new ArrayList<String>();
        JoinService.INSTANCE.setToasts(toasts::add);
        int port = JoinService.INSTANCE.listen(entry("gvc", "0.6.1"), ModSet.of(Collections.<String[]>emptyList()));
        Socket s = new Socket(InetAddress.getLoopbackAddress(), port);
        s.setSoTimeout(2000);
        assertEquals(-1, s.getInputStream().read());
        assertEquals(Collections.singletonList("You need gvc 0.6.1 to join Ucil world"), toasts);
    }

    @Test void closeFreesThePorts() throws Exception
    {
        JoinService.INSTANCE.listen(entry(), ModSet.of(Collections.<String[]>emptyList()));
        assertTrue(JoinService.INSTANCE.openPorts() > 0);
        JoinService.INSTANCE.closeAll();
        assertEquals(0, JoinService.INSTANCE.openPorts());
    }
}
```

(The echo server answers `/join/echo` like any path it accepts: change `EchoServer`'s `WebSocketServerProtocolHandler("/echo")` to accept any path by checking the request in a small handler, or register the path `/join/echo` as well.)

- [ ] **Step 2: Run to see it fail**

Run: `cd dev && ./gradlew test -q --tests 'net.decimation.fixes.hosting.JoinServiceTest'`
Expected: compile error (JoinService missing).

- [ ] **Step 3: Implement**

```java
package net.decimation.fixes.hosting;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The join side (spec 4.2): one 127.0.0.1 port per listed world (Decimation's Community rows ping and join it), each
 * connection carried to the world's relay room; a world whose mods this game lacks gets a port that refuses with a toast.
 */
public final class JoinService
{
    public static final JoinService INSTANCE = new JoinService();

    public interface Toasts
    {
        void show(String line);
    }

    private final List<ServerSocket> listeners = new CopyOnWriteArrayList<ServerSocket>();
    private volatile Toasts toasts = new Toasts()
    {
        public void show(String line)
        {
        }
    };

    private JoinService()
    {
    }

    public void setToasts(Toasts t)
    {
        toasts = t;
    }

    public int openPorts()
    {
        return listeners.size();
    }

    public int listen(final ListEntry e, ModSet mine) throws IOException
    {
        final String missing = mine.firstMissing(e.modSet());
        final ServerSocket ss = new ServerSocket(0, 8, InetAddress.getLoopbackAddress());
        listeners.add(ss);
        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                while (!ss.isClosed())
                {
                    try
                    {
                        final Socket s = ss.accept();
                        if (missing != null)
                        {
                            s.close();
                            toasts.show("You need " + missing + " to join " + e.name);
                            continue;
                        }
                        Pipe.start(s, new RelayClient(RelayClient.base()).joinSocket(e.room), true, null);
                    }
                    catch (IOException ex)
                    {
                        if (!ss.isClosed())
                        {
                            // the relay refuses the upgrade: 409 full, 404 not listed or no host (spec 5)
                            String m = String.valueOf(ex.getMessage());
                            toasts.show(m.contains("409") ? "This world is full (" + e.max + "/" + e.max + ")" : "The host stopped hosting");
                        }
                    }
                }
            }
        }, "DeciRemnant join " + e.room);
        t.setDaemon(true);
        t.start();
        return ss.getLocalPort();
    }

    public void closeAll()
    {
        for (ServerSocket s : listeners)
        {
            try
            {
                s.close();
            }
            catch (IOException e)
            {
                // closed
            }
        }
        listeners.clear();
    }
}
```

(The relay refuses a full room with HTTP 409 and a missing host with 404 before the WebSocket opens, so the handshake error carries the status; netty's text is "Invalid handshake response getStatus: 409 Conflict" [inferred from netty 4.0 source; the Task 10 dev test shows the real text]. A host that stops while a joiner plays closes the joiner's socket with 4410: Minecraft shows its own lost connection screen.)

- [ ] **Step 4: Run**

Run: `cd dev && ./gradlew test -q --tests 'net.decimation.fixes.hosting.*'`
Expected: all pass.

- [ ] **Step 5: Commit**

```bash
git add dev/src/main/java/net/decimation/fixes/hosting/JoinService.java dev/src/test
git commit -m "Hosting: join service (a local port per world, missing mods refused by name)"
```

---

### Task 9: Pause menu button and the Host Online page

**Files:**
- Create: `dev/src/main/java/net/decimation/fixes/hosting/HostMenu.java`, `.../hosting/HostScreen.java`
- Modify: `dev/src/main/java/net/decimation/fixes/Deci.java` (add `toast`), `dev/src/main/java/net/decimation/worldgen/DecimationWorldGen.java` (register), `dev/src/main/resources/assets/deciworldgen/lang/en_US.lang`
- Test: dev test `hostmenu` (new `dev/src/main/java/net/decimation/worldgen/devtest/HostMenuTest.java`, registered in `DevAutoTest.mode()`)

**Interfaces:**
- Consumes: `HostService` (Task 7), `ListEntry.cleanName`, `Deci.homeMenu`, `Deci.isHomeMenu`, `Deci.menuButton`.
- Produces: `Deci.toast(String line)` (`deci.b.b.b(String)`, ToastNotification.show, srg line 5121); `HostMenu.ID_HOST = 9300`; Decimation's pause menu (GuiMenuHome in a world): button 11 (Server Status) replaced by ours at its place, green while hosting; buttons 25 (the LAN globe) and ours open `HostScreen`; `HostScreen` with a name field, the online / offline pair, max players minus / plus, the mods line, the connection line, Start hosting / Stop hosting, Back; the players list while hosting.

- [ ] **Step 1: Write the failing dev test** `HostMenuTest`:

```java
package net.decimation.worldgen.devtest;

import net.decimation.fixes.Deci;
import net.decimation.fixes.hosting.HostMenu;
import net.decimation.fixes.hosting.HostScreen;
import net.decimation.worldgen.DevAutoTest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/** The pause menu shows Host Online in Server Status' place; it and the LAN globe open the Host Online page. */
public class HostMenuTest extends DevTestMode
{
    public String name() { return "hostmenu"; }

    private int step, wait;

    public boolean client(Minecraft mc)
    {
        DevAutoTest.keepScreen = true;
        wait++;
        switch (step)
        {
            case 0:
                mc.displayGuiScreen(Deci.homeMenu());
                step = 1;
                wait = 0;
                return true;
            case 1:
                if (wait < 10) return true;
                GuiButton host = DevTestUtil.byId(mc.currentScreen, HostMenu.ID_HOST);
                DevTestResults.check(name(), "Host Online button in the pause menu", host == null ? "none" : DevTestUtil.label(host),
                                     host != null && DevTestUtil.label(host).contains("Host Online"), "Host Online >>");
                DevTestResults.check(name(), "Server Status gone from the pause menu", DevTestUtil.byId(mc.currentScreen, 11) == null,
                                     DevTestUtil.byId(mc.currentScreen, 11) == null, "true");
                if (host != null) DevTestUtil.press(mc.currentScreen, host);
                step = 2;
                wait = 0;
                return true;
            case 2:
                if (wait < 10) return true;
                DevTestResults.check(name(), "Host Online opens its page", mc.currentScreen == null ? "none" : mc.currentScreen.getClass().getSimpleName(),
                                     mc.currentScreen instanceof HostScreen, "HostScreen");
                DevTestUtil.screenshot(mc, name(), "host_page");
                mc.displayGuiScreen(Deci.homeMenu());
                step = 3;
                wait = 0;
                return true;
            case 3:
                if (wait < 10) return true;
                GuiButton lan = DevTestUtil.byId(mc.currentScreen, 25);
                if (lan != null) DevTestUtil.press(mc.currentScreen, lan);
                step = 4;
                wait = 0;
                return true;
            case 4:
                if (wait < 10) return true;
                DevTestResults.check(name(), "the LAN globe opens the Host Online page", mc.currentScreen == null ? "none" : mc.currentScreen.getClass().getSimpleName(),
                                     mc.currentScreen instanceof HostScreen, "HostScreen");
                mc.displayGuiScreen(null);
                DevAutoTest.keepScreen = false;
                return false;
        }
        return false;
    }
}
```

Register in `DevAutoTest.mode()`: `if (name.equals("hostmenu")) return new net.decimation.worldgen.devtest.HostMenuTest();`

- [ ] **Step 2: Run to see it fail**

Run (check no user game first): `pgrep -fl "zulu-8.jdk.*GradleStart"`; then `python3 tools/devtest.py --live hostmenu`
Expected: compile error, then after stubs FAIL "Host Online button in the pause menu: none".

- [ ] **Step 3: Implement**

`Deci.java` (beside `toasts()`):

```java
    /** Shows one of Decimation's toasts (ToastNotification.show, deci.b.b.b): title "Decimation", one line. */
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public static void toast(String line)
    {
        deci.b.b.b(line);
    }
```

`HostMenu.java`:

```java
package net.decimation.fixes.hosting;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.List;
import net.decimation.fixes.Deci;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;

/** The pause menu's Host Online button (Server Status' place) and the LAN globe both open HostScreen (spec 3.1). */
public final class HostMenu
{
    public static final int ID_HOST = 9300;
    private static final int SERVER_STATUS = 11, LAN = 25;

    @SubscribeEvent
    public void onInit(GuiScreenEvent.InitGuiEvent.Post e)
    {
        if (!Deci.isHomeMenu(e.gui) || Minecraft.getMinecraft().theWorld == null || !Minecraft.getMinecraft().isSingleplayer())
        {
            return;
        }
        @SuppressWarnings("unchecked")
        List<GuiButton> buttons = e.buttonList;
        for (int i = 0; i < buttons.size(); i++)
        {
            GuiButton b = buttons.get(i);
            if (b.id == SERVER_STATUS)
            {
                boolean on = HostService.INSTANCE.state() == HostService.State.ONLINE;
                buttons.set(i, Deci.menuButton(ID_HOST, b.xPosition, b.yPosition, b.width, b.height,
                    (on ? "§a" : "") + I18n.format("deciworldgen.host.button"), I18n.format("deciworldgen.host.tooltip")));
            }
        }
    }

    @SubscribeEvent
    public void onClick(GuiScreenEvent.ActionPerformedEvent.Pre e)
    {
        if (Deci.isHomeMenu(e.gui) && (e.button.id == ID_HOST || e.button.id == LAN))
        {
            e.setCanceled(true);
            Minecraft.getMinecraft().displayGuiScreen(new HostScreen(e.gui));
        }
    }
}
```

`en_US.lang` additions:

```
deciworldgen.host.button=Host Online >>
deciworldgen.host.tooltip=Let others join your world
deciworldgen.host.title=Host Online
deciworldgen.host.name=Name on the Community list
deciworldgen.host.who=Who can join
deciworldgen.host.offline=Any account (offline)
deciworldgen.host.online=Mojang accounts only (online)
deciworldgen.host.max=Max players
deciworldgen.host.mods=Mods others need (from your game)
deciworldgen.host.route=Connection
deciworldgen.host.relay=Through the DeciRemnant relay (free, shared limit)
deciworldgen.host.start=Start hosting
deciworldgen.host.stop=Stop hosting
deciworldgen.host.back=Back
deciworldgen.host.listed=Listed on the Community page
deciworldgen.host.full=The free relay is full until tomorrow 07:00 WIB
deciworldgen.host.failed=Hosting failed: %s
```

`HostScreen.java`:

```java
package net.decimation.fixes.hosting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldSettings;
import net.decimation.fixes.Deci;

/**
 * The Host Online page (spec 3.1, mockup section 3): name, who can join, max players, mods, connection, Start / Stop.
 * Drawn in Decimation's style with its own buttons (Deci.menuButton) on the dimmed world, the panel where the server list
 * sits on Decimation's pages.
 */
public final class HostScreen extends GuiScreen
{
    private static final int START = 1, BACK = 2, OFFLINE = 3, ONLINE = 4, MINUS = 5, PLUS = 6;
    private final GuiScreen parent;
    private GuiTextField name;
    private boolean online;
    private int max = 4;

    public HostScreen(GuiScreen parent)
    {
        this.parent = parent;
        HostService.Settings s = HostService.INSTANCE.settings();
        if (s != null)
        {
            online = s.online;
            max = s.max;
        }
    }

    private int x0() { return width / 2 - 173 + 105; }

    @SuppressWarnings("unchecked")
    public void initGui()
    {
        int x = x0();
        name = new GuiTextField(fontRendererObj, x + 6, 70, 230, 14);
        name.setMaxStringLength(32);
        IntegratedServer s = mc.getIntegratedServer();
        name.setText(HostService.INSTANCE.settings() != null ? HostService.INSTANCE.settings().name
                     : s == null ? "" : s.getWorldName());
        buttonList.clear();
        boolean hosting = HostService.INSTANCE.state() == HostService.State.ONLINE;
        buttonList.add(Deci.menuButton(OFFLINE, x + 6, 98, 112, 12, (online ? "" : "§a") + I18n.format("deciworldgen.host.offline"), ""));
        buttonList.add(Deci.menuButton(ONLINE, x + 122, 98, 118, 12, (online ? "§a" : "") + I18n.format("deciworldgen.host.online"), ""));
        buttonList.add(Deci.menuButton(MINUS, x + 6, 124, 14, 12, "-", ""));
        buttonList.add(Deci.menuButton(PLUS, x + 40, 124, 14, 12, "+", ""));
        buttonList.add(Deci.menuButton(START, x + 60, 196, 120, 20, I18n.format(hosting ? "deciworldgen.host.stop" : "deciworldgen.host.start"), ""));
        buttonList.add(Deci.menuButton(BACK, width / 2 - 172, 86, 100, 20, I18n.format("deciworldgen.host.back"), ""));
        for (Object o : buttonList)
        {
            GuiButton b = (GuiButton) o;
            b.enabled = !hosting || b.id == START || b.id == BACK;
        }
        name.setEnabled(!hosting);
    }

    protected void actionPerformed(GuiButton b)
    {
        switch (b.id)
        {
            case OFFLINE: online = false; break;
            case ONLINE: online = true; break;
            case MINUS: max = Math.max(2, max - 1); break;
            case PLUS: max = Math.min(8, max + 1); break;
            case BACK: mc.displayGuiScreen(parent); return;
            case START: startOrStop(); break;
        }
        initGui();
    }

    private void startOrStop()
    {
        if (HostService.INSTANCE.state() == HostService.State.ONLINE)
        {
            HostService.INSTANCE.stop();
            return;
        }
        IntegratedServer s = mc.getIntegratedServer();
        String clean = ListEntry.cleanName(name.getText());
        if (s == null || clean == null)
        {
            return;
        }
        s.setOnlineMode(online);
        String port = s.getPublic() ? HostLan.port() : s.shareToLAN(WorldSettings.GameType.SURVIVAL, false);
        HostLan.remember(port);
        HostService.Settings st = new HostService.Settings();
        st.name = clean;
        st.online = online;
        st.max = max;
        st.player = mc.getSession().getUsername();
        st.version = ModSet.ourVersion();
        HostService.INSTANCE.start(st, Integer.parseInt(port), new HostService.Players()
        {
            public int count()
            {
                IntegratedServer srv = Minecraft.getMinecraft().getIntegratedServer();
                return srv == null ? 0 : Math.max(0, srv.getCurrentPlayerCount() - 1);
            }
        });
        HostService.State state = HostService.INSTANCE.state();
        if (state == HostService.State.FULL_TODAY)
        {
            Deci.toast(I18n.format("deciworldgen.host.full"));
        }
        else if (state == HostService.State.FAILED)
        {
            Deci.toast(I18n.format("deciworldgen.host.failed", HostService.INSTANCE.error()));
        }
    }

    protected void keyTyped(char c, int key)
    {
        name.textboxKeyTyped(c, key);
        super.keyTyped(c, key);
    }

    protected void mouseClicked(int x, int y, int b)
    {
        name.mouseClicked(x, y, b);
        super.mouseClicked(x, y, b);
    }

    public void drawScreen(int mx, int my, float t)
    {
        drawDefaultBackground();
        int x = x0();
        drawRect(x, 48, x + 246, 222, 0xD0000000);
        drawString(fontRendererObj, I18n.format("deciworldgen.host.title"), x + 6, 52, 0xFFFFFF);
        if (HostService.INSTANCE.state() == HostService.State.ONLINE)
        {
            IntegratedServer srv = mc.getIntegratedServer();
            java.util.List<?> players = srv == null ? java.util.Collections.emptyList() : srv.getConfigurationManager().playerEntityList;
            drawString(fontRendererObj, "§a● §f" + I18n.format("deciworldgen.host.listed") + "   "
                       + players.size() + "/" + max, x + 6, 150, 0xFFFFFF);
            int y = 222 + 4;
            for (Object o : players) // the players with their ping, under the panel
            {
                net.minecraft.entity.player.EntityPlayerMP p = (net.minecraft.entity.player.EntityPlayerMP) o;
                drawString(fontRendererObj, p.getCommandSenderName() + " §7" + p.ping + " ms", x + 6, y, 0xFFFFFF);
                y += 10;
            }
        }
        drawString(fontRendererObj, "§7" + I18n.format("deciworldgen.host.name"), x + 6, 61, 0xFFFFFF);
        name.drawTextBox();
        drawString(fontRendererObj, "§7" + I18n.format("deciworldgen.host.who"), x + 6, 88, 0xFFFFFF);
        drawString(fontRendererObj, "§7" + I18n.format("deciworldgen.host.max"), x + 6, 114, 0xFFFFFF);
        drawString(fontRendererObj, String.valueOf(max), x + 26, 126, 0xFFFFFF);
        drawString(fontRendererObj, "§7" + I18n.format("deciworldgen.host.mods"), x + 6, 160, 0xFFFFFF);
        drawString(fontRendererObj, mods(), x + 6, 170, 0xFFFFFF);
        drawString(fontRendererObj, "§7" + I18n.format("deciworldgen.host.route") + ": §f" + I18n.format("deciworldgen.host.relay"),
                   x + 6, 182, 0xFFFFFF);
        super.drawScreen(mx, my, t);
    }

    private static String mods()
    {
        StringBuilder b = new StringBuilder();
        for (String[] m : ModSet.current().pairs())
        {
            b.append(b.length() == 0 ? "" : ", ").append(m[0]).append(' ').append(m[1]);
        }
        return b.toString();
    }

    public boolean doesGuiPauseGame()
    {
        return false;
    }
}
```

`HostLan` (small helper in the same package, `HostLan.java`): remembers the LAN port returned by `shareToLAN` (1.7.10 cannot ask the server for it later):

```java
package net.decimation.fixes.hosting;

/** The port shareToLAN returned; the world stays open to LAN until it closes (1.7.10 cannot close it again). */
final class HostLan
{
    private static volatile String port = "-1";

    private HostLan()
    {
    }

    static void remember(String p)
    {
        if (p != null)
        {
            port = p;
        }
    }

    static String port()
    {
        return port;
    }
}
```

`DecimationWorldGen` (client side registration, beside MenuContinue's): register `new HostMenu()` on `MinecraftForge.EVENT_BUS`, `HostService.INSTANCE` on `MinecraftForge.EVENT_BUS` (WorldEvent.Unload), and a client tick that calls `HostService.INSTANCE.tick(System.currentTimeMillis())` (FML bus, `TickEvent.ClientTickEvent`, phase END).

- [ ] **Step 4: Run the dev test**

Run: `python3 tools/devtest.py --live --stop` (new classes), then `python3 tools/devtest.py --live hostmenu`
Expected: 4 checks PASS; read the `host_page` shot on `dev/run/client/devtest/sheet_hostmenu.png` against the mockup (section 3, before hosting).

- [ ] **Step 5: Commit**

```bash
git add dev/src/main/java/net/decimation/fixes dev/src/main/java/net/decimation/worldgen dev/src/main/resources
git commit -m "Hosting: Host Online button in the pause menu and its page"
```

---

### Task 10: The Community page filled from the list

**Files:**
- Create: `dev/src/main/java/net/decimation/fixes/hosting/CommunityList.java`
- Modify: `dev/src/main/java/net/decimation/fixes/Deci.java` (add `communityScreen`, `isCommunityScreen`, `communityRow`, `setCommunityRows`, `communityRowTexts`), `DecimationWorldGen.java` (register), `en_US.lang`
- Test: extend dev test `lanjoin` (`LanJoinTest`) to join through the Community page; extend `lanhost` (`LanHostTest`) with `real=true` to host through `HostService`

**Interfaces:**
- Consumes: `RelayClient.list`, `JoinService.listen / closeAll`, `ModSet.current`, `ListEntry`.
- Produces: `Deci.communityScreen(java.util.List<Object> rows)` = `new deci.i.j(deci.a.b.a(), rows, false)` (GuiMenuCommunityServers); `Deci.isCommunityScreen(GuiScreen)`; `Deci.communityRow(String name, int port, String label)` = `Deci.officialServer("community", name, "127.0.0.1", port, label, "chernarus")`; `Deci.setCommunityRows(GuiScreen screen, java.util.List<Object> entries, java.util.List<String> titles)` sets `((deci.i.j) screen).iW.a(iterator)` (ScrollPanel.setElements, srg 5224; panel field iW, srg 3411) with `new deci.d.p(0.5f, title, entry, false)` per row (ListElement(float, String, ServerListEntry, boolean)); `Deci.communityRowTexts(GuiScreen)` for the dev test (title per row). Community tab clicks (button 13) and Refresh (13 on that page) open our filled page; leaving the page closes JoinService's ports.

- [ ] **Step 1: Extend the dev tests (failing first)**

`LanHostTest`: a field `private final boolean real = "true".equals(System.getProperty("deciworldgen.autotest.real"));`. At `t == 20`, after `shareToLAN`, when `real`:

```java
            String relay = System.getProperty("deciworldgen.autotest.relay");
            if (relay != null) System.setProperty("deciworldgen.relay", relay);
            net.decimation.fixes.hosting.HostService.Settings st = new net.decimation.fixes.hosting.HostService.Settings();
            st.name = "Spike world"; st.online = online; st.max = 4; st.player = host.getCommandSenderName();
            st.version = net.decimation.fixes.hosting.ModSet.ourVersion();
            net.decimation.fixes.hosting.HostService.INSTANCE.start(st, lastPort, () -> Math.max(0, s.getCurrentPlayerCount() - 1));
            DevTestResults.check(name(), "host state", net.decimation.fixes.hosting.HostService.INSTANCE.state(),
                net.decimation.fixes.hosting.HostService.INSTANCE.state() == net.decimation.fixes.hosting.HostService.State.ONLINE, "ONLINE");
```

and at `end`: `net.decimation.fixes.hosting.HostService.INSTANCE.stop();`.

`LanJoinTest`: a field `private final boolean viaList = "true".equals(System.getProperty("deciworldgen.autotest.viaList"));`. Replace `case 1`'s connect line with:

```java
                if (viaList)
                {
                    String relay = System.getProperty("deciworldgen.autotest.relay");
                    if (relay != null) System.setProperty("deciworldgen.relay", relay);
                    net.decimation.fixes.hosting.CommunityList.open(mc);
                    next(20);
                    break;
                }
                mc.displayGuiScreen(new GuiConnecting(mc.currentScreen, mc, joinHost, joinPort));
```

and add before `case 2`:

```java
            case 20: // the Community page lists the host's world
            {
                java.util.List<String> rows = Deci.communityRowTexts(mc.currentScreen);
                int i = -1;
                for (int k = 0; k < rows.size(); k++) if (rows.get(k).contains("Spike world")) i = k;
                if (i < 0 && wait < 20 * 30) break;
                DevTestResults.check(name(), "row shows Spike world", rows, i >= 0, "a row titled Spike world");
                if (i < 0) return false;
                int port = net.decimation.fixes.hosting.JoinService.INSTANCE.portOf("Spike world");
                cpw.mods.fml.client.FMLClientHandler.instance().connectToServer(mc.currentScreen,
                    new net.minecraft.client.multiplayer.ServerData("Spike world", "127.0.0.1:" + port)); // what the row's button 123 does
                next(2);
                break;
            }
```

`JoinService` gains `public int portOf(String name)` (the port it opened for the entry with that name, -1 when none): keep a `Map<String, Integer>` filled in `listen` and cleared in `closeAll`.

Missing mod check: a JUnit test already covers the refusal text (Task 8); the dev test adds no second copy.

Run: `python3 tools/devtest.py --live --stop`; start `cd relay && npm run dev` (local relay on 8787) in the background; then the host with `python3 tools/devtest.py --live lanhost online=false scene=true real=true relay=http://127.0.0.1:8787 wait=12000 bus=BUS` and the joiner as in docs/player_hosting.md section 9 with `viaList=true relay=http://127.0.0.1:8787` added.
Expected: FAIL "row shows Spike world" (CommunityList missing).

- [ ] **Step 2: Implement `CommunityList`**

```java
package net.decimation.fixes.hosting;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayList;
import java.util.List;
import net.decimation.fixes.Deci;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;

/**
 * Decimation's Community page (GuiMenuCommunityServers) filled from our list (spec 3.1, mockup section 4): one row per
 * world on a local port of JoinService (Decimation pings and joins it like any server: real players and ping come
 * through the relay); the title carries the name and who can join, the hover tooltip the mods, a missing one red.
 */
public final class CommunityList
{
    private static final int COMMUNITY_TAB = 13;
    private static volatile List<ListEntry> shown = new ArrayList<ListEntry>();

    public static void open(final Minecraft mc)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                List<ListEntry> list;
                String error = null;
                try
                {
                    list = new RelayClient(RelayClient.base()).list();
                }
                catch (RelayClient.RelayFull e)
                {
                    list = new ArrayList<ListEntry>();
                    error = net.minecraft.client.resources.I18n.format("deciworldgen.host.full");
                }
                catch (Exception e)
                {
                    list = new ArrayList<ListEntry>();
                    error = net.minecraft.client.resources.I18n.format("deciworldgen.list.unreachable");
                }
                final List<ListEntry> got = list;
                final String err = error;
                mc.func_152344_a(new Runnable() // Minecraft.addScheduledTask
                {
                    public void run()
                    {
                        show(mc, got, err);
                    }
                });
            }
        }, "DeciRemnant list").start();
    }

    static void show(Minecraft mc, List<ListEntry> list, String error)
    {
        JoinService.INSTANCE.closeAll();
        JoinService.INSTANCE.setToasts(new JoinService.Toasts()
        {
            public void show(final String line)
            {
                Minecraft.getMinecraft().func_152344_a(new Runnable()
                {
                    public void run()
                    {
                        Deci.toast(line);
                    }
                });
            }
        });
        ModSet mine = ModSet.current();
        String version = ModSet.ourVersion();
        List<Object> rows = new ArrayList<Object>();
        List<String> titles = new ArrayList<String>();
        for (ListEntry e : list)
        {
            try
            {
                int port = JoinService.INSTANCE.listen(e, mine);
                boolean sameVersion = version.equals(e.version);
                rows.add(Deci.communityRow(e.name, port, e.online ? "Mojang accounts" : "Any account"));
                titles.add((sameVersion ? "" : "§7") + e.name + (sameVersion ? "" : " §c(needs " + e.version + ")"));
            }
            catch (java.io.IOException ex)
            {
                // no free local port: leave the row out
            }
        }
        shown = list;
        GuiScreen page = Deci.communityScreen(rows);
        mc.displayGuiScreen(page);
        Deci.setCommunityRows(page, rows, titles);
        if (error != null)
        {
            Deci.toast(error);
        }
    }

    @SubscribeEvent
    public void onClick(GuiScreenEvent.ActionPerformedEvent.Pre e)
    {
        if (e.button.id == COMMUNITY_TAB && (Deci.isCommunityScreen(e.gui) || e.gui.getClass().getName().equals("deci.i.i")))
        {
            e.setCanceled(true);
            open(Minecraft.getMinecraft());
        }
    }

    @SubscribeEvent
    public void onOpen(GuiOpenEvent e)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (Deci.isCommunityScreen(mc.currentScreen) && !Deci.isCommunityScreen(e.gui)
            && !(e.gui instanceof net.minecraft.client.multiplayer.GuiConnecting) && mc.theWorld == null)
        {
            JoinService.INSTANCE.closeAll(); // left the page without joining: free the ports
        }
    }

    @SubscribeEvent
    public void onDraw(GuiScreenEvent.DrawScreenEvent.Post e)
    {
        // the hovered row's mods as a tooltip, a missing one in red (Deci.hoveredCommunityRow returns the row index or -1)
        if (Deci.isCommunityScreen(e.gui))
        {
            int players = 0;
            for (ListEntry w : shown) players += w.players + 1; // the host plays too
            e.gui.drawString(Minecraft.getMinecraft().fontRenderer, "§a● §fPlayers Online: §a" + players
                             + " §fin " + shown.size() + " worlds", e.gui.width / 2 - 173 + 119, 63, 0xFFFFFF);
        }
        int i = Deci.hoveredCommunityRow(e.gui);
        if (i < 0 || i >= shown.size())
        {
            return;
        }
        ListEntry entry = shown.get(i);
        String missing = ModSet.current().firstMissing(entry.modSet());
        List<String> lines = new ArrayList<String>();
        lines.add(net.minecraft.client.resources.I18n.format("deciworldgen.list.mods"));
        for (String[] m : entry.modSet().pairs())
        {
            String s = m[0] + " " + m[1];
            lines.add((s.equals(missing) ? "§c" : "§f") + s);
        }
        lines.add("§7host " + entry.host + " · " + (entry.online ? "Mojang accounts" : "Any account"));
        tooltip(lines, e.mouseX, e.mouseY);
    }

    /** Minecraft's tooltip look (1.7.10 has no public helper for it). */
    private static void tooltip(List<String> lines, int x, int y)
    {
        net.minecraft.client.gui.FontRenderer f = Minecraft.getMinecraft().fontRenderer;
        int w = 0;
        for (String s : lines) w = Math.max(w, f.getStringWidth(s));
        int x0 = x + 12, y0 = y - 12, h = lines.size() * 10;
        net.minecraft.client.gui.Gui.drawRect(x0 - 3, y0 - 3, x0 + w + 3, y0 + h + 1, 0xF0100010);
        for (int k = 0; k < lines.size(); k++) f.drawStringWithShadow(lines.get(k), x0, y0 + k * 10, 0xFFFFFF);
    }

    /** The Community tab tells how many worlds are listed (Decimation's own tooltip said "Offline", then "coming soon"). */
    @SubscribeEvent
    public void onInit(GuiScreenEvent.InitGuiEvent.Post e)
    {
        if (!e.gui.getClass().getName().equals("deci.i.i") && !Deci.isCommunityScreen(e.gui)) return;
        @SuppressWarnings("unchecked")
        List<net.minecraft.client.gui.GuiButton> b = e.buttonList;
        for (int k = 0; k < b.size(); k++)
        {
            net.minecraft.client.gui.GuiButton old = b.get(k);
            if (old.id == COMMUNITY_TAB && old.yPosition < 80) // the tab at y 48, not Refresh List at y 86
            {
                b.set(k, Deci.menuButton(COMMUNITY_TAB, old.xPosition, old.yPosition, old.width, old.height, "§7Community",
                                         shown.size() + " worlds"));
            }
        }
    }

    private static volatile long joinedAt;
    private static volatile boolean retried;
    static volatile net.minecraft.client.multiplayer.ServerData lastServer;

    /** JoinService calls this before it pipes a join (the retry below needs the server and the time). */
    static void joining(net.minecraft.client.multiplayer.ServerData s)
    {
        lastServer = s;
        joinedAt = System.currentTimeMillis();
        retried = false;
    }

    /** Forge's handshake timed out on a busy computer (spec 4.2 step 4, seen in the spike): join once more by itself. */
    @SubscribeEvent
    public void onDisconnected(GuiOpenEvent e)
    {
        if (e.gui instanceof net.minecraft.client.gui.GuiDisconnected && !retried && lastServer != null
            && System.currentTimeMillis() - joinedAt < 20_000)
        {
            retried = true;
            e.gui = new net.minecraft.client.multiplayer.GuiConnecting(Deci.homeMenu(), Minecraft.getMinecraft(), lastServer);
        }
    }
}
```

`Deci.java` additions (obfuscated names from deobf/decimation.srg: GuiMenuCommunityServers `deci/i/j`, its panel `iW`, ScrollPanel.setElements `deci/d/l/a(Iterator)`, ListElement `deci/d/p`):

```java
    /** Decimation's Community page (GuiMenuCommunityServers deci.i.j) on these rows (ServerListEntry deci.k.a). */
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public static net.minecraft.client.gui.GuiScreen communityScreen(java.util.List<Object> rows)
    {
        java.util.List<deci.k.a> l = new java.util.ArrayList<deci.k.a>();
        for (Object o : rows) l.add((deci.k.a) o);
        return new deci.i.j(deci.a.b.a(), l, false);
    }

    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public static boolean isCommunityScreen(net.minecraft.client.gui.GuiScreen s)
    {
        return s instanceof deci.i.j;
    }

    /** One community row on a local port (pinged and joined like a server), the Chernarus pictures on hover. */
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public static Object communityRow(String name, int port, String label)
    {
        return officialServer("community", name, "127.0.0.1", port, label, "chernarus");
    }

    /** The page's ScrollPanel (GuiMenuCommunityServers.serverListPanel iW, private final). */
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    private static deci.d.l communityPanel(net.minecraft.client.gui.GuiScreen s)
    {
        try
        {
            java.lang.reflect.Field f = deci.i.j.class.getDeclaredField("iW");
            f.setAccessible(true);
            return (deci.d.l) f.get(s);
        }
        catch (ReflectiveOperationException e)
        {
            return null;
        }
    }

    /** Replaces the page's rows (ScrollPanel.setElements) with ListElements titled by us (not "Community Server"). */
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public static void setCommunityRows(net.minecraft.client.gui.GuiScreen s, java.util.List<Object> rows, java.util.List<String> titles)
    {
        java.util.List<deci.d.p> el = new java.util.ArrayList<deci.d.p>();
        for (int i = 0; i < rows.size(); i++) el.add(new deci.d.p(0.5f, titles.get(i), (deci.k.a) rows.get(i), false));
        deci.d.l panel = communityPanel(s);
        if (panel != null) panel.a(el.iterator());
    }

    /** The rows' titles (ListElement fX, private) in order; for dev tests. */
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public static java.util.List<String> communityRowTexts(net.minecraft.client.gui.GuiScreen s)
    {
        java.util.List<String> out = new java.util.ArrayList<String>();
        deci.d.l panel = s instanceof deci.i.j ? communityPanel(s) : null;
        if (panel == null) return out;
        for (Object o : panel.fl)
        {
            if (!(o instanceof deci.d.p)) continue;
            try
            {
                java.lang.reflect.Field f = deci.d.p.class.getDeclaredField("fX");
                f.setAccessible(true);
                out.add(String.valueOf(f.get(o)));
            }
            catch (ReflectiveOperationException e)
            {
                out.add("unreadable");
            }
        }
        return out;
    }

    /** The row under the mouse: Decimation raises a hovered row's fade fH toward 1 (ListElement); -1 when none is over 0.5. */
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public static int hoveredCommunityRow(net.minecraft.client.gui.GuiScreen s)
    {
        deci.d.l panel = s instanceof deci.i.j ? communityPanel(s) : null;
        if (panel == null) return -1;
        int best = -1, i = 0;
        float top = 0.5f;
        for (Object o : panel.fl)
        {
            if (o instanceof deci.d.p)
            {
                if (((deci.d.p) o).fH > top) { top = ((deci.d.p) o).fH; best = i; }
                i++;
            }
        }
        return best;
    }
```

ScrollPanel's element list is its public field `fl` (deobf/src/decimation/gui/ScrollPanel.java), the one `Deci.officialRowList` already reads for the official page; `fH` (hover fade) is public on ListElement (used by `Deci.showRowPicture`).

`en_US.lang`: `deciworldgen.list.mods=Mods` and `deciworldgen.list.unreachable=Community list not reachable, try Refresh`.

Register `new CommunityList()` on `MinecraftForge.EVENT_BUS` in DecimationWorldGen (client). In `JoinService`'s accept loop, right before `Pipe.start(...)`, add `CommunityList.joining(new net.minecraft.client.multiplayer.ServerData(e.name, "127.0.0.1:" + ss.getLocalPort()));` (pings also pass there: the retry only acts on a disconnect screen within 20 s, which a ping never opens).

- [ ] **Step 3: Run the dev tests against the local relay**

Run as in Step 1.
Expected: lanhost `host state ONLINE` PASS; lanjoin `row shows Spike world` PASS and every check of docs/player_hosting.md section 9 PASS through the local relay. Write the relay's message counts (wrangler dev log, or a counter added to Room for dev only) per minute of play into docs/player_hosting.md section 11 (spec 7 budget).

- [ ] **Step 4: Commit**

```bash
git add dev/src/main/java/net/decimation relay docs/player_hosting.md dev/src/main/resources
git commit -m "Hosting: Community page filled from the list, joins through the relay"
```

---

### Task 11: Deploy the relay and the real case test

**Files:**
- Modify: `dev/src/main/java/net/decimation/fixes/hosting/RelayClient.java` (`DEPLOYED`), `docs/player_hosting.md` section 11, `docs/production_test.md`, `CLAUDE.md`, `docs/tasks.md`

**Interfaces:**
- Consumes: everything above.
- Produces: the deployed relay address in `RelayClient.DEPLOYED`.

- [ ] **Step 1: The user's part (ask, do not do)**

Ask the user to create a free Cloudflare account and run `! cd relay && npx wrangler login` in this session. Ask before the first deploy (outward facing).

- [ ] **Step 2: Deploy and check**

Run: `cd relay && npx wrangler deploy` (after the user's yes). Expected: an address like `https://deciremnant-relay.<account>.workers.dev`. Check: `curl -s <address>/list` answers `[]`; `curl -s <address>/` answers `DeciRemnant relay`.

- [ ] **Step 3: Point the mod at it**

Set `RelayClient.DEPLOYED` to the address; `./gradlew test -q` still passes.

- [ ] **Step 4: Local games through the deployed relay**

Run the Task 10 dev tests with `relay=<address>` on both games. Expected: all PASS. Record ping and join time. Then once with `online=true` on the host: the joiner (a legacy account) must be refused by Minecraft's own login check ("Failed to verify username" in the joiner's log): the "Mojang accounts only" choice works.

- [ ] **Step 5: Real case on the brother's laptop (Tailscale)**

Ask at this point: the brother's agreement; Java 8 and the release candidate jar on the laptop (built by `tools/onejar.py`, sent by the user); whether the brother clicks or the user allows a shell over Tailscale. Then: host on this Mac (`lanhost real=true` or by hand), the laptop joins through the Community page. Measure: ping on the row, join time, bytes and messages per minute (the relay's counts). Write them into docs/player_hosting.md section 11.

- [ ] **Step 6: Full live suite**

Run: `python3 tools/devtest.py --live zombies checks places zones loot drive trade` then `mainmenu menupages` last. Expected: all PASS.

- [ ] **Step 7: Docs and commit**

CLAUDE.md: a v-line for the hosting feature (package `fixes/hosting`, relay folder, dev tests, deploy address, traps found). docs/tasks.md: part 1 done (not released). Then:

```bash
git add -A
git commit -m "Hosting part 1: relay deployed, real case test through it, docs"
```

Release stays batched (user): no GitHub release from this plan.

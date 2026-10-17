import { DurableObject } from 'cloudflare:workers';

/** One hosted world on the public list (docs/superpowers/specs/2026-10-18-player-hosting-part1-design.md 3.3). */
export interface Entry {
  room: string; secret: string; name: string; host: string; online: boolean; players: number; max: number;
  mods: [string, string][]; version: string; since: number; seen: number; ip: string;
}

const STALE_MS = 180_000;
const MAX_BODY = 8192;
const MAX_WORLDS = 200;
const PER_ADDRESS = 2;

/** The body as text, read at most `limit` bytes: a longer body is never held in memory (null). */
async function readCapped(req: Request, limit: number): Promise<string | null> {
  if (!req.body) return '';
  const reader = req.body.getReader();
  const parts: Uint8Array[] = [];
  let size = 0;
  for (;;) {
    const { done, value } = await reader.read();
    if (done) break;
    size += value.byteLength;
    if (size > limit) {
      await reader.cancel();
      return null;
    }
    parts.push(value);
  }
  const all = new Uint8Array(size);
  let at = 0;
  for (const p of parts) {
    all.set(p, at);
    at += p.byteLength;
  }
  return new TextDecoder().decode(all);
}
const NAME = /^[^§]{1,32}$/;
const PLAYER = /^[A-Za-z0-9_]{1,16}$/;

function token(n: number): string {
  const a = new Uint8Array(n);
  crypto.getRandomValues(a);
  return Array.from(a, (b) => 'abcdefghijklmnopqrstuvwxyz0123456789'[b % 36]).join('');
}

const json = (o: unknown, status = 200) =>
  new Response(JSON.stringify(o), { status, headers: { 'content-type': 'application/json' } });

/** The public list: register, beat, stop, list; an entry goes 3 min after its last heartbeat. */
export class Lobby extends DurableObject {
  entries = new Map<string, Entry>();
  registered = new Map<string, number>();
  offset = 0; // tests move the clock
  loaded = false;

  now() {
    return Date.now() + this.offset;
  }

  async load() {
    if (this.loaded) return;
    for (const e of (await this.ctx.storage.get<Entry[]>('entries')) ?? []) this.entries.set(e.room, e);
    this.loaded = true;
  }

  async save() {
    await this.ctx.storage.put('entries', [...this.entries.values()]);
  }

  prune() {
    const t = this.now();
    for (const [k, e] of this.entries) if (t - e.seen > STALE_MS) this.entries.delete(k);
  }

  /** May this socket reach the room? A host needs the secret, a joiner (secret null) a listed room. */
  async check(room: string, secret: string | null): Promise<{ ok: boolean; max: number }> {
    await this.load();
    this.prune();
    const e = this.entries.get(room);
    if (!e || (secret !== null && secret !== e.secret)) return { ok: false, max: 0 };
    return { ok: true, max: e.max };
  }

  /** The room's host went away (its control socket closed with no replacement): off the list at once. */
  async drop(room: string): Promise<void> {
    await this.load();
    if (this.entries.delete(room)) await this.save();
  }

  async fetch(req: Request): Promise<Response> {
    await this.load();
    this.prune();
    const path = new URL(req.url).pathname;
    if (req.method === 'GET' && path === '/list') {
      return json([...this.entries.values()].map(({ secret, seen, ip, ...pub }) => ({ ...pub, route: 'relay' })));
    }
    if (req.method !== 'POST') return json({ error: 'method' }, 405);
    // never read more than 8 KB: a list entry is far smaller (abuse guard)
    if (Number(req.headers.get('content-length') ?? '0') > MAX_BODY) return json({ error: 'too large' }, 413);
    const text = await readCapped(req, MAX_BODY);
    if (text === null) return json({ error: 'too large' }, 413);
    let body: any = null;
    try {
      body = JSON.parse(text);
    } catch {
      body = null;
    }
    if (!body) return json({ error: 'json' }, 400);
    if (path === '/list/host') {
      for (const [k, at] of this.registered) if (this.now() - at >= 60_000) this.registered.delete(k); // memory stays small
      if (this.entries.size >= MAX_WORLDS) return json({ error: 'list full' }, 503);
      const ip = req.headers.get('CF-Connecting-IP') ?? 'unknown';
      let held = 0;
      for (const e of this.entries.values()) if (e.ip === ip) held++;
      if (held >= PER_ADDRESS) return json({ error: 'too many worlds' }, 429);
      const last = this.registered.get(ip) ?? -Infinity;
      if (this.now() - last < 60_000) return json({ error: 'too often' }, 429);
      const name = typeof body.name === 'string' ? body.name.trim() : '';
      const ok = NAME.test(name)
        && typeof body.host === 'string' && PLAYER.test(body.host)
        && typeof body.online === 'boolean' && Number.isInteger(body.max) && body.max >= 2 && body.max <= 8
        && Array.isArray(body.mods) && body.mods.length <= 64
        && body.mods.every((m: any) => Array.isArray(m) && m.length === 2 && `${m[0]}`.length <= 64 && `${m[1]}`.length <= 32)
        && typeof body.version === 'string' && body.version.length <= 32;
      if (!ok) return json({ error: 'invalid' }, 400);
      this.registered.set(ip, this.now());
      const e: Entry = {
        room: token(16), secret: token(32), name, host: body.host, online: body.online, players: 0, max: body.max,
        mods: body.mods.map((m: any) => [`${m[0]}`, `${m[1]}`]), version: body.version, since: this.now(), seen: this.now(), ip,
      };
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
      if (e && e.secret === body.secret) {
        this.entries.delete(e.room);
        await this.save();
      }
      return json({ ok: true });
    }
    return json({ error: 'path' }, 404);
  }
}

import { DurableObject } from 'cloudflare:workers';

const ANSWER_MS = 10_000;

/**
 * One hosted world: the host's control socket, one socket per joiner, one host data socket per joiner; binary
 * messages forwarded between a joiner and its data socket (spec 3.2). Texts to the host: {"open": id}, {"close": id};
 * to a joiner: {"ready": true}. Closes: 4404 host offline, 4409 full, 4408 host did not answer, 4410 host stopped.
 */
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

  async webSocketClose(ws: WebSocket, _code: number, reason: string) {
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

import { Lobby } from './lobby';
import { Room } from './room';
export { Lobby, Room };
export { roomHint } from './hint';
import { roomHint } from './hint';

export default {
  async fetch(req: Request, env: Cloudflare.Env): Promise<Response> {
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
      // the host's control socket comes first and creates the room: place it near the host (joiners reuse it)
      const cf = (req as any).cf as { country?: string; continent?: string } | undefined;
      const hint = parts[0] === 'host' && parts.length === 2 ? roomHint(cf?.country, cf?.continent) : undefined;
      const id = env.ROOM.idFromName(parts[1]);
      const r = await (hint ? env.ROOM.get(id, { locationHint: hint }) : env.ROOM.get(id)).fetch(fwd);
      if (r.status !== 101 || !r.webSocket) return r;
      // which data center this socket landed in: the game retries one that landed far from the room (docs/player_hosting.md 16)
      const colo = ((req as any).cf as { colo?: string } | undefined)?.colo;
      return new Response(null, { status: 101, webSocket: r.webSocket, headers: colo ? { 'x-colo': colo } : {} });
    }
    return new Response('DeciRemnant relay', { status: 200 });
  },
};

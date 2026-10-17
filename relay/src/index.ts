import { Lobby } from './lobby';
import { Room } from './room';
export { Lobby, Room };

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
      return env.ROOM.get(env.ROOM.idFromName(parts[1])).fetch(fwd);
    }
    return new Response('DeciRemnant relay', { status: 200 });
  },
};

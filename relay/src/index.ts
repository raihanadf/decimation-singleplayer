import { Lobby } from './lobby';
import { Room } from './room';
export { Lobby, Room };

const SE_ASIA = new Set(['ID', 'SG', 'MY', 'TH', 'VN', 'PH', 'KH', 'LA', 'MM', 'BN', 'TL']);
const NE_ASIA = new Set(['JP', 'KR', 'KP', 'CN', 'TW', 'HK', 'MO', 'MN']);
const E_EUROPE = new Set(['PL', 'CZ', 'SK', 'HU', 'RO', 'BG', 'UA', 'BY', 'MD', 'LT', 'LV', 'EE', 'RU', 'RS', 'HR', 'SI', 'BA', 'GR', 'TR']);

/**
 * Where a new room should live: near the host whose control socket creates it (Durable Object location hint, best effort,
 * only on creation). Free Worker traffic from Indonesia was seen in Paris and Frankfurt (docs/player_hosting.md 14).
 */
export function roomHint(country?: string, continent?: string): DurableObjectLocationHint | undefined {
  if (continent === 'AS') return country && SE_ASIA.has(country) ? 'apac-se' : country && NE_ASIA.has(country) ? 'apac-ne' : 'apac';
  if (continent === 'OC') return 'oc';
  if (continent === 'EU') return country && E_EUROPE.has(country) ? 'eeur' : 'weur';
  if (continent === 'NA') return 'enam';
  if (continent === 'SA') return 'sam';
  if (continent === 'AF') return 'afr';
  return undefined;
}

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
      return (hint ? env.ROOM.get(id, { locationHint: hint }) : env.ROOM.get(id)).fetch(fwd);
    }
    return new Response('DeciRemnant relay', { status: 200 });
  },
};

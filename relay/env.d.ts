declare namespace Cloudflare {
  interface Env {
    LOBBY: DurableObjectNamespace<import('./src/lobby').Lobby>;
    ROOM: DurableObjectNamespace<import('./src/room').Room>;
    /** Local tests only (`wrangler dev --var DIRECT_LOOPBACK:true`): take [::1] as a direct address. Never deployed. */
    DIRECT_LOOPBACK?: string;
    /** Local tests only: data centers to report in turn (x-colo), e.g. "FRA,SIN", and the region every world gets. */
    FAKE_COLOS?: string;
    FAKE_REGION?: string;
  }
}

declare namespace Cloudflare {
  interface Env {
    LOBBY: DurableObjectNamespace<import('./src/lobby').Lobby>;
    ROOM: DurableObjectNamespace<import('./src/room').Room>;
  }
}

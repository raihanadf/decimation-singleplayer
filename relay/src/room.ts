import { DurableObject } from 'cloudflare:workers';
export class Room extends DurableObject {
  async fetch(): Promise<Response> { return new Response('not yet', { status: 501 }); }
}

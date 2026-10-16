# DeciRemnant Website Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:executing-plans (the user's standing rule: one session,
> no subagents, no workflows). Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build and ship DeciRemnant, the one page project website (download, features, screenshots, coming soon,
install guide, changelog, help), plus the dev test that photographs the features for it.

**Architecture:** A Next.js 16 App Router site in a new private repo `deciremnant` beside this one, deployed on
Vercel; the page is static and regenerated every 600 s (ISR) from GitHub's release list read on the server. The
game art it shows (Decimation's item icons and torn menu textures, screenshots) is exported by a tool in this repo
into the private repo only. A new dev test mode `siteshots` in this repo takes the screenshots in a fresh seed 1
world.

**Tech Stack:** Next.js 16.4.x, React 19, TypeScript, plain CSS, next/font/local, next/image; Vitest 5 with React
Testing Library and jsdom; agent-browser for page checks; Python 3 with Pillow for the asset tool; Java 8 (Forge
1.7.10 dev workspace) for the dev test.

**Spec:** docs/website.md (read it first; section numbers below refer to it). Skill: decimation-website.

## Global Constraints

- Site repo: `~/Projects/deciremnant`, branch `main`, PRIVATE on GitHub as `ucilmenangis/deciremnant`; Vercel project `deciremnant` (`deciremnant.vercel.app`).
- Next.js 16.4.x App Router, TypeScript, Cache Components OFF (`--no-cache-components`), page segment `export const revalidate = 600`.
- Release rule: the newest non draft release with an asset named exactly `Decimation.jar`, pre-releases included (same as fixes/Updater); the button links the asset's `browser_download_url`.
- Releases API: `https://api.github.com/repos/ucilmenangis/decimation-singleplayer/releases?per_page=10`, headers `Accept: application/vnd.github.html+json`, `X-GitHub-Api-Version: 2022-11-28`, `User-Agent: deciremnant`, optional `Authorization: Bearer $GITHUB_TOKEN`; override URL with env `RELEASES_API`.
- Font: Minecraft Standard (CC PD) only at 12, 18, 24, 36 px; shadow one font pixel (2, 3, 4, 6 px) at #3F3F3F for white; `-webkit-font-smoothing: none`.
- Colours: bg #1C1815, card #26211D, text #EDE6DA, muted #B9AE9E, dim #9A8F80, label #E08A5A, link #E59468, download #F89838 (shadow #3E260E, edges #A85E18 / #FFB868), Planned tag and splash #FFFF55 (shadow #3F3F15), Idea tag #55FFFF (shadow #153F3F), slot #15110F (edges #0B0908 / #3B332C).
- Texts only in `content/`; English; no dash punctuation in any page text (no em dash, no en dash, no spaced hyphen).
- Footer line verbatim: "Unofficial fan project. Decimation belongs to BoehMod. Not an official Minecraft product; not approved by or associated with Mojang or Microsoft."
- Never the Decimation logo image. Decimation's files (icons, textures, screenshots) only in the private site repo; this public repo never commits images, fonts or jars.
- Git in both repos: the agent commits (one plain line, no attribution of any kind), the user pushes; never `git push`.
- Before any live client test: `pgrep -fl "zulu-8.jdk.*GradleStart"` as its own call; a game without `-Ddeciworldgen.autotest` is the user's: ask first. Never Prism.
- Temp files: `$SCRATCH` below means this session's scratchpad directory (the harness names it); never `/tmp`.

## Review Focus

- GitHub answering 403 (rate limit) or 5xx while `next build` runs or while ISR refreshes: the build must pass with a "Download from GitHub" fallback, a failed refresh must keep the last good page. Tests: Task A3 (both phases), Task A8 steps 3 and 9.
- A newest release without `Decimation.jar` (or a draft, visible once a token is set): the button must still show the newest release that HAS the jar. Tests: Task A2.
- The download link must be the browser download URL (a redirect to the file), never the API asset URL (JSON). Tests: Task A2 (url field), Task A8 step 7 (HEAD answers 302).
- Phone width 390 px: the game font buttons and titles must not push the page sideways. Test: Task A8 step 5 (`scrollWidth` at 390).
- The random splash on a static page and release notes with no body: no hydration error in the console, "No notes for this release." instead of an empty fold out. Tests: Task A7 (Splash, Changelog), Task A8 step 6 (`agent-browser errors` empty).

## File map

Site repo `~/Projects/deciremnant` (new):

| Path | Job |
|---|---|
| `app/layout.tsx` | html shell, font, metadata |
| `app/page.tsx` | the page: loads release data, puts the sections together, `revalidate = 600` |
| `app/globals.css` | all styles (tokens, game font sizes, sections, phones) |
| `app/fonts/MinecraftStandard.otf`, `app/fonts/MinecraftStandard-readme.txt` | the font and its licence readme |
| `lib/releases.ts` | GitHub release types, `pickRelease`, `recentReleases`, `formatSize`, `formatDate` (pure) |
| `lib/github.ts` | `fetchReleases()` (server fetch, headers, 600 s cache) |
| `lib/load.ts` | `loadReleaseData()` (build phase fallback, run time throw) |
| `lib/inline.tsx` | `inline(text)`: renders `code` and **bold** in content strings |
| `content/types.ts`, `site.ts`, `features.ts`, `coming.ts`, `shots.ts`, `install.ts`, `help.ts`, `splashes.ts` | every text of the page |
| `components/*.tsx` | Nav, Hero, Splash, Download, Section, CardGrid, Gallery, Install, Changelog, Help, Footer |
| `public/icons/`, `public/ui/`, `public/shots/`, `public/og.jpg` | exported art (Task A4, C1) |
| `scripts/check-links.mjs` | link checker for a running page |
| `tests/*.test.ts(x)` | Vitest tests |

This repo:

| Path | Job |
|---|---|
| `tools/site/export_assets.py` | exports icons, textures, dirt strip, screenshots into the site repo |
| `tools/site/subjects.py` | lists feature coordinates from a server log (Task B3) |
| `tools/site/siteshots.tsv` | camera rows for the screenshots |
| `dev/src/main/java/net/decimation/worldgen/devtest/SiteShotsTest.java` | the `siteshots` dev test mode |
| `dev/src/main/java/net/decimation/worldgen/DevAutoTest.java` | registers the mode |
| `dev/dependencies.gradle` | `-Pwindow=WxH`, `-Pshots=...` |
| `tools/devtest.py` | `--window=WxH` for a live start |
| `dev/src/main/java/net/decimation/worldgen/LargeSites.java`, `.../city/LcCity.java` | one log line per planned military base and city refuge (positions for Task B3) |

---

## Part A: the site (works on its own with the stand in screenshots)

### Task 1 (A1): Scaffold the site repo

**Files:**
- Create: `~/Projects/deciremnant` (create-next-app), `vitest.config.mts`, `.gitignore` additions
- Modify: `package.json` (scripts)

**Interfaces:**
- Produces: a Next.js 16 app with `npm run build`, `npm run lint`, `npx vitest run` working; path alias `@/*` to the repo root.

- [ ] **Step 1: Create the app (Cache Components off, no Tailwind, no git yet)**

```bash
cd ~/Projects && npx create-next-app@16.4.2 deciremnant --ts --app --eslint --no-tailwind --no-src-dir \
  --import-alias "@/*" --use-npm --no-cache-components --no-react-compiler --no-agents-md --no-agent-feedback \
  --disable-git --yes
```
Expected: "Success! Created deciremnant". Check `grep -n cacheComponents ~/Projects/deciremnant/next.config.ts` prints nothing.

- [ ] **Step 2: Remove the template page content**

Replace `app/page.tsx` with:
```tsx
export default function Page() {
  return <main />;
}
```
Replace `app/globals.css` with an empty file and delete `app/page.module.css` and the template SVGs in `public/` (`rm -f app/page.module.css public/*.svg`).

- [ ] **Step 3: Test tooling**

```bash
cd ~/Projects/deciremnant && npm install -D vitest@5.0.5 @vitejs/plugin-react jsdom @testing-library/react @testing-library/dom vite-tsconfig-paths
```
Create `vitest.config.mts`:
```ts
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import tsconfigPaths from 'vite-tsconfig-paths';

export default defineConfig({
  plugins: [tsconfigPaths(), react()],
  test: {
    environment: 'jsdom',
    environmentOptions: { jsdom: { pretendToBeVisual: true } }, // requestAnimationFrame for the Splash test
    include: ['tests/**/*.test.{ts,tsx}'],
  },
});
```
In `package.json` scripts add `"test": "vitest run"` and `"typecheck": "tsc --noEmit"` (keep `dev`, `build`, `start`, `lint`).

- [ ] **Step 4: A smoke test so the runner is proven**

Create `tests/smoke.test.ts`:
```ts
import { expect, it } from 'vitest';

it('runs', () => {
  expect(1 + 1).toBe(2);
});
```
Run: `npm test` Expected: 1 passed.

- [ ] **Step 5: Build and lint the empty app**

Run: `npm run build && npm run lint && npm run typecheck` Expected: build lists route `/`, lint and tsc print no errors.

- [ ] **Step 6: Git init and first commit**

```bash
cd ~/Projects/deciremnant && git init -b main && git add -A && git commit -m "Next.js 16 app scaffold (no Cache Components, no Tailwind) with Vitest"
```
Check `git ls-files | grep -E '\.(jar|otf|png|jpg)$'` prints nothing yet.

### Task 2 (A2): Release logic (pure functions, test first)

**Files:**
- Create: `lib/releases.ts`
- Test: `tests/releases.test.ts`

**Interfaces:**
- Produces:
  - `type GhAsset = { name: string; size: number; browser_download_url: string; url?: string }`
  - `type GhRelease = { tag_name: string; draft: boolean; prerelease: boolean; published_at: string | null; html_url: string; body_html?: string | null; assets: GhAsset[] }`
  - `type LatestRelease = { tag: string; publishedAt: string; prerelease: boolean; size: number; downloadUrl: string; sha256Url: string | null; pageUrl: string }`
  - `type ChangelogEntry = { tag: string; publishedAt: string; prerelease: boolean; html: string; pageUrl: string }`
  - `pickRelease(list: GhRelease[]): LatestRelease | null`
  - `recentReleases(list: GhRelease[], n?: number): ChangelogEntry[]` (default 5)
  - `formatSize(bytes: number): string` ("241 MB")
  - `formatDate(iso: string): string` ("13 October 2026", UTC)

- [ ] **Step 1: Write the failing tests**

Create `tests/releases.test.ts`:
```ts
// @vitest-environment node
import { describe, expect, it } from 'vitest';
import { formatDate, formatSize, pickRelease, recentReleases, type GhAsset, type GhRelease } from '@/lib/releases';

const jar = (tag: string, size = 240980301): GhAsset => ({
  name: 'Decimation.jar',
  size,
  browser_download_url: `https://github.com/u/r/releases/download/${tag}/Decimation.jar`,
  url: `https://api.github.com/repos/u/r/releases/assets/${tag}`,
});
const sha = (tag: string): GhAsset => ({
  name: 'Decimation.jar.sha256',
  size: 64,
  browser_download_url: `https://github.com/u/r/releases/download/${tag}/Decimation.jar.sha256`,
});
const rel = (tag: string, published: string | null, assets: GhAsset[] = [jar(tag)], extra: Partial<GhRelease> = {}): GhRelease => ({
  tag_name: tag,
  draft: false,
  prerelease: true,
  published_at: published,
  html_url: `https://github.com/u/r/releases/tag/${tag}`,
  body_html: `<p>notes ${tag}</p>`,
  assets,
  ...extra,
});

describe('pickRelease', () => {
  it('takes the newest release with Decimation.jar, pre-releases included', () => {
    const list = [rel('0.58.0', '2026-10-13T08:37:13Z'), rel('0.59.0', '2026-10-13T09:02:06Z')];
    expect(pickRelease(list)?.tag).toBe('0.59.0');
    expect(pickRelease(list)?.prerelease).toBe(true);
  });

  it('skips a newer release without the jar', () => {
    const list = [rel('0.60.0', '2026-10-20T00:00:00Z', []), rel('0.59.0', '2026-10-13T09:02:06Z')];
    expect(pickRelease(list)?.tag).toBe('0.59.0');
  });

  it('skips drafts', () => {
    const list = [rel('0.61.0', '2026-10-21T00:00:00Z', undefined, { draft: true }), rel('0.59.0', '2026-10-13T09:02:06Z')];
    expect(pickRelease(list)?.tag).toBe('0.59.0');
  });

  it('needs the exact asset name', () => {
    const wrong = { ...jar('0.62.0'), name: 'decimation.jar' };
    expect(pickRelease([rel('0.62.0', '2026-10-22T00:00:00Z', [wrong])])).toBeNull();
  });

  it('links the browser download, not the API asset url', () => {
    const latest = pickRelease([rel('0.59.0', '2026-10-13T09:02:06Z')]);
    expect(latest?.downloadUrl).toBe('https://github.com/u/r/releases/download/0.59.0/Decimation.jar');
    expect(latest?.size).toBe(240980301);
    expect(latest?.pageUrl).toBe('https://github.com/u/r/releases/tag/0.59.0');
  });

  it('finds the sha256 asset when there is one', () => {
    const withSha = pickRelease([rel('0.59.1', '2026-10-14T00:00:00Z', [jar('0.59.1'), sha('0.59.1')])]);
    const without = pickRelease([rel('0.59.0', '2026-10-13T09:02:06Z')]);
    expect(withSha?.sha256Url).toBe('https://github.com/u/r/releases/download/0.59.1/Decimation.jar.sha256');
    expect(without?.sha256Url).toBeNull();
  });

  it('gives null for an empty list', () => {
    expect(pickRelease([])).toBeNull();
  });
});

describe('recentReleases', () => {
  it('lists the newest published releases first, drafts out, at most n', () => {
    const list = [
      rel('a', '2026-01-01T00:00:00Z'), rel('b', '2026-02-01T00:00:00Z'), rel('c', '2026-03-01T00:00:00Z'),
      rel('d', null, undefined, { draft: true }), rel('e', '2026-04-01T00:00:00Z'),
    ];
    expect(recentReleases(list, 3).map((r) => r.tag)).toEqual(['e', 'c', 'b']);
  });

  it('keeps releases without the jar and gives an empty html for missing notes', () => {
    const out = recentReleases([rel('x', '2026-05-01T00:00:00Z', [], { body_html: null })]);
    expect(out).toEqual([{ tag: 'x', publishedAt: '2026-05-01T00:00:00Z', prerelease: true, html: '', pageUrl: 'https://github.com/u/r/releases/tag/x' }]);
  });
});

describe('format', () => {
  it('writes sizes in decimal megabytes', () => {
    expect(formatSize(240980301)).toBe('241 MB');
    expect(formatSize(0)).toBe('0 MB');
  });

  it('writes dates as day month year in UTC', () => {
    expect(formatDate('2026-10-13T09:02:06Z')).toBe('13 October 2026');
    expect(formatDate('2026-10-13T23:59:00Z')).toBe('13 October 2026');
  });
});
```

- [ ] **Step 2: Run them, see them fail**

Run: `npm test -- tests/releases.test.ts` Expected: FAIL, cannot resolve `@/lib/releases`.

- [ ] **Step 3: Write `lib/releases.ts`**

```ts
/** GitHub release data for the page (docs/website.md section 5 in the mod repo). */

export type GhAsset = { name: string; size: number; browser_download_url: string; url?: string };

export type GhRelease = {
  tag_name: string;
  draft: boolean;
  prerelease: boolean;
  published_at: string | null;
  html_url: string;
  body_html?: string | null;
  assets: GhAsset[];
};

export type LatestRelease = {
  tag: string;
  publishedAt: string;
  prerelease: boolean;
  size: number;
  downloadUrl: string;
  sha256Url: string | null;
  pageUrl: string;
};

export type ChangelogEntry = { tag: string; publishedAt: string; prerelease: boolean; html: string; pageUrl: string };

const JAR = 'Decimation.jar';
const SHA = 'Decimation.jar.sha256';

/** Published releases (no drafts), newest first. */
function published(list: GhRelease[]): GhRelease[] {
  return list
    .filter((r) => !r.draft && r.published_at)
    .sort((a, b) => Date.parse(b.published_at as string) - Date.parse(a.published_at as string));
}

/** The newest release that carries Decimation.jar: the same rule as the game's updater (fixes/Updater). */
export function pickRelease(list: GhRelease[]): LatestRelease | null {
  for (const r of published(list)) {
    const jar = r.assets.find((a) => a.name === JAR);
    if (!jar) {
      continue;
    }
    const sha = r.assets.find((a) => a.name === SHA);
    return {
      tag: r.tag_name,
      publishedAt: r.published_at as string,
      prerelease: r.prerelease,
      size: jar.size,
      downloadUrl: jar.browser_download_url,
      sha256Url: sha ? sha.browser_download_url : null,
      pageUrl: r.html_url,
    };
  }
  return null;
}

/** The newest n published releases for the changelog, with GitHub's rendered notes. */
export function recentReleases(list: GhRelease[], n = 5): ChangelogEntry[] {
  return published(list)
    .slice(0, n)
    .map((r) => ({
      tag: r.tag_name,
      publishedAt: r.published_at as string,
      prerelease: r.prerelease,
      html: r.body_html ?? '',
      pageUrl: r.html_url,
    }));
}

export function formatSize(bytes: number): string {
  return `${Math.round(bytes / 1_000_000)} MB`;
}

const DATE = new Intl.DateTimeFormat('en-GB', { day: 'numeric', month: 'long', year: 'numeric', timeZone: 'UTC' });

export function formatDate(iso: string): string {
  return DATE.format(new Date(iso));
}
```

- [ ] **Step 4: Run them, see them pass**

Run: `npm test -- tests/releases.test.ts` Expected: all 11 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add lib/releases.ts tests/releases.test.ts && git commit -m "Release picker: newest release with Decimation.jar, changelog entries, size and date formats"
```

### Task 3 (A3): Release data loader with the build fallback (test first)

**Files:**
- Create: `lib/github.ts`, `lib/load.ts`
- Test: `tests/load.test.ts`

**Interfaces:**
- Consumes: `GhRelease`, `pickRelease`, `recentReleases`, `LatestRelease`, `ChangelogEntry` (Task A2).
- Produces:
  - `DEFAULT_API` (string), `fetchReleases(): Promise<GhRelease[]>` (throws on HTTP errors and non lists)
  - `type ReleaseData = { latest: LatestRelease | null; changelog: ChangelogEntry[]; ok: boolean }`
  - `loadReleaseData(): Promise<ReleaseData>`: on a GitHub failure returns `{ latest: null, changelog: [], ok: false }` while `NEXT_PHASE === 'phase-production-build'`, throws otherwise.

- [ ] **Step 1: Write the failing tests**

Create `tests/load.test.ts`:
```ts
// @vitest-environment node
import { afterEach, describe, expect, it, vi } from 'vitest';
import { DEFAULT_API } from '@/lib/github';
import { loadReleaseData } from '@/lib/load';

const release = {
  tag_name: '0.59.0',
  draft: false,
  prerelease: true,
  published_at: '2026-10-13T09:02:06Z',
  html_url: 'https://github.com/u/r/releases/tag/0.59.0',
  body_html: '<p>notes</p>',
  assets: [{ name: 'Decimation.jar', size: 240980301, browser_download_url: 'https://github.com/u/r/releases/download/0.59.0/Decimation.jar' }],
};

afterEach(() => {
  vi.unstubAllGlobals();
  vi.unstubAllEnvs();
});

describe('loadReleaseData', () => {
  it('returns the latest release and the changelog', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify([release]), { status: 200 })));
    const d = await loadReleaseData();
    expect(d.ok).toBe(true);
    expect(d.latest?.tag).toBe('0.59.0');
    expect(d.changelog.map((c) => c.tag)).toEqual(['0.59.0']);
  });

  it('asks GitHub with the headers it needs and a 600 s cache', async () => {
    const f = vi.fn().mockResolvedValue(new Response('[]', { status: 200 }));
    vi.stubGlobal('fetch', f);
    await loadReleaseData();
    const [url, init] = f.mock.calls[0];
    expect(url).toBe(DEFAULT_API);
    expect(init.headers.Accept).toBe('application/vnd.github.html+json');
    expect(init.headers['User-Agent']).toBe('deciremnant');
    expect(init.headers['X-GitHub-Api-Version']).toBe('2022-11-28');
    expect(init.next).toEqual({ revalidate: 600 });
    expect(init.headers.Authorization).toBeUndefined();
  });

  it('sends the token and the override url when set', async () => {
    vi.stubEnv('GITHUB_TOKEN', 'abc');
    vi.stubEnv('RELEASES_API', 'http://127.0.0.1:9/releases');
    const f = vi.fn().mockResolvedValue(new Response('[]', { status: 200 }));
    vi.stubGlobal('fetch', f);
    await loadReleaseData();
    expect(f.mock.calls[0][0]).toBe('http://127.0.0.1:9/releases');
    expect(f.mock.calls[0][1].headers.Authorization).toBe('Bearer abc');
  });

  it('falls back while next build runs and GitHub refuses (rate limit)', async () => {
    vi.stubEnv('NEXT_PHASE', 'phase-production-build');
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('rate limited', { status: 403 })));
    expect(await loadReleaseData()).toEqual({ latest: null, changelog: [], ok: false });
  });

  it('throws at run time, so ISR keeps the last good page', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('network down')));
    await expect(loadReleaseData()).rejects.toThrow('network down');
  });

  it('throws on an answer that is not a list', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('{"message":"Not Found"}', { status: 200 })));
    await expect(loadReleaseData()).rejects.toThrow('not a list');
  });
});
```

- [ ] **Step 2: Run them, see them fail**

Run: `npm test -- tests/load.test.ts` Expected: FAIL, cannot resolve `@/lib/github`.

- [ ] **Step 3: Write `lib/github.ts`**

```ts
import type { GhRelease } from './releases';

export const DEFAULT_API = 'https://api.github.com/repos/ucilmenangis/decimation-singleplayer/releases?per_page=10';

/** The mod's release list from GitHub, cached 600 s on the server (about 6 requests an hour in all). */
export async function fetchReleases(): Promise<GhRelease[]> {
  const headers: Record<string, string> = {
    Accept: 'application/vnd.github.html+json',
    'X-GitHub-Api-Version': '2022-11-28',
    'User-Agent': 'deciremnant',
  };
  if (process.env.GITHUB_TOKEN) {
    headers.Authorization = `Bearer ${process.env.GITHUB_TOKEN}`;
  }
  const res = await fetch(process.env.RELEASES_API ?? DEFAULT_API, { headers, next: { revalidate: 600 } });
  if (!res.ok) {
    throw new Error(`GitHub releases: HTTP ${res.status}`);
  }
  const data: unknown = await res.json();
  if (!Array.isArray(data)) {
    throw new Error('GitHub releases: not a list');
  }
  return data as GhRelease[];
}
```

- [ ] **Step 4: Write `lib/load.ts`**

```ts
import { fetchReleases } from './github';
import { pickRelease, recentReleases, type ChangelogEntry, type LatestRelease } from './releases';

export type ReleaseData = { latest: LatestRelease | null; changelog: ChangelogEntry[]; ok: boolean };

/**
 * Release data for the page. While `next build` runs a GitHub failure gives an empty result, so the build passes and
 * the button falls back to the releases page. At run time it throws: Next.js then keeps serving the last good page
 * ("If an error is thrown while attempting to revalidate data, the last successfully generated data will continue to
 * be served from the cache", Next.js ISR guide).
 */
export async function loadReleaseData(): Promise<ReleaseData> {
  try {
    const list = await fetchReleases();
    return { latest: pickRelease(list), changelog: recentReleases(list), ok: true };
  } catch (e) {
    if (process.env.NEXT_PHASE === 'phase-production-build') {
      console.warn('[deciremnant] release data unavailable during the build:', e);
      return { latest: null, changelog: [], ok: false };
    }
    throw e;
  }
}
```

- [ ] **Step 5: Run them, see them pass**

Run: `npm test` Expected: all tests PASS (smoke, releases, load).

- [ ] **Step 6: Commit**

```bash
git add lib/github.ts lib/load.ts tests/load.test.ts && git commit -m "GitHub release loader: 600 s cache, build fallback, run time errors keep the last page"
```

### Task 4 (A4): Game art and the font into the site repo

**Files:**
- Create (mod repo): `tools/site/export_assets.py`
- Create (site repo, by the tool): `public/icons/*.png`, `public/ui/panel.png`, `public/ui/mask.png`, `public/ui/dirt.png`, `public/shots/*.jpg`, `public/og.jpg`
- Create (site repo): `app/fonts/MinecraftStandard.otf`, `app/fonts/MinecraftStandard-readme.txt`

**Interfaces:**
- Produces: icon files named by Decimation item name (`bottlecap`, `map`, `firstaidkit`, `militarykeycard`, `radio`, `supplydropradio`, `bottlecapgold`, `achivement_trader_bandit`, `syringe_infection`, `infectedflesh`, `hugemilitarypack_green`, `graphics_card`, `mac10`); shot files `hero.jpg`, `city.jpg`, `highway.jpg`, `bridge.jpg`, `rail.jpg`, `gate.jpg`, `camp.jpg`, `refuge.jpg`, `base.jpg`, `airport.jpg`, `gun.jpg`.

- [ ] **Step 1: Write `tools/site/export_assets.py` (mod repo)**

```python
"""Copies the art of DeciRemnant, the project website, into its private repo (docs/website.md section 4).

    python3 tools/site/export_assets.py SITE_DIR                 # stand in screenshots from docs/shots
    python3 tools/site/export_assets.py SITE_DIR --shots DIR     # siteshots output: DIR/site_<name>.png

Item icons and the two torn menu textures come from Decimation.jar.original.bak, the MAC-10 icon is ours, the
dirt strip is generated (seeded noise). Screenshots become JPEG (at most 1920 wide) plus og.jpg (1200 x 630 from
the hero). Everything lands in SITE_DIR/public; nothing here is committed to this public repo.
"""
import argparse
import io
import os
import random
import sys
import zipfile

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
JAR = os.path.join(ROOT, "Decimation.jar.original.bak")
ITEMS = "assets/deci/textures/items/"
ICONS = {
    "bottlecap": ITEMS + "bottlecap.png",
    "map": ITEMS + "map.png",
    "firstaidkit": ITEMS + "firstaidkit.png",
    "militarykeycard": ITEMS + "militarykeycard.png",
    "radio": ITEMS + "radio.png",
    "supplydropradio": ITEMS + "supplydropradio.png",
    "bottlecapgold": ITEMS + "bottlecapgold.png",
    "achivement_trader_bandit": ITEMS + "achivement_trader_bandit.png",
    "syringe_infection": ITEMS + "syringe_infection.png",
    "infectedflesh": ITEMS + "infectedflesh.png",
    "hugemilitarypack_green": ITEMS + "hugemilitarypack_green.png",
    "graphics_card": ITEMS + "scraps/monitor/graphics_card.png",
}
UI = {"panel": "assets/deci/textures/gui/menu/buttonbg.png", "mask": "assets/deci/textures/gui/menu/buttonbg_red.png"}
MAC10 = os.path.join(ROOT, "dev/src/main/resources/assets/deci/textures/items/gun/smg/mac10.png")
SUBJECTS = ["hero", "city", "highway", "bridge", "rail", "gate", "camp", "refuge", "base", "airport", "gun"]
USER_CROP = (60, 150, 1700, 1073)  # the user's window shots: no title bar, no HUD
STAND_INS = {
    "hero": ("lc_superblock_v0.24.3/study_1.png", None),
    "city": ("lc_superblock_v0.24.3/study_2.png", None),
    "highway": ("lc_highway_v0.25.0b/study_3.png", None),
    "bridge": ("lc_bridge_v0.45.1/bridge_aerial.png", None),
    "rail": ("lc_rail_v0.44.0/rail_platform.png", None),
    "gate": ("wasteland_gate_v0.60.2_review/user_gate_1.png", USER_CROP),
    "camp": ("safezone_camp_v0.47.0_review/user_119.png", USER_CROP),
    "refuge": ("city_refuge_v0.48.0/refuge_market.png", None),
    "base": ("milbase_v0.42_review/user_99.png", USER_CROP),
    "airport": ("airport_v0.62.3/airport_helipad.png", None),
    "gun": ("mac10_v0.37/gunview_mac10.png", None),
}


def dirt(path):
    """Our own dirt strip: seeded noise in browns (not Mojang's texture)."""
    pal = [(115, 82, 56), (128, 92, 63), (101, 72, 49), (140, 101, 70), (88, 63, 42), (155, 118, 84)]
    weights = [30, 25, 20, 12, 8, 5]
    r = random.Random(7)
    im = Image.new("RGB", (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), r.choices(pal, weights)[0])
    im.save(path)


def shot(src, crop, dst):
    im = Image.open(src).convert("RGB")
    if crop:
        im = im.crop(crop)
    if im.width > 1920:
        im = im.resize((1920, round(im.height * 1920 / im.width)), Image.LANCZOS)
    im.save(dst, quality=85)
    return im


def og(hero, dst):
    w, h = hero.size
    target = 1200 / 630
    if w / h > target:
        cw = round(h * target)
        box = ((w - cw) // 2, 0, (w - cw) // 2 + cw, h)
    else:
        ch = round(w / target)
        box = (0, (h - ch) // 2, w, (h - ch) // 2 + ch)
    hero.crop(box).resize((1200, 630), Image.LANCZOS).save(dst, quality=85)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("site")
    ap.add_argument("--shots", help="folder with site_<name>.png from the siteshots dev test")
    a = ap.parse_args()
    pub = os.path.join(os.path.abspath(a.site), "public")
    for d in ("icons", "ui", "shots"):
        os.makedirs(os.path.join(pub, d), exist_ok=True)
    z = zipfile.ZipFile(JAR)
    for name, entry in ICONS.items():
        Image.open(io.BytesIO(z.read(entry))).save(os.path.join(pub, "icons", name + ".png"))
    for name, entry in UI.items():
        Image.open(io.BytesIO(z.read(entry))).save(os.path.join(pub, "ui", name + ".png"))
    Image.open(MAC10).save(os.path.join(pub, "icons", "mac10.png"))
    dirt(os.path.join(pub, "ui", "dirt.png"))
    missing = []
    hero = None
    for s in SUBJECTS:
        if a.shots:
            src, crop = os.path.join(a.shots, "site_%s.png" % s), None
        else:
            rel, crop = STAND_INS[s]
            src = os.path.join(ROOT, "docs", "shots", rel)
        if not os.path.isfile(src):
            missing.append(src)
            continue
        im = shot(src, crop, os.path.join(pub, "shots", s + ".jpg"))
        print("shot %-8s %4d x %-4d from %s" % (s, im.width, im.height, os.path.relpath(src, ROOT)))
        if s == "hero":
            hero = im
    if hero is not None:
        og(hero, os.path.join(pub, "og.jpg"))
    print("icons %d, ui 3, shots %d" % (len(ICONS) + 1, len(SUBJECTS) - len(missing)))
    if missing:
        print("MISSING:\n  " + "\n  ".join(missing))
        sys.exit(1)


if __name__ == "__main__":
    main()
```

- [ ] **Step 2: Run it with the stand ins**

Run: `cd ~/Projects/Decimation && python3 tools/site/export_assets.py ../deciremnant`
Expected: eleven `shot ...` lines, `icons 13, ui 3, shots 11`, exit 0.

- [ ] **Step 3: Check the output**

```bash
cd ~/Projects/deciremnant && python3 -c "
from PIL import Image; import glob
for f in sorted(glob.glob('public/icons/*.png')+glob.glob('public/ui/*.png')+['public/og.jpg']): print(f, Image.open(f).size)"
```
Expected: icons 16 x 16 or 32 x 32 (mac10 32 x 32), `panel.png` and `mask.png` 244 x 140, `dirt.png` 16 x 16, `og.jpg` 1200 x 630.

- [ ] **Step 4: The font (CC PD), into its own temp folder first**

```bash
T="$SCRATCH/font" && mkdir -p "$T" && curl -sL -o "$T/mcs.zip" "https://dl.dafont.com/dl/?f=minecraft_standard" && mkdir -p ~/Projects/deciremnant/app/fonts && \
python3 -I -c "import sys,zipfile; z=zipfile.ZipFile(sys.argv[1]); open(sys.argv[2],'wb').write(z.read('MinecraftStandard.otf')); open(sys.argv[3],'wb').write(z.read('readme.txt'))" \
  "$T/mcs.zip" ~/Projects/deciremnant/app/fonts/MinecraftStandard.otf ~/Projects/deciremnant/app/fonts/MinecraftStandard-readme.txt && \
grep -n "License: CC PD" ~/Projects/deciremnant/app/fonts/MinecraftStandard-readme.txt
```
Expected: the grep prints `5:License: CC PD` (any line number). If the line is missing, stop and tell the user.

- [ ] **Step 5: Commit both repos**

```bash
cd ~/Projects/Decimation && git add tools/site/export_assets.py && git commit -m "Website asset export tool: icons, torn menu textures, dirt strip, screenshots into the private site repo"
cd ~/Projects/deciremnant && git add public app/fonts && git commit -m "Game art and Minecraft Standard font (CC PD)"
```
Before the first commit check `git -C ~/Projects/Decimation status --short | grep -E 'png|jpg|otf'` prints nothing.

### Task 5 (A5): Content and the inline formatter (test first)

**Files:**
- Create: `content/types.ts`, `content/site.ts`, `content/features.ts`, `content/coming.ts`, `content/shots.ts`, `content/install.ts`, `content/help.ts`, `content/splashes.ts`, `lib/inline.tsx`
- Test: `tests/inline.test.tsx`, `tests/content.test.ts`

**Interfaces:**
- Produces:
  - `type Card = { icon: string; title: string; text: string; tag?: 'Planned' | 'Idea' }`
  - `type Shot = { file: string; caption: string }`
  - `type Step = { title: string; text: string }`, `type InstallTab = { label: string; steps: Step[]; note?: string }`
  - `type Requirement = { title: string; text: string; withSize?: boolean }`, `type Question = { q: string; a: string }`
  - `REPO`, `RELEASES_PAGE`, `ISSUES_PAGE`, `LEAD`, `FOOTER` (strings); `features: Card[]`, `coming: Card[]`, `shots: Shot[]`, `requirements: Requirement[]`, `installTabs: InstallTab[]`, `questions: Question[]`, `splashes: string[]`
  - `inline(text: string): ReactNode[]`

- [ ] **Step 1: Write the failing tests**

Create `tests/inline.test.tsx`:
```tsx
import { render } from '@testing-library/react';
import { expect, it } from 'vitest';
import { inline } from '@/lib/inline';

it('renders code and bold, keeps the rest as text', () => {
  const { container } = render(<p>{inline('Put `Decimation.jar` in **mods** now')}</p>);
  expect(container.innerHTML).toBe('<p>Put <code>Decimation.jar</code> in <b>mods</b> now</p>');
});

it('leaves plain text alone', () => {
  const { container } = render(<p>{inline('plain')}</p>);
  expect(container.innerHTML).toBe('<p>plain</p>');
});
```
Create `tests/content.test.ts`:
```ts
// @vitest-environment node
import { existsSync } from 'node:fs';
import { describe, expect, it } from 'vitest';
import { coming } from '@/content/coming';
import { features } from '@/content/features';
import { questions } from '@/content/help';
import { installTabs, requirements } from '@/content/install';
import { shots } from '@/content/shots';
import { FOOTER, LEAD } from '@/content/site';
import { splashes } from '@/content/splashes';

const all: string[] = [
  LEAD, FOOTER, ...splashes,
  ...features.flatMap((c) => [c.title, c.text]), ...coming.flatMap((c) => [c.title, c.text]),
  ...shots.map((s) => s.caption), ...requirements.flatMap((r) => [r.title, r.text]),
  ...installTabs.flatMap((t) => [t.label, t.note ?? '', ...t.steps.flatMap((s) => [s.title, s.text])]),
  ...questions.flatMap((q) => [q.q, q.a]),
];

describe('content', () => {
  it('has no dash punctuation (user rule)', () => {
    for (const s of all) {
      expect(s, s).not.toMatch(/[–—]| - /);
    }
  });

  it('shows 6 to 10 screenshots, each file present', () => {
    expect(shots.length).toBeGreaterThanOrEqual(6);
    expect(shots.length).toBeLessThanOrEqual(10);
    for (const s of shots) {
      expect(existsSync(`public/shots/${s.file}`), s.file).toBe(true);
    }
  });

  it('has every icon file', () => {
    for (const c of [...features, ...coming]) {
      expect(existsSync(`public/icons/${c.icon}.png`), c.icon).toBe(true);
    }
  });

  it('has six features and six coming soon cards with a tag each', () => {
    expect(features).toHaveLength(6);
    expect(coming).toHaveLength(6);
    expect(coming.every((c) => c.tag === 'Planned' || c.tag === 'Idea')).toBe(true);
  });

  it('carries the footer line verbatim', () => {
    expect(FOOTER).toBe('Unofficial fan project. Decimation belongs to BoehMod. Not an official Minecraft product; not approved by or associated with Mojang or Microsoft.');
  });
});
```

- [ ] **Step 2: Run them, see them fail**

Run: `npm test` Expected: FAIL, cannot resolve `@/lib/inline` and `@/content/...`.

- [ ] **Step 3: Write `lib/inline.tsx`**

```tsx
import { Fragment, type ReactNode } from 'react';

/** Renders `code` and **bold** inside a content string; everything else stays text. */
export function inline(text: string): ReactNode[] {
  const out: ReactNode[] = [];
  const re = /`([^`]+)`|\*\*([^*]+)\*\*/g;
  let last = 0;
  let k = 0;
  let m: RegExpExecArray | null;
  while ((m = re.exec(text)) !== null) {
    if (m.index > last) {
      out.push(<Fragment key={k++}>{text.slice(last, m.index)}</Fragment>);
    }
    out.push(m[1] !== undefined ? <code key={k++}>{m[1]}</code> : <b key={k++}>{m[2]}</b>);
    last = re.lastIndex;
  }
  if (last < text.length) {
    out.push(<Fragment key={k++}>{text.slice(last)}</Fragment>);
  }
  return out;
}
```

- [ ] **Step 4: Write the content files**

`content/types.ts`:
```ts
export type Card = { icon: string; title: string; text: string; tag?: 'Planned' | 'Idea' };
export type Shot = { file: string; caption: string };
export type Step = { title: string; text: string };
export type InstallTab = { label: string; steps: Step[]; note?: string };
export type Requirement = { title: string; text: string; withSize?: boolean };
export type Question = { q: string; a: string };
```
`content/site.ts`:
```ts
export const REPO = 'https://github.com/ucilmenangis/decimation-singleplayer';
export const RELEASES_PAGE = `${REPO}/releases`;
export const ISSUES_PAGE = `${REPO}/issues`;
export const LEAD =
  "Fixes that make BoehMod's zombie mod work without its dead servers, plus a whole generated world: cities, military bases, airports and safe zones.";
export const FOOTER =
  'Unofficial fan project. Decimation belongs to BoehMod. Not an official Minecraft product; not approved by or associated with Mojang or Microsoft.';
```
`content/features.ts`:
```ts
import type { Card } from './types';

export const features: Card[] = [
  { icon: 'bottlecap', title: 'Singleplayer that works', text: "Loot, zones, supply drops, armour and humanity, all without Decimation's dead servers." },
  { icon: 'map', title: 'Generated cities', text: '4 to 64 districts each, joined by highways, an underground railway and rivers.' },
  { icon: 'firstaidkit', title: 'Safe zones', text: 'Survivor camps and city refuges with traders, guards and a clinic.' },
  { icon: 'militarykeycard', title: 'Bases and airports', text: 'Military bases in three sizes and airfields in wasteland cities, locked rooms and loot inside.' },
  { icon: 'mac10', title: 'Soldiers, bandits, new guns', text: 'NPC tiers with real loadouts, plus the MAC-10, UMP9, HK416 and Mk18.' },
  { icon: 'radio', title: 'Updates in the game', text: 'When a new version is out, the menu button says Update.' },
];
```
`content/coming.ts`:
```ts
import type { Card } from './types';

/** Only items from docs/roadmap.md and docs/systems_plan.md of the mod repo; no dates. */
export const coming: Card[] = [
  { icon: 'bottlecapgold', title: 'Crate levels', text: 'Loot in four levels by place and danger, and bodies you can search.', tag: 'Planned' },
  { icon: 'achivement_trader_bandit', title: 'Smarter people', text: 'Police survivors, and NPCs that take cover, flank and pull back to heal.', tag: 'Planned' },
  { icon: 'syringe_infection', title: 'Infection in stages', text: 'Five stages from the bite to the end, and the dead get back up.', tag: 'Planned' },
  { icon: 'infectedflesh', title: 'Zombies that grow', text: 'More and stronger infected from day 1 to day 100, and horde nights.', tag: 'Planned' },
  { icon: 'hugemilitarypack_green', title: 'A survivor partner', text: 'A friend who follows you, wears the gear you give and carries a backpack.', tag: 'Idea' },
  { icon: 'graphics_card', title: 'Community servers', text: "Servers listed on this site and in Decimation's own server menu.", tag: 'Planned' },
];
```
`content/shots.ts` (order = grid places g1..g10; g1 and g8 are double size):
```ts
import type { Shot } from './types';

export const shots: Shot[] = [
  { file: 'city.jpg', caption: 'Generated city: towers, offices and shops on real streets' },
  { file: 'highway.jpg', caption: 'Highways between cities' },
  { file: 'bridge.jpg', caption: 'Rivers and bridges' },
  { file: 'rail.jpg', caption: 'Underground railway' },
  { file: 'gate.jpg', caption: 'Wasteland gate' },
  { file: 'camp.jpg', caption: 'Survivor camp' },
  { file: 'refuge.jpg', caption: 'City refuge market' },
  { file: 'base.jpg', caption: 'A forward operating base at sunset' },
  { file: 'airport.jpg', caption: 'Military airport' },
  { file: 'gun.jpg', caption: 'The MAC-10, our first gun' },
];
```
`content/install.ts`:
```ts
import type { InstallTab, Requirement } from './types';

export const requirements: Requirement[] = [
  { title: 'Account', text: 'Minecraft Java Edition' },
  { title: 'Java', text: 'Java 8 (Prism Launcher can download it)' },
  { title: 'Memory', text: '4 GB for the game or more' },
  { title: 'Download', text: 'One file, Decimation.jar', withSize: true },
];

export const installTabs: InstallTab[] = [
  {
    label: 'New player',
    steps: [
      { title: 'Install Prism Launcher', text: 'Get it from prismlauncher.org (Windows, macOS, Linux) and add your Microsoft account.' },
      { title: 'Make a Forge 1.7.10 instance', text: 'Add Instance, pick version 1.7.10, then Mod loader: Forge, version 10.13.4.1614. Name it Decimation.' },
      { title: 'Java and memory', text: 'Edit the instance, Settings: choose Java 8 and set the maximum memory to 4096 MB or more.' },
      { title: 'Add the mod', text: 'Download `Decimation.jar` above. Edit the instance, Mods, Add file, and pick it.' },
      { title: 'First start', text: "Launch. The first start takes a little longer. Decimation's own menu opens: click Play." },
      { title: 'Create the world', text: 'Singleplayer, Create New World, More World Options, then click World Type until it says **Decimation**. Create.' },
    ],
  },
  {
    label: 'Already have Decimation',
    steps: [
      { title: 'Back up', text: 'Close the game and copy your `saves` folder somewhere safe.' },
      { title: 'Swap the jar', text: "In the instance's `mods` folder, replace `Decimation.jar` with the new one. Keep the name." },
      { title: 'Remove old parts', text: 'Delete any `deciworldgen` jar: it is inside Decimation.jar now. The voice chat jar is optional.' },
      { title: 'Play', text: 'Old worlds keep working. Cities, bases and airports appear in new worlds with the world type Decimation.' },
    ],
  },
  {
    label: 'Updating',
    steps: [
      { title: 'Update button', text: "When a newer version is out, Decimation's Play button says Update." },
      { title: 'Download', text: 'Click it. The new jar downloads with a progress bar and is checked against its checksum.' },
      { title: 'Restart', text: 'Choose Quit game and start again, or Later: the new version goes in when you close the game.' },
    ],
    note: 'Or download the jar here and swap it by hand, as in "Already have Decimation".',
  },
  {
    label: 'Server',
    steps: [
      { title: 'Forge server', text: 'In an empty folder, with Java 8: `java -jar forge-1.7.10-10.13.4.1614-1.7.10-installer.jar --installServer`' },
      { title: 'Add the mod', text: "Put `Decimation.jar` in the server's `mods` folder." },
      { title: 'World type', text: 'In `server.properties` set `level-type=decimation` before the first start.' },
      { title: 'Start', text: '`java -Xmx4G -jar forge-1.7.10-10.13.4.1614-1.7.10-universal.jar nogui`, accept the EULA in `eula.txt`, start again. Players join with the same Decimation.jar.' },
    ],
  },
];
```
`content/help.ts`:
```ts
import type { Question } from './types';

export const questions: Question[] = [
  { q: 'The menu button says Update', a: 'A newer version is out. Click it, wait for the download, then start the game again.' },
  { q: 'My old world has no cities', a: 'Cities, bases and airports appear in new worlds made with the world type Decimation.' },
  { q: 'The game lags in cities', a: 'Lower the prop render distance in `config/deciworldgen_props.cfg`.' },
  { q: 'Found a bug?', a: 'Open an issue on GitHub and attach `logs/fml-client-latest.log`.' },
];
```
`content/splashes.ts`:
```ts
export const splashes: string[] = ['Now with airports!', 'Up to 64 districts!', 'No dead servers!', 'Mind the landmines!', 'Traders are open!'];
```

- [ ] **Step 5: Run the tests, see them pass**

Run: `npm test` Expected: all PASS (inline 2, content 5 and the earlier ones).

- [ ] **Step 6: Commit**

```bash
git add content lib/inline.tsx tests/inline.test.tsx tests/content.test.ts && git commit -m "Page texts in content/ and the inline code and bold formatter"
```

### Task 6 (A6): Styles, font and layout

**Files:**
- Modify: `app/layout.tsx`, `app/globals.css`

**Interfaces:**
- Produces: CSS classes used by Task A7: `wrap mc s2 s3 s4 s6 sh2 sh3 sh4 sh6 label steps hero hero-img hero-over nav brand links menu splash lead btns btn-dl btn-guide meta section note cards card card-top slot tag-soon idea gallery shot g1..g10 lightbox install req tabs tab on panel step n changelog notes tag footer dirt foot`; CSS variable `--font-mc`.

- [ ] **Step 1: Write `app/layout.tsx`**

```tsx
import type { Metadata } from 'next';
import localFont from 'next/font/local';
import './globals.css';

const mc = localFont({ src: './fonts/MinecraftStandard.otf', variable: '--font-mc', display: 'swap' });

export const metadata: Metadata = {
  metadataBase: new URL('https://deciremnant.vercel.app'),
  title: 'DeciRemnant: Decimation, alive again in singleplayer',
  description:
    "Fixes that make BoehMod's zombie mod Decimation work without its dead servers, plus a whole generated world: cities, military bases, airports and safe zones. Unofficial fan project.",
  openGraph: {
    title: 'DeciRemnant',
    description: 'Decimation, alive again in singleplayer.',
    images: [{ url: '/og.jpg', width: 1200, height: 630 }],
  },
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en" className={mc.variable}>
      <body>{children}</body>
    </html>
  );
}
```

- [ ] **Step 2: Write `app/globals.css`**

```css
:root {
  --bg: #1C1815; --card: #26211D; --text: #EDE6DA; --muted: #B9AE9E; --dim: #9A8F80; --label: #E08A5A; --link: #E59468; --line: #332B25;
  --dl: #F89838; --dl-hi: #FFB868; --dl-lo: #A85E18; --dl-shadow: #3E260E; --splash: #FFFF55; --idea: #55FFFF; --slot: #15110F;
}
* { box-sizing: border-box; }
html { scroll-behavior: smooth; }
body { margin: 0; background: var(--bg); color: var(--text); font: 15px/1.6 -apple-system, system-ui, "Segoe UI", Roboto, sans-serif; }
a { color: var(--link); text-decoration: none; }
a:hover { text-decoration: underline; }
:focus-visible { outline: 2px solid var(--dl); outline-offset: 2px; }
.wrap { max-width: 880px; margin: 0 auto; padding: 0 28px; }

/* the game's font at the game's sizes: one font pixel = font size / 6 (GUI scale 2, 3, 4, 6) */
.mc { font-family: var(--font-mc), ui-monospace, monospace; font-weight: 400; line-height: 1.5; -webkit-font-smoothing: none; font-smooth: never; }
.s2 { font-size: 12px; } .s3 { font-size: 18px; } .s4 { font-size: 24px; } .s6 { font-size: 36px; }
.sh2 { text-shadow: 2px 2px 0 #3F3F3F; } .sh3 { text-shadow: 3px 3px 0 #3F3F3F; } .sh4 { text-shadow: 4px 4px 0 #3F3F3F; } .sh6 { text-shadow: 6px 6px 0 #3F3F3F; }
.label { color: var(--label); text-shadow: 2px 2px 0 #382216; margin: 0; }
.steps { clip-path: polygon(0 4px, 4px 4px, 4px 0, calc(100% - 4px) 0, calc(100% - 4px) 4px, 100% 4px, 100% calc(100% - 4px), calc(100% - 4px) calc(100% - 4px), calc(100% - 4px) 100%, 4px 100%, 4px calc(100% - 4px), 0 calc(100% - 4px)); }

/* hero */
.hero { position: relative; height: 440px; overflow: hidden; }
.hero-img { object-fit: cover; filter: saturate(.85) brightness(.82); }
.hero::after { content: ""; position: absolute; inset: 0; pointer-events: none; background: linear-gradient(to bottom, rgba(28,24,21,.5) 0%, rgba(28,24,21,.05) 40%, rgba(28,24,21,.85) 100%); }
.nav { position: absolute; z-index: 3; top: 0; left: 0; right: 0; display: flex; justify-content: space-between; align-items: center; padding: 16px 28px; }
.brand { color: #fff; white-space: nowrap; }
.links { display: flex; gap: 18px; white-space: nowrap; }
.links a { color: #E4DCCF; }
.menu { display: none; }
.hero-over { position: absolute; z-index: 2; left: 0; right: 0; bottom: 26px; }
.hero h1 { margin: 6px 0 0; color: #fff; max-width: 680px; font-weight: 400; }
.splash { position: absolute; z-index: 3; right: 40px; bottom: 196px; color: var(--splash); text-shadow: 3px 3px 0 #3F3F15; background: none; border: 0; padding: 0; cursor: pointer; white-space: nowrap; transform: rotate(-20deg); animation: splash .55s ease-in-out infinite alternate; }
@keyframes splash { from { transform: rotate(-20deg) scale(1); } to { transform: rotate(-20deg) scale(1.08); } }
@media (prefers-reduced-motion: reduce) { .splash { animation: none; } }

/* download */
.lead { font-size: 18px; line-height: 1.6; color: var(--muted); margin: 30px 0 0; max-width: 660px; }
.btns { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 22px; align-items: center; }
.btn-dl { display: inline-flex; align-items: center; gap: 10px; background: var(--dl); color: #fff; text-shadow: 3px 3px 0 var(--dl-shadow); padding: 12px 22px 16px;
  box-shadow: inset 0 -5px 0 var(--dl-lo), inset 0 3px 0 var(--dl-hi);
  -webkit-mask: url(/ui/mask.png) center / 100% 100% no-repeat; mask: url(/ui/mask.png) center / 100% 100% no-repeat; }
.btn-guide { display: inline-flex; align-items: center; color: #fff; padding: 14px 22px 18px; background: url(/ui/panel.png) center / 100% 100% no-repeat; }
.btn-dl:hover, .btn-guide:hover { filter: brightness(1.08); text-decoration: none; }
.btn-dl img { image-rendering: pixelated; }
.meta { font-size: 13px; color: var(--dim); margin-top: 12px; }

/* sections and cards */
.section { padding-top: 64px; }
.section h2 { margin: 4px 0 22px; font-weight: 400; }
.note { margin: 12px 0 0; font-size: 13px; color: var(--dim); }
.cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.card { background: var(--card); padding: 16px 16px 18px; }
.card-top { display: flex; align-items: center; gap: 12px; }
.card h3 { margin: 0; font-weight: 400; }
.card p { margin: 10px 0 0; font-size: 14px; color: var(--muted); line-height: 1.5; }
.slot { width: 40px; height: 40px; flex: none; display: flex; align-items: center; justify-content: center; background: var(--slot); box-shadow: inset 2px 2px 0 #0B0908, inset -2px -2px 0 #3B332C; }
.slot img { image-rendering: pixelated; }
.tag-soon { display: inline-block; margin-top: 10px; color: var(--splash); text-shadow: 2px 2px 0 #3F3F15; }
.tag-soon.idea { color: var(--idea); text-shadow: 2px 2px 0 #153F3F; }

/* gallery */
.gallery { display: grid; grid-template-columns: repeat(4, 1fr); grid-auto-rows: 132px; gap: 10px; }
.shot { position: relative; overflow: hidden; padding: 0; border: 0; background: var(--card); cursor: zoom-in; text-align: left; }
.shot img { object-fit: cover; transition: transform .25s; }
.shot:hover img { transform: scale(1.04); }
.shot span { position: absolute; left: 0; right: 0; bottom: 0; padding: 18px 10px 7px; color: #fff; background: linear-gradient(to bottom, rgba(0,0,0,0), rgba(0,0,0,.75)); }
.g1 { grid-column: 1 / span 2; grid-row: 1 / span 2; } .g2 { grid-column: 3; grid-row: 1; } .g3 { grid-column: 4; grid-row: 1; }
.g4 { grid-column: 3; grid-row: 2; } .g5 { grid-column: 4; grid-row: 2; } .g6 { grid-column: 1; grid-row: 3; } .g7 { grid-column: 2; grid-row: 3; }
.g8 { grid-column: 3 / span 2; grid-row: 3 / span 2; } .g9 { grid-column: 1; grid-row: 4; } .g10 { grid-column: 2; grid-row: 4; }
.lightbox { position: fixed; inset: 0; z-index: 50; background: rgba(10,8,7,.92); display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 14px; cursor: zoom-out; padding: 16px; }
.lightbox img { max-width: 88vw; max-height: 78vh; width: auto; height: auto; }
.lightbox p { color: #fff; margin: 0; }

/* install */
.req { background: var(--card); padding: 14px 18px; display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 18px; }
.req div { font-size: 13.5px; color: var(--muted); }
.req b { display: block; color: var(--text); margin-bottom: 2px; }
.tabs { display: flex; flex-wrap: wrap; gap: 6px; }
.tab { padding: 9px 14px 12px; background: #221D19; color: var(--dim); border: 0; cursor: pointer; box-shadow: inset 0 -3px 0 #171310; }
.tab.on { background: var(--card); color: #fff; box-shadow: none; }
.panel { background: var(--card); padding: 8px 18px 18px; }
.step { display: grid; grid-template-columns: 92px 1fr; gap: 14px; padding: 14px 0; border-bottom: 1px solid var(--line); }
.step:last-of-type { border-bottom: 0; }
.step .n { color: var(--label); text-shadow: 2px 2px 0 #382216; padding-top: 2px; }
.step h3 { margin: 0 0 4px; font-weight: 400; }
.step p { margin: 0; font-size: 14px; color: var(--muted); line-height: 1.55; }
code { font: 12.5px ui-monospace, Menlo, monospace; background: var(--bg); padding: 1px 5px; color: var(--text); overflow-wrap: anywhere; }

/* changelog and help */
details { background: var(--card); margin-bottom: 8px; padding: 12px 16px; }
summary { cursor: pointer; list-style: none; }
summary::-webkit-details-marker { display: none; }
summary::before { content: "+"; display: inline-block; width: 20px; color: var(--label); }
details[open] summary::before { content: "\2212"; }
.notes, details p { margin: 8px 0 2px 20px; font-size: 14px; color: var(--muted); }
.notes ul { padding-left: 18px; }
.tag { font: 12px -apple-system, system-ui, sans-serif; color: var(--dim); margin-left: 8px; text-shadow: none; -webkit-font-smoothing: auto; }

/* footer */
.footer { margin-top: 70px; }
.dirt { height: 28px; background: linear-gradient(rgba(0,0,0,.35), rgba(0,0,0,.35)), url(/ui/dirt.png) 0 0 / 40px; image-rendering: pixelated; }
.foot { background: #14110F; padding: 22px 0 26px; font-size: 13px; color: var(--dim); }
.foot .wrap { display: flex; justify-content: space-between; gap: 20px; }

/* phones: one column, menu button, game font one scale down where it would not fit */
@media (max-width: 720px) {
  .wrap { padding: 0 16px; }
  .nav { padding: 12px 16px; }
  .menu { display: inline-block; background: none; border: 0; color: #fff; cursor: pointer; }
  .links { display: none; position: absolute; top: 48px; right: 16px; flex-direction: column; gap: 10px; background: var(--card); padding: 14px 18px; }
  .links.open { display: flex; }
  .hero { height: 340px; }
  .hero h1.s6 { font-size: 24px; text-shadow: 4px 4px 0 #3F3F3F; }
  .splash.s3 { font-size: 12px; text-shadow: 2px 2px 0 #3F3F15; right: 16px; bottom: 150px; }
  .btn-dl.s3, .btn-guide.s3 { font-size: 12px; }
  .btn-dl { text-shadow: 2px 2px 0 var(--dl-shadow); }
  .btn-dl img { display: none; }
  .btn-guide.sh3 { text-shadow: 2px 2px 0 #3F3F3F; }
  .cards { grid-template-columns: 1fr; }
  .gallery { grid-template-columns: 1fr 1fr; grid-auto-rows: 110px; }
  .gallery .shot { grid-column: auto; grid-row: auto; }
  .req { grid-template-columns: 1fr 1fr; }
  .step { grid-template-columns: 1fr; gap: 4px; }
  .foot .wrap { flex-direction: column; }
}
```

- [ ] **Step 3: Build**

Run: `npm run build && npm run typecheck` Expected: build passes (the page is still empty), no type errors.

- [ ] **Step 4: Commit**

```bash
git add app/layout.tsx app/globals.css && git commit -m "Layout with the Minecraft font and the warm night styles"
```

### Task 7 (A7): Components (tests first where they act)

**Files:**
- Create: `components/Nav.tsx`, `Hero.tsx`, `Splash.tsx`, `Download.tsx`, `Section.tsx`, `CardGrid.tsx`, `Gallery.tsx`, `Install.tsx`, `Changelog.tsx`, `Help.tsx`, `Footer.tsx`
- Test: `tests/components.test.tsx`

**Interfaces:**
- Consumes: `LatestRelease`, `ChangelogEntry`, `formatSize`, `formatDate` (A2); content types and data (A5); `inline` (A5); CSS classes (A6).
- Produces (default exports): `Nav()`, `Hero()`, `Splash({ lines: string[] })`, `Download({ latest: LatestRelease | null })`, `Section({ id, label, title, children })`, `CardGrid({ cards: Card[] })`, `Gallery({ shots: Shot[] })`, `Install({ requirements: Requirement[]; tabs: InstallTab[]; jarSize: string | null })`, `Changelog({ entries: ChangelogEntry[] })`, `Help({ items: Question[] })`, `Footer()`.

- [ ] **Step 1: Write the failing tests**

Create `tests/components.test.tsx`:
```tsx
import { act, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Changelog from '@/components/Changelog';
import Download from '@/components/Download';
import Gallery from '@/components/Gallery';
import Install from '@/components/Install';
import Splash from '@/components/Splash';
import { installTabs, requirements } from '@/content/install';
import { RELEASES_PAGE } from '@/content/site';
import type { LatestRelease } from '@/lib/releases';

const latest: LatestRelease = {
  tag: '0.59.0',
  publishedAt: '2026-10-13T09:02:06Z',
  prerelease: true,
  size: 240980301,
  downloadUrl: 'https://github.com/u/r/releases/download/0.59.0/Decimation.jar',
  sha256Url: null,
  pageUrl: 'https://github.com/u/r/releases/tag/0.59.0',
};

afterEach(() => vi.restoreAllMocks());

describe('Download', () => {
  it('links the jar with version and size', () => {
    render(<Download latest={latest} />);
    const a = screen.getByRole('link', { name: /Download 0\.59\.0 · 241 MB/ });
    expect(a.getAttribute('href')).toBe(latest.downloadUrl);
    expect(screen.getByText(/Pre-release · 13 October 2026/)).toBeDefined();
  });

  it('falls back to the releases page without data', () => {
    render(<Download latest={null} />);
    expect(screen.getByRole('link', { name: /Download from GitHub/ }).getAttribute('href')).toBe(RELEASES_PAGE);
  });
});

describe('Changelog', () => {
  it('opens the newest and says when notes are missing', () => {
    const { container } = render(
      <Changelog entries={[
        { tag: '0.59.0', publishedAt: '2026-10-13T09:02:06Z', prerelease: true, html: '<p>Auto update</p>', pageUrl: 'x' },
        { tag: '0.58.0', publishedAt: '2026-10-13T08:37:13Z', prerelease: true, html: '', pageUrl: 'y' },
      ]} />,
    );
    const details = container.querySelectorAll('details');
    expect(details[0].open).toBe(true);
    expect(details[1].open).toBe(false);
    expect(screen.getByText('Auto update')).toBeDefined();
    expect(screen.getByText('No notes for this release.')).toBeDefined();
  });

  it('shows only the link when there are no releases', () => {
    const { container } = render(<Changelog entries={[]} />);
    expect(container.querySelectorAll('details')).toHaveLength(0);
    expect(screen.getByRole('link', { name: 'Every release on GitHub' })).toBeDefined();
  });
});

describe('Install', () => {
  it('shows the first tab and switches tabs', () => {
    render(<Install requirements={requirements} tabs={installTabs} jarSize="241 MB" />);
    expect(screen.getByText('Install Prism Launcher').closest('[role=tabpanel]')?.hasAttribute('hidden')).toBe(false);
    expect(screen.getByText('Forge server').closest('[role=tabpanel]')?.hasAttribute('hidden')).toBe(true);
    fireEvent.click(screen.getByRole('tab', { name: 'Server' }));
    expect(screen.getByText('Forge server').closest('[role=tabpanel]')?.hasAttribute('hidden')).toBe(false);
    expect(screen.getByText(/One file, Decimation\.jar, 241 MB/)).toBeDefined();
  });
});

describe('Gallery', () => {
  it('opens a shot large and closes on Escape', () => {
    render(<Gallery shots={[{ file: 'city.jpg', caption: 'Generated city' }, { file: 'rail.jpg', caption: 'Underground railway' }]} />);
    fireEvent.click(screen.getByRole('button', { name: 'Open: Underground railway' }));
    expect(screen.getByRole('dialog', { name: 'Underground railway' })).toBeDefined();
    fireEvent.keyDown(window, { key: 'Escape' });
    expect(screen.queryByRole('dialog')).toBeNull();
  });
});

describe('Splash', () => {
  it('starts on the first line, picks a random one after mount, next on click', async () => {
    vi.spyOn(Math, 'random').mockReturnValue(0.5);
    render(<Splash lines={['a', 'b', 'c', 'd']} />);
    expect(screen.getByRole('button').textContent).toBe('a');
    await act(async () => {
      await new Promise((r) => requestAnimationFrame(() => r(null)));
    });
    expect(screen.getByRole('button').textContent).toBe('c');
    fireEvent.click(screen.getByRole('button'));
    expect(screen.getByRole('button').textContent).toBe('d');
  });
});
```

- [ ] **Step 2: Run them, see them fail**

Run: `npm test -- tests/components.test.tsx` Expected: FAIL, cannot resolve `@/components/...`.

- [ ] **Step 3: Write the components**

`components/Section.tsx`:
```tsx
import type { ReactNode } from 'react';

export default function Section({ id, label, title, children }: { id: string; label: string; title: string; children: ReactNode }) {
  return (
    <section id={id} className="section">
      <p className="label mc s2">{label}</p>
      <h2 className="mc s4 sh4">{title}</h2>
      {children}
    </section>
  );
}
```
`components/Splash.tsx`:
```tsx
'use client';
import { useEffect, useState } from 'react';

/** The title screen's yellow splash text: a random line per visit, the next one on a click. */
export default function Splash({ lines }: { lines: string[] }) {
  const [i, setI] = useState(0);
  useEffect(() => {
    // after the first paint, so the server page and the first client render match (no hydration error)
    const id = requestAnimationFrame(() => setI(Math.floor(Math.random() * lines.length)));
    return () => cancelAnimationFrame(id);
  }, [lines.length]);
  return (
    <button type="button" className="splash mc s3" onClick={() => setI((i + 1) % lines.length)}>
      {lines[i]}
    </button>
  );
}
```
`components/Nav.tsx`:
```tsx
'use client';
import { useState } from 'react';
import { REPO } from '@/content/site';

const LINKS: [string, string][] = [['#features', 'Features'], ['#screenshots', 'Screenshots'], ['#install', 'Install'], ['#changelog', 'Changelog']];

export default function Nav() {
  const [open, setOpen] = useState(false);
  return (
    <nav className="nav">
      <a className="brand mc s3 sh3" href="#top">DeciRemnant</a>
      <button type="button" className="menu mc s2 sh2" aria-expanded={open} aria-controls="navlinks" onClick={() => setOpen(!open)}>
        Menu
      </button>
      <div id="navlinks" className={open ? 'links open' : 'links'}>
        {LINKS.map(([href, label]) => (
          <a key={href} className="mc s2 sh2" href={href} onClick={() => setOpen(false)}>{label}</a>
        ))}
        <a className="mc s2 sh2" href={REPO}>GitHub</a>
      </div>
    </nav>
  );
}
```
`components/Hero.tsx`:
```tsx
import Image from 'next/image';
import { splashes } from '@/content/splashes';
import Nav from './Nav';
import Splash from './Splash';

export default function Hero() {
  return (
    <header id="top" className="hero">
      <Image className="hero-img" src="/shots/hero.jpg" alt="A generated city seen from above" fill priority sizes="100vw" />
      <Nav />
      <div className="hero-over">
        <div className="wrap">
          <p className="label mc s2">Minecraft 1.7.10 · unofficial</p>
          <h1 className="mc s6 sh6">Decimation, alive again in singleplayer</h1>
        </div>
      </div>
      <Splash lines={splashes} />
    </header>
  );
}
```
`components/Download.tsx`:
```tsx
import Image from 'next/image';
import { LEAD, RELEASES_PAGE } from '@/content/site';
import { formatDate, formatSize, type LatestRelease } from '@/lib/releases';

export default function Download({ latest }: { latest: LatestRelease | null }) {
  return (
    <div className="download">
      <p className="lead">{LEAD}</p>
      <div className="btns">
        <a className="btn-dl mc s3" href={latest ? latest.downloadUrl : RELEASES_PAGE}>
          <Image src="/icons/supplydropradio.png" alt="" width={32} height={32} unoptimized />
          {latest ? `Download ${latest.tag} · ${formatSize(latest.size)}` : 'Download from GitHub'}
        </a>
        <a className="btn-guide mc s3 sh3" href="#install">Install guide &gt;&gt;</a>
      </div>
      <p className="meta">
        {latest ? `${latest.prerelease ? 'Pre-release · ' : ''}${formatDate(latest.publishedAt)} · ` : ''}
        Minecraft 1.7.10 with Forge · <a href={RELEASES_PAGE}>All versions on GitHub</a>
      </p>
    </div>
  );
}
```
`components/CardGrid.tsx`:
```tsx
import Image from 'next/image';
import type { Card } from '@/content/types';

export default function CardGrid({ cards }: { cards: Card[] }) {
  return (
    <div className="cards">
      {cards.map((c) => (
        <article key={c.title} className="card steps">
          <div className="card-top">
            <span className="slot"><Image src={`/icons/${c.icon}.png`} alt="" width={32} height={32} unoptimized /></span>
            <h3 className="mc s2 sh2">{c.title}</h3>
          </div>
          <p>{c.text}</p>
          {c.tag && <span className={c.tag === 'Idea' ? 'tag-soon idea mc s2' : 'tag-soon mc s2'}>{c.tag}</span>}
        </article>
      ))}
    </div>
  );
}
```
`components/Gallery.tsx`:
```tsx
'use client';
import Image from 'next/image';
import { useEffect, useState } from 'react';
import type { Shot } from '@/content/types';

export default function Gallery({ shots }: { shots: Shot[] }) {
  const [open, setOpen] = useState<number | null>(null);
  useEffect(() => {
    if (open === null) {
      return;
    }
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setOpen(null);
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [open]);
  const big = open === null ? null : shots[open];
  return (
    <>
      <div className="gallery">
        {shots.map((s, i) => (
          <button key={s.file} type="button" className={`shot g${i + 1}`} aria-label={`Open: ${s.caption}`} onClick={() => setOpen(i)}>
            <Image src={`/shots/${s.file}`} alt={s.caption} fill sizes="(max-width: 720px) 50vw, 25vw" />
            <span className="mc s2 sh2">{s.caption}</span>
          </button>
        ))}
      </div>
      {big && (
        <div className="lightbox" role="dialog" aria-modal="true" aria-label={big.caption} onClick={() => setOpen(null)}>
          <Image src={`/shots/${big.file}`} alt={big.caption} width={1920} height={1080} sizes="88vw" />
          <p className="mc s3 sh3">{big.caption}</p>
        </div>
      )}
    </>
  );
}
```
`components/Install.tsx`:
```tsx
'use client';
import { useState } from 'react';
import type { InstallTab, Requirement } from '@/content/types';
import { inline } from '@/lib/inline';

export default function Install({ requirements, tabs, jarSize }: { requirements: Requirement[]; tabs: InstallTab[]; jarSize: string | null }) {
  const [tab, setTab] = useState(0);
  return (
    <div className="install">
      <div className="req steps">
        {requirements.map((r) => (
          <div key={r.title}>
            <b className="mc s2 sh2">{r.title}</b>
            {r.text}
            {r.withSize && jarSize ? `, ${jarSize}` : ''}
          </div>
        ))}
      </div>
      <div className="tabs" role="tablist">
        {tabs.map((t, i) => (
          <button key={t.label} type="button" role="tab" aria-selected={i === tab} className={i === tab ? 'tab on mc s2' : 'tab mc s2'} onClick={() => setTab(i)}>
            {t.label}
          </button>
        ))}
      </div>
      {tabs.map((t, i) => (
        <div key={t.label} role="tabpanel" className="panel" hidden={i !== tab}>
          {t.steps.map((s, k) => (
            <div key={s.title} className="step">
              <span className="n mc s2">Step {k + 1}</span>
              <div>
                <h3 className="mc s2 sh2">{s.title}</h3>
                <p>{inline(s.text)}</p>
              </div>
            </div>
          ))}
          {t.note && <p className="note">{inline(t.note)}</p>}
        </div>
      ))}
    </div>
  );
}
```
`components/Changelog.tsx`:
```tsx
import { RELEASES_PAGE } from '@/content/site';
import { formatDate, type ChangelogEntry } from '@/lib/releases';

/** GitHub renders and sanitises the notes (body_html); only our own releases are shown. */
export default function Changelog({ entries }: { entries: ChangelogEntry[] }) {
  return (
    <div className="changelog">
      {entries.map((e, i) => (
        <details key={e.tag} open={i === 0}>
          <summary className="mc s2 sh2">
            {e.tag} <span className="tag">{formatDate(e.publishedAt)}{e.prerelease ? ' · pre-release' : ''}</span>
          </summary>
          {e.html ? <div className="notes" dangerouslySetInnerHTML={{ __html: e.html }} /> : <p className="notes">No notes for this release.</p>}
        </details>
      ))}
      <p className="meta"><a href={RELEASES_PAGE}>Every release on GitHub</a></p>
    </div>
  );
}
```
`components/Help.tsx`:
```tsx
import { ISSUES_PAGE } from '@/content/site';
import type { Question } from '@/content/types';
import { inline } from '@/lib/inline';

export default function Help({ items }: { items: Question[] }) {
  return (
    <div className="help">
      {items.map((q) => (
        <details key={q.q}>
          <summary className="mc s2 sh2">{q.q}</summary>
          <p>{inline(q.a)}</p>
        </details>
      ))}
      <p className="meta"><a href={ISSUES_PAGE}>Report a bug</a></p>
    </div>
  );
}
```
`components/Footer.tsx`:
```tsx
import { FOOTER, REPO } from '@/content/site';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="dirt" />
      <div className="foot">
        <div className="wrap">
          <span>{FOOTER}</span>
          <span><a href={REPO}>GitHub</a></span>
        </div>
      </div>
    </footer>
  );
}
```

- [ ] **Step 4: Run the tests, see them pass**

Run: `npm test` Expected: all PASS (components 7 and the earlier ones). If `next/image` fails in jsdom, add to `vitest.config.mts` `test.server.deps.inline: ['next']` and run again.

- [ ] **Step 5: Commit**

```bash
git add components tests/components.test.tsx && git commit -m "Page components: hero, splash, download, cards, gallery, install tabs, changelog, help, footer"
```

### Task 8 (A8): Put the page together and check it like a visitor

**Files:**
- Modify: `app/page.tsx`
- Create: `scripts/check-links.mjs`

**Interfaces:**
- Consumes: `loadReleaseData` (A3), all components (A7), content (A5).
- Produces: the finished page at `/`, ISR every 600 s; `node scripts/check-links.mjs URL` (exit 1 on a bad link).

- [ ] **Step 1: Write `app/page.tsx`**

```tsx
import CardGrid from '@/components/CardGrid';
import Changelog from '@/components/Changelog';
import Download from '@/components/Download';
import Footer from '@/components/Footer';
import Gallery from '@/components/Gallery';
import Help from '@/components/Help';
import Hero from '@/components/Hero';
import Install from '@/components/Install';
import Section from '@/components/Section';
import { coming } from '@/content/coming';
import { features } from '@/content/features';
import { questions } from '@/content/help';
import { installTabs, requirements } from '@/content/install';
import { shots } from '@/content/shots';
import { loadReleaseData } from '@/lib/load';
import { formatSize } from '@/lib/releases';

/** Rebuilt from GitHub's release list at most every 10 minutes; a failed rebuild keeps the last page. */
export const revalidate = 600;

export default async function Page() {
  const data = await loadReleaseData();
  return (
    <>
      <Hero />
      <main className="wrap">
        <Download latest={data.latest} />
        <Section id="features" label="What it adds" title="A world worth surviving in">
          <CardGrid cards={features} />
        </Section>
        <Section id="screenshots" label="In the game" title="Screenshots">
          <Gallery shots={shots} />
        </Section>
        <Section id="coming" label="Coming soon" title="What we are building next">
          <CardGrid cards={coming} />
          <p className="note">Plans, not promises: no dates.</p>
        </Section>
        <Section id="install" label="Install" title="Installation guide">
          <Install requirements={requirements} tabs={installTabs} jarSize={data.latest ? formatSize(data.latest.size) : null} />
        </Section>
        <Section id="changelog" label="Changelog" title="What changed">
          <Changelog entries={data.changelog} />
        </Section>
        <Section id="help" label="Help" title="Questions">
          <Help items={questions} />
        </Section>
      </main>
      <Footer />
    </>
  );
}
```

- [ ] **Step 2: Unit tests, lint, types, build**

Run: `npm test && npm run lint && npm run typecheck && npm run build`
Expected: tests PASS, no lint or type errors, the build table marks `/` as static with revalidate 10m (`○ /  10m` or similar).

- [ ] **Step 3: The build survives GitHub being down**

Run: `RELEASES_API=http://127.0.0.1:9/releases npm run build 2>&1 | tail -20`
Expected: the build passes and logs `[deciremnant] release data unavailable during the build`. Then start the server in the background, one process with its pid kept:
`node node_modules/next/dist/bin/next start -p 3000 > "$SCRATCH/next.log" 2>&1 & echo $! > "$SCRATCH/next.pid"`;
`curl -s localhost:3000 | grep -o 'Download from GitHub'` prints it once; stop it with `kill $(cat "$SCRATCH/next.pid")`. If the build FAILS instead, `NEXT_PHASE` is not visible where the page prerenders: log `process.env.NEXT_PHASE` in the catch of `lib/load.ts`, rebuild, and fix the phase test from what it prints before going on.

- [ ] **Step 4: Normal build and server for the checks**

Run: `npm run build`, then `node node_modules/next/dist/bin/next start -p 3000 > "$SCRATCH/next.log" 2>&1 & echo $! > "$SCRATCH/next.pid"`; wait until `curl -s -o /dev/null -w '%{http_code}' localhost:3000` prints 200. Stop it after Step 7 with `kill $(cat "$SCRATCH/next.pid")`.

- [ ] **Step 5: Desktop and phone pictures, read them**

```bash
agent-browser open http://localhost:3000 && agent-browser set viewport 1300 900 && agent-browser screenshot --full "$SCRATCH/site_desktop.png"
agent-browser set viewport 390 844 && agent-browser screenshot --full "$SCRATCH/site_phone.png"
agent-browser eval "document.documentElement.scrollWidth"
```
Expected: `scrollWidth` prints 390 or less. Read both pictures (crop long pages into parts with Pillow if needed): every section present in order, hero text clear of the splash, cards with crisp icons, gallery, install tabs, changelog with the real 0.59.0 notes, help, footer. Fix and rebuild until right.

- [ ] **Step 6: Text, fonts, console**

```bash
agent-browser set viewport 1300 900
agent-browser eval "(document.body.innerText.match(/[–—]| - /g) || []).length"
agent-browser eval "[...new Set([...document.querySelectorAll('.mc')].map(e => getComputedStyle(e).fontSize))].join(',')"
agent-browser errors
agent-browser console
```
Expected: dash count 0; font sizes only from `12px,18px,24px,36px`; no page errors, no hydration warning in the console.

- [ ] **Step 7: Every link answers**

Create `scripts/check-links.mjs`:
```js
// Checks every link of a running page: same page anchors exist, other links answer 2xx or 3xx.
const base = process.argv[2] ?? 'http://localhost:3000/';
const html = await (await fetch(base)).text();
const hrefs = [...new Set([...html.matchAll(/href="([^"]+)"/g)].map((m) => m[1].replaceAll('&amp;', '&')))];
let bad = 0;
for (const href of hrefs) {
  if (href.startsWith('#')) {
    const ok = html.includes(`id="${href.slice(1)}"`);
    console.log(ok ? 'ok  ' : 'BAD ', href);
    bad += ok ? 0 : 1;
    continue;
  }
  const url = new URL(href, base);
  let status = -1;
  try {
    status = (await fetch(url, { method: 'HEAD', redirect: 'manual' })).status;
  } catch {
    status = -1;
  }
  const ok = status >= 200 && status < 400;
  console.log(ok ? 'ok  ' : 'BAD ', status, url.href);
  bad += ok ? 0 : 1;
}
console.log(bad ? `${bad} bad link(s)` : 'all links ok');
process.exit(bad ? 1 : 0);
```
Run: `node scripts/check-links.mjs http://localhost:3000/` Expected: `all links ok`; the `Decimation.jar` download link answers 302 (GitHub's redirect to the file).

- [ ] **Step 8: Commit**

```bash
git add app/page.tsx scripts/check-links.mjs && git commit -m "The page put together: release data, sections, link checker"
```

- [ ] **Step 9 (slow, once): a failed refresh keeps the last page**

With the normal build from Step 4 still built: start the server with a dead API (`RELEASES_API=http://127.0.0.1:9/x node node_modules/next/dist/bin/next start -p 3000 > "$SCRATCH/stale.log" 2>&1 & echo $! > "$SCRATCH/next.pid"`), request the page once, wait 610 s in the background (Bash `run_in_background`, then the notification), request it twice more with `curl -s -D - localhost:3000 -o "$SCRATCH/page.html"`. Expected: both answers 200, `Download 0.59.0` still in `page.html` (the build's data), header `x-nextjs-cache` STALE or HIT. Stop it with `kill $(cat "$SCRATCH/next.pid")`.

### Task 9 (A9): Check the install guide's facts (spec section 9)

**Files:**
- Modify: `content/install.ts` (only where a check finds a difference); in the mod repo `docs/website.md` section 9

**Interfaces:**
- Consumes: `installTabs` (A5).

- [ ] **Step 1: Forge file names**

```bash
for f in installer universal; do curl -s -o /dev/null -I -w "%{http_code} $f\n" "https://maven.minecraftforge.net/net/minecraftforge/forge/1.7.10-10.13.4.1614-1.7.10/forge-1.7.10-10.13.4.1614-1.7.10-$f.jar"; done
```
Expected: `200 installer` and `200 universal`. Another status: open the folder listing `https://maven.minecraftforge.net/net/minecraftforge/forge/1.7.10-10.13.4.1614-1.7.10/`, take the real names and fix the Server tab in `content/install.ts`.

- [ ] **Step 2: Prism Launcher's own words**

Read Prism's getting started and mods pages (WebFetch `https://prismlauncher.org/wiki/getting-started/` and the pages it links for creating an instance, Java and adding mods). Confirm: "Add Instance", the Mod loader choice with Forge versions, "Edit" on an instance, its Settings with Java and memory, the Mods tab with "Add file", and whether Prism downloads Java 8 itself. Change any step text in `content/install.ts` to Prism's wording. Never open Prism itself.

- [ ] **Step 3: What Decimation's Play button opens**

Read docs/decimation_code/menus.md and the home menu's Play action in the readable tree (`deobf/src`, GuiMenuHome = deci/i/e). If Play opens Minecraft's world list directly, change "New player" step 6 to start with "Click Play, then Create New World"; if it opens the vanilla title menu, keep "Singleplayer". Write what the code does into docs/website.md section 9.

- [ ] **Step 4: Tests and commits**

Run: `npm test` Expected: PASS (the content test still finds no dash punctuation).
```bash
cd ~/Projects/deciremnant && git add content/install.ts && git commit -m "Install guide checked against Forge's files, Prism's wording and Decimation's Play button"
cd ~/Projects/Decimation && git add docs/website.md && git commit -m "Website spec: install guide facts checked"
```
(Skip a commit when nothing changed.)

---

## Part B: the `siteshots` dev test (this repo)

### Task 10 (B1): Window size for the dev client

**Files:**
- Modify: `dev/dependencies.gradle` (append), `tools/devtest.py:116-135` (live start)

**Interfaces:**
- Produces: `./gradlew runClient -Pwindow=1920x1080` (Minecraft `--width 1920 --height 1080`); `-Pshots=a,b` sets `deciworldgen.autotest.shots`; `python3 tools/devtest.py --live --window=1920x1080 MODE` starts the live game at that size.

- [ ] **Step 1: Gradle properties (append to `dev/dependencies.gradle`)**

```groovy
// `gradlew runClient -Pwindow=1920x1080`: game window size (website screenshots, docs/website.md section 6)
tasks.matching { it.name.startsWith("runClient") }.configureEach {
    if (project.hasProperty("window")) {
        def wh = project.property("window").toString().split("x")
        args("--width", wh[0], "--height", wh[1])
    }
}

// `-Pshots=hero,gun`: the siteshots dev test shoots only these rows of tools/site/siteshots.tsv
tasks.matching { it.name.startsWith("runClient") }.configureEach {
    if (project.hasProperty("shots")) {
        jvmArgs("-Ddeciworldgen.autotest.shots=" + project.property("shots"))
    }
}
```

- [ ] **Step 2: `--window=WxH` in `tools/devtest.py` `run_live`, after the `dress` lines**

```python
        windows = [a[len("--window="):] for a in sys.argv if a.startswith("--window=")]
        if windows:  # window size of a new live game (website shots: docs/website.md section 6)
            start += ["-Pwindow=" + windows[0]]
```
Add to the module docstring usage: `python3 tools/devtest.py --live --window=1920x1080 MODE   # start the live game with that window size`.

- [ ] **Step 3: Measure what the window really gives (spike)**

Check for a running game as its own call: `pgrep -fl "zulu-8.jdk.*GradleStart"`. Ours (has `-Ddeciworldgen.autotest`): `python3 tools/devtest.py --live --stop`, wait for it to exit. The user's: stop and ask.
Run: `python3 tools/devtest.py --live --window=1920x1080 cityview`, then
`python3 -c "from PIL import Image; print(Image.open('dev/run/client/screenshots/cityview_props_on_1.png').size)"`.
Expected: `(1920, 1080)`. If it is still `(854, 480)`, the run task dropped the arguments: read how RetroFuturaGradle's run task builds Minecraft's program arguments (`./gradlew help --task runClient` and its RunMinecraftTask source) and pass `--width` / `--height` through that instead, then measure again. If it is bigger than 854 x 480 but smaller than asked (macOS keeps windows inside the screen), note the size it gave; the shots use the largest 16:9 size that works (try 1600x900, then 1440x810) and docs/website.md section 6 gets that size.

- [ ] **Step 4: Commit**

```bash
git add dev/dependencies.gradle tools/devtest.py && git commit -m "Dev client window size (-Pwindow, devtest.py --window) and -Pshots for the website screenshots"
```

### Task 11 (B2): The `siteshots` mode

**Files:**
- Create: `dev/src/main/java/net/decimation/worldgen/devtest/SiteShotsTest.java`, `tools/site/siteshots.tsv`
- Modify: `dev/src/main/java/net/decimation/worldgen/DevAutoTest.java` (method `mode`, beside the `cityview` line)

**Interfaces:**
- Consumes: `DevTestMode`, `DevTestUtil.player()`, `DevTestResults.shot/value/check`, `-Pwindow`, `-Pshots` (B1).
- Produces: mode `siteshots`; files `dev/run/client/screenshots/site_<name>.png`; TSV format `name<TAB>x<TAB>y<TAB>z<TAB>yaw<TAB>pitch<TAB>time` (y absolute or `~N` = N above the ground; yaw 0 south, 90 west, 180 north, 270 east; pitch positive looks down; time in ticks, 6000 noon, 12500 sunset).

- [ ] **Step 1: A first TSV with two rows to prove the mode**

`tools/site/siteshots.tsv` (tabs between columns):
```
# DeciRemnant website screenshots (docs/website.md section 6), seed 1 world type Decimation, fresh world
# name	x	y	z	yaw	pitch	time      y "~N" = N above the ground; yaw 0 south 90 west 180 north 270 east
hero	3	151	-9	0	25	6000
gun	3	~2	-9	0	0	6000
```

- [ ] **Step 2: Write `SiteShotsTest.java`**

```java
package net.decimation.worldgen.devtest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.decimation.worldgen.DecimationWorldGen;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

/**
 * Screenshots for the project website (docs/website.md section 6): one shot per row of tools/site/siteshots.tsv
 * (name, x, y, z, yaw, pitch, time; y "~N" = N above the ground). The server holds the player there flying, no rain,
 * the row's time of day; rows with "~" wait 100 ticks high above first so the ground is generated. The client waits
 * until the chunks are drawn (no chunk updates for 3 s, at least 10 s, at most 60 s) and saves
 * screenshots/site_<name>.png with the GUI hidden. Row "gun" holds our MAC-10: the hand stays, the HUD is cancelled.
 * -Pshots=a,b shoots only those rows; -Pwindow=1920x1080 sets the window size.
 */
public class SiteShotsTest extends DevTestMode
{
    public String name() { return "siteshots"; }

    /** Shots need the current generation everywhere: a fresh seed 1 world. */
    public boolean freshWorld() { return true; }

    private static final int MIN_WAIT = 200, QUIET = 60, MAX_WAIT = 1200, GROUND_WAIT = 100;

    /** One camera row of the TSV. */
    static final class Row
    {
        final String name, y;
        final double x, z;
        final float yaw, pitch;
        final long time;

        Row(String[] c)
        {
            name = c[0];
            x = Double.parseDouble(c[1]);
            y = c[2];
            z = Double.parseDouble(c[3]);
            yaw = Float.parseFloat(c[4]);
            pitch = Float.parseFloat(c[5]);
            time = Long.parseLong(c[6]);
        }

        boolean relative()
        {
            return y.startsWith("~");
        }
    }

    static List<Row> read(File f, String only) throws IOException
    {
        List<String> keep = only == null || only.isEmpty() ? null : Arrays.asList(only.split(","));
        List<Row> rows = new ArrayList<Row>();
        for (String line : Files.readAllLines(f.toPath(), StandardCharsets.UTF_8))
        {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
            {
                continue;
            }
            String[] c = line.split("\t");
            if (c.length < 7)
            {
                throw new IOException("siteshots.tsv needs 7 columns: " + line);
            }
            Row r = new Row(c);
            if (keep == null || keep.contains(r.name))
            {
                rows.add(r);
            }
        }
        return rows;
    }

    /** Hides the HUD (not the hand) while a "gun" row is shot. */
    public static final class HudHider
    {
        volatile boolean active;

        @SubscribeEvent
        public void overlay(RenderGameOverlayEvent.Pre e)
        {
            if (active && e.type == RenderGameOverlayEvent.ElementType.ALL)
            {
                e.setCanceled(true);
            }
        }
    }

    private static HudHider hud;
    private volatile List<Row> rows;
    private volatile int index;
    private volatile int placed = -1;
    private int serverRow = -1, serverT, waited, quiet;
    private double groundY;
    private float savedFov, savedGamma;
    private int savedDistance;
    private boolean savedClouds, saved;

    public boolean client(Minecraft mc)
    {
        if (rows == null)
        {
            String only = System.getProperty("deciworldgen.autotest.shots");
            File tsv = new File(mc.mcDataDir, "../../../tools/site/siteshots.tsv");
            try
            {
                rows = read(tsv.getCanonicalFile(), only);
            }
            catch (IOException e)
            {
                DevTestResults.check(name(), "siteshots.tsv", e.getMessage(), false, "a readable tools/site/siteshots.tsv");
                return false;
            }
            if (hud == null)
            {
                hud = new HudHider();
                MinecraftForge.EVENT_BUS.register(hud);
            }
        }
        if (!saved)
        {
            saved = true;
            savedFov = mc.gameSettings.fovSetting;
            savedGamma = mc.gameSettings.gammaSetting;
            savedDistance = mc.gameSettings.renderDistanceChunks;
            savedClouds = mc.gameSettings.clouds;
        }
        int i = index;
        if (i >= rows.size())
        {
            // the dev client is also used for play: leave its options as they were
            mc.gameSettings.fovSetting = savedFov;
            mc.gameSettings.gammaSetting = savedGamma;
            mc.gameSettings.renderDistanceChunks = savedDistance;
            mc.gameSettings.clouds = savedClouds;
            mc.gameSettings.hideGUI = false;
            hud.active = false;
            return false;
        }
        Row r = rows.get(i);
        boolean gun = "gun".equals(r.name);
        mc.gameSettings.fovSetting = 70.0F;
        mc.gameSettings.gammaSetting = 0.6F;
        mc.gameSettings.renderDistanceChunks = 16;
        mc.gameSettings.clouds = true;
        mc.gameSettings.hideGUI = !gun;
        hud.active = gun;
        if (mc.thePlayer == null || placed < i)
        {
            waited = 0;
            quiet = 0;
            return true;
        }
        if (gun)
        {
            mc.thePlayer.inventory.currentItem = 0;
        }
        // hold the camera: a touched mouse or touchpad must not turn the shot
        mc.thePlayer.rotationYaw = mc.thePlayer.prevRotationYaw = r.yaw;
        mc.thePlayer.rotationPitch = mc.thePlayer.prevRotationPitch = r.pitch;
        waited++;
        quiet = chunkUpdates(mc) == 0 ? quiet + 1 : 0;
        if (waited < MIN_WAIT || quiet < QUIET && waited < MAX_WAIT)
        {
            return true;
        }
        String file = "site_" + r.name + ".png";
        net.minecraft.util.ScreenShotHelper.saveScreenshot(mc.mcDataDir, file, mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
        File out = new File(mc.mcDataDir, "screenshots/" + file);
        DevTestResults.shot(name(), file);
        DevTestResults.value(name(), "shot " + r.name, mc.displayWidth + " x " + mc.displayHeight + ", waited " + waited
                             + " ticks" + (quiet < QUIET ? " (chunks still drawing)" : ""));
        DevTestResults.check(name(), "saved " + r.name, out.isFile(), out.isFile(), "a png in screenshots/");
        FMLLog.info("[%s] siteshots %s at %.1f %s %.1f yaw %.0f pitch %.0f", DecimationWorldGen.MODID, r.name, r.x, r.y, r.z, r.yaw, r.pitch);
        index = i + 1;
        return true;
    }

    /** "60 fps, 12 chunk updates": the chunk updates of the last second. */
    private static int chunkUpdates(Minecraft mc)
    {
        String d = mc.debug;
        int a = d.indexOf(", "), b = d.indexOf(" chunk");
        return a < 0 || b < a ? 0 : Integer.parseInt(d.substring(a + 2, b).trim());
    }

    public void server()
    {
        EntityPlayerMP p = DevTestUtil.player();
        List<Row> list = rows;
        int i = index;
        if (p == null || list == null || i >= list.size())
        {
            return;
        }
        Row r = list.get(i);
        if (serverRow != i)
        {
            serverRow = i;
            serverT = 0;
            p.capabilities.allowFlying = true;
            p.capabilities.isFlying = true;
            p.sendPlayerAbilities();
            p.worldObj.setWorldTime(r.time);
            p.worldObj.getWorldInfo().setRaining(false);
            p.worldObj.getWorldInfo().setThundering(false);
            if ("gun".equals(r.name))
            {
                giveMac10(p);
            }
        }
        serverT++;
        p.worldObj.setWorldTime(r.time); // hold the light while the client waits
        double y;
        if (r.relative())
        {
            if (serverT < GROUND_WAIT)
            {
                y = 200; // above everything while the chunks around generate
            }
            else
            {
                if (serverT == GROUND_WAIT)
                {
                    groundY = p.worldObj.getHeightValue((int) Math.floor(r.x), (int) Math.floor(r.z));
                }
                y = groundY + Double.parseDouble(r.y.substring(1));
            }
        }
        else
        {
            y = Double.parseDouble(r.y);
        }
        p.playerNetServerHandler.setPlayerLocation(r.x, y, r.z, r.yaw, r.pitch);
        if (!r.relative() || serverT > GROUND_WAIT)
        {
            placed = i;
        }
    }

    private static void giveMac10(EntityPlayerMP p)
    {
        Object o = Item.itemRegistry.getObject("deci:mac10");
        if (o == null)
        {
            o = Item.itemRegistry.getObject("deciworldgen:mac10");
        }
        if (o == null)
        {
            DevTestResults.check("siteshots", "MAC-10 item", "missing", false, "deci:mac10 or deciworldgen:mac10");
            return;
        }
        ItemStack s = new ItemStack((Item) o);
        s.stackTagCompound = new NBTTagCompound(); // gun data ready before the first frame
        s.stackTagCompound.setInteger("ammo", 0);
        p.inventory.currentItem = 0;
        p.inventory.mainInventory[0] = s;
        p.inventoryContainer.detectAndSendChanges();
    }
}
```

- [ ] **Step 3: Register the mode in `DevAutoTest.mode`, next to the cityview line**

```java
        if (name.equals("siteshots")) return new net.decimation.worldgen.devtest.SiteShotsTest();
```

- [ ] **Step 4: Compile**

Run: `cd dev && ./gradlew compileJava -q` Expected: exit 0.

- [ ] **Step 5: First run in a fresh world (one launch)**

Check for a running game as its own call (Task B1 step 3 rule), stop ours if needed. Run:
`python3 tools/devtest.py siteshots -Pwindow=1920x1080`
Expected: results `saved hero PASS`, `saved gun PASS`, `shot hero 1920 x 1080, waited ...` (or the size B1 measured); read `dev/run/client/screenshots/site_hero.png` and `site_gun.png`: no GUI in the hero shot, the MAC-10 in hand and no HUD in the gun shot. If Decimation's own HUD (stats, top right) still shows in the gun shot, find how IngameHud draws (docs/decimation_code/menus.md, deci/g/j) and hide it in `HudHider` the same way, then run again.

- [ ] **Step 6: Commit**

```bash
git add dev/src/main/java/net/decimation/worldgen/devtest/SiteShotsTest.java dev/src/main/java/net/decimation/worldgen/DevAutoTest.java tools/site/siteshots.tsv
git commit -m "siteshots dev test: website screenshots from camera rows in tools/site/siteshots.tsv"
```

### Task 12 (B3): Find the subjects and shoot all eleven

**Files:**
- Create: `tools/site/subjects.py`
- Modify: `dev/src/main/java/net/decimation/worldgen/LargeSites.java` (method `militaryBase`), `dev/src/main/java/net/decimation/worldgen/city/LcCity.java` (where the CityRefugePlan is added), `tools/site/siteshots.tsv` (final rows), `docs/shots_index.md`, `docs/website.md` (section 6 status)

**Interfaces:**
- Consumes: the server log of a seed 1 run (`dev/run/server/logs/latest.log`), the `siteshots` mode (B2).
- Produces: eleven rows in the TSV (hero, city, highway, bridge, rail, gate, camp, refuge, base, airport, gun); `dev/run/client/screenshots/site_<name>.png` for each; keepers in `docs/shots/site_v<version>/`.

- [ ] **Step 1: One log line per planned military base and city refuge**

Bases and refuges log no position today (every placed thing should: one line with coordinates). In `LargeSites.militaryBase`, just before `return new net.decimation.worldgen.military.MilitaryBasePlan(...)`:
```java
        cpw.mods.fml.common.FMLLog.info("[deciworldgen] military base M%d_%d size %d planned at %d,%d (turns %d)",
                                        siteX, siteZ, size, x, z, turns);
```
In `LcCity`, right after `out.add(new net.decimation.worldgen.military.CityRefugePlan(...));`:
```java
            cpw.mods.fml.common.FMLLog.info("[deciworldgen] city refuge lcz_%d_%d planned at %d,%d (turns %d)",
                                            cellX, cellZ, bx + 16, bz + 16, refuge[2]);
```
Run: `cd dev && ./gradlew compileJava -q` Expected: exit 0. Logs only: no block changes.

- [ ] **Step 2: Write `tools/site/subjects.py`**

```python
"""Lists where the website's subjects are in a generated world, nearest to spawn first (docs/website.md section 6).

    python3 tools/site/subjects.py dev/run/server/logs/latest.log [N]

Reads our placement log lines (rail stations, bridges, wasteland gates, airports, highways, camps, city refuges,
military bases) and prints the N nearest of each kind (default 3) with their distance from 0,0. A rail station
stands in every city: the nearest one is where the city and hero shots go.
"""
import math
import re
import sys

PATTERNS = {
    "rail": re.compile(r"rail station at (-?\d+),(-?\d+),(-?\d+)"),
    "bridge": re.compile(r"lc bridge.* at (-?\d+),(-?\d+),(-?\d+)"),
    "gate": re.compile(r"lc (?:edge )?gate .* at (-?\d+),(-?\d+),(-?\d+)"),
    "airport": re.compile(r"lc airport at (-?\d+),(-?\d+)"),
    "highway": re.compile(r"highway .* at (-?\d+),(-?\d+),(-?\d+)"),
    "camp": re.compile(r"camp site \S+ planned at (-?\d+),(-?\d+)"),
    "base": re.compile(r"military base \S+ size \d planned at (-?\d+),(-?\d+)"),
    "refuge": re.compile(r"city refuge \S+ planned at (-?\d+),(-?\d+)"),
}


def main():
    log, n = sys.argv[1], int(sys.argv[2]) if len(sys.argv) > 2 else 3
    found = {k: [] for k in PATTERNS}
    for line in open(log, errors="replace"):
        for kind, pat in PATTERNS.items():
            m = pat.search(line)
            if not m:
                continue
            g = [int(v) for v in m.groups()]
            found[kind].append((g[0], None, g[1]) if len(g) == 2 else (g[0], g[1], g[2]))
    for kind, spots in found.items():
        spots = sorted(set(spots), key=lambda s: math.hypot(s[0], s[2]))[:n]
        print("%-8s %s" % (kind, "  ".join("%d,%s,%d (%d)" % (x, "?" if y is None else y, z, math.hypot(x, z)) for x, y, z in spots) or "none in this log"))


if __name__ == "__main__":
    main()
```

- [ ] **Step 3: Generate seed 1 on the server and list the subjects**

Run: `python3 tools/servertest.py 1 type=decimation pregen=0,0,40` then `python3 tools/site/subjects.py dev/run/server/logs/latest.log 3`.
Expected: at least one spot for every kind. A kind with "none in this log": rerun servertest with `pregen=` around a farther spot from `python3 tools/servertest.py 1 type=decimation gatescan=0,0,3000` (it lists gates and airport sites without generating), then list again. Write the chosen spots into a comment block at the top of `siteshots.tsv`.

- [ ] **Step 4: Camera rows for all eleven subjects**

For each subject add a row (the city and hero shots go to the city of the nearest rail station): street level shots `y = ~2` (eye height), views from above `y = ~40` to `~80` with pitch 25 to 45, yaw toward the subject, time 6000 except `base` at 12500 (sunset). Starting points: `city` on a street of the nearest city looking along it; `hero` above the same city looking over the towers; `bridge` from a bank looking along the bridge; `rail` on a station platform (y from the station line, absolute); `gate` from the street outside looking at the closed gate; `camp` inside the market street; `refuge` inside the market hall (absolute y of its ground floor + 2); `base` from above at an angle; `airport` from the apron toward the tower; `gun` on a city street.

- [ ] **Step 5: Shoot, read every picture, adjust, repeat**

Run (live, the fresh world from B2 step 5 stays; `--window` only applies when the live game starts, so stop a live game started without it): `python3 tools/devtest.py --live --window=1920x1080 siteshots` (all rows) or `... siteshots -Pshots=camp,refuge` for a few.
Read EVERY `site_<name>.png` (not only the sheet). A shot passes when: its subject fills the frame and reads at a glance, no GUI, no window bar, no floating test platform, no wall or ceiling filling the frame, chunks drawn to the horizon, daylight (base: sunset). Adjust the row and reshoot until all eleven pass.

- [ ] **Step 6: Keep the pictures and describe them**

Copy the eleven PNGs to `docs/shots/site_v<version>/` (version = the newest tag, `git describe --tags --abbrev=0`) and add a section `site_v<version>/` to `docs/shots_index.md` with one line per shot (what it shows, where). In `docs/website.md` section 6 write the size actually used and "shot <date>".

- [ ] **Step 7: Commit**

```bash
git add dev/src/main/java/net/decimation/worldgen/LargeSites.java dev/src/main/java/net/decimation/worldgen/city/LcCity.java tools/site/subjects.py tools/site/siteshots.tsv docs/shots_index.md docs/website.md
git commit -m "Website screenshots: subjects listed from the server log, eleven camera rows, shots described"
```
Check `git status --short | grep -E 'png|jpg'` prints nothing (docs/shots stays ignored).

---

## Part C: go live

### Task 13 (C1): The real screenshots into the site

**Files:**
- Modify (site repo, by the tool): `public/shots/*.jpg`, `public/og.jpg`

- [ ] **Step 1: Export from the siteshots output**

Run: `cd ~/Projects/Decimation && python3 tools/site/export_assets.py ../deciremnant --shots dev/run/client/screenshots`
Expected: eleven `shot` lines with the B1 size (1920 x 1080 or the measured one), exit 0.

- [ ] **Step 2: Recheck the site**

In the site repo repeat Task A8 steps 2, 4, 5, 6 and 7 (tests, build, desktop and phone pictures read, text, fonts, console, links).

- [ ] **Step 3: Commit (site repo)**

```bash
cd ~/Projects/deciremnant && git add public && git commit -m "Real screenshots from the siteshots dev test"
```

### Task 14 (C2): GitHub, Vercel, live check, docs

**Files:**
- Modify (mod repo): `docs/website.md`, `docs/roadmap.md`, `docs/tasks.md`, `CLAUDE.md`, `.claude/skills/decimation-website/SKILL.md`, graph files

- [ ] **Step 1: Ask the user before creating the GitHub repo**

Ask: "Create the private repo ucilmenangis/deciremnant with gh and add it as origin? (no push)". Only after a yes:
`cd ~/Projects/deciremnant && gh repo create ucilmenangis/deciremnant --private --source . --remote origin`
Expected: the repo exists, `git remote -v` shows origin. Do not push.

- [ ] **Step 2: The user's steps (tell them, in this order)**

1. `cd ~/Projects/deciremnant && git push -u origin main`
2. vercel.com, Add New, Project, import `deciremnant` (Framework Preset Next.js, no settings changed), Deploy.
3. If the name `deciremnant` is taken, Vercel shows another `.vercel.app` address: tell it back.

- [ ] **Step 3: Check the live site after the deploy**

```bash
agent-browser open https://deciremnant.vercel.app && agent-browser set viewport 1300 900 && agent-browser screenshot --full "$SCRATCH/live_desktop.png"
agent-browser set viewport 390 844 && agent-browser screenshot --full "$SCRATCH/live_phone.png" && agent-browser eval "document.documentElement.scrollWidth"
agent-browser errors
node ~/Projects/deciremnant/scripts/check-links.mjs https://deciremnant.vercel.app/
curl -sI https://deciremnant.vercel.app/ | grep -i -E "x-vercel-cache|cache-control"
```
Expected: pictures read and right, `scrollWidth` 390 or less, no errors, `all links ok`, the page served from the cache with revalidation (`x-vercel-cache` HIT or STALE). Only now is the site "done".

- [ ] **Step 4: Docs and knowledge**

- `docs/website.md`: status LIVE (date, address, the size the shots use), section 9 items checked or still open.
- `docs/roadmap.md` UI 2: v1 DONE with the date and address; the server list stays open.
- `docs/tasks.md`: website item done.
- `CLAUDE.md` knowledge index line for docs/website.md: add "live at <address>".
- Skill decimation-website: casebook case 2 (the build: what failed on the way, how it was fixed, the checks) and any new lesson from the user's look at the live site.
- Graph: `python3 tools/graph_update.py prepare`, then `python3 tools/graph_carry.py "$SCRATCH/extra_live.json" docs/website.md docs/roadmap.md docs/tasks.md CLAUDE.md .claude/skills/decimation-website/SKILL.md` with this JSON (address and date filled in), then `python3 tools/graph_update.py finish`:
```json
{"nodes": [["docs_website_live", "DeciRemnant website live at ADDRESS since DATE: download, guide, screenshots, coming soon, changelog, help", "docs/website.md"]],
 "edges": [["docs_website_spec", "docs_website_live", "status", "docs/website.md"]],
 "relabel": {"docs_roadmap_project_website": "Roadmap UI 2: DeciRemnant v1 live; the community server list is still open"}}
```

- [ ] **Step 5: Commit (mod repo)**

```bash
git add docs CLAUDE.md .claude/skills/decimation-website graphify-out && git commit -m "DeciRemnant website live: docs, roadmap, skill case 2"
```
Then ask the user to look at the live site and record their verdict in the skill.

# Tasks (what is open right now)

Short checklist of the work in flight, shown in the usage band and the usage pane of Claude Code
(the usage-pane mod in ~/.claude-mods reads this file every 3 s). Details live in docs/roadmap.md
and the linked docs; this file only names each item. Keep it current: add an item when it opens,
`- [~]` while it is being worked on, `- [x]` when done (prune done items now and then).

- [x] name the 597 unnamed Decimation methods (deobf/maps/m_methods_rest.tsv)
- [x] usage pane: group tasks into working now and pending
- [x] study each Decimation feature: how its code works, its purpose (docs/decimation_code/)
- [x] WorldClock day never advances: saved day counter (v0.62.9)
- [x] DeciRemnant website v1 live: https://deciremnant.vercel.app/ (checked live)
- [ ] DeciRemnant: GITHUB_TOKEN in Vercel (recommended; user). Verdict given 17 Oktober: kept, study famous sites next time
- [x] DeciRemnant SEO (decimation + minecraft): live, Lighthouse 100 on mobile and desktop
- [x] Google Search Console: verified, sitemap.xml submitted, indexing requested (user, 17 Oktober 2026)
- [ ] 20 Oktober 2026: tell the user to check Google indexing of deciremnant.vercel.app (docs/website.md 9b)
- [x] DeciRemnant: install for new players (lined table, game screens) + server guide /docs/server: live checked
- [x] Decimation main menu: news panels (GitHub, Updates, Website, Coming soon) + real scene background every 7 s (v0.63.0, dev client)
- [ ] main menu: the user's look in game (needs a release with v0.63.0)
- [x] Decimation logo in the website's top bar (site fc0d159), then replaced by our official logo
- [x] our official logo in the top bar, BoehMod logo in the footer: site 72cc3c7 live checked (1300 and 390, links, console)
- [x] main menu news panels: real topic pictures (GitHub repo, release list, website top, coming soon cards) in the jars
- [x] main menu v0.64.0: Website / GitHub links, DeciRemnant and our version, Continue, our corner (dev client)
- [x] main menu v0.64.1: server pages coming soon (Bukovka, Chernarus), status rows, Early access (dev test menupages)
- [x] release 0.64.6 published 17 Oktober 2026 (pre-release; GitHub's digest = local sha256 d6b8dfe2); site download 0.64.6 at 12:45, old clients see Update
- [ ] forum on our website for My Community (later, roadmap UI 3): Discord first (docs/forum.md)
- [ ] user: create the DeciRemnant Discord server from the site repo's docs/discord/channel-plan.md, send the invite
- [ ] site: Discord link in top bar and footer, "A forum" card becomes "Join our Discord" (needs the invite)
- [x] in-game update finds the running jar (v0.64.7 local; players on 0.59 .. 0.64.6 install the next release by hand once)
- [x] update runs on Decimation's own progress screen, Quit game / Later (v0.64.7; dev and production client)
- [x] our DeciRemnant logo in Decimation's menu banners (v0.64.7; dev and production client)
- [x] release 0.64.7 published 17 Oktober 2026 15:33 (pre-release, digest = local sha256 ad4197ab); real self update 0.64.6 to 0.64.7 checked
- [x] site coming soon cards (forum, official servers): live 17 Oktober 2026
- [x] release 0.64.8 published 16:53 (digest = local sha256 73049bc7); real update 0.64.7 to 0.64.8 checked in the production client
- [x] user: real auto update in Prism 0.64.7 to 0.64.8 worked (16:57, title v0.64.8, Updates "Up to date: 0.64.8")
- [x] update screen bar stops at about 80 %: fixes/UpdateBar (in code, next release; GUI width 500: 250 of 250 px)
- [ ] next release: Continue tooltip "Open world" reads like an instruction: "Last played: <name>"
- [x] website: coral on charcoal + camo, title screen hero, small logo on scroll: live 17:30, checked (mobile 93 / 96, desktop 100)
- [x] website sections: About, showcase rows, And more, closing band, map tab icon, GitHub logo, no dirt: live, checked
- [x] website intro (signal then title screen) and project docs (/docs, 24 pages): live, checked
- [x] page audit of all 26 pages (layout, SEO, links, Lighthouse 95 to 97 / 100 / 100 / 100): fixes in site 4cf80c8, live checked (Lighthouse 98 to 100 / 100 / 100 / 100)
- [x] "Entity is already tracked": swapped mobs added twice at chunk load, double speed; fixes/JoinOnce (v0.64.2)
- [ ] survivor partner idea: design talk (docs/ideas.md item 21)
- [~] player hosting part 1: built, reviewed, relay deployed, brother test smooth (150 to 270 ms, docs/player_hosting.md 12); "Commands: only me" checked in the real game (/tp works), pushed; open: release (batched)
- [x] Community page: "Players Online: N" only, placeholder for an empty list (CommunityText, dev client)
- [x] main menu news: Multiplayer first (opens the Community list), fresh pictures (three jars)
- [~] website multiplayer teaser: hero link + section (site 5ca55b6 local); open: user's look, push, live check
- [~] promotion drafts (docs/promotion.md): Reddit + Discord teaser; user decides on BoehMod permission, posts
- [x] joiners without the voice chat mod get into a host that has it (OptionalMods, 179e211; two game test PASS)
- [ ] bug: joining a world with another missing mod sits on Connecting for 2 minutes (bug.md)
- [x] corpse could not be opened with a gun in hand (joined player report): fixes/GunUseKey, two game test PASS
- [x] natural spawns off for tests: /gamerule doMobSpawning false also stops our spawners (fixes/NaturalSpawns, dev test nospawn)
- [~] plan: 1.0.0 beta (docs/release_1.0.md), user to confirm scope and order
- [~] plan: city content replacement (docs/city_replacement.md): ChaosZPack blocked by its license, McJty Lost Cities (MIT) proposed; user decides
- [ ] city content replacement with ChaoszPack Lost Cities (copyright; roadmap, after multiplayer)
- [x] HUD crash when a world is joined without Decimation's menu: fixes/HudGuard (in code, next release)
- [x] place names gone after a rejoin: PlaceNames forgets a player on logout (bug.md, in code, next release)
- [x] hosting spike joiner side: crate, hummer, trader, AKM, bandit fire and tracers, lists all PASS (docs/player_hosting.md 9)
- [x] backend thread leak: PatchBackend v2, no BoehMod connection (v0.62.8)
- [x] one skill per Decimation feature (.claude/skills/decimation-code*)
- [ ] flaky shots checks: AKM count, spotting at 70 (pending, user)
- [ ] loot step 2 questions (docs/systems_plan.md section 8)
- [x] gate walk tests for every gate look: path finder check; checkpoint HESCO stair fixed (v0.64.4)
- [x] tools/hwmap.py on the sized city layout (tools/citylayout.py, identical to Java)
- [ ] black mobs: graphics log line from the brother's laptop
- [ ] graph.json over 5 MB: keep committing or not
- [x] 64 district city refuge check: seed 1, 4 refuges, none in wasteland (gatescan lists refuges now)
- [x] airport garrison manned in game: dev test garrison; far posts and wandering guards fixed (v0.64.3)
- [ ] airport verdict in game (user, seed 1, `/tp -2470 66 1290`)

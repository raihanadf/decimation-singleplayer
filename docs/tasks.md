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
- [~] our official logo in the top bar, BoehMod logo in the footer: site 72cc3c7, waits for the push and live check
- [x] main menu news panels: real topic pictures (GitHub repo, release list, website top, coming soon cards) in the jars
- [x] main menu v0.64.0: Website / GitHub links, DeciRemnant and our version, Continue, our corner (dev client)
- [x] main menu v0.64.1: server pages coming soon (Bukovka, Chernarus), status rows, Early access (dev test menupages)
- [~] release 0.64.1: one jar built and production tested (server Done, client 22 PASS); waits for the user's push of main + tag, then the GitHub release
- [ ] forum on our website for My Community (later, roadmap UI 3)
- [x] "Entity is already tracked": swapped mobs added twice at chunk load, double speed; fixes/JoinOnce (v0.64.2)
- [ ] survivor partner idea: design talk (docs/ideas.md item 21)
- [x] backend thread leak: PatchBackend v2, no BoehMod connection (v0.62.8)
- [x] one skill per Decimation feature (.claude/skills/decimation-code*)
- [ ] flaky shots checks: AKM count, spotting at 70 (pending, user)
- [ ] loot step 2 questions (docs/systems_plan.md section 8)
- [ ] gate walk tests for the other gate looks
- [ ] tools/hwmap.py on the sized city layout
- [ ] black mobs: graphics log line from the brother's laptop
- [ ] graph.json over 5 MB: keep committing or not
- [x] 64 district city refuge check: seed 1, 4 refuges, none in wasteland (gatescan lists refuges now)
- [ ] airport garrison manned in game
- [ ] airport verdict in game (user, seed 1, `/tp -2470 66 1290`)

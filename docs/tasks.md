# Tasks (what is open right now)

Short checklist of the work in flight, shown in the usage band and the usage pane of Claude Code
(the usage-pane mod in ~/.claude-mods reads this file every 3 s). Details live in docs/roadmap.md
and the linked docs; this file only names each item. Keep it current: add an item when it opens,
`- [~]` while it is being worked on, `- [x]` when done (prune done items now and then).

- [x] name the 597 unnamed Decimation methods (deobf/maps/m_methods_rest.tsv)
- [x] usage pane: group tasks into working now and pending
- [~] study each Decimation feature: how its code works, its purpose
- [ ] WorldClock day never advances (Decimation resets world time daily, bug.md): fix test first
- [ ] backend thread leak (one thread per 15 s, bug.md): fix with PatchBackend v2? (user)
- [ ] one skill per Decimation feature (.claude/skills/decimation-code-*)
- [ ] flaky shots checks: AKM count, spotting at 70 (pending, user)
- [ ] loot step 2 questions (docs/systems_plan.md section 8)
- [ ] call logger first use: name methods or list silent handlers
- [ ] gate walk tests for the other gate looks
- [ ] tools/hwmap.py on the sized city layout
- [ ] black mobs: graphics log line from the brother's laptop
- [ ] graph.json over 5 MB: keep committing or not
- [ ] 64 district city refuge check
- [ ] airport garrison manned in game
- [ ] airport verdict in game (user, seed 1, `/tp -2470 66 1290`)

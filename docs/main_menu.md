# Our main menu: Decimation's own menu with our news and real scenes (17 Oktober 2026)

How Decimation's menu is built, element by element: docs/decimation_code/menus.md section 7. This doc is what we
changed in it.

## 1. What the user asked

- "lets make the news panel now. 1. github repo, 2. update release, 3. our website release, 4. Coming soon feature.
  that for new panel/first main menu page"
- "for the new look, i agree with you to replace with real scene with your suggestion animation. do that"
- "just use component from decimation gui menu its self, no need to add something new on it"
- "if you can make the background image, changed every 7 second"
- Later (not done yet): "put real decimation logo from main menu to add it on website logo too" (docs/tasks.md).

## 2. What a player sees (shots: docs/shots/main_menu_v0.62.9/, shots_index.md)

- **News panels**: Decimation's own news panel (ArticlePanel, 120 x 70: title, a small line, a picture, red on hover
  with its hover sound and link mark, a click opens the page) four times, in the 2 x 2 grid Decimation keeps for news
  right of the buttons (GuiMenuHome would fill it only from BoehMod's dead backend):

  | Panel | Line | Opens | Picture (a real picture of the topic) |
  |---|---|---|---|
  | GitHub | Source code, issues, all releases | the public repo | our repo page on GitHub (dark) |
  | Updates | follows fixes/Updater: "Checking GitHub releases", "Up to date: 0.62.9" (dev suffix cut), "New version X is out", "Release check failed, see GitHub" | the newest release's page, else the release list | the release list on GitHub with 0.59.0 |
  | Website | Download, install and server guides | deciremnant.vercel.app | the site's top with the Decimation logo |
  | Coming soon | What we are building next | the site's #coming | the site's coming soon cards |

  The first pictures were scenes of our world (MAC-10, airport, camp, base at sunset); the user: "replace the picture of
  news panel to real topic image rather using placeholder like that". The topic pictures are agent-browser screenshots
  at 2.32 : 1 in docs/shots/main_menu_v0.62.9/news_src/ (GitHub pages in dark mode at 928 x 400, the site's top at
  1160 x 500, the coming soon cards at 928 x 400), made 464 x 200 by tools/menu_assets.py. Retake them when they go
  stale: the Updates picture shows the newest release (0.59.0), the website pictures the site as it is.

  Main menu only: in a world the same screen is the pause menu, no panels there.
- **Background**: Decimation's own background drawing (one picture with its slow sway, slow zoom in and out and the
  mouse parallax, its drifting fog, side shadow and dark top and bottom), now showing six real scenes of our world one
  after another, 7 s each, the next fading in over 1.2 s, starting with the one Decimation picked for the launch. The
  opaque black layer that hid Decimation's own pictures, the rotating lava "earth" cube, the white "sun" cube and its
  six glow sprites are gone. Scenes (from the website's shots, dimmed to 62 % and 80 % colour): a city from above, a
  city refuge among towers, a wasteland gate street, a bridge over a canal, a base at sunset, the airport.

## 2b. v0.64.0 (17 Oktober 2026): links, our name and version, Continue, our corner

The user picked from suggestions: "Links to our site", "Replace FURY advert", "Our version shown" ("for version replace
version code with our version code name, and for decimation replace with deciremnant"), "Continue last world". Not
picked: Play straight to the worlds, hiding the dead pages.
- The red links top right say **Website** and **GitHub** on every menu page and open our site and our repo
  (PatchMenuTexts: labels in GuiMenuBase and the other pages, addresses in ClientResources).
- Our name and version where Decimation showed its own: the title line **DeciRemnant: Zombie Apocalypse**, **v0.63.0**
  under the logo (our version, a dev build's suffix cut), the welcome box **Welcome to DeciRemnant v0.63.0! Have fun!**
  (Decimation's lang file plus the version call), the window title **DeciRemnant v0.63.0 (Minecraft 1.7.10)**. Our
  texts carry no dash (user rule; Decimation's "Decimation - " and "Minecraft - Decimation v1.21.10f" had one).
  Decimation's own mod version stays 1.21.10f (servers and the handshake use it).
- **Continue** under Play (Decimation's own black menu button, tooltip "Last played: <world>" since 0.65.0; it said "Open
  <world>", which read like an order for a world named "world"): opens the world played last; the
  buttons under it move down one row; main menu only, only when a world exists (fixes/MenuContinue). Dev tests never
  click it (it would open the newest world of the dev run folder, maybe the user's).
- The FURY store advert's corner shows ours: Decimation's torn black panel with **DeciRemnant**, "Download and guides"
  and deciremnant.vercel.app in the Minecraft font the website uses; the title pulses like FURY's did.

## 2c. v0.64.1 (17 Oktober 2026): the server pages say coming soon, Early access, Chernarus

The user's words: "for server status, changed it like this: Global Server, Community Server, Download, with that name 3
of them for the offline"; "for my supporter, change the name to Early access"; "for server that is down, replace it with
Bukovka and Chernarus only and its say coming soon, same with player online its say coming soon instead of unknown, with
same community to when notification appear, its say community server coming soon instead of failed to load"; five
Chernarus pictures ("here the chernarus location"). Shots: docs/shots/main_menu_v0.64.0/menupages_*.png (shots_index.md).
- **Official server list** (Play): two rows, **Bukovka** and **Chernarus**, line "Official DeciRemnant Server (Official)",
  state **Coming soon** in gold. Decimation drew "?/?" over the state text of every row without a player count (its
  "Not Reachable" read "Not Reach?/?le") and " - 0ms" for a ping that never happened: both gone. Above the list
  **Players Online: Coming soon** (was Unknown).
- **Map pictures**: hovering a row fades one of its map's eight pictures in behind the page and inside the row (Decimation's
  own, picked at random per hover: textures/gui/menu/serverbackgrounds/<map>0..7.png). Bukovka keeps Decimation's eight;
  Chernarus shows the user's five pictures (coastal town, harbour with cranes, castle ruin, village under power lines,
  village with a pond; 720 x 405, sources in docs/shots/main_menu_v0.64.0/chernarus_src), the first three twice.
- **Community tab** on that page: tooltip "Coming soon" (was "Offline"); a click shows the toast **Community server coming
  soon** (was "Failed to load Community list!").
  Since player hosting part 1 (18 Oktober 2026, docs/superpowers/specs/2026-10-18-player-hosting-part1-design.md): the
  tooltip counts the listed worlds ("N worlds") and a click opens Decimation's Community page filled from our relay's
  list (fixes/hosting/CommunityList); the "coming soon" toast is only reached by code that skips Forge's click event.
- **Server Status page**: Global Server (Official DeciRemnant server), Community Server (Servers run by players), Download
  (DeciRemnant download server), each **Offline**; the fourth row, Website (BoehMod's site), unchanged and Online.
- **My Community popup** (the button under Continue): **★ Early access** (line "New features before everyone else!"; a
  click: toast "★ Early access is coming soon!"), Website ("Download, install and server guides"), Forums (unchanged:
  BoehMod's forums; the user: "later we make forum on our website on it"), GitHub ("Source code, issues, all releases").
- The profile cards say **No early access** (was "Not Supporter :(" and "Is not a Supporter :("), the lounge title
  "★ Early access lounge" (nobody reaches it: no player is a supporter without BoehMod's backend).
- Since v0.64.7 (user): toasts carry the title DeciRemnant (ToastNotification deci/b/b), and the status page's fourth
  row is our Website (Official DeciRemnant Website, deciremnant.vercel.app port 80, Online when the site answers).
- No network: every row's host ends in .invalid (RFC 6761: never resolves), so the pings fail at once and nobody's
  old domain gets our players' connections. The status page's pinger has no timeout: on Decimation's dead hosts it sat
  at "Pinging..." for over 20 s (a resolving host that never answers holds the socket about 75 s).

## 2d. v0.64.7 (17 Oktober 2026): Decimation's own update screen, our logo

- **Update** (user: "why not using from decimation instead? as i know decimation has it owns update loading screen"):
  the menu's Update button opens Decimation's own progress screen GuiUpdateProgress (deci.i.m: black page, the banner
  with the soldier and the infected, "DO NOT CLOSE YOUR GAME!" in red while it downloads, "SAFE TO CLOSE GAME!" in green
  after, "Downloading: Decimation.jar (12.8mb)", two lines, "Downloading: DeciRemnant 0.64.6 from GitHub", the green
  bar with its flare, "40.0mb/12.8mb" (Decimation prints total first)). fixes/UpdateProgress fills the fields it draws
  (UpdateChecker azL .. azP and fileName, Deci.setUpdateProgress) from our download each client tick. When the jar is
  checked: **Quit game** and **Later** under the bar (that screen draws its black layer over its own buttons, so ours
  are drawn again on DrawScreenEvent.Post). A failed download or a check that found nothing yet shows our old
  UpdateScreen (the reason, Try again / Back). The two lines come from Decimation's lang file, rewritten by
  tools/menu_assets.py (no "your game may freeze", no "your game will Shut down": ours does neither).
- **Logo** (user: "replace old logo on decimation with our new logo"): banner.png (top left of every menu page; in game
  too) and banner_intro.png (home page fade in, update screen) carry our official DeciRemnant logo (the website's,
  docs/shots/main_menu_v0.64.0/logo_src, white paper cleared by tools/site/export_assets.py clear_paper), at twice
  Decimation's size (drawn by UV 0..1: sharper, same place). banner_intro keeps Decimation's soldier and infected; only
  its lettering and barbed wire (row 127 down) are replaced. The window icon (a character portrait, no name) and the
  BoehMod logo (credit) stay. tools/menu_assets.py logos(), three jars.
- Checked (RED first, then PASS): `update` (fake release: Decimation's screen opens, it shows 33 % / downloading /
  13.5 of 40.0 MB, ready: Quit game and Later, safe to close), `menu` ("menu logo is ours": 922 x 202 and 1024 x 480
  loaded, was 461 x 101 and 512 x 240); in the production client with the one jar: `menu update mainmenu menupages
  real=true jar=true from=0.59.0 expect=0.64.6` all PASS, the real 238 MB release downloaded in 19 s on Decimation's
  screen and swapped into mods/Decimation.jar. Shots docs/shots/update_v0.64.7 (shots_index.md).

- v0.64.8 (user: update screen polish, the release for watching a real auto update in Prism): the lines as Decimation's
  own updater filled them: "Downloading: DeciRemnant 0.64.8 (239.5mb)" (version and file size), "Downloading: 33%" then
  "Downloading: Complete!" (its lang key menu.updating.complete), the size line done before total ("80.0mb/239.5mb";
  the screen prints totalBytes first, so UpdateProgress puts done in totalBytes and the size in downloadedBytes).
  Dev test `update` checks "progress lines" and "ready lines" (Deci.updateLines), RED first. Shots
  docs/shots/update_v0.64.8.

## 2e. 18 Oktober 2026: Multiplayer news first (promote player hosting)

- User: "update home news page with latest ones since its old image, i think we need to put some highlight to
  multiplayer server on website and main menu". Picked (AskUserQuestion): Multiplayer first, then Updates, Website,
  Coming soon (the GitHub panel goes; GitHub stays as the top right link); a click on Multiplayer opens the in-game
  Community list, not a web page.
- fixes/MenuNews: panel "Multiplayer", line "NEW: host your world, join others", address MenuNews.COMMUNITY
  ("deciremnant:community"); Deci.addNewsPanel makes ArticlePanel a subclass whose openUrl (deci.d.c.o) opens
  CommunityList for that address. Deci.openNewsPanel (tests) does what a click does.
- Pictures retaken 18 Oktober 2026 (news_src): multiplayer.png = the Community page with a hosted world "Survivors"
  (Join, 1/4, 534 ms; two game harness lanjoin promo=true), update.png = the release list with 0.64.8, website.png =
  the site's top, coming.png = the coming soon cards; the old four kept only in the scratchpad. Written into the three
  jars by tools/menu_assets.py (NEWS now news_multiplayer). The website picture goes stale again once the site carries
  the multiplayer banner: retake it then.
- Dev test mainmenu: order Multiplayer, Updates, Website, Coming soon and "Multiplayer panel opens the Community list"
  (RED 2 FAIL, GREEN). Shot docs/shots/main_menu_v0.64.9/mainmenu_home.png.

## 2f. 20 Oktober 2026: dev and test builds never offer an update

- User (dev client after 0.65.0 was out): "why it show update?" Main builds read version 0.64.8-main.N (0.65.0 is tagged
  on the prerelease branch), so the check took 0.65.0 for an update; the dev folder cannot install it (bug.md "Dev
  client and main's test jars offered the 0.65.0 pre-release as an update").
- Only release builds look now (fixes/Updater.offersUpdates). A dev or test build: Play, the Updates panel says
  "Updates off: dev build (main)" (or "(prerelease)"), and our version reads "0.64.8 dev" under the logo, in the
  welcome box and the window title ("DeciRemnant v0.64.8 dev (Minecraft 1.7.10)"). A release jar is unchanged.
- Shot docs/shots/main_menu_v0.65.0/mainmenu_home_dev.png (shots_index.md).

## 2g. 20 and 21 Oktober 2026: updates are the player's choice, Supporter back, status rows from real sources

The user's words: "make option when there is update so user can do update or not at that time so they dont force user
to update their version, if (worse case) the updater is doesnt support on some client, there is link to download new
jar. for ealry access, rewind it to supporter, it will use for people who donate to me so they got supporter benefit";
earlier the same day: "download server its take from gituhb release link, and for community server it take status from
cloudflare, for the offical it take from server list, official mean not from my server, its combined with other
community dedicated server, and later i want to make rating upvote system in there".
- **Never forced**: fixes/Updater.required() (what Decimation calls isUpdateRequired, PatchNoUpdate) answers false.
  Before, a known release put Update in Play's place AND in Resume Game's place in the pause menu, and Play opened the
  update (GuiMenuBase.addMenuButtons, handleMenuButton case 9); the "New update available!" corner notice went with it.
- **The question** (fixes/UpdatePrompt): once a launch, on the title menu only (never over a game), after the welcome
  fade, Decimation's own question box (ConfirmScreen deci/d/s) asks "DeciRemnant X is out / N MB. Update now? / Later:
  Settings, View Updates" with Later (left, red) and Update. Update opens Decimation's progress screen (the download
  runs as before); Later leaves the menu as it was, nothing asks again until the next launch. Afterwards the update is
  one click away: Settings, View Updates (Decimation's button 1000, which UpdateProgress already turned into ours) and
  the Updates news panel (its click opens the release page).
- **Where the updater cannot swap the jar** (Updater.canSwap: the game's jar not found): the box says "Download it from
  GitHub and put it in your mods folder" with Later / Download; Download opens the release's GitHub page (the jar is
  there). A failed update (fixes/UpdateScreen) shows Try again / Download page / Back. Shot
  docs/shots/main_menu_v0.65.0/update_prompt.png.
- **Supporter back**: PatchMenuTexts no longer swaps the supporter texts (My Community popup "★ My Supporter /
  Personalize your Supporter settings!", its toast "You must be a ★ Supporter to access this!", profile cards "Not
  Supporter :(", the lounge "Supporters Lounge"). Supporter benefits for the user's donors (who counts as a supporter,
  where that list lives) are not designed yet: nobody is a supporter while BoehMod's backend is gone.
- **Server Status rows from real sources** (fixes/MenuStatus): Global Server from the official server list (Coming soon
  while the list holds only rows that never resolve, else Online when one of its servers answers), Community Server
  from the relay (main: its /list answers; the pre-release has no relay and says Coming soon), Download from the GitHub
  release link (HEAD of releases/latest), Website still Decimation's own ping. ServerPinger's Unknown state reads
  "Coming soon" (PatchMenuTexts, deci/l/a). Checks run on a thread when the page opens, at most once in 30 s. Shot
  docs/shots/main_menu_v0.65.0/menupages_status.png.
- Tests: unit UpdaterTest, UpdatePromptTest (lines fit the 200 pixel box, no dashes, the failure screen's buttons),
  MenuStatusTest; dev tests `update` (now leaves the world like mainmenu: run it after modes that need a player;
  `choice=later` answers Later then updates from Settings, `noswap=true` plays a game that cannot swap), `menupages`
  (supporter texts, status rows), `menu`, `mainmenu`. All PASS on main and in the prerelease worktree (Community
  Server Coming soon there).
- 21 Oktober 2026 (user, asked whether the Esc menu should ask too: "yes"): the question also comes on the Esc menu
  when the player is in a world (still once a launch). Quit game on the update screens now leaves the world and waits
  until it is saved before closing (fixes/SafeQuit, as DevAutoTest.quitSafely: mc.shutdown inside a world once broke a
  level.dat). Dev test `update inworld=true`: Resume Game stays, the box asks, the update runs, Quit game "world left,
  saved, would close" (it stops before closing). RED first (no question in the world), then PASS, main and prerelease.
- Next (not designed): the official list as community run dedicated servers with an upvote rating (roadmap UI 4;
  docs/ideas.md 24), and a supporter tier for donors through the website (docs/ideas.md 23).

## 2h. 21 Oktober 2026: detail popups, Discord, our footer, a corner of our own (BUILT the same night)

The user's words: "for home page or new page, can you add popup page to show detail of the content? like for
multiplayer, it show several image with carousel design and below that show title and description about the feat, for
updates same like multiplayer it show updates list, website stay intact, coming soon show information about what feat
we gonna add in future (i wanted on the website and news page to show less feat we have, i think dont need to show bunch
of feat we dont make so we dont make big promise to end user). just like that. for the github, replace with discord
instead. same like my community, forum replace with discord"; footer: "replace boehmod logo and web link in the bottom
with ours, and for on the right side maybe you have better design suggestion? maybe we can design using legacy image
that we remove before (fury) and learn that design and after that we make our version with it??".
- Mockups (docs/shots/main_menu_v0.65.0/mockups/, shots_index.md): popup_multiplayer.png and popup_coming.png (a popup
  over the darkened menu in Decimation's look: black panel, red top line, title and line, a 16:9 picture with torn arrow
  buttons and dots, a heading and two lines, a red torn action button and Close); footer_corner.png (our logo where
  BoehMod's sat, the copyright line keeping the credit "Unofficial: Decimation by BoehMod", a FURY style tile).
- FURY studied (Decimation's donor_rank_0.png and donor_rank_1.png, 800 x 800, drawn 150 x 150 GUI px in the bottom right
  corner, the second layer pulsing): a soldier in red and black camouflage with glowing red goggles and a rifle, a red ink
  splash fading toward the top left, a big red horror brush title "FURY" with a white glow, "JOIN THE FURY", "NOW
  AVAILABLE ON THE DECI WEBSTORE!", the store address; the pulse layer holds only the texts. It advertised a paid donor
  rank. Our version proposed: "JOIN THE REMNANT" for our Discord now (title from our own logo, no new font), a supporter
  tier version once that exists (docs/ideas.md 23); the character a real render of a survivor in our gear.
- Decimation has its own updates page (GuiMenuUpdates deci/i/l: the Server Status layout with a scrolling release list,
  ReleaseEntry rows, Refresh Releases in Play's place) that UpdateProgress swaps out today; the user asked for the
  Updates popup to look like the Multiplayer one with the release list.
- The user's answers: Discord https://discord.com/invite/ZJ47zfXn8H; Coming soon keeps two (multiplayer and new cities,
  both 1.0.0; the website session was told, docs/website.md); the footer drops BoehMod (credited on the website only:
  "we can put it to website only and credit it anyway"); the corner: Discord now, a supporter version later.
- BUILT (main 897183a, e666300, f133a2a; prerelease bd9fab9, 3de0c39, 5246e4b):
  - Links (PatchMenuTexts): top right Website and Discord (the Twitter slot, ClientResources bb); My Community reads
    My Supporter, Website, GitHub (the Forums slot, bc), Discord (the Twitter slot). Footer: "Copyright <year>
    DeciRemnant (deciremnant.vercel.app)", BoehMod's small address gone, our logo in boehmod.png (menu_assets.py).
  - Popups (fixes/MenuDetail draws, fixes/MenuDetails holds the content): news panel addresses deciremnant:detail/<id>
    (MenuNews.open). Multiplayer (main only): 3 slides (Host Online page, a hosted world listed, a world of ours),
    "Open Community"; Updates: the release list of the last check (Updater.releases, version, date, first line of the
    notes), "Update to X" or "All releases"; Coming soon: 2 slides (Multiplayer in 1.0.0, New cities in 1.0.0); Website
    still opens the site. Slides change every 6 s; arrows, dots, Close, Esc or a click outside. Pictures
    detail_<id><n>.png by menu_assets.py (a picture wider than 16:9 kept whole on a dark band).
  - Corner (menu_assets.py corner(), fixes/MenuCorner): dev test `cornerart` draws an elite_military soldier (helmet,
    night vision, rifle) on magenta with the inventory preview and saves him cut out (run/client/devtest/corner_*.png,
    kept in docs/shots/main_menu_v0.65.0/corner_src); the art: red ink splash, the soldier with a red rim glow, our
    logo, "JOIN THE" "REMNANT", "News, help and players on our Discord", discord.gg/ZJ47zfXn8H; the words pulse
    (donor_rank_1). A click on the words opens the Discord (a button that draws nothing, 79 x 47 at the corner). At
    854 x 480 the soldier reaches into the Coming soon panel (Decimation's FURY did too); wider windows keep clear.
  - Tests: unit MenuDetailsTest (RED without the class, then 5 / 5); mainmenu: panel addresses, each popup's title and
    slides, arrow, Close, Multiplayer's Open Community, the corner opens Discord; menupages: popup lines, footer texts.
    All PASS on main and prerelease (prerelease: Updates and Coming soon only). The corner check never ran RED.
  - Pre-release jars drop the Multiplayer popup's pictures with news_multiplayer.png (tools/linecheck.py STRIP).
  - The user's first click crashed the game (bug.md "Clicking a news popup crashed the game"): fixed in main 089edfd;
    mainmenu now clicks the panels like a mouse.
- Second round (user, 21 Oktober 2026 02:04, shots of the corner at a big and a small window: "it still not similar
  like previous ones, not well detail. please learn again from old design detail by detail deeply"; Multiplayer: "more
  descriptive ... what the feat, what they do, how it work but dont use technical like dev we use ... show 2-3 mob
  in-game as preview so you dont expose the new feat at all"):
  - FURY measured (donor_rank_0 alpha): a landscape banner over the BOTTOM HALF of the square only (alpha from row 397,
    solid from x 232 to the right edge, soft top and left fades), opaque dark with a red smoke band, black splatter and
    drips at the right edge, thin diagonal streaks; the soldier a close up from the waist up, cut by the bottom edge,
    red light and a red rim glow, rifle across his body toward the words; the words one tight centred stack in x 441 ..
    724, y 486 .. 706 (the pulse layer's box): a huge brush title, "JOIN THE FURY" white and nearly as wide, a tiny line,
    the address in red and white. Drawn 150 x 150 GUI units, so the banner is about 136 x 75 units in the corner.
  - My first corner had none of that: a full body soldier, loose blobs over most of the square, oversized words stacked
    by font size (MinecraftStandard's letters are about 1.4 times the size given: "REMNANT" ran into the small lines);
    in small windows it covered the Coming soon panel. Redone to FURY's layout (menu_assets.py corner()): the banner,
    our soldier waist up in the solid part with red light and glow, our logo as the title, "JOIN THE REMNANT", "NEWS,
    HELP AND PLAYERS ON OUR DISCORD", "discord.gg/" red and the code white; every line placed by its measured box.
  - Multiplayer popup: three lines a slide for players (what it is, what you do, how it works; no relay, port, server
    words: MenuDetailsTest guards it); pictures of the game with two or three people (tools/site/mpshots.tsv: survivors
    against infected, three friends in a city street, two heading out on the highway), also for the Multiplayer panel
    and Coming soon's first slide; no picture of the new screens.

## 3. Code map

| Piece | Job |
|---|---|
| fixes/MenuNews | Forge GuiScreenEvent.InitGuiEvent.Post on the home menu (Deci.isHomeMenu, no world): four panels through Deci.addNewsPanel, pictures first into ArticlePanel's picture map (Deci.newsPicture: a key missing there is downloaded as a URL every frame); client tick keeps the Updates line current (Deci.newsLine). Registered in DecimationWorldGen (client, both buses). |
| fixes/MenuBackground | the slideshow: `draw(x, y, picked, w, h, zoom)` replaces the menu's one picture draw (Deci.drawCentered, and the alpha one for the fade); `shown()` for tests. Finds background0.png, background1.png ... in Decimation's menu textures. |
| Deci | isHomeMenu, addNewsPanel (ArticlePanel deci.d.c, DeciScreen.addPanel deci.d.i.a), newsLine, newsPicture (deci.d.c.et), newsPanels (tests), menuShown (ClientState.bO and isMenuFadedOut), menuBackground (ClientState.bC), drawCentered (RenderUtil deci.j.a.a, two forms) |
| tools/patches/PatchMenuBackground.java | Javassist on RenderUtil.drawMenuBackground (deci.j.a.b(GuiScreen)): removes the black layer, the two cube draws and the six glow sprites, sends the picture draw to MenuBackground.draw. Three jars. |
| tools/menu_assets.py | writes background0..5.png (from docs/shots/site_v0.62.9), news_github / update / website / coming.png (from news_src), the corner donor_rank_0 / 1.png (Decimation's buttonbg plus tools/menu/MinecraftStandard.otf, public domain) and the welcome line of Decimation's lang file into the three jars; Decimation art, never committed |
| fixes/MenuTexts | our version (a release's tag; a dev or test build's as "<tag> dev", MenuTexts.shown) and the window title, called from Decimation's code by PatchMenuTexts |
| fixes/MenuContinue | the Continue button: Forge InitGuiEvent.Post (Deci.menuButton, buttons 27, 11, 5, 0 moved down 21) and ActionPerformedEvent.Pre (launches the newest save) |
| tools/patches/PatchMenuTexts.java | ASM on the ORIGINAL classes plus PatchMenuStats (base.jar, section 4b): 47 texts in 12 classes since v0.64.1 (labels, title, version calls, link addresses, window title; status rows and hosts in deci/i/h; row state, "?/?" and " - 0ms" in ListElement deci/d/p; official line and Community tooltip in deci/i/i; popup, toast and profile texts in deci/d/k, i/a, i/e, i/k). Three jars. |
| fixes/MenuServers | the official list's rows (Bukovka, Chernarus on *.deciremnant.invalid, pictures by map key) through Deci.officialServer (ServerListEntry deci.k.a, setBackground y) |
| tools/patches/PatchOfficialServers.java | Javassist: ServerListPinger.officialServers (deci.k.b.bg) returns MenuServers.official(). Three jars. |
| fixes/UpdateProgress | the update on Decimation's progress screen: GuiOpenEvent swaps GuiMenuUpdates (deci.i.l) for it and starts Updater.download; client tick mirrors the download (Deci.setUpdateProgress) and falls back to UpdateScreen on a failure; InitGuiEvent.Post adds Quit game / Later when ready, DrawScreenEvent.Post draws them over the black layer, ActionPerformedEvent.Pre handles them. Registered on both buses. |
| tools/menu_assets.py logos() | banner.png 922 x 202 and banner_intro.png 1024 x 480 with our logo (source docs/shots/main_menu_v0.64.0/logo_src), the update screen's two lang lines |
| Deci (tests) | officialRows, showRowPicture (ListElement fH / fK), statusRows (deci.i.h iS..iV, ServerPinger deci.l.a), toasts (ToastNotification deci.b.b S, R, W), popupLines / clickPopupLine (DeciSubScreen deci.d.j fa, fb, g), menuTooltip (DeciMenuButton eN) |

## 4. Rebuild (after new site shots, before a release)

1. Patch from the ORIGINAL class (Decimation.jar.original.bak copied to a name ending in .jar), our classes compiled:
   ```bash
   cd dev && ./gradlew compileJava && cd ..
   mkdir -p build/patch_menubg/out && cp Decimation.jar.original.bak build/patch_menubg/original.jar
   javac -cp tools/lib/javassist.jar -source 8 -target 8 -d build/patch_menubg tools/patches/PatchMenuBackground.java
   java -cp tools/lib/javassist.jar:build/patch_menubg PatchMenuBackground build/patch_menubg/original.jar build/patch_menubg/out dev/build/classes/java/main
   (cd build/patch_menubg/out && zip <jar> deci/j/a.class)   # Decimation.jar.patched, dist/Decimation.jar, dev/libs/Decimation-base.jar
   ```
2. `python3 tools/menu_assets.py` (all three jars; `--only-dir DIR` to look at the pictures first).
3. Check: entry counts (the first run adds 7 entries: background3..5 and four news pictures), `unzip -tq`, the backup's
   hash unchanged. Checked 17 Oktober 2026: 21597 / 21591 / 21591 before the slideshow, +3 after; backup 57946011...

## 4b. Rebuild PatchMenuTexts (after PatchMenuStats, before menu_assets)

```bash
ASM=$(ls ~/.gradle/caches/modules-2/files-2.1/org.ow2.asm/asm-debug-all/5.0.3/*/asm-debug-all-5.0.3.jar | head -1)
javac -cp $ASM -source 8 -target 8 -d build/menutexts_rebuild/cls tools/patches/PatchMenuTexts.java
java -cp $ASM:build/menutexts_rebuild/cls PatchMenuTexts build/menutexts_rebuild/base.jar build/menutexts_rebuild/texts_out   # 43 texts in 14 classes (21 Oktober 2026)
(cd build/menutexts_rebuild/texts_out && zip <jar> deci/i/a.class deci/i/e.class deci/i/g.class deci/i/i.class deci/i/j.class \
     deci/i/k.class deci/i/l.class deci/d/k.class deci/b/c.class deci/m/b.class deci/i/h.class deci/d/p.class \
     deci/b/b.class deci/l/a.class)   # the three jars; compare with the last output first: only the classes you meant change
# the official list's rows (v0.64.1): deci/k/b is the original class in every jar; Javassist reads only names ending in .jar
javac -cp tools/lib/javassist.jar --release 8 -d build/patch_servers tools/patches/PatchOfficialServers.java
java -cp tools/lib/javassist.jar:build/patch_servers PatchOfficialServers dist/Decimation.jar build/patch_servers/out dev/build/classes/java/main
(cd build/patch_servers/out && zip <jar> deci/k/b.class)   # the three jars
```
The input must be the ORIGINAL classes with only PatchMenuStats on them (this patch removes the texts it looks for):
copy Decimation.jar.original.bak to base.jar, run PatchMenuStats on it and zip its three classes back in, then run
PatchMenuTexts on base.jar (done 17 Oktober 2026 in build/menutexts_rebuild; base.jar's ten classes matched the jars;
deci/i/h and deci/d/p, added in v0.64.1, were the original classes in all three jars). Every text of the plan must
be changed exactly once in its class, or the patch stops and names it (per text since 17 Oktober 2026, after a code
review: a total per class could hide a missing text behind a doubled one; the stricter run gave the same 12 classes).

## 5. Tests

- `python3 tools/devtest.py --live mainmenu`: leaves the world, opens the home menu, waits for the welcome box and the
  fade, checks the four panels (titles in order, a line, an https address), saves mainmenu_home, waits for the next
  background picture (check: changes within 7 s, at least 2 pictures), saves mainmenu_next. RED first both times (no
  panels with the hook unregistered; "picture -1 -> -1 of 0" before the patch), then PASS (6 pictures, 0 -> 1 after
  122 ticks).
- v0.64.0 checks in `mainmenu` (RED first: "Discord ... Twitter", "Minecraft - Decimation v1.21.10f", no Continue; then
  PASS): the links say Website and GitHub and their addresses are ours, the window title is "DeciRemnant v0.63.0
  (Minecraft 1.7.10)", a Continue button is there.
- With it, `menu` (no forced update) and `stats` (menu counters) still PASS.
- Since 20 Oktober 2026 (section 2f): `menu` checks "update check" (OFF in a dev or test build, asking GitHub in a
  release build) and `mainmenu` "updates panel line" ("Updates off: dev build (<branch>)" in a dev or test build).
- `menupages` (v0.64.1, run it last like mainmenu: it leaves the world): My Community popup lines, the Early access
  click's toast, the official rows (Bukovka and Chernarus, both not reachable), "Coming soon" as the player count, all
  16 map pictures load as textures (not the missing texture), the Community tab's tooltip and toast, the status rows
  (first three Offline), and our texts in the loaded classes (profile card, lounge: nothing to click). Shots
  menupages_community, menupages_official (Chernarus' first picture shown as a hover shows it), menupages_status.
  RED first (every check FAIL on the old jars), then PASS: 9 of 9 with menu, stats and mainmenu (17 Oktober 2026).
  Trap found: a class check for "Supporter" also matched the field name isSupporter; check exact old texts.
- TRAP: `mainmenu` ends at the title menu with no world; tests that wait for a player (`menu`, `stats`, most others)
  then wait forever and `--live --stop` times out (the quit waits for the run). Run them before `mainmenu`, or restart
  the live game.

## 6. Open

- The user's look in their own game: released in 0.64.6, the update screen and logo in 0.64.7 (17 Oktober 2026,
  GitHub pre-releases). Players on 0.59.0 .. 0.64.6 install 0.64.7 by hand once (bug.md); the dev client shows it too.
- Still dead and kept (not picked): My Community, Server Status, "Not Supporter :(", and Play still opens the dead
  official server list first (menus.md section 7). Since v0.64.1 those pages say coming soon (section 2c).
- Later (user): a forum on our website for the My Community popup's Forums line (roadmap UI 3); Global Server,
  Community Server, Download and the Bukovka / Chernarus rows wait for real servers (MenuServers holds the rows).
- Next, from the user (20 Oktober 2026, not designed yet; roadmap UI 4): the Server Status rows from real sources.
  Download from the GitHub release link, Community Server from the Cloudflare relay, Global Server from the official
  server list. "Official" means community run dedicated servers, not the user's own. Later an upvote rating so popular
  servers rank on top. The user has more main menu fixes and additions to name. A project skill for all main menu work
  (home menu and in game) is asked for after that ("just like other skills we have").
- Answered by the user (17 Oktober 2026): toasts say DeciRemnant and the Website row pings our site (both v0.64.7);
  the red X icon of an unreachable row stays ("no problem"); the Chernarus pictures are not the user's, their creator
  allowed their use; credited in the 0.64.7 release notes (creator's name not known here).
- The Decimation logo (banner.png) on the website: built 17 Oktober 2026 (site fc0d159), live after the user's push.

## 7. References looked at first (skill decimation-website lesson 17)

- Minecraft's title screen: six square panorama faces (front, right, back, left, up, down), the camera turns inside
  them; vanilla 1.7.10 renders them at 256 x 256 with 64 jittered passes and 7 blur passes, pitch sway sin(t / 400)
  * 25 + 20, yaw 0.1 per tick (dev/build/rfg GuiMainMenu.drawPanorama). Panorama mods take the faces at 90 FOV.
- Menu advice found: subtle motion keeps a background alive; darken behind the buttons so they read.
- Launcher news: a picture plus a small info box per item; players found oversized news areas cluttered.
- Taken: real scenes, slow motion, dark behind the buttons, small picture news panels. The panorama was not built:
  the user asked to use Decimation's own menu parts; a camera turning over a city stays an option.

## 8. Full map: every piece of the main menu, home and in game (20 Oktober 2026)

Studied for the main menu skill (.claude/skills/decimation-main-menu, user: "hint agent to make feat/fix/delete/update
in there while mention all code that related with main menu, from in-game and home main menu"). Decimation's side in
detail: docs/decimation_code/menus.md sections 7 and 7b (deobf/src/decimation/gui is its ORIGINAL code: our jar patches
are not in it).

How a menu opens: Decimation's ClientEventHandler.onGuiOpen turns Minecraft's title screen into GuiIntroJumpscare,
GuiIntroBoehModLogo (once each), GuiEulaAgreement (until accepted), GuiUpdateProgress (while a download runs:
UpdateChecker.downloading, which fixes/UpdateProgress sets) or GuiMenuHome (deci/i/e). Esc in a world (GuiIngameMenu)
also becomes GuiMenuHome: ONE screen is the title menu and the pause menu; `mc.theWorld != null` tells them apart.

| Piece (home menu unless said) | Decimation code | Ours | Test |
|---|---|---|---|
| background scenes, slideshow every 7 s | RenderUtil.drawMenuBackground (deci/j/a), only without a world | fixes/MenuBackground, tools/patches/PatchMenuBackground.java, pictures by tools/menu_assets.py | mainmenu |
| logo and "v<version>" under it | GuiMenuBase.drawScreen, textures/gui/menu/banner.png | logo: menu_assets.py logos(); version: fixes/MenuTexts (PatchMenuTexts) | menu, mainmenu |
| footer bottom left: logo (15 %) and copyright line | GuiMenuBase.drawScreen, boehmod.png | menu_assets.py footer(), PatchMenuTexts (our name and site) | menupages |
| title line "DeciRemnant: Zombie Apocalypse", page title | GuiMenuBase.drawScreen, setTitle | PatchMenuTexts | mainmenu |
| Website / Discord links top right | buttons 12, 15 (were Discord, Twitter), ClientResources addresses aZ, bb | PatchMenuTexts | mainmenu |
| profile card: name, Human Kills, Deaths, Zombie Kills, supporter line, player model | GuiMenuBase.drawScreen, BackendProfileCache, MenuPreviewWorld | fixes/LocalStats (numbers), PatchMenuStats (labels); the supporter line is Decimation's again (2g) | stats, menupages |
| home icon + Play (green) / Update / Resume Game / Play Offline | GuiMenuBase.addMenuButtons: Update (id 8) replaces Play (9) AND Resume (23) while UpdateChecker.isUpdateRequired | PatchNoUpdate: isUpdateRequired answers fixes/Updater.required(), false since 2g: never forced | menu, update |
| the update question (Update / Later, Download where it cannot swap) | ConfirmScreen (deci/d/s) | fixes/UpdatePrompt (title menu, or the Esc menu in a world; once a launch); Quit game through fixes/SafeQuit | update |
| Play's click | GuiMenuBase.handleMenuButton case 9: the update page while an update is required, else the official server list | official list: fixes/MenuServers, PatchOfficialServers | menupages |
| Continue (last world) and its tooltip | (none: ours) | fixes/MenuContinue (InitGuiEvent.Post, ActionPerformedEvent.Pre, Deci.menuButton; buttons under it move down 21) | mainmenu |
| My Community >> popup: My Supporter, Website, Forums, GitHub | id 27: SupporterOnlyScreen (deci/d/k, a DeciSubScreen) | PatchMenuTexts (Website and GitHub lines; the supporter line and toast are Decimation's) | menupages |
| Server Status >> page: four rows | id 11: GuiMenuServerStatus (deci/i/h), rows ServerPinger (deci/l/a, a plain socket each, PingStatus deci/l/a$a) | PatchMenuTexts (names, .invalid hosts, the Website row, Unknown reads Coming soon); fixes/MenuStatus sets the first three rows (Deci.setStatus) | menupages |
| Settings >> page, its Updates button | id 5: GuiMenuOptions (deci/i/f); button 1000 opens GuiMenuUpdates | UpdateProgress swaps GuiMenuUpdates (deci/i/l) for Decimation's progress screen and starts our download | update |
| Exit Game (Disconnect in a world) and its confirm box | id 0: ConfirmScreen (deci/d/s, buttons 10 left red and 11 right, ids gm = shutdown) | (unchanged) | |
| news panels: Multiplayer (main only), Updates, Website, Coming soon | ArticlePanel (deci/d/c) | fixes/MenuNews (Deci.addNewsPanel, Deci.newsLine), pictures by menu_assets.py | mainmenu |
| a news panel's detail popup (carousel or release list) | (none: ours, in Decimation's look) | fixes/MenuDetail, fixes/MenuDetails, MenuNews.open, pictures by menu_assets.py | mainmenu |
| corner bottom right (was the FURY store advert) | donor_rank_0 / 1.png drawn by GuiMenuBase.drawScreen | menu_assets.py corner() (our soldier: dev test cornerart), fixes/MenuCorner (click: Discord) | mainmenu |
| totals line bottom centre | GuiMenuBase.drawScreen | PatchMenuStats labels, LocalStats numbers | stats |
| update notice bottom left ("New update available!") | GuiMenuBase.drawScreen while isUpdateRequired | (follows Updater.required()) | |
| welcome box and loading fade (clicks ignored until ClientState.cg <= 0.1) | GuiMenuHome.drawWelcomeNotification, drawLoadingFade; welcome text from the lang file | menu_assets.py (welcome line) | mainmenu ("menu shown") |
| toasts (sliding notices, title DeciRemnant) | ToastNotification (deci/b/b) | Deci.toast, PatchMenuTexts (title) | menupages |
| update progress screen, Quit game / Later | GuiUpdateProgress (deci/i/m) | fixes/UpdateProgress, fixes/UpdateBar; failures: fixes/UpdateScreen (Try again / Download page / Back) | update |
| window title | WindowSetup (deci/m/b) | MenuTexts.windowTitle (PatchMenuTexts) | mainmenu |
| official server list page (Play) | GuiMenuOfficialServers (deci/i/i), rows ListElement (deci/d/p) on ServerListEntry (deci/k/a), ServerListPinger.officialServers (deci/k/b) | MenuServers, PatchOfficialServers, PatchMenuTexts ("Coming soon"), Chernarus pictures by menu_assets.py | menupages |
| Community page (tab on the official list) | GuiMenuCommunityServers (deci/i/j) | main only: fixes/hosting/CommunityList, CommunityText | mainmenu, hostmenu |
| IN GAME (Esc): Resume Game (23), Disconnect (24), LAN globe (25, singleplayer), achievements (26); no background, a dark layer | GuiMenuBase.addMenuButtons with a world | main only: fixes/hosting/HostMenu puts Host Online (9300) in Server Status' place and sends the LAN globe to HostScreen | hostmenu |

## 9. How to change it (feat, fix, delete, update)

- New button: Forge InitGuiEvent.Post on the screen (Deci.isHomeMenu), add `Deci.menuButton(id, x, y, w, h, label,
  tooltip)` (Decimation's own button look), handle it in ActionPerformedEvent.Pre (cancel the event); move the buttons
  below by 21 like MenuContinue. Pick an id no Decimation page uses (ours: 9101, 9102, 9300...).
- Change a Decimation text: add the exact OLD string to tools/patches/PatchMenuTexts.java (a text must be found at least
  once per class or the patch stops), rebuild it from build/menutexts_rebuild/base.jar (section 4b) and zip the classes
  into the three jars (Decimation.jar.patched, dist/Decimation.jar, dev/libs/Decimation-base.jar). Never edit a string
  in deobf/src: that tree is for reading.
- Dynamic text Decimation draws: send the constant to a static method of ours (MenuTexts pattern: PatchMenuTexts
  replaces the constant with a call).
- Remove or hide an element: a button by removing it in InitGuiEvent.Post; something drawn in drawScreen needs a jar
  patch of that draw (PatchMenuBackground removed the cube planet that way), or paint over it in DrawScreenEvent.Post.
- A question to the player: Decimation's ConfirmScreen (deci/d/s) over the screen (setDescription lines, setLabels
  left / right; it returns to the screen under it), clicks caught in ActionPerformedEvent.Pre.
- A notice: Deci.toast(line). A picture: tools/menu_assets.py writes Decimation's menu textures into the three jars
  (Decimation art: never committed).
- News panel: fixes/MenuNews (title, line, address; Deci.addNewsPanel), its picture via menu_assets.py news_<id>.
- Server Status rows: their names and hosts are texts of class deci/i/h in PatchMenuTexts; a row's state is
  ServerPinger.status (Deci.statusRows reads them).
- Official list rows: fixes/MenuServers (Deci.officialServer); the list page itself through PatchOfficialServers.
- Updates: fixes/Updater (check, required, download), UpdateProgress, UpdateScreen; what Decimation calls "update
  required" is Updater.required() (PatchNoUpdate).
- Every change: a dev test check that fails first (menu, mainmenu, menupages, update, stats), then passes; `mainmenu`
  and `menupages` leave the world: run them LAST in a live game.

## 10. Two lines

The base jar (Decimation with our jar patches) is shared by main and prerelease: a jar patch must be fine for both
(texts, drawing, hooks both lines have); main only behaviour (Community page, Host Online, the Multiplayer news panel,
anything of the relay) lives in our classes on main only (CLAUDE.md "TWO LINES SIDE BY SIDE"). Carry plain menu fixes
to prerelease ("from main abc1234") and check the base jar with tools/linecheck.py before a release.

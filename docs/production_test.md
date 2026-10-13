# Testing the shipped game, not the dev client (13 Oktober 2026)

The dev client runs Minecraft with readable names, our classes from Gradle and Decimation from
dev/libs. Bugs that only players see (user: "maybe there is issue with local and production") need the
RELEASE jar in a plain Forge game. Never Prism (off limits): we build our own production client and
server in the scratchpad. Nothing here is committed except this doc and tools/prodclient/classpath.py.

## Production server (dedicated)
1. Forge installer 1.7.10-10.13.4.1614 from maven.minecraftforge.net, `java -jar installer.jar
   --installServer` in an empty folder (Java 8: ~/.jdks/zulu-8.jdk).
2. mods/ = only the release Decimation.jar (dist/onejar), eula.txt, server.properties (level-type
   decimation, seed, port 25577, online-mode false).
3. Start `java -jar forge-...-universal.jar nogui`, wait for "Done (", send `stop`.
Checked with it: bundled content unpacked, cities generated, no errors (bug.md v0.58.0).

## Production client
- Classpath (tools/prodclient/classpath.py DIR): Forge universal FIRST, Forge's libraries (from a
  server install's libraries/ folder), the 1.7.10 client libraries from the Gradle cache (the same
  versions the dev client uses, LWJGL 2.9.4 nightly with osx-arm64 natives extracted into DIR/natives),
  the vanilla client jar LAST (Mojang's version json; sha1 e80d9b3b...).
- Start: `java -Djava.library.path=DIR/natives -Dfml.ignoreInvalidMinecraftCertificates=true
  [-Ddeciworldgen.autotest=true -Ddeciworldgen.autotest.live=true] -cp CP net.minecraft.launchwrapper.Launch
  --username X --version 1.7.10 --gameDir DIR/game --assetsDir ~/.gradle/caches/retro_futura_gradle/assets
  --assetIndex 1.7.10 --uuid 0...1 --accessToken 0 --userProperties {} --userType legacy --tweakClass
  cpw.mods.fml.common.launcher.FMLTweaker`; mods/ = only the release jar.
- With the autotest flags it opens the live test port: `python3 tools/devtest.py --live MODE` drives it
  like the dev client (it connects to whatever listens on 25599). Our dev tests are in the release jar
  (inert without the flag).
- Traps found setting it up:
  1. Do NOT use the Forge universal jar from a server install folder: its manifest Class-Path names
     minecraft_server.1.7.10.jar, which then sits before the client on the classpath; every Forge
     patch reported "binary discrepency" (server classes). Use the universal jar downloaded on its own.
  2. No -XstartOnFirstThread on macOS with LWJGL 2: Decimation's preInit hung in Display.setTitle.
  3. The Gradle cache holds a -javadoc jar next to LWJGL: pick the plain one.
  4. The first test in a new world runs while the player still falls: run it twice.
- GitHub's release API answered 403 (unauthenticated rate limit, 60 requests an hour per address)
  after many checks in one afternoon [inferred]; the updater then shows Play (no update).

## Results (13 Oktober 2026)
- Left click (bug.md, friend's report), dev test `click` in the production client, simulated presses
  (KeyBinding with key code -100 as Minecraft.runTick does): bare hand breaks dirt, also after holding a
  gun (attack key 0 while the gun is held, -100 after), a fire axe hits a pig: all PASS, same as the dev
  client. Not tested: a real physical mouse click (desktop control needs macOS Accessibility / Screen
  Recording permission for the Claude app; `click manual=true` waits for a person's click).

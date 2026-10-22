package net.decimation.worldgen.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.common.registry.GameRegistry;
import net.decimation.fixes.Deci;
import net.decimation.fixes.Durability;
import net.decimation.fixes.GripRails;
import net.decimation.fixes.GunWear;
import net.decimation.fixes.LootPools;
import net.decimation.fixes.MountStrip;
import net.decimation.fixes.SightMounts;
import net.decimation.fixes.gunpack.GunPack;
import net.decimation.fixes.gunpack.GunPackRows;
import net.decimation.fixes.gunpack.GunPacks;
import net.decimation.fixes.gunpack.MeshGun;
import net.decimation.fixes.gunsmith.GunParts;
import net.decimation.fixes.gunsmith.GunSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/**
 * Gun packages (fixes/gunpack, docs/gun_model_spec.md section 7): what the loader found and refused, and for every
 * registered package's gun: the item, its stats against the like gun's plus the overrides, magazines, loot (Decimation's
 * crates and our lists), wear, the Gunsmith's grip and sight rules, a foregrip taken off where the gun has no rail, and
 * on the client its files, model, animations, name, sounds, hands and attachment rows as placed; a stand in named
 * gc<gun> (tools/guns/gunpack_standin.py) must get the same rows as <gun>. Then each gun held, first person:
 * gunpack_<name>.png. Reload, firing, an NPC, aim and cost: modes gun, playerfire, gunview, gunperf with the gun's name.
 */
public class GunPackLiveTest extends DevTestMode
{
    public String name() { return "gunpack"; }

    private static final int SHOTS = 80, EACH = 40;   // a fresh live world needs its first seconds to show the arena
    private volatile int ticks;
    private volatile boolean clientChecked;
    private int shot;

    public boolean client(Minecraft mc)
    {
        if (mc.thePlayer == null)
        {
            return true;
        }
        int t = ticks;
        List<GunPacks.Loaded> packs = GunPacks.loaded();
        if (t >= 10 && !clientChecked)
        {
            clientChecked = true;
            for (GunPacks.Loaded l : packs)
            {
                clientChecks(mc, l);
            }
        }
        if (t >= SHOTS)
        {
            mc.gameSettings.hideGUI = false;
            mc.gameSettings.thirdPersonView = 0;
            mc.thePlayer.inventory.currentItem = 0;
            mc.thePlayer.rotationYaw = 180;
            mc.thePlayer.rotationPitch = 0;
            if (shot < packs.size() && t >= SHOTS + shot * EACH + 28)
            {
                DevTestUtil.screenshot(mc, name(), "gunpack_" + packs.get(shot).pack.name);
                shot++;
            }
        }
        boolean shotsDone = clientChecked && shot >= packs.size() && t >= SHOTS + packs.size() * EACH;
        if (shotsDone && !meshChecked)
        {
            meshChecked = true;
            meshDrawn(packs);
        }
        if (shotsDone && perf && !packs.isEmpty())
        {
            return perf(packs);
        }
        return !shotsDone;
    }

    private boolean meshChecked;

    /**
     * Mesh guns (converter milestone 4) once each was held: Decimation compiled their placeholder parts, so ModelBatch must
     * have put every mesh group into a list of its own (counted since the game started: lists are compiled once).
     */
    private void meshDrawn(List<GunPacks.Loaded> packs)
    {
        int groups = 0, tris = 0;
        for (GunPacks.Loaded l : packs)
        {
            MeshGun m = l.pack.mesh == null ? null : MeshGun.byId(MeshGun.id(l.pack.name));
            if (m != null)
            {
                groups += m.groups.size();
                for (MeshGun.Group g : m.groups)
                {
                    tris += g.triangles();
                }
            }
        }
        if (groups == 0)
        {
            return;
        }
        long lists = net.decimation.fixes.ModelBatch.meshLists, drawn = net.decimation.fixes.ModelBatch.meshTriangles;
        DevTestResults.check(name(), "mesh guns drawn from their meshes", lists + " lists, " + drawn + " triangles",
                             lists >= groups && drawn >= tris, "at least " + groups + " lists and " + tris + " triangles");
    }

    /** perf=true: each package gun's draw against its twin's (gc<gun>) or else its like gun's, glFinish around each. */
    private final boolean perf = "true".equals(System.getProperty("deciworldgen.autotest.perf"));
    private DrawPair probe;

    private boolean perf(List<GunPacks.Loaded> packs)
    {
        if (probe == null)
        {
            List<ItemStack[]> pairs = new ArrayList<ItemStack[]>();
            for (GunPacks.Loaded l : packs)
            {
                Item twin = l.pack.name.startsWith("gc") ? GameRegistry.findItem("deci", l.pack.name.substring(2)) : null;
                twin = twin != null || !l.pack.name.startsWith("gc") ? twin : GameRegistry.findItem("deciworldgen", l.pack.name.substring(2));
                Item other = Deci.isGun(twin) ? twin : l.like;
                pairs.add(new ItemStack[] {stack(l.gun), stack(other)});
            }
            probe = new DrawPair(pairs);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(probe);
            return true;
        }
        if (probe.samples() < DrawPair.SAMPLES)
        {
            return true;
        }
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(probe);
        for (int i = 0; i < probe.pairs.size(); i++)
        {
            ItemStack[] p = probe.pairs.get(i);
            double a = DrawPair.median(probe.times.get(2 * i)), b = DrawPair.median(probe.times.get(2 * i + 1));
            double ca = DrawPair.median(probe.cpu.get(2 * i)), cb = DrawPair.median(probe.cpu.get(2 * i + 1));
            String an = GameRegistry.findUniqueIdentifierFor(p[0].getItem()).name, bn = GameRegistry.findUniqueIdentifierFor(p[1].getItem()).name;
            boolean twin = an.equals("gc" + bn);
            DevTestResults.value(name(), "perf: " + an + " draw", String.format("%.3f ms (CPU %.3f), %s %.3f ms (CPU %.3f), median of %d,"
                + " GPU included", a, ca, bn, b, cb, DrawPair.SAMPLES));
            // a twin is the same model: equal within noise; another gun: the like's cost and half again at most
            boolean ok = twin ? Math.abs(a - b) <= Math.max(0.05, 0.1 * b) : a <= b * 1.5 + 0.05;
            DevTestResults.check(name(), "perf: " + an + " against " + bn, String.format("%+.3f ms", a - b), ok,
                                 twin ? "the same as the gun it copies (within 0.05 ms or 10 %)" : "at most the like gun's x 1.5 + 0.05 ms");
        }
        DevTestResults.check(name(), "perf: GL errors and stack around the draws", probe.errors + " errors, " + probe.depthChanged
                             + " stack changes", probe.errors == 0 && probe.depthChanged == 0, "0 and 0");
        return false;
    }

    private static ItemStack stack(Item gun)
    {
        ItemStack s = new ItemStack(gun);
        s.stackTagCompound = new NBTTagCompound();
        s.stackTagCompound.setInteger("ammo", 0);
        return s;
    }

    /**
     * After the world each frame: every pair's two guns drawn once more as Decimation draws a held gun (their own
     * GunItemRenderer, EQUIPPED, two blocks ahead), glFinish before and after each (GPU included), which goes first
     * alternating per frame; the first 4 frames compile display lists and are not timed.
     */
    public static final class DrawPair
    {
        static final int SAMPLES = 200;
        final List<ItemStack[]> pairs;
        final List<List<Long>> times = new ArrayList<List<Long>>(), cpu = new ArrayList<List<Long>>();
        int frames, errors, depthChanged;
        private net.minecraft.client.renderer.RenderBlocks blocks;

        DrawPair(List<ItemStack[]> pairs)
        {
            this.pairs = pairs;
            for (int i = 0; i < pairs.size() * 2; i++)
            {
                times.add(new ArrayList<Long>());
                cpu.add(new ArrayList<Long>());
            }
        }

        int samples()
        {
            int k = Integer.MAX_VALUE;
            for (List<Long> t : times)
            {
                k = Math.min(k, t.size());
            }
            return k;
        }

        static double median(List<Long> l)
        {
            List<Long> c = new ArrayList<Long>(l);
            java.util.Collections.sort(c);
            return c.isEmpty() ? -1 : c.get(c.size() / 2) / 1e6;
        }

        @cpw.mods.fml.common.eventhandler.SubscribeEvent
        public void onWorldLast(net.minecraftforge.client.event.RenderWorldLastEvent e)
        {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer == null)
            {
                return;
            }
            if (blocks == null)
            {
                blocks = new net.minecraft.client.renderer.RenderBlocks();
            }
            while (org.lwjgl.opengl.GL11.glGetError() != 0)
            {
                // errors of the frame so far: not the draws'
            }
            int d0 = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_MODELVIEW_STACK_DEPTH);
            net.minecraft.util.Vec3 look = mc.thePlayer.getLookVec();
            for (int i = 0; i < pairs.size(); i++)
            {
                for (int j = 0; j < 2; j++)
                {
                    int k = frames % 2 == 0 ? j : 1 - j;
                    ItemStack s = pairs.get(i)[k];
                    net.minecraftforge.client.IItemRenderer r = net.minecraftforge.client.MinecraftForgeClient.getItemRenderer(s,
                        net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED);
                    if (r == null)
                    {
                        continue;
                    }
                    org.lwjgl.opengl.GL11.glPushMatrix();
                    org.lwjgl.opengl.GL11.glTranslated(look.xCoord * 2, look.yCoord * 2, look.zCoord * 2);
                    org.lwjgl.opengl.GL11.glFinish();
                    long t0 = System.nanoTime();
                    r.renderItem(net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED, s, blocks, mc.thePlayer);
                    long submitted = System.nanoTime() - t0;
                    org.lwjgl.opengl.GL11.glFinish();
                    long dt = System.nanoTime() - t0;
                    org.lwjgl.opengl.GL11.glPopMatrix();
                    if (frames >= 4)
                    {
                        times.get(2 * i + k).add(dt);
                        cpu.get(2 * i + k).add(submitted);
                    }
                }
            }
            if (org.lwjgl.opengl.GL11.glGetError() != 0)
            {
                errors++;
            }
            if (org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_MODELVIEW_STACK_DEPTH) != d0)
            {
                depthChanged++;
            }
            frames++;
        }
    }

    public void server()
    {
        int t = ++ticks;
        EntityPlayerMP p = DevTestUtil.player();
        if (p == null)
        {
            return;
        }
        List<GunPacks.Loaded> packs = GunPacks.loaded();
        if (t == 2)
        {
            DevTestArena.build(p.worldObj, p);
            p.worldObj.setWorldTime(6000);
            p.capabilities.isFlying = true;
            p.playerNetServerHandler.setPlayerLocation(DevTestArena.X + 0.5, DevTestArena.Y, DevTestArena.Z + 0.5, 180, 0);
            loaderChecks(packs);
            for (GunPacks.Loaded l : packs)
            {
                serverChecks(p, l);
            }
        }
        for (int i = 0; i < packs.size(); i++)
        {
            if (t == SHOTS + i * EACH)
            {
                ItemStack s = new ItemStack(packs.get(i).gun);
                s.stackTagCompound = new NBTTagCompound();   // gun data ready before the first frame
                s.stackTagCompound.setInteger("ammo", 0);
                p.inventory.currentItem = 0;
                p.inventory.mainInventory[0] = s;
                p.inventoryContainer.detectAndSendChanges();
                p.playerNetServerHandler.setPlayerLocation(DevTestArena.X + 0.5, DevTestArena.Y, DevTestArena.Z + 0.5, 180, 0);
            }
        }
    }

    private void loaderChecks(List<GunPacks.Loaded> packs)
    {
        DevTestResults.value(name(), "packages found", GunPacks.found());
        for (String sh : GunPacks.shadowed())
        {
            DevTestResults.value(name(), "shadowed", sh);
        }
        List<String> refused = GunPacks.problems();
        boolean onlyBroken = true, brokenSeen = false, brokenFound = false;
        for (String f : GunPacks.found())
        {
            brokenFound |= f.startsWith("gcbroken ");
        }
        for (String r : refused)
        {
            DevTestResults.value(name(), "refused", r);
            onlyBroken &= r.contains("gcbroken");
            brokenSeen |= r.contains("gcbroken");
        }
        DevTestResults.check(name(), "only the broken stand in refused", refused.size() + " refused", onlyBroken
            && brokenSeen == brokenFound, "gcbroken refused with its reasons when present, nothing else");
        DevTestResults.check(name(), "a refused package registers nothing", String.valueOf(GameRegistry.findItem("deciworldgen", "gcbroken")),
                             GameRegistry.findItem("deciworldgen", "gcbroken") == null, "no deciworldgen:gcbroken");
        DevTestResults.check(name(), "packages registered", String.valueOf(packs.size()), !packs.isEmpty(),
                             "at least one (tools/guns/gunpack_standin.py, or -Pgunpacks=DIR)");
        double ms = GunPacks.nanos() / 1e6;
        DevTestResults.check(name(), "loader time at startup", String.format("%.1f ms", ms), ms < 2000,
                             "under 2 s for all packages (once, at startup)");
        for (GunPacks.Loaded l : packs)
        {
            DevTestResults.value(name(), l.pack.name + " load", String.format("%.1f ms from %s", l.nanos / 1e6, l.source.where()));
            for (String w : l.warnings)
            {
                DevTestResults.value(name(), l.pack.name + " warning", w);
            }
            for (String r : l.rows)
            {
                DevTestResults.value(name(), l.pack.name + " row", r);
            }
        }
    }

    /** The like gun's fact with this package's override applied: what the package's gun must show. */
    private static Object expected(String key, Object like, GunPack.Stats o)
    {
        if ("damage".equals(key) && o.damage != null)
        {
            return Math.round(o.damage * 0.5f);
        }
        if ("secondsPerShot".equals(key) && o.rpm != null)
        {
            return (double) (1.0f / (o.rpm * 1.3f / 60.0f));
        }
        if ("pitch".equals(key) && o.pitch != null)
        {
            return o.pitch;
        }
        if ("yaw".equals(key) && o.yaw != null)
        {
            return o.yaw;
        }
        if ("recovery".equals(key) && o.recovery != null)
        {
            return o.recovery;
        }
        if ("slowdown".equals(key) && o.slowdown != null)
        {
            return o.slowdown;
        }
        if ("modes".equals(key) && o.auto != null)
        {
            return o.auto ? "[AUTO, SINGLE]" : "[SINGLE]";
        }
        return like;
    }

    private void serverChecks(EntityPlayerMP player, GunPacks.Loaded l)
    {
        GunPack p = l.pack;
        String n = p.name;
        Item gun = GameRegistry.findItem("deciworldgen", n);
        DevTestResults.check(name(), n + " registered", String.valueOf(gun), Deci.isGun(gun) && gun == l.gun
                             && p.category.equals(Deci.gunCategory(gun)), "deciworldgen:" + n + ", a " + p.category);
        if (!Deci.isGun(gun))
        {
            return;
        }
        Map<String, Object> mine = Deci.gunFacts(gun), like = Deci.gunFacts(l.like);
        List<String> wrong = new ArrayList<String>();
        int equal = 0;
        for (Map.Entry<String, Object> e : like.entrySet())
        {
            if (e.getKey().equals("mags") || e.getKey().equals("category"))
            {
                continue;
            }
            Object want = expected(e.getKey(), e.getValue(), p.stats);
            Object got = mine.get(e.getKey());
            boolean same = want instanceof Number && got instanceof Number
                ? Math.abs(((Number) want).doubleValue() - ((Number) got).doubleValue()) < 1e-6 : String.valueOf(want).equals(String.valueOf(got));
            if (same)
            {
                equal++;
            }
            else
            {
                wrong.add(e.getKey() + " " + got + " want " + want);
            }
        }
        DevTestResults.check(name(), n + " stats like " + p.like, equal + " of " + (equal + wrong.size()) + (wrong.isEmpty() ? ""
                             : " " + wrong), wrong.isEmpty(), "the like gun's, overrides " + (p.stats.any() ? "applied" : "none"));
        if (p.magazine == null)
        {
            DevTestResults.check(name(), n + " magazines", String.valueOf(mine.get("mags")), String.valueOf(mine.get("mags"))
                                 .equals(String.valueOf(like.get("mags"))), "the like gun's: " + like.get("mags"));
        }
        else
        {
            Item mag = GameRegistry.findItem("deciworldgen", p.magazine.name);
            boolean ok = mag != null && Deci.gunTakes(gun, mag) && Deci.magazineCapacity(mag) == p.magazine.capacity
                && Deci.magazineBullet(mag) == Deci.magazineBullet(l.likeMagazine);
            DevTestResults.check(name(), n + " magazine", mag + " " + Deci.magazineCapacity(mag), ok,
                                 p.magazine.capacity + " rounds of " + p.magazine.like + "'s bullet, the gun takes it");
        }
        int poolsLike = 0, poolsMine = 0;
        for (Map.Entry<Integer, List<ItemStack>> e : Deci.lootTable().values())
        {
            boolean a = false, b = false;
            for (ItemStack s : e.getValue())
            {
                a |= s != null && s.getItem() == l.like;
                b |= s != null && s.getItem() == gun;
            }
            poolsLike += a ? 1 : 0;
            poolsMine += b ? 1 : 0;
        }
        DevTestResults.check(name(), n + " crate loot", poolsMine + " pools", poolsMine == poolsLike && poolsMine > 0,
                             "where " + p.like + " is: " + poolsLike);
        int listsLike = 0, listsMine = 0;
        String likeName = Item.itemRegistry.getNameForObject(l.like), myName = Item.itemRegistry.getNameForObject(gun);
        for (String list : LootPools.names())
        {
            boolean a = false, b = false;
            for (LootPools.Entry e : LootPools.pool(list).items)
            {
                a |= likeName.equals(e.item);
                b |= myName.equals(e.item);
            }
            listsLike += a ? 1 : 0;
            listsMine += b ? 1 : 0;
        }
        DevTestResults.check(name(), n + " our loot lists", listsMine + " lists", listsMine == listsLike,
                             "the lists naming " + likeName + ": " + listsLike);
        ItemStack s = new ItemStack(gun);
        DevTestResults.check(name(), n + " wear", String.valueOf(GunWear.max(s)), GunWear.max(s) == Durability.gunMax(p.category),
                             "a " + p.category + "'s (" + Durability.gunMax(p.category) + ")");
        boolean railed = p.railed();
        Item foregrip = GameRegistry.findItem("deci", "foregrip"), rk1 = GameRegistry.findItem("deci", "rk1"),
            eotech = GameRegistry.findItem("deci", "eotech558"), reddot = GameRegistry.findItem("deci", "reddot");
        DevTestResults.check(name(), n + " grip rail", GripRails.fits(n, "foregrip") + " / " + GripRails.fits(n, "rk1"),
                             GripRails.fits(n, "foregrip") == railed && GripRails.fits(n, "rk1") == railed,
                             railed ? "a foregrip and the RK-1 (gripRail)" : "no foregrip (no gripRail or not a rifle)");
        boolean fitsGrip = GunParts.fits(GunSlot.GRIP, s, new ItemStack(foregrip)) && GunParts.fits(GunSlot.GRIP, s, new ItemStack(rk1));
        DevTestResults.check(name(), n + " Gunsmith takes a foregrip", String.valueOf(fitsGrip), fitsGrip == railed,
                             String.valueOf(railed));
        boolean sights = p.takesSights() && !"slide".equals(p.points.mount);
        boolean dot = p.takesSights() && "slide".equals(p.points.mount);
        DevTestResults.check(name(), n + " sight mount", SightMounts.fits(n, "eotech558") + " / red dot " + SightMounts.fits(n, "reddot"),
                             SightMounts.fits(n, "eotech558") == sights && SightMounts.fits(n, "reddot") == dot,
                             "mount " + p.points.mount);
        DevTestResults.check(name(), n + " Gunsmith takes the EOTech", String.valueOf(GunParts.fits(GunSlot.SIGHT, s, new ItemStack(eotech))),
                             GunParts.fits(GunSlot.SIGHT, s, new ItemStack(eotech)) == sights, String.valueOf(sights));
        DevTestResults.value(name(), n + " Gunsmith takes the red dot", String.valueOf(GunParts.fits(GunSlot.SIGHT, s, new ItemStack(reddot))));
        // a foregrip fitted anyway (loot, another player): kept on a rail, taken off without one (fixes/MountStrip)
        ItemStack fitted = new ItemStack(gun);
        fitted.stackTagCompound = new NBTTagCompound();
        fitted.stackTagCompound.setString("gripAttach", "foregrip");
        ItemStack before = player.inventory.mainInventory[8];
        player.inventory.mainInventory[8] = fitted;
        int popped = MountStrip.strip(player);
        String left = fitted.stackTagCompound.getString("gripAttach");
        DevTestResults.check(name(), n + " foregrip without a rail pops", left.isEmpty() ? "taken off" : "kept",
                             left.isEmpty() != railed, railed ? "kept (rail)" : "taken off (no rail)");
        player.inventory.mainInventory[8] = before;
        for (int i = 0; i < player.inventory.mainInventory.length && popped > 0; i++)
        {
            ItemStack x = player.inventory.mainInventory[i];
            if (x != null && x.getItem() == foregrip && i != 8)
            {
                player.inventory.mainInventory[i] = null;   // the popped part (only ours went in)
                popped--;
            }
        }
        player.inventoryContainer.detectAndSendChanges();
    }

    private void clientChecks(Minecraft mc, GunPacks.Loaded l)
    {
        GunPack p = l.pack;
        String n = p.name;
        Item gun = l.gun;
        int parts = Deci.gunModelParts(gun), top = l.model.topLevel();
        DevTestResults.check(name(), n + " model", parts + " top level parts", parts == top && parts > 0,
                             "the package's " + top + " (of " + l.model.declared + " declared, children hang from their parent),"
                             + " loaded through the resource manager");
        if (p.mesh != null)
        {
            MeshGun mesh = MeshGun.byId(MeshGun.id(n));
            int tris = 0;
            for (MeshGun.Group g : mesh == null ? new ArrayList<MeshGun.Group>() : mesh.groups)
            {
                tris += g.triangles();
            }
            DevTestResults.check(name(), n + " mesh", mesh == null ? "not registered" : mesh.groups.size() + " groups, " + tris
                                 + " triangles", mesh != null && mesh.groups.size() == top && tris > 0,
                                 "registered under its name's id, a group a placeholder part (" + top + ")");
        }
        List<String> missing = new ArrayList<String>();
        for (String f : new String[] {p.modelPath(), p.texturePath(), p.iconPath(), p.animationPath("Fire"), p.animationPath("Reload1")})
        {
            try
            {
                mc.getResourceManager().getResource(new ResourceLocation("deci", f)).getInputStream().close();
            }
            catch (Exception e)
            {
                missing.add(f);
            }
        }
        DevTestResults.check(name(), n + " files served", missing.isEmpty() ? "all" : "missing " + missing, missing.isEmpty(),
                             "model, texture, icon, Fire, Reload1 as deci:...");
        boolean anims = Deci.gunAnimationFound(n, "Fire") && Deci.gunAnimationFound(n, "Reload1");
        DevTestResults.check(name(), n + " animations", (anims ? "Fire, Reload1" : "missing") + (Deci.gunAnimationFound(n, "Rack")
                             ? ", Rack" : "") + (Deci.gunAnimationFound(n, "SlideBack") ? ", SlideBack" : ""), anims,
                             "Decimation's GunAnimation finds them");
        String shown = new ItemStack(gun).getDisplayName();
        DevTestResults.check(name(), n + " name", shown, p.display.equals(shown), p.display);
        if (l.magazine != null)
        {
            DevTestResults.value(name(), n + " magazine name", new ItemStack(l.magazine).getDisplayName());
        }
        String icon = gun.getIconFromDamage(0) == null ? "none" : gun.getIconFromDamage(0).getIconName();
        DevTestResults.check(name(), n + " icon", icon, ("deci:gun/" + p.category + "/" + n).equals(icon), "deci:gun/" + p.category
                             + "/" + n);
        String likeName = GameRegistry.findUniqueIdentifierFor(l.like).name;
        String from = "guns/" + Deci.gunCategory(l.like) + "/" + likeName + "/";
        Map<String, String> sounds = Deci.gunSoundPaths(n);
        boolean redirected = sounds.size() >= 3;
        for (String path : sounds.values())
        {
            redirected &= path != null && path.startsWith(from);
        }
        DevTestResults.check(name(), n + " sounds", sounds.size() + " entries", redirected, "every one at " + from);
        float[] hands = Deci.gunHands(gun), want = l.hands ? Deci.gunHands(l.like) : null;
        double[] rh = l.model.header("rhPos");
        boolean handsOk = hands != null && (l.hands ? java.util.Arrays.equals(hands, want)
            : rh == null || Math.abs(hands[3] - rh[0]) < 1e-4 && Math.abs(hands[4] - rh[1]) < 1e-4 && Math.abs(hands[5] - rh[2]) < 1e-4);
        DevTestResults.check(name(), n + " hands", l.hands ? "the like gun's" : "the model's", handsOk,
                             l.hands ? likeName + "'s hand poses" : "rhPos / lhPos of the model");
        // rows as placed (Deci.attachmentOffset: the per gun attachment copies)
        List<String> rowWrong = new ArrayList<String>();
        int rows = 0;
        for (String r : l.rows)
        {
            String table = r.substring(0, r.indexOf(':')), line = r.substring(r.indexOf(':') + 2);
            String[] f = line.split(" ");
            float[] at = null;
            float[] wantOff = null;
            if (table.equals(GunPackRows.SIGHT_TABLE))
            {
                at = Deci.attachmentOffset(gun, f[1]);
                wantOff = new float[] {f.length == 4 ? Float.parseFloat(f[3]) : 0f, Float.parseFloat(f[2]), 0f};
            }
            else if (table.equals(GunPackRows.GRIP_TABLE) || table.equals(GunPacks.BARREL))
            {
                at = Deci.attachmentOffset(gun, f[1]);
                wantOff = new float[] {Float.parseFloat(f[2]), Float.parseFloat(f[3]), Float.parseFloat(f[4])};
            }
            if (wantOff == null)
            {
                continue;
            }
            rows++;
            if (at == null || Math.abs(at[0] - wantOff[0]) > 1e-4 || Math.abs(at[1] - wantOff[1]) > 1e-4 || Math.abs(at[2] - wantOff[2]) > 1e-4)
            {
                rowWrong.add(line + " placed " + (at == null ? "nowhere" : at[0] + " " + at[1] + " " + at[2]));
            }
        }
        DevTestResults.check(name(), n + " rows placed", rows + " rows" + (rowWrong.isEmpty() ? "" : " " + rowWrong), rowWrong.isEmpty(),
                             "every sight and grip row on the gun's attachment copies");
        int batched = 0;
        for (String g : l.grips.keySet())
        {
            batched += Deci.attachmentBatch(gun, g) > 0 ? 1 : 0;
        }
        DevTestResults.check(name(), n + " grips from one list", batched + " of " + l.grips.size(), batched == l.grips.size(),
                             "GripPlacement batched every grip copy");
        // a stand in gc<gun> made from <gun>: the same rows as the gun it copies
        Item twin = n.startsWith("gc") ? GameRegistry.findItem("deci", n.substring(2)) : null;
        twin = twin != null || !n.startsWith("gc") ? twin : GameRegistry.findItem("deciworldgen", n.substring(2));
        if (Deci.isGun(twin))
        {
            List<String> differ = new ArrayList<String>();
            int same = 0;
            for (String a : new String[] {"reddot", "2x", "4x", "8x", "dragunovScope", "eotech558", "ta11acog", "specterdr",
                                          "foregrip", "rk1", "laser", "flashlight", "arSuppressor", "smgSuppressor"})
            {
                float[] x = Deci.attachmentOffset(twin, a), y = Deci.attachmentOffset(gun, a);
                if (x == null && y == null)
                {
                    continue;
                }
                if (x != null && y != null && Math.abs(x[0] - y[0]) < 0.002 && Math.abs(x[1] - y[1]) < 0.002 && Math.abs(x[2] - y[2]) < 0.002)
                {
                    same++;
                }
                else
                {
                    differ.add(a + " " + (x == null ? "none" : x[0] + "," + x[1] + "," + x[2]) + " vs "
                               + (y == null ? "none" : y[0] + "," + y[1] + "," + y[2]));
                }
            }
            DevTestResults.check(name(), n + " same rows as " + n.substring(2), same + " same" + (differ.isEmpty() ? "" : " " + differ),
                                 differ.isEmpty() && same > 0, "every attachment placed as on " + n.substring(2));
        }
    }
}

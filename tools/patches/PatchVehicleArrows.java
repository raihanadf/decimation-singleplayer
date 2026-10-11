import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * Javassist patch: arrows (and any other indirect hit by a player) no longer pick up an empty
 * vehicle (bug.md "Military jeep/tank/helicopter destroyed in one hit", roadmap open bug).
 *
 * VehicleEntity (deci.ad.e) attackEntityFrom (func_70097_a) removes an empty vehicle and drops its
 * item whenever DamageSource.getEntity() is a player, except for gun bullets ("gundeci"). An arrow's
 * source is indirect (getEntity = the shooter, getSourceOfDamage = the arrow), so a shot arrow
 * picked the vehicle up; thrown items and the player's own TNT did the same. Melee is handled by
 * fixes/VehicleHitHandler (sneak + punch still picks up).
 *
 * The patch swaps an indirect player source for DamageSource.generic before the method body runs,
 * so those hits take the health path like any other damage. Direct hits (melee, creative punch) and
 * gun bullets are unchanged. Only vanilla classes are referenced: the patched jar does not need ours.
 * Seats and hitboxes (deci.ad.b) forward to the body, so they are covered too.
 *
 *   javac -cp tools/lib/javassist.jar --release 8 -d build/patch_vehicle tools/patches/PatchVehicleArrows.java
 *   java -cp tools/lib/javassist.jar:build/patch_vehicle PatchVehicleArrows dist/Decimation.jar build/patch_vehicle/out
 *   (cd build/patch_vehicle/out && zip <jar> deci/ad/e.class)   # for each of the three jars
 */
public class PatchVehicleArrows {
    public static void main(String[] a) throws Exception {
        ClassPool pool = new ClassPool(true);
        pool.insertClassPath(a[0]);
        pool.insertClassPath("tools/lib/minecraft-1.7.10-srg.jar");
        pool.insertClassPath("tools/lib/forge-1.7.10-srg.jar");

        CtClass vehicle = pool.get("deci.ad.e");
        CtMethod hit = vehicle.getMethod("func_70097_a", "(Lnet/minecraft/util/DamageSource;F)Z");
        hit.insertBefore("{ if ($1 != null && $1.func_76346_g() instanceof net.minecraft.entity.player.EntityPlayer"
                         + " && $1.func_76364_f() != $1.func_76346_g()"
                         + " && !\"gundeci\".equalsIgnoreCase($1.field_76373_n)) {"
                         + " $1 = net.minecraft.util.DamageSource.field_76377_j; } }");
        vehicle.writeFile(a[1]);
        System.out.println("patched deci/ad/e.func_70097_a: indirect player hits take the health path");
    }
}

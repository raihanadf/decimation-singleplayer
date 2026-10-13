import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * Javassist patch: Decimation's loot counts include their upper bound (docs/loot_levels.md section 2,
 * user 13 Oktober 2026: "fix the off by one too").
 *
 * IntRange (deci.aB.a) is half open, [lower, upper), and pick (b) rolls lower + nextInt(upper - lower).
 * Every loot range is built with IntRange.upTo(n) = [1, n), so a crate listed with n items per open
 * gave 1 .. n-1 (military, police, medical, ammo crates and the weapon cabinet: n = 2, always exactly
 * 1), and an entry listed as a stack of n gave 1 .. n-1 of it. IntRange is used by the loot code only
 * (LootTableRegistry, LootPool, LootEntry, the unused LootConfigJson / LootPool2).
 *
 * The patch makes pick inclusive: lower + nextInt(upper - lower + 1), so 1 .. n. contains() is left
 * as it is (nothing in the loot path calls it).
 *
 *   javac -cp tools/lib/javassist.jar --release 8 -d build/patch_loot tools/patches/PatchLootRolls.java
 *   java -cp tools/lib/javassist.jar:build/patch_loot PatchLootRolls dist/Decimation.jar build/patch_loot/out
 *   (cd build/patch_loot/out && zip <jar> deci/aB/a.class)   # for each of the three jars
 */
public class PatchLootRolls {
    public static void main(String[] a) throws Exception {
        ClassPool pool = new ClassPool(true);
        pool.insertClassPath(a[0]);

        CtClass range = pool.get("deci.aB.a");
        CtMethod pick = range.getMethod("b", "(Ljava/util/Random;)I");
        pick.setBody("{ return this.aeX >= this.aeY ? this.aeY : this.aeX + $1.nextInt(this.aeY - this.aeX + 1); }");
        range.writeFile(a[1]);
        System.out.println("patched deci/aB/a.b: loot ranges pick lower .. upper inclusive");
    }
}

import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.*;
import java.io.*;

/** ListInsn CLASS METHOD DESC N: prints instructions N-12 .. N+3 (ordinals as LineTag numbers them). */
public class ListInsn
{
    public static void main(String[] a) throws Exception
    {
        ClassNode c = new ClassNode();
        new ClassReader(Files.readAllBytes(Paths.get(a[0]))).accept(c, 0);
        int n = Integer.parseInt(a[3]);
        for (MethodNode m : c.methods)
        {
            if (!m.name.equals(a[1]) || !m.desc.equals(a[2])) continue;
            Textifier t = new Textifier();
            TraceMethodVisitor v = new TraceMethodVisitor(t);
            int i = 0;
            for (AbstractInsnNode x : m.instructions.toArray())
            {
                if (x.getOpcode() < 0) continue;
                i++;
                if (i < n - 12 || i > n + 3) continue;
                x.accept(v);
                StringWriter sw = new StringWriter();
                t.print(new PrintWriter(sw));
                t.getText().clear();
                System.out.print((i == n ? ">>" : "  ") + i + " " + sw.toString().trim() + "\n");
            }
        }
    }
}

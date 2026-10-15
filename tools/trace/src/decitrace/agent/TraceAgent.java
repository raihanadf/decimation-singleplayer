package decitrace.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import decitrace.Trace;

/**
 * Java agent of the dev call logger (docs/call_trace.md; dev client only, `gradlew runClient -Ptrace`). Every class
 * of the traced packages gets, at the start of each method, a call to Trace.enter(id). Runs on the final bytes of a
 * class (after Forge's own transformers), so names are the dev names: Decimation's obfuscated classes and methods,
 * MCP names for Minecraft. No frames are recomputed: the two added instructions branch nowhere.
 * Argument: packages=deci/,net/decimation/ (internal name prefixes).
 */
public final class TraceAgent implements ClassFileTransformer
{
    private final String[] packages;
    private int failed;

    private TraceAgent(String[] packages)
    {
        this.packages = packages;
    }

    public static void premain(String args, Instrumentation inst)
    {
        String pk = "deci/,net/decimation/,com/boehmod/";
        if (args != null)
        {
            for (String a : args.split(";"))
            {
                if (a.startsWith("packages="))
                {
                    pk = a.substring("packages=".length());
                }
            }
        }
        inst.addTransformer(new TraceAgent(pk.replace('.', '/').split(",")), false);
        System.out.println("[decitrace] call logger agent on, marking " + pk);
    }

    @Override
    public byte[] transform(ClassLoader loader, final String className, Class<?> redefined, ProtectionDomain domain,
                            byte[] bytes)
    {
        if (className == null || className.startsWith("decitrace/") || !traced(className))
        {
            return null;
        }
        try
        {
            ClassReader r = new ClassReader(bytes);
            ClassWriter w = new ClassWriter(r, 0);
            r.accept(new ClassVisitor(Opcodes.ASM5, w)
            {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] ex)
                {
                    MethodVisitor mv = super.visitMethod(access, name, desc, sig, ex);
                    if ((access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0)
                    {
                        return mv;
                    }
                    final int id = Trace.register(className + "." + name + desc);
                    return new MethodVisitor(Opcodes.ASM5, mv)
                    {
                        @Override
                        public void visitCode()
                        {
                            super.visitCode();
                            super.visitLdcInsn(id);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, "decitrace/Trace", "enter", "(I)V", false);
                        }

                        @Override
                        public void visitMaxs(int stack, int locals)
                        {
                            super.visitMaxs(Math.max(stack, 1), locals);
                        }
                    };
                }
            }, 0);
            return w.toByteArray();
        }
        catch (Throwable t)
        {
            if (failed++ < 20)
            {
                System.out.println("[decitrace] left unmarked " + className + ": " + t);
            }
            return null;
        }
    }

    private boolean traced(String className)
    {
        for (String p : packages)
        {
            if (className.startsWith(p))
            {
                return true;
            }
        }
        return false;
    }
}

import java.io.*;
import java.nio.file.*;
import org.objectweb.asm.*;

/** LineTag IN.class OUT.class: gives every instruction its own line number (= its ordinal in the method). */
public class LineTag
{
    public static void main(String[] a) throws Exception
    {
        ClassReader r = new ClassReader(Files.readAllBytes(Paths.get(a[0])));
        ClassWriter w = new ClassWriter(r, 0);
        r.accept(new ClassVisitor(Opcodes.ASM5, w)
        {
            public void visitSource(String s, String d) { }
            public void visitEnd() { super.visitSource("Tagged.java", null); super.visitEnd(); }
            public MethodVisitor visitMethod(int acc, String name, String desc, String sig, String[] ex)
            {
                return new MethodVisitor(Opcodes.ASM5, super.visitMethod(acc, name, desc, sig, ex))
                {
                    int n = 0;
                    void tag() { Label l = new Label(); super.visitLabel(l); super.visitLineNumber(++n, l); }
                    public void visitLineNumber(int line, Label start) { }
                    public void visitInsn(int o) { tag(); super.visitInsn(o); }
                    public void visitIntInsn(int o, int v) { tag(); super.visitIntInsn(o, v); }
                    public void visitVarInsn(int o, int v) { tag(); super.visitVarInsn(o, v); }
                    public void visitTypeInsn(int o, String t) { tag(); super.visitTypeInsn(o, t); }
                    public void visitFieldInsn(int o, String ow, String nm, String d) { tag(); super.visitFieldInsn(o, ow, nm, d); }
                    public void visitMethodInsn(int o, String ow, String nm, String d, boolean i) { tag(); super.visitMethodInsn(o, ow, nm, d, i); }
                    public void visitInvokeDynamicInsn(String nm, String d, Handle h, Object... b) { tag(); super.visitInvokeDynamicInsn(nm, d, h, b); }
                    public void visitJumpInsn(int o, Label l) { tag(); super.visitJumpInsn(o, l); }
                    public void visitLdcInsn(Object c) { tag(); super.visitLdcInsn(c); }
                    public void visitIincInsn(int v, int i) { tag(); super.visitIincInsn(v, i); }
                    public void visitTableSwitchInsn(int mn, int mx, Label d, Label... ls) { tag(); super.visitTableSwitchInsn(mn, mx, d, ls); }
                    public void visitLookupSwitchInsn(Label d, int[] k, Label[] ls) { tag(); super.visitLookupSwitchInsn(d, k, ls); }
                    public void visitMultiANewArrayInsn(String d, int dims) { tag(); super.visitMultiANewArrayInsn(d, dims); }
                };
            }
        }, 0);
        Files.write(Paths.get(a[1]), w.toByteArray());
    }
}

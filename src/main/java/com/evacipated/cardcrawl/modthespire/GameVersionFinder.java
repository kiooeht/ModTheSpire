package com.evacipated.cardcrawl.modthespire;

import org.objectweb.asm.*;
import org.objectweb.asm.ClassVisitor;

public class GameVersionFinder extends ClassVisitor
{
    public GameVersionFinder()
    {
        super(Opcodes.ASM5);
    }

    @Override
    public MethodVisitor visitMethod(int i, String s, String s1, String s2, String[] strings)
    {
        return new MethodVisitor(Opcodes.ASM5)
        {
            @Override
            public AnnotationVisitor visitAnnotationDefault()
            {
                return null;
            }

            @Override
            public AnnotationVisitor visitAnnotation(String s, boolean b)
            {
                return null;
            }

            @Override
            public AnnotationVisitor visitParameterAnnotation(int i, String s, boolean b)
            {
                return null;
            }

            @Override
            public void visitAttribute(Attribute attribute)
            {

            }

            @Override
            public void visitCode()
            {

            }

            @Override
            public void visitFrame(int i, int i1, Object[] objects, int i2, Object[] objects1)
            {

            }

            @Override
            public void visitInsn(int i)
            {

            }

            @Override
            public void visitIntInsn(int i, int i1)
            {

            }

            @Override
            public void visitVarInsn(int i, int i1)
            {

            }

            @Override
            public void visitTypeInsn(int i, String s)
            {

            }

            @Override
            public void visitFieldInsn(int i, String s, String s1, String s2)
            {

            }

            @Override
            public void visitMethodInsn(int i, String s, String s1, String s2)
            {

            }

            @Override
            public void visitJumpInsn(int i, Label label)
            {

            }

            @Override
            public void visitLabel(Label label)
            {

            }

            @Override
            public void visitLdcInsn(Object o)
            {
                if (o instanceof String) {
                    String possibleVersion = (String)o;
                    if (possibleVersion.startsWith("[EARLY_ACCESS]")) {
                        Loader.setGameVersion(possibleVersion.substring("[EARLY_ACCESS]".length()).trim());
                    }
                }
            }

            @Override
            public void visitIincInsn(int i, int i1)
            {

            }

            @Override
            public void visitTableSwitchInsn(int i, int i1, Label label, Label[] labels)
            {

            }

            @Override
            public void visitLookupSwitchInsn(Label label, int[] ints, Label[] labels)
            {

            }

            @Override
            public void visitMultiANewArrayInsn(String s, int i)
            {

            }

            @Override
            public void visitTryCatchBlock(Label label, Label label1, Label label2, String s)
            {

            }

            @Override
            public void visitLocalVariable(String s, String s1, String s2, Label label, Label label1, int i)
            {

            }

            @Override
            public void visitLineNumber(int i, Label label)
            {

            }

            @Override
            public void visitMaxs(int i, int i1)
            {

            }

            @Override
            public void visitEnd()
            {

            }
        };
    }
}

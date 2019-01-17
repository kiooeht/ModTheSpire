package com.evacipated.cardcrawl.modthespire;

import org.objectweb.asm.*;

public class GameBetaFinder extends ClassVisitor
{
    public GameBetaFinder()
    {
        super(Opcodes.ASM5);
        // TODO?
        //super(classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
    {
        MethodVisitor r = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals("<clinit>")) {
            r = new MyMethodAdapter(r);
        }
        return r;
    }

    class MyMethodAdapter extends MethodVisitor
    {
        private boolean isBeta = false;

        MyMethodAdapter(MethodVisitor methodVisitor)
        {
            super(Opcodes.ASM5);
            // TODO?
            //super(methodVisitor);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor)
        {
            super.visitFieldInsn(opcode, owner, name, descriptor);
            if (name.equals("isBeta") && opcode == Opcodes.PUTSTATIC) {
                Loader.STS_BETA = isBeta;
            }
        }

        @Override
        public void visitInsn(int opcode)
        {
            super.visitInsn(opcode);
            if (opcode == Opcodes.ICONST_0) {
                isBeta = false;
            } else if (opcode == Opcodes.ICONST_1) {
                isBeta = true;
            }
        }
    }
}

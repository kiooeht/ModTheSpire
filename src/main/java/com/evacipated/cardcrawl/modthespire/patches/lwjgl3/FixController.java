package com.evacipated.cardcrawl.modthespire.patches.lwjgl3;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.megacrit.cardcrawl.helpers.controller.CInputHelper;
import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.convert.Transformer;

// In addition to this patch, gdx-controllers-lwjgl3 has to be shaded
@SpirePatch2(
    clz = CInputHelper.class,
    method = "initializeIfAble"
)
public class FixController
{
    // We do it this way because a normal instrument won't work because the Display class
    // reference we're trying to remove gets ignored by the instrument
    @SpireRawPatch
    public static void RemoveDisplayReference(CtBehavior ctBehavior) throws CannotCompileException
    {
        ctBehavior.instrument(new MyCodeConverter());
    }

    private static class MyCodeConverter extends CodeConverter
    {
        public MyCodeConverter()
        {
            transformers = new TransformDisplayIsActive(transformers);
        }
    }

    private static class TransformDisplayIsActive extends Transformer
    {
        private static final String className = "org.lwjgl.opengl.Display";
        private static final String methodName = "isActive";
        private static final String methodDescriptor = "()Z";


        /* cache */
        protected CodeAttribute codeAttr;
        protected int newIndex;
        protected ConstPool constPool;

        public TransformDisplayIsActive(Transformer next)
        {
            super(next);
        }

        @Override
        public void initialize(ConstPool cp, CodeAttribute attr) {
            if (constPool != cp) {
                newIndex = 0;
            }
            codeAttr = attr;
        }

        @Override
        public int transform(CtClass clazz, int pos, CodeIterator it, ConstPool cp)
        {
            int c = it.byteAt(pos);
            if (c == INVOKESTATIC) {
                int index = it.u16bitAt(pos + 1);
                String cname = cp.eqMember(methodName, methodDescriptor, index);
                if (cname != null && matchClass(cname, clazz.getClassPool())) {
                    it.writeByte(ICONST_1, pos);
                    it.write16bit(NOP, pos + 1);
                }
            }

            return pos;
        }

        private boolean matchClass(String name, ClassPool pool) {
            if (className.equals(name))
                return true;

            try {
                CtClass clazz = pool.get(name);
                CtClass declClazz = pool.get(className);
                if (clazz.subtypeOf(declClazz))
                    try {
                        CtMethod m = clazz.getMethod(methodName, methodDescriptor);
                        return m.getDeclaringClass().getName().equals(className);
                    }
                    catch (NotFoundException e) {
                        // maybe the original method has been removed.
                        return true;
                    }
            }
            catch (NotFoundException e) {
                return false;
            }

            return false;
        }
    }
}

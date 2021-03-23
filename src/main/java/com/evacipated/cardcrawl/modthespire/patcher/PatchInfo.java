package com.evacipated.cardcrawl.modthespire.patcher;

import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import javassist.*;
import javassist.bytecode.annotation.AnnotationImpl;

import java.lang.reflect.Proxy;

public abstract class PatchInfo
{
    private static int modNum = 0;

    private int modOrder;
    SpirePatch patch;
    CtBehavior ctMethodToPatch;
    CtMethod patchMethod;

    public PatchInfo(CtBehavior ctMethodToPatch, CtMethod patchMethod)
    {
        this.ctMethodToPatch = ctMethodToPatch;
        this.patchMethod = patchMethod;
        this.modOrder = modNum;
    }

    public PatchInfo setSpirePatch(SpirePatch patch)
    {
        this.patch = patch;
        return this;
    }

    public void debugPrint()
    {
        System.out.println("Patch Class: [" + patchClassName() + "]");
        if (isSpirePatch2()) {
            System.out.println(" - SpirePatch2");
        }
        System.out.println(" - Patching [" + ctMethodToPatch.getLongName() + "]");
        System.out.print(" - ");
        System.out.println(debugMsg());
    }

    protected String patchClassName()
    {
        return patchMethod.getDeclaringClass().getName();
    }

    protected abstract String debugMsg();

    public static void nextMod()
    {
        ++modNum;
    }

    final public int modOrdering()
    {
        return modOrder;
    }

    final public boolean isSpirePatch2()
    {
        if (patch == null) {
            return false;
        }

        try {
            AnnotationImpl impl = (AnnotationImpl) Proxy.getInvocationHandler(patch);
            return SpirePatch2.class.getName().equals(impl.getTypeName());
        } catch (IllegalArgumentException | ClassCastException ignored) {
            return false;
        }
    }

    // Lower is earlier
    public abstract int patchOrdering();

    public abstract void doPatch() throws PatchingException;

    protected static boolean paramByRef(Object[] annotations)
    {
        for (Object o : annotations) {
            if (o instanceof ByRef) {
                return true;
            }
        }
        return false;
    }

    // Gets the typename from the ByRef annotation
    protected static String paramByRefTypename(Object[] annotations)
    {
        for (Object o : annotations) {
            if (o instanceof ByRef) {
                return ((ByRef) o).type();
            }
        }
        return "";
    }

    // Gets the typename from the patched method's marameter types
    protected static String paramByRefTypename2(CtBehavior ctMethodToPatch, int index) throws NotFoundException
    {
        if (!Modifier.isStatic(ctMethodToPatch.getModifiers())) {
            --index;
        }
        try {
            return ctMethodToPatch.getParameterTypes()[index].getName();
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    protected static String paramByRefTypenamePrivateCapture(CtBehavior ctMethodToPatch, String paramName) throws NotFoundException
    {
        CtClass ctClass = ctMethodToPatch.getDeclaringClass();
        CtField ctField = ctClass.getField(paramName);
        return ctField.getType().getName();
    }
}

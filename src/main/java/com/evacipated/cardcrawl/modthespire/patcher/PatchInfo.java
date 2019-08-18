package com.evacipated.cardcrawl.modthespire.patcher;

import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import javassist.*;
import javassist.bytecode.*;

import java.util.ArrayList;
import java.util.List;

public abstract class PatchInfo
{
    private static int modNum = 0;

    private int modOrder;
    CtBehavior ctMethodToPatch;
    CtMethod patchMethod;

    public PatchInfo(CtBehavior ctMethodToPatch, CtMethod patchMethod)
    {
        this.ctMethodToPatch = ctMethodToPatch;
        this.patchMethod = patchMethod;
        this.modOrder = modNum;
    }

    public void debugPrint()
    {
        System.out.println("Patch Class: [" + patchClassName() + "]");
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

    // Lower is earlier
    public abstract int patchOrdering();

    public abstract void doPatch() throws PatchingException;

    protected static List<ParamInfo> paramInfo(CtBehavior ctMethod)
    {
        List<ParamInfo> ret = new ArrayList<>();

        MethodInfo methodInfo = ctMethod.getMethodInfo();
        LocalVariableAttribute table = (LocalVariableAttribute) methodInfo.getCodeAttribute().getAttribute(LocalVariableAttribute.tag);
        if (table != null) {
            int position = 0;
            for (int i=0; i<table.tableLength(); ++i) {
                if (table.startPc(i) == 0) {
                    if (position == 0 && !table.variableName(i).equals("this")) {
                        ++position; // Skip to position 1 if `this` doesn't exist (static method)
                    }
                    ret.add(new ParamInfo(position, table.variableName(i)));
                    ++position;
                }
            }
        }

         return ret;
    }

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

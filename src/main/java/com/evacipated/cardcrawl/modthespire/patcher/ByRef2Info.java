package com.evacipated.cardcrawl.modthespire.patcher;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.ByRef2;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtPrimitiveType;

public class ByRef2Info
{
    private final CtMethod patchMethod;
    private final int paramPosition;
    private final CtClass paramType;

    public ByRef2Info(CtMethod patchMethod, int paramPosition, CtClass paramType)
    {
        this.patchMethod = patchMethod;
        this.paramPosition = paramPosition;
        this.paramType = paramType;
    }

    public void doPatch() throws CannotCompileException
    {
        String src = ByRef2.Internal.class.getName() + ".store[" + paramPosition + "] = ";
        CtPrimitiveType ctPrimitive = null;
        if (paramType.isPrimitive()) {
            ctPrimitive = (CtPrimitiveType) paramType;
        }
        if (ctPrimitive != null) {
            src += "new " + ctPrimitive.getWrapperName() + "(";
        }
        src += "$" + (paramPosition+1);
        if (ctPrimitive != null) {
            src += ")";
        }
        src += ";";
        if (Loader.DEBUG) {
            System.out.println(src);
        }
        patchMethod.insertAfter(src);
    }
}

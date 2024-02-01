package com.evacipated.cardcrawl.modthespire.patcher;

import com.evacipated.cardcrawl.modthespire.ModTheSpire;
import com.evacipated.cardcrawl.modthespire.lib.ByRef2;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtPrimitiveType;

import java.util.ArrayList;
import java.util.List;

public class ByRef2Info
{
    public final CtMethod patchMethod;
    private final List<Param> params = new ArrayList<>();

    public ByRef2Info(CtMethod patchMethod, int paramPosition, CtClass paramType)
    {
        this.patchMethod = patchMethod;
        params.add(new Param(paramPosition, paramType));
    }

    public void add(ByRef2Info other)
    {
        params.addAll(other.params);
    }

    public void debugPrint()
    {
        System.out.println("Patch Method: [" + patchMethod.getLongName() + "]");
    }

    public void doPatch() throws CannotCompileException
    {
        StringBuilder src = new StringBuilder("{\n");
        for (Param p : params) {
            src.append(ByRef2.class.getName()).append(".$Internal.$store[").append(p.position).append("] = ");
            CtPrimitiveType ctPrimitive = null;
            if (p.type.isPrimitive()) {
                ctPrimitive = (CtPrimitiveType) p.type;
            }
            if (ctPrimitive != null) {
                src.append("new ").append(ctPrimitive.getWrapperName()).append("(");
            }
            src.append("$").append(p.position + 1);
            if (ctPrimitive != null) {
                src.append(")");
            }
            src.append(";\n");
        }
        src.append("}");
        if (ModTheSpire.DEBUG) {
            System.out.println(src);
        }
        patchMethod.insertAfter(src.toString());
    }

    private static class Param
    {
        final int position;
        final CtClass type;

        Param(int position, CtClass type)
        {
            this.position = position;
            this.type = type;
        }
    }
}

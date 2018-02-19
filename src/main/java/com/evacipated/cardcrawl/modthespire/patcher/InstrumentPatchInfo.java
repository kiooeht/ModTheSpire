package com.evacipated.cardcrawl.modthespire.patcher;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InstrumentPatchInfo extends PatchInfo
{
    private Method method;

    public InstrumentPatchInfo(CtBehavior ctMethodToPatch, Method method)
    {
        super(ctMethodToPatch, null);
        this.method = method;
    }

    @Override
    protected String patchClassName()
    {
        return method.getDeclaringClass().getName();
    }

    @Override
    protected String debugMsg()
    {
        return "Adding Instrument...";
    }

    @Override
    public int patchOrdering()
    {
        return -1;
    }

    @Override
    public void doPatch() throws CannotCompileException, InvocationTargetException, IllegalAccessException
    {
        ExprEditor exprEditor = (ExprEditor)method.invoke(null);
        ctMethodToPatch.instrument(exprEditor);
    }
}

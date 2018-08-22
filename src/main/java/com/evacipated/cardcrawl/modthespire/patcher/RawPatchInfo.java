package com.evacipated.cardcrawl.modthespire.patcher;

import javassist.CtBehavior;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RawPatchInfo extends PatchInfo
{
    private Method method;

    public RawPatchInfo(CtBehavior ctMethodToPatch, Method method)
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
        return "Raw Javassist...";
    }

    @Override
    public int patchOrdering()
    {
        return 5;
    }

    @Override
    public void doPatch() throws PatchingException
    {
        try {
            method.invoke(null, ctMethodToPatch);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new PatchingException(e);
        }
    }
}

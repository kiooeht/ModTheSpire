package com.evacipated.cardcrawl.modthespire.patcher;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtMethod;

public class ReplacePatchInfo extends PatchInfo
{
    public ReplacePatchInfo(CtBehavior ctMethodToPatch, CtMethod replaceMethod)
    {
        super(ctMethodToPatch, replaceMethod);
    }

    @Override
    protected String debugMsg()
    {
        return "Replacing...";
    }

    @Override
    public int patchOrdering()
    {
        return 0;
    }

    @Override
    public void doPatch() throws PatchingException
    {
        try {
            ((CtMethod) ctMethodToPatch).setBody(patchMethod, null);
        } catch (CannotCompileException e) {
            throw new PatchingException(e);
        }
    }
}

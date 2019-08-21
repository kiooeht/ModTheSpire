package com.evacipated.cardcrawl.modthespire.patcher;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtConstructor;
import javassist.CtMethod;

public class PrefixPatchInfo extends ParameterPatchInfo
{
    public PrefixPatchInfo(CtBehavior ctMethodToPatch, CtMethod patchMethod)
    {
        super(ctMethodToPatch, patchMethod);
        canSpireReturn = true;
        canByRefParams = true;
    }

    @Override
    protected String debugMsg()
    {
        return "Adding Prefix...";
    }

    @Override
    public int patchOrdering()
    {
        return 1;
    }

    @Override
    protected void applyPatch(String src) throws CannotCompileException
    {
        if (ctMethodToPatch instanceof CtConstructor && !((CtConstructor) ctMethodToPatch).isClassInitializer()) {
            ((CtConstructor) ctMethodToPatch).insertBeforeBody(src);
        } else {
            ctMethodToPatch.insertBefore(src);
        }
    }
}

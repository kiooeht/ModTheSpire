package com.evacipated.cardcrawl.modthespire.patcher;

import javassist.CtMethod;

public class NonStaticPatchMethodException extends Exception
{
    public NonStaticPatchMethodException(CtMethod patchMethod)
    {
        super(patchMethod.getLongName());
    }
}

package com.evacipated.cardcrawl.modthespire.patcher;

import javassist.CtClass;
import javassist.CtMethod;

public class ByRef2Info
{
    public final CtMethod patchMethod;
    public final int paramPosition;
    public final CtClass paramType;

    public ByRef2Info(CtMethod patchMethod, int paramPosition, CtClass paramType)
    {
        this.patchMethod = patchMethod;
        this.paramPosition = paramPosition;
        this.paramType = paramType;
    }
}

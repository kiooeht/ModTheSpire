package com.evacipated.cardcrawl.modthespire.patcher;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import javassist.CtClass;

public class MissingParamTypesException extends Exception
{
    public MissingParamTypesException(CtClass patchClass, SpirePatch patch)
    {
        super(String.format("Patch %s\nPatching %s.%s:\nHas overloads and no paramtypes defined", patchClass.getName(), patch.cls(), patch.method()));
    }
}

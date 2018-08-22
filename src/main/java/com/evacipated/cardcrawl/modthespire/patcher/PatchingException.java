package com.evacipated.cardcrawl.modthespire.patcher;

import javassist.CtBehavior;
import javassist.CtClass;

public class PatchingException extends Exception
{
    public PatchingException(CtClass ctClass, String msg)
    {
        super(ctClass.getName() + ": " + msg);
    }

    public PatchingException(CtBehavior m, String msg)
    {
        super(m.getDeclaringClass().getName() + "." + m.getName() + ": " + msg);
    }

}

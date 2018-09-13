package com.evacipated.cardcrawl.modthespire.patcher;

import javassist.CtBehavior;
import javassist.CtClass;

public class PatchingException extends Exception
{
    public PatchingException(String msg)
    {
        super(msg);
    }

    public PatchingException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    public PatchingException(Throwable cause)
    {
        super(cause);
    }

    public PatchingException(CtClass ctClass, String msg)
    {
        super(ctClass.getName() + ": " + msg);
    }

    public PatchingException(CtBehavior m, String msg)
    {
        super(m.getLongName() + ": " + msg);
    }

}

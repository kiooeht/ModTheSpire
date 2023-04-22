package com.evacipated.cardcrawl.modthespire.patcher;

public class SpireMethodException extends PatchingException
{
    public SpireMethodException(String msg, Object... args)
    {
        super(String.format(msg, args));
    }
}

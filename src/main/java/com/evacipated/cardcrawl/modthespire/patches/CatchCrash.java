package com.evacipated.cardcrawl.modthespire.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.megacrit.cardcrawl.core.ExceptionHandler;

public class CatchCrash
{
    @SpirePatch2(
        clz=ExceptionHandler.class,
        method="handleException"
    )
    public static class MegaCritHandler
    {
        public static void Prefix(Exception e)
        {
            HandleCrash.crash = e;
        }
    }
}

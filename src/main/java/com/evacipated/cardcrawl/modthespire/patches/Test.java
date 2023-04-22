package com.evacipated.cardcrawl.modthespire.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireMethod;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.relics.RunicDome;

class Test
{
    @SpirePatch(
        clz = RunicDome.class,
        method = SpirePatch.CLASS
    )
    static class Patch1
    {
        @SpireMethod(from = TestInterface.class)
        static int testMethod(RunicDome __instance)
        {
            return 3;
        }
    }

    @SpirePatch(
        clz = RunicDome.class,
        method = "onEquip"
    )
    static class Patch2
    {
        static void Postfix(RunicDome __instance)
        {
            ((TestInterface) __instance).testMethod();
        }
    }

    public interface TestInterface
    {
        int testMethod();
    }
}

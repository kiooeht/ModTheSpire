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
            System.out.println("Patch1");
            return 3;
        }
    }

    @SpirePatch(
        clz = RunicDome.class,
        method = SpirePatch.CLASS
    )
    static class Patch2
    {
        @SpireMethod(from = TestInterface.class)
        static int testMethod(RunicDome __instance)
        {
            System.out.println("Patch2");
            return 7;
        }
    }

    @SpirePatch(
        clz = RunicDome.class,
        method = "onEquip"
    )
    static class Patch3
    {
        static void Postfix(RunicDome __instance)
        {
            int i = ((TestInterface) __instance).testMethod();
            System.out.println(i);
        }
    }

    public interface TestInterface
    {
        int testMethod();
    }
}

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
        static int testMethod(int __result, RunicDome __instance, SpireMethod.Super<Integer> __super, boolean b, String s)
        {
            System.out.println("Patch1: " + __result);
            if (__super.timesInvoked() == 0) {
                return __super.invoke(b, s);
            }
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
        static int testMethod(int __result, RunicDome __instance, SpireMethod.Super<Integer> __super, boolean b, String s)
        {
            System.out.println("Patch2: " + __result);
            if (__super.timesInvoked() == 0) {
                return __super.invoke(b, s);
            }
            return 7;
        }
    }

    @SpirePatch(
        clz = RunicDome.class,
        method = "onEquip"
    )
    static class PatchTest
    {
        static void Postfix(RunicDome __instance)
        {
            int i = ((TestInterface) __instance).testMethod(true, "asdf");
            System.out.println(i);
        }
    }

    public interface TestInterface
    {
        default int testMethod(boolean b, String s)
        {
            System.out.println("default testMethod");
            System.out.println(b + ", " + s);
            return 1;
        }
    }
}

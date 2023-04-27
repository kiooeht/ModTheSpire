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
        static int testMethod(SpireMethod.Helper<RunicDome, Integer> __helper, boolean b, String s)
        {
            System.out.println("Patch1: " + __helper.hasResult() + ": " + __helper.result());
            System.out.println(__helper.instance().name);
            if (!__helper.wasSuperCalled()) {
                return __helper.callSuper(b, s);
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
        static int testMethod(SpireMethod.Helper<RunicDome, Integer> __helper, boolean b, String s)
        {
            System.out.println("Patch2: " + __helper.hasResult() + ": " + __helper.result());
            System.out.println(__helper.instance().description);
            if (!__helper.wasSuperCalled()) {
                return __helper.callSuper(b, s);
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

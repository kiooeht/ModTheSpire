package com.evacipated.cardcrawl.modthespire.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireMethod;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.relics.AbstractRelic;
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
            System.out.println("super called: " + __helper.timesSuperCalled());
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
        @SpireMethod(from = TestInterface.class, methodName = "testMethod")
        static int someOtherName(SpireMethod.Helper<RunicDome, Integer> __helper, boolean b, String s)
        {
            System.out.println("Patch2: " + __helper.hasResult() + ": " + __helper.result());
            System.out.println("super called: " + __helper.timesSuperCalled());
            System.out.println(__helper.instance().description);
            if (!__helper.wasSuperCalled()) {
                return __helper.callSuper(b, s);
            }
            return 7;
        }

        @SpireMethod(from = TestInterface.class)
        static void voidTest(SpireMethod.Helper<RunicDome, Void> __helper)
        {
            __helper.callSuper();
            System.out.println("void test");
        }

        @SpireMethod(from = AbstractRelic.class)
        static void atBattleStart(SpireMethod.Helper<RunicDome, Void> __helper)
        {
            __helper.callSuper();
            System.out.println("atBattleStart");
        }
    }

    @SpirePatch(
        clz = RunicDome.class,
        method = SpirePatch.CLASS
    )
    static class Patch3
    {
        @SpireMethod(from = DupInterface.class)
        static int testMethod(SpireMethod.Helper<RunicDome, Integer> __helper, boolean b, String s)
        {
            System.out.println("Patch3: " + __helper.hasResult() + ": " + __helper.result());
            System.out.println("super called: " + __helper.timesSuperCalled());
            if (!__helper.wasSuperCalled()) {
                return __helper.callSuper(b, s);
            }
            return 9;
        }

        @SpireMethod(from = SignatureTestInterface.class)
        static int testMethod(SpireMethod.Helper<RunicDome, Integer> __helper, float f1, float f2, float f3)
        {
            System.out.println("Patch3: " + __helper.hasResult() + ": " + __helper.result());
            System.out.println("super called: " + __helper.timesSuperCalled());
            return __helper.callSuper(f1, f2, f3);
        }
    }

    @SpirePatch(
        clz = RunicDome.class,
        method = SpirePatch.CLASS
    )
    static class Patch4
    {
        @SpireMethod(from = ReturnTestInterface.class)
        static float testMethod(SpireMethod.Helper<RunicDome, Integer> __helper, boolean b, String s)
        {
            System.out.println("Patch4: " + __helper.hasResult() + ": " + __helper.result());
            System.out.println("super called: " + __helper.timesSuperCalled());
            return 19.1f;
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
            i = ((SignatureTestInterface) __instance).testMethod(1.2f, 3.9f, 0.3f);
            System.out.println(i);
            ((TestInterface) __instance).voidTest();
            System.out.println(((ReturnTestInterface) __instance).testMethod(true, "string"));
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

        void voidTest();
    }

    public interface DupInterface
    {
        default int testMethod(boolean b, String s)
        {
            System.out.println("duplicate testMethod");
            System.out.println(b + ", " + s);
            return 2;
        }
    }

    public interface SignatureTestInterface
    {
        default int testMethod(float f1, float f2, float f3)
        {
            System.out.println("signature testMethod");
            System.out.println(f1 + ", " + f2 + ", " + f3);
            return -2;
        }
    }

    public interface ReturnTestInterface
    {
        float testMethod(boolean b, String s);
    }
}

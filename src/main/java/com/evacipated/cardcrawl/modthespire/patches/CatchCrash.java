package com.evacipated.cardcrawl.modthespire.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.megacrit.cardcrawl.core.ExceptionHandler;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class CatchCrash
{
    @SpirePatch(
        cls="com.badlogic.gdx.backends.lwjgl.LwjglApplication$1",
        method="run"
    )
    public static class GdxHandler
    {
        public static ExprEditor Instrument()
        {
            return new ExprEditor()
            {
                @Override
                public void edit(MethodCall m) throws CannotCompileException
                {
                    if (m.getMethodName().equals("setCursorCatched")) {
                        m.replace(
                            DisableGdxForceExit.class.getName() + ".crash = t;" +
                                "$_ = $proceed($$);"
                        );
                    }
                }
            };
        }
    }

    @SpirePatch2(
        clz=ExceptionHandler.class,
        method="handleException"
    )
    public static class MegaCritHanlder
    {
        public static void Prefix(Exception e)
        {
            DisableGdxForceExit.crash = e;
        }
    }
}

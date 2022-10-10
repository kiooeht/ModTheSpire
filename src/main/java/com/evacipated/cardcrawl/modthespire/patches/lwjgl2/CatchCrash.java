package com.evacipated.cardcrawl.modthespire.patches.lwjgl2;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.patches.HandleCrash;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

@SpirePatch(
    cls="com.badlogic.gdx.backends.lwjgl.LwjglApplication$1",
    method="run"
)
public class CatchCrash
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
                        HandleCrash.class.getName() + ".crash = t;" +
                            "$_ = $proceed($$);"
                    );
                }
            }
        };
    }
}

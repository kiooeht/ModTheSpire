package com.evacipated.cardcrawl.modthespire.patches;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

@SpirePatch(
    clz=LwjglApplication.class,
    method="mainLoop"
)
public class DisableGdxForceExit
{
    public static Throwable crash = null;

    public static void maybeExit()
    {
        if (crash != null) {
            System.out.println("Game crashed.");
            System.out.println("Cause:");
            crash.printStackTrace();
            Loader.restoreWindowOnCrash();
        } else {
            System.out.println("Game closed.");
            Loader.closeWindow();
        }
    }

    public static ExprEditor Instrument()
    {
        return new ExprEditor() {
            @Override
            public void edit(FieldAccess f) throws CannotCompileException
            {
                if (f.isReader() && f.getFieldName().equals("forceExit")) {
                    f.replace(
                            DisableGdxForceExit.class.getName() + ".maybeExit();" +
                            "$_ = false;"
                    );
                }
            }
        };
    }
}

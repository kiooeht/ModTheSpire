package com.evacipated.cardcrawl.modthespire.patches.lwjgl2;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.patches.HandleCrash;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

@SpirePatch(
    clz= LwjglApplication.class,
    method="mainLoop"
)
public class Lwjgl2DisableGdxForceExit
{
    public static ExprEditor Instrument()
    {
        return new ExprEditor() {
            @Override
            public void edit(FieldAccess f) throws CannotCompileException
            {
                if (f.isReader() && f.getFieldName().equals("forceExit")) {
                    f.replace(
                        HandleCrash.class.getName() + ".maybeExit();" +
                            "$_ = false;"
                    );
                }
            }
        };
    }
}

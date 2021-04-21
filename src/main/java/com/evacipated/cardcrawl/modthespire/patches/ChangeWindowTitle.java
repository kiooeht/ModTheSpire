package com.evacipated.cardcrawl.modthespire.patches;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.desktop.DesktopLauncher;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

@SpirePatch(
    clz=DesktopLauncher.class,
    method="main"
)
public class ChangeWindowTitle
{
    public static ExprEditor Instrument()
    {
        return new ExprEditor() {
            @Override
            public void edit(FieldAccess f) throws CannotCompileException
            {
                if (f.isWriter() && f.getClassName().equals(LwjglApplicationConfiguration.class.getName())
                    && f.getFieldName().equals("title")) {
                    f.replace("$proceed(\"Modded \" + $1);");
                }
            }
        };
    }
}

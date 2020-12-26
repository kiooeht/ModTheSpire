package com.evacipated.cardcrawl.modthespire.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.Settings;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

@SpirePatch(
    clz=Settings.class,
    method=SpirePatch.STATICINITIALIZER
)
public class IsModded
{
    public static ExprEditor Instrument()
    {
        return new ExprEditor() {
            @Override
            public void edit(FieldAccess f) throws CannotCompileException
            {
                if (f.isWriter() && f.getFieldName().equals("isModded")) {
                    f.replace("$proceed(true);");
                } else if (f.isWriter() && f.getFieldName().equals("isDev")) {
                    f.replace("$proceed(false);");
            }
            }
        };
    }
}

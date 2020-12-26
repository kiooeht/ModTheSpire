package com.evacipated.cardcrawl.modthespire.patches;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

@SpirePatch(
    clz=CardCrawlGame.class,
    method=SpirePatch.STATICINITIALIZER
)
public class VersionNum
{
    public static ExprEditor Instrument()
    {
        return new ExprEditor() {
            @Override
            public void edit(FieldAccess f) throws CannotCompileException
            {
                if (f.isWriter() && f.getFieldName().equals("VERSION_NUM")) {
                    f.replace("$proceed($1 + \" [ModTheSpire " + Loader.MTS_VERSION.toString() + "]\");");
                }
            }
        };
    }
}

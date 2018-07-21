package com.evacipated.cardcrawl.modthespire.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

@SpirePatch(
    cls="com.megacrit.cardcrawl.screens.mainMenu.MenuPanelScreen",
    method="initializePanels"
)
public class AlwaysEnableCustomMode
{
    public static ExprEditor Instrument()
    {
        return new ExprEditor() {
            @Override
            public void edit(FieldAccess f) throws CannotCompileException
            {
                if (f.getClassName().equals("com.megacrit.cardcrawl.screens.stats.CharStat")
                    && f.getFieldName().equals("highestDaily")) {
                    f.replace("$_ = 1;");
                }
            }
        };
    }
}

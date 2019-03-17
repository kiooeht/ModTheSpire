package com.evacipated.cardcrawl.modthespire.patches;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

@SpirePatch(
    clz=MainMenuScreen.class,
    method="render"
)
public class MainMenuModList
{
    public static ExprEditor Instrument()
    {
        return new ExprEditor() {
            @Override
            public void edit(FieldAccess f) throws CannotCompileException
            {
                if (f.getFieldName().equals("VERSION_INFO")) {
                    f.replace(String.format("$_ = %s.alterVersion($proceed($$));", MainMenuModList.class.getName()));
                }
            }

            @Override
            public void edit(MethodCall m) throws CannotCompileException
            {
                if (m.getMethodName().equals("renderSmartText")) {
                    m.replace(
                        "{" +
                            String.format("$5 = %s.getSmartHeight($2, $3, $6, $7);", MainMenuModList.class.getName()) +
                            "$proceed($$);" +
                            "}"
                    );
                }
            }
        };
    }

    public static String alterVersion(String version)
    {
        String ver = CardCrawlGame.VERSION_NUM;
        String mtsver = ver.substring(ver.indexOf("ModTheSpire"), ver.length()-1);
        ver = ver.substring(0, ver.indexOf(" [ModTheSpire"));
        return MainMenuScreen.TEXT[0] + " NL " + ver + " NL " + mtsver + " - " + Loader.MODINFOS.length + " mod" + (Loader.MODINFOS.length > 1 ? "s" : "");
    }

    public static float getSmartHeight(BitmapFont font, String msg, float lineWidth, float lineSpacing)
    {
        return -FontHelper.getSmartHeight(font, msg, lineWidth, lineSpacing) + lineSpacing;
    }
}

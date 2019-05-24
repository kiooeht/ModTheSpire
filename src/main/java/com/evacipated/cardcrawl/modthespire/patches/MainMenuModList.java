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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static Pattern re = Pattern.compile("(\\[.+] \\(.+\\)(?:v\\d)*) \\[(ModTheSpire .+)]( BETA)?");

    public static String alterVersion(String version)
    {
        Matcher m = re.matcher(version);
        if (!m.find()) {
            return version;
        }

        String ver = m.group(1);
        String mtsver = m.group(2);
        String beta = m.group(3);
        return ver + (beta == null ? "" : beta) + " NL " + makeMTSVersionModCount(mtsver);
    }

    public static String alterVersion2(String version)
    {
        Matcher m = re.matcher(version);
        if (!m.find()) {
            return version;
        }

        String ver = m.group(1);
        String beta = m.group(3);
        return ver + (beta == null ? "" : beta);
    }

    public static String makeMTSVersionModCount(String version)
    {
        return version + " - " + Loader.MODINFOS.length + " mod" + (Loader.MODINFOS.length > 1 ? "s" : "");
    }

    public static float getSmartHeight(BitmapFont font, String msg, float lineWidth, float lineSpacing)
    {
        return -FontHelper.getSmartHeight(font, msg, lineWidth, lineSpacing) + lineSpacing;
    }
}

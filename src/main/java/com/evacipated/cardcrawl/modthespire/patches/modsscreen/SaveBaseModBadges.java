package com.evacipated.cardcrawl.modthespire.patches.modsscreen;

import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

@SpirePatch(
    cls="basemod.BaseMod",
    method="registerModBadge",
    optional=true
)
public class SaveBaseModBadges
{
    @SpireInsertPatch(
        rloc=8,
        localvars={"badge"}
    )
    public static void Insert(Texture t, String name, String author, String desc, Object settingsPanel, Object badge)
    {
        if (ModsScreen.baseModBadges == null) {
            ModsScreen.baseModBadges = new HashMap<>();
        }

        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        try {
            Class<?> cls = SaveBaseModBadges.class.getClassLoader().loadClass(stacktrace[3].getClassName());
            URL modFile = cls.getProtectionDomain().getCodeSource().getLocation().toURI().toURL();
            ModsScreen.baseModBadges.put(modFile, badge);
        } catch (ClassNotFoundException | URISyntaxException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static ExprEditor Instrument()
    {
        return new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException
            {
                if (m.getMethodName().equals("add")) {
                    m.replace("$_ = true;");
                }
            }
        };
    }
}

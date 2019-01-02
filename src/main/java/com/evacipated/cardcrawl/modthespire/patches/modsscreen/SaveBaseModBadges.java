package com.evacipated.cardcrawl.modthespire.patches.modsscreen;

import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
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
            URL location = cls.getProtectionDomain().getCodeSource().getLocation();
            if (location == null) {
                try {
                    ClassPool pool = Loader.getClassPool();
                    CtClass ctCls = pool.get(cls.getName());
                    String url = ctCls.getURL().getFile();
                    int i = url.lastIndexOf('!');
                    url = url.substring(0, i);
                    location = new URL(url);
                } catch (NotFoundException | MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            if (location != null) {
                URL modFile = location.toURI().toURL();
                ModsScreen.baseModBadges.put(modFile, badge);
            }
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

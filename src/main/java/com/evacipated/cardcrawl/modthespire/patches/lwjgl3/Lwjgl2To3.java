package com.evacipated.cardcrawl.modthespire.patches.lwjgl3;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglGraphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowConfiguration;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.desktop.DesktopLauncher;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.lang.reflect.Field;
import java.net.MalformedURLException;

@SpirePatch(
    clz = DesktopLauncher.class,
    method = "main"
)
public class Lwjgl2To3
{
    public static void Raw(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException, MalformedURLException
    {
        CtClass ctClass = ctBehavior.getDeclaringClass();
        ClassPool pool = ctClass.getClassPool();

        CtClass configClass = pool.get(LwjglApplicationConfiguration.class.getName());
        CodeConverter codeConverter = new CodeConverter();
        CtField f = configClass.getDeclaredField("setDisplayModeCallback");
        codeConverter.replaceFieldWrite(f, pool.get(Lwjgl2To3.class.getName()), "no");
        f = configClass.getDeclaredField("resizable");
        codeConverter.replaceFieldWrite(f, pool.get(Lwjgl2To3.class.getName()), "setResizable");
        f = configClass.getDeclaredField("title");
        codeConverter.replaceFieldWrite(f, pool.get(Lwjgl2To3.class.getName()), "setTitle");

        f = configClass.getDeclaredField("width");
        codeConverter.replaceFieldWrite(f, pool.get(Lwjgl2To3.class.getName()), "setWidth");
        f = configClass.getDeclaredField("height");
        codeConverter.replaceFieldWrite(f, pool.get(Lwjgl2To3.class.getName()), "setHeight");
        f = configClass.getDeclaredField("foregroundFPS");
        codeConverter.replaceFieldWrite(f, pool.get(Lwjgl2To3.class.getName()), "setForegroundFPS");
        codeConverter.replaceFieldRead(f, pool.get(Lwjgl2To3.class.getName()), "getForegroundFPS");
        f = configClass.getDeclaredField("backgroundFPS");
        codeConverter.replaceFieldWrite(f, pool.get(Lwjgl2To3.class.getName()), "setBackgroundFPS");
        f = configClass.getDeclaredField("fullscreen");
        codeConverter.replaceFieldWrite(f, pool.get(Lwjgl2To3.class.getName()), "setFullscreen");
        codeConverter.replaceFieldRead(f, pool.get(Lwjgl2To3.class.getName()), "getFullscreen");
        f = configClass.getDeclaredField("vSyncEnabled");
        codeConverter.replaceFieldWrite(f, pool.get(Lwjgl2To3.class.getName()), "setVSync");

        f = configClass.getDeclaredField("preferencesDirectory");
        codeConverter.replaceFieldRead(f, pool.get(Lwjgl2To3.class.getName()), "getPreferencesDirectory");
        ctClass.instrument(codeConverter);

        ctClass.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if (m.getClassName().equals(LwjglApplicationConfiguration.class.getName()) && m.getMethodName().equals("addIcon")) {
                    m.replace("$_ = " + Lwjgl2To3.class.getName() + ".setWindowIcon($0, $$);");
                }
            }
        });

        ClassMap classMap = new ClassMap();
        classMap.put(LwjglApplicationConfiguration.class.getName(), Lwjgl3ApplicationConfiguration.class.getName());
        classMap.put(LwjglApplication.class.getName(), Lwjgl3Application.class.getName());

        CtMethod main = ctClass.getDeclaredMethod("main");
        replaceMethod(main, ctClass, classMap);

        CtMethod loadSettings = ctClass.getDeclaredMethod("loadSettings");
        replaceMethod(loadSettings, ctClass, classMap);
    }

    private static void replaceMethod(CtMethod oldMethod, CtClass declaring, ClassMap classMap) throws CannotCompileException, NotFoundException
    {
        CtMethod newMethod = CtNewMethod.copy(oldMethod, declaring, classMap);
        declaring.removeMethod(oldMethod);
        declaring.addMethod(newMethod);
    }

    public static void no(Object obj, LwjglGraphics.SetDisplayModeCallback param) {}
    private static int width = 1910;
    private static int height = 1080;
    public static void setWidth(Object obj, int param)
    {
        width = param;
        ((Lwjgl3WindowConfiguration) obj).setWindowedMode(width, height);
    }
    public static void setHeight(Object obj, int param)
    {
        height = param;
        ((Lwjgl3WindowConfiguration) obj).setWindowedMode(width, height);
    }
    public static int getForegroundFPS(Object obj)
    {
        return 60;
    }
    public static void setForegroundFPS(Object obj, int param)
    {
        // TODO
    }
    public static void setBackgroundFPS(Object obj, int param)
    {
        ((Lwjgl3ApplicationConfiguration) obj).setIdleFPS(param);
    }
    public static Object setWindowIcon(Object obj, String path, Files.FileType fileType)
    {
        ((Lwjgl3WindowConfiguration) obj).setWindowIcon(fileType, path);
        return null;
    }
    public static void setResizable(Object obj, boolean param)
    {
        ((Lwjgl3WindowConfiguration) obj).setResizable(param);
    }
    public static void setTitle(Object obj, String param)
    {
        ((Lwjgl3WindowConfiguration) obj).setTitle(param);
    }
    // TODO fullscreen stuff
    public static void setFullscreen(Object obj, boolean param) {}
    public static boolean getFullscreen(Object obj)
    {
        return false;
    }
    public static void setVSync(Object obj, boolean param)
    {
        ((Lwjgl3ApplicationConfiguration) obj).useVsync(param);
        // TODO lwjgl3 has no fps limiter
        ((Lwjgl3ApplicationConfiguration) obj).useVsync(true);
    }
    public static String getPreferencesDirectory(Object obj)
    {
        try {
            Field f = Lwjgl3ApplicationConfiguration.class.getDeclaredField("preferencesDirectory");
            f.setAccessible(true);
            return (String) f.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}

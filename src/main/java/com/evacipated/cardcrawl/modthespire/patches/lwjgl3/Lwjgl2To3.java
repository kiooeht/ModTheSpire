package com.evacipated.cardcrawl.modthespire.patches.lwjgl3;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglGraphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowConfiguration;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.DisplayConfig;
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
        codeConverter.replaceFieldRead(f, pool.get(Lwjgl2To3.class.getName()), "getWidth");
        f = configClass.getDeclaredField("height");
        codeConverter.replaceFieldWrite(f, pool.get(Lwjgl2To3.class.getName()), "setHeight");
        codeConverter.replaceFieldRead(f, pool.get(Lwjgl2To3.class.getName()), "getHeight");
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
        loadSettings = replaceMethod(loadSettings, ctClass, classMap);

        final int[] insertLine = {-1};
        loadSettings.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if (m.getClassName().equals(System.class.getName()) && m.getMethodName().equals("setProperty")) {
                    m.replace("$_ = " + Lwjgl2To3.class.getName() + ".setProperty(config, $$);");
                } else if (m.getClassName().equals(Lwjgl3ApplicationConfiguration.class.getName()) && m.getMethodName().equals("getDesktopDisplayMode")) {
                    m.replace("$_ = $0.getDisplayMode($$);");
                } else if (m.getMethodName().equals("setVSync")) {
                    insertLine[0] = m.getLineNumber();
                }
            }
        });
        if (insertLine[0] >= 0) {
            loadSettings.insertAt(insertLine[0], Lwjgl2To3.class.getName() + ".ReadXY(displayConf);");
        }
        loadSettings.insertAfter(Lwjgl2To3.class.getName() + ".FinishConfig(config);");
    }

    private static CtMethod replaceMethod(CtMethod oldMethod, CtClass declaring, ClassMap classMap) throws CannotCompileException, NotFoundException
    {
        CtMethod newMethod = CtNewMethod.copy(oldMethod, declaring, classMap);
        declaring.removeMethod(oldMethod);
        declaring.addMethod(newMethod);
        return newMethod;
    }

    public static void ReadXY(DisplayConfig displayConfig)
    {
        x = SaveWindowPosition.DisplayConfigFields.x.get(displayConfig);
        y = SaveWindowPosition.DisplayConfigFields.y.get(displayConfig);
    }

    public static void FinishConfig(Lwjgl3ApplicationConfiguration config)
    {
        if (fullscreen) {
            Graphics.DisplayMode activeDisplayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
            Graphics.DisplayMode bestMode = null;
            for (Graphics.DisplayMode mode : Lwjgl3ApplicationConfiguration.getDisplayModes()) {
                if (mode.width == width && mode.height == height &&
                    (bestMode == null || bestMode.refreshRate < activeDisplayMode.refreshRate)) {
                    bestMode = mode;
                }
            }
            if (bestMode == null) {
                bestMode = activeDisplayMode;
            }
            config.setFullscreenMode(bestMode);
        } else {
            config.setDecorated(!borderless);
            config.setWindowedMode(width, height);
            config.setWindowPosition(x, y);
        }

        config.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public void focusLost()
            {
                if (Gdx.app != null && Gdx.app.getApplicationListener() != null) {
                    Gdx.app.getApplicationListener().pause();
                }
            }

            @Override
            public void focusGained()
            {
                if (Gdx.app != null && Gdx.app.getApplicationListener() != null) {
                    Gdx.app.getApplicationListener().resume();
                }
            }
        });
    }

    private static int x = -1;
    private static int y = -1;
    private static int width = 1910;
    private static int height = 1080;
    private static boolean fullscreen = false;
    private static boolean borderless = false;

    public static void no(Object obj, LwjglGraphics.SetDisplayModeCallback param) {}
    public static int getWidth(Object obj)
    {
        return width;
    }
    public static void setWidth(Object obj, int param)
    {
        width = param;
    }
    public static int getHeight(Object obj)
    {
        return height;
    }
    public static void setHeight(Object obj, int param)
    {
        height = param;
    }
    public static int getForegroundFPS(Object obj)
    {
        return FramerateLimiter.fps;
    }
    public static void setForegroundFPS(Object obj, int param)
    {
        FramerateLimiter.fps = param;
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
    public static void setFullscreen(Object obj, boolean param)
    {
        fullscreen = param;
    }
    public static boolean getFullscreen(Object obj)
    {
        return fullscreen;
    }
    public static void setVSync(Object obj, boolean param)
    {
        ((Lwjgl3ApplicationConfiguration) obj).useVsync(param);
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
    public static String setProperty(Lwjgl3ApplicationConfiguration config, String key, String value)
    {
        if ("org.lwjgl.opengl.Window.undecorated".equals(key)) {
            borderless = Boolean.parseBoolean(value);
            return null;
        }
        return System.setProperty(key, value);
    }
}

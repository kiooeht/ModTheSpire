package com.evacipated.cardcrawl.modthespire.patches.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.megacrit.cardcrawl.core.DisplayConfig;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowPosCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SaveWindowPosition
{
    private static final String FILENAME = "info.moddisplayconfig";

    @SpirePatch(
        clz = DisplayConfig.class,
        method = SpirePatch.CLASS
    )
    public static class DisplayConfigFields
    {
        public static SpireField<Integer> x = new SpireField<>(() -> -1);
        public static SpireField<Integer> y = new SpireField<>(() -> -1);
    }

    @SpirePatch2(
        clz = DisplayConfig.class,
        method = "readConfig"
    )
    public static class ReadConfig
    {
        public static void Postfix(DisplayConfig __result, Logger ___logger)
        {
            List<String> configLines = new ArrayList<>();
            try (Scanner s = new Scanner(new File(FILENAME))) {
                while (s.hasNextLine()) {
                    configLines.add(s.nextLine());
                }

                DisplayConfigFields.x.set(__result, Integer.parseInt(configLines.get(0).trim()));
                DisplayConfigFields.y.set(__result, Integer.parseInt(configLines.get(1).trim()));
            } catch (FileNotFoundException ignored) {
                ___logger.info("File " + FILENAME + " not found.");
            } catch (Exception ignored) {
                ___logger.info("Failed to parse " + FILENAME + " using defaults.");
                DisplayConfigFields.x.set(__result, -1);
                DisplayConfigFields.y.set(__result, -1);
            }
        }
    }

    @SpirePatch2(
        clz = Lwjgl3Window.class,
        method = SpirePatch.CONSTRUCTOR
    )
    public static class WindowMoveCallback
    {
        private static GLFWWindowPosCallback windowPosCallback;

        private static class MyWindowPosCallback extends GLFWWindowPosCallback
        {
            private final Lwjgl3Window window;
            private MyRunnable runnable = null;

            private MyWindowPosCallback(Lwjgl3Window window)
            {
                this.window = window;
            }

            @Override
            public void invoke(long windowHandle, int x, int y)
            {
                synchronized (this) {
                    if (runnable == null) {
                        runnable = new MyRunnable(x, y);
                        window.postRunnable(runnable);
                    } else {
                        runnable.setXY(x, y);
                    }
                }
            }

            private class MyRunnable implements Runnable
            {
                private int x, y;

                public MyRunnable(int x, int y)
                {
                    setXY(x, y);
                }

                public void setXY(int x, int y)
                {
                    this.x = x;
                    this.y = y;
                }

                @Override
                public void run()
                {
                    synchronized (MyWindowPosCallback.this) {
                        MyWindowPosCallback.this.runnable = null;
                    }
                    System.out.printf("%d, %d%n", x, y);
                    PrintWriter writer = null;
                    try {
                        writer = new PrintWriter(FILENAME, "UTF-8");
                        writer.println(x);
                        writer.println(y);
                    } catch (FileNotFoundException | UnsupportedEncodingException e) {
                        Logger logger = null;
                        try {
                            Field f = DisplayConfig.class.getDeclaredField("logger");
                            f.setAccessible(true);
                            logger = (Logger) f.get(null);
                        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
                        if (logger != null) {
                            logger.error("Exception caught", e);
                        }
                    } finally {
                        if (writer != null) {
                            writer.close();
                        }
                    }
                }
            }
        }

        public static void Postfix(Lwjgl3Window __instance, long windowHandle)
        {
            windowPosCallback = new MyWindowPosCallback(__instance);
            GLFW.glfwSetWindowPosCallback(windowHandle, windowPosCallback);
        }
    }

    @SpirePatch2(
        clz = Lwjgl3Window.class,
        method = "dispose"
    )
    public static class WindowMoveCallbackDispose
    {
        private static void Prefix(long ___windowHandle)
        {
            GLFW.glfwSetWindowPosCallback(___windowHandle, null);
            WindowMoveCallback.windowPosCallback = null;
        }
    }
}

package com.evacipated.cardcrawl.modthespire.patches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglGraphics;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;

import java.lang.reflect.Field;

@SpirePatch(
    clz=LwjglApplication.class,
    method="mainLoop"
)
public class DisableGdxForceExit
{
    @SpireInsertPatch(loc=248)
    public static void Insert(LwjglApplication __instance)
    {
        try {
            Field f = LwjglGraphics.class.getDeclaredField("config");
            f.setAccessible(true);
            LwjglApplicationConfiguration config = (LwjglApplicationConfiguration) f.get(Gdx.app.getGraphics());
            config.forceExit = false;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Game closed.");
            if (!Loader.DEBUG) {
                Loader.closeWindow();
            }
        }
    }
}

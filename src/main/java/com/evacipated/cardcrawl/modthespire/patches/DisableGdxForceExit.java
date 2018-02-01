package com.evacipated.cardcrawl.modthespire.patches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglGraphics;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;

import java.lang.reflect.Field;

@SpirePatch(
    cls="com.badlogic.gdx.backends.lwjgl.LwjglApplication",
    method="mainLoop"
)
public class DisableGdxForceExit
{
    @SpireInsertPatch(loc=248)
    public static void Insert(Object __obj_instance)
    {
        try {
            Field f = LwjglGraphics.class.getDeclaredField("config");
            f.setAccessible(true);
            LwjglApplicationConfiguration config = (LwjglApplicationConfiguration) f.get(Gdx.app.getGraphics());
            config.forceExit = false;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

package com.evacipated.cardcrawl.modthespire.patches.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.patches.HandleCrash;

@SpirePatch2(
    clz = Lwjgl3Application.class,
    method = "cleanup"
)
public class CloseMTSWindow
{
    public static void Postfix()
    {
        HandleCrash.maybeExit();
    }
}

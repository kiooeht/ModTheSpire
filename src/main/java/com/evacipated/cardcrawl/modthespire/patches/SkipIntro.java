package com.evacipated.cardcrawl.modthespire.patches;

import com.evacipated.cardcrawl.modthespire.ModTheSpire;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.screens.splash.SplashScreen;

@SpirePatch(
    clz=SplashScreen.class,
    method="update"
)
public class SkipIntro
{
    public static SpireReturn<Void> Prefix(SplashScreen __instance)
    {
        if (ModTheSpire.SKIP_INTRO) {
            __instance.isDone = true;
            return SpireReturn.Return();
        }
        return SpireReturn.Continue();
    }
}

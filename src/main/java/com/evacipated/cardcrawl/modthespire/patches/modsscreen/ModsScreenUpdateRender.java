package com.evacipated.cardcrawl.modthespire.patches.modsscreen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;

public class ModsScreenUpdateRender
{
    @SpirePatch(
        clz=MainMenuScreen.class,
        method="update"
    )
    public static class Update
    {
        public static void Postfix(MainMenuScreen __instance)
        {
            if (__instance.screen == ModsScreen.Enum.MODS_LIST) {
                ModMenuButton.modsScreen.update();
            }
        }
    }

    @SpirePatch(
        clz=MainMenuScreen.class,
        method="render"
    )
    public static class Render
    {
        public static void Postfix(MainMenuScreen __instance, SpriteBatch sb)
        {
            if (__instance.screen == ModsScreen.Enum.MODS_LIST) {
                ModMenuButton.modsScreen.render(sb);
            }
        }
    }
}

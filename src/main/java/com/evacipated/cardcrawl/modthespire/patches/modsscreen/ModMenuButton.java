package com.evacipated.cardcrawl.modthespire.patches.modsscreen;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.megacrit.cardcrawl.screens.mainMenu.MenuButton;

import java.lang.reflect.Field;

public class ModMenuButton
{
    @SpireEnum
    static MenuButton.ClickResult MODS;

    static ModsScreen modsScreen = null;

    @SpirePatch(
        clz=MenuButton.class,
        method="setLabel"
    )
    public static class SetLabel
    {
        public static void Postfix(MenuButton __instance)
        {
            try {
                if (__instance.result == MODS) {
                    Field f_label = MenuButton.class.getDeclaredField("label");
                    f_label.setAccessible(true);
                    f_label.set(__instance, "Mods");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SpirePatch(
        clz=MenuButton.class,
        method="buttonEffect"
    )
    public static class ButtonEffect
    {
        public static void Postfix(MenuButton __instance)
        {
            if (__instance.result == MODS) {
                if (modsScreen == null) {
                     modsScreen = new ModsScreen();
                }
                modsScreen.open();
            }
        }
    }
}

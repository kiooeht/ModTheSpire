package com.evacipated.cardcrawl.modthespire.patches.modsscreen;

import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MenuButton;

import java.util.Arrays;

@SpirePatch(
    cls="com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen",
    method="setMainMenuButtons"
)
public class MainMenuItem
{
    @SpireInsertPatch(
        rloc=4,
        localvars={"index"}
    )
    public static void Insert(Object __obj_instance, @ByRef int[] index)
    {
        MainMenuScreen __instance = (MainMenuScreen)__obj_instance;
        __instance.buttons.add(new MenuButton(ModMenuButton.MODS, index[0]++));
    }
}

package com.evacipated.cardcrawl.modthespire.patches.modsscreen;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.megacrit.cardcrawl.screens.mainMenu.MenuButton;

import java.lang.reflect.Field;

@SpirePatch(
    cls="com.megacrit.cardcrawl.screens.mainMenu.MenuButton",
    method="setLabel"
)
public class ModMenuButton
{
    @SpireEnum
    public static MenuButton.ClickResult MODS;

    public static void Postfix(Object __obj_instance)
    {
        try {
            if (((MenuButton)__obj_instance).result == MODS) {
                Field f_label = MenuButton.class.getDeclaredField("label");
                f_label.setAccessible(true);
                f_label.set(__obj_instance, "Mods");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

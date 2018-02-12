package com.evacipated.cardcrawl.modthespire.patches.modsscreen;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MenuCancelButton;
import com.megacrit.cardcrawl.screens.mainMenu.PatchNotesScreen;

public class ModsScreen
{
    private static final float START_Y = Settings.HEIGHT - 300.0F * Settings.scale;
    private float scrollY = START_Y;
    private float targetY = scrollY;
    public MenuCancelButton button = new MenuCancelButton();

    public void open()
    {
        button.show(PatchNotesScreen.TEXT[0]);
        targetY = Settings.HEIGHT - 100.0F * Settings.scale;
        scrollY = Settings.HEIGHT - 400.0F * Settings.scale;
        CardCrawlGame.mainMenuScreen.darken();
    }
}

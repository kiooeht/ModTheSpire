package com.evacipated.cardcrawl.modthespire.patches.modsscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.InputHelper;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MenuCancelButton;
import com.megacrit.cardcrawl.screens.mainMenu.PatchNotesScreen;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;

public class ModsScreen
{
    @SpireEnum
    public static MainMenuScreen.CurScreen MODS_LIST;

    private static final float START_Y = Settings.HEIGHT - 200.0F * Settings.scale;
    private float scrollY = START_Y;
    private float targetY = scrollY;
    private float scrollLowerBound;
    private float scrollUpperBound;
    private MenuCancelButton button = new MenuCancelButton();
    private boolean grabbedScreen = false;
    private float grabStartY = 0;

    static Map<URL, Object> baseModBadges;
    private static Field ModBadge_x;
    private static Field ModBadge_y;
    private static Method ModBadge_render;

    static
    {
        try {
            Class<?> ModBadge = ModsScreen.class.getClassLoader().loadClass("basemod.ModBadge");
            ModBadge_x = ModBadge.getDeclaredField("x");
            ModBadge_x.setAccessible(true);
            ModBadge_y = ModBadge.getDeclaredField("y");
            ModBadge_y.setAccessible(true);
            ModBadge_render = ModBadge.getDeclaredMethod("receiveRender", SpriteBatch.class);
        } catch (ClassNotFoundException e) {
            // NOP
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void open()
    {
        button.show(PatchNotesScreen.TEXT[0]);
        targetY = Settings.HEIGHT - 150.0F * Settings.scale;
        scrollY = Settings.HEIGHT - 400.0F * Settings.scale;
        CardCrawlGame.mainMenuScreen.darken();
        CardCrawlGame.mainMenuScreen.screen = MODS_LIST;

        scrollUpperBound = targetY;
        scrollLowerBound = Settings.HEIGHT - 600.0F * Settings.scale;
    }

    public void update()
    {
        button.update();
        if (button.hb.clicked || InputHelper.pressedEscape) {
            InputHelper.pressedEscape = false;
            CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.MAIN_MENU;
            button.hide();
            CardCrawlGame.mainMenuScreen.lighten();
        }
        updateScrolling();
        InputHelper.justClickedLeft = false;
    }

    private void updateScrolling()
    {
        int y = InputHelper.mY;
        if (!grabbedScreen) {
            if (InputHelper.scrolledDown) {
                targetY += Settings.SCROLL_SPEED;
            } else if (InputHelper.scrolledUp) {
                targetY -= Settings.SCROLL_SPEED;
            }
            if (InputHelper.justClickedLeft) {
                grabbedScreen = true;
                grabStartY = y - targetY;
            }
        } else if (InputHelper.isMouseDown) {
            targetY = y - grabStartY;
        } else {
            grabbedScreen = false;
        }
        scrollY = MathHelper.scrollSnapLerpSpeed(scrollY, targetY);
        resetScrolling();
    }

    private void resetScrolling()
    {
        if (targetY < scrollLowerBound) {
            targetY = MathHelper.scrollSnapLerpSpeed(targetY, scrollLowerBound);
        } else if (targetY > scrollUpperBound) {
            targetY = MathHelper.scrollSnapLerpSpeed(targetY, scrollUpperBound);
        }
    }

    public void render(SpriteBatch sb)
    {
        FontHelper.renderFontCentered(sb, FontHelper.SCP_cardTitleFont_small, "Mod List",
            Settings.WIDTH / 2.0f,
            Settings.HEIGHT - 70.0f * Settings.scale,
            Settings.GOLD_COLOR);

        renderModList(sb);

        button.render(sb);
    }

    private void renderModList(SpriteBatch sb)
    {
        OrthographicCamera camera = null;
        try {
            Field f = CardCrawlGame.class.getDeclaredField("camera");
            f.setAccessible(true);
            camera = (OrthographicCamera)f.get(Gdx.app.getApplicationListener());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (camera != null) {
            sb.flush();
            Rectangle scissors = new Rectangle();
            float y = Settings.HEIGHT - 110 * Settings.scale;
            Rectangle clipBounds = new Rectangle(50 * Settings.scale, y,
                500 * Settings.scale, button.hb.y - y + button.hb.height);
            ScissorStack.calculateScissors(camera, sb.getTransformMatrix(), clipBounds, scissors);
            ScissorStack.pushScissors(scissors);
        }

        sb.setColor(new Color(0, 0, 0, 0.8f));
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0.0F, 0.0F, Settings.WIDTH, Settings.HEIGHT);
        sb.setColor(Color.WHITE);

        float tmpY = 0;
        for (int i=0; i<Loader.MODONLYURLS.length; ++i) {
            final URL modURL = Loader.MODONLYURLS[i];
            FontHelper.renderFontLeftTopAligned(sb, FontHelper.buttonLabelFont, Loader.MODINFOS[i].Name,
                90.0f * Settings.scale,
                tmpY + scrollY,
                Settings.CREAM_COLOR);

            if (baseModBadges != null) {
                for (Map.Entry<URL, Object> entry : baseModBadges.entrySet()) {
                    try {
                        if (entry.getKey().equals(modURL)) {
                            Object modBadge = entry.getValue();
                            ModBadge_x.set(modBadge, 55.0f * Settings.scale);
                            ModBadge_y.set(modBadge, tmpY + scrollY - 27.0f * Settings.scale);
                            MainMenuScreen.CurScreen tmpScreen = CardCrawlGame.mainMenuScreen.screen;
                            CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.MAIN_MENU;
                            ModBadge_render.invoke(modBadge, sb);
                            CardCrawlGame.mainMenuScreen.screen = tmpScreen;
                            break;
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }

            tmpY -= 45.0f * Settings.scale;
        }

        if (camera != null) {
            sb.flush();
            ScissorStack.popScissors();
        }
    }
}

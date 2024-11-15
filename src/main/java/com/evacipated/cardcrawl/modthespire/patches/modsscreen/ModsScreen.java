package com.evacipated.cardcrawl.modthespire.patches.modsscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.ModTheSpire;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.evacipated.cardcrawl.modthespire.patches.MTSLocalization;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MenuCancelButton;
import com.megacrit.cardcrawl.screens.mainMenu.PatchNotesScreen;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class ModsScreen
{
    public static class Enum
    {
        @SpireEnum
        public static MainMenuScreen.CurScreen MODS_LIST;
    }

    private static final float START_Y = Settings.HEIGHT - 200.0F * Settings.scale;
    private static final float SCROLLBAR_WIDTH = 15f * Settings.scale;
    private float scrollY = START_Y;
    private float targetY = scrollY;
    private float scrollLowerBound;
    private float scrollUpperBound;
    private MenuCancelButton closeButton = new MenuCancelButton();
    private ModConfigButton configButton = new ModConfigButton();
    private boolean grabbedScreen = false;
    private float grabStartY = 0;

    private ArrayList<Hitbox> hitboxes = new ArrayList<>();
    private int selectedMod = -1;

    static Map<URL, Object> baseModBadges;
    private static boolean justClosedModPanel = false;
    private static Field ModBadge_x;
    private static Field ModBadge_y;
    private static Field ModBadge_modPanel;
    private static Method ModBadge_render;
    private static Method ModBadge_onClick;
    private static Field ModPanel_isUp;
    private static Method ModPanel_update;
    private static Field BaseMod_modSettingsUp;

    static
    {
        try {
            Class<?> ModBadge = ModsScreen.class.getClassLoader().loadClass("basemod.ModBadge");
            ModBadge_x = ModBadge.getDeclaredField("x");
            ModBadge_x.setAccessible(true);
            ModBadge_y = ModBadge.getDeclaredField("y");
            ModBadge_y.setAccessible(true);
            ModBadge_modPanel = ModBadge.getDeclaredField("modPanel");
            ModBadge_modPanel.setAccessible(true);
            ModBadge_render = ModBadge.getDeclaredMethod("receiveRender", SpriteBatch.class);
            ModBadge_render.setAccessible(true);
            ModBadge_onClick = ModBadge.getDeclaredMethod("onClick");
            ModBadge_onClick.setAccessible(true);

            Class<?> ModPanel = ModsScreen.class.getClassLoader().loadClass("basemod.ModPanel");
            ModPanel_isUp = ModPanel.getDeclaredField("isUp");
            ModPanel_isUp.setAccessible(true);
            ModPanel_update = ModPanel.getDeclaredMethod("update");
            ModPanel_update.setAccessible(true);

            BaseMod_modSettingsUp = ModsScreen.class.getClassLoader().loadClass("basemod.BaseMod").getDeclaredField("modSettingsUp");
        } catch (ClassNotFoundException e) {
            // NOP
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public ModsScreen()
    {
        for (int i = 0; i< ModTheSpire.MODINFOS.length; ++i) {
            hitboxes.add(new Hitbox(430.0f * Settings.scale, 40.0f * Settings.scale));
        }
    }

    public void open()
    {
        closeButton.show(PatchNotesScreen.TEXT[0]);
        scrollY = targetY = Settings.HEIGHT - 150.0F * Settings.scale;
        CardCrawlGame.mainMenuScreen.darken();
        CardCrawlGame.mainMenuScreen.screen = Enum.MODS_LIST;

        selectedMod = -1;

        scrollUpperBound = targetY + Math.max(0, ModTheSpire.MODINFOS.length - 13) * 45.0f * Settings.scale;
        scrollLowerBound = targetY;
    }

    public void update()
    {
        boolean baseModSettingsUp = false;
        if (BaseMod_modSettingsUp != null) {
            try {
                baseModSettingsUp = (boolean) BaseMod_modSettingsUp.get(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        closeButton.update();
        if (closeButton.hb.clicked || InputHelper.pressedEscape) {
            closeButton.hb.clicked = false;
            InputHelper.pressedEscape = false;
            try {
                if (!baseModSettingsUp) {
                    CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.MAIN_MENU;
                    closeButton.hide();
                    configButton.hideInstantly();
                    CardCrawlGame.mainMenuScreen.lighten();
                } else {
                    BaseMod_modSettingsUp.set(null, false);
                    justClosedModPanel = true;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        configButton.update();
        if (configButton.hb.clicked) {
            configButton.hb.clicked = false;
            modBadge_onClick(baseModBadges.get(ModTheSpire.MODINFOS[selectedMod].jarURL));
        }

        if (baseModBadges != null) {
            for (Map.Entry<URL, Object> entry : baseModBadges.entrySet()) {
                modPanel_update(entry.getValue());
            }
            if (justClosedModPanel) {
                justClosedModPanel = false;
                CardCrawlGame.mainMenuScreen.darken();
                CardCrawlGame.mainMenuScreen.screen = Enum.MODS_LIST;
                closeButton.show(PatchNotesScreen.TEXT[0]);
                if (baseModBadges != null && baseModBadgeExistsAndModPanelIsNotNull(baseModBadges.get(ModTheSpire.MODINFOS[selectedMod].jarURL))) {
                    showConfigButton();
                }
            }
        }

        if (!baseModSettingsUp) {
            updateScrolling();
            float tmpY = 0;
            Rectangle bounds = getModListBounds();
            Vector2 mouse = new Vector2(InputHelper.mX, InputHelper.mY);
            for (int i = 0; i < hitboxes.size(); ++i) {
                hitboxes.get(i).x = 90.0f * Settings.scale;
                hitboxes.get(i).y = tmpY + scrollY - 30.0f * Settings.scale;
                if (bounds.contains(mouse) && bounds.overlaps(hitboxToRect(hitboxes.get(i)))) {
                    hitboxes.get(i).update();
                } else {
                    hitboxes.get(i).hovered = false;
                }
                if (hitboxes.get(i).hovered && InputHelper.isMouseDown) {
                    hitboxes.get(i).clickStarted = true;
                }
                tmpY -= 45.0f * Settings.scale;

                if (hitboxes.get(i).clicked) {
                    hitboxes.get(i).clicked = false;
                    selectedMod = i;
                    if (baseModBadges != null && baseModBadgeExistsAndModPanelIsNotNull(baseModBadges.get(ModTheSpire.MODINFOS[selectedMod].jarURL))) {
                        if (configButton.isHidden) {
                            showConfigButton();
                        }
                    } else {
                        configButton.hide();
                    }
                }
            }
        }

        InputHelper.justClickedLeft = false;
    }

    private static Rectangle hitboxToRect(Hitbox hb)
    {
        return new Rectangle(hb.x, hb.y, hb.width, hb.height);
    }

    private void updateScrolling()
    {
        if (hitboxes.size() > 13) {
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
        FontHelper.renderFontCentered(sb, FontHelper.SCP_cardTitleFont_small, MTSLocalization.getString("ModList"),
            Settings.WIDTH / 2.0f,
            Settings.HEIGHT - 70.0f * Settings.scale,
            Settings.GOLD_COLOR);

        renderModInfo(sb);

        renderModList(sb);

        try {
            if (baseModBadges != null && (boolean)BaseMod_modSettingsUp.get(null)) {
                for (Map.Entry<URL, Object> entry : baseModBadges.entrySet()) {
                    try {
                        MainMenuScreen.CurScreen tmpScreen = CardCrawlGame.mainMenuScreen.screen;
                        CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.MAIN_MENU;
                        ModBadge_render.invoke(entry.getValue(), sb);
                        CardCrawlGame.mainMenuScreen.screen = tmpScreen;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        for (Hitbox hitbox : hitboxes) {
            hitbox.render(sb);
        }

        configButton.render(sb);
        closeButton.render(sb);
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
            Rectangle clipBounds = getModListBounds();
            ScissorStack.calculateScissors(camera, sb.getTransformMatrix(), clipBounds, scissors);
            ScissorStack.pushScissors(scissors);
        }

        sb.setColor(new Color(0, 0, 0, 0.8f));
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0.0F, 0.0F, Settings.WIDTH, Settings.HEIGHT);
        sb.setColor(Color.WHITE);

        float tmpY = 0;
        for (int i = 0; i< ModTheSpire.MODINFOS.length; ++i) {
            if (hitboxes.get(i).hovered) {
                Color c = sb.getColor();
                sb.setColor(1, 1, 1, (hitboxes.get(i).clickStarted ? 0.8f : 0.4f));
                sb.draw(ImageMaster.WHITE_SQUARE_IMG, hitboxes.get(i).x, hitboxes.get(i).y, hitboxes.get(i).width, hitboxes.get(i).height);
                sb.setColor(c);
            }

            final URL modURL = ModTheSpire.MODINFOS[i].jarURL;
            FontHelper.renderFontLeftTopAligned(sb, FontHelper.buttonLabelFont, ModTheSpire.MODINFOS[i].Name,
                95.0f * Settings.scale,
                tmpY + scrollY,
                Settings.CREAM_COLOR);
            if (i == selectedMod) {
                drawRect(sb,
                    hitboxes.get(i).x,
                    hitboxes.get(i).y,
                    hitboxes.get(i).width,
                    hitboxes.get(i).height,
                    2);
            }

            // Render BaseMod ModBadges
            if (baseModBadges != null) {
                for (Map.Entry<URL, Object> entry : baseModBadges.entrySet()) {
                    try {
                        if (entry.getKey().equals(modURL)) {
                            Object modBadge = entry.getValue();
                            ModBadge_x.set(modBadge, 55.0f * Settings.scale);
                            ModBadge_y.set(modBadge, tmpY + scrollY - 27.0f * Settings.scale);

                            boolean tmpModSettingsUp = (boolean)BaseMod_modSettingsUp.get(null);
                            BaseMod_modSettingsUp.set(null, false);
                            MainMenuScreen.CurScreen tmpScreen = CardCrawlGame.mainMenuScreen.screen;
                            CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.MAIN_MENU;
                            ModBadge_render.invoke(modBadge, sb);
                            CardCrawlGame.mainMenuScreen.screen = tmpScreen;
                            BaseMod_modSettingsUp.set(null, tmpModSettingsUp);
                            break;
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }

            tmpY -= 45.0f * Settings.scale;
        }

        // Render scrollbar
        if (hitboxes.size() > 13) {
            Rectangle bounds = getModListBounds();
            float scrollbarHeight = bounds.height / ((45f * Settings.scale * hitboxes.size()) / bounds.height);
            float scrollbarOffset = ((scrollY - scrollLowerBound) / (scrollUpperBound - scrollLowerBound)) * (bounds.height - scrollbarHeight);
            Color c = sb.getColor();
            sb.setColor(1, 1, 1, 0.5f);
            sb.draw(
                ImageMaster.WHITE_SQUARE_IMG,
                bounds.x + bounds.width - SCROLLBAR_WIDTH,
                bounds.y + bounds.height - scrollbarHeight - scrollbarOffset,
                SCROLLBAR_WIDTH,
                scrollbarHeight
            );
            sb.setColor(c);
        }

        if (camera != null) {
            sb.flush();
            ScissorStack.popScissors();
        }
    }

    private Rectangle getModListBounds()
    {
        float y = Settings.HEIGHT - 110 * Settings.scale;
        Rectangle ret = new Rectangle(
            50 * Settings.scale,
            y,
            500 * Settings.scale,
            closeButton.hb.y - y + (closeButton.hb.height * 2)
        );
        if (ret.height < 0) {
            ret.y += ret.height;
            ret.height *= -1;
        }
        return ret;
    }

    private void renderModInfo(SpriteBatch sb)
    {
        // Draw bg rectangle
        sb.setColor(new Color(0, 0, 0, 0.8f));
        float screenPadding = 50 * Settings.scale;
        float x = 600 * Settings.scale;
        float y = 110 * Settings.scale;
        sb.draw(ImageMaster.WHITE_SQUARE_IMG,
            x, screenPadding,
            Settings.WIDTH - x - screenPadding, Settings.HEIGHT - y - screenPadding);
        sb.setColor(Color.WHITE);

        float padding = 20 * Settings.scale;
        if (selectedMod >= 0) {
            ModInfo info = ModTheSpire.MODINFOS[selectedMod];
            String text = info.Name;
            text += " NL Version: " + (info.ModVersion != null ? info.ModVersion : "<MISSING>");
            text += " NL Mod ID: " + (info.ID != null ? info.ID : "<MISSING>");
            text += " NL Author" + (info.Authors.length > 1 ? "s" : "") + ": " + StringUtils.join(info.Authors, ", ");
            if (info.Credits != null && !info.Credits.isEmpty()) {
                text += " NL Credits: " + newlineToNL(info.Credits);
            }
            text += " NL NL " + newlineToNL(info.Description);

            FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont,
                text,
                x + padding, Settings.HEIGHT - y - padding,
                Settings.WIDTH - x - screenPadding,
                26 * Settings.scale,
                Settings.CREAM_COLOR);
        }
    }

    private String newlineToNL(String str)
    {
        return str.replace(" \n ", " NL ").replace("\n ", " NL ").replace("\n", " NL ");
    }

    private void drawRect(SpriteBatch sb, float x, float y, float width, float height, float thickness)
    {
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x, y, width, thickness);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x, y, thickness, height);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x, y+height-thickness, width, thickness);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, x+width-thickness, y, thickness, height);
    }

    private void showConfigButton()
    {
        float closeGlowAlpha = 0f;
        try {
            // Ensure buttons flash in-sync
            Field f = MenuCancelButton.class.getDeclaredField("glowAlpha");
            f.setAccessible(true);
            closeGlowAlpha = f.getFloat(closeButton);
        } catch (NoSuchFieldException | IllegalAccessException ignore) {}
        configButton.show(MTSLocalization.getString("ModConfig"), closeGlowAlpha);
    }

    private void modPanel_update(Object badge)
    {
        if (badge != null) {
            try {
                Object modPanel = ModBadge_modPanel.get(badge);
                if (modPanel != null && (boolean) ModPanel_isUp.get(modPanel)) {
                    ModPanel_update.invoke(modPanel);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void modBadge_onClick(Object badge)
    {
        if (badge != null) {
            try {
                configButton.hide();
                closeButton.show(MTSLocalization.getString("Close"));
                ModBadge_onClick.invoke(badge);
                CardCrawlGame.mainMenuScreen.screen = Enum.MODS_LIST;
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean baseModBadgeExistsAndModPanelIsNotNull(Object badge) {
        if (badge != null) {
            try {
                Object modPanel = ModBadge_modPanel.get(badge);
                if (modPanel != null) {
                    return true;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}

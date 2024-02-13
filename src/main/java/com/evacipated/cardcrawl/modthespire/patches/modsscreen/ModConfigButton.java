package com.evacipated.cardcrawl.modthespire.patches.modsscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.ModTheSpire;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

class ModConfigButton
{
    private static final Texture CONFIG_BUTTON;
    private static final int W = 512;
    private static final int H = 256;
    private static final Color HOVER_BLEND_COLOR = new Color(1.0F, 1.0F, 1.0F, 0.4F);
    private static final float SHOW_X = 256.0F * Settings.scale;
    private static final float DRAW_Y = 228.0F * Settings.scale;
    public static final float HIDE_X = SHOW_X - 400.0F * Settings.scale;

    public float current_x = HIDE_X;
    private float target_x = current_x;
    public boolean isHidden = true;
    private float glowAlpha = 0.0F;
    private Color glowColor = Settings.GOLD_COLOR.cpy();
    private String buttonText = "NOT_SET";

    private static final float TEXT_OFFSET_X = -136.0F * Settings.scale;
    private static final float TEXT_OFFSET_Y = 57.0F * Settings.scale;

    public Hitbox hb = new Hitbox(300.0F * Settings.scale, 100.0F * Settings.scale);

    public ModConfigButton() {
        hb.move(SHOW_X - 106.0F * Settings.scale, DRAW_Y + 60.0F * Settings.scale);
    }

    public void update() {
        if (!isHidden) {
            updateGlow();
            hb.update();
            if (InputHelper.justClickedLeft && hb.hovered) {
                hb.clickStarted = true;
                CardCrawlGame.sound.play("UI_CLICK_1");
            }
            if (hb.justHovered)
                CardCrawlGame.sound.play("UI_HOVER");
            if (CInputActionSet.cancel.isJustPressed())
                hb.clicked = true;
        }
        if (current_x != target_x) {
            current_x = MathUtils.lerp(current_x, target_x, Gdx.graphics.getDeltaTime() * 9.0F);
            if (Math.abs(current_x - target_x) < Settings.UI_SNAP_THRESHOLD)
                current_x = target_x;
        }
    }

    private void updateGlow() {
        glowAlpha += Gdx.graphics.getDeltaTime() * 3.0F;
        if (glowAlpha < 0.0F)
            glowAlpha *= -1.0F;
        float tmp = MathUtils.cos(glowAlpha);
        if (tmp < 0.0F) {
            glowColor.a = -tmp / 2.0F + 0.3F;
        } else {
            glowColor.a = tmp / 2.0F + 0.3F;
        }
    }

    public boolean hovered() {
        return hb.hovered;
    }

    public void hide() {
        if (!isHidden) {
            hb.clicked = false;
            hb.hovered = false;
            InputHelper.justClickedLeft = false;
            target_x = HIDE_X;
            isHidden = true;
        }
    }

    public void hideInstantly() {
        if (!isHidden) {
            hb.hovered = false;
            InputHelper.justClickedLeft = false;
            target_x = HIDE_X;
            current_x = target_x;
            isHidden = true;
        }
    }

    public void show(String buttonText, float otherGlowAlpha) {
        if (isHidden) {
            glowAlpha = otherGlowAlpha;
            current_x = HIDE_X;
            target_x = SHOW_X;
            isHidden = false;
            this.buttonText = buttonText;
        } else {
            current_x = HIDE_X;
            this.buttonText = buttonText;
        }
        hb.hovered = false;
    }

    public void showInstantly(String buttonText) {
        current_x = SHOW_X;
        target_x = SHOW_X;
        isHidden = false;
        this.buttonText = buttonText;
        hb.hovered = false;
    }

    public void render(SpriteBatch sb) {
        renderShadow(sb);
        renderOutline(sb);
        renderButton(sb);
        if (hb.hovered && !hb.clickStarted) {
            sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            sb.setColor(HOVER_BLEND_COLOR);
            renderButton(sb);
            sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        Color tmpColor = Settings.LIGHT_YELLOW_COLOR;
        if (hb.clickStarted)
            tmpColor = Color.LIGHT_GRAY;
        if (Settings.isControllerMode) {
            FontHelper.renderFontLeft(sb, FontHelper.buttonLabelFont, buttonText, current_x + TEXT_OFFSET_X - 30.0F * Settings.scale, DRAW_Y + TEXT_OFFSET_Y, tmpColor);
        } else {
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, buttonText, current_x + TEXT_OFFSET_X, DRAW_Y + TEXT_OFFSET_Y, tmpColor);
        }
        if (!isHidden) {
            hb.render(sb);
        }
    }

    private void renderShadow(SpriteBatch sb) {
        sb.setColor(Color.WHITE);
        sb.draw(ImageMaster.CONFIRM_BUTTON_SHADOW, current_x - 256.0F, DRAW_Y - 128.0F, 256.0F, 128.0F, 512.0F, 256.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, W, H, true, false);
    }

    private void renderOutline(SpriteBatch sb) {
        sb.setColor(glowColor);
        sb.draw(ImageMaster.CONFIRM_BUTTON_OUTLINE, current_x - 256.0F, DRAW_Y - 128.0F, 256.0F, 128.0F, 512.0F, 256.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, W, H, true, false);
    }

    private void renderButton(SpriteBatch sb) {
        sb.setColor(Color.WHITE);
        sb.draw(CONFIG_BUTTON, current_x - 256.0F, DRAW_Y - 128.0F, 256.0F, 128.0F, 512.0F, 256.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, W, H, false, false);
    }

    static {
        try (InputStream is = ModTheSpire.class.getResourceAsStream("/assets/sts/modConfigButton.png")) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[4096];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            byte[] bytes =  buffer.toByteArray();

            Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
            CONFIG_BUTTON = new Texture(pixmap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

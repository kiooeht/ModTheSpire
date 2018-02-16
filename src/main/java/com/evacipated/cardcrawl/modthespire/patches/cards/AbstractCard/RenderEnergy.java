package com.evacipated.cardcrawl.modthespire.patches.cards.AbstractCard;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.ImageMaster;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SpirePatch(
    cls="com.megacrit.cardcrawl.cards.AbstractCard",
    method="renderEnergy"
)
public class RenderEnergy
{
    @SpireInsertPatch(
        rloc=25,
        localvars={"drawX", "drawY"}
    )
    public static void Insert(Object __obj_instance, Object sb, float drawX, float drawY)
    {
        AbstractCard card = (AbstractCard)__obj_instance;
        switch (card.color) {
            case RED:
            case GREEN:
            case BLUE:
            case COLORLESS:
            case CURSE:
                return;
        }

        try {
            Field renderColor = AbstractCard.class.getDeclaredField("renderColor");
            renderColor.setAccessible(true);

            Method renderHelper = AbstractCard.class.getDeclaredMethod("renderHelper", SpriteBatch.class, Color.class, Texture.class, float.class, float.class);
            renderHelper.setAccessible(true);
            renderHelper.invoke(card, sb, renderColor.get(card), ImageMaster.CARD_COLORLESS_ORB, drawX, drawY);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}

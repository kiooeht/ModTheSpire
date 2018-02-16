package com.evacipated.cardcrawl.modthespire.patches.cards.AbstractCard;

import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.CardHelper;

import java.lang.reflect.Field;

@SpirePatch(
    cls="com.megacrit.cardcrawl.cards.AbstractCard",
    method="createCardImage"
)
public class CreateCardImage
{
    public static void Prefix(Object __obj_instance)
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
            // TODO: What is even the point of any of these fields?
            //       As far as I can tell, they are never used
            Field bgColor = AbstractCard.class.getDeclaredField("bgColor");
            bgColor.setAccessible(true);
            bgColor.set(card, CardHelper.getColor(19.0F, 45.0F, 40.0F));

            Field backColor = AbstractCard.class.getDeclaredField("backColor");
            backColor.setAccessible(true);
            backColor.set(card, CardHelper.getColor(32.0F, 91.0F, 43.0F));

            Field frameColor = AbstractCard.class.getDeclaredField("frameColor");
            frameColor.setAccessible(true);
            frameColor.set(card, CardHelper.getColor(52.0F, 123.0F, 8.0F));

            Field frameOutlineColor = AbstractCard.class.getDeclaredField("frameOutlineColor");
            frameOutlineColor.setAccessible(true);
            frameOutlineColor.set(card, new Color(1.0F, 0.75F, 0.43F, 1.0F));

            Field descBoxColor = AbstractCard.class.getDeclaredField("descBoxColor");
            descBoxColor.setAccessible(true);
            descBoxColor.set(card, CardHelper.getColor(53.0F, 58.0F, 64.0F));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

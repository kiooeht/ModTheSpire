package com.evacipated.cardcrawl.modthespire.patches.cards.AbstractCard;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

@SpirePatch(
    cls="com.megacrit.cardcrawl.cards.AbstractCard",
    method="renderAttackBg"
)
public class RenderAttackBg
{
    public static ExprEditor Instrument()
    {
        return new ExprEditor() {
            @Override
            public void edit(FieldAccess f) throws CannotCompileException
            {
                if (f.getClassName().equals("com.megacrit.cardcrawl.helpers.ImageMaster") && f.getFieldName().equals("CARD_SKILL_BG_BLACK")) {
                    f.replace("if (color != com.megacrit.cardcrawl.cards.AbstractCard.CardColor.CURSE) {" +
                        "$_ = com.megacrit.cardcrawl.helpers.ImageMaster.CARD_ATTACK_BG_BLUE;" +
                        "} else {" +
                        "$_ = $proceed();" +
                        "}");
                }
            }
        };
    }
}

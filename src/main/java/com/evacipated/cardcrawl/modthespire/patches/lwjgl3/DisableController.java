package com.evacipated.cardcrawl.modthespire.patches.lwjgl3;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.helpers.controller.CInputHelper;

// Disable controller support, crashes because of linkage error
// TODO look into fixing
@SpirePatch2(
    clz = CInputHelper.class,
    method = "initializeIfAble"
)
public class DisableController {
    public static SpireReturn<Void> Prefix() {
        return SpireReturn.Return();
    }
}

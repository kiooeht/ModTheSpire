package com.evacipated.cardcrawl.modthespire.patches;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.Patcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;

import java.io.IOException;

@SpirePatch(
    cls="com.megacrit.cardcrawl.core.CardCrawlGame",
    method="create"
)
public class CardCrawlGame_create
{
    @SpireInsertPatch(
        rloc=45
    )
    public static void Insert(Object __obj_instance)
    {
        ClassLoader loader = __obj_instance.getClass().getClassLoader();

        System.out.printf("Patching enums...");
        try {
            // Patch SpireEnums from core patches
            Patcher.patchEnums(loader, ClassLoader.getSystemResource(Loader.COREPATCHES_JAR));
            // Patch SpireEnums from mods
            Patcher.patchEnums(loader, Loader.MODONLYURLS);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Done.");
    }
}

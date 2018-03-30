package com.evacipated.cardcrawl.modthespire.patches.modsscreen.BaseMod;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;

import java.lang.reflect.Field;
import java.util.ArrayList;

@SpirePatch(
    cls="basemod.BaseMod",
    method="publishPostInitialize",
    optional=true
)
public class DisableBaseModBadges
{
    public static void Postfix()
    {
        try {
            Class<?> basemod = DisableBaseModBadges.class.getClassLoader().loadClass("basemod.BaseMod");
            Field f = basemod.getDeclaredField("renderSubscribers");
            f.setAccessible(true);
            ArrayList<?> renderSubscribers = (ArrayList<?>)f.get(null);
            renderSubscribers.removeIf(o -> o.getClass().getName().equals("basemod.ModBadge"));

            f = basemod.getDeclaredField("preUpdateSubscribers");
            f.setAccessible(true);
            ArrayList<?> preUpdateSubscribers = (ArrayList<?>)f.get(null);
            preUpdateSubscribers.removeIf(o -> o.getClass().getName().equals("basemod.ModBadge"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

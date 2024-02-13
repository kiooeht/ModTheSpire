package com.evacipated.cardcrawl.modthespire.patches;

import com.evacipated.cardcrawl.modthespire.ModTheSpire;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.core.Settings;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MTSLocalization
{
    private static final String MISSING = "<MISSING:%s:%s>";
    private static final Map<Settings.GameLanguage, Map<String, String>> strings;

    static {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<Settings.GameLanguage, Map<String, String>>>(){}.getType();

        InputStream is = ModTheSpire.class.getResourceAsStream("/mtsLocalization.json");
        if (is != null) {
            strings = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), type);
        } else {
            System.out.println("FAILED TO READ MTS LOCALIZATION");
            strings = null;
        }
        System.out.println();
    }

    public static String getString(String id)
    {
        return getString(Settings.language, id);
    }

    public static String getString(Settings.GameLanguage language, String id)
    {
        return getStringBackup(language, language, id);
    }

    private static String getStringBackup(Settings.GameLanguage language, Settings.GameLanguage originalLanguage, String id)
    {
        if (strings == null) {
            return String.format(MISSING, originalLanguage.name(), id);
        }

        Map<String, String> lang = strings.get(language);
        if (lang == null) {
            if (language == Settings.GameLanguage.ENG) {
                return String.format(MISSING, originalLanguage.name(), id);
            }
            return getStringBackup(Settings.GameLanguage.ENG, language, id);
        }

        String str = lang.get(id);
        if (str == null) {
            if (language == Settings.GameLanguage.ENG) {
                return String.format(MISSING, originalLanguage.name(), id);
            }
            return getStringBackup(Settings.GameLanguage.ENG, language, id);
        }
        return str;
    }
}

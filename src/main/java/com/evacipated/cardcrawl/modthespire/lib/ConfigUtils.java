package com.evacipated.cardcrawl.modthespire.lib;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;

public class ConfigUtils
{
    private static final String APP_NAME = "ModTheSpire";
    public static final String CONFIG_DIR;

    static {
        String basedir;
        if (SystemUtils.IS_OS_WINDOWS) {
            // %LOCALAPPDATA%/APP_NAME/
            // Fallback to %APPDATA%/APP_NAME/
            String appdata = System.getenv("LOCALAPPDATA");
            if (appdata == null || appdata.isEmpty()) {
                appdata = System.getenv("APPDATA");
            }
            basedir = appdata;
        } else if (SystemUtils.IS_OS_LINUX) {
            // /home/x/.config/APP_NAME/
            basedir = SystemUtils.USER_HOME + File.separator
                + ".config" + File.separator;
        } else if (SystemUtils.IS_OS_MAC) {
            // /Users/x/Library/Preferences/APP_NAME/
            basedir = SystemUtils.USER_HOME + File.separator
                + "Library" + File.separator
                + "Preferences" + File.separator;
        } else {
            // user.home/APP_NAME/
            basedir = SystemUtils.USER_HOME;
        }
        CONFIG_DIR = basedir + File.separator +
            APP_NAME;

        // Make config directory
        File directory = new File(CONFIG_DIR);
        directory.mkdirs();
    }
}

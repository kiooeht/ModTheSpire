package com.evacipated.cardcrawl.modthespire;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Loader extends JFrame {
    public static String MTS_VERSION = "1.1.2";
    private static String MOD_DIR = "mods/";
    private static String STS_JAR = "desktop-1.0.jar";

    private static Object ARGS;

    public static void main(String[] args) {
        ARGS = args;

        EventQueue.invokeLater(() -> {
           ModSelectWindow ex = new ModSelectWindow(getAllModFiles());
            ex.setVisible(true);
        });
    }

    // runMods - sets up the ClassLoader, sets the isModded flag and launches the game
    public static void runMods(File[] modJars) {
        try {
            // Construct ClassLoader
            URL[] modUrls = buildUrlArray(modJars);
            URLClassLoader loader = new URLClassLoader(modUrls, ClassLoader.getSystemClassLoader());

            // Set Settings.isModded = true
            Class<?> Settings = loader.loadClass("com.megacrit.cardcrawl.core.Settings");
            Field isModded = Settings.getDeclaredField("isModded");
            isModded.set(null, true);

            // Add ModTheSpire section to CardCrawlGame.VERSION_NUM
            Class<?> CardCrawlGame = loader.loadClass("com.megacrit.cardcrawl.core.CardCrawlGame");
            Field VERSION_NUM = CardCrawlGame.getDeclaredField("VERSION_NUM");
            String oldVersion = (String) VERSION_NUM.get(null);
            VERSION_NUM.set(null, oldVersion + " [ModTheSpire " + MTS_VERSION + "]");

            // Initialize any mods which declare an initialization function
            for (int i = 0; i < modUrls.length - 1; i++) {
                String modUrl = modUrls[i].toString();
                String modName = modUrl.substring(modUrl.lastIndexOf('/') + 1, modUrl.length() - 4);
                
                try {
                    Class<?> modMainClass = loader.loadClass(modName.toLowerCase() + "." + modName);
                    Method initialize = modMainClass.getDeclaredMethod("initialize");
                    initialize.invoke(null);
                } catch (ClassNotFoundException e) {
                    continue;
                } catch (NoSuchMethodException e) {
                    continue;
                }
            }

            // Launch the game
            Class<?> DesktopLauncher = loader.loadClass("com.megacrit.cardcrawl.desktop.DesktopLauncher");
            Method method = DesktopLauncher.getDeclaredMethod("main", String[].class);
            method.invoke(null, ARGS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // buildUrlArray - builds the URL array to pass to the ClassLoader
    private static URL[] buildUrlArray(File[] modJars) throws Exception {
        URL[] urls = new URL[modJars.length + 1];
        for (int i = 0; i < modJars.length; i++) {
            urls[i] = modJars[i].toURI().toURL();
        }

        urls[modJars.length] = new File(STS_JAR).toURI().toURL();
        return urls;
    }

    // getAllModFiles - returns a File array containing all of the JAR files in the mods directory
    private static File[] getAllModFiles() {
        File file = new File(MOD_DIR);
        if (!file.exists() || !file.isDirectory()) return null;

        File[] files = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jar");
            }
        });

        if (files.length > 0) return files;
        return null;
    }
}

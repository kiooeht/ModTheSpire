package com.evacipated.cardcrawl.modthespire;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class Loader {
    public static boolean DEBUG = false;

    public static Version MTS_VERSION = new Version("2.2.1");
    private static String MOD_DIR = "mods/";
    public static String STS_JAR = "desktop-1.0.jar";
    private static String STS_JAR2 = "SlayTheSpire.jar";
    public static String COREPATCHES_JAR = "corepatches.jar";
    public static ModInfo[] MODINFOS;
    public static URL[] MODONLYURLS;

    private static Object ARGS;

    public static void main(String[] args) {
        ARGS = args;
        if (Arrays.asList(args).contains("--debug")) {
            System.out.println("Debug mode!");
            DEBUG = true;
        }

        try {
            String thisJarName = new File(Loader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName();
            if (thisJarName.equals(STS_JAR)) {
                STS_JAR = STS_JAR2;
            }
        } catch (URISyntaxException e) {
            // NOP
        }

        EventQueue.invokeLater(() -> {
           ModSelectWindow ex = new ModSelectWindow(getAllModFiles());
            ex.setVisible(true);
        });
    }

    // runMods - sets up the ClassLoader, sets the isModded flag and launches the game
    public static void runMods(File[] modJars) {
        try {
            // Check that desktop-1.0.jar exists
            File tmp = new File(STS_JAR);
            if (!tmp.exists()) {
                JOptionPane.showMessageDialog(null, "Unable to find '" + STS_JAR + "'");
                return;
            }

            // Construct ClassLoader
            URL[] modUrls = buildUrlArray(modJars);
            MTSClassLoader loader = new MTSClassLoader(ClassLoader.getSystemResourceAsStream(COREPATCHES_JAR), modUrls, ClassLoader.getSystemClassLoader());

            if (modJars.length > 0) {
                ModInfo[] modInfos = buildInfoArray(modJars);
                MODINFOS = modInfos;

                // Remove the base game jar from the search path
                URL[] modOnlyUrls = new URL[modUrls.length - 1];
                System.arraycopy(modUrls, 0, modOnlyUrls, 0, modOnlyUrls.length);
                MODONLYURLS = modOnlyUrls;

                ClassPool pool = ClassPool.getDefault();
                pool.insertClassPath(new LoaderClassPath(loader));
                loader.addStreamToClassPool(pool); // Inserts infront of above path
                Set<CtClass> ctClasses = new HashSet<>();
                // Find and inject core patches
                System.out.println("Finding core patches...");
                ctClasses.addAll(Patcher.injectPatches(loader, pool, Patcher.findPatches(new URL[]{ClassLoader.getSystemResource(Loader.COREPATCHES_JAR)})));
                // Find and inject mod patches
                System.out.println("Finding patches...");
                ctClasses.addAll(Patcher.injectPatches(loader, pool, Patcher.findPatches(modOnlyUrls, MODINFOS)));
                Patcher.compilePatches(loader, ctClasses);

                System.out.printf("Patching enums...");
                Patcher.patchEnums(loader, ClassLoader.getSystemResource(Loader.COREPATCHES_JAR));
                // Patch SpireEnums from mods
                Patcher.patchEnums(loader, Loader.MODONLYURLS);
                System.out.println("Done.");
                
                // Set Settings.isModded = true
                System.out.printf("Setting isModded = true...");
                System.out.flush();
                Class<?> Settings = loader.loadClass("com.megacrit.cardcrawl.core.Settings");
                Field isModded = Settings.getDeclaredField("isModded");
                isModded.set(null, true);
                System.out.println("Done.");

                // Add ModTheSpire section to CardCrawlGame.VERSION_NUM
                System.out.printf("Adding ModTheSpire to version...");
                System.out.flush();
                Class<?> CardCrawlGame = loader.loadClass("com.megacrit.cardcrawl.core.CardCrawlGame");
                Field VERSION_NUM = CardCrawlGame.getDeclaredField("VERSION_NUM");
                String oldVersion = (String) VERSION_NUM.get(null);
                VERSION_NUM.set(null, oldVersion + " [ModTheSpire " + MTS_VERSION.get() + "]");
                System.out.println("Done.");

                // Initialize any mods that implement SpireInitializer.initialize()
                System.out.println("Initializing mods...");
                List<String> initialized = Patcher.initializeMods(loader, modOnlyUrls);
                // DEPRECATED
                // Initialize any mods which declare an initialization function
                for (int i = 0; i < modUrls.length - 1; i++) {
                    String modUrl = modUrls[i].toString();
                    String modName = modUrl.substring(modUrl.lastIndexOf('/') + 1, modUrl.length() - 4);

                    try {
                        Class<?> modMainClass = loader.loadClass(modName.toLowerCase() + "." + modName);
                        Method initialize = modMainClass.getDeclaredMethod("initialize");
                        if (!initialized.contains(modMainClass.getName())) {
                            System.out.println("WARNING: <ModName>.<ModName>.initialize() method is deprecated and will be removed in a future version of ModTheSpire." +
                                " Use @SpireInitializer intead.");
                            initialize.invoke(null);
                        }
                    } catch (ClassNotFoundException e) {
                        continue;
                    } catch (NoSuchMethodException e) {
                        continue;
                    }
                }
                System.out.println("Done.");
            }

            System.out.println("Starting game...");
            Class<?> cls = loader.loadClass("com.megacrit.cardcrawl.desktop.DesktopLauncher");
            Method method = cls.getDeclaredMethod("main", String[].class);
            method.invoke(null, (Object) ARGS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // buildUrlArray - builds the URL array to pass to the ClassLoader
    private static URL[] buildUrlArray(File[] modJars) throws MalformedURLException {
        URL[] urls = new URL[modJars.length + 1];
        for (int i = 0; i < modJars.length; i++) {
            urls[i] = modJars[i].toURI().toURL();
        }

        urls[modJars.length] = new File(STS_JAR).toURI().toURL();
        return urls;
    }

    public static ModInfo[] buildInfoArray(File[] modJars) {
        ModInfo[] infos = new ModInfo[modJars.length];
        for (int i = 0; i < modJars.length; ++i) {
            infos[i] = ModInfo.ReadModInfo(modJars[i]);
        }
        return infos;
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

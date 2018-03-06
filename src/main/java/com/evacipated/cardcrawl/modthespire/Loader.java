package com.evacipated.cardcrawl.modthespire;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class Loader {
    public static boolean DEBUG = false;

    public static Version MTS_VERSION;
    private static String MOD_DIR = "mods/";
    public static String STS_JAR = "desktop-1.0.jar";
    private static String MAC_STS_JAR = "SlayTheSpire.app/Contents/Resources/" + STS_JAR;
    private static String STS_JAR2 = "SlayTheSpire.jar";
    public static String COREPATCHES_JAR = "/corepatches.jar";
    public static ModInfo[] MODINFOS;
    public static URL[] MODONLYURLS;

    private static final String PROPERTIES_FILEPATH = "ModTheSpire.properties";
    public static Properties MTS_PROPERTIES;

    private static Object ARGS;
    private static ModSelectWindow ex;

    public static void main(String[] args) {
        ARGS = args;
        MTS_PROPERTIES = new Properties();
        File file = new File(PROPERTIES_FILEPATH);
        if (file.exists()) {
            try {
                MTS_PROPERTIES.load(new FileInputStream(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        MTS_PROPERTIES.setProperty("debug", MTS_PROPERTIES.getProperty("debug", Boolean.toString(false)));
        DEBUG = Boolean.parseBoolean(MTS_PROPERTIES.getProperty("debug", Boolean.toString(false)));

        if (Arrays.asList(args).contains("--debug")) {
            DEBUG = true;
        }

        try {
            Properties properties = new Properties();
            properties.load(Loader.class.getResourceAsStream("/META-INF/version.prop"));
            MTS_VERSION = new Version(properties.getProperty("version"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
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
            ex = new ModSelectWindow(getAllModFiles());
            ex.setVisible(true);

            String java_version = System.getProperty("java.version");
            if (!java_version.startsWith("1.8")) {
                String msg = "ModTheSpire requires Java version 8 to run properly.\nYou are currently using Java " + java_version;
                JOptionPane.showMessageDialog(null, msg, "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    public static void saveProperties()
    {
        try {
            MTS_PROPERTIES.store(new FileOutputStream(PROPERTIES_FILEPATH), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeWindow()
    {
        ex.dispatchEvent(new WindowEvent(ex, WindowEvent.WINDOW_CLOSING));
    }

    // runMods - sets up the ClassLoader, sets the isModded flag and launches the game
    public static void runMods(File[] modJars) {
        if (Loader.DEBUG) {
            System.out.println("Debug mode!");
        }
        try {
            // Check that desktop-1.0.jar exists
            {
                File tmp = new File(STS_JAR);
                if (!tmp.exists()) {
                    // Check if for the Mac version
                    tmp = new File(MAC_STS_JAR);
                    checkFileInfo(tmp);
                    if (!tmp.exists()) {
                        checkFileInfo(new File("SlayTheSpire.app"));
                        checkFileInfo(new File("SlayTheSpire.app/Contents"));
                        checkFileInfo(new File("SlayTheSpire.app/Contents/Resources"));

                        JOptionPane.showMessageDialog(null, "Unable to find '" + STS_JAR + "'");
                        return;
                    } else {
                        System.out.println("Using Mac version at: " + MAC_STS_JAR);
                        STS_JAR = MAC_STS_JAR;
                    }
                }
            }

            // Construct ClassLoader
            URL[] modUrls = buildUrlArray(modJars);
            MTSClassLoader loader = new MTSClassLoader(Loader.class.getResourceAsStream(COREPATCHES_JAR), modUrls, Loader.class.getClassLoader());

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
                SortedMap<String, CtClass> ctClasses = new TreeMap<>();
                // Find and inject core patches
                System.out.println("Finding core patches...");
                for (CtClass cls : Patcher.injectPatches(loader, pool, Patcher.findPatches(new URL[]{Loader.class.getResource(Loader.COREPATCHES_JAR)}))) {
                    ctClasses.put(countSuperClasses(cls) + cls.getName(), cls);
                }
                // Find and inject mod patches
                System.out.println("Finding patches...");
                for (CtClass cls :Patcher.injectPatches(loader, pool, Patcher.findPatches(modOnlyUrls, MODINFOS))) {
                    ctClasses.put(countSuperClasses(cls) + cls.getName(), cls);
                }

                Patcher.finalizePatches(loader);
                Patcher.compilePatches(loader, ctClasses);

                System.out.printf("Patching enums...");
                Patcher.patchEnums(loader, Loader.class.getResource(Loader.COREPATCHES_JAR));
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
        if (!file.exists() || !file.isDirectory()) return new File[0];

        File[] files = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jar");
            }
        });

        if (files.length > 0) return files;
        return new File[0];
    }

    private static void checkFileInfo(File file)
    {
        System.out.printf(file.getName() + ": ");
        System.out.println(file.exists() ? "Exists" : "Does not exist");

        if (file.exists()) {
            System.out.printf("Type: ");
            if (file.isFile()) {
                System.out.println("File");
            } else if (file.isDirectory()) {
                System.out.println("Directory");
                System.out.println("Contents:");
                for (File subfile : Objects.requireNonNull(file.listFiles())) {
                    System.out.println("  " + subfile.getName());
                }
            } else {
                System.out.println("Unknown");
            }
        }
    }

    private static int countSuperClasses(CtClass cls)
    {
        String name = cls.getName();
        int count = 0;

        while (cls != null) {
            try {
                cls = cls.getSuperclass();
            } catch (NotFoundException e) {
                break;
            }
            ++count;
        }

        return count;
    }
}

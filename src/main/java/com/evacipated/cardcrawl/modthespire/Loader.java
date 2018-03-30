package com.evacipated.cardcrawl.modthespire;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import org.objectweb.asm.ClassReader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
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

    static SpireConfig MTS_CONFIG;
    static String STS_VERSION = null;

    private static Object ARGS;
    private static ModSelectWindow ex;
    private static URL latestReleaseURL = null;

    public static void main(String[] args) {
        ARGS = args;
        try {
            Properties defaults = new Properties();
            defaults.setProperty("debug", Boolean.toString(false));
            defaults.putAll(ModSelectWindow.getDefaults());
            MTS_CONFIG = new SpireConfig(null, "ModTheSpire", defaults);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DEBUG = MTS_CONFIG.getBool("debug");

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

        // Check if we are desktop-1.0.jar
        try {
            String thisJarName = new File(Loader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName();
            if (thisJarName.equals(STS_JAR)) {
                STS_JAR = STS_JAR2;
            }
        } catch (URISyntaxException e) {
            // NOP
        }
        // Check that desktop-1.0.jar exists
        {
            File tmp = new File(STS_JAR);
            if (!tmp.exists()) {
                // Search for Steam install
                String steamJar = SteamSearch.findDesktopJar();
                if (steamJar != null && new File(steamJar).exists()) {
                    STS_JAR = steamJar;
                } else {
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
        }

        findGameVersion();

        EventQueue.invokeLater(() -> {
            try {
                ex = new ModSelectWindow(getAllModFiles());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            ex.setVisible(true);

            String java_version = System.getProperty("java.version");
            if (!java_version.startsWith("1.8")) {
                String msg = "ModTheSpire requires Java version 8 to run properly.\nYou are currently using Java " + java_version;
                JOptionPane.showMessageDialog(null, msg, "Warning", JOptionPane.WARNING_MESSAGE);
            }

            // Check for ModTheSpire update
            new Thread(() -> {
                ex.setUpdateIcon(ModSelectWindow.UpdateIconType.CHECKING);
                try {
                    UpdateChecker updateChecker = new GithubUpdateChecker("kiooeht", "ModTheSpire");
                    if (updateChecker.isNewerVersionAvailable(MTS_VERSION)) {
                        latestReleaseURL = updateChecker.getLatestReleaseURL();
                        ex.setUpdateIcon(ModSelectWindow.UpdateIconType.UPDATE_AVAILABLE);
                    } else {
                        ex.setUpdateIcon(ModSelectWindow.UpdateIconType.UPTODATE);
                    }
                } catch (IOException e) {
                    // NOP
                }
            }).start();
        });
    }

    public static void closeWindow()
    {
        ex.dispatchEvent(new WindowEvent(ex, WindowEvent.WINDOW_CLOSING));
    }

    public static void openLatestReleaseURL()
    {
        if (latestReleaseURL != null) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(latestReleaseURL.toURI());
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // runMods - sets up the ClassLoader, sets the isModded flag and launches the game
    public static void runMods(File[] modJars) {
        if (Loader.DEBUG) {
            System.out.println("Debug mode!");
        }
        try {
            ModInfo[] modInfos = buildInfoArray(modJars);
            checkDependencies(modInfos);
            modInfos = orderDependencies(modInfos);
            MODINFOS = modInfos;

            printMTSInfo();

            MTSClassLoader loader = new MTSClassLoader(Loader.class.getResourceAsStream(COREPATCHES_JAR), buildUrlArray(modInfos), Loader.class.getClassLoader());

            if (modJars.length > 0) {
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
                for (CtClass cls : Patcher.injectPatches(loader, pool, Patcher.findPatches(MODINFOS))) {
                    ctClasses.put(countSuperClasses(cls) + cls.getName(), cls);
                }

                Patcher.finalizePatches(loader);
                Patcher.compilePatches(loader, ctClasses);

                System.out.printf("Patching enums...");
                Patcher.patchEnums(loader, Loader.class.getResource(Loader.COREPATCHES_JAR));
                // Patch SpireEnums from mods
                Patcher.patchEnums(loader, modInfos);
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
                List<String> initialized = Patcher.initializeMods(loader, modInfos);
                // DEPRECATED
                // Initialize any mods which declare an initialization function
                for (int i = 0; i < modInfos.length - 1; i++) {
                    String modUrl = modInfos[i].jarURL.toString();
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
        } catch (MissingDependencyException e) {
            System.err.println("ERROR: " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "Missing Dependency", JOptionPane.ERROR_MESSAGE);
        } catch (DuplicateModIDException e) {
            System.err.println("ERROR: " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "Duplicate Mod ID", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setGameVersion(String versionString)
    {
        if (versionString.startsWith("(") && versionString.endsWith(")")) {
            versionString = versionString.substring(1, versionString.length()-1);
        }
        STS_VERSION = versionString;
    }

    private static void findGameVersion()
    {
        try {
            URLClassLoader tmpLoader = new URLClassLoader(new URL[]{new File(STS_JAR).toURI().toURL()});
            InputStream in = tmpLoader.getResourceAsStream("com/megacrit/cardcrawl/core/CardCrawlGame.class");
            ClassReader classReader = new ClassReader(in);

            classReader.accept(new GameVersionFinder(), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // buildUrlArray - builds the URL array to pass to the ClassLoader
    private static URL[] buildUrlArray(ModInfo[] modInfos) throws MalformedURLException {
        URL[] urls = new URL[modInfos.length + 1];
        for (int i = 0; i < modInfos.length; i++) {
            urls[i] = modInfos[i].jarURL;
        }

        urls[modInfos.length] = new File(STS_JAR).toURI().toURL();
        return urls;
    }

    public static ModInfo[] buildInfoArray(File[] modJars) throws MalformedURLException
    {
        ModInfo[] infos = new ModInfo[modJars.length];
        for (int i = 0; i < modJars.length; ++i) {
            infos[i] = ModInfo.ReadModInfo(modJars[i]);
            infos[i].jarURL = modJars[i].toURI().toURL();
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

    private static void printMTSInfo()
    {
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("Slay the Spire version: " + STS_VERSION);
        System.out.println("ModTheSpire version: " + MTS_VERSION.get());
        System.out.printf("Mod list: ");
        for (ModInfo info : MODINFOS) {
            if (info.ID == null || info.ID.isEmpty()) {
                System.out.printf(info.Name);
            } else {
                System.out.printf(info.ID);
            }
            if (info.Version != null) {
                System.out.printf(" (%s)", info.Version.get());
            }
            System.out.printf(", ");
        }
        System.out.println();
    }

    private static void checkDependencies(ModInfo[] modinfos) throws MissingDependencyException, DuplicateModIDException
    {
        Map<String, ModInfo> dependencyMap = new HashMap<>();
        for (final ModInfo info : modinfos) {
            if (info.ID != null) {
                if (!dependencyMap.containsKey(info.ID)) {
                    dependencyMap.put(info.ID, info);
                } else {
                    throw new DuplicateModIDException(dependencyMap.get(info.ID), info);
                }
            }
        }

        for (final ModInfo info : modinfos) {
            for (String dependency : info.Dependencies) {
                boolean has = false;
                for (final ModInfo dependinfo : modinfos) {
                    if (dependinfo.ID != null && dependinfo.ID.equals(dependency)) {
                        has = true;
                        break;
                    }
                }
                if (!has) {
                    throw new MissingDependencyException(info, dependency);
                }
            }
        }
    }

    private static int findDependencyIndex(ModInfo[] modInfos, String dependencyID)
    {
        for (int i=0; i<modInfos.length; ++i) {
            if (modInfos[i].ID.equals(dependencyID)) {
                return i;
            }
        }
        return -1;
    }

    private static ModInfo[] orderDependencies(ModInfo[] modInfos) throws CyclicDependencyException
    {
        GraphTS<ModInfo> g = new GraphTS<>();

        for (final ModInfo info : modInfos) {
            g.addVertex(info);
        }

        for (int i=0; i<modInfos.length; ++i) {
            for (String dependency : modInfos[i].Dependencies) {
                g.addEdge(findDependencyIndex(modInfos, dependency), i);
            }
        }

        g.tsortStable();

        return g.sortedArray.toArray(new ModInfo[g.sortedArray.size()]);
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

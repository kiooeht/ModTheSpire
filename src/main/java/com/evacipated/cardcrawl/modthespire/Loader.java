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
    public static int MTS_VERSION_NUM = 1;
    private static String MOD_DIR = "mods/";

    public static void main(String[] args) {
        File[] mod_jars = getAllModFiles();

        EventQueue.invokeLater(() -> {
           ModSelectWindow ex = new ModSelectWindow(mod_jars, args);
            ex.setVisible(true);
        });
    }

    public static void runMod(File mod_jar, String[] args)
    {
        try {
            String this_jar_name = new File(Loader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName();
            //System.out.println(this_jar_name);

            File desktop_jar = new File("desktop-1.0.jar");
            File slaythespire_jar = new File("SlayTheSpire.jar");

            String mod_name = "";
            if (mod_jar != null) {
                System.out.println(mod_jar.getName());
                mod_name = mod_jar.getName();
                mod_name = mod_name.substring(0, mod_name.length() - 4);
            }

            ArrayList<URL> urls = new ArrayList<>();
            // Add mod jar, if found
            if (mod_jar != null) {
                urls.add(mod_jar.toURI().toURL());
            }
            urls.add(slaythespire_jar.toURI().toURL());
            // Don't add desktop-1.0.jar if we're desktop-1.0.jar
            if (!this_jar_name.equals("desktop-1.0.jar")) {
                urls.add(desktop_jar.toURI().toURL());
            }
            URL[] urls_arr = new URL[urls.size()];
            urls.toArray(urls_arr);
            URLClassLoader loader = URLClassLoader.newInstance(urls_arr, ClassLoader.getSystemClassLoader());

            if (mod_jar != null) {
                // Set Settings.isModded = true
                Class<?> Settings = loader.loadClass("com.megacrit.cardcrawl.core.Settings");
                Field isModded = Settings.getDeclaredField("isModded");
                isModded.set(null, true);

                // Add "[ModTheSpire: MOD_NAME]" to version text
                InputStream in = loader.getResourceAsStream("ModTheSpireVersion");
                System.out.println(mod_name);
                if (in != null) {
                    Scanner s = new Scanner(in).useDelimiter("\\A");
                    mod_name = s.hasNext() ? s.next() : "";
                    System.out.println(mod_name);
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
                if (!mod_name.isEmpty()) {
                    System.out.println(mod_name);
                    Class<?> CardCrawlGame = loader.loadClass("com.megacrit.cardcrawl.core.CardCrawlGame");
                    Field VERSION_NUM = CardCrawlGame.getDeclaredField("VERSION_NUM");
                    String oldversion = (String) VERSION_NUM.get(null);
                    VERSION_NUM.set(null, "[ModTheSpire: " + mod_name + "] " + oldversion);
                }
            }

            Class<?> cls = loader.loadClass("com.megacrit.cardcrawl.desktop.DesktopLauncher");
            Method method = cls.getDeclaredMethod("main", String[].class);
            method.invoke(null, (Object) args);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String[] ignored_jars = {"desktop-1.0.jar", "SlayTheSpire.jar", "ModTheSpire.jar"};

    private static File[] getAllModFiles()
    {
        File file = new File(MOD_DIR);
        if (!file.exists() || !file.isDirectory())
            return null;

        File[] files = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.toLowerCase().endsWith(".jar") && !Arrays.asList(ignored_jars).contains(name)) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        if (files.length > 0)
            return files;
        return null;
    }
}

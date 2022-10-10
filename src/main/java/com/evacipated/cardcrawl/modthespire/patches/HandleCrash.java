package com.evacipated.cardcrawl.modthespire.patches;

import com.evacipated.cardcrawl.modthespire.Loader;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class HandleCrash
{
    public static Throwable crash = null;

    public static void maybeExit()
    {
        if (crash != null) {
            System.err.println("Game crashed.");
            Loader.printMTSInfo(System.err);
            tryPrintModsInStacktrace(crash);
            System.err.println("Cause:");
            crash.printStackTrace();
            Loader.restoreWindowOnCrash();
        } else {
            System.out.println("Game closed.");
            if (!Loader.DEBUG) {
                Loader.closeWindow();
            }
        }
    }

    private static void tryPrintModsInStacktrace(Throwable exception) {
        try {
            printModsInStacktrace(exception);
        } catch (Exception e) {
            // ignore
        }
    }

    private static void printModsInStacktrace(Throwable exception) {
        Set<String> classes = new HashSet<>();
        addClassesInThrowable(exception, classes);

        Set<URL> urls = new HashSet<>();
        for (String className : classes) {
            try {
                Class<?> cls = Class.forName(className);
                urls.add(cls.getProtectionDomain().getCodeSource().getLocation());
            } catch (ClassNotFoundException | NoClassDefFoundError ignore) {
                // ignore
            }
        }

        Set<String> modInfoLines = new HashSet<>();
        for (URL url : urls) {
            if (url == null) {
                continue;
            }

            Arrays.stream(Loader.MODINFOS).filter(m -> m.jarURL.equals(url)).findFirst().ifPresent(modInfo -> {
                if (!modInfo.ID.equals("basemod")) {
                    modInfoLines.add(String.format("%s (%s)", modInfo.ID, modInfo.ModVersion));
                }
            });
        }

        if (modInfoLines.size() > 0) {
            System.err.println("Mods in stacktrace:");
            for (String modId : modInfoLines.stream().sorted().collect(Collectors.toList())) {
                System.err.println(" - " + modId);
            }
        }
    }

    private static void addClassesInThrowable(Throwable exception, Set<String> classes) {
        for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
            classes.add(stackTraceElement.getClassName());
        }

        for (Throwable suppressed : exception.getSuppressed()) {
            if (suppressed != null) {
                addClassesInThrowable(suppressed, classes);
            }
        }

        Throwable cause = exception.getCause();
        if (cause != null) {
            addClassesInThrowable(cause, classes);
        }
    }
}

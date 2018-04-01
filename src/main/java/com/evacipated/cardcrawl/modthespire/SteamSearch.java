package com.evacipated.cardcrawl.modthespire;

import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SteamSearch
{
    private static final int appId = 646570;

    private static String installDir = null;

    public static String findDesktopJar()
    {
        prepare();

        if (installDir == null) {
            return null;
        }

        return Paths.get(installDir, Loader.STS_JAR).toString();
    }

    private static void prepare()
    {
        if (installDir != null) {
            return;
        }

        Path steamPath = getSteamPath();
        if (steamPath == null) {
            System.err.println("ERROR: Failed to find Steam installation.");
        }

        if (containsAcfFile(steamPath)) {
            installDir = steamToSTSPath(steamPath).toString();
            return;
        }

        File tmp = Paths.get(steamPath.toString(), "libraryfolders.vdf").toFile();
        if (tmp.exists()) {
            List<Path> libraries = readLibraryFolders(tmp);
            for (Path library : libraries) {
                if (containsAcfFile(library)) {
                    installDir = steamToSTSPath(library).toString();
                    return;
                }
            }
        }
    }

    private static Path getSteamPath()
    {
        Path steamPath = null;

        if (SystemUtils.IS_OS_WINDOWS) {
            steamPath = Paths.get(System.getenv("ProgramFiles") + " (x86)", "Steam", "steamapps");
            if (!steamPath.toFile().exists()) {
                steamPath = Paths.get(System.getenv("ProgramFiles"), "Steam", "steamapps");
                if (!steamPath.toFile().exists()) {
                    steamPath = null;
                }
            }
            return steamPath;
        } else if (SystemUtils.IS_OS_MAC) {
            steamPath = Paths.get("~/Library/Application Support/Steam/steamapps");
            if (!steamPath.toFile().exists()) {
                steamPath = null;
            }
        } else if (SystemUtils.IS_OS_LINUX) {
            steamPath = Paths.get("~/.steam/steam/SteamApps");
            if (!steamPath.toFile().exists()) {
                steamPath = Paths.get("~/.local/share/steam/SteamApps");
                if (!steamPath.toFile().exists()) {
                    steamPath = null;
                }
            }
        }

        return steamPath;
    }

    private static Path steamToSTSPath(Path path)
    {
        return Paths.get(path.toString(), "common", "SlayTheSpire");
    }

    private static boolean containsAcfFile(Path path)
    {
        Path acfFilePath = Paths.get(path.toString(), "appmanifest_" + appId + ".acf");
        return acfFilePath.toFile().exists();
    }

    private static List<Path> readLibraryFolders(File file)
    {
        List<Path> libraries = new ArrayList<>();

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = line.split("\t");
                if (tokens.length == 4 && isInteger(tokens[1].substring(1, tokens[1].length()-1))) {
                    libraries.add(Paths.get(tokens[3].substring(1, tokens[3].length()-1), "steamapps"));
                }
            }
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return libraries;
    }

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }
}

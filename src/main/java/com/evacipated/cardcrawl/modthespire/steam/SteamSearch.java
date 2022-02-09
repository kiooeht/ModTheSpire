package com.evacipated.cardcrawl.modthespire.steam;

import com.evacipated.cardcrawl.modthespire.Loader;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SteamSearch
{
    private static final int appId = 646570;

    private static String installDir = null;

    public static String findJRE()
    {
        return findJRE("jre");
    }

    public static String findJRE51()
    {
        return findJRE(Loader.JRE_51_DIR);
    }

    public static String findJRE(String jreBase)
    {
        String javaExecName = "java";
        if (SystemUtils.IS_OS_WINDOWS) {
            javaExecName = "java.exe";
        }

        Path local = Paths.get(jreBase, "bin", javaExecName);
        if (local.toFile().exists()) {
            System.out.println("Using local StS JRE @ " + local.toAbsolutePath().toString());
            return local.toString();
        }

        prepare();

        if (installDir == null) {
            return null;
        }

        Path install = Paths.get(installDir, jreBase, "bin", javaExecName);
        if (install.toFile().exists()) {
            System.out.println("Using install StS JRE @ " + install.toAbsolutePath().toString());
            return install.toString();
        }
        return null;
    }

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
            return;
        }

        if (containsAcfFile(steamPath)) {
            installDir = steamToSTSPath(steamPath).toString();
            return;
        }

        File tmp = Paths.get(steamPath.toString(), "libraryfolders.vdf").toFile();
        if (tmp.exists()) {
            List<Path> libraries = readLibraryFolders2(tmp);
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
            steamPath = Paths.get(SystemUtils.USER_HOME, "Library/Application Support/Steam/steamapps");
            if (!steamPath.toFile().exists()) {
                steamPath = null;
            }
        } else if (SystemUtils.IS_OS_LINUX) {
            Path[] possiblePaths = {
                Paths.get(SystemUtils.USER_HOME, ".steam/steam/SteamApps"),
                Paths.get(SystemUtils.USER_HOME, ".steam/steam/steamapps"),
                Paths.get(SystemUtils.USER_HOME, ".local/share/steam/SteamApps"),
                Paths.get(SystemUtils.USER_HOME, ".local/share/steam/steamapps")
            };

            for (Path p : possiblePaths) {
                if (p.toFile().exists()) {
                    steamPath = p;
                    break;
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

    // Parses the old libraryfolders.vdf format
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

    // Parses the new (as of 2021/07/29) libraryfolders.vdf format
    // Falls back to old (readLibraryFolders) if no libraries are found
    private static List<Path> readLibraryFolders2(File file)
    {
        List<Path> libraries = new ArrayList<>();

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = Arrays.stream(line.split("\t"))
                    .filter(str -> !str.isEmpty())
                    .toArray(String[]::new);
                if (tokens.length == 2 && "\"path\"".equals(tokens[0])) {
                    libraries.add(Paths.get(tokens[1].substring(1, tokens[1].length()-1), "steamapps"));
                }
            }
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (libraries.isEmpty()) {
            return readLibraryFolders(file);
        }
        return libraries;
    }

    private static boolean isInteger(String s)
    {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }

    public static class WorkshopInfo
    {
        private final String title;
        private final String id;
        private final String installPath;
        private final int timeUpdated;
        private List<String> tags;

        public WorkshopInfo(String title, String id, String installPath, String timeUpdated, String tagsString)
        {
            this.title = title;
            this.id = id;
            this.installPath = Paths.get(installPath).toAbsolutePath().toString();
            int tmpTime = 0;
            try {
                tmpTime = Integer.parseInt(timeUpdated);
            } catch (NumberFormatException ignore) {
            }
            this.timeUpdated = tmpTime;
            String[] tmp = tagsString.split(",");
            tags = new ArrayList<>();
            for (String s : tmp) {
                tags.add(s.toLowerCase().trim());
            }
        }

        public String getTitle()
        {
            return title;
        }

        public String getID()
        {
            return id;
        }

        public String getInstallPath()
        {
            return installPath;
        }

        public int getTimeUpdated()
        {
            return timeUpdated;
        }

        public List<String> getTags()
        {
            return tags;
        }

        public boolean hasTag(String tag)
        {
            tag = tag.toLowerCase().trim();
            String finalTag = tag;
            return tags.stream().anyMatch(t -> t.equals(finalTag));
        }
    }
}

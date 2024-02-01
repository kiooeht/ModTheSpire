package com.evacipated.cardcrawl.modthespire;

import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.desktop.DesktopLauncher;
import javassist.*;
import org.apache.commons.lang3.NotImplementedException;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.jar.*;
import java.util.stream.Collectors;

class PackageJar
{
    private static class Entries
    {
        private final Map<String, Entry> entries = new HashMap<>();

        public int size()
        {
            return entries.size();
        }

        public boolean add(Entry entry)
        {
            if (entries.containsKey(entry.path)) {
                return false;
            }
            entries.put(entry.path, entry);
            return true;
        }

        public boolean contains(String path)
        {
            return entries.containsKey(path);
        }

        public Entry get(String path)
        {
            return entries.get(path);
        }

        public Iterable<Entry> getOutJarEntries()
        {
            return entries.values().stream()
                .filter(x -> x.type == Entry.Type.OUTJAR)
                .collect(Collectors.toList());
        }
    }

    private static class Entry
    {
        enum Type
        {
            BASEGAME,
            MOD,
            OUTJAR,
            MTS,
            COREPATCH,
            KOTLIN
        }

        String path;
        String modID = null;
        Type type;
        byte[] b;
        URL locationURL;


        Entry(String path, Type type)
        {
            this.path = path;
            this.type = type;
        }

        Entry(String path, String modID)
        {
            this.path = path;
            this.modID = modID;
            if (modID == null) {
                type = Type.BASEGAME;
            } else {
                type = Type.MOD;
            }
        }

        Entry(String path, byte[] b, URL locationURL)
        {
            this.path = path;
            this.b = b;
            try {
                if (!Objects.equals(locationURL, new File(ModTheSpire.STS_JAR).toURI().toURL())) {
                    this.locationURL = locationURL;
                }
            } catch (MalformedURLException ignored) {}
            type = Type.OUTJAR;
        }

        public String getName()
        {
            switch (type) {
                case BASEGAME:
                    return "base game";
                case OUTJAR:
                    return "out-jar";
                case MTS:
                    return "ModTheSpire";
                case COREPATCH:
                    return "core patch";
                case KOTLIN:
                    return "kotlin";
                case MOD:
                default:
                    return modID;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return path.equals(entry.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path);
        }
    }

    private static void findMTSEntries(Entries entries, InputStream src)
        throws IOException
    {
        findEntries(entries, src, path -> new Entry(path, Entry.Type.MTS));
    }

    private static void findCorePatchEntries(Entries entries, InputStream src)
        throws IOException
    {
        findEntries(entries, src, path -> new Entry(path, Entry.Type.COREPATCH));
    }

    private static void findKotlinEntries(Entries entries, InputStream src)
        throws IOException
    {
        findEntries(entries, src, path -> new Entry(path, Entry.Type.KOTLIN));
    }

    private static void findModEntries(Entries entries, File src, String modID)
        throws IOException
    {
        findEntries(entries, new FileInputStream(src), path -> new Entry(path, modID));
    }

    private static final Set<String> exceptions = new HashSet<>();
    private static void findEntries(Entries entries, InputStream src, Function<String, Entry> factory)
        throws IOException
    {
        JarInputStream srcJar = new JarInputStream(src);

        JarEntry entry;
        while ((entry = srcJar.getNextJarEntry()) != null) {
            JarEntry finalEntry = entry;
            if (exceptions.stream().anyMatch(x -> finalEntry.getName().startsWith(x))) {
                continue;
            }
            if (!entries.add(factory.apply(entry.getName()))) {
                //System.out.println("Duplicate: " + entry.getName());
            }
        }

        srcJar.close();
    }

    private static void copyJarContents(JarOutputStream destJar, Entries entries, File src, String modID)
        throws IOException
    {
        Entry.Type type = modID == null ? Entry.Type.BASEGAME : Entry.Type.MOD;
        copyJarContents(destJar, entries, new FileInputStream(src), modID, type);
    }

    private static void copyJarContents(JarOutputStream destJar, Entries entries, InputStream src, String modID, Entry.Type type)
        throws IOException
    {
        JarInputStream srcJar = new JarInputStream(src);

        JarEntry jarEntry;
        while ((jarEntry = srcJar.getNextJarEntry()) != null) {
            JarEntry finalJarEntry = jarEntry;
            if (exceptions.stream().anyMatch(x -> finalJarEntry.getName().startsWith(x))) {
                continue;
            }
            if (!entries.contains(jarEntry.getName())) {
                System.out.println(jarEntry.getName());
                System.out.println("Huh?");
                continue;
            }
            Entry entry = entries.get(jarEntry.getName());
            if (entry.type != type || !Objects.equals(entry.modID, modID)) {
                //System.out.println("    " + jarEntry.getName());
                //System.out.println("    Skipped (using " + entries.get(jarEntry.getName()).getName() + "'s version instead)");
                continue;
            }

            try {
                destJar.putNextEntry(jarEntry);
                byte[] buffer = new byte[4048];
                int bytesRead;
                while ((bytesRead = srcJar.read(buffer)) != -1) {
                    destJar.write(buffer, 0, bytesRead);
                }
            } catch (Exception e) {
                System.out.println(jarEntry.getName());
                System.out.println(e);
            }
        }

        srcJar.close();
    }

    private static CtClass setupPrepackagedLauncher(MTSClassPool pool)
    {
        try {
            CtClass ctClass = pool.get(PrepackagedLauncher.class.getName());
            pool.importPackage("java.net");
            pool.importPackage("java.nio.file");
            pool.importPackage("com.google.gson");

            // SpireInitializers
            CtMethod ctMethod = ctClass.getDeclaredMethod("callInitializers");
            StringBuilder src = new StringBuilder("{\n");

            for (ModInfo info : ModTheSpire.MODINFOS) {
                if (Patcher.annotationDBMap.containsKey(info.jarURL)) {
                    Set<String> initializers = Patcher.annotationDBMap.get(info.jarURL).getAnnotationIndex().get(SpireInitializer.class.getName());
                    if (initializers != null) {
                        for (String initializer : initializers) {
                            src.append(initializer).append(".");
                            if (info.ID.startsWith("__sideload_")) {
                                try {
                                    pool.get(initializer).getDeclaredMethod("sideload");
                                    src.append("sideload");
                                } catch (NotFoundException e) {
                                    src.append("initialize");
                                }
                            } else {
                                src.append("initialize");
                            }
                            src.append("();\n");
                        }
                    }
                } else {
                    System.err.println(info.jarURL + " Not in DB map. Something is very wrong");
                }
            }

            src.append("}");
            ctMethod.setBody(src.toString());

            // MODINFOS
            ctMethod = ctClass.getDeclaredMethod("getModInfos");
            src.setLength(0);
            src.append("{\n");
            src.append(ModInfo.class.getName()).append("[] ret = new ").append(ModInfo.class.getName()).append("[").append(ModTheSpire.MODINFOS.length).append("];\n");

            src.append("Gson gson = new GsonBuilder()\n")
                .append(".excludeFieldsWithModifiers(new int[] {java.lang.reflect.Modifier.STATIC})\n")
                .append(".setDateFormat(\"MM-dd-yyyy\")\n")
                .append(".create();\n");

            Gson gson = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .setDateFormat("MM-dd-yyyy")
                .create();

            src.append("URL baseURL = ").append(PrepackagedLauncher.class.getName()).append(".class.getProtectionDomain().getCodeSource().getLocation();\n");
            for (int i = 0; i< ModTheSpire.MODINFOS.length; ++i) {
                URL oldURL = ModTheSpire.MODINFOS[i].jarURL;
                try {
                    ModTheSpire.MODINFOS[i].jarURL = Paths.get("package").resolve(Paths.get(oldURL.toURI()).getFileName()).toUri().toURL();
                    ModTheSpire.MODINFOS[i].jarURL = new URL("file:package/" + Paths.get(oldURL.toURI()).getFileName().toString());
                } catch (MalformedURLException | URISyntaxException e) {
                    e.printStackTrace();
                }
                String json = gson.toJson(ModTheSpire.MODINFOS[i]);
                ModTheSpire.MODINFOS[i].jarURL = oldURL;
                src.append("ret[").append(i).append("] = gson.fromJson(").append("\"").append(json.replaceAll("\"", "\\\\\"")).append("\", ")
                    .append(ModInfo.class.getName()).append(".class);\n");
                src.append("adjustJarURL(ret[").append(i).append("]);\n");
            }

            src.append("return ret;\n");

            src.append("}");
            //System.out.println(src);
            ctMethod.setBody(src.toString());

            return ctClass;
        } catch (NotFoundException | CannotCompileException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String createModdedJarName(String filename)
    {
        // Split into base and extension
        String[] tokens = filename.split("\\.(?=[^\\.]+$)");
        if (tokens.length < 2) {
            return filename;
        }

        return tokens[0] + "-modded." + tokens[1];
    }

    private static String createClassPath()
    {
        StringBuilder sb = new StringBuilder();

        for (ModInfo info : ModTheSpire.MODINFOS) {
            try {
                String filename = Paths.get(info.jarURL.toURI()).getFileName().toString();
                sb.append("package/")
                    .append(createModdedJarName(filename))
                    .append(" ");
            } catch (URISyntaxException ignored) {}
        }
        // Remove trailing space
        sb.setLength(sb.length() - 1);

        return sb.toString();
    }

    public static void packageJar(MTSClassPool pool, String jarPath)
        throws SecurityException, IllegalArgumentException, IOException, URISyntaxException
    {
        CtClass ctPrePackagedLauncher = setupPrepackagedLauncher(pool);

        File outFile = new File(jarPath);

        try {
            Manifest manifest = new Manifest();
            Attributes attributes = manifest.getMainAttributes();
            attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            attributes.put(Attributes.Name.MAIN_CLASS, PrepackagedLauncher.class.getName());
            attributes.put(Attributes.Name.CLASS_PATH, createClassPath());
            attributes.put(new Attributes.Name("Created-By"), "ModTheSpire");
            JarOutputStream outJar = new JarOutputStream(new FileOutputStream(outFile), manifest);
            Entries entries = new Entries();

            try {
                assert ctPrePackagedLauncher != null;
                entries.add(new Entry(ctPrePackagedLauncher.getName().replaceAll("\\.", "/") + ".class", ctPrePackagedLauncher.toBytecode(), null));
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }

            System.out.println("  Finding entries...");
            // Find ModTheSpire
            try {
                //exceptions.add("assets/");
                exceptions.add("com/codedisaster/steamworks");
                exceptions.add("com/google/gson");
                exceptions.add("com/megacrit/cardcrawl/desktop/DesktopLauncher");
                findMTSEntries(entries, new FileInputStream(new File(ModTheSpire.class.getProtectionDomain().getCodeSource().getLocation().toURI())));
                exceptions.clear();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            // Find kotlin
            findKotlinEntries(entries, ModTheSpire.class.getResourceAsStream(ModTheSpire.KOTLIN_JAR));
            // Find out-jar
            Set<CtClass> ctClasses = pool.getOutJarClasses();
            for (CtClass ctClass : ctClasses) {
                try {
                    URL locationURL = null;
                    try {
                        String url = ctClass.getURL().getFile();
                        int i = url.lastIndexOf('!');
                        if (i >= 0) {
                            url = url.substring(0, i);
                        }
                        if (url.endsWith(".jar")) {
                            locationURL = new URL(url);
                        }
                    } catch (NotFoundException ignored) {
                    }

                    String className = ctClass.getName();
                    byte[] b = ctClass.toBytecode();
                    String classPath = className.replaceAll("\\.", "/") + ".class";
                    entries.add(new Entry(classPath, b, locationURL));
                } catch (IOException | CannotCompileException e) {
                    // eat it - just means this isn't a file we've loaded
                }
            }
            // Find core patches
            findCorePatchEntries(entries, ModTheSpire.class.getResourceAsStream(ModTheSpire.COREPATCHES_JAR));
            // Find mods
            for (ModInfo modInfo : ModTheSpire.MODINFOS) {
                try {
                    findModEntries(entries, new File(modInfo.jarURL.toURI()), modInfo.ID);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            // Find base game
            findModEntries(entries, new File(ModTheSpire.STS_JAR), null);

            System.out.println("  " + entries.size() + " entries");

            // Copy ModTheSpire
            System.out.println("  Copying ModTheSpire entries...");
            try {
                //exceptions.add("assets/");
                exceptions.add("com/codedisaster/steamworks");
                exceptions.add("com/google/gson");
                exceptions.add("com/megacrit/cardcrawl/desktop/DesktopLauncher");
                InputStream is = new FileInputStream(new File(ModTheSpire.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
                copyJarContents(outJar, entries, is, null, Entry.Type.MTS);
                exceptions.clear();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            // Copy kotlin
            System.out.println("  Copying kotlin entries...");
            copyJarContents(outJar, entries, ModTheSpire.class.getResourceAsStream(ModTheSpire.KOTLIN_JAR), null, Entry.Type.KOTLIN);
            // Copy base game out-jar
            System.out.println("  Copying base game out-jar entries...");
            for (Entry entry : entries.getOutJarEntries()) {
                if (entry.locationURL == null) {
                    outJar.putNextEntry(new JarEntry(entry.path));
                    outJar.write(entry.b);
                }
            }
            // Copy core patches
            System.out.println("  Copying core patch entries...");
            copyJarContents(outJar, entries, ModTheSpire.class.getResourceAsStream(ModTheSpire.COREPATCHES_JAR), null, Entry.Type.COREPATCH);
            // Copy base game
            System.out.println("  Copying base game entries...");
            copyJarContents(outJar, entries, new File(ModTheSpire.STS_JAR), null);

            outJar.close();

            // Do mod jars
            new File("package").mkdirs();
            for (ModInfo modInfo : ModTheSpire.MODINFOS) {
                String filename = Paths.get(modInfo.jarURL.toURI()).getFileName().toString();
                outJar = new JarOutputStream(new FileOutputStream(Paths.get("package", createModdedJarName(filename)).toFile()));
                System.out.println("  Copying " + modInfo.ID + "...");
                // Copy mod out-jar
                System.out.println("    Copying mod out-jar entries...");
                for (Entry entry : entries.getOutJarEntries()) {
                    if (entry.locationURL != null && entry.locationURL.equals(modInfo.jarURL)) {
                        outJar.putNextEntry(new JarEntry(entry.path));
                        outJar.write(entry.b);
                    }
                }
                // Copy mods
                System.out.println("    Copying mod entries...");
                try {
                    copyJarContents(outJar, entries, new File(modInfo.jarURL.toURI()), modInfo.ID);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                outJar.close();
            }
        } finally {
            // NOP?
        }
    }

    public static class PrepackagedLauncher
    {
        public static void main(String[] args)
            throws IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchFieldException, URISyntaxException
        {
            ModTheSpire.STS_JAR = new File(PrepackagedLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
            ModTheSpire.loadMTSVersion("p");
            CardCrawlGame.VERSION_NUM += " [ModTheSpire " + ModTheSpire.MTS_VERSION + "]";

            ModTheSpire.MODINFOS = getModInfos();

            try {
                Field f = ModTheSpire.class.getDeclaredField("POOL");
                f.setAccessible(true);
                f.set(null, ClassPool.getDefault());
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

            bustEnums();
            callInitializers();

            DesktopLauncher.main(args);
        }

        private static ModInfo[] getModInfos()
        {
            throw new NotImplementedException("This shouldn't exist");
        }

        private static void bustEnums()
            throws IOException, NoSuchFieldException, ClassNotFoundException, IllegalAccessException
        {
            List<URL> urls = Arrays.stream(ModTheSpire.MODINFOS)
                .map(x -> x.jarURL)
                .collect(Collectors.toList());
            urls.add(0, PrepackagedLauncher.class.getProtectionDomain().getCodeSource().getLocation());
            URL[] arr = urls.toArray(urls.toArray(new URL[0]));

            Patcher.bustEnums(PrepackagedLauncher.class.getClassLoader(), arr);
        }

        private static void callInitializers()
        {
            throw new NotImplementedException("This shouldn't exist");
        }

        @SuppressWarnings("unused")
        private static void adjustJarURL(ModInfo modInfo) throws MalformedURLException
        {
            URL oldURL = modInfo.jarURL;
            int index = oldURL.toString().lastIndexOf('/') + 1;
            String filename = oldURL.toString().substring(index);
            modInfo.jarURL = Paths.get("package")
                .resolve(PackageJar.createModdedJarName(filename))
                .toUri().toURL();
        }
    }
}

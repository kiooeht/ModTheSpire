package com.evacipated.cardcrawl.modthespire;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.apache.logging.log4j.core.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

class OutJar
{
    public static class FilePathAndBytes
    {
        public String path;
        public byte[] b;

        public FilePathAndBytes(String path, byte[] b)
        {
            this.path = path;
            this.b = b;
        }
    }

    /* https://stackoverflow.com/questions/2548384/java-get-a-list-of-all-classes-loaded-in-the-jvm?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa */
    private static Iterator<Class<?>> getClassList(ClassLoader CL)
        throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        Class<?> CL_class = CL.getClass();
        while (CL_class != java.lang.ClassLoader.class) {
            CL_class = CL_class.getSuperclass();
        }
        java.lang.reflect.Field ClassLoader_classes_field = CL_class
            .getDeclaredField("classes");
        ClassLoader_classes_field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Vector<Class<?>> classes = (Vector<Class<?>>) ClassLoader_classes_field.get(CL);
        return classes.iterator();
    }

    /* https://stackoverflow.com/questions/22591903/javassist-how-to-inject-a-method-into-a-class-in-jar?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa */
    public static class JarHandler
    {
        public void writeOut(String jarPathAndName, List<FilePathAndBytes> files)
            throws IOException
        {
            File jarFile = new File(jarPathAndName);

            try {
                JarOutputStream tempJar = new JarOutputStream(new FileOutputStream(jarFile));

                try {
                    // Open the given file.

                    try {
                        // Create a jar entry and add it to the temp jar.

                        for (FilePathAndBytes file : files) {
                            String fileName = file.path;
                            byte[] fileByteCode = file.b;
                            JarEntry entry = new JarEntry(fileName);
                            tempJar.putNextEntry(entry);
                            tempJar.write(fileByteCode);
                        }

                    } catch (Exception ex) {
                        System.out.println(ex);

                        // Add a stub entry here, so that the jar will close
                        // without an
                        // exception.

                        tempJar.putNextEntry(new JarEntry("stub"));
                    }

                } catch (Exception ex) {
                    System.out.println(ex);

                    // IMportant so the jar will close without an
                    // exception.

                    tempJar.putNextEntry(new JarEntry("stub"));
                } finally {
                    tempJar.close();
                }
            } finally {
                // do I need to do things here
            }
        }
    }

    public static void dumpJar(ClassLoader loader, ClassPool pool, String jarPath)
        throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, IOException
    {
        Iterator<Class<?>> loadedClasses = getClassList(loader);
        List<FilePathAndBytes> files = new ArrayList<>();
        for (; loadedClasses.hasNext();) {
            try {
                String className = loadedClasses.next().getName();
                CtClass ctClass;
                ctClass = pool.get(className);
                byte[] b = ctClass.toBytecode();
                String classPath = className.replaceAll("\\.", "/") + ".class";
                files.add(new FilePathAndBytes(classPath, b));
            } catch (NotFoundException | IOException | CannotCompileException e) {
                // eat it - just means this isn't a file we've loaded
            }
        }
        JarHandler handler = new JarHandler();
        handler.writeOut(jarPath, files);
    }

    public static void dumpAlteredDesktopJar()
        throws NotFoundException, IOException, CannotCompileException
    {
        SortedMap<String, CtClass> ctClasses = new TreeMap<>();

        ctClasses = Patcher.patchEverythingPublic(null, ctClasses);

        System.out.printf("Start compiling...");
        for (Map.Entry<String, CtClass> cls : ctClasses.entrySet()) {
            cls.getValue().writeFile("public-desktop-1.0");
        }
        System.out.println("Done.");

        System.out.printf("Creating JAR...");
        File archiveFile = new File("public-desktop-1.0.jar");
        FileOutputStream stream = new FileOutputStream(archiveFile);
        JarOutputStream out = new JarOutputStream(stream, new Manifest());

        byte buffer[] = new byte[10240];
        Path path = Paths.get("public-desktop-1.0");
        Files.walkFileTree(path, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                File pathFile = file.toFile();
                String name = pathFile.getAbsolutePath();
                if (name.startsWith(path.toAbsolutePath().toString())) {
                    name = name.substring(path.toAbsolutePath().toString().length() + 1);
                }
                name = name.replace("\\", "/");
                JarEntry jarAdd = new JarEntry(name);
                jarAdd.setTime(pathFile.lastModified());
                out.putNextEntry(jarAdd);

                FileInputStream in = new FileInputStream(pathFile);
                while (true) {
                    int nRead = in.read(buffer, 0, buffer.length);
                    if (nRead <= 0) {
                        break;
                    }
                    out.write(buffer, 0, nRead);
                }
                in.close();

                return FileVisitResult.CONTINUE;
            }
        });
        System.out.println("Done.");

        out.close();
        stream.close();

        System.out.printf("Cleaning up...");
        Files.walk(path)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
        System.out.println("Done");
    }
}

package com.evacipated.cardcrawl.modthespire;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

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
}

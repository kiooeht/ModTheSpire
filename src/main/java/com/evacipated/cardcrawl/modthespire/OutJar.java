package com.evacipated.cardcrawl.modthespire;

import javassist.CannotCompileException;
import javassist.CtClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    public static void dumpJar(MTSClassPool pool, String jarPath)
        throws SecurityException, IllegalArgumentException, IOException
    {
        Set<CtClass> ctClasses = pool.getOutJarClasses();
        List<FilePathAndBytes> files = new ArrayList<>();
        for (CtClass ctClass : ctClasses) {
            try {
                String className = ctClass.getName();
                byte[] b = ctClass.toBytecode();
                String classPath = className.replaceAll("\\.", "/") + ".class";
                files.add(new FilePathAndBytes(classPath, b));
            } catch (IOException | CannotCompileException e) {
                // eat it - just means this isn't a file we've loaded
            }
        }
        JarHandler handler = new JarHandler();
        handler.writeOut(jarPath, files);
    }
}

package com.evacipated.cardcrawl.modthespire;

import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ByteArrayMapClassPath implements ClassPath
{
    protected Map<String, Info> classes = new HashMap<>();

    public ByteArrayMapClassPath() {}

    public void addClass(CtClass ctClass) throws CannotCompileException
    {
        try {
            URL url = null;
            try {
                url = ctClass.getURL();
            } catch (NotFoundException ignored) {}
            classes.put(ctClass.getName(), new Info(url, ctClass.toBytecode()));
        } catch (IOException e) {
            throw new CannotCompileException(e);
        }
    }

    @Override
    public void close() {}

    @Override
    public InputStream openClassfile(String classname)
    {
        Info classInfo = classes.get(classname);
        if (classInfo != null) {
            return new ByteArrayInputStream(classInfo.classfile);
        }
        return null;
    }

    @Override
    public URL find(String classname)
    {
        if (classes.containsKey(classname)) {
            return classes.get(classname).url;
        }

        return null;
    }

    public void printDebugInfo()
    {
        int bytes = 0;
        for (Info info : classes.values()) {
            bytes += info.classfile.length;
        }
        bytes /= 1024;

        System.out.printf("%d modified classes (%dKB)%n", classes.size(), bytes);
    }

    private static class Info
    {
        final URL url;
        final byte[] classfile;

        Info(URL url, byte[] classfile)
        {
            this.url = url;
            this.classfile = classfile;
        }
    }
}

package com.evacipated.cardcrawl.modthespire.agent;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModTheSpire;
import javassist.ClassMap;
import javassist.bytecode.ClassFile;
import javassist.bytecode.Descriptor;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

class MTSAgent
{
    public static void premain(String agentArgs, Instrumentation inst)
    {
        System.out.println("ModTheSpire Premain");
        inst.addTransformer(new RenameLoader());
    }

    public static void agentmain(String agentArgs, Instrumentation inst)
    {
        System.out.println("Done.");
        inst.addTransformer(new RenameLoader());
    }

    static class RenameLoader implements ClassFileTransformer
    {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException
        {
            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(classfileBuffer))) {
                ClassFile cf = new ClassFile(in);
                ClassMap cm = new ClassMap() {
                    @Override
                    public void put(String oldname, String newname)
                    {
                        put0(oldname, newname);
                    }

                    @Override
                    public Object get(Object jvmClassName)
                    {
                        String n = toJavaName((String) jvmClassName);
                        put0(n, n);
                        return null;
                    }

                    @Override
                    public void fix(String name) {}
                };
                cf.getRefClasses(cm);
                if (cm.containsKey(Loader.class.getName())) {
                    cf.renameClass(
                        Descriptor.toJvmName(Loader.class.getName()),
                        Descriptor.toJvmName(ModTheSpire.class.getName())
                    );

                    ByteArrayOutputStream barr = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(barr);
                    cf.write(out);
                    out.close();
                    return barr.toByteArray();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }
    }
}

package com.evacipated.cardcrawl.modthespire.agent;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModTheSpire;
import com.evacipated.cardcrawl.modthespire.steam.SteamSearch;
import javassist.CannotCompileException;
import javassist.ClassMap;
import javassist.bytecode.*;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Objects;

class MTSAgent
{
    public static void premain(String agentArgs, Instrumentation inst)
    {
        System.out.println("ModTheSpire Premain");
        inst.addTransformer(new RenameLoader());
        inst.addTransformer(new WorkshopInfoIDFixer());
        ModTheSpire.AGENT_ENABLED = true;
    }

    public static void agentmain(String agentArgs, Instrumentation inst)
    {
        if (ModTheSpire.AGENT_ENABLED) {
            System.out.println("MTSAgent already enabled.");
            return;
        }
        System.out.println("Done.");
        inst.addTransformer(new RenameLoader());
        inst.addTransformer(new WorkshopInfoIDFixer());
        ModTheSpire.AGENT_ENABLED = true;
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

    static class WorkshopInfoIDFixer implements ClassFileTransformer
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
                if (cm.containsKey(SteamSearch.WorkshopInfo.class.getName())) {
                    boolean fixedClass = false;
                    ConstPool cp = cf.getConstPool();
                    for (MethodInfo methodInfo : (List<MethodInfo>) cf.getMethods()) {
                        boolean fixedMethod = false;
                        CodeAttribute codeAttr = methodInfo.getCodeAttribute();
                        if (codeAttr == null) {
                            continue;
                        }
                        CodeIterator it = codeAttr.iterator();
                        while (it.hasNext()) {
                            try {
                                int pos = it.next();

                                int c = it.byteAt(pos);
                                if (c == Opcode.INVOKEVIRTUAL) {
                                    int v = it.u16bitAt(pos + 1);
                                    String methodClassName = cp.getMethodrefClassName(v);
                                    String methodName = cp.getMethodrefName(v);
                                    String methodType = cp.getMethodrefType(v);
                                    if (Objects.equals(methodClassName, SteamSearch.WorkshopInfo.class.getName()) && Objects.equals(methodName, "getID") && Objects.equals(methodType, "()Ljava/lang/String;")) {
                                        Bytecode b = new Bytecode(cp);
                                        // Replace "String getID()" calls with "long getID()"
                                        b.addInvokevirtual(methodClassName, methodName, "()J");
                                        // Then add "Long.toString(getID())" to mod receives a String as expected
                                        b.addInvokestatic(Long.class.getName(), "toString", "(J)Ljava/lang/String;");
                                        it.writeByte(Opcode.NOP, pos);
                                        it.write16bit(Opcode.NOP, pos + 1);
                                        it.insert(pos, b.get());
                                        fixedMethod = true;
                                    }
                                }
                            } catch (BadBytecode e) {
                                throw new CannotCompileException(e);
                            }
                        }

                        if (fixedMethod) {
                            fixedClass = true;
                        }
                    }

                    if (fixedClass) {
                        ByteArrayOutputStream barr = new ByteArrayOutputStream();
                        DataOutputStream out = new DataOutputStream(barr);
                        cf.write(out);
                        out.close();
                        return barr.toByteArray();
                    }
                }
            } catch (IOException | CannotCompileException e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }
    }
}

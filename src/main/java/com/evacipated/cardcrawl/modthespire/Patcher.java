package com.evacipated.cardcrawl.modthespire;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.*;
import javassist.*;
import org.scannotation.AnnotationDB;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class Patcher {
    private static Map<URL, AnnotationDB> annotationDBMap = new HashMap<>();
    private static Map<Class<?>, EnumBusterReflect> enumBusterMap = new HashMap<>();
    private static TreeSet<PatchInfo> patchInfos = new TreeSet<>(new PatchInfoComparator());

    public static List<String> initializeMods(ClassLoader loader, URL... urls) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException
    {
        List<String> result = new ArrayList<>();

        for (URL url : urls) {
            if (annotationDBMap.containsKey(url)) {
                Set<String> initializers = annotationDBMap.get(url).getAnnotationIndex().get(SpireInitializer.class.getName());
                if (initializers != null) {
                    for (String initializer : initializers) {
                        try {
                            Method init = loader.loadClass(initializer).getDeclaredMethod("initialize");
                            init.invoke(null);
                            result.add(initializer);
                        } catch (NoSuchMethodException e) {
                            System.out.println("WARNING: Unable to find method initialize() on class marked @SpireInitializer: " + initializer);
                        }
                    }
                }
            } else {
                System.err.println(url + " Not in DB map. Something is very wrong");
            }
        }

        return result;
    }

    public static List<Iterable<String>> findPatches(URL[] urls) throws IOException
    {
        return findPatches(urls, null);
    }

    public static List<Iterable<String>> findPatches(URL[] urls, ModInfo[] modInfos) throws IOException
    {
        List<Iterable<String>> patchSetList = new ArrayList<>();
        for (int i = 0; i < urls.length; ++i) {
            if (modInfos == null || modInfos[i].MTS_Version.compareTo(Loader.MTS_VERSION) <= 0) {
                AnnotationDB db;
                if (annotationDBMap.containsKey(urls[i])) {
                    db = annotationDBMap.get(urls[i]);
                } else {
                    db = new AnnotationDB();
                    annotationDBMap.put(urls[i], db);
                }
                db.scanArchives(urls[i]);
                patchSetList.add(db.getAnnotationIndex().get(SpirePatch.class.getName()));
            } else {
                String str = "ERROR: " + modInfos[i].Name + " requires ModTheSpire v" + modInfos[i].MTS_Version.get() + " or greater!";
                System.out.println(str);
                JOptionPane.showMessageDialog(null, str);
            }
        }
        return patchSetList;
    }

    public static void patchEnums(ClassLoader loader, URL... urls) throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException
    {
        AnnotationDB db = new AnnotationDB();
        db.setScanClassAnnotations(false);
        db.setScanMethodAnnotations(false);
        db.scanArchives(urls);

        Set<String> annotations = db.getAnnotationIndex().get(SpireEnum.class.getName());
        if (annotations == null) {
            return;
        }

        for (String s : annotations) {
            Class<?> cls = loader.loadClass(s);
            for (Field field : cls.getDeclaredFields()) {
                SpireEnum spireEnum = field.getDeclaredAnnotation(SpireEnum.class);
                if (spireEnum != null) {
                    String enumName = field.getName();
                    if (!spireEnum.name().isEmpty()) {
                        enumName = spireEnum.name();
                    }

                    EnumBusterReflect buster;
                    if (enumBusterMap.containsKey(field.getType())) {
                        buster = enumBusterMap.get(field.getType());
                    } else {
                        buster = new EnumBusterReflect(loader, field.getType());
                        enumBusterMap.put(field.getType(), buster);
                    }
                    Enum<?> enumValue = buster.make(enumName);
                    buster.addByValue(enumValue);

                    field.setAccessible(true);
                    field.set(null, enumValue);
                }
            }
        }
    }

    public static void finalizePatches(ClassLoader loader) throws Exception
    {
        System.out.printf("Injecting patches...");
        if (Loader.DEBUG) {
            System.out.println();
        }
        for (PatchInfo p : patchInfos) {
            if (Loader.DEBUG) {
                p.debugPrint();
            }
            p.doPatch();
        }
        System.out.println("Done.");
    }

    public static void compilePatches(ClassLoader loader, Set<CtClass> ctClasses) throws CannotCompileException
    {
        System.out.printf("Compiling patched classes...");
        if (Loader.DEBUG) {
            System.out.println();
        }
        for (CtClass cls : ctClasses) {
            if (Loader.DEBUG) {
                System.out.println("  " + cls.getName());
            }
            cls.toClass(loader, null);
        }
        System.out.println("Done.");
    }

    public static HashSet<CtClass> injectPatches(ClassLoader loader, ClassPool pool, List<Iterable<String>> class_names) throws Exception
    {
        HashSet<CtClass> ctClasses = new HashSet<>();
        for (Iterable<String> it : class_names) {
            HashSet<CtClass> tmp = injectPatches(loader, pool, it);
            if (tmp != null) {
                ctClasses.addAll(tmp);
            }
            PatchInfo.nextMod();
        }
        return ctClasses;
    }

    public static HashSet<CtClass> injectPatches(ClassLoader loader, ClassPool pool, Iterable<String> class_names) throws Exception
    {
        if (class_names == null)
            return null;

        HashSet<CtClass> ctClasses = new HashSet<CtClass>();
        for (String cls_name : class_names) {
            CtClass ctPatchClass = pool.get(cls_name);
            if (!ctPatchClass.hasAnnotation(SpirePatch.class)) {
                JOptionPane.showMessageDialog(null, "Something went wrong finding SpirePatch on [" + cls_name + "].\n" +
                        "Most likely the mod was compiled with a different version of ModTheSpireLib.");
                continue;
            }
            SpirePatch patch = (SpirePatch) ctPatchClass.getAnnotation(SpirePatch.class);

            CtClass ctClsToPatch = pool.get(patch.cls());
            CtBehavior ctMethodToPatch = null;
            try {
                CtClass[] ctParamTypes = patchParamTypes(pool, patch);
                if (patch.method().equals("ctor")) {
                    if (ctParamTypes == null)
                        ctMethodToPatch = ctClsToPatch.getDeclaredConstructors()[0];
                    else
                        ctMethodToPatch = ctClsToPatch.getDeclaredConstructor(ctParamTypes);
                } else if (patch.method().equals("<staticinit>")) {
                    ctMethodToPatch = ctClsToPatch.getClassInitializer();
                    if (ctMethodToPatch == null) {
                        System.out.println("No class initializer, making one");
                        ctMethodToPatch = ctClsToPatch.makeClassInitializer();
                    }
                } else {
                    if (ctParamTypes == null)
                        ctMethodToPatch = ctClsToPatch.getDeclaredMethod(patch.method());
                    else
                        ctMethodToPatch = ctClsToPatch.getDeclaredMethod(patch.method(), ctParamTypes);
                }
            } catch (NotFoundException e) {
                System.err.println("ERROR: No method [" + patch.method() + "] found on class [" + patch.cls() + "]");
            }
            if (ctMethodToPatch == null)
                continue;

            for (CtMethod m : ctPatchClass.getDeclaredMethods()) {
                PatchInfo p = null;
                if (m.getName().equals("Prefix")) {
                    p = new PrefixPatchInfo(ctMethodToPatch, m);
                } else if (m.getName().equals("Postfix")) {
                    p = new PostfixPatchInfo(ctMethodToPatch, m);
                } else if (m.getName().equals("Insert")) {
                    SpireInsertPatch insertPatch = (SpireInsertPatch) m.getAnnotation(SpireInsertPatch.class);
                    if (insertPatch == null) {
                        System.err.println("    ERROR: Insert missing SpireInsertPatch info!");
                    } else if (insertPatch.loc() == -1 && insertPatch.rloc() == -1) {
                        System.err.println("    ERROR: SpireInsertPatch missing line number! Must specify either loc or rloc");
                    } else if (insertPatch.loc() >= 0) {
                        p = new InsertPatchInfo(insertPatch, insertPatch.loc(), ctMethodToPatch, m);
                    } else {
                        int abs_loc = ctMethodToPatch.getMethodInfo().getLineNumber(0) + insertPatch.rloc();
                        p = new InsertPatchInfo(insertPatch, abs_loc, ctMethodToPatch, m);
                    }
                } else if (m.getName().equals("Instrument")) {
                    p = new InstrumentPatchInfo(ctMethodToPatch, loader.loadClass(cls_name).getDeclaredMethod(m.getName()));
                } else if (m.getName().equals("Replace")) {
                    p = new ReplacePatchInfo(ctMethodToPatch, m);
                } else if (m.getName().equals("Raw")) {
                    p = new RawPatchInfo(ctMethodToPatch, findRawMethod(loader.loadClass(cls_name), m.getName()));
                }

                if (p != null) {
                    patchInfos.add(p);
                }
            }

            ctClasses.add(ctClsToPatch);
        }

        return ctClasses;
    }

    private static CtClass[] patchParamTypes(ClassPool pool, SpirePatch patch) throws NotFoundException {
        String[] def = {"DEFAULT"};
        if (Arrays.equals(patch.paramtypes(), def))
            return null;

        return pool.get(patch.paramtypes());
    }

    private static Method findRawMethod(Class<?> cls, String name) throws NoSuchMethodException
    {
        for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == 1) {
                return m;
            }
        }
        throw new NoSuchMethodException();
    }
}

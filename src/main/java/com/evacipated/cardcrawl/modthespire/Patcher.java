package com.evacipated.cardcrawl.modthespire;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.*;
import com.evacipated.cardcrawl.modthespire.patcher.InsertPatchInfo.LineNumberAndPatchType;

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
    public static Map<URL, AnnotationDB> annotationDBMap = new HashMap<>();
    private static Map<Class<?>, EnumBusterReflect> enumBusterMap = new HashMap<>();
    private static TreeSet<PatchInfo> patchInfos = new TreeSet<>(new PatchInfoComparator());

    public static void initializeMods(ClassLoader loader, ModInfo... modInfos) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException
    {
        for (ModInfo info : modInfos) {
            if (annotationDBMap.containsKey(info.jarURL)) {
                Set<String> initializers = annotationDBMap.get(info.jarURL).getAnnotationIndex().get(SpireInitializer.class.getName());
                if (initializers != null) {
                    for (String initializer : initializers) {
                        try {
                            Method init = loader.loadClass(initializer).getDeclaredMethod("initialize");
                            init.invoke(null);
                        } catch (NoSuchMethodException e) {
                            System.out.println("WARNING: Unable to find method initialize() on class marked @SpireInitializer: " + initializer);
                        }
                    }
                }
            } else {
                System.err.println(info.jarURL + " Not in DB map. Something is very wrong");
            }
        }
    }

    public static List<Iterable<String>> findPatches(URL[] urls) throws IOException
    {
        return findPatches(urls, null);
    }

    public static List<Iterable<String>> findPatches(ModInfo[] modInfos) throws IOException
    {
        URL[] urls = new URL[modInfos.length];
        for (int i = 0; i < modInfos.length; i++) {
            urls[i] = modInfos[i].jarURL;
        }
        return findPatches(urls, modInfos);
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
                patchSetList.add(db.getAnnotationIndex().get(SpirePatches.class.getName()));
            } else {
                String str = "ERROR: " + modInfos[i].Name + " requires ModTheSpire v" + modInfos[i].MTS_Version.get() + " or greater!";
                System.out.println(str);
                JOptionPane.showMessageDialog(null, str);
            }
        }
        return patchSetList;
    }

    public static void patchEnums(ClassLoader loader, ModInfo[] modInfos) throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException
    {
        URL[] urls = new URL[modInfos.length];
        for (int i = 0; i < modInfos.length; i++) {
            urls[i] = modInfos[i].jarURL;
        }
        patchEnums(loader, urls);
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
            System.out.println();
        }
        for (PatchInfo p : patchInfos) {
            if (Loader.DEBUG) {
                p.debugPrint();
            }
            try {
                p.doPatch();
            } catch (Exception e) {
                if (!Loader.DEBUG) {
                    System.out.println();
                    p.debugPrint();
                }
                throw e;
            }
            if (Loader.DEBUG) {
                System.out.println();
            }
        }
        patchInfos.clear();
        System.out.println("Done.");
    }

    public static void compilePatches(ClassLoader loader, SortedMap<String, CtClass> ctClasses) throws CannotCompileException
    {
        System.out.printf("Compiling patched classes...");
        if (Loader.DEBUG) {
            System.out.println();
        }
        for (Map.Entry<String, CtClass> cls : ctClasses.entrySet()) {
            if (Loader.DEBUG) {
                System.out.println("  " + cls.getValue().getName());
            }
            cls.getValue().toClass(loader, null);
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

        HashSet<CtClass> ctClasses = new HashSet<>();
        for (String cls_name : class_names) {
            CtClass ctPatchClass = pool.get(cls_name);
            
            SpirePatch[] patchArr = null;
            SpirePatches patches = (SpirePatches) ctPatchClass.getAnnotation(SpirePatches.class);
            if (patches != null) {
                patchArr = patches.value();
            } else {
                SpirePatch patch = (SpirePatch) ctPatchClass.getAnnotation(SpirePatch.class);
                if (patch != null) {
                    patchArr = new SpirePatch[]{patch};
                }
            }

            for (SpirePatch patch : patchArr) {
                CtClass ctClsToPatch = null;
                try {
                    if (!patch.clz().equals(void.class)) {
                        ctClsToPatch = pool.get(patch.clz().getName());
                    } else if (!patch.cls().isEmpty()) {
                        ctClsToPatch = pool.get(patch.cls());
                    }
                } catch (NotFoundException e) {
                    if (patch.optional()) {
                        continue;
                    }
                    throw e;
                }
                if (ctClsToPatch == null) {
                    throw new PatchingException(ctPatchClass, "No class defined to patch. Must define either clz or cls in @SpirePatch.");
                }
                CtBehavior ctMethodToPatch = null;
                try {
                    CtClass[] ctParamTypes = patchParamTypez(pool, patch);
                    if (ctParamTypes == null) {
                        ctParamTypes = patchParamTypes(pool, patch);
                    }
                    if (patch.method().equals(SpirePatch.CONSTRUCTOR)) {
                        if (ctParamTypes == null) {
                            ctMethodToPatch = ctClsToPatch.getDeclaredConstructors()[0];
                        } else {
                            ctMethodToPatch = ctClsToPatch.getDeclaredConstructor(ctParamTypes);
                        }
                    } else if (patch.method().equals(SpirePatch.STATICINITIALIZER)) {
                        ctMethodToPatch = ctClsToPatch.getClassInitializer();
                        if (ctMethodToPatch == null) {
                            System.out.println("No class initializer, making one");
                            ctMethodToPatch = ctClsToPatch.makeClassInitializer();
                        }
                    } else if (patch.method().equals(SpirePatch.CLASS)) {
                        patchInfos.add(new ClassPatchInfo(ctClsToPatch, ctPatchClass));
                        ctClasses.add(ctClsToPatch);
                        ctClasses.add(ctPatchClass);
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
                    if (m.getName().equals("Prefix") || m.hasAnnotation(SpirePrefixPatch.class)) {
                        p = new PrefixPatchInfo(ctMethodToPatch, m);
                    } else if (m.getName().equals("Postfix") || m.hasAnnotation(SpirePostfixPatch.class)) {
                        p = new PostfixPatchInfo(ctMethodToPatch, m);
                    } else if (m.getName().equals("Locator")) {
                        continue;
                    } else if (m.getName().equals("Insert") || m.hasAnnotation(SpireInsertPatch.class)) {
                        SpireInsertPatch insertPatch = (SpireInsertPatch) m.getAnnotation(SpireInsertPatch.class);

                        LocatorInfo locatorInfo = null;
                        if (insertPatch != null && !insertPatch.locator().equals(SpireInsertPatch.NONE.class)) {
                            locatorInfo = new LocatorInfo(ctMethodToPatch, loader.loadClass(insertPatch.locator().getName()));
                        }

                        if (!isInsertPatchValid(insertPatch, locatorInfo)) {
                            throw new PatchingException(m, "SpireInsertPatch missing line number! Must specify either loc, rloc, locs, rlocs, or a Locator");
                        }

                        List<LineNumberAndPatchType> locs = new ArrayList<>();

                        if (locatorInfo != null) {
                            int[] abs_locs = locatorInfo.findLines();
                            if (abs_locs.length < 1) {
                                throw new PatchingException(m, "Locator must locate at least 1 line!");
                            }
                            for (int i = 0; i < abs_locs.length; i++) {
                                locs.add(new LineNumberAndPatchType(abs_locs[i]));
                            }
                        }

                        if (insertPatch != null) {
                            if (insertPatch.loc() >= 0) {
                                locs.add(new LineNumberAndPatchType(insertPatch.loc()));
                            }
                            if (insertPatch.rloc() >= 0) {
                                locs.add(new LineNumberAndPatchType(
                                    ctMethodToPatch.getMethodInfo().getLineNumber(0) + insertPatch.rloc(), insertPatch.rloc()));
                            }
                            for (int i = 0; i < insertPatch.locs().length; i++) {
                                locs.add(new LineNumberAndPatchType(insertPatch.locs()[i]));
                            }
                            for (int i = 0; i < insertPatch.rlocs().length; i++) {
                                locs.add(new LineNumberAndPatchType(
                                    ctMethodToPatch.getMethodInfo().getLineNumber(0) + insertPatch.rlocs()[i], insertPatch.rlocs()[i]));
                            }
                        }

                        p = new InsertPatchInfo(insertPatch, locs, ctMethodToPatch, m);
                    
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
        }

        return ctClasses;
    }

    private static boolean isInsertPatchValid(SpireInsertPatch insertPatch, LocatorInfo locatorInfo) {
        if (locatorInfo != null) {
            return true;
        }
        if (insertPatch != null) {
            if (insertPatch.loc() != -1 || insertPatch.rloc() != -1
                || insertPatch.locs().length != 0 || insertPatch.rlocs().length != 0) {
                return true;
            }
        }
        return false;
    }

    private static CtClass[] patchParamTypes(ClassPool pool, SpirePatch patch) throws NotFoundException {
        String[] def = {"DEFAULT"};
        if (Arrays.equals(patch.paramtypes(), def)) {
            return null;
        }

        return pool.get(patch.paramtypes());
    }

    private static CtClass[] patchParamTypez(ClassPool pool, SpirePatch patch) throws NotFoundException {
        if (patch.paramtypez().length == 1 && void.class.equals(patch.paramtypez()[0])) {
            return null;
        }

        String[] names = new String[patch.paramtypez().length];
        for (int i=0; i<patch.paramtypez().length; ++i) {
            names[i] = patch.paramtypez()[i].getName();
        }
        return pool.get(names);
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

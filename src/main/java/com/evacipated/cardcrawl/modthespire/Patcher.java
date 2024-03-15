package com.evacipated.cardcrawl.modthespire;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.*;
import com.evacipated.cardcrawl.modthespire.patcher.InsertPatchInfo.LineNumberAndPatchType;
import com.evacipated.cardcrawl.modthespire.patcher.javassist.MyCodeConverter;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.SyntheticAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationImpl;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.scannotation.AnnotationDB;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.*;

public class Patcher {
    public static Map<URL, AnnotationDB> annotationDBMap = new HashMap<>();
    private static Map<Class<?>, EnumBusterReflect> enumBusterMap = new HashMap<>();
    private static TreeSet<PatchInfo> patchInfos = new TreeSet<>(new PatchInfoComparator());
    private static Map<CtMethod, ByRef2Info> byRef2Infos = new LinkedHashMap<>();

    public static void initializeMods(ClassLoader loader, ModInfo... modInfos) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException
    {
        for (ModInfo info : modInfos) {
            if (annotationDBMap.containsKey(info.jarURL)) {
                Set<String> initializers = annotationDBMap.get(info.jarURL).getAnnotationIndex().get(SpireInitializer.class.getName());
                if (initializers != null) {
                    System.out.println(" - " + info.Name);
                    for (String initializer : initializers) {
                        System.out.println("   - " + initializer);
                        try {
                            long startTime = System.nanoTime();
                            Method init = null;
                            if (info.ID.startsWith("__sideload_")) {
                                init = loader.loadClass(initializer).getDeclaredMethod("sideload");
                            }
                            if (init == null) {
                                init = loader.loadClass(initializer).getDeclaredMethod("initialize");
                            }
                            init.invoke(null);
                            long endTime = System.nanoTime();
                            long duration = endTime - startTime;
                            System.out.println("   - " + (duration / 1000000) + "ms");
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

    public static ModInfo[] sideloadMods(MTSClassLoader tmpPatchingLoader, MTSClassLoader loader, ClassPool pool, ModInfo[] allModInfos, ModInfo[] modInfos)
        throws IOException, NotFoundException, ClassNotFoundException
    {
        List<String> sideloadList = new ArrayList<>();
        for (ModInfo modInfo : modInfos) {
            if (modInfo.MTS_Version.compareTo(ModTheSpire.MTS_VERSION) <= 0) {
                AnnotationDB db;
                if (annotationDBMap.containsKey(modInfo.jarURL)) {
                    db = annotationDBMap.get(modInfo.jarURL);
                } else {
                    db = new AnnotationDB();
                    annotationDBMap.put(modInfo.jarURL, db);
                }
                db.scanArchives(modInfo.jarURL);
                Iterable<String> tmp = db.getAnnotationIndex().get(SpireSideload.class.getName());
                if (tmp != null) {
                    tmp.forEach(sideloadList::add);
                }
            } else {
                String str = "ERROR: " + modInfo.Name + " requires ModTheSpire v" + modInfo.MTS_Version + " or greater!";
                System.out.println(str);
                JOptionPane.showMessageDialog(null, str);
            }
        }

        for (String class_name : sideloadList) {
            CtClass ctSideloadClass = pool.get(class_name);

            SpireSideload sideload = (SpireSideload) ctSideloadClass.getAnnotation(SpireSideload.class);
            if (sideload != null) {
                for (String modid : sideload.modIDs()) {
                    if (!ModTheSpire.isModLoaded(modid)) {
                        System.out.print("Sideloading " + modid + "...");
                        ModInfo info = null;
                        for (ModInfo allInfo : allModInfos) {
                            if (allInfo.ID.equals(modid)) {
                                info = allInfo;
                                break;
                            }
                        }
                        if (info != null) {
                            // Add dummy value to modid TODO?
                            info.ID = "__sideload_" + info.ID;
                            // Sideload mod into classloaders
                            tmpPatchingLoader.addURL(info.jarURL);
                            loader.addURL(info.jarURL);
                            // Sideload mod into MODINFOS
                            modInfos = Arrays.copyOf(modInfos, modInfos.length + 1);
                            modInfos[modInfos.length - 1] = info;
                            System.out.println("Done.");
                        } else {
                            System.out.println("Not found.");
                        }
                    }
                }
            }
        }

        return modInfos;
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
            if (modInfos == null || modInfos[i].MTS_Version.compareTo(ModTheSpire.MTS_VERSION) <= 0) {
                AnnotationDB db;
                if (annotationDBMap.containsKey(urls[i])) {
                    db = annotationDBMap.get(urls[i]);
                } else {
                    db = new AnnotationDB();
                    annotationDBMap.put(urls[i], db);
                }
                db.scanArchives(urls[i]);
                Set<String> set = new HashSet<>();
                Set<String> it = db.getAnnotationIndex().get(SpirePatch.class.getName());
                if (it != null) {
                    set.addAll(it);
                }
                it = db.getAnnotationIndex().get(SpirePatches.class.getName());
                if (it != null) {
                    set.addAll(it);
                }
                it = db.getAnnotationIndex().get(SpirePatch2.class.getName());
                if (it != null) {
                    set.addAll(it);
                }
                it = db.getAnnotationIndex().get(SpirePatches2.class.getName());
                if (it != null) {
                    set.addAll(it);
                }
                patchSetList.add(set);
            } else {
                String str = "ERROR: " + modInfos[i].Name + " requires ModTheSpire v" + modInfos[i].MTS_Version + " or greater!";
                System.out.println(str);
                JOptionPane.showMessageDialog(null, str);
            }
        }
        return patchSetList;
    }

    public static void patchEnums(ClassLoader loader, ClassPool pool, ModInfo[] modInfos)
        throws IOException, ClassNotFoundException, NotFoundException, CannotCompileException
    {
        URL[] urls = new URL[modInfos.length];
        for (int i = 0; i < modInfos.length; i++) {
            urls[i] = modInfos[i].jarURL;
        }
        patchEnums(loader, pool, urls);
    }

    public static void patchEnums(ClassLoader loader, ClassPool pool, URL... urls)
        throws IOException, ClassNotFoundException, NotFoundException, CannotCompileException
    {
        AnnotationDB db = new AnnotationDB();
        db.setScanClassAnnotations(false);
        db.setScanMethodAnnotations(false);
        db.scanArchives(urls);

        Set<String> annotations = db.getAnnotationIndex().get(SpireEnum.class.getName());
        if (annotations == null) {
            return;
        }

        boolean hasPrintedWarning = false;

        for (String s : annotations) {
            CtClass cls = pool.get(s);
            for (CtField field : cls.getDeclaredFields()) {
                SpireEnum spireEnum = (SpireEnum) field.getAnnotation(SpireEnum.class);
                if (spireEnum != null) {
                    String modId = spireEnum.requiredModId();
                    if (!modId.isEmpty() && !ModTheSpire.isModLoadedOrSideloaded(modId)) {
                        cls.removeField(field);
                        continue;
                    }
                    String enumName = field.getName();
                    if (!spireEnum.name().isEmpty()) {
                        enumName = spireEnum.name();
                    }

                    // Patch new field onto the enum
                    try {
                        CtClass ctClass = pool.get(field.getType().getName());
                        CtField f = new CtField(ctClass, enumName, ctClass);
                        f.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL | Modifier.ENUM);
                        ConstPool constPool = ctClass.getClassFile().getConstPool();
                        AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                        for (Object a : field.getAvailableAnnotations()) {
                            if (Proxy.getInvocationHandler(a) instanceof AnnotationImpl) {
                                AnnotationImpl impl = (AnnotationImpl) Proxy.getInvocationHandler(a);
                                if (impl.getTypeName().equals(SpireEnum.class.getName())) {
                                    continue;
                                }
                                Annotation annotation = new Annotation(impl.getTypeName(), constPool);
                                if (impl.getAnnotation().getMemberNames() != null) {
                                    for (Object memberName : impl.getAnnotation().getMemberNames()) {
                                        annotation.addMemberValue((String) memberName, impl.getAnnotation().getMemberValue((String) memberName));
                                    }
                                }
                                attr.addAnnotation(annotation);
                            }
                        }
                        f.getFieldInfo().addAttribute(attr);
                        ctClass.addField(f);
                    } catch (DuplicateMemberException ignore) {
                        // Field already exists
                        if (!ModTheSpire.DEBUG && !hasPrintedWarning) {
                            hasPrintedWarning = true;
                            System.out.println();
                        }
                        System.out.println(String.format("Warning: @SpireEnum %s %s is already defined.", field.getType().getName(), enumName));
                    }
                }
            }
        }
    }

    public static void bustEnums(ClassLoader loader, ModInfo[] modInfos)
        throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException
    {
        URL[] urls = new URL[modInfos.length];
        for (int i = 0; i < modInfos.length; i++) {
            urls[i] = modInfos[i].jarURL;
        }
        bustEnums(loader, urls);
    }

    public static void bustEnums(ClassLoader loader, URL... urls)
        throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException
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
                    try {
                        Field constantField = field.getType().getField(enumName);
                        ReflectionHelper.setStaticFinalField(constantField, enumValue);
                    } catch (NoSuchFieldException ignored) {
                    }

                    field.setAccessible(true);
                    field.set(null, enumValue);
                }
            }
        }
    }

    public static void finalizePatches(ClassLoader loader, ClassPool pool) throws Exception
    {
        createByRef2Storage(pool);

        System.out.printf("Injecting patches...");
        if (ModTheSpire.DEBUG) {
            System.out.println();
            System.out.println();
        }
        for (PatchInfo p : patchInfos) {
            if (ModTheSpire.DEBUG) {
                p.debugPrint();
            }
            try {
                p.doPatch();
            } catch (Exception e) {
                if (!ModTheSpire.DEBUG) {
                    System.out.println();
                    p.debugPrint();
                }
                throw e;
            }
            if (ModTheSpire.DEBUG) {
                System.out.println();
            }
        }
        patchInfos.clear();
        System.out.println("Done.");

        patchByRef2();
    }

    public static void addByRef2(ByRef2Info info)
    {
        ByRef2Info v = byRef2Infos.putIfAbsent(info.patchMethod, info);
        if (v != null) {
            v.add(info);
        }
    }

    private static void createByRef2Storage(ClassPool pool) throws NotFoundException, CannotCompileException
    {
        CtClass ctByRef2 = pool.get(ByRef2.class.getName());
        CtClass ctInternal = ctByRef2.makeNestedClass("$Internal", true);
        ConstPool cp = ctInternal.getClassFile().getConstPool();

        ctInternal.getClassFile().addAttribute(new SyntheticAttribute(cp));

        CtField ctStore = new CtField(pool.get(Object[].class.getName()), "$store", ctInternal);
        ctStore.setModifiers(Modifier.STATIC | Modifier.FINAL | Modifier.PUBLIC);
        ctStore.getFieldInfo().addAttribute(new SyntheticAttribute(cp));
        ctInternal.addField(ctStore, CtField.Initializer.byNewArray(pool.get(Object[].class.getName()), 256));
    }

    private static void patchByRef2() throws CannotCompileException
    {
        System.out.print("Patching ByRef2...");
        if (ModTheSpire.DEBUG) {
            System.out.println();
            System.out.println();
        }
        for (ByRef2Info info : byRef2Infos.values()) {
            if (ModTheSpire.DEBUG) {
                info.debugPrint();
            }
            try {
                info.doPatch();
            } catch (Exception e) {
                if (!ModTheSpire.DEBUG) {
                    System.out.println();
                    info.debugPrint();
                }
                throw e;
            }
            if (ModTheSpire.DEBUG) {
                System.out.println();
            }
        }
        byRef2Infos.clear();
        System.out.println("Done.");
    }

    public static ClassPath compilePatches(MTSClassLoader loader, MTSClassPool pool) throws CannotCompileException
    {
        System.out.printf("Compiling patched classes...");
        if (ModTheSpire.DEBUG) {
            System.out.println();
        }

        // Use topological sort to compile classes in a good order
        Map<String, CtClass> modifiedClasses = new HashMap<>();
        for (CtClass ctClass : pool.getModifiedClasses()) {
            modifiedClasses.put(ctClass.getName(), ctClass);
        }
        GraphTS<String> g = new GraphTS<>();
        // Object is going to be referenced by everything, put it in first to make indexOf faster
        g.addVertex(Object.class.getName());
        for (final CtClass ctClass : modifiedClasses.values()) {
            addInheritanceTree(g, ctClass);
        }
        try {
            g.tsort();
        } catch (CyclicDependencyException e) {
            throw new RuntimeException(e);
        }

        ByteArrayMapClassPath cp = new ByteArrayMapClassPath();
        for (String clsName : g.sortedArray) {
            CtClass cls = modifiedClasses.get(clsName);
            if (cls == null) {
                continue;
            }
            if (ModTheSpire.DEBUG) {
                System.out.println("  " + cls.getName());
            }
            try {
                cls.toClass(loader, null);
            } catch (CannotCompileException ignore) {
                System.out.println("    failed");
            }
            loader.registerPackage(cls); //register missing package information
            cp.addClass(cls);
            cls.detach();
        }
        System.out.println("Done.");
        if (ModTheSpire.DEBUG) {
            cp.printDebugInfo();
        }
        return cp;
    }

    private static void addInheritanceTree(GraphTS<String> g, CtClass ctClass)
    {
        try {
            // vert: this class
            if (g.indexOf(ctClass.getName()) == -1) {
                g.addVertex(ctClass.getName());
            }
            if (!ctClass.isInterface()) {
                // vert: super class
                CtClass ctSuper = ctClass.getSuperclass();
                if (ctSuper != null) {
                    addInheritanceTree(g, ctSuper);
                    // edge: this class -> super
                    g.addEdge(ctSuper.getName(), ctClass.getName());
                }
            }
            // verts: interfaces
            for (CtClass ctInterface : ctClass.getInterfaces()) {
                addInheritanceTree(g, ctInterface);
                // edge: this class -> interface
                g.addEdge(ctInterface.getName(), ctClass.getName());
            }
        } catch (NotFoundException ignore) {}
    }

    public static void injectPatches(ClassLoader loader, ClassPool pool, List<Iterable<String>> class_names) throws Exception
    {
        for (Iterable<String> it : class_names) {
            injectPatches(loader, pool, it);
            PatchInfo.nextMod();
        }
    }

    public static void injectPatches(ClassLoader loader, ClassPool pool, Iterable<String> class_names) throws Exception
    {
        if (class_names == null)
            return;

        for (String cls_name : class_names) {
            CtClass ctPatchClass = pool.get(cls_name);
            if (!Modifier.isPublic(ctPatchClass.getModifiers())) {
                ctPatchClass.setModifiers(Modifier.setPublic(ctPatchClass.getModifiers()));
            }

            List<SpirePatch> patchArr = new ArrayList<>();
            SpirePatches patches = (SpirePatches) ctPatchClass.getAnnotation(SpirePatches.class);
            if (patches != null) {
                Collections.addAll(patchArr, patches.value());
            } else {
                SpirePatch patch = (SpirePatch) ctPatchClass.getAnnotation(SpirePatch.class);
                if (patch != null) {
                    patchArr.add(patch);
                }
            }

            SpirePatches2 patches2 = (SpirePatches2) ctPatchClass.getAnnotation(SpirePatches2.class);
            if (patches2 != null) {
                Arrays.stream(patches2.value())
                    .map(p2 -> convertSpirePatch2To1(loader, pool, p2))
                    .forEachOrdered(patchArr::add);
            } else {
                SpirePatch2 patch2 = (SpirePatch2) ctPatchClass.getAnnotation(SpirePatch2.class);
                if (patch2 != null) {
                    patchArr.add(convertSpirePatch2To1(loader, pool, patch2));
                }
            }

            // Remove patches that require mods that aren't loaded
            Iterator<SpirePatch> iter = patchArr.iterator();
            while (iter.hasNext()) {
                SpirePatch patch = iter.next();
                String modId = patch.requiredModId();
                if (!modId.isEmpty() && !ModTheSpire.isModLoadedOrSideloaded(modId)) {
                    iter.remove();
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
                    throw new PatchingException(ctPatchClass.getName(), e);
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
                            CtConstructor[] constructors = ctClsToPatch.getDeclaredConstructors();
                            if (constructors.length == 1) {
                                ctMethodToPatch = constructors[0];
                            } else {
                                throw new MissingParamTypesException(ctPatchClass, patch);
                            }
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
                    } else {
                        if (ctParamTypes == null) {
                            CtMethod[] methods = ctClsToPatch.getDeclaredMethods(patch.method());
                            if (methods.length == 1) {
                                ctMethodToPatch = methods[0];
                            } else if (methods.length == 0) {
                                throw new NoSuchMethodException(String.format("Patch %s:\nNo method named [%s] found on\nclass [%s]",
                                    ctPatchClass.getName(),
                                    patch.method(),
                                    patchClassName(patch)
                                ));
                            } else {
                                throw new MissingParamTypesException(ctPatchClass, patch);
                            }
                        } else {
                            ctMethodToPatch = ctClsToPatch.getDeclaredMethod(patch.method(), ctParamTypes);
                        }
                    }
                } catch (NotFoundException e) {
                    throw new NoSuchMethodException(String.format("Patch %s:\nNo method [%s(%s)] found on\nclass [%s]",
                        ctPatchClass.getName(),
                        patch.method(),
                        patchParamTypesString(patch),
                        patchClassName(patch)
                    ));
                }
                if (ctMethodToPatch == null)
                    continue;

                for (CtMethod m : ctPatchClass.getDeclaredMethods()) {
                    PatchInfo p = null;
                    if (m.getName().equals("Prefix") || m.hasAnnotation(SpirePrefixPatch.class)) {
                        p = new PrefixPatchInfo(ctMethodToPatch, m).setSpirePatch(patch);
                    } else if (m.getName().equals("Postfix") || m.hasAnnotation(SpirePostfixPatch.class)) {
                        p = new PostfixPatchInfo(ctMethodToPatch, m).setSpirePatch(patch);
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

                        p = new InsertPatchInfo(insertPatch, locs, ctMethodToPatch, m).setSpirePatch(patch);
                    } else if (m.getName().equals("Instrument") || m.hasAnnotation(SpireInstrumentPatch.class)) {
                        p = new InstrumentPatchInfo(ctMethodToPatch, findInstrumentMethod(loader.loadClass(cls_name), m.getName())).setSpirePatch(patch);
                    } else if (m.getName().equals("Replace")) {
                        p = new ReplacePatchInfo(ctMethodToPatch, m).setSpirePatch(patch);
                    } else if (m.getName().equals("Raw") || m.hasAnnotation(SpireRawPatch.class)) {
                        p = new RawPatchInfo(ctMethodToPatch, findRawMethod(loader.loadClass(cls_name), m.getName())).setSpirePatch(patch);
                    }

                    if (p != null) {
                        if (!Modifier.isPublic(m.getModifiers())) {
                            m.setModifiers(Modifier.setPublic(m.getModifiers()));
                        }
                        if (!Modifier.isStatic(m.getModifiers())) {
                            throw new NonStaticPatchMethodException(m);
                        }

                        patchInfos.add(p);
                    }
                }
            }
        }
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

    private static CtClass[] patchParamTypez(ClassPool pool, SpirePatch patch) throws NotFoundException
    {
        if (patch.paramtypez().length == 1 && void.class.equals(patch.paramtypez()[0])) {
            return null;
        }

        String[] names = new String[patch.paramtypez().length];
        for (int i = 0; i < patch.paramtypez().length; ++i) {
            names[i] = patch.paramtypez()[i].getName();
        }
        return pool.get(names);
    }

    private static Method findInstrumentMethod(Class<?> cls, String name) throws NoSuchMethodException
    {
        for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == 0) {
                m.setAccessible(true);
                return m;
            }
        }
        throw new NoSuchMethodException();
    }

    private static Method findRawMethod(Class<?> cls, String name) throws NoSuchMethodException
    {
        for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == 1) {
                m.setAccessible(true);
                return m;
            }
        }
        throw new NoSuchMethodException();
    }

    private static String patchParamTypesString(SpirePatch patch)
    {
        if (patch.paramtypez().length == 1 && void.class.equals(patch.paramtypez()[0])) {
            String[] def = {"DEFAULT"};
            if (Arrays.equals(patch.paramtypes(), def))
                return "";

            return String.join(", ", patch.paramtypes());
        } else {
            String[] tmp = new String[patch.paramtypez().length];
            for (int i=0; i<patch.paramtypez().length; ++i) {
                tmp[i] = patch.paramtypez()[i].getName();
            }
            return String.join(", ", tmp);
        }
    }

    private static String patchClassName(SpirePatch patch)
    {
        if (patch.clz().equals(void.class)) {
            return patch.cls();
        } else {
            return patch.clz().getName();
        }
    }

    static void patchOverrides(ClassLoader loader, ClassPool pool, ModInfo[] modInfos) throws PatchingException
    {
        System.out.println("Patching Overrides...");
        MyCodeConverter.reset();

        for (AnnotationDB db : annotationDBMap.values()) {
            Set<String> classNames = db.getAnnotationIndex().get(SpireOverride.class.getName());
            if (classNames != null) {
                for (String className : classNames) {
                    if (ModTheSpire.DEBUG) {
                        System.out.println("Class: [" + className + "]");
                    }
                    try {
                        CtClass cc = pool.get(className);

                        for (CtMethod ctMethod : cc.getDeclaredMethods()) {
                            if (ctMethod.hasAnnotation(SpireOverride.class)) {
                                CtMethod superMethod = findSuperMethod(ctMethod);
                                if (superMethod == null) {
                                    throw new PatchingException(ctMethod, "Has no matching method signature in any superclass");
                                }

                                if (ModTheSpire.DEBUG) {
                                    System.out.println(" - Overriding [" + superMethod.getLongName() + "]");
                                    System.out.println("      Fixing invocations in superclass " + superMethod.getDeclaringClass().getSimpleName() + "...");
                                }

                                MyCodeConverter codeConverter = new MyCodeConverter();
                                codeConverter.redirectSpecialMethodCall(superMethod);
                                superMethod.getDeclaringClass().instrument(codeConverter);

                                if (ModTheSpire.DEBUG) {
                                    System.out.println("      Replacing SpireSuper calls...");
                                }
                                ExprEditor exprEditor = new ExprEditor() {
                                    @Override
                                    public void edit(MethodCall m) throws CannotCompileException
                                    {
                                        try {
                                            if (m.getClassName().equals(SpireSuper.class.getName())) {
                                                if (ModTheSpire.DEBUG) {
                                                    System.out.println("        @ " + m.getLineNumber());
                                                }
                                                String src = " { ";
                                                if (!ctMethod.getReturnType().equals(CtClass.voidType)) {
                                                    src += "$_ = ";
                                                }
                                                src += "super." + ctMethod.getName() + "(";
                                                for (int i=0; i<ctMethod.getParameterTypes().length; ++i) {
                                                    if (i > 0) {
                                                        src += ", ";
                                                    }
                                                    src += makeObjectCastedString(ctMethod.getParameterTypes()[i], "$1[" + i + "]");
                                                }
                                                src += ");\n";
                                                if (ctMethod.getReturnType().equals(CtClass.voidType)) {
                                                    src += "$_ = null;";
                                                }
                                                src += " }";
                                                if (ModTheSpire.DEBUG) {
                                                    System.out.println(src);
                                                }
                                                m.replace(src);
                                            }
                                        } catch (NotFoundException e) {
                                            throw new CannotCompileException(e);
                                        }
                                    }
                                };
                                ctMethod.instrument(exprEditor);
                            }
                        }
                    } catch (NotFoundException | CannotCompileException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private static CtMethod findSuperMethod(CtMethod ctMethod) throws NotFoundException
    {
        CtClass superclass = ctMethod.getDeclaringClass().getSuperclass();

        while (superclass != null) {
            try {
                CtMethod superMethod = superclass.getDeclaredMethod(ctMethod.getName(), ctMethod.getParameterTypes());
                if (ctMethod.getReturnType().equals(superMethod.getReturnType())) {
                    return superMethod;
                }
            } catch (NotFoundException ignored) {
            }

            superclass = superclass.getSuperclass();
        }

        return null;
    }

    private static String makeObjectCastedString(CtClass ctType, String value)
    {
        String typename = ctType.getName();
        String extra = "";
        if (ctType.isPrimitive()) {
            if (ctType.equals(CtPrimitiveType.intType)) {
                typename = "Integer";
            } else if (ctType.equals(CtPrimitiveType.charType)) {
                typename = "Character";
            } else {
                typename = typename.substring(0, 1).toUpperCase() + typename.substring(1);
            }

            extra = "." + ctType.getName() + "Value()";
        }

        return "((" + typename + ") " + value + ")" + extra;
    }

    private static SpirePatch convertSpirePatch2To1(ClassLoader loader, ClassPool pool, SpirePatch2 patch2)
    {
        AnnotationImpl impl = (AnnotationImpl) Proxy.getInvocationHandler(patch2);
        Annotation a = impl.getAnnotation();
        return (SpirePatch) AnnotationImpl.make(loader, SpirePatch.class, pool, a);
    }
}

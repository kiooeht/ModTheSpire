package com.evacipated.cardcrawl.modthespire;

import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import javassist.*;
import javassist.expr.ExprEditor;
import org.scannotation.AnnotationDB;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class Patcher {
    public static Set<String> findMTSPatches() throws URISyntaxException, IOException {
        System.out.println("Finding core patches...");

        AnnotationDB db = new AnnotationDB();
        db.scanArchives(ClassLoader.getSystemResource(Loader.COREPATCHES_JAR));
        return db.getAnnotationIndex().get(SpirePatch.class.getName());
    }

    public static List<Iterable<String>> findPatches(URL[] urls) throws IOException
    {
        System.out.println("Finding patches...");

        // Remove the base game jar from the search path
        URL[] urls_cpy = new URL[urls.length - 1];
        System.arraycopy(urls, 0, urls_cpy, 0, urls_cpy.length);

        List<Iterable<String>> patchSetList = new ArrayList<>();
        for (URL url : urls_cpy) {
            AnnotationDB db = new AnnotationDB();
            db.scanArchives(url);
            patchSetList.add(db.getAnnotationIndex().get(SpirePatch.class.getName()));
        }
        return patchSetList;
    }

    public static void compilePatches(ClassLoader loader, Set<CtClass> ctClasses) throws CannotCompileException
    {
        System.out.println("Compiling patched classes...");
        for (CtClass cls : ctClasses) {
            System.out.println("  " + cls.getName());
            cls.toClass(loader, null);
        }
        System.out.println("Done.");
    }

    public static HashSet<CtClass> injectPatches(ClassLoader loader, ClassPool pool, List<Iterable<String>> class_names) throws CannotCompileException, NotFoundException, ClassNotFoundException, InvocationTargetException, IllegalAccessException
    {
        HashSet<CtClass> ctClasses = new HashSet<>();
        for (Iterable<String> it : class_names) {
            HashSet<CtClass> tmp = injectPatches(loader, pool, it);
            if (tmp != null) {
                ctClasses.addAll(tmp);
            }
        }
        return ctClasses;
    }

    public static HashSet<CtClass> injectPatches(ClassLoader loader, ClassPool pool, Iterable<String> class_names) throws ClassNotFoundException, NotFoundException, CannotCompileException, InvocationTargetException, IllegalAccessException
    {
        if (class_names == null)
            return null;

        HashSet<CtClass> ctClasses = new HashSet<CtClass>();
        for (String cls_name : class_names) {
            System.out.println("Patch [" + cls_name + "]");

            Class<?> patchClass = loader.loadClass(cls_name);
            if (!patchClass.isAnnotationPresent(SpirePatch.class)) {
                JOptionPane.showMessageDialog(null, "Something went wrong finding SpirePatch on [" + cls_name + "].\n" +
                        "Most likely the mod was compiled with a different version of ModTheSpireLib.");
                continue;
            }
            SpirePatch patch = patchClass.getAnnotation(SpirePatch.class);

            CtClass ctClsToPatch = pool.get(patch.cls());
            CtBehavior ctMethodToPatch = null;
            try {
                CtClass[] ctParamTypes = patchParamTypes(pool, patch);
                if (patch.method().equals("ctor")) {
                    if (ctParamTypes == null)
                        ctMethodToPatch = ctClsToPatch.getDeclaredConstructors()[0];
                    else
                        ctMethodToPatch = ctClsToPatch.getDeclaredConstructor(ctParamTypes);
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

            System.out.println("  Patching [" + patch.cls() + "." + patch.method() + "]");

            for (Method m : patchClass.getDeclaredMethods()) {
                if (m.getName().equals("Prefix")) {
                    System.out.println("    Adding Prefix...");
                    addPrefix(ctMethodToPatch, pool.getMethod(patchClass.getName(), m.getName()));
                } else if (m.getName().equals("Postfix")) {
                    System.out.println("    Adding Postfix...");
                    addPostfix(ctMethodToPatch, pool.getMethod(patchClass.getName(), m.getName()));
                } else if (m.getName().equals("Insert")) {
                    SpireInsertPatch insertPatch = m.getAnnotation(SpireInsertPatch.class);
                    if (insertPatch == null) {
                        System.err.println("    ERROR: Insert missing SpireInsertPatch info!");
                    } else if (insertPatch.loc() == -1 && insertPatch.rloc() == -1) {
                        System.err.println("    ERROR: SpireInsertPatch missing line number! Must specify either loc or rloc");
                    } else if (insertPatch.loc() >= 0) {
                        System.out.println("    Adding Insert @ " + insertPatch.loc() + "...");
                        addInsert(insertPatch, insertPatch.loc(), ctMethodToPatch, pool.getMethod(patchClass.getName(), m.getName()));
                    } else {
                        int abs_loc = ctMethodToPatch.getMethodInfo().getLineNumber(0) + insertPatch.rloc();
                        System.out.println("    Adding Insert @ r" + insertPatch.rloc() + " (abs:" + abs_loc + ")...");
                        addInsert(insertPatch, abs_loc, ctMethodToPatch, pool.getMethod(patchClass.getName(), m.getName()));
                    }
                } else if (m.getName().equals("Instrument")) {
                    System.out.println("    Adding Instrument...");
                    addInstrument(ctMethodToPatch, m);
                }
            }

            ctClasses.add(ctClsToPatch);
        }

        return ctClasses;
    }

    private static void addPrefix(CtBehavior ctMethodToPatch, CtMethod prefix) throws CannotCompileException, NotFoundException, ClassNotFoundException
    {
        String src = "{\n";
        String funccall = prefix.getDeclaringClass().getName() + "." + prefix.getName() + "(";
        String postcallsrc = "";

        int paramOffset = (Modifier.isStatic(ctMethodToPatch.getModifiers()) ? 1 : 0);
        CtClass[] prefixParamTypes = prefix.getParameterTypes();
        Object[][] prefixParamAnnotations = prefix.getParameterAnnotations();
        for (int i = 0; i < prefixParamTypes.length; ++i) {
            if (paramByRef(prefixParamAnnotations[i])) {
                src += prefixParamTypes[i].getName() + " __param" + i + " = new " + prefixParamTypes[i].getName() + "{" + "$" + (i + paramOffset) + "};\n";
                funccall += "__param" + i;
                postcallsrc += "$" + (i + paramOffset) + " = __param" + i + "[0];\n";
            } else {
                funccall += "$" + (i + paramOffset);
            }
            if (i < prefixParamTypes.length - 1) {
                funccall += ", ";
            }
        }

        src += funccall + ");\n";
        src += postcallsrc;
        src += "}";

        System.out.println(src);
        if (ctMethodToPatch instanceof CtConstructor) {
            ((CtConstructor)ctMethodToPatch).insertBeforeBody(src);
        } else {
            ctMethodToPatch.insertBefore(src);
        }
    }

    private static void addPostfix(CtBehavior ctMethodToPatch, CtMethod postfix) throws NotFoundException, CannotCompileException {
        CtClass returnType = postfix.getReturnType();
        CtClass[] parameters = postfix.getParameterTypes();

        boolean returnsValue = false;
        boolean takesResultParam = false;

        if (!returnType.equals(CtPrimitiveType.voidType)) {
            returnsValue = true;
            System.out.println("      Return: " + returnType.getName());
        }
        if (parameters.length >= 1 && parameters[0].equals(returnType)) {
            takesResultParam = true;
            System.out.println("      Result param: " + parameters[0].getName());
        }

        String src = postfix.getDeclaringClass().getName() + "." + postfix.getName() + "(";
        if (returnsValue) {
            src = "return ($r)" + src;
        }
        if (takesResultParam) {
            src += "$_";
        }
        if (!Modifier.isStatic(ctMethodToPatch.getModifiers())) {
            if (src.charAt(src.length()-1) != '(') {
                src += ", ";
            }
            src += "$0";
        }
        if (src.charAt(src.length()-1) != '(') {
            src += ", ";
        }
        src += "$$);";
        System.out.println("      " + src);
        ctMethodToPatch.insertAfter(src);
    }

    private static void addInsert(SpireInsertPatch info, int loc, CtBehavior ctMethodToPatch, CtMethod insert) throws CannotCompileException, NotFoundException, ClassNotFoundException
    {
        CtClass[] insertParamTypes = insert.getParameterTypes();
        Object[][] insertParamAnnotations = insert.getParameterAnnotations();
        int insertParamsStartIndex = ctMethodToPatch.getParameterTypes().length;
        if (!Modifier.isStatic(ctMethodToPatch.getModifiers())) {
            insertParamsStartIndex += 1;
        }
        String[] localVarTypeNames = new String[insertParamAnnotations.length - insertParamsStartIndex];
        for (int i = insertParamsStartIndex; i < insertParamAnnotations.length; ++i) {
            if (paramByRef(insertParamAnnotations[i])) {
                if (!insertParamTypes[i].isArray()) {
                    System.out.println("      WARNING: ByRef parameter is not array type");
                } else {
                    localVarTypeNames[i - insertParamsStartIndex] = insertParamTypes[i].getName();
                }
            }
        }

        String src = "{\n";
        // Setup array holders for each local variable
        for (int i = 0; i < info.localvars().length; ++i) {
            if (localVarTypeNames[i] != null) {
                src += localVarTypeNames[i] + " __" + info.localvars()[i] + " = new " + localVarTypeNames[i] + "{" + info.localvars()[i] + "};\n";
            }
        }

        src += insert.getDeclaringClass().getName() + "." + insert.getName() + "(";
        if (!Modifier.isStatic(ctMethodToPatch.getModifiers())) {
            if (src.charAt(src.length()-1) != '(') {
                src += ", ";
            }
            src += "$0";
        }
        if (src.charAt(src.length()-1) != '(') {
            src += ", ";
        }
        src += "$$";
        for (int i = 0; i < info.localvars().length; ++i) {
            src += ", ";
            if (localVarTypeNames[i] != null) {
                src += "__";
            }
            src += info.localvars()[i];
        }
        src += ");\n";

        // Set local variables to changed values
        for (int i = 0; i < info.localvars().length; ++i) {
            if (localVarTypeNames[i] != null) {
                src += info.localvars()[i] + " = __" + info.localvars()[i] + "[0];\n";
            }
        }
        src += "}";
        System.out.println(src);
        ctMethodToPatch.insertAt(loc, src);
    }

    private static void addInstrument(CtBehavior ctMethodToPatch, Method method) throws InvocationTargetException, IllegalAccessException, CannotCompileException
    {
        ExprEditor exprEditor = (ExprEditor)method.invoke(null);
        ctMethodToPatch.instrument(exprEditor);
    }

    private static boolean paramByRef(Object[] annotations) {
        for (Object o : annotations) {
            if (o instanceof ByRef) {
                return true;
            }
        }
        return false;
    }

    private static CtClass[] patchParamTypes(ClassPool pool, SpirePatch patch) throws NotFoundException {
        String[] def = {"DEFAULT"};
        if (Arrays.equals(patch.paramtypes(), def))
            return null;

        return pool.get(patch.paramtypes());
    }
}

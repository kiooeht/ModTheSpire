package com.evacipated.cardcrawl.modthespire;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import javassist.*;
import org.scannotation.AnnotationDB;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Patcher {
    public static void patchCredits(ClassLoader loader, ClassPool pool, String mod_name, String mod_author) throws NotFoundException, CannotCompileException {
        CtClass ctCreditsScreen = pool.get("com.megacrit.cardcrawl.credits.CreditsScreen");
        if (ctCreditsScreen != null) {
            CtConstructor ctConstructor = ctCreditsScreen.getDeclaredConstructors()[0];
            String src = "{" +
                    "this.lines.add(new com.megacrit.cardcrawl.credits.CreditLine(\"ModTheSpire\", tmpY -= 150.0F, true));" +
                    "this.lines.add(new com.megacrit.cardcrawl.credits.CreditLine(\"kiooeht\", tmpY -= 45.0F, false));";
            if (!mod_author.isEmpty()) {
                src += "this.lines.add(new com.megacrit.cardcrawl.credits.CreditLine(\"" + mod_name + " Mod\", tmpY -= 150.0F, true));";
                String[] mod_authors = mod_author.split(",");
                for (String author : mod_authors) {
                    src += "this.lines.add(new com.megacrit.cardcrawl.credits.CreditLine(\"" + author + "\", tmpY -= 45.0F, false));";
                }
            }
            src += "}";
            ctConstructor.insertAt(66, src);
            ctCreditsScreen.toClass(loader, null);
        }
    }

    public static Set<String> findPatches(File jar) throws IOException
    {
        System.out.println("Finding patches...");

        URL[] urls = { jar.toURI().toURL() };
        AnnotationDB db = new AnnotationDB();
        db.scanArchives(urls);
        return db.getAnnotationIndex().get(SpirePatch.class.getName());
    }

    public static void injectPatches(ClassLoader loader, ClassPool pool, Iterable<String> class_names) throws ClassNotFoundException, NotFoundException, CannotCompileException {
        if (class_names == null)
            return;

        HashSet<CtClass> ctClasses = new HashSet<CtClass>();
        for (String cls_name : class_names) {
            System.out.println("Patch [" + cls_name + "]");

            Class<?> patchClass = loader.loadClass(cls_name);
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
                    addPrefix(ctMethodToPatch, m);
                } else if (m.getName().equals("Postfix")) {
                    System.out.println("    Adding Postfix...");
                    addPostfix(ctMethodToPatch, pool.getMethod(patchClass.getName(), m.getName()));
                }
            }

            ctClasses.add(ctClsToPatch);
        }

        System.out.println("Compiling patched classes...");
        for (CtClass cls : ctClasses) {
            System.out.println("  " + cls.getName());
            cls.toClass(loader, null);
        }
    }

    private static void addPrefix(CtBehavior ctMethodToPatch, Method prefix)
    {
        String src = prefix.getDeclaringClass().getName() + "." + prefix.getName() + "(";
        if (!Modifier.isStatic(ctMethodToPatch.getModifiers())) {
            src += "$0";
        }
        if (src.charAt(src.length()-1) != '(') {
            src += ", ";
        }
        src += "$$);";
        System.out.println("      " + src);
        try {
            ctMethodToPatch.insertBefore(src);
        } catch (CannotCompileException e) {
            e.printStackTrace();
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

    private static CtClass[] patchParamTypes(ClassPool pool, SpirePatch patch) throws NotFoundException {
        String[] def = {"DEFAULT"};
        if (Arrays.equals(patch.paramtypes(), def))
            return null;

        return pool.get(patch.paramtypes());
    }
}

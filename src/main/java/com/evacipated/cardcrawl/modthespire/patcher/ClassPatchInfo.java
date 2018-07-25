package com.evacipated.cardcrawl.modthespire.patcher;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.StaticSpireField;
import javassist.*;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassPatchInfo extends PatchInfo
{
    private CtClass ctPatchClass;
    private CtClass ctClassToPatch;

    public ClassPatchInfo(CtClass ctClassToPatch, CtClass ctPatchClass)
    {
        super(null, null);
        this.ctClassToPatch = ctClassToPatch;
        this.ctPatchClass = ctPatchClass;
    }

    @Override
    public void debugPrint()
    {
        System.out.println("Patch Class: [" + patchClassName() + "]");
        System.out.println(" - Patching [" + ctClassToPatch.getName() + "]");
    }

    @Override
    protected String debugMsg()
    {
        return "";
    }

    @Override
    protected String patchClassName()
    {
        return ctPatchClass.getName();
    }

    @Override
    public int patchOrdering()
    {
        return -5;
    }

    @Override
    public void doPatch() throws NotFoundException, CannotCompileException
    {
        for (CtField f : ctPatchClass.getDeclaredFields()) {
            boolean isStatic = f.getType().getName().equals(StaticSpireField.class.getCanonicalName());
            boolean isSpireField = isStatic || f.getType().getName().equals(SpireField.class.getCanonicalName());
            if (isSpireField) {
                // Make the field
                String fieldName = String.format("%s_%d", f.getName(), new Random().nextInt(1000));
                String fieldType = f.getGenericSignature();
                Pattern pattern = Pattern.compile("Lcom/evacipated/cardcrawl/modthespire/lib/SpireField<L(.+);>;");
                Matcher matcher = pattern.matcher(fieldType);
                matcher.find();
                fieldType = matcher.group(1).replace('/', '.');
                if (fieldType.contains("<")) {
                    fieldType = fieldType.substring(0, fieldType.indexOf('<'));
                }
                String str = String.format("public%s %s %s;",
                    (isStatic ? " static" : ""),
                    fieldType, fieldName);
                if (Loader.DEBUG) {
                    System.out.println(" - Adding Field: " + str);
                }
                CtField new_f = CtField.make(str, ctClassToPatch);
                String expr = String.format("(%s) %s.%s.getDefaultValue()", fieldType, ctPatchClass.getName(), f.getName());
                ctClassToPatch.addField(new_f, CtField.Initializer.byExpr(expr));

                // Make and initialize SpireField object
                CtConstructor staticinit = ctPatchClass.getClassInitializer();
                if (staticinit == null) {
                    staticinit = ctPatchClass.makeClassInitializer();
                }
                String src = String.format("{\n" +
                        "if (%s == null) { %s = new %s(null); }\n" +
                        "%s.initialize(%s, \"%s\");\n" +
                        "}",
                    f.getName(), f.getName(), (isStatic ? StaticSpireField.class.getCanonicalName() : SpireField.class.getCanonicalName()),
                    f.getName(), ctClassToPatch.getName() + ".class", fieldName);
                if (Loader.DEBUG) {
                    System.out.println(src);
                }
                staticinit.insertAfter(src);
            }
        }
        if (Loader.DEBUG) {
            System.out.println();
        }
    }
}

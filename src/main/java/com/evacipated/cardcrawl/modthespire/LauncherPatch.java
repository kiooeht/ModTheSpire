package com.evacipated.cardcrawl.modthespire;

import javassist.*;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.NewExpr;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;

class LauncherPatch
{
    static void patch()
    {
        Path mtsLauncherJar = Paths.get("mts-launcher.jar");

        ClassPool pool = new ClassPool(true);
        ClassPath classPath;
        try {
            classPath = pool.appendClassPath(mtsLauncherJar.toAbsolutePath().toString());
        } catch (NotFoundException e) {
            System.out.println("Could not find mts-launcher.jar, skipped.");
            return;
        }

        boolean anyPatchesApplied = false;
        try {
            anyPatchesApplied = v1(pool) || anyPatchesApplied;
            // Call new patches here
        } catch (Err e) {
            System.out.println(e.getMessage() + ", skipped.");
            return;
        }

        if (!anyPatchesApplied) {
            return;
        }

        try (FileSystem fs = FileSystems.newFileSystem(mtsLauncherJar, null)) {
            CtClass main = pool.get("com.megacrit.mtslauncher.Main");
            Path path = fs.getPath("/com/megacrit/mtslauncher/Main.class");
            OutputStream out = Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            main.toBytecode(new DataOutputStream(out));
            // Necessary to close the jar opened by ClassPool so writing can happen
            pool.removeClassPath(classPath);
            out.close();
        } catch (IOException | NotFoundException | CannotCompileException e) {
            e.printStackTrace();
        }
    }

    // Make mts-launcher pass arguments to ModTheSpire
    private static boolean v1(ClassPool pool) throws Err
    {
        try {
            CtClass main = pool.get("com.megacrit.mtslauncher.Main");
            if (hasPatchMarker(main, "v1")) {
                return false;
            }
            System.out.println("Applying mts-launcher.jar patch v1...");
            main.instrument(new ExprEditor() {
                @Override
                public void edit(NewExpr e) throws CannotCompileException
                {
                    if (e.getClassName().equals(ProcessBuilder.class.getName())) {
                        e.replace(
                            "String[] arr = new String[$1.length + args.length];" +
                                "System.arraycopy($1, 0, arr, 0, $1.length);" +
                                "System.arraycopy(args, 0, arr, $1.length, args.length);" +
                                "$_ = $proceed(arr);"
                        );
                    }
                }
            });
            makePatchMarker(main, "v1");
        } catch (NotFoundException | CannotCompileException e) {
            throw new Err("v1", e);
        }
        return true;
    }

    private static boolean hasPatchMarker(CtClass ctClass, String patchName)
    {
        try {
            ctClass.getDeclaredField("$MTS_PATCH_" + patchName, Descriptor.of(CtClass.booleanType));
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    private static void makePatchMarker(CtClass ctClass, String patchName) throws CannotCompileException
    {
        CtField field = new CtField(CtClass.booleanType, "$MTS_PATCH_" + patchName, ctClass);
        field.setModifiers(Modifier.STATIC | Modifier.FINAL);
        ctClass.addField(field, CtField.Initializer.constant(true));
    }

    private static class Err extends Exception
    {
        private final String n;

        Err(String n, String message)
        {
            super(message);
            this.n = n;
        }

        Err(String n, Throwable cause)
        {
            super(cause);
            this.n = n;
        }

        @Override
        public String getMessage()
        {
            return String.format("Patch %s failed: %s", n, super.getMessage());
        }
    }
}

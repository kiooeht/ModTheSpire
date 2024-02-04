package com.evacipated.cardcrawl.modthespire;

import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationImpl;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

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

        Set<CtClass> patchedClasses = new HashSet<>();
        try {
            patchedClasses.addAll(v1(pool));
            // Call new patches here
        } catch (Err e) {
            System.out.println(e.getMessage() + ", skipped.");
            return;
        }

        if (patchedClasses.isEmpty()) {
            return;
        }

        try (FileSystem fs = FileSystems.newFileSystem(mtsLauncherJar, null)) {
            for (CtClass ctClass : patchedClasses) {
                Path path = fs.getPath(ctClass.getName().replace('.', File.separatorChar) + ".class");
                OutputStream out = Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                ctClass.toBytecode(new DataOutputStream(out));
                out.close();
            }
            // Necessary to close the jar opened by ClassPool so writing can happen
            pool.removeClassPath(classPath);
        } catch (IOException | CannotCompileException e) {
            e.printStackTrace();
        }
    }

    private static Collection<CtClass> v1(ClassPool pool) throws Err
    {
        try {
            CtClass ctMain = pool.get("com.megacrit.mtslauncher.Main");
            CtClass ctPatch = pool.get(LauncherPatch.class.getName());

            boolean signatureChanged = false;
            boolean versionChanged = false;
            CtMethod patchRunMTSProcess = ctPatch.getDeclaredMethod("runMTSProcess");
            Version patchVersion = (Version) patchRunMTSProcess.getAnnotation(Version.class);
            try {
                // Check if runMTSProcess method has already been patched in
                CtMethod mainRunMTSProcess = ctMain.getDeclaredMethod("$runMTSProcess");
                if (!patchRunMTSProcess.getSignature().equals(mainRunMTSProcess.getSignature())) {
                    signatureChanged = true;
                }
                Version mainVersion = (Version) mainRunMTSProcess.getAnnotation(Version.class);
                if (patchVersion.value() != mainVersion.value()) {
                    versionChanged = true;
                }
                ctMain.removeMethod(mainRunMTSProcess);
            } catch (NotFoundException ignore) {}
            CtMethod newMethod = CtNewMethod.copy(patchRunMTSProcess, "$runMTSProcess", ctMain, null);
            // Copy Version annotation
            ConstPool constPool = ctMain.getClassFile().getConstPool();
            AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
            if (Proxy.getInvocationHandler(patchVersion) instanceof AnnotationImpl) {
                AnnotationImpl impl = (AnnotationImpl) Proxy.getInvocationHandler(patchVersion);
                Annotation annotation = new Annotation(impl.getTypeName(), constPool);
                if (impl.getAnnotation().getMemberNames() != null) {
                    for (Object memberName : impl.getAnnotation().getMemberNames()) {
                        annotation.addMemberValue((String) memberName, impl.getAnnotation().getMemberValue((String) memberName));
                    }
                }
                attr.addAnnotation(annotation);
            }
            newMethod.getMethodInfo().addAttribute(attr);
            ctMain.addMethod(newMethod);

            final boolean[] startReplaced = {false};
            ctMain.getDeclaredMethod("main").instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException
                {
                    if (m.getClassName().equals("com.megacrit.mtslauncher.Main") && m.getMethodName().equals("$runMTSProcess")) {
                        m.replace("$_ = $runMTSProcess(" + getParameterNames(patchRunMTSProcess) + ");");
                    } else if (m.getClassName().equals(ProcessBuilder.class.getName()) && m.getMethodName().equals("start")) {
                        startReplaced[0] = true;
                        m.replace("$_ = $runMTSProcess(" + getParameterNames(patchRunMTSProcess) + ");");
                    }
                }
            });

            if (signatureChanged || versionChanged || startReplaced[0]) {
                String v = "v1." + patchVersion.value();
                if (startReplaced[0]) {
                    System.out.println("Applying mts-launcher.jar patch " + v + "...");
                } else if (signatureChanged) {
                    System.out.println("Updating mts-launcher.jar patch " + v + " because signature changed...");
                } else {
                    System.out.println("Updating mts-launcher.jar patch " + v + " because version changed...");
                }
                return Collections.singleton(ctMain);
            } else {
                return Collections.emptyList();
            }
        } catch (NotFoundException | CannotCompileException | ClassNotFoundException e) {
            throw new Err("v1", e);
        }
    }

    private static String getParameterNames(CtMethod ctMethod)
    {
        MethodInfo methodInfo = ctMethod.getMethodInfo2();
        LocalVariableAttribute table = (LocalVariableAttribute) methodInfo.getCodeAttribute().getAttribute(LocalVariableAttribute.tag);
        if (table == null) {
            return "";
        }

        List<String> names = new ArrayList<>();
        for (int i=0; i<table.tableLength(); ++i) {
            if (table.startPc(i) == 0) {
                names.add(table.variableName(i));
            }
        }
        return String.join(", ", names);
    }

    @SuppressWarnings("unused")
    private static volatile Logger logger;
    @SuppressWarnings("unused")
    @Version(0) // NOTE: increment this number when making changes to this method
    private static Process runMTSProcess(ProcessBuilder pb, String[] args, String path) throws IOException
    {
        // Make mts-launcher pass arguments to ModTheSpire
        pb.command().addAll(Arrays.asList(args));
        // Make mts-launcher run ModTheSpire with -javaagent
        pb.command().add(1, "-javaagent:" + path);
        logger.info(pb.command().toString());
        Process p = pb.start();
        try {
            if (p.waitFor(1, TimeUnit.SECONDS)) {
                if (p.exitValue() != 0) {
                    logger.warning("Failed to launch ModTheSpire, trying again without javaagent");
                    pb.command().remove(1);
                    logger.info(pb.command().toString());
                    pb.start();
                }
            }
        } catch (InterruptedException ignore) {}
        return p;
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

    @Retention(RetentionPolicy.CLASS)
    private @interface Version
    {
        int value();
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

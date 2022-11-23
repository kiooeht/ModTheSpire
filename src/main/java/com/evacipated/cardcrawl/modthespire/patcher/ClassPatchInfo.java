package com.evacipated.cardcrawl.modthespire.patcher;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpireMethod;
import com.evacipated.cardcrawl.modthespire.lib.StaticSpireField;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationImpl;
import javassist.expr.*;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassPatchInfo extends PatchInfo
{
    private CtClass ctPatchClass;
    private CtClass ctClassToPatch;

    private static final Map<String, CtMethod> methodCache = new HashMap<>();
    private static final Map<String, List<String>> methodBodyCache = new HashMap<>();

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
    public void doPatch() throws PatchingException
    {
        try {
            for (CtField f : ctPatchClass.getDeclaredFields()) {
                boolean isStatic = f.getType().getName().equals(StaticSpireField.class.getCanonicalName());
                boolean isSpireField = isStatic || f.getType().getName().equals(SpireField.class.getCanonicalName());
                if (isSpireField) {
                    int tries = 100;
                    while (tries > 0) {
                        --tries;
                        // Make the field
                        String fieldName = String.format("%s_%d", f.getName(), new Random().nextInt(1000));
                        String fieldType;

                        try {
                            // Determine field type using javassist signature descriptors
                            SignatureAttribute.ObjectType fieldSig = SignatureAttribute.toFieldSignature(f.getGenericSignature());
                            if (fieldSig instanceof SignatureAttribute.ClassType) {
                                SignatureAttribute.TypeArgument[] typeArguments = ((SignatureAttribute.ClassType) fieldSig).getTypeArguments();
                                if (typeArguments == null || typeArguments.length != 1) {
                                    throw new BadBytecode("fake");
                                }
                                String descriptor = typeArguments[0].getType().encode();
                                descriptor = descriptor.replaceAll("<.+>", "");
                                fieldType = Descriptor.toClassName(descriptor);
                            } else {
                                throw new BadBytecode("fake");
                            }
                        } catch (BadBytecode e) {
                            // Fallback to the old method of determining the field type
                            // Regex and string manip the type descriptor
                            fieldType = f.getGenericSignature();
                            Pattern pattern = Pattern.compile("Lcom/evacipated/cardcrawl/modthespire/lib/(?:Static)?SpireField<(\\[?)L(.+);>;");
                            Matcher matcher = pattern.matcher(fieldType);
                            if (!matcher.find()) {
                                if (Loader.DEBUG) {
                                    System.out.println(fieldType);
                                }
                            }
                            boolean isArrayType = !matcher.group(1).isEmpty();
                            fieldType = matcher.group(2).replace('/', '.');
                            if (fieldType.contains("<")) {
                                fieldType = fieldType.substring(0, fieldType.indexOf('<'));
                            }
                            if (isArrayType) {
                                fieldType += "[]";
                            }
                        }

                        String str = String.format("public%s %s %s;",
                            (isStatic ? " static" : ""),
                            fieldType, fieldName);
                        if (Loader.DEBUG) {
                            System.out.println(" - Adding Field: " + str);
                        }
                        CtField new_f = CtField.make(str, ctClassToPatch);

                        // Copy annotations
                        ConstPool constPool = ctClassToPatch.getClassFile().getConstPool();
                        AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                        for (Object a : f.getAvailableAnnotations()) {
                            if (Proxy.getInvocationHandler(a) instanceof AnnotationImpl) {
                                if (Loader.DEBUG) {
                                    System.out.println("   - Copying annotation: " + a);
                                }
                                AnnotationImpl impl = (AnnotationImpl) Proxy.getInvocationHandler(a);
                                Annotation annotation = new Annotation(impl.getTypeName(), constPool);
                                if (impl.getAnnotation().getMemberNames() != null) {
                                    for (Object memberName : impl.getAnnotation().getMemberNames()) {
                                        annotation.addMemberValue((String) memberName, impl.getAnnotation().getMemberValue((String) memberName));
                                    }
                                }
                                attr.addAnnotation(annotation);
                            }
                        }
                        new_f.getFieldInfo().addAttribute(attr);

                        String expr = String.format("(%s) %s.%s.getDefaultValue()", fieldType, ctPatchClass.getName(), f.getName());
                        try {
                            ctClassToPatch.addField(new_f, CtField.Initializer.byExpr(expr));
                        } catch (DuplicateMemberException e) {
                            if (tries == 0) {
                                throw e;
                            }
                            continue;
                        }

                        CtConstructor staticinit = ctPatchClass.getClassInitializer();
                        if (staticinit == null) {
                            staticinit = ctPatchClass.makeClassInitializer();
                        }

                        // Create field accessor to avoid reflection at runtime
                        CtClass ctAccessor = ctPatchClass.makeNestedClass(fieldName + "_Accessor", true);
                        ctAccessor.setSuperclass(f.getType());
                        // Check for any pre-existing initializers for SpireFields
                        FindSpireFieldInitializers found = new FindSpireFieldInitializers(ctPatchClass.getClassPool(), ctAccessor);
                        ctPatchClass.instrument(found);

                        // Finish creating field accessor
                        CtClass ctSpireField = f.getType().getClassPool().get(SpireField.class.getName());
                        ctAccessor.addConstructor(CtNewConstructor.make(
                            new CtClass[]{ctSpireField},
                            null,
                            CtNewConstructor.PASS_PARAMS,
                            null,
                            null,
                            ctAccessor
                        ));
                        // Getter
                        String getStr = "";
                        if (found.madeGet) {
                            getStr = "super_get(__instance);";
                        }
                        ctAccessor.addMethod(CtNewMethod.make(
                            String.format("public Object get(Object __instance) {" +
                                    getStr +
                                    "return ((%s) __instance).%s;" +
                                    "}",
                                ctClassToPatch.getName(), fieldName
                            ),
                            ctAccessor
                        ));
                        // Setter
                        String setStr = "";
                        if (found.madeSet && fieldType.equals(found.setType.getName())) {
                            setStr = String.format("super_set(__instance, (%s) value);", found.setType.getName());
                        }
                        ctAccessor.addMethod(CtNewMethod.make(
                            String.format("public void set(Object __instance, Object value) {" +
                                    "((%s) __instance).%s = (%s) value;" +
                                    setStr +
                                    "}",
                                ctClassToPatch.getName(), fieldName, fieldType
                            ),
                            ctAccessor
                        ));

                        // Make and initialize SpireField object
                        String src = String.format("{\n" +
                                "%s = new %s(%s);" +
                                "%s.initialize(%s, \"%s\");\n" +
                                "}",
                            f.getName(), ctAccessor.getName(), f.getName(),
                            f.getName(), ctClassToPatch.getName() + ".class", fieldName);
                        if (Loader.DEBUG) {
                            System.out.println(src);
                        }
                        staticinit.insertAfter(src);

                        break;
                    }
                }
            }
            for (CtMethod m : ctPatchClass.getDeclaredMethods()) {
                if (m.hasAnnotation(SpireMethod.class)) {
                    CtClass[] params = m.getParameterTypes();
                    SpireMethod anno = (SpireMethod) m.getAnnotation(SpireMethod.class);
                    boolean hasReturnValue = !anno.returnType().equals(void.class);
                    if (Loader.DEBUG) {
                        System.out.println(" - Adding Method: " + ctClassToPatch.getName() + "." + m.getName() + " " + m.getSignature());
                        System.out.print("   Params: ");
                        for (CtClass param : params) {
                            System.out.print(param.getSimpleName() + ", ");
                        }
                        System.out.println();
                    }
                    CtClass returnType;
                    if (anno.returnType().isPrimitive()) {
                        switch (anno.returnType().getName()) {
                            case "int":
                                returnType = CtPrimitiveType.intType;
                                break;
                            case "float":
                                returnType = CtPrimitiveType.floatType;
                                break;
                            case "double":
                                returnType = CtPrimitiveType.doubleType;
                                break;
                            case "long":
                                returnType = CtPrimitiveType.longType;
                                break;
                            case "char":
                                returnType = CtPrimitiveType.charType;
                                break;
                            case "byte":
                                returnType = CtPrimitiveType.byteType;
                                break;
                            case "short":
                                returnType = CtPrimitiveType.shortType;
                                break;
                            case "boolean":
                                returnType = CtPrimitiveType.booleanType;
                                break;
                            case "void":
                                returnType = CtPrimitiveType.voidType;
                                break;
                            default:
                                throw new CannotCompileException("Unknown primitive type: " + anno.returnType().getName());
                        }
                    } else {
                        returnType = ctClassToPatch.getClassPool().getCtClass(anno.returnType().getName());
                    }
                    CtMethod newMethod = new CtMethod(returnType, m.getName(), Arrays.copyOfRange(params, hasReturnValue ? 2 : 1, params.length), ctClassToPatch);
                    String methodBody = ctPatchClass.getName() + "." + m.getName() + "($0%s" + "$$);";
                    if (hasReturnValue) {
                        methodBody = String.format(methodBody, ",retVal,");
                    } else {
                        methodBody = String.format(methodBody, ",");
                    }
                    try {
                        ctClassToPatch.addMethod(newMethod);
                        List<String> src = new ArrayList<>();
                        src.add("java.lang.Object[] retVal = new java.lang.Object[1];");
                        src.add(methodBody);
                        src.add(String.format("return%s", hasReturnValue ? " ($r) retVal[0];" : ";"));
                        methodBodyCache.put(Descriptor.ofMethod(newMethod.getReturnType(), newMethod.getParameterTypes()), src);
                        methodCache.put(Descriptor.ofMethod(newMethod.getReturnType(), newMethod.getParameterTypes()), newMethod);
                        StringBuilder tmp = new StringBuilder();
                        tmp.append("{");
                        for (String s : src) {
                            tmp.append(s).append("\n");
                        }
                        tmp.append("}");
                        newMethod.setBody(tmp.toString());

                        ctClassToPatch.setModifiers(ctClassToPatch.getModifiers() | Modifier.PUBLIC);
                        if ((ctClassToPatch.getModifiers() & Modifier.ABSTRACT) > 0) {
                            ctClassToPatch.setModifiers(ctClassToPatch.getModifiers() & ~Modifier.ABSTRACT);
                        }
                    } catch (DuplicateMemberException duplicateMemberException) {
                        newMethod = methodCache.get(Descriptor.ofMethod(newMethod.getReturnType(), Arrays.copyOfRange(params, hasReturnValue ? 2 : 1, params.length)));
                        List<String> src = methodBodyCache.get(Descriptor.ofMethod(newMethod.getReturnType(), Arrays.copyOfRange(params, hasReturnValue ? 2 : 1, params.length)));
                        if (SpireMethod.POSTFIX.equals(anno.onConflict())) {
                            src.add(src.size() - 1, methodBody);
                        } else {
                            src.add(1, methodBody);
                        }
                        StringBuilder tmp = new StringBuilder();
                        tmp.append("{");
                        for (String s : src) {
                            tmp.append(s).append("\n");
                        }
                        tmp.append("}");
                        newMethod.setBody(tmp.toString());
                    }
                }
            }
            if (Loader.DEBUG) {
                System.out.println();
            }
        } catch (CannotCompileException | NotFoundException | ClassNotFoundException e) {
            throw new PatchingException(e);
        }
    }

    private static class FindSpireFieldInitializers extends ExprEditor
    {
        private ClassPool pool;
        private CtClass ctSpireField;
        private CtClass ctStaticSpireField;
        private CtClass ctAccessor;

        boolean madeGet = false;
        boolean madeSet = false;
        CtClass setType = null;

        FindSpireFieldInitializers(ClassPool pool, CtClass ctAccessor) throws NotFoundException
        {
            this.pool = pool;
            ctSpireField = pool.get(SpireField.class.getName());
            ctStaticSpireField = pool.get(StaticSpireField.class.getName());
            this.ctAccessor = ctAccessor;
        }

        @Override
        public void edit(NewExpr e) throws CannotCompileException
        {
            if (e.getClassName().endsWith("_Accessor")) {
                return;
            }

            try {
                CtClass ctOriginal = pool.get(e.getClassName());
                CtClass ctClass = ctOriginal;

                do {
                    if (ctSpireField.equals(ctClass) || ctStaticSpireField.equals(ctClass)) {
                        if (!ctOriginal.equals(ctClass)) {
                            scrubSuperMethodCalls(ctOriginal, "get");
                            scrubSuperMethodCalls(ctOriginal, "set");
                        }
                        break;
                    }
                    ctClass = ctClass.getSuperclass();
                } while (ctClass != null);
            } catch (NotFoundException ignored) {
            }
        }

        private void scrubSuperMethodCalls(CtClass ctClass, String methodName) throws NotFoundException, CannotCompileException
        {
            for (CtMethod m : ctClass.getDeclaredMethods(methodName)) {
                try {
                    ctAccessor.getDeclaredMethod("super_" + methodName);
                } catch (NotFoundException e) {
                    CtMethod newMethod = CtNewMethod.copy(m, "super_" + methodName, ctAccessor, null);
                    // Remove calls to super.get/set
                    newMethod.instrument(new ExprEditor() {
                        @Override
                        public void edit(MethodCall m) throws CannotCompileException
                        {
                            CtMethod method;
                            try {
                                method = m.getMethod();

                                if (method.getName().equals(methodName) && method.getDeclaringClass().equals(ctClass.getSuperclass())) {
                                    m.replace("$_ = null;");
                                }
                            } catch (NotFoundException e) {
                                throw new CannotCompileException(e);
                            }
                        }
                    });
                    ctAccessor.addMethod(newMethod);

                    switch (methodName) {
                        case "get":
                            madeGet = true;
                            break;
                        case "set":
                            madeSet = true;
                            setType = newMethod.getParameterTypes()[1];
                            break;
                    }
                }
            }
        }
    }
}

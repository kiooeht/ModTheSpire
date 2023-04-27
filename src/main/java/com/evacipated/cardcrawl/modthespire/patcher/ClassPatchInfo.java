package com.evacipated.cardcrawl.modthespire.patcher;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpireMethod;
import com.evacipated.cardcrawl.modthespire.lib.StaticSpireField;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.SignatureAttribute.ClassSignature;
import javassist.bytecode.SignatureAttribute.ClassType;
import javassist.bytecode.SignatureAttribute.TypeArgument;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationImpl;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

import java.lang.reflect.Proxy;
import java.util.Arrays;
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
    public void doPatch() throws PatchingException
    {
        try {
            patchSpireFields();
            patchSpireMethods();
        } catch (CannotCompileException | NotFoundException | ClassNotFoundException e) {
            throw new PatchingException(e);
        }
    }

    private void patchSpireFields() throws CannotCompileException, NotFoundException
    {
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
        if (Loader.DEBUG) {
            System.out.println();
        }
    }

    private void patchSpireMethods() throws NotFoundException, ClassNotFoundException, SpireMethodException, CannotCompileException
    {
        for (CtMethod m : ctPatchClass.getDeclaredMethods()) {
            if (!m.hasAnnotation(SpireMethod.class)) {
                continue;
            }

            m.setModifiers(Modifier.setPublic(m.getModifiers()));

            SpireMethod spireMethod = (SpireMethod) m.getAnnotation(SpireMethod.class);
            CtClass ctSpireMethodHelper = ctPatchClass.getClassPool().get(SpireMethod.Helper.class.getName());
            String methodName = m.getName();
            boolean hasReturn = !m.getReturnType().equals(CtClass.voidType);
            CtClass[] patchParamTypes = m.getParameterTypes();
            if (patchParamTypes.length < 1 || !patchParamTypes[0].equals(ctSpireMethodHelper)) {
                throw new SpireMethodException("missing SpireMethod.Helper as first parameter: " + m.getName());
            }
            CtClass[] realParamTypes = Arrays.copyOfRange(patchParamTypes, 1, patchParamTypes.length);
            CtClass ctFromClass = ctPatchClass.getClassPool().get(spireMethod.from().getName());

            if (!ctClassToPatch.subtypeOf(ctFromClass)) {
                if (ctFromClass.isInterface()) {
                    // Add interface to class
                    ctClassToPatch.addInterface(ctFromClass);
                } else {
                    throw new SpireMethodException("from: %s does not extend %s", ctClassToPatch.getName(), ctFromClass.getName());
                }
            }

            CtMethod superMethod;
            try {
                superMethod = ctFromClass.getDeclaredMethod(methodName, realParamTypes);
            } catch (NotFoundException e) {
                StringBuilder params = new StringBuilder("(");
                for (CtClass paramType : realParamTypes) {
                    params.append(paramType.getName()).append(", ");
                }
                if (realParamTypes.length > 1) {
                    params.setLength(params.length() - 2);
                }
                params.append(')');
                throw new SpireMethodException("from: %s does not contain method %s%s", ctFromClass.getName(), methodName, params);
            }
            if (!superMethod.getReturnType().equals(m.getReturnType())) {
                throw new SpireMethodException("Return types do not match: has %s, expects %s", m.getReturnType().getName(), superMethod.getReturnType().getName());
            }

            CtMethod newMethod;
            try {
                // Try to get the method...
                newMethod = ctClassToPatch.getDeclaredMethod(methodName, realParamTypes);
            } catch (NotFoundException ignored) {
                // ...if it doesn't exist, create it
                newMethod = CtNewMethod.delegator(superMethod, ctClassToPatch);
                if (Modifier.isAbstract(superMethod.getModifiers())) {
                    newMethod.setModifiers(newMethod.getModifiers() & ~Modifier.ABSTRACT);
                }
                ctClassToPatch.addMethod(newMethod);

                // Create proxy for calling super method
                CtMethod proxySuper = CtNewMethod.delegator(superMethod, ctClassToPatch);
                proxySuper.setModifiers(Modifier.setPackage(proxySuper.getModifiers()));
                proxySuper.setName("super$" + proxySuper.getName());
                proxySuper.getMethodInfo().addAttribute(new SyntheticAttribute(proxySuper.getMethodInfo().getConstPool()));
                ctClassToPatch.addMethod(proxySuper);

                // Create SpireMethod.Helper impl class
                CtClass ctHelperImpl = ctClassToPatch.makeNestedClass(methodName + "_HelperImpl", true);
                ctHelperImpl.setModifiers(Modifier.setPrivate(ctHelperImpl.getModifiers()));
                // Add interface to impl class and set its generic signature
                ctHelperImpl.addInterface(ctSpireMethodHelper);
                CtClass superReturnType = superMethod.getReturnType();
                String superReturnTypeName = superReturnType.getName();
                if (superReturnType.isPrimitive()) {
                    superReturnTypeName = ((CtPrimitiveType) superReturnType).getWrapperName();
                }
                ClassSignature cs = new ClassSignature(
                    null,
                    null,
                    new ClassType[]{
                        new ClassType(
                            ctSpireMethodHelper.getName(),
                            new TypeArgument[]{
                                new TypeArgument(new ClassType(ctClassToPatch.getName())),
                                new TypeArgument(new ClassType(superReturnTypeName))
                            }
                        )
                    }
                );
                ctHelperImpl.setGenericSignature(cs.encode());
                // Create field for instance
                CtField ctInstanceField = CtField.make("private " + ctClassToPatch.getName() + " _instance;", ctHelperImpl);
                ctHelperImpl.addField(ctInstanceField);
                // Create field for timesSuperCalled tracking
                CtField ctSuperCalledField = CtField.make("private int _timesSuperCalled = 0;", ctHelperImpl);
                ctHelperImpl.addField(ctSuperCalledField);
                // Create field for hasResult
                CtField ctHasResultField = CtField.make("private boolean _hasResult = false;", ctHelperImpl);
                ctHelperImpl.addField(ctHasResultField);
                // Create field for result
                CtField ctResultField = CtField.make("private " + superReturnType.getName() + " _result;", ctHelperImpl);
                ctHelperImpl.addField(ctResultField);
                // Create constructor
                ctHelperImpl.addConstructor(CtNewConstructor.make(
                    new CtClass[]{ ctClassToPatch },
                    null,
                    "{ _instance = $1; }",
                    ctHelperImpl
                ));
                // Create timesSuperCalled method
                CtMethod ctTimesSuperCalled = CtNewMethod.delegator(ctSpireMethodHelper.getDeclaredMethod("timesSuperCalled"), ctHelperImpl);
                ctTimesSuperCalled.setBody("return _timesSuperCalled;");
                ctHelperImpl.addMethod(ctTimesSuperCalled);
                // Create instance method
                CtMethod ctInstance = CtNewMethod.delegator(ctSpireMethodHelper.getDeclaredMethod("instance"), ctHelperImpl);
                ctInstance.setBody("return _instance;");
                ctHelperImpl.addMethod(ctInstance);
                // Create hasResult method
                CtMethod ctHasResult = CtNewMethod.delegator(ctSpireMethodHelper.getDeclaredMethod("hasResult"), ctHelperImpl);
                ctHasResult.setBody("return _hasResult;");
                ctHelperImpl.addMethod(ctHasResult);
                // Create result method
                CtMethod ctResult = CtNewMethod.delegator(ctSpireMethodHelper.getDeclaredMethod("result"), ctHelperImpl);
                ctResult.setBody("return ($w) _result;");
                ctHelperImpl.addMethod(ctResult);
                // Create storeResult method
                CtMethod ctStoreResult = CtNewMethod.make("void storeResult(" + superReturnType.getName() + " result) {" +
                    "_hasResult = true;" +
                    "_result = result;" +
                    "}", ctHelperImpl);
                ctHelperImpl.addMethod(ctStoreResult);
                // Create callSuper method
                CtMethod ctCallSuper = CtNewMethod.delegator(ctSpireMethodHelper.getDeclaredMethod("callSuper"), ctHelperImpl);
                StringBuilder src = new StringBuilder("{" +
                    "++_timesSuperCalled;" +
                    "return ($w) _instance.super$" + superMethod.getName() + "(");
                for (int i = 0; i < realParamTypes.length; i++) {
                    src.append("((");
                    if (realParamTypes[i].isPrimitive()) {
                        src.append(((CtPrimitiveType) realParamTypes[i]).getWrapperName());
                    } else {
                        src.append(realParamTypes[i].getName());
                    }
                    src.append(") $1[").append(i).append("])");
                    if (realParamTypes[i].isPrimitive()) {
                        src.append('.').append(((CtPrimitiveType) realParamTypes[i]).getGetMethodName()).append("()");
                    }
                    src.append(", ");
                }
                if (realParamTypes.length > 0) {
                    src.setLength(src.length() - 2); // remove trailing ", "
                }
                src.append(");}");
                if (Loader.DEBUG) {
                    System.out.println(src);
                }
                ctCallSuper.setBody(src.toString());
                ctHelperImpl.addMethod(ctCallSuper);
                // Instantiate HelperImpl in method
                newMethod.setBody(null);
                newMethod.addLocalVariable("helperImpl", ctHelperImpl);
                newMethod.insertBefore(ctHelperImpl.getName() + " helperImpl = new " + ctHelperImpl.getName() + "(this);");
            }

            StringBuilder src = new StringBuilder();
            if (hasReturn) {
                src.append("$_ = ");
            }
            src.append(ctPatchClass.getName());
            src.append('.').append(m.getName());
            src.append('(');
            src.append("helperImpl, $$);");
            if (hasReturn) {
                src.append("\nhelperImpl.storeResult($_);");
            }
            if (Loader.DEBUG) {
                System.out.println(src);
            }
            newMethod.insertAfter(src.toString());
            fixHelperImplLocalVariable(newMethod);
        }
        if (Loader.DEBUG) {
            System.out.println();
        }
    }

    // This stops luyten from failing to decompile the newly added method
    private static void fixHelperImplLocalVariable(CtMethod m)
    {
        ConstPool cp = m.getMethodInfo().getConstPool();
        CodeAttribute code = m.getMethodInfo().getCodeAttribute();
        LocalVariableAttribute oldLocals = (LocalVariableAttribute) code.getAttribute(LocalVariableAttribute.tag);
        LocalVariableAttribute newLocals = new LocalVariableAttribute(cp);
        for (int i=0; i<oldLocals.tableLength(); ++i) {
            int codeLength = oldLocals.codeLength(i);
            if ("helperImpl".equals(oldLocals.variableName(i))) {
                codeLength = code.getCodeLength();
            }
            newLocals.addEntry(oldLocals.startPc(i), codeLength, oldLocals.nameIndex(i), oldLocals.descriptorIndex(i), oldLocals.index(i));
        }
        code.getAttributes().removeIf(x -> x instanceof LocalVariableAttribute);
        code.getAttributes().add(newLocals);
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

package com.evacipated.cardcrawl.modthespire.agent;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModTheSpire;
import de.icongmbh.oss.maven.plugin.javassist.ClassTransformer;
import javassist.*;
import javassist.build.JavassistBuildException;
import javassist.bytecode.*;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.Objects;

public class DeprecateLoaderPlugin extends ClassTransformer
{

    @Override
    public boolean shouldTransform(CtClass ctClass) throws JavassistBuildException
    {
        return Objects.equals(ctClass.getName(), Loader.class.getName());
    }

    @Override
    public void applyTransformations(CtClass ctClass) throws JavassistBuildException
    {
        try {
            ClassPool pool = ctClass.getClassPool();
            CtClass ctModTheSpire = pool.get(ModTheSpire.class.getName());

            // Remove line numbers from auto-generated empty constructor
            for (CtConstructor ctor : ctClass.getDeclaredConstructors()) {
                CodeAttribute code = ctor.getMethodInfo().getCodeAttribute();
                List<AttributeInfo> codeAttributes = code.getAttributes();
                codeAttributes.removeIf(attr -> LineNumberAttribute.tag.equals(attr.getName()));
            }

            int curLn = 1;
            // Create static initializer
            {
                CtConstructor clinit = ctClass.makeClassInitializer();
                clinit.setBody("throw new " + NotImplementedException.class.getName() + "(DEPRECATED_MESSAGE);");
                CodeAttribute code = clinit.getMethodInfo().getCodeAttribute();
                code.getAttributes().add(makeLineNumberAttribute(clinit.getMethodInfo().getConstPool(), curLn++));
            }
            // Copy public fields
            for (CtField field : ctModTheSpire.getDeclaredFields()) {
                if (Modifier.isPublic(field.getModifiers())) {
                    CtField newField = new CtField(field, ctClass);
                    ctClass.addField(newField);
                }
            }
            // Copy public methods
            for (CtMethod method : ctModTheSpire.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    CtMethod newMethod = CtNewMethod.copy(method, ctClass, null);
                    if (method.getGenericSignature() != null) {
                        newMethod.setGenericSignature(method.getGenericSignature());
                    }
                    newMethod.setBody("throw new " + NotImplementedException.class.getName() + "(DEPRECATED_MESSAGE);");
                    CodeAttribute code = newMethod.getMethodInfo().getCodeAttribute();
                    code.getAttributes().add(makeLineNumberAttribute(newMethod.getMethodInfo().getConstPool(), curLn++));
                    ctClass.addMethod(newMethod);
                }
            }
        } catch (NotFoundException | CannotCompileException e) {
            e.printStackTrace();
            throw new JavassistBuildException(e);
        }
    }

    private static AttributeInfo makeLineNumberAttribute(ConstPool cp, int startLn)
    {
        final int[] lines = new int[] {0};
        byte[] data = new byte[(lines.length * 4) + 2];

        int idx = 0;
        ByteArray.write16bit(lines.length, data, 0);
        idx += 2;

        for (int i=0; i<lines.length; ++i) {
            ByteArray.write16bit(lines[i], data, idx);
            idx += 2;
            ByteArray.write16bit(startLn + i, data, idx);
            idx += 2;
        }

        return new AttributeInfo(cp, LineNumberAttribute.tag, data);
    }
}

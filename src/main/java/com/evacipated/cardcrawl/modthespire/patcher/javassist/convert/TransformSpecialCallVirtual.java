package com.evacipated.cardcrawl.modthespire.patcher.javassist.convert;

import com.evacipated.cardcrawl.modthespire.Loader;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.*;
import javassist.convert.Transformer;

public class TransformSpecialCallVirtual extends Transformer
{
    protected String classname, methodname, methodDescriptor;
    protected CodeAttribute codeAttr;

    /* cache */
    protected int newIndex;
    protected ConstPool constPool;

    public TransformSpecialCallVirtual(Transformer next, CtMethod origMethod)
    {
        super(next);
        methodname = origMethod.getName();
        methodDescriptor = origMethod.getMethodInfo2().getDescriptor();
        classname = origMethod.getDeclaringClass().getName();
        constPool = null;
    }

    @Override
    public void initialize(ConstPool cp, CodeAttribute attr) {
        if (constPool != cp) {
            newIndex = 0;
        }
        codeAttr = attr;
    }

    /**
     * Modify INVOKESPECIAL to use INVOKEVIRTUAL.
     */
    @Override
    public int transform(CtClass clazz, int pos, CodeIterator iterator,
                         ConstPool cp) throws BadBytecode
    {
        int c = iterator.byteAt(pos);
        if (c == INVOKESPECIAL) {
            int index = iterator.u16bitAt(pos + 1);
            String cname = cp.eqMember(methodname, methodDescriptor, index);
            if (cname != null && matchClass(cname, clazz.getClassPool())) {
                int ntinfo = cp.getMemberNameAndType(index);
                pos = match(c, pos, iterator, cp.getNameAndTypeDescriptor(ntinfo), cp);
            }
        }

        return pos;
    }

    private boolean matchClass(String name, ClassPool pool) {
        if (classname.equals(name))
            return true;

        try {
            CtClass clazz = pool.get(name);
            CtClass declClazz = pool.get(classname);
            if (clazz.subtypeOf(declClazz))
                try {
                    CtMethod m = clazz.getMethod(methodname, methodDescriptor);
                    return m.getDeclaringClass().getName().equals(classname);
                }
                catch (NotFoundException e) {
                    // maybe the original method has been removed.
                    return true;
                }
        }
        catch (NotFoundException e) {
            return false;
        }

        return false;
    }

    protected int match(int c, int pos, CodeIterator iterator,
                        int typedesc, ConstPool cp) throws BadBytecode
    {
        LineNumberAttribute ainfo = (LineNumberAttribute) codeAttr.getAttribute(LineNumberAttribute.tag);

        int nt = cp.addNameAndTypeInfo(cp.addUtf8Info(methodname), typedesc);
        int ci = cp.addClassInfo(classname);
        if (c == INVOKEINTERFACE) {
            newIndex = cp.addInterfaceMethodrefInfo(ci, nt);
        } else {
            if (c == INVOKESPECIAL) {
                if (Loader.DEBUG) {
                    System.out.println("        @ " + ainfo.toLineNumber(pos));
                }
                iterator.writeByte(INVOKEVIRTUAL, pos);
            }

            newIndex = cp.addMethodrefInfo(ci, nt);
        }

        constPool = cp;

        iterator.write16bit(newIndex, pos + 1);
        return pos;
    }
}

package com.evacipated.cardcrawl.modthespire.patcher.javassist.convert;

import javassist.CtClass;
import javassist.bytecode.*;
import javassist.convert.Transformer;

public class TransformInsertGoto extends Transformer
{
    protected CodeAttribute codeAttr;
    protected int fromLoc;
    protected int toLoc;

    protected int locals;
    protected int maxLocals;

    public TransformInsertGoto(Transformer next, int fromLoc, int toLoc)
    {
        super(next);
        this.fromLoc = fromLoc;
        this.toLoc = toLoc;
        locals = 0;
        maxLocals = 0;
    }

    @Override
    public void initialize(ConstPool cp, CodeAttribute attr)
    {
        super.initialize(cp, attr);
        codeAttr = attr;
    }

    @Override
    public int transform(CtClass clazz, int pos, CodeIterator iterator,
                         ConstPool cp) throws BadBytecode
    {
        LineNumberAttribute ainfo = (LineNumberAttribute) codeAttr.getAttribute(LineNumberAttribute.tag);
        int line = ainfo.toLineNumber(pos);

        if (line >= fromLoc && line < toLoc) {
            iterator.move(pos);
            iterator.writeByte(Opcode.NOP, pos);

            return iterator.next();
        }

        return pos;
    }
}

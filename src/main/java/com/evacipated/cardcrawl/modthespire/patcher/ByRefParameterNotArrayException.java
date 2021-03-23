package com.evacipated.cardcrawl.modthespire.patcher;

public class ByRefParameterNotArrayException extends PatchingException
{
    public ByRefParameterNotArrayException(int index)
    {
        super(Integer.toString(index));
    }

    public ByRefParameterNotArrayException(String name)
    {
        super(name);
    }
}

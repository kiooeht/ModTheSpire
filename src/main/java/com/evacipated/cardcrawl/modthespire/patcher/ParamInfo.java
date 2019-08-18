package com.evacipated.cardcrawl.modthespire.patcher;

class ParamInfo
{
    private int position;
    private String name;
    private boolean isPrivate = false;

    ParamInfo(int position, String name)
    {
        this.position = position;
        if (name.startsWith("___")) {
            isPrivate = true;
            name = name.replaceFirst("___", "");
        }
        this.name = name;
    }

    boolean isPrivateCapture()
    {
        return isPrivate;
    }

    int getPosition()
    {
        return position;
    }

    String getName()
    {
        return name;
    }
}

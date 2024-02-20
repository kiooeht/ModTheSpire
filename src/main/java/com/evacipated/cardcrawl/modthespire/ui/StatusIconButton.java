package com.evacipated.cardcrawl.modthespire.ui;

import javax.swing.*;

class StatusIconButton extends JButton
{
    private boolean isHovered = false;

    StatusIconButton(Icon icon)
    {
        this(icon, null);
    }

    StatusIconButton(Icon icon, Icon hoverIcon)
    {
        super(icon);
        setRolloverIcon(hoverIcon);
    }

    public boolean hasHoverIcon()
    {
        return getRolloverIcon() != null;
    }

    public void setHovered(boolean hovered)
    {
        if (hovered != isHovered && hasHoverIcon()) {
            // Swap icon and rollover icon
            Icon tmp = getIcon();
            setIcon(getRolloverIcon());
            setRolloverIcon(tmp);
            isHovered = hovered;
        }
    }

    public boolean isHovered()
    {
        return isHovered;
    }
}

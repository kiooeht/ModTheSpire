package com.evacipated.cardcrawl.modthespire.ui;

import com.formdev.flatlaf.icons.FlatAbstractIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

class FlatRenameIcon extends FlatAbstractIcon
{
    public FlatRenameIcon()
    {
        super(16, 16, null);
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g)
    {
        paintPencil(c, g);
    }

    private void paintPencil(Component c, Graphics2D g)
    {
        Path2D.Float path = new Path2D.Float(Path2D.WIND_NON_ZERO, 5);

        // Pencil body
        path.moveTo(2f, 14f);
        path.lineTo(3f, 11f);
        path.lineTo(10f, 4f);
        path.lineTo(12f, 6f);
        path.lineTo(5f, 13f);
        path.closePath();

        // "Eraser"
        path.moveTo(13f, 1f);
        path.lineTo(15f, 3f);
        path.lineTo(13f, 5f);
        path.lineTo(11f, 3f);
        path.closePath();

        g.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (isEnabled(c)) {
            g.setColor(UIManager.getColor("Button.foreground"));
        } else {
            g.setColor(UIManager.getColor("Button.disabledText"));
        }
        g.draw(path);
    }

    private boolean isEnabled(Component c)
    {
        return c instanceof AbstractButton && ((AbstractButton) c).isEnabled();
    }
}

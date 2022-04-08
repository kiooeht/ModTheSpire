package com.evacipated.cardcrawl.modthespire.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class TextFieldWithPlaceholder extends JTextField {
    private String placeholder;

    public TextFieldWithPlaceholder() {
        this.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                TextFieldWithPlaceholder.this.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                TextFieldWithPlaceholder.this.repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if(getText().isEmpty() && FocusManager.getCurrentKeyboardFocusManager().getFocusOwner() != this){
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Color.gray);
            g2.setFont(getFont().deriveFont(Font.ITALIC));
            g2.drawString(placeholder, 5, getHeight() - 10);
            g2.dispose();
        }
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        this.revalidate();
        this.repaint();
    }
}

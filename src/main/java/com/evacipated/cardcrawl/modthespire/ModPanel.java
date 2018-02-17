package com.evacipated.cardcrawl.modthespire;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

@SuppressWarnings("serial")
public class ModPanel extends JPanel {
    private static final Color lightRed = new Color(229,115,115);
    public ModInfo info;
    public File modFile;
    public JCheckBox checkBox;
    private InfoPanel infoPanel;
    
    public ModPanel(ModInfo info, File modFile) {
        this.info = info;
        this.modFile = modFile;
        this.checkBox = new JCheckBox();
        this.setLayout(new BorderLayout());
        infoPanel = new InfoPanel();
        this.add(infoPanel, BorderLayout.CENTER);

        if (info.MTS_Version.compareTo(Loader.MTS_VERSION) > 0) {
            checkBox.setEnabled(false);
            checkBox.setBackground(lightRed);
            infoPanel.setBackground(lightRed);
            setToolTipText("This mod requires ModTheSpire v" + info.MTS_Version.get() + " or higher.");
        }
    }
    
    public class InfoPanel extends JPanel {
        JPanel buttonPanel;

        public InfoPanel() {
            this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            
            buttonPanel = buildButtonPanel(info, checkBox);
            buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.add(buttonPanel);
            
            if (info.Description != null && !info.Description.equals("")) {
                JLabel description = new JLabel(info.Description);
                description.setFont(description.getFont().deriveFont(Font.PLAIN));
                description.setAlignmentX(Component.LEFT_ALIGNMENT);
                this.add(description);
            }

            if (info.Author != null && !info.Author.equals("")) {
                JLabel author = new JLabel("Author: " + info.Author);
                author.setAlignmentX(Component.LEFT_ALIGNMENT);
                this.add(author);
            }

            this.setBorder(new MatteBorder(0, 0, 1, 0, Color.darkGray));
        }
        
        public JPanel buildButtonPanel(ModInfo info, JCheckBox box) {
            JPanel buttonPanel = new JPanel(new BorderLayout());
            String nameString = ((info.Name != null) ? info.Name : "");
            
            JLabel name = new JLabel(nameString, JLabel.LEFT);
            name.setFont(name.getFont().deriveFont(14.0F));
            
            buttonPanel.add(name, BorderLayout.WEST);
            buttonPanel.add(box, BorderLayout.EAST);
            return buttonPanel;
        }

        @Override
        public void setBackground(Color c)
        {
            super.setBackground(c);
            if (buttonPanel != null) {
                buttonPanel.setBackground(c);
            }
        }
    }
    
}

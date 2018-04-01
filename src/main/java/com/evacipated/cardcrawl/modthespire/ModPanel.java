package com.evacipated.cardcrawl.modthespire;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

import javax.swing.*;
import javax.swing.border.MatteBorder;

@SuppressWarnings("serial")
public class ModPanel extends JPanel {
    private static final Color lightRed = new Color(229,115,115);
    private static final Color lightYellow = new Color(255, 238, 88);
    public ModInfo info;
    public File modFile;
    public JCheckBox checkBox;
    private InfoPanel infoPanel;
    
    public ModPanel(ModInfo info, File modFile, Dimension parentSize) {
        this.info = info;
        this.modFile = modFile;
        this.checkBox = new JCheckBox();
        this.setLayout(new BorderLayout());
        infoPanel = new InfoPanel(parentSize);
        this.add(infoPanel, BorderLayout.CENTER);

        if (info.MTS_Version.compareTo(Loader.MTS_VERSION) > 0) {
            checkBox.setEnabled(false);
            checkBox.setBackground(lightRed);
            infoPanel.setBackground(lightRed);
            setToolTipText("This mod requires ModTheSpire v" + info.MTS_Version.get() + " or higher.");
        } else if (Loader.STS_VERSION != null && info.STS_Version != null && !Loader.STS_VERSION.equals(info.STS_Version)) {
            checkBox.setBackground(lightYellow);
            infoPanel.setBackground(lightYellow);
            setToolTipText("<html>This mod explicitly supports StS " + info.STS_Version + ".<br/>" +
                "You are running StS " + Loader.STS_VERSION + ".<br/>" +
                "You may encounter problems running it.</html>");
        }
    }

    public boolean isSelected()
    {
        return checkBox.isEnabled() && checkBox.isSelected();
    }
    
    public class InfoPanel extends JPanel {
        JPanel buttonPanel;
        JTextArea description;
        JTextArea author;

        public InfoPanel(Dimension parentSize) {
            this.setLayout(new BorderLayout());
            
            buttonPanel = buildButtonPanel(info, checkBox);
            buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.add(buttonPanel, BorderLayout.NORTH);

            this.add(buildInfoPanel(info, parentSize), BorderLayout.CENTER);

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

        public JPanel buildInfoPanel(ModInfo info, Dimension parentSize)
        {
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

            if (info.Description != null && !info.Description.equals("")) {
                description = new JTextArea(info.Description);
                description.setAlignmentX(Component.LEFT_ALIGNMENT);
                description.setLineWrap(true);
                description.setWrapStyleWord(true);
                description.setEditable(false);
                description.setBorder(null);
                description.setOpaque(false);
                description.setFont(description.getFont().deriveFont(Font.PLAIN));
                description.setSize(parentSize.width, description.getPreferredSize().height);
                infoPanel.add(description);
            }

            if (info.Authors != null && info.Authors.length > 0) {
                String label = "Author" + (info.Authors.length > 1 ? "s" : "") + ": ";
                author = new JTextArea(label + String.join(", ", info.Authors));
                author.setAlignmentX(Component.LEFT_ALIGNMENT);
                author.setLineWrap(true);
                author.setWrapStyleWord(true);
                author.setEditable(false);
                author.setBorder(null);
                author.setOpaque(false);
                author.setFont(author.getFont().deriveFont(Font.BOLD));
                author.setSize(parentSize.width, author.getPreferredSize().height);
                infoPanel.add(author);

                author.addComponentListener(new ComponentAdapter()
                {
                    @Override
                    public void componentResized(ComponentEvent e)
                    {
                        super.componentResized(e);
                        System.out.println("resized");
                    }
                });
            }

            return infoPanel;
        }

        @Override
        public void setBackground(Color c)
        {
            super.setBackground(c);
            if (buttonPanel != null) {
                buttonPanel.setBackground(c);
            }
            if (author != null) {
                author.setOpaque(c.equals(lightRed) || c.equals(lightYellow));
                author.setBackground(c);
            }
            if (description != null) {
                description.setOpaque(c.equals(lightRed) || c.equals(lightYellow));
                description.setBackground(c);
            }
        }

        @Override
        public Dimension getPreferredSize()
        {
            int height = 0;
            if (buttonPanel != null) {
                height += buttonPanel.getPreferredSize().height;
            }
            if (description != null) {
                height += description.getPreferredSize().height;
            }
            if (author != null) {
                height += author.getPreferredSize().height;
            }
            return new Dimension(-1, height);
        }
    }
    
}

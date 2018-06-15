package com.evacipated.cardcrawl.modthespire.ui;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.MatteBorder;

@SuppressWarnings("serial")
public class ModPanel extends JPanel {
    private static final Color lightRed = new Color(229,115,115);
    private static final Color lightOrange = new Color(255, 159, 0); // orange peel (https://en.wikipedia.org/wiki/Shades_of_orange#Orange_peel)
    private static final Color lightYellow = new Color(255, 238, 88);
    public ModInfo info;
    public File modFile;
    public JCheckBox checkBox;
    private InfoPanel infoPanel;
    
    private static boolean dependenciesChecked(ModInfo info, JModPanelCheckBoxList parent) {
        String[] dependencies = info.Dependencies;
        boolean[] checked = new boolean[dependencies.length]; // initializes to false
        for (int i = 0; i < parent.getModel().getSize(); i++) {
            ModPanel panel = parent.getModel().getElementAt(i);
            for (int j = 0; j < dependencies.length; j++) {
                if (panel.info != null && panel.info.ID != null && panel.info.ID.equals(dependencies[j]) && panel.checkBox.isSelected()) {
                    checked[j] = true;
                }
            }
        }
        boolean allChecked = true;
        for (int i = 0; i < checked.length; i++) {
            if (!checked[i]) {
                allChecked = false;
            }
        }

        return allChecked;
    }
    
    private static String[] missingDependencies(ModInfo info, JModPanelCheckBoxList parent) {
        String[] dependencies = info.Dependencies;
        boolean[] checked = new boolean[dependencies.length]; // initializes to false
        for (int i = 0; i < parent.getModel().getSize(); i++) {
            ModPanel panel = parent.getModel().getElementAt(i);
            for (int j = 0; j < dependencies.length; j++) {
                if (panel.info != null && panel.info.ID != null && panel.info.ID.equals(dependencies[j]) && panel.checkBox.isSelected()) {
                    checked[j] = true;
                }
            }
        }
        java.util.List<String> missing = new ArrayList<String>();
        for (int i = 0; i < checked.length; i++) {
            if (!checked[i]) {
                missing.add(dependencies[i]);
            }
        }
        String[] returnType = new String[missing.size()];
        return missing.toArray(returnType);
    }
    
    public ModPanel(ModInfo info, File modFile, Dimension parentSize, JModPanelCheckBoxList parent) {
        this.info = info;
        this.modFile = modFile;
        this.checkBox = new JCheckBox();
        this.setLayout(new BorderLayout());
        infoPanel = new InfoPanel(parentSize);
        this.add(infoPanel, BorderLayout.CENTER);

        checkBox.addItemListener((event) -> {
            parent.publishBoxChecked();
        });
        parent.publishBoxChecked();
    }
    
    public void recalcModWarnings(JModPanelCheckBoxList parent) {
        if (info.MTS_Version.compareTo(Loader.MTS_VERSION) > 0) {
            checkBox.setEnabled(false);
            checkBox.setBackground(lightRed);
            infoPanel.setBackground(lightRed);
            setToolTipText("This mod requires ModTheSpire v" + info.MTS_Version.get() + " or higher.");
        } else if (checkBox.isSelected() && !dependenciesChecked(info, parent)) {
            checkBox.setBackground(lightOrange);
            infoPanel.setBackground(lightOrange);
            String[] missingDependencies = missingDependencies(info, parent);
            StringBuilder tooltip = new StringBuilder("");
            tooltip.append("Missing dependencies: [");
            tooltip.append(String.join(", ", missingDependencies));
            tooltip.append("]");
            setToolTipText(tooltip.toString());
        } else if (Loader.STS_VERSION != null && info.STS_Version != null && !Loader.STS_VERSION.equals(info.STS_Version)) {
            checkBox.setBackground(lightYellow);
            infoPanel.setBackground(lightYellow);
            setToolTipText("<html>This mod explicitly supports StS " + info.STS_Version + ".<br/>" +
                "You are running StS " + Loader.STS_VERSION + ".<br/>" +
                "You may encounter problems running it.</html>");
        } else {
            checkBox.setBackground(Color.WHITE);
            infoPanel.setBackground(Color.WHITE);
            setToolTipText(null);
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
            checkBox.setBackground(Color.WHITE);
            setBackground(Color.WHITE);
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
                description.setOpaque(true);
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
                author.setOpaque(true);
                author.setFont(author.getFont().deriveFont(Font.BOLD));
                author.setSize(parentSize.width, author.getPreferredSize().height);
                infoPanel.add(author);

                author.addComponentListener(new ComponentAdapter()
                {
                    @Override
                    public void componentResized(ComponentEvent e)
                    {
                        super.componentResized(e);
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
                author.setBackground(c);
            }
            if (description != null) {
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

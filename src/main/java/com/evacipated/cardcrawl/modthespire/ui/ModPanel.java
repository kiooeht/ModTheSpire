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

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

@SuppressWarnings("serial")
public class ModPanel extends JPanel
{
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
    
    public ModPanel(ModInfo info, File modFile, JModPanelCheckBoxList parent) {
        this.info = info;
        this.modFile = modFile;
        checkBox = new JCheckBox();
        setLayout(new BorderLayout());
        infoPanel = new InfoPanel();

        add(checkBox, BorderLayout.WEST);
        add(infoPanel, BorderLayout.CENTER);

        setBorder(new MatteBorder(0, 0, 1, 0, Color.darkGray));

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
    
    public class InfoPanel extends JPanel
    {
        JLabel name = new JLabel();
        JLabel version = new JLabel();
        JLabel update = new JLabel();

        public InfoPanel()
        {
            setLayout(new BorderLayout());

            name.setOpaque(true);
            name.setText(info.Name);
            name.setFont(name.getFont().deriveFont(14.0f));
            add(name, BorderLayout.CENTER);

            version.setOpaque(true);
            version.setFont(name.getFont().deriveFont(10.0f));
            if (info.Version != null) {
                version.setText(info.Version.get());
            } else {
                version.setText("missing version");
            }
            add(version, BorderLayout.SOUTH);


            update.setIcon(new ImageIcon(getClass().getResource("/assets/good.gif")));
            update.setHorizontalAlignment(JLabel.CENTER);
            update.setVerticalAlignment(JLabel.BOTTOM);
            update.setOpaque(true);
            update.setToolTipText("Up to date.");
            update.setBorder(new EmptyBorder(0, 0, 0, 4));
            add(update, BorderLayout.EAST);

            checkBox.setBackground(Color.WHITE);
            setBackground(Color.WHITE);
        }

        @Override
        public void setBackground(Color c)
        {
            super.setBackground(c);
            if (name != null) {
                name.setBackground(c);
            }
            if (version != null) {
                version.setBackground(c);
            }
            if (update != null) {
                update.setBackground(c);
            }
        }
    }
    
}

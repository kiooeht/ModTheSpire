package com.evacipated.cardcrawl.modthespire;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class ModSelectWindow extends JFrame {
    private File[] mods;

    public ModSelectWindow(File[] modJars) {
        mods = modJars;
        initUI();
    }

    private void initUI() {
        setTitle("Mod The Spire " + Loader.MTS_VERSION);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        rootPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1;
        
        // Remove the .jar from mod names to display in modList
        String[] modNames = new String[mods.length];
        for (int i = 0; i < mods.length; i++) {
            String modName = mods[i].getName();
            modNames[i] = modName.substring(0, modName.length() - 4);
        }
        
        // Mod List
        JList modList = new JList(modNames);
        modList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        modList.setLayoutOrientation(JList.VERTICAL);
        modList.setVisibleRowCount(-1);
        
        JScrollPane modScroller = new JScrollPane(modList);
        modScroller.setPreferredSize(new Dimension(300, 200));
        add(modScroller, gbc);  

        // Play button
        JButton playBtn = new JButton("Play");
        playBtn.addActionListener((ActionEvent event) -> {
            // Build array of selected mods
            int[] selectedIndices = modList.getSelectedIndices();
            File[] selectedMods = new File[selectedIndices.length];
            for (int i = 0; i < selectedIndices.length; i++) {
                selectedMods[i] = mods[selectedIndices[i]];
            }
            
            Loader.runMods(selectedMods);
        });
        add(playBtn, gbc);
        
        pack();
        setLocationRelativeTo(null);
    }
}

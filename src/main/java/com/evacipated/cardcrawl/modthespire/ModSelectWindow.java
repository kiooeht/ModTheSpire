package com.evacipated.cardcrawl.modthespire;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

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
        int modsLength = mods != null ? mods.length : 0;
        String[] modNames = new String[modsLength];
        for (int i = 0; i < modsLength; i++) {
            String modName = mods[i].getName();
            modNames[i] = modName.substring(0, modName.length() - 4);
        }

        // Mod List
        DefaultListModel<JCheckBox> model = new DefaultListModel<>();
        JCheckBoxList modList = new JCheckBoxList(model);
        for (String name : modNames) {
            model.addElement(new JCheckBox(name));
        }
        
        JScrollPane modScroller = new JScrollPane(modList);
        modScroller.setPreferredSize(new Dimension(300, 200));
        add(modScroller, gbc);

        // Play button
        JButton playBtn = new JButton("Play");
        playBtn.addActionListener((ActionEvent event) -> {
            playBtn.setEnabled(false);

            getContentPane().removeAll();

            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1;

            JTextArea textArea = new JTextArea();
            textArea.setFont(new Font("monospaced", Font.PLAIN, 12));
            JScrollPane logScroller = new JScrollPane(textArea);
            logScroller.setPreferredSize(new Dimension(700, 800));
            add(logScroller, gbc);
            MessageConsole mc = new MessageConsole(textArea);
            mc.redirectOut(null, System.out);
            mc.redirectErr(null, System.err);

            setResizable(true);
            pack();
            setLocationRelativeTo(null);

            Thread t = new Thread(() -> {
                // Build array of selected mods
                int[] selectedIndices = modList.getCheckedIndices();
                File[] selectedMods = new File[selectedIndices.length];
                for (int i = 0; i < selectedIndices.length; i++) {
                    selectedMods[i] = mods[selectedIndices[i]];
                }

                Loader.runMods(selectedMods);
            });
            t.start();
        });
        add(playBtn, gbc);
        
        pack();
        setLocationRelativeTo(null);
    }
}

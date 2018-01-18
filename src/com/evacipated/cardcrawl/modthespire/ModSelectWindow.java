package com.evacipated.cardcrawl.modthespire;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class ModSelectWindow extends JFrame
{
    private File[] mods;
    private String[] main_args;

    public ModSelectWindow(File[] mod_jars, String[] args)
    {
        mods = mod_jars;
        main_args = args;
        initUI();
    }

    private void initUI()
    {
        setTitle("Mod The Spire");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        rootPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1;

        JButton vanillaBtn = new JButton("Vanilla");
        vanillaBtn.addActionListener((ActionEvent event) -> {
            Loader.runMod(null, main_args);
        });

        add(vanillaBtn, gbc);
        add(new JLabel("", SwingConstants.CENTER), gbc);

        if (mods != null) {
            add(new JLabel("Mods:", SwingConstants.CENTER), gbc);

            for (File mod : mods) {
                String mod_name = mod.getName();
                mod_name = mod_name.substring(0, mod_name.length() - 4);
                JButton btn = new JButton(mod_name);
                btn.addActionListener((ActionEvent event) -> {
                    Loader.runMod(mod, main_args);
                });
                add(btn, gbc);
            }
        } else {
            add(new JLabel("No mods found", SwingConstants.CENTER), gbc);
        }

        pack();
        setLocationRelativeTo(null);
    }
}

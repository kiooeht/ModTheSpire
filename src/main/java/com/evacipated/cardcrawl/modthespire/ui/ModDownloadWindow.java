package com.evacipated.cardcrawl.modthespire.ui;

import javax.swing.*;
import java.awt.*;

public class ModDownloadWindow extends JDialog {
    JLabel downloadingLabel;
    JProgressBar progressBar;

    public ModDownloadWindow(Component parent) {
        super((Frame)null, "Downloading Mods", true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(500, 100));

        downloadingLabel = new JLabel("Downloading...");
        downloadingLabel.setHorizontalAlignment(SwingConstants.CENTER);

        progressBar = new JProgressBar();
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        add(downloadingLabel, BorderLayout.NORTH);
        add(progressBar, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(parent);
    }

    public void setDownloadPercentage(int percentage){
        SwingUtilities.invokeLater(() -> progressBar.setValue(percentage));
    }

    public void setDownloadingMod(String mod){
        SwingUtilities.invokeLater(() -> downloadingLabel.setText("Downloading " + mod));
    }
}

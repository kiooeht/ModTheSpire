package com.evacipated.cardcrawl.modthespire.ui;

import com.evacipated.cardcrawl.modthespire.DownloadAndRestarter;
import com.evacipated.cardcrawl.modthespire.ModUpdate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

class UpdateWindow extends JDialog
{
    UpdateWindow(JFrame parent)
    {
        super(parent);
        setModal(true);
        initUI();
    }

    private void initUI()
    {
        if (ModSelectWindow.MODUPDATES.size() == 1) {
            setTitle("Update Available");
        } else {
            setTitle("Updates Available");
        }
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(true);

        getContentPane().setPreferredSize(new Dimension(300, 200));

        rootPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        setLayout(new BorderLayout());

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        JScrollPane modScroller = new JScrollPane(list);
        getContentPane().add(modScroller, BorderLayout.CENTER);

        for (ModUpdate update : ModSelectWindow.MODUPDATES) {
            model.addElement(update.info.Name);
        }

        String tmp;
        if (ModSelectWindow.MODUPDATES.size() == 1) {
            tmp = "The following mod has an update available:";
        } else {
            tmp = "The following mods have updates available:";
        }
        getContentPane().add(new JLabel(tmp), BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridBagLayout());
        JButton downloadBtn = new JButton("Download Updates and Restart ModTheSpire");
        JButton browserBtn = new JButton("Open Releases in Browser");
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        btnPanel.add(downloadBtn, c);
        c.gridy = 1;
        btnPanel.add(browserBtn, c);
        getContentPane().add(btnPanel, BorderLayout.SOUTH);

        // Open each update's release url in browser
        browserBtn.addActionListener((ActionEvent event) -> {
            if (Desktop.isDesktopSupported()) {
                for (ModUpdate update : ModSelectWindow.MODUPDATES) {
                    try {
                        Desktop.getDesktop().browse(update.releaseURL.toURI());
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Download each update
        downloadBtn.addActionListener((ActionEvent event) -> {
            getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            URL[] downloadURLs = new URL[ModSelectWindow.MODUPDATES.size()];
            for (int i=0; i<ModSelectWindow.MODUPDATES.size(); ++i) {
                downloadURLs[i] = ModSelectWindow.MODUPDATES.get(i).downloadURL;
            }
            try {
                DownloadAndRestarter.downloadAndRestart(downloadURLs);
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });

        pack();
        setLocationRelativeTo(getParent());
    }
}

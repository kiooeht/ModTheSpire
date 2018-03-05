package com.evacipated.cardcrawl.modthespire;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.util.Properties;

public class ModSelectWindow extends JFrame {
    /**
     * 
     */
    private static final long serialVersionUID = -8232997068791248057L;
    private static final String WINDOW_PROPERTIES_FILEPATH = "ModTheSpire.properties";
    private File[] mods;
    private ModInfo[] info;
    private Properties windowProperties;
    private int widthOffset = 0;
    private int heightOffset = 0;
    
    public ModSelectWindow(File[] modJars) {
        mods = modJars;
        info = Loader.buildInfoArray(mods);
        readWindowPosSize();
        initUI();
    }

    private void readWindowPosSize()
    {
        windowProperties = new Properties();
        File file = new File(WINDOW_PROPERTIES_FILEPATH);
        if (file.exists()) {
            try {
                windowProperties.load(new FileInputStream(file));
            } catch (IOException e) {
            }
        } else {
            windowProperties.setProperty("x", "center");
            windowProperties.setProperty("y", "center");
            windowProperties.setProperty("width", "300");
            windowProperties.setProperty("height", "200");
            saveWindowProperties();
        }
    }

    private void initUI() {
        setTitle("Mod The Spire " + Loader.MTS_VERSION.get());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);

        int w, h;
        try {
            w = Integer.parseInt(windowProperties.getProperty("width"));
        } catch (NumberFormatException e) {
            w = 300;
        }
        try {
            h = Integer.parseInt(windowProperties.getProperty("height"));
        } catch (NumberFormatException e) {
            h = 200;
        }

        ModSelectWindow tmpthis = this;
        final int finalW = w;
        final int finalH = h;
        this.addComponentListener(new ComponentAdapter()
        {
            private boolean firstResize = true;

            @Override
            public void componentResized(ComponentEvent e)
            {
                super.componentResized(e);

                Dimension d = tmpthis.getContentPane().getSize();
                if (firstResize) {
                    firstResize = false;
                    widthOffset = d.width - finalW;
                    heightOffset = d.height - finalH;
                } else {
                    d.width -= widthOffset;
                    d.height -= heightOffset;
                    setWindowDimensions(d);
                }
            }
        });

        rootPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        setLayout(new BorderLayout());

        // Mod List
	    DefaultListModel<ModPanel> model = new DefaultListModel<>();
        JModPanelCheckBoxList modList = new JModPanelCheckBoxList(model);
        LoadOrder.loadModsInOrder(model, mods, info);

        JScrollPane modScroller = new JScrollPane(modList);
        modScroller.setPreferredSize(new Dimension(w, h));
        this.getContentPane().add(modScroller, BorderLayout.CENTER);

        // Play button
        JButton playBtn = new JButton("Play");
        playBtn.addActionListener((ActionEvent event) -> {
            playBtn.setEnabled(false);

            this.getContentPane().removeAll();

            JTextArea textArea = new JTextArea();
            textArea.setFont(new Font("monospaced", Font.PLAIN, 12));
            JScrollPane logScroller = new JScrollPane(textArea);
            logScroller.setPreferredSize(new Dimension(700, 800));
            this.getContentPane().add(logScroller, BorderLayout.CENTER);
            MessageConsole mc = new MessageConsole(textArea);
            mc.redirectOut(null, System.out);
            mc.redirectErr(null, System.err);

            setResizable(true);
            pack();
            setLocationRelativeTo(null);
            
            Thread tCfg = new Thread(() -> {
                // Save new load order cfg
                LoadOrder.saveCfg(modList.getCheckedMods());
            });
            tCfg.start();

            Thread t = new Thread(() -> {
                // Build array of selected mods
                File[] selectedMods = modList.getCheckedMods();

                Loader.runMods(selectedMods);
            });
            t.start();
        });

        JPanel playPane = new JPanel();
        playPane.setLayout(new BorderLayout());
        playPane.add(playBtn, BorderLayout.CENTER);
        JCheckBox debugCheck = new JCheckBox("Debug");
        if (Loader.DEBUG) {
            debugCheck.setSelected(true);
        }
        debugCheck.addActionListener((ActionEvent event) -> {
            Loader.DEBUG = debugCheck.isSelected();
        });
        playPane.add(debugCheck, BorderLayout.EAST);

        add(playPane, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    void saveWindowProperties()
    {
        try {
            windowProperties.store(new FileOutputStream(WINDOW_PROPERTIES_FILEPATH), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setWindowDimensions(Dimension d)
    {
        windowProperties.setProperty("width", String.valueOf(d.width));
        windowProperties.setProperty("height", String.valueOf(d.height));
        saveWindowProperties();
    }
}

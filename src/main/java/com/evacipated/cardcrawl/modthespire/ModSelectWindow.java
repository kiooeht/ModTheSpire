package com.evacipated.cardcrawl.modthespire;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Properties;

public class ModSelectWindow extends JFrame {
    /**
     * 
     */
    private static final long serialVersionUID = -8232997068791248057L;
    private static final String WINDOW_PROPERTIES_FILEPATH = "ModTheSpire.properties";
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 226;
    private File[] mods;
    private ModInfo[] info;
    private Properties windowProperties;
    private boolean showingLog = false;
    private boolean isMaximized = false;
    
    public ModSelectWindow(File[] modJars) {
        mods = modJars;
        info = Loader.buildInfoArray(mods);
        readWindowPosSize();
        initUI();
        System.out.println(windowProperties.getProperty("maximize"));
        if (Boolean.parseBoolean(windowProperties.getProperty("maximize", "false"))) {
            isMaximized = true;
            this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        }
    }

    private void readWindowPosSize()
    {
        windowProperties = new Properties();
        File file = new File(WINDOW_PROPERTIES_FILEPATH);
        if (file.exists()) {
            try {
                windowProperties.load(new FileInputStream(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            windowProperties.setProperty("x", "center");
            windowProperties.setProperty("y", "center");
            windowProperties.setProperty("width", String.valueOf(DEFAULT_WIDTH));
            windowProperties.setProperty("height", String.valueOf(DEFAULT_HEIGHT));
            windowProperties.setProperty("maximize", "false");
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
            w = DEFAULT_WIDTH;
        }
        try {
            h = Integer.parseInt(windowProperties.getProperty("height"));
        } catch (NumberFormatException e) {
            h = DEFAULT_HEIGHT;
        }

        ModSelectWindow tmpthis = this;
        this.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                super.componentResized(e);

                if (!showingLog) {
                    Dimension d = tmpthis.getContentPane().getSize();
                    System.out.println(d.width + ", " + d.height);
                    if (!isMaximized) {
                        saveWindowDimensions(d);
                    }
                }
            }
        });
        this.addWindowStateListener(new WindowAdapter()
        {
            @Override
            public void windowStateChanged(WindowEvent e)
            {
                super.windowStateChanged(e);
                if ((e.getNewState() & Frame.MAXIMIZED_BOTH) != 0) {
                    isMaximized = true;
                    saveWindowMaximize();
                } else {
                    isMaximized = false;
                    saveWindowMaximize();
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
        this.getContentPane().setPreferredSize(new Dimension(w, h));
        this.getContentPane().add(modScroller, BorderLayout.CENTER);

        // Play button
        JButton playBtn = new JButton("Play");
        playBtn.addActionListener((ActionEvent event) -> {
            showingLog = true;
            playBtn.setEnabled(false);

            this.getContentPane().removeAll();

            JTextArea textArea = new JTextArea();
            textArea.setFont(new Font("monospaced", Font.PLAIN, 12));
            JScrollPane logScroller = new JScrollPane(textArea);
            this.getContentPane().setPreferredSize(new Dimension(700, 800));
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

    void saveWindowDimensions(Dimension d)
    {
        windowProperties.setProperty("width", String.valueOf(d.width));
        windowProperties.setProperty("height", String.valueOf(d.height));
        saveWindowProperties();
    }

    void saveWindowMaximize()
    {
        windowProperties.setProperty("maximize", String.valueOf(isMaximized));
        saveWindowProperties();
    }
}

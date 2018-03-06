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
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 226;
    private File[] mods;
    private ModInfo[] info;
    private Properties windowProperties;
    private boolean showingLog = false;
    private boolean isMaximized = false;
    private boolean isCentered = false;
    private Rectangle location;
    
    public ModSelectWindow(File[] modJars) {
        mods = modJars;
        info = Loader.buildInfoArray(mods);
        readWindowPosSize();
        initUI();
        if (Boolean.parseBoolean(windowProperties.getProperty("maximize", "false"))) {
            isMaximized = true;
            this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        }
    }

    private void readWindowPosSize()
    {
        windowProperties = Loader.MTS_PROPERTIES;

        // Ensure all properties are present
        windowProperties.setProperty("x", windowProperties.getProperty("x", "center"));
        windowProperties.setProperty("y", windowProperties.getProperty("y", "center"));
        windowProperties.setProperty("width", windowProperties.getProperty("width", String.valueOf(DEFAULT_WIDTH)));
        windowProperties.setProperty("height", windowProperties.getProperty("height", String.valueOf(DEFAULT_HEIGHT)));
        windowProperties.setProperty("maximize", windowProperties.getProperty("maximize", "false"));

        // Sanity check values
        if (Integer.parseInt(windowProperties.getProperty("width", String.valueOf(DEFAULT_WIDTH))) < DEFAULT_WIDTH) {
            windowProperties.setProperty("width", String.valueOf(DEFAULT_WIDTH));
        }
        if (Integer.parseInt(windowProperties.getProperty("height", String.valueOf(DEFAULT_HEIGHT))) < DEFAULT_HEIGHT) {
            windowProperties.setProperty("height", String.valueOf(DEFAULT_HEIGHT));
        }
        location = new Rectangle();
        location.width = Integer.parseInt(windowProperties.getProperty("width", String.valueOf(DEFAULT_WIDTH)));
        location.height = Integer.parseInt(windowProperties.getProperty("height", String.valueOf(DEFAULT_HEIGHT)));
        if (windowProperties.getProperty("x", "center").equals("center") || windowProperties.getProperty("y", "center").equals("center")) {
            isCentered = true;
        } else {
            isCentered = false;
            location.x = Integer.parseInt(windowProperties.getProperty("x", "0"));
            location.y = Integer.parseInt(windowProperties.getProperty("y", "0"));
            if (!isInScreenBounds(location)) {
                windowProperties.setProperty("x", "center");
                windowProperties.setProperty("y", "center");
                isCentered = true;
            }
        }

        saveWindowProperties();
    }

    private void initUI() {
        setTitle("Mod The Spire " + Loader.MTS_VERSION.get());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);

        ModSelectWindow tmpthis = this;
        this.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                super.componentResized(e);

                if (!showingLog) {
                    Dimension d = tmpthis.getContentPane().getSize();
                    if (!isMaximized) {
                        saveWindowDimensions(d);
                    }
                }
            }

            int skipMoves = 2;

            @Override
            public void componentMoved(ComponentEvent e)
            {
                super.componentMoved(e);

                if (!showingLog && skipMoves == 0) {
                    if (isInScreenBounds(getLocationOnScreen(), getBounds())) {
                        saveWindowLocation();
                    }
                    isCentered = false;
                } else if (skipMoves > 0) {
                    --skipMoves;
                }
            }
        });
        this.addWindowStateListener(new WindowAdapter()
        {
            @Override
            public void windowStateChanged(WindowEvent e)
            {
                super.windowStateChanged(e);

                if (!showingLog) {
                    if ((e.getNewState() & Frame.MAXIMIZED_BOTH) != 0) {
                        isMaximized = true;
                        saveWindowMaximize();
                    } else {
                        isMaximized = false;
                        saveWindowMaximize();
                    }
                }
            }
        });

        rootPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        setLayout(new BorderLayout());

        // Mod List
        DefaultListModel<ModPanel> model = new DefaultListModel<>();
        JModPanelCheckBoxList modList = new JModPanelCheckBoxList(model);
        LoadOrder.loadModsInOrder(model, mods, info, new Dimension(location.width, location.height));

        JScrollPane modScroller = new JScrollPane(modList);
        this.getContentPane().setPreferredSize(new Dimension(location.width, location.height));
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
            if (isCentered) {
                setLocationRelativeTo(null);
            }
            
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
            Loader.MTS_PROPERTIES.setProperty("debug", Boolean.toString(Loader.DEBUG));
            Loader.saveProperties();
        });
        playPane.add(debugCheck, BorderLayout.EAST);

        add(playPane, BorderLayout.SOUTH);

        pack();
        if (isCentered) {
            setLocationRelativeTo(null);
        } else {
            setLocation(location.getLocation());
        }
    }

    void saveWindowProperties()
    {
        Loader.saveProperties();
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

    void saveWindowLocation()
    {
        Point loc = getLocationOnScreen();
        windowProperties.setProperty("x", String.valueOf(loc.x));
        windowProperties.setProperty("y", String.valueOf(loc.y));
        saveWindowProperties();
    }

    boolean isInScreenBounds(Point location, Rectangle size)
    {
        size.setLocation(location);
        return isInScreenBounds(size);
    }

    boolean isInScreenBounds(Rectangle location)
    {
        for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            Rectangle bounds = gd.getDefaultConfiguration().getBounds();
            // Expand screen bounds slightly
            bounds.x -= 10;
            bounds.width += 20;
            bounds.y -= 10;
            bounds.height += 20;
            if (bounds.contains(location)) {
                return true;
            }
        }
        return false;
    }
}

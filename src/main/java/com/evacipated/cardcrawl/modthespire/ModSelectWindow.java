package com.evacipated.cardcrawl.modthespire;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.MalformedURLException;
import java.util.Properties;

public class ModSelectWindow extends JFrame {
    
	/**
     * 
     */
    private static final long serialVersionUID = -8232997068791248057L;
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 226;
    private static final String DEBUG_OPTION = "Debug";
    private static final String PLAY_OPTION = "Play";
    private static final String JAR_DUMP_OPTION = "Dump Patched Jar";
    private File[] mods;
    private ModInfo[] info;
    private boolean showingLog = false;
    private boolean isMaximized = false;
    private boolean isCentered = false;
    private Rectangle location;
    private JPanel playPane;

    enum UpdateIconType
    {
        NONE, CHECKING, UPDATE_AVAILABLE, UPTODATE
    }

    public static Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.setProperty("x", "center");
        properties.setProperty("y", "center");
        properties.setProperty("width", Integer.toString(DEFAULT_WIDTH));
        properties.setProperty("height", Integer.toString(DEFAULT_HEIGHT));
        properties.setProperty("maximize", Boolean.toString(false));
        return properties;
    }
    
    public ModSelectWindow(File[] modJars) throws MalformedURLException
    {
        mods = modJars;
        info = Loader.buildInfoArray(mods);
        readWindowPosSize();
        initUI();
        if (Loader.MTS_CONFIG.getBool("maximize")) {
            isMaximized = true;
            this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        }
    }

    private void readWindowPosSize()
    {
        // Sanity check values
        if (Loader.MTS_CONFIG.getInt("width") < DEFAULT_WIDTH) {
            Loader.MTS_CONFIG.setInt("width", DEFAULT_WIDTH);
        }
        if (Loader.MTS_CONFIG.getInt("height") < DEFAULT_HEIGHT) {
            Loader.MTS_CONFIG.setInt("height", DEFAULT_HEIGHT);
        }
        location = new Rectangle();
        location.width = Loader.MTS_CONFIG.getInt("width");
        location.height = Loader.MTS_CONFIG.getInt("height");
        if (Loader.MTS_CONFIG.getString("x").equals("center") || Loader.MTS_CONFIG.getString("y").equals("center")) {
            isCentered = true;
        } else {
            isCentered = false;
            location.x = Loader.MTS_CONFIG.getInt("x");
            location.y = Loader.MTS_CONFIG.getInt("y");
            if (!isInScreenBounds(location)) {
                Loader.MTS_CONFIG.setString("x", "center");
                Loader.MTS_CONFIG.setString("y", "center");
                isCentered = true;
            }
        }

        try {
            Loader.MTS_CONFIG.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        LoadOrder.loadModsInOrder(model, mods, info, new Dimension(location.width, location.height), modList);

        JScrollPane modScroller = new JScrollPane(modList);
        this.getContentPane().setPreferredSize(new Dimension(location.width, location.height));
        this.getContentPane().add(modScroller, BorderLayout.CENTER);

        // Play button
        JButton playBtn = new JButton(
        		Loader.OUT_JAR ? JAR_DUMP_OPTION : PLAY_OPTION
        		);
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

        playPane = new JPanel();
        playPane.setLayout(new BorderLayout());
        playPane.add(playBtn, BorderLayout.CENTER);
        JCheckBox debugCheck = new JCheckBox(DEBUG_OPTION);
        if (Loader.DEBUG) {
            debugCheck.setSelected(true);
        }
        debugCheck.addActionListener((ActionEvent event) -> {
            Loader.DEBUG = debugCheck.isSelected();
            Loader.MTS_CONFIG.setBool("debug", Loader.DEBUG);
            try {
                Loader.MTS_CONFIG.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        playPane.add(debugCheck, BorderLayout.EAST);
        
        setUpdateIcon(UpdateIconType.NONE);

        add(playPane, BorderLayout.SOUTH);

        pack();
        if (isCentered) {
            setLocationRelativeTo(null);
        } else {
            setLocation(location.getLocation());
        }
    }

    void saveWindowDimensions(Dimension d)
    {
        Loader.MTS_CONFIG.setInt("width", d.width);
        Loader.MTS_CONFIG.setInt("height", d.height);
        try {
            Loader.MTS_CONFIG.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void saveWindowMaximize()
    {
        Loader.MTS_CONFIG.setBool("maximize", isMaximized);
        try {
            Loader.MTS_CONFIG.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void saveWindowLocation()
    {
        Point loc = getLocationOnScreen();
        Loader.MTS_CONFIG.setInt("x", loc.x);
        Loader.MTS_CONFIG.setInt("y", loc.y);
        try {
            Loader.MTS_CONFIG.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    synchronized void setUpdateIcon(UpdateIconType type)
    {
        if (playPane.getComponentCount() > 2) {
            playPane.remove(2);
        }
        switch (type) {
            case NONE:
                playPane.add(Box.createRigidArea(new Dimension(20, 16)), BorderLayout.WEST);
                break;
            case CHECKING: {
                JLabel label = new JLabel(new ImageIcon(getClass().getResource("/assets/ajax-loader.gif")), JLabel.CENTER);
                label.setToolTipText("Checking for updates...");
                label.setBorder(new EmptyBorder(0, 0, 0, 4));
                playPane.add(label, BorderLayout.WEST);
                break;
            }
            case UPDATE_AVAILABLE: {
                JLabel label = new JLabel(new ImageIcon(getClass().getResource("/assets/warning.gif")), JLabel.CENTER);
                label.setToolTipText("An update for ModTheSpire is available.");
                label.setBorder(new EmptyBorder(0, 0, 0, 4));
                playPane.add(label, BorderLayout.WEST);
                label.setCursor(new Cursor(Cursor.HAND_CURSOR));
                label.addMouseListener(new MouseAdapter()
                {
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        Loader.openLatestReleaseURL();
                    }
                });
                break;
            }
            case UPTODATE: {
                JLabel label = new JLabel(new ImageIcon(getClass().getResource("/assets/good.gif")), JLabel.CENTER);
                label.setToolTipText("ModTheSpire is up to date.");
                label.setBorder(new EmptyBorder(0, 0, 0, 4));
                playPane.add(label, BorderLayout.WEST);
                break;
            }
        }
        revalidate();
        repaint();
    }
}

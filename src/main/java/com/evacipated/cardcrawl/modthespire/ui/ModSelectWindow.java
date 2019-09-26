package com.evacipated.cardcrawl.modthespire.ui;

import com.evacipated.cardcrawl.modthespire.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class ModSelectWindow extends JFrame
{
    /**
     *
     */
    private static final long serialVersionUID = -8232997068791248057L;
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 500;
    private static final String DEBUG_OPTION = "Debug";
    private static final String PLAY_OPTION = "Play";
    private static final String JAR_DUMP_OPTION = "Dump Patched Jar";

    static final Icon ICON_UPDATE   = new ImageIcon(ModSelectWindow.class.getResource("/assets/update.gif"));
    static final Icon ICON_LOAD     = new ImageIcon(ModSelectWindow.class.getResource("/assets/ajax-loader.gif"));
    static final Icon ICON_GOOD     = new ImageIcon(ModSelectWindow.class.getResource("/assets/good.gif"));
    static final Icon ICON_WARNING  = new ImageIcon(ModSelectWindow.class.getResource("/assets/warning.gif"));
    static final Icon ICON_ERROR    = new ImageIcon(ModSelectWindow.class.getResource("/assets/error.gif"));
    static final Icon ICON_WORKSHOP = new ImageIcon(ModSelectWindow.class.getResource("/assets/workshop.gif"));

    private ModInfo[] info;
    private boolean showingLog = false;
    private boolean isMaximized = false;
    private boolean isCentered = false;
    private Rectangle location;
    private JButton playBtn;

    private JModPanelCheckBoxList modList;

    private ModInfo currentModInfo;
    private TitledBorder name;
    private JTextArea authors;
    private JLabel modVersion;
    private JTextArea status;
    private JLabel mtsVersion;
    private JLabel stsVersion;
    private JTextArea description;
    private JTextArea credits;

    private JPanel bannerNoticePanel;
    private JLabel mtsUpdateBanner;
    private JLabel betaWarningBanner;

    private JPanel modBannerNoticePanel;
    private JLabel modUpdateBanner;

    static List<ModUpdate> MODUPDATES;

    public enum UpdateIconType
    {
        NONE, CAN_CHECK, CHECKING, UPDATE_AVAILABLE, UPTODATE, WORKSHOP
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
    
    public ModSelectWindow(ModInfo[] modInfos, boolean skipLauncher)
    {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
        }

        info = modInfos;
        readWindowPosSize();
        setupDetectMaximize();
        initUI(skipLauncher);
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

    private void setupDetectMaximize()
    {
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
    }

    private void initUI(boolean skipLauncher)
    {
        setTitle("ModTheSpire " + Loader.MTS_VERSION);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(true);

        rootPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        setLayout(new BorderLayout());
        getContentPane().setPreferredSize(new Dimension(location.width, location.height));

        getContentPane().add(makeModListPanel(), BorderLayout.WEST);
        getContentPane().add(makeInfoPanel(), BorderLayout.CENTER);
        getContentPane().add(makeTopPanel(), BorderLayout.NORTH);

        pack();
        if (isCentered) {
            setLocationRelativeTo(null);
        } else {
            setLocation(location.getLocation());
        }

        if (skipLauncher) {
            playBtn.doClick();
        } else {
            // Default focus Play button
            JRootPane rootPane = SwingUtilities.getRootPane(playBtn);
            rootPane.setDefaultButton(playBtn);
            EventQueue.invokeLater(playBtn::requestFocusInWindow);
        }
    }

    private JPanel makeModListPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(220, 300));

        // Mod List
        DefaultListModel<ModPanel> model = new DefaultListModel<>();
        modList = new JModPanelCheckBoxList(this, model);
        ModList mods = ModList.loadModLists();
        mods.loadModsInOrder(model, info, modList);
        modList.publishBoxChecked();

        JScrollPane modScroller = new JScrollPane(modList);
        panel.add(modScroller, BorderLayout.CENTER);

        // Play button
        playBtn = new JButton(
            Loader.OUT_JAR ? JAR_DUMP_OPTION : PLAY_OPTION
        );
        playBtn.addActionListener((ActionEvent event) -> {
            showingLog = true;
            playBtn.setEnabled(false);

            this.getContentPane().removeAll();

            JTextArea textArea = new JTextArea();
            textArea.setLineWrap(true);
            textArea.setFont(new Font("monospaced", Font.PLAIN, 12));
            JScrollPane logScroller = new JScrollPane(textArea);
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
                ModList.save(ModList.getDefaultList(), modList.getCheckedMods());
            });
            tCfg.start();

            Thread t = new Thread(() -> {
                // Build array of selected mods
                File[] selectedMods = modList.getCheckedMods();

                Loader.runMods(selectedMods);
            });
            t.start();
        });
        if (Loader.STS_BETA && !Loader.allowBeta) {
            playBtn.setEnabled(false);
        }
        panel.add(playBtn, BorderLayout.SOUTH);

        // Open mod directory
        JButton openFolderBtn = new JButton(UIManager.getIcon("FileView.directoryIcon"));
        openFolderBtn.setToolTipText("Open Mods Directory");
        openFolderBtn.addActionListener((ActionEvent event) -> {
            try {
                File file = new File(Loader.MOD_DIR);
                if (!file.exists()) {
                    file.mkdir();
                }
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        // Check for Updates button
        JButton updatesBtn = new JButton(ICON_UPDATE);
        updatesBtn.setToolTipText("Check for Mod Updates");
        updatesBtn.addActionListener(event -> {
            startCheckingForModUpdates(updatesBtn);
        });
        // Settings button
        JButton settingsBtn = new JButton("Settings");
        settingsBtn.addActionListener((ActionEvent event) -> {
            // TODO
        });
        // Toggle all button
        JButton toggleAllBtn = new JButton(UIManager.getIcon("Tree.collapsedIcon"));
        toggleAllBtn.setToolTipText("Toggle all mods On/Off");
        toggleAllBtn.addActionListener((ActionEvent event) -> {
            modList.toggleAllMods();
            repaint();
        });

        JPanel btnPanel = new JPanel(new GridLayout(1, 0));
        //btnPanel.add(settingsBtn);
        btnPanel.add(updatesBtn);
        btnPanel.add(openFolderBtn);

        JComboBox<String> profilesList = new JComboBox<>(ModList.getAllModListNames().toArray(new String[0]));
        JButton addProfile = new JButton("+");
        JButton delProfile = new JButton("-");

        profilesList.addActionListener((ActionEvent event) -> {
            String profileName = (String) profilesList.getSelectedItem();
            delProfile.setEnabled(!ModList.DEFAULT_LIST.equals(profileName));
            ModList newList = new ModList(profileName);
            DefaultListModel<ModPanel> newModel = (DefaultListModel<ModPanel>) modList.getModel();
            newList.loadModsInOrder(newModel, info, modList);

            Thread tCfg = new Thread(() -> {
                // Save new load order cfg
                ModList.save(profileName, modList.getCheckedMods());
            });
            tCfg.start();
        });
        if (Loader.profileArg != null) {
            profilesList.setSelectedItem(Loader.profileArg);
        } else {
            profilesList.setSelectedItem(ModList.getDefaultList());
        }

        JPanel profilesPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.ipady = 2;
        profilesPanel.add(toggleAllBtn, c);
        c.weightx = 0.9;
        c.ipady = 0;
        profilesPanel.add(profilesList, c);
        c.weightx = 0;
        c.ipady = 2;
        // Add profile button
        addProfile.setToolTipText("Add new profile");
        addProfile.addActionListener((ActionEvent event) -> {
            String s = JOptionPane.showInputDialog(
                this,
                "Profile Name:",
                "New Profile",
                JOptionPane.PLAIN_MESSAGE
            );
            if (s != null && !s.isEmpty()) {
                profilesList.addItem(s);
                profilesList.setSelectedIndex(profilesList.getItemCount() - 1);
            }
        });
        profilesPanel.add(addProfile, c);
        // Delete profile button
        delProfile.setToolTipText("Delete profile");
        delProfile.addActionListener((ActionEvent event) -> {
            String profileName = (String) profilesList.getSelectedItem();

            int n = JOptionPane.showConfirmDialog(
                this,
                "Are you sure?\nThis action cannot be undone.",
                "Delete Profile \"" + profileName + "\"",
                JOptionPane.YES_NO_OPTION
            );
            if (n == 0) {
                profilesList.removeItem(profileName);
                profilesList.setSelectedItem(ModList.DEFAULT_LIST);
                ModList.delete(profileName);
            }
        });
        profilesPanel.add(delProfile, c);

        JPanel topPanel = new JPanel(new GridLayout(0, 1));
        topPanel.add(btnPanel);
        topPanel.add(profilesPanel);
        panel.add(topPanel, BorderLayout.NORTH);

        return panel;
    }

    private JPanel makeInfoPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Top mod banner panel
        panel.add(makeModBannerPanel(), BorderLayout.NORTH);

        // Bottom status panel
        panel.add(makeStatusPanel(), BorderLayout.SOUTH);

        // Main info panel
        JPanel infoPanel = new JPanel();
        name = BorderFactory.createTitledBorder("Mod Info");
        name.setTitleFont(name.getTitleFont().deriveFont(Font.BOLD));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            name,
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        infoPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;

        authors = makeInfoTextAreaField("Author(s)", " ");
        infoPanel.add(authors, c);

        c.gridy = 1;
        modVersion = makeInfoLabelField("ModVersion", " ");
        infoPanel.add(modVersion, c);

        c.gridy = 2;
        mtsVersion = makeInfoLabelField("ModTheSpire Version", " ");
        infoPanel.add(mtsVersion, c);

        c.gridy = 3;
        stsVersion = makeInfoLabelField("Slay the Spire Version", " ");
        infoPanel.add(stsVersion, c);

        c.gridy = 4;
        credits = makeInfoTextAreaField("Additional Credits", " ");
        infoPanel.add(credits, c);

        c.gridy = 5;
        status = makeInfoTextAreaField("Status", " ");
        infoPanel.add(status, c);

        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 7;
        c.weightx = 1;
        c.weighty = 1;
        description = makeInfoTextAreaField("Description", " ");
        infoPanel.add(description, c);

        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private JLabel makeInfoLabelField(String title, String value)
    {
        JLabel label = new JLabel(value);

        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
        label.setFont(label.getFont().deriveFont(Font.PLAIN));
        label.setBorder(BorderFactory.createCompoundBorder(
            border,
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        return label;
    }

    private JTextArea makeInfoTextAreaField(String title, String value)
    {
        JTextArea label = new JTextArea(value);

        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
        label.setBorder(BorderFactory.createCompoundBorder(
            border,
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        label.setEditable(false);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        label.setOpaque(false);
        label.setFont(border.getTitleFont().deriveFont(Font.PLAIN).deriveFont(11.0f));

        return label;
    }

    private JPanel makeModBannerPanel()
    {
        modBannerNoticePanel = new JPanel();
        modBannerNoticePanel.setLayout(new GridLayout(0, 1));
        modBannerNoticePanel.setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 0));

        modUpdateBanner = new JLabel();
        modUpdateBanner.setIcon(ICON_WARNING);
        modUpdateBanner.setText("<html>" +
            "An update is available for this mod." +
            "</html>");
        modUpdateBanner.setHorizontalAlignment(JLabel.CENTER);
        modUpdateBanner.setOpaque(true);
        modUpdateBanner.setBackground(new Color(255, 193, 7));
        modUpdateBanner.setBorder(new EmptyBorder(5, 5, 5, 5));

        return modBannerNoticePanel;
    }

    private JPanel makeStatusPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new MatteBorder(1, 0, 0, 0, Color.darkGray));

        // StS version
        JLabel sts_version = new JLabel("Slay the Spire version: " + Loader.STS_VERSION);
        if (Loader.STS_BETA) {
            sts_version.setText(sts_version.getText() + " BETA");
        }
        sts_version.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(sts_version, BorderLayout.EAST);

        // Debug checkbox
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
        panel.add(debugCheck, BorderLayout.WEST);

        return panel;
    }

    private JPanel makeTopPanel()
    {
        bannerNoticePanel = new JPanel();
        bannerNoticePanel.setLayout(new GridLayout(0, 1));

        if (Loader.STS_BETA) {
            betaWarningBanner = new JLabel();
            betaWarningBanner.setIcon(ICON_ERROR);
            betaWarningBanner.setText("<html>" +
                "You are on the Slay the Spire beta branch.<br/>" +
                "If mods are not working correctly,<br/>" +
                "switch to the main branch for best results." +
                "</html>");
            betaWarningBanner.setHorizontalAlignment(JLabel.CENTER);
            betaWarningBanner.setOpaque(true);
            betaWarningBanner.setBackground(new Color(255, 80, 80));
            betaWarningBanner.setBorder(new EmptyBorder(5, 5, 5, 5));
            bannerNoticePanel.add(betaWarningBanner);
        }

        mtsUpdateBanner = new JLabel();
        mtsUpdateBanner.setIcon(ICON_WARNING);
        mtsUpdateBanner.setText("<html>" +
            "An update for ModTheSpire is available.<br/>" +
            "Click here to open the download page." +
            "</html>");
        mtsUpdateBanner.setHorizontalAlignment(JLabel.CENTER);
        mtsUpdateBanner.setOpaque(true);
        mtsUpdateBanner.setBackground(new Color(255, 193, 7));
        mtsUpdateBanner.setBorder(new EmptyBorder(5, 5, 5, 5));
        mtsUpdateBanner.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return bannerNoticePanel;
    }

    private void setMTSUpdateAvailable(URL url)
    {
        bannerNoticePanel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(url.toURI());
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        bannerNoticePanel.add(mtsUpdateBanner);
        pack();
        repaint();
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

    void setModInfo(ModInfo info)
    {
        currentModInfo = info;

        name.setTitle(info.Name);
        authors.setText(String.join(", ", info.Authors));
        if (info.ModVersion != null) {
            modVersion.setText(info.ModVersion.toString());
        } else {
            modVersion.setText(" ");
        }
        if (info.MTS_Version != null) {
            mtsVersion.setText(info.MTS_Version + "+");
        } else {
            mtsVersion.setText(" ");
        }
        if (info.STS_Version != null && !info.STS_Version.isEmpty()) {
            stsVersion.setText(info.STS_Version);
        } else {
            stsVersion.setText(" ");
        }
        description.setText(info.Description);
        credits.setText(info.Credits);

        status.setText(info.statusMsg);

        setModUpdateBanner(info);

        repaint();
    }

    synchronized void setModUpdateBanner(ModInfo info)
    {
        if (currentModInfo != null && currentModInfo.equals(info)) {
            boolean needsUpdate = false;
            if (MODUPDATES != null) {
                for (ModUpdate modUpdate : MODUPDATES) {
                    if (modUpdate.info.equals(info)) {
                        needsUpdate = true;
                        break;
                    }
                }
            }
            if (needsUpdate) {
                modBannerNoticePanel.add(modUpdateBanner);
            } else {
                modBannerNoticePanel.remove(modUpdateBanner);
            }
        }
    }

    public void startCheckingForMTSUpdate()
    {
        new Thread(() -> {
            try {
                // Check for ModTheSpire updates
                UpdateChecker updateChecker = new GithubUpdateChecker("kiooeht", "ModTheSpire");
                if (updateChecker.isNewerVersionAvailable(Loader.MTS_VERSION)) {
                    URL latestReleaseURL = updateChecker.getLatestReleaseURL();
                    setMTSUpdateAvailable(latestReleaseURL);
                    return;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("ERROR: ModTheSpire: " + e.getMessage());
            } catch (IOException e) {
                // NOP
            }
        }).start();
    }

    public void startCheckingForModUpdates(JButton updatesBtn)
    {
        updatesBtn.setIcon(ICON_LOAD);

        new Thread(() -> {
            // Set all icons to checking
            for (int i=0; i<info.length; ++i) {
                if (info[i].UpdateJSON == null || info[i].UpdateJSON.isEmpty()) {
                    continue;
                }

                modList.setUpdateIcon(info[i], UpdateIconType.CHECKING);
            }

            // Check for mod updates
            boolean anyNeedUpdates = false;
            MODUPDATES = new ArrayList<>();
            for (int i=0; i<info.length; ++i) {
                if (info[i].UpdateJSON == null || info[i].UpdateJSON.isEmpty()) {
                    continue;
                }
                try {
                    UpdateChecker updateChecker = new GithubUpdateChecker(info[i].UpdateJSON);
                    if (updateChecker.isNewerVersionAvailable(info[i].ModVersion)) {
                        anyNeedUpdates = true;
                        MODUPDATES.add(new ModUpdate(info[i], updateChecker.getLatestReleaseURL(), updateChecker.getLatestDownloadURL()));
                        setModUpdateBanner(info[i]);
                        revalidate();
                        repaint();
                        modList.setUpdateIcon(info[i], UpdateIconType.UPDATE_AVAILABLE);
                    } else {
                        modList.setUpdateIcon(info[i], UpdateIconType.UPTODATE);
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("ERROR: " + info[i].Name + ": " + e.getMessage());
                } catch (IOException e) {
                    // NOP
                    System.out.println(e);
                }
            }

            if (anyNeedUpdates) {
                updatesBtn.setIcon(ICON_WARNING);
                updatesBtn.setToolTipText("Mod updates are available.");
                for (ActionListener listener : updatesBtn.getActionListeners()) {
                    updatesBtn.removeActionListener(listener);
                }
                updatesBtn.addActionListener(e -> {
                    UpdateWindow win = new UpdateWindow(this);
                    win.setVisible(true);
                });
            } else {
                updatesBtn.setIcon(ICON_UPDATE);
            }
        }).start();
    }

    public void warnAboutMissingVersions()
    {
        for (ModInfo modInfo : info) {
            if (modInfo.ModVersion == null) {
                JOptionPane.showMessageDialog(null,
                    modInfo.Name + " has a missing or bad version number.\nGo yell at the author to fix it.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}

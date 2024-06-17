package com.evacipated.cardcrawl.modthespire.ui;

import com.alexandriasoftware.swing.JSplitButton;
import com.evacipated.cardcrawl.modthespire.*;
import com.evacipated.cardcrawl.modthespire.lib.ConfigUtils;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

public class ModSelectWindow extends JFrame
{
    /**
     *
     */
    private static final long serialVersionUID = -8232997068791248057L;
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 500;
    private static final String PLAY_OPTION = "Play";
    private static final String JAR_DUMP_OPTION = "Dump Patched Jar";
    private static final String PACKAGE_OPTION = "Create Prepackaged Jar";

    static final Image APP_ICON = Toolkit.getDefaultToolkit().createImage(ModSelectWindow.class.getResource("/assets/icon.png"));
    static final Icon ICON_SETTINGS = new ImageIcon(ModSelectWindow.class.getResource("/assets/settings.gif"));
    static final Icon ICON_UPDATE   = new ImageIcon(ModSelectWindow.class.getResource("/assets/update.gif"));
    static final Icon ICON_FOLDER   = new ImageIcon(ModSelectWindow.class.getResource("/assets/folder.gif"));
    static final Icon ICON_FILE     = new ImageIcon(ModSelectWindow.class.getResource("/assets/file.gif"));
    static final Icon ICON_LOAD     = new ImageIcon(ModSelectWindow.class.getResource("/assets/ajax-loader.gif"));
    static final Icon ICON_GOOD     = new ImageIcon(ModSelectWindow.class.getResource("/assets/good.gif"));
    static final Icon ICON_WARNING  = new ImageIcon(ModSelectWindow.class.getResource("/assets/warning.gif"));
    static final Icon ICON_ERROR    = new ImageIcon(ModSelectWindow.class.getResource("/assets/error.gif"));
    static final Icon ICON_WORKSHOP = new ImageIcon(ModSelectWindow.class.getResource("/assets/workshop.gif"));
    static final Icon ICON_WORKSHOP_HOVER = new ImageIcon(ModSelectWindow.class.getResource("/assets/workshop_hover.gif"));

    private ModInfo[] info;
    private boolean showingLog = false;
    private boolean isMaximized = false;
    private boolean isCentered = false;
    private Rectangle location;
    private JSplitButton playBtn;
    private JPopupMenu playBtnPopup;

    private JModPanelCheckBoxList modList;
    private JComboBox<String> profilesList;

    private ModInfo currentModInfo;
    private TitledBorder name;
    private JLabel modID;
    private JTextArea authors;
    private JLabel modVersion;
    private JTextArea status;
    private JLabel mtsVersion;
    private JLabel stsVersion;
    private JTextArea description;
    private JTextArea credits;
    private JLabel dependencies;

    private JPanel bannerNoticePanel;
    private JLabel mtsUpdateBanner;
    private JLabel betaWarningBanner;

    private JPanel modBannerNoticePanel;
    private JLabel modUpdateBanner;

    static List<ModUpdate> MODUPDATES;

    static float UI_SCALE = 1f;
    static boolean MODDER_MODE = false;
    public static String stsDistributor = null;

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
        FlatLaf.registerCustomDefaultsSource("mtsThemes");
        rootPane.putClientProperty("JRootPane.titleBarShowTitle", false);
        setTheme(ModTheSpire.MTS_CONFIG.getString("uiTheme", "Light"));
        UI_SCALE = ModTheSpire.MTS_CONFIG.getFloat("uiScale", 1f);
        if (UI_SCALE != 1f) {
            SwingDPI.setScaleFactor(UI_SCALE);
            SwingDPI.setScaleApplied(true);
        }
        MODDER_MODE = ModTheSpire.MTS_CONFIG.getBool("modder", false);

        setIconImage(APP_ICON);

        info = modInfos;
        readWindowPosSize();
        setupDetectMaximize();
        initUI(skipLauncher);
        if (ModTheSpire.MTS_CONFIG.getBool("maximize")) {
            isMaximized = true;
            this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        }
    }

    public void setModInfos(ModInfo[] modInfos){
        info = modInfos;

        //Refresh the modlist panel
        ModList mods = ModList.loadModLists();
        mods.loadModsInOrder((DefaultListModel<ModPanel>) modList.getModel(), info, modList);
    }

    static void setTheme(String theme)
    {
        LookAndFeel laf;
        switch (theme) {
            case "Light":
                laf = new FlatLightLaf();
                break;
            case "Dark":
                laf = new FlatDarkLaf();
                break;
            default:
                laf = null;
                break;
        }
        if (laf != null) {
            if (FlatLaf.setup(laf)) {
                FlatLaf.updateUI();
            }
        }
    }

    private void readWindowPosSize()
    {
        // Sanity check values
        if (ModTheSpire.MTS_CONFIG.getInt("width") < DEFAULT_WIDTH) {
            ModTheSpire.MTS_CONFIG.setInt("width", DEFAULT_WIDTH);
        }
        if (ModTheSpire.MTS_CONFIG.getInt("height") < DEFAULT_HEIGHT) {
            ModTheSpire.MTS_CONFIG.setInt("height", DEFAULT_HEIGHT);
        }
        location = new Rectangle();
        location.width = ModTheSpire.MTS_CONFIG.getInt("width");
        location.height = ModTheSpire.MTS_CONFIG.getInt("height");
        if (ModTheSpire.MTS_CONFIG.getString("x").equals("center") || ModTheSpire.MTS_CONFIG.getString("y").equals("center")) {
            isCentered = true;
        } else {
            isCentered = false;
            location.x = ModTheSpire.MTS_CONFIG.getInt("x");
            location.y = ModTheSpire.MTS_CONFIG.getInt("y");
            if (!isInScreenBounds(location)) {
                ModTheSpire.MTS_CONFIG.setString("x", "center");
                ModTheSpire.MTS_CONFIG.setString("y", "center");
                isCentered = true;
            }
        }

        try {
            ModTheSpire.MTS_CONFIG.save();
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
        setTitle("ModTheSpire");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(true);
        setJMenuBar(makeMenuBar());

        rootPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        setLayout(new BorderLayout());
        getContentPane().setPreferredSize(new Dimension(location.width, location.height));

        getContentPane().add(makeSplitPanel(), BorderLayout.CENTER);
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

    private JMenuBar makeMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("ModTheSpire");
        JMenuItem item;
        menu.setMnemonic(KeyEvent.VK_M);

        // Open Folder menu
        JMenu openMenu = new JMenu("Open");
        openMenu.setMnemonic(KeyEvent.VK_O);
        item = new JMenuItem("Slay the Spire");
        try {
            String surl = new File(ModTheSpire.STS_JAR).toURI().toURL().toString();
            URL url = new URL("jar:" + surl + "!/images/ui/icon.png");
            ImageIcon icon = new ImageIcon(url);
            icon.setImage(icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            item.setIcon(icon);
        } catch (Exception ignored) {}
        item.setMnemonic(KeyEvent.VK_S);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_MASK));
        item.addActionListener((ActionEvent event) -> {
            openFolder(Paths.get(ModTheSpire.STS_JAR).toFile().getParent(), false);
        });
        openMenu.add(item);
        item = new JMenuItem("Workshop Mods", ICON_WORKSHOP);
        item.setMnemonic(KeyEvent.VK_W);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_MASK));
        item.addActionListener((ActionEvent event) -> {
            Optional<String> installPath = Arrays.stream(info)
                .filter(x -> x.workshopInfo != null)
                .map(x -> x.workshopInfo.getInstallPath())
                .findFirst();
            if (installPath.isPresent()) {
                Path path = Paths.get(installPath.get());
                openFolder(path.getParent().toString(), false);
            }
        });
        item.setEnabled(Arrays.stream(info).anyMatch(x -> x.workshopInfo != null));
        openMenu.add(item);
        item = new JMenuItem("Local Mods", ICON_FOLDER);
        item.setMnemonic(KeyEvent.VK_M);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.CTRL_MASK));
        item.addActionListener((ActionEvent event) -> {
            openFolder(ModTheSpire.MOD_DIR, true);
        });
        openMenu.add(item);
        item = new JMenuItem("Config Files", ICON_SETTINGS);
        item.setMnemonic(KeyEvent.VK_C);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.CTRL_MASK));
        item.addActionListener((ActionEvent event) -> {
            openFolder(ConfigUtils.CONFIG_DIR, false);
        });
        openMenu.add(item);
        item = new JMenuItem("Log Files", ICON_FILE);
        item.setMnemonic(KeyEvent.VK_L);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.CTRL_MASK));
        item.addActionListener((ActionEvent event) -> {
            openFolder("sendToDevs/", false);
        });
        openMenu.add(item);

        // Check for Updates button
        item = new JMenuItem("Check for Mod Updates", ICON_UPDATE);
        final JMenuItem updateItem = item;
        item.addActionListener(event -> {
            startCheckingForModUpdates(updateItem);
        });
        // TODO rework updates and add menu item
        //menu.add(item);

        // Settings
        item = new JMenuItem("Settings...");
        item.setMnemonic(KeyEvent.VK_S);
        item.addActionListener((ActionEvent event) -> {
            JDialog settingsWindow = new SettingsWindow(ModSelectWindow.this);
            settingsWindow.pack();
            settingsWindow.setLocationRelativeTo(ModSelectWindow.this);
            settingsWindow.setVisible(true);
        });
        menu.add(item);
        menu.addSeparator();
        // About
        item = new JMenuItem("About");
        item.setMnemonic(KeyEvent.VK_A);
        item.addActionListener((ActionEvent event) -> {
            JDialog aboutWindow = new AboutWindow(ModSelectWindow.this);
            aboutWindow.pack();
            aboutWindow.setLocationRelativeTo(ModSelectWindow.this);
            aboutWindow.setVisible(true);
        });
        menu.add(item);
        menuBar.add(menu);

        // Edit menu
        menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        // Mod List editor
        item = new JMenuItem("Mod Lists...");
        item.setMnemonic(KeyEvent.VK_L);
        item.addActionListener(e -> {
            JDialog modListEditor = new ModListEditorWindow(ModSelectWindow.this);
            modListEditor.pack();
            modListEditor.setLocationRelativeTo(ModSelectWindow.this);
            modListEditor.setVisible(true);
        });
        menu.add(item);
        menu.addSeparator();
        // Enable all mods
        item = new JMenuItem("Enable All Mods");
        item.setMnemonic(KeyEvent.VK_E);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK));
        item.addActionListener(e -> {
            modList.enableAllMods(true);
            modList.repaint();
        });
        menu.add(item);
        // Disable all mods
        item = new JMenuItem("Disable All Mods");
        item.setMnemonic(KeyEvent.VK_D);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK));
        item.addActionListener(e -> {
            modList.enableAllMods(false);
            modList.repaint();
        });
        menu.add(item);
        menuBar.add(menu);

        menuBar.add(openMenu);

        return menuBar;
    }
    private static void openFolder(String path, boolean createIfNotExist)
    {
        try {
            File file = new File(path);
            if (!file.exists()) {
                if (createIfNotExist) {
                    file.mkdir();
                } else {
                    return;
                }
            }
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSplitPane makeSplitPanel()
    {
        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            true,
            makeModListPanel(),
            makeInfoPanel()
        );
        // Load divider location
        EventQueue.invokeLater(() -> {
            if (ModTheSpire.MTS_CONFIG.has("split")) {
                float splitLocation = ModTheSpire.MTS_CONFIG.getFloat("split");
                split.setDividerLocation(splitLocation);
            }
        });
        // Save divider location when it changes
        split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
            if (((Integer) evt.getOldValue()) == -1) {
                return;
            }

            Integer v = (Integer) evt.getNewValue();
            float percent = v / (float) (split.getWidth() - split.getDividerSize());
            ModTheSpire.MTS_CONFIG.setFloat("split", percent);
            try {
                ModTheSpire.MTS_CONFIG.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return split;
    }

    private JPanel makeModListPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(220, 300));
        panel.setMinimumSize(new Dimension(180, 300));

        // Mod List
        DefaultListModel<ModPanel> model = new DefaultListModel<>();
        modList = new JModPanelCheckBoxList(this, model);
        ModList mods = ModList.loadModLists();
        mods.loadModsInOrder(model, info, modList);
        modList.publishBoxChecked();

        JScrollPane modScroller = new JScrollPane(modList);
        panel.add(modScroller, BorderLayout.CENTER);

        // Play button
        playBtn = new JSplitButton() {
            @Override
            public void updateUI()
            {
                super.updateUI();

                setArrowColor(UIManager.getColor("ComboBox.buttonArrowColor"));
                setDisabledArrowColor(UIManager.getColor("ComboBox.buttonDisabledArrowColor"));

                JPopupMenu popupMenu = getPopupMenu();
                if (popupMenu != null) {
                    popupMenu.updateUI();
                    for (Component component : popupMenu.getComponents()) {
                        if (component instanceof JComponent) {
                            ((JComponent) component).updateUI();
                        }
                    }
                }
            }
        };
        playBtn.setMnemonic(KeyEvent.VK_P);
        playBtn.updateUI(); // forces arrow color update
        setPlayButtonLabel();
        playBtnPopup = new JPopupMenu();
        {
            ButtonGroup group = new ButtonGroup();
            JMenuItem play = playBtnPopup.add(new JRadioButtonMenuItem(new AbstractAction(PLAY_OPTION)
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ModTheSpire.OUT_JAR = false;
                    ModTheSpire.PACKAGE = false;
                    setPlayButtonLabel();
                }
            }));
            group.add(play);
            JMenuItem outJar = playBtnPopup.add(new JRadioButtonMenuItem(new AbstractAction(JAR_DUMP_OPTION)
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ModTheSpire.OUT_JAR = true;
                    ModTheSpire.PACKAGE = false;
                    setPlayButtonLabel();
                }
            }));
            group.add(outJar);
            JMenuItem packageJar = playBtnPopup.add(new JRadioButtonMenuItem(new AbstractAction(PACKAGE_OPTION)
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ModTheSpire.OUT_JAR = false;
                    ModTheSpire.PACKAGE = true;
                    setPlayButtonLabel();
                }
            }));
            group.add(packageJar);

            if (ModTheSpire.PACKAGE) {
                packageJar.setSelected(true);
            } else if (ModTheSpire.OUT_JAR) {
                outJar.setSelected(true);
            } else {
                play.setSelected(true);
            }
        }
        setPlayButtonOptions(MODDER_MODE);
        playBtn.addButtonClickedActionListener((ActionEvent e) -> {
            showingLog = true;
            playBtn.setEnabled(false);

            rootPane.putClientProperty("JRootPane.titleBarShowTitle", true);
            setJMenuBar(null);
            this.getContentPane().removeAll();

            JTextArea textArea = new JTextArea();
            textArea.setLineWrap(true);
            textArea.setFont(new Font("monospaced", Font.PLAIN, SwingDPI.scale(12)));
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

            Thread t = new Thread(() -> {
                ModTheSpire.runMods(modList.getCheckedModIDs());
                if (ModTheSpire.CLOSE_WHEN_FINISHED) {
                    ModTheSpire.closeWindow();
                }
            });
            t.start();
        });
        if (ModTheSpire.STS_BETA && !ModTheSpire.allowBeta) {
            playBtn.setEnabled(false);
        }
        panel.add(playBtn, BorderLayout.SOUTH);

        profilesList = new JComboBox<>();
        updateProfilesList();

        JTextField filter = new JTextField();
        filter.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search");
        filter.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSearchIcon());
        filter.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        // Focus filter text field  on Ctrl+F
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK),
            "search"
        );
        rootPane.getActionMap().put("search", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                filter.requestFocusInWindow();
            }
        });

        profilesList.addActionListener((ActionEvent event) -> {
            String profileName = (String) profilesList.getSelectedItem();
            if (profileName == null) return;
            ModList newList = new ModList(profileName);
            DefaultListModel<ModPanel> newModel = (DefaultListModel<ModPanel>) modList.getModel();
            newList.loadModsInOrder(newModel, info, modList);
            filter.setText("");

            ModList.setDefaultList(profileName);
        });
        if (ModTheSpire.profileArg != null) {
            profilesList.setSelectedItem(ModTheSpire.profileArg);
        } else {
            profilesList.setSelectedItem(ModList.getDefaultList());
        }

        Runnable filterModList = () -> {
            String filterText = filter.getText().trim().toLowerCase();
            String[] filterKeys = filterText.length() == 0 ? null : filterText.split("\\s+");
            for (int i = 0; i < model.size(); i++) {
                ModPanel modPanel = model.getElementAt(i);
                modPanel.filter(filterKeys);
            }
            modList.updateUI();
        };
        filter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterModList.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterModList.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterModList.run();
            }
        });

        JPanel topPanel = new JPanel(new GridLayout(0, 1));
        topPanel.add(profilesList);
        topPanel.add(filter);
        panel.add(topPanel, BorderLayout.NORTH);

        return panel;
    }

    void saveCurrentModList()
    {
        Thread tCfg = new Thread(() -> {
            // Save new load order cfg
            ModList.save(ModList.getDefaultList(), modList.getCheckedMods());
        });
        tCfg.start();
    }

    void updateProfilesList()
    {
        updateProfilesList(null, null);
    }

    void updateProfilesList(String oldName, String newName)
    {
        String selected = (String) profilesList.getSelectedItem();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(ModList.getAllModListNames().toArray(new String[0]));
        profilesList.setModel(model);

        if (oldName != null && newName != null) {
            if (oldName.equals(selected)) {
                selected = newName;
            }
        }

        if (model.getIndexOf(selected) == -1) {
            selected = null;
        }
        if (selected != null) {
            profilesList.setSelectedItem(selected);
        } else {
            profilesList.setSelectedIndex(0);
        }
    }

    public void swapModList(String listToLoad){
        // Sanity check
        if (listToLoad == null){
            return;
        }

        profilesList.setSelectedItem(listToLoad);
    }

    private void setPlayButtonLabel()
    {
        playBtn.setText(
            ModTheSpire.PACKAGE ? PACKAGE_OPTION :
                ModTheSpire.OUT_JAR ? JAR_DUMP_OPTION :
                    PLAY_OPTION
        );
        playBtn.setDisplayedMnemonicIndex(playBtn.getText().indexOf('P'));
    }

    private void setPlayButtonOptions(boolean enabled)
    {
        if (enabled) {
            playBtn.setPopupMenu(playBtnPopup);
        } else {
            playBtn.setPopupMenu(null);
        }
        playBtn.repaint();
    }

    void updateModderMode(boolean enabled)
    {
        setPlayButtonOptions(enabled);
        modID.setVisible(enabled);
        refreshDependenciesView(enabled);
    }

    private JPanel makeInfoPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setMinimumSize(new Dimension(400, 300));

        // Top mod banner panel
        panel.add(makeModBannerPanel(), BorderLayout.NORTH);

        // Bottom status panel
        panel.add(makeStatusPanel(), BorderLayout.SOUTH);

        // Main info panel
        JPanel infoPanel = new JPanel();
        name = BorderFactory.createTitledBorder("Mod Info");
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            name,
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        infoPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        // Right panel
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 0.25;

        modID = makeInfoLabelField("ID", " ");
        infoPanel.add(modID, c);
        modID.setVisible(MODDER_MODE);
        ++c.gridy;

        modVersion = makeInfoLabelField("Version", " ");
        infoPanel.add(modVersion, c);
        ++c.gridy;

        mtsVersion = makeInfoLabelField("ModTheSpire Version", " ");
        infoPanel.add(mtsVersion, c);
        ++c.gridy;

        stsVersion = makeInfoLabelField("Slay the Spire Version", " ");
        infoPanel.add(stsVersion, c);
        ++c.gridy;

        dependencies = makeInfoLabelField("Dependencies", " ");
        infoPanel.add(dependencies, c);
        ++c.gridy;

        status = makeInfoTextAreaField("Status", " ");
        infoPanel.add(status, c);

        // Left panel
        c.gridheight = c.gridy + 1;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.75;
        c.weighty = 1;

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        description = makeInfoTextAreaField("Description", " ");
        leftPanel.add(makeTextAreaScrollable(description), BorderLayout.CENTER);

        JPanel leftCredits = new JPanel();
        leftCredits.setLayout(new BoxLayout(leftCredits, BoxLayout.Y_AXIS));

        authors = makeInfoTextAreaField("Author(s)", " ");
        leftCredits.add(authors);

        credits = makeInfoTextAreaField("Additional Credits", " ");
        leftCredits.add(credits);

        leftPanel.add(leftCredits, BorderLayout.SOUTH);
        infoPanel.add(leftPanel, c);
        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private JLabel makeInfoLabelField(String title, String value)
    {
        JLabel label = new JLabel(value);

        TitledBorder border = BorderFactory.createTitledBorder(title);
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
        label.setBorder(BorderFactory.createCompoundBorder(
            border,
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        label.setEditable(false);
        label.setFocusable(false);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        label.setOpaque(false);

        return label;
    }

    private JScrollPane makeTextAreaScrollable(JTextArea textArea)
    {
        Border border = textArea.getBorder();
        textArea.setBorder(null);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(border);
        return scrollPane;
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
        JLabel sts_version = new JLabel("Slay the Spire version: " + ModTheSpire.STS_VERSION);
        if (ModTheSpire.STS_BETA) {
            sts_version.setText(sts_version.getText() + " BETA");
        }
        sts_version.setText(sts_version.getText() + String.format(" (%s)", stsDistributor));
        sts_version.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(sts_version, BorderLayout.EAST);

        return panel;
    }

    private JPanel makeTopPanel()
    {
        bannerNoticePanel = new JPanel();
        bannerNoticePanel.setLayout(new GridLayout(0, 1));

        if (ModTheSpire.STS_BETA) {
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
        ModTheSpire.MTS_CONFIG.setInt("width", d.width);
        ModTheSpire.MTS_CONFIG.setInt("height", d.height);
        try {
            ModTheSpire.MTS_CONFIG.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void saveWindowMaximize()
    {
        ModTheSpire.MTS_CONFIG.setBool("maximize", isMaximized);
        try {
            ModTheSpire.MTS_CONFIG.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void saveWindowLocation()
    {
        Point loc = getLocationOnScreen();
        ModTheSpire.MTS_CONFIG.setInt("x", loc.x);
        ModTheSpire.MTS_CONFIG.setInt("y", loc.y);
        try {
            ModTheSpire.MTS_CONFIG.save();
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
        modID.setText(info.ID);
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
        description.setCaretPosition(0);
        credits.setText(info.Credits);
        refreshDependenciesView(MODDER_MODE);

        status.setText(info.statusMsg);

        setModUpdateBanner(info);

        repaint();
    }

    private void refreshDependenciesView(boolean modderMode)
    {
        if (currentModInfo != null) {
            dependencies.setText(currentModInfo.getDependenciesRepr(!modderMode));
        }
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
                if (updateChecker.isNewerVersionAvailable(ModTheSpire.MTS_VERSION)) {
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

    public void startCheckingForModUpdates(AbstractButton updatesBtn)
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

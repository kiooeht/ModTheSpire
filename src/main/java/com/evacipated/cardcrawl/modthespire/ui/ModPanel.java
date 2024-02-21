package com.evacipated.cardcrawl.modthespire.ui;

import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.ModTheSpire;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("serial")
public class ModPanel extends JPanel
{
    private static final Color lightRed = new Color(229,115,115);
    private static final Color lightOrange = new Color(255, 159, 0); // orange peel (https://en.wikipedia.org/wiki/Shades_of_orange#Orange_peel)
    private static final Color lightYellow = new Color(255, 238, 88);
    private static final Icon ICON_BLANK = new BlankIcon(ModSelectWindow.ICON_WORKSHOP.getIconWidth(), ModSelectWindow.ICON_WORKSHOP.getIconHeight());

    public ModInfo info;
    public File modFile;
    public JCheckBox checkBox;
    private InfoPanel infoPanel;
    private JPanel iconsPanel;
    List<StatusIconButton> icons = new ArrayList<>();
    private JButton update;
    private boolean isFilteredOut = false;
    
    private static boolean dependenciesChecked(ModInfo info, JModPanelCheckBoxList parent) {
        ModInfo.Dependency[] dependencies = info.Dependencies;
        boolean[] checked = new boolean[dependencies.length]; // initializes to false
        for (int i = 0; i < parent.getModel().getSize(); i++) {
            ModPanel panel = parent.getModel().getElementAt(i);
            for (int j = 0; j < dependencies.length; j++) {
                if (panel.info != null && dependencies[j].compare(panel.info) && panel.checkBox.isSelected()) {
                    checked[j] = true;
                }
            }
        }
        boolean allChecked = true;
        for (int i = 0; i < checked.length; i++) {
            if (!checked[i]) {
                allChecked = false;
            }
        }

        return allChecked;
    }
    
    private static String[] missingDependencies(ModInfo info, JModPanelCheckBoxList parent) {
        ModInfo.Dependency[] dependencies = info.Dependencies;
        boolean[] checked = new boolean[dependencies.length]; // initializes to false
        for (int i = 0; i < parent.getModel().getSize(); i++) {
            ModPanel panel = parent.getModel().getElementAt(i);
            for (int j = 0; j < dependencies.length; j++) {
                if (panel.info != null && dependencies[j].compare(panel.info) && panel.checkBox.isSelected()) {
                    checked[j] = true;
                }
            }
        }
        java.util.List<String> missing = new ArrayList<String>();
        for (int i = 0; i < checked.length; i++) {
            if (!checked[i]) {
                missing.add(dependencies[i].toString());
            }
        }
        String[] returnType = new String[missing.size()];
        return missing.toArray(returnType);
    }
    
    public ModPanel(ModInfo info, File modFile, JModPanelCheckBoxList parent) {
        this.info = info;
        this.modFile = modFile;
        checkBox = new JCheckBox();
        setLayout(new BorderLayout());
        infoPanel = new InfoPanel();

        add(checkBox, BorderLayout.WEST);
        add(infoPanel, BorderLayout.CENTER);

        // TODO display all applicable icons:
        // - FILE (local mod)
        // - WORKSHOP (workshop mod)
        // Icons
        iconsPanel = new JPanel();
        iconsPanel.setLayout(new BoxLayout(iconsPanel, BoxLayout.X_AXIS));
        iconsPanel.setOpaque(false);
        iconsPanel.setBackground(null);
        iconsPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        // Update icon
        update = new JButton();
        update.setRolloverIcon(ModSelectWindow.ICON_WARNING);
        if (info.isWorkshop) {
            setUpdateIcon(ModSelectWindow.UpdateIconType.WORKSHOP);
        } else if (info.UpdateJSON != null && !info.UpdateJSON.isEmpty()) {
            setUpdateIcon(ModSelectWindow.UpdateIconType.CAN_CHECK);
        } else {
            setUpdateIcon(ModSelectWindow.UpdateIconType.NONE);
        }
        update.addActionListener(event -> {
            // TODO
        });
//        icons.add(update);
        // Local icon
        if (isLocalMod()) {
            StatusIconButton local = new StatusIconButton(ModSelectWindow.ICON_FOLDER);
            icons.add(local);
        }
        // Workshop icon
        if (isWorkshopMod()) {
            StatusIconButton workshop = new StatusIconButton(ModSelectWindow.ICON_WORKSHOP, ModSelectWindow.ICON_WORKSHOP_HOVER);
            workshop.addActionListener(event -> {
                try {
                    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(new URI("steam://url/CommunityFilePage/" + info.workshopInfo.getID()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            icons.add(workshop);
        }

        for (JButton icon : icons) {
            icon.setHorizontalAlignment(JLabel.CENTER);
            icon.setVerticalAlignment(JLabel.CENTER);
            icon.setOpaque(false);
            icon.setBackground(null);
            icon.setBorder(new EmptyBorder(0, 0, 0, 0));
            iconsPanel.add(icon);
            iconsPanel.add(Box.createRigidArea(new Dimension(4, 0)));
        }
        add(iconsPanel, BorderLayout.EAST);

        setBorder(new MatteBorder(0, 0, 1, 0, Color.darkGray));

        checkBox.addItemListener((event) -> {
            parent.publishBoxChecked();
        });
        parent.publishBoxChecked();
    }

    public void recalcModWarnings(JModPanelCheckBoxList parent)
    {
        info.statusMsg = " ";
        checkBox.setBackground(null);
        infoPanel.setBackground(null);

        if (info.MTS_Version == null) {
            checkBox.setEnabled(false);
            Color error = UIManager.getColor("Component.error.focusedBorderColor");
            if (error == null) {
                error = lightRed;
            }
            checkBox.setBackground(error);
            infoPanel.setBackground(error);
            this.putClientProperty("JComponent.outline", "error");
            info.statusMsg = "This mod is missing a valid ModTheSpire version number.";
            return;
        }
        if (info.MTS_Version.compareTo(ModTheSpire.MTS_VERSION) > 0) {
            checkBox.setEnabled(false);
            Color error = UIManager.getColor("Component.error.focusedBorderColor");
            if (error == null) {
                error = lightRed;
            }
            checkBox.setBackground(error);
            infoPanel.setBackground(error);
            info.statusMsg = "This mod requires ModTheSpire v" + info.MTS_Version + " or higher.";
            return;
        }

        if (checkBox.isSelected() && !dependenciesChecked(info, parent)) {
            Color warning = UIManager.getColor("Component.warning.focusedBorderColor");
            if (warning == null) {
                warning = lightOrange;
            }
            checkBox.setBackground(warning);
            infoPanel.setBackground(warning);
            String[] missingDependencies = missingDependencies(info, parent);
            StringBuilder tooltip = new StringBuilder();
            tooltip.append("Missing dependencies: [");
            tooltip.append(String.join(", ", missingDependencies));
            tooltip.append("]");
            info.statusMsg = tooltip.toString();
        }
        if (ModTheSpire.STS_VERSION != null && info.STS_Version != null && !ModTheSpire.STS_VERSION.equals(info.STS_Version)) {
            //checkBox.setBackground(lightYellow);
            //infoPanel.setBackground(lightYellow);
            if (Objects.equals(info.statusMsg, " ")) {
                info.statusMsg = "This mod explicitly supports StS " + info.STS_Version + ".\n" +
                    "You are running StS " + ModTheSpire.STS_VERSION + ".\n" +
                    "You may encounter problems running it.";
            }
        }
    }

    public boolean isSelected()
    {
        return checkBox.isEnabled() && checkBox.isSelected();
    }

    public void setSelected(boolean b)
    {
        if (checkBox.isEnabled()) {
            checkBox.setSelected(b);
        }
    }

    @Override
    public void setBackground(Color bg)
    {
        super.setBackground(bg);

        if (iconsPanel != null) {
            iconsPanel.setBackground(null);
        }
        if (icons != null) {
            for (StatusIconButton icon : icons) {
                icon.setBackground(null);
            }
        }
    }

    public synchronized void setUpdateIcon(ModSelectWindow.UpdateIconType type)
    {
        switch (type) {
            case NONE:
                update.setIcon(ModSelectWindow.ICON_FILE);
                break;
            case CAN_CHECK:
                update.setIcon(ModSelectWindow.ICON_UPDATE);
                break;
            case CHECKING:
                update.setIcon(ModSelectWindow.ICON_LOAD);
                break;
            case UPDATE_AVAILABLE:
                update.setIcon(ModSelectWindow.ICON_WARNING);
                break;
            case UPTODATE:
                update.setIcon(ModSelectWindow.ICON_GOOD);
                break;
            case WORKSHOP:
                update.setIcon(ModSelectWindow.ICON_WORKSHOP);
        }
    }

    private boolean isLocalMod()
    {
        return !info.isWorkshop;
    }

    private boolean isWorkshopMod()
    {
        return info.workshopInfo != null;
    }

    public void filter(String[] filterKeys) {
        if (filterKeys == null) {
            isFilteredOut = false;
            return;
        }

        String workshopInfoKey = "";

        if (info.workshopInfo != null) {
            workshopInfoKey = String.format("%s %s", info.workshopInfo.getTitle(), String.join(" ", info.workshopInfo.getTags()));
        }

        String modInfoKey = String.format("%s %s %s %s", info.ID, info.Name, String.join(" ", info.Authors), workshopInfoKey).toLowerCase();
        boolean isFilteredOut = false;
        for (String filterKey : filterKeys) {
            if (!modInfoKey.contains(filterKey)) {
                isFilteredOut = true;
                break;
            }
        }
        this.isFilteredOut = isFilteredOut;
    }

    public boolean isFilteredOut() {
        return isFilteredOut;
    }

    public class InfoPanel extends JPanel
    {
        JLabel name = new JLabel();
        JLabel version = new JLabel();

        public InfoPanel()
        {
            setLayout(new BorderLayout());

            name.setText(info.Name);
            name.putClientProperty("FlatLaf.styleClass", "h3");
            add(name, BorderLayout.CENTER);

            version.putClientProperty("FlatLaf.styleClass", "small");
            if (info.ModVersion != null) {
                version.setText(info.ModVersion.toString());
            } else {
                version.setText("missing version");
            }
            add(version, BorderLayout.SOUTH);
        }

        @Override
        public Dimension getPreferredSize()
        {
            Dimension d = super.getPreferredSize();
            return new Dimension(0, d.height);
        }

        @Override
        public void setBackground(Color c)
        {
            super.setBackground(c);
            if (name != null) {
                name.setBackground(c);
            }
            if (version != null) {
                version.setBackground(c);
            }
        }
    }

    private static class BlankIcon implements Icon
    {
        private final int width;
        private final int height;

        BlankIcon(int w, int h)
        {
            width = w;
            height = h;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {}

        @Override
        public int getIconWidth()
        {
            return width;
        }

        @Override
        public int getIconHeight()
        {
            return height;
        }
    }
}

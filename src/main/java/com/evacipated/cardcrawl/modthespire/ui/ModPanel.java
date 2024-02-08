package com.evacipated.cardcrawl.modthespire.ui;

import com.evacipated.cardcrawl.modthespire.ModTheSpire;
import com.evacipated.cardcrawl.modthespire.ModInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class ModPanel extends JPanel
{
    private static final Color lightRed = new Color(229,115,115);
    private static final Color lightOrange = new Color(255, 159, 0); // orange peel (https://en.wikipedia.org/wiki/Shades_of_orange#Orange_peel)
    private static final Color lightYellow = new Color(255, 238, 88);
    public ModInfo info;
    public File modFile;
    public JCheckBox checkBox;
    private InfoPanel infoPanel;
    private JLabel update = new JLabel();
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

        // Update icon
        update.setHorizontalAlignment(JLabel.CENTER);
        update.setVerticalAlignment(JLabel.CENTER);
        update.setOpaque(true);
        update.setBorder(new EmptyBorder(0, 0, 0, 4));
        if (info.isWorkshop) {
            setUpdateIcon(ModSelectWindow.UpdateIconType.WORKSHOP);
        } else if (info.UpdateJSON != null && !info.UpdateJSON.isEmpty()) {
            setUpdateIcon(ModSelectWindow.UpdateIconType.CAN_CHECK);
        } else {
            setUpdateIcon(ModSelectWindow.UpdateIconType.NONE);
        }
        add(update, BorderLayout.EAST);

        setBorder(new MatteBorder(0, 0, 1, 0, Color.darkGray));

        checkBox.addItemListener((event) -> {
            parent.publishBoxChecked();
        });
        parent.publishBoxChecked();

        setToolTipText("Double click to add an alias for this mod");
    }
    
    public void recalcModWarnings(JModPanelCheckBoxList parent)
    {
        info.statusMsg = " ";
        checkBox.setBackground(Color.WHITE);
        infoPanel.setBackground(Color.WHITE);

        if (info.MTS_Version == null) {
            checkBox.setEnabled(false);
            checkBox.setBackground(lightRed);
            infoPanel.setBackground(lightRed);
            info.statusMsg = "This mod is missing a valid ModTheSpire version number.";
            return;
        }
        if (info.MTS_Version.compareTo(ModTheSpire.MTS_VERSION) > 0) {
            checkBox.setEnabled(false);
            checkBox.setBackground(lightRed);
            infoPanel.setBackground(lightRed);
            info.statusMsg = "This mod requires ModTheSpire v" + info.MTS_Version + " or higher.";
            return;
        }

        if (checkBox.isSelected() && !dependenciesChecked(info, parent)) {
            checkBox.setBackground(lightOrange);
            infoPanel.setBackground(lightOrange);
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
            if (info.statusMsg == " ") {
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

    public synchronized void setUpdateIcon(ModSelectWindow.UpdateIconType type)
    {
        switch (type) {
            case NONE:
                update.setIcon(null);
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

    public void filter(String[] filterKeys) {
        if (filterKeys == null) {
            isFilteredOut = false;
            return;
        }

        String workshopInfoKey = "";

        if (info.workshopInfo != null) {
            workshopInfoKey = String.format("%s %s", info.workshopInfo.getTitle(), String.join(" ", info.workshopInfo.getTags()));
        }

        String modInfoKey = String.format("%s %s %s %s %s", info.ID, info.Name, infoPanel.alias, String.join(" ", info.Authors), workshopInfoKey).toLowerCase();
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

    // set the alias for this mod and save
    public void setAlias(String alias) {
        if (alias == null || alias.isEmpty()) return;
        infoPanel.setNameText(alias);
        Loader.MTS_CONFIG.setString(info.ID, alias);
        try {
            Loader.MTS_CONFIG.save();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public class InfoPanel extends JPanel
    {
        String alias = "";
        JLabel name = new JLabel();
        JLabel version = new JLabel();

        public InfoPanel()
        {
            setLayout(new BorderLayout());

            name.setOpaque(true);
            name.setText(info.Name);
            name.setFont(name.getFont().deriveFont(13.0f).deriveFont(Font.BOLD));
            add(name, BorderLayout.CENTER);

            version.setOpaque(true);
            version.setFont(version.getFont().deriveFont(10.0f).deriveFont(Font.PLAIN));
            if (info.ModVersion != null) {
                version.setText(info.ModVersion.toString());
            } else {
                version.setText("missing version");
            }
            add(version, BorderLayout.SOUTH);

            String alias = Loader.MTS_CONFIG.getString(info.ID);
            setNameText(alias);

            checkBox.setBackground(Color.WHITE);
            setBackground(Color.WHITE);
        }

        public void setNameText(String alias) {
            if (alias == null) return;
            this.alias = alias;
            if (alias.isEmpty()) {
                name.setText(info.Name);
            } else {
                name.setText(String.format("<html>[%s]  <font color=#a0a0a0>%s</font></html>", alias, info.Name));
            }
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
            if (update != null) {
                update.setBackground(c);
            }
        }
    }
    
}

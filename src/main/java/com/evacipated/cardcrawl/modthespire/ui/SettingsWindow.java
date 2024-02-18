package com.evacipated.cardcrawl.modthespire.ui;

import com.evacipated.cardcrawl.modthespire.ModTheSpire;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class SettingsWindow extends JDialog
{
    private JPanel contentPane;
    private JButton buttonClose;
    private JCheckBox checkDebug;
    private JCheckBox checkImGui;
    private JCheckBox checkSkipIntro;
    private JComboBox<UIScale> comboUIScale;
    private JComboBox<String> comboTheme;
    private JCheckBox checkModderMode;

    public SettingsWindow(Frame owner)
    {
        super(owner, true);
        setTitle("Settings");
        setIconImage(ModSelectWindow.APP_ICON);
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonClose);

        buttonClose.addActionListener(e -> onClose());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                onClose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
            e -> onClose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        registerCheckBox(
            checkModderMode,
            "modder",
            SettingsWindow::getModderMode,
            v -> {
                setModderMode(v);
                ((ModSelectWindow) owner).setPlayButtonOptions(v);
            }
        );
        checkModderMode.addItemListener(e -> {
            switch (e.getStateChange()) {
                case ItemEvent.SELECTED:
                case ItemEvent.DESELECTED:
                    checkDebug.setEnabled(checkModderMode.isSelected());
                    checkImGui.setEnabled(checkModderMode.isSelected());
                    break;
            }
        });
        registerCheckBox(
            checkDebug,
            "debug",
            SettingsWindow::getDebug,
            SettingsWindow::setDebug
        );
        checkDebug.setEnabled(checkModderMode.isSelected());
        registerCheckBox(
            checkImGui,
            "imgui",
            SettingsWindow::getImGui,
            SettingsWindow::setImGui
        );
        checkImGui.setEnabled(checkModderMode.isSelected());
        registerCheckBox(
            checkSkipIntro,
            "skip-intro",
            SettingsWindow::getSkipIntro,
            SettingsWindow::setSkipIntro
        );

        for (float f = 1f; f <= 3f; f += 0.25f) {
            comboUIScale.addItem(new UIScale(f));
        }
        comboUIScale.setSelectedItem(new UIScale(ModSelectWindow.UI_SCALE));
        comboUIScale.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                UIScale selected = (UIScale) comboUIScale.getSelectedItem();
                if (selected != null) {
                    saveSetting("uiScale", selected.getScale());
                }
            }
        });


        if (ModTheSpire.MTS_CONFIG.has("uiTheme")) {
            comboTheme.setSelectedItem(ModTheSpire.MTS_CONFIG.getString("uiTheme"));
        }
        comboTheme.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String themeName = (String) comboTheme.getSelectedItem();
                if (themeName != null) {
                    saveSetting("uiTheme", themeName);
                    EventQueue.invokeLater(() -> ModSelectWindow.setTheme(themeName));
                }
            }
        });
    }

    @Override
    public Dimension getPreferredSize()
    {
        Dimension d = super.getPreferredSize();
        return SwingDPI.getScaledDimension(new Dimension(Math.max(d.width, 300), d.height));
    }

    private void registerCheckBox(JCheckBox checkBox, String saveKey, BooleanSupplier getter, Consumer<Boolean> setter)
    {
        checkBox.setSelected(getter.getAsBoolean());
        checkBox.addItemListener(e -> {
            switch (e.getStateChange()) {
                case ItemEvent.SELECTED:
                    setter.accept(true);
                    break;
                case ItemEvent.DESELECTED:
                    setter.accept(false);
                    break;
            }
            saveSetting(saveKey, getter.getAsBoolean());
        });
    }

    private static void saveSetting(String setting, String value)
    {
        ModTheSpire.MTS_CONFIG.setString(setting, value);
        try {
            ModTheSpire.MTS_CONFIG.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveSetting(String setting, float value)
    {
        ModTheSpire.MTS_CONFIG.setFloat(setting, value);
        try {
            ModTheSpire.MTS_CONFIG.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveSetting(String setting, boolean value)
    {
        ModTheSpire.MTS_CONFIG.setBool(setting, value);
        try {
            ModTheSpire.MTS_CONFIG.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getters and setters for config options
    private static boolean getModderMode()
    {
        return ModSelectWindow.MODDER_MODE;
    }

    private static void setModderMode(boolean value)
    {
        ModSelectWindow.MODDER_MODE = value;
    }

    private static boolean getDebug()
    {
        return ModTheSpire.DEBUG;
    }

    private static void setDebug(boolean value)
    {
        ModTheSpire.DEBUG = value;
    }

    private static boolean getImGui()
    {
        return ModTheSpire.LWJGL3_ENABLED;
    }

    private static void setImGui(boolean value)
    {
        ModTheSpire.LWJGL3_ENABLED = value;
    }

    private static boolean getSkipIntro()
    {
        return ModTheSpire.SKIP_INTRO;
    }

    private static void setSkipIntro(boolean value)
    {
        ModTheSpire.SKIP_INTRO = value;
    }

    private void onClose()
    {
        // add your code here
        dispose();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        contentPane.setEnabled(true);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.ipadx = 5;
        gbc.ipady = 5;
        contentPane.add(panel1, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        buttonClose = new JButton();
        buttonClose.setText("Close");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(buttonClose, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        panel3.setEnabled(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 5;
        gbc.ipady = 5;
        contentPane.add(panel3, gbc);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(panel4, gbc);
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Game Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel4.add(panel5, gbc);
        checkSkipIntro = new JCheckBox();
        checkSkipIntro.setSelected(false);
        checkSkipIntro.setText("Skip Intro");
        checkSkipIntro.setToolTipText("Will skip the Mega Crit logo splash screen, getting to the main menu slightly faster.");
        checkSkipIntro.setVerticalAlignment(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel5.add(checkSkipIntro, gbc);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(panel6, gbc);
        panel6.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Modder Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel6.add(panel7, gbc);
        checkDebug = new JCheckBox();
        checkDebug.setEnabled(false);
        checkDebug.setSelected(false);
        checkDebug.setText("Debug Patching");
        checkDebug.setToolTipText("ModTheSpire will output a lot more information during the patching process.");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel7.add(checkDebug, gbc);
        checkImGui = new JCheckBox();
        checkImGui.setEnabled(false);
        checkImGui.setText("ImGui Mode");
        checkImGui.setToolTipText("Game will launch using LWJGL3, allowing BaseMod to use Dear ImGui.");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel7.add(checkImGui, gbc);
        checkModderMode = new JCheckBox();
        checkModderMode.setText("Modder Mode");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel7.add(checkModderMode, gbc);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(panel8, gbc);
        panel8.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "ModTheSpire Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel8.add(panel9, gbc);
        comboUIScale = new JComboBox();
        comboUIScale.setEnabled(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel9.add(comboUIScale, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("UI Scale (requires restart)");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel9.add(label1, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel9.add(spacer1, gbc);
        comboTheme = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Light");
        defaultComboBoxModel1.addElement("Dark");
        comboTheme.setModel(defaultComboBoxModel1);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel9.add(comboTheme, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Theme");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel9.add(label2, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return contentPane;
    }

}

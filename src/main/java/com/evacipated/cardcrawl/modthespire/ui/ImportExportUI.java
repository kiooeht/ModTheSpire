package com.evacipated.cardcrawl.modthespire.ui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Handles all UI related functionality of Import and Export.
 */
public class ImportExportUI {
    /** File extension given to all exported modlist files. */
    private static final String exportedModlistFileExtension = ".mtsmodlist";

    /**
     * Opens the 'Successful Export' export window. If requested, exports the modlist key to a file.
     * @param owner : Owner component of the new window.
     * @param serializedModlist : Serialized modlist key.
     */
    public static void openSuccessfulExportWindow(Component owner, String serializedModlist){
        String[] options = new String[]{"Save to file", "Close"};

        int result = JOptionPane.showOptionDialog(
            owner,
            "Modlist key has been copied to clipboard!",
            "Success",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[1]
        );

        if(result == 0){
            // User has requested to export to file
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("StS Modlists", exportedModlistFileExtension);
            fileChooser.setFileFilter(filter);

            int saveDialogResult = fileChooser.showSaveDialog(owner);
            if (saveDialogResult == JFileChooser.APPROVE_OPTION) {
                // Get the user selected file and append the file extension to it
                File selectedFile = fileChooser.getSelectedFile();

                String filePath = selectedFile.getAbsolutePath();
                if (!filePath.endsWith(exportedModlistFileExtension)) {
                    filePath += exportedModlistFileExtension;
                    selectedFile = new File(filePath);
                }

                // Write to file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                    writer.write(serializedModlist);
                } catch (IOException e) {
                    System.out.println("Failed to write modlist to file due to " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Opens a new import window, allowing the user to either input the modkey or load it from a file.
     * @param owner : Owner frame of this window.
     * @return : Imported modlist key, or null if none was loaded.
     */
    public static String openImportWindow(Frame owner){
        final String[] importedModkey = {null};

        // Create the main dialog window
        JDialog dialog = new JDialog(owner);
        dialog.setTitle("Import");
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(owner);
        dialog.setPreferredSize(new Dimension(500, 300));

        // Create the main panel within the window
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.add(mainPanel, BorderLayout.CENTER);

        // Create the content panel within the window
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Create grid constraints for the window
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        // Create the header of the window
        JLabel header = new JLabel("Enter modlist key:");
        constraints.gridx = 0;
        constraints.gridy = 0;
        contentPanel.add(header, constraints);

        // Create the modlist key input box.
        JTextArea inputArea = new JTextArea();
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(inputArea);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        contentPanel.add(scrollPane, constraints);

        // Create the OR label for import from file.
        JLabel orLabel = new JLabel("OR");
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        contentPanel.add(orLabel, constraints);

        // Create the import from file button.
        JButton importFromFileButton = new JButton("Import from file");
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(importFromFileButton, constraints);
        importFromFileButton.addActionListener(event -> {
            // User has requested to load from file, do so.
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("StS Modlists", exportedModlistFileExtension);
            fileChooser.setFileFilter(filter);

            int returnValue = fileChooser.showOpenDialog(dialog);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                try {
                    importedModkey[0] = new String(Files.readAllBytes(selectedFile.toPath()));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(owner, "Failed to import modlist from file.", "Failure", JOptionPane.ERROR_MESSAGE);
                    System.out.println("Failed to read modlist from file due to " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

            dialog.dispose();
        });

        // Add second button to bottom panel
        JButton importButton = new JButton("Import");
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(importButton);
        importButton.addActionListener(event -> {
            importedModkey[0] = inputArea.getText();
            dialog.dispose();
        });

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setVisible(true);

        return importedModkey[0];
    }
}

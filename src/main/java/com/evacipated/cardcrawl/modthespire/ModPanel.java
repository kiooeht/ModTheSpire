package com.evacipated.cardcrawl.modthespire;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

@SuppressWarnings("serial")
public class ModPanel extends JPanel {
	public ModInfo info;
	public File modFile;
	public JCheckBox checkBox;
	
	public ModPanel(ModInfo info, File modFile) {
		this.info = info;
		this.modFile = modFile;
		this.checkBox = new JCheckBox();
		this.setLayout(new BorderLayout());
		this.add(new InfoPanel(), BorderLayout.CENTER);
	}
	
	
	public class InfoPanel extends JPanel {
		
		public InfoPanel() {
			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			
			JPanel buttonPanel = buildButtonPanel(info, checkBox);
			buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			this.add(buttonPanel);
			
			if (info.Description != null && !info.Description.equals("")) {
				JLabel description = new JLabel("Description: " + info.Description);
				description.setAlignmentX(Component.LEFT_ALIGNMENT);
				this.add(description);
			}

			if (info.Author != null && !info.Author.equals("")) {
				JLabel author = new JLabel("Author: " + info.Author);
				author.setAlignmentX(Component.LEFT_ALIGNMENT);
				this.add(author);
			}
			
			this.setBorder(new MatteBorder(0, 0, 1, 0, Color.BLUE));
		}
		
		public JPanel buildButtonPanel(ModInfo info, JCheckBox box) {
			JPanel buttonPanel = new JPanel(new BorderLayout());
			String nameString = ((info.Name != null) ? info.Name : "") + 
					" " + 
					((info.MTS_Version != null) ? "MTS Version: " + info.MTS_Version.toString() : "");
			
			JLabel name = new JLabel(nameString, JLabel.LEFT);
			name.setFont(new Font("Serif", Font.PLAIN, 18));
			
			buttonPanel.add(name, BorderLayout.WEST);
			buttonPanel.add(box, BorderLayout.EAST);
			return buttonPanel;
		}
		
	}
	
}

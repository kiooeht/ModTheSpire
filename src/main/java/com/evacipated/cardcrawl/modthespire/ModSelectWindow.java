package com.evacipated.cardcrawl.modthespire;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class ModSelectWindow extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8232997068791248057L;
	private File[] mods;
	private ModInfo[] info;
	
	public ModSelectWindow(File[] modJars) {
		mods = modJars;
		info = Loader.buildInfoArray(mods);
		initUI();
	}

	private void initUI() {
		setTitle("Mod The Spire " + Loader.MTS_VERSION.get());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);

		rootPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setLayout(new BorderLayout());

		// Mod List
		DefaultListModel<ModPanel> model = new DefaultListModel<>();
		JModPanelCheckBoxList modList = new JModPanelCheckBoxList(model);
		LoadOrder.loadModsInOrder(model, mods, info);

		JScrollPane modScroller = new JScrollPane(modList);
		modScroller.setMinimumSize(new Dimension(300, 200));
		this.getContentPane().add(modScroller, BorderLayout.CENTER);

		// Play button
		JButton playBtn = new JButton("Play");
		playBtn.addActionListener((ActionEvent event) -> {
			playBtn.setEnabled(false);

			this.getContentPane().removeAll();

			JTextArea textArea = new JTextArea();
			textArea.setFont(new Font("monospaced", Font.PLAIN, 12));
			JScrollPane logScroller = new JScrollPane(textArea);
			logScroller.setPreferredSize(new Dimension(700, 800));
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
		add(playBtn, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(null);
	}
}

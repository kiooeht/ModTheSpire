package com.evacipated.cardcrawl.modthespire;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class LoadOrder {
    
    private static String CFG_FILE = "mod_order.xml";
    
    private static class ModDescriptor {
        public File mod;
        public ModInfo info;
		public boolean checked;
        
        public ModDescriptor(File mod, ModInfo info, boolean checked) {
            this.mod = mod;
            this.info = info;
			this.checked = checked;
        }
    }
    
    public static void defaultLoad(DefaultListModel<ModPanel> model, File[] mods, ModInfo[] info) {
        for (int i = 0; i < info.length; i++) {
            model.addElement(new ModPanel(info[i], mods[i]));
        }
        return;
    }
    
    public static void loadModsInOrder(DefaultListModel<ModPanel> model, File[] mods, ModInfo[] info) {
        File cfg_file = new File(CFG_FILE);
        
        if (!cfg_file.exists()) {
            defaultLoad(model, mods, info);
            return;
        }
        
        Document d;
        
        try {
            d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(cfg_file));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            System.out.println("could not load config file: " + CFG_FILE);
            System.out.println("exception was: " + e.toString());
            e.printStackTrace();
            defaultLoad(model, mods, info);
            return;
        }
        
        ArrayList<ModDescriptor> loadOrder = new ArrayList<>();
        NodeList modsFromCfg = d.getElementsByTagName("mod");
        ArrayList<Integer> foundMods = new ArrayList<>();
        // O(n^2) will be unhappy with lots of mods
        for (int i = 0; i < modsFromCfg.getLength(); i++) {
            for (int j = 0; j < mods.length; j++) {
                if (modsFromCfg.item(i).getTextContent().equals(mods[j].getName())) {
                    loadOrder.add(new ModDescriptor(mods[j], info[j], true));
                    foundMods.add(i);
                }
            }
        }
        
        // give error messages about mods that weren't found
        for (int i = 0; i < modsFromCfg.getLength(); i++) {
            if (!foundMods.contains(i)) {
                System.out.println("could not find mod: " + modsFromCfg.item(i).getTextContent() + " even though it was specified in load order");
            }
        }
        
        // add the rest of the mods that didn't have an order specified
        for (int i = 0; i < mods.length; i++) {
            boolean found = false;
            for (int j = 0; j < loadOrder.size(); j++) {
                ModDescriptor descriptor = loadOrder.get(j);
                if (descriptor.mod == mods[i] && descriptor.info == info[i]) {
                    found = true;
                }
            }
            if (!found) {
                loadOrder.add(new ModDescriptor(mods[i], info[i], false));
            }
        }
        
        // actually set them in order in the list
        for (ModDescriptor descriptor : loadOrder) {
			ModPanel toAdd = new ModPanel(descriptor.info, descriptor.mod);
			toAdd.checkBox.setSelected(descriptor.checked);
            model.addElement(toAdd);
        }
    }
    
    private static void closeWriter(BufferedWriter writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            System.out.println("Exception during writer.close(), BufferedWriter may be leaked. " + e.toString());
        }
    }
    
    public static void saveCfg(File[] mods) {
        BufferedWriter br = null;
        try {
            File outFile = new File(CFG_FILE);
            br = new BufferedWriter(new FileWriter(outFile));
            br.write("<mts_cfg>\n");
            for (File mod : mods) {
                br.write("\t<mod>" + mod.getName() + "</mod>\n");
            }
            br.write("</mts_cfg>\n");
        } catch (IOException e) {
            System.out.println("could not save mod load order");
            System.out.println("exception was: " + e.toString());
            e.printStackTrace();
        } finally {
            closeWriter(br);
        }

    }
    
}
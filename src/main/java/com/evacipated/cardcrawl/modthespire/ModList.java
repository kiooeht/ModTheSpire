package com.evacipated.cardcrawl.modthespire;

import com.evacipated.cardcrawl.modthespire.lib.ConfigUtils;
import com.evacipated.cardcrawl.modthespire.ui.JModPanelCheckBoxList;
import com.evacipated.cardcrawl.modthespire.ui.ModPanel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ModList
{
    private static String OLD_CFG_FILE = ConfigUtils.CONFIG_DIR + File.separator + "mod_order.xml";
    private static String CFG_FILE = ConfigUtils.CONFIG_DIR + File.separator + "mod_lists.json";

    public static final String DEFAULT_LIST = "<Default>";

    private static ModListSaveData saveData = null;

    private String name;
    private List<String> mods;

    private static class ModListSaveData
    {
        public String defaultList = DEFAULT_LIST;
        public Map<String, List<String>> lists = new HashMap<>();
    }

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

    public static String getDefaultList()
    {
        if (saveData == null) {
            return DEFAULT_LIST;
        }
        return saveData.defaultList;
    }

    public static void setDefaultList(String list)
    {
        saveData.defaultList = list;
        save();
    }

    public static Collection<String> getAllModListNames()
    {
        return saveData.lists.keySet();
    }

    public static ModList loadModLists()
    {
        File oldConfig = new File(OLD_CFG_FILE);
        if (oldConfig.exists()) {
            convertOldConfig(oldConfig);
        } else if (Files.exists(Paths.get(CFG_FILE))) {
            try {
                String data = new String(Files.readAllBytes(Paths.get(CFG_FILE)));
                Gson gson = new Gson();
                saveData = gson.fromJson(data, ModListSaveData.class);
            } catch (JsonSyntaxException e) {
                saveData = new ModListSaveData();
            } catch (IOException e) {
                saveData = new ModListSaveData();
                e.printStackTrace();
            }
        } else {
            saveData = new ModListSaveData();
        }

        if (saveData == null) {
            saveData = new ModListSaveData();
        }

        return new ModList(saveData.defaultList);
    }

    public static void convertOldConfig(File oldConfig)
    {
        try {
            // read old config
            Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(oldConfig));
            NodeList modsFromCfg = d.getElementsByTagName("mod");

            // convert to new config
            saveData = new ModListSaveData();
            List<String> mods = new ArrayList<>();
            for (int i = 0; i < modsFromCfg.getLength(); i++) {
                mods.add(modsFromCfg.item(i).getTextContent());
            }
            saveData.lists.put(DEFAULT_LIST, mods);

            // write new config
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String data = gson.toJson(saveData);
            Files.write(Paths.get(CFG_FILE), data.getBytes());
            // then delete the old config
            oldConfig.delete();
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    public ModList(String list)
    {
        if (saveData == null) {
            loadModLists();
        }

        name = list;
        saveData.lists.putIfAbsent(name, Collections.emptyList());
        mods = saveData.lists.getOrDefault(name, Collections.emptyList());
    }

    public String getName()
    {
        return name;
    }

    public void loadModsInOrder(DefaultListModel<ModPanel> model, ModInfo[] info, JModPanelCheckBoxList parent)
    {
        model.clear();

        File[] modFiles = ModTheSpire.getModFiles();

        List<ModDescriptor> loadOrder = new ArrayList<>();
        List<Integer> foundMods = new ArrayList<>();
        // O(n^2) will be unhappy with lots of mods
        for (int i = 0; i < mods.size(); ++i) {
            for (int j = 0; j < modFiles.length; ++j) {
                if (mods.get(i).equals(modFiles[j].getName())) {
                    loadOrder.add(new ModDescriptor(modFiles[j], info[j], true));
                    foundMods.add(i);
                }
            }
        }

        // give error messages about mods that weren't found
        for (int i = 0; i < mods.size(); ++i) {
            if (!foundMods.contains(i)) {
                System.out.println("could not find mod: " + mods.get(i) + " even though it was specified in load order");
            }
        }

        // add the rest of the mods that didn't have an order specified
        for (int i = 0; i < modFiles.length; ++i) {
            boolean found = false;
            for (int j = 0; j < loadOrder.size(); ++j) {
                ModDescriptor descriptor = loadOrder.get(j);
                if (descriptor.mod == modFiles[i] && descriptor.info == info[i]) {
                    found = true;
                }
            }
            if (!found) {
                loadOrder.add(new ModDescriptor(modFiles[i], info[i], false));
            }
        }

        // actually set them in order in the list
        for (ModDescriptor descriptor : loadOrder) {
            ModPanel toAdd = new ModPanel(descriptor.info, descriptor.mod, parent);
            if (toAdd.checkBox.isEnabled()) {
                toAdd.checkBox.setSelected(descriptor.checked);
            }
            model.addElement(toAdd);
        }
    }

    public List<ModInfo> toModInfos(){
        ModInfo[] infos = ModTheSpire.ALLMODINFOS;

        // Find all mod files.
        File[] modFiles = ModTheSpire.getModFiles();

        // Get all mods and add checked ones to the list
        List<ModInfo> modInfos = new ArrayList<>();
        for (int i = 0; i < mods.size(); ++i) {
            for (int j = 0; j < modFiles.length; ++j) {
                if (mods.get(i).equals(modFiles[j].getName())) {
                    modInfos.add(infos[j]);
                }
            }
        }

        return modInfos;
    }

    public static void save(String list, File[] modFiles)
    {
        saveData.defaultList = list;
        List<String> modList = new ArrayList<>();
        for (File modFile : modFiles) {
            modList.add(modFile.getName());
        }
        saveData.lists.put(list, modList);

        save();
    }

    private static void save()
    {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String data = gson.toJson(saveData);
            Files.write(Paths.get(CFG_FILE), data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void delete(String list)
    {
        saveData.defaultList = DEFAULT_LIST;
        saveData.lists.remove(list);
        save();
    }

    public static void rename(String list, String newName)
    {
        List<String> mods = saveData.lists.remove(list);
        if (mods != null) {
            saveData.lists.put(newName, mods);
            saveData.defaultList = newName;
        }
        save();
    }
}

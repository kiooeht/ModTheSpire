package com.evacipated.cardcrawl.modthespire;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

public class ModInfo {
    public String Name;
    public String Author;
    public Version MTS_Version;
    public String Description;

    private ModInfo()
    {
        Name = "";
        Author = "";
        MTS_Version = new Version("0.0.0");
        Description = "";
    }
    
    public static void closeLoader(URLClassLoader loader)
    {
    	try {
    		if (loader != null) {
    			loader.close();
    		}
    	} catch (Exception e) {
    		System.out.println("Exception during loader.close(), URLClassLoader may be leaked. " + e.toString());
    	}
    }

    public static ModInfo ReadModInfo(File mod_jar)
    {
        ModInfo info = new ModInfo();
        // Default mod name to jar name
        info.Name = mod_jar.getName();
        info.Name = info.Name.substring(0, info.Name.length() - 4);

        URLClassLoader loader = null;
        try {
            loader = new URLClassLoader(new URL[] {mod_jar.toURI().toURL()});
            // Read ModTheSpire.config
            Properties prop = new Properties();
            InputStream inProp = loader.getResourceAsStream("ModTheSpire.config");
            if (inProp != null) {
                prop.load(inProp);
                info.Name = prop.getProperty("name");
                info.Author = prop.getProperty("author");
                info.MTS_Version = new Version(prop.getProperty("mts_version", "0.0.0"));
                info.Description = prop.getProperty("description");
            }
        } catch (Exception e) {
            System.out.println("ERROR: Failed to read Mod info from " + mod_jar.getName());
        } finally {
        	closeLoader(loader);
        }
        return info;
    }
}

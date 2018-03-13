package com.evacipated.cardcrawl.modthespire;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

public class ModInfo implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 7452562412479584982L;
    public String Name;
    public String Author;
    public String Description;
    public Version MTS_Version;
    public Date STS_Version;

    private ModInfo()
    {
        Name = "";
        Author = "";
        Description = "";
        MTS_Version = new Version("0.0.0");
        STS_Version = null;
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
                prop.load(new InputStreamReader(inProp,"UTF-8"));
                info.Name = prop.getProperty("name");
                info.Author = prop.getProperty("author");
                info.MTS_Version = new Version(prop.getProperty("mts_version", "0.0.0"));
                info.Description = prop.getProperty("description");

                String stsVersionString = prop.getProperty("sts_version");
                if (stsVersionString != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
                    sdf.setTimeZone(TimeZone.getTimeZone("PST"));
                    info.STS_Version = sdf.parse(stsVersionString, new ParsePosition(0));
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: Failed to read Mod info from " + mod_jar.getName());
        } finally {
            closeLoader(loader);
        }
        return info;
    }
}

package com.evacipated.cardcrawl.modthespire.lib;

import java.io.*;
import java.util.Properties;

public class SpireConfig
{
    private final static String EXTENSION = ".properties";
    private Properties properties;
    private File file;
    private String filePath;

    public SpireConfig(String modName, String fileName) throws IOException
    {
        this(modName, fileName, new Properties());
    }

    public SpireConfig(String modName, String fileName, Properties defaultProperties) throws IOException
    {
        properties = new Properties(defaultProperties);
        String dirPath;
        if (modName == null) {
            dirPath = ConfigUtils.CONFIG_DIR + File.separator;
        } else {
            dirPath = ConfigUtils.CONFIG_DIR + File.separator
                + modName + File.separator;
        }
        filePath = dirPath + fileName + EXTENSION;
        File dir = new File(dirPath);
        dir.mkdirs();

        file = new File(filePath);
        file.createNewFile();
        load();
    }

    public void load() throws IOException
    {
        properties.load(new FileInputStream(file));
    }

    public void save() throws IOException
    {
        properties.store(new FileOutputStream(file), null);
    }

    public String getString(String key)
    {
        return properties.getProperty(key);
    }

    public void setString(String key, String value)
    {
        properties.setProperty(key, value);
    }

    public boolean getBool(String key)
    {
        return Boolean.parseBoolean(getString(key));
    }

    public void setBool(String key, boolean value)
    {
        setString(key, Boolean.toString(value));
    }

    public int getInt(String key)
    {
        return Integer.parseInt(getString(key));
    }

    public void setInt(String key, int value)
    {
        setString(key, Integer.toString(value));
    }

    public float getFloat(String key)
    {
        return Float.parseFloat(getString(key));
    }

    public void setFloat(String key, float value)
    {
        setString(key, Float.toString(value));
    }
}

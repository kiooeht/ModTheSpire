package com.evacipated.cardcrawl.modthespire;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
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
    @SerializedName("modid")
    public String ID;
    @SerializedName("name")
    public String Name;
    @SerializedName("author_list")
    public String[] Authors;
    @SerializedName("description")
    public String Description;
    @SerializedName("mts_version")
    public Version MTS_Version;
    @SerializedName("sts_version")
    public Date STS_Version;

    private ModInfo()
    {
        Name = "";
        Authors = new String[]{};
        Description = "";
        MTS_Version = new Version("0.0.0");
        STS_Version = null;
    }
    
    private static void closeLoader(URLClassLoader loader)
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
        Gson gson = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.STATIC)
            .registerTypeAdapter(Version.class, new VersionDeserializer())
            .setDateFormat("MM-dd-yyyy")
            .create();

        URLClassLoader loader = null;
        try {
            loader = new URLClassLoader(new URL[] {mod_jar.toURI().toURL()}, null);
            InputStream in = loader.getResourceAsStream("ModTheSpire.json");
            if (in == null) {
                // Fallback to old info file
                return ReadModInfoOld(mod_jar);
            }
            ModInfo info = gson.fromJson(new InputStreamReader(in,"UTF-8"), ModInfo.class);
            in.close();
            return info;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (loader != null) {
                closeLoader(loader);
            }
        }

        return null;
    }

    private static ModInfo ReadModInfoOld(File mod_jar)
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
                String author = prop.getProperty("author");
                if (author != null && !author.isEmpty()) {
                    info.Authors = author.split(",");
                }
                info.MTS_Version = new Version(prop.getProperty("mts_version", "0.0.0"));
                info.Description = prop.getProperty("description");

                String stsVersionString = prop.getProperty("sts_version");
                if (stsVersionString != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
                    sdf.setTimeZone(TimeZone.getTimeZone("PST"));
                    info.STS_Version = sdf.parse(stsVersionString, new ParsePosition(0));
                }
                inProp.close();
            }
        } catch (Exception e) {
            System.out.println("ERROR: Failed to read Mod info from " + mod_jar.getName());
        } finally {
            closeLoader(loader);
        }
        return info;
    }

    private static class VersionDeserializer implements JsonDeserializer<Version>
    {
        @Override
        public Version deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
        {
            return new Version(jsonElement.getAsJsonPrimitive().getAsString());
        }
    }
}

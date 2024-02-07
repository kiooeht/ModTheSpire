package com.evacipated.cardcrawl.modthespire;

import com.evacipated.cardcrawl.modthespire.steam.SteamSearch;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.SemverException;

import java.io.*;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModInfo implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 7452562412479584982L;
    private static final Gson gson = new GsonBuilder()
        .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT)
        .registerTypeAdapter(Dependency.class, new DependencyDeserializer())
        .registerTypeAdapter(Semver.class, new VersionDeserializer())
        .setDateFormat("MM-dd-yyyy")
        .create();

    public transient URL jarURL;
    public transient String statusMsg = " ";
    public transient boolean isWorkshop = false;
    public transient SteamSearch.WorkshopInfo workshopInfo;
    @SerializedName("modid")
    public String ID;
    @SerializedName("name")
    public String Name;
    @SerializedName("version")
    public Semver ModVersion;
    @SerializedName("author_list")
    public String[] Authors;
    @SerializedName("credits")
    public String Credits;
    @SerializedName("description")
    public String Description;
    @SerializedName("mts_version")
    public Semver MTS_Version;
    @SerializedName("sts_version")
    public String STS_Version;
    @SerializedName("dependencies")
    public Dependency[] Dependencies;
    @SerializedName("optional_dependencies")
    public String[] OptionalDependencies;
    @SerializedName("update_json")
    public String UpdateJSON;

    private ModInfo()
    {
        Name = "";
        Authors = new String[]{};
        Description = "";
        MTS_Version = ModInfo.safeVersion("0.0.0");
        STS_Version = null;
        Dependencies = new Dependency[]{};
        OptionalDependencies = new String[]{};
        UpdateJSON = null;
    }

    public String getIDName() {
        if (ID == null || ID.isEmpty()) {
            return Name;
        } else {
            return ID;
        }
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
        URLClassLoader loader = null;
        try {
            loader = new URLClassLoader(new URL[] {mod_jar.toURI().toURL()}, null);
            InputStream in = loader.getResourceAsStream("ModTheSpire.json");
            if (in == null) {
                // Fallback to old info file
                ModInfo info = ReadModInfoOld(mod_jar);
                info.jarURL = mod_jar.toURI().toURL();
                return info;
            }
            ModInfo info = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), ModInfo.class);
            info.jarURL = mod_jar.toURI().toURL();
            in.close();
            return info;
        } catch (Exception e) {
            System.out.println(mod_jar);
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
                prop.load(new InputStreamReader(inProp, StandardCharsets.UTF_8));
                info.Name = prop.getProperty("name");
                String author = prop.getProperty("author");
                if (author != null && !author.isEmpty()) {
                    info.Authors = author.split(",");
                }
                info.MTS_Version = ModInfo.safeVersion(prop.getProperty("mts_version", "0.0.0"));
                info.Description = prop.getProperty("description");

                info.STS_Version = prop.getProperty("sts_version");
                inProp.close();
            }
        } catch (Exception e) {
            System.out.println("ERROR: Failed to read Mod info from " + mod_jar.getName());
        } finally {
            closeLoader(loader);
        }
        return info;
    }

    static class DependencyDeserializer implements JsonDeserializer<Dependency>
    {
        @Override
        public Dependency deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
        {
            String str = jsonElement.getAsString();
            return Dependency.parse(str);
        }
    }

    static class VersionDeserializer implements JsonDeserializer<Semver>
    {
        @Override
        public Semver deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
        {
            try {
                return safeVersion(jsonElement.getAsJsonPrimitive().getAsString());
            } catch (SemverException e) {
                return null;
            }
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ModInfo)) {
            return false;
        }

        ModInfo info = (ModInfo) obj;
        if (ID == null && info.ID == null) {
            return Objects.equals(Name, info.Name);
        } else {
            return Objects.equals(ID, info.ID);
        }
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ID, Name);
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        out.writeObject(ID);
        out.writeObject(Name);
        out.writeObject(ModVersion.toString());
        out.writeObject(Authors);
        out.writeObject(Credits);
        out.writeObject(Description);
        out.writeObject(MTS_Version.toString());
        out.writeObject(STS_Version);
        out.writeObject(Dependencies);
        out.writeObject(OptionalDependencies);
        out.writeObject(UpdateJSON);
        out.writeObject(statusMsg);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        ID = (String) in.readObject();
        Name = (String) in.readObject();
        ModVersion = safeVersion((String) in.readObject());
        Authors = (String[]) in.readObject();
        Credits = (String) in.readObject();
        Description = (String) in.readObject();
        MTS_Version = safeVersion((String) in.readObject());
        STS_Version = (String) in.readObject();
        Dependencies = (Dependency[]) in.readObject();
        OptionalDependencies = (String[]) in.readObject();
        UpdateJSON = (String) in.readObject();
        statusMsg = (String) in.readObject();
    }

    public static Semver safeVersion(String verString)
    {
        return new Semver(verString, Semver.SemverType.NPM);
    }

    public static class Dependency implements Serializable
    {
        public String id;
        private Semver version;
        private Comparison comparison;

        public boolean compare(ModInfo modInfo)
        {
            if (!Objects.equals(id, modInfo.ID)) {
                return false;
            }
            if (version != null && comparison != null) {
                int result = modInfo.ModVersion.compareTo(version);
                return comparison.isCompareTo(result);
            }
            return true;
        }

        private static final Pattern p = Pattern.compile("^(?<id>[^=<>]+)(?:(?<cmp>==|>=|>|<=|<)(?<version>.+))?$");
        static Dependency parse(String toParse)
        {
            Dependency ret = new Dependency();
            ret.parseImpl(toParse);
            return ret;
        }

        private void parseImpl(String toParse)
        {
            Matcher m = p.matcher(toParse);
            if (!m.matches()) {
                throw new IllegalArgumentException(toParse);
            }
            id = m.group("id");
            comparison = Comparison.parse(m.group("cmp"));
            String versionStr = m.group("version");
            if (versionStr != null) {
                version = safeVersion(versionStr);
            }
        }

        @Override
        public String toString()
        {
            if (id == null) {
                throw new IllegalStateException("Dependency id should not be null");
            }

            StringBuilder sb = new StringBuilder(id);
            if (version != null && comparison != null) {
                sb.append(comparison.repr()).append(version);
            }
            return sb.toString();
        }

        private void writeObject(java.io.ObjectOutputStream out) throws IOException
        {
            out.writeObject(toString());
        }

        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
        {
            parseImpl((String) in.readObject());
        }

        private enum Comparison
        {
            EQUALS, GREATER_THAN, GREATER_OR_EQUALS, LESS_THAN, LESS_OR_EQUALS;

            boolean isCompareTo(int compareTo)
            {
                switch (this) {
                    case EQUALS:
                        return compareTo == 0;
                    case GREATER_THAN:
                        return compareTo > 0;
                    case GREATER_OR_EQUALS:
                        return compareTo >= 0;
                    case LESS_THAN:
                        return compareTo < 0;
                    case LESS_OR_EQUALS:
                        return compareTo <= 0;
                    default:
                        return false;
                }
            }

            String repr()
            {
                switch (this) {
                    case EQUALS:
                        return "==";
                    case GREATER_THAN:
                        return ">";
                    case GREATER_OR_EQUALS:
                        return ">=";
                    case LESS_THAN:
                        return "<";
                    case LESS_OR_EQUALS:
                        return "<=";
                    default:
                        return null;
                }
            }

            static Comparison parse(String repr)
            {
                if (repr == null) {
                    return null;
                }
                switch (repr) {
                    case "==":
                        return EQUALS;
                    case ">":
                        return GREATER_THAN;
                    case ">=":
                        return GREATER_OR_EQUALS;
                    case "<":
                        return LESS_THAN;
                    case "<=":
                        return LESS_OR_EQUALS;
                    default:
                        return null;
                }
            }
        }
    }
}

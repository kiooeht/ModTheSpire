package com.evacipated.cardcrawl.modthespire;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.vdurmont.semver4j.Semver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public abstract class UpdateChecker
{
    protected static Map<String, SaveInfo> lastModified;

    private class SaveInfo
    {
        public String lastModified;
        public JsonObject releaseInfo;
    }

    protected URL jsonURL;
    protected JsonObject latest = null;

    public UpdateChecker(String jsonURL) throws MalformedURLException
    {
        this.jsonURL = new URL(jsonURL);
    }

    protected void obtainLatestRelease() throws IOException
    {
        if (lastModified == null) {
            String path = SpireConfig.makeFilePath(null, "Updater", "json");
            if (new File(path).isFile()) {
                String data = new String(Files.readAllBytes(Paths.get(path)));
                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, SaveInfo>>()
                {
                }.getType();
                lastModified = gson.fromJson(data, type);
            } else {
                lastModified = new HashMap<>();
            }
        }

        if (latest != null) {
            return;
        }

        HttpURLConnection request = (HttpURLConnection) jsonURL.openConnection();
        String urlString = jsonURL.toString();
        if (lastModified.containsKey(urlString)) {
            request.setRequestProperty("If-Modified-Since", lastModified.get(urlString).lastModified);
        }
        request.connect();

        //System.out.println(request.getHeaderField("X-RateLimit-Remaining"));
        //System.out.println(request.getResponseCode());

        if (request.getResponseCode() == 304) {
            latest = lastModified.get(urlString).releaseInfo;
            return;
        }

        System.out.println(request.getHeaderField("Last-Modified"));

        try {
            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            latest = root.getAsJsonObject();

            SaveInfo saveInfo = new SaveInfo();
            saveInfo.lastModified = request.getHeaderField("Last-Modified");
            saveInfo.releaseInfo = latest;
            lastModified.put(urlString, saveInfo);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String data = gson.toJson(lastModified);
            Files.write(Paths.get(SpireConfig.makeFilePath(null, "Updater", "json")), data.getBytes());
        } catch (JsonSyntaxException e) {
            System.out.println(jsonURL);
            System.out.println(e.getMessage());
        }
    }

    public boolean isNewerVersionAvailable(Semver current) throws IOException
    {
        obtainLatestRelease();
        if (latest == null) {
            return false;
        }
        Semver latestVersion = getLatestReleaseVersion();
        if (latestVersion != null) {
            return latestVersion.compareTo(current) > 0;
        } else {
            return false;
        }
    }

    public JsonElement getElement(String key) throws IOException
    {
        obtainLatestRelease();
        if (latest == null) {
            return null;
        }
        return latest.get(key);
    }

    public String getElementAsString(String key) throws IOException
    {
        JsonElement element = getElement(key);
        if (element == null) {
            return null;
        }
        return getElement(key).getAsString();
    }

    public abstract Semver getLatestReleaseVersion() throws IOException;

    public abstract URL getLatestReleaseURL() throws IOException;

    public abstract URL getLatestDownloadURL() throws IOException;
}

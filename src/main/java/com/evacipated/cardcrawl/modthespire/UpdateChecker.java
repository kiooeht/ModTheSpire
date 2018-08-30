package com.evacipated.cardcrawl.modthespire;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.vdurmont.semver4j.Semver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class UpdateChecker
{
    protected URL jsonURL;
    protected JsonObject latest = null;

    public UpdateChecker(String jsonURL) throws MalformedURLException
    {
        this.jsonURL = new URL(jsonURL);
    }

    protected void obtainLatestRelease() throws IOException
    {
        if (latest != null) {
            return;
        }

        HttpURLConnection request = (HttpURLConnection) jsonURL.openConnection();
        request.connect();

        try {
            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            latest = root.getAsJsonObject();
        } catch (JsonSyntaxException e) {
            System.out.println(jsonURL);
            System.out.println(e.getMessage());
        }
    }

    public boolean isNewerVersionAvailable(Semver current) throws IOException
    {
        obtainLatestRelease();
        return getLatestReleaseVersion().compareTo(current) > 0;
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

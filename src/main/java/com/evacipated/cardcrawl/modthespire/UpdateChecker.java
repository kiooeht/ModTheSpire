package com.evacipated.cardcrawl.modthespire;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class UpdateChecker
{
    private URL jsonURL;
    private JsonObject latest = null;

    public UpdateChecker(String jsonURL) throws MalformedURLException
    {
        this.jsonURL = new URL(jsonURL);
    }

    private void obtainLatestRelease() throws IOException
    {
        if (latest != null) {
            return;
        }

        HttpURLConnection request = (HttpURLConnection) jsonURL.openConnection();
        request.connect();

        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        latest = root.getAsJsonObject();
    }

    public boolean isNewerVersionAvailable(Version current) throws IOException
    {
        obtainLatestRelease();
        return getLatestReleaseVersion().compareTo(current) > 0;
    }

    public JsonElement getElement(String key) throws IOException
    {
        obtainLatestRelease();
        return latest.get(key);
    }

    public String getElementAsString(String key) throws IOException
    {
        return getElement(key).getAsString();
    }

    public abstract Version getLatestReleaseVersion() throws IOException;

    public abstract URL getLatestReleaseURL() throws IOException;
    
    public abstract URL getDownloadURL() throws IOException;
}

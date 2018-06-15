package com.evacipated.cardcrawl.modthespire;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class GithubUpdateChecker extends UpdateChecker
{
    public GithubUpdateChecker(String username, String reponame) throws MalformedURLException
    {
        this("https://api.github.com/repos/" + username + "/" + reponame + "/releases/latest");
    }

    public GithubUpdateChecker(String url) throws MalformedURLException
    {
        super(url);
    }

    @Override
    public Version getLatestReleaseVersion() throws IOException
    {
        return new Version(getElementAsString("tag_name"));
    }

    @Override
    public URL getLatestReleaseURL() throws IOException
    {
        return new URL(getElementAsString("html_url"));
    }

    @Override
    public URL getLatestDownloadURL() throws IOException
    {
        JsonElement assets = getElement("assets");

        if (!(assets instanceof JsonArray)) {
            throw new RuntimeException("Excepted assets to be of type array");
        }

        for (JsonElement asset : (JsonArray)assets) {
            if (asset instanceof JsonObject) {
                JsonObject object = (JsonObject)asset;
                String url = object.get("browser_download_url").getAsString();
                if (isJar(url)) {
                    return new URL(url);
                }
            }
        }

        throw new RuntimeException("Could not find jar asset to download");
    }

    private boolean isJar(String url) {
        return url.toLowerCase().endsWith(".jar");
    }
}

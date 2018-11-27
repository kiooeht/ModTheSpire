package com.evacipated.cardcrawl.modthespire;

import com.google.gson.*;
import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.SemverException;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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

    private static String removeLatestFromURL(String url)
    {
        if (url.endsWith("/latest")) {
            return url.substring(0, url.length() - "/latest".length());
        }
        return url;
    }

    @Override
    protected void obtainLatestRelease() throws IOException
    {
        // If no latest release exists because all releases are pre-releases, grab the latest pre-release
        try {
            super.obtainLatestRelease();
        } catch (FileNotFoundException e) {
            jsonURL = new URL(removeLatestFromURL(jsonURL.toString()));

            HttpURLConnection request = (HttpURLConnection) jsonURL.openConnection();
            request.connect();

            try {
                JsonParser jp = new JsonParser();
                JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
                latest = root.getAsJsonArray().get(0).getAsJsonObject();
            } catch (JsonSyntaxException e2) {
                System.out.println(jsonURL);
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public Semver getLatestReleaseVersion() throws IOException
    {
        try {
            return ModInfo.safeVersion(getElementAsString("tag_name"));
        } catch (SemverException e) {
            JOptionPane.showMessageDialog(null,
                getElementAsString("html_url")
                    + "\nrelease has a missing or bad version number: \""
                    + getElementAsString("tag_name")
                    + "\".\nGo yell at the author to fix it.",
                "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }
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

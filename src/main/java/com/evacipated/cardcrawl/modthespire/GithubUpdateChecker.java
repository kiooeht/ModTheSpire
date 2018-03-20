package com.evacipated.cardcrawl.modthespire;

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
}

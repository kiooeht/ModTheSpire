package com.evacipated.cardcrawl.modthespire;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
    
    private boolean URLisJar(String uri) {
    	return uri.substring(uri.lastIndexOf(".") + 1).toLowerCase().equals("jar");
    }
    
    @Override
    public URL getDownloadURL() throws IOException {
    	JsonElement assets = getElement("assets");
    	
    	if (!(assets instanceof JsonArray)) {
    		throw new IOException("expected assets to be of type array");
    	}
    	
    	JsonArray assetsArray = (JsonArray) assets;
    	
    	for (JsonElement individualAsset : assetsArray) {
    		if (!(individualAsset instanceof JsonObject)) continue;
    		
    		JsonObject individualObject = (JsonObject) individualAsset;
    		String browserDownloadURL = individualObject.get("browser_download_url").getAsString();
    		
    		if (URLisJar(browserDownloadURL)) {
    			return new URL(browserDownloadURL);
    		}
    	}
    	
    	throw new IOException("could not find jar asset to download");
    }
    
}

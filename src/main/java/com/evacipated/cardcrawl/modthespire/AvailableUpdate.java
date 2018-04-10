package com.evacipated.cardcrawl.modthespire;

import java.net.URL;

public class AvailableUpdate {

	public URL latestReleaseURL;
	public URL downloadURL;
	public boolean needsUpdate;
	
	public AvailableUpdate(URL latestReleaseURL, URL downloadURL, boolean needsUpdate) {
		this.latestReleaseURL = latestReleaseURL;
		this.downloadURL = downloadURL;
		this.needsUpdate = needsUpdate;
	}
	
}

package no.bekk.bekkopen.mightycrawler;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;

public class Resource {
	public String id = "";
	public String url = "";
	public String content = "";
	
	public String contentType = "";
	public int responseCode;
	public String encoding = "";
	
	public Date timeStamp = null;
	public long responseTime;
	
	public int recursionLevel = 0;
	public Collection<String> urls = new LinkedHashSet<String>();

	public boolean isVisited = false;
	public boolean isDownloaded = false;
	public boolean isParsed = false;
	public boolean isStored = false;

	public boolean wasRedirect = false;
	public boolean wasError = false;
	
	public Resource(String url) {
		this.url = url;
	}
	
	public Resource(String url, int recursionLevel) {
		this.url = url;
		this.recursionLevel = recursionLevel;
	}
}



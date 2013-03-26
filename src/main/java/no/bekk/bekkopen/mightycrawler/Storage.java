package no.bekk.bekkopen.mightycrawler;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Storage {

	static final Logger log = LoggerFactory.getLogger(Storage.class);

	public String outputDirectory = null;
	
	public Storage(Configuration c) {
		this.outputDirectory = c.outputDirectory;
		log.info("Using storage directory: " + c.outputDirectory);
	}
	
	public boolean save(String url, String content) {
		String fileName = mapURLtoFileName(url); 
		try {
			File f = new File(outputDirectory + fileName);
			if (f.exists()) {
				log.error("Two different URLs map to the same file name. Stopping before overwriting the existing file: " + outputDirectory + fileName);
    			return false;				
			}
			FileUtils.writeStringToFile(f, toXML(url, content), "UTF-8");
			log.debug("Wrote page at: " + url + " to file: " + outputDirectory + fileName);
		} catch (IOException ioe) {
			log.error("Error saving url: " + url + " to disk, cause: " + ioe);
			return false;
		}
		return true;
	}

	public String mapURLtoFileName(String url) {
		// Extract host name
		String hostName = StringUtils.substringAfter(url, "://");
		hostName = hostName.substring(0, hostName.indexOf("/") + 1);
		
		// Append a unique string based on the URL and time stamp
		String timeStamp = "" + System.currentTimeMillis();
		String fileName = hostName + Integer.toHexString(url.hashCode());
		fileName = fileName + "-" + StringUtils.right(timeStamp, 4) + ".xml";
		return fileName;
	}
	
	public String toXML(String url, String page) {
		// Restart any CDATA blocks in case the input is XHTML
		page = page.replaceAll("]]>", "]]]]><![CDATA[>");
		// Remove any control characters
		page = page.replaceAll("\\p{Cntrl}", "");
		
		// XML encode the URL string
		// First: Decode any encoded &'s, if present 
		url = url.replaceAll("&amp;", "&");
		// Then: Encode all &'s. Now there will be no double encoding of &'s (& -> &amp; -> &amp;amp;)
		url = url.replaceAll("&", "&amp;");
		
		return "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" + 
		"<resource>\n" +
		"<url>" + url + "</url>\n" +
		"<content><![CDATA[\n" + page + "\n]]></content>\n" +
		"</resource>";
	}
}

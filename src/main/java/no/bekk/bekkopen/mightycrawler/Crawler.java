package no.bekk.bekkopen.mightycrawler;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Crawler {

	private Configuration config = null;
	private DownloadManager download = null;
	
	static final Logger log = LoggerFactory.getLogger(Crawler.class);
	
	public void init(String propertiesFile) {
		log.info("Starting up...");
		config = new Configuration(propertiesFile);
		try {
			FileUtils.deleteDirectory(new File(config.databaseDirectory));
			FileUtils.deleteDirectory(new File(config.outputDirectory));
			FileUtils.deleteDirectory(new File(config.reportDirectory));
		} catch (IOException ioe) {
			log.error("Could not empty directory: " + ioe);
		}
	}
	
	public void start() {
		download = new DownloadManager(config);
 		download.start();
 		for (String url : config.startURLs) {
 			Resource res = new Resource(url);
 	 		download.addToQueue(res);			
 		}
 		while (!download.isTerminated()) {
 			try {
				Thread.sleep(10000);
			} catch (Exception e) {
	 			log.error("Main thread sleep interrupted.");
			}
 		} 
 		try {
	 		download.join();	 		
 		} catch (Exception e) {
 			log.debug("Could not wait for threads to stop: " + e);
 		}

		log.info("Crawling finished.");
	}
	
	public static void main(String[] args) {
		Crawler c = new Crawler();
		String configFile = "crawler.properties";
		if (args.length == 1) {
			configFile = args[0];
		}		
		c.init(configFile);
		c.start();
	}
	
}

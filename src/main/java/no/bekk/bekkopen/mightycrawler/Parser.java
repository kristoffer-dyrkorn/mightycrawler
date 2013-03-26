package no.bekk.bekkopen.mightycrawler;


import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Parser  {

	private IncludeExcludeFilter linkFilter = null;
	
	static final Logger log = LoggerFactory.getLogger(Parser.class);

	public Parser(IncludeExcludeFilter f) {
		linkFilter = f;
	}
	
	public Collection<String> extractLinks(Resource res) {
		Collection<String> newUrls = linkFilter.getMatches(res.content);
		log.debug("Done parsing " + res.url + ", number of URLs found: " + newUrls.size());
		return newUrls;
	}
}

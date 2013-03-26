package no.bekk.bekkopen.mightycrawler;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncludeExcludeFilter {
	private Pattern includeFilter;
	private Pattern excludeFilter;

	static final Logger log = LoggerFactory.getLogger(IncludeExcludeFilter.class);
	
	public IncludeExcludeFilter(String include, String exclude) {
		includeFilter = Pattern.compile(include);
		excludeFilter = Pattern.compile(exclude);
	}

	// TODO: Convenience method to filter lists of items
	public boolean letsThrough(String item) {
		Matcher includeMatcher = includeFilter.matcher(item);
	    if (includeMatcher.matches()) {
    		log.debug("Item " + item + " matches the inclusion filter.");
	    	Matcher excludeMatcher = excludeFilter.matcher(item);
	    	if (!excludeMatcher.matches()) {
	    		log.debug("Item " + item + " included as it matched the inclusion filter and did not match the exclusion filter.");
	    		return true;
	    	} else {
	    		log.debug("Item " + item + " excluded as it matched the exclusion filter.");
	    	}
    	} else {
    		log.debug("Item " + item + " excluded as it did not match the inclusion filter.");
    	}
	    return false;
	}
	
	public Collection<String> getMatches(String content) {
		Collection<String> matchList = new HashSet<String>();
		Matcher matcher = includeFilter.matcher(content);
		while (matcher.find()) {
			int i=1;
			while (i <= matcher.groupCount()) {
				if (matcher.group(i) != null) {
					matchList.add(matcher.group(i));				
				}
				i++;
			}
		}
		return matchList;
	}
}

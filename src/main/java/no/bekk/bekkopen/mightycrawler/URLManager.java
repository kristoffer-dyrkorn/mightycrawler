package no.bekk.bekkopen.mightycrawler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class URLManager {

	private LinkedHashSet<String> urlsToVisit = new LinkedHashSet<String>();
	private LinkedHashSet<String> urlsVisited = new LinkedHashSet<String>();
	
	private IncludeExcludeFilter crawlFilter;
	
	static final Logger log = LoggerFactory.getLogger(URLManager.class);

	public URLManager(IncludeExcludeFilter f) {
		crawlFilter = f;
	}

	public Collection<String> updateQueues(Resource res) {
		markURLAsVisited(res.url);

		Collection<String> newURLs = res.urls;
		newURLs = normalizeURLs(newURLs, res.url);
		newURLs = removeKnownURLs(newURLs);
		newURLs = filterURLs(newURLs);

		addNewURLs(newURLs);
		log.info("Page: " + res.url + ", urls added to queue: " + newURLs.size());
		
		log.debug("Urls visited: " + urlsVisited.size());
		log.debug("Urls to visit: " + urlsToVisit.size());
		return newURLs;
	}
	
	public Collection<String> removeKnownURLs(Collection<String> newUrls) {
		newUrls.removeAll(urlsVisited);
		newUrls.removeAll(urlsToVisit);
		return newUrls;
	}

	public void markURLAsVisited(String url) {
		urlsToVisit.remove(url);
		urlsVisited.add(url);
	}

	public void addNewURLs(Collection<String> newUrls) {
		urlsToVisit.addAll(newUrls);
	}

	public Collection<String> filterURLs(Collection<String> urlList) {
		Collection<String> filteredURLs = new HashSet<String>();
		log.debug("Pre filtering: " + urlList);
		for (String u : urlList) {
			if (crawlFilter.letsThrough(u)) {
				filteredURLs.add(u);
			}
		}
		log.debug("Post filtering: " + filteredURLs);
		return filteredURLs;
	}

	public Collection<String> normalizeURLs(Collection<String> urlList, String baseUrl) {
		Collection<String> normalizedURLs = new HashSet<String>();
		for (String u : urlList) {
			normalizedURLs.add(normalize(u, baseUrl));
		}
		return normalizedURLs;
	}

	public String normalize(String url, String baseUrl) {		
		url = url.trim();
		url = StringUtils.substringBeforeLast(url, "#");
		url = StringUtils.substringBeforeLast(url, ";jsessionid");
		
		String absoluteURL = "";
		try {
			URI base = new URI(baseUrl);
			URI fullUrl = base.resolve(url);
			absoluteURL = fullUrl.toString();
			String query = fullUrl.getRawQuery();
			if (query != null) {
				String beforeQuery = StringUtils.substringBefore(absoluteURL, query);
				absoluteURL = beforeQuery + sortQueryParameters(query);
			}
		} catch (URISyntaxException e) {
			log.error("Normalization error. Skipping URL. Base url: " + baseUrl + " violates URL standards (RFC 2396).");
		} catch (IllegalArgumentException e) {
			log.warn("Normalization error. Skipping URL since it violates URL standards (RFC 2396). Base: " + baseUrl + ", url: " + url);
		}
		log.debug("Normalized url: " + url + " to: " + absoluteURL);
		return absoluteURL;
	}
	
	public String sortQueryParameters(String queryMap) {
		String[] vars = queryMap.split("&");
		Arrays.sort(vars);
		return StringUtils.join(vars, "&");
	}
}

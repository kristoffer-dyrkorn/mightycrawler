package no.bekk.bekkopen.mightycrawler;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadWorker implements Callable<Resource> {
	
	private HttpClient httpClient;
	private HttpContext context;
	private Resource res;

	private Configuration c;
	
	static final Logger log = LoggerFactory.getLogger(DownloadWorker.class);
	
    public DownloadWorker(HttpClient httpClient, Resource res, Configuration c) {
        this.httpClient = httpClient;
        this.context = new BasicHttpContext();
        this.res = res;
        this.c = c;
    }
    
    public Resource call() {
    	HttpGet httpGet = new HttpGet(res.url);
        try {
        	log.debug("Fetching " + res.url + ", delay = " + c.downloadDelay + " seconds.");
        	Thread.sleep(c.downloadDelay * 1000);
	    	
			long startTime = System.currentTimeMillis();
			HttpResponse response = httpClient.execute(httpGet, context);

			res.isVisited = true;
        	res.responseCode = response.getStatusLine().getStatusCode();
			
			if (res.responseCode == HttpStatus.SC_TEMPORARY_REDIRECT ||
				res.responseCode == HttpStatus.SC_MOVED_PERMANENTLY ||
				res.responseCode == HttpStatus.SC_MOVED_TEMPORARILY) {

				Header newLocation = response.getFirstHeader("Location");
				String newUrl = newLocation.getValue();
				log.info("Redirected. Original url: " + res.url + ", final url: " + newUrl);

				// treat the redirect target as a newly discovered url 
				// so it becomes normalized and filtered
				res.wasRedirect = true;
				res.urls.add(newUrl);
			}
			
	    	res.responseTime = System.currentTimeMillis() - startTime;
    		res.timeStamp = parseTimestamp(response.getFirstHeader("Date"));

	    		
        	HttpEntity entity = response.getEntity();
        	if (entity != null) {        		
        		if (res.responseCode == HttpStatus.SC_OK) {
        			ContentType contentType = ContentType.getOrDefault(entity);
        			res.contentType = contentType.getMimeType();
        			if (contentType.getCharset() != null) {
        				res.encoding = contentType.getCharset().name();
        			}
    	    		res.content = EntityUtils.toString(entity);
				} else {
					log.debug("Did not get content from " + res.url + ", response code was " + res.responseCode);
				}
	        	EntityUtils.consume(entity);
	    		res.isDownloaded = true;
            } else {
            	log.warn("Response entity for url: " + res.url + " was null.");
            }
        } catch (InterruptedException ie) {        	
            res.wasError = true;
            log.info("Request was interrupted. Url not downloaded: " + res.url);
        } catch (SocketTimeoutException ste) {
            httpGet.abort();
            res.wasError = true;
            res.responseCode = HttpStatus.SC_REQUEST_TIMEOUT;
            log.warn("Timeout (" + c.responseTimeout + " seconds) reached when requesting: " + res.url + ", " + ste);
        } catch (IllegalStateException ise) {
            httpGet.abort();
            res.wasError = true;
            log.warn("Aborted request for: " + res.url + ", " + ise);
        } catch (IOException e) {
            httpGet.abort();
            res.wasError = true;
            log.error("Aborted request for: " + res.url + ", " + e);
        }
        return res;
    }
    
	public Date parseTimestamp(Header dateHeader) {
		Date d = new Date();
		if (dateHeader != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
			try {
				d = sdf.parse(dateHeader.getValue());
			} catch (Exception e) {
				log.warn("Could not parse date: " + dateHeader.getValue() + ", using current local time instead.");
			}
		}
		return d;
	}

}

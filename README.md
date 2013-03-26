Mightycrawler - A multithreaded web crawler written in Java
=======================================================================

Getting it
-----------

Warning: Mightycrawler is experimental. Some features are not fully implemented.

    git clone git://github.com/kristofd/mightycrawler.git
    cd mightycrawler
    mvn clean package


Description
-----------

Mightycrawler is a multithreaded web crawler with reporting capabilities. A separate thread pool (of configurable size) downloads resources in parallel, while content parsing and storage to disk is done by the main thread. During crawling, various statistics about the web site performance is gathered and put in a database. After crawling is done, user-specified queries are run against the database to produce various reports in plain text format.

**Warning!** Mightycrawler is indeed mighty and can generate a lot of requests in short time. Please do use the program properly and with care.


Quick start
-----------

After downloading and building, edit `crawler.properties` to suit your needs.

Then run mightycrawler by typing

	java -jar target/mightycrawler-[version].jar [myconfig.properties]


Configuration
-------------

All options for running mightycrawler are given in the `crawler.properties` file:

* `startURL`: Where to start crawling. Host names must end with a "/".

* `includePattern`: Restrict crawling to URLs matching this regex. Defaults to everything under startURL.

* `excludePattern`: Among the included URLs, exclude those matching this regex. Defaults to none (don't exclude any).

* `urlFile`: Visit all the URLs in this file (one URL per line). if provided, will override all of the above settings and turn off crawling.

* `extractPattern`: Extract links from content types matching this regex. Defaults to none (if nothing is specified).

* `linkPattern`: Consinder content captured by this regex as links to resources. Defaults to none (if nothing is specified).

* `storePattern`: Download and save to disk all content types matching this regex. Defaults to none (if nothing is specified).

* `userAgent`: The user agent the crawler reports to be.

* `useCookies`: Set to false to disable cookie support, thus disabling session stickyness. Defaults to true (ie supporting sticky sessions).

* `followRedirects`: Specify whether the crawler should follow redirects. If true, only the final URL (after redirect) will be logged. Defaults to true.

* `downloadThreads`: Number of threads for downloading content.

* `maxVisits`: Stop after visiting this number of pages.

* `maxDownloads`: Stop after downloading this number of pages.

* `maxRecursion`: Stop when reaching this recursion level.

* `maxTime`: Stop when running for this number of minutes.

* `downloadDelay`: For each resource, pause of this number of seconds before downloading.

* `responseTimeout`: Move on if a request gets no response after this number of seconds.

* `crawlerTimeout`: Stop crawling if no new URLs are discovered within this number of seconds.

* `outputDirectory`: Where to put the downloaded web pages. MANDATORY.

* `reportDirectory`: Where to put download statistics. MANDATORY.

* `databaseDirectory`: Where to put the crawler database. MANDATORY.

* `reportSQL`: SQL statements that are run against the crawler database after completion. Output is sent to the filename specified. 
Multiple SQLs can be provided, one per line, each line ending with \ and the next line starting with |.

Example: 

    reportSQL=SQL1@reportfile1.txt\
    |SQL2@reportfile2.txt\
    |SQL3@reportfile3.txt


Tables in the crawler database
------------------------------

    DOWNLOADS
    ---------
    url VARCHAR(4095)
    http_code INTEGER default 0
    content_type VARCHAR(255)
    response_time INTEGER default 0
    downloaded_at DATETIME default NOW
    downladed BOOLEAN

    LINKS
    -----
    url_from VARCHAR(4095)
    url_to VARCHAR(4095)


TODO
-----------

* Support stopping and resuming the crawler
* Improve the display of various run-time statistics
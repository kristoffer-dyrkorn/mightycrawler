package no.bekk.bekkopen.mightycrawler;

import java.io.File;
import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.io.FileUtils;
import org.hsqldb.jdbc.JDBCCommonDataSource;
import org.hsqldb.jdbc.JDBCDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Report {

	private Driver hsqldbDriver = null;
	private String connectionString = null;
	private DataSource datasource = null;
	
	private String reportDirectory = null;
	private Collection<String> reportSQL = null;

	
	private SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	static final Logger log = LoggerFactory.getLogger(Report.class);

	public Report(Configuration c) {
		reportSQL = c.reportSQL;
		reportDirectory = c.reportDirectory;

		log.info("Using database directory: " + c.databaseDirectory);		
		log.info("Using report directory: " + c.reportDirectory);
				
		File currentDir = new File("");
		String dbPath =  currentDir.getAbsolutePath() + File.separator + c.databaseDirectory + "db";
		connectionString = "jdbc:hsqldb:file:" + dbPath;

		datasource = new JDBCDataSource();
		((JDBCCommonDataSource)datasource).setUrl(connectionString);
		
		try {
			hsqldbDriver = (Driver) Class.forName("org.hsqldb.jdbcDriver").newInstance();
			DriverManager.registerDriver(hsqldbDriver);
		} catch (Exception e) {
			log.error("Could not instantiate database driver: " + e.getMessage());
		}

		write("DROP SCHEMA PUBLIC CASCADE");
		write("SET DATABASE DEFAULT RESULT MEMORY ROWS 1000");
		write("CREATE CACHED TABLE downloads ( url VARCHAR(4095), http_code INTEGER default 0, content_type VARCHAR(255), response_time INTEGER default 0, downloaded_at DATETIME default NOW, downloaded BOOLEAN)");
		write("CREATE CACHED TABLE links ( url_from VARCHAR(4095), url_to VARCHAR(4095))");
	}

	public void registerVisit(Resource res) {
		String timeString = timeStampFormat.format(res.timeStamp);
		// TODO: Escaping
		write("INSERT INTO downloads (url, http_code, content_type, response_time, downloaded_at, downloaded) values (?,?,?,?,?,?)",
				res.url, res.responseCode, res.contentType, res.responseTime, timeString, res.isDownloaded);				
	}

	public void registerOutboundLinks(String url, Collection<String> outlinks) {
		for (String l : outlinks) {
			// TODO: Escaping
			write("INSERT INTO links (url_from, url_to) values (?, ?)", url, l);
		}
	}
	
	private void printReport(String fileName, List<Map<String, Object>> result) {
		StringBuilder out = new StringBuilder();
		for (Map<String, Object> h : result) {
			Set<Entry<String, Object>> entries = h.entrySet();
			for (Entry<String, Object> e: entries) {
    			out.append(e.getValue() + " ");
			}
			out.append("\n");
		}

		try {
			File f = new File(reportDirectory + fileName);
			FileUtils.writeStringToFile(f, out.toString(), "UTF-8");
		} catch (IOException ioe) {
			log.error("Could not create report file: " + ioe);
		}
	}

	public void createReport() {
		if (!reportSQL.isEmpty()) {
			for (String reportLine : reportSQL) {
				String[] reportInfo = reportLine.split("@");
				printReport(reportInfo[1], read(reportInfo[0]));
			}
		}
	}
	
	public List<Map<String, Object>> read(String sql){		
		ResultSetHandler<List<Map<String, Object>>> rsh = new MapListHandler();
		List<Map<String, Object>> result = null;
		QueryRunner run = new QueryRunner(datasource);		
		try {
			result = run.query(sql, rsh);
		} catch (SQLException e) {
			log.error("Could not execute statement: " + e.getMessage());
			log.error("SQL was: " + sql);
		}
		return result;
	}
	
	public int write(String sql, Object... params) {
		int updated = 0;
		QueryRunner run = new QueryRunner(datasource);
		try {
			updated = run.update(sql, params);
		} catch (SQLException e) {
			log.error("Could not execute statement: " + e.getMessage());
			log.error("SQL was: " + sql);
		}
		log.debug("Rows changed: " + updated);
		return updated;
	}
	
	public void shutDown(){
		write("SHUTDOWN");
		try {
			DriverManager.deregisterDriver(hsqldbDriver);
        } catch (SQLException e) {
			log.error("Could not deregister hsqldb driver: " + e.getMessage());
        }
	}	
}

package org.globus.transfer.reliable.client.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * some utility functions for log
 * @author Liu Wantao liuwt@uchicago.edu
 *
 */
public class LogFileUtils {
	
	/**
	 * create a new log4j configuration file
	 * @param logFileLocation
	 */
	public static void createNewLogConfigFile(String logFileLocation) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(UIConstants.LOG_CONFIG);			
			Properties prop = new Properties();
			prop.setProperty("log4j.rootCategory", "ERROR, A1");
			prop.setProperty("log4j.appender.A1", "org.apache.log4j.FileAppender");
			prop.setProperty("log4j.appender.A1.file", logFileLocation);
			prop.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
			prop.setProperty("log4j.appender.A1.layout.ConversionPattern", "%d{ISO8601} %-5p %c{2} [%t,%M:%L] %m%n");
			prop.setProperty("log4j.category.org.globus", "DEBUG");
			prop.store(fos, null);			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {				
			}
		}
	}
	
	public static void updateLogConfigFile(String logFileLocation) {
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			is = new BufferedInputStream(new FileInputStream(UIConstants.LOG_CONFIG));
			Properties prop = new Properties();
			prop.load(is);
			prop.setProperty("log4j.appender.A1.file", logFileLocation);
			
			fos = new FileOutputStream(UIConstants.LOG_CONFIG);
			prop.store(fos, null);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				is.close();
				fos.close();
			} catch (IOException e) {				
			}			
		}		
	}
	
	public static void main(String[] args) {
		createNewLogConfigFile("fd");
		updateLogConfigFile("fefefe");
	}
}

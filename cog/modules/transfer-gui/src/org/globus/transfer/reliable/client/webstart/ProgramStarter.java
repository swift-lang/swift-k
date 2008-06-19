package org.globus.transfer.reliable.client.webstart;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.globus.transfer.reliable.client.utils.UIConstants;

/**
 * This class is intended for launching the client using Java Web Start.
 * It can download, untar GT ws-core automatically, specify system properties
 * for the client and launch it. 
 *   
 * @author Liu Wantao  liuwt@uchicago.edu
 *
 */
public class ProgramStarter {
	private static Log logger = LogFactory.getLog(ProgramStarter.class);
	private static String JWSCacheDir = System.getProperty("deployment.user.cachedir");	
	private static String GLOBUS_LOCATION = JWSCacheDir + File.separator + "ws_core1";
	private static String downloadLink = "http://www-unix.globus.org/ftppub/gt4/4.0/4.0.7/ws-core/bin/ws-core-4.0.7-bin.zip";
	private static String gtFileName = JWSCacheDir + File.separator + "ws-core-4.0.7-bin.zip";
	

	/**
	 * Download GT ws-core automatically
	 * @param link
	 * @return
	 */
	private boolean downloadGT(String link) {
		logger.debug("download GT ws-core from:" + link);
		
		boolean ret = false;
		InputStream inStream = null;
		FileOutputStream fos = null;		
		try {
			URL url = new URL(link);
			URLConnection conn = url.openConnection();
			inStream = conn.getInputStream();
			fos = new FileOutputStream(gtFileName);
			byte[] buffer = new byte[5 * 1024 * 1024];
			int length = -1;
			
			while ((length = inStream.read(buffer)) != -1) {
				fos.write(buffer, 0, length);
			}
			
			ret = true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			ret = false;
		} finally {
			try {				
				inStream.close();
				fos.close();
			} catch (Exception e) {
				
			}
		}
		
		return ret;
	}
	
	/**
	 * extract GT ws-core from downloaded zip file
	 * @param zipFileName
	 * 
	 */
	private void extractGT(String zipFileName) {
		logger.debug("extract GT ws-core from:" + zipFileName);
		InputStream input = null;
		BufferedOutputStream bos = null;
		ZipFile zfile = null;		
		
		try {
			zfile = new ZipFile(zipFileName);
			Enumeration zlist = zfile.entries();
			ZipEntry entry = null;
			byte[] buf = new byte[8192];
			 
			//iterate every entry in the zip file
			while (zlist.hasMoreElements()) {
				//System.out.println(entry.getName());
				entry = (ZipEntry) zlist.nextElement();				
				if (entry.isDirectory()) {
					continue;
				}
				
				String name = entry.getName();
				name = name.substring(name.indexOf("/"));				
				input = zfile.getInputStream(entry);
				File file = new File(GLOBUS_LOCATION, name);
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				bos = new BufferedOutputStream(new FileOutputStream(file));
				
				int length = -1;
				while ((length = input.read(buf)) != -1) {
					bos.write(buf, 0, length);
				}	
				
				bos.flush();
			}			
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				zfile.close();
				input.close();
				bos.close();
			} catch (Exception e) {
				
			}			
		}		
	}
	
	/**
	 * set GLOBUS_LOCATION and axis.ClientConfigFile for the GUI client
	 * @param globus_location
	 */
	private void setEnv(String globus_location) {
		System.setProperty("GLOBUS_LOCATION", GLOBUS_LOCATION);
		String path = GLOBUS_LOCATION + File.separator + "client-config.wsdd";
		System.setProperty("axis.ClientConfigFile", path);
	}
	
	/**
	 * check if GT ws_core is exist in local disk
	 * @return
	 */
	private boolean isGTExist() {
		File gt = new File(GLOBUS_LOCATION);
		if (gt.exists() && gt.isDirectory()) {
			return true;
		}
		
		return false;
	}
	
	private void loadGUI() {
		org.globus.transfer.reliable.client.GridFTPGUIApp.main(null);
	}
	
	private void invokeAnt() {		
		try {			
			URL url = Thread.currentThread().getContextClassLoader().getResource("scripts/build.xml");		
			System.out.println(url);
			logger.debug(url);
			Project p = new Project();			
			p.init();
			p.setProperty("JWSCacheDir", JWSCacheDir);
			ProjectHelper helper = ProjectHelper.getProjectHelper();			
			helper.parse(p, url);
			p.executeTarget(p.getDefaultTarget());
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage(), e);
		}
		
	}
	
	private void configLog() {
		File logLocationFile = new File(UIConstants.LOG_CONFIG);
		
		if (!logLocationFile.exists()) {
			loadDefaultLog();			
		} else {
			try {
				PropertyConfigurator.configure(UIConstants.LOG_CONFIG);
			} catch (Exception e) {
				loadDefaultLog();
			}
		}		
	}
	
	private void loadDefaultLog() {
		URL url = Thread.currentThread().getContextClassLoader().getResource(UIConstants.DEFAULT_LOG_CONFIG);
		PropertyConfigurator.configure(url);
	}
	
	public static void main(String[] args) {		
		ProgramStarter starter = new ProgramStarter();			
		starter.configLog();		
		starter.invokeAnt();
		
		if (!starter.isGTExist()) {
			if (starter.downloadGT(downloadLink)) {
				starter.extractGT(gtFileName);
			} else {
				System.exit(1);
			}
		}
				
		starter.setEnv(GLOBUS_LOCATION);
		starter.loadGUI();			
	}
}

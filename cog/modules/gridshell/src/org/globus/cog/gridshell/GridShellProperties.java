/*
 * 
 */
package org.globus.cog.gridshell;

import java.io.File;
import java.io.IOException;

import java.net.URI;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.model.ScopeException;
import org.globus.cog.gridshell.model.ScopeImpl;
import org.globus.cog.gridshell.model.ScopeableProperties;

/**
 * 
 */
public class GridShellProperties extends ScopeableProperties {

	private static Logger logger = Logger.getLogger(GridShellProperties.class);

	private static GridShellProperties defaultProperties;

	public static final String DEFAULT_PATH = ScopeImpl.getSystemScope().getValue("globus.home").toString();
	
	/**
	 * @param file
	 * @throws IOException
	 * @throws ScopeException
	 */
	public GridShellProperties(File file) throws IOException, ScopeException {
		super(file);
	}

	public GridShellProperties() throws IOException, ScopeException {
		super();
	}
	public static GridShellProperties getDefault() {
		if (defaultProperties == null) {
			try {
				defaultProperties = new GridShellProperties(
						getDefaultPropertiesFile("gridshell"));
			} catch (Exception exception) {
				logger.warn(
						"Couldn't load the default gridshell properties file. Using empty properties.",
						exception);
				try {
				    defaultProperties = new GridShellProperties();
				}catch(Exception e) {}
			}
		}
		return defaultProperties;
	}
	/**
	 * 1) Checks for -D<identifier>.configuration=<some-location>
	 * 2) Starts searching the classpath for ../etc/<identifier>.properties
	 * 3) returns globus.home/<identifier>.properties
	 * @return
	 */
	public static File getDefaultPropertiesFile(String identifier) {
	    String fileName = System.getProperty(identifier+".configuration");
	    if(fileName!=null) {
	        return new File(fileName);
	    }
	    
		String classPath = System.getProperty("java.class.path");
		String pathSeparator = System.getProperty("path.separator");
		if(classPath != null && pathSeparator!=null) {
			String[] paths = classPath.split(pathSeparator);
			for(int i=0;paths!=null && i<paths.length;i++) {
				logger.debug("path="+paths[i]);
				File file = new File(paths[i]);
								
				// try and file the <identifier>.properties in ../etc/<identifier>.properties
				try {
					if(file.isDirectory()) {
						file = file.getParentFile();
					}else if (file.isFile()) {
						file = file.getParentFile().getParentFile();
					}
					URI gshURI = new URI(file.getAbsoluteFile().toURI()+"/etc/"+identifier+".properties");
					File gridshellProp = new File(gshURI);
					if(gridshellProp.exists() && gridshellProp.isFile()) {						
						return gridshellProp;
					}else {
						logger.debug("Didn't find "+identifier+".properties at '"+gridshellProp+"'");
					}
				}catch(Exception exception) {
					logger.debug("Didn't find "+identifier+".properties relative to '"+paths[i]+"'",exception);
				}
			}
		}else {
			logger.warn("couldn't get either classPath='" + classPath
					+ "' or path separtor='" + pathSeparator
					+ "' to determine properties for "+identifier);
		}
		String DEFAULT_FILE = DEFAULT_PATH+identifier+".properties";
		logger.debug("returning default "+DEFAULT_FILE);
		return new File(DEFAULT_FILE);
	}
	
}

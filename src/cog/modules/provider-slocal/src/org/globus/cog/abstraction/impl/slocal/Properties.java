//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 20, 2005
 */
package org.globus.cog.abstraction.impl.slocal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.globus.cog.util.GridMap;

public class Properties extends java.util.Properties {
	private static final long serialVersionUID = -6453083236409802510L;

	private static Logger logger = Logger.getLogger(Properties.class);

	public static final String PROPERTIES = "provider-slocal.properties";

	private static Properties properties;

	public static synchronized Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
			properties.load();
		}
		return properties;
	}

	private void load() {
		setDefaults();
		InputStream is = getClass().getClassLoader().getResourceAsStream(PROPERTIES);
		if (is == null) {
			logger.warn("Could not find " + PROPERTIES + ". Using defaults.");
		}
		else {
			try {
				super.load(is);
			}
			catch (IOException e) {
			}
		}
	}

	private void setDefaults() {
		String gridMap = System.getProperty("grid.mapfile");
		if (gridMap == null) {
			gridMap = GridMap.DEFAULT_GRID_MAP;
		}
		setGridMap(gridMap);
		setSudo("/usr/bin/sudo");
		setNoSudo("/bin/sh");
		setWrapper(System.getProperty("COG_INSTALL_PATH") + File.separator + "libexec"
				+ File.separator + "job-wrapper");
	}

	public String getGridMap() {
		return super.getProperty("grid.mapfile");
	}

	public void setGridMap(String gridMap) {
		super.setProperty("grid.mapfile", gridMap);
	}

	public String getSudo() {
		return super.getProperty("sudo");
	}

	public void setSudo(String file) {
		super.setProperty("sudo", file);
	}

	public String getNoSudo() {
		return super.getProperty("nosudo");
	}

	public void setNoSudo(String file) {
		super.setProperty("nosudo", file);
	}

	public String getWrapper() {
		return super.getProperty("job.wrapper");
	}

	public void setWrapper(String wrapper) {
		super.setProperty("job.wrapper", wrapper);
	}
}

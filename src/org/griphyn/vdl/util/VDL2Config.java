//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 5, 2006
 */
package org.griphyn.vdl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.globus.common.CoGProperties;

public class VDL2Config extends Properties {
	public static final String CONFIG_FILE_NAME = "vdl2.properties";
	public static final String[] CONFIG_FILE_SEARCH_PATH = new String[] {
			System.getProperty("vds.home") + File.separator + "etc" + File.separator
					+ "vdl2.properties",
			System.getProperty("user.home") + File.separator + ".vdl2" + File.separator
					+ "vdl2.properties" };
	
	public static final String POOL_FILE = "sites.file";
	public static final String TC_FILE = "tc.file";
	public static final String IP_ADDRESS = "ip.address";

	private static VDL2Config config;

	public static VDL2Config getConfig() throws IOException {
		VDL2Config conf = getDefaultConfig();
		return conf.check();
	}

	private static synchronized VDL2Config getDefaultConfig() throws IOException {
		if (config == null) {
			config = new VDL2Config();
			for (int i = 0; i < CONFIG_FILE_SEARCH_PATH.length; i++) {
				config.load(CONFIG_FILE_SEARCH_PATH[i]);
			}
			String ip = config.getIP();
			if (ip != null) {
				CoGProperties.getDefault().setIPAddress(ip);
			}
		}
		return config;
	}

	public static VDL2Config getConfig(String file) throws IOException {
		VDL2Config config = getConfig();
		VDL2Config c = new VDL2Config(config);
		c.load(file);
		return c.check();
	}

	private List files, tried;

	private VDL2Config() {
		files = new LinkedList();
		tried = new LinkedList();
	}

	private VDL2Config(VDL2Config other) {
		this.putAll(other);
		this.files.addAll(other.files);
	}

	protected void load(String file) throws IOException {
		tried.add(file);
		File f = new File(file);
		if (f.exists()) {
			files.add(file);
			super.load(new FileInputStream(f));
		}
	}
	
	protected VDL2Config check() throws IOException {
		if (files.size() == 0) {
			throw new FileNotFoundException("No VDL2 configuration file found. Tried " + tried);
		}
		else {
			return this;
		}
	}

	/**
	 * Overriden to do variable expansion. Variables will be expanded if there
	 * is a system property with that name. Otherwise, the expansion will not
	 * occur.
	 */
	public synchronized Object put(Object key, Object value) {
		String svalue = (String) value;
		if (svalue.indexOf("${") == -1) {
			return super.put(key, value);
		}
		else {
			StringBuffer sb = new StringBuffer();
			int index = 0, last = 0;
			while (index >= 0) {
				index = svalue.indexOf("${", index);
				if (index >= 0) {
					if (last != index) {
						sb.append(svalue.substring(last, index));
						last = index;
					}
					int end = svalue.indexOf("}", index);
					if (end == -1) {
						sb.append(svalue.substring(index));
						break;
					}
					else {
						String name = svalue.substring(index + 2, end);
						String pval = System.getProperty(name);
						index = end + 1;
						if (pval == null) {
							continue;
						}
						else {
							sb.append(pval);
							last = index;
						}
					}
				}
			}
			sb.append(svalue.substring(last));
			return super.put(key, sb.toString());
		}
	}
	
	public String getPoolFile() {
		return getProperty(POOL_FILE);
	}
	
	public String getTCFile() {
		return getProperty(TC_FILE);
	}
	
	public String getIP() {
		return getProperty(IP_ADDRESS);
	}

	public String toString() {
		return "VDL2 configuration " + files;
	}
}

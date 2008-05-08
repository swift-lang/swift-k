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
	public static final String CONFIG_FILE_NAME = "swift.properties";
	public static final String[] CONFIG_FILE_SEARCH_PATH = new String[] {
			System.getProperty("vds.home") + File.separator + "etc" + File.separator
					+ CONFIG_FILE_NAME,
			System.getProperty("user.home") + File.separator + ".swift" + File.separator
					+ CONFIG_FILE_NAME };

	private static VDL2Config config;

	public static VDL2Config getConfig() throws IOException {
		VDL2Config conf = getDefaultConfig();
		return conf.check();
	}

	private static synchronized VDL2Config getDefaultConfig() throws IOException {
		checkDeprecatedConfigFile();
		if (config == null) {
			config = new VDL2Config();
			for (int i = 0; i < CONFIG_FILE_SEARCH_PATH.length; i++) {
				config.load(CONFIG_FILE_SEARCH_PATH[i]);
			}
		}
		return config;
	}

	public static VDL2Config getConfig(String file) throws IOException {
		VDL2Config c;
		try {
			VDL2Config config = getConfig();
			c = new VDL2Config(config);
		}
		catch (Exception e) {
			c = new VDL2Config();
		}
		c.load(file);
		return c.check();
	}

	private List files, tried;

	private VDL2Config() {
		files = new LinkedList();
		tried = new LinkedList();
		put(VDL2ConfigProperties.POOL_FILE, "${vds.home}/etc/sites.xml");
		put(VDL2ConfigProperties.TC_FILE, "${vds.home}/var/tc.data");
		put(VDL2ConfigProperties.LAZY_ERRORS, "false");
		put(VDL2ConfigProperties.CACHING_ALGORITHM, "LRU");
		put(VDL2ConfigProperties.PGRAPH, "false");
		put(VDL2ConfigProperties.PGRAPH_GRAPH_OPTIONS, "splines=\"compound\", rankdir=\"TB\"");
		put(VDL2ConfigProperties.PGRAPH_NODE_OPTIONS, "color=\"seagreen\", style=\"filled\"");
		put(VDL2ConfigProperties.CLUSTERING_ENABLED, "false");
		put(VDL2ConfigProperties.CLUSTERING_QUEUE_DELAY, "4");
		put(VDL2ConfigProperties.CLUSTERING_MIN_TIME, "60");
		put(VDL2ConfigProperties.KICKSTART_ENABLED, "maybe");
		put(VDL2ConfigProperties.KICKSTART_ALWAYS_TRANSFER, "false");
		put("throttle.submit", "4");
		put("throttle.host.submit", "2");
		put("throttle.transfers", "4");
		put("throttle.file.operations", "8");
		put("throttle.score.job.factor", "4");
		put(VDL2ConfigProperties.SITEDIR_KEEP, "false");
		
		put("replication.enabled", "false");
		put("replication.min.queue.time", "60");
		put("replication.limit", "3");
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
		String ip = getIP();
		if (ip != null) {
			CoGProperties.getDefault().setIPAddress(ip);
		}
		String tcpPortRange = getTCPPortRange();
		if (tcpPortRange != null) {
			CoGProperties.getDefault().put("tcp.port.range", tcpPortRange);
		}
	}

	protected VDL2Config check() throws IOException {
		if (files.size() == 0) {
			throw new FileNotFoundException("No Swift configuration file found. Tried " + tried);
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
		return getProperty(VDL2ConfigProperties.POOL_FILE);
	}

	public String getTCFile() {
		return getProperty(VDL2ConfigProperties.TC_FILE);
	}

	public String getIP() {
		return getProperty(VDL2ConfigProperties.IP_ADDRESS);
	}

	public String getTCPPortRange() {
		return getProperty(VDL2ConfigProperties.TCP_PORT_RANGE);
	}

	public boolean getLazyErrors() {
		return Boolean.valueOf(getProperty(VDL2ConfigProperties.LAZY_ERRORS, "true")).booleanValue();
	}

	public TriStateBoolean getKickstartEnabled() {
		return TriStateBoolean.valueOf(getProperty(VDL2ConfigProperties.KICKSTART_ENABLED, "false"));
	}

	public boolean getSitedirKeep() {
		return Boolean.valueOf(getProperty(VDL2ConfigProperties.SITEDIR_KEEP, "true")).booleanValue();
	}

	public String toString() {
		return "Swift configuration " + files;
	}

	public Object clone() {
		VDL2Config conf = new VDL2Config();
		conf.putAll(this);
		return conf;
	}

	// TODO this can be removed after 0.5 is released
	static public void checkDeprecatedConfigFile() {
		String fn = System.getProperty("user.home") + File.separator + ".vdl2" + File.separator + "vdl2.properties"; 
		File f = new File(fn);
		if(f.exists()) {
			System.err.println("The .vdl2 directory is deprecated. Swift has detected the presence of a now-unsupported configuration file, "+fn+". Configuration information will not be loaded from that file. Remove that file to suppress this message.");
		}
	}

}

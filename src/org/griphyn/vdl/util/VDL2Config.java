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

import org.apache.log4j.Logger;

import org.globus.cog.karajan.util.BoundContact;
import org.globus.common.CoGProperties;

public class VDL2Config extends Properties {

    private static final long serialVersionUID = 1L;

    public static final Logger logger = Logger.getLogger(VDL2Config.class);

	public static final String CONFIG_FILE_NAME = "swift.properties";
	public static final String[] CONFIG_FILE_SEARCH_PATH = new String[] {
			System.getProperty("swift.home") + File.separator + "etc" + File.separator
					+ CONFIG_FILE_NAME,
			System.getProperty("user.home") + File.separator + ".swift" + File.separator
					+ CONFIG_FILE_NAME,
			System.getProperty("vds.home") + File.separator + "etc" + File.separator
					+ CONFIG_FILE_NAME };

	private static VDL2Config config;

	public static VDL2Config getConfig() throws IOException {
		if(config == null) {
			config = getDefaultConfig();
		}
		return config.check();
	}

	public static synchronized VDL2Config getDefaultConfig() throws IOException {
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
			VDL2Config d = getConfig();
			c = new VDL2Config(d);
		}
		catch (Exception e) {
			c = new VDL2Config();
		}
		c.load(file);
		config = c;
		return config.check();
	}

	private List<String> files, tried;

	private VDL2Config() {
		files = new LinkedList<String>();
		tried = new LinkedList<String>();
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
		put(VDL2ConfigProperties.PROVENANCE_LOG, "false");
		
		put("replication.enabled", "false");
		put("replication.min.queue.time", "60");
		put("replication.limit", "3");
		put("status.mode", "files");
		put("wrapper.parameter.mode", "args");
		put("wrapper.invocation.mode", "absolute");
		
		put("cdm.broadcast.mode", "file");
		put("use.provider.staging", "false");
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
		String hostname = getHostName();
		if (hostname != null) {
		    CoGProperties.getDefault().setHostName(hostname);
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
	 * Overridden to do variable expansion. Variables will be expanded if there
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
	
	public String getHostName() {
	    return getProperty(VDL2ConfigProperties.HOST_NAME);
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

	public boolean getProvenanceLog() {
		return Boolean.valueOf(getProperty(VDL2ConfigProperties.PROVENANCE_LOG, "false")).booleanValue();
	}

	public String toString() {
		return "Swift configuration " + files;
	}

	public Object clone() {
		VDL2Config conf = new VDL2Config();
		conf.putAll(this);
		return conf;
	}

	public String getProperty(String name, BoundContact bc) {
		if(bc!=null) {
			if(logger.isDebugEnabled()) {
				logger.debug("Checking BoundContact "+bc+" for property "+name);
			}
			String prop = (String) bc.getProperty(name);
			if(prop != null) {
				return prop;
			}
		}
		if(logger.isDebugEnabled()) {
			logger.debug("Getting property "+name+" from global configuration");
		}
		return getProperty(name);
	}
}

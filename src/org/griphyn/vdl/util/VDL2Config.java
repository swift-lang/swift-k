/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Dec 5, 2006
 */
package org.griphyn.vdl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
		config.check();
		return config;
	}

	private List<String> files, tried;
	private Map<Object, ConfigPropertyType> types;
	private Map<Object, String> propertySource;
	private String currentFile;

	private VDL2Config() {
		files = new LinkedList<String>();
		tried = new LinkedList<String>();
		propertySource = new HashMap<Object, String>();
		types = new HashMap<Object, ConfigPropertyType>();
		put(VDL2ConfigProperties.POOL_FILE, "${swift.home}/etc/sites.xml", ConfigPropertyType.FILE);
		put(VDL2ConfigProperties.TC_FILE, "${swift.home}/etc/tc.data", ConfigPropertyType.FILE);
		put(VDL2ConfigProperties.LAZY_ERRORS, "false", ConfigPropertyType.BOOLEAN);
		put(VDL2ConfigProperties.CACHING_ALGORITHM, "LRU", ConfigPropertyType.STRING);
		put(VDL2ConfigProperties.CLUSTERING_ENABLED, "false", ConfigPropertyType.BOOLEAN);
		put(VDL2ConfigProperties.CLUSTERING_QUEUE_DELAY, "4", ConfigPropertyType.INT);
		put(VDL2ConfigProperties.CLUSTERING_MIN_TIME, "60", ConfigPropertyType.INT);
		put("throttle.submit", "4", ConfigPropertyType.INT);
		put("throttle.host.submit", "2", ConfigPropertyType.INT);
		put("throttle.transfers", "4", ConfigPropertyType.INT);
		put("throttle.file.operations", "8", ConfigPropertyType.INT);
		put("throttle.score.job.factor", "4", ConfigPropertyType.FLOAT);
		put(VDL2ConfigProperties.SITEDIR_KEEP, "false", ConfigPropertyType.BOOLEAN);
		put(VDL2ConfigProperties.PROVENANCE_LOG, "false", ConfigPropertyType.BOOLEAN);
		
		put("execution.retries", "0", ConfigPropertyType.INT);
		
		put("replication.enabled", "false", ConfigPropertyType.BOOLEAN);
		put("replication.min.queue.time", "60", ConfigPropertyType.INT);
		put("replication.limit", "3", ConfigPropertyType.INT);
		put("status.mode", "files", ConfigPropertyType.choices("files", "provider"));
		put("wrapper.parameter.mode", "args", ConfigPropertyType.choices("args", "files"));
		put("wrapper.invocation.mode", "absolute", ConfigPropertyType.choices("absolute", "relative"));
		
		// TODO what are the valid values here?
		put("cdm.broadcast.mode", "file");
		put("use.provider.staging", "false", ConfigPropertyType.BOOLEAN);
		put("use.wrapper.staging", "false", ConfigPropertyType.BOOLEAN);
		put("ticker.date.format", "", ConfigPropertyType.STRING);
		put("ticker.prefix", "Progress: ", ConfigPropertyType.STRING);
		
		put(VDL2ConfigProperties.FILE_GC_ENABLED, "true", ConfigPropertyType.BOOLEAN);
		put(VDL2ConfigProperties.DM_CHECKER, "on", ConfigPropertyType.ONOFF);
	}

	private VDL2Config(VDL2Config other) {
		this.putAll(other);
		this.files.addAll(other.files);
		this.propertySource.putAll(other.propertySource);
	}

	protected void load(String file) throws IOException {
	    this.currentFile = file;
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

	public void validateProperties() {
	    for (Map.Entry<Object, Object> e : this.entrySet()) {
	        checkType(e.getKey(), e.getValue());
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
	
	public synchronized Object put(Object key, Object value, ConfigPropertyType type) {
	    types.put(key, type);
	    return put(key, value);
	}

	/**
	 * Overridden to do variable expansion. Variables will be expanded if there
	 * is a system property with that name. Otherwise, the expansion will not
	 * occur.
	 */
	public synchronized Object put(Object key, Object value) {
	    propertySource.put(key, currentFile);
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
			value = sb.toString();
			return super.put(key, value);
		}
	}

	private void checkType(Object key, Object value) {
	    ConfigPropertyType type = types.get(key);
	    if (type != null) {
	        type.checkValue((String) key, (String) value, propertySource.get(key));
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
	
	private Boolean provenanceLogCached;

	public boolean getProvenanceLog() {
	    if (provenanceLogCached == null) {
	        provenanceLogCached = Boolean.valueOf(getProperty(VDL2ConfigProperties.PROVENANCE_LOG, "false"));
	    }
		return provenanceLogCached;
	}
	
	public boolean isTracingEnabled() {
        return Boolean.valueOf(getProperty(VDL2ConfigProperties.TRACING_ENABLED, "false")).booleanValue();
    }

	public String getTickerDateFormat() { 
		return getProperty("ticker.date.format");
	}
	
	public String getTickerPrefix() { 
		return getProperty("ticker.prefix");
	}
	
	public String toString() {
		return "Swift configuration (" + files + "): " + super.toString();
	}

	public Object clone() {
		VDL2Config conf = new VDL2Config();
		conf.putAll(this);
		conf.files.addAll(files);
        conf.propertySource.putAll(propertySource);
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

    public void setCurrentFile(String f) {
        this.currentFile = f;
    }
}

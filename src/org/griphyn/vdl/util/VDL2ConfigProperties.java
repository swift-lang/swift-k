/*
 * Created on Dec 23, 2006
 */
package org.griphyn.vdl.util;

import java.util.Map;
import java.util.TreeMap;

public class VDL2ConfigProperties {
	public static final String POOL_FILE = "sites.file";
	public static final String TC_FILE = "tc.file";
	public static final String IP_ADDRESS = "ip.address";
	public static final String HOST_NAME = "hostname";
	public static final String TCP_PORT_RANGE = "tcp.port.range";
	public static final String LAZY_ERRORS = "lazy.errors";
	public static final String PGRAPH = "pgraph";
	public static final String PGRAPH_GRAPH_OPTIONS = "pgraph.graph.options";
	public static final String PGRAPH_NODE_OPTIONS = "pgraph.node.options";
	public static final String CACHING_ALGORITHM = "caching.algorithm";
	public static final String CLUSTERING_ENABLED = "clustering.enabled";
	public static final String CLUSTERING_QUEUE_DELAY = "clustering.queue.delay";
	public static final String CLUSTERING_MIN_TIME = "clustering.min.time";
	public static final String KICKSTART_ENABLED = "kickstart.enabled";
	public static final String KICKSTART_ALWAYS_TRANSFER = "kickstart.always.transfer";
	public static final String WRAPPERLOG_ALWAYS_TRANSFER = "wrapperlog.always.transfer";
	public static final String SITEDIR_KEEP = "sitedir.keep";
	public static final String PROVENANCE_LOG = "provenance.log";
	public static final Map<String, PropInfo> PROPERTIES;

	static {
		PROPERTIES = new TreeMap<String, PropInfo>();
		PROPERTIES.put(POOL_FILE, new PropInfo("file",
				"Points to the location of the sites.xml file"));
		PROPERTIES.put(TC_FILE, new PropInfo("file", "Points to the location of the tc.data file"));
		PROPERTIES.put(IP_ADDRESS, new PropInfo("aaa.bbb.ccc.ddd",
				"Can be used to specify a publicly reacheable IP address for "
						+ "this machine which is generally used for Globus callbacks. "
						+ "Normally this should be auto-detected, but if you have "
						+ "multiple network cards or NAT then you may need to set this"));
		PROPERTIES.put(HOST_NAME, new PropInfo("string", 
		        "Can be used to specify a publicly reacheable DNS name or IP address for "
                        + "this machine which is generally used for Globus callbacks. "
                        + "Normally this should be auto-detected, but if you do "
                        + "not have a public DNS name, you may want to set this."));
		PROPERTIES.put(TCP_PORT_RANGE, new PropInfo("start,end",
				"A TCP port range can be specified to "
						+ "restrict the ports on which GRAM callback services are started. "
						+ "This is likely needed if your submit host is behind a firewall, "
						+ "in which case the firewall should be configured to allow "
						+ "incoming connections on ports in the range."));
		PROPERTIES.put(LAZY_ERRORS, new PropInfo("true|false",
				"Use a lazy mode to deal with errors. When set to 'true' swift will proceed with the "
						+ "execution until no more data can be derived because of "
						+ "errors in dependent steps. If set to 'false', an error will "
						+ "cause the execution to immediately stop"));
		PROPERTIES.put(PGRAPH, new PropInfo("true|false|<filename>",
				"Whether to generate a provenance "
						+ "graph or not. If 'true' is used, the file name for the graph will "
						+ "be chosen by swift."));
		PROPERTIES.put(PGRAPH_GRAPH_OPTIONS, new PropInfo("<string>",
				"Graph options to be passed to the .dot file. "
						+ "These will appear in the 'graph' clause in the .dot file: "
						+ "graph [<string>]; "));
		PROPERTIES.put(PGRAPH_NODE_OPTIONS, new PropInfo("<string>",
				"Node options to be passed to the .dot file."
						+ "These will appear in the 'node' clause in the .dot file: "
						+ "node [<string>]; "));
		PROPERTIES.put(CACHING_ALGORITHM, new PropInfo("[LRU]", "The algorithm to use for the "
				+ "swift file caching mechanism. LRU is the only one available now."));
		PROPERTIES.put(CLUSTERING_ENABLED, new PropInfo("true|false",
				"Whether to enable clustering of small jobs. If enabled, jobs with a "
						+ "max wall time which is less than the value of the "
						+ CLUSTERING_MIN_TIME
						+ " property will be clustered into one job which has a cummulative"
						+ " max wall time greater or equal to the value of the "
						+ CLUSTERING_MIN_TIME + " property."));
		PROPERTIES.put(CLUSTERING_QUEUE_DELAY, new PropInfo("<seconds>", "The delay at which "
				+ "the clustering code scans the clustering queue. A job marked for clustering "
				+ "will spend no more than the value of this property in the clustering queue."));
		PROPERTIES.put(CLUSTERING_MIN_TIME, new PropInfo("<seconds>", "The threshold determines "
				+ " if a job as being clusterable. Also represents the minimum cummulative "
				+ "wall time that a cluster will have."));
		PROPERTIES.put(KICKSTART_ENABLED, new PropInfo("<true|false|maybe>",
				"Controls the use of Kickstart by Swift. The \"maybe\" "
						+ "value tells Swift to use Kickstart on sites where it is available."));
		PROPERTIES.put(
				KICKSTART_ALWAYS_TRANSFER,
				new PropInfo(
						"<true|false>",
						"If Kickstart is used (see \""
								+ KICKSTART_ENABLED
								+ "\"), it controls when "
								+ "Kickstart records are transfered back to the submit host. If set to \"false\" "
								+ "Swift will only transfer a Kickstart record for a job when the job fails. "
								+ "If set to \"true\", Swift will transfer Kickstart records whether a job "
								+ "fails or not."));
		PROPERTIES.put(
				WRAPPERLOG_ALWAYS_TRANSFER,
				new PropInfo(
						"<true|false>",
								"Controls when "
								+ "wrapper logs are transfered back to the submit host. If set to \"false\" "
								+ "Swift will only transfer a wrapper log for a job when the job fails. "
								+ "If set to \"true\", Swift will transfer wrapper logs whether a job "
								+ "fails or not."));
		PROPERTIES.put(
				SITEDIR_KEEP,
				new PropInfo(
					"<true|false>",
					"If set to true, keeps remote site run directory after execution has completed."));


		PROPERTIES.put(
				PROVENANCE_LOG,
				new PropInfo(
					"<true|false>",
					"If set to true, will record provenance information in the log file"));

	}

	public static Map<String, PropInfo> getPropertyDescriptions() {
		return PROPERTIES;
	}

	public static String getPropertyDescription(String name) {
		return PROPERTIES.get(name).desc;
	}

	public static class PropInfo {
		public final String validValues;
		public final String desc;

		public PropInfo(String validValues, String desc) {
			this.validValues = validValues;
			this.desc = desc;
		}
	}
}

/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 17, 2005
 */
package org.globus.cog.coaster;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.globus.cog.util.TextFileLoader;

public class RemoteConfiguration {
	private static final Logger logger = Logger.getLogger(RemoteConfiguration.class);

	public static final String KEEPALIVE = "keepalive";
	public static final String CALLBACK = "callback";
	public static final String RECONNECT = "reconnect";
	public static final String POLL = "poll";
	public static final String BUFFER = "buffer";
	public static final String HEARTBEAT = "heartbeat";

	private List<Entry> entries;
	private static final Entry DEFAULT;
	static {
		DEFAULT = new Entry(".*", "CALLBACK, POLL(120)");
	}

	private static RemoteConfiguration def;

	public synchronized static RemoteConfiguration getDefault() {
		if (def == null) {
			def = new RemoteConfiguration();
		}
		return def;
	}

	public RemoteConfiguration() {
		entries = new LinkedList<Entry>();
		TextFileLoader tfl = new TextFileLoader();
		String conf = TextFileLoader.loadFromResource("remote.properties");
		StringTokenizer st = new StringTokenizer(conf, "\n");
		while (st.hasMoreTokens()) {
			String line = st.nextToken().trim();
			if (line.startsWith("#")) {
				continue;
			}
			if (!line.startsWith("\"")) {
				logger.warn("Invalid line in remote.properties: " + line);
				continue;
			}
			int eq = line.lastIndexOf('"');
			String regexp = line.substring(1, eq);
			String options = line.substring(eq + 1);
			entries.add(0, new Entry(regexp, options));
		}
	}

	public void append(Entry entry) {
		entries.add(entry);
	}

	public void prepend(Entry entry) {
		entries.add(0, entry);
	}

	public Entry find(String host) {
		logger.info("Find: " + host);
		for (Entry e : entries) {
			logger.info("Find: " + e.getUnparsed() + " - " + host);
			if (e.compiled.matcher(host).matches()) {
				return e;
			}
		}
		return DEFAULT;
	}

	public static class Entry {
		private final String regexp;
		private String unparsed;
		private Pattern compiled;
		private final Map<String, String> options;

		public Entry(String regexp, Map<String, String> options, String unparsed) {
			this.regexp = regexp;
			this.options = options;
			this.unparsed = unparsed;
			if (regexp != null) {
				this.compiled = Pattern.compile(regexp);
			}
		}

		public Entry(String regexp, String options) {
			this.regexp = regexp;
			if (regexp != null) {
				this.compiled = Pattern.compile(regexp);
			}
			this.unparsed = options;
			this.options = new HashMap<String, String>();
			StringTokenizer ot = new StringTokenizer(options, ",");
			while (ot.hasMoreTokens()) {
				String opt = ot.nextToken().trim().toLowerCase();
				int op = opt.indexOf('(');
				if (op != -1) {
					if (!opt.endsWith(")")) {
						logger.warn("Invalid option in remote.properties: " + opt + " (" + options
								+ ")");
						continue;
					}
					String arg = opt.substring(op + 1, opt.length() - 1);
					opt = opt.substring(0, op);
					this.options.put(opt, arg);
				}
				else {
					this.options.put(opt, null);
				}
			}
		}

		public Collection<String> getOptions() {
			return options.keySet();
		}

		public String getArg(String option) {
			return options.get(option);
		}

		public boolean hasOption(String option) {
			return options.containsKey(option);
		}

		public boolean hasArg(String option) {
			return options.get(option) != null;
		}

		public void addOption(String option, String arg) {
			options.put(option, arg);
			if (arg == null) {
				unparsed += ", " + option;
			}
			else {
				unparsed += ", " + option + "(" + arg + ")";
			}
		}

		public synchronized String getUnparsed() {
			if (unparsed == null) {
				StringBuffer sb = new StringBuffer();
				Iterator<Map.Entry<String, String>> i = options.entrySet().iterator();
				while (i.hasNext()) {
					Map.Entry<String, String> entry = i.next();
					sb.append(entry.getKey());
					if (entry.getValue() != null) {
						sb.append('(');
						sb.append(entry.getValue());
						sb.append(')');
					}
					if (i.hasNext()) {
						sb.append(", ");
					}
				}
				unparsed = sb.toString();
			}
			return unparsed;
		}

		public String getRegexp() {
			return regexp;
		}

		public String toString() {
			return regexp + " -> " + options.toString();
		}

		public Entry copy() {
			return new Entry(regexp, options, unparsed);
		}
	}
}

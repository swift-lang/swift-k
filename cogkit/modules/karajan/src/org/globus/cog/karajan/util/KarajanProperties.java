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
 * Created on Jun 21, 2005
 */
package org.globus.cog.karajan.util;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

public class KarajanProperties extends Properties {
	private static final long serialVersionUID = 1198467724006509622L;

	private static final Logger logger = Logger.getLogger(KarajanProperties.class);

	private static KarajanProperties def, restricted;

	private final List<String> defaultIncludeDirs;

	public synchronized static KarajanProperties getDefault() {
		if (def == null) {
			def = parseProperties();
		}
		return def;
	}
	
	public synchronized static KarajanProperties getRestricted() {
		if (restricted == null) {
			restricted = parseRestrictedProperties();
		}
		return restricted;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<String> getPropertyNames() {
		return (Collection<String>) Collections.list(super.propertyNames());
	}


	protected static KarajanProperties parseProperties(String name, KarajanProperties properties)
			throws Exception {
		URL url = KarajanProperties.class.getClassLoader().getResource(name);
		if (properties == null) {
			properties = new KarajanProperties();
		}
		if (url != null) {
			properties.load(url.openStream());
		}
		else {
			throw new Exception("Invalid resource: " + name);
		}
		properties.defaultIncludeDirs.clear();
		for (Map.Entry<Object, Object> e : properties.entrySet()) {
			String propName = (String) e.getKey();
			if (propName.equals("include.dirs")) {
				properties.addDefaultIncludeDirs(properties.getProperty(propName));
			}
		}
		return properties;
	}

	protected static KarajanProperties parseProperties() {
		KarajanProperties props = null;
		try {
			props = parseProperties("karajan-default.properties", props);
		}
		catch (Exception e) {
			logger.warn("Failed to load default properties", e);
		}
		try {
			props = parseProperties("karajan.properties", props);
		}
		catch (Exception e) {
			logger.debug("Failed to load properties", e);
		}
		if (props == null) {
			return new KarajanProperties();
		}
		else {
			return props;
		}
	}
	
	protected static KarajanProperties parseRestrictedProperties() {
		KarajanProperties props = null;
		try {
			props = parseProperties("karajan-default.properties", props);
		}
		catch (Exception e) {
			logger.warn("Failed to load default properties", e);
		}
		try {
			props = parseProperties("karajan-restricted.properties", props);
		}
		catch (Exception e) {
			logger.error("Failed to load restricted properties", e);
			throw new RuntimeException("Failed to load restricted properties");
		}
		return props;
	}

	public KarajanProperties() {
		this.defaultIncludeDirs = new LinkedList<String>();
	}

	public List<String> getDefaultIncludeDirs() {
		return defaultIncludeDirs;
	}

	public void addDefaultIncludeDir(String dir) {
		defaultIncludeDirs.add(dir);
	}

	public void addDefaultIncludeDirs(String dirs) {
		StringTokenizer tokenizer = new StringTokenizer(dirs, ":");
		while (tokenizer.hasMoreTokens()) {
			addDefaultIncludeDir(tokenizer.nextToken());
		}
	}

	public void insertDefaultIncludeDir(String dir) {
		defaultIncludeDirs.add(0, dir);
	}

	public void removeDefaultIncludeDir(String dir) {
		defaultIncludeDirs.remove(dir);
	}

}

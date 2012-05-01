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
 * Created on Nov 21, 2006
 */
package org.griphyn.vdl.util;

import java.util.StringTokenizer;

/**
 * 
 * Encapsulates a fully qualified name (namespace:name:version)
 * 
 */
public class FQN {
	private final String namespace;
	private final String name;
	private final String version;
	private final int hashCode; 

	public FQN(String namespace, String name, String version) {
		this.namespace = namespace;
		this.name = name;
		this.version = version;
		this.hashCode = _hashCode();
	}

	public FQN(String fqn) {
		/*
		 * 1. name 2. namespace:name 3. :name:version 4. namespace:name:version
		 */
		if (fqn == null || fqn.length() == 0) {
			throw new IllegalArgumentException("FQN is null/empty");
		}
		String[] s = split(fqn);
		if (fqn.charAt(0) == ':') {
			// 3
			namespace = null;
			name = s[0];
			version = s[1];
		}
		else if (s.length == 1) {
			// 1
			namespace = null;
			name = s[0];
			version = null;
		}
		else if (s.length == 2) {
			// 2
			namespace = s[0];
			name = s[1];
			version = null;
		}
		else if (s.length == 3) {
			namespace = s[0];
			name = s[1];
			version = s[2];
		}
		else {
			throw new IllegalArgumentException("Invalid FQN: " + fqn);
		}
		this.hashCode = _hashCode();
	}

	private String[] split(String fqn) {
		StringTokenizer st = new StringTokenizer(fqn, ":");
		String[] s = new String[st.countTokens()];
		for (int i = 0; i < s.length; i++) {
			s[i] = st.nextToken();
		}
		return s;
	}

	public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getVersion() {
		return version;
	}

	public String toString() {
		if (namespace == null) {
			if (version == null) {
				return name;
			}
			else {
				return ':' + name + ':' + version;
			}
		}
		else {
			if (version == null) {
				return namespace + ':' + name;
			}
			else {
				return namespace + ':' + name + ':' + version;
			}
		}
	}

	public boolean equals(Object o) {
		if (o instanceof FQN) {
			FQN of = (FQN) o;
			if (hashCode != of.hashCode) {
			    return false;
			}
			return cmpStr(namespace, of.namespace) && cmpStr(name, of.name)
					&& cmpStr(version, of.version);
		}
		else {
			return false;
		}
	}
	
	private int _hashCode() {
		int hc = 0;
		hc += namespace == null ? 0 : namespace.hashCode();
		hc += name == null ? 0 : name.hashCode();
		hc += version == null ? 0 : version.hashCode();
		return hc;
	}
	
	public int hashCode() {
	    return hashCode;
	}

	private boolean cmpStr(String s1, String s2) {
		if (s1 == null) {
			return s2 == null;
		}
		else {
			return s1.equals(s2);
		}
	}
}

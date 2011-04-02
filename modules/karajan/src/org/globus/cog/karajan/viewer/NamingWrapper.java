//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 18, 2005
 */
package org.globus.cog.karajan.viewer;

public class NamingWrapper {
	private String prefix, name;

	public NamingWrapper() {
	}

	public NamingWrapper(String full) {
		set(full);
	}

	public void set(String full) {
		int i = full.indexOf(':');
		if (i != -1) {
			prefix = full.substring(0, i - 1);
			name = full.substring(i + 1);
		}
		else {
			prefix = null;
			name = full;
		}
	}

	public boolean equals(Object obj) {
		if (obj instanceof NamingWrapper) {
			NamingWrapper w = (NamingWrapper) obj;
			if (prefix == null || w.prefix == null) {
				return name.equals(w.name);
			}
			else {
				return prefix.equals(w.prefix) && name.equals(w.name);
			}
		}
		return false;
	}

	public int hashCode() {
		return name.hashCode();
	}
}
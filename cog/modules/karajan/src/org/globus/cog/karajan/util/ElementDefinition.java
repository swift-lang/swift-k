//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 19, 2005
 */
package org.globus.cog.karajan.util;


public final class ElementDefinition {
	private final String nsprefix, name;
	private final Object def;
	private final boolean restricted;
	private int cachedHash;

	public ElementDefinition(String nsprefix, String name, Object def, boolean restricted) {
		this.nsprefix = nsprefix;
		this.name = name;
		this.def = def;
		this.restricted = restricted;
	}

	public Object getDef() {
		return def;
	}

	public String getName() {
		return name;
	}

	public String getNsprefix() {
		return nsprefix;
	}

	public String toString() {
		if (nsprefix != null) {
			return nsprefix + ":" + name + " = " + def;
		}
		else {
			return name + " = " + def;
		}
	}

	public boolean isRestricted() {
		return restricted;
	}

	public boolean equals(Object obj) {
		if (obj instanceof ElementDefinition) {
			ElementDefinition other = (ElementDefinition) obj;
			if (cachedHash != 0 && other.cachedHash != 0 && cachedHash != other.cachedHash) {
				return false;
			}
			if (nsprefix == null) {
				if (other.nsprefix != null) {
					return false;
				}
			}
			else if (!nsprefix.equals(other.nsprefix)) {
				return false;
			}
			if (!name.equals(other.name)) {
				return false;
			}
			if (restricted != other.restricted) {
				return false;
			}
			if (!def.equals(other.def)) {
				return false;
			}
			return true;
		}
		else {
			return false;
		}
	}

	public int hashCode() {
		if (cachedHash != 0) {
			return cachedHash;
		}
		int hash = 0;
		if (nsprefix != null) {
			hash += nsprefix.hashCode();
		}
		hash += name.hashCode();
		hash += def.hashCode();
		hash += restricted ? 0 : 10;
		cachedHash = hash;
		return hash;
	}
}

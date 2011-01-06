
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on May 7, 2004
 */
package org.globus.cog.karajan.util;

import java.util.Map;

public class Property implements Map.Entry {
	private String name, type;
	private Object value;

	public Property() {
	}

	public Property(String name, String value, String type) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	/*public Object getTypedValue() {
		if (type == null) {
			return value;
		}
		else if (type.equalsIgnoreCase("String")) {
			return value;
		}
		else if (type.equalsIgnoreCase("Integer")) {
			return Integer.valueOf(value);
		}
		else if (type.equalsIgnoreCase("Double")) {
			return Double.valueOf(value);
		}
		else if (type.equalsIgnoreCase("Float")) {
			return Float.valueOf(value);
		}
		else if (type.equalsIgnoreCase("Boolean")) {
			return Boolean.valueOf(value);
		}
		else if (type.equalsIgnoreCase("Long")) {
			return Long.valueOf(value);
		}
		else {
			return value;
		}
	}*/

	public Object getKey() {
		return name;
	}

	public Object setValue(Object value) {
		Object old = this.value;
		this.value = value;
		return old;
	}
	
	public String toString() {
		return name + "=" + value;
	}
}


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

public class Property implements Map.Entry<String, Object> {
	private String name;
	private Object value;

	public Property() {
	}

	public Property(String name, Object value) {
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

	public String getKey() {
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

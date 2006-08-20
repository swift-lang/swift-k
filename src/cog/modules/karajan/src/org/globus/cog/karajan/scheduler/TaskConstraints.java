
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 1, 2004
 *
 */
package org.globus.cog.karajan.scheduler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TaskConstraints {
	private static final long serialVersionUID = -5513157963657615563L;
	private Map map;
	
	public TaskConstraints() {
	}
	
	private synchronized Map getMap() {
		if (map == null) {
			map = new HashMap();
		}
		return map;
	}
	
	public void addConstraint(String name, Object value) {
		getMap().put(name, value);
	}
	
	public Object getConstraint(String name) {
		return getMap().get(name);
	}
	
	public Collection getConstraintNames() {
		return getMap().entrySet();
	}
	
	public String toString() {
		return getMap().toString();
	}
}

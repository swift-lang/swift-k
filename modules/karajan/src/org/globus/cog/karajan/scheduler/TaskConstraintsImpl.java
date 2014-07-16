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

public class TaskConstraintsImpl implements TaskConstraints {
	private static final long serialVersionUID = -5513157963657615563L;
	private Map<String, Object> map;

	public TaskConstraintsImpl() {
	}

	private synchronized Map<String, Object> getMap() {
		if (map == null) {
			map = new HashMap<String, Object>();
		}
		return map;
	}

	public void addConstraint(String name, Object value) {
		getMap().put(name, value);
	}

	public Object getConstraint(String name) {
		return getMap().get(name);
	}

	public Collection<String> getConstraintNames() {
		return getMap().keySet();
	}

	public String toString() {
		return getMap().toString();
	}

	public boolean equals(Object obj) {
		if (obj instanceof TaskConstraintsImpl) {
			TaskConstraintsImpl tc = (TaskConstraintsImpl) obj;
			if (map == null) {
				return tc.map == null;
			}
			else {
				return map.equals(tc.map);
			}
		}
		else {
			return false;
		}
	}

	public int hashCode() {
		return map == null ? 0 : map.hashCode();
	}
}

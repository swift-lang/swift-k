// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Nov 3, 2003
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.HashMap;
import java.util.Map;

import org.globus.cog.karajan.workflow.KarajanRuntimeException;

public class UIDMap {
	private final Map map = new HashMap();
	private final Map rmap = new HashMap();
	private int nextId;

	public UIDMap() {
		nextId = 0;
	}

	public void put(Integer uid, FlowElement node) {
		if (rmap.containsKey(node)) {
			Integer oldId = (Integer) rmap.get(node);
			map.remove(oldId);
		}
		if (map.containsKey(uid)) {
			FlowElement oldNode = (FlowElement) map.get(uid);
			map.remove(oldNode);
		}
		map.put(uid, node);
		rmap.put(node, uid);
		int id = uid.intValue();
		if (id > nextId) {
			nextId = id + 1;
		}
	}

	public FlowElement get(Integer uid) {
		FlowElement fe = (FlowElement) map.get(uid);
		if (fe == null) {
			throw new KarajanRuntimeException("No element with UID " + uid + " found");
		}
		return fe;
	}

	public Integer get(FlowElement node) {
		return (Integer) map.get(node);
	}

	public Integer nextUID() {
		return new Integer(nextId++);
	}

	public void putAll(UIDMap uidMap) {
		map.putAll(uidMap.map);
	}

	public boolean contains(Integer uid) {
		return map.containsKey(uid);
	}
}

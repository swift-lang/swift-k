/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.items;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractStatefulItem implements StatefulItem {
	private StatefulItem parent;
	private Set children;
	private final String id;

	public AbstractStatefulItem(String id) {
		if (id == null) {
			throw new IllegalArgumentException("The id cannot be null");
		}
		this.id = id;
	}

	public void addChild(StatefulItem child) {
		synchronized (this) {
			if (children == null) {
				children = new HashSet();
			}
		}
		synchronized (children) {
			children.add(child);
		}
	}

	public Collection getChildren() {
		return children;
	}

	public StatefulItem getParent() {
		if (parent != null && parent.getItemClass().equals(StatefulItemClass.BRIDGE)) {
			return parent.getParent();
		}
		else {
			return parent;
		}
	}

	public void removeChild(StatefulItem child) {
		/*if (children == null) {
			throw new IllegalStateException("No such child: " + child);
		}
		synchronized (children) {
			children.remove(child);
		}*/
	}

	public void setParent(StatefulItem parent) {
		this.parent = parent;
	}

	public String getID() {
		return id;
	}
}

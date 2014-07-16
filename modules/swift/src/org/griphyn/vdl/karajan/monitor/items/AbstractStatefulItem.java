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
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.items;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractStatefulItem implements StatefulItem {
	private StatefulItem parent;
	private Set<StatefulItem> children;
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
				children = new HashSet<StatefulItem>();
			}
		}
		synchronized (children) {
			children.add(child);
		}
	}

	public Collection<StatefulItem> getChildren() {
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
		if (children == null) {
			throw new IllegalStateException("No such child: " + child);
		}
		synchronized (children) {
			children.remove(child);
		}
	}

	public void setParent(StatefulItem parent) {
		this.parent = parent;
	}

	public String getID() {
		return id;
	}

    @Override
    public void addListener(Listener l) {
        // not implemented by default
    }
}

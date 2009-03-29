/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.items;

import java.util.Collection;

public interface StatefulItem {
    StatefulItem getParent();
	void setParent(StatefulItem parent);
	
	void addChild(StatefulItem child);
	void removeChild(StatefulItem child);
	Collection getChildren();
	
	StatefulItemClass getItemClass();
	
	String getID();
}

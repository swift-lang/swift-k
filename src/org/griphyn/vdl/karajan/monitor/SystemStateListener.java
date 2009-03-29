/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor;

import org.griphyn.vdl.karajan.monitor.items.StatefulItem;

public interface SystemStateListener {
	public static final int ITEM_ADDED = 0;
	public static final int ITEM_REMOVED = 1;
	public static final int ITEM_UPDATED = 2;
	
	void itemUpdated(int updateType, StatefulItem item);
}

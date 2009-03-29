/*
 * Created on Sep 24, 2007
 */
package org.griphyn.vdl.karajan.monitor.common;

import org.griphyn.vdl.karajan.monitor.items.StatefulItem;

public interface StatefulItemModel {
	StatefulItem getItem(int row);
}

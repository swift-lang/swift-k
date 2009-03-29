/*
 * Created on Feb 19, 2007
 */
package org.griphyn.vdl.karajan.monitor.items;

public class Bridge extends AbstractStatefulItem {
	public Bridge(String id) {
		super(id);
	}

	public StatefulItemClass getItemClass() {
		return StatefulItemClass.BRIDGE;
	}

	public String toString() {
		return "->" + getParent();
	}
}

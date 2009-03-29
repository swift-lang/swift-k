/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.SystemStateListener;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;

public abstract class AbstractMonitor implements Monitor, SystemStateListener {
	private SystemState state;

	public SystemState getState() {
		return state;
	}

	public void setState(SystemState state) {
		this.state = state;
		state.addListener(this);
	}

	public void itemUpdated(int updateType, StatefulItem item) {
	}
}

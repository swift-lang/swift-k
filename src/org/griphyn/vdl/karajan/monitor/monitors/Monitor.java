/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors;

import org.griphyn.vdl.karajan.monitor.SystemState;

public interface Monitor {
	void setState(SystemState state);

    void shutdown();
}

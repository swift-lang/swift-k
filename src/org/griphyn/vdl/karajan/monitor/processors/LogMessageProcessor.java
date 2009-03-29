/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.processors;

import org.griphyn.vdl.karajan.monitor.SystemState;

public interface LogMessageProcessor {
	void processMessage(SystemState state, Object message, Object details);
	
	String getSupportedSource();
	
	Object getSupportedCategory();
}

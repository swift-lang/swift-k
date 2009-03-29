/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.processors;

import org.apache.log4j.Level;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.Bridge;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;

public class ProcedureProcessor implements LogMessageProcessor {

	public Object getSupportedCategory() {
		return Level.INFO;
	}

	public String getSupportedSource() {
		return "org.griphyn.vdl.karajan.lib.Log";
	}

	public void processMessage(SystemState state, Object message, Object details) {
		SimpleParser p = new SimpleParser(String.valueOf(message));
		try {
			String appid, threadid, replicationgroup;
			if (p.matchAndSkip("PROCEDURE ")) {
			    state.incTotal();
			}
			else if (p.matchAndSkip("END_SUCCESS ")) {
			    state.incCompleted();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}

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

public class AppThreadProcessor implements LogMessageProcessor {

	public Object getSupportedCategory() {
		return Level.DEBUG;
	}

	public String getSupportedSource() {
		return "org.griphyn.vdl.karajan.lib.Log";
	}

	public void processMessage(SystemState state, Object message, Object details) {
		SimpleParser p = new SimpleParser(String.valueOf(message));
		try {
			String appid, threadid, replicationgroup;
			if (p.matchAndSkip("THREAD_ASSOCIATION ")) {
			    p.skip("jobid=");
				appid = p.word();
				p.skip("thread=");
				threadid = p.word();
				p.skip("replicationGroup=");
				replicationgroup = p.word();
				
				StatefulItem app = state.getItemByID(appid, StatefulItemClass.APPLICATION);
				if (app == null) {
					app = new ApplicationItem(appid);
					state.addItem(app);
				}
				
				StatefulItem thread = state.getItemByID(threadid, StatefulItemClass.BRIDGE);
				if (thread == null) {
					thread = new Bridge(threadid);
					thread.setParent(app);
					state.addItem(thread);
					app.addChild(thread);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}

/*
 * Created on Aug 28, 2008
 */
package org.griphyn.vdl.karajan.monitor.processors;

import org.apache.log4j.Level;
import org.globus.cog.karajan.scheduler.WeightedHostScoreScheduler;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.HostItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;

public class SchedulerInfoProcessor implements LogMessageProcessor {

	public Object getSupportedCategory() {
		return Level.INFO;
	}

	public String getSupportedSource() {
		return WeightedHostScoreScheduler.class.getName();
	}

	public void processMessage(SystemState state, Object message, Object details) {
		SimpleParser p = new SimpleParser(String.valueOf(message));
		try {
		    if (p.matchAndSkip("Sorted: [")) {
		        HostItem hi;
		        boolean done = false;
		        while (!done) {
		            p.beginToken();
		            p.markTo(":");
		            hi = new HostItem(p.getToken().trim());
		            p.skipTo("(");
		            p.beginToken();
		            p.markTo(")");
		            hi.setScore(p.getToken());
		            p.skip(":");
		            p.beginToken();
		            p.markTo("/");
		            hi.setJobsRunning(p.getToken());
		            p.beginToken();
		            p.markTo(" ");
		            hi.setJobsAllowed(p.getToken());
		            p.skip("overload: ");
		            p.beginToken();
		            try {
		                p.markTo(",");
		                hi.setOverload(p.getToken());
		            }
		            catch (ParsingException e) {
		                p.markTo("]");
		                hi.setOverload(p.getToken());
		                done = true;
		            }
	                state.addItem(hi);
	                state.itemUpdated(hi);
		        }
		    }
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
	}
}

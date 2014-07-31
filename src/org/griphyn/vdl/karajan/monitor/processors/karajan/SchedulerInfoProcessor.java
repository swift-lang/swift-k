/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Aug 28, 2008
 */
package org.griphyn.vdl.karajan.monitor.processors.karajan;

import org.apache.log4j.Level;
import org.globus.cog.karajan.scheduler.WeightedHostScoreScheduler;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.HostItem;
import org.griphyn.vdl.karajan.monitor.processors.AbstractMessageProcessor;
import org.griphyn.vdl.karajan.monitor.processors.ParsingException;
import org.griphyn.vdl.karajan.monitor.processors.SimpleParser;

public class SchedulerInfoProcessor extends AbstractMessageProcessor {

	public Level getSupportedLevel() {
		return Level.INFO;
	}

	public Class<?> getSupportedSource() {
		return WeightedHostScoreScheduler.class;
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

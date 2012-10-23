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
package org.griphyn.vdl.karajan.monitor.processors;

import org.apache.log4j.Level;
import org.griphyn.vdl.karajan.lib.RuntimeStats.ProgressTicker;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.items.SummaryItem;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.SummaryPane;

public class SummaryProcessor extends AbstractMessageProcessor {

	public Level getSupportedLevel() {
		return Level.INFO;
	}

	public Class<?> getSupportedSource() {
		return ProgressTicker.class;
	}

	public void processMessage(SystemState state, Object message, Object details) {
		String msg = String.valueOf(message);
		SummaryItem s;
		synchronized(this) {
		    s = (SummaryItem) state.getItemByID(SummaryItem.ID, StatefulItemClass.WORKFLOW);
		    if (s == null) {
		        s = new SummaryItem();
		        state.addItem(s);
		    }
		}
		String[] pairs = msg.split("  ");
		for (String key : SummaryPane.STATES) {
		    s.setCount(key, 0);
		}
		for (String pair : pairs) {
		    if (pair.equals("")) {
		        continue;
		    }
		    String[] v = pair.split(":");
		    s.setCount(v[0].trim(), Integer.parseInt(v[1]));
		}
		state.itemUpdated(s);
	}
}

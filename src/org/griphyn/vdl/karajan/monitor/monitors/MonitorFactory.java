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
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors;

import java.util.HashMap;
import java.util.Map;

import org.griphyn.vdl.karajan.monitor.monitors.ansi.ANSIMonitor;
import org.griphyn.vdl.karajan.monitor.monitors.swing.SwingMonitor;

public class MonitorFactory {
	private static Map<String, Class<? extends Monitor>> classes;

	static {
		classes = new HashMap<String, Class<? extends Monitor>>();
		classes.put("text", TextMonitor.class);
		classes.put("ANSI", ANSIMonitor.class);
		classes.put("Swing", SwingMonitor.class);
	}

	public static Monitor newInstance(String type) throws InstantiationException,
			IllegalAccessException {
		Class<? extends Monitor> cls = classes.get(type);
		if (cls == null) {
			throw new IllegalArgumentException("Unsupported monitor type (" + type
					+ "). The supported types are: " + classes.keySet());
		}
		return cls.newInstance();
	}
}

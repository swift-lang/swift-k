/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors;

import java.util.HashMap;
import java.util.Map;

import org.griphyn.vdl.karajan.monitor.monitors.ansi.ANSIMonitor;
import org.griphyn.vdl.karajan.monitor.monitors.swing.SwingMonitor;

public class MonitorFactory {
	private static Map classes;

	static {
		classes = new HashMap();
		classes.put("text", TextMonitor.class);
		classes.put("ANSI", ANSIMonitor.class);
		classes.put("Swing", SwingMonitor.class);
	}

	public static Monitor newInstance(String type) throws InstantiationException,
			IllegalAccessException {
		Class cls = (Class) classes.get(type);
		if (cls == null) {
			throw new IllegalArgumentException("Unsupported monitor type (" + type
					+ "). The supported types are: " + classes.keySet());
		}
		return (Monitor) cls.newInstance();
	}
}

/*
 * Created on Aug 29, 2008
 */
package org.griphyn.vdl.karajan.monitor.processors;

import org.griphyn.vdl.karajan.lib.Execute;

public class JobProcessor extends TaskProcessor {

	public String getSupportedSource() {
		return Execute.class.getName();
	}	
}

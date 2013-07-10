
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 25, 2004
 */
package org.globus.cog.gui.grapheditor.util;

import javax.swing.Icon;

import org.apache.log4j.Logger;

public class ConsoleStatusRenderer implements StatusRenderer {
	private static Logger logger = Logger.getLogger(ConsoleStatusRenderer.class);

	public void setStatusText(String text) {
		logger.info(text);
	}

	public void setStatusIcon(Icon icon) {
	}

	public void initializeProgress(int size) {
	}

	public void setProgress(int value) {	
	}

	public void incrementProgress() {
	}

	public void removeProgress() {
	}
}

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 26, 2006
 */
package org.globus.cog.gui.grapheditor.canvas;

import java.awt.Color;

public interface LogConsole {
	void output(Color color, String message);

	boolean isVisible();

	void setVisible(boolean b);
}

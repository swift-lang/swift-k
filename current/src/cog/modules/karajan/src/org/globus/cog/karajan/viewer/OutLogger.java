//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 26, 2006
 */
package org.globus.cog.karajan.viewer;

import java.awt.Color;

import org.globus.cog.gui.grapheditor.targets.swing.LogWindow;
import org.globus.cog.karajan.arguments.AbstractWriteOnlyVariableArguments;

public class OutLogger extends AbstractWriteOnlyVariableArguments {

	private LogWindow log;
	private boolean commutative;
	private Color color;

	public OutLogger(LogWindow log, boolean commutative, Color color) {
		this.log = log;
		this.commutative = commutative;
		this.color = color;
	}

	public void append(Object value) {
		log.output(color, String.valueOf(value));
	}

	public boolean isCommutative() {
		return commutative;
	}
}

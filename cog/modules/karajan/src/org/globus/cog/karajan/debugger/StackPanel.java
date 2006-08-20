//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 18, 2005
 */
package org.globus.cog.karajan.debugger;

import java.awt.Color;

import org.globus.cog.karajan.stack.VariableStack;


public class StackPanel extends DebuggerPanel {
	private static final long serialVersionUID = -8333615124078031598L;

	public StackPanel() {
		super();
		setBackground(Color.WHITE);
		this.setLayout(new SimpleLayout());
	}

	public void removeStack(VariableStack stack) {
		removeAll();
		validate();
		repaint();
	}

	public void addStack(VariableStack stack) {
		removeAll();
		StackEntry se = new StackEntry(stack);
		add(se);
		se.setLocation(0, 0);
		validate();
		repaint();
	}
}

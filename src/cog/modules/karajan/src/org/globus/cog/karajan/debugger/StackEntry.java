// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 9, 2005
 */
package org.globus.cog.karajan.debugger;

import java.util.Iterator;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.globus.cog.karajan.stack.StackFrame;
import org.globus.cog.karajan.stack.VariableStack;

public class StackEntry extends JTree {
	private static final long serialVersionUID = -4056005166103174115L;
	
	private DefaultMutableTreeNode root;
	private VariableStack stack;

	public StackEntry(VariableStack stack) {
		this(new StackNode("Stack", stack));
		this.stack = stack;
		setFont(DebuggerFrame.INTERFACE_FONT);
	}

	public StackEntry(DefaultMutableTreeNode root) {
		super(root);
		this.root = root;
	}

	public static class StackNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = 853909499235944193L;

		public StackNode(String name, Object obj) {
			super(name);
			if (obj instanceof VariableStack) {
				VariableStack stack = (VariableStack) obj;
				for (int i = 0; i < stack.frameCount(); i++) {
					StackFrame frame = stack.getFrame(i);
					add(new StackNode("Frame " + (stack.frameCount() - i), frame));
				}
			}
			if (obj instanceof StackFrame) {
				StackFrame frame = (StackFrame) obj;
				Iterator i = frame.names().iterator();
				while (i.hasNext()) {
					String varname = (String) i.next();
					add(new StackNode(varname + " = " + frame.getVar(varname).toString(), null));
				}
			}
		}
	}

	public VariableStack getStack() {
		return stack;
	}
}

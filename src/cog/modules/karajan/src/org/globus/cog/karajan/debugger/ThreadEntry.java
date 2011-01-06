// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 9, 2005
 */
package org.globus.cog.karajan.debugger;

import java.awt.Color;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.globus.cog.karajan.stack.StackFrame;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadedElement;

public class ThreadEntry extends JTree {
	private static final long serialVersionUID = 3988060781204515782L;
	
	private TreeNode root;
	private VariableStack stack;

	public static final Color SELECTED_COLOR = new Color(243, 245, 250);
	public static final Color UNSELECTED_COLOR = new Color(255, 255, 255);
	
	private Color bgColor;

	public ThreadEntry(ThreadedElement te, VariableStack stack) {
		this(new TreeNode(te.getThread().toString(), stack));
		this.stack = stack;
		setFont(DebuggerFrame.INTERFACE_FONT);
		setFocusable(true);
		this.setCellRenderer(new Renderer());
	}

	public ThreadEntry(TreeNode root) {
		super(root);
		this.root = root;
	}

	public static class TreeNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = -4834012660636122486L;

		public TreeNode(String name, VariableStack stack) {
			super(name.equals("") ? "Main" : name);
			update(stack);
		}

		public void update(VariableStack stack) {
			if (stack != null) {
				removeAllChildren();
				for (int i = 0; i < stack.frameCount(); i++) {
					StackFrame frame = stack.getFrame(i);
					if (frame.isDefined("#caller")) {
						add(new TreeNode(frame.getVar("#caller").toString(), null));
					}
				}
			}
		}
	}

	public VariableStack getStack() {
		return stack;
	}

	public void setSelected(boolean selected) {
		if (selected) {
			bgColor = SELECTED_COLOR;
			this.setBackground(SELECTED_COLOR);
		}
		else {
			bgColor = UNSELECTED_COLOR;
			this.setBackground(UNSELECTED_COLOR);
		}
	}

	public void setStack(VariableStack stack) {
		this.stack = stack;
		root.update(stack);
	}

	public class Renderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 9222503908081363740L;

		public Color getBackgroundNonSelectionColor() {
			return bgColor;
		}
	}
}

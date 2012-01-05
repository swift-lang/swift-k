
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 22, 2004
 *
 */
package org.globus.cog.gui.grapheditor.ant;

import java.util.EventObject;

import org.globus.cog.gui.grapheditor.targets.swing.SwingNodeRenderer;
import org.globus.cog.gui.grapheditor.util.swing.ComponentAction;

public class TargetNodeRenderer extends SwingNodeRenderer {
	private ComponentAction execute;
	
	public TargetNodeRenderer() {
		execute = new ComponentAction("Execute target");
		execute.addActionListener(this);
		addAction(execute);
	}
	
	public void event(EventObject e) {
		if (e.getSource() == execute) {
			((TargetNode) getNodeComponent()).execute();
		}
		else {
			super.event(e);
		}
	}
}

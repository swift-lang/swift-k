
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;

import java.awt.event.ActionEvent;
import java.util.EventObject;

import javax.swing.JButton;

import org.globus.cog.util.ImageLoader;

public class TargetCanvas extends TaskCanvas{
	private JButton execute;
	
	public TargetCanvas() {
		setClassRendererClass(TargetCanvasRenderer.class);
		addNodeType(new SerialNode());
		addNodeType(new ParallelNode());
		execute = new JButton("Run");
		ImageLoader il = new ImageLoader();
		execute.setIcon(il.loadImage("images/ant-execute.png"));
		execute.addActionListener(this);
	}
	
	public void event(EventObject e) {
		if (e instanceof ActionEvent) {
			ActionEvent ee = (ActionEvent) e;
			if (ee.getSource() == execute) {
				TargetNode tn = (TargetNode) getOwner();
				tn.execute();
				return;
			}
		}
		super.event(e);
	}
}


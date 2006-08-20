
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;

import org.globus.cog.gui.grapheditor.canvas.transformation.NodeFilter;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.HierarchicalLayout;
import org.globus.cog.gui.grapheditor.targets.swing.views.GraphView;

public class SerialFlowView extends GraphView {
	public SerialFlowView(){
		setName("Task flow");
		setType("SerialFlowView");
		setTransformation(new NodeFilter(AntNode.class));
		addTransformation(new SerialFlowTransformation());
		setLayoutEngine(new HierarchicalLayout());
		setAntiAliasing(true);
	}
}


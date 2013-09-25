
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;

import org.globus.cog.gui.grapheditor.canvas.transformation.NodeAndEdgeFilter;
import org.globus.cog.gui.grapheditor.targets.swing.views.GraphView;

/**
 * Only shows FlowEdge edges and Target nodes
 */
public class FlowView extends GraphView{
    public FlowView(){
        setName("Target flow");
        setTransformation(new NodeAndEdgeFilter(TargetNode.class, FlowEdge.class));
		setAntiAliasing(true);
    }
}

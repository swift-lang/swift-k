
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;

import org.globus.cog.gui.grapheditor.canvas.transformation.NodeAndEdgeFilter;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.ExtendedSpringLayout;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.PersistentLayoutEngine2;
import org.globus.cog.gui.grapheditor.targets.swing.views.GraphView;

public class TargetsGraphView extends GraphView{
    public TargetsGraphView(){
        super(new PersistentLayoutEngine2(new ExtendedSpringLayout()));
        setName("Dependency view");
        setTransformation(new NodeAndEdgeFilter(TargetNode.class, TargetDependency.class));
		setAntiAliasing(true);
    }
}

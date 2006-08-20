
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;

import org.globus.cog.gui.grapheditor.canvas.transformation.NodeFilter;
import org.globus.cog.gui.grapheditor.targets.swing.views.ListView;

public class TargetsListView extends ListView{
    public TargetsListView(){
        super();
        setName("Targets list");
        setTransformation(new NodeFilter(TargetNode.class));
    }
}

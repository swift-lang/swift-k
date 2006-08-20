
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;

import org.globus.cog.gui.grapheditor.edges.SimpleArrow;

/**
 * Represents the flow edge.
 */
public class FlowEdge extends SimpleArrow{
    public FlowEdge(){
        setComponentType("edge.flow");
		loadIcon("images/ant-flow.png");
    }
}

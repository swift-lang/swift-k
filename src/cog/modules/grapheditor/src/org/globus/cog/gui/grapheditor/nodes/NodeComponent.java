
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.nodes;

import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.edges.EdgeComponent;

/**
 * This interface defines the specifics of an object that can be used as a node in
 * the editor
 */
public interface NodeComponent extends GraphComponent {

    /**
     * Retrieves the canvas for this node.
     * @return
     */
    public GraphCanvas getCanvas();
    
    public GraphCanvas createCanvas();

    /**
     * Determines if a specific edge can be connected to this node
     * @param edge
     * @return
     */
    public boolean acceptsInEdgeConnection(EdgeComponent edge);

    /**
     * Determines if a specific edge can be connected to this node
     * @param edge
     * @return
     */
    public boolean acceptsOutEdgeConnection(EdgeComponent edge);
	
	public boolean isResizable();

}

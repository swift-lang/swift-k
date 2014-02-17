
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.canvas.views.layouts;

import java.util.Hashtable;

import org.globus.cog.util.graph.GraphInterface;

/**
 * Interface defining a layout engine for a graph.
 */
public interface GraphLayoutEngine {
    /**
     * This method takes a graph and a Hashtable with the nodes that cannot be moved and
     * returns a Hashtable with the coordinates of the nodes after doing the layout.
     * @param graph The graph that needs to be layed out
     * @param fixedNodes A Hashtable containing Node objects as keys, and
     * Points as elements, which specify the coordinates of the non-movable nodes
     * @return a Hashtable with Node objects as keys and Point objects as coordinates
     * for the nodes.
     */
    public Hashtable layoutGraph(GraphInterface graph, Hashtable fixedNodes);
}

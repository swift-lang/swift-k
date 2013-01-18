
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 4, 2004
 *
 */
package org.globus.cog.gui.grapheditor.targets.swing.util.export;


import java.awt.Point;
import java.util.Hashtable;

import org.globus.cog.gui.grapheditor.canvas.views.layouts.GraphLayoutEngine;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.targets.swing.views.GraphView;
import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.Node;
import org.globus.cog.util.graph.NodeIterator;

public class PassiveLayout implements GraphLayoutEngine {

	public Hashtable layoutGraph(GraphInterface graph, Hashtable fixedNodes) {
		Hashtable coords = new Hashtable();
		NodeIterator i = graph.getNodesIterator();
		while (i.hasMoreNodes()) {
			Node n = i.nextNode();
			NodeComponent nc = (NodeComponent) n.getContents();
			Point p = (Point) nc.getPropertyValue(GraphView.LOCATION);
			coords.put(n, new Point(p.x, p.y));
		}
		return coords;
	}

}

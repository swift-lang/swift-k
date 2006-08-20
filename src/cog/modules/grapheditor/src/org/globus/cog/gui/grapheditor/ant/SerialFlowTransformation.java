
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.ant;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.globus.cog.util.graph.Graph;
import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.Node;
/**
 * Entry point for a flow transformation from a sequential construct
 */
public class SerialFlowTransformation extends FlowTransformation {
	public GraphInterface transform(GraphInterface graph) {
		Graph newGraph = new Graph();
		TaskNode start = new TaskNode();
		Node nStart = newGraph.addNode(start);
		start.setComponentType("start");
		List l = new LinkedList();
		l.add(nStart);
		l = serial(newGraph, graph, l);
		TaskNode end = new TaskNode();
		end.setComponentType("end");
		Node nEnd = newGraph.addNode(end);
		Iterator i = l.iterator();
		while (i.hasNext()) {
			Node n = (Node) i.next();
			newGraph.addEdge(n, nEnd, new FlowEdge());
		}
		return newGraph;
	}
}

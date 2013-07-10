
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.Node;
import org.globus.cog.gui.grapheditor.canvas.transformation.GraphTransformation;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;

/**
 * Transform parallel and serial nested elements into a flow graph. This is just
 * a base class that implements recursive transformation of both serial and parallel
 * containers. Derived classes should represent entry points for the recursive transformation
 */
public abstract class FlowTransformation implements GraphTransformation {
	
	public List serial(GraphInterface newg, GraphInterface oldg, List last){
		Iterator i = oldg.getNodesIterator();
		while (i.hasNext()){
			Node n = (Node) i.next();
			NodeComponent nc = (NodeComponent) n.getContents();
			if (nc instanceof ParallelNode) {
				GraphInterface pg = nc.getCanvas().getGraph();
				if (last.size() > 1) {
					Node join = newg.addNode(new JoinNode());
					Iterator j = last.listIterator();
					while (j.hasNext()) {
						Node jn = (Node) j.next();
						newg.addEdge(jn, join, new FlowEdge());
					}
					last = new LinkedList();
					last.add(join);
				}
				last = parallel(newg, pg, last);
			}
			else if (nc instanceof SerialNode) {
				GraphInterface pg = nc.getCanvas().getGraph();
				last = serial(newg, pg, last);
			}
			else {
				Node newNode = newg.addNode(nc);
				if (last.size() != 0) {
					Iterator j = last.listIterator();
					while (j.hasNext()) {
						Node jn = (Node) j.next();
						newg.addEdge(jn, newNode, new FlowEdge());
					}
				}
				last = new LinkedList();
				last.add(newNode);
			}
		}
		return last;
	}
	
	public List parallel(GraphInterface newg, GraphInterface oldg, List last){
		List newLast = new LinkedList();
		Iterator i = oldg.getNodesIterator();
		while (i.hasNext()){
			Node n = (Node) i.next();
			NodeComponent nc = (NodeComponent) n.getContents();
			if (nc instanceof ForNode) {
				Node newNode = newg.addNode(nc);
				if (last.size() != 0) {
					Iterator j = last.listIterator();
					while (j.hasNext()) {
						Node jn = (Node) j.next();
						newg.addEdge(jn, newNode, new FlowEdge());
					}
				}
				List tLast = new LinkedList();
				tLast.add(newNode);
				GraphInterface pg = nc.getCanvas().getGraph();
				tLast = serial(newg, pg, tLast);
				Node dot = newg.addNode(new ForNodeEnd(nc));
				if (tLast.size() != 0) {
					Iterator j = tLast.listIterator();
					while (j.hasNext()) {
						Node jn = (Node) j.next();
						newg.addEdge(jn, dot, new FlowEdge());
					}
				}
				newg.addEdge(dot, newNode, new ForLoopEdge((ForNode) nc));
				newLast.add(dot);
			}
			else if (nc instanceof ParallelNode) {
				GraphInterface pg = nc.getCanvas().getGraph();
				newLast.addAll(parallel(newg, pg, last));
			}
			else if (nc instanceof SerialNode) {
				GraphInterface pg = nc.getCanvas().getGraph();
				newLast.addAll(serial(newg, pg, last));
			}
			else {
				Node newNode = newg.addNode(nc);
				if (last.size() == 1) {
					Node jn = (Node) last.get(0);
					newg.addEdge(jn, newNode, new FlowEdge());
				}
				else if (last.size() > 1) {
					throw new RuntimeException("This should not be happening");
				}
				newLast.add(newNode);
			}
		}
		return newLast;
	}
}


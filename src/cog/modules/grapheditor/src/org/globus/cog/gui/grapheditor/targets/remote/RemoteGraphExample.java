
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Jan 28, 2004
 *
 */
package org.globus.cog.gui.grapheditor.targets.remote;

import org.globus.cog.gui.grapheditor.generic.GenericEdge;
import org.globus.cog.gui.grapheditor.generic.GenericNode;
import org.globus.cog.gui.grapheditor.generic.RootNode;
import org.globus.cog.util.graph.Graph;
import org.globus.cog.util.graph.Node;

public class RemoteGraphExample {

	public static void main(String[] args) {
		RemoteContainer container = new RemoteContainer("localhost:9999");
		RemoteContainerRunner runner = new RemoteContainerRunner(container);
		runner.start();
		if (container.isDone()) {
			System.exit(1);
		}
		RootNode root = new RootNode();
		container.setRootNode(root);
		Graph graph = new Graph();
		GenericNode nc1 = new GenericNode();
		GenericNode nc2 = new GenericNode();
		GenericNode nc3 = new GenericNode();
		Node node1 = graph.addNode(nc1);
		Node node2 = graph.addNode(nc2);
		Node node3 = graph.addNode(nc3);
		
		GenericEdge ec1 = new GenericEdge();
		GenericEdge ec2 = new GenericEdge();
		
		graph.addEdge(node1, node2, ec1);
		graph.addEdge(node1, node3, ec2);
		
		root.getCanvas().setGraph(graph);
		nc1.setPropertyValue("status", new Integer(1));
		container.quit();
		System.exit(0);
	}
	
	public static class RemoteContainerRunner extends Thread {
		
		private RemoteContainer container;
		public RemoteContainerRunner(RemoteContainer container) {
			this.container = container;
		}
		
		public void start() {
			super.start();
			while (!container.isConnected() && !container.isDone()) {
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void run() {
			container.run();
		}
	}
}

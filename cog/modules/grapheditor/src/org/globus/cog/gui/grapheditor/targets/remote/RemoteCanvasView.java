
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

import java.util.HashSet;
import java.util.Iterator;

import org.globus.cog.gui.grapheditor.ComponentRenderer;
import org.globus.cog.gui.grapheditor.canvas.views.AbstractView;
import org.globus.cog.gui.grapheditor.canvas.views.CanvasView;
import org.globus.cog.gui.grapheditor.edges.EdgeComponent;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.Node;

public class RemoteCanvasView extends AbstractView implements CanvasView {
	private HashSet renderers;
	
	public RemoteCanvasView() {
		this("Remote canvas view");
		renderers = new HashSet();
	}
	
	public RemoteCanvasView(String name) {
		super();
		setName(name);
	}	
	
	public void invalidate() {
		Iterator i = renderers.iterator();
		while (i.hasNext()) {
			ComponentRenderer renderer = (ComponentRenderer) i.next(); 
			renderer.dispose();
			i.remove();
		}
		i = getCanvas().getGraph().getNodesIterator();
		while (i.hasNext()) {
			Node n = (Node) i.next();
			NodeComponent nc = (NodeComponent) n.getContents();
			renderers.add(nc.newRenderer("remote"));
		}
		i = getCanvas().getGraph().getEdgesIterator();
		while (i.hasNext()) {
			Edge e = (Edge) i.next();
			EdgeComponent ec = (EdgeComponent) e.getContents();
			renderers.add(ec.newRenderer("remote"));
		}
	}
}

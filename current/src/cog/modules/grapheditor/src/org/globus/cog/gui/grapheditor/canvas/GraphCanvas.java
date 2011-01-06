
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.canvas;


import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.edges.EdgeComponent;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.util.EventConsumer;
import org.globus.cog.util.graph.GraphInterface;

/*
 * Describes a canvas which is a generic workspace on which editing of graph
 * components can take place. The canvas can be seen as a hyper-node.
 */
public interface GraphCanvas extends EventConsumer {
	/**
	 * This method is used to define the valid node types that can appear
	 * within this canvas.
	 * 
	 * @return A list of node components that are valid for this canvas
	 */
	public java.util.List getSupportedNodes();

	/**
	 * This method is used to define the valid edge types that can appear
	 * within this canvas.
	 * 
	 * @return A list of edge components that are valid for this canvas
	 */
	public java.util.List getSupportedEdges();

	/**
	 * sets the graph that is to be displayed/edited by this canvas
	 * 
	 * @param graph
	 */
	public void setGraph(GraphInterface graph);

	/**
	 * @return the graph of this canvas
	 */
	public GraphInterface getGraph();

	/**
	 * Allows the automatic creation of a graph component based on the
	 * requested type. The method should consult the list of supported nodes
	 * and edges and return a new graph component accordingly. The component
	 * should be uninitialized and it should not be added to the graph in order
	 * to allow proper initialization by the requesting object.
	 * 
	 * @param type
	 *            the requested type
	 * @return the new component
	 */
	public GraphComponent createComponent(String type);

	/**
	 * Adds a graph component to the graph
	 * 
	 * @param c
	 */
	public void addComponent(GraphComponent c);

	/**
	 * Removes a component from the graph. It will also remove other components
	 * if neccessary. For example, removing a node, will also cause the removal
	 * of incident edges.
	 * 
	 * @param c
	 */
	public void removeComponent(GraphComponent c);

	/**
	 * A specialized method for createComponent that only creates nodes.
	 * 
	 * @param type
	 * @return
	 */
	public NodeComponent createNode(String type);

	/**
	 * A specialized method for createComponent that only creates edges.
	 * 
	 * @param type
	 * @return
	 */
	public EdgeComponent createEdge(String type);

	/**
	 * @return the node that owns this canvas
	 */
	public NodeComponent getOwner();

	/**
	 * Sets the value of the owner of this canvas
	 * 
	 * @param owner
	 */
	public void setOwner(NodeComponent owner);

	/**
	 * Each canvas can have a status manager that can be used to represent
	 * various pieces of information pertaining to outcomes of various actions
	 * performed on the canvas or its descendants. This method returns the
	 * status manager of this canvas.
	 */
	public StatusManager getStatusManager();

	/**
	 * Creates a new renderer for this canvas using the current target
	 */
	public CanvasRenderer newRenderer();

	/**
	 * Creates a new renderer for this canvas using the specified target
	 */
	public CanvasRenderer newRenderer(String target);

	/**
	 * Adds a listener that can be used to receive various notification events
	 * about the state of the canvas
	 */
	public void addCanvasEventListener(CanvasEventListener listener);

	/**
	 * Removes a previously added listener
	 */
	public void removeCanvasEventListener(CanvasEventListener listener);

	public void fireCanvasEvent(CanvasEvent e);

	public void addStatusEventListener(StatusEventListener listener);

	public void removeStatusEventListener(StatusEventListener listener);

	public void fireStatusEvent(StatusEvent e);

	/**
	 * Notifies the canvas about changes in its sub-components that may require
	 * the re-evaluation of the state of the canvas object, most notably a
	 * re-rendering of the canvas.
	 */
	public void invalidate();
	
	public void setEventsActive(boolean eventsActive);
}

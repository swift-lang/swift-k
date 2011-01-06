// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.grapheditor.canvas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.ClassTargetPair;
import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.NoSuchRendererException;
import org.globus.cog.gui.grapheditor.RendererFactory;
import org.globus.cog.gui.grapheditor.edges.EdgeComponent;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.gui.grapheditor.targets.swing.util.GraphComponentWrapperEvent;
import org.globus.cog.gui.grapheditor.targets.swing.util.GraphComponentWrapperListener;
import org.globus.cog.gui.grapheditor.util.ConservativeArrayList;
import org.globus.cog.gui.grapheditor.util.EventConsumer;
import org.globus.cog.gui.grapheditor.util.EventDispatcher;
import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.EdgeNotFoundException;
import org.globus.cog.util.graph.Graph;
import org.globus.cog.util.graph.GraphChangedEvent;
import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.GraphListener;
import org.globus.cog.util.graph.Node;
import org.globus.cog.util.graph.NodeNotFoundException;

public abstract class AbstractCanvas implements GraphCanvas, ActionListener,
		GraphComponentWrapperListener, EventConsumer, PropertyChangeListener, GraphListener {

	private static Logger logger = Logger.getLogger(AbstractCanvas.class);

	private HashMap instanceRenderers;

	private Map nodeids;

	private GraphInterface graph;

	private List propertyChangeListeners;

	private List canvasEventListeners;

	private List statusEventListeners;

	private java.util.List npool;

	private java.util.List epool;

	private NodeComponent owner;

	private Hashtable types;

	private boolean propagateEvents;

	private StatusManager sbm;

	private boolean eventsActive;

	public AbstractCanvas(NodeComponent owner) {
		propertyChangeListeners = new LinkedList();
		propagateEvents = false;
		nodeids = new HashMap();
		types = new Hashtable();

		setGraph(new Graph());
		setOwner(owner);

		npool = new LinkedList();
		epool = new LinkedList();
		eventsActive = true;
	}

	public synchronized void setInstanceRendererClass(Class cls, String target) {
		if (instanceRenderers == null) {
			instanceRenderers = new HashMap();
		}
		instanceRenderers.put(new ClassTargetPair(getClass(), target), cls);
	}

	public void setInstanceRendererClass(Class cls) {
		setInstanceRendererClass(cls, RendererFactory.getDefaultTarget());
	}

	protected void setClassRendererClass(Class cls) {
		RendererFactory.addClassRenderer(getClass(), cls);
	}

	protected void setClassRendererClass(Class cls, String target) {
		RendererFactory.addClassRenderer(getClass(), target, cls);
	}

	public CanvasRenderer newRenderer() {
		return newRenderer(RendererFactory.getCurrentTarget());
	}

	private synchronized Class getInstanceRendererClass(String target) {
		if (instanceRenderers == null) {
			return null;
		}
		else {
			return (Class) instanceRenderers.get(new ClassTargetPair(getClass(), target));
		}
	}

	private Class getClassRendererClass(String target) {
		Class cls = getClass();
		while (cls != Object.class) {
			try {
				Class rendererClass = RendererFactory.getClassRenderer(cls, target);
				logger.debug("New renderer for " + getClass() + ": " + rendererClass);
				return rendererClass;
			}
			catch (NoSuchRendererException e) {
				cls = cls.getSuperclass();
			}
		}
		return null;
	}

	public CanvasRenderer newRenderer(String target) {
		try {
			Class rendererClass = getInstanceRendererClass(target);
			if (rendererClass == null) {
				rendererClass = getClassRendererClass(target);
			}
			if (rendererClass != null) {
				CanvasRenderer cr = (CanvasRenderer) rendererClass.newInstance();
				cr.setCanvas(this);
				return cr;
			}
			else {
				throw new NoSuchRendererException(getClass() + " - " + target);
			}
		}
		catch (Exception e) {
			logger.error("Cannot instantiate renderer", e);
		}
		return null;
	}

	public AbstractCanvas() {
		this(null);
	}

	/**
	 * Adds a property change listener to the listeners list. The listener will
	 * be notified of any property changes on this canvas. If propagateEvents is
	 * also set (@see setPropagateEvents) , the listener will also be notified
	 * of property changes for nodes contained in this canvas. If the listener
	 * was added previously, it will not be added again. In other words, at most
	 * one instance of a listener can be present in the listener list.
	 * 
	 * @param l
	 *            the listener to be added to the list
	 */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChangeListeners.add(l);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param l
	 *            the listener to be removed
	 */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChangeListeners.remove(l);
	}

	/**
	 * Fires a PropertyChangeEvent on behalf of this canvas
	 * 
	 * @param e
	 *            the event to be fired
	 */
	public void firePropertyChange(PropertyChangeEvent e) {
		Iterator i = propertyChangeListeners.iterator();
		while (i.hasNext()) {
			((PropertyChangeListener) i.next()).propertyChange(e);
		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (propagateEvents) {
			firePropertyChange(e);
			getOwner().firePropertyChange(e);
		}
	}

	public void actionPerformed(ActionEvent e) {
		EventDispatcher.queue(this, e);
	}

	public void event(EventObject e) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.ogce.gui.grapheditor.canvas.GraphCanvas#setGraph(null)
	 */
	public void setGraph(GraphInterface graph) {
		if (this.graph != null) {
			Iterator i = this.graph.getNodesIterator();
			while (i.hasNext()) {
				Node n = (Node) i.next();
				((NodeComponent) n.getContents()).removePropertyChangeListener(this);
			}
		}
		this.graph = graph;
		invalidate();
		if (graph != null) {
			nodeids = new HashMap();
			Iterator i = graph.getNodesIterator();
			while (i.hasNext()) {
				Node n = (Node) i.next();
				NodeComponent nc = (NodeComponent) n.getContents();
				nc.setParent(owner);
				if (nc.hasProperty("nodeid")) {
					nodeids.put(nc.getPropertyValue("nodeid"), n);
				}
				nc.addPropertyChangeListener(this);
			}
		}
		graph.addGraphListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.ogce.gui.grapheditor.canvas.GraphCanvas#getGraph()
	 */
	public GraphInterface getGraph() {
		return graph;
	}

	private GraphComponent createComponent(String type, Iterator i) {
		GraphComponent template = null;
		while (i.hasNext()) {
			GraphComponent n = (GraphComponent) i.next();
			if (n.supportsType(type)) {
				template = n;
				types.put(type, template);
				break;
			}
		}
		if (template != null) {
			GraphComponent nn = template.newInstance();
			nn.setComponentType(type);
			return nn;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.ogce.gui.grapheditor.canvas.GraphCanvas#createComponent(java.lang.String)
	 */
	public GraphComponent createComponent(String type) {
		GraphComponent template = (GraphComponent) types.get(type);
		if (template != null) {
			GraphComponent nn = template.newInstance();
			nn.setComponentType(type);
			return nn;
		}
		GraphComponent c = createComponent(type, getSupportedEdges().listIterator());
		if (c == null) {
			c = createComponent(type, getSupportedNodes().listIterator());
		}
		return c;
	}
	
	private static int nid = 0;

	private void registerNode(NodeComponent c, Node n) {
		if (c == null) {
			return;
		}
		if (!c.hasProperty("nodeid")) {
			synchronized (this) {
				c.setPropertyValue("nodeid", "t" + nid++);
			}
		}
		nodeids.put(c.getPropertyValue("nodeid"), n);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.ogce.gui.grapheditor.canvas.GraphCanvas#addComponent(org.globus.ogce.gui.grapheditor.GraphComponent)
	 */
	public void addComponent(GraphComponent c) {
		c.setParent(getOwner());
		if (c instanceof NodeComponent) {
			Node n = getGraph().addNode(c);
			c.addPropertyChangeListener(this);
			registerNode((NodeComponent) c, n);
			fireCanvasEvent(new CanvasEvent(this, CanvasEvent.COMPONENT_ADDED, c, n));
		}
		else if (c instanceof EdgeComponent) {
			if (!c.hasProperty("from")) {
				logger.error("Invalid edge; 'from' property missing");
			}
			if (!c.hasProperty("to")) {
				logger.error("Invalid edge; 'to' property missing");
			}
			Object fromId = c.getPropertyValue("from");
			Object toId = c.getPropertyValue("to");
			if (fromId == null) {
				logger.error("Invalid edge; 'from' property is not set");
				Iterator i = c.getProperties().iterator();
				while (i.hasNext()) {
					Property prop = (Property) i.next();
					logger.error("  name: " + prop.getName() + "  value: " + prop.getValue());
				}

			}
			if (toId == null) {
				logger.error("Invalid edge; 'to' property is not set");
			}
			addEdge((EdgeComponent) c, fromId, toId);
		}
	}

	protected void addEdge(EdgeComponent ec, Object fromId, Object toId) {
		Node from = null, to = null;

		from = (Node) nodeids.get(fromId);
		to = (Node) nodeids.get(toId);

		if (from == null) {
			throw new NodeNotFoundException("Could not find a node with the following nodeid:"
					+ fromId.toString());
		}
		if (to == null) {
			throw new NodeNotFoundException("Could not find a node with the following nodeid:"
					+ toId.toString());
		}
		Edge e = getGraph().addEdge(from, to, ec);
		fireCanvasEvent(new CanvasEvent(this, CanvasEvent.COMPONENT_ADDED, ec, e));
	}

	public void removeComponent(GraphComponent c) {
		if (c instanceof NodeComponent) {
			// remove edges first, so we can fire events properly
			// otherwise, the Graph will remove the edges
			Node n = getGraph().findNode(c);
			if (n != null) {
				LinkedList edges = new LinkedList();
				synchronized (getGraph()) {
					Iterator i = n.getInEdgesIterator();
					while (i.hasNext()) {
						edges.add(i.next());
					}
					i = n.getOutEdgesIterator();
					while (i.hasNext()) {
						edges.add(i.next());
					}
					i = edges.iterator();
					while (i.hasNext()) {
						Edge e = (Edge) i.next();
						getGraph().removeEdge(e);
						fireCanvasEvent(new CanvasEvent(this, CanvasEvent.COMPONENT_REMOVED,
								(GraphComponent) e.getContents(), e));
					}
					getGraph().removeNode(n);
				}
				fireCanvasEvent(new CanvasEvent(this, CanvasEvent.COMPONENT_REMOVED, c, n));
			}
			else {
				logger.warn("Component not found " + c);
			}
		}
		else {
			try {
				Edge e = getGraph().findEdge(c);
				getGraph().removeEdge(e);
				fireCanvasEvent(new CanvasEvent(this, CanvasEvent.COMPONENT_REMOVED, c, e));
			}
			catch (EdgeNotFoundException e) {

			}
		}
	}

	private static int rid = 0;

	/**
	 * Adds a node to this canvas
	 * 
	 * @param nc
	 *            node to be added
	 */
	public void addNode(NodeComponent nc) {
		getGraph().addNode(nc);
		nc.addPropertyChangeListener(this);
	}

	/**
	 * Adds an edge to this canvas
	 * 
	 * @param ec
	 *            the edge to be added
	 */
	public void addEdge(EdgeComponent ec) {
		addComponent(ec);
	}

	public void addEdge(EdgeComponent ec, NodeComponent from, NodeComponent to) {
		Object fromId = from.getPropertyValue("nodeid");
		Object toId = to.getPropertyValue("nodeid");
		if (fromId == null) {
			throw new NodeNotFoundException("From node has no id");
		}
		if (toId == null) {
			throw new NodeNotFoundException("To node has no id");
		}
		addEdge(ec, fromId, toId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.ogce.gui.grapheditor.canvas.GraphCanvas#createNode(java.lang.String)
	 */
	public NodeComponent createNode(String type) {
		return (NodeComponent) createComponent(type, getSupportedNodes().listIterator());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.ogce.gui.grapheditor.canvas.GraphCanvas#createEdge(java.lang.String)
	 */
	public EdgeComponent createEdge(String type) {
		return (EdgeComponent) createComponent(type, getSupportedEdges().listIterator());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.ogce.gui.grapheditor.canvas.GraphCanvas#getSupportedNodes()
	 */
	public java.util.List getSupportedNodes() {
		return npool;
	}

	/**
	 * @return
	 */
	public java.util.List getSupportedEdges() {
		return epool;
	}

	/**
	 * Adds a prototype node to this canvas
	 * 
	 * @param node
	 */
	public void addNodeType(NodeComponent node) {
		npool.add(0, node);
	}

	public void removeNodeType(Class node) {
		Iterator i = npool.iterator();
		while (i.hasNext()) {
			NodeComponent nc = (NodeComponent) i.next();
			if (nc.getClass() == node) {
				i.remove();
			}
		}
	}

	/**
	 * Adds an edge prototype to this canvas
	 * 
	 * @param edge
	 */
	public void addEdgeType(EdgeComponent edge) {
		epool.add(0, edge);
	}

	public void removeEdgeType(Class edge) {
		Iterator i = epool.iterator();
		while (i.hasNext()) {
			EdgeComponent ec = (EdgeComponent) i.next();
			if (ec.getClass() == edge) {
				i.remove();
			}
		}
	}

	public void graphComponentEvent(GraphComponentWrapperEvent e) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.ogce.gui.grapheditor.canvas.GraphCanvas#getOwner()
	 */
	public NodeComponent getOwner() {
		return owner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.ogce.gui.grapheditor.canvas.GraphCanvas#setOwner(org.globus.ogce.gui.grapheditor.nodes.NodeComponent)
	 */
	public void setOwner(NodeComponent owner) {
		this.owner = owner;
	}

	/**
	 * Determines whether node events are being propagated to the listeners for
	 * this canvas
	 * 
	 * @param v
	 */
	public void setPropagateEvents(boolean v) {
		this.propagateEvents = v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.ogce.gui.grapheditor.canvas.GraphCanvas#getStatusManager()
	 */
	public synchronized StatusManager getStatusManager() {
		if (sbm == null) {
			sbm = new ForwardingStatusManager(this);
		}
		return sbm;
	}

	public synchronized void addCanvasEventListener(CanvasEventListener listener) {
		if (canvasEventListeners == null) {
			canvasEventListeners = new ConservativeArrayList();
		}
		if (!canvasEventListeners.contains(listener)) {
			canvasEventListeners.add(listener);
		}
	}

	public synchronized void removeCanvasEventListener(CanvasEventListener listener) {
		if (canvasEventListeners != null) {
			canvasEventListeners.remove(listener);
		}
	}

	public synchronized void fireCanvasEvent(CanvasEvent e) {
		if (!eventsActive) {
			return;
		}
		if (canvasEventListeners == null) {
			return;
		}
		Iterator i = canvasEventListeners.iterator();
		while (i.hasNext()) {
			Object listener = i.next();
			((CanvasEventListener) listener).canvasEvent(e);
		}
	}

	public synchronized void addStatusEventListener(StatusEventListener listener) {
		if (statusEventListeners == null) {
			statusEventListeners = new ConservativeArrayList();
		}
		if (!statusEventListeners.contains(listener)) {
			statusEventListeners.add(listener);
		}
	}

	public synchronized void removeStatusEventListener(StatusEventListener listener) {
		if (statusEventListeners != null) {
			statusEventListeners.remove(listener);
		}
	}

	public synchronized void fireStatusEvent(StatusEvent e) {
		if (statusEventListeners == null) {
			return;
		}
		Iterator i = statusEventListeners.iterator();
		while (i.hasNext()) {
			Object listener = i.next();
			((StatusEventListener) listener).statusEvent(e);
		}
	}

	public void invalidate() {
		fireCanvasEvent(new CanvasEvent(this, CanvasEvent.INVALIDATE));
	}

	public void setEventsActive(boolean eventsActive) {
		this.eventsActive = eventsActive;
	}

	public void graphChanged(GraphChangedEvent e) {
		if (e.getType() == GraphChangedEvent.NODE_ADDED) {
			NodeComponent nc = (NodeComponent) e.getNode().getContents();
			registerNode(nc, e.getNode());
		}
	}
}

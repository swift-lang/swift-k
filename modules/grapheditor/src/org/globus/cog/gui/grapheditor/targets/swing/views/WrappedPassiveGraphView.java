
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.grapheditor.targets.swing.views;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.canvas.CanvasEvent;
import org.globus.cog.gui.grapheditor.canvas.CanvasEventListener;
import org.globus.cog.gui.grapheditor.canvas.CanvasLayout;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.canvas.views.CanvasView;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.GraphLayoutEngine;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.PersistentLayoutEngine2;
import org.globus.cog.gui.grapheditor.edges.EdgeComponent;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.properties.PropertyHolder;
import org.globus.cog.gui.grapheditor.targets.swing.util.EdgeComponentWrapper;
import org.globus.cog.gui.grapheditor.targets.swing.util.GraphComponentWrapper;
import org.globus.cog.gui.grapheditor.targets.swing.util.GraphComponentWrapperEvent;
import org.globus.cog.gui.grapheditor.targets.swing.util.GraphComponentWrapperListener;
import org.globus.cog.gui.grapheditor.targets.swing.util.NodeComponentWrapper;
import org.globus.cog.gui.grapheditor.util.swing.RepaintMonitoringContainer;
import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.EdgeIterator;
import org.globus.cog.util.graph.Node;

/**
 * Implements a graph view. The initial layout is determined by a
 * layout enging. Can filter nodes and edges based on their class
 * types.
 */
public class WrappedPassiveGraphView extends SwingView implements CanvasView,
	PropertyChangeListener, CanvasEventListener, GraphComponentWrapperListener {

	private static Logger logger = Logger.getLogger(WrappedPassiveGraphView.class);

	//private FastContainer container;

	private int minx, miny, maxh, maxw;

	private RepaintMonitoringContainer rmc;

	private Hashtable wrappers;

	private Hashtable components;

	private GraphLayoutEngine layoutEngine;

	public WrappedPassiveGraphView() {
		this(new PersistentLayoutEngine2());
	}

	public WrappedPassiveGraphView(GraphLayoutEngine engine) {
		this(engine, "Graph View");
	}

	public WrappedPassiveGraphView(GraphLayoutEngine engine, String name) {
		rmc = new RepaintMonitoringContainer();
		rmc.setLayout(new CanvasLayout());
		setName(name);
		setType("GraphView");
		setComponent(rmc);
		setLayoutEngine(engine);
	}

	public void activate() {
		wrappers = new Hashtable();
		components = new Hashtable();
		super.activate();
		getCanvas().addCanvasEventListener(this);
	}

	public void clean() {
		getCanvas().removeCanvasEventListener(this);
		Iterator i = wrappers.values().iterator();
		while (i.hasNext()) {
			((GraphComponentWrapper) i.next()).dispose();
		}
		if (getGraph() == null) {
			return;
		}
		i = getGraph().getNodesIterator();
		while (i.hasNext()) {
			GraphComponent gc = (GraphComponent) ((Node) i.next()).getContents();
			gc.removePropertyChangeListener(this);
		}
		i = getGraph().getEdgesIterator();
		while (i.hasNext()) {
			GraphComponent gc = (GraphComponent) ((Edge) i.next()).getContents();
			gc.removePropertyChangeListener(this);
		}
		rmc.removeAll();
		wrappers.clear();
		components.clear();
	}

	public GraphLayoutEngine getLayoutEngine() {
		return layoutEngine;
	}

	public CanvasView getNewInstance(GraphCanvas canvas) {
		WrappedPassiveGraphView cv = (WrappedPassiveGraphView) super.getNewInstance(canvas);
		cv.setLayoutEngine(getLayoutEngine());
		cv.setName(getName());
		return cv;
	}

	public void invalidate() {
		invalidate(false);
	}

	public void invalidate(boolean ignoreOld) {
		super.invalidate();
		rmc.removeAll();
		if (getCanvas() == null) {
			//rmc.reallyValidate();
			return;
		}
		Hashtable newWrappers = new Hashtable();
		Hashtable fixedNodes = new Hashtable();

		Iterator n = getGraph().getNodesIterator();
		while (n.hasNext()) {
			Node node = (Node) n.next();
			NodeComponent nc = (NodeComponent) node.getContents();
			if (nc.hasProperty(GraphView.LOCATION)) {
				Point p = (Point) nc.getPropertyValue(GraphView.LOCATION);
				fixedNodes.put(node, p);
			}
		}
		Hashtable coords = null;
		if (fixedNodes.size() != getGraph().nodeCount()) {
			if (getLayoutEngine() instanceof PersistentLayoutEngine2) {
				coords = ((PersistentLayoutEngine2) getLayoutEngine()).layoutGraph(
					getGraph(), fixedNodes, ignoreOld);
			}
			else {
				coords = getLayoutEngine().layoutGraph(getGraph(), fixedNodes);
			}
		}
		else {
			coords = fixedNodes;
		}

		fixedNodes = null;

		Iterator j = getGraph().getNodesIterator();
		while (j.hasNext()) {
			Node node = (Node) j.next();
			NodeComponent nc = (NodeComponent) node.getContents();
			createWrapper(node, nc, wrappers, newWrappers, (Point) coords.get(node));
		}

		j = getGraph().getEdgesIterator();
		while (j.hasNext()) {
			Edge edge = (Edge) j.next();
			EdgeComponent el = (EdgeComponent) edge.getContents();
			createWrapper(edge, el, wrappers, newWrappers);
		}
		Iterator i = wrappers.keySet().iterator();
		while (i.hasNext()) {
			Object nodeOrEdge = i.next();
			GraphComponentWrapper oldWrapper = (GraphComponentWrapper) wrappers.get(nodeOrEdge);
			GraphComponentWrapper newWrapper = (GraphComponentWrapper) newWrappers.get(nodeOrEdge);

			if (oldWrapper != newWrapper) {
				rmc.remove(oldWrapper);
			}
			if (!newWrappers.containsKey(nodeOrEdge)) {
				//maybe there should be an interface there
				Object contents = null;
				if (nodeOrEdge instanceof Node) {
					contents = ((Node) nodeOrEdge).getContents();
					((PropertyHolder) contents).removePropertyChangeListener(this);
				}
				components.remove(contents);
			}
		}
		wrappers = newWrappers;
		resizeContainer();

		//container.reallyValidate();
		rmc.repaint();
	}

	public boolean isEditable() {
		return false;
	}

	public void reLayout() {
		invalidate();
	}

	private void resizeContainer() {
		Iterator i = wrappers.values().iterator();
		maxw = 0;
		maxh = 0;
		minx = Integer.MAX_VALUE;
		miny = Integer.MAX_VALUE;
		while (i.hasNext()) {
			GraphComponentWrapper wrapper = (GraphComponentWrapper) i.next();
			Component comp = wrapper;
			int cw = comp.getX() + comp.getWidth();
			if (cw > maxw) {
				maxw = cw;
			}
			if (comp.getX() < minx) {
				minx = comp.getLocation().x;
			}
			int ch = comp.getY() + comp.getHeight();
			if (ch > maxh) {
				maxh = ch;
			}
			if (comp.getY() < miny) {
				miny = comp.getY();
			}
		}
		if ((minx < Integer.MAX_VALUE) || (miny < Integer.MAX_VALUE)) {
			Iterator j = wrappers.values().iterator();
			while (j.hasNext()) {
				GraphComponentWrapper wrapper = (GraphComponentWrapper) j.next();
				Component comp = wrapper;
				Point p = comp.getLocation();
				p.x -= minx;
				p.y -= miny;
				comp.setLocation(p);
			}
			maxw -= minx;

			maxh -= miny;
		}
		rmc.setSize(maxw, maxh);
		minx = 0;
		miny = 0;
	}

	public void setLayoutEngine(GraphLayoutEngine layoutEngine) {
		this.layoutEngine = layoutEngine;
	}

	public void setCenter(Component component, Point p) {
		component.setLocation(p.x - component.getWidth() / 2, p.y - component.getHeight()
			/ 2);
	}

	public Point getCenter(Component component) {
		return new Point(component.getX() + component.getWidth() / 2, component.getY()
			+ component.getHeight() / 2);
	}

	public void setEdgeCoords(EdgeComponentWrapper edge, Component from, Component to) {
		Point fc = getCenter(from);
		Point tc = getCenter(to);
		Point pf = GraphView.getRectIntersection(from, tc);
		Point pt = GraphView.getRectIntersection(to, fc);
		edge.setCoords(pf.x, pf.y, pt.x, pt.y);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof NodeComponent) {
			Node node = (Node) components.get(evt.getSource());
			if (node == null) {
				return;
			}
			GraphComponentWrapper wrapper = (GraphComponentWrapper) wrappers.get(node);
			if (wrapper == null) {
				return;
			}
			Component comp = wrapper;
			if (evt.getPropertyName().equals(GraphView.LOCATION)) {
				setCenter(comp, (Point) evt.getNewValue());
			}
			if (evt.getPropertyName().equals(GraphView.SIZE)) {
				Point p = getCenter(comp);
				((NodeComponentWrapper) comp).setNodeSize((Dimension) evt.getNewValue());
				setCenter(comp, p);
			}
			EdgeIterator i = node.getOutEdgesIterator();
			while (i.hasMoreEdges()) {
				Edge te = i.nextEdge();
				Node to = te.getToNode();
				NodeComponentWrapper wto = (NodeComponentWrapper) wrappers.get(to);
				EdgeComponentWrapper wte = (EdgeComponentWrapper) wrappers.get(te);
				setEdgeCoords(wte, wrapper, wto);
			}
			i = node.getInEdgesIterator();
			while (i.hasMoreEdges()) {
				Edge fe = i.nextEdge();
				Node from = fe.getFromNode();
				NodeComponentWrapper wfrom = (NodeComponentWrapper) wrappers.get(from);
				EdgeComponentWrapper wfe = (EdgeComponentWrapper) wrappers.get(fe);
				setEdgeCoords(wfe, wfrom, wrapper);
			}
		}
	}

	public void canvasEvent(CanvasEvent e) {
		if (e.getType() == CanvasEvent.INVALIDATE) {
			invalidate();
		}
		else if (e.getType() == CanvasEvent.COMPONENT_ADDED) {
			logger.debug("Received COMPONENT_ADDED");
			if (e.getComponent() instanceof NodeComponent) {
				createWrapper(e.getNode(), (NodeComponent) e.getComponent(), wrappers,
					wrappers, new Point(10, 10));
			}
			else {
				createWrapper(e.getEdge(), (EdgeComponent) e.getComponent(), wrappers,
					wrappers);
			}
			rmc.repaint();
		}
		else if (e.getType() == CanvasEvent.COMPONENT_REMOVED) {
			e.getComponent().removePropertyChangeListener(this);
			if (e.getNode() != null) {
				GraphComponentWrapper gc = (GraphComponentWrapper) wrappers.get(e.getNode());
				rmc.remove(gc);
			}
			if (e.getEdge() != null) {
				GraphComponentWrapper gc = (GraphComponentWrapper) wrappers.get(e.getEdge());
				rmc.remove(gc);
			}
			rmc.repaint();
		}
	}

	public void createWrapper(Node node, NodeComponent nc, Hashtable oldWrappers,
		Hashtable newWrappers, Point l) {
		NodeComponentWrapper w;
		if (!oldWrappers.containsKey(node)) {
			w = new NodeComponentWrapper(nc);
			w.addGraphComponentListener(this);
			components.put(nc, node);
			nc.addPropertyChangeListener(this);
		}
		else {
			w = (NodeComponentWrapper) oldWrappers.get(node);
		}
		newWrappers.put(node, w);
		if (nc.hasProperty(GraphView.SIZE)) {
			Dimension d = (Dimension) nc.getPropertyValue(GraphView.SIZE);
			w.setNodeSize(d);
		}
		else {
			w.setNodeSize(w.getPreferredNodeSize());
		}
		Point p = (Point) nc.getPropertyValue(GraphView.LOCATION);
		if (p == null) {
			p = l;
		}
		setCenter(w, p);
		rmc.add(w);
	}

	public void createWrapper(Edge edge, EdgeComponent ec, Hashtable oldWrappers,
		Hashtable newWrappers) {
		EdgeComponentWrapper w;
		if (!oldWrappers.containsKey(edge)) {
			w = new EdgeComponentWrapper(ec);
			components.put(ec, edge);
		}
		else {
			w = (EdgeComponentWrapper) oldWrappers.get(edge);
		}
		newWrappers.put(edge, w);
		NodeComponentWrapper from = (NodeComponentWrapper) newWrappers.get(edge.getFromNode());
		NodeComponentWrapper to = (NodeComponentWrapper) newWrappers.get(edge.getToNode());
		setEdgeCoords(w, from, to);
		rmc.add(w);
	}

	public void graphComponentEvent(GraphComponentWrapperEvent e) {
		if ((e.getType() == GraphComponentWrapperEvent.MOVED)
			|| (e.getType() == GraphComponentWrapperEvent.RESIZED)) {
			if (e.getSource() instanceof NodeComponentWrapper) {
				NodeComponentWrapper w = (NodeComponentWrapper) e.getSource();
				NodeComponent nc = (NodeComponent) w.getGraphComponent();
				Point loc = getCenter(w);
				Dimension size = w.getNodeSize();
				minx = Math.min(minx, w.getX());
				miny = Math.min(miny, w.getY());
				int nmaxw = Math.max(maxw, w.getX() + w.getWidth());
				int nmaxh = Math.max(maxh, w.getY() + w.getHeight());
				Node node;
				node = (Node) components.get(nc);
				if (node == null) {
					logger.error("Components map is invalid");
					return;
				}
				Iterator i = node.getInEdgesIterator();
				while (i.hasNext()) {
					Edge edge = (Edge) i.next();
					EdgeComponentWrapper ew = (EdgeComponentWrapper) wrappers.get(edge);
					if (ew == null) {
						continue;
					}
					NodeComponentWrapper wf = (NodeComponentWrapper) wrappers.get(edge.getFromNode());
					setEdgeCoords(ew, wf, w);
				}

				i = node.getOutEdgesIterator();
				while (i.hasNext()) {
					Edge edge = (Edge) i.next();
					EdgeComponentWrapper ew = (EdgeComponentWrapper) wrappers.get(edge);
					if (ew == null) {
						continue;
					}
					NodeComponentWrapper wt = (NodeComponentWrapper) wrappers.get(edge.getToNode());
					setEdgeCoords(ew, w, wt);
				}
				if ((minx < 0) || (miny < 0)) {
					Component[] comps = rmc.getComponents();
					for (int j = 0; j < comps.length; j++) {
						int nx = comps[j].getX() - minx;
						int ny = comps[j].getY() - miny;
						comps[j].setLocation(nx, ny);
					}
					nmaxw -= minx;
					nmaxh -= miny;
					minx = 0;
					miny = 0;
				}
				if ((nmaxw > maxw) || (nmaxh > maxh)) {
					rmc.setSize(nmaxw, nmaxh);
					maxw = nmaxw;
					maxh = nmaxh;
				}
			}
		}
	}

}

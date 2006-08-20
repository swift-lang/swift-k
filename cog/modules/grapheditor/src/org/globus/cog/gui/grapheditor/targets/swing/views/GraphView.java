// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.grapheditor.targets.swing.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.canvas.CanvasEvent;
import org.globus.cog.gui.grapheditor.canvas.CanvasEventListener;
import org.globus.cog.gui.grapheditor.canvas.CanvasLayout;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.canvas.StatusManager;
import org.globus.cog.gui.grapheditor.canvas.views.CanvasView;
import org.globus.cog.gui.grapheditor.canvas.views.Editor;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.FlowLayout;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.GraphLayoutEngine;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.GraphLayoutEngine2;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.HierarchicalLayout;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.PersistentLayoutEngine2;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.RadialFlowLayout;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.RadialHierarchicalLayout;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.RadialLayout;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.SpringLayout;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.StatusReporter;
import org.globus.cog.gui.grapheditor.edges.EdgeComponent;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.properties.OverlayedProperty;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.gui.grapheditor.properties.PropertyHolder;
import org.globus.cog.gui.grapheditor.targets.swing.SwingCanvasRenderer;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasAction;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionEvent;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionListener;
import org.globus.cog.gui.grapheditor.targets.swing.util.EdgeComponentWrapper;
import org.globus.cog.gui.grapheditor.targets.swing.util.GraphComponentWrapper;
import org.globus.cog.gui.grapheditor.targets.swing.util.GraphComponentWrapperEvent;
import org.globus.cog.gui.grapheditor.targets.swing.util.GraphComponentWrapperListener;
import org.globus.cog.gui.grapheditor.targets.swing.util.NodeComponentWrapper;
import org.globus.cog.gui.grapheditor.targets.swing.util.OverviewFrame;
import org.globus.cog.gui.grapheditor.util.swing.FastContainer;
import org.globus.cog.gui.grapheditor.util.swing.RepaintMonitoringContainer;
import org.globus.cog.util.ImageLoader;
import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.EdgeIterator;
import org.globus.cog.util.graph.GraphChangedEvent;
import org.globus.cog.util.graph.Node;
import org.globus.cog.util.graph.NodeIterator;

/**
 * Implements a graph view. The initial layout is determined by a layout engine.
 * Can filter nodes and edges based on their class types.
 */
//TODO Something has to be done about the size of this thing
public class GraphView extends SwingView implements ActionListener, CanvasView,
		GraphComponentWrapperListener, MouseListener, MouseMotionListener, Editor,
		CanvasActionListener, WindowListener, CanvasEventListener {

	private static final int EDGE_EDITING_STEP_FROM_NODE = 1;

	private static final int EDGE_EDITING_STEP_NONE = 0;

	private static final int EDGE_EDITING_STEP_TO_NODE = 2;

	private static final int MODE_SELECT = 0;

	private static final int MODE_NODE = 1;

	private static final int MODE_EDGE = 2;

	public static final String LOCATION = "graphview.location";

	public static final String SIZE = "graphview.size";

	public static final String CONTROL_POINTS = "graphview.controlPoints";

	public static final Dimension DEFAULT_SIZE = new Dimension(40, 20);

	private static Logger logger = Logger.getLogger(GraphView.class);

	private boolean antiAliasing;

	private FastContainer container;

	private int edgeEditingStep;

	private boolean editable;

	private Hashtable graphElements;

	private NodeComponentWrapper hl;

	private GraphLayoutEngine layoutEngine;

	private int minx, miny, maxh, maxw;

	private CanvasAction overview, antiAliasingMI, reLayout, persistence, selectionArrow, dilate,
			contract;

	private OverviewFrame overviewFrame;

	private RepaintMonitoringContainer rmc;

	private boolean selective;

	private EdgeComponent tempEdge;

	private EdgeComponentWrapper tempEdgeWrapper;

	private NodeComponentWrapper tempFromNodeWrapper;

	private NodeComponentWrapper tempToNodeWrapper;

	private Rectangle viewport;

	private Hashtable wrappers;

	private Hashtable components;

	private LinkedHashMap layouts;

	private GraphComponent crtComp;

	private int mode;

	private Set selected;

	private boolean invalidating;
	private int dx, dy;

	public GraphView() {
		this(new PersistentLayoutEngine2());
	}

	public GraphView(GraphLayoutEngine engine) {
		this(engine, "Graph View");
	}

	public GraphView(GraphLayoutEngine engine, String name) {
		dx = 0;
		dy = 0;
		mode = MODE_SELECT;
		antiAliasing = false;
		container = new FastContainer();
		container.setLayout(new CanvasLayout());
		container.addMouseListener(this);
		rmc = new RepaintMonitoringContainer();
		rmc.setLayout(new BorderLayout());
		rmc.add(container);
		setName(name);
		setType("GraphView");
		setComponent(rmc);
		setLayoutEngine(engine);
		selective = false;
		editable = true;
		edgeEditingStep = EDGE_EDITING_STEP_NONE;
		overview = new CanvasAction("30#View>53#Overview", CanvasAction.ACTION_SWITCH);
		antiAliasingMI = new CanvasAction("View>51#Anti-Aliasing", CanvasAction.ACTION_SWITCH
				+ CanvasAction.SEPARATOR_AFTER + CanvasAction.SEPARATOR_BEFORE);
		reLayout = new CanvasAction("30#View>20#Layout>10#Re-Layout", CanvasAction.ACTION_NORMAL);
		persistence = new CanvasAction("30#View>20#Layout>11#Persistence",
				CanvasAction.ACTION_SWITCH + CanvasAction.SEPARATOR_AFTER);
		dilate = new CanvasAction("30#View>31#Dilate", CanvasAction.ACTION_NORMAL
				+ CanvasAction.SEPARATOR_BEFORE);
		contract = new CanvasAction("30#View>32#Contract", CanvasAction.ACTION_NORMAL);
		selectionArrow = new CanvasAction("1#comps>1#",
				ImageLoader.loadIcon("images/cursor-arrow.png"), CanvasAction.ACTION_SELECTOR);
		overview.addCanvasActionListener(this);
		antiAliasingMI.addCanvasActionListener(this);
		reLayout.addCanvasActionListener(this);
		persistence.addCanvasActionListener(this);
		selectionArrow.addCanvasActionListener(this);
		selectionArrow.setSelected(true);
		dilate.addCanvasActionListener(this);
		contract.addCanvasActionListener(this);
		layouts = new LinkedHashMap();
		GraphLayoutEngine layout;
		layouts.put(new CanvasAction("View>Layout>20#Spring Layout", CanvasAction.ACTION_SELECTOR),
				new SpringLayout());
		layouts.put(new CanvasAction("View>Layout>21#Radial Layout", CanvasAction.ACTION_SELECTOR),
				new RadialLayout());
		layouts.put(new CanvasAction("View>Layout>23#Radial Hierarchical Layout",
				CanvasAction.ACTION_SELECTOR), new RadialHierarchicalLayout());
		layouts.put(new CanvasAction("View>Layout>22#Hierarchical Layout",
				CanvasAction.ACTION_SELECTOR), new HierarchicalLayout());
		layouts.put(new CanvasAction("View>Layout>25#Flow Layout", CanvasAction.ACTION_SELECTOR),
				layout = new FlowLayout());
		layouts.put(new CanvasAction("View>Layout>26#Radial Flow Layout",
				CanvasAction.ACTION_SELECTOR), new RadialFlowLayout());
		setLayoutEngine(new PersistentLayoutEngine2(layout));
		Iterator i = layouts.keySet().iterator();
		while (i.hasNext()) {
			((CanvasAction) i.next()).addCanvasActionListener(this);
		}
		selected = new HashSet();
	}

	public synchronized void activate() {
		if (isActive()) {
			logger.warn("Activate called on an already active view");
			return;
		}
		super.activate();
		components = new Hashtable();
		getCanvas().addCanvasEventListener(this);
		SwingCanvasRenderer renderer = getSwingRenderer();
		if (getCanvas() != null) {
			int index = 10;
			renderer.addToolBarItem(selectionArrow);
			renderer.addToolBarItem(new CanvasAction("1#comps>9#", CanvasAction.SEPARATOR));
			Iterator i = getCanvas().getSupportedNodes().iterator();
			while (i.hasNext()) {
				NodeComponent nc = (NodeComponent) i.next();
				logger.debug("Adding " + nc.getComponentType());
				CanvasAction action = new CanvasAction("1#comps>" + index + "#",
						(Icon) nc.getPropertyValue("icon"), CanvasAction.ACTION_SELECTOR);
				action.addCanvasActionListener(this);
				components.put(action, nc);
				renderer.addToolBarItem(action);
				index++;
			}
			renderer.addToolBarItem(new CanvasAction("1#comps>" + index + "#",
					CanvasAction.SEPARATOR));
			index++;
			i = getCanvas().getSupportedEdges().iterator();
			while (i.hasNext()) {
				EdgeComponent ec = (EdgeComponent) i.next();
				logger.debug("Adding " + ec.getComponentType());
				CanvasAction action = new CanvasAction("1#comps>" + index + "#",
						(Icon) ec.getPropertyValue("icon"), CanvasAction.ACTION_SELECTOR);
				action.addCanvasActionListener(this);
				components.put(action, ec);
				renderer.addToolBarItem(action);
				index++;
			}
		}
		renderer.addMenuItem(overview);
		renderer.addMenuItem(antiAliasingMI);
		renderer.addMenuItem(reLayout);
		renderer.addMenuItem(persistence);
		renderer.addMenuItem(dilate);
		renderer.addMenuItem(contract);
		GraphLayoutEngine layout;
		if (getLayoutEngine() instanceof PersistentLayoutEngine2) {
			layout = ((PersistentLayoutEngine2) getLayoutEngine()).getLayoutEngine();
			persistence.setSelectedQuiet(true);
		}
		else {
			layout = getLayoutEngine();
		}
		Iterator i = layouts.keySet().iterator();
		while (i.hasNext()) {
			CanvasAction action = (CanvasAction) i.next();
			if (layouts.get(action) == layout) {
				action.setSelectedQuiet(true);
			}
			renderer.addMenuItem(action);
		}
		renderer.updatePanel();
	}

	public void enable() {
		getCanvas().addCanvasEventListener(this);
		if (getCanvas() != null) {
			wrappers = new Hashtable(getCanvas().getGraph().nodeCount()
					+ getCanvas().getGraph().edgeCount());
			graphElements = new Hashtable();
			if (getCanvas().getOwner().hasProperty("overview.active")) {
				if (getCanvas().getOwner().getPropertyValue("overview.active").equals(Boolean.TRUE)) {
					openOverview();
				}
			}
			if (getCanvas().getOwner().hasProperty("graphview.antialiasing")) {
				setAntiAliasing(getCanvas().getOwner().getPropertyValue("graphview.antialiasing").equals(
						Boolean.TRUE));
				antiAliasingMI.setSelected(getAntiAliasing());
			}
		}
		else {
			wrappers = new Hashtable();
		}
		super.enable();
	}

	public synchronized void cancelEdgeEditing() {
		if (tempEdgeWrapper != null) {
			container.remove(tempEdgeWrapper);
			tempEdgeWrapper = null;
			tempEdge = null;
			container.reallyValidate();
			container.repaint();
		}
		setEdgeEditingStep(EDGE_EDITING_STEP_NONE);
	}

	public void cancelEdgeRequest() {
		setEdgeEditingStep(EDGE_EDITING_STEP_NONE);
	}

	/**
	 * Synchronized so that it waits until any pending invalidations are
	 * completed.
	 */
	public synchronized void clean() {
		if (!isActive()) {
			logger.debug("Clean called on a non-active view");
			return;
		}
		logger.debug("Cleaning view");
		if (getSwingRenderer() != null) {
			getSwingRenderer().removeMenuItem(overview);
			getSwingRenderer().removeMenuItem(antiAliasingMI);
			getSwingRenderer().removeMenuItem(reLayout);
			getSwingRenderer().removeMenuItem(persistence);
			Iterator i = layouts.keySet().iterator();
			while (i.hasNext()) {
				getSwingRenderer().removeMenuItem((CanvasAction) i.next());
			}
			getSwingRenderer().removeToolBarItem(selectionArrow);
			i = components.keySet().iterator();
			while (i.hasNext()) {
				CanvasAction action = (CanvasAction) i.next();
				action.removeCanvasActionListener(this);
				getSwingRenderer().removeToolBarItem(action);
			}
			components = null;
		}
		graphElements = null;
		super.clean();
	}

	public void disable() {
		if (getLayoutEngine() instanceof PersistentLayoutEngine2) {
			((PersistentLayoutEngine2) getLayoutEngine()).setGraph(null);
		}
		container.removeAll();
		container.repaint();
		getCanvas().removeCanvasEventListener(this);
		closeOverview(Boolean.TRUE);
		if (wrappers != null) {
			Iterator i = wrappers.values().iterator();
			while (i.hasNext()) {
				GraphComponentWrapper w = (GraphComponentWrapper) i.next();
				w.removeGraphComponentListener(this);
				w.dispose();
			}
			wrappers = null;
		}
		super.disable();
	}

	private void closeOverview(Boolean clean) {
		if (overviewFrame != null) {
			PropertyHolder owner = getCanvas().getOwner();
			owner.setPropertyValue("overview.location", overviewFrame.getLocation());
			owner.setPropertyValue("overview.size", overviewFrame.getSize());
			owner.setPropertyValue("overview.active", clean);
			overviewFrame.setVisible(false);
			overviewFrame.dispose();
			overviewFrame = null;
			overview.setSelected(false);
		}
	}

	public boolean getAntiAliasing() {
		return antiAliasing;
	}

	public Point getCenter(Component component) {
		return new Point(component.getX() + component.getWidth() / 2 - dx, component.getY()
				+ component.getHeight() / 2 - dy);
	}

	public GraphLayoutEngine getLayoutEngine() {
		return layoutEngine;
	}

	public CanvasView getNewInstance(GraphCanvas canvas) {
		GraphView cv = (GraphView) super.getNewInstance(canvas);
		cv.setLayoutEngine(getLayoutEngine());
		cv.setAntiAliasing(getAntiAliasing());
		cv.setName(getName());
		return cv;
	}

	public static Point getRectIntersection(NodeComponent comp, Point p) {
		Point l = (Point) comp.getPropertyValue(LOCATION);
		Dimension d = (Dimension) comp.getPropertyValue(SIZE);
		if (d == null) {
			// TODO
			return getRectIntersection2(l.x, l.y, DEFAULT_SIZE.width, DEFAULT_SIZE.height, p);
		}
		else {
			return getRectIntersection2(l.x, l.y, d.width, d.height, p);
		}
	}

	public static Point getRectIntersection(Component comp, Point p) {
		return getRectIntersection(comp.getX(), comp.getY(), comp.getWidth(), comp.getHeight(), p);
	}

	public static Point getRectIntersection(int x, int y, int width, int height, Point p) {
		return getRectIntersection(x + width / 2, y + height / 2, x, y, width, height, p);
	}

	public static Point getRectIntersection2(int xCenter, int yCenter, int width, int height,
			Point p) {
		return getRectIntersection(xCenter, yCenter, xCenter - width / 2, yCenter - height / 2,
				width, height, p);
	}

	/*
	 * This method was taken from JGraph
	 */
	public static Point getRectIntersection(int xCenter, int yCenter, int x, int y, int width,
			int height, Point p) {
		int dx = p.x - xCenter;
		int dy = p.y - yCenter;
		double alpha = Math.atan2(dy, dx);
		int xout = 0, yout = 0;
		double pi = Math.PI;
		double pi2 = Math.PI / 2.0;
		double beta = pi2 - alpha;
		double t = Math.atan2(height, width);
		if (alpha < -pi + t || alpha > pi - t) { // Left edge
			xout = x;
			yout = yCenter - (int) (width * Math.tan(alpha) / 2);
		}
		else if (alpha < -t) { // Top Edge
			yout = y;
			xout = xCenter - (int) (height * Math.tan(beta) / 2);
		}
		else if (alpha < t) { // Right Edge
			xout = x + width;
			yout = yCenter + (int) (width * Math.tan(alpha) / 2);
		}
		else { // Bottom Edge
			yout = y + height;
			xout = xCenter + (int) (height * Math.tan(beta) / 2);
		}
		return new Point(xout, yout);
	}

	public void graphChanged(GraphChangedEvent e) {
	}

	public void graphComponentEvent(GraphComponentWrapperEvent e) {
		if (e.getType() == GraphComponentWrapperEvent.REQUEST_SELECTION) {
			if (mode == MODE_EDGE) {
				return;
			}
			unselectAll();
			e.getWrapper().setSelected(true);
			selected.add(e.getSource());
		}
		if (e.getType() == GraphComponentWrapperEvent.REQUEST_UNSELECTION) {
			selected.remove(e.getSource());
			e.getWrapper().setSelected(false);
		}
		if (invalidating) {
			return;
		}
		if (e.getType() == GraphComponentWrapperEvent.TIP_MOVED) {
			EdgeComponentWrapper ecw = (EdgeComponentWrapper) e.getSource();
			Point p = ecw.getTipCoords();
			Component c = container.getComponentAt(p);
			if (c instanceof NodeComponentWrapper) {
				if (c != hl) {
					if (hl != null) {
						hl.setHighlighted(false);
					}
					hl = (NodeComponentWrapper) c;
					hl.setHighlighted(true);
				}
			}
			else if (c == container) {
				if (hl != null) {
					hl.setHighlighted(false);
				}
			}
		}
		else if ((e.getType() == GraphComponentWrapperEvent.MOVED)
				|| (e.getType() == GraphComponentWrapperEvent.RESIZED)) {
			NodeComponentWrapper w = (NodeComponentWrapper) e.getSource();
			NodeComponent nc = (NodeComponent) w.getGraphComponent();
			Point loc = getCenter(w);
			Dimension size = w.getNodeSize();
			nc.setPropertyValue(LOCATION, loc);
			nc.setPropertyValue(SIZE, size);
			Node node;
			if (!graphElements.containsKey(w)) {
				node = getGraph().findNode(nc);
				graphElements.put(w, node);
			}
			else {
				node = (Node) graphElements.get(w);
			}
			Iterator i = node.getInEdgesIterator();
			while (i.hasNext()) {
				Edge edge = (Edge) i.next();
				EdgeComponentWrapper ew = (EdgeComponentWrapper) wrappers.get(edge);
				if (ew == null) {
					continue;
				}
				NodeComponent from = (NodeComponent) edge.getFromNode().getContents();
				setEdgeCoords(ew, from, nc);
			}

			i = node.getOutEdgesIterator();
			while (i.hasNext()) {
				Edge edge = (Edge) i.next();
				EdgeComponentWrapper ew = (EdgeComponentWrapper) wrappers.get(edge);
				if (ew == null) {
					continue;
				}
				NodeComponent to = (NodeComponent) edge.getToNode().getContents();
				setEdgeCoords(ew, nc, to);
			}

			boolean invalid = false;
			if (loc.x < minx) {
				minx = loc.x;
				invalid = true;
			}
			if (loc.y < miny) {
				miny = loc.y;
				invalid = true;
			}
			if (loc.x + size.width > maxw) {
				maxw = loc.x + size.width;
				invalid = true;
			}
			if (loc.y + size.height > maxh) {
				maxh = loc.y + size.height;
				invalid = true;
			}
			if (invalid) {
				maxw -= minx;
				maxh -= miny;
				int ddx = -minx - dx;
				int ddy = -miny - dy;
				dx = -minx;
				dy = -miny;
				Component[] components = container.getComponents();
				for (int j = 0; j < components.length; j++) {
					Point p = components[j].getLocation();
					p.x += ddx;
					p.y += ddy;
					components[j].setLocation(p);
				}
				container.setSize(Math.max(maxw, container.getWidth()), Math.max(maxh,
						container.getHeight()));
				// minx = 0;
				// miny = 0;
			}
		}
	}

	public void invalidate() {
		invalidate(false);
	}

	private Set getIgnoredEdges() {
		Set edges = new HashSet();
		EdgeIterator e = getGraph().getEdgesIterator();
		while (e.hasMoreEdges()) {
			Edge edge = e.nextEdge();
			EdgeComponent ec = (EdgeComponent) edge.getContents();
			if (ec.hasProperty("layout.ignore")) {
				edges.add(edge);
			}
		}
		return edges;
	}

	public synchronized void invalidate(boolean ignoreOld) {
		invalidating = true;
		super.invalidate();
		container.removeAll();
		if (getCanvas() == null) {
			container.reallyValidate();
			invalidating = false;
			return;
		}
		synchronized (getCanvas()) {
			getCanvas().getStatusManager().push("Laying out graph...", StatusManager.BUSY_ICON);
			Hashtable fixedNodes = new Hashtable();
			Hashtable newWrappers = new Hashtable();
			Set ignoredEdges = getIgnoredEdges();
			if (viewport == null) {
				selective = false;
			}
			else {
				if (getGraph().nodeCount() > 2000) {
					logger.info("Switching to selective displaying...");
					selective = true;
				}
				else {
					selective = false;
				}
			}
			if (!ignoreOld) {
				Iterator n = getGraph().getNodesIterator();
				while (n.hasNext()) {
					Node node = (Node) n.next();
					NodeComponent nc = (NodeComponent) node.getContents();
					if (nc.hasProperty(LOCATION)) {
						Point p = (Point) nc.getPropertyValue(LOCATION);
						fixedNodes.put(node, p);
					}
				}
			}
			Hashtable coords = null;
			if (fixedNodes.size() != getGraph().nodeCount()) {
				if (getLayoutEngine() instanceof StatusReporter) {
					((StatusReporter) getLayoutEngine()).setStatusManager(getCanvas().getStatusManager());
				}
				if (getLayoutEngine() instanceof GraphLayoutEngine2) {
					((GraphLayoutEngine2) getLayoutEngine()).setIgnoredEdges(ignoredEdges);
				}
				if (getLayoutEngine() instanceof PersistentLayoutEngine2) {
					coords = ((PersistentLayoutEngine2) getLayoutEngine()).layoutGraph(getGraph(),
							fixedNodes, ignoreOld);
				}
				else {
					coords = getLayoutEngine().layoutGraph(getGraph(), fixedNodes);
				}
			}
			else {
				coords = fixedNodes;
			}
			boolean incomplete = false;
			NodeIterator n = getGraph().getNodesIterator();
			while (n.hasMoreNodes()) {
				Node node = n.nextNode();
				NodeComponent nc = (NodeComponent) node.getContents();
				Point p = (Point) coords.get(node);
				if (p == null) {
					p = new Point(10, 10);
					incomplete = true;
				}
				nc.setPropertyValue(LOCATION, p);
			}
			if (incomplete) {
				logger.warn("Incomplete layout");
			}
			EdgeIterator e = getGraph().getEdgesIterator();
			while (e.hasMoreEdges()) {
				Edge edge = e.nextEdge();
				EdgeComponent ec = (EdgeComponent) edge.getContents();
				NodeComponent from = (NodeComponent) edge.getFromNode().getContents();
				NodeComponent to = (NodeComponent) edge.getToNode().getContents();
				ec.getControlPoint(0).setLocation((Point) from.getPropertyValue(LOCATION));
				ec.getControlPoint(1).setLocation((Point) to.getPropertyValue(LOCATION));
			}
			getCanvas().getStatusManager().pop();
			fixedNodes = null;
			if (selective) {
				initializeNodesSelective(coords);
				initializeEdgesSelective(coords);
			}
			else {
				initializeNodesNormal(newWrappers, coords);
				initializeEdgesNormal(newWrappers, coords);
				setUpListenersNormal(newWrappers);
			}

			if (!selective) {
				Iterator i = wrappers.keySet().iterator();
				while (i.hasNext()) {
					Object nodeOrEdge = i.next();
					GraphComponentWrapper oldWrapper = (GraphComponentWrapper) wrappers.get(nodeOrEdge);
					GraphComponentWrapper newWrapper = (GraphComponentWrapper) newWrappers.get(nodeOrEdge);

					if (oldWrapper != newWrapper) {
						container.remove(oldWrapper);
						oldWrapper.dispose();
					}
				}
				wrappers = newWrappers;
			}
			resizeContainer();
			if (selective) {
				updateVisibleComponents();
			}
		}
		getCanvas().getStatusManager().push("Validating container...");
		container.reallyValidate();
		container.repaint();
		if (overviewFrame != null) {
			overviewFrame.repaint();
		}
		getCanvas().getStatusManager().pop();
		invalidating = false;
	}

	public void initializeNodesSelective(Map coords) {

	}

	public void initializeNodesNormal(Map newWrappers, Map coords) {
		int count = 0;
		getCanvas().getStatusManager().push("Initializing nodes...", StatusManager.BUSY_ICON);
		getCanvas().getStatusManager().initializeProgress(getGraph().nodeCount() / 50);
		NodeIterator i = getGraph().getNodesIterator();
		while (i.hasMoreNodes()) {
			count++;
			if ((count % 50) == 0) {
				getCanvas().getStatusManager().stepProgress();
			}
			Node node = i.nextNode();
			NodeComponentWrapper w;
			if (!wrappers.containsKey(node)) {
				w = newWrapper(node);
			}
			else {
				w = updateWrapper(node);
			}
			newWrappers.put(node, w);
			container.add(w);
		}
		getCanvas().getStatusManager().removeProgress();
		getCanvas().getStatusManager().pop();
	}

	public NodeComponentWrapper newWrapper(Node n) {
		NodeComponent nc = (NodeComponent) n.getContents();
		NodeComponentWrapper w = new NodeComponentWrapper(nc);
		w.setAntiAliasing(antiAliasing);
		Dimension d = (Dimension) nc.getPropertyValue(SIZE);
		if (d == null) {
			d = w.getPreferredNodeSize();
			nc.setPropertyValue(SIZE, d);
		}
		w.setNodeSize(d);
		Point p = (Point) nc.getPropertyValue(LOCATION);
		setCenter(w, p);
		w.addGraphComponentListener(this);
		return w;
	}

	public NodeComponentWrapper updateWrapper(Node n) {
		NodeComponent nc = (NodeComponent) n.getContents();
		NodeComponentWrapper w = (NodeComponentWrapper) wrappers.get(n);
		Dimension d;
		if (nc.hasProperty(SIZE)) {
			d = (Dimension) nc.getPropertyValue(SIZE);
		}
		else {
			d = w.getPreferredNodeSize();
			nc.setPropertyValue(SIZE, d);
		}
		w.setNodeSize(d);
		Point p = (Point) nc.getPropertyValue(LOCATION);
		setCenter(w, p);
		return w;
	}

	public void initializeEdgesSelective(Map coords) {

	}

	public void initializeEdgesNormal(Map newWrappers, Map coords) {
		getCanvas().getStatusManager().push("Initializing edges...", StatusManager.BUSY_ICON);
		getCanvas().getStatusManager().initializeProgress(getGraph().edgeCount() / 50);
		int count = 0;
		EdgeIterator i = getGraph().getEdgesIterator();
		while (i.hasMoreEdges()) {
			count++;
			if ((count % 50) == 0) {
				getCanvas().getStatusManager().stepProgress();
			}
			Edge edge = i.nextEdge();
			EdgeComponentWrapper w;
			if (!wrappers.containsKey(edge)) {
				w = newWrapper(edge);
			}
			else {
				w = updateWrapper(edge);
			}
			newWrappers.put(edge, w);
			container.add(w);
		}
		getCanvas().getStatusManager().removeProgress();
		getCanvas().getStatusManager().pop();
	}

	public EdgeComponentWrapper newWrapper(Edge e) {
		EdgeComponent ec = (EdgeComponent) e.getContents();
		EdgeComponentWrapper w = new EdgeComponentWrapper(ec);
		w.setAntiAliasing(antiAliasing);
		w.moveToBack();
		NodeComponent from = (NodeComponent) e.getFromNode().getContents();
		NodeComponent to = (NodeComponent) e.getToNode().getContents();
		setEdgeCoords(w, from, to);
		w.addGraphComponentListener(this);
		return w;
	}

	public EdgeComponentWrapper updateWrapper(Edge e) {
		EdgeComponentWrapper w = (EdgeComponentWrapper) wrappers.get(e);
		NodeComponent from = (NodeComponent) e.getFromNode().getContents();
		NodeComponent to = (NodeComponent) e.getToNode().getContents();
		setEdgeCoords(w, from, to);
		return w;
	}

	public void setUpListenersNormal(Map newWrappers) {
		getCanvas().getStatusManager().push("Setting up listeners...");
		if (getGraph().nodeCount() > 250) {
			getCanvas().getStatusManager().initializeProgress(getGraph().nodeCount() / 50);
		}
		int count = 0;
		Iterator nws = newWrappers.values().iterator();
		while (nws.hasNext()) {
			Object next = nws.next();
			if (!(next instanceof NodeComponentWrapper)) {
				continue;
			}
			count++;
			if ((count % 50) == 0) {
				getCanvas().getStatusManager().stepProgress();
			}
			NodeComponentWrapper w = (NodeComponentWrapper) next;
			w.addGraphComponentListener(this);
		}
		getCanvas().getStatusManager().pop();
	}

	public boolean isEditable() {
		return editable;
	}

	public boolean isSelective() {
		return selective;
	}

	public void canvasActionPerformed(CanvasActionEvent e) {
		logger.debug("Action: " + e.getCanvasAction());
		if (e.getType() == CanvasActionEvent.SELECTED_STATE_CHANGED) {
			if (e.getSource() == overview) {
				if (!overview.isSelected()) {
					closeOverview(Boolean.FALSE);
				}
				else {
					openOverview();
				}
			}
			else if (e.getCanvasAction() == antiAliasingMI) {
				setAntiAliasing(antiAliasingMI.isSelected());
				repaint();
			}
			else if (e.getCanvasAction() == persistence) {
				if (persistence.isSelected()) {
					logger.debug("Enabling persistence");
					setLayoutEngine(new PersistentLayoutEngine2(getLayoutEngine()));
				}
				else {
					logger.debug("Disabling persistence");
					setLayoutEngine(((PersistentLayoutEngine2) getLayoutEngine()).getLayoutEngine());
				}
			}
		}
		else if (e.getType() == CanvasActionEvent.PERFORM) {
			if (e.getCanvasAction() == reLayout) {
				reLayout();
			}
			else if (components.containsKey(e.getSource())) {
				GraphComponent gc = (GraphComponent) components.get(e.getSource());
				crtComp = gc;
				if (gc instanceof NodeComponent) {
					setMode(MODE_NODE);
					logger.debug("Node mode enabled");
				}
				else {
					setMode(MODE_EDGE);
					logger.debug("Edge mode enabled");
				}
			}
			else if (e.getCanvasAction() == selectionArrow) {
				crtComp = null;
				setMode(MODE_SELECT);
				logger.debug("Selection mode enabled");
			}
			else if (e.getCanvasAction() == dilate) {
				dilate();
			}
			else if (e.getCanvasAction() == contract) {
				contract();
			}
			else if (layouts.containsKey(e.getCanvasAction())) {
				GraphLayoutEngine layout = (GraphLayoutEngine) layouts.get(e.getCanvasAction());
				logger.debug("Switching layout");
				if (persistence.isEnabled()) {
					setLayoutEngine(new PersistentLayoutEngine2(layout));
					reLayout();
				}
				else {
					setLayoutEngine(layout);
					reLayout();
				}
			}
		}
	}

	protected void scale(int mul, int div) {
		NodeIterator i = getGraph().getNodesIterator();
		while (i.hasMoreNodes()) {
			Node n = i.nextNode();
			NodeComponent nc = (NodeComponent) n.getContents();
			Point p = (Point) nc.getPropertyValue(LOCATION);
			Point q = new Point(p.x * mul / div, p.y * mul / div);
			nc.setPropertyValue(LOCATION, q);
			updateWrapper(n);
		}
		EdgeIterator j = getGraph().getEdgesIterator();
		while (j.hasMoreEdges()) {
			Edge e = j.nextEdge();
            updateWrapper(e);
		}
        resizeContainer();
	}

	protected void dilate() {
		scale(3, 2);
	}

	protected void contract() {
		scale(2, 3);
	}

	protected void setMode(int mode) {
		logger.debug("Setting mode to " + mode);
		if (this.mode == mode) {
			return;
		}
		if (this.mode == MODE_EDGE) {
			container.removeMouseMotionListener(this);
			Component[] components = container.getComponents();
			for (int i = 0; i < components.length; i++) {
				if (components[i] instanceof NodeComponentWrapper) {
					components[i].removeMouseListener(this);
				}
			}
			cancelEdgeEditing();
		}
		if (mode == MODE_EDGE) {
			container.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			Component[] components = container.getComponents();
			for (int i = 0; i < components.length; i++) {
				if (components[i] instanceof NodeComponentWrapper) {
					components[i].addMouseListener(this);
				}
			}
			tempEdge = (EdgeComponent) crtComp.newInstance();
			setEdgeEditingStep(EDGE_EDITING_STEP_FROM_NODE);
		}
		if (mode == MODE_SELECT) {
			container.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		if (mode == MODE_NODE) {
			container.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}
		this.mode = mode;
	}

	public void mouseClicked(MouseEvent e) {
		logger.debug("Mouse clicked: " + e);
		if (mode == MODE_SELECT) {
			// nothing. Let the wrappers handle this
			unselectAll();
		}
		else if (mode == MODE_NODE) {
			// add a node
			logger.debug("Adding node...");
			GraphComponent gc = getCanvas().createComponent(crtComp.getComponentType());
			gc.setPropertyValue("name", gc.getPropertyValue("nodeid"));
			gc.setPropertyValue(LOCATION, new Point(e.getX(), e.getY()));
			getCanvas().addComponent(gc);
		}
		else if (mode == MODE_EDGE) {
			if (e.getSource() != getComponent()) {
				wrapperClicked(e);
			}
		}
	}

	private void unselectAll() {
		GraphComponentWrapper[] c = (GraphComponentWrapper[]) selected.toArray(new GraphComponentWrapper[0]);
		for (int i = 0; i < c.length; i++) {
			c[i].setSelected(false);
		}
	}

	private void unselectAllExcept(GraphComponentWrapper except) {
		GraphComponentWrapper[] c = (GraphComponentWrapper[]) selected.toArray(new GraphComponentWrapper[0]);
		for (int i = 0; i < c.length; i++) {
			if (c[i] == except) {
				continue;
			}
			c[i].setSelected(false);
		}
	}

	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	public void mouseEntered(MouseEvent e) {
		if (e.getSource() instanceof GraphComponentWrapper) {
			wrapperEntered(e);
		}
	}

	public void mouseExited(MouseEvent e) {
		if (e.getSource() instanceof GraphComponentWrapper) {
			wrapperExited(e);
		}
	}

	public void mouseMoved(MouseEvent e) {
		if (edgeEditingStep == EDGE_EDITING_STEP_TO_NODE) {
			setEdgeCoords(tempEdgeWrapper, tempFromNodeWrapper, e.getPoint());
		}
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.cog.gui.grapheditor.canvas.views.Editor#newEdgeRequested(org.globus.cog.gui.grapheditor.edges.EdgeComponent)
	 */
	public void newEdgeRequested(EdgeComponent prototype) {
		if (edgeEditingStep != EDGE_EDITING_STEP_NONE) {
			cancelEdgeEditing();
		}
		if (!isEditable()) {
			return;
		}
		if (getGraph().nodeCount() < 2) {
			// well, not really
			JOptionPane.showMessageDialog(null,
					"You must have at least two nodes to create an edge", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		setEdgeEditingStep(EDGE_EDITING_STEP_FROM_NODE);
		tempEdge = (EdgeComponent) prototype.newInstance();
	}

	private void openOverview() {
		overviewFrame = new OverviewFrame(getSwingRenderer().getPanel(), this);
		overviewFrame.addWindowListener(this);
		overviewFrame.setResizable(true);
		PropertyHolder owner = getCanvas().getOwner();
		if (owner.hasProperty("overview.location")) {
			overviewFrame.setLocation((Point) owner.getPropertyValue("overview.location"));
		}
		if (owner.hasProperty("overview.size")) {
			overviewFrame.setSize((Dimension) owner.getPropertyValue("overview.size"));
		}
		else {
			overviewFrame.setSize(new Dimension(160, 140));
		}
		overviewFrame.setVisible(true);
		overview.setSelected(true);
	}

	public void reLayout() {
		invalidate(true);
	}

	private void resizeContainer() {
		NodeIterator i = getGraph().getNodesIterator();
		maxw = 0;
		maxh = 0;
		minx = Integer.MAX_VALUE;
		miny = Integer.MAX_VALUE;
		while (i.hasMoreNodes()) {
			NodeComponent nc = (NodeComponent) i.nextNode().getContents();
			Point p = (Point) nc.getPropertyValue(LOCATION);
			Dimension d = (Dimension) nc.getPropertyValue(SIZE);
			int x1, x2, y1, y2, w, h;
			if (d == null) {
				w = DEFAULT_SIZE.width / 2;
				h = DEFAULT_SIZE.height / 2;
			}
			else {
				w = d.width / 2;
				h = d.height / 2;
			}
			x1 = p.x - w;
			x2 = p.x + w;
			y1 = p.y - h;
			y2 = p.y + h;
			if (x2 > maxw) {
				maxw = x2;
			}
			if (x1 < minx) {
				minx = x1;
			}
			if (y2 > maxh) {
				maxh = y2;
			}
			if (y1 < miny) {
				miny = y1;
			}
		}
		logger.debug("minx=" + minx + ", miny=" + miny + ", maxw=" + maxw + ", maxh=" + maxh);
		if ((minx < Integer.MAX_VALUE) || (miny < Integer.MAX_VALUE)) {
			Iterator j = wrappers.values().iterator();
			while (j.hasNext()) {
				Component comp = (Component) j.next();
				Point p = comp.getLocation();
				p.x -= minx;
				p.y -= miny;
				comp.setLocation(p);
			}
			NodeIterator n = getGraph().getNodesIterator();
			while (n.hasMoreNodes()) {
				Node node = n.nextNode();
				NodeComponent nc = (NodeComponent) node.getContents();
				Point p = (Point) nc.getPropertyValue(LOCATION);
				p.setLocation(p.x - minx, p.y - miny);
			}
			EdgeIterator e = getGraph().getEdgesIterator();
			while (e.hasMoreEdges()) {
				Edge edge = e.nextEdge();
				EdgeComponent ec = (EdgeComponent) edge.getContents();
				Point p = ec.getControlPoint(0);
				p.setLocation(p.x - minx, p.y - miny);
				p = ec.getControlPoint(1);
				p.setLocation(p.x - minx, p.y - miny);
			}
			maxw -= minx;
			maxh -= miny;
		}
		container.setSize(maxw, maxh);
		minx = 0;
		miny = 0;
	}

	public void setAntiAliasing(boolean antiAliasing) {
		this.antiAliasing = antiAliasing;
		if (wrappers != null) {
			Iterator i = wrappers.values().iterator();
			while (i.hasNext()) {
				GraphComponentWrapper w = (GraphComponentWrapper) i.next();
				w.setAntiAliasing(antiAliasing);
			}
		}
		PropertyHolder owner = getCanvas().getOwner();
		if (!owner.hasProperty("graphview.antialiasing")) {
			owner.addProperty(new OverlayedProperty(owner, "graphview.antialiasing", Property.RWH));
		}
		owner.setPropertyValue("graphview.antialiasing", Boolean.valueOf(antiAliasing));
	}

	public void setCenter(Component component, Point p) {
		component.setLocation(p.x - component.getWidth() / 2 + dx, p.y - component.getHeight() / 2
				+ dy);
	}

	public void setEdgeCoords(EdgeComponentWrapper edge, NodeComponent from, NodeComponent to) {
		Point fc = (Point) from.getPropertyValue(LOCATION);
		Point tc = (Point) to.getPropertyValue(LOCATION);
		Point pf = getRectIntersection(from, tc);
		Point pt = getRectIntersection(to, fc);
		pf.translate(dx, dy);
		pt.translate(dx, dy);
		edge.setCoords(pf.x, pf.y, pt.x, pt.y);
	}

	public void setEdgeCoords(EdgeComponentWrapper edge, Component from, Component to) {
		Point fc = getCenter(from);
		Point tc = getCenter(to);
		Point pf = getRectIntersection(from, tc);
		Point pt = getRectIntersection(to, fc);
		edge.setCoords(pf.x, pf.y, pt.x, pt.y);
	}

	public static void setEdgeCoords(EdgeComponentWrapper edge, Component from, Point pt) {
		Point pf = getRectIntersection(from, pt);
		edge.setCoords(pf.x, pf.y, pt.x, pt.y);
	}

	private void setEdgeEditingStep(int edgeEditingStep) {
		this.edgeEditingStep = edgeEditingStep;
		if (edgeEditingStep == EDGE_EDITING_STEP_FROM_NODE) {
			container.removeMouseMotionListener(this);
		}
		else if (edgeEditingStep == EDGE_EDITING_STEP_TO_NODE) {
			container.addMouseMotionListener(this);
		}
		logger.debug("Edge editing step: " + edgeEditingStep);
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public void setLayoutEngine(GraphLayoutEngine layoutEngine) {
		this.layoutEngine = layoutEngine;
	}

	public void setViewport(Rectangle rect) {
		this.viewport = rect;
		if (getGraph() == null) {
			return;
		}
		if (this.viewport == null) {
			selective = false;
		}
		else {
			if (getGraph().nodeCount() > 2000) {
				selective = true;
			}
			else {
				selective = false;
			}
		}
		if (selective) {
			updateVisibleComponents();
		}
	}

	private void updateVisibleComponents() {
		boolean changed = false;
		int tot = 0;
		NodeIterator i = getGraph().getNodesIterator();
		while (i.hasNext()) {
			if (tot++ % 1000 == 0) {
				logger.debug("" + (tot - 1) + " nodes");
			}
			Node n = i.nextNode();
			NodeComponent nc = (NodeComponent) n.getContents();
			Point l = (Point) nc.getPropertyValue(LOCATION);
			Dimension d = (Dimension) nc.getPropertyValue(SIZE);
			if (l == null) {
				continue;
			}
			if (d == null) {
				// TODO
				d = DEFAULT_SIZE;
			}
			Rectangle rect = new Rectangle(l.x - d.width / 2, l.y - d.height / 2, d.width, d.height);
			if (viewport.intersects(rect)) {
				if (!wrappers.containsKey(n)) {
					NodeComponentWrapper nw = newWrapper(n);
					wrappers.put(n, nw);
					container.add(nw);
					changed = true;
				}
			}
			else {
				if (wrappers.containsKey(n)) {
					NodeComponentWrapper nw = (NodeComponentWrapper) wrappers.get(n);
					container.remove(nw);
					wrappers.remove(n);
					changed = true;
				}
			}
		}
		tot = 0;
		EdgeIterator k = getGraph().getEdgesIterator();
		while (k.hasMoreEdges()) {
			if (tot++ % 1000 == 0) {
				logger.debug("" + (tot - 1) + " edges");
			}
			Edge e = k.nextEdge();
			EdgeComponent ec = (EdgeComponent) e.getContents();
			Point p1 = ec.getControlPoint(0);
			Point p2 = ec.getControlPoint(1);
			if (!viewport.intersectsLine(p1.x, p1.y, p2.x, p2.y)) {
				if (wrappers.containsKey(e)) {
					EdgeComponentWrapper ew = (EdgeComponentWrapper) wrappers.get(e);
					container.remove(ew);
					wrappers.remove(e);
					changed = true;
				}
			}
			else {
				if (!wrappers.containsKey(e)) {
					EdgeComponentWrapper ew = newWrapper(e);
					wrappers.put(e, ew);
					container.add(ew);
					changed = true;
				}
			}
		}
		if (changed) {
			container.reallyValidate();
		}
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		closeOverview(Boolean.FALSE);
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	private void wrapperClicked(MouseEvent e) {
		logger.debug("Wrapper clicked " + e);
		if (!(e.getSource() instanceof NodeComponentWrapper)) {
			return;
		}
		if (edgeEditingStep == EDGE_EDITING_STEP_FROM_NODE) {
			tempFromNodeWrapper = (NodeComponentWrapper) e.getSource();
			tempEdgeWrapper = new EdgeComponentWrapper(tempEdge);
			tempEdgeWrapper.setAntiAliasing(antiAliasing);
			tempEdgeWrapper.moveToFront();
			setEdgeCoords(tempEdgeWrapper, tempFromNodeWrapper, e.getPoint());
			container.add(tempEdgeWrapper);
			tempEdgeWrapper.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			container.reallyValidate();
			container.repaint();
			setEdgeEditingStep(EDGE_EDITING_STEP_TO_NODE);
		}
		else if (edgeEditingStep == EDGE_EDITING_STEP_TO_NODE) {
			container.remove(tempEdgeWrapper);
			NodeComponentWrapper toWrapper = (NodeComponentWrapper) e.getSource();
			tempEdge.setPropertyValue("from",
					tempFromNodeWrapper.getGraphComponent().getPropertyValue("nodeid"));
			tempEdge.setPropertyValue("to",
					toWrapper.getGraphComponent().getPropertyValue("nodeid"));
			getCanvas().addComponent(tempEdge);
			tempEdge = (EdgeComponent) crtComp.newInstance();
			setEdgeEditingStep(EDGE_EDITING_STEP_FROM_NODE);
			((NodeComponentWrapper) e.getSource()).setHighlighted(false);
		}
	}

	protected void wrapperEntered(MouseEvent e) {
		logger.debug("Wrapper entered");
		((NodeComponentWrapper) e.getSource()).setHighlighted(true);
		if (edgeEditingStep == EDGE_EDITING_STEP_TO_NODE) {
			setEdgeCoords(tempEdgeWrapper, tempFromNodeWrapper, (Component) e.getSource());
		}
	}

	protected void wrapperExited(MouseEvent e) {
		((NodeComponentWrapper) e.getSource()).setHighlighted(false);
	}

	public void actionPerformed(ActionEvent e) {

	}

	public synchronized void canvasEvent(CanvasEvent e) {
		if (e.getType() == CanvasEvent.COMPONENT_ADDED) {
			updateVisibleComponents();
		}
		if (e.getType() == CanvasEvent.COMPONENT_REMOVED) {
			Object o = e.getNode();
			if (o == null) {
				o = e.getEdge();
			}
			GraphComponentWrapper w = (GraphComponentWrapper) wrappers.get(o);
			if (w != null) {
				container.remove(w);
				w.removeGraphComponentListener(this);
				w.removeMouseListener(this);
				w.dispose();
			}
			wrappers.remove(o);
			container.reallyValidate();
			container.repaint();
		}
		if (e.getType() == CanvasEvent.INVALIDATE) {
			invalidate(true);
		}
	}
}

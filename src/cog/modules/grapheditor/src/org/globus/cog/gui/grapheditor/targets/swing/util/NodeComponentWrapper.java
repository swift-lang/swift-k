// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.grapheditor.targets.swing.util;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JSeparator;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.canvas.CanvasEvent;
import org.globus.cog.gui.grapheditor.canvas.CanvasEventListener;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.HierarchicalLayout;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.PersistentLayoutEngine2;
import org.globus.cog.gui.grapheditor.nodes.EditableNodeComponent;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.targets.swing.SwingCanvasRenderer;
import org.globus.cog.gui.grapheditor.targets.swing.SwingComponentRenderer;
import org.globus.cog.gui.grapheditor.targets.swing.views.GraphView;
import org.globus.cog.gui.grapheditor.targets.swing.views.WrappedPassiveGraphView;
import org.globus.cog.gui.grapheditor.util.swing.EventTrappingContainer;
import org.globus.cog.gui.grapheditor.util.swing.MouseEventDispatcher;
import org.globus.cog.gui.grapheditor.util.swing.SquareAnchor;
import org.globus.cog.gui.grapheditor.util.swing.TransparentContainer;
import org.globus.cog.util.ImageLoader;
import org.globus.cog.util.graph.GraphChangedEvent;
import org.globus.cog.util.graph.GraphListener;
import org.globus.cog.util.graph.Node;
import org.globus.cog.util.graph.NodeIterator;

/**
 * This class defines a visual component that wraps a node component allowing
 * resizing and moving
 */
public class NodeComponentWrapper extends GraphComponentWrapper implements AnchorListener,
		MouseListener, MouseMotionListener, FocusListener, PropertyChangeListener, ActionListener,
		ComponentListener, CanvasEventListener, GraphListener {

	private static Logger logger = Logger.getLogger(NodeComponentWrapper.class);
	private static Cursor MOVE_CURSOR = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
	private static Cursor CROSSHAIR_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(
			ImageLoader.loadIcon("images/cursor-circle.png").getImage(), new Point(8, 8), "null");
	private static Cursor DEFAULT_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

	public static final String KEEP_ASPECT_RATIO = "wrapper.keepaspectratio";

	public static final String EXPANDED = "wrapper.expanded";

	public static final String SHOW_SUBNODES = "wrapper.showsubnodes";

	public static final String INTERACTIVE = "wrapper.interactive";

	private static final int RESIZING = 0x0100;
	private static final int MOVING = 0x0200;
	private static final int FRAME_VISIBLE = 0x0400;
	private static final int HIGHLIGHTED = 0x0800;
	private static final int SHOW_FRAME_WHEN_FOCUSED = 0x1000;
	private static final int DRAG_CURSOR = 0x2000;
	private static final int EDITABLE = 0x4000;

	private short dxi, dyi;

	private Anchor[] anchors;

	private EventTrappingContainer ec;

	private CanvasAction expanded;

	private CanvasAction keepAspectRatio;

	private CanvasAction showSubNodes;

	private CanvasAction interactive;

	private SwingCanvasRenderer canvasRenderer;

	public NodeComponentWrapper(NodeComponent e) {
		super(e);
		e.addPropertyChangeListener(this);
		unsetFlag(RESIZING);
		unsetFlag(MOVING);
		unsetFlag(FRAME_VISIBLE);
		unsetFlag(HIGHLIGHTED);
		setFlag(SHOW_FRAME_WHEN_FOCUSED);
		setMovable(true);
		enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
		enableEvents(AWTEvent.FOCUS_EVENT_MASK);
		addWrapperAction(expanded = new CanvasAction("101#Expanded", CanvasAction.ACTION_SWITCH
				+ CanvasAction.SEPARATOR_BEFORE));
		addWrapperAction(keepAspectRatio = new CanvasAction("102#Keep Aspect Ratio",
				CanvasAction.ACTION_SWITCH));
		addWrapperAction(showSubNodes = new CanvasAction("103#Show Sub Node Count",
				CanvasAction.ACTION_SWITCH));
		addWrapperAction(interactive = new CanvasAction("104#Interactive",
				CanvasAction.ACTION_SWITCH));
	}

	private void createAnchors() {
		synchronized (this) {
			anchors = new Anchor[8];

			if (getNodeComponent().isResizable()) {

				for (int i = 0; i < 8; i++) {
					anchors[i] = new SquareAnchor(this);
					anchors[i].addAnchorListener(this);
					add(anchors[i]);
				}

				anchors[0].setType(Anchor.N);
				anchors[0].setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));

				anchors[1].setType(Anchor.NE);
				anchors[1].setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));

				anchors[2].setType(Anchor.E);
				anchors[2].setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));

				anchors[3].setType(Anchor.SE);
				anchors[3].setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));

				anchors[4].setType(Anchor.S);
				anchors[4].setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));

				anchors[5].setType(Anchor.SW);
				anchors[5].setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));

				anchors[6].setType(Anchor.W);
				anchors[6].setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));

				anchors[7].setType(Anchor.NW);
				anchors[7].setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
			}
		}
		doLayout();
	}

	private void removeAnchors() {
		if (anchors != null) {
			for (int i = 0; i < 8; i++) {
				if (anchors[i] != null) {
					anchors[i].removeAnchorListener(this);
					remove(anchors[i]);
				}
			}
		}
	}

	public JMenu createMenuFromActions() {
		JMenu menu = super.createMenuFromActions();
		JLabel info;
		String name = "Node";
		if (getNodeComponent().hasProperty("name")) {
			name = (String) getNodeComponent().getPropertyValue("name");
		}
		if (isEditable()) {
			GraphCanvas canvas = getNodeComponent().getCanvas();
			if (canvas == null) {
				info = new JLabel(name + ": editable, 0 sub-nodes");
			}
			else {
				int numNodes = canvas.getGraph().nodeCount();
				info = new JLabel(name + ": editable, " + numNodes + " sub-node(s)");
			}
		}
		else {
			info = new JLabel(name);
		}
		info.setOpaque(true);
		menu.add(info, 0);
		menu.add(new JSeparator(), 1);
		return menu;
	}

	/**
	 * For some reason I don't like this method
	 */
	public void setUpComponent() {
		NodeComponent nc = getNodeComponent();
		if (nc.hasProperty(SHOW_SUBNODES)) {
			showSubNodes.setSelectedQuiet(((Boolean) nc.getPropertyValue(SHOW_SUBNODES)).booleanValue());
		}
		else {
			showSubNodes.setSelectedQuiet(false);
		}
		if (nc.hasProperty(EXPANDED)) {
			expanded.setSelectedQuiet(((Boolean) nc.getPropertyValue(EXPANDED)).booleanValue());
		}
		else {
			expanded.setSelectedQuiet(false);
		}
		if (!getFlag(EDITABLE)) {
			expanded.setEnabled(false);
		}
		if (nc.hasProperty(INTERACTIVE)) {
			interactive.setSelectedQuiet(((Boolean) nc.getPropertyValue(INTERACTIVE)).booleanValue());
		}
		else {
			interactive.setSelectedQuiet(false);
		}
		if (nc.hasProperty(KEEP_ASPECT_RATIO)) {
			keepAspectRatio.setSelectedQuiet(((Boolean) nc.getPropertyValue(KEEP_ASPECT_RATIO)).booleanValue());
		}
		else {
			keepAspectRatio.setSelectedQuiet(true);
		}
		if (isExpanded()) {
			interactive.setEnabled(true);
			keepAspectRatio.setEnabled(true);

			if (nc.getCanvas() == null) {
				nc.createCanvas();
			}

			if (getRenderer() != null) {
				getRenderer().dispose();
			}

			canvasRenderer = new ScalingSwingCanvasRenderer();
			canvasRenderer.setCanvas(nc.getCanvas());
			addListeners(nc.getCanvas());
			WrappedPassiveGraphView view = new WrappedPassiveGraphView(new PersistentLayoutEngine2(
					new HierarchicalLayout()), "Graph View - Level Layout");
			canvasRenderer.setView(view);

			nc.getCanvas().addCanvasEventListener(this);
			ScalingContainer ecc = new ScalingContainer(canvasRenderer.getComponent());
			if (isUsedAsRenderer()) {
				ecc.setImmediate(true);
			}
			ecc.setPaintLock(nc.getCanvas());
			if (nc.hasProperty("name")) {
				ecc.setTitle((String) nc.getPropertyValue("name"));
			}
			else {
				ecc.setTitle(nc.getComponentType());
			}
			ecc.setIcon((ImageIcon) nc.getPropertyValue("icon"));
			ecc.setKeepAspectRatio(keepAspectRatio.isSelected());
			//disable them for now
			ecc.disableMouseEvents();
			this.ec = ecc;
			setComponent(this.ec);
			Dimension d;
			if (nc.hasProperty("wrapper.expandedsize")) {
				d = (Dimension) nc.getPropertyValue("wrapper.expandedsize");
				nc.removeProperty(nc.getProperty("wrapper.expandedsize"));
			}
			else {
				d = new Dimension(160, 140);
			}
			setNodeSize(d);
			if (!d.equals(nc.getPropertyValue(GraphView.SIZE))) {
				nc.setPropertyValue(GraphView.SIZE, d);
			}
			fireGraphComponentEvent(new GraphComponentWrapperEvent(this,
					GraphComponentWrapperEvent.RESIZED));
		}
		else {
			if (keepAspectRatio.isEnabled()) {
				keepAspectRatio.setEnabled(false);
			}
			if (canvasRenderer != null) {
				canvasRenderer.dispose();
				canvasRenderer = null;
			}
			removeListeners(nc.getCanvas());
			SwingComponentRenderer componentRenderer = (SwingComponentRenderer) getNodeComponent().newRenderer();
			this.ec = new TransparentContainer(componentRenderer.getVisualComponent());
			this.ec.disableMouseEvents();
			setComponent(null);
			setRenderer(componentRenderer);
			setComponent(this.ec);

			Dimension d;
			if (nc.hasProperty("wrapper.normalsize")) {
				d = (Dimension) nc.getPropertyValue("wrapper.normalsize");
				nc.removeProperty(nc.getProperty("wrapper.normalsize"));
			}
			else {
				d = getPreferredNodeSize();
			}
			setNodeSize(d);
			if (!d.equals(nc.getPropertyValue(GraphView.SIZE))) {
				nc.setPropertyValue(GraphView.SIZE, d);
			}
			fireGraphComponentEvent(new GraphComponentWrapperEvent(this,
					GraphComponentWrapperEvent.RESIZED));
		}
	}

	private void addListeners(GraphCanvas canvas) {
		NodeIterator i = canvas.getGraph().getNodesIterator();
		while (i.hasMoreNodes()) {
			Node n = i.nextNode();
			NodeComponent nc = (NodeComponent) n.getContents();
			nc.addPropertyChangeListener(this);
		}
	}

	private void removeListeners(GraphCanvas canvas) {
		if (canvas == null) {
			return;
		}
		NodeIterator i = canvas.getGraph().getNodesIterator();
		while (i.hasMoreNodes()) {
			Node n = i.nextNode();
			NodeComponent nc = (NodeComponent) n.getContents();
			nc.removePropertyChangeListener(this);
		}
	}

	public void setComponent(Component comp) {
		if (getComponent() != null) {
			getComponent().removeComponentListener(this);
			remove(getComponent());
		}
		super.setComponent(comp);
		if (getComponent() != null) {
			NodeComponent nc = getNodeComponent();
			setFlag(EDITABLE, (nc instanceof EditableNodeComponent));
			add(getComponent());
			getComponent().addComponentListener(this);
			nc.addPropertyChangeListener(this);
			if (getFlag(EDITABLE)) {
				if (nc.getCanvas() != null) {
					nc.getCanvas().getGraph().addGraphListener(this);
				}
			}
		}
	}

	public Dimension getPreferredSize() {
		Dimension d = getComponent().getPreferredSize();
		return new Dimension(d.width + 10, d.height + 10);
	}

	private boolean isExpanded() {
		return expanded.isSelected() && !(isUsedAsRenderer());
	}

	public void doLayout() {
		Dimension size = getSize();
		Component c = getComponent();
		if (c != null) {
			c.setSize(size.width - 10, size.height - 10);
			c.setLocation(5, 5);
		}
		updateAnchors();
		if (isExpanded()) {
			if (ec != null) {
				((ScalingContainer) ec).invalidate();
			}
		}
	}

	public void updateAnchors() {
		if (!getNodeComponent().isResizable()) {
			return;
		}
		if (anchors == null) {
			return;
		}
		Dimension size = getSize();
		int xl = 0, xc = (size.width - 5) / 2, xr = size.width - 5;
		int yt = 0, yc = (size.height - 5) / 2, yb = size.height - 5;
		synchronized (this) {
			anchors[0].setLocation(xc, yt);
			anchors[1].setLocation(xr, yt);
			anchors[2].setLocation(xr, yc);
			anchors[3].setLocation(xr, yb);
			anchors[4].setLocation(xc, yb);
			anchors[5].setLocation(xl, yb);
			anchors[6].setLocation(xl, yc);
			anchors[7].setLocation(xl, yt);
		}
	}

	public void paint(Graphics g) {
		if (!getFlag(VALID)) {
			validate();
		}
		if (getAntiAliasing()) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		}
		Dimension size = getSize();
		if (getFlag(FRAME_VISIBLE) || getFlag(HIGHLIGHTED)) {
			g.clearRect(0, 0, size.width, size.height);
			Color c = g.getColor();
			if (getFlag(RESIZING) || getFlag(MOVING)) {
				g.setColor(Color.red);
			}
			else if (getFlag(HIGHLIGHTED)) {
				g.setColor(Color.green);
			}
			g.drawRect(2, 2, size.width - 5, size.height - 5);
			g.setColor(c);
		}
		super.paint(g);

		if (showSubNodes.isSelected()) {
			int numNodes = 0;
			if (getNodeComponent().getCanvas() != null) {
				numNodes = getNodeComponent().getCanvas().getGraph().nodeCount();
			}
			String sNumNodes = String.valueOf(numNodes);
			FontMetrics fMetrics = g.getFontMetrics();
			Rectangle2D tb = fMetrics.getStringBounds(sNumNodes, g);
			g.drawString(sNumNodes, 2, (int) tb.getHeight() + 2);
		}
	}

	public void anchorEvent(AnchorEvent e) {
		if (e.getType() == AnchorEvent.BEGIN_DRAG) {
			setFlag(RESIZING);
			repaint();
			return;
		}

		if (e.getType() == AnchorEvent.END_DRAG) {
			unsetFlag(RESIZING);
			repaint();
			return;
		}

		if (e.getType() == AnchorEvent.DRAG) {
			Rectangle b = getBounds();
			int nx = b.x + e.getX();
			int ny = b.y + e.getY();
			int nw = b.width + e.getX();
			int nh = b.height + e.getY();
			int cw = b.width - e.getX();
			int ch = b.height - e.getY();

			if (e.getSource() == anchors[0]) {
				setBounds(b.x, ny, b.width, ch);
			}
			if (e.getSource() == anchors[1]) {
				setBounds(b.x, ny, nw, ch);
			}
			if (e.getSource() == anchors[2]) {
				setBounds(b.x, b.y, nw, b.height);
			}
			if (e.getSource() == anchors[3]) {
				setBounds(b.x, b.y, nw, nh);
			}
			if (e.getSource() == anchors[4]) {
				setBounds(b.x, b.y, b.width, nh);
			}
			if (e.getSource() == anchors[5]) {
				setBounds(nx, b.y, cw, nh);
			}
			if (e.getSource() == anchors[6]) {
				setBounds(nx, b.y, cw, b.height);
			}
			if (e.getSource() == anchors[7]) {
				setBounds(nx, ny, cw, ch);
			}
			doLayout();
			NodeComponent nc = getNodeComponent();
			nc.setPropertyValue(GraphView.SIZE, getSize());
			fireGraphComponentEvent(new GraphComponentWrapperEvent(this,
					GraphComponentWrapperEvent.RESIZED));
		}
	}

	public void setFrameVisible(boolean fv) {
		if (!getFlag(SHOW_FRAME_WHEN_FOCUSED)) {
			return;
		}
		if (getNodeComponent().isResizable()) {
			if (anchors != null) {
				for (int i = 0; i < 8; i++) {
					anchors[i].setVisible(fv);
				}
			}
		}
		setFlag(FRAME_VISIBLE, fv);
		repaint();
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2 && getFlag(EDITABLE)) {
			logger.debug("mouse clicked: " + e);
			Boolean exp = (Boolean) getNodeComponent().getPropertyValue(EXPANDED);
			if (exp == null) {
				exp = Boolean.FALSE;
			}
			this.getNodeComponent().setPropertyValue(EXPANDED, Boolean.valueOf(!exp.booleanValue()));
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (!isFocusable()) {
			return;
		}
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			if (!isSelected()) {
				moveToFront();
				requestSelection();
				dxi = (short) e.getX();
				dyi = (short) e.getY();
				return;
			}
			setFlag(MOVING);
			repaint();
		}
		else {
			super.mousePressed(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (!isFocusable()) {
			return;
		}
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			unsetFlag(MOVING);
			repaint();
		}
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		Point p = getLocation();
		int deltaX = e.getX() - dxi;
		int deltaY = e.getY() - dyi;
		setLocation(p.x + deltaX, p.y + deltaY);
		fireGraphComponentEvent(new GraphComponentWrapperEvent(this,
				GraphComponentWrapperEvent.MOVED));
	}

	public int getThickness() {
		return 5;
	}

	public void focusGained(FocusEvent e) {
	}

	public void focusLost(FocusEvent e) {
	}

	public void setHighlighted(boolean highlighted) {
		setFlag(HIGHLIGHTED, highlighted);
		repaint();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() != getNodeComponent()) {
			return;
		}
		logger.debug("Property change - name=" + evt.getPropertyName() + ", old value: "
				+ evt.getOldValue() + ", new value: " + evt.getNewValue());
		if (evt.getPropertyName().equals(EXPANDED)) {
			expanded.setSelectedQuiet(((Boolean) evt.getNewValue()).booleanValue());
			NodeComponent nc = getNodeComponent();
			if (expanded.isSelected()) {
				nc.setPropertyValue("wrapper.normalsize", nc.getPropertyValue(GraphView.SIZE));
			}
			else {
				nc.setPropertyValue("wrapper.expandedsize", nc.getPropertyValue(GraphView.SIZE));
			}
			setUpComponent();
		}
		if (evt.getPropertyName().equals(INTERACTIVE)) {
			Boolean b = (Boolean) evt.getNewValue();
			interactive.setSelectedQuiet(b.booleanValue());
			if (b.booleanValue()) {
				if (isSelected()) {
					setFrameVisible(false);
				}
				setCursor(DEFAULT_CURSOR);
			}
			else {
				if (isSelected()) {
					moveToFront();
					requestSelection();
				}
			}
		}
		if (evt.getPropertyName().equals("name")) {
			if (ec instanceof ScalingContainer) {
				((ScalingContainer) ec).setTitle((String) evt.getNewValue());
				ec.repaint();
			}
		}
		if (evt.getPropertyName().equals(KEEP_ASPECT_RATIO)) {
			if (ec instanceof ScalingContainer) {
				Boolean b = (Boolean) evt.getNewValue();
				keepAspectRatio.setSelectedQuiet(b.booleanValue());
				((ScalingContainer) ec).setKeepAspectRatio(b.booleanValue());
				ec.repaint();
			}
		}
		if (evt.getPropertyName().equals(SHOW_SUBNODES)) {
			showSubNodes.setSelectedQuiet(((Boolean) evt.getNewValue()).booleanValue());
		}
		repaint();
	}

	public NodeComponent getNodeComponent() {
		return (NodeComponent) getGraphComponent();
	}

	public void canvasActionPerformed(CanvasActionEvent e) {
		if (e.getType() == CanvasActionEvent.SELECTED_STATE_CHANGED) {
			if (e.getCanvasAction() == expanded) {
				getNodeComponent().setPropertyValue(EXPANDED,
						Boolean.valueOf(expanded.isSelected()));
				getNodeComponent().getParent().getCanvas().invalidate();
				return;
			}
			if (e.getCanvasAction() == keepAspectRatio) {
				getNodeComponent().setPropertyValue(KEEP_ASPECT_RATIO,
						Boolean.valueOf(keepAspectRatio.isSelected()));
				return;
			}
			if (e.getCanvasAction() == showSubNodes) {
				getNodeComponent().setPropertyValue(SHOW_SUBNODES,
						Boolean.valueOf(showSubNodes.isSelected()));
				return;
			}
			if (e.getCanvasAction() == interactive) {
				getNodeComponent().setPropertyValue(INTERACTIVE,
						Boolean.valueOf(interactive.isSelected()));
				return;
			}
		}
		super.canvasActionPerformed(e);
	}

	public void componentResized(ComponentEvent e) {
		if (e.getSource() == getComponent()) {
			Dimension d = getComponent().getSize();
			Dimension m = getSize();
			d.width += 10;
			d.height += 10;
			if (!m.equals(d)) {
				setSize(d);
				doLayout();
			}
		}
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void graphChanged(GraphChangedEvent e) {
		if ((e.getType() == GraphChangedEvent.NODE_ADDED)
				|| (e.getType() == GraphChangedEvent.NODE_REMOVED)) {
			repaint();
		}
	}

	public void setNodeSize(Dimension d) {
		setSize(new Dimension(d.width + 10, d.height + 10));
		doLayout();
	}

	public Dimension getPreferredNodeSize() {
		validate();
		return getComponent().getPreferredSize();
	}

	public Dimension getNodeSize() {
		validate();
		return getComponent().getSize();
	}

	private boolean isEditable() {
		return (getNodeComponent() instanceof EditableNodeComponent);
	}

	protected void processMouseEvent(MouseEvent e) {
		if (expanded.isSelected() && interactive.isSelected() && isInsideFrame(e.getX(), e.getY())) {
			e.translatePoint(-getComponent().getX(), -getComponent().getY());
			((ScalingContainer) getComponent()).translateAndDispatchMouseEvent(e);
		}
		else if (interactive.isSelected()) {
			if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
				//capture right-clicks
				super.processMouseEvent(e);
			}
			else {
				e.setSource(getRenderer().getVisualComponent());
				getRenderer().getVisualComponent().dispatchEvent(e);
			}
		}
		else {
			super.processMouseEvent(e);
		}
	}

	protected void processMouseMotionEvent(MouseEvent e) {
		if (expanded.isSelected() && interactive.isSelected() && isInsideFrame(e.getX(), e.getY())) {
			if (getFlag(DRAG_CURSOR)) {
				unsetFlag(DRAG_CURSOR);
				setCursor(CROSSHAIR_CURSOR);
			}
			e.translatePoint(-getComponent().getX(), -getComponent().getY());
			((ScalingContainer) getComponent()).translateAndDispatchMouseEvent(e);
		}
		else if (interactive.isSelected()) {
			getComponent().dispatchEvent(e);
		}
		else {
			if (!getFlag(DRAG_CURSOR)) {
				setFlag(DRAG_CURSOR);
				setCursor(MOVE_CURSOR);
			}
			super.processMouseMotionEvent(e);
		}
	}

	protected boolean isInsideFrame(int x, int y) {
		return (x > 10) && (y > 20) && (x < getWidth() - 10) && (y < getHeight() - 10);
	}

	protected void processFocusEvent(FocusEvent e) {
		super.processFocusEvent(e);
		if (!isFocusable()) {
			return;
		}
		if (e.getID() == FocusEvent.FOCUS_GAINED) {
			focusGained(e);
		}
		else if (e.getID() == FocusEvent.FOCUS_LOST) {
			focusLost(e);
		}
	}

	public void dispose() {
		if (getRenderer() != null) {
			getRenderer().dispose();
		}
		if (getComponent() != null) {
			getComponent().removeComponentListener(this);
		}
		if (getComponent() instanceof ScalingContainer) {
			((ScalingContainer) getComponent()).dispose();
		}
		if (getGraphComponent() != null) {
			getGraphComponent().removePropertyChangeListener(this);
		}
		if (canvasRenderer != null) {
			canvasRenderer.dispose();
			canvasRenderer = null;
		}
		super.dispose();
	}

	public void canvasEvent(CanvasEvent e) {
		if (e.getType() == CanvasEvent.INVALIDATE) {
			if (getComponent() instanceof ScalingContainer) {
				((ScalingContainer) getComponent()).repaint();
			}
		}
	}

	public void setUsedAsRenderer(boolean usedAsRenderer) {
		if (isUsedAsRenderer() != usedAsRenderer) {
			if (usedAsRenderer) {
				removeWrapperAction(expanded);
				removeWrapperAction(keepAspectRatio);
				removeWrapperAction(interactive);
				unsetFlag(SHOW_FRAME_WHEN_FOCUSED);
			}
			else {
				addWrapperAction(expanded);
				setFlag(SHOW_FRAME_WHEN_FOCUSED);
			}
		}
		super.setUsedAsRenderer(usedAsRenderer);
	}

	public void setSelected(boolean selected) {
		if (selected == isSelected()) {
			return;
		}
		if (selected) {
			createAnchors();
			setFrameVisible(true);
			setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			repaint();
		}
		else {
			setFrameVisible(false);
			removeAnchors();
			setCursor(Cursor.getDefaultCursor());
			repaint();
		}
		super.setSelected(selected);
	}
}
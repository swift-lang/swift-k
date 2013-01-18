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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import javax.swing.JMenu;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.edges.ControlPoint;
import org.globus.cog.gui.grapheditor.edges.EdgeComponent;
import org.globus.cog.gui.grapheditor.targets.swing.SwingComponentRenderer;
import org.globus.cog.gui.grapheditor.targets.swing.SwingEdge;
import org.globus.cog.gui.grapheditor.targets.swing.SwingEdgeRenderer;
import org.globus.cog.gui.grapheditor.util.RectUtil;
import org.globus.cog.gui.grapheditor.util.swing.ComponentAction;
import org.globus.cog.gui.grapheditor.util.swing.SquareAnchor;

/**
 * Wrapps around edge components to allow for reisizing, moving, etc. Allows for
 * the existence of "control points" on the edge that will be represented
 * graphically by square anchors.
 */
public class EdgeComponentWrapper extends GraphComponentWrapper implements AnchorListener {
	private static Logger logger = Logger.getLogger(EdgeComponentWrapper.class);

	private static Cursor nullCursor;

	protected static final int MOVING = 0x0100;
	protected static final int OVER = 0x0200;

	private short dxi, dyi;

	private Anchor[] anchors;

	private Cursor savedCursor;

	private static int border = 2;

	private JMenu menu;

	public EdgeComponentWrapper(EdgeComponent e) {
		super(e);
		unsetFlag(MOVING);
		unsetFlag(OVER);
		setMovable(false);

		if (nullCursor == null) {
			nullCursor = getToolkit().createCustomCursor(
					new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(0, 0),
					"null cursor");
		}

		if (e.numControlPoints() < 2) {
			throw new RuntimeException("An edge has to have at least two control points");
		}
		enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
		enableEvents(AWTEvent.FOCUS_EVENT_MASK);
	}

	public void setGraphComponent(GraphComponent gc) {
		super.setGraphComponent(gc);
		SwingComponentRenderer renderer = (SwingComponentRenderer) gc.newRenderer("swing");
		setRenderer(renderer);
	}

	private void createAnchors() {
		Anchor[] anchors = new Anchor[getEdgeComponent().numControlPoints() - 2];
		for (int i = 2; i < getEdgeComponent().numControlPoints(); i++) {
			Anchor a = new SquareAnchor(this);
			anchors[i - 2] = a;
			a.setMovable(true);
			a.addAnchorListener(this);
			a.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			add(a);
		}
		this.anchors = anchors;
		doLayout();
	}

	public Dimension getPreferredSize() {
		return getComponent().getPreferredSize();
	}

	public Point getTipCoords() {
		return getEdgeComponent().getControlPoint(0);
	}

	public Point getTailCoords() {
		return getEdgeComponent().getControlPoint(1);
	}

	public void doLayout() {
		Point begin = getEdgeComponent().getControlPoint(0);
		int dx = begin.x - getX();
		int dy = begin.y - getY();
		SwingEdge se = getSwingRenderer().getSwingEdge();
		for (int i = 1; i < getEdgeComponent().numControlPoints() - 1; i++) {
			if (anchors != null) {
				Anchor a = anchors[i - 1];
				a.setHSLocation(se.getPointX(i) + dx, se.getPointY(i) + dy);
			}
		}
		repaint();
	}

	public void paint(Graphics g) {
		if (getAntiAliasing()) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		}
		Graphics gc = g.create();
		//g.setColor(Color.RED);
		//g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
		SwingEdge se = getSwingRenderer().getSwingEdge();
		Rectangle bbox = RectUtil.border(RectUtil.abs(se.getBoundingBox()), border);
		gc.translate(-bbox.x + border, -bbox.y + border);
		//gc.setColor(Color.GREEN);
		//gc.drawRect(bbox.x, bbox.y, bbox.width-1, bbox.height-1);
		//gc.drawRect(0, 0, getComponent().getWidth() - 1,
		// getComponent().getHeight() - 1);
		getComponent().paint(gc);
		//gc.setColor(Color.RED);
		//gc.drawLine(-2, 0, 2, 0);
		//gc.drawLine(0, -2, 0, 2);
		super.paint(g);
	}

	public void anchorEvent(AnchorEvent e) {
		if (e.getType() == AnchorEvent.BEGIN_DRAG) {
			savedCursor = ((Component) e.getSource()).getCursor();
			((Component) e.getSource()).setCursor(nullCursor);
			repaint();
			return;
		}

		if (e.getType() == AnchorEvent.END_DRAG) {
			((Component) e.getSource()).setCursor(savedCursor);
			repaint();
			return;
		}

		if (e.getType() == AnchorEvent.DRAG) {
			int dx = e.getX();
			int dy = e.getY();
			int index = -1;
			for (int i = 0; i < anchors.length; i++) {
				if (anchors[i] == e.getSource()) {
					index = i;
				}
			}
			Point begin = getEdgeComponent().getControlPoint(0);
			Point end = getEdgeComponent().getControlPoint(1);
			int xo = Math.min(begin.x, end.x);
			int yo = Math.min(begin.y, end.y);
			if (index != -1) {
				index += 2;
				SwingEdge se = getSwingRenderer().getSwingEdge();
				ControlPoint p = getEdgeComponent().getControlPoint(index);
				int x = dx + p.x;
				int y = dy + p.y;
				getEdgeComponent().updateControlPoint(index, x, y);
				

				Rectangle bbox = RectUtil.abs(RectUtil.border(se.getBoundingBox(), 2));
				setLocation(begin.x + bbox.x - border, begin.y + bbox.y - border);
				setSize(Math.abs(bbox.width) + 2 * border, Math.abs(bbox.height) + 2 * border);

				doLayout();
			}
		}
	}

	public void setFrameVisible(boolean fv) {
		if (anchors != null) {
			for (int i = 0; i < anchors.length; i++) {
				anchors[i].setVisible(fv);
			}
			repaint();
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public boolean isFocusable() {
		return true;
	}

	public void mousePressed(MouseEvent e) {
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			if (!contains(e.getX(), e.getY())) {
				return;
			}
			if (!isSelected()) {
				moveToFront();
				requestSelection();
				return;
			}
			if (isMovable()) {
				setFlag(MOVING);
				dxi = (short) e.getX();
				dyi = (short) e.getY();
				repaint();
			}
		}
		else {
			super.mousePressed(e);
		}
	}

	private void createMenu() {
		menu = new JMenu();
		Iterator i = getRenderer().getActions().iterator();
		while (i.hasNext()) {
			menu.add(((ComponentAction) i.next()).createComponent());
		}
	}

	public void mouseReleased(MouseEvent e) {
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			unsetFlag(MOVING);
			repaint();
		}
	}

	public void mouseMoved(MouseEvent e) {
		if (contains(e.getX(), e.getY())) {
			if (!getFlag(OVER)) {
				setFlag(OVER);
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			}
		}
		else {
			if (getFlag(OVER)) {
				unsetFlag(OVER);
				setCursor(Cursor.getDefaultCursor());
			}
		}
	}

	public boolean contains(int x, int y) {
		if (getSwingEdge() == null) {
			return false;
		}
		if (getSwingEdge().edgeContains(x - border, y - border)) {
			return true;
		}
		if (anchors != null) {
			for (int i = 0; i < anchors.length; i++) {
				Anchor a = anchors[i];
				if (a.contains(x - a.getX(), y - a.getY())) {
					return true;
				}
			}
		}
		return false;
	}

	public void mouseDragged(MouseEvent e) {
		if (!getFlag(MOVING)) {
			return;
		}
		Point p = getLocation();
		int deltaX = e.getX() - dxi;
		int deltaY = e.getY() - dyi;
		setLocation(p.x + deltaX, p.y + deltaY);
	}

	public int getThickness() {
		return 5;
	}

	public void focusGained(FocusEvent e) {
	}

	public void focusLost(FocusEvent e) {
	}

	public void setCoords(int x1, int y1, int x2, int y2) {
		EdgeComponent ec = getEdgeComponent();
		ec.updateControlPoint(0, x1, y1);
		ec.updateControlPoint(1, x2, y2);
		SwingEdge se = getSwingRenderer().getSwingEdge();
		se.setPoint(0, x2 - x1, y2 - y1);

		Rectangle bbox = RectUtil.abs(RectUtil.border(se.getBoundingBox(), 2));
		setLocation(x1 + bbox.x - border, y1 + bbox.y - border);
		setSize(Math.abs(bbox.width) + 2 * border, Math.abs(bbox.height) + 2 * border);
		doLayout();
	}

	public EdgeComponent getEdgeComponent() {
		return (EdgeComponent) getGraphComponent();
	}

	protected void processMouseMotionEvent(MouseEvent e) {
		super.processMouseMotionEvent(e);
		if (e.getID() == MouseEvent.MOUSE_MOVED) {
			mouseMoved(e);
		}
		else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
			mouseDragged(e);
		}
		else if (e.getID() == MouseEvent.MOUSE_ENTERED) {
			mouseEntered(e);
		}
		else if (e.getID() == MouseEvent.MOUSE_EXITED) {
			mouseExited(e);
		}
	}

	protected void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);
		if (e.getID() == MouseEvent.MOUSE_CLICKED) {
			mouseClicked(e);
		}
		else if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			mousePressed(e);
		}
		else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
			mouseReleased(e);
		}
	}

	protected void processFocusEvent(FocusEvent e) {
		super.processFocusEvent(e);
		if (e.getID() == FocusEvent.FOCUS_GAINED) {
			focusGained(e);
		}
		else if (e.getID() == FocusEvent.FOCUS_LOST) {
			focusLost(e);
		}
	}

	public SwingEdge getSwingEdge() {
		return (SwingEdge) getComponent();
	}

	public SwingEdgeRenderer getSwingRenderer() {
		return (SwingEdgeRenderer) getRenderer();
	}

	private void removeAnchors() {
		if (anchors != null) {
			for (int i = 0; i < anchors.length; i++) {
				anchors[i].removeAnchorListener(this);
				remove(anchors[i]);
			}
		}
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
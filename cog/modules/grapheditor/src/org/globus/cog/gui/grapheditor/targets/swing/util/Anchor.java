
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.targets.swing.util;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLayeredPane;

import org.apache.log4j.Logger;

/**
 * An anchor is a component used as visual guides for manipulating the
 * dimensions of other components.
 */
public class Anchor extends Component {
	private static Logger logger = Logger.getLogger(Anchor.class);

	public static short NONE = 0;
	public static short N = 1;
	public static short S = 2;
	public static short E = 4;
	public static short W = 8;
	public static short NW = (short) (N + W);
	public static short NE = (short) (N + E);
	public static short SW = (short) (S + W);
	public static short SE = (short) (S + E);
	public static short BEGIN = 16;
	public static short END = 17;

	private List AnchorListeners;
	private short type;
	private static int size = 5;
	private int savedLayer;

	public Anchor(GraphComponentWrapper owner, short type) {
		AnchorListeners = new ArrayList(0);
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
		setSize(size, size);
		setVisible(false);
		this.type = type;
	}
	
	public void setMovable(boolean movable) {
		if (movable) {
			enableEvents(AWTEvent.MOUSE_EVENT_MASK);
			enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
		}
		else {
			disableEvents(AWTEvent.MOUSE_EVENT_MASK);
			disableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
		}
	}

	public Anchor(GraphComponentWrapper owner) {
		this(owner, NONE);
	}

	public void addAnchorListener(AnchorListener l) {
		AnchorListeners.add(l);
	}

	public void removeAnchorListener(AnchorListener l) {
		AnchorListeners.remove(l);
	}

	public void dispatchAnchorEvent(AnchorEvent e) {
		Iterator i = AnchorListeners.listIterator();
		while (i.hasNext()) {
			((AnchorListener) i.next()).anchorEvent(e);
		}
	}

	public void dispatchAnchorEvent(AnchorEvent e, Object exclude) {
		Iterator i = AnchorListeners.listIterator();
		while (i.hasNext()) {
			AnchorListener l = (AnchorListener) i.next();
			if (l == exclude) {
				continue;
			}
			l.anchorEvent(e);
		}
	}

	public Dimension getPreferredSize() {
		return new Dimension(size, size);
	}

	public void setHSLocation(Point p) {
		setHSLocation(p.x, p.y);
	}

	public void setHSLocation(int x, int y) {
		setLocation(x - getSize().width / 2, y - getSize().height / 2);
	}

	public Point getHSLocation() {
		return new Point(
			getLocation().x + getSize().width / 2,
			getLocation().y + getSize().height / 2);
	}

	public void setSize(int w, int h) {
		size = (w + h) / 2;
		super.setSize(size, size);
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			dispatchAnchorEvent(new AnchorEvent(this, AnchorEvent.BEGIN_DRAG));
			if (getParent() instanceof JLayeredPane) {
				JLayeredPane layeredPane = (JLayeredPane) getParent();
				savedLayer = layeredPane.getLayer(this);
				layeredPane.setLayer(this, JLayeredPane.DRAG_LAYER.intValue());
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			dispatchAnchorEvent(new AnchorEvent(this, AnchorEvent.END_DRAG));
			if (getParent() instanceof JLayeredPane) {
				JLayeredPane layeredPane = (JLayeredPane) getParent();
				layeredPane.setLayer(this, savedLayer);
			}
		}
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		AnchorEvent ae = new AnchorEvent(this, AnchorEvent.DRAG);
		ae.setCoords(e.getX() - 3, e.getY() - 3);
		dispatchAnchorEvent(ae);
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

	public void setType(short type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public GraphComponentWrapper getOwner() {
		//return owner;
		return null;
	}

	public void translate(int dx, int dy) {
		Point p = getLocation();
		setLocation(p.x + dx, p.y + dy);
	}
}

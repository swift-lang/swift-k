/*
 * Created on Jun 14, 2004
 */
package org.globus.cog.gui.grapheditor.util.swing;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;


public class MouseEventDispatcher {
	public static void dispatchMouseEvent(MouseEvent e, MouseListener l) {
		if (e.getID() == MouseEvent.MOUSE_CLICKED) {
			l.mouseClicked(e);
		}
		else if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			l.mousePressed(e);
		}
		else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
			l.mouseReleased(e);
		}
		else if (e.getID() == MouseEvent.MOUSE_ENTERED) {
			l.mouseEntered(e);
		}
		else if (e.getID() == MouseEvent.MOUSE_EXITED) {
			l.mouseExited(e);
		}
	}
	
	public static void dispatchMouseMotionEvent(MouseEvent e, MouseMotionListener l) {
		if (e.getID() == MouseEvent.MOUSE_MOVED) {
			l.mouseMoved(e);
		}
		else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
			l.mouseDragged(e);
		}
	}
}

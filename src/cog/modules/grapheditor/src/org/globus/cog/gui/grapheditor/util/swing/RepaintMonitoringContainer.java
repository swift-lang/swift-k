
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 24, 2004
 */
package org.globus.cog.gui.grapheditor.util.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.PaintEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class RepaintMonitoringContainer extends Container implements MouseListener{
	private static Logger logger = Logger.getLogger(RepaintMonitoringContainer.class);
	private List repaintListeners;
	
	public RepaintMonitoringContainer() {
		repaintListeners = Collections.synchronizedList(new LinkedList());
		setLayout(new BorderLayout());
		addMouseListener(this);
	}
	
	public boolean isShowing() {
		return true;
	}

	public void repaint(long tm, int x, int y, int w, int h) {
		fireRepaintEvent(new Rectangle(x, y, w, h));
		super.repaint(tm, x, y, w, h);
	}
	
	public void addRepaintListener(RepaintListener l) {
		synchronized (repaintListeners) {
			repaintListeners.add(l);
		}
	}

	public void removeRepaintListener(RepaintListener l) {
		synchronized (repaintListeners) {
			repaintListeners.remove(l);
		}
	}

	public void fireRepaintEvent(Rectangle r) {
		if (repaintListeners == null) {
			return;
		}
		PaintEvent pe = new PaintEvent(this, PaintEvent.UPDATE, r);
		synchronized (repaintListeners) {
			Iterator i = repaintListeners.iterator();
			while (i.hasNext()) {
				((RepaintListener) i.next()).repaint(pe);
			}
		}
	}
	
	public void mouseClicked(MouseEvent e) {
		logger.debug(this +" - mouse clicked at " + e.getX() + ", " + e.getY());
	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}
	
}

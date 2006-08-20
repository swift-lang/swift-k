
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


/*
 * Created on Aug 26, 2003
 */
package org.globus.cog.gui.grapheditor.targets.swing.util;


import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.globus.cog.gui.grapheditor.targets.swing.CanvasPanel;
import org.globus.cog.gui.grapheditor.targets.swing.views.GraphView;

public class OverviewFrame extends JFrame
	implements
		ChangeListener,
		MouseListener,
		MouseMotionListener,
		ComponentListener {
	private OverviewContainer container;

	private CanvasPanel panel;

	public OverviewFrame(CanvasPanel panel, GraphView view) {
		this.panel = panel;
		container = new OverviewContainer(view);
		container.setKeepAspectRatio(true);
		container.setEventsEnabled(false);
		container.setVisibleArea(panel.getVisibleArea());
		container.addMouseListener(this);
		container.addMouseMotionListener(this);
		container.removeTitle();
		panel.getViewport().addChangeListener(this);
		setTitle("Overview");
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(container, BorderLayout.CENTER);
		addComponentListener(this);
		container.repaint();
	}

	public void stateChanged(ChangeEvent e) {
		container.setVisibleArea(panel.getVisibleArea());
		container.repaintImmediately();
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		Point p = container.realToScaled(e.getX(), e.getY());
		if (p.x < container.minX()) {
			p.x = container.minX();
		}
		if (p.x > container.maxX()) {
			p.x = container.maxX();
		}
		if (p.y < container.minY()) {
			p.y = container.minY();
		}
		if (p.y > container.maxY()) {
			p.y = container.maxY();
		}
		panel.setVisibleAreaOrigin(p);
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		Point p = container.realToScaled(e.getX(), e.getY());
		if (p.x < container.minX()) {
			p.x = container.minX();
		}
		if (p.x > container.maxX()) {
			p.x = container.maxX();
		}
		if (p.y < container.minY()) {
			p.y = container.minY();
		}
		if (p.y > container.maxY()) {
			p.y = container.maxY();
		}
		panel.setVisibleAreaOrigin(p);
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void dispose() {
		container.dispose();
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentResized(ComponentEvent e) {
		container.clear();
	}

	public void componentShown(ComponentEvent e) {
	}
}


// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.targets.swing;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.globus.cog.gui.grapheditor.canvas.CanvasRenderer;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.generic.RootNode;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;

/**
 * This is an example of a frame. It works with an Ant project.
 */
public class CanvasFrame
	extends JFrame
	implements Runnable, PropertyChangeListener, ComponentListener, WindowListener {
	private NodeComponent node;
	public static volatile int activeFrames = 0;
	private SwingCanvasRenderer canvasRenderer;
	private boolean autoTitle;
    private LogWindow log;

	public CanvasFrame() {
		autoTitle = true;
        log = new LogWindow();
        log.attach(this);
	}

	public CanvasFrame(NodeComponent p) {
        this();
		setNode(p);
	}

	public NodeComponent getNode() {
		return node;
	}
    

	public LogWindow getLog() {
		return log;
	}

	public void setNode(NodeComponent p) {
		this.node = p;
		GraphCanvas c = p.getCanvas();
		if (c == null) {
			c = p.createCanvas();
		}
		canvasRenderer = (SwingCanvasRenderer) c.newRenderer("swing");
        canvasRenderer.setLogWindow(log);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		getContentPane().add(canvasRenderer.getComponent());
		if (node.hasProperty("framesize")) {
			setSize((Dimension) node.getPropertyValue("framesize"));
		}
		else {
			setSize(640, 400);
		}
		if (node.hasProperty("framelocation")) {
			Point l = (Point) node.getPropertyValue("framelocation");
			setLocation(l.x, l.y);
		}
		if (p.hasProperty("name")) {
			setTitle((String) p.getPropertyValue("name"));
		}
		p.addPropertyChangeListener(this);
		addComponentListener(this);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(this);
		activeFrames++;
	}

	public void setTitle(String name) {
		if (autoTitle) {
			if (name == null) {
				super.setTitle(
					node.getComponentType() + " Canvas - Java CoG Kit Graph Viewer");
			}
			else {
				super.setTitle(
					name
						+ " - "
						+ node.getComponentType()
						+ " Canvas - Java CoG Kit Graph Viewer");
			}
		}
		else {
			super.setTitle(name);
		}
	}

	public JFrame getFrame() {
		return this;
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getSource() == node) {
			if (e.getPropertyName().equals("name")) {
				setTitle((String) e.getNewValue());
			}
			else if (e.getPropertyName().equals("framesize")) {
				removeComponentListener(this);
				setSize((Dimension) node.getPropertyValue("framesize"));
				addComponentListener(this);
			}
			else if (e.getPropertyName().equals("framelocation")) {
				removeComponentListener(this);
				Point l = (Point) node.getPropertyValue("framelocation");
				setLocation(l.x, l.y);
				addComponentListener(this);
			}
		}
	}
	
	public void activate() {
		canvasRenderer.initialize();
		setVisible(true);
	}

	public void run() {
		activate();
		while (activeFrames > 0) {
			try {
				Thread.sleep(250);
			}
			catch (InterruptedException e) {
			}
		}
		dispose();
	}

	public static void main(String[] args) {
		CanvasFrame cf = new CanvasFrame(new RootNode());
		cf.run();
		System.exit(0);
	}

	public void componentResized(ComponentEvent e) {
		Dimension d = getSize();
		node.setPropertyValue("framesize", d);
	}

	public void componentMoved(ComponentEvent e) {
		Point p = getLocation();
		node.setPropertyValue("framelocation", p);
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		activeFrames--;
		canvasRenderer.dispose();
		setVisible(false);
		dispose();
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public SwingCanvasRenderer getSwingCanvasRenderer() {
		return canvasRenderer;
	}

	public CanvasRenderer getCanvasRenderer() {
		return canvasRenderer;
	}

	public void setCanvasRenderer(SwingCanvasRenderer canvasRenderer) {
		this.canvasRenderer = canvasRenderer;
	}

	public GraphCanvas getCanvas() {
		return getSwingCanvasRenderer().getCanvas();
	}
	public boolean isAutoTitle() {
		return autoTitle;
	}

	public void setAutoTitle(boolean autoTitle) {
		this.autoTitle = autoTitle;
	}

}


// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.nodes;


import org.globus.cog.gui.grapheditor.AbstractGraphComponent;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.edges.EdgeComponent;
import org.globus.cog.gui.grapheditor.properties.DelegatedClassProperty;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.gui.grapheditor.properties.PropertyHolder;

/**
 * Implements the basic functionality for a node component
 */
public abstract class AbstractNodeComponent extends AbstractGraphComponent
	implements
		NodeComponent,
		PropertyHolder {

	private GraphCanvas canvas;

	private Class canvasType;

	private boolean resizable;

	static {
		addClassProperty(new DelegatedClassProperty(
			AbstractNodeComponent.class, "nodeid", "_ID", Property.RW));
	}

	public AbstractNodeComponent() {
		setComponentType("-");
		setResizable(true);
	}

	/**
	 * Returns a canvas for this node, if it is supported. If a canvas has not
	 * already been defined for this node, the getCanvasType() method is
	 * called, and a new canvas is instantiated. This form of lazy creation is
	 * used in order to allow generic nodes that can be generated recursively,
	 * without entering an infinite loop at initialization.
	 * 
	 * @return An existing canvas, a new canvas, or null if the node does not
	 *         support a canvas.
	 */
	public GraphCanvas getCanvas() {
		return canvas;
	}

	public GraphCanvas createCanvas() {
		if (canvasType != null) {
			try {
				canvas = (GraphCanvas) canvasType.newInstance();
				canvas.setOwner(this);
				return canvas;
			}
			catch (InstantiationException e) {
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void setCanvas(GraphCanvas canvas) {
		this.canvas = canvas;
	}

	public boolean acceptsInEdgeConnection(EdgeComponent edge) {
		return true;
	}

	public boolean acceptsOutEdgeConnection(EdgeComponent edge) {
		return true;
	}

	public Class getCanvasType() {
		return canvasType;
	}

	public void setCanvasType(Class canvasType) {
		this.canvasType = canvasType;
	}

	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	public boolean isResizable() {
		return resizable;
	}
}

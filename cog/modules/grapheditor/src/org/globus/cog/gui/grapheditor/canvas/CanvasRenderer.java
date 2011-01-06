
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 21, 2004
 */
package org.globus.cog.gui.grapheditor.canvas;

import java.awt.Dimension;

import org.globus.cog.gui.grapheditor.RootContainer;
import org.globus.cog.gui.grapheditor.canvas.views.CanvasView;

public interface CanvasRenderer extends CanvasEventListener{

	/**
	 * Binds this renderer to a canvas
	 */
	public void setCanvas(GraphCanvas canvas);
	
	/**
	 * Returns the canvas that this renderer renders
	 */
	public GraphCanvas getCanvas();

	public void setSize(Dimension dimension);
	
	/**
	 * Sets the active view for the canvas
	 * @param View
	 */
	public void setView(CanvasView View);

	/**
	 * @return the active view for the canvas
	 */
	public CanvasView getView();

	/**
	 *
	 * @return a list with the views supported by this canvas
	 */
	public java.util.List getSupportedViews();
	
	public void setRootContainer(RootContainer rootContainer);
	
	public RootContainer getRootContainer();
	
	/**
	 * For cleanup purposes
	 */
	public void dispose();
}

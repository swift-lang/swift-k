
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.canvas.views;

import java.awt.Rectangle;

import org.globus.cog.gui.grapheditor.canvas.CanvasRenderer;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.canvas.transformation.GraphTransformation;
import org.globus.cog.util.graph.GraphInterface;
/**
 * This interface defines methods that should be implemented by
 * views for a canvas
 */
public interface CanvasView{
    /**
     * Sets the canvas to which this view belongs
     * @param graph
     */
    public void setRenderer(CanvasRenderer renderer);
    
    /**
     * Sets the transformation. The said transformation is applied before
     * rendering.
     */
    public void setTransformation(GraphTransformation transformation);
	
	/**
	 * Adds a transformation to the chain of transformations. The transformations
	 * will be applied successively, in REVERSE order: Last added first.
	 */
	public void addTransformation(GraphTransformation transformation);

    public CanvasRenderer getRenderer();
    
    public GraphInterface getGraph();
    
    public void setGraph(GraphInterface graph);

    public CanvasView getNewInstance(GraphCanvas canvas);

    /**
     * Forces the view to reevaluate its internal state. This method
     * is called whenever the structure of the graph changes
     */
    public void invalidate();
    
    public void reLayout();

    public String getName();
    
    /**
     * Cleans up this view. This method should be called when
     * a view is deactivated. The view can be reused later, by 
     * invalidating it.
     *
     */
    public void clean();
    
    public void activate();
    
    public void setViewport(Rectangle r);
    
    public Rectangle getViewport();
    
    public boolean isSelective();
}

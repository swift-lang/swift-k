
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
/*
 * Created on Jun 24, 2003
 */
package org.globus.cog.gui.grapheditor;


/**
 * Defines an interface for a component renderer. A renderer is
 * responsible with displaying the node/edge and interpreting their
 * properties in a visual/behavioral fashion.
 */
public interface ComponentRenderer {
	/**
	 * Sets the component that this renderer is supposed to represent
	 * @param comp
	 */
	public void setComponent(GraphComponent comp);
	
	/**
	 * Returns the component that this renderer represents
	 * @return
	 */
	public GraphComponent getComponent();
	
	/**
	 * For cleanup purposes. Removes external references to the renderer so that
	 * it can be garbage collected.
	 *
	 */
	public void dispose();
}

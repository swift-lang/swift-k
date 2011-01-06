
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor;

import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.properties.PropertyHolder;

/**
 * Interface for a generic component (node or edge) that can be used by the
 * editor
 */

public interface GraphComponent extends PropertyHolder, Cloneable{

    /**
     * Gets the type of this component. The type can be used to differentiate
     * components from each other.
     * @return A string representing the type
     */
    public String getComponentType();

    /**
     * Sets the type of this component
     * @param name
     */
    public void setComponentType(String name);

    /**
     * Creates a renderer for this component using the current target. 
     */
    public ComponentRenderer newRenderer();
    
    /**
     * Creates a renderer for this components using the specified target. 
     */
    public ComponentRenderer newRenderer(String target);

    /**
     * Creates a new component using the current one as prototype.
     */
    public GraphComponent newInstance();

    /**
     * @return The parent node in the hierarchical graph
     */
    public NodeComponent getParent();

    /**
     * Sets the parent node in the hierarchical graph
     */
    public void setParent(NodeComponent parent);

    /**
     * This method is used to allow the existence of generic objects which
     * can render a range of types.
     * @param type The type to be checked
     * @return true if this component can render the specified type
     */
    boolean supportsType(String type);
	
	/**
	 * Sets the id of this component. An id can be used to uniquely reference a 
	 * component. 
	 */
	public void set_ID(String id);
	
	/**
	 * Returns the id of this component
	 */
	public String get_ID();
	
	public NodeComponent getRootNode();
	
	public Object clone();
}
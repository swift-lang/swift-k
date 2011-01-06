
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.generic;

import org.globus.cog.gui.grapheditor.edges.SimpleArrow;
import org.globus.cog.gui.grapheditor.properties.ComponentClassProperty;
import org.globus.cog.gui.grapheditor.properties.Property;

public class GenericEdge extends SimpleArrow {
	
	static {
		addClassProperty(new ComponentClassProperty(GenericEdge.class, "_ID", Property.RWH));
	}
	
	public GenericEdge() {
		setComponentType("edge");
	}
}

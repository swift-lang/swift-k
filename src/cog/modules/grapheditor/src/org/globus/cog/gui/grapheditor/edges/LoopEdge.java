
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.edges;

import java.awt.Point;

import org.globus.cog.gui.grapheditor.properties.ComponentProperty;
import org.globus.cog.gui.grapheditor.properties.OverlayedProperty;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.util.ImageLoader;

/**
 * A rectangular edge made out of three lines.
 */
public class LoopEdge extends AbstractEdgeComponent{
	private ControlPoint split;
	
	public LoopEdge() {
	    super();
	    setClassRendererClass(LoopEdgeRenderer.class);
	    setComponentType("arrow");
	    addProperty(new OverlayedProperty(this, "icon", Property.HIDDEN));
	    getProperty("icon").setValue(ImageLoader.loadIcon("images/icon-arrow.png"));
		split = new ControlPoint(0, 0);
		addProperty(new ComponentProperty(this, "split", Property.HIDDEN));
		addControlPoint(split);
	}

	
	
	public void setSplit(Point split){
		split = new ControlPoint(split);
	}
	
	public Point getSplit(){
		return new Point(split.x, split.y);
	}
	
}


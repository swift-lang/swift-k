
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.edges;

import java.awt.Color;

import javax.swing.Icon;

import org.globus.cog.gui.grapheditor.properties.ComponentClassProperty;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.util.ImageLoader;

/**
 * This class implements a simple edge component that renders an arrow
 */

public class SimpleArrow extends AbstractEdgeComponent {
	private static Icon ICON = ImageLoader.loadIcon("images/16x16/co/arrow.png");

	private Icon icon;
	private Color color;
	
	static {
		addClassProperty(new ComponentClassProperty(SimpleArrow.class, "icon", Property.RWH+Property.NONPERSISTENT));
		addClassProperty(new ComponentClassProperty(SimpleArrow.class, "color", Property.RW));
		setClassRendererClass(SimpleArrow.class, SimpleArrowRenderer.class);
	}

	public SimpleArrow() {
		super();
		setComponentType("arrow");
		setIcon(ICON);
		setColor(Color.BLACK);
	}

	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public void loadIcon(String name) {
		ImageLoader il = new ImageLoader();
		setIcon(il.loadImage(name));
	}
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

}

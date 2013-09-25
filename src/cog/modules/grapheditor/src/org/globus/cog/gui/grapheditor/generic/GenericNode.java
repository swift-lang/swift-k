
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.generic;


import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.Icon;

import org.globus.cog.gui.grapheditor.nodes.EditableNodeComponent;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.properties.ComponentClassProperty;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.gui.grapheditor.targets.swing.SwingNodeRenderer;
import org.globus.cog.gui.grapheditor.targets.swing.util.ScalableRenderer;
import org.globus.cog.util.ImageLoader;

/**
 * A generic node with a generic canvas
 */
public class GenericNode extends EditableNodeComponent
	implements
		NodeComponent,
		ScalableRenderer {
	private static Icon ICON = ImageLoader.loadIcon("images/16x16/co/task.png");

	public static final int STATUS_STOPPED = 0;

	public static final int STATUS_RUNNING = 1;

	public static final int STATUS_FAILED = 2;

	public static final int STATUS_COMPLETED = 3;
	
	public static final Integer STOPPED = new Integer(STATUS_STOPPED);
	
	public static final Integer RUNNING = new Integer(STATUS_RUNNING);
	
	public static final Integer FAILED = new Integer(STATUS_FAILED);
	
	public static final Integer COMPLETED = new Integer(STATUS_COMPLETED);

	private float hue;

	private float saturation;

	private float value;

	private int status;

	private String iconfile;

	private String name;

	private Icon icon;

	static {
		addClassProperty(new ComponentClassProperty(GenericNode.class, "hue",
			Property.RW));
		addClassProperty(new ComponentClassProperty(GenericNode.class,
			"saturation", Property.RW));
		addClassProperty(new ComponentClassProperty(GenericNode.class, "value",
			Property.RW));
		addClassProperty(new ComponentClassProperty(GenericNode.class,
			"status", Property.RW));
		addClassProperty(new ComponentClassProperty(GenericNode.class,
			"iconfile", Property.RW));
		addClassProperty(new ComponentClassProperty(GenericNode.class, "name"));
		addClassProperty(new ComponentClassProperty(GenericNode.class, "icon",
			(short) (Property.NONPERSISTENT + Property.RWH)));
		setClassRendererClass(GenericNode.class, SwingNodeRenderer.class);
	}

	public GenericNode() {
		//loads the icon
		setIcon(ICON);
		//set the canvas type
		setCanvasType(GenericCanvas.class);
		//set the type
		setComponentType("node");
		//set the component
		hue = (float) 0.0;
		saturation = (float) 1.0;
		value = (float) 1.0;
	}

	public boolean supportsType(String type) {
		return true;
	}

	public void setStatus(Integer status) {
		this.status = status.intValue();
	}

	public Integer getStatus() {
		return new Integer(status);
	}

	public void setIconfile(String iconfile) {
		this.iconfile = iconfile;
		if (iconfile != null) {
			setIcon(ImageLoader.loadIcon(iconfile));
		}
	}

	public String getIconfile() {
		return iconfile;
	}

	public void setHue(Float hue) {
		this.hue = hue.floatValue();
	}

	public Float getHue() {
		return new Float(hue);
	}

	public void setSaturation(Float saturation) {
		this.saturation = saturation.floatValue();
	}

	public Float getSaturation() {
		return new Float(saturation);
	}

	public void setValue(Float value) {
		this.value = value.floatValue();
	}

	public Float getValue() {
		return new Float(value);
	}

	public String getName() {
		return name;
	}

	public void setName(String string) {
		this.name = string;
	}

	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public void paint(Graphics2D g, int x, int y, int w, int h) {
		Color c = g.getColor();
		switch (status) {
			case STATUS_STOPPED :
				g.setColor(Color.BLACK);
				break;
			case STATUS_RUNNING :
				g.setColor(Color.BLUE);
				break;
			case STATUS_COMPLETED :
				g.setColor(Color.GREEN);
				break;
			case STATUS_FAILED :
				g.setColor(Color.RED);
				break;
		}
		g.fillRect(x, y, w, h);
		g.setColor(c);
	}
}

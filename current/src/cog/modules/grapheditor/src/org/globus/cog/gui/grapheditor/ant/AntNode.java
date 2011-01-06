
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.ant;

import javax.swing.Icon;

import org.globus.cog.gui.grapheditor.nodes.EditableNodeComponent;
import org.globus.cog.gui.grapheditor.properties.ComponentProperty;
import org.globus.cog.gui.grapheditor.properties.DelegatedProperty;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.util.ImageLoader;

/**
 * This is the root class for Ant nodes
 */
public abstract class AntNode extends EditableNodeComponent {
	public static int STATUS_STOPPED = 0;
	public static int STATUS_RUNNING = 1;
	public static int STATUS_FAILED = 2;
	public static int STATUS_COMPLETED = 3;

	private String name;
	private Icon icon;
	private int status;

	public AntNode() {
		//loads the icon
		setIcon(ImageLoader.loadIcon("images/16x16/co/task.png"));
		//set the canvas type
		setClassRendererClass(AntRenderer.class);
		setCanvasType(TaskCanvas.class);
		//set the type
		setComponentType("defaultNode");
		//set the component
		Property nodeid = getProperty("nodeid");
		if (nodeid != null){
			nodeid.setAccess((short)(Property.NONPERSISTENT+Property.R+Property.HIDDEN));
		}
		addProperty(new ComponentProperty(this, "status", Property.RWH));
		addProperty(new DelegatedProperty(this, "sstatus", "status", "statusAsText",(short)( Property.R+Property.NONPERSISTENT)));
		//addProperty(new ComponentProperty(this, "name"));
		addProperty(new ComponentProperty(this, "icon", (short)(Property.HIDDEN + Property.R+Property.NONPERSISTENT)));
	}

	public boolean supportsType(String type) {
		return getComponentType().equals(type);
	}

	public void setStatus(Integer status) {
		this.status = status.intValue();
		firePropertyChange("status", null, getStatusAsText());
	}

	public Integer getStatus() {
		return new Integer(status);
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

	public String getStatusAsText() {
		int status = getStatus().intValue();
		if (status == STATUS_COMPLETED) {
			return "Completed";
		}
		if (status == STATUS_FAILED) {
			return "Failed";
		}
		if (status == STATUS_RUNNING) {
			return "Running";
		}
		if (status == STATUS_STOPPED) {
			return "Stopped";
		}
		return "Unknown";
	}

	public void loadIcon(String iconFile) {
		setIcon(ImageLoader.loadIcon(iconFile));
	}
}

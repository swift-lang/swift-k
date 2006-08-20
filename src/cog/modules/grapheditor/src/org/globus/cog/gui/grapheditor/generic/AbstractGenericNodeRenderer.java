
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Feb 3, 2004
 *
 */
package org.globus.cog.gui.grapheditor.generic;


import java.awt.Image;
import java.beans.PropertyChangeEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.AbstractRenderer;
import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.gui.grapheditor.util.graphics.ImageProcessor;

public abstract class AbstractGenericNodeRenderer extends AbstractRenderer {
	private static Logger logger = Logger.getLogger(AbstractGenericNodeRenderer.class);

	private Image image, adjusted;

	private ImageIcon running, stopped, failed, completed, crt;

	private String label;

	public void setComponent(GraphComponent component) {
		super.setComponent(component);
		if (component.hasProperty("name")) {
			label = (String) component.getPropertyValue("name");
		}
		else {
			label = component.getComponentType();
		}
		if (label == null) {
			label = "";
		}
		resetIcon();
		updateIcon();
	}

	public void updateStatus() {
		if (getComponent().hasProperty("status")) {
			int status = ((Integer) getComponent().getPropertyValue("status")).intValue();
			setStatus(status);
		}
	}

	public void setStatus(int status) {
		if (status == GenericNode.STATUS_RUNNING) {
			setIcon(getRunningIcon());
		}
		if (status == GenericNode.STATUS_STOPPED) {
			setIcon(getStoppedIcon());
		}
		if (status == GenericNode.STATUS_FAILED) {
			setIcon(getFailedIcon());
		}
		if (status == GenericNode.STATUS_COMPLETED) {
			setIcon(getCompletedIcon());
		}
	}

	public void resetIcon() {
		try {
			Icon icon = (Icon) getComponent().getPropertyValue("icon");
			image = null;
			if (icon instanceof ImageIcon) {
				ImageIcon ii = (ImageIcon) icon;
				image = ii.getImage();
				adjusted = image;
				stopped = null;
				running = null;
				failed = null;
				completed = null;
			}
		}
		catch (Exception e) {
			logger.warn("Something went wrong while loading the icons:");
			logger.warn(e);
		}
	}

	protected void updateIcon() {
		if (image == null) {
			return;
		}
		adjusted = ImageProcessor.adjustHSV(image, getNode().getHue().doubleValue(), getNode()
			.getSaturation().doubleValue(), getNode().getValue().doubleValue());
		stopped = null;
		running = null;
		failed = null;
		completed = null;
		updateStatus();
	}

	private ImageIcon getRunningIcon() {
		if (running == null) {
			if (image == null) {
				return null;
			}
			running = StatusIconHelper.makeRunningIcon(adjusted);
		}
		return running;
	}

	private ImageIcon getFailedIcon() {
		if (failed == null) {
			if (image == null) {
				return null;
			}
			failed = StatusIconHelper.makeFailedIcon(adjusted);

		}
		return failed;
	}

	private ImageIcon getCompletedIcon() {
		if (completed == null) {
			if (image == null) {
				return null;
			}
			completed = StatusIconHelper.makeCompletedIcon(adjusted);
		}
		return completed;
	}

	private ImageIcon getStoppedIcon() {
		if (stopped == null) {
			if (image == null) {
				return null;
			}
			stopped = StatusIconHelper.makeStoppedIcon(adjusted);
		}
		return stopped;
	}

	private void setIcon(ImageIcon icon) {
		this.crt = icon;
	}

	public ImageIcon getIcon() {
		return crt;
	}

	public GenericNode getNode() {
		return (GenericNode) getComponent();
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals("name") || e.getPropertyName().equals("type")) {
			Property prop = getComponent().getProperty(e.getPropertyName());
			if (prop.getPropertyClass() == String.class) {
				label = (String) prop.getValue();
			}
		}
		if (e.getPropertyName().equals("iconfile")) {
			resetIcon();
			updateIcon();
		}
		if (e.getPropertyName().equals("icon")) {
			resetIcon();
			updateIcon();
		}
		if (e.getPropertyName().equals("status")) {
			updateStatus();
		}
		if (e.getPropertyName().equals("hue") || e.getPropertyName().equals("saturation")
			|| e.getPropertyName().equals("value")) {
			updateIcon();
		}
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}


// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


/*
 * Created on Jun 24, 2003
 */
package org.globus.cog.gui.grapheditor.ant;

import java.awt.Font;
import java.awt.Image;
import java.beans.PropertyChangeEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.AbstractRenderer;
import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.gui.grapheditor.targets.swing.AbstractSwingRenderer;
import org.globus.cog.gui.grapheditor.util.graphics.ImageProcessor;
import org.globus.cog.util.ImageLoader;

public class AntRenderer extends AbstractSwingRenderer {
	private static Logger logger = Logger.getLogger(AbstractRenderer.class);
	
	private JLabel label;
	private Image image;
	private ImageIcon running, stopped, failed, completed;

	public AntRenderer() {
		//	create the rendering component
		label = new JLabel();
		label.setFont(Font.decode("Lucida Sans Regular-PLAIN-10"));
		setVisualComponent(label);
	}

	public void setComponent(GraphComponent component) {
		super.setComponent(component);
		resetIcon();
		updateIcon();
		String name = null;
		if (component.hasProperty("name")){
			name = (String) component.getPropertyValue("name");
		}
		if (name != null) {
			label.setText(name);
		}
		else {
			if (component.hasProperty("type")) {
				label.setText((String) component.getPropertyValue("type"));
			}
			else{
				label.setText(component.getComponentType());
			}
		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals("name")) {
			Property prop = getComponent().getProperty(e.getPropertyName());
			if (prop.getPropertyClass() == String.class) {
				label.setText((String) prop.getValue());
			}
		}
		else if (e.getPropertyName().equals("type")) {
			Property name = getComponent().getProperty("name");
			if (((String) name.getValue()).length() == 0) {
				Property prop = getComponent().getProperty(e.getPropertyName());
				if (prop.getPropertyClass() == String.class) {
					label.setText((String) prop.getValue());
				}
			}
		}
		else if (e.getPropertyName().equals("status")) {
			updateStatus();
		}
		else{
			super.propertyChange(e);
		}
	}

	public void updateStatus() {
		if (getComponent().hasProperty("status")) {
			int status = ((Integer) getComponent().getPropertyValue("status")).intValue();
			setStatus(status);
		}
	}

	public void setStatus(int status) {
		if (status == AntNode.STATUS_RUNNING) {
			label.setIcon(getRunningIcon());
		}
		if (status == AntNode.STATUS_STOPPED) {
			label.setIcon(getStoppedIcon());
		}
		if (status == AntNode.STATUS_FAILED) {
			label.setIcon(getFailedIcon());
		}
		if (status == AntNode.STATUS_COMPLETED) {
			label.setIcon(getCompletedIcon());
		}
	}

	public void resetIcon() {
		try {
			Icon icon = (Icon) getComponent().getPropertyValue("icon");
			image = null;
			if (icon instanceof ImageIcon) {
				ImageIcon ii = (ImageIcon) icon;
				image = ii.getImage();
				stopped = null;
				running = null;
				failed = null;
				completed = null;
			}
		}
		catch (Exception e) {
			logger.error("Something went wrong while loading the icons:");
			logger.error(e);
		}
	}

	public AntNode getNode() {
		return (AntNode) getComponent();
	}

	private void updateIcon() {
		if (image == null) {
			return;
		}
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
			running =
				new ImageIcon(
					ImageProcessor.compose(
						ImageProcessor.highlight(image, 0.5),
						ImageLoader.loadIcon("images/16x16/co/overlay-running.png").getImage()));
		}
		return running;
	}

	private ImageIcon getFailedIcon() {
		if (failed == null) {
			if (image == null) {
				return null;
			}
			failed =
				new ImageIcon(
					ImageProcessor.compose(
							image,
						ImageLoader.loadIcon("images/16x16/co/overlay-failed.png").getImage()));
		}
		return failed;
	}

	private ImageIcon getCompletedIcon() {
		if (completed == null) {
			if (image == null) {
				return null;
			}
			completed =
				new ImageIcon(
					ImageProcessor.compose(
							image,
						ImageLoader.loadIcon("images/16x16/co/overlay-completed.png").getImage()));
		}
		return completed;
	}

	private ImageIcon getStoppedIcon() {
		if (stopped == null) {
			if (image == null) {
				return null;
			}
			stopped = new ImageIcon(image);
		}
		return stopped;
	}
}

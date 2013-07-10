// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jun 24, 2003
 */
package org.globus.cog.gui.grapheditor.targets.swing;

import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.EventObject;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.RendererFactory;
import org.globus.cog.gui.grapheditor.RootContainer;
import org.globus.cog.gui.grapheditor.RootContainerInstantiationException;
import org.globus.cog.gui.grapheditor.generic.GenericNode;
import org.globus.cog.gui.grapheditor.generic.StatusIconHelper;
import org.globus.cog.gui.grapheditor.nodes.EditableNodeComponent;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.gui.grapheditor.util.graphics.ImageProcessor;
import org.globus.cog.gui.grapheditor.util.swing.ComponentAction;

public class SwingNodeRenderer extends AbstractSwingRenderer {
	private static Logger logger = Logger.getLogger(SwingNodeRenderer.class);

	private static Font font = Font.decode("Lucida Sans Regular-PLAIN-10");

	private JLabel label;

	private Image image, adjusted;

	private ImageIcon running, stopped, failed, completed;

	private ComponentAction edit;

	public SwingNodeRenderer() {
		//	create the rendering component
		label = new JLabel();
		label.setFont(font);
		setVisualComponent(label);
	}

	public void setComponent(GraphComponent component) {
		super.setComponent(component);
		if (component instanceof EditableNodeComponent) {
			edit = new ComponentAction("Edit");
			edit.addActionListener(this);
			addAction(edit);
		}
		resetIcon();
		updateIcon();
		label.setText((String) component.getPropertyValue("name"));
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals("name") || e.getPropertyName().equals("type")) {
			Property prop = getComponent().getProperty(e.getPropertyName());
			if (prop.getPropertyClass() == String.class) {
				label.setText((String) prop.getValue());
				label.setSize(label.getPreferredSize());
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

	public void updateStatus() {
		Integer status = (Integer) getComponent().getPropertyValue("status");
		if (status != null) {
			setStatus(status.intValue());
		}
	}

	public void setStatus(int status) {
		if (status == GenericNode.STATUS_RUNNING) {
			label.setIcon(getRunningIcon());
		}
		if (status == GenericNode.STATUS_STOPPED) {
			label.setIcon(getStoppedIcon());
		}
		if (status == GenericNode.STATUS_FAILED) {
			label.setIcon(getFailedIcon());
		}
		if (status == GenericNode.STATUS_COMPLETED) {
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

	public GenericNode getNode() {
		return (GenericNode) getComponent();
	}

	private void updateIcon() {
		if (image == null) {
			return;
		}
		GenericNode node = getNode();
		if ((node.getHue().doubleValue() != 0.0) || (node.getSaturation().doubleValue() != 1.0)
				|| (node.getValue().doubleValue() != 1.0)) {
			adjusted = ImageProcessor.adjustHSV(image, getNode().getHue().doubleValue(),
					getNode().getSaturation().doubleValue(), getNode().getValue().doubleValue());
		}
		else {
			adjusted = image;
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

	public NodeComponent getNodeComponent() {
		return (NodeComponent) getComponent();
	}

	public void event(EventObject e) {
		if (e instanceof ActionEvent) {
			ActionEvent ee = (ActionEvent) e;
			if (ee.getSource() == getAction("Edit")) {
				if (getNodeComponent().getCanvas() == null) {
					getNodeComponent().createCanvas();
				}
				try {
					RootContainer rootContainer = RendererFactory.newRootContainer();
					rootContainer.setRootNode(getNodeComponent());
					rootContainer.activate();
				}
				catch (RootContainerInstantiationException e1) {
					logger.error("Cannot instantiate root container.", e1);
				}
			}
		}
		super.event(e);
	}

	protected JLabel getLabel() {
		return label;
	}

	protected void setLabel(JLabel label) {
		this.label = label;
	}
}
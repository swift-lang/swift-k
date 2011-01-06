
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 16, 2003
 */
package org.globus.cog.gui.grapheditor.ant;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.targets.swing.AbstractSwingRenderer;

public class IconRenderer extends AbstractSwingRenderer {
	private static Logger logger = Logger.getLogger(IconRenderer.class);
	
	private JLabel label;
	private Icon icon;

	public IconRenderer() {
		//	create the rendering component
		label = new JLabel();
		setVisualComponent(label);
	}

	public void setComponent(GraphComponent component) {
		super.setComponent(component);
		updateIcon();
	}

	public void updateIcon() {
		try {
			icon = (Icon) getComponent().getPropertyValue("icon");
			label.setIcon(icon);
		}
		catch (Exception e) {
			logger.error("Something went wrong while loading the icons:");
			logger.error(e);
		}
	}
}

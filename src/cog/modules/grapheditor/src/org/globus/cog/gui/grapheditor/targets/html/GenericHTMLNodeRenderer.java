
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.targets.html;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.StreamRenderer;
import org.globus.cog.gui.grapheditor.generic.AbstractGenericNodeRenderer;

public class GenericHTMLNodeRenderer extends AbstractGenericNodeRenderer implements StreamRenderer {
	private static Logger logger = Logger.getLogger(GenericHTMLNodeRenderer.class);

	public GenericHTMLNodeRenderer() {
	}

	public void render(Writer wr) throws IOException {
		if (!getRootNode().hasProperty("_html.node.renderer.index")) {
			getRootNode().setPropertyValue("_html.node.renderer.index", new Integer(0));
		}
		if (!getRootNode().hasProperty("_html.node.renderer.map")) {
			getRootNode().setPropertyValue("_html.node.renderer.map", new HashMap());
		}
		HashMap icons = (HashMap) getRootNode().getPropertyValue("_html.node.renderer.map");
		int iconIndex = ((Integer) getRootNode().getPropertyValue("_html.node.renderer.index"))
			.intValue();
		String iconName;
		if (icons.containsKey(getIcon().getImage())) {
			iconName = (String) icons.get(getIcon().getImage());
		}
		else {
			iconName = "icon" + iconIndex++ + ".png";
			icons.put(getIcon().getImage(), iconName);
			String outputDir = (String) getRootNode().getPropertyValue("html.outputdir");
			ImageIO.write((BufferedImage) getIcon().getImage(), "png",
				new File(outputDir, iconName));
		}
		wr.write("<img align=\"middle\" src=\"" + iconName + "\">&nbsp;");
		wr.write(getLabel());
		getRootNode().setPropertyValue("_html.node.renderer.index", new Integer(iconIndex));
	}

}

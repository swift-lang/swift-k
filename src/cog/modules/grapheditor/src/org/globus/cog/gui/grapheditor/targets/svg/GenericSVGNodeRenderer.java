
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.targets.svg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.StreamRenderer;
import org.globus.cog.gui.grapheditor.generic.AbstractGenericNodeRenderer;
import org.globus.cog.gui.grapheditor.targets.swing.SwingNodeRenderer;

public class GenericSVGNodeRenderer extends AbstractGenericNodeRenderer implements StreamRenderer{
	private static Logger logger = Logger.getLogger(SwingNodeRenderer.class);
	
	public GenericSVGNodeRenderer() {
	}

	public void render(Writer wr) throws IOException{
		if (!getRootNode().hasProperty("_svg.node.renderer.index")) {
			getRootNode().setPropertyValue("_svg.node.renderer.index", new Integer(0));
		}
		if (!getRootNode().hasProperty("_svg.node.renderer.map")) {
			getRootNode().setPropertyValue("_svg.node.renderer.map", new HashMap());
		}
		HashMap icons = (HashMap) getRootNode().getPropertyValue("_svg.node.renderer.map");
		int iconIndex = ((Integer) getRootNode().getPropertyValue("_svg.node.renderer.index")).intValue();
		String iconName;
		if (icons.containsKey(getIcon().getImage())) {
			iconName = (String) icons.get(getIcon().getImage());
		}
		else {
			iconName = "icon"+iconIndex+++".png";
			icons.put(getIcon().getImage(), iconName);
			String outputDir = (String) getRootNode().getPropertyValue("svg.outputdir");
			ImageIO.write((BufferedImage)getIcon().getImage(), "png", new File(outputDir, iconName));
		}
		wr.write("<image x=\"0\" y=\"0\" width=\""+getIcon().getIconWidth()+"px\" height=\""+getIcon().getIconHeight()+"px\" xlink:href=\""+iconName+"\">\n");
		wr.write("<title>");wr.write(getLabel());wr.write("</title>\n");
		wr.write("</image>\n");
		wr.write("<text x=\""+(getIcon().getIconWidth()+4)+"\" y=\""+(getIcon().getIconHeight()-2)+"\" font-family=\"Helvetica\" font-size=\"9\">\n");
		wr.write(getLabel()+"\n");
		wr.write("</text>\n");
		getRootNode().setPropertyValue("_svg.node.renderer.index", new Integer(iconIndex));
	}
}

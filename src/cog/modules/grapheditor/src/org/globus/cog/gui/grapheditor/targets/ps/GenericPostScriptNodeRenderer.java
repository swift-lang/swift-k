
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.targets.ps;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.StreamRenderer;
import org.globus.cog.gui.grapheditor.generic.AbstractGenericNodeRenderer;
import org.globus.cog.gui.grapheditor.targets.swing.SwingNodeRenderer;
import org.globus.cog.gui.grapheditor.util.HexUtil;

public class GenericPostScriptNodeRenderer extends AbstractGenericNodeRenderer implements StreamRenderer{
	private static Logger logger = Logger.getLogger(SwingNodeRenderer.class);
	private  HashMap icons;

	public GenericPostScriptNodeRenderer() {
	}

	public void render(Writer wr) throws IOException{
		Integer icon;
		if (getRootNode().hasProperty("postscript.iconcache")) {
			icons = (HashMap) getRootNode().getPropertyValue("postscript.iconcache");
		}
		else {
			icons = new HashMap();
			getRootNode().setPropertyValue("postscript.iconcache", icons);
		}
		if (icons.containsKey(getIcon().getImage())) {
			icon = (Integer) icons.get(getIcon().getImage());
		}
		else {
			int iconIndex;
			if (getRootNode().hasProperty("postscript.iconindex")) {
				Integer iconIndexO = (Integer) getRootNode().getPropertyValue("postscript.iconindex");
				iconIndex = iconIndexO.intValue();
			}
			else {
				getRootNode().setPropertyValue("postscript.iconindex", new Integer(0));
				iconIndex = 0;
			}
			icon = new Integer(iconIndex++);
			icons.put(getIcon().getImage(), icon);
			BufferedImage im = (BufferedImage) getIcon().getImage();
			wr.write("/icon"+icon.toString()+" {"+im.getWidth()+" "+im.getHeight()+" 8 ["+im.getWidth()+" 0 0 "+(-im.getHeight())+" 0 "+im.getHeight()+"]\n");
			wr.write("{<\n");
			for (int i = 0; i < im.getHeight(); i++) {
				for (int j = 0; j <im.getWidth(); j++) {
					int val = im.getRGB(j, i);
					int alpha = (val >> 24) & 0x000000ff;
					//shorter to change alpha than my 3 mistakes below
					alpha = 255 - alpha;
					int r = (val) & 0x000000ff;
					int g = (val >> 8) & 0x000000ff;
					int b = (val >> 16) & 0x000000ff;
					r = (r*(255-alpha)+255*alpha) / 256;
					g = (g*(255-alpha)+255*alpha) / 256;
					b = (b*(255-alpha)+255*alpha) / 256;
					val = (b << 16) + (g << 8) + r; 
					wr.write(HexUtil.hex24(val));
				}
				wr.write("\n");
			}
			wr.write(">}\n");
			wr.write("false 3 colorimage}\n");
			wr.write("def\n");
			getRootNode().setPropertyValue("postscript.iconindex", new Integer(iconIndex));
		}
		wr.write("gsave "+getIcon().getIconWidth()+" "+getIcon().getIconHeight()+" scale\n");
		wr.write("icon"+icon.toString()+"\n");
		wr.write("grestore "+getIcon().getIconWidth()+" 4 moveto\n");
		wr.write("("+getLabel()+") show\n");
	}
	
	public Dimension getSize() {
		//??? 
		//well, it's better than nothing
		if (getLabel() != null) {
			return new Dimension(getIcon().getIconWidth()+getLabel().length()*8, getIcon().getIconHeight());
		}
		else {
			return new Dimension(getIcon().getIconWidth(), getIcon().getIconHeight());
		}
	}
}

// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 27, 2004
 */
package org.globus.cog.karajan.viewer;

import java.io.IOException;
import java.io.Writer;

import org.globus.cog.gui.grapheditor.targets.html.GenericHTMLNodeRenderer;

public class KarajanHTMLNodeRenderer extends GenericHTMLNodeRenderer {

	public KarajanHTMLNodeRenderer() {
		super();
	}

	public void render(Writer wr) throws IOException {
		if (getComponent().getComponentType().equals("gridTransfer")) {
			Long total = (Long) getComponent().getPropertyValue(KarajanNode.TOTAL);
			Long crt = (Long) getComponent().getPropertyValue(KarajanNode.CURRENT);
			if ((total != null) && (crt != null) && (total.longValue() > 0)) {
				int percent = (int) (crt.longValue() * 100 / total.longValue());
				wr.write("<table width=\"50px\" height=\"5px\" border=\"1\" bordercolor=\"#404040\" cellspacing=\"0\" cellpadding=\"0\">\n");
				wr.write("<tr><td width=\""+percent+"px\" bgcolor=\"#30a0ff\"/>&nbsp;</td><td>&nbsp;</td></tr></table><br>");
			}
		}
		super.render(wr);
	}
}
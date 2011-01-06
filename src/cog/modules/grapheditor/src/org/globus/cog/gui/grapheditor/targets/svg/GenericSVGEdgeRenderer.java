
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

import java.awt.Point;
import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.RootContainer;
import org.globus.cog.gui.grapheditor.StreamRenderer;
import org.globus.cog.gui.grapheditor.edges.EdgeRenderer;

public class GenericSVGEdgeRenderer extends EdgeRenderer implements StreamRenderer {
	private static Logger logger = Logger.getLogger(GenericSVGEdgeRenderer.class);
	private RootContainer cachedRootContainer;

	public void render(Writer wr) throws IOException {
		Point pf = getEdge().getControlPoint(0);
		Point pt = getEdge().getControlPoint(1);
		int l = (int) pf.distance(pt);
		double alpha = Math.atan2(pt.y - pf.y, pt.x - pf.x)*180/Math.PI;
		wr.write("<g transform=\"translate("+pf.x+", "+pf.y+")\">\n");
		wr.write("	<g transform=\"rotate("+(int)alpha+")\">\n");
		line(wr, 0, 0, l, 0);
		line(wr, l-5, -5, l, 0);
		line(wr, l-5, 5, l, 0);
		wr.write("	</g>\n");
		wr.write("</g>\n");
	}
	
	private void line(Writer wr, int x1, int y1, int x2, int y2) throws IOException {
		wr.write("		<line x1=\""+x1+"\" y1=\""+y1+"\" x2=\""+x2+"\" y2=\""+y2+"\" stroke-width=\"1\"/>\n");
	}

}
